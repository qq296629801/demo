# Evaluation Rubric

## Score Summary

Total score: 100 points.

| Dimension | Points | What to Measure |
|---|---:|---|
| Requirements recovery quality | 20 | `code-index` documents modules, user stories, API, data model, and acceptance criteria from source |
| IIDP SDD completeness | 20 | `create-project` generates requirements, contracts, backend-spec, frontend-spec, tasks, and validation |
| Generated app runnability | 20 | Maven build, configuration files, packaging, and application startup |
| Docker environment consistency | 15 | Account, password, port, database, Redis, and MinIO values match across all required files |
| Smoke-test pass rate | 20 | User-story JSON-RPC smoke cases pass |
| Evidence and reviewability | 5 | Logs, diffs, score table, commit SHAs, and failure reasons are complete |

## Gate Rules

- If Docker environment consistency fails, the Docker dimension is 0 and the run cannot be reported as smoke-test passing.
- If the application cannot start, generated app runnability is at most 8 and smoke-test pass rate is 0.
- If no user stories or test cases are recovered from source, requirements recovery quality is at most 10 and smoke-test pass rate is at most 5.
- If an improvement modifies files outside `skills/create-project/`, the experiment is invalid even if the score increases.
- Only compare scores produced from the same benchmark commit SHA and the same local environment.

## Dimension Details

### Requirements Recovery Quality: 20

- 4 pts: module and feature inventory is present.
- 4 pts: user stories are concrete and traceable to source behavior.
- 4 pts: API documentation includes endpoint/service shape and request/response details.
- 4 pts: database structure includes tables, core fields, and relationships.
- 4 pts: acceptance criteria or test cases exist and can drive validation.

### IIDP SDD Completeness: 20

- 4 pts: `requirements.md` exists and preserves source-derived scope.
- 4 pts: `contracts.md` or integration map defines models, services, permissions, and view/menu keys.
- 4 pts: `backend-spec.md` contains IIDP naming, models, methods, views, seed data, and permissions.
- 4 pts: `frontend-spec.md` or an explicit standard-template no-code decision exists.
- 4 pts: `tasks.md` and `validation.md` connect requirements to implementation and tests.

### Generated App Runnability: 20

- 5 pts: generated files land in the expected IIDP app/module layout.
- 5 pts: Maven build or package command succeeds.
- 5 pts: app configuration is syntactically valid and resolves required properties.
- 5 pts: application starts and exposes the expected JSON-RPC endpoint.

### Docker Environment Consistency: 15

- 4 pts: MySQL host, port, database, username, and password match.
- 3 pts: Redis host, port, and password match.
- 3 pts: MinIO endpoint, access key, secret key, and bucket expectations match.
- 3 pts: app port and active profile match Docker startup.
- 2 pts: evidence shows the exact files and values compared, with secrets redacted in summaries.

### Smoke-Test Pass Rate: 20

Calculate from generated JSON-RPC cases:

```text
smoke_points = round(20 * passed_cases / total_cases)
```

If `total_cases = 0`, score 0. Cases must be traceable to recovered user stories or test cases.

### Evidence and Reviewability: 5

- 1 pt: benchmark URL and commit SHA are recorded.
- 1 pt: generated spec/app paths are recorded.
- 1 pt: Docker and smoke-test logs are summarized.
- 1 pt: changed `create-project` files and git commit SHAs are recorded.
- 1 pt: keep/revert decision has a concrete reason.

## Acceptance Decision

An experiment is accepted only when:

```text
new_benchmark_score > previous_benchmark_score
```

Equal scores are not accepted. Improvements observed only on sample-pool repositories are diagnostic, not sufficient for acceptance.
