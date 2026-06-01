# Sample Pool

## Purpose

The sample pool exists to expose missing `create-project` guidance across varied backend management frameworks. It does not replace the fixed benchmark. Sample results guide hypotheses; the fixed benchmark decides whether a change is kept.

## Sources

Samples may come from:

- User-provided repository URLs.
- Existing local repositories.
- Websearch discovery for backend management framework repositories.

When websearch is requested, search for up to 50 candidates per run. Prefer repositories that are active, source-available, and representative of admin/backend management systems.

## Candidate Search Queries

Use queries like:

- `GitHub Spring Boot Vue admin management framework`
- `GitHub Java backend management system Spring Boot`
- `GitHub admin framework MyBatis Plus Vue`
- `GitHub open source backend management platform`
- `GitHub SaaS admin backend framework Java`

Adjust terms for other stacks only when the user asks to broaden beyond Java/Spring-style frameworks.

## Filtering

Accept a candidate only if:

- The repository is reachable.
- The license is visible or the user explicitly permits evaluation.
- It contains backend source code, not only screenshots or generated docs.
- It is plausibly a backend/admin/management framework or application.
- A commit SHA can be recorded.

Reject and record the reason if:

- It cannot be cloned or inspected.
- It has no clear source tree.
- It is unrelated to backend management systems.
- It is a mirror without useful history.
- It lacks a clear license and the user did not authorize use.

## Sample Record

Use this record shape in `evolve-evidence.md`:

```markdown
### Sample: <name>

- URL: <repo-url>
- Commit SHA: <sha>
- License: <license or unknown>
- Stack: <framework/language notes>
- Accepted: yes/no
- Filter reason: <why accepted or rejected>
- code-index result: <spec quality summary>
- create-project result: <generation summary>
- Docker/smoke result: <pass/fail summary>
- Suspected create-project gap: <one sentence>
```

## Using Sample Results

After evaluating samples:

1. Group failures by root cause.
2. Prefer fixes that affect multiple samples or the fixed benchmark.
3. Form one small hypothesis for `skills/create-project/`.
4. Run the fixed benchmark before keeping the edit.

Do not edit sample repositories or generated IIDP apps to make sample scores pass.
