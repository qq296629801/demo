# backend / frontend 技能参考指南

`backend` 和 `frontend` skill 是 `create-project` 的底层支撑，提供 IIDP 平台的完整编码规范和模板。一般不直接调用，而是在 `/sdd-spec` 和 `/sdd-implement` 执行时被自动引用。

---

## 目录

1. [Backend Skill 参考](#backend-skill-参考)
2. [Frontend Skill 参考](#frontend-skill-参考)
3. [与 create-project 的关系](#与-create-project-的关系)

---

## Backend Skill 参考

入口文件：`skills/backend/SKILL.md`（15 个能力域路由）

### 关键规范速查

#### ID 类型：必须 String

IIDP 所有模型主键均为雪花算法转字符串，**禁止使用 Long 类型**：

```java
// ✅ 正确
private String id;
String studentId;

// ❌ 错误
private Long id;
Long studentId;
```

#### 审计字段：isAutoLog 自动维护

只需在 `@Model` 注解中声明 `isAutoLog = Bool.True`，平台自动维护 `create_user`、`create_date`、`update_user`、`update_date` 四个字段。**禁止在模型类中手动声明这四个字段**。

```java
// ✅ 正确
@Model(displayName = "学生", isAutoLog = Bool.True)
public class StudentModel extends BaseModel<StudentModel> {
    // 不需要手动声明审计字段
}

// ❌ 错误（手动声明审计字段）
@Model(displayName = "学生", isAutoLog = Bool.True)
public class StudentModel extends BaseModel<StudentModel> {
    private String createUser;   // 错误！
    private Date createDate;     // 错误！
}
```

#### ManyToOne：必须成对声明

每个 ManyToOne 关系需要**两个字段**配对：

```java
// ✅ 正确：FK String ID 字段 + ManyToOne ORM 对象字段，两者缺一不可
@Selection(model = "ClassModel", properties = {"id", "className"})
@Property(displayName = "班级")
@Validate.NotBlank
private String classId;          // FK ID 字段，存入数据库

@ManyToOne(displayName = "班级", cascade = CascadeType.DEL_SET_NULL)
@JoinColumn(name = "class_id", referencedProperty = "id")
private ClassModel classModel;   // ORM 对象字段，不存库，用于关联查询

// ❌ 错误：只有 ManyToOne 对象字段，缺少 FK String ID 字段
@ManyToOne(displayName = "班级")
@JoinColumn(name = "class_id")
private ClassModel classModel;   // 缺少配对的 String classId 字段！
```

#### 平台注解（非 Lombok / Spring 原生）

| 正确（IIDP 平台） | 错误（禁止使用） |
|---|---|
| `@StaticVar` | Lombok `@Data` / `@Getter` / `@Setter` |
| `@Getter` / `@Setter`（平台版） | Spring `@Service` 原生注解 |
| `@MethodService` | `@Component` / `@Bean` 声明服务 |
| `Bool.True` / `Bool.False` | Java 原生 `boolean` / `Boolean` |

#### 字段名大小写严格匹配

`@Property`、`set()`、`getStr()` 等 Map 操作的字符串参数，必须与 Java `private` 字段声明**完全相同**（区分大小写）：

```java
// 字段声明
private String subClass;

// ✅ 正确
set("subClass", value);
getStr("subClass");

// ❌ 错误
set("subclass", value);   // 大小写不一致！
set("SubClass", value);   // 大小写不一致！
```

### 核心参考文件路径

| 文件 | 内容 |
|---|---|
| `skills/backend/references/core/platform-standards.md` | 项目宪法：命名/注解/常量/异常/SQL 规范 |
| `skills/backend/references/core/model-property-advanced.md` | 模型字段高级用法：ER 指令集、ManyToOne、计算字段 |
| `skills/backend/references/core/method-service.md` | 服务层规范：@MethodService、事务、异常处理 |
| `skills/backend/references/core/view.md` | 视图层规范：视图 key 命名、grid/search/form 配对 |
| `skills/backend/references/core/permission.md` | 权限规范：权限码格式、菜单挂载、按钮级权限 |
| `skills/backend/references/core/api-contract.md` | API 契约：请求/响应格式、分页参数、错误码 |

---

## Frontend Skill 参考

入口文件：`skills/frontend/SKILL.md`（路由到 6 个子技能）

### 6 个子技能说明

| 子技能 | 入口文件 | 用途 |
|---|---|---|
| **init** | `skills/frontend/init/SKILL.md` | 初始化 IIDP 前端工程（首次建项目） |
| **spec-code** | `skills/frontend/spec-code/SKILL.md` | 根据 frontend-spec.md 生成前端代码 |
| **spec-doc** | `skills/frontend/spec-doc/SKILL.md` | 生成前端规格文档（节点树 / selector） |
| **dev-manual** | `skills/frontend/dev-manual/SKILL.md` | 前端开发手册（Hook / 生命周期 / 调试） |
| **extension-dev** | `skills/frontend/extension-dev/SKILL.md` | Extension 扩展视图开发 |
| **standard-ids** | `skills/frontend/standard-ids/SKILL.md` | 标准 ID 规范（视图 key / 组件 ID 命名） |

### 常用场景

| 场景 | 使用子技能 |
|---|---|
| 标准模板页（list + form，无需自定义代码） | spec-code（直接配置视图 key） |
| 需要自定义交互的页面 | extension-dev |
| 审批流 / 状态步骤条 | extension-dev + dev-manual |
| 自定义 Vue2 组件 | extension-dev（Extension 扩展组件） |
| 前端节点树规格 | spec-doc |

### IIDP 前端核心概念

- **视图 key**：格式 `{appPkg}.{modelName}.{action}`，在 `contracts.md` 中统一定义，frontend-spec.md 直接引用
- **标准模板页**：list（列表）+ form（新增/编辑弹窗）+ search（搜索栏），通过视图 key 配置，无需前端代码
- **Extension**：需要超出标准模板的自定义交互时，在 Extension 扩展视图中用 Vue2 组件实现
- **Hook**：IIDP 前端生命周期钩子，用于在标准模板页面中注入自定义逻辑

---

## 与 create-project 的关系

```
create-project 命令
      ↓ (读取)
backend/SKILL.md → 路由到对应能力域文件
frontend/SKILL.md → 路由到对应子技能文件
      ↓ (生成)
backend-spec.md（字段注解、服务设计、视图 key 均符合平台规范）
frontend-spec.md（节点树、selector、实现分支均符合前端规范）
      ↓ (实现)
/sdd-implement 按 spec 文件 + backend/frontend skill 规范生成代码
```

具体来说：
- `/sdd-spec` 生成规格时，会 `@` 引用 `skills/backend/references/sdd-backend.md` 和 `skills/frontend/references/sdd-frontend.md` 模板
- `/sdd-implement` 执行任务时，子 skill 从 spec 文件读取参数，按 backend/frontend 规范生成符合 IIDP 平台标准的代码
- **生成任何后端内容前**，必须先读取 `skills/backend/references/core/platform-standards.md`（项目宪法），确保命名、代码规范、异常处理等所有约定符合 IIDP 平台强制要求
