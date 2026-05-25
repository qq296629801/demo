---
description: 【SDD Step 1.3a】契约先行（强制）。生成 integration-map.md 和 contracts.md，必须先于规格书。
handoffs:
  - label: 生成规格 Spec
    command: sdd-spec
    prompt: 生成 backend-spec.md 和 frontend-spec.md
    send: true
  - label: 可选 Critique
    command: sdd-critique
    prompt: 对规格做产品战略 + 工程风险批判
    send: false
---

# /sdd-contracts

## 用户输入

```text
$ARGUMENTS
```

可传入功能目录路径；为空时从 `CLAUDE.md` 读取活动功能目录。

## 前置检查

1. 确认 `requirements.md` 存在且 `## Clarifications` 节已填写（或用户确认跳过 Clarify）。
2. **必须先读取** `skills/backend/references/core/model.md`、`references/core/method-service.md`、`references/core/view.md`、`references/core/menu.md`、`references/core/app-json.md`，不得凭记忆填写注解参数或字段类型。

## 执行步骤

按 `skills/create-project/references/sdd-workflow.md § Step 1.3a` 和 `sdd-constitution.md` 中的模板执行：

### ① 架构级契约 integration-map.md

**模板查找**：先检查 `specs/templates/integration-map.md` 是否存在。
- 存在 → 以该文件为骨架追加本 Phase 内容；
- 不存在 → 使用 `sdd-constitution.md § integration-map.md 模板`。

填写并更新 `specs/integration-map.md`（每个 Phase 追加，不覆盖其他 Phase 内容）：

- 模型清单与 ER 关系（含外键字段名）
- 每个模型的页面/能力 → 视图 key、菜单 key、服务、权限码、前端实现方式
- 跨模型服务（服务名、挂载模型、涉及模型、事务边界、权限码）
- **权限码总览**（唯一定义来源，格式 `{model_name}:{action}`）

### ② API 级契约 contracts.md

**模板查找**：先检查 `specs/templates/contracts.md` 是否存在。
- 存在 → 以该文件为骨架填写本功能契约；
- 不存在 → 使用 `sdd-constitution.md § contracts.md 模板`。

新建 `specs/features/<feature>/contracts.md`，按上一步确定的模板填写：

| 契约项 | 必须先读的 backend skills |
|---|---|
| 模型属性契约（字段/注解/类型） | `model.md`、`model-property-advanced.md` |
| ER 字段契约（外键/related/ManyToOne） | `model.md § ER 关系注解` |
| 服务签名契约（服务名/参数/权限码） | `method-service.md` |
| 视图 key / 菜单 key | `view.md`、`menu.md` |
| app.json 条目 | `app-json.md` |

## 完成标志

- `specs/integration-map.md` 包含本 Phase 模型、权限码总览（新增模型分组）。
- `specs/features/<feature>/contracts.md` 已生成，所有字段注解、服务签名、权限码可从文件中直接查到。
- 输出提示：backend-spec 和 frontend-spec 的所有内容**必须取自本文件**，不得自行发明。
- 下一步：`/sdd-critique`（可选）或直接 `/sdd-spec`。
