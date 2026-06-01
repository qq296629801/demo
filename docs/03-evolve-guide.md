# evolve 进化修复系统使用指南

打分驱动的 `create-project` skill 自动优化系统：10 维 Rubric 评分 → Hill-Climbing 修复 → 人工审核合并。

---

## 目录

1. [核心机制](#核心机制)
2. [使用命令](#使用命令)
3. [运行流程](#运行流程)
4. [证据链输出](#证据链输出)
5. [10 维 Rubric 快速参考](#10-维-rubric-快速参考)
6. [常见问题](#常见问题)

---

## 核心机制

### 独立法官模式

两个独立 sub-agent 分别打分，取平均值。这来自实测数据：LLM 自评准确率仅 **46.4%**，独立法官提升到 **73.8%**（darwin-skill 研究结论）。

### Git Ratchet（棘轮机制）

每轮只修改 1 个文件的 1 处内容：
- **Δ > 0**（分数提升）→ 保留 commit，更新基准分
- **Δ ≤ 0**（分数未提升）→ `git revert`，回到上一 commit，丢弃改动

### 人工审核门控

分数提升 → 创建 PR，**但不自动合并**。PR 必须经过人工审核才能合并到目标分支。

---

## 使用命令

```bash
/skill-evolve                        # 完整运行（最多 5 轮 Hill-Climbing）
/skill-evolve --eval-only            # 仅评分，不修改任何文件
/skill-evolve --max-rounds 3         # 限制最多 3 轮
/skill-evolve --target-branch dev    # PR 目标分支（默认 claude/practical-cray-ZIZ7P）
```

---

## 运行流程

### Phase 0：基线评分

1. 创建进化分支 `skill-evolve/YYYYMMDD-HHMM`
2. 初始化 `skill-evolve-results.tsv` 和 `skill-evolve-evidence.md`（本地文件，不提交 git）
3. 两个独立法官 sub-agent 并行对 `skills/create-project/` 打 10 维分
4. 法官对**最低分 2 个维度**输出扩展诊断（3-5 句：扣分原因 + 具体反例 + 期望写法）
5. 基线诊断写入 `skill-evolve-evidence.md`

**🔴 CHECKPOINT**：展示基线分数表（10 维得分 + 总分）和扩展诊断，等用户确认后继续。

### Phase 1：Hill-Climbing 循环（最多 N 轮）

每轮执行：

```
1. 找当前最低分维度（Dx）
2. 读取目标文件改动位置上下 10-15 行（before 快照）
3. 生成 1 个针对 Dx 的具体修改方案（只改 1 个文件的 1 处）
4. 应用修改 → git commit（message: "evolve: fix D{x} - {one-line description}"）
5. 写入 before/after 代码对比到 skill-evolve-evidence.md
6. 两个新的独立法官 sub-agent 重新评分（不复用 Phase 0 的法官）
7. 判断 Δ：
   - Δ > 0 → KEEP，更新基准分，追加重评结果到证据链
   - Δ ≤ 0 → REVERT（git revert），追加 REVERT 决策到证据链
```

**🔴 CHECKPOINT**：展示 diff + 分数变化（before/after 代码块 + 法官重评理由），等用户确认继续下一轮。

**Early Stop 条件**：连续 2 轮 Δ < 2pt → 已达局部最优，停止循环。

### Phase 2：决策门

**总分有提升 且 至少 1 个成功 KEEP**：

```
→ 创建 PR：base=目标分支, head=skill-evolve/xxx
→ PR title: "skill-evolve: +{Δ}pt ({from}→{to}) - {主要改动摘要}"
→ PR body: 包含 before/after 分数表 + 证据链节 + <details> 折叠完整证据文档
→ 等待人工审核合并（不自动合并）
→ 输出 PR URL
```

**总分未提升 或 所有修改均 REVERT**：

```
→ git checkout 原始分支
→ git branch -D skill-evolve/xxx（丢弃分支）
→ 输出原因报告（每轮为何 REVERT）
```

---

## 证据链输出

### `skill-evolve-evidence.md`（本地文件，不提交 git）

每轮记录完整证据链，格式示例：

```markdown
## 第 1 轮 — D2（字段规范性）

### 改动目标
sdd-backend.md §3 erField 行缺少 FK String 字段示例，仅展示 ManyToOne 对象字段

### 改前（before）
文件：skills/create-project/references/sdd-backend.md，位置：§3 模型设计表 erField 行

[改前的相关段落原文]

### 改后（after）
[改后的段落，展示 FK String + ManyToOne 双字段模式]

### 重评结果

| 法官 | D2 改前 | D2 改后 | Δ | 改后诊断 |
|---|---|---|---|---|
| 法官 C | 7 | 9 | +2 | 增加了 FK String 字段行，AI 有完整双字段示例 |
| 法官 D | 7 | 9 | +2 | 模板现在成对出现，符合 ManyToOne 强制规范 |
| **平均** | 7 | 9 | **+2** | |

**总分变化**：76 → 78（Δ = +2）

### 决策：KEEP ✅
原因：分数提升，保留 commit abc1234
```

### `skill-evolve-results.tsv`（可机器解析）

12 列：

```
timestamp | branch | round | dimension | old_score | new_score | delta | status | file_changed | commit_short | judge_diagnosis | change_reason
```

### PR body

PR body 包含：
1. **Before/After 分数表**（10 维对比）
2. **证据链节**（每个 KEEP 轮次的 before/after 代码块 + 评分变化）
3. **`<details>` 折叠块**（完整 `skill-evolve-evidence.md` 全文）

---

## 10 维 Rubric 快速参考

| 维度 | 满分 | 评分重点 |
|---|---|---|
| **D1 模板完整性** | 12pt | 每个 Step（0-5）的输出模板是否完整，无"待补充"占位符 |
| **D2 字段规范性** | 12pt | ID 类型、审计字段、ManyToOne 双字段模式是否在模板中正确示范 |
| **D3 平台合规性** | 12pt | 模板是否符合 platform-standards.md（命名/注解/常量规范） |
| **D4 失败机制编码** | 10pt | 是否有明确的 if-then 回退路径（不能只写"注意"，要写"若X则做Y"） |
| **D5 可操作性** | 10pt | 指令是否具体可执行，无"建议考虑"等模糊措辞 |
| **D6 ER 关系设计** | 10pt | ManyToOne/OneToMany/ManyToMany 的成对声明规则是否完整 |
| **D7 约束执行力** | 10pt | 禁止事项是否有明确黑名单（"禁止手动声明审计字段"而非"避免"） |
| **D8 跨步骤一致性** | 8pt | 各 Step 文档之间字段类型/命名规范是否一致 |
| **D9 测试可验性** | 8pt | 生成的代码是否有可验证的验收标准（AC→TC 可追溯） |
| **D10 前端交互规格** | 8pt | 生成的 UI 规格是否包含状态流转/详情页/Dashboard |

完整评分标准见 `skills/evolve/references/skill-rubric.md`。

---

## 常见问题

**Q：为什么分数有时候会下降？**  
A：Git Ratchet 就是为了处理这种情况的。当 Δ ≤ 0 时，系统自动 `git revert` 丢弃该改动，分数回到上一基准。下一轮会换一个维度或换一种改法继续尝试。

**Q：什么情况下 Early Stop？**  
A：连续 2 轮 Δ < 2pt（边际收益很小）时停止，说明已达局部最优。此时如果总分有提升，仍会创建 PR；若未提升则丢弃分支。

**Q：PR 创建后能自动合并吗？**  
A：不能。系统设计上强制要求人工审核，`/skill-evolve` 只执行 `gh pr create`，不会执行 `gh pr merge`。这是为了保证人工能审查证据链，确认改动方向正确再合并。

**Q：每次运行会影响已有代码吗？**  
A：不会。每次运行都在独立的 `skill-evolve/YYYYMMDD-HHMM` 分支上，原始分支不受影响。分数未提升时，该分支会被直接删除。
