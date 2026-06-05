# IIDP Component Rules

本文件只记录 IIDP 组件的最小可用协议和易错点。生成或修改组件节点前，先读取 [COMPONENT_RULES_COVERAGE.md](COMPONENT_RULES_COVERAGE.md) 确认目标组件是否已覆盖，再按以下优先级查找组件规则：

1. **本文件已覆盖的组件** → 直接按这里写，不要套用 Element UI 或通用前端属性。
2. **本文件未覆盖的组件** → 先到 IIDP 开发文档（通过 [iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)）中查找对应组件的属性和用法。
3. **文档中也找不到** → 询问用户补充组件文档或确认属性，不要猜。

## 通用规则

- 每个组件节点必须有 `type`。
- 扩展新增的稳定业务节点建议配置 `id`。
- 子节点统一放在 `items`。
- 通用样式使用 `style`、`className`、`css`。
- 显示隐藏优先使用节点通用属性 `display` 或 `bind_display`。
- 平台组件文档没有声明但 Element UI 支持的属性，放到 `ATTRS`。
- Element UI 原生事件放到 `ONS`。
- IIDP 平台事件优先使用 `bind_on_事件名`，不要臆造事件名。

## button

用途：按钮。

最小配置：

```js
{
  type: 'button',
  id: 'xxx_button',
  value: '按钮文本',
  options: {
    type: 'primary',
    size: 'small'
  },
  bind_on_click: (params) => {
    const { self: vm } = params;
  }
}
```

关键规则：

- 按钮文本使用 `value`，不要使用 `text`。
- 样式和按钮状态放在 `options`。
- 常用 `options`：`type`、`size`、`icon`、`disabled`、`plain`。
- 点击事件使用 `bind_on_click`。
- 需要调用数据源时，在按钮上配置 `ds_config`，事件中使用 `params.self.request('dsName', params)`。

## container

用途：空容器，用于布局和承载子组件。

最小配置：

```js
{
  type: 'container',
  id: 'xxx_container',
  style: {},
  items: []
}
```

关键规则：

- 子组件放在 `items`。
- 只需要包裹一组节点或控制布局时使用 `container`。
- 样式放 `style`、`className`、`css`。

## row

用途：24 栅格分栏布局。

最小配置：

```js
{
  type: 'row',
  gutter: 20,
  items: [
    {
      type: 'text',
      span: 12,
      value: '内容'
    }
  ]
}
```

关键规则：

- 行布局组件 `type` 是 `row`。
- 子节点可用 `span` 控制列宽。
- `selfAdaptionW` 用于自适应列宽，嵌套表单时更常见。

## table

用途：表格，基于 Vxe-grid。

最小配置：

```js
{
  type: 'table',
  id: 'xxx_table',
  height: '300px',
  bind_tableData: '$ds.tableData',
  items: [
    { type: 'seq', width: 60, title: '序号' },
    { type: 'text', field: 'name', title: '名称' }
  ]
}
```

关键规则：

- 表格节点 `type` 是 `table`。
- 表格数据使用 `tableData` 或 `bind_tableData`。
- 列配置放在 `items`，也兼容 `columns`。
- 常用列属性：`type`、`field`、`title`、`width`、`sortable`、`fixed`。
- 序号列使用 `type: 'seq'`。
- 操作列使用 `type: 'operation'`，按钮放在该列 `items` 中。
- 复杂 Vxe-grid 配置放在 `gridOptions`。
- 行内编辑配置常见于列的 `editRender` 或后端视图的 `editConfigs`。
- 表格按钮点击常见事件是 `bind_on_clickOptionBtn`，不要和普通按钮的 `bind_on_click` 混用。

## form

用途：表单，收集、校验、提交数据。

最小配置：

```js
{
  type: 'form',
  id: 'xxx_form',
  formConfig: {
    labelPosition: 'right',
    labelWidth: '90px'
  },
  dataSource: {
    form: {
      name: ''
    }
  },
  items: [
    {
      type: 'input',
      name: 'name',
      text: '名称'
    }
  ]
}
```

关键规则：

- 表单配置放 `formConfig`。
- 表单值放在 `dataSource.form`。
- 表单项用 `name` 对应 `dataSource.form` 的字段。
- 表单内输入项通常不使用 `modelValue`；直接通过 `vm.$ds.form[field]` 读写。
- 嵌套复杂布局导致表单赋值或校验异常时，可使用 `form-container` 或 `row` 包裹表单项。
- 字段校验可配置在表单项 `rules` 或表单 `rules` 中。

## input

用途：输入框。

最小配置：

```js
{
  type: 'input',
  id: 'xxx_input',
  name: 'name',
  text: '名称',
  placeholder: '请输入'
}
```

关键规则：

- 表单内通过 `name` 绑定 `dataSource.form[name]`。
- 表单外单独使用时，可配置 `model: { [name]: value }`。
- 常用属性：`placeholder`、`inputType`、`readonly`、`showPassword`、`prefixIcon`、`suffixIcon`、`append`。
- 常用事件：`bind_on_changeHandler`、`bind_on_keyupEnter`、`bind_on_inputHandler`、`bind_on_handleInputFocus`、`bind_on_handleInputBlur`。

## dialog

用途：弹窗对话框。

最小配置：

```js
{
  type: 'dialog',
  id: 'xxx_dialog',
  title: '提示',
  width: '45%',
  display: true,
  showCancel: true,
  showConfirm: true,
  bind_on_operates: (params) => {
    const { value } = params;
    value.close();
  },
  items: []
}
```

关键规则：

- 弹窗显示控制使用 `display`。
- 标题使用 `title`，宽度使用 `width`。
- 内容组件放 `items`。
- 底部按钮可用 `showCancel`、`showConfirm`、`customButtons`、`customMiddleButtons`。
- 底部操作事件使用 `bind_on_operates`。
- `bind_on_operates` 中 `params.value.close()` 可关闭弹窗。

## drawer

用途：抽屉弹窗。

最小配置：

```js
{
  type: 'drawer',
  id: 'xxx_drawer',
  title: '抽屉标题',
  width: '45%',
  visible: true,
  beforeClose: (done) => {
    done();
  },
  items: []
}
```

关键规则：

- 抽屉显示控制使用 `visible`。
- 标题使用 `title`，宽度使用 `width`。
- 内容组件放 `items`。
- 关闭前逻辑使用 `beforeClose(done)`。
- 需要隐藏标题时使用 `hasTitle: false`。

## lookup-table

用途：下拉多列选择器。

最小配置：

```js
{
  type: 'lookup-table',
  name: 'app_id',
  text: '应用',
  searchModel: 'meta_app',
  matchColumns: ['name', 'display_name'],
  searchKey: 'name',
  labelField: 'display_name',
  selectedValue: 'id'
}
```

关键规则：

- 必填关键信息：`searchModel`、`matchColumns`、`searchKey`。
- `labelField` 控制选中后显示字段；未配置时默认取 `searchKey`。
- `selectedValue` 控制实际值字段，默认 `id`。
- 多选使用 `multiple: true`。
- 请求前处理使用 `searchReqBefore(params, searchVal)`，必须返回 params。
- 在表格行内编辑中使用时，放在列的 `editConfigs` 中，并配置 `editType: 'lookup-table'`。

## custom-vue-component

用途：自定义 Vue 组件，用于封装真实 Vue/Element UI 组件或复杂交互。

典型文件：

```vue
<template>
  <div>
    <el-switch
      v-model="innerValue"
      active-color="#13ce66"
      inactive-color="#ff4949"
      :active-text="customAttrs"
    >
    </el-switch>
  </div>
</template>

<script>
export default {
  name: "tech-switch-custom",
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    customAttrs: {
      type: String,
      default: "",
    },
  },
  data() {
    return {
      innerValue: this.value,
    };
  },
};
</script>
```

注册入口：

```js
// apps/<appName>/common/comps.js
import customVueComp from "../../component/example/customVueComp.vue";

export default {
  customVueComp,
};
```

视图中使用：

```js
{
  type: 'switch-custom',
  name: 'isEnable',
  text: '是否可用',
  custom: true
}
```

关键规则：

- 只有用户明确要求新建、注册或使用自定义 Vue 组件时，才读取或修改 `apps/<appName>/common/comps.js`。
- 自定义 Vue 组件使用 Vue2 语法，不使用 Vue3 `defineComponent` / Composition API 写法。
- Vue 组件内必须声明稳定 `name`，且组件名强制以 `tech-` 开头。
- 在 `common/comps.js` 中 import 并 export 后，才能在视图配置中通过组件 `type` 使用。
- **视图中的 `type` 使用组件 `name` 去掉 `tech-` 前缀后的值，不是 `comps.js` 中 import/export 的变量名。** 例如组件声明 `name: 'tech-switch-custom'`，视图中 `type` 应为 `'switch-custom'`，而不是 `comps.js` 中的 `customVueComp`。
- `comps.js` 中的 import 变量名仅用于注册，不参与视图 `type` 的命名。
- **组件内调用接口使用全局方法 `window.Tech.httpMeta`**，不要单独引入 axios 或其他请求库。调用格式必须遵循元模型后端传参规范（参考 [API接口参数说明](../../../backend/references/complete/api-params.md)）：
  ```js
  const res = await window.Tech.httpMeta({
    data: {
      params: {
        args: {
          filter: [
            // 前缀波兰表达式：逻辑符在前，条件三元组在后
            // AND（默认）：[["name", "=", "ABC"], ["status", "=", "ENABLE"]]
            // OR：        ["|", ["name", "like", "%admin"], ["email", "like", "%126"]]
            // 混合：      ["&", ["state", "=", "RELEASED"], ["|", ["userId", "=", "1000"], ["userId", "=", false]]]
            // 操作符：= != > >= < <= like ilike "not like" "not ilike" in "not in" child_of parent_of
            // 逻辑符：&（AND，二元，省略时默认） |（OR，二元） !（NOT，单目）
            ["status", "=", "ENABLE"],
          ],
          limit: 31,
          offset: 0,
          properties: ["*"],
          order: "id desc",
          // 其他参数按前后端契约填写：ids、values、valuesList 等
        },
        model: "rbac_user",
        service: "search",
        app: "base",
      },
    },
  });
  // 返回结果在 res.data 中
  ```
  **重要**：`params` 里的每个字段都必须依据前后端契约填写：
  - `app`：后端应用名称，不可随意填写。
  - `model`：元模型名称，由后端定义。
  - `service`：服务方法名（如 `search`/`create`/`update`/`delete`），由后端定义。
  - `args`：请求参数对象。
  - 接口返回的数据在 `res.data` 中。
- 如果只是复用一段 IIDP 视图配置，不需要写 Vue 组件，优先使用 `custom-view-component`。
- **扩展视图 JS 文件（`views/` 目录下的 `.js` 文件）禁止直接 import Vue 组件（`.vue` 文件）。** 组件注册只在 `comps.js` 中完成，扩展视图通过 `type` 引用已注册的组件。Vue 组件之间的父子引用在 `.vue` 文件内部的 `components` 选项中完成。

> **常见错误**：扩展视图中 `type` 写成 `comps.js` 的 export 变量名（如 `TraceForwardPage`）或直接抄规格文档中的大写名称。正确做法是取组件 `name` 去掉 `tech-` 前缀（如 `trace-forward-page`）。三个值的对应关系：
>
> | 来源                   | 示例值                    | 用途                                    |
> | ---------------------- | ------------------------- | --------------------------------------- |
> | 组件 `name` 属性       | `tech-trace-forward-page` | 组件声明、comps.js 注册                 |
> | comps.js export 变量名 | `TraceForwardPage`        | 仅用于 JS 模块注册，**不用于**视图 type |
> | 扩展视图 `type`        | `trace-forward-page`      | 视图配置中引用组件                      |

### 组件目录结构

当需求涉及多个自定义 Vue 组件时，**必须按页面/业务模块建立文件夹层级**，禁止将所有组件文件平铺在 `apps/component` 根目录下。

目录组织规则：

- 同一页面/业务模块的所有 Vue 组件（父组件和子组件）放在同一个以业务命名的文件夹下。
- 文件夹命名采用小写短横线风格，如 `apps/component/order-manage/`。
- `comps.js` 中的 import 路径对应更新到子文件夹。

示例：

```
apps/component/
├── order-manage/           ← 订单管理页面的组件目录
│   ├── OrderManage.vue     ← 父组件（页面主组件）
│   ├── OrderTable.vue      ← 子组件（表格区域）
│   └── OrderForm.vue       ← 子组件（表单弹窗）
├── user-profile/           ← 用户档案页面的组件目录
│   ├── UserProfile.vue
│   └── UserDetail.vue
└── tech-switch.vue         ← 单个简单组件可直接放在根目录
```

注册入口对应更新：

```js
// apps/<appName>/common/comps.js
import OrderManage from "../../component/order-manage/OrderManage.vue";
import OrderTable from "../../component/order-manage/OrderTable.vue";
import OrderForm from "../../component/order-manage/OrderForm.vue";

export default {
  OrderManage,
  OrderTable,
  OrderForm,
};
```

### 父子组件引入

当自定义 Vue 组件存在父子关系时，**父组件必须在 `components` 选项中显式注册子组件**，然后在 `template` 中使用。

规则：

- 父组件必须 `import` 子组件的 `.vue` 文件，并在 `export default` 的 `components` 中注册。
- 禁止在 `template` 中使用未引入的子组件。
- 子组件 `name` 仍需以 `tech-` 开头，用于 IIDP 的 `comps.js` 注册；父组件内引用使用 import 的变量名。

示例：

```vue
<!-- apps/component/order-manage/OrderManage.vue -->
<template>
  <div class="order-manage">
    <tech-order-table :table-data="tableData" @edit="handleEdit" />
    <tech-order-form
      :visible="formVisible"
      :form-data="currentRow"
      @save="handleSave"
    />
  </div>
</template>

<script>
// 引入子组件
import OrderTable from "./OrderTable.vue";
import OrderForm from "./OrderForm.vue";

export default {
  name: "tech-order-manage",
  // 注册子组件
  components: {
    "tech-order-table": OrderTable,
    "tech-order-form": OrderForm,
  },
  props: {
    value: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      tableData: [],
      formVisible: false,
      currentRow: {},
    };
  },
  methods: {
    handleEdit(row) {
      this.currentRow = row;
      this.formVisible = true;
    },
    handleSave() {
      this.formVisible = false;
    },
  },
};
</script>
```

```vue
<!-- apps/component/order-manage/OrderTable.vue -->
<template>
  <el-table :data="tableData" border>
    <el-table-column prop="name" label="名称" />
    <el-table-column label="操作">
      <template slot-scope="{ row }">
        <el-button type="text" @click="$emit('edit', row)">编辑</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
export default {
  name: "tech-order-table",
  props: {
    tableData: {
      type: Array,
      default: () => [],
    },
  },
};
</script>
```

## custom-view-component

用途：自定义视图组件，也叫区块视图组件，用于复用一段 IIDP 视图配置，不写 Vue 运行时代码。

组件文件：

```js
// apps/component/test.js
const test = {
  name: "tech-test",
  __block: true,
  view: {
    type: "container",
    id: "tt-1",
    autoChildrenId: true,
    items: [
      {
        type: "button",
        id: "tt-2",
        value: "区块视图组件测试按钮",
      },
    ],
  },
};

export default test;
```

注册入口：

```js
// apps/<appName>/common/comps.js
import test from "../../component/test.js";

export default {
  test,
};
```

视图中使用：

```js
{
  __block: 'test',
  preId: 'my-pre-',
  id: 'newId',
  text: 'myText'
}
```

关键规则：

- 文件通常放在 `apps/component/<name>.js`。
- 组件定义必须包含 `name`、`__block: true`、`view`。
- `name` 必须以 `tech-` 开头。
- `name` 是组件内部名称，必须作为视图调用 `__block` 的来源。
- `view.id` 建议唯一；`autoChildrenId: true` 可自动为子节点添加 id。
- 视图调用处 `__block` 必须与组件 `name` 去掉 `tech-` 前缀后的值完全一致，包括大小写和中划线。
- 例如定义 `name: 'tech-my-block-view'`，调用处必须写 `__block: 'my-block-view'`，不能写成 `myBlockView`。
- `common/comps.js` 中导出的 key 不作为 `__block` 的命名来源；不要用导出 key 推断或替代 `__block`。
- 新建或使用自定义视图组件时，必须同时核对：组件 `name`、调用处 `__block` 是否满足去 `tech-` 前缀后一致。
- 调用处字段会与组件 `view` 深度合并，同名字段以调用处为准。
- `type` 以组件 `view.type` 为准。
- 调用处配置 `preId` 时，会给顶级节点和子节点 id 添加统一前缀。
- 缺少组件注册 key、区块组件名或复用参数时先询问用户，不要猜。
