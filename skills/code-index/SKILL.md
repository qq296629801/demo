---
name: code-index
description: |
  把存量代码（任意语言/框架）转成完整功能规格书（Codebook）。整合 codegraph 框架，
  以 codegraph_search、codegraph_callers、codegraph_trace 三条命令为核心，
  自动解析代码语义图谱，再通过 LLM Prompt 模板生成 SRS、PRD、HLA、API 文档、
  用户故事、流程图（Mermaid/SVG）、UI 静态页面、数据库结构等；识别到 IIDP 时还必须
  额外生成可被 brownfield 增量改造消费的 baseline-spec 结构化基线规格。
  支持 IIDP、RuoYi、JEECG Boot、yudao-cloud、maku-boot 等 Java 快速开发框架，
  以及通用 Spring Boot（JPA/MyBatis-Plus）、Python（Django/FastAPI）、
  TypeScript（NestJS/Express）、Go（Gin/Echo）、
  前端（React/Vue/Angular）的自动识别与模式提取。
  大型项目（模块 > 5）自动按模块拆分生成 codebook/modules/{module}/ 目录。
  当用户提到"代码转文档"、"生成规格书"、"分析存量项目"、
  "理解老项目代码"、"code to codebook"、"codegraph 分析"时必须使用本 skill。
---

# code-index — 代码转规格书 Skill

## 目录

1. [总体流程](#总体流程)
2. [第一步：安装与初始化 codegraph](#第一步安装与初始化-codegraph)
3. [第二步：核心命令](#第二步核心命令)
4. [第三步：框架自动识别](#第三步框架自动识别)
5. [第四步：规格书生成流程](#第四步规格书生成流程)
6. [输出规格书结构](#输出规格书结构)
7. [参考文件索引](#参考文件索引)

---

## 断点恢复

每次 Skill 被调用时，**首先**检查是否存在未完成的任务：

```
# 检查断点
如果 codebook/.progress.md 存在：
  Read("codebook/.progress.md")
  → 找第一个 [ ] 条目，从该步骤继续执行
  → 跳过所有 [x] 已完成条目，不重做
  → 无需重新执行 Phase A/B

如果 codebook/.progress.md 不存在：
  → 从 Phase A 重新开始
```

`.progress.md` 只保留断点恢复所需的最小任务清单，按模块列出待生成文件：

```markdown
# .progress.md — 生成进度跟踪

最后更新：{{TIMESTAMP}}
项目：{{PROJECT_NAME}}
框架：{{FRAMEWORK}}
模块总数：{{MODULE_COUNT}}

## 数据采集阶段
- [ ] Phase A：项目概况与框架识别
- [ ] Phase B：模块划分与输出路径确定
- [ ] Phase C：逐模块源码深挖与完整性核对

## 系统级文件
- [ ] codebook/hla.md
- [ ] codebook/database-overview.md
- [ ] codebook/overview.md

## IIDP baseline-spec 文件（仅 IIDP 项目）
- [ ] codebook/baseline-spec/apps.json
- [ ] codebook/baseline-spec/menus.json
- [ ] codebook/baseline-spec/models.json
- [ ] codebook/baseline-spec/views.json
- [ ] codebook/baseline-spec/services.json
- [ ] codebook/baseline-spec/frontend-extends.json
- [ ] codebook/baseline-spec/capability-list.json
- [ ] codebook/baseline-spec/artifact-map.json
- [ ] codebook/baseline-spec/trace-map.json
- [ ] codebook/baseline-spec/unresolved.json

## 模块级文件
- [ ] codebook/modules/{{MODULE}}/overview.md
- [ ] codebook/modules/{{MODULE}}/hla.md
- [ ] codebook/modules/{{MODULE}}/srs.md
- [ ] codebook/modules/{{MODULE}}/prd.md
- [ ] codebook/modules/{{MODULE}}/user-stories.md
- [ ] codebook/modules/{{MODULE}}/api.md
- [ ] codebook/modules/{{MODULE}}/database.md
- [ ] codebook/modules/{{MODULE}}/error-codes.md
- [ ] codebook/modules/{{MODULE}}/flowcharts/
- [ ] codebook/modules/{{MODULE}}/ui/
```

---

## 总体流程

```
代码仓库
   │
   ▼
codegraph init + index          ← tree-sitter 解析，写入 SQLite
   │
   ├─ codegraph_search          ← 全文搜索符号
   ├─ codegraph_callers         ← 找调用者（向上追踪）
   └─ codegraph_trace           ← 调用链追踪（向下展开）
   │
   ▼
框架识别 + 语义归类
   │
   ▼
LLM Prompt 模板 → 生成 Codebook
   │
   ├─ SRS（需求规格）
   ├─ PRD（产品需求）
   ├─ HLA（高阶架构）
   ├─ API 文档
   ├─ 用户故事
   ├─ 流程图（Mermaid）
   ├─ UI/UX 静态页面（HTML）
   └─ 数据库结构
```

---

## 第一步：安装与初始化 codegraph

> 详细安装步骤见 `references/codegraph-setup.md`

### 快速安装

```bash
# 全局安装
npm install -g @colbymchenry/codegraph

# 进入目标项目
cd /path/to/your-project

# 初始化并建立索引（-i = index immediately）
codegraph init -i

# 查看索引状态
codegraph status
```

> **跨平台一键安装**（Windows / macOS / Linux 均可，只需 Node.js ≥ 18）：
> ```bash
> node skills/code-index/scripts/install-codegraph.js /path/to/your-project
> ```
> Linux/macOS 也可使用 Shell 脚本：`bash skills/code-index/scripts/install-codegraph.sh /path/to/your-project`

### 配置 MCP（Claude Code 环境）

```json
// ~/.claude.json
{
  "mcpServers": {
    "codegraph": {
      "type": "stdio",
      "command": "codegraph",
      "args": ["serve", "--mcp"]
    }
  }
}
```

### 忽略不必要文件

在 `.codegraph/config.json` 中排除 `target/`、`node_modules/`、`dist/`，
大型 Java 项目特别要排除 `**.class` 和 Maven 缓存：

```json
{
  "exclude": [
    "target/**", "node_modules/**", "dist/**", "build/**",
    ".git/**", "*.min.js", "**/*.class"
  ],
  "extractDocstrings": true,
  "trackCallSites": true
}
```

---

## 第二步：核心命令

> 完整输出格式与字段说明见 `references/codegraph-setup.md`
>
> **铁律**：枚举/清点 → `codegraph_files`（确定完整）；定位单个已知符号 → `codegraph_search`（模糊、会截断）。

### `codegraph_search` — 符号定位（不是枚举器）

**用途**：当你**已知一个符号名**（类名/方法名）时，快速拿到它的位置和 node id，供 callers/callees/trace 使用。
**典型场景**：定位 `OrderService.createOrder` 拿 id 再追踪调用链。

```
# MCP 工具调用（Claude Code 内）
codegraph_search(query="UserController", kind="class", limit=10)
codegraph_search(query="login", kind="function")
codegraph_search(query="SysUser")        # 搜索相关符号
```

**输出字段**：`id`, `name`, `kind`, `file`, `line`, `signature`, `docstring`

> ⚠️ **关键限制：`codegraph_search` 是相关性排序的模糊搜索，且受 `limit` 截断。**
> 它**不是子串过滤器，不能用于"枚举某一类的全部符号"或计数**。实测：项目实有 35 个
> Controller，`codegraph_search("Controller", kind="class")` 只回 2 个；`kind="route"`
> 搜索也同样模糊+截断（`query="admin"` 回 100/195）。对任何 kind 都如此。
>
> **要"找出全部 / 清点数量"，一律改用 `codegraph_files`（文件枚举，确定且完整）+
> `codegraph_status`（各 kind 计数，作为完整性 oracle）。`codegraph_search` 只用于"查单个已知符号"。**

---

### `codegraph_files` — 文件枚举（穷举的唯一可靠工具）

**用途**：按目录 + 文件名 glob 精确列出**全部**匹配文件，一个不漏（走文件系统索引）。
**典型场景**：列出全部 Controller / Entity / Service / 前端页面，作为后续遍历 Read 的驱动清单。

```
# MCP 工具调用（推荐）
codegraph_files(path="src/main/java/.../system", pattern="*Controller.java", format="flat")
codegraph_files(path="src/api", pattern="*.ts", format="flat")
codegraph_files(path="src/views", pattern="*.vue", format="flat")
```

实测：`codegraph_files(pattern="*Controller.java")` 精确返回全部 35 个控制器（与 `find` 一致）。
这是**枚举/清点的唯一可靠入口**。CLI 的 `codegraph files --filter`（glob）实测可能返回空，
优先用 MCP `codegraph_files(pattern=...)`。

---

### codegraph 节点类型（kind）速查

| kind | 含义 | 用法 |
|------|------|------|
| `class` / `method` / `interface` | 结构符号 | 定位类/方法；计数看 `codegraph_status` |
| `route` | **后端 HTTP 端点**（Spring `@xxxMapping`、Express 等服务端注解抽取，**前端没有**）| 拿单条端点 file:line 很准，可**交叉核对**后端接口；但 search 截断，不能作唯一来源 |
| `component` | **前端组件**（Vue SFC / React 组件）| 定位单个组件；枚举页面仍用 `codegraph_files(pattern="*.vue")` |

> 端点穷举仍以"`codegraph_files` 枚举 Controller 文件 → 逐个 Read 解析 @xxxMapping"为准，
> 用 `codegraph_status` 的 `route` 计数（如 `route: 195`）兜底校验是否有遗漏。

---

### `codegraph_callers` — 向上追踪（谁调用了它）

**用途**：找某函数/方法的所有调用者，理解触发路径  
**典型场景**：理解一个 Service 方法被哪些 Controller 或定时任务触发

```
# MCP 工具调用
codegraph_callers(symbol_id="<node_id>", depth=3)
codegraph_callers(name="UserService.login")

# CLI 等价
codegraph query "UserService.login" --json | jq '.[0].id' | xargs ...
```

**输出**：调用者节点列表 + 调用位置（file:line）+ 调用边类型（direct/indirect）

---

### `codegraph_trace` — 调用链追踪（向下展开）

> codegraph 原生对应 `codegraph_callees` + `codegraph_impact`，
> 本 skill 将其封装为 **trace** 语义：从入口向下追踪完整调用链。

```
# MCP 工具调用（使用 codegraph_callees 实现 trace）
codegraph_callees(symbol_id="<node_id>", depth=5)
codegraph_impact(symbol_id="<node_id>")   # 变更影响面分析

# 组合使用：完整调用链
1. codegraph_search("OrderService.createOrder")  → 获得 node_id
2. codegraph_callees(node_id, depth=4)            → 向下展开调用树
3. codegraph_callers(node_id, depth=2)            → 向上找调用入口
```

**输出**：完整调用图 JSON，包含每层节点的 `name`、`file`、`signature`

---

## 第三步：框架自动识别

> IIDP 存量系统识别与提取规则见 `references/iidp-framework.md`
> Java 后端框架详细规则见 `references/java-frameworks.md`  
> 前端框架识别规则见 `references/frontend-frameworks.md`  
> Python/TypeScript/Go 后端框架规则见 `references/multi-framework-patterns.md`

**IIDP 项目优先识别：**

当后端出现 `com.sie.iidp`、`sie-snest-sdk`、`sie-snest-engine`、`sie-iidp-plugin`、
`apps/apps.json`、`**/app.json`、`@Model`、`@Property`、`@MethodService`、`BaseModel`
或 `views/*.json` / `menus.json` 时，判定为 IIDP 后端候选。

当前端 `package.json` 出现 `init:tech`、`install:tech`、`@tech/t-core`、`@tech/t-el-ui`、
`@tech/t-build`、`@tech/t-base`，或存在 `apps/*/common/index.js`、`apps/*/model-views/*.js`、
`apps/*/views/**/*.js`、`config/apps.json` 时，判定为 IIDP 前端候选。

识别为 IIDP 后，不要只按 Controller/API 生成 `ENDPOINT_LIST`。应按
`references/iidp-framework.md` 读取后端 `@Model/@Property/@MethodService`、`menus.json`、
`views/*.json`、`data/*.json`，以及前端 `model-views`、`extendView`、`hook`、`selector`、
`ds_config`，形成 `CAPABILITY_LIST`，再驱动 SRS/API/UI/流程图生成。

在生成任何 IIDP Codebook 之前，必须先执行 `references/iidp-framework.md` 的“输入形态识别与前后端配套关系”规则，判断当前输入是仅后端、仅前端、前后端配套，还是多工程混合。若识别为前后端配套，必须额外生成 `integration/frontend-backend-map.md`、`integration/contract-matrix.md`、`integration/unmatched-items.md`，不能只分别输出后端和前端规格。

前后端配套识别不得只按 app 名称、仓库名或目录名判定。app 一致只能产生候选配对；强配套必须继续命中至少两类非 app 证据，例如 `model -> @Model`、`service -> @MethodService/标准服务`、`selector.value -> menus.json.name`、`model-views.viewId -> menus.json.view/views/*.json`、字段 -> `@Property/view columns`。

识别到 IIDP 前端时，不能只输出扩展机制概览。必须额外输出面向正向生成链路的前端交互规格和实现契约，至少包含页面定位、目标节点、扩展类型、事件 `bind_on_*`、hook 点、数据源 `ds_config`、元服务调用 `app/model/service/args`、组件/弹窗/openView、状态变化、校验规则、生成文件建议。否则不能声称该 Codebook 可用于 `create-project` 正向生成前端代码。

识别到 IIDP 后，除 Markdown Codebook 外还必须生成 `codebook/baseline-spec/`。Markdown 面向人阅读，`baseline-spec` 面向后续 `iidp-brownfield-change`、`create-project`、backend/frontend 正向链路消费。不得只把 `srs.md` 或 `create-project-contract.md` 当作可执行基线；必须把 app、menu、model、view、service、frontend extension、capability、trace、unresolved 等事实归一化为结构化 JSON。

**Java 项目（通用注解发现，无需枚举框架名）：**

```
# 第一步：定位代表性源文件
codegraph_search("@RestController")  → Controller 文件路径
codegraph_search("@TableName")       → Entity 文件路径
codegraph_search("@Service")         → Service 文件路径

# 第二步：Read 这些文件，收集 import 包前缀
Read(Controller/Entity/Service 文件) → 提取所有 import 行
→ 排除 java.*、sun.*，保留第三方包前缀

# 第三步：对比 java-frameworks.md 包前缀映射表
→ com.baomidou.mybatisplus → MyBatis-Plus
→ io.swagger.v3.oas.annotations → SpringDoc
→ cn.dev33.satoken → Sa-Token
→ org.springframework.cloud.openfeign → OpenFeign/微服务
→ 未命中包前缀 → 按注解名称模式语义推断
```

**Python 后端：**

```
codegraph_search("models.Model")     → Django ORM
codegraph_search("APIRouter")        → FastAPI
```

**TypeScript / Go 后端：**

```
codegraph_search("@Controller")      → NestJS
codegraph_search("router.GET")       → Gin/Echo（Go）
```

**前端框架检测序列：**

```
# 优先读取 package.json 判断框架（详细规则见 references/frontend-frameworks.md）
Read("package.json") → 对比依赖键映射表
  → vue → Vue 3/2；react + react-dom → React；@angular/core → Angular
  → pinia/vuex → 状态管理；vue-router/react-router-dom → 路由
  → axios → HTTP 层；element-plus/antd → UI 组件库

# package.json 缺失时退回符号搜索（仅用于"判断用了哪个框架"，不要用它枚举 store/页面）
codegraph_search("defineStore")         → Vue + Pinia
codegraph_search("createSlice")         → React + Redux
codegraph_search("@NgModule")           → Angular
# 确认框架后，枚举页面/store/api 一律用 codegraph_files(pattern=...)，见 Phase CF
```

识别框架后，使用对应的 **框架提取规则** 理解代码分层，再进入第四步。

**框架识别失败的兜底处理：**

```
# 当 package.json 不存在且符号搜索也无法命中任何已知框架时
→ 在 codebook/overview.md 中注明"技术栈未能自动识别"
→ 列出扫描到的文件扩展名分布（.java / .py / .go / .ts / .vue 等）
→ 列出顶层目录结构（Linux/macOS: `ls -d */`，Windows: `Get-ChildItem -Directory`，或用 `codegraph_files` 工具代替，跨平台推荐）
→ 由人工根据目录结构和文件扩展名判断框架类型，再手动指定重新运行
```

---

## 第四步：规格书生成流程

> LLM Prompt 模板详见 `references/llm-prompts.md`。
> 本文件是 Codebook 输出结构与断点恢复契约的唯一来源，不再维护独立的 `codebook-templates.md`。

### 4.1 三条核心命令的职责分工

| 命令 | 用途 | 驱动哪些 Codebook |
|------|------|-------------|
| `codegraph_files` | 枚举各层文件全集（Controller/Entity/页面/api），作为遍历驱动清单 | 模块划分、ENDPOINT_LIST、完整性校验 |
| `codegraph_search` + `Read` | 定位已知类文件，读取源码提取字段/注解 | API 字段校验矩阵、DDL、错误码表 |
| `codegraph_callers` | 向上追踪——发现定时任务/MQ/事件等非 Controller 触发点 | SRS 触发条件、HLA 异步架构 |
| `codegraph_trace` | 两点间完整调用路径（Controller → Mapper）| 模块 `flowcharts/` 业务流程图骨架 |

### 4.2 数据采集流程（Phase A → D）

**Phase A：项目概况**

```
codegraph status → 记录总量基线（后续用于完整性校验）：
  文件总数 F、节点总数 N、各 kind 计数（class 数 C、route 数 R 等）
  语言分布（判断是 Java/Python/Go/TS/前端）

# 用 codegraph_files 枚举各层文件（禁止用 codegraph_search 枚举——它会截断、漏掉大半）
codegraph_files(path=源码根, pattern="*Controller.java", format="flat") → 控制器全集
codegraph_files(pattern="*ServiceImpl.java", format="flat")            → 服务全集
codegraph_files(pattern="*DO.java",          format="flat")            → 实体全集（MyBatis-Plus）
codegraph_files(pattern="*Entity.java",      format="flat")            → 实体全集（JPA）

# 完整性校验（用文件数与状态计数对账，不再用 search 之和）：
#   codegraph_files 返回的 Controller 数 = 期望控制器数（确定值，非估算）
#   与 codegraph_status 的 class 计数 C 互相印证；如某层为空，确认是命名差异还是真的没有
```

**Phase B：模块划分**

```
按包路径/子目录将 Controller 分组为业务模块

模块识别规则（按语言）：
  Java：取 Controller 类完整包名的第三层
        com.company.project.{module}.controller → 模块名为 {module}
  前端：src/views/{module}/ 或 src/pages/{module}/ 的一级目录名
  Python：apps/{module}/ 目录名 或 routers/{module}.py 文件名前缀
  Go：handler/{module}/ 或 controller/{module}/ 目录名

所有项目统一采用模块化输出目录：系统级文档放在 `codebook/` 顶层，模块级详细设计放在 `codebook/modules/{module}/`。

# Phase B 完成后立即写入进度文件（断点恢复的基础）
Write("codebook/.progress.md") → 写入完整任务清单（见本文件"断点恢复"章节的 `.progress.md` 最小模板）
  - Phase A/B 标记为 [x]，所有 Phase C/D 条目标记为 [ ]
  - 列出所有预期生成的 Codebook 文件路径
```

**Phase C：逐模块深挖（对每个模块执行以下步骤）**

```
【C1 — 遍历模块内所有 Controller 文件，建立接口功能清单（ENDPOINT_LIST）】

# Step 1：用文件枚举找出本模块所有 Controller 文件（精确、不漏）
codegraph_files(path="…/{module}/…/controller", pattern="*Controller.java", format="flat")
  → 本模块控制器全集（≥1 个）；按模块包目录过滤，确定值
# 不要用 codegraph_search("{module}*Controller") —— 会模糊截断，漏掉大半

# Step 2：对每个 Controller 文件执行 Read（不得跳过任何一个）
for each ControllerFile in 文件列表：
  Read(ControllerFile) → 从源码中提取所有 endpoint：
    - HTTP 方法 + 路径（@GetMapping / @PostMapping / @PutMapping / @DeleteMapping）
    - 方法名 + @Operation/@ApiOperation 注解描述
    - @PreAuthorize / @SaCheckPermission 权限码

# Step 2.5：route 节点交叉校验（发现 Read 可能漏掉的端点）
codegraph_search("{模块关键词}", kind="route", limit=300) → 拿到该模块部分端点的 file:line
  → 与 Step 2 解析出的 ENDPOINT_LIST 比对：route 里有、ENDPOINT_LIST 没有的 → 复核对应 Controller
  → 全局兜底：ENDPOINT_LIST 总数应与 codegraph_status 的 route 计数大致吻合（route 含 app/admin 多端）

# Step 3：汇总为本模块「接口功能清单」（ENDPOINT_LIST）
ENDPOINT_LIST = [
  { method: "POST",   path: "/system/user/create",  name: "创建用户",   permission: "system:user:create" },
  { method: "PUT",    path: "/system/user/update",  name: "更新用户",   permission: "system:user:update" },
  { method: "DELETE", path: "/system/user/{id}",    name: "删除用户",   permission: "system:user:delete" },
  { method: "GET",    path: "/system/user/list",    name: "查询用户列表", permission: "system:user:list"   },
  ...（穷举，不遗漏）
]
→ ENDPOINT_LIST 是后续 SRS/API/流程图/UI 遍历生成的驱动依据
→ 将"本模块接口数 = N"记录到 codebook/.progress.md

# Step 4：读取其余关联源码
codegraph_search("XxxSaveReqVO") / codegraph_search("XxxRespVO")
  → 获取 VO/DTO 文件路径
Read(VO file) → 提取字段类型、@NotBlank/@Pattern/@Size/@Schema 注解 → 字段校验矩阵

codegraph_search("XxxDO" / "XxxEntity", kind="class")
Read(Entity file) → @TableName/@Table 表名、字段类型长度、@Column 约束、@Index 定义

codegraph_search("ErrorCodeConstants") / codegraph_search("*ErrorCode.java")
Read(错误码文件) → 错误码编号 + 常量名 + 中文描述

# Step 5：打包 Prompt 8（UI）所需变量 → UI_CONTEXT（Phase D 直接注入，无需重新查找）
UI_CONTEXT[{module}] = {
  LIST_FIELDS:  来自 RespVO 或 Entity 字段列表（作为列表页的表格列）
  QUERY_FIELDS: 来自 Controller @RequestParam 或 *QueryReqVO 字段（作为搜索栏条件）
  FORM_FIELDS:  来自 CreateReqVO/UpdateReqVO 字段 + @NotBlank/@Size 等校验注解（作为表单字段）
  OPERATIONS:   来自 ENDPOINT_LIST 权限码，归类为：新增/编辑/删除/导出/导入
}

【C2 — 按 ENDPOINT_LIST 遍历触发点（写操作优先）】
for each endpoint in ENDPOINT_LIST where method in [POST, PUT, DELETE, PATCH]：
  codegraph_callers("{ServiceImpl}.{对应Service方法名}")
    → 找出：Controller / @Scheduled 定时任务 / @EventListener 事件 / MQ Consumer
    → 补充到 SRS"触发方式"和 HLA"异步流程"节点
# 注：GET 查询接口通常无异步触发，可跳过；如有疑问则同样执行

【C3 — 按 ENDPOINT_LIST 遍历调用链，每个写操作生成一条 trace 骨架】
for each endpoint in ENDPOINT_LIST where method in [POST, PUT, DELETE, PATCH]：
  codegraph_trace(from="{Controller}.{methodName}", to="{Mapper}.{insertOrUpdate}")
    → Controller → Service → Mapper 完整调用路径
    → 发现事务边界、权限校验、缓存访问、消息发送等横切关注点
    → 记录为流程图 {operation}-{entity}-flow.mmd 的节点序列
GET 列表/详情查询：执行一次代表性 trace，作为 query-{entity}-flow.mmd 的骨架
```

**Phase C 补充步骤 CF（识别到前端框架时追加执行，全栈项目在 C1-C3 后继续）：**

```
CF1. 前端页面与路由（注意：前端没有 route 节点，vue-router 是 TS 配置对象，不能用 route 搜索）
     a) 页面清单：codegraph_files(path="src/views", pattern="*.vue", format="flat") → 穷举页面
        （实测 src/views/system/user 精确返回 7 个）
     b) 静态路由：Read(src/router/modules/*.ts) → 配置数组里的 path + component + meta
     c) 动态路由识别：检测 src/store/modules/permission.ts 是否有 generateRoutes()、
        是否从后端菜单接口（/system/menu 等）构建路由。若是 →
        注明"业务路由运行时由后端菜单生成，页面↔路由映射需结合后端菜单种子数据"
     → 输出字段供模块 `overview.md`、`hla.md` 和 `ui/` 原型使用

CF2. Pinia/状态管理 store
     codegraph_files(path="src/store/modules" 或 "src/stores", pattern="*.ts", format="flat") → store 全集
     for each store 文件：Read → store name、state 字段类型、actions 方法签名
     → 输出字段供模块 `hla.md` 使用

CF3. API 服务层
     codegraph_files(path="src/api", pattern="*.ts", format="flat") → API 模块全集
        （实测 src/api/system 精确返回 28 个）
     for each API 文件：Read → 提取 request.get/post/put/delete({url}) → 端点 + 参数 + 返回类型
     → 与后端 ENDPOINT_LIST 做前后端契约对照
     → 作为 {{FRONTEND_API_SOURCE}} 变量注入 Prompt 5（codebook/modules/{module}/api.md 前端调用对照）

CF4. codegraph_callers("useUserStore" / "usePermissionStore")
     → 找出依赖权限 Store 的页面/组件 → 权限守卫文档

CF5. codegraph_trace(from="LoginPage.submit", to="useUserStore.setToken")
     → 登录流程调用链 → 前端流程图
```

**Phase C'：完整性核对（Phase C 结束后，Phase D 开始前）**

```
for each 模块 in 模块列表：
  ✓ 已 Read 的 Controller 数 == codegraph_files(pattern="*Controller.java") 返回数（全部遍历，非仅第一个）
  ✓ ENDPOINT_LIST 已建立，条目数 == Controller 源码中
    @GetMapping/@PostMapping/@PutMapping/@DeleteMapping 注解总数
    （允许 ±1 误差，处理 @RequestMapping 在类级别的情况）
    并与 codegraph_status 的 route 计数对账，明显差异需逐 Controller 复核
    如不一致 → 重新 Read 遗漏的 Controller 文件，补全 ENDPOINT_LIST
  ✓ Entity/DO 文件已 Read（已提取 ≥ 1 个 @TableName 或 @Entity）
  ✓ ErrorCodeConstants 已 Read（或确认本模块无错误码文件）
  ✓ codegraph_callers 已对 ENDPOINT_LIST 中每个 POST/PUT/DELETE 接口的 Service 方法执行
  ✓ codegraph_trace   已对 ENDPOINT_LIST 中每个 POST/PUT/DELETE 接口执行（每写操作一条 trace）

如果识别到前端框架（FRONTEND_FRAMEWORK != "无"）：
  ✓ 页面已用 codegraph_files(path="src/views", pattern="*.vue") 枚举；路由配置（router/modules）
    或动态路由生成（permission.ts generateRoutes）已识别（CF1）
  ✓ Store 文件已用 codegraph_files 枚举并 Read（CF2，已提取 ≥ 1 个 store）
  ✓ API 服务文件已用 codegraph_files 枚举并 Read（CF3，已提取 HTTP 调用，将作为 {{FRONTEND_API_SOURCE}}）

如果某模块未通过：
  → 补充执行缺失步骤 → 重新检查 → 通过后才继续
  → 更新 codebook/.progress.md 的完整性校验表格（含"接口数"列）

全部模块通过 → 进入 Phase D
```

**Phase D：生成规格书**

```
每个模块独立生成，按顺序：overview → hla → srs → prd → user-stories → api（含前端调用对照，如有 CF3 数据）→ database → error-codes → ui → flowcharts
最后生成系统级文档：hla → database-overview → overview

# SRS 生成规则：
# 将 ENDPOINT_LIST 作为 FR 清单骨架传入 Prompt 2
# 要求 LLM 为每个 endpoint 生成一个独立功能需求条目（不得合并多个接口为一个功能）
# srs.md 中功能需求条目数必须 == ENDPOINT_LIST 长度（Phase D 结束后校验）

# API 文档生成规则：
# 将 ENDPOINT_LIST 作为接口清单骨架传入 Prompt 5
# 要求 LLM 为每个 endpoint 生成一个独立章节（### METHOD /path）
# api.md 中接口章节数必须 == ENDPOINT_LIST 长度（Phase D 结束后校验）

# 流程图生成规则（flowcharts/）：
# for each endpoint in ENDPOINT_LIST where method in [POST, PUT, DELETE, PATCH]：
#   生成 codebook/modules/{module}/flowcharts/{operation}-{entity}-flow.mmd（用 C3 trace 数据驱动）
# GET 列表查询/详情查询：生成一个 query-{entity}-flow.mmd
# 最少生成：N 个写操作 → N 个 .mmd 文件（不得将多个操作合并为 1 个流程图）

# ui/ 生成规则（所有项目均生成，不限于前端项目，只要有业务 Entity 和 CRUD 接口即生成）：
# 将 UI_CONTEXT[{module}]（Phase C C1 已打包）注入 Prompt 8
# for each 业务 Entity in 模块（来自 C1 的 Entity 读取结果）：
#   生成 codebook/modules/{module}/ui/{entity}-list.html
#   按状态字段、子表、关键流程补充 {entity}-detail.html / {entity}-workflow.html
#   每个模块固定生成 codebook/modules/{module}/ui/dashboard.html
# 多个 Entity 不合并，各自独立生成

# 流程图 SVG 渲染规则（在所有 .mmd 写完后执行）：
# for each .mmd 文件（已 Write 完毕）：
#   show_widget(mermaid_code) → 渲染为 SVG 图像
#   Write("codebook/modules/{module}/flowcharts/{name}.svg", svg_content)
# 每个 .mmd 对应一个同名 .svg，两者成对出现

# 关键：每生成一个文件后，立即更新 codebook/.progress.md 对应条目为 [x]
# 如果 context 在生成过程中溢出，该文件仍标记为 [ ]（未完成），恢复后重做
# 宁可重做一个文件，不要留下内容不完整的半成品

# 所有文件生成完成后：
# 在 codebook/overview.md 末尾追加一行：
# > 生成完成：{时间戳}，覆盖 {N} 个模块，{M} 张数据表，{P} 个 API 接口
```

**Phase D-IIDP：生成 baseline-spec（仅 IIDP 项目）**

```
如果 FRAMEWORK == IIDP：
  在 Markdown Codebook 生成后，立即生成 codebook/baseline-spec/ 下的结构化 JSON。
  baseline-spec 不从 Markdown 反抽；必须直接使用 Phase A/C 中读取到的源码、JSON 配置、前端扩展和配套矩阵事实。

  必须输出：
    apps.json
    menus.json
    models.json
    views.json
    services.json
    frontend-extends.json
    capability-list.json
    artifact-map.json
    trace-map.json
    unresolved.json

  每个规格项必须包含：
    id                稳定规格 ID，例如 model.example_student / view.demo_example_class_grid
    kind              app/menu/model/view/service/frontendExtend/capability 等
    sourceFile        来源文件路径；无法定位时为 null 并写入 unresolved.json
    sourceLine        来源行号；无法定位时为 null
    confidence        high/medium/low
    status            confirmed / inferred / needs-confirmation / placeholder
    unresolvedReason  status 不是 confirmed 时必须填写

  生成原则：
    - baseline-spec 是当前代码事实，不描述新需求。
    - baseline-spec 默认只读，后续开发者应修改 delta-spec，而不是直接修改 baseline-spec。
    - capability-list.json 必须由菜单、视图按钮、@MethodService、标准服务、前端扩展、hook 合并生成。
    - trace-map.json 必须能把 capability/model/view/service/frontendExtend 回连到源码文件。
    - unresolved.json 必须集中收录无法确认的 app/model/service/nodeId/权限/字段/运行时节点。
```

### 4.3 语义归类

将采集到的符号按业务模块归类：
- **用户权限模块** → sys_user / SysUserController / LoginController
- **业务核心模块** → 业务相关 Entity + Service + Controller
- **数据访问层** → Mapper/Repository + DB 表结构
- **公共基础设施** → 配置类、工具类、拦截器、MQ 消费者
- **前端页面层** → Pages + Router + Store + API Client

### 4.4 输入 LLM Prompt → 生成各类 Codebook

每种文档对应一个 Prompt 模板（见 `references/llm-prompts.md`）。
按如下顺序生成，后面的文档依赖前面的输出：

```
模块 overview → 模块 HLA → SRS → PRD → 用户故事 → API 文档（含字段校验矩阵）→ 数据库结构 → 错误码表 → UI/UX → 流程图 → 系统 HLA → 系统数据库总览 → 系统 overview
```

---

## 输出规格书结构

### 统一模块化结构

```
codebook/
├── .progress.md                断点恢复与生成进度
├── overview.md                 系统级概览、完整文档导航、模块索引、生成统计
├── hla.md                      系统级架构、模块拓扑、跨模块依赖、部署/安全总览
├── database-overview.md        系统级数据模型总览、跨模块 ER 关系
├── baseline-spec/              IIDP 项目的结构化基线规格（仅识别到 IIDP 时必须输出）
│   ├── apps.json               App 清单、装载方式、前后端 app 配对候选
│   ├── menus.json              菜单、页面入口、绑定模型和视图
│   ├── models.json             @Model/@Property/@Validate/@Dict/@Selection/关系
│   ├── views.json              后端 views/*.json 与前端 model-views 的统一视图规格
│   ├── services.json           @MethodService、标准元服务、传统 Controller 服务
│   ├── frontend-extends.json   selector/type/view/beforeOperate/hook/ds_config/bind_on_*
│   ├── capability-list.json    现有能力清单，作为 brownfield 差异规格的基线
│   ├── artifact-map.json       规格项到推荐修改文件类型/目标路径的映射
│   ├── trace-map.json          规格项到 sourceFile/sourceLine 的追踪映射
│   └── unresolved.json         待确认、占位、低置信度和运行时依赖项
└── modules/
    ├── {module}/
    │   ├── overview.md         模块概览、模块内导航、功能入口清单
    │   ├── hla.md              模块架构、模块调用图、依赖图、前置条件
    │   ├── srs.md              模块软件需求规格（详细功能需求）
    │   ├── prd.md              模块产品需求文档
    │   ├── user-stories.md     模块用户故事与验收标准
    │   ├── api.md              模块 API（字段校验矩阵 + 前端调用对照）
    │   ├── database.md         模块 DDL + ER 图 + 索引定义
    │   ├── error-codes.md      模块错误码表
    │   ├── flowcharts/         模块流程图（.mmd + .svg 成对出现）
    │   └── ui/                 模块 HTML 原型（dashboard/list/detail/workflow）
    └── ...
```

> 顶层文件只描述整个系统的架构、系统功能模块、跨模块依赖与导航；模块目录内才写详细功能设计、PRD、用户故事、API、UI 和流程图。
> 前端 API 调用对照内容并入模块 `api.md` 的"前端调用层对照"章节；页面/路由/状态信息并入模块 `overview.md`、`hla.md` 和 `ui/` 原型，不再生成顶层 10-13 号文件。

### 输出格式规则

| 文档类型 | 格式 | 工具 |
|---------|------|------|
| SRS/PRD/HLA | Markdown | 直接写 .md |
| 流程图 | Mermaid 代码块 + SVG | `show_widget` 渲染 |
| API 文档 | OpenAPI 3.0 YAML/Markdown | 写 .md 或 .yaml |
| 数据库结构 | SQL DDL + Mermaid ER 图 | 直接输出 |
| UI/UX | 静态 HTML + Tailwind CSS | 写 .html 文件 |
| 用户故事 | Markdown 表格 | 直接写 .md |
| IIDP baseline-spec | JSON | 直接从源码事实归一化输出，不从 Markdown 反抽 |

---

## 参考文件索引

| 文件 | 何时读取 |
|-----|---------|
| `references/codegraph-setup.md` | 安装/初始化/CLI 命令完整参考 |
| `references/iidp-framework.md` | 识别到 IIDP 存量系统时必读（元模型、菜单/视图、前端扩展、hook）|
| `references/java-frameworks.md` | 识别到 Java 项目时必读（Spring 生态注解发现）|
| `references/frontend-frameworks.md` | 识别到前端项目时必读（Vue/React/Angular via package.json）|
| `references/multi-framework-patterns.md` | 识别到 Python/TS/Go 后端项目时必读 |
| `references/llm-prompts.md` | 生成任何 Codebook 文档前必读 |
| `scripts/install-codegraph.sh` | 批量安装脚本 |
