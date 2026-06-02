# Skills 使用指南

本目录的 Markdown 文件是给 AI Agent 的操作说明，同时也是开发者的使用手册。在对话里 **@ 引用对应文件** 或把路径写进项目规则（如 `CLAUDE.md`），Agent 会按文档执行。

---

## 快速开始

| 我想做什么 | 对应指南 |
|---|---|
| 把已有代码逆向成规格书 | [code-index 使用指南](docs/01-code-index-guide.md) |
| 根据需求生成 IIDP 应用代码 | [create-project 使用指南](docs/02-create-project-guide.md) |
| 自动优化 AI 生成质量 | [evolve 进化系统指南](docs/03-evolve-guide.md) |
| 了解后端/前端规范 | [backend/frontend 参考指南](docs/04-backend-frontend-guide.md) |

---

## Skills 目录结构

```
skills/
├── code-index/        # 存量项目逆向规格书（任意语言 → SRS/HLD/API 文档/HTML 原型）
├── create-project/    # SDD 规格驱动开发（IIDP 应用生成，14 条命令）
├── evolve/            # 打分驱动的 Skill 进化修复系统（6 维 Rubric + Docker 冒烟验证）
├── backend/           # IIDP 后端能力域（被 create-project 调用）
└── frontend/          # IIDP 前端能力域（被 create-project 调用）
```

---

## evolve 进化系统指南

`evolve` 是 `skills/create-project` 的自进化评测框架，通过固定基准 + 100 分打分 + Hill-Climbing 循环，自动发现并改进 `create-project` 的指导缺口。

### 核心机制

| 机制 | 说明 |
|---|---|
| **固定基准** | 始终使用同一仓库（`ruoyi-vue-pro`）的同一 commit SHA 评分，确保结果可比较 |
| **源码转规格** | `code-index` 将基准源码转为 SRS、用户故事、API 文档、数据库结构和测试用例 |
| **IIDP 应用生成** | `create-project` 根据规格书生成完整 SDD 产物和 IIDP 应用代码 |
| **Docker 冒烟验证** | 启动依赖服务，构建并运行生成应用，执行 JSON-RPC 冒烟用例 |
| **Git Ratchet** | 每轮改动先 commit 再打分：Δ > 0 保留，Δ ≤ 0 回滚 |
| **人工审核门控** | 分数提升创建 PR，但不自动合并 |

### 6 维评分 Rubric（满分 100）

| 维度 | 分值 |
|---|---:|
| 需求还原质量 | 20 |
| IIDP SDD 完整性 | 20 |
| 生成应用可运行性 | 20 |
| Docker 环境一致性 | 15 |
| 冒烟测试通过率 | 20 |
| 证据链与可审查性 | 5 |

### 使用命令

```bash
/evolve                       # 完整运行（基准 + 样本池 + 改进循环）
/evolve --eval-only           # 仅评分，不修改任何文件
/evolve --max-rounds 3        # 限制最多 3 轮 Hill-Climbing
/evolve --sample <repo-url>   # 指定额外样本仓库
/evolve --refresh-baseline    # 重新记录基准 commit SHA
```

### 硬边界

- 自动改进阶段**只能修改** `skills/create-project/` 下的文件。
- 每轮只做**一个小改动**，第二个问题留到下一轮。
- 样本池结果仅用于生成假设，是否保留改动只由固定基准分决定。
- **永远不自动合并** evolve 分支。

详细说明见 [evolve 使用指南](docs/03-evolve-guide.md)。

---

## 安装

```bash
# 注册 create-project 命令（14 条 /sdd-* 命令）
mkdir -p .claude/commands
cp skills/create-project/commands/sdd-*.md .claude/commands/

# 安装所有 Skills 上下文文件
mkdir -p .claude/skills
cp -r skills/* .claude/skills/
```

安装 codegraph（code-index 依赖）：

```bash
node skills/code-index/scripts/install-codegraph.js /path/to/your-project
```

详细说明见各章节指南。
