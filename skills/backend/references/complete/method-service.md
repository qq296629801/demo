---
title: 添加方法和服务
date: 2023-09-25 17:31:35
permalink: /pages/59895d/
---
### 3.3 添加方法和服务

模型的方法在定义模型的java类中定义，但并不是类中所有的java方法都会成为模型的方法。满足以下规则的java方法才会被解析为模型的方法：

​                ● 同一模型中方法名必须唯一。为了避免冲突，模型方法不支持重载，类似于Python的方法。

​                ● 必须声明为public。模型类中定义的非public方法会被认为是辅助方法，不会解析为模型方法。

模型的方法可以定义在多个扩展或多个被继承的模型中，它们可能在不同的java类中，并且这些java类没有继承关系。模型的方法可以在扩展或继承模型中重写。

服务远程遵循 jsonrpc 2.0 协议，调用的地址：http(s)://ip:port/root/rpc/service，示例：

清求：

```json
{
	"jsonrpc": "2.0",
	"method": "read",
	"id": "99BA5433-DF5F-A898-C8E0-78B8BA55F251",
	"params": {
		"args": {
			"ids": [
				"01ng9if6fve2o"
			],
			"fields": [
				"name",
				"login"
			]
		},
		"model": "res.user",
		"context": {
			"tz": "",
			"uid": "01ng9if6fve2o",
			"lang": "zh_CN",
			"ticket": "ticket",
			"tenant": "tenant"
		}
	}
}
```

响应：

```json
{
	"id": "99BA5433-DF5F-A898-C8E0-78B8BA55F251",
	"result": {
		"data": [
			{
				"name": "张三",
				"id": "01no6a1ocg9hc",
				"login": "zhangsan"
			}
		],
		"context": {
			"token": "token"
		}
	},
	"jsonrpc": "2.0"
}
```





###             3.3.1.     服务声明

​      模型的方法只能在代码中调用，服务可以提供远程api调用。服务有两种方式声明：

​                ● 在模型方法上使用@MethodService声明

​                ● 在模型上使用@Service声明一个BaseService的子类提供服务方法

在模型上使用@SDK.Service声明一个BaseService的子类提供服务方法该子类建议重写BaseService所有方法，包括before,execute,getArgsSpec,getResultSpec

####             3.3.1.1.     @MethodService声明

```java
@Model
public class TestUser extends BaseModel {

    @MethodService(description = "登录")
    public Map<String, Object> login(RecordSet rec, String login, String password, boolean remember,
                                         Map<String, Object> userAgent) {

    }
}
```



###             3.3.2.     通用模型服务

通用模型服务即每个模型都具备的服务。

####             3.3.2.1.     create

为模型创建新记录，请求：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"valuesList": []
		},
		"service": "create",
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master"
	}
}
```

响应：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"result": {
		"data": {},
		"context": {}
	}
}
```



####             3.3.2.2.     delete

删除当前集合的记录。

请求：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"ids": [
				"id"
			]
		},
		"service": "delete",
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master"
	}
}
```

响应：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"result": {
		"data": {},
		"context": {}
	}
}
```



####             3.3.2.3.     update

使用提供的值更新当前集中的所有记录。

请求：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"values": {},
			"ids": [
				"id"
			]
		},
		"service": "update",
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master"
	}
}
```

响应：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"result": {
		"data": {},
		"context": {}
	}
}
```



####             3.3.2.4.     read

读取记录集指定字段的值。

请求：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"properties": []
		},
		"service": "read",
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master"
	}
}
```

响应：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"result": {
		"data": {},
		"context": {}
	}
}
```

####             3.3.2.5.     find

根据参数搜索记录。

请求：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"filter": [],
			"offset": 0,
			"limit": 25,
			"order": ""
		},
		"service": "find",
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master"
	}
}
```

响应：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"result": {
		"data": {},
		"context": {}
	}
}
```



####             3.3.2.6.     search

搜索并读取记录集指定字段的值。

请求：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"filter": [],
			"offset": 1,
			"limit": 1,
			"properties": [],
			"order": ""
		},
		"service": "search",
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master"
	}
}
```

响应：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"result": {
		"data": {},
		"context": {}
	}
}
```

####             3.3.2.7.     count

统计匹配条件的记录数。

请求：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"filter": []
		},
		"service": "count",
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
    "model": "meta_model",
		"tag": "master"
	}
}
```

响应：

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"result": {
		"data": {},
		"context": {}
	}
}
```

####             3.3.2.8.     **exists**

返回存在的记录

请求

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"ids": [
				"id"
			]
		},
		"service": "exists",
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_app_dependency",
		"tag": "master"
	}
}
```

响应

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"result": {
		"data": {},
		"context": {}
	}
}
```



###             3.3.3.     自定义模型服务

返回传入的参数

![img](https://wdcdn.qpic.cn/MTY4ODg1NzU0NjMyMjY3Mw_229352_zB9myNTTaF9CvneE_1672361879?w=1510&h=316)

请求

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"userName": "test"
		},
		"service": "find2",
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "mom_item",
		"tag": "master"
	}
}
```

响应

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"result": {
		"data": {},
		"context": {}
	}
}
```

### 3.3.4 API服务接口请求参数和响应结果

#### 3.3.4.1 API请求参数

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
| filter             | String  | 否   | 查询过滤条件,示例:<br/>`["\|",["name","like","%admin"],["email","like","%126"]]` |         |
| useDisplayForModel | Boolean | 否   | 需要显示关联ManyToOne对象name时,设置为true                   | true    |
| ids                | Array   | 否   | 主键id集合,示例: `["020uzt2d3sr9c"]`                         |         |
| values             | Object  | 否   | service=update时,需要更新的对象字段                          |         |
| valuesList         | Array   | 否   | service=create时,需要创建的对象字段                          |         |



#### 3.3.4.2 请求响应成功

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

#### 3.3.4.3 请求响应失败

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





###             3.3.5.     服务编排

服务编排一般用来，对已有的服务进行组合编排，对外实现新的服务能力。

####             3.3.5.1.     声明新服务

```java
    @MethodService(name = "service3", auth = "read", description = "测试模型model2服务3", ServiceOrchestrateDefine = TestServiceOrchestrateDefine.class)
    public String service3(String param3) {
        System.out.println("model2_service3");
        System.out.println("入口参数param3：" + param3);
        return param3;
    }
```



####             3.3.5.2.     指定编排逻辑

通过@MethodService中ServiceOrchestrateDefine指定编排逻辑
```java
/**
 * 服务编排测试，使用用户模型的服务进行测试
 */
public class TestServiceOrchestrateDefine implements IServiceOrchestrateDefine {
    @Override
    public ServiceOrchestrate execute(String soName) {

        //有向图的数据结构
        //服务编排组成元素定义
        ServiceOrchestrateObjectFactory serviceOrchestrateObjectFactory = new ServiceOrchestrateObjectFactory();
        BaseMeta start = serviceOrchestrateObjectFactory.createStart(soName);
        List<BaseMeta> list = new ArrayList<>();
        list.add(start);
        ServiceOrchestrate serviceOrchestrate = ServiceOrchestrate.createServiceOrchestrate(soName, list);
        /**
         * start -> refModel1Service1 -> conditionGateway1 -> refModel2Service2 -> end
         *                                                 -> condition1        -> refModel1Service2 -> refModel2Service1 -> end
         *                                                 -> condition2        -> refModel1Service3 -> refModel2Service4 -> end
         */

        //服务编排定义
        //property\model Map<k,V>
        serviceOrchestrate
                .start()
                .forwardService("refModel1Service1")
                .forwardConditionGateway(
                        "conditionGateway1", "refModel2Service2",
                        "condition1", "condition2").end()
                .append("conditionGateway1.condition1", "refModel1Service2").end()
                .append("conditionGateway1.condition2", "refModel1Service3").end();
        serviceOrchestrate.append("refModel1Service2", "refModel2Service1");
        return serviceOrchestrate.append("refModel1Service3", "refModel2Service4").build();
    }
}
```
####             3.3.5.3.     新服务调用

新定义编排服务，与常规服务调用方式一样，通过标准的api格式调用。

### 3.3.6 异步执行

#### 3.3.6.1模型开启新线程执行方法

![aa7d8c0c087de2c6522defe77d67ca8](./images/aa7d8c0c087de2c6522defe77d67ca8.png)

#### 3.3.6.2 第三方线程调用模型方法

```java

    @Ignore
    private static Meta meta1;

    /**
     * 在该方法被执行时，缓存meta
     * @param rs
     */
    public void start(RecordSet rs) {
        meta1 = rs.getMeta();//在启动事件时（二选一即可）
        meta1 = getMeta();// 服务被调用时（二选一即可）
    }

    /**
     * 只能在第三方线程调用该方法
     * @param data
     */
    public static void test1(Map<String, Object> data) {
        try (Meta meta  = meta1.createNewMeta()) {
            BaseContextHandler.setMeta(meta);

            //执行模型操作
            Customer customer = new Customer();
            customer.setName(Objects.toString(data.get("name")));
            customer.create();

            BaseContextHandler.remove();
            meta.flush();
        }
    }
```

