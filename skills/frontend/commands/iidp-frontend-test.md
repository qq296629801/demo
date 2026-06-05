---
description: 手动验证刚生成的 IIDP 前端代码。执行静态合规检查、工程初始化启动、Playwright 用户故事验证、多轮修复对比、Skill 反馈和评分报告。
handoffs:
  - label: 修复后重新测试
    command: iidp-frontend-test
    prompt: 前端失败项已修复，重新测试刚生成的前端代码
    send: false
---

# /iidp-frontend-test

## 用户输入

```text
$ARGUMENTS
```

支持自然语言调用：

```text
/iidp-frontend-test 帮我测试刚生成的前端代码
```

也支持参数调用：

```text
/iidp-frontend-test --project <前端工程路径> --app <appName> --feature <规格目录> --api <后端API地址>
```

## 触发规则

- 本命令只在用户显式调用 `/iidp-frontend-test` 时执行。
- 不得在 `iidp-frontend` 生成代码后自动触发本命令。
- 如果用户只说“帮我测前端”或类似表达，提示用户改用 `/iidp-frontend-test`，不要自行执行本流程。
- `/sdd-validate frontend` 不自动替代本命令；最多提示用户可手动运行 `/iidp-frontend-test` 做浏览器级验证。

## 执行原则

- 始终按“静态合规检查 → 初始化并启动前端工程 → Playwright 浏览器验证”的顺序执行。
- 不执行 `npm run install:tech`。
- 后端 API 地址未知时必须停下询问，不能猜测、跳过或继续 Playwright。
- 设置 `sessionStorage.tempApi` 后必须刷新页面。
- 登录固定使用 `admin_000001 / Admin_000001`。
- 静态合规失败时立即停止，不继续启动工程或运行 Playwright。
- 启动失败时立即停止，不运行 Playwright，不计算用户故事分数。
- Playwright 失败必须保存截图和 console 摘要。
- 每次完整进入本命令流程算一轮，无论失败发生在哪一段；最多 3 轮。
- 修复后必须重新完整执行三段流程，不得跳过已经通过的阶段。
- 第 3 轮仍失败时停止，输出人工确认项，不再自动重试。
- 不自动修改 Skill，只输出 `skill-feedback.md` 优化建议。

## 输入收集

启动后先解析 `$ARGUMENTS`，按以下顺序收集信息。

### 必填项

| 参数 | 说明 | 未提供时的处理 |
|---|---|---|
| `--project <frontendProject>` | 前端工程路径 | 必须询问用户，不能猜测 |
| `--app <appName>` | 目标应用名 | 必须询问用户，不能猜测 |
| `--api <backendApi>` | 后端 API 地址，用于设置 `sessionStorage.tempApi` | 必须停下询问，不能跳过、不能继续 Playwright |

### 可选项

| 参数 | 说明 | 未提供时的处理 |
|---|---|---|
| `--feature <featureDir>` | 规格目录，包含 `validation.md` 等文件 | 跳过，报告中标注“未提供” |
| 本次变更文件 | 优先用 git 获取 | 按“变更范围识别兜底策略”执行 |

### 测试账号

```text
用户名：admin_000001
密码：Admin_000001
```

## 轮次目录

在前端工程根目录创建或复用：

```text
frontend-validation-runs/
  001/
    diff-before.txt
    diff-after.txt
    static-result.md
    startup-result.md
    playwright-result.md
    screenshots/
    score.json
  002/
  003/
```

轮次编号使用 3 位数字。每轮开始前写入 `diff-before.txt`，结束后写入 `diff-after.txt`。

## 变更范围识别

按以下顺序尝试确定本次变更文件：

1. 在前端工程根目录执行 `git diff --name-only`，获取未提交变更。
2. 如果为空，执行 `git diff HEAD~1 --name-only`，获取最近一次提交的变更。
3. 如果 git 不可用或仍无法识别，扫描以下目录的全部源码文件：

```text
apps/<appName>/views
apps/<appName>/common
apps/<appName>/config
apps/component
```

报告中必须注明变更范围识别方式，并标注是否为兜底全量扫描。

静态检查只扫描本次变更涉及的源码文件，且只在以上源码目录范围内判断前端合规。禁止目录变更不受源码目录限制，发现即失败。

## Playwright 用例来源

从 `--feature <featureDir>` 指定目录按优先级读取。优先级高的文件已经能支撑完整用例时，不继续向下读取；只能补充未覆盖场景，不能覆盖高优先级用例。

| 优先级 | 文件 | 规则 |
|---|---|---|
| Level 1 | `validation.md` | 提取所有 `TC-FE-*` 条目，每条生成一个 Playwright 验收用例，按 Given / When / Then 展开 |
| Level 2 | `interaction-spec.md` | 补充点击路径、弹窗、表单、状态变化、异常提示，只补 Level 1 未覆盖场景 |
| Level 3 | `frontend-spec.md` | 补充页面入口、appName、selector、节点树、事件绑定、数据源，只补 Level 1/2 未覆盖页面结构 |
| Level 4 | `requirements.md` | 仅当 `validation.md` 完全缺失时启用，从用户故事和验收标准生成临时用例 |

### 覆盖度规则

- 完全无法生成任何用例时，停止并询问用户提供用例或规格文件；不得编造页面、按钮、字段或断言；不得继续执行 Playwright。
- 规格文件存在但不完整时，继续执行已有用例；在报告“未覆盖功能点”列出未覆盖功能点；标注覆盖率：`已覆盖 TC 数 / 可识别功能点总数`；不得为未覆盖部分编造断言。
- Level 4 生成的用例必须标注为“临时用例，非正式 TC”。

## 第一段：静态合规检查

先读取并执行：

```text
skills/frontend/references/iidp-frontend-codegen-protocol.md
```

如果存在以下补充文件，按顺序读取，后者补充前者，不覆盖前者：

1. `skills/frontend/references/iidp-frontend-codegen-protocol.md`
2. `skills/frontend/references/iidp-frontend-codegen-examples.md`
3. `skills/frontend/references/iidp-frontend-codegen-constraints.md`

### 必须检查项

| 检查项 | 失败条件 |
|---|---|
| 禁止改动目录 | 本次变更涉及 `node_modules`、`dist`、`distApp`、`distTmp`、`umdComps`、`build` |
| 禁止 HTTP 调用 | 出现 `axios`、`fetch(`、`XMLHttpRequest` 或手写 HTTP 封装 |
| 禁止 `type: "page"` 替换标准页 | 替换现有标准页时使用 `type: "page"` 或 `type: 'page'` |
| 扩展钩子结构 | 扩展钩子缺少 `selector`、`type: "merge"` 或 `hook` |
| hook 方法 return | hook 方法缺少对应 return |
| `vm.super[...]` return | 调用 `vm.super[...]` 的结果没有被 return |
| Vue import 边界 | `apps/<appName>/views/**/*.js` 直接 import `.vue` |
| Vue 组件注册 | 自定义 Vue 组件未在 `apps/<appName>/common/comps.js` 注册 |
| Vue 组件命名 | 组件 `name` 未以 `tech-` 开头 |
| 视图 type 与组件名 | 视图 `type` 与 Vue 组件名去掉 `tech-` 后的 kebab-case 不匹配 |
| IIDP button 文本字段 | IIDP `button` 使用 `text` 表示按钮文本，应使用框架指定字段 |
| 表格操作列按钮 | 表格操作列按钮错误使用普通 `bind_on_click` |
| 禁止新增 `var` | 新增 JS 中出现 `var` |
| selector / 节点 id 来源 | selector 或节点 id 无规格、标准模板、现有代码或用户确认来源 |

### 静态失败处理

任一静态违规失败时：

1. 写入本轮 `static-result.md`，列出失败项、文件路径、行号、证据和主因。
2. 写入本轮 `startup-result.md` 和 `playwright-result.md`，标注“未执行：静态合规失败”。
3. 写入本轮 `score.json`、`diff-after.txt` 和总报告草稿。
4. 停止，不执行初始化、启动或 Playwright。
5. 提示修复代码后重新触发 `/iidp-frontend-test`。

## 第二段：初始化并启动前端工程

进入 `--project` 指定的前端工程根目录后依次执行：

```bash
node -v
npm -v
npm run init:tech
npm run start
```

禁止执行：

```bash
npm run install:tech
```

### 启动成功标准

- `npm run init:tech` 成功完成。
- dev server 输出本地访问地址，或默认端口可访问。
- 无阻塞性编译错误。
- warning 只记录，不判失败。
- 端口冲突时换端口重试一次，记录实际 URL。

### 启动失败分类

| 分类 | 说明 |
|---|---|
| `init-fail` | `npm run init:tech` 执行失败 |
| `startup-fail` | `npm run start` 失败或页面不可访问 |
| `environment-fail` | Node、npm、网络、私服、端口等环境问题 |

启动失败时写入 `startup-result.md`，停止，不执行 Playwright，不计算用户故事分数。

## 第三段：Playwright 浏览器验证

### 浏览器初始化步骤

严格按以下顺序执行：

1. 打开前端页面。
2. 在页面 console 执行：

```js
sessionStorage.setItem("tempApi", "<后端API地址>");
```

3. 设置成功后必须执行 `page.reload()` 刷新页面。
4. 使用固定账号登录：
   - 用户名：`admin_000001`
   - 密码：`Admin_000001`
5. 执行 `TC-FE-*` 用户故事。
6. 每条用例保存截图、console 摘要、断言结果。

### 基础检查项

每轮 Playwright 必须先跑基础检查：

- 页面无白屏。
- console 无阻塞错误。
- `window.Tech` 存在。
- `tech_app` 存在。
- 本次新增按钮、表格列、表单、弹窗、自定义组件可见。
- 用户故事关键路径可完成。

### 截图命名

```text
frontend-validation-runs/<轮次编号>/screenshots/TC-FE-001-pass.png
frontend-validation-runs/<轮次编号>/screenshots/TC-FE-002-fail.png
frontend-validation-runs/<轮次编号>/screenshots/baseline-loaded.png
```

## 多轮修复与变更对比

每轮结束后生成本轮对比报告，写入对应结果文件，并汇总到 `frontend-validation-report.md`。

| 维度 | 说明 |
|---|---|
| 修改文件变化 | 本轮新增 / 删除 / 变更的文件列表 |
| 静态违规数变化 | 上轮 N 条 → 本轮 M 条，差值 ± |
| 启动状态变化 | 失败 → 成功 / 成功 → 失败 / 无变化 |
| Playwright 通过数变化 | 上轮 X/Y → 本轮 X'/Y'，差值 ± |
| 总分变化 | 上轮分数 → 本轮分数 |
| 是否引入新问题 | 是 / 否；如是，列出新增失败项 |
| 修复有效性 | 每个修复项标注：有效 / 无效 / 引入新问题 |

第 3 轮仍失败时，在总报告中输出人工确认项，不再自动重试。

## Skill 反向优化闭环

验证结束后生成：

```text
skill-feedback.md
```

### 归因分类

每个失败项必须标注主因（单选）和次因（可选，多选）。

| 分类标签 | 含义 |
|---|---|
| `frontend-skill-gap` | Skill 规则缺失或表述模糊，导致生成代码不符合预期 |
| `spec-gap` | 规格文档不完整，Skill 无法从中获取足够信息 |
| `environment-gap` | 本地环境、网络、依赖版本等问题，与 Skill 无关 |
| `implementation-bug` | 规则清晰，但生成代码未正确执行，属于模型执行偏差 |

### 多归因判断

1. 优先判断是否为 `environment-gap`：环境问题与 Skill 无关，不写回。
2. 其次判断是否为 `implementation-bug`：规则存在但未被执行，标注后观察是否多次复现。
3. 如果规则本身不存在或有歧义，归为 `frontend-skill-gap`。
4. 如果规格文档缺失导致生成方向偏差，归为 `spec-gap`。
5. 单次偶发现象只记录，不沉淀为硬规则；同一失败模式在 2 次及以上出现时才建议写回。

### 建议写回格式

每条 Skill 优化建议必须按以下格式输出：

```markdown
## Skill 优化建议 #N

- **失败模式**：<简述失败现象>
- **证据**：<代码位置 / 日志行 / 截图路径 / diff 片段>
- **归因**：主因 `frontend-skill-gap`，次因 `implementation-bug`（如适用）
- **建议写回文件**：`skills/frontend/references/iidp-frontend-codegen-protocol.md`
- **建议写回章节**：<章节名，例如“扩展钩子规则”>
- **建议新增内容**：
  > <具体的规则文本或示例代码>
- **验证方式**：<下次生成后如何验证该规则已生效>
```

### 约束

- 不自动修改 Skill，只输出建议。
- `environment-gap` 类问题不写回 Skill。
- 单次偶发现象只记录，不建议写回。
- `spec-gap` 类问题建议同步反馈给规格文档维护方，不写入 Skill。

## 评分规则

总分 100 分。

| 维度 | 分值 | 说明 |
|---|---:|---|
| 静态合规 | 40 | 每个违规项扣分，全部通过得满分 |
| 初始化与启动 | 10 | `init:tech` 成功 5 分，`start` 成功 5 分 |
| Playwright 用户故事 | 35 | 通过率 × 35；基础检查失败按用例失败或阻塞记录 |
| 多轮变更对比 | 10 | 每轮 diff 和对比 4 分，修复有效 3 分，未引入新问题 3 分 |
| Skill 反向优化建议 | 5 | 产出有效 `skill-feedback.md` 且归因准确得满分 |

### 通过标准

| 分数 | 结论 |
|---|---|
| `>= 85` | 通过 |
| `70 - 84` | 部分通过，需修复后重测 |
| `< 70` | 失败 |
| 静态合规失败 | 无论总分多少，判定失败 |

## 输出文件

最终输出以下文件到前端工程根目录：

```text
frontend-validation-report.md
frontend-validation-score.json
frontend-validation-runs/
  001/
  002/
  003/
skill-feedback.md
```

### `frontend-validation-report.md` 必须包含

- 触发方式：手动命令 `/iidp-frontend-test`。
- 工程路径、`appName`、验证时间。
- 后端 API 地址是否已设置，`tempApi` 设置后是否已刷新页面。
- 登录账号。
- 变更范围识别方式：git diff / 最近提交 / 全量扫描。
- 本次变更文件列表。
- Playwright 用例来源：Level 1/2/3/4 及覆盖率。
- 未覆盖功能点列表（如有）。
- 静态检查结果：违规项和代码位置。
- `init:tech` / `start` 结果。
- 用户故事执行结果：每条 TC 的 pass/fail 和截图路径。
- console 错误摘要。
- 多轮变更对比表。
- 评分表。
- 失败分类汇总。
- Skill 反向优化建议摘要，完整内容见 `skill-feedback.md`。
- 人工确认项：第 3 轮仍失败时必须包含。

### `frontend-validation-score.json` 结构

```json
{
  "runAt": "2026-06-05T00:00:00Z",
  "appName": "xxx",
  "totalRounds": 2,
  "finalScore": 88,
  "passed": true,
  "dimensions": {
    "staticCompliance": { "score": 40, "maxScore": 40, "violations": 0 },
    "startup": { "score": 10, "maxScore": 10, "initTech": true, "start": true },
    "playwrightStories": { "score": 31, "maxScore": 35, "passed": 13, "total": 15 },
    "changeComparison": { "score": 9, "maxScore": 10, "newIssuesIntroduced": false },
    "skillFeedback": { "score": 5, "maxScore": 5, "feedbackGenerated": true }
  },
  "rounds": [
    { "round": 1, "score": 72, "staticViolations": 3, "playwrightPassed": 10 },
    { "round": 2, "score": 88, "staticViolations": 0, "playwrightPassed": 13 }
  ]
}
```

## 完成标志

- 已按顺序完成静态、启动和 Playwright 阶段，或在静态/启动/用例不可生成处按规则停止。
- 已生成本轮 `frontend-validation-runs/<轮次编号>/` 详情文件。
- 已生成或更新 `frontend-validation-report.md`、`frontend-validation-score.json` 和 `skill-feedback.md`。
- 最终响应只输出结论摘要、报告路径、最终分数、是否通过、阻塞项和下一步修复建议。
