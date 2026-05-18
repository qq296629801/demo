# IIDP 前后端契约

> **本文件说明**：这是 `sdd-contracts.md` 的 **skill 参考文件**，描述契约表的格式规范、JSON-RPC 协议、Filter 规则和节点契约规范。
> 生成每个 Feature 的项目产物（`specs/features/<feature>/sdd-contracts.md`）时，请使用 `sdd-constitution.md` 中的 **sdd-contracts.md 模板**。

## 多模型业务契约组织

业务涉及多个模型时，契约表必须按模型分组，不要把所有模型的服务和按钮混在一张表里。建议结构：

```markdown
## 契约总览

### 模型 1：[ModelName] (`[model_name]`)

| app | model | service | 类型 | args 参数（名称: 类型） | 权限码 | 前端入口 | 节点 id | 待确认 |
|---|---|---|---|---|---|---|---|---|
| `sie-iidp-demo-{appName}` | `{model_name}` | `search` | 内置 | 平台标准（详见 `method-service.md` §search 入参） | `read` | grid 加载 | `{model_name}_grid` | — |
| `sie-iidp-demo-{appName}` | `{model_name}` | `create` | 内置 | 平台标准（详见 `method-service.md` §create 入参） | `create` | tbar 新增 | — | — |
| `sie-iidp-demo-{appName}` | `{model_name}` | `update` | 内置 | 平台标准（详见 `method-service.md` §update 入参） | `update` | 行编辑 | — | — |
| `sie-iidp-demo-{appName}` | `{model_name}` | `delete` | 内置 | 平台标准（详见 `method-service.md` §delete 入参） | `delete` | 行删除 | — | — |
| `sie-iidp-demo-{appName}` | `{model_name}` | `[xxx]` | 自定义 | `[paramName]: [type]`，多参数用 ` / ` 分隔 | `[auth]` | grid 按钮 | `[id]` | — |

### 模型 2：[ModelName] (`[model_name]`)
...
```

> **自定义服务 args 类型映射规则**（来自 `backend-spec.md` §4 Java 方法签名）：
>
> | Java 参数类型 | args 前端类型 | 填写示例 |
> |---|---|---|
> | `String` | `string` | `reason: string` |
> | `Long` / `Integer` | `number` | `days: number` |
> | `Boolean` | `boolean` | `force: boolean` |
> | `List<Long>` / `Long[]` | `number[]` | `ids: number[]` |
> | `List<String>` | `string[]` | `codes: string[]` |
> | `Map<String,Object>` | `object` | `extra: object` |
> | `RecordSet rs` | **不填**（平台通过 `bind_ids` 自动注入） | — |
> | `Filter` / `int limit` 等内置参数 | **不填**（平台标准参数） | — |
>
> 只有 `RecordSet` 之后的业务自定义参数才写入 args 列。多个参数写法示例：方法 `batchEnable(RecordSet rs, String reason, Integer days)` → args 列填 `reason: string / days: number`。

### 跨模型服务契约表

涉及多个模型的业务流程服务，单独列表说明：

| service | 挂载模型 | 涉及模型 | 事务边界 | 触发入口 | 节点 id | 权限码 |
|---|---|---|---|---|---|---|
| `[serviceName]` | `[主模型]` | `[模型1], [模型2]` | IIDP 请求级事务（抛 `ModelException` 自动回滚） | grid 按钮/form 内 | `[id]` | `[auth]` |

### 权限码总览（单一事实来源）

| 权限码 | 含义 | 涉及模型 | 后端校验位置 | 前端控制位置 |
|---|---|---|---|---|
| `[auth_code]` | [说明] | `[model]` | `@MethodService` 服务内 | 按钮 `auth` 字段 |

### 状态机契约（业务含状态流转时必填）

IIDP 业务中 90% 涉及状态机（工单、合同、订单、审批、采购等）。状态机契约必须包含 3 部分：状态枚举、流转规则、按状态映射的服务和按钮。

#### 1. 状态枚举

| 状态值（英文） | 状态值（中文） | 含义 | 允许编辑字段 | 终态/可删除 |
|---|---|---|---|---|
| `DRAFT` | 草稿 | 初始态 | 所有业务字段 | 否 / 是 |
| `RELEASED` | 已下达 | 流程已开始 | 计划日期、备注 | 否 / 否 |
| `IN_PROGRESS` | 执行中 | 在做 | 报工数量 | 否 / 否 |
| `COMPLETED` | 已完工 | 完成 | 只读 | 否 / 否 |
| `CLOSED` | 已关闭 | 归档 | 只读 | 是 / 否 |

#### 2. 状态流转规则

```text
DRAFT ──release──> RELEASED ──startWork──> IN_PROGRESS ──completeWork──> COMPLETED ──closeWork──> CLOSED
                                                                                                    │
                                                                                       任何状态 ──cancel──> CANCELLED（如有）
```

- 流转方向：单向不可回退（除非有显式回退服务和权限）
- 跳跃状态：禁止跳跃（如不允许 DRAFT → COMPLETED）；如允许，明确特殊权限和原因

#### 3. 状态 × 服务映射表

| 状态 | 允许的服务 | 触发权限码 | 状态后转 | 系统副作用 |
|---|---|---|---|---|
| `DRAFT` | `update`, `delete`, `release` | `edit`/`delete`/`wo_release` | RELEASED | 无 |
| `RELEASED` | `startWork`, `cancel` | `wo_start` | IN_PROGRESS | 写 `actualStartTime` |
| `IN_PROGRESS` | `completeWork` | `wo_complete` | COMPLETED | 写 `actualEndTime`、`goodQty`/`scrapQty` |
| `COMPLETED` | `closeWork` | `wo_close` | CLOSED | 写关闭时间 |
| `CLOSED` | 无（只读） | — | — | — |

#### 4. 状态 × 按钮显示规则

| 状态 | 显示按钮 | 禁用按钮 | 隐藏字段 |
|---|---|---|---|
| `DRAFT` | 编辑/删除/下达 | — | — |
| `RELEASED` | 开工/取消 | 删除 | — |
| `IN_PROGRESS` | 完工 | 编辑/删除 | — |
| `COMPLETED` | 关闭 | 所有编辑类 | — |
| `CLOSED` | — | 全部 | — |

#### 5. 状态机后端实现规则

- 每个状态变更服务必须在 `@MethodService` 内按 IIDP 异常规范分层校验（见 `skills/backend/references/core/platform-standards.md` 异常与事务）：
  - 字段级必填/长度/唯一：在模型层用 `@Validate` 表达
  - 参数缺失或格式错误：抛 `ValidationException`（不触发回滚）
  - 当前状态不允许该流转、权限不足、业务流程失败：抛 `ModelException`（触发请求级事务回滚）
- 状态字段使用 `@Property @Selection(values = {...})`，枚举值与本契约表完全一致
- 状态机服务必须在同一个 `@MethodService` 内完成状态变更与副作用（如生成审批记录），依赖 IIDP 请求级事务统一提交；失败抛 `ModelException` 触发自动回滚。需要分段提交才使用 `Meta` 手动 `flush/commit`（见 `skills/backend/references/core/method-service.md` 事务控制章节）
- 前端按钮显隐通过 `bind_display`/`auth` 配合后端服务校验，**前端控制不替代后端二次校验**

---

## JSON-RPC 基本契约

```json
{
  "id": "guid",
  "jsonrpc": "2.0",
  "method": "service",
  "params": {
    "model": "{model_name}",
    "service": "search",
    "tag": "master",
    "app": "sie-iidp-demo-{appName}",
    "context": {
      "uid": "",
      "lang": "zh_CN"
    },
    "useDisplayForModel": true,
    "args": {
      "filter": [["status", "=", "ENABLE"]],
      "properties": ["id", "name"],
      "limit": 20,
      "offset": 0,
      "order": "id desc"
    }
  }
}
```

| 参数 | 说明 |
|---|---|
| `params.context.uid/lang` | 用户和语言上下文 |
| `params.useDisplayForModel` | `true` 时 ManyToOne/Selection/Dict 返回显示值而非原始值 |
| `args.valuesList` | create 入参（批量） |
| `args.ids` + `args.values` | update 入参 |
| `args.ids` | delete / read 入参 |
| `args.filter/properties/limit/offset/order` | search / find / count 入参 |

> 完整参数契约参照 `skills/backend/references/core/api-filter-sql.md` §JSON-RPC 请求结构。

## Filter 规则

单条件三元组：`[["name", "=", "ABC"]]`

关系字段点访问：`[["org.parent.name", "like", "%华南%"]]`

混合逻辑（前缀波兰表达式）：

```json
["&", ["state", "=", "RELEASED"], ["|", ["userId", "=", "1000"], ["userId", "=", false]]]
```

常用操作符：

| 操作符 | 说明 |
|---|---|
| `=` `!=` `>` `>=` `<` `<=` | 比较 |
| `like` / `ilike` | 模糊；`ilike` 忽略大小写 |
| `not like` / `not ilike` | 反向模糊 |
| `in` / `not in` | 在列表中 |
| `child_of` / `parent_of` | **仅用于树或父子关系模型**；普通模型不支持 |

逻辑符：`|`（OR，二元）、`&`（AND，二元，省略时默认 AND）、`!`（NOT，单目）

- 自定义查询服务必须继续支持平台 Filter、分页、排序和字段选择。

## 按钮与服务契约

```json
{
  "name": "启用",
  "action": "enable",
  "auth": "enable",
  "model": "{model_name}",
  "service": "enableDisable",
  "args": {
    "bind_ids": "$ds.checkedDataIds",
    "statusFlag": "ENABLE"
  },
  "actionAfter": "refreshTable"
}
```

后端必须提供匹配的 `@MethodService`，并在服务中重新查询记录、校验状态和权限，不能只信任前端按钮显隐。

## 节点 id 契约

前端扩展需要节点 id 时按顺序获取：

1. 用户明确提供。
2. 标准模板 ID 规则库推导，并标记为“待确认”。
3. 询问用户提供，或指导在浏览器控制台验证。

不得根据菜单名、按钮文案或模型名自行拼接节点 id。

## 节点属性与事件契约

前端扩展规格必须把“原型里的交互”转换为可执行的 IIDP 节点契约。

| 契约项 | 必写内容 | 常见风险 |
|---|---|---|
| 节点层级 | `page/container/search/grid/form/dialog/table/button` | 只写视觉区域，没有写节点落点 |
| `selector` | 目标节点 id、属性选择器、来源和验证方式 | selector 命中错误节点或运行时 id |
| `ds_config` | `type/name/autoRequest/options/reqPrep/reqAfter` | 数据源名不稳定、参数和后端服务不一致 |
| `bind_` | 绑定属性、数据路径、transform | `$ds` 路径不存在或跨节点选择器错误 |
| `bind_two_` | 表单字段和数据源双向关系 | 双向绑定导致保存参数结构漂移 |
| `bind_on_` | 真实事件名、回调职责、失败处理 | 臆造事件名或混用表格/按钮事件 |
| `commands` | 命令名、入参、调用位置 | 命令隐藏副作用，没有验收点 |
| hook | hook 路径、`vm.super`、读写 `vm.biz` | 覆盖平台默认查询、保存或权限逻辑 |

自定义 Vue2 组件只描述组件边界，不把组件内部状态当作 IIDP 平台事实。组件接入层仍要说明节点属性如何映射到组件 props，以及事件如何回传给 `bind_on_`、数据源或后端服务。
