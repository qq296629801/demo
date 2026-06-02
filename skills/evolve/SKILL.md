---
name: evolve
description: Use when 需要通过基准实验、源码转规格、Docker 冒烟测试或样本池验证来优化 IIDP create-project skill。
---

# evolve

## 概述

`evolve` 是 `skills/create-project`、`skills/backend`、`skills/frontend` 的自进化评测框架。它用固定基准、从源码生成的需求文档、生成的 IIDP 应用、Docker 冒烟测试和一个 100 分评分，判断某个 skills 改动是否值得保留。

本 skill 参考 autoresearch 循环：固定环境，只修改允许修改的目标，用一个可比较指标度量，只提交提升，最终交给人工审核。**每次评分提升后，强制执行 Phase 5 技能反馈回写**，把解决过程中发现的隐性知识结构化写回技能文档。

## 硬边界

- 固定基准仓库是 `https://github.com/YunaiV/ruoyi-vue-pro.git`。
- 首次使用时记录基准 commit SHA。后续比较必须复用同一个 SHA，除非用户明确要求刷新基准。
- 自动改进阶段只能修改以下目录下的文件：
  - `skills/create-project/`：SDD 指令文件、命令文件
  - `skills/backend/`：后端能力域文档（示例代码、用法覆盖、路由修复）
  - `skills/frontend/`：前端能力域文档（子技能 SKILL.md、组件规则库、iidpDoc）
- 不得修改 `skills/code-index/`、`skills/evolve/` 本身、样本仓库、生成的 IIDP 应用、Docker 基础设施或测试产物来强行提分。
- 每一轮改进只能做一个小而可审查的改动。如果还需要处理第二个问题，放到下一轮。
- 样本池结果可以指导下一次 `create-project` 修改，但是否保留改动只由固定基准分决定。
- 分数提升则保留分支等待人工审核；分数未提升必须回滚。
- 永远不要自动合并 evolve 分支。

## 私有规则来源门禁

IIDP backend/frontend 规则是私有平台知识。`evolve` 禁止从公网推断 IIDP backend/frontend 规则，也不得凭训练知识、通用框架经验或单次失败现象把推测写成稳定规则。

允许作为 backend/frontend 规则依据的来源仅限：

- 本地 IIDP 文档：`skills/backend/`、`skills/frontend/` 及其引用文件。
- 现有 IIDP 示例源码或真实工程源码。
- 编译、启动、Docker、JSON-RPC 冒烟测试等运行证据。
- 平台日志、数据库记录、配置文件事实。
- 用户人工确认。

没有证据的规则必须标记为 `待确认` 或 `knowledge-gap`，不得写回 `skills/backend/` 或 `skills/frontend/`。如果只能通过公网资料解释普通开源框架行为，可以作为样本筛选或需求理解参考，但不能作为 IIDP 私有规则依据。

## 必需输入与输出

对每个被评测的源码仓库：

1. 使用 `skills/code-index` 从源码生成需求文档。
2. 至少要求产出 SRS、用户故事、API 文档、数据库结构、测试用例或验收标准。
3. 使用 `skills/create-project` 根据需求文档生成 IIDP SDD 产物和 IIDP 应用。
4. 从用户故事和测试用例生成冒烟测试。
5. 按 `skills/create-project/references/sdd-validation.md` 和本 skill 的冒烟验证参考执行 Docker 与 JSON-RPC 验证。
6. 输出包含日志、commit SHA、配置证据和失败原因的评分报告。

## 工作流

### Phase 0：加载参考资料

只读取当前动作需要的参考资料：

| 需要 | 读取 |
|---|---|
| 实验循环、分支、保留/回滚规则 | `references/autoresearch-loop.md` |
| 评分计算和接收门禁 | `references/evaluation-rubric.md` |
| 手工样本或 websearch 测试样本 | `references/sample-pool.md` |
| Docker 账号密码检查和 JSON-RPC 冒烟测试 | `references/smoke-validation.md` |

运行真实评测前，还必须读取 `skills/code-index/SKILL.md`、`skills/create-project/SKILL.md` 和 `skills/create-project/references/sdd-validation.md`。

### Phase 1：建立基准

1. 克隆或复用固定基准仓库。
2. checkout 已记录的基准 SHA；如果是首次运行，则记录当前 SHA。
3. 使用 `code-index` 执行源码到规格书生成。
4. 使用 `create-project` 执行规格书到 IIDP 应用生成。
5. 执行 Docker 配置一致性检查。
6. 执行从用户故事和测试用例生成的功能冒烟测试。
7. 对基准打分并保存基准证据。

基准分是后续 `create-project` 改动的唯一接收阈值。

### Phase 2：探索样本池

使用样本仓库发现 `create-project` 的薄弱点：

- 优先使用用户提供的样本。
- 如果用户要求，使用 websearch 最多发现 50 个后端管理框架仓库。
- 过滤不可访问、没有源码、无关或许可证不清晰的仓库。
- 对每个接收的样本，记录 URL、commit SHA、框架说明、生成规格质量、IIDP 生成失败、冒烟测试失败和疑似缺失的 `create-project` 指导。

不要只根据样本池分数保留或拒绝 `create-project` 改动。

### Phase 3：改进 skills

1. 创建或切换到隔离的 evolve 分支。
2. 从基准或样本池中选择一个失败模式。
3. 先按失败原因分类，避免把资料不足误判为 create-project 失败：
   - `route-gap`：文档或规则已存在但未被加载，修 `skills/create-project/` 或 skill 路由。
   - `backend-doc-gap`：backend 私有规则缺失、示例不足或边界用法不清，且有本地文档、源码、日志、配置、测试或用户确认作为证据，修 `skills/backend/`。
   - `frontend-doc-gap`：frontend 私有规则缺失、组件规则不足或扩展协议不清，且有 `iidpDoc`、源码、日志、配置、测试或用户确认作为证据，修 `skills/frontend/`。
   - `sdd-template-gap`：SDD 模板、契约映射或验证规则表达不足，修 `skills/create-project/references/sdd-*.md`。
   - `knowledge-gap`：没有足够私有事实源，输出缺口报告，列出需要用户补充的文档、API、示例、日志或确认事项，不自动修改 backend/frontend 规则。
4. 按以下优先级选择改进方向，每轮只做一小块改动：
   - **优先**：修复 skills 路由断链（路由不可达直接导致 AI 找不到指令）
   - 补充 `sdd-backend.md` 或拆分后的能力域文件（model/method/view/contracts）的示例代码
   - 补充 `skills/backend/references/core/` 各文件的示例和边界用法
   - 补充 `sdd-frontend.md` 和 `sdd-frontend-interaction.md` 的交互规则和状态流转
   - 补充 `skills/frontend/` 子技能的用法覆盖和组件规则
5. 重新评测前先提交该改动，确保可以干净回滚。
6. 使用相同基准 SHA 和相同环境重新运行固定基准。
7. 运行 IIDP 合规门禁检查（见 `references/evaluation-rubric.md`）：门禁不通过直接 REVERT，不进入评分。
8. 比较新基准分与上一轮基准分：
   - `new_score > previous_score`：保留 commit，更新证据，**立即执行 Phase 5 技能反馈回写**，然后分支留给人工审核。
   - `new_score <= previous_score`：回滚 commit，并记录失败原因。

### Phase 5：技能反馈回写

**触发条件：** Phase 3 评分提升后强制执行。不能跳过。

**目的：** 把本轮解决过程中通过"调试、逆向、试错"发现的隐性知识结构化写入对应技能文件，消除下次运行的同类踩坑成本。

#### 5.1 收集发现

回顾本轮从开始到评分提升的全过程，逐个检查以下信号：

| 信号 | 含义 | 示例 |
|------|------|------|
| 通过反编译/读源码才搞清的协议细节 | 文档缺少操作级说明 | JSON-RPC 的 token 从 HTTP header 传递、body 参数层级 |
| 试错 3 次以上才成功的命令 | 文档缺少可执行模板 | curl 命令的 JSON 结构、数据库查询语句 |
| 从日志/数据库反推的初始化流程 | 文档缺少环境准备步骤 | 首 token 来源、默认账号密码 |
| 需要手动检查才能发现的不一致 | 文档缺少校验脚本 | Docker 配置与 app.json 之间的服务名对应 |
| 运行失败后通过本地代码搜索、日志或配置排查才定位的报错 | 文档缺少故障排除 | "找不到服务：APP:null" 的根因 |

**每轮至少产生 1 条发现。** 如果评分为满分或有明确文档改进，可以写 "本轮未发现文档缺口"。

#### 5.2 结构化记录

每条发现用此模板写入 `evolve-evidence.md` 的本轮日志中：

```markdown
### 发现 N：<一句话标题>

| 字段 | 内容 |
|------|------|
| **问题** | 本轮什么操作因文档缺失而失败/低效 |
| **根因** | 哪个技能文件的哪个章节信息不足（精确到行或标题） |
| **解法** | 实际采用的解决方法（命令/代码/流程） |
| **失败分类** | `route-gap` / `backend-doc-gap` / `frontend-doc-gap` / `sdd-template-gap` / `knowledge-gap` |
| **证据来源** | 本地文档路径、源码路径、日志片段、配置项、测试结果或用户确认 |
| **目标文件** | `skills/<skill>/references/xxx.md` |
| **插入位置** | 在哪个已有章节/段落之后新增 |
| **验证方式** | 修复后通过的静态检查、构建、Docker 或 JSON-RPC 冒烟测试 |
```

#### 5.3 写回技能文件

对每条发现，直接编辑目标技能文件：

1. **定位插入点：** 按发现中的"插入位置"，Read 目标文件确认上下文。
2. **编写内容：** 写可独立理解的 markdown 段落，包含可复制的命令/代码、关键约束、反例（常见错误写法）。
3. **编辑写入：** 用 Edit 工具插入到目标位置。
4. **验证：** 确认新增内容在目标文件中完整可见。

**写回规则：**

- 每次只写一条发现，确保 diff 可审查。
- 内容面向"下一次运行的 LLM"，提供可直接执行的信息，不写冗长的背景故事。
- 命令和代码必须可直接复制执行，用 `<placeholder>` 标注可变部分。
- 写回 backend/frontend 的规则必须包含失败现象、证据来源、规则结论和验证方式；缺任一项时只能记录 `knowledge-gap`，不能把推测写成规则。
- 从单次运行现象得到的结论只能作为候选规则；除非有本地文档、源码、日志、配置、测试证据或用户确认支撑，否则不得沉淀为稳定规则。
- 如果目标章节已有相关描述，在原内容上补充而非重复。
- 如果新增内容可能与其他技能文件内容冲突，在 commit message 中注明。

**目标技能的定位规则：**

| 发现类型 | 目标技能 | 典型文件 |
|---------|---------|---------|
| JSON-RPC 协议、API 参数、Filter、SQL | `backend` | `references/core/api-filter-sql.md` |
| 部署、Docker、编译、环境配置 | `backend` | `references/core/pom-structure.md`、`SKILL.md` |
| 模型注解、视图 JSON、菜单种子数据 | `backend` | `references/core/model.md`、`view.md`、`menu.md`、`seed-data.md` |
| SDD 产物模板、契约映射、验证规则 | `create-project` | `references/sdd-backend.md`、`sdd-contracts.md`、`sdd-validation.md` |
| 前端组件、交互规范、iidpDoc | `frontend` | 对应子技能 SKILL.md 或组件规则文件 |
| 冒烟测试脚本、测试格式 | `create-project` | `references/sdd-validation.md` |

#### 5.4 提交

Phase 5 的写回改动与 Phase 3 的技能改动在同一个 evolve 分支上，可以合并为同一个 commit 或紧随其后单独 commit：

- **合并：** 发现改动的文件与 Phase 3 改动文件不重叠时，可以合并到一个 commit。
- **单独：** 发现改动涉及新文件时，单独 commit，message 格式：`evolve: 回写 Phase 5 发现 N — <标题>`

回写改动同样受硬边界约束：只能修改 `skills/create-project/`、`skills/backend/`、`skills/frontend/`。

---

### Phase 4：人工审核门禁

当至少一个 commit 提升了基准分：

- 保留 evolve 分支。
- 汇总分数变化、变更文件、基准 SHA、测试日志和样本池证据。
- 请求人工审核；如果用户要求发布到 GitHub，则创建 PR。
- 不要自动合并。

当没有任何 commit 提升基准分：

- 回滚所有失败实验 commit。
- 返回原始分支。
- 报告观察到的最有价值失败模式，以及没有保留改动的原因。

## 证据文件

运行循环时使用本地、可审查的证据文件：

- `evolve-results.tsv`：每个基准、样本或轮次一行。
- `evolve-evidence.md`：人类可读运行日志，包含评分表、diff、冒烟测试摘要、Docker 配置发现、保留/回滚决策。

除非用户明确要求，否则不要提交这些证据文件。

## 完成检查清单

- 已记录基准仓库 URL 和 commit SHA。
- `code-index` 需求文档包含 SRS、用户故事、API、数据库、测试或验收标准。
- `create-project` 已生成必需的 SDD 产物和 IIDP 应用文件。
- Docker 账号密码、数据库名、Redis/MinIO 设置、应用端口在 compose、Docker 配置和生成应用配置之间一致。
- 冒烟测试来自已记录的用户故事和测试用例。
- 评分使用 `references/evaluation-rubric.md`。
- 任何保留的改动只修改 `skills/create-project/`、`skills/backend/` 或 `skills/frontend/`。
- 失败改动已回滚并解释。
- 提升分数的改动保留在分支上等待人工审核，不自动合并。
- **Phase 5 已执行：** 本轮解决过程中的隐性知识已结构化写回对应技能文件。`evolve-evidence.md` 中有每条发现的记录。
