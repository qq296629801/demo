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
| 维护 IIDP SDD 规格分支、Plan 和公共规格 | [create-project SDD 工作流指南](docs/05-create-project-sdd-workflow-guide.md) |

---

## 文档索引

| 文档 | 内容 |
|---|---|
| [01-code-index-guide.md](docs/01-code-index-guide.md) | 使用 `code-index` 将存量代码逆向成 Codebook、SRS、API、数据库和 UI 原型。 |
| [02-create-project-guide.md](docs/02-create-project-guide.md) | 使用 `create-project` 将业务需求转成 IIDP SDD 产物和实现任务。 |
| [03-evolve-guide.md](docs/03-evolve-guide.md) | 使用 `evolve` 对 `create-project` 进行基准评分、样本验证和改进闭环。 |
| [04-backend-frontend-guide.md](docs/04-backend-frontend-guide.md) | 查阅 IIDP 后端与前端 skill 的能力边界和协作方式。 |
| [05-create-project-sdd-workflow-guide.md](docs/05-create-project-sdd-workflow-guide.md) | 说明当前 `create-project` 下的规格分支、Plan 执行、公共规格维护和 Spec Sync 流程。 |

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
