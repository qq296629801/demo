---
name: code-index
description: |
  把存量代码（任意语言/框架）转成完整功能规格书（Spec）。整合 codegraph 框架，
  以 codegraph_search、codegraph_callers、codegraph_trace 三条命令为核心，
  自动解析代码语义图谱，再通过 LLM Prompt 模板生成 SRS、PRD、HLA、API 文档、
  用户故事、流程图（Mermaid/SVG）、UI 静态页面、数据库结构等。
  支持 RuoYi、JEECG Boot、yudao-cloud、maku-boot 等 Java 快速开发框架，
  以及通用 Spring Boot（JPA/MyBatis-Plus）、Python（Django/FastAPI）、
  TypeScript（NestJS/Express）、Go（Gin/Echo）、
  前端（React/Vue/Angular）的自动识别与模式提取。
  大型项目（模块 > 5）自动按模块拆分生成 spec/modules/{module}/ 目录。
  当用户提到"代码转文档"、"生成规格书"、"分析存量项目"、
  "理解老项目代码"、"code to spec"、"codegraph 分析"时必须使用本 skill。
---

# code-index — 代码转规格书 Skill

## 目录

1. [总体流程](#总体流程)
2. [第一步：安装与初始化 codegraph](#第一步安装与初始化-codegraph)
3. [第二步：三条核心命令](#第二步三条核心命令)
4. [第三步：框架自动识别](#第三步框架自动识别)
5. [第四步：规格书生成流程](#第四步规格书生成流程)
6. [输出规格书结构](#输出规格书结构)
7. [参考文件索引](#参考文件索引)

---

## 断点恢复

每次 Skill 被调用时，**首先**检查是否存在未完成的任务：

```
# 检查断点
如果 spec/.progress.md 存在：
  Read("spec/.progress.md")
  → 找第一个 [ ] 条目，从该步骤继续执行
  → 跳过所有 [x] 已完成条目，不重做
  → 无需重新执行 Phase A/B

如果 spec/.progress.md 不存在：
  → 从 Phase A 重新开始
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
LLM Prompt 模板 → 生成 Spec
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

## 第二步：三条核心命令

> 完整输出格式与字段说明见 `references/codegraph-setup.md`

### `codegraph_search` — 符号搜索

**用途**：按名称/关键词快速定位函数、类、接口、变量  
**典型场景**：找入口点、找服务类、找 Controller

```
# MCP 工具调用（Claude Code 内）
codegraph_search(query="UserController", kind="class", limit=10)
codegraph_search(query="login", kind="function")
codegraph_search(query="SysUser")        # 搜索所有相关符号

# CLI 等价命令
codegraph query "UserController" --kind class --json
```

**输出字段**：`id`, `name`, `kind`, `file`, `line`, `signature`, `docstring`

**大型项目分页策略（重要）：**

```
# 问题：单次查询有 limit 上限，大型项目（800+ 类）会截断
# 解决：用命名模式分批精确查询，代替空字符串全量查询

# 正确做法：分批查询 + 截断检测
codegraph_search("Controller", kind="class", limit=200)
codegraph_search("ServiceImpl", kind="class", limit=200)
codegraph_search("Mapper",      kind="class", limit=200)
codegraph_search("DO",          kind="class", limit=200)   # MyBatis-Plus 实体
codegraph_search("Entity",      kind="class", limit=200)   # JPA 实体
codegraph_search("ErrorCode",   kind="class", limit=200)

# 截断检测：如果 len(results) == limit（返回数 = 上限值）
#   → 说明可能被截断，需换更精确的 query 重查
#   → 例：query="system" + kind="class" 缩小到单个包

# 完整性校验：各批结果数之和 ≈ codegraph status 的 class 总数（允许 ±20% 误差）
# 如差距 > 20%，补充查询直到覆盖全部
```

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

> Java 后端框架详细规则见 `references/java-frameworks.md`  
> 前端框架识别规则见 `references/frontend-frameworks.md`  
> Python/TypeScript/Go 后端框架规则见 `references/multi-framework-patterns.md`

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

# package.json 缺失时退回符号搜索
codegraph_search("defineStore")         → Vue + Pinia
codegraph_search("createSlice")         → React + Redux
codegraph_search("@NgModule")           → Angular
```

识别框架后，使用对应的 **框架提取规则** 理解代码分层，再进入第四步。

**框架识别失败的兜底处理：**

```
# 当 package.json 不存在且符号搜索也无法命中任何已知框架时
→ 在 spec/00-overview.md 中注明"技术栈未能自动识别"
→ 列出扫描到的文件扩展名分布（.java / .py / .go / .ts / .vue 等）
→ 列出顶层目录结构（ls -d */ 输出）
→ 由人工根据目录结构和文件扩展名判断框架类型，再手动指定重新运行
```

---

## 第四步：规格书生成流程

> LLM Prompt 模板详见 `references/llm-prompts.md`  
> Spec 输出模板详见 `references/spec-templates.md`

### 4.1 三条核心命令的职责分工

| 命令 | 用途 | 驱动哪些 Spec |
|------|------|-------------|
| `codegraph_search` + `Read` | 定位类文件，读取源码提取字段/注解 | API 字段校验矩阵、DDL、错误码表 |
| `codegraph_callers` | 向上追踪——发现定时任务/MQ/事件等非 Controller 触发点 | SRS 触发条件、HLA 异步架构 |
| `codegraph_trace` | 两点间完整调用路径（Controller → Mapper）| 06-flowcharts/ 业务流程图骨架 |

### 4.2 数据采集流程（Phase A → D）

**Phase A：项目概况**

```
codegraph status → 记录总量基线（后续用于完整性校验）：
  文件总数 F、节点总数 N、class 节点数 C（关键：作为后续查询完整性的参照基线）
  语言分布（判断是 Java/Python/Go/TS/前端）

# 用命名模式分批查询（禁止空字符串全量查询，会截断）
codegraph_search("Controller", kind="class", limit=200) → 控制器列表
codegraph_search("ServiceImpl", kind="class", limit=200) → 服务列表
codegraph_search("DO",          kind="class", limit=200) → 实体列表（MyBatis-Plus）
codegraph_search("Entity",      kind="class", limit=200) → 实体列表（JPA）
# 如 len(results) == 200 → 截断，改用更精确 query 分包查询

各批结果数之和是否接近 class 总数 C：
  差距 ≤ 20% → 继续
  差距 > 20% → 补充查询（按包名前缀细分）
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

模块数 > 5 → 采用模块化输出目录（见下方）
模块数 ≤ 5 → 输出到顶层 spec/ 目录

# Phase B 完成后立即写入进度文件（断点恢复的基础）
Write("spec/.progress.md") → 写入完整任务清单（见 spec-templates.md §.progress.md 模板）
  - Phase A/B 标记为 [x]，所有 Phase C/D 条目标记为 [ ]
  - 列出所有预期生成的 Spec 文件路径
```

**Phase C：逐模块深挖（对每个模块执行以下步骤）**

```
【C1 — 遍历模块内所有 Controller 文件，建立接口功能清单（ENDPOINT_LIST）】

# Step 1：找出本模块所有 Controller 文件（一个模块可能有多个 Controller）
codegraph_search("{module}*Controller", kind="class") → 得到文件列表（≥1 个）
# 如模块名不明确，改用包路径过滤：
codegraph_search("Controller", kind="class") → 按包前缀过滤出本模块的结果

# Step 2：对每个 Controller 文件执行 Read（不得跳过任何一个）
for each ControllerFile in 文件列表：
  Read(ControllerFile) → 从源码中提取所有 endpoint：
    - HTTP 方法 + 路径（@GetMapping / @PostMapping / @PutMapping / @DeleteMapping）
    - 方法名 + @Operation/@ApiOperation 注解描述
    - @PreAuthorize / @SaCheckPermission 权限码

# Step 3：汇总为本模块「接口功能清单」（ENDPOINT_LIST）
ENDPOINT_LIST = [
  { method: "POST",   path: "/system/user/create",  name: "创建用户",   permission: "system:user:create" },
  { method: "PUT",    path: "/system/user/update",  name: "更新用户",   permission: "system:user:update" },
  { method: "DELETE", path: "/system/user/{id}",    name: "删除用户",   permission: "system:user:delete" },
  { method: "GET",    path: "/system/user/list",    name: "查询用户列表", permission: "system:user:list"   },
  ...（穷举，不遗漏）
]
→ ENDPOINT_LIST 是后续 SRS/API/流程图/UI 遍历生成的驱动依据
→ 将"本模块接口数 = N"记录到 spec/.progress.md

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
CF1. codegraph_search("router" / "routes", kind="variable")
     Read(src/router/index.ts 或等价文件) → 路由表（path + component + meta.auth）
     → 输出字段供 Prompt 12（spec/10-pages.md）使用

CF2. codegraph_search("defineStore") → Pinia store 文件
     Read(store 文件) → store name、state 字段类型、actions 方法签名
     → 输出字段供 Prompt 12（spec/12-state.md）使用

CF3. codegraph_search("request" / "axios", kind="function") → API 服务文件
     Read(src/api/ 目录) → API 函数 → 调用端点 + 参数类型 + 返回类型
     → 作为 {{FRONTEND_API_SOURCE}} 变量注入 Prompt 5（spec/05-api.md 前端调用对照）

CF4. codegraph_callers("useUserStore" / "usePermissionStore")
     → 找出依赖权限 Store 的页面/组件 → 权限守卫文档

CF5. codegraph_trace(from="LoginPage.submit", to="useUserStore.setToken")
     → 登录流程调用链 → 前端流程图
```

**Phase C'：完整性核对（Phase C 结束后，Phase D 开始前）**

```
for each 模块 in 模块列表：
  ✓ 模块内所有 Controller 文件已 Read（不仅第一个，全部遍历）
  ✓ ENDPOINT_LIST 已建立，条目数 == Controller 源码中
    @GetMapping/@PostMapping/@PutMapping/@DeleteMapping 注解总数
    （允许 ±1 误差，处理 @RequestMapping 在类级别的情况）
    如不一致 → 重新 Read 遗漏的 Controller 文件，补全 ENDPOINT_LIST
  ✓ Entity/DO 文件已 Read（已提取 ≥ 1 个 @TableName 或 @Entity）
  ✓ ErrorCodeConstants 已 Read（或确认本模块无错误码文件）
  ✓ codegraph_callers 已对 ENDPOINT_LIST 中每个 POST/PUT/DELETE 接口的 Service 方法执行
  ✓ codegraph_trace   已对 ENDPOINT_LIST 中每个 POST/PUT/DELETE 接口执行（每写操作一条 trace）

如果识别到前端框架（FRONTEND_FRAMEWORK != "无"）：
  ✓ 路由配置文件已 Read（CF1，已提取 ≥ 1 个 path）
  ✓ Store 文件已 Read（CF2，已提取 ≥ 1 个 defineStore/createSlice）
  ✓ API 服务文件已 Read（CF3，已提取 ≥ 1 个 HTTP 调用函数，将作为 {{FRONTEND_API_SOURCE}}）

如果某模块未通过：
  → 补充执行缺失步骤 → 重新检查 → 通过后才继续
  → 更新 spec/.progress.md 的完整性校验表格（含"接口数"列）

全部模块通过 → 进入 Phase D
```

**Phase D：生成规格书**

```
每个模块独立生成，按顺序：SRS → API（含前端调用对照，如有 CF3 数据）→ Database → 错误码 → UI → 流程图
最后汇总：HLA → PRD → 用户故事 → 全局概览
如识别到前端框架，追加生成：10-pages.md → 11-components.md → 12-state.md → 13-i18n.md（如有 i18n）

# SRS 生成规则：
# 将 ENDPOINT_LIST 作为 FR 清单骨架传入 Prompt 2
# 要求 LLM 为每个 endpoint 生成一个 FR-XXX 条目（不得合并多个接口为一个 FR）
# SRS 中 FR 条目数必须 == ENDPOINT_LIST 长度（Phase D 结束后校验）

# API 文档生成规则：
# 将 ENDPOINT_LIST 作为接口清单骨架传入 Prompt 5
# 要求 LLM 为每个 endpoint 生成一个独立章节（### METHOD /path）
# 05-api.md 中接口章节数必须 == ENDPOINT_LIST 长度（Phase D 结束后校验）

# 流程图生成规则（06-flowcharts/）：
# for each endpoint in ENDPOINT_LIST where method in [POST, PUT, DELETE, PATCH]：
#   生成 spec/06-flowcharts/{operation}-{entity}-flow.mmd（用 C3 trace 数据驱动）
# GET 列表查询/详情查询：生成一个 query-{entity}-flow.mmd
# 最少生成：N 个写操作 → N 个 .mmd 文件（不得将多个操作合并为 1 个流程图）

# 09-ui/ 生成规则（所有项目均生成，不限于前端项目，只要有业务 Entity 和 CRUD 接口即生成）：
# 将 UI_CONTEXT[{module}]（Phase C C1 已打包）注入 Prompt 8
# for each 业务 Entity in 模块（来自 C1 的 Entity 读取结果）：
#   小型项目：生成 spec/09-ui/{entity}-list.html + spec/09-ui/{entity}-form.html
#   大型项目：生成 spec/modules/{module}/09-ui/{entity}-list.html + {entity}-form.html
# 多个 Entity 不合并，各自独立生成

# 流程图 SVG 渲染规则（在所有 .mmd 写完后执行）：
# for each .mmd 文件（已 Write 完毕）：
#   show_widget(mermaid_code) → 渲染为 SVG 图像
#   Write("spec/06-flowcharts/{name}.svg", svg_content)
# 每个 .mmd 对应一个同名 .svg，两者成对出现

# 关键：每生成一个文件后，立即更新 spec/.progress.md 对应条目为 [x]
# 如果 context 在生成过程中溢出，该文件仍标记为 [ ]（未完成），恢复后重做
# 宁可重做一个文件，不要留下内容不完整的半成品

# 所有文件生成完成后：
# 在 spec/00-overview.md 末尾追加一行：
# > 生成完成：{时间戳}，覆盖 {N} 个模块，{M} 张数据表，{P} 个 API 接口
```

### 4.3 语义归类

将采集到的符号按业务模块归类：
- **用户权限模块** → sys_user / SysUserController / LoginController
- **业务核心模块** → 业务相关 Entity + Service + Controller
- **数据访问层** → Mapper/Repository + DB 表结构
- **公共基础设施** → 配置类、工具类、拦截器、MQ 消费者
- **前端页面层** → Pages + Router + Store + API Client

### 4.4 输入 LLM Prompt → 生成各类 Spec

每种文档对应一个 Prompt 模板（见 `references/llm-prompts.md`）。
按如下顺序生成，后面的文档依赖前面的输出：

```
HLA → SRS → PRD → 用户故事 → API 文档（含字段校验矩阵）→ 流程图 → 数据库结构 → 错误码表 → UI/UX
```

---

## 输出规格书结构

### 小型项目（模块 ≤ 5）— 平铺结构

```
spec/
├── 00-overview.md          项目概览、技术栈、框架
├── 01-hla.md               高阶架构（含 Mermaid 系统图）
├── 02-srs.md               软件需求规格
├── 03-prd.md               产品需求文档
├── 04-user-stories.md      用户故事列表
├── 05-api.md               API 规格（含字段校验矩阵 + 前端调用对照）
├── 06-flowcharts/          流程图（Mermaid + SVG）
│   ├── login-flow.mmd
│   ├── login-flow.svg
│   └── ...
├── 07-database.md          数据库结构（DDL + ER 图）
├── 08-error-codes.md       错误码表
├── 09-ui/                  界面布局（静态 HTML）
├── 10-pages.md             前端页面路由表              ← 仅前端项目生成
├── 11-components.md        核心组件树（Props/Emits）   ← 仅前端项目生成
├── 12-state.md             状态管理（Store 模块）       ← 仅前端项目生成
└── 13-i18n.md              国际化 key 列表             ← 仅前端 + i18n 时生成
```

### 大型项目（模块 > 5）— 模块化结构

```
spec/
├── 00-overview.md              全局概览
├── 01-hla.md                   全局架构图（微服务拓扑/模块依赖）
├── 04-user-stories.md          跨模块用户故事汇总
├── 07-database-overview.md     全库 ER 总图（模块间关系）
└── modules/
    ├── {module-a}/
    │   ├── 02-srs.md           模块需求规格（含字段校验规则）
    │   ├── 05-api.md           模块 API（含字段校验矩阵 + 错误码）
    │   ├── 07-database.md      模块 DDL + ER 图（含索引定义）
    │   ├── 08-error-codes.md   模块错误码表
    │   ├── 09-ui/              UI 静态原型（HTML）
    │   │   ├── {entity}-list.html
    │   │   └── {entity}-form.html
    │   └── 06-flowcharts/
    │       ├── *.mmd
    │       └── *.svg
    ├── {module-b}/
    └── ...
```

> **前端规格文件（10-13 号）** 已整合到主 `spec/` 目录，不再单独放 `spec/frontend/` 子目录。  
> 仅在识别到前端框架时生成（纯后端项目跳过 10-13 号文件）。  
> 原 `04-api-client.md` 的 API 调用对照内容并入 `05-api.md` 的"前端调用层对照"章节，使同一接口下同时展示后端字段校验矩阵与前端 TS 类型定义。

```
spec/                           （含前端项目时）
├── ...（00-09 后端规格文件）
├── 10-pages.md             前端页面路由表              ← 仅前端项目生成
├── 11-components.md        核心组件树（Props/Emits）   ← 仅前端项目生成
├── 12-state.md             状态管理（Store 模块）       ← 仅前端项目生成
└── 13-i18n.md              国际化 key 列表             ← 仅前端 + i18n 时生成
```

### 输出格式规则

| 文档类型 | 格式 | 工具 |
|---------|------|------|
| SRS/PRD/HLA | Markdown | 直接写 .md |
| 流程图 | Mermaid 代码块 + SVG | `show_widget` 渲染 |
| API 文档 | OpenAPI 3.0 YAML/Markdown | 写 .md 或 .yaml |
| 数据库结构 | SQL DDL + Mermaid ER 图 | 直接输出 |
| UI/UX | 静态 HTML + Tailwind CSS | 写 .html 文件 |
| 用户故事 | Markdown 表格 | 直接写 .md |

---

## 参考文件索引

| 文件 | 何时读取 |
|-----|---------|
| `references/codegraph-setup.md` | 安装/初始化/CLI 命令完整参考 |
| `references/java-frameworks.md` | 识别到 Java 项目时必读（Spring 生态注解发现）|
| `references/frontend-frameworks.md` | 识别到前端项目时必读（Vue/React/Angular via package.json）|
| `references/multi-framework-patterns.md` | 识别到 Python/TS/Go 后端项目时必读 |
| `references/llm-prompts.md` | 生成任何 Spec 文档前必读 |
| `references/spec-templates.md` | 需要具体文档模板结构时读取 |
| `scripts/install-codegraph.sh` | 批量安装脚本 |
