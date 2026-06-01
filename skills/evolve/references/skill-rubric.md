# create-project Skill 质量评分 Rubric（14 维，140 分）

> 改编自 darwin-skill 9 维 rubric（arXiv 2605.23899 SkillLens），针对 IIDP create-project 具体问题域定制。
> 实证基础：LLM 自评准确率 46.4%，使用独立法官 + meta-skill 维度后提升到 73.8%。
>
> **v2**：新增 F1-F4 前端评分维度（40pt），总分从 100pt 扩展至 140pt。

---

## 后端评分维度（D1-D10，满分 100pt）

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
| **D10** | **后端实测表现** | 8pt | 用 test-scenarios.md 中的场景实际运行 `/sdd-spec` 命令，检查生成的 backend-spec 是否包含：正确 ID 类型、ManyToOne 成对声明、无手动审计字段、驼峰严格一致 | 生成的规格出现已知错误模式 |

**后端小计 = Σ(D1-D10)**，满分 100pt。

---

## 前端评分维度（F1-F4，满分 40pt）

| # | 维度 | 权重 | 满分条件 | 常见扣分场景 |
|---|---|---|---|---|
| **F1** | **实现分支合规性** | 12pt | `sdd-frontend.md` §9 决策链完整（标准模板 → hook → 扩展视图 → Vue2 组件）；每个分支有明确进入条件和禁止条件；禁止"跳级"直接写 Vue2 组件而不经过优先级判断 | 缺少决策链；跳过 hook/扩展视图直接使用 Vue2；无进入条件说明 |
| **F2** | **节点与 selector 规范** | 10pt | 节点 id 来源有明确优先级（用户提供 > 菜单 key 推导 > 标准模板规则库 > 待确认）；无法确认的节点 id 标记"待确认"，**不得按文案猜测或自行拼接**；selector 写法（`attr`/`value`）符合平台格式 | 节点 id 凭按钮文案自拼；未标记"待确认"；selector 写法错误 |
| **F3** | **数据源与绑定规范** | 10pt | 所有 API 请求通过 IIDP `meta`/`api` 数据源或 `window.Tech.httpMeta`，**禁止引入 axios/fetch**；`bind_`/`bind_two_`/`bind_on_` 语法正确；服务 args 来自 `contracts.md`，不凭按钮文案推断；`bind_on_` 的事件名必须真实存在 | 使用 axios/fetch；bind_on_ 用了不存在的事件名；args 凭空推断 |
| **F4** | **权限与契约一致性** | 8pt | 每个操作按钮有 `auth` 字段，格式为 `{model_name}:{action}`，与 `contracts.md` 权限码列完全一致；无前端额外权限判断逻辑（禁止用前端 `v-if` 代替 auth）；`effectPaths` 取 product 配置，不随意添加 `effectPageIds` | 按钮缺 auth；权限码格式不符；前端硬编码权限判断 |

**前端小计 = Σ(F1-F4)**，满分 40pt。

---

## 总分

**总分 = 后端小计 + 前端小计**，满分 **140pt**。

> Hill-Climbing 时可分别对后端维度（D1-D10）和前端维度（F1-F4）独立优化，也可混合排序选最低分维度。

---

## 评分规则

- 每个维度打 0 到满分整数分
- **D10 / F-实测**：必须实际运行测试场景（不允许只凭文档推断），运行结果即证据
- 改进后总分必须**严格高于**改进前（Δ > 0）才保留，Δ = 0 视为未改进，丢弃
- 连续 2 轮 Δ < 2pt → Early Stop（局部最优）

---

## 独立法官 Prompt 模板

> 将以下 prompt 完整发送给独立 sub-agent（不带任何上下文，让其独立判断）：

```
你是 IIDP create-project skill 质量评审员。请阅读以下文件并按 14 维 rubric 打分（后端 D1-D10 共 100pt，前端 F1-F4 共 40pt，总分 140pt）：

【必须读取的文件】
后端：
1. skills/create-project/references/sdd-backend.md
2. skills/create-project/references/sdd-workflow.md
3. skills/create-project/commands/sdd-spec.md
4. skills/create-project/commands/sdd-contracts.md
前端：
5. skills/create-project/references/sdd-frontend.md
6. skills/create-project/references/sdd-frontend-interaction.md
评分基准：
7. skills/evolve/references/skill-rubric.md（本 rubric 定义）
8. skills/evolve/references/test-scenarios.md（测试场景）

【D10 后端实测要求】
用 test-scenarios.md 的场景 1（RuoYi-Vue-Pro 用户管理），模拟运行 /sdd-spec 流程，
检查生成的 backend-spec 模板中：
- ID 类型是否为 String（不是 Long）
- ManyToOne 是否有成对的 FK String 字段
- 是否出现手动声明的 create_user/create_date 等审计字段

【F1-F4 前端评分要求】
阅读 sdd-frontend.md，重点检查：
- F1：§9 决策链（标准模板 → hook → 扩展视图 → Vue2）是否完整，各分支有无进入/禁止条件
- F2：节点 id 来源优先级是否有明确说明，是否有"不得凭文案猜测"的禁止条款
- F3：是否有"禁止引入 axios/fetch"的明确禁令；bind_on_ 事件名约束是否存在；args 来源约束是否明确
- F4：auth 字段格式规范（{model_name}:{action}）是否与 contracts.md 对齐；是否有禁止前端硬编码权限判断的说明

用 test-scenarios.md 的场景 5（前端实测，见场景 5 节）模拟运行 /sdd-spec 前端规格流程，
检查生成的 frontend-spec 模板中节点 id 来源、数据源类型、按钮 auth 字段是否符合规范。

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
后端小计: [合计]/100
F1: [分数]/12 — [1-2句诊断说明]
F2: [分数]/10 — [1-2句诊断说明]
F3: [分数]/10 — [1-2句诊断说明]
F4: [分数]/8 — [1-2句诊断说明]
前端小计: [合计]/40
总分: [合计]/140
最低分维度: D[X]或F[X]（[维度名]，[分数]/[满分]）
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
| D10 后端实测表现 | 运行 `/sdd-spec`（test-scenarios.md 场景 1-4） | — |
| **F1 实现分支合规性** | `sdd-frontend.md` §9 | `sdd-workflow.md` |
| **F2 节点与 selector 规范** | `sdd-frontend.md` §6 | `sdd-frontend-interaction.md` |
| **F3 数据源与绑定规范** | `sdd-frontend.md` §10 | `sdd-contracts.md` |
| **F4 权限与契约一致性** | `sdd-frontend.md` §8 | `references/sdd-contracts.md` |
