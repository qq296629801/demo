# 菜单种子数据参考（menus.json）

本文件补充菜单配置的后端生成要点。菜单负责把 `model`、`view`、权限、过滤条件和页面类型串起来，是后端 Demo 能否在前端打开的关键。需要完整底稿时读取 `../complete/basic-view.md` 或 `../complete/uiview-guide.md`。

## 文件位置

`src/main/java/com/sie/iidp/{appPkg}/data/menus.json`

必须在 `app.json` 的 `data` 数组中登记此路径，否则菜单不会被加载：

```json
{
  "data": [
    "data/menus.json"
  ]
}
```

---

## 基础结构（一级菜单 + 功能菜单）

```json
{
  "menus": {
    "{appPkg}_{moduleName}_root_menu": {
      "name": "{appPkg}_{moduleName}_root_menu",
      "display_name": "{模块中文名}",
      "sequence": 10,
      "active": true
    },
    "{appPkg}_{entity}_menu": {
      "name": "{appPkg}_{entity}_menu",
      "display_name": "{功能中文名}",
      "sequence": 10,
      "active": true,
      "model": "{model_name}",
      "view": "{model_name}_grid,{model_name}_search,{model_name}_form",
      "parent_ids": {
        "@ref": "{appPkg}_{moduleName}_root_menu"
      }
    }
  }
}
```

`parent_ids: { "@ref": "..." }` 是推荐写法。`parent_id` 直接字符串写法不推荐。

---

## 字段说明

| 字段 | 必填 | 说明 |
|---|---|---|
| `name`（key） | 是 | 全局唯一标识，建议以 `appPkg` 开头，避免与其他 app 冲突 |
| `display_name` | 是 | 菜单显示名称 |
| `sequence` | 是 | 同级菜单排序，数字越小越靠前 |
| `active` | 是 | 是否启用，通常为 `true` |
| `model` | 功能菜单必填 | 关联的模型名，即 Java `@Model(name)` |
| `view` | 功能菜单必填 | 视图 key。多个后端视图用英文逗号连接 |
| `parent_ids` | 子菜单必填 | 引用式父菜单，推荐写法 `{ "@ref": "父菜单name" }` |
| `parent_id` | 不推荐 | 直接字符串，不推荐使用 |
| `filter` | 可选 | 进入该菜单后统一追加到接口请求的过滤条件 |
| `config` | 可选 | 按接口或字段追加过滤条件、缓存等菜单级配置 |

---

## 完整示例（来自工程 example/views/menus.json）

```json
{
  "menus": {
    "demo_example_menu": {
      "sequence": "1",
      "name": "demo_example_menu",
      "display_name": "Example(示例)"
    },
    "demo_example_unit_menu": {
      "name": "demo_example_unit_menu",
      "display_name": "Example-单位",
      "sequence": "1",
      "active": true,
      "model": "example_unit",
      "view": "demo_example_unit_grid,demo_example_unit_grid_search,demo_example_unit_grid_param_form",
      "parent_ids": {
        "@ref": "demo_example_menu"
      }
    },
    "demo_example_item_menu": {
      "name": "demo_example_item_menu",
      "display_name": "Example-物料",
      "sequence": "2",
      "active": true,
      "model": "example_item",
      "view": "demo_example_item_grid,demo_example_item_grid_search,demo_example_item_grid_param_form",
      "parent_ids": {
        "@ref": "demo_example_menu"
      }
    },
    "demo_example_org_level_menu": {
      "name": "demo_example_org_level_menu",
      "display_name": "Example-企业层级(树形菜单)",
      "sequence": "4",
      "active": true,
      "model": "example_org_level",
      "view": "demo_example_org_level_tree,demo_example_org_level_form,demo_example_org_level_grid,demo_example_org_level_search",
      "parent_ids": {
        "@ref": "demo_example_menu"
      }
    }
  }
}
```

规律说明：
- `sequence` 在工程中写**字符串**（`"1"`、`"2"`），而非整数。
- 根菜单（分组菜单）**不写** `model` / `view` / `active`，只有 `name` / `display_name` / `sequence`。
- 功能菜单 `active: true` 必须写。
- 树视图菜单的 `view` 通常包含 4 个 key：`tree,form,grid,search`。
- `view` 中各 key 顺序影响默认打开视图，列表类菜单通常 grid 在最前。

---

## 菜单复用后端视图

复用页面时，功能菜单的 `model`、`view` 与被复用菜单一致，但菜单自身的 `name`、路径/URL（若有）和 `sequence` 必须唯一。

典型用途：同一模型按不同业务类型拆成多个菜单，每个菜单通过 `filter` 固定范围。

```json
{
  "books_available_menu": {
    "name": "books_available_menu",
    "display_name": "在库图书",
    "sequence": 10,
    "active": true,
    "model": "books_manage",
    "view": "books_manage_grid,books_manage_search,books_manage_form",
    "parent_ids": { "@ref": "books_bookmgr_root_menu" },
    "filter": [["status", "=", "IN_STOCK"]]
  },
  "books_borrowed_menu": {
    "name": "books_borrowed_menu",
    "display_name": "借出图书",
    "sequence": 20,
    "active": true,
    "model": "books_manage",
    "view": "books_manage_grid,books_manage_search,books_manage_form",
    "parent_ids": { "@ref": "books_bookmgr_root_menu" },
    "filter": [["status", "=", "BORROWED"]]
  }
}
```

---

## 菜单过滤条件

### 所有接口追加统一过滤

```json
{
  "filter": [["businessType", "in", ["3", "4", "6"]]]
}
```

页面内所有接口请求都会把菜单 `filter` 与接口自身 `filter` 组合。

### 指定接口追加过滤

`config._self.{service}` 用于给该菜单下某类接口追加过滤，例如所有 `lookup`：

```json
{
  "filter": [["businessType", "in", ["3", "4", "6"]]],
  "config": {
    "_self": {
      "lookup": [["enabled", "=", "1"]]
    }
  }
}
```

### 指定字段的指定接口追加过滤

用于表单字段、可编辑表格字段调用 `lookup` 等接口时：

```json
{
  "config": {
    "businessType": {
      "lookup": [["businessType", "in", ["3", "4", "6"]]]
    }
  }
}
```

---

## 菜单类型

`view` 不只可以写后端视图 key，也可以写页面类型：

| 类型 | 说明 |
|---|---|
| 后端视图别名 | 多个后端视图用逗号隔开，常见为 `search,grid,form,tree` |
| `empty` | 空白页面 |
| `iframe` | 内嵌页面，配合 URL |
| `_blank` | 浏览器新标签页/窗口打开 URL |
| `_self` | 当前页面打开 URL |
| `_top` | 顶级上下文打开 URL |
| `_parent` | 父级上下文打开 URL |
| `wujie` | 微应用模式，配合 URL |
| `custom` | 自定义页面，通常需要把 JSON 视图转成字符串填入 |

生成普通 IIDP 后端 Demo 时，优先使用后端视图别名；只有技术方案明确要求 iframe、微应用或空白页时才用其他类型。

---

## 多模块菜单示例（图书管理 App）

```json
{
  "menus": {
    "books_bookmgr_root_menu": {
      "name": "books_bookmgr_root_menu",
      "display_name": "图书管理",
      "sequence": 10,
      "active": true
    },
    "books_manage_menu": {
      "name": "books_manage_menu",
      "display_name": "图书档案",
      "sequence": 10,
      "active": true,
      "model": "books_manage",
      "view": "books_manage_grid,books_manage_search,books_manage_form",
      "parent_ids": { "@ref": "books_bookmgr_root_menu" }
    },
    "books_cate_menu": {
      "name": "books_cate_menu",
      "display_name": "图书分类",
      "sequence": 20,
      "active": true,
      "model": "books_cate",
      "view": "books_cate_tree,books_cate_form,books_cate_grid,books_cate_search",
      "parent_ids": { "@ref": "books_bookmgr_root_menu" }
    },

    "books_readermgr_root_menu": {
      "name": "books_readermgr_root_menu",
      "display_name": "读者管理",
      "sequence": 20,
      "active": true
    },
    "books_reader_menu": {
      "name": "books_reader_menu",
      "display_name": "读者档案",
      "sequence": 10,
      "active": true,
      "model": "books_reader",
      "view": "books_reader_grid,books_reader_search,books_reader_form",
      "parent_ids": { "@ref": "books_readermgr_root_menu" }
    },

    "books_borrowmgr_root_menu": {
      "name": "books_borrowmgr_root_menu",
      "display_name": "借还管理",
      "sequence": 30,
      "active": true
    },
    "books_borrow_menu": {
      "name": "books_borrow_menu",
      "display_name": "借阅记录",
      "sequence": 10,
      "active": true,
      "model": "books_borrow",
      "view": "books_borrow_grid,books_borrow_search,books_borrow_form",
      "parent_ids": { "@ref": "books_borrowmgr_root_menu" }
    }
  }
}
```

---

## 字典种子数据示例

多个种子数据文件逐条追加到 `app.json` 的 `data`：

```json
{
  "data": [
    "data/menus.json",
    "data/yes_no.json",
    "data/book_status.json"
  ]
}
```

字典种子使用 `base_dict_type` + `base_dict_value` 模型格式（见 `seed-data.md` 零节），**不是** `"dicts"` 格式。

`yes_no.json`（正确格式）：

```json
{
  "data": {
    "yes_no": {
      "model": "base_dict_type",
      "properties": { "dictName": "是否", "dictType": "yes_no", "status": 0 }
    },
    "yes_no_yes": {
      "model": "base_dict_value",
      "properties": { "dictLabel": "是", "dictValue": "1", "dictType": "yes_no", "status": 0, "dictSort": 1 }
    },
    "yes_no_no": {
      "model": "base_dict_value",
      "properties": { "dictLabel": "否", "dictValue": "0", "dictType": "yes_no", "status": 0, "dictSort": 2 }
    }
  }
}
```

完整字典写法、普通初始化数据、附件种子、`@ref`、`@eval`、`@fileId/@fileUrl/@filePath` 见同目录 `seed-data.md`。

---

## 常见问题

| 问题 | 原因 |
|---|---|
| 菜单不显示 | `app.json` 的 `data` 数组未登记 `menus.json`，或 jar 未加入 `apps/apps.json` |
| 点菜单空白 | 菜单 `view` 未匹配任何后端视图 key，或视图文件未登记/未打进 jar |
| 菜单 key 冲突 | 不同 app 的菜单 key 重复，统一加 `appPkg` 前缀 |
| 子菜单找不到父菜单 | `parent_ids.@ref` 与父菜单 `name` 不一致 |
| 菜单顺序不对 | 调整同级菜单 `sequence` |
| 复用菜单数据范围不对 | 菜单 `filter` 或 `config._self` 与接口自身 filter 组合不符合预期 |

## 生成检查清单

- 根菜单不写 `model` / `view`，功能菜单必须写。
- 功能菜单 `model` 等于 Java `@Model(name)`。
- 功能菜单 `view` 包含至少一个列表视图 key，通常同时含 search/form。
- 父子关系使用 `parent_ids: { "@ref": "父菜单name" }`（推荐），不使用 `parent_id` 直接字符串。
- 复用后端视图时只改变菜单自身信息和过滤条件，不复制一份重复视图。
- 每个菜单 key、`name`、路径/URL（若有）全局唯一。
