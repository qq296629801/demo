# 模型、属性、日期与视图模型高级参考

本文件补齐模型层高级能力。生成普通业务模型先读 `model.md`；涉及模型类型、模型继承/扩展、ER 指令集、属性高级参数、分组校验、Selection 联动、Dict 种子、日期范围或视图模型时，再读本文件。需要完整底稿时读取 `../complete/create-model.md`、`../complete/create-property.md`、`../complete/date-type.md` 或 `../complete/view-model.md`。

## 完整底稿覆盖

| 完整底稿 | 本文件覆盖点 |
|---|---|
| `../complete/create-model.md` | `@Model` 参数、模型类型、BaseModel 内置能力、继承/扩展/多继承、ER 指令集 |
| `../complete/create-property.md` | `@Property` 全量参数、类型映射、typed getter/setter、校验分组、Selection/Dict/related/compute/自动字段 |
| `../complete/date-type.md` | `DataType.DATE/DATE_TIME`、`dateFormat`、`getDate/getTimestamp`、搜索 widget 与范围限制 |
| `../complete/view-model.md` | `ModelType.View`、`@View.From`、`@View.MapProperty`、`@View.MapFunction`、聚合查询 |

---

## @Model 高级参数

| 参数 | 后端生成要求 |
|---|---|
| `name` | 模型唯一名；与视图 `model`、菜单 `model` 一致 |
| `type` | 默认业务模型；特殊模型按 `Buss/Data/Memory/Tree/Config/View` 选择 |
| `parent` | 字符串数组；继承、扩展、多继承都使用它表达 |
| `tableName` | 需要固定物理表名时配置 |
| `isAbstract` | 抽象模型不生成表，只做继承基类 |
| `orderBy` | 默认排序 SQL 片段，如 `id desc` |
| `files` | 模型关联配置文件；可不写后缀，由平台搜索配置文件 |
| `isAutoLog` | 自动维护创建/更新人和时间 |

模型类型：

| 类型 | 用途 |
|---|---|
| `Buss` | 默认业务模型，持久化到数据库，支持常规 ER 和字段类型 |
| `Data` | 瞬态数据模型，会话结束销毁，不适合大量数据 |
| `Memory` | 内存模型，可同步到数据库，查询以内存为主 |
| `Tree` | 树状模型，用于层级数据 |
| `Config` | 配置模型，可读取本地文件或远程配置中心 |
| `View` | 视图模型，不持久化，通过映射其他模型查询 |

---

## BaseModel 内置能力

所有继承 `BaseModel<T>` 的业务模型都有内置 CRUD 能力：

| 服务 | 典型参数 | 说明 |
|---|---|---|
| `create` | `valuesList` | 批量新增 |
| `delete` | `ids` 或当前 `RecordSet` | 删除当前集合 |
| `update` | `ids` + `values` | 更新当前集合 |
| `find` | `filter/limit/offset/order` | 查找一条或一组记录 |
| `search` | `filter/properties/limit/offset/order` | 查询列表 |
| `count` | `filter` | 统计条数 |
| `selectById` | `id` | 按主键查询 |

重写内置服务时，必须保留参数契约，并通过 `callSuper` 调平台默认实现，除非明确替换行为。

---

## 模型索引

> **强制规则**：`@Index` 只能作为 `@Model(indexes = {...})` 的参数使用，加在**类**上。禁止将 `@Index` 放在属性字段或方法上。

需要唯一约束、常用过滤字段或外键查询时，在 `@Model(indexes = {...})` 上声明索引：

```java
@Model(
    name = "books_borrow",
    indexes = {
        @Index(name = "UQ_BOOKS_BORROW_CODE", columnList = {"borrow_code"}, unique = true),
        @Index(name = "IDX_BOOKS_BORROW_READER_STATUS", columnList = {"reader_id", "status"})
    }
)
public class BooksBorrow extends BaseModel<BooksBorrow> {
}
```

生成规则：

- 唯一业务编码、租户内唯一编码等必须用唯一索引和服务校验双保险。
- `columnList` 使用数据库列名；若字段配置了 `columnName`，以 `columnName` 为准。
- ManyToOne 外键、树父字段、上下表子表外键、菜单过滤字段、`orderBy` 高频字段建议建索引。
- 索引名全局清晰，建议 `UQ_{MODEL}_{COL}` 或 `IDX_{MODEL}_{COL}`，不要复用旧示例名。
- 联合索引顺序按常见查询条件排列：等值条件在前，范围/排序字段在后。

---

## 表分片（isShard / @Shard）

`isShard = Bool.True` 启用分片，`@Shard` 配置分片策略。常用于按时间或枚举值水平拆表。

```java
// 按年分表（shardType = "tables"，shardPoint = "字段:策略"）
@Model(
    tableName = "example_student",
    name = "example_student",
    isShard = Bool.False,   // False = 声明了策略但当前不启用；True = 启用分片
    shard = @Shard(
        shardType = "tables",          // 按物理表分片
        shardPoint = "create_date:YEAR", // 按 create_date 字段的年份分表
        shardValues = "2021"           // 历史分片起始年
    )
)
public class ExampleStudent extends BaseModel<ExampleStudent> { ... }
```

```java
// 按枚举值分区（shardType = "partition"，shardPoint = "字段:LIST"）
@Model(
    tableName = "example_class",
    name = "example_class",
    isShard = Bool.False,
    shard = @Shard(
        shardType = "partition",            // 按分区键分区
        shardPoint = "class_name:LIST",     // 按 class_name 字段的值分区
        shardValues = "one;two;three"       // 分区枚举值（分号分隔）
    )
)
public class ExampleClass extends BaseModel<ExampleClass> { ... }
```

| 参数 | 说明 |
|---|---|
| `isShard` | `Bool.True` 启用分片；`Bool.False` 声明策略但不启用（常用于预配置） |
| `shardType` | `tables`：物理多表；`partition`：数据库分区 |
| `shardPoint` | `字段名:策略`，策略可选 `YEAR`（按年）、`LIST`（按枚举值） |
| `shardValues` | 历史分片值（`tables` 用逗号，`partition` 用分号） |

---

## 树形自关联（@ManyToOne 自引用 + @OneToMany children）

同一模型的记录存在父子层级时，使用自关联实现树形结构：

```java
@StaticVar @Getter @Setter
@Model(tableName = "example_org_level", name = "example_org_level",
       displayName = "企业层级", isLogicDelete = Bool.True, isAutoLog = Bool.True)
public class ExampleOrgLevel extends BaseModel<ExampleOrgLevel> {

    @Property(displayName = "上级ID", columnName = "hl_org_cate_id", length = 64)
    private String hlOrgCateId;

    // 计算字段：带出上级名称，store=false 不持久化
    @Property(displayName = "上级名称", store = false, computeMethod = "obtainHlOrgCateName")
    private String hlOrgCateName;

    // 自关联：当前记录的父节点
    @ManyToOne(displayName = "上级类型")
    @JoinColumn(name = "hl_org_cate_id", referencedProperty = "treeOrgCateId")
    private ExampleOrgLevel hlOrgCateIdp;

    // 自关联：当前记录的子节点列表
    @OneToMany
    private List<ExampleOrgLevel> children;

    // 计算字段方法
    public String obtainHlOrgCateName(Map<String, Object> data) {
        Object hlId = data.get(F_HL_ORG_CATE_ID);
        if (hlId == null) return null;
        // 通过 RPC 查父节点名称，或在内存中 Map 缓存
        return OrgCategoryUtils.convertCateName(data, F_HL_ORG_CATE_ID);
    }
}
```

> **规则**：
> - `@JoinColumn(name = "外键列名", referencedProperty = "父节点唯一业务键")`，`referencedProperty` 通常指向父模型的 `id` 或业务唯一键。
> - `@OneToMany private List<T> children` 字段名必须为 `children`，视图树配置依赖此约定。
> - 树形视图需要在视图 JSON 中配置 `type: "tree"`，`props.children` 指向 `children` 字段。

---

## 继承、扩展与多继承

| 场景 | 写法 | 含义 |
|---|---|---|
| 继承 | `@Model(name = "child_model", parent = {"base_model"})` | 子模型拥有父模型字段和服务 |
| 扩展 | `@Model(name = "base_model", parent = {"base_model"})` | 给已有模型增加字段或重写服务 |
| 多继承 | `parent = {"base_a", "base_b"}` | 同时继承多个父模型 |

规则：

- 多父模型必须用数组，不写成逗号拼接字符串。
- 扩展已有模型时，`name` 与 `parent` 指向同一模型名。
- 继承不需要复制父模型字段；只写新增字段和需要重写的服务。
- 如果移除父模型服务，需按平台服务移除能力配置，不能靠重名空方法遮蔽。

---

## ER 指令集

OneToMany 和 ManyToMany 在 API 保存时可使用三元组指令。生成服务或种子数据时要识别这些命令。

ManyToMany：

| 指令 | 含义 |
|---|---|
| `(0, 0, values)` | 新建从记录并建立关系 |
| `(1, id, values)` | 更新指定从记录 |
| `(2, id)` | 删除从记录并删除关系 |
| `(3, id)` | 只断开关系，不删除记录 |
| `(4, id)` | 建立与已有记录的关系 |
| `(5)` | 清空所有关系 |
| `(6, 0, ids)` | 用 ids 替换原关系 |

OneToMany：

| 指令 | 含义 |
|---|---|
| `(0, 0, values)` | 新建子记录 |
| `(1, id, values)` | 更新子记录 |
| `(2, id)` | 删除子记录和主从关系 |

后端服务要求：

- `create` 通常只接受 `(0, 0, values)` 类新增指令。
- `update` 可处理 `(1/id)`、`(2/id)`、`(4/id)`、`(6/ids)` 等关系维护。
- 批量关系变更必须检查主记录权限和子记录存在性。
- `@eval` 种子关系中的 `[4, @ref(x), 0]` 表示关联已有记录。

### OneToMany 指令集 JSON-RPC 示例

> **id 类型**：IIDP 所有模型主键均为 String 类型（雪花算法转字符串），JSON-RPC 中传 id 时必须使用字符串形式，如 `"id": "abc123"`，不得使用整数。

以 `ExampleItem`（主）→ `ExampleItemCate`（子，`@OneToMany itemCateList`）为例：

**新建主记录同时新建子记录 `(0, 0, values)`**

```json
{
  "model": "example_item",
  "service": "create",
  "args": [{
    "itemCode": "ITEM-001",
    "itemName": "测试物料",
    "isEnable": "1",
    "itemCateList": [
      [0, 0, {"cateCode": "C001", "cateName": "分类A", "cateType": "type1"}],
      [0, 0, {"cateCode": "C002", "cateName": "分类B", "cateType": "type2"}]
    ]
  }]
}
```

**更新主记录时同时更新/新增/删除子记录**

```json
{
  "model": "example_item",
  "service": "update",
  "args": {
    "id": "abc100",
    "itemName": "更新后物料名",
    "itemCateList": [
      [1, "abc201", {"cateName": "分类A-修改"}],
      [0, 0, {"cateCode": "C003", "cateName": "新增分类C", "cateType": "type3"}],
      [2, "abc202"]
    ]
  }
}
```

> `[1, "abc201", {...}]`：更新 id=abc201 的子记录；`[0, 0, {...}]`：新建子记录；`[2, "abc202"]`：删除 id=abc202 的子记录。

### ManyToMany 指令集 JSON-RPC 示例

以 `ExampleStudent`（主）↔ `ExampleClass`（`@ManyToMany`）为例：

**新建主记录时建立与已有记录的关系 `(4, id)`**

```json
{
  "model": "example_student",
  "service": "create",
  "args": [{
    "name": "张三",
    "classList": [
      [4, "abc10"],
      [4, "abc11"]
    ]
  }]
}
```

**更新时同时新增关系、断开关系、替换全量关系**

```json
{
  "model": "example_student",
  "service": "update",
  "args": {
    "id": "abc50",
    "classList": [
      [4, "abc12"],
      [3, "abc10"],
      [6, 0, ["abc11", "abc12", "abc13"]]
    ]
  }
}
```

> `[4, "abc12"]`：关联已有 class id=abc12；`[3, "abc10"]`：断开与 id=abc10 的关系（不删记录）；`[6, 0, ["abc11","abc12","abc13"]]`：用 ids 替换全量关系。

**清空所有关系 `(5)`**

```json
{
  "model": "example_student",
  "service": "update",
  "args": {
    "id": "abc50",
    "classList": [[5]]
  }
}
```

---

## @Property 高级参数

| 参数 | 说明 |
|---|---|
| `name/columnName` | 属性名和数据库列名；`columnName` 为下划线时，Java 访问仍用属性名 |
| `displayName/toolTips` | 显示名和帮助提示 |
| `isReadonly/readonly` | 只读只影响 UI；服务仍需校验不可改字段 |
| `isRequired/required` | 必填建议同时写 `@Validate.NotBlank` |
| `isStore/store` | 是否存库；计算/回显字段可设为 false |
| `dbIndex` | 存储字段才有效，外键/常用过滤字段建议加索引 |
| `ruleScript/computeScript` | 脚本规则或脚本计算；优先用 Java 方法便于维护 |
| `computeMethod/defaultMethod/validationMethod` | 方法名必须存在于模型或服务中 |
| `isDisplayForModel/displayForModel` | ManyToOne 或选择器回显字段 |
| `validationType` | 平台校验类型，复杂校验用 `@Validate` 或服务 |
| `widget` | 前端推荐组件；不能替代视图字段 `custom/type` |
| `percent/password/multiple/contentType/onChange` | 百分比、密码、多选、文件类型、变化事件等扩展属性 |

类型补充：

| 类型 | 建议 |
|---|---|
| `List/Map/Object` | 按 JSON 存储和读取，接口文档写清结构 |
| `File` | 配合 `contentType`、文件服务和 MinIO |
| `Text` | MySQL 长文本按长度选择 TEXT/MEDIUMTEXT/LONGTEXT；Oracle 长文本映射 CLOB |
| `Date/DateTime` | 使用 `DataType.DATE` 或 `DataType.DATE_TIME`，必要时配置 `dateFormat` |

---

## typed getter/setter

BaseModel 底层按 Map 存储字段。自定义 getter/setter 时：

- `columnName = "role_name"` 只影响数据库列，Java 取值仍用属性名 `roleName`。
- `Date` 用 `getDate("field")`。
- `Timestamp` 用 `getTimestamp("field")`。
- `Integer/Long/Boolean/BigDecimal/String` 分别用平台 typed getter。
- setter 使用 `set("fieldName", value)` 并返回 `this` 时，注意泛型类型。

不要在 getter 中直接做复杂查询；需要计算字段时优先使用 `computeMethod` 或服务层。

---

## 分组校验

当同一模型因类型不同有不同必填字段时，使用校验分组：

```java
@Property(displayName = "数据源类型", widget = "radio-group")
@Selection(values = {
    @Option(label = "DB/SQL", value = "DB", groups = Db.class),
    @Option(label = "API", value = "API", groups = Api.class)
})
private String type;

public interface Db {}
public interface Api {}
```

扩展字段按分组校验：

```java
@Validate.NotBlank(groups = TestDataSource.Api.class)
@Property(displayName = "请求头")
private String header;
```

API 调用 `create/update` 时，引擎可按类型触发分组校验。自定义服务中可显式使用：

```java
public void testValidate(@Validate.Validated(value = TestDataSource.Api.class) TestDataSourceApi api) {
}
```

### 多数据源联动完整示例（基类 + 继承 + groups 分组）

来自工程 `ExampleDataSource` + `ExampleDataSourceApi`，展示完整的"基类声明类型选项 + 子类按分组校验"模式：

**Step 1：基类声明类型字段和分组接口**

```java
@Model(name = "ExampleDataSource", description = "数据源")
public class ExampleDataSource extends BaseModel<ExampleDataSource> {

    @Property(displayName = "数据源类型", widget = "radio-group")
    @Selection(values = {
        @Option(label = "DB/SQL", value = "DB",   groups = Db.class),
        @Option(label = "API",    value = "API",  groups = Api.class),
        @Option(label = "Excel",  value = "Excel"),           // 无扩展字段
        @Option(label = "IIOT",   value = "IIOT", groups = Iiot.class)
    })
    private String type;

    // 分组接口：仅用于 @Validate(groups=...) 标记，无需实现任何方法
    public interface Db   {}
    public interface Api  {}
    public interface Iiot {}
}
```

**Step 2：子类继承基类，按分组添加必填校验**

```java
// API 类型子模型：仅 type=API 时校验以下字段
@Model(name = "ExampleDataSource", parent = "ExampleDataSource")
public class ExampleDataSourceApi extends BaseModel<ExampleDataSourceApi> {

    @Property(columnName = "header", displayName = "请求头")
    @Validate.NotBlank(groups = ExampleDataSource.Api.class)
    private String header;

    @Property(columnName = "parameters", displayName = "请求参数")
    @Validate.NotBlank(groups = ExampleDataSource.Api.class)
    private String parameters;
}
```

> **规则**：
> - `parent = "基类 model name"`，子类共用基类的表和字段。
> - `@Option(groups = Xxx.class)` 表示选中该选项时激活对应分组校验。
> - 子类字段加 `@Validate.Xxx(groups = 基类.Xxx.class)`，引擎在 `create/update` 时按 `type` 值自动触发对应分组。
> - 无扩展字段的选项（如 `Excel`）不需要子模型，只声明 `@Option` 即可。

---

## Selection 联动与回显

Selection 来源：

| 来源 | 写法 | 要求 |
|---|---|---|
| 静态选项 | `@Selection(values = {...})` | value 稳定，不随意改历史值 |
| 方法选项 | `@Selection(method = "selectX")` | 方法处理回显值和选项列表两种场景 |
| 模型选项 | `@Selection(model = "target", properties = "name", filter = "...")` | 目标模型可查询，filter 合法 |
| 多选 | `multiple = true` | 字段类型可为数组、List 或 ManyToMany |
| 联动 | `linkageFields = {"city", "area"}` | 子级方法参数顺序和视图 linkage 一致 |

方法选项必须处理：

- 回显：传入 value 时只返回当前 value 对应的 `Options`。
- 列表：未传 value 时返回完整可选项。
- 多选回显：传入 Object[] 或数组时返回所有匹配项。
- 联动：子级方法接收父字段值和当前 value。

常用方法签名：

| 场景 | 签名建议 | 说明 |
|---|---|---|
| 单选方法 | `public List<Options> selectX(Object value)` | `value` 非空时做回显，否则返回全量选项 |
| 多选方法 | `public List<Options> selectX(Object[] value)` | 按数组过滤回显，字段可为 `String[]` 或集合 |
| 二级联动 | `public List<Options> selectCity(String provinceId, String value)` | 最后一个参数为当前值，用于回显 |
| 三级联动 | `public List<Options> selectArea(String provinceId, String cityId, String value)` | 参数顺序与视图 `linkage.params` 一致 |
| 模型选项 | `@Selection(model = "target", properties = "name", filter = "...")` | 保存目标模型 `id`，展示 `properties` 指定字段 |

当联动目标是 ManyToOne/Lookup 查询时，视图侧 `linkage.type` 通常设为 `filter`，后端要保证目标模型的 `search/count/lookup` 能接收该 Filter。

---

## Dict 种子与查询回显

`@Dict(typeCode = "...")` 依赖字典类型和字典值数据。生成字典时，至少有：

```json
{
  "data": {
    "DictTypeBookStatus": {
      "model": "base_dict_type",
      "properties": {
        "dictName": "图书状态",
        "dictType": "bookStatus",
        "status": 0
      }
    },
    "DictValueBookEnable": {
      "model": "base_dict_value",
      "properties": {
        "dictLabel": "启用",
        "dictValue": "ENABLE",
        "dictType": "bookStatus",
        "isDefault": true,
        "status": 0
      }
    }
  }
}
```

查询 `@ManyToOne`、`@Selection`、`@Dict` 字段时，`useDisplayForModel=true` 会返回适合前端显示的 Options 或 Options 集合。服务端可用：

```java
getMeta().addArgument(MetaConstant.USE_DISPLAY_FOR_MODEL, true);
```

注意：

- `useDisplayForModel` 通常只对指定字段查询生效。
- 查询 `Arrays.asList("*")` 时可能不能得到期望显示结构。
- 查询结束后平台会清理该上下文参数，不要跨查询依赖。

---

## Related、Compute 与自动字段

`related` 用于带出 ManyToOne 关联模型字段，最多支持 3 层：

```java
@Property(displayName = "组织名称", related = "org.name", store = false)
private String orgName;
```

计算字段：

```java
@Property(displayName = "计算结果", store = false, computeMethod = "computeValue")
private String result;

public String computeValue(Map<String, Object> row) {
    return "computed";
}
```

自动字段：

| 字段 | 说明 |
|---|---|
| `id` | 雪花算法生成并转字符串 |
| `create_user/create_date` | 创建人和创建时间 |
| `update_user/update_date` | 更新人和更新时间 |
| `tenant_id` | 租户 ID |

自动字段默认不显示。需要在视图展示时，字段对象要写 `custom: true` 和 `display: true`。

---

## 日期模型与视图

模型字段：

```java
@Property(displayName = "年", dataType = DataType.DATE, dateFormat = "yyyy")
private Date year;

@Property(displayName = "时间", dataType = DataType.DATE_TIME, dateFormat = "yyyy-MM-dd HH:mm:ss")
private Timestamp datetime;
```

读取：

- 日期用 `getDate("field")`。
- 时间戳用 `getTimestamp("field")`。
- 年、月、时间需要显式 `dateFormat`。

视图规则：

- `year/month/date/datetime` 可用于表单、表格、搜索。
- `monthrange/daterange/datetimerange` 只能用于搜索条件，不能用于表单、表格字段。
- 后端 search 要把范围值转换成开始/结束条件。

---

## 视图模型 View Model

视图模型用于把一个或多个模型组合成查询模型，对前端像普通模型一样使用：

```java
@Model(name = "books_order_vm", type = Model.ModelType.View)
@View.From("books_order")
public class BooksOrderVm extends BaseModel<BooksOrderVm> {
    private String code;

    @View.MapProperty("reader.name")
    private String readerName;

    @View.MapProperty("detailList.bookName")
    @View.MapFunction(value = SQLFunction.JOIN, args = ",")
    private String bookNames;

    @Property(displayName = "统计时间", store = false)
    private String statTime;
}
```

规则：

- `@View.From` 必填，指定主映射模型。
- 字段未写 `@View.MapProperty` 时，默认按同名属性映射。
- ManyToOne 可多级关联。
- OneToMany 和 ManyToMany 只能关联一级；聚合字段可用 `JOIN/SUM/COUNT/AVG/MAX/MIN`。
- 如果所有映射来自同一个模型，增删改查通常可用；映射多个模型时优先只开放查询。
- 复杂赋值可重写 `search`，但要保留分页、排序、过滤契约。

聚合查询 properties 可传对象：

```json
{
  "properties": [
    "date",
    { "value": "JOIN", "args": [","], "alias": "names", "name": "name" },
    { "value": "SUM", "alias": "totalQty", "name": "qty" }
  ]
}
```

生成视图模型时，必须给前端视图提供稳定的字段名、别名和显示名。
