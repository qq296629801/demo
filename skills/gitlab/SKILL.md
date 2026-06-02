---
name: gitlab-mcp
description: >-
  GitLab MCP（Gitlab-MCP-Server / Cursor stdio）：合并请求、代码搜索、CI 流水线与 Job、ADR 文档、云部署触发。
  在 Cursor 中通过 MCP 调用；工具参数使用 JSON 字符串 args/kwargs。适用于 GitLab、MR、Pipeline、search_code、ADR、Gitlab-MCP-Server。
---

# GitLab MCP 使用说明

本 Skill 对应通过 **Cursor MCP** 接入的 **GitLab MCP Server**（典型入口脚本为 `gitlab_cursor_stdio.py`，底层实现见 `gitlab_mcp_server.py`）。Agent 在调用前应在 MCP 描述符目录中确认 **server 名称**（常见为 `gitlab`，多根工作区可能显示为 `project-0-agents-gitlab` 等别名）。

## 1. Cursor 调用约定：`args` 与 `kwargs`

GitLab 工具在 MCP 层统一暴露两个字符串参数：

- **`args`**：JSON **数组**，按位置传给 Python 函数（一般留空 `[]`）。
- **`kwargs`**：JSON **对象**，以**关键字**传入业务参数。

错误示例：把 `project` 当作关键字（会报错）；代码搜索等项目相关参数名是 **`project_id`**（可为 `group/project` 或数字 ID）。

**逻辑等价**：`kwargs` 解析后相当于 `await tool_fn(**kwargs)`（必要时配合 `*args`）。

### 调用示例（概念）

对 `search_code`：

```json
{
  "args": "[]",
  "kwargs": "{\"project_id\": \"group/subproject\", \"query\": \"foo\", \"ref\": \"main\", \"max_results\": 20}"
}
```

## 2. 工具一览与参数要点

下列参数名与 **`gitlab_mcp_server.py`** 中异步函数一致。未列出的参数见函数默认值。

### 合并请求（MR）

| 工具 | 作用 | 主要 `kwargs` |
|------|------|----------------|
| `list_merge_requests` | 列出 MR | `project_id`（必填）, `state`（`opened`/`closed`/`merged`/`all`）, `scope`（`created_by_me`/`assigned_to_me`/`all`）, `author_username`, `assignee_username` |
| `get_merge_request_details` | MR 详情 | `project_id`, `mr_iid`, `include_changes`, `include_discussions` |
| `create_merge_request` | 创建 MR（写） | `project_id`, `source_branch`, `target_branch`, `title`, `description`, `assignee_id`, `reviewer_ids`, `labels`, `remove_source_branch`, `squash`, `draft` |
| `approve_merge_request` | 审批（写） | `project_id`, `mr_iid`, `approval_comment` |
| `merge_merge_request` | 合并（写） | `project_id`, `mr_iid`, `squash`, `remove_source_branch`, `merge_commit_message` |
| `rebase_merge_request` | Rebase（写） | `project_id`, `mr_iid`, `skip_ci` |

### 代码搜索

| 工具 | 作用 | 主要 `kwargs` |
|------|------|----------------|
| `search_code` | 项目内 blob 搜索 | `project_id`, `query`, `ref`（默认 `main`，可按仓库改为 `master`）, `path`（限定子路径）, `max_results` |

### CI：流水线与 Job

| 工具 | 作用 | 主要 `kwargs` |
|------|------|----------------|
| `list_pipeline_jobs` | 列出 Pipeline 下 Job | `project_id`, `pipeline_id`（省略则取最新一条）, `scope`, `include_retried` |
| `get_job_log` | Job 日志/Trace | `project_id`, `job_id`, `lines`（可选，有上限） |
| `analyze_failed_jobs` | 分析失败 Job 并给建议 | `project_id`, `pipeline_id`, `suggest_fixes` |
| `retry_failed_job` | 重试 Job（写） | `project_id`, `job_id` |
| `trigger_pipeline` | 触发新流水线（写） | `project_id`, `ref`, `variables`（`{"KEY":"value"}`） |
| `cancel_pipeline` | 取消流水线（写） | `project_id`, `pipeline_id` |
| `retry_pipeline` | 重试流水线（写） | `project_id`, `pipeline_id` |

### ADR（架构决策记录）

| 工具 | 作用 | 主要 `kwargs` |
|------|------|----------------|
| `create_adr_document` | 生成 ADR Markdown | `title`, `status`, `context`, `decision`, `consequences`, `alternatives`, `related_decisions`, `participants`, `save_path`, `project_id`（可选，用于编号） |
| `commit_adr_to_gitlab` | 提交 ADR 到仓库（写） | `project_id`, `adr_content`, `adr_title`, `branch`, `create_mr` |

### 云部署（通过触发带变量的 Pipeline）

| 工具 | 作用 | 主要 `kwargs` |
|------|------|----------------|
| `deploy_to_cloud` | 按云厂商组装变量并 `trigger_pipeline` | `provider`（`aws`/`azure`/`gcp`）, `project_id`, `environment`（`dev`/`staging`/`prod`）, `deployment_config`（字典，含各云必填字段）, `require_approval` |

`prod` 需环境变量 `ALLOW_PROD_DEPLOY=true`，否则返回错误。

## 3. 安全与行为提示

- **`@check_project_access`**：多数工具会校验项目是否在允许列表及 token 权限范围内。
- **写操作**（创建 MR、合并、触发流水线等）受 **`SAFE_MODE`**、**`DRY_RUN`** 影响；生产合并前应让用户确认。
- 返回体多为 `{"status": "success"|"error", ...}`，Agent 应检查 `status` 与 `error` 字段。

## 4. 何时启用本 Skill

用户提到 **GitLab、合并请求、MR、Pipeline、CI Job、代码搜索、ADR、Gitlab-MCP-Server**，或需要代查/代操作上述能力时：先读本 Skill，再查 MCP 工具描述符，最后用正确的 **`project_id` + `kwargs` JSON 字符串** 调用。
