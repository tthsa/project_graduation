# 开发日志 - 2026-05-12 - 评分规则可配置化

## 背景

评分链路原本写死: `test_score` 来自通过率 ×100, `llm_score` 来自单维度"代码质量"prompt 提取的 0-10 分。两者独立存两列、从不汇总, 老师无法干预评分逻辑。

本次让评分规则下沉到每个 `homework` 的配置, 评测时系统按配置动态计算综合分与等级。粒度 = 每个 homework 一套, 旧数据不回填(NULL 显示 `-`)。

## 三阶段实现

### 阶段 1 — schema + 实体 + Mapper 基础设施

#### DB schema

`homework` 新增 6 列(全部带 DEFAULT, 配合 `ddl-auto: update` 平滑兼容旧数据):

| 列 | 类型 | 默认值 |
|---|---|---|
| `test_weight` | INTEGER | 70 |
| `llm_weight` | INTEGER | 30 |
| `grade_a_threshold` | INTEGER | 90 |
| `grade_b_threshold` | INTEGER | 75 |
| `grade_c_threshold` | INTEGER | 60 |
| `llm_dimensions` | TEXT (JSON) | NULL |

`evaluation_result` 新增 3 列:

| 列 | 类型 | 默认值 |
|---|---|---|
| `final_score` | INTEGER | NULL |
| `grade` | VARCHAR(2) | NULL |
| `llm_dimension_scores` | TEXT (JSON) | NULL |

`llm_dimensions` JSON 形如 `[{"name":"代码质量","weight":50},{"name":"可读性","weight":30},{"name":"性能","weight":20}]`(各 weight 和=100); NULL = 默认单维度。

#### 后端

- `entity/Homework.java`、`entity/EvaluationResult.java` 加对应字段
- `HomeworkMapper.insert/update` + `EvaluationResultMapper.insert` SQL 扩列

### 阶段 2 — 权重综合分 + 等级映射

#### 新增 `service/ScoringService.java`

纯函数, 三个核心方法:

- `computeFinalScore(testScore, llmScore, Homework)` → 0-100 综合分
  - `llmScore == null` → `final = testScore` (LLM 缺失只算测试)
  - `testScore == null` → `final = llmScore × 10` (无测试用例只算 LLM)
  - 两者都有 → `test × test_weight/100 + llm×10 × llm_weight/100`, clamp `[0,100]`
- `computeGrade(finalScore, Homework)` → `A/B/C/D`, finalScore=null 时返 null
- `orDefault(...)` 兜底: 配置缺失时落回常量 (70/30/90/75/60)

#### 修改

- `controller/HomeworkController` `create/update` 加 `validateScoringConfig`:
  - 测试权重 + LLM 权重必须同时设置且和 = 100
  - 三个等级阈值必须同时设置且满足 A > B > C, 范围 0-100
  - 失败返 `Result.fail(400, msg)`, 前端 el-message 弹错误
  - 同时加 `JwtUtils + HomeworkOwnershipService` 校验 (`update/delete` 只允许作业归属老师操作)
- `service/TaskProcessorService.processTask` 评测后调 `ScoringService` 算 `finalScore` / `grade` 并写库
- 前端 `views/teacher/HomeworkView.vue` 弹窗加 `el-collapse` "评分配置(可选)" 面板:
  - 2 个 `el-input-number` (test/llm 权重)
  - 3 个 `el-input-number` (A/B/C 阈值)
  - 三个 `validator` 函数同步校验权重和、阈值序、范围
- 前端 `api/homework.ts` `Homework / AddHomeworkParams / UpdateHomeworkParams` 加 6 个字段
- 前端 `api/report.ts` `EvaluationResult` 加 `finalScore: number | null` / `grade: 'A'|'B'|'C'|'D'|null` / `llmDimensionScores: string | null`
- 三个展示视图加列(均带 `getGradeType` 把 A/B/C/D 映射到 `success/warning/info/danger`):
  - `views/student/SubmissionDetailView.vue` — 综合分 + 等级 两行
  - `views/student/SubmissionsView.vue` — 综合分 + 等级 两列
  - `views/teacher/ReviewView.vue` — 综合分/等级 列表两列 + 详情两行 (列表 `scoreMap` 预拉取, 避免每行单独请求)

### 阶段 3 — LLM 评分维度自定义

#### 新增

- `dto/LlmDimension.java` (`name: String, weight: Integer`) — 老师配置
- `dto/DimensionScore.java` (`name: String, score: Integer`) — LLM 提取结果, score=null 表示该维度未提取到

#### 修改

- `service/LlmReviewService`:
  - 新增 `DEFAULT_DIMENSIONS = [LlmDimension("代码质量", 100)]` 默认单维度兜底
  - 新增重载 `reviewCode(submissionId, result, dimensions)`
  - `buildReviewContent` 改为接受 `List<LlmDimension>`, prompt 按维度列举评分要求
  - 新增 `extractDimensionScores(review, dimensions)` 用 `Pattern.quote(dimName)` 安全转义维度名(老师可写"代码 / 性能"等含正则元字符的名字), 兼容半角/全角冒号与 `X分` / `X/10` 两种格式; 提取失败该维度记 null, 调用方降级
  - 旧的 `extractScore` 保留作为单维度 fallback
- `service/ScoringService`:
  - 新增 `aggregateLlmScore(scores, dimensions)`: 按权重加权平均, 部分 score=null 时按剩余权重归一化, 全 null 返 null
  - 新增 `parseDimensions(json)`: 解析失败/空时回退到 `LlmReviewService.DEFAULT_DIMENSIONS`
  - 新增 `serializeDimensionScores(scores)`: 写库前序列化, 失败返 null
- `controller/HomeworkController.validateScoringConfig` 扩展校验 `llmDimensions` JSON:
  - 必须是 JSON 数组, ≤5 项
  - 每项 `name` 非空、`weight` 在 0-100
  - 权重之和必须 = 100
- `service/TaskProcessorService.processTask` 串起整条链路:
  ```java
  List<LlmDimension> dimensions = scoringService.parseDimensions(homework.getLlmDimensions());
  llmReview = llmReviewService.reviewCode(submissionId, result, dimensions);
  List<DimensionScore> dimScores = llmReviewService.extractDimensionScores(llmReview, dimensions);
  llmScore = scoringService.aggregateLlmScore(dimScores, dimensions);
  dimensionScoresJson = scoringService.serializeDimensionScores(dimScores);
  if (llmScore == null) {
      llmScore = llmReviewService.extractScore(llmReview); // 兼容旧单维度
  }
  ```
- 前端 `views/teacher/HomeworkView.vue` 加动态维度列表 UI:
  - 默认 1 行"代码质量 / 100"
  - 增删按钮、最多 5 项
  - 实时权重和回显: =100 绿、≠100 红
  - `validateDimensions` validator 校验 (数量、名称、权重、和)
- 前端 `views/student/SubmissionDetailView.vue` 加"LLM 各维度评分"卡片:
  - 计算属性 `dimensionScores` 解析 `evaluation.llmDimensionScores` JSON
  - 用 `el-table` 渲染维度名 + 得分
  - 维度未提取到时显示斜体"未提取到"

## 验证

- 后端: `cd backend && ./mvnw -q -DskipTests compile` — 零输出通过
- 前端: `cd frontend && npx vue-tsc --noEmit` — 零输出通过
- **兼容**: 旧作业(配置为 NULL)的旧提交 final_score/grade 显示 `-`, 不影响 LLM 评分维度展示卡片(其在 `llmDimensionScores` 为空时根本不渲染)
- **容错**: LLM 调用失败 → llmScore=null → ScoringService 兜底 `final = testScore`, 等级仍能算出
- **维度部分失败**: 某维度未提取到 → DimensionScore.score=null → aggregateLlmScore 按剩余权重归一化, 不会让一个维度提取失败拖垮整体打分

## 设计选择 (备忘)

1. **JSON 用 String 存** — `llm_dimensions / llm_dimension_scores` 走 String 列, Service 层 ObjectMapper 来回转, 不引入 MyBatis TypeHandler。配置类小数据没必要上 TypeHandler。
2. **维度名正则安全** — `Pattern.quote(dimName)` 转义, 老师可写"代码 / 性能"这种含正则元字符的名字
3. **Hibernate `ddl-auto: update` + 全 DEFAULT** — ALTER 后旧行自动填默认值, 无需数据迁移
4. **双层默认** — `ScoringService` 内嵌默认权重/阈值常量, `LlmReviewService.DEFAULT_DIMENSIONS` 内嵌单维度("代码质量"×100), 缺配置时全链路有默认
5. **教师所有权校验** — `HomeworkController.update/delete` 复用 `HomeworkOwnershipService.isHomeworkOwnedBy`, 防止老师 A 改老师 B 的作业评分规则
6. **算法在 ScoringService 里完全纯函数化** — 没有任何 Mapper/外部依赖, 便于以后加单元测试

## 仍未处理 / 未来工作

- ScoringService 没写单测 — 算法逻辑较多边界(单边 null、权重和兜底、等级阈值同值), 写一组 JUnit 能让回归更安心
- LLM prompt 长度上界 — 5 个维度 × 短描述 + 测试结果在当前 SiliconFlow 模型 context 内安全, 但若以后允许更多维度需要做截断
- 前端"评分配置(可选)"面板的展开/折叠状态没有持久化, 老师每次打开弹窗都是折叠态(目前是有意为之, 避免对默认配置无感知)

## 涉及文件清单

**后端新增 (3)**:
- `backend/src/main/java/com/javaevaluation/dto/LlmDimension.java`
- `backend/src/main/java/com/javaevaluation/dto/DimensionScore.java`
- `backend/src/main/java/com/javaevaluation/service/ScoringService.java`

**后端修改 (7)**:
- `backend/src/main/java/com/javaevaluation/entity/Homework.java`
- `backend/src/main/java/com/javaevaluation/entity/EvaluationResult.java`
- `backend/src/main/java/com/javaevaluation/mapper/HomeworkMapper.java`
- `backend/src/main/java/com/javaevaluation/mapper/EvaluationResultMapper.java`
- `backend/src/main/java/com/javaevaluation/controller/HomeworkController.java`
- `backend/src/main/java/com/javaevaluation/service/LlmReviewService.java`
- `backend/src/main/java/com/javaevaluation/service/TaskProcessorService.java`

**前端修改 (6)**:
- `frontend/src/api/homework.ts`
- `frontend/src/api/report.ts`
- `frontend/src/views/teacher/HomeworkView.vue`
- `frontend/src/views/student/SubmissionDetailView.vue`
- `frontend/src/views/student/SubmissionsView.vue`
- `frontend/src/views/teacher/ReviewView.vue`

**DB / 脚本 (2)**:
- `scripts/db/migrations/01_scoring_rules.sql` (新增)
- `scripts/db/current_schema.sql` (同步)
