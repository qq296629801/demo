# LLM Prompt 模板 — 代码转规格书

每个模板对应一种 Codebook 文档类型。使用时将 `{{...}}` 占位符替换为 codegraph 采集结果。

`SKILL.md` 是输出目录和断点恢复契约的唯一来源；本文件只描述各文档的生成提示与内容要求。所有详细功能设计都写入 `codebook/modules/{module}/`，顶层 `codebook/` 只保留系统级文档。

---

## 通用变量说明

```
{{PROJECT_NAME}}              项目名称
{{FRAMEWORK}}                 框架名（RuoYi / JEECG Boot / yudao-cloud / Spring Boot / NestJS / Django 等）
{{MODULE_NAME}}               当前分析的模块名称
{{MODULES}}                   模块列表（来自 Phase B）
{{CONTROLLERS}}               Controller 类列表（含方法签名）
{{CONTROLLER_SOURCE}}         Controller 完整源码（逐文件 Read）
{{ENTITIES}}                  Entity 类列表（含字段）
{{ENTITY_SOURCE}}             Entity/DO 完整源码
{{VO_SOURCE}}                 ReqVO/RespVO/DTO 完整源码
{{SERVICES}}                  Service 接口和实现类列表
{{SERVICE_SOURCE}}            ServiceImpl 关键源码
{{CALL_TRACE}}                当前功能的完整调用链（文本描述形式）
{{CALLERS}}                   某方法的调用者列表
{{ENDPOINT_LIST}}             模块接口功能清单（HTTP 方法 + 路径 + 名称 + 权限码）
{{API_ANNOTATIONS}}           API 注解信息（@ApiOperation / @Operation）
{{ERROR_CODES_SOURCE}}        错误码常量文件源码
{{FRONTEND_API_SOURCE}}       前端 API 调用源码（无前端则填"无"）

{{CODEGRAPH_STATUS_OUTPUT}}   codegraph status 命令的完整输出（节点计数、语言分布、kind 统计）
{{FILE_TREE}}                 顶层目录与包结构（来自 codegraph_files 或 ls 命令输出）
{{MODULE_SUMMARIES}}          各模块单段摘要（模块名 + 核心实体 + 主要功能，一行一条）
{{CROSS_MODULE_DEPENDENCIES}} 跨模块调用与依赖清单（来自 codegraph_callers 跨模块边）
{{SERVICE_DEPENDENCIES}}      当前模块依赖的外部 Service/远程调用/缓存/MQ 列表
{{MODULE_FILE_LIST}}          当前模块内所有源文件路径清单（来自 codegraph_files）
{{UI_CONTEXT}}                模块 UI 元数据打包（LIST_FIELDS/QUERY_FIELDS/FORM_FIELDS/OPERATIONS，Phase C C1 Step 5 打包）
{{LOG_ANNOTATIONS}}           操作日志注解列表（@Log / @OperateLog 覆盖的方法及描述）
{{PERMISSION_ANNOTATIONS}}    权限注解列表（@PreAuthorize / @SaCheckPermission 及权限码）
{{ROLES}}                     系统角色清单（来自权限定义文件或 menus.json 角色字段）
{{ENTITY_RELATIONSHIPS}}      实体关系描述（外键、关联表、一对多/多对多关系）
{{AUDIT_LOGS}}                操作日志或审计日志示例数据（来自 @Log 注解的描述字段）
{{API_BASE_URL}}              API 基础路径（来自 @RequestMapping 类级别路径或配置文件）
{{AUTH_METHOD}}               认证方式（JWT Bearer / Sa-Token / Session Cookie 等）
{{API_VERSION}}               API 版本标识（来自 @RequestMapping("/v1/...") 或 OpenAPI info.version；无则填"无"）
{{CALL_TRACE_JSON}}           调用链 JSON 数据（codegraph_callees 输出，含节点名/文件/行号）
{{SCENARIO_NAME}}             当前生成流程图的业务场景名称（如"创建订单"、"审批流转"）
{{ENTITY_NAME}}               当前模块主实体名称（英文，用于 HTML 文件命名）
{{LIST_FIELDS}}               列表页表格字段（来自 RespVO 字段，含字段名和中文标签）
{{QUERY_FIELDS}}              搜索栏筛选条件字段（来自 *QueryReqVO 或 @RequestParam）
{{FORM_FIELDS}}               新增/编辑表单字段（来自 CreateReqVO/UpdateReqVO，含校验规则）
{{DETAIL_FIELDS}}             详情页字段（来自 RespVO 详情字段，含子表/关联数据）
{{OPERATIONS}}                操作按钮清单（来自 ENDPOINT_LIST 权限码，归类为新增/编辑/删除/导出等）
{{WORKFLOW_STATES}}           状态流转定义（来自 status/state/auditStatus 枚举字段及转换规则）
{{HAS_CHILD_TABLES}}          是否有子表（true/false，来自 Entity 关联关系或 API 接口参数）
{{MODULE_DATABASE_SUMMARIES}} 各模块数据库模型摘要（模块名 + 表清单 + 关键外键关系，各模块一段）
{{MODULE_CALL_GRAPHS}}        各模块调用图摘要（来自各模块 hla.md 的 Mermaid 调用图）
{{MODULE_DEPENDENCY_GRAPHS}}  各模块依赖图摘要（来自各模块 hla.md 的 Mermaid 依赖图）
{{MODULE_PRECONDITIONS}}      各模块前置条件摘要（鉴权/配置/数据初始化/外部服务/调度触发）
{{GENERATION_STATS}}          生成统计信息（模块数 / 数据表数 / API 接口数 / 生成耗时）

{{IIDP_APPS_SOURCE}}          IIDP 后端 apps/apps.json 或各模块 app.json 的原始内容
{{IIDP_MENUS_SOURCE}}         IIDP menus.json 的原始内容
{{IIDP_FRONTEND_APPS}}        IIDP 前端 config/apps.json 的原始内容（无前端则填"无"）
{{IIDP_MODEL_SOURCE}}         IIDP @Model/@Property/@Validate/@Selection/@Dict 注解的相关源码
{{IIDP_VIEWS_SOURCE}}         IIDP 后端 views/*.json 的原始内容（逐文件合并）
{{IIDP_MODEL_VIEWS_SOURCE}}   IIDP 前端 model-views/*.js 的原始内容（无前端则填"无"）
{{IIDP_METHOD_SERVICE_SOURCE}} IIDP @MethodService 注解方法的源码（自定义服务）
{{IIDP_META_SERVICE_CALLS}}   IIDP 前端 httpMeta / vm.request 调用的源码（无前端则填"无"）
{{IIDP_FRONTEND_EXTEND_SOURCE}} IIDP 前端扩展源码（selector/type/view/beforeOperate/hook/ds_config）
{{IIDP_CAPABILITY_SOURCE}}    已生成的 menus.json + views.json + services.json + frontend-extends.json 摘要（用于 Prompt 17）
{{IIDP_ALL_BASELINE_SPECS}}   已生成的全部 baseline-spec JSON 内容（用于 Prompt 18 汇总 unresolved）
```

---

## Prompt 1 — 系统级 HLA

> 执行顺序：系统级第 1 步 | 依赖：全部模块采集完成 | 输出：`codebook/hla.md`

```
你是一名资深系统架构师，请根据以下代码分析结果生成系统级高阶架构文档（HLA）。

## 项目基本信息
- 项目名称：{{PROJECT_NAME}}
- 技术框架：{{FRAMEWORK}}
- 模块列表：{{MODULES}}

## codegraph 提取的符号统计
{{CODEGRAPH_STATUS_OUTPUT}}

## 顶层目录与包结构
{{FILE_TREE}}

## 模块摘要
{{MODULE_SUMMARIES}}

## 跨模块调用与依赖
{{CROSS_MODULE_DEPENDENCIES}}

请输出 Markdown，使用中文，专业规范，不要猜测代码中不存在的功能。

必须包含：
- 系统定位：描述整个系统是什么、服务于哪些用户、解决什么系统级问题。
- 系统架构图：Mermaid graph TB，展示客户端层、接入层、应用层、模块层、数据层。
- 模块拓扑图：Mermaid graph LR，展示模块之间的调用关系和依赖方向。
- 技术选型：表格列出层次、技术、版本、用途。
- 系统功能模块：只写模块级能力摘要，不写模块内部详细功能设计。
- 安全、部署、数据、缓存、消息等横切架构：仅写系统级机制。
- 前置条件总览：列出系统运行、鉴权、数据初始化、外部服务等跨模块前置条件。

禁止输出模块级 PRD、用户故事、接口明细或页面原型说明；这些内容由模块目录内文档承担。
```

---

## Prompt 2 — 模块 HLA

> 执行顺序：模块级第 2 步 | 依赖：模块源码采集完成 | 输出：`codebook/modules/{module}/hla.md`

```
你是一名模块架构师，请根据以下代码分析结果生成模块级 HLA。

## 模块信息
- 系统名称：{{PROJECT_NAME}}
- 模块名称：{{MODULE_NAME}}
- 框架：{{FRAMEWORK}}

## 模块入口与服务
{{CONTROLLER_SOURCE}}
{{SERVICE_SOURCE}}

## 数据模型
{{ENTITY_SOURCE}}

## 调用链与依赖
{{CALL_TRACE}}
{{CALLERS}}
{{SERVICE_DEPENDENCIES}}

请输出 Markdown，重点描述模块内部详细设计。

必须包含：
- 模块定位与职责边界。
- 模块调用图：Mermaid flowchart 或 sequenceDiagram，展示 Controller → Service → Repository/Mapper → 外部依赖。
- 模块依赖图：Mermaid graph LR，区分内部 Service、远程调用、缓存、消息、文件、第三方服务。
- 前置条件：权限、数据状态、配置项、外部服务、定时任务或事件触发条件。
- 关键业务流程：按核心接口或状态流转列出主流程、异常流程、副作用。
- 核心服务方法设计：只写复杂方法或事务方法，说明事务、步骤、异常、写入表和外部调用。
- 数据模型概览：模块内实体关系图，字段详情引用 `database.md`。

不要输出系统级拓扑，也不要把其他模块的详细功能复制进来。
```

---

## Prompt 3 — 模块 Overview

> 执行顺序：模块级第 1 步 | 依赖：模块边界确定 | 输出：`codebook/modules/{module}/overview.md`

```
请生成模块概览文档。

## 输入
- 项目名称：{{PROJECT_NAME}}
- 模块名称：{{MODULE_NAME}}
- 模块源码清单：{{MODULE_FILE_LIST}}
- 接口功能清单：{{ENDPOINT_LIST}}
- 关联实体：{{ENTITIES}}
- UI 上下文：{{UI_CONTEXT}}

输出内容：
- 模块简介：用业务语言描述模块能力。
- 功能入口清单：按接口或页面列出入口、权限码、触发方式。
- 文档导航：链接到本模块 `hla.md`、`srs.md`、`prd.md`、`user-stories.md`、`api.md`、`database.md`、`error-codes.md`、`flowcharts/`、`ui/`。
- 页面与交互导航：列出会生成的 HTML 原型文件和可演示流程。
- 待确认项：只列代码无法确认但影响业务理解的问题。
```

---

## Prompt 4 — SRS 软件需求规格

> 执行顺序：模块级第 3 步 | 依赖：模块 HLA | 输出：`codebook/modules/{module}/srs.md`

```
你是一名需求工程师，请根据以下代码分析结果逆向生成模块软件需求规格说明书（SRS）。

## 模块信息
- 系统名称：{{PROJECT_NAME}}
- 模块名称：{{MODULE_NAME}}
- 框架：{{FRAMEWORK}}

## 功能入口
{{CONTROLLER_SOURCE}}

## 操作日志覆盖的功能点
{{LOG_ANNOTATIONS}}

## 权限定义
{{PERMISSION_ANNOTATIONS}}

## 触发点
{{CALLERS}}

## 接口功能清单
{{ENDPOINT_LIST}}

输出 Markdown。每个 ENDPOINT_LIST 条目必须对应一个独立功能需求，不得合并或省略。

每个功能需求包含：
- 功能名称
- 描述
- 优先级
- 相关 API
- 触发方式
- 前置条件
- 主流程
- 异常流程
- 字段约束
- 唯一性约束
- 删除策略或状态变更策略

不要编造代码中不存在的需求，遇到不确定的地方用 [需确认] 标注。
```

---

## Prompt 5 — PRD 产品需求文档

> 执行顺序：模块级第 4 步 | 依赖：模块 SRS | 输出：`codebook/modules/{module}/prd.md`

```
你是一名产品经理，请将以下模块代码分析结果转化为面向产品和业务的 PRD 文档。

## 模块基本信息
- 产品/系统名称：{{PROJECT_NAME}}
- 模块名称：{{MODULE_NAME}}
- 目标用户：{{ROLES}}

## 模块功能
{{ENDPOINT_LIST}}

## 主要业务流程
{{CALL_TRACE}}

## 数据实体关系
{{ENTITY_RELATIONSHIPS}}

输出面向非技术读者，避免堆叠类名、方法名、包名。

必须包含：
- 背景与目标：模块解决的业务问题，以及"Why Now"（当前阶段为何需要此模块）。
- 用户角色矩阵：角色、目标、可执行操作。
- 功能详述：按功能域描述业务能力、前置条件、输入输出和成功标准。
- 业务流程描述：覆盖主流程、异常流程、状态变化。
- 范围边界（Non-Goals）：必须明确写出"本模块不负责什么"，至少 2 条。
- 成功指标：使用表格列出指标名、当前基线、目标值、衡量时间窗口（若代码中无法判断基线则标注 [待定]）。
- 风险与依赖：已知外部依赖、数据依赖、权限依赖，以及已识别的风险点。

禁止输出系统级整体 PRD；顶层不生成 PRD 文件。
```

---

## Prompt 6 — 用户故事

> 执行顺序：模块级第 5 步 | 依赖：模块 SRS、PRD | 输出：`codebook/modules/{module}/user-stories.md`

```
请根据以下模块接口列表、操作日志和角色定义，生成标准用户故事。

## 接口列表
{{ENDPOINT_LIST}}

## 操作日志
{{AUDIT_LOGS}}

## 角色定义
{{ROLES}}

输出 Markdown，按角色和业务流程分组。

每条用户故事包含：
- 故事：作为 / 我希望 / 以便
- 优先级：P0/P1/P2
- 前置条件
- 验收标准：Given / When / Then（至少 2 条场景，其中 1 条必须是异常或边界场景）
- 性能验收条件：对写操作和查询操作各注明预期响应时间（如"页面加载 ≤ 2s，接口返回 ≤ 500ms"；无法判断则标注 [需确认]）
- 关联 API
- 关联 UI 原型（如可对应到 `ui/*.html`）

只生成当前模块用户故事；顶层不生成跨模块用户故事汇总。
```

---

## Prompt 7 — API 文档

> 执行顺序：模块级第 6 步 | 依赖：模块 SRS | 输出：`codebook/modules/{module}/api.md`

```
你是一名 API 文档工程师，请根据以下代码分析结果生成模块 API 规格文档。

## 项目信息
- API Base URL：{{API_BASE_URL}}
- 认证方式：{{AUTH_METHOD}}
- 模块名称：{{MODULE_NAME}}
- API 版本：{{API_VERSION}}（如无版本控制则填"无"）

## Controller 完整源码
{{CONTROLLER_SOURCE}}

## ReqVO / DTO 完整源码
{{VO_SOURCE}}

## 错误码常量
{{ERROR_CODES_SOURCE}}

## 接口功能清单
{{ENDPOINT_LIST}}

## 前端 API 调用源码
{{FRONTEND_API_SOURCE}}

输出 Markdown，每个接口一个独立章节，章节数必须与 ENDPOINT_LIST 一致。

强制要求：
- 每个接口章节顶部必须列出：请求 Header（Content-Type、Authorization、Accept），以及是否需要租户头（Tenant-Id 等）。
- Request Body 使用字段校验矩阵：字段名、类型、必填、规则、错误提示。
- 创建接口和更新接口的必填字段可能不同，必须分别标注。
- 错误码必须来自源码，不得推测。
- 权限码必须来自权限注解，不得推测。
- 安全说明：为每个接口注明认证要求（Bearer Token / 免认证）、访问控制（角色/权限码）、是否有频率限制（Rate Limiting）。若源码中未体现限流，标注 [需确认]。
- 如 API_VERSION 非空，注明接口版本号及是否存在 Deprecated 标记。
- 如 FRONTEND_API_SOURCE 非空，为每个接口追加"前端调用层对照"表格；前后端字段差异用 `[需确认]` 标注。
```

---

## Prompt 8 — 数据库结构文档

> 执行顺序：模块级第 7 步 | 依赖：Entity 源码 | 输出：`codebook/modules/{module}/database.md`

```
请根据以下 Entity/DO 类完整源码生成模块数据库结构文档。

## Entity/DO 类完整源码
{{ENTITY_SOURCE}}

请输出：
- 数据库表清单：表名、中文名、模块、说明。
- 每张表详细结构：字段、类型、长度、可空、默认值、索引、说明。
- DDL：从源码和注解可推断的信息生成，无法确认处标注 [需确认]。
- ER 关系图：Mermaid erDiagram。
- 数据字典：枚举、常量、状态字段。

规则：
- 字段类型、长度、索引必须来自实际源码或注解，不得凭空猜测。
- 继承字段必须单独标注来源父类。
- 公共字段如 creator/create_time/updater/update_time/deleted/tenant_id 必须注明来源。
```

---

## Prompt 9 — 错误码文档

> 执行顺序：模块级第 8 步 | 依赖：错误码源码 | 输出：`codebook/modules/{module}/error-codes.md`

```
请根据以下错误码常量文件源码生成模块错误码文档。

## 错误码常量文件源码
{{ERROR_CODES_SOURCE}}

输出错误码表：错误码编号、常量名、中文描述、触发场景、关联功能。

规则：
- 错误码编号和常量名必须与源码完全一致。
- 触发场景可从常量名和调用点推断，不确定则标注 [需确认]。
- 按错误码编号升序排列。
- 如一个文件包含多个模块的错误码，只提取当前模块相关项。
```

---

## Prompt 10 — 流程图生成

> 执行顺序：模块级第 9 步 | 依赖：模块 SRS、API、调用链 | 输出：`codebook/modules/{module}/flowcharts/`

```
请根据以下调用链分析结果生成模块业务流程图。

## 调用链数据
{{CALL_TRACE_JSON}}

## 业务场景名称
{{SCENARIO_NAME}}

请为每个写操作和关键查询流程生成一组文件：
- `{operation}-{entity}-flow.mmd`
- `{operation}-{entity}-flow.svg`

Mermaid 内容必须包含：
- 用户操作视角流程图：flowchart TD，节点用业务语言，判断节点用菱形，异常路径标红。
- 技术调用链时序图：sequenceDiagram，参与者不超过 6 个。

不要把多个写操作合并成一个流程图。
```

---

## Prompt 11 — UI/UX 静态页面

> 执行顺序：模块级第 10 步 | 依赖：模块 API 字段、UI_CONTEXT | 输出：`codebook/modules/{module}/ui/`

```
请根据以下业务模块信息生成完整的静态 HTML 管理页面原型集。

## 模块信息
- 模块名称：{{MODULE_NAME}}
- 实体名称：{{ENTITY_NAME}}
- 列表字段：{{LIST_FIELDS}}
- 搜索条件：{{QUERY_FIELDS}}
- 表单字段：{{FORM_FIELDS}}
- 详情字段：{{DETAIL_FIELDS}}
- 操作按钮：{{OPERATIONS}}
- 状态流转：{{WORKFLOW_STATES}}
- 是否有子表：{{HAS_CHILD_TABLES}}
- 接口清单：{{ENDPOINT_LIST}}

必须生成：
- `dashboard.html`：模块仪表板。
- `{entity}-list.html`：实体列表页。

按条件生成：
- `{entity}-detail.html`：有详情字段、子表或操作历史时生成。
- `{entity}-workflow.html`：有 status/state/auditStatus 等状态字段时生成。

交互要求：
- 使用 localStorage 初始化 mock 数据。
- 实现查询、重置、分页、每页条数切换。
- 实现新增、编辑、删除、批量删除。
- 实现查看详情、Tab 切换、返回列表。
- 有状态字段时实现状态流转、审批意见、操作历史时间线。
- 涉及接口的地方用内嵌 mock service 模拟请求、响应、loading、成功提示和错误提示。
- 表单字段按类型选择输入控件，并展示必填校验、长度校验、枚举校验。
- dashboard 展示统计卡片、近 7 天趋势图、快捷入口和最近记录。

技术约束：
- 单文件 HTML，CSS 和 JS 全部内嵌。
- 可使用 Tailwind CSS CDN 和 Bootstrap Icons CDN。
- 图表和步骤条使用内联 SVG，禁止 ECharts / Chart.js / D3。
- 交互使用原生 JS，禁止 jQuery / Vue / React。
- 页面可直接用浏览器打开并演示基础业务流程。

输出每个 HTML 文件的完整内容，文件之间用 `---FILE: {filename}---` 分隔。
```

---

## Prompt 12 — 系统数据库总览

> 执行顺序：系统级第 2 步 | 依赖：全部模块 `database.md` | 输出：`codebook/database-overview.md`

```
请根据所有模块的数据模型摘要生成系统级数据库总览。

## 模块数据模型摘要
{{MODULE_DATABASE_SUMMARIES}}

输出内容：
- 全库核心表清单：按模块分组。
- 跨模块 ER 总图：Mermaid erDiagram，只展示模块间关键关系。
- 数据边界：说明哪些表属于哪个模块，哪些关系跨模块。
- 公共字段与租户/审计/软删除策略总览。

不要重复模块内字段明细；字段明细由各模块 `database.md` 承担。
```

---

---

## IIDP baseline-spec Prompts（仅 IIDP 项目执行）

> 以下 Prompt 14–18 对应 `SKILL.md` Phase D-IIDP 阶段，仅在识别为 IIDP 项目时执行。
> 所有输出必须直接来自 Phase A/C 读取到的源码事实，**不得从已生成的 Markdown 反抽**。
> 每条规格项必须包含 `id`、`kind`、`sourceFile`、`sourceLine`、`confidence`、`status`、`unresolvedReason`（status 非 confirmed 时必填）。

---

## Prompt 14 — IIDP apps.json + menus.json

> 执行顺序：Phase D-IIDP 第 1 步 | 依赖：Phase A/C IIDP 识别完成 | 输出：`codebook/baseline-spec/apps.json`、`codebook/baseline-spec/menus.json`

```
你是一名 IIDP 基线规格提取工程师，请根据以下 IIDP 项目原始事实生成结构化基线规格 JSON。

## IIDP 应用清单原始数据
{{IIDP_APPS_SOURCE}}

## 菜单配置原始数据
{{IIDP_MENUS_SOURCE}}

## 前端应用识别结果
{{IIDP_FRONTEND_APPS}}

请输出两个 JSON 文件内容（用 ---FILE: {filename}--- 分隔）：

**apps.json** 格式（数组）：
每条记录包含：
- id: 规格 ID，格式 app.{appName}
- kind: "app"
- appName: 应用名（来自 apps.json 或 app.json 的 name 字段）
- backendModule: 后端模块路径（来自 apps/apps.json 的 module 字段，无则 null）
- frontendApp: 前端 app 名称（来自前端 config/apps.json 的 appId，配对候选）
- pairConfidence: 前后端配对置信度 high/medium/low（按 iidp-framework.md 配套规则评分）
- sourceFile: 来源文件路径
- sourceLine: 来源行号（无法定位则 null）
- confidence: high/medium/low
- status: confirmed/inferred/needs-confirmation
- unresolvedReason: status 非 confirmed 时必填

**menus.json** 格式（数组）：
每条记录包含：
- id: 规格 ID，格式 menu.{menuId 或 name}
- kind: "menu"
- name: 菜单名称
- url: 页面路由路径
- appId: 关联 app 名称
- modelId: 绑定的 model（来自菜单配置的 model 字段，无则 null）
- viewId: 绑定的 view（来自菜单配置的 view 字段，无则 null）
- permissions: 菜单下的按钮权限列表
- sourceFile / sourceLine / confidence / status / unresolvedReason

规则：
- 数据必须来自源码事实，禁止凭空推断不存在的 app 或菜单。
- 前后端 app 配对候选必须注明配对依据（app 名一致 / model 节点交叉 / service 节点交叉）。
- 无法确认来源行号的字段 sourceLine 填 null。
```

---

## Prompt 15 — IIDP models.json + views.json

> 执行顺序：Phase D-IIDP 第 2 步 | 依赖：apps.json 完成 | 输出：`codebook/baseline-spec/models.json`、`codebook/baseline-spec/views.json`

```
你是一名 IIDP 基线规格提取工程师，请根据以下源码事实生成 models.json 和 views.json。

## @Model / @Property / @Validate / @Selection / @Dict 注解源码
{{IIDP_MODEL_SOURCE}}

## 后端 views/*.json 原始内容
{{IIDP_VIEWS_SOURCE}}

## 前端 model-views/*.js 原始内容
{{IIDP_MODEL_VIEWS_SOURCE}}

请输出两个 JSON 文件内容（用 ---FILE: {filename}--- 分隔）：

**models.json** 格式（数组）：
每条记录包含：
- id: model.{modelName}
- kind: "model"
- modelName: 模型名（@Model 的 value 或类名）
- appId: 所属 app
- fields: 字段列表，每个字段包含 fieldName / javaType / iidpType / required / selections / validations
- relations: 关联模型列表（外键 / 多对多）
- sourceFile / sourceLine / confidence / status / unresolvedReason

**views.json** 格式（数组）：
每条记录包含：
- id: view.{viewId}
- kind: "view"
- viewId: 视图 ID（来自 views/*.json 的 id 字段或 model-views 的 viewId）
- source: "backend" / "frontend" / "both"（后端视图、前端扩展视图或两者均有）
- modelId: 关联 model 的规格 ID
- viewType: grid / form / tree / detail 等
- columns: 列定义列表（含 field / label / visible / sortable）
- buttons: 按钮定义（含 operationType / permission）
- frontendExtendFile: 若 source 包含 frontend，填前端 model-views 文件路径
- sourceFile / sourceLine / confidence / status / unresolvedReason

规则：
- @Property 中的 @Selection / @Dict 必须展开写入 fields[].selections。
- 后端 view 与前端 model-views 如能配对，合并为一条 views.json 记录，source 填 "both"。
- 无法配对的分别保留，source 分别填 "backend" 或 "frontend"。
```

---

## Prompt 16 — IIDP services.json + frontend-extends.json

> 执行顺序：Phase D-IIDP 第 3 步 | 依赖：models.json 完成 | 输出：`codebook/baseline-spec/services.json`、`codebook/baseline-spec/frontend-extends.json`

```
你是一名 IIDP 基线规格提取工程师，请根据以下源码事实生成 services.json 和 frontend-extends.json。

## @MethodService 注解源码（自定义服务）
{{IIDP_METHOD_SERVICE_SOURCE}}

## 标准元服务调用（httpMeta / vm.request）
{{IIDP_META_SERVICE_CALLS}}

## 前端扩展源码（selector / type / view / beforeOperate / hook / ds_config）
{{IIDP_FRONTEND_EXTEND_SOURCE}}

请输出两个 JSON 文件内容（用 ---FILE: {filename}--- 分隔）：

**services.json** 格式（数组）：
每条记录包含：
- id: service.{serviceId}
- kind: "service"
- serviceType: "custom"（@MethodService）/ "standard"（元服务）/ "controller"（传统接口）
- methodName: 方法名或服务标识
- appId: 所属 app
- inputParams: 入参列表（含 name / type / required）
- outputType: 返回类型描述
- calledBy: 调用此服务的前端扩展或菜单按钮（来自 ds_config / beforeOperate）
- sourceFile / sourceLine / confidence / status / unresolvedReason

**frontend-extends.json** 格式（数组）：
每条记录包含：
- id: frontendExtend.{pageId}.{extendType}.{index}
- kind: "frontendExtend"
- extendType: selector / type / view / beforeOperate / hook
- targetView: 目标视图 ID（viewId）
- targetNode: 扩展挂载节点（列名、按钮 operationType、hook 阶段）
- bindEvents: 触发事件列表（bind_on_select / bind_on_click 等）
- dsConfig: 数据源配置（app / model / service / args）
- componentPath: 自定义组件路径（如有）
- openView: 打开的视图 ID（如有）
- hookPhase: page / grid / form / tree（hook 类型时填写）
- sourceFile / sourceLine / confidence / status / unresolvedReason

规则：
- ds_config 中的 service 字段必须与 services.json 的 id 保持一致（可交叉引用）。
- 运行时才能确定的节点 ID 填 null 并写入 unresolved.json。
```

---

## Prompt 17 — IIDP capability-list.json + artifact-map.json + trace-map.json

> 执行顺序：Phase D-IIDP 第 4 步 | 依赖：services.json、views.json、frontend-extends.json 完成 | 输出：三个 JSON

```
你是一名 IIDP 基线规格提取工程师，请根据以下已生成的规格数据合成三个索引文件。

## 已生成的规格数据摘要
{{IIDP_CAPABILITY_SOURCE}}

（包含：menus.json 的按钮权限、views.json 的 buttons、services.json、frontend-extends.json 的 beforeOperate/hook）

请输出三个 JSON 文件内容（用 ---FILE: {filename}--- 分隔）：

**capability-list.json** 格式（数组）：
每条记录代表一项现有能力，包含：
- id: capability.{capabilityId}
- kind: "capability"
- name: 能力名称（中文业务语言）
- appId: 所属 app
- sourceRefs: 来源规格 ID 列表（menu/view button/service/frontendExtend 的 id）
- capabilityType: crud / query / workflow / import / export / custom
- permissions: 关联权限码列表

**artifact-map.json** 格式（数组）：
每条记录描述规格项到推荐修改文件的映射，包含：
- specId: 规格 ID（来自 models/views/services/frontend-extends）
- artifactType: model-java / view-json / model-views-js / service-java / hook-js 等
- targetPath: 推荐修改文件路径（基于现有文件路径推断）
- changeType: add / modify / delete
- notes: 修改说明

**trace-map.json** 格式（数组）：
每条记录提供规格项到源码的追踪，包含：
- specId: 规格 ID
- sourceFile: 源码文件路径
- sourceLine: 源码行号（null 则说明无法定位）
- symbolName: 对应的类名/方法名/变量名
- confidence: high/medium/low

规则：
- capability-list.json 必须合并菜单按钮权限、视图按钮权限、@MethodService、标准元服务和前端 beforeOperate 四类来源。
- trace-map.json 中每条 specId 必须在 artifact-map.json 中有对应条目。
- 无法追踪源码的 specId 在 trace-map.json 中 sourceFile 填 null，并同步写入 unresolved.json。
```

---

## Prompt 18 — IIDP unresolved.json

> 执行顺序：Phase D-IIDP 第 5 步，最后执行 | 依赖：全部 baseline-spec JSON 完成 | 输出：`codebook/baseline-spec/unresolved.json`

```
你是一名 IIDP 基线规格提取工程师，请汇总所有 baseline-spec 文件中 status 非 confirmed 的条目，生成 unresolved.json。

## 待汇总的规格文件
{{IIDP_ALL_BASELINE_SPECS}}

（将 apps.json / menus.json / models.json / views.json / services.json / frontend-extends.json / capability-list.json / trace-map.json 中 status 为 inferred / needs-confirmation / placeholder 的条目全部汇总）

输出 unresolved.json（数组），每条记录包含：
- specId: 原规格 ID
- specFile: 来源规格文件名（如 models.json）
- status: inferred / needs-confirmation / placeholder
- unresolvedReason: 详细原因（来自原规格的 unresolvedReason 字段）
- suggestedAction: 建议的确认方式（如"查阅运行时日志"、"联系业务方确认角色定义"）
- priority: P0（影响核心流程）/ P1（影响完整性）/ P2（锦上添花）

规则：
- 所有 status 非 confirmed 的条目必须汇总，不得遗漏。
- suggestedAction 必须具体可操作，不得只写"待确认"。
- 按 priority 升序排列（P0 在前）。
- 最终输出一个数字汇总行：P0 条数 / P1 条数 / P2 条数 / 合计。
```

---

## Prompt 13 — 系统 Overview

> 执行顺序：系统级第 3 步，最后生成 | 输出：`codebook/overview.md`

```
请生成系统级 overview 文档。

## 输入
- 项目名称：{{PROJECT_NAME}}
- 框架：{{FRAMEWORK}}
- 模块列表：{{MODULES}}
- codegraph 统计：{{CODEGRAPH_STATUS_OUTPUT}}
- 模块摘要：{{MODULE_SUMMARIES}}
- 模块调用图摘要：{{MODULE_CALL_GRAPHS}}
- 模块依赖图摘要：{{MODULE_DEPENDENCY_GRAPHS}}
- 模块前置条件摘要：{{MODULE_PRECONDITIONS}}
- 生成统计：{{GENERATION_STATS}}

必须包含：
- 基本信息：项目、框架、架构模式、代码规模、生成时间。
- 系统功能模块一览：模块名、系统级功能、核心入口、核心实体、文档链接。
- 系统文档导航：`hla.md`、`database-overview.md`。
- 模块文档导航：每个模块链接到 `overview.md`、`hla.md`、`srs.md`、`prd.md`、`user-stories.md`、`api.md`、`database.md`、`error-codes.md`、`flowcharts/`、`ui/`。
- 各模块调用图摘要：可嵌入 Mermaid 或链接到模块 HLA。
- 各模块依赖图摘要：说明内部依赖、外部依赖、跨模块依赖。
- 各模块前置条件摘要：鉴权、配置、数据、外部服务、调度或事件触发。
- codegraph 分析统计。
- 完成标记：生成完成时间、模块数、数据表数、API 接口数。

顶层 overview 只写系统级信息和导航，不写模块详细功能设计。
```
