# Schema Changelog & Versioning Policy

Policy for evolving the classification facet schema. Referenced by:

- [README.md](README.md) — overview of the classification system as a whole
- [facet-kinds.md](facet-kinds.md) — kind definitions
- [tools/classification/layer.schema.json](../../../tools/classification/layer.schema.json) — canonical wire format

## Current version

No schema versions have been released yet. The in-flight draft is `v1`,
documented by [README.md](README.md), [facet-kinds.md](facet-kinds.md), and
the generated facet registry in
[tools/classification/src/schema/facets.ts](../../../tools/classification/src/schema/facets.ts).
`v1` freezes when the first layer file ships with `schema_version: 1`.

## Breaking vs non-breaking

Every schema change is classified. Non-breaking → minor bump (v1 → v1.1).
Breaking → major bump (v1 → v2) and migration path required.

### Non-breaking

Safe to ship without regenerating old layers:

- Adding a new facet.
- Adding a new allowed value to an `enum` / `multi_enum`.
- Widening a `free_text` / `multi_free_text` regex (new pattern accepts everything the old one did).
- Adding a new `mode` value.
- Widening a `numeric` facet's range.
- Adding descriptive metadata (`description`, `examples`,
  `vocabulary_backed`, …).
- Promoting a reserved kind (`item_ref`, `numeric`) to first use.

Behaviour: older layers load unchanged. `FacetIndex` silently ignores facet values
it doesn't know about with a one-line log warning. Later pipeline runs backfill
new facets on the items they apply to.

### Breaking

Requires a major version bump and explicit migration:

- Removing or renaming a facet.
- Removing or renaming a value from an `enum` / `multi_enum`.
- Narrowing a `free_text` regex (rejects previously-valid values).
- Narrowing a `numeric` range.
- Changing a facet's `kind` (e.g. `enum` → `multi_enum`).
- Changing cardinality within a kind.
- Changing the default `mode` for a facet.

Behaviour: older layers **cannot** load unchanged. Three migration options per
affected facet:

1. **Forward-migrate** — re-run the pipeline against the new schema. Produces fresh
   layer files. Expected path for `vanilla-base`, `per-mod`, `modpack`.
2. **Translator** — a one-off rewrite step applied when loading older layers. Only
   viable when the change is purely mechanical (rename, value remap, cardinality
   widening).
3. **Drop** — entries referring to removed facets are silently stripped with a
   warning. The rest of the layer is preserved.

### Player layers at major bumps

Player layers are special: they cannot be regenerated. The upgrade path must
preserve `player_island` intent. Concretely:

- Translator path is the only acceptable option for player layers at a breaking bump.
- A breaking bump must ship with:
  - Migration code that rewrites stored player layers in-place (or writes a new file alongside the old).
  - Round-trip tests: load old v1 player fixture → migrate → save → load → assert shape.
  - A backup of the pre-migration file so users can recover if migration goes wrong.
- If a facet in the player layer cannot be translated, the specific entry is dropped
  with a visible warning in-game — **never silently**. Player-island entries must
  never be silently discarded.

## Version bump process

1. Draft the change as a PR touching:
   - [README.md](README.md) or the generated facet registry docs, if the facet
     catalog changes
   - [facet-kinds.md](facet-kinds.md) (if kind behaviour changes)
   - This file (mandatory for every bump)
2. Classify the change using the rules above. If uncertain, choose the stricter
   interpretation (breaking).
3. For non-breaking: bump minor version in the shipped schema file; append an entry
   to [Version history](#version-history).
4. For breaking: bump major version, author migration code in the runtime, add
   fixtures and tests covering each migration path, append a migration note to
   the version-history entry.
5. Re-run the pipeline against `reference/classification/mcmeta` and each per-mod
   target. Commit the regenerated layer files in the same PR as the schema bump.

## Schema-proposals review cadence

The pipeline's stage 3 emits `schema_proposals` in each output file (see
[tools/classification/layer.schema.json](../../../tools/classification/layer.schema.json)).
Review cadence:

- **Per run.** Pipeline prints a summary count at the end of each invocation.
- **Quarterly.** Curator reviews accumulated proposals across runs; triages as
  accept / reject / defer. Accepted proposals queue for the next version bump.
- **Before any major bump.** Fold applicable accepted proposals into the same PR.

CI (when it exists) should fail if the total unreviewed proposal count exceeds
50 — forces review before drift becomes unmanageable.

## Version history

No entries yet. First entry lands when `v1` freezes.

Entry format:

> ### v1.0.0 — YYYY-MM-DD
>
> First stable release. Facets: 28. Kinds in use: `enum`, `multi_enum`,
> `free_text`, `multi_free_text`, `boolean`. Reserved: `numeric`, `item_ref`,
> `multi_item_ref`.
>
> **Changes from v0 draft:** …
>
> **Breaking migrations required:** none (first release).
