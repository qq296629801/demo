# 模板、Hook 与 OpenView 后端契约

本文件覆盖标准模板、主表单 + 子表、上下表、树表、抽屉表单、openView、前端 Hook 在后端侧需要满足的契约。

## 标准模板

标准模板依赖菜单的 `model/view` 加载后端视图：

- 列表页：`grid + search + form`。
- 树页：`tree + form + grid + search`。
- 主表单：`form.tabs` 定义子表。
- 上下表：主表 grid 选中行驱动子表 search/grid。

后端生成时，不要只生成 Java 模型；必须同时生成菜单和视图。

---

## 主表单 + 子表

Java ER 字段：

```java
@OneToMany
@Property(displayName = "借阅明细")
private List<BooksBorrowDetail> detailList;
```

Form tabs：

```json
{
  "header": "借阅明细",
  "tbar": [
    { "name": "新增", "action": "createEr", "auth": "create" },
    { "name": "删除", "action": "deleteEr", "auth": "delete" }
  ],
  "body": {
    "type": "grid,form,search",
    "field": "detailList",
    "columns": ["bookCode", "bookName", "borrowQty"],
    "buttons": ["@defaultsEr"]
  }
}
```

要求：

- `body.field` 等于 Java ER 字段名。
- 子表模型有独立字段校验。
- 子表单独保存时，子表服务不能依赖主表未提交数据。

---

## 上下表模板

主表：

```json
{
  "type": "grid",
  "checkbox": "single_2",
  "hasDefaultSelectRow": true,
  "columns": ["borrowCode", "readerName"]
}
```

子表通过主表选中行过滤：

```json
{
  "type": "grid",
  "searchByMainTable": {
    "field": "borrowId",
    "mainField": "id",
    "service": "search",
    "model": "books_borrow_detail"
  },
  "columns": ["bookCode", "bookName"]
}
```

后端要求：

- 子表外键字段有索引。
- 子表 `search` 支持按主表 ID 过滤。
- 删除主表时明确子表级联策略。

---

## 树表模板

树模型建议字段：

```java
@Property(displayName = "节点编码", length = 64)
private String nodeCode;

@Property(displayName = "节点名称", length = 120, displayForModel = true)
private String nodeName;

@Property(displayName = "父节点ID", length = 64)
private String parentId;

@Property(displayName = "是否叶子")
private Boolean leaf;
```

Tree 视图配置见 `view.md`。后端服务需支持：

- 查询根节点。
- 按父节点查询子节点。
- 搜索结果树状展示时，返回可组装树的父子字段。
- 新增/删除/编辑节点时维护父子关系和排序。

---

## OpenView

openView 会打开另一个后端视图，后端侧必须保证目标 `model/view/service` 可独立工作。

常见配置：

```json
{
  "name": "选择图书",
  "action": "openView",
  "auth": "read",
  "openView": {
    "showType": "dialog",
    "model": "books_manage",
    "type": "books_manage_grid,books_manage_search",
    "customDefaultValue": {
      "status": "ENABLE"
    }
  }
}
```

后端要求：

- 被打开模型有查询视图。
- 自定义 search/count/save 服务存在。
- `customDefaultValue` 对应字段存在。
- 多选返回值能被调用方保存。

---

## 弹窗与抽屉表单

弹窗/抽屉只是前端展示方式，后端关注：

- 表单视图字段完整。
- 保存服务可独立调用。
- 默认值、只读、必填、联动字段有模型支撑。
- 抽屉表单翻页时 `find/search` 支持当前数据范围。

---

## Hook 后端契约

| Hook 场景 | 后端要提供 |
|---|---|
| 查询前后 Hook | `search/count/searchPage` 参数稳定，返回字段完整 |
| 保存前后 Hook | `create/update` 或自定义保存服务，异常提示明确 |
| 删除前 Hook | `delete` 支持业务状态校验和 ER 约束 |
| 按钮显隐/禁用 Hook | 服务 `auth`、状态字段、选中行字段 |
| 行内编辑 Hook | `update` 支持部分字段更新 |
| 子表 Hook | ER 字段、子表服务、外键字段 |
| 树 Hook | 树查询、保存、删除服务 |

不要为了 Hook 生成重复服务；先复用平台内置 CRUD，只有业务规则超出内置能力时才新增 `@MethodService`。

---

## 自定义页面与业务组件

自定义页面通常由前端实现，后端只提供：

- 数据模型。
- 查询/统计/保存服务。
- 菜单入口，必要时 `view: "custom"`。
- 静态资源或模板文件。
- API 文档或 DTO。

---

## 生成检查清单

- 模板类型已明确：主表格、主表单、树表、上下表、卡片、自定义。
- 模板需要的所有视图 key 都在 JSON 中存在。
- 子表 `field` 与 Java ER 字段一致。
- openView 目标模型和服务可独立调用。
- Hook 依赖的服务、字段、权限、状态全部存在。
- 纯前端展示不生成空后端实现，只提供数据契约。
