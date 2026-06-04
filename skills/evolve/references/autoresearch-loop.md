# Autoresearch 循环映射

## 原则

`evolve` 将 autoresearch 模式适配到 skill 维护：

| Autoresearch 思路 | evolve 对应机制 |
|---|---|
| 固定训练环境 | 固定基准需求文档和文件 hash（存于 `.evolve/baseline-spec/manifest.json`） |
| 一个可编辑训练文件 | 改进阶段只允许编辑 `skills/create-project/`、`skills/backend/`、`skills/frontend/` |
| 一个验证指标 | 一个 100 分基准评分 |
| Git 作为记忆 | 每次实验先 commit 再测量 |
| 保留胜利、丢弃失败 | 分数提升则保留，未提升则回滚 |
| 人类编写 program.md | 人工审核 skill 指导和最终 PR |

循环必须优化可测量的 IIDP skills 行为，而不是优化评测框架本身。

## 分支与提交规则

- 除非用户提供其他名称，否则创建隔离分支 `evolve/skills-YYYYMMDD-HHMM`。
- 开始前记录原始分支。
- 每个尝试性改动都要在运行基准前提交。
- 提交信息格式：`evolve: improve skills <short reason>`。
- 如果基准分没有提升，对实验 commit 执行正常 revert，并记录原因。
- 如果分数提升，保留 commit，把它作为下一轮的上一轮分数，并立即执行 Phase 4 技能反馈回写。

## 单轮形态

每一轮按以下顺序执行：

1. 选择一个失败模式。
2. 用一句话说明改进假设。
3. 将失败分类为 `route-gap`、`backend-doc-gap`、`frontend-doc-gap`、`sdd-template-gap` 或 `knowledge-gap`。
4. 修改允许范围内的一小段内容：`skills/create-project/`、`skills/backend/` 或 `skills/frontend/`。如果是 `knowledge-gap`，只记录缺口和所需私有事实源，不修改 backend/frontend 规则。
5. 提交改动。
6. 使用同一份基准需求文档（相同 hash）重新运行固定基准。
7. 用 `evaluation-rubric.md` 打分。
8. 保留或回滚。
9. 若 KEEP，执行 Phase 4 技能反馈回写；若发现缺少证据来源、目标文件、插入位置或验证方式，只记录为 `knowledge-gap`。
10. 追加证据。

不要打包无关修复。更大的指导改动必须拆成多轮，每一轮都通过固定基准后才能继续。

## 证据模板

```markdown
## 第 <n> 轮：<hypothesis>

- 基准文档：<文档标题>
- 文档 Hash：<sha256>
- 文档路径：.evolve/baseline-spec/
- 运行目录：.evolve/runs/<timestamp>/
- 实验 commit：<sha>
- 可编辑范围已检查：only skills/create-project/, skills/backend/, skills/frontend/

### 失败模式

<基准或样本池证据>

### 失败分类

`route-gap` / `backend-doc-gap` / `frontend-doc-gap` / `sdd-template-gap` / `knowledge-gap`

### 私有规则证据

- 证据来源：<本地文档路径、源码路径、日志片段、配置项、测试结果或用户确认>
- 规则结论：<可写回的规则，或 knowledge-gap 缺口说明>
- 验证方式：<静态检查、构建、Docker 或 JSON-RPC 冒烟测试>

### 改动

<文件和简洁 before/after 摘要>

### 评分

| 指标 | Previous | New | Delta |
|---|---:|---:|---:|
| Total | 0 | 0 | 0 |

### 决策

KEEP 或 REVERT，并说明原因。

### Phase 4 反馈回写

- 发现：<标题，或本轮未发现可写回文档缺口>
- 目标文件：<skills/... 或 knowledge-gap>
- 插入位置：<章节/标题>
- 验证方式：<静态检查、构建、Docker 或 JSON-RPC 冒烟测试>
```

## 停止条件

出现以下情况时停止循环：

- 达到用户指定的最大轮数。
- 连续两轮保留改动的提升都小于 2 分。
- 连续三个尝试性改动都被回滚。
- Docker 或外部基础设施不可用，导致无法进行可比较评分。
- 下一个有用改动需要修改允许范围之外的文件。
- 失败被分类为 `knowledge-gap`，且缺少继续判断所需的 IIDP 私有文档、源码、日志、配置、测试证据或用户确认。
