# IIDP 存量系统识别与规格书提取规则

本文档由以下样本项目归纳：

- 后端：`E:\Sie\codes\AI\demo\iidp-backend-demo-ai`
- 前端：`E:\Sie\codes\AI\demo\iidp-frontend-demo`

用于 `code-index` 在识别到 IIDP 项目时，从通用 Spring/Vue 分析切换到 IIDP 元模型、视图配置、菜单配置和前端扩展协议的提取流程。

---

## 1. 识别信号

### 1.1 后端 IIDP 信号

满足 2 项以上即可判定为 IIDP 后端候选：

- 根 `pom.xml` 的 `groupId` 或模块名包含 `com.sie.iidp`、`iidp`、`sie-iidp-*`。
- Maven 多模块包含 `*-apps`、`*-start`、`*-common`。
- 依赖或属性出现 `sie-snest-sdk`、`sie-snest-engine`、`sie-iidp-plugin`、`sie-iidp-iam`、`sie-iidp-tenant`、`sie-iidp-dev-center`。
- 存在 `apps/apps.json`，且 `loaders` 包含 `ApiLoader`、`SdkLoader`，`apps.SDK` 中列出 IIDP jar。
- 业务模块下存在 `src/main/java/**/app.json`。
- Java 源码 import `com.sie.snest.sdk.BaseModel` 或注解 `com.sie.snest.sdk.annotation.meta.Model`、`Property`、`MethodService`。
- 资源或源码目录中存在 `views/*.json`、`data/*.json`、`menus.json`。

### 1.2 前端 IIDP 信号

满足 3 项以上即可判定为 IIDP 前端候选：

- `package.json` scripts 包含 `init:tech`、`install:tech`、`build:apps`、`build:comp`。
- `package.json` dependencies 包含 `@tech/t-core`、`@tech/t-el-ui`、`@tech/t-build`、`@tech/t-base`。
- 存在 `build/webpack.dev.js`、`build/webpack.prod.js`、`build/webpack.comp.js`。
- 存在 `config/apps.json`，其中 app 映射到 `/umdComps/tech-*/config/app.json`。
- 存在 `apps/<appName>/index.js`，并从 `@tech/t-core/RuntimeIndex/build.RuntimeIndex.umd.js` 引入 `mergeExtend`。
- 存在 `apps/<appName>/common/index.js`，导出 `asset`、`assetImport`、`common`、`schema`、`extendView`、`comps`、`hook`。
- 存在 `apps/<appName>/views/**/*.js`，对象内含 `selector`、`type`、`view`、`beforeOperate`、`hook`。
- 存在 `apps/<appName>/model-views/*.js` 或 `.mjs`，导出 `views` 配置。

---

## 2. 后端提取规则

### 2.1 应用与模块

枚举：

```text
pom.xml
apps/apps.json
**/src/main/java/**/app.json
**/src/main/java/**/views/*.json
**/src/main/java/**/data/*.json
**/src/main/resources/i18n/*.properties
```

从 `app.json` 提取：

- `name`：IIDP app 名称。
- `displayName`、`description`、`summary`：模块说明。
- `product`、`productDesc`：产品线。
- `type`：通常为 `SDK`。
- `resolved`：Java 包根。
- `data`：初始化数据文件清单。
- `dependencies`：应用依赖。

从 `apps/apps.json` 提取：

- `loaders.API`、`loaders.SDK`：加载器类型。
- `apps.SDK`、`apps.API`：平台 jar 与业务 jar 装载清单。
- 将 `sie-iidp-*` jar 归为平台能力，将业务 `*-SNAPSHOT.jar` 归为本项目应用模块。

### 2.2 业务模型入口

IIDP 后端业务入口不是 Controller 优先，而是元模型优先。

必须枚举并读取：

```text
**/src/main/java/**/model/*.java
**/src/main/java/**/*Model.java
```

识别条件：

- 类继承 `BaseModel`。
- 类上有 `@Model(...)` 或 `@ModelRef(...)`。

从 `@Model` 提取：

- `name`：模型名，优先作为业务实体 ID。
- `tableName`：物理表名；缺失时通常可用 `name` 作为候选表名并标注需确认。
- `displayName`：业务名称。
- `description`：业务说明。
- `parent`：模型继承/扩展关系。
- `type`：数据模型/业务模型类型。
- `isLogicDelete`：是否逻辑删除。
- `isAutoLog`：是否自动日志。
- `isPrint`：是否支持打印。
- `isShard`、`shard`：分库分表或分区规则。
- `indexes`：索引和唯一约束。

从字段和注解提取：

- 字段名和 Java 类型。
- `@Property(displayName, columnName, length, dataType, dateFormat, defaultValue, readonly, store, computeMethod, related, widget, displayForModel)`。
- `@Validate.*`：必填、长度、格式、分组校验、错误提示。
- `@Selection(values)`：枚举选项。
- `@Selection(model, properties, method, multiple)`：引用模型、远程下拉或选择器。
- `@Dict(typeCode, multiple)`：字典类型。
- `@ManyToOne`、`@OneToMany`、`@ManyToMany`、`@JoinColumn`、`@JoinTable`：模型关系和 ER 图。
- `@ModelRef`、`@Function`：聚合/引用视图模型。

### 2.3 业务服务入口

必须在模型类中枚举：

```text
@MethodService(...)
```

从 `@MethodService` 提取：

- `name`：服务名；缺失时使用 Java 方法名。
- `description`：业务动作名称。
- `hiddenApi`：是否隐藏接口；隐藏接口不作为普通用户功能入口，但应进入内部能力说明。
- `doc`：外部文档链接或本地文档。
- 方法签名：参数、返回类型、异常。
- 方法体调用链：`RecordSet.callSuper`、`getMeta().get(model)`、`BaseContextHandler.getMeta()`、`initService()`、`BaseModelExtUtils`、`DbUtils`、`RelationDBAccessor`。

IIDP 默认 CRUD 通常由元模型服务提供，不一定有显式 Controller。规格书应把以下来源合并为功能清单：

- 视图 `tbar/buttons` 中的 `action/auth/service`。
- 模型类 `@MethodService` 自定义服务。
- 标准服务名：`create`、`update`、`delete`、`search`、`find`、`count`、`export`、`import`。

### 2.4 菜单与页面

枚举：

```text
**/views/menus.json
```

从 `menus` 提取：

- 菜单 ID：对象 key 或 `name`。
- `display_name`：菜单名称。
- `parent_ids`：菜单树。
- `sequence`：排序。
- `active`：是否启用。
- `model`：绑定模型。
- `view`：绑定视图集合，通常是逗号分隔的 grid/search/form/tree。

菜单是模块规格书的页面入口来源。若存在菜单绑定模型，应优先按菜单归类页面，而不是只按 Java 包归类。

### 2.5 后端视图 JSON

枚举：

```text
**/views/*.json
```

从 `views` 提取：

- 视图 ID。
- `name`：视图显示名。
- `model`：绑定模型。
- `type`：`grid`、`search`、`form`、`tree` 等。
- `mode`：主视图模式。
- `body.columns`：列表列、搜索字段、表单字段。
- `body.buttons`：行按钮。
- `body.tbar`：工具栏按钮。
- `action`：标准动作或自定义动作。
- `auth`：权限动作，如 `read`、`create`、`update`、`delete`、`enable`、`disable`。
- `service`：调用的模型服务，需回连 `@MethodService`。
- `args`、`bind_*`、`actionAfter`：交互参数和执行后动作。
- `tabs`、子 `body.model`、`field`：子表/明细关系。
- `useOpenView`：弹窗选择、跨模型引用。
- `api.grid.search/count`：前后端或视图层接口参数。

视图 JSON 是 IIDP UI 规格的主要事实来源。生成 UI 原型时不要凭 Entity 字段猜页面，应以 `menus.json` + `views/*.json` 为主，`@Model/@Property` 为字段补充。

### 2.6 初始化数据和字典

枚举：

```text
**/data/*.json
```

常见模型：

- `base_dict_type`
- `base_dict_value`
- `iidp_encoder_rule`
- `iidp_encoder_rule_detail`
- `iidp_biz_setting`

提取为：

- 字典值和枚举说明。
- 编码规则。
- 业务配置项。
- 示例/种子数据。

---

## 3. 前端提取规则

### 3.1 前端应用结构

枚举：

```text
package.json
config/apps.json
apps/*/index.js
apps/*/config/app.json
apps/*/common/*.js
apps/*/model-views/*.{js,mjs}
apps/*/views/**/*.js
apps/component/**/*.{js,vue}
```

从 `apps/<appName>/index.js` 提取：

- `export const name = 'tech-...'`：UMD 组件名。
- `mergeExtend(commonConfig)`：扩展合并入口。
- `config.global`：应用全局配置。

从 `common/index.js` 提取：

- `asset`：静态资源声明。
- `assetImport`：全局资源和外部脚本引入。
- `common`：公共配置。
- `schema`：初始视图结构扩展。
- `extendView`：视图扩展入口。
- `comps`：自定义组件注册。
- `hook`：钩子扩展入口。

### 3.2 后端视图放前端工程

`apps/<appName>/model-views/*.js` 可导出：

```js
module.exports = {
  views: {
    view_id: { ... }
  }
}
```

这些内容语义等价于后端 `views/*.json`，应一并进入页面规格。提取字段同后端视图 JSON。

### 3.3 扩展视图

枚举：

```text
apps/<appName>/views/**/*.js
```

每个导出对象的 key 是扩展名。提取：

- 扩展名：通常形如 `应用_菜单_模块_作用_extend_view`。
- `selector.attr`、`selector.value`：目标节点定位。
- `type`：扩展类型。
- `view`：新增、合并、替换或删除的视图节点。
- `beforeOperate(app, operateItem, options)`：扩展执行前逻辑。
- `hook`：标准页/组件钩子。

扩展类型含义：

- `before`：插入到目标节点前。
- `after`：插入到目标节点后。
- `merge`：和目标节点深度合并；目标节点有 `items` 时通常不合并 `items`。
- `unshift`：插入到目标节点 `items` 头部。
- `append`：插入到目标节点 `items` 尾部。
- `replace`：替换目标节点。
- `delete`：删除目标节点。
- `custom`：不自动处理目标节点，仅执行 `beforeOperate`。

常见视图节点和交互字段：

- `type`：`button`、`dialog`、`form`、`row`、`input`、`table`、`tabs`、自定义组件类型。
- `id`：节点 ID，规格中必须保留。
- `value`、`text`、`title`：显示文本。
- `items`：子节点。
- `options`、`style`、`className`：展示配置。
- `dataSource`：本地数据。
- `ds_config`：数据源配置，常见 `type: 'meta'`、`method: 'save'`、`options.params.args`。
- `bind_on_click`、`bind_on_operates`、`bind_on_keyupEnter`、`bind_on_changeHandler`、`bind_on_cellClick`、`bind_on_rowDblclick`：交互事件。

### 3.4 前端调用后端元服务

常见调用方式：

- `window.Tech.httpMeta({ data: { params: { app, model, service, args }}})`
- `vm.request(name, { args })`
- `ds_config` 中的 `options.params.model/service/app/args`

提取为前后端契约：

- `app`：目标 IIDP 应用。
- `model`：目标元模型。
- `service`：目标服务，回连后端 `@MethodService` 或标准服务。
- `args`：参数结构。
- `properties`、`filter`、`limit`、`offset`、`valuesList`、`bind_ids`：常见参数。

### 3.5 Hook

扩展对象可包含 `hook`，常见层级：

- `page.queryView`
- `gridPage.created/mounted/destroy`
- `detailPage.created/mounted/destroy`
- `grid.created/mounted/destroy`
- `grid.canCreate/canEdit/canDelete`
- `grid.onConfirm/beforeDelete/delete/afterDelete`
- `grid.beforeQuery/query/afterQuery/queryCount`
- `grid.select/cancelSelect`
- `grid.edit.canCreate/canDelete/canSave/onConfirm`

提取规则：

- 若调用 `vm.super['xxx']`，说明是在平台默认逻辑前后增强。
- 若返回 `false`，表示拦截默认行为。
- 若修改 `params.args`，记录为请求参数改写。
- 若使用 `window.ELEMENT.Message`，记录为用户提示。
- 若访问 `vm.biz.*`，记录为标准页业务上下文依赖。

---

## 4. IIDP Codebook 输出映射

### 4.1 模块划分

优先级：

1. 后端 `app.json` 的 `name` 和 `displayName`。
2. `menus.json` 顶层菜单和子菜单。
3. Java 包路径，如 `com.sie.iidp.example.studentmgr`。
4. 前端 `apps/<appName>` 和 `views/<business>/`。

### 4.2 功能清单

IIDP 的 `ENDPOINT_LIST` 应改名或扩展为 `CAPABILITY_LIST`，来源包括：

- 菜单入口。
- 视图按钮：`body.buttons`、`body.tbar`。
- `service` 指向的模型服务。
- `@MethodService` 自定义服务。
- 标准 CRUD/查询/导入/导出服务。
- 前端扩展新增按钮和事件。

字段建议：

```text
menuId, menuName, model, viewId, viewType, action, auth, service,
sourceFile, sourceKind, args, frontendExtension, hook, targetNodeId
```

### 4.3 SRS

每个菜单页面输出一组页面级需求：

- 页面入口和菜单路径。
- 绑定模型和视图集合。
- 搜索条件。
- 列表字段。
- 表单字段和校验。
- 工具栏操作。
- 行操作。
- 子表/弹窗/openView。
- 权限动作。
- 自定义服务和前端扩展。

每个 `CAPABILITY_LIST` 条目至少生成一个功能点，不能只按 Controller/API 生成。

### 4.4 API 文档

IIDP API 文档应分两类：

- 元服务调用：`app + model + service + args`。
- 传统 HTTP Controller：如项目中存在 `@RestController`，仍按通用 Spring 规则输出。

对元服务，章节标题建议：

```text
### app/model/service
```

内容包括：

- 调用来源：视图按钮、前端扩展、hook、模型方法。
- 参数：`args`、`valuesList`、`filter`、`properties`、分页。
- 返回：从 Java 方法返回类型、视图期望和调用代码推断。
- 权限：`auth`。
- 后端实现：`@MethodService` 方法位置。

### 4.5 数据库与模型

从 `@Model/@Property` 生成逻辑数据模型，从 `tableName/columnName` 推断物理表和字段。

若缺失 `tableName` 或 `columnName`：

- 使用模型名/字段名作为候选。
- 标注 `[需确认]`。

关系图应包含：

- `@ManyToOne`
- `@OneToMany`
- `@ManyToMany`
- `@JoinTable`
- `@Model(parent=...)`
- 视图 JSON 中的子表 `field/model/type`

### 4.6 UI 原型

IIDP UI 原型应优先由 `menus.json` + `views/*.json` + `model-views/*.js` 驱动：

- `grid` → 列表页。
- `search` → 搜索区。
- `form` → 新增/编辑/详情表单。
- `tree` → 左树右表或树形页面。
- `tabs`/子 `body` → 子表。
- `tbar/buttons` → 工具栏和操作列。
- 前端扩展 `after/append/unshift` → 新增按钮/区域。
- 前端扩展 `replace` → 替换区域或自定义页面。
- hook → 行为说明，不直接可视化为静态控件，除非产生按钮/弹窗。

### 4.7 流程图

优先为以下行为生成流程图：

- 视图按钮调用 `service`。
- 前端 `bind_on_*` 中调用 `window.Tech.httpMeta` 或 `vm.request`。
- 后端 `@MethodService` 中调用 `RecordSet.callSuper`、`getMeta().get()`、`initService()`、远程 RPC、事务、导入导出。
- hook 中拦截或增强查询、保存、删除。

### 4.8 baseline-spec 结构化基线规格

IIDP Codebook 必须同时输出 Markdown 文档和结构化 `baseline-spec`。Markdown 用于人阅读和评审，`baseline-spec` 用于后续 brownfield 差异规格、增量补丁生成和规格/代码一致性校验。

输出目录：

```text
codebook/baseline-spec/
├── apps.json
├── menus.json
├── models.json
├── views.json
├── services.json
├── frontend-extends.json
├── capability-list.json
├── artifact-map.json
├── trace-map.json
└── unresolved.json
```

通用字段要求：每个数组项都必须尽量包含以下字段。

```json
{
  "id": "稳定规格 ID",
  "kind": "app|menu|model|view|service|frontendExtend|capability",
  "name": "业务或技术名称",
  "sourceFile": "相对输入工程的源码路径",
  "sourceLine": 1,
  "confidence": "high|medium|low",
  "status": "confirmed|inferred|needs-confirmation|placeholder",
  "unresolvedReason": ""
}
```

规则：

- `sourceFile/sourceLine` 能从源码、JSON 或前端扩展定位时必须填写；只能定位到文件时 `sourceLine` 可为 `null`。
- `confidence=high` 只能用于源码事实直接确认或前后端多层证据闭合的项。
- `status=placeholder` 用于示例占位，如 `model: "xxx"`、注释中的 `httpMeta`、未启用的示例扩展。
- `status=needs-confirmation` 的项必须同步写入 `unresolved.json`。
- 不允许从 Markdown 文档反抽 baseline；必须从 Phase A/C 读取到的源码事实、配置文件和前后端配套矩阵直接生成。

各文件职责：

| 文件 | 内容 |
|---|---|
| `apps.json` | 后端 `app.json`、`apps/apps.json`、前端 `config/apps.json`、`apps/<app>/index.js`，以及前后端 app 配对候选 |
| `menus.json` | `menuId/name/display_name/model/view/parent_ids/active/sequence`，以及菜单到模块/页面的归属 |
| `models.json` | `@Model/@ModelRef/@Property/@Validate/@Selection/@Dict`、关系注解、索引、分片、逻辑删除、表名和字段 |
| `views.json` | 后端 `views/*.json` 和前端 `model-views/*.{js,mjs}` 的统一视图结构，包含 viewId、type、model、字段、按钮、tabs、openView、api |
| `services.json` | `@MethodService`、标准元服务、传统 Controller、服务参数、返回、权限和实现位置 |
| `frontend-extends.json` | `selector`、`type`、`view`、`beforeOperate`、`hook`、`ds_config`、`bind_on_*`、状态变化和目标节点 |
| `capability-list.json` | 由菜单、视图按钮、服务、标准 CRUD、前端扩展和 hook 合并出来的当前能力清单 |
| `artifact-map.json` | 每类规格项推荐落点，如后端 model、后端 view JSON、前端 `apps/<app>/views/**/*.js`、`views/index.js`、`common/comps.js` |
| `trace-map.json` | `specId -> sourceFile/sourceLine/evidence` 的追踪映射，供差异规格和补丁生成回连源码 |
| `unresolved.json` | 待确认、占位、低置信度、运行时节点、缺失后端或前端配套项 |

`capability-list.json` 字段建议：

```json
{
  "id": "capability.demo_example_student_menu.enableDisable",
  "appName": "sie-iidp-demo-example",
  "menuId": "demo_example_student_menu",
  "menuName": "学生管理",
  "model": "example_student",
  "viewId": "demo_example_student_grid",
  "viewType": "grid",
  "action": "enable",
  "auth": "enable",
  "service": "enableDisable",
  "serviceType": "methodService|standard|controller|frontendOnly",
  "args": {},
  "frontendExtension": null,
  "hook": null,
  "targetNodeId": "demo_example_student_menu_table_toolbar_enable",
  "sourceKind": "viewButton|methodService|standardService|frontendExtend|hook|menu",
  "sourceFile": "sie-iidp-demo-example/.../views/example_student_view.json",
  "sourceLine": null,
  "confidence": "high",
  "status": "confirmed"
}
```

`artifact-map.json` 字段建议：

```json
{
  "specId": "frontendExtend.demo_example_unit_enable_btn",
  "artifactType": "frontendExtend",
  "recommendedTarget": "apps/sie-iidp-demo-example/views/tablePage/example_enable_btn.js",
  "indexFile": "apps/sie-iidp-demo-example/views/index.js",
  "generationMode": "append-existing|create-new|merge-existing|needs-confirmation",
  "ownerSkill": "frontend",
  "notes": "已有扩展文件存在，增量修改时优先保留原 export key"
}
```

baseline-spec 与 Markdown 文档的关系：

- `srs.md` 可以由 `capability-list.json` 派生，但不能替代 `capability-list.json`。
- `api.md` 可以由 `services.json` 派生，但不能替代 `services.json`。
- `database.md` 可以由 `models.json` 派生，但不能替代 `models.json`。
- `interaction-spec.md` 与 `create-project-contract.md` 可以由 `frontend-extends.json`、`views.json` 和 `artifact-map.json` 派生。

---

## 5. 完整性核对

IIDP 项目不可只用 `codegraph_status.route` 校验接口完整性，因为大量能力不是 HTTP route。

必须核对：

- `app.json` 数量 == 识别到的 IIDP 应用数量。
- `menus.json` 中 active 菜单均有对应 codebook 页面说明。
- 每个菜单的 `model` 能回连到一个 `@Model`，若不能则标注 `[需确认]`。
- 每个菜单的 `view` 中列出的 viewId 能在后端 `views/*.json` 或前端 `model-views/*.js` 中找到。
- 每个视图按钮的 `service` 能回连到 `@MethodService` 或标准服务名。
- 每个前端 `views/**/*.js` 扩展都有目标 `selector.value`，并在规格中记录目标节点 ID。
- 每个 `hook` 都记录所属页面/节点、触发点和是否调用 `vm.super`。
- 每个 `data/*.json` 的字典、编码规则和业务配置均进入字典/配置章节。
- `baseline-spec/` 目录存在，且十个 JSON 文件均已生成。
- `capability-list.json` 中每个能力至少能回连到菜单、视图按钮、服务、前端扩展或 hook 中的一类来源。
- `trace-map.json` 覆盖所有 `models.json`、`views.json`、`services.json`、`frontend-extends.json` 和 `capability-list.json` 的 confirmed/inferred 项。
- `unresolved.json` 覆盖所有 `status=needs-confirmation|placeholder` 或 `confidence=low` 的项。

---

## 6. 样本依据速查

- 后端根 POM：`iidp-backend-demo-ai/pom.xml`，包含 `com.sie.iidp`、`sie-snest-sdk`、`sie-iidp-plugin`、平台 app jar。
- 后端装载清单：`iidp-backend-demo-ai/apps/apps.json`。
- 后端应用描述：`sie-iidp-demo-example/src/main/java/com/sie/iidp/example/app.json`。
- 后端模型：`studentmgr/model/ExampleStudent.java`，包含 `@Model`、`@Property`、`@Validate`、`@Selection`、`@Dict`、关系注解、`@MethodService`。
- 后端菜单：`views/menus.json`，菜单绑定 `model` 与 `view`。
- 后端视图：`studentmgr/views/example_student_view.json`，包含 grid/search/form、按钮、权限、服务调用。
- 前端 package：`iidp-frontend-demo/package.json`，包含 `@tech/*` 依赖和 `init:tech/build:apps`。
- 前端应用入口：`apps/sie-iidp-demo-example/index.js`。
- 前端公共入口：`apps/sie-iidp-demo-example/common/index.js`。
- 前端 model-view：`apps/sie-iidp-demo-example/model-views/example_class_view.js`。
- 前端扩展：`apps/sie-iidp-demo-example/views/tablePage/example_enable_btn.js`。
- 前端 hook：`apps/sie-iidp-demo-example/views/hook/example_org_level_menu_hook.js`。

---

## 7. 与正向 skills 的对应关系

`code-index` 对 IIDP 的工作是 `create-project` / backend / frontend 正向生成链路的逆向操作：

```text
正向：业务需求 -> 规格 -> backend/frontend skills -> IIDP 代码与配置
逆向：IIDP 代码与配置 -> code-index -> 规格 / Codebook
```

### 7.1 对齐 `skills/backend`

逆向提取时，应把 `skills/backend/SKILL.md` 及其 core references 作为正向事实来源的镜像：

- `references/core/pom-structure.md` -> 逆向识别 Maven 多模块、`apps/apps.json`、启动模块和 jar 装载关系。
- `references/core/app-json.md` -> 逆向读取 `app.json` 的 `name/resolved/view/data/events`。
- `references/core/model.md` -> 逆向解析 `@Model/@Property/@Validate/@Selection`。
- `references/core/model-property-advanced.md` -> 逆向解析模型继承、扩展、日期、字典、视图模型和高级属性。
- `references/core/er.md` -> 逆向生成 ER 图和子表关系。
- `references/core/method-service.md` -> 逆向解析 `@MethodService`、CRUD 重写、业务方法、Excel、RPC、事务和 DTO。
- `references/core/RecordSet.md`、`references/core/DbUtils.md` -> 逆向理解模型服务内的数据访问和跨模型调用。
- `references/core/view.md`、`references/core/view-advanced.md` -> 逆向解析后端视图 JSON。
- `references/core/menu.md`、`references/core/seed-data.md` -> 逆向解析菜单、字典、编码规则和初始化数据。
- `references/core/data-source-api.md` -> 逆向整理元模型 API 契约。
- `references/core/template-hook-openview.md` -> 逆向识别标准模板、Hook、openView、上下表、树表和抽屉。

因此，生成 `codebook/modules/{module}/srs.md`、`api.md`、`database.md`、`ui/` 时，应优先还原这些后端产物，而不是把 IIDP 后端误判为普通 Controller/Service/Mapper 项目。

### 7.2 对齐 `skills/frontend`

逆向提取时，应把前端 skill 的分层作为规格还原目标：

- `iidp-frontend-spec-doc`：逆向输出的页面规格应覆盖页面定位、页面结构、字段规格、操作事件、状态规则、IIDP 实现建议、接口与数据契约、待确认事项。
- `iidp-frontend-spec-code`：逆向规格应保留目标工程、目标 app、页面/菜单、节点 id、扩展类型、hook 建议、数据源和是否需要前端代码。
- `iidp-frontend-extension-dev`：逆向解析 `selector`、`type`、`beforeOperate`、`view`、`hook`、`ds_config`、`bind_on_*` 时，应按扩展开发规则解释其含义。
- `iidp-frontend-dev-manual`：当组件属性、扩展协议、hook 返回值或 `vm.biz` 语义不确定时，应回查开发手册，而不是凭样本猜测。
- `iidp-frontend-standard-ids`：如果扩展目标是标准模板节点，逆向规格应记录节点 ID 并标注其来源。

### 7.3 逆向规格输出约束

为了能回流到正向生成链路，IIDP 逆向规格必须保留以下字段：

- 后端：`appName`、`product`、`resolved`、模型名、表名、字段、校验、字典、关系、服务、菜单、视图 key、按钮权限、初始化数据。
- 前端：工程/app 目录、页面/菜单、目标节点 id、扩展类型、hook 点、数据源、事件、调用的 `app/model/service/args`。
- 判断：推荐实现方式必须区分 `后端在线视图配置`、`前端扩展`、`hook`、`自定义 Vue2 组件`。
- 不确定项：接口、模型、节点 id、枚举、权限、数据库字段无法从源码确认时，写入 `[需确认]`，不得补全成事实。

这些字段必须同时出现在人类可读 Markdown 和 `baseline-spec/*.json` 中。后续正向链路、差异规格或增量补丁生成应优先读取 `baseline-spec`，Markdown 只作为评审说明和上下文补充。

### 7.4 baseline-spec 与 brownfield 差异规格的衔接

`baseline-spec` 表示当前代码事实，默认只读。开发者新增或修改功能时，不应直接修改 `baseline-spec`，而应由后续 brownfield skill 创建 `delta-spec`：

```text
baseline-spec（当前事实，只读）
  + delta-spec（开发者修改的新需求/差异）
  -> target-spec（合成后的目标规格）
  -> 增量补丁
```

因此，`baseline-spec` 必须满足：

- 每个 `id` 稳定，可被 `delta-spec` 引用，例如 `model.example_student`、`menu.demo_example_student_menu`、`view.demo_example_student_grid`。
- 每个可改造项都有 `trace-map`，能定位到源码或配置。
- 每个不确定项都集中到 `unresolved.json`，避免后续生成器把推断当事实。
- `artifact-map.json` 能说明同类变更通常应改后端模型、视图 JSON、前端扩展文件、hook 文件还是 `views/index.js`。

---

## 8. codegraph 缺失时的 IIDP fallback 流程

当本机未安装 `codegraph`、MCP 不可用，或 `codegraph init/index/status` 无法完成时，仍然可以生成 IIDP Codebook，但必须显式标记为 fallback 产物，并遵循以下流程。

### 8.1 文件枚举

使用确定性文件枚举替代 `codegraph_files`：

```text
rg --files <backend>
rg --files <frontend>
```

必须覆盖以下事实源：

- 后端：`pom.xml`、`apps/apps.json`、`**/app.json`、`**/model/*.java`、`**/*Model.java`、`**/views/menus.json`、`**/views/*.json`、`**/data/*.json`、`**/application*.properties`。
- 前端：`package.json`、`config/apps.json`、`apps/*/index.js`、`apps/*/config/app.json`、`apps/*/common/*.js`、`apps/*/model-views/*.{js,mjs}`、`apps/*/views/**/*.js`、`apps/component/**/*.{js,vue}`。

### 8.2 静态提取

在没有符号图时，按文本和结构化文件提取：

- JSON 文件使用 JSON 解析读取，不用肉眼猜测字段。
- Java 模型使用 `@Model`、`@Property`、`@MethodService`、`BaseModel`、`@Selection`、`@Dict`、关系注解作为事实源。
- 前端扩展使用 `selector`、`type`、`beforeOperate`、`view`、`hook`、`ds_config`、`bind_on_*`、`window.Tech.httpMeta`、`vm.request` 作为事实源。
- 定时任务和基础设施模块额外扫描 `@XxlJob`、`@Scheduled`、`@Component`、`application.properties`。

### 8.3 完整性校验

fallback 模式下不能声称已经取得真实调用图。必须在 Codebook 中说明：

- 未生成 `codegraph` node id。
- 未生成真实 callers/callees/impact。
- 流程图为源码语义骨架，不是 codegraph trace 结果。
- 数据库 DDL 如果不是运行库导出，必须标注为由 `@Model/@Property` 推断。

### 8.4 输出要求

fallback 产物仍应生成完整文档目录，但 `self-review-and-improvements.md` 或 `overview.md` 必须记录：

- fallback 原因。
- 使用的替代命令和事实源。
- 未覆盖的动态运行时信息。
- 建议安装 `codegraph` 后重跑并补齐调用图。

---

## 9. 输出目录不可写时的处理规则

当用户指定的输出目录不在当前可写根目录内，且提权审批失败或不可用时：

1. 不得绕过沙箱或使用其他方式强写目标目录。
2. 应改写到当前可写工作区下的等价目录，例如 `codebook-iidp-demo/`。
3. 在 `overview.md`、`.progress.md` 和 `self-review-and-improvements.md` 中明确写明：
   - 用户指定目录。
   - 实际输出目录。
   - 未写入指定目录的原因。
4. 最终回复必须提醒用户实际产物位置，避免误以为已经落到指定目录。
5. 如果后续用户允许新的可写根或审批恢复，可再执行复制/同步，不应在本次生成中伪造目标路径。

---

## 10. IIDP 输入形态识别与前后端配套关系

`code-index` 在识别到 IIDP 项目后，不能立刻按“后端模块 / 前端模块”分别生成文档。必须先执行“输入形态识别”，判断用户给的是：

1. 仅后端工程。
2. 仅前端工程。
3. 前后端配套工程。
4. 多个后端或多个前端混合工程。

识别完成后，再决定输出后端对接面、前端依赖面，或前后端联动矩阵。

### 10.1 后端单独输入

当输入目录满足后端 IIDP 信号，但没有匹配的前端工程时，判定为“后端单独输入”。

必须提取：

- `app.json`：`name`、`displayName`、`product`、`resolved`、`dependencies`、`data`、`events`。
- `apps/apps.json`：平台 app、业务 app、SDK/API 装载清单。
- `menus.json`：`menuId/name/display_name/model/view/parent_ids/active/sequence`。
- `views/*.json`：`viewId/model/type/body/tbar/buttons/action/auth/service/args/api/useOpenView/tabs`。
- Java 元模型：`@Model(name/tableName/displayName/parent)`、`@Property`、`@MethodService`。

后端单独输入时，除后端规格外，必须输出“前端可对接契约”：

```text
integration/backend-contract.md
```

内容至少包括：

- 后端 app 清单。
- 菜单入口清单。
- `menuId -> model -> viewId[]` 映射。
- `viewId/button -> service/auth/action` 映射。
- `model -> @MethodService[]` 映射。
- 可被前端扩展的标准节点 ID 推断规则。
- 前端调用建议：`app/model/service/args`。
- 无法从源码确认的节点 ID、权限、动态视图，标注 `[需运行时确认]`。

### 10.2 前端单独输入

当输入目录满足前端 IIDP 信号，但没有匹配的后端工程时，判定为“前端单独输入”。

必须提取：

- `package.json`：`@tech/*` 依赖、`build:apps`、`init:tech`、`templateApp` 相关构建脚本。
- `config/apps.json`：本地 app、外部 app、`apiHost`、`routerBase`、`templateApp`。
- `apps/*/index.js`：UMD app 名称、`mergeExtend` 入口。
- `apps/*/common/index.js`：`asset`、`assetImport`、`common`、`schema`、`extendView`、`comps`、`hook`。
- `apps/*/model-views/*.{js,mjs}`：前端内置视图定义。
- `apps/*/views/**/*.js`：`selector`、`type`、`beforeOperate`、`view`、`hook`、`ds_config`、`bind_on_*`。
- 元服务调用：`window.Tech.httpMeta`、`vm.request`、`ds_config.options.params`。

前端单独输入时，除前端规格外，必须输出“后端依赖契约”：

```text
integration/frontend-dependencies.md
```

内容至少包括：

- 前端 app 清单。
- 推断需要存在的后端 app。
- 推断需要存在的 `model/service`。
- `selector.value` 指向的菜单、视图或标准节点。
- `model-views` 中声明的视图和模型。
- `httpMeta/vm.request/ds_config` 调用清单。
- 对没有后端源码佐证的 app/model/service/node 标注 `[后端待确认]`。

同时必须输出面向正向生成链路的前端交互规格：

```text
modules/frontend-demo/interaction-spec.md
modules/frontend-demo/create-project-contract.md
```

`interaction-spec.md` 必须按页面/菜单/节点描述：

- 页面入口：`app`、`menuId`、`model`、`viewId`。
- 目标节点：`selector.attr`、`selector.value`、节点类型、所在区域。
- 扩展类型：`before/after/merge/append/unshift/replace/delete/custom`。
- 可视结构：按钮、表单、弹窗、表格、树、tab、openView、自定义组件。
- 交互事件：全部 `bind_on_*`，包括输入、点击、选择、分页、表格行、确认/取消。
- hook：`page/gridPage/detailPage/grid/search/form/tree/tabs/drawerForm/addRel` 的触发点、是否调用 `vm.super`、是否拦截默认行为。
- 数据源：`ds_config`、`reqPrep`、`reqAfter`、`window.Tech.httpMeta`、`vm.request`。
- 状态变化：`display`、`disabled`、`form data`、`tableData`、`vm.biz`、`$ds`、`$select`。
- 校验与提示：表单 rules、`window.ELEMENT.Message`、`window.Tech.confirmbox`。

`create-project-contract.md` 必须给出正向生成建议：

- 应生成或更新哪个 `apps/<app>/views/**/*.js` 文件。
- 应导入到哪个 `views/index.js`。
- 应写入 `extendView`、`hook`、`comps` 还是 `model-views`。
- 需要依赖的后端 `app/model/service/args`。
- 哪些节点/服务/字段需要运行时确认。
- 每条交互规格的验收标准。

### 10.3 前后端配套输入

当同时输入后端和前端工程时，必须执行配套关系识别。不能只分别输出 backend/frontend 两套文档。

严禁只按 app 名称或目录名称判定前后端强配套。app 名称一致、UMD 名称一致、仓库名相似只能产生“候选配对”。要升级为“强配套”，必须至少再满足以下证据中的两类：

- 前端 `model/model-views/httpMeta/ds_config` 命中后端 `@Model(name)`。
- 前端 `service` 命中后端 `@MethodService` 或 IIDP 标准服务，且其 model 已命中。
- 前端 `selector.value` 以某个后端 `menus.json.name` 作为前缀。
- 前端 `model-views` 的 view key 命中后端 `menus.json.view` 或后端 `views/*.json` key。
- 前端字段、表格列、搜索项或表单项命中后端 `@Property` 或后端 view columns。

强配套的最低门槛是：至少 1 条 app/UMD/product 级候选证据 + 至少 2 类非 app 级闭合证据。非 app 级证据必须来自 `model`、`service`、`menuId`、`viewId`、`selector.value`、字段中的不同类别，不能用同一类证据重复计数。

若只有 app 名称一致，但缺少 model/menu/view/service/selector 等证据，只能标为“疑似配套”，并写入 `unmatched-items.md` 待确认。

强匹配判定公式：

```text
强配套 = 至少 1 类 app/UMD/product 级证据
       + 至少 2 类非 app 级证据
       + 非 app 级证据必须来自 model、menuId、viewId、selector.value、service、字段中的至少两类
```

配套识别按以下置信度评分：

| 关联维度 | 判定规则 | 置信度 |
|---|---|---|
| app 名称一致 | 前端 `config/apps.json` 或 `apps/<app>` 命中后端 `app.json.name` | 候选证据，不能单独判强配套 |
| product 一致 | 前后端都出现相同 `product/productDesc` 或仓库名产品前缀 | 候选证据 |
| UMD 名称可还原 | `tech-<backendAppName>` 或 `/umdComps/tech-<app>/config/app.json` 命中后端 app | 候选证据 |
| model 一致 | 前端 `model`、`model-views.model`、`httpMeta.model` 命中后端 `@Model(name)` | 高 |
| service 一致 | 前端 `service` 命中后端 `@MethodService(name/方法名)` 或 IIDP 标准服务 | 高 |
| menuId 前缀一致 | 前端 `selector.value` 以某个后端 `menuId` 开头 | 高 |
| viewId 一致 | 前端 `model-views` 或 selector 命中后端 `views/*.json` key | 高 |
| 字段一致 | 前端表格/表单/搜索字段命中后端 `@Property` 或 view columns | 中 |
| 目录/名称相似 | 仓库名、app 目录、模块名近似，但缺少 app/model/menu/service 证据 | 低 |

配套输入时必须输出：

```text
integration/frontend-backend-map.md
integration/contract-matrix.md
integration/unmatched-items.md
```

### 10.4 `frontend-backend-map.md` 内容

该文件回答“这两个工程为什么是配套的”。

必须包含：

- 输入工程清单。
- 前端 app 与后端 app 的匹配矩阵。
- 每条匹配的证据和置信度。
- 典型闭环链路：

```text
后端 app.json.name
 -> 后端 menus.json.menuId
 -> 后端 menus.json.model
 -> 后端 menus.json.view
 -> 后端 @Model / @MethodService
 -> 前端 config/apps.json / apps/<app>
 -> 前端 selector.value / model-views
 -> 前端 httpMeta / vm.request / ds_config
```

### 10.5 `contract-matrix.md` 内容

该文件回答“前端每个调用、扩展、节点到底对应后端哪里”。

必须包含：

- `frontendApp -> backendApp`。
- `selector.value -> menuId/viewId/node`。
- `model-views.viewId -> backend viewId/model`。
- `httpMeta/vm.request/ds_config -> app/model/service`。
- `service -> @MethodService/标准服务`。
- `字段 -> @Property/view columns`。
- 置信度：高/中/低。
- 依据文件路径。

### 10.6 `unmatched-items.md` 内容

该文件回答“哪些地方还没对上”。

必须包含：

- 前端调用了但后端未找到的 app/model/service。
- 前端 selector 指向但后端菜单/view/节点无法静态确认的项。
- 后端暴露了但前端未显式使用的菜单、模型、方法服务。
- 只存在于注释、示例或占位中的调用，单独标注为“示例占位”。
- 因未运行系统、未导出运行库、未安装 codegraph 导致无法确认的项。

### 10.7 标准节点 ID 的关联规则

IIDP 前端扩展常用 `selector.value` 指向标准页面节点。配套识别时，应优先用后端 `menus.json.name` 作为前缀匹配：

```text
menuId = demo_example_unit_ext_menu

selector.value 可能为：
demo_example_unit_ext_menu_container_main
demo_example_unit_ext_menu_table_main_table
demo_example_unit_ext_menu_table_toolbar_export
demo_example_unit_ext_menu_form_main_detail_top_save
demo_example_unit_ext_menu_form_main_table_search_btn
```

判断规则：

- `selector.value == menuId` 或 `selector.value` 以 `menuId + "_"` 开头，判定为菜单级强匹配。
- selector 中出现 `_table_`、`_form_`、`_search_`、`_tree_`、`_tab_` 时，结合后端 `view` 类型判断目标区域。
- selector 指向按钮时，应回连后端 view 的 `tbar/buttons` 或标准模板按钮。
- 不能回连到具体 view 节点时，仍可记录为 `menuId` 强匹配 + `node` 待运行时确认。

### 10.8 标准服务与自定义服务

前端调用 `service` 时，按以下顺序判断：

1. 命中后端 `@MethodService(name = "...")`。
2. 未显式 name 时，命中 `@MethodService` 所在 Java 方法名。
3. 命中 IIDP 标准服务：`create`、`update`、`delete`、`search`、`count`、`find`、`save`、`import`、`export`。
4. 仍未命中时，标注 `[后端服务待确认]`。

前端 `vm.request(name, { args })` 如果没有显式 `app/model/service`，应标注为继承当前页面上下文，不得编造成固定后端服务。

### 10.9 输出模式选择

最终输出目录按输入形态选择：

| 输入形态 | 必须输出 |
|---|---|
| 仅后端 | 后端 codebook + `integration/backend-contract.md` + `baseline-spec/` |
| 仅前端 | 前端 codebook + `integration/frontend-dependencies.md` + `baseline-spec/` |
| 前后端配套 | 后端 codebook + 前端 codebook + `integration/frontend-backend-map.md` + `integration/contract-matrix.md` + `integration/unmatched-items.md` + `baseline-spec/` |
| 多工程混合 | 先输出 app 配对候选表，再按每一对生成 integration；无法配对的工程单独输出 contract/dependencies；所有已识别 IIDP 事实统一进入 `baseline-spec/` |

任何模式下，都必须在 `overview.md` 的“关键发现”中说明输入形态和配套识别结果。
