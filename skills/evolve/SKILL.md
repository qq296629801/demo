---
name: skill-evolve
description: "IIDP create-project Skill 进化修复系统：用打分驱动的 Hill-Climbing 循环自动检测 create-project skill 文档的质量缺陷，以 14 维 Rubric 评分（140 分，后端 D1-D10 共 100pt + 前端 F1-F4 共 40pt），每轮只修改最低分维度，独立法官重评，分数提升则保留 commit 并申请 PR 供人工审核合并，分数未提升则丢弃分支。参考 darwin-skill（9 维 rubric + git ratchet）和 autoresearch（keep/discard 循环）设计。触发词：进化 skill / skill 打分 / skill 质量 / skill-evolve / optimize create-project / skill 优化"
---

# Skill Evolve — create-project 自动进化修复系统

> 设计灵感：
> - **darwin-skill**（alchaincyf）：9 维 rubric + 独立法官 + git ratchet + 人工 checkpoint
> - **autoresearch**（Karpathy）：单一指标驱动的 keep/discard 循环，修改单文件，指标不提升就丢弃
>
> 核心约束：分数未提升 → 丢弃分支；分数提升 → 创建 PR，**人工审核才能合并到 dev，不允许自动合并**。

---

## 使用方式

```
/skill-evolve                          # 优化 create-project skill，最多 5 轮
/skill-evolve --max-rounds 3           # 限制最多 3 轮
/skill-evolve --eval-only              # 只评分，不修改
```

---

## 核心命令

| 命令 | 描述 |
|---|---|
| `/skill-evolve` | 主命令：完整进化循环（评分 → 修复 → 重评 → PR 或丢弃） |

参考文件：
- `references/skill-rubric.md` — 14 维评分 rubric + 独立法官 prompt 模板（后端 D1-D10 / 前端 F1-F4）
- `references/test-scenarios.md` — 标准测试场景（场景 1-4 后端实测，场景 5 前端实测）

---

## 设计原则

1. **单维修改** — 每轮只修改 1 个维度对应的 1 处文档，保证因果链清晰
2. **独立法官** — 两个 sub-agent 各自独立打分，取平均，防止 LLM 自评偏差（自评准确率仅 46.4%）
3. **Strict Ratchet** — 新分必须严格高于旧分（Δ > 0）才保留，相等也丢弃
4. **人工守门** — 分支不能自动合并到 dev，必须人工审核 PR
5. **Early Stop** — 连续 2 轮 Δ < 2pt 则停止（局部最优，继续也无意义）

---

## 反模式黑名单（禁止事项）

- ❌ 不得用同一个 agent 既修改又打分（自评）
- ❌ 不得同时修改多个维度（破坏因果链）
- ❌ 不得在分数未提升时创建 PR 或合并分支
- ❌ 不得使用 `git reset --hard`（用 `git revert` 保留历史）
- ❌ 不得自动合并 PR（必须人工审核）
- ❌ 不得跳过后端测试场景验证（D10，场景 1-4，不能省略）
- ❌ 不得跳过前端测试场景验证（F1-F4，场景 5，不能省略）
