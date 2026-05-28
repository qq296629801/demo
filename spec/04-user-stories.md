# 04 — 用户故事

## 角色定义

| 角色 | 说明 |
|------|------|
| IIDP 新手开发者 | 首次接触 SNEST Engine，需要通过示例学习各类能力 |
| 平台管理员 | 负责配置区域、职位、编码序列等基础数据 |
| 业务用户 | 使用学生/物料/订单等功能的最终用户 |
| 二开方（ISV） | 基于 IIDP 平台进行二次开发的独立软件供应商 |

---

## 用户故事列表

| 编号 | 角色 | 故事 | 验收标准 |
|------|------|------|---------|
| US-001 | 新手开发者 | 我希望能看到一个最简单的 `@Model` + `@Property` 示例，以便我了解如何定义一个业务模型 | 能看到 `ExampleArea` 类，运行后表自动创建，CRUD 接口自动生成 |
| US-002 | 新手开发者 | 我希望能了解 `@Validate` 注解的用法，以便我为字段添加校验规则 | `ExampleArea.areaCode` 非空校验生效，入参为空时返回 ValidationException |
| US-003 | 新手开发者 | 我希望了解如何实现"启用/禁用"批量操作，以便我在其他模块复用该模式 | `enableDisable` 方法按 ID 列表批量更新 `isEnable` 字段 |
| US-004 | 新手开发者 | 我希望了解 Excel 导入导出的标准实现，以便我在自己的模块中快速接入 EasyExcel | `excelExport` 返回正确的 ExportInfo，`excelImport` 数据写入数据库 |
| US-005 | 新手开发者 | 我希望了解 `@Model(isShard=True)` 的用法，以便我对海量数据进行分表存储 | `example_area` 按月自动分表，数据写入对应月份子表 |
| US-006 | 新手开发者 | 我希望了解 MBM 继承模式，以便我对平台模型进行安全的扩展 | `MbmMaterialInventory` 继承 `demo_material_inventory`，覆盖 `weld` 方法有效 |
| US-007 | 新手开发者 | 我希望了解 `@InjectMeta` 服务注入的用法，以便我将业务逻辑抽到 Service 层 | `MbmMaterialInventoryService` 成功注入 Model，`create` 调用链路正常 |
| US-008 | 新手开发者 | 我希望了解 XXL-JOB 执行器的接入方式，以便我实现定时任务 | 执行器注册成功，`GET /sayHello` 返回 200 和 "hello, xxljob" |
| US-009 | 平台管理员 | 我希望能管理省-市-区-街道四级区域数据，以便系统中的地址选择组件能正常工作 | `selectCity` 按父编码返回下级区域列表，支持四级联动 |
| US-010 | 平台管理员 | 我希望批量导入区域数据，以便快速完成初始化配置 | Excel 导入支持批量写入，错误行有明确提示 |
| US-011 | 业务用户 | 我希望能按班级筛选学生列表，以便快速找到所需学生信息 | `example_student` 支持 `Filter.equal("classId", ...)` 过滤 |
| US-012 | 业务用户 | 我希望能管理物料分类，以便对物料进行结构化归类 | `example_item_cate` 支持树形父子结构，子分类正确关联父分类 |
| US-013 | 二开方 | 我希望通过插槽机制扩展平台模型的业务逻辑，以便在不修改平台代码的前提下注入自定义处理 | `slot2` 方法在二开层被覆盖后，调用链路走二开实现 |
| US-014 | 二开方 | 我希望了解 `hiddenApi` 的作用，以便我知道哪些接口不对外开放 | `hiddenApi=true` 的 MethodService 不出现在公开 API 列表中 |
