---
description: 【SDD Step 2】生成实现计划 plan.md，展示计划摘要等用户确认，完成后写回 CLAUDE.md 标记。
handoffs:
  - label: 生成任务 Tasks
    command: sdd-tasks
    prompt: 计划已确认，生成 tasks.md 任务清单
    send: true
---

# /sdd-plan

## 用户输入

```text
$ARGUMENTS
```

可传入模组目录路径（如 `specs/modules/student-mgr/`）；为空时从 `CLAUDE.md` 读取活动模组目录。

## 前置检查

1. 确定活动规格书目录（按优先级）：
   a. `$ARGUMENTS` 不为空 → 使用指定路径。
   b. `CLAUDE.md` 存在 `<!-- IIDP-SDD START -->` 标记 → 读取 `当前活动模组` 字段。
   c. 以上均无 → 提示用户：
      > "请输入模组规格书目录路径（如 `specs/modules/student-mgr/`）："
      等待用户输入后继续。
2. 确认 `backend-spec.md` 和 `frontend-spec.md` 均已存在；否则提示先运行 `/sdd-spec`。
2. 读取 `contracts.md` 确认无 `待确认` 阻塞项。

## 执行步骤

按 `skills/create-project/references/sdd-workflow.md § Step 2` 执行：

1. **读取** `requirements.md`、`backend-spec.md`、`frontend-spec.md`、`contracts.md`。
2. **生成 `plan.md`**，包含：
   - 方案概述（2–3 句说明为何采用这些 IIDP 能力）
   - 后端改动（Maven 模块 / app.json / Java 模型 / 服务 / 视图 JSON / 菜单与种子数据 / apps/apps.json）
   - 前端改动（实现分支 / 目标工程 / 目标应用 / 目标页面或节点 id / 目录）
   - 风险与待确认事项

3. **Plan Review Gate（必须暂停）**：展示计划摘要，等待用户明确确认：

   ```markdown
   ## 计划摘要确认：[功能名称]

   **后端改动量**：[N] 个模型 / [N] 个服务 / [N] 个视图文件
   **前端实现分支**：[...]（[N] 个页面需前端代码，[N] 个无需）
   **风险与待确认**：[列表]
   **估计复杂度**：S / M / L

   计划已就绪。**确认后生成任务清单**；如需调整，请说明修改点。
   ```

4. **用户确认后**，写回 `CLAUDE.md`（`<!-- IIDP-SDD START/END -->` 标记）：

   ```markdown
   <!-- IIDP-SDD START -->
   当前活动模组：specs/modules/<moduleName>/
   实现计划：specs/modules/<moduleName>/plan.md
   当前阶段：Step 3 Tasks（待生成）
   <!-- IIDP-SDD END -->
   ```

## 完成标志

- `plan.md` 已保存。
- 用户已确认计划摘要。
- `CLAUDE.md` 已更新（标记区间写入当前功能路径和阶段）。
- 下一步：`/sdd-tasks`。
