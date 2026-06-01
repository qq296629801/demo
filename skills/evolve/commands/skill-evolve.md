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

创建以下两个本地文件（均不提交 git）：

**`skill-evolve-results.tsv`**（每轮打分记录，可机器解析）：

```
timestamp	branch	round	dimension	old_score	new_score	delta	status	file_changed	commit_short	judge_diagnosis	change_reason
```

新增 2 列说明：
- `judge_diagnosis`：法官重评后对该维度的 1 句核心诊断（为什么分数变了）
- `change_reason`：本轮修改的 1 句理由（为什么选这个改法）

**`skill-evolve-evidence.md`**（人类可读的完整证据链，供 PR body 引用）：

```markdown
# Skill 进化证据链

**分支**：${EVOLVE_BRANCH}
**基线分**：[待填入]/100
**开始时间**：[ISO 8601 timestamp]

---
```

**步骤 0.3 — 召唤两个独立法官 sub-agent（并行）**

> 关键：两个 sub-agent 独立运行，不共享上下文，防止 LLM 自评偏差（自评准确率仅 46.4%，独立法官提升到 73.8%）

向每个 sub-agent 发送 `skills/evolve/references/skill-rubric.md` 中的「独立法官 Prompt 模板」，要求：
1. 读取指定的 create-project skill 文档
2. 用 test-scenarios.md 场景 1 实际模拟 `/sdd-spec` 流程（D10 实测）
3. 按格式输出 D1-D10 各维度得分和诊断
4. 对**得分最低的 2 个维度**额外输出扩展诊断（3-5 句），包含：
   - 具体扣分位置（哪个文件哪个 § 节）
   - 1 个具体反例（如"§3 模型表第 5 行写了 `Long id`"）
   - 期望的正确写法

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

将最低分 2 个维度的扩展诊断写入 `skill-evolve-evidence.md`：

```markdown
## 基线诊断（最低分维度）

### D[X] [维度名]（法官平均 [X]/[满分]）

**法官 A 诊断**：[具体文件 + § 节 + 反例 + 期望正确写法，3-5 句]

**法官 B 诊断**：[同格式]

**综合诊断**：[两位法官共同指出的核心问题，1-2 句]

---
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

**改前快照**（在 Edit 工具修改文件之前）：

用 Read 工具读取目标修改位置上下 15 行，记录为 `BEFORE_SNAPSHOT`，写入 `skill-evolve-evidence.md`：

```markdown
## 第 [ROUND] 轮 — D[X]（[维度名]）

### 改动目标
[1-2 句：当前问题是什么，预期改成什么]

### 改前（before）
文件：`[filename]`，位置：[§节 + 行描述]

\`\`\`
[改前相关段落原文，10-15 行]
\`\`\`

### 改动理由
[法官扩展诊断中的核心问题 + 本次修改如何解决它]
```

应用修改后，将改后内容也追加写入 `skill-evolve-evidence.md`：

```markdown
### 改后（after）

\`\`\`
[改后相关段落，与改前对比]
\`\`\`
```

然后提交：

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
  → 记录到 results.tsv: [timestamp] [branch] [ROUND] D[X] [CURRENT_SCORE] [NEW_SCORE] [DELTA] keep [file] [COMMIT_HASH] [judge_diagnosis] [change_reason]
  → CONSECUTIVE_SMALL_DELTA = 0（若 DELTA >= 2）或 CONSECUTIVE_SMALL_DELTA + 1（若 DELTA < 2）

ELSE（DELTA <= 0）:
  → REVERT（丢弃本轮 commit）
  → git revert HEAD --no-edit
  → CURRENT_SCORE 不变
  → 记录到 results.tsv: [timestamp] [branch] [ROUND] D[X] [CURRENT_SCORE] [NEW_SCORE] [DELTA] revert [file] [COMMIT_HASH] [judge_diagnosis] [change_reason]
  → CONSECUTIVE_SMALL_DELTA + 1
```

无论 KEEP 还是 REVERT，将重评结果追加到 `skill-evolve-evidence.md` 当前轮节：

```markdown
### 重评结果

| 法官 | D[X] 改前 | D[X] 改后 | Δ | 重评诊断摘要 |
|---|---|---|---|---|
| 法官 C | X | X | +X/-X | [1句：为什么分数变了] |
| 法官 D | X | X | +X/-X | [1句] |
| **平均** | X | X | **+X/-X** | |

**总分变化**：[CURRENT_SCORE] → [NEW_SCORE]（Δ = [DELTA]）

### 决策：KEEP ✅ / REVERT ❌

**原因**：[KEEP → "分数提升 +[DELTA]pt，保留 commit [COMMIT_HASH]" / REVERT → "分数未提升（Δ=[DELTA]），已执行 git revert"]

---
```

[🔴 **CHECKPOINT**]：展示本轮结果（修改内容摘要 + Δ 分），等待用户确认继续。

输出格式：
```markdown
## 第 [ROUND] 轮结果

**目标维度**：D[X]（[维度名]）
**修改文件**：[filename] — [1句描述]
**评分变化**：[OLD_SCORE] → [NEW_SCORE]（Δ = [DELTA]）
**决策**：[KEEP ✅ / REVERT ❌]

### 改前
\`\`\`
[BEFORE_SNAPSHOT 核心段落]
\`\`\`

### 改后
\`\`\`
[改后对应段落]
\`\`\`

### 法官重评理由
[judge_diagnosis：为什么分数变了 / 为什么没变]
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

**步骤 2.0 — 终态完整重评（强制，不可跳过）**

> Hill-Climbing 每轮只重评被修改的维度，累计 Δ 可能存在误差。**必须在创建 PR 之前，召唤两位全新独立法官对所有修改后的文件做完整的 10 维重评 + D10 实测**，以终态分数作为 PR 的唯一数字依据。

召唤两个新的独立 sub-agent（不复用任何之前的法官），使用 `skill-rubric.md` 中的「独立法官 Prompt 模板」，要求：
1. 读取所有修改过的文件（本轮进化分支上所有 KEEP commit 涉及的文件）
2. 执行 D10 实测（用 test-scenarios.md 场景 1 套用模板，检查五项验证点）
3. 按格式输出全部 D1-D10 得分

计算两位法官平均分，得到 `FINAL_SCORE`。

**将终态评分结果追加到 `skill-evolve-evidence.md`：**

```markdown
## 终态评分验证（Phase 2 门控）

> 两位全新独立法官（法官 M / 法官 N）在所有修改完成后的完整 10 维重评结果。

### D10 实测（场景 1 五项检查）

| 检查项 | 结果 |
|---|---|
| Course.id 类型为 String | ✅/❌ |
| categoryId FK String 成对存在 | ✅/❌ |
| category @ManyToOne 成对 | ✅/❌ |
| 无手动声明审计字段 | ✅/❌ |
| sortOrder 驼峰引用有示例 | ✅/❌ |

### 终态分数表（基线 vs 终态）

| 维度 | 基线 | 终态 | Δ |
|---|---|---|---|
| D1-D10 ... | ... | ... | ... |
| **总分** | [BASELINE_SCORE] | [FINAL_SCORE] | [FINAL_SCORE - BASELINE_SCORE] |

法官 M：[分数]/100，法官 N：[分数]/100，平均：**[FINAL_SCORE]/100**
```

**重新计算总提升**：`CONFIRMED_DELTA = FINAL_SCORE - BASELINE_SCORE`

**门控判断**：
- `CONFIRMED_DELTA > 0` → 继续情况 A（创建 PR）
- `CONFIRMED_DELTA <= 0` → 即使 Hill-Climbing 过程中 Δ 为正，终态未提升也执行情况 B（丢弃分支）

> **设计原则**：PR 标题中的分数必须来自终态完整重评，不得使用累计估算值。

---

**计算确认总提升**：`CONFIRMED_DELTA = FINAL_SCORE - BASELINE_SCORE`

#### 情况 A：终态总分提升（CONFIRMED_DELTA > 0）

创建 PR：

```bash
git push -u origin ${EVOLVE_BRANCH}
```

PR 内容：

```
标题：skill-evolve: +[CONFIRMED_DELTA]pt ([BASELINE_SCORE]→[FINAL_SCORE]) - [主要改动摘要]

正文：
## 终态评分（两位独立法官完整重评确认）

| 维度 | 修改前 | 修改后 | Δ |
|---|---|---|---|
| D1 模板完整性 (/12) | X | X | +X |
| D2 字段规范性 (/12) | X | X | +X |
| ... | | | |
| **总分 (/100)** | [BASELINE] | [FINAL_SCORE] | +[CONFIRMED_DELTA] |

## D10 实测结果（场景 1，修改后验证）

[五项检查 ✅/❌ 表格]

## 证据链（逐轮 Before → After，仅展示 KEEP 的轮次）

[对每个 KEEP 轮次，从 skill-evolve-evidence.md 摘取：]

### 第 [N] 轮：D[X] [维度名]（+[Δ]pt）

**问题（法官诊断）**：[综合诊断 1-2 句]

**改前**（`[filename]` § 节位置）：
\`\`\`
[BEFORE_SNAPSHOT 核心段落]
\`\`\`

**改后**：
\`\`\`
[改后对应段落]
\`\`\`

**评分变化**：[old]/[满分] → [new]/[满分]（+[Δ]）
**法官重评理由**：[judge_diagnosis]

---

## 修改内容（共 [N] 次成功 commit）

[按时间列出每次 keep 的 commit：文件 + 1句描述 + commit hash]

## 测试场景验证

基于 test-scenarios.md 场景 1（含 ManyToOne 关系的课程管理）验证：
- [ ] ID 类型为 String（不是 Long）
- [ ] ManyToOne 有成对的 FK String 字段
- [ ] 无手动声明的审计字段
- [ ] 驼峰引用与字段声明一致

## ⚠️ 注意事项

**此 PR 需要人工审核后才能合并，不允许自动合并。**
请检查每次 commit 的 diff，确认修改符合 IIDP 平台规范。

<details>
<summary>📋 完整证据文档（skill-evolve-evidence.md 全文）</summary>

[将 skill-evolve-evidence.md 的全部内容粘贴到此处]

</details>

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
- **Phase 2 终态重评已完成**（两位全新法官 + D10 实测，五项检查全部 ✅）
- PR 标题中的分数来自 `FINAL_SCORE`（终态评分），不是 Hill-Climbing 累计估算值
- PR 已创建，URL 已输出
- `skill-evolve-results.tsv` 已记录所有轮次数据（含 `judge_diagnosis` 和 `change_reason` 列）
- `skill-evolve-evidence.md` 包含：基线诊断 + 每轮 before/after 快照 + **终态评分验证节**（已嵌入 PR body）
- 进化分支已推送到远程
- 未自动合并（等待人工审核）

**情况 B（分数未提升）**：
- 进化分支已本地删除
- 工作区已回到 `BASE_BRANCH`
- 原因报告已输出
- `skill-evolve-results.tsv` 和 `skill-evolve-evidence.md` 保留记录（不提交 git）
