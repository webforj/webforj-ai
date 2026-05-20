---
description: Run a full evaluation and iteration loop on an Agent Skill using the official skill-creator plugin. Spawns with-skill and without-skill subagents per eval, grades against the skill's evals.json assertions, aggregates a benchmark, opens the review viewer, then iterates based on feedback. Use whenever I say "evaluate my skill", "run evals on", "benchmark this skill", "iterate on this skill", or name a skill folder I want tested.
argument-hint: <skill-path>
---

Use the skill-creator skill to run a full evaluation and iteration loop on the Agent Skill at:

$ARGUMENTS

Follow skill-creator's `SKILL.md` workflow exactly. Always include a baseline (`without_skill`) run for every eval. Continue iterating until I tell you to stop.

## Required directory layout (use from the START, do not restructure later)

```
<skill>-workspace/
  iteration-<N>/
    eval-<id>-<name>/
      <config>/                      # with_skill or without_skill
        run-1/
          workspace/                 # copy of base project; subagent CWD; subagent edits real files here
          outputs/                   # small files the viewer renders inline (SUMMARY.md, key snippets, tool_calls.json)
          eval_metadata.json         # eval_id, eval_name, prompt, assertions
          grading.json               # per references/schemas.md
          timing.json                # total_tokens + duration_ms + total_duration_seconds
```

Rules:

- `run-1/` IS mandatory. `aggregate_benchmark.py` require it. Without it the aggregator returns Delta +0.00.
- `eval_metadata.json` MUST be at `<config>/run-1/eval_metadata.json` (preferred) OR `<config>/eval_metadata.json` (fallback). `generate_review.py` only checks those two paths. If you put it at `eval-<id>-<name>/eval_metadata.json` the viewer renders "(No prompt found)".
- `workspace/` (the editable project copy) and `outputs/` (the viewer-rendered artifacts) are SEPARATE. Subagent CWD is `workspace/`. The subagent writes a small `outputs/SUMMARY.md` describing what it did, plus any artifacts you want the human to see inline. Do NOT put the project copy under `outputs/`.

## Setup steps before spawning subagents

For each eval:

1. Copy the base project (determined from `evals/files/<base>/pom.xml` in evals.json) to `<config>/run-1/workspace/` — both `with_skill` and `without_skill`.
2. Apply per-eval seed file overlays (the additional paths in `evals.json` `files: [...]` after the first entry) on top of `workspace/`.
3. Write `<config>/run-1/eval_metadata.json` with the eval's id, name, prompt, assertions.
4. Spawn the subagent with `cwd = <config>/run-1/workspace/` and tell it to save artifacts to `<config>/run-1/outputs/`.

Corrections from prior sessions, do not repeat these mistakes:

- The only documented input-files mechanism for an eval is `files: [...]` per `references/schemas.md`. Do not invent fields like `fixture` or directories like `fixtures/`.
- Use the provided `aggregate_benchmark.py` and `generate_review.py`. Do not write a custom benchmark builder or HTML viewer.
- ALWAYS invoke `aggregate_benchmark.py` with `python3` (the script uses Python 3 type hints; `python` may resolve to Python 2 and fail with SyntaxError on `list[float]`).
