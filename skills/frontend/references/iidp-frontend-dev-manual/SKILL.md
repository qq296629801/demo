---
name: iidp-frontend-dev-manual
description: IIDP 前端开发手册完整索引和核心参考。当需要查阅 IIDP 前端框架、组件、扩展机制、数据源配置、属性绑定、事件绑定、公共命令、钩子、在线视图、工程配置等文档时使用。其他 skill 应通过本 skill 获取 IIDP 前端开发知识，而非直接读取 iidpDoc/ 原始文档。
---

# IIDP 前端开发手册

本 skill 是 `./iidpDoc/03.前端开发手册/` 的完整索引和核心知识库。其他 skill（如 [iidp-frontend-extension-dev](../iidp-frontend-extension-dev/SKILL.md)、[iidp-frontend-spec-code](../iidp-frontend-spec-code/SKILL.md)、[iidp-frontend-spec-doc](../iidp-frontend-spec-doc/SKILL.md)、[iidp-frontend-init](../iidp-frontend-init/SKILL.md)）应通过本 skill 获取文档参考，而非直接遍历原始文档目录。

## 定位与依赖

- 定位：IIDP 前端开发文档的完整索引与核心参考（框架机制/组件/扩展/hook/工程等“事实来源”）。
- 上游：无。
- 下游：[iidp-frontend-spec-doc](../iidp-frontend-spec-doc/SKILL.md)、[iidp-frontend-spec-code](../iidp-frontend-spec-code/SKILL.md)、[iidp-frontend-extension-dev](../iidp-frontend-extension-dev/SKILL.md)、[iidp-frontend-init](../iidp-frontend-init/SKILL.md) 在需要文档事实时应优先跳转到此处。
- 何时使用：遇到框架语义不确定、组件属性不确定、扩展协议/hook 规则不确定、工程配置与命令不确定时。

## 文档索引

> 文档根路径：
> - 主手册：`./iidpDoc/03.前端开发手册/`
> - 常见问题：`./iidpDoc/07.平台常见问题/`

### 前言

| 文档                         | 内容                                                                          |
| ---------------------------- | ----------------------------------------------------------------------------- |
| `01.前言/02.学习路线.md`     | 学习路线与入门指引                                                            |
| `01.前言/03.平台开发规范.md` | IIDP 平台开发规范                                                             |
| `01.前言/04.扩展开发规范.md` | 扩展命名规范（主题扩展、自定义组件、扩展名称、样式、全局变量、节点 id、注释） |
| `01.前言/05.开发注意事项.md` | 开发注意事项                                                                  |

### 快速上手

| 文档                         | 内容                                                               |
| ---------------------------- | ------------------------------------------------------------------ |
| `02.快速上手/03.DEMO介绍.md` | 通过复制 `/apps/demo` 目录创建新应用，`npm run build:all` 打包部署 |
| `02.快速上手/04.标准模板.md` | 管理后台标准模板介绍                                               |

### 扩展说明

| 需求场景                                                                             | 查阅文档                                     |
| ------------------------------------------------------------------------------------ | -------------------------------------------- |
| 新建扩展应用（`tech app`、按需加载配置）                                             | `03.扩展说明/01.新建扩展应用.md`             |
| 扩展类型（before/after/merge/unshift/replace/append/delete/custom）                  | `03.扩展说明/02.扩展类型.md`                 |
| 扩展命名与执行顺序（weight/排序规则）                                                | `03.扩展说明/03.扩展名称.md`                 |
| 扩展复用（`selector.pre`/同扩展应用到多个页面）                                      | `03.扩展说明/04.扩展复用.md`                 |
| 扩展钩子总览                                                                         | `03.扩展说明/05.扩展钩子/`                   |
| ├ 扩展协议钩子                                                                       | `03.扩展说明/05.扩展钩子/01.扩展协议钩子.md` |
| ├ 生命周期钩子（gridPage/detailPage/grid/search/form/tree created/mounted/destroy）  | `03.扩展说明/05.扩展钩子/02.生命周期钩子.md` |
| ├ 视图查询钩子（`page.queryView`）                                                   | `03.扩展说明/05.扩展钩子/03.视图查询钩子.md` |
| ├ 主表格钩子（beforeQuery/query/afterQuery/delete/select等）                         | `03.扩展说明/05.扩展钩子/04.主表格钩子.md`   |
| ├ 主表单钩子（beforeQuery/query/afterQuery/init/validate/beforeSave/save/afterSave） | `03.扩展说明/05.扩展钩子/05.主表单钩子.md`   |
| ├ 树钩子（canCreate/canDelete/canEdit/query/save/delete）                            | `03.扩展说明/05.扩展钩子/06.树钩子.md`       |
| └ 子表格钩子（queryView/crud/edit/search/drawerForm/addRel）                         | `03.扩展说明/05.扩展钩子/07.子表格钩子.md`   |
| 动态注册扩展（`tech_app.setExtend`）                                                 | `03.扩展说明/06.扩展动态定义.md`             |
| 复杂扩展实践（完整多行编辑保存案例）                                                 | `03.扩展说明/07.复杂扩展实践过程.md`         |
| 扩展多次开发（arrange 编排协议/多层级扩展）                                          | `03.扩展说明/09.扩展多次开发.md`             |
| 调试扩展链路（`tech_app.extendViewTrace`）                                           | `03.扩展说明/10.打印节点扩展链路.md`         |
| 自定义页面（`type: 'page'` 节点）                                                    | `03.扩展说明/11.自定义页面.md`               |
| 扩展 Demo 介绍                                                                       | `03.扩展说明/12.扩展Demo介绍.md`             |
| 扩展自定义皮肤                                                                       | `03.扩展说明/13.扩展自定义皮肤 .md`          |

### 框架

| 需求场景                                                             | 查阅文档                                          |
| -------------------------------------------------------------------- | ------------------------------------------------- |
| 节点 node（type/id/items/lifecycle/vm实例/updatePageNode）           | `06.框架/01.节点 node.md`                         |
| 数据源（meta/api/static/multi/save/autoRequest/reqPrep/reqAfter）    | `06.框架/02.数据源.md`                            |
| 属性绑定（`bind_`/`bind_two_`/`$ds`/`$self`/`$cmd`/`${}`/transform） | `06.框架/03.属性绑定.md`                          |
| 选择器（`$select`/`$selectAll`/id选择器/层级关系/属性条件）          | `06.框架/04.选择器.md`                            |
| 数据联动（doWhenChange/bind_filter/省市联动/显隐联动）               | `06.框架/05.数据联动.md`                          |
| 事件绑定（`bind_on_事件名`/params参数结构）                          | `06.框架/06.事件绑定.md`                          |
| 公共命令（commands/`$cmd`/内置命令）                                 | `06.框架/07.公共命令.md`                          |
| 视图行为协议 - view协议（click/mouseover/动态加载视图节点）          | `06.框架/08.视图行为协议/01.view协议.md`          |
| 视图行为协议 - openView协议（showType/后端视图加载/弹窗选择器）      | `06.框架/08.视图行为协议/02.openView协议.md`      |
| 视图行为协议 - 弹窗选择器（customSelect/回填/树选择器）              | `06.框架/08.视图行为协议/03.弹窗选择器.md`        |
| 函数字符串转换协议（`fn_str_xxx`/`useFnStr`）                        | `06.框架/09.函数字符串转换协议.md`                |
| 自定义指令协议（`def_directives`/analyse/activeFn）                  | `06.框架/010.自定义指令协议.md`                   |
| 配置iframe性能模式（TABIFRAME/独立渲染进程）                         | `06.框架/011.配置iframe性能模式.md`               |
| 权限系统 - 系统权限（功能权限/数据权限）                             | `06.框架/012.权限系统/01.系统权限.md`             |
| 权限系统 - 自定义权限                                                | `06.框架/012.权限系统/02.自定义权限.md`           |
| 系统级变量                                                           | `06.框架/013.系统级变量/01.系统级变量.md`         |
| 控制系统级变量范围（`$url_`/`$app_`/`$url_global_`）                 | `06.框架/013.系统级变量/02.控制系统级变量范围.md` |
| 路由说明                                                             | `06.框架/014.路由说明.md`                         |
| 样式说明                                                             | `06.框架/015.样式说明.md`                         |

### 组件

| 类别                   | 文档                                                |
| ---------------------- | --------------------------------------------------- |
| Row 行布局             | `07.组件/01.基础组件/01.Row 行布局.md`              |
| Container 容器         | `07.组件/01.基础组件/02.Container 容器.md`          |
| Card 卡片列表          | `07.组件/01.基础组件/04.card 卡片列表.md`           |
| Button 按钮            | `07.组件/01.基础组件/07.Button 按钮.md`             |
| Input 输入框           | `07.组件/01.基础组件/08.Input 输入框.md`            |
| Textarea 文本域        | `07.组件/01.基础组件/09.Textarea 文本域.md`         |
| Cascader 级联选择器    | `07.组件/01.基础组件/011.Cascader 级联选择器.md`    |
| Switch 开关            | `07.组件/01.基础组件/012.Switch 开关.md`            |
| Radio 单选框           | `07.组件/01.基础组件/013.Radio 单选框.md`           |
| Checkbox 多选框        | `07.组件/01.基础组件/014.Checkbox 多选框.md`        |
| Lookup 选择器          | `07.组件/01.基础组件/016.Lookup 选择器.md`          |
| Upload 上传            | `07.组件/01.基础组件/017.Upload 上传.md`            |
| BreadCrumb 面包屑      | `07.组件/01.基础组件/018.BreadCrumb 面包屑.md`      |
| Carousels 走马灯       | `07.组件/01.基础组件/019.Carousels 走马灯.md`       |
| Collapse 折叠面板      | `07.组件/01.基础组件/020.Collapse 折叠面板.md`      |
| Tabs 标签页            | `07.组件/01.基础组件/021.Tabs 标签页.md`            |
| Search 搜索            | `07.组件/01.基础组件/023.Search 搜索.md`            |
| Paging 分页            | `07.组件/01.基础组件/024.Paging 分页.md`            |
| Dialog 弹窗            | `07.组件/01.基础组件/025.Dialog 弹窗.md`            |
| Drawer 抽屉弹窗        | `07.组件/01.基础组件/026.Drawer 抽屉弹窗.md`        |
| Tooltip 提示语         | `07.组件/01.基础组件/027.Tooltip 提示语.md`         |
| InputNumber 数字输入框 | `07.组件/01.基础组件/028.InputNumber 数字输入框.md` |
| Steps 步骤条           | `07.组件/01.基础组件/030.Steps 步骤条.md`           |
| Transfer 穿梭框        | `07.组件/01.基础组件/031.Transfer穿梭框.md`         |
| Progress 进度条        | `07.组件/01.基础组件/032.progress进度条.md`         |
| Badge 标记             | `07.组件/01.基础组件/033.Badge标记.md`              |
| Timeline 时间线        | `07.组件/01.基础组件/034.Timeline时间线.md`         |
| Link 链接              | `07.组件/01.基础组件/035.Link链接.md`               |
| ConditionalTree 条件树 | `07.组件/01.基础组件/036.ConditionalTree条件树.md`  |
| Alert 提示             | `07.组件/01.基础组件/037.Alert提示.md`              |
| InputRichText 富文本   | `07.组件/01.基础组件/038.InputRichText富文本.md`    |
| Calendar 日历          | `07.组件/01.基础组件/039.Calendar日历.md`           |
| Image 图片             | `07.组件/01.基础组件/040.image 图片组件.md`         |
| TimePicker 时间选择器  | `07.组件/01.基础组件/041.TimePicker 时间选择器.md`  |
| 图标库                 | `07.组件/06.图标库.md`                              |
| 文件预览               | `07.组件/07.文件预览.md`                            |

### 在线视图

| 文档                                   | 内容                 |
| -------------------------------------- | -------------------- |
| `08.在线视图/02.搜索视图.md`           | 后端搜索视图配置     |
| `08.在线视图/05.菜单配置.md`           | 菜单配置             |
| `08.在线视图/06.后端视图放前端工程.md` | 后端视图放到前端工程 |

### 多语言

| 文档                               | 内容             |
| ---------------------------------- | ---------------- |
| `04.多语言/01.配置及使用.md`       | 多语言配置与使用 |
| `04.多语言/02.自动扫描工程中文.md` | 自动扫描工程中文 |

### 工程说明

| 文档                                 | 内容             |
| ------------------------------------ | ---------------- |
| `05.工程说明/03.工程运行.md`         | 工程运行命令     |
| `05.工程说明/07.静态资源.md`         | 静态资源管理     |
| `05.工程说明/08.自定义工程化配置.md` | 自定义工程化配置 |

### 业务场景

| 文档                                       | 内容                 |
| ------------------------------------------ | -------------------- |
| `010.业务场景/01.业务场景.md`              | 业务场景总览         |
| `010.业务场景/02.第三方登录.md`            | 第三方登录集成       |
| `010.业务场景/03.自定义页面.md`            | 自定义页面场景       |
| `010.业务场景/04.按产品线或APP配置路由.md` | 按产品线/APP配置路由 |

### 标准模板

| 文档                                               | 内容              |
| -------------------------------------------------- | ----------------- |
| `011.标准模板/01.管理后台模板/01.树.md`            | 树模板            |
| `011.标准模板/01.管理后台模板/02.主表格.md`        | 主表格模板        |
| `011.标准模板/01.管理后台模板/03.主表单+子表.md`   | 主表单+子表模板   |
| `011.标准模板/01.管理后台模板/04.抽屉表单.md`      | 抽屉表单模板      |
| `011.标准模板/01.管理后台模板/05.侧边栏+主内容.md` | 侧边栏+主内容模板 |

### 插件发版信息

| 文档                                    | 内容           |
| --------------------------------------- | -------------- |
| `012.插件发版信息/01.流水线镜像信息.md` | 流水线镜像信息 |
| `012.插件发版信息/03.流水线配置.md`     | 流水线配置     |

### 平台常见问题

> 文档根路径：`./iidpDoc/07.平台常见问题/02.前端开发常见问题/`

| 需求场景                                                       | 查阅文档                   |
| -------------------------------------------------------------- | -------------------------- |
| 依赖版本更新、字段条件禁用/隐藏/必填、视图配置、功能实现 FAQ   | `01.前端常见问题.md`       |
| 扩展实战案例（工具栏追加按钮、操作列自定义按钮、接口调用等）   | `02.前端常用拓展案例.md`   |
| 前端常见报错排查（空白页、loadView 异常、启动报错等）          | `03.前端常见报错.md`       |

---

### 常用能力

| 文档                              | 内容         |
| --------------------------------- | ------------ |
| `013.常用能力/01.异常提醒.md`     | 异常提醒     |
| `013.常用能力/02.拼接搜索参数.md` | 拼接搜索参数 |
| `013.常用能力/03.版本更新提示.md` | 版本更新提示 |

---

## 核心框架速查

### 节点 (Node)

IIDP 视图由节点树组成，根结构为 `app > page > container > 具体组件`。

**节点通用属性**：

- `type`：节点类型，即组件名（如 `app`、`page`、`container`、`text`、`button`、`table`、`form` 等）。
- `id`：节点唯一标识，业务扩展强烈建议写稳定 id。
- `items`：子节点数组。
- `style`、`css`、`className`：样式配置。注意 `css` 使用的是经过 `jsonCssToObj` 处理的 JSON 对象格式，非普通 CSS 字符串。
- `display`：控制节点是否渲染（`true`/`false`）。
- 生命周期钩子：`created(vm)`、`mounted(vm)`、`beforeDestroy(vm)`、`destroy(vm)`。
- `dataSource`：节点本地缓存数据。
- `ds_config`：数据源配置。

**节点实例 vm 常用能力**：

- `vm.data`：当前节点配置对象。
- `vm.parent`：父节点 vm。`vm.children`：子节点 vm 数组。
- `vm.instance`：Vue 组件实例（`.instance` 前通常需要先确认已完成渲染）。
- `vm.page`：当前页面实例，提供 `vm.page.getNode(id)` 按 id 获取节点、`vm.page.on(path, callback)` 监听页面状态。
- `vm.app`：应用实例，提供 `vm.app.openPage(idOrVm)` 打开页面。
- `vm.app.updatePageNode(node, parentNode, callback)`：动态插入或替换节点。操作完真实 `items` 后应立即用此方法注册更新。
- `vm.refresh()`：刷新当前节点。

**动态节点更新**：直接操作 `items` 后必须调用 `updatePageNode`。如果旧节点已存在，先移除或避免重复 id。

**调试**：`tech_app.printObj(vm)` 可在控制台打印节点完整属性。

### 数据源 (DataSource)

`dataSource` 是节点上的数据缓存，运行时通过 `vm.$ds` 访问。数据源可继承父级，子级可覆盖同名字段；断开继承使用 `inheritData: false`。

#### 三种数据源类型

**1. 元模型数据源（type: 'meta'）**：通过技术平台后端接口获取数据。

```js
{
  ds_config: {
    type: 'meta',
    name: 'data1',
    autoRequest: true,
    options: {
      params: {
        service: 'search',
        model: 'xxx_model',
        args: { properties: ['id', 'name', 'status'] }
      }
    }
  }
}
```

**2. 第三方 API 数据源（type: 'api'）**：调用外部或自定义接口。

```js
{
  ds_config: {
    type: 'api',
    name: 'data2',
    options: {
      axios: { method: 'post', url: '/user/12345', data: {} }
    }
  }
}
```

**3. 静态数据源**：直接写 `dataSource`，适合本地状态和默认值。

```js
{
  dataSource: { data3: { a: 1, b: 1 } }
}
```

#### 多数据源

```js
{
  ds_config: {
    list: [
      {
        type: "meta",
        name: "ds1",
        options: { params: { service: "search", model: "m1" } },
      },
      {
        type: "meta",
        name: "ds2",
        options: { params: { service: "search", model: "m2" } },
      },
    ];
  }
}
```

#### 保存逻辑

```js
{
  ds_config: {
    type: 'meta',
    method: 'save',
    name: 'saveData',
    options: { params: { service: 'save', model: 'xxx' } }
  }
}
```

保存类型的结果不会缓存到 `dataSource`。

#### 关键配置字段

- `ds_config.name`：数据缓存名，使用表达业务含义的稳定名称。
- `autoRequest: true`：节点渲染后自动请求。需要事件触发或按条件查询时设为 `false`。
- 手动请求：`vm.request('name', params?)`，传入参数会与 `options.params` 深度合并。
- `reqPrep(originParams)`：请求前处理参数，必须返回最终参数。
- `reqAfter(res)`：响应后处理，必须返回处理后的数据。
- `isSuccess(res)`：自定义成功判断，返回 boolean。
- `success(res)`：成功回调。
- `error(err)`：失败回调。

### 属性绑定

属性绑定将数据源、节点属性或命令结果同步到节点属性。

**常用写法**：

- `bind_属性名`：单向绑定，数据源变化 → 组件属性更新。
- `bind_two_属性名`：双向绑定，数据源 ↔ 组件属性互相更新。
- `$ds.名称`：当前节点可见的数据源字段。
- `$self`：当前节点自身属性。
- `$cmd`：当前节点可见的命令。
- `${}`：表达式插值语法，支持运算、逻辑判断和函数返回。

**基础示例**：

```js
// 单向绑定
{
  bind_value: "$ds.form.name";
}

// 双向绑定
{
  bind_two_value: "$ds.form.name";
}

// 插值运算
{
  bind_value: "${$ds.data.a + $ds.data.b}";
}

// 表达式
{
  bind_value: '${$ds.form.b === 1 ? "是" : "否"}';
}

// 显示隐藏联动
{
  bind_display: "${$ds.form.b === 1}";
}
```

**完整写法**（绑定其他节点的属性）：

```js
{
  bind_value: {
    path: 'dataSource.xxx',    // 被监听的属性路径，跨节点时不用 $ds.xxx
    selector: 'target_node_id', // 目标节点选择器
    transform(params) {         // 转换函数，返回值写入绑定属性
      // params.self: 当前节点
      // params.related: 关联节点
      // params.value: 路径值
      return params.value + '后缀';
    }
  }
}
```

### 选择器 (Selector)

JS 中使用：

- `vm.$select(selector)`：返回单个节点 vm 或 `null`。
- `vm.$selectAll(selector)`：返回节点 vm 数组。

**选择器类型**：

- **字符串**：按 id 查找，支持相对关系 `parent`、`children`、`brother`、`prev`、`next`。
- **对象**：按 `attr` + `value` 匹配，支持 `attr: 'type'`、`attr: 'className'`、`attr: 'id'` 等。

**id 选择器规则**：

- 第一个 id 必须是当前菜单完整 id，保证性能。
- 空格表示跨层级查找。
- `>` 表示直接子级，前后不加空格。
- 支持 `:first-child`、`:last-child`、`:nth-child(n)`。
- 支持属性条件：`[type=container]`、`[className^=xxx]`、`[className*=xxx]`。

不要长期依赖运行时生成的 `_items_` id。优先用稳定前缀 + 层级后缀 + 位置或特征属性组合选择。

### 数据联动

通过 `doWhenChange` 和 `bind_filter` 实现组件间的联动行为。

**省市联动**（选择省份后城市自动重新加载）：

```js
// 省份下拉 - 自动请求
{ type: 'select', ds_config: { name: 'province', autoRequest: true, ... } }

// 城市下拉 - 监听省份变化后重新加载
{
  type: 'select',
  ds_config: { name: 'city', autoRequest: false, ... },
  doWhenChange: 'reload',
  bind_filter: '${[["province_id", "=", $ds.form.province]]}'
}
```

**显隐联动**：

```js
{ type: 'input', bind_display: '${$ds.form.type === 1}' }
```

### 事件绑定

语法格式为 `bind_on_事件名`。事件名必须是 Vue 组件内部实际存在且会 `$emit` 的方法。

**事件回调参数**：

- `params.value`：组件内部事件方法的返回值。
- `params.self`：当前节点 vm，可继续访问 `$ds`、`$cmd`、`$select` 等。

```js
{
  type: 'button',
  bind_on_click: (params) => {
    const { value, self: vm } = params;
    vm.$ds.someData = value;
  }
}
```

复杂逻辑不要堆在绑定表达式里，优先抽成 `commands`。

### 公共命令 (Commands)

`commands` 是节点上的公共方法集合，可继承、可覆盖，通过 `vm.$cmd` 调用。

**声明与使用**：

```js
{
  type: 'container',
  commands: {
    submit(params) { /* 业务逻辑 */ }
  },
  items: [
    {
      type: 'button',
      value: '提交',
      bind_on_click: (params) => {
        params.self.$cmd.submit(params);
      }
    }
  ]
}
```

**规则**：

- 父节点声明的命令对子节点可见。
- 子节点同名命令会覆盖父级命令。
- 多个节点复用的业务逻辑优先抽成 commands。

**常用内置命令**：

- `$cmd.meta.addIdPre(view, idPre)` - 给视图添加 id 前缀。
- `$cmd.meta.getView(...)` - 获取动态页面/表格/表单/弹窗/树等模板视图。
- `$cmd.meta.popupView(vm, view, idPre)` - 设置底部弹窗。
- `$cmd.meta.addSearch(srcArr, newArr, tag)` - 追加搜索条件。
- `$cmd.meta.formFormat.*` - 转换后端表单视图配置。
- `$cmd.meta.tableFormat.*` - 转换后端表格视图配置。
- `$cmd.meta.checkErUnique(...)` - 处理子表关系和指令集缓存。
- `$cmd.meta.dealTableErCatch(...)` - 处理子表异常捕获。

### 视图行为协议

#### view 协议（动态加载视图）

```js
{
  view: {
    click: {
      type: 'dialog',
      title: '弹窗标题',
      width: '80%',
      __parentId: 'target_container',
      items: [ /* 视图节点 */ ]
    }
  }
}
```

参数说明：

- `__parentId`：挂载到指定父节点下。
- `_noCache: true`：不使用缓存，每次重建。
- `_reInit: true`：初始化数据。
- `inheritApp: true`：继承 app 上下文。
- `inheritData: true`：继承数据源。
- `beforeOperate(app, operate, options)`：挂载前钩子。
- `mounted(vm)`：挂载后回调。

#### openView 协议（加载后端视图）

```js
{
  view: {
    click: {
      openView: {
        showType: 'dialog',      // dialog/drawer/dropdown/container
        preId: 'pref_',
        model: 'example_model',
        type: 'form,grid,search'  // 后端视图类型
      }
    }
  }
}
```

#### 弹窗选择器

表单字段通过 `useCustomClick: true` 和 `view.customClick` 配置弹窗选择数据并回填：

```js
{
  name: 'material_name',
  useCustomClick: true,
  view: {
    customClick: {
      customSelect: {
        showType: 'dialog',
        useOpenView: {
          preId: 'pre_',
          model: 'material',
          type: 'grid,search',
          checkbox: 'single',     // single/multiple
          valueField: 'matCode',  // 回填 value 的字段
          labelField: 'matName'   // 回填 label 的字段
        }
      }
    }
  }
}
```

### 自定义指令 (def_directives)

在节点上定义指令，子节点可继承或覆盖：

```js
{
  def_directives: {
    t_confirm_dialog: {
      analyse(vm, config) { /* 处理逻辑 */ },
      activeFn(vm) { return true; },  // 是否激活
      beforeBind: true,               // 在绑定逻辑前执行
      statement: '_print'
    }
  }
}
```

指令通过匹配的属性名调用：

```js
{
  t_confirm_dialog: {
    title: "确认删除？";
  }
}
```

### 权限系统

**功能权限**：`loadView` 接口返回的权限控制视图节点是否生成实例。grid 级别控制整个表格；tbar 按钮级别控制单个按钮。

**数据权限**：分为列权限（控制表格列的数据展示）和行权限（控制表格行展示）。权限与用户、角色绑定。

---

## 扩展开发核心

### 扩展定义结构

```js
export default {
  my_extend_view: {
    type: "merge", // 扩展类型
    selector: {
      // 目标节点选择器
      attr: "id",
      value: "target_node_id",
    },
    beforeOperate(app, operate, options) {
      // operate.view: 当前扩展配置的新视图
      // options.element: selector 选中的当前视图
      // options.originElement: 选中视图的初始状态
      // options.parent: 选中节点的父级
      // options.mountedNode: 当前扩展展开的挂载上下文
      return operate.view; // 返回最终参与扩展处理的视图
    },
    view: {
      /* 新视图 */
    },
  },
};
```

### 扩展类型选择

| 类型      | 行为                                       |
| --------- | ------------------------------------------ |
| `before`  | 在目标节点**前**插入同级节点               |
| `after`   | 在目标节点**后**插入同级节点               |
| `append`  | 在目标节点 `items` **尾部**追加子节点      |
| `unshift` | 在目标节点 `items` **头部**插入子节点      |
| `merge`   | 深度合并目标节点属性（不合并 `items`）     |
| `replace` | 用新 `view` 完全替换目标节点               |
| `delete`  | 删除目标节点                               |
| `custom`  | 不直接修改目标节点，仅执行 `beforeOperate` |

### 扩展复用

通过 `selector.pre` 将扩展复用到多个节点前缀：

```js
// 字符串前缀
selector: { attr: 'id', pre: 'prex_', value: '_sidebar_button' }

// 数组前缀
selector: { attr: 'id', pre: ['prex1_', 'prex2_'], value: '_sidebar_button' }

// 函数动态返回
selector: {
  attr: 'id',
  pre: (extendName, config) => ['prex1_', 'prex2_'],
  value: '_sidebar_button'
}
```

逻辑复用在 `beforeOperate` 中调用公共方法，通过 `customOptions` 差异化定制。

### 扩展多次开发（arrange 编排）

支持多层级扩展，按扩展名数字/字母顺序执行。通过 `arrange` 配置字段合并策略：

```js
xxx_extend_view_dev2: {
  type: 'merge',
  arrange: {
    'css': 'concat',                    // 字符串拼接
    'ttt.testFn': 'concat',             // 函数串行执行
    'custom.path': (app, ops) => {      // 自定义合并
      // ops.lastView / ops.currentView / ops.path
    }
  },
  view: { css: '.test1{color: red;}', ttt: { testFn } }
}
```

### 动态扩展注册

```js
tech_app.setExtend("my_custom_extend_view_uuid", {
  type: "merge",
  selector: { attr: "id", value: "target_id" },
  view: { newAttr: "newAttr" },
});
```

### 调试扩展

```js
// 查看节点扩展链路
tech_app.extendViewTrace("nodeId"); // nodeId 支持模糊匹配

// 查看重复扩展名
window.techRepeatExtendName;
```

---

## Hook 系统速查

Hook 扩展通常挂在页面顶级节点上（`页面菜单 url + _container_main`，如 `rbac_user_app_menu_container_main`）。

Hook 是异步方法，常见签名：`async (vm, params, options) => {}`。

**所有 hook 方法必须 return 数据**，不 return 会导致页面数据丢失、空白或逻辑中断。**业务逻辑必须在 return 之前执行完毕**：

- `before*` 类钩子：`return params`（处理后的参数）。
- `query`/`save`/`delete` 等执行类钩子：`return await vm.super['hook.path'](vm, params, options)` 或 `return res`（接口返回数据或其他业务需要的新数据）。
- `after*` 类钩子：返回原方法的数据（`return await vm.super['hook.path'](vm, params, options)`），或接口数据，或其他业务需要的新数据。
- `can*` 钩子：`return true` 或 `return false`。
- `select`/`cancelSelect`：`return params`。
- `init`：`return params`。

保留平台原逻辑：`vm.super['hook.path'](vm, params, options)`，**必须 return 其返回值**。

- `can*` 钩子返回 `false` 表示按钮置灰或不可操作。
- 非 `can*` 钩子返回 `false` 表示中断后续逻辑。
- `vm.biz` 是标准页业务上下文，按 `data`、`req`、`baseMethods`、`methods`、`nodes` 组织。

### 标准页生命周期钩子

```js
hook: {
  gridPage: {   // 列表页
    created: async (vm) => { await vm.super['gridPage.created'](vm); },
    mounted: async (vm) => { await vm.super['gridPage.mounted'](vm); },
    destroy: async (vm) => { await vm.super['gridPage.destroy'](vm); }
  },
  detailPage: { // 详情页（同理）
    created: async (vm) => {},
    mounted: async (vm) => {},
    destroy: async (vm) => {}
  }
}
```

### 视图查询钩子

```js
hook: {
  page: {
    queryView: async (vm, params, options) => {
      // params: loadView 接口参数
      return await vm.super["page.queryView"](vm, params, options);
    };
  }
}
```

### 主表格 (grid) 钩子

- 查询：`grid.beforeQuery` / `grid.query` / `grid.afterQuery` / `grid.queryCount`
- 删除：`grid.canDelete` / `grid.onConfirm` / `grid.beforeDelete` / `grid.delete` / `grid.afterDelete`
- 选择：`grid.select` / `grid.cancelSelect`
- 搜索栏：`search.canQuery` / `search.validateQuery`

### 主表单 (form) 钩子

- 查询：`form.beforeQuery` / `form.query` / `form.afterQuery`
- 初始化：`form.init`（`options.isEdit`/`options.isCreate` 区分模式）
- 校验：`form.validate`
- 保存：`form.beforeSave` / `form.save` / `form.afterSave`（`options.triggerType` 区分新增/编辑）

### 树 (tree) 钩子

- `tree.canCreate` / `tree.canDelete` / `tree.canEdit`
- `tree.beforeQuery` / `tree.query` / `tree.afterQuery`
- `tree.select` / `tree.cancelSelect`
- `tree.save` / `tree.delete`

### 子表格钩子

根键选择：上下表格用 `grid.tabs.<field>`，主表单子表用 `form.tabs.<field>`。

```js
form: {
  tabs: {
    child_field_name: {
      grid: {
        beforeQuery: async (vm, params, options) => {},
        query: async (vm, params, options) => {},
        afterQuery: async (vm, params, options) => {},
        canDelete: async (vm, params, options) => {},
        delete: async (vm, params, options) => {},
        edit: {
          canCreate: async (vm, params, options) => {},
          beforeSave: async (vm, params, options) => {},
          save: async (vm, params, options) => {},
          afterSave: async (vm, params, options) => {}
        },
        search: {
          canQuery: async (vm, params, options) => {},
          validateQuery: async (vm, params, options) => {}
        },
        drawerForm: {
          beforeQuery: async (vm, params, options) => {},
          query: async (vm, params, options) => {}
        },
        addRel: {
          query: async (vm, params, options) => {}
        }
      }
    }
  }
}
```

---

## 扩展开发规范要点

1. **主题扩展**（公共模块如顶部、侧边栏、菜单）：需独立新增 app 内扩展，可单独安装卸载。
2. **自定义组件命名**：`应用名称-功能名称`，如 `tech-iot-echarts`。
3. **扩展名称**：`应用名称_菜单名_功能名称_extend`。
4. **样式扩展**：修改 Element 样式时使用局部作用域，避免全局污染。
5. **全局变量/方法**：使用 `应用名称 + 功能名称` 前缀。
6. **节点 id**：选择扩展节点时，包含 `undefined` 的节点可能是平台异常。
7. **注释**：扩展时补充注释，说明页面节点、功能描述等。

---

## 工程结构

标准工程目录如下（根据 `05.工程说明/` 及各文档）：

```
project/
├── apps/
│   ├── base/                  # 公共工程逻辑（common/views/config/resource）
│   ├── <appName>/             # 业务应用
│   │   ├── common/            # 公共扩展（assetImport.js/asset.json/common.js/comps.js/extendView.js/hook.js/schema.js/index.js）
│   │   ├── views/             # 业务视图扩展
│   │   ├── config/app.json    # 应用配置（按需加载/全局变量）
│   │   └── resource/          # 语言包、静态资源等
│   └── component/             # 公共业务组件
├── config/
│   └── apps.json              # 工程级配置（全局变量/按需加载覆盖）
├── build/                     # 构建配置
└── dist/                      # 编译产物（不要手动修改）
```

不要将业务代码写入 `dist`、`distApp`、`distTmp`、`node_modules` 等临时或编译目录。

---

## 样式约定

- 通用样式使用 `style`（内联样式对象）、`className`（CSS class）、`css`（JSON CSS 对象，经 `jsonCssToObj` 处理）。
- `css` 字段会处理为全局样式（`<style>` 标签），可能造成全局污染；优先使用局部的 `style` 或名空间化的 `className`。
- 使用 Element UI 原生属性时放在 `ATTRS` 中。
- Element UI 原生事件放在 `ONS` 中。

---

## 使用指引

1. **需要框架机制详解**（数据源/绑定/选择器/联动/事件/命令/权限）→ 查阅上方的"核心框架速查"和相关文档文件。
2. **需要写扩展代码** → 查阅"扩展开发核心"、"Hook 系统速查"和 `iidp-frontend-extension-dev` skill。
3. **需要查组件属性** → 查阅上方的"组件"索引，定位到具体的 `.md` 文件。
4. **需要标准模板** → 查阅"标准模板"索引，参考管理后台模板。
5. **需要工程配置** → 查阅"工程说明"和"插件发版信息"。
6. **排查性能/内存问题** → 查阅"性能优化"和"效率与调试"。
7. **遇到常见问题/报错/需要扩展案例** → 查阅"平台常见问题"索引。
8. **遇到不确定的文档是否存在** → 按上方索引查找，索引未覆盖的再搜索 `iidpDoc/` 目录。
