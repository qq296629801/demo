# 00 — 项目概览

## 项目信息

| 项目 | 值 |
|------|-----|
| 项目名称 | SIE IIDP Backend Demo（`iidp-backend-demo`） |
| GroupId | `com.sie.iidp` |
| 产品代号 | `SIE-IIDP-DEMO` |
| 版本 | `1.0.0`（snapshot） |
| 许可证 | LGPL 3.0 |
| 作者 | SIE IIDP Team |

## 项目定位

这是 **SIE 赛意 IIDP（智能集成开发平台）** 的官方后端 Demo 工程，以"原子能力样例"形式，系统性演示 IIDP 平台各类开发能力，供新手开发者学习与参考。

## 模块结构

```
iidp-backend-demo（父 POM）
├── sie-iidp-demo-start          启动模块（Spring Boot 入口）
├── sie-iidp-demo-apps/
│   ├── sie-iidp-demo-example    核心示例 APP（学生 / 物料 / 区域 / 订单 等）
│   └── sie-iidp-demo-xxljob-executor  XXL-JOB 定时任务示例 APP
├── sie-iidp-demo-common         公共工具类 / 基础能力
└── apps/                        应用元数据
```

## 技术栈

| 层次 | 技术 |
|------|------|
| 框架 | SIE SNEST Engine v3.0.0-RELEASE |
| 平台 SDK | SIE SNEST SDK v3.0.0-RELEASE |
| Web 框架 | Spring Boot 2.7.13 + Spring MVC |
| ORM/持久层 | MyBatis-Plus（通过 SNEST Engine 封装） |
| 定时任务 | XXL-JOB 2.3.1 |
| HTTP 客户端 | Forest 1.5.33 |
| Excel 处理 | EasyExcel 3.1.5 |
| 工具库 | Hutool 5.8.11、Guava 31.0.1、Commons-Lang3 3.12.0 |
| 消息队列 | RabbitMQ（amqp-client 5.16.0） |
| 序列化 | Jackson 2.13.0 |
| Java 版本 | JDK 11+ |

## 应用列表

| APP 名称 | displayName | 说明 |
|---------|-------------|------|
| `sie-iidp-demo-example` | 赛意IIDP Demo Example APP | 覆盖学生管理、物料管理、区域管理、订单管理、班级管理等核心示例 |
| `sie-iidp-demo-xxljob-executor` | 赛意IIDP Demo XXL-JOB APP | 演示 XXL-JOB 定时任务接入与 REST 健康检查 |

## 业务域概览

| 业务域 | 模型名（`@Model.name`） | 说明 |
|--------|-------------------------|------|
| 学生管理 | `example_student` | 学生基本信息 |
| 学生档案 | `example_student_profile` | 学生档案（含逻辑删除、自动日志） |
| 班级管理 | `example_class` | 班级数据 |
| 区域管理 | `example_area` | 省市区三级区域，含启用/禁用 |
| 物料管理 | `example_item` | 物料基本资料 |
| 物料分类 | `example_item_cate` | 物料分类树 |
| 物料扩展属性 | `example_item_attribute` | 物料属性扩展 |
| 订单管理 | `example_order` | 订单信息（逻辑删除） |
| 课程管理 | `example_course` | 课程数据 |
| 休假管理 | `example_vocation` | 休假、特殊休假 |
| 职位管理 | `job_level` | 职位等级（逻辑删除） |
| 编码序列 | `example_code_sequence` | 编码生成规则 |
| 组织层级 | `example_org_level` | 树形企业层级 |
| 物料库存 | `demo_material_inventory` | 物料库存管理（含 MBM 扩展子类） |
| 定时任务 | XXL-JOB Executor | 定时任务健康检查 |
