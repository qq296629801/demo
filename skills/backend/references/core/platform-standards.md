# IIDP 后端开发规范

本文件把 IIDP 平台约定、`iidp-backend-demo` 工程现状、阿里 Java 规约和环境规范合并为后端生成时的硬性标准。

规范约束力分三类：

- **强制**：生成代码、文档或资源时必须满足；如与现有工程冲突，先说明冲突并按可运行架构处理。
- **推荐**：默认采用，除非业务场景、性能或既有工程模式有明确理由偏离。
- **参考**：用于设计取舍和说明，不作为硬性验收失败条件。

## 优先级

1. 用户提供的技术方案或已确认命名。
2. 当前仓库已有工程模式和版本。
3. `skills/backend` 的 IIDP 约定。
4. 阿里 Java 开发规范与环境规范。

如果外部通用规范与 IIDP 平台写法冲突，以 IIDP 平台能运行的写法为准，并在交付说明中点明取舍。

---

## 命名规范

| 对象 | 规则 | 示例 |
|---|---|---|
| `appName` | 小写中划线 | `demo-books` |
| `appPkg` | 全小写包段 | `books` |
| `moduleName` | 小写英文目录 | `bookmgr` |
| 模型名 | 小写下划线，全局唯一 | `books_manage` |
| Java 类 | UpperCamelCase | `BooksManage` |
| 字段名 | lowerCamelCase | `bookCode` |
| 常量 | 全大写下划线 | `BOOK_STATUS_ENABLE` |
| 视图 key | 小写下划线，带模型前缀 | `books_manage_grid` |
| 菜单 key | app 前缀 + 模块/实体 | `books_bookmgr_root_menu` |

强制规则：

- `app.json.name`、Java `@Model(name)`、菜单 key、视图 key 必须全局唯一，并带明确业务前缀。
- 模型名使用小写下划线，不加无意义 `model` 后缀，不使用驼峰拼接。
- 项目名、App 名、Maven artifactId 使用字母、数字、下划线或短横线，不包含空格和特殊字符。
- 包名使用全小写；工具类、视图目录等复数概念可使用复数命名，如 `utils`、`views`。

禁止：

- 拼音和英文混用。
- 无意义命名，如 `data1`、`temp2`。
- 菜单、模型、视图 key 不带业务前缀导致跨 App 冲突。
- 多父模型写成逗号拼接字符串，必须用 `parent = {"a", "b"}`。
- 直接在代码中重复散落模型名、状态值、字段名、服务名等魔法字符串。

---

## Java 代码规范

生成模型类必须：

- 使用 `@StaticVar`、`@Getter`、`@Setter`，需要日志时使用 `@Slf4j`。
- 继承 `BaseModel<T>`；复杂服务分层使用 `SdkService<T>`。
- 每个可持久化字段有 `@Property(displayName = "...")`。
- 必填、长度、唯一性在模型层用 `@Validate` 表达。
- 下拉、字典、关联选择用 `@Selection`、`@Dict`、ORM 注解表达。
- 魔法字符串抽为常量，尤其是模型名、状态值、字段名、服务名。
- 业务异常给用户可理解的中文提示；日志记录上下文，不泄露敏感值。

禁止：

- `System.out.println`、`printStackTrace`。
- 捕获 `Exception` 后吞掉异常。
- 返回含糊的 `false` 但不说明失败原因。
- 在模型类里堆叠超过 80 行的复杂业务流程；应拆到 Service。
- 无参数校验地执行批量更新、删除、导入。
- 使用 Spring Boot 原生注解和写法：`@Service`、`@Repository`、`@Controller`、`@RestController`、`@RequestMapping`、`@GetMapping`、`@PostMapping`、`@Autowired`、`@Component`、`@Bean`、`@Configuration`、`@SpringBootApplication`——IIDP 以元模型驱动，所有模型继承 `BaseModel<T>`，服务分层使用 `SdkService<T>`，不使用 Spring MVC / Spring IoC 原生体系。

注释要求：

- 公共模型、服务类、复杂业务方法需有简洁 Javadoc，说明业务含义、关键入参、返回值和异常条件。
- 注释解释“为什么这样做”，不要复述代码语法。

依赖要求：

- 不新增无明确业务必要的第三方依赖，避免 App 包过大和版本冲突。
- 当前工程已有依赖按父 POM 和专题模板使用，不因旧规范中“禁止引入某包”的泛化描述主动删除现有可运行依赖。

---

## 模型与 App 设计规范

IIDP 后端以元模型驱动为核心：一切皆模型，模型可继承、可扩展，可按业务选择 `Buss/Data/Memory/Tree/Config/View` 等类型。

强制规则：

- 每个业务模型必须通过 `@Model` 声明，并继承 `BaseModel<T>`。
- 业务模型默认使用 `Buss`，抽象模型只做基类或扩展承载，不直接生成无意义页面。
- 唯一业务编码、外键、高频过滤字段和排序字段必须设计索引。
- 避免循环依赖；跨 App 继承、扩展、ER 关系必须在 `app.json.dependencies` 中体现安装顺序。
- OneToMany、ManyToOne、ManyToMany 必须声明清晰的字段名、目标模型和级联策略。
- 在循环中不要逐条读取 ER 关系字段，避免 N+1 查询；应批量查询后组装。

继承与扩展：

- `@Model(name = "child", parent = {"base"})` 表示继承。
- `@Model(name = "base", parent = {"base"})` 表示扩展已有模型。
- 多继承使用 `parent = {"base_a", "base_b"}`，不使用逗号拼接字符串。
- 子模型移除父模型服务时，应使用平台服务移除能力，不用空方法遮蔽。

App 拆分推荐：

- 按商业售卖、复用性、职责边界、性能、部署场景和变化频率拆分 App。
- 通用层级可按 `L1 平台通用 / L2 业务通用 / L3 产品通用 / L4 行业通用 / L5 定制` 识别。
- 高内聚领域模型放在同一 App；变化频繁或扩展型模型可拆到独立 App。
- 高并发或性能敏感能力可独立 App 化，降低运行时影响面。
- App 调用关系不应强制安装顺序；App 依赖关系会影响安装顺序，必须显式声明。
- N:1、N:N 可使用单边关系减少跨 App 耦合；1:N 不适合跨 App 循环依赖场景。

---

## 异常与事务

| 场景 | 建议 |
|---|---|
| 用户输入错误 | 抛 `ValidationException`，提示具体字段或业务原因 |
| 平台/模型错误 | 保留 `ModelException`，不要包装丢失原因 |
| 业务 App 异常 | 需要国际化或统一拦截时继承平台 `AppException` 体系，动态参数用占位符 |
| 业务流程失败 | 抛明确异常，触发事务回滚 |
| 导入部分失败 | 使用导入校验结果返回错误行，不要静默跳过 |

事务原则：

- 默认使用 IIDP 请求级事务，抛异常自动回滚。
- 手动 `flush/commit` 只用于确需分段提交的场景。
- 跨 App 事务要先检查依赖顺序和幂等性。

---

## SQL 与数据访问

优先级：

1. `BaseModel` / `RecordSet` / `DbUtils` / `SdkService`。
2. `Filter` 组合查询。
3. 直接 SQL，仅在复杂统计或平台 API 无法表达时使用。

直接 SQL 必须：

- 使用参数化占位，不拼接用户输入。
- 控制返回字段和分页。
- 大表查询必须有索引条件。
- IN 列表控制大小，必要时分批。

Filter 规范：

- Filter 使用平台数组表达式和波兰表达式，逻辑运算符为 `|`、`&`、`!`。
- 条件使用 `["field_name", "operator", value]`，关系字段可用点号访问。
- 常用操作符包括 `= != > >= < <= like ilike not like not ilike in not in child_of parent_of`。
- 操作符大小写要按平台规范写，不能混用大写 `AND/OR` 与小写 `and/or` 造成解析差异。
- 自定义查询服务必须继续支持平台 Filter，不要改成只接收固定字段。

---

## SDK 与服务调用规范

- 优先使用平台 `BaseModel`、`RecordSet`、`DbUtils`、`Filter` 和 `@MethodService` 能力。
- 不建议 `new` 一个模型对象绕过元模型能力；跨模型调用使用 `getMeta().get(model)`、`RecordSet.call` 或平台统一调用方式。
- 服务名、模型名、字段名要使用常量集中管理，避免字符串散落。
- 只使用 SDK 必要能力；复杂扩展优先使用 SDK 提供的扩展点，不绕过引擎上下文。
- 异步线程或外部回调调用模型服务时，必须创建独立 `Meta` 上下文并做好清理。

---

## 环境与配置

| 项 | 规范 |
|---|---|
| JDK | 主工程 Java 8，XXL-Job 模块可 Java 17 |
| Maven | 使用当前工程父 POM 管理版本 |
| MySQL | 字符集 UTF-8/utf8mb4，索引命名清晰 |
| DDL | 首次开发可 `CREATE/UPDATE`，生产必须 `VALIDATE` |
| Redis | Key 带业务前缀和 TTL，锁必须设置过期 |
| MinIO | 文件能力必须确认 bucket、endpoint、accessKey 配置 |
| 配置安全 | 不提交生产密码、Token、密钥 |
| 授权 | Dev 环境可不走应用市场授权，非 Dev 启动前必须确认平台和租户授权 |
| 部署系统 | 后端部署推荐 Linux，避免开发和部署 OS/中间件版本差异过大 |

部署要求：

- 明确单机或分布式模式，配置 `engine.run.mode`，不要让部署模式隐含在口头说明中。
- 开发、测试、部署环境应尽量保持 JDK、数据库、Redis、MinIO、配置文件结构一致。
- 环境差异必须在交付说明中列出，并补充必要验证。

---

## 资源与打包

- `app.json` 必须位于 `resolved` 对应包路径下。
- `src/main/java` 下的 `json/xlsx/png/md` 等资源必须通过 Maven resource 配置打包。
- 新增业务 jar 必须登记到 `apps/apps.json` 的 `apps.SDK`。
- 新增视图、菜单、字典、模板文件必须登记到 `app.json` 的 `view` 或 `data`。

建议目录约定：

- `app.json`、业务模块目录、`data/`、`file/` 位于 `resolved` 包路径下。
- 同一业务模块内保持 `model/`、`service/`、`views/` 清晰分层。
- 视图 JSON 字段名、服务入参和 Java 模型字段名必须能互相追踪。

---

## 权限、多租户与国际化

权限：

- 平台启动和租户启用应满足授权要求；多租户场景需要确认租户可用 App 和数量限制。
- 行权限、列权限、菜单权限、按钮权限和服务权限共同形成闭环，后端写服务不能只依赖前端隐藏。
- 千人千面优先使用平台行列权限、作用域和菜单过滤表达，必要时服务端合并用户上下文。

多语言：

- 语言包命名为 `messages_xx-XX.properties`，并随 App Jar 打包。
- 常见文件：`messages_zh-CN.properties`、`messages_en-US.properties`、`messages_ja-JP.properties`。
- 用户自定义语料使用 `messages_user_defined_xx-XX.properties`，同 key 时优先于内置语料。
- 翻译 key 可按模型字段、服务、菜单、视图、异常、日志或 App 通用粒度组织。
- 菜单 key 使用 `$menus.menuKey:原文=译文`，视图 key 使用 `$views.viewKey:原文=译文`。
- 异常建议使用平台异常体系和动态参数占位符，非异常提示需业务服务自行调用平台翻译能力。

---

## 日志、测试与质量门禁

日志：

- 使用平台日志能力和 `@Slf4j`，让日志包含 model、service、请求 ID、线程、用户、业务 ID 和关键参数。
- 日志用于定位问题，不输出明文密码、Token、密钥、完整 SQL 或大体量请求体。

测试：

- 后端服务使用 `@ExtendWith(SieEngineTestExtension.class)` 集成测试，每个测试聚焦一个功能点或场景。
- 关键服务覆盖正常、边界、异常、权限、租户隔离和幂等场景。
- 功能测试需覆盖页面布局、交互、表单验证、查询、保存和权限。
- 集成测试需覆盖跨模型、跨 App、第三方 API、多数据源、文件、Excel、任务等链路。
- 性能测试需明确目标、环境、数据量、负载模型、监控指标和结果报告。
- 回归测试必须覆盖本次改动影响的旧流程。

质量门禁：

- CI/CD 流程应包含编译、集成测试、静态分析。
- 敏感信息通过环境变量或配置中心注入，不写入代码仓库。
- 可使用 SpotBugs 等静态分析工具重点扫描 SQL 注入、空指针、资源泄漏等风险。

---

## 生成前检查

- 技术方案里的 P0 模型、菜单、页面不能遗漏。
- 每个模型至少有列表/搜索/表单视图，除非明确是纯接口或配置模型。
- 所有自定义按钮都有后端服务或明确是纯前端动作。
- 所有 `auth` 都有稳定权限码。
- 所有外部依赖（Redis、MinIO、XXL-Job、RabbitMQ）都有配置说明和降级/异常处理。

---

## 版本管理与协作

- 使用 Git 进行版本管理，按项目约定选择分支策略，如 `main/master`、`develop`、`feature`、`release`、`hotfix`。
- 不建议在多人协作分支上随意 `rebase`，避免重写提交历史造成冲突和追踪困难。
- 提交消息要有业务含义，建议使用 `feat:`、`fix:`、`docs:`、`refactor:`、`test:` 等前缀。
- 合并前需要代码审查，合并后运行必要测试。
- 稳定发布版本使用 Git tag 标识版本号和发布日期。
- 定期清理过期分支和标签，保持仓库可维护。
