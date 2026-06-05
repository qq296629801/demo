---
name: code-index-evolve
description: |
  code-index skill 的自进化评测框架。通过固定样本代码仓库、agents 专家评审、100 分 Codebook
  质量评分和 autoresearch 循环，持续提升 code-index 生成规格文档的准确性与完整性。
  当需要"测试 code-index 生成质量"、"评审 Codebook 是否准确"、"优化规格书生成流程"、
  "发现 code-index 薄弱点"、"迭代提升文档生成效果"时使用本 skill。
---

# code-index-evolve

## 概述

`code-index-evolve` 是 `skills/code-index` 的自进化评测框架。它用固定样本代码仓库、
agents 专家多视角评审、100 分 Codebook 质量评分，判断某次 code-index skill 改动是否
值得保留——只有 **接口覆盖率 + 可追溯性 + Agent 评审 + 变量完整性 + 文件完整性** 五个
维度综合评分提升才接收改动。

本 skill 参考 autoresearch 循环：固定样本仓库环境，只修改 `skills/code-index/`，
用同一份代码可复现评分，只提交提升，最终交给人工审核。每次评分提升后，强制执行
**Phase 4 技能反馈回写**，把本轮发现的问题模式和解法结构化写回 code-index skill。

---

## 硬边界

- 固定基准是一个**样本代码仓库**（git commit hash 锁定），存于 `.code-index-evolve/baseline-repo/`，
  hash 记录在 `.code-index-evolve/baseline.json`。
- 首次使用时记录基准仓库 hash。后续评测必须复用同一个 commit（相同 hash），除非用户明确要求刷新基准。
- 自动改进阶段**只能修改** `skills/code-index/` 目录下的文件：
  - `skills/code-index/SKILL.md`：采集流程、Phase 规则、断点恢复
  - `skills/code-index/references/*.md`：Prompt 模板、框架识别规则、变量说明
- **不得修改** `agents/`、`skills/code-index-evolve/` 本身、样本仓库、生成的 Codebook
  产物或 codegraph 索引文件来强行提分。
- 每一轮改进只做一个小而可审查的改动。第二个问题放到下一轮。
- 分数提升则保留分支等待人工审核；分数未提升必须回滚。
- 永远不要自动合并 evolve 分支。

---

## 私有规则来源门禁

code-index 的采集规则依赖框架私有知识（IIDP 元模型、Spring 注解约定等）。
**禁止**从公网推断采集规则，也不得凭训练知识或单次失败现象把推测写成稳定规则。

允许作为规则依据的来源仅限：
- 本地文档：`skills/code-index/references/` 下的参考文件
- 样本仓库实际源码和 codegraph 输出
- 真实 IIDP/Java 工程源码
- 用户人工确认

没有证据的规则必须标记为 `knowledge-gap`，不得写回 `skills/code-index/`。

---

## 失败分类

| 分类 | 描述 | 典型现象 |
|---|---|---|
| `phase-c-gap` | Phase C 采集不完整 | 漏了 Controller/Entity/VO 文件；ENDPOINT_LIST 少接口 |
| `prompt-gap` | Prompt 模板表达不足 | 生成文档缺关键章节或字段；输出结构不符合要求 |
| `variable-gap` | 变量采集步骤缺失 | 生成文档中残留未替换的 `{{...}}` 占位符 |
| `iidp-extract-gap` | IIDP baseline-spec 提取规则不足 | baseline-spec JSON 字段缺失或 confidence 全 low |
| `framework-detect-gap` | 框架识别规则有误 | 非 Java 项目识别失败；前端框架未检测到 |
| `knowledge-gap` | 缺乏样本证据，无法确定改法 | 只有 1 个样本失败，无法判断是规则问题 |

---

## 运行产物协议

使用仓库根目录下 `.code-index-evolve/` 作为唯一工作区：

```
.code-index-evolve/
├── baseline.json                       # 基准仓库标题、路径、commit hash、首次记录时间、最近评分
├── baseline-repo/                      # 只读。固定基准代码仓库（或符号链接）
├── samples/
│   └── <sample-name>/                  # 样本仓库目录，每个样本一个子目录
├── runs/
│   ├── <timestamp>-baseline/           # 基准运行产物（codebook 副本、评分报告）
│   └── <timestamp>-sample-<name>/      # 样本运行产物，与基准隔离
├── evolve-results.tsv                  # 每轮一行的机器可读摘要
└── evolve-evidence.md                  # 人类可读运行日志（评分表、Agent 评审结果、决策、Phase 4 发现）
```

除非用户明确要求，`.code-index-evolve/` 下的运行产物不提交 git。

---

## 工作流

### Phase 0：加载参考资料

按需读取以下文件：

| 需要 | 读取 |
|---|---|
| 实验循环、分支、保留/回滚规则 | `references/autoresearch-loop.md` |
| 评分计算和接收门禁 | `references/evaluation-rubric.md` |
| agents 评审任务分配和评分标准 | `references/agent-review-protocol.md` |
| 样本仓库 ground truth 格式 | `references/sample-repos.md` |

运行真实评测前，还必须读取 `skills/code-index/SKILL.md` 确认当前 code-index 流程。

---

### Phase 1：建立基准评分

```
1. 加载 .code-index-evolve/baseline-repo/ 中的样本代码仓库
   首次运行：
     a. 对样本仓库执行 codegraph init -i，建立索引
     b. 读取或手工确认 ground truth（接口数 GT_API、实体数 GT_ENTITY、模块数 GT_MODULE）
     c. 将 ground truth 写入 .code-index-evolve/baseline.json

2. 执行 code-index skill（完整运行，Phase A→B→C→D→D'）
   生成 Codebook 到 .code-index-evolve/runs/<timestamp>-baseline/codebook/

3. 执行数量对账（按 evaluation-rubric.md § 接口文档完整性）：
   a. 统计生成 codebook 中各模块 api.md 的接口章节数（ACTUAL_API）
   b. 统计 database.md 中的表数（ACTUAL_ENTITY）
   c. 统计 modules/ 下子目录数（ACTUAL_MODULE）
   d. 计算覆盖率 = ACTUAL / GT（来自 baseline.json）

4. 执行 Agent 多视角评审（按 references/agent-review-protocol.md）：
   a. testing-reality-checker 审 hla.md
   b. testing-api-tester 审 api.md
   c. product-manager 审 prd.md
   三者并行，汇总各维度得分

5. 执行可追溯性抽查（按 evaluation-rubric.md § 可追溯性与无幻觉）：
   从 api.md 随机抽取 10 个字段，逐一 Read 对应 VO/Entity 源码验证存在性

6. 检查变量填充完整性：
   grep -r '{{[A-Z_]*}}' codebook/ | wc -l  → 统计残留占位符数

7. 检查文件完整性：
   对照 .progress.md 确认所有 [x] 条目和必须文件存在

8. 按 evaluation-rubric.md 汇总 100 分评分，写入 evolve-evidence.md
```

基准分是后续 skill 改动的唯一接收阈值。

#### eval-only 模式

当用户要求试跑或只建立 baseline 时：只执行 Phase 0 和 Phase 1，不创建实验分支，
不修改任何 skill 文件，不提交 commit。

---

### Phase 2：探索样本池（可选）

使用多个额外代码仓库样本，发现 code-index skill 的薄弱点：

- 样本测试不得修改或读写 `.code-index-evolve/baseline-repo/` 中的任何文件
- 样本文档存入 `.code-index-evolve/samples/<name>/`，生成产物存入独立的 `runs/<timestamp>-sample-<name>/`
- 对每个样本执行与 Phase 1 相同的完整评测流程（建索引→生成→对账→Agent 评审→打分）
- 将结果写入 `evolve-evidence.md` 对应样本记录

**不要只根据样本池分数保留或拒绝 skill 改动**；只有基准分提升才接收。

---

### Phase 3：改进 code-index skill

```
1. 创建或切换到隔离的 evolve 分支（命名：code-index-evolve/YYYYMMDD-HHMM）

2. 从基准或样本池评分报告中选择一个失败模式（低分维度）

3. 按失败分类，避免把资料不足误判为 skill 问题：
   - phase-c-gap    → 修 SKILL.md 的 Phase C 采集步骤，或 iidp-framework.md 提取规则
   - prompt-gap     → 修 references/llm-prompts.md 对应 Prompt 模板
   - variable-gap   → 修 references/llm-prompts.md 变量说明，或 SKILL.md 中的 UI_CONTEXT 打包步骤
   - iidp-extract-gap → 修 references/iidp-framework.md 或 Prompt 14-18
   - framework-detect-gap → 修 SKILL.md 框架识别步骤或对应 references/ 文件
   - knowledge-gap  → 只记录缺口和所需证据，不修改 skill

4. 每轮只做一小块改动；修改前写明改进假设（一句话）

5. 提交改动（commit message 格式：code-index-evolve: improve <short reason>）

6. 使用相同基准仓库（相同 hash）重新运行完整评测（Phase 1）

7. 检查合规门禁（evaluation-rubric.md § 合规门禁）：
   - 门禁不通过 → 直接 REVERT，不进入评分

8. 比较新基准分与上一轮基准分：
   - new_score > previous_score → 保留 commit，更新证据，立即执行 Phase 4 技能反馈回写
   - new_score <= previous_score → 回滚 commit，记录失败原因
```

---

### Phase 4：技能反馈回写

**触发条件：** Phase 3 评分提升后强制执行，不能跳过。

**目的：** 把本轮通过调试、对账、Agent 评审发现的隐性知识结构化写回 code-index skill，
消除下次运行的同类踩坑成本。

#### 4.1 收集发现信号

| 信号 | 含义 | 示例 |
|---|---|---|
| 对账时发现漏了某类 Controller 命名模式 | Phase C 采集规则有缺口 | `*ApiController.java` 未被 glob 匹配 |
| Prompt 输出缺失某章节超过 3 次 | Prompt 模板强制要求表达不足 | PRD 缺 Non-Goals 章节 |
| 某变量连续 2 次未被填充 | 变量采集步骤在 SKILL.md 中没有明确触发点 | `{{ROLES}}` 未从权限注解提取 |
| Agent 评审连续指出同一问题 | 对应 Prompt 约束条件缺失 | api.md 无 Header 约定说明 |
| 某框架的实体类命名没有被识别 | 框架识别规则未覆盖该命名后缀 | `*PO.java`、`*BO.java` |

**每轮至少产生 1 条发现。** 若评分满分或无明显缺口，写"本轮未发现可回写的文档缺口"。

#### 4.2 结构化记录（写入 evolve-evidence.md）

```markdown
### 发现 N：<一句话标题>

| 字段 | 内容 |
|---|---|
| **问题** | 本轮什么操作因 skill 缺失而失败/低效 |
| **根因** | skills/code-index/ 哪个文件哪个章节信息不足（精确到标题） |
| **解法** | 实际采用的修改方式 |
| **失败分类** | phase-c-gap / prompt-gap / variable-gap / iidp-extract-gap / framework-detect-gap / knowledge-gap |
| **证据来源** | 对账数据、Agent 评审输出、源码路径、codegraph 输出 |
| **目标文件** | skills/code-index/SKILL.md 或 references/xxx.md |
| **插入位置** | 在哪个已有章节/段落之后新增 |
| **验证方式** | 重新运行 Phase 1 后对应维度评分提升 |
```

#### 4.3 写回 skill 文件

对每条发现，直接编辑 `skills/code-index/` 目标文件：
1. 按发现的"插入位置"定位，Read 目标文件确认上下文
2. 写可独立理解的内容：包含具体规则、可执行示例、反例（常见错误写法）
3. 用 Edit 工具插入

**写回规则：**
- 每次只写一条发现，确保 diff 可审查
- 写回规则必须包含失败现象、证据来源、规则结论、目标文件、验证方式；缺任一项时只记录 `knowledge-gap`
- 从单次现象得到的结论只能作为候选规则；需有 ≥ 2 个样本证据才能沉淀为稳定规则

**目标文件定位规则：**

| 发现类型 | 目标文件 |
|---|---|
| Phase C 采集步骤不完整 | `skills/code-index/SKILL.md` § Phase C |
| Prompt 模板缺关键约束 | `skills/code-index/references/llm-prompts.md` 对应 Prompt |
| 变量定义缺失或采集来源不清 | `skills/code-index/references/llm-prompts.md` § 通用变量说明 |
| IIDP 提取规则不足 | `skills/code-index/references/iidp-framework.md` |
| Java 框架注解识别有缺口 | `skills/code-index/references/java-frameworks.md` |
| 前端框架识别有缺口 | `skills/code-index/references/frontend-frameworks.md` |
| 非 Java 后端框架有缺口 | `skills/code-index/references/multi-framework-patterns.md` |

#### 4.4 提交

Phase 4 写回改动与 Phase 3 改动在同一 evolve 分支：
- 文件不重叠时可合并到同一 commit
- 涉及新文件时单独 commit，message：`code-index-evolve: 回写 Phase 4 发现 N - <标题>`

---

### Phase 5：人工审核门禁

当至少一个 commit 提升了基准分：
- 保留 evolve 分支
- 汇总分数变化、变更文件、基准仓库 hash、评分报告和样本证据
- 请求人工审核；若用户要求发布到 GitHub，则创建 PR
- **不要自动合并**

当没有任何 commit 提升基准分：
- 回滚所有失败实验 commit
- 返回原始分支
- 报告观察到的最有价值失败模式，以及没有保留改动的原因

---

## 完成检查清单

- [ ] 已记录基准仓库路径、commit hash 和运行环境摘要（`.code-index-evolve/baseline.json`）
- [ ] Ground truth（GT_API、GT_ENTITY、GT_MODULE）已手工确认并记录
- [ ] code-index skill 已完整运行（Phase A→D'），生成 Codebook 产物
- [ ] 数量对账已执行（ACTUAL vs GT），覆盖率已计算
- [ ] 三个 Agent 评审（reality-checker / api-tester / product-manager）均已执行
- [ ] 可追溯性抽查已执行（10 个字段逐一验证）
- [ ] 变量填充完整性检查已执行（grep 残留 `{{...}}`）
- [ ] 文件完整性检查已执行（.progress.md 全 [x]）
- [ ] 评分使用 `references/evaluation-rubric.md`
- [ ] 任何保留的改动只修改 `skills/code-index/`
- [ ] 失败改动已回滚并解释
- [ ] **Phase 4 已执行：** 本轮发现已结构化写回 code-index skill，或已按 `knowledge-gap` 记录不能写回的原因
