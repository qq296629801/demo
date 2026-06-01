# code-index 存量项目使用指南

把已有代码（Java / Python / TypeScript / Go 等任意语言）逆向生成完整规格书（SRS、HLD、API 文档、UI 静态原型等）。

---

## 目录

1. [安装 codegraph](#安装-codegraph)
2. [核心命令速查](#核心命令速查)
3. [使用流程（5 阶段）](#使用流程5-阶段)
4. [输出规格书结构](#输出规格书结构)
5. [与 create-project 衔接](#与-create-project-衔接)

---

## 安装 codegraph

### 方式一：跨平台脚本（推荐，Windows / macOS / Linux 均可）

只需 Node.js ≥ 18，无其他依赖：

```bash
node skills/code-index/scripts/install-codegraph.js /path/to/your-project
```

脚本会自动：检查 Node.js 版本 → 全局安装 `@colbymchenry/codegraph` → 写入 `.codegraph/config.json`（排除 target/node_modules 等） → 运行 `codegraph init -i` 建立索引。

### 方式二：手动安装

```bash
# 全局安装
npm install -g @colbymchenry/codegraph

# 进入目标项目，初始化并建立索引（-i = index immediately）
cd /path/to/your-project
codegraph init -i

# 查看索引状态（确认索引行数 > 0）
codegraph status
```

### 配置 MCP（Claude Code 环境必须）

```json
// ~/.claude.json（或项目 .claude/settings.json）
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

---

## 核心命令速查

| 命令 | 用途 | 典型场景 |
|---|---|---|
| `codegraph_search` | 按符号名定位（模糊，会截断） | 定位已知类/方法，拿到 node id |
| `codegraph_files` | 按目录+glob 枚举文件（精确，完整） | 清点所有 Controller / Entity / Vue 页面 |
| `codegraph_callers` | 向上追踪谁调用了某函数 | 理解一个 Service 被哪些入口触发 |
| `codegraph_callees` | 向下展开调用树 | 追踪核心方法的完整调用链 |

> **铁律**：枚举/清点 → 用 `codegraph_files`（确定完整）；定位单个已知符号 → 用 `codegraph_search`。

```
# 示例
codegraph_search(query="UserController", kind="class", limit=10)
codegraph_files(path="src/main/java", pattern="*Controller.java", format="flat")
codegraph_callers(name="UserService.login")
codegraph_callees(symbol_id="<node_id>", depth=5)
```

---

## 使用流程（5 阶段）

在 Claude Code 对话中输入以下指令，Skill 会按阶段执行（断点后重启会自动从 `.progress.md` 续接）：

```
@skills/code-index/SKILL.md  分析 /path/to/your-project 生成规格书
```

### Phase A：项目概览

```bash
codegraph status   # 查看索引行数、各类型符号数量
```

输出：框架类型识别（Spring Boot / RuoYi / Django 等）、模块数量、文件分布。

### Phase B：模块分组

- **≤ 5 个模块**：直接扁平分析，输出到 `spec/` 平铺目录
- **> 5 个模块**：按功能域分组（建议 3-5 组），逐组深挖，输出到 `spec/modules/{module}/`

```
# 枚举所有顶级模块
codegraph_files(path="src/main/java", pattern="*", format="tree")
```

### Phase C：逐模块深挖

每个模块按顺序分析：

1. `codegraph_files(pattern="*Controller.java")` → 列出所有接口入口
2. 对每个 Controller：`codegraph_search` 定位 → `codegraph_callees` 向下追调用链
3. 读取 Entity / Service 源码，提取字段、业务规则、状态机
4. 收集状态枚举、枚举类、配置常量

### Phase D：规格生成

按 `skills/code-index/references/llm-prompts.md` 中的 8 个 Prompt 依次生成：

| Prompt | 输出文件 | 内容 |
|---|---|---|
| P1 | `01-hla.md` | 高阶架构：架构图 + 关键服务方法设计 + ER 摘要 + Mermaid 流程图 |
| P2 | `02-srs.md` | 功能需求规格（FR 编号 + 验收标准） |
| P3 | `03-prd.md` | 产品需求文档（用户视角） |
| P4 | `04-user-stories.md` | 用户故事（As A / I Want / So That） |
| P5 | `05-api.md` | 接口文档（端点 + 请求/响应结构） |
| P6 | `06-flowcharts/` | 业务流程图（Mermaid flowchart） |
| P7 | `07-database.md` | 数据库表结构（建表 DDL + ER 图） |
| P8 | `09-ui/` | 静态 HTML 原型（列表/表单/详情/审批/Dashboard） |

### Phase C'：完整性核查

```bash
codegraph status   # 对比 route 计数与 API 文档接口数
```

确认：API 文档覆盖率 ≥ 90%、所有 Controller 均有对应文档、主要状态机均在流程图中。

---

## 输出规格书结构

### 小型项目（模块 ≤ 5）

```
spec/
├── .progress.md          # 进度追踪（断点续接）
├── 01-hla.md             # 高阶架构（含 Mermaid 流程图、ER 摘要）
├── 02-srs.md             # 功能需求规格
├── 03-prd.md             # 产品需求文档
├── 04-user-stories.md    # 用户故事
├── 05-api.md             # 接口文档
├── 06-flowcharts/        # 业务流程图
│   ├── login-flow.md
│   └── order-flow.md
├── 07-database.md        # 数据库结构
├── 08-migration.md       # 迁移说明（可选）
├── 09-ui/                # 静态 HTML 原型
│   ├── {entity}-list.html
│   ├── {entity}-form.html
│   ├── {entity}-detail.html
│   ├── {entity}-workflow.html
│   └── dashboard.html
├── 10-i18n.md            # 国际化词条（可选）
└── 11-deployment.md      # 部署说明（可选）
```

### 大型项目（模块 > 5）

```
spec/
├── .progress.md
├── 00-overview.md        # 全局架构概览
└── modules/
    ├── user-mgr/         # 用户管理模块
    │   ├── 01-hla.md
    │   ├── 02-srs.md
    │   └── ...
    ├── order-mgr/
    └── report/
```

---

## 与 create-project 衔接

`code-index` 生成的规格书可直接作为 `create-project` 的输入：

```
code-index 输出：spec/02-srs.md + spec/04-user-stories.md
                          ↓
create-project 输入：/sdd-specify（粘贴 SRS 功能点）
                          ↓
生成 IIDP 应用代码
```

如果 `spec/09-ui/` 目录下有静态 HTML 原型，可用 `/sdd-ui-parse` 命令解析交互需求：

```
/sdd-ui-parse   # 解析 spec/09-ui/*.html → 生成 interaction-spec.md
```

详见 [create-project 使用指南](02-create-project-guide.md)。
