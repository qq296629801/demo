# IIDP 标准模板节点 ID 规则库

> 从 IIDP 前端源码 (`t-base/components/TechMetaPage/schema/views/`) 提取的标准模板节点结构和 ID 命名规则。
> AI 可根据页面类型直接推导标准模板页面的节点 ID，无需运行时数据。

## 重要规则

- **page 类型节点不能被 replace**：需要替换整个页面内容时，应 replace `page` 节点下的 `container` 子节点，而非 `page` 节点本身。
- **禁止 replace `container_main`**：`container_main` 是整个标准页根容器（包含树、内容区、详情区等），replace 它会导致页面结构异常。需要替换整个表格页内容时，应 replace `table_main_wrap`（表格页视图根节点）。

## ID 前缀规则

源码中 `addIdPre` 函数会对所有节点 ID 加前缀：`fullId = idPre + baseId`

- `idPre` 通常是菜单路径，如 `rbac_role_menu_`
- Hook 挂载点 = `{idPre}container_main`，即页面顶级节点
- 扩展中的 selector 需要使用 fullId

## 子表 ID 前缀规则（idPreTab）

子表每个 tab 有独立的 ID 前缀 `idPreTab`，构造规则：

```
idPreTab = idPre + 'tab_' + addTag + '_'
```

`body.field` 来源：后端视图 JSON 中 form 视图里的子表 tab 定义的 `body.field` 属性，对应 Java 模型 `@OneToMany` / `@ManyToMany` 注解的 ER 关系字段名。从前端获取时，通过 `loadView` 接口返回的视图对象中读取。具体的 `field` 值需查阅 SDD 流程生成的契约产物文件（模板规范见 `@skills/create-project/references/sdd-contracts.md`）。

视图 JSON 示例（子表 tab 定义）：

```json
{
  "type": "form",
  "tabs": [
    {
      "header": "物料属性列表",
      "body": {
        "type": "item_attribute_grid,item_attribute_search",
        "field": "itemAttributeList",
        "columns": [...]
      }
    }
  ],
  "buttons": ["@defaults"]
}
```

上例中 `body.field = "itemAttributeList"`（字符串），对应 Java 模型中 `@OneToMany` 字段名 `itemAttributeList`。

`addTag` 的取值逻辑（按优先级）：

| 条件                                  | addTag 值                                                | 示例                                                                             |
| ------------------------------------- | -------------------------------------------------------- | -------------------------------------------------------------------------------- |
| tab 的 `body.field` 是对象（ER 关系） | `field.attrs.relateModel + (field.attrs.unique \|\| '')` | `relateModel` 为 `rbac_permission`，`unique` 为空 → `addTag = "rbac_permission"` |
| tab 的 `body.field` 是字符串          | `field` 字段名                                           | `field = "orders"` → `addTag = "orders"`                                         |
| tab 无 `body.field`                   | tab 的索引值 `i`（从 0 开始）                            | 第 0 个 tab → `addTag = 0`                                                       |
| 上述结果与其他 tab 重复               | 追加 `_` + 索引                                          | `"orders"` 重复 → `"orders_1"`                                                   |

完整示例：`idPre = "rbac_role_menu_"`，tab 的 field 为 `"orders"` → `idPreTab = "rbac_role_menu_tab_orders_"`

## 标准页入口

源码文件：`index/dynamic/page.js`

```
idPre + "container_main"                  type: container  标准页根容器（hook 挂载点）
  idPre + "tree_main"                     type: tree       树（默认隐藏，树形页面时显示）
  idPre + "container_main_content"        type: container  内容区容器
    idPre + "empty_main"                  type: empty      空状态提示（默认隐藏）
    idPre + "table_main"                  type: container  表格主页（gridPage 生命周期挂载点）
      （table_main 内部由 tablePage.js 动态填充，结构见下方"表格页"章节）
    idPre + "table_detail"                type: container  详情区（默认隐藏，点击行/新增/编辑时显示）
      （table_detail 内部由 formPage.js 动态填充，结构见下方"表单页"章节）
```

---

## 表格页（gridPage）

源码文件：`tablePage.js` + `table-search.js`

`tablePage.js` 导出的根节点 `table_main_wrap` 会作为 `table_main` 的内容动态加载。

```
idPre + "table_main_wrap"                              type: container   表格页视图根容器
  idPre + "container_table_search"                     type: container   搜索区域
    idPre + "form_main_table_search"                   type: container   搜索表单容器
      idPre + "form_main_table_search_common"          type: form       常规搜索表单
      idPre + "form_main_table_search_dropdown"        type: form       下拉搜索表单
    idPre + "container_table_search_wrap"              type: container   搜索按钮容器
      idPre + "form_main_table_search_btn"             type: button     搜索/重置按钮
      idPre + "dropdown_pop_form"                      type: meta-dropdown  下拉搜索面板
      idPre + "form_main_table_search_configs_btn"     type: popover    搜索配置
  idPre + "table_main_table"                           type: table      表格主体
  idPre + "table_main_toolbar"                         type: container  工具栏
    idPre + "table_main_toolbar_refresh"               type: button     刷新按钮
    idPre + "table_main_toolbar_showColumns"            -                列显示配置
    idPre + "table_toolbar_{action}"                    type: button     后端视图定义的业务按钮（action 为按钮的 action 值）
  idPre + "table_main_paging"                          type: paging     分页组件
```

Hook 路径（$improve 映射）：

- `gridPage.*`：`grid.canCreate`、`grid.canDelete`、`grid.onConfirm`、`grid.beforeDelete`、`grid.delete`、`grid.afterDelete`、`grid.canEdit`、`grid.beforeQuery`、`grid.query`、`grid.afterQuery`、`grid.queryCount`、`grid.select`、`grid.cancelSelect`、`grid.beforeToolbar`
- `grid.edit.*`：行内编辑相关
- `search.*`：搜索相关（`search.validateQuery`、`search.canQuery`）

---

## 表单页（detailPage）

源码文件：`formPage.js` + `formMain.js`

```
idPre + "form_main_detail_top"           type: container   表单页根容器
  idPre + "form_main_detail_top_header"  type: container   表单头部
    idPre + "form_main_detail_top_back"  type: button      返回按钮
  idPre + "form_main_detail_top_common"  type: form        表单主体
```

Hook 路径（$improve 映射）：

- `form.beforeQuery`、`form.query`、`form.afterQuery`、`form.init`、`form.beforeSave`、`form.save`、`form.afterSave`、`form.validate`

---

## 树表页

源码文件：`tree.js`

```
idPre + "tree_main_wrap"                         type: container   树形页面根容器
  idPre + "tree_main_wrap_left"                  -                 左侧树区域
  idPre + "tree_main_wrap_right_table"           -                 右侧表格
  idPre + "tree_main_wrap_left_dialog"           -                 树编辑对话框
  idPre + "tree_table_detail"                    -                 树表单详情区
```

Hook 路径（$improve 映射）：

- `tree.created`、`tree.mounted`、`tree.destroy`、`tree.canCreate`、`tree.canDelete`、`tree.canEdit`、`tree.canQuery`、`tree.beforeSave`、`tree.save`、`tree.afterSave`、`tree.onConfirm`、`tree.beforeDelete`、`tree.delete`、`tree.afterDelete`、`tree.beforeQuery`、`tree.query`、`tree.afterQuery`、`tree.select`、`tree.cancelSelect`

---

## 子表（主表单中的 tabs 子表）

源码文件：`form-tabs.js` + `form-tabs-table.js` + `form-tabs-formpart.js`

主表单子表通过 tabs 组织，每个 tab 有自己的 idPre（`idPreTab`）。

```
idPre + "form-tabs"                              type: container   子表 tabs 父容器（主表单详情页中）
  idPre + "form-tabs-wrap"                       type: container   tabs 包装容器
    idPre + "form-tabs-node"                     type: tabs        tabs 组件（动态加载子表内容）
```

子表 Tab 内的表格结构（每个子表有自己的 idPreTab）：

```
{idPreTab} + "container_main_content"            type: container   子表内容容器
  {idPreTab} + "table_main"                      type: container   子表容器
    {idPreTab} + "table_main_table"              type: table       子表表格
    {idPreTab} + "table_main_toolbar"            type: container   子表工具栏
    {idPreTab} + "container_table_search"        type: container   子表搜索区
    {idPreTab} + "table_main_paging"             type: paging      子表分页
```

子表 Tab 内的表单部分（formpart）：

```
{idPreTab} + "container_formpart"                type: container   子表表单部分容器
  {idPreTab} + "form_formpart"                   type: form        子表表单
```

子表相关容器：

```
idPre + "container_form_main_wrap"               type: container   主表单区域包装容器
idPre + "container_table_main_wrap"              type: container   子表 tabs 区域包装容器
```

子表 Hook 路径：

- `form.tabs.<field>.grid`：主表单子表的表格钩子
- `grid.tabs.<field>.grid`：上下表子表的表格钩子

---

## 抽屉/弹窗表单

源码文件：`pop-drawer-form.js` + `pop-select-table.js`

### 抽屉表单

```
idPre + "table_drawer"                           type: drawer      抽屉表单
  idPre + "table_drawer_title"                   type: container   抽屉标题区
  idPre + "table_drawer_form"                    type: form        抽屉表单主体
  idPre + "meta_set_drawer_form"                 type: container   抽屉表单配置区
  idPre + "drawer_tab_sub_table_wrap"            type: container   抽屉子表容器
    idPre + "drawer_form_tabs_node"              type: tabs        抽屉子表 tabs
```

Hook 路径（$improve 映射）：

- `drawerForm.queryView`、`drawerForm.beforeQuery`、`drawerForm.query`、`drawerForm.afterQuery`、`drawerForm.init`、`drawerForm.validate`、`drawerForm.beforeSave`、`drawerForm.save`、`drawerForm.afterSave`

### 弹窗选表

```
idPre + "dialog_pop_select_table"                type: dialog      弹窗选表
  idPre + "table_pop_body"                       type: container   弹窗主体
    idPre + "table_pop_left_ctn"                 type: container   左侧已选列表
      idPre + "table_pop_left_title"             type: container   左侧标题区
        idPre + "table_pop_selectAll"            type: checkbox    全选
      idPre + "table_pop_left_body"              type: container   左侧列表体
        idPre + "checkbox-group_selected_list"   type: checkbox-group  已选列表
    idPre + "table_pop_right_ctn"                type: container   右侧搜索表格
      idPre + "table_pop_form_ctn"               type: container   搜索表单容器
        idPre + "table_pop_form"                 type: form        搜索表单
        idPre + "table_pop_form_dropdown"        type: meta-dropdown  搜索下拉
      idPre + "table_pop_toolbar"                type: container   工具栏
        idPre + "table_pop_paging"               type: paging      分页
        idPre + "table_pop_refresh"              type: button      刷新
      idPre + "table_pop_content"                type: table       选择表格
```

---

## 固定页面节点 ID（无 idPre 前缀）

以下页面不使用 idPre 前缀，节点 ID 是固定值。

### 登录页

源码文件：`login/login.js`

```
page_meta_login                                  type: page        登录页根
  container_meta_login_top                       type: container   登录顶部容器
    container_meta_login_contents                type: container   登录内容容器
      container_meta_login_text                  type: container   登录标题
        container_meta_login_content             type: container   登录表单容器
          row_meta_login_row0                    type: row         用户名行
            inp_meta_login_name                  type: input       用户名输入框
          row_meta_login_row1                    type: row         密码行
            inp_meta_login_pwd                   type: input       密码输入框
          row_meta_login_row2                    type: row         记住密码行
            checkbox_meta_login_remember         type: checkbox    记住密码
          button_meta_login_btn                  type: button      登录按钮
    container_meta_login_bottom                  type: container   登录底部
      container_meta_login_bottom_text           type: text        底部文字
```

### 注册页

源码文件：`register/register.js`

```
register                                         type: page        注册页根
  base_overview_menu_container_main_register     type: container   注册容器
    container_meta_login_content                 type: container   注册表单容器
      （类似登录页结构）
```

### 主页框架

源码文件：`index/index.js`

```
page_meta_index                                  type: page        主页根
  container_meta_header_top                      type: container   头部
  menu_meta_siderbar                             type: container   侧边栏/菜单
  main_cover_container                           type: container   内容区 cover
```

### 头部

源码文件：`index/header.js`

```
container_meta_header_top                        type: container   头部根容器
  container_meta_header_left                     type: container   头部左侧
    container_meta_header_logo                   type: container   Logo 区域
      container_meta_header_user_icon            type: text       Logo/图标
  container_meta_header_right                    type: container   头部右侧
    user_change_role                             type: container   切换角色
    dropdown_meta_header_skin                    type: meta-dropdown  皮肤下拉
      container_meta_header_skin                 type: container   皮肤选项
    container_meta_header_user_icon              type: container   用户头像容器
      meta_header_user_icon                      type: image       用户头像
    dropdown_meta_header_user                    type: meta-dropdown  用户下拉
      container_meta_header_user_top             type: container   用户下拉内容
        button_meta_header_user_index            type: button      个人中心
        button_meta_header_user_rest_pwd         type: button      修改密码
        button_meta_header_user_exit             type: button      退出登录
    pop_top_rest_pwd_container                   type: container   修改密码弹窗
```

### 侧边栏/菜单

源码文件：`index/sidebar.js`

```
menu_meta_siderbar                               type: container   侧边栏根容器
（菜单项为动态生成）
```

### 内容区

源码文件：`index/content.js`

```
container_meta_main_content_wrap                 type: container   内容区根容器
  container_first_reset_password                 type: container   首次修改密码
    dialog_meta_rest_pwd                         type: dialog      修改密码弹窗
      form_meta_rest_pwd                         type: form        修改密码表单
  tabs_meta_main_content_labels                  type: tabs        多标签页 tabs
  container_meta_footer_content                  type: container   底部
    container_meta_footer_content_bread          type: container   面包屑容器
      breadcrumb_meta_footer_breadcrumb          type: breadcrumb 面包屑
    container_meta_footer_copyright_wrap         type: container   版权容器
      container_meta_footer_copyright            type: text        版权文字
```

### 修改密码弹窗

源码文件：`index/resetPwd.js`

```
dialog_meta_rest_pwd                             type: dialog      修改密码弹窗
  form_meta_rest_pwd                             type: form        修改密码表单
    row_meta_rest_pwd_np                         type: row         新密码行
      input_meta_rest_pwd_np                     type: input       新密码输入
    row_meta_rest_pwd_npa                        type: row         确认密码行
      input_meta_rest_pwd_npa                    type: input       确认密码输入
```

### 角色权限弹窗

源码文件：`roleAuthority/index.js`

```
dialog_meta_role_authority                       type: dialog      角色权限弹窗
  tabs_meta_role_authority                       type: tabs        权限 tabs
    container_meta_role_authority_system         type: container   系统权限 tab
      tree_meta_role_authority_system            type: tree        系统权限树
    container_meta_role_authority_diytem         type: container   自定义权限 tab
```

### 空页面

源码文件：`index/dynamic/emptyPage.js`

```
container_main_empty                             type: container   空页面容器
```

---

## 业务术语 → 节点 ID 快速映射

### 标准业务页面（需加 idPre）

| 业务描述                      | 基础 ID                           | 说明                                        |
| ----------------------------- | --------------------------------- | ------------------------------------------- |
| 页面顶级容器 / hook 挂载点    | `container_main`                  | 标准 hook 扩展的 selector 目标              |
| 内容区容器                    | `container_main_content`          | 包含表格主页和详情区                        |
| 表格主页（gridPage 生命周期） | `table_main`                      | gridPage $improve 映射的挂载节点            |
| 详情区（detailPage 生命周期） | `table_detail`                    | detailPage $improve 映射的挂载节点          |
| 表格页视图根容器              | `table_main_wrap`                 | tablePage.js 导出的视图根节点               |
| 搜索区                        | `container_table_search`          | 搜索区域容器                                |
| 搜索表单（常规）              | `form_main_table_search_common`   | 常规搜索字段                                |
| 搜索表单（下拉）              | `form_main_table_search_dropdown` | 下拉/更多搜索字段                           |
| 搜索按钮                      | `form_main_table_search_btn`      | 搜索/重置按钮                               |
| 主表格                        | `table_main_table`                | 表格数据区域                                |
| 工具栏                        | `table_main_toolbar`              | 表格上方工具栏                              |
| 工具栏业务按钮                | `table_toolbar_{action}`          | 后端视图定义的按钮，action 为按钮 action 值 |
| 刷新按钮                      | `table_main_toolbar_refresh`      | 工具栏刷新                                  |
| 分页                          | `table_main_paging`               | 分页组件                                    |
| 表单页根容器                  | `form_main_detail_top`            | detailPage 视图根节点                       |
| 表单页头部                    | `form_main_detail_top_header`     | 详情页头部                                  |
| 返回按钮                      | `form_main_detail_top_back`       | 详情页返回                                  |
| 表单主体                      | `form_main_detail_top_common`     | 详情页表单                                  |
| 子表 tabs                     | `form-tabs`                       | 主表单子表 tabs 父容器                      |
| 子表 tabs 组件                | `form-tabs-node`                  | 动态加载子表内容的 tabs                     |
| 主表单包装                    | `container_form_main_wrap`        | 主表单区域包装                              |
| 子表区域包装                  | `container_table_main_wrap`       | 子表 tabs 区域包装                          |
| 树根容器                      | `tree_main_wrap`                  | 树形页面根                                  |
| 抽屉表单                      | `table_drawer`                    | 抽屉弹窗                                    |
| 弹窗选表                      | `dialog_pop_select_table`         | 选择表格弹窗                                |

### 固定页面（无需 idPre）

| 页面     | 关键节点 ID                                                                              |
| -------- | ---------------------------------------------------------------------------------------- |
| 登录页   | `page_meta_login`、`inp_meta_login_name`、`inp_meta_login_pwd`、`button_meta_login_btn`  |
| 头部     | `container_meta_header_top`、`dropdown_meta_header_user`、`button_meta_header_user_exit` |
| 侧边栏   | `menu_meta_siderbar`                                                                     |
| 内容区   | `container_meta_main_content_wrap`、`tabs_meta_main_content_labels`                      |
| 修改密码 | `dialog_meta_rest_pwd`、`form_meta_rest_pwd`                                             |
| 角色权限 | `dialog_meta_role_authority`、`tabs_meta_role_authority`                                 |
