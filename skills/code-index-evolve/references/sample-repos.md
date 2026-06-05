# 样本仓库清单与 Ground Truth 管理

## 样本仓库的作用

code-index-evolve 使用**已知 ground truth 的代码仓库**作为固定基准，验证 code-index skill
生成的 Codebook 文档是否准确覆盖了真实的接口、实体和模块。

---

## Ground Truth 格式规范

每个样本仓库需要一份 `baseline.json` 文件，存放于 `.code-index-evolve/` 根目录。

```json
{
  "repoName": "示例仓库名称",
  "repoPath": "/path/to/local/repo",
  "commitHash": "<git-commit-sha>",
  "framework": "IIDP | SpringBoot | NestJS | Django | Other",
  "language": "Java | TypeScript | Python | Other",
  "modules": ["module-a", "module-b", "module-c"],
  "groundTruth": {
    "moduleCount": 3,
    "totalApiCount": 47,
    "totalEntityCount": 12,
    "totalErrorCodeCount": 28,
    "sampleFields": [
      {
        "module": "example",
        "class": "ExampleStudentDO",
        "file": "ExampleStudentDO.java",
        "field": "studentNo",
        "type": "String"
      },
      {
        "module": "example",
        "class": "ExampleStudentDO",
        "file": "ExampleStudentDO.java",
        "field": "className",
        "type": "String"
      }
    ],
    "sampleEndpoints": [
      {
        "module": "example",
        "method": "POST",
        "path": "/example/student/create",
        "controllerClass": "ExampleStudentController",
        "permission": "example:student:create"
      },
      {
        "module": "example",
        "method": "GET",
        "path": "/example/student/list",
        "controllerClass": "ExampleStudentController",
        "permission": "example:student:list"
      }
    ],
    "sampleErrorCodes": [
      {
        "constantName": "EXAMPLE_STUDENT_NOT_EXISTS",
        "code": 1000300001,
        "message": "学生不存在"
      }
    ]
  },
  "notes": "可选：说明该仓库的特殊性，如缺失哪类文件、是否有多租户等"
}
```

### Ground Truth 字段说明

| 字段 | 必填 | 说明 |
|---|---|---|
| `repoName` | 是 | 仓库名，用于报告标识 |
| `repoPath` | 是 | 本地绝对路径，供 codegraph init 使用 |
| `commitHash` | 是 | 锁定基准版本，保证评测可重现 |
| `framework` | 是 | 框架标识，影响 code-index Phase A 的框架检测 |
| `modules` | 是 | 预期模块列表，用于模块划分准确率计算 |
| `groundTruth.totalApiCount` | 是 | 手工统计的 Controller 方法总数（不含继承方法） |
| `groundTruth.totalEntityCount` | 是 | 手工统计的数据表/Entity 总数 |
| `groundTruth.sampleFields` | 是 | 用于可追溯性抽查，需覆盖多个模块，≥ 10 条 |
| `groundTruth.sampleEndpoints` | 是 | 用于验证接口覆盖，需覆盖多个 Controller，≥ 5 条 |
| `groundTruth.sampleErrorCodes` | 推荐 | 用于错误码可信度验证，≥ 5 条 |

---

## Ground Truth 确认方法

Ground Truth 不得来自推测。必须通过以下方式实际确认：

### 接口总数确认（Java 项目示例）

```bash
# 统计 @RequestMapping / @PostMapping / @GetMapping 等注解的方法数
# 在项目根目录执行
grep -rn "@PostMapping\|@GetMapping\|@PutMapping\|@DeleteMapping\|@RequestMapping" \
  --include="*Controller.java" . | grep -v "^.*//.*@" | wc -l

# IIDP 项目用 codegraph 确认
codegraph_files("controller")   # 列出所有 Controller 文件
# 然后逐文件计数 @RequestMapping 方法
```

### 实体总数确认

```bash
# Java：统计 @TableName / @Entity / @Table 注解的类
grep -rn "@TableName\|@Entity\|@Table" --include="*.java" . | grep -v "^.*//.*@" | wc -l

# IIDP：统计 @Model 注解类
grep -rn "@Model" --include="*.java" . | grep "^[^/]" | wc -l
```

### 错误码总数确认

```bash
# 统计 ErrorCodeConstants 文件中的常量数
grep -rn "ErrorCodeConstants\|ErrorCode" --include="*.java" . -l
# 然后对每个文件统计 public static final ErrorCode 行数
```

### 字段样本确认

从多个 DO/Entity/VO 类中各取 2-3 个字段，确认字段名和类型与源码一致。

---

## 样本仓库选择原则

### 基准仓库（Baseline Repo）

- 每次运行 evolve 循环**固定使用同一个 commit hash**
- 存放于 `.code-index-evolve/baseline-repo/`（可以是 symlink 或路径记录）
- 修改 `skills/code-index/` 后用相同基准重新跑，才能对比分数变化
- **禁止**：在评测期间更新基准仓库的代码（即使是小改动）

### 样本池仓库（Sample Pool）

- 用于发现更多薄弱点，辅助诊断失败模式
- 存放于 `.code-index-evolve/samples/<repo-name>/`
- 在样本池上观察到的提升**只能作为诊断信号**，不足以接收改动
- 至少包含以下类型的仓库（覆盖不同场景）：

| 场景 | 目的 |
|---|---|
| 标准 IIDP 项目（多模块） | 检验 IIDP 框架识别和 baseline-spec 生成 |
| 非 IIDP Java Spring 项目 | 检验通用框架识别和 codegraph 采集 |
| 小型项目（≤ 3 模块，≤ 20 接口） | 检验基础流程完整性 |
| 大型项目（≥ 5 模块，≥ 50 接口） | 检验 Phase C 采集不遗漏 |

### 隔离规则

```
.code-index-evolve/
├── baseline.json              # 当前基准的 ground truth
├── baseline-repo/             # 基准仓库路径记录（或 symlink）
├── samples/
│   ├── repo-a/baseline.json   # 样本池仓库 A 的 ground truth
│   └── repo-b/baseline.json   # 样本池仓库 B 的 ground truth
├── runs/                      # 每次评测的输出快照
│   └── <timestamp>/
│       ├── codebook/          # 本轮生成的 Codebook（只读存档）
│       └── score.json         # 本轮各维度分数
├── evolve-evidence.md         # 每轮实验记录（累积追加）
└── evolve-results.tsv         # 分数对比表（每轮一行）
```

**重要**：`runs/<timestamp>/codebook/` 仅作存档参考，不得用于对比评分（只比较同一基准 + 同一环境下的分数）。

---

## 内置样本仓库：iidp-backend-demo-ai

这是 IIDP 官方示例仓库，推荐作为首选基准仓库。

```json
{
  "repoName": "iidp-backend-demo-ai",
  "framework": "IIDP",
  "language": "Java",
  "modules": ["example"],
  "groundTruth": {
    "moduleCount": 1,
    "totalApiCount": 12,
    "totalEntityCount": 3,
    "totalErrorCodeCount": 5,
    "sampleFields": [
      {"module": "example", "class": "ExampleStudentDO", "file": "ExampleStudentDO.java", "field": "studentNo", "type": "String"},
      {"module": "example", "class": "ExampleStudentDO", "file": "ExampleStudentDO.java", "field": "className", "type": "String"},
      {"module": "example", "class": "ExampleStudentSaveReqVO", "file": "ExampleStudentSaveReqVO.java", "field": "studentNo", "type": "String"},
      {"module": "example", "class": "ExampleStudentSaveReqVO", "file": "ExampleStudentSaveReqVO.java", "field": "birthday", "type": "LocalDate"},
      {"module": "example", "class": "ExampleStudentRespVO", "file": "ExampleStudentRespVO.java", "field": "id", "type": "Long"},
      {"module": "example", "class": "ExampleStudentPageReqVO", "file": "ExampleStudentPageReqVO.java", "field": "studentNo", "type": "String"}
    ],
    "notes": "若实际 ground truth 与上方不符，以 codegraph_files + 手工计数为准，及时更新此文件"
  }
}
```

> **首次使用时**：在运行 Phase 1 之前，必须通过上方"Ground Truth 确认方法"手工核对并更新 `totalApiCount`、`totalEntityCount`、`totalErrorCodeCount`，确保 ground truth 精确。

---

## 多仓库评测时的模块代表性选取

当 baseline 仓库有多个模块时，按以下规则选取代表性模块用于 Agent 评审（最多 3 个）：

1. 优先选取**接口数最多**的模块（覆盖最广）
2. 其次选取**有独立数据表**的模块（验证 database.md 质量）
3. 若有**跨模块调用**的模块，优先纳入（验证 hla.md 调用图准确性）
4. 小型项目（≤ 3 模块）：全部模块参与评审
