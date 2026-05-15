# Facet Kind System

Formal specification of how facet values are authored, validated, and merged
across classification layers. Referenced by:

- [README.md](README.md) — overview of the classification system as a whole
- [schema-authoring-rules.md](schema-authoring-rules.md) — how to decide
  whether a facet may be closed, vocabulary-backed, exact, or free text
- [tools/classification/layer.schema.json](../../../tools/classification/layer.schema.json) — the canonical JSON Schema that validates layer files
- [schema-changelog.md](schema-changelog.md) — versioning policy

Eight kinds are defined. `item_ref` / `multi_item_ref` / `numeric` are reserved for
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

Examples: `rarity`, `carry_frequency`, `y_level_range`, and `dye_color`.

Do not choose `enum` merely because the first known values are short strings.
Pack-shaped semantic sets such as roles, forms, origins, stations, tool
classes, equipment slots, storage kinds, mob interactions, combat bonuses,
environmental properties, transport media, and multiblock roles should be
vocabulary-backed unless the schema authoring audit explicitly scopes them to a
narrow SLOT-owned or vanilla-only meaning.

### `multi_enum` — multiple values from a closed set

Facet declares a list of allowed values. Zero or more values per entry.

Wire format:

```json
"<closed_multi_facet>": { "values": ["value_a", "value_b"], "mode": "add" }
```

Validation: every string in `values` ∈ the facet's declared set. Duplicates within
the same `values` list are an error.

Modes: `replace`, `add` (default), `remove`.

Examples: none in the current semantic schema unless a future narrow SLOT-owned
multi-value scale is deliberately added.

Existing `multi_enum` facets are not automatically valid closed sets. If a mod
can add a valid value, migrate the facet to vocabulary-backed
`multi_free_text`.

### `free_text` — single string matching a regex

Facet declares a regex constraint rather than a value list. Exactly one value per
entry, or absent.

Wire format:

```json
"tier": { "value": "advanced", "mode": "replace" }
```

Validation: `value` matches the facet's declared `pattern`.

Modes: `replace` (default), `override-if-null`.

Examples: `mod_namespace`, or a vocabulary-backed scalar facet such as
`tier`.

### `multi_free_text` — multiple strings each matching a regex

Same validation as `free_text` applied element-wise to a list.

Wire format:

```json
"mod_subsystem": { "values": ["trains", "logistics"], "mode": "add" }
```

Modes: `replace`, `add` (default), `remove`.

Examples: `activity`, `workflow`, `workflow_role`, `used_at`,
`organization_group`, `mod_subsystem`, `primary_uses` (uses `^.{1,80}$` for
phrases), `biome`, `produces_effect`, `multiblock_component_of`.

### Vocabulary-backed values

Most semantic free-text facets are backed by a pack vocabulary artifact. The
registry validates shape; `out/<pack>.facet-vocabulary.json` supplies grounding
values for that pack. Values marked `review` in the vocabulary are still usable
by default; the marker is a watchlist/debugging signal, not a rejection.

Canonical value-id forms:

```text
<token>
<resource_namespace>:<token_path>
<workflow_value_id>#<role_token>
```

`token` is lowercase snake case and starts with a letter. Use
namespace-qualified values only when the value is a real registry/resource id,
such as a station, status effect, multiblock, or biome id. `workflow_role` uses
the scoped `#role` form, for example `steelmaking#input`.

### `boolean` — true/false

Facet has no value list. Absence means false (no separate null state).

Wire format:

```json
"is_fuel": { "value": true, "mode": "replace" }
```

Boolean entries may be exact runtime facts, LLM judgments, or both. Code-derived
boolean evidence is advisory for LLM-authored facets unless the facet is
documented as an exact runtime fact.

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

### `multi_item_ref` — multiple item ids (reserved)

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
| `multi_item_ref` | multi | empty list | `add` |

## Facet declaration (live registry format)

Each facet is declared in
[`FACETS`](../../../tools/classification/src/schema/facets.ts):

```json
{
  "id": "carry_frequency",
  "kind": "enum",
  "description": "How often this item lives in a player's carried inventory.",
  "values": ["everyday", "frequent", "occasional", "rare", "display_only"],
  "llm_authored": true
}
```

For free-text kinds, replace `values` with `pattern`:

```json
{
  "id": "role",
  "kind": "free_text",
  "description": "Vocabulary-backed player-recognized item role.",
  "pattern": "VOCABULARY_VALUE_ID_PATTERN",
  "llm_authored": true,
  "vocabulary_backed": true
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
  "deterministic": true,
  "llm_authored": true
}
```

Live registry metadata fields:

- `llm_authored: true` — Stage 3 targets this facet for LLM judgment when the
  caller includes it.
- `vocabulary_backed: true` — usable values come from the pack vocabulary
  artifact; unlisted useful values may be kept with `vocab_review: true`.
- `deterministic: true` — exact/reference code may fill this facet for
  diagnostics or raw facts. Do not use this flag to make semantic guesses
  authoritative over LLM classification.
- `examples: [...]` — prompt examples for non-vocabulary facets, or small shape
  hints. Vocabulary-backed facets should receive their usable values from the
  pack vocabulary artifact, not from examples.

## Cross-layer merge rules

Given `(item, facet)` and a sequence of layers asserting entries:

1. Walk layers lowest → highest priority: bundled vanilla, bundled per-mod,
   datapack/modpack, server, then player.
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

### LLM-authored facet metadata

Current LLM classification output always gets `source: "llm:stage3"` when it is
written into a layer. It may also carry:

- `rationale` for non-obvious choices or unlisted vocabulary values
- `vocab_review: true` when a vocabulary-backed facet uses an unlisted value
- `ambiguous: true` only for a single-value facet where the model intentionally
  emitted two possible values

`confidence` and `signal` are not classification contract fields. Legacy model
responses may still contain them, but the parser ignores those keys and layer
JSON must not write them.
