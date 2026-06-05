# IIDP 前端 Skill 维护说明

`skills/frontend` 是 IIDP 前端开发提示词集合，采用“入口路由 + 专项 skill + 共享 reference”的组织方式。维护时优先保证规则只有一个权威来源，其他文档只引用，不复制整段规则。

## 目录职责

| 路径 | 职责 |
|---|---|
| `SKILL.md` | 前端总入口，只负责意图识别、路由和强制协议加载 |
| `commands/iidp-frontend-test.md` | 手动触发的前端代码验证命令，负责静态检查、启动、Playwright、评分和 Skill 反馈 |
| `references/iidp-frontend-codegen-protocol.md` | 前端代码生成唯一强制协议，维护门禁、实现分支和合规扫描 |
| `references/iidp-frontend-project-locator.md` | 前端工程发现、扩展应用定位、现有模式读取流程 |
| `references/iidp-frontend-spec-doc/SKILL.md` | 业务需求到 IIDP 前端规格文档 |
| `references/iidp-frontend-spec-code/SKILL.md` | 规格文档到工程代码的编排层 |
| `references/iidp-frontend-init/SKILL.md` | 工程初始化、扩展应用创建、依赖和本地启动 |
| `references/iidp-frontend-extension-dev/SKILL.md` | 存量工程扩展开发规则，包含扩展视图（普通节点扩展和扩展钩子）、组件边界 |
| `references/iidp-frontend-standard-ids/SKILL.md` | 标准模板节点 ID 规则库入口 |
| `references/iidp-frontend-dev-manual/SKILL.md` | IIDP 前端开发手册索引和事实来源 |

## 规则归属

| 规则主题 | 唯一维护位置 | 其他文档写法 |
|---|---|---|
| 是否能写前端代码、实现前门禁、实现后合规扫描 | `iidp-frontend-codegen-protocol.md` | 只写“执行 codegen protocol” |
| 工程发现、app 定位、读取现有模式 | `iidp-frontend-project-locator.md` | 只链接定位流程 |
| 工程创建、扩展应用创建、按需加载细节 | `iidp-frontend-init` | 只说明何时调用 init |
| 扩展视图（普通节点扩展和扩展钩子）、selector、`vm.biz`、`vm.super` | `iidp-frontend-extension-dev/SKILL.md` | 只链接对应章节 |
| 组件最小协议和易错点 | `COMPONENT_RULES_COVERAGE.md` + `COMPONENT_RULES.md` | 只要求先查组件规则 |
| 标准模板节点 ID | `iidp-frontend-standard-ids` | 只链接规则库 |
| 框架机制和原始文档索引 | `iidp-frontend-dev-manual` | 只作为事实来源引用 |

## 维护原则

- 不在入口 skill 中维护代码生成细则。
- 不在 `spec-code` 中复制扩展视图（普通节点扩展和扩展钩子）或组件规则。
- 不在 `extension-dev` 中复制工程创建和按需加载命令细节。
- 新增禁止项、门禁项或合规扫描项时，优先修改 `iidp-frontend-codegen-protocol.md`。
- 新增项目发现规则时，只修改 `iidp-frontend-project-locator.md`。
- 如果同一条规则需要同时改 2 个以上文件，先检查是否应该抽到共享 reference。

## 快速检查关键词

修改前端 skill 后，建议搜索以下关键词，确认没有出现新的重复规则或冲突描述：

```text
前置门禁
实现后合规扫描
type: 'page'
vm.super
effectPaths
COMPONENT_RULES
存量项目定位流程
```

## 快速检查命令

在仓库根目录执行：

```powershell
# 查看 frontend skill 主要文档体量
Get-ChildItem -Path skills\frontend -Recurse -Filter *.md |
  Where-Object { $_.FullName -match 'README|SKILL.md|protocol|locator|reference|template|COMPONENT_RULES|STANDARD_TEMPLATE_IDS' } |
  Select-Object FullName,@{Name='Lines';Expression={(Get-Content -Encoding UTF8 $_.FullName | Measure-Object -Line).Lines}} |
  Sort-Object Lines -Descending

# 查工程定位流程是否又被复制到其他 skill
rg -n --encoding utf-8 "工程发现|应用定位|存量项目定位流程|package.json.*init:tech" skills/frontend

# 查代码生成门禁和合规扫描是否仍集中在 protocol
rg -n --encoding utf-8 "实现前门禁|实现后合规扫描|禁止目录|待确认项" skills/frontend

# 查旧的目录冲突描述
rg -n --encoding utf-8 "resource.*static-resource.*views|static-resource.*views" skills/frontend

# 查 hook 术语是否需要补充说明
rg -n --encoding utf-8 "hook|扩展钩子|hook path" skills/frontend/README.md skills/frontend/references
```
