# IIDP 前端开发快查

本文保留常用框架事实的轻量速查。需要更完整的实现规则时，优先读取：

- 扩展开发：[../iidp-frontend-extension-dev/SKILL.md](../iidp-frontend-extension-dev/SKILL.md)
- 代码生成门禁：[../iidp-frontend-codegen-protocol.md](../iidp-frontend-codegen-protocol.md)
- 组件规则：[../iidp-frontend-extension-dev/COMPONENT_RULES.md](../iidp-frontend-extension-dev/COMPONENT_RULES.md)

## 节点

IIDP 视图由节点树组成，常见结构为 `app > page > container > 具体组件`。

常用属性：

- `type`：节点类型或组件名。
- `id`：节点唯一标识，业务扩展应使用稳定 id。
- `items`：子节点数组。
- `style`、`css`、`className`：样式配置。
- `dataSource` / `ds_config`：数据源配置。
- `commands`：节点公共命令集合。
- `hook`：扩展钩子集合。

运行时常用能力：

- `vm.page.getNode(id)`：按 id 获取节点。
- `vm.$select(selector)`：获取单个节点。
- `vm.$selectAll(selector)`：获取节点数组。

## 数据源

常见类型：

- `meta`：后端元数据/模型接口。
- `api`：自定义接口。
- `static`：静态数据。
- `multi`：组合数据源。
- `save`：保存类数据源。

常见钩子：

- `reqPrep(params)`：请求前处理，必须返回最终参数。
- `reqAfter(res)`：响应后处理，必须返回处理后的数据。
- `isSuccess(res)`：自定义成功判断。
- `success(res)` / `error(err)`：成功或失败回调。

API 请求优先使用 IIDP 数据源或 `window.Tech.httpMeta`，不要自行引入 axios、fetch。

## 属性绑定

常见写法：

- `bind_<属性名>`：单向绑定。
- `bind_two_<属性名>`：双向绑定。
- `$ds.xxx`：当前节点可见的数据源字段。
- `$self`：当前节点自身属性。
- `$cmd`：当前节点可见命令。
- `${}`：表达式插值。

复杂逻辑不要堆在绑定表达式里，优先抽成 `commands` 或扩展钩子。

## 选择器

JS 中常用：

- `vm.$select(selector)`：返回单个节点 vm 或 `null`。
- `vm.$selectAll(selector)`：返回节点 vm 数组。

选择器优先使用稳定 id。不要长期依赖运行时生成的 `_items_` id。目标节点 id 的来源必须来自用户、标准模板规则库、现有确认事实或浏览器验证。

## 事件与命令

事件绑定格式为 `bind_on_<事件名>`。事件名必须是 IIDP 组件实际支持并会 `$emit` 的事件。

`commands` 是节点上的公共方法集合，可通过 `vm.$cmd` 调用。多节点复用的业务逻辑优先抽成 `commands`。

## 视图行为协议

`view` 协议用于动态加载视图节点，常见挂载参数：

- `__parentId`：挂载到指定父节点。
- `_noCache`：不使用缓存。
- `_reInit`：重新初始化数据。
- `inheritApp` / `inheritData`：继承 app 或数据上下文。

`openView` 协议用于加载后端视图，常见参数：

- `showType`：`dialog`、`drawer`、`dropdown`、`container`。
- `preId`：节点 id 前缀。
- `model`：后端模型。
- `type`：后端视图类型。

## 扩展视图

普通节点扩展定义通常包含：

- 全局唯一扩展名。
- `selector`。
- `type`：`before`、`after`、`append`、`unshift`、`merge`、`replace`、`delete`、`custom`。
- `view` 或 `beforeOperate`。

页面整体替换优先使用 `replace` 替换标准页内部目标节点。不要用 `type: 'page'` 替换现有标准页。

## 扩展钩子

扩展钩子通常挂在页面顶级节点上，例如 `页面菜单 url + _container_main`。

正确结构：

```js
export default {
  example_hook: {
    selector: { attr: "id", value: "example_container_main" },
    type: "merge",
    hook: {
      grid: {
        async afterQuery(vm, params, options) {
          return await vm.super["grid.afterQuery"](vm, params, options);
        },
      },
    },
  },
};
```

返回值规则：

- `before*` 返回处理后的 `params`。
- `query`、`save`、`delete` 等执行类钩子返回接口结果或 `vm.super` 返回值。
- `after*` 返回处理后的结果数据。
- `can*` 返回 boolean。
- 调用 `vm.super[...]` 时必须 return 其返回值。

刷新主表格数据优先使用 `vm.biz.grid.methods.runRefresh()`。

## 工程结构

常见源码目录：

```text
apps/<appName>/views       业务视图扩展
apps/<appName>/common      公共扩展和组件注册
apps/<appName>/config      应用配置
apps/<appName>/resource    语言包、静态资源
apps/component             公共业务组件
```

不要修改 `node_modules`、`dist`、`distApp`、`distTmp`、`umdComps`、`build` 或编译产物。

## 样式

- 通用样式使用 `style`、`className`、`css`。
- `css` 会进入全局样式，优先使用局部 `style` 或命名空间化 `className`。
- Element UI 原生属性放入 `ATTRS`。
- Element UI 原生事件放入 `ONS`。
