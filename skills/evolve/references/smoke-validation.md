# 冒烟验证

## 与 create-project 验证的关系

先读取 `skills/create-project/references/sdd-validation.md`。本文件为生成的 IIDP 应用补充 evolve 专用评分和配置一致性门禁。

## 必需验证流程

对每个被评测的需求文档：

1. 从需求文档的用户故事和验收标准派生 JSON-RPC 冒烟用例（必须可追溯，见下方"冒烟用例追溯要求"）。
2. 启动前比较 Docker 配置和生成应用配置。
3. 当相关服务存在时，用 `docker compose up -d mysql redis minio minio-init` 启动依赖。
4. 使用项目标准 Maven 命令构建生成应用。
5. 启动 IIDP 应用容器或本地应用进程。
6. 运行 `tests/functional/smoke_test.py` 或等价 JSON-RPC runner。
7. 使用 `evaluation-rubric.md` 打分。

## 配置一致性门禁

比较以下文件中的值：

- `docker-compose.yml`
- `docker/config/application.properties`
- `docker/config/application-dev.properties`
- `docker/config/dbcp.properties`
- 生成的 IIDP 应用配置文件，包括 `application*.properties`、`application*.yml`、`.env` 和模块专用配置文件。

必需一致项：

| 范围 | 值 |
|---|---|
| MySQL | host、port、database name、username、password |
| Redis | host、port、password、database index（如使用） |
| MinIO | endpoint、access key、secret key、bucket（如配置） |
| App | port、active profile、context path 或 JSON-RPC endpoint |

如果某个值因为容器内部网络而有意不同，记录两个值及映射关系。例如：宿主机 `localhost:3306` 映射到容器服务 `mysql:3306`。

## 失败分类

每个失败用例归类为以下一种：

- `spec-gap`：需求或用户故事没有保留源码行为。
- `generation-gap`：`create-project` 未生成必需 IIDP 产物或代码。
- `config-gap`：Docker 与生成应用配置不一致。
- `startup-gap`：依赖或应用无法启动。
- `smoke-gap`：应用已启动，但 JSON-RPC 行为失败。
- `environment-gap`：本地 Docker、网络、Maven 或外部依赖导致无法进行可比较评测。

失败归因必须先映射到 evolve 的改进分类，再决定是否修改 skills：

- 可复现的 `generation-gap`、`config-gap` 或 `smoke-gap` 若根因是 SDD 模板、契约映射或验证规则表达不足，归为 `sdd-template-gap`，候选修改 `skills/create-project/`。
- 如果根因是已有 backend/frontend 规则未被加载，归为 `route-gap`，候选修改 `skills/create-project/` 或 skill 路由。
- 如果根因是 backend 私有规则缺失、示例不足或边界用法不清，且有本地文档、源码、日志、配置、测试或用户确认支撑，归为 `backend-doc-gap`，候选修改 `skills/backend/`。
- 如果根因是 frontend 私有规则缺失、组件规则不足或扩展协议不清，且有 `iidpDoc`、源码、日志、配置、测试或用户确认支撑，归为 `frontend-doc-gap`，候选修改 `skills/frontend/`。
- `environment-gap` 应停止比较，不产出误导性分数；资料不足时归为 `knowledge-gap`，只记录缺口和所需证据。

## 认证要求

IIDP 引擎强制 token 认证，未提供 token 时所有 JSON-RPC 调用返回 `{"code":7100,"message":"token不能为空"}`。

- 请求头：`Authorization: Bearer <token>`
- Token 来源：`rbac_token` 表的 `token` 字段，或 `apps.json` 中的 `apiToken` 字段
- 测试时可通过环境变量 `IIDP_API_TOKEN` 传入，避免硬编码
- Docker 冒烟测试前需确认 token 可用且未过期

当 token 不可用时，所有冒烟用例应标记为 `environment-gap`，不得归因于生成质量。

## 冒烟用例追溯要求

**冒烟用例必须可追溯到需求文档**，不得凭空编写。每个用例的 `trace` 字段至少包含：

- 需求文档中的功能模块或用户故事编号（对应 `us` 字段）
- 对应的验收标准条目（对应 `ac` 字段）
- 推导出的测试用例描述（对应 `tc` 字段）
- 文档章节引用（对应 `doc_section` 字段，格式：`"第 X.Y 章 <模块名>"`）

若需求文档没有显式的 `US-*` / `AC-*` 编号，按 `requirements-doc-guide.md` § 从文档派生冒烟用例 中的规则自动生成编号。

**不得**：
- 跳过追溯直接编写测试用例
- 以源码行为代替需求文档验收标准作为用例依据
- 用例通过但缺少 `trace` 的，冒烟得分正常计算，但追溯矩阵标为 `partial`

## JSON-RPC 用例要求

每个用例必须包含：

- `storyId`
- `caseId` 或用例 `name`
- `trace`：至少包含 `us`、`fr`、`ac`、`tc` 中的一个；推荐完整填写 `US-*` / `FR-*` / `AC-*` / `TC-*`
- JSON-RPC 2.0 request（必须包含 `params.service`，至少填写 `"search"` 等内置服务名）
- expected HTTP status
- expected result 或 expected error
- 到生成需求中的用户故事、功能需求、验收标准或测试用例的追溯关系；缺少 `trace` 的通过用例不能为“代码与需求可追溯性”拿满分

最低服务覆盖应包含可用的 CRUD 风格行为：

- `search` or `find`
- `create`
- `update`
- `delete`
- 当源码需求中存在相关行为时，至少包含一个必填字段、权限、非法状态或记录不存在的负向用例

## 冒烟评分

使用 rubric 公式：

```text
smoke_points = round(35 * passed_cases / total_cases)
```

不要把跳过用例计为通过。如果应用无法启动，所有冒烟用例都失败。

通过率只计算执行结果；追溯完整性另按 `evaluation-rubric.md` 的“代码与需求可追溯性”评分。也就是说，用例通过但缺少 `trace.ac` 或 `trace.tc` 时，冒烟测试可以按通过计数，但追溯矩阵中对应行只能标为 `partial`。

推荐 JSON-RPC case 片段：

```json
{
  "storyId": "US-001",
  "caseId": "TC-BE-001",
  "name": "create-student-success",
  "trace": {
    "us": "US-001",
    "fr": "FR-001",
    "ac": "AC-001",
    "tc": "TC-BE-001"
  },
  "request": {
    "id": "guid-001",
    "jsonrpc": "2.0",
    "method": "service",
    "params": {
      "model": "demo_student",
      "service": "create",
      "args": {
        "valuesList": [{ "name": "张三" }]
      }
    }
  },
  "expectedHttpStatus": 200,
  "expectedResult": true
}
```

## 证据要求

记录：

- 被比较的精确配置文件
- 脱敏后的凭据比较结果
- Docker 服务健康摘要
- 应用启动命令和结果
- JSON-RPC 用例通过和失败数量
- 每类失败的代表性响应体
- 最终评分贡献
