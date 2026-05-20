---
description: 【SDD Step 4】执行 tasks.md 中下一个未完成任务。一次只处理一个任务，完成后勾选并停止。
handoffs:
  - label: 继续下一个任务
    command: sdd-implement
    prompt: 继续执行下一个未完成任务
    send: false
  - label: 运行验收 Validate
    command: sdd-validate
    prompt: 所有任务已完成，开始验收
    send: true
---

# /sdd-implement

## 用户输入

```text
$ARGUMENTS
```

可传入 `next`（默认，执行下一个未完成任务）、具体任务 ID（如 `T003`）或功能目录路径。

## 前置检查

1. 从 `CLAUDE.md` 标记读取活动功能目录。
2. 读取 `tasks.md`，找到第一个 `- [ ]` 未勾选任务。
3. 若所有任务已勾选 → 提示运行 `/sdd-validate`，不执行实现。
4. 确认当前 git 分支不在主干（`main`/`master`/`develop`）；若在主干，提示切换分支后继续。

## 执行步骤

按 `skills/create-project/references/sdd-workflow.md § Step 4` 执行：

1. **识别当前任务**：读取 `tasks.md` 找到第一个 `- [ ]` 任务，显示给用户确认。
2. **路由到对应子 skill**：

   | 任务类型 | 必须经由 | 输入文件 |
   |---|---|---|
   | 后端工程、模型、视图、菜单、服务、数据 | `skills/backend/SKILL.md` Step 1～10 | `backend-spec.md` |
   | 前端规格 → 代码（含工程初始化） | `skills/frontend/SKILL.md` → `iidp-frontend-spec-code` | `frontend-spec.md` |
   | 前端扩展开发 | `skills/frontend/SKILL.md` → `iidp-frontend-extension-dev` | `frontend-spec.md` |
   | 前端标准模板页（§9 标注无需代码） | 勾选"前端无需新增代码" | — |

3. **执行规则**：
   - 修改前读取相邻文件，沿用当前工程风格。
   - 不顺手重构无关模块。
   - 对缺失事实写"待确认"，不补成假事实。

4. **任务完成后**：将 `- [ ]` 改为 `- [x]`，输出完成摘要，**停止**。
5. **输出剩余任务数**，提示下一步：
   - 还有未完成任务 → 继续运行 `/sdd-implement`。
   - 全部完成 → 运行 `/sdd-validate`。

## 完成标志

- 本次执行恰好一个任务，`tasks.md` 已勾选。
- 输出完成任务的文件变更摘要。
- **不批量声明完成**，不自动执行下一个任务。
