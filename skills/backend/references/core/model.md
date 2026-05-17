# Java 模型开发参考

本文件覆盖常规 Java 模型生成。涉及模型类型、继承/扩展、多继承、ER 指令集、属性高级参数、分组校验、Selection 回显、Dict 种子、日期范围或视图模型时，继续读取 `model-property-advanced.md`；若仍需完整底稿，读取 `../complete/create-model.md` 或 `../complete/create-property.md`。

## 模型类基本结构

```java
package com.sie.iidp.{appPkg}.{moduleName}.model;

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
import com.sie.snest.sdk.annotation.orm.Selection;


import com.sie.snest.sdk.annotation.Dict;    // @Dict 字典
import com.sie.snest.sdk.CascadeType;        // CascadeType.DEL_SET_NULL 等

// ====  服务方法常量 （必须引入） =====
import com.sie.iidp.common.util.consts.MethodConst;

// ===== 服务方法常用 API（按需）=====
import com.sie.snest.engine.data.RecordSet;               // RecordSet
import com.sie.snest.engine.context.BaseContextHandler;   // BaseContextHandler.getMeta()
import com.sie.snest.engine.container.Meta;               // 手动事务控制
import com.sie.snest.engine.rule.Filter;                  // Filter 查询条件
import com.sie.snest.engine.model.ModelMeta;              // 模型元信息
import com.sie.snest.engine.constant.MetaConstant;        // MetaConstant.USE_DISPLAY_FOR_MODEL 等
import com.sie.snest.engine.exception.ModelException;     // 业务异常（会触发事务回滚）
import com.sie.snest.engine.exception.ValidationException;// 校验异常（不触发回滚）
import com.sie.snest.sdk.db.DbUtils;                      // DbUtils.search / batchCreate 等

// ===== 直接执行 SQL（按需）=====
import com.sie.snest.engine.data.access.BussModelDataAccess;
import com.sie.snest.engine.data.access.ModelDataAccessFactory;
import com.sie.snest.engine.data.access.ModelTypeEnum;
import com.sie.snest.engine.db.relationdb.RelationDBAccessor;
import com.sie.snest.engine.db.relationdb.provider.SqlProvider;

// ===== 缓存 / 配置（按需）=====
import com.sie.snest.sdk.cache.RedisHelper;         // Redis 分布式锁 / 缓存
import com.sie.snest.engine.utils.ConfigUtils;      // 读取配置中心值

// ===== 工具类（按需）=====
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.date.DateUtil;

// ===== Java 标准库 =====
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Model(
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
| `indexes` | 数据库联合索引 | `@Index(name="IDX_XX", columnList={"col1","col2"})` |
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

// 字典
@Dict(typeCode = "yes_no")
@Property(displayName = "是否绝版")
private String isOutOfPrint;
```

---

## ER 关系注解

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
