# 05 — API 规格

## 通用规范

### 请求格式

```
Base URL: http://{gateway-host}/
Authorization: Bearer {access-token}
Content-Type: application/json
```

### 统一响应格式

```json
{
  "code": 0,
  "data": {},
  "msg": "成功"
}
```

| code | 含义 |
|------|------|
| `0` | 成功 |
| `401` | 未登录 |
| `403` | 无权限 |
| `500` | 系统错误 |
| 业务错误码 | 见各模块错误码枚举 |

### 分页请求格式

```json
{
  "pageNo": 1,
  "pageSize": 10
}
```

### 分页响应格式

```json
{
  "code": 0,
  "data": {
    "list": [],
    "total": 100
  }
}
```

---

## 1. 认证模块（/system/auth）

### POST /system/auth/login — 账号密码登录

**请求体**
```json
{
  "username": "admin",
  "password": "admin123",
  "captchaVerification": "验证码token"
}
```

**响应**
```json
{
  "code": 0,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresTime": 1700000000000,
    "userId": 1,
    "userInfo": {
      "id": 1,
      "nickname": "芋道源码",
      "avatar": "http://..."
    }
  }
}
```

---

### GET /system/auth/get-permission-info — 获取权限信息

**响应**（用于前端菜单渲染和按钮权限控制）
```json
{
  "code": 0,
  "data": {
    "user": { "id": 1, "nickname": "admin" },
    "roles": ["super_admin"],
    "permissions": ["system:user:create", "system:user:query"],
    "menus": [
      {
        "id": 1,
        "name": "系统管理",
        "path": "/system",
        "component": "Layout",
        "children": []
      }
    ]
  }
}
```

---

## 2. 用户管理（/system/user）

### POST /system/user/create

```json
{
  "username": "test_user",
  "nickname": "测试用户",
  "mobile": "13800138000",
  "email": "test@example.com",
  "deptId": 100,
  "postIds": [1, 2],
  "roleIds": [1],
  "sex": 1,
  "status": 0
}
```

### GET /system/user/page

**Query 参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `pageNo` | int | 页码，默认 1 |
| `pageSize` | int | 每页条数，默认 10 |
| `username` | string | 用户名（模糊） |
| `mobile` | string | 手机号 |
| `status` | int | 状态 0=启用 1=禁用 |
| `deptId` | long | 部门 ID（含子部门） |
| `createTime` | date[] | 创建时间范围 |

---

## 3. 工作流（/bpm）

### POST /bpm/process-instance/create — 发起流程

```json
{
  "processDefinitionId": "leave:1:xxx",
  "variables": {
    "startUserId": 1,
    "reason": "个人假期",
    "startTime": "2024-01-01",
    "endTime": "2024-01-03",
    "day": 3
  }
}
```

### GET /bpm/task/todo-page — 待办任务分页

**响应**
```json
{
  "code": 0,
  "data": {
    "list": [
      {
        "id": "task-id",
        "name": "部门经理审批",
        "processInstanceId": "instance-id",
        "processDefinitionName": "请假流程",
        "createTime": "2024-01-01 10:00:00",
        "assigneeUser": { "id": 1, "nickname": "张三" }
      }
    ],
    "total": 5
  }
}
```

---

## 4. CRM 客户（/crm/customer）

### POST /crm/customer/create

```json
{
  "name": "测试客户公司",
  "mobile": "13800138000",
  "phone": "021-12345678",
  "email": "contact@company.com",
  "areaId": 110100,
  "detailAddress": "XX路XX号",
  "ownerUserId": 1,
  "followUpStatus": false,
  "lockStatus": false,
  "industryId": 1,
  "levelId": 1,
  "sourceId": 1
}
```

### GET /crm/customer/page

| 参数 | 说明 |
|------|------|
| `name` | 客户名称（模糊） |
| `mobile` | 手机号 |
| `followUpStatus` | 跟进状态 |
| `lockStatus` | 锁定状态 |
| `ownerUserId` | 归属人 |
| `sceneType` | 场景：0=我的客户 1=下属 2=公海 |

---

## 5. 商城商品（/product/spu）

### POST /product/spu/create — 创建商品

```json
{
  "name": "测试商品",
  "categoryId": 1,
  "brandId": 1,
  "picUrl": "http://...",
  "sliderPicUrls": ["http://..."],
  "introduction": "商品简介",
  "description": "商品详情 HTML",
  "sort": 0,
  "giveIntegral": 0,
  "price": 9900,
  "marketPrice": 12900,
  "costPrice": 5000,
  "stock": 100,
  "deliveryTemplateId": 1,
  "specType": 2,
  "skus": [
    {
      "properties": [{ "propertyId": 1, "valueId": 1 }],
      "price": 9900,
      "stock": 50,
      "picUrl": "http://..."
    }
  ]
}
```

---

## 6. 支付（/pay/order）

### POST /pay/order/create — 创建支付单

```json
{
  "appId": 1,
  "userIp": "127.0.0.1",
  "merchantOrderId": "ORDER_20240101_001",
  "subject": "商品购买",
  "body": "商品描述",
  "price": 9900,
  "expireTime": "2024-01-01 12:00:00"
}
```

**响应**
```json
{
  "code": 0,
  "data": {
    "id": 1,
    "status": 0,
    "channelOrderNo": null
  }
}
```

### POST /pay/order/submit — 提交支付（选择支付渠道）

```json
{
  "id": 1,
  "channelCode": "wx_pub",
  "userIp": "127.0.0.1",
  "channelExtras": {
    "openid": "oxxx"
  }
}
```

---

## 7. AI 聊天（/ai/chat/message）

### POST /ai/chat/message/send — 发送消息

```json
{
  "conversationId": 1,
  "content": "你好，请介绍一下自己",
  "useContext": true
}
```

**响应**（SSE 流式）
```
data: {"id":1,"content":"你好！","finishReason":null}
data: {"id":1,"content":"我是","finishReason":null}
data: {"id":1,"content":"AI助手","finishReason":"stop"}
```

---

## 8. 文件上传（/infra/file）

### POST /infra/file/upload — 上传文件

```
Content-Type: multipart/form-data

file: (binary)
path: avatar/user_1.png
```

**响应**
```json
{
  "code": 0,
  "data": "https://minio.example.com/bucket/avatar/user_1.png"
}
```
