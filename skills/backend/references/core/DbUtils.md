---
title: DbUtils接口文档
date: 2024-12-27 14:12:12
---


# DbUtils接口文档

DbUtils 是一个数据库操作工具类，提供了一系列静态方法来执行数据库的CRUD操作（创建、读取、更新、删除）以及批量处理和异步操作。

## 1. 批量保存
- **方法签名**: `public static <T> void batchCreate(List<T> list)`
- **描述**: 批量保存对象列表。
- **参数**:
    - `list`: 需要批量保存的对象列表。
- **返回**: 无
- **批量插入大量数据时**，请参考:[**23.批量插入大量数据性能优化**](#_23-批量插入大量数据性能优化)

## 2. 批量保存（指定类）
- **方法签名**: `public static void batchCreate(Class<?> clazz, List<?> list)`
- **描述**: 批量保存对象列表，指定类。
- **参数**:
    - `clazz`: 对象的类类型。
    - `list`: 需要批量保存的对象列表。
- **返回**: 无

## 3. 批量保存（指定模型名）
- **方法签名**: `public static void batchCreate(String modelName, List<Map<String, Object>> list)`
- **描述**: 批量保存对象列表，指定模型名。
- **参数**:
    - `modelName`: 模型名称。
    - `list`: 需要批量保存的对象列表，以Map形式表示。
- **返回**: 无

## 4. 新增
- **方法签名**: `public static <T extends BaseModel> T create(T t, Class<?> clazz)`
- **描述**: 新增对象。
- **参数**:
    - `t`: 需要新增的对象。
    - `clazz`: 对象的类类型。
- **返回**: 新增后的对象。

## 5. 删除
- **方法签名**: `public static <T> void delete(String id, Class<?> clazz)`
- **描述**: 根据ID删除对象。
- **参数**:
    - `id`: 对象的ID。
    - `clazz`: 对象的类类型。
- **返回**: 无

## 6. 条件删除
- **方法签名**: `public static void delete(Filter filter, Class<?> clazz)`
- **描述**: 根据条件删除对象。
- **参数**:
    - `filter`: 删除条件。
    - `clazz`: 对象的类类型。
- **返回**: 无

## 7. 条件删除（直接执行SQL）
- **方法签名**: `public static void deleteByFilter(Filter filter, String modelName)`
- **描述**: 根据条件删除对象，直接走SQL。
- **参数**:
    - `filter`: 删除条件。
    - `modelName`: 模型名称。
- **返回**: 无

## 8. 条件删除（直接执行SQL，级联）
- **方法签名**: `public static void deleteByFilter(Filter filter, boolean cascade, String modelName)`
- **描述**: 根据条件删除对象，直接走SQL，支持级联删除。
- **参数**:
    - `filter`: 删除条件。
    - `cascade`: 是否级联删除。
    - `modelName`: 模型名称。
- **返回**: 无

## 9. 更新对象
- **方法签名**: `public static <T extends BaseModel> void update(T t, Class<?> clazz)`
- **描述**: 更新对象。
- **参数**:
    - `t`: 需要更新的对象。
    - `clazz`: 对象的类类型。
- **返回**: 无

## 10. 更新对象（指定模型名）
- **方法签名**: `public static void update(Map<String, Object> values, String modelName)`
- **描述**: 更新对象，指定模型名。
- **参数**:
    - `values`: 需要更新的值。
    - `modelName`: 模型名称。
- **返回**: 无

## 11. 批量更新
- **方法签名**: `public static <T> void batchUpdate(List<T> list)`
- **描述**: 批量更新对象列表。
- **参数**:
    - `list`: 需要批量更新的对象列表。
- **返回**: 无

## 12. 批量更新（指定模型名）
- **方法签名**: `public static void batchUpdate(List<Map<String, Object>> list, String modelName)`
- **描述**: 批量更新对象列表，指定模型名。
- **参数**:
    - `list`: 需要批量更新的对象列表，以Map形式表示。
    - `modelName`: 模型名称。
- **返回**: 无

## 13. 计数
- **方法签名**: `public static long count(Filter filter, Class<?> clazz)`
- **描述**: 根据条件计数。
- **参数**:
    - `filter`: 计数条件。
    - `clazz`: 对象的类类型。
- **返回**: 计数结果。

## 14. 根据ID查找对象
- **方法签名**: `public static <T extends BaseModel> T selectById(String id, @ModelName String modelName, Class<? extends BaseModel> modelClass, String... columns)`
- **描述**: 根据ID查找对象。
- **参数**:
    - `id`: 对象的ID。
    - `modelName`: 模型名称。
    - `modelClass`: 对象的类类型。
    - `columns`: 需要查询的列（可选）。
- **返回**: 查找到的对象。

## 15. 复制对象
- **方法签名**: `public static <T extends BaseModel> T copy(Class<? extends BaseModel> modelClass, Map<String, Object> value)`
- **描述**: 复制对象。
- **参数**:
    - `modelClass`: 对象的类类型。
    - `value`: 需要复制的值。
- **返回**: 复制后的对象。

## 16. 复制多个对象
- **方法签名**: `public static <T> List<T> copys(Class<? extends BaseModel> modelClass, List<Map<String, Object>> values)`
- **描述**: 复制多个对象。
- **参数**:
    - `modelClass`: 对象的类类型。
    - `values`: 需要复制的值列表。
- **返回**: 复制后的对象列表。

## 17. 查找对象
- **方法签名**: `public static <T extends BaseModel> T select(Filter filter, @ModelName String modelName, Class<? extends BaseModel> modelClass)`
- **描述**: 查找对象。
- **参数**:
    - `filter`: 查找条件。
    - `modelName`: 模型名称。
    - `modelClass`: 对象的类类型。
- **返回**: 查找到的对象。

## 18. 查找对象（指定列）
- **方法签名**: `public static <T extends BaseModel> T select(Filter filter, @ModelName String modelName, Class<? extends BaseModel> modelClass, String... columns)`
- **描述**: 查找对象，指定列。
- **参数**:
    - `filter`: 查找条件。
    - `modelName`: 模型名称。
    - `modelClass`: 对象的类类型。
    - `columns`: 需要查询的列。
- **返回**: 查找到的对象。

## 19. 搜索
- **方法签名**: `public static <T> List<T> search(Filter filter, List<String> properties, Integer limit, Integer offset, String order, Class<? extends BaseModel> modelClass)`
- **描述**: 搜索对象列表。
- **参数**:
    - `filter`: 搜索条件。
    - `properties`: 属性列表。
    - `limit`: 每页条数。
    - `offset`: 页码。
    - `order`: 排序。
    - `modelClass`: 对象的类类型。
- **返回**: 搜索结果列表。

## 20. 搜索（指定模型名）
- **方法签名**: `public static List<Map<String, Object>> search(Filter filter, List<String> properties, Integer limit, Integer offset, String order, String modelName)`
- **描述**: 搜索对象列表，指定模型名。
- **参数**:
    - `filter`: 搜索条件。
    - `properties`: 属性列表。
    - `limit`: 每页条数。
    - `offset`: 页码。
    - `order`: 排序。
    - `modelName`: 模型名称。
- **返回**: 搜索结果列表。

## 21. 异步调用
- **方法签名**: `public static CompletableFuture<Void> callAsync(Runnable runnable, Class clazz)`
- **描述**: 异步调用。
- **参数**:
    - `runnable`: 需要异步执行的Runnable。
    - `clazz`: 类类型。
- **返回**: 异步执行结果。

## 22. 异步调用（返回值）
- **方法签名**: `public static <T> CompletableFuture<T> callAsync(Supplier<T> supplier, Class clazz)`
- **描述**: 异步调用，返回结果。
- **参数**:
    - `supplier`: 需要异步



## 23. 批量插入大量数据性能优化

批量插入大量数据时,比如1万-100万数据，优化性能是非常重要的。以下是一些优化批量插入操作的步骤和建议：

### 第一步：调整数据库连接配置

1. **启用`rewriteBatchedStatements`参数**：

    - 在数据库连接URL中添加`rewriteBatchedStatements=true`参数。这将使得JDBC驱动能够将批量SQL语句合并为单个请求发送到数据库，从而提高性能。

    - 示例URL配置：

      ```properties
      url=jdbc:mysql://127.0.0.1:3307/snest?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true
      ```

2. **减少SQL统计数量**：

    - 在`dbcp.properties`配置中添加`connectionProperties`，减少Druid统计的SQL数量，这有助于减少内存消耗和提高性能。

    - 示例配置：

      ```properties
      connectionProperties:druid.stat.sql.MaxSize=100;druid.stat.mergeSql=true
      ```

3. **`dbcp.properties`完整的示例**：

    - 在`dbcp.properties`配置中添加`connectionProperties`，减少Druid统计的SQL数量，这有助于减少内存消耗和提高性能。

    - 示例配置：

      ```properties
      ########DBCP##########
      driverClassName=com.mysql.cj.jdbc.Driver
      #url
      url=jdbc:mysql://127.0.0.1:3306/snest?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true
      username=***
      password=***
      initialSize=5
      maxActive=30
      minIdle=5
      maxWait=6000
      filters=stat
      timeBetweenEvictionRunsMillis=60000
      minEvictableIdleTimeMillis=300000
      testOnBorrow=false
      testOnReturn=false
      testWhileIdle=true
      poolPreparedStatements: true
      maxOpenPreparedStatements: 20
      connectionProperties:druid.stat.sql.MaxSize=50;druid.stat.mergeSql=true
      ```


### 第二步：调用批量插入方法

1. **使用`DbUtils.batchCreate`方法**：

    - 默认情况下，每1000条数据作为一个批次，事物会自动提交一次。您可以在Spring配置文件中修改批次提交大小以适应不同的需求。

    - 示例配置：

      ```properties
      orm.batch.size=4000
      ```

2. **调整批次大小**：

    - 根据实际情况调整批次大小，以平衡性能和内存消耗。较大的批次大小可以减少数据库交互次数，但会增加内存消耗。

### 第三步：优化数据插入前的准备

1. **跳过数据校验**：

    - 如果数据量较大，可以在插入前设置`MetaConstant.IGNORE_VALIDATE=true`以跳过数据校验。这可以显著提高插入速度，但需要确保业务逻辑中已经进行了数据格式和唯一性的校验。

    - 示例代码：

      ```java
      rs.getMeta().addArgument(MetaConstant.IGNORE_VALIDATE, true);
      ```

2. **示例代码**：

    - 创建一个包含大量用户的列表，并使用`DbUtils.batchCreate`方法进行批量插入。

    - 示例代码：

      ```java
      @MethodService(description = "创建用户")
      public void createUser(RecordSet rs) {
          List<TestUser> users = new ArrayList<TestUser>(50000);
          for (int i = 0; i < 50000; i++) {
              TestUser user = new TestUser();
              user.setName(System.currentTimeMillis() + "");
              user.setAge(RandomUtils.nextInt(0, 10));
              user.setEmail(System.currentTimeMillis() + "@qq.com");
              user.setPhone("18767176707");
              user.setPassword("123456");
              user.setCreated(new Date());
              users.add(user);
          }
          rs.getMeta().addArgument(MetaConstant.IGNORE_VALIDATE, true);
          DbUtils.batchCreate(users);
      }
      ```
