# Autoresearch 循环适配 — code-index-evolve

## 原则映射

将 autoresearch 模式适配到 code-index 维护场景：

| Autoresearch 思路 | code-index-evolve 对应机制 |
|---|---|
| 固定训练环境 | 固定样本代码仓库 + git commit hash（存于 `.code-index-evolve/baseline.json`） |
| 一个可编辑训练文件 | 改进阶段只允许编辑 `skills/code-index/`（SKILL.md + references/ 下所有 .md 文件） |
| 一个验证指标 | 一个 100 分 Codebook 质量评分（见 `evaluation-rubric.md`） |
| Git 作为记忆 | 每次改 skill 先 commit 再测量，commit sha 写入证据文件 |
| 保留胜利、丢弃失败 | new_score > prev_score → KEEP；未提升 → REVERT |
| 人类编写 program.md | 人工审核 skill 改动和最终 PR |

循环必须优化可测量的 Codebook 生成质量，而不是优化评测框架本身（禁止修改 evaluation-rubric.md 来"提高"分数）。

---

## 分支与提交规则

- 除非用户提供其他名称，创建隔离分支 `code-index-evolve/YYYYMMDD-HHMM`
- 开始前记录原始分支和最近一次基准分数
- 每个尝试性改动在运行基准前提交（"改 → 提交 → 测量"，顺序不可颠倒）
- 提交信息格式：`code-index-evolve: <gap分类> — <一句话假设>`
  - 示例：`code-index-evolve: prompt-gap — 增强 Prompt 7 的 Header 约定要求`
- 分数未提升：对实验 commit 执行 `git revert`，记录原因
- 分数提升：保留 commit，更新基准分，立即执行 Phase 4 技能反馈回写

---

## 单轮形态（10 步）

每一轮按以下顺序严格执行：

```
步骤 1：选择失败模式
        → 从上一轮评分报告中找得分最低的维度或具体问题
        → 优先攻克分数差距最大的单一问题

步骤 2：说明改进假设（一句话）
        → 格式："如果 [修改内容]，预期 [哪个维度] 提升 [N] 分"
        → 示例："如果在 Prompt 7 中强制要求 Header 约定，预期 Agent评审/api 提升 1-2 分"

步骤 3：失败分类
        → 从以下 6 类中选一个（不得混合）：
          · phase-c-gap   ：Phase C 采集不完整（漏 Controller/Entity/VO 文件）
          · prompt-gap    ：Prompt 模板表达不足（文档缺关键字段/章节/约束）
          · variable-gap  ：变量采集步骤缺失（{{...}} 未被填充）
          · iidp-extract-gap：IIDP baseline-spec 提取规则不足
          · framework-detect-gap：框架识别规则有误（非 Java 项目识别失败）
          · knowledge-gap ：缺乏样本证据，无法确定改法（本轮只记录，不修改）

步骤 4：修改 skills/code-index/ 中一小块内容
        → 每轮只改一处，改动必须能被单一假设解释
        → knowledge-gap 时：只在 evolve-evidence.md 记录缺口，不改 skill

步骤 5：提交改动
        → git add skills/code-index/
        → git commit -m "code-index-evolve: <gap分类> — <假设>"
        → 记录 commit sha

步骤 6：使用相同样本仓库（相同 commit hash）重新运行 code-index skill
        → 先执行 codegraph init -i（如索引已存在可跳过）
        → 完整执行 Phase A → B → C → D → D'
        → 生成的 Codebook 快照存入 .code-index-evolve/runs/<timestamp>/codebook/

步骤 7：执行 Agent 评审 + 数量对账
        → 按 agent-review-protocol.md 并行执行三个 agent 评审
        → 统计新 Codebook 的 ACTUAL_API / ACTUAL_ENTITY / ACTUAL_MODULE
        → 抽查 10 个字段的可追溯性（见 evaluation-rubric.md §②）

步骤 8：按 evaluation-rubric.md 打分（100 分制）
        → 先执行合规门禁检查
        → 逐维度计算分数
        → 记录各维度分数到 evolve-results.tsv

步骤 9：保留或回滚
        → new_score > prev_score → KEEP：更新基准分，继续步骤 10
        → new_score ≤ prev_score → REVERT：
            git revert <experiment-commit>
            在 evolve-evidence.md 记录"回滚原因"
            直接进入下一轮步骤 1

步骤 10：若 KEEP → 执行 Phase 4 技能反馈回写
         → 发现新的可写回规则时，更新 skills/code-index/ 相应文件
         → 追加证据到 evolve-evidence.md
```

---

## 证据模板

每轮实验结束后，在 `.code-index-evolve/evolve-evidence.md` 末尾**追加**以下内容：

```markdown
---

## 第 <n> 轮：<hypothesis>

- 基准仓库：<repoName>
- 仓库 Commit Hash：<commitHash>
- 运行目录：.code-index-evolve/runs/<timestamp>/
- 实验 commit：<sha>
- 可编辑范围已检查：only skills/code-index/

### 失败模式

<来自上一轮评分报告的具体问题，引用原文>

### 失败分类

`phase-c-gap` / `prompt-gap` / `variable-gap` / `iidp-extract-gap` / `framework-detect-gap` / `knowledge-gap`

### 私有规则证据

- 证据来源：<本地文档路径 / 源码路径 / codegraph 输出 / 用户确认>
- 规则结论：<可写回 skills/code-index/ 的具体规则，或 knowledge-gap 缺口说明>
- 验证方式：<codegraph 输出验证 / 字段溯源检查 / Agent 评审分数 / 接口数对账>

### 改动摘要

<文件路径 + before/after 核心差异，不超过 20 行>

### 评分对比

| 维度 | Previous | New | Delta |
|---|---:|---:|---:|
| 接口文档完整性 (30) | 0 | 0 | 0 |
| 可追溯性与无幻觉 (25) | 0 | 0 | 0 |
| Agent 评审质量 (25) | 0 | 0 | 0 |
| 变量采集完整性 (10) | 0 | 0 | 0 |
| 文件完整性 (10) | 0 | 0 | 0 |
| **Total** | **0** | **0** | **0** |

### 决策

**KEEP** / **REVERT** — <原因一句话>

### Phase 4 技能反馈回写

- 发现：<可写回的规则标题，或"本轮未发现可写回文档缺口">
- 目标文件：<skills/code-index/references/... 或 knowledge-gap>
- 插入位置：<章节/标题>
- 验证方式：<下一轮如何验证该规则已生效>
```

---

## evolve-results.tsv 格式

`.code-index-evolve/evolve-results.tsv` 存储每轮的分数摘要，便于趋势观察：

```tsv
round	commit_sha	gap_type	hypothesis	api_completeness	traceability	agent_review	variable	file_completeness	total	decision
0	<baseline>	—	基准分	0	0	0	0	0	0	BASELINE
1	<sha1>	prompt-gap	增强 Prompt 7 Header 约定	0	0	0	0	0	0	KEEP/REVERT
```

---

## 停止条件

出现以下情况时停止循环：

| 条件 | 说明 |
|---|---|
| 达到用户指定的最大轮数 | 默认上限 10 轮（可覆盖） |
| 连续两轮保留改动的提升都 < 1 分 | 边际收益递减，停止迭代 |
| 连续三次尝试性改动都被回滚 | 当前方向无效，需人工干预 |
| 失败分类为 `knowledge-gap` 且缺少继续判断的证据 | 记录缺口，等待用户提供样本 |
| 下一个有用改动需要修改允许范围之外的文件 | 如需改 agents/、codegraph 配置等，报告给用户 |
| 合规门禁连续两轮触发 REVERT | 说明基础流程被破坏，需人工修复 |

停止时输出：
1. 最终分数与基准分对比
2. 所有 KEEP 改动的汇总（文件 + 改动描述）
3. 未解决的 knowledge-gap 列表
4. 建议的下一步人工审核项

---

## 与 evolve（IIDP skill 进化）的关键差异

| 维度 | evolve | code-index-evolve |
|---|---|---|
| 基准输入 | 需求文档（PRD/SRS） | 样本代码仓库（已知 ground truth） |
| 验证执行 | Docker + JSON-RPC 冒烟测试 | codegraph 采集 + Agent 评审 + 字段溯源 |
| "冒烟"等价物 | 应用能跑、JSON-RPC 通 | 接口数覆盖率 + 字段 MATCH 率 |
| 主评分指标 | 冒烟测试通过率（35分） | 接口文档完整性（30分）+ 可追溯性（25分） |
| Phase 4 回写目标 | skills/create-project/ backend/ frontend/ | skills/code-index/ |
| 失败分类 | route-gap / backend-doc-gap / knowledge-gap | phase-c-gap / prompt-gap / variable-gap 等 |
