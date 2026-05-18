# SDD 完整项目实战手册索引

本索引用于渐进加载 `create-project` 的 SDD 参考资料。先读本文件判断任务范围，再只读取相关专题文件；不要默认一次性读取所有 reference。

## 使用顺序

1. 创建、规划或标准化项目时，先读本索引。
2. 根据用户目标读取下表中最小必要专题。
3. 写后端规格或代码前，继续读取 `skills/backend/SKILL.md` 和命中的后端专题。
4. 写前端规格或代码前，继续读取 `skills/frontend/SKILL.md` 和命中的前端专题。
5. 涉及 UI 原型时，先提示用户提供任意支持 MCP 的原型/设计/产品工具服务、资源 URI、页面或 Frame。

## 专题文件

| 文件 | 内容 | 何时读取 |
|---|---|---|
| `sdd-constitution.md` | 核心定位、项目宪法、`mission.md`、`iidp-stack.md`、`ui-constitution.md`、`integration-map.md` | 建项、补项目宪法、补 UI 宪法、定义跨功能约束 |
| `sdd-workflow.md` | 规格层次、功能迭代闭环、能力识别、Clarify 规格澄清（Step 1.2）、Critique、Plan Review Gate、Blueprint、计划、任务、验收目录 | 生成 requirements/plan/tasks/validation，执行规格澄清或解释 SDD 流程 |
| `sdd-backend.md` | IIDP 后端规格模板：命名、工程文件、模型、服务、视图、菜单、权限、验收 | 生成或审核后端规格 |
| `sdd-frontend.md` | IIDP 前端**技术落地**规格模板：节点树、selector、`ds_config`、绑定、事件、commands、hook；读完后按 `SKILL.md` 前端子 skill 路由规则继续执行 | 已知实现方式，需要输出节点/绑定/事件技术细节时；从需求生成规格时先走 `iidp-frontend-spec-doc` |
| `sdd-frontend-interaction.md` | 前端交互设计规格模板：用户流程、节点层级、状态表、响应式、可访问性、验收标准；产物为 `interaction-spec.md`（触发条件见 `sdd-workflow.md` Step 1.5c） | 页面含复杂状态机、响应式策略、可访问性要求或危险操作时生成 `interaction-spec.md` |
| `sdd-contracts.md` | 前后端契约：JSON-RPC、Filter、按钮服务、节点 id、节点属性与事件 | 生成契约表、接口参数、视图按钮或节点扩展契约 |
| `sdd-validation.md` | 实现顺序、后端/前端验证、偏差处理、失败分类、阶段复盘、Spec Sync 漂移检测（含 propose/apply/backfill 修复流程）、PR Bridge PR 描述生成、CHANGELOG | 验证交付、处理失败、阶段复盘、漂移检测与修复闭环、生成 PR 描述 |
| `sdd-brownfield.md` | 存量项目接入：侦察、契约提取、增量规格；EDCR 能力发现框架（Evidence→Discovery→Capabilities→Risk）；IIDP 威胁矩阵（STRIDE 适配）；风险优先级与 Phase 门控 | 接入或改造已有 IIDP 项目；需要能力发现、风险建模或技术债评估时 |
| `usage.md` | create-project 详细使用说明、输入输出、Prompt、质量检查、常见误用 | 用户询问“怎么用 create-project”或需要完整操作指南时 |
| `sdd-skill-maintenance.md` | `create-project` skill 维护规则、文件结构、校验命令 | 修改本 skill 或检查是否符合 skill 规范 |
| `sdd-prompts-tools-principles.md` | Prompt 速查、工具命令、核心原则、常见陷阱 | 用户要提示词、工具清单、原则或总览时 |
| `sdd-review.md` | 深度审查：触发时机、5 个标准子 Agent（规格一致性/后端对齐/前端对齐/安全边界/AI 可操作性）、汇总报告格式、调用约定 | Phase 结束、PR 前或规格重大变更时触发多 Agent 并行审查 |


## IIDP SDD 总原则

- 当前项目以 `skills/backend` 和 `skills/frontend` 为事实来源。
- 不套用通用开源框架范式；需求先落到 IIDP 的模型、视图、菜单、服务、权限、标准模板、hook、扩展视图和 Vue2 组件边界。
- 传统管理后台优先标准模板和在线视图；只有标准能力不足时才写前端扩展。
- 未知模型、节点 id、权限码、枚举、服务、路由、原型来源必须标记为“待确认”。
- 需要 UI 原型描述时，优先读取任意支持 MCP 的原型/设计/产品工具服务；无法读取时用截图、导出文档或文字描述兜底，并标记来源可信度。
- `integration-map.md` 在 **Step 1.3a** 填写（先于规格书），Phase 完成后复盘更新；`roadmap.md`、`decisions.md`、`CHANGELOG.md` 在 Phase 完成后同步。

## IIDP 完整项目结构

```text
agents/
├── AGENTS.md
├── specs/
│   ├── mission.md
│   ├── iidp-stack.md
│   ├── ui-constitution.md
│   ├── roadmap.md
│   ├── integration-map.md
│   ├── decisions.md
│   ├── features/
│   │   └── phase1-[feature]/
│   │       ├── requirements.md
│   │       ├── sdd-contracts.md      # API 级契约（Step 1.3a 生成，先于规格书）
│   │       ├── backend-spec.md       # 后端技术落地规格（模型、服务、视图、菜单、权限细节）
│   │       ├── frontend-spec.md      # 前端技术落地规格（仅需要前端代码时生成）
│   │       ├── interaction-spec.md   # 前端交互规格（含复杂状态/响应式/可访问性时生成）
│   │       ├── plan.md
│   │       ├── tasks.md
│   │       └── validation.md
│   └── legacy/                     # Legacy 代码分析（存量项目）
│       └── [module]-business-rules.md
├── skills/
│   ├── backend/
│   ├── frontend/
│   └── create-project/
│       ├── SKILL.md
│       ├── agents/
│       │   └── openai.yaml
│       └── references/
│           ├── sdd.md
│           ├── sdd-constitution.md
│           ├── sdd-workflow.md
│           ├── sdd-backend.md
│           ├── sdd-frontend.md
│           ├── sdd-frontend-interaction.md
│           ├── sdd-contracts.md
│           ├── sdd-validation.md
│           ├── sdd-review.md
│           ├── sdd-brownfield.md
│           ├── usage.md
│           ├── sdd-skill-maintenance.md
│           └── sdd-prompts-tools-principles.md
├── iidp-backend-demo-ai/
│   ├── pom.xml
│   ├── settings.xml
│   ├── apps/
│   ├── docker-compose.yml
│   ├── docker/
│   ├── sie-iidp-demo-apps/
│   ├── sie-iidp-demo-common/
│   └── sie-iidp-demo-start/
├── [frontend-project]/
│   ├── package.json
│   ├── config/
│   └── apps/
│       └── <appName>/
│           ├── config/
│           ├── common/
│           ├── views/
│           └── resource/
├── backlog/
│   └── ideas.md
├── CHANGELOG.md
└── README.md
```
`specs/legacy/` 用于 Legacy 代码分析（存量项目），保存代码侦察、业务规则提取和变更区域规格依据。
`backlog/ideas.md` 用于记录待研究想法、候选需求、未排期能力和产品探索，不等同于已进入 `roadmap.md` 或 `specs/features/` 的承诺范围。
