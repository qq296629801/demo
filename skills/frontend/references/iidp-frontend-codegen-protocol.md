---
name: iidp-frontend-codegen-protocol
description: IIDP 前端代码生成强制协议。任何入口只要会写入或修改 IIDP 前端代码、文件或配置，都必须先执行本协议的实现前门禁，并在完成后执行合规扫描。
---

# IIDP 前端代码生成协议

本文是 IIDP 前端代码生成的唯一强制协议。任何入口只要最终会写入前端代码、文件或配置，都必须先执行本文；包括 `iidp-frontend`、`iidp-frontend-spec-doc` 自动衔接代码生成、`iidp-frontend-spec-code`、`iidp-frontend-extension-dev`、`create-project /sdd-implement` 和 `iidp-brownfield`。

## 目标

- 不管从哪个 skill 切入，最终写出的前端代码都符合 IIDP 前端标准。
- 把分散在各 skill 中的“必须/禁止/优先级”统一为一套可执行门禁。
- 降低 AI 因通用 Vue/Element UI 经验、示例代码或不完整规格产生幻觉的概率。

## 适用范围

写入或修改以下目录前必须执行：

- `apps/<appName>/views`
- `apps/<appName>/common`
- `apps/<appName>/config`
- `apps/component`

标准模板/在线视图已满足且 SDD §9 或非 SDD 事实清单明确“前端无需新增代码”时，不写前端代码，只记录跳过原因。

## 必读输入

进入代码生成前，必须按当前入口读取并提取事实。

### SDD 规格驱动场景

当任务来自 `/sdd-implement`、`iidp-brownfield`、`iidp-frontend-spec-code` 或明确给出 feature 规格目录时，必须读取：

| 输入 | 目的 |
|---|---|
| `frontend-spec.md` | 读取 §9 实现分支、目标应用、节点树、selector、数据源、绑定和事件 |
| `contracts.md` | 核对 app、model、service、args、权限码、视图 key、菜单 key |
| `interaction-spec.md`（如存在） | 核对用户流程、交互状态、响应式和可访问性验收 |
| `skills/frontend/SKILL.md` | 前端总入口和工程门禁 |
| `skills/frontend/references/iidp-frontend-project-locator.md` | 工程发现、扩展应用定位、现有模式读取 |
| `skills/frontend/references/iidp-frontend-extension-dev/SKILL.md` | 扩展协议（普通节点扩展和扩展钩子）、selector、vm.biz、vm.super |
| `skills/frontend/references/iidp-frontend-extension-dev/COMPONENT_RULES_COVERAGE.md` | 已覆盖组件索引和未覆盖组件处理规则 |
| `skills/frontend/references/iidp-frontend-extension-dev/COMPONENT_RULES.md` | 组件最小协议和易错点 |

### 非 SDD 存量扩展场景

当用户直接要求修改已有 IIDP 前端工程，但没有 `frontend-spec.md` / `contracts.md` 时，必须先建立本次实现事实清单，再写代码：

- 目标前端工程、`appName`、目标页面或视图文件。
- 实现分支：扩展视图（普通节点扩展或扩展钩子）、自定义 Vue2 组件，或确认标准模板/在线视图已满足而无需代码。
- selector / 节点 id 的来源：用户提供、现有代码确认、标准模板规则库、浏览器验证或已记录事实。
- 请求事实：app、model、service、args、权限码、数据源名；没有来源时保持待确认。
- 组件事实：先读 `COMPONENT_RULES_COVERAGE.md`，再读 `COMPONENT_RULES.md` 或 dev-manual。

缺少会影响代码正确性的事实时，暂停实现并向用户确认；不得用页面文案、模型名、示例代码或通用 Element UI 经验补齐。

需要推导标准模板节点 id 时，还必须读取：

```text
skills/frontend/references/iidp-frontend-standard-ids/STANDARD_TEMPLATE_IDS.md
```

需要组件或框架事实时，通过：

```text
skills/frontend/references/iidp-frontend-dev-manual/SKILL.md
```

## 实现前门禁

任一项失败时，停止写代码，先修正规格、契约或待确认项。

| 门禁 | 通过条件 | 失败处理 |
|---|---|---|
| 实现分支 | SDD 场景由 `frontend-spec.md` §9 明确；非 SDD 场景由已确认事实清单明确标准模板/在线视图、扩展视图（普通节点扩展或扩展钩子）或自定义 Vue2 组件 | 补全 §9 或事实清单 |
| 是否需要代码 | SDD 场景由 §9.1 判断；非 SDD 场景由已确认事实判断。标准模板/在线视图满足时，前端不新增代码 | 记录“前端无需新增代码”和原因，不写文件 |
| 工程与应用 | 已确认 IIDP 前端工程和 `apps/<appName>` | 执行 `iidp-frontend-project-locator` 或 init 流程 |
| 契约完整 | SDD 场景请求 app、model、service、args、权限码来自 `contracts.md`；非 SDD 场景来自用户、现有代码或已确认事实 | 先补全契约或事实清单 |
| selector 来源 | selector 目标 id 来自用户、规格、标准模板规则库或已确认事实 | 标记待确认并暂停实现 |
| 组件规则 | 新增/修改组件节点前已核对 `COMPONENT_RULES_COVERAGE.md` 和 `COMPONENT_RULES.md`，未覆盖组件已查 dev-manual | 不得套用通用 Vue/Element 属性 |
| 页面级 Vue2 接管 | 如果页面 80% 以上功能需要自定义 Vue2 组件实现，必须选择页面级 Vue2 接管，不得混搭标准模板局部能力 | 将实现分支调整为页面级 Vue2 组件，或补充事实证明未达到阈值 |
| 待确认项 | 影响代码正确性的节点 id、接口、模型、枚举、权限码不再是“待确认” | 保持待确认，不写误导性代码 |

## 实现分支规则

### A. 标准模板/在线视图

- 不写前端扩展、Vue 页面、空组件或假接口。
- 输出跳过原因：标准模板/在线视图满足哪些主流程。
- 需要修改的内容应落在后端视图、菜单、权限或平台配置中。

### B. 扩展视图：扩展钩子

- 扩展钩子必须写成 `selector` + `type: "merge"` + `hook` 结构。
- selector 通常命中页面顶级节点，例如 `{idPre}container_main`，但必须有来源。
- 所有 hook 方法必须 return 数据：
  - `before*` 返回 `params`
  - `after*` 返回结果数据
  - `can*` 返回 boolean
  - 调用 `vm.super[...]` 时必须 return 其返回值
- 禁止直接以节点 id 作为导出对象 key。
- 刷新主表格优先使用 `vm.biz.grid.methods.runRefresh()`，不要编造 `vm.biz` 路径。

### C. 扩展视图：普通节点扩展

- 扩展对象必须包含全局唯一 key、`selector`、`type`，以及必要的 `view` 或 `beforeOperate`。
- `type` 只能使用扩展协议支持值：`before`、`after`、`append`、`unshift`、`merge`、`replace`、`delete`、`custom`。
- 新增节点建议配置稳定业务 id。
- 组件属性必须先按 `COMPONENT_RULES_COVERAGE.md` 判断覆盖状态，再来自 `COMPONENT_RULES.md` 或 dev-manual；未确认属性放入待确认，不猜。
- 表格操作列按钮事件使用 `bind_on_clickOptionBtn`，不要和普通按钮的 `bind_on_click` 混用。
- IIDP `button` 节点按钮文本使用 `value`，不要使用 `text`。

### C1. 完全替换标准模板页

- 首选 `replace` 替换表格页视图根节点 `table_main_wrap`。
- 禁止 replace `page` 节点或 `container_main`。
- 禁止使用 `type: 'page'` / `type: "page"` 替换现有标准页。
- `type: 'page'` 仅允许在“新增独立路由页面”且规格明确给出理由时使用。

### D. 自定义 Vue2 组件

- 仅在 IIDP 节点和扩展视图无法表达复杂交互时使用。
- 使用 Vue2 / Vue 2.7 兼容写法，不使用 Vue3 `defineComponent` 或 Composition API。
- 组件 `name` 必须稳定并以 `tech-` 开头。
- 扩展视图中的 `view.type` 必须等于组件 `name` 去掉 `tech-` 后的 kebab-case 值，不是 `comps.js` 的 import/export 变量名。
- 组件注册只在 `apps/<appName>/common/comps.js` 中完成。
- `apps/<appName>/views/**/*.js` 禁止直接 import `.vue` 文件。
- 多个 Vue 组件必须按页面/业务模块放入子目录，禁止全部平铺在 `apps/component` 根目录。
- 父组件在 template 中使用子组件时，必须在 script 中 import 并在 `components` 注册。
- 自定义 Vue 组件内调用接口使用 `window.Tech.httpMeta`，不要引入 axios、fetch 或其他请求库。
- Element UI 已由 IIDP 前端工程全局引入，自定义 Vue2 组件中不要单独引入 Element UI。

### D1. 页面级 Vue2 组件接管

- 如果一个页面 80% 以上功能需要自定义 Vue2 组件实现，必须采用页面级 Vue2 组件整体接管。
- 页面级 Vue2 接管后，搜索、主体、详情、弹窗等页面内核心功能应由同一个页面级 Vue2 组件体系统一承接，不要混搭“标准模板搜索区 + Vue2 主体”等局部方案。
- 已有标准页仍通过扩展视图 `replace` 嵌入页面级 Vue2 组件，优先替换合适的标准页内部根节点，例如 `table_main_wrap`。
- 页面级 Vue2 接管不等于默认使用 `type: 'page'`；`type: 'page'` 仍仅允许在新增独立路由页面且规格明确给出理由时使用。
- 平台外壳、菜单加载、权限入口和扩展挂载机制不属于页面内部功能混搭，可继续使用 IIDP 标准能力。

## 通用代码质量与配置规则

- 代码注释和日志输出（`console.log` / `console.warn` / `console.error`）使用中文。
- 变量声明禁止 `var`，统一使用 `const`（不重新赋值时）或 `let`（需要重新赋值时）。
- 规格文档含 `product` 时，创建应用自动配置 `effectPaths.includeRegExp` 为 `^/<product>/`；无 `product` 时不修改默认按需加载配置。
- 默认不添加 `effectPageIds`，仅在用户明确要求按 page id 加载时配置。

## 事实来源矩阵

| 信息 | 允许来源 | 禁止 |
|---|---|---|
| appName / appPkg / frontend app | `contracts.md`、`frontend-spec.md`、用户确认 | 自行命名 |
| model / service / args | `contracts.md`、用户确认、现有代码中的已确认数据源或接口配置 | 按按钮文案或经验猜 |
| 权限码 | `integration-map.md`、`contracts.md` | 前端自行定义 |
| selector / 节点 id | 用户提供、标准模板规则库、现有确认事实、浏览器验证 | 按菜单名、按钮文案、模型名拼接 |
| 组件属性和事件 | `COMPONENT_RULES_COVERAGE.md`、`COMPONENT_RULES.md`、dev-manual | 套 Element UI/HTML 通用属性 |
| 自定义组件 type | Vue 组件 `name` 去掉 `tech-` | `comps.js` export 变量名 |
| HTTP 请求 | IIDP 数据源、`window.Tech.httpMeta` | axios、fetch、XMLHttpRequest、手写 HTTP 封装 |
| 按需加载配置 | `frontend-spec.md`、用户明确要求、现有应用配置 | 无依据修改 `effectPaths` 或默认添加 `effectPageIds` |

## 实现后合规扫描

前端任务完成后、勾选 `tasks.md` 前必须执行。任一失败项都必须修复或回到待确认，不能只在最终回复中说明。

| 检查项 | 失败条件 |
|---|---|
| 实现分支一致 | 实际改动与 `frontend-spec.md` §9 或非 SDD 事实清单不一致 |
| 通用请求库 | 出现 `import axios`、`from "axios"`、`require('axios')`、`fetch(`、`XMLHttpRequest` |
| 标准页替换 | 替换现有标准页时出现 `type: 'page'` 或 `type: "page"` |
| 扩展钩子结构 | 扩展钩子缺少 `selector`、`type: "merge"` 或 `hook` |
| 扩展钩子返回值 | 扩展钩子未按 before/after/can/super 规则 return |
| Vue import 边界 | `apps/<appName>/views/**/*.js` 直接 import `.vue` |
| 组件注册 | 自定义 Vue 组件未在 `common/comps.js` 注册却被视图使用 |
| 组件命名 | 组件 `name` 未以 `tech-` 开头，或视图 `type` 与组件名不匹配 |
| 组件目录 | 多组件平铺在 `apps/component` 根目录 |
| 页面级 Vue2 混搭 | 页面 80% 以上功能由自定义 Vue2 实现时，仍保留标准模板搜索区、表格区、详情区等页面内部局部能力混搭 |
| 组件属性 | 组件属性没有规则来源，且未放入 `ATTRS`/`ONS` 或待确认 |
| Element UI 引入 | 自定义 Vue2 组件中单独 import Element UI |
| 按钮文本 | IIDP `button` 节点用 `text` 表示按钮文本 |
| 操作列事件 | 表格操作列按钮使用普通 `bind_on_click` |
| selector 来源 | selector id 无来源说明或自行拼接 |
| 待确认项 | 待确认接口、模型、节点 id、枚举或权限码被实现成具体事实 |
| 禁止目录 | 修改了 `node_modules`、`dist`、`distApp`、`distTmp`、`umdComps`、`build` 或编译产物 |
| 变量声明 | 新增 JS 中出现 `var` |
| 代码质量 | 日志/注释未按中文要求书写 |
| 按需加载 | 无依据修改默认 `effectPaths`，或用户未明确要求时新增 `effectPageIds` |

## 输出要求

完成前端实现或跳过前端代码时，必须输出：

- 使用的实现分支：标准模板/在线视图、扩展视图（普通节点扩展或扩展钩子）或自定义 Vue2 组件。
- 修改文件；无修改时写“无”并说明跳过原因。
- 关键 selector、节点 id、扩展钩子路径（hook path）、数据源名、事件名、commands 名称。
- 合规扫描结果。
- 仍需用户确认的接口、模型、节点 id、枚举或权限项。

## 与验证流程的关系

`npm run init:tech` 和 `npm run start` 只能证明工程可初始化和启动，不能替代 IIDP 前端合规检查。`/sdd-validate frontend` 必须先执行本文的合规扫描，再执行运行验收。
