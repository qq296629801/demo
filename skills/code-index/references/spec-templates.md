# 规格书输出模板

## 文档体系说明

> **注意**：`spec/` 目录下始终包含 `.progress.md` 进度文件（见本文件最后一节）。
> 它是断点恢复和完成状态的唯一依据。

### 小型项目（模块 ≤ 5）— 平铺结构

```
spec/
├── 00-overview.md          项目概览（1-2页）
├── 01-hla.md               高阶架构（HLA）
├── 02-srs.md               软件需求规格（SRS，含字段校验规则）
├── 03-prd.md               产品需求文档（PRD）
├── 04-user-stories.md      用户故事（User Stories）
├── 05-api.md               API 规格文档（含字段校验矩阵 + 前端调用对照）
├── 06-flowcharts/          流程图（Mermaid 源码）
├── 07-database.md          数据库结构文档（DDL + 索引 + ER 图）
├── 08-error-codes.md       错误码表（从 ErrorCodeConstants 提取）
├── 09-ui/                  UI/UX 静态原型（HTML）
├── 10-pages.md             前端页面路由表              ← 仅前端项目生成
├── 11-components.md        核心组件树（Props/Emits）   ← 仅前端项目生成
├── 12-state.md             状态管理（Store 模块）       ← 仅前端项目生成
└── 13-i18n.md              国际化 key 列表             ← 仅前端 + i18n 时生成
```

### 大型项目（模块 > 5）— 模块化结构

```
spec/
├── 00-overview.md              全局概览（含前后端技术栈）
├── 01-hla.md                   全局架构图（微服务拓扑/模块依赖）
├── 04-user-stories.md          跨模块用户故事汇总
├── 07-database-overview.md     全库 ER 总图（模块间关系）
├── 10-pages.md                 前端页面路由表（全局）      ← 仅前端项目生成
├── 12-state.md                 状态管理模块总览            ← 仅前端项目生成
├── 13-i18n.md                  国际化 key 列表             ← 仅前端 + i18n 时生成
└── modules/
    ├── {module-a}/
    │   ├── 02-srs.md           模块需求规格（含字段校验规则）
    │   ├── 05-api.md           模块 API（含字段校验矩阵 + 前端调用对照）
    │   ├── 07-database.md      模块 DDL + ER 图（含所有索引定义）
    │   ├── 08-error-codes.md   模块错误码表
    │   └── 06-flowcharts/
    │       └── *.mmd
    └── {module-b}/
        └── ...
```

> **说明**：前端规格文件（10-13 号）已整合到主 `spec/` 目录，不再单独放 `spec/frontend/` 子目录。
> 原 `spec/frontend/04-api-client.md` 的内容并入 `05-api.md` 的"前端调用层对照"章节，使同一接口下同时展示后端字段校验矩阵与前端 TS 类型定义。
> 10-13 号文件仅在识别到前端框架时生成，纯后端项目不生成。

---

## 00-overview.md 模板

```markdown
# {{PROJECT_NAME}} — 项目概览

## 基本信息

| 项目 | 内容 |
|-----|------|
| 项目名称 | {{PROJECT_NAME}} |
| 版本 | v{{VERSION}} |
| 后端技术栈 | Java 17 + Spring Boot 3.x + MyBatis-Plus |
| 前端技术栈 | {{FRONTEND_STACK}}（无前端则填"N/A"）|
| 框架 | {{FRAMEWORK}} |
| 架构模式 | 单体应用 / 微服务 |
| 代码规模 | {{FILE_COUNT}} 文件，{{NODE_COUNT}} 符号节点 |
| 文档生成时间 | {{DATE}} |

## 功能模块一览

| 序号 | 模块名 | 核心功能 | 核心类 |
|-----|-------|---------|-------|
| 1 | 系统管理 | 用户、角色、菜单、部门管理 | SysUserController |
| ... | | | |

## codegraph 分析统计

| 指标 | 数量 |
|-----|------|
| Controller 类数 | xx |
| Service 类数 | xx |
| Entity 类数（数据库表数）| xx |
| API 接口总数 | xx |
| 平均调用链深度 | x 层 |

## 文档导航

- [高阶架构](./01-hla.md)
- [软件需求规格](./02-srs.md)
- [产品需求文档](./03-prd.md)
- [用户故事](./04-user-stories.md)
- [API 文档](./05-api.md)
- [流程图](./06-flowcharts/)
- [数据库结构](./07-database.md)
- [UI 原型](./09-ui/)
- [前端页面路由](./10-pages.md)（仅全栈项目）
- [前端组件树](./11-components.md)（仅全栈项目）
- [前端状态管理](./12-state.md)（仅全栈项目）
- [国际化](./13-i18n.md)（仅含 i18n 的前端项目）
```

---

## 01-hla.md 模板

```markdown
# 高阶架构文档（HLA）

## 系统定位

[1-2 段描述系统是什么、服务于哪些用户、解决什么核心问题]

## 系统架构图

​```mermaid
graph TB
    subgraph 客户端层
        Browser[浏览器 Web]
        Mobile[移动端 App]
    end
    subgraph 接入层
        Gateway[API 网关 / Nginx]
    end
    subgraph 应用层
        Auth[认证服务]
        System[系统管理模块]
        Biz[业务模块]
    end
    subgraph 数据层
        MySQL[(MySQL)]
        Redis[(Redis 缓存)]
        OSS[对象存储]
    end
    Browser --> Gateway
    Mobile --> Gateway
    Gateway --> Auth
    Gateway --> System
    Gateway --> Biz
    System --> MySQL
    Biz --> MySQL
    Auth --> Redis
​```

## 技术选型

| 层次 | 技术 | 版本 | 说明 |
|-----|------|------|------|
| Web 框架 | Spring Boot | 3.x | |
| ORM | MyBatis-Plus | 3.5.x | |
| 认证 | JWT / Sa-Token | - | |
| 数据库 | MySQL | 8.0 | |
| 缓存 | Redis | 7.x | |

## 模块职责

| 模块 | 包路径 | 职责 | 核心类 |
|-----|-------|------|-------|

## 安全架构

[从 Security 配置和权限注解推断]

## 部署架构

[从配置文件和 Docker 文件推断，若无则注明"未识别到部署配置"]
```

---

## 服务依赖图模板（追加至 01-hla.md）

在 `spec/01-hla.md` 末尾追加以下章节（由 Prompt 11 生成后追加）：

```markdown
## 服务依赖关系图

​```mermaid
graph LR
    UserServiceImpl --> DeptServiceImpl
    UserServiceImpl --> PermissionServiceImpl
    UserServiceImpl --> PasswordEncoder
    OrderServiceImpl --> UserServiceImpl
    OrderServiceImpl --> StockServiceImpl
    OrderServiceImpl --> PayOrderApi
​```

## 外部依赖说明

| 依赖服务 | 类型 | 用途 |
|---------|------|------|
| DeptServiceImpl | 内部 Service | 校验部门是否存在 |
| PayOrderApi | @FeignClient | 调用支付模块创建支付单 |
| RedisTemplate | 基础设施 | 缓存用户信息 |
```

---

## 02-srs.md 模板

```markdown
# 软件需求规格说明书（SRS）

## 1. 引言

### 1.1 目的
本文档描述 {{PROJECT_NAME}} 系统的软件功能需求（逆向生成自源代码）。

### 1.2 范围
[模块列表]

### 1.3 文档状态
本文档由 code-index skill 基于 codegraph 代码分析自动生成，需业务团队确认业务意图。

## 2. 整体描述

### 2.1 产品概览
[系统定位，1 段]

### 2.2 用户群体
| 用户类型 | 描述 | 权限范围 |
|---------|------|---------|

### 2.3 运行环境
[从配置文件推断]

## 3. 功能需求

### 3.1 {{模块名}}

#### FR-001 {{功能名}}
- **描述**：
- **优先级**：P0/P1/P2（P0=核心流程，P1=辅助功能，P2=配置/管理）
- **相关 API**：
- **触发方式**：用户手动触发 / 定时任务 / 事件驱动（从 callers 推断）
- **前置条件**：
- **主流程**：[步骤列表]
- **异常流程**：
- **字段约束**：（从 VO/DTO 注解提取，每个字段一行）
  - fieldName：必填，规则描述（正则/长度/枚举）
- **唯一性约束**：（从 `@Column(unique=true)` 或 UNIQUE INDEX 推断）
- **删除策略**：软删除（deleted 字段）/ 物理删除

[每个功能一个小节，按 FR-XXX 编号]

## 4. 非功能需求

### 4.1 安全性
[从 Spring Security / Sa-Token 配置推断]

### 4.2 数据完整性
[从 @NotNull、@Length、数据库约束推断]

### 4.3 审计日志
[从 @Log / @AutoLog 覆盖范围推断]

## 5. 约束与假设

[数据权限范围、框架约束、已知限制]
```

---

## 04-user-stories.md 模板

```markdown
# 用户故事列表

## 模块：{{模块名}}

| 编号 | 故事 | 优先级 | 关联 API |
|-----|------|-------|---------|
| US-001 | 作为管理员，我希望能搜索用户列表，以便快速定位目标用户 | P0 | GET /system/user/list |

---

### US-001 详细描述

**作为** 系统管理员  
**我希望** 通过姓名/手机号/状态筛选用户列表  
**以便** 快速定位和管理目标用户

**验收标准**：
- **Given** 管理员已登录系统
- **When** 在用户列表页输入搜索条件点击查询
- **Then** 系统返回符合条件的用户分页列表，包含：用户名、姓名、手机号、状态、创建时间

**优先级**：P0  
**Story Points**：3  
**关联 API**：`GET /system/user/list?pageNum=1&pageSize=10&userName=&phonenumber=&status=`
```

---

## 05-api.md 模板

```markdown
# API 规格文档

## 概述

| 项目 | 值 |
|-----|---|
| Base URL | http://{{host}}/api |
| 协议 | HTTPS |
| 认证方式 | Bearer Token（JWT）|
| 响应格式 | application/json |

## 统一响应格式

​```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
​```

## 错误码

| 错误码 | 说明 |
|-------|------|
| 200 | 操作成功 |
| 401 | 未授权 |
| 403 | 权限不足 |
| 500 | 服务器错误 |

---

## {{模块名}} 接口

### GET /{{path}}/list — 查询列表

**描述**：[从 @ApiOperation/@Operation 提取]  
**权限**：`{{permission_string}}`

**请求参数**：
| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|

**响应**：
​```json
{
  "code": 200,
  "rows": [],
  "total": 0
}
​```

**业务错误码**（从 ErrorCodeConstants 提取该接口相关错误码）：
| 错误码 | 常量名 | 说明 | 触发场景 |
|-------|--------|------|---------|
| 1002001 | MODULE_ENTITY_EXISTS | 记录已存在 | 创建/更新时数据重复 |
| 1002004 | MODULE_ENTITY_NOT_EXISTS | 记录不存在 | 查询/更新/删除时 ID 无效 |

**HTTP 状态码**：
| 状态码 | 说明 |
|-------|------|
| 200 | 操作成功 |
| 400 | 请求参数校验失败 |
| 401 | 未登录或 Token 失效 |
| 403 | 权限不足 |

**前端调用层对照**（仅全栈项目，纯后端项目省略此节）：
| 前端函数名 | HTTP 方法 | 端点 | TS 请求类型 | TS 响应类型 | 使用页面 |
|-----------|---------|------|-----------|-----------|---------|
| getXxxPage | GET | /{{path}}/list | XxxQueryReqVO | CommonResult\<PageResult\<XxxRespVO\>\> | Xxx 列表页 |
```

---

## 07-database.md 模板

```markdown
# 数据库结构文档

## 数据库概览

| 指标 | 值 |
|-----|---|
| 数据库类型 | MySQL 8.0 |
| 字符集 | utf8mb4 |
| 表总数 | {{TABLE_COUNT}} |
| 生成方式 | codegraph 逆向 Entity 类 |

## 1. 数据库表清单

| 表名 | 中文名 | 模块 | 说明 |
|-----|-------|------|------|
| sys_user | 用户信息表 | 系统管理 | 从 @TableName 和类注释提取 |
| sys_dept | 部门表 | 系统管理 | |

## 2. 表详细说明

### {{table_name}} — {{表中文名}}

**来源类**：`{{EntityClassName}}.java`

| 字段名 | 类型 | 长度 | 可空 | 默认值 | 说明 |
|-------|------|------|------|-------|------|
| id | bigint | 20 | 否 | 自增 | 主键 |
| create_by | varchar | 64 | 是 | '' | 创建者（继承自 BaseDO）|
| create_time | datetime | - | 是 | - | 创建时间（继承自 BaseDO）|
| update_by | varchar | 64 | 是 | '' | 更新者（继承自 BaseDO）|
| update_time | datetime | - | 是 | - | 更新时间（继承自 BaseDO）|
| del_flag | char | 1 | 是 | '0' | 删除标志（继承自 BaseDO）|

**索引**：
- PRIMARY KEY (id)
- [其他索引，来自 @UniqueConstraint / @TableIndex / 类注释]

**DDL**：
​```sql
CREATE TABLE `{{table_name}}` (
  ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{{表注释}}';
​```

## 3. ER 关系图

​```mermaid
erDiagram
    SYS_USER {
        bigint user_id PK
        varchar username
        bigint dept_id FK
    }
    SYS_DEPT {
        bigint dept_id PK
        varchar dept_name
    }
    SYS_USER }|--|| SYS_DEPT : "belongs to"
​```

## 4. 数据字典（枚举/常量）

| 字典名 | 来源 | 键值对 |
|-------|------|-------|
| 用户状态 | UserStatusEnum | 0=禁用，1=启用 |
| 删除标志 | del_flag | 0=正常，2=删除 |
（来自代码中的枚举类和 @Dict 注解）
```

---

## 流程图命名规范

```
06-flowcharts/
├── login-flow.mmd              用户登录流程
├── logout-flow.mmd             退出登录流程
├── user-register-flow.mmd      用户注册流程
├── order-create-flow.mmd       订单创建流程
├── order-approve-flow.mmd      订单审批流程
├── permission-check-flow.mmd   权限校验流程
└── {业务动词}-{名词}-flow.mmd  自定义命名
```

每个 .mmd 文件同时生成对应的 SVG 版本（通过 `show_widget` 渲染后保存）。

---

## 06-flowcharts/ 模板

每个 `.mmd` 文件包含以下两种图之一：

### 用户操作视角流程图（面向产品/业务）

```markdown
​```mermaid
flowchart TD
    A[用户打开功能页] --> B[填写表单/触发操作]
    B --> C{参数校验}
    C -->|校验失败| D[提示错误信息]
    C -->|校验通过| E[调用接口]
    E --> F{业务处理}
    F -->|成功| G[返回成功提示]
    F -->|失败| H[返回业务错误]
    style D fill:#f96
    style H fill:#f96
​```
```

### 技术调用链时序图（面向开发）

```markdown
​```mermaid
sequenceDiagram
    actor User
    participant Controller
    participant Service
    participant Mapper
    participant DB as MySQL

    User->>Controller: HTTP Request
    Controller->>Service: 业务方法调用
    Service->>Mapper: 数据库查询/写入
    Mapper->>DB: SQL 执行
    DB-->>Mapper: 结果集
    Mapper-->>Service: 返回数据
    Service-->>Controller: 处理结果
    Controller-->>User: HTTP Response
​```
```

**规则：**
- 节点名使用业务语言，不要用类名
- 判断节点用菱形（`{}`）
- 异常/错误路径标红（`style xxx fill:#f96`）
- 时序图中参与者不超过 6 个

---

## 字段校验矩阵模板（05-api.md 中使用）

每个 POST/PUT 接口的请求体必须包含此表格，字段来自实际 VO/DTO 源码的注解：

```markdown
**请求体字段校验矩阵**：
| 字段 | 类型 | 必填 | 规则/约束 | 错误提示 |
|------|------|------|-----------|---------|
| username | string | 是 | 4-30位，仅字母数字（^[a-zA-Z0-9]{4,30}$）| 用户账号由数字、字母组成 |
| password | string | 创建必填 | 4-16位 | 密码长度为 4-16 位 |
| nickname | string | 是 | 1-30位 | 用户昵称不能为空 |
| email | string | 否 | 邮箱格式 | 邮箱地址不正确 |
| mobile | string | 否 | ^1[3-9]\d{9}$ | 手机号格式错误 |
| sex | integer | 否 | 枚举值：0=未知 1=男 2=女 | - |
| deptId | long | 否 | 部门必须存在 | 部门不存在 |

**前端调用层对照**（仅全栈项目生成，纯后端项目省略此节）：
| 前端函数名 | HTTP 方法 | 端点 | TS 请求类型 | TS 响应类型 | 使用页面 |
|-----------|---------|------|-----------|-----------|---------|
| createUser | POST | /system/user/create | UserSaveReqVO | CommonResult\<Long\> | 用户创建弹窗 |
```

**字段来源规则：**
- 正则约束：来自 `@Pattern(regexp = "...")`
- 长度约束：来自 `@Size(min=, max=)` 或 `@Length`
- 必填标记：来自 `@NotBlank` / `@NotNull`（更新接口通常标"更新可选"）
- 错误提示：来自注解的 `message` 属性
- 枚举约束：来自 `@Schema(description)` 或枚举类常量
- **前端调用层**：来自 `src/api/*.ts` 的 TypeScript 函数签名，TS 类型字段与后端字段如有差异，标注 `[⚠️ 类型不一致]`

---

## 错误码表模板（08-error-codes.md）

```markdown
# {{MODULE_NAME}} 模块错误码

> 来源文件：`ErrorCodeConstants.java` / `*ErrorCode.java`

| 错误码编号 | 常量名 | 中文描述 | 触发场景 |
|-----------|--------|---------|---------|
| 1002001 | USER_USERNAME_EXISTS | 用户账号已存在 | 创建/更新用户时用户名重复 |
| 1002002 | USER_MOBILE_EXISTS | 手机号已存在 | 创建/更新用户时手机号重复 |
| 1002003 | USER_EMAIL_EXISTS | 邮箱已存在 | 创建/更新用户时邮箱重复 |
| 1002004 | USER_NOT_EXISTS | 用户不存在 | 查询/更新/删除时 ID 无效 |
| 1002007 | USER_PASSWORD_EXPIRED | 密码已过期 | 登录时检测到密码超过有效期 |
```

---

## 前端页面路由表模板（spec/10-pages.md）

```markdown
# 页面路由表

| 路径 | 组件文件 | 布局 | 权限守卫 | 功能描述 |
|-----|---------|------|---------|---------|
| /login | views/Login.vue | 空布局（BasicLayout） | 无需登录 | 用户名/密码登录 |
| /dashboard | views/Dashboard.vue | 管理布局（AdminLayout） | 需登录 | 系统首页/工作台 |
| /system/user | views/system/user/index.vue | 管理布局 | 需登录 + `system:user:list` | 用户列表管理 |
| /system/user/create | views/system/user/form.vue | 管理布局 | `system:user:create` | 创建用户 |
| /bpm/process | views/bpm/process/index.vue | 管理布局 | `bpm:process:query` | 流程定义管理 |
```

---

## 前端 Store 模块模板（spec/12-state.md）

```markdown
# 状态管理（Pinia）

## useUserStore

**文件**：`src/store/modules/user.ts`

### State 字段
| 字段 | 类型 | 初始值 | 说明 |
|-----|------|-------|------|
| token | string | '' | 登录 Token（存 localStorage） |
| name | string | '' | 用户昵称 |
| avatar | string | '' | 头像 URL |
| roles | string[] | [] | 角色编码列表 |
| permissions | string[] | [] | 权限码列表（用于按钮级权限） |

### Actions
| 方法 | 参数 | 说明 |
|-----|------|------|
| `login(form)` | `{ username, password, captcha }` | 调用登录接口，存储 token |
| `getInfo()` | - | 获取当前用户信息和权限列表 |
| `logout()` | - | 清除 token，跳转登录页 |
```

---

## 09-ui/ 模板

每个模块生成两个文件：`{module}-list.html`（列表页）和 `{module}-form.html`（表单弹窗）。

### 列表页要素

```markdown
- 顶部搜索栏（根据查询条件字段生成，每个条件对应输入框/下拉/日期选择器）
- 操作按钮区（新增 / 批量删除 / 导入 / 导出，按 @PreAuthorize 权限点决定显隐）
- 数据表格（列：来自 RespVO 字段；最后一列：编辑/删除操作按钮）
- 分页组件（页码 + 每页条数选择）
```

### 弹窗表单要素

```markdown
- 表单字段（根据字段类型自动选择组件：文本框 / 下拉 / 日期选择器 / 开关）
- 必填标记（* 号）及验证错误提示
- 确认 / 取消按钮
```

### 技术约束

```markdown
- 使用 Tailwind CSS（CDN 引入）
- 使用 Bootstrap Icons 图标库
- 配色方案：专业蓝灰（主色 #1d4ed8）
- 完全静态，无需后端，用 localStorage mock 数据
- 响应式布局，支持 1280px 以上宽屏
- 代码内嵌 CSS 和 JS，单文件交付
```

---

## .progress.md 模板（进度跟踪 + 断点恢复）

> 路径：`spec/.progress.md`
> 写入时机：Phase B 完成后立即写入。每完成一个步骤后更新对应 `[ ]` 为 `[x]`。
> 用途：① 记录任务进度；② 上下文溢出后的断点恢复依据；③ 完成状态标志（全 `[x]` = 完成）。

```markdown
# .progress.md — 生成进度跟踪

最后更新：{{TIMESTAMP}}
项目：{{PROJECT_NAME}}
框架：{{FRAMEWORK}}
前端框架：{{FRONTEND_FRAMEWORK}}（无则填"无"，恢复时据此决定是否生成 10-13 号文件）
模块总数：{{MODULE_COUNT}}
codegraph class 总数（基线）：{{CLASS_COUNT_BASELINE}}
各模块接口数：{{MODULE_1}}={{N1}} 个，{{MODULE_2}}={{N2}} 个（Phase C C1 建立 ENDPOINT_LIST 后填写）

---

## 数据采集阶段

- [x] Phase A：项目概况（class 总数 {{CLASS_COUNT_BASELINE}}，文件数 {{FILE_COUNT}}）
- [x] Phase B：模块划分（发现 {{MODULE_COUNT}} 个模块：{{MODULE_LIST}}）
- [ ] Phase C：{{MODULE_1}} — C1(源码提取) C2(callers) C3(trace)
- [ ] Phase C：{{MODULE_2}} — C1(源码提取) C2(callers) C3(trace)
（每个模块一行，Phase B 完成后按实际模块列表生成）

## 完整性校验（Phase C' 填写）

| 模块 | Controller文件数 | 接口总数(ENDPOINT_LIST) | Entities | Callers覆盖 | Trace覆盖 | 通过 |
|------|----------------|----------------------|---------|------------|----------|------|
| {{MODULE_1}} | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ |
| {{MODULE_2}} | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ |
（Phase C 每个模块完成后填写实际数量；"接口总数"= ENDPOINT_LIST 长度，需与 Controller 源码注解数吻合；Phase C' 全部核对后改为 ✓）

---

## Spec 文件生成阶段

### 全局文件
- [ ] spec/01-hla.md
- [ ] spec/03-prd.md
- [ ] spec/04-user-stories.md
- [ ] spec/00-overview.md（最后生成）
- [ ] spec/10-pages.md             ← 如有前端框架
- [ ] spec/11-components.md        ← 如有前端框架
- [ ] spec/12-state.md             ← 如有前端框架
- [ ] spec/13-i18n.md              ← 如有前端框架且有 i18n

### 模块文件（大型项目，小型项目调整为顶层路径）
- [ ] spec/modules/{{MODULE_1}}/02-srs.md
- [ ] spec/modules/{{MODULE_1}}/05-api.md
- [ ] spec/modules/{{MODULE_1}}/07-database.md
- [ ] spec/modules/{{MODULE_1}}/08-error-codes.md
- [ ] spec/modules/{{MODULE_1}}/06-flowcharts/（至少 1 个 .mmd）
- [ ] spec/modules/{{MODULE_2}}/02-srs.md
（按模块重复，Phase B 后根据实际模块列表展开）

---

## 完成标志

所有 Spec 文件条目全部变为 [x] → 生成完成
完成后在 spec/00-overview.md 末尾追加：
> 生成完成：{时间戳}，覆盖 {N} 个模块，{M} 张数据表，{P} 个 API 接口
```

---

### .progress.md 使用规则速查

| 事件 | 操作 |
|------|------|
| Phase B 完成 | `Write("spec/.progress.md")` 写入完整清单，Phase A/B 标 `[x]`，其余标 `[ ]` |
| 每个 Phase C 模块完成 | 更新对应 `[ ]` 为 `[x]`，填写完整性校验表格 |
| 每个 Spec 文件生成完成 | 立即更新对应 `[ ]` 为 `[x]`（生成途中溢出则保留 `[ ]`，恢复后重做） |
| Skill 被重新调用 | `Read("spec/.progress.md")` → 找第一个 `[ ]` → 从那里继续 |
| 所有条目变为 `[x]` | 任务完成，写入 00-overview.md 完成时间戳 |
