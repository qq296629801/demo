---
title: 基本视图
date: 2023-09-25 17:31:35
permalink: /pages/405eb0/
---
# 基本视图

我们能够为给定模型生成默认视图。在实践中，业务应用程序永远不会接受默认视图。相反，我们至少应该以一种合乎逻辑的方式组织各个领域。

视图在带有操作和菜单的 JSON 文件中定义。它们是 ui_view模型的实例。

视图配置:  app.json 中view节点配置视图的路径

视图路径: 在resolved包下新建views目录,所有视图文件必须放在views目录

data种子数据路径: 在resolved包下新建data目录,所有数据文件必须放在data目录

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_223223_-0TDYaRO8rUAYRLX_1672361879.png)

```json
{
    "name": "base",
    "displayName": "base",
    "company": "sie",
    "category": "base",
    "description": "基础模块",
    "summary": "基础模块",
    "type": "SDK",
    "tag": "1.0",
    "resolved": "com.sie.iidp.base",
    "dependencies": [],
    "license": "LGPL 3.0",
    "view": [
        "views/ui_view_view.json",
        "views/menu_view.json"
    ],
    "data": [
        "data/rbac_user.json"
    ]
}
```

## 3.8.1.1. 表单(form)

### 表单视图（form）

| 参数    | 类型     | 必填  | 描述  | 可选值     |
| ----- | ------ | --- | --- | ------- |
| name  | String | 是   | 名称  |         |
| model | String | 是   | 模型  | 模型名     |
| type  | String | 否   | 类型  | form    |
| mode  | String | 是   | 模式  | primary |
| body  | Object |     |     |         |
| showEditBtn    | Boolean  | 否   | 详情页面是否显示编辑按钮 |  |

主要信息

| 参数      | 类型     | 必填  | 描述  | 可选值        |
| ------- | ------ | --- | --- | ---------- |
| type    | String | 是   | 类型  |            |
| columns | String | 是   | 属性  |            |
| tabs    | Array  | 否   | 切换  | 参考4.7.1.11 |
| tbar    | Array  | 否   | 工具栏 | 参考4.7.1.5  |
| showEditBtn    | Boolean  | 否   | 详情页面是否显示编辑按钮 |  |
示例代码：

注意:如果需要显示ER关联属性,必须在columns添加相关字段

```json
"rbac_user_form": {
          "name": "用户表单",
          "model": "rbac_user",
          "mode": "primary",
          "body": {
            "type": "form",
            "columns": [
              "name",
              "login",
              "mobile",
              "email",
              "role_ids" //如果需要显示ER关联属性,必须在columns添加相关字段
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
                  "type": "grid,form",
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
```

### 分组表单视图（group）

| 属性        | 类型     | 描述       | 可选值                             |
| --------- | ------ | -------- | ------------------------------- |
| name      | String | 显示名称     |                                 |
| groupConf | Object | 分组对象     | `{ "name":"基本信息","id":"group1" }` |
| span      | 占行宽度   | 占行宽度(24) | 6                               |
| row       | 所在行    |          | 1                               |
| rowSpan   | 跨行展示   |          | 2                               |

示例代码

```json
{
      "name": "code", // 表单字段
      "groupConf": {  // 有该字段表明是表单分组
        "name": "基本信息", // 分组名称
        "id": "group1"  // 分组唯一id
      },
      "span": 6,  // 该字段所占行宽度（总宽度为24）
      "row": 1,  // 该字段所在行，值不同即不同行
      "rowSpan": 2  // 跨行展示，2表示该字段占两行
}
```

页面展示:

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_219923_gOVgIiTlrm8cJdMK_1672361879.png)

## 3.8.1.2.     表格(grid)

表格视图（grid）注意事项（不填type则读取body中的type）

| 参数    | 类型     | 必填  | 描述  | 可选值                     |
| ----- | ------ | --- | --- | ----------------------- |
| name  | String | 是   | 名称  |                         |
| model | String | 是   | 模型  | 模型名                     |
| type  | String | 否   | 类型  | grid                    |
| mode  | String | 是   | 模式  | primary 主要 Extension 扩展 |
| body  | Object |     |     |                         |

主要信息

| 参数      | 类型     | 必填  | 描述  | 可选值 |
| ------- | ------ | --- | --- | --- |
| type    | String | 是   | 类型  |     |
| columns | Array  | 是   | 属性  |     |
| buttons | Array  | 是   | 按钮  |     |
| tbar    | Array  | 是   | 工具栏 |     |

示例代码：

```json
"rbac_user_grid": {
          "name": "用户表格",
          "model": "rbac_user",
          "mode": "primary",
          "body": {
            "type": "grid",
            "columns": [
              "name",
              "login",
              "mobile",
              "email"
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
                ]
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
```

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_227738_sOGBJnBhvWLQUTys_1672361879.png)

## 3.8.1.3.     搜索(search)

搜索（search）

| 参数    | 类型     | 必填  | 描述  | 可选值 |
| ----- | ------ | --- | --- | --- |
| name  | String | 是   | 名称  |     |
| model | String | 是   | 模型  |     |
| type  | String | 否   | 类型  |     |
| mode  | String | 是   | 模式  |     |
| body  | Object |     |     |     |

body 内容

| 参数      | 类型     | 必填  | 描述  | 可选值 |
| ------- | ------ | --- | --- | --- |
| type    | String | 是   | 名称  |     |
| columns | Array  | 是   | 属性  |     |

示例代码:

```json
"meta_app_search": {
          "name": "应用查询",
          "model": "meta_app",
          "mode": "primary",
          "body": {
            "type": "search",
            "columns": [
              "name",
              "category_ids",
              "state",
              "type"
            ]
          }
        }
```

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_223757_LnAxuQoh9Jr7nedX_1672361879.png)

##### 3.8.1.3.1 日期类型

如果字段是日期、时间类型，可以指定 widget, 前端使用不同的日期组件进行渲染。

目前支持的 widget

1. year: 选择年份
2. month: 选择月份
3. date: 选择日期
4. datetime: 选择时间
5. monthrange: 月份范围
6. daterange: 日期范围
7. datetimerange: 时间范围

```json
{
  "views": {
    "datedemo_grid": {
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
            "name": "编辑"
          }
        ],
        "columns": [
          {
            "label": "年",
            "name": "year"
          },
          {
            "label": "月",
            "name": "month"
          },
          {
            "label": "日",
            "name": "date"
          },
          {
            "label": "时间",
            "name": "datetime"
          },
          {
            "label": "月范围",
            "name": "monthRange"
          },
          {
            "label": "日范围",
            "name": "dateRange"
          },
          {
            "label": "时间范围",
            "name": "datetimeRange"
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
          }
        ],
        "type": "grid"
      },
      "mode": "primary",
      "model": "DateDemo",
      "name": "日期Demo-表格",
      "type": "grid"
    },
    "datedemo_form": {
      "body": {
        "columns": [
          {
            "label": "年",
            "name": "year",
            "widget": "year"
          },
          {
            "label": "月",
            "name": "month",
            "widget": "month"
          },
          {
            "label": "日",
            "name": "date",
            "widget": "date"
          },
          {
            "label": "时间",
            "name": "datetime",
            "widget": "datetime"
          },
          {
            "label": "月范围",
            "name": "monthRange",
            "widget": "month"
          },
          {
            "label": "日范围",
            "name": "dateRange",
            "widget": "date"
          },
          {
            "label": "时间范围",
            "name": "datetimeRange",
            "widget": "datetime"
          }
        ],
        "tabs": [],
        "type": "form"
      },
      "mode": "primary",
      "model": "DateDemo",
      "name": "日期Demo-表单",
      "type": "form"
    },
    "datedemo_search": {
      "body": {
        "columns": [
          {
            "label": "年",
            "name": "year",
            "widget": "year"
          },
          {
            "label": "月",
            "name": "month",
            "widget": "month"
          },
          {
            "label": "月范围",
            "name": "monthRange",
            "widget": "monthrange"
          },
          {
            "label": "日",
            "name": "date",
            "widget": "date"
          },
          {
            "label": "日范围",
            "name": "dateRange",
            "widget": "daterange"
          },
          {
            "label": "时间",
            "name": "datetime",
            "widget": "datetime"
          },
          {
            "label": "时间范围",
            "name": "datetimeRange",
            "widget": "datetimerange"
          }
        ],
        "type": "search"
      },
      "mode": "primary",
      "model": "DateDemo",
      "name": "日期Demo-搜索",
      "type": "search"
    }
  }
}
```

## 3.8.1.4.     卡片(card)

卡片（card） （type 不填则读取body中的type）

| 参数    | 类型     | 必填  | 描述  | 可选值 |
| ----- | ------ | --- | --- | --- |
| name  | String | 是   | 名称  |     |
| model | String | 是   | 模型  |     |
| type  | String | 否   | 类型  |     |
| mode  | String | 是   | 模式  |     |
| body  | Object |     |     |     |

主要内容

| 参数      | 类型     | 必填  | 描述  | 可选值 |
| ------- | ------ | --- | --- | --- |
| type    | String | 是   | 名称  |     |
| columns | Array  | 是   | 属性  |     |

示例代码:

```json
"rbac_user_card": {
          "name": "用户卡片",
          "model": "rbac_user",
          "body": {
            "type": "card",
            "columns": [
              "name",
              "email",
              "login",
              "mobile"
            ]
          }
        }
```

## 3.8.1.5.     菜单(menu)

菜单（menu）

| 参数           | 类型     | 必填  | 描述  | 可选值                     |
| ------------ | ------ | --- | --- | ----------------------- |
| name         | String | 是   | 唯一  |                         |
| display_name | String | 是   | 名称  |                         |
| model        | String | 是   | 模型  |                         |
| view         | String | 是   | 视图  | grid ,form, search ,自定义 |
| sequence     | Long   | 否   | 排序  |                         |
| parent_ids   | Object |     |     | @ref                    |

1. 场景1

树形菜单如何配置，如下所示

```json
"base_developer_center": {
      "sequence": "31",
      "name": "base_developer_center",
      "display_name": "开发者中心"
    },
    "meta_model_menu": {
      "sequence": "5",
      "view": "grid,form,search",
      "name": "meta_model_menu",
      "model": "meta_model",
      "display_name": "模型",
      "parent_ids": {
        "@ref": "base_developer_center"
      },
      "meta_sub_model_menu": {
      "sequence": "5",
      "view": "grid,form,search",
      "name": "meta_sub_model_menu",
      "model": "meta_sub_model",
      "display_name": "子模型",
      "parent_ids": {
        "@ref": "meta_model_menu"
      }
}
```

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_232556_Sv-wgL1unBs2A1ox_1672361879.png)

## 3.8.1.6.     工具栏(tbar)

工具栏（tbar）

| 参数     | 类型     | 必填  | 描述  | 可选值                                                                                               |
| ------ | ------ | --- | --- | ------------------------------------------------------------------------------------------------- |
| name   | String | 是   | 名称  |                                                                                                   |
| action | String | 是   | 动作  | add添加delete删除save保存update修改createEr  er创建addEr  er增加按钮deleteEr  er删除按钮updateEr er更新按钮@defaults 默认 |
| auth   | String | 是   | 权限  |                                                                                                   |

示例代码：

1. @defaults

   "tbar": ["@defaults"]

a. type等于grid时,"@defaults等于默认添加 新增,删除按钮

b. type等于form时,"@defaults等于默认添加  保存按钮

c. type等于from并且存在er关系时,@defaults等于默认 添加 新增,删除按钮

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_219294_P2fHoxgXOo4quuKx_1672361879.png)

自定义tbar

```json
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
          }, {
            "name": "刷新应用",//自定义tbar
            "model": "meta_app",
            "service": "restartApp",//调用的服务名,默认参数是ids
            "auth": "restartApp"
          }
        ]
```

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_219726_zJHx8sApCAuVxgBy_1672361879.png)

## 3.8.1.7.     按钮 (buttons)

按钮（buttons）

| 参数      | 类型     | 必填  | 描述  | 可选值    |
| ------- | ------ | --- | --- | ------ |
| name    | String | 是   | 名称  |        |
| action  | String | 是   | 动作  |        |
| auth    | String | 是   | 权限  |        |
| service | String | 否   | 服务  |        |
| model   | String | 否   | 模型  |        |
| views   | String | 否   | 视图  | custom |
| params  | Array  | 否   | 参数  |        |

示例代码：

​            1.     @defaults 默认添加详情,编辑按钮

"buttons": ["@defaults"]

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_225420_hdEH5b25afNs6Y5G_1672361879.png)

```json
 "buttons": [
              {
                "name": "编辑",
                "action": "edit",
                "auth": "update"
              },
              {
                "name": "安装",//自定义按钮
                "model": "meta_app",//指定服务
                "service": "installApp",//服务名
                "auth": "installApp",//权限字符串
                "enableCondition": "function condition(row) {return row && row[0] && row[0].state === 'installable';}"//按钮禁用条件
              },
              {
                "name": "更新数据",
                "model": "meta_app",
                "service": "refreshApp",
                "auth": "refreshApp"
              }
            ]
```

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_227154_8xHFh7HYzQzTMMel_1672361879.png)

## 3.8.1.8. 切换(tabs)

切换（tabs）

| 参数      | 类型     | 必填  | 描述           | 可选值 |
| ------- | ------ | --- | ------------ | --- |
| header  | String | 是   |              |     |
| rowspan | String | 否   |              |     |
| tbar    | Array  | 否   | 参考4.7.1.5工具栏 |     |
| body    | Object | 否   | 一对多 多对多 内容   |     |

主要内容

| 参数      | 类型     | 必填  | 描述   | 可选值       |
| ------- | ------ | --- | ---- | --------- |
| type    | String | 是   |      | grid,form |
| field   | String | 否   | 关系字段 |           |
| columns | Array  | 否   |      |           |

示例代码：

```json
"tabs": [
          {
            "header": "应用",
            "rowspan": 3,
            "tbar": [
              {
                "name": "添加",
                "action": "addEr"
              },
              {
                "name": "删除",
                "action": "deleteEr"
              }
            ],
            "body": {
              "type": "grid",
              "field": "app_ids",
              "columns": [
                "name",
                "tag",
                "description",
                "state"
              ]
            }
          }
        ]
```

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_224006_OL-IdKUa8rwLlx42_1672361879.png)

## 3.8.1.9. 关系(er)

​            1.     一对多:  示例:应用分类-应用列表(OneToMany)

​            a.     form里面columns需要指定OneToMany的属性:app_ids

​            b.     tab里面的field需要指定OneToMany的属性:app_ids

![img](./images/MTY4ODg1NzU0NjMyMjY3Mw_220574_KlGvUq-T5NaocRZA_1672361879.png)

```json
"meta_app_category_form": {
          "name": "应用分类表单",
          "model": "meta_app_category",
          "mode": "primary",
          "body": {
            "type": "form",
            "columns": [
              "name",
              "app_ids" //需要指定关联的属性:app_ids
            ],
            "tabs": [
              {
                "header": "应用",
                "rowspan": 3,
                "tbar": [
                  {
                    "name": "添加",
                    "action": "addEr"
                  },
                  {
                    "name": "删除",
                    "action": "deleteEr"
                  }
                ],
                "body": {
                  "type": "grid",
                  "field": "app_ids", //指定关联的属性:app_ids
                  "columns": [//关联表的属性
                    "name",
                    "tag",
                    "description",
                    "state"
                  ]
                }
              }
            ]
          }
        }
```

​            2.     多对多  模型-属性(ManyToMany)

```json
  "meta_model_form": {
      "name": "模型表单",
      "model": "meta_model",
      "mode": "primary",
      "body": {
        "type": "form",
        "columns": [
          "name",
          "display_name",
          "table_name",
          "description",
          "parents",
          "tag",
          "source",
          "app_ids",
          "is_abstract",
          "auto_log",
          "order_by",
          "display_format",
          "property_ids" //form必须包含关联的属性
        ],
        "tabs": [
          {
            "header": "属性",
            "rowspan": 3,
            "body": {
              "type": "grid",
              "field": "property_ids",//关联的属性必填
              "columns": [
                "name",
                "display_name",
                "data_type",
                "required",
                "read_only",
                "store",
                "db_index"
              ],
              "edit": { //form编辑
                "type": "form",
                "columns": [
                  "name",
                  "data_type",
                  "display_name",
                  "description",
                  "display",
                  "source",
                  "default_value",
                  "length",
                  "display_for_model",
                  "required",
                  "read_only",
                  "unique",
                  "store",
                  "db_index",
                  "property_type",
                  "compute_script"
                ],
                "buttons": [
                  {
                    "name": "编辑",
                    "action": "edit",
                    "auth": "update"
                  }
                ]
              }
            },
            "tbar": [
              {
                "name": "创建",
                "action": "createEr",  //ER关系tbar,button需要带后缀er
                "auth": "create"
              },
              {
                "name": "删除",
                "action": "deleteEr",
                "auth": "delete"
              }
            ]
          }
        ]
      }
    }
```

​            3.     多对一

属性name设置isDisplayForModel

```java
@Model(name = "meta_app_category", displayName = "应用分类")
public class MetaAppCategory extends Model {
    public static Property name = Property.String().displayName("名称").tooltips("分类名称").isRequired().isDisplayForModel();
}
```

在应用列表grid添加category字段,在应用列表会自动显示分类的name.

```json
"meta_app_grid": {
          "name": "应用列表",
          "model": "meta_app",
          "mode": "primary",
          "body": {
            "type": "grid",
            "columns": [
              "name",
              "display_name",
              "state",
              "tag",
              "product",
              "category_ids",
              "type",
              "summary"
            ],
            "buttons": [
              {
                "name": "编辑",
                "action": "edit",
                "auth": "update"
              }
            ],
            "tbar": [
              {
                "name": "新增",
                "action": "create",
                "auth": "create"
              }
            ]
          }
        }
```

## 3.8.1.10.  视图扩展(extend)

在视图中，表单，列表和搜索视图是使用json结构定义的。 如果要通过继承扩展原有视图，我们需要用一种方法来修改这个json。 这需要通过两步来实现，1、定位到视图json中某个界面元素的位置；2、然后在这个位置插入增补定义的视图。通过这两步就可以达到扩展视图的目的。

| 参数          | 类型     | 必填  | 描述                                                                                   | 可选值                                                                                                                             |
| ----------- | ------ | --- | ------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------- |
| name        | String | 是   | 名称                                                                                   |                                                                                                                                 |
| model       | String | 是   | 模型                                                                                   |                                                                                                                                 |
|             | String |     | 模型                                                                                   |                                                                                                                                 |
| type        | String |     | 视图类型                                                                                 | grid表格 form表单search搜索                                                                                                           |
| mode        | String | 是   | 视图模式:扩展视图                                                                            | extension                                                                                                                       |
| inherit_ids | Object | 是   | 继承的视图id:格式:  "@ref": "应用名.继承视图key".示例: "inherit_ids": {"@ref": "mom.mom_item_grid" } |                                                                                                                                 |
| body        | Object | 是   | {"jsonpath":[{"expr":"columns","position":"inside","body":["barcode","type"]}]}      |                                                                                                                                 |
| jsonpath    | Array  | 是   | 指定要替换的路径和内容                                                                          |                                                                                                                                 |
| expr        | String | 是   | columnscolumns.name                                                                  |                                                                                                                                 |
| position    | String | 否   | 定位节点位置                                                                               | inside（默认值）:匹配节点内的追加内容。after:将内容添加到父元素之中，匹配的节点之后。before:添加内容在匹配节点之前。replace:替换匹配的节点。如果使用空内容，它将删除该匹配的元素。attributes：修改匹配元素的XML属性。 |
| has_not     | String | 否   | 如果节点不存在则创建,支持buttons,tabs,tars                                                       |                                                                                                                                 |
| body        | Object | 否   |                                                                                      |                                                                                                                                 |

示例代码：

```json
{
      "views": {
        "rbac_user_grid_ext": {
          "name": "用户表格扩展",
          "model": "rbac_user",
          "type": "grid",
          "mode": "extension",
          "inherit_ids": {
            "@ref": "base.rbac_user_grid"
          },
          "body": {
            "jsonpath": [
              {
                "expr": "columns.email",
                "position": "after",
                "body": [
                  "mobile"
                ]
              }
            ]
          }
        },
        "rbac_user_form_ext": {
          "name": "用户表单扩展",
          "model": "rbac_user",
          "type": "form",
          "mode": "extension",
          "inherit_ids": {
            "@ref": "base.rbac_user_form"
          },
          "body": {
            "jsonpath": [
              {
                "expr": "columns.email",
                "position": "before",
                "body": [
                  "create_user"
                ]
              },
              {
                "has_not": "tabs",
                "expr": "tabs",
                "position": "inside",
                "body": [
                  {
                    "header": "用户日志",
                    "tbar": [
                      {
                        "name": "添加",
                        "action": "addEr",
                        "icon": "add",
                        "auth": "create"
                      },
                      {
                        "name": "删除",
                        "action": "deleteEr",
                        "auth": "delete"
                      }
                    ],
                    "body": {
                      "type": "grid",
                      "field": "logs",
                      "columns": [
                        "ip",
                        "user_agent",
                        "url"
                      ]
                    }
                  }
                ]
              }
            ]
          }
        },
        "rbac_user_search_ext": {
          "name": "用户搜索扩展",
          "model": "rbac_user",
          "mode": "extension",
          "type": "search",
          "inherit_ids": {
            "@ref": "base.rbac_user_search"
          },
          "body": {
            "jsonpath": [
              {
                "expr": "columns.email",
                "position": "inside",
                "body": [
                  "create_user"
                ]
              }
            ]
          }
        }
      }
    }
```

## 3.8.1.11.  种子数据 (data)

种子数据（data）

data种子数据路径:在resolved包下新建data目录,所有数据文件必须放在data目录

| 参数         | 类型     | 必填  | 描述    | 可选值                         |
| ---------- | ------ | --- | ----- | --------------------------- |
| model      | String | 是   | 模型    |                             |
| properties | Object |     |       |                             |
|            | String |     | @eval | [4,@ref(rbac_role_admin),0] |

属性（properties）

示例代码：

```json
{
      "data": {
        "rbac_role_admin": {
          "model": "rbac_role",
          "properties": {
            "name": "管理员",
            "is_admin": 1
          }
        },
        "rbac_role_common": {
          "model": "rbac_role",
          "properties": {
            "name": "普通用户",
            "is_admin": 0
          }
        },


        "rbac_user_admin": {
          "model": "rbac_user",
          "properties": {
            "name": "管理员",
            "login": "admin",
            "mobile": "18888888888",
            "password": "admin",
            "email": "1388888888@sie.com",
            "role_ids": [
              {
                "@eval": "[4, @ref(rbac_role_admin), 0]" //注意:此处使用了Many2Many指令集,在用户角色表中插入一条关联记录, @ref(rbac_role_admin)的意思是先获取rbac_role_admin生成的id,然后再插入到用户角色关联表里面
              }
            ]
          }
        },
        "rbac_user_lijun": {
          "model": "rbac_user",
          "properties": {
            "name": "李某",
            "login": "lijun",
            "mobile": "13600314629",
            "password": "lijun",
            "email": "lijun10@chinasie.com",
            "role_ids": [
              {
                "@eval": "[4, @ref(rbac_role_admin), 0]"
              }
            ]
          }
        }
      }
    }
```

## 3.8.1.12.  树 (tree)

树 （tree）

| 参数    | 类型     | 必填  | 描述   | 可选值  |
| ----- | ------ | --- | ---- | ---- |
| name  | String | 是   | 名称   |      |
| model | String | 是   | 模型   |      |
| type  | String | 是   | 类型   | tree |
| mode  | String | 是   | 视图模式 |      |
| body  | Object | 是   | 内容   |      |

body内容

| 参数      | 类型     | 必填  | 描述  | 可选值  |
| ------- | ------ | --- | --- | ---- |
| type    | String | 是   | 类属  | tree |
| model   | String | 是   | 模型  |      |
| props   | Object | 否   | 绑定  |      |
| tbar    | Array  | 否   | 工具栏 |      |
| buttons | Object | 否   | 按钮  |      |
| columns | Array  | 否   | 字段  |      |

columns对象

```json
"columns": [
    "name",
    "parent_id",
    "children_ids",
    "description"
]
```

props对象

```json
"props": {
    "children": "children",
    "parent": "parent",
    "label": "name",
    "showTree": false // 搜索结果是否为树状结构，默认为false
}
```

tbar对象

```json
"tbar": [{
    "name": "单击",
    "auth": "click",
    "service": "click",
    "action": "openView",
    "model": "rbac_organization",
    "views": "custom",
    "params": []
}]
```

buttons对象

```json
"buttons": [{
    "name": "新增",
    "auth": "create",
    "service": "create",
    "action": "openView",
    "model": "rbac_organization",
    "views": "custom",
    "params": []
},{
    "name": "删除",
    "auth": "delete",
    "service": "delete",
    "action": "openView",
    "model": "rbac_organization",
    "views": "custom",
    "params": []
}]
```

## 3.8.1.13.导入导出(buttons)

------

Import导入

| 字段        | 名称       | 可选值                      | 可空  |
|:--------- | -------- | ------------------------ | --- |
| action    | 事件       | import                   | 否   |
| model     | 模型名      | base_test                | 否   |
| service   | 服务名      | excelImport              | 否   |
| fileLimit | 文件限制     |                          |     |
| -ext      | 文件类型     | .xls ，.xlsx   默认值.xls 可空 | 是   |
| -maxSize  | 文件大小单位MB | 默认1MB                    | 是   |

前端视grid视图tbar配置

```json
{
    "action": "import",
    "model": "base_test",
    "service": "excelImport",
    "fileLimit": {
        "ext": ".xls,.xlsx",
        "maxSize": "2048"
    }
}
```

| 字段         | 名称  | 可选值            | 可空  |
| ---------- | --- | -------------- | --- |
| action     | 导出  | export         | 否   |
| source     | 来源  | select         | 否   |
| properties | 属性  | ["name","age"] | 否   |
| model      | 模型  | base_test      | 否   |
| service    | 服务  | excelExport    | 否   |

前端grid视图tbar工具栏配置

```json
{
    "action": "export",
    "source": "selected",
    "properties": ["name","sex"],
    "model": "base_test",
    "service": "excelExport"
}
```

## 3.8.1.14.树(tree)

1. 左边树为自己模型 (例子来源)

   ```json
   "mi_base_folder_tree": {
       "name": "文件目录-树",
       "model": "mi_base_folder",
       "type": "tree",
       "mode": "primary",
       "body": {
           "type": "tree",
           "model": "mi_base_folder",
           "columns": [
               "name",
               "parent",
               "children"
           ],
           "props": {
               "children": "children",
               "parent": "parent",
               "label": "name"
           },
           "search": {}
       }
   }
   ```

   外面的Model与body里面的Model保持一致，左边树为自己模型。例如：组织功能，菜单功能

2. 左边树为其他模型

   ```json
     "mi_base_file_tree": {
         "name": "文件-树",
         "model": "mi_base_file",
         "type": "tree",
         "mode": "primary",
         "body": {
           "type": "tree",
           "model": "mi_base_folder",
           "columns": [
             "name",
             "parent",
             "children"
           ],
           "props": {
             "children": "children",
             "parent": "parent",
             "label": "name",
             "subApp": "smi-base-asset",
             "subModel": "mi_base_file",
             "subViewType": "grid,search,form",
             "subViewFilter": "folder"
           },
           "search": {}
         }
       }
   ```

   body里面的的模型可以配置成其他模型model :文件夹，columns也是模型的属性。

   props操作内容 children 为子字段，parent为父字段，label为展示名。

   subApp为应用名，subModel为模型名，subViewType为视图类型，subViewFilter为过滤条件。

   单击左边选中项请求sub的内容。

## 3.8.1.15.字段(columns)

对象结构的方式（详情参照文档）

| 字段     | 描述           | 可选值        | 类型      |
| ------ | ------------ | ---------- | ------- |
| name   | 属性名          |            | Strin   |
| hide   | 显示隐藏表单(form) | true/false | Boolean |
| hidden | 显示隐藏表格(grid) | true/false | Boolean |
| custom | 覆盖前端视图相同属性的值 | true/false | Boolean |

代码示例：

```json
 "columns": [
    {
        "label": "昵称",
        "name": "nickname",
        "sortable": true,
        "width": "80px"
    }
 ]
```

