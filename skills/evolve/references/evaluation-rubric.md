# 评分 Rubric

## 评分总览

总分：100 分。评分前必须先通过 IIDP 合规门禁，门禁不通过直接 REVERT，不进入评分。

| 维度 | 分值 | 衡量内容 |
|---|---:|---|
| 代码与需求可追溯性 | 20 | 需求中的接口、字段、业务约束在生成代码中的覆盖率 |
| Skills 指令质量 | 15 | sdd-*.md 和 backend/frontend skills 的路由可达性、示例覆盖、用法完整度 |
| 生成应用可运行性 | 20 | Maven 构建、配置文件、应用启动、JSON-RPC 端点暴露 |
| Docker 环境一致性 | 10 | MySQL/Redis/MinIO/App 配置在 compose 与生成应用之间一致 |
| 冒烟测试通过率 | 30 | 从需求派生的 JSON-RPC 用例通过率（需求驱动的直接证明） |
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

来源：`skills/frontend/references/iidp-frontend-extension-dev/SKILL.md`

| 检查项 | grep 方式 |
|---|---|
| 禁止引入通用请求库 | 不含 `import axios`、`import fetch`、`require('axios')` |
| 禁止 `type: 'page'` 替换现有标准页 | 替换场景不含 `type: 'page'` |
| API 请求必须走 IIDP | 有 HTTP 请求时只使用 `window.Tech.httpMeta` 或 IIDP 数据源 |
| 节点 ID 来自规则库或用户提供 | 不允许编造节点 ID |

---

## 门禁级联规则

- Docker 环境一致性失败 → Docker 维度记 0 分，且本轮不能报告冒烟测试通过。
- 应用无法启动 → 生成应用可运行性最高 8 分，冒烟测试通过率记 0 分。
- 未还原出用户故事或测试用例 → 冒烟测试通过率最高 10 分。
- 改进修改了允许范围之外的文件 → 即使分数提升，该实验也无效。
- 只能比较同一基准 commit SHA 和同一本地环境下产生的分数。

---

## 维度细则

### 代码与需求可追溯性：20

- 5 分：需求定义的接口/服务，生成代码有对应 `@MethodService` 实现（接口覆盖率 ≥ 80%）
- 5 分：需求中的核心字段，在 `@Model`/`@Property` 中存在且类型正确（字段覆盖率 ≥ 80%）
- 5 分：需求中的业务约束（必填、唯一、状态值）在 `@Validate`/`@Selection`/常量中有体现
- 5 分：前端规格中的页面/操作，在 `app.json` 视图配置或前端扩展代码中有对应实现

### Skills 指令质量：15

衡量 `skills/create-project/`、`skills/backend/`、`skills/frontend/` 的指令文件是否足够专业：

- 4 分：**路由可达性**——`create-project` 引用 backend/frontend 能力时路径可达；`sdd-*.md` 之间交叉引用无断链；`SKILL.md` 子技能路由覆盖所有场景
- 4 分：**示例代码覆盖**——核心能力（模型关系、MethodService 分层、视图联动、前端扩展）有完整可执行示例，而非只有描述
- 4 分：**用法覆盖完整性**——`sdd-backend.md` 各能力域（model/method/view/contracts）使用场景完整；`sdd-frontend-interaction.md` 状态流转、弹窗、详情页有明确规则
- 3 分：**失败处理路径**——指令中有明确 if-then 回退路径（"若 X 则做 Y"），不能只写"注意"

### 生成应用可运行性：20

- 5 分：生成文件落在预期的 IIDP app/module 布局中
- 5 分：Maven build 命令成功
- 5 分：应用配置语法有效，能解析必需属性
- 5 分：应用启动，暴露预期 JSON-RPC 端点

### Docker 环境一致性：10

- 3 分：MySQL host、port、database、username、password 一致
- 2 分：Redis host、port、password 一致
- 2 分：MinIO endpoint、access key、secret key、bucket 一致
- 2 分：应用端口和 active profile 与 Docker 启动一致
- 1 分：证据展示被比较的精确文件和值，敏感信息脱敏

### 冒烟测试通过率：30

根据生成的 JSON-RPC 用例计算：

```text
smoke_points = round(30 * passed_cases / total_cases)
```

如果 `total_cases = 0`，记 0 分。用例必须可追溯到还原出的用户故事或测试用例。

### 证据链与可审查性：5

- 1 分：记录基准 URL 和 commit SHA
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
