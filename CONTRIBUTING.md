# Contributing

Maintainer workflow for this repo. End users installing the plugin
should follow [README.md](README.md); this file is for people editing
the source.

### Source of truth

- `skills/` is the single place to edit skill content. Claude Code,
  Copilot CLI, Gemini CLI, and VS Code all read from here directly.
- `.mcp.json` is the single place to define the MCP server connection.
- `plugin.json` and `mcp.json` at the repo root are the portable
  [Agent Plugins 1.0.0](https://agent-plugins.org/) manifests. Clients
  that implement the standard read these and need nothing else.
- Per-client manifests live at the path each client expects:
  `.claude-plugin/` (Claude Code), `.github/plugin/` (Copilot CLI),
  `gemini-extension.json` (Gemini CLI), and `.codex-plugin/` (Codex,
  legacy format). Each is authored directly; they do not duplicate
  each other.

### Repo root is the plugin root

The whole repository *is* the plugin. Agent Plugins 1.0.0 fixes where
the portable pieces live — `plugin.json` and `mcp.json` at the root,
skills in `skills/` — and every client reads that one copy. There is
no per-client bundle to keep in sync.

Two MCP files exist on purpose and are not duplicates of convenience:

- `mcp.json` is the portable manifest. It uses the standard's
  `streamable-http` transport name.
- `.mcp.json` is what Claude Code and legacy Codex auto-discover, and
  it doubles as the project-scoped MCP config for anyone working in
  this repo. It uses the `http` transport name.

Change the server URL in both, or neither.

## Common Tasks

### Edit an existing skill

1. Change files under `skills/<skill-name>/`.
2. Commit. Every client reads `skills/` directly — there is nothing
   to mirror or regenerate.

### Add a new skill

1. Create `skills/<new-skill-name>/SKILL.md` with frontmatter
   (`name`, `description`). Add references and scripts as needed.
2. Commit.

All clients auto-discover the new skill from `skills/`. No manifest
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

1. Edit **both** `mcp.json` and `.mcp.json` at the repo root. They
   describe the same server in two transport spellings; leaving them
   out of step means some clients silently keep the old URL.
2. Also update `server.json` (`remotes[0].url`), `gemini-extension.json`
   (`mcpServers.webforj-mcp.httpUrl`), and README snippets if the URL
   appears elsewhere.

### Release a new version

1. Update `CHANGELOG.md` with what's changing under a new `## [X.Y.Z]`
   heading.
2. Bump versions across every manifest:

   ```bash
   node scripts/bump.mjs 0.2.0
   ```

   This updates the `version` field in every manifest that carries one
   (Agent Plugins `plugin.json`, Claude, Copilot, Gemini, Codex,
   `server.json`, `marketplace.json`).

3. Commit and tag:

   ```bash
   git add -A
   git commit -m "chore(release): 0.2.0"
   git tag v0.2.0
   git push && git push --tags
   ```

### Add support for a new AI client

Check whether the client implements
[Agent Plugins 1.0.0](https://agent-plugins.org/) first — if it does,
the root `plugin.json` and `mcp.json` already cover it and no new file
is needed. Failing that, many clients follow the Agent Skills open
standard, so `skills/` alone may be enough.

If the client needs its own manifest:

1. Add the manifest at the path that client expects (research their
   docs).
2. If the manifest has a `version` field, add its path to the
   `targets` array in `scripts/bump.mjs` so future bumps update it.
3. Add an "Install" and "Uninstall" section in `README.md`.
