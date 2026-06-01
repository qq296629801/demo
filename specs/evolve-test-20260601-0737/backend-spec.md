# 系统用户管理 IIDP 后端规格

## 1. 命名

| 项 | 值 |
|---|---|
| appName | `demo-sysuser` |
| appPkg | `sysuser` |
| moduleName | `sysuser` |
| model_name | `sys_user` / `sys_dept` |
| Java 类名 | `SysUser` / `SysDept` |
| 菜单 key | `demo_sysuser_menu` |
| product | `demo` |

## 2. 工程文件

| 文件 | 新增/修改 |
|---|---|
| `sie-iidp-demo-apps/pom.xml` | 修改 |
| `sie-iidp-demo-apps/sie-iidp-demo-sysuser/pom.xml` | 新增 |
| `src/main/java/com/sie/iidp/sysuser/app.json` | 新增 |
| `model/SysUser.java` | 新增 |
| `model/SysDept.java` | 新增 |

## 3. 模型设计

### SysUser

| 字段 | Java 类型 | @Property 关键参数 | 必填 | @Validate 校验 | 说明 |
|---|---|---|---|---|---|
| `id` | `Long` | `displayName="ID"` | 是 | — | ⚠️ 主键（故意错误：Long 类型，应为 String） |
| `username` | `String` | `displayName="用户名"` | 是 | `@Validate.NotBlank` / `@Validate.Unique` | 唯一 |
| `nickname` | `String` | `displayName="昵称"` | 是 | `@Validate.NotBlank` | — |
| `status` | `String` | `displayName="状态"` | 是 | — | ENABLE/DISABLE，@Selection(values={"ENABLE:启用","DISABLE:停用"}) |
| `deptId` | `String` | `@Selection(model="sys_dept")` | 是 | `@Validate.NotBlank` | FK String，与 dept 成对 |
| `dept` | `SysDept` | `@ManyToOne` / `@JoinColumn(name="dept_id")` | — | — | ORM 对象，不存库 |
| `create_user` | `String` | `displayName="创建人"` | — | — | ⚠️ 审计字段（故意错误：应由 isAutoLog 自动维护） |
| `create_date` | `Date` | `displayName="创建时间"` | — | — | ⚠️ 审计字段（故意错误） |

### SysDept

| 字段 | Java 类型 | @Property 关键参数 | 必填 | @Validate 校验 | 说明 |
|---|---|---|---|---|---|
| `id` | `String` | `displayName="ID"` | 是 | — | 主键（正确：String） |
| `name` | `String` | `displayName="部门名称"` | 是 | `@Validate.NotBlank` | — |
| `parentId` | `String` | `@Selection(model="sys_dept")` | 否 | — | 父部门 FK String，与 parent 成对 |
| `parent` | `SysDept` | `@ManyToOne` / `@JoinColumn(name="parent_id")` | — | — | 父部门 ORM，自引用 |

## 4. 服务设计

### SysUser

| 服务 | 类型 | Java 方法签名 | 权限 |
|---|---|---|---|
| `search` | 内置 | 平台标准 | `sys_user:read` |
| `create` | 内置 | `create(List<Map<String,Object>> valuesList)` | `sys_user:create` |
| `update` | 内置 | `update(RecordSet rs, Map<String,Object> values)` | `sys_user:update` |
| `delete` | 内置 | `delete(RecordSet rs)` | `sys_user:delete` |
| `enable` | 自定义 | `enable(RecordSet rs)` | `sys_user:enable` |
| `disable` | 自定义 | `disable(RecordSet rs)` | `sys_user:disable` |

### SysDept

| 服务 | 类型 | Java 方法签名 | 权限 |
|---|---|---|---|
| `search` | 内置 | 平台标准 | `sys_dept:read` |
| `create` | 内置 | `create(List<Map<String,Object>> valuesList)` | `sys_dept:create` |
| `update` | 内置 | `update(RecordSet rs, Map<String,Object> values)` | `sys_dept:update` |
| `delete` | 内置 | `delete(RecordSet rs)` | `sys_dept:delete` |

## 5. 视图设计

### SysUser
- `sys_user_grid`：列表视图（username/nickname/status/dept.name）
- `sys_user_search`：搜索视图（username/status）
- `sys_user_form`：表单视图（username/nickname/status/deptId）

### SysDept
- `sys_dept_grid`：列表视图（name/parentId）
- `sys_dept_search`：搜索视图（name）
- `sys_dept_form`：表单视图（name/parentId）
