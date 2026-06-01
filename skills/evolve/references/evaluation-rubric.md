# 评分 Rubric

## 评分总览

总分：100 分。

| 维度 | 分值 | 衡量内容 |
|---|---:|---|
| 需求还原质量 | 20 | `code-index` 是否从源码文档化模块、用户故事、API、数据模型和验收标准 |
| IIDP SDD 完整性 | 20 | `create-project` 是否生成 requirements、contracts、backend-spec、frontend-spec、tasks 和 validation |
| 生成应用可运行性 | 20 | Maven 构建、配置文件、打包和应用启动 |
| Docker 环境一致性 | 15 | 所有必需文件中的账号、密码、端口、数据库、Redis、MinIO 配置是否一致 |
| 冒烟测试通过率 | 20 | 用户故事 JSON-RPC 冒烟用例是否通过 |
| 证据链与可审查性 | 5 | 日志、diff、评分表、commit SHA 和失败原因是否完整 |

## 门禁规则

- 如果 Docker 环境一致性失败，Docker 维度记 0 分，且本轮不能报告为冒烟测试通过。
- 如果应用无法启动，生成应用可运行性最高 8 分，冒烟测试通过率记 0 分。
- 如果没有从源码还原出用户故事或测试用例，需求还原质量最高 10 分，冒烟测试通过率最高 5 分。
- 如果改进修改了 `skills/create-project/` 之外的文件，即使分数提升，该实验也无效。
- 只能比较同一基准 commit SHA 和同一本地环境下产生的分数。

## 维度细则

### 需求还原质量：20

- 4 分：存在模块和功能清单。
- 4 分：用户故事具体，并可追溯到源码行为。
- 4 分：API 文档包含端点/服务形态和请求/响应细节。
- 4 分：数据库结构包含表、核心字段和关系。
- 4 分：存在可驱动验证的验收标准或测试用例。

### IIDP SDD 完整性：20

- 4 分：`requirements.md` 存在，并保留源码还原出的范围。
- 4 分：`contracts.md` 或 integration map 定义模型、服务、权限和视图/菜单 key。
- 4 分：`backend-spec.md` 包含 IIDP 命名、模型、方法、视图、种子数据和权限。
- 4 分：`frontend-spec.md` 存在，或明确说明使用标准模板无需前端代码。
- 4 分：`tasks.md` 和 `validation.md` 将需求连接到实现和测试。

### 生成应用可运行性：20

- 5 分：生成文件落在预期的 IIDP app/module 布局中。
- 5 分：Maven build 或 package 命令成功。
- 5 分：应用配置语法有效，并能解析必需属性。
- 5 分：应用启动，并暴露预期 JSON-RPC 端点。

### Docker 环境一致性：15

- 4 分：MySQL host、port、database、username、password 一致。
- 3 分：Redis host、port、password 一致。
- 3 分：MinIO endpoint、access key、secret key 和 bucket 预期一致。
- 3 分：应用端口和 active profile 与 Docker 启动一致。
- 2 分：证据展示被比较的精确文件和值，并在摘要中脱敏敏感信息。

### 冒烟测试通过率：20

根据生成的 JSON-RPC 用例计算：

```text
smoke_points = round(20 * passed_cases / total_cases)
```

如果 `total_cases = 0`，记 0 分。用例必须可追溯到还原出的用户故事或测试用例。

### 证据链与可审查性：5

- 1 分：记录基准 URL 和 commit SHA。
- 1 分：记录生成的 spec/app 路径。
- 1 分：汇总 Docker 和冒烟测试日志。
- 1 分：记录变更的 `create-project` 文件和 git commit SHA。
- 1 分：保留/回滚决策有具体理由。

## 接收决策

只有满足以下条件时才接收实验：

```text
new_benchmark_score > previous_benchmark_score
```

分数相等不接收。只在样本池仓库上观察到的提升只能作为诊断信号，不足以接收改动。
