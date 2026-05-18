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
- 本地启动 IIDP 前端工程，或排查扩展应用未加载问题。

需要精确命令、配置样例和排查细节时，读取 [reference.md](reference.md)。

查阅 IIDP 前端框架机制、组件、工程结构等完整文档，使用 [iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)。

## 定位与依赖

- 定位：从 0 初始化 IIDP 前端工程与扩展应用，处理按需加载配置、本地启动与常见排查。
- 上游：[iidp-frontend-dev-manual](../iidp-frontend-dev-manual/SKILL.md)（工程结构/机制/能力边界的事实参考）、[reference.md](reference.md)（命令与排查细节）。
- 下游：通常被 [iidp-frontend-spec-code](../iidp-frontend-spec-code/SKILL.md) 调用以准备工程环境，也可直接承接初始化与启动类任务。

## 硬性规则

- 前端工程只能使用脚手架命令 `tech project <projectName>` 创建。
- 扩展应用只能在工程根目录使用脚手架命令 `tech app <appName>` 创建。
- 判断 `t-cli` 是否已安装时，必须先运行 `tech --help`；只有命令执行成功并返回正常帮助信息才说明已安装，报错输出不算已安装。
- 如果缺少工程存放目录，必须先询问用户，不要自行假设目录。
- 只有用户提到启动、本地运行、安装依赖或更新依赖时，才主动执行依赖命令；单纯创建工程或创建应用时，不要主动执行 `npm run init:tech`。
- 用户提到更新依赖时，必须先确认要更新到哪个版本通道：`latest`、`beta`、`uat` 或 `HotFix`；用户未指定时不要默认执行。
- 不要把 Git 下载、离线 ZIP、复制旧工程、复制 `apps/demo`、手工维护新应用配置作为主创建路径。
- 不要混用 IIDP `t-cli` 流程和其他 CLI 流程。
- 创建应用时，如果规格文档提供了 `product`（产品线标识），自动将 `apps/<app>/config/app.json` 的 `effectPaths.includeRegExp` 配置为 `^/<product>/`。
- 如果没有 `product`，不修改按需加载占位符 `^/需要配置/`，保留原配置不动。
- 按需加载的路由正则**不要带 `/iidp/` 前缀**，只配 `/iidp/` 之后的路径部分。
- **默认不配置 `effectPageIds`**，仅在用户明确要求按 page id 加载时才添加此项。

## 工作流

1. 确认名称和环境。
   - 如果缺少信息，先询问 `projectName`、工程存放目录、`appName`、目标菜单路由或 page id。
   - Node.js 必须 `>=14`，建议 `16.16.0`。
   - 工程名和应用名只使用字母、数字、下划线或短横。

2. 使用脚手架创建工程。
   - 先运行 `tech --help` 判断是否安装 `t-cli`。
   - 如果 `tech --help` 执行成功并返回正常帮助信息，说明 `t-cli` 已安装；如果命令不存在、退出失败或返回报错，则先安装 `t-cli`。
   - 切换到用户指定的工程存放目录。
   - 执行 `tech project <projectName>`。
   - 进入生成的工程目录。

3. 按需安装依赖。
   - 只有用户要求启动、本地运行、安装依赖或更新依赖时，才执行依赖命令。
   - 安装依赖使用 `npm run init:tech`。
   - 用户要求更新依赖时，先询问版本通道；确认后再执行对应脚本：`npm run update:tech`、`npm run update:beta`、`npm run update:uat` 或 `npm run update:hotfix`。
   - 如果用户只要求创建工程或创建应用，创建完成后停止，不主动执行 `npm run init:tech`。

4. 使用脚手架创建扩展应用。
   - 在工程根目录执行 `tech app <appName>`。
   - 不要通过复制已有 app 目录创建应用。
   - 如果规格文档提供了 `product`，创建完成后自动将 `apps/<appName>/config/app.json` 的 `effectPaths.includeRegExp` 配置为 `^/<product>/`。
   - 如果没有 `product`，不修改按需加载配置，保留占位符 `^/需要配置/`。

5. 配置按需加载。
   - 如果规格文档提供了 `product`，打开 `apps/<appName>/config/app.json`，将 `effectPaths.includeRegExp` 配置为 `^/<product>/`。
   - 如果没有 `product`，不修改按需加载配置，保留占位符 `^/需要配置/`。
   - 路由正则**不要带 `/iidp/` 前缀**，只配 `/iidp/` 之后的路径部分。
   - **默认不配置 `effectPageIds`**，仅在用户明确要求按 page id 加载时才添加此项。
   - 优先使用 `apps/<appName>/config/app.json` 的应用级配置；注意 `config/apps.json` 的 `appsDetail` 可能覆盖应用级 `effectPaths`。

6. 按需配置本地后端访问。
   - 稳定联调优先配置 `build/webpack.dev.js` proxy。
   - 临时切换后端时，在浏览器控制台使用 `sessionStorage.setItem("tempApi", "...")`。

7. 本地启动并验证。
   - 普通本地启动使用 `npm run start`。
   - 需要启动前先把外部依赖 app 下载到本地时，使用 `npm run start:local`。
   - 需要通过 API 或应用市场流程获取外部 app 时，使用 `npm run start:base --market=<iidpFrontHost>`。
   - 需要加载平台默认 app 时，不要用 `dev` 代替 `npm run start`。

## 验证清单

- `tech --help` 执行成功并返回正常帮助信息，或已从 IIDP npm registry 安装 `t-cli`。
- 工程创建位置来自用户指定的工程存放目录。
- 工程由 `tech project` 创建。
- 应用由 `tech app` 创建。
- 如果规格文档提供了 `product`，`apps/<appName>/config/app.json` 的 `effectPaths.includeRegExp` 已配置为 `^/<product>/`；如果没有 `product`，保留默认占位符 `^/需要配置/`。
- 当前 URL 中 `/iidp` 后的路径匹配 `effectPaths.includeRegExp`（注意 `effectPaths` 不带 `/iidp/` 前缀），或当前 page id 匹配 `effectPageIds`。
- 本地可用 `window.tech_apps_comps['xxx-app']` 检查 app 加载状态。
- 如果扩展未生效，检查 `config/apps.json`、应用按需加载规则、重复扩展名、重复 selector 或元素 id。
