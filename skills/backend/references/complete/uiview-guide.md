# 后端视图开发手册



# 第一部分：后端基础视图开发

## 第一章：视图文件结构与配置

### **1.1 核心概念**

在赛意谷神平台中，**视图是 `ui_view` 模型的实例**，所有视图都通过JSON文件定义，包含以下关键特性：

- **模型驱动**：每个视图必须关联一个数据模型
- **JSON定义**：视图配置采用JSON格式
- **文件存储**：视图文件存储在特定目录中

### **1.2 项目目录结构**

**resolved路径**: `resolved` 是模块的唯一包路径标识，对应物理目录。平台使用此标识定位模块资源、加载配置，机制类似Java包名。

**视图路径**: 在`resolved`包下新建 **`views/`** 目录，所有视图JSON文件必须放置于此目录。

**种子数据路径**: 在`resolved`包下新建 **`data/`** 目录，所有种子数据文件必须放置于此目录。

```
com.sie.iidp.iam/                    # resolved包（如com.sie.iidp.iam）
├── views/                            # 视图目录（必须）
│   ├── rbac_user_view.json          # 用户管理视图
│   ├── menu_view.json               # 菜单配置视图
│   ├── ui_view_seed_view.json       # 后端视图
│   └── ...                         # 其他视图文件
├── data/                            # 种子数据目录（必须）
│   ├── rbac_user.json              # 用户种子数据
│   ├── rbac_role.json              # 角色种子数据
│   └── ...                         # 其他数据文件
├── src/                            # 源代码目录（可选）
├── resources/                      # 资源文件（可选）
└── app.json                        # 应用配置文件（必须）
```

![image-20251229100936851](/iidpwiki/uploads/uiview-guide/ui-view-resolve-1.png)



**`app.json`配置示例：**

```json
{
    "name": "sie-iidp-iam",
    "displayName": "权限",
    "author": "snest",
    "company": "sie",
    "category": "base",
    "categoryDesc": "权限模块",
    "product": "base",
    "productDesc": "工业互联网平台",
    "description": "权限系统",
    "summary": "权限系统",
    "type": "SDK",
    "tag": "master",
    "version": "v3.0.0-RELEASE",
    "resolved": "com.sie.iidp.iam",
    "dependencies": [
        "iidp-user-prefer",
        "base-cache",
        "sie-iidp-tenant"
    ],
    "application": true,
    "icon": null,
    "global": true,
    "reinstall": true,
    "license": "LGPL 3.0",
    "sequence": 30,
    "view": [
        "views/menu_view.json",
        "views/rbac_role_view.json",
        "views/sdk_user_view.json",
        "views/sdk_view_ref_model.json",
        "views/ui_menu_view.json",
        "views/ui_view_seed_view.json"
    ],
    "data": [
        "data/rbac_tenant.json",
        "data/rbac_user.json",
        "data/rbac_token.json"
    ],
    "events": {
        "broadcast": [
            "tenant_action_scope::syncSgData",
            "tenant_permission::initAuth"
        ],
        "startUp": [
            "tenant_action_scope::syncSgData",
            "init_user_session::start"
        ],
        "register": [],
        "login": [
            "rbac_user::setUserInfo"
        ]
    },
    "globalConfig": {
        "sgWhiteList": {
            "value": "*.custom_view_model,*.rbac_password_policy,*.ui_menu,*.tenant_action_dimension,*.rbac_token"
        },
        "urlWhiteList": {
            "value": "sie-iidp-iam.access_auth_model.getPermissionByTenantId"
        }
    },
    "appConfig": {
        "multiSg": {
            "desc": "设置作用域切换值是否多选：true，多选；false，单选。注意：若将多选改为单选（true改为false），会清空已选的多选项。",
            "value": "false"
        },
        "safeSg": {
            "desc": "设置作用域是否支持安全值：true为支持(开启后，如果用户没有关联实例，会显示未授权)，false为关闭。",
            "value": "false"
        }
    }
}
```



## **第二章：基础视图类型详解**

### **2.1 表单视图（Form View）**

用于创建或编辑单条记录。

![image-20251229111105190](/iidpwiki/uploads/uiview-guide/image-20251229111105190.png)

##### **1. 基础结构：**

```json
{
  "rbac_user_form": {
    "name": "用户表单",
    "model": "rbac_user",
    "type": "form",
    "mode": "primary",
    "body": {
      "type": "form",
      "columns": ["name", "login", "mobile", "email"],
      "tabs": [...],
      "tbar": [...]
    }
  }
}
```

**参数说明：**

| 参数           | 类型   | 必填 | 描述                   | 示例值              |
| -------------- | ------ | ---- | ---------------------- | ------------------- |
| `name`         | String | 是   | 视图显示名称           | "用户表单"          |
| `model`        | String | 是   | 关联的模型名称         | "rbac_user"         |
| `type`         | String | 是   | 视图类型，固定为`form` | "form"              |
| `mode`         | String | 是   | 视图模式               | "primary"（主视图） |
| `body.type`    | String | 是   | 主体类型，固定为`form` | "form"              |
| `body.columns` | Array  | 是   | 表单中显示的字段数组   | `["name", "login"]` |

##### **2. 基础表单示例：**

```json
  {
    "rbac_user_form": {
      "name": "用户表单",
      "model": "rbac_user",
      "type": "form",
      "mode": "primary",
      "body": {
        "type": "form",
        "columns": [
          {
            "displayName": "姓名",
            "name": "name"
          },
          {
            "displayName": "组织",
            "name": "org_id"
          },
          {
            "displayName": "登录名",
            "name": "login"
          },
          {
            "displayName": "手机号",
            "name": "mobile",
            "disable_update": "edit"
          },
          {
            "displayName": "邮箱",
            "name": "email"
          },
          {
            "displayName": "密码",
            "name": "password",
            "disable_update": "edit"
          }
        ],
        "tabs": [
          {
            "header": "角色",
            "tbar": [
              {
                "name": "添加",
                "action": "addEr",
                "auth": "update"
              },
              {
                "name": "删除",
                "action": "deleteEr",
                "auth": "delete"
              }
            ],
            "body": {
              "type": "rbac_role_grid,rbac_role_form,rbac_role_search",
              "field": "role_ids",
              "columns": [
                "name",
                "is_admin"
              ]
            }
          }
        ],
        "tbar": [
          "@defaults"
        ]
      }
    }
  }
```

##### 3.表单高级示例(带ER关系):

```json
{
  "app_user_form": {
    "mode": "primary",
    "name": "用户管理表单",
    "model": "rbac_user",
    "type": "form",
    "body": {
      "submitChanged": "true",
      "buttons": [
        "@defaults"
      ],
      "tabs": [
        {
          "header": "角色",
          "tbar": [
            {
              "name": "设置角色",
              "action": "addEr",
              "auth": "update"
            },
            {
              "name": "删除",
              "action": "deleteEr",
              "auth": "delete"
            }
          ],
          "body": {
            "type": "app_role_grid,app_role_search,app_role_form",
            "field": "role_ids",
            "columns": [
              "name",
              "code",
              "remark"
            ]
          }
        },
        {
          "header": "用户组",
          "tbar": [
            {
              "name": "设置用户组",
              "action": "addEr",
              "auth": "update"
            },
            {
              "name": "删除",
              "action": "deleteEr",
              "auth": "delete"
            }
          ],
          "body": {
            "type": "app_user_group_grid,app_user_group_form,app_user_group_search",
            "field": "userGroupList",
            "columns": [
              "name",
              "description"
            ]
          }
        },
        {
          "header": "关联维度",
          "tbar": [
            {
              "name": "设置维度数据",
              "action": "addEr",
              "auth": "update"
            },
            {
              "name": "删除",
              "action": "deleteEr",
              "auth": "delete"
            }
          ],
          "body": {
            "type": "app_role_grid,app_role_search,app_role_form",
            "field": "role_ids",
            "checkbox": "multiple_2",
            "checkType": "bgSync",
            "columns": [
              "name",
              "code",
              "remark"
            ]
          }
        }
      ],
      "columns": [
        "name",
        {
          "name": "org_id",
          "custom": true,
          "type": "select",
          "useCustomClick": true,
          "view": {
            "customClick": {
              "customSelect": {
                "showType": "dialog",
                "width": "30%",
                "height": "60vh",
                "useSelectTree": {
                  "preId": "rbac_user_app_menu_form_main_detail_top_common_items_0_items_org_id_",
                  "type": "single"
                }
              }
            }
          }
        },
        "login",
        {
          "name": "password",
          "custom": true,
          "bind_display": "${$ds.clickType==='add'}"
        },
        "avatar",
        "gender",
        "mobile",
        "email",
        {
          "label": "状态",
          "name": "status",
          "defaultValue": "0"
        },
        "remark",
        {
          "label": "租户",
          "name": "tenant_id",
          "display": true,
          "disabled": true
        }
      ],
      "type": "form"
    }
  }
}
```



### **2.2 表格视图（Grid View）**

用于以表格形式展示多条记录。

![image-20251229110312927](/iidpwiki/uploads/uiview-guide/image-20251229110312927.png)

##### 1. 基础结构：

```json
{
  "rbac_user_grid": {
    "name": "用户表格",
    "model": "rbac_user",
    "type": "grid",
    "mode": "primary",
    "body": {
      "type": "grid",
      "columns": ["name", "login", "mobile", "email"],
      "buttons": [...],
      "tbar": [...]
    }
  }
}
```

**参数说明：**

| 参数           | 类型   | 必填 | 描述                   |
| -------------- | ------ | ---- | ---------------------- |
| `name`         | String | 是   | 视图显示名称           |
| `model`        | String | 是   | 关联的模型名称         |
| `type`         | String | 是   | 视图类型，固定为`grid` |
| `mode`         | String | 是   | 视图模式               |
| `body.type`    | String | 是   | 主体类型，固定为`grid` |
| `body.columns` | Array  | 是   | 表格列定义             |
| `body.buttons` | Array  | 否   | 行操作按钮             |
| `body.tbar`    | Array  | 否   | 表格顶部工具栏         |

##### 2. 基础表格示例：

```json
{
  "rbac_user_grid": {
    "name": "用户表格",
    "model": "rbac_user",
    "type": "grid",
    "mode": "primary",
    "body": {
      "type": "grid",
      "columns": [
        {
          "displayName": "姓名",
          "name": "name"
        },
        {
          "displayName": "组织",
          "name": "org_id"
        },
        {
          "displayName": "登录名",
          "name": "login"
        },
        {
          "displayName": "手机号",
          "name": "mobile"
        }
      ],
      "buttons": [
        {
          "name": "编辑",
          "action": "edit",
          "auth": "update"
        },
        {
          "name": "重置密码",
          "auth": "resetPassword",
          "service": "resetPassword",
          "action": "openView",
          "model": "rbac_user",
          "views": "custom",
          "params": [
            "newPassword"
          ],
          "actionAfter": "refreshTable"
        }
      ],
      "tbar": [
        {
          "name": "新增",
          "action": "create",
          "auth": "create"
        },
        {
          "name": "删除",
          "action": "delete",
          "auth": "delete"
        }
      ]
    }
  }
}
```

##### 3.表格高级示例

```json
  {
    "app_user_grid": {
      "mode": "primary",
      "name": "用户管理表格",
      "model": "rbac_user",
      "type": "grid",
      "body": {
        "checkbox": "multiple_2",
        "buttons": [
          {
            "name": "编辑",
            "action": "edit",
            "auth": "update",
            "btnType": "origin_edit"
          },
          {
            "name": "关联维度",
            "action": "edit",
            "auth": "update",
            "btnType": "associated_dimension"
          },
          {
            "name": "重置密码",
            "auth": "resetPassword",
            "service": "resetPassword",
            "action": "openView",
            "model": "rbac_user",
            "views": "custom",
            "params": [
              "newPassword"
            ]
          }
        ],
        "columns": [
          "name",
          {
            "name": "org_id",
            "hidden": true,
            "custom": true
          },
          "orgNamePath",
          "login",
          "remark",
          "email",
          "status",
          "gender",
          "mobile",
          "avatar"
        ],
        "tbar": [
          {
            "name": "新增",
            "action": "create",
            "auth": "create"
          },
          {
            "name": "删除",
            "action": "delete",
            "auth": "delete"
          }
        ],
        "type": "grid",
        "searchByMainTable": {
          "service": {
            "search": "searchUser",
            "count": "count"
          },
          "model": "rbac_user",
          "args": {
            "filter": [
              [
                "userType",
                "=",
                "0"
              ]
            ]
          }
        }
      }
    }

```



### **2.3 搜索视图（Search View）**

定义列表页顶部的筛选条件。

##### 1.**基础结构：**

```json
{
  "rbac_user_search": {
    "name": "用户搜索",
    "model": "rbac_user",
    "type": "search",
    "mode": "primary",
    "body": {
      "type": "search",
      "columns": ["name", "login", "status"]
    }
  }
}
```

##### 2.**基础搜索示例：**

```json
{
  "rbac_user_search": {
    "name": "用户搜索",
    "model": "rbac_user",
    "type": "search",
    "mode": "primary",
    "body": {
      "type": "search",
      "columns": [
        {
          "displayName": "姓名",
          "name": "name"
        },
        {
          "displayName": "登录名",
          "name": "login"
        },
        {
          "displayName": "状态",
          "name": "status"
        }
      ]
    }
  }
}
```

##### 3.高级搜索示例:

```json
{
  "app_user_search": {
    "mode": "primary",
    "name": "租户用户搜索",
    "model": "rbac_user",
    "type": "search",
    "body": {
      "columns": [
        {
          "displayName": "姓名",
          "name": "name"
        },
        {
          "name": "org_id",
          "custom": true,
          "type": "select",
          "useCustomClick": true,
          "view": {
            "customClick": {
              "customSelect": {
                "showType": "dialog",
                "width": "30%",
                "height": "60vh",
                "useSelectTree": {
                  "preId": "rbac_user_app_menu_search_org_id_",
                  "type": "single",
                  "confirm": "(vm,result)=>{sessionStorage.setItem(vm.data.id+'__values',JSON.stringify([vm.$ds.form[vm.data.name]]))}"
                }
              }
            }
          }
        },
        {
          "displayName": "登录名",
          "name": "login"
        },
        {
          "displayName": "状态",
          "name": "status"
        },
        {
          "label": "接收时间",
          "custom": true,
          "display": true,
          "widget": "datetimerange",
          "name": "create_date"
        }
      ],
      "type": "search",
      "searchByMainTable": {
        "service": {
          "search": "search",
          "count": "count"
        },
        "model": "rbac_user",
        "args": {
          "filter": [
            [
              "userType",
              "=",
              "0"
            ]
          ]
        }
      }
    }
  }
}
```

##### 4.**支持的时间选择器类型**

如果字段是日期、时间类型，可以指定 `widget`，前端使用不同的日期组件进行渲染。
目前支持的 `widget` 值如下：

| widget的值    | 含义     | 示例格式（返回）                                |
| ------------- | -------- | ----------------------------------------------- |
| year          | 选择年份 | 2025                                            |
| month         | 选择月份 | 2025-05                                         |
| date          | 选择日期 | 2025-05-28                                      |
| datetime      | 选择时间 | 2025-05-28 14:30:00                             |
| monthrange    | 月份范围 | \["2025-01", "2025-05"]                         |
| daterange     | 日期范围 | \["2025-05-01", "2025-05-28"]                   |
| datetimerange | 时间范围 | \["2025-05-28 00:00:00", "2025-05-28 23:59:59"] |

**配置示例**

```
{
  "name": "create_date",
  "label": "创建时间",
   "display": true,
  "custom": true,
  "widget": "datetimerange"  // 精确到秒的时间范围
}
```



### **2.4 树视图（Tree View）**

展示具有层级结构的数据。 树后端视图配置参考文档:http://iidp.chinasie.com:9999/iidpdoc/pages/4f7a25/

![image-20251229114528096](/iidpwiki/uploads/uiview-guide/image-20251229114528096.png)

##### 1. 基础结构：

```json
{
  "rbac_user_tree": {
    "name": "用户组织树",
    "model": "rbac_user",
    "type": "tree",
    "mode": "primary",
    "body": {
      "type": "tree",
      "model": "rbac_organization",
      "columns": [
        "name",
        "parent_id",
        "description"
      ],
      "props": {
        "parent": "parent_id",
        "label": "name",
        "subApp": "base",
        "subModel": "rbac_user",
        "subViewType": "rbac_organization_grid,rbac_organization_search,rbac_organization_form",
        "subViewFilter": "org_id",
        "subViewFromDefault": "id",
        "hasIcon": true
      },
      "tbar": [
        "@defaults"
      ],
      "buttons": [
        "@defaults"
      ]
    }
  }
}
```

**props参数说明：**

| 参数                | 类型    | 描述             |
| ------------------- | ------- | ---------------- |
| `children`          | String  | 子节点字段名     |
| `parent`            | String  | 父节点字段名     |
| `label`             | String  | 显示标签字段     |
| `expandOnClickNode` | Boolean | 点击节点是否展开 |
| `hasIcon`           | Boolean | 是否显示图标     |

##### 2. 树高级示例：

```json
{
    "rbac_role_tree": {
        "name": "角色树",
        "model": "rbac_role",
        "type": "tree",
        "mode": "primary",
        "body": {
            "type": "tree",
            "model": "rbac_role",
            "columns": [
                "name",
                "code",
                "type",
                "path",
                "parent"
            ],
            "props": {
                "showRoot": true,
                "parent": "parent",
                "label": "name",
                "subApp": "sie-iidp-iam",
                "subModel": "rbac_role",
                "subViewType": "rbac_role_grid,rbac_role_search,rbac_role_form",
                "subViewFilter": "parent",
                "subViewFilterValue": "id",
                "expandOnClickNode": false,
                "hasIcon": false,
                "showTree": true,
                "customSubViewType": "rbac_role_simple_form"
            },
            "tbar": [
                {
                    "action": "create",
                    "name": "新增"
                },
                {
                    "action": "viewAll",
                    "name": "全部",
                    "icon": "iconfont icon-tushi"
                }
            ],
            "searchConfig": {
                "model": "rbac_role",
                "service": "queryRoleTree",
                "args": {
                    "useDisplayForModel": true,
                    "filter": [
                        [
                            "roleType",
                            "=",
                            "0"
                        ]
                    ],
                    "order": "id"
                }
            },
            "buttons": [
                {
                    "action": "create"
                },
                {
                    "action": "edit"
                },
                {
                    "action": "delete",
                    "message": "删除角色，将同步删除当前角色、当前角色下的所有子级角色，请确认是否删除？"
                }
            ]
        }
    }
}
```



### 2.5 上下表视图  "typeView": "subTable"

参考文档:http://iidp.chinasie.com:9999/iidpdoc/pages/b76191/#%E4%B8%8A%E4%B8%8B%E8%A1%A8%E6%A0%BC%E6%A8%A1%E6%9D%BF

####  上下表格模板配置

- typeView 控制子表的显示隐藏 在表格配置
- dbClickEnterDetails 禁止双击进入详情页

```
{
	"mbm_main_process_tech_route_grid": {
		"body": {
			"buttons": [
				{
					"action": "preview",
					"auth": "read",
					"name": "详情"
				},
				{
					"action": "edit",
					"auth": "update",
					"name": "编辑",
					"enableCondition": "(row) => {\r\n              if (row[0].isEnable.value == '1') {\r\n                return false\r\n              } else {\r\n                return true\r\n              }\r\n            }"
				}
			],
			"columns": [
				{
					"displayName": "编码",
					"name": "code"
				},
				{
					"label": "名称",
					"name": "name"
				},
				{
					"displayName": "状态",
					"name": "isEnable"
				},
				{
					"displayName": "制程数",
					"name": "processNum"
				}
			],
			"tbar": [
				{
					"action": "create",
					"auth": "create",
					"name": "新增"
				},
				{
					"action": "delete",
					"auth": "delete",
					"name": "删除"
				},
				{
					"action": "enable",
					"auth": "enable",
					"name": "启用",
					"model": "ProcessTechRouteVO",
					"service": "enable",
					"actionAfter": "refreshTable",
					"options": {
						"icon": "el-icon-open"
					},
					"args": {
						"bind_ids": "$ds.checkedDataIds"
					},
					"bind_disabled": "${$ds.checkedDataList.length === 0}"
				},
				{
					"name": "禁用",
					"action": "disable",
					"auth": "disable",
					"model": "ProcessTechRouteVO",
					"service": "disable",
					"actionAfter": "refreshTable",
					"options": {
						"icon": "el-icon-turn-off"
					},
					"args": {
						"bind_ids": "$ds.checkedDataIds"
					},
					"bind_disabled": "${$ds.checkedDataList.length === 0}"
				},
				{
					"name": "导出",
					"action": "export",
					"source": "selected",
					"properties": [
						"code",
						"name",
						"isEnable",
						"processNum",
						"create_user",
						"create_date",
						"update_user",
						"update_date"
					],
					"model": "ProcessTechRouteVO",
					"service": "excelExport"
				}
			],
			"checkbox": "multiple_2",
			"type": "grid",
			"typeView": "subTable"
		},
		"mode": "primary",
		"model": "ProcessTechRouteVO",
		"name": "制程工艺路线-表格",
		"type": "grid"
	}
}
```





### 2.5 完整的视图示例:

```json
{
    "data": {},
    "views": {
        "rbac_role_grid": {
            "name": "角色表格",
            "model": "rbac_role",
            "type": "grid",
            "mode": "primary",
            "body": {
                "type": "grid",
                "columns": [
                    "name",
                    "code",
                    "is_admin",
                    "remark",
                    "update_user",
                    "update_date",
                    "create_user",
                    "create_date",
                    {
                        "name": "path",
                        "custom": true,
                        "hidden": true
                    }
                ],
                "buttons": [
                    {
                        "name": "详情",
                        "action": "preview",
                        "auth": "read"
                    },
                    {
                        "name": "编辑",
                        "action": "edit",
                        "auth": "update"
                    },
                    {
                        "name": "授权",
                        "model": "rbac_role",
                        "auth": "authPermission",
                        "service": "authPermission",
                        "views": "custom"
                    },
                    {
                        "name": "复制",
                        "model": "rbac_role",
                        "service": "copy",
                        "auth": "copy"
                    }
                ],
                "tbar": [
                    {
                        "name": "新增",
                        "action": "create",
                        "auth": "create"
                    },
                    {
                        "name": "删除",
                        "action": "delete",
                        "auth": "delete"
                    },
                    {
                        "name":"导入",
                        "action": "import",
                        "model": "rbac_role",
                        "service": "Import",
                        "fileLimit": {
                            "ext": ".xls,.xlsx",
                            "maxSize": "2048"
                        }
                    },
                    {
                        "name": "导出",
                        "action": "export",
                        "properties": ["name","code","is_admin","remark","tenant_id"],
                        "model": "rbac_role",
                        "service": "export"
                    }
                ]
            }
        },
        "rbac_role_form": {
            "name": "角色表单",
            "model": "rbac_role",
            "type": "form",
            "mode": "primary",
            "body": {
                "type": "form",
                "columns": [
                    "name",
                    "is_admin",
                    "code",
                    "remark",
                    {
                        "displayName": "父角色",
                        "name": "parent",
                        "display": true
                    }
                ],
                "tabs": [
                    {
                        "header": "用户",
                        "rowspan": 3,
                        "tbar": [
                            {
                                "name": "添加",
                                "action": "addEr",
                                "auth": "update"
                            },
                            {
                                "name": "删除",
                                "action": "deleteEr",
                                "auth": "delete"
                            }
                        ],
                        "body": {
                            "type": "grid,search",
                            "field": "user_ids",
                            "columns": [
                                "login",
                                "name",
                                "email",
                                "mobile",
                                "status"
                            ]
                        }
                    }
                ]
            }
        },
        "rbac_role_simple_form": {
            "name": "角色表单",
            "model": "rbac_role",
            "type": "form",
            "mode": "primary",
            "body": {
                "type": "form",
                "columns": [
                    "name",
                    "code",
                    "remark",
					{
                    "displayName": "父角色",
                    "custom": true,
                    "name": "parent",
                    "bind_disabled": "${$ds.clickType != 'add'}",
                    "type": "select",
                    "required": true,
                    "getData": "async({config})=>{const res=await window.Tech.httpMeta({data:{params:{app:'sie-iidp-iam',model:'rbac_role',service:'lookupParentRole',args:{}}}});if(res?.data){return{items:res.data}}return[]}"
                    },
                    {
                        "displayName": "角色类型",
                        "name": "roleType",
                        "hidden": true
                    },
                    {
                        "displayName": "可删",
                        "name": "roleDelete",
                        "hidden": true
                    }
                ],
                "tabs": []
            }
        },
        "rbac_role_search": {
            "name": "角色查询",
            "model": "rbac_role",
            "mode": "primary",
            "type": "search",
            "body": {
                "type": "search",
                "columns": [
                    "name",
                    "code"
                ]
            }
        },
        "rbac_role_tree": {
            "name": "角色树",
            "model": "rbac_role",
            "type": "tree",
            "mode": "primary",
            "body": {
                "type": "tree",
                "model": "rbac_role",
                "columns": [
                    "name",
                    "code",
                    "type",
                     "path",
                    "parent"
                ],
                "props": {
                    "showRoot": true,
                    "parent": "parent",
                    "label": "name",
                    "subApp": "sie-iidp-iam",
                    "subModel": "rbac_role",
                    "subViewType": "rbac_role_grid,rbac_role_search,rbac_role_form",
                    "subViewFilter": "parent",
                    "subViewFilterValue": "id",
                    "expandOnClickNode": false,
                    "hasIcon": false,
                    "showTree": true,
                    "customSubViewType": "rbac_role_simple_form",
                    "maxLevel": 10
                },
                "tbar": [
                    {
                        "action": "create",
                        "name": "新增"
                    },
                    {
                        "action": "viewAll",
                        "name": "全部",
                        "icon": "iconfont icon-tushi"
                    }
                ],
              	"searchConfig": {
					"model": "rbac_role",
					"service": "queryRoleTree",
					"args": {
						"useDisplayForModel": true,
						"filter": [
							[
								"roleType",
								"=",
								"0"
							]
						],
						"order": "id",
						"showRoot": false
					}
				},
                "buttons": [
                    {
                        "action": "create",
                         "parentDisabled": true
                    },
                    {
                        "action": "edit"
                    },
                    {
                        "action": "delete",
                        "message":"删除角色，将同步删除当前角色、当前角色下的所有子级角色，请确认是否删除？"
                    }
                ]
            }
        }
    }
}

```





## **第三章：基础组件配置**

### **3.1 字段配置（Columns）**

**3.1.1 简单字段定义**

```json
"columns": [
  "name",           // 字符串形式，使用默认配置
  "login",
  "mobile"
]
```

**3.1.2 详细字段定义**

```json
"columns": [
  {
    "name": "name",            // 字段名称（必填）
    "displayName": "姓名",      // 显示名称
    "sortable": true,          // 是否可排序
    "hidden": false,           // 在表格中是否隐藏
    "custom": true            // 是否自定义渲染
  }
]
```

**3.1.3 字段分组（Form中使用）**

```json
{
  "name": "min_length",
  "displayName": "最小长度",
  "span": "6",                // 宽度（总24格）
  "groupConf": {
    "name": "密码策略",       // 分组名称
    "id": "group1"            // 分组ID
  }
}
```

### **3.2 工具栏（tbar）**

**3.2.1 默认工具栏**

```json
"tbar": ["@defaults"]
```

- **grid视图**：默认添加"新增"、"删除"按钮
- **form视图**：默认添加"保存"按钮

**3.2.2 自定义工具栏按钮**

```json
"tbar": [
  {
    "name": "新增", // 按钮显示文本
    "action": "create", // 动作类型
    "auth": "create", // 权限标识
    "icon": "el-icon-plus" // 图标（可选）
  },
  {
    "name": "删除",
    "action": "delete",
    "auth": "delete"
  }
]

// 自定义配置 - ER操作
"tbar": [
  { "name": "添加关联", "action": "addEr", "auth": "update" },
  { "name": "删除关联", "action": "deleteEr", "auth": "delete" }
]
```

**3.2.3 常用 Action 值**

| Action          | 说明       | 适用场景   |
| :-------------- | :--------- | :--------- |
| **`create`**    | 创建记录   | 通用新建   |
| **`delete`**    | 删除记录   | 通用删除   |
| **`import`**    | 导入数据   | 批量导入   |
| **`export`**    | 导出数据   | 数据导出   |
| **`createEr`**  | 创建ER关系 | 多对多关联 |
| **`addEr`**     | 添加ER关系 | 添加关联   |
| **`deleteEr`**  | 删除ER关系 | 移除关联   |
| **`updateEr`**  | 更新ER关系 | 修改关联   |
| **`@defaults`** | 默认配置   | 快速配置   |



### **3.3 行操作按钮（buttons）**

**核心参数**

- **`name`** - 按钮文本
- **`action`** - 动作类型
- **`auth`** - 权限标识
- **`service`** - 后端服务名
- **`enableCondition`** - 启用条件

**3.3.1 默认按钮配置**

```json
"buttons": ["@defaults"]
```

自动添加“详情”、“编辑”按钮

**3.3.2 自定义按钮配置**

```json
"buttons": [
  {
    "name": "编辑",
    "action": "edit",
    "auth": "update"
  },
  {
    "name": "重置密码",
    "auth": "resetPassword",
    "service": "resetPassword",  // 调用的后台服务
    "action": "openView"         // 打开视图
  }
]
```

**3.3.3 常用 Action 值**

**基础操作**

- **`preview`** - 查看详情

- **`edit`** - 编辑记录

- **`delete`**- 删除记录

- **`openView`** - 打开自定义视图



**核心参数**

- **`name`** - 按钮文本

- **`action`** - 动作类型

- **`auth`** - 权限标识

- **`service`** - 后端服务名

- **`enableCondition`** - 启用条件



**3.3.4 完整示例**

```json
"buttons": [
  {
    "action": "preview",
    "auth": "read",
    "name": "详情"
  },
  {
    "action": "edit",
    "auth": "update",
    "name": "编辑"
  },
  {
    "action": "preview",
    "auth": "read",
    "name": "详情",
    "enableCondition": "(row,vm) => { return row && row[0] && row[0].type != 'TENANT_SG'; }"
  },
  {
    "action": "edit",
    "auth": "update",
    "name": "编辑",
    "enableCondition": "(row,vm) => { const hasUniqueElements = (userRoles, authRoles) => userRoles.some(item  => !authRoles.includes(item.id)); return row && row[0] && row[0].type != 'TENANT_SG' && (tech.userInfo.roles.find(item  => item.code  == 'rbac_role_tenant_admin') != null || row[0].noAuthRoles == null || hasUniqueElements(tech.userInfo.roles, row[0].noAuthRoles)) }"
  },
  {
    "action": "edit",
    "auth": "update",
    "buttonType": "copy",
    "name": "复制",
    "enableCondition": "(row,vm) => { return row && row[0] && row[0].type != 'TENANT_SG'; }"
  },
  {
    "action": "delete",
    "auth": "delete",
    "name": "删除",
    "model": "tenant_action_scope",
    "service": "delete",
    "actionAfter": "refreshTable",
    "args": {
      "id": "$row.id"
    },
    "enableCondition": "(row,vm) => { const hasUniqueElements = (userRoles, authRoles) => userRoles.some(item  => !authRoles.includes(item.id)); return row && row[0] && row[0].type != 'TENANT_SG' && (tech.userInfo.roles.find(item  => item.code  == 'rbac_role_tenant_admin') != null || row[0].noAuthRoles == null || hasUniqueElements(tech.userInfo.roles, row[0].noAuthRoles)) }"
  }
]
```





## **第四章：模型关系（ER）集成**

### **4.1 一对多关系（OneToMany）**

```json
{
  "name": "meta_app_category_form",
  "model": "meta_app_category",
  "body": {
    "type": "form",
    "columns": [
      "name",
      "app_ids"  // 必须包含关联字段
    ],
    "tabs": [
      {
        "header": "应用",
        "body": {
          "type": "meta_app_grid",
          "field": "app_ids",  // 关联字段名
          "columns": ["name", "state"]
        },
        "tbar": [
          {
            "name": "添加",
            "action": "addEr",  // ER操作需带Er后缀
            "auth": "update"
          }
        ]
      }
    ]
  }
}
```

### **4.2 多对多 模型-属性(ManyToMany)**

```json
{
  "app_user_form": {
    "mode": "primary",
    "name": "用户管理表单",
    "model": "rbac_user",
    "type": "form",
    "body": {
      "buttons": [
        "@defaults"
      ],
      "tabs": [
        {
          "header": "角色",//ER关系tabs标题
          "tbar": [
            {
              "name": "设置角色",
              "action": "addEr",//ER关系tbar,action需要带后缀er
              "auth": "update"
            },
            {
              "name": "删除",
              "action": "deleteEr",//ER关系tbar,action需要带后缀er
              "auth": "delete"
            }
          ],
          "body": {
            "type": "app_role_grid,app_role_search,app_role_form",//ER关系模型的视图key
            "field": "role_ids",//关联的属性必填
            "columns": [//ER关系模型显示的字段
              "name",
              "code",
              "remark"
            ]
          }
        },
        {
          "header": "用户组",//ER关系tabs标题
          "tbar": [
            {
              "name": "设置用户组",
              "action": "addEr",//ER关系tbar,action需要带后缀er
              "auth": "update"
            },
            {
              "name": "删除",
              "action": "deleteEr",//ER关系tbar,action需要带后缀er
              "auth": "delete"
            }
          ],
          "body": {
            "type": "app_user_group_grid,app_user_group_form,app_user_group_search",//对方模型的视图
            "field": "userGroupList",//关联的Many2Many属性必填
            "columns": [
              "name",
              "description"
            ]
          }
        },
        {
          "header": "关联维度",
          "tbar": [
            {
              "name": "设置维度数据",
              "action": "addEr",
              "auth": "update"
            },
            {
              "name": "删除",
              "action": "deleteEr",
              "auth": "delete"
            }
          ],
          "body": {
            "type": "app_role_grid,app_role_search,app_role_form",
            "field": "role_ids",
            "checkbox": "multiple_2",
            "checkType": "bgSync",
            "columns": [
              "name",
              "code",
              "remark"
            ]
          }
        }
      ],
      "columns": [
        "name",
        {
          "name": "org_id",
          "custom": true
        },
        "login",
        {
          "name": "password",
          "custom": true,
          "bind_display": "${$ds.clickType==='add'}"
        },
        "remark",
        {
          "label": "租户",
          "name": "tenant_id",
          "display": true,
          "disabled": true
        }
      ],
      "type": "form"
    }
  }
}
```

## **第五章：菜单配置**

### **5.1 基础菜单定义**

```json
{
  "menus": {
    "rbac_user_app_menu": {
      "sequence": "10",                           // 排序号
      "view": "rbac_user_grid,rbac_user_form",    // 关联的视图
      "name": "rbac_user_app_menu",               // 菜单唯一标识
      "model": "rbac_user",                       // 关联的模型
      "display_name": "用户管理",                  // 显示名称
      "parent_ids": {
        "@ref": "rbac_user_permission_menu"       // 父菜单引用
      }
    }
  }
}
```

### **5.2 菜单树配置**

```json
{
  "menus": {
    "base_developer_center": {
      "sequence": "31",
      "name": "base_developer_center",
      "display_name": "开发者中心"
    },
    "meta_model_menu": {
      "sequence": "5",
      "name": "meta_model_menu",
      "display_name": "模型",
      "parent_ids": {
        "@ref": "base_developer_center"  // 第一级子菜单
      }
    },
    "meta_sub_model_menu": {
      "sequence": "5",
      "name": "meta_sub_model_menu",
      "display_name": "子模型",
      "parent_ids": {
        "@ref": "meta_model_menu"  // 第二级子菜单
      }
    }
  }
}
```

## **第六章：种子数据（Data）**

### **6.1 基础数据定义**

```json
{
  "data": {
    "rbac_role_admin": {
      "model": "rbac_role",          // 模型名称
      "properties": {                // 字段属性
        "name": "管理员",
        "is_admin": 1
      }
    }
  }
}
```

### **6.2 关联数据定义**

```json
{
  "rbac_user_admin": {
    "model": "rbac_user",
    "properties": {
      "name": "管理员",
      "login": "admin",
      "role_ids": [                  // 多对多关联
        {
          "@eval": "[4, @ref(rbac_role_admin), 0]"  // Many2Many指令
        }
      ]
    }
  }
}
```

**@eval指令说明**：`[4, @ref(rbac_role_admin), 0]`

- `@ref(rbac_role_admin)`：引用已生成的`rbac_role_admin`记录ID



## 第七章：基础开发流程

### **7.1 开发步骤**

1. **定义模型**：确保后端元模型已正确定义
2. **创建视图文件**：在`views/`目录下创建JSON文件
3. **配置基础视图**：按需创建form、grid、search、tree视图
4. **配置菜单**：在`menu_view.json`中定义导航菜单
5. **配置种子数据**：在`data/`目录下创建初始数据
6. **更新app.json**：添加视图文件路径
7. **测试验证**：部署后测试功能

### **7.2 文件命名规范**

- 模型相关视图：`{model_name}_view.json`
- 菜单配置：`menu_view.json`
- 种子数据：`{model_name}.json`

### **7.3 最佳实践**

1. **先模型后视图**：确保模型字段正确定义后再开发视图
2. **使用默认配置**：优先使用`@defaults`减少配置量
3. **保持一致性**：相同模型的视图配置保持统一风格

---



# **第二部分：高级视图开发**



## **第八章：自定义交互与联动**

### **8.1 字段联动配置**

#### **8.1.1 值变化监听（`bind_on_changeHandler`）**

当字段值发生变化时执行自定义逻辑：

```json
{
  "name": "complexity",
  "bind_on_changeHandler": "(params) => {
    const {self: vm, value} = params;
    const form = vm.$ds.form;

    let desc = '必须包含大、小写字母、数字和特殊字符的三种组合';
    let min_length = 8;

    if(value === 'SIMPLE') {
      desc = '可以包含任意字符';
      min_length = 4;
    } else if(value === 'WEAK') {
      desc = '必须包含大、小写字母和数字的组合，不能包含特殊字符';
      min_length = 6;
    } else if(value === 'STRONG') {
      desc = '必须包含大、小写字母、数字和特殊字符的四种组合';
      min_length = 10;
    }

    form.min_length = min_length;
    form.description = desc;
  }"
}
```

**参数说明：**

- `params.self`：当前字段组件实例
- `params.value`：字段变化后的值
- `vm.$ds.form`：访问表单数据对象

#### **8.1.2 输入框失焦处理（`bind_on_handleInputBlur`）**

```json
{
  "name": "token_expire_min",
  "bind_on_handleInputBlur": "(data) => {
    const val = data.self.$ds.form.token_expire_min.replace(/[^\d\-]/g, '');
    let newValue = val;

    // 处理多个负号的情况
    if (val.indexOf('-') > 0) {
      newValue = val.replace(/-/g, '');
    }
    if ((val.match(/-/g) || []).length > 1) {
      newValue = '-' + val.substring(1).replace(/-/g, '');
    }

    // 确保为正数
    if (newValue < 0) {
      newValue = Math.abs(newValue).toString();
    }

    data.self.$ds.form.token_expire_min = newValue;
  }"
}
```

#### **8.1.3 字段联动（`linkage`）**

实现字段间的依赖关系：

```json
{
  "name": "ruleType",
  "linkage": {
    "params": {},  // 传递的参数
    "children": ["parentId"]  // 影响的子字段
  }
},
{
  "name": "parentId",
  "linkage": {
    "params": {
      "ruleType": "${ruleType}"  // 获取ruleType字段的值
    }
  }
}
```

### **8.2 条件渲染与显示控制**

#### **8.2.1 表达式条件渲染（`bind_display`）**

```json
{
  "name": "logoutAllClient",
  "bind_display": "${$ds.form.token_policy === 'MULTI_CLIENT'}"
}
```

**支持的表达式语法：**

- `$ds.form.fieldName`：访问表单字段值
- `$ds.mainTableRowData.fieldName`：访问表格行数据

#### **8.2.2 函数条件渲染**

```json
{
  "bind_display": {
    "transform": "(params) => {
      return window.tech?.userInfo?.userTenantInfo?.hasChildren ? true : false;
    }"
  }
}
```

#### **8.2.3 禁用条件（`bind_disabled`）**

```json
{
  "name": "type",
  "bind_disabled": "${$ds.mainTableRowData.source == 'SYSTEM_BUILT_IN'}"
}
```

#### **8.2.4 编辑状态控制（`disable_update`）**

```json
{
  "name": "mobile",
  "disable_update": "edit"  // 编辑状态下禁用
}
```

### **8.3 数据转换处理**

#### **8.3.1 表单初始化转换（`transformInitValue`）**

```json
{
  "transformInitValue": "(form) => {
    // 将数字类型转换为字符串
    if (typeof form.token_expire_min == 'number') {
      form.token_expire_min = form.token_expire_min.toString();
    }
    return form;
  }"
}
```

#### **8.3.2 表单数据转换（`transformRes`）**

```json
{
  "transformRes": "(form) => {
    // 将关联字段的显示值保存到新字段
    form.modelDisplayName = form.modelMetaId___values?.label;
    form.relQueryModelDisplayName = form.relQueryModelMetaId___values?.label;
    return form;
  }"
}
```

**注意**：关联字段的值通常以`___values`后缀对象存储完整信息，如：

- `modelMetaId`：存储ID值
- `modelMetaId___values`：存储完整的对象信息 `{value: 'id', label: '显示名'}`

#### **8.3.3 请求参数预处理（`reqPrep`）**

```json
{
  "name": "propertyMetaId",
  "reqPrep": "(vm, options) => {
    let commonForm = vm.$select('tenant_action_dimension__drawer_form_main_detail_top_common');
    if(commonForm.$ds.modelMetaId) {
      options.args.modelMetaId = commonForm.$ds.modelMetaId;
    }
    options.args.useDisplayForModel = true;
    return options;
  }"
}
```

#### **8.3.4 请求响应后处理（`reqAfter`）**

```json
{
  "reqAfter": "(vm, res) => {
    let common = vm.$select('tenant_action_dimension__drawer_form_main_detail_top_common');
    if(common.$ds.modelMetaId) {
      return res;
    }
    return [];
  }"
}
```

### **8.4 按钮条件控制**

#### **8.4.1 启用条件（`enableCondition`）**

```json
{
  "name": "安装",
  "enableCondition": "function condition(row) {
    return row && row[0] && row[0].state === 'installable';
  }"
}
```

#### **8.4.2 复杂条件判断**

```json
{
  "name": "编辑",
  "enableCondition": "(row, vm) => {
    const hasUniqueElements = (userRoles, authRoles) =>
      userRoles.some(item => !authRoles.includes(item.id));

    return row && row[0] && row[0].type != 'TENANT_SG' &&
      (tech.userInfo.roles.find(item => item.code == 'rbac_role_tenant_admin') != null ||
       row[0].noAuthRoles == null ||
       hasUniqueElements(tech.userInfo.roles, row[0].noAuthRoles));
  }"
}
```

## **第九章：高级选择器与弹窗**

### **9.1 树形选择器（`useSelectTree`）**

#### **9.1.1 基础树选择器**

```json
{
  "name": "parent_id",
  "type": "select",
  "useCustomClick": true,
  "view": {
    "customClick": {
      "customSelect": {
        "showType": "dialog",
        "width": "30%",
        "height": "60vh",
        "useSelectTree": {
          "preId": "organization_tree_main_wrap_right_form_items_0_items_parent_id_",
          "type": "single",  // 单选模式
          "confirm": "(vm, result) => {
            // 选择确认后的回调
            sessionStorage.setItem(vm.data.id + '__values', JSON.stringify([vm.$ds.form[vm.data.name]]));
          }"
        }
      }
    }
  }
}
```

#### **9.1.2 多选树选择器**

```json
{
  "useSelectTree": {
    "preId": "user_group_form_user_ids_",
    "type": "multiple",  // 多选模式
    "confirm": "(vm, result) => {
      // 处理多选结果
      const selectedValues = result.map(item => item.value);
      vm.$ds.form.user_ids = selectedValues;
    }"
  }
}
```

### **9.2 弹窗视图选择器（`useOpenView`）**

#### **9.2.1 基础弹窗选择器**

```json
{
  "view": {
    "customClick": {
      "customSelect": {
        "showType": "dialog",
        "width": "60%",
        "useOpenView": {
          "preId": "tenant_action_dimension_menu_pre",
          "model": "tenant_action_dimension",
          "type": "grid,search",  // 显示的视图类型
          "checkbox": "single",   // 选择模式：single/multiple
          "valueField": "metaId", // 返回值字段
          "labelField": "name",   // 显示标签字段
          "confirm": "(vm, result) => {
            // 选择确认后的处理逻辑
            let common = tech_app.page.getNode('tenant_action_dimension__drawer_form_main_detail_top_common');
            common.$ds.modelMetaId = result[0].metaId;
          }"
        }
      }
    }
  }
}
```

#### **9.2.2 带搜索的弹窗选择器**

```json
{
  "useOpenView": {
    "preId": "base_data_permission_form_modeName_menu_",
    "checkbox": "single",
    "model": "tenant_action_dimension",
    "app": "sie-iidp-iam",  // 指定应用
    "type": "grid,search",  // 同时显示表格和搜索
    "valueField": "metaId",
    "labelField": "name",
    "args": {  // 传递查询参数
      "filter": [
        ["type", "=", "DIMENSION"]
      ]
    }
  }
}
```

### **9.3 自定义弹窗配置**

#### **9.3.1 抽屉式弹窗（Drawer）**

```json
{
  "showType": "drawer",
  "width": "50%",
  "title": "已分配用户",
  "preId": "rbac_user_app_menu_table_tbar_allocated_",
  "model": "rbac_user_tenant",
  "type": "rbac_user_tenant_search,rbac_user_tenant_grid"
}
```

#### **9.3.2 对话框配置**

```json
{
  "showType": "dialog",
  "width": "30%",
  "height": "60vh",  // 设置高度
  "title": "选择组织",
  "hasFooter": true,  // 是否显示底部按钮
  "modal": true,      // 是否模态
  "closeOnClickModal": false  // 点击遮罩是否关闭
}
```



## **第十章：数据查询与服务调用**

### **10.1 自定义数据查询（`searchByMainTable`）**

#### **10.1.1 基础查询配置**

```json
{
  "searchByMainTable": {
    "service": {
      "search": "searchUser",  // 搜索服务
      "count": "count"         // 计数服务
    },
    "model": "rbac_user",
    "args": {
      "filter": [
        ["userType", "=", "0"]
      ],
      "order": "create_date desc",
      "limit": 20,
      "offset": 0
    }
  }
}
```

#### **10.1.2 复杂查询配置**

```json
{
  "searchByMainTable": {
    "service": {
      "search": "queryRoleTree",
      "count": "countRoleTree"
    },
    "model": "rbac_role",
    "args": {
      "useDisplayForModel": true,
      "filter": [
        ["roleType", "=", "0"],
        ["state", "=", "active"]
      ],
      "order": "id",
      "showRoot": true,  // 是否显示根节点
      "includeChildren": true  // 是否包含子节点
    }
  }
}
```

### **10.2 树形数据搜索配置（`searchConfig`）**

```json
{
  "searchConfig": {
    "model": "rbac_organization",
    "service": "searchForUser",
    "args": {
      "limit": 2147483647,  // 最大限制
      "filter": [
        ["type", "=", "department"],
        ["state", "=", "active"]
      ]
    }
  }
}
```

### **10.3 服务调用配置**

#### **10.3.1 按钮调用服务**

```json
{
  "name": "刷新应用",
  "model": "meta_app",
  "service": "restartApp",  // 服务名称
  "auth": "restartApp",
  "showPopLoading": true  // 显示加载提示
}
```

#### **10.3.2 带参数的服务调用**

```json
{
  "name": "删除",
  "model": "tenant_action_scope",
  "service": "delete",
  "actionAfter": "refreshTable",  // 执行后的动作
  "args": {
    "id": "$row.id"  // 使用行数据作为参数
  }
}
```

### **10.4 分页配置（`pagin`）**

```json
{
  "pagin": {
    "display": false,     // 是否显示分页
    "unPaging": true,     // 是否禁用分页
    "pageSize": 20,       // 每页数量
    "pageSizes": [10, 20, 50, 100],  // 可选页大小
    "layout": "total, sizes, prev, pager, next, jumper"  // 布局
  }
}
```

## **第十一章：权限控制与安全**

### **11.1 按钮权限控制**

#### **11.1.1 基础权限控制**

```json
{
  "name": "新增",
  "action": "create",
  "auth": "create"  // 权限标识，对应rbac_permission表中的auth字段
}
```

#### **11.1.2 复合权限检查**

```json
{
  "name": "数据权限",
  "auth": "data_auth",
  "bind_display": {
    "transform": "(params) => {
      // 检查用户角色和功能权限
      const user = window.tech?.userInfo;
      if (!user) return false;

      // 检查是否为管理员
      const isAdmin = user.roles?.some(role => role.code === 'rbac_role_admin');
      if (isAdmin) return true;

      // 检查具体权限
      return user.permissions?.includes('data_auth_manage');
    }"
  }
}
```

### **11.2 字段级别权限控制**

#### **11.2.1 基于角色的字段控制**

```json
{
  "name": "source",
  "bind_disabled": {
    "transform": "(params) => {
      const userRoles = window.tech?.userInfo?.roles || [];
      const isSystemAdmin = userRoles.some(role => role.code === 'SYSTEM_ADMIN');
      return !isSystemAdmin;  // 非系统管理员禁用
    }"
  }
}
```

#### **11.2.2 基于数据的字段控制**

```json
{
  "name": "ruleType",
  "bind_disabled": "$ds.clickType != 'add'",  // 仅新增时可编辑
  "bind_display": "${$ds.form.source !== 'SYSTEM_BUILT_IN'}"  // 非系统内置时显示
}
```

### **11.3 数据域过滤（`domains`）**

```json
{
  "body": {
    "type": "search",
    "columns": [...],
    "domains": [
      {
        "column": "source",  // 应用字段
        "domain": [          // 过滤条件
          ["source", "=", "base"]
        ]
      },
      {
        "column": "type",    // 多条件过滤
        "domain": [
          "|",
          ["type", "=", "function"],
          ["type", "=", "api"]
        ]
      }
    ]
  }
}
```

## **第十二章：视图扩展与继承**

### **12.1 视图扩展机制（`mode: extension`）**

#### **12.1.1 基础扩展配置**

```json
{
  "rbac_user_grid_ext": {
    "name": "用户表格扩展",
    "model": "rbac_user",
    "type": "grid",
    "mode": "extension",  // 扩展模式
    "inherit_ids": {
      "@ref": "base.rbac_user_grid"  // 继承的视图
    },
    "body": {
      "jsonpath": [...]  // JSON路径操作
    }
  }
}
```

#### **12.1.2 JSON路径操作（`jsonpath`）**

**位置参数（`position`）：**

- `inside`：在匹配节点内部追加（默认）
- `after`：在匹配节点之后添加
- `before`：在匹配节点之前添加
- `replace`：替换匹配的节点
- `attributes`：修改节点属性

**示例：添加字段**

```json
{
  "jsonpath": [
    {
      "expr": "columns.email",
      "position": "after",
      "body": ["mobile"]  // 在email字段后添加mobile字段
    }
  ]
}
```

**示例：添加Tabs**

```json
{
  "jsonpath": [
    {
      "has_not": "tabs",  // 如果原视图没有tabs则创建
      "expr": "tabs",
      "position": "inside",
      "body": [
        {
          "header": "用户日志",
          "body": {
            "type": "grid",
            "field": "logs",
            "columns": ["ip", "user_agent", "url"]
          }
        }
      ]
    }
  ]
}
```

**示例：修改字段属性**

```json
{
  "jsonpath": [
    {
      "expr": "columns.email",
      "position": "attributes",
      "body": {
        "hidden": true,  // 隐藏email字段
        "width": "150px"
      }
    }
  ]
}
```

### **12.2 组件动态控制**

#### **12.2.1 按钮动态显示函数（`showBtnFn`）**

```json
{
  "showBtnFn": "(item) => {
    if (!item?.parent_id) {
      return { create: true };  // 根节点只显示创建按钮
    } else {
      return { create: true, edit: true, delete: true };
    }
  }"
}
```

#### **12.2.2 自定义按钮过滤（`buttonsFilterMethod`）**

```json
{
  "buttonsFilterMethod": "(btn, value, data) => {
    if (!value.parent_id && (btn?.action === 'edit' || btn?.action === 'delete')) {
      return false;  // 根节点不显示编辑和删除按钮
    }
    return true;
  }"
}
```

### **12.3 模板继承与复用**

#### **12.3.1 创建基础模板视图**

```json
{
  "base_form_template": {
    "name": "基础表单模板",
    "body": {
      "type": "form",
      "columns": [
        {
          "name": "name",
          "displayName": "名称",
          "required": true
        },
        {
          "name": "code",
          "displayName": "编码",
          "required": true
        },
        {
          "name": "description",
          "displayName": "描述"
        }
      ],
      "tbar": ["@defaults"]
    }
  }
}
```

#### **12.3.2 继承并扩展模板**

```json
{
  "specific_model_form": {
    "name": "特定模型表单",
    "model": "specific_model",
    "type": "form",
    "mode": "extension",
    "inherit_ids": {
      "@ref": "base.base_form_template"
    },
    "body": {
      "jsonpath": [
        {
          "expr": "columns",
          "position": "inside",
          "body": [
            {
              "name": "specific_field",
              "displayName": "特定字段"
            }
          ]
        }
      ]
    }
  }
}
```


## **第十四章：最佳实践总结**

### **14.1 代码组织规范**

#### **14.1.1 文件结构**

```
views/
├── base/                    # 基础视图
│   ├── rbac_user_view.json
│   ├── rbac_role_view.json
│   └── rbac_org_view.json
├── tenant/                  # 租户相关视图
│   ├── tenant_user_view.json
│   └── tenant_role_view.json
├── extension/               # 扩展视图
│   ├── rbac_user_ext_view.json
│   └── rbac_role_ext_view.json
└── menu_view.json          # 菜单配置
```

#### **14.1.2 命名规范**

- **视图名称**：`{model_name}_{view_type}`，如`rbac_user_grid`
- **扩展视图**：`{model_name}_{view_type}_ext`，如`rbac_user_grid_ext`
- **菜单名称**：`{module}_{function}_menu`，如`rbac_user_management_menu`
- **自定义字段**：使用`custom_`前缀，如`custom_display_field`

### **14.2 配置最佳实践**

#### **14.2.1 保持配置简洁**

```json
{
  // 好：使用默认配置
  "tbar": ["@defaults"],
  "buttons": ["@defaults"],

  // 好：必要的最小配置
  "tbar": [
    { "name": "新增", "action": "create", "auth": "create" },
    { "name": "删除", "action": "delete", "auth": "delete" }
  ],

  // 避免：过度配置
  "tbar": [
    { "name": "新增", "action": "create", "auth": "create", "icon": "el-icon-plus", "type": "primary", "size": "small", "plain": false, "round": false, "circle": false }
  ]
}
```

#### **14.2.2 使用函数封装复杂逻辑**

```javascript
// 在单独的文件中定义工具函数
const ViewUtils = {
  // 权限检查函数
  checkPermission: (permissionCode) => {
    const user = window.tech?.userInfo;
    return user?.permissions?.includes(permissionCode) ||
           user?.roles?.some(role => role.code === 'admin');
  },

  // 数据格式化函数
  formatDate: (timestamp) => {
    return new Date(timestamp).toLocaleString();
  }
};

// 在视图中引用
{
  "bind_display": {
    "transform": "(params) => ViewUtils.checkPermission('data_manage')"
  }
}
```

### **14.3 性能优化建议**

1. **减少不必要的字段**：只显示业务需要的字段
2. **合理使用缓存**：对不常变的数据启用缓存
3. **分页加载**：大数据集使用分页，避免一次性加载
4. **懒加载**：树形数据使用懒加载
5. **虚拟滚动**：长列表使用虚拟滚动

### **14.4 常见问题排查**

#### **14.4.1 视图不显示**

1. 检查`app.json`中视图路径配置是否正确
2. 检查JSON语法是否正确（可以使用JSON验证工具）
3. 检查模型名称是否与后端一致
4. 检查浏览器控制台是否有错误信息

#### **14.4.2 按钮不生效**

1. 检查`auth`权限标识是否正确
2. 检查`service`服务名称是否存在
3. 检查`action`动作类型是否支持
4. 检查`enableCondition`条件是否满足

#### **14.4.3 数据不加载**

1. 检查`searchByMainTable`配置是否正确
2. 检查网络请求是否有响应
3. 检查后端服务是否正常运行
4. 检查数据格式是否符合预期

---

这份完整的手册涵盖了赛意谷神平台视图开发的基础和高级特性。建议在实际开发中：

1. 从**基础视图**开始，掌握基本结构
2. 逐步学习**高级特性**，按需使用
3. 遵循**最佳实践**，保持代码质量
4. 善用**调试工具**，快速定位问题