# Autoresearch Loop Mapping

## Principle

`evolve` adapts the autoresearch pattern to skill maintenance:

| Autoresearch idea | evolve equivalent |
|---|---|
| Fixed training setup | Fixed benchmark repository and commit SHA |
| One editable training file | Only `skills/create-project/` is editable during improvement |
| One validation metric | One 100-point benchmark score |
| Git as memory | Commit each experiment before measurement |
| Keep wins, discard losses | Keep score increases, revert non-increases |
| Human-written program.md | Human-reviewed skill guidance and final PR |

The loop must optimize measured `create-project` behavior, not the evaluation harness.

## Branch and Commit Rules

- Create an isolated branch named `evolve/create-project-YYYYMMDD-HHMM` unless the user provides another name.
- Capture the original branch before starting.
- Commit each attempted edit before running the benchmark.
- Commit message format: `evolve: improve create-project <short reason>`.
- If benchmark score does not increase, run a normal revert for the experiment commit and record the reason.
- If score increases, keep the commit and use it as the new previous score for the next round.

## Round Shape

Each round follows this order:

1. Select one failure pattern.
2. State the hypothesis in one sentence.
3. Modify one small section under `skills/create-project/`.
4. Commit the change.
5. Re-run the fixed benchmark from the same benchmark commit SHA.
6. Score with `evaluation-rubric.md`.
7. Keep or revert.
8. Append evidence.

Do not bundle unrelated fixes. A bigger instruction may be split into multiple rounds only after each round has passed the fixed benchmark.

## Evidence Template

```markdown
## Round <n>: <hypothesis>

- Benchmark repo: https://github.com/YunaiV/ruoyi-vue-pro.git
- Benchmark SHA: <sha>
- Experiment commit: <sha>
- Editable scope checked: only skills/create-project/

### Failure Pattern

<baseline or sample-pool evidence>

### Change

<file and concise before/after summary>

### Score

| Metric | Previous | New | Delta |
|---|---:|---:|---:|
| Total | 0 | 0 | 0 |

### Decision

KEEP or REVERT, with reason.
```

## Stop Conditions

Stop the loop when:

- The user-specified maximum round count is reached.
- Two consecutive kept rounds improve by less than 2 points each.
- Three consecutive attempted edits are reverted.
- Docker or external infrastructure is unavailable and prevents comparable scoring.
- The next useful edit would require changing files outside `skills/create-project/`.
