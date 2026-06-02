# Java 项目注解发现与提取规则

不依赖预先枚举框架名称。通过分析项目实际使用的 import 包前缀，自动识别框架组合，
再通过语义推断处理未知注解。适用于任何 Spring 生态项目（包括自研框架）。

---

## 第一节：通用注解发现流程

```
Step 1 — 提取项目使用的第三方注解包前缀
  对代表性源文件执行 Read，收集 import 列表
  重点文件：Controller 类、Service 类、Entity/DO 类、VO/DTO 类
  排除标准库：java.*、javax.* (非 validation)、sun.*

  或使用 codegraph_search 批量定位：
  codegraph_search("@RestController")  → 找到 Controller 文件路径
  codegraph_search("@TableName")       → 找到 Entity 文件路径
  codegraph_search("@Service")         → 找到 Service 文件路径
  → Read 这些文件，提取 import 行

Step 2 — 对比包前缀映射表（见第二节）
  → 识别出项目实际使用的框架/库组合
  → 例：发现 com.baomidou.mybatisplus → 使用 MyBatis-Plus → @TableName=表名

Step 3 — 对每个命中的框架执行对应提取策略
  → 按第二节的"Codebook 意义"列，决定每种注解写入哪个文档

Step 4 — 未命中注解：按第三节规则推断语义
  → 通过注解名称模式自动归类到对应 Codebook 文档
```

---

## 第二节：包前缀 → 框架映射表

### Web 层

| 包前缀 | 框架/库 | 关键注解 → Codebook 意义 |
|--------|--------|---------------------|
| `org.springframework.web` | Spring MVC | `@RestController`=API入口、`@RequestMapping/@GetMapping/@PostMapping`=API路由 |
| `org.springframework.web.bind.annotation` | Spring MVC | `@RequestBody/@RequestParam/@PathVariable`=API参数定义 |

### 安全与权限层

| 包前缀 | 框架/库 | 关键注解 → Codebook 意义 |
|--------|--------|---------------------|
| `org.springframework.security` | Spring Security | `@PreAuthorize/@PostAuthorize`=权限码（写入 API 权限说明）|
| `cn.dev33.satoken` | Sa-Token | `@SaCheckPermission`=权限码、`@SaCheckLogin`=需登录、`@SaCheckRole`=角色 |
| `org.apache.shiro` | Shiro | `@RequiresPermissions`=权限码、`@RequiresRoles`=角色、`@RequiresAuthentication`=需登录 |

### API 文档层

| 包前缀 | 框架/库 | 关键注解 → Codebook 意义 |
|--------|--------|---------------------|
| `io.swagger.v3.oas.annotations` | SpringDoc（OpenAPI 3） | `@Tag(name)`=模块分组、`@Operation(summary)`=接口描述、`@Schema(description)`=DTO字段说明 |
| `io.swagger.annotations` | Swagger 2（springfox）| `@Api(tags)`=模块分组、`@ApiOperation`=接口描述、`@ApiModelProperty`=DTO字段说明 |

### 数据访问层

| 包前缀 | 框架/库 | 关键注解 → Codebook 意义 |
|--------|--------|---------------------|
| `com.baomidou.mybatisplus` | MyBatis-Plus | `@TableName`=数据库表名、`@TableField`=字段映射、`@TableId`=主键 |
| `jakarta.persistence` / `javax.persistence` | JPA / Hibernate | `@Entity`=数据库表、`@Table(name)`=表名、`@Column`=字段约束（长度/唯一/非空）、`@Index`=索引 |
| `org.springframework.data` | Spring Data | `extends JpaRepository/CrudRepository`=仓储层 |
| `org.apache.ibatis` | MyBatis | `@Select/@Insert/@Update/@Delete`=SQL语句（从中提取表操作语义）|

### 校验层

| 包前缀 | 框架/库 | 关键注解 → Codebook 意义 |
|--------|--------|---------------------|
| `jakarta.validation` | Bean Validation 3（Spring Boot 3.x）| `@NotBlank/@NotNull`=必填、`@Size`=长度约束、`@Pattern(regexp)`=正则校验、`@Email`=邮箱格式、`@Min/@Max`=数值范围 → **全部写入 API 字段校验矩阵** |
| `javax.validation` | Bean Validation 2（Spring Boot 2.x）| 同上 |
| `org.hibernate.validator` | Hibernate Validator 扩展 | `@Length`=字符串长度、`@Range`=数值范围、`@URL`=URL格式 → API 字段校验矩阵 |

### 事务与缓存层

| 包前缀 | 框架/库 | 关键注解 → Codebook 意义 |
|--------|--------|---------------------|
| `org.springframework.transaction` | Spring TX | `@Transactional`=事务边界（写入 SRS 事务说明）|
| `org.springframework.cache` | Spring Cache | `@Cacheable`=缓存查询接口（写入 HLA 缓存策略）、`@CacheEvict`=缓存失效触发点 |

### 异步与调度层

| 包前缀 | 框架/库 | 关键注解 → Codebook 意义 |
|--------|--------|---------------------|
| `org.springframework.scheduling` | Spring Scheduling | `@Scheduled`=**定时触发**（写入 SRS 触发方式、HLA 异步流程）|
| `org.springframework.context.event` | Spring Events | `@EventListener`=**领域事件触发**（写入 SRS 触发方式）|
| `org.springframework.amqp` | RabbitMQ | `@RabbitListener`=**MQ 消费触发**（写入 SRS 触发方式、HLA 消息流）|
| `org.springframework.kafka` | Kafka | `@KafkaListener`=**MQ 消费触发**（写入 SRS 触发方式）|

### 微服务层

| 包前缀 | 框架/库 | 关键注解 → Codebook 意义 |
|--------|--------|---------------------|
| `org.springframework.cloud.openfeign` | OpenFeign | `@FeignClient(name)`=微服务调用（写入 HLA 服务依赖图）|
| `com.alibaba.dubbo` / `org.apache.dubbo` | Dubbo | `@DubboService`=RPC服务提供方、`@DubboReference`=RPC调用方（写入 HLA RPC 调用图）|
| `org.springframework.cloud.gateway` | Spring Cloud Gateway | 路由配置 → HLA 网关架构 |

### 无 Codebook 意义（忽略）

| 包前缀 | 框架/库 | 处理方式 |
|--------|--------|---------|
| `lombok` | Lombok | `@Data/@Builder/@Slf4j` 均无业务语义，**不写入任何 Codebook** |
| `org.mapstruct` | MapStruct | `@Mapper/@Mapping` 为对象转换，**不写入 Codebook**（可在 HLA 分层中注明转换层存在）|
| `org.projectreactor` / `reactor.core` | Reactor | 响应式编程标记，注明"异步响应式"即可 |

---

## 第三节：未命中注解的语义推断规则

当某注解的包前缀**不在上方映射表中**时，通过注解**名称模式**推断其 Codebook 意义：

| 名称模式（大小写不敏感）| 推断意义 | 写入哪个 Codebook |
|------------------------|---------|-------------|
| `*Log*` / `*Audit*` / `*Record*` / `*Operation*` | 操作日志注解 | SRS §审计日志需求 |
| `*Permission*` / `*Auth*` / `*Check*` / `*Requires*` | 权限控制注解 | API 接口权限说明 |
| `*Limit*` / `*RateLimit*` / `*Throttle*` / `*RepeatSubmit*` | 接口限流/防重复提交 | API 非功能需求 |
| `*Idempotent*` / `*Repeat*` | 幂等控制 | API 接口约束 |
| `*Encrypt*` / `*Decrypt*` / `*Sensitive*` / `*Desensitize*` | 数据脱敏/加密 | SRS 安全需求 |
| `*DataScope*` / `*DataFilter*` / `*DataPermission*` | 数据权限过滤 | SRS 数据权限约束 |
| `*Tenant*` / `*MultiTenant*` | 多租户隔离 | HLA 多租户架构 |
| `*Cache*` / `*Cacheable*` | 自定义缓存注解 | HLA 缓存策略 |
| `*Valid*` / `*Validate*` / `*Assert*` | 自定义校验 | API 字段校验矩阵 |
| `*Dict*` / `*Enum*` / `*Select*` | 数据字典/枚举绑定 | 数据库文档数据字典 |
| `*Version*` / `*OptimisticLock*` | 乐观锁 | SRS 并发约束 |
| `*Excel*` / `*Export*` / `*Import*` | 导入导出功能 | SRS 功能需求（批量操作）|
| `*Sign*` / `*Signature*` | 签名验证 | SRS 安全需求 |

**推断置信度标注规则：**
- 名称完全匹配（如 `@OperationLog`）→ 高置信度，直接写入 Codebook
- 名称部分匹配 → 在 Codebook 中标注 `[推断，需确认]`
- 完全无法推断 → 在 HLA 的"横切关注点"节列出，标注 `[未知注解，需人工确认]`

---

## 第四节：通用 Spring 分层提取规则（与框架无关）

无论使用何种快速开发框架或自研框架，只要基于 Spring，以下规则适用：

### API 入口层

```
codegraph_search("@RestController" / "@Controller", kind="class")
  → 获取所有 Controller 文件路径

Read(Controller 文件)
  → 提取：
    - @RequestMapping 路径前缀
    - 每个方法的 @GetMapping/@PostMapping/@PutMapping/@DeleteMapping 路径
    - 方法参数中 @RequestBody 对应的 DTO 类名
    - 权限注解（任意包前缀，按第二节映射或第三节推断）
    - API 描述注解（@Operation/@ApiOperation 的 summary）
```

### DTO / VO 层（字段校验矩阵来源）

```
从 Controller 方法的 @RequestBody 参数类型，提取 DTO 类名
codegraph_search(DTO类名, kind="class") → 获取 DTO 文件路径

Read(DTO 文件)
  → 提取每个字段上的 Bean Validation 注解（jakarta.validation.*）：
    @NotBlank(message) → 必填 + 错误提示
    @Pattern(regexp, message) → 正则约束
    @Size(min, max, message) → 长度约束
    @Email(message) → 邮箱格式
    @Min/@Max → 数值范围
  → 生成字段校验矩阵（字段名/类型/必填/规则/错误提示）
```

### 数据库表层

```
# MyBatis-Plus 项目
codegraph_search("@TableName", kind="class") → Entity/DO 文件列表
Read(Entity 文件) → @TableName(value)=表名、@TableField=字段映射、字段类型和注释

# JPA 项目
codegraph_search("@Entity", kind="class") → Entity 文件列表
Read(Entity 文件) → @Table(name)=表名、@Column(name/length/nullable/unique)=字段约束
                    @Index(columnList/unique)=索引定义

# 公共字段识别（继承自父类）
codegraph_search("BaseDO" / "BaseEntity" / "TenantBaseDO") → 父类文件
Read(父类文件) → 提取公共字段列表，在数据库文档中标注"继承自 {父类名}"
```

### 服务层触发点（callers 发现非 HTTP 入口）

```
对核心 Service 方法执行：
codegraph_callers("XxxService.createXxx" / "XxxService.updateXxx")
  → 结果中按触发类型分类：
    - Controller 方法 → HTTP 接口触发（正常路径）
    - @Scheduled 方法 → 定时任务触发 → 写入 SRS"触发方式"
    - @EventListener 方法 → 领域事件触发 → 写入 SRS"触发方式"
    - @RabbitListener / @KafkaListener → MQ 消费触发 → 写入 SRS"触发方式" + HLA 消息流
    - @FeignClient 调用方 → 上游微服务触发 → 写入 HLA 服务调用关系
```

### 错误码提取

```
codegraph_search("ErrorCode" / "ErrorCodeConstants", kind="class")
  → 获取错误码常量文件路径

Read(错误码文件)
  → 提取格式：static final ErrorCode XXX_YYY = new ErrorCode(编号, "描述");
  → 生成错误码表（编号/常量名/描述/触发场景）
```

---

## 使用示例：对未知框架的处理

以 **SmartAdmin**（一个不在映射表中的框架）为例：

```
Step 1 — Read SmartAdmin 的 Controller 文件，发现 import：
  import net.lab1024.sa.base.common.annocation.auth.Auth;
  import net.lab1024.sa.base.common.annocation.log.SaLog;
  import io.swagger.v3.oas.annotations.Operation;

Step 2 — 包前缀匹配：
  net.lab1024.sa.* → 不在映射表中（未知框架）
  io.swagger.v3.oas.annotations → SpringDoc → @Operation=接口描述 ✓

Step 3 — 未命中注解语义推断：
  @Auth → 匹配 *Auth* 模式 → 推断为权限控制注解 → API 权限说明 [推断，需确认]
  @SaLog → 匹配 *Log* 模式 → 推断为操作日志注解 → SRS 审计需求 [推断，需确认]

Step 4 — 通用分层规则照常执行：
  @RestController → Controller 文件，提取路由
  @TableName → MyBatis-Plus Entity，提取 DDL
  Bean Validation → DTO 字段校验矩阵
```
