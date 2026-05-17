# 组件与字段映射参考

本文件覆盖组件类能力在后端侧的落点。组件本身由前端渲染，后端负责模型字段、选项数据、视图配置、服务入参和文件资源。需要完整底稿时读取 `../complete/basic-view.md` 或 `../complete/uiview-guide.md`。

## 通用规则

- 字段默认从 Java 模型 `@Property` 生成 `fields`，视图中写字符串即可。
- 需要覆盖组件、标题、必填、显隐、默认值时，视图字段写对象并配置 `custom: true`。
- 选项类组件优先用 `@Dict` 或 `@Selection`；动态选项用 `@MethodService`。
- 关联选择器优先用 ORM 关系；简单回显可用 `related` 冗余字段。

---

## 基础输入组件

| 组件 | Java 字段 | 模型注解 | 视图配置要点 |
|---|---|---|---|
| Input 输入框 | `String` | `@Property(length=...)`、`@Validate.Size` | `{ "name": "code", "custom": true, "type": "input" }` |
| Textarea 文本域 | `String` | 长文本用 `dataType = DataType.TEXT` | `type: "textarea"` |
| InputNumber 数字 | `Integer` / `Long` / `BigDecimal` | 金额配置 `length/scale/defaultValue` | `type: "inputNumber"` |
| Switch 开关 | `Boolean` 或状态 String | Boolean 直接字段；String 用 `@Selection` | `type: "switch"` |
| DatePicker 日期 | `Date` | `DataType.DATE` + `dateFormat` | `type: "datepicker"` |
| TimePicker 时间 | `Date` 或 `String` | 时间点建议 `DataType.DATE_TIME` | `type: "timePicker"` |
| Calendar 日历 | `Date` + 业务状态字段 | 日期字段 + 状态/标题字段 | 需要日历视图或自定义页面数据服务 |
| RichText 富文本 | `String` | `DataType.TEXT` | `type: "inputRichText"` |

示例：

```java
@Property(displayName = "备注", dataType = DataType.TEXT)
private String remark;

@Property(displayName = "金额", length = 22, scale = 2, defaultValue = "0")
private BigDecimal amount;
```

---

## 选项与字典组件

| 组件 | 后端实现 |
|---|---|
| Select | `@Selection(values=...)`、`@Dict(typeCode=...)` 或 `@Selection(method=...)` |
| Radio | 单选，通常同 Select |
| Checkbox | 多选，字段可为 String/List，复杂场景用 ManyToMany |
| Tags | 状态或标签展示，后端提供枚举/字典值 |
| Badge | 状态数量或状态字段，后端提供统计服务或字段 |
| Dropdown | 操作集合多为前端行为；涉及服务时配置 `service/auth/action` |

静态选项：

```java
@Property(displayName = "状态", defaultValue = "ENABLE")
@Selection(values = {
    @Option(label = "启用", value = "ENABLE"),
    @Option(label = "禁用", value = "DISABLE")
})
private String status;
```

字典：

```java
@Dict(typeCode = "yes_no")
@Property(displayName = "是否默认")
private String defaultFlag;
```

动态选项：

```java
@Selection(method = "selectBook", linkageFields = {"categoryId"})
@Property(displayName = "图书")
private String bookId;
```

---

## 关联与选择器

| 组件 | 后端契约 |
|---|---|
| Lookup | 关联模型可 `@ManyToOne`；普通弹窗选择用 `@Selection(model=...)` |
| LookupTable | 目标模型必须有 grid/search 视图和 `search/count/lookup` 服务 |
| Cascader | 后端提供层级字段，如 `parentId/code/name/leaf/type`；可用 `@Selection(cascader=true)` |
| ConditionalTree | 后端提供树数据查询服务和过滤条件 |
| Transfer | 多选关联，优先 ManyToMany 或中间表模型 |
| Tree | 树模型 + tree 视图 + 必要的父子字段 |

ManyToOne 示例：

```java
@ManyToOne(displayName = "图书分类", cascade = CascadeType.DEL_SET_NULL)
@JoinColumn(name = "category_id", referencedProperty = "id")
private BooksCategory category;

@Property(displayName = "分类名称", related = "category.categoryName", store = false)
private String categoryName;
```

ManyToMany 示例：

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

## 展示与布局组件

以下多数是前端视图能力，后端只提供字段和视图配置：

| 组件 | 后端支持方式 |
|---|---|
| Row / Container / Tabs / Collapse / Drawer / Dialog | 视图 JSON 布局、openView、form tabs |
| Tooltip / Alert / Progress / Timeline / Steps | 字段值、状态字段、统计/进度服务 |
| BreadCrumb / Link | 菜单、路由、URL 字段或 openView 配置 |
| Card 卡片列表 | 提供列表数据、图片/状态字段、卡片视图或自定义页面数据服务 |
| Image 图片组件 | 文件 ID/URL 字段、MinIO 文件服务、预览配置 |
| Carousels 走马灯 | 图片资源模型或静态资源路径 |

若用户要求“后端生成这些组件”，不要生成前端组件源码；生成对应的后端模型字段、数据服务和视图配置即可。

---

## 文件与上传组件

Upload/Image/FilePreview 后端至少确认：

- 文件服务 jar 已在 `apps/apps.json`。
- `app.json` 登记静态模板或菜单附件数据。
- 模型里有 `fileId/fileName/fileUrl/contentType` 等字段。
- 视图列或表单项配置预览/上传组件。
- MinIO 配置存在。

详细见 `file-excel-print-job.md`。

---

## 生成检查清单

- 每个组件字段都能在 Java 模型中找到对应属性。
- 选项组件有字典、静态选项、动态服务或关联模型来源。
- 关联组件的目标模型有可查询视图和服务。
- 上传/图片/文件预览有文件服务依赖和字段契约。
- 纯前端组件不生成空 Java 类，只补数据契约。
