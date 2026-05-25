# Skills 目录使用指南

本目录下的 Markdown 文件是 **给 AI Agent 用的操作说明**：描述何时启动、输入输出、检查清单与命名约束。人类读者用本指南快速选对文件；在对话里 **@ 引用对应文件** 或把路径写进项目规则（如 `CLAUDE.md` / `.cursor/rules`），Agent 会按文档执行。

---

## 目录

1. [概述](#概述)
2. [安装](#安装)（Skills 安装 & 命令注册）
3. [两种使用方式](#两种使用方式)
4. [方式一：手动命令](#方式一手动命令推荐新手)
5. [方式二：自动 Workflow（推荐新手）](#方式二自动-workflow)
6. [完整示例：从零搭建学生管理模块](#完整示例从零搭建学生管理模块)
7. [CLAUDE.md 续接机制](#claudemd-续接机制)
8. [常见问题](#常见问题)

---

## 概述

`create-project` 把 IIDP 项目需求转成可执行的 SDD 规格产物：

```
需求描述
  → requirements.md（功能规格）
  → contracts.md（前后端契约）
  → backend-spec.md + frontend-spec.md（技术落地规格）
  → plan.md（实现计划）
  → tasks.md（任务清单）
  → 代码实现
  → validation.md（验收报告）
```

---

## 安装

### 1. 注册 create-project Slash Commands

将 `sdd-*.md` 命令文件复制到 Claude Code 命令目录，即可在对话中使用 `/sdd-` 前缀命令：

```bash
mkdir -p .claude/commands
cp skills/create-project/commands/sdd-*.md .claude/commands/
```

安装后在 Claude Code 输入 `/sdd-` 即可看到全部 12 条命令：

| 命令 | 说明 | 是否必须 |
|---|---|---|
| `/sdd-init-templates` | 初始化项目级模板目录 | 可选 |
| `/sdd-specify` | 生成功能规格 requirements.md | 必须 |
| `/sdd-clarify` | 规格歧义澄清（可多轮） | 必须 |
| `/sdd-contracts` | 契约先行：integration-map + contracts | **强制** |
| `/sdd-critique` | 产品 + 工程双视角批判报告 | 可选 |
| `/sdd-spec` | 生成 backend-spec + frontend-spec | 必须 |
| `/sdd-plan` | 生成 plan.md，写回 CLAUDE.md 标记 | 必须 |
| `/sdd-tasks` | 生成 tasks.md + validation.md TC 节 | 必须 |
| `/sdd-blueprint` | 代码蓝图（伪代码 + 文件清单） | 可选 |
| `/sdd-implement` | 执行单个任务（重复直到全部完成） | 必须 |
| `/sdd-validate` | 运行验收清单，输出验收报告 | 必须 |
| `/sdd-review` | 5 子 Agent 并行深度审查 | 必须 |

---

### 2. 安装 Skills 上下文文件

将 `skills/` 目录下所有技能文件复制到 `.claude/skills/`，Claude Code 会自动加载：

```bash
mkdir -p .claude/skills
cp -r skills/* .claude/skills/
```

安装后包含以下技能：

| 技能文件 | 用途 |
|---|---|
| `backend/SKILL.md` | IIDP 后端工程：模型、服务、视图、权限、API |
| `backend/greenfield/SKILL.md` | 从零搭建 IIDP 父工程（首次建项目） |
| `frontend/SKILL.md` | IIDP 前端总入口，自动路由到 6 个子技能 |
| `app-store/SKILL.md` | IIDP 应用市场 JSON-RPC 服务构建 |
| `gitlab/SKILL.md` | GitLab MCP：MR、代码搜索、CI 流水线 |
| `jenkins/SKILL.md` | Jenkins MCP：任务查询、触发、构建日志 |

---

## 两种使用方式

| 方式 | 适用场景 | 入口 |
|---|---|---|
| **手动命令** | 分步控制、局部触发、断点续接 | `/sdd-xxx` |
| **自动 Workflow** | 一键跑完整流程，含循环和门控 | `skills/create-project/workflows/iidp-sdd.yml` |

---


## 方式二：自动 Workflow （推荐新手）

```
/create-project 正逆向追溯功能规格说明书V8.0.md
```

### 内置循环结构

```text
[第 0 步] 读取项目宪法和前后端 Skill
    ↓
[需求进入] 识别 IIDP 能力域
    ↓
[Specify] 写功能规格：模型、页面、服务、权限、数据、验收
    ↓
[Clarify / Step 1.2] ★ 规格歧义扫描，生成结构化澄清问题（≤5 条），答案写回 requirements.md（必经步骤）
    ↓
[契约先行 / Step 1.3a] ★ 定义 integration-map.md + contracts.md 契约（必须先于规格书，强制步骤）
    ↓
[Critique / Step 1.4] ★ 产品战略 + 工程风险双视角批判，暂停等用户确认（可选）
    ↓
[Backend Spec] 按 sdd-backend.md 模板生成 backend-spec.md（必写）
    ↓
[Frontend Spec] 按 sdd-frontend.md 模板生成 frontend-spec.md（必写，§9 记录实现分支）
    ↓
[Interaction Spec] 含复杂状态/响应式/可访问性？→ 按 sdd-frontend-interaction.md 模板生成 interaction-spec.md（可选）
    ↓
[Plan] 生成实现计划：后端优先，前端按实现分支决策 （必写）
    ↓
[Plan Review Gate] ★ 展示计划摘要，暂停等用户确认后才生成任务（可选）
    ↓
[Tasks] 拆解可执行任务：文件级、服务级、视图级、验证级（必写）
    ↓
[Blueprint] ★ 生成代码蓝图（伪代码+文件清单），暂停等用户确认（可选）
    ↓
[Implement] 按任务落地：一次一个任务，子 skill 读取 spec 文件（必写）
    ↓
[Validate] 按 IIDP 清单验证；完成后提示生成 PR Bridge 描述（必写）
```

## 方式一：手动命令

### 完整流程

```
/sdd-init-templates   ← [可选] 初始化项目级模板（首次使用建议运行，后续无需重复）
     ↓
/sdd-specify          ← 输入功能描述，生成 requirements.md
     ↓
/sdd-clarify          ← 规格澄清（可多轮，直到无歧义）
     ↓
/sdd-contracts  ★     ← 契约先行（强制，必须先于规格书）
     ↓
/sdd-critique         ← 规格批判（可选）
     ↓
/sdd-spec             ← 生成 backend-spec.md + frontend-spec.md
     ↓
/sdd-plan             ← 生成 plan.md，展示摘要等确认，写回 CLAUDE.md
     ↓
/sdd-tasks            ← 生成 tasks.md + validation.md TC 节
     ↓
/sdd-blueprint        ← 代码蓝图（可选）
     ↓
/sdd-implement        ← 执行一个任务（重复直到全部完成）
     ↓
/sdd-validate         ← 运行验收检查
     ↓
/sdd-review           ← 5 子 Agent 深度审查，Phase 结束
```

`★` 为强制步骤，不可跳过。

---

### 各命令说明

#### `/sdd-init-templates [模板名称...]` （可选）

将 `sdd-constitution.md` 中的默认模板提取到 `specs/templates/`，用户可直接编辑定制模板结构。**后续所有规格生成命令将自动优先使用项目级模板。**

> 不运行此命令也可以：手动在 `specs/templates/` 创建与默认模板同名的 `.md` 文件即可生效。

**使用方式：**

```
/sdd-init-templates                        # 进入交互式选择
/sdd-init-templates requirements contracts # 直接初始化指定模板
/sdd-init-templates all                    # 初始化全部模板
```

**可选模板列表（交互模式下显示）：**

```
A) requirements.md    — 功能规格（US 格式、FR 编号、成功标准章节结构）
B) contracts.md       — API 契约（字段表格列、服务签名、app.json 格式）
C) mission.md         — 项目使命（定位、目标用户、核心价值）
D) iidp-stack.md      — 技术栈约束（工程路径、命名规则、Git 工作流）
E) ui-constitution.md — UI 宪法（设计来源、组件规则、可访问性）
F) roadmap.md         — 路线图（Phase 结构、验收标准、技术债表格）
G) integration-map.md — 集成地图（模型清单、ER、权限码总览表格）
H) all                — 全部初始化
```

**产物：**
- `specs/templates/{filename}.md`：已提取的可编辑模板，顶部含说明注释
- `specs/templates/README.md`：已初始化模板列表、覆盖规则、注意事项

**完成后：** 编辑 `specs/templates/` 中的文件，再运行 `/sdd-specify` 或 `/sdd-contracts` 即会自动使用定制模板。

---

#### `/sdd-specify [功能描述]`

生成 `requirements.md` 初稿。

```
/sdd-specify 学生信息管理：支持新增、编辑、删除学生，按班级筛选，导出 Excel
```

产物：`specs/features/phase1-student-mgr/requirements.md`

---

#### `/sdd-clarify`

扫描 10 类歧义，逐题交互，答案写回 `requirements.md § Clarifications`。每轮最多 5 题，有歧义时重复运行。

```
/sdd-clarify
```

---

#### `/sdd-contracts` ★

定义所有模型、服务、视图、权限码的契约，**必须先于规格书执行**。

```
/sdd-contracts
```

产物：
- `specs/integration-map.md`（架构级：模型清单、ER、权限码总览）
- `specs/features/<feature>/contracts.md`（API 级：字段注解、服务签名、视图 key）

> 后续规格书的所有字段注解、权限码必须取自 `contracts.md`，不得自行发明。

---

#### `/sdd-critique`

产品战略 + 工程风险双视角批判，**不修改规格，只输出发现报告**，等用户决策。

触发条件（满足一项即建议运行）：涉及 3+ 模型 / 包含状态机 / 待确认事项超过 3 项。

---

#### `/sdd-spec [backend|frontend|both]`

生成技术落地规格书。

```
/sdd-spec both
```

产物：
- `backend-spec.md`：命名、模型注解、服务设计、视图、菜单、权限
- `frontend-spec.md`：节点树、selector、数据源、实现分支（§9）
- `interaction-spec.md`（条件生成）：含复杂状态 / 响应式 / 可访问性时自动生成

---

#### `/sdd-plan`

生成 `plan.md`，展示计划摘要**等用户确认**后，写回 `CLAUDE.md` 标记。

```
/sdd-plan
```

摘要示例：
```
后端改动量：3 个模型 / 5 个服务 / 6 个视图文件
前端实现分支：标准模板（3 个页面无需前端代码）
估计复杂度：M（1–3 天）
```

---

#### `/sdd-tasks [tdd]`

生成 `tasks.md`，同步从 `requirements.md` 验收标准提取 TC-BE-xx / TC-FE-xx 写入 `validation.md`。

```
/sdd-tasks          # 标准模式
/sdd-tasks tdd      # TDD 模式：测试任务移至对应实现块之前
```

---

#### `/sdd-blueprint`

生成代码蓝图（伪代码 + 文件清单），**不写真实代码**，等用户确认后再实现。

触发条件：L 级复杂度 / 跨模型服务 / 团队首次接入 IIDP 前端扩展。

---

#### `/sdd-implement [next|任务ID]`

执行 `tasks.md` 中下一个未完成任务，**一次只处理一个**。

```
/sdd-implement          # 执行下一个未完成任务
/sdd-implement T005     # 执行指定任务
```

完成后将 `- [ ]` 改为 `- [x]`，输出变更摘要后停止。重复运行直到所有任务完成。

---

#### `/sdd-validate [backend|frontend|both]`

按 `validation-checklist.md` 运行验收，输出报告，更新 `validation.md` 测试覆盖率追踪表。

有失败项时按分类（规格 / 实现 / 环境 / 平台限制）给出最小修复方案，修复后重新运行。

---

#### `/sdd-review`

启动 5 个子 Agent 并行深度审查，Phase 结束标志。

| Agent | 视角 |
|---|---|
| 规格内部一致性 | 规格文件之间的矛盾与漏洞 |
| 后端规格对齐 | 注解 / 服务 / 权限码与 backend skills 对照 |
| 前端规格对齐 | 节点 / 分支 / 扩展与 frontend skills 对照 |
| 安全与权限边界 | 权限旁路、多租户、敏感字段 |
| AI 可操作性 | 任务可执行性、待确认阻塞、矛盾指令 |

完成后写回 `CLAUDE.md` 为 `Phase 完成 ✅`，高优先级发现记入 `decisions.md`。

---


## 完整示例：从零搭建学生管理模块

```bash
# 仅首次安装命令
cp skills/create-project/commands/sdd-*.md .claude/commands/
```

```
# Step 1：描述功能
/sdd-specify 学生信息管理：新增/编辑/删除学生，按班级和年级筛选，支持 Excel 导入导出

# Step 2：澄清歧义（交互式）
/sdd-clarify
# → Q: 学生状态有哪些？  A: 在校 / 休学 / 毕业（三态）
# → Q: 导出范围？        A: 当前筛选结果，最多 5000 条
# 无更多歧义，继续

# Step 3：定义契约（强制）
/sdd-contracts

# Step 4：生成规格书
/sdd-spec

# Step 5：生成计划，等确认摘要
/sdd-plan
# → 摘要：3 个模型 / 5 个服务 / 6 个视图，复杂度 M
# → 用户确认后继续

# Step 6：生成任务清单
/sdd-tasks

# Step 7：逐任务实现（每次一个）
/sdd-implement   # T001 Git 分支准备
/sdd-implement   # T002 新增 POM 模块
/sdd-implement   # T003 编写 StudentModel.java
# ... 重复直到所有任务完成（tasks.md 全部 [x]）

# Step 8：验收
/sdd-validate

# Step 9：Phase 结束深度审查
/sdd-review
# → 汇总报告，决策处置，Phase 完成 ✅
```

---

## CLAUDE.md 续接机制

`/sdd-plan` 和 `/sdd-review` 执行后自动写回 `CLAUDE.md`：

```markdown
<!-- IIDP-SDD START -->
当前活动功能：specs/features/phase1-student-mgr/
实现计划：specs/features/phase1-student-mgr/plan.md
当前阶段：Step 4 Implement（进行中）
<!-- IIDP-SDD END -->
```

**新会话续接**：直接运行命令，Claude 自动读取标记，无需重新描述上下文：

```
/sdd-implement   # 自动知道当前功能和步骤，继续执行下一个未完成任务
```

---

## 常见问题

**Q：`/sdd-contracts` 必须每次都跑吗？**  
A：每个新 feature 运行一次。`integration-map.md` 会追加，不覆盖其他 Phase 内容。

**Q：`/sdd-implement` 可以跳过某个任务吗？**  
A：可以：`/sdd-implement T005`。但建议按顺序执行，任务间有依赖关系。

**Q：前端标准模板页需要跑 `/sdd-spec` 吗？**  
A：需要。`frontend-spec.md` 必须生成，在 §9 标注"标准模板，前端无需新增代码"，这是有意为之的明确记录。

**Q：Critique 和 Blueprint 都可选，什么时候跳过？**  
A：简单单模型 CRUD 可跳过。涉及跨模型服务、状态机、或首次接入 IIDP 前端时建议触发。

**Q：`/sdd-review` 的 5 个子 Agent 是真正并行的吗？**  
A：支持 `Agent` 工具的环境（Claude Code Web / API）下是并行的；普通对话会顺序执行，结果相同，速度较慢。
