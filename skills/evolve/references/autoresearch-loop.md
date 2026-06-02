# Autoresearch 循环映射

## 原则

`evolve` 将 autoresearch 模式适配到 skill 维护：

| Autoresearch 思路 | evolve 对应机制 |
|---|---|
| 固定训练环境 | 固定基准仓库和 commit SHA |
| 一个可编辑训练文件 | 改进阶段只允许编辑 `skills/create-project/` |
| 一个验证指标 | 一个 100 分基准评分 |
| Git 作为记忆 | 每次实验先 commit 再测量 |
| 保留胜利、丢弃失败 | 分数提升则保留，未提升则回滚 |
| 人类编写 program.md | 人工审核 skill 指导和最终 PR |

循环必须优化可测量的 `create-project` 行为，而不是优化评测框架本身。

## 分支与提交规则

- 除非用户提供其他名称，否则创建隔离分支 `evolve/create-project-YYYYMMDD-HHMM`。
- 开始前记录原始分支。
- 每个尝试性改动都要在运行基准前提交。
- 提交信息格式：`evolve: improve create-project <short reason>`。
- 如果基准分没有提升，对实验 commit 执行正常 revert，并记录原因。
- 如果分数提升，保留 commit，并把它作为下一轮的上一轮分数。

## 单轮形态

每一轮按以下顺序执行：

1. 选择一个失败模式。
2. 用一句话说明改进假设。
3. 修改 `skills/create-project/` 下的一小段内容。
4. 提交改动。
5. 使用同一个基准 commit SHA 重新运行固定基准。
6. 用 `evaluation-rubric.md` 打分。
7. 保留或回滚。
8. 追加证据。

不要打包无关修复。更大的指导改动必须拆成多轮，每一轮都通过固定基准后才能继续。

## 证据模板

```markdown
## 第 <n> 轮：<hypothesis>

- 基准仓库：https://github.com/YunaiV/ruoyi-vue-pro.git
- 基准 SHA：<sha>
- 实验 commit：<sha>
- 可编辑范围已检查：only skills/create-project/

### 失败模式

<基准或样本池证据>

### 改动

<文件和简洁 before/after 摘要>

### 评分

| 指标 | Previous | New | Delta |
|---|---:|---:|---:|
| Total | 0 | 0 | 0 |

### 决策

KEEP 或 REVERT，并说明原因。
```

## 停止条件

出现以下情况时停止循环：

- 达到用户指定的最大轮数。
- 连续两轮保留改动的提升都小于 2 分。
- 连续三个尝试性改动都被回滚。
- Docker 或外部基础设施不可用，导致无法进行可比较评分。
- 下一个有用改动需要修改 `skills/create-project/` 之外的文件。
