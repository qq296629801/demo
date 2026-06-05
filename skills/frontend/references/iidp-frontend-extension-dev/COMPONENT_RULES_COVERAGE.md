# IIDP Component Rules Coverage

本文记录 `COMPONENT_RULES.md` 已覆盖的组件范围，以及未覆盖组件的处理规则。生成或修改 IIDP 组件节点前，先看本文件，再进入具体组件规则。

## 已覆盖组件

以下组件可直接按 `COMPONENT_RULES.md` 生成：

| 组件 | 规则位置 | 备注 |
|---|---|---|
| `button` | `COMPONENT_RULES.md ## button` | 按钮文本用 `value` |
| `container` | `COMPONENT_RULES.md ## container` | 子节点放 `items` |
| `row` | `COMPONENT_RULES.md ## row` | 24 栅格布局 |
| `table` | `COMPONENT_RULES.md ## table` | 操作列事件用 `bind_on_clickOptionBtn` |
| `form` | `COMPONENT_RULES.md ## form` | 表单字段用 `name` |
| `input` | `COMPONENT_RULES.md ## input` | 表单内不使用 `modelValue` |
| `dialog` | `COMPONENT_RULES.md ## dialog` | 显示控制用 `display` |
| `drawer` | `COMPONENT_RULES.md ## drawer` | 显示控制用 `visible` |
| `lookup-table` | `COMPONENT_RULES.md ## lookup-table` | 必须明确 searchModel/matchColumns/searchKey |
| `custom-vue-component` | `COMPONENT_RULES.md ## custom-vue-component` | Vue 组件 name 必须以 `tech-` 开头 |
| `custom-view-component` | `COMPONENT_RULES.md ## custom-view-component` | `__block` 与去 `tech-` 后名称一致 |

## 未覆盖组件处理

未在上表出现的组件，必须按以下顺序处理：

1. 通过 `skills/frontend/references/iidp-frontend-dev-manual/SKILL.md` 查组件索引。
2. 读取对应原始文档，确认属性、事件和示例。
3. 文档仍找不到时，停止生成该组件，向用户确认或写入“待确认”。

## 禁止行为

- 禁止把 Element UI 属性直接当作 IIDP 组件属性。
- 禁止把 HTML 原生属性直接写进 IIDP 节点。
- 禁止臆造 `bind_on_` 事件名。
- 禁止因为示例中出现过某个字段，就推断所有组件都支持。

## 属性放置规则

- IIDP 文档明确支持的属性：直接写在节点上。
- IIDP 文档未声明但 Element UI 原生支持的属性：放入 `ATTRS`。
- Element UI 原生事件：放入 `ONS`。
- IIDP 平台事件：使用真实存在的 `bind_on_事件名`。
