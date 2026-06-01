---
name: evolve
description: Use when optimizing the IIDP create-project skill with benchmark-driven experiments, source-to-spec evaluation, Docker smoke tests, or sample-pool validation.
---

# evolve

## Overview

`evolve` is the self-evolution harness for `skills/create-project`. It uses a fixed benchmark, source-code-derived requirements, generated IIDP apps, Docker smoke tests, and a single 100-point score to decide whether a `create-project` change is worth keeping.

This skill is inspired by the autoresearch loop: keep the environment fixed, change only the editable target, measure one comparable metric, commit only improvements, and leave final review to humans.

## Hard Boundaries

- The fixed benchmark repository is `https://github.com/YunaiV/ruoyi-vue-pro.git`.
- Record the benchmark commit SHA on first use. Reuse that same SHA for every later comparison unless the user explicitly asks to refresh the benchmark.
- During automatic improvement, only modify files under `skills/create-project/`.
- Do not modify `skills/code-index/`, `skills/evolve/`, sample repositories, generated IIDP apps, Docker infrastructure, or test artifacts to force a higher score.
- One improvement round may make only one small, reviewable change. If it needs a second concern, run a second round.
- Sample-pool results may guide the next `create-project` edit, but only the fixed benchmark score decides whether an edit is kept.
- A score increase keeps the branch for human review. A non-increase must be reverted.
- Never auto-merge an evolve branch.

## Required Inputs and Outputs

For every evaluated source repository:

1. Use `skills/code-index` to generate requirements from source code.
2. Require at least SRS, user stories, API documentation, database structure, and test cases or acceptance criteria.
3. Use `skills/create-project` to generate IIDP SDD artifacts and an IIDP app from those requirements.
4. Generate smoke tests from the user stories and test cases.
5. Run Docker and JSON-RPC validation according to `skills/create-project/references/sdd-validation.md` and this skill's smoke validation reference.
6. Produce a score report with logs, commit SHAs, configuration evidence, and failure reasons.

## Workflow

### Phase 0: Load References

Read only the references needed for the current action:

| Need | Read |
|---|---|
| Experiment loop, branching, keep/revert rules | `references/autoresearch-loop.md` |
| Score calculation and acceptance gates | `references/evaluation-rubric.md` |
| Manual or websearch test samples | `references/sample-pool.md` |
| Docker account/password checks and JSON-RPC smoke tests | `references/smoke-validation.md` |

Also read `skills/code-index/SKILL.md`, `skills/create-project/SKILL.md`, and `skills/create-project/references/sdd-validation.md` before running a real evaluation.

### Phase 1: Establish Baseline

1. Clone or reuse the fixed benchmark repository.
2. Checkout the recorded benchmark SHA, or record the current SHA if this is the first run.
3. Run source-to-spec with `code-index`.
4. Run spec-to-IIDP generation with `create-project`.
5. Run Docker configuration consistency checks.
6. Run functional smoke tests generated from user stories and test cases.
7. Score the benchmark and save baseline evidence.

The baseline score is the only acceptance threshold for later `create-project` edits.

### Phase 2: Explore Sample Pool

Use sample repositories to find `create-project` weaknesses:

- User-provided samples are preferred.
- If requested, use websearch to discover up to 50 backend management framework repositories.
- Filter out unavailable, non-source, unrelated, or license-unclear repositories.
- For each accepted sample, record URL, commit SHA, framework notes, generated spec quality, IIDP generation failures, smoke-test failures, and the suspected missing `create-project` guidance.

Do not keep or reject a `create-project` edit based on sample-pool score alone.

### Phase 3: Improve `create-project`

1. Create or switch to an isolated evolve branch.
2. Pick one failure pattern from the baseline or sample pool.
3. Edit one small area under `skills/create-project/`.
4. Commit the edit before re-evaluation so it can be cleanly reverted.
5. Re-run the fixed benchmark using the same benchmark SHA and environment.
6. Compare the new benchmark score against the previous benchmark score:
   - `new_score > previous_score`: keep the commit, update the evidence, and leave the branch for human review.
   - `new_score <= previous_score`: revert the commit and record why it failed.

### Phase 4: Human Review Gate

When at least one commit improves the benchmark:

- Keep the evolve branch.
- Summarize score delta, changed files, benchmark SHA, test logs, and sample-pool evidence.
- Ask for human review or create a PR if the user requested GitHub publishing.
- Do not merge automatically.

When no commit improves the benchmark:

- Revert all failed experiment commits.
- Return to the original branch.
- Report the best observed failure patterns and why no change was kept.

## Evidence Files

Use local, reviewable evidence files when running the loop:

- `evolve-results.tsv`: one row per baseline/sample/round.
- `evolve-evidence.md`: human-readable run log with score tables, diffs, smoke-test summaries, Docker configuration findings, and keep/revert decisions.

Do not commit these evidence files unless the user explicitly asks.

## Completion Checklist

- Benchmark repository URL and commit SHA are recorded.
- `code-index` requirements include SRS, user stories, API, database, and tests or acceptance criteria.
- `create-project` generated required SDD artifacts and IIDP app files.
- Docker credentials, database names, Redis/MinIO settings, and app port are consistent across compose, Docker config, and generated app config.
- Smoke tests are derived from documented user stories and test cases.
- Score uses `references/evaluation-rubric.md`.
- Any kept change modifies only `skills/create-project/`.
- Failed changes are reverted and explained.
- Improved changes remain on a branch for human review, not auto-merged.
