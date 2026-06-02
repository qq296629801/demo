---
title: 视图模型
date: 2023-09-25 17:31:35
permalink: /pages/87cb74/
---
### 概念

1. 视图模型vm是其他模型（一个或多个）的逻辑组合，通过vm及vm的属性记录组合规则。
2. vm不持久化，也不在内存存储数据。vm的查询、创建、更新、删除操作是通过调用其他模型的方法实现的。
3. 对前端来说，vm和普通模型没有区别。
4. vm支持查询操作，创建、更新、删除等尽量支持，用户可以重写所有操作。

使用到的注解：

`@View.From` 指定要映射的模型

`@View.MapProperty` 指定要映射的属性，`filter`指定内部筛选条件（字段使用from模型的字段）

```java
@Model(type = Model.ModelType.View)
@View.From("meta_app")
public class MetaAppVm extends BaseModel<MetaAppVm> {
    /**
     * 映射meta_app模型的name属性，字符串类型
     */
    private String name;

    private String display_name;

    private String summary;

    /**
     * 映射meta_app模型的source属性，字符串类型
     */
    @View.MapProperty(value = "source", filter = "[[\"source\", \"=\", \"'base'\"]]")
    private String app_source;

    @View.MapProperty(value = "category_ids.name")
    private String categoryName;

    @View.MapProperty(value = "category_ids.description")
    private String categoryDescription;

    /**
     * 映射meta_app模型的dependency_ids属性，OneToMany
     */
    @View.MapProperty("dependency_ids")
    private String dependency;

    /**
     * 映射meta_app模型的product属性，ManyToOne
     */
    private String product;

    /**
     * 累加一对多的多的一侧的值，可以JOIN、SUM
     */
    @View.MapProperty("model_ids.name")
    @View.MapFunction(value = SQLFunction.JOIN, args = ",")
    private String modelNames;

    /**
     * 只添加@Property属性，代表不映射
     * 可重写search方法，自由赋值
     */
    @Property
    private String time;
}
```

当所有的`@View.MapProperty`映射的都是同一个模型的属性时，视图模型的增删改查都有效，如果不是，则只有查询生效。

### 配置模式

@View.From是必须的

字段上如果没有@View.MapProperty注解，则@View.MapProperty的值默认为字段名称，如下：

```java
private String name;
```

#### Many2One的属性可以多级关联

如下，model_ids是Many2One

```java
@View.MapProperty("model_ids.name")
private String model;
```

也可以这样，此时model_ids和user_ids都是Many2One

```java
@View.MapProperty("model_ids.user_ids")
private String user;
```

#### One2Many和Many2Many只能关联一级

如下，model_ids是One2Many或Many2Many，这种情况是关联了model_ids对应的模型

```java
@View.MapProperty("model_ids")
private Object model;
```

如下，model_ids是One2Many或Many2Many，此时是把model_ids对应的模型的所有name属性值，用join操作拼接在一起（以逗号分隔），类似于java的`String.join`方法

```java
@View.MapProperty("model_ids.name")
@View.MapFunction(value = SQLFunction.JOIN, args = ",")
private Object model;
```

### 聚合查询

vm除了支持属性名查询外，还支持聚合查询，目前支持SUM COUNT AVG MAX MIN JOIN。

查询时请注意类型是否支持。

例如如下是查询所有的名称，并拼接：

```json
{
    "id": "guid",
    "jsonrpc": "2.0",
    "method": "service",
    "params": {
        "args": {
            "useDisplayForModel": false,
            "order": "",
            "filter": [],
            "limit": 31,
            "offset": 0,
            "properties": [
                {
                    "value":"JOIN",
                    "args":[","],
                    "alias":"names",
                    "name":"name"
                }
            ]
        },
        "context": {
            "uid": "",
            "lang": "zh_CN"
        },
        "model": "demo_order_vm",
        "tag": "master",
        "service": "search",
        "app": "newSdkApp"
    }
}
```

结果如下：

```json
{
    "id": "guid",
    "jsonrpc": "2.0",
    "result": {
        "data": [
            {
                "names": "order1,order2,order3,order4,order5,order6"
            }
        ]
    }
}
```

还部分支持分组功能：

如下：

```json
{
    "id": "guid",
    "jsonrpc": "2.0",
    "method": "service",
    "params": {
        "args": {
            "useDisplayForModel": false,
            "order": "",
            "filter": [],
            "limit": 31,
            "offset": 0,
            "properties": [
                "date",
                {
                    "value":"JOIN",
                    "args":[","],
                    "alias":"names",
                    "name":"name"
                }
            ]
        },
        "context": {
            "uid": "",
            "lang": "zh_CN"
        },
        "model": "demo_order_vm",
        "tag": "master",
        "service": "search",
        "app": "newSdkApp"
    }
}
```

结果：

```json
{
    "id": "guid",
    "jsonrpc": "2.0",
    "result": {
        "data": [
            {
                "date": "2021-10-10 18:00:00",
                "names": "order3"
            },
            {
                "date": "2022-12-10 18:00:00",
                "names": "order1,order4,order5,order6"
            },
            {
                "date": "2023-12-05 17:00:00",
                "names": "order2"
            }
        ]
    }
}
```

其他示例:

```json
                {
                    "value":"MAX",SUM COUNT AVG MAX MIN中任一个
                    "alias":"price", 别名
                    "name":"price" 名称
                }
```

代码调用可使用`ViewModelDataAccess.MapFunction`
