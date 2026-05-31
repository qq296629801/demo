# OSS 验证指南（Playwright MCP）

> 用于 `/evolve-oss-validate` 命令，通过 Playwright MCP 解析开源网站 Demo，提取功能清单并与 IIDP 生成代码对比。

---

## 一、Playwright MCP 工具速查

| 工具 | 用途 | 注意事项 |
|---|---|---|
| `mcp__playwright__navigate` | 打开 URL（支持 https:// 和 file:///） | 等待 load 事件完成 |
| `mcp__playwright__snapshot` | 获取 accessibility tree（文字、按钮、输入框、表格） | 比 screenshot 更结构化，优先用 |
| `mcp__playwright__screenshot` | 截图存 PNG | 用于存档，不用于解析 |
| `mcp__playwright__click` | 点击按钮/链接 | 点击后需再次 snapshot |
| `mcp__playwright__fill` | 填充输入框 | 用于搜索/登录表单 |
| `mcp__playwright__select` | 选择下拉选项 | — |
| `mcp__playwright__hover` | 悬停（触发 tooltip/下拉菜单） | — |
| `mcp__playwright__evaluate` | 执行 JS 脚本 | 获取 localStorage、隐藏元素内容 |

---

## 二、标准访问流程

### 步骤 1：访问首页 + 检测登录状态

```
mcp__playwright__navigate(url)
snapshot = mcp__playwright__snapshot()

if "登录" in snapshot OR "login" in snapshot URL:
  → 执行登录流程（见下方 §三）
else:
  → 直接进入功能发现
```

### 步骤 2：功能发现（提取导航菜单）

```
snapshot = mcp__playwright__snapshot()
从 accessibility tree 提取：
  - nav 下所有 menuitem / link / button 文本
  - 侧边栏所有展开项
  - Tab 标签
记录为：FEATURE_LIST = [功能名称, URL/路由, 父级菜单]
```

### 步骤 3：逐功能分析

对每个功能模块：
```
1. mcp__playwright__navigate(feature_url)
2. mcp__playwright__screenshot() → 存 oss-screenshots/{feature}-list.png
3. mcp__playwright__snapshot() → 提取：
   - 页面标题（h1/h2）
   - 表格列名
   - 搜索区域字段
   - 操作按钮（新增/编辑/删除/导出/审批）
   - 状态枚举值（下拉选项/Badge 文本/步骤条节点）
4. 点击"新增"或"编辑"按钮：
   mcp__playwright__click("新增" OR "添加" OR "创建")
   mcp__playwright__snapshot() → 提取：
   - 表单字段（label + input type）
   - 必填标记（*）
   - 下拉选项值
5. 记录：PAGE_ANALYSIS = {title, columns, search_fields, buttons, status_values, form_fields}
```

---

## 三、登录处理策略

### 策略 A：使用已知 Demo 账号

常见开源系统 Demo 账号（优先尝试）：

| 系统 | 账号 | 密码 | Demo URL |
|---|---|---|---|
| RuoYi | admin | admin123 | http://demo.ruoyi.vip |
| RuoYi-Vue | admin | admin123 | http://vue.ruoyi.vip |
| JEECG | admin | admin123 | http://boot.jeecg.com |
| Yudao Cloud | admin | admin123 | http://dashboard.yudao.iocoder.cn |
| Sa-Token Demo | 按页面提示 | — | — |

登录操作：
```
mcp__playwright__fill("#username OR input[name=username]", "admin")
mcp__playwright__fill("#password OR input[name=password]", "admin123")
mcp__playwright__click("button[type=submit] OR .login-btn")
mcp__playwright__navigate(dashboard_url)  # 确认登录成功
```

### 策略 B：检测并跳过需登录的功能

```
if snapshot 包含 "请先登录" OR "401" OR "Unauthorized":
  → 记录为：REQUIRES_AUTH = true
  → 跳过该功能，在报告中标注"需授权访问"
```

### 策略 C：截图存档后人工确认

如果登录失败（CAPTCHA/验证码等）：
```
mcp__playwright__screenshot() → 存 oss-screenshots/login-blocked.png
记录：LOGIN_BLOCKED = true
在报告中说明限制，使用功能的公开可见部分继续分析
```

---

## 四、动态内容处理

### 等待策略

Playwright MCP 的 navigate 默认等待 load 事件。对于 SPA（Vue/React）：
```
# 先 navigate，然后等待关键元素出现
mcp__playwright__navigate(url)
# 如果 snapshot 返回空内容或 loading 状态，等待后重试
mcp__playwright__snapshot()  # 通常 SPA 会在 navigate 完成后渲染
```

### 分页内容

```
# 只分析第一页（不翻页，减少 token）
snapshot 中的表格列 = 所有分页共有的列
```

### Tab 切换

```
tabs = snapshot 中 [role=tab] 元素列表
for tab in tabs:
  mcp__playwright__click(tab)
  mcp__playwright__snapshot() → 记录该 Tab 的内容
```

---

## 五、功能提取模板

每个功能模块分析后，填写以下结构：

```json
{
  "module": "功能模块名",
  "url": "访问URL",
  "pages": {
    "list": { "exists": true, "columns": [], "search_fields": [], "buttons": [] },
    "form": { "exists": true, "fields": [], "required_fields": [] },
    "detail": { "exists": false },
    "workflow": { "exists": false, "states": [] }
  },
  "status_values": [],
  "special_operations": [],
  "relations": []
}
```

---

## 六、已验证的开源系统（推荐用于测试）

| 系统 | 特点 | 适合验证的 Gap |
|---|---|---|
| **RuoYi** (http://demo.ruoyi.vip) | 标准 CRUD + 权限管理 | G1(ID/字段), G6(权限), G7(校验) |
| **JEECG Boot** (http://boot.jeecg.com) | 低代码平台，含工作流 | G5(状态流转), G4(详情页) |
| **Yudao Cloud** (http://dashboard.yudao.iocoder.cn) | 完整业务系统，含审批 | G5(审批服务), G4(workflow页) |
| **pig** | 微服务架构 | G8(接口规范), G2(服务分层) |

---

## 七、报告输出格式

分析完成后输出 `oss-validation-report.md`：

```markdown
# OSS 验证报告

**目标系统**：[系统名] ([URL])  
**验证时间**：[timestamp]  
**登录状态**：成功/失败/跳过  

## 功能覆盖对比

| 功能 | OSS 存在 | IIDP 生成 | 差距说明 |
|---|---|---|---|
| 用户列表 | ✅ | ✅ | — |
| 用户详情页 | ✅ | ❌ | 缺少 detail.html |
| 审批流程 | ✅ | ❌ | 缺少 workflow 页和 approve/reject 服务 |

## 合规评分

| 维度 | 得分 | 满分 | 诊断 |
|---|---|---|---|
| D1 功能覆盖率 | X | 20 | [1句] |
| D2 页面完整性 | X | 15 | [1句] |
| ... | | | |
| **总分** | **X** | **100** | |

## 识别的 Gap（对应 gap-taxonomy.md）

- G4-1：[具体描述] → 建议修复 llm-prompts.md Prompt 8
- G5-2：[具体描述] → 建议修复 sdd-backend.md §4

## 截图存档

- [oss-screenshots/功能名-list.png]
```
```

---

## 八、注意事项

- **不要抓取大量数据**：每个功能只需首页 + 新增/编辑弹窗，不需要翻页或深度操作
- **优先 snapshot 而非 screenshot**：snapshot 返回结构化文本，更容易提取字段名和按钮名；screenshot 只用于存档
- **无法访问时记录而非跳过**：网络失败、CAPTCHA、需要真实账号等情况需在报告中说明，不能假装功能不存在
- **不要填写真实个人数据**：测试表单时使用 "测试" + 时间戳等虚构数据
