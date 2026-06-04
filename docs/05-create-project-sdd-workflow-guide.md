# SpecKit 规格分支、Plan 执行与公共规格维护说明

> 本文已按当前 `create-project` skill 的 IIDP SDD 流程调整。这里仍保留 "SpecKit" 这个标题，是为了说明它和通用 Spec Kit 命令习惯的对应关系；实际执行时以 `skills/create-project/SKILL.md`、`references/sdd.md`、`references/sdd-workflow.md` 和 `references/sdd-validation.md` 为准。

## 目录

1. [核心结论](#1-核心结论)
2. [当前 create-project 的规格目录关系](#2-当前-create-project-的规格目录关系)
3. [新增功能与修改已有功能](#3-新增功能与修改已有功能)
4. [修改 requirements.md 后如何进入 Plan](#4-修改-requirementsmd-后如何进入-plan)
5. [Plan 会生成或依赖哪些产物](#5-plan-会生成或依赖哪些产物)
6. [Plan 之后的完整流程](#6-plan-之后的完整流程)
7. [同一个项目有多个 feature 规格怎么办](#7-同一个项目有多个-feature-规格怎么办)
8. [公共部分应该如何维护](#8-公共部分应该如何维护)
9. [公共部分是否需要单独建分支](#9-公共部分是否需要单独建分支)
10. [推荐项目结构](#10-推荐项目结构)
11. [公共文档如何被 feature 引用](#11-公共文档如何被-feature-引用)
12. [公共规格变更后已有 feature 怎么处理](#12-公共规格变更后已有-feature-怎么处理)
13. [公共能力与业务功能的依赖写法](#13-公共能力与业务功能的依赖写法)
14. [常用 AI 提示词模板](#14-常用-ai-提示词模板)
15. [常见问题](#15-常见问题)
16. [最佳实践总结](#16-最佳实践总结)

---

## 1. 核心结论

当前项目的 `create-project` 不是直接照搬 GitHub Spec Kit 的：

```text
specs/<branch>/spec.md → /speckit.plan → research.md/data-model.md/quickstart.md
```

而是落到 IIDP SDD 的功能目录：

```text
specs/features/phaseN-[feature]/
├── requirements.md
├── contracts.md
├── backend-spec.md
├── frontend-spec.md
├── interaction-spec.md   # 条件生成
├── plan.md
├── tasks.md
└── validation.md
```

推荐遵循：

```text
一个 feature 分支 ≈ 一个 specs/features/phaseN-[feature]/ 目录
```

示例：

```text
Git 分支：feature/demo/phase1-student
规格目录：specs/features/phase1-student/
需求文件：specs/features/phase1-student/requirements.md
```

写代码前必须确认分支；如果当前在 `main`、`master`、`develop` 等主干分支，按照 `create-project` 的 Implement 前置规则，应先暂停并提示创建 feature 分支。

---

## 2. 当前 create-project 的规格目录关系

项目级稳定约束放在：

```text
specs/
├── mission.md
├── iidp-stack.md
├── ui-constitution.md
├── roadmap.md
├── integration-map.md
├── decisions.md
└── templates/
```

功能级规格放在：

```text
specs/features/phaseN-[feature]/
```

分支名不再要求和目录名完全相同，但建议保持可追踪关系：

```text
feature/{appName}/phase{N}-{feature-name}
specs/features/phase{N}-{feature-name}/
```

每次进入 Plan、Tasks、Implement 前，应明确当前要处理的 feature 目录，不依赖 AI 猜测 active spec。

确认当前分支：

```bash
git branch --show-current
```

确认目标规格目录：

```bash
ls specs/features/phase1-student/requirements.md
```

---

## 3. 新增功能与修改已有功能

### 3.1 新增功能

新增 IIDP 功能时，不是只生成一个 `spec.md`，而是按 SDD 流程逐步生成：

```text
Specify → Clarify → 契约先行 → Backend Spec → Frontend Spec → Plan → Tasks → Implement → Validate
```

最小起点是：

```text
specs/features/phaseN-[feature]/requirements.md
```

然后继续生成：

```text
contracts.md
backend-spec.md
frontend-spec.md
plan.md
tasks.md
validation.md
```

### 3.2 修改已有功能

如果已经存在：

```text
specs/features/phase1-student/requirements.md
```

只是补充需求，不要新建新的 feature 目录，也不要把变更写进公共文档。应直接修改当前 feature 的相关文件：

```text
requirements.md
contracts.md
backend-spec.md
frontend-spec.md
plan.md
tasks.md
validation.md
```

提示 AI 时要明确：

```text
请直接修改 specs/features/phase1-student/requirements.md，不要创建新的 feature 目录，不要改其他 feature。

本次只是在现有学生管理功能上补充需求：
1. 支持导入学生名单
2. 导入失败时记录错误行号和原因
3. 列表支持按状态筛选

请同步检查并更新：
- requirements.md 的用户故事、功能需求、成功标准、待确认事项
- contracts.md 的模型属性、服务签名、视图 key
- backend-spec.md 的模型、服务、视图、权限
- frontend-spec.md 的实现分支和页面节点
- plan.md / tasks.md / validation.md 中受影响的实现和验收项

未知模型、权限码、节点 id、枚举和原型来源必须标记为待确认，不要编造。
```

---

## 4. 修改 requirements.md 后如何进入 Plan

修改 `requirements.md` 后，不能直接跳到 Plan。当前 `create-project` 的强制顺序是：

```text
Step 1 Specify
→ Step 1.2 Clarify（必经）
→ Step 1.3a 契约先行（integration-map.md + contracts.md，强制）
→ Step 1.5a backend-spec.md（必须）
→ Step 1.5b frontend-spec.md（必须）
→ Step 1.5c interaction-spec.md（条件生成）
→ Step 2 plan.md
```

关键点：

- `Clarify` 必须扫描并写回 `requirements.md` 的 `## Clarifications`。
- `contracts.md` 必须先于 `backend-spec.md` 和 `frontend-spec.md`。
- `backend-spec.md` 每个 feature 必须生成。
- `frontend-spec.md` 每个 feature 必须生成，即使最终判定为标准模板/在线视图、前端无需新增代码，也要在第 9 节记录原因。
- 涉及复杂状态、响应式、可访问性、批量操作、危险操作或多步流程时，补 `interaction-spec.md`。

进入 Plan 的提示词：

```text
请基于 specs/features/phase1-student/ 当前 SDD 产物生成或更新 plan.md。

执行前请确认：
1. requirements.md 已完成 Clarify，待确认事项有保留理由
2. specs/integration-map.md 已包含本 Phase 的模型、权限码和前端实现方式决策
3. contracts.md 已定义模型属性、ER 字段、服务签名、视图 key、菜单 key
4. backend-spec.md 已按当前 backend skill 规则生成
5. frontend-spec.md 已按当前 frontend skill 规则生成，并在第 9 节标明实现分支

要求：
- 不要实现代码
- 不要创建新的 feature 目录
- 不要修改其他 feature
- 不要凭记忆填写 IIDP 注解、JSON 结构、权限码或节点 id
- 发现契约缺失时先返回补 contracts.md，不要继续猜 plan
```

---

## 5. Plan 会生成或依赖哪些产物

通用 Spec Kit 的 `/speckit.plan` 常见产物是 `research.md`、`data-model.md`、`quickstart.md`、`contracts/`。当前 `create-project` 不采用这套默认结构。

当前 Plan 阶段的核心文件是：

```text
specs/features/phaseN-[feature]/plan.md
```

Plan 必须依赖已经存在的：

| 文件 | 作用 |
|---|---|
| `requirements.md` | 用户故事、功能需求、成功标准、Clarify 结果 |
| `contracts.md` | API 级契约：模型属性、ER 字段、服务签名、视图 key、菜单 key |
| `backend-spec.md` | IIDP 后端技术落地输入：模型、服务、视图、菜单、权限、验收 |
| `frontend-spec.md` | IIDP 前端技术落地输入：实现分支、节点树、selector、数据源、绑定、事件 |
| `interaction-spec.md` | 条件文件：复杂状态、响应式、可访问性和危险操作验收 |

Plan 自身应包含：

```text
方案概述
后端改动：Maven 模块、app.json、Java 模型、服务、视图 JSON、菜单、apps/apps.json
前端改动：实现分支、目标工程、目标应用、目标页面或节点 id、需要修改的目录
风险与待确认：接口、模型、节点 id、权限码、枚举
```

Plan 生成后有门控：必须展示计划摘要并等待用户确认，确认后才进入 `tasks.md`。

---

## 6. Plan 之后的完整流程

当前完整流程：

```text
requirements.md
→ Clarify 写回
→ contracts.md + integration-map.md
→ backend-spec.md
→ frontend-spec.md
→ interaction-spec.md（条件）
→ plan.md
→ Plan Review Gate
→ tasks.md + validation.md 测试用例规格
→ Blueprint（可选）
→ Implement（经 backend/frontend 子 skill）
→ Validate
→ Phase 复盘与深度审查
```

各阶段作用：

| 阶段 | 作用 |
|---|---|
| Specify | 明确业务对象、模型、服务、权限、数据和验收 |
| Clarify | 发现歧义并写回 `requirements.md` |
| 契约先行 | 在 `integration-map.md` 和 `contracts.md` 中固化模型、服务、权限、视图、菜单 |
| Backend Spec | 生成后端实现输入，必须读取 backend skill |
| Frontend Spec | 生成前端实现输入，必须读取 frontend skill；标准模板也要记录 |
| Plan | 制定实现顺序和风险，不写代码 |
| Tasks | 拆成文件级、服务级、视图级、验证级任务 |
| Implement | 后端经 `skills/backend/SKILL.md`，前端经 `skills/frontend/SKILL.md` 路由 |
| Validate | 静态数量检查、格式检查、单元测试、前端运行验收、Docker 冒烟测试 |

---

## 7. 同一个项目有多个 feature 规格怎么办

一个项目中有多个 feature 是正常的：

```text
specs/features/
├── phase1-student/
├── phase1-course/
├── phase2-payment/
└── phase2-report/
```

但每次工作只处理一个目标 feature。不要让 AI 在一个请求里同时更新多个 feature，除非用户明确要求做跨功能同步。

推荐提示：

```text
当前只处理 specs/features/phase1-student/。
不要读取 phase1-course、phase2-payment 作为当前需求。
如果发现公共契约冲突，只记录影响和建议，不要直接修改其他 feature。
```

### 7.1 主干下有多个 feature 是否正常

正常。主干可以包含所有已合并的 `specs/features/*`。

但开始写代码前，如果当前在 `main`、`master`、`develop`，必须按 `create-project` 的 Implement 前置规则暂停，建议创建：

```bash
git checkout -b feature/{appName}/phase{N}-{feature-name}
```

### 7.2 分支名和 feature 目录不一致怎么办

可以工作，但必须在提示中显式指定目标目录：

```text
当前目标规格目录是 specs/features/phase1-student/，即使分支名不同，也只处理该目录。
```

更推荐让分支名和目录保持对应：

```text
feature/demo/phase1-student
specs/features/phase1-student/
```

---

## 8. 公共部分应该如何维护

公共部分不要塞进某一个业务 feature，也不要长期维护一个永远不合并的公共分支。

当前 `create-project` 推荐的公共层级：

```text
specs/
├── mission.md              # 项目使命
├── iidp-stack.md           # 技术栈与工程约束
├── ui-constitution.md      # UI 宪法
├── roadmap.md              # 路线图
├── integration-map.md      # 架构级契约与跨 feature 集成关系
├── decisions.md            # 决策记录
├── templates/              # 项目级模板覆盖
└── features/
```

公共规范优先放在项目级文件里：

- 项目定位、范围、成功标准：`mission.md`
- IIDP 后端/前端技术栈、分支命名、工程约束：`iidp-stack.md`
- UI 原型、标准模板、组件、可访问性和样式约束：`ui-constitution.md`
- 跨功能模型、权限码、前端实现方式决策：`integration-map.md`
- 被否决方案、约束取舍和变更原因：`decisions.md`
- 阶段计划和状态：`roadmap.md`

通用模板改造放在：

```text
specs/templates/
```

模板覆盖遵循：

```text
specs/templates/{filename}.md > create-project 默认模板
```

---

## 9. 公共部分是否需要单独建分支

不建议长期维护一个公共分支。

不推荐：

```text
common-spec
shared-spec
platform-spec
```

更推荐：

```text
main / dev 作为公共规格最终来源
公共变更开短生命周期文档分支
合并后影响到的 feature 分别做 Spec Sync
```

示例：

```bash
git switch dev
git switch -c docs/update-iidp-contracts
```

修改：

```text
specs/iidp-stack.md
specs/ui-constitution.md
specs/integration-map.md
specs/decisions.md
specs/templates/*.md
```

完成后合并回主干或 `dev`，再对受影响 feature 做漂移检测与修复。

---

## 10. 推荐项目结构

```text
.
├── specs/
│   ├── mission.md
│   ├── iidp-stack.md
│   ├── ui-constitution.md
│   ├── roadmap.md
│   ├── integration-map.md
│   ├── decisions.md
│   ├── templates/
│   │   ├── requirements.md
│   │   ├── contracts.md
│   │   └── ...
│   ├── features/
│   │   └── phase1-student/
│   │       ├── requirements.md
│   │       ├── contracts.md
│   │       ├── backend-spec.md
│   │       ├── frontend-spec.md
│   │       ├── interaction-spec.md
│   │       ├── plan.md
│   │       ├── tasks.md
│   │       └── validation.md
│   └── legacy/
│       └── [module]-business-rules.md
├── backlog/
│   └── ideas.md
├── docs/
│   └── adr/
├── CHANGELOG.md
└── README.md
```

说明：

- `specs/legacy/` 用于存量项目接入、代码侦察和业务规则提取。
- `backlog/ideas.md` 用于候选想法，不等同于已进入 roadmap 或 feature 的承诺。
- `docs/adr/` 可记录跨项目架构决策；IIDP SDD 阶段内的决策仍同步到 `specs/decisions.md`。

---

## 11. 公共文档如何被 feature 引用

在每个 feature 的 `requirements.md`、`contracts.md` 或 `plan.md` 中增加公共规格引用。

推荐写法：

```markdown
## 公共规格引用

本功能必须遵守以下项目级规格：

- `specs/mission.md`
- `specs/iidp-stack.md`
- `specs/ui-constitution.md`
- `specs/integration-map.md`
- `specs/decisions.md`

本功能使用以下项目级模板或约束：

- `specs/templates/requirements.md`（如存在）
- `specs/templates/contracts.md`（如存在）
```

如果依赖其他公共能力 feature：

```markdown
## 依赖关系

本功能依赖以下公共能力：

- `specs/features/phase1-auth/requirements.md`
- `specs/features/phase1-permission/contracts.md`
- `specs/features/phase1-file/backend-spec.md`
```

这样 AI 在生成 Plan 和 Tasks 时不会重复设计登录、权限、文件上传等公共能力。

---

## 12. 公共规格变更后已有 feature 怎么处理

公共规格变更后，应做 Spec Sync 漂移检测与修复。当前 create-project 的验证专题支持：

```text
propose → apply → backfill
```

推荐流程：

```text
1. 修改项目级公共规格
2. 在 decisions.md 记录变更原因和影响范围
3. 更新 CHANGELOG.md
4. 找出受影响 feature
5. 对每个受影响 feature 执行漂移检测
6. 修复 requirements/contracts/backend-spec/frontend-spec/plan/tasks/validation
7. Phase 结束或 PR 前执行深度审查
```

示例提示：

```text
公共权限码规则已经更新。请对 specs/features/phase1-student/ 做 Spec Sync 漂移检测：

1. 读取 specs/integration-map.md、specs/decisions.md 和当前 feature 的 contracts.md / backend-spec.md / frontend-spec.md
2. 找出权限码、服务 auth、按钮权限和前端显隐规则的冲突
3. 先输出 propose 修复建议，不要直接修改代码
4. 我确认后再 apply 到规格文件
5. 若实现已存在，补 backfill 任务到 tasks.md 和 validation.md
```

---

## 13. 公共能力与业务功能的依赖写法

公共能力如果只是规范，放在项目级文件。

例如：

- 统一权限码规则 → `specs/integration-map.md`
- UI 标准模板约束 → `specs/ui-constitution.md`
- 技术栈和工程路径 → `specs/iidp-stack.md`
- 重要取舍和例外 → `specs/decisions.md`

公共能力如果需要实现、测试和交付，应作为独立 feature：

```text
specs/features/phase1-auth/
specs/features/phase1-permission/
specs/features/phase1-file/
specs/features/phase1-audit-log/
```

业务 feature 引用公共能力时，不要复制它的完整设计，只写依赖关系和调用边界：

```markdown
## 依赖关系

本功能依赖：

- 权限中心：`specs/features/phase1-permission/`
- 文件能力：`specs/features/phase1-file/`
- 项目权限码总览：`specs/integration-map.md`

本功能只新增学生管理相关模型、服务、视图和权限，不重新实现登录、角色、菜单树或文件存储。
```

---

## 14. 常用 AI 提示词模板

### 14.1 修改已有 requirements.md

```text
请直接修改 specs/features/phase1-student/requirements.md。

要求：
1. 不要创建新的 feature 目录
2. 不要修改其他 feature
3. 不要实现代码
4. 不要删除已有需求
5. 未知模型、权限码、节点 id、枚举和原型来源标记为待确认

变更内容：
- 支持学生信息导入
- 导入失败时记录错误行号和原因
- 列表支持按状态筛选

请合并到：
- 能力识别
- 用户故事与验收场景
- 功能需求
- 成功标准
- 假设与边界
- Clarifications（如有新澄清）
```

### 14.2 执行 Clarify

```text
请对 specs/features/phase1-student/requirements.md 做 Clarify 扫描。

要求：
1. 按 create-project 的 10 个维度识别歧义和缺失
2. 只提出影响最大的 ≤5 个问题
3. 每次只问一个问题，等待我回答
4. 每条回答确认后写回 ## Clarifications，并同步修改受影响章节
5. 不进入 backend-spec/frontend-spec/plan 之前不得跳过 Clarify
```

### 14.3 契约先行

```text
请为 specs/features/phase1-student/ 执行 Step 1.3a 契约先行。

要求：
1. 更新 specs/integration-map.md：模型清单、ER、权限码、跨模型服务、前端实现方式决策
2. 生成或更新 specs/features/phase1-student/contracts.md：模型属性、ER 字段、服务签名、视图 key、菜单 key、app.json 条目
3. 填写前必须读取对应 backend skill 参考，不得凭记忆写 @Model、@Property、@MethodService 或视图 JSON 参数
4. 缺失事实标记为待确认
```

### 14.4 生成 Plan

```text
请基于 specs/features/phase1-student/ 生成或更新 plan.md。

输入文件：
- requirements.md
- contracts.md
- backend-spec.md
- frontend-spec.md
- interaction-spec.md（如存在）

要求：
1. 不要实现代码
2. 不要创建新的 feature 目录
3. 后端改动按 Maven 模块、app.json、Java 模型、服务、视图 JSON、菜单、apps/apps.json 拆分
4. 前端改动按 frontend-spec.md 第 9 节实现分支拆分
5. 风险与待确认必须列出接口、模型、节点 id、权限码、枚举
6. 生成后输出 Plan Review Gate 摘要，等待我确认后再生成 tasks.md
```

### 14.5 生成 Tasks

```text
请基于 specs/features/phase1-student/plan.md 生成 tasks.md，并同步更新 validation.md 的测试用例规格。

要求：
1. 不要实现代码
2. 任务按后端工程基础、模型、服务、视图菜单、前端实现分支、前端页面、测试、验证拆分
3. 每个任务必须可执行、可勾选
4. validation.md 中的 TC-BE-xx / TC-FE-xx 必须能追溯到 requirements.md 的 AC
5. 写代码前必须包含 Git 分支检查任务
```

### 14.6 公共规格变更

```text
请修改 specs/integration-map.md，补充统一权限码和跨模型服务命名规则。

要求：
1. 不要修改任何 specs/features/* 业务功能目录
2. 不要实现代码
3. 同步在 specs/decisions.md 记录本次规则变更原因
4. 同步在 CHANGELOG.md 记录公共规格变更
5. 列出可能受影响的 feature，后续逐个做 Spec Sync
```

---

## 15. 常见问题

### Q1：项目里多个 feature 会冲突吗？

不会。`specs/features/` 下可以有多个功能目录，但一次只处理一个目标 feature。

### Q2：为什么不能直接从 requirements.md 跳到 Implement？

因为当前 create-project 强制要求契约先行和技术规格落地：

```text
requirements.md → contracts.md/integration-map.md → backend-spec.md/frontend-spec.md → plan.md/tasks.md → Implement
```

跳过中间层会导致模型、权限码、视图 key、服务签名、节点 id 在实现阶段被猜出来。

### Q3：标准模板页面还要生成 frontend-spec.md 吗？

要。每个 feature 都必须生成 `frontend-spec.md`。如果前端无需新增代码，也要在第 9 节写明：

```text
实现分支：标准模板/在线视图
结论：前端无需新增代码
原因：后端视图和菜单配置已满足搜索、表格、表单和操作需求
```

### Q4：公共规范是否要做成 feature？

一般不需要。公共规范放项目级文件；只有需要实现、测试和交付的公共能力才做成独立 feature。

### Q5：是否可以创建 `phase0-common`？

可以作为索引，但不推荐把所有公共内容塞进去。公共规则优先放在：

```text
specs/mission.md
specs/iidp-stack.md
specs/ui-constitution.md
specs/integration-map.md
specs/decisions.md
specs/templates/
```

如果创建 `specs/features/phase0-common/`，建议只记录公共能力索引和依赖边界，不承载所有项目规则。

### Q6：公共规格变化后是否要重跑所有 feature？

不需要无差别重跑。先根据 `integration-map.md`、`contracts.md`、权限码、模型和前端实现分支找受影响 feature，再逐个做 Spec Sync。

### Q7：Plan Review Gate 可以跳过吗？

当前 `sdd-workflow.md` 将 Plan Review Gate 定义为 Step 2 后的门控：`plan.md` 生成后必须展示摘要并等待用户确认，再进入 `tasks.md`。除非用户明确授权跳过，否则不要自动继续。

---

## 16. 最佳实践总结

推荐：

```text
项目级硬约束：
specs/mission.md
specs/iidp-stack.md
specs/ui-constitution.md
specs/integration-map.md
specs/decisions.md

项目级模板：
specs/templates/*.md

公共能力：
specs/features/phase1-auth/
specs/features/phase1-permission/
specs/features/phase1-file/

业务功能：
specs/features/phase1-student/
specs/features/phase1-course/

存量规则：
specs/legacy/*.md

候选想法：
backlog/ideas.md
```

不推荐：

```text
长期维护 common-spec 分支
把所有公共内容写进一个超大的 common spec
在主干直接进入 Implement 写代码
跳过 contracts.md 直接写 backend-spec/frontend-spec
让一个业务 feature 同时承担公共规范和业务需求
```

最终口诀：

```text
新功能：建 specs/features/phaseN-feature
改旧功能：直接改对应 feature 的 requirements/contracts/backend-spec/frontend-spec/plan/tasks/validation
进入 Plan：先 Clarify，再契约先行，再 backend/frontend spec
进入 Tasks：先过 Plan Review Gate
进入 Implement：先检查 Git 分支，再经 backend/frontend 子 skill
公共规则进项目级 specs
公共能力单独建 feature
公共变更开短分支，合并后做 Spec Sync
```
