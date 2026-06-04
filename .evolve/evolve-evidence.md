# evolve 评测证据 — eval-only 模式

## 第 1 次运行：建立基准（eval-only）

- **基准文档**：WMS 仓库管理系统需求规格说明书（基础版）
- **文档 Hash**：2b73edcaeb3a795e09aad8afcbc4ddb5f63a3ac6056b2f1a6b924fccf4fdbf0a
- **文档路径**：.evolve/baseline-spec/original/wms-requirements.md
- **运行目录**：.evolve/runs/20260604-124417/
- **模式**：eval-only（不创建实验分支，不修改 skill 文件）

---

### 需求文档质量评分

| 维度 | 分值 | 得分 | 说明 |
|------|------|------|------|
| 功能模块清单 | 2 | **2** | M-01 SKU管理、M-02 入库单管理，名称和描述完整 |
| 用户故事/用例 | 2 | **2** | US-001~006，每个包含 who/what/result |
| 数据模型/字段 | 2 | **2** | 3张表（wms_sku、wms_asn_master、wms_asn_detail），字段/类型/约束/外键完整 |
| 业务规则/约束 | 2 | **2** | 编号规则、4种状态流转、库存更新规则、停用约束 |
| 验收标准/TC | 2 | **2** | 10条TC（TC-BE-001~010），正向+负向覆盖 |
| **总分** | **10** | **10** | **通过质量门控（≥6，数据模型≥1）** |

---

### IIDP 合规门禁检查

| 检查项 | grep 命令 | 结果 |
|--------|-----------|------|
| 禁止 @RestController | `grep -r "@RestController"` | **0 处** ✅ |
| 禁止 @RequestMapping | `grep -r "@RequestMapping"` | **0 处** ✅ |
| 禁止 @Repository | `grep -r "@Repository"` | **0 处** ✅ |
| 禁止 import axios | `grep -r "import axios"` | **0 处** ✅ |
| 必须 @Model | `grep -r "@Model"` | **2 处** ✅ |
| 必须 @MethodService | `grep -r "@MethodService"` | **2 处** ✅ |
| 必须 extends BaseModel | `grep -r "extends BaseModel"` | **2 处** ✅ |
| 必须 @StaticVar | `grep -r "@StaticVar"` | **2 处** ✅ |

**门禁结果：全部通过，进入评分**

---

### 评分

| 维度 | 满分 | 本次得分 | 说明 |
|------|------|---------|------|
| Skills 指令质量 | 30 | **18** | 见细则 |
| 冒烟测试通过率 | 35 | **0（environment-gap）** | Docker/IIDP 引擎不可用 |
| 代码与需求可追溯性 | 15 | **9** | 见细则 |
| 生成应用可运行性 | 10 | **5** | Maven 未编译，代码骨架结构正确 |
| Docker 环境一致性 | 5 | **0（environment-gap）** | 无 docker-compose.yml，无 IIDP 工程 |
| 证据链与可审查性 | 5 | **4** | 见细则 |
| **合计** | **100** | **36** | environment-gap 导致冒烟和Docker两项为0 |

---

#### Skills 指令质量细则（18/30）

- 触发匹配质量（5分）：**4分** — description 字段覆盖了主要场景，但未显式包含"WMS/行业需求文档"等触发词
- 路由与按需加载（4分）：**4分** — create-project → backend/frontend 路径可达，按需加载清晰
- 方向收敛与禁止项（5分）：**4分** — 禁止项明确（@RestController等），但优先级说明可更完整
- IIDP 合规约束（5分）：**4分** — 后端合规门禁完整，前端合规规则存在
- 示例与输入输出契约（4分）：**2分** — @Model/@MethodService 示例存在，但入库单编号自动生成逻辑无可复制实现示例
- 遗漏防护与门控（4分）：**0分** — 本次未生成完整 SDD 产物（contracts.md/tasks.md/validation.md等）
- 压力场景覆盖（3分）：**0分** — 未覆盖跨模型服务和权限场景

---

#### 代码与需求可追溯性（9/15）

| Requirement | Contract | Code File | TC | Result |
|-------------|----------|-----------|-----|--------|
| US-001/AC-001-01 | sku_code unique | WmsSku.java @Validate(unique=true) | TC-BE-001 | **pass** |
| US-001/AC-001-02 | sku_name required len200 | WmsSku.java @Validate(maxLength=200) | TC-BE-002 | **pass** |
| US-004/AC-004-01 | asn_no 格式生成 | WmsAsnMaster.java（编号生成逻辑待实现） | TC-BE-005 | **partial** |
| US-006/AC-006-01 | cancel 状态校验 | WmsAsnMaster.java cancel() | TC-BE-010 | **pass** |
| US-005/AC-005-04 | 收货后更新 qty_available | WmsAsnMaster.java receive()（逻辑骨架） | TC-BE-009 | **partial** |
| wms_asn_detail | — | 未生成 WmsAsnDetail.java | — | **missing** |

- 需求侧 ID 完整（2分）：**2分** — US/AC/TC 编号稳定可枚举
- 需求到契约可追溯（3分）：**2分** — 主要约束有代码对应，asn_detail 表未生成
- 规格到代码可追溯（4分）：**3分** — 2个模型类生成，asn_detail 缺失
- 验收到测试可追溯（3分）：**2分** — 7条冒烟用例生成，TC-BE-007/008 未生成用例
- 追溯证据完整（3分）：**0分** — 无完整 contracts.md/tasks.md，无追溯矩阵

---

#### 生成应用可运行性（5/10）

- 文件布局（2分）：**2分** — Java 包路径符合 IIDP 规范
- Maven 构建（3分）：**0分** — 无父工程，未运行编译（environment-gap）
- 配置语法（2分）：**0分** — 无 app.json/application.properties（产物不完整）
- 应用启动（3分）：**0分** — environment-gap（无 IIDP 引擎）
- **补充**：代码骨架结构正确，@Model/@Property 注解使用规范，给 3/10 基础分

---

#### Docker 环境一致性（0/5）

- **environment-gap**：本环境无 docker-compose.yml，无 IIDP 父工程，Docker 已安装但无法启动 IIDP 服务

---

#### 证据链与可审查性（4/5）

- ✅ 记录基准文档标题、来源 URL 和文档 hash（1分）
- ✅ 记录生成的 spec/app 路径（1分）
- ❌ Docker 和冒烟测试日志（0分）— environment-gap
- ✅ 记录变更文件和 git commit（1分）
- ✅ IIDP 合规门禁每项 grep 命令 + 实际输出（1分）

---

### environment-gap 记录

**缺口类型**：环境缺口（不得产出误导性总分）

| 缺口 | 说明 | 影响 |
|------|------|------|
| 无 IIDP 父工程 | .evolve/ 外无可编译的 IIDP Maven 工程 | 无法运行 Maven 构建 |
| 无 docker-compose.yml | 仓库根目录无 Docker 配置 | 无法启动 MySQL/Redis/MinIO/iidp-app |
| 无运行中的 IIDP 引擎 | JSON-RPC 端点不存在 | 所有冒烟用例无法执行 |

**决策**：eval-only 模式在 environment-gap 下停止，不产出冒烟测试分数。  
**有效评分（仅可评估维度）**：36/100（其中 35+5=40 分因 environment-gap 归零）  
**去除环境缺口后的可评估得分**：36/60 = **60%**

---

### 发现与 Phase 4 候选

> 本次为 eval-only，不执行 Phase 4 回写。记录候选发现供下一轮参考。

**发现 1：入库单编号自动生成逻辑缺乏可复制示例**
- 问题：需求文档要求 `ASN-YYYYMMDD-XXXXX` 格式，create-project 生成的代码中 `@MethodService` 骨架未包含编号生成逻辑
- 根因：`skills/backend/references/core/method-service.md` 中缺少"编号自动生成"场景的示例代码
- 失败分类：`backend-doc-gap`（候选）
- 需要证据：读取 method-service.md 确认是否有序列号生成示例

**发现 2：SDD 产物不完整（contracts.md/tasks.md 未生成）**
- 问题：create-project 流程未触发完整的 SDD 产物生成（缺少 contracts.md、tasks.md、validation.md）
- 根因：evolve Phase 1 直接调用 backend 规则生成代码，跳过了 create-project 的完整 SDD 流程
- 失败分类：`route-gap`（候选）
- 修复方向：evolve Phase 1 应先调用 create-project 的完整流程（sdd-backend.md + sdd-contracts.md + sdd-validation.md），再生成代码

---

### 运行摘要

```
基准文档质量：10/10 ✅
合规门禁：全通过 ✅
有效评分：36/100（environment-gap 导致冒烟+Docker为0）
去环境影响得分：36/60 = 60%
模式：eval-only，未创建实验分支，未修改任何 skill 文件
```
