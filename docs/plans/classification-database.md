# Classification Database And Pack UX Plan

Last updated: 2026-05-12

Status: partially implemented. Installed-pack scanning, jar-backed
classification, runtime export, pack facet vocabulary generation including
`mod_subsystem`, datapack pack-layer generation, and in-game classifier
diagnostics are now implemented. Public database distribution,
review/diff/publish tooling, runtime-crawl, and persistent server/player facet
layers remain planned.
This plan turns item classification from a local one-off generation tool into
shared mod metadata that normal SLOT users consume automatically. It covers
distribution, pack scanning, cache identity, and contribution workflows.

For the system overview, see
[../design/classification/README.md](../design/classification/README.md).
For the current operational baseline, see [../status.md](../status.md).

## Product Principle

Most players should never run the LLM classifier.

Classification is durable mod metadata. Once SLOT has generated and
reviewed a semantic layer for a specific mod release, every player and
pack using that same release should reuse it. Local generation remains
valuable for private mods, unreleased forks, KubeJS-heavy packs, and
contributors preparing new public data, but it should be explicit
curator/operator work.

## JEI / EMI Boundary

SLOT should not recreate JEI or EMI. Those tools already answer recipe,
usage, substitute, and acquisition questions extremely well, and SLOT
should integrate with them rather than duplicate their surfaces.

The classification layer is for non-obvious inventory semantics:

- where an item belongs in an organized inventory or chest system
- whether a player probably carries it, stashes it, displays it, or
  treats it as disposable clutter
- which workflow, project, mod subsystem, or mental bucket it belongs to
- whether pack scripts or quests have turned a normal item into
  progression state, currency, or another semantic role
- which uncertain or low-confidence items deserve curator review

Raw recipe graph, acquisition, worldgen, and substitution facts should
remain JEI/EMI-owned unless SLOT needs a compact semantic summary to make
an inventory decision.

## Use Cases

### Normal Player

Goal:

- install SLOT into an arbitrary modpack and get good classification
  without API keys, external tools, or LLM calls

Behavior:

- bundled vanilla and popular-mod data loads by default
- optional public database cache fills known mod layers that SLOT did
  not bundle
- runtime crawl derives deterministic facets for unknown items
- unknown semantic facets are absent rather than guessed at runtime

Exit criteria:

- an unknown pack still has stable, deterministic homes for obvious
  forms/materials
- known mods do not require per-user regeneration
- missing semantic coverage is visible in diagnostics, not hidden

### Pack Author Or Power User

Goal:

- point the tool at an installed instance or `mods/` folder and know
  which mods are covered, stale, unknown, or worth generating
- for KubeJS/datapack-heavy packs, export the live item/recipe/tag state
  from a running instance and generate a pack-specific layer from that
  exact runtime truth

Behavior:

- `scan --mods <folder>` reports local jar identity, public/bundled
  coverage, cache status, item-count estimates, and expected generation
  work
- `fetch-public --mods <folder>` downloads matching public layers where
  available
- `/slot classification export` writes a deterministic runtime export
  from the loaded game when static jar data is not enough
- `/slot classification inspect` verifies the loaded classifier view for
  one item, and `/slot classification rehome` bulk-recomputes homes for
  carried items plus accessible claimed chests
- `generate-missing --mods <folder>` only runs for explicit missing
  semantic layers
- `write-modpack-layer` emits pack-specific overrides for private
  semantics

Exit criteria:

- a pack author can prepare classification for a pack from the installed
  files they already have
- private/custom mods can be improved locally without modifying SLOT
  source
- the report distinguishes "covered by shared data" from "derived by
  runtime crawl" from "needs semantic generation"
- a KubeJS-heavy pack can produce a modpack layer from live registry,
  tag, and recipe state rather than from guessed static jar contents

### SLOT Maintainer

Goal:

- generate high-quality public data once per meaningful mod release

Behavior:

- curator workflow produces a layer, review report, provenance metadata,
  and diff against any previous layer
- publication requires schema validation and review gates
- public database stores exact input identity so consumers can avoid
  stale or mismatched layers

Exit criteria:

- a reviewed layer can be published independently of a SLOT code release
- repeated runs are traceable to prompt/model/tool versions
- generated output is treated as reviewed data, not deterministic truth

### Mod Author

Goal:

- optionally ship authoritative classification data with the mod itself

Behavior:

- SLOT can load `data/slot/classification/<modid>.json` from another mod
  jar when present
- mod-shipped layers carry provenance `source_kind: "mod-shipped"`
- mod-shipped semantic data wins over the public database for the same
  item unless a modpack/server/player layer overrides it

Exit criteria:

- a mod author can make SLOT understand their item semantics without a
  central database PR
- badly formed mod-shipped layers fail closed with diagnostics and do
  not block vanilla/base data

### Server Operator

Goal:

- pin a classification state for a server or curated pack

Behavior:

- server/pack layers can be distributed alongside server config or
  datapacks
- server layer can correct public/mod-shipped data for local policy
- player layer still wins for personal organization

Exit criteria:

- clients see consistent server-approved semantics where required
- local player homes remain personal overrides

## Layer Order

Effective layer priority, lowest to highest:

1. `vanilla-base` — bundled SLOT data for `minecraft:*`.
2. `public-per-mod` — public database layers fetched or bundled by SLOT.
3. `mod-shipped` — classification layer shipped inside a third-party mod
   jar.
4. `runtime-crawl` — deterministic facts from the actual loaded game.
5. `modpack` — pack-author overrides.
6. `server` — server-operator overrides.
7. `player` — per-player homes and corrections.

Runtime crawl deliberately sits above public/mod-shipped layers for
deterministic facets because the live registry, tags, recipes, and
components are the final truth for a loaded pack. It must not invent
LLM-authored semantic facets.

A saved running-instance export is an offline form of runtime crawl. The
export feeds the classifier tool and usually writes a `modpack` layer,
because its scope is the pack's exact loaded state rather than a reusable
upstream mod release.

## Public Database Model

The public database is a versioned static artifact. It can start as a
Git repository and later move behind a CDN without changing the data
format.

Proposed layout:

```text
slot-classification-db/
  index.v1.json
  mods/
    create/
      create-6.0.4-mc1.21.1-neoforge.sha512-abc123.json
      create-6.0.4-mc1.21.1-neoforge.sha512-abc123.report.json
```

### Index Entry

Each public layer must be discoverable by exact identity first:

```json
{
  "schema_version": 1,
  "database_version": "2026-05-08",
  "entries": [
    {
      "mod_id": "create",
      "display_name": "Create",
      "loader": "neoforge",
      "minecraft_versions": ["1.21.1"],
      "mod_version": "6.0.4",
      "jar_sha512": "abc123...",
      "jar_sha1": "def456...",
      "modrinth_project_id": "...",
      "modrinth_version_id": "...",
      "curseforge_project_id": 328085,
      "curseforge_file_id": 1234567,
      "item_set_signature": "sha256:...",
      "layer_path": "mods/create/create-6.0.4-mc1.21.1-neoforge.sha512-abc123.json",
      "review_status": "reviewed",
      "coverage": {
        "items": 580,
        "role": 580,
        "material_family": 420,
        "primary_uses": 560
      }
    }
  ]
}
```

### Match Rules

Consumers must choose layers conservatively:

1. exact jar SHA512 or SHA1
2. exact platform file id, e.g. Modrinth version id or CurseForge file id
3. exact item-set signature
4. same mod id plus explicitly declared compatible version range
5. no semantic layer; use runtime crawl only

Fallback #4 is optional and must be opt-in in the database entry. Silent
same-mod-id reuse is forbidden because it can misclassify renamed or
repurposed items.

### Layer Provenance

Every generated layer should include metadata beyond the current wire
minimum:

- input source kind: `jar`, `source`, `modrinth`, `curseforge`,
  `mod-shipped`, `manual`
- jar hash(es)
- platform project/file ids when known
- loader and Minecraft version when known
- extracted namespaces
- item count and item-set signature
- tool version
- schema version
- prompt version
- LLM backend/model/provider
- generation timestamp
- review status: `raw`, `reviewed`, `curated`

The runtime can ignore unknown metadata fields, but the publication and
scan tools must preserve and validate them.

## Tool UX

### Primary Commands

```sh
slot-classify scan --mods /path/to/instance/mods
slot-classify fetch-public --mods /path/to/instance/mods --cache ~/.slot/classification
slot-classify classify-folder --mods /path/to/instance/mods --out out --stages 1,2
slot-classify generate-pack-layer --runtime-export exports/pack.runtime-items.ndjson --out out
slot-classify generate-missing --mods /path/to/instance/mods --out out --stages 1,2,3
slot-classify write-modpack-layer --mods /path/to/instance/mods --out config/slot/classification
slot-classify publish-candidate --layer out/create.facets.complete.json
```

`scan` is the main usability unlock. It must run without network or LLM
access and produce an actionable plan before any expensive work happens.

### Scan Report

`scan --mods` should emit both machine-readable JSON and a concise human
summary:

- jar path
- mod id(s), display name, version, loader
- detected namespace(s)
- jar hashes
- platform ids if inferable from sidecar metadata
- resource counts: items, lang keys, recipes, loot tables, tags, models
- public/bundled/mod-shipped layer match status
- stale cache status
- skipped reason for libraries or zero-item mods
- estimated stage-3 batch count if semantic generation is requested
- exact next command

Status values:

- `covered:bundled`
- `covered:public-exact`
- `covered:public-compatible`
- `covered:mod-shipped`
- `covered:local-cache`
- `partial:runtime-crawl-only`
- `missing:semantic-generation-available`
- `skipped:library`
- `blocked:ambiguous-mod-id`
- `blocked:malformed-jar`

### Generation UX

Local generation must be opt-in and resumable:

- default `classify-folder` runs stages 1 and 2 only
- stage 3 requires `--stages 1,2,3` or `generate-missing`
- resume decisions use jar hash + tool metadata, not just output
  existence
- stage 3 writes fixtures/cache so interrupted runs can continue
- low-confidence, ambiguous, schema-proposal, and fill-in queues are
  written next to the layer
- runtime exports may run stage 3 only for items whose semantic facets
  are missing or pack-specific, not for every public-covered item

### Review UX

Add focused review commands before publication:

```sh
slot-classify review --layer out/create.facets.complete.json --low-confidence
slot-classify review --layer out/create.facets.complete.json --facet role
slot-classify diff --old db/mods/create/old.json --new out/create.facets.complete.json
slot-classify report --layer out/create.facets.complete.json --html out/create.report.html
```

Review output should prioritize:

- role changes
- material/form changes
- low-confidence role/activity/primary_uses
- ambiguous single-value facets
- schema proposals
- fill-ins that point to missing deterministic rules
- items with no semantic facets

## Input Adapters

### Installed `mods/` Folder

First implementation target.

Rationale:

- works for CurseForge, Modrinth, Prism, ATLauncher, manual packs, and
  private packs
- avoids automated download policy issues
- local jars are the exact files the game will load

Deliverables:

- scan `*.jar`
- read `META-INF/mods.toml`, `META-INF/neoforge.mods.toml`,
  `fabric.mod.json`, and known loader metadata
- derive mod id, display name, version, loader, and candidate
  namespaces
- compute jar hashes
- produce input manifest v2

### Jar Extractor

Build a jar-backed adapter that produces the same bundle shape as the
current source-tree extractor.

It must support both resource naming eras:

- `data/<ns>/recipes/**` and `data/<ns>/recipe/**`
- `data/<ns>/loot_tables/**` and `data/<ns>/loot_table/**`
- `data/<ns>/tags/items/**` and `data/<ns>/tags/item/**`
- `data/<ns>/tags/blocks/**` and `data/<ns>/tags/block/**`
- `assets/<ns>/lang/en_us.json`
- `assets/<ns>/models/**`
- `assets/<ns>/blockstates/**`
- `assets/<ns>/items/**`

Item candidates should merge multiple signals:

- lang keys
- item definition files
- model files referenced by item definitions
- recipe outputs
- loot outputs
- tags that directly mention namespace items

No single signal is sufficient. The initial jar implementation preserves
those signals through the normal stage-1 fields: display names, direct
tags, recipe roles, loot sources, model parents, and resource metadata.
A future provenance pass can add per-item `extractor_meta` if review
tooling needs to explain exactly which static signals admitted an item.

### Source Repository

Keep source repositories as a curator enrichment path.

Source scans can add:

- README / documentation context for `mod_subsystem`
- source-level templates before Gradle substitution
- generated resources when present

They should not be required for normal pack classification.

### `.mrpack`

Second import target.

Behavior:

- parse `modrinth.index.json`
- validate paths and hashes
- classify bundled override jars if present
- optionally download files from declared Modrinth URLs into a local
  cache
- normalize to input manifest v2 before extraction

### CurseForge Manifest

Third import target.

Behavior:

- parse manifest project/file ids
- classify any local jars that are already present
- optionally resolve metadata through the official CurseForge API when
  the user provides an API key
- respect missing download URLs and distribution restrictions
- report manual-required files rather than scraping or bypassing policy

### Running Instance Runtime Export

First-class adapter for KubeJS-heavy packs, datapack-heavy packs, and
private servers.

Static jar scanning sees upstream resources. It cannot see the final
registry, tag graph, or recipe graph after KubeJS, CraftTweaker,
datapacks, and server config have mutated the pack. A running-instance
export captures that state after the game has loaded it.

In-game command:

```text
/slot classification export
/slot classification export <pack_id>
```

Current output:

```text
config/slot/classification/exports/<pack-id>.runtime-items.ndjson
config/slot/classification/exports/<pack-id>.runtime-summary.json
```

Initial implementation exists for Forge 1.20.1 and NeoForge 1.21.1. It
writes stage-1-compatible item NDJSON from the live item registry and
recipe manager, plus a summary JSON with namespace counts, item tag
membership, block item ids, block tag membership, recipe counts, and
runtime-export notes. It intentionally does not invoke an LLM or any
network service.

The export should include, per item:

- registered item id *(implemented)*
- display name / translation key *(implemented)*
- owning namespace *(implemented)* and loaded mod metadata *(remaining)*
- stack size, durability, rarity, equipment-like components where
  accessible *(implemented)*; food components *(remaining)*
- whether it is a block item and block id where applicable *(implemented)*
- resolved item tags and direct item tags *(implemented as live resolved
  membership for both fields in v1)*
- resolved block tags for block items *(implemented)*
- recipes consuming and producing the item, grouped by recipe type
  *(implemented from `RecipeManager`)*
- fuels, compostables, tool requirements, and creative tab membership
  where accessible
- source markers showing whether the item/tag/recipe came from vanilla,
  mod resources, datapacks, KubeJS, or another script layer when the
  loader exposes that provenance

The summary should include:

- loaded mod list *(remaining)*
- jar hashes where available *(remaining)*
- datapack list *(remaining)*
- KubeJS script hashes *(remaining)*
- item count *(implemented)*
- recipe count *(implemented)*
- tag membership maps *(implemented)*
- export command/tool version *(implemented as `slot-runtime-export`)*
- world/pack identifier supplied by the operator or derived from the
  instance *(implemented)*

Offline command:

```sh
slot-classify generate-pack-layer \
  --runtime-export config/slot/classification/exports/<pack-id>.runtime-items.ndjson \
  --mods /path/to/instance \
  --datapack \
  --out out
```

Exit criteria:

- deterministic facets for KubeJS/custom items come from live game
  state, not static jar guesses
- public per-mod semantic data can still seed upstream mod items
- generated pack layer only carries pack-specific additions/corrections
  where possible, so it remains reviewable

### KubeJS Source Enrichment

KubeJS scripts can be useful LLM evidence, but only when scoped tightly.
Dumping whole `server_scripts` / `startup_scripts` files into prompts is
likely to add noise, leak unrelated pack logic, and consume context on
code the model cannot reliably execute.

Use script source as optional per-item evidence when the exporter can
derive a small, relevant slice:

- the script line/register call that creates `kubejs:<item>`
- recipe definitions that consume or produce the item
- tag additions/removals mentioning the item
- display-name/lore assignments
- event handlers that directly reference the item id
- nearby comments immediately attached to those statements

Do not include:

- whole script files
- unrelated quest/progression scripts
- large helper functions unless the item-specific call cannot be
  understood without a short helper name
- snippets whose only relationship is a broad namespace wildcard

Prompt policy:

- mark snippets as `pack_script_evidence`, not authoritative facts
- keep a per-item byte/line cap
- prefer deterministic runtime fields over script text when they
  disagree
- use source snippets mainly for semantic facets: `role`,
  `primary_uses`, `activity`, `mod_subsystem`, `origin`, and
  pack-specific `material_family` names
- write any model-inferred script semantics into the review queue when
  confidence is low or the source is ambiguous

Worth doing:

- yes for custom `kubejs:*` items and heavily repurposed upstream items
- yes when a recipe/tag alone does not explain why an item matters
- no for public-covered items whose live runtime facts only changed
  deterministic recipe/tag membership
- no as a default prompt expansion for every item in a pack

## Runtime Integration

SLOT runtime should not call any LLM backend.

Startup behavior:

1. load bundled vanilla and bundled popular-mod snapshot
2. load public/local cache layers if configured and hash-matched
3. load mod-shipped layers from loaded jars
4. run runtime crawl for deterministic facts from live registries
5. apply generated pack/runtime-export layers as `modpack` data
6. apply server/player overrides

If network-backed public database fetch exists, it should be explicit or
config-gated. Offline play must still work from bundled and cached data.

## Privacy And Policy

- never upload a user's mod list without an explicit command
- public lookup can be done by hashes and mod ids, but the command must
  be clear that it is contacting the public database
- never scrape CurseForge downloads
- only use official platform APIs for optional resolution
- if a platform does not expose a downloadable file for automated tools,
  report that the user must provide the jar locally

Reference constraints:

- Modrinth `.mrpack` files carry a `modrinth.index.json` manifest with
  file paths, hashes, environment flags, and download URLs:
  <https://support.modrinth.com/en/articles/8802351-modrinth-modpack-format-mrpack>
- Modrinth's API exposes project/version/file metadata suitable for
  hash- or version-id-based matching: <https://docs.modrinth.com/api/>
- CurseForge resolution must go through the official API and respect
  distribution/download metadata rather than scraping:
  <https://docs.curseforge.com/rest-api/>

## Slice Sequence

### Slice 1: Stabilize Existing Tooling

Goal:

- make the current manifest/source workflow safe enough to build on

Deliverables:

- validate numeric CLI args (`batch-size`, `concurrency`,
  `mod-concurrency`, retry thresholds)
- update help text so default OpenRouter/deepseek behavior and current
  mod support are accurate
- add tests for `modpack.ts`: manifest validation, source resolution,
  skip/already-classified/process decisions, malformed output handling
- validate existing complete files before treating them as reusable
- include tool/schema/prompt metadata in generated outputs
- update `sync-to-runtime.ts` to maintain `per-mod/index.json`

Exit criteria:

- bad CLI numeric input exits with an actionable error
- stale/malformed output cannot be mistaken for fresh coverage
- syncing a new mod layer makes it loadable by `FacetIndexBootstrap`

### Slice 2: Input Manifest V2

Goal:

- define one normalized input format for sources, jars, installed packs,
  and platform manifests

Deliverables:

- `input-manifest.v2.schema.json`
- generated manifest shape with mod id, display name, loader, version,
  source kind, paths, hashes, platform ids, namespaces, and cache key
- cache-key helper shared by scan/classify/sync/publish commands
- migration path from current hand-authored `modpacks/*.json`

Exit criteria:

- the pipeline can plan work from a generated manifest without knowing
  whether the original input was a source tree, jar, or pack file
- cache decisions are based on input identity and pipeline metadata

### Slice 3: Mods-Folder Scanner

Goal:

- make `scan --mods <folder>` useful before classification exists for
  jars

Deliverables:

- jar enumeration and hash computation
- metadata readers for Forge/NeoForge/Fabric mod descriptors
- local Prism/Packwiz `.index/*.pw.toml` metadata reader for platform
  ids when available
- namespace and item-candidate estimation from jar resources
- KubeJS script/data/asset footprint summary for installed instances
- skip heuristics for obvious libraries / client utilities / zero-item
  mods
- scan summary JSON and human terminal report

Exit criteria:

- pointing at a real `mods/` folder produces a useful coverage plan
- scanner never runs LLM calls or writes classification layers
- ambiguous or malformed jars fail closed with diagnostics

### Slice 4: Jar-Backed Stage 1

Goal:

- classify installed jars through stages 1 and 2 without requiring
  source repositories

Deliverables:

- jar resource reader
- singular/plural resource directory support
- item candidate merger with extractor provenance
- jar-backed bundle feeding existing deterministic rules
- tests using fixture jar directories or generated test zips

Exit criteria:

- `classify-folder --mods <folder> --stages 1,2` emits valid partial
  layers for content mods
- jars with no classifiable items are skipped explicitly
- extraction reports resource coverage per mod

Initial implementation note:

- The current `classify-folder` command scans a local mods folder or
  Prism instance root, skips covered/library/blocked entries by default,
  reads stage-1 records directly from jar resources, feeds the existing
  deterministic rules, and writes jar provenance into layer metadata.
  Stage 3 is available via `--stages 1,2,3`, but jar-backed runs only use
  cached `mod_subsystem` vocabularies because they do not have source
  README context.

### Slice 5: Public Database Manifest And Fetch

Goal:

- consume shared classification data before generating anything locally

Deliverables:

- `index.v1.json` database schema
- exact-match lookup by jar hash and platform ids
- item-set signature fallback
- public cache directory layout
- `fetch-public --mods <folder>` command
- scan status integration

Exit criteria:

- a known mod jar resolves to a public layer without local generation
- mismatched hashes do not reuse stale layers
- public fetch can be disabled and cached data remains usable offline

### Slice 6: Local Semantic Generation For Missing Mods

Goal:

- keep LLM generation available for contributors and private packs
  without making it the default player path

Deliverables:

- `generate-missing --mods <folder>`
- per-mod fixture/cache directories keyed by prompt + input identity
- review queues next to each output
- provenance fields filled for model/backend/provider/prompt/tool
- explicit cost/work estimate before stage 3 starts

Exit criteria:

- generation only runs for missing/stale semantic layers
- interrupted runs resume cleanly
- generated layers are marked `raw` until reviewed

### Slice 7: Running-Instance Export And Pack Layer

Goal:

- support KubeJS/datapack-heavy packs by exporting the actual loaded
  registry/tag/recipe state and turning it into a pack-specific layer

Deliverables:

- `/slot classification export` command *(initial Forge 1.20.1 +
  NeoForge 1.21.1 command implemented)*
- runtime item NDJSON export *(initial live registry/tag/recipe export
  implemented)*
- runtime summary JSON with item/tag/recipe counts *(implemented)* and
  mod/datapack/KubeJS provenance hashes *(remaining)*
- offline `generate-pack-layer --runtime-export` command *(implemented;
  supports static jar enrichment with `--mods` and datapack packaging with
  `--datapack`)*
- `/slot classification inspect` and `/slot classification rehome`
  commands *(implemented on Forge 1.20.1 + NeoForge 1.21.1)* for
  verifying loaded layers and recomputing classifier-owned homes against
  a played instance
- optional per-item KubeJS snippet extraction under strict byte/line caps
- tests for export shape using a small fake registry/recipe/tag fixture

Exit criteria:

- custom `kubejs:*` items can receive deterministic facets from live
  state
- pack-specific recipe/tag changes can override public per-mod
  deterministic facets
- LLM enrichment for script evidence is opt-in and reviewable
- runtime export does not require network access or an LLM backend

### Slice 8: Review, Diff, And Publish Candidate

Goal:

- make generated data safe to share

Deliverables:

- `report` command producing Markdown or HTML
- `review` filters for low confidence, ambiguous facets, role changes,
  fill-ins, and schema proposals
- `diff` command for old/new layer comparison
- `publish-candidate` command that assembles layer + report + provenance
  for public database review

Exit criteria:

- a maintainer can review a mod update without opening the full JSON by
  hand
- publication artifacts include enough evidence to accept/reject the
  layer

### Slice 9: Runtime Layer Loading Expansion

Goal:

- make SLOT consume public/local/mod-shipped layers and runtime crawl in
  the intended order

Deliverables:

- runtime loader for local public cache
- loader for mod-shipped classification resources
- deterministic runtime-crawl layer for live registry/tag/recipe facts
- merge semantics for the new layer order
- diagnostics for each loaded/skipped layer

Exit criteria:

- known mods use semantic shared data
- unknown mods get deterministic runtime facets
- modpack/server/player overrides can correct shared data
- no runtime path invokes LLM generation

## Open Questions

- Should public database fetch happen in-game, in the launcher/setup
  tool, or only through an external command?
- Should mod-shipped layers outrank public layers by default, or should
  exact public curated data outrank raw mod-shipped data?
- How much compatible-version fallback is acceptable for mods that keep a
  stable item set across patch releases?
- Should the first public database live in this repository, a separate
  repository, or a static release artifact?
- What review status is required before SLOT bundles a layer snapshot?
- How much KubeJS source should the export retain by default, given that
  script snippets may reveal private pack logic?
- Should running-instance exports be generated from client singleplayer,
  integrated server, dedicated server, or all three?

## Implemented Baseline And Next Work

The implemented baseline covers the original first milestone plus the
first running-instance pack-layer workflow:

- fix current tool safety
- define input manifest v2
- implement `scan --mods`
- implement jar-backed stages 1 and 2
- implement runtime export
- implement pack facet vocabulary generation including `mod_subsystem`
- implement `generate-pack-layer --runtime-export --mods --datapack`
- implement in-game inspect/rehome diagnostics

The next useful milestone is review/diff/publish tooling plus public
cache identity and distribution. Runtime-crawl remains valuable for
unknown items, but reviewed pack/mod layers are now the higher-value path
for large played packs.
