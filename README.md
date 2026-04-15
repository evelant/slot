# SLOT

SLOT means `Significantly Less Organizational Tedium`.

SLOT is an experimental Minecraft inventory overhaul for modded survival play.
The goal is to reduce inventory-management friction so players can focus on the
game instead of on shuffling stacks between isolated grids.

SLOT is not trying to turn the player into remote storage, a logistics network,
or an autocrafting system. The design target is a better way to browse,
understand, and act on the inventories the player is already carrying or has
actually opened.

## Current Status

SLOT is unreleased and still changing aggressively.

Current baseline:

- the headless authority/projection/workflow kernel is landed
- the session coordinator, intent router, and routed crafting pipeline are
  landed
- the next milestone is the first real NeoForge host over that core; screen
  replacement configs remain off by default until that work lands

Current target:

- Minecraft `1.21.1`
- Java `21`
- NeoForge first
- optional integrations where available, especially EMI, Tom's Storage, and
  Sophisticated Backpacks

## Documentation Map

- [PRODUCT_DIRECTION.md](PRODUCT_DIRECTION.md): why SLOT exists, the player
  problems it is trying to solve, and the current feature direction.
- [ARCHITECTURE.md](ARCHITECTURE.md): the living core model for authority,
  projection, actions, crafting surfaces, and the workflow/activity runtime.
- [HOST_UI_ARCHITECTURE.md](HOST_UI_ARCHITECTURE.md): the planned host and UI
  architecture above the current headless kernel.
- [SPEC.md](SPEC.md): the normative behavior specification.
- [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md): the current near-term
  engineering plan.
- [INTEGRATION_LEARNINGS.md](INTEGRATION_LEARNINGS.md): integration gotchas and
  practical lessons from Minecraft, NeoForge, EMI, Tom's Storage, Sophisticated
  Backpacks, and similar mods.
- [AGENTS.md](AGENTS.md): contributor instructions for working in this repo.

### History / Reference

Content in the `archive` folder is old previous prototyping and should not be considered part of the current project. It is there for reference only as it may include some insights into integrating with other mods or other inventory quirks.

## What SLOT Is Trying To Do

- present carried inventory as one coherent browsing model without losing exact
  source identity for real actions
- make it easier to find needed items through search, categories, collections,
  recents, and workflow helpers
- support faster transfer, storage, crafting, and tool interactions while
  staying source-aware and server-authoritative
- keep workflow and activity semantics durable enough that recents, cleanup,
  loadouts, and future undo/recovery flows do not depend on one specific screen
- keep UI experiments decoupled from inventory authority so the product can
  iterate without rewriting the core each time

## What SLOT Is Not

SLOT is not:

- remote storage
- infinite inventory
- hidden logistics automation
- recursive autocrafting
- a backpack progression system
- a replacement for real container authority

## Design Principles

- One user action should have one authoritative pipeline.
- Screen code should render state and forward intents, not own inventory
  semantics.
- Exact authority and derived projections are different things and must stay
  separate.
- Inventory authority is the source of truth; workflow and activity history are
  supporting domain state, not replacement authority.
- Unsupported integration must fail closed.
- Reflection belongs behind narrow compat bridges.
- UI refresh must preserve valid interaction state unless the logical session
  changed.

## Development

Useful commands:

```bash
./gradlew compileJava compileTestJava
./gradlew test
./gradlew test build
```
