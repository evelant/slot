# SLOT

SLOT means `Significantly Less Organizational Tedium`.

SLOT is an experimental Minecraft inventory overhaul for modded survival play.
The goal is to reduce inventory-management friction so players can focus on the
game instead of on shuffling stacks between isolated grids.

SLOT is not remote storage, infinite inventory, a logistics network, or
autocrafting. The design target is a better way to browse, understand, and act
on the inventories the player is already carrying or has actually opened.

Current target: Minecraft `1.21.1`, Java `21`, NeoForge-first, LDLib2 required
for the dedicated test instance. Optional integrations where available —
especially EMI, Tom's Storage, and Sophisticated Backpacks.

## Start Here

New session? Read in this order:

1. This file.
2. [docs/status.md](docs/status.md) — current code state, next work, and
   verification commands.
3. The topic docs below that match the task.

## Documentation Map

Operational:

- [docs/status.md](docs/status.md) — operational handoff: current baseline,
  next work, project structure, verification commands, working rules.

Product:

- [docs/product/direction.md](docs/product/direction.md) — why SLOT exists and
  the current feature direction.
- [docs/product/spec.md](docs/product/spec.md) — normative player-visible
  behavior.

Architecture:

- [docs/architecture/overview.md](docs/architecture/overview.md) — living core
  model: authority, projection, actions, crafting, workflow/activity runtime.
- [docs/architecture/action-taxonomy.md](docs/architecture/action-taxonomy.md)
  — verb + quantity + scope + conflict policy model.
- [docs/architecture/host-ui.md](docs/architecture/host-ui.md) — host/UI layer
  above the kernel; workspace composition and profile model.

Design:

- [docs/design/atlas.md](docs/design/atlas.md) — triage-first pan/zoom visual
  atlas: islands, homes, lenses, progressive disclosure.
- [docs/design/kits.md](docs/design/kits.md) — task-oriented Kits (unifies
  earlier "collections + loadouts").

Plans:

- [docs/plans/current.md](docs/plans/current.md) — current near-term
  engineering sequence.
- [docs/plans/atlas-prototype.md](docs/plans/atlas-prototype.md) — atlas
  prototype slices and blocking decisions.
- [docs/plans/kit-prototype.md](docs/plans/kit-prototype.md) — Kit prototype
  slices.

Decisions (ADR-style, historical):

- [docs/decisions/0001-core-rewrite.md](docs/decisions/0001-core-rewrite.md)
  — reasoning behind the core architecture rewrite.
- [docs/decisions/0002-ldlib2-workspace.md](docs/decisions/0002-ldlib2-workspace.md)
  — accepted LDLib2 workspace transport decision.

Research and assessments:

- [docs/research/ui-ux-brainstorm.md](docs/research/ui-ux-brainstorm.md) —
  exploratory UI/UX problem-space analysis.
- [docs/research/ui-library-assessment.md](docs/research/ui-library-assessment.md)
  — UI framework assessment and the LDLib2 boundary.
- [docs/research/core-inventory-library-assessment.md](docs/research/core-inventory-library-assessment.md)
  — assessment of external inventory primitives and reference mods.
- [docs/research/integration-learnings.md](docs/research/integration-learnings.md)
  — practical lessons from Minecraft, NeoForge, EMI, Tom's Storage,
  Sophisticated Backpacks, and similar mods.

Contributor rules: [AGENTS.md](AGENTS.md).

Content in the `archive/` folder is old prototyping kept only as reference; it
is not part of the current project.

## Design Principles

- One user action should have one authoritative pipeline.
- Screen code renders state and forwards intents; it does not own inventory
  semantics.
- Exact authority and derived projections are different layers and must stay
  separate.
- Inventory authority is the source of truth. Workflow and activity history
  are supporting domain state, not replacement authority.
- Unsupported integration must fail closed.
- Reflection belongs behind narrow compat bridges.
- UI refresh must preserve valid interaction state unless the logical session
  changed.

## Development

Fast compile checks:

```bash
./gradlew :common:testClasses
./gradlew :neoforge:testClasses
```

Focused test pass:

```bash
./gradlew :common:test --tests 'dev.imagio.slot.*'
./gradlew :neoforge:test
```
