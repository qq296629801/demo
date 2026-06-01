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
├── evolve/            # 打分驱动的 Skill 进化修复系统（10 维 Rubric + 独立法官）
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
