# Core Inventory Library Assessment

Last updated: 2026-04-14

This assessment reviews external libraries and reference mods that may help
SLOT's inventory core on Minecraft `1.21.1` NeoForge.

The conclusion has changed slightly after the core rewrite:

- SLOT now already owns the core authority/projection/action model it needs.
- External libraries are still valuable, but mostly as low-level primitives,
  adapter targets, or architectural references.
- No reviewed library should replace SLOT's authority, projected-row planning,
  workflow, or crafting-surface model.

## Executive Conclusion

SLOT should keep owning:

- exact authority snapshots
- derived working-set projection
- source-aware action targets
- broad row-transfer planning
- crafting-surface descriptors
- event-backed workflow and activity projections for recents, cleanup,
  collections, protection, tags, and loadouts

The best use of external code is:

1. use NeoForge capabilities and item handlers as low-level primitives
2. build narrow adapters for ecosystems such as Sophisticated Backpacks and
   accessory mods
3. use EMI's public APIs as the recipe-viewer integration surface
4. treat larger QoL/storage mods as references, not as authority libraries

## What The Current Core Already Solves

The rewritten kernel now has first-class support for:

- slot-backed and provider-entry authority
- carried-source identity across multiple physical carried containers
- derived merged browsing that preserves exact backing refs
- row-driven transfer planning with deterministic ordering
- source-wide destination targeting
- crafting surfaces with exact input/output slot targets
- event-backed workflow semantics and bounded inventory-activity history

That removes the main reason to look for a replacement core library.

## Where External Libraries Still Help

### NeoForge APIs

NeoForge `IItemHandler` and capabilities remain the default low-level storage
primitive.

They are useful for:

- reading/writing slot-backed inventories
- bridging block/entity/item-backed handlers
- implementing authoritative provider or menu operations

They are not enough by themselves because they do not supply:

- source identity
- pane membership
- provider-entry semantics
- workflow policy
- merged-row planning

### Sophisticated Core / Sophisticated Backpacks

Still the strongest practical adapter target.

Best uses now:

- stable backpack/container identity
- carried backpack source discovery
- crafting-upgrade surfaces
- upgrade and inventory handler access

SLOT should continue to convert those capabilities into SLOT-owned:

- source descriptors
- authority snapshots
- crafting-surface descriptors
- authoritative mutation operations

### EMI

Still the correct recipe-viewer integration surface.

Best use:

- translate recipe-viewer actions into SLOT-owned crafting or transfer intents

EMI should not become:

- inventory authority
- action planner
- slot identity source for SLOT

### Accessory Ecosystems

Curios, Accessories, and similar mods remain good future adapter candidates.

The relevant question is no longer whether SLOT can model them at all. The core
now can. The relevant question is whether a narrow bridge can emit clean SLOT
descriptors and authoritative operations for them.

## Recommendation Matrix

### Keep As Foundations

- NeoForge capabilities and item handlers
- vanilla menu slot authority for slot-backed screens

### Keep As Optional Adapter Targets

- Sophisticated Core / Sophisticated Backpacks
- Curios / Accessories and similar equipment ecosystems
- Tom's Storage and similar terminal/storage integrations

### Keep As Integration Surfaces

- EMI and similar recipe viewers

### Keep As References, Not Dependencies

- Inventory Profiles Next
- AE2
- InventoryEssentials
- Crafting Tweaks
- Mouse Tweaks

## Bottom Line

The main architecture decision is now settled:

SLOT should not adopt a third-party inventory core. SLOT should keep its own
authority/projection/action/workflow model and use external libraries only where
they strengthen adapters, descriptors, and low-level authoritative operations.
