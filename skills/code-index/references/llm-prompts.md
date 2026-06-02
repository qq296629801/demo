# LLM Prompt 模板 — 代码转规格书

每个模板对应一种 Codebook 文档类型。使用时将 `{{...}}` 占位符替换为 codegraph 采集结果。

`SKILL.md` 是输出目录和断点恢复契约的唯一来源；本文件只描述各文档的生成提示与内容要求。所有详细功能设计都写入 `codebook/modules/{module}/`，顶层 `codebook/` 只保留系统级文档。

---

## 通用变量说明

```
{{PROJECT_NAME}}        项目名称
{{FRAMEWORK}}           框架名（RuoYi / JEECG Boot / yudao-cloud / Spring Boot / NestJS / Django 等）
{{MODULE_NAME}}         当前分析的模块名称
{{MODULES}}             模块列表（来自 Phase B）
{{CONTROLLERS}}         Controller 类列表（含方法签名）
{{CONTROLLER_SOURCE}}   Controller 完整源码（逐文件 Read）
{{ENTITIES}}            Entity 类列表（含字段）
{{ENTITY_SOURCE}}       Entity/DO 完整源码
{{VO_SOURCE}}           ReqVO/RespVO/DTO 完整源码
{{SERVICES}}            Service 接口和实现类列表
{{SERVICE_SOURCE}}      ServiceImpl 关键源码
{{CALL_TRACE}}          当前功能的完整调用链
{{CALLERS}}             某方法的调用者列表
{{ENDPOINT_LIST}}       模块接口功能清单
{{API_ANNOTATIONS}}     API 注解信息（@ApiOperation / @Operation）
{{ERROR_CODES_SOURCE}}  错误码常量文件源码
{{FRONTEND_API_SOURCE}} 前端 API 调用源码（无前端则填"无"）
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
- 背景与目标：模块解决的业务问题。
- 用户角色矩阵：角色、目标、可执行操作。
- 功能详述：按功能域描述业务能力、前置条件、输入输出和成功标准。
- 业务流程描述：覆盖主流程、异常流程、状态变化。
- 范围边界：说明本模块负责什么、不负责什么。

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
- 验收标准：Given / When / Then
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
- Request Body 使用字段校验矩阵：字段名、类型、必填、规则、错误提示。
- 创建接口和更新接口的必填字段可能不同，必须分别标注。
- 错误码必须来自源码，不得推测。
- 权限码必须来自权限注解，不得推测。
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

> 执行顺序：模块级第 10 步 | 依赖：模块 SRS、API、调用链 | 输出：`codebook/modules/{module}/flowcharts/`

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

> 执行顺序：模块级第 9 步 | 依赖：模块 API 字段、UI_CONTEXT | 输出：`codebook/modules/{module}/ui/`

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
