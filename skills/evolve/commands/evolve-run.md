---
description: 【Evolve Pipeline】完整进化流水线：读取 code-index 规格书 → 调用 create-project 生成 IIDP 应用 → OSS 验证 → Gap 修复循环。编排 evolve-oss-validate + skill-evolve 两个子命令。
---

# /evolve-run

## 用途

端到端自动化进化流水线：

```
code-index 规格书输出
  → 调用 create-project 生成 IIDP 应用规格/代码
  → evolve-oss-validate 对比开源系统验证合规性
  → skill-evolve 对 create-project skill 文档评分修复
  → 创建 PR（人工审核）
```

## 用户输入

```text
$ARGUMENTS
```

必填：
- `--project <name>`：项目名（建立 `specs/<name>/` 目录）
- `--requirements <path>`：code-index 输出的规格书目录（含 01-hla.md, 02-srs.md 等）

可选：
- `--oss-url <url>`：开源系统 Demo URL（用于合规验证，不提供则跳过 Phase 2）
- `--skip-implement`：跳过代码生成，只运行验证和评分（已有生成结果时使用）
- `--max-rounds N`：skill 进化最大轮数（默认 3）
- `--auto`：自动模式（减少 checkpoint，仅在明确错误时暂停）

---

## 执行流程

### Phase 0：初始化

**0.1 — 创建项目目录结构**

```bash
PROJECT_DIR="specs/<name>"
mkdir -p ${PROJECT_DIR}/{backend,frontend,oss-screenshots}
```

**0.2 — 读取 code-index 规格书**

读取以下文件（存在则读，不存在则跳过）：
- `<requirements>/01-hla.md` → 提取：模块列表、核心服务、ER 概览
- `<requirements>/02-srs.md` → 提取：功能需求（FR 列表）
- `<requirements>/03-erd.md` → 提取：实体定义、字段、关系
- `<requirements>/04-user-stories.md` → 提取：用户故事（US 列表）
- `<requirements>/05-api.md` → 提取：接口清单（API 列表）

输出摘要：
```
已读取规格书：[N] 个文件
模块清单：[模块1, 模块2, ...]
实体清单：[实体1, 实体2, ...]（含关系类型）
接口清单：[N] 个 API
```

**0.3 — 初始化进度记录**

创建 `${PROJECT_DIR}/evolve-progress.md`：
```markdown
# 进化进度记录

**项目**：<name>  
**启动时间**：[timestamp]  
**规格书路径**：<requirements>  

## Phase 状态

- [ ] Phase 0：初始化
- [ ] Phase 1：SDD 规格生成
- [ ] Phase 2：OSS 验证
- [ ] Phase 3：Skill 进化
- [ ] Phase 4：总结报告
```

[🔴 **CHECKPOINT**]：展示初始化摘要，确认项目配置后继续。

---

### Phase 1：SDD 规格生成（调用 create-project 命令链）

> 如果用户指定了 `--skip-implement`，跳过本 Phase。

**1.1 — 生成 requirements.md**

基于 `02-srs.md` 和 `04-user-stories.md` 的内容，以 create-project 的 `requirements.md` 格式写入 `${PROJECT_DIR}/requirements.md`。

格式参考：`skills/create-project/commands/sdd-specify.md` 的输出模板。

**1.2 — 运行 sdd-clarify（自动回答模式）**

规则：
- 能从 code-index 文档中找到答案的问题 → 自动填写
- 不确定的问题 → 标注"待确认"，继续进行

**1.3 — 运行 sdd-contracts**

读取：`skills/create-project/references/sdd-contracts.md`  
输出：`${PROJECT_DIR}/contracts.md`

**1.4 — 运行 sdd-spec**

读取：
- `skills/create-project/references/sdd-backend.md`
- `skills/create-project/references/sdd-frontend.md`

输出：
- `${PROJECT_DIR}/backend-spec.md`
- `${PROJECT_DIR}/frontend-spec.md`

**1.5 — 运行 sdd-plan + sdd-tasks**

输出：`${PROJECT_DIR}/tasks.md`（分解为 task block 列表）

**1.6 — 运行 sdd-implement（循环）**

对 `tasks.md` 中每个 task block 执行：
1. 读取 task 内容
2. 按 task 类型生成代码/规格（backend-model / backend-service / backend-view / frontend-page）
3. 写入对应文件

[🔴 **CHECKPOINT**]：每完成 3 个 task 暂停，展示进度（已完成 N/M 个 task）。

**1.7 — 快速验证（Gap 扫描）**

对生成的 `backend-spec.md` 执行 gap-taxonomy.md 的 P0 级检查：
- G1-1：搜索 `Long id` / `List<Long>` → 有则标注
- G1-2：搜索 `create_user` / `create_date` → 有则标注
- G1-3：检查每个 ManyToOne 是否有配对的 FK String 字段

输出：`Phase 1 Gap 预扫描结果：发现 [N] 个 P0 级 Gap`

---

### Phase 2：OSS 验证（可选）

**进入条件**：用户提供了 `--oss-url`

调用方式：

```
执行 /evolve-oss-validate 命令的所有步骤，参数：
  --oss-url <oss-url>
  --project-path ${PROJECT_DIR}
  --output ${PROJECT_DIR}/oss-validation-report.md
```

输出：
- `oss-validation-report.md`（含 10 维合规分）
- `OSS_COMPLIANCE_SCORE`（0-100）
- Gap 清单（G1-G8 分类）

[🔴 **CHECKPOINT**]：展示合规报告总分和主要 Gap，确认是否继续进行 Skill 进化。

若没有 `--oss-url`，跳过本 Phase，`OSS_COMPLIANCE_SCORE = null`。

---

### Phase 3：Skill 进化循环

调用方式：

```
执行 /skill-evolve 命令的所有步骤，参数：
  --max-rounds <max-rounds>（默认 3）
  --target-branch claude/practical-cray-ZIZ7P
```

输入参考：
- Phase 1 Gap 扫描结果（用于 Phase 0 基线评分的优先级）
- Phase 2 OSS 合规报告中的 Gap 清单（额外输入给法官 sub-agent）

每轮 hill-climbing 的 CHECKPOINT 照常执行（见 skill-evolve.md）。

---

### Phase 4：总结报告

生成 `${PROJECT_DIR}/evolve-summary.md`：

```markdown
# 进化总结报告

**项目**：<name>  
**完成时间**：[timestamp]  

## Phase 1：SDD 生成结果

- 任务总数：[N] 个 task block
- 已完成：[M] 个
- P0 Gap（预扫描）：[N] 个

## Phase 2：OSS 合规验证

- 目标系统：[oss-url 或 "跳过"]
- 合规总分：[OSS_COMPLIANCE_SCORE 或 "N/A"]
- 主要 Gap：[Gap 清单]

## Phase 3：Skill 进化结果

- 评分变化：[BASELINE] → [CURRENT]（+[DELTA]pt）
- 修复的维度：[D列表]
- PR 状态：[PR URL 或 "未创建（分数未提升）"]

## 产出物清单

- [ ] `${PROJECT_DIR}/backend-spec.md`
- [ ] `${PROJECT_DIR}/frontend-spec.md`
- [ ] `${PROJECT_DIR}/contracts.md`
- [ ] `${PROJECT_DIR}/tasks.md`
- [ ] `${PROJECT_DIR}/oss-validation-report.md`（若有）
- [ ] `${PROJECT_DIR}/evolve-progress.md`

## 待手动处理事项

[列出未自动修复的 Gap 和建议]
```

---

## 完成标志

- `evolve-summary.md` 已写入
- Phase 1 生成的规格书文件存在且非空
- Phase 3 Skill 进化结果已记录（PR URL 或分支丢弃说明）
- 用户收到完整总结
