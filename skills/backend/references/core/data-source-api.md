# 数据源与 API 服务契约

本文件覆盖视图、数据源和公共 API 的基本契约。涉及完整 JSON-RPC 参数、Filter 操作符、成功/失败响应结构时，继续读取 `api-filter-sql.md`。

本文件覆盖数据源、公共 API、Lookup/openView 中涉及的后端接口契约。需要完整底稿时读取 `../complete/api-params.md` 或 `../complete/filter.md`。

## 元模型 API 基本结构

前端通常通过 `window.Tech.httpMeta` 或数据源配置调用：

```json
{
  "jsonrpc": "2.0",
  "method": "service",
  "params": {
    "model": "books_manage",
    "service": "search",
    "filter": [],
    "properties": ["id", "bookName"],
    "limit": 30,
    "offset": 0,
    "order": "id desc",
    "args": {}
  }
}
```

后端必须保证：

- `model` 存在且已被 App 加载。
- `service` 是内置服务或 `@MethodService` 暴露服务名。
- `filter/properties/limit/offset/order/args` 可被方法签名接收。

---

## 内置服务契约

| 服务 | 典型用途 | 后端要求 |
|---|---|---|
| `search` | 表格/选择器查询 | 支持 `Filter`、字段列表、分页、排序 |
| `count` | 表格分页总数 | 与 search 使用同一过滤条件 |
| `find` | 按 ID 或条件查详情 | 详情页、编辑页依赖 |
| `create` | 新增 | 校验必填、唯一、默认值 |
| `update` | 修改 | 校验 ID、业务状态、并发 |
| `delete` | 删除 | 逻辑删除、ER 约束 |
| `lookup` | 弹窗/下拉选择 | 返回 value/display 字段 |
| `loadView` | 加载视图 | `views/*.json` 和菜单 key 必须正确 |

若要重写内置服务，Java 方法名保持内置服务名：

```java
@MethodService(description = "create")
public Object create(List<Map<String, Object>> valuesList) {
    // 业务逻辑
    return getMeta().get(this.getModelName()).callSuper(BooksManage.class, MethodConst.CREATE, valuesList);
}
```

---

## 自定义服务入参

视图按钮 `args` 常用变量：

| 变量 | 场景 |
|---|---|
| `$ds.checkedDataIds` | 表格勾选 ID |
| `$ds.checkedDataList` | 表格勾选行 |
| `$ds.selectedTableData` | 当前选中行 |
| `$table.row` | 主表格当前行 |
| `$table.subRow` | 子表格当前行 |
| `$form.curForm` | 主表单当前数据 |
| `othersParams` | 动态构造参数函数 |

后端服务方法可接收：

```java
@MethodService(description = "批量审核")
public boolean approve(RecordSet rs, String approveStatus, Map<String, Object> extra) {
    // rs 来自 bind_ids 选中的记录集
    return true;
}
```

生成按钮时，`args` 与方法签名必须一一对应；复杂参数优先封装 DTO。

---

## 搜索与统计扩展

表格高级查询、服务端排序、服务端筛选、动态列、合并 search/count 为 `searchPage` 时，后端应提供稳定服务：

```java
@MethodService(description = "searchPage")
public PageViewDTO searchPage(Filter filter, List<String> properties, Integer limit, Integer offset, String order) {
    List<BooksManage> rows = this.search(filter, properties, limit, offset, order);
    Long total = this.count(filter);
    return PageViewDTO.of(rows, total);
}
```

要求：

- 不忽略菜单 `filter`。
- 不默认查全字段和全表。
- 统计接口与列表接口过滤条件一致。

---

## 多数据源

模型级数据源：

```java
@Model(name = "example_data_source_db", dataSource = "example_db", displayName = "外部数据源")
public class ExampleDataSourceDb extends BaseModel<ExampleDataSourceDb> {
}
```

服务级数据源：

```java
@MethodService(description = "同步外部数据", dataSource = "example_db")
public boolean syncExternalData() {
    return true;
}
```

规范：

- 数据源名称必须与环境配置一致。
- 跨数据源写操作要设计幂等和补偿。
- 不在代码中硬编码数据库账号密码。

---

## 第三方 API

第三方 API 可由后端服务封装，前端只调用 IIDP 服务：

```java
@MethodService(description = "查询外部库存")
public List<Map<String, Object>> queryExternalStock(String materialCode) {
    // 调用 Forest/HTTP 客户端
    return Collections.emptyList();
}
```

要求：

- 超时、重试、错误码映射明确。
- 日志记录请求标识，不记录敏感 Token。
- 返回结构稳定，必要时用 ResponseModel。
- 外部接口失败时给前端明确错误，不返回半截数据。

---

## 数据保存契约

主表单、子表、抽屉表单、行内编辑可能触发不同保存方式：

| 场景 | 后端设计 |
|---|---|
| 主表单保存 | 重写 `create/update` 或保持内置服务 |
| 子表 ER 保存 | ER 字段 + `addEr/deleteEr/createEr` |
| 子表单独保存 | 子表模型提供独立 `create/update/delete` |
| 行内编辑 | `update` 支持部分字段更新 |
| openView 确认保存 | 自定义服务，返回成功标识或业务数据 |

---

## 生成检查清单

- 每个前端 `service` 都能对应一个内置服务或 `@MethodService`。
- 自定义服务有明确入参、返回值、异常提示。
- 查询服务支持分页、排序、过滤，不全表扫描。
- 第三方 API 和多数据源不泄露配置。
- 子表、openView、行内编辑的保存服务与视图动作一致。
