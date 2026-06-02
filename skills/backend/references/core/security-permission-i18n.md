# 权限、菜单与多语言规范

本文件覆盖系统权限、自定义权限、菜单权限、多语言扫描、语料文件、异常翻译、数据多语言和用户可见文案。

## 权限基本原则

- 菜单控制入口可见性。
- 视图按钮用 `auth` 控制操作权限。
- 服务方法可用 `@MethodService(auth = "...")` 补充服务权限。
- 后端关键写操作必须校验业务状态，不能只依赖前端按钮隐藏。

---

## 标准权限码

| 操作 | `auth` |
|---|---|
| 查看/详情 | `read` |
| 新增 | `create` |
| 编辑/修改 | `update` |
| 删除 | `delete` |
| 导入 | `import` |
| 导出 | `export` |
| 启用 | `enable` |
| 禁用 | `disable` |
| 自定义 | lowerCamelCase 或小写英文 |

视图示例：

```json
{
  "name": "启用",
  "action": "enable",
  "auth": "enable",
  "model": "books_manage",
  "service": "enableDisable"
}
```

服务示例：

```java
@MethodService(name = "enableDisable", description = "启用禁用", auth = "enable")
public boolean enableDisable(RecordSet rs, String statusFlag) {
    return true;
}
```

---

## 自定义权限

当 demo 需求出现“按钮可见性”“接口权限”“按字段控制 lookup”时：

- 视图按钮写 `auth`。
- 后端视图必须配置可识别的 `name`，且 `name` 与最外层 `type` 保持同级，避免权限配置中权限名称为空。
- 同一视图层级内的 `auth` 值保持唯一，避免按钮配置后不显示或不受权限控制。
- 菜单 `config._self.{service}` 可为特定服务附加过滤。
- 后端服务仍需要检查记录状态和用户上下文。

禁止：

- 只在前端 Hook 中判断权限，后端服务无限制执行。
- 所有自定义按钮复用 `update`，导致权限粒度失真。

---

## 菜单权限与菜单复用

复用菜单时：

- 新菜单 `name` 唯一。
- `model/view` 可复用。
- `filter/config` 表示数据范围。
- 权限分配以新菜单为粒度。

示例见 `menu.md`。

---

## 数据范围与作用域

涉及组织、租户、用户范围时：

- 优先使用平台作用域能力或菜单过滤。
- 模型可按需开启 `isScope`。
- 自定义服务中不要绕过作用域查询，除非显式 `ignoreScope` 并有业务理由。

```java
@MethodService(description = "管理员查询", ignoreScope = true)
public List<BooksManage> adminSearch(Filter filter) {
    return this.search(filter, Collections.singletonList("*"), 100, 0, "id desc");
}
```

使用 `ignoreScope` 必须谨慎，交付说明要写明原因。

---

## 多语言与文案

IIDP 多语言基于元模型动态渲染能力。后端交付需要保证元模型、视图、菜单、异常和必要的业务数据都有稳定可扫描、可翻译的 key。

后端生成的用户可见文案包括：

- `@Model(displayName/description)`。
- `@Property(displayName/toolTips)`。
- 视图 `name/displayName/label/header/tips`。
- 菜单 `display_name`。
- 异常提示、导入错误提示。

### 启用条件

- 引擎版本需支持国际化能力，建议 `>= v2.3.0.RELEASE`。
- 后端需引入内置国际化 App：`sie-snest-i18n`。
- 前端多语言包通常由应用市场安装 `tech-i18nApp`。
- 普通中文 Demo 可直接写中文；明确要求国际化、出海、多语种交付或异常翻译时，必须补语言包和扫描配置。

### Maven 语言包扫描

需要国际化时，在业务 App 子模块的 `sie-snest-maven-plugin` 中配置语言包扫描。父 POM 已承担打包时，不要重复破坏 `repackage` 配置，只在子模块补扫描执行和语言配置。

核心配置项：

| 配置 | 说明 |
|---|---|
| `scan-meta-langKey` | 扫描元模型和后端视图文案，生成语言包 key |
| `full-scan-langKey` | 全量扫描工程文本，仅用于辅助发现语料，结果需人工确认 |
| `scanMetaLang` | 是否扫描元模型属性，默认可为 `true`；使用后可改为 `false` 避免每次打包覆盖 |
| `insert` | 发现新翻译条目时插入已有翻译文件 |
| `supportLanguage` | 支持语种，如 `zh-CN;en-US` |
| `scanViewMetaKeys` | 视图扫描字段，常用 `display_name/displayName/description/label/name/header/tips` |
| `sortLang.paths` | 需要整理的语言包，如 `messages_en-US.properties`、`messages_zh-CN.properties` |

最小示意：

```xml
<execution>
    <id>scan-lang</id>
    <phase>package</phase>
    <goals>
        <goal>scan-meta-langKey</goal>
    </goals>
    <configuration>
        <scanMetaLang>true</scanMetaLang>
        <insert>true</insert>
        <sorted>false</sorted>
        <supportLanguage>zh-CN;en-US</supportLanguage>
        <scanViewMetaKeys>
            <scanViewMetaKey>display_name</scanViewMetaKey>
            <scanViewMetaKey>displayName</scanViewMetaKey>
            <scanViewMetaKey>description</scanViewMetaKey>
            <scanViewMetaKey>label</scanViewMetaKey>
            <scanViewMetaKey>name</scanViewMetaKey>
            <scanViewMetaKey>header</scanViewMetaKey>
            <scanViewMetaKey>tips</scanViewMetaKey>
        </scanViewMetaKeys>
    </configuration>
</execution>
```

### 语言包文件

| 文件类型 | 命名规则 | 示例 | 说明 |
|---|---|---|---|
| 内置语料 | `messages_xx-XX.properties` | `messages_zh-CN.properties`、`messages_en-US.properties`、`messages_zh-TW.properties` | 语种编码来自国际化 App 的多语言管理 |
| 用户自定义语料 | `messages_user_defined_xx-XX.properties` | `messages_user_defined_zh-CN.properties` | 与内置 key 相同时，自定义语料优先 |
| 全量扫描辅助文件 | `scan_langKey.properties` | `scan_langKey.properties` | 只作辅助检查，不能直接当交付语料 |

若其他规范中出现 `message_语言标识.properties` 写法，生成 backend 时以国际化插件要求的 `messages_` 前缀为准。

语言包放置在资源目录中，通常为 `src/main/resources` 或插件生成的 resource 输出目录。最终必须进入 Jar。

### 翻译 key 规则

| 场景 | key 规则 |
|---|---|
| 模型字段 | `modelName.propertyName:原文=译文` |
| 服务元数据 | `modelName.serviceName:原文=译文` |
| 菜单 | `$menus.menuKey:原文=译文` |
| 视图 | `$views.viewKey:原文=译文` |
| 异常提示 | `modelName.serviceName:exceptionKey=译文` |
| 日志/提示 | `modelName.serviceName:logKey=译文` |
| 模型内通用 | `modelName:原文=译文` |
| App 内通用 | `原文=译文` |
| 全局通用 | `*.原文=译文` |

规则：

- 中文 demo 可直接写中文 displayName。
- 其他语种语言包中的 key 必须与中文语言包保持一致，只改 value。
- 自动生成 key 左侧不允许随意修改，否则翻译无法匹配。
- 手工补视图按钮文案时，按 `$views.{viewKey}.{按钮名}` 写，例如 `$views.test_form.新增=Add`。
- 不在异常中输出内部类名、SQL、堆栈。
- 同一业务词在模型、视图、菜单中保持一致。

### 代码中的翻译

服务中需要按当前上下文取翻译时，使用平台 Meta 翻译能力：

```java
String text = getMeta().l10n("books_manage.enable:success", bookCode);
```

非异常提示、非 HTTP 入口异常或工具类提示，业务服务需要自行调用翻译方法，不能假设前端会翻译后端返回值。

### 异常多语言

业务 App 可细化自身异常，但需要继承平台异常体系，便于 HTTP 请求自动拦截翻译。平台规范推荐继承 `com.sie.snest.engine.exception.AppException`；Demo 工程中若已有 `SdkAppException` 体系，以当前工程可编译异常基类为准，并保持动态参数占位符。

```java
public class BooksAppException extends AppException {
    public BooksAppException(String messageFormat, Object... args) {
        super(messageFormat, args);
    }
}
```

语言包示例：

```properties
books_manage.enable:book_disabled=图书%s已禁用
```

```properties
books_manage.enable:book_disabled=Book %s is disabled
```

### 数据多语言边界

- 元模型文案多语言是 backend 交付重点。
- 业务数据多语言需要国际化 App 的数据翻译能力，不建议在普通业务表中自行增加多套语言字段。
- 若模型开启数据多语言，需确认译文存储方式：统一字典表 `COMMON` 或单独字典表 `SINGLE`。
- 单独字典表命名建议 `i18n_dict_{model_name}`。
- 如果中文简体业务数据存在缓存，数据翻译页编辑译文后要配置后置服务清缓存或刷新派生数据。

后置服务支持的常见签名：

```java
public void clearCache() {}
public void clearCache(RecordSet rs) {}
public void clearCache(List<String> dataIds) {}
public void clearCache(RecordSet rs, List<String> dataIds) {}
```

---

## 登录、第三方登录、系统级变量

这些大多属于前端/平台配置。后端侧只在以下场景生成内容：

- 需要用户偏好、系统配置、业务设置时，生成配置模型或种子数据。
- 第三方登录需要后端回调或用户映射时，生成服务方法和安全校验。
- 系统级变量影响接口过滤或默认值时，在服务中读取上下文或配置中心。

---

## 生成检查清单

- 每个菜单和按钮都有合理 `auth`。
- 后端视图的 `name/type/auth` 层级正确，权限刷新后能在权限点中识别。
- 自定义写服务不只依赖前端隐藏，后端也校验。
- 菜单复用的数据范围由 `filter/config` 明确表达。
- 使用 `ignoreScope` 有明确原因。
- 用户可见文案一致，必要时配置多语言扫描。
- 国际化交付时存在 `messages_zh-CN.properties` 和目标语种语言包，key 集合一致。
- 视图、菜单、模型字段、服务、异常 key 命名符合本文件规则。
- 自定义语料使用 `messages_user_defined_xx-XX.properties`，并说明覆盖策略。
- 数据多语言涉及缓存时，有后置服务或刷新策略。
