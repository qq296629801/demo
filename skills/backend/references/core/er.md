# 使用OneToMany实现ManyToMany

## 需求

使用 ManyToMany 注解会生成一个中间表，但该中间表无法直接添加额外的业务字段。如果希望中间表包含自定义业务字段（例如有效期字段），可以改用 OneToMany + ManyToOne 的方式来实现。

例如，在用户与角色的关联场景中，我们希望在中间表中加入“有效开始时间/结束时间”等业务字段，这时就应使用 OneToMany + ManyToOne 的实现方式。

## 版本要求

1. 引擎与前端要求 v2.9.1 正式版
2. 权限 APP sie-iidp-iam >= v2.9.1-UAT.025

## 效果图

表格效果

![grid](./images/user_o2m_as_m2m_grid.png)

添加角色效果

![dialog](./images/user_o2m_as_m2m_dialog.png)


## 实现

### 模型

#### UserM2M.java

```java
@Model(displayName = "Many2Many demo 用户", isAutoLog = Bool.True, isLogicDelete = Bool.True)
public class UserM2M extends BaseModel<UserM2M> {

    @Property(displayName = "姓名", displayForModel = true)
    private String name;

    @Property(displayName = "用户名")
    private String username;

    @Property(displayName = "邮箱")
    private String email;

    @Property(displayName = "备注")
    private String remark;

    @OneToMany(displayName = "角色")
    private List<UserRoleRelM2M> roles;
}
```

#### RoleM2M.java

```java
@Model(isAutoLog = Bool.True, isLogicDelete = Bool.True)
public class RoleM2M extends BaseModel<RoleM2M> {

    @Property(displayName = "名称", displayForModel = true)
    private String name;

    @Property(displayName = "编码")
    private String code;

    @Property(displayName = "是否是管理员")
    @Selection(values = {@Option(label = "否", value = "0"), @Option(label = "是", value = "1")})
    private Boolean isAdmin;

    @OneToMany(displayName = "用户")
    private List<UserRoleRelM2M> users;
}
```

#### UserRoleRelM2M.java

注意：如果要在视图中显示 ManyToOne 关联对象的字段，且该字段不是简单的字符串类型（String），需要使用 `related` 显式声明该字段，以便前端正确地显示对应的文本（例如中文标签）。否则像 `role.isAdmin` 这种字段，前端直接读出来可能是 0 / 1，而不是对应的“否/是”标签。

在该中间模型中，两个 `ManyToOne` 注解均设置了 `cascade = CascadeType.DELETE`，这样在删除用户或角色时，中间表记录会随之删除。如果不设置 `cascade = CascadeType.DELETE`，删除用户或角色时中间表数据不会被自动删除，从而可能导致另一侧仍能看到该条已失效的数据。

```java
@Model(isAutoLog = Bool.True, isLogicDelete = Bool.True)
public class UserRoleRelM2M extends BaseModel<UserRoleRelM2M> {

    @ManyToOne(cascade = CascadeType.DELETE)
    @JoinColumn
    private UserM2M user;

    @ManyToOne(cascade = CascadeType.DELETE)
    @JoinColumn
    private RoleM2M role;

    @Property(displayName = "是否管理员", related = "role.isAdmin", store = false)
    @Selection(values = {@Option(label = "否", value = "0"), @Option(label = "是", value = "1")})
    private Boolean roleIsAdmin;

    @Property(displayName = "有效开始时间")
    private Date startDate;

    @Property(displayName = "有效结束时间")
    private Date endDate;
}
```

### 视图

#### m2m_user_role.json

先用开发者中心的“模型管理”生成中间模型的视图，然后重点修改每个视图的 `columns`（列），以显示两端关联对象的字段。

示例：将列配置为：

```
"columns": [
  "role.name",
  "role.code",
  "user.name",
  "user.username"
]
```

完整示例：

```json
{
  "views": {
    "UserRoleRelM2M_grid": {
      "mode": "primary",
      "name": "UserRoleRelM2M-表格",
      "model": "UserRoleRelM2M",
      "type": "grid",
      "body": {
        "buttons": [
          {
            "auth": "read",
            "name": "详情",
            "action": "preview"
          },
          {
            "auth": "update",
            "name": "编辑",
            "action": "edit"
          }
        ],
        "columns": [
          "role.name",
          "role.code",
          "roleIsAdmin",
          "user.name",
          "user.username",
          {
            "displayName": "有效开始时间",
            "name": "startDate"
          },
          {
            "displayName": "有效结束时间",
            "name": "endDate"
          }
        ],
        "tbar": [
          {
            "auth": "create",
            "name": "新增",
            "action": "create"
          },
          {
            "auth": "delete",
            "name": "删除",
            "action": "delete"
          }
        ],
        "type": "grid"
      }
    },
    "UserRoleRelM2M_form": {
      "mode": "primary",
      "name": "UserRoleRelM2M-表单",
      "model": "UserRoleRelM2M",
      "type": "form",
      "body": {
        "columns": [
          "role.name",
          "role.code",
          "roleIsAdmin",
          "user.name",
          "user.username",
          {
            "displayName": "有效开始时间",
            "name": "startDate"
          },
          {
            "displayName": "有效结束时间",
            "name": "endDate"
          }
        ],
        "tabs": [],
        "type": "form"
      }
    },
    "UserRoleRelM2M_search": {
      "mode": "primary",
      "name": "UserRoleRelM2M-搜索",
      "model": "UserRoleRelM2M",
      "type": "search",
      "body": {
        "columns": [
          "role.name",
          "role.code",
          "user.name",
          "user.username"
        ],
        "type": "search"
      }
    }
  }
}
```

#### m2m_user.json

重点说明 `UserM2M_form` 的配置要点：

1. 在 `body` 中配置 `manyToMany`，用于前端处理多对多的“选择/添加”动作。`field` 表示中间模型中指向“对方模型”的字段（在用户视图中我们要看到的是角色的信息，因此这里写 `role`）；`type` 填写打开的视图类型（例如 `RoleM2M_grid,RoleM2M_search,RoleM2M_form`）。

```json
"manyToMany": {
  "field": "role",
  "type": "RoleM2M_grid,RoleM2M_search,RoleM2M_form"
}
```

2. 修改选项卡（tab）中的工具栏按钮  
OneToMany 默认生成的按钮是 `createEr`，但在多对多选择场景中应使用 `addEr`，这样前端会打开“添加角色”的弹出框。

```json
"tbar": [
  {
    "auth": "update",
    "name": "添加角色",
    "action": "addEr"
  }
]
```

3. 子表格（tab）中只显示角色相关字段  
中间模型的视图可能同时包含用户和角色字段，但在用户详情的角色标签页中只需显示角色相关列，所以 `columns` 只配置角色字段。

```json
"columns": [
  "role.name",
  "role.code",
  "roleIsAdmin",
  {
    "displayName": "有效开始时间",
    "name": "startDate"
  },
  {
    "displayName": "有效结束时间",
    "name": "endDate"
  }
]
```

完整示例：

```json
{
  "views": {
    "UserM2M_grid": {
      "mode": "primary",
      "name": "Many2Many demo 用户-表格",
      "model": "UserM2M",
      "type": "grid",
      "body": {
        "buttons": [
          {
            "auth": "read",
            "name": "详情",
            "action": "preview"
          },
          {
            "auth": "update",
            "name": "编辑",
            "action": "edit"
          }
        ],
        "columns": [
          {
            "displayName": "姓名",
            "name": "name"
          },
          {
            "displayName": "用户名",
            "name": "username"
          },
          {
            "displayName": "备注",
            "name": "remark"
          },
          {
            "displayName": "邮箱",
            "name": "email"
          }
        ],
        "tbar": [
          {
            "auth": "create",
            "name": "新增",
            "action": "create"
          },
          {
            "auth": "delete",
            "name": "删除",
            "action": "delete"
          }
        ],
        "type": "grid"
      }
    },
    "UserM2M_form": {
      "mode": "primary",
      "name": "Many2Many demo 用户-表单",
      "model": "UserM2M",
      "type": "form",
      "body": {
        "columns": [
          {
            "displayName": "姓名",
            "name": "name"
          },
          {
            "displayName": "用户名",
            "name": "username"
          },
          {
            "displayName": "备注",
            "name": "remark"
          },
          {
            "displayName": "邮箱",
            "name": "email"
          }
        ],
        "tabs": [
          {
            "header": "角色",
            "tbar": [
              {
                "auth": "update",
                "name": "添加角色",
                "action": "addEr"
              },
              {
                "auth": "delete",
                "name": "删除",
                "action": "deleteEr"
              }
            ],
            "body": {
              "buttons": [
                {
                  "auth": "read",
                  "name": "详情",
                  "action": "previewEr"
                },
                {
                  "auth": "update",
                  "name": "编辑",
                  "action": "updateEr"
                }
              ],
              "manyToMany": {
                "field": "role",
                "type": "RoleM2M_grid,RoleM2M_search,RoleM2M_form"
              },
              "field": "roles",
              "columns": [
                "role.name",
                "role.code",
                "roleIsAdmin",
                {
                  "displayName": "有效开始时间",
                  "name": "startDate"
                },
                {
                  "displayName": "有效结束时间",
                  "name": "endDate"
                }
              ],
              "type": "UserRoleRelM2M_form,UserRoleRelM2M_grid,UserRoleRelM2M_search"
            }
          }
        ],
        "type": "form"
      }
    },
    "UserM2M_search": {
      "mode": "primary",
      "name": "Many2Many demo 用户-搜索",
      "model": "UserM2M",
      "type": "search",
      "body": {
        "columns": [
          {
            "displayName": "姓名",
            "name": "name"
          },
          {
            "displayName": "用户名",
            "name": "username"
          },
          {
            "displayName": "邮箱",
            "name": "email"
          }
        ],
        "type": "search"
      }
    }
  }
}
```

#### m2m_role.json

与用户视图类似，但字段替换为用户相关字段。

完整示例：

```json
{
  "views": {
    "RoleM2M_search": {
      "mode": "primary",
      "name": "RoleM2M-搜索",
      "model": "RoleM2M",
      "type": "search",
      "body": {
        "columns": [
          {
            "displayName": "名称",
            "name": "name"
          },
          {
            "displayName": "编码",
            "name": "code"
          },
          {
            "displayName": "是否是管理员",
            "name": "isAdmin"
          }
        ],
        "type": "search"
      }
    },
    "RoleM2M_form": {
      "mode": "primary",
      "name": "RoleM2M-表单",
      "model": "RoleM2M",
      "type": "form",
      "body": {
        "columns": [
          {
            "displayName": "名称",
            "name": "name"
          },
          {
            "displayName": "编码",
            "name": "code"
          },
          {
            "displayName": "是否是管理员",
            "name": "isAdmin"
          }
        ],
        "tabs": [
          {
            "header": "用户",
            "tbar": [
              {
                "auth": "update",
                "name": "添加用户",
                "action": "addEr"
              },
              {
                "auth": "delete",
                "name": "删除",
                "action": "deleteEr"
              }
            ],
            "body": {
              "buttons": [
                {
                  "auth": "read",
                  "name": "详情",
                  "action": "previewEr"
                },
                {
                  "auth": "update",
                  "name": "编辑",
                  "action": "updateEr"
                }
              ],
              "field": "users",
              "manyToMany": {
                "field": "user",
                "type": "UserM2M_grid,UserM2M_search,UserM2M_form"
              },
              "columns": [
                "user.name",
                "user.username",
                {
                  "displayName": "有效开始时间",
                  "name": "startDate"
                },
                {
                  "displayName": "有效结束时间",
                  "name": "endDate"
                }
              ],
              "type": "UserRoleRelM2M_form,UserRoleRelM2M_grid,UserRoleRelM2M_search"
            }
          }
        ],
        "type": "form"
      }
    },
    "RoleM2M_grid": {
      "mode": "primary",
      "name": "RoleM2M-表格",
      "model": "RoleM2M",
      "type": "grid",
      "body": {
        "buttons": [
          {
            "auth": "read",
            "name": "详情",
            "action": "preview"
          },
          {
            "auth": "update",
            "name": "编辑",
            "action": "edit"
          }
        ],
        "columns": [
          {
            "displayName": "名称",
            "name": "name"
          },
          {
            "displayName": "编码",
            "name": "code"
          },
          {
            "displayName": "是否是管理员",
            "name": "isAdmin"
          }
        ],
        "tbar": [
          {
            "auth": "create",
            "name": "新增",
            "action": "create"
          },
          {
            "auth": "delete",
            "name": "删除",
            "action": "delete"
          }
        ],
        "type": "grid"
      }
    }
  }
}
```

### 代码中查询

由于改成 OneToMany 实现后，后端代码的查询方式会有变化。所以如果现有模型已经使用了 ManyToMany，需要评估修改查询代码的工作量。

原来 ManyToMany 的查询方式如下：

```java
@MethodService
public void queryDemo() {
    // 查询指定角色的用户
    List<User> users = new User()
            .search(Filter.in("role_ids", Arrays.asList("1", "2")), Collections.singletonList("*"), 0, 1, null);
}
```

改成 OneToMany 实现后，查询方式如下：

```java
@MethodService
public void queryDemo() {
    String today = "2025-10-10";
    Filter validFilter = Filter.AND(
            Filter.OR(Filter.equalNull("startDate"),
                    Filter.lessOrEqual("startDate", today)),
            Filter.OR(Filter.equalNull("endDate"),
                    Filter.greaterOrEqual("endDate", today)));
    // 查询指定角色的用户
    List<UserM2M> users = new UserM2M()
            .search(Filter.AND(Filter.in("roles.roleId", Arrays.asList("1", "2")), validFilter),
                    Collections.singletonList("*"), 0, 1, null);

    // 查询包含某些用户的角色
    List<RoleM2M> roles = new RoleM2M()
            .search(Filter.AND(Filter.in("users.userId", Arrays.asList("1", "2")), validFilter),
                    Collections.singletonList("*"), 0, 1, null);
}
```

## 已知不支持项

### 子表格无法使用 related 字段进行排序

例如用户菜单里面的角色标签页，虽然可以显示角色的名称，但是后端暂不支持使用 role.name 进行排序。