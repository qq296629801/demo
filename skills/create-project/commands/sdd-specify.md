---
description: 【SDD Step 0+1】能力识别 + 生成 requirements.md。输入功能描述，输出结构化需求规格。
handoffs:
  - label: 规格澄清 Clarify
    command: sdd-clarify
    prompt: 对刚生成的 requirements.md 做 Clarify 扫描
    send: true
---

# /sdd-specify

## 用户输入

```text
$ARGUMENTS
```

用户输入不为空时，将其作为功能描述直接使用；为空时先向用户询问功能描述。

## 前置检查

读取 `CLAUDE.md` 的 `<!-- IIDP-SDD START -->` 标记：
- 存在 → 确认活动功能目录，询问用户是覆盖现有规格还是新建功能。
- 不存在 → 全新功能，继续下一步。

## 执行步骤

按 `skills/create-project/references/sdd-workflow.md § Step 0 + Step 1` 执行：

1. **读取** `skills/create-project/SKILL.md` 和 `references/sdd-workflow.md`。
2. **能力识别（Step 0）**：将用户输入映射到 IIDP 后端能力域和前端实现分支；填写 `## 能力识别` 模板表格。
3. **生成功能目录**：命名格式 `specs/features/<phaseN>-<short-name>/`，创建目录。
4. **编写 requirements.md（Step 1）**：回答"用户要完成什么 / 后端要提供什么 / 前端实现分支意向 / 契约是什么 / 如何验收"五个问题；使用 `待确认` 标记未知事项，不编造平台事实。
5. **保存** `specs/features/<phaseN>-<short-name>/requirements.md`。

## 完成标志

- `requirements.md` 已保存，包含 `## 能力识别` 节和五个 Step 1 问题的答案。
- `待确认` 标记数量已列出（建议 ≤5 项进入 Clarify）。
- 输出下一步提示：`/sdd-clarify` 开始规格澄清。
