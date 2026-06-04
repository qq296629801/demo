# 样本池

## 目的

样本池用于在多种行业场景的需求文档中暴露 `create-project`、`backend`、`frontend` 的指导缺口。它不能替代固定基准。样本结果用于指导改进假设，固定基准决定是否保留改动。

---

## 来源

样本可以来自：

- 用户提供的需求文档（Markdown、PDF 链接或本地文件）
- 通过 websearch 发现的开源项目需求/设计文档
- WMS / MES / APS / ERP / CRM / OA 等行业场景的公开规格文档

当用户要求 websearch 时，每轮最多搜索 50 个候选文档。优先选择字段描述完整、有用户故事或验收标准、能代表真实业务场景的文档。

> 详细搜索词和行业场景指引见 `references/requirements-doc-guide.md` § 行业场景搜索指引。

---

## 过滤规则

只有候选满足以下条件时才接收：

- 文档可访问，且能获取到全文内容
- 描述真实业务场景（非教程、Demo 或玩具示例）
- 包含功能模块清单或用户故事
- 包含数据模型或字段说明（让 `create-project` 能生成 `@Model`）
- 包含验收标准或测试场景描述（让冒烟测试有来源）
- 通过 `requirements-doc-guide.md` 质量门控（总分 ≥ 6，数据模型 ≥ 1），或可通过预处理补全至准入线

出现以下情况时拒绝并记录原因：

- 文档不可访问或内容获取失败
- 只有截图、架构图或高层描述，无字段/流程/规则细节
- 与后台管理/业务系统无关（游戏、纯前端、移动端文档）
- 经预处理仍无法达到数据模型 ≥ 1 分
- 涉及保密或版权不明确的内容（且用户未明确允许）

---

## 样本记录

在 `evolve-evidence.md` 中使用以下记录格式：

```markdown
### 样本：<name>

- Doc URL: <文档链接或本地路径>
- Doc Hash: <sha256>
- Source: <来源说明>
- Business Domain: <WMS / MES / APS / ERP / CRM / OA / 其他>
- Quality Score: <总分>/10（见 requirements-doc-guide.md 质量门控）
- Accepted: yes/no
- Filter reason: <接收或拒绝原因>
- Preprocessed: yes/no（是否经过 AI 补全）
- create-project result: <生成摘要>
- Docker/smoke result: <通过/失败摘要>
- Suspected skills gap: <`route-gap` / `backend-doc-gap` / `frontend-doc-gap` / `sdd-template-gap` / `knowledge-gap` + 一句话说明>
```

---

## 使用样本结果

评测样本后：

1. 按根因对失败分组。
2. 优先处理影响多个样本或固定基准的问题。
3. 按失败分类形成一个小的改进假设：
   - `route-gap` 或 `sdd-template-gap`：候选目标通常是 `skills/create-project/`。
   - `backend-doc-gap`：候选目标通常是 `skills/backend/`，但必须有本地文档、源码、日志、配置、测试或用户确认作为 IIDP 私有规则证据。
   - `frontend-doc-gap`：候选目标通常是 `skills/frontend/`，但必须有 `iidpDoc`、源码、日志、配置、测试或用户确认作为 IIDP 私有规则证据。
   - `knowledge-gap`：只记录缺口和所需证据，不修改 backend/frontend 规则。
4. 保留改动前必须运行固定基准。

不得修改需求文档内容或生成的 IIDP 应用代码来让样本分数通过。
