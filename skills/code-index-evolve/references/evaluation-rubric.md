# 评分 Rubric — Codebook 质量评分

## 评分总览

总分：100 分。评分前必须先通过合规门禁，门禁不通过直接 REVERT，不进入评分。

本 Rubric 的目标是评估 **code-index skill 修改是否能提高 Codebook 生成质量**。
`接口文档完整性` 和 `可追溯性与无幻觉` 是主权重；Agent 评审（核心文档 + 安全合规 + 性能）
验证文档多视角规范性；变量完整性和文件完整性是底线门控。

| 维度 | 分值 | 衡量内容 |
|---|---:|---|
| 接口文档完整性 | 30 | api.md 接口数 vs ground truth；实体/模块覆盖率 |
| 可追溯性与无幻觉 | 20 | 文档声明能否回溯到源码；无虚构字段/错误码 |
| ③a 核心文档评审 | 15 | reality-checker(hla) + api-tester(api) + product-manager(prd) |
| ③b 安全与合规审查 | 10 | appsec(api安全) + sec-architect(hla安全架构) + compliance(db+prd合规) |
| ④ 性能规格完整性 | 5 | performance-benchmarker 对 srs.md + hla.md 的性能指标评审 |
| 变量采集完整性 | 10 | 生成文档中无残留 `{{...}}` 占位符 |
| 文件完整性 | 10 | .progress.md 全 [x]；所有必须输出文件存在 |

---

## 合规门禁（一票否决）

打分前先执行以下检查。**任意一项不通过，本轮直接 REVERT，不进入评分。**

| 检查项 | 检查方式 | 判定 |
|---|---|---|
| Phase C 采集未完成 | api.md 中接口章节数为 0 | REVERT |
| Prompt 变量原样输出 | 任意生成 .md 文件中含 `{{ENDPOINT_LIST}}` 原文 | REVERT |
| .progress.md 未生成 | codebook/.progress.md 不存在 | REVERT |
| 系统级 hla.md 未生成 | codebook/hla.md 不存在 | REVERT |

```bash
# 快速检查脚本（在 codebook/ 目录下执行）
grep -r '{{ENDPOINT_LIST}}' . --include="*.md" | wc -l   # 应为 0
ls hla.md overview.md database-overview.md .progress.md  # 应全部存在
find modules/ -name "api.md" -exec grep -l "^### " {} \; | wc -l  # 应 > 0
```

---

## 门禁级联规则

- Phase C 完全跳过（ENDPOINT_LIST 为空）→ 接口文档完整性记 0 分，且可追溯性最高 8 分
- 生成文档中残留 `{{...}}` 超过 5 处 → 变量采集完整性记 0 分
- 某模块缺少 api.md → 该模块接口完整性不参与计算，但不影响其他模块
- 评测只能比较同一基准仓库（相同 commit hash）和同一本地环境下产生的分数

---

## 维度细则

### ① 接口文档完整性：30 分

衡量生成 Codebook 对源码中接口的覆盖程度。使用 `baseline.json` 中的 ground truth。

**计算方式：**

```
# API 覆盖率（12分）
actual_api = 所有模块 api.md 中独立接口章节数（grep "^### " 计数）
gt_api     = baseline.json.groundTruth.totalApiCount
api_score  = round(12 × min(actual_api / gt_api, 1.0))

# 实体覆盖率（8分）
actual_entity = 所有模块 database.md 中表格数（grep "^### " 或 "^## 表" 计数）
gt_entity     = baseline.json.groundTruth.totalEntityCount
entity_score  = round(8 × min(actual_entity / gt_entity, 1.0))

# 模块划分准确率（6分）
actual_modules = codebook/modules/ 下子目录名称集合
gt_modules     = baseline.json.groundTruth.modules
match_rate     = |actual_modules ∩ gt_modules| / |gt_modules|
module_score   = round(6 × match_rate)

# SRS 完整性（4分）
对每个模块：srs.md 中功能需求条目数是否 == api.md 接口章节数
  全部匹配 → 4 分；一半匹配 → 2 分；均不匹配 → 0 分
```

**扣分示例：**
- ground truth 47 个接口，api.md 只有 31 个 → api_score = round(12 × 31/47) = 8 分
- ground truth 3 个模块，只生成 2 个 → module_score = round(6 × 2/3) = 4 分

---

### ② 可追溯性与无幻觉：20 分

衡量生成文档是否基于真实源码事实，没有凭空推断的字段、接口或错误码。

**抽查执行步骤：**

```
步骤 1：从 api.md 字段校验矩阵中随机抽取 10 个字段（跨多个接口）
步骤 2：对每个字段：
  a. 定位来源 VO/Entity 类名（来自字段章节标注或上下文推断）
  b. codegraph_search("{类名}") → 获取文件路径
  c. Read(文件路径) → 确认该字段名存在于源码中
步骤 3：记录：字段名 / 声明文件路径 / 实际文件路径 / MATCH 或 MISMATCH

步骤 4：从 error-codes.md 抽取 5 个错误码（错误码编号 + 常量名）
步骤 5：对每个错误码：
  codegraph_search("{常量名}") → Read(错误码文件) → 确认编号和常量名与源码一致

步骤 6：检查 api.md 中是否存在未在源码中找到的虚构错误码
```

**评分细则：**

- 10 个字段验证（每字段 1 分，共 10 分）：
  - MATCH（字段存在于源码）→ 1 分
  - MISMATCH（字段不在源码中）→ 0 分
  - 来源文件找不到 → 0.5 分（可能是 codegraph 索引问题，不全扣）

- 5 个错误码验证（每错误码 0.5 分，共 2.5 分）：
  - 编号和常量名完全一致 → 0.5 分
  - 常量名一致但编号不同 → 0.25 分（注解推断误差）
  - 找不到该常量 → 0 分

- 无虚构错误码（7.5 分）：
  - api.md 中所有错误码均能在源码中找到 → 7.5 分
  - 每有 1 个虚构错误码扣 1.5 分，最低 0 分

> 注：三项合计 10 + 2.5 + 7.5 = 20 分。

**示例追溯矩阵：**

| 字段 | api.md 声明类 | 实际源码文件 | 结果 |
|---|---|---|---|
| studentNo | ExampleStudentCreateReqVO | ExampleStudentSaveReqVO.java | MATCH |
| teacherName | ExampleStudentCreateReqVO | （未找到） | MISMATCH（幻觉字段） |
| className | ExampleStudentDO | ExampleStudentDO.java | MATCH |

---

### ③a 核心文档评审：15 分

通过三个专家 agent 对核心 Codebook 文档进行结构化评审。详细评审方式见
`references/agent-review-protocol.md`。

**评分分配：**

| Agent | 审查文档 | 满分 |
|---|---|---|
| `testing-reality-checker` | 每个模块的 `hla.md` | 7 |
| `testing-api-tester` | 每个模块的 `api.md` | 5 |
| `product-manager` | 每个模块的 `prd.md` | 3 |

**多模块处理：** 对每个模块分别评审，取平均分后按满分折算。
例如：3 个模块的 reality-checker 评分分别为 5/7、6/7、4/7 → 平均 5/7 → 最终得 5 分。

---

### ③b 安全与合规审查：10 分

通过三个安全专家 agent 审查 API 安全性、架构安全设计和数据合规。详细评审标准见
`references/agent-review-protocol.md`（Agent 4–6）。

**评分分配：**

| Agent | 审查文档 | 满分 |
|---|---|---|
| `security-appsec-engineer` | 每个模块的 `api.md`（安全质量） | 4 |
| `security-architect` | 每个模块的 `hla.md`（安全架构） | 3 |
| `security-compliance-auditor` | 每个模块的 `database.md` + `prd.md`（合规） | 3 |

**多模块处理：** 同 ③a，各模块独立评审取平均。

**门禁级联：** 若某模块缺少 database.md，合规检查点"敏感表标注"和"审计日志字段"
记为"无法验证（0.5分/项）"，不直接判 0 分。

---

### ④ 性能规格完整性：5 分

通过 `testing-performance-benchmarker` 评审 `srs.md` 和 `hla.md` 中的性能指标说明。
详细评审标准见 `references/agent-review-protocol.md`（Agent 7）。

**评分分配：**

| 检查点 | 满分 |
|---|---|
| 性能指标量化（P95响应时间 + 吞吐量/并发数） | 2 |
| 扩展性说明 | 1 |
| 缓存设计有源码依据（无缓存声明则自动通过） | 1 |
| 监控/告警/降级策略说明 | 1 |

**多模块处理：** 取所有模块平均分后折算为 5 分满分。

---

### ⑤ 变量采集完整性：10 分

衡量生成文档中是否存在未被替换的 Prompt 占位符，表明采集步骤被跳过。

```bash
# 统计所有生成 .md 文件中残留的 {{...}} 占位符
residual_count=$(grep -r '{{[A-Z_]*}}' codebook/ --include="*.md" | wc -l)
```

**评分：**
- 0 个残留 → 10 分
- 1-2 个残留 → 7 分（轻微遗漏）
- 3-5 个残留 → 4 分（明显遗漏）
- 6 个及以上 → 0 分（采集流程严重缺失）

**常见残留原因 → 对应失败分类：**
- `{{ROLES}}` 未替换 → `variable-gap`（SKILL.md 中未说明如何采集角色定义）
- `{{CALL_TRACE_JSON}}` 未替换 → `phase-c-gap`（Phase C3 trace 步骤被跳过）
- `{{ENTITY_RELATIONSHIPS}}` 未替换 → `variable-gap`（未从 Entity 关联字段提取）

---

### ⑥ 文件完整性：10 分

衡量 code-index 是否按 SKILL.md 规范生成了所有必须文件。

**评分细则：**

- .progress.md 中所有 [x]（无 [ ] 残留）→ 3 分
  - 有 [ ] 残留的按比例：(1 - 残留比例) × 3 分
- 系统级文件完整（hla.md + database-overview.md + overview.md 均存在）→ 3 分
  - 每缺 1 个扣 1 分
- 模块级文件完整（每模块 10 类文件均存在）→ 4 分
  - 检查最多 3 个模块，按平均缺失率折算：
    - 某模块缺文件数 / 10 = 缺失率
    - 平均缺失率为 0 → 4 分；每增加 10% 缺失率扣 0.5 分

```bash
# 快速检查单个模块完整性（在 codebook/modules/{module}/ 下）
ls overview.md hla.md srs.md prd.md user-stories.md api.md database.md error-codes.md
ls -d flowcharts/ ui/
```

---

## 接收决策

只有同时满足以下两个条件时才接收实验：

```
合规门禁全部通过
AND new_benchmark_score > previous_benchmark_score
```

分数相等不接收。只在样本池仓库上观察到的提升只能作为诊断信号，不足以接收改动。

**典型分数区间参考（首次建立基准时的预期）：**

| 分数区间 | 含义 |
|---|---|
| 80-100 | 优秀：接口全覆盖、字段准确、文档规范、无残留 |
| 60-79 | 良好：覆盖率 85%+，偶有字段溯源失败 |
| 40-59 | 及格：覆盖率 70%+，部分 Prompt 约束缺失 |
| 0-39 | 不及格：Phase C 采集严重不全，或大量占位符未替换 |
