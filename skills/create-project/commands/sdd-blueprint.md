---
description: 【SDD Step 3.5】生成代码蓝图（可选）。输出伪代码 + 文件清单，暂停等用户确认后再实现。
handoffs:
  - label: 开始实现 Implement
    command: sdd-implement
    prompt: 蓝图已确认，执行 tasks.md 中第一个未完成任务
    send: true
---

# /sdd-blueprint

## 用户输入

```text
$ARGUMENTS
```

可传入功能目录路径；为空时从 `CLAUDE.md` 读取活动功能目录。

## 触发条件

满足以下任一时建议触发（用户也可主动调用）：
- L 级复杂度（预计 > 3 天）
- 包含跨模型服务
- 团队第一次在该项目使用 IIDP 前端扩展

## 执行步骤

按 `skills/create-project/references/sdd-workflow.md § Step 3.5` 执行：

**不写真实代码，只描述结构和逻辑。**

1. **读取** `tasks.md`、`backend-spec.md`、`frontend-spec.md`。

2. **后端蓝图**：

   | 文件路径 | 类型 | 关键内容 |
   |---|---|---|
   | `src/.../XxxModel.java` | `@Model` | 字段：id / name / status / ... |
   | `views/xxx_view.json` | grid/search/form | 按钮：新增 / 编辑 / 删除 / [自定义] |
   | `data/menus.json` | 菜单 | 挂载：[父菜单 key] |

   每个 Java 文件只写路径、类名、关键字段和方法签名（伪代码，不写完整实现）。

3. **前端蓝图**（需要前端代码时）：

   | 文件路径 | 扩展类型 | 目标节点 | id 来源 |
   |---|---|---|---|
   | `apps/[app]/views/[biz].js` | merge | `[node_id]` | 待确认 |

   每个扩展文件只写路径、扩展类型（`before/after/merge/replace/custom`）和 selector；hook 只写伪代码逻辑。

4. **暂停**，等待用户确认蓝图方向正确。

## 完成标志

- 蓝图已输出（后端文件清单 + 前端文件清单）。
- **用户确认后**才进入 `/sdd-implement`；用户要求调整时修改 `backend-spec.md` 或 `frontend-spec.md` 再重新运行。
