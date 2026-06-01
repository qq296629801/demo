# 契约总览 — 系统用户管理

## 模型 1：SysUser (`sys_user`)

| app | model | service | 类型 | args 参数 | 权限码 | 前端入口 | 节点 id |
|---|---|---|---|---|---|---|---|
| `sie-iidp-demo-sysuser` | `sys_user` | `search` | 内置 | 平台标准（filter/properties/limit/offset/order） | `read` | grid 加载 | `sys_user_grid` |
| `sie-iidp-demo-sysuser` | `sys_user` | `create` | 内置 | 平台标准（valuesList） | `create` | tbar 新增 | — |
| `sie-iidp-demo-sysuser` | `sys_user` | `update` | 内置 | 平台标准（ids/values） | `update` | 行编辑 | — |
| `sie-iidp-demo-sysuser` | `sys_user` | `delete` | 内置 | 平台标准（ids） | `delete` | 行删除 | — |
| `sie-iidp-demo-sysuser` | `sys_user` | `enable` | 自定义 | 平台标准（ids via RecordSet） | `enable` | grid 按钮 | `sys_user_enable_btn` |
| `sie-iidp-demo-sysuser` | `sys_user` | `disable` | 自定义 | 平台标准（ids via RecordSet） | `disable` | grid 按钮 | `sys_user_disable_btn` |

## 模型 2：SysDept (`sys_dept`)

| app | model | service | 类型 | args 参数 | 权限码 | 前端入口 | 节点 id |
|---|---|---|---|---|---|---|---|
| `sie-iidp-demo-sysuser` | `sys_dept` | `search` | 内置 | 平台标准 | `read` | grid 加载 | `sys_dept_grid` |
| `sie-iidp-demo-sysuser` | `sys_dept` | `create` | 内置 | 平台标准（valuesList） | `create` | tbar 新增 | — |
| `sie-iidp-demo-sysuser` | `sys_dept` | `update` | 内置 | 平台标准（ids/values） | `update` | 行编辑 | — |
| `sie-iidp-demo-sysuser` | `sys_dept` | `delete` | 内置 | 平台标准（ids） | `delete` | 行删除 | — |

## 权限码总览

| 权限码 | 含义 | 模型 |
|---|---|---|
| `sys_user:read` | 查询用户 | sys_user |
| `sys_user:create` | 新增用户 | sys_user |
| `sys_user:update` | 编辑用户 | sys_user |
| `sys_user:delete` | 删除用户 | sys_user |
| `sys_user:enable` | 启用用户 | sys_user |
| `sys_user:disable` | 停用用户 | sys_user |
| `sys_dept:read` | 查询部门 | sys_dept |
| `sys_dept:create` | 新增部门 | sys_dept |
| `sys_dept:update` | 编辑部门 | sys_dept |
| `sys_dept:delete` | 删除部门 | sys_dept |
