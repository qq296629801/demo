---
title: 创建模型
date: 2023-09-25 17:31:34
permalink: /pages/0dc273/
---
#             3.1.     **创建模型**

BaseModel是业务模型的基类所有业务模型继承该模型

##             3.1.1.     **通过@Model注解声明模型**

@Model参数说明

| 参数        | 说明                                                         | 类型                                                         | 默认值         |
| ----------- | ------------------------------------------------------------ | ------------------------------------------------------------ | -------------- |
| name        | 模型的名称，与inherit应至少指定一个                          | String                                                       | 默认为类名     |
| type        | 模型类型                                                     | Define:定义模型Buss:业务模型Memory:内存模型Data:瞬态模型Cache:缓存模型Config:配置模型 |                |
| description | 模型的说明                                                   | String                                                       |                |
| parent      | 继承的模型，与name应至少指定一个如果name已设置，则表示要继承的模型的名称如果name未设置，则表示扩展的单个模型的名称 | String[]                                                     |                |
| tableName   | 模型的表名                                                   | String                                                       | 默认为类名小写 |
| isAbstract  | 是否抽象模型，抽象模型不生成数据库                           | Bool                                                         |                |
| orderBy     | 默认排序SQL，如：name asc                                    | String                                                       |                |
| files       | 配置模型对应的文件 文件可以不填后缀名，则以此搜索 .properties .json .yml | String[]                                                     |                |
| isAutoLog   | 是否自动生成创建人、创建时间、修改人、修改时间字段           | Bool                                                         | false          |

###             3.1.2.     **通过extends BaseModel标识模型具备元模型能力**

BaseModel提供了操作模型的通用接口，包括如下：

| 接口名     | 接口描述   | 入参                                                         | 返回值  |
| ---------- | ---------- | ------------------------------------------------------------ | ------- |
| create     | 新增       | 无                                                           | T       |
| delete     | 删除       | 无                                                           | void    |
| count      | 统计条数   | Filter filter                                                | void    |
| search     | 查询       | Filter filter, List`<String>` properties, Integer limit, Integer offset, String order | List`<T>` |
| selectById | 根据id查询 | String id                                                    | T       |
| update     | 更新       | update                                                       | void    |



###             3.1.3.     **内置模型类型**

开发者在内置模型的基础上自定义模型。内置模型按类型提供了具有不同特色的功能，但都具有模型的所有基本功能，即提供增删改查服务，继承扩展等。

####             3.1.3.1.     **业务模型**

默认选项。

​      type = Model.ModelType.Buss

默认持久化到数据库，支持一对多、多对多和多对一关系，支持所有属性类型。

业务模型是功能强大丰富的模型类型，适合建立各种常规模型。

####             3.1.3.2.     **数据模型**

​      type = Model.ModelType.Data

又称为瞬时模型。不持久化，当会话结束时自动销毁。不适合大量数据。

数据模型建立方便，增删改查消耗少，速度快。

####             3.1.3.3.      **内存模型**

​     type = Model.ModelType.Memory

内存模型是带有持久化功能的数据模型。可以更新到数据库，查询则只通过内存查询。

####             3.1.3.4.     **树状模型**

type = Model.ModelType.Tree

树状模型以树形结构存储数据，适合非关系型数据结构。

####             3.1.3.5.     **配置模型**

type = Model.ModelType.Config

配置模型可以读取本地文件，同步远程配置中心数据（比如阿波罗配置中心，nacos）



###             3.1.4.     **模型的继承和扩展**

系统提供了以模块化的方式来继承或扩展模型：

​                ● 子模型继承父模型，子模型会拥有父模型所有的属性和服务

​                ● 扩展已定义的模型，为已有模型增加能力，或改变已有模型的现有能力

####             3.1.4.1.     **继承**

通过@Model指定name和parent，当name和parent不一致时为模型继承关系，parent是父模型名称，name是子模型的名称。新模型将继承原模型的所有字段、方法和服务。示例如下:

@Model(name = "a_model",parent = "b_model")public class AModel extends TestUser{}

继承服务移除：子模型继承了父模型，但是不想具备父模型中个别的服务，这时需要用到服务移除@Service(remove=[“serviceName1”,”serviceName2”]

####             3.1.4.2.     **扩展**

​       通过@Model指定name和parent，当name和parent一致时为模型扩展关系。扩展已定义的模型，为已有模型增加能力，或改变已有模型的现有能力，示例如下:

@Model(name = "TestUser",parent = "TestUser")

public class TestUserExt extends TestUser{}

####             3.1.4.3.     **多继承**

​         @Model 中parent是个数组，可以通过设置多个parent，表示多继承，该模型会自动继承多个父模型。

###             3.1.5.     **关联模型维护**
关联模型维护是指在维护一个模型时，同时对其关联的模型进行操作，简单的说就是通过一个API接口同时操作有ER关系的多个模型CRUD。

####             3.1.5.1.     **指令集**

**指令集: OneToMany and ManyTomany级联操作使用特殊的指令集**

OneToMany 和 ManyToMany 使用特殊的 “命令” 格式来操纵存储在字段中/与字段相关联的记录集。

此格式是按顺序执行的三元组列表，其中每个三元组是在记录集上执行的命令。 并非所有命令都适用于所有情况。 可能的命令是：



ManyToMany

(0,0,{values}) 根据values里面的信息新建一个记录。

(1,ID,{values})更新id=ID的记录（写入values里面的数据）

(2,ID) 删除id=ID的数据（调用unlink方法，删除数据以及整个主从数据链接关系）

(3,ID) 切断主从数据的链接关系但是不删除这个数据

(4,ID) 为id=ID的数据添加主从链接关系。

(5) 删除所有的从数据的链接关系就是向所有的从数据调用(3,ID)

(6,0,[IDs]) 用IDs里面的记录替换原来的记录（就是先执行(5)再执行循环IDs执行（4,ID））

例子[(6, 0, [8, 5, 6, 4])] 设置 ManyTomany to ids [8, 5, 6, 4]

OneToMany

(0, 0,{ values })根据values里面的信息新建一个记录。

(1,ID,{values}) 更新id=ID的记录（对id=ID的执行write 写入values里面的数据）

(2,ID) 删除id=ID的数据（调用unlink方法，删除数据以及整个主从数据链接关系）



####             3.1.5.2.     **一对多创建:创建模型和添加属性**

**使用指令(0, 0, values)**

添加从提供的值 values创建的新记录。

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"valuesList": [
				{
					"tag": "master",
					"source": "manual",
					"is_abstract": false,
					"auto_log": false,
					"name": "iot_device",
					"display_name": "iot_device",
					"table_name": "iot_device",
					"description": "设备",
					"app_ids": "021r397wz5fcw",
					"property_ids": [
						[
							0,
							0,
							{
								"name": "status",
								"data_type": "Integer",
								"display_name": "状态",
								"description": null,
								"display": true,
								"source": "manual",
								"default_value": null,
								"length": null,
								"display_for_model": false,
								"required": false,
								"read_only": false,
								"unique": false,
								"store": true,
								"db_index": false,
								"property_type": "Normal",
								"compute_script": null
							}
						],
						[
							0,
							0,
							{
								"name": "name",
								"data_type": "String",
								"display_name": "设备名称",
								"description": "设备名称",
								"display": true,
								"source": "manual",
								"default_value": null,
								"length": null,
								"display_for_model": false,
								"required": false,
								"read_only": false,
								"unique": false,
								"store": true,
								"db_index": false,
								"property_type": "Normal",
								"compute_script": null
							}
						]
					]
				}
			]
		},
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master",
		"service": "create"
	}
}
```





####             3.1.5.3.     **一对多删除**

**使用指令集: (2, id, 0)**

从集合中删除 id id 的记录，然后删除它（从数据库中）。 不能在 create中使用。

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"values": {
				"property_ids": [
					[
						2,
						"022568sr2jbpc",
						0
					]
				]
			},
			"ids": [
				"022568sqik4xs"
			]
		},
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master",
		"service": "update"
	}
}
```



####             3.1.5.4.     **一对多修改子表**

**使用指令集 (1, id, values)**

使用值中的值更新 id id的现有记录。 不能在create中使用

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"values": {
				"app_ids": "021r397wz5fcw",
				"description": "设备",
				"source": "manual",
				"display_name": "iot_device",
				"table_name": "iot_device",
				"is_abstract": false,
				"auto_log": false,
				"property_ids": [
					[
						1,
						"022568sr2jbpc",
						{
							"name": "ip",
							"data_type": "String",
							"display_name": "ip",
							"description": null,
							"display": true,
							"source": "manual",
							"default_value": null,
							"length": null,
							"display_for_model": false,
							"required": false,
							"read_only": false,
							"unique": false,
							"store": true,
							"db_index": false,
							"property_type": "Normal",
							"compute_script": null
						}
					]
				],
				"id": "022568sqik4xs"
			},
			"ids": [
				"022568sqik4xs"
			]
		},
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master",
		"service": "update"
	}
}
```



####             3.1.5.5.     **一对多添加-模型添加属性**

**使用指令(0, 0, values)**



{  "id": "guid",  "jsonrp

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"values": {
				"app_ids": "021r397wz5fcw",
				"description": "设备",
				"source": "manual",
				"display_name": "iot_device",
				"table_name": "iot_device",
				"is_abstract": false,
				"auto_log": false,
				"property_ids": [
					[
						0,
						0,
						{
							"name": "ip",
							"data_type": "String",
							"display_name": "ip",
							"description": null,
							"display": true,
							"source": "manual",
							"default_value": null,
							"length": null,
							"display_for_model": false,
							"required": false,
							"read_only": false,
							"unique": false,
							"store": true,
							"db_index": false,
							"property_type": "Normal",
							"compute_script": null
						}
					]
				],
				"name": "iot_device",
				"id": "02254bc01kz5s",
				"tag": "master"
			},
			"ids": [
				"02254bc01kz5s"
			]
		},
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master",
		"service": "update"
	}
}
```





####             3.1.5.6.     **一对多查询**

模型-属性:

第一步:根据模型查询模型所有属性,property_ids返回所有的属性id列表

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"filter": [
				[
					"name",
					"=",
					"meta_app"
				]
			],
			"offset": 0,
			"limit": 25,
			"properties": [
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
				"property_ids"
			]
		},
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model",
		"tag": "master",
		"service": "search"
	}
}
```

第二步:返回的,property_ids列表查询所有的属性

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"filter": [
				[
					"id",
					"in",
					[
						"0224zv0atrmkg",
						"0224zv09ftnnk",
						"0224zv0bdqtc0",
						"0224zv0b19bls",
						"0224zv0bnqeps",
						"0224zv09nbcow"
					]
				]
			],
			"offset": 0,
			"limit": 0,
			"properties": [
				"name",
				"display_name",
				"data_type",
				"required",
				"read_only",
				"store",
				"db_index"
			],
			"order": ""
		},
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "meta_model_property",
		"tag": "master",
		"service": "search"
	}
}
```



####             3.1.5.7.     **多对多创建**

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"valuesList": [
				{
					"name": "开发者",
					"is_admin": false,
					"user_ids": [
						[
							4,
							"02gonoedj1a0x",
							0
						]
					]
				}
			]
		},
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "rbac_role",
		"tag": "master",
		"service": "create",
		"app": "base"
	}
}
```



####             3.1.5.8.     **多对多删除**

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"values": {
				"is_admin": false,
				"name": "普通用户",
				"id": "02gonoe8t86ip",
				"user_ids": [
					[
						3,
						"02gonoedbjkzk",
						0
					]
				]
			},
			"ids": [
				"02gonoe8t86ip"
			]
		},
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "rbac_role",
		"tag": "master",
		"service": "update",
		"app": "base"
	}
}
```



####             3.1.5.9.     **多对多添加**

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"valuesList": [
				{
					"user_ids": [
						[
							4,
							"024shzay3nwn4",
							0
						]
					],
					"name": "111111111111",
					"is_admin": true
				}
			]
		},
		"context": {
			"uid": "",
			"lang": "zh_CN",
			"useDisplayForModel": true
		},
		"model": "rbac_role",
		"tag": "master",
		"service": "create"
	}
}
```



####             3.1.5.10.     **多对多查询**

分2步,第一步先查询主表数据

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"filter": [
				[
					"id",
					"=",
					"02gwfpd3os2kg"
				]
			],
			"offset": 0,
			"limit": 1,
			"order": "",
			"properties": [
				"name",
				"is_admin",
				"user_ids",
				"permission_ids"
			],
			"useDisplayForModel": true
		},
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "rbac_role",
		"tag": "master",
		"service": "search",
		"app": "base"
	}
}
```

第2步,根据主表id去查询关联表

```json
{
	"id": "guid",
	"jsonrpc": "2.0",
	"method": "service",
	"params": {
		"args": {
			"filter": [
				[
					"role_ids",
					"=",
					"02gwfpd3os2kg"
				]
			],
			"offset": 0,
			"limit": 31,
			"properties": [
				"login",
				"email",
				"mobile",
				"name"
			],
			"order": "",
			"useDisplayForModel": true
		},
		"context": {
			"uid": "",
			"lang": "zh_CN"
		},
		"model": "rbac_user",
		"tag": "master",
		"service": "search",
		"app": "base"
	}
}
```

### 3.1.6.索引

可以在模型上，为最终生成的表添加索引

```java
// 复合唯一索引
@Model(name = "model_name", indexes = { @Index(name = "IDX_USER_IP", columnList = {"user_ids","ip" }, unique = true)})

// 复合索引
@Model(name = "model_name", indexes = { @Index(name = "UQ_USER_IP", columnList = {"user_ids","ip" })})

// 单索引
@Model(name = "model_name", indexes = { @Index(name = "IDX_USER", columnList = {"user_ids"})})
```

