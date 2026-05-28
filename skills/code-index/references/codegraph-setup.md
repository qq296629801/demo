# CodeGraph 安装与命令参考

## 安装

```bash
npm install -g @colbymchenry/codegraph   # 全局安装
npx @colbymchenry/codegraph              # 零安装交互式向导（推荐首次）
```

支持语言（19种）：TypeScript、JavaScript、Python、Go、Rust、**Java**、C#、PHP、Ruby、C、C++、Swift、Kotlin、Dart、Svelte、Liquid、Pascal/Delphi

---

## CLI 命令速查

| 命令 | 说明 |
|-----|------|
| `codegraph init [path]` | 初始化项目（生成 .codegraph/） |
| `codegraph init -i [path]` | 初始化并立即建立索引 |
| `codegraph index [path]` | 全量索引（`--force` 强制重建） |
| `codegraph sync [path]` | 增量更新 |
| `codegraph status [path]` | 查看索引统计（节点数、边数、文件数） |
| `codegraph query <term>` | 搜索符号（`--kind`, `--limit`, `--json`） |
| `codegraph files [path]` | 文件结构（`--json`） |
| `codegraph context <task>` | 为某任务构建上下文（`--format markdown`） |
| `codegraph affected [files]` | 影响分析（CI 用） |
| `codegraph serve --mcp` | 启动 MCP 服务器 |

---

## MCP 工具完整参数表

### `codegraph_search`
```json
{
  "query":  "符号名或关键词（支持模糊搜索）",
  "kind":   "function|class|method|interface|variable|type（可选）",
  "limit":  20,
  "file":   "过滤特定文件路径（可选）"
}
```

**返回结构**：
```json
[{
  "id":        "唯一节点 ID（用于其他工具）",
  "name":      "符号名",
  "kind":      "function/class/method/...",
  "file":      "相对文件路径",
  "line":      123,
  "signature": "完整签名（含参数、返回值）",
  "docstring": "注释文档（如有）",
  "language":  "java/python/typescript/..."
}]
```

---

### `codegraph_callers`
```json
{
  "symbol_id": "<node_id>（来自 codegraph_search）",
  "depth":     3
}
```

**返回结构**：
```json
{
  "symbol": { "id": "...", "name": "...", "file": "...", "line": 0 },
  "callers": [{
    "caller": { "id": "...", "name": "...", "file": "...", "line": 0 },
    "call_site": { "file": "...", "line": 0 },
    "edge_type": "direct|indirect"
  }]
}
```

---

### `codegraph_callees`（即 codegraph_trace 的底层实现）
```json
{
  "symbol_id": "<node_id>",
  "depth":     5
}
```

**返回结构**：
```json
{
  "symbol": { "id": "...", "name": "...", "file": "...", "line": 0 },
  "callees": [{
    "callee": { "id": "...", "name": "...", "file": "...", "line": 0 },
    "call_site": { "file": "...", "line": 0 },
    "depth": 1
  }]
}
```

---

### `codegraph_impact`
```json
{ "symbol_id": "<node_id>", "depth": 3 }
```
返回所有受影响的符号和文件列表。

---

### `codegraph_node`
```json
{ "symbol_id": "<node_id>", "include_source": true }
```
返回单个节点的完整信息，包含源代码片段。

---

### `codegraph_files`
```json
{ "max_depth": 3, "filter": "*.java" }
```
返回项目文件树（比 ls/find 快 10 倍）。

---

## 分析大型 Java 项目的推荐顺序

```bash
# 1. 查看项目概貌
codegraph status
codegraph files --json --filter "*.java" --max-depth 4

# 2. 枚举所有 Controller（API 入口）
codegraph query "Controller" --kind class --json

# 3. 枚举所有 Entity（数据模型）
codegraph query "Entity" --kind class --json
codegraph query "TableName" --json   # MyBatis-Plus

# 4. 枚举所有 Service
codegraph query "Service" --kind class --json

# 5. 追踪核心业务流程
codegraph query "createOrder" --json | jq '.[0].id'
# 用返回的 id 继续追踪...
```

---

## 索引性能参考（Java 大型项目）

| 项目规模 | 文件数 | 节点数 | 索引时间 |
|---------|-------|-------|---------|
| 小型（RuoYi-Vue）| ~800 | ~15k | ~5s |
| 中型（JEECG Boot）| ~3,000 | ~60k | ~30s |
| 大型（yudao-cloud）| ~8,000 | ~150k | ~90s |

> 索引一次，查询永远在毫秒级。
