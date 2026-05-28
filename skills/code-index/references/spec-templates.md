# 规格书输出模板

## 文档体系说明

```
Spec 文档体系
├── 00-overview.md          项目概览（1-2页）
├── 01-hla.md               高阶架构（HLA）
├── 02-srs.md               软件需求规格（SRS）
├── 03-prd.md               产品需求文档（PRD）
├── 04-user-stories.md      用户故事（User Stories）
├── 05-api.md               API 规格文档
├── 06-flowcharts/          流程图（Mermaid 源码 + SVG）
├── 07-database.md          数据库结构文档
└── 08-ui/                  UI/UX 静态原型（HTML）
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
