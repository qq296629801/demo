---
name: iidp-frontend
description: IIDP 前端开发总入口。根据用户任务自动路由到对应子技能：规格文档生成、工程初始化、扩展开发、代码生成、开发手册查阅。**强制前置门禁：生成任何前端代码前必须检查前端工程是否存在，不存在则停止并询问用户是否需要创建工程，绝对禁止在无工程的情况下直接生成前端代码文件。** Use when the user asks about IIDP frontend development, including generating spec docs, writing frontend code, developing extensions, initializing projects, or consulting framework references.
---

# IIDP 前端开发

本 skill 是 IIDP 前端开发的总入口，负责根据用户意图路由到对应的子技能。

维护说明见 [README.md](README.md)。入口只负责路由和强制协议加载，不维护独立代码生成规则副本。

## 🚨 前置门禁：工程存在性检查（MANDATORY — 不可绕过）

**在生成任何前端代码、文件、脚本之前，必须执行此检查。违反即为流程违规。**

1. 先执行 [iidp-frontend-codegen-protocol](references/iidp-frontend-codegen-protocol.md) 的实现前门禁。
2. 工程与应用定位统一按 [iidp-frontend-project-locator](references/iidp-frontend-project-locator.md) 执行。
3. 未发现 IIDP 前端工程时，停止前端代码生成，并询问用户是否创建工程、指定工程路径或跳过前端代码。
4. 在用户确认前，禁止创建任何前端文件或目录。
5. 此门禁适用于所有前端代码生成场景。唯一例外是用户明确说“只生成后端代码”。

## 子技能索引

| 子技能                                                                         | 定位            | 何时使用                                                                                |
| ------------------------------------------------------------------------------ | --------------- | --------------------------------------------------------------------------------------- |
| [iidp-frontend-standard-ids](references/iidp-frontend-standard-ids/SKILL.md)   | 节点 ID 规则库  | 查阅标准模板节点 ID 命名规则，为扩展开发提供准确的节点 ID 参考                          |
| [iidp-frontend-spec-doc](references/iidp-frontend-spec-doc/SKILL.md)           | 需求 → 规格文档 | 用户提供业务需求、用户故事、场景描述，需要生成 IIDP 前端实现规格文档                    |
| [iidp-frontend-init](references/iidp-frontend-init/SKILL.md)                   | 工程初始化      | 创建 IIDP 前端工程、创建扩展应用、安装依赖、配置按需加载、本地启动                      |
| [iidp-frontend-extension-dev](references/iidp-frontend-extension-dev/SKILL.md) | 扩展开发        | 在已有工程和应用中开发业务功能：扩展视图（普通节点扩展和扩展钩子）、数据源、事件绑定、自定义组件 |
| [iidp-frontend-spec-code](references/iidp-frontend-spec-code/SKILL.md)         | 规格文档 → 代码 | 已有规格文档，需要按文档生成/修改 IIDP 前端工程代码（内部会调用 init 和 extension-dev） |
| [iidp-frontend-dev-manual](references/iidp-frontend-dev-manual/SKILL.md)       | 框架文档参考    | 查阅 IIDP 前端框架机制、组件属性、扩展协议、工程配置等文档（其他子技能的事实来源）      |

## 共享参考

| 参考文档 | 定位 |
|---|---|
| [iidp-frontend-codegen-protocol](references/iidp-frontend-codegen-protocol.md) | 代码生成唯一强制协议 |
| [iidp-frontend-project-locator](references/iidp-frontend-project-locator.md) | 工程发现、应用定位、读取现有模式 |

## 强制代码生成协议

所有前端代码生成、文件写入或配置修改场景，在进入具体子技能前必须先读取并执行 [iidp-frontend-codegen-protocol](references/iidp-frontend-codegen-protocol.md)。

该协议是前端代码生成的唯一强制标准，覆盖：

- 实现分支判断：标准模板/在线视图、扩展视图（普通节点扩展和扩展钩子）、自定义 Vue2 组件。
- 实现前门禁：工程、应用、契约、selector、组件规则和待确认项。
- 分支生成规则：扩展视图（普通节点扩展、扩展钩子）、标准页替换、自定义组件。
- 实现后合规扫描：请求库、`type: 'page'`、扩展钩子结构、组件注册、selector 来源等。

## 工作流概览

```
需求 → [spec-doc] → 规格文档 → [init] → 工程就绪 → [extension-dev] → 工程代码
                                              ↓
                              存量项目：[发现工程 → 确认 → 定位 app → 读取模式] → [extension-dev]
                                       [dev-manual]（全程提供文档参考）
```

- [spec-code](references/iidp-frontend-spec-code/SKILL.md) 是 [init](references/iidp-frontend-init/SKILL.md) + [extension-dev](references/iidp-frontend-extension-dev/SKILL.md) 的编排层，按规格文档自动完成工程准备和代码生成。
- 任意路径只要会写前端代码，最终都必须经过 [iidp-frontend-codegen-protocol](references/iidp-frontend-codegen-protocol.md)。

## 路由规则

> **所有涉及代码生成的路由（规则 1、2、3、5、5a），在进入子技能之前必须先通过 [前置门禁](#-前置门禁工程存在性检查mandatory--不可绕过)。**

1. **用户只提供了文件路径/文档引用，没有说明意图** → 先读取文件，根据内容自动判断：
   - 内容是 IIDP 前端规格文档（包含页面结构、字段规格、IIDP 实现建议等章节）→ **执行前置门禁** → 按 [iidp-frontend-spec-code](references/iidp-frontend-spec-code/SKILL.md) 流程生成代码。
   - 内容是需求/业务描述（用户故事、场景说明、功能清单等）→ 按 [iidp-frontend-spec-doc](references/iidp-frontend-spec-doc/SKILL.md) 流程先生成规格文档。
   - 内容无法识别 → 询问用户想对文件做什么。
2. **用户要做新需求但还没有规格文档** → 先调用 [iidp-frontend-spec-doc](references/iidp-frontend-spec-doc/SKILL.md) 生成规格，**生成完成后自动判断是否需要前端代码**：
   - 规格判定需要前端代码 → **执行前置门禁** → **自动衔接** [iidp-frontend-spec-code](references/iidp-frontend-spec-code/SKILL.md) 生成代码。
   - 规格判定无需前端代码 → 输出规格文档并说明原因，等待用户后续指令。
3. **用户已有规格文档要写代码** → **执行前置门禁** → 调用 [iidp-frontend-spec-code](references/iidp-frontend-spec-code/SKILL.md)。
4. **用户要创建工程/应用/安装依赖/启动项目** → 直接调用 [iidp-frontend-init](references/iidp-frontend-init/SKILL.md)（新建工程场景的前置门禁自然满足）。
5. **用户要在现有应用中加功能（有明确目标节点/页面）** → **执行前置门禁** → 调用 [iidp-frontend-extension-dev](references/iidp-frontend-extension-dev/SKILL.md)。
5a. **用户要在现有项目中加功能，但未明确工程位置或目标应用** → **执行前置门禁** → 先按 [iidp-frontend-project-locator](references/iidp-frontend-project-locator.md) 定位工程与 app，再调用 [iidp-frontend-extension-dev](references/iidp-frontend-extension-dev/SKILL.md)。
6. **用户问框架怎么用、组件有哪些属性、扩展协议是什么** → 调用 [iidp-frontend-dev-manual](references/iidp-frontend-dev-manual/SKILL.md)。
7. **用户要查阅标准模板节点 ID 命名规则** → 调用 [iidp-frontend-standard-ids](references/iidp-frontend-standard-ids/SKILL.md)。
8. **用户需求跨越多个阶段** → 按工作流顺序依次调用对应子技能。

## 技能调用约定

当用户询问 IIDP 前端相关问题或知识时，不要仅凭通用知识回答。按问题类型路由：

- 具体业务扩展、扩展视图、扩展钩子、数据源、绑定、事件和 commands 实现问题 → 调用 [iidp-frontend-extension-dev](references/iidp-frontend-extension-dev/SKILL.md)。
- 纯框架机制、组件属性、工程配置、在线视图和原始文档查阅问题 → 调用 [iidp-frontend-dev-manual](references/iidp-frontend-dev-manual/SKILL.md)。

## 核心原则

- 本入口只负责工程门禁、子技能路由和主协议强制加载，不维护独立代码生成规则副本。
- 实现分支、组件规则、请求方式、扩展格式、自定义组件边界、禁止项和合规扫描，均以 [iidp-frontend-codegen-protocol](references/iidp-frontend-codegen-protocol.md) 为准。
- 框架机制、组件属性和扩展协议事实来自 [iidp-frontend-dev-manual](references/iidp-frontend-dev-manual/SKILL.md) 及其索引文档；无法确认时进入待确认，不猜。
