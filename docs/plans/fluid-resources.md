# First-Class Fluid Resources

Last updated: 2026-07-08

Plan for adding fluid discovery and recipe accounting without weakening item
mutation authority. ADR [0010](../decisions/0010-first-class-fluid-resources.md)
records the resource-model decision.

## Scope

The current slice is **Track + Plan**:

- Track fluid amounts as resources measured in millibuckets.
- Show fluid resource cards with labels, source presence, and mB/bucket
  formatting.
- Preserve existing filled-container item identity behavior.
- Capture item, fluid, and mixed EMI/GregTech recipes into craft runs.
- Reject item-only commands on fluid resource cards with explicit diagnostics.

Out of scope for v1:

- Fluid fill, drain, transfer, trash, desired/wanted mutation, or hotbar
  movement.
- AE2 fluid-key network storage.
- Automatic completion tracking for fluid outputs.
- Assuming a GregTech multiblock controller owns hatch fluid IO.

## Slice Sequence

1. **Resource primitives and persistence**
   - Add `SlotResourceIdentity`, `SlotResourceAmount`,
     `SlotResourceDisplay`, and `FluidStackAccess`.
   - Keep `ItemIdentity` as the item mutation/grouping identity.
   - Extend storage memory, workspace snapshots, craft-run persistence, and
     view-model codecs with resource fields while old data defaults to empty
     fluid counts.

2. **Read-only fluid discovery**
   - Enumerate fluid capabilities on carried item containers.
   - Enumerate fluid capabilities on item stacks inside world inventories.
   - Enumerate block/machine tank handlers for claimed and display world
     storage targets.
   - Treat inaccessible or unsupported handlers as absent with diagnostics,
     not as fallback item counts.

3. **Projection and UI**
   - Project fluid totals into resource cards while keeping filled containers
     as normal item cards.
   - Add fluid resource ids, labels, and fingerprints to search.
   - Serialize resource refs through Forge and NeoForge view models.
   - Gate item-only actions on fluid cards in UI and common command services.

4. **Recipe and craft-run accounting**
   - Preserve EMI fluid inputs and outputs from `EmiStack` key/id/NBT or
     component data and amount.
   - Supplement GregTech EMI recipes from `GTRecipe` fluid capabilities when
     generic EMI data is incomplete.
   - Represent tag alternatives as resource alternatives.
   - Sum consumed fluid requirements across tracked recipes, and let fluid
     producer outputs raise to downstream fluid demand.

5. **Mutation follow-up**
   - Design a fluid authority/mutation model before implementing fill/drain.
   - Route mutations through common planners/executors, not UI or recipe
     adapters.
   - Restore partial movement through the same handler/layer that extracted.
   - Add AE2 fluid-key storage through a dedicated delegate if playtesting
     needs ME fluid networks.

## Acceptance Criteria

- A carried filled bucket, GregTech drum, super tank, or cell contributes both
  its item card and its contained fluid resource amount.
- A filled container stored inside a chest contributes the same two views.
- A block or machine tank exposed through a loader fluid capability contributes
  fluid resource amounts, including multi-tank machines.
- Empty tanks and inaccessible handlers do not create phantom fluid resources.
- A GregTech chemical-reactor recipe with item and fluid inputs creates a
  craft-run entry that preserves exact mB requirements and alternatives.
- Fluid outputs can be tracked as craft-run outputs and can raise producer
  amounts when downstream recipes require that fluid.
- Search finds fluids by label, resource id, resource kind, and fingerprint.
- Fluid cards reject item-only actions with diagnostics such as
  `fluid_resource_read_only:<action>`.
- Full verification target remains:

```bash
./gradlew :common:test :neoforge:test :forge-1.20:test :common:compileJava :neoforge:compileJava :forge-1.20:compileJava
```

## Test Matrix

- Resource identity stability, fingerprint handling, long mB aggregation, and
  formatting.
- Storage-memory round trip and old-file load with empty fluid counts.
- Cache/revision invalidation when only fluid amount changes.
- GregTech fluid-container item identity remains fluid-type-specific and
  amount-insensitive.
- Carried container fluids, world inventory container fluids, direct block
  tanks, empty tanks, multi-tank machines, and unsupported handlers.
- EMI item-only, fluid-only, mixed item/fluid, GregTech chemical reactor, tag
  alternative, and opaque non-item/non-fluid recipes.
- Forge and NeoForge resource view-model codecs.
- Search and item-only command rejection for fluid cards.
