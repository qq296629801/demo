# Smoke Validation

## Relationship to create-project Validation

Start with `skills/create-project/references/sdd-validation.md`. This file adds evolve-specific scoring and configuration consistency gates for generated IIDP apps.

## Required Validation Flow

For each evaluated repository:

1. Derive JSON-RPC smoke cases from recovered user stories and test cases.
2. Compare Docker and generated app configuration before startup.
3. Start dependencies with `docker compose up -d mysql redis minio minio-init` when those services exist.
4. Build the generated app with the project-standard Maven command.
5. Start the IIDP app container or local app process.
6. Run `tests/functional/smoke_test.py` or an equivalent JSON-RPC runner.
7. Score with `evaluation-rubric.md`.

## Configuration Consistency Gate

Compare values across:

- `docker-compose.yml`
- `docker/config/application.properties`
- `docker/config/application-dev.properties`
- `docker/config/dbcp.properties`
- generated IIDP app configuration files, including `application*.properties`, `application*.yml`, `.env`, and module-specific config files.

Required matches:

| Area | Values |
|---|---|
| MySQL | host, port, database name, username, password |
| Redis | host, port, password, database index if used |
| MinIO | endpoint, access key, secret key, bucket if configured |
| App | port, active profile, context path or JSON-RPC endpoint |

If a value is intentionally different because of container-internal networking, record both values and the mapping. Example: host machine `localhost:3306` maps to container service `mysql:3306`.

## Failure Classification

Classify every failed case as one of:

- `spec-gap`: requirements or user story did not preserve source behavior.
- `generation-gap`: `create-project` failed to generate required IIDP artifacts or code.
- `config-gap`: Docker and generated app configuration are inconsistent.
- `startup-gap`: dependencies or app cannot start.
- `smoke-gap`: app starts but JSON-RPC behavior fails.
- `environment-gap`: local Docker, network, Maven, or external dependency prevents comparable evaluation.

Only `generation-gap`, repeatable `config-gap`, and `smoke-gap` should normally drive `create-project` edits. `environment-gap` should stop comparison instead of producing a misleading score.

## JSON-RPC Case Requirements

Each case must include:

- `storyId`
- `caseId` or case `name`
- JSON-RPC 2.0 request
- expected HTTP status
- expected result or expected error
- trace to a user story, test case, or acceptance criterion from the generated requirements

Minimum service coverage should include available CRUD-style behavior:

- `search` or `find`
- `create`
- `update`
- `delete`
- at least one negative case for required fields, permissions, invalid status, or missing record when present in the source requirements

## Smoke Score

Use the rubric formula:

```text
smoke_points = round(20 * passed_cases / total_cases)
```

Do not count skipped cases as passed. If the app cannot start, all smoke cases fail.

## Evidence Requirements

Record:

- exact config files compared
- redacted credential comparison result
- Docker service health summary
- app startup command and result
- number of JSON-RPC cases passed and failed
- representative failure body for each failure class
- final score contribution
