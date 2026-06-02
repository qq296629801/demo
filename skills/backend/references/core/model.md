# Java 模型开发参考

本文件覆盖常规 Java 模型生成。涉及模型类型、继承/扩展、多继承、ER 指令集、属性高级参数、分组校验、Selection 回显、Dict 种子、日期范围或视图模型时，继续读取 `model-property-advanced.md`；若仍需完整底稿，读取 `../complete/create-model.md` 或 `../complete/create-property.md`。

## 模型类基本结构

```java
package com.sie.iidp.{appPkg}.{moduleName}.model;

// ===== 元插件（必须，生成 static 字段常量 / getter / setter）=====
import com.sie.meta.plugin.StaticVar;
import com.sie.meta.plugin.Getter;  // IIDP 平台元插件，不是 lombok.Getter，禁止替换
import com.sie.meta.plugin.Setter;  // IIDP 平台元插件，不是 lombok.Setter，禁止替换

// ===== 模型注解（必须引入）=====
import com.sie.snest.sdk.BaseModel;
import com.sie.snest.sdk.annotation.meta.Model;
import com.sie.snest.sdk.annotation.meta.Property;
import com.sie.snest.sdk.annotation.meta.MethodService;
import com.sie.snest.engine.model.Bool;

// ===== 属性注解类型（必须引入）=====
import com.sie.snest.sdk.DataType;

// ===== 校验注解（按需）=====
import com.sie.snest.sdk.annotation.validate.Validate;

// ===== ORM 关联注解（按需）=====
import com.sie.snest.sdk.annotation.orm.*;   // @Selection @Option @OneToMany @ManyToOne @JoinColumn @JoinTable @Shard @Index
import com.sie.snest.sdk.annotation.Dict;    // @Dict 字典
import com.sie.snest.sdk.CascadeType;        // CascadeType.DEL_SET_NULL 等

// ====  服务方法常量 （按需） =====
import com.sie.iidp.common.util.consts.MethodConst;

// ===== 服务方法常用 API（按需）=====
import com.sie.snest.engine.data.RecordSet;
import com.sie.snest.engine.context.BaseContextHandler;
import com.sie.snest.engine.container.Meta;
import com.sie.snest.engine.rule.Filter;
import com.sie.snest.engine.constant.MetaConstant;
import com.sie.snest.engine.exception.ModelException;
import com.sie.snest.engine.exception.ValidationException;
import com.sie.snest.sdk.db.DbUtils;

// ===== 日志（需要日志时加）=====
import lombok.extern.slf4j.Slf4j;

// ===== Java 标准库 =====
import java.util.*;

// 类级注解说明：
// @StaticVar  — 元插件，自动生成每个字段的 static 常量（如 F_FIELD_NAME），用于 Filter/values.put 避免硬编码字符串
// @Getter     — 元插件，生成类型安全的 getter（内部调用 BaseModel 的 typed getter，非普通 Lombok）
// @Setter     — 元插件，生成 setter（内部调用 BaseModel 的 set 方法）
// @Slf4j      — Lombok，注入 log 变量；只有需要日志时才加
@StaticVar
@Getter
@Setter
@Slf4j
@Model(
    tableName = "{table_name}",
    name = "{model_name}",
    displayName = "{中文名}",
    isAutoLog = Bool.True,
    isLogicDelete = Bool.True,
    orderBy = "id desc"
)
public class {ModelName} extends BaseModel<{ModelName}> {

    @Property(displayName = "{字段中文名}", length = 64)
    @Validate.NotBlank(message = "{字段中文名}不能为空")
    private String {fieldName};
}
```

> **元插件说明**：`@StaticVar @Getter @Setter` 是 IIDP 平台元插件，**不是** Lombok 的 `@Getter/@Setter`，两者不可混用。编译时由平台插件处理，生成的 getter/setter 底层调用 `BaseModel.getStr/getInt/getDate` 等 typed getter，保证字段值从 Map 中正确取出。

---

## BaseModel 继承能力

模型类声明 `extends BaseModel<{ModelName}>` 后，当前模型即可直接使用 BaseModel 提供的通用模型能力。`extends BaseModel<T>` 不只是类型声明，也意味着模型内的 `@MethodService` 方法可以调用 BaseModel 的 CRUD、查询、事务、SQL、异步、属性 typed getter 和服务获取能力。

生成或修改模型服务方法时，如果要使用 `create`、`update`、`delete`、`select`、`selectById`、`search`、`count`、`commit`、`rollback`、`execute`、`getMeta`、`getStr`、`getInt`、`getLong`、`getDate`、`runAsync`、`supplyAsync`、`initService`、`initNewService` 等方法，必须先读取 [`BaseModel.md`](BaseModel.md)，以其中的方法签名和约束为准，不得凭记忆补全参数或返回值。

---

## 注解作用域规则（严格执行）

> 完整速查表见 [`annotation-scope.md`](annotation-scope.md)。

**类上只允许以下注解**：`@StaticVar`、`@Getter`、`@Setter`、`@Slf4j`、`@Model`。

**方法上只允许以下注解**：`@MethodService`。

**以下注解只能标注在字段上，禁止标注在类或方法上**：

| 注解 | 说明 |
|---|---|
| `@Property` | 字段元信息声明 |
| `@Validate.NotBlank` / `@Validate.Size` / `@Validate.Pattern` / `@Validate.Unique` | 字段校验 |
| `@Selection` / `@Option` | 下拉选项 |
| `@Dict` | 字典 |
| `@ManyToOne` / `@OneToMany` / `@ManyToMany` | ER 关联 |
| `@JoinColumn` / `@JoinTable` | ER 关联映射 |

违反此规则会导致编译失败或引擎无法识别字段元信息。

---

## 完整模型示例（来自 ExampleStudent）

展示常见字段类型、校验、ER 关联、计算字段、字典、分片、索引等组合使用：

```java
@StaticVar
@Getter
@Setter
@Slf4j
@Model(
    tableName = "example_student",
    name = "example_student",
    displayName = "Example学生",
    isLogicDelete = Bool.True,
    isAutoLog = Bool.True,
    isPrint = Bool.True,
    isShard = Bool.False,
    shard = @Shard(shardType = "tables", shardPoint = "create_date:YEAR", shardValues = "2021"),
    indexes = {@Index(name = "IDX_CLASS_LEVEL", columnList = {"class_id", "student_level"})}
)
public class ExampleStudent extends BaseModel<ExampleStudent> {

    // 普通字符串字段 + 校验
    @Validate.Size(max = 30)
    @Validate.NotBlank(message = "姓名不能为空")
    @Property(displayName = "姓名", columnName = "name", length = 30)
    private String name;

    // 静态选项字段
    @Property(displayName = "性别", columnName = "sex", length = 1)
    @Selection(values = {
        @Option(label = "男", value = "1"),
        @Option(label = "女", value = "0")
    })
    @Validate.NotBlank(message = "性别不能为空")
    private String sex;

    // 日期字段
    @Property(columnName = "birth_day", displayName = "生日",
              dataType = DataType.DATE, dateFormat = "yyyy-MM-dd")
    @Validate.NotBlank(message = "生日不能为空")
    private Date birthDay;

    // 字典字段
    @Dict(typeCode = "studentLevel")
    @Property(displayName = "学生等级")
    @Validate.NotBlank(message = "学生等级不能为空")
    private Integer studentLevel;

    // 计算字段（store=false，不持久化，由 computeMethod 动态计算）
    @Property(displayName = "是否4级学生", store = false, computeMethod = "isFourLevel")
    private Boolean isFourLevel;

    // 关联选择字段（外键 ID）
    @Validate.NotBlank(message = "班级不能为空")
    @Selection(model = "example_class", properties = {"id", "className"})
    @Property(displayName = "班级")
    private String classId;

    // ManyToOne ER 关联（与 classId 对应同一外键列）
    @ManyToOne(displayName = "班级", cascade = CascadeType.DEL_SET_NULL)
    @JoinColumn(name = "class_id", referencedProperty = "id")
    private ExampleClass exampleClass;

    // related 字段：带出关联模型字段，store=false
    @Property(displayName = "班级编码", related = "exampleClass.classCode")
    private String classCode;

    @Property(displayName = "班级名称", related = "exampleClass.className")
    private String className;

    // ManyToMany 关联
    @ManyToMany(displayName = "课程")
    @JoinTable(name = "example_student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id"))
    private List<ExampleCourse> courseList;

    // OneToMany 关联
    @OneToMany
    private List<ExampleStudentAttachment> attachmentList;

    // 计算字段方法
    public Boolean isFourLevel(Map<String, Object> valMap) {
        Object level = valMap.get(F_STUDENT_LEVEL);
        return level != null && Integer.parseInt(level.toString()) == 4;
    }
}
```

---

## @Model 常用参数

| 参数 | 说明 | 常用值 |
|---|---|---|
| `name` | 模型名（小写下划线，**必填**，全局唯一） | `"books_manage"` |
| `tableName` | 数据库表名，默认与 `name` 一致 | `"books_manage"` |
| `displayName` | 模型中文名 | `"图书管理"` |
| `isAutoLog` | 自动生成创建人/时间/修改人/时间字段 | `Bool.True` |
| `isLogicDelete` | 启用逻辑删除 | `Bool.True` |
| `isPrint` | 是否支持打印 | `Bool.True` |
| `orderBy` | 默认排序字段 | `"id desc"` |
| `indexes` | 数据库联合索引，**`@Index` 只能写在 `@Model` 的 `indexes` 参数中（类级别），禁止放到属性或方法上** | `@Index(name="IDX_XX", columnList={"col1","col2"})` |
| `parent` | 继承/扩展的父模型名（字符串数组） | `{"example_unit"}` |
| `isShard` / `shard` | 分库分表开关与策略 | `isShard = Bool.False` |
| `type` | 模型类型，DTO/Mix 场景才需要显式配置 | `Model.ModelType.Mix` |
| `hiddenApi` | 在 API 文档中隐藏指定服务 | `{"internalMethod"}` |
| `dataSource` | 指定模型数据源 | `"example_db"` |

---

## 数据类型

| Java 类型 | 说明 | 备注 |
|---|---|---|
| `String` | 字符串，默认长度 240 | 超过 5500 字符改用 `dataType=DataType.TEXT` |
| `Integer` | 32 位整型 | |
| `Long` | 64 位长整型 | |
| `BigDecimal` | 高精度小数，默认精度 (38, 3) | 金额、价格等 |
| `Double` | 双精度浮点，默认精度 (22, 2) | |
| `Boolean` | 布尔值 | |
| `Date` | 日期/时间，需配合 `@Property(dataType, dateFormat)` | `DataType.DATE` 仅日期；`DataType.DATE_TIME` 含时间 |

---

## @Property 常用参数

| 参数 | 说明 | 默认值 |
|---|---|---|
| `displayName` | 字段中文名（**必填**） | — |
| `columnName` | 数据库列名，默认驼峰转下划线 | 属性名转下划线 |
| `length` | 字段长度 | 类型默认值 |
| `scale` | 小数位数（BigDecimal） | 3 |
| `defaultValue` | 新增时的默认值 | — |
| `store` | 是否存储到数据库，`false` = 不存库 | true |
| `readonly` | 字段只读 | false |
| `display` | 是否默认显示 | true |
| `nullable` | DDL 是否允许为空 | true |
| `computeMethod` | 计算方法名，配合 `store=false` | — |
| `defaultMethod` | 默认值方法名 | — |
| `validationMethod` | 自定义校验方法名 | — |
| `related` | 关联字段路径 `"modelProperty.fieldName"` | — |
| `dataType` | 日期类型：`DataType.DATE` / `DataType.DATE_TIME` | — |
| `dateFormat` | 日期格式字符串 | — |
| `displayForModel` | 标记为模型显示字段，用于 ManyToOne 回显 | false |
| `widget` | 建议前端组件名 | — |
| `multiple` | 是否多选 | false |
| `password` | 是否密码字段 | false |
| `toolTips` | 字段提示 | — |

### 常用字段示例

```java
// 普通字符串
@Property(displayName = "书名", columnName = "book_name", length = 120)
private String bookName;

// 带默认值整型
@Property(displayName = "库存数量", length = 15, defaultValue = "0")
private Integer stockQty;

// BigDecimal
@Property(displayName = "定价", length = 22, scale = 2, defaultValue = "0")
private BigDecimal price;

// 日期
@Property(displayName = "出版日期", dataType = DataType.DATE, dateFormat = "yyyy-MM-dd")
private Date publishDate;

// 日期时间
@Property(displayName = "借阅时间", dataType = DataType.DATE_TIME, dateFormat = "yyyy-MM-dd HH:mm:ss")
private Date borrowTime;

// 长文本
@Property(displayName = "简介", dataType = DataType.TEXT)
private String summary;

// 计算字段（不存库）
@Property(displayName = "是否超期", store = false, computeMethod = "isOverdue")
private Boolean isOverdue;

// 关联字段（冗余存储）
@Property(displayName = "书名", related = "booksManage.bookName")
private String bookName;

// 关联字段（不存库）
@Property(displayName = "书号", related = "booksManage.isbn", store = false)
private String isbn;

// displayForModel = true：标记该字段为 ManyToOne 关联模型的显示字段
// 查询时 useDisplayForModel=true，平台用此字段值作为外键 ID 的显示文本
// 来自 ExampleItemCate.cateName
@Property(displayName = "分类名称", columnName = "cate_name", length = 60, displayForModel = true)
private String cateName;

// 对应 ManyToOne 外键：外键 ID 字段 + displayForModel 字段配对使用
@ManyToOne(displayName = "物料", cascade = CascadeType.DELETE)
@JoinColumn(name = "item_id", referencedProperty = "id")
private ExampleItem exampleItem;
```

---

## @Validate 校验注解

| 注解 | 参数 | 说明 |
|---|---|---|
| `@Validate.NotBlank` | `message` | 非空校验 |
| `@Validate.Size` | `max`、`min` | 字符串长度限制 |
| `@Validate.Pattern` | `regex` | 正则格式校验 |
| `@Validate.Unique` | `properties`、`message` | 唯一性校验（支持联合唯一） |

```java
@Validate.NotBlank(message = "书名不能为空")
@Validate.Size(max = 120)
@Property(displayName = "书名", length = 120)
private String bookName;

// 联合唯一
@Validate.Unique(properties = {"isbn", "edition"}, message = "ISBN:${isbn}已存在")
private String isbn;
```

---

## @Selection 下拉选项

```java
// 枚举常量
@Property(displayName = "状态", defaultValue = "1")
@Selection(values = {
    @Option(label = "在库", value = "1"),
    @Option(label = "借出", value = "0")
})
private String status;

// 关联模型（properties 第一个为 value，其余为展示字段）
@Selection(model = "books_manage", properties = {"id", "bookName"})
@Property(displayName = "图书")
private String bookId;

// 方法提供选项（支持联动）
@Selection(method = "selectBook", linkageFields = "categoryId")
@Property(displayName = "图书编号")
private String bookCode;

// 多选（ManyToMany）
@ManyToMany(targetModel = "books_tag")
@JoinTable(name = "books_manage_tag",
    joinColumns = @JoinColumn(name = "book_id", nullable = false),
    inverseJoinColumns = @JoinColumn(name = "tag_id", nullable = false))
@Selection(multiple = true, properties = {"tagName"})
@Property(displayName = "标签")
private List<BooksTag> tagList;

// 字典（String 类型）
@Dict(typeCode = "yes_no")
@Property(displayName = "是否绝版")
private String isOutOfPrint;

// 字典（Integer 类型，来自 ExampleItemAttribute.attributeType）
@Dict(typeCode = "itemAttributeType")
@Property(displayName = "物料属性类型", columnName = "attribute_type", length = 10)
private Integer attributeType;

// widget：建议前端使用的渲染组件（来自 ExampleDataSource）
// 常用值："radio-group"（单选按钮组）；省略时平台按字段类型自动选择
@Property(displayName = "数据源类型", widget = "radio-group")
@Selection(values = {
    @Option(label = "DB/SQL", value = "DB", groups = Db.class),
    @Option(label = "API",    value = "API", groups = Api.class),
    @Option(label = "Excel",  value = "Excel")
})
private String type;

// groups 用于条件渲染：当选中该 Option 时，标记了同 groups 的字段才显示/必填
// groups 值为自定义 interface，与视图 bind_display 联动
interface Db {}
interface Api {}

// 外键 Selection + ManyToOne 配对写法（来自 ExampleStudent，工程中最常见模式）
// classId：前端选择器绑定字段，存外键 ID
// exampleClass：ORM 关联对象，提供 related 字段带出
@Validate.NotBlank(message = "班级不能为空")
@Selection(model = "example_class", properties = {"id", "className"})
@Property(displayName = "班级")
private String classId;

@ManyToOne(displayName = "班级", cascade = CascadeType.DEL_SET_NULL)
@JoinColumn(name = "class_id", referencedProperty = "id")
private ExampleClass exampleClass;

// 通过 related 带出关联模型字段（不存库）
@Property(displayName = "班级名称", related = "exampleClass.className")
private String className;
```

---

## ER 关系注解

普通 `ManyToMany` 适合无额外字段的直接多对多关系；如果中间关系需要有效期、状态、排序、权限范围等业务字段，先读取 [`er.md`](er.md)，使用 OneToMany + ManyToOne 中间模型方案，并同步调整 ER 子表视图与 `addEr/createEr` 按钮行为。

### OneToMany（一对多）

```java
// 同 App
@OneToMany
@Property(displayName = "附件列表")
private List<BooksManageAttachment> attachmentList;

// 跨 App
@OneToMany(targetModel = "other_app.other_model", targetProperty = "parentId")
private List<Map<String, Object>> items;
```

### ManyToOne（多对一）

```java
// 同 App，删除主记录时子表外键置 null
@ManyToOne(displayName = "图书分类", cascade = CascadeType.DEL_SET_NULL)
@JoinColumn(name = "cate_id", referencedProperty = "id")
private BooksCate booksCate;

// 同 App，级联删除子表
@ManyToOne(displayName = "借阅单", cascade = CascadeType.DELETE)
@JoinColumn(name = "borrow_id", referencedProperty = "id")
private BooksBorrow booksBorrow;

// 跨 App（字段类型用 Map）
@ManyToOne(displayName = "组织", targetModel = "mbm-main.res_enterprise")
@JoinColumn(name = "org_id", referencedProperty = "id")
private Map<String, Object> enterprise;
```

CascadeType 可选值：
- 默认：删除主表不影响子表
- `DEL_SET_NULL`：子表外键置 null
- `DELETE`：级联删除子表
- `DEL_NO_PERMIT_RELATIVE`：子表有数据时禁止删除主表

### ManyToMany（多对多）

```java
@ManyToMany(targetModel = "books_tag")
@JoinTable(name = "books_manage_tag",
    joinColumns = @JoinColumn(name = "book_id", nullable = false),
    inverseJoinColumns = @JoinColumn(name = "tag_id", nullable = false))
@Selection(multiple = true, properties = {"tagName"})
@Property(displayName = "标签")
private List<BooksTag> tagList;
```

---

## 模型扩展与继承

```java
// 扩展（parent 指向自身模型名）
@Model(name = "books_manage", parent = {"books_manage"}, displayName = "图书扩展")
public class BooksManageExt extends BaseModel<BooksManageExt> {
    @Property(displayName = "新增字段")
    private String newField;
}

// 继承（parent 指向其他模型）
@Model(name = "books_manage_item", parent = {"books_manage", "example_base_item"}, displayName = "图书（带基础物料属性）")
public class BooksManageItem extends BaseModel<BooksManageItem> { }
```

`parent` 是字符串数组。单父模型可以写 `parent = "example_unit"` 或 `parent = {"example_unit"}`，多父模型必须写数组；不要写成逗号拼接字符串。

---

## DTO / Mix 入参出参模型

`iidp-backend-demo` 的二开示例把复杂业务入参、出参拆成 DTO，并把复杂逻辑放到 `service/` 层：

| 类型 | 基类 | 放置位置 | 用途 |
|---|---|---|---|
| 请求 DTO | `RequestModel` | `model/request/*Req.java` | 强类型入参、字段校验、默认值计算 |
| 响应 DTO | `ResponseModel` | `model/response/*Res.java` | 强类型出参、接口文档字段说明 |
| 业务 Service | `SdkService<T>` | `service/*Service.java` | 承载复杂业务逻辑，避免 Model 过胖 |

请求 DTO 示例：

```java
@Getter
@Setter
@Model(displayName = "图书新增入参", name = "d_books_manage_add_req")
public class BooksManageAddReq extends RequestModel {
    @Validate.NotBlank(message = "书名不能为空")
    @Property(displayName = "书名")
    private String bookName;

    @Property(displayName = "图书编码", computeMethod = "generateCode")
    private String bookCode;

    public String generateCode() {
        return IdGenerator.nextId();
    }
}
```

响应 DTO 示例：

```java
@Getter
@Setter
@Model(displayName = "图书出参", name = "d_books_manage_res")
public class BooksManageRes extends ResponseModel {
    @Property(displayName = "ID")
    private String id;

    @Property(displayName = "书名")
    private String bookName;
}
```

模型层注入 Service：

```java
@InjectMeta
private BooksManageService service = this.initService(BooksManageService.class);

@MethodService(description = "新增-入参强类型", doc = "./doc/二开能力专题.md")
public boolean create(List<BooksManageAddReq> valuesList) {
    return service.create(valuesList);
}
```

当业务方法超过简单 CRUD 重写、需要复用逻辑或强类型接口文档时，优先使用 DTO + Service 分层。
