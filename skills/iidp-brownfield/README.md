# IIDP Brownfield Skill 维护说明

`skills/iidp-brownfield` 是 IIDP 存量项目的 SDD 编排入口，负责把 code-index、create-project、backend、frontend 等 skill 按正确顺序串起来。本身不重新实现 SDD，也不维护独立规则副本。

## 目录职责

| 路径 | 职责 |
|---|---|
| `SKILL.md` | 存量项目 SDD 编排入口，11 步流程：定位项目 → code-index → brownfield-init → requirements → delta-spec → target-spec → contracts → 技术规格 → plan → tasks → implement → sync |
| （无独立 references） | 所有规则复用 `skills/create-project`、`skills/backend`、`skills/frontend` 的 references |

## 与其他 Skill 的关系

| 依赖 Skill | 使用场景 |
|---|---|
| `skills/code-index` | Step 1：生成 baseline-spec，Step 11：final diff 刷新 |
| `skills/create-project` | Step 2–9：brownfield-init、specify、contracts、spec、plan、tasks、implement、sync |
| `skills/backend` | Step 10.2：后端模型 → 服务 → 控制器 → 权限 → 菜单 → 数据种子 |
| `skills/frontend` | Step 10.3：前端工程存在性检查 → 子技能路由 → codegen-protocol 合规扫描 |

## 核心原则

- **SDD 是主流程**，code-index 只负责提供存量上下文。
- **不维护独立规则副本**：所有前端分支判断、组件规则、扩展格式、禁止项和合规扫描均以 `skills/frontend/references/iidp-frontend-codegen-protocol.md` 为唯一执行标准。
- **不维护前端速查清单**：brownfield 只负责路由到 `iidp-frontend` 和 `iidp-frontend-codegen-protocol`。
- **baseline-spec 表示当前代码事实**，默认只读；开发者需求先转成 delta-spec，再与 baseline 合成 target-spec。
- **不全量覆盖存量 App**，不修改无关模块。
- **不编造** appName、model、service、selector、权限码、节点 id 或运行时参数。

## 前端验证

存量项目前端代码生成后，用户可手动运行 `/iidp-frontend-test` 进行浏览器级验证（静态合规 → 启动 → Playwright）。该命令由 `skills/frontend/commands/iidp-frontend-test.md` 定义，brownfield 不维护独立副本。

## 快速检查命令

在仓库根目录执行：

```powershell
# 确认 brownfield 未复制前端规则
rg -n --encoding utf8 "前置门禁|实现后合规扫描|codegen-protocol" skills/iidp-brownfield

# 确认 brownfield 未复制后端规则
rg -n --encoding utf8 "@Model|@Service|@Permission" skills/iidp-brownfield

# 确认 brownfield 只引用而不复制 SDD 流程
rg -n --encoding utf8 "skills/create-project|skills/backend|skills/frontend" skills/iidp-brownfield
```
