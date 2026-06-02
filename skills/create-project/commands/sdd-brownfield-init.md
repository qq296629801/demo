---
description: 【存量工程初始化】先通过 /sdd-init-templates 生成项目宪法模板，再用 code-index 分析代码提取真实版本号和命名规范，填入生成的模板文件。
handoffs:
  - label: 补全其余宪法模板
    command: sdd-init-templates
    prompt: 继续初始化其余宪法模板（mission、roadmap、integration-map）
    send: false
---

# /sdd-brownfield-init

## 用途

为已经存在的 IIDP 工程初始化存量项目宪法。命令通过 `/code-index` 和 CodeGraph 侦察现有代码，把真实版本号、命名规范、视图/菜单/数据规则和现有 App 清单填入 `specs/iidp-stack.md`。

本命令遵循“先生成模板，再填入真实数据”的两阶段设计，并在最后生成正式宪法文件：

1. **第一阶段**：调用 `/sdd-init-templates iidp-stack`，将 `sdd-constitution.md` 中的 `iidp-stack.md` 默认模板复制到 `specs/templates/iidp-stack.md`。
2. **第二阶段**：通过 `/code-index` 分析现有代码，把真实值填入 `specs/templates/iidp-stack.md`，替换 `[待确认]` 占位符。
3. **第三阶段**：基于填充完成的项目级模板，生成 `specs/iidp-stack.md`。

## 用户输入

```text
$ARGUMENTS
```

通常不需要参数。若用户提供项目根路径，先切换或提示用户切换到该路径后执行本命令。

## 执行步骤

### 步骤 1 — 确认工作目录

检查当前目录是否为 IIDP 项目根：

- 存在 `pom.xml` → 可作为 Maven 聚合项目根。
- 存在 `apps/apps.json` → 可作为 IIDP App 注册根。
- 两者都不存在 → 停止执行，提示用户 `cd` 到正确目录。

输出示例：

```markdown
当前目录不是 IIDP 项目根：未发现 `pom.xml` 或 `apps/apps.json`。
请切换到包含聚合 POM 或 App 注册文件的目录后重新运行 `/sdd-brownfield-init`。
```

### 步骤 2 — 调用 `/sdd-init-templates iidp-stack`

执行：

```text
/sdd-init-templates iidp-stack
```

目标是在 `specs/templates/` 目录下生成 `iidp-stack.md`，并保留顶部项目级模板说明注释。此文件即后续填充真实值的目标文件。

若 `specs/templates/iidp-stack.md` 已存在，先询问用户是否覆盖。用户不覆盖时，在现有文件上填充可识别的 `[待确认]` 占位符，不删除用户已有内容。

### 步骤 3 — 运行 `/code-index` 并提取版本信息

按 `skills/code-index/SKILL.md` 的 Phase A 流程执行：

```bash
codegraph init -i
codegraph status
```

`codegraph status` 输出的文件数、节点数作为后续查询完整性基线。

#### 后端版本（Maven POM）

优先用 CodeGraph 搜索定位含版本信息的 POM，再读取对应文件提取具体值：

```text
codegraph_search("sie-snest-engine")          → 定位含此 artifactId 的 pom.xml
codegraph_search("sie-snest-sdk")             → 定位 SDK 版本声明
codegraph_search("sie-iidp-sdk")              → 定位 IIDP SDK 版本声明
codegraph_search("sie-snest-maven-plugin")    → 定位 Maven 插件版本
codegraph_search("spring-boot.version")       → 定位 Spring Boot 版本 property
codegraph_search("maven.compiler.source")     → 定位 Java 版本
codegraph_search("xxl-job-core")              → 定位 XXL-Job 版本（如存在）
```

若 `codegraph_search` 对 XML 符号搜索无结果，直接读取项目根 `pom.xml`；如根 POM 使用 `<module>`、`<parent>`、`<dependencyManagement>` 或 profile，再读取相关子 POM。按 `<artifactId>`、`<version>`、`<properties>` 标签提取真实版本号。

#### 前端版本（package.json）

参考 `skills/code-index/SKILL.md § 第三步：框架自动识别`，优先读取项目根 `package.json`，提取：

- IIDP 前端框架版本：依赖键匹配 `@sie/iidp-*`。
- Vue 版本：依赖键 `vue`。
- Element UI 版本：依赖键 `element-plus` 或 `element-ui`。
- 构建工具版本：依赖键 `webpack`、`vite` 或项目实际使用工具。

若 `package.json` 不在项目根目录，用以下搜索定位前端源文件，再从源文件目录向上查找最近的 `package.json`：

```text
codegraph_search("defineComponent")
codegraph_search("createApp")
```

找不到前端工程时，将前端框架填为 `N/A`，并在待确认项说明“未发现 package.json 或前端扩展工程”。

### 步骤 4 — 提取模型命名规范

参考 `skills/backend/references/core/model.md`，使用 CodeGraph 定位业务模型：

```text
codegraph_search("@Model", kind="class")
codegraph_search("BaseModel", kind="class")
```

读取每个候选 Java 文件，提取并归纳：

- `@Model(name = "...")` → 模型 name 规则，例如 `{app_prefix}_{entity}`。
- `@Model(tableName = "...")` → 表名规则，例如 `{entity_snake_case}`。
- Java 包名 → appPkg 规则，例如 `com.sie.iidp.{domain}.{feature}.model`。
- Maven 模块目录名 → 模块命名规则，例如 `sie-iidp-demo-{feature}`。

同时扫描服务方法设计规律：

```text
codegraph_search("@MethodService", kind="method")
```

读取样本方法，提取：

- `description` 的命名风格：内置名、中文描述或英文描述。
- 参数类型规律：何时使用 `RecordSet`，何时使用 DTO，何时使用简单类型。
- 返回类型规律：`RecordSet`、实体、集合、`void` 等。

若项目存在多套规则，不强行合并为一个结论；在 `命名规范` 表中写主流规则，并把例外样本加入待确认项或备注。

### 步骤 5 — 提取视图、菜单、种子数据、字典数据命名规范

参考以下文件：

- `skills/backend/references/core/view.md`
- `skills/backend/references/core/menu.md`
- `skills/backend/references/core/seed-data.md`
- `skills/backend/references/core/app-json.md`

CodeGraph 用于定位文件路径；若搜索不到 JSON 字面量，直接用文件路径和目录结构读取。

#### 视图 JSON

```text
codegraph_search("_view.json")
codegraph_search("_grid")
codegraph_search("_search")
codegraph_search("_form")
```

读取 1-2 个代表性 `*_view.json` 文件，提取：

- 视图 key 命名规则，例如 `{model_name}_grid` 或 `{prefix}_{model}_{type}`。
- views 根结构，例如 `{ "views": { ... } }`。
- grid/search/form 的组织方式和是否同文件承载。

#### 菜单 JSON

```text
codegraph_search("menus.json")
```

读取每个 `menus.json`，提取：

- 功能菜单 key 规则，例如 `{app_prefix}_{entity}_menu`。
- 根菜单 key 规则，例如 `{app_prefix}_{module}_root_menu`。
- `parent_ids` 引用模式，特别是 `@ref` 写法。

#### 种子数据 JSON

```text
codegraph_search("data/*.json")
```

读取 1-2 个 `data/` 目录下的种子数据文件，提取：

- 文件命名规则，例如 `{model_name}.json`。
- 根结构，例如 `{ "data": { ... } }`。
- 是否按模型、业务模块或初始化批次拆分文件。

#### 字典种子数据

```text
codegraph_search("_dict.json")
codegraph_search("base_dict_type")
codegraph_search("base_dict_value")
```

读取字典种子文件，提取：

- 字典 `typeCode` 与 `@Dict(typeCode = "...")` 的对应关系。
- 字典文件命名规则，例如 `{typeCode}_dict.json`、`yes_no.json` 或项目实际命名。

#### app.json

```text
codegraph_search("app.json")
```

读取每个 `app.json`，提取：

- `resolved` 包路径规则。
- `view` 数组路径格式，例如 `"{moduleName}/views/{model_name}_view.json"`。
- `data` 数组路径格式。
- App `name` 命名规则，例如 `sie-iidp-demo-{feature}`。
- App 内 `dependencies` 或类似依赖声明。

### 步骤 6 — 提取现有 App 清单

读取 `apps/apps.json`，提取已注册 App 列表：

- appName。
- resolved 包名或 jar/module 标识。
- 依赖关系。
- SDK/App 分组（如项目中区分 `apps.SDK`、业务 App、平台 App）。

若 `apps/apps.json` 不存在但存在多个 `app.json`，从各 App 的 `app.json` 反推 App 清单，并将“未发现 apps/apps.json”列入待确认项。

### 步骤 7 — 填充 `specs/templates/iidp-stack.md`

将步骤 3-6 提取的真实值写入 `specs/templates/iidp-stack.md`：

- `项目类型` → `存量接入（brownfield）`。
- `初始化方式` → `/sdd-brownfield-init` 通过 code-index 从现有代码自动提取。
- `初始化时间` → 当前日期。
- `版本约束` → 填入真实版本；未发现的填 `N/A` 或保留 `[待确认]` 并说明原因。
- `命名规范` → 填入主流规则、示例和来源。
- `现有 App 清单` → 填入 `apps/apps.json` 或 `app.json` 提取结果。

无法自动推导的项保留 `待确认`，并在文件内或输出摘要中附注原因。不得编造版本号、权限码、视图 key、菜单 key 或 App 依赖。

### 步骤 8 — 生成 `specs/iidp-stack.md` 并输出侦察摘要

基于已填充的项目级模板生成最终文件：

```text
specs/iidp-stack.md
```

输出摘要：

```markdown
## 存量工程侦察完成 ✓

### 已生成文件
- specs/templates/iidp-stack.md（项目级模板，已填充真实值）
- specs/iidp-stack.md（项目宪法 iidp-stack 节）

### 提取结果摘要
- IIDP 引擎版本：[版本]  SDK 版本：[版本]
- Spring Boot：[版本] / Java：[版本]
- 模型 name 规则：[规则]  表名规则：[规则]
- 视图 key 规则：[规则]  菜单 key 规则：[规则]
- 发现 App 数：[N] 个

### 待确认项
- [无法自动推导的项，标注原因]

### 后续选项
A) 继续初始化其余宪法模板：/sdd-init-templates mission roadmap integration-map
B) 直接进入 sdd-brownfield.md 步骤 2，补全 mission.md / roadmap.md
C) 手动修正 specs/templates/iidp-stack.md 中的待确认项
```

## 完成标志

- `specs/templates/iidp-stack.md` 已存在，且 `项目类型` 为 `存量接入（brownfield）`。
- `specs/iidp-stack.md` 已生成。
- 已提取或明确标注待确认的版本约束、命名规范和现有 App 清单。
- 输出摘要包含已生成文件、提取结果、待确认项和后续选项。
