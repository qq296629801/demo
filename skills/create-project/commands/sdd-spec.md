---
description: 【SDD Step 1.5】生成技术落地规格。按 scope 生成 backend-spec.md / frontend-spec.md / interaction-spec.md。
handoffs:
  - label: 生成计划 Plan
    command: sdd-plan
    prompt: 规格已生成，生成实现计划 plan.md
    send: true
---

# /sdd-spec

## 用户输入

```text
$ARGUMENTS
```

可传入 `backend`、`frontend`、`both`（默认）或功能目录路径。

## 前置检查

1. 确认 `contracts.md` 和 `integration-map.md` 已存在（`/sdd-contracts` 已执行）；否则提示先运行 `/sdd-contracts`。
2. **所有规格内容必须取自 `contracts.md`**，不得自行发明字段注解、服务签名、权限码。

## 执行步骤

按 `skills/create-project/references/sdd-workflow.md § Step 1.5a / 1.5b / 1.5c` 执行：

### Step 1.5a：backend-spec.md（必须生成）

读取 `sdd-backend.md` 模板，生成 `specs/features/<feature>/backend-spec.md`，包含：

| 章节 | 来源 |
|---|---|
| 命名（appName / appPkg / moduleName / model_name） | `contracts.md` |
| 工程文件清单（POM / app.json / 视图 / 菜单 / apps.json） | `app-json.md` |
| 模型设计（字段 / 注解 / 校验 / 索引） | `contracts.md § 模型属性契约` |
| 服务设计（内置 / 自定义，含详细设计块） | `contracts.md § 服务签名契约` |
| 视图和菜单（视图 key / grid / search / form） | `contracts.md § 视图 key / 菜单 key` |
| 数据和权限（种子数据 / 权限码必须取自 integration-map.md） | `integration-map.md § 权限码总览` |
| 验收（app.json 登记 / JSON 可解析 / 编译） | — |

### Step 1.5b：frontend-spec.md（必须生成）

读取 `sdd-frontend.md` 模板，生成 `specs/features/<feature>/frontend-spec.md`；**即使标准模板无需前端代码，也必须生成此文件**，在 §9 标注"前端无需新增代码"及理由。

### Step 1.5c：interaction-spec.md（条件生成）

满足以下任一时生成：复杂状态机（>3 个状态）/ 需响应式策略 / 有可访问性要求 / 含批量/危险操作。

## 完成标志

- `backend-spec.md` 已生成，字段注解和权限码与 `contracts.md` 一致。
- `frontend-spec.md` 已生成，§9 明确标注实现分支。
- `interaction-spec.md`（如触发）已生成，与 `frontend-spec.md` 不重复输出技术细节。
- 下一步：`/sdd-plan`。
