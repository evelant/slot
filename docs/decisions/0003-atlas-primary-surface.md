# 0003: Atlas Is The Primary Player-Inventory Surface

Status: accepted

Created: 2026-04-16

This record captures the decision to make a triage-first pan/zoom visual
atlas the primary player-inventory surface, and to abandon the earlier
list-first workspace prototype.

For the concept, see [../design/atlas.md](../design/atlas.md).
For the engineering slices, see [../plans/done/atlas-prototype.md](../plans/done/atlas-prototype.md).
For the UI transport boundary this still runs over, see
[0002-ldlib2-workspace.md](0002-ldlib2-workspace.md).

## Decision

- The player-inventory workspace is an atlas: a pan/zoom canvas where
  each item identity occupies a stable visual "home."
- Homes are primarily player-authored. Automatic categorization is
  conservative — only very high-confidence cases (obvious placeable
  blocks) auto-home; everything else lands in a first-class `Triage`
  island until the player places it.
- The earlier list-first workspace prototype is abandoned. It is not a
  supported mode, not a fallback, and not a maintenance target.
- The LDLib2 workspace transport boundary established in ADR 0002
  continues to carry the atlas; this pivot is presentation + projection,
  not transport.

## Context

The list-first prototype proved the LDLib2 workspace transport works
end-to-end (server-owned view model, RPC transfer commands, exact hotbar
assignment) but exposed two product-level problems:

- **Icon entropy in modded play** — sorted rows of modded items do not
  actually make the inventory scannable. Names help but the list becomes
  a spreadsheet that competes poorly with vanilla's dense grid for
  spatial memory.
- **Spatial memory is the real scaling lever** — players can remember
  "my rockets live top-right" across thousands of item identities if the
  UI supports it. Lists destroy that memory every refresh.

The atlas proof of concept showed that pan/zoom + stable item homes +
progressive disclosure at zoom level is viable over the existing LDLib2
transport, including camera preservation through view refreshes and the
same hotbar transfer RPC.

The remaining uncertainty is not the surface itself — it is whether
players can give new item identities memorable places quickly enough to
rely on spatial memory. That question drives the triage-first direction
rather than a richer automatic taxonomy.

## Rationale

Why atlas over a smarter list:

- lists reset spatial context on every filter/sort; atlases preserve it
- category filters force a taxonomy decision SLOT cannot get right in
  modded packs; triage + player placement sidesteps the taxonomy problem
- spatial memory scales with item count; list scanning does not
- atlas geometry naturally accommodates future surfaces (Kit Rack,
  external source compare, source memory) as landmarks/islands without
  competing with a dominant list chrome

Why triage-first over aggressive auto-categorization:

- bad silent homes erode trust faster than visible triage costs attention
- heuristic classifiers in modded packs have poor precision
- player-authored homes are authoritative; suggestions are not
- this keeps the first iteration testable — the core UX question is
  "can the player quickly place new items and rely on the placement?"

## Consequences

Benefits:

- spatial memory as a first-class scaling mechanism
- presentation pivots (hotbar rail placement, inspector placement, lens
  overlays) do not require layout rewrites; the atlas is the surface
- follow-on concepts (Kits, external source islands, source memory)
  compose as atlas-native objects
- the LDLib2 transport from ADR 0002 is reused without re-architecting

Costs:

- atlas navigation must be fast enough for casual inventory opens or it
  loses to the list on everyday tasks — hotbar rail stays fixed, default
  viewport must be immediately useful, search must spotlight in place
- `GraphView` gesture quirks (wheel target, left-drag on empty
  background, offset setters not refreshing transform) are now part of
  the workspace contract; documented in [../../AGENTS.md](../../AGENTS.md)
- item LOD becomes a real design surface — the atlas needs
  screen-budget-aware rendering so zoom produces information density, not
  just "the same thing, bigger"
- visual home state is a new presentation domain distinct from inventory
  authority; home commands must stay separate from mutation commands

## Non-Reversal Guidance

Reverting to a list-first primary surface should require evidence that
the triage/home loop fails its UX test (players cannot place items
quickly or do not trust placement). Recording that evidence as a new ADR
is preferable to silent reversion, because the atlas decision is the
basis for the Kits concept (ADR 0004) and for the external-storage
direction documented in the atlas plan.
