# 需求文档获取与质量评估指南

## 用途

在 evolve 框架中，固定基准和样本池均以**需求文档**为输入，而非源码仓库。本文件规定如何获取需求文档、如何判断其质量是否足够让 `create-project` 直接生成 IIDP 代码，以及如何管理文档版本。

---

## 需求文档质量门控（10 分制）

每份文档在进入 evolve 基准或样本池前，必须按以下 5 个维度打分：

| 维度 | 分值 | 判断标准 |
|------|------|---------|
| 功能模块清单 | 2 | 有明确的模块列表，每个模块有名称和一句话描述 |
| 用户故事或用例 | 2 | 能推导出"谁做什么、输入什么、预期结果是什么"的结构 |
| 数据模型/字段说明 | 2 | 有实体名称、字段名、字段类型和实体间关系（让 create-project 能生成 `@Model`） |
| 业务规则/约束 | 2 | 有必填项、唯一性约束、状态流转、校验规则或计算逻辑 |
| 验收标准/测试用例 | 2 | 有可派生冒烟测试输入/预期输出的条目（`AC-*` 或测试场景描述） |

**准入门槛**：总分 **≥ 6 分**，且**数据模型维度 ≥ 1 分**。

数据模型维度为 0 时，`create-project` 无法生成 `@Model`，不允许进入基准或样本池。此时应先执行文档预处理，补全数据模型后重新打分。

---

## 行业场景搜索指引

当用户要求通过 websearch 扩充样本池时，按以下分类搜索：

### WMS（仓库管理系统）

```
WMS warehouse management system requirements specification site:github.com
开源 WMS 系统需求文档 功能说明 数据库设计 filetype:md
github wms open source functional requirements user story
仓库管理系统 需求规格说明书 入库 出库 盘点 数据模型
```

典型模块：入库管理、出库管理、库存盘点、货位管理、供应商管理、收发货单

### MES（制造执行系统）

```
MES manufacturing execution system requirements document github
开源 MES 系统 详细设计文档 数据模型 工单 工序
manufacturing execution system functional specification filetype:md
生产执行系统 需求规格说明书 工单管理 质检 报工
```

典型模块：工单管理、工序管理、报工记录、质量检验、设备管理、物料追溯

### APS（高级计划排程）

```
APS advanced planning scheduling requirements specification
生产排程系统 需求规格说明书 排程 产能 约束 site:github.com
advanced planning system functional requirements data model
```

典型模块：需求计划、产能分析、排程规则、工单排产、物料齐套分析

### ERP / 进销存 / OA

```
ERP 进销存系统 需求文档 功能模块 数据库设计 github
OA 办公自动化 审批流 需求规格 数据模型
open source erp requirements specification functional filetype:md
```

### 通用补充搜索词

当以上搜索词效果不佳时，尝试：

```
<系统名> 详细设计文档 数据字典 接口说明
<系统名> software requirements specification SRS
<系统名> 需求分析报告 功能需求 非功能需求
```

---

## 文档预处理规范

当文档质量不足（总分 < 6 或数据模型为 0）时，Agent 可补全，但须严格遵守以下规则：

### 允许补全的内容

| 补全类型 | 标注方式 |
|---------|---------|
| 从功能描述推导数据模型 | `[AI 推导，待用户确认]` |
| 从业务规则推导验收标准 | `[AI 补充，来源：原文第 X 章]` |
| 补全缺失字段类型 | `[AI 推测，待确认]` |

### 不允许补全的内容

- 核心业务规则（如状态流转、计算公式、权限控制）——必须来自原文或用户确认
- 删减原始文档内容

### 补全文档存放位置

```
.evolve/baseline-spec/preprocessed/
├── <doc-name>-preprocessed.md   # 补全后文档
└── <doc-name>-diff.md           # 补全记录（原文 vs 补全内容）
```

---

## 基准文档版本管理

每份进入 evolve 的需求文档必须有稳定的版本记录：

### 目录结构

```
.evolve/baseline-spec/
├── original/                    # 原始文档（只读，不得修改）
│   └── <doc-name>.<ext>
├── preprocessed/                # AI 补全版本
│   ├── <doc-name>-preprocessed.md
│   └── <doc-name>-diff.md
├── smoke-cases/                 # 从文档派生的冒烟用例
│   └── <module>-cases.json
└── manifest.json                # 文档注册表
```

### manifest.json 格式

```json
{
  "baseline_type": "requirements-doc",
  "baseline_doc_name": "<文档标题>",
  "baseline_doc_source": "<来源 URL 或本地路径>",
  "baseline_doc_hash": "<sha256 hash，确保可复现>",
  "baseline_doc_version": "<版本号或日期>",
  "quality_score": {
    "total": 8,
    "modules": 2,
    "user_stories": 2,
    "data_model": 2,
    "business_rules": 1,
    "acceptance_criteria": 1
  },
  "preprocessed": true,
  "preprocessed_reason": "<为什么需要补全>",
  "recorded_at": "YYYY-MM-DD HH:MM:SS"
}
```

### 版本锁定规则

- 首次记录后，`baseline_doc_hash` 不得修改，除非用户明确要求刷新基准
- 刷新基准时，旧版本归档到 `original/archived/`，并在 `manifest.json` 中记录刷新原因
- 同一个评测循环的所有轮次必须使用同一份文档（相同 hash）

---

## 从文档派生冒烟用例

每个冒烟用例的 `trace` 字段必须回指需求文档中的具体条目：

```json
{
  "trace": {
    "us": "US-001",
    "fr": "FR-003",
    "ac": "AC-001-01",
    "tc": "TC-BE-001",
    "doc_section": "第 3.2 章 入库单管理"
  }
}
```

当需求文档没有显式的 `US-*` / `FR-*` / `AC-*` 编号时，按以下规则生成：

| 来源 | 编号格式 |
|------|---------|
| 功能模块 | `US-<模块序号>` |
| 模块内功能点 | `FR-<模块序号>-<功能序号>` |
| 业务规则/约束条目 | `AC-<FR编号>-<约束序号>` |
| 派生测试用例 | `TC-BE-<序号>` 或 `TC-FE-<序号>` |
