---
name: backend-demo-generator
description: >-
  Use when the task involves IIDP backend or Demo development, including IIDP
  工程、模型、@Model、app.json、JSON 视图、菜单、@MethodService、权限、
  API/Filter/SQL、文件、Excel、打印、定时任务，或用户提到“补齐 backend”、
  “完善 backend skill”、“IIDP 后端规范”、“生成后端代码”。
---

# IIDP 后端开发与 Demo 工程生成 Skill

## Backend 文件索引

读取此 SKILL.md 后，按需加载以下 backend 文件。未来交付以 `skills/backend` 为准，不依赖外部旧目录。

| 文件 | 内容 | 何时读取 |
|---|---|---|
| `greenfield/SKILL.md` | 从零克隆 IIDP 父工程、首次环境、首包与可选 Compose；与根 Skill 分工见该文件 | 用户明确「全新后端项目」「从零建 IIDP 工程」「先搭 demo 父工程」或工作区无目标聚合工程时 **先于** Step 2 读取并执行 |
| `references/core/capability-map.md` | IIDP 后端能力域到产物的覆盖矩阵 | Step 0 能力识别；用户要求“补齐 backend”或“覆盖平台全能力”时必须先读 |
| `references/core/platform-standards.md` | IIDP 平台开发规范：命名、常量、模型与 App 设计、SDK、日志、测试、权限、多租户、国际化、版本、部署、环境与资源打包硬性规范 | Step 0 规范加载；写 Java、POM、资源、服务、测试、部署说明前必须读 |
| `references/core/pom-structure.md` | 工程目录结构、POM 版本、apps.json、Dockerfile、docker-compose.yml 与本地部署配置 | Step 2~3、Step 8~9；用户要求 Docker/Compose/环境部署时必须读 |
| `references/core/app-json.md` | app.json 完整字段说明（name/resolved/view/data/events 等）与两类示例 | Step 4 创建 app.json 时必须读 |
| `references/core/model.md` | @Model/@Property/@Validate/@Selection/ER 关系注解详细参数 | Step 5 编写模型类 |
| `references/core/model-property-advanced.md` | 模型层高级能力：模型类型、继承扩展、ER 指令集、属性高级参数、日期、视图模型 | Step 5 涉及模型/属性/日期/视图模型高级能力时必须读 |
| `references/core/method-service.md` | @MethodService 全场景：CRUD重写、业务方法、Excel、RPC、SQL、事务、Redis、DTO 服务分层 | Step 5 需要服务方法时 |
| `references/core/api-filter-sql.md` | 接口层能力：JSON-RPC、Filter 波兰表达式、内置服务参数、服务编排、异步、原生 SQL | Step 5~6 涉及接口、Filter、SQL、编排、异步时必须读 |
| `references/core/view.md` | 后端视图 JSON 完整结构、search/grid/form/tree 配置、tbar/buttons、子表、扩展视图 | Step 6 编写视图文件 |
| `references/core/view-advanced.md` | 后端视图高级能力：联动、上下表、选择器、弹窗、数据域、扩展继承、JSONPath | Step 6 涉及高级视图或复杂模板时必须读 |
| `references/core/menu.md` | menus.json 结构、字段说明、菜单过滤、菜单复用、多模块示例、字典种子数据 | Step 7 配置菜单时 |
| `references/core/seed-data.md` | 普通数据种子、附件种子、`@ref/@eval/@file*`、`${}` 变量、`isGlobal/policy`、Java 注解到 seed 字段映射 | Step 7 涉及菜单以外的初始化数据、字典、附件、权限规则、跨 App 引用或种子占位符时 |
| `references/core/component-field-mapping.md` | 组件到模型字段、字典、关联、服务、视图配置的映射 | 用户提到组件、选择器、上传、富文本、日期、树、表格组件时 |
| `references/core/data-source-api.md` | 元模型 API、内置服务、自定义服务入参、多数据源、第三方 API、保存契约 | 用户提到数据源、公共 API、Lookup、openView 服务、接口调用时 |
| `references/core/security-permission-i18n.md` | 系统权限、自定义权限、菜单权限、作用域、多语言、可见文案规范 | 用户提到权限、菜单过滤、按钮权限、多语言、用户上下文时 |
| `references/core/file-excel-print-job.md` | 文件预览、上传、图片、Excel 导入导出、静态模板、打印、进度、XXL-Job | 用户提到文件、Excel、导入导出、打印、进度条、定时任务时 |
| `references/core/template-hook-openview.md` | 标准模板、主表单+子表、上下表、树表、抽屉、Hook、openView 后端契约 | 用户提到标准模板、Hook、openView、弹窗/抽屉、上下表、树表时 |
| `references/core/validation-checklist.md` | 交付前文件登记、命名、Java、视图、服务、静态命令、运行验证清单 | 交付前必须读并按可行范围执行 |
| `references/core/testing.md` | IIDP 集成测试：`@ExtendWith(SieEngineTestExtension.class)`、Meta 上下文获取、状态机服务测试、租户上下文、TC-BE 覆盖优先级 | 编写或审查 `@MethodService` 测试时 |
| `references/core/tdd.md` | 测试驱动开发（TDD）规范：铁律、红-绿-重构流程、常见借口对照、警示信号 | 编写任何新业务代码前必须读；用户提到 TDD、测试先行、红绿重构时必须读 |

完整底稿也已迁入 `skills/backend/references/complete/*.md`。优先使用上表中的精炼专题；当专题信息不足、用户要求完整平台细节，或需要核对边界示例时，再读取下表对应的 `references/complete/...` 文件。

| 完整底稿 | 覆盖内容 |
|---|---|
| `references/complete/create-model.md` | 创建模型、模型类型、继承扩展、ER 关系、索引 |
| `references/complete/create-property.md` | 创建属性、Validate、Selection、Dict、Related、Compute、自动字段 |
| `references/complete/date-type.md` | 日期/时间模型字段与视图 widget |
| `references/complete/view-model.md` | 视图模型、映射、聚合查询 |
| `references/complete/method-service.md` | 方法服务、内置服务、服务编排、异步 |
| `references/complete/api-params.md` | JSON-RPC 接口参数与响应结构 |
| `references/complete/filter.md` | Filter 表达式、操作符、波兰表达式 |
| `references/complete/native-sql.md` | 原生 SQL 执行、多条件、IN 查询、缓存失效 |
| `references/complete/basic-view.md` | 基础视图 form/grid/search/card/menu/tbar/buttons/tabs/tree |
| `references/complete/uiview-guide.md` | 后端视图完整手册、高级视图、联动、选择器、权限、扩展 |

## 能力识别流程（Step 0 必须执行）

1. 先读 `references/core/capability-map.md`，把用户需求映射到后端产物：`pom/app.json`、Java 模型、视图 JSON、菜单/数据、服务、权限、文件模板、任务或外部 API。
2. 再读 `references/core/platform-standards.md`，确认命名、Java 规约、异常事务、SQL、环境配置、资源打包和安全要求。
3. 只加载与需求相关的专题文件；如果用户要求“补齐 backend”或“资料不能遗漏”，至少加载能力矩阵中命中的所有专题和完整底稿。
4. 对纯前端展示能力，不生成无意义 Java 类；只生成必要的模型字段、服务、视图 key、菜单入口和资源契约。
5. 交付前读 `references/core/validation-checklist.md`，按当前修改范围执行静态验证；能访问依赖时再跑 Maven 编译。

---

## 命名规范（Step 1 必须先确认）

| 命名项 | 格式 | 示例 |
|---|---|---|
| appName | 小写中划线 | `demo-books` |
| appPkg | 全小写英文（Java 包） | `books` |
| moduleName | 小写英文（业务模块文件夹名） | `bookmgr` |
| 模型名 model name | `{appPkg}_{entity}` 小写下划线 | `books_manage` |
| Java 模型类名 | PascalCase | `BookManage` |
| 视图文件名 | `{model_name}_view.json` | `books_manage_view.json` |
| 菜单 key | `{appPkg}_{module}_menu` | `books_bookmgr_root_menu` |
| Maven artifactId | `sie-iidp-demo-{appName}` | `sie-iidp-demo-books` |

---

## 生成步骤

### Step 1：确认命名规范
从用户输入提取并确认上方命名表中的所有命名项，有歧义时主动询问。

### Step 2：工程结构声明
读取 `references/core/pom-structure.md` 和 `references/core/platform-standards.md` → 说明需新增/修改的文件清单：
- 根 `pom.xml` 的 `<modules>` 新增子模块
- `sie-iidp-demo-apps/pom.xml` 的 `<modules>` 新增业务模块
- 子模块 `pom.xml` 保留 `src/main/java` 资源打包配置，确保 `app.json`、`views/*.json`、`data/*.json` 被打进 jar

### Step 3：创建子模块 pom.xml
路径：`sie-iidp-demo-apps/sie-iidp-demo-{appName}/pom.xml`
参见 `references/core/pom-structure.md` → "子模块 pom.xml 模板"章节。

### Step 4：创建 app.json
路径：`src/main/java/com/sie/iidp/{appPkg}/app.json`
**关键：** 必须放在 `resolved` 字段对应的 Java 包路径下。
参见 `references/core/app-json.md`（字段定义、view/data 登记规则、events 钩子完整示例）。

### Step 5：编写 Java 模型类

**多模型业务时，必须按 ER 依赖顺序对 Step 5～7 进行循环展开：被引用的模型先写。**

执行规则：
1. 在开始 Step 5 前，先列出本次功能涉及的所有模型清单（含 model_name、Java 类名、ER 关系）。
2. 按依赖顺序对每个模型完整执行 Step 5（Java 类）→ Step 6（视图）→ Step 7（菜单/数据），不要全部 Java 写完再统一写视图。
3. 跨模型自定义服务在所有模型 Java 类完成后再补充：可挂在主模型上，但需明确事务边界和涉及的所有模型。事务遵循 `references/core/method-service.md` 事务控制章节与 `references/core/platform-standards.md` 事务原则——默认使用 IIDP 请求级事务，业务流程失败抛 `ModelException` 自动回滚；确需分段提交才用 `Meta` 手动 `flush/commit`。
4. 每完成一个模型，在 `app.json` 的 `view` 和 `data` 数组中**同步**登记该模型的视图和菜单文件路径，不要等到最后再批量登记（容易遗漏）。

---

路径：`src/main/java/com/sie/iidp/{appPkg}/{moduleName}/model/{ModelName}.java`

读取 `references/core/model.md` 获取注解详细参数；读取 `references/core/platform-standards.md` 约束命名、异常、事务、SQL、安全和代码规约。
涉及模型类型、继承/扩展、ER 指令集、属性高级参数、分组校验、日期、Dict/Selection 回显或视图模型时，读取 `references/core/model-property-advanced.md`。
按需读取 `references/core/component-field-mapping.md`、`references/core/method-service.md`、`references/core/data-source-api.md`、`references/core/api-filter-sql.md`、`references/core/file-excel-print-job.md` 获取组件映射、服务、接口、Filter、SQL、外部 API、文件/Excel/打印/任务写法。

**生成任何 Java 模型类前，必须先读取 `references/core/model.md` 开头的 import 区，以该文件中的 import 语句为准生成所有 import，不得凭训练知识补全或替换。** 尤其注意：`@StaticVar`、`@Getter`、`@Setter` 来自 `com.sie.meta.plugin`，不是 Lombok；`@Slf4j` 来自 `lombok.extern.slf4j.Slf4j`，是唯一允许的 Lombok import。

模型类必须包含：
- 类头：`@StaticVar @Getter @Setter @Slf4j @Model(...)`
- 继承：`extends BaseModel<{ModelName}>`
- 字段：`@Property` + 按需加 `@Validate` / `@Selection` / `@Dict` / ER 关系注解
- 服务方法：按业务需要添加 `@MethodService`
- 服务分层判断：编写每个 `@MethodService` 方法前，先按下表评估是否需要拆到 `service/` 目录的 `SdkService<T>` 子类

**强制拆分**（直接生成 `service/` 目录，模型类只保留 `@MethodService` 入口）：

| 触发条件 | 说明 |
|---|---|
| 方法体 > 40 行 | 超出单方法认知负荷上限（不含注解、方法签名、空行）|
| 涉及 ≥ 3 个模型 | 跨域编排属于应用服务层职责 |
| 同一逻辑被 ≥ 2 个 `@MethodService` 复用 | 复用逻辑不重复写在模型类 |
| 包含 Savepoint 局部回滚 | 事务编排属于服务层职责 |
| 调用外部 HTTP / 消息队列 / 异步 Job | 外部依赖不耦合进领域模型 |
| 圈复杂度 > 5（if/else/switch 分支超过 5 条）| 超出单方法可读性上限 |

**建议拆分**（生成代码前提示用户确认）：

| 触发条件 | 说明 |
|---|---|
| 方法体 21–40 行且涉及 ≥ 2 个模型 | 跨域逻辑开始超出模型自治边界 |
| 方法体 21–40 行且涉及 1 个模型 | 单模型但逻辑较长，可考虑提取 |
| 涉及 2 个模型且无其他强制触发 | 跨域协调信号，视复杂度决定 |

**保留模型类**（方法体 ≤ 20 行、仅涉及 1 个模型、无强制触发条件）：直接在模型类内实现。

强制拆分时，读取 `references/core/method-service.md` 的「DTO 入参出参与 Service 分层」章节生成 `service/` 目录结构；建议拆分时，向用户说明触发原因并等待确认后再决定生成位置。
- 查询/聚合：优先模型 API 和 Filter；复杂统计可用 View Model 或受控原生 SQL

### Step 6：编写视图文件（JSON View）
路径：`src/main/java/com/sie/iidp/{appPkg}/{moduleName}/views/{model_name}_view.json`

读取 `references/core/view.md` 获取完整结构；涉及高级视图、联动、弹窗、上下表、数据域、扩展继承时必须读取 `references/core/view-advanced.md`；涉及接口入参、Filter、分页、排序、服务响应时读取 `references/core/api-filter-sql.md`；按需读取 `references/core/template-hook-openview.md`、`references/core/component-field-mapping.md`、`references/core/data-source-api.md` 和 `references/core/security-permission-i18n.md`。
每个普通模型标准包含三段视图：
- `{model_name}_grid`：列表视图
- `{model_name}_search`：搜索视图
- `{model_name}_form`：表单视图

视图文件必须是 `{ "views": { ... } }` 根结构；菜单中的 `view` 字段通常配置为逗号分隔的视图 key，例如 `{model_name}_grid,{model_name}_search,{model_name}_form`。新生成模块建议在 `app.json` 的 `view` 数组中显式登记视图文件路径。

如果需求涉及树表、上下表、主表单 + 子表、Hook、openView/useOpenView/useSelectTree、抽屉/弹窗、导入导出、文件预览、自定义按钮、`searchByMainTable/searchConfig/pagin/domains`、`mode: extension/jsonpath`，必须确保视图中的 `model/service/args/auth/action/openView` 能对应到真实后端模型和服务。

### Step 7：配置菜单与种子数据
路径：`src/main/java/com/sie/iidp/{appPkg}/data/menus.json`

读取 `references/core/menu.md` 获取完整结构和多模块示例；涉及权限、菜单过滤、菜单复用、多语言文案时读取 `references/core/security-permission-i18n.md`。
涉及普通初始化数据、字典、附件、权限规则 `config`、跨 App 引用、`@ref/@eval/@fileId/@fileUrl/@filePath` 或 `${meta.*}` 变量时，必须读取 `references/core/seed-data.md`。

注意事项：
- 菜单 key 全局唯一，统一加 `{appPkg}_` 前缀
- `parent_id` 值必须与父菜单的 `name` 完全一致；需要多父菜单或引用写法时用 `parent_ids: { "@ref": "父菜单name" }`
- 功能菜单的 `view` 可配置一个或多个后端视图 key，多个 key 用英文逗号连接
- `data/menus.json` 路径必须在 `app.json` 的 `data` 数组中登记
- 普通数据种子放 `data/*.json`，每个 seed 的 `model` 必须对应 Java `@Model(name)`，`properties` 使用 Java 字段名而不是 `@Property(columnName)`
- 附件种子放 `file/*.json`，真实文件放 `file/document/*`；引用附件时使用 `@fileId(key)`、`@fileUrl(key)` 或 `@filePath(key)`
- 所有 `data/*.json`、`file/*.json` 种子配置都要在 `app.json.data` 中登记
- 若有字典种子数据（如 `yes_no.json`），同样在 `data` 数组中逐条追加
- ManyToOne 使用 `{ "@ref": "seed_key" }`；ManyToMany 关系使用 `@eval` 指令，例如 `{ "@eval": "[4, @ref(role_seed), 0]" }`
- 选择项字段写 `@Option.value`，`store = false` 计算字段不写入种子，平台级/租户级数据明确 `isGlobal` 与 `policy`
- 菜单和按钮权限码必须稳定，不能只靠前端隐藏控制后端写操作

### Step 8：注册到 apps/apps.json
读取 `references/core/pom-structure.md` → **§6.1 apps.json 结构与加载路径** 及 §6 全文。
将编译产物 jar 加入 `apps.SDK` 数组：
```json
"sie-iidp-demo-{appName}-1.0-SNAPSHOT.jar"
```

### Step 9：启动模块与本地 Docker Compose（首次搭建时）
通常复用已有 `sie-iidp-demo-start` 模块，无需修改。
参见 `references/core/pom-structure.md` → "启动模块配置"和"Docker Compose 本地部署"章节。

当用户提到 `docker-compose.yml`、Docker 部署、环境部署、本地一键启动、中间件部署时，必须同时检查：
- `Dockerfile` 是否指向真实存在的启动 jar（默认 `sie-iidp-demo-start/target/sie-iidp-demo-start-1.0-SNAPSHOT.jar`）。
- `docker-compose.yml` 是否包含 MySQL、Redis、MinIO、MinIO bucket 初始化和 `iidp-app` 服务。
- `docker/config/application.properties`、`application-dev.properties`、`dbcp.properties` 是否使用 compose 服务名：`mysql`、`redis`、`minio`。
- `apps/apps.json` 与 `/apps` 挂载目录是否一致；业务与平台 jar 物理文件在 `apps/modules/`（与 `pom-structure.md` §6 一致），且 `apps.SDK` 已登记需加载的 jar 名。
- 文档中是否说明首次启动、端口、默认账号、生产密码替换和 DDL 模式风险。

### Step 10：编译验证
交付前读取 `references/core/validation-checklist.md`，至少执行空白、Markdown/JSON、资源登记和命名一致性检查；能访问私服和依赖时再运行 Maven。

```bash
# 编译所有业务 app 模块
mvn clean package -pl sie-iidp-demo-apps -am

# 启动引擎
mvn spring-boot:run -pl sie-iidp-demo-start

# Docker Compose 本地启动
docker compose up -d --build

# 验证接口
# POST http://localhost:8060/root/api/master
```

---

## 常见问题速查

| 问题 | 原因 & 解决 |
|---|---|
| app.json 无法加载 | `resolved` 包路径与 app.json 实际位置不一致 |
| 视图找不到 | `app.json` 的 `view` 数组未登记该路径，或路径格式错误 |
| 模型表未创建 | `engine.model2ddl.mode` 未配置为 `CREATE` |
| jar 未被引擎加载 | `apps/apps.json` 的 `apps.SDK` 未添加编译产物 jar 名称，或 jar 不在 `apps/modules/` |
| 菜单不显示 | `data` 数组未登记菜单文件路径，或菜单 key 与已有数据冲突 |
| 跨 APP 关联失败 | 跨 APP 模型继承/ManyToMany 需在 `app.json` 配置 `strong` 依赖 |

---

## 示例测试提示词

以下提示词可用于验证 skill 触发效果：

1. `帮我生成一个图书借阅管理的 IIDP 后端工程，包含图书、读者、借阅三个模型`
2. `给我写一个 ExamOrder 模型，字段有订单编号、状态（下拉选）、总金额，重写 create 方法自动生成编号`
3. `生成 books_manage 模型的完整视图文件，列表需要导入导出按钮`
4. `新建一个 IIDP app，appName 是 demo-hr，包含员工和部门两个模块`
5. `写一个跨 APP 调用的 @MethodService，从 example 模块查学生同步到 books 模块的读者`
