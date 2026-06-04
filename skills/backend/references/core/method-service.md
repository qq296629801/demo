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

### 日期格式流水号生成（如 ASN-YYYYMMDD-XXXXX）

需求文档中常见"单据编号自动生成"要求（格式：`前缀-YYYYMMDD-5位序号`）。推荐在重写 `create` 时生成：

```java
@MethodService(description = "create")
public Object create(List<Map<String, Object>> valuesList) {
    RecordSet rs = BaseContextHandler.getMeta().get(this.getMeta().getModelName());
    try {
        // 方式一：使用平台 CodeGenTempUtil（ORDER_TYPE 为业务类型标识符常量）
        // String asnNo = CodeGenTempUtil.genOneCode(ORDER_TYPE);

        // 方式二：自行拼接日期前缀 + 毫秒/雪花 ID 保证唯一
        String dateStr = DateUtil.format(DateUtil.date(), "yyyyMMdd");
        String seq = String.format("%05d", System.currentTimeMillis() % 100000);
        String asnNo = "ASN-" + dateStr + "-" + seq;

        if (CollUtil.isNotEmpty(valuesList)) {
            valuesList.forEach(v -> {
                v.putIfAbsent("asn_no", asnNo);
                v.putIfAbsent("status", 0); // 初始状态：待收货
            });
        }
        return rs.callSuper(WmsAsnMaster.class, MethodConst.CREATE, valuesList);
    } catch (ModelException | ValidationException e) {
        throw e;
    } catch (Exception e) {
        throw new ValidationException("创建入库单失败，请联系运维人员");
    }
}
```

> **注意**：方式二在高并发场景下可能重复（毫秒级冲突），生产环境建议使用 `CodeGenTempUtil.genOneCode()` 或 Redis 原子自增。`putIfAbsent` 防止前端传入值被覆盖。

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

### 重写 delete（删除前校验）

```java
@MethodService(description = "delete")
public Object delete(RecordSet rs) {
    String[] ids = rs.getIds();
    // 查出待删除数据，校验是否可删
    RecordSet records = (RecordSet) rs.callSuper(null, MethodConst.FIND,
        Filter.in("id", Arrays.asList(ids)), null, null, null);
    if (records.any()) {
        // 业务前置校验：如状态检查
        records.getData().forEach(row -> {
            if ("LOCKED".equals(row.get("status"))) {
                throw new ValidationException("已锁定的记录不允许删除");
            }
        });
    }
    return rs.callSuper(null, MethodConst.DELETE);
}
```

### 重写 search / count（前置过滤）

当模型需要按固定条件隔离数据时（如同一张表区分不同业务类型），在 `search` 和 `count` 中统一注入过滤条件：

```java
private static final String CATE_TYPE_ID = "enterprise";

@Override
public List<ExampleOrgLevel> search(Filter filter, List<String> properties,
                                    Integer limit, Integer offset, String order) {
    // 前置注入固定过滤条件，保证所有查询只返回当前业务类型数据
    filter.and(Filter.equal(ExampleOrgLevel.F_CATE_TYPE, CATE_TYPE_ID));
    return super.search(filter, properties, limit, offset, order);
}

@Override
public long count(Filter filter) {
    filter.and(Filter.equal(ExampleOrgLevel.F_CATE_TYPE, CATE_TYPE_ID));
    return super.count(filter);
}

public Object create(List<Map<String, Object>> valuesList) {
    Meta meta = this.getMeta();
    RecordSet rs = meta.get(this.getModelName());
    // 新增时自动补充固定字段，避免前端遗漏
    valuesList.forEach(values -> values.put(ExampleOrgLevel.F_CATE_TYPE, CATE_TYPE_ID));
    // 执行业务前置校验后调父类实现
    OrgLevelUtils.createCheck(this, valuesList);
    return rs.callSuper(ExampleOrgLevel.class, "create", valuesList);
}
```

> **规则**：`search`/`count`/`create` 三者必须同步维护同一个固定条件，否则统计数和列表数会不一致。

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

**方式一：返回 `List<Map<String, Object>>`（查数据库）**

```java
@MethodService(description = "selectBook")
public List<Map<String, Object>> selectBook() {
    RecordSet bookRs = getMeta().get("books_manage");
    return bookRs.search(new Filter(), Collections.singletonList("*"), 0, 0, "");
}
```

**方式二：返回 `List<Options>`（来自枚举或常量，来自工程 ExampleOrgLevel）**

`Options.of(label, value)` 构建选项，参数 `value` 为 `Object`（接收当前字段值，可为 `null`）：

```java
@MethodService(description = "分类列表")
public List<Options> orgCategoryList(Object value) {
    List<Options> options = new ArrayList<>();
    for (OrgCategory cate : OrgCategory.values()) {
        options.add(Options.of(cate.getDesc(), String.valueOf(cate.getId())));
    }
    // 若 value 非空，只返回匹配当前值的选项（编辑回显场景）
    if (value != null && StringUtils.isNotBlank(value.toString())) {
        return options.stream()
            .filter(o -> o.getValue().equals(value.toString()))
            .collect(Collectors.toList());
    }
    return options;
}
```

> `Options` 来自 `com.sie.snest.engine.utils.Options`。下拉方法的 `Object value` 参数为前端传来的当前字段值，首次打开下拉时为 `null`，编辑回显时为字段存储值。

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
        // 第一批：更新后单独提交
        someRecord.update();
        meta.flush();
        meta.commit();
        // 第二批：新增后单独提交
        newRecord.create();
        meta.flush();
        meta.commit();
    } finally {
        BaseContextHandler.remove();
    }
    return true;
}
```

### Savepoint 局部回滚（嵌套事务）

当需要对某一段操作回滚、同时保留其他操作时，使用 `setSavepoint` + `rollback(point)`：

```java
@MethodService(description = "savepointDemo")
public boolean savepointDemo(RecordSet rs) {
    try (Meta meta = new Meta(null, new HashMap<>())) {
        // 第一段操作：更新——无论后续是否失败都保留
        existingStudent.set(ExampleStudent.F_STUDENT_LEVEL, "2");
        existingStudent.update();

        // 新建事务保存点
        // Oracle 的保存点名称有字符限制，建议使用 "SP" + 数字 ID
        // MySQL 可以使用 UUID
        String point1 = "SP" + IdGenerator.nextId();

        try {
            meta.setSavepoint(point1);          // 标记保存点
            newStudent.create();                 // 第二段操作：新增
            int i = 1 / 0;                       // 模拟异常
            meta.commit();                       // 成功时提交（含保存点之前的操作）
        } catch (Exception e) {
            meta.rollback(point1);               // 仅回滚保存点之后的操作，第一段更新不受影响
        }
    } finally {
        BaseContextHandler.remove();
    }
    return true;
}
```

> **注意**：
> - `meta.rollback(point)` 只回滚该保存点之后的操作，保存点之前的写入不受影响。
> - Oracle 保存点名称不能含 `-`，推荐 `"SP" + IdGenerator.nextId()`；MySQL 无此限制。
> - 若不调用 `meta.commit()`，请求结束时平台会统一提交所有未回滚的操作。

### 过账模式（多模型事务）

过账、审批提交、确认生效等场景涉及多个模型的联动写操作，必须在一个请求内完成。引擎在请求结束时统一提交，抛 `ModelException` 自动回滚。模板如下：

```java
@MethodService(description = "过账出库单")
public boolean postOutbound(RecordSet rs, String orderId) {
    // 1. 校验参数
    if (orderId == null || orderId.isEmpty()) {
        throw new ValidationException("单据ID不能为空");
    }

    // 2. 查询主单据
    Map<String, Object> order = get(orderId);
    if (order == null) {
        throw new ValidationException("单据不存在");
    }
    String status = MapUtils.getString(order, "status", "");
    if ("POSTED".equals(status)) {
        throw new ValidationException("单据已过账，不可重复操作");
    }

    // 3. 查询明细
    List<Map<String, Object>> items = getItems(orderId);

    // 4. 逐行校验库存（所有校验通过后才写库，避免部分写）
    for (Map<String, Object> item : items) {
        String productId = MapUtils.getString(item, "productId");
        int qty = MapUtils.getIntValue(item, "quantity", 0);
        if (qty <= 0) {
            throw new ValidationException("明细数量必须大于0");
        }
        // 查询当前库存
        Map<String, Object> stock = queryOne("SELECT * FROM wms_stock WHERE product_id = ?", productId);
        int currentQty = stock == null ? 0 : MapUtils.getIntValue(stock, "quantity", 0);
        if (currentQty < qty) {
            throw new ModelException("产品 [" + productId + "] 库存不足，当前库存: " + currentQty);
        }
    }

    // 5. 批量写操作（校验通过 → 一次性执行所有写操作）
    // 平台自动事务：任何一步失败，全部回滚
    for (Map<String, Object> item : items) {
        String productId = MapUtils.getString(item, "productId");
        int qty = MapUtils.getIntValue(item, "quantity", 0);

        // 5a. 扣减库存
        DbUtils.update("UPDATE wms_stock SET quantity = quantity - ? WHERE product_id = ?", qty, productId);

        // 5b. 写库存流水（审计追溯）
        Map<String, Object> ledger = new HashMap<>();
        ledger.put("productId", productId);
        ledger.put("warehouseId", MapUtils.getString(order, "warehouseId"));
        ledger.put("transactionType", "OUTBOUND");
        ledger.put("orderId", orderId);
        ledger.put("orderNo", MapUtils.getString(order, "orderNo"));
        ledger.put("quantity", qty);
        ledger.put("balanceAfter", currentQty - qty); // 之前查到的 currentQty
        ledger.put("transactionDate", new Date());
        ledger.put("createdBy", BaseContextHandler.get().getUid());
        getMeta("wms_stock_ledger").create(ledger);
    }

    // 6. 更新单据状态
    Map<String, Object> updateOrder = new HashMap<>();
    updateOrder.put("id", orderId);
    updateOrder.put("status", "POSTED");
    updateOrder.put("postedBy", BaseContextHandler.get().getUid());
    updateOrder.put("postedAt", new Date());
    setValues(updateOrder);
    update();

    return true;
    // 引擎请求结束统一提交；任何一步抛 ModelException 则全部回滚
}
```

> **过账模式要点**：
> - **先校验后写库**：所有业务校验（库存、状态、权限）完成后再一次性执行写操作，避免写到一半才发现不满足条件。
> - **库存扣减用 SQL**：`UPDATE ... SET quantity = quantity - ?` 避免并发覆盖，优于先查再 set。
> - **流水必须写全**：stock_ledger 必须包含变动前余额和变动后余额，不可只写变动量。
> - **状态前置检查**：过账前必须检查 `status != POSTED`，防止重复过账。
> - **使用 ModelException**：业务异常抛 `ModelException`（触发回滚），参数校验抛 `ValidationException`（轻量提示）。
> - **禁止补丁式修复**：不要在过账后才发现问题、再写一个"反过账"方法来补救——所有验证在过账前完成。

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
