# create-project 使用说明

## 适用范围

`create-project` 用于把 IIDP 项目想法、业务需求或存量系统改造诉求转成可执行的 SDD 规格资料。它不负责直接替代 `skills/backend` 或 `skills/frontend` 写代码，而是先固定项目宪法、功能规格、前后端契约、计划、任务和验收标准。

适合使用本 skill 的场景：

- 从零规划一个 IIDP 业务 App 或模块。
- 把业务需求整理为 `requirements.md`、`plan.md`、`tasks.md`、`validation.md`。
- 为后端模型、视图、菜单、服务、权限写规格。
- 为前端标准模板、在线视图、hook、扩展视图、自定义 Vue2 组件写规格。
- 根据任意支持 MCP 的原型/设计/产品工具服务、截图或文字描述补齐 UI 原型说明。
- 为前后端对齐 JSON-RPC、Filter、按钮服务、菜单、权限、节点 id 和数据源契约。
- 接入存量 IIDP 项目，只为变更区域补规格。
- 阶段完成后做复盘，更新 roadmap、integration-map、decisions 和 CHANGELOG。

不适合使用本 skill 的场景：

- 直接生成通用开源框架项目模板。
- 不经过 IIDP 标准模板判断，直接要求写自定义前端页面。
- 在没有事实来源时补全模型名、节点 id、权限码、接口或原型来源。
- 把普通后台管理页面设计成独立前端应用。

## 使用入口

触发本 skill 后，先读取：

```text
skills/create-project/SKILL.md
skills/create-project/references/sdd.md
```

`sdd.md` 是索引，不是完整手册。根据任务类型继续读取专题文件：

| 任务 | 读取文件 |
|---|---|
| 项目宪法、UI 宪法、路线图 | `references/sdd-constitution.md` |
| SDD 流程、需求、计划、任务 | `references/sdd-workflow.md` |
| 后端规格 | `references/sdd-backend.md` + `skills/backend/SKILL.md` |
| 前端规格、UI 原型、节点树 | `references/sdd-frontend.md` + `skills/frontend/SKILL.md` |
| 前端交互设计规格 | `references/sdd-frontend-interaction.md` + `skills/frontend/SKILL.md` |
| 前后端契约 | `references/sdd-contracts.md` |
| 验证、失败处理、复盘 | `references/sdd-validation.md` |
| 存量项目接入 | `references/sdd-brownfield.md` |
| skill 自身维护 | `references/sdd-skill-maintenance.md` |
| Prompt、工具、原则 | `references/sdd-prompts-tools-principles.md` |

## create-project 目录说明

`skills/create-project` 自身是一个 Codex skill，不是业务项目源码。它的目录职责如下：

```text
skills/create-project/
├── SKILL.md                         # skill 入口：触发条件、核心流程、按需加载、验证门禁
├── agents/
│   └── openai.yaml                  # Codex UI 展示信息
├── references/
│   ├── sdd.md                       # SDD 手册索引，不放长模板
│   ├── sdd-constitution.md          # 项目宪法、UI 宪法、路线图、集成总览模板
│   ├── sdd-workflow.md              # SDD 分层、功能迭代闭环、计划、任务、验收目录
│   ├── sdd-backend.md               # IIDP 后端规格模板
│   ├── sdd-frontend.md              # IIDP 前端、UI 原型、节点树、交互规格模板
│   ├── sdd-frontend-interaction.md  # 前端交互设计规格
│   ├── sdd-contracts.md             # JSON-RPC、Filter、按钮服务、节点属性与事件契约
│   ├── sdd-validation.md            # 验证、失败分类、阶段复盘、变更记录
│   ├── sdd-brownfield.md            # 存量 IIDP 项目接入
│   ├── usage.md                     # create-project 详细使用说明
│   ├── sdd-skill-maintenance.md     # 本 skill 的维护规则和校验命令
│   └── sdd-prompts-tools-principles.md # Prompt、工具命令、核心原则、常见陷阱
├── github_spec_kit_architecture.svg # 历史参考图，后续建议迁入 assets/ 或删除
└── openspec_architecture.svg        # 历史参考图，后续建议迁入 assets/ 或删除
```

## 推荐输入

用户提出需求时，尽量提供以下信息。没有提供时，未知项必须写为“待确认”。

| 信息 | 示例 |
|---|---|
| 业务目标 | “管理客户合同，支持合同创建、审批、归档” |
| 用户角色 | 管理员、业务员、审批人 |
| 业务对象 | 合同、客户、审批记录、附件 |
| 字段 | 合同编号、客户、金额、生效日期、状态 |
| 流程 | 新建合同 → 提交审批 → 审批通过 → 归档 |
| 权限 | 谁能新建、审批、删除、导出 |
| 页面形态 | 搜索 + 表格 + 表单、主表单 + 子表、树表、弹窗 |
| 原型来源 | 任意支持 MCP 的原型/设计/产品工具服务名称、资源 URI、页面/Page、Frame |
| 后端命名 | appName、appPkg、moduleName、model_name |
| 前端目标 | 工程名、`apps/<appName>`、页面或节点 id |

## 标准输出

`create-project` 的输出分两层：规格文档结构和完整项目结构。规格结构只描述 `specs/` 如何组织；完整项目结构还包括 IIDP 后端工程、前端工程、skills、维护记录和项目说明。

### 项目级文档

```text
specs/
├── mission.md
├── iidp-stack.md
├── ui-constitution.md
├── roadmap.md
├── integration-map.md
├── decisions.md
└── legacy/
    └── [module]-business-rules.md
```

`specs/legacy/` 用于 Legacy 代码分析（存量项目），保存代码侦察、业务规则提取和变更区域规格依据。

### 功能级文档

```text
specs/features/
└── phase1-[feature]/
    ├── requirements.md
    ├── plan.md
    ├── tasks.md
    └── validation.md
```

### 维护记录

```text
backlog/
└── ideas.md

CHANGELOG.md
```

`backlog/ideas.md` 记录待研究想法、候选需求、未排期能力和产品探索。只有被确认进入路线图后，才移动或拆分到 `specs/features/<feature>/`。

### IIDP 完整项目结构

```text
agents/
├── AGENTS.md
├── specs/
│   ├── mission.md
│   ├── iidp-stack.md
│   ├── ui-constitution.md
│   ├── roadmap.md
│   ├── integration-map.md
│   ├── decisions.md
│   ├── features/
│   │   └── phase1-[feature]/
│   │       ├── requirements.md
│   │       ├── plan.md
│   │       ├── tasks.md
│   │       └── validation.md
│   └── legacy/                     # Legacy 代码分析（存量项目）
│       └── [module]-business-rules.md
├── skills/
│   ├── backend/
│   ├── frontend/
│   └── create-project/
│       ├── SKILL.md
│       ├── agents/
│       │   └── openai.yaml
│       └── references/
│           ├── sdd.md
│           ├── sdd-constitution.md
│           ├── sdd-workflow.md
│           ├── sdd-backend.md
│           ├── sdd-frontend.md
│           ├── sdd-frontend-interaction.md
│           ├── sdd-contracts.md
│           ├── sdd-validation.md
│           ├── sdd-brownfield.md
│           ├── usage.md
│           ├── sdd-skill-maintenance.md
│           └── sdd-prompts-tools-principles.md
├── iidp-backend-demo-ai/
│   ├── pom.xml
│   ├── settings.xml
│   ├── apps/
│   ├── docker-compose.yml
│   ├── docker/
│   ├── sie-iidp-demo-apps/
│   ├── sie-iidp-demo-common/
│   └── sie-iidp-demo-start/
├── [frontend-project]/
│   ├── package.json
│   ├── config/
│   └── apps/
│       └── <appName>/
│           ├── config/
│           ├── common/
│           ├── views/
│           └── resource/
├── backlog/
│   └── ideas.md
├── CHANGELOG.md
└── README.md
```

说明：`README.md` 是业务项目根目录的可选说明文件；不要在 `skills/create-project/` skill 目录内新增 `README.md`。

## 基本流程

1. 读取 `SKILL.md` 和 `references/sdd.md` 索引。
2. 根据用户目标读取最小必要专题文件。
3. 识别后端能力域和前端实现分支。
4. 固定项目或功能规格。
5. 明确前后端契约，包括模型、服务、视图、菜单、权限、节点 id、数据源和事件。
6. 生成计划和任务，每个任务落到文件或可验证动作。
7. 执行或交付前运行质量检查。
8. 阶段完成后更新复盘和变更记录。

## 常用 Prompt

### 创建项目宪法

```text
使用 create-project，为 [项目描述] 创建 IIDP 项目宪法。
请生成 mission.md、iidp-stack.md、ui-constitution.md、roadmap.md、integration-map.md、decisions.md。
先读取 skills/backend 和 skills/frontend，不要套用通用开源框架。
```

### 生成后端规格

```text
使用 create-project，为 [功能名] 生成 IIDP 后端规格。
包含 appName、appPkg、moduleName、model_name、Java 模型、服务、视图、菜单、数据、权限和验收。
未知命名和权限码写待确认。
```

### 生成前端规格

```text
使用 create-project，为 [页面名] 生成 IIDP 前端规格。
先判断标准模板/在线视图是否满足；如需扩展，列出页面节点树、selector、节点 id 来源、ds_config、bind_、bind_on_、commands 和 hook。
```

### 补充 UI 原型

```text
使用 create-project 补全 UI 原型规格。
请先提示我提供任意支持 MCP 的原型/设计/产品工具服务名称、资源 URI、页面或 Frame。
读取原型后，输出页面骨架、用户流程、状态清单、原型到 IIDP 的映射，以及需要产品确认的差异。
```

### 生成前后端契约

```text
使用 create-project，为 [功能名] 生成前后端契约表。
包含 model、service、args、Filter、view key、menu key、auth、前端实现方式、节点 id、数据源和待确认事项。
```

### 存量项目接入

```text
使用 create-project 分析当前 IIDP 存量项目。
只做侦察，不修改代码。输出工程结构、app.json、views、menus、模型、服务、权限、前端 apps 配置、hook、扩展视图、数据源和风险。
```

### 阶段复盘

```text
Phase [N] 已完成。使用 create-project 做复盘。
请更新 roadmap.md、integration-map.md、decisions.md、CHANGELOG.md，并列出技术债、验证缺口和待确认事项。
```

## UI 原型 MCP 使用规则

需要 UI 原型描述时，必须先向用户索取：

- MCP 服务名称。该服务可以来自任意原型、设计或产品工具，只要该产品支持 MCP 并能暴露原型资源即可。
- MCP 资源 URI。
- 页面/Page。
- Frame/画板。
- 原型版本或更新时间。

能读取 MCP 资源时，用 MCP 内容提取页面骨架、区域布局、用户流程、状态、弹窗、抽屉、空态和异常态。不能读取时，可以使用截图、导出文档或文字描述兜底，但必须标记“原型来源：待确认”。不要把规则写死到某个具体产品。

原型不等于自定义前端代码。落地顺序始终是：

```text
标准模板/在线视图 → hook → 扩展视图 → 自定义 Vue2 组件
```

## 质量检查

### Skill 自身检查

修改 `skills/create-project` 后运行：

```bash
python3 /Users/mjy/.codex/skills/.system/skill-creator/scripts/quick_validate.py skills/create-project
find skills/create-project -name '*.md' -print0 | xargs -0 -n1 awk 'BEGIN{c=0} /^```/{c++} END{if (c % 2 == 0) exit 0; print FILENAME ": unbalanced " c; exit 1}'
awk '/[ \t]$/{print FILENAME ":" FNR ":" $0; bad=1} END{exit bad}' skills/create-project/SKILL.md skills/create-project/references/*.md
git diff --check -- skills/create-project
```

### SDD 文档检查

- `references/sdd.md` 只做索引，详细内容在 `references/sdd-*.md`。
- Markdown 没有未处理模板占位符，除非明确标记为“待确认”。
- UI 原型有 MCP、截图、导出文档或文字来源。
- 前端规格包含 UI 宪法、页面节点树、selector、节点 id 来源、数据源、绑定、事件和 commands。
- 重要阶段完成后同步 `roadmap.md`、`integration-map.md`、`decisions.md`、`CHANGELOG.md`。

### 后端检查

- POM、`app.json`、视图、菜单、数据、附件、`apps/apps.json` 登记完整。
- `@Model(name)` 与视图、菜单、服务一致。
- 自定义服务校验状态、权限、作用域和必填参数。
- 查询服务支持 Filter、分页、排序和字段选择。

### 前端检查

- 传统管理后台没有绕过标准模板硬写自定义页面。
- 扩展应用由 `tech app` 创建。
- 目标节点 id 来源明确。
- hook 路径、扩展类型、数据源名、commands 名称清晰。
- 未修改 `node_modules`、`dist`、`distApp`、`distTmp`、`umdComps`、`build`。

## 失败处理

验证失败时先分类，再修复：

| 分类 | 处理方式 |
|---|---|
| 规格问题 | 先修改规格或提出 ADR，确认后再改实现 |
| 实现问题 | 修正最小必要实现，并重新运行失败项 |
| 环境问题 | 记录环境事实、阻塞项和可替代静态验证 |
| 平台能力限制 | 标记限制，给出扩展视图或自定义组件方案 |
| 原型输入不足 | 请求补充任意支持 MCP 的原型工具服务或把来源标记为待确认 |

## 常见误用

| 误用 | 正确做法 |
|---|---|
| 把后台管理页默认写成自定义 Vue 页面 | 先判断标准模板/在线视图是否满足 |
| 按按钮文案猜节点 id | 使用用户提供、标准模板规则库或标记待确认 |
| 只隐藏前端按钮控制权限 | 后端服务必须二次校验 |
| UI 原型没有来源 | 先请求 MCP、截图、导出文档或文字描述 |
| SDD 文档只写需求不写契约 | 必须写模型、服务、视图、菜单、权限、节点和数据源 |
| 任务清单无法验证 | 每个任务落到文件或可执行检查 |

## 维护建议

- 使用说明类内容放在 `references/usage.md`，不要新增 `README.md`。
- 长模板继续按专题拆分，避免 `SKILL.md` 或 `sdd.md` 变成大文件。
- 新增 reference 后，必须同时更新 `SKILL.md` 的按需加载表和 `references/sdd.md` 索引。
- 若本说明与 `skills/backend` 或 `skills/frontend` 冲突，以对应 skill 和当前工程实际代码为准。
