# IIDP 后端 SDD 规格模板

生成后端规格时，先读取 `skills/backend/SKILL.md`，再按能力域读取对应专题。本文只提供 `create-project` 侧的 SDD 输出结构，不替代 backend skill 的硬性规则。

**输出文件路径**：`specs/features/<phase>-<feature>/backend-spec.md`

```markdown
# [功能名称] IIDP 后端规格

## 1. 命名

| 项 | 值 | 说明 |
|---|---|---|
| appName | `[demo-xxx]` | 小写中划线 |
| appPkg | `[xxx]` | Java 包段，全小写 |
| moduleName | `[xxx]` | 业务模块目录 |
| model_name | `[app_entity]` | 小写下划线，全局唯一 |
| Java 类名 | `[EntityName]` | UpperCamelCase |
| 菜单 key | `[app_module_menu]` | 带业务前缀 |
| product | `[xxx]` | 产品线标识，写入 `app.json.product/category`，全小写 |
| productDesc | `[中文产品线名称]` | 产品线显示名，写入 `app.json.productDesc/categoryDesc` |
| productSequence | `[-100]` | 产品线在导航中的排序，数字越小越靠前，写入 `app.json.sequence` |

## 2. 工程文件

| 文件 | 新增/修改 | 说明 |
|---|---|---|
| `sie-iidp-demo-apps/pom.xml` | 修改 | 登记业务模块 |
| `sie-iidp-demo-apps/sie-iidp-demo-{appName}/pom.xml` | 新增 | 子模块 POM |
| `src/main/java/com/sie/iidp/{appPkg}/app.json` | 新增 | App 描述和资源登记 |
| `model/{ModelName}.java` | 新增 | 元模型 |
| `views/{model_name}_view.json` | 新增 | 后端视图 |
| `data/menus.json` | 新增/修改 | 菜单 |
| `apps/apps.json` | 修改 | 登记 jar |

> **生成 `app.json` 时，必须读取 `skills/backend/references/core/pom-structure.md` §app.json 章节获取完整字段定义**，不使用本文内联模板（避免字段漂移）。按 §1 命名表中的 `appName`、`appPkg`、`product`、`productDesc`、`productSequence` 填写对应字段。

## 3. 模型设计

| 字段 | Java 类型 | @Property 关键参数 | 必填 | @Validate 校验 | 索引 | 说明 |
|---|---|---|---|---|---|---|
| `[fieldName]` | `String/Long/Integer/Boolean/Date` | `displayName="[中文名]"` | 是/否 | `required/length/unique` | 是/否 | [说明] |
| `[dictField]` | `String` / `Integer` | `displayName="[中文名]"` | 是/否 | — | 否 | 配合 `@Selection` 或 `@Dict`；`widget` 仅在需要特定组件（如 `"radio-group"`）时才写，普通下拉不写 widget |
| `[dateField]` | `Date` | `displayName="[中文名]", dataType=DataType.DateTime, dateFormat="yyyy-MM-dd HH:mm:ss"` | 否 | — | 否 | 日期字段必须指定 dataType 和 dateFormat |
| `[erField]` | ManyToOne | `@ManyToOne` + `@JoinColumn(name="[col]")` | 是/否 | — | 是 | 同 App ER 关联 |

> **生成任何模型字段前必须先读取以下文件**：
> - `skills/backend/references/core/model.md`：`@Property` 完整参数（`store`、`readonly`、`dataType`、`dateFormat`、`widget`、`contentType`、`multiple`、`computeMethod`、`defaultMethod` 等）、`@Model` 声明、`@Selection`、`@Dict` 用法
> - `skills/backend/references/core/model-property-advanced.md`：`@ManyToOne`、`@OneToMany`、`@ManyToMany`、`@JoinColumn`、`@JoinTable` 等高级 ER 注解的完整参数和示例
>
> **不得凭记忆或推断填写注解参数**；对照以上文件中的实际代码示例生成，缺参数就补，错参数就改。

模型规则：
- 模型类使用 `@StaticVar @Getter @Setter @Model`，需要日志时加 `@Slf4j`。`@StaticVar`/`@Getter`/`@Setter` 是 **IIDP 平台元插件**（`com.sie.meta.plugin.*`），**不是 Lombok**，两者不可混用。
- 继承 `BaseModel<T>`。
- 业务字段必须有 `@Property(displayName = "...")`。
- 选项、字典、关联字段用 `@Selection`、`@Dict` 或 ORM 注解。
- 唯一编码、外键、高频过滤字段和排序字段要考虑索引。

**跨 App 外部模型引用（弱引用 + 冗余字段模式）**：

当需要引用其他 App 的模型（如产品、工作中心、组织）且不做强依赖时，使用此模式：

| 字段 | 类型 | 注解 | 说明 |
|---|---|---|---|
| `[externalId]` | `Long` | `@Property(displayName="[外部对象]ID")` | 存外部模型主键，弱引用 |
| `[externalName]` | `String` | `@Property(displayName="[外部对象]名称")` | 创建/编辑时从外部接口冗余，防止外部改名影响本表 |
| `[externalCode]` | `String` | `@Property(displayName="[外部对象]编码")` | 同上，按需冗余 |

- `app.json` 中声明 `"depends": [{"app": "[外部 appName]", "type": "weak"}]`
- `create`/`update` 服务负责读取外部数据并写入冗余字段；若外部服务不可用，要求调用方显式传入冗余字段
- 不使用 IIDP `@ManyToOne` 跨 App 关联（会引入强依赖）

## 4. 服务设计

| 服务 | 类型 | Java 方法签名 | 出参 | 权限 | 副作用 | 说明 |
|---|---|---|---|---|---|---|
| `search` | 内置 | `search(Filter, List<String> properties, int limit, int offset, String order)` | 列表 | `{model_name}:read` | 无 | 保留平台契约 |
| `create` | 内置/重写 | `create(List<Map<String,Object>> valuesList)` | `[ModelName]` | `{model_name}:create` | 写入主表 | [说明] |
| `update` | 内置/重写 | `update(RecordSet rs, Map<String,Object> values)` 或 `update(List<Map<String,Object>> valuesList)` | `[ModelName]` | `{model_name}:update` | 更新主表 | [说明] |
| `delete` | 内置/重写 | `delete(RecordSet rs)` | 无 | `{model_name}:delete` | 软删除 | [说明] |
| `[serviceName]` | 自定义 `@MethodService` | `[methodName](RecordSet rs, [业务入参...])` | `[result]` | `{model_name}:[auth]` | [副作用] | [说明] |

> **§4 完成后必须同步到 `sdd-contracts.md`**：每个自定义 `@MethodService` 完成后，将以下信息填入契约对应模型的服务契约表：
> 1. `service` 列：`@MethodService(name=...)` 的 `name` 值
> 2. `args 参数` 列：Java 方法签名中 **`RecordSet rs` 之后** 的每个业务参数，按 `参数名: 前端类型` 格式填写，多个用 ` / ` 分隔；`RecordSet` 本身不填
> 3. `权限码` 列：`@MethodService(auth=...)` 的值
>
> 类型映射规则见 `sdd-contracts.md` args 类型映射规则表。

> **副作用列**：列出服务执行成功后自动写入的字段、生成的关联记录、外部调用等。失败时由 IIDP 请求级事务统一回滚（见事务决策树）。

服务规则：
- 内置 CRUD 重写不得破坏平台入参契约。
- 自定义服务通过 `@MethodService` 暴露。
- 写服务必须校验状态、权限、作用域和必填参数。
- 原生 SQL 只在必要时使用，必须参数化。
- **三级异常规范**（来自 `sdd-contracts.md` §5）：
  - 字段级必填/长度/唯一 → 模型层 `@Validate`
  - 参数缺失或格式错误 → 抛 `ValidationException`（**不**触发事务回滚）
  - 状态不允许、权限不足、业务流程失败 → 抛 `ModelException`（触发请求级事务回滚）

**状态机服务（多服务状态流转）**：

业务对象有多个状态且每个流转对应独立操作时，为每个流转定义独立的 `@MethodService`，不合并为通用 `changeStatus`：

| 服务 | 类型 | 前置状态 | 后置状态 | 入参 | 权限 | 副作用 |
|---|---|---|---|---|---|---|
| `[action1]` | 自定义 | `[FROM]` | `[TO]` | `id: Long` | `{model_name}:[action1]` | [如：写 actualStartTime] |
| `[action2]` | 自定义 | `[TO]` | `[NEXT]` | `id: Long` | `{model_name}:[action2]` | [如：写 actualEndTime] |

> 完整状态机契约（状态 × 服务映射、按钮显示规则、异常分层）在 `sdd-contracts.md` 状态机章节维护，backend-spec.md 只声明服务清单和副作用，不重复状态机规则。

- 每个状态变更服务：**先查库当前状态 → 状态不符合抛 `ModelException`**（请求级事务自动回滚）
- `@MethodService(name="[action]", displayName="[中文操作名]", auth="{model_name}:[action]")`
- 视图按钮 `service` 字段使用 `@MethodService.name`，不使用 Java 方法名

**事务决策树（跨模型 / 跨 App 写操作）**：

```
同一 App，所有操作在一次请求内完成？
  ├─ 是 → IIDP 请求级事务（默认）
  │        失败抛 ModelException，整体回滚，无需额外处理
  │
  └─ 否
       ├─ 需要部分提交（如：草稿保存后发通知，通知失败不回滚主数据）？
       │    └─ 使用 Meta.flush()/commit() 手动分段提交
       │       在 backend-spec.md §4 说明注释中标注"分段提交：原因"
       │
       └─ 跨 App 写操作（如：工单确认 → 仓库扣料）？
            └─ 设计补偿服务 → 见下方"跨 App 写操作补偿声明"
```

**跨 App 写操作补偿声明**（跨 App 无统一事务，必须声明补偿服务）：

| 服务 | 挂载模型 | 调用的跨 App 操作 | 失败场景 | 补偿服务 | 幂等保证 |
|---|---|---|---|---|---|
| `[serviceName]` | `[主模型]` | `[外部 App].[服务]` | 外部调用失败 / 超时 | `[compensationService]`（逆向操作） | 入参携带业务唯一键，重试不产生重复数据 |

> 跨 App 写操作补偿服务在 Phase 1 可标记"待实现"并说明原因；但补偿策略必须在 backend-spec.md 中声明，不得推到实现阶段才考虑。

### 4.x 自定义服务详细设计

**必须写详细设计块的情况**：

| 情况 | 是否必须写 |
|---|---|
| 自定义 `@MethodService` | 必须 |
| 重写内置 `create`，加了编码生成/关联写入/外部调用等逻辑 | 必须 |
| 重写内置 `update`，加了状态校验/字段联动/副作用逻辑 | 必须 |
| 重写内置 `delete`，加了软删除/级联/前置校验逻辑 | 必须 |
| 内置 CRUD 完全不重写，无任何额外逻辑 | 省略 |

判断标准：**服务清单表"类型"列写了"内置/重写"，就必须写详细设计块；写"内置"且说明列为空或只写"保留平台契约"，则省略。**

````markdown
#### [serviceName]（[中文操作名]）

**入参校验**：

| 参数 | 类型 | 校验规则 | 不满足时 |
|---|---|---|---|
| `id` | `Long` | 非空 | 抛 `ValidationException("id 不能为空")` |
| `[param]` | `[类型]` | [规则] | 抛 `ValidationException("[提示]")` |

**查询逻辑**：

```
// 查主记录
[ModelName] record = meta.get("[model_name]").find(Filter.equal("id", id), ...)

// 关联查询（按需）
// 方式一：ER 字段直接访问（已有 @ManyToOne 关联时）
record.get[RelatedField]()

// 方式二：批量 find（无 ER 或跨模型时）
List<?> children = meta.get("[child_model]").find(
    Filter.equal("[fk_field]", id), null, null, null, null)

// 方式三：N+1 禁止，超过 1 条记录时批量查询后在内存组装
List<Long> ids = records.stream().map(...).collect(...)
List<?> related = meta.get("[model]").find(Filter.in("id", ids), ...)
```

**业务步骤**：

1. 状态校验：`record.status != [EXPECTED_STATUS]` → 抛 `ModelException("[提示]")`
2. 业务规则校验：[如：数量不得为负、编码不能重复]
3. 主操作：[写入哪些字段，如 `status = SUBMITTED`、`submitTime = now()`]
4. 关联操作：[如：批量更新子表、写入关联记录]
5. 外部调用（如有）：[调用跨 App 服务，失败策略]
6. 返回：[返回值说明，如更新后的记录或 void]

**异常分层**：

| 场景 | 异常类型 | 是否回滚 |
|---|---|---|
| 必填参数缺失 | `ValidationException` | 否 |
| 前置状态不符 | `ModelException` | 是 |
| 业务规则违反 | `ModelException` | 是 |
| 外部服务失败 | `ModelException` | 是（补偿见上方） |

**事务说明**：IIDP 请求级事务（默认）/ 分段提交（原因：[...]）
````

> **填写规则**：每个 `@MethodService` 对应一个详细设计块；顺序与服务清单表一致。查询逻辑重点说明关联方式（ER 字段 / 批量 find / 禁止 N+1），业务步骤用编号列出操作顺序，不需要写 Java 语法，伪代码即可。
>
> **规范来源（生成代码时必须对照）**：
> - 查询逻辑 / N+1 禁止：`skills/backend/references/core/platform-standards.md` §ER 查询
> - Filter 构建（equal/in/like/and 等）：`skills/backend/references/core/method-service.md` → "Filter 常用构建"
> - ValidationException / ModelException 分层：`skills/create-project/references/sdd-contracts.md` §5 异常与事务
> - 事务 / 分段提交（Meta.flush/commit）：`skills/backend/references/core/method-service.md` → 事务控制
> - @MethodService 声明与重写 create/update/delete：`skills/backend/references/core/method-service.md`

## 5. 视图和菜单

视图：

> **生成任何视图 JSON 前必须先读取 `skills/backend/references/core/view.md`**，获取 grid/search/form/tree 各类型视图的完整 JSON 结构、tbar、columns、buttons、search fields、body 嵌套层级和实际示例。不使用本文内联字段表（避免结构漂移）。

标准三视图 key（key 命名规则以 `view.md` 为准）：

| 视图 key | type | mode | 说明 |
|---|---|---|---|
| `{model_name}_grid` | `grid` | `primary` | 列表 |
| `{model_name}_search` | `search` | `primary` | 搜索 |
| `{model_name}_form` | `form` | `primary` | 表单 |

菜单：

> **生成 `menus.json` 前必须先读取 `skills/backend/references/core/menu.md`**，获取完整 JSON 字段定义、根菜单结构、子菜单结构和实际示例。不使用本文内联字段表（避免格式漂移）。以下为字段速查，**实际 JSON 结构以 `menu.md` 为准**：

| 字段 | 必填 | 说明 |
|---|---|---|
| `name`（key） | 是 | 全局唯一，建议以 appPkg 开头，如 `{appPkg}_{entity}_menu` |
| `display_name` | 是 | 菜单显示名 |
| `sequence` | 是 | 同级排序，数字越小越靠前 |
| `active` | 是 | 是否启用，通常 `true` |
| `model` | 功能菜单必填 | 与 Java `@Model(name)` 一致 |
| `view` | 功能菜单必填 | 逗号连接多个视图 key |
| `parent_ids` | 子菜单必填 | `{ "@ref": "{appPkg}_{moduleName}_root_menu" }` |

操作栏按钮（从 §4 服务设计表提取）：

> **生成按钮 JSON 时必须对照 `view.md` 按钮结构**，获取 `action`、`service`、`auth`、`args`、`bind_ids`、`actionAfter`、`bind_display` 等完整属性和示例。以下为速查，**实际按钮 JSON 结构以 `view.md` 为准**：

| 按钮 | action | service | auth | 位置 |
|---|---|---|---|---|
| 新增 | `create` | —（内置，无需 service） | `create` | tbar |
| 删除 | `delete` | —（内置，无需 service） | `delete` | tbar |
| 详情 | `preview` | —（内置，无需 service） | `read` | buttons（行操作） |
| 编辑 | `edit` | —（内置，无需 service） | `update` | buttons（行操作） |
| [自定义操作] | `[自定义 action]` | `[serviceName]` | `{model_name}:[auth]` | tbar 或 buttons |

> **规则**：内置操作（create/delete/preview/edit）只需 `action` + `auth`，不写 `service`；自定义操作必须同时有 `action`、`service`、`auth`，其中 `service` 值必须与 §4 `@MethodService(name=...)` 完全一致。`auth` 值在同一视图内必须唯一。

**前端实现决策（详情页）**：

| 详情页需求 | Phase 1 处理方式 | Phase 2 处理方式 |
|---|---|---|
| 标准字段查看/编辑 | IIDP 标准 form 视图，无需前端代码 | — |
| 状态步骤条（步骤条组件） | 标记为 Phase 2，Phase 1 用状态字段 Tag 代替 | 前端扩展视图插入步骤条节点 |
| 左右分栏布局 + 操作日志 | 标记为 Phase 2 | 扩展视图或自定义 Vue2 组件 |
| 内联编辑/只读切换（行内 toggle） | 标记为 Phase 2 | hook 控制字段 `disabled` 状态 |

> 遇到复杂详情页布局时，先在 backend-spec.md §5 末尾添加"**附：遗留问题（Phase 2）**"表格记录，不在 Phase 1 规格中展开，避免规格超出交付范围。

## 6. 数据和权限

- 种子数据和字典：

> **生成 `data/*.json` 前必须先读取 `skills/backend/references/core/seed-data.md`**，获取业务种子数据（零节：字典 `base_dict_type`/`base_dict_value`；一节：普通业务数据审计字段、树形 parentId 写法）、`policy` 枚举值、`@ref`/`@fileId`/`@fileUrl` 引用语法和完整示例。不使用本文内联结构提示（避免格式漂移）。
>
> - **字典种子格式**：使用 `base_dict_type` + `base_dict_value` 两条记录，均置于顶层 `"data"` 对象下。**不存在 `"dicts"` 顶层 key**，与 `seed-data.md` 零节对齐。
> - **速查**：业务种子文件为 `data/{model_name}.json`；字典文件为 `data/yes_no.json` 等独立文件或 `data/{type}_dict.json`。**实际 JSON 结构以 `seed-data.md` 为准**。
- 附件：
  - 文件索引：`file/{file_key}.json`（`"file"` 顶层 key）；实际文件：`file/document/{path}`
  - 种子引用（字符串形式，非对象）：`"@fileId({file_key})"` / `"@fileUrl({file_key})"` / `"@filePath({file_key})"`
- **权限码汇总**（从 §4 服务设计表的 `权限` 列聚合，格式 `{model_name}:{action}`）：
  - 菜单权限：`{model_name}:read`
  - 按钮/服务权限：`{model_name}:create` / `{model_name}:update` / `{model_name}:delete` / `[自定义 auth]`
  - > 完整定义和角色映射放在 `integration-map.md` 权限码总览；本规格只声明本模型用到的权限码，不重复定义角色映射。
- 多租户/作用域：
- 多语言：

> **权限码单一事实来源原则**：权限码的完整定义（含义、角色映射、涉及模型）只在 `integration-map.md`（项目级总览）和 `sdd-contracts.md`（前后端契约）中维护。本后端规格只引用权限码，不重复定义，避免三处不一致。

## 7. 验收

- [ ] `app.json` 路径与 `resolved` 一致
- [ ] `app.json.view/data` 登记完整
- [ ] `apps/apps.json` 登记新增 jar
- [ ] Java `@Model(name)` 与视图、菜单一致
- [ ] JSON 文件可解析
- [ ] 能编译或已说明无法编译的原因
- [ ] §8.1–8.4 规格-产物对照表已填写，无空行、无 ❌ 项
- [ ] §8.5 自定义服务实现核查已填写，所有 @MethodService 均有对应核查组，无 ❌ 项

> 完成 §8 对照表后，按 `skills/create-project/references/sdd-validation.md` 执行 AC → TC 提取，将测试用例写入当前 feature 的 `validation.md`，并满足 Phase 完成门控（所有 TC-BE/TC-FE 无 ⬜ 待执行）。
> 交付前静态自检见 `skills/backend/references/core/validation-checklist.md`。

## 8. 规格-产物对照表（生成后必填）

> **填写规则**：生成完所有产物后逐行填写。"一致"列填 ✅（一致）或 ❌（不一致，需立即修正），不得留空或标"待确认"。发现 ❌ 必须在提交前修正并重新填 ✅。

### 8.1 字段对照（§3 → Java @Property）

| §3 字段名 | Java @Property name | 类型一致 | 注解一致 |
|---|---|---|---|
| `[fieldName]` | `[fieldName]` | ✅/❌ | ✅/❌ |

### 8.2 服务对照（§4 → Java @MethodService / 内置服务）

| §4 服务名 | Java 方法 / 内置 | auth 码与 §4 一致 |
|---|---|---|
| `[serviceName]` | `@MethodService(name="[serviceName]")` / 内置 | ✅/❌ |

### 8.3 按钮对照（§5 buttons → §4 服务 → §6 权限码）

| 按钮 service | §4 中存在 | auth 码 | §6 中存在 |
|---|---|---|---|
| `[serviceName]` | ✅/❌ | `[model_name]:[auth]` | ✅/❌ |

### 8.4 视图与菜单登记对照（§5 → app.json）

| 视图 key | app.json.view 已登记 | 菜单 view 字段引用 |
|---|---|---|
| `[model_name]_grid` | ✅/❌ | ✅/❌ |
| `[model_name]_search` | ✅/❌ | ✅/❌ |
| `[model_name]_form` | ✅/❌ | ✅/❌ |

### 8.5 自定义服务实现核查（每个 @MethodService 一组）

> 参照 `skills/backend/references/core/method-service.md` 的实际代码模式逐项核查，不得使用业务具体名称作占位符。

**通用核查项（每个自定义服务必查）**：

| 核查项 | 标准（来自 method-service.md） | 确认 |
|---|---|---|
| 注解 name 与视图 service 一致 | `@MethodService(name="[serviceName]")` 中的 `name` 值 == 视图 button `"service": "[serviceName]"` | ✅/❌ |
| 方法签名与服务类型匹配 | 重写 create → `(List<Map<String,Object>> valuesList)`；重写 update → `(RecordSet rs, Map<String,Object> values)` 或 `(List<Map<String,Object>> valuesList)`；业务方法 → `(RecordSet rs, [业务入参])`；计算字段 → `(Map<String,Object> valMap)` | ✅/❌ |
| 异常处理结构正确 | `catch (ModelException\|ValidationException e){throw e;}` + `catch(Exception e){throw new ValidationException("...")}` | ✅/❌ |
| §4 副作用字段全部赋值 | §4 服务行"副作用"列声明的字段，在方法体中能找到对应赋值语句 | ✅/❌ |

**状态机服务额外核查项**（仅状态变更类 @MethodService）：

| 核查项 | 标准 | 确认 |
|---|---|---|
| 从 DB 查当前记录 | 用 `DbUtils.search(Filter.equal("id", id), ...)` 或 `rs.callSuper(null, MethodConst.FIND, ...)` 查库，**不**用入参中的状态字段 | ✅/❌ |
| 前置状态校验抛 ModelException | `if (!前置状态.equals(record.getStatus())) throw new ModelException("...")` | ✅/❌ |
| 后置状态字段显式赋值 | `values.put("status", [后置状态常量])` 或直接 `record.setStatus(...)` | ✅/❌ |

**跨模型/跨 App 服务额外核查项**：

| 核查项 | 标准 | 确认 |
|---|---|---|
| 使用平台 RPC 调用 | `getMeta().get("[target_model_name]").call("[serviceName]", ...)` | ✅/❌ |
| 跨 App 调用有补偿声明 | §4 跨 App 补偿声明表中已登记（允许 Phase 1 标"待实现"） | ✅/❌ |
```

## 多模型业务规格

业务涉及多个模型时（如主表 + 子表、主流程 + 关联记录），**必须在规格中枚举所有模型**，不得只生成第一个模型后停止。

### ER 关系总览（多模型时必写）

在命名表之后，先列出模型清单和关系，再逐一展开每个模型的完整规格：

```markdown
## ER 关系总览

| 模型 | Java 类名 | model_name | 说明 | 与其他模型的关系 |
|---|---|---|---|---|
| [主模型] | `[MainEntity]` | `[app_main]` | [说明] | 一对多 → [SubEntity] |
| [子模型] | `[SubEntity]` | `[app_sub]` | [说明] | ManyToOne → [MainEntity] |
| [关联模型] | `[RelEntity]` | `[app_rel]` | [说明] | ManyToOne → [MainEntity] |
```

### 逐模型规格展开

ER 关系确认后，**按依赖顺序**（被引用的模型先写）逐一展开每个模型的第 3～7 节规格。每个模型独立形成一份完整规格块：

```markdown
---
## 模型 N：[ModelName]

### 3. 模型设计
| 字段 | 类型 | IIDP 注解 | 必填 | 校验 | 索引 | 说明 |
|---|---|---|---|---|---|---|
| `[fieldName]` | `[String/Date/...]` | `@Property` | 是/否 | `@Validate` | 是/否 | [说明] |
| `[foreignKey]` | ManyToOne | `@ManyToOne` | 是 | — | 是 | 关联 [父模型] |

### 4. 服务设计
...（同单模型模板）

### 5. 视图和菜单
...

### 6. 数据和权限
...

### 7. 验收
...

### 8. 规格-产物对照表
...（同单模型模板，按本模型字段/服务填写）
---
```

### 跨模型自定义服务

涉及多个模型的业务流程服务（如"提交审批"需要同时操作主表和关联记录），规格中必须说明：

| 服务 | 挂载模型 | 涉及模型 | 事务边界 | 入参 | 校验项 |
|---|---|---|---|---|---|
| `[serviceName]` | `[主模型]` | `[主模型], [关联模型]` | IIDP 请求级事务（抛 `ModelException` 自动回滚） | `[args]` | 状态、权限、必填、作用域 |

## 审核重点

- **§8.1–8.4 规格-产物对照表是否已填写，无 ❌ 项**（字段、服务、按钮、视图/菜单全部对齐）。
- **§8.5 自定义服务实现核查是否已填写**，覆盖所有 @MethodService；状态机服务核查了"从 DB 查状态"和"抛 ModelException"；跨模型服务核查了 `getMeta().get(...).call(...)` 模式。
- 后端规格是否先确认 `appName`、`appPkg`、`moduleName`、`model_name`、菜单 key。
- **多模型业务是否有 ER 总览表，且每个模型都有独立的完整规格块。**
- **跨模型服务是否说明挂载模型、涉及模型、事务边界和校验项。**
- 服务表格是否有**副作用列**，副作用（自动写字段、生成关联记录、外部调用）已明确声明。
- **有状态机的模型是否输出状态 × 服务映射表**（含前置状态、后置状态、系统副作用）。
- **跨 App 写操作是否有补偿服务声明**（补偿服务、幂等保证、失败场景），不得推到实现阶段。
- 事务边界是否经过决策树判断（请求级 / Meta 分段提交 / 跨 App 补偿），不得默认用请求级事务处理跨 App 写。
- 是否把视图、菜单、种子数据、附件、jar 登记到对应清单。
- 自定义服务是否保留 JSON-RPC、Filter、分页、排序和字段选择契约。
- 写服务三级异常是否对齐：`@Validate` / `ValidationException` / `ModelException`。
- 权限是否同时覆盖菜单、按钮、服务，不依赖前端显隐兜底。
