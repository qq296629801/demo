# IIDP 存量项目接入

## 何时使用存量接入流程

以下场景应使用本指南的存量接入流程，而非全新项目流程：

| 场景 | 说明 |
|---|---|
| 为现有 IIDP 项目新增 App 或功能 | 项目已有 apps.json、多个 App 在生产运行，只需增量扩展 |
| 对遗留 App 模块做现代化改造 | 现有 App 使用非标准视图、直接操作 DOM 或引入了不符合 IIDP 规范的依赖 |
| 将现有项目纳入 SDD 规格管理 | 项目已运行但无 specs/ 目录，需要补充规格文档和能力地图 |
| 跨 App 集成或整合 | 需要打通两个及以上现有 App 的模型、服务或视图 |
| 接手他人遗留 IIDP 工程 | 缺少文档，需要先做侦察和业务规则提取再动手修改 |
| 技术债专项治理 | 已知存在权限漏洞、无规格的自定义扩展或大量标准偏离，需要系统性梳理 |

如果是全新 IIDP 工程（尚无任何 App），使用标准 SDD 新建流程。

---

## 与全新工程的核心差异

| 阶段 | 全新工程流程 | 存量接入流程 |
|---|---|---|
| 步骤 1 | 初始化父工程，克隆仓库并通过首包验证 | 不新建父工程，先侦察现有工程结构，运行 `/sdd-brownfield-init` |
| 步骤 2 | 宪法从团队目标、父工程模板和协商约定中建立 | 宪法从现有代码、POM、App 配置、模型、视图、菜单和数据中提取，再由团队补全 |
| 步骤 3 | 从空白功能开始写 requirements 和 contracts | 与全新工程相同，但 contracts 必须遵循已提取的命名、版本和 App 边界 |
| 步骤 4 | 生成 backend-spec / frontend-spec / interaction-spec | 与全新工程相同，但规格书不得偏离存量项目的强制规范 |
| 步骤 5 | 拆解任务并实施 | 与全新工程步骤 5-6 相同，但任务必须包含兼容性、迁移风险和生产依赖保护 |

存量接入的关键原则是：**先尊重运行中的系统，再把它纳入 SDD 管理**。任何新增模型、视图、菜单、服务或前端扩展，都必须先读取 `specs/iidp-stack.md` 中由 `/sdd-brownfield-init` 提取出的项目类型、版本约束、命名规范和现有 App 清单。

---

## 五步存量接入工作流

```text
步骤 1：侦察与宪法初始化
  → 运行 /sdd-brownfield-init
  → 分析现有 POM、@Model、views/*.json、menus.json、app.json、apps/apps.json
  → 产出 specs/iidp-stack.md（含真实版本号、命名规则、现有 App 清单）

步骤 2：补全项目宪法
  → 产出/完善 specs/mission.md / specs/roadmap.md / specs/integration-map.md

步骤 3：编写需求规格与契约
  → 与 greenfield 步骤 3 相同
  → 产出 requirements.md / contracts.md

步骤 4：生成规格书
  → 与 greenfield 步骤 4 相同
  → 产出 backend-spec.md / frontend-spec.md / interaction-spec.md

步骤 5：拆解任务 → 执行实施
  → 对应 greenfield 步骤 5-6
  → 产出 tasks.md / validation.md，并按任务清单实施
```

---

## 步骤详情

### 步骤 1：侦察与宪法初始化

**目的**：在不改动现有业务代码的前提下，识别项目真实运行约束，将版本号、命名规则、App 清单和现有集成边界写入 `specs/iidp-stack.md`，作为后续所有规格生成的强制基线。

**执行**：运行 `/sdd-brownfield-init`。该命令先调用 `/sdd-init-templates iidp-stack` 生成项目级模板，再通过 `/code-index` 和 CodeGraph 分析现有工程：
- POM 和 package.json：提取 Java、Spring Boot、IIDP 引擎、SDK、Maven 插件、前端框架版本。
- Java 模型：提取 `@Model.name`、`@Model.tableName`、包名、模块名和 `@MethodService` 设计规律。
- JSON 配置：提取 `views/*.json`、`menus.json`、`data/*.json`、字典种子数据和 `app.json` 的命名/引用模式。
- App 注册：读取 `apps/apps.json`，形成现有 App 清单和依赖边界。

**步骤 1 产出摘要**：

```markdown
## 存量工程侦察完成 ✓

### 已生成文件
- `specs/templates/iidp-stack.md`
- `specs/iidp-stack.md`

### 提取结果摘要
- IIDP 引擎版本：[版本]  SDK 版本：[版本]
- Spring Boot：[版本] / Java：[版本]
- 模型 name 规则：[规则]  表名规则：[规则]
- 视图 key 规则：[规则]  菜单 key 规则：[规则]
- 发现 App 数：[N] 个

### 待确认项
- [无法自动推导的项，标注原因]
```

**后续选项**：

```text
A) 进入步骤 2，补全 mission.md / roadmap.md / integration-map.md
B) 手动修正 specs/templates/iidp-stack.md 中的待确认项，再重新生成 specs/iidp-stack.md
C) 暂停，先处理阻塞项（如项目根目录不正确、CodeGraph 未初始化、POM 结构异常）
```

---

### 步骤 2：补全项目宪法

**目的**：在已提取的技术栈和命名规范之上，补齐项目使命、路线图、App 职责、跨 App 依赖和技术债治理边界，使存量系统具备可持续演进的 SDD 基础。

**执行**：读取 `references/sdd-constitution.md`，优先使用 `specs/templates/` 中的项目级模板，产出或完善：
- `specs/mission.md`：系统定位、目标用户、核心业务价值、明确不做事项。
- `specs/roadmap.md`：Phase 规划、技术债治理节奏、功能优先级。
- `specs/integration-map.md`：现有 App 清单、App 职责、依赖顺序、跨模型服务、权限码总览。
- `specs/decisions.md`：记录存量规则无法自动推导、团队人工确认或需要兼容历史写法的关键决策。

`specs/iidp-stack.md` 中 `项目类型: 存量接入（brownfield）`、版本约束、命名规范和现有 App 清单不得在本步骤中被随意改写；如需修正，必须标注证据来源或在 `decisions.md` 记录人工确认原因。

**步骤 2 产出摘要**：

```markdown
## 存量项目宪法已补全 ✓

### 已确认内容
- 项目使命：[简述]
- 当前 App：[App 数] 个，核心职责：[简述]
- 关键依赖：[AppA] → [AppB] → ...
- 技术债主题：[数量] 个

### 产出文件
- `specs/mission.md`
- `specs/roadmap.md`
- `specs/integration-map.md`
- `specs/decisions.md`（如有关键人工决策）

### 待确认项
- [业务目标、职责边界或依赖关系中仍需确认的事项]
```

**后续选项**：

```text
A) 进入步骤 3，为第一个存量改造或新增功能编写 requirements.md / contracts.md
B) 继续梳理 App 职责和跨 App 依赖，先完善 integration-map.md
C) 先建立技术债治理 Phase，再进入具体功能规格
```

---

### 步骤 3：编写需求规格与契约

**目的**：把本次存量改造、新增 App、跨 App 集成或技术债治理事项转换为可验收的需求规格，并在生成技术规格前锁定模型、视图、服务、权限和前端参数契约。

**执行**：读取 `references/sdd-workflow.md` Step 1.1-1.3a，产出：
- `specs/features/phase{N}-{feature}/requirements.md`
- `specs/features/phase{N}-{feature}/contracts.md`

执行时必须先读取 `specs/iidp-stack.md` 和 `specs/integration-map.md`：
- 新增模型、视图、菜单、数据文件、模块名必须遵循已提取命名规范。
- 涉及现有 App 时，contracts.md 必须写明复用、扩展或兼容的对象。
- 跨 App 调用必须在 integration-map.md 中已有依赖边界，或在本步骤补充后再继续。
- 对已有生产行为的改动必须写出兼容策略和回滚边界。

**步骤 3 产出摘要**：

```markdown
## 存量需求规格与契约已生成 ✓

### 功能范围
- 类型：新增 App / 新增功能 / 遗留改造 / 跨 App 集成 / 技术债治理
- 涉及 App：[App 列表]
- 涉及模型：[模型列表]

### 契约关键条目
| appName | model_name | service | auth | 前端 args | 兼容说明 |
|---|---|---|---|---|---|
| [值] | [值] | [值] | [值] | [值] | [值] |

### 待确认项
- [节点 id / 权限码 / 依赖 App / 历史数据兼容项]
```

**后续选项**：

```text
A) 进入步骤 4，生成 backend-spec / frontend-spec
B) 对待确认项执行 Clarify
C) 回到步骤 2 更新 integration-map.md 或 decisions.md
```

---

### 步骤 4：生成规格书

**目的**：将 requirements.md 和 contracts.md 转换成可执行的后端、前端和交互规格，同时保持与存量代码风格、版本能力和 App 边界一致。

**执行**：
- 后端规格：读取 `references/sdd-backend.md`，产出 `backend-spec.md`。
- 前端规格：读取 `references/sdd-frontend.md`，判断是否需要标准模板、hook、扩展视图或自定义 Vue2 组件，必要时产出 `frontend-spec.md`。
- 交互规格：含复杂状态机、响应式、可访问性或跨页面流程时，读取 `references/sdd-frontend-interaction.md`，产出 `interaction-spec.md`。

规格书必须以 `contracts.md` 为准，不得重新发明 service、auth、视图 key、菜单 key、args 或节点 id。遇到旧代码与规范不一致时，规格中标注“兼容现状”或“治理目标”，并在 tasks.md 中拆出迁移任务。

**步骤 4 产出摘要**：

```markdown
## 存量规格书已生成 ✓

### 后端规格概览
- 新增模型：[数量] 个
- 复用/改造模型：[数量] 个
- 服务：[数量] 个（复用 X，新增 X，重写 X）
- 视图/菜单/数据配置：[数量] 项

### 前端规格概览
- 实现分支：标准模板 / hook / 扩展视图 / 自定义 Vue2 组件
- 受影响页面：[列表]
- 兼容约束：[旧节点 id / 旧路由 / 旧数据源]

### 待确认项
- [规格书中仍标记待确认的事项]
```

**后续选项**：

```text
A) 进入步骤 5，拆解任务并实施
B) 先执行规格一致性审查，检查 contracts.md 与 backend-spec/frontend-spec 是否漂移
C) 回到步骤 3 调整契约
```

---

### 步骤 5：拆解任务 → 执行实施

**目的**：将规格书拆解为可逐项实施和验收的任务，按任务清单完成后端代码、视图、菜单、数据、前端扩展和集成验证。

**执行**：
- 拆解任务：读取 `references/sdd-workflow.md` Step 3，产出 `tasks.md` 和 `validation.md`。
- 执行实施：读取 `references/sdd-workflow.md` Step 4，后端按 `skills/backend/SKILL.md`，前端按 `skills/frontend` 对应子 skill。
- 每个任务完成后更新 `tasks.md` 状态，并按 `validation.md` 执行验收。
- 涉及生产存量行为时，任务必须包含兼容验证、历史数据验证或回滚说明。

**Git 分支门控**：进入实施前检查当前分支。若在 `main`、`master` 或 `develop`，暂停并提示用户创建 feature 分支；不得在主干直接修改工程文件。

**步骤 5 产出摘要**：

```markdown
## 存量功能实施完成 ✓

### 任务概览
- 任务总数：X 个（后端 X，前端 X，配置 X，验证 X）
- 已完成：X 个
- 兼容性验证：通过 / 失败（原因：）
- 受影响 App：[App 列表]

### 验收结果
- TC-BE：X/X 通过
- TC-FE：X/X 通过
- 集成验证：通过 / 失败

### 后续处理
- roadmap.md 更新：[是/否]
- integration-map.md 更新：[是/否]
- decisions.md 更新：[是/否]
```

**后续选项**：

```text
A) 进入 PR / Review Gate
B) 继续处理下一个存量功能或 App
C) 回到步骤 2，更新技术债治理路线图
```

---

## 多 App 存量规划

存量项目通常已经存在多个 App，规划重点不是“从零设计 App 清单”，而是识别真实依赖并避免破坏生产边界。

**在 `integration-map.md` 中维护现有 App 清单**：

```markdown
## 现有 App 清单

| appName | appPkg / resolved 包 | 职责 | 依赖 App | 状态 | 备注 |
|---|---|---|---|---|---|
| [App1] | [包路径] | [职责] | 无 | 生产运行 | [说明] |
| [App2] | [包路径] | [职责] | [App1] | 生产运行 | [说明] |
| [App3] | [包路径] | [职责] | [App1], [App2] | 待治理 | [说明] |
```

新增 App 或改造 App 时，必须先确认：
- 是否复用现有模型或服务。
- 是否新增 `app.json` dependency 或 `apps/apps.json` 注册项。
- 是否影响已有菜单入口、权限码、路由、数据源或字典。
- 是否需要迁移历史数据或保持旧 key 兼容。

---

## 产物清单

```text
specs/
├── mission.md                          ← 步骤 2
├── iidp-stack.md                       ← 步骤 1 生成，步骤 2 可人工确认待确认项
├── roadmap.md                          ← 步骤 2
├── integration-map.md                  ← 步骤 2 起维护现有 App / 模型 / 权限 / 依赖
├── decisions.md                        ← 步骤 2 起记录存量兼容和治理决策
├── templates/
│   └── iidp-stack.md                   ← 步骤 1 生成并填充的项目级模板
└── features/
    └── phase1-{feature}/
        ├── requirements.md             ← 步骤 3
        ├── contracts.md                ← 步骤 3
        ├── backend-spec.md             ← 步骤 4
        ├── frontend-spec.md            ← 步骤 4（需要前端代码时）
        ├── interaction-spec.md         ← 步骤 4（含复杂状态/响应式时）
        ├── tasks.md                    ← 步骤 5
        └── validation.md               ← 步骤 5
```

---

## 常见存量场景

### 场景 1：接手他人遗留项目，无规格文档

适用深度：完整五步。

关键注意：
- 先运行 `/sdd-brownfield-init`，不要直接补写 backend-spec。
- 步骤 2 优先补 `mission.md` 和 `integration-map.md`，明确每个 App 的真实职责。
- 对无法解释的历史写法，不要立即改动；先写入 `decisions.md` 或技术债条目。

### 场景 2：为已有 IIDP 项目新增 App

适用深度：完整五步，但步骤 2 可聚焦 App 清单和依赖边界。

关键注意：
- 新 App 的 appName、Maven 模块名、appPkg、模型 name、视图 key 必须遵循 `iidp-stack.md` 中提取出的现有规则。
- 新 App 依赖已有 App 时，先在 `integration-map.md` 写清依赖和复用契约。
- 新 jar 和 App 注册必须同步到 `apps/apps.json`。

### 场景 3：将遗留 App 纳入 SDD 管理（技术债治理）

适用深度：步骤 1-2 必做，步骤 3-5 按治理批次执行。

关键注意：
- 不以“重写”为默认策略，先把现有模型、视图、菜单、服务和前端扩展纳入规格。
- 技术债按风险排序，写入 `roadmap.md` 的技术债表。
- 每个治理批次都要有独立 requirements.md、contracts.md、tasks.md 和 validation.md。

### 场景 4：跨 App 集成打通

适用深度：完整五步，且步骤 2-3 需要更细的 integration-map 和 contracts。

关键注意：
- 被依赖 App 的模型、服务、权限码必须作为契约来源，不在调用方重复定义。
- 跨 App 服务要写明事务边界、失败回滚、权限校验和数据所有权。
- 已在生产运行的依赖关系必须在规格中描述为“现状依赖”，新增依赖描述为“本次变更”。

---

## 常见问题排查

| 问题 | 可能原因 | 排查方式 |
|---|---|---|
| `/sdd-brownfield-init` 提示不是 IIDP 项目根 | 当前目录缺少 `pom.xml` 和 `apps/apps.json`，或在子模块目录执行 | `cd` 到聚合 POM 或包含 `apps/apps.json` 的项目根后重试 |
| CodeGraph 无索引或搜索不到 XML/JSON 事实 | 项目未初始化 CodeGraph，或 XML/JSON 不会被符号索引完整覆盖 | 执行 `codegraph init -i`；若 `codegraph_search` 无结果，直接读取 `pom.xml`、`package.json`、`app.json`、`menus.json`、`views/*.json` |
| 现有模型 `name` / `tableName` 不规范 | 历史 App 使用过多套命名规则，或早期代码未遵循当前规范 | `iidp-stack.md` 中记录“主流规则”和“例外样本”；新增代码遵循主流规则，修复例外需单独建技术债任务 |
| 现有视图 key 与宪法提取结果不一致 | 同一模型存在历史 view key、复制改造遗留 key 或多模块前缀差异 | 以生产正在引用的 key 为兼容基线；新视图按提取出的主流规则命名，旧 key 迁移写入 tasks.md |
| 菜单 `parent_ids` 引用无法解析 | 菜单文件拆分、根菜单在其他 App，或使用 `@ref` 间接引用 | 读取所有 `menus.json` 和 `apps/apps.json`，在 `integration-map.md` 中写明跨 App 菜单依赖 |
| 跨 App 依赖已在生产运行，但规格中没有描述 | 现状依赖是历史代码自然形成，未进入文档 | 在 `integration-map.md` 标为“现状依赖”，contracts.md 中描述调用方、被调用方、服务、权限和失败行为 |
| 版本号无法自动推导 | POM 使用父级继承、私服 BOM、profile 或变量间接声明 | 保留 `待确认`，在 `iidp-stack.md` 注明来源不明；人工确认后写入 `decisions.md` |
| 前端 package.json 不在项目根 | 前端工程在 `apps/`、`frontend/` 或独立扩展应用目录 | 通过 `createApp`、`defineComponent` 或扩展应用目录定位源文件，再向上查找最近的 package.json |
