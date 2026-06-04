---
description: 【SDD Phase 结束】启动 5 子 Agent 并行深度审查。汇总报告后写回 CLAUDE.md，等用户决定是否进入下一 Phase。
handoffs:
  - label: 开始下一 Phase
    command: sdd-specify
    prompt: 输入下一个功能描述，开始新 Phase
    send: false
---

# /sdd-review

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

## 触发时机

| 时机 | 必选子 Agent | 可选子 Agent |
|---|---|---|
| Phase 结束（tasks.md 全勾选） | 规格一致性、后端对齐 | 安全边界、AI 可操作性 |
| PR 前 | 全部 5 个 | — |
| 规格重大变更 | 规格一致性 | — |

## 执行步骤

按 `skills/create-project/references/sdd-review.md` 执行：

**并行启动以下 5 个子 Agent（独立上下文，不共享中间结果）：**

| Agent | 视角 | 只读范围 |
|---|---|---|
| 1. 规格内部一致性 | 规格文件之间的矛盾与漏洞 | 所有 specs/ 文件 |
| 2. 后端规格对齐 | 对照 backend skills 检查注解/服务/权限 | `backend-spec.md` + `skills/backend/references/core/` |
| 3. 前端规格对齐 | 对照 frontend skills 检查节点/分支/扩展 | `frontend-spec.md` + `skills/frontend/SKILL.md` |
| 4. 安全与权限边界 | 权限旁路、多租户、敏感字段、SQL 注入 | `backend-spec.md`、`contracts.md` |
| 5. AI 可操作性 | 任务可执行性、待确认阻塞、矛盾指令 | `tasks.md`、`validation.md` |

**每个子 Agent 约束**：只读规格，不写文件，不执行命令；发现问题只描述事实（文件路径、问题），不自动修复。

**协调 Agent** 等所有子 Agent 完成后，按 `sdd-review.md § 汇总报告格式` 输出：

```markdown
# 深度审查报告：[Phase N] [Feature Name]

## 总览
| 子 Agent | 高优先级 | 中优先级 | 低优先级 |
|---|---|---|---|
| 规格一致性 | N | N | N |
...

## 需要用户决策的事项
1. [问题] — 来自：[Agent 名称]

## 高优先级发现汇总
[合并去重，按影响排序]
```

**写回 CLAUDE.md**（审查完成后）：

```markdown
<!-- IIDP-SDD START -->
当前活动模组：specs/modules/<moduleName>/
实现计划：specs/modules/<moduleName>/plan.md
当前阶段：Phase 完成 ✅（YYYY-MM-DD）
<!-- IIDP-SDD END -->
```

**高优先级发现处置**：在 `decisions.md` 记录"修复 / 推迟 / 接受风险"后，Phase 才视为正式结束；未处置的高优先级发现须加入 `roadmap.md` 技术债表。

## 完成标志

- 汇总报告已输出，用户决策事项已列出。
- `CLAUDE.md` 标记已更新为 Phase 完成。
- `decisions.md` 已记录高优先级发现处置决策。
- 用户决定继续 → 运行 `/sdd-specify` 输入下一功能描述；结束 → 更新 `CHANGELOG.md`。
