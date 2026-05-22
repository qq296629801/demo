# 注解作用域速查

本文件是 IIDP 平台注解使用规范的速查卡。所有注解的允许/禁止位置均为**严格执行**规则，违反会导致编译失败或引擎无法识别元信息。

---

## 一、作用域对照表

| 注解 | 类 | 方法 | 字段 | 说明 |
|---|:---:|:---:|:---:|---|
| `@Model` | ✅ | ❌ | ❌ | 声明模型元信息 |
| `@StaticVar` | ✅ | ❌ | ❌ | 元插件，生成字段 static 常量（`F_FIELD_NAME`） |
| `@Getter` | ✅ | ❌ | ❌ | 元插件，生成类型安全 getter（**非** Lombok） |
| `@Setter` | ✅ | ❌ | ❌ | 元插件，生成 setter（**非** Lombok） |
| `@Slf4j` | ✅ | ❌ | ❌ | Lombok，注入 `log` 变量 |
| `@MethodService` | ❌ | ✅ | ❌ | 暴露方法为可调用服务 |
| `@Property` | ❌ | ❌ | ✅ | 字段元信息声明 |
| `@Validate.NotBlank` | ❌ | ❌ | ✅ | 非空校验 |
| `@Validate.Size` | ❌ | ❌ | ✅ | 字符串长度限制 |
| `@Validate.Pattern` | ❌ | ❌ | ✅ | 正则格式校验 |
| `@Validate.Unique` | ❌ | ❌ | ✅ | 唯一性校验（支持复合唯一） |
| `@Selection` / `@Option` | ❌ | ❌ | ✅ | 下拉选项 |
| `@Dict` | ❌ | ❌ | ✅ | 字典映射 |
| `@ManyToOne` | ❌ | ❌ | ✅ | ER 多对一关联 |
| `@OneToMany` | ❌ | ❌ | ✅ | ER 一对多关联 |
| `@ManyToMany` | ❌ | ❌ | ✅ | ER 多对多关联 |
| `@JoinColumn` | ❌ | ❌ | ✅ | ER 外键列映射 |
| `@JoinTable` | ❌ | ❌ | ✅ | ER 多对多中间表映射 |

---

## 二、分类说明

### 只能标注在类上

```java
@StaticVar
@Getter
@Setter
@Slf4j
@Model(
    tableName = "xxx",
    name = "xxx",
    displayName = "中文名",
    isAutoLog = Bool.True,
    isLogicDelete = Bool.True
)
public class MyModel extends BaseModel<MyModel> { ... }
```

- `@StaticVar` / `@Getter` / `@Setter` 是 **IIDP 平台元插件**，不是 Lombok，不可混用
- `@Model` 每个类只允许一个，且只能在类上

### 只能标注在方法上

```java
@MethodService(description = "create")
public Object create(List<Map<String, Object>> valuesList) { ... }

@MethodService(description = "自定义业务操作")
public boolean enableDisable(RecordSet rs, String statusFlag) { ... }
```

- `@MethodService` 禁止标注在类或字段上
- 重写内置 CRUD 时，Java 方法名保持 `create` / `update` / `delete` / `find` / `search` / `count`

### 只能标注在字段上

```java
// 校验
@Validate.NotBlank(message = "编码不能为空")
@Property(displayName = "编码", length = 60)
private String code;

// 下拉选项（静态值）
@Selection(values = {
    @Option(label = "启用", value = "1"),
    @Option(label = "禁用", value = "0")
})
@Property(displayName = "状态", defaultValue = "1")
private String status;

// 字典
@Dict(typeCode = "unitType")
@Property(displayName = "单位类型")
private Integer unitType;

// ER 关联
@ManyToOne(displayName = "班级", cascade = CascadeType.DEL_SET_NULL)
@JoinColumn(name = "class_id", referencedProperty = "id")
private ExampleClass exampleClass;

@OneToMany
private List<ExampleStudentAttachment> attachmentList;
```

---

## 三、禁止规则速记

> 违反以下任何一条，将导致**编译失败**或**引擎无法识别字段元信息**。

| 错误写法 | 原因 |
|---|---|
| 把 `@Property` 标注在类上 | `@Property` 只能在字段上 |
| 把 `@Validate.*` 标注在类上 | 校验注解只能在字段上 |
| 把 `@Selection` / `@Dict` 标注在类上 | 选项/字典注解只能在字段上 |
| 把 `@ManyToOne` / `@OneToMany` 等标注在类上 | ER 注解只能在字段上 |
| 把 `@MethodService` 标注在类或字段上 | 服务注解只能在方法上 |
| 把 `@Model` 标注在方法或字段上 | 模型注解只能在类上 |
| 在字段上混用 Lombok `@Getter` / `@Setter` | 与平台元插件冲突，产生类型不匹配 |

---

## 四、标准字段注解顺序

同一字段上有多个注解时，建议按以下顺序书写，保持代码一致性：

```
@Validate.*          ← 校验注解（最上方）
@Selection / @Dict   ← 选项 / 字典
@ManyToOne 等        ← ER 关联（有关联时）
@JoinColumn          ← 外键映射（紧跟 ER 注解）
@Property            ← 字段元信息（紧邻字段声明）
private Type field;  ← 字段声明
```

---

## 参考文档

- 模型核心规范：`model.md`（注解作用域规则章节）
- 服务方法规范：`method-service.md`
- 高级属性与分组校验：`model-property-advanced.md`
