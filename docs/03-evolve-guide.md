# evolve 使用指南

`evolve` 是 `skills/create-project` 的自进化评测框架：固定基准仓库 → 源码转规格书 → 生成 IIDP 应用 → Docker 冒烟测试 → 100 分评分 → Hill-Climbing 改进 → 人工审核。

---

## 目录

1. [核心机制](#核心机制)
2. [硬边界](#硬边界)
3. [使用命令](#使用命令)
4. [运行流程](#运行流程)
5. [评分 Rubric](#评分-rubric)
6. [冒烟验证](#冒烟验证)
7. [样本池](#样本池)
8. [证据文件](#证据文件)
9. [常见问题](#常见问题)

---

## 核心机制

`evolve` 将 autoresearch 循环适配到 skill 维护：

| Autoresearch 思路 | evolve 对应机制 |
|---|---|
| 固定训练环境 | 固定基准仓库和 commit SHA |
| 一个可编辑训练文件 | 改进阶段只允许编辑 `skills/create-project/` |
| 一个验证指标 | 一个 100 分基准评分 |
| Git 作为记忆 | 每次实验先 commit 再测量 |
| 保留胜利、丢弃失败 | 分数提升则保留，未提升则回滚 |
| 人类审核 | 人工审核 skill 指导和最终 PR |

循环必须优化可测量的 `create-project` 行为，而不是优化评测框架本身。

---

## 硬边界

- **固定基准仓库**：`https://github.com/YunaiV/ruoyi-vue-pro.git`
- 首次运行时记录基准 commit SHA；后续所有比较必须复用同一个 SHA，除非用户明确要求刷新基准。
- 自动改进阶段**只能修改** `skills/create-project/` 下的文件。
- 不得修改 `skills/code-index/`、`skills/evolve/`、样本仓库、生成的 IIDP 应用、Docker 基础设施或测试产物来强行提分。
- 每一轮只能做**一个小而可审查的改动**。第二个问题放到下一轮。
- 样本池结果只用于指导假设；是否保留改动只由固定基准分决定。
- 分数提升则保留分支等待人工审核；分数未提升必须回滚。
- **永远不要自动合并 evolve 分支。**

---

## 使用命令

```bash
/evolve                          # 完整运行（基准 + 样本池 + 改进循环）
/evolve --eval-only              # 仅对当前 create-project 评分，不修改任何文件
/evolve --max-rounds 3           # 限制最多 3 轮 Hill-Climbing
/evolve --sample <repo-url>      # 指定额外样本仓库
/evolve --refresh-baseline       # 重新记录基准 commit SHA
```

---

## 运行流程

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

1. 克隆或复用固定基准仓库，checkout 已记录的基准 SHA。
2. 使用 `code-index` 执行源码 → 规格书生成（必须产出 SRS、用户故事、API 文档、数据库结构、测试用例或验收标准）。
3. 使用 `create-project` 根据规格书生成 IIDP SDD 产物和 IIDP 应用。
4. 执行 Docker 配置一致性检查。
5. 从用户故事和测试用例生成冒烟测试，并执行 JSON-RPC 冒烟验证。
6. 用 Rubric 打分，保存基准分和证据。

基准分是后续所有改动的**唯一接收阈值**。

**🔴 CHECKPOINT**：展示基准评分表（6 维得分 + 总分）和失败摘要，等用户确认后继续。

### Phase 2：探索样本池

使用样本仓库发现 `create-project` 的薄弱点：

- 优先使用用户提供的样本。
- 如果用户要求，使用 websearch 每轮最多发现 50 个后端管理框架仓库。
- 过滤不可访问、没有源码、无关或许可证不清晰的仓库。
- 对每个接收的样本，记录 URL、commit SHA、框架说明、规格生成质量、IIDP 生成失败、冒烟测试失败和疑似 `create-project` 缺口。

样本池分数**不能**作为保留或拒绝 `create-project` 改动的依据。

### Phase 3：改进 `create-project`

创建隔离分支（默认命名：`evolve/create-project-YYYYMMDD-HHMM`），每轮执行：

```
1. 从基准或样本池中选择一个失败模式
2. 用一句话说明改进假设
3. 只修改 skills/create-project/ 下的一小段内容
4. 提交改动（message: "evolve: improve create-project <short reason>"）
5. 使用相同基准 SHA 和相同环境重新运行固定基准
6. 用 Rubric 打分，比较 new_score 与 previous_score：
   - new_score > previous_score → KEEP，更新基准分，追加证据
   - new_score ≤ previous_score → REVERT，记录失败原因
```

**🔴 CHECKPOINT**：每轮展示 diff + 分数变化（before/after + 决策原因），等用户确认继续下一轮。

**Early Stop 条件**（满足任一即停止）：

- 达到用户指定的最大轮数。
- 连续两轮保留改动的提升都小于 2 分。
- 连续三个尝试性改动都被回滚。
- Docker 或外部基础设施不可用，导致无法进行可比较评分。
- 下一个有用改动需要修改 `skills/create-project/` 之外的文件。

### Phase 4：人工审核门禁

**有至少 1 个 KEEP commit**：

```
→ 保留 evolve 分支
→ 汇总分数变化、变更文件、基准 SHA、测试日志和样本池证据
→ 如果用户要求，创建 PR（base=目标分支, head=evolve/create-project-xxx）
→ PR title: "evolve: +{Δ}pt ({from}→{to}) - {主要改动摘要}"
→ PR body: before/after 分数表 + 证据链 + <details> 折叠完整证据文档
→ 等待人工审核，不自动合并
```

**没有任何 KEEP commit**：

```
→ 回滚所有失败实验 commit
→ 返回原始分支
→ 报告观察到的失败模式和未保留改动的原因
```

---

## 评分 Rubric

总分 **100 分**，6 个维度：

| 维度 | 分值 | 衡量内容 |
|---|---:|---|
| 需求还原质量 | 20 | `code-index` 是否从源码文档化模块、用户故事、API、数据模型和验收标准 |
| IIDP SDD 完整性 | 20 | `create-project` 是否生成 requirements、contracts、backend-spec、frontend-spec、tasks 和 validation |
| 生成应用可运行性 | 20 | Maven 构建、配置文件、打包和应用启动 |
| Docker 环境一致性 | 15 | 账号、密码、端口、数据库、Redis、MinIO 配置在所有必需文件中是否一致 |
| 冒烟测试通过率 | 20 | 用户故事 JSON-RPC 冒烟用例通过率：`round(20 × passed / total)` |
| 证据链与可审查性 | 5 | 日志、diff、评分表、commit SHA 和失败原因是否完整 |

### 门禁规则

- Docker 环境一致性失败 → Docker 维度记 0 分，且本轮不能报告冒烟测试通过。
- 应用无法启动 → 生成应用可运行性最高 8 分，冒烟测试通过率记 0 分。
- 未还原出用户故事或测试用例 → 需求还原质量最高 10 分，冒烟测试通过率最高 5 分。
- 改进修改了 `skills/create-project/` 之外的文件 → 即使分数提升，该实验也**无效**。
- 只能比较同一基准 commit SHA 和同一本地环境下产生的分数。

### 接收决策

```
new_benchmark_score > previous_benchmark_score
```

分数相等不接收。仅在样本池观察到的提升不足以接收改动。

---

## 冒烟验证

### 流程

对每个被评测仓库：

1. 从还原出的用户故事和测试用例派生 JSON-RPC 冒烟用例。
2. 启动前比较 Docker 配置与生成应用配置。
3. 启动依赖服务：`docker compose up -d mysql redis minio minio-init`。
4. 用项目标准 Maven 命令构建生成应用。
5. 启动 IIDP 应用容器或本地进程。
6. 运行 `tests/functional/smoke_test.py` 或等价 JSON-RPC runner。

### 配置一致性检查文件

| 文件 |
|---|
| `docker-compose.yml` |
| `docker/config/application.properties` |
| `docker/config/application-dev.properties` |
| `docker/config/dbcp.properties` |
| 生成应用的 `application*.properties`、`application*.yml`、`.env`、模块专用配置 |

必需一致项：MySQL（host、port、database、username、password）、Redis（host、port、password、database index）、MinIO（endpoint、access key、secret key、bucket）、App（port、active profile、context path、JSON-RPC endpoint）。

### 失败分类

| 类型 | 含义 |
|---|---|
| `spec-gap` | 需求或用户故事没有保留源码行为 |
| `generation-gap` | `create-project` 未生成必需 IIDP 产物或代码 |
| `config-gap` | Docker 与生成应用配置不一致 |
| `startup-gap` | 依赖或应用无法启动 |
| `smoke-gap` | 应用已启动，但 JSON-RPC 行为失败 |
| `environment-gap` | 本地 Docker、网络、Maven 或外部依赖导致无法可比较评测 |

通常只有 `generation-gap`、可复现的 `config-gap` 和 `smoke-gap` 应驱动 `create-project` 修改。`environment-gap` 应停止比较，而非产出误导性分数。

---

## 样本池

### 来源

- 用户提供的仓库 URL。
- 已存在的本地仓库。
- 通过 websearch 发现的后端管理框架仓库（每轮最多 50 个候选）。

### 过滤规则

接收条件：仓库可访问、许可证可见（或用户授权）、包含后端源码、确实是后端管理框架、可记录 commit SHA。

拒绝条件：无法克隆、没有清晰源码树、与后端管理系统无关、缺少有效历史、许可证不清晰且用户未授权。

### 样本记录格式（写入 `evolve-evidence.md`）

```markdown
### 样本：<name>

- URL: <repo-url>
- Commit SHA: <sha>
- License: <license 或 unknown>
- Stack: <框架/语言说明>
- Accepted: yes/no
- Filter reason: <接收或拒绝原因>
- code-index result: <规格质量摘要>
- create-project result: <生成摘要>
- Docker/smoke result: <通过/失败摘要>
- Suspected create-project gap: <一句话说明>
```

---

## 证据文件

两个本地文件，**除非用户明确要求，否则不提交 git**：

### `evolve-evidence.md`

每轮记录完整证据链：

```markdown
## 第 <n> 轮：<hypothesis>

- 基准仓库：https://github.com/YunaiV/ruoyi-vue-pro.git
- 基准 SHA：<sha>
- 实验 commit：<sha>
- 可编辑范围已检查：only skills/create-project/

### 失败模式
<基准或样本池证据>

### 改动
<文件和简洁 before/after 摘要>

### 评分

| 指标 | Previous | New | Delta |
|---|---:|---:|---:|
| 需求还原质量 | 0 | 0 | 0 |
| IIDP SDD 完整性 | 0 | 0 | 0 |
| 生成应用可运行性 | 0 | 0 | 0 |
| Docker 环境一致性 | 0 | 0 | 0 |
| 冒烟测试通过率 | 0 | 0 | 0 |
| 证据链与可审查性 | 0 | 0 | 0 |
| **Total** | 0 | 0 | 0 |

### 决策
KEEP ✅ / REVERT ❌ — <原因>
```

### `evolve-results.tsv`

每行一个基准/样本/轮次，12 列：

```
timestamp | branch | round | dimension | old_score | new_score | delta | status | file_changed | commit_short | judge_diagnosis | change_reason
```

---

## 完成检查清单

- [ ] 已记录基准仓库 URL 和 commit SHA
- [ ] `code-index` 规格书包含 SRS、用户故事、API、数据库、测试或验收标准
- [ ] `create-project` 已生成必需 SDD 产物和 IIDP 应用文件
- [ ] Docker 账号密码、数据库名、Redis/MinIO 设置、应用端口在所有配置文件中一致
- [ ] 冒烟测试可追溯到还原出的用户故事和测试用例
- [ ] 评分使用 `references/evaluation-rubric.md`
- [ ] 任何保留的改动只修改 `skills/create-project/`
- [ ] 失败改动已回滚并记录原因
- [ ] 提升分数的改动保留在分支上等待人工审核，未自动合并

---

## 常见问题

**Q：为什么基准仓库不能随意更换？**  
A：所有轮次的分数必须在同一基准 SHA 和同一环境下才可比较。切换仓库相当于换了测量尺，历史分数失去参考意义。

**Q：样本池里某个仓库分数提升了，能保留改动吗？**  
A：不能。样本池结果只用于生成假设，是否保留只由固定基准分决定。

**Q：什么情况下触发 Early Stop？**  
A：连续两轮提升 < 2 分（局部最优）、连续三次 REVERT、Docker 基础设施不可用、或下一个改动需要超出 `skills/create-project/` 范围。

**Q：PR 会自动合并吗？**  
A：不会。系统强制要求人工审核，evolve 只创建 PR，不执行合并。这是为了保证人工能审查完整证据链后再决定是否合并。

**Q：每次运行会影响原始分支吗？**  
A：不会。所有实验在 `evolve/create-project-YYYYMMDD-HHMM` 独立分支上进行。分数未提升时该分支会被丢弃，原始分支保持不变。

**Q：`environment-gap` 导致 Docker 无法启动怎么办？**  
A：停止本轮比较，记录 `environment-gap`，不产出评分。修复基础设施问题后重新运行，避免产出误导性分数。
