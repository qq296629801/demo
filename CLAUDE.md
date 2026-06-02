# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 项目定位

IIDP（Intelligent Infrastructure Development Platform）AI 辅助开发提示词仓库。存放面向 AI Agent 的技能文档（`skills/`），帮助 Agent 生成 IIDP 后端 Java 工程和前端扩展代码。本仓库本身不含可运行的应用代码。

---

## 交互规则

- **语言**: 始终使用中文回复
- **思考逻辑**: 在给出代码前，先用两句话简述实现思路
- **精简模式**: 除非我要求，否则不需要解释基础语法
- **修正**: 如果我提出的需求违反了现有的架构，请直接指出并建议更好的方案
- **代码注释**: 代码注释和日志输出统一使用中文


## Maven 构建命令

```bash
# 编译所有业务 app 模块（需私服可访问）
mvn -s ./settings.xml -pl sie-iidp-demo-apps -am clean package

# 跳过测试编译
mvn -s ./settings.xml -DskipTests clean package

# 启动引擎
mvn spring-boot:run -pl sie-iidp-demo-start

# Docker Compose 本地启动
docker compose up -d --build
```

Maven 私服配置在根目录 `settings.xml`，镜像地址 `http://192.168.168.156:8081/repository/maven-public/`。

---

## 其他文件说明

| 文件 | 用途 |
|---|---|
| `AGENTS.md` | Codex Agent 交互规则（与 CLAUDE.md 内容一致） |
| `big-project-dev-guide.md` | Cursor 与 Claude Code 大型项目最佳实践参考 |
| `build-guide.md` | Claude Code 安装搭建教程 |
| `.mcp.json` | Pencil 设计工具 MCP 服务器配置 |

<!-- IIDP-SDD START -->
<!-- IIDP-SDD END -->
