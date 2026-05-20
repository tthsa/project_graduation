# 工作日志 - 2026-05-13 - 代码审查与简化

## 概述

对 Java 作业评测系统进行全面的代码审查和冗余代码清理，删除死代码、提取公共方法、消除重复逻辑、统一代码风格。前后端编译均通过。

---

## 一、删除死代码（6 个文件）

### 1. `frontend/src/stores/counter.ts`

**原因**: 未被任何文件引用。内容是早期版本的 user store，功能已被 `stores/user.ts` 完全覆盖，且 `user.ts` 已实现 localStorage 持久化等更完整的功能。

### 2. `backend/.../entity/EvaluationTask.java`

**原因**: 死实体。数据库 `evaluation_task` 表 0 条数据，且未被任何业务代码调用。Entity 字段与真实表结构也不一致（`status` 类型错位：表是 integer，Entity 是 String）。

### 3. `backend/.../mapper/EvaluationTaskMapper.java`

**原因**: 死 Mapper，与 EvaluationTask 配套。`findByStatus("PENDING")` 因类型错位永远拿不到记录。

### 4. `backend/.../properties/ReviewProperties.java`

**原因**: 死配置类。定义了 RabbitMQ 和结果配置，但 `RabbitMQConfig.java` 完全使用硬编码常量，从未引用此配置类。

### 5. `backend/.../resources/mapper/SubmissionFileMapper.xml`

**原因**: 死 XML 文件。引用了已不存在的 `file_path` 字段（真实表字段是 `file_name/file_content/file_order`）。`SubmissionFileMapper` 是注解式 Mapper，此 XML 不会被加载。

### 6. `backend/.../controller/LlmReviewController.java`

**原因**: 只有一个 `/api/llm/extract-score` 调试接口，无权限保护。生产环境暴露此接口存在安全风险，LLM 分数提取已在 `TaskProcessorService` 中完整集成。

---

## 二、提取公共方法

### 2.1 `JwtUtils` - 新增 `getUserIdFromHeader()` / `getUserTypeFromHeader()`

**位置**: `backend/.../security/JwtUtils.java`

**问题**: 3 个 Controller（`HomeworkController`、`SubmissionController`、`TeacherStatsController`）中有完全相同的 JWT 解析逻辑：

```java
if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
String token = authHeader.substring(7);
if (!jwtUtils.validateToken(token)) return null;
return jwtUtils.getUserIdFromToken(token);
```

**改动**: 将上述逻辑提取到 `JwtUtils` 的两个公共方法中：
- `getUserIdFromHeader(String authHeader)` → `Integer`
- `getUserTypeFromHeader(String authHeader)` → `String`

**收益**: 消除 3 处重复，共节省约 25 行代码。后续新增 Controller 可直接复用。

---

## 三、Controller 简化

### 3.1 `HomeworkController` / `TeacherStatsController`

**改动**: `currentUserId()` / `currentTeacherId()` 私有方法改为直接调用 `jwtUtils.getUserIdFromHeader(authHeader)`。

### 3.2 `SubmissionController`

**改动**:
- 删除私有的 `currentUserId()` 方法（3 处调用全部替换为 `jwtUtils.getUserIdFromHeader()`）
- `canAccessSubmission()` 简化为直接调用 `jwtUtils.getUserTypeFromHeader()` 和 `jwtUtils.getUserIdFromHeader()`，避免重复解析 token

### 3.3 `AuthController`

**改动**: `getCurrentUser()` 中 `authHeader.replace("Bearer ", "")` 改为 `startsWith("Bearer ") + substring(7)`。

**原因**: `replace` 会替换 token 内容中所有 `"Bearer "` 子串，存在潜在 bug。`JwtFilter` 早已使用 `startsWith + substring`，现在统一风格。

---

## 四、Service 简化

### 4.1 `CodeSubmitService.submitHomework()`

**问题**: `if/else` 两个分支都执行了相同的操作：创建文件记录 + 循环插入 + 发送评测任务。重复代码约 15 行。

**改动**: 将公共逻辑（文件插入 + 发 MQ）提到 `if/else` 之后，两个分支只处理差异化的部分（新建 vs 复用提交记录）。

### 4.2 `ResultService`

**改动**: 删除未使用的 `SubmissionMapper` 注入。`ResultService` 只有 `getResult()` 和 `evictCache()` 两个方法，均只用到 `EvaluationResultMapper`。

---

## 五、异常处理合并

### 5.1 `GlobalExceptionHandler`

**问题**: `handleValidationException(MethodArgumentNotValidException)` 和 `handleBindException(BindException)` 两个方法的实现代码完全相同（4 行）。

**改动**: 合并为单个 handler：

```java
@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
public Result<Void> handleValidationException(Exception e) { ... }
```

---

## 六、统计查询优化

### 6.1 `AdminStatsController`

**问题**: 使用 `teacherMapper.findAll().size()` 等方式统计数量，会将全表数据加载到内存再计数。

**改动**:
- `AdminStatsController` 改为调用各 Mapper 的 `count()` 方法
- 给 `TeacherMapper`、`StudentMapper`、`CourseMapper`、`HomeworkMapper` 各新增 `count()` 方法（`SELECT COUNT(*)`）

**收益**: 避免全表加载，数据库只返回一个数字。

---

## 七、前端清理

### 7.1 提取公共工具函数 `formatTime`

**问题**: 3 个 Vue 文件各自定义了相同的 `formatTime()` 函数：
- `views/teacher/ReviewView.vue`
- `views/student/IndexView.vue`
- `views/teacher/IndexView.vue`

**改动**: 新增 `frontend/src/utils/format.ts`，统一导出 `formatTime()`。三个 Vue 文件删除本地定义，改为 import。

### 7.2 统一 `Submission` 类型定义

**问题**: `api/submission.ts` 和 `api/report.ts` 各自定义了相同的 `Submission` 接口。

**改动**: `api/submission.ts` 改为从 `report.ts` re-export：`export type { Submission } from './report'`。

### 7.3 修正页面标题和系统名称

**改动**:
- `router/index.ts`: 默认标题 `"毕业设计管理系统"` → `"Java 作业评测系统"`
- `views/admin/IndexView.vue`: 系统信息卡片中的名称同步修改

---

## 八、其他清理

### 8.1 `BackendApplication.java`

**改动**: 删除 `@EnableScheduling`。

**原因**: 整个项目中没有任何 `@Scheduled` 方法，此注解是多余的。

---

## 九、验证

### 后端编译

```bash
cd backend && ./mvnw -q -DskipTests compile
```

**结果**: 零输出通过（无编译错误）。

### 前端类型检查

```bash
cd frontend && npx vue-tsc --noEmit
```

**结果**: 零输出通过（无类型错误）。

---

## 十、改动统计

| 类别 | 数量 |
|------|------|
| 删除文件 | 6 个 |
| 修改文件 | 20 个 |
| 新增文件 | 1 个 (`frontend/src/utils/format.ts`) |
| 删除/简化代码行数 | 约 530 行 |

### 涉及文件清单

**后端修改 (14)**:
- `BackendApplication.java`
- `AdminStatsController.java`
- `AuthController.java`
- `HomeworkController.java`
- `SubmissionController.java`
- `TeacherStatsController.java`
- `GlobalExceptionHandler.java`
- `JwtUtils.java`
- `CodeSubmitService.java`
- `ResultService.java`
- `TeacherMapper.java`
- `StudentMapper.java`
- `CourseMapper.java`
- `HomeworkMapper.java`

**后端删除 (5)**:
- `LlmReviewController.java`
- `EvaluationTask.java`
- `EvaluationTaskMapper.java`
- `ReviewProperties.java`
- `SubmissionFileMapper.xml`

**前端修改 (7)**:
- `router/index.ts`
- `views/admin/IndexView.vue`
- `views/student/IndexView.vue`
- `views/teacher/IndexView.vue`
- `views/teacher/ReviewView.vue`
- `api/submission.ts`

**前端删除 (1)**:
- `stores/counter.ts`

**前端新增 (1)**:
- `utils/format.ts`
