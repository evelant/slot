# 0004: Kits Supersede Collections + Loadouts

Status: accepted

Created: 2026-04-16

This record captures the decision to collapse the previously separate
"collections" and "loadouts" concepts into a single task-shaped Kit
concept.

For the current Kit design, see [../design/kits.md](../design/kits.md).
For the prototype slices, see [../plans/kit-prototype.md](../plans/kit-prototype.md).

## Decision

- A **Kit** is one task-shaped package owning: one or more hotbar pages,
  a non-hotbar bring list, and protection flags active while the Kit is
  active.
- Kits replace the earlier split between:
  - `CollectionDefinition` + `CollectionMembership` (item groupings with
    atlas-card tag rendering and membership toggles)
  - `QuickAccessLoadoutDefinition` (hotbar arrangements applied through
    `LoadoutApplyService`)
- Kits are atlas-native. They surface through a camera-anchored Belt
  landmark and a toggleable Kit Rack of Kit Cards; they are not a
  sidebar panel competing with atlas geography.
- The earlier sidebar-style collections/loadouts prototype is abandoned
  and is not a supported surface.

## Context

The previous model split one user intent — "when I mine, I want these
items available and my hotbar laid out this way" — into two parallel
lists the player had to curate separately. Two problems emerged:

- **Double curation overhead** — adding a new mining item meant editing
  both the Mining collection and a Mining loadout. Most players will not
  do this consistently enough for either to be trustworthy.
- **Surface competition** — collections-as-tags and loadouts-as-rows
  both wanted chrome around the atlas. Two secondary surfaces crowd the
  map and teach the player to read an item in two places.

Separately, the atlas decision (ADR 0003) reframed the player-inventory
workspace as a pan/zoom canvas where task switching should feel physical
— swapping which items are on your belt, not selecting from a list.
"Kit" names the task-shaped object naturally; "collection" + "loadout"
does not.

## Rationale

Why one concept instead of two:

- the player thinks in tasks, not in "item groups" and "hotbar
  arrangements" separately
- the data does not actually gain clarity from the split — a loadout
  implies a set of items; a collection for a task implies a preferred
  hotbar placement
- reducing to one concept makes the UI surface dedicated and discoverable
  instead of two half-surfaces competing for chrome

Why atlas-native Belt + Kit Rack instead of a sidebar:

- the Belt is the vanilla hotbar reified as an atlas landmark; it is
  always present, always visible, and camera-anchored
- Kit Cards live in a toggleable Rack so Kits are reachable without
  permanently spending atlas chrome
- this keeps the atlas the dominant surface; Kits read as objects on the
  map, not as a separate app

Why drop generic collections:

- "favorites," "project materials," and "long-term saving" are real
  needs, but they are distinct from task switching
- if any of those earn a surface later, they can be designed for their
  own shape rather than reusing a collections construct that was already
  serving task switching poorly

## Consequences

Benefits:

- single curation step when a task gains a new item
- a single dedicated UI surface (Belt + Kit Rack) instead of two
  secondary surfaces
- Kit activation can drive hotbar page, bring-list targeting, and
  task-scoped protection coherently
- the `LoadoutApplyService` path is reused as the Kit activation executor
  — no new mutation pipeline

Costs:

- any existing code or docs referencing `CollectionDefinition` /
  `CollectionMembership` / `QuickAccessLoadoutDefinition` as
  user-facing concepts must migrate to Kit vocabulary (internal workflow
  domain names may persist temporarily as implementation detail)
- "non-task groupings" are deferred; players who want favorites or
  project materials do not have a surface for those needs until one is
  designed explicitly
- multi-page Kit switching introduces hotbar page state the atlas must
  visualize clearly to avoid confusing real mutation with page change

## Non-Reversal Guidance

Reverting to a collections + loadouts split should require evidence that
the unified Kit concept cannot cover a real player need that the split
model handled well. Record that evidence in a new ADR rather than
reintroducing the split silently, because the Kit concept is the
workflow-rail basis the atlas is designed around (ADR 0003).
