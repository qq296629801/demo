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
├── 05-api.md               API 规格文档（含字段校验矩阵）
├── 06-flowcharts/          流程图（Mermaid 源码）
├── 07-database.md          数据库结构文档（DDL + 索引 + ER 图）
├── 08-error-codes.md       错误码表（从 ErrorCodeConstants 提取）
└── 09-ui/                  UI/UX 静态原型（HTML）
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
    │   ├── 05-api.md           模块 API（含字段校验矩阵 + 业务错误码）
    │   ├── 07-database.md      模块 DDL + ER 图（含所有索引定义）
    │   ├── 08-error-codes.md   模块错误码表
    │   └── 06-flowcharts/
    │       └── *.mmd
    └── {module-b}/
        └── ...
```

### 前端项目 — 前端规格目录

```
spec/frontend/
├── 00-overview.md         技术栈/构建工具/目录结构
├── 01-pages.md            页面路由表（路径/组件/权限守卫）
├── 02-components.md       核心组件树及 Props/Emits 定义
├── 03-state.md            Store 模块结构（state/action/effect）
├── 04-api-client.md       API 服务层（调用的后端端点+请求/响应类型）
└── 05-i18n.md             国际化 key 列表（如存在 i18n 文件）
```

---

## 00-overview.md 模板

```markdown
# {{PROJECT_NAME}} — 项目概览

## 基本信息

| 项目 | 内容 |
|-----|------|
| 项目名称 | {{PROJECT_NAME}} |
| 版本 | v{{VERSION}} |
| 技术栈 | Java 17 + Spring Boot 3.x + MyBatis-Plus |
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
- [UI 原型](./08-ui/)
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
- **输入**：
- **处理**：
- **输出**：
- **业务规则**：
- **异常处理**：
- **相关 API**：

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

## ER 总览图

​```mermaid
erDiagram
    [从 Entity 关系推断]
​```

## 表详细说明

### {{table_name}} — {{表中文名}}

**来源类**：`{{EntityClassName}}.java`

| 字段名 | 类型 | 长度 | 可空 | 默认值 | 说明 |
|-------|------|------|------|-------|------|
| id | bigint | 20 | 否 | 自增 | 主键 |
| create_by | varchar | 64 | 是 | '' | 创建者 |
| create_time | datetime | - | 是 | - | 创建时间 |
| update_by | varchar | 64 | 是 | '' | 更新者 |
| update_time | datetime | - | 是 | - | 更新时间 |
| del_flag | char | 1 | 是 | '0' | 删除标志 |

**索引**：
- PRIMARY KEY (id)
- [其他索引]

**DDL**：
​```sql
CREATE TABLE `{{table_name}}` (
  ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='{{表注释}}';
​```
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
```

**字段来源规则：**
- 正则约束：来自 `@Pattern(regexp = "...")`
- 长度约束：来自 `@Size(min=, max=)` 或 `@Length`
- 必填标记：来自 `@NotBlank` / `@NotNull`（更新接口通常标"更新可选"）
- 错误提示：来自注解的 `message` 属性
- 枚举约束：来自 `@Schema(description)` 或枚举类常量

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

## 前端页面路由表模板（spec/frontend/01-pages.md）

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

## 前端 Store 模块模板（spec/frontend/03-state.md）

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

## .progress.md 模板（进度跟踪 + 断点恢复）

> 路径：`spec/.progress.md`
> 写入时机：Phase B 完成后立即写入。每完成一个步骤后更新对应 `[ ]` 为 `[x]`。
> 用途：① 记录任务进度；② 上下文溢出后的断点恢复依据；③ 完成状态标志（全 `[x]` = 完成）。

```markdown
# .progress.md — 生成进度跟踪

最后更新：{{TIMESTAMP}}
项目：{{PROJECT_NAME}}
框架：{{FRAMEWORK}}
模块总数：{{MODULE_COUNT}}
codegraph class 总数（基线）：{{CLASS_COUNT_BASELINE}}

---

## 数据采集阶段

- [x] Phase A：项目概况（class 总数 {{CLASS_COUNT_BASELINE}}，文件数 {{FILE_COUNT}}）
- [x] Phase B：模块划分（发现 {{MODULE_COUNT}} 个模块：{{MODULE_LIST}}）
- [ ] Phase C：{{MODULE_1}} — C1(源码提取) C2(callers) C3(trace)
- [ ] Phase C：{{MODULE_2}} — C1(源码提取) C2(callers) C3(trace)
（每个模块一行，Phase B 完成后按实际模块列表生成）

## 完整性校验（Phase C' 填写）

| 模块 | Controllers | Entities | Callers执行 | Trace执行 | 通过 |
|------|------------|---------|------------|----------|------|
| {{MODULE_1}} | ✗ | ✗ | ✗ | ✗ | ✗ |
| {{MODULE_2}} | ✗ | ✗ | ✗ | ✗ | ✗ |
（Phase C 每个模块完成后填写实际数量，Phase C' 核对后改为 ✓）

---

## Spec 文件生成阶段

### 全局文件
- [ ] spec/01-hla.md
- [ ] spec/03-prd.md
- [ ] spec/04-user-stories.md
- [ ] spec/00-overview.md（最后生成）

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
