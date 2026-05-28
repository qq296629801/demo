# 00 — 项目概览

## 项目信息

| 项目 | 值 |
|------|-----|
| 项目名称 | yudao-cloud |
| 定位 | 基于 Spring Cloud 微服务架构的企业级快速开发平台 |
| 开源协议 | MIT |
| 作者 | 芋道源码（YunaiV） |
| 技术栈 | Spring Cloud 2021 + Spring Boot 2.7.18 + Vue 3.2 |
| 演示地址 | http://dashboard-vue3.yudao.iocoder.cn |

## 项目定位

yudao-cloud 是一款**全部开源、永久免费**的企业级微服务快速开发平台，提供完整的权限管理、工作流、商城、CRM、ERP、IoT 等业务模块，适用于中大型企业信息化系统建设。

## 代码规模

| 指标 | 数值 |
|------|------|
| Java 文件 | 5,221 个 |
| Controller 类 | 455 个 |
| Service 实现类 | 379 个 |
| 数据对象（DO） | 394 个 |
| 数据库表 | 367 张 |
| codegraph 节点 | 106,086 个 |
| codegraph 边 | 147,144 条 |

## 模块结构

```
yudao-cloud
├── yudao-server               启动入口（Spring Boot + Spring Cloud）
├── yudao-gateway              API 网关（Spring Cloud Gateway + Sa-Token）
├── yudao-framework            框架层（安全、日志、权限、数据权限、Excel等）
├── yudao-dependencies         依赖版本管理 BOM
│
├── yudao-module-system        系统管理（用户/角色/菜单/字典/租户/日志等）
├── yudao-module-infra         基础设施（代码生成/文件/配置/定时任务等）
├── yudao-module-bpm           工作流（Flowable，流程设计/审批/OA）
├── yudao-module-ai            AI 能力（聊天/绘画/音乐/知识库/工作流）
├── yudao-module-crm           客户关系管理（客户/线索/商机/合同/回款）
├── yudao-module-erp           企业资源计划（采购/销售/库存/财务）
├── yudao-module-iot           物联网（设备/产品/数据采集/告警）
├── yudao-module-mp            微信公众号（账号/消息/素材/菜单/标签）
├── yudao-module-pay           支付（支付单/退款/钱包/转账）
├── yudao-module-report        报表（Goview 图表配置）
├── yudao-module-wms           仓储管理（入/出库/调拨）
├── yudao-module-member        会员中心（注册/积分/等级）
└── yudao-module-mall          商城
    ├── yudao-module-product   商品（SPU/SKU/分类/品牌/属性）
    ├── yudao-module-trade     交易（订单/购物车/物流/售后）
    ├── yudao-module-promotion 营销（秒杀/拼团/砍价/积分/客服）
    └── yudao-module-statistics 统计（商品/交易/会员/支付分析）
```

## 技术栈

| 层次 | 技术 |
|------|------|
| 微服务框架 | Spring Cloud 2021 + Spring Cloud Alibaba |
| Web 框架 | Spring Boot 2.7.18 + Spring MVC |
| 安全框架 | Spring Security + Token（JWT） |
| ORM | MyBatis-Plus |
| 分页 | MyBatis-Plus Page |
| 工作流 | Flowable 6.x |
| 缓存 | Redis + Redisson |
| 消息队列 | RocketMQ / RabbitMQ |
| 文件存储 | MinIO / 阿里云 OSS / 本地 |
| API 文档 | Swagger 3 (springdoc-openapi) |
| 定时任务 | XXL-JOB |
| 日志 | ELK / SkyWalking |
| 数据库 | MySQL 5.7+ |
| 前端 | Vue 3 + Element Plus |
| 构建 | Maven 多模块 |
| Java 版本 | JDK 8（兼容 JDK 17/21） |

## 各模块 Controller 数量

| 模块 | Admin API 数 |
|------|-------------|
| mes-server | 127 |
| system-server | 35 |
| promotion-server | 37 |
| crm-server | 21 |
| erp-server | 23 |
| iot-server | 18 |
| ai-server | 14 |
| trade-server | 19 |
| wms-server | 17 |
| pay-server | 19 |
| infra-server | 14 |
| product-server | 13 |
| mp-server | 12 |
| bpm-server | 11 |
| member-server | 20 |
| statistics-server | 4 |
