# IIDP SDD 深度审查

## 概述

深度审查（Deep Review）在每个 Phase 结束、向主干发起 PR 之前触发。审查由协调 Agent 并行召唤多个专项子 Agent，每个子 Agent 只输出发现报告，**不自动修改规格或代码**；协调 Agent 汇总后交用户决策。

该模式对应 spec-kit 中的 Red Team（多视角并行识别风险）+ Review Extension（实现后专项检查）。

---

## 触发时机

| 时机 | 触发条件 | 必选 | 可选 |
|---|---|---|---|
| Phase 结束 | 任意 Phase 的 `tasks.md` 全部勾选 ✅ | 规格一致性、后端对齐 | 安全边界、AI 可操作性 |
| PR 前 | 发起向主干的合并请求前 | 全部 5 个子 Agent | — |
| 规格重大变更 | `requirements.md` 或 `integration-map.md` 大幅修改 | 规格一致性、契约对齐 | — |

---

## 标准子 Agent 阵列

协调 Agent 一次性启动以下子 Agent（**并行，不串行**），等全部完成后合并报告：

### Agent 1：规格内部一致性（Spec Consistency）

> **视角**：找规格文件之间的矛盾与漏洞

检查范围：

- `requirements.md` 中的业务需求与 `integration-map.md` 中的视图 key、菜单 key、权限码是否对齐
- `tasks.md` 覆盖所有模型（每个模型都有后端实现任务 + 前端实现分支判断）
- 模型清单中的每个 model_name 在视图 JSON、菜单 JSON、Java 类和 `app.json.view` 中是否拼写一致
- 有多模型 ER 关系时，父子模型的任务顺序是否符合依赖序（父先于子）
- 待确认事项是否被遗漏、未处理或被错误地填上了编造值

输出格式：

```markdown
## Agent 1 发现：规格一致性

### 高优先级（可能导致构建失败或功能缺失）
- [位置] [问题描述]

### 中优先级（可能导致验收失败）
- [位置] [问题描述]

### 低优先级（改善文档质量）
- [位置] [问题描述]
```

---

### Agent 2：后端规格对齐（Backend Alignment）

> **视角**：对照 `skills/backend/SKILL.md` 和 `skills/backend/references/core/` 检查后端规格

检查范围：

- 每个 `@MethodService` 是否声明了：状态校验、权限校验、作用域校验、必填参数校验
- 状态变更服务是否使用 IIDP 请求级事务（`ModelException` 自动回滚），而非 Spring `@Transactional`
- 参数错误是否抛 `ValidationException`（不触发回滚），业务流程失败是否抛 `ModelException`（触发回滚）
- 自定义查询服务是否支持平台 Filter、分页（`limit`/`offset`）、排序、字段选择
- `app.json` 是否登记所有模型的 `view` 和 `data` 路径
- `apps/apps.json` 是否登记新增 jar
- 权限码在后端服务 `auth` + 视图按钮 `auth` + `integration-map.md` 三处是否完全对齐

输出格式：同上（高/中/低三档）

---

### Agent 3：前端规格对齐（Frontend Alignment）

> **视角**：对照 `skills/frontend/SKILL.md` 检查前端规格

检查范围：

- 对每个页面/模型是否做了前端实现分支判断（标准模板 / hook / 扩展视图 / Vue2 组件）
- 标准模板可完成的页面是否已明确"前端无需新增代码"
- 需要前端介入的页面：节点 id 是否有来源（用户提供、标准模板规则库或浏览器控制台验证），不得按文案猜测
- hook 是否 return 数据；`before*`/`after*`/`can*` hook 返回类型是否正确
- 扩展视图：`selector` 命中方式是否明确，`ds_config` 的 `type/name/autoRequest/options` 是否完整
- 完全替换标准模板页面是否使用 `replace`（不得使用 `type: 'page'`）
- `effectPaths.includeRegExp` 是否无 `/iidp/` 前缀

输出格式：同上（高/中/低三档）

---

### Agent 4：安全与权限边界（Security Boundaries）

> **视角**：找安全漏洞、权限旁路和敏感数据暴露风险

检查范围：

- 写服务是否存在仅靠前端按钮禁用控制权限、后端服务未做二次校验的情况
- 多租户场景：服务是否有租户范围检查（`org`/`tenant` 过滤），防止跨租户数据泄露
- 敏感字段（密码、密钥、个人身份信息）是否在视图 `properties` 列表或导出接口中未被过滤
- 自定义查询服务是否拼接动态 SQL（应改用 IIDP Filter 或 `@SelectSql` 参数化查询）
- 批量操作（删除/状态变更）是否仅校验传入 id 的所有权，防止越权操作他人记录
- 文件上传接口是否限制文件类型和大小（应委托 MinIO 前置规则）

输出格式：同上（高/中/低三档）

---

### Agent 5：AI 可操作性（AI Operability）

> **视角**：规格对 AI 执行任务的可操作性评估

检查范围：

- 每个 `tasks.md` 任务是否有明确的完成判断标准（能客观验证"完成"与否）
- 是否存在"待确认"事项堆积、且无决策路径的任务（AI 执行到此会卡住）
- `validation.md` 的验收项是否可用静态检查或构建命令自动验证，还是全靠人工
- 规格是否出现矛盾指令（如"标准模板即可"与"必须写扩展视图"同时存在于同一页面）
- 多模型任务中，任务块的顺序是否会导致 AI 在父模型完成前尝试创建子模型
- 所有 `model_name`、`app.json` 路径、Java 包名是否可以从规格文件中无歧义地推导

输出格式：同上（高/中/低三档）

---

## 汇总报告格式

协调 Agent 收到所有子 Agent 报告后，生成结构化汇总：

```markdown
# 深度审查报告：[Phase N] [Feature Name]

审查时间：[日期]
审查范围：[规格目录]

## 总览

| 子 Agent | 高优先级 | 中优先级 | 低优先级 |
|---|---|---|---|
| 规格一致性 | N | N | N |
| 后端对齐 | N | N | N |
| 前端对齐 | N | N | N |
| 安全边界 | N | N | N |
| AI 可操作性 | N | N | N |

## 需要用户决策的事项

> 以下问题存在规格歧义或涉及业务决策，AI 无法自行解决，请逐条确认。

1. [问题描述] — 来自：[Agent 名称]
2. ...

## 高优先级发现汇总

[各 Agent 高优先级发现合并，去重，按影响范围排序]

## 中/低优先级发现

[可选，仅列标题，供用户按需展开]

## 下一步建议

- 修复高优先级发现后，重新触发 [Agent N] 复查。
- 以下低优先级问题建议推迟到 Phase N+1 处理：[列表]
```

---

## 调用约定

### 协调 Agent 启动 Prompt 模板

```text
请对 [specs/modules/{moduleName}/] 目录下的规格文件执行深度审查。

并行启动以下 5 个子 Agent（使用 Agent 工具，独立上下文，不共享中间结果）：
1. 规格内部一致性 Agent — 只读规格文件，按 sdd-review.md Agent 1 检查范围输出发现报告，不修改任何文件。
2. 后端规格对齐 Agent — 读 skills/backend/references/core/ 和当前规格，按 Agent 2 检查范围输出，不修改。
3. 前端规格对齐 Agent — 读 skills/frontend/SKILL.md 和当前规格，按 Agent 3 检查范围输出，不修改。
4. 安全与权限边界 Agent — 只读规格，按 Agent 4 检查范围输出，不修改。
5. AI 可操作性 Agent — 只读规格，按 Agent 5 检查范围输出，不修改。

全部完成后，按 sdd-review.md 汇总报告格式合并结果，列出需要用户决策的事项。
```

### 子 Agent 约束

- 每个子 Agent **只读规格文件，不写文件，不执行命令**。
- 发现问题时只描述事实（文件路径、行范围、问题），不提 PR 也不改代码。
- 输出中的"建议修复方式"只给思路，最终执行须经用户确认。

---

## 与阶段复盘的关系

深度审查产出的高优先级发现，须在 `decisions.md` 中记录处置决策（修复 / 推迟 / 接受风险）后，才视为本 Phase 正式结束；未处置的高优先级发现须加入 `roadmap.md` 的技术债表。
