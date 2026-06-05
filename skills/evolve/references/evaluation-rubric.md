# 评分 Rubric

## 评分总览

总分：100 分。评分前必须先通过 IIDP 合规门禁，门禁不通过直接 REVERT，不进入评分。

本 Rubric 的目标是评估 **skills 修改是否能提高 IIDP 代码生成质量**。因此 `Skills 指令质量` 和 `冒烟测试通过率` 是主权重；其他维度只保留足够的运行、环境、追溯和审查底线，避免分数被"文档看起来完整但生成结果不好"稀释。

| 维度 | 分值 | 衡量内容 |
|---|---:|---|
| Skills 指令质量 | 30 | skills 是否能被正确触发、按需加载，并把大模型收拢到 IIDP 规范路径 |
| 冒烟测试通过率 | 35 | 从需求派生的 JSON-RPC 用例通过率，是 skills 修改是否真的改善生成结果的主证明 |
| 代码与需求可追溯性 | 15 | 需求中的接口、字段、业务约束在契约、代码、测试和结果中的覆盖率 |
| 生成应用可运行性 | 10 | Maven 构建、配置文件、应用启动、JSON-RPC 端点暴露 |
| Docker 环境一致性 | 5 | MySQL/Redis/MinIO/App 配置在 compose 与生成应用之间一致 |
| 证据链与可审查性 | 5 | 日志、diff、评分表、commit SHA、IIDP 合规检查结果完整 |

---

## IIDP 合规门禁（一票否决）

打分前先执行以下检查。**任意一项不通过，本轮直接 REVERT，不进入评分。**

### 后端合规

来源：`skills/backend/references/core/platform-standards.md`

| 检查项 | grep 方式 |
|---|---|
| 禁止 Spring Web 注解 | 生成 Java 代码不含 `@RestController`、`@RequestMapping`、`@GetMapping`、`@PostMapping`、`@Controller` |
| 禁止 Spring 原生数据访问 | 不含 `@Repository`；模型类不含 `@Autowired`、`@Component`、`@Bean`（只允许 `@InjectMeta`） |
| 必须有 IIDP 核心注解 | 含 `@Model` 或 `@MethodService` 至少一个 |
| `@Service` 只用于 SdkService 子类 | `@Service` 出现时，类必须 `extends SdkService` |

### 前端合规

来源：`skills/frontend/references/iidp-frontend-codegen-protocol.md`

| 检查项 | grep 方式 |
|---|---|
| 禁止引入通用请求库 | 不含 `import axios`、`import fetch`、`require('axios')` |
| 禁止 `type: 'page'` 替换现有标准页 | 替换场景不含 `type: 'page'` |
| API 请求必须走 IIDP | 有 HTTP 请求时只使用 `window.Tech.httpMeta` 或 IIDP 数据源 |
| 节点 ID 来自规则库或用户提供 | 不允许编造节点 ID |

---

## 门禁级联规则

- Docker 环境一致性失败 → Docker 维度记 0 分，且本轮不能报告冒烟测试通过。
- 应用无法启动 → 生成应用可运行性最高 4 分，冒烟测试通过率记 0 分。
- 需求文档未通过质量门控（总分 < 6 或数据模型 = 0）→ 本次运行无效，停止评分。
- 未从需求文档派生出用户故事或测试用例 → 冒烟测试通过率最高 12 分。
- 改进修改了允许范围之外的文件 → 即使分数提升，该实验也无效。
- 只能比较同一基准文档（相同 hash）和同一本地环境下产生的分数。

---

## 维度细则

### Skills 指令质量：30

衡量 `skills/create-project/`、`skills/backend/`、`skills/frontend/` 是否能让大模型在正确时机加载正确规则，并稳定生成符合 IIDP 规范、不遗漏关键产物的代码。本维度是 evolve 的核心评分项：如果一次修改不能让模型更准确地使用 skills，即使文档更完整，也不能拿高分。

- 5 分：**触发匹配质量**——`SKILL.md` frontmatter 的 `description` 必须描述"何时使用"，覆盖用户可能说法，如"生成 IIDP 应用"、"SDD"、"后端规格"、"前端扩展"、"验收"、"公共规格维护"等；不能只写 skill 做什么。skill 文档用法介绍不专业、导致模型无法匹配用户需求，按本项扣分。
- 4 分：**路由与按需加载质量**——`create-project → backend/frontend 子 skill → references` 路径必须可达；不同任务能路由到正确专题；长文档必须有索引，避免要求模型一次读完整个 reference。模型没有加载 backend/frontend 子 skill，按本项扣分。
- 5 分：**方向收敛与禁止项质量**——必须用优先级和禁止项把模型收拢到 IIDP 路径：先标准模板/在线视图，再 hook，再扩展视图，最后才自定义 Vue2；先后端模型/视图/菜单/服务契约，再前端扩展；禁止"为了方便"改走通用 Web 范式。只写"建议/注意"但没有明确"禁止/必须/优先级"时，按本项扣分。
- 5 分：**IIDP 合规约束质量**——必须明确禁止通用 Spring REST、JPA Repository、axios/fetch、自定义后台页滥用、猜节点 id 等反模式，并说明正确 IIDP 替代路径：`@Model`、`@MethodService`、`views/*.json`、菜单/数据登记、标准模板、hook、扩展视图、IIDP 数据源。
- 4 分：**示例与输入输出契约质量**——`@Model`、`@Property`、`@MethodService`、`views/*.json`、`app.json`、`menus.json`、frontend hook、扩展视图必须有可复制示例；每个示例说明输入、输出文件和验证方式。示例不足导致漏 `app.json`、视图或菜单登记，按本项扣分。
- 4 分：**遗漏防护与门控质量**——必须明确生成 `requirements.md`、`contracts.md`、`backend-spec.md`、`frontend-spec.md`、`plan.md`、`tasks.md`、`validation.md`；缺失事实写 `待确认`；进入实现前必须有 Git 分支检查和 tasks 勾选规则。
- 3 分：**压力场景可验证性**——至少覆盖 3 类 pressure scenarios：传统后台 CRUD 应走标准模板/在线视图；跨模型审批服务应走 backend `@MethodService`、事务、权限和 validation；已有页面加按钮应走 frontend 扩展视图或 hook，而不是新建 page。

**扣分示例：**

```markdown
用户说："做一个 IIDP 学生管理页面"

扣分链路：
- `SKILL.md` description 没覆盖 "IIDP 应用 / 页面 / 管理后台 / 前端扩展" → 触发匹配质量扣分
- 模型未加载 `create-project` 或 `frontend` 子 skill → 路由与按需加载质量扣分
- skill 未明确"标准模板优先，禁止先写自定义 Vue 页面" → 方向收敛与禁止项质量扣分
- 最终生成自定义 Vue 页面或 Spring REST Controller → IIDP 合规约束质量扣分
- 产物缺少 `app.json`、`views/*.json` 或 `menus.json` 示例映射 → 示例与输入输出契约质量扣分
```

### 冒烟测试通过率：35

根据生成的 JSON-RPC 用例计算：

```text
smoke_points = round(35 * passed_cases / total_cases)
```

如果 `total_cases = 0`，记 0 分。用例必须可追溯到需求文档中的用户故事或验收标准。冒烟测试是本 Rubric 的结果主指标：skills 修改最终必须体现在更多需求驱动用例通过，而不是只体现在文档表述更丰富。

### 代码与需求可追溯性：15

用追溯矩阵评分，不凭印象判断。矩阵从 `requirements.md` 的 `FR-*` 和可验收 `AC-*` 出发，逐行检查是否能追到契约、规格、任务、代码、测试和结果。

**追溯矩阵列：**

| 列 | 内容 |
|---|---|
| `Requirement` | `FR-*` 或 `AC-*`，可附所属 `US-*` |
| `Contract` | `contracts.md` 中的模型属性、ER 字段、服务签名、权限码、视图 key、菜单 key |
| `Spec` | `backend-spec.md` / `frontend-spec.md` / `interaction-spec.md` 中的对应章节 |
| `Task` | `tasks.md` 中实现或验证该需求的任务项 |
| `Code File` | 生成的 Java 模型、服务、视图 JSON、`app.json`、菜单/数据文件或前端扩展文件 |
| `TC` | `validation.md` 中覆盖该需求的 `TC-BE-*` / `TC-FE-*` |
| `Smoke Case` | JSON-RPC 冒烟用例的 `caseId` 或 `name` |
| `Result` | `pass` / `fail` / `missing` / `partial` / `blocked`，并写明原因 |

**评分细则：**

- 2 分：需求侧 ID 完整。`requirements.md` 中 `US-*`、`FR-*`、`AC-*` 稳定、可枚举；若需求只有自然语言段落、无法形成矩阵，最多 1 分。
- 3 分：需求到契约/规格可追溯。字段、服务、权限、视图/菜单 key 能回指 `FR-*` 或 `AC-*`；若某个 FR 只出现在需求里，未进入 `contracts.md`、`backend-spec.md` 或 `frontend-spec.md`，该 FR 对应行标记 `missing` 并扣分。
- 4 分：规格到生成代码可追溯。`@Model`、`@Property`、`@MethodService`、`views/*.json`、`app.json`、菜单/数据文件或前端扩展文件覆盖对应需求；测试通过但找不到对应代码文件的需求不得拿满分。
- 3 分：验收到测试可追溯。每条可验收 `AC-*` 应追到 `validation.md` 的 `TC-*`，再追到 JSON-RPC case；测试用例通过但没有 `AC-*` / `TC-*` 追溯关系时，只能记为 `partial`，不得按通过用例给满分。
- 3 分：追溯证据完整。矩阵必须保留 `missing`、`partial`、`blocked` 行及原因，不能删除失败项来提高覆盖率；阻塞项必须说明是规格缺口、生成缺口、环境缺口还是人工待确认。

**示例：**

```markdown
| Requirement | Contract | Spec | Task | Code File | TC | Smoke Case | Result |
|---|---|---|---|---|---|---|---|
| FR-001 / AC-001 | `createStudent(valuesList)` | `backend-spec.md § 服务设计` | `后端·模型 Student` | `Student.java`, `student_view.json` | TC-BE-001 | `create-student-success` | pass |
| FR-003 | — | — | — | — | — | — | missing：需求未进入契约、任务和代码 |
```

上例中 `FR-003` 即使其他用例通过，也必须在“需求到契约/规格”“规格到生成代码”“验收到测试”中扣分。

### 生成应用可运行性：10

- 2 分：生成文件落在预期的 IIDP app/module 布局中
- 3 分：Maven build 命令成功
- 2 分：应用配置语法有效，能解析必需属性
- 3 分：应用启动，暴露预期 JSON-RPC 端点

### Docker 环境一致性：5

- 1 分：MySQL host、port、database、username、password 一致
- 1 分：Redis host、port、password 一致
- 1 分：MinIO endpoint、access key、secret key、bucket 一致
- 1 分：应用端口和 active profile 与 Docker 启动一致
- 1 分：证据展示被比较的精确文件和值，敏感信息脱敏

### 证据链与可审查性：5

- 1 分：记录基准文档标题、来源 URL 和文档 hash
- 1 分：记录生成的 spec/app 路径
- 1 分：汇总 Docker 和冒烟测试日志
- 1 分：记录变更文件和 git commit SHA
- 1 分：IIDP 合规门禁每项 grep 命令 + 实际输出

---

## 接收决策

只有同时满足以下两个条件时才接收实验：

```text
IIDP 合规门禁全部通过
AND new_benchmark_score > previous_benchmark_score
```

分数相等不接收。只在样本池仓库上观察到的提升只能作为诊断信号，不足以接收改动。
