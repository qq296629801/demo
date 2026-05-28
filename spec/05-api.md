# 05 — API 规格

## 说明

IIDP 平台的大多数业务接口由 **SNEST Engine** 根据 `@Model` + `@MethodService` 注解**自动生成**，统一路由规则为：

```
POST /{app-name}/{model-name}/{method-name}
```

本文档列出以下两类 API：
1. **框架自动生成的标准 Model API**（CRUD + 自定义 MethodService）
2. **显式声明的 REST 接口**（TestController）

---

## 1. XXL-JOB 执行器接口

**模块**: `sie-iidp-demo-xxljob-executor`  
**Controller**: `com.sie.iidp.demo.xxljob.executor.controller.TestController`

### GET /sayHello

健康检查接口，验证 XXL-JOB 执行器是否正常启动。

| 项目 | 值 |
|------|----|
| 方法 | `GET` |
| 路径 | `/sayHello` |
| 认证 | 无 |
| 响应类型 | `text/plain` |

**响应示例**
```
hello, xxljob
```

---

## 2. 区域管理（example_area）

**模型**: `ExampleArea`  
**表名**: `example_area`

### 标准 CRUD（平台自动生成）

| 操作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 查询列表 | `POST` | `/example_area/search` | 分页 + 过滤查询 |
| 新增 | `POST` | `/example_area/create` | 创建区域记录 |
| 修改 | `POST` | `/example_area/update` | 更新区域字段 |
| 删除 | `POST` | `/example_area/delete` | 物理删除（`isLogicDelete=False`） |
| 查询单条 | `POST` | `/example_area/findById` | 按 ID 查询 |

### 自定义 MethodService

#### POST /example_area/enableDisable — 启用/禁用

**描述**: 批量启用或禁用区域记录

**请求体**
```json
{
  "rs": { "ids": ["id1", "id2"] },
  "statusFlag": "1"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `rs.ids` | `string[]` | 要操作的区域 ID 列表 |
| `statusFlag` | `string` | `"1"` 启用 / `"0"` 禁用 |

**响应**
```json
{ "success": true }
```

---

#### POST /example_area/excelExport — Excel 导出

**描述**: 导出区域列表为 Excel 文件

**请求体**
```json
{
  "rs": {},
  "filter": { "areaType": "0" },
  "properties": ["areaCode", "areaName", "areaType", "effectDay"]
}
```

**响应**: `ExportInfo` 对象，包含文件名和导出数据

---

#### POST /example_area/excelImport — Excel 导入

**描述**: 从 Excel 导入区域数据

**请求体**
```json
{
  "data": {
    "Sheet1": [
      { "编码": "001", "名称": "广东省" }
    ]
  }
}
```

**校验规则**:
- `编码` 不能为空，长度不超过 6 位
- `名称` 不能为空

---

#### POST /example_area/selectArea — 省市下拉列表

**描述**: 获取区域下拉选项（省/市）

**请求体**
```json
{
  "filter": { "areaType": "1" },
  "properties": ["areaCode", "areaName"],
  "limit": 100,
  "offset": 0,
  "order": "areaCode asc"
}
```

**响应**: `ExampleArea[]`

---

#### POST /example_area/selectCity — 查询地区（省-市-区县市-街道镇）

**描述**: 按父编码查询下级地区

**请求体**
```json
{ "filter": "01" }
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `filter` | `string` | 父区域编码，为空时默认查询 `01` 下级 |

**响应**: `ExampleArea[]`（按 `areaCode asc` 排序）

---

## 3. 物料库存管理（demo_mbm_material_inventory）

**模型**: `MbmMaterialInventory`（继承自 `demo_material_inventory`）

### POST /demo_mbm_material_inventory/create — 新增

**请求体**
```json
[
  {
    "maxCount": 100,
    "mbmMinCount": 10
  }
]
```
**响应**: `boolean`

---

### POST /demo_mbm_material_inventory/update — 修改

**请求体**: `MaterialInventoryExtendUpdateReq`（具体字段见对应 DTO）  
**响应**: `MbmMaterialInventoryRes`

---

### POST /demo_mbm_material_inventory/slot2 — 插槽2（二开扩展点）

**描述**: 隐藏 API，供 MBM 模块二次开发方覆盖扩展业务逻辑

**请求体**: `MbmMaterialInventoryDtoReq`  
**响应**: `ResponseModel`

---

### POST /demo_mbm_material_inventory/weld — 焊接（覆盖）

**描述**: 覆盖父类焊接工序，跳过装配步骤（隐藏 API）

**请求体**: `MbmMaterialInventoryDtoReq`  
**响应**: `MaterialInventoryRes`

---

## 4. 通用请求/响应格式（SNEST 标准）

### 请求体（POST）

```json
{
  "filter": {},
  "properties": ["field1", "field2"],
  "limit": 20,
  "offset": 0,
  "order": "create_date desc"
}
```

### 响应体

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "total": 0
}
```

### Filter 语法

| 操作符 | 示例 |
|--------|------|
| 等于 | `Filter.equal("areaType", "0")` |
| IN | `Filter.in("id", ["id1","id2"])` |
| 模糊搜索 | `Filter.like("areaName", "广东")` |
| 组合 AND | `Filter.and(f1, f2)` |
