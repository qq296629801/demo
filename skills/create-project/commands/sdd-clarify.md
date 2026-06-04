---
description: 【SDD Step 1.2】Clarify 规格澄清。扫描 requirements.md 的 10 类歧义，生成 ≤5 个问题，答案写回文件。
handoffs:
  - label: 契约先行 Contracts
    command: sdd-contracts
    prompt: 生成 integration-map.md 和 contracts.md
    send: true
  - label: 再做一轮 Clarify
    command: sdd-clarify
    prompt: requirements.md 还有待确认事项，继续澄清
    send: false
---

# /sdd-clarify

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
2. 确认 `requirements.md` 已存在，否则提示先运行 `/sdd-specify`。

## 执行步骤

按 `skills/create-project/references/sdd-workflow.md § Step 1.2` 执行：

1. **加载** `specs/modules/<feature>/requirements.md`。
2. **扫描 10 类歧义维度**（功能范围、领域模型、交互流程、非功能属性、集成依赖、边界条件、约束权衡、术语命名、完成信号、IIDP 特有待确认项）。
3. **生成 ≤5 个问题**，按影响程度排优先级，每题附 2–4 个选项或简答指引。
4. **逐题交互**：一次只问一题，等待用户回答后再问下一题。
   - 建议最优选项并说明理由（1–2 句）。
   - 接受"推荐"/"是"等快捷确认。
5. **答案写回**：每题确认后立即执行两个动作：
   - 在 `## Clarifications / ### Session YYYY-MM-DD` 追加 `- Q: ... → A: ...`。
   - 同步更新受影响章节（字段表、AC、权限描述等）；删除矛盾旧内容。
6. **残留扫描**：全部问题结束后，列出剩余 `待确认` 标记及保留理由。

## 完成标志

- `requirements.md` 已更新 `## Clarifications` 节，本轮问题数 ≤ 5。
- 输出覆盖率摘要（已解决 / 推迟 / 仍有歧义）。
- 如仍有高影响歧义 → 建议再运行 `/sdd-clarify`；否则 → 提示运行 `/sdd-contracts`。
