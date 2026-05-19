# IIDP 存量项目接入

## 何时使用存量接入流程

以下场景应使用本指南的存量接入流程，而非全新项目流程：

| 场景 | 说明 |
|---|---|
| 为现有 IIDP 项目新增 App 或功能 | 项目已有 apps.json、多个 App 在生产运行，只需增量扩展 |
| 对遗留 App 模块做现代化改造 | 现有 App 使用非标准视图、直接操作 DOM 或引入了不符合 IIDP 规范的依赖 |
| 将现有项目纳入 SDD 规格管理 | 项目已运行但无 specs/ 目录，需要补充规格文档和能力地图 |
| 跨 App 集成或整合 | 需要打通两个及以上现有 App 的模型、服务或视图 |
| 接手他人遗留 IIDP 工程 | 缺少文档，需要先做侦察和业务规则提取再动手修改 |
| 技术债专项治理 | 已知存在权限漏洞、无规格的自定义扩展或大量标准偏离，需要系统性梳理 |

如果是全新 IIDP 工程（尚无任何 App），使用标准 SDD 新建流程。

---

## 三阶段流程

```text
Phase 1：侦察现有工程
  → 读取 POM、app.json、views、menus、前端 apps 配置和扩展入口

Phase 2：提取现有契约
  → 模型、视图 key、菜单 key、服务、权限、节点 id、数据源

Phase 3：增量修改
  → 只为变更区域写规格和任务，不重写整套系统
```

## 侦察深度选项

根据任务复杂度和时间预算选择侦察深度，默认使用标准侦察：

| 深度 | 时间估算 | 覆盖范围 | 适用场景 |
|---|---|---|---|
| **快速侦察** | 5–10 分钟 | apps.json、app.json、顶层目录结构 | 快速上手陌生工程；初步判断工程规模和 App 数量 |
| **标准侦察**（默认）| 20–40 分钟 | @Model、@MethodService、views、menus、前端 apps/ 扩展入口 | 大多数增量开发场景；新增 App 或功能前的摸底 |
| **深度侦察** | 1–3 小时 | 完整代码路径、技术债、隐式约束、历史决策、数据源配置、跨 App 依赖 | 存量现代化改造；接手遗留工程；技术债专项治理 |

快速侦察只执行证据采集的前两条命令；标准侦察执行完整 EDCR 的 Evidence 步骤；深度侦察在标准侦察基础上补充：

```bash
# 深度侦察追加命令
# 数据源配置
find iidp-backend-demo-ai -name 'ds_config*' -o -name '*datasource*'

# 跨 App 依赖（服务引用）
rg -n 'import.*AppName\|@Autowired' iidp-backend-demo-ai/sie-iidp-demo-apps --include="*.java"

# 历史提交摘要（了解近期变更方向）
git log --oneline --since="3 months ago" -- iidp-backend-demo-ai/sie-iidp-demo-apps/

# 非标准前端引用（识别技术债）
rg -n 'axios\|fetch(\|document\.\|window\.' [frontend-project]/apps --include="*.js"
```

---

## 代码侦察 Prompt

```text
分析当前 IIDP 项目，生成侦察报告：

1. 后端工程结构：POM 聚合、业务模块、启动模块、apps/apps.json。
2. App 契约：app.json、views、data、menus、file。
3. 模型与服务：@Model、@Property、@MethodService、权限码。
4. 前端工程：apps/<appName>/views、common、config、扩展入口。
5. 发现的风险：命名冲突、未登记资源、未确认节点 id、敏感配置。

只做分析，不修改代码。
```

## 业务规则提取 Prompt

```text
分析 [模块路径]，提取业务规则：

1. 用户流程。
2. 后端模型字段、校验、状态流转。
3. 服务入参、出参和副作用。
4. 视图按钮、菜单过滤和权限码。
5. 前端 hook、扩展视图、数据源和节点 id。

对每条规则标注：
- 确定：代码或配置明确体现。
- 推断：根据配置关系推断，需要确认。
- 不确定：缺少事实来源。
```

## 增量修改规则

- 只为变更区域写 `requirements.md`、`plan.md`、`tasks.md` 和 `validation.md`。
- 不试图一次性补全整个存量系统的所有规格。
- 反向提取的事实必须标注来源：代码、配置、日志、页面观察或人工确认。
- 推断出的模型、权限、节点 id、数据源名必须写”待确认”。
- 如果现有实现违反 IIDP 当前规范，先记录技术债和影响范围，再设计最小迁移路径。

---

## 产物生成策略

根据接入目标和存量工程规模选择产物模式：

| 模式 | 产物 | 适用场景 |
|---|---|---|
| **极简模式** | 仅 `specs/integration-map.md` | 快速摸底、只需了解工程全貌而不立即动手 |
| **标准模式** | `integration-map.md` + 核心 App 的 `requirements.md` | 为现有工程新增单个 App 或功能 |
| **完整模式** | 全套 specs（`requirements.md`、`plan.md`、`tasks.md`、`validation.md`） | 大规模现代化改造；需要完整规格追溯的场景 |
| **混合模式**（推荐）| `integration-map.md` + 2–3 个核心 App 的完整规格 | 大多数存量接入场景；平衡规格完整性与投入成本 |

**integration-map.md 最小结构**：

```markdown
# 集成地图

## App 清单
| appName | appPkg | 主要模型 | 主要服务 | 前端扩展入口 |
|---|---|---|---|---|

## 关键约束
- 已占用的节点 id 和权限码（新增时不可冲突）
- 跨 App 依赖关系
- 数据源名称

## 技术债概览
[标准偏离和自定义技术债条目，来自能力分层结果]
```

产物生成 Prompt：

```text
基于 EDCR 分析结果，生成 [选择的模式] 所需产物：
1. 如果包含 integration-map.md：填写 App 清单表格、关键约束列表、技术债概览。
2. 如果包含 requirements.md：只覆盖本次变更范围的 App，不补全整个存量系统。
3. 推断项一律标注「待确认」，并在文档末尾汇总为待确认事项清单。
4. 不生成超出所选模式范围的文档。
```

---

## EDCR 能力发现框架

存量项目接入时，在三阶段流程的 Phase 1–2 基础上，使用 EDCR 四步法产出结构化能力地图和风险底数。

```text
Evidence（证据采集）→ Discovery（领域发现）→ Capabilities（能力分层）→ Risk（风险登记）
```

### E — 证据采集

只读不写，采集原始事实，不做推断：

```bash
# 应用注册
cat iidp-backend-demo-ai/apps/apps.json
find iidp-backend-demo-ai -name 'app.json' | xargs grep -l '"views"'

# 模型与服务
rg -n '@Model\|@MethodService\|@Property' iidp-backend-demo-ai/sie-iidp-demo-apps --include="*.java"

# 视图与菜单
find iidp-backend-demo-ai -name '*.json' -path '*/views/*'
find iidp-backend-demo-ai -name 'menus.json'

# 权限码
rg -n '"auth"' iidp-backend-demo-ai/sie-iidp-demo-apps --include="*.json"
rg -n 'auth\s*=' iidp-backend-demo-ai/sie-iidp-demo-apps --include="*.java"

# 前端扩展
find [frontend-project]/apps -name '*.js' -path '*/views/*'
rg -n 'selector\|hook\.\|ds_config' [frontend-project]/apps --include="*.js"
```

证据采集 Prompt：

```text
请对存量 IIDP 项目做证据采集，只读取、不推断：
1. 列出所有 apps.json 登记的 jar 和 app.json 路径。
2. 列出每个 App 下的模型类（@Model）、服务方法（@MethodService）。
3. 列出视图 key、菜单 key 和权限码（auth）原始值。
4. 列出前端 apps/ 目录的扩展文件路径和 selector 值。
5. 输出格式：每条证据标注来源文件和行号；不加解释，不做推断。
```

### D — 领域发现

基于证据，绘制领域地图：

| 维度 | 发现内容 | 证据来源 |
|---|---|---|
| 应用边界 | appName、appPkg、App 间依赖 | apps.json、app.json |
| 数据模型 | model_name、字段、关系（@OneToMany 等）| Java @Model 文件 |
| 服务契约 | serviceName、auth、入参、出参 | @MethodService 注解 |
| 视图与菜单 | view key、menu key、按钮 service | views/*.json、menus.json |
| 权限体系 | 权限码、角色映射 | auth 参数、种子数据 |
| 前端扩展 | 实现分支、selector、hook、数据源 | apps/<appName>/views/ |

领域发现 Prompt：

```text
基于证据采集结果，生成领域地图（不写代码，只整理结构）：
1. 按 App 列出模型清单（model_name、主要字段、关联关系）。
2. 按模型列出服务（method name、auth、核心入参）。
3. 列出视图 key → 菜单 key → 按钮 service 的三级映射表。
4. 列出前端扩展：selector → hook/ds/command 对应关系。
5. 标出「推断」（根据配置关系推测，未在代码中明确声明）和「不确定」（缺少事实来源）条目。
```

### C — 能力分层

把发现的每个能力点分入四类：

| 分类 | 含义 | 处理建议 |
|---|---|---|
| **标准对齐** | 使用 IIDP 标准 @Model/views/menus/hook，无偏离 | 记录即可，纳入 integration-map |
| **标准偏离** | 使用平台 API 但方式不规范（如用 `type:'page'` 替换标准模板、直接操作 DOM、引入 axios）| 记录技术债，规划最小迁移路径 |
| **自定义扩展（有据可查）** | 自定义 Vue2 组件或复杂 hook，但有注释、文档或规格可追溯 | 纳入规格，补 backend-spec / frontend-spec |
| **自定义扩展（技术债）** | 自定义代码无规格、无注释、逻辑不清 | 标记高优先级技术债，触发 backfill（见 Spec Sync）|

能力分层 Prompt：

```text
基于领域地图，对每个能力点做分类（标准对齐 / 标准偏离 / 自定义有据 / 自定义技术债）：
1. 后端：每个模型和服务各归一类，写明分类依据。
2. 前端：每个扩展实现各归一类，写明分类依据。
3. 汇总：标准偏离和自定义技术债条目列为「需关注项」，按影响范围排序。
4. 不要提修复方案；只做分类，方案在 Risk 步骤决定。
```

### R — 风险登记（IIDP 威胁矩阵）

使用 STRIDE 框架适配 IIDP 场景，对能力分层中的「需关注项」逐条评估：

| 威胁类型 | IIDP 典型表现 | 严重度判断 |
|---|---|---|
| **身份伪造（Spoofing）** | 服务无 `auth` 参数；前端按钮权限码与后端 `@MethodService` auth 不对齐 | 高：任何人可调用写服务 |
| **数据篡改（Tampering）** | 写服务未校验状态流转；未过滤 scope，跨作用域修改数据 | 高：数据完整性破坏 |
| **抵赖（Repudiation）** | 关键操作（删除、状态变更）无操作日志或审计字段 | 中：无法追溯变更来源 |
| **信息泄露（Info Disclosure）** | 查询服务无 scope 过滤，返回跨租户数据；接口返回敏感字段未脱敏 | 高：数据越权访问 |
| **服务拒绝（DoS）** | 列表查询无分页限制；大批量操作无异步保护 | 中：性能劣化或系统不可用 |
| **权限提升（Elevation）** | 菜单/按钮显隐仅靠前端；后端服务无权限校验；`@MethodService` 缺少 `auth` | 高：前端绕过后端权限 |

风险登记表格式：

```markdown
## 风险登记

| ID | 威胁类型 | 涉及能力 | 具体表现 | 严重度 | 现有缓解 | 建议措施 | 处理优先级 |
|---|---|---|---|---|---|---|---|
| R-01 | 权限提升 | OrderService.submit | 无 auth 参数 | 高 | 无 | 补充 auth="order:submit" | P0 立即 |
```

风险建模 Prompt：

```text
基于能力分层的「需关注项」，完成 IIDP 威胁矩阵评估：
1. 按 STRIDE 六类逐条检查每个需关注项。
2. 对每条风险：写明涉及能力（模型/服务/视图/前端节点）、具体表现、严重度（高/中/低）、现有缓解措施。
3. 输出风险登记表（ID、威胁类型、涉及能力、具体表现、严重度、现有缓解、建议措施、处理优先级）。
4. P0（高严重度 + 无缓解）条目必须在进入 Phase Implement 前处理完毕。
5. 不要修改代码；只产出风险登记文档。
```

### EDCR 输出结构

完成 EDCR 后，在 `specs/legacy/<module>-business-rules.md` 中记录：

```markdown
# [模块名] 业务规则与能力地图

## 1. 领域地图（Discovery）
[模型清单、服务契约、视图/菜单映射表、前端扩展]

## 2. 能力分层（Capabilities）
[四分类结果表，含「需关注项」汇总]

## 3. 风险登记（Risk）
[IIDP 威胁矩阵，含 P0 条目列表]

## 4. 增量规格范围（结论）
[基于 EDCR 确定本次 Phase 的变更边界和技术债处理计划]
```

---

## 存量接入最佳实践

1. **从一个小功能验证，逐步扩大**
   先选择最简单的一个 App 或功能点完成完整 EDCR 流程，验证侦察和规格方法在该工程中可行后，再扩展到其他 App。不要一次性对整个工程铺开 EDCR。

2. **尊重现有 IIDP 模式**
   新增代码必须遵循现有工程已确立的 @Model / @MethodService / views / menus 体系。即使发现现有工程存在标准偏离，在未制定迁移计划前，不要在新代码中引入另一套非标准模式。

3. **增量规格演进，版本记录**
   每次 Phase 完成后更新 `integration-map.md`，记录新增的 App、服务、权限码和节点 id。在 `decisions.md` 中记录关键接入决策（如为何选择某个现有视图 key、为何跳过某个技术债）。

4. **重视集成点验证**
   跨 App 依赖、共享数据源、前端 selector 与后端节点 id 的对应关系是最容易出错的集成点。在 `validation.md` 中为每个集成点写明验证步骤，优先于功能性验证执行。

5. **先验证侦察结论再实施**
   EDCR 产出的推断项（标注「待确认」的节点 id、权限码、数据源名）必须在进入 Phase Implement 前与项目负责人或实际运行环境核对。不要基于未确认的推断直接写代码。

6. **用 SDD 记录和管理技术债**
   侦察发现的技术债不要忽略、也不要立即全部修复。统一记录在 `specs/tech-debt.md` 中，标注严重度和影响范围，纳入 roadmap 管理，按 P0/P1/P2 分批处理。

---

## 常见接入场景

### 场景 1：为生产中的 IIDP 项目新增业务 App

适用深度：标准侦察 + 混合模式产物。

关键步骤：
1. 通过 EDCR 确认已有的节点 id 和权限码，新 App 不得冲突。
2. 在 `apps.json` 中确认新 appName 和 appPkg 的命名规则与现有 App 保持一致。
3. 新 App 的 `app.json` 参照现有 App 结构，不引入现有工程中未使用的新配置节点。
4. 前端扩展入口按现有 `apps/<appName>/` 目录规范建立，不新建额外根目录。

### 场景 2：遗留 App 模块现代化

适用深度：深度侦察 + 完整模式产物。

典型问题：
- 非标准视图（`type:'page'` 或直接操作 DOM 的 js 文件）需迁移到标准 menus/views 体系。
- 权限码与后端 `@MethodService` auth 未对齐，需补全后端权限声明。

迁移策略：
1. 先用能力分层标出所有标准偏离项，评估迁移影响范围。
2. 制定最小迁移路径：优先修复 P0 风险（权限类），再逐步迁移视图结构。
3. 每次迁移后保留旧视图 key 作为兼容过渡（若有前端路由依赖），在 `validation.md` 中记录兼容期限。

### 场景 3：多 App 集成整合

适用深度：标准侦察（每个 App 分别执行）+ 混合模式产物。

关键步骤：
1. 为每个参与集成的 App 分别完成 EDCR 的 Discovery 步骤，生成各自的领域地图。
2. 在 `integration-map.md` 中明确标注跨 App 依赖：哪个 App 的服务被另一个 App 调用，共享数据源名称。
3. 集成点的 selector 和 hook 必须在两侧 App 的规格中都有记录，不允许单侧描述。
4. 风险登记重点关注跨 App 的信息泄露（Info Disclosure）和权限提升（Elevation）风险。

---

## 常见问题排查

| 问题 | 可能原因 | 排查方式 |
|---|---|---|
| 侦察结论与实际运行代码不符 | 存在多个同名 App 包、或 apps.json 未登记所有 jar | `find . -name 'apps.json'` 检查是否有多个注册文件；对比实际部署 jar 列表 |
| App 边界不清导致规格重叠 | 两个 App 共用同一模型或服务，但 app.json 分别声明 | 在 Discovery 领域地图中明确标注共享模型归属；选定一个 App 作为模型的"主 App" |
| 推断的节点 id 与实际不一致 | 节点 id 在运行时由平台动态生成，与配置文件中的 key 不是同一层级 | 在实际运行环境中通过开发者工具抓取节点 id，不依赖静态推断 |
| 推断的权限码与实际不一致 | 种子数据中的 auth 值与 @MethodService 注解的 auth 参数拼写不一致 | `rg -n '"auth"' --include="*.json"` 与 `rg -n 'auth\s*=' --include="*.java"` 结果逐条对比 |
| 技术债过多不知从哪下手 | 缺少优先级排序，所有技术债混在一起 | 先完成风险登记（R 步骤），按 P0/P1/P2 分类；仅处理 P0，其余记入 `specs/tech-debt.md` |
| 前端 selector 匹配失败 | selector 值与实际视图 key 或节点 id 不一致 | 在 E 步骤采集时，同时记录 `apps/<appName>/views/` 中的 selector 值和对应的 `views/*.json` 中的 key 值，逐一核对 |

---

## 风险优先级与 Phase 门控

| 优先级 | 含义 | 门控规则 |
|---|---|---|
| **P0** | 高严重度 + 无现有缓解 | 必须在当前 Phase Implement 前修复或明确降级理由，记入 decisions.md |
| **P1** | 高严重度 + 有部分缓解，或中严重度 + 无缓解 | 本 Phase 内规划修复任务；不阻断开发但须在 validation.md 标注 |
| **P2** | 低严重度或中严重度 + 有充分缓解 | 记入 roadmap.md 技术债，不影响当前 Phase |
