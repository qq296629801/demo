---
title: RecordSet接口文档
date: 2024-12-27 14:28:39
---
# RecordSet接口文档

RecordSet 是一个数据库记录集操作类，支持获取模型、数据库的CRUD操作、查询、统计、异步处理和属性访问，提供灵活的数据管理功能。

## 1. 构造函数

- **方法签名**: `public RecordSet(Meta meta, ModelMeta model, String[] ids, Supplier<String[]> prefetchIds)`
- **描述**: 构造一个新的RecordSet实例。
- **参数**:
    - `meta`: 元数据容器。
    - `model`: 模型元数据。
    - `ids`: 记录ID数组。
    - `prefetchIds`: 预取ID供应商。

## 2. 记录集检查
- **方法签名**: `public boolean any()`
- **描述**: 检查记录集是否包含记录。

- **方法签名**: `public String[] getIds()`
- **描述**: 获取记录集的所有ID。

- **方法签名**: `public Supplier<String[]> getPrefetchIds()`
- **描述**: 获取预取ID供应商。

## 3. 记录集操作
- **方法签名**: `public int size()`
- **描述**: 获取记录集的大小。

- **方法签名**: `public String getId()`
- **描述**: 获取记录集的单一ID，如果记录集不包含恰好一条记录，则返回null。

## 4. 元数据访问
- **方法签名**: `@JsonIgnore public Meta getMeta()`
- **描述**: 获取Meta对象。

- **方法签名**: `@JsonIgnore public ModelMeta getModel()`
- **描述**: 获取模型元数据。

## 5. 浏览记录
- **方法签名**: `public RecordSet browse(Collection<String> ids)`
- **描述**: 浏览指定ID集合的记录。

- **方法签名**: `public RecordSet browse(String... ids)`
- **描述**: 浏览指定ID的记录。

## 6. 预取设置
- **方法签名**: `public RecordSet withPrefetch(Supplier<String[]> prefetchIds)`
- **描述**: 设置预取ID供应商。

## 7. 记录操作
- **方法签名**: `public void ensureOne()`
- **描述**: 确保记录集只包含一条记录。

- **方法签名**: `public RecordSet first()`
- **描述**: 获取记录集的第一记录。

- **方法签名**: `public RecordSet firstOrDefault()`
- **描述**: 获取记录集的第一记录，如果为空则返回当前记录集。

## 8. 查询与统计
- **方法签名**: `public RecordSet find(Filter filter, Integer limit, Integer offset, String order)`
- **描述**: 根据查询标准加载数据集。

- **方法签名**: `public long count(Filter filter)`
- **描述**: 统计记录数量。

- **方法签名**: `public List<Map<String, Object>> search(Filter filter, List<String> properties, Integer limit, Integer offset, String order)`
- **描述**: 根据查询标准读取指定属性。

## 9. 读写操作
- **方法签名**: `public List<Map<String, Object>> read(List<String> properties)`
- **描述**: 读取记录集的指定属性。

- **方法签名**: `public RecordSet create(List<Map<String, Object>> valuesList)`
- **描述**: 批量创建记录。

- **方法签名**: `public RecordSet create(Map<String, Object> values)`
- **描述**: 创建一条记录。

## 10. 存在性检查
- **方法签名**: `public RecordSet exists()`
- **描述**: 返回已保存到数据库中的记录集。

## 11. 更新与删除
- **方法签名**: `public void update(Map<String, Object> values)`
- **描述**: 更新所有记录的值。

- **方法签名**: `public void batchUpdate(List<Map<String, Object>> valuesList)`
- **描述**: 批量更新多条记录。

- **方法签名**: `public void delete()`
- **描述**: 删除记录集。

- **方法签名**: `public void hardDelete()`
- **描述**: 物理删除记录集。

## 12. 异步执行
- **方法签名**: `public <T> CompletableFuture<T> executeAsync(Supplier<T> supplier, String method)`
- **描述**: 异步执行lambda表达式。

- **方法签名**: `public CompletableFuture<Object> callAsync(@ModelMethod String method, Object... args)`
- **描述**: 异步调用方法。

## 13. 调用与属性访问
- **方法签名**: `public Object call(@ModelMethod String method, Object... args)`
- **描述**: 调用方法或服务。

- **方法签名**: `public Object get(String propertyName)`
- **描述**: 获取属性值。

- **方法签名**: `public void set(String propertyName, Object value)`
- **描述**: 设置属性值。

## 14. 迭代器
- **方法签名**: `@Override public Iterator<RecordSet> iterator()`
- **描述**: 返回记录集的迭代器。

---
