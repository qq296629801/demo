---
description: 【evolve eval runner】从 code-index 规格生成可评分的 IIDP SDD 产物、应用骨架、冒烟用例和评分输入。仅用于基准实验或 eval-only。
---

# /sdd-eval-runner

## 用户输入

```text
$ARGUMENTS
```

推荐参数：

```text
--run-dir .evolve/runs/<timestamp>
--source-spec .evolve/runs/<timestamp>/spec/<name>.md
--source-root .evolve/runs/<timestamp>/source
--mode eval-only
--scope backend-only
```

## 使用边界

- 本命令只服务 `evolve` 基准实验、样本池评测或 `--eval-only`。
- 不修改 `skills/evolve/`、基准源码、生成后的测试结果或 Docker 基础设施。
- 只能把产物写入 `--run-dir` 下的 `generated-app/`、`smoke/`、`logs/`。
- 发现 IIDP 私有规则缺失时写 `knowledge-gap`，不得补成稳定规则。
- 无法运行 Docker、Maven、Node、数据库或 JSON-RPC 时写 `environment-gap`，不得产出误导性总分。

## 前置检查

1. `--run-dir` 必须存在，且包含 `spec/`、`source/` 或显式传入对应路径。
2. `--source-spec` 必须可读，且至少包含用户故事、API、领域模型、业务规则、验收标准中的 3 类。
3. 读取：
   - `skills/create-project/SKILL.md`
   - `skills/create-project/references/sdd.md`
   - `skills/create-project/references/sdd-validation.md`
   - 后端产物需要 `skills/backend/SKILL.md`
   - 前端产物需要 `skills/frontend/SKILL.md`
4. 若本地没有 IIDP 父工程或标准 demo 工程，按 `skills/backend/greenfield/SKILL.md` 判断是否可创建；不能创建时记录 `environment-gap`。

## 非交互门控

基准实验必须可复现。遇到普通 SDD workflow 的人工 gate 时按下表处理：

| Gate | eval runner 默认行为 | 记录要求 |
|---|---|---|
| Clarify | 不向用户追问，保留 `待确认` | 在 `requirements.md ## Clarifications` 写明 eval 模式跳过 |
| Critique | 跳过 | 在 `logs/sdd-eval-runner.md` 记录 |
| Plan Review | 自动 approve | 记录 plan 摘要路径 |
| Blueprint | 默认 skip | 若生成蓝图，记录原因 |
| Validate 修复 | 不自动修复生成应用 | 失败归类后停止 |
| Phase End | done | 写 manifest 和评分输入 |

## 执行步骤

### 1. 初始化输出目录

在 `--run-dir` 下创建：

```text
generated-app/
  specs/features/evolve-baseline/
smoke/
logs/
```

若目录已存在，先读取其中的 `evolve-manifest.json`；除非用户明确要求重跑，不覆盖已有评分结果。

### 2. 规格转换

把 `--source-spec` 转成以下 SDD 产物：

```text
generated-app/specs/features/evolve-baseline/
  requirements.md
  contracts.md
  backend-spec.md
  frontend-spec.md
  plan.md
  tasks.md
  validation.md
```

生成要求：

- `requirements.md` 必须保留 `US-*`、`FR-*`、`AC-*`。
- `contracts.md` 必须列出模型、服务、字段、权限码、视图 key、菜单 key。
- `backend-spec.md` 必须写明 `appName`、`appPkg`、`moduleName`、`model_name`、`@Model`、`@Property`、`@MethodService`、`views/*.json`、菜单和数据登记。
- `frontend-spec.md` §9 必须明确实现分支；标准模板能满足时写“无需新增前端代码”。
- `validation.md` 必须把 AC 转成 `TC-BE-*` / `TC-FE-*`，并保留追溯关系。

### 3. 生成 IIDP 应用产物

按 `tasks.md` 顺序执行实现任务，产物只写入 `generated-app/`。后端最小可评分产物包括：

```text
generated-app/
  apps/apps.json
  <module>/src/main/java/**/*
  <module>/src/main/resources/**/app.json
  <module>/src/main/resources/**/views/*.json
  <module>/src/main/resources/**/data/*.json
  <module>/src/test/java/**/*
  <module>/src/test/resources/**/*Test.json
```

如果需要前端代码，产物写入：

```text
generated-app/frontend/
  package.json
  apps/<appName>/
```

标准模板或在线视图满足需求时，不生成前端工程；在 `frontend-spec.md` 和 manifest 中写明跳过原因。

### 4. 生成冒烟用例

从 `validation.md` 生成：

```text
smoke/cases.json
smoke/runner-notes.md
```

`cases.json` 的每个用例必须包含：

- `storyId`
- `caseId`
- `trace.us` / `trace.fr` / `trace.ac` / `trace.tc`
- JSON-RPC 2.0 request
- expected HTTP status
- expected result 或 expected error

### 5. 验证与评分输入

执行 `skills/create-project/references/sdd-validation.md` 和 `skills/evolve/references/smoke-validation.md` 中的检查。输出：

```text
logs/sdd-eval-runner.md
logs/iidp-compliance.md
logs/docker-config-check.md
logs/build.log
logs/startup.log
smoke/results.json
generated-app/evolve-manifest.json
```

`evolve-manifest.json` 必须包含：

```json
{
  "schemaVersion": 1,
  "mode": "eval-only",
  "sourceSpec": "<path>",
  "sourceRoot": "<path>",
  "featureDir": "generated-app/specs/features/evolve-baseline",
  "generatedApp": "generated-app",
  "smokeCases": "smoke/cases.json",
  "smokeResults": "smoke/results.json",
  "status": "pass | fail | blocked",
  "failureClass": "none | route-gap | sdd-template-gap | backend-doc-gap | frontend-doc-gap | knowledge-gap | environment-gap",
  "scoreInputReady": true
}
```

当 `scoreInputReady=false` 时必须写明缺失文件和失败分类。

## 输出报告

最后输出一段 Markdown 报告，格式如下：

```markdown
## evolve eval runner 报告

| 项 | 结果 |
|---|---|
| sourceSpec | ... |
| generatedApp | ... |
| smokeCases | ... |
| scoreInputReady | true/false |
| failureClass | ... |

### 失败或阻塞

- ...

### 下一步

- scoreInputReady=true：交给 evolve rubric 打分。
- environment-gap：补环境后重跑，不修改 backend/frontend 规则。
- knowledge-gap：补私有规则证据后再决定是否写回技能。
```

## 完成标志

- `generated-app/evolve-manifest.json` 已生成。
- `smoke/cases.json` 已生成，或在 manifest 中说明无法生成的原因。
- 所有失败都归类为 rubric 和 smoke-validation 支持的失败类型。
- 未修改基准源码、`skills/evolve/` 或评测结果来强行提分。
