# IIDP 实现、验证与复盘

## 测试用例规格（从 AC 派生）

### 触发时机

Step 1.5a（backend-spec.md 生成后）立即执行 AC 提取，将 `requirements.md` 验收标准转化为结构化测试用例，写入当前 feature 的 `validation.md` 测试用例规格节。tasks.md 中的测试任务块通过 TC-ID 与此节对应。

### AC → TC 提取 Prompt

读取 `requirements.md` 中每条验收标准（AC），按以下结构输出到 `validation.md` 的测试用例规格节：

```
TC-<编号>  <用例名称>
前置条件：<环境或数据依赖>
操作步骤：<调用的服务 / 方法>
期望结果：<成功或失败的断言>
覆盖 AC：<对应 AC 编号>
```

---

## 静态检查

静态检查在代码生成后、运行测试前执行，通过数量对比和格式扫描发现明显遗漏。

### 数量检查

比对规格文档与生成代码之间的数量是否严格一致：

| 检查项 | 规格来源 | 代码来源 | 失败条件 |
|--------|---------|---------|---------|
| 模型类数量 | `backend-spec.md` 中声明的模型数 | `grep -r "@Model" src/main --include="*.java" \| wc -l` | 不相等 |
| 服务方法数量 | `backend-spec.md` 中每个模型声明的方法数 | `grep -r "@MethodService" src/main --include="*.java" \| wc -l` | 不相等 |
| 测试方法数量 | 每个业务类的 public 方法数 | `*Test.json` 中的顶层键数量 | 不相等 |
| 测试用例数量 | `validation.md` 中的 TC 总数 | `*Test.json` 所有数组元素之和 | 少于 TC 数 |

### 格式检查

扫描以下规范违规点，任意一项不符合则标记失败：

- `@Model` 类必须包含 `tableName` 属性（`@Model(tableName = "...")`）
- 每个 public 业务方法必须有对应的 `@DDTest` 测试方法
- `*Test.json` 顶层结构必须是 `{ "methodName": [ {...} ] }`，每个元素包含 `displayName`、`args`、`expected`
- 测试类必须标注 `@IIDPTest`，测试方法必须标注 `@DDTest`
- `expected` 字段不能为空对象 `{}`

---

## 单元测试（TDD 红-绿-重构循环）

遵循 `tdd.md` 铁律：**没有失败的测试，就没有生产代码**。

### 循环步骤

1. **红色** — 针对一个业务场景，先编写 `*Test.java` + `*Test.json`（仅一个测试用例），运行 `mvn test -pl <模块>`，确认测试**失败**（不是报错）。
2. **绿色** — 编写最少的生产代码使测试通过。运行 `mvn test`，确认通过且无其他回归。
3. **重构** — 仅在绿色之后清理重复、改善命名。保持测试绿色。重复循环直至覆盖全部场景。

### DDTest 框架规范（来自 `testing.md`）

**覆盖率要求：**
- Jacoco 行覆盖率 ≥ 90%，通过 `mvn verify` 验证
- 每个 public 方法至少覆盖：正常路径、异常路径、边界值、业务规则（各 ≥ 1 条）

**测试类结构：**

```java
@IIDPTest(value = true, engine = true)  // value=true: 含 BaseModel；engine=true: 含 getMeta()
class ExampleTest {

    @DDTest
    void methodName(
            @DDArgs String param1,
            @DDArgs int param2,
            @DDExpected SomeResult result,
            @DDExpected ExpectedError error) {

        SomeModel model = new SomeModel();
        // 仅在检测到 getMeta().get(...).call(...) 时配置 Mock
        RecordSet mockRs = RecordSetMock.spy("model_name");
        doReturn(someValue).when(mockRs).call(eq("methodName"), anyString());

        try {
            Object actual = model.methodName(param1, param2);
            assertThat(actual).isEqualTo(result);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(ValidationException.class)
                         .hasMessageContaining(error.getMessage());
        }
    }
}
```

**测试数据 JSON 结构：**

```json
{
  "methodName": [
    {
      "displayName": "正常路径描述",
      "args": { "param1": "value", "param2": 1 },
      "expected": { "result": { "id": "xxx" } }
    },
    {
      "displayName": "异常路径描述",
      "args": { "param1": "", "param2": -1 },
      "expected": { "error": { "message": "参数不合法" } }
    }
  ]
}
```

**产物位置：**
- `src/test/java/<包路径>/<类名>Test.java`
- `src/test/resources/<包路径>/<类名>Test.json`

---

## 功能测试（Docker 冒烟测试）

功能测试验证用户故事的端到端流程，通过 JSON-RPC 调用运行中的服务进行断言。

### 环境准备

使用项目根目录的 `docker-compose.yml` 启动基础设施：

```bash
# 1. 启动依赖服务（MySQL 8.0 / Redis 7 / MinIO）
docker compose up -d mysql redis minio minio-init

# 2. 等待所有服务健康
docker compose ps

# 3. 构建应用镜像
mvn package -DskipTests

# 4. 启动应用
docker compose up -d iidp-app
```

默认连接信息（来自 `.env.example`）：

| 服务 | 地址 | 账号 | 密码 |
|------|------|------|------|
| MySQL | `localhost:3306` 数据库 `iidp_demo` | `iidp` | `iidp123456` |
| Redis | `localhost:6379` | — | `redis` |
| MinIO | `http://localhost:9000` | `snest` | `12345678` |
| 应用 | `http://localhost:8060/root/rpc/service/master` | — | Bearer token（rbac_token 表） |

### JSON-RPC 请求规范

所有请求遵循 JSON-RPC 2.0 协议（来自 `api-params.md` / `api-filter-sql.md`）：

```json
{
  "id": "<uuid>",
  "jsonrpc": "2.0",
  "method": "service",
  "params": {
    "model": "<模型名>",
    "service": "<服务名>",
    "tag": "master",
    "app": "<应用名>",
    "context": { "uid": "", "lang": "zh_CN" },
    "args": {
      "filter": [],
      "properties": ["id", "name"],
      "limit": 10,
      "offset": 0,
      "order": "id desc",
      "ids": [],
      "values": {},
      "valuesList": []
    }
  }
}
```

**内置服务参数对应关系：**

| 服务 | 使用的 args 字段 |
|------|----------------|
| `search` | `filter` `properties` `limit` `offset` `order` |
| `create` | `valuesList` |
| `update` | `ids` `values` |
| `delete` | `ids` |
| `find` | `ids`(查单条) 或 `filter` `limit` `offset` `order` |
| `count` | `filter` |

**Filter 表达式规范（来自 `filter.md`）：**

```json
// 单条件
[["status", "=", "active"]]

// 多条件 AND（默认，可省略 "&"）
[["status", "=", "active"], ["type", "=", "student"]]

// OR 条件（波兰前缀表达式）
["|", ["status", "=", "active"], ["status", "=", "pending"]]

// 混合 AND + OR
["&", ["state", "=", "confirm"], "|", ["uid", "=", "1"], ["uid", "=", false]]
```

常用操作符：`=` `!=` `>` `>=` `<` `<=` `like` `ilike` `not like` `not ilike` `in` `not in` `child_of` `parent_of`

**响应结构：**

```json
// 成功
{ "id": "guid", "jsonrpc": "2.0", "result": { "data": [...] } }

// 失败
{ "id": "guid", "jsonrpc": "2.0", "error": { "code": 7100, "message": "...", "data": { "debug": "..." } } }
```

### JSON-RPC 测试文件格式

在 `tests/functional/jsonrpc/` 下，每个用户故事对应一个 JSON 文件，TC-ID 与 `validation.md` 中的用例对应：

```json
{
  "storyId": "US-001",
  "description": "用户故事描述（来自 requirements.md）",
  "endpoint": "http://localhost:8060/root/rpc/service/master",
  "token": "<从 rbac_token 表获取或设环境变量 IIDP_API_TOKEN>",
  "cases": [
    {
      "name": "TC-001 正常 search 流程",
      "request": {
        "id": "guid-001",
        "jsonrpc": "2.0",
        "method": "service",
        "params": {
          "model": "example_student",
          "service": "search",
          "tag": "master",
          "app": "sie-iidp-demo-example",
          "context": { "uid": "", "lang": "zh_CN" },
          "args": {
            "filter": [["status", "=", "active"]],
            "properties": ["id", "name", "status"],
            "limit": 10,
            "offset": 0,
            "order": "id desc"
          }
        }
      },
      "expectedHttpStatus": 200,
      "expectedResult": true
    },
    {
      "name": "TC-002 create 新增记录",
      "request": {
        "id": "guid-002",
        "jsonrpc": "2.0",
        "method": "service",
        "params": {
          "model": "example_student",
          "service": "create",
          "tag": "master",
          "app": "sie-iidp-demo-example",
          "context": { "uid": "", "lang": "zh_CN" },
          "args": {
            "valuesList": [{ "name": "测试学生", "status": "active" }]
          }
        }
      },
      "expectedHttpStatus": 200,
      "expectedResult": true
    },
    {
      "name": "TC-003 异常 - 必填字段缺失",
      "request": {
        "id": "guid-003",
        "jsonrpc": "2.0",
        "method": "service",
        "params": {
          "model": "example_student",
          "service": "create",
          "tag": "master",
          "app": "sie-iidp-demo-example",
          "context": { "uid": "", "lang": "zh_CN" },
          "args": {
            "valuesList": [{}]
          }
        }
      },
      "expectedHttpStatus": 200,
      "expectedError": true
    }
  ]
}
```

每条 AC 映射为 ≥ 1 个 case，正常路径和异常路径都要覆盖。

### 冒烟测试脚本

创建 `tests/functional/smoke_test.py`，扫描所有测试文件并执行断言：

```python
import requests, json, sys, pathlib, uuid, os

JSONRPC_PATH = pathlib.Path("tests/functional/jsonrpc")
failures = []
total = 0

# 从环境变量或测试文件 spec 中获取 token，优先使用环境变量
AUTH_TOKEN = os.environ.get("IIDP_API_TOKEN")

for f in sorted(JSONRPC_PATH.glob("*.json")):
    spec = json.loads(f.read_text(encoding="utf-8"))
    endpoint = spec.get("endpoint", "http://localhost:8060/root/rpc/service/master")
    token = spec.get("token") or AUTH_TOKEN
    headers = {"Authorization": f"Bearer {token}"} if token else {}
    for case in spec["cases"]:
        total += 1
        req = dict(case["request"])
        req["id"] = str(uuid.uuid4())
        try:
            resp = requests.post(endpoint, json=req, headers=headers, timeout=30)
            body = resp.json()
        except Exception as e:
            print(f"FAIL: {spec['storyId']} / {case['name']}  error={e}")
            failures.append(case["name"])
            continue

        ok = resp.status_code == case.get("expectedHttpStatus", 200)
        # IIDP 响应始终包含 result 和 error 两个 key，成功时 error 为 null，失败时 result 为 null
        if case.get("expectedResult"):
            ok = ok and body.get("error") is None
        if case.get("expectedError"):
            ok = ok and body.get("error") is not None

        tag = f"{spec['storyId']} / {case['name']}"
        if ok:
            print(f"PASS: {tag}")
        else:
            print(f"FAIL: {tag}  status={resp.status_code}  body={json.dumps(body, ensure_ascii=False)}")
            failures.append(tag)

print(f"\n结果: {total - len(failures)}/{total} 通过，{len(failures)} 失败")
sys.exit(1 if failures else 0)
```

运行方式：

```bash
pip install requests  # 如未安装
python tests/functional/smoke_test.py
```

### 验证通过标准

| 检查项 | 通过条件 |
|--------|---------|
| 静态数量检查 | 模型数、服务方法数、测试方法数均与规格一致 |
| 静态格式检查 | 无注解缺失、无空 expected、JSON 结构合规 |
| 单元测试 | `mvn test` 全绿；Jacoco 行覆盖率 ≥ 90% |
| 功能冒烟测试 | 所有容器 healthy；`smoke_test.py` 退出码 0 |
