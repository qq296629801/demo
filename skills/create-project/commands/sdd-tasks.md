---
description: 【SDD Step 3】生成 tasks.md 任务清单，同步提取 AC→TC 写入 validation.md。
handoffs:
  - label: 生成蓝图 Blueprint（可选）
    command: sdd-blueprint
    prompt: 生成代码蓝图供确认
    send: false
  - label: 开始实现 Implement
    command: sdd-implement
    prompt: 执行 tasks.md 中第一个未完成任务
    send: true
---

# /sdd-tasks

## 用户输入

```text
$ARGUMENTS
```

可传入 `tdd`（启用 TDD 模式，测试先行）或模组目录路径（如 `specs/modules/student-mgr/`）。

## 前置检查

1. 确定活动规格书目录（按优先级）：
   a. `$ARGUMENTS` 不为空且非 `tdd` → 使用指定路径；若为 `tdd` → 从 CLAUDE.md 或用户输入获取目录后启用 TDD 模式。
   b. `CLAUDE.md` 存在 `<!-- IIDP-SDD START -->` 标记 → 读取 `当前活动模组` 字段。
   c. 以上均无 → 提示用户：
      > "请输入模组规格书目录路径（如 `specs/modules/student-mgr/`）："
      等待用户输入后继续。
2. 确认 `plan.md` 已存在且用户已确认计划摘要（Plan Review Gate 已通过）。
2. 确认 git 分支不在主干；若在主干，提示创建 feature 分支后再继续。

## 执行步骤

按 `skills/create-project/references/sdd-workflow.md § Step 3` 执行：

1. **读取** `plan.md`、`backend-spec.md`、`frontend-spec.md`、`requirements.md`。
2. **生成 `tasks.md`**，包含以下任务块：

   | 任务块 | 说明 |
   |---|---|
   | Git·分支准备 | 确认 feature 分支，每个 feature 只执行一次 |
   | 后端·工程基础 | POM、app.json（只做一次） |
   | 后端·模型 N | 每个模型独立任务块，按依赖顺序排列 |
   | 后端·跨模型服务 | 有复杂流程时必写 |
   | 后端·登记收尾 | apps/apps.json |
   | 测试·后端服务 | TDD 模式时移至对应实现块之前 |
   | 前端·实现分支判断 | 读 frontend-spec.md §9 |
   | 前端·工程基础 | 仅全新前端时执行 |
   | 前端·页面 N | 按页面/模型逐个展开 |
   | 前端·收尾 | lint、节点控制台验证 |
   | 测试·前端验收场景 | TC-FE-xx 逐条 |
   | 验证 | 构建、静态检查 |

3. **AC → TC 提取**：从 `requirements.md` 验收标准提取 TC-BE-xx / TC-FE-xx，写入 `validation.md` 测试用例规格节；`tasks.md` 测试任务通过 TC-ID 与之对应。

4. **TDD 模式**（传入 `tdd` 时激活）：将"测试·后端服务"块移至对应实现块之前。

5. **更新 CLAUDE.md**：将阶段更新为 `Step 4 Implement（进行中）`。

## 完成标志

- `tasks.md` 已生成，所有任务格式为 `- [ ] [规模] 描述`。
- `validation.md` 已写入测试用例规格节，TC-ID 与 tasks.md 对应。
- `CLAUDE.md` 标记已更新。
- 下一步：`/sdd-blueprint`（可选）或直接 `/sdd-implement`。
