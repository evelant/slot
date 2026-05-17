# Classification Facet Vocabulary Plan

> **Outdated historical plan.** This file was moved out of the active docs on
> 2026-05-14. It contains the older detailed vocabulary/stage-3 planning
> narrative and should not be used as current process guidance. Current
> classification authoring direction lives in
> [../classification-facet-vocabulary.md](../classification-facet-vocabulary.md).

Last updated: 2026-05-14

Status: active. The current strategy is LLM-judgement-first for both pack
vocabulary and item classification. Pre-LLM code gathers, normalizes,
deduplicates, and summarizes evidence; it does not pre-decide semantic facet
values, lock those decisions, or restrict the LLM to a deterministic stage-2
semantic layer. The pack workflow is now:

1. collect runtime/static/guide/quest/tooltip/recipe/item evidence
2. refine vocabulary with an LLM loop that feeds the previous vocabulary and a
   fresh rotating item sample back into the next round
3. classify items with the usable vocabulary as grounding input
4. accept valid LLM output as the classification layer
5. assemble the generated layer into bundled base/per-mod resources or a
   modpack datapack

`accepted`, `review`, and `rejected` are review states, not a hidden second
classifier. `accepted` and `review` values are usable by default; `review`
means "accepted, but keep this on a watchlist for prompt/vocabulary debugging."
Only explicitly rejected values are withheld. Likewise, classification output is
not overridden, overwritten, or discarded by post-LLM semantic rules. Bad or
surprising output should feed the next prompt/vocabulary/data iteration, not a
new pack-specific keyword deny list.

`organization_group` still needs extra prompt care because it directly affects
player homes. The model should see all useful context, including workflows,
machines, guide text, recipes, and item samples, but it should output only
human-sized storage groups: broad item/use buckets a player would actually
create. If it emits query-like groups, the fix is to improve the vocabulary
prompt, examples, item sample, or feedback loop. Technical helpers may rank,
budget, or summarize context before the prompt; they must not silently override
the synthesizer after it answers.

This plan owns the generic modpack-classification workflow: pack vocabulary,
evidence extraction, vocabulary-backed semantic facets, and pack-layer
regeneration. TerraFirmaGreg is the first deep validation fixture because it
stresses the system hard; it must not be baked into the tool's schema, prompts,
runtime, or default rules.

It does not define EMI recipe context or generic task UI; those live in
[ADR 0007](../../decisions/0007-emi-recipe-sidebar.md) and
[ambient-task-views.md](../ambient-task-views.md).

For the existing classification pipeline, see
[item-classification-v1.md](item-classification-v1.md) and
[../../design/classification/README.md](../../design/classification/README.md).
For classification database/distribution work, see
[classification-database.md](classification-database.md).

## Problem

The pack-layer generator can scan installed mods, merge runtime exports with
static jar evidence, call stage 3, and package a drop-in datapack. The missing
piece is a pack-aware semantic vocabulary that is stable before item batches
are classified.

Without that vocabulary:

- the LLM invents semantic labels batch-by-batch
- `mod_subsystem` gets overloaded with task context
- `organization_group` has no consistent source of truth
- rich recipe/tag facts stay as raw evidence instead of structured facets
- complex packs collapse many items into broad roles like Materials, Utility,
  or Miscellaneous

This is a generic modpack problem. Any deep pack can introduce named process
areas, quest-taught concepts, custom recipe families, datapack tags, and
cross-mod material chains. TerraFirmaGreg is only the current test case.

## Goals

- generate stable pack-specific facet vocabulary before item classification
- keep built-in organization homes explicit in the vocabulary artifact, then let
  the vocabulary pass discover pack-specific values for every other
  vocabulary-backed facet
- distinguish workflow/task context from mod-internal identity
- add structured domain facets where they generalize beyond one pack
- improve `organization_group`, `mod_subsystem`, `primary_uses`, and
  activity/use-case coverage
- distinguish raw recipe evidence (`processing_in`) from
  player-facing station/process context (`used_at`)
- use runtime exports, recipes, tags, item ids, display names, mod metadata,
  guide books, quests, and advancements as evidence
- produce concise watchlist/review artifacts for surprising vocabulary and
  low-confidence assignments without blocking usable model output by default
- validate generated pack layers with both offline coverage checks and runtime
  `/slot classification inspect` / `rehome` smoke tests

## Non-Goals

- no EMI goal-tab UI
- no recipe goal projection resolver
- no generic task-view UI
- no hidden autocrafting or inventory mutation
- no assumption that quest or guide-book item links are exhaustive membership
  lists
- no TerraFirmaGreg-specific hardcoding in shared schema, prompts, runtime, or
  default command behavior
- no sparse layer output until facet-level merge semantics exist; generated
  pack layers should remain complete per-item overlays

## Design Rules

1. **Pack vocabulary is generic; evidence is pack-specific.** The tool may read
   pack-specific facts, but the code should express them as data-backed
   evidence, not hardcoded namespace branches.
2. **Schema owns contracts; vocabulary owns semantic value sets.** The facet
   catalog defines shape, merge semantics, and runtime contracts. Semantic value
   sets are supplied by the vocabulary artifact, either derived exactly from
   authoritative data when the source is complete or generated by the LLM
   vocabulary loop. Do not hardcode pack-specific semantic defaults in
   TypeScript.
3. **Vocabulary is not membership.** A guide-book chapter or quest title can
   name a workflow. It does not prove every linked item belongs to that
   workflow.
4. **Workflow is not home assignment.** `workflow` answers "which task/process
   context can show this item?" `organization_group` answers "where should the
   wall auto-home this item?" A wall-home group should feel like one of roughly
   15-20 broad player sections, primarily by item type/role with use case or
   state only as secondary refinement. A workflow may imply a home only through
   an explicit, reviewable rule.
5. **`mod_subsystem` stays identity-oriented.** It labels the mod-internal
   system an item itself belongs to. Recipe participation alone must not assign
   `mod_subsystem`, and subsystem ids do not create main-wall sections.
6. **Raw recipe facts are not player-facing station context.** `processing_in`
   remains raw evidence about recipe participation. `used_at` /
   station-context facets should capture the player concept: the station,
   machine, tool, or surface where the item is used.
7. **Unknown facets fail closed.** The generator must not emit facets the parser
   drops. New facets require TypeScript registry, prompt, parser tests, docs,
   and any Java runtime accessors needed by consumers.
8. **Review output is for optional human attention.** The evidence artifact and
   prompt fixtures remain available for debugging, but the primary vocabulary
   review artifact should be a concise yes/no/rename surface: value id, label,
   description, rationale, examples, policy notes, and an editable human-review
   field. A `review` state does not block use by default. Do not make the model
   regurgitate source refs as a validation scheme.
9. **Semantic text is the main LLM input.** Tooltip/lore prose, Patchouli page
   bodies, FTB Quest text, advancement/lang text, KubeJS/datapack overlays, and
   mod descriptions should be preserved as prompt evidence. Do not compress
   them away into a handful of seed item ids before curation.
10. **Spend context on semantics, not provenance.** The target vocabulary model
   is cheap and has a large context window, but that space should carry item
   names, tooltips, recipes, guide prose, quest text, and other meaning-bearing
   snippets. Do not flood prompts with file paths, jar paths, support metadata,
   source refs, or candidate-scoring scaffolding.
11. **Vocabulary is iterative refinement.** Feed the previous working
   vocabulary into later rounds along with fresh item samples so the model can
   keep good values, add missing values, and correct weak ones. Existing values
   are intentionally sticky, but not authoritative; the model may rename,
   replace, or retire them when the evidence supports that decision.

## Pipeline Fit

Current pipeline:

1. Evidence extraction collects item records and pack-level semantic evidence
   from source trees, jars, runtime exports, guides, quests, tooltips, recipes,
   tags, lang files, and overlays.
2. The vocabulary loop asks the LLM to synthesize and refine usable values for
   every vocabulary-backed facet, using the previous working vocabulary plus a
   rotating item sample.
3. Item classification asks the LLM to assign facets for every item using the
   usable vocabulary as grounding input.
4. Validation checks JSON shape, schema compatibility, vocabulary references,
   and resource/datapack loadability. It is not a second semantic classifier.
5. Assembly writes the generated layer into bundled resources or a drop-in
   datapack.

There is no production stage where deterministic code pre-fills semantic facets
and tells the LLM not to change them. Exact facts can still be extracted as raw
evidence or diagnostic fields, but modpack semantic classification is authored
by the LLM.

## Vocabulary Model

### Vocabulary-Backed Facets

The vocabulary pass should not be limited to `workflow`. It should emit a
pack-specific vocabulary for most semantic facets that depend on player
interpretation:

```text
activity
workflow
workflow_role
used_at
food_category
food_use
preparation_state
material_process_stage
stock_profile
container_state
equipment_effect
protection_context
progression_stage
loadout_context
use_affordance
organization_group
mod_subsystem
```

Each vocabulary-backed facet needs:

- stable id values
- display labels and aliases for prompt/search/UI text
- short descriptions that tell stage 3 when to use the value
- short rationales and examples that let a human reviewer approve, reject, or
  rename the value
- whether values are built-in seeds, pack-proposed, or manually curated
- whether the value can influence auto-home, task views, search only, or review
  only

V1 should model pack-extensible semantic facets as vocabulary-backed closed
sets. The layer schema validates facet shape and value-id grammar; the
vocabulary artifact validates whether a value is accepted for the current pack.
This gives us closed-enum behavior during item classification without baking
pack-specific values into code. Code-closed enums remain appropriate for
structural facets that runtime code interprets directly: `role`, `equip_slot`,
`required_tool`, booleans, and possibly a small base `form` vocabulary.

### Value Grammar

Vocabulary-backed facets should use stable facet-scoped values, not display
labels or provenance prefixes. Labels and natural-language variants live in
`label` / `aliases`; provenance lives in evidence metadata.

Canonical value forms:

```text
<token>
<token_path>
<workflow_value>#<role_token>
<resource_namespace>:<token_path>
```

Where:

```text
token       = [a-z][a-z0-9_]*
token_path  = token(/token)*
role_token  = token
```

Examples:

```text
cooking
mining
mechanical_power
autocrafting
steelmaking
food_prep
casting#input
steelmaking#catalyst
minecraft:furnace
```

Rules:

- values are scoped by the facet that contains them
- use simple lower_snake values for synthesized semantic concepts
- use namespace-qualified values only when the value is a real registry/resource
  id, such as a station, status effect, multiblock, or biome id
- use `#<role_token>` only for scoped relationship facets such as
  `workflow_role`
- do not encode display names, spaces, hyphens, capitalization, or item ids into
  vocabulary values; keep those as labels, aliases, or review examples
- start strict; loosen the grammar only when a usable value cannot be
  represented cleanly with aliases or evidence metadata

### Provenance Policy

Do not encode ownership or source provenance into vocabulary values. The facet
already scopes the value, and review/evidence metadata records where it came
from. `mod_subsystem=kinetics` is clearer than a value that repeats the mod
namespace; the classifier can still inspect `mod_namespace`, source records, and
evidence to understand where the subsystem was observed.

### Pattern Validation Strategy

The value grammar and provenance policy above are the V1 starting point. During the
first implementation, run the context-record extractor against the current
deep-pack fixture and lint every proposed value:

1. Generate raw context records with labels, source evidence, suggested facet,
   and stable source handles. Do not treat those handles as proposed output
   vocabulary values.
2. Apply the proposed grammar and report rejected values, collisions, aliases,
   provenance conflicts, and generic catch-alls.
3. Manually inspect top usable values and top rejected/colliding values.
4. Add fixture tests for the grammar and normalization rules.
5. Tune thresholds and stop-word lists from the review report, not by hardcoding
   fixture namespaces into shared code.

### `activity`

`activity` should be vocabulary-backed and generated for the current pack.
Stage 3 should classify against the activity values rendered from the
vocabulary artifact, not a hardcoded prompt list.

Example activity values:

```text
mining
exploration
cooking
building
decorating
combat
farming
redstone
automation
logistics
storage_management
brewing
enchanting
magic
power_generation
transportation
```

Rules:

- generated activities should be broad player-facing activities,
  not just internal recipe type names
- recipe verbs can inform activities, but technical recipe IDs should stay in
  `processing_in` and player-facing stations/processes should go in `used_at`
  and `workflow`
- generic `crafting` should be avoided unless a concrete consumer needs a broad
  crafting-prep view; otherwise use workflow/station-specific values

### `workflow`

`workflow` is a pack/mod-specific multi-value facet for player-facing process
areas. Values are stable facet-scoped strings generated from a pack vocabulary
artifact. It answers "which process/task context can show this item?"

Examples from possible packs:

```text
casting
anvil_work
food_prep
greenhouse
ore_processing
steelmaking
ritual_setup
oxygen_production
```

Rules:

- use stable values; labels and aliases are search/display data
- prefer process names players learn from the pack, not internal class names
- do not create catch-alls such as `materials`, `misc`, or `crafting`
- a workflow can be broad enough to span roles: ingredients, tools, stations,
  containers, outputs, and byproducts can all participate
- recipe, station, guide, quest, advancement, tag, and sibling evidence are all
  allowed for `workflow`; this is intentionally different from `mod_subsystem`
- `workflow` alone does not auto-home an item; `organization_group` remains the
  direct wall-home signal

### `workflow_role`

The relationship between workflow and role must survive multi-workflow items.
Do not model it as an independent flat enum if an item can belong to two
workflows with different roles.

V1 should use scoped scalar values:

`workflow_role` is a `multi_free_text` facet with values like
`some_mod:casting#input` and `some_mod:casting#vessel`. This is directly
queryable from the existing flat facet model and still preserves per-workflow
role meaning for items that participate in multiple workflows.

Starter role tokens:

```text
input
intermediate
output
byproduct
tool
station
container
vessel
fuel
catalyst
mold
pattern
upgrade
structure_part
consumable_supply
```

Rules:

- the vocabulary pass may add pack-specific roles when a process needs them
- avoid synonym pairs such as `ingredient` and `input` unless the distinction is
  implemented and validated
- workflow roles should not replace `role`; they describe the item's
  relationship to a workflow, not the item's fundamental kind
- Slice 0 must define a value pattern that permits scoped role tokens such as
  `example:casting#input`; the current generic namespaced-token pattern is not
  sufficient for this facet

### `organization_group` And `mod_subsystem`

Both facets should be fed by the vocabulary artifact, but they answer different
questions:

- `organization_group`: player-facing wall-home value. It can be derived
  from workflow vocabulary only when the vocabulary marks a default home and
  item evidence supports membership. It is a scarce, broad main-wall home
  signal, not a general query facet; reject mod-name buckets, mod subsystem
  labels, rock taxonomy, stackable/pileable material properties, material
  form/state splits, workstation-specific processes, and tag variants that
  would fragment obvious siblings.
- `mod_subsystem`: identity-oriented mod subsystem. It should stay conservative
  and should not be assigned merely because an item is consumed by, produced by,
  or compatible with a process. It is semantic/query evidence, not a wall-home
  source.

Rules:

- prompt examples for these facets should be rendered from the vocabulary
  artifact, not hardcoded for any particular pack
- `organization_group` values may match workflow ids, but they are not
  synonymous with workflow membership
- built-in high-specificity sections such as Ores & Raw Stock, Metal Stock,
  Gems & Crystals, Dusts & Powders, Wood, Seeds, Crops, Plants,
  Ceramics & Molds, Organic Materials, Stairs, Slabs, Food, Tools, and Storage
  keep ownership of the main home; this means "already a good default home",
  not "bad grouping." Item containers should fall into Storage, lamps/light
  sources into Lighting, crops into Crops, pottery into Ceramics & Molds, and
  redstone components into Redstone. Vocabulary values for those distinctions
  should power search/filter/task views or within-section ordering unless
  playtest proves a broad wall-home bucket is better
- curation and item prompts should include the protected built-in section list
  and explicitly reject near-duplicates unless the built-in parent is overloaded
  and the value is a broad, player-obvious subset; `Wood` is protected for
  stock wood such as sticks, logs, planks, boards, and lumber; `Seeds`,
  `Crops`, `Plants`, `Ceramics & Molds`, and `Organic Materials` are protected stock
  sections for their obvious item families; pack-broad groups such as
  Beekeeping or Glass Products can be valid custom homes when they have enough
  siblings and do not merely rename a default; `Materials` is not protected
  because it is an overloaded runtime fallback that should split into a few
  useful broad storage sections in large packs
- `mod_subsystem` vocabulary should be generated by the same artifact pipeline
  so task-like concepts can move to `workflow` / `used_at` instead of
  overloading subsystem identity

### Food Domain Facets

Food classification should be generic. The first implementation may include
rules for whichever pack is under test, but the facet vocabulary should not be
named for that pack. These facets should be vocabulary-backed because packs
often add food systems that vanilla does not model.

Candidate facets:

```text
food_category:
  fruit
  vegetable
  grain
  flour
  dough
  bread
  meat
  fish
  dairy
  cheese
  egg
  spice
  sweetener
  fat_oil
  sauce
  prepared_meal
  preserve
  drink
  bowl

preparation_state:
  raw
  cooked
  dried
  salted
  pickled
  fermented
  curdled
  flour
  dough
  preserved
  unsealed
  sealed

food_use:
  eat_now
  ingredient
  meal_component
  drink
  preserve
  animal_feed
  buff_food
  cooking_fat
  sweetener
  spice
```

Rules:

- item/food tags can be exact evidence for these facets when tag names are
  explicit
- animal-feed, composting, and storage-helper tags should not automatically
  imply player cooking membership
- foods can carry both food facets and workflow membership when the pack has a
  named food-preparation process
- `food_use` answers why the player cares about the item in food workflows; it
  is allowed on tools/containers when the food use is obvious and useful

### Material And Process Domain Facets

Material/process chains need structured stages, but their vocabularies are
pack-varying. The tool must not carry a universal list of stages such as
`double_ingot`, `bloom`, `billet`, or `molten`; those are real in some packs and
noise in others. The vocabulary loop synthesizes these values from item samples,
tags, recipes, tooltips, quests, guides, and lang text.

Vocabulary-backed material/process facets:

```text
material_family:
  iron
  wood_oak
  black_steel

material_secondary:
  wood_oak
  leather

tier:
  bronze
  steel
  high_voltage

required_tool_tier:
  iron
  steel

material_process_stage:
  [synthesize broad process forms/stages from the pack]
```

Decision: do not add a separate `process_material` facet for V1. Use
`material_family` / `material_secondary` for the item substance, and combine
`material_process_stage`, `workflow`, `workflow_role`, and `used_at` for process
context when a mold, catalyst, fuel, container, machine, or alloy relationship
matters.

### Station And Interaction Facets

`processing_in` remains useful raw evidence: it records recipe
types where an item appears as an input. It does not answer the player-facing
question "where do I use this?" because recipe type IDs can be technical,
cross-mod, or too broad, and they do not reliably distinguish consumed inputs,
stations, catalysts, containers, or reusable tools.

Add vocabulary-backed station/interaction facets:

```text
used_at:
  example:anvil
  example:oven
  example:loom
  tech_mod:assembler
  tech_mod:chemical_reactor

use_affordance:
  place
  eat
  drink
  equip
  fuel
  repair
  configure
  cast
  fill
  empty
  preserve
  harvest
  scan
  launch

loadout_context:
  mining_run
  cave_run
  farming_run
  building_project
  exploration_trip
  combat_trip
  base_maintenance
  machine_setup
```

Rules:

- `used_at` values should come from recipe categories, EMI/JEI category labels,
  guide/quest text, and repeated station item evidence
- the same `used_at` value can apply to a station item, its inputs, its
  reusable tools, and its outputs when that helps a player collect the process
  context
- `use_affordance` captures direct interaction verbs; do not use it as a
  generic recipe-membership marker
- `loadout_context` is allowed even before a dedicated UI exists because it can
  power search, kits, future task views, and review diagnostics

### Inventory Behavior Facets

Inventory views need to know how players handle quantities and containers, not
just what an item is.

```text
stock_profile:
  bulk
  small_batch
  singleton
  tooling
  reserve
  display
  overflow

container_state:
  empty_container
  filled_container
  fluid_container
  gas_container
  energy_container
  reusable_mold
  single_use_mold
  pattern_template
  has_contents
  accepts_contents
```

Rules:

- `stock_profile` is distinct from `carry_frequency`: it describes desired
  stock shape, not how often the item lives in pockets
- `container_state` should capture inventory behavior and player expectation,
  not just Java item class or NBT capability
- packs can extend both facets when they introduce meaningful container or stock
  concepts

### Equipment And Progression Facets

Equipment effects and progression gates matter for task views and inventory
readiness.

```text
equipment_effect:
  night_vision
  water_breathing
  oxygen_supply
  flight
  step_assist
  speed_boost
  reach_boost
  tool_mode

protection_context:
  heat
  cold
  radiation
  vacuum
  pressure
  fire
  poison
  fall
  magic

progression_stage:
  example:early_survival
  example:metal_age
  tech_mod:low_voltage
  tech_mod:high_voltage
  space_mod:moon
```

Rules:

- `equipment_effect` should describe the player-visible effect granted by
  carrying, wearing, or using the item
- `protection_context` should describe hazards the item protects against or is
  designed to operate within
- `progression_stage` must be vocabulary-backed; there is no universal stage
  ladder across packs
- quest/guide chapters can name stages, but item assignment still needs item
  evidence

## Evidence Model

The vocabulary pass needs pack-level evidence records, not just item records.
Each adapter should emit normalized evidence:

```json
{
  "kind": "recipe_type",
  "id": "example:casting",
  "label": "Casting",
  "namespace": "example",
  "source": "runtime-summary",
  "item_refs": ["example:ingot_mold"],
  "recipe_refs": ["example:metal/cast_ingot"],
  "confidence": 0.85
}
```

Evidence kinds:

- `runtime_item`: id, display name, tags, components, recipe roles
- `recipe_type`: recipe type id, counts, examples, owning namespace
- `recipe_role_summary`: compact role facts for an item in a recipe family:
  input/output/tool/station/catalyst/container/remainder/byproduct, examples of
  co-ingredients and outputs, and whether the item appears consumed or reused
- `recipe_id_family`: repeated recipe-id path prefixes
- `item_tag`: tag id, member count, examples, whether direct membership is
  known
- `block_tag`: block tag id, member count, examples
- `guide_page`: guide-book category/entry/page title, item links, recipe links
- `quest_node`: quest chapter/title/tasks/rewards/dependencies/icons
- `advancement`: title, description, criteria item predicates
- `mod_metadata`: display name, description, README/manifest snippets
- `existing_vocab`: prior facet vocabulary, `mod_subsystem`, workflow, or
  curated values

Adapter rules:

- each adapter is optional; missing Patchouli/quest data must not block the
  pipeline
- adapters should preserve source file/resource ids, not long prose blobs
- guide/quest/advancement text is vocabulary evidence first and membership
  evidence second
- evidence extraction must work from installed packs where possible, not only
  cloned source repos
- recipe evidence should include player-facing labels when available from
  runtime integrations such as EMI/JEI/category metadata, while preserving raw
  recipe type ids for audit

## Vocabulary Generation Contract

The vocabulary command is the highest-leverage step in this plan. Its output is
not item classification yet; it is the grounding vocabulary stage 3 should
prefer for consistent names. If this step is noisy, too sparse, or unstable,
every downstream facet gets worse.

The command should be implemented as a pipeline, not one monolithic prompt:

1. **Collect semantic context mechanically.** Mine evidence records for
   useful signal: recipe type labels, recipe-id path families, repeated
   station/item tokens, tag names, guide/quest/advancement titles, mod metadata,
   existing vocabulary, previous generated layers, and prior `primary_uses`
   phrases when available. These records are source handles and
   evidence bundles, not proposed vocabulary values.
2. **Build pack-overview summaries and rotating item samples.** Include
   compact raw runtime item observations so the model sees the shape of the
   item universe: item ids/names, raw tags, creative tabs, components, model
   parents, recipe-role facts, and tooltip/lore snippets. For
   `organization_group`, also include pack-wide context such as default-section
   pressure, runtime item family clusters, tag membership summaries,
   recipe-use neighborhoods, and human-visible text pools. These summaries and
   item samples are context only; they must not be converted one-for-one into
   values or treated as deterministic semantic classifications.
3. **Normalize and cluster context records.** Lowercase/snake-case labels,
   keep namespace/pack details in source handles and metadata, merge obvious
   aliases, reject generic catch-alls, and group context records by target facet
   before any LLM call.
4. **Ask the LLM to synthesize values from context.** The prompt receives
   compact context records, pack-overview summaries, semantic snippets,
   examples, and facet policy. These records are not an allowed-value list.
   `context_id` values are source handles; the model should output a value only
   after deciding it is the best stable vocabulary value, and should synthesize
   clearer values when the evidence points at a broader or more human concept.
   Pack-specific storage families should use concise facet-scoped values such
   as `beekeeping`, not provenance-prefixed ids.
   For `organization_group`, the desired output is a small review shortlist
   when broad pack-specific storage families are evident; an empty result is
   correct only when protected built-ins already cover every useful family.
5. **Apply mechanical validation and watchlist annotation.** Validate ids,
   value patterns, duplicate/synonym conflicts, maximum count guidelines, and
   loadability constraints. These checks may mark values for review or reject
   malformed values, but they must not replace the LLM's semantic judgement with
   pack-specific rules. `accepted` and `review` values become classifier
   vocabulary; explicitly `rejected` values do not.
6. **Emit review artifacts.** Preserve only the synthesized value decisions a
   maintainer needs to act on, plus explicit harmful or previous values that
   should be rejected. Do not bulk-render every ordinary noisy context record as
   a rejected value, and do not include full candidate dumps or raw LLM
   responses in the primary review JSON.
7. **Iterate automatically before optional human review.** Run several
   refinement rounds with the previous round's working vocabulary plus a fresh
   item sample. The working vocabulary carries `accepted`, `review`, and
   `rejected` generator decisions forward as context for the next round.
8. **Apply human decisions only when useful.** Maintainers may edit
   `human_review` fields in the final review JSON to approve, reject, or rename
   proposed values, or run the interactive `review-pack-facet-vocabulary`
   helper to make y/n decisions from the terminal. Skipping review does not make
   `review` values unusable; they remain grounded vocabulary with a watchlist
   marker.

### Value State

Every generated value should have one lifecycle state:

```text
accepted
review
rejected
```

`accepted` and `review` values are rendered into classification prompts.
`review` values are usable by default and remain visible to maintainers as a
watchlist. `rejected` is reserved for previous values that should be retired,
malformed values, or specific harmful ids that need a visible block; ordinary
noisy context records are omitted from the review output.

Every generated value should also carry an origin:

```text
built_in
pack_generated
namespace_generated
manual
previous
stage3_proposed
```

### Facet Generation Policy

The vocabulary command needs per-facet policy because "generate values" means
different things for each facet.

| Facet | Value source | Acceptance bias |
| --- | --- | --- |
| `activity` | generated pack activity concepts from guide/quest/process/item evidence | conservative; prefer broad player activities, reject recipe-internal verbs |
| `workflow` | recipe families, station groups, guide/quest chapters, repeated process language | medium; process values are useful even before auto-home consumes them |
| `workflow_role` | derived from accepted workflows plus observed recipe roles | conservative; scoped values only, no free-floating roles |
| `used_at` | station items, recipe category labels, EMI/JEI labels, guide text | medium; must describe a player-facing station/surface, not raw recipe ownership |
| `food_category` / `food_use` / `preparation_state` | food tags, components, names, recipes, guide text | medium; explicit food evidence required |
| `material_process_stage` | ids, tags, recipe families, forms, item siblings | conservative; avoid generic "material" restatements |
| `stock_profile` | stack size, role, recipe degree, storage semantics, player-use phrases | conservative; prefer defaults unless evidence is clear |
| `container_state` | component/NBT hints, ids, tags, recipe roles, reusable/remainder evidence | medium; container behavior matters for views |
| `equipment_effect` / `protection_context` | components, lore, tags, guide text, equipment slot evidence | medium; player-visible effect/hazard required |
| `progression_stage` | quest/guide chapters, tiers, voltage/age systems, dimension gates | conservative; always vocabulary-backed, never universalized |
| `loadout_context` | activity vocabulary, guide/quest text, common trip/task phrases, prior uses | medium; useful for kits/search even before dedicated UI |
| `use_affordance` | item action semantics, components, names, tags, station roles | conservative; direct interaction verbs only |
| `organization_group` | broad human storage values synthesized from semantic item/document evidence, stack groups, and curated/manual values | conservative; direct auto-home impact |
| `mod_subsystem` | mod metadata and identity-owned functional systems | conservative; not recipe participation |

### Evidence Ranking

When evidence conflicts, rank it roughly:

1. Manual or previous working vocabulary.
2. Explicit guide/quest/advancement labels that name a player-facing concept.
3. Runtime recipe category labels and station/machine identities.
4. Direct static tags when direct membership is known.
5. Repeated recipe-id families and runtime resolved tags.
6. Prior `primary_uses` phrases and item-name token clusters.
7. Raw namespace/mod metadata with no item-level support.

Lower-ranked evidence can propose a value, but should usually leave it in
`review` unless multiple independent signals agree.

### Stability Rules

- Prefer preserving a previous usable value over renaming it for style.
- If two proposed values are synonyms, accept one value and record aliases on it.
- Render built-in organization homes into the vocabulary-generation prompt as
  protected usable values. The LLM should not propose synonyms or narrow
  splits of those homes; add a new value only when it is a genuinely distinct
  reusable concept.
- Do not encode provenance in the value. Keep pack/mod/source ownership in
  metadata, evidence, and review notes.
- Reject generic values such as `misc`, `general`, `materials`, `components`,
  `crafting`, `items`, and `blocks` unless a facet policy explicitly allows the
  value and a consumer needs it.
- Add a value-count warning when a facet explodes into too many low-evidence
  values; this usually means the context extractor is leaking item families
  into semantic vocabulary.

## Pack Vocabulary Artifact

Output path: `out/<pack>.facet-vocabulary.json`.

Review output path: `out/<pack>.facet-vocabulary.review.json`.

Proposed shape:

```json
{
  "schema_version": 1,
  "kind": "slot-pack-facet-vocabulary",
  "pack_id": "example-pack",
  "generated_by": "slot-classify v0.1.0",
  "generated_at": "2026-05-11T00:00:00Z",
  "source": {
    "runtime_items": "out/example.runtime-items.ndjson",
    "runtime_summary": "out/example.runtime-summary.json",
    "mods_path": "/path/to/instance/mods"
  },
  "facets": {
    "activity": {
      "values": {
        "cooking": {
          "label": "Cooking",
          "origin": "pack_generated",
          "aliases": ["food prep"],
          "state": "accepted",
          "confidence": 0.9
        }
      }
    },
    "workflow": {
      "values": {
        "example:casting": {
          "label": "Casting",
          "origin": "pack_generated",
          "aliases": ["metal casting", "molds"],
          "state": "accepted",
          "evidence": [
            {"kind": "recipe_type", "id": "example:casting", "confidence": 0.9},
            {"kind": "item_tag", "id": "example:ingot_molds", "confidence": 0.8}
          ],
          "seed_items": ["example:ingot_mold"],
          "related_activity": ["automation"],
          "default_organization_group": "ceramics_molds",
          "confidence": 0.9
        }
      }
    },
    "workflow_role": {
      "values": {
        "example:casting#input": {
          "label": "Casting input",
          "origin": "pack_generated",
          "parent": "example:casting",
          "state": "accepted",
          "confidence": 0.85
        }
      }
    }
  }
}
```

Review artifact requirements:

- rejected or uncertain facet values
- possible duplicates/synonyms
- low-evidence values
- values whose id namespace differs from most seed items
- vocabulary changes compared with a previous run
- values proposed by stage 3 that were not in the vocabulary it received

## Item Facet Pass

The item classifier consumes the vocabulary artifact and assigns:

- existing facets: `role`, `primary_uses`, `activity`, `mod_subsystem`,
  `organization_group`
- new vocabulary-backed facets selected in Slice 0: `workflow`,
  `workflow_role`, `used_at`, `food_use`, `stock_profile`, `container_state`,
  `equipment_effect`, `protection_context`, `progression_stage`,
  `loadout_context`, `use_affordance`, `material_family`,
  `material_secondary`, `tier`, `required_tool_tier`,
  `material_process_stage`, `produces_effect`, `multiblock_component_of`, and
  `biome`
- exact raw/reference facts that truly do not require semantic judgement, such
  as namespace, stackability, block-item status, recipe participation, and
  stable enum facts

Prompt rules:

- render the usable facet vocabulary (`accepted` and `review`) for concepts
  present in the batch; built-in organization homes are included because they
  are vocabulary values, not because they are hardcoded outside the artifact
- instruct the LLM to prefer known vocabulary values for consistency, but to use
  judgement when no value fits and surface useful missing values as proposals
  for the next vocabulary loop
- keep `mod_subsystem` conservative and identity-oriented; do not auto-home by
  subsystem id
- allow `workflow`, `workflow_role`, and `used_at` from recipe/guide/quest
  evidence when the item has a real process role
- ask the LLM to turn rich `primary_uses`-style judgments into structured
  facets in the same pass instead of leaving all semantics as free text
- emit `organization_group` for every item where a sensible vocabulary value is
  available, with strong preference for broad default groups and only broad
  pack-specific groups when they are clearly better than a default home; groups
  too narrow to deserve one of the pack's scarce main-wall sections should be
  proposed as search/workflow vocabulary instead of home vocabulary
- keep `processing_in` visible as evidence, but do not treat it as a substitute
  for `used_at`

Runtime rules:

- `workflow` does not automatically create a wall section
- `organization_group` is the intended direct auto-home facet, but runtime
  group homing is temporarily disabled until a fresh LLM-classified pack layer
  validates in playtesting
- task/search/kit/workflow UI should consume semantic facets (`workflow`,
  `used_at`, `loadout_context`, `progression_stage`, etc.), not infer tasks from
  `organization_group`
- generated datapacks must remain complete enough that current whole-entry
  layer merge semantics do not erase lower-layer facets

## Current Handoff

The vocabulary-generation and stage-3 integration plumbing is in place. The
toolchain now supports:

- vocabulary-backed semantic facets with scoped value-id validation
- `collect-pack-facet-evidence` for runtime/static/guide/quest/advancement
  evidence plus Ponder/category lang, KubeJS client tooltips, stack groups, and
  zipped resource-pack lang overrides
- `refine-pack-facet-vocabulary` with large semantic prompts, rotating item
  samples, fixture record/replay, accepted/review/rejected output, and validated
  vocabulary artifacts
- `--facet-vocabulary` on stage-3 pack runs so prompts contain the usable pack
  vocabulary for vocabulary-backed facets
- conservative per-item `document_context` from evidence artifacts
- response validators that retry malformed or missing-item outputs and surface
  missing-vocabulary proposals without overwriting valid model judgements
- metadata recording the evidence/vocabulary files used for stage 3

The next slice is quality validation of the new strategy: continue a few
vocabulary refinement loops with larger rotating item samples, run a broader
classification canary with full usable vocabulary, inspect role quality,
watchlist/proposal output, prompt fixtures, warnings, and runtime
`/slot classification inspect` output, then move to a full
`classify-runtime-pack` run. The Stage 3 prompt should repeat the important
judgement and vocabulary-grounding rules at the end of the user message so they
sit immediately next to the batch evidence.

## Implementation Slices

### Slice 0: Contract And Schema Decision

Status: implemented 2026-05-11 for the TypeScript toolchain. Java runtime
continues to intentionally consume only existing runtime facets until a later
UI/runtime consumer needs the new semantic facets.

- implement vocabulary-backed closed enums for most semantic facets: layer
  schema validates the value-id grammar, and the pack vocabulary artifact
  validates the accepted value set
- keep only structural runtime-interpreted facets code-closed unless a concrete
  consumer needs dynamic values
- adopt scoped scalar `workflow_role` values for V1
- choose the cleanest schema-version path; backward compatibility is not a
  constraint for the current experimental/test consumers
- add new facet definitions to `tools/classification/src/schema/facets.ts`
- add the accepted facet set: `material_family`, `material_secondary`, `tier`,
  `required_tool_tier`, `workflow`, `workflow_role`, `used_at`,
  `food_category`, `food_use`, `preparation_state`,
  `material_process_stage`, `stock_profile`, `container_state`,
  `equipment_effect`, `protection_context`, `progression_stage`,
  `loadout_context`, `use_affordance`, `produces_effect`,
  `multiblock_component_of`, `biome`
- update prompt target facets and expected output examples
- update parser tests so unknown facets are rejected and new facets are
  accepted
- add validation for vocabulary-backed values: grammar validation in layer
  schema, value-set validation against the pack vocabulary artifact
- add Java `FacetIndex` accessors only for facets needed by runtime consumers
- update classification docs with the new facet contracts

Exit criteria:

- a tiny hand-authored layer containing the new facets validates and parses
- a tiny hand-authored vocabulary artifact validates and rejects malformed facet
  values
- grammar tests cover facet-scoped values, real namespace/resource values, and
  scoped `#role` values while rejecting artificial provenance prefixes
- stage 3 can emit the new facets without parser warnings
- Java runtime either exposes or intentionally ignores each new facet
- no command emits a facet that current tooling drops

### Slice 1: Pack Evidence Assembly

Status: implemented 2026-05-11 for the TypeScript toolchain. The command
`collect-pack-facet-evidence` writes `out/<pack>.facet-evidence.json` from a
runtime export, optional static jar enrichment, optional installed-pack guide /
quest / advancement adapters, Ponder/category lang text, KubeJS client tooltip
mappings, stack groups, zipped resource-pack lang overrides, and adapter
diagnostics. Runtime recipe evidence is compacted into recipe-type,
recipe-role, recipe-family, item-tag, and block-tag records with bounded
examples.

- add an evidence schema under `tools/classification`
- collect runtime-export summary evidence already available today
- collect static jar/source evidence already available today
- add compact `recipe_role_summary` evidence so stage 3 sees station/tool/input
  relationships without reading full recipe graphs
- add station/category label extraction where available from runtime recipe UI
  integrations
- add optional guide-book, quest, and advancement adapters as normalized
  evidence sources
- add optional Ponder/category lang, KubeJS client tooltip, stack group, and
  zipped resource-pack lang evidence sources
- write `out/<pack>.facet-evidence.json`
- include adapter diagnostics: missing source, parse failure, unsupported
  format, record count

Exit criteria:

- the command works on a pack with no guide/quest data
- the command works on a pack with guide/quest/advancement data when present
- output evidence records carry source ids and bounded examples
- evidence extraction has tests for at least one synthetic guide-book record,
  one quest record, one advancement record, one recipe type, one recipe role
  summary, one station/category label, one tag, one Ponder/lang record, one
  KubeJS tooltip mapping, one stack group, and one zipped resource-pack lang
  override

### Slice 2: Pack Facet Vocabulary Command

Status: implemented 2026-05-11 for the TypeScript toolchain and revised
2026-05-14 for iterative LLM refinement. The command family consumes pack
evidence, previous working vocabulary, and rotating raw item samples; builds
ranked context records and pack-wide summaries; writes dry-run prompt pairs;
keeps facets together when they fit the prompt budget; reuses the shared LLM
replay/recording clients; asks the LLM to synthesize values from context rather
than choose from precomputed ids; validates shape/loadability; and emits
`facet-vocabulary.json` plus `facet-vocabulary.review.json`. Stage 3 consumes
the usable artifact through the Slice 3 `--facet-vocabulary` path.

- add `refine-pack-facet-vocabulary`
- reuse the existing LLM client, split-prompt, fixture recording, and replay
  infrastructure
- consume `facet-evidence.json` plus optional existing vocabulary
- implement context extraction, normalization, alias clustering, LLM synthesis,
  mechanical validation, and watchlist annotation as separate functions
- model value lifecycle as `accepted` / `review` / `rejected`
- emit `out/<pack>.facet-vocabulary.json`
- emit `out/<pack>.facet-vocabulary.review.json`
- add `--dry-run`, `--force`, `--namespace`, `--min-evidence`, and
  `--previous-vocabulary` options
- add `--facet <facet>` for regenerating one facet vocabulary during prompt
  iteration

Exit criteria:

- dry run writes prompt pairs and a prompt summary
- replay can regenerate from fixtures without live LLM calls
- usable vocabulary values are stable, facet-scoped, and evidence-backed
- initial evidence thresholds are prompt-budgeting heuristics and are surfaced
  in the review report for tuning
- uncertain values are included as `review` watchlist values unless malformed or
  explicitly rejected
- duplicate/synonym values are visible in review output
- built-in seeds and pack-generated values are distinguishable in output
- replay fixtures prove the same evidence + previous vocabulary produces stable
  usable values
- stage-3-proposed values from a prior run can be fed back as review evidence
  and become usable vocabulary unless explicitly rejected

### Slice 3: Stage 3 Vocabulary Integration

Status: implemented 2026-05-11 for the TypeScript toolchain, with one
deliberate scope change: `classify-runtime-pack` does **not** silently run
vocabulary refinement when no vocabulary file is supplied. Baseline pack runs
should pass an explicit vocabulary artifact so expensive/biasing generation is
visible and reviewable.

- feed conservative per-item `document_context` into stage 3 from the
  `facet-evidence.json` guide/advancement records; keep current quest SNBT out
  until the quest adapter is local enough for item classification
- add a `--facet-vocabulary` option to `classify-runtime-pack` and
  `generate-pack-layer`
- require explicit vocabulary input for baseline pack runs; do not auto-run
  vocabulary proposal inside `classify-runtime-pack` yet
- render usable vocabulary-backed facet values into the stage-3 system prompt
- replace hardcoded facet examples in the shared prompt with vocabulary-rendered
  examples wherever practical
- add response validation, parse/merge warnings, and proposal reporting for
  useful missing values not in the usable vocabulary
- write metadata linking the generated layer to the facet vocabulary file

Exit criteria:

- stage 3 batches receive the relevant usable facet vocabulary
- layer metadata records the facet vocabulary source
- out-of-vocabulary suggestions become watchlist/proposal output for the next
  vocabulary loop
- stage 3 receives `accepted` and `review` vocabulary values, but never
  `rejected` values
- `mod_subsystem` uses the normal usable pack vocabulary values instead of a
  separate runtime proposer/cache

### Slice 4: Reference-Derived Diagnostic Facts

- implement generic food-tag mapping rules
- do not implement material-family, tier, or process-stage mapping rules from
  ids/tags/recipe families; those are vocabulary-backed semantic facets
- retain raw `processing_in` recipe facts plus semantic `used_at` evidence
  generation; do not collapse one into the other
- keep pack-specific mappings data-driven where possible
- avoid over-broad rules that turn helper tags into player-facing facets

Exit criteria:

- exact/reference extraction helpers have focused unit tests
- food facets are populated only from explicit food evidence
- material/process/tier facets are grounded by usable vocabulary and LLM
  judgement, not hardcoded defaults or pack-specific rule tables
- `used_at` assignments are produced only from station/category evidence or
  LLM-reviewed vocabulary evidence, not from arbitrary recipe ownership
- broad animal-feed, helper, blacklist/whitelist, and recipe-viewer tags do not
  imply player workflow membership

### Slice 5: Generic Validation Harness

- add coverage reports by namespace and facet
- add vocabulary coverage reports by facet value, source, confidence band, and
  review state
- compare before/after for broad role buckets, Miscellaneous fallback, and
  low-confidence facets
- sample inspect output for high-value vocabulary-backed facets and random
  low-confidence items
- validate final layer and datapack layer with `bun run src/cli.ts validate`
- verify generated datapack load on both NeoForge and Forge where practical
- inspect processed jar/resource output so classification resources actually
  ship in each loader

Exit criteria:

- report includes namespace totals, facet coverage, vocabulary-backed value
  coverage, workflow coverage, and top generic buckets
- report flags regressions where generic buckets grow unexpectedly
- report lists items with no LLM-authored facets after repair
- runtime smoke test confirms `inspect` shows expected facets and `rehome`
  does not push most items into generic groups

### Slice 6: Deep-Pack Validation Fixture

- run the full workflow against a deep real pack as a validation fixture
- do not hardcode fixture namespaces in shared logic
- keep fixture-specific expected samples in test data or review docs
- publish the generated datapack only after coverage and runtime checks pass

Initial fixture acceptance criteria:

- facet vocabulary has stable, evidence-backed values across several major
  namespaces and multiple facet types
- workflow, station, food, stock, container, equipment/protection, progression,
  loadout, use-affordance, and material/domain facets have non-zero coverage in
  expected process families
- `organization_group` appears where it improves home placement and stays
  absent where universal sections are better
- old `mod_subsystem` gaps no longer block player-facing projections
- auto-home data improves instead of pushing most items into generic groups

## Validation Matrix

Run these checks before trusting a generated pack layer:

- `bun run src/cli.ts validate <layer>`
- `bun run src/cli.ts validate <datapack-layer>`
- coverage report: namespace by facet
- coverage report: vocabulary-backed facet value by count, source, and
  confidence band
- review report: unknown/proposed facet ids
- review report: out-of-vocabulary value suggestions
- review report: low-confidence vocabulary-backed assignments
- before/after report: broad role bucket counts
- before/after report: Miscellaneous fallback count
- runtime: `/slot classification inspect <known workflow item>`
- runtime: `/slot classification inspect <generic item that should stay generic>`
- runtime: `/slot classification rehome`
- loader packaging: verify bundled/common classification resources and datapack
  layers are present for both active loaders

## Non-Blocking Tuning Questions

- What generic coverage threshold is good enough before publishing a generated
  pack layer?
- What initial evidence thresholds should each facet use before the validation
  harness has enough data to tune them?
- Which specific conflict cases should override the default evidence ranking
  after the first review artifacts expose real disagreement patterns?
