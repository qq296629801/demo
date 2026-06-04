# Prompt、工具与核心原则

## Prompt 速查表

### 项目启动

| 场景 | Prompt 模板 |
|---|---|
| 建立项目宪法 | `基于当前 IIDP 项目，为 [项目描述] 创建 mission.md、iidp-stack.md、ui-constitution.md、roadmap.md、integration-map.md。先读取 skills/backend 和 skills/frontend。` |
| 创建 UI 宪法 | `基于 IIDP 标准模板、在线视图和原型来源，为 [项目/页面] 创建 ui-constitution.md，包含模板视觉约束、节点规则、容器/iframe、可用性和原型落地规则。` |
| 识别能力域 | `读取需求并映射到 IIDP 后端能力域和前端实现分支，列出需要读取的专题文件。不要写代码。` |

### 功能规格

| 场景 | Prompt 模板 |
|---|---|
| 后端规格 | `为 [功能] 写 IIDP 后端规格，包含命名、模型、服务、视图、菜单、数据、权限和验收。` |
| 前端规格 | `为 [页面] 写 IIDP 前端规格，先判断标准模板/在线视图是否满足；如需扩展，列出 hook、节点 id 和数据源。` |
| UI 原型补全 | `请先提示我补充任意支持 MCP 的原型/设计/产品工具服务名称、资源 URI、页面或 Frame；读取 MCP 原型后，补全 UI 原型描述、状态清单和原型到 IIDP 的映射。` |
| 交互节点规格 | `把 [页面/原型] 转成 IIDP 节点规格，输出页面节点树、selector、ds_config、bind_、bind_on_、commands、hook 和验收点。` |
| 前后端契约 | `为 [功能] 生成前后端契约表，包含 model、service、args、view key、menu key、auth、前端实现方式和待确认事项。` |

### 规格质量门控

| 场景 | Prompt 模板 |
|---|---|
| 规格批判（Critique） | `对 [requirements.md] 做 Critique：从产品战略视角质疑功能价值和边界，从工程风险视角识别待确认阻塞项和漂移风险。不修改规格，只输出发现报告。` |
| 代码蓝图（Blueprint） | `在开始实现前，为 [功能名称] 生成代码蓝图：后端文件清单（路径/类型/关键内容）和前端扩展清单（路径/扩展类型/selector/id来源）。不写真实代码，只描述结构。` |
| 漂移检测（Spec Sync） | `对 specs/modules/[feature]/ 和当前工程代码做漂移检测，按"代码超前/规格超前/真实冲突"分类列出每条漂移，给出最小处理建议，不修改文件。` |
| PR 描述（PR Bridge） | `基于 specs/modules/{moduleName}/ 目录生成 PR 描述，包含功能概述、变更范围（后端/前端）、规格目录链接、验收清单、待确认事项。` |
| 规格澄清（Clarify） | `读取 specs/modules/[feature]/requirements.md，对功能范围、领域模型、交互流程、非功能属性、集成依赖、边界条件、约束权衡、术语命名、完成信号、IIDP 特有待确认项 10 个维度做歧义扫描；生成 ≤5 个高影响力澄清问题（每题附 2–4 个选项或简答指引）；等用户回答后，将答案写入 requirements.md 的 ## Clarifications 节，并更新受影响章节。不进入 Plan 之前不得跳过此步骤。` |
| AC→TC 提取 | `读取 specs/modules/[feature]/requirements.md 的验收标准，生成测试用例规格：后端服务测试（TC-BE-xx，覆盖正常流程/状态拒绝/权限拦截/必填校验）和前端验收场景（TC-FE-xx，覆盖加载/增删改查/权限显隐/空态异常态）。输出写入 validation.md 测试用例规格节，不写代码。` |
| 测试缺口扫描 | `扫描 specs/modules/[feature]/validation.md 的测试覆盖率追踪表，列出所有"⬜ 待执行"的 TC-ID；按严重度分类（Critical：权限/数据完整性；Medium：用户可见流程；Low：内部逻辑）；给出每条 TC 的推荐执行顺序和前置条件。仅报告，不修改文件。` |

### 实现计划与任务

| 场景 | Prompt 模板 |
|---|---|
| 生成计划 | `读取 requirements.md，生成 IIDP 实现计划。后端按模型/服务/视图/菜单拆分，前端按实现分支拆分。` |
| 生成任务 | `读取 plan.md，生成文件级任务清单，每个任务可独立验证。不要开始实现。` |
| 执行任务 | `执行 tasks.md 中第一个未完成任务，完成后运行对应验证，然后停止汇报。` |

### 验证与复盘

| 场景 | Prompt 模板 |
|---|---|
| 后端验证 | `对照 skills/backend/references/core/validation-checklist.md 检查当前改动，列出通过、失败和无法验证项。` |
| 前端验证 | `对照 skills/frontend 的扩展规则检查当前改动，确认节点 id、hook、扩展类型、数据源和构建验证。` |
| 阶段复盘 | `Phase N 完成，请更新 roadmap.md、integration-map.md 和 decisions.md，记录技术债和下阶段重点。` |
| 失败诊断 | `验证失败，请先分类为规格问题、实现问题、环境问题、平台能力限制或原型输入不足，再给出证据和最小修复方案。` |
| 更新变更记录 | `基于本次已完成需求，更新 CHANGELOG.md 的 [未发布] 部分，并同步 decisions.md 中影响后续实现的决策。` |
| EDCR 能力发现 | `对存量 IIDP 项目执行 EDCR 四步法：E（证据采集，只读不推断）→ D（领域地图：模型/服务/视图/菜单/前端扩展）→ C（能力分层：标准对齐/标准偏离/自定义有据/自定义技术债）→ R（IIDP 威胁矩阵：STRIDE 六类）。输出写入 specs/legacy/[module]-business-rules.md，不修改工程代码。` |

## 工具与命令

### 后端常用命令

```bash
git status --short
git diff --check

cd iidp-backend-demo-ai
mvn -s ./settings.xml -DskipTests clean package
docker compose config
docker compose up -d --build
```

### 前端常用命令

```bash
tech --help
tech project <projectName>
tech app <appName>
npm run init:tech
npm run start
npm run build
```

### 原型 MCP 和页面验证

```text
list_mcp_resources              # 查找任意支持 MCP 的原型/设计/产品工具资源
list_mcp_resource_templates     # 查找参数化原型资源模板
read_mcp_resource               # 读取用户指定原型 URI
Browser                         # 打开本地前端页面，检查布局、交互和响应式
```

### CI/CD 和协作工具

- GitLab：代码评审、分支合并、流水线状态和制品追踪。
- Jenkins：后端 Maven 构建、Docker Compose 或部署脚本编排。
- 私服/制品库：Maven 依赖和 IIDP 业务 jar 的发布与加载路径确认。
- 环境配置：MySQL、Redis、MinIO、XXL-Job 等运行依赖按当前工程配置验证。

### 代码和文档检索

```bash
rg -n "@Model|@MethodService|app.json|menus.json" iidp-backend-demo-ai
rg -n "type:|selector|hook|ds_config|bind_|commands" [frontend-project]/apps/<appName>
rg -n "待确认|TODO|FIXME" specs
```

## 10 条不变原则

1. **先识别 IIDP 能力域**：不要直接套通用页面或接口模板。
2. **后端契约是主干**：模型、视图、菜单、服务、权限先稳定，前端再决定是否扩展。
3. **标准模板优先**：传统后台能用在线视图完成时，不新增前端代码。
4. **hook 优先于重写页面**：查询、保存、权限、按钮控制优先走标准页 hook。
5. **节点 id 不猜测**：没有稳定来源就写待确认或询问。
6. **权限后端兜底**：前端显隐不能替代服务端校验。
7. **资源必须登记**：新增视图、菜单、种子、文件和 jar 都要进入对应清单。
8. **原型先有来源**：需要 UI 原型描述时，先要 MCP、截图或文字来源，并标记可信度。
9. **复盘更新契约**：Phase 完成后同步 roadmap、integration-map、decisions 和 CHANGELOG。
10. **验证先于完成声明**：没有新鲜验证结果，不说已完成。

## 常见陷阱

| 陷阱 | 后果 | 预防 |
|---|---|---|
| 把后台管理页写成自定义前端页面 | 绕过 IIDP 标准能力，维护成本高 | 先判断标准模板和在线视图 |
| 只写 Java 模型，不登记视图和菜单 | 引擎可加载但页面不可用 | 对照 `app.json.view/data` 和 `menus.json` 检查 |
| 按按钮文案猜节点 id | 扩展不生效或误命中 | 使用用户提供或标准模板规则库 |
| 只做前端按钮禁用 | 可绕过权限 | 后端服务二次校验 |
| 自定义查询服务不支持 Filter | 搜索、分页、菜单过滤失效 | 保留平台 Filter 契约 |
| 忽略 `apps/apps.json` | jar 未被引擎加载 | 构建后同步 jar 并登记 SDK |
| 复制旧前端应用 | 按需加载和扩展入口混乱 | 使用 `tech app` 创建 |
| 修改生成目录或底座源码 | 构建后丢失或升级冲突 | 只改业务扩展源码 |
| 原型描述没有 MCP 或截图来源 | UI 规格不可追溯 | 先提示用户补充任意支持 MCP 的原型工具服务和资源 URI |
| 阶段结束不更新契约文档 | 后续实现依据漂移 | 复盘时同步 roadmap、integration-map、decisions、CHANGELOG |
