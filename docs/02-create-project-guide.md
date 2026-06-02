# create-project 使用指南

根据需求描述，通过 SDD（规格驱动开发）流水线，自动生成符合 IIDP 平台标准的完整应用代码。

---

## 目录

1. [概述](#概述)
2. [安装](#安装)
3. [命令列表](#命令列表)
4. [方式一：手动命令](#方式一手动命令)
5. [方式二：自动 Workflow](#方式二自动-workflow)
6. [新增命令说明](#新增命令说明)
7. [完整示例](#完整示例)
8. [CLAUDE.md 续接机制](#claudemd-续接机制)
9. [常见问题](#常见问题)

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

```bash
mkdir -p .claude/commands
cp skills/create-project/commands/sdd-*.md .claude/commands/
```

### 2. 安装 Skills 上下文文件

```bash
mkdir -p .claude/skills
cp -r skills/* .claude/skills/
```

---

## 命令列表

安装后在 Claude Code 输入 `/sdd-` 即可看到全部 14 条命令：

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
| `/sdd-ui-parse` | 解析静态 HTML 原型，生成 interaction-spec.md | 可选 |
| `/sdd-brownfield-init` | 存量项目 SDD 接入初始化 | 可选 |

---

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
/sdd-ui-parse         ← [可选] 若有静态 HTML 原型，解析生成 interaction-spec.md
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

```
/sdd-init-templates                        # 进入交互式选择
/sdd-init-templates requirements contracts # 直接初始化指定模板
/sdd-init-templates all                    # 初始化全部模板
```

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

---

#### `/sdd-contracts` ★

定义所有模型、服务、视图、权限码的契约，**必须先于规格书执行**。

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

产物：
- `backend-spec.md`：命名、模型注解、服务设计、视图、菜单、权限
- `frontend-spec.md`：节点树、selector、数据源、实现分支（§9）
- `interaction-spec.md`（条件生成）：含复杂状态 / 响应式 / 可访问性时自动生成

---

#### `/sdd-plan`

生成 `plan.md`，展示计划摘要**等用户确认**后，写回 `CLAUDE.md` 标记。

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

## 方式二：自动 Workflow

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
[Plan] 生成实现计划：后端优先，前端按实现分支决策（必写）
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

---

## 新增命令说明

### `/sdd-ui-parse`

**用途**：解析 `spec/modules/*/ui/` 目录下的静态 HTML 原型，使用 Playwright MCP 提取结构化交互需求，生成 `interaction-spec.md`（8 节格式）。

**触发时机**：`code-index` skill 输出了 HTML 原型文件后，在 `/sdd-spec` 之前或之后运行。

**执行过程**：
1. 对每个 HTML 文件：`mcp__playwright__navigate(file:///path/to/page.html)` → `snapshot`（获取 accessibility tree）→ `screenshot`
2. 模拟点击"新增"/"编辑"/"查看"等按钮，捕获弹窗/状态变化
3. 识别状态流转（步骤条 / Timeline / workflow CSS 类）
4. 将识别到的操作与 `contracts.md` 接口签名匹配

**产物**：`specs/features/<feature>/interaction-spec.md`，包含 8 节：
1. 页面目标与入口
2. 用户操作流程（主路径 + 分支）
3. 交互状态表（页面级 / 区域级 / 权限与错误）
4. 响应式与容器策略
5. 无障碍与易用性
6. 组件清单
7. API 调用映射
8. 待确认项

---

### `/sdd-brownfield-init`

**用途**：存量 IIDP 项目接入 SDD 流程——从已有代码逆向生成规格书初稿，再通过 SDD 流水线补全缺失部分。

**使用场景**：已有运行中的 IIDP 应用，希望用 SDD 规范化后续开发。

**执行步骤**：
1. 扫描已有代码（Model / Service / View 文件）
2. 逆向生成 `requirements.md`、`contracts.md` 初稿
3. 在 `CLAUDE.md` 写入 `[BROWNFIELD]` 标记，后续命令自动跳过已实现部分

---

## 完整示例

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

# Step 5：（可选）若有 HTML 原型，解析交互需求
/sdd-ui-parse

# Step 6：生成计划，等确认摘要
/sdd-plan
# → 摘要：3 个模型 / 5 个服务 / 6 个视图，复杂度 M
# → 用户确认后继续

# Step 7：生成任务清单
/sdd-tasks

# Step 8：逐任务实现（每次一个）
/sdd-implement   # T001 Git 分支准备
/sdd-implement   # T002 新增 POM 模块
/sdd-implement   # T003 编写 StudentModel.java
# ... 重复直到所有任务完成（tasks.md 全部 [x]）

# Step 9：验收
/sdd-validate

# Step 10：Phase 结束深度审查
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

**Q：已有存量项目怎么接入 SDD 流程？**  
A：先用 `code-index` skill 生成规格书，再用 `/sdd-brownfield-init` 逆向生成规格初稿，然后正常走 SDD 流水线。

**Q：`/sdd-ui-parse` 需要在哪一步运行？**  
A：在 `code-index` 生成了 `spec/modules/*/ui/*.html` 之后，`/sdd-spec` 前后均可运行。建议在 `/sdd-contracts` 之后运行，这样 API 映射（§7）才能引用到 `contracts.md` 中的接口。
