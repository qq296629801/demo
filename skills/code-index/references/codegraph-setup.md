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
  "query":  "符号名或关键词",
  "kind":   "function|class|method|interface|variable|type|route|component（可选）",
  "limit":  20
}
```

> ⚠️ `query` 是**相关性排序的模糊搜索，且受 `limit` 截断**：适合"定位一个已知符号"，
> **不适合枚举/计数**（实测 35 个 Controller 只回 2 个；`route` 搜索同样模糊+截断）。
> 要"列出某类全部 / 清点数量"，请用 `codegraph_files`（文件枚举）+ `codegraph_status`（kind 计数）。
> 新增可用 kind：`route`=后端 HTTP 端点（服务端注解抽取，前端无）、`component`=前端组件。
> 旧文档里的 `file` 参数在当前 MCP schema 中不存在（CLI-only / 已废弃），勿依赖。

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
{
  "path":    "目录过滤，如 src/api（可选）",
  "pattern": "glob 文件名，如 *Controller.java（可选）",
  "format":  "tree|flat|grouped（可选，枚举建议 flat）",
  "maxDepth": 3
}
```
返回匹配文件全集（比 ls/find 快 10 倍）。**这是枚举/清点的唯一可靠工具**——
`path` + `pattern` 精确穷举（实测 `pattern="*Controller.java"` 完整返回 35 个，与 find 一致）。

> 注意：CLI 的 `codegraph files --filter "*.java"` 实测可能返回空；优先用 MCP
> `codegraph_files(pattern=...)`。

---

## 分析大型 Java 项目的推荐顺序

```text
# 1. 查看项目概貌（拿基线：文件数、各 kind 计数 class/route）
codegraph_status

# 2. 枚举所有 Controller（API 入口）—— 用文件枚举，不用 search
codegraph_files(pattern="*Controller.java", format="flat")

# 3. 枚举所有 Entity（数据模型）
codegraph_files(pattern="*DO.java", format="flat")       # MyBatis-Plus
codegraph_files(pattern="*Entity.java", format="flat")   # JPA

# 4. 枚举所有 Service
codegraph_files(pattern="*ServiceImpl.java", format="flat")

# 5. 追踪核心业务流程（这才是 search 的正确用法：定位单个已知方法拿 id）
codegraph_search(query="createOrder")   # 拿到 node id
# 用返回的 id 继续 codegraph_callees / codegraph_callers 追踪...
```

> 为什么不用 `codegraph query "Controller" --kind class` 枚举？因为 search 是模糊+截断的，
> 实测只回 2/35。枚举一律走 `codegraph_files`。

---

## 索引性能参考（Java 大型项目）

| 项目规模 | 文件数 | 节点数 | 索引时间 |
|---------|-------|-------|---------|
| 小型（RuoYi-Vue）| ~800 | ~15k | ~5s |
| 中型（JEECG Boot）| ~3,000 | ~60k | ~30s |
| 大型（yudao-cloud）| ~8,000 | ~150k | ~90s |

> 索引一次，查询永远在毫秒级。
