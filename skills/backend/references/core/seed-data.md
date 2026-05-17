---
name: seed-data-guide
description: 种子数据编写指南，包括数据种子、附件种子和各种占位符的写法
---

# 种子数据编写指南

本指南介绍如何在 IIDP 工程中编写种子数据，包括数据种子、附件种子和各种占位符的使用方法。

## 目录结构

种子数据文件放在应用模块的 `src/main/java/com/<package>/` 目录下：

```
<app-module>/
└── src/main/java/com/sie/iidp/<app>/
    ├── app.json              # 应用配置文件
    ├── data/                 # 数据种子目录
    │   └── <model_name>.json # 数据种子文件
    └── file/                 # 附件种子目录
        ├── <file_config>.json # 附件配置文件
        └── document/         # 附件文件存放目录
            └── <files>       # 实际文件 (png, xlsx, pdf 等)
```

## 零、字典种子数据（data/*_dict.json 或 data/yes_no.json）

字典种子固定写法：先写 `base_dict_type`（字典类型），再写 `base_dict_value`（字典值）。字典 typeCode 必须与模型 `@Dict(typeCode = "xxx")` 一致。

**完整示例（来自工程 yes_no.json + unit_type.json）**：

```json
{
  "data": {
    "yes_no": {
      "model": "base_dict_type",
      "properties": {
        "dictName": "是否",
        "dictType": "yes_no",
        "status": 0
      }
    },
    "yes_no_no": {
      "model": "base_dict_value",
      "properties": {
        "dictLabel": "否",
        "dictValue": "0",
        "dictType": "yes_no",
        "status": 0,
        "dictSort": 1
      }
    },
    "yes_no_yes": {
      "model": "base_dict_value",
      "properties": {
        "dictLabel": "是",
        "dictValue": "1",
        "dictType": "yes_no",
        "status": 0,
        "dictSort": 2
      }
    },
    "unit_type": {
      "model": "base_dict_type",
      "properties": {
        "dictName": "单位类型",
        "dictType": "unitType",
        "status": 0
      }
    },
    "unit_type_length": {
      "model": "base_dict_value",
      "properties": {
        "dictLabel": "长度",
        "dictValue": "1",
        "dictType": "unitType",
        "status": 0,
        "dictSort": 1
      }
    },
    "unit_type_weight": {
      "model": "base_dict_value",
      "properties": {
        "dictLabel": "质量",
        "dictValue": "2",
        "dictType": "unitType",
        "status": 0,
        "dictSort": 2
      }
    }
  }
}
```

字典字段规则：

| 字段 | 说明 |
|---|---|
| `dictType` | 与 Java `@Dict(typeCode = "xxx")` 完全一致，小写下划线 |
| `dictValue` | 存入数据库的实际值，与模型字段类型一致（String 写 `"0"`，Integer 对应 `@Dict` 时也写字符串） |
| `dictLabel` | 前端显示文本 |
| `dictSort` | 下拉选项排序，数字越小越靠前 |
| `status` | 固定填 `0`（启用） |

同一文件可包含多个字典类型，`base_dict_type` 必须在对应的 `base_dict_value` 之前出现。

---

## 一、普通业务种子数据

### 审计字段（来自工程 example_unit.json）

业务种子数据通常需要填写 `create_user`、`create_date`、`update_user`、`update_date` 四个审计字段。`create_user` / `update_user` 填写对应用户的 **seed key**（如 `rbac_user_admin`），引擎会自动解析为该用户的 ID。

```json
{
  "data": {
    "example_unit_1": {
      "model": "example_unit",
      "properties": {
        "unitCode": "1111",
        "unitName": "百万",
        "unitType": 1,
        "effectDay": "2024-05-20 09:00:00",
        "isEnable": 1,
        "create_user": "rbac_user_admin",
        "create_date": "2024-05-20 09:00:00",
        "update_user": "rbac_user_admin",
        "update_date": "2024-05-20 09:00:00"
      }
    },
    "example_unit_2": {
      "model": "example_unit",
      "properties": {
        "unitCode": "2222",
        "unitName": "千万",
        "unitType": 1,
        "effectDay": "2024-05-20 09:00:00",
        "isEnable": 1,
        "create_user": "rbac_user_admin",
        "create_date": "2024-05-20 09:00:00",
        "update_user": "rbac_user_admin",
        "update_date": "2024-05-20 09:00:00"
      }
    }
  }
}
```

审计字段规则：

| 字段 | 值写法 | 说明 |
|---|---|---|
| `create_user` | `"rbac_user_admin"` | 写 seed key，引擎解析为 ID |
| `update_user` | `"rbac_user_admin"` | 同上 |
| `create_date` | `"2024-05-20 09:00:00"` | 字符串格式 `yyyy-MM-dd HH:mm:ss` |
| `update_date` | `"2024-05-20 09:00:00"` | 同上 |

---

### 树形结构 parentId（来自工程 job_level.json）

树形模型的 `parentId` 使用**纯字符串 seed key**（不加 `@ref` 包装），引擎按先后顺序解析，父节点必须在子节点之前定义。根节点 `parentId` 填 `null`。

```json
{
  "data": {
    "demo_job_level_example_ceo": {
      "model": "job_level",
      "properties": {
        "jobTitle": "CEO",
        "parentId": null,
        "description": "公司总裁",
        "create_user": "rbac_user_admin",
        "create_date": "2024-06-24 09:00:00",
        "update_user": "rbac_user_admin",
        "update_date": "2024-06-24 09:00:00"
      }
    },
    "demo_job_level_example_hr_manager": {
      "model": "job_level",
      "properties": {
        "jobTitle": "HR经理",
        "parentId": "demo_job_level_example_ceo",
        "description": "公司HR经理",
        "create_user": "rbac_user_admin",
        "create_date": "2024-06-24 09:00:00",
        "update_user": "rbac_user_admin",
        "update_date": "2024-06-24 09:00:00"
      }
    },
    "demo_job_level_example_product_manager": {
      "model": "job_level",
      "properties": {
        "jobTitle": "产品经理主管",
        "parentId": "demo_job_level_example_ceo",
        "description": "公司产品经理主管",
        "create_user": "rbac_user_admin",
        "create_date": "2024-06-24 09:00:00",
        "update_user": "rbac_user_admin",
        "update_date": "2024-06-24 09:00:00"
      }
    },
    "demo_job_level_example_product_leader": {
      "model": "job_level",
      "properties": {
        "jobTitle": "高级产品经理",
        "parentId": "demo_job_level_example_product_manager",
        "description": "高级产品经理",
        "create_user": "rbac_user_admin",
        "create_date": "2024-06-24 09:00:00",
        "update_user": "rbac_user_admin",
        "update_date": "2024-06-24 09:00:00"
      }
    }
  }
}
```

> **parentId 写法对比**：
> - 树形自引用：`"parentId": "demo_job_level_example_ceo"` — 纯字符串 seed key，无 `@ref`
> - 跨模型外键：`"tenant_id": { "@ref": "rbac_tenant_root" }` — 对象形式 `@ref`
>
> 两者不同，树形 `parentId` 直接写 seed key 字符串即可，根节点填 `null`。

---

## 三、数据种子基本结构与策略

### 基本结构

```json
{
  "data": {
    "<seed_key>": {
      "model": "<model_name>",
      "isGlobal": true,           // 可选，平台级数据
      "policy": "neverUpdate",    // 可选，更新策略
      "properties": {
        "<field1>": "<value1>",
        "<field2>": "<value2>"
      }
    }
  }
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `seed_key` | string | 是 | 种子数据的唯一标识，用于 @ref 引用 |
| `model` | string | 是 | 对应的模型名称 |
| `isGlobal` | boolean | 否 | true=平台级数据，false=租户级数据（默认false） |
| `policy` | string | 否 | 更新策略：`neverUpdate`=永不更新，`alwaysUpdate`=总是更新 |
| `properties` | object | 是 | 数据字段键值对 |

### 基础数据类型示例

```json
{
  "data": {
    "basic_string": {
      "model": "test_seed_item",
      "properties": {
        "name": "string_test",
        "description": "字符串类型"
      }
    },
    "basic_number": {
      "model": "test_seed_item",
      "properties": {
        "name": "number_test",
        "sequence": 100,
        "price": 99.99
      }
    },
    "basic_boolean": {
      "model": "test_seed_item",
      "properties": {
        "name": "boolean_test",
        "enabled": true,
        "deleted": false
      }
    },
    "basic_array": {
      "model": "test_seed_item",
      "properties": {
        "name": "array_test",
        "tags": "[\"tag1\", \"tag2\", \"tag3\"]"
      }
    },
    "basic_json": {
      "model": "test_seed_item",
      "properties": {
        "name": "json_test",
        "config": "{\"key1\": \"value1\", \"key2\": 123}"
      }
    }
  }
}
```

### isGlobal + policy 组合示例

```json
{
  "data": {
    "global_never_update": {
      "model": "test_seed_item",
      "isGlobal": true,
      "policy": "neverUpdate",
      "properties": {
        "name": "global_never_item",
        "description": "平台级永不更新"
      }
    },
    "tenant_always_update": {
      "model": "test_seed_item",
      "isGlobal": false,
      "policy": "alwaysUpdate",
      "properties": {
        "name": "tenant_always_item",
        "description": "租户级总是更新"
      }
    }
  }
}
```

## 四、占位符语法

### 1. @ref - 引用其他种子数据的ID

用于引用同一应用或跨应用的种子数据ID。

**对象形式（用于ID字段）：**
```json
{
  "item_id": { "@ref": "ref_base_item" }
}
```

**字符串内嵌形式：**
```json
{
  "expression": "item:@ref(ref_base_item):read"
}
```

**跨应用引用（带应用前缀）：**
```json
{
  "user_id": { "@ref": "base.rbac_user_superuser" }
}
```

### 2. @eval - 数组表达式

用于 ManyToMany 多对多关系字段，格式为 `[type, id, extra]`：

```json
{
  "role_ids": [
    { "@eval": "[4, @ref(rbac_role_admin), 0]" }
  ]
}
```

**多个关联：**
```json
{
  "role_ids": [
    { "@eval": "[4, @ref(rbac_role_admin), 0]" },
    { "@eval": "[4, @ref(rbac_role_user), 0]" }
  ]
}
```

### 3. @fileId / @fileUrl / @filePath - 文件占位符

用于引用附件种子数据：

```json
{
  "logo_id": "@fileId(test_logo)",
  "logo_url": "@fileUrl(test_logo)",
  "logo_path": "@filePath(test_logo)"
}
```

| 占位符 | 说明 |
|--------|------|
| `@fileId(<key>)` | 替换为文件的数据库ID |
| `@fileUrl(<key>)` | 替换为文件的访问URL |
| `@filePath(<key>)` | 替换为文件的存储路径 |

### 4. ${} - 变量替换

用于运行时变量替换：

```json
{
  "config": "{\"row\":{\"R\":[{\"field\":\"tenant_id\",\"op\":\"=\",\"value\":\"${meta.user.tenantId}\"}]}}"
}
```

常用变量：
- `${meta.user.tenantId}` - 当前租户ID
- `${meta.user.orgId}` - 当前组织ID

## 五、附件种子 (file/*.json)

### 附件配置文件结构

```json
{
  "file": {
    "<file_key>": {
      "name": "<display_name>",
      "path": "file/document/<filename>"
    }
  }
}
```

### 示例

**file/test_file.json:**
```json
{
  "file": {
    "test_logo": {
      "name": "test_logo.png",
      "path": "file/document/test_logo.png"
    },
    "import_template": {
      "name": "导入模板.xlsx",
      "path": "file/document/导入模板.xlsx"
    }
  }
}
```

**目录结构：**
```
file/
├── test_file.json
└── document/
    ├── test_logo.png
    └── 导入模板.xlsx
```

### 在数据种子中引用附件

```json
{
  "data": {
    "item_with_file": {
      "model": "test_seed_item",
      "properties": {
        "name": "file_test",
        "logo_id": "@fileId(test_logo)",
        "config": "@fileUrl(import_template)"
      }
    }
  }
}
```

## 六、复杂规则配置 (config 字段)

用于权限规则等复杂配置：

### 简单条件
```json
{
  "config": "{\"usable\":true,\"col\":{\"all\":true},\"row\":{\"R\":[{\"field\":\"status\",\"op\":\"=\",\"value\":\"published\"}]}}"
}
```

### AND 条件 (&)
```json
{
  "config": "{\"usable\":true,\"col\":{\"all\":true},\"row\":{\"R\":[\"&\",{\"field\":\"status\",\"op\":\"=\",\"value\":\"published\"},{\"field\":\"enabled\",\"op\":\"=\",\"value\":\"true\"}]}}"
}
```

### OR 条件 (|)
```json
{
  "config": "{\"usable\":true,\"col\":{\"all\":true},\"row\":{\"R\":[\"|\",{\"field\":\"status\",\"op\":\"=\",\"value\":\"draft\"},{\"field\":\"status\",\"op\":\"=\",\"value\":\"published\"}]}}"
}
```

### CRUD 操作规则
```json
{
  "config": "{\"usable\":true,\"col\":{\"all\":true},\"row\":{\"R\":[{\"field\":\"deleted\",\"op\":\"=\",\"value\":\"false\"}],\"C\":[{\"field\":\"status\",\"op\":\"=\",\"value\":\"draft\"}],\"D\":[{\"field\":\"status\",\"op\":\"!=\",\"value\":\"published\"}],\"U\":[{\"field\":\"enabled\",\"op\":\"=\",\"value\":\"true\"}]}}"
}
```

## 七、通配符表达式

用于权限表达式：

```json
{
  "expression": "base.master#IN_APP_ALL"
}
```

| 通配符 | 说明 |
|--------|------|
| `#IN_APP_ALL` | 应用内所有权限 |
| `!` 前缀 | 排除特定功能 |
| `.*` 后缀 | 匹配所有子项 |

**示例：**
```json
{
  "include": "base.master#IN_APP_ALL",
  "exclude": "!function.base.rbac_user_permission_menu",
  "wildcard": "function.base.menu.*"
}
```

## 八、完整示例

### 用户数据种子 (data/rbac_user.json)

```json
{
  "data": {
    "rbac_role_admin": {
      "model": "rbac_role",
      "properties": {
        "name": "管理员",
        "code": "admin",
        "is_admin": 1
      }
    },
    "rbac_user_admin": {
      "model": "rbac_user",
      "properties": {
        "name": "管理员",
        "login": "admin",
        "password": "admin",
        "email": "admin@example.com",
        "role_ids": [
          { "@eval": "[4, @ref(rbac_role_admin), 0]" }
        ]
      }
    }
  }
}
```

### 附件种子 (file/logo_file.json)

```json
{
  "file": {
    "app_logo": {
      "name": "应用图标.png",
      "path": "file/document/app_logo.png"
    }
  }
}
```

### 引用附件的数据种子

```json
{
  "data": {
    "app_config": {
      "model": "app_setting",
      "properties": {
        "name": "应用配置",
        "logo": "@fileId(app_logo)"
      }
    }
  }
}
```

## 九、Java 模型类与注解

种子数据的字段名与 Java 模型类的注解密切相关。理解这些注解有助于正确编写种子数据。

### 1. @Model - 模型注解

定义模型的基本信息，`name` 属性对应种子数据中的 `model` 字段。

```java
@Model(displayName = "用户表", name = "rbac_user", type = Model.ModelType.Buss)
public class User extends BaseModel<User> {
    // ...
}
```

| 属性 | 说明 | 种子数据对应 |
|------|------|--------------|
| `name` | 模型名称 | `"model": "rbac_user"` |
| `displayName` | 显示名称 | - |
| `type` | 模型类型 (Buss/Meta) | - |
| `isAutoLog` | 是否自动记录日志 | - |

### 2. @Property - 属性注解

定义字段的元数据，Java 字段名即为种子数据中的属性名。

```java
@Property(displayName = "登录名", toolTips = "账号登录")
@Validate.NotBlank
@Validate.Unique
private String login;

@Property(displayName = "状态", defaultValue = "0")
@Selection(values = {
    @Option(label = "正常", value = "0", selected = true),
    @Option(label = "锁定", value = "1")
})
private String status;
```

| 属性 | 说明 | 种子数据示例 |
|------|------|--------------|
| `displayName` | 显示名称 | - |
| `defaultValue` | 默认值 | 不填则使用默认值 |
| `dataType` | 数据类型 | DATE_TIME, FILE 等 |
| `length` | 字段长度 | - |
| `store` | 是否存储 | false 表示计算字段，不写入种子 |
| `columnName` | 数据库列名 | 如与字段名不同需注意 |

### 3. @ManyToOne - 多对一关系

定义外键关联，`@JoinColumn` 的 `name` 属性决定种子数据中的字段名。

```java
@ManyToOne(displayName = "租户", targetModel = "rbac_tenant")
@JoinColumn(name = "tenant_id", referencedProperty = "id")
private Map<String, Object> tenant_id;

@ManyToOne(displayName = "组织", targetModel = "rbac_organization")
@JoinColumn(name = "org_id", referencedProperty = "id")
private Map<String, Object> org_id;
```

**种子数据写法：**
```json
{
  "properties": {
    "tenant_id": { "@ref": "rbac_tenant_root" },
    "org_id": { "@ref": "org_headquarters" }
  }
}
```

### 4. @ManyToMany - 多对多关系（重点）

多对多关系是种子数据中最复杂的部分，需要理解 `@JoinTable` 注解。

```java
@ManyToMany(targetModel = "rbac_role")
@JoinTable(
    name = "rbac_role_user",                                    // 中间表名
    joinColumns = @JoinColumn(name = "user_ids", nullable = false),      // 当前模型侧字段
    inverseJoinColumns = @JoinColumn(name = "role_ids", nullable = false) // 目标模型侧字段
)
@Property(displayName = "用户角色")
private List<Role> role_ids;
```

**关键理解：**

| 注解属性 | 说明 | 示例值 |
|----------|------|--------|
| `@JoinTable.name` | 中间表名称 | `rbac_role_user` |
| `joinColumns.name` | 当前模型在中间表的字段名 | `user_ids` |
| `inverseJoinColumns.name` | 目标模型在中间表的字段名 | `role_ids` |

**种子数据写法：**
```json
{
  "rbac_user_admin": {
    "model": "rbac_user",
    "properties": {
      "name": "管理员",
      "login": "admin",
      "role_ids": [
        { "@eval": "[4, @ref(rbac_role_admin), 0]" }
      ]
    }
  }
}
```

**@eval 数组格式解释：**
```
[Command, 关联ID, 排序值]
```

| Command | 说明 |
|---------|------|
| `4` | 添加关联 (ADD) |
| `3` | 删除关联 (DELETE) |
| `6` | 替换全部关联 (REPLACE) |

### 5. @Selection + @Option - 选择项

定义枚举值，种子数据中直接使用 `value` 值。

```java
@Selection(values = {
    @Option(label = "正常", value = "0", selected = true),
    @Option(label = "锁定", value = "1"),
    @Option(label = "禁用", value = "2")
})
@Property(displayName = "状态", defaultValue = "0")
private String status;
```

**种子数据写法：**
```json
{
  "properties": {
    "status": "0"
  }
}
```

### 6. 字段名映射规则

| Java 字段名 | 种子数据字段名 | 说明 |
|-------------|----------------|------|
| `login` | `login` | 普通字段，名称一致 |
| `roleType` | `roleType` | 驼峰命名保持不变 |
| `role_ids` | `role_ids` | 下划线命名保持不变 |
| `tenant_id` | `tenant_id` | ManyToOne 外键字段 |
| `@Property(columnName="xxx")` | 使用 Java 字段名 | columnName 仅影响数据库列名 |

### 7. 完整示例：User 模型种子数据

根据 User.java 的注解定义，编写种子数据：

```json
{
  "data": {
    "rbac_role_admin": {
      "model": "rbac_role",
      "properties": {
        "name": "管理员",
        "code": "admin",
        "is_admin": true,
        "roleType": "0",
        "roleDelete": "0"
      }
    },
    "rbac_user_admin": {
      "model": "rbac_user",
      "properties": {
        "login": "admin",
        "name": "管理员",
        "password": "admin123",
        "status": "0",
        "gender": "0",
        "email": "admin@example.com",
        "mobile": "13800138000",
        "userType": "0",
        "tenant_id": { "@ref": "base.rbac_tenant_root" },
        "org_id": { "@ref": "base.rbac_org_root" },
        "role_ids": [
          { "@eval": "[4, @ref(rbac_role_admin), 0]" }
        ]
      }
    }
  }
}
```

## 十、注意事项

1. **seed_key 唯一性**：同一应用内 seed_key 必须唯一
2. **引用顺序**：被引用的数据必须在引用之前定义（或在同一文件中）
3. **跨应用引用**：使用 `<app>.<seed_key>` 格式
4. **字符串转义**：JSON 字符串中的引号需要转义 `\"`
5. **附件路径**：相对于应用模块的 classpath 根目录
6. **policy 默认值**：不指定时默认为空（根据系统配置决定行为）
7. **isGlobal 默认值**：不指定时默认为 false（租户级数据）
8. **ManyToMany 字段名**：使用 `@JoinTable.inverseJoinColumns.name` 中定义的名称
9. **store=false 字段**：不要在种子数据中设置，这些是计算字段
10. **Selection 字段**：使用 `@Option.value` 中定义的值，而非 label
