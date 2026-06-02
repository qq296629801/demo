# 冒烟验证

## 与 create-project 验证的关系

先读取 `skills/create-project/references/sdd-validation.md`。本文件为生成的 IIDP 应用补充 evolve 专用评分和配置一致性门禁。

## 必需验证流程

对每个被评测仓库：

1. 从还原出的用户故事和测试用例派生 JSON-RPC 冒烟用例。
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

通常只有 `generation-gap`、可复现的 `config-gap` 和 `smoke-gap` 应驱动 `create-project` 修改。`environment-gap` 应停止比较，而不是产出误导性分数。

## 认证要求

IIDP 引擎强制 token 认证，未提供 token 时所有 JSON-RPC 调用返回 `{"code":7100,"message":"token不能为空"}`。

- 请求头：`Authorization: Bearer <token>`
- Token 来源：`rbac_token` 表的 `token` 字段，或 `apps.json` 中的 `apiToken` 字段
- 测试时可通过环境变量 `IIDP_API_TOKEN` 传入，避免硬编码
- Docker 冒烟测试前需确认 token 可用且未过期

当 token 不可用时，所有冒烟用例应标记为 `environment-gap`，不得归因于生成质量。

## JSON-RPC 用例要求

每个用例必须包含：

- `storyId`
- `caseId` 或用例 `name`
- JSON-RPC 2.0 request（必须包含 `params.service`，至少填写 `"search"` 等内置服务名）
- expected HTTP status
- expected result 或 expected error
- 到生成需求中的用户故事、测试用例或验收标准的追溯关系

最低服务覆盖应包含可用的 CRUD 风格行为：

- `search` or `find`
- `create`
- `update`
- `delete`
- 当源码需求中存在相关行为时，至少包含一个必填字段、权限、非法状态或记录不存在的负向用例

## 冒烟评分

使用 rubric 公式：

```text
smoke_points = round(20 * passed_cases / total_cases)
```

不要把跳过用例计为通过。如果应用无法启动，所有冒烟用例都失败。

## 证据要求

记录：

- 被比较的精确配置文件
- 脱敏后的凭据比较结果
- Docker 服务健康摘要
- 应用启动命令和结果
- JSON-RPC 用例通过和失败数量
- 每类失败的代表性响应体
- 最终评分贡献
