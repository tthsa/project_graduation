# 开发日志 - 2026-05-15 - 评测触发改造:自动 → 教师手动

## 背景

评测链路原本是「学生提交即评测」:`CodeSubmitService.submitHomework()` 在文件写入后立即调用
`sendEvaluationTask(submission)` 把 `CodeTask` 推进 RabbitMQ,`TaskConsumer` 自动消费触发评测。
本次改为「教师手动控制评测时机」:学生提交只入库 (status=0),教师在评审页点按钮才发 MQ。

确认的边界:
- **失败 (status=3) 可重评,已完成 (status=2) 不可重评**
- **需要一键「评测全部待评测」批量按钮**

## 改动

### 一、后端

#### 1.1 `service/CodeSubmitService.java` — 移除自动发 MQ

- 删除 `submitHomework()` 末尾的 `sendEvaluationTask(submission);` 调用
- 删除整个私有方法 `sendEvaluationTask(Submission)`(逻辑迁移到新 Service)
- 移除依赖注入 `TaskQueueService` / `HomeworkMapper` 以及对应 import
- 学生提交后状态保持 0(待评测),不再自动转入 1(评测中)

#### 1.2 新增 `service/EvaluationService.java` — 评测触发入口

```java
@Service
public class EvaluationService {
    private final SubmissionMapper submissionMapper;
    private final HomeworkMapper homeworkMapper;
    private final TaskQueueService taskQueueService;

    public boolean triggerOne(Integer submissionId) {...}      // 单条触发
    public Map<String,Integer> triggerBatch(Integer hwId) {...} // 批量触发
    private boolean triggerIfEligible(Submission s) {...}       // 状态校验 + 发 MQ
    private void sendTaskForSubmission(Submission s) {...}      // 构造 CodeTask
}
```

**状态规则**(在 `triggerIfEligible` 中):
- status ∈ {0, 3} → 发 MQ + 立即把 status 设为 1,return true
- status == 1 → 跳过(已在评测),return false
- status == 2 → 跳过(已完成不可重评),return false
- submission == null → return false

**为什么发 MQ 后立即把 status 设为 1**: 用户点完按钮就期望看到"评测中",而 MQ 消费者执行第一行 `updateSubmissionStatus(1)` 之间可能有几秒延迟(队列堆积时更长)。所以在 trigger 层先抢着设一次,消费者那次 update 是冗余但无害。

#### 1.3 新增 `controller/EvaluationController.java`

路径前缀 `/api/teacher/evaluation/*`,自动被 SecurityConfig 中 `/api/teacher/**` 规则保护(教师/管理员)。

```
POST /api/teacher/evaluation/trigger/{submissionId}   单条触发
POST /api/teacher/evaluation/trigger-batch            批量触发(body: {homeworkId})
```

**权限校验**:
- 拿 `teacherId = jwtUtils.getUserIdFromHeader(authHeader)`
- 单条: 先取 submission → 拿其 homeworkId → `HomeworkOwnershipService.isHomeworkOwnedBy(homeworkId, teacherId)`
- 批量: 直接 `isHomeworkOwnedBy(body.homeworkId, teacherId)`
- 未通过返 `Result.fail(ErrorCode.FORBIDDEN)`
- 状态被拒(triggerOne 返 false)返 `Result.fail(ErrorCode.BAD_REQUEST, "当前状态不允许评测(评测中或已完成)")`

#### 1.4 未改动的部分

- `TaskConsumer` / `TaskProcessorService` 完全不动。消费者仍在 `processTask` 第一步 `updateSubmissionStatus(1)`,与新流程的 trigger 层 update 重复但幂等。
- `SubmissionMapper` 无新增方法,批量触发用现有 `findByHomeworkId(hwId)` + Service 内过滤。
- `SecurityConfig` 无改动,新接口被现有 `/api/teacher/**` 规则覆盖。

---

### 二、前端

#### 2.1 新增 `api/evaluation.ts`

```typescript
export function triggerEvaluation(submissionId: number): Promise<null>
export function triggerBatchEvaluation(homeworkId: number): Promise<{triggered, skipped}>
```

#### 2.2 `views/teacher/ReviewView.vue` — 评审页加按钮

**表头**:
- 加「评测全部待评测 (N)」主按钮,N 为待触发条数。`disabled = !hasPending || batchLoading`
- 加「刷新」按钮,触发评测后用户可手动拉新状态
- 「返回」按钮保留

**操作列**(从 width 120 扩到 220):
| status | 按钮 |
|---|---|
| 0 (待评测) | 「开始评测」(primary) + 「查看详情」 |
| 1 (评测中) | 「评测中...」(disabled) + 「查看详情」 |
| 2 (完成) | 「查看详情」 |
| 3 (失败) | 「重新评测」(warning) + 「查看详情」 |

**新增 state**:
- `batchLoading: ref<boolean>` — 批量按钮 loading
- `itemLoading: ref<Record<number, boolean>>` — 每行按钮独立 loading
- `pendingCount: computed` — `status === 0 || status === 3` 的条数
- `hasPending: computed` — `pendingCount > 0`

**新增方法**:
- `handleTriggerOne(row)`: 调 `triggerEvaluation(row.id)` → toast「已开始评测」→ `fetchSubmissions()`
- `handleTriggerBatch()`: 调 `triggerBatchEvaluation(homeworkId)` → toast「已触发 N 条,跳过 M 条」→ `fetchSubmissions()`

**样式**: 新增 `.header-actions { display: flex; gap: 8px }` 包住三个表头按钮。

---

## 状态机变化

**旧**:
```
学生提交 → status=0 (瞬时) → 自动发 MQ → status=1 → status=2 或 3
```

**新**:
```
学生提交 → status=0 (停留,等教师)
教师点「开始评测」/「评测全部待评测」
   → trigger 层立即 status=1 + 发 MQ
   → TaskConsumer 消费 → status=2 或 3
status=3 → 教师可点「重新评测」(→1→2/3)
status=2 → 锁定,操作列只剩「查看详情」
```

## 设计选择(备忘)

1. **新建 EvaluationController 而非合并到 SubmissionController**:`SubmissionController` 在 `/api/student/submission/*` 下,学生身份;教师触发的接口应在 `/api/teacher/*` 下,职责分离。

2. **批量触发不开新 Mapper**:用现有 `findByHomeworkId` + Service 层过滤 status。这个表查询不是热路径,N 通常 < 100。

3. **并发安全**: 若两个教师同时点同一作业的批量,两次都可能通过 status 检查(0→1)。但 RabbitMQ task_queue 已串行消费,第二次只是让消费者多跑一次评测,结果一致。这里不加分布式锁,简单起见接受重复触发的开销。

4. **trigger 后立即 status=1**: 与 TaskProcessor 的 `updateSubmissionStatus(1)` 冗余,但用户体验上更连贯(点完即变状态)。冗余 update 是幂等的,无副作用。

5. **不动 evaluation_result 表/Mapper**: 重评失败提交时,旧 evaluation_result 行保留,新行 insert,展示用 `ORDER BY created_at DESC LIMIT 1` 兜住(FIX_LOG_2026-05-12 已确认)。

## 验证

- **后端编译**: `cd backend && ./mvnw -q -DskipTests compile` — 零输出通过
- **前端类型检查**: `cd frontend && npx vue-tsc --noEmit` — 零输出通过
- **端到端**(需重启后端):
  - 学生提交一份新代码 → DB 查 `submission.status` 应为 `0`,`evaluation_result` **不应**新增行
  - 教师进对应作业评审页 → 表格新提交那行操作列显示「开始评测」
  - 点「开始评测」→ 状态 0→1,几秒后→2 或 3
  - 多提交几份 → 表头「评测全部待评测 (N)」按钮 N 实时反映 → 点击 → 全部 0/3 转 1
  - SQL `UPDATE submission SET status=3 WHERE id=X` 模拟失败 → 评审页该行出现「重新评测」按钮 → 可重评
  - status=2 的提交在评审页只显示「查看详情」,无重评按钮
- **权限**: 教师 A 用 token 调用教师 B 名下作业的 `/trigger/{id}` 应返 403

## 仍未处理 / 未来工作

- **评测中无自动轮询**: 教师点完「开始评测」后,只能手动点「刷新」拉最新状态。可加 `setInterval` 5s 一次,仅在 `submissionList` 含 status=1 时启动,清空后停止。
- **失败原因不可见**: status=3 时教师只看到「失败」标签,具体错误(编译/超时/沙箱崩溃)需要查 `evaluation_result.llm_review` 或后端日志。可在重评按钮旁加 hover tooltip 显示最新一条评测的 review。
- **批量触发的并发优化**: 当前批量是循环调 sendTask,N 大时(>50)会有循环开销。可改用 `convertAndSend` 批量发送,但 RabbitMQ Java client API 是单条的,优化收益有限。
- **EvaluationService 没写单测**: 状态校验逻辑(0/3 通,1/2 拒)和批量 triggered/skipped 统计是核心逻辑,写一组 JUnit 能让回归更安心。

## 涉及文件清单

**后端新增 (2)**:
- `backend/src/main/java/com/javaevaluation/service/EvaluationService.java`
- `backend/src/main/java/com/javaevaluation/controller/EvaluationController.java`

**后端修改 (1)**:
- `backend/src/main/java/com/javaevaluation/service/CodeSubmitService.java`

**前端新增 (1)**:
- `frontend/src/api/evaluation.ts`

**前端修改 (1)**:
- `frontend/src/views/teacher/ReviewView.vue`
