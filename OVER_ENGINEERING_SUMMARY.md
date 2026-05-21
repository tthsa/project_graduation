# 过度设计与框架滥用反思总结

**背景**: 对 Java 作业自动评测系统进行代码简化时，发现大量"看起来很标准但实际不必要"的代码。

---

## 一、发现的具体问题

### 1. @ConfigurationProperties 用于 4 个字段

```java
// 过度设计：为一个只有 4 个字段的配置单独建类
@Data
@Component
@ConfigurationProperties(prefix = "siliconflow")
public class SiliconFlowProperties {
    private String apiKey;
    private String baseUrl;
    private String model;
    private Integer timeout = 60000;
}

// 实际只需要
@Value("${siliconflow.api-key}")
private String apiKey;
```

**判断依据**: 配置项只有 4 个、只在一处使用、不需要校验。

### 2. TaskQueueService 一行代码的 Service

```java
// 过度设计：为 rabbitTemplate.convertAndSend() 做一层方法包装
@Service
public class TaskQueueService {
    public void sendTask(CodeTask task) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, task);
    }
}

// 实际直接在调用方注入 RabbitTemplate 调用即可
```

### 3. 手写 ConcurrentHashMap 缓存（内存泄漏）

```java
// 过度设计：自己管理缓存生命周期
private final Map<Integer, EvaluationResult> cache = new ConcurrentHashMap<>();

// 实际只需要一行注解
@Cacheable(value = "evaluationResult", key = "#submissionId")
```

**风险**: 无过期机制、无大小限制，提交越多内存越大，最终 OOM。

### 4. security 包职责混乱

| 类 | 实际职责 | 应属包 |
|---|---|---|
| SecurityConfig | Spring 配置类 | config/ |
| JwtUtils | 工具类 | utils/ |
| JwtFilter | Servlet 过滤器 | filter/ |
| UserDetailsServiceImpl | Service 实现 | service/impl/ |

**问题**: 不同职责的类混在一个包里，违反单一职责原则。

### 5. Controller 中重复定义 token 解析

`currentTeacherId()` / `currentUserId()` 私有方法在 4 个 Controller 中重复定义，且实现不一致（有的直接调工具类，有的手写解析逻辑）。

### 6. Lombok 注解叠加冗余

```java
// 冗余：@Data 已包含无参构造器（无 final 字段时）
@Data
@Builder
@NoArgsConstructor      // 多余
@AllArgsConstructor     // 多余
```

---

## 二、为什么会这样？

### 1. LLM 的信息不对称

LLM 生成代码时**默认假设是"企业级项目"**，倾向于给出教科书式的"最佳实践"，而不是根据实际规模调整：

| LLM 假设 | 实际情况 |
|---------|---------|
| "这可能是微服务架构" | 毕业设计单体应用 |
| "配置项可能会扩展" | 4 个字段且不会再增加 |
| "需要面向接口编程" | 只有 1 个实现类 |

### 2. "标准 = 专业"的误区

初学者容易认为：
- 用上高级特性 = 代码质量好
- 代码看起来"像大厂写的" = 水平高
- 提前考虑扩展 = 架构思维

但实际上：**正确的特性用在错误的场景 = 负担**。

### 3. 类比：小流量系统用 K8s

```
小流量系统用 K8s  =  4 个配置项用 @ConfigurationProperties
```

都是拿大厂的锤子砸自家的钉子：
- 运维复杂度 >> 收益
- 排查问题多绕一层
- 团队学习成本高

---

## 三、YAGNI 原则

**You Aren't Gonna Need It**（你不会需要它）。

核心思想：
> 只在真正需要的时候才引入抽象，而不是"万一以后需要"。

毕业设计的代码：
- 交完大概率不会再动
- 功能边界清晰固定
- 不需要考虑"10 个开发人员协作"

这时候的"可扩展性"是**假设性的负担**。

---

## 四、判断标准

以后看到类似代码，问自己：

> **"如果需求明天变了，我改这个简单封装快，还是改底层调用快？"**

| 场景 | 简单方案 | 复杂方案 | 选哪个 |
|------|---------|---------|-------|
| 4 个配置项 | @Value | @ConfigurationProperties | 简单 |
| 发一条 MQ | 直接调 rabbitTemplate | 封装 Service | 简单 |
| 缓存一个查询 | @Cacheable | 手写 HashMap | 简单 |
| 10+ 配置项需要校验 | @Value | @ConfigurationProperties + @Validated | 复杂 |
| 多处发送不同 MQ | 直接调 | 封装抽象 Service | 复杂 |

**原则**: 不是"特性高级不高级"，而是**改起来快不快**。

---

## 五、学习曲线的价值

这个经历的意义：

```
阶段1：能写就行              → 代码直来直去
阶段2：学了框架特性           → 到处用，觉得越多越好（你在这里）
阶段3：能判断什么时候不需要    → 知道简单和复杂的边界
阶段4：大项目能设计，小项目能简化
```

**现在的价值**:
- 用过 `@ConfigurationProperties`，知道它是什么
- 删过它，知道什么时候不需要
- 下次遇到 20 个配置项的场景，你会主动用它

这比"从来没写过"然后"看别人用觉得高级"要好得多。

---

## 六、修复成果

| # | 问题 | 修复方式 | 收益 |
|---|------|---------|------|
| 1 | SiliconFlowProperties 类 | 删除，改为 @Value | -1 个类文件 |
| 2 | TaskQueueService | 删除，内联到 EvaluationService | -1 个类文件 |
| 3 | 手写 ConcurrentHashMap 缓存 | 改为 @Cacheable | 消除内存泄漏风险 |
| 4 | security 包混乱 | SecurityConfig→config/, JwtUtils→utils/ 等 | 职责清晰 |
| 5 | Controller token 解析重复 | 统一调用 jwtUtils.getUserIdFromHeader() | -4 个重复方法 |
| 6 | ResultService @Autowired | 统一为 @RequiredArgsConstructor | 注入方式一致 |
| 7 | Result 注解冗余 | 删除 @NoArgsConstructor + @AllArgsConstructor | 代码简洁 |
| 8 | AuthController switch 重复 | 提取 findUserById() 方法 | 减少重复代码 |

**总计**: 减少 2 个类文件，消除 1 个内存泄漏风险，删除 4 个重复方法。
