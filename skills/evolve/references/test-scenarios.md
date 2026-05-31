# Skill 进化标准测试场景

> 用于 D10 实测维度评分和每轮修复后的效果验证。
> 场景设计原则：覆盖已知的高频错误（ManyToOne/ID类型/审计字段/驼峰），包含典型业务关系。

---

## 场景 1：含 ManyToOne 关系的课程管理（核心测试场景）

**实体描述：**
- `Course`（课程）：有状态字段 `status`（DRAFT/PUBLISHED/ARCHIVED），有多个 `Lesson`
- `Lesson`（课时）：多个课时属于一个课程（ManyToOne → Course），有序号 `sortOrder`
- `Category`（分类）：课程属于某个分类（ManyToOne → Category）

**关键字段（用于验证 D2/D6）：**

```
Course:
  - id: String（雪花）
  - title: String，@Property(displayName="课程名称")
  - status: String，@Property(displayName="状态") 枚举：DRAFT/PUBLISHED/ARCHIVED
  - categoryId: String，@Selection(model="category") → FK String
  - category: Category，@ManyToOne → ORM 对象（与 categoryId 成对）
  - [审计字段由 isAutoLog=Bool.True 自动维护，不手动声明]

Lesson:
  - id: String
  - title: String
  - sortOrder: Integer
  - courseId: String，@Selection(model="course") → FK String
  - course: Course，@ManyToOne → ORM 对象（与 courseId 成对）
```

**验证检查点（运行 `/sdd-spec` 后检查 backend-spec.md）：**

| 检查项 | 期望结果 | 对应维度 |
|---|---|---|
| Course.id 类型 | `String`（不是 `Long`） | D2 |
| categoryId 字段 | 存在，类型 `String`，有 `@Selection` | D6 |
| category 字段 | 存在，类型 `Category`，有 `@ManyToOne` + `@JoinColumn` | D6 |
| 审计字段 | create_user/create_date 等字段**不出现**在模型字段列表中 | D2 |
| sortOrder 引用 | `set("sortOrder", value)` / `getStr("sortOrder")`（不是 `setSort_order` 等变体） | D2 |
| @Model 注解 | `isAutoLog = Bool.True`（不是 Spring 原生 JPA 注解） | D3 |

---

## 场景 2：含状态流转的请假申请（工作流验证）

**实体描述：**
- `LeaveRequest`（请假申请）：有状态机 PENDING/APPROVED/REJECTED/CANCELLED
- `Employee`（员工）：请假申请属于某员工（ManyToOne → Employee）

**关键字段：**

```
LeaveRequest:
  - id: String
  - reason: String
  - startDate: Date
  - endDate: Date
  - auditStatus: String，枚举：PENDING/APPROVED/REJECTED/CANCELLED
  - employeeId: String，@Selection → FK String
  - employee: Employee，@ManyToOne → ORM 对象
```

**验证检查点：**

| 检查项 | 期望结果 | 对应维度 |
|---|---|---|
| auditStatus 枚举 | 在 backend-spec 字段表中有完整枚举值列表 | D9 |
| 状态变更接口 | contracts.md 中有 approve/reject 方法签名 | D9 |
| 前端 workflow 页 | frontend-spec 中有状态流转页面的说明 | D1 |

---

## 场景 3：简单单表（基线测试，无关系）

**实体描述：**
- `Department`（部门）：名称、描述、是否启用

**用途**：验证最基础的 CRUD 生成，排除关系复杂度的干扰。如果连此场景都出错（ID 用 Long），说明问题是根本性的。

**验证检查点：**

| 检查项 | 期望结果 |
|---|---|
| id 类型 | `String` |
| @Model 注解 | 有 `isAutoLog = Bool.True` |
| 无关联字段 | 字段列表中不出现多余的外键 |
| tasks.md 任务数 | 包含 backend-model / backend-service / backend-view 三个 task block |

---

## 使用说明

- D10 实测维度评分：必须实际运行**场景 1** 的 `/sdd-spec`，观察 backend-spec.md 输出
- 修复验证：每轮 hill-climbing 修改后，用**场景 1 + 场景 2** 重新运行，对比错误数量
- 基线确认：首次运行前用**场景 3** 快速检查基础 CRUD 是否正常，确认环境可用
