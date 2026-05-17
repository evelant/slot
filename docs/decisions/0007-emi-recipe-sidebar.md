# 0007: EMI Recipe Context Uses The SLOT Sidebar, Not Recipe Goals

Status: accepted

Created: 2026-05-16

This record captures the decision to treat an open EMI recipe screen as a
temporary SLOT sidebar filter instead of growing the SLOT-side recipe-goal
system.

## Decision

- When an EMI recipe screen is open, SLOT mounts the normal sidebar on the
  recipe screen and keeps LDLib sync attached to EMI's underlying handled
  menu.
- The sidebar projects only the visible EMI recipe ingredients, using normal
  wall cards, sections, storage pips, and existing missing/craft target chrome.
- The projection is transient. It is not persisted, not a goal tab, and not a
  planner.
- EMI remains the recipe explanation surface. SLOT only answers whether the
  player already has the visible recipe ingredients in carried or known
  storage.
- Existing recipe-goal code may remain until removed in a focused cleanup, but
  it is no longer the near-term EMI product direction.

## Context

The first EMI integration explored SLOT-side recipe goals: explicit goal tabs,
captured recipe graphs, manual producer choices, goal-scoped desired counts,
and recursive missing requirements. That direction produced useful machinery,
but it also pulled SLOT toward explaining and planning recipes that EMI already
owns.

The simpler player need is immediate and local: while looking at a recipe in
EMI, see the SLOT view filtered to that recipe's inputs so carried, nearby, and
tracked storage status is visible at a glance. Missing ingredients can reuse
the existing target-gap/craft card treatment.

The technical wrinkle is that EMI's `RecipeScreen` is a plain `Screen`, while
the SLOT sidebar historically mounted only on `AbstractContainerScreen`. EMI
keeps the previous handled screen alive as `old`, so SLOT can render into the
recipe screen while syncing through the old menu.

## Rationale

This keeps the boundary clean:

- EMI owns recipe discovery, recipe alternatives, categories, and explanation.
- SLOT owns inventory/storage authority and card actions.
- A filtered sidebar is cheaper to reason about than a persistent recipe goal.
- The player can inspect the real inventory context without committing to a
  plan or creating cleanup state.

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
- the feature remains useful even if the goal system is later removed

Costs:

- EMI recipe-screen internals still require small, guarded reflection to read
  the current visible recipe groups
- the sidebar must handle a non-container render host while preserving the
  handled menu as the sync host
- recipe requirements temporarily borrow `desiredCount` card chrome, so
  recipe-mode context menus must avoid exposing global desired-count edits

## Non-Reversal Guidance

Do not return to SLOT-side recipe goals because the sidebar is less ambitious.
Reconsider only if playtesting shows a concrete repeated need to persist,
compare, or revisit recipe ingredient state after the EMI recipe screen closes.
Even then, prefer a small persisted ingredient list over a recursive pack
planner unless the player need is explicitly larger than "do I have the things
for this recipe?"
