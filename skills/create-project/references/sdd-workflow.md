# IIDP SDD 工作流

## 总体工作流

```text
[第 0 步] 读取项目宪法和前后端 Skill
    ↓
[需求进入] 识别 IIDP 能力域
    ↓
[Specify] 写功能规格：模型、页面、服务、权限、数据、验收
    ↓
[Clarify / Step 1.2] ★ 规格歧义扫描，生成结构化澄清问题（≤5 条），答案写回 requirements.md（必经步骤）
    ↓
[契约先行 / Step 1.3a] ★ 定义 integration-map.md + sdd-contracts.md 契约（必须先于规格书，强制步骤）
    ↓
[Critique / Step 1.4] ★ 产品战略 + 工程风险双视角批判，暂停等用户确认（可选）
    ↓
[Backend Spec] 按 sdd-backend.md 模板生成 backend-spec.md（必写）
    ↓
[Frontend Spec] 按 sdd-frontend.md 模板生成 frontend-spec.md（必写，§9 记录实现分支）
    ↓
[Interaction Spec] 含复杂状态/响应式/可访问性？→ 按 sdd-frontend-interaction.md 模板生成 interaction-spec.md（可选）
    ↓
[Plan] 生成实现计划：后端优先，前端按实现分支决策 （必写）
    ↓
[Plan Review Gate] ★ 展示计划摘要，暂停等用户确认后才生成任务（可选）
    ↓
[Tasks] 拆解可执行任务：文件级、服务级、视图级、验证级（必写）
    ↓
[Blueprint] ★ 生成代码蓝图（伪代码+文件清单），暂停等用户确认（可选）
    ↓
[Implement] 按任务落地：一次一个任务，子 skill 读取 spec 文件（必写）
    ↓
[Validate] 按 IIDP 清单验证；完成后提示生成 PR Bridge 描述（必写）
```

## 规格分层

| 层级     | 说明                                                  | 典型文件                                                              |
| -------- | ----------------------------------------------------- | --------------------------------------------------------------------- |
| 项目宪法 | 稳定原则、工程约束、路线图、UI 约束                   | `specs/mission.md`、`specs/iidp-stack.md`、`specs/ui-constitution.md` |
| 功能规格 | 单个业务能力的需求与契约                              | `specs/features/<feature>/requirements.md`                            |
| 后端规格 | IIDP 后端技术落地细节（模型、服务、视图、菜单、权限） | `specs/features/<feature>/backend-spec.md`                            |
| 前端规格 | IIDP 前端技术落地细节（§9 实现分支决定是否写代码）         | `specs/features/<feature>/frontend-spec.md`                           |
| 实现计划 | 文件清单、实现顺序、风险                              | `specs/features/<feature>/plan.md`                                    |
| 任务清单 | 可逐项执行的工作项                                    | `specs/features/<feature>/tasks.md`                                   |
| 验收清单 | 静态、构建、运行、页面检查                            | `specs/features/<feature>/validation.md`                              |
| 维护记录 | 阶段复盘、决策、变更记录                              | `specs/roadmap.md`、`specs/decisions.md`、`CHANGELOG.md`              |

## 后端能力识别

> **识别后端能力域前必须先读取 `skills/backend/references/core/capability-map.md`**，按其能力域总览表匹配需求，再按匹配结果加载对应专题文件。不得只使用下方内联速查（仅覆盖 7 个常见域，缺少 openView/弹窗/视图联动/聚合查询/输入组件/Redis/XXL-Job 等扩展域）。

常见能力域速查（完整 15 个域及专题文件见 `capability-map.md`）：

- 工程、应用、部署：POM、`app.json`、`apps/apps.json`、Docker Compose。
- 模型、字段、校验、ER：`@Model`、`@Property`、`@Validate`、ORM 注解。
- 标准页面：`views/*.json`、`grid/search/form/tree`、`tbar/buttons/tabs`。
- 菜单和种子数据：`data/menus.json`、`data/*.json`、`file/*.json`。
- 服务和 API：`@MethodService`、内置 CRUD、Filter、JSON-RPC、原生 SQL。
- 权限、多租户、多语言：`auth`、菜单权限、服务权限、语言包。
- 文件、Excel、打印、任务：MinIO、Excel 导入导出、打印、XXL-Job。

## 前端实现分支

| 分支              | 适用场景                                           | 处理方式                                                  |
| ----------------- | -------------------------------------------------- | --------------------------------------------------------- |
| 标准模板/在线视图 | 传统后台：搜索、表格、表单、树、子表               | 前端不新增代码，主要由后端视图和菜单配置完成              |
| hook              | 查询、保存、删除、权限控制、错误提示等标准流程增强 | 在扩展应用中写 hook，必要时调用 `vm.super`                |
| 扩展视图          | 插入按钮、调整节点、补充弹窗、合并属性             | 写 `before/after/append/merge/replace/delete/custom` 扩展 |
| 自定义 Vue2 组件  | IIDP 节点难以表达的复杂交互                        | 写 Vue2 兼容组件并按项目入口注册                          |

## 前端交互与节点规格

旧版通用前端 Spec 里的“组件层级、Props、事件、状态”在当前项目中要改写为 IIDP 视图节点、数据源和后端契约：

| 通用前端概念        | IIDP 当前项目写法                                                      |
| ------------------- | ---------------------------------------------------------------------- |
| Page/Component 层级 | `app > page > container > search/grid/form/dialog/table/button` 节点树 |
| Props               | 节点属性、`dataSource`、`ds_config.options`、后端视图字段配置          |
| State               | `$ds`、节点 `dataSource`、标准页 `vm.biz.data`、后端模型状态字段       |
| Event handler       | `bind_on_事件名`、hook、commands、按钮 `service/actionAfter`           |
| Selector            | `selector`、`vm.$select`、`vm.page.getNode(id)`，节点 id 必须有来源    |
| API call            | `ds_config` 的 `meta/api/static` 数据源或 JSON-RPC 服务契约            |
| Responsive layout   | 标准模板容器、`container`、`row`、`span`、弹窗/抽屉滚动策略            |

节点规格最少包含：

- `type`：IIDP 组件类型，必须来自平台组件或已注册自定义组件。
- `id`：稳定业务 id；无法确认时写“待确认”，不得按文案猜测。
- `selector`：扩展视图命中的目标节点，说明来源和验证方式。
- `ds_config`：数据源类型、`name`、`autoRequest`、`options.params`、`reqPrep`、`reqAfter`。
- `bind_`/`bind_two_`：属性和数据源的绑定关系。
- `bind_on_`：真实组件事件名和回调职责。
- `commands`/`$cmd`：公共命令名称、入参和调用位置。
- `hook`：标准页 hook 路径、是否调用 `vm.super`、读写的 `vm.biz` 字段。

## 功能目录

```text
specs/features/
└── phase1-[feature-name]/
    ├── requirements.md         # 功能需求与契约
    ├── sdd-contracts.md        # API 级契约（Step 1.3a 生成，先于规格书）
    ├── backend-spec.md         # 后端技术落地规格（命名、模型、服务、视图、菜单、权限）
    ├── frontend-spec.md        # 前端技术落地规格（必写，含实现分支判断）
    ├── interaction-spec.md     # 前端交互规格（含复杂状态/响应式/可访问性时生成）
    ├── plan.md
    ├── tasks.md
    └── validation.md
```

## Step 0：能力识别

> **输出**：将以下模板填写后写入 `requirements.md` 的 `## 能力识别` 节（作为 Step 1 Specify 的前置内容）。

```markdown
## 能力识别

- 业务对象：
- 模型清单（有多个模型时必填，按依赖顺序排列）：
  | # | 模型名 | Java 类名 | model_name | 与其他模型的关系 |
  |---|---|---|---|---|
  | 1 | [主模型] | `[MainEntity]` | `[app_main]` | 一对多 → [SubEntity] |
  | 2 | [子模型] | `[SubEntity]` | `[app_sub]` | ManyToOne → [MainEntity] |
- 跨模型自定义服务（有复杂业务流程时必填）：
  | 服务名 | 挂载模型 | 涉及模型 | 业务目标 |
  |---|---|---|---|
  | `[serviceName]` | `[model]` | `[model1], [model2]` | [说明] |
- 后端能力域：
- 前端实现分支：
- 是否需要新增 App：
- 是否需要新增前端扩展应用：
- 是否涉及权限/多租户/多语言：
- 是否涉及文件/Excel/打印/任务：
- 待确认事项：
```

## Step 1：Specify

`requirements.md` 必须回答：

| 问题             | 当前项目写法                                        |
| ---------------- | --------------------------------------------------- |
| 用户要完成什么   | 用业务语言描述流程和结果                            |
| 后端要提供什么   | 模型、字段、服务、视图、菜单、数据、权限            |
| 前端实现分支意向 | 标准模板/在线视图优先，必要时说明倾向 hook 或扩展（最终判定在 frontend-spec.md §9） |
| 契约是什么       | JSON-RPC、Filter、args、视图 key、节点 id、数据源名 |
| 如何验收         | 文件登记、编译、启动、页面链路、权限和异常          |

---

## Step 1.2：Clarify（必经，Step 1 完成后立即执行）

`requirements.md` 初稿写完后，**在进入 Step 1.5 Spec 之前必须执行 Clarify**。目的是在规格固化前消除歧义、补全遗漏，避免下游规格和实现的高代价返工。

> 用户明确说"跳过澄清"时，必须提示：**"跳过 Clarify 将增加 Plan / Implement 阶段的返工风险，待确认事项将在规格中保留为'待确认'标记。"** 确认跳过后方可继续。

### 扫描维度（10 类）

对 `requirements.md` 逐维度扫描，识别歧义、缺失和不可测项：

| # | 维度 | IIDP 典型歧义 |
|---|---|---|
| 1 | **功能范围与行为** | 成功标准不可量化；边界不清；不做事项未声明 |
| 2 | **领域模型** | `@Model` 实体缺字段；状态枚举未列全；ER 关系方向不明；主键策略未定 |
| 3 | **交互与 UX 流程** | 空态/加载态/异常态未定义；标准模板 vs 扩展实现未决策；多步骤流程跳转路径不清 |
| 4 | **非功能属性** | 分页默认值未定；列表数据量量级未估；并发写入场景未考虑；导出文件上限未定 |
| 5 | **集成与外部依赖** | 跨 App `@Model` 依赖类型（`strong`/`weak`）未选；外部系统接口协议假设未确认；数据源名称未定 |
| 6 | **边界条件与失败处理** | 无权限时行为未描述；状态流转拒绝的错误提示未定；`ModelException` vs `ValidationException` 场景划分不清 |
| 7 | **约束与权衡** | IIDP 标准能力是否足够未确认；被否决的实现方案未记录；平台限制未标注 |
| 8 | **术语与命名一致性** | `model_name` 前缀规范未统一；同一业务概念用了多个名称；视图 key / 菜单 key 命名规则未定 |
| 9 | **完成信号** | AC 不可测试（"系统运行正常"之类）；未定义人工验收的前置条件；DoD 缺失 |
| 10 | **IIDP 特有待确认项** | 节点 id 来源未确认；视图 key 是否稳定；菜单层级和父菜单 key 未定；权限码分配责任人未明 |

### 问题生成规则

- **每次最多生成 5 个问题**（单次 Clarify session 上限）。
- 每个问题必须满足：影响架构/模型/任务拆解/测试设计/UX 行为其中之一；可通过 2–4 个选项选择，或 ≤5 字简答。
- 优先覆盖歧义程度最高的维度，已有明确答案的跳过。
- **不问实现细节**（如"用哪个 Java 类名"），那属于 Spec 阶段。

### 答案写回格式

用户回答后，将确认内容写入 `requirements.md`，两个动作同时完成：

**① 在文件末尾维护 `## Clarifications` 节**（不存在则新建）：

```markdown
## Clarifications

### Session YYYY-MM-DD

- Q: 订单状态枚举有哪些？ → A: DRAFT / SUBMITTED / APPROVED / REJECTED（四态）
- Q: 无权限时是隐藏按钮还是禁用并提示？ → A: 按钮不显示，后端服务仍做 auth 校验
- Q: 列表默认分页大小？ → A: 20 条/页，最大 200
```

**② 同步更新受影响章节**（替换旧内容，不保留矛盾描述）：
- 答案影响字段清单 → 更新字段表
- 答案影响 AC → 更新验收标准
- 答案影响权限 → 更新权限描述

写回完成后，扫描 `requirements.md` 中残留的"待确认"标记，确认已全部通过 Clarify 处理或有理由保留。

### Clarify Prompt

```text
请对 specs/features/[phaseN-feature]/requirements.md 做 Clarify 扫描：

1. 按 10 个维度（功能范围、领域模型、交互流程、非功能属性、集成依赖、边界条件、约束权衡、术语命名、完成信号、IIDP 特有）识别歧义和缺失项。
2. 从中选出影响最大的 ≤5 个问题，每个问题提供 2–4 个选项或简答指引。
3. 逐问题等待我的回答（不要一次列完等我批量回答）。
4. 每条回答确认后，立即更新 requirements.md：写入 ## Clarifications 节，并同步修改受影响章节。
5. 所有问题回答完毕后，列出 requirements.md 中剩余"待确认"标记及保留理由。
6. 不进入 Step 1.5 Spec 之前不得跳过此流程。
```

---

## Step 1.3a：定义本 Phase 前后端契约（**必须先于 backend-spec 和 frontend-spec，强制步骤**）

生成任何规格书之前，先在以下两个文件中完成本 Phase 的契约定义。
**定义时必须参考对应的 backend skills 文件，不得凭记忆填写注解参数、字段类型或 JSON 结构。**

---

**① integration-map.md** — 架构级契约

| 契约项 | 内容 | 必须先读的 backend skills |
|---|---|---|
| 模型清单 | model_name、Java 类名、ER 关系（含外键字段名）、状态=In-Flight | `references/core/model.md`、`references/core/app-json.md` |
| 权限码（唯一定义来源） | `{model_name}:{action}` 格式，含义；角色映射可"待确认" | `references/core/method-service.md` §注解属性 |
| 跨模型服务（如有） | 服务名、挂载模型、涉及模型、事务边界 | `references/core/method-service.md` §跨模型 RPC |
| 前端实现方式决策 | 每个页面的实现方式（标准模板/hook/扩展视图/Vue2）及原因 | `references/core/view.md`、`references/core/view-advanced.md` |

---

**② sdd-contracts.md** — API 级契约

| 契约项 | 内容 | 必须先读的 backend skills |
|---|---|---|
| 模型属性契约 | 字段名、`@Property` 关键参数（displayName/dataType/store 等）、前端类型、是否必填 | `references/core/model.md`、`references/core/model-property-advanced.md` |
| ER 字段契约 | 外键字段（存 ID）+ related 字段（带出展示）+ ManyToOne/OneToMany 声明 | `references/core/model.md` §ER 关系注解、`references/core/model-property-advanced.md` |
| 服务签名契约 | 服务名（`@MethodService.name`）、参数名、参数类型（Java→前端映射）；内置服务标"内置契约" | `references/core/method-service.md` |
| 视图 key / 菜单 key | 命名规则、视图类型（grid/search/form/tree）、菜单 parent_ids | `references/core/view.md`、`references/core/menu.md` |
| app.json 条目 | resolved、view 数组、data 数组 | `references/core/app-json.md` |

---

> **契约先行原则（强制）**：
> - backend-spec 的字段注解、服务签名、auth 值、视图结构 必须取自上表已定义内容，**不得自行发明**
> - frontend-spec 的字段类型、服务参数、前端实现方式 必须取自上表已定义内容，**不得自行发明**
> - 若生成规格书时发现契约条目缺失，**必须先返回本步骤补充（并先读对应 backend skills），再继续规格书生成**
> - 契约与规格书产生冲突，**以修订契约为准，同步更新规格书**，不得只改规格书

---

## Step 1.4：Critique（可选，规格重大或不确定性高时触发）

Critique 从两个对立视角对 `requirements.md` 做批判性审查，目的是在生成规格书前识别高风险假设和业务决策漏洞。**不修改规格，只输出发现报告并等待用户决策。**

### 触发条件

用户明确要求，或满足以下任一：规格涉及 3 个以上模型、包含状态机、有明显歧义的成功标准、待确认事项超过 3 项。

### 产品战略视角质疑点

- 这个功能解决的是用户真实痛点，还是技术负债或低频诉求？
- 功能边界是否清晰，是否会在实现中自然扩大范围（scope creep）？
- 成功标准是可量化验收的，还是模糊的主观判断？
- 是否存在更轻量的实现方式（如后端在线视图已满足，无需前端扩展）？
- 多模型设计是否必要，还是过度设计？

### 工程风险视角质疑点

- 规格中是否有"待确认"事项会在 Implement 阶段造成阻塞？
- ER 关系、状态机、跨模型服务的事务边界是否足够清晰？
- 权限码、视图 key、菜单 key 是否已经稳定，还是可能在实现中变更？
- 前端实现分支判断是否会因为节点 id 未确认导致返工？
- 是否有遗漏的验收场景（异常态、权限边界、并发修改、多租户）？

### Critique 输出格式

```markdown
## Critique 报告：[功能名称]

### 产品战略发现

- [高/中/低] [发现描述]

### 工程风险发现

- [高/中/低] [发现描述]

### 需要用户决策的事项

1. [问题描述] — 选项 A：[...] / 选项 B：[...]
```

输出后**暂停，等待用户决策**后再进入 Step 1.5 Spec。

---

## Step 1.5：Spec（技术落地规格）

`requirements.md` 完成后、Step 1.3a 契约定义完成后、进入 Plan 之前，生成技术落地规格。后端规格和前端规格每个功能都必须生成。

### Step 1.5a：Backend Spec（必须生成）

读取 `sdd-backend.md` 模板，输出 `backend-spec.md`。**所有功能必须生成此文件**，它是 Step 4 后端实现的输入。

必须包含：

| 章节       | 内容                                                            | 对应 sdd-backend.md 章节 |
| ---------- | --------------------------------------------------------------- | ------------------------ |
| 命名       | appName、appPkg、moduleName、model_name、Java 类名、菜单 key    | §1                       |
| 工程文件   | 新增/修改文件清单（POM、app.json、视图、菜单、apps.json）       | §2                       |
| 模型设计   | 字段、IIDP 注解、校验、索引；多模型时先写 ER 总览再逐模型展开   | §3                       |
| 服务设计   | 内置/自定义服务清单、入参、出参、权限、事务边界；每个自定义服务附详细设计块（入参校验、查询逻辑、业务步骤、异常分层） | §4                       |
| 视图和菜单 | 视图 key、类型、grid/search/form 配置；菜单 key、model、view    | §5                       |
| 数据和权限 | 种子数据、字典、菜单/按钮/服务权限码（**必须取自** Step 1.3a 定义的 integration-map.md 权限码总览，不得自行发明） | §6                       |
| 验收       | app.json 登记、命名一致、JSON 可解析、编译                      | §7                       |

### Step 1.5b：Frontend Spec（必须生成）

**所有功能都必须生成此文件。** 即使标准模板页面无需前端代码，也需要按模板输出规格文档，在"9. IIDP 实现分支"中明确标注"标准模板/在线视图，前端无需新增代码"及理由。这样确保每个页面的前端承载方式都有明确记录，避免后续实现时遗漏判断。

读取 `sdd-frontend.md` 模板，输出 `frontend-spec.md`，必须包含：

| 章节                 | 内容                                                    | 对应 sdd-frontend.md 章节 |
| -------------------- | ------------------------------------------------------- | ------------------------- |
| 页面定位             | 实现分支、目标工程、目标应用                            | §1                        |
| 页面结构             | 搜索区、表格区、工具栏、弹窗等区域划分                  | §3                        |
| UI 宪法约束          | 标准模板匹配度、容器策略                                | §4                        |
| 页面骨架与 IIDP 映射 | 骨架树、原型到 IIDP 承载方式                            | §5                        |
| 节点与绑定规格       | 节点树、selector、绑定/事件/命令                        | §6                        |
| 字段规格             | 控件类型、校验、默认值                                  | §7                        |
| 操作与事件           | 入口、触发条件、前端行为、后端服务                      | §8                        |
| IIDP 实现分支        | 标准模板/hook/扩展视图/自定义 Vue2 的选择依据和实现要求 | §9                        |
| 数据源与绑定         | ds_config、reqPrep/reqAfter、autoRequest                | §10                       |
| 待确认事项           | 节点 id、接口、模型、权限码                             | §13                       |

**`backend-spec.md` 和 `frontend-spec.md` 是 Step 4 实现阶段子 skill 的输入。** 缺失时必须先补全再进入实现。

### Step 1.5c：Interaction Spec（条件生成）

**触发条件**：满足以下任一时生成，否则跳过：
- 页面含复杂状态机（超过 3 个状态且有状态转移动作）
- 需要明确响应式布局策略（弹窗滚动、表格高度、栅格列宽）
- 有可访问性要求（WCAG、键盘操作、屏幕阅读器）
- 含批量操作、危险操作确认或多步向导流程

读取 `sdd-frontend-interaction.md` 模板，输出 `interaction-spec.md`，必须包含：

| 章节 | 内容 | 对应 sdd-frontend-interaction.md 章节 |
|---|---|---|
| 页面目标 | 目标用户、核心任务、推荐实现方式 | §1 |
| 用户流程 | 进入→操作→成功/失败完整路径 | §2 |
| IIDP 节点层级 | 页面节点树（page > container > 子节点） | §3 |
| 交互状态表 | 每个节点的 default/loading/empty/error/disabled 及触发条件 | §4 |
| 节点属性与数据契约 | id 来源、selector、ds_config、bind_on_、hook | §5 |
| 响应式与容器策略 | 栅格、弹窗尺寸、滚动归属、TABIFRAME | §6 |
| 可访问性与可用性 | label、必填标识、错误文字、危险操作确认 | §7 |
| 验收标准 | 可操作、可验证、可追踪的检查项 | §8 |

`interaction-spec.md` 与 `frontend-spec.md` **不重复输出**：节点 id / selector / ds_config 等技术细节写在 `frontend-spec.md`，交互状态表、用户流程、响应式策略写在 `interaction-spec.md`。

**三个技术规格文件总结：**

| 文件 | 来源模板 | 生成条件 | Step 4 读取方 |
|---|---|---|---|
| `backend-spec.md` | `sdd-backend.md` | 必须生成 | backend skill |
| `frontend-spec.md` | `sdd-frontend.md` | 必须生成 | frontend skill（§9 实现分支决定是否写代码） |
| `interaction-spec.md` | `sdd-frontend-interaction.md` | 含复杂状态/响应式/可访问性时 | frontend skill |

---

## Step 2：Plan

```markdown
# 实现计划：[功能名称]

## 方案概述

[2-3 句话说明为什么采用这些 IIDP 能力]

## 后端改动

- Maven 模块：
- app.json：
- Java 模型：
- 服务：
- 视图 JSON：
- 菜单与种子数据：
- apps/apps.json：

## 前端改动

- 实现分支：标准模板/在线视图/hook/扩展视图/Vue2 组件
- 目标工程：
- 目标应用：
- 目标页面或节点 id：
- 需要修改的目录：

## 风险与待确认

- [接口/模型/节点 id/权限码/枚举]
```

## Plan Review Gate（Step 2 完成后的强制门控）

`plan.md` 生成后，AI **必须暂停**，向用户展示计划摘要并等待明确确认，再进入 Step 3 生成任务清单。**不得在用户确认前自动执行 Step 3。**

### 计划摘要模板

```markdown
## 计划摘要确认：[功能名称]

**后端改动量**：[N] 个模型 / [N] 个服务 / [N] 个视图文件
**前端实现分支**：[标准模板/hook/扩展视图/Vue2 组件]（[N] 个页面需前端代码，[N] 个无需）
**风险与待确认**：

- [待确认事项 1]
- [待确认事项 2]
  **估计复杂度**：S（< 1 天）/ M（1–3 天）/ L（> 3 天）

计划已就绪。**确认后生成任务清单**；如需调整，请说明修改点。
```

---

## Step 3：Tasks

单模型业务使用下方基础模板；**多模型业务必须为每个模型生成独立任务块**，并在末尾补充跨模型服务任务。

**TDD 模式**（用户明确要求"测试先行"或"TDD"时激活）：将"测试·后端服务"块移至对应实现块之前，Agent 先生成测试骨架、假定红灯，再执行实现；默认模式下测试块保留在原位（实现之后）。

生成 tasks.md 时，同步从 `requirements.md` 的验收标准提取测试用例 ID（TC-BE-xx / TC-FE-xx），写入 `validation.md` 的"测试用例规格"节（见 `sdd-validation.md` §测试用例规格）；tasks.md 测试任务通过 TC-ID 与 validation.md 对应。

```markdown
# 任务清单：[功能名称]

## Git·分支准备（每个 feature 执行一次，写代码前必须完成）

- [ ] [S] 确认当前所在分支：`git branch --show-current`
- [ ] [S] 从主干创建 feature 分支：`git checkout -b feature/{appName}/phase{N}-{feature-name}`
- [ ] [S] 确认切换成功后再开始任何文件改动

## 后端·工程基础（只做一次）

- [ ] [S] 确认所有模型命名：appName、appPkg、moduleName、各 model_name、菜单 key
- [ ] [M] 新增或修改业务模块 POM
- [ ] [M] 新增 `app.json` 并预登记所有模型的 view/data 路径

## 后端·模型 1：[ModelName]

- [ ] [M] 编写 Java `@Model` 和字段（含 ER 关系注解）
- [ ] [M] 编写 `views/[model_name]_view.json`（grid/search/form）
- [ ] [S] 编写菜单入口和种子数据

## 后端·模型 2：[ModelName]

- [ ] [M] 编写 Java `@Model` 和字段（含 ManyToOne 关联父模型）
- [ ] [M] 编写 `views/[model_name]_view.json`
- [ ] [S] 编写菜单入口和种子数据

<!-- 有更多模型时，复制上方模型块，按依赖顺序追加 -->

## 后端·跨模型自定义服务（有复杂流程时必写）

- [ ] [M] 编写 `[serviceName]`：校验状态、权限、作用域；操作涉及模型；事务采用 IIDP 请求级事务（失败抛 `ModelException` 自动回滚），如需分段提交则用 `Meta.flush/commit`
- [ ] [M] 编写 `[serviceName2]`（如有）

## 后端·登记与收尾

- [ ] [S] 更新 `apps/apps.json` 登记新 jar

## 测试·后端服务（TDD 模式时移至各实现块之前）

<!-- 每个 @MethodService 方法对应 validation.md 中的 TC-BE-xx；测试骨架由 AC 自动派生 -->
- [ ] [M] 新建测试文件 `src/test/java/[pkg]/[ModuleName]ServiceTest.java`
- [ ] [M] TC-BE-01：[AC 描述，如"正常提交：草稿→已提交"]——Arrange / Act / Assert 三段注释已填，实现留空
- [ ] [S] TC-BE-02：[AC 描述，如"非法状态拒绝：非草稿抛 ModelException"]
- [ ] [S] TC-BE-03：[AC 描述，如"权限拦截：无 auth 抛异常"]
<!-- 按 validation.md 测试用例规格中的 TC 数量补充行，一条 AC 对应一条任务 -->
- [ ] [S] 运行 `mvn test -pl [module]` 确认测试骨架可编译（红灯通过即可，业务逻辑在实现后补全）

## 前端·实现分支判断（必先做，基于 frontend-spec.md §9）

- [ ] [S] 读取 `frontend-spec.md`"9. IIDP 实现分支"，确认每个页面的实现方式
- [ ] [S] 标准模板/在线视图的页面，勾选"前端无需新增代码"，跳过后续前端任务段
- [ ] [S] 需要前端介入的页面（hook/扩展视图/Vue2 组件），列出目标工程、目标应用 `apps/<appName>`、目标节点 id 和 id 来源

## 前端·工程基础（仅全新前端时执行一次）

- [ ] [S] 使用 `tech project <projectName>` 创建工程
- [ ] [M] 使用 `tech app <appName>` 创建扩展应用
- [ ] [S] 配置 `apps/<appName>/config/app.json`、`effectPaths`

## 前端·页面 1：[页面名]（按页面/模型逐个展开）

- [ ] [M] 编写扩展视图 `apps/<appName>/views/<business>/<business>.js`
- [ ] [S] 在 `apps/<appName>/views/index.js` 汇总导出
- [ ] [M] 如需 hook：写在同一个 business 文件中，使用 `selector` + `type: "merge"` + `hook` 结构
- [ ] [M] 如需自定义 Vue 组件：放 `apps/<appName>/common/comps.js` 注册（扩展视图 JS 禁止 import `.vue`）
- [ ] [S] 如需公共组件复用：放 `apps/component/`

<!-- 有更多页面时，复制上方页面块，按业务顺序追加 -->

## 前端·收尾

- [ ] [S] 运行 `npm run lint` 或说明无法运行原因
- [ ] [S] 浏览器控制台用 `tech_app.printObj(节点id)` / `Tech.$page(id)` 验证节点

## 测试·前端验收场景

<!-- 每条对应 validation.md 中的 TC-FE-xx，在 IIDP 运行时中手工执行 -->
- [ ] [M] TC-FE-01：[AC 描述，如"列表加载：进入页面后表格显示数据行"]——前置条件 / 操作步骤 / 预期结果已在 validation.md 填写
- [ ] [S] TC-FE-02：[AC 描述，如"新增弹窗：点击新增按钮弹窗打开，提交后列表刷新"]
- [ ] [S] TC-FE-03：[AC 描述，如"权限隐藏：无权限用户不显示操作按钮"]
<!-- 按 validation.md TC-FE 数量补充行 -->
- [ ] [M] 按 validation.md 测试用例规格逐条执行，记录"通过 / 失败 / 阻塞"和截图/日志

## 验证（构建与静态检查）

- [ ] [S] 运行空白和 JSON 校验（所有模型视图文件）
- [ ] [M] 运行 Maven 编译或说明无法运行原因
- [ ] [M] 运行前端 lint/build 或说明无法运行原因
- [ ] [S] 后端测试：`mvn test -pl [module]` 全绿（骨架通过 + 业务逻辑实现后）
- [ ] [S] 所有 TC-BE-xx 和 TC-FE-xx 在 validation.md 中标注执行结果
```

## Step 3.5：Blueprint（可选，复杂功能或团队初次接入 IIDP 时触发）

Blueprint 在开始写代码之前，先生成一份完整的代码蓝图（伪代码 + 文件清单），让用户验证方向正确后再执行。**不写真实代码，只描述结构和逻辑。**

### 触发条件

用户明确要求，或满足以下任一：L 级复杂度、包含跨模型服务、团队第一次在该项目使用 IIDP 前端扩展。

### 蓝图内容要求

**后端蓝图**：

- 每个 Java 文件的路径、类名、关键字段和方法签名（伪代码，不写完整实现）
- 每个视图 JSON 的结构骨架（key、model、主要按钮 service）
- `app.json` 的 view/data 登记路径清单

**前端蓝图**（需要前端代码时）：

- 每个扩展文件的路径和扩展类型（`before/after/merge/replace/custom`）
- 关键节点的 `selector` 和 `type`，标注节点 id 来源（用户提供 / 推导 / 待确认）
- hook 的挂载路径和伪代码逻辑（不写真实 JS）

### Blueprint 输出格式

```markdown
## 代码蓝图：[功能名称]

### 后端文件清单

| 文件路径                | 类型             | 关键内容                            |
| ----------------------- | ---------------- | ----------------------------------- |
| `src/.../XxxModel.java` | `@Model`         | 字段：id / name / status / ...      |
| `views/xxx_view.json`   | grid/search/form | 按钮：新增 / 编辑 / 删除 / [自定义] |
| `data/menus.json`       | 菜单             | 挂载：[父菜单 key]                  |

### 前端文件清单（仅需前端代码时）

| 文件路径                    | 扩展类型 | 目标节点    | id 来源 |
| --------------------------- | -------- | ----------- | ------- |
| `apps/[app]/views/[biz].js` | merge    | `[node_id]` | 待确认  |
```

输出后**暂停，等待用户确认蓝图**，确认后进入 Step 4 Implement。

---

## Step 4：Implement

**实现阶段必须经由对应子 skill，不要直接基于 create-project 的轻量任务清单写代码。**

| 任务类型                               | 必须经由                                                         | 输入文件                       |
| -------------------------------------- | ---------------------------------------------------------------- | ------------------------------ |
| 后端工程、模型、视图、菜单、服务、数据 | `skills/backend/SKILL.md` 的 Step 1～10 流程                     | `backend-spec.md`（必须存在）  |
| 前端规格 → 代码（含工程初始化）        | `skills/frontend/SKILL.md` 路由 → `iidp-frontend-spec-code`      | `frontend-spec.md`（必须存在） |
| 前端扩展开发（已有工程内）             | `skills/frontend/SKILL.md` 路由 → `iidp-frontend-extension-code` | `frontend-spec.md`             |
| 框架/组件/扩展协议查阅                 | `skills/frontend/SKILL.md` 路由 → `iidp-frontend-dev-manual`     | 无                             |
| 节点 id 后缀推导                       | `skills/frontend/SKILL.md` 路由 → `iidp-frontend-standard-ids`   | 无                             |

**重要**：

- 后端 skill (`skills/backend/SKILL.md`) 必须读取 `backend-spec.md` 作为输入，按 Step 1～10 执行代码生成。如果 `backend-spec.md` 缺失，必须先回到 Step 1.5a 生成。
- `iidp-frontend-spec-code` 必须读取 `frontend-spec.md`。读取后先检查"9. IIDP 实现分支"：如果实现分支为"标准模板/在线视图"且明确"前端无需新增代码"，则跳过前端代码生成，仅在 tasks.md 中勾选"前端无需新增代码"；如果需要 hook、扩展视图或自定义 Vue2 组件，则正常调用前端 skill 写代码。

执行规则：

- 一次只处理一个任务。
- 修改前读取相邻文件，沿用当前工程风格。
- 不顺手重构无关模块。
- 不回退用户已有变更。
- 对缺失事实写"待确认"，不补成假事实。
- **每个任务完成后回到 `tasks.md` 勾选状态，再启动下一个任务**；不要批量声明完成。

## Step 5：Validate

验收必须覆盖文件登记、命名一致、Java 模型、视图菜单、服务 API、前端扩展和部署运行。详细清单见 `sdd-validation.md`。
