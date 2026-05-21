# 代码简化与结构优化日志

**日期**: 2026-05-20

**目标**: 消除代码冗余，优化项目结构，提升可维护性

---

## 一、实体层：提取公共基类

### 新增文件
- `backend/src/main/java/com/javaevaluation/entity/BaseEntity.java`
  - 包含公共字段：`id`（`@TableId`）、`createdAt`、`updatedAt`

### 改造的实体（继承 BaseEntity，删除重复字段）

| 实体 | 删除的重复字段 |
|------|--------------|
| `Student` | `id`, `createdAt`, `updatedAt` |
| `Teacher` | `id`, `createdAt`, `updatedAt` |
| `Admin` | `id`, `createdAt`, `updatedAt` |
| `Homework` | `id`, `createdAt`, `updatedAt` |
| `ClassInfo` | `id`, `createdAt`, `updatedAt` |

### 添加 `@TableName` 注解的实体（为 MyBatis-Plus 适配）

- `Student` / `Teacher` / `Admin` / `Homework` / `ClassInfo` / `Course`
- `Submission` / `EvaluationResult` / `TestCase` / `SubmissionFile`
- `SubmissionHistory` / `SystemConfig` / `Notification`

> 未继承 `BaseEntity` 的实体是因为字段不完全匹配（如无 `updatedAt` 或无 `createdAt`），但均加了 `@TableName`。

---

## 二、Mapper 层：接入 MyBatis-Plus BaseMapper

### 新增文件
- `backend/src/main/java/com/javaevaluation/mapper/BaseMapperExt.java`
  - 扩展 `BaseMapper<T>`，提供与原方法名兼容的默认实现：
    - `findById(id)` -> `selectById(id)`
    - `findAll()` -> `selectList(null)`
    - `delete(id)` -> `deleteById(id)`
    - `count()` -> `selectCount(null)`
  - **作用**：避免修改大量调用方代码，实现平滑迁移

### 所有 Mapper 变更（13 个）

| Mapper | 继承关系 | 删除的方法 | 保留的方法 |
|--------|---------|-----------|-----------|
| `StudentMapper` | `BaseMapperExt<Student>` | `findById`, `findAll`, `count`, `insert`, `delete` | `findByStudentNo`, `findByClassId`, `update`, `updatePassword`, `markFirstLoginDone` |
| `TeacherMapper` | `BaseMapperExt<Teacher>` | `findById`, `findAll`, `count`, `insert`, `delete` | `findByTeacherNo`, `update`, `updatePassword` |
| `AdminMapper` | `BaseMapperExt<Admin>` | `findById`, `insert` | `findByUsername`, `update`, `updatePassword` |
| `HomeworkMapper` | `BaseMapperExt<Homework>` | `findById`, `findAll`, `count`, `insert`, `delete` | `findByCourseId`, `update`, `countByTeacherId` |
| `CourseMapper` | `BaseMapperExt<Course>` | `findById`, `findAll`, `count`, `insert`, `delete` | `findByTeacherId`, `update`, `countByTeacherId` |
| `ClassInfoMapper` | `BaseMapperExt<ClassInfo>` | `findById`, `findAll` | `findByTeacherId`, `findByNameLike` |
| `SubmissionMapper` | `BaseMapperExt<Submission>` | `findById`, `insert`, `update` | `updateStatus`, `findByHomeworkIdAndStudentId`, `findByHomeworkId`, `findByStudentId`, `countByHomeworkId`, `countPendingByTeacherId`, `countByTeacherIdAndStatus` |
| `EvaluationResultMapper` | `BaseMapperExt<EvaluationResult>` | `findById`, `insert` | `findBySubmissionId` |
| `TestCaseMapper` | `BaseMapperExt<TestCase>` | `findById`, `insert`, `delete` | `findByHomeworkId`, `findPublicByHomeworkId`, `update`, `deleteByHomeworkId`, `countByHomeworkId` |
| `SubmissionFileMapper` | `BaseMapperExt<SubmissionFile>` | `findById`, `insert` | `findBySubmissionId`, `deleteBySubmissionId`, `countBySubmissionId` |
| `SubmissionHistoryMapper` | `BaseMapperExt<SubmissionHistory>` | `insert` | `findBySubmissionId` |
| `SystemConfigMapper` | `BaseMapperExt<SystemConfig>` | `findById`, `findAll` | `findByConfigKey` |
| `NotificationMapper` | `BaseMapperExt<Notification>` | `insert`, `delete` | `findByUserId`, `findUnreadByUserId`, `markAsRead`, `markAllAsRead` |

---

## 三、配置层：结构修复与安全加固

### 新增文件
- `backend/src/main/java/com/javaevaluation/config/WebConfig.java`
  - 放置 `RestTemplate` Bean（从 `RedisConfig` 迁移）

### 修改文件

#### `RedisConfig.java`
- **移除** `RestTemplate` Bean 定义（迁移到 `WebConfig`）
- **移除** 危险的 `activateDefaultTyping` 配置（存在反序列化漏洞风险）
- **改用** `GenericJackson2JsonRedisSerializer` 替代自定义的 `Jackson2JsonRedisSerializer`

#### `SecurityConfig.java`
- **修复** CORS 配置：`setAllowedOriginPatterns("*")` + `setAllowCredentials(true)` 的冲突
- **改为** 明确指定 `localhost` 和 `127.0.0.1` 来源模式

---

## 四、Service 层：消除手动 JSON 拼接

### `LlmReviewService.java`
- **重构** `callLlmApi()` 方法：使用 `ObjectMapper.writeValueAsString()` 构建请求体，替代手动字符串拼接
- **删除** `escapeJson()` 私有方法（不再需要）

---

## 五、项目根目录：添加 `.gitignore`

### 新增文件
- `.gitignore`
  - 排除 IDE 文件（`.idea/`, `*.iml`, `.vscode/`）
  - 排除构建产物（`target/`, `node_modules/`）
  - 排除开发日志（`WORK_LOG_*.md`, `DEV_LOG_*.md`, `FIX_LOG_*.md`, `CODE_REVIEW.md`）
  - 排除测试数据（`test/output/`）
  - 排除环境文件（`.env`）

---

## 统计

| 指标 | 数量 |
|------|------|
| 新增文件 | 4 |
| 修改的实体类 | 13 |
| 简化的 Mapper | 13 |
| 修改的配置类 | 2 |
| 重构的 Service | 1 |
| **总计改动文件** | **33** |

---

## 验证建议

编译检查：

```bash
cd backend
mvn clean compile
```

运行测试（如有）：

```bash
mvn test
```
