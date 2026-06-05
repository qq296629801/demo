# Agent 评审协议

本文件定义如何使用 `agents/` 目录中的专家 agent 对 code-index 生成的 Codebook
进行结构化多视角评审，以及各 agent 的评审输入、评审标准和评分输出。

---

## 评审架构

```
code-index-evolve（协调员）
    │
    ├── agents/testing/testing-reality-checker.md  →  审查 hla.md（架构声明真实性）
    ├── agents/testing/testing-api-tester.md       →  审查 api.md（API 规范完整性）
    └── agents/product/product-manager.md          →  审查 prd.md（业务文档规范性）
```

三个评审**并行执行**（互不依赖），各自产出评审报告，由协调员汇总计入总分 ③。

---

## Agent 1：testing-reality-checker → 审查 hla.md

**角色定位（来自 agents/testing/testing-reality-checker.md）：**
默认态度"NEEDS WORK"，要求压倒性证据才能给出通过认证。
核心原则：每一项架构声明必须有对应的源码证据支撑。

### 输入材料

```markdown
## 评审对象
{hla.md 完整内容}

## 对应源码证据
{该模块的 Controller 源码片段（来自 codegraph_search 结果）}
{该模块的 Service 类名列表（来自 codegraph_files 结果）}
{pom.xml 或 package.json 中的依赖版本（如有）}
```

### 评审标准（10 分制）

Reality Checker 按以下 5 个检查点逐项评审，每项 2 分：

| 检查点 | 通过条件 | 失败示例 |
|---|---|---|
| 架构图组件真实性 | Mermaid 架构图中的每个节点能在源码中找到对应类/模块 | 图中有"消息队列层"但源码无 MQ 相关类 |
| 技术选型有依据 | 技术栈表格的版本来自 pom.xml/package.json，不是凭空估计 | Spring Boot 2.7.0 但 pom.xml 是 3.1.0 |
| 横切架构有证据 | 安全/缓存/消息等横切说明能在源码中找到对应注解/配置 | 写了"使用 Redis 缓存"但无 @Cacheable 注解 |
| 模块调用图准确 | Mermaid 调用图中的依赖关系能被 codegraph_callers 验证 | 图中 A 调用 B，但 codegraph 显示 B 调用 A |
| 无幻觉功能声明 | hla.md 描述的功能在 ENDPOINT_LIST 中有对应接口 | 描述了"报表导出功能"但无对应 Controller 方法 |

**评审输出格式：**
```markdown
## hla.md Reality Check 评审报告

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] 架构图组件真实性：{分数}/2 — {具体说明}
- [ ] 技术选型有依据：{分数}/2 — {具体说明}
- [ ] 横切架构有证据：{分数}/2 — {具体说明}
- [ ] 模块调用图准确：{分数}/2 — {具体说明}
- [ ] 无幻觉功能声明：{分数}/2 — {具体说明}

**总分**：{N}/10

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 失败分类：{gap 类型}
2. ...
```

---

## Agent 2：testing-api-tester → 审查 api.md

**角色定位（来自 agents/testing/testing-api-tester.md）：**
API 测试专家，确保 API 文档完整、安全字段不缺失、前后端字段一致。
核心原则：每个接口必须通过功能、安全、性能三个验证维度。

### 输入材料

```markdown
## 评审对象
{api.md 完整内容（或前 3 个接口章节，若文档过长）}

## 对应源码
{Controller 源码（含权限注解）}
{ReqVO / RespVO 源码}
{错误码常量文件（前 20 条）}
```

### 评审标准（8 分制）

API Tester 按以下 4 个检查点逐项评审，每项 2 分：

| 检查点 | 通过条件 | 失败示例 |
|---|---|---|
| Header 约定完整 | 每个接口章节包含 Content-Type、Authorization、是否需要租户头 | 接口章节无任何 Header 说明 |
| 安全字段不缺失 | 每个接口有认证要求说明 + 权限码（来自注解）+ Rate Limiting 说明或 [需确认] | 接口无权限码，无 Bearer Token 说明 |
| 字段校验矩阵规范 | Request Body 有字段校验矩阵（字段名/类型/必填/规则/错误提示）| 只有字段列表，无必填和校验规则 |
| 错误码来源可信 | 错误码来自 ErrorCodeConstants 源码，无虚构错误码 | 使用了 "USER_NOT_FOUND: 1001" 但源码无此常量 |

**评审输出格式：**
```markdown
## api.md API Tester 评审报告

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] Header 约定完整：{分数}/2 — {具体说明，指出哪些接口缺失}
- [ ] 安全字段不缺失：{分数}/2 — {具体说明}
- [ ] 字段校验矩阵规范：{分数}/2 — {具体说明}
- [ ] 错误码来源可信：{分数}/2 — {具体说明，列出可疑错误码}

**总分**：{N}/8

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 对应 llm-prompts.md Prompt 7 的哪条强制要求未生效
2. ...
```

---

## Agent 3：product-manager → 审查 prd.md

**角色定位（来自 agents/product/product-manager.md）：**
产品经理 Alex，确保 PRD 面向非技术读者、成功指标量化、范围边界明确。
核心原则：没有 Non-Goals 和 Success Metrics 的 PRD 是不完整的。

### 输入材料

```markdown
## 评审对象
{prd.md 完整内容}

## 上下文
- 模块名称：{MODULE_NAME}
- 接口总数：{ENDPOINT_LIST 长度}
- 主要实体：{ENTITY_NAME}
```

### 评审标准（7 分制）

Product Manager 按以下检查点逐项评审：

| 检查点 | 分值 | 通过条件 | 失败示例 |
|---|---|---|---|
| Non-Goals 明确 | 2 | 有"范围边界"章节且包含 ≥ 2 条"不负责什么" | 只写"负责什么"，无"不做什么" |
| 成功指标量化 | 2 | 有成功指标表格（指标名/基线/目标/时间窗口）| 无成功指标，或只写"提升用户体验" |
| 用户角色矩阵完整 | 1 | 有角色、目标、可执行操作三列 | 只列角色名，无目标和操作 |
| Why Now 有说明 | 1 | 背景段落有说明为何需要此模块 | 背景只有一句"此模块用于管理XX" |
| 面向非技术读者 | 1 | 无堆叠类名/方法名/包名（少于 3 处技术术语）| 大量出现 @Model、ServiceImpl、Mapper |

**评审输出格式：**
```markdown
## prd.md Product Manager 评审报告

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] Non-Goals 明确：{分数}/2 — {引用 prd.md 中的相关段落}
- [ ] 成功指标量化：{分数}/2 — {引用或说明缺失}
- [ ] 用户角色矩阵完整：{分数}/1 — {具体说明}
- [ ] Why Now 有说明：{分数}/1 — {具体说明}
- [ ] 面向非技术读者：{分数}/1 — {列出出现的技术术语}

**总分**：{N}/7

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 对应 llm-prompts.md Prompt 5 的哪条必须要求未生效
2. ...
```

---

## 多模块评审处理规则

当 Codebook 包含多个模块时：

1. **选取代表性模块**：最多评审 3 个模块（小型项目全评，大型项目取覆盖不同功能类型的模块）
2. **计算平均分**：各模块得分求平均，再按满分折算
3. **最差模块标注**：在 evolve-evidence.md 中特别标注得分最低的模块及原因
4. **跨模块一致性**：检查不同模块间 api.md 格式是否一致（同一批 Prompt 应产生一致格式）

**评审结果汇总模板：**
```markdown
## Agent 评审汇总

| 模块 | Reality Checker (hla) | API Tester (api) | Product Manager (prd) | 模块总分 |
|---|---|---|---|---|
| {module1} | {n}/10 | {n}/8 | {n}/7 | {n}/25 |
| {module2} | {n}/10 | {n}/8 | {n}/7 | {n}/25 |
| **平均** | {avg}/10 | {avg}/8 | {avg}/7 | **{avg}/25** |

**主要失败模式**：
- {模块} hla.md：{最严重问题} → 失败分类：{gap 类型}
- {模块} api.md：{最严重问题} → 失败分类：{gap 类型}
- {模块} prd.md：{最严重问题} → 失败分类：{gap 类型}
```

---

## 评审执行注意事项

1. **证据优先**：评审时尽量引用原文（hla.md/api.md/prd.md 中的具体段落）而非笼统描述
2. **不假设**：看不到对应源码验证时，记为"无法验证"而非直接判 FAIL
3. **一致性**：同一批生成的多个模块使用相同标准评审，不因模块大小调整标准
4. **Problem-First**：发现问题时，先定位到是 code-index 的哪个环节导致（Phase C 采集？Prompt 模板？变量采集？），便于 Phase 4 精准回写
