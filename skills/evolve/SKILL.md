---
name: skill-evolve
description: "IIDP create-project Skill 进化修复系统：以端到端验证通过率（smoke_test.py）为主指标的 Hill-Climbing 循环——需求文档（OSS 动态提取 / code-index 输出）→ 运行完整 create-project 流程 → sdd-validation 校准（静态检查 + mvn test + smoke_test.py）→ 通过率提升则保留 commit + 创建 PR（人工审核）→ 未提升则丢弃分支。Rubric（D1-D10/F1-F4，14维140pt）仅作失败 TC 诊断工具，不再是主评分指标。触发词：进化 skill / skill 打分 / skill 质量 / skill-evolve / optimize create-project / skill 优化"
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
/skill-evolve --oss-url http://dashboard.yudao.iocoder.cn   # 从 Yudao Cloud 动态提取需求，运行完整校准
/skill-evolve --requirements specs/my-project               # 使用 code-index 已生成的需求文档
/skill-evolve --oss-url <url> --max-rounds 3                # 限制最多 3 轮
/skill-evolve --oss-url <url> --eval-only                   # 只做基线校准，不修改 skill 文档
```

---

## 核心命令

| 命令 | 描述 |
|---|---|
| `/skill-evolve` | 主命令：完整进化循环（评分 → 修复 → 重评 → PR 或丢弃） |

参考文件：
- `references/oss-validation-guide.md` — Playwright MCP 访问 OSS Demo 提取需求实体的标准流程
- `references/skill-rubric.md` — 14 维诊断 rubric（D1-D10/F1-F4），用于失败 TC 根因定位
- `references/test-scenarios.md` — 静态降级场景（无 OSS 访问时的备用需求输入）
- `references/gap-taxonomy.md` — G1-G8 Gap 分类，辅助失败 TC 根因诊断

---

## 设计原则

1. **输出驱动** — 主指标是 smoke_test.py 通过率，评的是 skill 文档修改后 AI 生成的实际产物，而非文档写法本身
2. **需求动态化** — 优先从 OSS 实时抓取需求实体，避免静态场景退化为"背答案"
3. **单处修改** — 每轮只修改 1 个 skill 文档的 1 处，保证"失败 TC → 根因 → 修改 → 效果"因果链清晰
4. **Strict Ratchet** — 通过率必须严格高于改前（Δ > 0）才保留，相等也丢弃
5. **人工守门** — 分支不能自动合并，必须人工审核 PR
6. **Rubric 降级** — D1-D10/F1-F4 rubric 只作失败 TC 的诊断工具，不驱动 KEEP/REVERT

---

## 反模式黑名单（禁止事项）

- ❌ 不得只修改 skill 文档而不重新运行 create-project 全流程（无法验证修改是否真正影响输出）
- ❌ 不得同时修改多个 skill 文档（破坏因果链，无法判断哪个修改起效）
- ❌ 不得在通过率未提升时创建 PR 或合并分支
- ❌ 不得使用 `git reset --hard`（用 `git revert` 保留历史）
- ❌ 不得自动合并 PR（必须人工审核）
- ❌ 不得跳过 Phase 2 终态完整校准（每轮只跑受影响模块，累计误差需终态全量校准修正）
