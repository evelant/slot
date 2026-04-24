# Facet Kind System

Formal specification of how facet values are authored, validated, and merged
across classification layers. Referenced by:

- [docs/plans/item-classification.md](../../plans/item-classification.md) — the planning document
- [layer-format.schema.json](layer-format.schema.json) — the JSON Schema that validates layer files
- [schema-changelog.md](schema-changelog.md) — versioning policy

Eight kinds are defined. `item_ref` / `item_ref_multi` / `numeric` are reserved for
V2 but the validator recognises them so forward-compatible schemas don't need a
breaking bump when we activate them.

## Kinds

### `enum` — single value from a closed set

Facet declares a list of allowed string values. Exactly one value per entry, or
absent (facet not asserted for this item).

Wire format:

```json
"role": { "value": "mechanism", "mode": "replace" }
```

Validation: `value` ∈ the facet's declared `values` list.

Modes: `replace` (default), `override-if-null`.

Examples: `role`, `rarity`, `frequency`, `form`, `material_family`, `required_tool`,
`required_tool_tier`, `equip_slot`, `multiblock_role`, `y_level_range`, `dye_color`.

### `multi_enum` — multiple values from a closed set

Facet declares a list of allowed values. Zero or more values per entry.

Wire format:

```json
"activity": { "values": ["mining", "combat"], "mode": "add" }
```

Validation: every string in `values` ∈ the facet's declared set. Duplicates within
the same `values` list are an error.

Modes: `replace`, `add` (default), `remove`.

Examples: `activity`, `origin`, `flavor`, `palette`, `storage_categories`,
`material_secondary`, `spawn_interaction`, `combat_bonus`, `environmental_property`,
`transport_medium`.

### `free_text` — single string matching a regex

Facet declares a regex constraint rather than a value list. Exactly one value per
entry, or absent.

Wire format:

```json
"tier": { "value": "mekanism:advanced", "mode": "replace" }
```

Validation: `value` matches the facet's declared `pattern`. Standard pattern for
namespace-scoped values: `^[a-z0-9_]+(:[a-z0-9_]+)?$`.

Modes: `replace` (default), `override-if-null`.

Examples: `tier`, `mod_namespace`.

### `multi_free_text` — multiple strings each matching a regex

Same validation as `free_text` applied element-wise to a list.

Wire format:

```json
"mod_subsystem": { "values": ["create:trains", "create:logistics"], "mode": "add" }
```

Modes: `replace`, `add` (default), `remove`.

Examples: `mod_subsystem`, `primary_uses` (uses `^.{1,40}$` for phrases),
`biome`, `produces_effect`, `multiblock_component_of`.

### `boolean` — true/false

Facet has no value list. Absence means false (no separate null state).

Wire format:

```json
"is_fuel": { "value": true, "mode": "replace" }
```

Most booleans are derived at extraction time, not authored by hand.

Modes: `replace` (default). `override-if-null` is not meaningful here — absence and
false are indistinguishable.

Examples: `is_block_item`, `is_stackable`, `is_fuel`, `has_durability`,
`has_enchantments`, `has_nbt_variation`, `is_creative_only`.

### `numeric` — number with optional unit (reserved)

Facet declares min/max range and optional unit string. Exactly one value per entry,
or absent.

Wire format:

```json
"container_capacity": { "value": 27, "mode": "replace" }
```

Validation: `value` is a finite number in the declared range.

Modes: `replace` (default).

Examples: none in V1. First expected use is `container_capacity` when the storage
facet gains ranking queries.

### `item_ref` — single item id (reserved)

Facet declares a regex constraint against Minecraft resource-location format. Validates
structure only; does **not** validate that the referenced item is currently loaded (items
from uninstalled mods are valid data).

Wire format:

```json
"grows_into": { "value": "minecraft:wheat", "mode": "replace" }
```

Standard pattern: `^[a-z0-9_.-]+:[a-z0-9_/.-]+$`.

Modes: `replace` (default), `override-if-null`.

Examples: none in V1. Reserved for V2 facets like `grows_from`, `grows_into`,
`parent_item`, `charges_with`.

### `item_ref_multi` — multiple item ids (reserved)

Multi-value form of `item_ref`.

Wire format:

```json
"crafting_companions": { "values": ["minecraft:stick", "minecraft:leather"], "mode": "add" }
```

Modes: `replace`, `add` (default), `remove`.

## Cardinality summary

| Kind | Cardinality | Absence semantic | Default mode |
| --- | --- | --- | --- |
| `enum` | single | null | `replace` |
| `multi_enum` | multi | empty list | `add` |
| `free_text` | single | null | `replace` |
| `multi_free_text` | multi | empty list | `add` |
| `boolean` | single | false | `replace` |
| `numeric` | single | null | `replace` |
| `item_ref` | single | null | `replace` |
| `item_ref_multi` | multi | empty list | `add` |

## Facet declaration (schema v1 format)

Each facet is declared in `schema.v1.json`:

```json
{
  "id": "role",
  "kind": "enum",
  "description": "The fundamental kind of thing.",
  "values": [
    "material", "natural_resource", "building_block", "decorative_block",
    "functional_block", "storage_block", "mechanism", "redstone_component",
    "tool", "weapon", "armor", "consumable", "ammunition", "transport",
    "container_portable", "utility", "curiosity", "upgrade", "trophy", "admin"
  ]
}
```

For free-text kinds, replace `values` with `pattern`:

```json
{
  "id": "tier",
  "kind": "free_text",
  "description": "Progression tier (vanilla or mod-specific).",
  "pattern": "^[a-z0-9_]+(:[a-z0-9_]+)?$"
}
```

For numeric kinds:

```json
{
  "id": "container_capacity",
  "kind": "numeric",
  "description": "Slot count for storage items.",
  "min": 0,
  "max": 10000,
  "unit": "slots"
}
```

For booleans:

```json
{
  "id": "is_fuel",
  "kind": "boolean",
  "description": "Burns in a furnace.",
  "derived": true
}
```

Optional common fields across all kinds:

- `deprecated: true` — retained for compatibility, not emitted by new pipeline runs.
- `llm_authored: true` — hint for stage 3 that this facet needs LLM judgement.
- `runtime_derivable: true` — hint that the runtime-crawl layer can fill this.
- `applies_when: "<dsl>"` — optional precondition (e.g. `role in (tool, weapon, armor)` for `tier`). Purely advisory.

## Cross-layer merge rules

Given `(item, facet)` and a sequence of layers asserting entries:

1. Walk layers lowest → highest priority. See [layer order](../../plans/item-classification.md#layer-order-lowest-priority-first).
2. For each entry in priority order, apply its `mode` against the accumulating state:
   - `replace` — new value replaces accumulating value (single) or list (multi).
   - `add` — union new values into accumulating list (multi only).
   - `remove` — subtract new values from accumulating list (multi only).
   - `override-if-null` — set accumulating value only if it's still null (single only).
3. Final value is the accumulating state after all layers.

### Error conditions

- Two entries at the same layer for the same `(item, facet)`: layer rejected, error logged. Layers must be conflict-free internally.
- `mode` doesn't match kind (e.g. `add` on a single-value facet): entry rejected, rest of layer accepted.
- Value fails kind-specific validation (bad enum value, regex mismatch, out-of-range number): entry rejected.

### LLM-authored facet provenance

Every entry produced by stage 3 of the pipeline also carries `confidence` (0–1),
`rationale` (free text), `source` (e.g. `llm:haiku-4.5`), and optionally
`ambiguous: true` (when the model emitted multiple low-confidence candidates for
a single-value facet — see [LLM ambiguity policy](../../plans/item-classification.md#stage-3--llm-assisted-completion)).

These fields are metadata and don't affect merge semantics, but they are preserved
all the way to the runtime `FacetIndex` so debug tooling can explain *why* an item
is where it is.
