# SLOT Product Direction

Last updated: 2026-04-14

This document explains why SLOT exists and what player problems it is trying to
solve. It is the living feature-direction document, not the normative behavior
spec and not the engineering execution plan.

For current core architecture, see [../architecture/overview.md](../architecture/overview.md).
For normative behavior, see [spec.md](spec.md).
For the current engineering sequence, see [../plans/current.md](../plans/current.md).

## Core Goal

SLOT should reduce inventory-management friction so the player can stay focused
on gameplay instead of on moving items between isolated container grids.

The design target is not one specific screen layout. The design target is a
core that lets us try different inventory UIs while keeping authority,
transfers, workflows, and integrations consistent.

## Player Problems SLOT Is Trying To Solve

### 1. Carried inventory is fragmented

Players routinely have useful items split across:

- player main inventory
- hotbar
- offhand
- backpacks or similar carried storage

The default Minecraft model makes the player remember where things live and
open containers one at a time. SLOT should let the player browse carried items
as one working set without erasing the real source boundaries needed for safe
actions.

### 2. Finding the right item is slower than it should be

Modded survival produces too many items for a plain grid to stay comfortable.

SLOT should make it easier to answer:

- what do I have on me right now?
- where is the item I need?
- what am I missing for the thing I am trying to do?
- what did I just pick up and need to triage?

### 3. Transfer and cleanup workflows are awkward

Common pain points:

- depositing items into storage
- pulling the right items back out
- moving exact types across multiple carried sources
- avoiding accidental movement of favorites, loadout items, or portable
  containers
- dealing with overflow, junk, trash, and void flows

SLOT should make broad actions understandable, deterministic, and safe.

### 4. Crafting and tool flows break the browsing model

Crafting panels, recipe viewers, backpack upgrades, and storage terminals often
split the interaction model into separate widget systems and separate action
rules.

SLOT should integrate those tools into the same browsing and action model
instead of forcing the player to context-switch into a completely different
interaction surface.

### 5. Workflow support is missing from the default inventory model

Players often need lightweight planning and workflow features:

- favorites
- task-oriented Kits (see [../design/kits.md](../design/kits.md))
- recent pickups
- task-specific browsing
- clear handling for overflow or cleanup

SLOT should support those workflows without turning into logistics automation.

Kits unify what earlier notes split into "collections" (item groupings) plus
"loadouts" (hotbar arrangements). A Kit is a single task-shaped package that
owns one or more hotbar pages, a bring list of non-hotbar items to keep in
carried inventory, and protection flags that activate when the Kit is active.
This is the current user-facing answer to "switch quickly between tasks and
have the right items in the right places."

## Product Principles

- Keep real inventory authority visible where it matters, hidden where it
  doesn't.
- Prefer faster browsing and clearer action semantics over mimicking every
  vanilla layout habit.
- Let broad actions be powerful, but never mysterious.
- Treat crafting, recipe viewers, and storage terminals as first-class workflow
  surfaces.
- Keep recents, cleanup, and future undo/recovery flows grounded in real
  inventory activity rather than transient screen heuristics.
- Keep unsupported or ambiguous integrations out rather than half-supported.
- Preserve room for UI experiments by keeping core logic decoupled from any one
  screen design.

## Long-Term Direction

### Unified carried browsing

- one coherent carried working set
- source-aware aggregation
- strong search, sort, and grouping
- stable handling for multiple carried containers

### Better discovery and planning

- automatic categories
- manual collections
- placeholders and desired counts for tracked items
- recents and triage surfaces

### Faster transfer and storage interaction

- deterministic move-one, move-stack, move-all-exact, and move-all-visible
- better deposit and cleanup flows
- storage interactions that stay grounded in real authority

### Better tool and mod integration

- crafting surfaces
- recipe viewer integration
- backpack and terminal support
- future support for more equipment/accessory ecosystems

### Workflow helpers

- task-oriented Kits (multi-page hotbar presets with bring lists and
  activation-time protection; see [../design/kits.md](../design/kits.md))
- protected/favorite item handling
- overflow and trash flows with explicit intent
- undo/redo where the action family supports it

## Near-Term Experiment Tracks

These are the feature lanes we actively want to explore on top of the current
core. They are directionally important, but the exact UI form is still open.

### 1. Carried browser concepts

Experiment with list-first, pane-first, and hybrid carried UIs that all consume
the same authority/projection/action model.

### 2. Search and grouping

Experiment with which grouping model best reduces friction:

- category-first
- Kit-first (task-oriented groupings; see [../design/kits.md](../design/kits.md))
- recent-first
- mixed sectioning

### 3. Transfer workflows

Experiment with how best to expose:

- move-all-exact across merged rows
- deposit and cleanup actions
- trash and overflow flows
- quick access between carried and external panes

### 4. Crafting and recipe-assisted workflows

Experiment with how browsing, crafting surfaces, and recipe viewers should sit
together without splitting the action model again.

### 5. Workflow surfaces

Experiment with how Kits, recents, favorites, and planning metadata should be
surfaced without overwhelming ordinary browsing. See
[../design/kits.md](../design/kits.md) for the current Kit direction.

## Non-Goals

SLOT should not become:

- remote storage
- infinite inventory
- a logistics network
- hidden autocrafting
- recursive carried-storage traversal
- a mod that mutates items outside real authoritative inventory operations
