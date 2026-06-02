---
description: 【可选工具】初始化项目级模板目录 specs/templates/，将选定的技能默认模板复制到项目中供用户定制。
handoffs:
  - label: 开始规格编写
    command: sdd-specify
    prompt: 使用已定制的模板编写功能需求规格
    send: false
---

# /sdd-init-templates

## 用途

将 `skills/create-project/references/sdd-constitution.md` 中的默认模板提取到 `specs/templates/`，用户可直接编辑这些文件定制模板结构，后续所有规格生成命令将自动优先使用项目级模板。

**不运行此命令也可以**：手动在 `specs/templates/` 创建与默认模板同名的 `.md` 文件即可生效。

## 用户输入

```text
$ARGUMENTS
```

可传入模板名称（如 `requirements contracts`）直接初始化；为空时进入交互选择。

## 执行步骤

1. **展示可选模板列表**（用户未指定时）：

   ```text
   以下是可定制的项目级模板，请选择要初始化哪些（输入字母，多选用空格分隔，或输入 all）：

   A) requirements.md    — 功能规格（US 格式、FR 编号、成功标准章节结构）
   B) contracts.md       — API 契约（字段表格列、服务签名、app.json 格式）
   C) mission.md         — 项目使命（定位、目标用户、核心价值）
   D) iidp-stack.md      — 技术栈约束（工程路径、命名规则、Git 工作流）
   E) ui-constitution.md — UI 宪法（设计来源、组件规则、可访问性）
   F) roadmap.md         — 路线图（Phase 结构、验收标准、技术债表格）
   G) integration-map.md — 集成地图（模型清单、ER、权限码总览表格）
   H) all                — 全部初始化
   ```

2. **创建目录**：确保 `specs/templates/` 存在。

3. **提取并写入**（对每个选中的模板）：
   - 从 `sdd-constitution.md` 对应的 `## {filename}.md 模板` 节提取模板内容
   - 写入 `specs/templates/{filename}.md`
   - 在文件顶部追加说明注释：
     ```markdown
     <!-- 项目级模板：覆盖 sdd-constitution.md 默认模板。编辑此文件即可定制规格结构。
          三条约束（IIDP 规范优先、契约先行、不编造平台事实）不受模板影响，始终适用。 -->
     ```

4. **生成 `specs/templates/README.md`**：列出已初始化的模板、覆盖说明、注意事项。

   ```markdown
   # 项目级模板

   本目录中的文件覆盖 sdd-constitution.md 的对应默认模板。

   ## 已初始化的模板

   | 文件 | 覆盖的默认节 | 初始化时间 |
   |---|---|---|
   | requirements.md | sdd-constitution.md § requirements.md 模板 | [日期] |
   | ... | ... | ... |

   ## 使用规则

   - AI 生成规格文件时自动优先读取本目录中的模板，无需额外配置。
   - 删除某个文件即恢复使用 sdd-constitution.md 默认模板。
   - `[...]` 格式的占位符会被业务内容填充；未填充项自动标记为 `待确认`。

   ## 不可修改的内容

   以下约束不受模板控制，始终强制适用：
   - 三条约束（IIDP 规范优先 / 契约先行 / 不编造平台事实）
   - `specs/features/<phaseN>-<feature>/` 目录结构
   - `skills/backend/` 和 `skills/frontend/` 中的 IIDP 平台技术规范
   ```

5. **输出初始化摘要**：列出已写入的文件路径。

## 完成标志

- `specs/templates/` 目录已创建。
- 选中的模板文件已写入，顶部包含说明注释。
- `specs/templates/README.md` 已生成。
- 提示用户：编辑 `specs/templates/` 中的文件后，运行 `/sdd-specify` 或 `/sdd-contracts` 即会自动使用定制模板。
