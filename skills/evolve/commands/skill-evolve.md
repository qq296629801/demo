---
description: 【Skill 进化】对 create-project skill 文档进行打分驱动的 Hill-Climbing 修复：10 维 Rubric 评分 → 独立法官重评 → 分数提升保留 commit + 创建 PR（人工审核）→ 分数未提升丢弃分支。参考 darwin-skill rubric 和 autoresearch keep/discard 循环设计。
---

# /skill-evolve

## 用途

对 `skills/create-project/` 目录下的所有 skill 文档执行**打分驱动的自动修复循环**：

1. **Phase 0**：基线评分——两个独立 sub-agent 用 10 维 rubric 打分，找最低分维度
2. **Phase 1**：Hill-Climbing——每轮只修改 1 个维度的 1 处文档，独立重评，分数提升则 keep，否则 `git revert`
3. **Phase 2**：决策门——总分提升 → 创建 PR（人工审核，不自动合并）；总分未提升 → 丢弃分支

> **设计约束**：分支不能自动合并到 dev。PR 创建后必须等待人工审核。

## 用户输入

```text
$ARGUMENTS
```

可选参数：
- `--max-rounds N`：最多 N 轮 hill-climbing（默认 5）
- `--eval-only`：只运行 Phase 0 评分，不修改任何文件
- `--target-branch <branch>`：指定 PR 的目标分支（默认 `claude/practical-cray-ZIZ7P`，当 dev 分支就绪后改为 `dev`）

## 前置检查

1. 确认当前工作目录是 git 仓库（`git status` 不报错）
2. 确认 `skills/create-project/` 目录存在
3. 确认 `skills/evolve/references/skill-rubric.md` 和 `test-scenarios.md` 可读
4. 读取当前分支名，记录为 `BASE_BRANCH`

---

## 执行步骤

### Phase 0：建立基线评分

**步骤 0.1 — 创建进化分支**

```bash
TIMESTAMP=$(date +%Y%m%d-%H%M)
EVOLVE_BRANCH="skill-evolve/${TIMESTAMP}"
git checkout -b ${EVOLVE_BRANCH}
```

**步骤 0.2 — 初始化结果记录**

创建 `skill-evolve-results.tsv`（不提交 git，参考 autoresearch 的 results.tsv 设计）：

```
timestamp	branch	round	dimension	old_score	new_score	delta	status	file_changed	commit_short
```

**步骤 0.3 — 召唤两个独立法官 sub-agent（并行）**

> 关键：两个 sub-agent 独立运行，不共享上下文，防止 LLM 自评偏差（自评准确率仅 46.4%，独立法官提升到 73.8%）

向每个 sub-agent 发送 `skills/evolve/references/skill-rubric.md` 中的「独立法官 Prompt 模板」，要求：
1. 读取指定的 create-project skill 文档
2. 用 test-scenarios.md 场景 1 实际模拟 `/sdd-spec` 流程（D10 实测）
3. 按格式输出 D1-D10 各维度得分和诊断

**步骤 0.4 — 计算基线分**

对两个法官的各维度得分取平均（四舍五入），得到 `BASELINE_SCORE`（总分/100）。

输出基线报告：

```markdown
## 基线评分报告

| 维度 | 法官A | 法官B | 平均 | 诊断摘要 |
|---|---|---|---|---|
| D1 模板完整性 (/12) | X | X | X | [1句] |
| D2 字段规范性 (/12) | X | X | X | [1句] |
| D3 平台合规性 (/12) | X | X | X | [1句] |
| D4 失败机制编码 (/10) | X | X | X | [1句] |
| D5 可操作性 (/10) | X | X | X | [1句] |
| D6 ER 关系设计 (/10) | X | X | X | [1句] |
| D7 约束执行力 (/10) | X | X | X | [1句] |
| D8 跨步骤一致性 (/8) | X | X | X | [1句] |
| D9 测试可验性 (/8) | X | X | X | [1句] |
| D10 实测表现 (/8) | X | X | X | [1句] |
| **总分** | | | **XX/100** | |

**最低分维度**：D[X]（[维度名]，[分数]/[满分]）
**次低分维度**：D[X]（[维度名]，[分数]/[满分]）
```

[🔴 **CHECKPOINT**]：展示基线报告，等待用户确认后进入 Phase 1。

若用户指定了 `--eval-only`，到此结束，不进入 Phase 1。

---

### Phase 1：Hill-Climbing 修复循环

**进入条件**：用户确认了基线报告

**初始化**：
```
CURRENT_SCORE = BASELINE_SCORE
CONSECUTIVE_SMALL_DELTA = 0
ROUND = 0
MAX_ROUNDS = 5（或用户指定值）
```

**LOOP（ROUND < MAX_ROUNDS）**：

#### 步骤 1.1 — 确定本轮修改目标

找当前最低分维度（已用过的维度若本轮分数提升后降为最低，可再次选择；但同一轮内不重复）。

查表 `skills/evolve/references/skill-rubric.md` 的「维度与文件映射」，确定对应的 skill 文件。

输出：
```
本轮目标：D[X]（[维度名]）当前 [分数]/[满分]
对应文件：skills/create-project/references/[filename].md
修改思路：[1-2句具体说明改哪里、改成什么]
```

#### 步骤 1.2 — 生成并应用 1 个具体修改

**约束**：
- 只修改 1 个文件中的 1 处
- 修改必须是增量（添加内容、明确禁令、完善示例），不是大幅重写
- 如果目标是 D2（字段规范性）：在 sdd-backend.md 的模型设计表中增强 ManyToOne 双字段示例
- 如果目标是 D4（失败机制）：在某个 command 文件中把"注意..."改为"若 X 则 Y，否则 Z"的显式分支
- 如果目标是 D5（可操作性）：扫描并替换"建议/可以考虑/根据情况"等软化措辞为具体指令

应用修改后：

```bash
git add [修改的文件]
git commit -m "evolve: fix D${X} - [一句话描述改动]"
COMMIT_HASH=$(git rev-parse --short HEAD)
```

#### 步骤 1.3 — 召唤新的两个独立法官重评（不复用 Phase 0 的法官）

> 必须是全新 sub-agent，避免锚定偏差（上一轮的评分会影响下一轮）

只需评 D[X]（本轮修改的维度）和总分，其余维度沿用上轮分数（节省 token）。

例外：如果修改了 `sdd-backend.md`，D2/D3/D6/D8 可能联动变化，需对这几个维度也重评。

#### 步骤 1.4 — Ratchet 决策

计算新总分 `NEW_SCORE` 和增量 `DELTA = NEW_SCORE - CURRENT_SCORE`：

```
IF DELTA > 0:
  → KEEP（保留 commit）
  → CURRENT_SCORE = NEW_SCORE
  → 记录到 results.tsv: [timestamp] [branch] [ROUND] D[X] [CURRENT_SCORE] [NEW_SCORE] [DELTA] keep [file] [COMMIT_HASH]
  → CONSECUTIVE_SMALL_DELTA = 0（若 DELTA >= 2）或 CONSECUTIVE_SMALL_DELTA + 1（若 DELTA < 2）

ELSE（DELTA <= 0）:
  → REVERT（丢弃本轮 commit）
  → git revert HEAD --no-edit
  → CURRENT_SCORE 不变
  → 记录到 results.tsv: [timestamp] [branch] [ROUND] D[X] [CURRENT_SCORE] [NEW_SCORE] [DELTA] revert [file] [COMMIT_HASH]
  → CONSECUTIVE_SMALL_DELTA + 1
```

[🔴 **CHECKPOINT**]：展示本轮结果（修改内容摘要 + Δ 分），等待用户确认继续。

输出格式：
```markdown
## 第 [ROUND] 轮结果

**修改**：[文件名] — [1句描述]
**评分变化**：[CURRENT_SCORE] → [NEW_SCORE]（Δ = [DELTA]）
**决策**：[KEEP ✅ / REVERT ❌]

[如果 KEEP，展示 git diff 摘要]
[如果 REVERT，说明原因]
```

#### 步骤 1.5 — Early Stop 检查

```
IF CONSECUTIVE_SMALL_DELTA >= 2:
  → 停止循环（连续 2 轮 Δ < 2pt，已到局部最优）
  → 输出：Early Stop — 连续 {N} 轮边际收益 < 2pt

IF ROUND >= MAX_ROUNDS:
  → 停止循环（达到最大轮数）
```

**ROUND += 1，继续循环**

---

### Phase 2：决策门

**计算总提升**：`TOTAL_DELTA = CURRENT_SCORE - BASELINE_SCORE`

#### 情况 A：总分提升（TOTAL_DELTA > 0）

创建 PR：

```bash
git push -u origin ${EVOLVE_BRANCH}
```

PR 内容：

```
标题：skill-evolve: +[TOTAL_DELTA]pt ([BASELINE_SCORE]→[CURRENT_SCORE]) - [主要改动摘要]

正文：
## 评分变化

| 维度 | 修改前 | 修改后 | Δ |
|---|---|---|---|
| D1 模板完整性 (/12) | X | X | +X |
| D2 字段规范性 (/12) | X | X | +X |
| ... | | | |
| **总分 (/100)** | [BASELINE] | [CURRENT] | +[TOTAL_DELTA] |

## 修改内容（共 [N] 次成功 commit）

[按时间列出每次 keep 的 commit：文件 + 1句描述]

## 测试场景验证

基于 test-scenarios.md 场景 1（含 ManyToOne 关系的课程管理）验证：
- [ ] ID 类型为 String（不是 Long）
- [ ] ManyToOne 有成对的 FK String 字段
- [ ] 无手动声明的审计字段
- [ ] 驼峰引用与字段声明一致

## ⚠️ 注意事项

**此 PR 需要人工审核后才能合并，不允许自动合并。**
请检查每次 commit 的 diff，确认修改符合 IIDP 平台规范。

https://claude.ai/code/session_01W68642RteUKXT5UwES3bBp
```

输出 PR URL，等待人工审核。

#### 情况 B：总分未提升（TOTAL_DELTA <= 0）

```bash
# 切回基础分支，删除进化分支
git checkout ${BASE_BRANCH}
git branch -D ${EVOLVE_BRANCH}
```

输出原因报告：

```markdown
## 进化循环结束 — 分支已丢弃

**原因**：经过 [ROUND] 轮修复，总分未提升（基线 [BASELINE_SCORE] → 最终 [CURRENT_SCORE]，Δ = [TOTAL_DELTA]）

**尝试过的修改**：
[列出每次 revert 的维度和原因]

**建议**：
- 考虑手动分析最低分维度（D[X]），可能需要更大幅度的文档重构
- 或使用 `/skill-evolve --eval-only` 重新查看当前评分，再人工规划改进方向
```

---

## 完成标志

**情况 A（分数提升）**：
- PR 已创建，URL 已输出
- `skill-evolve-results.tsv` 已记录所有轮次数据
- 进化分支已推送到远程
- 未自动合并（等待人工审核）

**情况 B（分数未提升）**：
- 进化分支已本地删除
- 工作区已回到 `BASE_BRANCH`
- 原因报告已输出
- `skill-evolve-results.tsv` 保留记录（不提交 git）
