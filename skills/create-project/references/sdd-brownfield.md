# IIDP 存量项目 SDD 接入

本文定义已有 IIDP 项目如何接入 SDD。核心原则是：**先用 code-index 建立现状基线，再用 SDD 管理本次变更，最后用差异比较确认计划和代码是否真的覆盖目标规格。**

`sdd-brownfield` 不是独立开发流程，而是 `create-project` SDD 的存量入口。它复用 `/sdd-specify`、`/sdd-contracts`、`/sdd-spec`、`/sdd-plan`、`/sdd-tasks`、`/sdd-implement`、`/sdd-sync`，只补足存量项目必需的基线、差异和回校验。

---

## 适用场景

使用本流程：

- 已有 IIDP 工程，要新增 App。
- 已有 IIDP App，要新增功能、页面、服务、字段或前端扩展。
- 遗留 IIDP 工程要纳入 SDD 管理。
- 需要跨 App 集成、技术债治理或兼容性改造。
- 用户通过类似 `/iidp-brownfield 项目路径 ... 需求 ...` 的编排入口发起存量开发。

不使用本流程：

- 完全从零创建 IIDP 父工程，用 `sdd-greenfield.md`。
- 只是解释代码，不产生规格和开发计划，用 `code-index`。
- 已经有明确 `backend-spec.md` / `frontend-spec.md`，只执行实现任务，用 `/sdd-implement`。

---

## 核心概念

| 名称                                   | 含义                                            | 来源                                          |
| -------------------------------------- | ----------------------------------------------- | --------------------------------------------- |
| `baseline-spec`                        | 当前代码事实，默认只读                          | `code-index` 输出的 `codebook/baseline-spec/` |
| `delta-spec`                           | 本次用户要新增、修改或删除的差异                | SDD 从用户需求生成                            |
| `target-spec`                          | `baseline-spec + delta-spec` 合成后的目标状态   | SDD 合成                                      |
| `diff`                                 | 比较现状、需求、计划和实现是否一致              | SDD brownfield 检查                           |
| `contracts.md`                         | 本次实现的 app/model/service/view/menu/权限契约 | `/sdd-contracts`                              |
| `backend-spec.md` / `frontend-spec.md` | 可执行技术规格                                  | `/sdd-spec`                                   |

关系：

```text
baseline-spec（现状）
  + delta-spec（本次变化）
  -> target-spec（目标）
  -> contracts/spec/plan/tasks
  -> implement
  -> re-index
  -> final diff
```

---

## 用户入口

推荐由编排 skill 或命令接入，例如 `/iidp-brownfield`：

```text
/iidp-brownfield 项目路径：E:\xxx\iidp-project
新增一个 order app
需求是：支持订单创建、订单明细、订单状态流转、订单查询。
```

如果未提供项目路径，默认当前目录。若当前目录不是 IIDP 项目根，或发现多个候选项目，必须让用户确认。

最少输入：

- 项目路径或当前 IIDP 项目根。
- 变更描述。

推荐输入：

- 变更类型：新增 App / 新增功能 / 修改功能 / 技术债治理 / 跨 App 集成。
- 目标 appName 或新 appName。
- 需求文档路径。
- 是否只生成 SDD 产物，还是继续执行实现。

---

## 总流程

```text
0. 定位项目
1. code-index 生成 baseline-spec
2. /sdd-brownfield-init 导入存量上下文
3. /sdd-specify 生成 requirements.md
4. 生成 delta-spec 并做第一次 diff
5. /sdd-contracts 生成 contracts.md
6. /sdd-spec 生成 backend-spec/frontend-spec/interaction-spec
7. /sdd-plan 生成 plan.md，并做计划差异检查
8. /sdd-tasks 生成 tasks.md/validation.md
9. /sdd-implement 按任务实现
10. /sdd-sync 同步代码变更到规格
11. 重新 code-index，做最终 diff
```

---

## Step 0：定位项目

目标：确认 SDD 要接入哪个存量 IIDP 工程。

检查：

- 后端：`pom.xml`、`apps/apps.json`、`**/app.json`、`**/views/menus.json`。
- 前端：`package.json`、`config/apps.json`、`apps/<app>/index.js`、`apps/<app>/views/index.js`。
- 若后端和前端分仓或分目录，记录两者路径。

输出：

```text
specs/brownfield/project-context.md
```

内容至少包含：

- 项目根。
- 后端路径。
- 前端路径。
- 当前分支。
- 输入需求。
- 识别到的 IIDP 信号。

---

## Step 1：建立 baseline-spec

目标：让 SDD 知道老系统现在是什么样。

执行：

```text
code-index 分析当前项目
```

必须生成：

```text
codebook/baseline-spec/
├── apps.json
├── menus.json
├── models.json
├── views.json
├── services.json
├── frontend-extends.json
├── capability-list.json
├── artifact-map.json
├── trace-map.json
└── unresolved.json
```

要求：

- `baseline-spec` 是当前代码事实，不写新需求。
- 不从 Markdown Codebook 反抽；直接来自源码、JSON 配置、前端扩展和 code-index 的 IIDP 提取结果。
- 低置信度、示例占位、运行时节点和无法确认服务必须进入 `unresolved.json`。

若已有 `codebook/baseline-spec/`：

- 如果代码文件比 baseline 新，提示需要重新运行 code-index。
- 如果用户明确要求复用旧 baseline，可以继续，但在 `diff.md` 中标注风险。

---

## Step 2：导入 SDD 存量上下文

目标：把 `baseline-spec` 变成 SDD 可用的项目上下文。

执行：

```text
/sdd-brownfield-init
```

增强要求：

- 优先读取 `codebook/baseline-spec/`。
- 保留现有从 POM、package.json、`app.json`、`apps/apps.json` 提取版本和命名规则的能力。
- 生成 `specs/iidp-stack.md` 时，必须引用 baseline 中的现有 App、命名规则、视图规则和前端扩展结构。

产物：

```text
specs/
├── iidp-stack.md
├── integration-map.md
├── unresolved.md
└── baseline/
    ├── apps.md
    ├── capabilities.md
    ├── models.md
    ├── views.md
    ├── services.md
    └── frontend-extends.md
```

`specs/baseline/` 是给人读的摘要，真实机器基线仍以 `codebook/baseline-spec/*.json` 为准。

---

## Step 3：生成 requirements.md

目标：把用户需求转成 SDD 需求规格。

执行：

```text
/sdd-specify <用户需求>
```

产物：

```text
specs/features/<feature>/requirements.md
```

要求：

- 必须读取 `specs/iidp-stack.md` 和 `specs/baseline/`。
- 对新增 App，说明它与已有 App 的关系和依赖。
- 对修改已有 App，明确是新增、替换、扩展还是治理。
- 不确定的节点、服务、权限、字典、运行时规则写为“待确认”，不得编造。

---

## Step 4：生成 delta-spec 并做第一次 diff

目标：明确“这次需求相对现状改变了什么”。

生成：

```text
specs/features/<feature>/delta-spec.md
specs/features/<feature>/diff.md
```

`delta-spec.md` 必须包含：

| 字段       | 说明                                                         |
| ---------- | ------------------------------------------------------------ |
| 变更类型   | 新增 App / 新增功能 / 修改功能 / 删除能力 / 技术债治理       |
| 目标 app   | 新 app 或已有 app                                            |
| 受影响对象 | model、menu、view、service、frontendExtend、data、permission |
| 新增项     | 本次新增的能力                                               |
| 修改项     | 本次修改的现有能力，必须引用 baseline specId                 |
| 删除项     | 本次删除或停用的能力，必须说明兼容策略                       |
| 待确认项   | 无法从 baseline 或需求确认的内容                             |

第一次 diff 比较：

```text
baseline-spec vs delta-spec
```

检查：

- appName 是否已存在。
- model/menu/view/service/permission 是否冲突。
- 是否误把已有能力当新增。
- 是否修改已有生产能力。
- 是否引用了 `unresolved.json` 中的待确认项。
- 是否需要跨 App 依赖。

`diff.md` 输出：

- `added`：新增项。
- `changed`：修改项。
- `removed`：删除/停用项。
- `conflicts`：冲突项。
- `needsConfirmation`：待确认项。
- `risk`：兼容、数据、权限和运行时风险。

---

## Step 5：生成 target-spec

目标：形成“完成本次开发后系统应是什么样”的目标规格。

生成：

```text
specs/features/<feature>/target-spec.md
```

规则：

- `target-spec = baseline-spec + delta-spec`。
- 新增项必须有目标文件类型或推荐落点。
- 修改项必须能回连到 baseline 的 `trace-map`。
- 删除项必须有兼容和回滚说明。
- `target-spec` 是后续 `/sdd-contracts`、`/sdd-spec`、`/sdd-plan` 的主要输入。

---

## Step 6：生成 contracts.md

目标：锁定本次实现的 IIDP 契约。

执行：

```text
/sdd-contracts
```

产物：

```text
specs/features/<feature>/contracts.md
```

contracts 必须覆盖：

- appName / appPkg / Maven module / frontend app。
- model_name / tableName / fields。
- service / args / auth / return。
- menuId / viewId / view type。
- frontend selector / hook / ds_config / `window.Tech.httpMeta` 或 `vm.request`。
- data seed / dict / encoder rule。
- 待确认项和证据来源。

---

## Step 7：生成 backend/frontend/interaction spec

目标：把契约转换成可实现规格。

执行：

```text
/sdd-spec
```

产物：

```text
specs/features/<feature>/backend-spec.md
specs/features/<feature>/frontend-spec.md
specs/features/<feature>/interaction-spec.md
```

规则：

- 后端规格必须遵守 `specs/iidp-stack.md` 和 `contracts.md`。
- 前端规格必须说明标准模板、在线视图、hook、扩展视图、自定义 Vue2 组件的选择理由。
- 交互规格按 `sdd-frontend-interaction.md` 输出，不只写用户流程，也要保留页面、状态、事件、数据源和验收映射。

---

## Step 8：生成 plan.md 并做计划差异检查

目标：确认计划覆盖目标规格，且没有扩大修改范围。

执行：

```text
/sdd-plan
```

产物：

```text
specs/features/<feature>/plan.md
specs/features/<feature>/impact.md
```

第二次 diff 比较：

```text
baseline-spec vs target-spec vs plan.md
```

检查：

- `target-spec` 中的新增/修改项是否都进入计划。
- 计划是否修改了未在 target-spec 中声明的 App、模型或视图。
- 是否遗漏注册文件，例如 `apps/apps.json`、`config/apps.json`、`views/index.js`。
- 是否涉及生产兼容或迁移。

`impact.md` 输出：

- 新增文件。
- 修改文件。
- 不应修改文件。
- 受影响 App。
- 受影响模型/服务/权限。
- 兼容风险和回滚策略。

---

## Step 9：生成 tasks.md

目标：拆成可逐项完成的任务。

执行：

```text
/sdd-tasks
```

产物：

```text
specs/features/<feature>/tasks.md
specs/features/<feature>/validation.md
```

任务要求：

- 每个任务必须引用 `target-spec` 或 `contracts.md` 中的条目。
- 新增 App 任务必须包含注册、模型、视图、菜单、data、前端入口、验证。
- 修改已有 App 任务必须包含兼容性验证。
- 涉及前端节点时，必须包含 selector 或节点 id 确认任务。

---

## Step 10：实现并做代码差异检查

目标：按任务增量修改代码。

执行：

```text
/sdd-implement
```

实现规则：

- 一次只处理一个未完成任务。
- 不全量覆盖已有 App。
- 不修改无关模块。
- 不猜测未确认节点、权限、服务参数。
- 完成任务后勾选 `tasks.md`。

第三次 diff 比较：

```text
git diff vs tasks.md vs target-spec.md
```

生成：

```text
specs/features/<feature>/patch-diff.md
```

检查：

- 是否多改无关文件。
- 是否遗漏计划文件。
- 是否出现未计划的重构。
- 是否覆盖存量业务。

---

## Step 11：同步规格和最终回校验

执行：

```text
/sdd-sync
```

然后必须重新运行 code-index，得到新的 `codebook/baseline-spec/`。新的 baseline 是下一次 brownfield 新需求的现状输入；不得只同步规格而跳过 code-index 刷新，否则下一次需求会基于旧事实产生断层。

最终 diff：

```text
target-spec vs new baseline-spec
```

生成：

```text
specs/features/<feature>/final-diff.md
```

检查：

- 新增能力是否在新 baseline 中出现。
- 服务、模型、视图、前端扩展是否能回连源码。
- `unresolved` 是否减少或仍需确认。
- 实际代码是否偏离 target-spec。

---

## 新增 App 场景检查清单

新增 IIDP App 时，必须检查：

- 后端 appName 是否与已有 App 冲突。
- Maven module、appPkg、`app.json.resolved` 是否符合存量规则。
- `apps/apps.json` 是否注册。
- 模型 name、tableName、字段、索引、字典是否符合规范。
- `views/*.json`、`menus.json`、`data/*.json` 是否完整。
- 前端 app 是否需要新增，或是否只需扩展已有 app。
- 前端 `config/apps.json` 是否注册。
- `apps/<app>/views/index.js` 是否存在。
- 是否需要 `model-views`、hook、扩展视图或自定义组件。
- `contracts.md` 中是否写清 app/model/service/args/auth。

---

## 修改已有 App 场景检查清单

修改已有 App 时，必须检查：

- 是否引用 baseline 中的 specId。
- 是否影响已有菜单、按钮、视图、服务、权限。
- 是否有生产兼容策略。
- 是否需要保留旧 key。
- 是否需要数据迁移。
- 是否需要前端 selector 运行时确认。
- 是否可以用 hook 或扩展视图完成，避免改底座或生成目录。

---

## 输出总览

```text
codebook/
└── baseline-spec/                    # code-index 当前事实

specs/
├── iidp-stack.md                     # 存量项目宪法
├── integration-map.md                # App/模型/服务/前后端关系
├── unresolved.md                     # 存量待确认摘要
├── baseline/                         # 给人读的 baseline 摘要
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

---

## 常见失败处理

| 问题                                    | 处理                                                  |
| --------------------------------------- | ----------------------------------------------------- |
| 找不到 IIDP 项目根                      | 要求用户提供包含 `pom.xml` 或 `apps/apps.json` 的路径 |
| baseline-spec 不存在                    | 先运行 code-index                                     |
| baseline-spec 过期                      | 提示重跑 code-index，或标注风险后继续                 |
| 新需求与已有 app/model 冲突             | 写入 `diff.md conflicts`，停止进入 `/sdd-contracts`   |
| 节点 id 无法确认                        | 写入 `unresolved`，拆出确认任务                       |
| 计划修改范围超过 target-spec            | 停止进入 `/sdd-tasks`，先修正 plan                    |
| 实现后 target-spec 与新 baseline 不一致 | 写入 `final-diff.md`，补任务或修正规格                |
