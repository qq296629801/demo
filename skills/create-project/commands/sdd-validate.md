---
description: 【SDD Step 5】运行验收检查。按 validation-checklist 检查交付物，输出通过/失败/无法验证。
handoffs:
  - label: 深度审查 Review
    command: sdd-review
    prompt: 验收通过，启动 Phase 结束深度审查
    send: true
  - label: 修复后重新验证
    command: sdd-validate
    prompt: 失败项已修复，重新运行验收
    send: false
---

# /sdd-validate

## 用户输入

```text
$ARGUMENTS
```

可传入 `backend`、`frontend`、`both`（默认）或功能目录路径。

## 前置检查

1. 确认 `tasks.md` 所有 `- [ ]` 已勾选为 `- [x]`；若有未完成任务，提示先运行 `/sdd-implement`。
2. 从 `CLAUDE.md` 读取活动功能目录。

## 执行步骤

按 `skills/create-project/references/sdd-validation.md` 详细清单执行：

### 后端验收（按 validation-checklist.md）

- 文件登记：所有模型的 `view/data` 路径在 `app.json` 中已登记。
- 命名一致：`model_name`、视图 key、菜单 key、Java 类名在各文件中拼写一致。
- JSON 可解析：所有 `views/*.json`、`data/*.json` 无语法错误。
- Maven 编译：运行 `mvn -s settings.xml -DskipTests clean package` 或说明无法运行的原因。
- 服务校验：每个 `@MethodService` 有状态校验、权限校验、必填入参校验。
- `apps/apps.json` 已登记新 jar。

### 前端验收

- 实现分支判断：每个页面的 `frontend-spec.md §9` 已明确标注。
- 标准模板页：确认无多余前端代码。
- 扩展视图：`selector` 命中方式明确，`ds_config` 完整。
- lint / build：运行 `npm run lint` 或说明无法运行的原因。
- 节点验证：浏览器控制台 `tech_app.printObj(节点id)` 可验证（手工步骤标注）。

### 测试覆盖率更新

读取 `validation.md` 测试用例规格节，将已执行的 TC-ID 状态更新为 `✅ 通过` / `❌ 失败` / `⏸ 阻塞`。

### 输出格式

```markdown
## 验收报告：[功能名称]

| 检查项 | 状态 | 备注 |
|---|---|---|
| app.json 登记 | ✅ 通过 | — |
| JSON 可解析 | ✅ 通过 | — |
| Maven 编译 | ❌ 失败 | 缺少 XXX 依赖 |
| TC-BE-01 | ✅ 通过 | — |
| TC-FE-01 | ⏸ 阻塞 | 需要运行时环境 |
```

## 完成标志

- 验收报告已输出，每项标注状态。
- `validation.md` 测试覆盖率追踪表已更新。
- **无失败项** → 提示运行 `/sdd-review` 启动 Phase 结束深度审查。
- **有失败项** → 按失败分类（规格问题 / 实现问题 / 环境问题 / 平台限制）给出最小修复方案，提示修复后再运行 `/sdd-validate`。
