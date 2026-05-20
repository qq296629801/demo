---
description: 【SDD Step 1.4】Critique 规格批判（可选）。产品战略 + 工程风险双视角审查，输出发现报告，暂停等用户决策。
handoffs:
  - label: 生成规格 Spec
    command: sdd-spec
    prompt: 规格已批判确认，生成 backend-spec.md 和 frontend-spec.md
    send: true
  - label: 修改后再 Critique
    command: sdd-critique
    prompt: requirements.md 已修改，重新批判
    send: false
---

# /sdd-critique

## 用户输入

```text
$ARGUMENTS
```

可传入功能目录路径；为空时从 `CLAUDE.md` 读取活动功能目录。

## 触发条件

满足以下任一时建议触发（用户也可主动调用）：
- 规格涉及 3 个以上模型
- 包含状态机
- 有明显歧义的成功标准
- 待确认事项超过 3 项

## 执行步骤

按 `skills/create-project/references/sdd-workflow.md § Step 1.4` 执行：

**只读 `requirements.md`，不修改规格，只输出发现报告。**

### 产品战略视角

- 功能解决的是真实痛点还是低频诉求？
- 功能边界是否清晰，是否存在 scope creep 风险？
- 成功标准是否可量化验收？
- 是否有更轻量的实现方式（如后端在线视图已满足）？
- 多模型设计是否必要？

### 工程风险视角

- 是否有 `待确认` 事项会在 Implement 阶段造成阻塞？
- ER 关系、状态机、跨模型服务的事务边界是否足够清晰？
- 权限码、视图 key、菜单 key 是否已稳定？
- 前端实现分支判断是否会因节点 id 未确认导致返工？
- 是否有遗漏的验收场景（异常态、权限边界、并发、多租户）？

### 输出格式

```markdown
## Critique 报告：[功能名称]

### 产品战略发现
- [高/中/低] [发现描述]

### 工程风险发现
- [高/中/低] [发现描述]

### 需要用户决策的事项
1. [问题] — 选项 A：[...] / 选项 B：[...]
```

## 完成标志

- 报告已输出，**暂停**等待用户决策。
- 用户选择"修改规格" → 修改 `requirements.md` 后再运行 `/sdd-critique`。
- 用户选择"确认通过" → 运行 `/sdd-spec` 生成技术规格。
