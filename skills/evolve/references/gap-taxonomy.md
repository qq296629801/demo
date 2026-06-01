# create-project 已知 Gap 分类（G1–G8）

> 基于实际测试发现的缺陷模式，用于 `/evolve-gap-detect` 的结构化检测。
> 每个 Gap 包含：根因、检测方式、对应 skill 文档、已修复状态。

---

## G1 — 模型设计缺陷

### G1-1：ID 类型错误（Long 替代 String）

- **根因**：模板示例或旧文档中 id 类型为 Long，LLM 跟随示例生成错误类型
- **平台规范**：IIDP 雪花算法生成的 id 是 String，所有字段（包括入参 id、外键 id、`List<String> ids`）均为 String
- **检测**：在生成的 `backend-spec.md` 中搜索 `Long id`、`List<Long>`
- **对应文件**：`sdd-backend.md` §3 模型表、§4 入参校验表
- **修复状态**：✅ 已在 sdd-backend.md 修复

### G1-2：审计字段手动声明

- **根因**：AI 把 create_user/create_date/update_user/update_date 当普通字段处理
- **平台规范**：`@Model(isAutoLog = Bool.True)` 自动维护，**禁止**在模型类中手动声明
- **检测**：在 backend-spec.md 字段表中搜索 `create_user`、`create_date`、`update_user`、`update_date`
- **对应文件**：`sdd-backend.md` §3 模型规则
- **修复状态**：✅ 已在 sdd-backend.md 修复

### G1-3：ManyToOne 未成对声明

- **根因**：模板只展示 ORM 对象字段，缺少 FK String 字段
- **平台规范**：每个 ManyToOne 关系必须同时声明：① FK String 字段（存库）② ORM 对象字段（不存库）
- **检测**：在 backend-spec.md 字段表中，若有 `@ManyToOne` 行但无对应 `@Selection(model=...)` String 行
- **对应文件**：`sdd-backend.md` §3 模型表
- **修复状态**：✅ 已在 sdd-backend.md 修复

### G1-4：字段驼峰大小写不一致

- **根因**：`set()`/`getStr()` 中的字符串与 `private` 字段声明大小写不同（如 `subClass` vs `subclass`）
- **检测**：在生成代码中搜索 `.set("` 和 `.getStr("` 后的字符串，逐一对比字段声明
- **对应文件**：`sdd-backend.md` §3 模型规则
- **修复状态**：✅ 已在 sdd-backend.md 修复

### G1-5：ManyToMany 缺失完整示例

- **根因**：模板没有 ManyToMany 行，AI 只会写 ManyToOne
- **检测**：当需求中有多对多关系时，backend-spec.md 字段表缺少 `@ManyToMany` + `@JoinTable` 双字段行
- **对应文件**：`sdd-backend.md` §3 模型表
- **修复状态**：✅ 已在 sdd-backend.md 补充 OneToMany 和 ManyToMany 模板行

---

## G2 — 服务方法缺陷

### G2-1：缺少服务分层决策

- **根因**：AI 直接写所有逻辑到模型类，不判断是否需要拆分到 `SdkService<T>`
- **检测**：backend-spec.md §4 详细设计中，复杂服务（> 40 行 / 涉及 ≥ 3 模型）未标注"拆分到 service 层"
- **对应文件**：`sdd-backend.md` §4 服务分层决策表
- **修复状态**：✅ 已有完整决策表（强制/建议/保留三档）

### G2-2：跨 App 写操作缺少补偿声明

- **根因**：跨 App 无统一事务，但 AI 经常忽略补偿服务设计
- **检测**：backend-spec.md 是否有"跨 App 写操作补偿声明"表；若有跨 App 调用但无此表则为 Gap
- **对应文件**：`sdd-backend.md` §4 跨 App 补偿声明
- **修复状态**：✅ 模板已有补偿声明表

### G2-3：分页查询返回结构不规范

- **根因**：自定义查询服务返回结构不统一（有的返回 List，有的返回 Map，有的返回自定义 DTO）
- **平台规范**（来源：`api-params.md` §2）：分页入参为 `args.limit`（默认 31）/ `args.offset`（默认 0），响应为 `result.data`（数组）；不存在 `pageNum`/`pageSize` 入参，也不存在 `{total, list}` 返回 Map
- **检测**：backend-spec.md §4 中是否出现 `pageNum`/`pageSize` 入参或 `Map{total,list}` 返回结构；有则为 Gap
- **对应文件**：`sdd-backend.md` §4 查询逻辑
- **修复状态**：✅ 已在 sdd-backend.md §4 修正分页参数（pageNum/pageSize → limit/offset）和返回结构

---

## G3 — 视图设计缺陷

### G3-1：视图 key 命名不一致

- **根因**：grid/search/form 视图 key 命名风格随机（有时用 camelCase，有时用 snake_case）
- **平台规范**：视图 key 格式为 `{model_name}_grid`、`{model_name}_search`、`{model_name}_form`（小写下划线）
- **检测**：contracts.md 中视图 key 列的命名是否与 `{model_name}` 前缀一致
- **对应文件**：`sdd-backend.md` §5 视图设计

### G3-2：Grid/Search/Form 未配对声明

- **根因**：只声明了 grid 视图，没有配套的 search 和 form
- **检测**：backend-spec.md §5 中每个实体是否有完整的三视图（grid + search + form）
- **对应文件**：`sdd-backend.md` §5

---

## G4 — 前端交互规格缺陷

### G4-1：仅生成列表/表单页，缺少详情页/审批页

- **根因**：Prompt 8 默认只生成 list + form，缺少 detail/workflow/dashboard 触发条件
- **检测**：实体有 status 字段或 OneToMany 关系时，`spec/09-ui/` 下是否存在 `{entity}-detail.html`
- **对应文件**：`code-index/references/llm-prompts.md` Prompt 8
- **修复状态**：✅ 已增强 Prompt 8（新增 detail/workflow/dashboard 3 种页面类型）

### G4-2：按钮级权限控制未在前端规格中声明

- **根因**：sdd-frontend.md §8 操作表未明确列出每个按钮的权限码
- **检测**：frontend-spec.md §8 中操作表的"触发条件"列是否包含 `{model_name}:action` 格式的权限码
- **对应文件**：`sdd-frontend.md` §8
- **修复状态**：✅ 已在 sdd-frontend.md §8 补充按钮级权限控制规范表

---

## G5 — 状态流转缺陷

### G5-1：状态枚举不完整

- **根因**：只声明部分状态（如 PENDING/APPROVED），遗漏中间态（如 IN_REVIEW/CANCELLED）
- **检测**：contracts.md 状态机表中的状态值是否覆盖了需求中的所有业务场景
- **对应文件**：`sdd-backend.md` §4 状态机服务

### G5-2：审批流程服务缺失

- **根因**：有状态字段的实体，AI 只生成 CRUD 不生成 approve/reject 等状态变更服务
- **检测**：实体有 auditStatus/status 字段时，backend-spec.md 是否有独立的状态变更服务（非通用 changeStatus）
- **对应文件**：`sdd-backend.md` §4 状态机服务

---

## G6 — 权限控制缺陷

### G6-1：权限码未在 contracts.md 中定义

- **根因**：服务清单中的权限码只是推断，未在 contracts.md 服务契约表中明确声明
- **检测**：backend-spec.md 服务清单的"权限码"列是否与 contracts.md 服务契约表一一对应
- **对应文件**：`sdd-contracts.md` 服务契约表

### G6-2：菜单权限未挂载到 menus.json

- **根因**：生成的 menus.json 缺少 `permission` 字段或权限码与服务不对应
- **检测**：menus.json 中每个功能菜单是否有正确的 permission 值
- **对应文件**：`sdd-backend.md` §6 菜单设计

---

## G7 — 数据校验缺陷

### G7-1：字段校验规则缺失

- **根因**：模型字段只有 `@Property` 没有 `@Validate.*`，导致必填/长度/格式校验缺失
- **检测**：backend-spec.md 字段表的"@Validate 校验"列是否为空（必填字段不应为空）
- **对应文件**：`sdd-backend.md` §3 模型表

### G7-2：唯一性约束缺失

- **根因**：需要唯一的字段（如编码、名称）没有 `@Validate.Unique`
- **检测**：需求中标注"唯一"的字段是否有对应的 `@Validate.Unique` 注解
- **对应文件**：`sdd-backend.md` §3 模型表

---

## G8 — 接口设计缺陷

### G8-1：自定义服务破坏平台 JSON-RPC 契约

- **根因**：重写 create/update/delete 时改变了入参结构，与平台 JSON-RPC 规范不符
- **平台规范**：`create(List<Map<String,Object>> valuesList)`，`update(RecordSet rs, Map<String,Object> values)`，`delete(RecordSet rs)` 签名不可改变
- **检测**：backend-spec.md §4 中内置服务重写是否保留了平台标准入参签名
- **对应文件**：`sdd-backend.md` §4 服务规则

### G8-2：查询服务缺少 Filter/Sort/Fields 支持

- **根因**：自定义查询服务不支持 IIDP 平台标准的 Filter/Sort/Fields 参数
- **检测**：contracts.md 中查询类服务是否声明支持平台标准 Filter 对象
- **对应文件**：`sdd-backend.md` §4 服务规则

---

## Gap 检测优先级

| 优先级 | Gap | 频率 | 影响 |
|---|---|---|---|
| P0（最高） | G1-1 ID 类型 | 高频 | 运行时类型错误 |
| P0 | G1-2 审计字段 | 高频 | 平台自动字段冲突 |
| P0 | G1-3 ManyToOne 未成对 | 高频 | ER 关联失效 |
| P1 | G4-2 按钮权限缺失 | 中频 | 前端权限控制失效 |
| P1 | G2-3 分页返回结构 | 中频 | 前端分页渲染错误 |
| P1 | G5-2 审批服务缺失 | 中频 | 工作流无法执行 |
| P2 | G6-1 权限码未定义 | 低频 | 权限管理混乱 |
| P2 | G8-1 平台契约破坏 | 低频 | 服务调用失败 |
