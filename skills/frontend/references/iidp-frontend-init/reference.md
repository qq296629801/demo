# IIDP 前端初始化参考

## 知识来源

本参考整理自 IIDP 前端开发手册与平台常见问题，主要覆盖这些主题：

- 快速上手：环境准备、脚手架安装、工程创建、依赖安装、本地启动。
- 扩展说明：新建扩展应用、应用按需加载、`effectPaths`、`effectPageIds`。
- 工程说明：本地运行命令、后端代理、`tempApi`、应用市场 app 加载。
- 平台常见问题：本地扩展不生效、启动命令差异、工程路径特殊字符导致的启动报错。

## 环境要求

- Node.js 必须 `>=14`，建议使用 `16.16.0`。
- 开发机器建议 16 GB 内存、4 核 CPU 或更高。
- 工程名和路径只使用字母、数字、下划线、短横。路径或项目名包含特殊字符时，Windows 本地启动可能出现 `ENOENT`。

## 脚手架命令

判断是否已安装 `t-cli`：

```sh
tech --help
```

只有该命令执行成功并返回正常帮助信息，才说明已安装。命令不存在、退出失败或返回报错输出时，都不算已安装，需要先安装 `t-cli`。

安装 `t-cli`：

```sh
npm i t-cli -g --registry http://iidp.chinasie.com:9999/maven/repository/npm-group/
```

创建工程前必须确认工程存放目录。如果用户没有提供目录，先询问用户，不要自行假设。

创建 IIDP 前端工程：

```sh
tech project iidp-frontend-demo
```

在工程根目录创建扩展应用：

```sh
tech app app1
```

只有用户明确需要子资源时，才创建 appSub：

```sh
tech appSub app1 sub1
```

只有用户明确需要公共资源时，才创建和使用 appPublic：

```sh
tech appPublic xxapp
tech appPublicUse xxapp xxproject 80xx xxpublic-app
```

重要约束：工程创建必须使用 `tech project`，应用创建必须使用 `tech app`。不要把 Git 下载、离线 ZIP、复制 `apps/demo` 或手工维护 `apps.json` 作为创建流程。

## 依赖与启动

依赖命令不是创建工程或创建应用的默认后续动作。只有用户明确提到启动、本地运行、安装依赖或更新依赖时，才主动执行下面的命令。若用户只要求创建工程或创建应用，不要主动执行 `npm run init:tech`。

安装依赖：

```sh
npm run init:tech
```

组件库发布新版本后更新依赖。用户提到更新依赖时，必须先确认版本通道，不要默认执行。可选版本通道：

```sh
npm run update:tech    # latest
npm run update:beta    # beta
npm run update:uat     # uat
npm run update:hotfix  # HotFix
```

这些脚本分别对应：

```json
{
  "update:tech": "npm i @tech/t-core@latest @tech/t-el-ui@latest @tech/t-build@latest @tech/t-base@latest -S --registry http://iidp.chinasie.com:9999/maven/repository/npm-group/",
  "update:beta": "npm i @tech/t-core@beta @tech/t-el-ui@beta @tech/t-build@beta @tech/t-base@beta -S --registry http://iidp.chinasie.com:9999/maven/repository/npm-group/",
  "update:uat": "npm i @tech/t-core@uat @tech/t-el-ui@uat @tech/t-build@uat @tech/t-base@uat -S --registry http://iidp.chinasie.com:9999/maven/repository/npm-group/",
  "update:hotfix": "npm i @tech/t-core@HotFix @tech/t-el-ui@HotFix @tech/t-build@HotFix @tech/t-base@HotFix -S --registry http://iidp.chinasie.com:9999/maven/repository/npm-group/"
}
```

普通本地启动：

```sh
npm run start
```

工程运行文档中也出现的等价启动命令：

```sh
npm start
```

启动前先把外部依赖 app 下载到本地：

```sh
npm run start:local
```

通过 API 或应用市场获取外部 app：

```sh
npm run start:base --market=http://xx.xx.com:31815/iidp
```

只加载应用市场 app，不合并本地工程 app：

```sh
npm run start:base --market=http://xx.xx.com:31815/iidp --app=onlyMarketApp
```

需要加载平台默认 app 时，不要用 `dev` 代替 `npm run start`。

## 后端代理与临时 API

稳定本地联调优先配置 `build/webpack.dev.js`：

```js
module.exports = merge(mergeCommon, {
  devServer: {
    port: "8085",
    proxy: {
      "/fileSystem": {
        target: "http://iidp.chinasie.com:9999/fileSystem/",
        pathRewrite: { "^/fileSystem": "" }
      },
      "/api": {
        target: "http://127.0.0.1:8060",
        pathRewrite: { "^/api": "" }
      },
      "/register": {
        target: "http://192.168.168.176:8080/",
        pathRewrite: { "^/register": "" }
      }
    }
  }
});
```

临时切换后端时，在 DevTools console 执行后刷新页面。该配置优先级高于 `webpack.dev.js` 中的 IP：

```js
sessionStorage.setItem("tempApi", "http://192.168.1.2:8060");
sessionStorage.setItem("tempApi", "http://test.snest.com:31815/api");
```

文档中的默认本地访问地址：

```text
http://localhost:8085/
```

文档中的 demo 账号：

```text
admin_000001
Admin_000001
```

## 按需加载

执行 `tech app <appName>` 后，新版本会在 `apps/<appName>/config/app.json` 生成按需加载占位配置：

```json
{
  "effectPaths": {
    "includeRegExp": "^/需要配置/"
  }
}
```

按需加载配置规则：如果规格文档提供了 `product`（产品线标识），创建应用后自动将 `effectPaths.includeRegExp` 配置为 `^/<product>/`。如果没有 `product`，不修改按需加载配置，保留占位符 `^/需要配置/`，不要编造配置值。

注意：如果最终保留占位符 `^/需要配置/`，启动或打包时会报错并中止进程，需要在运行前手动修改为真实配置。

按菜单路由加载示例：

```json
{
  "effectPaths": {
    "includeRegExp": "^/SIE-IIDP-DEMO/"
  }
}
```

匹配路径通常指浏览器 URL 中 `/iidp` 后面的部分。例如：

```text
URL: http://localhost:8085/iidp/SIE-IIDP-DEMO/demo_books_menu/demo_books_book_manage_menu
includeRegExp: ^/SIE-IIDP-DEMO/demo_books_menu/demo_books_book_manage_menu
```

在浏览器控制台生成参考路由配置：

```js
window.Tech.$dev.genAppEffect("app文件夹名");
```

按 page id 加载示例：

```json
{
  "effectPageIds": {
    "include": ["页面id"],
    "exclude": ["页面id"]
  }
}
```

在浏览器控制台查看当前 page id：

```js
tech_app.page.data.id
```

优先级说明：如果 `config/apps.json` 在 `appsDetail` 中为某个 app 配置了 `effectPaths`，会覆盖 `apps/<appName>/config/app.json` 中的应用级 `effectPaths`。

非菜单内容区域扩展：如果 app 包含作用于顶级节点的指令扩展等非菜单内容区域扩展，配置按需加载可能导致扩展不生效。此类扩展应抽到不配置按需加载的公共 app。

## 按需加载验证

本地验证：

```js
window.tech_apps_comps["xxx-app"]
```

预期结果：目标页面有值，非目标页面为 `undefined`。

线上验证：在 DevTools Network 中搜索：

```text
xxx-app.umd.min.js
```

预期结果：只有匹配页面会加载该文件。

## 常见问题排查

### 本地扩展应用不生效

检查这些内容：

- `config/apps.json` 的 `self` 和 `apps` 是否包含本地 app。
- `apps/<appName>/config/app.json` 是否配置了按需加载。
- 当前页面路由或 page id 是否匹配 `effectPaths` 或 `effectPageIds`。
- 扩展名是否重复。
- 目标 selector 或元素 id 是否被多个扩展重复处理并产生冲突。

### 平台默认 app 缺失

使用：

```sh
npm run start
```

不要用 `dev` 期待相同行为。FAQ 说明 `dev` 与 `npm run start` 有区别，启动命令错误时可能不会加载平台默认 app。

### 启动出现 ENOENT

工程名和路径只使用字母、数字、下划线、短横，避免空格和特殊字符。

### 对比 app 版本

单机环境可访问：

```text
http://host:port/config/apps.json
```

浏览器控制台可辅助查看：

```js
window._prepareListApps
window.tech_apps.apps
techPluginsVersion
```
