# 0006: Cross-Loader Support Targets Forge 1.20.1

Status: accepted

Created: 2026-05-06

This record captures the decision to make SLOT portable from the current
Minecraft 1.21.1 NeoForge + LDLib2 build to a Minecraft 1.20.1 Forge
build for broader modpack reach.

For the engineering slices, see
[../plans/cross-loader-refactor.md](../plans/cross-loader-refactor.md).
For the LDLib2 workspace decision this refactors around, see
[0002-ldlib2-workspace.md](0002-ldlib2-workspace.md).

## Decision

- SLOT will keep the modern 1.21.1 NeoForge + LDLib2 build.
- SLOT will add a legacy Minecraft 1.20.1 Forge build.
- UI construction moves behind a small SLOT-owned UI SPI. The modern
  backend adapts to LDLib2; the legacy backend renders through vanilla
  `Screen` / `GuiGraphics` with the same Taffy layout engine.
- Legacy Forge intentionally does not implement drag. Legacy gestures use
  cursor/drop and keyboard paths that are first proven on the modern
  build. Modern users keep LDLib2 drag.
- The next risk to retire is not the whole UI backend. It is compiling
  shared domain/platform code against Forge 1.20.1 before the SPI
  migration grows.

## Context

Minecraft 1.20.1 remains the practical center of many modded packs. The
existing SLOT implementation targets Minecraft 1.21.1 NeoForge and uses
LDLib2 for the workspace UI, data binding, RPC, menu holder plumbing,
and drag system. LDLib2 v2 is a modern NeoForge library and is not a
drop-in dependency for Forge 1.20.1.

Three options were considered:

- backport LDLib2
- rewrite the UI directly on vanilla screens
- introduce a narrow SLOT-owned UI SPI with two backends

Backporting LDLib2 would put a large third-party UI/networking library
under SLOT maintenance. A direct vanilla rewrite would fork the UI
implementation and make modern/legacy parity expensive. The SPI approach
keeps one widget-construction surface while letting each loader own its
transport and rendering details.

The Phase 0 Forge spike validated the most important assumption behind
the SPI: Taffy can be used directly on Forge 1.20.1 to render and
hit-test a scrollable vanilla `Screen` tree.

## Rationale

Why not backport LDLib2:

- the non-portable parts are not only rendering; they include NeoForge
  networking, sync managers, menu holder integration, and drag dispatch
- owning an LDLib2 fork would make upstream changes and bug fixes harder
  to consume
- SLOT uses only a small slice of LDLib2's surface

Why a SLOT UI SPI:

- both builds can share the high-level workspace builders
- Taffy remains the layout model on both sides
- modern LDLib2 quirks can be documented as explicit SPI invariants
- legacy can omit drag without forcing modern UX backwards

Why compile/platform work comes before the UI migration:

- `common` is currently compiled in a 1.21.1 environment, and several
  shared classes touch Minecraft APIs whose names or semantics changed
  between 1.20.1 and 1.21.1
- storage and item identity behavior matter more than an empty screen;
  a UI port that silently ignores Forge capabilities or misidentifies
  component/NBT-bearing stacks would be worse than no port
- early compile gates expose real platform deltas while the refactor is
  still cheap to reshape

## Consequences

Benefits:

- broader 1.20.1 modpack compatibility without abandoning the modern
  NeoForge build
- a clearer boundary between SLOT workspace semantics and LDLib2
  transport/rendering details
- cursor and keyboard paths become first-class, which improves modern
  accessibility and gives legacy a smaller input system

Costs:

- the UI SPI becomes a SLOT-owned compatibility surface
- loader-specific storage, event, networking, and item-stack shims are
  required before the legacy build can be trusted
- modern drag support needs an adapter/hook boundary so common builders
  do not import LDLib2 directly
- test coverage must run meaningful shared contracts against both
  loader environments where practical

## Non-Reversal Guidance

Abandoning the Forge 1.20.1 target should require evidence from the
early platform compile/storage slices that compatibility risk is larger
than the modpack reach justifies. Do not reverse simply because the SPI
work is tedious. Conversely, do not preserve a partial port that compiles
but fails storage authority or item identity semantics; fail closed and
park the legacy backend if the platform layer proves unsound.
