---
name: iidp-frontend-standard-ids
description: IIDP 标准模板节点 ID 规则库。帮助查阅标准模板节点 ID 命名规则，为扩展开发提供准确的节点 ID 参考。
---

# IIDP 页面节点 ID 规则库

## 使用场景

当用户需要：
- 查阅 IIDP 标准模板的节点 ID 命名规则
- 为扩展开发查找标准模板页面的节点 ID

## 定位与依赖

- 定位：标准模板节点 ID 规则库，为扩展开发提供标准模板页面的节点 ID 参考。
- 上游：无（AI 主动查阅规则库）。
- 下游：[iidp-frontend-extension-dev](../iidp-frontend-extension-dev/SKILL.md)、[iidp-frontend-spec-code](../iidp-frontend-spec-code/SKILL.md) 会读取规则库获取节点 ID。

## ID 查找优先级

当需要获取节点 ID 时，按以下优先级查找：

```
1. 用户已提供节点 ID（最高优先级）
   → 用户在需求中明确指定了目标节点 ID，直接使用
   ↓ 未提供
2. 标准模板 ID 规则库（本 skill 的 STANDARD_TEMPLATE_IDS.md）
   → 根据页面类型（gridPage/detailPage/treePage）和业务描述推导
   → 仅适用于标准模板页面
   ↓ 无法推导（非标准模板页面，或动态生成的业务按钮等）
3. 询问用户（兜底）
   → 指导用户在浏览器控制台获取节点 ID
```

## 标准模板 ID 规则库

详见 [STANDARD_TEMPLATE_IDS.md](STANDARD_TEMPLATE_IDS.md)。

规则库从 IIDP 前端源码中提取，包含：

- **ID 前缀规则**：`fullId = idPre + baseId`，idPre 通常为菜单路径
- **表格页（gridPage）节点结构**：搜索区、工具栏、表格、分页等
- **表单页（detailPage）节点结构**：头部、表单主体等
- **树表页节点结构**：左侧树、右侧表格等
- **子表节点结构**：tabs 组织、子表表格、子表表单等
- **抽屉/弹窗表单节点结构**
- **固定页面节点 ID**：登录页、头部、侧边栏、内容区等（无 idPre 前缀）
- **业务术语 → 节点 ID 映射表**：快速查找常用节点 ID

### 规则库使用方式

1. 确认目标页面的类型（gridPage/detailPage/treePage）
2. 获取页面的 idPre（可通过 `tech_app.page.data.id` 推算）
3. 根据业务描述在映射表中找到对应的 baseId
4. 拼接 `idPre + baseId` 得到完整节点 ID
5. 建议在浏览器控制台用 `tech_app.page.getNode('推导的ID')` 验证

## 与扩展开发配合

- [iidp-frontend-extension-dev](../iidp-frontend-extension-dev/SKILL.md) 会先查规则库，无匹配时询问用户提供节点 ID
- [iidp-frontend-spec-code](../iidp-frontend-spec-code/SKILL.md) 在生成代码前会检查规则库
