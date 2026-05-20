# Contributing

Maintainer workflow for this repo. End users installing the plugin
should follow [README.md](README.md); this file is for people editing
the source.

### Source of truth

- `skills/` is the single place to edit skill content. Claude Code,
  Copilot CLI, Gemini CLI, and VS Code all read from here directly.
- `.mcp.json` is the single place to define the MCP server connection.
- Per-client plugin manifests live at the path each client expects:
  `.claude-plugin/` (Claude Code), `.github/plugin/` (Copilot CLI),
  `gemini-extension.json` (Gemini CLI) at the repo root, and
  `plugins/webforj/.codex-plugin/` (Codex, subdirectory required).
  Each is authored directly; they do not duplicate each other.

### Why `plugins/webforj/` exists

Codex CLI requires each plugin to be a self-contained subdirectory
under a marketplace root. Other clients (Claude Code, Copilot CLI,
Gemini CLI) read their manifests from the repo root directly.

`plugins/webforj/` holds the Codex plugin bundle:

- `plugins/webforj/.codex-plugin/plugin.json` is the source of truth
  for the Codex plugin manifest. Edit it directly when you need to
  change Codex-specific fields.
- `plugins/webforj/.mcp.json` and `plugins/webforj/skills/` are
  **rebuild artifacts** produced by `scripts/sync.mjs`. Never edit
  these — edit the root versions and run sync.

## Common Tasks

### Edit an existing skill

1. Change files under `skills/<skill-name>/`.
2. Run:

   ```bash
   node scripts/sync.mjs
   ```

3. Commit both the root `skills/` change and the mirrored
   `plugins/webforj/skills/` change.

### Add a new skill

1. Create `skills/<new-skill-name>/SKILL.md` with frontmatter
   (`name`, `description`). Add references and scripts as needed.
2. Run:

   ```bash
   node scripts/sync.mjs
   ```

Claude Code, Copilot CLI, and Gemini CLI auto-discover the new skill
from `skills/`. Codex picks it up from the sync'd copy. No manifest
edits needed.

### Evaluate a skill

Run the `/eval-skill` slash command from the repo root in Claude Code, passing the skill path:

```
/eval-skill webforj-ai/skills/webforj-styling-apps
```

The command spawns one `with_skill` subagent and one `without_skill` baseline subagent per eval in parallel, grades both against the assertions in `evals/evals.json`, runs the official skill-creator `aggregate_benchmark.py`, and opens the review viewer on `http://localhost:3117/`.

**MCP server prerequisite.** The webforj-mcp server must be reachable BEFORE you launch an eval. The eval framework gives BOTH the `with_skill` and `without_skill` subagents access to whatever MCP servers are configured in `.mcp.json` at the repo root. This is the fair comparison: skills are evaluated for the workflow they teach, not for the existence of an MCP server the user already has installed.

Concretely, before running `/eval-skill`:

1. Confirm `.mcp.json` at the repo root has the `webforj-mcp` entry.
2. Read the skill's `SKILL.md` and check it does not require additional MCP servers not in `.mcp.json`. If it does, add them.
3. Do not strip MCP from the baseline. The orchestrator must pass the same MCP environment to both subagents.

**Why both configs see MCP.** A skill's job is to teach a workflow, not to look up facts the MCP server can provide in one query. If a skill's `with_skill` vs `without_skill` delta is small because the MCP-aware baseline can answer the question on its own, that is a real finding about the skill, not a measurement artifact. Stripping MCP from the baseline would inflate the delta and conflate "skill value" with "MCP value".

### Update the MCP server URL

1. Edit `.mcp.json` at the repo root.
2. Run `node scripts/sync.mjs`.
3. Also update `server.json` (`remotes[0].url`) and README snippets if
   the URL appears elsewhere.

### Release a new version

1. Update `CHANGELOG.md` with what's changing under a new `## [X.Y.Z]`
   heading.
2. Bump versions across every manifest:

   ```bash
   node scripts/bump.mjs 0.2.0
   ```

   This updates the `version` field in every manifest that carries one
   (Claude, Copilot, Gemini, Codex, server.json, marketplace.json) and
   auto-runs `sync.mjs` to mirror root `.mcp.json` and `skills/` into
   `plugins/webforj/`.

3. Commit and tag:

   ```bash
   git add -A
   git commit -m "chore(release): 0.2.0"
   git tag v0.2.0
   git push && git push --tags
   ```

### Add support for a new AI client

Most clients follow the Agent Skills open standard, so `skills/` alone
may be enough (they read skills directly from a repo URL or clone).

If the client needs its own manifest:

1. Add the manifest at the path that client expects (research their
   docs).
2. If the manifest has a `version` field, add its path to the
   `targets` array in `scripts/bump.mjs` so future bumps update it.
3. If the client requires a subdirectory layout like Codex, add a new
   mirror entry in `scripts/sync.mjs` and the corresponding
   `plugins/<name>/` subdirectory.
4. Add an "Install" and "Uninstall" section in `README.md`.
