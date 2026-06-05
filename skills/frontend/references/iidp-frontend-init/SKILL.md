---
name: iidp-frontend-init
description: 指导 IIDP 前端从 0 初始化，包括工程创建、扩展应用创建、安装依赖、更新依赖、按需加载配置和本地启动。Use when the user asks to initialize an IIDP frontend project, create an IIDP extension app, install or update IIDP frontend dependencies, configure IIDP app on-demand loading, or start/debug a local IIDP frontend project.
---

# IIDP 前端初始化

## 使用场景

当用户要求处理 IIDP 前端从 0 到本地运行的流程时使用本 skill，尤其是：

- 创建新的 IIDP 前端工程。
- 创建新的 IIDP 扩展应用。
- 配置 `effectPaths` 或 `effectPageIds` 按需加载。
- 安装或更新依赖。
- 本地启动 IIDP 前端工程，或排查扩展应用未加载问题。

精确命令、配置样例和排查细节统一维护在 [reference.md](reference.md)。本 skill 只保留初始化流程的入口判断和硬性规则。

## 定位与依赖

- 定位：从 0 初始化 IIDP 前端工程与扩展应用，处理按需加载配置、本地启动与常见排查。
- 上游：[reference.md](reference.md)（命令与排查细节）、[iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)（工程结构/机制/能力边界的事实参考）。
- 下游：通常被 [iidp-frontend-spec-code](../iidp-frontend-spec-code/SKILL.md) 调用以准备工程环境，也可直接承接初始化与启动类任务。

## 硬性规则

- 前端工程只能使用 `tech project <projectName>` 创建。
- 扩展应用只能在工程根目录使用 `tech app <appName>` 创建。
- 判断 `t-cli` 是否已安装时，必须先运行 `tech --help`；只有命令执行成功并返回正常帮助信息才说明已安装。
- 缺少工程存放目录、工程名、应用名、版本通道或目标路由时，先询问用户，不要自行假设。
- 只有用户提到启动、本地运行、安装依赖或更新依赖时，才主动执行依赖命令；单纯创建工程或创建应用时，不要主动执行 `npm run init:tech`。
- 用户提到更新依赖时，必须先确认版本通道：`latest`、`beta`、`uat` 或 `HotFix`。
- 不要把 Git 下载、离线 ZIP、复制旧工程、复制 `apps/demo`、手工维护新应用配置作为主创建路径。
- 创建应用时，如果规格文档提供了 `product`，自动将 `apps/<appName>/config/app.json` 的 `effectPaths.includeRegExp` 配置为 `^/<product>/`。
- 如果没有 `product`，不修改默认按需加载配置。
- 按需加载的路由正则不要带 `/iidp/` 前缀，只匹配 `/iidp/` 之后的路径部分。
- 默认不配置 `effectPageIds`，仅在用户明确要求按 page id 加载时才添加。

## 工作流

1. 确认名称和环境：`projectName`、工程存放目录、`appName`、目标菜单路由或 page id、Node.js 版本。
2. 需要创建工程时，按 [reference.md](reference.md) 的脚手架命令创建工程。
3. 需要创建扩展应用时，在工程根目录按 [reference.md](reference.md) 的脚手架命令创建应用。
4. 需要配置按需加载时，按 [reference.md](reference.md) 的 `effectPaths` / `effectPageIds` 规则处理。
5. 仅当用户明确要求安装、更新依赖或启动项目时，执行对应依赖或启动命令。
6. 启动或扩展未生效时，按 [reference.md](reference.md) 的验证与排查清单检查。

## 输出要求

完成初始化、配置或排查说明时，明确给出：

- 已创建或建议创建的工程路径、应用目录。
- 已执行或建议执行的命令。
- `effectPaths` / `effectPageIds` 配置结果。
- 是否安装依赖、是否启动项目，以及原因。
- 仍需用户确认的目录、名称、版本通道、路由或 page id。
