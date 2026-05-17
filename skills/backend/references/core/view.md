# 视图开发参考（JSON View）

本文件汇总标准模板、搜索视图、表格视图、表单视图、树视图、卡片视图和后端视图放前端工程的后端可用部分。生成后端 Demo 时，以这里的基础结构为主；遇到联动、上下表、选择器、弹窗、数据域、扩展继承和 JSONPath，继续读取 `view-advanced.md`。需要完整底稿时读取 `../complete/basic-view.md` 或 `../complete/uiview-guide.md`。

## 页面加载链路

标准页面渲染链路：

1. 前端底座加载登录页、首页顶部栏、菜单栏等框架。
2. 登录后调用 `loadMenu`，取当前账号有权限的应用和菜单。
3. 菜单里读取 `model` 和 `view`。
4. 前端根据 `model`、`view` 调 `loadView` 获取后端视图 JSON。
5. 前端把后端视图转换成 Vue2.7 + Element UI 组件并渲染。

所以后端生成必须同时保证：

- `model` 与 Java `@Model(name = "...")` 一致。
- `view` 与 JSON 文件中 `views` 下的 key 一致。
- 菜单、视图、模型、服务按钮的 `service` 名称互相能对上。

---

## 文件命名规范

- 文件名：`{model_name}_view.json`
- 根结构：`{ "views": { ... } }`
- 常用视图 key：
  - 列表：`{model_name}_grid`
  - 搜索：`{model_name}_search`
  - 表单：`{model_name}_form`
  - 树：`{model_name}_tree`

历史 demo 中也常见 `demo_{model_name}_grid_search`、`demo_{model_name}_grid_param_form` 这类带前缀的 key。新生成时优先用短 key；如果扩展既有模块，沿用既有 key 风格。

菜单 `view` 字段可配置多个视图 key，用英文逗号连接：

```json
"view": "books_manage_grid,books_manage_search,books_manage_form"
```

---

## 完整视图文件结构

```json
{
  "views": {
    "books_manage_search": {
      "mode": "primary",
      "name": "图书搜索",
      "model": "books_manage",
      "type": "search",
      "body": {
        "type": "search",
        "columns": [
          "bookCode",
          "bookName",
          { "name": "status", "isPrecise": "all" }
        ],
        "row": 2,
        "col": 3
      }
    },
    "books_manage_grid": {
      "mode": "primary",
      "name": "图书列表",
      "model": "books_manage",
      "type": "grid",
      "body": {
        "type": "grid",
        "checkbox": "multiple_2",
        "columns": [
          "bookCode",
          "bookName",
          "author",
          { "name": "status", "displayName": "状态", "custom": true }
        ],
        "buttons": [
          { "name": "详情", "action": "preview", "auth": "read" },
          { "name": "编辑", "action": "edit", "auth": "update" }
        ],
        "tbar": [
          { "name": "新增", "action": "create", "auth": "create" },
          { "name": "删除", "action": "delete", "auth": "delete" },
          {
            "name": "启用",
            "action": "enable",
            "auth": "enable",
            "model": "books_manage",
            "service": "enableDisable",
            "actionAfter": "refreshTable",
            "args": {
              "bind_ids": "$ds.checkedDataIds",
              "statusFlag": "1"
            },
            "bind_disabled": "${$ds.checkedDataList.length === 0}"
          }
        ]
      }
    },
    "books_manage_form": {
      "mode": "primary",
      "name": "图书表单",
      "model": "books_manage",
      "type": "form",
      "body": {
        "type": "form",
        "columns": [
          { "name": "bookCode", "required": true, "custom": true },
          { "name": "bookName", "required": true, "custom": true },
          "author",
          {
            "name": "publishDate",
            "displayName": "出版日期",
            "custom": true,
            "type": "datepicker",
            "defaultValueFn": "return new Date()"
          }
        ],
        "buttons": ["@defaults"],
        "tbar": ["@defaults"]
      }
    }
  }
}
```

字段可直接写字符串，表示使用模型 `fields` 中的字段定义；需要覆盖显示名、必填、组件类型、显隐或默认值时，写对象并配置 `custom: true`。

---

## Search 视图

最小结构：

```json
{
  "type": "search",
  "columns": ["bookCode", "bookName", "status"]
}
```

常用配置：

| 属性 | 说明 | 示例 |
|---|---|---|
| `row` | 默认展示行数 | `2` |
| `col` | 每行搜索项数量，尽量能被 24 整除 | `3`、`4`、`6` |
| `filterTitle` | 隐藏筛选条件标题 | `"hidden"` |
| `showSearchConfig` | 是否显示筛选标题配置图标 | `false` |
| `searchAlign` | 搜索项对齐方式 | `"left"` |
| `techAutoSaveSearch` | 保存最后一次查询方案 | `true` |

搜索字段对象常用配置：

| 属性 | 说明 | 示例 |
|---|---|---|
| `isPrecise` | 查询匹配方式，`all` 精确，`left/right` 单侧模糊，默认左右模糊 | `"all"` |
| `splitSymbol` | 输入字符串拆分成多个 like 条件 | `","` |
| `defaultValue` | 默认固定值 | `"ENABLE"` |
| `defaultValueFn` | 默认值函数 | `"(vm) => new Date()"` |
| `defaultSelected` | 下拉默认选第一个 | `true` |
| `isTrim` | 过滤前后空格 | `true` |
| `cache` | 跨 tab/menu 复用 sessionStorage 值 | `"bookSearch"` |

---

## Grid 视图

`grid` 里要区分：

- `columns`：表格列。
- `buttons`：操作列按钮，例如详情、编辑、打开视图。
- `tbar`：工具栏按钮，例如新增、删除、导入、导出、自定义批量操作。

常用 `grid` 属性：

| 属性 | 说明 | 示例 |
|---|---|---|
| `checkbox` | 表格勾选 | `"multiple_2"`、`"single_2"` |
| `checkType` | 勾选与高亮行同步 | `"bgSync"` |
| `headerSortRemote` | 服务端排序 | `true` |
| `sortMultiple` | 多列排序 | `true` |
| `headerFilterRemote` | 服务端筛选 | `true` |
| `disabledInitAutoSearch` | 禁止初始化自动查询 | `true` |
| `openDetailInTab` | 新菜单标签打开详情页 | `{ "tabMenuTitle": "$menName" }` |
| `gridOptions` | 透传 vxe-grid 原生配置 | `{ "checkbox-config": { ... } }` |
| `mergeAttrs` | 透传到渲染后的前端节点 | `{ "xxx": true }` |

标准工具栏按钮：

```json
{ "name": "新增", "action": "create", "auth": "create" }
{ "name": "删除", "action": "delete", "auth": "delete" }
{ "name": "添加", "action": "addEr", "auth": "update" }
{ "name": "创建", "action": "createEr", "auth": "create" }
{ "name": "删除", "action": "deleteEr", "auth": "delete" }
```

导入按钮：

```json
{
  "name": "导入",
  "action": "import",
  "service": "excelImport",
  "model": "books_manage",
  "auth": "import",
  "fileLimit": {
    "ext": ".xls,.xlsx",
    "maxSize": "2048"
  },
  "args": {
    "bind_ids": "$ds.checkedDataIds",
    "name": "$table.row.name",
    "name2": "$form.curForm.name"
  }
}
```

全量导入/导出模板：

```json
{
  "name": "导入",
  "statement": "_fullImport",
  "modelName": "books_manage",
  "fileName": "@filePath(book_import_template)",
  "buttonList": [
    { "text": "下载模板", "action": "downloadTemplate", "icon": "el-icon-document" },
    { "text": "准确性校验", "action": "importValidate" },
    { "text": "强制导入", "action": "forceImport" },
    { "text": "确认导入", "action": "confirmImport" }
  ]
}
```

```json
{
  "name": "导出",
  "action": "_fullExport",
  "auth": "_fullExport",
  "statement": "_fullExport",
  "modelName": "books_manage"
}
```

自定义服务按钮：

```json
{
  "name": "归还",
  "action": "returnBook",
  "auth": "returnBook",
  "model": "books_borrow",
  "service": "returnBook",
  "actionAfter": "refreshTable",
  "args": {
    "bind_ids": "$ds.checkedDataIds",
    "othersParams": "(vm) => { return { source: 'grid' } }"
  },
  "bind_disabled": "${$ds.checkedDataList.length === 0}",
  "actionBeforeFn": "(vm) => true",
  "actionAfterFn": "(vm) => window.ELEMENT.Message.success('操作成功')"
}
```

启用/禁用按钮（带图标、args 传参、bind_disabled 勾选控制）：

```json
{
  "name": "启用",
  "action": "enable",
  "auth": "enable",
  "model": "example_item",
  "service": "enableDisable",
  "actionAfter": "refreshTable",
  "options": {
    "icon": "el-icon-open"
  },
  "args": {
    "bind_ids": "$ds.checkedDataIds",
    "statusFlag": "1"
  },
  "bind_disabled": "${$ds.checkedDataList.length === 0}"
}
```

导出选中行（`source: "selected"` 仅导出勾选数据）：

```json
{
  "name": "导出",
  "action": "export",
  "source": "selected",
  "model": "example_item",
  "service": "excelExport"
}
```

行级操作按钮启用条件（`enableCondition` 函数）：

```json
{
  "name": "编辑",
  "action": "updateEr",
  "auth": "update",
  "enableCondition": "(row) => {return row[0].isEnable !== true;}"
}
```

表格内联行编辑（`gridEditMode`，来自工程 example_class_view.json / example_item_view.json）：

```json
{
  "gridEditMode": {
    "mode": "multipleRow",
    "rowBtns": ["update", "add", "delete"],
    "defaultCancelEdit": true
  },
  "columns": [
    {
      "name": "classCode",
      "displayName": "班级编码",
      "sortable": true,
      "rowEditable": true
    },
    {
      "name": "className",
      "displayName": "班级名称",
      "rowEditable": true
    }
  ]
}
```

| `gridEditMode` 字段 | 说明 |
|---|---|
| `mode` | 固定 `"multipleRow"` — 多行同时编辑 |
| `rowBtns` | 行内操作按钮，可含 `"update"` / `"add"` / `"delete"` |
| `defaultCancelEdit` | `true` — 点击空白处自动取消编辑 |
| `rowEditable` | 列级配置，`true` 表示该列可内联编辑 |

---

## Form 视图

最小结构：

```json
{
  "type": "form",
  "columns": ["bookCode", "bookName", "status"],
  "buttons": ["@defaults"],
  "tbar": ["@defaults"]
}
```

表单字段常用配置：

| 属性 | 说明 | 示例 |
|---|---|---|
| `required` | 是否必填 | `true` |
| `display` | 固定显示/隐藏 | `false` |
| `bind_display` | 动态显示/隐藏 | `"${$ds.form.status === '1'}"` |
| `bind_disabled` | 动态禁用 | `"${$ds.clickType === 'preview'}"` |
| `custom` | 覆盖模型 fields 定义 | `true` |
| `type` | 自定义组件类型 | `"select"`、`"datepicker"`、`"hidden"` |
| `defaultValue` / `defaultValueFn` | 默认值 | `"ENABLE"`、`"return new Date()"` |
| `rules` | 前端校验规则 | `[{ "required": true, "message": "必填" }]` |
| `row` / `span` | 行和栅格宽度 | `1`、`12` |
| `groupConf` | 表单分组 | `{ "id": "base", "name": "基础信息" }` |

子表 tabs（来自工程 example_class_view.json / example_item_view.json）：

```json
{
  "header": "物料属性列表",
  "isAloneSave": true,
  "tbar": [
    { "name": "新增", "action": "createEr", "auth": "createEr" },
    { "name": "删除", "action": "deleteEr", "auth": "deleteEr" }
  ],
  "body": {
    "type": "grid,form",
    "model": "example_item_attribute",
    "field": "itemAttributeList",
    "hideRelForm": "(vm, val) => { if (val.itemCode == '123') return true; }",
    "gridEditMode": {
      "mode": "multipleRow",
      "rowBtns": ["update", "add", "delete"],
      "defaultCancelEdit": true
    },
    "beforeEditMethod": "({ row, rowIndex, column, columnIndex }) => { if (row.color == 123 && column.field == 'price') { return false; } return true; }",
    "mainTableColumns": [
      {
        "name": "code",
        "custom": true,
        "disabled": true,
        "readonly": true
      }
    ],
    "columns": [
      { "name": "color", "rowEditable": true },
      { "name": "price", "rowEditable": true },
      "weight",
      "itemAttributeType"
    ]
  }
}
```

子表 tab 关键属性：

| 属性 | 位置 | 说明 |
|---|---|---|
| `isAloneSave` | tab 级 | `true` — 该子表有独立保存按钮，不随主表单一起提交 |
| `hideRelForm` | body 级 | 函数，返回 `true` 则隐藏该 tab 的关联表单面板；`val` 为主表单当前数据 |
| `gridEditMode` | body 级 | 内联行编辑，见 Grid 章节 |
| `beforeEditMethod` | body 级 | 函数，返回 `false` 则禁止该单元格编辑；参数 `{ row, rowIndex, column, columnIndex }` |
| `mainTableColumns` | body 级 | 从主表单注入字段到子表 grid；`custom: true` + `disabled/readonly` 通常用于只读回显 |

基础子表 tab（无内联编辑，仅 addEr/deleteEr）：

```json
{
  "header": "学生",
  "isAloneSave": true,
  "tbar": [
    { "name": "添加", "action": "addEr", "auth": "update" },
    { "name": "删除", "action": "deleteEr", "auth": "deleteEr" }
  ],
  "body": {
    "model": "example_student",
    "type": "demo_example_student_grid,demo_example_student_grid_search",
    "field": "studentList",
    "buttons": [
      { "action": "preview", "auth": "read", "name": "详情" },
      {
        "name": "编辑",
        "action": "updateEr",
        "auth": "update",
        "enableCondition": "(row) => {return row[0].isEnable !== true;}"
      }
    ],
    "pagin": { "limit": 0, "display": false, "unPaging": true },
    "columns": ["name"]
  }
}
```

> `"type": "demo_example_student_grid,demo_example_student_grid_search"` — 引用已注册的视图 key 组合，替代内联 `type: "grid,search"`。

表单级常用属性：

| 属性 | 说明 |
|---|---|
| `showEditBtn` | 详情页是否显示编辑按钮 |
| `onlyLookRequired` | 是否显示“只看必填项”开关 |
| `back` | 保存后是否返回列表，`false` 表示停留并刷新当前表单 |
| `title` | 主表单页标题 |
| `search` | 主表单顶部展示搜索按钮 |
| `formConfig.labelPosition` | 表单 label 位置：`right/left/top` |
| `formtabAutoHeight` | 子表自适应高度 |

---

## Card 视图

卡片视图用于轻量列表或概览卡片。后端侧仍然要保证 `model`、`columns`、查询服务和菜单入口完整：

```json
{
  "books_manage_card": {
    "name": "图书卡片",
    "model": "books_manage",
    "type": "card",
    "mode": "primary",
    "body": {
      "type": "card",
      "columns": ["bookCode", "bookName", "author", "status"]
    }
  }
}
```

生成规则：

- `columns` 字段必须来自模型字段或明确的非存储回显字段。
- 卡片列表也要支持 `search/count`、分页和菜单过滤，不能全量返回大表。
- 若卡片有操作按钮，按 Grid 的 `buttons/tbar` 契约生成 `auth/action/service/args`。

---

## Tree 视图

树视图常用于左树右表、树表模板或组织层级维护。

```json
{
  "views": {
    "books_category_tree": {
      "name": "图书分类树",
      "model": "books_category",
      "type": "tree",
      "mode": "primary",
      "body": {
        "type": "tree",
        "model": "books_category",
        "props": {
          "label": "categoryName",
          "children": "children",
          "value": "id"
        },
        "subApp": "sie-iidp-demo-books",
        "subModel": "books_manage",
        "subViewType": "grid,search,form",
        "subViewFilter": "categoryId",
        "subViewFilterValue": "id",
        "showSubView": true,
        "treeWidth": "260px"
      }
    }
  }
}
```

常用配置：

| 属性 | 说明 |
|---|---|
| `model` | 请求树数据的模型 |
| `props.label` | 树节点显示字段 |
| `subModel` | 右侧视图请求模型 |
| `subViewType` | 右侧加载的视图类型或视图 key |
| `subViewFilter` | 右侧查询拼接的 filter 字段 |
| `subViewFilterValue` | 从当前树节点取值的字段 |
| `isLoadViews` | 点击树节点是否重新调用右侧 `loadView` |
| `showSubView` | 未选中树节点时是否显示右侧视图 |
| `buttons` | 树节点操作按钮 |

---

## 后端视图放前端工程

调试视图时，可以把后端 `views/xx_view.json` 临时放进前端工程：

1. 在后端工程找到 `views/xx_view.json`。
2. 拷贝到前端工程 `apps/{app}/views/` 或业务约定目录。
3. 改成 `.js` 文件并 `export default` 视图对象。
4. 热更新启动后，前端优先使用本地视图。

优先级：前端本地视图高于后端 `loadView` 返回视图。完成调试后，应把最终 JSON 同步回后端视图文件，避免前后端视图漂移。

---

## 生成检查清单

- `views` 根节点存在，且每个视图 key 全局不冲突。
- 每个视图的 `model` 等于 Java `@Model(name)`。
- 菜单 `view` 字段包含本页面需要的视图 key。
- `grid` 中工具栏按钮放 `tbar`，操作列按钮放 `buttons`。
- `card` 视图的 `columns`、查询服务和菜单入口完整。
- 自定义服务按钮的 `model`、`service`、`args` 与 Java `@MethodService` 方法签名一致。
- 表单字段需要覆盖模型字段时加 `custom: true`。
- 子表 `body.field` 等于 Java 模型中的 ER 字段名。
- 新视图文件已在 `app.json` 的 `view` 数组登记，或已确认当前 App 会自动扫描视图资源。
