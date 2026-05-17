---
title: API接口参数说明
date: 2023-09-25 17:31:35
permalink: /pages/b9a31b/
---
### 3.3.2 API接口请求参数和响应结果

------

#### 1. API请求参数

```json
{
    "id": "guid",
    "jsonrpc": "2.0",
    "method": "service",
    "params": {
        "args": {
            "ids": [
                "02254bc01kz5s"
            ],
            "values": {
                "description": "设备",
                "name": "iot_device",
                "property_ids": [
                    [
                        0,
                        0,
                        {
                            "name": "ip",
                            "data_type": "String",
                            "display_name": "ip"
                        }
                    ]
                ]
            },
            "valuesList": [
                {
                    "tag": "master",
                    "app_ids": "021r397wz5fcw",
                    "property_ids": [
                        [
                            0,
                            0,
                            {
                                "name": "status",
                                "data_type": "Integer"
                            }
                        ]
                    ]
                }
            ],
            "filter": [
                "|",
                [
                    "name",
                    "like",
                    "%admin"
                ],
                [
                    "email",
                    "like",
                    "%126"
                ]
            ],
            "order": "",
            "limit": 31,
            "offset": 0,
            "properties": [
                "name",
                "login",
                "mobile"
            ]
        },
        "context": {
            "uid": "",
            "lang": "zh_CN"
        },
        "useDisplayForModel": true,
        "model": "rbac_user",
        "tag": "master",
        "service": "search",
        "app": "base"
    }
}
```



####              请求参数描述

| 参数               | 类型    | 必填 | 描述                                                         | 可选值  |
| ------------------ | ------- | ---- | ------------------------------------------------------------ | ------- |
| id                 | String  | 否   | 请求Id,UUID格式                                              |         |
| jsonrpc            | String  | 是   | jsonrpc版本,默认值:2.0                                       | 2.0     |
| method             | String  | 是   | 服务类型,默认值:service                                      | service |
| model              | String  | 是   | 模型名称                                                     |         |
| service            | String  | 是   | 服务名称或者接口命名                                         |         |
| tag                | String  | 是   | 版本号,默认值:master                                         | master  |
| app                | String  | 否   | 应用名称                                                     |         |
| context.uid        | String  | 否   | 用户id                                                       |         |
| context.lang       | String  | 否   | 语言,默认值:zh_CN                                            | zh_CN   |
| properties         | Array   | 否   | 获取字段列表                                                 |         |
| limit              | int     | 否   | 分页limit,默认值:31                                          |         |
| offset             | int     | 否   | 分页offset,默认值:0                                          |         |
| order              | String  | 否   | 分页排序字段,示例:"id asc,create_date desc"                  |         |
| filter             | String  | 否   | 查询过滤条件,示例:<br/>`["|",["name","like","%admin"],["email","like","%126"]]` |         |
| useDisplayForModel | Boolean | 否   | 需要显示关联ManyToOne对象name时,设置为true                   | true    |
| ids                | Array   | 否   | 主键id集合,示例: `["020uzt2d3sr9c"]`                         |         |
| values             | Object  | 否   | service=update时,需要更新的对象字段                          |         |
| valuesList         | Array   | 否   | service=create时,需要创建的对象字段                          |         |





#### 2.1 请求响应成功

```json
{
    "id": "guid",
    "jsonrpc": "2.0",
    "result": {
        "data": [
            {
                "mobile": null,
                "remark": null,
                "login": "huangbaoming",
                "name": "黄宝明",
                "id": "02k94e932xgjk",
                "email": null,
                "status": "0"
            }
        ]
    }
}
```

#### 2.2 请求响应失败

```json
{
    "id": "guid",
    "jsonrpc": "2.0",
    "error": {
        "code": 7100,
        "message": "身份验证失败",
        "data": {
            "name": "java.lang.NullPointerException",
            "debug": "java.lang.NullPointerException:com.sie.iidp.engine.container.Meta.setArguments(Meta.java:143)"
        }
    }
}
```



| 参数       | 类型   | 必填 | 描述                 | 可选值 |
| ---------- | ------ | ---- | -------------------- | ------ |
| id         | String | 否   | 请求id               |        |
| jsonrpc    | String | 是   | jsonrpc版本,默认:2.0 |        |
| error      | Object | 是   | 响应错误信息         |        |
| code       | int    | 是   | 错误编码             |        |
| data.name  | String | 否   | 错写消息             |        |
| data.debug | String | 否   | 异常详情             |        |



####



