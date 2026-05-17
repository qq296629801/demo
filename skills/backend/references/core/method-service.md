# @MethodService 服务方法参考

本文件覆盖常规 `@MethodService` 写法。涉及 JSON-RPC 入参/响应、Filter 波兰表达式、服务编排、异步线程调用模型、原生 SQL 参数化与缓存失效时，继续读取 `api-filter-sql.md`。

## 注解属性说明

| 属性 | 说明 |
|---|---|
| `name` | 服务名，默认等于 Java 方法名。远程接口和前端视图 `service` 参数使用服务名 |
| `description` | 服务描述，用于说明/API 文档；普通业务可写中文描述 |
| `auth` | 服务权限码 |
| `hiddenApi` | 是否在 API 文档中隐藏，内部插槽/非对外服务可设为 `true` |
| `doc` | Markdown 文档地址，可写相对路径或 HTTP 地址 |
| `principal` / `introduction` | API 文档负责人和说明 |

注意：`RecordSet.call("xxx")`、`callSuper(..., "xxx", ...)` 传的是方法名；前端接口 `service: "xxx"` 传的是服务名。未显式配置 `name` 时二者通常一致。重写内置服务时，Java 方法名保持 `create`、`update`、`delete` 等内置方法名。

---

## 重写内置 CRUD 方法

内置 description 值：`create`、`update`、`delete`、`find`、`search`、`count`

### 重写 create（新增前自定义校验/生成编码）

```java
@MethodService(description = "create")
public Object create(List<Map<String, Object>> valuesList) {
    RecordSet rs = BaseContextHandler.getMeta().get(this.getMeta().getModelName());
    try {
        String code = CodeGenTempUtil.genOneCode(ORDER_TYPE);
        if (CollUtil.isNotEmpty(valuesList)) {
            valuesList.forEach(v -> v.put("code", code));
        }
        return rs.callSuper(BooksManage.class, MethodConst.CREATE, valuesList);
    } catch (ModelException | ValidationException e) {
        throw e;
    } catch (Exception e) {
        throw new ValidationException("创建失败，请联系运维人员");
    }
}
```

### 重写 update（更新前调用 RPC）

```java
@MethodService(description = "update")
public Object update(RecordSet rs, Map<String, Object> values) {
    RecordSet modelRs = BaseContextHandler.getMeta().get(this.getMeta().getModelName());
    try {
        getMeta().get("books_reader").call("syncByBookId", values.get("id").toString());
        return modelRs.callSuper(BooksManage.class, MethodConst.UPDATE, rs, values);
    } catch (ModelException e) {
        throw e;
    } catch (Exception e) {
        throw new ValidationException("更新失败，请联系运维人员");
    }
}
```

---

## 自定义业务方法

### 启用/禁用（批量更新状态）

```java
@MethodService(description = "启用禁用")
public boolean enableDisable(RecordSet rs, String statusFlag) {
    String[] ids = rs.getIds();
    RecordSet recordSet = (RecordSet) rs.callSuper(null, MethodConst.FIND,
        Filter.in("id", Arrays.asList(ids)), null, null, null);
    if (recordSet.any()) {
        Map<String, Object> valMap = new HashMap<>();
        valMap.put("isEnable", statusFlag);
        recordSet.callSuper(null, MethodConst.UPDATE, valMap);
    }
    return true;
}
```

对应视图按钮：
```json
{
  "name": "启用", "action": "enable", "auth": "enable",
  "model": "books_manage", "service": "enableDisable",
  "actionAfter": "refreshTable",
  "args": { "bind_ids": "$ds.checkedDataIds", "statusFlag": "1" },
  "bind_disabled": "${$ds.checkedDataList.length === 0}"
}
```

若希望服务名与方法名不同，需要显式配置 `name`，并确保视图按钮的 `service` 使用服务名：

```java
@MethodService(name = "enableDisable", description = "启用禁用")
public boolean changeEnableStatus(RecordSet rs, String statusFlag) {
    // ...
    return true;
}
```

```json
{ "name": "启用", "action": "enable", "model": "books_manage", "service": "enableDisable" }
```

### 计算字段方法（配合 store=false）

```java
@Property(displayName = "是否超期", store = false, computeMethod = "isOverdue")
private Boolean isOverdue;

// 参数固定为 Map<String, Object>，接收当前记录所有字段值
@MethodService(description = "isOverdue")
public Boolean isOverdue(Map<String, Object> valMap) {
    Object returnDate = valMap.get("returnDate");
    if (ObjectUtil.isNull(returnDate)) return false;
    return DateUtil.date().after(DateUtil.parse(returnDate.toString()));
}
```

### 下拉选项提供方法（配合 @Selection(method="xxx")）

```java
@MethodService(description = "selectBook")
public List<Map<String, Object>> selectBook() {
    RecordSet bookRs = getMeta().get("books_manage");
    return bookRs.search(new Filter(), Collections.singletonList("*"), 0, 0, "");
}
```

---

## Excel 导出

### 方式一：调用平台 base_excel 服务

```java
@MethodService(description = "excelExport")
public void excelExport(RecordSet rs, Filter filter, Integer limit, Integer offset, String order) throws Exception {
    getMeta().addArgument(MetaConstant.USE_DISPLAY_FOR_MODEL, true);
    List<BooksManage> list = this.search(filter,
        Arrays.asList("bookName", "isbn", "author", "publishDate"), limit, offset, order);
    if (CollectionUtils.isEmpty(list)) {
        throw new Exception("没有符合数据");
    }
    Map<String, List<Map<String, Object>>> exportDataList = new LinkedHashMap<>();
    List<Map<String, Object>> result = new ArrayList<>();
    for (BooksManage book : list) {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("书名", book.getBookName());
        map.put("ISBN", book.getIsbn());
        map.put("作者", book.getAuthor());
        result.add(map);
    }
    exportDataList.put("图书列表", result);
    String fileName = "图书-" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + ".xlsx";
    rs.getMeta().get("base_excel").call("fileExport", exportDataList, fileName);
}
```

---

## Excel 导入

### 方式一：传入 fileId 解析

```java
@MethodService(description = "excelImport")
public boolean excelImport(RecordSet rs, String fileId) throws Exception {
    Map<String, List<Map<String, Object>>> fileMap =
        (Map<String, List<Map<String, Object>>>) rs.getMeta().get("base_excel").call("fileImport", fileId);
    List<Map<String, Object>> rawDataList = fileMap.get("Sheet1");
    if (CollUtil.isNotEmpty(rawDataList)) {
        BaseContextHandler.getMeta().get(this.getModelName()).create(rawDataList);
    }
    return true;
}
```

### 方式二：前端预校验 + 导入（固定方法名 verification）

```java
// 校验方法（固定使用 name = "verification"）
@MethodService(name = "verification")
public Map<String, Map<String, List<Map<String, Object>>>> verification(
        Map<String, List<Map<String, Object>>> data) throws ValidationException {
    if (data.isEmpty()) throw new ValidationException("没有任何可导入数据");
    List<Map<String, Object>> sheet1 = data.get("Sheet1");
    List<Map<String, Object>> dataList = new ArrayList<>();
    for (Map<String, Object> rowData : sheet1) {
        String bookName = (String) rowData.get("书名");
        BooksManage book = new BooksManage();
        if (StringUtils.isBlank(bookName)) {
            book.put(ExcelConstant.EXCEL_IMPORT_ERROR_MSG, "书名不能为空");
        }
        book.setBookName(bookName);
        dataList.add(book);
    }
    Map<String, List<Map<String, Object>>> sheet1Map = new HashMap<>();
    sheet1Map.put(ExcelConstant.EXCEL_IMPORT_HEADER_LIST, getHeader());
    sheet1Map.put(ExcelConstant.EXCEL_IMPORT_DATA_LIST, dataList);
    Map<String, Map<String, List<Map<String, Object>>>> result = new HashMap<>();
    result.put("Sheet1", sheet1Map);
    return result;
}
```

---

## 跨模型 RPC 调用

```java
// 被调用方（提供数据）
@MethodService(description = "searchBooks")
public List<BooksManage> searchBooks(Integer limit, Integer offset) {
    return DbUtils.search(new Filter(), Arrays.asList("id", "bookName"),
        limit, offset, "id", BooksManage.class);
}

// 调用方（同 App）
@MethodService(description = "syncBooksToReader")
public Boolean syncBooksToReader(RecordSet rs) {
    RecordSet readerRs = getMeta().get("books_reader");
    String[] ids = rs.getIds();
    // ... 业务逻辑
    readerRs.call("saveReaderByBooks", someList);
    return true;
}

// 跨 App 调用
@MethodService(description = "syncToExampleApp")
public Boolean syncToExampleApp(RecordSet rs) throws Exception {
    RecordSet targetRs = getMeta().get("example_student");
    try {
        targetRs.call("saveByBookId", rs.getIds());
    } catch (ModelException me) {
        throw me;
    } catch (Exception e) {
        throw new Exception("同步失败，请联系管理员");
    }
    return true;
}
```

---

## DTO 入参出参与 Service 分层

复杂二开场景不要把所有逻辑堆在模型类里。参考 `iidp-backend-demo` 的二开能力示例，可将请求 DTO 放在 `model/request`，响应 DTO 放在 `model/response`，业务逻辑放在 `service`。

模型层只声明服务入口：

```java
@InjectMeta
private BooksManageService service = this.initService(BooksManageService.class);

@MethodService(description = "新增-入参强类型", doc = "./doc/二开能力专题.md")
public boolean create(List<BooksManageAddReq> valuesList) {
    return service.create(valuesList);
}

@MethodService(description = "修改-入参出参强类型", hiddenApi = false)
public BooksManageRes update(BooksManageUpdateReq values) {
    return service.update(values);
}
```

Service 层继承 `SdkService<T>`：

```java
@Service
public class BooksManageService extends SdkService<BooksManage> {
    public boolean create(List<BooksManageAddReq> valueList) throws SdkAppException {
        super.batchCreate(valueList);
        return true;
    }

    public BooksManageRes update(BooksManageUpdateReq values) throws SdkAppException {
        super.update(values);
        List<BooksManage> list = super.search(
            Filter.equal("id", values.getId()),
            Arrays.asList("id", "bookName"),
            1,
            0,
            "id desc"
        );
        return CollectionUtils.isEmpty(list) ? null : super.copyToRes(list.get(0), BooksManageRes.class);
    }
}
```

适用场景：
- 方法入参/出参需要强类型和接口文档。
- 业务流程有多个步骤、会被多个服务复用。
- 需要隐藏内部服务或给公开服务挂 `doc` 文档说明。

---

## 直接执行 SQL

```java
@MethodService(description = "customSql")
public List<Map<String, Object>> customSql(RecordSet recordSet) {
    BussModelDataAccess bussModelDataAccess =
        (BussModelDataAccess) ModelDataAccessFactory.getDataAccess(ModelTypeEnum.Buss);
    RelationDBAccessor dataAccessor = bussModelDataAccess.getRelationDBAccessor();
    SqlProvider sqlProvider = dataAccessor.getSqlProvider();

    // 简单查询
    dataAccessor.execute("SELECT * FROM books_manage WHERE is_deleted = 0");
    List<Map<String, Object>> result = dataAccessor.fetchMapAll();

    // 带参数查询（%s 占位符）
    dataAccessor.execute("SELECT * FROM books_manage WHERE book_name=%s AND status=%s",
        Arrays.asList("Java 编程", "1"));

    // IN 查询（两层 List）
    dataAccessor.execute("SELECT * FROM books_manage WHERE id IN %s",
        Arrays.asList(Arrays.asList("id1", "id2")));

    return result;
}
```

SqlProvider 常用方法：
- `sqlProvider.quote("fieldName")` → 加引号包裹字段名/表名
- `sqlProvider.quoteValue("value")` → 加引号包裹字段值
- `sqlProvider.asQuote("alias")` → 设置别名
- `sqlProvider.getPaging(sql, limit, offset)` → 添加分页

---

## 事务控制

### 自动提交（推荐）

```java
@MethodService(description = "batchCreate")
public boolean batchCreate(List<Map<String, Object>> valuesList) {
    // 正常执行，引擎在请求结束时统一提交
    // 抛出 ModelException 自动回滚
    BaseContextHandler.getMeta().get(this.getModelName()).create(valuesList);
    if (someError) {
        throw new ModelException("发生错误，触发回滚");
    }
    return true;
}
```

### 手动 flush + commit

```java
@MethodService(description = "splitCommit")
public boolean splitCommit(RecordSet rs) {
    try (Meta meta = new Meta(null, new HashMap<>())) {
        // 第一批
        someRecord.update();
        meta.flush();
        meta.commit();
        // 第二批
        newRecord.create();
        meta.flush();
        meta.commit();
    } finally {
        BaseContextHandler.remove();
    }
    return true;
}
```

---

## Redis 缓存与分布式锁

```java
// 分布式锁
@MethodService(description = "lockAndProcess")
public String lockAndProcess(RecordSet recordSet) {
    String key = "lock:" + recordSet.getId();
    Boolean isLocked = false;
    try {
        isLocked = RedisHelper.tryLock(key, 2L, 30L); // waitSeconds, expireSeconds
        if (!isLocked) {
            throw new ValidationException("资源处理中，请稍后重试");
        }
        // 业务逻辑...
        return "操作成功";
    } finally {
        if (isLocked) RedisHelper.unlock(key);
    }
}

// 缓存存取
@MethodService(description = "cacheExample")
public String cacheExample(RecordSet recordSet) {
    String key = "cache:" + recordSet.getId();
    RedisHelper.set(key, "缓存值");
    return (String) RedisHelper.get(key);
}
```

---

## 常用 API 速查

### RecordSet 方法

| 方法 | 说明 |
|---|---|
| `rs.getIds()` | 获取所有记录 ID 数组 |
| `rs.getId()` | 获取第一条记录 ID |
| `rs.any()` | 是否包含数据 |
| `rs.getData()` | 获取原始 `List<Map<String,Object>>` |
| `rs.callSuper(clazz, method, args...)` | 调用平台内置方法 |
| `rs.call(method, args...)` | 调用当前模型另一个服务方法 |
| `rs.getMeta()` | 获取当前请求上下文 Meta |

### callSuper 签名

```java
rs.callSuper(ModelClass.class, MethodConst.CREATE, valuesList);
rs.callSuper(ModelClass.class, MethodConst.UPDATE, rs, valMap);
rs.callSuper(null, MethodConst.FIND, filter, null, null, null);
rs.callSuper(null, MethodConst.DELETE);
```

### Filter 常用构建

```java
Filter.equal("fieldName", value)
Filter.in("fieldName", list)
Filter.like("fieldName", "keyword")
Filter.gt("fieldName", value)
Filter.lt("fieldName", value)
new Filter()                         // 空条件（查全部）
filter.and(Filter.equal(...))        // 组合多条件 AND
```
