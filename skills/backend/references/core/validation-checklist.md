# IIDP 后端生成验收清单

本文件用于交付前自检。生成或修改 IIDP 后端 Demo 后，至少完成静态检查；能编译时再运行 Maven 验证。

## 文件登记

- [ ] 根 `pom.xml` 不被无故修改。
- [ ] `sie-iidp-demo-apps/pom.xml` 已登记新增业务模块。
- [ ] 子模块 `pom.xml` 包含资源打包配置或继承父级可打包 `src/main/java` 资源。
- [ ] `app.json` 位于 `resolved` 对应包路径。
- [ ] `app.json.view` 登记所有视图文件。
- [ ] `app.json.data` 登记菜单、字典、模板、初始化数据，以及 `file/*.json` 附件种子配置。
- [ ] 附件种子引用的真实文件位于 `file/document/*`，路径与 `file/*.json` 的 `path` 一致。
- [ ] `app.json.events/globalConfig/appConfig` 只在有真实事件、白名单、作用域或业务开关时配置。
- [ ] `apps/apps.json` 的 `apps.SDK` 登记新增 jar 名，且对应文件位于 `apps/modules/`。

## 命名一致性

- [ ] `appName/appPkg/moduleName/model_name/Java 类名/视图文件名/菜单 key` 与方案一致。
- [ ] `app.json.name`、Java `@Model(name)`、菜单 key、视图 key 全局唯一，并带业务前缀。
- [ ] 模型名使用小写下划线，不带无意义 `model` 后缀；Java 类、字段、常量分别符合 UpperCamelCase、lowerCamelCase、UPPER_SNAKE_CASE。
- [ ] Java `@Model(name)` 与视图 `model`、菜单 `model` 一致。
- [ ] 菜单 `view` 中的每个 key 都存在于视图 JSON。
- [ ] 自定义按钮 `service` 能对应 Java 服务名。

## Java 模型

- [ ] 模型类有 `@StaticVar @Getter @Setter @Model`。
- [ ] 模型类型选择正确：Buss/Data/Memory/Tree/Config/View 不混用。
- [ ] 继承、扩展、多继承的 `parent` 使用数组或合法单值，不用逗号拼接。
- [ ] 业务字段有 `@Property(displayName)`。
- [ ] `@Property` 高级参数（store、readonly、dbIndex、computeMethod、defaultMethod、widget、contentType）与业务一致。
- [ ] 必填、长度、唯一性、格式校验已表达。
- [ ] 分组校验、Selection 联动方法、Dict 种子数据和 `useDisplayForModel` 回显契约已处理。
- [ ] Date/DateTime 使用正确 `DataType/dateFormat`，范围字段只用于搜索条件。
- [ ] 选项字段有 `@Selection` 或 `@Dict`。
- [ ] ER 字段有正确 ORM 注解和级联策略。
- [ ] ER 关系查询避免循环中逐条读取导致 N+1；必要时批量查询后组装。
- [ ] 唯一业务编码、外键、高频过滤字段和排序字段已设计索引。
- [ ] ER API 指令集 `(0/1/2/3/4/5/6, ...)` 的 create/update 场景合法。
- [ ] 视图模型 `ModelType.View` 的 `@View.From/@View.MapProperty/@View.MapFunction` 映射、聚合别名和可写能力明确。
- [ ] 复杂业务已拆到 Service/DTO。
- [ ] 模型名、状态值、字段名、服务名等字符串已抽常量或集中定义，不在服务逻辑中散落。
- [ ] 没有 `System.out.println`、`printStackTrace`、吞异常。

## 视图与菜单

- [ ] 每个普通模型有 `grid/search/form`。
- [ ] `grid` 的工具栏按钮放 `tbar`，操作列按钮放 `buttons`。
- [ ] 子表 tabs 的 `body.field` 等于 Java ER 字段。
- [ ] 上下表 `typeView: subTable`、`searchByMainTable` 的主子表字段和服务一致。
- [ ] `auth` 权限码稳定且粒度合理。
- [ ] 视图 `name/type/auth` 层级正确，同层级 `auth` 不重复，刷新权限点后可识别。
- [ ] `bind_*`、`linkage`、`transformRes`、`reqPrep/reqAfter` 依赖的字段和服务入参存在。
- [ ] `useSelectTree/useOpenView` 目标模型、视图、服务、返回字段存在。
- [ ] `searchConfig/pagin/domains` 与后端 search/count/作用域过滤一致。
- [ ] 扩展视图 `mode: extension` 的 `inherit_ids` 可解析，`jsonpath` 能命中目标节点。
- [ ] 菜单父子关系正确，`parent_id` 或 `parent_ids` 不冲突。
- [ ] 菜单过滤条件符合复用菜单的数据范围。

## 种子数据

- [ ] 每个 `data/*.json` 使用 `{ "data": { "<seed_key>": ... } }` 或对应平台支持的字典结构，`seed_key` 在当前 App 内唯一。
- [ ] 每条普通数据种子的 `model` 对应 Java `@Model(name)`，没有使用 Java 类名或数据库表别名替代。
- [ ] `properties` 使用 Java 字段名；即使 `@Property(columnName = "...")` 指定数据库列名，也不使用列名写种子字段。
- [ ] `store = false`、计算字段、纯回显字段不写入种子数据。
- [ ] `@Selection` 字段使用 `@Option.value`，不使用 label 文案。
- [ ] ManyToOne 外键使用 `{ "@ref": "seed_key" }`，跨 App 使用 `<app>.<seed_key>`，目标种子存在且加载顺序可解析。
- [ ] ManyToMany 关系使用 `@eval` 指令，字段名与 Java ER 字段或 `@JoinTable.inverseJoinColumns.name` 一致。
- [ ] `@fileId(key)`、`@fileUrl(key)`、`@filePath(key)` 引用的 file key 存在于 `file/*.json`。
- [ ] `isGlobal` 与 `policy` 已按平台级/租户级、永不更新/总是更新策略明确填写或明确使用默认值。
- [ ] 复杂 `config` 字段中的 JSON 字符串已正确转义，CRUD 规则 `R/C/D/U`、通配符和 `${meta.user.*}` 变量符合业务权限设计。

## 服务与 API

- [ ] JSON-RPC 的 `params.model/service/app/tag/context/args` 与接口文档一致。
- [ ] 内置服务 `create/delete/update/read/find/search/count/exists` 的入参契约未被破坏。
- [ ] Filter 支持 `= != > >= < <= like ilike not like in not in child_of parent_of` 和 `| & !` 波兰表达式。
- [ ] 自定义查询服务继续支持平台 Filter、分页、排序和字段选择，没有改成只接收固定字段。
- [ ] 自定义服务入参与视图 `args` 匹配。
- [ ] 查询服务支持分页、排序、过滤。
- [ ] 原生 SQL 使用 `%s` 参数化占位，不拼接用户输入；写 SQL 后清缓存。
- [ ] 服务编排、异步线程调用模型时有 Meta 上下文、默认分支、幂等和异常处理。
- [ ] 写服务校验业务状态和必填参数。
- [ ] 导入/导出/文件/打印等服务有明确失败提示。
- [ ] 第三方 API、多数据源、Redis、XXL-Job 有配置和异常处理。

## 权限、多租户与国际化

- [ ] 菜单权限、按钮权限、服务权限、行列权限和作用域策略一致。
- [ ] 多租户场景已确认租户可用 App、授权数量、租户隔离字段和作用域过滤。
- [ ] 使用 `ignoreScope`、白名单或跨租户查询时有明确业务理由和二次校验。
- [ ] 需要多语言时，语言包文件名符合 `messages_xx-XX.properties`，并已随 App Jar 打包。
- [ ] 自定义语料文件符合 `messages_user_defined_xx-XX.properties`，同 key 覆盖策略已说明。
- [ ] 菜单翻译 key 使用 `$menus.menuKey`，视图翻译 key 使用 `$views.viewKey`，异常提示可按 `model.service:errorKey` 组织。
- [ ] `messages_zh-CN.properties` 与目标语种语言包 key 集合一致，自动扫描生成的 key 未被手工改名。
- [ ] 子模块 POM 的 `scan-meta-langKey/supportLanguage/scanViewMetaKeys` 配置符合目标语种和视图字段范围。
- [ ] 用户可见异常不泄露 SQL、堆栈、密钥和内部类名。
- [ ] 数据多语言涉及缓存时，已配置后置服务或刷新策略。

## 测试、质量与版本

- [ ] 关键服务有 `@ExtendWith(SieEngineTestExtension.class)` 集成测试或明确的手工验证脚本，覆盖正常、边界、异常、权限、租户和幂等场景。
- [ ] 跨模型、跨 App、第三方 API、多数据源、文件、Excel、任务链路有集成验证说明。
- [ ] 性能敏感查询有分页、索引、数据量和响应时间验证。
- [ ] 已运行编译、JSON 校验、静态检查；能使用质量工具时补充 SpotBugs/静态分析结果。
- [ ] 提交消息、分支、合并、tag 和发版说明符合项目版本管理约定。

## 部署与环境

- [ ] `engine.run.mode` 的单机/分布式模式已明确。
- [ ] 开发、测试、部署环境的 JDK、数据库、Redis、MinIO 和配置文件差异已说明。
- [ ] 要求 Docker 部署时，`docker-compose.yml` 已包含 MySQL、Redis、MinIO、MinIO bucket 初始化和 `iidp-app` 服务。
- [ ] `Dockerfile` 指向真实启动 jar，默认是 `sie-iidp-demo-start/target/sie-iidp-demo-start-1.0-SNAPSHOT.jar`。
- [ ] Docker 专用配置位于 `docker/config/`，并使用 compose 服务名 `mysql`、`redis`、`minio`，未引用历史内网地址。
- [ ] 已执行 `docker compose config` 或说明无法执行的原因。
- [ ] 生产密码、Token、密钥未写入代码仓库，敏感配置通过环境变量或配置中心注入。
- [ ] 非 Dev 环境启动前已确认平台授权和租户授权要求。

## 静态命令

```bash
# 查看改动
git diff -- skills/backend iidp-backend-demo

# 检查空白错误
git diff --check

# 检查 JSON 文件能否解析
find iidp-backend-demo -name '*.json' -print0 | xargs -0 -n1 node -e 'JSON.parse(require("fs").readFileSync(process.argv[1],"utf8"))'

# 检查新增 view/data 是否登记
rg -n '"view"|"data"|"model"|"service"|"auth"' iidp-backend-demo/sie-iidp-demo-apps

# 检查 Docker Compose 配置
(cd iidp-backend-demo && docker compose config)
```

## 编译验证

能访问私服和依赖时运行：

```bash
mvn clean package -pl sie-iidp-demo-apps -am
```

若只改某个模块：

```bash
mvn clean package -pl sie-iidp-demo-apps/sie-iidp-demo-{appName} -am
```

## 运行验证

```bash
mvn spring-boot:run -pl sie-iidp-demo-start
```

启动后验证：

- [ ] 引擎加载新增 jar。
- [ ] 菜单可见。
- [ ] `loadView` 能加载目标视图。
- [ ] 列表查询、搜索、详情、新增、编辑、删除可用。
- [ ] 自定义按钮、导入导出、文件预览等按需可用。
