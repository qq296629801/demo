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

## 使用说明

- **D10 后端实测**：必须实际运行**场景 1 或场景 2** 的 `/sdd-spec`，观察 backend-spec.md 输出
- **F1-F4 前端实测**：必须实际运行**场景 5** 的 `/sdd-spec`，观察 frontend-spec.md 输出
- **修复验证**：每轮 hill-climbing 修改后，用**场景 1 + 场景 3**（后端）及**场景 5**（前端）重新运行，对比错误数量
- **基线确认**：首次运行前用**场景 4** 快速检查基础 CRUD，确认环境可用
- **工作流验证**：用**场景 3**（JeecgBoot 请假）验证状态机生成

## 真实项目对比说明

| 项目 | ID 类型 | 审计字段 | 外键风格 |
|---|---|---|---|
| RuoYi-Vue-Pro | `Long` | 手动声明 `createTime` | `Long deptId` |
| macrozheng/mall | `Long` | 手动声明 `createTime/modifyTime` | `Long memberId` |
| JeecgBoot | `String`（已接近 IIDP）| 手动声明 `createTime/createBy` | `String applyUserId` |
| Guns | `Long` | 手动声明 `createTime/updateTime` | `Long orgParentId` |
| **IIDP 要求** | **`String`（雪花）** | **`isAutoLog=Bool.True` 自动维护** | **`String xxxId` + `@Selection` + ManyToOne 成对** |
