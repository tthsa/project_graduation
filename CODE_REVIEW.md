# Java 作业评测系统 - 代码审查报告

> 审查日期:2026-05-11(2026-05-11 修订:用真实数据库 schema 重写)
> 审查范围:`backend/`、`frontend/`、`docker/`、`scripts/`、`init-db/`、`test-backend.sh`
> 整体状态:**半成品**,部分核心流程可跑(已有 5 条提交、28 条评测结果),但安全/前端业务化未完成

---

## 目录

- [整体架构](#整体架构)
- [关于本次修订](#关于本次修订)
- [🔴 阻断级问题](#-阻断级问题)
- [🟠 安全/越权级问题](#-安全越权级问题)
- [🟡 设计/逻辑问题](#-设计逻辑问题)
- [🟢 风格/清理建议](#-风格清理建议)
- [优先修复顺序](#优先修复顺序)

---

## 整体架构

- **后端**:Spring Boot 3.2.0 + Java 17 + MyBatis(注解式)+ Spring Security + JJWT
- **存储**:PostgreSQL 15 + Redis 7
- **消息**:RabbitMQ 3.12
- **评测**:Docker Java(沙箱)+ SiliconFlow LLM(DeepSeek-V3.2)
- **前端**:Vue 3 + TypeScript + Element Plus + Pinia + Vite

主要数据流:

```
学生提交 .java 文件
  → CodeSubmitService 存库 + 发 MQ
  → TaskConsumer 收消息
  → DockerSandboxService 起容器编译/跑测试
  → LlmReviewService 调 SiliconFlow 评审
  → 写 evaluation_result 表
  → 教师/学生查询结果
```

---

## 关于本次修订

首轮审查我以 `scripts/db/init.sql` 和 `scripts/db/init_v2.sql` 为准对照 Mapper,得出"教师登录失败、submission_file 字段缺失、BCrypt 是占位符"等阻断级结论。

实际从运行中的 `java-eval-postgres` 容器导出真实 schema(已保存为 `scripts/db/current_schema.sql`)后发现:

- **真实 schema 早已迁移**,与 Java 代码字段基本对齐
- `teacher.teacher_no`、`student.student_no`、`submission_file.{file_name,file_content,file_order}`、`evaluation_result.{test_score,llm_score,llm_review,execution_time}` 都已存在
- 数据库里 admin/teacher/student 各 1 条种子数据,密码 hash 是真实 BCrypt(`$2a$10$` 60字符)
- 已经存在 5 条提交、28 条评测结果 → 提交→评测→存结果的主链路是能跑的

因此下面的清单已删除那些被过期 SQL 误导的条目,只保留真实仍然成立的问题。**两份过期的 init SQL 本身就是一个需要修复的问题**(见问题 1)。

---

## 🔴 阻断级问题

### 1. 两份 init SQL 与真实 schema 完全不一致

`scripts/db/init.sql` 和 `scripts/db/init_v2.sql` 与数据库现状偏差很大,任何按这两份 SQL 新建数据库的开发者都会得到一个跑不起来的环境:

| 偏差点 | 旧 init.sql | 旧 init_v2.sql | 真实 schema |
|---|---|---|---|
| teacher 登录字段 | `username` | `username` | **`teacher_no`** |
| student.student_no 唯一约束 | 无 | 无 | **UNIQUE** |
| submission_file 字段 | `file_path/file_name/file_size` | 只有 `file_path` | **`file_name,file_content,file_order,created_at`** |
| evaluation_result 字段 | `test_case_id/passed/actual_output/score` | 与代码一致 | 与代码一致 |
| class 表是否存在 description/status | 无 | 无 | **有** |
| teacher 是否有 phone | 无 | 无 | **有** |
| llm_config 表 | 无 | 有 | **有** |
| 默认密码 hash | 假占位符(规律重复) | 假占位符 | **真实 BCrypt** |

- [ ] 用导出的 `scripts/db/current_schema.sql` 作为新的权威版本
- [ ] 删除 `scripts/db/init.sql` 和 `scripts/db/init_v2.sql`,或归档到 `scripts/db/legacy/`
- [ ] 在 `current_schema.sql` 里追加种子 INSERT(从生产库的实际 hash 复制,或写一段"运行 `PasswordTest` 生成 hash"的注释)
- [ ] `docker-compose.yml:16` 挂载点是 `../init-db:/docker-entrypoint-initdb.d`,但 `init-db/` 是空目录 → 把 `current_schema.sql` 也放一份进去,新环境才能自动初始化

### 2. 状态字典前后端三处不一致

| 位置 | 0 | 1 | 2 | 3 |
|---|---|---|---|---|
| `Submission.java` 注释 | 待评测 | 评测中 | 完成 | 失败 |
| 真实 `submission.status` 注释 | 待评测 | 评测中 | **已完成** | 失败 |
| `HomeworkWithStatus.java` 注释 | 未提交 | 待评测 | 评测中 | 已完成 |
| `frontend/.../student/HomeworkView.vue:117-127` | 未提交 | 已提交 | 已批改 | (未知) |
| `frontend/.../teacher/ReviewView.vue:137-150` | 待评测 | 评测中 | 完成 | 失败 |

- [ ] `CodeSubmitService.java:80` 新建提交 `setStatus(0)`,按 Submission 是"待评测",但 HomeworkWithStatus 里 0 = "未提交" → 学生作业列表永远显示错误状态
- [ ] 修复:以 Submission/真实 schema 为准,统一所有前后端引用

### 3. 前端依赖版本伪造,可能装不上

`frontend/package.json` 多个版本号超前/不存在:

| 包 | 当前声明 | 建议替换为 |
|---|---|---|
| vue-router | `^5.0.4`(不存在,Vue 3 配套是 4.x) | `^4.4.0` |
| pinia | `^3.0.4`(尚未发布) | `^2.2.0` |
| typescript | `~6.0.0`(尚未发布) | `~5.6` |
| vite | `^8.0.8`(尚未发布) | `^5.4` |
| eslint | `^10.2.1`(尚未发布) | `^9.x` |
| eslint-plugin-vue | `^10.8.0`(尚未发布) | `^9.x` |

- [ ] 修订版本号 → `rm -rf node_modules package-lock.json && npm install`
- [ ] 如果当前 `node_modules` 已能跑,以 `npm ls vue vue-router pinia vite typescript eslint` 实际安装版本回填到 package.json

### 4. 测试脚本/构建脚本失效

- [ ] `test-backend.sh:6` baseURL 写的 `/api/v1`,但代码里没有 v1 前缀 → 所有测试 404
- [ ] `test-backend.sh` 全文用 `jq`,Windows Git Bash 没装 → 删除或换 `node -e` 解析 JSON
- [ ] `scripts/build.sh:17` 把 jar 拷到 `docker/backend/app.jar`,但 `docker/backend/Dockerfile` 用 builder stage 自己 `mvn package` 重新打 → cp 步骤是死代码

---

## 🟠 安全/越权级问题

### 5. 路由前缀 + 权限映射严重越权

`SecurityConfig.java:88-92`:

```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/teacher/**").hasAnyRole("ADMIN", "TEACHER")
.requestMatchers("/api/student/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
```

但是:

- [ ] `StudentController.java` 的 create/update/delete 都挂在 `/api/student/` → **学生可创建/删除/修改任意学生记录**
- [ ] `TeacherController.java` 的 delete 在 `/api/teacher/` → **任意教师能删除其他教师**
- [ ] `HomeworkController.java` 的教师 update/delete 没做归属校验,任意教师改其他教师的作业
- [ ] 修复思路:把管理类接口拆到 `/api/admin/teacher`、`/api/admin/student`、`/api/teacher/students` 等命名空间

### 6. ReportController 未在 SecurityConfig 中限定

- [ ] `ReportController.java:16` 用 `/api/report/**`,SecurityConfig 没列 → 走 `anyRequest().authenticated()`
- [ ] 任何登录学生能调 `/api/report/homework/{id}`、`/api/report/student/{id}` 看其他学生提交
- [ ] 修复:挪到 `/api/teacher/report/**` 或加权限规则

### 7. studentId 来自前端可被伪造

- [ ] `SubmissionController.java:26` 直接读 `@RequestParam Integer studentId` → 学生 A 可传学生 B 的 id 替 B 提交
- [ ] `HomeworkController.java:101、164` 同样问题,可枚举他人作业状态
- [ ] 修复:所有学生身份相关接口从 `SecurityContextHolder.getContext().getAuthentication().getPrincipal()` 取

### 8. 作业/测试用例没有归属校验(真实 schema 已确认)

真实 `homework` 表字段:`id, course_id, title, description, deadline, status, create_time, updated_at` — **没有 teacher_id**

- [ ] 任意教师能改/删别人的作业,`TestCaseController` 同问题
- [ ] 修复:`ALTER TABLE homework ADD COLUMN teacher_id INTEGER REFERENCES teacher(id);` + Entity/Mapper/Controller 校验所有权
- [ ] 或者借道 `course.teacher_id`(真实 schema `course` 表里就有,且 homework 关联 course_id),让 Controller `update/delete` 时 join 校验:`homework.course_id → course.teacher_id == currentUser.id`

### 9. 实体直接序列化暴露 BCrypt 密码

- [ ] `Admin/Teacher/Student` 实体 `password` 字段直接被 `AuthController.java:111` getCurrentUser 返回前端
- [ ] 修复:在 password 字段加 `@JsonIgnore`

### 10. CORS 过于宽松

- [ ] `SecurityConfig.java:108-111`:`setAllowedOriginPatterns("*") + setAllowCredentials(true)`
- [ ] 生产环境收紧到明确域名列表

### 11. Docker 沙箱安全不足

`DockerSandboxService.java:138` 创建容器时缺少多项保护:

- [ ] 没 `withUser("sandbox")` → 学生代码以 **root** 运行(`docker/java-executor/Dockerfile` 已建 sandbox 用户但没启用)
- [ ] 没 `withNetworkDisabled(true)` → 用户代码能联网下载/外发
- [ ] 只限内存没限 CPU/PID → 容易 fork bomb 打满宿主
- [ ] `executeCommand:359` `awaitCompletion` 超时不杀进程
- [ ] `runTestCase:315` `"java -cp . " + className + " < input.txt"` 进 `sh -c`,className 来自学生源码 → 如果学生类名带特殊字符可执行任意 shell 命令

### 12. JSON 拼接和日志泄露

- [ ] `LlmReviewService.java:112-118` 手工拼 JSON,`escapeJson` 没处理控制字符(`\b \f` 等)
- [ ] `:118` `log.info("Request Body: {}", requestBody)` 把整段学生代码打进日志
- [ ] 修复:改用 `objectMapper.writeValueAsString(Map.of("model", ..., "messages", List.of(...)))`,日志只打 model + 字符数

### 13. Redis 默认类型序列化漏洞

- [ ] `RedisConfig.java:32-35` `activateDefaultTyping(NON_FINAL)` → 反序列化任意类风险(Jackson 历史多个 CVE)
- [ ] 修复:用白名单 `PolymorphicTypeValidator` 或避免存储多态对象

### 14. JWT/认证细节

- [ ] `AuthController.java:112` `authHeader.replace("Bearer ", "")` 应该用 `startsWith` + `substring(7)`,`replace` 会把消息体里所有 `"Bearer "` 都换掉
- [ ] `getCurrentUser` 重复 validate token(JwtFilter 已做)
- [ ] 没有 logout 接口,但 `frontend/.../api/user.ts:33` 调 `/auth/logout`
- [ ] 没有 token 黑名单/刷新机制
- [ ] `application.yaml:76` `jwt.secret: ${JWT_SECRET}` 无默认值,环境变量未设时启动失败

---

## 🟡 设计/逻辑问题

### 15. EvaluationTask Entity 与真实表不一致

真实 `evaluation_task` 表:`id, homework_id, name, description, status:integer, created_at`

代码 `entity/EvaluationTask.java`:

```java
private Integer id;
private Integer homeworkId;
private String status;            // "PENDING/RUNNING/COMPLETED/FAILED"
private Integer totalCount;       // ← 表里没有
private Integer completedCount;   // ← 表里没有
private LocalDateTime startTime;  // ← 表里没有
private LocalDateTime endTime;    // ← 表里没有
private LocalDateTime createdAt;
```

- [ ] **数据类型错位**:表 `status` 是 integer,Entity 是 String → 查询能跑(Jackson 转换),但 `findByStatus("PENDING")` 拿不到任何记录(参数变 "PENDING" 文本,与 integer 列比较失败)
- [ ] Entity 字段比表多 4 个,会全部映射成 null
- [ ] 真实表里目前 0 条数据,这个表/Entity 整套**没有被任何业务调用** → 死代码
- [ ] 修复方案二选一:
  - 删除 EvaluationTask + Mapper + 表(确认不用)
  - 或在表上 `ALTER TABLE` 加缺失列,并改 status 为 varchar

### 16. course 表存在但 Java 端无 Course Entity / Mapper

真实 schema 有完整的 course 表:`id, name, teacher_id, class_id, create_time`,homework 通过 `course_id` 外键关联。

但代码里:
- 没有 `entity/Course.java`、`mapper/CourseMapper.java`、`controller/CourseController.java`
- 前端 `teacher/HomeworkView.vue:98、137` 硬编码 `courseId: 1` 发布作业
- 当前生产库有 1 条 course 记录,创建作业能跑;一旦清空库或 course=1 被删,所有新作业插入会因外键失败

- [ ] 补 Course 实体/Mapper/Controller
- [ ] 前端发布作业时弹出 course 选择框
- [ ] 或者(更激进)从 homework 移除 course_id 依赖,直接关联 teacher_id

### 17. class 表名用了 SQL 标准保留字

- [ ] PostgreSQL 把 `class` 当作 non-reserved keyword,所以 `FROM class` 实际能跑;但 SQL 标准中 `class` 是保留字
- [ ] 后续如果换到 MySQL/Oracle 或者升级 PostgreSQL,可能踩坑
- [ ] 建议改名为 `class_info`(同步改 Mapper),或者所有引用都加双引号 `"class"`

### 18. TaskProcessorService 事务范围过大

- [ ] `TaskProcessorService.java:33` `@Transactional` 包了 Docker 执行(可能 30 秒)+ LLM 调用(可能 60 秒)→ DB 连接池容易耗尽
- [ ] catch 内 `throw e` 会触发回滚,把已经做的 `status=3` 也回滚
- [ ] 修复:拆事务,只在 DB 写入小事务里加 `@Transactional`,外面用普通方法编排

### 19. ResultService 与 Redis 矛盾

- [ ] `ResultService.java:23` 用进程内 `ConcurrentHashMap` 缓存,但项目已集成 Redis 没用上
- [ ] TaskProcessorService 保存结果后没调 evictCache → 永远读到旧数据
- [ ] 修复:用 Spring Cache + Redis,或者在 saveEvaluationResult 后主动 evict

### 20. RabbitMQ 缺死信队列

- [ ] `RabbitMQConfig.java` 设了 `x-message-ttl: 600000`,但没绑 DLX → 过期/重试失败的消息直接丢
- [ ] `result_queue` 定义了但没消费者,死代码
- [ ] 修复:加 DLX + 死信交换机 + 死信队列

### 21. 提交流程缺陷

- [ ] `CodeSubmitService.java:104` `new String(file.getBytes(), StandardCharsets.UTF_8)` 假定 UTF-8 编码,学生用 GBK 会乱码
- [ ] `SubmissionFile.fileName` 直接写入 `DockerSandboxService.java:118` `new File(localWorkDir, file.getFileName())` → **路径穿越漏洞**:学生上传名为 `../../etc/passwd` 的文件会逃出工作目录
- [ ] 没截止时间校验,过期后还能提交
- [ ] `SubmissionHistory` 表只存 `id, submission_id, submit_time`,没存文件快照 → 重新提交后旧版本代码丢失

### 22. DockerSandboxService 实现脆弱

- [ ] `:123` `file.getFileContent().contains("public static void main")` → 注释里写一句就误命中
- [ ] `:377` `extractClassName` 只匹配 `public class`,包私有类找不到
- [ ] `:325` 输出比较 `expected.equals(actual)` 太严格(末尾换行/空格差异都算失败),通常需要 normalize 行尾
- [ ] `:356` 同一个 StringBuilder 收 stdout + stderr,编译错误信息会被当作程序输出去比较
- [ ] `:152` 每个测试用例重新做 tar 上传 stdin 文件,效率差;可改 bind mount 或 exec 管道传入

### 23. extractScore 假阳性

- [ ] `LlmReviewService.java:181-212` 第二个正则 `(\d+)\s*分` 太宽,LLM 一句"通过 8 个测试,满分 10 分"会取 8 当代码质量分
- [ ] 修复:要求 LLM 返回结构化 JSON,或正则只匹配"代码质量评价"上下文

### 24. 前端业务定位错位

整个 admin/student 首页和路由是从"毕业设计管理系统"复制来的,**与 Java 作业评测系统业务不匹配**:

- [ ] `admin/IndexView.vue:76-80` 跳 `/admin/topic`、`/admin/system` — 路由不存在
- [ ] `admin/IndexView.vue:95` "系统名称:毕业设计管理系统"
- [ ] `student/IndexView.vue:72-83` 三个按钮跳 `/student/topic`、`/student/progress`、`/student/document` — 路由不存在,点击 404
- [ ] 应替换成"我的作业"、"提交记录"、"评测结果"等真实业务页面

### 25. 前端用户态不持久 + 路由守卫死循环

- [ ] `stores/user.ts:8` 只把 token 存 localStorage,userInfo 没存 → 刷新页面 token 在但 userInfo 是 null
- [ ] `router/index.ts:112、135` 用 `userStore.userInfo?.userType` 决定跳哪里,刷新后取不到 → 可能卡在 login
- [ ] 修复:userInfo 也持久化到 localStorage,或在 main.ts/router beforeEach 检测 token 时调 `/auth/me` 恢复

### 26. 前端类型与后端响应不符

- [ ] `api/user.ts:27` `getUserInfo(): Promise<LoginResult>` 声明返回 LoginResult
- [ ] 但后端 `/auth/me` 实际返回 Admin/Teacher/Student 实体(没有 token、userType、name 等字段)
- [ ] `stores/user.ts:38-43` 用 `result.userId/userType` 会得到 undefined

---

## 🟢 风格/清理建议

- [ ] `mybatis-plus` 已引入但全部用手写 SQL,可以删除依赖或改用 `BaseMapper`
- [ ] `properties/ReviewProperties.java` 无人使用,删除或接入 RabbitMQConfig
- [ ] `config/RedisConfig.java:20` `RestTemplate` Bean 放错位置且没设超时(默认无限超时)
- [ ] `controller/LlmReviewController.java` 只有一个 extract-score 接口,看起来是调试用,要么加权限保护要么删
- [ ] `controller/AuthController.java:111` getCurrentUser 应该直接从 SecurityContext 取,不要再 parse token
- [ ] `mapper/AdminMapper.java:15` insert 用 `create_time`,但 `mapper/EvaluationResultMapper.java:15` insert 用 `created_at` → 列名风格不统一(对照真实 schema,大多数表是 `create_time`,只有 `evaluation_result.created_at`、`submission_file.created_at`、`evaluation_task.created_at` 用 `created_at`,需要在文档里说明这是有意为之还是历史遗留)
- [ ] `BackendApplication.java:8` `@EnableScheduling` 但项目里没有 `@Scheduled` 方法,可删
- [ ] `docker/redis/redis.conf:3` `protected-mode no` + 无密码,只靠 docker 网络隔离,建议加 `requirepass`
- [ ] `security/SecurityConfig.java:48` `DaoAuthenticationProvider` 在 Spring Security 6.4+ 已 deprecated
- [ ] `frontend/package-lock.json` 与 `package.json` 版本号大概率严重不一致,需要重做依赖
- [ ] `dto/HomeworkWithStatus.java` 注释与 Submission 不一致(见问题 2)
- [ ] `entity/Submission.java:16` status 字段建议用枚举,而不是 Integer
- [ ] `controller/SubmissionController.java` 用 `Map.of(...)` 手写响应,与其他 Controller 用 `Result<>` 风格不一致
- [ ] `mapper/SubmissionFileMapper.xml` 仍引用旧的 `file_path` 字段(真实表已无此字段),整个 XML 是死文件,可直接删除
- [ ] `llm_config` 表存在但项目目前用 `application.yaml + SiliconFlowProperties` 注入 LLM 配置,数据库表是死表;要么接入要么删

---

## 优先修复顺序

按"先把环境对齐 → 修真实问题 → 安全加固 → 清理"思路:

### 阶段 1:对齐 schema 文档(对应问题 1)

1. 用 `scripts/db/current_schema.sql` 替代 `init.sql` / `init_v2.sql`
2. 在 current_schema.sql 末尾补一段种子 INSERT(可用 `pg_dump --data-only --inserts -t admin -t teacher -t student -t class -t course -t llm_config -t system_config` 导出真实初始数据)
3. 把 current_schema.sql(或 split 成 `01-schema.sql` / `02-seed.sql`)放进 `init-db/`,让新环境 `docker-compose up` 能自动建库
4. 删除或归档过期 `init.sql` 与 `init_v2.sql`

### 阶段 2:修真实存在的小问题(对应问题 15、16、17、2)

5. 删除或修复 `EvaluationTask` 实体/Mapper(目前是死代码)
6. 决定 course 是补 Entity 还是从 homework 移除 course_id 依赖
7. 统一 Submission 状态字典,改前端 HomeworkView/ReviewView
8. (可选)class → class_info 改名

### 阶段 3:修真正阻断主流程的问题(对应问题 3、4、21、22)

9. 前端 package.json 版本号修订 + 重装
10. test-backend.sh 或删或重写
11. 提交流程:加 .java 后缀和路径穿越校验、加截止时间校验
12. DockerSandboxService:正则 / 输出比较 / stderr 分流 / 主类查找

### 阶段 4:前端业务化(对应问题 24、25、26)

13. 重写 admin/student/teacher 三个 IndexView,贴合作业评测业务
14. 清理路由表,删掉不存在的 topic/progress/document 跳转
15. userInfo 持久化或在刷新时恢复
16. 修正 api/user.ts getUserInfo 类型与处理

### 阶段 5:安全加固(对应问题 5~14、20)

17. Controller 路径按角色拆分,studentId 从 JWT 取
18. ReportController 加权限规则,作业/测试用例做归属校验(借道 course.teacher_id 或新增 homework.teacher_id)
19. 实体 password 字段加 `@JsonIgnore`
20. Docker 沙箱:withUser + withNetworkDisabled + 限 CPU/PID + sh 命令参数化
21. LlmReviewService 改 ObjectMapper,日志脱敏
22. RedisConfig 缩小反序列化白名单
23. RabbitMQ 配置 DLX

### 阶段 6:清理(对应 🟢 区)

24. 删死代码,统一风格,补 README

---

## 附:文件清单速查

| 模块 | 关键文件 |
|---|---|
| 入口 | `backend/.../BackendApplication.java` |
| 配置 | `application.yaml`、`config/RabbitMQConfig.java`、`config/RedisConfig.java`、`config/DockerConfig.java` |
| 安全 | `security/SecurityConfig.java`、`security/JwtFilter.java`、`security/JwtUtils.java`、`security/UserDetailsServiceImpl.java` |
| 评测核心 | `service/CodeSubmitService.java`、`service/TaskProcessorService.java`、`service/DockerSandboxService.java`、`service/LlmReviewService.java` |
| 消息 | `service/TaskQueueService.java`、`service/TaskConsumer.java` |
| Controller | `controller/AuthController.java`、`SubmissionController.java`、`HomeworkController.java`、`TestCaseController.java`、`ReportController.java`、`TeacherController.java`、`StudentController.java`、`LlmReviewController.java`、`HealthController.java` |
| 数据库 | **`scripts/db/current_schema.sql`(真实,以此为准)** ⭐<br/>`scripts/db/init.sql`(过期,待删)<br/>`scripts/db/init_v2.sql`(过期,待删) |
| 容器 | `docker/docker-compose.yml`、`docker/backend/Dockerfile`、`docker/java-executor/Dockerfile`、`docker/redis/redis.conf` |
| 前端入口 | `frontend/src/main.ts`、`router/index.ts`、`stores/user.ts`、`api/request.ts` |
| 前端视图 | `views/LoginView.vue`、`views/{admin,teacher,student}/*.vue`、`layout/MainLayout.vue` |
