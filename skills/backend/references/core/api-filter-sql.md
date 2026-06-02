# API、Filter、SQL 与服务运行契约

本文件补齐后端接口和服务运行规则。生成 `@MethodService`、视图按钮服务、查询服务、原生 SQL 或异步服务时，除 `method-service.md` 与 `data-source-api.md` 外，还要读取本文件。需要完整底稿时读取 `../complete/filter.md`、`../complete/api-params.md`、`../complete/method-service.md` 或 `../complete/native-sql.md`。

## 完整底稿覆盖

| 完整底稿 | 本文件覆盖点 |
|---|---|
| `../complete/filter.md` | Filter 数组结构、波兰表达式、逻辑符、操作符、父子关系操作 |
| `../complete/api-params.md` | JSON-RPC 请求/响应、`params.args/context/model/service/app/tag` 参数契约 |
| `../complete/method-service.md` | 服务声明规则、通用服务、服务编排、异步线程调用模型 |
| `../complete/native-sql.md` | `RelationDBAccessor`、参数化 SQL、IN 查询、写 SQL 后缓存失效 |

---

## JSON-RPC 请求结构

平台服务远程调用遵循 JSON-RPC 2.0：

```json
{
  "id": "guid",
  "jsonrpc": "2.0",
  "method": "service",
  "params": {
    "args": {
      "ids": ["id1"],
      "values": {},
      "valuesList": [],
      "filter": [],
      "properties": ["id", "name"],
      "limit": 31,
      "offset": 0,
      "order": "id desc"
    },
    "context": {
      "uid": "",
      "lang": "zh_CN"
    },
    "model": "books_manage",
    "service": "search",
    "tag": "master",
    "app": "sie-iidp-demo-books",
    "useDisplayForModel": true
  }
}
```

参数契约：

| 参数 | 说明 |
|---|---|
| `id` | 请求 ID，建议 UUID |
| `jsonrpc` | 固定 `2.0` |
| `method` | 通常为 `service` |
| `params.model` | 模型名 |
| `params.service` | **（必填）** 服务名或接口名。漏传会导致引擎 NPE，即使是内置 CRUD 也必须指明 `"search"` `"find"` `"create"` 等 |
| `params.app` | 跨 App 调用时指定应用 |
| `params.tag` | 默认 `master` |
| `params.context.uid/lang` | 用户和语言上下文 |
| `args.ids` | 主键集合 |
| `args.values` | update 入参 |
| `args.valuesList` | create 入参 |
| `args.filter` | 查询过滤条件 |
| `args.properties` | 查询字段列表 |
| `args.limit/offset/order` | 分页与排序 |
| `useDisplayForModel` | ManyToOne/Selection/Dict 显示值开关 |

响应：

- 成功：`result.data` 存放数据，`result.context` 可返回上下文。
- 失败：`error.code/message/data` 存放错误；不要把内部堆栈、SQL、密钥直接暴露给前端。

---

## 内置服务参数速查

| 服务 | 入参 | 生成要求 |
|---|---|---|
| `create` | `valuesList` | 支持批量；每项都校验必填、唯一、默认值 |
| `delete` | `ids` 或当前 `RecordSet` | 校验状态、权限、ER 约束 |
| `update` | `ids` + `values` | 禁止非法字段更新；支持局部更新 |
| `read` | `ids/properties` | 只返回请求字段 |
| `find` | `filter/limit/offset/order` | 返回单条或匹配记录 |
| `search` | `filter/properties/limit/offset/order` | 支持分页、排序、显示值 |
| `count` | `filter` | 与 search 过滤一致 |
| `exists` | `ids` | 返回存在记录 |

自定义服务：

- Java 方法必须 `public`。
- 同一模型服务名唯一，不支持重载。
- 视图按钮里的 `service` 使用 `@MethodService(name)` 或方法名。
- 方法入参顺序与视图 `args`、API `args` 保持一致；复杂入参用 DTO。

---

## Filter 表达式

Filter 通常是数组，单个条件为三元组：

```json
[["name", "=", "ABC"]]
```

关系字段可用点访问：

```json
[["org.parent.name", "like", "%华南%"]]
```

常用操作符：

| 操作符 | 说明 |
|---|---|
| `=` / `!=` / `>` / `>=` / `<` / `<=` | 比较 |
| `like` / `ilike` | 模糊匹配；`ilike` 忽略大小写 |
| `not like` / `not ilike` | 反向模糊 |
| `in` / `not in` | 是否在列表中 |
| `child_of` | 判断是否为某节点子记录 |
| `parent_of` | 判断是否为某节点父记录 |

逻辑符使用前缀表达：

| 逻辑符 | 说明 |
|---|---|
| `|` | OR，二元 |
| `&` | AND，二元；省略时默认 AND |
| `!` | NOT，单目 |

示例：

```json
[
  "&",
  ["state", "=", "confirm"],
  "|",
  ["user_id", "=", "1000"],
  ["user_id", "=", false]
]
```

生成规则：

- 多条件默认 AND 时，可直接写条件列表。
- 混合 OR/AND 时要按波兰表达式放置逻辑符，避免歧义。
- `in/not in` 的值必须是数组；大数组要分批或限制大小。
- `child_of/parent_of` 只用于树或父子关系模型。
- 任何来自前端的 filter 都要叠加菜单过滤、作用域和服务端权限。

---

## 服务编排

服务编排用于组合已有服务对外形成新服务。声明时通过 `@MethodService` 指定编排定义：

```java
@MethodService(
    name = "approveFlow",
    auth = "approve",
    description = "审批服务编排",
    ServiceOrchestrateDefine = ApproveServiceOrchestrateDefine.class
)
public String approveFlow(String orderId) {
    return orderId;
}
```

生成要求：

- 编排服务仍按普通服务暴露和鉴权。
- 编排节点名称稳定，便于排查。
- 条件网关要覆盖默认分支，避免无路径可走。
- 编排中每个被调用服务都要幂等，失败时给出明确异常。
- 涉及写操作时要检查事务边界和补偿策略。

---

## 异步执行与线程上下文

模型方法在请求线程中天然拥有 `Meta` 上下文。第三方线程调用模型能力时，必须创建新 `Meta` 并设置上下文：

```java
try (Meta meta = cachedMeta.createNewMeta()) {
    BaseContextHandler.setMeta(meta);
    // 执行模型操作
    meta.flush();
} finally {
    BaseContextHandler.remove();
}
```

规范：

- 只在启动事件或服务调用时缓存必要的 `Meta` 引用。
- 第三方线程必须使用 `createNewMeta()`，不能复用请求线程 `Meta`。
- 线程结束必须 `BaseContextHandler.remove()`。
- 写操作完成后按需要 `flush/commit`。
- 异步任务要有任务 ID、幂等控制和错误日志。

---

## 原生 SQL

只有在模型 API 无法表达复杂查询或存在明确性能原因时使用原生 SQL：

```java
BussModelDataAccess dataAccess =
    (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
RelationDBAccessor accessor = dataAccess.getRelationDBAccessor();
accessor.execute("SELECT * FROM books_manage WHERE status=%s", Arrays.asList("ENABLE"));
List<Map<String, Object>> rows = accessor.fetchMapAll();
```

多条件：

```java
accessor.execute(
    "SELECT * FROM books_manage WHERE book_code=%s AND status=%s",
    Arrays.asList(bookCode, status)
);
```

IN 查询：

```java
accessor.execute(
    "SELECT * FROM books_manage WHERE id IN %s",
    Arrays.asList(Arrays.asList("id1", "id2"))
);
```

写 SQL 后必须清缓存：

```java
rs.getMeta().getStore().invalidate();
```

SQL 规则：

- 使用 `%s` 占位和参数列表，不拼接用户输入。
- 控制查询字段、分页和排序。
- 写 SQL 后清理缓存，避免模型数据不一致。
- 不在循环中重复执行单条 SQL；批量处理要合并或分批。
- 不用 `System.out.println` 输出 SQL 结果，使用日志并控制敏感字段。

---

## 接口错误处理

失败响应结构包含 `error.code/message/data`。后端服务要：

- 用户输入问题抛 `ValidationException`，message 给业务可理解提示。
- 平台模型问题保留 `ModelException`，不吞原因。
- 生产环境不向前端暴露 `data.debug` 中的 SQL、密钥、堆栈。
- 日志记录 request id、model、service、关键业务 id。

---

## 生成检查清单

- JSON-RPC 入参包含 `model/service/tag/context/args`，视图按钮 args 能映射到服务方法。
- `create/update/delete/read/find/search/count/exists` 的参数契约不被破坏。
- Filter 操作符、逻辑符、关系字段路径合法。
- 前端 filter 已叠加菜单过滤、作用域和后端权限。
- 原生 SQL 使用参数化占位；写 SQL 后清缓存。
- 服务编排有稳定节点、默认分支、异常和事务处理。
- 异步线程使用新 Meta，上下文清理完整。
