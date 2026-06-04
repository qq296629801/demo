# 调试速查导航

遇到问题时，先按症状找到对应分类，再读指定文件的对应章节，不要直接猜答案。

## 通用调试步骤

```
1. 完整贴出错误信息/日志（不要只贴最后一行）
2. 对照下方分类表，确定症状属于哪一类
3. 读对应 skills 文件的指定章节
4. 若仍无解 → 读 validation-checklist.md 完整故障速查表
5. 问题解决后 → 将新问题和解法追加到本文件对应分类（知识回流）
```

---

## A. 环境与部署类

| 症状 | 可能原因 | 先读这个文件 |
|------|----------|-------------|
| `docker compose up` 报端口冲突 | 本地端口占用 | `pom-structure.md` § Docker 配置 |
| iidp-app 容器启动后立即退出 | jar 路径错误 / 配置文件缺失 | `pom-structure.md` § Dockerfile；`validation-checklist.md` § 部署与环境 |
| MySQL / Redis / MinIO 连接失败 | docker-compose.yml 与 application.properties 配置不一致 | `validation-checklist.md` § 部署与环境（配置一致性检查表） |
| `docker compose config` 报错 | YAML 格式或变量替换问题 | `pom-structure.md` § Docker Compose 模板 |
| Maven 编译报私服依赖下载失败 | settings.xml 未配置或私服不可达 | `CLAUDE.md` § Maven 构建命令 |
| Maven 编译报 Java 版本不兼容 | JDK 版本不匹配（主工程要求 Java 8） | `platform-standards.md` § 环境与配置 |
| 引擎启动后日志无限报错循环 | apps.json 登记了不存在的 jar | `validation-checklist.md` § 文件登记 |

---

## B. Token 与鉴权类

| 症状 | 可能原因 | 先读这个文件 |
|------|----------|-------------|
| JSON-RPC 返回 `{"code":7100,"message":"token不能为空"}` | Authorization 头未传递或格式错误 | `api-filter-sql.md` § Token 获取与鉴权 |
| 不知道怎么获取 superuser token | 从数据库查询即可 | `skills/create-project/references/sdd-validation.md` § Token 获取方式（SQL：`SELECT token FROM rbac_token WHERE id = 'rbac_token_superuser'`） |
| 冒烟测试所有用例都返回 token 错误 | token 过期或环境变量 `IIDP_API_TOKEN` 未设置 | `skills/evolve/references/smoke-validation.md` § 认证要求 |
| 前端页面登录后跳转失败 | httpOnly token 被拦截 | `skills/frontend/references/iidp-frontend-dev-manual/iidpDoc/03.前端开发手册/010.业务场景/02.第三方登录.md` |

**Token 快速获取命令**：
```bash
mysql -u iidp -piidp123456 -h localhost iidp_demo -N -e \
  "SELECT token FROM rbac_token WHERE id = 'rbac_token_superuser' LIMIT 1"
```
请求头格式：`Authorization: Bearer <token>`

---

## C. 冒烟测试类

| 症状 | 可能原因 | 先读这个文件 |
|------|----------|-------------|
| 不知道如何启动完整冒烟测试 | — | `skills/create-project/references/sdd-validation.md` § 冒烟测试步骤（依赖启动 → 构建 → 启动 app → 运行 smoke_test.py） |
| `smoke_test.py` 报导入错误 | Python 依赖缺失 | `skills/create-project/references/sdd-validation.md` § 冒烟测试脚本 |
| 所有用例都失败（非 token 问题） | 应用未完全启动或请求格式错误 | `skills/evolve/references/smoke-validation.md` § 失败分类（startup-gap / smoke-gap 区分） |
| 冒烟用例部分通过部分失败 | 生成代码不完整或 Filter 表达式错误 | `skills/evolve/references/smoke-validation.md` § 失败分类（generation-gap / spec-gap） |
| JSON-RPC 返回 `result:null` 且 `error` 有内容 | 服务调用失败，查 `error.data.debug` 字段 | `api-filter-sql.md` § 响应结构；`skills/create-project/references/sdd-validation.md` § 响应体结构 |
| 不知道冒烟覆盖范围要求 | — | `skills/evolve/references/smoke-validation.md` § JSON-RPC 用例要求（最低覆盖 search/create/update/delete + 1 个负向用例） |

**冒烟测试标准启动流程**：
```bash
# 1. 启动依赖
docker compose up -d mysql redis minio minio-init
# 2. 构建应用
mvn -s ./settings.xml -DskipTests clean package
# 3. 启动引擎
docker compose up -d iidp-app
# 4. 获取 token 后运行冒烟脚本
IIDP_API_TOKEN=<token> python tests/functional/smoke_test.py
```

---

## D. 数据与模型类

| 症状 | 可能原因 | 先读这个文件 |
|------|----------|-------------|
| JSON-RPC search 返回空数据（数据应存在） | Filter 表达式错误 | `api-filter-sql.md` § Filter 规范 |
| 模型字段在视图中不显示 | `@Property` 缺失，或 `store=false` 字段写入了种子数据 | `model.md` § Property 注解；`seed-data.md` § 注意事项 |
| 种子数据导入失败 | `model` 字段与 `@Model(name)` 不一致，或 `@ref` 目标不存在 | `seed-data.md` § 结构规范 |
| ManyToOne 关联字段查询为空 | 外键字段名与 `@JoinColumn(name)` 不匹配 | `seed-data.md` § ManyToOne 写法 |
| ManyToMany 关联数据未写入 | `@eval` 数组格式不正确 | `seed-data.md` § @eval 语法 |
| N+1 查询导致性能问题 | 关联关系未配置 fetch 策略 | `platform-standards.md` § 模型与 App 设计规范 |

---

## E. 视图与菜单类

| 症状 | 可能原因 | 先读这个文件 |
|------|----------|-------------|
| 视图 JSON 加载报 404 | `app.json.view` 数组未登记该路径 | `validation-checklist.md` § 文件登记；`app-json.md` |
| 菜单不显示 | `app.json.data` 未登记菜单文件，或菜单 key 冲突 | `menu.md`；`validation-checklist.md` § 文件登记 |
| app.json 无法加载 | `resolved` 包路径与实际 Java 包路径不一致 | `app-json.md` § 路径规则 |
| jar 未被引擎加载 | `apps/apps.json` 的 `apps.SDK` 未添加新模块 | `validation-checklist.md` § 文件登记 |
| 按钮点击无响应 | `@MethodService` 未声明，或权限码缺失 | `method-service.md`；`security-permission-i18n.md` |
| 子表数据不关联 | tabs 的 `body.field` 与 Java ER 字段名不一致 | `view-advanced.md` § 子表配置 |

---

## F. 测试与 TDD 类

| 症状 | 可能原因 | 先读这个文件 |
|------|----------|-------------|
| `@DDTest` 断言失败 | expected 格式与实际响应不一致 | `testing.md` § @DDExpected 规范 |
| 集成测试运行报 Spring 上下文错误 | 缺少 `@ExtendWith(SieEngineTestExtension.class)` | `testing.md` § 基础配置 |
| 测试通过但功能仍然错误 | 测试未覆盖正确场景（警示信号） | `tdd.md` § 7 条警示信号 |

---

## G. 前端类（快速索引）

前端问题直接进入以下路径查阅：

| 场景 | 文件路径（相对 `skills/frontend/references/iidp-frontend-dev-manual/iidpDoc/`） |
|------|----------------|
| 常见报错 | `07.平台常见问题/02.前端开发常见问题/03.前端常见报错.md` |
| 常见问题 | `07.平台常见问题/02.前端开发常见问题/01.前端常见问题.md` |
| 调试工具（8 大模块） | `03.前端开发手册/015.效率与调试/02.调试工具.md` |
| AI 提示词（7 场景） | `03.前端开发手册/015.效率与调试/03.结合AI工具.md` |
| 前端环境准备 | `03.前端开发手册/02.快速上手/01.环境准备.md` |
| 前端工程运行 | `03.前端开发手册/05.工程说明/03.工程运行.md` |
| 第三方登录 / Token | `03.前端开发手册/010.业务场景/02.第三方登录.md` |
