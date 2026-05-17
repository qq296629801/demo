# IIDP 存量项目接入

## 三阶段流程

```text
Phase 1：侦察现有工程
  → 读取 POM、app.json、views、menus、前端 apps 配置和扩展入口

Phase 2：提取现有契约
  → 模型、视图 key、菜单 key、服务、权限、节点 id、数据源

Phase 3：增量修改
  → 只为变更区域写规格和任务，不重写整套系统
```

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
- 推断出的模型、权限、节点 id、数据源名必须写“待确认”。
- 如果现有实现违反 IIDP 当前规范，先记录技术债和影响范围，再设计最小迁移路径。

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

## 风险优先级与 Phase 门控

| 优先级 | 含义 | 门控规则 |
|---|---|---|
| **P0** | 高严重度 + 无现有缓解 | 必须在当前 Phase Implement 前修复或明确降级理由，记入 decisions.md |
| **P1** | 高严重度 + 有部分缓解，或中严重度 + 无缓解 | 本 Phase 内规划修复任务；不阻断开发但须在 validation.md 标注 |
| **P2** | 低严重度或中严重度 + 有充分缓解 | 记入 roadmap.md 技术债，不影响当前 Phase |
