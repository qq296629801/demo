# create-project Skill 质量评分 Rubric（10 维，100 分）

> 改编自 darwin-skill 9 维 rubric（arXiv 2605.23899 SkillLens），针对 IIDP create-project 具体问题域定制。
> 实证基础：LLM 自评准确率 46.4%，使用独立法官 + meta-skill 维度后提升到 73.8%。

---

## 评分维度

| # | 维度 | 权重 | 满分条件 | 常见扣分场景 |
|---|---|---|---|---|
| **D1** | **模板完整性** | 12pt | 每个 Step（0-5）的输出模板结构完整，无"待补充"/"[TODO]"占位符，每个步骤有明确输入/输出声明 | Step 缺少输出格式示例；"详见后续"等空话 |
| **D2** | **字段规范性** | 12pt | ID 类型全为 String，无手动声明审计字段，ManyToOne 双字段模式（FK String + ORM 对象）在模板中有正确示范 | 出现 `Long id`；出现 `create_user` 字段声明；只有 ManyToOne 对象字段无 FK String |
| **D3** | **平台合规性** | 12pt | 所有示例代码/注解符合 platform-standards.md（`@Model`/`@Property`/`@StaticVar`/`@Getter`/`@Setter` 是平台插件，不是 Lombok；`@MethodService` 非 Spring `@Service`）| 出现 Spring Boot 原生注解替代 IIDP 平台体系 |
| **D4** | **失败机制编码** | 10pt | 每个关键步骤有显式 if-then 失败分支（"若 contracts.md 不存在 → 停止并提示"而非"注意要先准备好 contracts.md"）；有 fallback 路径 | 只写正向流程；失败处理用模糊措辞"请注意"/"建议先" |
| **D5** | **可操作性** | 10pt | 指令具体可直接执行；无"建议考虑"/"根据情况"/"灵活把握"/"视具体需求"等软化措辞；示例代码可直接复制使用 | 每出现 1 处软化措辞扣 1pt |
| **D6** | **ER 关系设计** | 10pt | ManyToOne/OneToMany/ManyToMany 的配对规则完整：FK String 字段 + ORM 对象字段缺一不可；JoinColumn name 与 FK 列名对应关系有明确说明 | 只有 ORM 对象字段；缺少成对声明说明 |
| **D7** | **约束执行力** | 10pt | 禁止事项有明确黑名单，使用"禁止"而非"避免"/"尽量不要"；每条禁令有对应违反后果说明 | 禁令措辞模糊；无违反后果说明 |
| **D8** | **跨步骤一致性** | 8pt | sdd-backend.md / sdd-frontend.md / sdd-workflow.md / contracts.md 之间字段类型/命名规范一致（同一字段在多处文档中类型相同、注解写法相同） | 同一字段在 backend-spec 和 contracts 模板中类型不同 |
| **D9** | **测试可验性** | 8pt | 生成的 backend-spec/frontend-spec 模板中包含可追溯的验收标准（AC 到 TC 的映射格式）；Step 5 validate 检查点有具体检查项 | 验收标准只有"功能正常"等无法操作的描述 |
| **D10** | **实测表现** | 8pt | 用 test-scenarios.md 中的场景实际运行 `/sdd-spec` 命令，检查生成的 backend-spec 是否包含：正确 ID 类型、ManyToOne 成对声明、无手动审计字段、驼峰严格一致 | 生成的规格出现已知错误模式 |

**总分 = Σ(各维度得分)**，满分 100。

---

## 评分规则

- 每个维度打 0 到满分整数分
- **D10 实测维度**：必须实际运行测试场景（不允许只凭文档推断），运行结果即证据
- 改进后总分必须**严格高于**改进前（Δ > 0）才保留，Δ = 0 视为未改进，丢弃
- 连续 2 轮 Δ < 2pt → Early Stop（局部最优）

---

## 独立法官 Prompt 模板

> 将以下 prompt 完整发送给独立 sub-agent（不带任何上下文，让其独立判断）：

```
你是 IIDP create-project skill 质量评审员。请阅读以下文件并按 10 维 rubric 打分：

【必须读取的文件】
1. skills/create-project/references/sdd-backend.md
2. skills/create-project/references/sdd-workflow.md
3. skills/create-project/commands/sdd-spec.md
4. skills/create-project/commands/sdd-contracts.md
5. skills/evolve/references/skill-rubric.md（本 rubric 定义）
6. skills/evolve/references/test-scenarios.md（测试场景）

【D10 实测要求】
用 test-scenarios.md 的场景 1（含 ManyToOne 关系的实体），模拟运行 /sdd-spec 流程，
检查生成的 backend-spec 模板中：
- ID 类型是否为 String（不是 Long）
- ManyToOne 是否有成对的 FK String 字段
- 是否出现手动声明的 create_user/create_date 等审计字段

【输出格式】
请严格按以下格式输出，每行一个维度：
D1: [分数]/12 — [1-2句诊断说明]
D2: [分数]/12 — [1-2句诊断说明]
D3: [分数]/12 — [1-2句诊断说明]
D4: [分数]/10 — [1-2句诊断说明]
D5: [分数]/10 — [1-2句诊断说明]
D6: [分数]/10 — [1-2句诊断说明]
D7: [分数]/10 — [1-2句诊断说明]
D8: [分数]/8 — [1-2句诊断说明]
D9: [分数]/8 — [1-2句诊断说明]
D10: [分数]/8 — [1-2句诊断说明]
总分: [合计]/100
最低分维度: D[X]（[维度名]，[分数]/[满分]）
```

**重要**：两个独立法官使用完全相同的 prompt，取各维度平均分（四舍五入）作为最终分。

---

## 维度与 create-project 文件映射

| 维度 | 主要对应文件 | 次要对应文件 |
|---|---|---|
| D1 模板完整性 | `sdd-workflow.md` | 所有 `commands/*.md` |
| D2 字段规范性 | `sdd-backend.md` | `model-property-advanced.md` |
| D3 平台合规性 | `sdd-backend.md` | `platform-standards.md` |
| D4 失败机制编码 | 所有 `commands/*.md` | `sdd-workflow.md` |
| D5 可操作性 | `sdd-backend.md`, `sdd-frontend.md` | 所有 `commands/*.md` |
| D6 ER 关系设计 | `sdd-backend.md` | `model-property-advanced.md` |
| D7 约束执行力 | `sdd-backend.md`, `platform-standards.md` | 所有 `commands/*.md` |
| D8 跨步骤一致性 | `sdd-backend.md` ↔ `sdd-workflow.md` ↔ `contracts.md` | — |
| D9 测试可验性 | `sdd-validate.md`, `sdd-tasks.md` | `sdd-workflow.md` |
| D10 实测表现 | 运行 `/sdd-spec`（test-scenarios.md 场景） | — |
