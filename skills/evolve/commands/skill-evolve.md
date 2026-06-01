---
description: 【Skill 进化】以端到端验证通过率为主指标的 Hill-Climbing 循环：需求文档 → 运行完整 create-project 流程 → sdd-validation 校准（静态检查 + mvn test + smoke_test.py）→ 通过率提升则保留 commit + 创建 PR（人工审核）→ 通过率未提升则丢弃分支。Rubric（D1-D10/F1-F4）仅作失败 TC 的诊断工具，不再是主评分指标。
---

# /skill-evolve

## 用途

给定需求文档，运行完整 create-project 命令链并执行 `sdd-validation.md` 校准链，以
**smoke_test.py 通过率**为主指标驱动 Hill-Climbing：

1. **Phase 0**：建立基线——准备需求 → 运行全流程 → 校准 → 记录 `BASELINE_PASS_RATE`
2. **Phase 1**：Hill-Climbing——分析失败 TC → 定位 skill 文档 → 修改 1 处 → 重跑校准 → 通过率提升则 keep，否则 `git revert`
3. **Phase 2**：决策门——终态通过率提升 → 创建 PR（人工审核）；未提升 → 丢弃分支

> **设计约束**：分支不能自动合并。Rubric 评分只在 TC 失败后用于定位问题，不驱动 KEEP/REVERT 决策。

---

## 用户输入

```text
$ARGUMENTS
```

**需求来源（二选一，必填）：**
- `--oss-url <url>`：开源系统 Demo URL，通过 Playwright 动态提取功能实体作为需求输入（如 `http://dashboard.yudao.iocoder.cn`）
- `--requirements <path>`：code-index 已输出的需求文档目录（含 `01-hla.md`/`02-srs.md`/`03-erd.md` 等）

**可选参数：**
- `--max-rounds N`：最多 N 轮 hill-climbing（默认 5）
- `--eval-only`：只运行 Phase 0 校准，不修改任何文件
- `--target-branch <branch>`：PR 目标分支（默认 `claude/practical-cray-ZIZ7P`）
- `--module <name>`：只测试指定功能模块（默认全部）

---

## 前置检查

1. 确认当前工作目录是 git 仓库（`git status` 不报错）
2. 确认 `skills/create-project/` 目录存在
3. 确认 Docker 可用：`docker compose ps`（需要 mysql/redis/minio 健康）
4. 确认 `--oss-url` 或 `--requirements` 至少提供一个
5. 读取当前分支名，记录为 `BASE_BRANCH`

---

## 执行步骤

### Phase 0：建立基线

**步骤 0.1 — 创建进化分支**

```bash
TIMESTAMP=$(date +%Y%m%d-%H%M)
EVOLVE_BRANCH="skill-evolve/${TIMESTAMP}"
git checkout -b ${EVOLVE_BRANCH}
```

**步骤 0.2 — 准备需求文档**

```
IF --oss-url 提供:
  → 读取 skills/evolve/references/oss-validation-guide.md
  → 执行标准访问流程（登录 → 功能发现 → 逐功能分析）
  → 提取 2-3 个含 ManyToOne 关系的功能模块（优先有状态枚举和外键选择器的模块）
  → 将提取结果转换为需求文档格式，写入 specs/evolve-${TIMESTAMP}/requirements.md：
    - 实体清单（名称、字段、类型、关系）
    - 服务清单（CRUD + 状态变更接口）
    - 用户故事（US-001：列表查询；US-002：新增；US-003：编辑；US-004：删除；US-005：状态变更）
    - 验收标准（每个 US 至少 1 条正常路径 AC + 1 条异常路径 AC）
  → PROJECT_DIR = specs/evolve-${TIMESTAMP}

IF --requirements 提供:
  → 读取指定目录下的 01-hla.md / 02-srs.md / 03-erd.md / 04-user-stories.md
  → PROJECT_DIR = --requirements 路径的父目录
```

**步骤 0.3 — 运行完整 create-project 流程**

按 `skills/create-project/references/sdd-workflow.md` 执行完整命令链，自动回答模式（不能从文档中确认的标注"待确认"，不中断流程）：

```
sdd-clarify   → 澄清歧义，写回 requirements.md
sdd-contracts → 生成 ${PROJECT_DIR}/contracts.md
sdd-spec      → 生成 ${PROJECT_DIR}/backend-spec.md + frontend-spec.md
sdd-tasks     → 生成 ${PROJECT_DIR}/tasks.md
sdd-implement → 按 tasks.md 逐个 task 生成代码（backend-model / backend-service / backend-view）
```

记录生成的文件列表为 `GENERATED_FILES`。

**步骤 0.3.5 — 生成 JSON-RPC 冒烟测试文件**

> smoke_test.py 依赖 `tests/functional/jsonrpc/*.json`，必须在运行校准前生成。

从 `${PROJECT_DIR}/requirements.md` 的用户故事（US）和验收标准（AC）生成测试文件：

对每个用户故事，创建 `tests/functional/jsonrpc/{us-id}.json`，格式如下（参考 `sdd-validation.md` §JSON-RPC 测试文件格式）：

```
规则：
- 每个 US 一个 JSON 文件，storyId = US-xxx
- 每条 AC 至少对应 1 个正常路径 case + 1 个异常路径 case
- 正常路径：expectedResult: true（期望返回 result 字段）
- 异常路径：expectedError: true（期望返回 error 字段，如必填校验失败）
- model/service/app 取自 contracts.md 中对应 US 的服务契约
- args 取自 contracts.md 的入参定义（filter/valuesList/ids/values）

US-001 列表查询 → service: search，args: {filter:[], properties:[...], limit:10}
US-002 新增      → service: create，args: {valuesList:[{...正常数据...}]}
                   + 异常 case：valuesList:[{}]（必填字段为空）
US-003 编辑      → service: update，args: {ids:["test-id"], values:{...}}
US-004 删除      → service: delete，args: {ids:["test-id"]}
US-005 状态变更  → service: <状态服务名>，args: {ids:["test-id"]}
```

确认 `tests/functional/smoke_test.py` 存在（若不存在，从 `sdd-validation.md` 中的脚本模板创建）。

记录：`SMOKE_TC_TOTAL` = 生成的 case 总数；输出文件列表。

**步骤 0.4 — 运行 sdd-validation 校准链**

读取 `skills/create-project/references/sdd-validation.md`，依次执行：

**① 静态检查（gap-taxonomy P0 级）：**

```bash
# 模型数量对比
grep -r "@Model" src/main --include="*.java" | wc -l

# 服务方法数量对比
grep -r "@MethodService" src/main --include="*.java" | wc -l

# P0 Gap 扫描
grep -r "Long id\|List<Long>" ${PROJECT_DIR}/backend-spec.md
grep -r "create_user\|create_date\|update_user\|update_date" ${PROJECT_DIR}/backend-spec.md
```

记录：`STATIC_PASS`（通过项数）/ `STATIC_TOTAL`（总检查项数）

**② 单元测试：**

```bash
mvn test -pl <模块> 2>&1 | tail -20
```

记录：`UNIT_PASS` = 绿色测试数，`UNIT_FAIL` = 失败测试数

**③ 功能冒烟测试：**

```bash
# 确认容器健康
docker compose ps

# 构建并启动
mvn package -DskipTests -q
docker compose up -d iidp-app
sleep 15  # 等待启动

# 执行冒烟测试
python tests/functional/smoke_test.py 2>&1
```

解析输出，记录：
- `SMOKE_PASS`：通过的 TC 数
- `SMOKE_TOTAL`：总 TC 数
- `FAILED_TCS`：失败的 TC 列表（含 storyId、caseName、实际响应）

**步骤 0.5 — 计算基线通过率**

```
BASELINE_PASS_RATE = SMOKE_PASS / SMOKE_TOTAL  （主指标）
BASELINE_STATIC_RATE = STATIC_PASS / STATIC_TOTAL
BASELINE_UNIT_RATE = UNIT_PASS / (UNIT_PASS + UNIT_FAIL)
```

**步骤 0.6 — 初始化记录文件**

创建本地文件（不提交 git）：

**`skill-evolve-results.tsv`**：
```
timestamp	branch	round	skill_file_changed	change_desc	smoke_before	smoke_after	delta	status	commit_short	tc_fixed	tc_broken
```

**`skill-evolve-evidence.md`**：
```markdown
# Skill 进化证据链

**分支**：${EVOLVE_BRANCH}
**需求来源**：[OSS URL 或 requirements 路径]
**基线通过率**：[SMOKE_PASS]/[SMOKE_TOTAL]（[BASELINE_PASS_RATE * 100]%）
**失败 TC 清单**：[FAILED_TCS 列表]
**开始时间**：[ISO 8601 timestamp]

---
```

**输出基线报告：**

```markdown
## 基线校准报告

**需求来源**：[oss-url / requirements 路径]
**生成文件**：[GENERATED_FILES 列表]

### 校准结果

| 层级 | 通过 | 总数 | 通过率 |
|---|---|---|---|
| 静态检查（P0） | [STATIC_PASS] | [STATIC_TOTAL] | [%] |
| 单元测试 | [UNIT_PASS] | [UNIT_PASS+UNIT_FAIL] | [%] |
| 功能冒烟测试 | [SMOKE_PASS] | [SMOKE_TOTAL] | **[BASELINE_PASS_RATE*100]%** |

### 失败 TC（需要修复）

| TC-ID | 用户故事 | 失败原因摘要 |
|---|---|---|
| [TC-xxx] | [US-xxx] | [1句：实际响应 vs 期望] |
...
```

[🔴 **CHECKPOINT**]：展示基线报告，等待用户确认后进入 Phase 1。
若用户指定了 `--eval-only`，到此结束。

---

### Phase 1：Hill-Climbing 修复循环

**初始化**：
```
CURRENT_PASS_RATE = BASELINE_PASS_RATE
CURRENT_FAILED_TCS = FAILED_TCS
CONSECUTIVE_NO_IMPROVE = 0
ROUND = 0
MAX_ROUNDS = 5（或用户指定值）
```

**LOOP（ROUND < MAX_ROUNDS 且 CURRENT_FAILED_TCS 非空）**：

#### 步骤 1.1 — 诊断：失败 TC → 对应 skill 文档

分析 `CURRENT_FAILED_TCS` 中影响最多 TC 的失败根因，使用 `skill-rubric.md`（D1-D10/F1-F4）**作为诊断工具**定位问题所在的 skill 文档：

```
失败模式 → skill 文档定位：

生成的 backend-spec 字段类型错误（如 Long id）
  → D2 字段规范性 → sdd-backend.md §3 模型设计表

生成的 backend-spec 缺少 ManyToOne FK String 字段
  → D6 ER 关系设计 → sdd-backend.md §3 ManyToOne 规则

生成的 contracts.md 服务方法签名与 backend-spec 不一致
  → D8 跨步骤一致性 → sdd-contracts.md 或 sdd-backend.md

生成的 backend-spec 有 create_user/update_date 字段
  → D2 字段规范性 → sdd-backend.md §3 审计字段规则

生成的服务缺少状态变更方法（approve/reject/submit）
  → D9 测试可验性 → sdd-backend.md §4 状态机服务

生成的代码使用 Spring @Service 而非 @MethodService
  → D3 平台合规性 → sdd-backend.md §4 服务注解规范

生成的 frontend-spec 直接写 Vue2 组件跳过 hook
  → F1 实现分支合规性 → sdd-frontend.md §9 决策链

生成的 frontend-spec 节点 id 无来源标注
  → F2 节点规范性 → sdd-frontend.md §6

生成的 frontend-spec 数据源使用 axios
  → F3 数据源规范 → sdd-frontend.md §10
```

输出本轮诊断：
```
本轮目标：[失败 TC 数量] 个失败 TC，影响最大的根因：[1句]
定位维度：[D/F 编号]（[维度名]）
对应文件：skills/create-project/references/[filename].md §[节号]
修改思路：[1-2句：当前 skill 文档写法是什么，导致 AI 生成了什么错误，要改成什么]
预计修复 TC：[TC-ID 列表]
```

#### 步骤 1.2 — 修改对应 skill 文档（1 处）

**约束**：
- 只修改 1 个文件中的 1 处（保证因果链清晰）
- 修改必须是对 AI 行为有直接影响的内容：强化示例、明确禁令、补充规则、修正错误写法
- 不得修改测试文件、需求文档、生成的规格文件

**改前快照**：用 Read 工具读取目标位置前后 15 行，写入 `skill-evolve-evidence.md`：

```markdown
## 第 [ROUND] 轮

### 失败 TC
[CURRENT_FAILED_TCS 中本轮目标 TC 列表 + 失败原因]

### 诊断
根因：[1句]  定位：[维度] → [文件 §节]

### 改前（before）
文件：`[filename]`，位置：§[节] 第 [N] 行附近

\`\`\`
[改前原文 10-15 行]
\`\`\`

### 改动理由
[skill 文档的哪个写法导致 AI 生成了错误，修改后如何引导正确行为]
```

应用修改（Edit 工具），将改后内容追加到 evidence：

```markdown
### 改后（after）
\`\`\`
[改后对应段落]
\`\`\`
```

提交：
```bash
git add [修改的文件]
git commit -m "evolve: fix [D/F编号] - [一句话描述改动对 AI 行为的影响]"
COMMIT_HASH=$(git rev-parse --short HEAD)
```

#### 步骤 1.3 — 重新运行 create-project 全流程 + 校准

**重要**：必须重新运行完整流程，不能只重跑测试——skill 文档修改后 AI 的输出会变化，需要重新生成规格和代码。

```
sdd-contracts → sdd-spec → sdd-tasks → sdd-implement（仅重生成受影响的模块）
→ 静态检查 → mvn test → smoke_test.py
```

解析新的测试结果：
- `NEW_PASS_RATE = NEW_SMOKE_PASS / SMOKE_TOTAL`
- `NEW_FAILED_TCS`：本轮仍然失败的 TC

#### 步骤 1.4 — Ratchet 决策

```
DELTA = NEW_PASS_RATE - CURRENT_PASS_RATE

IF DELTA > 0:
  → KEEP（保留 commit）
  → CURRENT_PASS_RATE = NEW_PASS_RATE
  → CURRENT_FAILED_TCS = NEW_FAILED_TCS
  → TC_FIXED = 本轮新通过的 TC 列表
  → TC_BROKEN = 本轮新失败的 TC 列表（回归）
  → 记录到 results.tsv
  → CONSECUTIVE_NO_IMPROVE = 0

ELSE（DELTA <= 0）:
  → REVERT
  → git revert HEAD --no-edit
  → CURRENT_PASS_RATE 不变
  → 记录到 results.tsv（status=revert）
  → CONSECUTIVE_NO_IMPROVE += 1
```

将本轮结果追加到 `skill-evolve-evidence.md`：

```markdown
### 重跑结果

| 层级 | 改前 | 改后 | Δ |
|---|---|---|---|
| 冒烟通过率 | [CURRENT]% | [NEW]% | [DELTA*100]% |
| 新修复 TC | — | [TC_FIXED] | — |
| 新回归 TC | — | [TC_BROKEN] | — |

### 决策：KEEP ✅ / REVERT ❌
原因：[通过率变化说明]

---
```

[🔴 **CHECKPOINT**]：展示本轮结果，等待用户确认继续：

```markdown
## 第 [ROUND] 轮结果

**修改文件**：[filename] §[节] — [1句描述]
**通过率变化**：[BEFORE]% → [NEW]%（Δ = [DELTA*100]%）
**决策**：[KEEP ✅ / REVERT ❌]
**修复的 TC**：[列表]
**新回归 TC**：[列表（如有）]
```

#### 步骤 1.5 — Early Stop 检查

```
IF CONSECUTIVE_NO_IMPROVE >= 2:
  → 停止（连续 2 轮无提升，局部最优）

IF CURRENT_FAILED_TCS 为空:
  → 停止（所有 TC 已通过，任务完成）

IF ROUND >= MAX_ROUNDS:
  → 停止（达到最大轮数）
```

**ROUND += 1，继续循环**

---

### Phase 2：决策门

**步骤 2.0 — 终态完整校准（强制，不可跳过）**

> Hill-Climbing 每轮只重跑受影响模块，可能存在漏测。必须在创建 PR 前重新运行完整流程 + 完整校准，以终态通过率作为 PR 的唯一数字依据。

```bash
# 完整重跑
sdd-contracts → sdd-spec → sdd-tasks → sdd-implement（全量）
→ 静态检查（全量）→ mvn test → smoke_test.py（全量）
```

计算：`FINAL_PASS_RATE = FINAL_SMOKE_PASS / SMOKE_TOTAL`
`CONFIRMED_DELTA = FINAL_PASS_RATE - BASELINE_PASS_RATE`

将终态结果追加到 `skill-evolve-evidence.md`：

```markdown
## 终态校准验证（Phase 2 门控）

| 层级 | 基线 | 终态 | Δ |
|---|---|---|---|
| 静态检查通过率 | [%] | [%] | [%] |
| 单元测试通过率 | [%] | [%] | [%] |
| 冒烟测试通过率 | [BASELINE_PASS_RATE*100]% | [FINAL_PASS_RATE*100]% | **[CONFIRMED_DELTA*100]%** |

**通过的 TC**：[列表]
**仍失败的 TC**：[列表]
```

**门控判断**：
- `CONFIRMED_DELTA > 0` → 情况 A（创建 PR）
- `CONFIRMED_DELTA <= 0` → 情况 B（丢弃分支）

---

#### 情况 A：终态通过率提升

```bash
git push -u origin ${EVOLVE_BRANCH}
```

PR 内容：

```
标题：skill-evolve: smoke +[CONFIRMED_DELTA*100]% ([BASELINE]%→[FINAL]%) - [主要改动摘要]

正文：
## 终态校准结果

| 层级 | 基线 | 终态 | Δ |
|---|---|---|---|
| 冒烟通过率 | [BASELINE]% | [FINAL]% | +[DELTA]% |
| 静态检查 | [%] | [%] | [%] |
| 单元测试 | [%] | [%] | [%] |

**需求来源**：[OSS URL / requirements 路径]

## 修复的失败 TC

| TC-ID | 用户故事 | 根因维度 | 修改的 skill 文档 |
|---|---|---|---|
| [TC-xxx] | [US-xxx] | [D/F编号] | [filename §节] |
...

## 仍失败的 TC（需后续处理）
[列表，如有]

## 证据链（逐轮 Before → After，仅 KEEP 轮次）

### 第 [N] 轮：[D/F编号] [维度名]（+[Δ]%）

**失败 TC**：[列表]
**根因**：[诊断结论 1句]

**改前**（`[filename]` §[节]）：
\`\`\`
[BEFORE_SNAPSHOT]
\`\`\`

**改后**：
\`\`\`
[改后段落]
\`\`\`

**效果**：冒烟通过率 [before]% → [after]%，修复 TC：[列表]

---

## ⚠️ 注意事项
此 PR 需要人工审核后才能合并，不允许自动合并。
请检查每次 commit 的 diff，确认修改符合 IIDP 平台规范。

<details>
<summary>📋 完整证据文档</summary>
[skill-evolve-evidence.md 全文]
</details>
```

输出 PR URL，等待人工审核。

#### 情况 B：终态通过率未提升

```bash
git checkout ${BASE_BRANCH}
git branch -D ${EVOLVE_BRANCH}
```

输出原因报告：

```markdown
## 进化循环结束 — 分支已丢弃

**原因**：经过 [ROUND] 轮修复，冒烟通过率未提升
（基线 [BASELINE_PASS_RATE*100]% → 终态 [FINAL_PASS_RATE*100]%，Δ = [CONFIRMED_DELTA*100]%）

**尝试过的修改**：
[每轮 revert 的 skill 文档位置 + 改动描述 + 未生效原因]

**仍失败的 TC 及推断根因**：
[列表，含对应的 skill 文档位置建议]

**建议**：
- 使用 --eval-only 重新查看当前基线，确认失败 TC 的根因
- 考虑调整需求场景（--oss-url 换其他模块）排除需求本身的歧义
- 对仍失败的根因进行更大范围的 skill 文档重构，再启动新一轮进化
```

---

## 完成标志

**情况 A（通过率提升）**：
- Phase 2 终态完整校准已完成（全量流程 + smoke_test.py）
- PR 标题中的通过率来自终态校准，不是 Hill-Climbing 累计估算值
- PR 已创建，URL 已输出
- `skill-evolve-results.tsv` 记录所有轮次数据
- `skill-evolve-evidence.md` 包含：失败 TC → 诊断 → before/after → 通过率变化
- 未自动合并（等待人工审核）

**情况 B（通过率未提升）**：
- 进化分支已本地删除
- 工作区已回到 `BASE_BRANCH`
- 原因报告已输出，含失败 TC 根因和下一步建议
