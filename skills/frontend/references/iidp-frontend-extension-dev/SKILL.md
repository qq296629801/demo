---
name: iidp-frontend-extension-dev
description: 协助在已有 IIDP 前端工程和应用中开发业务功能扩展。当用户询问 IIDP 前端相关问题或知识，或需要实现 IIDP 前端业务功能、写应用扩展、扩展视图、修改节点、配置数据源、属性绑定、选择器、事件绑定、公共命令、扩展钩子或 hook 时使用。
---

# IIDP Frontend Extension Development

## 使用前提

使用此 skill 时，默认用户已经准备好 IIDP 前端工程和应用，当前任务是在应用内开发业务功能。

### 存量项目定位流程

> 如果用户已明确提供工程路径和 appName，跳过本流程，直接进入下方"开始实现前先确认"。

当用户未指定工程路径或 appName 时，按以下三步完成定位：

**步骤 1：工程发现**

从用户工作区中搜索 IIDP 前端工程。搜索策略（按优先级执行，命中即停止）：

1. 用户指定的路径 → 直接验证
2. 常见位置快速探测：`front/`、`frontend/`、项目根目录下的 `sie-iidp-frontend-project/`
3. 以上未命中时，再以最大深度 2 层搜索 `**/package.json`，逐个验证标志

IIDP 前端工程的识别标志（满足 ≥3 项即视为候选）：

| 标志 | 说明 |
|---|---|
| `package.json` 含 `init:tech` 脚本 | IIDP 脚手架特有命令 |
| `apps/` 目录存在 | 业务应用存放目录 |
| `config/apps.json` 存在 | 工程级应用注册与全局配置 |
| `build/webpack.dev.js` 存在 | IIDP 构建配置 |

发现结果处理：

- **0 个候选**：告知用户未找到 IIDP 前端工程，路由到 [iidp-frontend-init](../iidp-frontend-init/SKILL.md) 创建工程。
- **1 个候选**：记录工程路径，继续步骤 2。
- **多个候选**：列出每个候选的路径、`package.json` 中的 `name` 字段、`apps/` 下的应用数量，使用交互弹框（AskUserQuestion）让用户选择。

**步骤 2：应用定位**

工程确认后，扫描 `apps/` 目录定位目标应用。扫描时排除非应用目录：`component`、`base`、以 `dist` 开头的目录、`node_modules`、隐藏目录。

对每个候选应用目录，读取 `apps/<name>/config/app.json` 获取 `effectPaths` 等上下文信息。

- 用户提供了 appName → 验证 `apps/<appName>/` 是否存在。不存在则展示已发现的应用列表，请用户选择或确认正确名称。
- 用户未提供 appName → 列出所有候选应用（名称 + 路由匹配规则），使用交互弹框让用户选择。
- 仅 1 个候选应用 → 自动使用，但需向用户确认。
- 0 个应用 → 路由到 [iidp-frontend-init](../iidp-frontend-init/SKILL.md) 创建应用。

**步骤 3：读取现有模式**

工程路径和 appName 都确认后，读取以下文件了解现有代码约定（用于匹配风格，而非全局读取）：

- `apps/<appName>/views/index.js`：扩展入口结构、import 命名风格。
- `apps/<appName>/config/app.json`：当前按需加载配置（`effectPaths`、`effectPageIds`）。
- `apps/<appName>/views/` 目录下的文件列表：业务目录命名约定（不逐个读取文件内容）。
- `apps/<appName>/common/comps.js`（如存在）：已注册的自定义组件列表。

完成定位后，进入下方"开始实现前先确认"。

开始实现前先确认：

- 目标应用目录，例如 `apps/<appName>`。
- 目标业务页面、菜单、视图或节点；如果用户只描述业务需求，先搜索现有视图和扩展入口。
- 功能类型：新增/调整视图、表单、表格、弹窗、接口数据、联动、按钮事件、公共逻辑。
- 是否已有节点 id、数据源名、模型名、接口参数或后端元模型约定。

## 定位与依赖

- 定位：在既有 IIDP 前端工程与应用内实现业务扩展（扩展视图/hook/数据源/绑定/事件/commands/自定义组件）。
- 上游：[iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)（框架机制、组件属性、扩展协议、工程结构等事实参考）。
- 下游：通常被 [iidp-frontend-spec-code](../iidp-frontend-spec-code/SKILL.md) 调用，用于将规格落到扩展实现；也可直接承接业务开发任务。

## 工程结构定位

> 如果工程根目录和 appName 尚未确定，先执行"使用前提"中的存量项目定位流程。

优先在业务应用目录内工作：

- `apps/<appName>/common`：公共总扩展。常见入口包括 `assetImport.js`、`asset.json`、`common.js`、`comps.js`、`extendView.js`、`hook.js`、`schema.js`、`index.js`。
- `apps/<appName>/views`：纯 JS 格式扩展视图，通过扩展能力合并到主视图中。业务视图通常放在独立业务目录或业务命名文件中，并由 `views/index.js` 汇总。
- `apps/<appName>/config`：当前 App 的额外配置，例如 `app.json`、全局变量。
- `apps/<appName>/resource`、`apps/<appName>/static-resource`：语言包、皮肤、静态资源等。
- `apps/component`：公共业务组件。

不要把业务代码写进运行临时生成目录：`resource`、`static-resource`、`views`、`dist`、`distApp`、`distTmp`。如果仓库中存在源目录和生成目录同名，先结合构建脚本、入口导入和 git 状态判断真实源文件。

## 搜索边界

默认只读取和修改业务扩展源码：

- `apps/<appName>/views`
- `apps/<appName>/common`
- `apps/<appName>/config`
- `apps/component`
- 当前业务相关源码和 IIDP 文档

不要读取或分析以下目录和文件，除非用户明确要求调试底座源码或依赖包实现：

- `node_modules`
- `dist`
- `distApp`
- `distTmp`
- `umdComps`
- `build`
- 编译后的 `.min.js`、`.umd.js`、bundle 文件

遇到框架能力不清楚时，优先查 IIDP 文档和当前应用扩展代码，不要通过 `node_modules` 或底座编译产物反推框架实现。

## 文档索引

> **遇到框架能力不确定时（数据源类型、bind_ 属性名、事件名、vm.biz API、goTo 方法等），必须先读取 [iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)**，不得凭示例或经验猜测。该 skill 涵盖节点系统、数据源、属性绑定、选择器、数据联动、事件绑定、公共命令、视图行为协议、Hook 系统、扩展开发等核心机制的完整参考，以及所有组件的文档索引。需要原始文档细节时再按索引到 `iidpDoc/` 查找。

## 组件规则

生成或修改 IIDP 组件节点前，按以下优先级查找组件规则：

1. 先读取 [COMPONENT_RULES.md](COMPONENT_RULES.md)，已覆盖的组件按该文件写法生成。
2. COMPONENT_RULES.md 未覆盖的组件，先通过 [iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md) 到 IIDP 开发文档中查找。
3. 文档中也找不到时，询问用户补充组件文档或确认属性，不要猜。

- 不要直接套用 Element UI、HTML 或通用前端组件属性。
- 只是新增按钮、容器、表格、弹窗等已有 IIDP 节点时，查对应基础组件规则。
- 需要真实 Vue 组件能力时，使用 `custom-vue-component` 规则。
- 只是复用一段 IIDP 视图配置时，优先使用 `custom-view-component` 规则。
- 组件文档未显式支持但 Element UI 支持的属性，放入 `ATTRS`。
- Element UI 原生事件放入 `ONS`。

## 组件注册与引用边界

- **Vue 组件注册只在 `apps/<appName>/common/comps.js` 中完成**，通过 import `.vue` 文件并 export。
- **扩展视图 JS 文件（`apps/<appName>/views/` 下的 `.js` 文件）禁止 import `.vue` 文件。** 扩展视图通过 `type` 引用已在 `comps.js` 中注册的组件，不需要也不应该直接引用组件文件。
- **Vue 组件之间的父子引用在 `.vue` 文件内部的 `import` + `components` 选项中完成**，不经过 `comps.js`。

## 业务扩展最小读取策略

当用户明确给出 `apps/<appName>` 和业务扩展目标时，采用最小读取范围：

- 新建扩展时默认只读取 `apps/<appName>/views`，以及必要的 `apps/<appName>/views/index.js` 入口信息。
- 只有涉及应用配置、按需加载或全局变量时，才读取 `apps/<appName>/config`。
- 新建普通业务扩展时不需要特意读取 `apps/<appName>/common`。
- 只有用户明确要求自定义 Vue 组件、自定义组件注册、自定义视图组件或区块视图组件时，才读取或修改 `apps/<appName>/common/comps.js` 与 `apps/component`。
- 只在确有公共组件复用需求或自定义视图组件需求时读取 `apps/component`。
- 不要搜索框架源码。
- 不要为了理解标准按钮、表格、表单、toolbar、删除按钮等底层实现去读取 `node_modules`。
- 需要框架规则时只查 IIDP 文档；需要节点 id 时优先查规则库，无匹配则必须由用户提供。
- 用户没有提供目标节点 id 时，不要猜测、编造、查源码推断或按命名习惯生成节点 id。

<!-- 例如“在主表格页工具栏删除按钮后新增一个测试按钮”这类需求，必须先让用户提供删除按钮稳定节点 id；拿到 id 后只需要定位业务 App 的 `views/index.js` 并用 `type: 'after'` 插入按钮，不需要读取 `common`、底座源码或 `node_modules`。 -->

## 扩展开发协议

### 通用规范

- **API 请求统一走 IIDP 数据源或 `window.Tech.httpMeta`**，禁止自行引入 axios、fetch 等请求库。`window.Tech.httpMeta` 的 `params` 参数（包括 `app` 后端应用名、`model`、`service`、`args` 里的对象结构）必须依据前后端契约填写，不同接口的参数结构不同，**不允许凭猜测编写**。接口返回数据在 `res.data` 中。
- **业务涉及跳转菜单或跳转页面时，先查阅 IIDP 文档确认 `goTo` 方法是否满足需求**。如果 `goTo` 满足则优先使用；不满足时再考虑其他方式，并说明原因。

### 目标节点 id 强制规则

新建扩展时必须先确认目标节点 id。按以下优先级查找：

1. **用户已提供**：用户在需求中明确指定了目标节点 ID，直接使用，无需查阅规则库。
2. **标准模板 ID 规则库**：查阅 [STANDARD_TEMPLATE_IDS.md](../iidp-frontend-standard-ids/STANDARD_TEMPLATE_IDS.md)，根据页面类型（gridPage/detailPage/treePage）和业务描述推导节点 ID。推导的 ID 标注为"基于标准模板推导"，建议用户在浏览器控制台用 `tech_app.page.getNode('推导的ID')` 验证。
3. **询问用户**：无法从规则库推导时，停止并询问用户提供节点 ID。

禁止行为：

- 不得根据菜单名、按钮名、模型名、常见命名习惯自行拼接或猜测节点 id。
- 不得通过读取 `node_modules`、底座源码或编译产物反推节点 id。
- 不得先写一个可能正确的 selector 让用户后续再改。

允许行为：

- 根据 [STANDARD_TEMPLATE_IDS.md](../iidp-frontend-standard-ids/STANDARD_TEMPLATE_IDS.md) 规则库推导标准模板页面的节点 ID（标注为"待确认"）。
- 指导用户在浏览器控制台或页面审查中获取节点 id。
- 询问用户提供目标节点 id 后再继续编写扩展。

业务扩展优先放在 `apps/<appName>/views/<business>/<business>.js`，并由 `apps/<appName>/views/index.js` 引入后汇总导出：

```js
import business from "./business/business";

export default {
  ...business,
};
```

扩展文件通常导出扩展定义对象：

```js
export default {
  business_extend_view: {
    type: "merge",
    selector: { attr: "id", value: "menu_container_main" },
    view: {},
  },
};
```

扩展类型选择：

- `before` / `after`：在目标节点前后插入同级节点。
- `append` / `unshift`：在目标节点 `items` 尾部或头部插入子节点。
- `merge`：深度合并目标节点属性；目标节点已有 `items` 时通常不合并 `items`。
- `replace`：用新 `view` 完全替换目标节点。
- `delete`：删除目标节点。
- `custom`：不直接改目标节点，只执行 `beforeOperate`，适合自行动态构造。

### 页面替换策略

当业务需求是**完全替换标准模板页面**（如页面需要整体重做）时：

- **首选 `replace` 替换表格页视图根节点 `table_main_wrap`**（fullId 为 `idPre + "table_main_wrap"`），用新的 `view` 完全接管。不要 replace `container_main`（那是整个标准页根容器，包含树、内容区、详情区等，范围过大）。
- **禁止使用 `type: 'page'`** 创建自定义页面来替换现有标准页。`type: 'page'` 仅用于新增独立路由页面，不用于替换现有标准页。
- 即使页面完全由自定义 Vue2 组件构成，也应该通过 `replace` 将自定义组件嵌入到标准模板页面中，而非创建独立页面。
- 只有在需要新增独立路由页面的场景（如从菜单打开一个全新业务页）时，才使用 `type: 'page'`。

`beforeOperate(app, operate, options)` 会在扩展类型处理前执行：

- `app` 可访问页面节点和 `updatePageNode`。
- `operate.view` 是当前扩展配置里的新视图。
- `options.element` 是 selector 选中的当前视图对象。
- `options.originElement` 是选中视图的初始状态。
- `options.parent` 是选中节点的父级。
- `options.mountedNode` 可作为当前扩展展开的挂载上下文。
- 如果修改了 `operate.view`，返回值应是最终要参与扩展处理的新视图。

命名与顺序：

- 扩展文件按业务命名，例如 `tview__base__sidebar.js` 或现有项目约定的业务名。
- 扩展名必须全局唯一，默认按数字 `0-9`、字母 `a-z` 排序执行。
- 后运行的扩展可以二次扩展先运行扩展产生的节点。
- 需要指定执行顺序时配置 `extend: 'other_extend_view'`；同层继承可用 `weight` 调整顺序。
- 浏览器控制台可用 `window.techRepeatExtendName` 查看重复扩展名。

复用与动态扩展：

- `selector.pre` 可把同一扩展复用到多个 id 前缀，支持字符串、数组、函数。
- 多处复用且逻辑复杂时，把公共逻辑抽为普通 JS 方法，在 `beforeOperate` 中传入 `customOptions` 做差异化定制。
- 运行期动态注册扩展用 `tech_app.setExtend('全局唯一扩展名', definition)`，definition 与静态扩展定义一致。
- 调试节点扩展链路用 `tech_app.extendViewTrace('nodeId')`，入参支持节点 id 或模糊 id。

## 扩展钩子选择

标准页生命周期、查询、保存、权限控制等业务逻辑优先使用 hook；单纯节点结构调整再使用普通视图扩展。

hook 扩展通常挂在当前页面顶级节点上：`页面菜单 url + _container_main`，例如 `rbac_user_app_menu_container_main`。

### Hook 文件位置与格式

**Hook 扩展写在 `apps/<appName>/views/<business>/<business>.js`**，与普通扩展视图放在一起，由 `views/index.js` 汇总导出。**不要写到 `common/hook.js`**。

**正确格式**：hook 扩展必须使用 `selector` + `type: "merge"` + `hook` 结构：

```js
// apps/<appName>/views/syncLog/syncLog.js
export default {
  // 扩展名（全局唯一）
  sync_log_hook: {
    // selector 定位目标页面顶级节点
    selector: { attr: "id", value: "sync_log_container_main" },
    // hook 扩展使用 merge 类型
    type: "merge",
    // hook 对象包含各钩子函数
    hook: {
      grid: {
        async afterQuery(vm, params, options) {
          // 业务逻辑...

          if (vm.super?.["grid.afterQuery"]) {
            // 保留平台原逻辑
            return await vm.super["grid.afterQuery"](vm, params, options);
          }
          // 业务逻辑...
        },
        async select(vm, params, options) {
          // 业务逻辑...
          if (vm.super?.["grid.select"]) {
            return await vm.super["grid.select"](vm, params, options);
          }
          // 业务逻辑...
        },
      },
    },
  },
};
```

**错误格式**：直接以节点 id 作为 key，缺少 selector 和 type：

```js
// ❌ 错误写法
export default {
  "sync_log_container_main": {
    grid: {
      afterQuery(vm, params, options) { ... }
    }
  }
};
```

hook 结构按视图类型划分：

- `page`：标准页视图查询，如 `page.queryView`。
- `gridPage`：主列表页生命周期。
- `detailPage`：详情页生命周期和返回。
- `grid`：主表格查询、删除、选择、按钮、行内编辑；上下表子表放在 `grid.tabs.<field>`。
- `search`：主表搜索栏查询控制与校验。
- `form`：主表单查询、初始化、校验、保存；主表单子表放在 `form.tabs.<field>`。
- `tree`：树新增、删除、编辑、查询、保存、选择。

hook 规则：

- 钩子都是异步方法，常见签名是 `async (vm, params, options) => {}`。
- **所有 hook 方法必须 return 数据**。不 return 会导致页面数据丢失、空白或逻辑中断。**业务逻辑必须在 return 之前执行完毕**，不要在 return 之后再写业务代码（return 后的代码不会执行或逻辑会被跳过）。具体返回值规则：
  - `beforeQuery`/`beforeSave`/`beforeDelete` 等 before 类钩子：`return params`（处理后的参数）。
  - `query`/`save`/`delete` 等执行类钩子：`return await vm.super['hook.path'](vm, params, options)` 或 `return res`（接口返回数据或其他业务需要的新数据）。
  - `afterQuery`/`afterSave`/`afterDelete` 等 after 类钩子：返回原方法的数据（`return await vm.super['hook.path'](vm, params, options)`），或接口数据，或其他业务需要的新数据。
  - `can*` 钩子：`return true` 或 `return false`。
  - `select`/`cancelSelect`：`return params`。
  - `init`：`return params`（初始化后的表单数据）。
  - `validate`：`return await vm.super['hook.path'](vm, params, options)`。
- 调用 `vm.super['hook.path'](vm, params, options)` 时**必须 return 其返回值**，否则会丢失平台原逻辑的处理结果。
- `can*` 钩子返回 `false` 表示按钮置灰或不可操作。
- 非 `can*` 钩子返回 `false` 表示中断后续逻辑。
- `vm.biz` 是标准页业务上下文，按 `data`、`req`、`baseMethods`、`methods`、`nodes` 组织；业务扩展优先通过它读写标准页状态。
- **禁止编造 `vm.biz` 上的方法或属性路径**。`vm.biz` 下可用的 API 以 IIDP 文档为准。不确定某个方法是否存在时（如 `vm.biz.gridPage.baseMethods.openDrawer`），必须：
  1. 先查阅 IIDP 文档确认；
  2. 仍然无法确认时，标注为"待确认"并提示用户在浏览器控制台用 `window.Tech.$biz(id)` 验证，不要自行编造调用。
- **刷新表格数据时使用 `vm.biz.grid.methods.runRefresh()`**，不要调用 `vm.biz.grid.baseMethods.query()`。`runRefresh()` 封装了点击刷新按钮的操作，无需传参；`query()` 需要传参且容易出错。
- 可指导用户在控制台用 `window.Tech.$page(id)` 或 `window.Tech.$biz(id)` 查看标准页上下文。

按需求选择 hook：

- 查询视图：`page.queryView`。
- 主表格查询：`grid.beforeQuery`、`grid.query`、`grid.afterQuery`、`grid.queryCount`。
- 主表格删除：`grid.canDelete`、`grid.onConfirm`、`grid.beforeDelete`、`grid.delete`、`grid.afterDelete`。
- 主表格选择：`grid.select`、`grid.cancelSelect`。
- 搜索栏：`search.canQuery`、`search.validateQuery`。
- 主表单查询与保存：`form.beforeQuery`、`form.query`、`form.afterQuery`、`form.init`、`form.validate`、`form.beforeSave`、`form.save`、`form.afterSave`。
- 树：`tree.canCreate`、`tree.canDelete`、`tree.canEdit`、`tree.beforeQuery`、`tree.query`、`tree.afterQuery`、`tree.select`、`tree.save`、`tree.delete`。
- 子表格：上下表用 `grid.tabs.<field>.grid`，主表单子表用 `form.tabs.<field>.grid`。

## 工作流程

1. 定位工程与应用：如果工程路径和 appName 尚未确定，先执行"使用前提"中的存量项目定位流程。确认后，读取 `apps/<appName>/views/index.js`、已有业务文件命名风格、`config/app.json` 中的按需加载配置。
2. 理解现有视图：找目标页面、节点 id、数据源名、绑定表达式、事件和 commands；必要时用 `tech_app.printObj(...)` 的思路指导用户在浏览器中查看节点属性。
3. 判断扩展方式：生命周期、查询、保存、权限控制优先用 hook；节点结构调整、属性合并、插入删除再用普通视图扩展。
4. 确认组件协议：新增或修改组件节点前读取 `COMPONENT_RULES.md`；目标组件未覆盖时先询问用户，不猜属性。
5. 设计扩展点：判断应新增视图、合并视图、调整节点属性、配置数据源、绑定事件，还是抽公共命令。
6. 编写扩展代码：遵循已有模块导出方式、命名风格和 id 前缀；新增节点必须有稳定 id。
7. 校验框架语义：检查 `type`、`selector`、`beforeOperate`、`hook`、`vm.super`、`ds_config.name`、`bind_`/`bind_two_`、`bind_on_`、`commands` 和组件属性是否匹配 IIDP 规则。
8. 验证结果：优先运行项目已有 lint/test/build；无法运行时说明应在页面上验证的节点、事件、接口请求和数据变化。

## 输出要求

完成开发或方案说明时，明确给出：

- 修改或建议修改的文件。
- 关键节点 id、选择器和插入位置。
- 新增或复用的数据源名、请求类型和触发方式。
- 关键属性绑定、事件名、commands 名称。
- 使用 hook 时给出 hook 路径、是否调用 `vm.super`、关键 `vm.biz` 读写点。
- 验证方式，以及需要用户在浏览器控制台或页面交互中确认的点。

## 开发偏好

- 优先复用现有 App 的扩展入口、命名、id 前缀、数据源和 commands。
- 先读当前业务 App 的扩展源码和项目文档再改，不读取 `node_modules` 或底座编译产物。
- 对业务需求不完整的情况，先提出最少量关键问题；若能从代码中确定，直接采用现有约定。
- 保持扩展代码聚焦，不顺手重构无关模块。
- 涉及动态节点更新时，避免直接修改未注册的节点树；使用框架提供的更新方式。
- 涉及选择器时，优先使用稳定 id 或 id 选择器规则，避免脆弱的 `_items_` 运行时 id。

## 代码质量规则

生成扩展代码时必须遵守以下规则，适用于 `views`、`common`、`component` 下所有 JS 文件。

### 变量声明

- 禁止使用 `var`。不会重新赋值用 `const`，需要重新赋值用 `let`。
- 同一函数内禁止对同名变量重复声明（如两次 `const steps = ...`），应复用已有变量或使用不同名称。

### 消除重复代码

同一模式出现 2 次以上时，抽取为带参数的工具函数。

工具函数放在扩展文件顶部，导出的扩展对象通过闭包引用这些函数，不要内联在每次回调中。

### 优先使用内置方法

- 查找数组中元素索引：用 `Array.prototype.indexOf`，不手写 `for` 循环。
- 判断数组是否包含某元素：用 `Array.prototype.includes`。
- 查找数组中满足条件的元素：用 `Array.prototype.find`。
- 字符串拼接（尤其是多行 HTML）：用模板字面量 `` ` `` 替代 `+` 拼接。
- 深层属性访问优先使用可选链 `?.`，避免 `&&` 嵌套或直接访问可能为 `undefined` 的中间属性：

```js
// 推荐：可选链 + 默认值
const tableData = vm.biz?.grid?.data?.tableData || [];
const formData = vm.biz?.form?.data?.form || {};

// 不推荐：嵌套 && 或直接链式访问（中间属性可能为 undefined 导致报错）
const tableData =
  vm.biz && vm.biz.grid && vm.biz.grid.data && vm.biz.grid.data.tableData;
const formData = vm.biz.form.data.form; // 若 biz.form 为 undefined 会抛错
```

### HTML 安全

- 动态内容拼接到 HTML 时，必须通过 `escapeHtml` 转义，防止 XSS：

```js
function escapeHtml(str) {
  说;
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}
```

- 纯静态文本或常量值可免转义。
