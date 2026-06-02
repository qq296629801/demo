---
title: BaseModel基本能力
date: 2024-12-27 14:28:39
---

# BaseModel基本能力

BaseModel 是一个模型基类，提供了对模型和数据库的基本操作，包括模型的CRUD操作、属性的获取和设置、异步执行以及服务访问等功能。

## 1. 构造函数
- **方法签名**: `public BaseModel()`
- **描述**: 无参构造函数。

- **方法签名**: `public BaseModel(IDataAccess proxy)`
- **描述**: 带代理的构造函数。
- **参数**:
    - `proxy`: 数据访问代理。

## 2. 数据访问
- **方法签名**: `public ClassLoader getAppClassLoad()`
- **描述**: 获取应用类加载器。

- **方法签名**: `public void setAppClassLoad(ClassLoader appClassLoad)`
- **描述**: 设置应用类加载器。
- **参数**:
    - `appClassLoad`: 类加载器。

- **方法签名**: `public Meta getMeta()`
- **描述**: 获取元数据。
- **返回**: 元数据对象。

- **方法签名**: `public void setMeta(Meta meta)`
- **描述**: 设置元数据。
- **参数**:
    - `meta`: 元数据对象。

- **方法签名**: `@Deprecated public RecordSet getRecordSet()`
- **描述**: 获取记录集（已过时）。

- **方法签名**: `public void setRecordSet(RecordSet recordSet)`
- **描述**: 设置记录集。
- **参数**:
    - `recordSet`: 记录集对象。

## 3. CRUD 操作
- **方法签名**: `public T create()`
- **描述**: 创建当前对象。
- **返回**: 创建后的对象。

- **方法签名**: `public void batchCreate(List<T> tList)`
- **描述**: 批量创建对象。
- **参数**:
    - `tList`: 对象列表。

- **方法签名**: `public long count(Filter filter)`
- **描述**: 根据条件统计数量。
- **参数**:
    - `filter`: 过滤条件。
- **返回**: 数量。

- **方法签名**: `public void update()`
- **描述**: 更新当前对象。

- **方法签名**: `public void batchUpdate(List<T> list)`
- **描述**: 批量更新对象。
- **参数**:
    - `list`: 对象列表。

- **方法签名**: `public void delete()`
- **描述**: 删除当前对象。

- **方法签名**: `public void delete(Filter filter)`
- **描述**: 根据条件删除对象。
- **参数**:
    - `filter`: 过滤条件。

## 4. 高级查询
- **方法签名**: `public <T extends BaseModel> T select(Filter filter)`
- **描述**: 根据条件查询单个对象。
- **参数**:
    - `filter`: 过滤条件。
- **返回**: 查询到的对象。

- **方法签名**: `public <T extends BaseModel> T select(Filter filter, String... columns)`
- **描述**: 根据条件查询单个对象，指定字段。
- **参数**:
    - `filter`: 过滤条件。
    - `columns`: 字段列表。
- **返回**: 查询到的对象。

- **方法签名**: `public <T extends BaseModel> T selectById(String id)`
- **描述**: 根据ID查询单个对象。
- **参数**:
    - `id`: 对象ID。
- **返回**: 查询到的对象。

- **方法签名**: `public List<T> search(Filter filter, List<String> properties, Integer limit, Integer offset, String order)`
- **描述**: 根据条件查询对象列表。
- **参数**:
    - `filter`: 过滤条件。
    - `properties`: 属性列表。
    - `limit`: 限制数量。
    - `offset`: 偏移量。
    - `order`: 排序条件。
- **返回**: 查询到的对象列表。

## 5. 事务操作
- **方法签名**: `public void commit()`
- **描述**: 提交事务。

- **方法签名**: `public void rollback()`
- **描述**: 回滚事务。

## 6. SQL 执行
- **方法签名**: `public void execute(String sql)`
- **描述**: 执行SQL语句。
- **参数**:
    - `sql`: SQL语句。

- **方法签名**: `public void execute(String sql, Collection<?> params)`
- **描述**: 执行带参数的SQL语句。
- **参数**:
    - `sql`: SQL语句。
    - `params`: 参数列表。

- **方法签名**: `public void execute(List<String> sqlList)`
- **描述**: 批量执行SQL语句。
- **参数**:
    - `sqlList`: SQL语句列表。

## 7. 数据获取
- **方法签名**: `public List<Object[]> fetchAll()`
- **描述**: 获取所有结果集。

- **方法签名**: `public List<Map<String, Object>> fetchMapAll()`
- **描述**: 获取所有结果集，以Map形式返回。

- **方法签名**: `public <T> List<T> fetchEntryAll(Class<?> clazz)`
- **描述**: 获取所有结果集，转换为指定类型。
- **参数**:
    - `clazz`: 类型。
- **返回**: 结果列表。

## 8. 异步操作
- **方法签名**: `public CompletableFuture<Void> runAsync(Runnable runnable)`
- **描述**: 异步执行无返回值操作。
- **参数**:
    - `runnable`: 任务。
- **返回**: 异步结果。

- **方法签名**: `public <R> CompletableFuture<R> supplyAsync(Supplier<R> supplier)`
- **描述**: 异步执行有返回值操作。
- **参数**:
    - `supplier`: 任务。
- **返回**: 异步结果。

## 9. 属性获取
- **方法签名**: `public String getStr(String attr)`
- **描述**: 获取字符串属性值。
- **参数**:
    - `attr`: 属性名。
- **返回**: 属性值。

- **方法签名**: `public Integer getInt(String attr)`
- **描述**: 获取整数属性值。
- **参数**:
    - `attr`: 属性名。
- **返回**: 属性值。

- **方法签名**: `public Long getLong(String attr)`
- **描述**: 获取长整数属性值。
- **参数**:
    - `attr`: 属性名。
- **返回**: 属性值。

- **方法签名**: `public Date getDate(String attr)`
- **描述**: 获取日期属性值。
- **参数**:
    - `attr`: 属性名。
- **返回**: 属性值。

- **方法签名**: `public LocalDateTime getLocalDateTime(String datetime)`
- **描述**: 获取本地日期时间属性值。
- **参数**:
    - `datetime`: 属性名。
- **返回**: 属性值。

## 10. 服务获取
- **方法签名**: `public <S extends SdkService> S initService(Class<S> serviceClass)`
- **描述**: 获取单例服务。
- **参数**:
    - `serviceClass`: 服务类。
- **返回**: 服务实例。

- **方法签名**: `public <S extends SdkService> S initNewService(Class<S> serviceClass)`
- **描述**: 获取新的服务实例。
- **参数**:
    - `serviceClass`: 服务类。
- **返回**: 服务实例。

---

以上文档提供了`BaseModel`类的主要方法和功能描述，包括CRUD操作、数据访问、事务控制、SQL执行、异步操作以及属性获取等。
