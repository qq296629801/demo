---
name: iidp-frontend
description: IIDP 前端开发总入口。根据用户任务自动路由到对应子技能：规格文档生成、工程初始化、扩展开发、代码生成、开发手册查阅。Use when the user asks about IIDP frontend development, including generating spec docs, writing frontend code, developing extensions, initializing projects, or consulting framework references.
---

# IIDP 前端开发

本 skill 是 IIDP 前端开发的总入口，负责根据用户意图路由到对应的子技能。

## 子技能索引

| 子技能                                                                         | 定位            | 何时使用                                                                                |
| ------------------------------------------------------------------------------ | --------------- | --------------------------------------------------------------------------------------- |
| [iidp-frontend-standard-ids](references/iidp-frontend-standard-ids/SKILL.md)   | 节点 ID 规则库  | 查阅标准模板节点 ID 命名规则，为扩展开发提供准确的节点 ID 参考                          |
| [iidp-frontend-spec-doc](references/iidp-frontend-spec-doc/SKILL.md)           | 需求 → 规格文档 | 用户提供业务需求、用户故事、场景描述，需要生成 IIDP 前端实现规格文档                    |
| [iidp-frontend-init](references/iidp-frontend-init/SKILL.md)                   | 工程初始化      | 创建 IIDP 前端工程、创建扩展应用、安装依赖、配置按需加载、本地启动                      |
| [iidp-frontend-extension-dev](references/iidp-frontend-extension-dev/SKILL.md) | 扩展开发        | 在已有工程和应用中开发业务功能：扩展视图、hook、数据源、事件绑定、自定义组件            |
| [iidp-frontend-spec-code](references/iidp-frontend-spec-code/SKILL.md)         | 规格文档 → 代码 | 已有规格文档，需要按文档生成/修改 IIDP 前端工程代码（内部会调用 init 和 extension-dev） |
| [iidp-frontend-dev-manual](references/iidp-frontend-dev-manual/SKILL.md)       | 框架文档参考    | 查阅 IIDP 前端框架机制、组件属性、扩展协议、工程配置等文档（其他子技能的事实来源）      |

## 工作流概览

```
需求 → [spec-doc] → 规格文档 → [init] → 工程就绪 → [extension-dev] → 工程代码
                                              ↓
                              存量项目：[发现工程 → 确认 → 定位 app → 读取模式] → [extension-dev]
                                       [dev-manual]（全程提供文档参考）
```

- [spec-code](references/iidp-frontend-spec-code/SKILL.md) 是 [init](references/iidp-frontend-init/SKILL.md) + [extension-dev](references/iidp-frontend-extension-dev/SKILL.md) 的编排层，按规格文档自动完成工程准备和代码生成。

## 路由规则

1. **用户只提供了文件路径/文档引用，没有说明意图** → 先读取文件，根据内容自动判断：
   - 内容是 IIDP 前端规格文档（包含页面结构、字段规格、IIDP 实现建议等章节）→ 按 [iidp-frontend-spec-code](references/iidp-frontend-spec-code/SKILL.md) 流程生成代码（需确认目标工程和应用）。
   - 内容是需求/业务描述（用户故事、场景说明、功能清单等）→ 按 [iidp-frontend-spec-doc](references/iidp-frontend-spec-doc/SKILL.md) 流程先生成规格文档。
   - 内容无法识别 → 询问用户想对文件做什么。
2. **用户要做新需求但还没有规格文档** → 先调用 [iidp-frontend-spec-doc](references/iidp-frontend-spec-doc/SKILL.md) 生成规格，**生成完成后自动判断是否需要前端代码**：
   - 规格判定需要前端代码 → **自动衔接** [iidp-frontend-spec-code](references/iidp-frontend-spec-code/SKILL.md) 生成代码（需确认工程位置、工程名、应用名），无需用户再次发起对话。
   - 规格判定无需前端代码 → 输出规格文档并说明原因，等待用户后续指令。
3. **用户已有规格文档要写代码** → 调用 [iidp-frontend-spec-code](references/iidp-frontend-spec-code/SKILL.md)（它会自动编排 init + extension-dev）。
4. **用户要创建工程/应用/安装依赖/启动项目** → 调用 [iidp-frontend-init](references/iidp-frontend-init/SKILL.md)。
5. **用户要在现有应用中加功能（有明确目标节点/页面）** → 调用 [iidp-frontend-extension-dev](references/iidp-frontend-extension-dev/SKILL.md)。如果工程路径或 appName 未明确，先执行其存量项目定位流程。
5a. **用户要在现有项目中加功能，但未明确工程位置或目标应用** → 调用 [iidp-frontend-extension-dev](references/iidp-frontend-extension-dev/SKILL.md)，先执行存量项目定位流程（发现工程 → 确认 → 定位 app → 读取现有模式），定位完成后再进入正常扩展开发。
6. **用户问框架怎么用、组件有哪些属性、扩展协议是什么** → 调用 [iidp-frontend-dev-manual](references/iidp-frontend-dev-manual/SKILL.md)。
7. **用户要查阅标准模板节点 ID 命名规则** → 调用 [iidp-frontend-standard-ids](references/iidp-frontend-standard-ids/SKILL.md)。
8. **用户需求跨越多个阶段** → 按工作流顺序依次调用对应子技能。

## 技能调用约定

当用户询问 IIDP 前端相关问题或知识时，**调用 `iidp-frontend-extension-dev` 技能**来获取专业指导，而不是仅凭通用知识回答。该技能文档包含完整的扩展开发规范和参考文档索引。

## 核心原则

- 传统管理后台页面优先使用 IIDP 标准模板与在线视图，前端不新增代码。
- 需要前端介入时，优先 hook → 扩展视图 → 自定义 Vue2 组件。
- **完全替换标准模板页面时，禁止使用 `type: 'page'`，必须使用 `replace` 替换主表格页节点。** 即使页面完全由自定义 Vue2 组件构成，也通过 `replace` 嵌入标准模板页。`type: 'page'` 仅用于新增独立路由页面。
- 不确定的接口、节点 id、模型名必须保留为"待确认"，不编造平台事实。
- **API 请求统一走 IIDP 数据源或 `window.Tech.httpMeta`**，禁止自行引入 axios、fetch 等请求库或手写 HTTP 封装。
- 按需加载配置规则：规格文档含 `product` 时，创建应用自动配置 `effectPaths.includeRegExp` 为 `^/<product>/`；无 `product` 时不修改默认配置（保留占位符）。
- **默认不添加 `effectPageIds`**，仅在用户明确要求时配置。
- 代码注释和日志输出（`console.log`/`console.warn`/`console.error` 等）统一使用中文。
- 变量声明禁止使用 `var`，统一使用 `const`（不会重新赋值时）或 `let`（需要重新赋值时）。
- 代码书写方式遵循更好理解、更好性能的原则：
  - 优先使用内置方法（如 `Array.prototype.indexOf`、`includes`、`find`），不手写等价循环。
  - 优先使用 ES6+ 语法：模板字面量替代字符串拼接、可选链 `?.` 替代 `&&` 嵌套访问深层属性（如 `vm.biz?.grid?.data?.tableData`）、解构赋值、箭头函数（在不涉及 `this` 绑定的工具函数中）。
- 禁止重复代码。同一模式出现 3 次以上时，抽取为带参数的工具函数或工厂函数。
- 所有子技能都通过 [iidp-frontend-dev-manual](references/iidp-frontend-dev-manual/SKILL.md) 获取框架文档事实。
