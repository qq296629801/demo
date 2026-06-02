# app.json 完整参考

每个业务 App 在 `resolved` 对应的 Java 包路径下放一个 `app.json`，引擎启动时按 `apps/apps.json` 加载对应 jar，再读取 jar 内的 `app.json` 完成注册。

文件位置规则：
```
src/main/java/com/sie/iidp/{appPkg}/app.json
```

---

## 字段说明

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `name` | string | ✅ | App 唯一标识，全局不重复；通常与 Maven artifactId 一致，小写中划线 |
| `displayName` | string | ✅ | App 显示名，出现在平台 UI |
| `author` | string | | 作者标识 |
| `company` | string | | 公司标识 |
| `category` | string | ✅ | 产品线分类 key，小写，用于导航分组 |
| `categoryDesc` | string | ✅ | 产品线分类显示名 |
| `product` | string | ✅ | 产品标识，小写中划线；同产品下多个 App 填相同值 |
| `productDesc` | string | ✅ | 产品显示名 |
| `description` | string | | 简短描述 |
| `summary` | string | | 详细说明，出现在 App 市场 |
| `type` | string | ✅ | 固定填 `"SDK"`（SDK 模式业务 App） |
| `tag` | string | | 版本标签，通常填 `"master"` |
| `resolved` | string | ✅ | Java 包路径，引擎据此定位 `app.json` 和模型类；必须与实际包路径完全一致 |
| `dependencies` | array | | 依赖的其他 App name 列表；无依赖填 `[]` |
| `application` | boolean | ✅ | 固定填 `true`，表示这是一个可部署的应用 |
| `icon` | string | | 图标标识，平台内置值为 `"sie"` |
| `license` | string | | 许可证，填 `"LGPL 3.0"` |
| `version` | string | ✅ | App 版本号，如 `"1.0.0"` |
| `view` | array | ✅ | 视图 JSON 文件路径列表，相对于 `resolved` 包路径；遗漏则对应视图静默缺失 |
| `data` | array | ✅ | 种子数据文件路径列表，相对于 `resolved` 包路径；`menus.json` 必须在此登记 |
| `events` | object | | 生命周期事件钩子；无事件填 `{}` |

---

## 示例 1：标准业务 App（含 view / data）

来自 `sie-iidp-demo-example`：

```json
{
  "name": "sie-iidp-demo-example",
  "displayName": "赛意IIDP Demo Example APP",
  "author": "iidp",
  "company": "sie",
  "category": "iidp",
  "categoryDesc": "IIDP",
  "product": "sie-iidp-demo",
  "productDesc": "SIE IIDP Demo",
  "description": "SIE-IIDP-DEMO-EXAMPLE",
  "summary": "示例模块 APP，以 IIDP 平台原子能力为粒度开发示例源码",
  "type": "SDK",
  "tag": "master",
  "resolved": "com.sie.iidp.example",
  "dependencies": [],
  "application": true,
  "icon": "sie",
  "license": "LGPL 3.0",
  "version": "1.0.0",
  "view": [
    "classmgr/views/example_class_view.json",
    "studentmgr/views/example_student_view.json",
    "itemmgr/views/example_item_view.json",
    "ordermgr/views/example_order_view.json"
  ],
  "data": [
    "views/menus.json",
    "data/yes_no.json",
    "data/unit_type.json",
    "data/item_attribute_type.json",
    "data/example_order.json",
    "data/job_level.json"
  ],
  "events": {}
}
```

---

## 示例 2：带生命周期事件的 App（events）

来自 `sie-iidp-demo-xxljob-executor`，App 启动/卸载时触发模型服务：

```json
{
  "name": "sie-iidp-demo-xxljob-executor",
  "displayName": "赛意IIDP Demo XXL-JOB APP",
  "author": "iidp",
  "company": "sie",
  "category": "iidp",
  "categoryDesc": "IIDP",
  "product": "sie-iidp-demo",
  "productDesc": "SIE IIDP Demo",
  "description": "SIE-IIDP-DEMO-EXAMPLE",
  "summary": "XXL-JOB 模块 APP，展示定时任务集成方式",
  "type": "SDK",
  "tag": "master",
  "resolved": "com.sie.iidp.demo.xxljob.executor",
  "dependencies": [],
  "application": true,
  "icon": "sie",
  "license": "LGPL 3.0",
  "version": "1.0.0",
  "events": {
    "startUp": [
      "xxljob_spring_app::start"
    ],
    "unInstall": [
      "xxljob_spring_app::stop"
    ]
  }
}
```

`events` 字段格式：

| 事件 | 触发时机 | 值格式 |
|---|---|---|
| `startUp` | 引擎加载该 App 后 | `["modelName::serviceName"]` |
| `unInstall` | 该 App 被卸载前 | `["modelName::serviceName"]` |

> `"xxljob_spring_app::start"` 表示调用 `@Model(name="xxljob_spring_app")` 模型上的 `start` 服务。

---

## 关键规则

1. **`resolved` 必须与 Java 实际包路径完全一致**，否则引擎找不到模型类。
2. **`view` 路径相对于 `resolved` 包目录**，例如 `resolved = "com.sie.iidp.example"`，则 `"classmgr/views/xxx.json"` 对应物理路径 `src/main/java/com/sie/iidp/example/classmgr/views/xxx.json`。
3. **`data` 同理**，`menus.json` 必须登记，否则菜单不会加载。
4. **`view` / `data` 遗漏不报错**，引擎静默跳过，排查时只能通过功能缺失现象发现。
5. **`name` 全局唯一**，与 `apps/apps.json` 的 jar 文件名对应（jar 名含 `name` 前缀）。
6. **新增 App 后必须同时在 `apps/apps.json` 的 `apps.SDK` 中追加 jar 文件名**，否则引擎不加载。
