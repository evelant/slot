# 001: Core Architecture Rewrite Record

Status: rewrite record

Created: 2026-04-10

Updated: 2026-04-14

This document records the reasoning behind SLOT's core architecture rewrite. It
is no longer the live source of truth for the current design.

Current truth now lives in:

- [../product/direction.md](../product/direction.md)
- [../architecture/overview.md](../architecture/overview.md)
- [../product/spec.md](../product/spec.md)
- [../plans/current.md](../plans/current.md)

## Why The Rewrite Happened

SLOT had the right product direction but the implementation still carried
prototype architecture:

- screens owned too much behavior
- user actions did not reliably converge into one pipeline
- aggregation logic and authority logic were too entangled
- crafting and compat paths were split across helpers, panels, and operations
- workflow features were too screen-local

The rewrite was intended to make the design docs implementable rather than
aspirational.

## Main Decisions Landed

### 1. Authority And Projection Were Split

The current kernel distinguishes:

- exact authority snapshots
- derived working-set projection

Rows are projection. They are never authority.

### 2. Authority Can Be Slot-Backed Or Provider-Entry-Backed

The rewrite stopped pretending that every useful source can be represented as an
exact slot list.

The current model explicitly supports:

- exact slot-backed entries
- provider-owned entries with stable ids

### 3. Merged Carried Browsing Is Derived, Not Invented By UI

Multiple carried sources may merge into one visible row, but the row must keep
ordered backing entries and backing sources so broad actions remain exact and
deterministic.

### 4. Broad Row Transfers Became Planner-Driven

Projected-row transfer planning now consumes:

- exact authority
- caller-supplied visible rows
- explicit action scope
- explicit destination
- protection policy

and produces ordered concrete requests rather than guessing from aggregate row
counts.

### 5. Crafting Surfaces Stayed Slot-Backed

Crafting-capable tools now expose exact linked slot targets through a crafting
surface descriptor instead of relying on UI-local assumptions.

### 6. Host-Aware Canonicalization Replaced Hostless Alias Logic

Conflict detection, protection, and outcomes now resolve action identity through
the active host rather than through detached alias strings.

### 7. Workflow And Activity Moved To A Hybrid Event-Backed Runtime

The rewrite follow-on replaced snapshot-only mutable workflow stores with:

- a durable workflow event log for user semantic state
- a bounded activity event log for inventory activity and external observations
- read projections for collections/tags/loadouts/protection and for
  recents/cleanup/recovery
- snapshot browse state kept separate from that history

This was necessary to keep recents, cleanup, and future undo/recovery flows
decoupled from any one screen host.

## Research Conclusions That Still Hold

- NeoForge capabilities and item handlers are primitives, not a complete SLOT
  architecture.
- EMI should remain a recipe-viewer integration surface, not inventory
  authority.
- Sophisticated Backpacks and Tom's Storage are best treated as narrow adapter
  targets that emit SLOT-owned descriptors and operations.
- UI frameworks may help with layout and presentation later, but they cannot
  replace SLOT's core action and authority model.

## What This Record Is Still Useful For

This document should remain as a rewrite log and reasoning reference for:

- why the old prototype direction was replaced
- what assumptions guided the rewrite
- why certain boundaries are now considered non-negotiable

It should not be updated as if it were the day-to-day architecture spec.
