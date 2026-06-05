---
name: iidp-brownfield
description: |
  Use when 用户要在已有 IIDP 存量项目中新增 App、改造已有 App、增加功能、做跨 App 集成、
  或把存量 IIDP 项目纳入 SDD 流程。该 skill 是编排入口：先用 code-index 生成
  baseline-spec，再复用 create-project 的 SDD 命令完成 brownfield 初始化、需求规格、
  契约、技术规格、计划、任务、实现和差异回校验。
---

# IIDP Brownfield

本 skill 是 IIDP 存量项目的 SDD 编排入口。它不重新实现 SDD，也不直接替代 `code-index`、`create-project`、`backend` 或 `frontend`；它负责把这些能力按正确顺序串起来。

核心链路：

```text
用户需求
  -> 定位 IIDP 存量项目
  -> code-index 生成 baseline-spec
  -> create-project /sdd-brownfield-init
  -> /sdd-specify
  -> delta-spec + diff
  -> /sdd-contracts
  -> /sdd-spec
  -> /sdd-plan
  -> /sdd-tasks
  -> /sdd-implement
  -> /sdd-sync
  -> 重新 code-index 做 final diff
```

## 触发场景

当用户表达以下意图时使用本 skill：

- “在 IIDP 存量项目里新增一个 App”。
- “给已有 IIDP App 增加功能”。
- “把这个 IIDP 老项目走 SDD 流程”。
- “先 code-index，再 SDD，再生成任务/代码”。
- “基于存量代码和新需求比较差异，再做增量开发”。
- 用户用 `/iidp-brownfield ...`、`@skills/iidp-brownfield/SKILL.md ...` 或类似入口发起。

不要在以下场景使用：

- 用户只是想理解代码或生成 Codebook：使用 `skills/code-index/SKILL.md`。
- 用户要从零创建 IIDP 父工程：使用 `skills/create-project` 的 greenfield 流程。
- 用户已有完整 `backend-spec.md` / `frontend-spec.md`，只要求实现某个任务：直接走 `/sdd-implement` 或 backend/frontend skill。

## 用户输入

最少输入：

```text
项目路径：<IIDP 项目根，可省略；省略时用当前目录>
需求：<本次新增/修改内容>
```

推荐输入：

```text
项目路径：
变更类型：新增 App / 修改已有 App / 新增功能 / 跨 App 集成 / 技术债治理
目标 appName：
新 appName：
需求文档路径：
是否只生成 SDD 产物：是/否
是否继续实现代码：是/否
```

示例：

```text
@skills/iidp-brownfield/SKILL.md
项目路径：E:\demo\iidp-project
新增一个 order app
需求是：支持订单创建、订单明细、订单状态流转、订单查询。
```

## 工作原则

- SDD 是主流程，`code-index` 只负责提供存量上下文。
- `baseline-spec` 表示当前代码事实，默认只读。
- 开发者需求先转成 `delta-spec`，再与 baseline 合成 `target-spec`。
- 任何计划、任务和代码实现都必须能回连 `target-spec`。
- 不全量覆盖存量 App，不修改无关模块。
- 不编造 appName、model、service、selector、权限码、节点 id 或运行时参数。
- 低置信度、示例占位和运行时无法确认项必须进入 `unresolved` 或 `待确认`。

## 内部流程

### Step 0：定位项目

如果用户提供项目路径，验证该路径。若未提供，使用当前目录。

确认 IIDP 信号：

- 后端：`pom.xml`、`apps/apps.json`、`**/app.json`、`**/views/menus.json`、`@Model`。
- 前端：`package.json`、`config/apps.json`、`apps/<app>/index.js`、`apps/<app>/views/index.js`。

如果当前目录不是 IIDP 项目根，或发现多个候选项目，先让用户确认项目路径。

产物建议：

```text
specs/brownfield/project-context.md
```

### Step 1：运行 code-index

调用或按 `skills/code-index/SKILL.md` 执行：

```text
分析 <项目路径> 生成规格书
```

必须确认存在：

```text
codebook/baseline-spec/
```

若不存在，说明 code-index 未按 IIDP baseline-spec 流程完成，停止进入 SDD。

若已存在但可能过期：

- 代码文件修改时间晚于 baseline 生成时间时，建议重跑 code-index。
- 用户坚持复用旧 baseline 时，在后续 `diff.md` 标注风险。

### Step 2：导入 SDD brownfield 上下文

复用 `skills/create-project`。

读取：

- `skills/create-project/SKILL.md`
- `skills/create-project/references/sdd-brownfield.md`
- `skills/create-project/commands/sdd-brownfield-init.md`

执行等价流程：

```text
/sdd-brownfield-init <项目路径>
```

目标产物：

```text
specs/iidp-stack.md
specs/integration-map.md
specs/unresolved.md
specs/baseline/
```

### Step 3：生成 requirements.md

复用：

```text
/sdd-specify <用户需求>
```

产物：

```text
specs/features/<feature>/requirements.md
```

要求：

- requirements 必须引用存量上下文。
- 新增 App 要说明与已有 App 的边界和依赖。
- 修改已有 App 要说明是新增、扩展、替换还是治理。

### Step 4：生成 delta-spec 和 diff

基于 `requirements.md` 和 `codebook/baseline-spec/` 生成：

```text
specs/features/<feature>/delta-spec.md
specs/features/<feature>/diff.md
```

`delta-spec.md` 必须包含：

- 变更类型。
- 目标 app 或新 app。
- 新增项。
- 修改项，必须引用 baseline specId。
- 删除或停用项。
- 待确认项。

`diff.md` 必须比较：

```text
baseline-spec vs delta-spec
```

并输出：

- `added`
- `changed`
- `removed`
- `conflicts`
- `needsConfirmation`
- `risk`

如果存在 app/model/menu/view/service/permission 冲突，停止进入 `/sdd-contracts`，先让用户确认或修正 delta-spec。

### Step 5：生成 target-spec

合成：

```text
baseline-spec + delta-spec -> target-spec
```

产物：

```text
specs/features/<feature>/target-spec.md
```

`target-spec` 是后续 SDD 命令的主要输入。它表示本次开发完成后，系统应该是什么状态。

### Step 6：生成 contracts

复用：

```text
/sdd-contracts
```

产物：

```text
specs/features/<feature>/contracts.md
```

contracts 必须覆盖：

- appName / appPkg / Maven module / frontend app。
- model / table / fields。
- service / args / auth / return。
- menuId / viewId / view type。
- frontend selector / hook / ds_config / request。
- data seed / dict / encoder rule。

### Step 7：生成技术规格

复用：

```text
/sdd-spec
```

产物：

```text
specs/features/<feature>/backend-spec.md
specs/features/<feature>/frontend-spec.md
specs/features/<feature>/interaction-spec.md
```

需要前端交互时，读取：

```text
skills/create-project/references/sdd-frontend-interaction.md
```

### Step 8：生成计划并做计划差异检查

复用：

```text
/sdd-plan
```

产物：

```text
specs/features/<feature>/plan.md
specs/features/<feature>/impact.md
```

比较：

```text
baseline-spec vs target-spec vs plan.md
```

若计划修改了 target-spec 未声明的 App、模型、视图、服务或前端文件，停止进入 `/sdd-tasks`，先修正 plan。

### Step 9：拆任务

复用：

```text
/sdd-tasks
```

产物：

```text
specs/features/<feature>/tasks.md
specs/features/<feature>/validation.md
```

任务必须引用 `target-spec` 或 `contracts.md` 条目。

### Step 10：实现

复用：

```text
/sdd-implement
```

进入实现前必须执行 create-project 的分支检查规则。若当前分支是 `main`、`master` 或 `develop`，暂停并建议用户创建 feature 分支。

#### 10.1 通用规则

- 一次只完成 `tasks.md` 中一个未完成任务。
- 完成后在 `tasks.md` 中勾选该任务。
- 每个任务的实现必须能回连 `target-spec.md` 或 `contracts.md` 中的条目。
- 记录实际改动文件清单，供 Step 11 回校验使用。

#### 10.2 后端实现

- 调用 `skills/backend/SKILL.md`。
- 遵循 backend skill 的完整流程：模型 → 服务 → 控制器 → 权限 → 菜单 → 数据种子。

#### 10.3 前端实现 —— 必须通过 `iidp-frontend` 和 codegen protocol

> **核心约束：生成任何前端代码、文件、配置前，必须先加载并执行 `skills/frontend/SKILL.md` 的路由流程，然后执行 `skills/frontend/references/iidp-frontend-codegen-protocol.md`。不允许仅读取 frontend SKILL.md 后就直接写前端文件。**

**前置步骤：加载 `iidp-frontend` skill**

1. 读取并执行 `skills/frontend/SKILL.md`（skill 名：`iidp-frontend`）的路由流程。
2. `iidp-frontend` skill 加载后，会强制执行 **工程存在性检查**（前置门禁）：
   - 扫描工作区中的 IIDP 前端工程（识别标志：`package.json` 含 `init:tech` 脚本、`apps/` 目录存在、`config/apps.json` 存在、`build/webpack.dev.js` 存在；满足 ≥3 项视为候选）。
   - **0 个候选工程 → 必须停止前端代码生成**：告知用户未发现 IIDP 前端工程，询问是否需要创建工程。在用户明确确认前，禁止创建任何前端文件或目录。
   - **≥1 个候选 → 列出候选路径**让用户确认，确认后继续。
3. 工程确认后，`iidp-frontend` skill 会根据当前 brownfield 上下文自动路由到对应子技能。

**brownfield 场景下的子技能路由：**

| 当前 brownfield 状态                                       | 应路由的子技能                                | 说明                                                                        |
| ---------------------------------------------------------- | --------------------------------------------- | --------------------------------------------------------------------------- |
| Step 7 已生成 `frontend-spec.md`                           | `iidp-frontend-spec-code`                     | 按规格文档驱动代码生成，内部编排工程准备（init）+ 扩展开发（extension-dev） |
| 修改已有 App 的视图/hook/数据源/组件，无独立 frontend-spec | `iidp-frontend-extension-dev`                 | 先执行存量项目定位（发现工程 → 定位 App → 读取现有模式），再进入扩展开发    |
| 需要新建前端应用（新增 App 场景）                          | `iidp-frontend-init`（由 spec-code 内部调用） | 通过脚手架 `tech app <appName>` 创建应用，**禁止手动 mkdir 或复制目录**     |
| 查阅框架机制、组件属性、扩展协议                           | `iidp-frontend-dev-manual`                    | 框架文档事实来源，其他子技能均依赖它                                        |

**统一代码生成协议：**

在写入任何 `apps/<appName>/views`、`apps/<appName>/common`、`apps/component` 或 `apps/<appName>/config` 文件前，必须执行：

```text
skills/frontend/references/iidp-frontend-codegen-protocol.md
```

该协议统一承载实现分支判断、工程门禁、selector 来源、组件规则、hook/扩展格式、自定义组件边界、禁止项和实现后合规扫描。brownfield 不再维护独立规则副本，避免与 frontend 子技能产生分叉。

**brownfield 不维护前端速查清单。**

为避免规则分叉，本 skill 只负责把 brownfield 流程路由到 `iidp-frontend` 和 `iidp-frontend-codegen-protocol`。所有前端分支判断、组件规则、扩展格式、自定义组件边界、请求方式、禁止项和写入后合规扫描，均以 `skills/frontend/references/iidp-frontend-codegen-protocol.md` 为唯一执行标准。

### Step 11：同步规格和最终回校验

实现后复用：

```text
/sdd-sync
```

然后必须重新运行 code-index，得到新的：

```text
codebook/baseline-spec/
```

新的 `codebook/baseline-spec/` 是下一次 brownfield 新需求的现状基线；不得只同步规格而跳过 code-index 刷新，否则下一次需求会基于旧事实产生断层。

最终比较：

```text
target-spec vs new baseline-spec
```

生成：

```text
specs/features/<feature>/final-diff.md
```

如果不一致：

- 列出未落地项。
- 列出多余改动。
- 列出仍需确认项。
- 决定补任务、修正规格或记录决策。

## 输出总览

```text
codebook/
└── baseline-spec/

specs/
├── iidp-stack.md
├── integration-map.md
├── unresolved.md
├── baseline/
└── features/
    └── <feature>/
        ├── requirements.md
        ├── delta-spec.md
        ├── diff.md
        ├── target-spec.md
        ├── contracts.md
        ├── backend-spec.md
        ├── frontend-spec.md
        ├── interaction-spec.md
        ├── plan.md
        ├── impact.md
        ├── tasks.md
        ├── validation.md
        ├── patch-diff.md
        └── final-diff.md
```

## 停止条件

遇到以下情况必须停下来让用户确认：

- 找不到项目路径。
- 无法确认是否 IIDP 项目。
- `codebook/baseline-spec/` 不存在。
- 新需求与已有 app/model/menu/view/service/permission 冲突。
- target node id、服务参数、权限码无法确认且会影响实现。
- plan 修改范围超过 target-spec。
- final diff 显示实现与 target-spec 不一致。

## 完成回复要求

完成本 skill 的某个阶段后，回复必须说明：

- 当前完成到哪一步。
- 生成了哪些 SDD 文件。
- 是否存在 diff 冲突。
- 是否存在待确认项。
- 下一步应该运行哪个 SDD 命令。
