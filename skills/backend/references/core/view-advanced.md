# 后端视图高级配置补充

本文件补齐后端视图开发手册里的高级能力。生成或修改后端视图时，先读 `view.md` 的基础结构；遇到联动、弹窗选择器、上下表、权限域、扩展视图、模板继承、复杂服务调用时，再读本文件。需要完整底稿时读取 `../complete/uiview-guide.md`。

## uiview-guide.md 覆盖对照

| 能力章节 | 后端产物 | 必查文件 |
|---|---|---|
| 视图文件结构与配置 | `resolved` 包路径、`views/`、`data/`、`app.json.view/data/events/globalConfig/appConfig` | `pom-structure.md`、本文件 |
| Form/Grid/Search/Tree 基础视图 | `views/*.json` 中 `name/model/type/mode/body` | `view.md`、本文件 |
| 上下表视图 `typeView: subTable` | 主表 grid、子表查询、`searchByMainTable`、外键索引 | `template-hook-openview.md`、本文件 |
| 字段、tbar、buttons | `columns`、`tbar`、`buttons`、`auth/action/service/args` | `view.md`、`data-source-api.md`、本文件 |
| ER 集成 | Java ER 字段、form tabs、`addEr/deleteEr/createEr/updateEr` | `model.md`、`template-hook-openview.md`、本文件 |
| 菜单配置 | `data/menus.json`、`parent_ids`、菜单 `view` | `menu.md`、本文件 |
| 种子数据 | `data/*.json`、`properties`、`@ref`、`@eval` | `menu.md`、本文件 |
| 自定义交互与联动 | `bind_on_changeHandler`、`linkage`、`bind_display`、`transformRes`、`reqPrep/reqAfter` | 本文件 |
| 高级选择器与弹窗 | `useSelectTree`、`useOpenView`、drawer/dialog | `component-field-mapping.md`、`template-hook-openview.md`、本文件 |
| 数据查询与服务调用 | `searchByMainTable`、`searchConfig`、`pagin`、服务按钮 | `data-source-api.md`、本文件 |
| 权限控制与安全 | `auth`、字段级控制、`domains` | `security-permission-i18n.md`、本文件 |
| 视图扩展与继承 | `mode: extension`、`inherit_ids`、`jsonpath`、模板复用 | 本文件 |
| 最佳实践与排查 | 文件组织、命名、性能、按钮/数据排查 | `validation-checklist.md`、本文件 |

---

## app.json 与资源目录

`app.json` 必须放在 `resolved` 对应物理目录下，视图和种子数据放在同级 `views/`、`data/`：

```text
com/sie/iidp/{appPkg}/
├── app.json
├── views/
│   └── {model_name}_view.json
└── data/
    └── menus.json
```

`app.json` 除基础字段外，还可包含：

| 字段 | 后端生成要求 |
|---|---|
| `dependencies` | 跨 App 模型、视图、服务、权限或文件能力依赖必须声明 |
| `view` | 登记所有后端视图 JSON 路径 |
| `data` | 登记菜单、字典、初始化数据、模板数据 |
| `events.broadcast/startUp/register/login` | 只有确有平台事件服务时生成，事件值格式为 `model::service` |
| `globalConfig.sgWhiteList/urlWhiteList` | 只用于平台白名单或全局安全配置，禁止随意放宽 |
| `appConfig` | 业务开关、作用域、运行参数等可配置项；必须有 `desc` 和 `value` |

不要照抄 IAM 示例的版本号、白名单或事件。生成时以当前工程版本和业务真实依赖为准。

---

## 视图公共结构

每个视图 key 下必须保证：

- `name`：权限点和页面显示名，不能为空。
- `model`：与 Java `@Model(name)` 一致。
- `type`：`form/grid/search/tree` 或平台支持的视图类型。
- `mode`：普通视图用 `primary`，扩展视图用 `extension`。
- `body.type`：与视图主体一致。

完整视图文件可以包含 `data` 与 `views`，但业务视图必须放在 `views` 下：

```json
{
  "data": {},
  "views": {
    "books_manage_grid": {
      "name": "图书列表",
      "model": "books_manage",
      "type": "grid",
      "mode": "primary",
      "body": { "type": "grid", "columns": ["bookCode", "bookName"] }
    }
  }
}
```

---

## 表单高级配置

表单字段对象常用属性：

| 属性 | 后端关注点 |
|---|---|
| `displayName/label/name` | 字段必须能在 Java 模型或非存储回显字段中找到 |
| `custom/type` | 自定义组件要有字段类型、字典、选择器或服务支撑 |
| `required` | 前端必填也要在 Java `@Validate` 或服务中校验 |
| `display/hidden/disabled` | 固定显隐或禁用；敏感字段后端仍要校验 |
| `bind_display/bind_disabled/disable_update` | 动态控制依赖的字段必须在返回数据中存在 |
| `defaultValue/defaultValueFn` | 后端也要给关键默认值兜底 |
| `span/row/groupConf` | 仅影响布局，不能替代字段分组或权限逻辑 |

ER tabs 必须与模型 ER 字段一致：

```json
{
  "header": "角色",
  "tbar": [
    { "name": "添加", "action": "addEr", "auth": "update" },
    { "name": "删除", "action": "deleteEr", "auth": "delete" }
  ],
  "body": {
    "type": "app_role_grid,app_role_search,app_role_form",
    "field": "role_ids",
    "checkbox": "multiple_2",
    "checkType": "bgSync",
    "columns": ["name", "code", "remark"]
  }
}
```

检查点：

- `body.field` 等于 `@OneToMany/@ManyToMany` 字段名。
- 子表视图 key 都存在并登记。
- `addEr/deleteEr/createEr/updateEr` 与 ER 类型匹配。
- `submitChanged` 只控制提交变化字段，后端服务不能依赖前端判断完整性。

---

## 表格、上下表与按钮

Grid 常用高级属性：

| 属性 | 后端契约 |
|---|---|
| `checkbox` | 批量服务需要接收 `$ds.checkedDataIds` 或记录集 |
| `checkType: bgSync` | 高亮行与勾选同步，服务仍以 `args` 为准 |
| `typeView: subTable` | 上下表模板；子表查询必须由主表选中行过滤 |
| `dbClickEnterDetails` | 只影响前端交互 |
| `searchByMainTable` | 后端 search/count 服务必须接收固定过滤、分页和排序 |
| `enableCondition` | 只控制按钮可用性，后端服务仍要校验状态 |
| `bind_disabled` | 常用于未勾选禁用批量按钮 |

按钮字段：

| 字段 | 说明 |
|---|---|
| `name/action/auth` | 按钮显示、动作和权限码 |
| `model/service` | 自定义服务必须能在目标模型上调用 |
| `args/params` | 与 Java 方法签名或 DTO 字段一致 |
| `views` | `openView` 或 custom 视图目标；目标视图 key 必须存在 |
| `actionAfter/actionAfterFn/actionBeforeFn` | 前端行为；后端只保证服务返回稳定 |
| `source/properties/fileLimit` | 导入导出或文件按钮契约 |
| `btnType/buttonType/options/message/icon` | 展示或行为扩展，后端不据此跳过校验 |

上下表生成要求：

- 主表 grid 通常配置 `checkbox: single_2` 或默认选中行。
- 子表 `searchByMainTable.args.filter` 要包含主表外键。
- 子表模型外键字段建议加索引。
- 删除主表时必须明确级联、限制删除或软删除策略。

---

## 搜索、日期控件与分页

Search 视图字段可配置 `widget`：

| `widget` | 返回值示例 |
|---|---|
| `year` | `2025` |
| `month` | `2025-05` |
| `date` | `2025-05-28` |
| `datetime` | `2025-05-28 14:30:00` |
| `monthrange` | `["2025-01", "2025-05"]` |
| `daterange` | `["2025-05-01", "2025-05-28"]` |
| `datetimerange` | `["2025-05-28 00:00:00", "2025-05-28 23:59:59"]` |

后端服务要能把范围值转换为 `>=`、`<=` 或平台 `Filter` 表达式，不要把数组当普通字符串等值查。

分页配置：

```json
{
  "pagin": {
    "display": true,
    "unPaging": false,
    "pageSize": 20,
    "pageSizes": [10, 20, 50, 100],
    "layout": "total, sizes, prev, pager, next, jumper"
  }
}
```

若设置 `unPaging: true`，后端仍要限制最大查询量，避免全表返回。

---

## Tree 与树形选择器

Tree 视图常用 `props`：

| 属性 | 后端要求 |
|---|---|
| `parent/children/label/value` | 模型必须有对应父子、显示、值字段 |
| `showRoot/showTree/maxLevel` | 查询服务要支持根节点和层级限制 |
| `subApp/subModel/subViewType` | 右侧视图目标 App、模型、视图必须存在 |
| `subViewFilter/subViewFilterValue` | 点击节点后拼接右侧查询 filter |
| `customSubViewType` | 自定义右侧表单视图 key 必须存在 |
| `expandOnClickNode/hasIcon` | 仅影响前端展示 |

`searchConfig` 用于树数据查询：

```json
{
  "searchConfig": {
    "model": "books_category",
    "service": "queryCategoryTree",
    "args": {
      "filter": [["status", "=", "ENABLE"]],
      "order": "sortNo"
    }
  }
}
```

树形选择器 `useSelectTree` 后端契约：

- 目标树模型可查询。
- `preId` 对应前端节点前缀，生成后不要冲突。
- `type` 为 `single` 或 `multiple` 时，服务返回值结构要支持单选/多选回填。
- `confirm` 只做前端回填，后端保存时仍校验字段合法性。

---

## OpenView、弹窗与抽屉

`useOpenView` 选择器常用字段：

| 字段 | 后端要求 |
|---|---|
| `app/model/type` | 目标 App、模型、视图 key 存在 |
| `checkbox` | 单选/多选返回结构与保存字段一致 |
| `valueField/labelField` | 目标查询结果必须包含这些字段 |
| `args.filter/order/limit/offset` | 目标 search/count 服务必须支持 |
| `confirm` | 前端回填逻辑；后端不依赖它做安全校验 |

弹窗/抽屉配置：

| 字段 | 说明 |
|---|---|
| `showType` | `dialog` 或 `drawer` |
| `width/height/title` | 展示配置 |
| `hasFooter/modal/closeOnClickModal` | 交互配置 |

自定义保存场景要在目标模型提供 `save/create/update` 或明确的 `@MethodService`，并在视图按钮或 openView 配置中写清 `service/args/actionAfter`。

---

## 自定义交互与联动

这些配置主要由前端执行，但后端必须提供字段和服务契约：

| 配置 | 后端契约 |
|---|---|
| `bind_on_changeHandler` | 被改写的字段必须存在，后端保存时重新校验 |
| `bind_on_handleInputBlur` | 只清洗前端输入，服务层仍要做格式校验 |
| `linkage.params/children` | 子字段动态选项服务要接收父字段参数 |
| `linkage.type: filter` | 目标 ManyToOne/Lookup 查询服务要接收由父字段拼出的 Filter |
| `bind_display` 表达式 | 依赖字段必须在 form/grid 数据中返回 |
| `bind_display.transform` | 若依赖用户、角色、权限，后端服务也要校验 |
| `bind_disabled` | 只控制禁用，不能替代后端状态机 |
| `disable_update` | 编辑禁用字段仍不能被 update 服务非法修改 |
| `transformInitValue` | 只改变表单初始化数据形态 |
| `transformRes` | 保存前转换；服务入参要接受转换后的字段 |
| `reqPrep` | 请求前补参；服务方法签名要接收补充参数 |
| `reqAfter` | 请求后过滤；后端返回结构要稳定 |

关联选择字段常出现 `field___values`，后端保存通常只认 `field`，但若业务要保存显示名，需要显式建 `fieldDisplayName` 或非存储回显字段。

联动生成要求：

- 父字段变化时，子字段 `linkage.children` 列表必须与 Java `@Selection(linkageFields = {...})` 对齐。
- 子级选项方法参数顺序必须与视图 `linkage.params` 一致，并把最后一个参数保留给当前 value 回显。
- 对 Lookup/ManyToOne 联动，使用 Filter 传参，不要把父字段硬编码到 SQL 字符串。
- 后端保存时仍以字段值为准，不能信任前端回填的显示文本。

---

## 服务查询与数据域

`searchByMainTable` 标准结构：

```json
{
  "searchByMainTable": {
    "service": {
      "search": "searchUser",
      "count": "countUser"
    },
    "model": "rbac_user",
    "args": {
      "filter": [["userType", "=", "0"]],
      "order": "create_date desc",
      "limit": 20,
      "offset": 0
    }
  }
}
```

后端要求：

- `search` 与 `count` 使用一致过滤条件。
- `filter`、`order`、`limit`、`offset` 不被忽略。
- 复杂树查询可使用 `showRoot/includeChildren/useDisplayForModel` 等 args，但服务中要有默认值和边界值处理。

`domains` 用于字段或数据域过滤：

```json
{
  "domains": [
    { "column": "source", "domain": [["source", "=", "base"]] },
    { "column": "type", "domain": ["|", ["type", "=", "function"], ["type", "=", "api"]] }
  ]
}
```

`domains` 是前端查询约束，后端关键接口仍要合并菜单过滤、作用域和用户权限，不能只信任前端。

---

## 权限与字段级控制

按钮 `auth` 必须稳定、同层级唯一，并能刷新到权限点。字段级控制可写 `bind_display`、`bind_disabled`，但后端必须补充：

- 写服务按角色、状态、作用域校验。
- 不允许修改的字段在 `update` 或自定义保存服务中剔除或拒绝。
- 涉及 `window.tech.userInfo.roles/permissions` 的前端条件，只作为体验优化。
- 菜单过滤、`domains`、服务权限和模型作用域要保持一致。

---

## 扩展视图与 JSONPath

扩展视图使用 `mode: extension` 并通过 `inherit_ids` 指向被扩展视图：

```json
{
  "books_manage_grid_ext": {
    "name": "图书列表扩展",
    "model": "books_manage",
    "type": "grid",
    "mode": "extension",
    "inherit_ids": { "@ref": "base.books_manage_grid" },
    "body": {
      "jsonpath": [
        { "expr": "columns.bookName", "position": "after", "body": ["author"] }
      ]
    }
  }
}
```

`jsonpath.position` 支持：

| 值 | 用途 |
|---|---|
| `inside` | 追加到匹配节点内部 |
| `after` | 追加到匹配节点之后 |
| `before` | 插入到匹配节点之前 |
| `replace` | 替换匹配节点 |
| `attributes` | 修改匹配节点属性 |

生成扩展视图时：

- `inherit_ids.@ref` 必须能解析到已有视图。
- 跨 App 扩展要声明依赖。
- `jsonpath.expr` 要命中稳定字段，不要依赖动态生成 ID。
- `has_not` 可用于原视图没有 tabs/columns 时创建节点。
- 扩展视图也要登记到 `app.json.view` 或确认扫描机制。

---

## 组件动态控制与模板复用

树节点或按钮动态控制：

| 配置 | 说明 |
|---|---|
| `showBtnFn` | 按树节点或行数据返回可显示按钮集合 |
| `buttonsFilterMethod` | 按按钮、当前值、节点数据过滤按钮 |
| `enableCondition` | 控制按钮是否可点 |

基础模板视图可不绑定具体模型，但继承后的业务视图必须补齐 `model/type/mode/name`。模板继承适合统一基础字段和工具栏，不适合隐藏真实权限或服务差异。

---

## 菜单与种子数据补充

菜单文件可以命名为 `menu_view.json` 或统一的 `menus.json`；生成新 Demo 时优先使用 `data/menus.json`，并登记到 `app.json.data`。

菜单基础结构：

```json
{
  "menus": {
    "books_manage_menu": {
      "sequence": "10",
      "name": "books_manage_menu",
      "display_name": "图书管理",
      "model": "books_manage",
      "view": "books_manage_grid,books_manage_search,books_manage_form",
      "parent_ids": { "@ref": "books_root_menu" }
    }
  }
}
```

普通种子数据：

```json
{
  "data": {
    "books_status_enable": {
      "model": "base_dict_item",
      "properties": { "name": "启用", "code": "ENABLE" }
    }
  }
}
```

ManyToMany 种子关系可用 `@eval`：

```json
{
  "role_ids": [
    { "@eval": "[4, @ref(books_admin_role), 0]" }
  ]
}
```

---

## 性能与排查

性能要求：

- Grid/Search 只返回必要字段，`properties` 不默认全量。
- 大数据必须分页；树数据优先懒加载或按父节点查询。
- 可缓存的选项、字典、树节点可以使用前端缓存，但后端服务要幂等。
- 长列表或虚拟滚动属于前端渲染优化，后端仍要控制分页和排序。

排查顺序：

1. `app.json.view/data` 是否登记，资源是否打进 jar。
2. JSON 是否能解析，视图根节点是否为 `views`。
3. `model` 是否与 Java `@Model(name)` 一致。
4. 菜单 `view` 中的 key 是否存在。
5. 按钮 `auth/action/service/model/args` 是否完整。
6. `searchByMainTable/searchConfig/useOpenView` 的目标服务是否存在。
7. 权限点刷新后按钮是否显示且受控。

## 生成检查清单

- 覆盖基础视图：form/grid/search/tree/card。
- 覆盖上下表：`typeView: subTable`、`searchByMainTable`、主子表外键。
- 覆盖字段组件：`columns` 字符串/对象、`groupConf`、`widget`、`custom/type`。
- 覆盖按钮：`tbar/buttons`、全部常用 action、`auth/service/args/actionAfter`。
- 覆盖 ER：OneToMany/ManyToMany、tabs、`addEr/deleteEr/createEr/updateEr`。
- 覆盖菜单和种子：`menus`、`parent_ids`、`data`、`@ref`、`@eval`。
- 覆盖联动：change/blur/linkage/display/disabled/transform/reqPrep/reqAfter。
- 覆盖选择器：`useSelectTree`、`useOpenView`、drawer/dialog。
- 覆盖查询：`searchByMainTable`、`searchConfig`、`pagin`、`domains`。
- 覆盖权限：按钮权限、字段级控制、数据域过滤、后端二次校验。
- 覆盖扩展：`mode: extension`、`inherit_ids`、`jsonpath`、模板复用。
- 覆盖排查：视图不显示、按钮不生效、数据不加载三类问题。
