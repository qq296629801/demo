# 需求文档 — 系统用户管理

> 来源：test-scenarios.md 场景 1（RuoYi-Vue-Pro 用户管理，IIDP 适配版）

## 实体清单

### SysUser（系统用户）
| 字段 | 类型 | 说明 |
|---|---|---|
| id | String | 雪花 ID（平台生成） |
| username | String | 用户名，唯一，必填 |
| nickname | String | 昵称，必填 |
| status | String | 状态：ENABLE / DISABLE |
| deptId | String | 部门外键（FK） |
| dept | SysDept | 部门（ManyToOne，ORM 对象） |

审计字段（createTime/updateTime/createUser/updateUser）由平台 isAutoLog=Bool.True 自动维护，不手动声明。

### SysDept（部门）
| 字段 | 类型 | 说明 |
|---|---|---|
| id | String | 雪花 ID |
| name | String | 部门名称，必填 |
| parentId | String | 父部门外键（FK，自引用） |
| parent | SysDept | 父部门（ManyToOne，ORM 对象，自引用） |

## 服务清单

| 服务 | 模型 | 方法 | 描述 |
|---|---|---|---|
| 内置 CRUD | SysUser | search/create/update/delete | 用户增删改查 |
| 内置 CRUD | SysDept | search/create/update/delete | 部门增删改查 |
| 状态变更 | SysUser | enable / disable | 启用/停用用户 |

## 用户故事

### US-001 用户列表查询
作为管理员，我希望能查询用户列表（支持按用户名、状态筛选），以便管理系统用户。
- AC-001-1：查询返回分页列表（含 total、list、pageNum、pageSize）
- AC-001-2：支持按 status 筛选（ENABLE/DISABLE）

### US-002 新增用户
作为管理员，我希望能新增用户（填写用户名、昵称、所属部门），以便录入新员工。
- AC-002-1：成功创建，返回新用户 id
- AC-002-2：username 为空时返回校验错误

### US-003 编辑用户
作为管理员，我希望能修改用户信息（昵称、部门），以便维护用户资料。
- AC-003-1：成功修改，返回更新后数据

### US-004 删除用户
作为管理员，我希望能删除用户，以便清理无效账号。
- AC-004-1：成功删除，返回操作结果

### US-005 用户状态变更
作为管理员，我希望能启用或停用用户，以便控制用户访问权限。
- AC-005-1：启用用户（status DISABLE → ENABLE），成功返回
- AC-005-2：停用用户（status ENABLE → DISABLE），成功返回

## 验收标准补充

- 所有 ID 字段使用 String 类型（IIDP 雪花算法）
- ManyToOne 关系必须同时声明 FK String 字段 + ORM 对象字段
- 审计字段不得手动声明（isAutoLog=Bool.True 自动维护）
