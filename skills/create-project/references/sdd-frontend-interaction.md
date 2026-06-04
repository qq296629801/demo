# IIDP 前端交互规格

本文定义 SDD 中 `interaction-spec.md` 的生成规则。它不是泛 UI 文案，也不是替代 `frontend-spec.md` 的代码规格；它负责把**用户如何操作、页面如何变化、状态如何流转、数据源如何触发、验收如何判断**写清楚，并保留 IIDP 落地所需的关键锚点。

---

## 与其他 SDD 产物的分工

| 产物 | 负责什么 | 本文是否替代 |
|---|---|---|
| `requirements.md` | 用户故事、功能需求、验收意图 | 否 |
| `contracts.md` | app/model/service/args/auth/view/menu 契约 | 否 |
| `frontend-spec.md` | 前端实现分支、文件、节点树、selector、hook、ds_config、代码规则 | 否 |
| `interaction-spec.md` | 用户流程、页面状态、事件触发、数据变化、失败路径、可用性验收 | 是，本文定义 |
| `validation.md` | 测试用例、验收执行和结果 | 否 |

`interaction-spec.md` 可以引用 selector、hook、数据源和服务契约，但不写完整代码。技术细节以 `frontend-spec.md` 为准；交互行为以 `interaction-spec.md` 为准。

---

## 何时必须生成

以下情况必须生成 `interaction-spec.md`：

- 页面包含新增、编辑、删除、导入、导出、状态流转、审批、批量操作。
- 页面包含弹窗、抽屉、openView、主子表、树表、上下表、tab。
- 需要 hook 改写查询、保存、删除、权限、表单初始化或子表行为。
- 前端扩展涉及 `bind_on_*`、`commands`、`ds_config.reqPrep/reqAfter`。
- 需求涉及响应式、可访问性、键盘操作、加载/空态/错误态。
- 存量项目中需要解释已有前端扩展或和 `baseline-spec/frontend-extends.json` 对齐。

不需要生成：

- 纯后端数据模型变更且没有页面行为变化。
- 标准模板默认 CRUD 且无特殊交互，`frontend-spec.md` 已说明无需前端代码。

---

## 输入来源

生成时必须读取：

- `requirements.md`
- `contracts.md`
- `frontend-spec.md`（如果已生成）
- `codebook/baseline-spec/views.json`
- `codebook/baseline-spec/frontend-extends.json`
- `codebook/baseline-spec/services.json`
- `codebook/baseline-spec/unresolved.json`

可选读取：

- UI 原型、截图、产品说明。
- `codebook/modules/*/interaction-spec.md` 或 `create-project-contract.md`。
- IIDP 前端开发文档和标准节点 ID 规则。

无法确认的信息写“待确认”，并说明需要从哪里确认，例如浏览器控制台、运行时节点、后端服务或产品原型。

---

## 输出结构

`interaction-spec.md` 必须按以下结构输出。

```markdown
# 交互规格：[功能名]

## 1. 页面与入口
## 2. 用户流程
## 3. 页面状态
## 4. 操作与事件矩阵
## 5. 数据源与服务触发
## 6. Hook 与默认行为
## 7. 弹窗/抽屉/openView/子表
## 8. 权限、错误与失败路径
## 9. 响应式与可用性
## 10. 验收标准
## 11. 待确认项
```

---

## 1. 页面与入口

说明页面在哪、属于哪个 IIDP app、绑定哪个菜单和模型。

模板：

```markdown
## 1. 页面与入口

| 项 | 值 |
|---|---|
| frontendApp |  |
| backendApp |  |
| menuId |  |
| menuName |  |
| model |  |
| viewId |  |
| 页面类型 | grid / form / tree / tab / custom |
| 实现分支 | 标准模板 / 在线视图 / hook / 扩展视图 / 自定义 Vue2 组件 |
| baseline 证据 | baseline specId 或 sourceFile |
```

规则：

- 已有页面必须引用 baseline 中的 `menuId/viewId/selector`。
- 新增页面必须说明命名依据和目标注册位置。
- 前后端 app 不一致时，必须解释依赖关系。

---

## 2. 用户流程

用流程描述主要路径和失败路径。

模板：

````markdown
## 2. 用户流程

```text
用户进入页面
  -> 系统加载菜单、权限、视图和默认查询
  -> 展示初始状态
  -> 用户执行操作 A
     -> 成功：刷新/关闭/提示/跳转
     -> 失败：字段错误/权限错误/业务错误/网络错误
  -> 用户执行操作 B
```

### 2.1 关键路径

| 路径 | 触发 | 前置条件 | 成功结果 | 失败处理 | 备注 |
|---|---|---|---|---|---|
| 新增 | 点击新增 | 有 create 权限 | 打开表单 | 权限不足则隐藏或置灰 |  |
| 保存 | 点击保存 | 必填通过 | 关闭表单并刷新 | 字段错误停留表单 |  |
````

规则：

- 必须覆盖成功和失败路径。
- 批量操作、状态流转、删除必须写确认和后端兜底。
- 不能只写“点击按钮调用接口”，必须写用户看到什么、数据怎么变。

---

## 3. 页面状态

模板：

```markdown
## 3. 页面状态

| 状态 | 触发条件 | 页面表现 | 可操作项 | 数据变化 | IIDP 承载 |
|---|---|---|---|---|---|
| 初始态 | 首次进入 | 默认查询条件和列表 | 查询、新增 | 加载第一页 | 标准模板 |
| 加载态 | 查询中 | 表格 loading | 禁止重复查询 | 请求中 | 标准模板/hook |
| 空态 | 查询无结果 | 空数据提示 | 新增、重置 | tableData=[] | 标准模板 |
| 编辑态 | 打开表单 | 表单可编辑 | 保存、取消 | formData 加载 | 标准模板 |
| 保存中 | 点击保存 | 保存按钮 loading | 禁止重复提交 | 请求 save | hook |
| 错误态 | 服务失败 | 错误提示 | 重试/关闭 | 不更新数据 | 平台提示 |
```

规则：

- 至少包含初始、加载、空、错误、保存中。
- 有状态流转时，额外输出“状态 x 按钮显示规则”。

状态按钮表：

```markdown
| 业务状态 | 显示按钮 | 隐藏按钮 | 禁用按钮 | 后端校验 |
|---|---|---|---|---|
| DRAFT | 编辑、删除、提交 | 撤回 |  | 状态必须为 DRAFT |
| SUBMITTED | 审核、撤回 | 删除 | 编辑 | 状态必须为 SUBMITTED |
```

---

## 4. 操作与事件矩阵

列出用户操作、前端事件、数据源和后端契约的关系。

模板：

```markdown
## 4. 操作与事件矩阵

| 操作 | selector/节点 | 事件 | 前端行为 | 数据源/服务 | 成功后 | 失败后 |
|---|---|---|---|---|---|---|
| 查询 | search_btn | bind_on_click | 收集搜索条件 | search | 刷新表格 | 显示错误 |
| 保存 | form_save | bind_on_click | 校验表单 | save/create/update | 关闭并刷新 | 停留表单 |
| 删除 | row_delete | bind_on_click | 二次确认 | delete | 刷新表格 | 提示失败 |
```

规则：

- selector 可以写 `待确认`，但不能编造。
- 事件名必须保留源码拼写，例如历史代码中的 `bind_on_opreates`。
- 若通过 hook 触发，事件列写 hook 路径。

---

## 5. 数据源与服务触发

模板：

```markdown
## 5. 数据源与服务触发

| 名称 | 触发点 | 调用方式 | app | model | service | args | 返回影响 |
|---|---|---|---|---|---|---|---|
| tableData | 页面查询 | ds_config |  |  | search | filter/limit/offset | 更新表格 |
| formSave | 保存 | vm.request |  |  | save | valuesList | 刷新详情 |
| import | 导入确认 | httpMeta |  |  | import | fileId | 刷新列表 |
```

规则：

- `app/model/service/args` 必须来自 `contracts.md` 或 baseline。
- `vm.request` 未显式声明 app/model/service 时，写“继承当前页面上下文”。
- 示例占位如 `model: xxx` 只能写 placeholder，不作为真实契约。

---

## 6. Hook 与默认行为

模板：

```markdown
## 6. Hook 与默认行为

| hook 路径 | 触发时机 | 业务处理 | 是否调用 vm.super | 返回值 | 风险 |
|---|---|---|---|---|---|
| grid.beforeQuery | 查询前 | 补充过滤条件 | 是 | params | 参数错误会影响查询 |
| form.beforeSave | 保存前 | 校验状态 | 是/否 | params/false | false 会中断保存 |
```

规则：

- 所有 hook 必须说明是否调用 `vm.super`。
- before 类 hook 必须说明返回什么。
- 返回 `false` 必须说明是拦截默认逻辑。
- 不能编造 `vm.biz` 路径；不确定写待确认。

---

## 7. 弹窗/抽屉/openView/子表

模板：

```markdown
## 7. 弹窗、抽屉、openView 与子表

| 交互对象 | 打开方式 | 数据来源 | 关闭方式 | 保存后行为 | 待确认 |
|---|---|---|---|---|---|
| 新增弹窗 | 点击新增 | 空表单 | 取消/保存/ESC | 刷新列表 |  |
| 明细子表 | 主表选中 | 主表 id 过滤 | 切换主表 | 重新查询 | 子表 field |
| openView | 点击选择 | 目标模型 search | 选择确认 | 回填字段 | 目标 viewId |
```

规则：

- 主子表必须写清主表 id 如何传给子表。
- openView 必须写目标模型、回填字段和关闭行为。
- 弹窗必须写尺寸、滚动、保存后刷新策略。

---

## 8. 权限、错误与失败路径

模板：

```markdown
## 8. 权限、错误与失败路径

| 场景 | 前端表现 | 后端兜底 | 用户可操作 | 验收 |
|---|---|---|---|---|
| 无菜单权限 | 菜单不可见 | 路由/服务拒绝 | 无 | 无权限用户不可进入 |
| 无按钮权限 | 按钮隐藏/置灰 | service auth 拒绝 | 无 | 直接请求失败 |
| 字段错误 | 字段下方提示 | @Validate 拒绝 | 修改重试 | 错误文案可见 |
| 业务错误 | toast/弹窗 | MethodService 抛异常 | 确认关闭 | 数据不变 |
| 网络错误 | 平台错误提示 |  | 重试 | 可恢复 |
```

规则：

- 前端隐藏不足以作为安全控制，必须写后端兜底。
- 删除、批量删除、状态变更必须有确认或明确说明无需确认。

---

## 9. 响应式与可用性

模板：

```markdown
## 9. 响应式与可用性

| 区域 | 布局 | 滚动归属 | 小屏策略 | 键盘/可访问性 |
|---|---|---|---|---|
| 搜索区 | 自适应 | 无 | 可折叠 | Tab 可聚焦 |
| 表格区 | 剩余高度 | 表格内滚动 | 关键列优先 | 行操作可聚焦 |
| 表单弹窗 | 固定/自适应 | 弹窗内滚动 | 最大化或全屏 | ESC 关闭 |
```

可用性检查：

- 表单 label 清楚。
- 必填字段有标识。
- 错误提示不只依赖颜色。
- loading、empty、error 可触发。
- 危险操作有确认。
- 键盘可完成主要操作。

---

## 10. 验收标准

模板：

```markdown
## 10. 验收标准

- [ ] 页面入口、菜单、模型和视图与 contracts.md 一致。
- [ ] 主要用户流程成功路径可执行。
- [ ] 关键失败路径有明确提示。
- [ ] 所有新增/修改操作有权限控制和后端兜底。
- [ ] 数据源调用的 app/model/service/args 与 contracts.md 一致。
- [ ] hook 调用 `vm.super` 或拦截默认行为的策略明确。
- [ ] loading、empty、error、saving 状态可验证。
- [ ] 弹窗/抽屉/openView/子表交互可验证。
- [ ] 响应式和可用性检查通过。
```

---

## 11. 待确认项

模板：

```markdown
## 11. 待确认项

| 项 | 原因 | 确认方式 | 阻塞阶段 |
|---|---|---|---|
| targetNodeId | 静态源码无法确认运行时节点 | 浏览器控制台 `tech_app.getNode` | 实现前 |
| service args | 需求未说明参数结构 | 后端契约确认 | /sdd-contracts |
```

规则：

- 待确认项必须说明确认方式。
- 阻塞实现的待确认项不能留到 `/sdd-implement` 后才处理。

---

## 审核清单

- [ ] 是否引用了 `requirements.md`、`contracts.md` 和 baseline 事实。
- [ ] 是否覆盖成功路径和失败路径。
- [ ] 是否包含页面状态、操作事件、数据源和服务触发。
- [ ] 是否说明 hook 是否调用 `vm.super`。
- [ ] 是否区分真实契约、继承上下文和示例占位。
- [ ] 是否包含权限和后端兜底。
- [ ] 是否包含响应式、可用性和验收标准。
- [ ] 待确认项是否有确认方式和阻塞阶段。
