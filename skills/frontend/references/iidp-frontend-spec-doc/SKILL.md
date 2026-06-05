---
name: iidp-frontend-spec-doc
description: 将业务需求、用户故事和场景整理为 IIDP 前端实现规格文档。若规格生成后自动衔接代码生成，必须先执行 iidp-frontend-codegen-protocol 与 project-locator 门禁。Use when the user asks to generate, rewrite, refine, or standardize requirement/spec documents for IIDP frontend code generation, especially for WMS, management backend pages, standard templates, online views, extension views including extension hooks, or custom Vue2 components.
---

# IIDP 前端规格文档生成

> **注意**：需要查阅 IIDP 前端框架、组件、扩展机制等开发文档时，调用 [iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)，不要直接遍历 `iidpDoc/` 原始文档目录。
> **代码衔接门禁**：本 skill 只生成规格文档时不写前端工程文件；如果规格生成后需要自动衔接代码生成，必须先读取并执行 [iidp-frontend-codegen-protocol](../iidp-frontend-codegen-protocol.md)，并按 [iidp-frontend-project-locator](../iidp-frontend-project-locator.md) 完成工程与应用定位。

## 定位与依赖

- 定位：将业务需求、用户故事和场景整理为可落地的 IIDP 前端实现规格文档，作为后续代码生成与人工评审依据。
- 上游：[iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)（框架机制、组件与扩展协议的事实参考）。
- 下游：[iidp-frontend-codegen-protocol](../iidp-frontend-codegen-protocol.md)（代码衔接强制门禁）、[iidp-frontend-project-locator](../iidp-frontend-project-locator.md)（工程与应用定位）、[iidp-frontend-spec-code](../iidp-frontend-spec-code/SKILL.md)（按规格生成/修改 IIDP 前端工程代码）。
- 模板：[frontend-spec-template.md](frontend-spec-template.md)。

## 使用场景

当用户提供业务需求、用户故事、验收标准、场景或页面描述，并希望后续让 AI 依据文档生成 IIDP 前端代码时使用本 skill。

生成的规格文档必须满足：

- 可被人继续改造调整。
- 可作为后续 AI 生成 IIDP 前端代码的依据。
- 明确区分后端配置、前端扩展和自定义 Vue2 组件的边界。
- 对传统管理后台需求优先使用 IIDP 标准模板与在线视图。

## 需求分析流程

1. 抽取需求要素。
   - 用户角色、业务目标、优先级、前置条件、后置条件。
   - 页面、按钮、表格、搜索、表单、子表、弹窗、树等 UI 要素。
   - 字段、校验、默认值、状态流转、接口或后端能力。
   - 独立测试点、验收标准和异常场景。

2. 判断 IIDP 实现方式。
   - 传统管理后台页面，包含树、搜索、表格、表单、子表等：优先配置标准模板与在线视图，视图优先在后端工程配置，前端工程通常不处理。
   - 标准模板和在线视图不能满足，但仍属于 IIDP 视图能力范围：使用 IIDP 前端扩展，补充扩展视图（普通节点扩展和扩展钩子）、事件、数据源或 commands。
   - 复杂视图配置需要放到前端工程：新建扩展应用，并在前端维护后端视图配置。
   - 特别复杂、IIDP 节点和扩展难以表达的交互：使用自定义 Vue2 组件；Element UI 已全局引入，不要单独引入。

3. 明确生成边界。
   - 若需求只需要后端在线视图配置，文档要说明“前端无需新增代码”，但仍给出视图配置建议和字段协议。
   - 若需要前端扩展，必须列出目标应用、目标页面、目标节点 id 或需要用户补充的节点 id。
   - 不要编造接口、模型、节点 id、菜单 id、数据源名；未知项写成“待确认”。

## 输出文档格式

默认输出 Markdown 文档，建议文件名：

`docs/specs/YYYY-MM-DD-<业务名>-iidp-frontend-spec.md`

生成文档时使用 [frontend-spec-template.md](frontend-spec-template.md) 的结构。不要把模板当作固定提示词逐字照抄，应该根据业务需求补充字段、操作、状态规则、接口契约和待确认事项。

## IIDP 判断规则

优先级从上到下判断：

1. 能用标准模板和在线视图完成的，不生成前端扩展代码规格。
2. 只需要调整标准页查询、保存、校验、权限、按钮行为的，优先建议扩展视图中的扩展钩子。
3. 只需要插入、合并、替换节点结构的，建议扩展视图中的普通节点扩展。
4. 需要复杂交互但仍能用 IIDP 节点表达的，建议前端扩展应用维护扩展视图。
5. IIDP 节点难以表达或需要完整组件内部状态管理的，才建议自定义 Vue2 组件。

## 输出原则

- 用业务语言写清“要做什么”，用 IIDP 语言写清“怎么落地”。
- 对 AI 后续生成代码有用的信息必须结构化：字段表、操作表、状态规则、接口契约、待确认事项。
- 不确定的信息不要补全成事实，写入“待确认事项”。
- 避免把所有需求都推向自定义组件；标准模板和在线视图优先。
- 文档内容要适合人工继续编辑，不要输出一次性提示词。

## 生成后续：自动衔接代码生成

规格文档生成完成后，根据文档中“是否需要前端代码”的判定结果：

- **需要前端代码** → **自动衔接** [iidp-frontend-spec-code](../iidp-frontend-spec-code/SKILL.md) 进行代码生成，**不需要用户再次发起对话**。衔接前必须按下方门禁完成目标工程和应用定位。
- **无需前端代码** → 输出规格文档并明确说明“前端无需新增代码”，等待用户后续指令。

不要在生成规格文档后就停下来等用户说“生成代码”。

自动衔接前必须完成：

1. 读取并执行 [iidp-frontend-codegen-protocol](../iidp-frontend-codegen-protocol.md) 的实现前门禁。
2. 按 [iidp-frontend-project-locator](../iidp-frontend-project-locator.md) 验证或发现 IIDP 前端工程和 `apps/<appName>`。
3. 未发现工程、应用或关键契约缺失时，停止代码生成并询问用户，不得创建临时目录或猜测路径。
