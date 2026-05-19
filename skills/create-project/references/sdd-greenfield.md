# IIDP 全新工程接入

## 何时使用全新工程流程

以下场景使用本指南，而非存量接入流程：

| 场景 | 说明 |
|---|---|
| 工作区尚无 IIDP 父工程 | 本地目录下没有 `apps/apps.json`、`settings.xml` 或 IIDP 聚合 POM |
| 从零搭建业务系统 | 全新立项，没有任何可复用的现有模型、视图或菜单 |
| POC / 原型快速验证 | 需要一个干净的 IIDP 工程跑通关键能力，不基于现有代码 |
| 新增独立 App 包（工程已就位）| 父工程存在，但要新增一个与现有 App 无依赖关系的全新业务 App |

如果工程已存在、只是新增模块或改造已有功能，使用 `sdd-brownfield.md`。

---

## 六步全新工程工作流

```text
步骤 1：初始化父工程
  → 克隆仓库、检查环境、首包验证（BUILD SUCCESS 门控）

步骤 2：建立项目宪法
  → 产出 mission.md / iidp-stack.md / roadmap.md / integration-map.md

步骤 3：编写需求规格与契约
  → 产出 requirements.md / contracts.md（先于规格书，强制前后端对齐）

步骤 4：生成规格书
  → 产出 backend-spec.md / frontend-spec.md / interaction-spec.md

步骤 5：拆解实施任务
  → 产出 tasks.md / validation.md

步骤 6：执行实施
  → 按任务清单生成后端代码、视图、菜单、前端扩展
```

---

## 步骤详情

### 步骤 1：初始化父工程

**目的**：在本地搭建可构建的 IIDP 父工程，通过首包验证后才进入业务开发。

**执行**：读取并遵循 `skills/backend/greenfield/SKILL.md` 的前置流程（Git 克隆、JDK/Maven 检查、settings.xml、首包构建）。本步骤不重复其内容。

**首包门控**：

```text
构建结果为 BUILD SUCCESS，且无编译错误
  → 通过门控，进入步骤 2

构建失败
  → 按 backend/greenfield/SKILL.md 常见错误表排查
  → 未通过门控，不得进入步骤 2
```

**步骤 1 产出摘要**：

```markdown
## 父工程初始化完成 ✓

### 工程状态
- 工程路径：[本地路径]
- JDK 版本：[版本号]
- 构建结果：BUILD SUCCESS / 失败（原因：）
- 首包状态：通过 / 阻塞（环境原因：）

### 下一步选项
A) 进入步骤 2，建立项目宪法
B) 先配置 Docker Compose 本地运行环境
C) 记录环境阻塞原因，暂缓进入步骤 2
```

---

### 步骤 2：建立项目宪法

**目的**：在业务开发前确立项目的命名规范、技术栈约束、App 规划和路线图，避免后期大规模改动。

**执行**：读取 `references/sdd-constitution.md`，产出以下文件：
- `specs/mission.md`：项目定位、目标用户、核心价值
- `specs/iidp-stack.md`：技术栈约束、appPkg 命名规则、模型规范、前端框架版本
- `specs/roadmap.md`：Phase 规划、App 优先级、里程碑
- `specs/integration-map.md`：App 清单（含 appName / appPkg / 依赖顺序）

**交互示例**：

```text
AI：根据项目描述，我提案以下核心约束：
    - appPkg 命名：com.sie.iidp.{业务域}
    - 所有模型继承 BaseModel<T>，不使用 Spring 原生注解
    - 前端使用 IIDP 标准模板，仅在标准能力不足时写扩展视图
    请确认以上约束是否准确？是否有其他技术债或合规要求需要写入宪法？

用户：appPkg 改为 com.example.order，其他正确。

AI：了解，更新 appPkg 命名规则，并在 iidp-stack.md 中记录命名决策。
    接下来确认 App 清单和 Phase 规划……
```

**步骤 2 产出摘要**：

```markdown
## 项目宪法已生成 ✓

### 关键决策
1. **appPkg 命名规则**：[规则] — 后续所有 App 必须遵循
2. **App 清单**：[App 数量] 个，依赖顺序：[App1] → [App2] → …
3. **Phase 规划**：Phase 1 目标 [描述]，预计 [时间]

### 产出文件
- `specs/mission.md`
- `specs/iidp-stack.md`
- `specs/roadmap.md`
- `specs/integration-map.md`

### 待确认项
- [推断项，标注来源]

### 后续选项
A) 进入步骤 3，编写第一个 App 的需求规格
B) 细化 App 清单（补充模型数量和服务范围估算）
C) 调整 Phase 规划优先级
```

---

### 步骤 3：编写需求规格与契约

**目的**：明确本次 Phase 要做什么（requirements.md），并在写规格书前确定前后端契约（contracts.md），强制前后端对齐。

**执行**：读取 `references/sdd-workflow.md` Step 1.1–1.3a，产出：
- `specs/features/phase{N}-{feature}/requirements.md`
- `specs/features/phase{N}-{feature}/contracts.md`（先于 backend-spec，强制步骤）

**步骤 3 产出摘要**：

```markdown
## 需求规格与契约已生成 ✓

### 功能清单
- [功能点 1]（用户故事数：X，验收标准数：X）
- [功能点 2]

### 契约关键条目
| appName | model_name | service | auth | 前端 args |
|---|---|---|---|---|
| [值] | [值] | [值] | [值] | [值] |

### 待确认项
- [节点 id / 权限码 / 数据源名，标注待确认原因]

### 后续选项
A) 进入步骤 4，生成规格书
B) 补充 Clarify 规格澄清（requirements 中有模糊项时）
C) 调整契约表
```

---

### 步骤 4：生成规格书

**目的**：将需求规格和契约转化为可执行的技术落地规格，供步骤 6 代码生成使用。

**执行**：
- 后端规格：读取 `references/sdd-backend.md`，产出 `backend-spec.md`
- 前端规格：读取 `references/sdd-frontend.md`，判断实现分支后产出 `frontend-spec.md`（仅需要前端代码时）
- 交互规格：含复杂状态机/响应式/可访问性时，读取 `references/sdd-frontend-interaction.md`，产出 `interaction-spec.md`

**步骤 4 产出摘要**：

```markdown
## 规格书已生成 ✓

### 后端规格概览（backend-spec.md）
- 模型：[数量] 个（[ModelName1], [ModelName2], …）
- 服务：[数量] 个（内置重写 X，自定义 X）
- 需 Service 分层：[是 / 否]（触发条件：[描述]）
- 视图：[数量] 个，菜单：[数量] 个

### 前端规格概览
- 实现分支：标准模板 / 扩展视图 / 自定义 Vue2 组件
- 是否生成 frontend-spec.md：[是 / 否]（原因：）

### 待确认项
- [列出 backend-spec 中标注「待确认」的项]

### 后续选项
A) 进入步骤 5，拆解实施任务
B) 补全待确认项后再继续
C) 审查规格书一致性
```

---

### 步骤 5：拆解实施任务

**目的**：将规格书拆解为可逐项执行的原子任务，并同步生成测试用例规格。

**执行**：读取 `references/sdd-workflow.md` Step 3，产出：
- `specs/features/phase{N}-{feature}/tasks.md`
- `specs/features/phase{N}-{feature}/validation.md`（从 AC 提取 TC-BE / TC-FE）

**步骤 5 产出摘要**：

```markdown
## 任务清单已生成 ✓

### 任务概览
- 任务总数：X 个（后端 X，前端 X，集成验证 X）
- 关键路径：[依赖顺序最长的任务链]
- 高复杂度任务：[列出需要 Service 分层或跨 App 协调的任务]
- 预估工期：[天数]

### 后续选项
A) 进入步骤 6，开始实施
B) 重新排序任务优先级
C) 补充验收标准
```

---

### 步骤 6：执行实施

**目的**：按任务清单逐项生成后端代码、视图、菜单配置和前端扩展代码。

**执行**：
- 读取 `references/sdd-workflow.md` Step 4 的 Implement 规则
- 后端代码由 `skills/backend/SKILL.md` Step 1–10 生成
- 前端代码由 `skills/frontend` 对应子 skill 生成
- 每个任务完成后回到 `tasks.md` 勾选，再启动下一个任务

**Git 分支门控**（与 `create-project/SKILL.md` 一致）：进入本步骤前检查当前分支，若在主干则暂停提示用户创建 feature 分支。

---

## 多 App 规划

全新工程通常包含多个 App，在**步骤 2 宪法阶段**就应完成 App 规划，避免后期命名冲突和依赖循环。

**在 `integration-map.md` 中提前规划**：

```markdown
## App 清单（初始规划）

| appName | appPkg | 职责 | 依赖 App | Phase |
|---|---|---|---|---|
| [App1] | com.example.[app1] | [职责描述] | 无 | Phase 1 |
| [App2] | com.example.[app2] | [职责描述] | [App1] | Phase 1 |
| [App3] | com.example.[app3] | [职责描述] | [App1], [App2] | Phase 2 |
```

**每个 App 独立走步骤 3–6**，按依赖顺序排期——被依赖的 App 先完成。

**Feature 进度追踪**：

```text
当前 Phase 进度

正在进行
  [App1]-[功能名]（步骤 4 规格书生成中，60% 完成）
  依赖：无

已完成
  [App1]-基础模型（100%）

待开始
  [App2]-[功能名]（依赖 [App1]-基础模型 完成）
  [App3]-[功能名]（依赖 [App2] 完成）
```

---

## 产物清单

每步产出标注如下：

```text
specs/
├── mission.md                          ← 步骤 2
├── iidp-stack.md                       ← 步骤 2
├── roadmap.md                          ← 步骤 2
├── integration-map.md                  ← 步骤 2（后续每步完成后更新）
├── decisions.md                        ← 步骤 2 起，每次关键决策后更新
└── features/
    └── phase1-{feature}/
        ├── requirements.md             ← 步骤 3
        ├── contracts.md                ← 步骤 3
        ├── backend-spec.md             ← 步骤 4
        ├── frontend-spec.md            ← 步骤 4（需要前端代码时）
        ├── interaction-spec.md         ← 步骤 4（含复杂状态/响应式时）
        ├── plan.md                     ← 步骤 4（含技术决策）
        ├── tasks.md                    ← 步骤 5
        └── validation.md              ← 步骤 5
```

---

## 全新工程最佳实践

1. **先跑通首包再写业务**
   步骤 1 的 BUILD SUCCESS 是硬门控，不通过不进入步骤 2。工程环境问题留到业务开发阶段会放大排查成本。

2. **宪法阶段就定好 appPkg 命名规则和 App 边界**
   `appPkg` 一旦投入生产就极难修改（影响数据库表名、jar 包路径、前端 App 配置）。在步骤 2 宪法阶段和团队对齐，记录在 `iidp-stack.md` 和 `decisions.md`。

3. **contracts.md 先于 backend-spec**
   步骤 3 强制先写 `contracts.md`，再进步骤 4 写规格书。前后端契约对齐后，`backend-spec` 的服务签名、auth 值、视图 key 不得偏离已定义的契约。

4. **每个 App 完整走完步骤 3–6 再开下一个**
   不要所有 App 一起写需求、一起写规格、一起实施。按依赖顺序串行，每个 App 完成后更新 `integration-map.md`，再解锁依赖它的下一个 App。

5. **验收标准写在 validation.md，不要口头约定**
   步骤 5 从 requirements.md 的 AC 提取 TC-BE / TC-FE，写入 `validation.md`。实施完成后按清单逐项验收，不以"跑起来了"为完成标准。

---

## 常见场景

### 场景 1：从零搭建单 App 业务系统

适用深度：完整六步，无跳过。

关键注意：
- 步骤 2 宪法阶段只规划单个 App，integration-map.md 保持最小结构
- 步骤 3 的 contracts.md 需明确前端是否需要扩展视图（影响步骤 4 是否生成 frontend-spec.md）
- 步骤 6 按 `backend/SKILL.md` Step 1–10 顺序执行：命名 → 模型 → 视图 → 菜单 → 数据

### 场景 2：搭建多 App 平台

适用深度：完整六步 × App 数量，串行执行。

关键注意：
- 步骤 2 宪法阶段完成所有 App 的清单和依赖顺序规划
- 依赖链上的 App 必须先完成步骤 6（代码生成并通过验收）才能解锁后续 App 的步骤 3
- 跨 App 共享服务在被依赖 App 的 contracts.md 中定义，不在依赖方重复声明
- 每个 App 完成后更新 integration-map.md 的 App 清单和关键约束

### 场景 3：POC / 原型快速验证

适用深度：简化六步，部分文档可简化。

简化规则：
- 步骤 2：宪法只写 mission.md 和 iidp-stack.md，跳过 roadmap.md
- 步骤 3：requirements.md 只写核心用户故事，contracts.md 必须保留
- 步骤 4：跳过 interaction-spec.md，frontend-spec.md 只写关键节点
- 步骤 5：tasks.md 可合并任务，validation.md 只写关键验收点
- 步骤 6：正常执行，不因 POC 降低代码规范

POC 结束后若决定转正式开发，补全跳过的规格文档再进入下一 Phase。

---

## 常见问题排查

| 问题 | 可能原因 | 排查方式 |
|---|---|---|
| 步骤 1 首包失败 | JDK 版本不符、settings.xml 未指定、私服不可达 | 参考 `backend/greenfield/SKILL.md` 常见错误表 |
| 宪法阶段 appPkg 与现有工程冲突 | 团队已有其他 IIDP 工程使用相同 appPkg | 检查已注册的 appPkg 清单；appPkg 全局唯一，需协商后修改 |
| 多 App 依赖顺序导致构建失败 | App B 依赖 App A 的模型，但 App A 未先完成 | 按 integration-map.md 中的依赖顺序串行构建；在 `app.json` 的 `dependencies` 中显式声明 |
| contracts.md 与 backend-spec.md 不一致 | 步骤 4 规格书生成时未以 contracts.md 为基准 | 以 contracts.md 为准，重新对齐 backend-spec.md 的 service name、auth、args 类型 |
