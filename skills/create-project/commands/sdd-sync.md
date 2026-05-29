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
2. 收集变更文件列表（三个来源合并去重）：
   - `git diff HEAD --name-only`（未提交的暂存/工作区变更）
   - `git diff $(git merge-base HEAD origin/$(git symbolic-ref --short HEAD 2>/dev/null || echo main) 2>/dev/null || git merge-base HEAD origin/main 2>/dev/null || echo HEAD) HEAD --name-only`（已 commit 但未推送的变更；若 origin 不可达则跳过）
   - `.spec-sync-queue.txt`（如存在，PostToolUse hook 写入的待同步队列）
   
   > 若拉取 origin 比对失败（无网络或首次分支），降级为仅使用 `git diff HEAD --name-only` + 队列文件，并提示用户："无法比对远端，已 commit 的变更可能未被感知，建议手动传入功能目录参数确认范围。"
3. 过滤：排除 `specs/` 目录内文件、非代码文件（保留 `.java`、`.json`、`.vue`、`.js`、`.ts`、`.xml`）。
4. 若过滤后列表为空 → 输出"未检测到代码变更，规格书已同步"，退出。
5. 若 `git diff HEAD` 总行数 > 500 → 先输出变更摘要，暂停等用户确认再继续。

## 文件 → 规格章节映射

> 执行前先读取 `skills/backend/references/core/capability-map.md` 确认能力域，再按下表映射。

### 后端代码 → `backend-spec.md` / `contracts.md`

> IIDP 工程有两种模型文件布局，识别时两者都要匹配：
> - **标准布局**：`{moduleName}/model/{ModelName}.java`（绝大多数模块，如 `classmgr/model/ExampleClass.java`）
> - **遗留/顶层布局**：`model/{ModelName}.java`（直接放在 appPkg 根目录下的 model 文件夹）

| 变更文件路径模式 | 更新规格文件 | 更新章节 |
|---|---|---|
| `{moduleName}/model/{ModelName}.java` 或 `model/{ModelName}.java` | `backend-spec.md` | §3 模型设计（字段表行）、§4 服务设计（内置 CRUD 服务签名） |
| `{moduleName}/views/{model_name}_view.json` | `backend-spec.md` | §5 视图与菜单（视图 key、按钮配置行） |
| `views/menus.json` | `backend-spec.md` | §5 视图与菜单（菜单树结构） |
| `data/{model_name}.json`（种子数据） | `backend-spec.md` | §6 数据与权限（种子数据说明） |
| `{moduleName}/service/{ServiceName}Service.java` | `backend-spec.md` **+** `contracts.md` | `backend-spec.md §4`（@MethodService 签名）；同步将新增/变更服务的 `service name`、`args`、`权限码` 写入 `contracts.md` 对应模型的服务契约表 |
| `{moduleName}/service/model/request/*.java` / `response/*.java` | `backend-spec.md` + `contracts.md` | `backend-spec.md §4`（DTO 字段）；若 args 类型变更同步更新 `contracts.md` args 列 |
| `consts/*.java` / `enums/*.java` | `backend-spec.md` | §6 数据与权限（枚举说明）— 仅影响用户可见业务时更新 |
| `{appPkg}/app.json`（模块内 App 描述文件） | `backend-spec.md` | §1 命名（product/productDesc/productSequence）、§2 工程文件 |
| `apps/apps.json`（项目根 jar 注册） | `backend-spec.md` | §2 工程文件（jar 注册行） |
| `pom.xml`（模块新增/依赖变更） | `backend-spec.md` | §2 工程文件（Maven 模块注册） |

> 参考：`skills/backend/SKILL.md` Step 1～10（后端产物规则）、`skills/create-project/references/sdd-backend.md`（章节模板）、`skills/create-project/references/sdd-contracts.md`（contracts.md args 类型映射规则）

### 前端代码 → `frontend-spec.md`

| 变更文件路径模式 | 更新规格文件 | 更新章节 |
|---|---|---|
| `*.vue` / `*.js` / `*.ts`（业务组件/页面） | `frontend-spec.md` | 节点树、ds_config 数据源、bind_/bind_on_ 绑定、commands |
| `extension/*.js` / hook 文件 | `frontend-spec.md` | § 扩展视图/hook 列表（扩展类型、selector、目标节点 id、回调职责） |

> 参考：`skills/frontend/references/iidp-frontend-spec-doc/SKILL.md`（前端规格文档规则）、`skills/create-project/references/sdd-frontend.md`（章节模板）

## 执行步骤

1. **读取当前规格文件**：在做任何修改前，先读取目标规格文件（`backend-spec.md`、`frontend-spec.md`、`contracts.md`）的当前内容，记录各章节现有状态，避免重复写入或覆盖人工标注的内容。
2. **逐文件拉取 diff**：对每个受影响代码文件，优先执行 `git diff $(git merge-base HEAD origin/...) HEAD -- <file>`，回退时用 `git diff HEAD -- <file>`。
3. **语义分析**：识别 diff 中变化的：
   - 后端：字段名/类型/注解、服务方法签名/入参出参、视图 key、菜单 key、权限码
   - 前端：节点 id、ds_config 配置、事件名（bind_on_）、commands 调用、hook 路径
4. **最小外科式更新**：
   - 只修改映射到的规格章节中对应的行、表格行或小节
   - **不重写整个规格文件**，不修改无关章节
   - 若某章节不存在（规格未生成过）→ 在章节末尾标注"⚠ 待补充：发现代码变更但规格章节缺失，请先运行 `/sdd-spec` 生成完整规格"
   - 不修改 `requirements.md`、`plan.md`（需求与计划是人工决策产物）
   - 服务变更时，`backend-spec.md §4` 和 `contracts.md` 必须同步更新，不能只改其中一个
5. **输出变更摘要**：列出每个被更新的规格文件、章节、具体变更项（表格形式）。
6. **清空队列**：若 `.spec-sync-queue.txt` 存在，清空其内容（`> .spec-sync-queue.txt`）。

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
