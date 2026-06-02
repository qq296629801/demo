---
name: iidp-frontend-spec-code
description: Use when generating IIDP frontend code from an IIDP frontend implementation spec document, especially specs produced by iidp-frontend-spec-doc, and writing the result into an existing IIDP frontend project (discovered or user-specified).
---

# IIDP 前端规格到代码生成

## 使用场景

当用户提供或引用 IIDP 前端实现规格文档，并要求“根据规范文档生成 IIDP 前端代码”“把代码写进前端工程”“按规格实现页面/扩展/hook/组件”时使用本 skill。

本 skill 默认承接由 [iidp-frontend-spec-doc](../iidp-frontend-spec-doc/SKILL.md) 生成的规格文档。规格文档是事实来源，不能把待确认项补全成事实。

## 定位与依赖

- 定位：将 IIDP 前端实现规格落到具体工程代码变更（扩展视图/hook/组件/配置），并写入用户指定的 IIDP 前端工程。
- 上游：[iidp-frontend-spec-doc](../iidp-frontend-spec-doc/SKILL.md)（规格事实来源）、[iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)（框架/组件/扩展机制参考）、[iidp-frontend-init](../iidp-frontend-init/SKILL.md)（工程/应用创建与按需加载）、[iidp-frontend-extension-dev](../iidp-frontend-extension-dev/SKILL.md)（扩展视图/hook/数据源/绑定/命令/自定义组件规则）。
- 下游：目标 IIDP 前端工程（通过发现流程或用户指定确定）内的业务应用与扩展代码。

## 必读关联 Skill

- 查阅 IIDP 前端框架机制、组件属性、扩展协议、工程配置等完整文档，使用 [iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)。
- 需要新建工程、创建扩展应用、配置按需加载或启动项目时，使用 [iidp-frontend-init](../iidp-frontend-init/SKILL.md)。
- 需要写扩展视图、hook、数据源、绑定、commands 或自定义 Vue2 组件时，使用 [iidp-frontend-extension-dev](../iidp-frontend-extension-dev/SKILL.md)。
- 生成或修改 IIDP 组件节点前，按 extension-dev 的要求读取 [COMPONENT_RULES.md](../iidp-frontend-extension-dev/COMPONENT_RULES.md)。

## 工程与环境准备（生成代码前必须执行）

在写入任何代码之前，按顺序检查并准备工程环境：

### 1. 检查前端工程是否存在

- **用户已指定工程路径**：直接使用，验证目录存在。
- **用户未指定工程路径**：按 [iidp-frontend-extension-dev](../iidp-frontend-extension-dev/SKILL.md) 的存量项目定位流程搜索工作区（先探测常见位置，未命中再以深度 2 层搜索），识别标志：
  - `package.json` 含 `init:tech` 脚本
  - `apps/` 目录存在
  - `config/apps.json` 存在
  - `build/webpack.dev.js` 存在
  满足 ≥3 项即视为候选。
- **0 个候选**：按 `iidp-frontend-init` 的流程创建工程（先运行 `tech --help` 确认 `t-cli` 已安装，切换到工程存放目录执行 `tech project <projectName>`）。
- **1 个候选**：记录路径，进入下一步。
- **多个候选**：列出路径、工程名、应用数量，让用户选择。
- **工程已确认**：进入下一步。

### 2. 检查扩展应用是否存在

- 从规格文档或用户消息中提取 `appName`。
- **appName 已获得**：检查 `apps/<appName>/` 目录是否存在。
- **appName 未获得**：扫描 `apps/` 目录（排除 `component`、`base`、以 `dist` 开头的目录、`node_modules`、隐藏目录），列出候选应用及其 `config/app.json` 中的 `effectPaths`，让用户选择。仅 1 个候选时自动使用但需确认。
- **应用不存在**：按 `iidp-frontend-init` 的流程创建应用。
  - 在工程根目录执行 `tech app <appName>`。
  - 创建后如果规格文档提供了 `product`，自动将 `apps/<appName>/config/app.json` 的 `effectPaths.includeRegExp` 配置为 `^/<product>/`；如果没有 `product`，不修改按需加载配置。
- **应用已存在**：进入代码生成阶段。

### 3. 确认后再继续

- 工程和应用都就绪后，才进入下面的"工作原则"和"规格读取流程"。
- 如果工程或应用创建失败，停止并报告原因，不要继续生成代码。

## 工作原则

1. 先判断是否真的需要前端代码。
   - 传统管理后台页面，包含树、搜索、表格、表单、子表等，优先标准模板与在线视图。
   - 在线视图优先在后端工程配置，前端工程通常不写代码。
   - 只有标准模板/在线视图不能覆盖的前端行为，才写 IIDP 前端扩展。
   - 特别复杂且 IIDP 节点难以表达的交互，才写 Vue2 自定义组件。

2. 代码必须写入已确认的 IIDP 前端工程。
   - 工程路径通过发现流程或用户指定确定（不再硬编码默认路径）。
   - 默认只改业务扩展源码：`apps/<appName>/views`、`apps/<appName>/common`、`apps/<appName>/config`、`apps/component`。
   - 不要修改 `node_modules`、`dist`、`distApp`、`distTmp`、`umdComps`、`build` 或编译产物。

3. 不编造平台事实。
   - 未知接口、模型、菜单 id、页面 id、节点 id、数据源名、枚举值、权限码必须保留为待确认。
   - **节点 id 查找优先级**：用户已提供 → 规格文档中的推导 ID → 标准模板规则库（[STANDARD_TEMPLATE_IDS.md](../iidp-frontend-standard-ids/STANDARD_TEMPLATE_IDS.md)）→ 询问用户。
   - 扩展视图或 hook 需要目标节点 id 时，先按优先级查找；均无则停止并询问用户。
   - Element UI 已全局引入，自定义 Vue2 组件中不要单独引入 Element UI。

4. 按需加载配置规则。
   - 规格文档含 `product` 时，创建应用自动配置 `effectPaths.includeRegExp` 为 `^/<product>/`。
   - 无 `product` 时不修改按需加载配置，保留默认占位符。
   - **默认不添加 `effectPageIds`**，仅在用户明确要求按 page id 加载时才配置此项。

## 规格读取流程

读取规格文档后，提取并记录：

- 页面名称、页面类型、推荐 IIDP 实现方式、是否需要前端代码。
- 页面结构：搜索区、表格区、工具栏、表单/弹窗、子表/树。
- 字段规格：字段名、控件、必填、默认值、校验、备注。
- 操作与事件：入口、触发条件、前端行为、后端交互、失败处理。
- IIDP 实现建议：标准模板/在线视图、前端扩展、Vue2 组件。
- 接口与数据契约、验收测试、待确认事项。

如果规格缺少目标工程或目标应用目录，先从用户消息中补足；仍缺失时只问最少关键问题。

## 实现分支

### A. 标准模板/在线视图已满足

不要为了“生成代码”而硬写 Vue 页面、假接口或空扩展。

输出应明确：

- 前端无需新增代码。
- 需要在后端或平台配置的标准模板、在线视图、字段、校验、权限和接口契约。
- 当前前端工程不修改；即使用户说“生成代码写入工程”，只要规格判定无需前端代码，也必须停止代码生成并说明原因。
- 只有用户明确要求沉淀前端侧说明文档时，才询问是否写一份说明文档到前端工程文档目录。

### B. 需要标准页 hook

适用于保存前校验、查询参数调整、权限控制、后端错误提示加工、标准生命周期增强。

实现要求：

- **实现前必须先读取 [`iidp-frontend-extension-dev/SKILL.md`](../iidp-frontend-extension-dev/SKILL.md) §扩展钩子选择章节**，获取完整钩子点列表、return 值规则和 vm.super 调用规范，不得只凭示例推断可用钩子点。
- 业务文件放在 `apps/<appName>/views/<business>/<business>.js`。
- 在 `apps/<appName>/views/index.js` 引入并展开导出。
- hook 挂载目标通常是页面顶级节点，例如 `<pageId>_container_main`，但必须来自用户或现有代码证据。
- 保留平台原逻辑时调用 `vm.super['hook.path'](vm, params, options)`。
- 刷新表格数据时使用 `vm.biz.grid.methods.runRefresh()`，不要调用 `vm.biz.grid.baseMethods.query()`。

### C. 需要扩展视图

适用于插入按钮、调整节点属性、补充容器、表格列、弹窗、局部布局等。

实现要求：

- **实现前必须先读取 [`iidp-frontend-extension-dev/SKILL.md`](../iidp-frontend-extension-dev/SKILL.md) §扩展开发协议章节**，获取完整扩展类型规则（selector、beforeOperate、命名唯一性、权重顺序、merge 的 items 行为、replace 使用限制等）。
- 先确认目标节点 id 和插入/合并位置。
- 新增节点配置稳定业务 id。
- 新增组件节点前读取组件规则，不使用通用 Vue/Element 写法猜 IIDP 属性。
- 根据语义选择 `before`、`after`、`append`、`unshift`、`merge`、`replace`、`delete` 或 `custom`。
- 复杂复用逻辑抽普通 JS 方法，避免复制多个事件函数。

### C1. 完全替换标准模板页面

当规格要求**整个页面重做**（非局部调整）时：

- **首选 `replace` 替换主表格页节点**，用新的 `view` 完全接管标准页内容。
- **禁止使用 `type: 'page'`** 创建自定义页面，除非规格文档明确要求新增独立路由页面且给出了理由。`type: 'page'` 仅用于从零新建路由级别页面，不用于替换现有标准页。
- **规格文档中已明确指定扩展类型为 `replace` 时，必须严格按规格执行，不得改为 `type: 'page'`。** 即使页面完全由自定义 Vue2 组件构成，也应该通过 `replace` 将自定义组件嵌入到标准模板页面中，而非创建独立页面。

### D. 需要自定义 Vue2 组件

适用于完整内部状态管理、复杂交互、IIDP 节点难以表达的 UI。

实现要求：

- 使用 Vue 2.7 兼容写法。
- 组件放入现有项目约定的 `apps/component` 或业务应用组件目录。
- **多个组件时必须按页面/业务模块建立文件夹层级**，禁止将所有组件平铺在 `apps/component` 根目录下。例如 `apps/component/order-manage/OrderManage.vue`、`apps/component/order-manage/OrderTable.vue`。
- 单个简单组件可直接放在 `apps/component` 根目录。
- **父组件必须 import 子组件并在 `components` 中注册**，禁止在 template 中使用未引入的子组件。
- 在应用的 `common/comps.js` 或既有注册入口注册。
- 不单独引入 Element UI。
- 组件只承担前端复杂交互；核心业务规则、唯一性、状态流转优先由后端或标准能力承担。

## 写代码前检查

工程与环境准备已完成后，写入文件前确认：

- 当前需求不只是后端在线视图配置。
- 所需节点 id、接口、模型和枚举足够；不足时不生成会误导的代码。
- 已读取相邻示例文件和入口文件，遵循现有 import/export 风格。

### 代码质量自检

生成代码后、写入文件前，逐项检查：

1. **无 `var`**：所有变量声明使用 `const` 或 `let`。
2. **无重复代码**：同一逻辑模式出现不超过 1 次。重复出现 2 次以上的代码必须抽取为工具函数。
3. **使用内置方法**：数组查找用 `indexOf`/`includes`/`find`，不手写等价 for 循环；字符串拼接用模板字面量。
4. **文件长度**：预估生成代码超过 400 行时，主动按业务功能拆分为独立扩展文件，由 `views/index.js` 汇总导出。
5. **HTML 安全**：动态内容拼接 HTML 时已做转义处理。
6. **扩展视图 type 与组件 name 一致**：涉及自定义 Vue2 组件时，扩展视图的 `view.type` 必须等于组件 `name` 属性去掉 `tech-` 前缀后的值（kebab-case），**不是** `comps.js` 中的 export 变量名。例如组件声明 `name: 'tech-trace-forward-page'`，则扩展视图 `type` 应为 `'trace-forward-page'`，不是 `'TraceForwardPage'`。

## 验证

完成后优先运行项目已有校验：

- `npm run lint`：改动 JS/Vue/TS 较多时运行。
- `npm run build`：改动入口、注册、组件或构建配置时运行。
- 无法运行时说明原因，并给出页面人工验证点。

最终回复必须包含：

- 修改的文件；如果没有修改，写“无”。
- 使用的实现分支：标准模板/在线视图、hook、扩展视图或 Vue2 组件。
- 关键节点 id、hook 路径、数据源名、事件或 commands。
- 仍需用户确认的接口、模型、节点 id、枚举或权限项。
