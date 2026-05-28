---
name: code-index
description: |
  把存量代码（任意语言/框架）转成完整功能规格书（Spec）。整合 codegraph 框架，
  以 codegraph_search、codegraph_callers、codegraph_trace 三条命令为核心，
  自动解析代码语义图谱，再通过 LLM Prompt 模板生成 SRS、PRD、HLA、API 文档、
  用户故事、流程图（Mermaid/SVG）、UI 静态页面、数据库结构等。
  支持 RuoYi、JEECG Boot、yudao-cloud、maku-boot 等 Java 快速开发框架的
  自动识别与模式提取。当用户提到"代码转文档"、"生成规格书"、"分析存量项目"、
  "理解老项目代码"、"code to spec"、"codegraph 分析"时必须使用本 skill。
---

# code-index — 代码转规格书 Skill

## 目录

1. [总体流程](#总体流程)
2. [第一步：安装与初始化 codegraph](#第一步安装与初始化-codegraph)
3. [第二步：三条核心命令](#第二步三条核心命令)
4. [第三步：框架自动识别](#第三步框架自动识别)
5. [第四步：规格书生成流程](#第四步规格书生成流程)
6. [输出规格书结构](#输出规格书结构)
7. [参考文件索引](#参考文件索引)

---

## 总体流程

```
代码仓库
   │
   ▼
codegraph init + index          ← tree-sitter 解析，写入 SQLite
   │
   ├─ codegraph_search          ← 全文搜索符号
   ├─ codegraph_callers         ← 找调用者（向上追踪）
   └─ codegraph_trace           ← 调用链追踪（向下展开）
   │
   ▼
框架识别 + 语义归类
   │
   ▼
LLM Prompt 模板 → 生成 Spec
   │
   ├─ SRS（需求规格）
   ├─ PRD（产品需求）
   ├─ HLA（高阶架构）
   ├─ API 文档
   ├─ 用户故事
   ├─ 流程图（Mermaid）
   ├─ UI/UX 静态页面（HTML）
   └─ 数据库结构
```

---

## 第一步：安装与初始化 codegraph

> 详细安装步骤见 `references/codegraph-setup.md`

### 快速安装

```bash
# 全局安装
npm install -g @colbymchenry/codegraph

# 进入目标项目
cd /path/to/your-project

# 初始化并建立索引（-i = index immediately）
codegraph init -i

# 查看索引状态
codegraph status
```

### 配置 MCP（Claude Code 环境）

```json
// ~/.claude.json
{
  "mcpServers": {
    "codegraph": {
      "type": "stdio",
      "command": "codegraph",
      "args": ["serve", "--mcp"]
    }
  }
}
```

### 忽略不必要文件

在 `.codegraph/config.json` 中排除 `target/`、`node_modules/`、`dist/`，
大型 Java 项目特别要排除 `**.class` 和 Maven 缓存：

```json
{
  "exclude": [
    "target/**", "node_modules/**", "dist/**", "build/**",
    ".git/**", "*.min.js", "**/*.class"
  ],
  "extractDocstrings": true,
  "trackCallSites": true
}
```

---

## 第二步：三条核心命令

> 完整输出格式与字段说明见 `references/codegraph-setup.md`

### `codegraph_search` — 符号搜索

**用途**：按名称/关键词快速定位函数、类、接口、变量  
**典型场景**：找入口点、找服务类、找 Controller

```
# MCP 工具调用（Claude Code 内）
codegraph_search(query="UserController", kind="class", limit=10)
codegraph_search(query="login", kind="function")
codegraph_search(query="SysUser")        # 搜索所有相关符号

# CLI 等价命令
codegraph query "UserController" --kind class --json
```

**输出字段**：`id`, `name`, `kind`, `file`, `line`, `signature`, `docstring`

---

### `codegraph_callers` — 向上追踪（谁调用了它）

**用途**：找某函数/方法的所有调用者，理解触发路径  
**典型场景**：理解一个 Service 方法被哪些 Controller 或定时任务触发

```
# MCP 工具调用
codegraph_callers(symbol_id="<node_id>", depth=3)
codegraph_callers(name="UserService.login")

# CLI 等价
codegraph query "UserService.login" --json | jq '.[0].id' | xargs ...
```

**输出**：调用者节点列表 + 调用位置（file:line）+ 调用边类型（direct/indirect）

---

### `codegraph_trace` — 调用链追踪（向下展开）

> codegraph 原生对应 `codegraph_callees` + `codegraph_impact`，
> 本 skill 将其封装为 **trace** 语义：从入口向下追踪完整调用链。

```
# MCP 工具调用（使用 codegraph_callees 实现 trace）
codegraph_callees(symbol_id="<node_id>", depth=5)
codegraph_impact(symbol_id="<node_id>")   # 变更影响面分析

# 组合使用：完整调用链
1. codegraph_search("OrderService.createOrder")  → 获得 node_id
2. codegraph_callees(node_id, depth=4)            → 向下展开调用树
3. codegraph_callers(node_id, depth=2)            → 向上找调用入口
```

**输出**：完整调用图 JSON，包含每层节点的 `name`、`file`、`signature`

---

## 第三步：框架自动识别

> 详细框架特征与提取规则见 `references/java-frameworks.md`

运行以下搜索序列自动识别框架：

```
codegraph_search("@RestController")     → 判断是否 Spring MVC
codegraph_search("BaseController")      → 判断 RuoYi 系
codegraph_search("JeecgBootApplication")→ 判断 JEECG Boot
codegraph_search("YudaoApplication")    → 判断 yudao-cloud
codegraph_search("MakuApplication")     → 判断 maku-boot
codegraph_search("IService")            → MyBatis-Plus 服务层
codegraph_search("BaseMapper")          → MyBatis/MyBatis-Plus Mapper
```

识别框架后，使用对应的 **框架提取规则** 理解代码分层，再生成 Spec。

---

## 第四步：规格书生成流程

> LLM Prompt 模板详见 `references/llm-prompts.md`  
> Spec 输出模板详见 `references/spec-templates.md`

### 4.1 收集原始数据（codegraph 查询阶段）

```
Step 1: codegraph_search("") → 全量符号概览（按 kind 统计）
Step 2: 识别所有 @Controller / @RestController 类 → 枚举 API 入口
Step 3: 识别所有 @Entity / @TableName 类 → 提取数据模型
Step 4: codegraph_trace 每个主要 Controller 方法 → 得到完整调用链
Step 5: codegraph_callers 核心 Service → 了解业务触发点
Step 6: 识别 @Scheduled / 事件监听 → 异步/定时流程
```

### 4.2 语义归类

将收集到的符号按业务模块归类：
- **用户权限模块** → sys_user, SysUserController, LoginController
- **业务核心模块** → 业务相关 Entity + Service + Controller
- **数据访问层** → Mapper/Repository + DB 表结构
- **公共基础设施** → 配置类、工具类、拦截器

### 4.3 输入 LLM Prompt → 生成各类 Spec

每种文档对应一个 Prompt 模板（见 `references/llm-prompts.md`）。
按如下顺序生成，后面的文档依赖前面的输出：

```
HLA → SRS → PRD → 用户故事 → API 文档 → 流程图 → 数据库结构 → UI/UX
```

---

## 输出规格书结构

每次运行本 skill，按如下结构输出完整 Spec 包：

```
spec/
├── 00-overview.md          项目概览、技术栈、框架
├── 01-hla.md               高阶架构（含 Mermaid 系统图）
├── 02-srs.md               软件需求规格
├── 03-prd.md               产品需求文档
├── 04-user-stories.md      用户故事列表
├── 05-api.md               API 规格（RESTful）
├── 06-flowcharts/          流程图（Mermaid + SVG）
│   ├── login-flow.mmd
│   ├── order-flow.mmd
│   └── ...
├── 07-database.md          数据库结构（建表语句 + ER 图）
└── 08-ui/                  界面布局（静态 HTML）
    ├── dashboard.html
    ├── user-list.html
    └── ...
```

### 输出格式规则

| 文档类型 | 格式 | 工具 |
|---------|------|------|
| SRS/PRD/HLA | Markdown | 直接写 .md |
| 流程图 | Mermaid 代码块 + SVG | `show_widget` 渲染 |
| API 文档 | OpenAPI 3.0 YAML/Markdown | 写 .md 或 .yaml |
| 数据库结构 | SQL DDL + Mermaid ER 图 | 直接输出 |
| UI/UX | 静态 HTML + Tailwind CSS | 写 .html 文件 |
| 用户故事 | Markdown 表格 | 直接写 .md |

---

## 参考文件索引

| 文件 | 何时读取 |
|-----|---------|
| `references/codegraph-setup.md` | 安装/初始化/CLI 命令完整参考 |
| `references/java-frameworks.md` | 识别到 Java 框架时必读 |
| `references/llm-prompts.md` | 生成任何 Spec 文档前必读 |
| `references/spec-templates.md` | 需要具体文档模板结构时读取 |
| `scripts/install-codegraph.sh` | 批量安装脚本 |
