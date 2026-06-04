---
description: 【SDD Tool】解析 spec/09-ui/ 下的静态 HTML 原型，用 Playwright MCP 提取交互需求，生成 interaction-spec.md（sdd-frontend-interaction.md 8 节格式）
handoffs:
  - label: 继续生成前端节点规格
    command: sdd-spec
    prompt: interaction-spec.md 已生成，请补全 frontend-spec.md 中与交互行为相关的章节（§2 用户流程、§3 交互状态）
    send: false
---

# /sdd-ui-parse

## 用途

解析 `code-index` skill 生成的静态 HTML 原型（`spec/09-ui/` 目录），使用 Playwright MCP 对每个页面执行导航、无障碍树快照和关键交互模拟，提取结构化交互需求，输出 `specs/modules/<feature>/interaction-spec.md`。

输出格式严格遵循 `skills/create-project/references/sdd-frontend-interaction.md` 的 8 节结构：
1. 页面目标  2. 用户流程  3. 交互状态表  4. 响应式与容器策略  5. 无障碍与易用性
6. 组件清单  7. API 调用映射  8. 待确认项

本命令仅提取交互行为规格，**不输出**节点 id、selector、bind_、ds_config 等技术细节——技术落地规格由 `/sdd-spec` → `sdd-frontend.md` 完成。

## 用户输入

```text
$ARGUMENTS
```

可选参数：
- `--feature <name>`：功能模块名称，输出路径为 `specs/modules/<name>/interaction-spec.md`；不传时默认取 `spec/09-ui/` 的父目录 spec 名。
- `--ui-dir <path>`：静态 HTML 目录路径，默认为 `spec/09-ui/`。
- `--contracts <path>`：`contracts.md` 路径，默认为 `specs/modules/<feature>/contracts.md`；不存在时跳过 API 映射步骤并在 §8 标注"待确认"。

## 前置检查

1. 确认 `spec/09-ui/`（或 `--ui-dir` 指定路径）目录存在。
2. 列出目录下所有 `.html` 文件；若无则停止执行，提示用户先运行 `/code-index` 生成 UI 原型。
3. 尝试读取 `contracts.md`，记录是否存在（不存在时不报错，继续执行）。
4. 确认 Playwright MCP 可用：调用 `mcp__playwright__navigate` 导航到第一个 HTML 文件的 `file://` 路径，若工具不可用则停止执行并提示。

输出示例（前置检查不通过）：

```markdown
## ⚠ 前置检查失败

- `spec/09-ui/` 目录下未找到 .html 文件。
请先运行 `/code-index` 生成 UI 静态原型，再重新运行 `/sdd-ui-parse`。
```

## 执行步骤

### 步骤 1 — 读取 contracts.md（可选）

若 `contracts.md` 存在，读取并提取：
- 所有 `@MethodService` 定义的接口方法签名（方法名、入参、返回类型）
- 接口的中文描述（description 字段）

将这些接口列表保存到工作内存，用于步骤 4 的 API 映射。若文件不存在，API 映射列（§7）中每条记录填写"待确认（contracts.md 未就绪）"。

### 步骤 2 — 逐页解析 HTML 原型

对 `spec/09-ui/` 下每个 `.html` 文件，依次执行：

#### 2a. 打开页面并截初始快照

```text
mcp__playwright__navigate("file:///[绝对路径]/spec/09-ui/[page].html")
mcp__playwright__screenshot()        ← 存档视觉参考
mcp__playwright__snapshot()          ← 获取初始 accessibility tree
```

从快照中提取：
- **页面标题**：`<h1>` 或 `<title>` 文本
- **按钮列表**：所有 `role=button` 或 `<button>` 的 aria-label / 文本内容
- **表单字段**：所有 `<input>`、`<select>`、`<textarea>` 的 label 和 name/placeholder
- **Tab 标签**：所有 `role=tab` 的文本
- **表格列标题**：`<th>` 文本
- **状态 Badge/步骤条**：class 中含 `status`、`badge`、`step`、`workflow`、`stepper`、`timeline` 的元素
- **禁用/隐藏元素**：class 含 `disabled`、`hidden`、`opacity-0` 的可交互元素（记录其名称和禁用条件）

#### 2b. 模拟关键交互（捕获弹窗/状态变化）

对 2a 中识别到的关键按钮，依次模拟点击并再次快照：

**新增/创建类按钮**（按钮文本含"新增"、"添加"、"创建"、"新建"、"Add"）：
```text
mcp__playwright__click("[按钮 selector 或 aria-label]")
mcp__playwright__snapshot()   ← 捕获弹窗/抽屉内的表单字段
```

**编辑/修改类按钮**（文本含"编辑"、"修改"、"Edit"）：
```text
mcp__playwright__click("[按钮]")
mcp__playwright__snapshot()   ← 捕获编辑表单字段
```

**查看/详情类按钮**（文本含"查看"、"详情"、"View"）：
```text
mcp__playwright__click("[按钮]")
mcp__playwright__snapshot()   ← 捕获详情区域字段
```

**Tab 标签切换**（存在多个 Tab 时，点击非首个 Tab）：
```text
mcp__playwright__click("[第二个 Tab]")
mcp__playwright__snapshot()   ← 捕获 Tab 内容区
```

**状态操作类按钮**（文本含"提交"、"审批"、"通过"、"拒绝"、"撤回"、"Submit"、"Approve"、"Reject"）：
```text
mcp__playwright__click("[按钮]")
mcp__playwright__snapshot()   ← 捕获确认弹窗或状态变化
```

> 若点击失败（按钮 disabled 或不存在），跳过该按钮并在 §8 待确认项中说明。

#### 2c. 提取状态枚举（从 localStorage mock 数据）

查找 HTML 中 `<script>` 标签内的 `localStorage.setItem` 调用，提取 status/state/auditStatus 等字段的枚举值列表。

若发现 SVG 步骤条或 stepper 组件，提取每一步的文本标签作为状态节点。

### 步骤 3 — 汇总各页面提取结果

将步骤 2 对所有页面的提取结果合并，按以下维度汇总：

| 维度 | 内容 |
|---|---|
| 页面列表 | 文件名 → 页面中文标题 → 所属功能 |
| 组件清单 | 搜索框、表格、分页、新增弹窗、编辑弹窗、详情 Tab、步骤条、Timeline、状态 Badge |
| 按钮全集 | 按钮文本 → 所在页面 → 点击触发结果（弹窗/跳转/状态变化） |
| 表单字段全集 | 字段名 → label → 类型（input/select/date）→ 是否必填（label 含"*"） |
| 状态枚举 | 实体名 → 状态值列表（从 localStorage mock 或步骤条提取） |
| 弹窗/抽屉 | 弹窗标题 → 触发按钮 → 内部字段 → 操作按钮 |

### 步骤 4 — API 调用映射

将步骤 3 汇总的按钮与操作，映射到 contracts.md 中的接口：

| 操作 | 触发方式 | 推断接口 | 主要入参 | 映射来源 |
|---|---|---|---|---|
| 新增记录 | 点击"新增"→ 弹窗内"保存" | `create[Entity]` | 表单字段 | contracts.md |
| 编辑记录 | 点击"编辑"→ 弹窗内"保存" | `update[Entity]` | id + 表单字段 | contracts.md |
| 删除记录 | 点击"删除"→ 确认 | `delete[Entity]` | id | contracts.md |
| 查询列表 | 页面初始化/搜索 | `list[Entity]Page` | 搜索字段 + 分页参数 | contracts.md |
| 提交审批 | 点击"提交" | `submit[Entity]` | id | contracts.md |

若 contracts.md 不存在或接口名推断不确定，在"映射来源"列写"待确认"，并在 §8 列出。

### 步骤 5 — 生成 interaction-spec.md

基于步骤 2-4 提取的信息，按 `sdd-frontend-interaction.md` 模板格式生成 `interaction-spec.md`，写入 `specs/modules/<feature>/interaction-spec.md`。

文档结构（每个 HTML 文件生成独立的一级章节，8 节结构在每个页面章节内展开）：

````markdown
# 交互规格：[功能模块名称]

> 来源：由 `/sdd-ui-parse` 通过 Playwright MCP 解析 `spec/09-ui/` 自动生成
> 生成时间：[YYYY-MM-DD]
> 原型文件：[列出解析的 HTML 文件列表]

---

## 一、[页面文件名对应的功能名，如：员工列表页（employee-list.html）]

### 1. 页面目标

- 页面/菜单名称：[从 H1/title 提取]
- 目标用户：[根据功能推断，不确定填"待确认"]
- 核心任务：[从按钮/表单操作推断]
- 推荐 IIDP 实现方式：[根据页面复杂度推断：标准模板/扩展视图/Vue2 组件]
- 是否需要前端代码：是/否

### 2. 用户流程

```text
用户进入页面
  → 系统加载列表数据（分页）
  → 展示 [表格列表 / 初始空态]
  → 用户操作 A：[搜索] → 触发列表刷新
  → 用户操作 B：[点击新增] → 打开新增弹窗 → 填写表单 → 保存
    → 成功：关闭弹窗，刷新列表，Toast 提示成功
    → 失败：显示字段校验错误，弹窗保持打开
  → ...
```

#### 关键路径说明

| 路径 | 触发条件 | 成功结果 | 失败处理 | 业务规则 |
|---|---|---|---|---|
| [从步骤 3 按钮全集填充] | ... | ... | ... | ... |

### 3. 交互状态表

#### 3.1 页面级状态

| 状态 | 触发条件 | 界面表现 |
|---|---|---|
| 初始加载 | 页面打开 | 表格骨架屏/Spinner |
| 数据加载完成 | 接口返回成功 | 显示列表数据 |
| 空数据 | 接口返回空列表 | [Empty state 组件或"暂无数据"] |
| 保存中 | 点击保存后等待接口 | 按钮 loading 状态，禁止重复提交 |

#### 3.2 组件级状态

| 组件 | 状态 | 触发条件 | 表现 |
|---|---|---|---|
| [从步骤 3 汇总的弹窗/按钮填充] | ... | ... | ... |

#### 3.3 权限与错误状态

| 场景 | 表现 |
|---|---|
| 无操作权限 | 对应按钮隐藏或 disabled |
| 字段校验错误 | 字段红框 + 下方错误文字 |
| 服务异常 | Toast 错误提示 |
| 网络错误 | Toast 提示"网络异常，请重试" |

#### 3.4 状态 × 按钮可见规则

> 仅当存在工作流状态时输出此节

| 状态 | 可见按钮 | 隐藏/禁用按钮 |
|---|---|---|
| [从步骤 2c 状态枚举填充] | ... | ... |

### 4. 响应式与容器策略

| 区域 | 宽度策略 | 高度策略 | 滚动归属 |
|---|---|---|---|
| 页面主体 | 100% 减侧边栏 | 视口高度 | 页面滚动 |
| 弹窗 | [从弹窗 class 提取，如 max-w-2xl] | auto / 内容撑开 | 弹窗内部 |
| 表格区域 | 100% | 剩余高度 | 表格内滚动 |

### 5. 无障碍与易用性

- [ ] 所有表单 input 有对应 `<label>` 或 `aria-label`
- [ ] 必填字段有视觉标识（`*` 或 Required 文字）
- [ ] 错误提示在字段附近展示，不仅依赖颜色区分
- [ ] 删除/批量操作前有二次确认弹窗
- [ ] 弹窗可通过 Esc 键关闭
- [ ] 关键操作按钮（保存/提交）可通过 Tab 键聚焦

### 6. 组件清单

| 组件 | 页面位置 | 说明 |
|---|---|---|
| [从步骤 3 汇总填充] | ... | ... |

### 7. API 调用映射

| 用户操作 | 触发方式 | 接口方法 | 主要入参 | 来源 |
|---|---|---|---|---|
| [从步骤 4 填充] | ... | ... | ... | contracts.md / 待确认 |

### 8. 待确认项

| 项目 | 说明 | 优先级 |
|---|---|---|
| [从解析过程中记录的不确定项填充] | ... | 高/中/低 |

---

## 二、[下一个 HTML 文件对应章节]

...
````

### 步骤 6 — 输出摘要

```markdown
## UI 原型解析完成 ✓

### 已生成文件
- specs/modules/[feature]/interaction-spec.md

### 解析概览
| 页面文件 | 识别按钮数 | 表单字段数 | 状态枚举 | API 已映射 |
|---|---|---|---|---|
| [page1].html | N | N | [状态列表] | N/N |
| [page2].html | N | N | — | N/N |

### 待确认项汇总
- [未能映射到 contracts.md 的接口]
- [按钮点击失败（disabled）的场景]
- [localStorage mock 中未发现状态枚举的实体]

### 后续选项
A) 运行 /sdd-spec 继续补全 frontend-spec.md 的技术落地规格
B) 手动补充 interaction-spec.md §8 中的待确认项后再执行 A
```

## 完成标志

- `specs/modules/<feature>/interaction-spec.md` 已写入，包含全部 8 节（§3.4 仅当存在工作流状态时输出）。
- 每个 HTML 文件对应至少 1 个用户操作流程（§2）条目。
- §7 API 调用映射中每条操作有接口引用或明确标注"待确认"。
- 输出摘要包含解析概览、待确认项和后续选项。
