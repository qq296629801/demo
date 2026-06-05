---
name: iidp-frontend-spec-code
description: Use when generating IIDP frontend code from an IIDP frontend implementation spec document, especially specs produced by iidp-frontend-spec-doc, and writing the result into an existing IIDP frontend project (discovered or user-specified).
---

# IIDP 前端规格到代码生成

## 使用场景

当用户提供或引用 IIDP 前端实现规格文档，并要求“根据规范文档生成 IIDP 前端代码”“把代码写进前端工程”“按规格实现页面/扩展视图（普通节点扩展和扩展钩子）/组件”时使用本 skill。

本 skill 默认承接由 [iidp-frontend-spec-doc](../iidp-frontend-spec-doc/SKILL.md) 生成的规格文档。规格文档是事实来源，不能把待确认项补全成事实。

## 定位与依赖

- 定位：将 IIDP 前端实现规格落到具体工程代码变更（扩展视图〔普通节点扩展和扩展钩子〕/组件/配置），并写入用户指定的 IIDP 前端工程。
- 上游：[iidp-frontend-spec-doc](../iidp-frontend-spec-doc/SKILL.md)（规格事实来源）、[iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)（框架/组件/扩展机制参考）、[iidp-frontend-init](../iidp-frontend-init/SKILL.md)（工程/应用创建与按需加载）、[iidp-frontend-extension-dev](../iidp-frontend-extension-dev/SKILL.md)（扩展视图〔普通节点扩展和扩展钩子〕/数据源/绑定/命令/自定义组件规则）。
- 下游：目标 IIDP 前端工程（通过发现流程或用户指定确定）内的业务应用与扩展代码。

## 必读关联 Skill

- 生成或修改任何前端代码前，必须先读取并执行 [iidp-frontend-codegen-protocol](../iidp-frontend-codegen-protocol.md)。该协议是本 skill 的强制执行标准。
- 查阅 IIDP 前端框架机制、组件属性、扩展协议、工程配置等完整文档，使用 [iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)。
- 需要新建工程、创建扩展应用、配置按需加载或启动项目时，使用 [iidp-frontend-init](../iidp-frontend-init/SKILL.md)。
- 需要写扩展视图（普通节点扩展和扩展钩子）、数据源、绑定、commands 或自定义 Vue2 组件时，使用 [iidp-frontend-extension-dev](../iidp-frontend-extension-dev/SKILL.md)。
- 生成或修改 IIDP 组件节点前，按 extension-dev 的要求先读取 [COMPONENT_RULES_COVERAGE.md](../iidp-frontend-extension-dev/COMPONENT_RULES_COVERAGE.md)，再读取 [COMPONENT_RULES.md](../iidp-frontend-extension-dev/COMPONENT_RULES.md) 或 dev-manual。

## 工程与环境准备（生成代码前必须执行）

在写入任何代码之前，必须先完成 [iidp-frontend-codegen-protocol](../iidp-frontend-codegen-protocol.md) 的实现前门禁。

工程与应用定位统一执行 [iidp-frontend-project-locator](../iidp-frontend-project-locator.md)：

- 未发现 IIDP 前端工程时，停止前端代码生成，并询问用户是否创建工程、指定工程路径或跳过前端代码。
- 需要新建工程或扩展应用时，调用 [iidp-frontend-init](../iidp-frontend-init/SKILL.md)。
- 工程和应用都就绪后，才进入下面的"工作原则"和"规格读取流程"。
- 如果工程或应用创建失败，停止并报告原因，不要继续生成代码。

## 工作原则

- 本 skill 只负责编排“规格文档 → 工程准备 → 扩展开发”。前端代码生成规则不在本文件维护，统一执行 [iidp-frontend-codegen-protocol](../iidp-frontend-codegen-protocol.md)。
- 规格读取后，先用 codegen protocol 判断实现分支、是否需要代码、工程与应用、契约、selector、组件规则和待确认项；任一门禁失败时停止写代码。
- 需要推导标准模板节点 id 时读取 [STANDARD_TEMPLATE_IDS.md](../iidp-frontend-standard-ids/STANDARD_TEMPLATE_IDS.md)；需要组件或框架事实时读取 [iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)。

## 规格读取流程

读取规格文档后，提取并记录：

- 页面名称、页面类型、推荐 IIDP 实现方式、是否需要前端代码。
- 页面结构：搜索区、表格区、工具栏、表单/弹窗、子表/树。
- 字段规格：字段名、控件、必填、默认值、校验、备注。
- 操作与事件：入口、触发条件、前端行为、后端交互、失败处理。
- IIDP 实现建议：标准模板/在线视图、前端扩展、Vue2 组件。
- 接口与数据契约、验收测试、待确认事项。

如果规格缺少目标工程或目标应用目录，先从用户消息中补足；仍缺失时只问最少关键问题。

## 实现分支编排

本 skill 不维护实现分支细则。读取规格文档后，必须按 [iidp-frontend-codegen-protocol](../iidp-frontend-codegen-protocol.md) 的“实现分支规则”判断并执行 A/B/C/C1/D/D1 分支，避免本文件与唯一协议出现规则漂移。

编排要求：

1. 规格判定标准模板/在线视图已满足时，只输出跳过原因和后端/平台配置建议，不写前端工程文件。
2. 规格判定需要扩展视图、扩展钩子、自定义 Vue2 组件或页面级 Vue2 接管时，先执行 protocol 的实现前门禁，再按 [iidp-frontend-extension-dev](../iidp-frontend-extension-dev/SKILL.md) 读取对应机制和组件规则。
3. 规格中的实现分支、selector、契约、节点 id、权限和枚举存在待确认项时，停止代码生成并询问用户，不得补全成事实。
4. 完成实现后，统一按 protocol 的“实现后合规扫描”检查并修复问题。

## 写代码前检查

工程与环境准备已完成后，写入文件前确认：

- 已执行 [iidp-frontend-codegen-protocol](../iidp-frontend-codegen-protocol.md) 的“实现前门禁”。
- 当前需求不只是后端在线视图配置。
- 所需节点 id、接口、模型和枚举足够；不足时不生成会误导的代码。
- 已读取相邻示例文件和入口文件，遵循现有 import/export 风格。

### 代码质量自检

生成代码后、写入文件前，逐项检查：

1. **无 `var`**：所有变量声明使用 `const` 或 `let`。
2. **使用内置方法**：数组查找用 `indexOf`/`includes`/`find`，不手写等价 for 循环；字符串拼接用模板字面量。
3. **文件长度**：预估生成代码超过 400 行时，主动按业务功能拆分为独立扩展文件，由 `views/index.js` 汇总导出。
4. **HTML 安全**：动态内容拼接 HTML 时已做转义处理。
5. **扩展视图 type 与组件 name 一致**：涉及自定义 Vue2 组件时，扩展视图的 `view.type` 必须等于组件 `name` 属性去掉 `tech-` 前缀后的值（kebab-case），**不是** `comps.js` 中的 export 变量名。例如组件声明 `name: 'tech-trace-forward-page'`，则扩展视图 `type` 应为 `'trace-forward-page'`，不是 `'TraceForwardPage'`。
6. **合规扫描**：按 [iidp-frontend-codegen-protocol](../iidp-frontend-codegen-protocol.md) 的“实现后合规扫描”检查请求库、`type: 'page'`、扩展钩子结构、组件注册、selector 来源和禁止目录。

## 验证

完成后优先运行项目已有校验：

- `npm run lint`：改动 JS/Vue/TS 较多时运行。
- `npm run build`：改动入口、注册、组件或构建配置时运行。
- 无法运行时说明原因，并给出页面人工验证点。

最终回复必须包含：

- 修改的文件；如果没有修改，写“无”。
- 使用的实现分支：标准模板/在线视图、扩展视图（普通节点扩展或扩展钩子）或 Vue2 组件。
- 关键节点 id、扩展钩子路径（hook path）、数据源名、事件或 commands。
- 仍需用户确认的接口、模型、节点 id、枚举或权限项。
