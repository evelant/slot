# Classification LLM Authoring Plan

Last updated: 2026-05-14

Status: active. This is the current plan for modpack classification authoring.
Older V1/stage-2/refactor narratives were moved to
[outdated/](outdated/) because they described a deterministic semantic
pre-curation model that is no longer the strategy.

## Current Strategy

The classification authoring pipeline is LLM-judgement-first:

1. Gather item and pack evidence.
2. Refine vocabulary with an LLM loop.
3. Classify items with that vocabulary as grounding input.
4. Accept valid model output.
5. Assemble the generated layer into bundled resources or a modpack datapack.

There is no production stage where deterministic code pre-fills semantic facet
values and then prevents the LLM from changing them. Pre-LLM code may normalize
records, deduplicate evidence, summarize repeated facts, and enforce JSON/schema
shape. It must not encode pack-specific semantic defaults, override model
judgements, or discard valid output because a rule guessed differently.

## Evidence Input

Evidence extraction should preserve meaning-bearing context:

- runtime item ids, display names, tags, components, stack traits, and recipe
  participation from `/slot classification export`
- static jar enrichment such as model parents, lang text, loot sources, guide
  pages, advancements, and mod metadata
- tooltip/lore text, KubeJS/datapack overlays, resource-pack lang overrides,
  Ponder/category labels, stack groups, quests, and guide prose where available
- recipe role summaries and station/category labels as raw evidence

Prompts should spend context on semantic detail, not provenance scaffolding. Keep
source handles compact and avoid flooding prompts with jar paths, file paths,
candidate scores, or long metadata blocks that do not help the model decide what
an item means to a player.

## Vocabulary Loop

Vocabulary generation is a synthesis loop, not a candidate picker.

Each round receives:

- the stable semantic evidence corpus
- the previous round's working vocabulary
- a fresh rotating sample of raw item records, currently sized around 1536 items
  for large packs
- facet descriptions, default values, and instructions not to duplicate or
  narrowly split existing values

The model may keep, add, rename, merge, or retire vocabulary values. Existing
values are sticky context, not authority. Context records and item samples are
evidence only; they are not allowed output ids.

Vocabulary values are facet-scoped simple values unless the value is a real
registry/resource id:

```text
cooking
mechanical_power
steelmaking
steelmaking#input
minecraft:furnace
```

Do not encode pack, mod, or evidence provenance into generated values. Use
metadata, labels, aliases, descriptions, and review notes for that.

`accepted`, `review`, and `rejected` are lifecycle states:

- `accepted`: usable vocabulary.
- `review`: usable vocabulary, with a watchlist/debugging flag.
- `rejected`: explicit block; do not render into classification prompts.

`review` does not mean "withhold until a human approves." We can review values
when playtesting shows a problem, but default operation is to trust usable model
output.

## Classification Pass

The classification prompt receives item data plus the usable vocabulary. The
vocabulary grounds naming consistency, but the LLM still makes the facet
decisions for each item.

The prompt should clearly say:

- prefer the supplied vocabulary values when they fit
- use judgement for every facet rather than only filling gaps
- assign `organization_group` whenever a sensible vocabulary value exists
- keep `organization_group` broad and player-facing because it affects homes
- put machine/workflow/query slices into workflow/search facets instead of
  inventing narrow wall sections
- surface useful missing vocabulary as proposals for a future loop

The pipeline accepts valid classification output. It may retry malformed or
missing-item responses and reject schema-invalid values, but it should not
semantically rewrite model judgements after parsing.

## Organization Groups

`organization_group` answers: "where would a human player expect this item to
live on a wall with a small number of broad sections?"

Good groups are broad, player-facing, and useful for many sibling items. Built-in
defaults should cover common stock families such as equipment, tools, ores/raw
stock, metal stock, wood, crops, seeds, plants, food, storage, lighting,
redstone, decorative blocks, building parts, organic materials, and ceramics or
molds. Pack-specific groups are appropriate when a pack has a broad family a
player would naturally separate, such as beekeeping, glass products, or
textiles.

Bad groups are query views, not homes:

- mod-name buckets like "Create items"
- source/provenance buckets like `pack:tfg/...`
- rock taxonomy or other narrow technical properties
- single-machine or single-workflow buckets
- material form/state buckets that split related stock across many sections

When bad groups appear, improve the prompt, vocabulary, evidence sample, or
feedback loop. Do not add post-LLM pack-specific keyword cleanup.

## Human Review

Human review is optional and focused. The review utility should show only the
model-generated vocabulary decisions with labels, descriptions, rationales,
aliases, examples, and review notes. It should not require a reviewer to walk
input provenance or candidate dumps.

Review can explicitly reject or rename values. Otherwise, `accepted` and
`review` values remain usable. For classification output, review/watchlists are
debugging and playtesting signals, not a second classifier.

## Assembly

After classification:

1. Validate the generated layer for schema shape and vocabulary references.
2. Package it as bundled base/per-mod resources or a modpack datapack under
   `data/slot/classification/layers/<pack>.json`.
3. Install the datapack or resources and verify with:

```text
/datapack list enabled
/slot classification status
/slot classification inspect <known_item>
/slot classification rehome
```

`organization_group` homing is temporarily disabled in the mod until a fresh
LLM-classified pack layer validates in playtesting. The layer should still carry
organization groups so re-enabling richer homing does not require another schema
change.

## Current Next Step

Continue with the current TFG vocabulary artifact, run a larger classification
canary using full usable vocabulary and larger batches, inspect proposals and
sample item quality, then run the full `classify-runtime-pack` pass and assemble
the resulting base mod resources plus datapack.
