# IIDP 前端项目定位流程

本文是 IIDP 前端工程与扩展应用定位的唯一流程来源。入口 skill、规格到代码、存量扩展开发都只引用本文，不在各自文档中重复维护工程发现细节。

## 适用场景

当任务会读取、修改或生成 IIDP 前端工程内文件，但用户没有明确给出工程根目录或 `appName` 时，先执行本文流程。

如果用户已经明确提供工程路径和 `appName`，仍必须先按本文识别标志验证该路径是 IIDP 前端工程，并验证应用目录存在，再进入具体实现流程。

## 步骤 1：工程发现

从用户工作区搜索 IIDP 前端工程。搜索策略按优先级执行，命中即可停止：

1. 用户指定的路径，直接按下方识别标志验证。
2. 常见位置快速探测：`front/`、`frontend/`、项目根目录下的 `sie-iidp-frontend-project/`。
3. 以上未命中时，再以最大深度 2 层搜索 `**/package.json`，逐个验证识别标志。

IIDP 前端工程识别标志如下，满足 3 项及以上视为候选：

| 标志 | 说明 |
|---|---|
| `package.json` 含 `init:tech` 脚本 | IIDP 脚手架特有命令 |
| `apps/` 目录存在 | 业务应用存放目录 |
| `config/apps.json` 存在 | 工程级应用注册与全局配置 |
| `build/webpack.dev.js` 存在 | IIDP 构建配置 |

发现结果处理：

- 0 个候选：停止前端代码生成，告知用户未找到 IIDP 前端工程，并询问是否创建工程、指定工程路径或跳过前端代码。在用户确认前，禁止创建任何前端文件或目录。
- 1 个候选：记录工程路径，进入应用定位。
- 多个候选：列出候选路径、`package.json` 的 `name` 字段、`apps/` 下应用数量，让用户选择。

## 步骤 2：应用定位

工程确认后，扫描 `apps/` 目录定位目标应用。扫描时排除非应用目录：

- `component`
- `base`
- 以 `dist` 开头的目录
- `node_modules`
- 隐藏目录

对每个候选应用目录，读取 `apps/<name>/config/app.json` 获取 `effectPaths`、`effectPageIds` 等上下文。

结果处理：

- 用户提供了 `appName`：验证 `apps/<appName>/` 是否存在；不存在时展示已发现应用列表，让用户选择或确认正确名称。
- 用户未提供 `appName`：列出所有候选应用及其路由匹配规则，让用户选择。
- 仅 1 个候选应用：可以自动使用，但需要向用户说明并确认。
- 0 个应用：路由到 `iidp-frontend-init` 创建扩展应用。

## 步骤 3：读取现有模式

工程路径和 `appName` 都确认后，只读取最小必要文件了解现有约定：

- `apps/<appName>/views/index.js`：扩展入口结构、import/export 风格。
- `apps/<appName>/config/app.json`：当前按需加载配置。
- `apps/<appName>/views/` 文件列表：业务目录命名约定，不逐个读取文件内容。
- `apps/<appName>/common/comps.js`：仅在涉及自定义组件注册时读取。

完成后，进入对应 skill 的实现流程。

## 与其他规则的关系

- 工程或应用不存在且需要创建时，使用 `iidp-frontend-init`。
- 是否允许写代码、实现分支、契约完整性、selector 来源和合规扫描，以 `iidp-frontend-codegen-protocol.md` 为准。
- 扩展视图（普通节点扩展和扩展钩子）、组件注册和业务扩展边界，以 `iidp-frontend-extension-dev/SKILL.md` 为准。
