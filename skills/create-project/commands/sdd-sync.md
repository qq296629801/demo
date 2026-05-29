---
description: 【SDD Spec Sync】AI 直接改动代码后，通过 git diff 感知变更，精准更新受影响的规格书章节。一次只处理当前活动功能的规格，完成后输出变更摘要。
handoffs:
  - label: 运行验收 Validate
    command: sdd-validate
    prompt: 规格同步完成，开始验收
    send: false
---

# /sdd-sync

## 用途

当 AI 直接对代码做了修改（未走 `/sdd-implement` 流程），规格书可能与代码产生漂移。
本命令通过分析 git diff，将变更精准映射到对应规格章节，做最小外科式更新，避免手工维护规格书。

## 用户输入

```text
$ARGUMENTS
```

可传入具体功能目录路径（如 `specs/features/phase1-student-mgr/`），默认从 CLAUDE.md 读取当前活动功能。

## 前置步骤

1. 从 CLAUDE.md `<!-- IIDP-SDD START -->` 标记读取当前活动功能目录（`specs/features/<feature>/`）。
2. 收集变更文件列表（两个来源合并去重）：
   - `git diff HEAD --name-only`（未提交变更）
   - `.spec-sync-queue.txt`（如存在，PostToolUse hook 写入的待同步队列）
3. 过滤：排除 `specs/` 目录内文件、非代码文件（保留 `.java`、`.json`、`.vue`、`.js`、`.ts`、`.xml`）。
4. 若过滤后列表为空 → 输出"未检测到代码变更，规格书已同步"，退出。
5. 若 `git diff HEAD` 总行数 > 500 → 先输出变更摘要，暂停等用户确认再继续。

## 文件 → 规格章节映射

> 执行前先读取 `skills/backend/references/core/capability-map.md` 确认能力域，再按下表映射。

### 后端代码 → `backend-spec.md`

| 变更文件路径模式 | 更新规格文件 | 更新章节 |
|---|---|---|
| `{moduleName}/model/{EntityName}.java` | `backend-spec.md` | §3 模型设计（字段表行）、§4 服务设计（内置 CRUD 服务签名） |
| `{model_name}_view.json` | `backend-spec.md` | §5 视图与菜单（视图 key、按钮配置行） |
| `data/menus.json` / `data/{model_name}.json` | `backend-spec.md` | §5 视图与菜单（菜单树）、§6 数据与权限（种子数据说明） |
| `{moduleName}/service/{EntityName}Service.java` | `backend-spec.md` | §4 服务设计（自定义 @MethodService 签名及入参出参） |
| `{moduleName}/service/model/request/*.java` / `response/*.java` | `backend-spec.md` | §4 服务设计（对应服务的入参/出参 DTO 字段说明） |
| `consts/{Const}.java` / `enums/*.java` | `backend-spec.md` | §6 数据与权限（枚举说明）— 仅影响用户可见业务时更新 |
| `apps/apps.json` | `backend-spec.md` | §2 工程文件（jar 注册行） |
| `pom.xml`（模块新增/依赖变更） | `backend-spec.md` | §2 工程文件（Maven 模块注册） |

> 参考：`skills/backend/SKILL.md` Step 1～10（后端产物规则）、`skills/create-project/references/sdd-backend.md`（章节模板）

### 前端代码 → `frontend-spec.md`

| 变更文件路径模式 | 更新规格文件 | 更新章节 |
|---|---|---|
| `*.vue` / `*.js` / `*.ts`（业务组件/页面） | `frontend-spec.md` | 节点树、ds_config 数据源、bind_/bind_on_ 绑定、commands |
| `extension/*.js` / hook 文件 | `frontend-spec.md` | § 扩展视图/hook 列表（扩展类型、selector、目标节点 id、回调职责） |

> 参考：`skills/frontend/references/iidp-frontend-spec-doc/SKILL.md`（前端规格文档规则）、`skills/create-project/references/sdd-frontend.md`（章节模板）

## 执行步骤

1. **逐文件拉取 diff**：对每个受影响代码文件执行 `git diff HEAD -- <file>`，获取具体变更内容。
2. **语义分析**：识别 diff 中变化的：
   - 后端：字段名/类型/注解、服务方法签名/入参出参、视图 key、菜单 key、权限码
   - 前端：节点 id、ds_config 配置、事件名（bind_on_）、commands 调用、hook 路径
3. **最小外科式更新**：
   - 只修改映射到的规格章节中对应的行、表格行或小节
   - **不重写整个规格文件**，不修改无关章节
   - 若某章节不存在（规格未生成过）→ 在章节末尾标注"⚠ 待补充：发现代码变更但规格章节缺失，请先运行 `/sdd-spec` 生成完整规格"
   - 不修改 `requirements.md`、`plan.md`（需求与计划是人工决策产物）
4. **输出变更摘要**：列出每个被更新的规格文件、章节、具体变更项（表格形式）。
5. **清空队列**：若 `.spec-sync-queue.txt` 存在，清空其内容（`> .spec-sync-queue.txt`）。

## 输出格式

```
## Spec Sync 摘要

| 代码文件 | 规格文件 | 章节 | 变更说明 |
|---|---|---|---|
| area/model/ExampleArea.java | backend-spec.md | §3 模型设计 | 新增字段 `remark: String`，加入字段表 |
| example_area_view.json | backend-spec.md | §5 视图与菜单 | 新增 tbar 按钮 `exportExcel`，更新按钮配置行 |

已清空 .spec-sync-queue.txt
```

## 完成标志

- 所有受影响规格章节已按最小变更原则更新。
- 输出变更摘要表。
- `.spec-sync-queue.txt` 已清空（若存在）。
- **不批量重写规格**，不声明未实际完成的更新。
