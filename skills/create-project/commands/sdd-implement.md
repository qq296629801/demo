---
description: 【SDD Step 4】执行 tasks.md 中下一个未完成任务。一次只处理一个任务，完成后勾选并停止。
handoffs:
  - label: 继续下一个任务
    command: sdd-implement
    prompt: 继续执行下一个未完成任务
    send: false
  - label: 同步规格与基线
    command: sdd-sync
    prompt: 所有任务已完成，先同步规格并刷新 brownfield 基线
    send: true
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

可传入 `next`（默认，执行下一个未完成任务）、具体任务 ID（如 `T003`）或模组目录路径（如 `specs/modules/student-mgr/`）。

## 前置检查

1. 确定活动规格书目录（按优先级）：
   a. `$ARGUMENTS` 为路径格式 → 使用指定路径；为 `next` 或任务 ID → 从 CLAUDE.md 或用户输入获取目录。
   b. `CLAUDE.md` 存在 `<!-- IIDP-SDD START -->` 标记 → 读取 `当前活动模组` 字段。
   c. 以上均无 → 提示用户：
      > "请输入模组规格书目录路径（如 `specs/modules/student-mgr/`）："
      等待用户输入后继续。
2. 读取 `tasks.md`，找到第一个 `- [ ]` 未勾选任务。
3. 若所有任务已勾选 → 不执行实现；brownfield 场景自动交接 `/sdd-sync`，非 brownfield 场景自动交接 `/sdd-validate`。
4. 确认当前 git 分支不在主干（`main`/`master`/`develop`）；若在主干，提示切换分支后继续。

## 执行步骤

按 `skills/create-project/references/sdd-workflow.md § Step 4` 执行：

1. **识别当前任务**：读取 `tasks.md` 找到第一个 `- [ ]` 任务，显示给用户确认。
2. **路由到对应子 skill**：

   | 任务类型 | 必须经由 | 输入文件 |
   |---|---|---|
   | 后端工程、模型、视图、菜单、服务、数据 | `skills/backend/SKILL.md` Step 1～10 | `backend-spec.md` |
   | 前端规格 → 代码（含工程初始化） | `iidp-frontend`（`skills/frontend/SKILL.md`）→ `iidp-frontend-spec-code` | `frontend-spec.md` |
   | 前端扩展开发 | `iidp-frontend`（`skills/frontend/SKILL.md`）→ `iidp-frontend-extension-dev` | `frontend-spec.md` |
   | 前端标准模板页（§9 标注无需代码） | 勾选"前端无需新增代码" | — |

3. **执行规则**：
   - 修改前读取相邻文件，沿用当前工程风格。
   - 不顺手重构无关模块。
   - 对缺失事实写"待确认"，不补成假事实。
   - 前端任务在写入任何代码前，必须先通过 `iidp-frontend` 的工程门禁；生成或修改组件节点前必须读取 `skills/frontend/references/iidp-frontend-extension-dev/COMPONENT_RULES_COVERAGE.md` 和 `COMPONENT_RULES.md`；写扩展视图/hook/自定义组件前必须读取 `iidp-frontend-extension-dev` 对应协议，校验 `selector`、扩展 `type`、`hook` 结构、`bind_on_`、`ds_config` 和组件注册边界。
   - 前端任务必须读取并执行 `skills/frontend/references/iidp-frontend-codegen-protocol.md`：写入前执行实现前门禁，写入后执行合规扫描；任一失败不得勾选任务完成。

4. **任务完成后**：将 `- [ ]` 改为 `- [x]`，输出完成摘要，**停止**。
5. **输出剩余任务数**，提示下一步：
   - 还有未完成任务 → 继续运行 `/sdd-implement`。
   - 全部完成且为 brownfield 场景（存在 `codebook/baseline-spec/`、`specs/baseline/` 或 `specs/iidp-stack.md`）→ 自动交接 `/sdd-sync`，同步规格、重跑 code-index、刷新 baseline，再由 `/sdd-sync` 自动进入 `/sdd-validate`。
   - 全部完成且非 brownfield 场景 → 自动交接 `/sdd-validate`。

## 完成标志

- 本次执行恰好一个任务，`tasks.md` 已勾选。
- 输出完成任务的文件变更摘要。
- **不批量声明完成**，不自动执行下一个任务。
