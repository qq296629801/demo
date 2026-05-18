# IIDP 前端 SDD 技术落地规格模板

## 定位与使用时机

本文是"技术落地规格"模板，适用于**已知 IIDP 实现方式、需要输出节点树/数据源/绑定/事件/命令等技术细节**的场景。

| 场景                                               | 用哪个模板                                                             |
| -------------------------------------------------- | ---------------------------------------------------------------------- |
| 从业务需求/用户故事生成前端规格                    | 用 `skills/frontend/references/iidp-frontend-spec-doc/SKILL.md` 的模板 |
| 已有需求分析，需要输出节点树、数据源等技术落地细节 | 用**本文**模板                                                         |
| 需要页面级交互状态、响应式、可访问性验收           | 先读 `sdd-frontend-interaction.md`，再用本文补全技术细节               |

**分工边界**：

- `iidp-frontend-spec-doc`：业务语言 → IIDP 实现建议（"要做什么、用哪种方式"）
- `sdd-frontend-interaction.md`：交互行为、状态流转、响应式、可访问性（"用户怎么用、状态怎么变"）
- **本文**：IIDP 实现建议 → 技术落地规格（"节点怎么写、绑定怎么配"）

三个模板不重复输出。确定实现方式后，用本文输出可直接指导代码生成的技术规格。

---

## 上游依赖

生成 IIDP 前端规格时，**必须先调用** [iidp-frontend-spec-doc](../../frontend/references/iidp-frontend-spec-doc/SKILL.md)（以下简称 `spec-doc`）完成需求分析，再按本模板输出 SDD 前端规格。

需要交互行为描述时，先读取 [sdd-frontend-interaction.md](sdd-frontend-interaction.md) 生成交互规格，再回到本模板补全技术细节。本文不重复输出用户流程、响应式和可访问性内容。

**为什么不能跳过 spec-doc：** spec-doc 包含需求分析流程、IIDP 判断规则和输出原则，这些是生成正确前端规格的前提。本模板只是文档结构，不包含分析逻辑。

## IIDP 前端核心约束

以下约束源自 [IIDP 前端 SKILL.md](../../frontend/SKILL.md)，在填写模板时必须贯穿始终：

1. **实现优先级**：传统管理后台页面优先使用 IIDP 标准模板与在线视图，前端不新增代码。需要前端介入时，优先 hook → 扩展视图 → 自定义 Vue2 组件。
2. **节点 id 来源**（按优先级依次获取）：
   - ① 用户明确提供
   - ② 需求规格（`requirements.md`）中的菜单 key——标准视图节点 id 前缀取菜单 key（如菜单 key 为 `books_bookmgr_root_menu`，则节点 id 为 `books_bookmgr_root_menu_xxx`，`xxx`后缀来源于标准模板 ID 规则库）
   - ③ 标准模板 ID 规则库推导（**必须先读取** [iidp-frontend-standard-ids](../../frontend/references/iidp-frontend-standard-ids/SKILL.md) 获取节点 id 命名映射规则，不得凭菜单名或文案自行拼接）
   - ④ 询问用户提供或浏览器控制台验证

   不确定的必须保留为"待确认"，**不得按文案猜测或自行拼接**。

## 原型获取方式

生成 UI 原型描述前，必须先提示用户补充任意支持 MCP 的原型/设计/产品工具服务。当前环境能列出 MCP 资源时，优先使用 `list_mcp_resources` 查找；用户提供资源 URI 后，用 `read_mcp_resource` 读取。无法读取 MCP 服务时，允许截图、导出文档或文字描述兜底，但必须标记"原型来源：待确认"。

## 关联文件

| 文件                                                                                        | 用途                                   | 何时配合使用                                   |
| ------------------------------------------------------------------------------------------- | -------------------------------------- | ---------------------------------------------- |
| [sdd-frontend-interaction.md](sdd-frontend-interaction.md)                                  | 交互行为、状态、响应式、可访问性       | 需要用户流程、交互状态、响应式验收时先读此文件 |
| [sdd-contracts.md](sdd-contracts.md)                                                        | 前后端契约：JSON-RPC、Filter、节点属性 | 生成数据源、服务参数、权限码时参考             |
| [sdd-validation.md](sdd-validation.md)                                                      | 实现验证、失败处理、复盘               | 交付前运行验证清单                             |
| [iidp-frontend-standard-ids](../../frontend/references/iidp-frontend-standard-ids/SKILL.md) | 标准模板节点 ID 命名规则               | 推导节点 id 时参考                             |

---

## 规格文档模板

以下为完整的 SDD 前端规格文档模板。生成规格时，严格按照此结构输出，占位符 `[...]` 替换为实际内容，无法确认的信息填写"待确认"。

标注 **[核心]** 的章节必须输出；标注 **[可选]** 的章节按需输出。

````markdown
# [功能名称] IIDP 前端规格

## 1. 页面定位 [核心]

- 页面/菜单名称：
- 推荐实现：标准模板/在线视图/hook/扩展视图/自定义 Vue2 组件
- 是否需要前端代码：是/否
- 目标工程：
- 目标应用：`apps/<appName>`
- product（产品线标识）：`[product]`
- productDesc（产品线名称）：`[productDesc]`
- productSequence（导航排序）：`[productSequence]`

## 2. 原型来源 [核心]

- 原型来源：MCP/截图/导出文档/文字描述/待确认
- MCP 资源 URI（如有）：
- 页面/Frame：
- 读取状态：已读取/待授权/不可用/待确认

从原型中提取（如已读取）：

| 提取项              | 内容                            |
| ------------------- | ------------------------------- |
| 页面清单            | [...]                           |
| 关键区域            | [...]                           |
| 用户路径            | [...]（详细流程见交互规格文档） |
| IIDP 标准模板匹配度 | [...]                           |

## 3. 页面结构 [核心]

| 区域   | 内容   | 后端视图或前端节点            |
| ------ | ------ | ----------------------------- |
| 搜索区 | [字段] | `{model_name}_search`         |
| 表格区 | [列]   | `{model_name}_grid`           |
| 工具栏 | [按钮] | `{model_name}_grid` 内的 tbar |
| 表单   | [字段] | `{model_name}_form`           |
| 树     | [结构] | `{model_name}_tree`           |
| 子表   | [结构] | `{model_name}_form` 内的 tabs |

## 4. UI 宪法约束 [核心]

- 标准模板匹配度：
- 是否沿用平台主题：
- 是否需要自定义样式：
- 容器和滚动策略：
- 弹窗/抽屉尺寸策略：
- iframe/TABIFRAME：仅在平台能力或性能场景需要时启用，并标记依据
- 需要产品确认的原型差异：

## 5. 页面骨架与 IIDP 映射 [核心]

### 5.1 页面骨架

```text
[页面名称]
├── 顶部区域：[标题/面包屑/状态提示/全局按钮]
├── 搜索区：[搜索字段、默认展开/收起]
├── 主内容区：[表格/树表/卡片/主表单/上下表]
├── 操作区：[工具栏按钮、行按钮、批量按钮]
├── 弹窗/抽屉：[新增/编辑/详情/选择器]
└── 反馈区：[空态、加载中、成功/错误提示]
```

### 5.2 原型到 IIDP 的映射

| 原型元素         | 首选 IIDP 承载方式                 | 需要前端扩展的条件                 |
| ---------------- | ---------------------------------- | ---------------------------------- |
| 搜索表单         | `{model_name}_search`              | 复杂联动、特殊校验、动态字段       |
| 数据表格         | `{model_name}_grid`                | 复杂列渲染、特殊按钮、跨数据源聚合 |
| 新增/编辑表单    | `{model_name}_form`                | 保存前特殊处理、复杂子表           |
| 树/上下表/子表   | 后端高级视图和标准模板             | 标准模板无法表达交互细节           |
| 弹窗/抽屉/选择器 | openView/useOpenView/useSelectTree | 特殊布局或复杂回填逻辑             |
| 自定义交互区     | 扩展视图或 Vue2 组件               | IIDP 节点无法表达内部状态管理      |

> 交互行为细节（用户流程、状态清单、响应式、可访问性）见 [交互规格文档]。

## 6. 节点与绑定规格 [核心]

### 6.1 节点树

```text
page: [菜单或页面 key]
└── container: [页面主容器 id]
    ├── search: [搜索区视图或节点 id]
    ├── grid/table: [表格视图或节点 id]
    │   ├── tbar/button: [工具栏按钮]
    │   └── operation/button: [行操作按钮]
    ├── form/dialog/drawer: [表单或弹窗节点]
    └── extension/container: [扩展节点]
```

### 6.2 节点与选择器

| 节点     | type                        | id          | selector   | 来源                                    | 用途   |
| -------- | --------------------------- | ----------- | ---------- | --------------------------------------- | ------ |
| [节点名] | container/table/form/button | [id/待确认] | [selector] | 用户提供/后端契约/标准模板规则库/待确认 | [说明] |

填写示例：

| 节点            | type   | id                                               | selector         | 来源           | 用途          |
| --------------- | ------ | ------------------------------------------------ | ---------------- | -------------- | ------------- |
| 搜索区          | search | `books_bookmgr_root_menu_container_table_search` | 需求规格菜单 key | 后端视图定义   | 查询条件      |
| 列表表格        | grid   | `books_bookmgr_root_menu_table_main_table`       | 需求规格菜单 key | 后端视图定义   | 数据展示      |
| 新增按钮        | button | `books_bookmgr_root_menu_table_toolbar_create`   | 需求规格菜单 key | 标准模板规则库 | 触发新增      |
| 详情/编辑表单页 | dialog | `books_bookmgr_root_menu_table_detail`           | 需求规格菜单 key | 后端视图定义   | 编辑/查看表单 |

### 6.3 绑定、事件与命令

| 能力     | 配置              | 数据来源               | 触发方式   | 说明                               |
| -------- | ----------------- | ---------------------- | ---------- | ---------------------------------- |
| 单向绑定 | `bind_`           | `$ds`/`$self`/其他节点 | 数据变化   | [说明]                             |
| 双向绑定 | `bind_two_`       | 表单字段               | 用户输入   | [说明]                             |
| 条件显示 | `bind_display`    | `$ds`/`$self`/表达式   | 数据变化   | 控制节点显隐，常用于按钮按状态显示 |
| 事件     | `bind_on_事件名`  | params/self/value      | 用户操作   | 事件名必须真实存在，不臆造         |
| 命令     | `commands`/`$cmd` | 命令入参               | 事件或绑定 | [说明]                             |

填写示例：

| 能力     | 配置                                                      | 说明                 |
| -------- | --------------------------------------------------------- | -------------------- |
| 单向绑定 | `bind_tableData: "$ds.grid.data.tableData"`               | 表格数据             |
| 双向绑定 | `bind_two_value: "$ds.form.data.name"`                    | 表单字段             |
| 条件显示 | `bind_display: "$ds.grid.checkedData.status === 'DRAFT'"` | 仅草稿态显示编辑按钮 |
| 事件     | `bind_on_click`                                           | 点击事件             |
| 命令     | `commands: [{ cmd: "refreshTable", params: {} }]`         | 刷新表格             |

### 6.4 交互状态表

> 完整的交互状态描述（触发条件、展示效果、用户验收点）见 [交互规格文档]。本表只记录与技术实现相关的状态承载方式。

| 组件/节点 | 关键状态                             | IIDP 承载方式          | 数据或服务   | 技术验收点 |
| --------- | ------------------------------------ | ---------------------- | ------------ | ---------- |
| [节点]    | loading/empty/error/disabled/success | 标准模板/hook/扩展视图 | [ds/service] | [检查点]   |

## 7. 字段规格 [核心]

| 字段          | 含义   | IIDP 控件               | 必填  | 默认值 | 校验   | 后端字段      |
| ------------- | ------ | ----------------------- | ----- | ------ | ------ | ------------- |
| `[fieldName]` | [说明] | Input/Lookup/Upload/... | 是/否 | [值]   | [规则] | `[fieldName]` |

## 8. 操作与事件 [核心]

> **填写 args 列前必须先读契约**：
> - 内置服务（`search/create/update/delete`）直接填平台标准参数名（`valuesList`、`ids/values` 等）。
> - 自定义 `@MethodService` 的 args **必须从 `sdd-contracts.md` 对应模型的服务契约表 `args 参数（名称: 类型）` 列读取**，不得自行推断或凭按钮文案猜测。
> - 若契约中该服务 args 列为空或"待确认"，**先补全契约再填写本节**。

| 操作 | 入口 | 触发条件         | 前端行为 | 后端服务 | args（来自契约） | 成功结果 | 失败处理     |
| ---- | ---- | ---------------- | -------- | -------- | ---------------- | -------- | ------------ |
| 新增 | tbar | 有 `create` 权限 | 打开表单 | `create` | `valuesList`     | 刷新列表 | 显示平台错误 |

## 9. IIDP 实现分支 [核心]

### 决策路径

```text
需求能否由后端标准视图 + 在线视图完成？
  → 是：§9.1 标准模板/在线视图（前端无代码）
  → 否：是否可以通过 hook 增强（不改节点结构）？
    → 是：§9.2 hook
    → 否：是否可以通过插入/合并/替换节点实现？
      → 是：§9.3 扩展视图
      → 否：§9.4 自定义 Vue2 组件
```

### 9.1 标准模板/在线视图

- 是否满足：
- 后端视图配置位置：
- 前端无需新增代码的理由：

### 9.2 hook

> **生成 hook 前必须先读取 [`iidp-frontend-extension-dev/SKILL.md`](../../frontend/references/iidp-frontend-extension-dev/SKILL.md) §扩展钩子选择章节**，获取完整钩子点列表（`grid`/`form`/`search`/`tree`/`page`/`gridPage`/`detailPage`）、每类钩子的返回值规则（`before*` 返回 params、`after*` 返回 res、`can*` 返回 boolean）和 `vm.super` 调用规范。内联示例仅为格式参考，**实际可用钩子点和 return 规则以该文件为准**，不得凭猜测或示例推断。

- 是否需要：
- hook 路径：
- 是否调用 `vm.super`：
- 关键 `vm.biz` 读写点：

路径格式：`apps/<appName>/views/<moduleName>/hooks/<hookName>.js`

示例（仅展示格式，完整钩子点见 iidp-frontend-extension-dev/SKILL.md）：

```javascript
export default {
  test_hook_extend_view: {
    selector: {
      attr: "id",
      value: "menu_container_main", // id选择顶层节点，页面前缀 + 'container_main'
    },
    type: "merge",
    hook: {
      grid: {
        canCreate: async (vm, params, options) => {
          console.log("====grid.canCreate====", vm, params, options);
          return true; // 返回false则按钮禁用
        },
        beforeQuery: async (vm, params, options) => {
          // params: 调用查询接口时传递的参数
          params.testArg = 1; // 在查询接口调用前修改接口参数
          return params; // 必须 return params
        },
      },
      form: {
        beforeQuery: async (vm, params, options) => {
          // params: 表单查询参数
          console.log(vm, params, options, "==beforeQuery==");
          params.aaa = 1;
          return params; // 必须 return params
        },
      },
    },
  },
};
```

### 9.3 扩展视图

> **生成扩展视图前必须先读取 [`iidp-frontend-extension-dev/SKILL.md`](../../frontend/references/iidp-frontend-extension-dev/SKILL.md) §扩展开发协议章节**，获取完整扩展类型规则（selector 写法、beforeOperate 参数、命名唯一性、权重与顺序、selector.pre 复用、动态注册等）。内联扩展类型表仅为速查，**merge 的 items 合并行为、replace 的使用限制等细节以该文件为准**。

- 是否需要：
- 目标节点 id：
- 节点 id 来源：后端契约/标准模板规则库推导/用户提供/待确认
- 扩展类型：before/after/append/unshift/merge/replace/delete/custom
- 新增节点 id：

| 扩展类型  | 含义                                                                                        | 典型场景                                |
| --------- | ------------------------------------------------------------------------------------------- | --------------------------------------- |
| `before`  | 在目标节点前插入                                                                            | 在标准按钮前加自定义按钮                |
| `after`   | 在目标节点后插入                                                                            | 在标准字段后加自定义按钮                |
| `append`  | 在选中节点的 items 尾部插入视图                                                             | 在父容器底部追加一个子节点              |
| `unshift` | 在选中节点的 items 头部插入视图                                                             | 在父容器头部追加一个子节点              |
| `replace` | 替换目标节点                                                                                | 替换标准模板页面（禁止 `type: 'page'`） |
| `delete`  | 删除选中节点                                                                                | 删除某个节点                            |
| `custom`  | 对选中节点不做任何处理但会执行 beforeOperate 函数，用户自行动态构造                         | 在beforeOperate做其他操作               |
| `merge`   | 选中节点与新 view 视图进行属性合并 若选中节点有 items 则不合并 items 属性其他属性都深度合并 | 修改按钮 auth、添加事件                 |

### 9.4 自定义 Vue2 组件

- 是否需要：
- 触发原因（IIDP 节点无法表达的具体能力）：
- 组件职责：
- 注册入口：`apps/<appName>/common/comps.js`
- 组件边界（props 输入 / 事件输出如何映射到 IIDP 节点属性和 `bind_on_`）：

## 10. 数据源与绑定 [核心]

> **填写数据源和绑定配置前必须先读取 [`iidp-frontend-dev-manual/SKILL.md`](../../frontend/references/iidp-frontend-dev-manual/SKILL.md)**，获取完整的数据源类型（meta/api）、`reqPrep`/`reqAfter` 签名与返回规则、`bind_`/`bind_two_`/`bind_display`/`bind_on_` 可用属性和事件名。内联 JSON 示例仅为格式参考，**完整配置选项和事件名以 dev-manual 为准**，不得凭示例推断或猜测属性名。

| 数据源   | 类型     | service/model/url | 触发方式         | 返回用途 |
| -------- | -------- | ----------------- | ---------------- | -------- |
| `[name]` | meta/api | `[params]`        | autoRequest/手动 | [说明]   |

> **约束**：所有 API 请求必须通过 IIDP 数据源或 `window.Tech.httpMeta` 发起，禁止自行引入 axios、fetch 等请求库。

标准数据源配置示例（完整选项见 iidp-frontend-dev-manual/SKILL.md）：

```json
{
  "type": "meta",
  "name": "name1",
  "autoRequest": true,
  "options": {
    "model": "books_manage",
    "service": "search",
    "args": {
      "filter": [],
      "limit": 20,
      "offset": 0,
      "order": "id desc"
    }
  }
}
```

带 reqPrep/reqAfter 的数据源示例：

```json
{
  "type": "api",
  "name": "customList",
  "autoRequest": false,
  "options": {
    "model": "books_manage",
    "service": "getCustomList",
    "args": { "type": "ACTIVE" }
  },
  "reqPrep": "(vm, params) => { params.args.type = vm.biz.form.data.category; return params }",
  "reqAfter": "(vm, res) => { return res.data || [] }"
}
```

绑定和事件汇总：

| 类型           | 配置 | 说明 |
| -------------- | ---- | ---- |
| `bind_`        |      |      |
| `bind_two_`    |      |      |
| `bind_display` |      |      |
| `bind_on_`     |      |      |
| commands       |      |      |

## 11. 多模型/主子表场景 [可选]

> 仅在页面涉及多个模型（主子表、上下表、树表联动、跨应用 Lookup）时输出本章节。

### 11.1 模型关系

| 主模型         | 子模型             | 关系类型                | 联动方式                  |
| -------------- | ------------------ | ----------------------- | ------------------------- |
| `[model_name]` | `[sub_model_name]` | OneToMany/Many2One/Tree | tabs 子表/上下表/树选联动 |

### 11.2 多模型节点树

```text
page: [页面 key]
└── container: [主容器]
    ├── search: [主模型搜索]
    ├── grid: [主模型表格]
    ├── tabs: [子表区域]
    │   ├── tab1: [子模型1 表格]
    │   └── tab2: [子模型2 表格]
    └── form: [主表单]
        └── tabs: [子表单区域]
```

### 11.3 多模型数据源

| 数据源   | 模型           | 联动触发        | 过滤条件                  |
| -------- | -------------- | --------------- | ------------------------- |
| `[name]` | `[model_name]` | 主表行切换/手动 | `$ds.grid.checkedData.id` |

## 12. 按需加载配置 [可选]

- `effectPaths`：自动取第 1 节的 `product` 配置，格式为 `^/<product>/`（例如 product 为 `abc` 则填 `^/abc/`）。无 `product` 时不配置，保留默认占位符。
- `effectPageIds`：**默认不添加，仅在用户明确要求时配置**

## 13. 待确认事项 [核心]

- [原型来源/MCP 资源 URI/接口/模型/节点 id/权限码/枚举/页面 id]

## 14. 验收（规格完整性） [核心]

> 实现阶段验证详见 [sdd-validation.md](sdd-validation.md)。

- [ ] 标准模板或在线视图能完成主流程，未被误写为自定义页面
- [ ] 完全替换标准模板页面时使用了 `replace`，未使用 `type: 'page'`
- [ ] 所有 API 请求通过 IIDP 数据源或 `window.Tech.httpMeta`，未自行引入请求库
- [ ] `effectPageIds` 未自动添加，已提醒用户确认
- [ ] UI 原型已描述页面骨架和 IIDP 承载方式；交互行为引用交互规格文档
- [ ] 已给出页面节点树、目标 selector、节点 id 来源和绑定/事件/命令表
- [ ] 如有扩展，目标节点 id 有明确来源
- [ ] hook 路径正确，必要时调用 `vm.super`
- [ ] 数据源请求参数与后端 JSON-RPC 契约一致
- [ ] 节点 id 未编造，无法确认的已标记"待确认"
- [ ] 多模型场景（如有）已说明模型关系、联动方式和子表数据源
````
