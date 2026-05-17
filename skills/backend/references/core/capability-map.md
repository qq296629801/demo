# IIDP 后端能力覆盖矩阵

本文件用于在生成 IIDP 后端 Demo 前，将平台能力映射到后端产物。遇到用户说“补齐 backend”“支持平台所有后端规范”时，先读本文件，再按专题加载其他 backend 文件。

## 使用方式

1. 识别用户需求属于哪些能力域。
2. 只加载对应 backend 文件，避免一次性读完所有文档。
3. 若某能力主要是前端扩展，也要在后端侧提供稳定的 `model`、`view`、`service`、`data` 或文件资源契约。

---

## 能力域总览

| 能力域 | 后端落点 | 主要参考 |
|---|---|---|
| 工程、应用、路由、部署 | Maven 模块、`app.json`、`apps/apps.json`、App 拆分、资源打包、环境配置 | `pom-structure.md`、`platform-standards.md` |
| 模型、字段、校验、ER 关系 | Java `@Model` / `@Property` / `@Validate` / ORM 注解 | `model.md`、`model-property-advanced.md`、`platform-standards.md` |
| 标准模板、搜索、表格、表单、树视图 | `views/*.json`、菜单 `view`、视图 key、`tbar/buttons/tabs` | `view.md`、`view-advanced.md`、`template-hook-openview.md` |
| 输入类组件、选择器、上传、富文本、日期、树、表格组件 | 字段类型、字典/选项、关联查询、组件属性、服务方法 | `component-field-mapping.md` |
| 菜单、菜单复用、菜单过滤、页面类型 | `data/menus.json`、`filter`、`config`、权限码 | `menu.md`、`security-permission-i18n.md` |
| 种子数据、附件、字典、初始化数据 | `data/*.json`、`file/*.json`、`file/document/*`、`@ref/@eval/@file*`、`isGlobal/policy` | `seed-data.md`、`menu.md` |
| 数据源、元模型 API、第三方 API、多数据源 | `@MethodService`、`RecordSet`、`Filter`、`dataSource`、服务入参出参 | `data-source-api.md`、`method-service.md`、`api-filter-sql.md` |
| Filter、JSON-RPC、原生 SQL、服务编排、异步 | Filter 波兰表达式、API 参数、SQL 参数化、Meta 上下文 | `api-filter-sql.md` |
| 视图模型、聚合查询、日期范围 | `ModelType.View`、`@View.*`、聚合 properties、日期 widget | `model-property-advanced.md`、`view-advanced.md` |
| 权限、自定义权限、系统权限、多语言 | `auth`、服务权限、菜单权限、语言包扫描、显示文案规范 | `security-permission-i18n.md` |
| 文件预览、上传、导入导出、打印、静态模板 | 文件模型字段、MinIO、`base_excel`、导入校验、打印服务 | `file-excel-print-job.md` |
| openView、弹窗/抽屉、上下表、主从孙、Hook | 视图契约、服务按钮、子表 ER、查询/保存/删除服务 | `view-advanced.md`、`template-hook-openview.md` |
| 视图联动、数据域、扩展继承、JSONPath | `bind_*`、`linkage`、`domains`、`mode: extension`、`inherit_ids`、`jsonpath` | `view-advanced.md` |
| XXL-Job、Redis、缓存、分布式锁、消息 | 启动模块、执行器、`RedisHelper`、环境配置 | `file-excel-print-job.md`、`platform-standards.md` |
| 测试、验收、质量门禁、版本管理、发版检查 | 编译、JSON 校验、资源登记、接口契约检查、CI/CD、Git 分支、tag | `validation-checklist.md`、`platform-standards.md` |

---

## 完整资料到后端契约

### 完整底稿归并矩阵

| 完整底稿 | backend 落点 |
|---|---|
| `../complete/create-model.md` | `model.md`、`model-property-advanced.md` |
| `../complete/create-property.md` | `model.md`、`model-property-advanced.md`、`component-field-mapping.md` |
| `../complete/date-type.md` | `model-property-advanced.md`、`view-advanced.md` |
| `../complete/view-model.md` | `model-property-advanced.md`、`api-filter-sql.md` |
| `../complete/method-service.md` | `method-service.md`、`api-filter-sql.md` |
| `../complete/api-params.md` | `data-source-api.md`、`api-filter-sql.md` |
| `../complete/filter.md` | `api-filter-sql.md`、`data-source-api.md` |
| `../complete/native-sql.md` | `method-service.md`、`api-filter-sql.md`、`platform-standards.md` |
| `../complete/basic-view.md` | `view.md`、`view-advanced.md`、`seed-data.md` |
| `../complete/uiview-guide.md` | `view.md`、`view-advanced.md`、`template-hook-openview.md`、`seed-data.md` |

### 标准页面与模板

- 主表格：至少生成 `grid + search + form` 三类视图，菜单 `view` 用逗号连接。
- 主表单 + 子表：Java 模型必须有 ER 字段，form 视图 `tabs.body.field` 与字段名一致。
- ER API 指令集：OneToMany/ManyToMany 保存时支持 `(0/1/2/3/4/5/6, ...)` 指令，服务端要校验关系合法性。
- 种子数据：普通初始化数据用 `data/*.json`，附件配置用 `file/*.json` 并引用 `file/document/*`；关系引用用 `@ref/@eval`，文件引用用 `@fileId/@fileUrl/@filePath`。
- 树表模板：树模型必须提供父子字段或可查询树结构，tree 视图配置 `subModel/subViewFilter`。
- 上下表模板：主表和子表分别有 grid/search/form，子表查询条件由主表选中行拼接。
- 高级视图：`bind_*`、`linkage`、`reqPrep/reqAfter`、`searchByMainTable/searchConfig`、`pagin/domains` 都必须映射到稳定字段和后端服务。
- 扩展视图：`mode: extension`、`inherit_ids`、`jsonpath` 需要被继承视图可解析，并声明跨 App 依赖。
- 卡片列表、侧边栏 + 主内容、自定义页面：多数为前端布局，后端至少提供数据模型、查询服务、菜单入口和必要视图 key。

### 组件能力

- 纯展示组件（Badge、Alert、Tooltip、Progress、Timeline、Steps、Breadcrumb 等）：后端提供字段、状态枚举和视图 `columns` 配置。
- 输入组件（Input、Textarea、InputNumber、DatePicker、TimePicker、Calendar、Switch、Radio、Checkbox）：后端通过 `@Property` 类型、`@Validate`、`@Selection`、`@Dict` 和视图 `custom/type` 支撑。
- 选择器（Select、Lookup、LookupTable、Cascader、ConditionalTree、Transfer）：后端必须提供可查询数据源、选项服务或关联模型。
- Upload/Image/RichText/FilePreview：后端必须有文件字段、文件服务依赖、MinIO 配置和预览/上传契约。

### 扩展与 Hook

前端 Hook 文档里的新增、删除、查询、保存、按钮控制，本质上都依赖后端服务稳定：

- 查询 Hook：保证 `search/count/searchPage/lookup` 入参稳定。
- 保存 Hook：保证 `create/update/delete` 或自定义服务可被视图按钮调用。
- 按钮 Hook：视图 `auth/action/service/args/actionAfter` 要写清楚。
- openView：被打开视图的 `model/view/service` 要能单独加载和执行。
- Filter：所有查询服务必须支持平台 Filter 数组和波兰表达式，不能只支持简单等值查询。
- 原生 SQL：只用于模型 API 无法表达或明确性能场景，必须参数化并在写 SQL 后清缓存。
- 视图模型：聚合查询、跨字段映射优先用 `ModelType.View` 和 `@View.MapProperty/@View.MapFunction`。

### 平台规范

- 所有 Java 后端代码同时满足 IIDP 注解规范、当前工程版本、阿里 Java 规约、环境与中间件规范。
- 新增能力必须能通过 `validation-checklist.md` 的基础检查。
- 对纯前端展示能力，不在后端强行生成无意义 Java；只生成必要契约。
