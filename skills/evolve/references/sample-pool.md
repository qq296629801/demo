# 样本池

## 目的

样本池用于在多种后端管理框架中暴露 `create-project` 指导缺口。它不能替代固定基准。样本结果用于指导假设，固定基准决定是否保留改动。

## 来源

样本可以来自：

- 用户提供的仓库 URL。
- 已存在的本地仓库。
- 通过 websearch 发现的后端管理框架仓库。

当用户要求 websearch 时，每轮最多搜索 50 个候选。优先选择活跃、有源码、且能代表后台/后端管理系统的仓库。

## 候选搜索语句

可使用类似查询：

- `GitHub Spring Boot Vue admin management framework`
- `GitHub Java backend management system Spring Boot`
- `GitHub admin framework MyBatis Plus Vue`
- `GitHub open source backend management platform`
- `GitHub SaaS admin backend framework Java`

只有当用户要求扩展到 Java/Spring 风格之外的技术栈时，才调整搜索词。

## 过滤规则

只有候选满足以下条件时才接收：

- 仓库可访问。
- 许可证可见，或用户明确允许评测。
- 包含后端源码，而不是只有截图或生成文档。
- 看起来确实是后端/后台/管理框架或应用。
- 可以记录 commit SHA。

出现以下情况时拒绝并记录原因：

- 无法克隆或检查。
- 没有清晰源码树。
- 与后端管理系统无关。
- 是缺少有效历史的镜像。
- 缺少清晰许可证，且用户未授权使用。

## 样本记录

在 `evolve-evidence.md` 中使用以下记录格式：

```markdown
### 样本：<name>

- URL: <repo-url>
- Commit SHA: <sha>
- License: <license 或 unknown>
- Stack: <框架/语言说明>
- Accepted: yes/no
- Filter reason: <接收或拒绝原因>
- code-index result: <规格质量摘要>
- create-project result: <生成摘要>
- Docker/smoke result: <通过/失败摘要>
- Suspected create-project gap: <一句话说明>
```

## 使用样本结果

评测样本后：

1. 按根因对失败分组。
2. 优先处理影响多个样本或固定基准的问题。
3. 为 `skills/create-project/` 形成一个小的改进假设。
4. 保留改动前必须运行固定基准。

不得编辑样本仓库或生成的 IIDP 应用来让样本分数通过。
