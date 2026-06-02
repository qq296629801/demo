# IIDP SDD Workflow

## 概述

`iidp-sdd.yml` 是 IIDP SDD 完整开发周期的自动化 workflow，参照 spec-kit 的 workflow 引擎规范设计。

## 核心循环结构

spec-kit 的 workflow 引擎提供三种控制流原语，本 workflow 全部使用：

### 1. `do-while` — 至少执行一次，条件为真时重复

用于"问了才知道要不要再问"的场景：

```
Clarify 循环（最多 5 轮）
  └─ do-while condition: user.choice == "more"
       ├─ [clarify] 扫描 10 维度歧义，生成 ≤5 个问题
       └─ [gate]    用户回答后决定：done | more

Critique 循环（最多 3 轮）
  └─ do-while condition: user.choice == "revise"
       ├─ [critique] 产品战略 + 工程风险双视角批判
       └─ [gate]     approve | revise

Implement 逐任务循环（最多 100 次）
  └─ do-while condition: tasks.md 还有未勾选任务
       ├─ [implement-task] 执行下一个未完成任务
       └─ [task-status]   shell 检查 tasks.md 剩余数量

Validate 修复循环（最多 3 轮）
  └─ do-while condition: validate.has_failures == true
       ├─ [validate] 按 validation-checklist 检查
       └─ [gate]     fix | accept
```

### 2. `fan-out` + `fan-in` — 并行分发，等待汇总

用于可以同时进行的独立任务：

```
Spec 并行生成（并发 2）
  └─ fan-out items: ["backend", "frontend"]
       ├─ Agent A → backend-spec.md
       └─ Agent B → frontend-spec.md
  └─ fan-in  等两个 Spec 都完成后继续

深度审查（并发 5）
  └─ fan-out items: [spec-consistency, backend-alignment,
                     frontend-alignment, security-boundaries,
                     ai-operability]
       └─ 5 个只读子 Agent 并行输出发现报告
  └─ fan-in  协调 Agent 汇总，列出高优先级问题
```

### 3. `gate` — 人工确认门控

用于需要用户介入决策的节点：

| Gate | options | on_reject |
|---|---|---|
| plan-gate | approve / reject | abort（终止 workflow） |
| blueprint-gate | generate / skip | skip（跳过蓝图） |
| blueprint-confirm | approve / reject | abort |
| validate-gate | fix / accept | skip（接受当前状态） |
| phase-end-gate | next-phase / done | skip（自然结束） |

## 完整流程图

```
[specify]          能力识别 → requirements.md
    │
[clarify-cycle]   do-while ──┐
    │  clarify + gate        │ choice == "more"（最多 5 轮）
    │                   ─────┘
    │
[contracts]       integration-map.md + contracts.md（强制）
    │
[critique-cycle]  do-while ──┐
    │  critique + gate       │ choice == "revise"（最多 3 轮）
    │                   ─────┘
    │
[spec-parallel]   fan-out ──▶ backend-spec.md
    │                    └──▶ frontend-spec.md（并发 2）
[spec-collect]    fan-in（等两个 Spec 完成）
    │
[plan]            plan.md
[plan-gate]       ★ 门控：approve / reject(abort)
    │
[tasks]           tasks.md + validation.md TC 提取
    │
[blueprint-gate]  ★ 门控：generate / skip
[blueprint-branch] if generate → blueprint + confirm-gate
    │
[implement-cycle] do-while ──┐
    │  implement-task        │ has_pending == true（最多 100 次）
    │  task-status           │
    │                   ─────┘
    │
[validate-cycle]  do-while ──┐
    │  validate              │ has_failures == true（最多 3 轮）
    │  validate-gate         │
    │                   ─────┘
    │
[deep-review]     fan-out ──▶ spec-consistency
    │                    ├──▶ backend-alignment
    │                    ├──▶ frontend-alignment
    │                    ├──▶ security-boundaries
    │                    └──▶ ai-operability（并发 5）
[review-collect]  fan-in（汇总 5 份报告）
    │
[update-claude-md] 写回 CLAUDE.md IIDP-SDD 标记（Phase 完成）
    │
[phase-end-gate]  ★ 门控：next-phase / done(skip)
```

## on_reject 行为速查

| 值 | 含义 |
|---|---|
| `abort` | 用户拒绝 → workflow 立即终止，报错退出 |
| `skip` | 用户拒绝 → 跳过本步骤，继续后续步骤 |
| `retry` | 用户拒绝 → 保持 PAUSED 状态，等待 `workflow resume` |

## 状态持久化与续接

workflow 引擎在每步完成后将状态写入：

```
.iidp/workflows/runs/<run_id>/
├── state.json    # 当前步骤索引 + 各步输出
├── inputs.json   # 用户输入快照
├── log.jsonl     # 时间戳事件流
└── workflow.yml  # 原始定义快照（防止源文件移动导致续接失败）
```

gate 步骤暂停（非 TTY 环境）后，用 `iidp workflow resume <run_id>` 续接。
