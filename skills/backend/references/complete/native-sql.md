---
title: 原生sql执行
date: 2023-09-25 17:31:35
permalink: /pages/77e0e8/
---
###             执行SQL

遇到模型无法实现的复杂查询或者基于性能原因，可以通过BaseContextHandler获取上下文Meta,然后通过Meta的getDataAccessor方法获取当前数据库访问信息，允许直接执行SQL。

```java
    public void addManualModels(MetaContainer ctn) {
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor cr = bussModelDataAccess.getRelationDBAccessor();
        cr.execute("SELECT * FROM meta_model WHERE source=%s", Arrays.asList("manual"));
        for (Map<String, Object> data : cr.fetchMapAll()) {
            ModelMeta modelMeta = new ModelMeta();
            modelMeta = BaseMetaUtil.convertModel(data);
            ctn.addModel(modelMeta);
        }
    }
```



执行自定义的`INSERT\UPDATE\DELETE` SQL后，需要调用Cache的`invalidate()`方法清除缓存以避免模型的数据不一致。

```java
rs.getMeta().getStore().invalidate();
```



####             3.5.1.1.     多条件传参

```java
cr.execute("SELECT * FROM meta_model WHERE field1=%s and field2=%s", Arrays.asList("field1Value","field2Value"));
```



####             3.5.1.2.     in查询传参

```java
cr.execute("SELECT * FROM meta_model WHERE field1 in %s ", Arrays.asList(Arrays.asList("field1Value")));
```

#### 3.5.1.3.  示例代码

```java

@Model(name = "user", description = "用户")
public class User extends BaseModel {

    @Property(length = 128, displayName = "名称", displayForModel = true)
    private String name;

    @Property(length = 128, displayName = "地址", displayForModel = true)
    private String address;


    /**
     * 直接执行SQL
     */
    @MethodService(name = "fun1")
    public void fun1() {
        //获取数据库连接
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
        //执行sql
        dataAccessor.execute("SELECT * from User ");
        //获取执行结果
        List<Map<String, Object>> appMapList = dataAccessor.fetchMapAll();
        for (Map<String, Object> stringObjectMap : appMapList) {
            System.out.println(stringObjectMap.toString());
        }
    }

    /**
     * 多条件传参
     */
    @MethodService(name = "fun2")
    public void fun2() {
        //获取数据库连接
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
        //执行sql
        dataAccessor.execute("SELECT * from User WHERE name=%s and address=%s", Arrays.asList("zs", "fs"));
        //获取执行结果
        List<Map<String, Object>> appMapList = dataAccessor.fetchMapAll();
        for (Map<String, Object> stringObjectMap : appMapList) {
            System.out.println(stringObjectMap.toString());
        }
    }

    /**
     * in查询传参
     */
    @MethodService(name = "fun3")
    public void fun3() {
        //获取数据库连接
        BussModelDataAccess bussModelDataAccess = (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
        RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
        //执行sql
        dataAccessor.execute("SELECT * from User WHERE name=%s and address=%s", Arrays.asList("zs", "fs"));
        dataAccessor.execute("SELECT * from User WHERE name in %s ",
                Arrays.asList(Arrays.asList("zs", "ls")));
        //获取执行结果
        List<Map<String, Object>> appMapList = dataAccessor.fetchMapAll();
        for (Map<String, Object> stringObjectMap : appMapList) {
            System.out.println(stringObjectMap.toString());
        }
    }

}
```

