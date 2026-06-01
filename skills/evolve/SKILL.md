---
name: evolve
description: Use when 需要通过基准实验、源码转规格、Docker 冒烟测试或样本池验证来优化 IIDP create-project skill。
---

# evolve

## 概述

`evolve` 是 `skills/create-project` 的自进化评测框架。它用固定基准、从源码生成的需求文档、生成的 IIDP 应用、Docker 冒烟测试和一个 100 分评分，判断某个 `create-project` 改动是否值得保留。

本 skill 参考 autoresearch 循环：固定环境，只修改允许修改的目标，用一个可比较指标度量，只提交提升，最终交给人工审核。

## 硬边界

- 固定基准仓库是 `https://github.com/YunaiV/ruoyi-vue-pro.git`。
- 首次使用时记录基准 commit SHA。后续比较必须复用同一个 SHA，除非用户明确要求刷新基准。
- 自动改进阶段只能修改 `skills/create-project/` 下的文件。
- 不得修改 `skills/code-index/`、`skills/evolve/`、样本仓库、生成的 IIDP 应用、Docker 基础设施或测试产物来强行提分。
- 每一轮改进只能做一个小而可审查的改动。如果还需要处理第二个问题，放到下一轮。
- 样本池结果可以指导下一次 `create-project` 修改，但是否保留改动只由固定基准分决定。
- 分数提升则保留分支等待人工审核；分数未提升必须回滚。
- 永远不要自动合并 evolve 分支。

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

### Phase 3：改进 `create-project`

1. 创建或切换到隔离的 evolve 分支。
2. 从基准或样本池中选择一个失败模式。
3. 只修改 `skills/create-project/` 下的一小块内容。
4. 重新评测前先提交该改动，确保可以干净回滚。
5. 使用相同基准 SHA 和相同环境重新运行固定基准。
6. 比较新基准分与上一轮基准分：
   - `new_score > previous_score`：保留 commit，更新证据，分支留给人工审核。
   - `new_score <= previous_score`：回滚 commit，并记录失败原因。

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
- 任何保留的改动只修改 `skills/create-project/`。
- 失败改动已回滚并解释。
- 提升分数的改动保留在分支上等待人工审核，不自动合并。
