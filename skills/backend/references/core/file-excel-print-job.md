# 文件、Excel、打印与任务能力

本文件覆盖 Upload、Image、文件预览、Excel 导入导出、打印节点、进度条轮询、XXL-Job 等后端能力。

## 文件与上传

后端依赖：

- `apps/apps.json` 中加载 `sie-snest-file-*.jar`。
- 环境配置 MinIO：`endpoint/accessKey/secretKey/bucketName`。
- 模型保存文件标识字段，如 `fileId/fileName/fileUrl/contentType`。

字段示例：

```java
@Property(displayName = "附件ID", length = 64)
private String fileId;

@Property(displayName = "附件名称", length = 240)
private String fileName;

@Property(displayName = "附件类型", length = 64)
private String contentType;
```

### 文件模型完整示例（@Value 注入 + IdGenerator 生成文件ID）

来自工程 `ExampleFile`，展示如何在模型中注入 MinIO bucket 配置并生成文件对象：

```java
@Model(tableName = "example_file", name = "example_file",
       displayName = "文件管理", isLogicDelete = Bool.True)
public class ExampleFile extends BaseModel<ExampleFile> {

    @Property(displayName = "文件名")
    private String fileName;

    @Property(displayName = "文件地址")
    private String fileLocation;

    @Property(displayName = "文件Id")
    private String fileId;

    // 通过 @Value 注入 Spring 配置属性（docker/config/application.properties 中配置）
    @Value("minio.bucketName")
    private String minioBucket;

    // typed getter/setter：BaseModel 底层按 Map 存储，需显式实现
    public String getFileName()    { return getStr("fileName"); }
    public ExampleFile setFileName(String v) { this.set("fileName", v); return this; }
    public String getFileLocation()   { return getStr("fileLocation"); }
    public ExampleFile setFileLocation(String v) { this.set("fileLocation", v); return this; }
    public String getFileId()      { return getStr("fileId"); }
    public ExampleFile setFileId(String v) { this.set("fileId", v); return this; }

    public ExampleFile createFile() {
        Map<String, Object> arguments = getMeta().getArguments();
        ExampleFile exampleFile = new ExampleFile();
        // IdGenerator.nextId() 生成平台雪花 ID 作为文件唯一标识
        exampleFile.setId(IdGenerator.nextId());
        exampleFile.setFileName(String.valueOf(arguments.get("fileName")));
        // MinIO 存储路径 = bucketName + "/" + 业务路径
        exampleFile.setFileLocation(minioBucket + "/" + arguments.get("fileLocation"));
        exampleFile.setFileId(String.valueOf(arguments.get("fileId")));
        return exampleFile;
    }
}
```

> **规则**：
> - `@Value("minio.bucketName")` 注入配置属性，对应 `docker/config/application.properties` 中的 `minio.bucketName=xxx`。
> - `IdGenerator.nextId()` 返回平台雪花 ID（`String` 类型），用作文件记录主键或文件标识。
> - `getMeta().getArguments()` 获取调用方通过 `addArgument` 传入的上下文参数。
> - typed getter/setter 是 BaseModel 的强制要求：字段实际存储在 Map 中，直接访问 Java 字段会得到 `null`。

视图示例：

```json
{
  "name": "fileId",
  "displayName": "附件",
  "custom": true,
  "type": "upload",
  "fileLimit": {
    "ext": ".doc,.docx,.xls,.xlsx,.pdf,.png,.jpg",
    "maxSize": "2048"
  }
}
```

---

## 文件预览与图片

表格图片/文件预览：

```json
{
  "name": "fileUrl",
  "displayName": "图片",
  "custom": true,
  "type": "icon",
  "iconType": "url",
  "preview": true,
  "fn": "(row) => row.fileUrl"
}
```

规范：

- 后端返回可访问 URL 或文件 ID。
- 权限敏感文件不要直接返回永久公开 URL。
- 预览失败要能显示文件名和错误信息。

---

## Excel 导出

视图按钮：

```json
{
  "name": "导出",
  "action": "export",
  "model": "books_manage",
  "service": "excelExport",
  "auth": "export",
  "customExport": true
}
```

服务方法：

```java
@MethodService(description = "excel导出")
public void excelExport(RecordSet rs, Filter filter, Integer limit, Integer offset, String order) throws Exception {
    getMeta().addArgument(MetaConstant.USE_DISPLAY_FOR_MODEL, true);
    List<BooksManage> list = this.search(filter, Arrays.asList("bookCode", "bookName"), limit, offset, order);
    if (CollectionUtils.isEmpty(list)) {
        throw new ValidationException("没有符合数据");
    }
    Map<String, List<Map<String, Object>>> exportData = new LinkedHashMap<>();
    List<Map<String, Object>> rows = new ArrayList<>();
    for (BooksManage item : list) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("图书编码", item.getBookCode());
        row.put("图书名称", item.getBookName());
        rows.add(row);
    }
    exportData.put("图书列表", rows);
    rs.getMeta().get("base_excel").call("fileExport", exportData, "图书列表.xlsx");
}
```

---

## Excel 导入

普通导入按钮：

```json
{
  "name": "导入",
  "action": "import",
  "model": "books_manage",
  "service": "excelImport",
  "auth": "import",
  "fileLimit": { "ext": ".xls,.xlsx", "maxSize": "2048" }
}
```

完整导入模板：

```json
{
  "name": "导入",
  "statement": "_fullImport",
  "modelName": "books_manage",
  "fileName": "@filePath(book_import_template)",
  "buttonList": [
    { "text": "下载模板", "action": "downloadTemplate", "icon": "el-icon-document" },
    { "text": "准确性校验", "action": "importValidate" },
    { "text": "强制导入", "action": "forceImport" },
    { "text": "确认导入", "action": "confirmImport" }
  ]
}
```

导入校验服务固定使用：

```java
@MethodService(name = "verification")
public Map<String, Map<String, List<Map<String, Object>>>> verification(
        Map<String, List<Map<String, Object>>> data) throws ValidationException {
    if (data.isEmpty()) {
        throw new ValidationException("没有任何可导入数据");
    }
    // 返回 header/data/error message
    return new HashMap<>();
}
```

规范：

- 必填、长度、唯一性在导入校验中提前提示。
- 错误行写入错误信息字段，不静默丢弃。
- 强制导入仍要保证数据库约束和业务状态安全。

---

## 静态模板与下载

静态模板放在 App 资源路径下，并确保 Maven 打包：

```json
{
  "filename": "/apps/2023-10-30/图书导入模板.xlsx",
  "auth": "staticDownload",
  "name": "下载模板",
  "action": "staticDownload",
  "downloadName": "图书导入模板.xlsx"
}
```

动态模板：

```json
{
  "auth": "methodDownload",
  "name": "下载模板",
  "action": "methodDownload",
  "model": "books_manage",
  "service": "downloadTemplate"
}
```

---

## 打印

模型支持打印：

```java
@Model(name = "books_borrow", displayName = "借阅单", isPrint = Bool.True)
public class BooksBorrow extends BaseModel<BooksBorrow> {
}
```

若业务需要自定义打印数据，实现打印服务或平台钩子，返回打印模板所需结构。打印按钮使用自定义 `service/auth/action`，并保证选中行唯一或批量逻辑明确。

---

## 进度与轮询

Progress 进度条轮询后端时，后端服务应返回稳定结构：

```java
@MethodService(description = "查询导入进度")
public Map<String, Object> queryImportProgress(String taskId) {
    Map<String, Object> result = new HashMap<>();
    result.put("percent", 60);
    result.put("status", "RUNNING");
    result.put("message", "正在处理");
    return result;
}
```

规范：

- 长任务必须有 taskId。
- 查询接口幂等。
- 状态值使用枚举或常量。

---

## XXL-Job

需要定时任务时，优先使用已有 `sie-iidp-demo-xxljob-executor` 模块。

规范：

- XXL-Job 模块 Java 17，普通业务 App 不跟随升级。
- JobHandler 名称唯一。
- 任务参数校验清楚。
- 任务可重入或有分布式锁。
- 执行日志包含任务 ID、批次号、处理数量。

---

## 生成检查清单

- 文件能力确认 file jar、MinIO 配置、字段契约。
- 导入导出按钮与服务方法名称一致。
- 导入校验返回错误行信息。
- 静态模板资源能被打进 jar 或上传到约定路径。
- 打印模型开启 `isPrint` 或提供自定义打印服务。
- 长任务有进度查询和幂等处理。
