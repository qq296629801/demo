# Agent 评审协议

本文件定义如何使用 `agents/` 目录中的专家 agent 对 code-index 生成的 Codebook
进行结构化多视角评审，以及各 agent 的评审输入、评审标准和评分输出。

---

## 评审架构

```
code-index-evolve（协调员）
    │
    ├── ③a 核心文档评审
    │   ├── agents/testing/testing-reality-checker.md      →  审查 hla.md（架构声明真实性）
    │   ├── agents/testing/testing-api-tester.md           →  审查 api.md（API 规范完整性）
    │   └── agents/product/product-manager.md              →  审查 prd.md（业务文档规范性）
    │
    ├── ③b 安全与合规审查
    │   ├── agents/security/security-appsec-engineer.md    →  审查 api.md（安全质量）
    │   ├── agents/security/security-architect.md          →  审查 hla.md（安全架构设计）
    │   └── agents/security/security-compliance-auditor.md →  审查 database.md + prd.md（合规）
    │
    ├── ③c 工程质量评审
    │   ├── agents/engineering/engineering-backend-architect.md  →  审查 database.md + api.md（工程规范）
    │   ├── agents/engineering/engineering-software-architect.md →  审查 hla.md（架构决策质量）
    │   ├── agents/engineering/engineering-technical-writer.md   →  审查所有文档（可读性/一致性）
    │   └── agents/engineering/engineering-frontend-developer.md →  审查 ui/ + srs.md（前端规格，条件性）
    │
    └── ④ 性能规格完整性
        └── agents/testing/testing-performance-benchmarker.md → 审查 srs.md + hla.md（性能指标）
```

十一个评审**并行执行**（互不依赖），各自产出评审报告，由协调员汇总计入评分 ③a/③b/③c/④。

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

## Agent 4：security-appsec-engineer → 审查 api.md（安全质量）

**角色定位（来自 agents/security/security-appsec-engineer.md）：**
应用安全工程师，通过威胁建模和安全代码审查在 SDLC 中嵌入安全防控。
核心原则：安全字段必须来自源码注解，敏感数据必须有显式处理说明。

### 输入材料

```markdown
## 评审对象
{api.md 完整内容（或前 3 个接口章节）}

## 对应源码
{Controller 源码（含 @PreAuthorize / @RequiresPermissions 注解）}
{ReqVO / RespVO 源码（含字段类型和注解）}
{error-codes.md 全部内容}
```

### 评审标准（8 分制）

| 检查点 | 分值 | 通过条件 | 失败示例 |
|---|---|---|---|
| 权限矩阵完整 | 2 | 每个接口有权限码（来自注解）+ 可访问角色说明 | 接口无权限码，或权限码与注解不符 |
| 敏感字段标注 | 2 | PII/密码/Token 字段有脱敏说明或 [SENSITIVE] 标注 | password 字段直接出现在 Response 示例中无任何说明 |
| 认证方式规范 | 2 | 每个接口章节有认证方式说明（Bearer JWT / 公开接口标注 `[公开]`）| 接口无任何认证说明 |
| 错误不泄露内部信息 | 2 | error-codes 章节无含堆栈路径/SQL/类名的错误消息模板 | 错误消息包含 "NullPointerException at com.xxx" |

**评审输出格式：**
```markdown
## api.md AppSec Engineer 评审报告

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] 权限矩阵完整：{分数}/2 — {指出哪些接口缺少权限码，引用注解原文}
- [ ] 敏感字段标注：{分数}/2 — {列出出现的敏感字段及处理状态}
- [ ] 认证方式规范：{分数}/2 — {具体说明}
- [ ] 错误不泄露内部信息：{分数}/2 — {列出可疑错误消息}

**总分**：{N}/8

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 失败分类：{gap 类型}
```

---

## Agent 5：security-architect → 审查 hla.md（安全架构设计）

**角色定位（来自 agents/security/security-architect.md）：**
安全架构师，专注系统级信任边界分析和纵深防御设计。
核心原则：每一层防护声明必须有对应的源码或配置证据。

### 输入材料

```markdown
## 评审对象
{hla.md 完整内容}

## 对应源码
{application.yml / application-prod.yml 安全配置片段}
{SecurityConfig / WebSecurityConfigurerAdapter 源码（如有）}
{涉及加密/密钥管理的常量类或配置类（如有）}
```

### 评审标准（6 分制）

| 检查点 | 分值 | 通过条件 | 失败示例 |
|---|---|---|---|
| 信任边界可见 | 2 | 架构图或说明中标注了跨越信任边界的组件间通信（内网/外网/第三方） | 架构图中内外网组件混排，无任何边界标注 |
| 纵深防御分层 | 2 | 安全架构说明含认证层 + 授权层 + 输入校验层（来自源码注解或配置验证） | 只写"系统使用 JWT 认证"，无授权和校验层说明 |
| 敏感数据加密说明 | 2 | 密码/Token/私钥的存储和传输有加密方案说明（来自配置或源码常量验证） | 写了"密码加密存储"但源码无对应加密工具类 |

**评审输出格式：**
```markdown
## hla.md Security Architect 评审报告

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] 信任边界可见：{分数}/2 — {引用架构图中的相关节点，或说明缺失}
- [ ] 纵深防御分层：{分数}/2 — {列出已有和缺失的防护层}
- [ ] 敏感数据加密说明：{分数}/2 — {引用源码/配置证据，或说明无法验证}

**总分**：{N}/6

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 失败分类：{gap 类型}
```

---

## Agent 6：security-compliance-auditor → 审查 database.md + prd.md（合规）

**角色定位（来自 agents/security/security-compliance-auditor.md）：**
技术合规审计专家，通过控制评估和证据收集确保系统满足数据保护要求。
核心原则：数据合规要求必须在设计阶段明确，不能留到实现阶段。

### 输入材料

```markdown
## 评审对象
{database.md 完整内容}
{prd.md 完整内容}

## 上下文
- 模块名称：{MODULE_NAME}
- 主要实体：{ENTITY_NAME}
- 是否涉及用户个人信息：{是/否/未知}
```

### 评审标准（6 分制）

| 检查点 | 分值 | 通过条件 | 失败示例 |
|---|---|---|---|
| 敏感表标注 | 2 | database.md 中存储 PII/密码/手机/身份证的表有 `[敏感]` 标注 | user 表含 phone/id_card 字段，但无任何敏感标注 |
| 审计日志字段 | 2 | 涉及增删改的表有 creator / updater / create_time / update_time / deleted（或等价字段） | 核心业务表缺少 create_time / update_time 字段 |
| 数据合规说明 | 2 | prd.md 中有数据收集范围说明（收集什么、保留期限、谁可访问）| prd.md 无任何数据生命周期说明 |

**评审输出格式：**
```markdown
## database.md + prd.md Compliance Auditor 评审报告

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] 敏感表标注：{分数}/2 — {列出含敏感字段但缺少标注的表名}
- [ ] 审计日志字段：{分数}/2 — {列出缺少审计字段的表名}
- [ ] 数据合规说明：{分数}/2 — {引用 prd.md 中的相关段落，或说明缺失}

**总分**：{N}/6

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 失败分类：{gap 类型}
```

---

## Agent 7：testing-performance-benchmarker → 审查 srs.md + hla.md（性能规格）

**角色定位（来自 agents/testing/testing-performance-benchmarker.md）：**
性能测试和优化专家，确保性能 SLA 在规格阶段明确量化。
核心原则：没有 P95 响应时间目标的性能需求等于没有性能需求。

### 输入材料

```markdown
## 评审对象
{srs.md 完整内容（重点关注非功能性需求章节）}
{hla.md 完整内容（重点关注缓存/扩展性/监控章节）}

## 对应源码（可选，用于验证缓存声明）
{Redis / @Cacheable 相关类片段（如 hla.md 声明了缓存）}
```

### 评审标准（5 分制）

| 检查点 | 分值 | 通过条件 | 失败示例 |
|---|---|---|---|
| 性能指标量化 | 2 | srs.md 非功能需求中有 P95 响应时间目标 + 吞吐量或并发用户数 | 只写"系统响应要快"，无具体数字 |
| 扩展性说明 | 1 | hla.md 有水平或垂直扩展策略说明 | hla.md 无任何扩展性说明 |
| 缓存设计有依据 | 1 | 若 hla.md 声明使用缓存，则源码有 @Cacheable / Redis 相关类；若无缓存声明则此项自动通过 | 声称"使用 Redis 缓存热点数据"但源码无任何 Redis 配置 |
| 监控基线 | 1 | srs.md 或 hla.md 有监控/告警/降级策略说明 | 两份文档均无任何监控相关内容 |

**评审输出格式：**
```markdown
## srs.md + hla.md Performance Benchmarker 评审报告

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] 性能指标量化：{分数}/2 — {引用或说明缺失的具体指标}
- [ ] 扩展性说明：{分数}/1 — {具体说明}
- [ ] 缓存设计有依据：{分数}/1 — {引用源码证据，或说明无缓存声明（自动通过）}
- [ ] 监控基线：{分数}/1 — {具体说明}

**总分**：{N}/5

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 失败分类：{gap 类型}
```

---

## Agent 8：engineering-backend-architect → 审查 database.md + api.md（工程规范）

**角色定位（来自 agents/engineering/engineering-backend-architect.md）：**
后端架构师，专注可扩展系统设计、数据库架构、API 开发和云基础设施。
核心原则：可观测性是非协商项，API 必须定义超时/重试/幂等语义。

### 输入材料

```markdown
## 评审对象
{database.md 完整内容}
{api.md 完整内容（或前 3 个接口章节）}

## 对应源码
{主要 Entity/DO 类源码（含字段、索引注解）}
{Controller 源码（含超时/重试相关注解或配置，如有）}
```

### 评审标准（5 分制）

| 检查点 | 分值 | 通过条件 | 失败示例 |
|---|---|---|---|
| 数据库索引策略 | 2 | database.md 的表结构中有索引字段说明，或注明"无复合查询无需额外索引" | 表有 10+ 字段但无任何索引说明 |
| API 幂等性与容错语义 | 2 | 写操作接口（POST/PUT/DELETE）有幂等性说明 + 超时或重试建议 | 创建接口无幂等键说明，无超时 SLA |
| 可观测性基础 | 1 | hla.md 或 srs.md 中有结构化日志 / 分布式追踪 / SLO 中至少一项说明 | 两份文档均无任何监控/追踪说明 |

**评审输出格式：**
```markdown
## database.md + api.md Backend Architect 评审报告

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] 数据库索引策略：{分数}/2 — {指出缺少索引说明的表名}
- [ ] API 幂等性与容错语义：{分数}/2 — {指出缺少幂等说明的写操作接口}
- [ ] 可观测性基础：{分数}/1 — {具体说明}

**总分**：{N}/5

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 失败分类：{gap 类型}
```

---

## Agent 9：engineering-software-architect → 审查 hla.md（架构决策质量）

**角色定位（来自 agents/engineering/engineering-software-architect.md）：**
软件架构师，通过 DDD、ADR 和权衡分析设计可维护且与业务域对齐的系统。
核心原则："说出你放弃了什么"——每个架构决策必须说明为什么选择当前方案。

### 输入材料

```markdown
## 评审对象
{hla.md 完整内容}

## 上下文
- 模块名称：{MODULE_NAME}
- 主要实体：{ENTITY_NAME}
- 技术栈：{TECH_STACK}
```

### 评审标准（4 分制）

| 检查点 | 分值 | 通过条件 | 失败示例 |
|---|---|---|---|
| 架构决策有 rationale | 2 | 关键架构选择（框架/分层/通信模式）有"为什么这样选"的说明，不只列结果 | 只写"使用 Spring Boot + MyBatis"，无任何选型理由 |
| 模块边界与依赖方向 | 2 | 模块间依赖关系在架构图中清晰标注，且依赖方向符合"域策略不依赖基础设施"原则 | 架构图中模块间有双向箭头，或 Service 直接依赖 Controller |

**评审输出格式：**
```markdown
## hla.md Software Architect 评审报告

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] 架构决策有 rationale：{分数}/2 — {引用缺少 rationale 的具体决策点}
- [ ] 模块边界与依赖方向：{分数}/2 — {引用架构图中的问题节点/箭头}

**总分**：{N}/4

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 失败分类：{gap 类型}
```

---

## Agent 10：engineering-technical-writer → 审查所有文档（可读性与一致性）

**角色定位（来自 agents/engineering/engineering-technical-writer.md）：**
技术文档专家，让开发者实际读完并用得上文档。
核心原则："坏文档等同于产品缺陷"——代码示例必须可运行，术语必须一致。

### 输入材料

```markdown
## 评审对象
{overview.md 完整内容}
{api.md 中 2-3 个接口章节（含请求/响应示例）}

## 辅助文档（用于跨文档一致性检查）
- 在 api.md / hla.md / srs.md 中出现的核心术语列表（如：模块名、实体名、接口名）
```

### 评审标准（3 分制）

| 检查点 | 分值 | 通过条件 | 失败示例 |
|---|---|---|---|
| 示例格式可信 | 1 | api.md 请求/响应示例的字段名与源码字段名一致，JSON 格式合法 | 示例中有 `"studentId"` 但源码字段是 `"studentNo"` |
| 术语跨文档一致 | 1 | 同一概念在 overview.md / api.md / srs.md 中使用相同名称（不混用中英文或别名） | overview.md 写"学生管理"，api.md 写"学员信息" |
| 快速上手路径存在 | 1 | overview.md 或 srs.md 有"如何开始使用此模块"的明确入口说明 | overview.md 只有功能列表，无任何使用路径引导 |

**评审输出格式：**
```markdown
## 文档 Technical Writer 评审报告

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] 示例格式可信：{分数}/1 — {列出不一致的字段名对比}
- [ ] 术语跨文档一致：{分数}/1 — {列出不一致的术语对}
- [ ] 快速上手路径存在：{分数}/1 — {引用或说明缺失}

**总分**：{N}/3

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 失败分类：{gap 类型}
```

---

## Agent 11：engineering-frontend-developer → 审查 ui/ + srs.md（前端规格）

**角色定位（来自 agents/engineering/engineering-frontend-developer.md）：**
前端开发专家，构建响应式、无障碍、高性能的 Web 应用。
核心原则：无障碍合规和 Core Web Vitals 是默认要求，不是可选项。

> **条件性评审**：仅当 Codebook 存在 `ui/` 目录或 `srs.md` 中有前端相关需求时执行。
> 若项目为纯后端 API / 无前端输出，此 Agent 评审自动跳过，记为 N/A（不计入分母）。

### 输入材料

```markdown
## 评审对象
{ui/ 目录下所有文件内容（若存在）}
{srs.md 中的非功能性需求章节（重点：前端性能、无障碍、响应式）}

## 上下文
- 项目是否有前端：{是/否}
- 目标用户设备：{PC / Mobile / 两者}
```

### 评审标准（3 分制，条件性）

| 检查点 | 分值 | 通过条件 | 失败示例 |
|---|---|---|---|
| Core Web Vitals 目标 | 1 | srs.md 或 ui/ 中有 LCP/FID（INP）/CLS 具体目标值，或说明"无性能约束" | 有前端页面但无任何加载性能说明 |
| 无障碍合规声明 | 1 | ui/ 或 srs.md 有 WCAG 2.1 AA（或更高）合规说明，包括键盘导航和屏幕阅读器支持 | 有 UI 原型但无无障碍说明 |
| 响应式设计规范 | 1 | ui/ 说明了断点策略（移动 / 平板 / 桌面）和移动优先设计原则 | UI 原型只有桌面版布局，无响应式说明 |

**评审输出格式：**
```markdown
## ui/ + srs.md Frontend Developer 评审报告

**评审状态**：执行 / N/A（纯后端项目）

**评审结论**：PASS / NEEDS WORK / FAIL

**检查点结果**：
- [ ] Core Web Vitals 目标：{分数}/1 — {引用或说明缺失}
- [ ] 无障碍合规声明：{分数}/1 — {引用或说明缺失}
- [ ] 响应式设计规范：{分数}/1 — {引用或说明缺失}

**总分**：{N}/3（或 N/A）

**主要问题（需在 code-index skill 中修复）**：
1. {问题描述} → 失败分类：{gap 类型}
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

### ③a 核心文档评审（12分）

| 模块 | Reality Checker /hla (5) | API Tester /api (4) | Product Manager /prd (3) | 小计 |
|---|---|---|---|---|
| {module1} | {n}/5 | {n}/4 | {n}/3 | {n}/12 |
| {module2} | {n}/5 | {n}/4 | {n}/3 | {n}/12 |
| **平均** | {avg}/5 | {avg}/4 | {avg}/3 | **{avg}/12** |

### ③b 安全与合规审查（8分）

| 模块 | AppSec /api (3) | Sec Architect /hla (3) | Compliance /db+prd (2) | 小计 |
|---|---|---|---|---|
| {module1} | {n}/3 | {n}/3 | {n}/2 | {n}/8 |
| {module2} | {n}/3 | {n}/3 | {n}/2 | {n}/8 |
| **平均** | {avg}/3 | {avg}/3 | {avg}/2 | **{avg}/8** |

### ③c 工程质量评审（15分）

| 模块 | Backend Arch /db+api (5) | SW Architect /hla (4) | Tech Writer /all (3) | Frontend Dev /ui+srs (3) | 小计 |
|---|---|---|---|---|---|
| {module1} | {n}/5 | {n}/4 | {n}/3 | {n}/3 或 N/A | {n}/15 |
| {module2} | {n}/5 | {n}/4 | {n}/3 | {n}/3 或 N/A | {n}/15 |
| **平均** | {avg}/5 | {avg}/4 | {avg}/3 | {avg}/3 或 N/A | **{avg}/15** |

> 注：Frontend Developer 评审为条件性，纯后端项目记 N/A，其余三项满分折算为 12 分。

### ④ 性能规格完整性（5分）

| 模块 | Performance Benchmarker /srs+hla (5) |
|---|---|
| {module1} | {n}/5 |
| **平均** | {avg}/5 |

**主要失败模式**：
- {模块} hla.md：{最严重问题} → 失败分类：{gap 类型}
- {模块} api.md：{最严重问题} → 失败分类：{gap 类型}
- {模块} prd.md：{最严重问题} → 失败分类：{gap 类型}
- {模块} database.md：{最严重问题} → 失败分类：{gap 类型}
- {模块} srs.md：{最严重问题} → 失败分类：{gap 类型}
- {模块} ui/：{最严重问题} → 失败分类：{gap 类型}
```

---

## 评审执行注意事项

1. **证据优先**：评审时尽量引用原文（hla.md/api.md/prd.md 中的具体段落）而非笼统描述
2. **不假设**：看不到对应源码验证时，记为"无法验证"而非直接判 FAIL
3. **一致性**：同一批生成的多个模块使用相同标准评审，不因模块大小调整标准
4. **Problem-First**：发现问题时，先定位到是 code-index 的哪个环节导致（Phase C 采集？Prompt 模板？变量采集？），便于 Phase 4 精准回写
