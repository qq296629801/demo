# IIDP 项目宪法

## 核心定位

SDD 在当前项目中不是“先选一个 Web 框架再生成代码”，而是先把业务需求转换成 IIDP 平台能执行的结构化契约：

- 后端落点：Maven 模块、`app.json`、Java `@Model`、`@Property`、`@Validate`、`@MethodService`、`views/*.json`、`data/*.json`、`menus.json`、`apps/apps.json`。
- 前端落点：标准模板、在线视图、后端视图配置、扩展应用、hook、扩展视图、数据源、属性绑定、事件绑定、commands、自定义 Vue2 组件。
- 集成落点：JSON-RPC `service` 调用、平台 Filter、视图按钮 `service/args/auth/actionAfter`、菜单 `view`、权限码、多租户和多语言规则。

## 三条约束

1. **IIDP 规范优先**：外部通用规范与 IIDP 平台写法冲突时，以当前工程可运行的 IIDP 写法为准。
2. **契约先行**：模型名、视图 key、菜单 key、服务名、权限码、数据源名、节点 id 等必须在生成 backend-spec 和 frontend-spec 之前，先在 `integration-map.md`（架构级）和 `contracts.md`（API 级）中写清楚（对应 sdd-workflow.md Step 1.3a，强制步骤）。填写时必须参考 `skills/backend/references/core/` 对应文件，不得凭记忆填写注解参数、字段类型或 JSON 结构。
3. **不编造平台事实**：未知接口、模型、节点 id、菜单 id、枚举、权限码必须写为“待确认”。

## 文件结构

```text
specs/
├── mission.md
├── iidp-stack.md
├── ui-constitution.md
├── roadmap.md
├── integration-map.md          # 架构级契约（模型清单、ER、权限码、前端实现方式决策）
├── decisions.md
├── templates/                  # 可选，项目级模板覆盖（见下方"项目级模板覆盖规则"）
│   ├── requirements.md
│   ├── contracts.md
│   └── ...
└── modules/
    └── {moduleName}/
        ├── requirements.md
        ├── contracts.md        # API 级契约（属性/ER字段/服务签名/视图key，Step 1.3a 生成）
        ├── backend-spec.md
        ├── frontend-spec.md
        ├── plan.md
        ├── tasks.md
        └── validation.md
```

---

## 项目级模板覆盖规则

本文件下方的各 `## *.md 模板` 节是**技能级默认模板**。项目可在 `specs/templates/` 目录下放置同名文件，AI 生成规格文件时优先使用项目级模板，找不到时 fallback 到本文件默认模板。

**读取优先级**：`specs/templates/{filename}.md` > 本文件 `## {filename}.md 模板` 节

| 可覆盖的模板 | 项目级覆盖路径 | 本文件默认位置 |
|---|---|---|
| mission.md | `specs/templates/mission.md` | `## mission.md 模板` |
| iidp-stack.md | `specs/templates/iidp-stack.md` | `## iidp-stack.md 模板` |
| ui-constitution.md | `specs/templates/ui-constitution.md` | `## ui-constitution.md 模板` |
| roadmap.md | `specs/templates/roadmap.md` | `## roadmap.md 模板` |
| integration-map.md | `specs/templates/integration-map.md` | `## integration-map.md 模板` |
| requirements.md | `specs/templates/requirements.md` | `## requirements.md 模板` |
| contracts.md | `specs/templates/contracts.md` | `## contracts.md 模板` |

**查找示例**：生成 `requirements.md` 时，先尝试读取 `specs/templates/requirements.md`。
- 存在 → 以该文件为骨架，将本功能的业务内容填入其中的 `[...]` 占位符。
- 不存在 → 使用本文件 `## requirements.md 模板` 节中的内容作为骨架。

> **三条约束不受此规则影响**：无论使用哪层模板，本文件 `## 三条约束` 节中的三条规则始终强制适用，项目级模板不得降低或绕过这三条约束的要求。

> **初始化**：运行 `/sdd-init-templates` 可将选定的默认模板复制到 `specs/templates/` 供用户编辑；也可手动在该目录创建对应文件名的 `.md` 文件直接开始定制。

---

## mission.md 模板

```markdown
# 项目使命

## 项目名称
[业务系统或 App 名称]

## 使命陈述
[为哪个角色提供什么能力，解决什么业务问题]

## 目标用户
- 主要用户：
- 次要用户：

## 核心业务价值
- [价值 1]
- [价值 2]

## v1.0 成功标准
- [ ] [可验证的业务目标]
- [ ] [可验证的运行目标]

## 明确不在范围内
- [本阶段不做的能力]
```

## iidp-stack.md 模板

```markdown
# IIDP 技术栈与约束

## 项目类型
- 类型：全新工程（greenfield）/ 存量接入（brownfield）
- 初始化方式：
  - 全新工程：由 `skills/backend/greenfield/SKILL.md` 克隆父工程后，从父工程 `pom.xml` 确认版本，团队协商命名约定
  - 存量接入：由 `/sdd-brownfield-init` 通过 code-index 从现有代码自动提取
- 初始化时间：[YYYY-MM-DD]

> **大模型读取规则**：
> - `存量接入` → 下方版本约束和命名规范为**从现有代码提取的强制规范**，新增代码必须严格遵循，不得偏离现有命名风格
> - `全新工程` → 下方内容来自克隆父工程 `pom.xml` 和团队约定；如有 `待确认` 项，须在步骤 2 宪法阶段补全后才进入步骤 3

## 后端工程
- 目标工程：`iidp-backend-demo-ai`
- Java：主工程 Java 8；特殊模块按当前 POM 约束执行
- 构建：Maven，使用项目内 `settings.xml`
- 平台：IIDP 元模型、Snest SDK/Engine、Spring Boot 启动模块
- 运行依赖：MySQL、Redis、MinIO；按需求使用 XXL-Job
- 接口协议：JSON-RPC `POST /root/rpc/service/master`

## 后端生成约束
- 新增业务 App 默认放在 `sie-iidp-demo-apps/sie-iidp-demo-{appName}`
- `app.json` 必须位于 `resolved` 对应包路径
- 普通业务模型必须有 `grid/search/form` 三类视图
- 视图、菜单、数据、文件种子必须登记到 `app.json`
- 新增 jar 必须登记到 `apps/apps.json` 的 `apps.SDK`
- 不新增无明确业务必要的第三方依赖

## 前端工程
- 创建工程：只能使用 `tech project <projectName>`
- 创建扩展应用：只能在工程根目录使用 `tech app <appName>`
- 传统管理后台：优先标准模板和在线视图，通常不新增前端代码
- 前端扩展顺序：hook → 扩展视图 → 自定义 Vue2 组件
- Element UI 已全局引入，自定义组件中不要单独引入

## 前端生成约束
- 业务扩展默认只改 `apps/<appName>/views`、`common`、`config`、`apps/component`
- 不读取或修改 `node_modules`、`dist`、`distApp`、`distTmp`、`umdComps`、`build`
- 目标节点 id 不得猜测；优先用户提供，其次标准模板规则库，最后询问
- `effectPaths.includeRegExp` 不带 `/iidp/` 前缀

## Git 工作流约定
- feature 分支命名：`feature/{appName}/{moduleName}`（与 `specs/modules/{moduleName}/` 目录对齐）
- 每个 feature 对应一个分支和一个 PR，从主干（main/master）创建
- 合并策略：Squash merge，PR 描述引用对应 `specs/modules/` 规格目录
- **写代码前必须切换到 feature 分支**，不得在主干直接修改工程文件

## 版本约束
> 存量项目由 `/sdd-brownfield-init` 自动填充；全新工程由团队在宪法阶段手动确认。

- IIDP 引擎（sie-snest-engine）：[待确认]
- IIDP SDK（sie-snest-sdk / sie-iidp-sdk）：[待确认]
- IIDP Maven 插件（sie-snest-maven-plugin）：[待确认]
- Spring Boot：[待确认]
- Java：[待确认]
- XXL-Job（如启用）：[待确认 / N/A]
- 前端框架（@sie/iidp-* / vue / element）：[待确认 / N/A]

## 命名规范
> 存量项目由 `/sdd-brownfield-init` 从现有代码提取后填入，为**强制规范**；
> 全新工程由团队在宪法阶段协商后填入，为**设计约定**。

| 对象 | 规则 | 示例（来自现有代码） | 来源 |
|------|------|---------------------|------|
| 模型 name（`@Model.name`） | [待确认 \| 如 `{app_prefix}_{entity}`] | [如 `demo_student`] | `@Model` 注解 |
| 表名（`@Model.tableName`） | [待确认 \| 如 `{entity_snake_case}`] | [如 `example_student`] | `@Model` 注解 |
| 视图 key | [待确认 \| 如 `{model_name}_{type}`] | [如 `demo_student_grid`] | `*_view.json` |
| 菜单 key（功能菜单） | [待确认 \| 如 `{prefix}_{entity}_menu`] | [如 `demo_student_menu`] | `menus.json` |
| 菜单 key（根菜单） | [待确认 \| 如 `{prefix}_{module}_root_menu`] | [如 `demo_example_menu`] | `menus.json` |
| 种子数据文件名 | [待确认 \| 如 `{model_name}.json`] | [如 `example_student.json`] | `data/` 目录 |
| 字典种子文件名 | [待确认 \| 如 `{typeCode}_dict.json`] | [如 `yes_no.json`] | `data/` 目录 |
| appPkg（`@Model` 所在包） | [待确认 \| 如 `com.sie.iidp.{domain}`] | [如 `com.sie.iidp.example`] | Java 包名 |
| Maven 模块名 | [待确认 \| 如 `sie-iidp-demo-{feature}`] | [如 `sie-iidp-demo-student`] | 目录名 |
| `@MethodService` description | [待确认 \| 内置名或中文描述] | [如 `"create"` / `"启用禁用"`] | `@MethodService` |

## 现有 App 清单（存量项目必填，全新工程在步骤 2 填写）
> 存量项目由 `/sdd-brownfield-init` 从 `apps/apps.json` 提取。

| appName | resolved 包 | 职责 | 依赖 App |
|---------|------------|------|---------|
| [待填充] | | | |
```

## ui-constitution.md 模板

```markdown
# IIDP UI 宪法

## 适用范围

- 项目类型：单 App / 多 App
- 涉及的 App 列表（多 App 项目必填）：
  | App | appName | 业务定位 | 是否独立扩展工程 |
  |---|---|---|---|
  | App 1 | `[demo-xxx]` | [说明] | 是/否，复用 `[appName2]` |
  | App 2 | `[demo-yyy]` | [说明] | 是/否 |
- 跨 App 共享部分：如全局主题、公共组件、统一菜单结构、共用语言包

下方所有视觉约束、组件规则默认适用全部 App；个别 App 有差异时在对应章节末尾以"App 级例外：[appName]"标注。

## 设计来源
- 原型来源：支持 MCP 的原型/设计/产品工具服务/截图/导出文档/文字描述/待确认
- MCP 服务名称（不限产品）：
- MCP 资源 URI：
- 页面或 Frame：
- 原型版本：

## 标准模板视觉约束
- 传统管理后台优先使用 IIDP 标准模板和后端在线视图，不为常规搜索、表格、表单、树、上下表另起自定义页面。
- 颜色、字号、间距、按钮类型、表单标签宽度优先继承平台主题和 Element UI，不硬编码大面积自定义视觉系统。
- 自定义样式只作用于业务扩展节点，使用稳定节点 id、`className`、`style` 或 `css`，不得污染平台全局样式。
- 按钮文案、空态、错误提示、权限提示使用业务可理解语言；多语言项目必须登记语言资源或标记待确认。

## IIDP 组件使用规则
- 节点树根结构遵守 `app > page > container > 具体组件`。
- 每个扩展新增节点必须有 `type`，稳定业务节点建议有唯一 `id`，子节点放在 `items`。
- 布局承载优先使用 `container`、`row`、`form-container`；表格使用 `table`，表单使用 `form`，按钮使用 `button`。
- 显隐优先使用 `display` 或 `bind_display`；交互事件优先使用真实存在的 `bind_on_事件名`。
- 组件属性不确定时先查 `skills/frontend/references/iidp-frontend-extension-dev/COMPONENT_RULES.md`，不要猜属性名。

## 响应式、容器与 iframe 约束
- 标准后台页面以平台容器自适应为主，避免用固定宽高破坏主框架、Tab、弹窗或抽屉布局。
- `row` 使用 24 栅格和 `span` 控制列宽；复杂表单嵌套可用 `form-container` 或 `row` 包裹。
- 表格高度、弹窗内容和详情区要说明滚动归属，避免页面、弹窗、表格三层同时滚动。
- 涉及 iframe、Tab 独立渲染或性能模式时，先查前端开发手册对应章节，规格中标记是否启用 `TABIFRAME`。

## 可访问性与可用性
- 表单字段必须有清晰 label、必填标识、校验信息和错误恢复方式。
- 按钮禁用、权限隐藏、加载中、空数据、异常态必须有文字反馈，不只依赖颜色。
- 弹窗/抽屉必须说明打开入口、关闭方式、保存成功后的刷新策略和失败后的停留策略。
- 批量操作、删除、状态变更等危险操作必须有确认或后端二次校验。

## 原型到 IIDP 的落地规则
- 原型描述只说明用户看到什么和怎么操作，不自动推导为自定义 Vue2 组件。
- 原型元素先映射到后端视图和标准模板；标准能力不足时再写 hook、扩展视图或自定义组件。
- 原型中的复杂动画、营销式布局、非后台交互如与当前 IIDP 管理后台不匹配，必须标记为需产品确认。
```

## roadmap.md 模板

```markdown
# 项目路线图

## Phase 1：[阶段名]
目标：[业务目标]

功能列表（每项链接到 `specs/modules/<feature>/` 下的规格文件）：

| 状态 | 功能 | 规格目录 | 涉及模型/页面 | 负责人 | 完成日期 |
|---|---|---|---|---|---|
| ☐ 待开始 / ▶ 进行中 / ✅ 完成 | [功能名 1] | [`specs/modules/{moduleName1}/`](modules/{moduleName1}/requirements.md) | `[model]`/[页面] | — | — |
| ☐ | [功能名 2] | [`specs/modules/{moduleName2}/`](modules/{moduleName2}/requirements.md) | `[model]` | — | — |

验收标准：
- [ ] 后端：每个功能的模型、视图、菜单、服务可被 IIDP 引擎加载（参见各 feature 的 `validation.md`）
- [ ] 前端：标准页或扩展应用可以完成核心流程（参见各 feature 的 `validation.md`）
- [ ] 集成：跨功能契约一致，权限码总览同步更新

## 技术债
| 问题 | 影响 | 优先级 | 计划处理阶段 | 关联 feature |
|---|---|---|---|---|
| [问题] | [影响] | 高/中/低 | Phase X | `{moduleName}` |
```

**约定**：
- 新建 feature 时同步在本 roadmap 表中追加一行，链接路径与 `specs/modules/` 目录一致。
- feature 完成时把 ☐ 改为 ✅，填完成日期，避免 roadmap 与实际进度脱节。

## integration-map.md 模板

> **填写时机（强制）**：在 **Step 1.3a** 填写，必须先于 `backend-spec.md` 和 `frontend-spec.md`。
> 填写时必须参考 `skills/backend/references/core/model.md`（ER 注解写法）和 `references/core/method-service.md`（权限码格式），不得凭记忆填写。

多模型业务必须按模型分组组织，并补充跨模型服务和权限码总览。

```markdown
# 前后端契约总览

## 模型清单与 ER 关系

| 模型 | Java 类 | model_name | 关系（含外键字段名） | 所属 App |
|---|---|---|---|---|
| [主模型] | `[MainEntity]` | `[main]` | 一对多 → [子模型] | `[appName]` |
| [子模型] | `[SubEntity]` | `[sub]` | ManyToOne → [主模型]（外键字段: `[mainId]`） | `[appName]` |

## 模型 1：[ModelName]

| 页面/能力 | 视图 key | 菜单 key | 服务 | 权限码 | 前端实现方式 | 待确认 |
|---|---|---|---|---|---|---|
| [页面] | `[grid/search/form]` | `[menu_key]` | `[service]` | `[auth]` | 标准模板/hook/扩展视图/Vue2 组件 | [事项] |

## 模型 2：[ModelName]

| 页面/能力 | 视图 key | 菜单 key | 服务 | 权限码 | 前端实现方式 | 待确认 |
|---|---|---|---|---|---|---|
| ... |

## 跨模型服务

| 服务 | 挂载模型 | 涉及模型 | 触发入口 | 事务边界 | 权限码 |
|---|---|---|---|---|---|
| `[serviceName]` | `[主模型]` | `[模型1], [模型2]` | grid 按钮/form 内 | IIDP 请求级事务（抛 `ModelException` 自动回滚） | `[auth]` |

## 权限码总览（唯一定义来源，backend-spec 和按钮 auth 字段必须取自此处）

| 权限码 | 含义 | 涉及模型 | 角色 |
|---|---|---|---|
| `[auth_code]` | [说明] | `[model]` | [角色] |
```

## requirements.md 模板

> **填写时机**：Step 0 能力识别完成后，由 `/sdd-specify` 生成并写入 `specs/modules/<feature>/requirements.md`。
> 所有 `待确认` 标记须在 Clarify 阶段处理；未处理的保留说明理由。

```markdown
# [功能名称] 功能规格

**功能目录**：`specs/modules/<moduleName>/`
**创建日期**：YYYY-MM-DD
**状态**：草稿 / 澄清中 / 待评审 / 已确认

---

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

---

## 用户故事与验收场景

> 每个用户故事必须可独立测试，优先级决定 MVP 范围。

### US-01 — [简短标题]（优先级：P1）

[用业务语言描述用户完成什么目标，以及为什么有价值]

**为何是此优先级**：[说明业务价值及依赖关系]

**独立可测**：[描述如何单独验证此故事，例如："可通过 [具体操作] 完整验证，交付 [具体价值]"]

**验收场景**：

1. **Given** [初始状态]，**When** [用户操作]，**Then** [预期结果]
2. **Given** [初始状态]，**When** [用户操作]，**Then** [预期结果]

---

### US-02 — [简短标题]（优先级：P2）

[用业务语言描述]

**为何是此优先级**：[说明]

**独立可测**：[描述]

**验收场景**：

1. **Given** [初始状态]，**When** [用户操作]，**Then** [预期结果]

---

### US-03 — [简短标题]（优先级：P3）

[用业务语言描述]

**为何是此优先级**：[说明]

**独立可测**：[描述]

**验收场景**：

1. **Given** [初始状态]，**When** [用户操作]，**Then** [预期结果]

---

### 边界条件与异常场景

- 当 [边界条件] 时，系统如何响应？
- 当 [异常场景] 时，错误提示是什么？
- 无权限时行为：隐藏按钮 / 禁用并提示 / 后端拦截？（待确认）

---

## 功能需求

> 使用 `待确认` 标记尚未明确的需求；Clarify 阶段处理后替换为具体值。

- **FR-001**：系统必须 [具体能力，如"允许用户新增学生信息"]
- **FR-002**：系统必须 [具体能力，如"校验必填字段并给出明确提示"]
- **FR-003**：用户必须能够 [关键交互，如"按班级和年级组合筛选学生列表"]
- **FR-004**：系统必须 [数据要求，如"分页展示，默认 20 条/页，最大 200 条/页"]
- **FR-005**：系统必须 [行为，如"记录所有状态变更操作日志"]
- **FR-006**：系统必须 [待确认：[具体内容未明确，如"认证方式未指定：本地账号/SSO/LDAP？"]]
- **FR-007**：系统必须 [待确认：[具体内容未明确，如"数据保留周期未定"]]

### 后端技术要求

| 问题 | 当前项目写法 |
|---|---|
| 后端要提供什么 | 模型、字段、服务、视图、菜单、数据、权限 |
| 契约是什么 | JSON-RPC、Filter、args、视图 key、节点 id、数据源名 |
| 如何验收 | 文件登记、编译、启动、页面链路、权限和异常 |

---

## 成功标准

> 每项必须可量化、可验证；不得写"系统运行正常"之类的模糊描述。

- **SC-001**：[可量化指标，如"用户可在 2 分钟内完成学生信息新增"]
- **SC-002**：[性能指标，如"列表查询响应时间 < 1s（500 条数据以内）"]
- **SC-003**：[覆盖指标，如"核心流程（新增/编辑/删除/查询）通过全部验收场景"]
- **SC-004**：[业务指标，如"Excel 导出功能覆盖当前筛选结果，最多 5000 条"]

---

## 假设与边界

- [用户假设，如"操作者已登录并拥有对应权限"]
- [范围边界，如"移动端适配不在 v1 范围内"]
- [数据/环境假设，如"学生数据来自现有 Student 模型，不新建独立数据库"]
- [外部依赖，如"依赖现有权限系统，权限码由运维分配"]
- [明确不做，如"不支持批量导入超过 10000 条"]

---

## Clarifications

> 由 `/sdd-clarify` 执行后写入，每次以 Session 日期分组。

### Session YYYY-MM-DD

- Q: [问题] → A: [答案]
- Q: [问题] → A: [答案]
```

---

## contracts.md 模板

> **填写时机（强制）**：每个 Feature 在 **Step 1.3a** 生成 `specs/modules/<feature>/contracts.md`，必须先于 `backend-spec.md` 和 `frontend-spec.md`。
> 填写时必须参考 `skills/backend/references/core/` 对应文件，不得凭记忆填写注解参数或字段类型。
> backend-spec 的字段注解、服务签名、auth 值必须取自本文件，不得自行发明。

```markdown
# [功能名称] API 级契约

## 模型属性契约

参考 `skills/backend/references/core/model.md`、`references/core/model-property-advanced.md`

### [ModelName]（`[model_name]`）

| 字段名 | displayName | dataType | widget | store | 必填 | 前端类型 | 备注 |
|---|---|---|---|---|---|---|---|
| `name` | 名称 | — | — | — | 是 | string | — |
| `status` | 状态 | — | radio-group | — | 是 | string | @Selection 枚举值见下方 |
| `[mainId]` | [关联对象名] | — | — | — | 是 | number | ManyToOne 外键，存 ID |
| `[mainName]` | [关联展示名] | — | — | — | 否 | string | related 冗余存库 |
| `[mainCode]` | [关联编码名] | — | — | false | 否 | string | related 不存库（store=false） |

> - `dataType`：仅在平台默认推断不够时填写（如 `text`/`date`/`datetime`/`integer`/`float`）；无特殊需求填 `—`
> - `widget`：仅在需要非默认组件时填写（如 `radio-group`/`checkbox-group`）；默认输入框填 `—`
> - `store`：related 字段有两种选择——冗余存库（不填，默认 true）或不存库（填 `false`）；按业务决策填写

#### ER 关系声明

参考 `skills/backend/references/core/model.md` §ER 关系注解

| 字段名 | 注解 | 关联模型（Java 类名） | 说明 |
|---|---|---|---|
| `[mainId]` | `@ManyToOne` | `[MainModel]` | 存 ID，外键字段 |
| `[subList]` | `@OneToMany` | `[SubModel]` | 同 App 子表，平台自动推断；跨 App 加 `targetModel`/`targetProperty` |

### [ModelName2]（`[model_name2]`）

（多模型时复制上方模型块，按依赖顺序排列）

## 服务签名契约

参考 `skills/backend/references/core/method-service.md`

### [ModelName]（`[model_name]`）

| 服务名 | 类型 | 参数名: Java 类型 | 返回类型 | 权限码 |
|---|---|---|---|---|
| `search` | 内置 | 平台标准 | `RecordSet` | `read` |
| `create` | 内置 | 平台标准 | `RecordSet` | `create` |
| `update` | 内置 | 平台标准 | `RecordSet` | `update` |
| `delete` | 内置 | 平台标准 | `void` | `delete` |
| `[serviceName]` | 自定义 | `[paramName]: [JavaType]` | `[returnType]` | `[model_name]:[action]` |

> - 自定义服务多参数换行列出；`RecordSet rs` 是平台注入参数，不写入此表
> - 权限码格式 `{model_name}:{action}`，必须与 `integration-map.md` 权限码总览保持一致

## 视图 key / 菜单 key

参考 `skills/backend/references/core/view.md`、`references/core/menu.md`

| 模型 | 视图 key | 视图类型 | 菜单 key | 菜单 parent_ids |
|---|---|---|---|---|
| `[model_name]` | `[model_name]_view` | grid / search / form | `[menu_key]` | `["[parent_menu_key]"]` |

## app.json 条目

参考 `skills/backend/references/core/app-json.md`

```json
{
  "resolved": "[com.sie.iidp.demo.appName]",
  "view": [
    "[moduleName]/views/[model_name]_view.json"
  ],
  "data": [
    "data/menus.json",
    "data/[model_name]_data.json"
  ]
}
```

> `view` 路径相对于 `resolved` 包目录，按业务模块子目录组织，如 `"classmgr/views/example_class_view.json"`（参考 `app-json.md`）。
```
