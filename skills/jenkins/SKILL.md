---
name: mcp-jenkins
description: >-
  Jenkins MCP（mcp-jenkins / FastMCP）：查询与触发 Job、构建信息、控制台日志、队列与节点、视图、配置 XML。
  工具使用结构化 JSON 参数（非 GitLab 式 args 字符串）。适用于 Jenkins、流水线构建、mcp-jenkins、jenkins-py。
---

# Jenkins MCP（mcp-jenkins）使用说明

**安装与 Cursor `mcp.json` 配置**见同目录 [`mcp-jenkins-setup.md`](mcp-jenkins-setup.md)。

本 Skill 对应 **mcp-jenkins** 提供的 MCP 工具集（Cursor 中 server 名常见为 **`jenkins-py`**，多根工作区可能为 `project-0-agents-jenkins-py` 等）。与 GitLab MCP 不同：每个工具的 **arguments 即为具名字段**（boolean、integer、string、object），**不需要**把参数包进 `args`/`kwargs` 字符串。

## 1. 重要概念：`fullname`

几乎所有 Job 相关工具使用 **`fullname`**：Jenkins 中的**完整任务路径**，与 UI 面包屑一致，**区分大小写**。

- 顶层 Job：`my-job`
- Folder 内：`folder-name/job-name`
- 多级 Folder：`team1/backend/deploy`

不确定时先用 `get_all_items` 或 `query_items` 浏览。

## 2. 工具分组与参数

以下与 MCP 描述符一致；**required** 字段在调用时必须提供。

### Job / Item 发现与元数据

| 工具 | 说明 | 主要参数 |
|------|------|-----------|
| `get_all_items` | 列出全部 Item | 无参数 |
| `get_item` | 单个 Item 信息 | `fullname`（必填） |
| `query_items` | 按模式过滤 | `class_pattern`, `fullname_pattern`, `color_pattern`, `folder_depth`（均可选） |

### 触发构建与队列

| 工具 | 说明 | 主要参数 |
|------|------|-----------|
| `build_item` | 触发构建 | `fullname`（必填）, `build_type`（必填）：`build` 或 `buildWithParameters`, `params`（可选，对象：参数名 → 值） |
| `get_all_queue_items` | 当前队列 | 无参数 |
| `get_queue_item` | 队列单项 | `id`（必填，整数） |
| `cancel_queue_item` | 取消排队项 | `id`（必填） |

**注意**：若 Job 配置了参数，必须使用 **`build_type: "buildWithParameters"`**，并在 `params` 中传入对应参数。

### 构建信息、日志与测试

| 工具 | 说明 | 主要参数 |
|------|------|-----------|
| `get_build` | 单次构建信息 | `fullname`（必填）, `number`（可选，默认最新构建） |
| `get_running_builds` | 所有运行中构建 | 无参数 |
| `stop_build` | 停止构建 | `fullname`, `number`（必填） |
| `get_build_console_output` | 控制台输出 | `fullname`（必填）, `number`, `pattern`（正则，只保留匹配行）, `offset`, `limit` |
| `get_build_parameters` | 某次构建的参数值 | `fullname`, `number` |
| `get_build_scripts` | 构建中使用的脚本片段列表 | `fullname`, `number` |
| `get_build_test_report` | 测试报告 | `fullname`, `number` |

### 参数化 Job 定义

| 工具 | 说明 | 主要参数 |
|------|------|-----------|
| `get_item_parameters` | Job 的参数**定义**（名、类型、默认值等） | `fullname`（必填） |

典型流程：先 `get_item_parameters` 确认参数名 → 再 `build_item` + `buildWithParameters` + `params`。

### 配置 XML（高级）

| 工具 | 说明 | 主要参数 |
|------|------|-----------|
| `get_item_config` | 拉取 Job 的 `config.xml` | `fullname` |
| `set_item_config` | 更新配置（写） | `fullname`, `config_xml` |

修改前建议备份；需具备 Jenkins 对应权限。

### 节点（Agent）

| 工具 | 说明 | 主要参数 |
|------|------|-----------|
| `get_all_nodes` | 所有节点 | 无参数 |
| `get_node` | 单节点（含 executor 信息） | `name`（必填） |
| `get_node_config` | 节点配置 XML | `name` |
| `set_node_config` | 写节点配置（写） | `name`, `config_xml` |

### 视图（Views）

| 工具 | 说明 | 主要参数 |
|------|------|-----------|
| `get_all_views` | 顶层视图列表 | 无参数 |
| `get_view` | 按路径取视图（含 Job / 子视图） | `view_path`（必填，多级用 `/`，如 `frontend/nightly`）, `depth`（默认 0） |

## 3. 返回值习惯

部分工具使用 FastMCP 包装，列表结果可能出现在返回对象的 **`result`** 字段中（例如 `query_items`、`get_all_items`）；具体以 MCP 调用返回为准。字符串类结果（如控制台日志）也可能嵌套在 `result` 下。

## 4. 何时启用本 Skill

用户提到 **Jenkins、构建、Job、控制台日志、队列、mcp-jenkins、jenkins-py**，或需要触发/排查 CI 时：先读本 Skill，再在 MCP 工具列表中核对参数名，使用正确的 **`fullname`** 调用。
