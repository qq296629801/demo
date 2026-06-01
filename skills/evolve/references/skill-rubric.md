# create-project Skill 诊断 Rubric（14 维，D1-D10/F1-F4）

> **使用定位（v3）**：本文件是 **失败 TC 根因定位工具**，不再是主评分指标。
>
> 主指标是 `smoke_test.py` 通过率（见 `skill-evolve.md`）。
> 当冒烟测试出现失败 TC 时，用下方"TC 失败模式 → 诊断维度 → 对应 skill 文件"快速表定位修改目标。
> 修改决策仍由通过率 Δ > 0 决定，不由 rubric 分数驱动。
>
> （原评分功能已降级为可选的定性参考，独立法官 prompt 保留在文末供回归分析使用。）

---

## 快速诊断表：TC 失败模式 → 维度 → 修改目标

| TC 失败现象 | 诊断维度 | 修改目标（create-project skill） | 平台文档依据 |
|---|---|---|---|
| 生成的 backend-spec 有 `Long id` / `List<Long>` | D2 字段规范性 | `sdd-backend.md` §3 模型设计表 | `backend/references/core/model-property-advanced.md` — id 类型规范 |
| 生成的 backend-spec 有 `create_user`/`create_date` 字段声明 | D2 字段规范性 | `sdd-backend.md` §3 审计字段规则 | `backend/references/core/model.md` §@Model.isAutoLog |
| ManyToOne 只有 ORM 对象字段，无 FK String 字段 | D6 ER 关系设计 | `sdd-backend.md` §3 ManyToOne 规则 | `backend/references/core/model.md` — ManyToOne 成对示例 |
| 生成代码用 Spring `@Service` 而非 `@MethodService` | D3 平台合规性 | `sdd-backend.md` §4 服务注解规范 | `backend/references/core/annotation-scope.md` — @MethodService 作用域 |
| 生成代码用 Spring `@Autowired`/`@Component` | D3 平台合规性 | `sdd-backend.md` §4 注解黑名单 | `backend/references/core/annotation-scope.md` — 禁用注解列表 |
| contracts.md 缺失时流程无停止提示 | D4 失败机制编码 | `sdd-contracts.md` / `sdd-spec.md` 前置检查 | — |
| smoke_test 的 search/create/update/delete 返回非预期结构 | D8 跨步骤一致性 | `sdd-backend.md` ↔ `sdd-contracts.md` 入参签名对比 | `backend/references/complete/api-params.md` §2 — 内置服务入参 |
| 缺少 approve/reject 等状态变更服务，状态 TC 全失败 | D9 测试可验性 | `sdd-backend.md` §4 状态机服务 | `backend/references/core/method-service.md` — 状态变更服务规范 |
| 分页使用 `pageNum`/`pageSize` 入参或返回 `Map{total,list}` 而非 `limit`/`offset` + `result.data` | D8 跨步骤一致性 | `sdd-backend.md` §4 查询逻辑 | `backend/references/complete/api-params.md` §2 — limit/offset/result.data |
| 生成的 frontend-spec 直接写 Vue2 组件跳过 hook/扩展视图 | F1 实现分支合规性 | `sdd-frontend.md` §9 决策链 | `frontend/references/iidp-frontend-dev-manual/…/03.扩展说明/02.扩展类型.md` |
| 节点 id 凭按钮文案自拼，未标记"待确认" | F2 节点规范性 | `sdd-frontend.md` §6 节点 id 来源 | `frontend/references/iidp-frontend-dev-manual/…/06.框架/01.节点 node.md` |
| 数据源使用 axios/fetch，非 IIDP meta/api 数据源 | F3 数据源规范 | `sdd-frontend.md` §10 数据源规范 | `frontend/references/iidp-frontend-dev-manual/…/06.框架/02.数据源.md` |
| 按钮缺 auth 字段，或格式不是 `{model_name}:{action}` | F4 权限契约一致性 | `sdd-frontend.md` §8 按钮权限表 | `backend/references/core/security-permission-i18n.md` — 按钮级权限规范 |
| 指令文字有"建议/可以/根据情况"等软化措辞导致 AI 行为不确定 | D5 可操作性 | 对应 `commands/*.md` 失败步骤 | — |
| 生成的 backend-spec 字段驼峰大小写与 `set()`/`getStr()` 不一致 | D2 字段规范性 | `sdd-backend.md` §3 驼峰规则 | `backend/references/core/platform-standards.md` — 字段名大小写规范 |

> **路径前缀说明**：`backend/` = `skills/backend/`，`frontend/` = `skills/frontend/`

---

---

## 诊断维度详表（仅供根因分析参考）

### 后端维度（D1-D10，满分 100pt）

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

### 前端维度（F1-F4，满分 40pt）

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

---

## 独立法官 Prompt 模板（可选，用于回归分析）

> **说明**：以下 prompt 不再是 hill-climbing 的主决策依据。仅在需要对 skill 文档做全量诊断（如月度回归）时使用。
> 日常进化循环直接用上方快速诊断表定位失败 TC 的根因即可。

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

【D1/D5 静态检查要求（场景 7）】
阅读以下文件，按场景 7 检查点逐一核查：
- skills/create-project/references/sdd-workflow.md
- skills/create-project/commands/sdd-spec.md
- skills/create-project/commands/sdd-contracts.md
- skills/create-project/commands/sdd-tasks.md
- skills/create-project/commands/sdd-validate.md
D1 检查：每个 Step 是否有明确的输入/输出声明，是否存在 [TODO]/待补充/详见后续等空话占位符
D5 检查：统计"建议/可以考虑/根据情况/灵活把握/尽量/一般来说"等软化措辞出现次数，每处扣 1pt

【D4/D7 失败分支检查要求（场景 6）】
阅读 skills/create-project/commands/sdd-spec.md 和 sdd-contracts.md，检查：
- 是否有"若 contracts.md 不存在 → 停止"等显式 if-then 失败分支（D4）
- 禁令是否用"禁止/停止"而非"注意/建议"，是否附有违反后果说明（D7）

【D8 跨步骤一致性检查要求（场景 8）】
对比 sdd-backend.md 和 references/sdd-contracts.md 中同一字段（如外键 ID、状态枚举）的类型声明和示例格式，
检查是否存在类型不一致或命名漂移（如一处 String 一处 Long，或方法名拼写不同）

【F1-F4 前端评分要求】
阅读 sdd-frontend.md，重点检查：
- F1（场景 5/9）：§9 决策链完整性（标准模板 → hook → 扩展视图 → Vue2），hook 路径须有 return params 说明，
  扩展视图路径须禁止用 replace 替换整页
- F2（场景 5）：节点 id 来源优先级说明，是否有"不得凭文案猜测"的明确禁止条款
- F3（场景 5/10）：是否有"禁止引入 axios/fetch"明确禁令；reqPrep/reqAfter 签名和 return 规则是否完整；
  bind_on_ 事件名约束是否存在；service args 来源约束是否明确
- F4（场景 5）：auth 字段格式（{model_name}:{action}）及与 contracts.md 对齐说明；禁止前端硬编码权限判断

用 test-scenarios.md 的场景 5 模拟运行 /sdd-spec 前端规格流程，
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

## 维度与文件映射

> **两类文件区分**：
> - **平台文档**（`/backend` / `/frontend`）：规范的原始出处，判断对错的唯一依据
> - **修改目标**（`create-project/references/` 或 `commands/`）：create-project skill 中需要修正的文件

| 维度 | 平台文档（规范来源） | 修改目标（create-project skill） |
|---|---|---|
| D1 模板完整性 | — | `sdd-workflow.md`、所有 `commands/*.md` |
| D2 字段规范性 | `backend/references/core/model.md`（isAutoLog、@Validate）<br>`backend/references/core/model-property-advanced.md`（id 类型、ManyToOne）<br>`backend/references/core/platform-standards.md`（驼峰规范） | `sdd-backend.md` §3 |
| D3 平台合规性 | `backend/references/core/annotation-scope.md`（@MethodService vs @Service）<br>`backend/references/core/platform-standards.md`（禁用注解黑名单） | `sdd-backend.md` §4 |
| D4 失败机制编码 | — | 所有 `commands/*.md`、`sdd-workflow.md` |
| D5 可操作性 | — | `sdd-backend.md`、`sdd-frontend.md`、所有 `commands/*.md` |
| D6 ER 关系设计 | `backend/references/core/model.md`（ManyToOne 成对示例）<br>`backend/references/core/model-property-advanced.md`（OneToMany/ManyToMany） | `sdd-backend.md` §3 |
| D7 约束执行力 | — | `sdd-backend.md`、所有 `commands/*.md` |
| D8 跨步骤一致性 | `backend/references/complete/api-params.md`（内置服务入参/返回结构） | `sdd-backend.md` ↔ `sdd-contracts.md` |
| D9 测试可验性 | `backend/references/core/method-service.md`（状态机服务） | `sdd-validate.md`、`sdd-tasks.md` |
| D10 后端实测表现 | — | 运行 `/sdd-spec`（test-scenarios.md 场景 1-4） |
| **F1 实现分支合规性** | `frontend/references/iidp-frontend-dev-manual/…/03.扩展说明/02.扩展类型.md` | `sdd-frontend.md` §9 |
| **F2 节点与 selector 规范** | `frontend/references/iidp-frontend-dev-manual/…/06.框架/01.节点 node.md` | `sdd-frontend.md` §6 |
| **F3 数据源与绑定规范** | `frontend/references/iidp-frontend-dev-manual/…/06.框架/02.数据源.md` | `sdd-frontend.md` §10 |
| **F4 权限与契约一致性** | `backend/references/core/security-permission-i18n.md`（按钮级权限规范） | `sdd-frontend.md` §8、`sdd-contracts.md` |
