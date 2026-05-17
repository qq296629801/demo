---

## name: iidp-app-store
description: >-
  Builds IIDP JSON-RPC `service` payloads for the app market (search catalog, publish/upApp before install or update,
  install, list installed apps, update, create/save catalog entries). Use when the user mentions sie-iidp-app-store,
  meta_app_store, meta_app, iidp_mcp, or asks to install, update, query, or publish apps in the IIDP app market.

# IIDP 应用市场（App Store）

面向 `sie-iidp-app-store` 的 JSON-RPC 调用约定；通过 **iidp_mcp** 等 MCP 转发时，按下列 `model` / `service` 与 `args` 组装 `params`。

## 接口速查


| 能力        | model            | service      | 说明                         |
| --------- | ---------------- | ------------ | -------------------------- |
| 市场列表/搜索   | `meta_app_store` | `search`     | 可安装应用                      |
| 保存/新建市场条目 | `meta_app_store` | `create`     | `valuesList` 写入一条或多条市场记录   |
| 上架        | `meta_app_store` | `upApp`      | 按市场条目 id 上架（**安装、更新前应调用**） |
| 安装        | `meta_app_store` | `installApp` | 按市场条目 id 安装                |
| 已安装列表     | `meta_app`       | `search`     | 当前租户已装应用                   |
| 更新        | `meta_app`       | `updateApp`  | 按已安装应用 id 更新               |


公共字段（与示例一致即可，具体值由环境/MCP 注入）：

- `method`: `service`
- `params.app`: `sie-iidp-app-store`
- `params.tag`: `master`
- `params.context`: `uid`、`timeZone`（如 `UTC+8`）、`lang`（如 `zh-CN`）
- `params.authId`: 使用会话或 MCP 返回的鉴权值，**勿在技能或文档中硬编码真实 token**

请求骨架：

```json
{
  "id": "<request-id>",
  "jsonrpc": "2.0",
  "method": "service",
  "params": {
    "args": {},
    "context": { "uid": "", "timeZone": "UTC+8", "lang": "zh-CN" },
    "model": "<见上表>",
    "tag": "master",
    "service": "<见上表>",
    "authId": "<from-session-or-mcp>",
    "app": "sie-iidp-app-store"
  }
}
```

## 1. 市场查询（search / meta_app_store）

`args` 要点：`useDisplayForModel: true`，`filter` / `limit` / `offset` / `order`，`properties` 需包含展示与筛选所需字段。

推荐 `properties`：

`name`, `tag`, `display_name`, `version`, `product`, `category_ids`, `state`, `app_market`, `reinstall`, `extension_scope`, `summary`, `update_date`

示例 `args`：

```json
{
  "useDisplayForModel": true,
  "filter": [],
  "limit": 31,
  "offset": 0,
  "order": "",
  "properties": [
    "name", "tag", "display_name", "version", "product", "category_ids",
    "state", "app_market", "reinstall", "extension_scope", "summary", "update_date"
  ]
}
```

响应：`result.data[]` 为应用条目，常见字段含 `name`、`display_name`、`version`、`state`（如 `installable`）、`id`（安装时用作市场侧 id）等。

## 2. 上架（upApp / meta_app_store）

将市场条目**上架**后方可稳定执行安装或更新。`args`：`ids`（**市场条目 id**，与 `installApp` 所用 `meta_app_store` 行 id 相同）、`useDisplayForModel: true`。

### 请求示例

```json
{
  "id": "<request-id>",
  "jsonrpc": "2.0",
  "method": "service",
  "params": {
    "args": {
      "ids": ["<meta_app_store_row_id>"],
      "useDisplayForModel": true
    },
    "context": { "uid": "", "timeZone": "UTC+8", "lang": "zh-CN" },
    "model": "meta_app_store",
    "tag": "master",
    "app": "sie-iidp-app-store",
    "service": "upApp",
    "authId": "<from-session-or-mcp>"
  }
}
```

### 响应示例

成功时 `result.data.message` 含提示文案与类型。

```json
{
  "id": "<request-id>",
  "jsonrpc": "2.0",
  "result": {
    "data": {
      "message": {
        "text": "上架成功",
        "type": "success"
      }
    },
    "context": {
      "@Class": "com.alibaba.fastjson.JSONObject",
      "uid": "",
      "tenantId": "rbac_tenant_root",
      "skin": null,
      "host": "<实例主机>",
      "lang": "zh-CN",
      "curSg": null
    }
  }
}
```

## 3. 安装（installApp / meta_app_store）

`args`：`ids`（市场条目 id 数组）、`replicas`、`svcName`（如 `iidp-app`）、`useDisplayForModel`。

```json
{
  "ids": ["<meta_app_store_row_id>"],
  "replicas": "1",
  "svcName": "iidp-demo",
  "useDisplayForModel": true
}
```

## 4. 已安装列表（search / meta_app）

与市场查询类似，但 `model` 为 `meta_app`，`properties` 侧重已安装视图。

推荐 `properties`：

`name`, `tag`, `display_name`, `version`, `state`, `source`, `product`, `category_ids`, `summary`, `extension_scope`, `md5`, `meta_app_store_id`, `view_file_id`, `update_date`

## 5. 更新（updateApp / meta_app）

`args`：`ids`（**已安装应用** id，来自上文 **已安装列表**）、`useDisplayForModel`。

```json
{
  "ids": ["<meta_app_row_id>"],
  "useDisplayForModel": true
}
```

## 6. 应用市场保存（create / meta_app_store）

向市场**新增**一条应用记录：`service` 为 `create`，`model` 为 `meta_app_store`，`args` 含 `valuesList`（对象数组，通常一条）与 `useDisplayForModel: true`。

### 与文件上传的关系（必读）

- **iidp_mcp** 的 `**_iidp_file_upload`** 增加参数 `**bucketType**`（存储桶类型）；**默认** **`public`**（不传则按 `public`；需其它桶类型时显式传入约定值）。
- `valuesList[].jar_file_id` **必须**与 **iidp_mcp** 的 `**_iidp_file_upload`** 上传接口返回结果中的 `**id`** 一致（即上传成功后的文件记录 id）。
- 若上传无有效返回（无 `id` 或调用失败），视为上传失败，**不能**再调用本 `create` 期望保存成功；应先修复上传再保存。
- `jar_file_id_file.uid` 与 `upload_field_jar_file_id[].uid` 等字段需与实际上传结果、表单模型约定对齐；`upload_field_jar_file_id` 中常见含 `bucket`、`key`、`name`、`md5`、`size`、`content_type`、`model_id`（如 `meta_app_store`）等，与上传回包或 MCP 侧约定一致即可。

### 请求示例

```json
{
  "id": "<request-id>",
  "jsonrpc": "2.0",
  "method": "service",
  "params": {
    "args": {
      "valuesList": [
        {
         
          "jar_file_id": "<来自 _iidp_file_upload 返回的 id>",
          "view_file_id": null
        }
      ],
      "useDisplayForModel": true
    },
    "context": { "uid": "", "timeZone": "UTC+8", "lang": "zh-CN" },
    "model": "meta_app_store",
    "tag": "master",
    "service": "create",
    "authId": "<from-session-or-mcp>",
    "app": "sie-iidp-app-store"
  }
}
```

### 响应示例

成功时 `result.data` 为新建记录的 id 数组（与请求中 `valuesList` 条数对应）。

```json
{
  "id": "<request-id>",
  "jsonrpc": "2.0",
  "result": {
    "data": ["05q9cpuf9g4bn"],
    "context": {
      "@Class": "com.sie.snest.engine.data.RecordSet",
      "uid": "",
      "tenantId": "rbac_tenant_root",
      "skin": null,
      "host": "<实例主机>",
      "lang": "zh-CN",
      "curSg": null
    }
  }
}
```

## 工作流

### 安装应用

1. 通过 **iidp_mcp**（或等价通道）用上文 **市场查询（meta_app_store / search）** 按名称/关键字解析目标应用，取得 `result.data[].id`。
2. **先调用上架（upApp）**：`ids` 为上一步的市场条目 id（可与 `installApp` 相同，支持多个）。
3. 用 **安装（installApp）** 提交该市场 id（可多个 `ids`）。
4. 必要时再调 **已安装列表** 确认状态。

### 更新应用

1. 通过 **iidp_mcp** 用 **已安装列表（meta_app / search）** 定位应用，取得已安装行的 `id`（及版本信息）。
2. **先调用上架（upApp）**：`ids` 为该应用对应的**市场条目 id**（已安装记录上的 `meta_app_store_id`；若无则回到 **市场查询** 解析到同一应用的市场行 id）。
3. 用 **更新（updateApp）** 提交已安装行的 `ids`（**meta_app** 行 id，非市场 id）。

### 上传应用

本流程依赖**用户本机的 JAR 包**；在未与用户对齐路径与存在性之前，不要调用上传或 `create`。

1. **本地 JAR 与确认**
  - 请用户提供待上传 JAR 的**完整路径**（含目录与文件名，例如 `/path/to/app.jar`）。  
  - 与用户**逐项确认**：目录是否正确、文件名是否为目标 JAR、该路径下文件**是否存在**（可在用户同意后用终端或文件工具校验；若不存在则停止并让用户修正路径）。  
  - 确认无误后再进入上传步骤。
2. 通过 **iidp_mcp** 调用 `**_iidp_file_upload`** 上传上述已确认的 JAR；参数中增加 `**bucketType**`，**默认** **`public`**（可显式传 `public` 或依赖默认）。从返回中取 `**id**`（及 `uid`、`key`、`md5` 等与表单一致的字段）。
3. 将上一步的 `**id**` 填入 `**jar_file_id**`，并同步 `**upload_field_jar_file_id**` / `**jar_file_id_file**` 等嵌套结构后，调用 **应用市场保存（create / meta_app_store）**。
4. 从 `**result.data[]`** 读取新建市场条目的 id。
5. 若后续要**安装**该条目：先 **upApp**（`ids` 为步骤 4 的市场 id），再 **installApp**。

**进度核对（可复制）**

```
安装： [ ] 市场 search → [ ] 解析目标市场 id → [ ] upApp → [ ] installApp → [ ] 可选：已装 search 校验
更新： [ ] 已装 search → [ ] 解析 meta_app id 与市场 id（meta_app_store_id）→ [ ] upApp（市场 id）→ [ ] updateApp（已装 id）
上传： [ ] 用户提供 JAR 路径 → [ ] 与用户确认目录/文件名/存在性 → [ ] _iidp_file_upload → [ ] 取返回 id → [ ] create 对齐 jar_file_id → [ ] 校验 result.data → [ ] 若安装：upApp → installApp
```

## 延伸阅读

体量大的示例响应可单独放在 `reference.md` 或项目文档中；本文件仅保留调用约定与字段说明，避免重复占用上下文。