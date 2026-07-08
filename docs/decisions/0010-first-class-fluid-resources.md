# 0010: Fluids Are First-Class Resources

Status: accepted

Created: 2026-07-08

This record moves SLOT from item-only inventory identity toward first-class
item/fluid resource accounting.

## Decision

- SLOT represents tracked resources with `SlotResourceIdentity`: kind
  `ITEM` or `FLUID`, stable id, and optional fingerprint/component selector.
- `ItemIdentity` remains the item grouping and item-mutation identity. Item
  resources convert to and from `ItemIdentity`; fluid resources do not.
- Resource amounts are `long`. Item amounts remain item counts; fluid amounts
  are millibuckets.
- Filled fluid containers remain item identities when their item stack matters:
  a filled GregTech drum or super tank is still a distinct item card keyed by
  its contained fluid type, and amount churn does not split that item identity.
  The contained fluid is also counted as a separate fluid resource amount.
- Loader/platform code enumerates fluid contents through fluid capabilities on
  carried item containers, item containers inside world inventories, and block
  or machine tanks. Core code consumes only common fluid-content records.
- Machines, including GregTech hatches and tank-bearing machines, are fluid
  holders when their block exposes a fluid handler. SLOT does not assume a
  multiblock controller owns all fluid IO.
- EMI recipe capture preserves item, fluid, and mixed item/fluid inputs and
  outputs as resource alternatives. GregTech EMI recipes get an additional
  reflective reader for `GTRecipe` fluid capabilities when generic EMI stacks
  are not enough.
- v1 is read-only for fluids. Fluid fill, drain, transfer, desired/wanted
  mutations, trash, home assignment, hotbar movement, and other item-only
  commands reject fluid resource cards with explicit diagnostics.
- AE2 fluid networks are not tracked as fluid storage in v1. They need a
  future `WorldStorageAccess.Delegate` for AE fluid keys and must not be
  folded into item or current AE item-count records.

## Context

The item-only model was enough for ordinary storage, but it loses important
TerraFirmaGreg and GregTech state. Chemical reactors, hatches, drums, super
tanks, cells, buckets, and machine tanks all hold resources that recipes
consume and produce in millibuckets. Ignoring them means SLOT can show item
deficits while silently dropping the actual fluid requirements.

Overloading `ItemIdentity` would create a different bug class. Item identity
already answers "which movable item stack is this?" and carries item-specific
normalization, including the GregTech rule that fluid-container amount churn
does not split a drum identity. A resource identity answers "which counted
resource is this?" Those are related but not the same.

The storage abstraction already gives SLOT the right shape for open-ended
providers: carried sources and world targets are enumerated at the platform
edge, then common code projects and accounts for them. Fluids should follow
that same shape instead of letting recipe readers, UI code, or command
handlers call Forge/NeoForge capabilities directly.

## Rationale

Keeping `ItemIdentity` and `SlotResourceIdentity` separate lets existing item
commands stay authoritative and conservative while recipes and search can
reason about fluids. It also preserves the filled-container behavior players
expect: a water drum and lava drum are different item cards, but a 1000 mB
water drum and a 16000 mB water drum are the same item identity with a changed
fluid amount.

Read-only v1 gives immediate value without pretending SLOT can safely move
fluids. Fill/drain semantics vary by container, machine side, automation
rules, GregTech hatch role, and AE/network security. Until SLOT has a fluid
mutation planner and per-host authority model, showing fluid resources and
rejecting item-only actions is safer than best-effort transfers.

GregTech recipe support needs more than generic item-stack conversion. EMI can
surface fluids directly, but GregTech recipes also encode exact amounts and
tag alternatives through its recipe capabilities. A reflective compat reader
keeps loader code at the edge and avoids making common depend on GregTech.

## Consequences

Benefits:

- SLOT can show fluid totals across carried containers, containers inside
  storage, and block/machine tanks.
- Fluid labels and ids participate in search.
- EMI and GregTech chemical-reactor recipes keep fluid inputs, outputs,
  amounts, and alternatives instead of dropping non-item stacks.
- Craft runs can persist fluid outputs and compute producer floors from
  downstream fluid requirements.
- Existing item mutation and item identity behavior is preserved.

Costs:

- View models, codecs, storage-memory JSON, search, recipe sidebar state, and
  craft-run persistence all carry resource identity fields in addition to
  legacy item identity fields.
- Fluid cards need synthetic item ids for UI surfaces that are still shaped
  around item-card lists.
- v1 exposes read-only resource cards, so users can see a fluid deficit before
  SLOT can stage or transfer it automatically.
- AE2 fluid networks remain a visible gap until AE fluid-key storage gets its
  own delegate.

## Non-Reversal Guidance

Do not collapse fluids back into `ItemIdentity` or item counts. Reconsider the
separate resource model only if Minecraft, Forge/NeoForge, EMI, and GregTech
all converge on one stable cross-loader item/fluid resource abstraction that
can still preserve filled-container item identity separately from contained
fluid amount.

Before adding fluid mutations, require evidence for a common authority model
that can answer which tank/side/handler owns a fill or drain, can restore via
the same layer after partial movement, and can fail closed when a host exposes
ambiguous or inaccessible fluid state.
