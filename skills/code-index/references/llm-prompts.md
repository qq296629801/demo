# LLM Prompt 模板 — 代码转规格书

每个模板对应一种 Spec 文档类型。  
使用时将 `{{...}}` 占位符替换为 codegraph 查询结果。

---

## 通用变量说明

```
{{PROJECT_NAME}}        项目名称
{{FRAMEWORK}}           框架名（RuoYi / JEECG Boot / yudao-cloud / Spring Boot / NestJS / Django 等）
{{MODULE_NAME}}         当前分析的模块名称（大型项目按模块分析时使用）
{{MODULES}}             模块列表（从 codegraph_search 提取）
{{CONTROLLERS}}         Controller 类列表（含方法签名）
{{CONTROLLER_SOURCE}}   Controller 完整源码（codegraph_search → Read 获取）
{{ENTITIES}}            Entity 类列表（含字段）
{{ENTITY_SOURCE}}       Entity/DO 完整源码（codegraph_search → Read 获取）
{{VO_SOURCE}}           ReqVO/RespVO/DTO 完整源码（codegraph_search → Read 获取）
{{SERVICES}}            Service 接口列表
{{CALL_TRACE}}          某功能的完整调用链（codegraph_trace 输出）
{{CALLERS}}             某方法的调用者列表（codegraph_callers 输出）
{{API_ANNOTATIONS}}     API 注解信息（@ApiOperation / @Operation）
{{ERROR_CODES_SOURCE}}  错误码常量文件源码（Read ErrorCodeConstants.java 获取）
```

---

## Prompt 1 — 项目概览 + HLA 高阶架构

> 执行顺序：第 1 步 | 依赖：无（需先完成 Phase A/B 数据采集）| 输出：`spec/01-hla.md`

```
你是一名资深系统架构师，请根据以下代码分析结果生成项目高阶架构文档（HLA）。

## 项目基本信息
- 项目名称：{{PROJECT_NAME}}
- 技术框架：{{FRAMEWORK}}
- 主要模块：{{MODULES}}

## codegraph 提取的符号统计
{{CODEGRAPH_STATUS_OUTPUT}}
（格式：文件数 X，节点数 Y，边数 Z，语言分布）

## 识别到的顶层包结构
{{FILE_TREE}}

## 识别到的 Controller 类（API 入口）
{{CONTROLLERS}}

## 识别到的 Entity 类（数据模型）
{{ENTITIES}}

## 核心业务场景（从 @Transactional 方法名提取，2-4 个）
{{CORE_SCENARIOS}}
（格式：场景1, 场景2, ... ← 如：用户登录, 订单创建, 审批流转）

## Service Impl 关键源码（每个最多前 200 行）
{{SERVICE_SOURCE}}
（来自 codegraph_search("ServiceImpl", kind="class") → Read 获取，包含 @Transactional 方法）

请按以下结构输出完整 HLA 文档，使用中文，专业规范，不要猜测未在代码中体现的功能：

### 必须包含以下章节（按顺序）：

**§1 系统定位** — 1-2 段描述系统是什么、服务于哪些用户、解决什么核心问题

**§2 系统架构图** — Mermaid graph TB，含客户端层/接入层/应用层/数据层

**§3 技术选型** — 表格：层次/技术/版本/说明

**§4 模块职责** — 表格：模块/包路径/职责/核心类

**§5 安全架构** — 从 Security 配置和权限注解推断

**§6 部署架构** — 从配置文件和 Docker 文件推断，若无则注明"未识别到部署配置"

**§7 关键业务流程图** — 针对 CORE_SCENARIOS 中每个场景，各生成一个 Mermaid flowchart TD 图，直接嵌入本文档。
格式要求：
- 节点使用业务语言，不使用类名/方法名
- 判断节点用菱形 {}
- 异常/错误路径用 style xxx fill:#f96 标红
- 每个场景单独一个 ```mermaid 代码块，前面加三级标题"### {场景名}"
- 例：
  ### 用户登录流程
  ```mermaid
  flowchart TD
    A[用户输入账号密码] --> B{格式校验}
    B -- 失败 --> C[返回校验错误]
    style C fill:#f96
    B -- 通过 --> D[查询用户记录]
    D --> E{账号是否存在}
    E -- 否 --> F[返回账号不存在]
    style F fill:#f96
    E -- 是 --> G{密码是否正确}
    G -- 否 --> H[返回密码错误]
    style H fill:#f96
    G -- 是 --> I[生成 Token]
    I --> J[返回登录成功]
  ```

**§8 核心服务方法设计** — 针对 SERVICE_SOURCE 中每个 ServiceImpl，提取所有带 @Transactional 注解或方法体超过 20 行的关键业务方法，按以下格式输出：

### {ServiceImplName}

#### {methodName}({params}) → {returnType}
**事务**：@Transactional（传播级别/rollbackFor 如有）/ 无事务
**业务步骤**：
1. [步骤1，伪代码级别，描述做什么而非怎么做]
2. [步骤2]
3. ...
**异常**：[抛出的业务异常类型和条件]
**副作用**：[写入哪些表/发送哪些事件/调用哪些外部服务]

每个 ServiceImpl 只输出关键方法（@Transactional 优先，其次是复杂查询方法），不超过 5 个方法/Service，不输出简单 getter/setter 方法。

**§9 数据模型概览** — 基于 ENTITIES 信息，输出精简 ER 图（只含主要实体和主要关联关系，不含字段详情），并说明关键聚合边界：

```mermaid
erDiagram
  USER ||--o{ USER_ROLE : "拥有"
  ROLE ||--o{ ROLE_MENU : "关联"
  ...（只列主要实体间的 1:N 或 N:N 关系）
```

> 详细字段定义和 DDL 见 [07-database.md](./07-database.md)
```

---

## Prompt 2 — SRS 软件需求规格

> 执行顺序：第 2 步 | 依赖：Prompt 1（模块边界 + 系统定位）| 输出：`spec/02-srs.md` 或 `spec/modules/{module}/02-srs.md`

```
你是一名需求工程师，请根据以下代码分析结果，逆向生成软件需求规格说明书（SRS）。

## 系统概览
- 系统名称：{{PROJECT_NAME}}
- 模块名称：{{MODULE_NAME}}（大型项目请按模块单独生成）
- 框架：{{FRAMEWORK}}

## 功能入口（API Controller 完整源码）
{{CONTROLLER_SOURCE}}
（来自 codegraph_search → Read 读取源文件，包含所有端点和注解）

## 操作日志覆盖的功能点
{{LOG_ANNOTATIONS}}
（格式：@Log(title="模块名", businessType=BusinessType.INSERT/UPDATE/DELETE)）

## 权限定义
{{PERMISSION_ANNOTATIONS}}
（格式：@PreAuthorize("@ss.hasPermi('system:user:edit')")）

## 触发点（codegraph_callers 结果）
{{CALLERS}}
（格式：调用方类名.方法名 + 触发类型：Controller/Scheduler/EventListener/MQ）

## 接口功能清单（ENDPOINT_LIST，Phase C C1 建立）
{{ENDPOINT_LIST}}
（格式：HTTP方法 + 路径 + 功能名称 + 权限码，每行一条；FR 条目数必须与此清单一一对应，不得遗漏）

输出 Markdown，按业务模块分章节，每个功能需求一个独立章节。
**每个 ENDPOINT_LIST 条目必须对应一个 FR-XXX 节，不得合并或省略。**
不要编造代码中不存在的需求，遇到不确定的地方用 [需确认] 标注。
```

---

## Prompt 3 — PRD 产品需求文档

> 执行顺序：第 3 步 | 依赖：Prompt 2（功能需求列表）| 输出：`spec/03-prd.md`（大型项目顶层生成，不按模块分拆）

```
你是一名产品经理，请将以下技术代码分析结果转化为面向产品和业务的 PRD 文档。

## 系统基本信息
- 产品名称：{{PROJECT_NAME}}
- 目标用户：[根据权限体系推断，如：系统管理员、普通用户、审批人]

## 功能模块（从 @Api(tags) 或包结构提取）
{{MODULES_LIST}}

## 主要业务流程（codegraph_trace 输出）
{{CALL_TRACE_RESULTS}}
（一到三个核心业务流程的调用链）

## 数据实体关系
{{ENTITY_RELATIONSHIPS}}

> 输出格式：按产品文档惯例，4 节：背景与目标 / 用户角色矩阵 / 模块详述 / 业务流程描述
输出面向非技术读者，避免使用代码术语，用业务语言描述。
```

---

## Prompt 4 — 用户故事

> 执行顺序：第 4 步 | 依赖：Prompt 2、3（需求边界）| 输出：`spec/04-user-stories.md`

```
请根据以下 API 接口列表和操作日志注解，生成标准格式的用户故事（User Stories）。

## 接口列表
{{API_LIST}}
（格式：HTTP Method + Path + @ApiOperation/summary 描述）

## 操作日志
{{AUDIT_LOGS}}

## 角色定义
{{ROLES}}

按业务模块分组；P0=核心流程，P1=辅助功能，P2=配置/管理功能；使用中文，语言简洁清晰。
```

---

## Prompt 5 — API 文档（RESTful）

> 执行顺序：第 5 步 | 依赖：Prompt 2（字段约束来源）| 输出：`spec/05-api.md` 或 `spec/modules/{module}/05-api.md`

```
你是一名 API 文档工程师，请根据以下代码分析结果生成 RESTful API 规格文档。

## 项目信息
- API Base URL：/api（或从配置推断）
- 认证方式：{{AUTH_METHOD}}（JWT Bearer / Sa-Token / Session）
- 模块名称：{{MODULE_NAME}}

## Controller 完整源码
{{CONTROLLER_SOURCE}}
（来自 Read(Controller 文件)，包含所有端点、@PreAuthorize/@SaCheckPermission、@Operation 注解）

## ReqVO / DTO 完整源码（请求体字段定义）
{{VO_SOURCE}}
（来自 Read(ReqVO/DTO 文件)，包含所有字段的 @NotBlank/@Pattern/@Size/@Schema 注解）

## 错误码常量
{{ERROR_CODES_SOURCE}}
（来自 Read(ErrorCodeConstants.java)，格式：常量名 = 错误码编号; // 描述）

## 接口功能清单（ENDPOINT_LIST，Phase C C1 建立）
{{ENDPOINT_LIST}}
（格式：HTTP方法 + 路径 + 功能名称 + 权限码，每行一条；接口章节数必须与此清单一一对应，不得遗漏）

## 前端 API 调用源码（全栈项目填写，纯后端项目填"无"）
{{FRONTEND_API_SOURCE}}
（来自 Read(src/api/*.ts)，TypeScript 函数签名 + 请求/响应类型；无前端则填"无"）

输出 Markdown，按模块分组，每个接口用 `---` 分隔。
**强制要求**：
1. Request Body 必须用字段校验矩阵表格（字段名/类型/必填/规则/错误提示）
2. 创建接口和更新接口的必填字段可能不同，必须分别标注
3. 错误码必须来自 ErrorCodeConstants 源码，不得推测
4. 权限码必须来自 @PreAuthorize/@SaCheckPermission 注解，不得推测
5. 如 FRONTEND_API_SOURCE 非空，必须为每个接口追加"前端调用层对照"表格；TS 类型与后端 Java VO 字段如有差异，用 `[⚠️ 类型不一致]` 标注
对于不确定的字段类型，标注 [需确认]。
```

---

## Prompt 6 — 流程图生成

> 执行顺序：第 6 步 | 依赖：Prompt 2（业务流程）、Prompt 5（API 入口）、codegraph_trace 输出 | 输出：`spec/06-flowcharts/` 目录

```
请根据以下调用链分析结果，生成业务流程的 Mermaid 流程图。

## 调用链数据（codegraph_trace 输出）
{{CALL_TRACE_JSON}}

## 业务场景名称
{{SCENARIO_NAME}}（例如：用户登录流程、订单创建流程、审批流程）

请生成两种图：
1. **用户操作视角流程图**（flowchart TD，面向产品/业务，节点用业务语言，判断节点用菱形 {}，异常路径标红 style xxx fill:#f96）
2. **技术调用链时序图**（sequenceDiagram，面向开发，参与者不超过 6 个）
```

---

## Prompt 7 — 数据库结构文档

> 执行顺序：第 7 步 | 依赖：无（独立于 API 文档，直接读取 Entity 源码）| 输出：`spec/07-database.md` 或 `spec/modules/{module}/07-database.md`

```
请根据以下 Entity/DO 类完整源码，生成数据库结构文档。

## Entity/DO 类完整源码
{{ENTITY_SOURCE}}
（来自 codegraph_search("XxxDO/XxxEntity", kind="class") → Read(file) 获取实际源码）

请按 4 节结构生成：① 数据库表清单（表名/中文名/模块/说明）② 每张表详细结构（字段/类型/长度/可空/索引/DDL）③ ER 关系图（Mermaid erDiagram）④ 数据字典（枚举/常量）

规则：
- 字段类型/长度必须来自实际 DO 源码（如 `@Schema(description="...", example="...")`、`varchar(100)` 注释），不得推测
- DDL 必须包含所有索引：UNIQUE 索引、普通索引、联合索引（从 @UniqueConstraint 或 @TableIndex 或类注释提取）
- JSON 字段必须注明序列化方式（如 `JacksonTypeHandler`、`FastjsonTypeHandler`）
- 继承字段（来自 BaseDO/TenantBaseDO/BaseEntity）必须单独标注来源父类，避免遗漏
- 标注公共字段（creator/create_time/updater/update_time/deleted/tenant_id）
```

---

## Prompt 8 — UI/UX 静态页面

> 执行顺序：第 8 步 | 依赖：Prompt 5（API 字段 → 表单/列表字段）| 输出：`spec/09-ui/` 目录下多个 HTML 文件

```
请根据以下业务模块信息，生成完整的静态 HTML 管理页面原型集。

## 模块信息
- 模块名称：{{MODULE_NAME}}（例如：用户管理）
- 列表字段：{{LIST_FIELDS}}（来自 RespVO 或 Entity 字段）
- 搜索条件：{{QUERY_FIELDS}}（来自 *Query 或 @RequestParam）
- 表单字段：{{FORM_FIELDS}}（来自 CreateReqVO 或表单 Entity）
- 详情字段：{{DETAIL_FIELDS}}（来自 RespVO 全量字段，包含只读字段和关联字段）
- 操作按钮：{{OPERATIONS}}（来自 @PreAuthorize 权限点分析）
- 状态流转：{{WORKFLOW_STATES}}（枚举值列表，如 PENDING/APPROVED/REJECTED；无状态字段则填"无"）
- 是否有子表：{{HAS_CHILD_TABLES}}（true = 有 @OneToMany 或外键关联子表；false = 无）

请生成以下页面文件（根据条件决定是否生成）：

---

### 文件 1：{entity}-list.html（列表页）— 必须生成

包含：
- 顶部搜索栏（根据 QUERY_FIELDS 生成：文本框 / 下拉 / 日期选择器）
- 操作按钮区（新增 / 批量删除 / 导入 / 导出，按 OPERATIONS 决定显隐）
- 数据表格（列来自 LIST_FIELDS；最后一列含：查看详情 / 编辑 / 删除 操作按钮）
- 分页组件（页码 + 每页条数）
- 新增/编辑弹窗（包含 FORM_FIELDS 表单，支持必填验证提示）

---

### 文件 2：{entity}-detail.html（详情页）— 当 HAS_CHILD_TABLES=true 或 WORKFLOW_STATES≠"无" 时生成

包含：
- 顶部面包屑导航 + 返回按钮
- 顶部状态 Badge（若有 WORKFLOW_STATES）+ 操作按钮栏（审批/提交/撤回等，按状态决定显隐）
- 标签页（Tab）结构：
  - Tab 1"基本信息"：卡片布局，左右双栏展示 DETAIL_FIELDS（只读）
  - Tab 2"关联记录"（HAS_CHILD_TABLES=true 时显示）：子表数据表格，支持展开行
  - Tab 3"操作历史"（WORKFLOW_STATES≠"无" 时显示）：时间线展示操作记录
- Tab 切换用纯 JS 实现（不依赖 jQuery/Vue）

---

### 文件 3：{entity}-workflow.html（状态流转页）— 仅当 WORKFLOW_STATES≠"无" 时生成

包含：
- 顶部 SVG 步骤条（水平进度条，显示全部状态节点，当前节点高亮蓝色，已完成绿色，未到达灰色）
  - 步骤条使用内联 SVG 实现，不依赖第三方图表库
- 中部当前节点详情卡片（显示当前状态说明、责任人、操作截止时间）
- 操作区（审批意见输入框 + 通过/拒绝按钮，按当前状态决定是否显示）
- 底部操作历史时间线（竖向 timeline，每条记录含：时间 / 操作人 / 操作类型 / 备注）

---

### 文件 4：dashboard.html（模块仪表板）— 每个模块固定生成一个

包含：
- 顶部 4 宫格统计卡片（总数 / 今日新增 / 待处理数 / 其他关键指标，带趋势箭头）
- 中部折线图（近 7 天数据趋势，用内联 SVG 绘制，不依赖 ECharts/Chart.js 等 CDN 库）
  - SVG 折线图要求：有 X/Y 轴、网格线、数据点圆圈、悬停 tooltip（纯 JS 实现）
- 右侧快捷操作入口（按 OPERATIONS 生成 2-4 个入口卡片）
- 底部最近记录表格（显示最新 5 条记录，来自 LIST_FIELDS 的前 4 列）

---

## 通用技术规范（所有页面统一）

- **CSS 框架**：Tailwind CSS CDN（`https://cdn.tailwindcss.com`）
- **图标**：Bootstrap Icons CDN（`https://cdn.jsdelivr.net/npm/bootstrap-icons/font/bootstrap-icons.css`）
- **配色**：主色 #1d4ed8，成功绿 #16a34a，危险红 #dc2626，警告黄 #d97706，背景灰 #f8fafc
- **数据**：完全静态，localStorage mock 数据（页面加载时初始化，操作后更新 localStorage）
- **图表**：折线图、步骤条进度条均使用内联 SVG 实现，**禁止引入 ECharts / Chart.js / D3 等第三方图表 CDN**
- **交互**：Tab 切换、折叠面板、弹窗、tooltip 均用原生 JS 实现（**禁止引入 jQuery / Vue / React**）
- **布局**：响应式，支持 1280px+ 宽屏，侧边导航固定宽 240px
- **交付**：单文件，CSS 和 JS 全部内嵌，无外部依赖（除 Tailwind CSS 和 Bootstrap Icons CDN）

输出每个 HTML 文件的完整内容，文件之间用 `---FILE: {filename}---` 分隔。
```

---

## Prompt 9 — 现有框架识别总结

> 执行顺序：第 0 步（初始化阶段）| 依赖：无（Phase A 数据采集后立即执行）| 输出：框架识别结论，供所有后续 Prompt 的 `{{FRAMEWORK}}` 变量使用

```
请分析以下 codegraph 输出，识别该项目使用的框架并给出摘要报告。

## codegraph_search 结果摘要
{{SEARCH_RESULTS_SUMMARY}}

## 文件结构
{{FILE_TREE_TOP_LEVEL}}

## 请判断并输出：

1. **主框架**：RuoYi / JEECG Boot / yudao-cloud / maku-boot / 通用 Spring Boot / NestJS / Django / FastAPI / Gin / React / Vue / Angular 等
2. **次框架/组件**：Spring Boot 版本、MyBatis-Plus / JPA、Sa-Token / JWT、Flowable 等
3. **架构模式**：单体 / 微服务 / 模块化单体 / 前端 SPA
4. **前端分离情况**：前后端分离 / MVC 模板渲染 / 纯前端项目
5. **特殊组件**：代码生成器、低代码模块、工作流引擎
6. **魔改程度**：原版 / 轻度定制 / 深度定制（依据特征类名/包名变化判断）

输出格式：简洁的 Markdown 列表，不超过 1 页。
```

---

## Prompt 10 — 错误码文档

> 执行顺序：第 9 步 | 依赖：无（直接读取 ErrorCodeConstants 源码）| 输出：`spec/08-error-codes.md` 或 `spec/modules/{module}/08-error-codes.md`

```
请根据以下错误码常量文件源码，生成模块错误码文档。

## 错误码常量文件源码
{{ERROR_CODES_SOURCE}}
（来自 Read(ErrorCodeConstants.java 或 *ErrorCode.java) 实际源码）

请生成错误码表，格式：`| 错误码编号 | 常量名 | 中文描述 | 触发场景 |`

规则：
- 错误码编号和常量名必须与源码完全一致，不得推断
- 触发场景从常量名含义推断，标注 [需确认] 如不确定
- 按错误码编号升序排列
- 如一个文件包含多个模块的错误码，按模块前缀分组
```

---

## Prompt 11 — 服务依赖图

> 执行顺序：第 10 步 | 依赖：Prompt 1（已识别模块列表）| 输出：追加至 `spec/01-hla.md` 服务依赖图章节

```
请根据以下 Service Impl 源码，生成服务依赖关系图。

## Service Impl 源码（含 @Autowired/@Resource 注入字段）
{{SERVICE_IMPL_SOURCE}}
（来自 Read(XxxServiceImpl.java) 实际源码）

请生成：
1. **服务依赖关系图**（Mermaid graph LR，节点为 ServiceImpl/Client/基础设施类名）
2. **外部依赖说明表**（`| 依赖服务 | 类型 | 用途 |`，类型区分：内部 Service / @FeignClient / 基础设施）

规则：
- 只提取 @Autowired/@Resource/@Inject 注入的字段
- 区分：内部 Service / @FeignClient 远程调用 / 基础设施（Redis/MQ/OSS）
- 忽略工具类注入（StringUtils 等）
```

---

## Prompt 12 — 前端规格书

> 执行顺序：第 11 步（仅前端项目）| 依赖：frontend-frameworks.md 识别结论 | 输出：`spec/` 主目录（10-13 号文件）

```
你是一名前端架构师，请根据以下前端代码分析结果，生成前端规格文档。

## 项目信息
- 项目名称：{{PROJECT_NAME}}
- 前端框架：{{FRONTEND_FRAMEWORK}}（React / Vue 3 / Angular）
- 状态管理：{{STATE_MANAGEMENT}}（Redux / Pinia / Vuex / NgRx）
- 构建工具：{{BUILD_TOOL}}（Vite / Webpack / Next.js / Nuxt.js）

## 路由配置源码
{{ROUTER_SOURCE}}
（来自 Read(src/router/index.ts) 或 pages/ 目录结构）

## Store 文件源码
{{STORE_SOURCE}}
（来自 codegraph_search("defineStore") → Read store 文件）

## API 服务文件源码
{{API_SOURCE}}
（来自 Read(src/api/*.ts)）

请生成以下内容：
1. **spec/10-pages.md** — 页面路由表：`| 路径 | 组件文件 | 布局 | 权限守卫 | 功能描述 |`
2. **spec/11-components.md** — 核心复用组件树：`组件名 | 文件路径 | Props | Emits | 说明`
3. **spec/12-state.md** — Store 模块：State 字段表（字段/类型/初始值/说明）+ Actions 表（方法/参数/说明）

> **注意**：API 调用层对照表已在 Prompt 5 的 `spec/05-api.md` 中按接口逐条生成（"前端调用层对照"章节），此处不重复输出。

规则：
- 路由表必须包含权限守卫信息（从 meta.auth / canActivate 提取）
- Store actions 必须列出调用的 API 函数名
- 如存在 i18n，生成 spec/13-i18n.md（key + 中文值 + 使用组件）
```
