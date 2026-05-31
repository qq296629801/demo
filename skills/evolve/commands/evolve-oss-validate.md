---
description: 【Evolve Tool】通过 Playwright MCP 访问开源网站 Demo，截图并分析功能，与 IIDP 生成规格/代码对比，输出合规评分报告（10维，100分）
---

# /evolve-oss-validate

## 用途

访问指定的开源系统 Demo 网站，提取真实功能清单，与当前项目的 `backend-spec.md` + `frontend-spec.md` 对比，输出合规评分报告，识别 Gap（G1-G8 类别）。

## 输入参数

```text
$ARGUMENTS
```

必填：
- `--oss-url <url>`：开源系统 Demo URL（如 `http://demo.ruoyi.vip`）
- `--project-path <path>`：当前 IIDP 项目规格书目录（含 backend-spec.md/frontend-spec.md）

可选：
- `--modules <m1,m2>`：只验证指定功能模块（逗号分隔），默认验证所有
- `--login-user <user>`：登录账号（默认 admin）
- `--login-pass <pass>`：登录密码（默认 admin123）
- `--output <path>`：报告输出路径（默认 `<project-path>/oss-validation-report.md`）

---

## 执行步骤

### 步骤 1：前置准备

1. 读取 `<project-path>/backend-spec.md`，提取：
   - 所有实体名称（entity list）
   - 服务清单（service list）
   - 状态枚举（status enums）

2. 读取 `<project-path>/frontend-spec.md`，提取：
   - 页面清单（page list）
   - 操作按钮清单（button list）
   - 视图类型（grid/search/form/detail/workflow）

3. 读取 `skills/evolve/references/oss-validation-guide.md` — 获取登录策略和工具使用规范

### 步骤 2：访问 OSS 系统

**2.1 首页访问与登录检测**

```
mcp__playwright__navigate(<oss-url>)
snapshot = mcp__playwright__snapshot()

if snapshot 包含登录表单:
  mcp__playwright__fill(username_selector, <login-user>)
  mcp__playwright__fill(password_selector, <login-pass>)
  mcp__playwright__click(submit_selector)
  mcp__playwright__navigate(<oss-url>/dashboard 或首页)
  snapshot = mcp__playwright__snapshot()
  
  if 仍有登录表单:
    mcp__playwright__screenshot() → 存 oss-login-failed.png
    记录：LOGIN_STATUS = "failed"
    继续分析可公开访问的部分
  else:
    记录：LOGIN_STATUS = "success"
```

**2.2 功能发现**

```
从 snapshot 提取导航菜单：
  OSS_FEATURES = []
  for each nav/menuitem/link in snapshot:
    记录 {name, url, parent_menu}
    
  输出：
  发现 [N] 个功能模块：
  - [功能1]（[URL]）
  - [功能2]（[URL]）
  ...
```

**2.3 逐功能分析**（对每个功能模块，或 `--modules` 过滤后的子集）

```
for feature in OSS_FEATURES:
  mcp__playwright__navigate(feature.url)
  mcp__playwright__screenshot() → oss-screenshots/{feature.name}-list.png
  list_snapshot = mcp__playwright__snapshot()
  
  提取 list_data = {
    columns: [表格列名列表],
    search_fields: [搜索区域字段],
    buttons: [操作按钮名称列表],
    status_values: [状态筛选下拉选项]
  }
  
  // 尝试点击"新增/添加/创建"
  try:
    mcp__playwright__click("新增 OR 添加 OR 创建 OR Add OR Create")
    form_snapshot = mcp__playwright__snapshot()
    提取 form_data = {
      fields: [label文本列表],
      required: [带*的字段],
      dropdowns: [下拉选项]
    }
  catch:
    form_data = null
  
  // 检测详情页
  try:
    mcp__playwright__click("查看 OR 详情 OR View OR Detail")
    detail_snapshot = mcp__playwright__snapshot()
    detail_exists = true
  catch:
    detail_exists = false
  
  // 检测审批/工作流
  try:
    mcp__playwright__click("审批 OR 提交 OR 审核 OR Approve OR Submit")
    workflow_snapshot = mcp__playwright__snapshot()
    workflow_exists = true
    提取 state_steps = [步骤条/进度条节点名称]
  catch:
    workflow_exists = false
    state_steps = []
  
  OSS_FEATURE_MAP[feature.name] = {
    list: list_data,
    form: form_data,
    detail: detail_exists,
    workflow: workflow_exists,
    states: state_steps
  }
```

### 步骤 3：对比分析（10 维评分）

**对比 OSS 功能 vs IIDP 生成规格，计算各维度得分：**

| 维度 | 计算方式 | 满分 |
|---|---|---|
| D1 功能覆盖率 | IIDP 功能数 / OSS 功能数 × 20 | 20 |
| D2 页面完整性 | list✅+form✅+detail×(1-缺失率)+workflow×(1-缺失率) | 15 |
| D3 字段完整性 | IIDP 字段数 / OSS 可见字段数 × 15 | 15 |
| D4 操作完整性 | IIDP 按钮数 / OSS 按钮数 × 15 | 15 |
| D5 状态流转 | 状态枚举覆盖率 × 10 | 10 |
| D6 业务规则 | 必填/校验覆盖率 × 10 | 10 |
| D7 权限粒度 | 有按钮级权限控制 → 5，否则 → 0 | 5 |
| D8 接口规范 | 分页/排序/Filter 支持 → 5，否则 → 0 | 5 |
| D9 数据关联 | ER 关系数 / OSS 表单关联数 × 3 | 3 |
| D10 错误处理 | 有错误码/友好提示声明 → 2，否则 → 0 | 2 |

### 步骤 4：识别 Gap

对比分析结果，映射到 `gap-taxonomy.md` 的 G1-G8 分类：

```
Gap 识别规则：
- D2 页面完整性 < 10 且 detail_exists=false → G4-1（缺少详情页）
- D2 页面完整性 < 10 且 workflow_exists=false 但 OSS 有审批 → G5-2（缺少审批服务）
- D5 状态流转 < 7 → G5-1（状态枚举不完整）
- D7 权限粒度 = 0 → G4-2（按钮权限缺失）
- D8 接口规范 = 0 → G8-2（缺 Filter/Sort 支持）
- D3 字段完整性 < 10 → G7-1（字段校验缺失）
```

### 步骤 5：生成报告

将分析结果写入 `<output>` 文件：

```markdown
# OSS 验证报告

**目标系统**：[oss-url]  
**验证时间**：[ISO 8601 timestamp]  
**登录状态**：[LOGIN_STATUS]  
**IIDP 规格路径**：[project-path]  

## 功能覆盖对比

| 功能 | OSS | IIDP | 页面类型差距 | 字段差距 |
|---|---|---|---|---|
| [功能名] | ✅ | ✅/❌ | [差距描述] | [缺失字段] |
...

## 合规评分（共 100 分）

| 维度 | 得分 | 满分 | 主要问题 |
|---|---|---|---|
| D1 功能覆盖率 | X | 20 | [1句] |
| D2 页面完整性 | X | 15 | [1句] |
| D3 字段完整性 | X | 15 | [1句] |
| D4 操作完整性 | X | 15 | [1句] |
| D5 状态流转 | X | 10 | [1句] |
| D6 业务规则 | X | 10 | [1句] |
| D7 权限粒度 | X | 5 | [1句] |
| D8 接口规范 | X | 5 | [1句] |
| D9 数据关联 | X | 3 | [1句] |
| D10 错误处理 | X | 2 | [1句] |
| **总分** | **X** | **100** | |

## 识别的 Gap（G1-G8 分类）

| Gap | 类别 | 描述 | 建议修复文件 |
|---|---|---|---|
| G4-1 | 前端页面 | [具体描述] | llm-prompts.md Prompt 8 |
| G5-2 | 状态服务 | [具体描述] | sdd-backend.md §4 |
...

## 截图存档

[列出所有保存的截图路径]
```

---

## 完成标志

- `oss-validation-report.md` 已写入指定路径
- 报告包含 10 维合规得分和总分
- 识别到的 Gap 已映射到 G1-G8 分类
- 至少 1 张截图存档（login 或首个功能的 list 页）
- 输出报告文件路径，方便用户直接查看
