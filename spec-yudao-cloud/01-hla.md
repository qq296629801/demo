# 01 — 高阶架构（HLA）

## 系统架构总览

```mermaid
graph TB
    subgraph Client["客户端层"]
        VUE["Vue3 前端\n(Element Plus / Vben)"]
        APP["移动端 APP"]
        THIRD["第三方系统"]
    end

    subgraph Gateway["网关层"]
        GW["yudao-gateway\nSpring Cloud Gateway\n路由 + 鉴权 + 限流"]
    end

    subgraph Modules["业务微服务层"]
        SYS["yudao-module-system\n用户/角色/菜单/字典\n租户/日志/通知"]
        INFRA["yudao-module-infra\n代码生成/文件/配置\n定时任务/错误日志"]
        BPM["yudao-module-bpm\n工作流(Flowable)\n流程设计/审批/OA"]
        AI["yudao-module-ai\n聊天/绘画/音乐\n知识库/工作流"]
        CRM["yudao-module-crm\n客户/线索/商机\n合同/回款"]
        ERP["yudao-module-erp\n采购/销售/库存\n财务对账"]
        IOT["yudao-module-iot\n设备/产品/数据\n告警规则"]
        PAY["yudao-module-pay\n支付/退款/钱包\n转账"]
        MALL["yudao-module-mall\n商品/订单/营销\n统计"]
        WMS["yudao-module-wms\n入库/出库/调拨"]
        MP["yudao-module-mp\n公众号管理"]
        MEMBER["yudao-module-member\n会员/积分/等级"]
    end

    subgraph Framework["框架支撑层 (yudao-framework)"]
        SEC["Security\n(Spring Security + Token)"]
        PERM["DataPermission\n(数据权限过滤)"]
        LOG["ApiLog\n(接口访问日志)"]
        EXCEL["Excel\n(EasyExcel 导入导出)"]
        TENANT["Tenant\n(多租户隔离)"]
    end

    subgraph Infra["基础设施层"]
        DB["MySQL\n(主从/分库分表)"]
        REDIS["Redis\n(缓存/Session/限流)"]
        MQ["RocketMQ\n(异步消息/事件)"]
        NACOS["Nacos\n(服务注册/配置中心)"]
        MINIO["MinIO\n(文件存储)"]
        XXLJOB["XXL-JOB\n(定时任务调度)"]
    end

    VUE -->|HTTP/HTTPS| GW
    APP -->|HTTP/HTTPS| GW
    THIRD -->|OAuth2/API| GW
    GW -->|路由转发| Modules
    Modules --> Framework
    Framework --> DB
    Framework --> REDIS
    Modules --> MQ
    Modules --> NACOS
    Modules --> MINIO
    INFRA --> XXLJOB
```

---

## 标准分层架构（以 system 模块为例）

```mermaid
graph LR
    subgraph Controller["Controller 层"]
        C1["UserController\n/system/user/**"]
        C2["AuthController\n/system/auth/**"]
        C3["RoleController\n/system/role/**"]
    end

    subgraph Service["Service 层"]
        S1["AdminUserService\n(Interface)"]
        S2["AdminAuthService\n(Interface)"]
        S1I["AdminUserServiceImpl"]
        S2I["AdminAuthServiceImpl"]
        S1 --> S1I
        S2 --> S2I
    end

    subgraph Dal["DAL 层"]
        M1["AdminUserMapper\n(MyBatis-Plus)"]
        M2["MenuMapper"]
        DO1["AdminUserDO\n@TableName('system_user')"]
        DO2["MenuDO\n@TableName('system_menu')"]
    end

    subgraph DB["数据库"]
        T1[("system_user")]
        T2[("system_menu")]
        T3[("system_role")]
        T4[("system_role_menu")]
    end

    C1 --> S1
    C2 --> S2
    S1I --> M1
    M1 --> DO1
    DO1 --> T1
    M2 --> DO2
    DO2 --> T2
```

---

## 关键设计模式

| 模式 | 实现方式 | 代码示例 |
|------|---------|---------|
| **多租户** | `TenantUtils` + 字段隔离 | 所有表含 `tenant_id` 字段 |
| **数据权限** | `@DataPermission` + 部门树过滤 | `UserController.pageUser()` |
| **接口日志** | `@ApiAccessLog` AOP 拦截 | `@ApiAccessLog(operateType = EXPORT)` |
| **权限控制** | `@PreAuthorize("@ss.hasPermission(...)")` | `system:user:create` |
| **统一响应** | `CommonResult<T>` 包装 | `return success(id)` |
| **全局校验** | `@Validated` + `@Valid` + 自定义 Validator | `UserSaveReqVO` |
| **Excel 导入导出** | `ExcelUtils` + EasyExcel | `UserController.exportUserList()` |
| **OAuth2** | Spring Authorization Server | `/system/auth/login` |
| **工作流** | Flowable + 自定义扩展 | `BpmTaskController` |
| **软删除** | `deleted` 字段 + MyBatis-Plus 插件 | 全局 `LogicDeletePlugin` |

---

## 安全架构

```mermaid
sequenceDiagram
    participant C as 客户端
    participant GW as Gateway
    participant SYS as system-server
    participant Redis

    C->>GW: POST /system/auth/login {username, password}
    GW->>SYS: 转发请求
    SYS->>SYS: 校验账号密码
    SYS->>Redis: 存储 AccessToken + RefreshToken
    SYS-->>C: 返回 {accessToken, refreshToken, expiresTime}

    C->>GW: GET /system/user/page\nAuthorization: Bearer {token}
    GW->>Redis: 验证 token 有效性
    Redis-->>GW: token 有效，返回用户信息
    GW->>SYS: 转发请求（携带用户上下文）
    SYS->>SYS: @PreAuthorize 检查权限
    SYS-->>C: 返回数据
```

---

## 多租户架构

所有业务表包含 `tenant_id` 字段，框架层自动注入租户过滤条件：

- `TenantUtils.execute()` — 执行跨租户操作
- `TenantLineInnerInterceptor` — MyBatis-Plus 插件自动追加 `tenant_id` WHERE 条件
- 租户隔离粒度：数据行级别（共享数据库）
