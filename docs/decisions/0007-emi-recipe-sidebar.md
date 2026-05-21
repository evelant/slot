# 0007: EMI Recipe Context Uses The SLOT Sidebar And Craft Runs, Not Recipe Goals

Status: accepted

Created: 2026-05-16

This record captures the decision to treat an open EMI recipe screen as a
temporary SLOT sidebar filter plus a transient craft-run launcher instead of
growing the SLOT-side recipe-goal system.

## Decision

- When an EMI recipe screen is open, SLOT mounts the normal sidebar on the
  recipe screen and keeps LDLib sync attached to EMI's underlying handled
  menu.
- The sidebar projects only the visible EMI recipe ingredients, using normal
  wall cards, sections, storage pips, and existing missing/craft target chrome.
- The projection is transient. It is not persisted, not a goal tab, and not a
  planner.
- EMI remains the recipe explanation surface. SLOT answers whether the player
  already has the visible recipe ingredients in carried or known storage, and
  lets the player add that recipe to one non-persisted tracked recipe list.
- Craft runs are server-owned player intent: a flat list of recipe entries with
  remaining output counts, acquisition progress, and staging into player main
  inventory. They do not mutate workflows unless the player separately edits a
  workflow.
- The old recipe-goal code/UI/RPC/persistence surface is removed, not preserved
  as a compatibility layer.

## Context

The first EMI integration explored SLOT-side recipe goals: explicit goal tabs,
captured recipe graphs, manual producer choices, goal-scoped desired counts,
and recursive missing requirements. That direction produced useful machinery,
but it also pulled SLOT toward explaining and planning recipes that EMI already
owns.

The simpler player need is immediate and local: while looking at a recipe in
EMI, see the SLOT view filtered to that recipe's inputs so carried, nearby, and
tracked storage status is visible at a glance. Missing ingredients can reuse
the existing target-gap/craft card treatment. If the player wants to act on the
recipe, adding it to the tracked recipe list keeps the intent alive across
EMI/sidebar closes without turning the workflow into a recipe dashboard.

The technical wrinkle is that EMI's `RecipeScreen` is a plain `Screen`, while
the SLOT sidebar historically mounted only on `AbstractContainerScreen`. EMI
keeps the previous handled screen alive as `old`, so SLOT can render into the
recipe screen while syncing through the old menu.

## Rationale

This keeps the boundary clean:

- EMI owns recipe discovery, recipe alternatives, categories, and explanation.
- SLOT owns inventory/storage authority, card actions, transient craft intent,
  acquisition progress, and staging into player main inventory.
- A filtered sidebar plus one flat craft run is cheaper to reason about than a
  persistent recursive recipe goal.
- The player can inspect the real inventory context without committing to a
  permanent workflow change.

It also avoids the most fragile parts of the old approach: recursive recipe
graphs, producer selection, pack-scale planning, and goal persistence. Those
may be worth revisiting only if playtesting proves that a transient recipe
sidebar is not enough.

## Consequences

Benefits:

- recipe context works from the same normal wall/sidebar surface players
  already use
- missing ingredients reuse the existing "need to craft/source" state
- the implementation can be loader-local at the EMI boundary and common at the
  projection boundary
- craft-run pressure composes with workflows/desired/wanted counts without
  creating a second recipe-goal wall
- the legacy goal system is no longer part of the live code path

Costs:

- EMI recipe-screen internals still require small, guarded reflection to read
  the current visible recipe groups
- the sidebar must handle a non-container render host while preserving the
  handled menu as the sync host
- recipe requirements temporarily borrow `desiredCount` card chrome, so
  recipe-mode context menus must avoid exposing global desired-count edits
- explicit hovered-item `Use this` concretization for unresolved alternatives is
  still a follow-up; the initial craft run stores alternatives and stages any
  matching carried item by normal source priority

## Non-Reversal Guidance

Do not return to SLOT-side recipe goals because the sidebar and craft run are
less ambitious. Reconsider only if playtesting shows a concrete repeated need
to persist, compare, or revisit recipe ingredient state beyond the single
current craft run. Even then, prefer a small workflow-scoped recipe launcher
over a recursive pack planner unless the player need is explicitly larger than
"do I have the things for this recipe, and can I stage them?"
