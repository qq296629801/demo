# Skill 进化标准测试场景

> 基于 Gitee 后端热门开源项目（芋道源码/RuoYi-Vue-Pro、macrozheng/mall、jeecgboot/JeecgBoot、stylefeng/Guns）的真实实体设计。
> 这些项目均使用 Long 类型 ID，与 IIDP 平台要求的 String（雪花）形成对比，是检验 skill 能否正确指导类型转换的核心依据。

---

## 场景 1：RuoYi-Vue-Pro 用户管理（核心测试场景）

**来源**：`yudaocode/ruoyi-vue-pro` - `AdminUserDO.java` / `DeptDO.java`

**原始项目字段（Java Long ID 风格）：**
```java
// AdminUserDO（原始）
private Long id;
private Long deptId;        // 部门外键
private String username;
private String nickname;
private Integer status;     // 0正常 1停用
private Date createTime;
private Date updateTime;
// DeptDO（原始）
private Long id;
private Long parentId;      // 父部门外键
private String name;
```

**IIDP 适配后实体描述：**
- `SysUser`（系统用户）：有状态字段 `status`（ENABLE/DISABLE），属于某个部门（ManyToOne → SysDept）
- `SysDept`（部门）：有父部门自引用（ManyToOne → SysDept），有多个用户

**关键字段（IIDP 规范，用于验证 D2/D6）：**

```
SysUser:
  - id: String（雪花，原 Long → IIDP String）
  - username: String，@Property(displayName="用户名")
  - nickname: String，@Property(displayName="昵称")
  - status: String，@Property(displayName="状态") 枚举：ENABLE/DISABLE
  - deptId: String，@Selection(model="sysDept") → FK String（原 Long deptId）
  - dept: SysDept，@ManyToOne → ORM 对象（与 deptId 成对）
  - [审计字段由 isAutoLog=Bool.True 自动维护，createTime/updateTime 不手动声明]

SysDept:
  - id: String
  - name: String，@Property(displayName="部门名称")
  - parentId: String，@Selection(model="sysDept") → 自引用 FK
  - parent: SysDept，@ManyToOne → 自引用 ORM 对象
```

**验证检查点（运行 `/sdd-spec` 后检查 backend-spec.md）：**

| 检查项 | 期望结果 | 对应维度 |
|---|---|---|
| SysUser.id 类型 | `String`（原项目为 `Long`，IIDP 必须转换） | D2 |
| deptId 字段 | 存在，类型 `String`，有 `@Selection` | D6 |
| dept 字段 | 存在，类型 `SysDept`，有 `@ManyToOne` + `@JoinColumn` | D6 |
| 审计字段 | createTime/updateTime **不出现**在模型字段列表（isAutoLog 自动维护） | D2 |
| status 枚举 | ENABLE/DISABLE（不是 Integer 0/1） | D2 |
| @Model 注解 | `isAutoLog = Bool.True`（不是 Spring 原生 JPA 注解） | D3 |

---

## 场景 2：macrozheng/mall 订单主从表（ER 关系验证）

**来源**：`macrozheng/mall` - `OmsOrder.java` / `OmsOrderItem.java`

**原始项目字段（Java Long ID 风格）：**
```java
// OmsOrder（原始，44个字段）
private Long id;
private Long memberId;      // 会员外键
private Long couponId;      // 优惠券外键
private Integer status;     // 0待付款 1待发货 2已发货 3已完成 4已关闭
private Date createTime;
private Date modifyTime;
// OmsOrderItem（原始）
private Long id;
private Long orderId;       // 订单外键
private Long productId;     // 商品外键
private Integer productQuantity;
```

**IIDP 适配后实体描述：**
- `OmsOrder`（订单）：有状态机 PENDING/PAID/SHIPPED/COMPLETED/CLOSED，关联会员（ManyToOne → UmsMember）
- `OmsOrderItem`（订单明细）：多个明细属于一个订单（ManyToOne → OmsOrder）

**关键字段：**

```
OmsOrder:
  - id: String（雪花）
  - totalAmount: BigDecimal，@Property(displayName="订单总金额")
  - status: String，枚举：PENDING/PAID/SHIPPED/COMPLETED/CLOSED
  - memberId: String，@Selection(model="umsMember") → FK String
  - member: UmsMember，@ManyToOne → ORM 对象
  - [modifyTime 等审计字段由 isAutoLog=Bool.True 维护，不手动声明]

OmsOrderItem:
  - id: String
  - productName: String，@Property(displayName="商品名称")
  - productQuantity: Integer，@Property(displayName="购买数量")
  - orderId: String，@Selection(model="omsOrder") → FK String（原 Long orderId）
  - order: OmsOrder，@ManyToOne → ORM 对象
```

**验证检查点：**

| 检查项 | 期望结果 | 对应维度 |
|---|---|---|
| OmsOrder.id 类型 | `String`（原项目 `Long`） | D2 |
| orderId 字段 | 存在，类型 `String`，有 `@Selection`（原 `Long orderId`） | D6 |
| status 枚举 | 在 backend-spec 字段表中有完整枚举值列表 | D9 |
| 状态变更接口 | contracts.md 中有 pay/ship/complete 方法签名 | D9 |
| modifyTime | **不出现**在模型字段（isAutoLog 维护） | D2 |

---

## 场景 3：JeecgBoot 请假申请（工作流验证）

**来源**：`jeecgboot/JeecgBoot` - `JeecgOrderMain.java` / 请假流程 Demo

**原始项目字段（JeecgBoot 风格）：**
```java
// 请假申请（JeecgBoot OA模块）
@TableId(type = IdType.ASSIGN_ID)
private String id;          // JeecgBoot 已用 String，但手动声明了 createTime
private String applyUserId;
private String reason;
private Date startDate;
private Date endDate;
private String status;      // 0草稿 1提交 2审批中 3通过 4拒绝
@JsonFormat(timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
private Date createTime;    // 手动声明（IIDP 禁止此方式）
private String createBy;    // 手动声明（IIDP 禁止此方式）
```

**IIDP 适配后实体描述：**
- `LeaveRequest`（请假申请）：有状态机 DRAFT/SUBMITTED/APPROVING/APPROVED/REJECTED
- `Employee`（员工）：申请属于某员工（ManyToOne → Employee）

**关键字段：**

```
LeaveRequest:
  - id: String
  - reason: String，@Property(displayName="请假原因")
  - startDate: Date，@Property(displayName="开始日期")
  - endDate: Date，@Property(displayName="结束日期")
  - auditStatus: String，枚举：DRAFT/SUBMITTED/APPROVING/APPROVED/REJECTED
  - employeeId: String，@Selection(model="employee") → FK String
  - employee: Employee，@ManyToOne → ORM 对象
  - [createTime/createBy 等审计字段由 isAutoLog=Bool.True 维护，绝对禁止手动声明]
```

**验证检查点：**

| 检查项 | 期望结果 | 对应维度 |
|---|---|---|
| auditStatus 枚举 | 在 backend-spec 字段表中有完整枚举值列表（5个状态） | D9 |
| 状态变更接口 | contracts.md 中有 submit/approve/reject 方法签名 | D9 |
| createTime/createBy | **不出现**在模型字段（JeecgBoot 手动声明 → IIDP isAutoLog） | D2 |
| 前端 workflow 页 | frontend-spec 中有状态流转页面的说明 | D1 |
| employeeId 字段 | 存在，类型 `String`（而非 JeecgBoot 风格的 applyUserId String） | D6 |

---

## 场景 4：Guns/SmartAdmin 部门管理（基线测试，简单 CRUD）

**来源**：`stylefeng/Guns` - `HrOrganization.java` / `SmartAdmin` 部门模块

**原始项目字段：**
```java
// Guns HrOrganization（原始）
private Long orgId;         // Long 主键
private String orgName;
private String orgCode;
private Boolean statusFlag; // true启用 false停用
private Long orgParentId;   // 父部门 Long 外键
private Date createTime;
private Date updateTime;
```

**IIDP 适配后实体描述：**
- `HrDepartment`（部门）：简单单表，名称、编码、是否启用

**用途**：验证最基础的 CRUD 生成，无复杂关系。若连此场景都出错（ID 用 Long，或出现 createTime 字段），说明 D2 问题是根本性的。

**验证检查点：**

| 检查项 | 期望结果 |
|---|---|
| id 类型 | `String`（原 `Long orgId`） |
| @Model 注解 | 有 `isAutoLog = Bool.True` |
| createTime/updateTime | **不出现**（原 Guns 手动声明 → IIDP isAutoLog） |
| statusFlag | 转换为 `status: String`（枚举 ENABLE/DISABLE，不是 Boolean） |
| tasks.md 任务数 | 包含 backend-model / backend-service / backend-view 三个 task block |

---

## 场景 5：RuoYi-Vue-Pro 用户管理——前端规格（F1-F4 实测场景）

**来源**：与场景 1 相同实体（SysUser / SysDept），聚焦前端规格输出的正确性。

**业务描述**：
- 用户列表页（标准 CRUD 管理后台风格）：搜索 + 表格 + 新增/编辑弹窗
- SysUser 属于 SysDept（ManyToOne），需要部门选择器（Lookup）
- 状态字段 `status` 有 ENABLE/DISABLE 两个枚举值，表格列需要展示中文
- 无复杂交互逻辑，**期望走标准模板/在线视图路径，前端无需新增代码**

**模拟运行 `/sdd-spec` 后，检查生成的 frontend-spec.md：**

**验证检查点（F1-F4）：**

| 检查项 | 期望结果 | 对应维度 |
|---|---|---|
| §9 决策路径 | 给出"标准模板/在线视图"结论，说明无需前端代码的理由（不直接跳到扩展视图） | F1 |
| 节点 id 来源 | 有明确来源标注（菜单 key 推导 / 标准模板规则库 / 待确认），**不出现**按文案自拼的 id | F2 |
| 不确定节点 id | 标记为"待确认"，不伪造具体值 | F2 |
| 数据源类型 | `type: "meta"`，使用 IIDP 平台数据源；**不出现** axios/fetch 引入 | F3 |
| 部门选择器绑定 | `deptId` 字段使用 `@Selection(model="sysDept")`；对应 `bind_` 来自后端契约，不凭空写 service 名 | F3 |
| 操作按钮 auth | 新增按钮 auth = `sys_user:create`，编辑 = `sys_user:update`，删除 = `sys_user:delete`；格式为 `{model_name}:{action}` | F4 |
| 无前端权限硬编码 | **不出现** `v-if="hasPermission()"` 或 JS 逻辑判断权限码 | F4 |

**反模式（如出现以下内容则扣分）：**

```
❌ F1：直接写扩展视图或 Vue2 组件，跳过"是否可用标准模板"判断
❌ F2：节点 id 写成 "sys_user_list_table"（按文案拼接，无来源依据）
❌ F3：数据源配置里出现 import axios from 'axios'
❌ F3：bind_on_ 使用了 "on_row_select"（非平台真实事件名）
❌ F4：按钮 auth 写成 "sysUser:create"（camelCase model_name，应为下划线 sys_user）
❌ F4：JS 中出现 if (this.userInfo.roles.includes('admin')) 类前端权限判断
```

---

## 场景 6：contracts.md 缺失——失败分支验证（D4/D7）

**用途**：专门触发失败分支，验证 skill 是否有**显式 if-then 停止指令**，而非模糊提示。

**构造方式**：向 skill 发起以下请求，工作区**故意不提供** contracts.md：

```
请为 SysUser（用户管理）生成 backend-spec.md。
当前工作区：specs/features/user-mgmt/requirements.md 已存在，但 contracts.md 尚未创建。
```

**期望行为（满分）：**

skill 必须在检测到 contracts.md 缺失时**立即停止**，输出类似：

```
若 specs/features/user-mgmt/contracts.md 不存在 → 停止，提示：
  "请先运行 /sdd-contracts 生成契约文件，再继续生成 backend-spec。"
```

**验证检查点：**

| 检查项 | 期望结果 | 对应维度 |
|---|---|---|
| 失败分支是否存在 | `sdd-spec.md` 中有明确的"若 contracts.md 不存在 → 停止"分支，而非"注意/建议先准备" | D4 |
| 停止措辞强度 | 用"停止"/"禁止继续"，不用"建议先"/"注意要" | D7 |
| 违反后果说明 | 说明跳过契约直接生成 backend-spec 的后果（字段类型可能不一致、前后端脱轨） | D7 |
| 正向流程是否区分 | 有 contracts.md 存在时走正常生成路径，两条分支都有明确说明 | D4 |

**反模式（如出现以下内容则扣分）：**

```
❌ D4："请注意，生成 backend-spec 前建议先准备好 contracts.md"（软化措辞，非停止指令）
❌ D4：直接生成 backend-spec 而不检查 contracts.md 是否存在
❌ D7："尽量避免跳过契约步骤"（无后果说明）
❌ D7："可以考虑先完成契约定义"（建议语气，非禁止）
```

---

## 场景 7：模板完整性与可操作性——Step 输出结构验证（D1/D5）

**用途**：静态检查 `sdd-workflow.md` 和各 `commands/*.md` 的结构完整性，验证 D1（无 TODO 占位符）和 D5（无软化措辞）。

**检查方式**：法官直接阅读以下文件，按检查项逐一验证（不需要运行命令）：

```
skills/create-project/references/sdd-workflow.md
skills/create-project/commands/sdd-spec.md
skills/create-project/commands/sdd-contracts.md
skills/create-project/commands/sdd-tasks.md
skills/create-project/commands/sdd-validate.md
```

**验证检查点（D1）：**

| 检查项 | 期望结果 |
|---|---|
| 每个 Step 有输入声明 | Step 0-5 各自说明"读取哪些文件 / 依赖哪些前置产物" |
| 每个 Step 有输出声明 | Step 0-5 各自说明"生成哪个文件 / 输出什么格式" |
| 无 TODO/待补充占位符 | 全文不出现 `[TODO]`、`待补充`、`详见后续`、`参考 xxx`（无实际内容兜底） |
| 输出模板可直接使用 | 模板中的占位符仅为 `[实际值]` 风格，不是说明性文字替代实际格式 |

**验证检查点（D5）：**

扫描上述 5 个文件，统计出现以下软化措辞的次数（每处扣 1pt，上限扣满 10pt）：

| 软化措辞模式 | 示例 |
|---|---|
| 建议/可以考虑 | "建议先检查..."、"可以考虑增加..." |
| 根据情况/视具体 | "根据情况决定..."、"视具体需求选择..." |
| 灵活把握/酌情 | "灵活把握粒度"、"酌情添加字段" |
| 尽量/尽可能 | "尽量保持一致"、"尽可能复用" |
| 一般来说/通常 | "一般来说需要..."（无明确条件） |

**反模式（如出现以下内容则扣分）：**

```
❌ D1：Step 2（Backend Spec）缺少"输入：requirements.md + contracts.md"的声明
❌ D1：sdd-tasks.md 中有"任务格式见后续章节"但后续章节为空
❌ D5："根据业务复杂度灵活决定是否需要 contracts.md"
❌ D5："建议在生成任务前先与用户确认计划"（无具体触发条件）
```

---

## 场景 8：跨步骤一致性——同一实体在三份产物中的字段对比（D8）

**用途**：验证同一字段在 backend-spec / contracts / frontend-spec 三份产物模板中类型和注解是否一致，检测跨文档漂移。

**构造方式**：用场景 1（SysUser）同时运行以下两个命令，对比产物：

```
/sdd-spec    → 生成 backend-spec.md
/sdd-contracts → 生成 contracts.md
```

然后检查 `sdd-backend.md` 模板和 `references/sdd-contracts.md` 模板中，**同一字段的类型声明是否一致**。

**关键对比字段（SysUser.deptId）：**

| 产物 | 字段位置 | 期望类型 | 期望注解 |
|---|---|---|---|
| backend-spec.md §3 模型表 | `deptId` 行 | `String` | `@Selection(model="sysDept")` |
| contracts.md 服务入参表 | `create/update` 的 `valuesList` 中 `deptId` | `String` | — |
| frontend-spec.md §7 字段规格 | `deptId` 行 | `String` | IIDP 控件：Lookup |

**验证检查点：**

| 检查项 | 期望结果 | 对应维度 |
|---|---|---|
| deptId 类型一致 | 三份产物均为 `String`，不出现一处 `Long` | D8 |
| status 枚举值一致 | backend-spec 和 contracts 的枚举值列表相同（ENABLE/DISABLE） | D8 |
| @MethodService 方法名一致 | contracts.md 的方法名与 backend-spec 服务表中的方法名完全匹配 | D8 |
| sdd-backend.md ↔ sdd-contracts.md 模板对齐 | 两份 skill 参考文档中的字段类型示例格式一致（不出现一处用 String 一处用 Long 的示例） | D8 |

**反模式（如出现以下内容则扣分）：**

```
❌ D8：backend-spec 模板示例写 deptId: Long，contracts 模板写 deptId: String
❌ D8：backend-spec 方法名 "getUserList"，contracts 模板写 "queryUserList"（命名漂移）
❌ D8：sdd-frontend.md 字段规格表中 status 枚举值为 0/1（未与 backend-spec 的 ENABLE/DISABLE 对齐）
```

---

## 场景 9：前端 hook/扩展视图路径——复杂交互场景（F1）

**用途**：触发 F1 决策链的非标准路径，验证 skill 能否正确引导走 hook 或扩展视图，而不是直接写 Vue2 组件。

**业务描述**：
- 场景 A（hook 路径）：订单列表页（场景 2 的 OmsOrder），需要在**查询前**自动注入当前登录用户的 memberId 作为过滤条件，其余 CRUD 用标准模板
- 场景 B（扩展视图路径）：请假申请列表（场景 3 的 LeaveRequest），需要在表格工具栏**新增一个"批量提交"按钮**，触发自定义服务

**期望行为：**

| 场景 | 期望决策 | 期望产物 |
|---|---|---|
| 场景 A（查询前注入参数） | 走 **hook 路径**（`grid.beforeQuery`），不新增 Vue2 组件 | frontend-spec §9.2，hook 路径，写明 `beforeQuery` 钩子和 `return params` |
| 场景 B（新增工具栏按钮） | 走**扩展视图路径**（`after`/`append` 扩展），不替换整个标准模板页 | frontend-spec §9.3，扩展类型 `after`，目标节点 id 标注来源 |

**验证检查点（F1）：**

| 检查项 | 期望结果 |
|---|---|
| 场景 A：不出现 Vue2 组件 | `sdd-frontend.md` 的 §9 有明确说明：参数注入优先用 `grid.beforeQuery` hook |
| 场景 A：hook 返回值规范 | `beforeQuery` 说明必须 `return params`（不 return 则查询参数丢失） |
| 场景 B：不出现 `type: 'page'` replace | 扩展工具栏按钮用 `after`/`append`，不用 `replace` 替换整个页面节点 |
| 场景 B：目标节点 id 有来源 | 目标节点 id 有明确来源（标准模板规则库 / 后端视图定义 / 待确认），不自拼 |

**反模式（如出现以下内容则扣分）：**

```
❌ F1：场景 A 直接写一个 Vue2 组件封装查询表单（跳过 hook 优先级）
❌ F1：场景 B 用 replace 替换整个标准模板页来追加按钮
❌ F1：§9 缺少 hook 路径的进入条件（"不改节点结构时用 hook"）
❌ F1：beforeQuery 示例中没有 return params（导致参数丢失的隐性 bug）
```

---

## 场景 10：前端 api 数据源与 reqPrep/reqAfter——复杂绑定验证（F3）

**用途**：触发 F3 中 `api` 类型数据源和 `reqPrep`/`reqAfter` 的正确性验证。

**业务描述**：
- 请假申请详情页（场景 3 的 LeaveRequest），点击某条记录后加载**关联员工信息**
- 员工信息需要通过独立 API 查询（非自动加载），需要 `reqPrep` 动态注入选中行的 `employeeId`

**期望产物（frontend-spec.md §10 数据源配置）：**

```json
{
  "type": "api",
  "name": "employeeDetail",
  "autoRequest": false,
  "options": {
    "model": "employee",
    "service": "get",
    "args": { "id": "" }
  },
  "reqPrep": "(vm, params) => { params.args.id = vm.biz.grid.checkedData.employeeId; return params }",
  "reqAfter": "(vm, res) => { return res.data }"
}
```

**验证检查点（F3）：**

| 检查项 | 期望结果 |
|---|---|
| 数据源类型 | `type: "api"`（手动触发），不用 `autoRequest: true` |
| reqPrep 签名 | `(vm, params) => { ...; return params }`，必须 return params |
| reqAfter 签名 | `(vm, res) => { return res.data }`，必须 return 处理后数据 |
| service 名来源 | `get` 来自 contracts.md 服务契约表，不凭空推断 |
| 无 axios/fetch | 整个 frontend-spec 中不出现 axios / fetch / XMLHttpRequest |

**反模式（如出现以下内容则扣分）：**

```
❌ F3：reqPrep 中没有 return params（数据源调用会静默失败）
❌ F3：reqAfter 中没有 return（组件拿不到数据）
❌ F3：service 名写成 "getEmployeeById"（凭命名猜测，未对齐 contracts.md）
❌ F3：直接在 Vue2 组件的 mounted() 里 axios.get('/api/employee/' + id)
```

---

## 使用说明

| 场景 | 主要覆盖维度 | 运行方式 |
|---|---|---|
| 场景 1 | D2/D3/D6（核心后端） | 运行 `/sdd-spec`，检查 backend-spec.md |
| 场景 2 | D2/D6/D9（ER + 状态机） | 运行 `/sdd-spec`，检查 backend-spec.md |
| 场景 3 | D2/D9（工作流 + 审计字段） | 运行 `/sdd-spec`，检查 backend-spec.md |
| 场景 4 | D2/D3（基础 CRUD 基线） | 运行 `/sdd-spec`，快速确认环境 |
| 场景 5 | F1/F2/F3/F4（前端标准路径） | 运行 `/sdd-spec`，检查 frontend-spec.md |
| 场景 6 | D4/D7（失败分支 + 约束执行力） | 静态阅读 `commands/*.md`，检查分支写法 |
| 场景 7 | D1/D5（模板完整性 + 可操作性） | 静态阅读 `sdd-workflow.md` + `commands/*.md` |
| 场景 8 | D8（跨步骤一致性） | 同时运行 `/sdd-spec` + `/sdd-contracts`，对比产物 |
| 场景 9 | F1（hook/扩展视图路径） | 静态阅读 `sdd-frontend.md` §9，检查决策链 |
| 场景 10 | F3（api 数据源 + reqPrep/reqAfter） | 静态阅读 `sdd-frontend.md` §10，检查数据源配置 |

## 真实项目对比说明

| 项目 | ID 类型 | 审计字段 | 外键风格 |
|---|---|---|---|
| RuoYi-Vue-Pro | `Long` | 手动声明 `createTime` | `Long deptId` |
| macrozheng/mall | `Long` | 手动声明 `createTime/modifyTime` | `Long memberId` |
| JeecgBoot | `String`（已接近 IIDP）| 手动声明 `createTime/createBy` | `String applyUserId` |
| Guns | `Long` | 手动声明 `createTime/updateTime` | `Long orgParentId` |
| **IIDP 要求** | **`String`（雪花）** | **`isAutoLog=Bool.True` 自动维护** | **`String xxxId` + `@Selection` + ManyToOne 成对** |
