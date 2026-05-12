# Classification Pipeline Refactor Plan

Last updated: 2026-05-12

Status: active, implementation gate passed. Slices 0 through 4 are implemented:
the vocabulary generator is split into focused modules, `organization_group`
and `mod_subsystem` candidate logic are isolated, Stage 3 has a
`vocabulary_proposals` review path, deterministic ore/log gaps found by the
canary are patched, and the mixed Stage 3 canaries validate. The remaining
decision is operational: refresh/regenerate the accepted vocabulary once more
so new generic defaults such as `slot:open` are present, then run the full
`classify-runtime-pack`, or run full classification against the current policy5
vocabulary and accept that missing values will be omitted/reviewed. The
existing vocabulary feature plan remains the design reference for why pack
vocabulary matters; this plan is the implementation cleanup path.

Related docs:

- [classification-facet-vocabulary.md](classification-facet-vocabulary.md) -
  vocabulary-backed semantic facet design and validation history
- [item-classification.md](item-classification.md) - original classification
  pipeline and runtime export plan
- [../design/classification/README.md](../design/classification/README.md) -
  classification data layout and runtime consumption
- [../../tools/classification/README.md](../../tools/classification/README.md) -
  TypeScript authoring tool guide

## Problem

The pack-classification pipeline has the right broad shape:

1. extract static pack data from mod jars, manifests, lang files, recipes,
   tags, guide text, quests, advancements, and datapack/resource-pack overlays
2. merge runtime export data from `/slot classification export`, including
   client-only and KubeJS-mutated data such as tooltips and stack groups
3. preprocess that evidence into rich semantic input for vocabulary generation
4. ask an LLM to derive a pack-specific vocabulary for semantic facets
5. use the accepted vocabulary plus item evidence to classify individual items
6. emit validated mod resources / datapack layers for SLOT runtime use

Iteration has left several problems in the implementation:

- useful semantic evidence can be filtered, collapsed, or reduced to terse
  deterministic candidates before the LLM sees it
- some deterministic extractors act like final semantic classifiers instead of
  evidence collectors
- prompts and policy gates sometimes overconstrain the LLM to small fixed sets
  instead of letting it make human-like categorization judgements
- TerraFirmaGreg validation concepts have leaked into generic code and prompt
  shape
- `organization_group` has been polluted by workstation / workflow labels even
  though it should represent player-facing storage or mental buckets
- `mod_subsystem` has legacy separate handling even though it is just another
  pack vocabulary facet
- Stage 3 silently drops invalid vocabulary-backed values instead of surfacing
  missing-vocabulary proposals for review
- `tools/classification/src/vocabulary/pack_vocabulary.ts` has grown into a
  multi-responsibility file of roughly three thousand lines

The immediate goal is not a new feature surface. The goal is to make the
pipeline understandable and generic again, while preserving the good pieces
that already exist.

## Guiding Rules

- The core pipeline is modpack-agnostic. Pack-specific examples may exist in
  fixtures, generated artifacts, or explicitly supplied future hint files, but
  not in production defaults, prompts, or policy gates.
- Semantic text is primary evidence. Tooltip/lore text, guidebook bodies,
  quest text, advancement/lang text, Ponder/category labels, KubeJS/datapack
  overlays, stack groups, resource-pack lang overrides, and mod descriptions
  should remain available to the LLM unless they are clear machine noise.
- Deterministic preprocessing collects and organizes evidence. It may remove
  syntactic garbage, duplicates, and known non-semantic boilerplate, but it
  should not decide all semantic membership before the LLM stage.
- Vocabulary generation creates reviewable accepted/review/rejected values.
  It is not an item membership classifier.
- Stage 3 writes only accepted vocabulary ids into final classification
  resources, but it must have a review path for "none of the accepted values
  fit this item."
- `processing_in`, `used_at`, and `workflow_role` stay separate:
  `processing_in` is raw deterministic recipe/process evidence, `used_at` is
  player-facing station or process context, and `workflow_role` is the item's
  role in a process.

## Target Module Shape

Keep `pack_vocabulary.ts` as a compatibility barrel exporting the existing
top-level functions while moving implementation into focused modules under
`tools/classification/src/vocabulary/`:

- `pipeline.ts` - orchestration for candidate extraction, curation, retries,
  and artifact assembly
- `types.ts` - shared candidate, evidence, policy, decision, and review types
- `ids.ts` - value-id grammar, scoping helpers, label normalization, alias
  helpers, and duplicate detection
- `semantic_index.ts` - semantic evidence indexing, source joins, boilerplate
  filtering, and prompt-budget selection
- `prompt.ts` - vocabulary curation prompt construction and repeated output
  rules
- `curation.ts` - LLM response parsing, coverage validation, retry prompts, and
  accepted/review/rejected normalization
- `reports.ts` - review artifact formatting and diagnostic summaries
- `candidates/` - source-specific extractors with a shared extractor contract
- `policies/` - per-facet gates and LLM-visible guidance

Candidate extractors should be split by source or facet concern:

- universal defaults and previous/manual vocabulary
- document and semantic-text candidates
- item/block tags and tag domains
- recipe type, recipe role, and recipe-id families
- runtime stack groups and tooltip/lore data
- `organization_group`
- `mod_subsystem`
- `progression_stage`
- `workflow_role`, `used_at`, and related workflow facets

Policies should be small, facet-specific, and easy to audit. A policy may say
"this source is weak, mark as review" or "this value id is malformed, reject";
it should not encode a pack's workstation list or a specific modpack's tier
taxonomy.

## Behavioral Fixes

### Candidate Extraction Contract

Every extractor returns candidates with:

- facet id
- proposed value id / label / aliases when available
- source kind and source ids
- semantic evidence snippets or document references
- confidence or review reason
- machine-noise rejection reason when rejected before the LLM

Weak semantic signals should become review candidates with provenance. They
should not be silently dropped unless they are unusable as semantic input.

### Organization Groups

`organization_group` means "how a human player might group this item when
organizing inventory." It should collect candidate buckets from item names,
tags, stack groups, guide/quest prose, manuals, mod descriptions, prior
vocabulary, and broad semantic clusters.

Do not derive organization groups from workstation names or workflow values by
default. Workstation/process evidence can influence organization only when the
surrounding semantic evidence supports an actual player storage bucket such as
"cooking tools", "ore processing", "weaving", "decorative blocks", or
"building materials". Examples in prompts must be framed as examples, not a
closed list.

### Mod Subsystems

`mod_subsystem` is a normal vocabulary facet generated in the pack vocabulary
artifact. Remove any legacy runtime-only or separate subsystem path.

Subsystem candidates should come from namespace-owned systems, mod docs,
runtime item families, coherent owned tags, display-name families, and guide
or quest concepts. Do not infer a subsystem from generic recipe participation,
common tags, or arbitrary prose mentions.

### Workflow And Progression Facets

Workflow/process facets may use recipe categories, station tags, guide text,
quest context, and item roles as evidence, but they should stay player-facing
and pack-generic.

`progression_stage` should prefer explicit gate/tier language from quests,
guides, advancements, lang strings, tags, manuals, or previous vocabulary.
Low-evidence titles and incidental prose should be review candidates, not
accepted values.

### Stage 3 Missing-Vocabulary Path

Stage 3 prompts should continue to tell the LLM that final facet values must
use accepted vocabulary ids. When no accepted value fits, the model should omit
that facet value and emit a structured review proposal instead:

```json
{
  "vocabulary_proposals": [
    {
      "item": "namespace:item_id",
      "facet": "organization_group",
      "label": "Cooking Tools",
      "proposed_id": "slot:cooking_tools",
      "rationale": "The accepted vocabulary has cookware stations but no player-facing bucket for reusable cooking implements.",
      "evidence": ["display name, tooltip, guide, tag, or stack-group references"]
    }
  ]
}
```

Final generated resources still contain only accepted ids. Proposed ids are
review artifacts that can feed the next vocabulary refinement pass.

## Implementation Slices

### Slice 0 - Plan And Characterization

- Add this plan and point the active queue at it.
- Add or update characterization tests around the current top-level vocabulary
  functions before moving code.
- Capture representative dry-run prompt output and candidate summaries from
  existing fixtures so the mechanical split can be checked for accidental
  behavior changes.

Exit criteria:

- the current public `pack_vocabulary.ts` exports are known and covered
- the refactor has a reviewable baseline for candidate counts, prompt sections,
  and accepted/review/rejected parsing

### Slice 1 - Mechanical Module Split

- Move implementation out of `pack_vocabulary.ts` into the target module shape.
- Keep CLI behavior and public imports stable.
- Add focused unit tests for id helpers, semantic indexing, candidate grouping,
  prompt rendering, curation parsing, and policy dispatch.
- Do not intentionally change vocabulary quality in this slice.

Exit criteria:

- `pack_vocabulary.ts` is a small compatibility barrel / thin orchestrator
- existing tests pass with no intentional behavior changes
- future policy work can happen in small files

### Slice 2 - Semantic Evidence And Policy Repair

- Replace over-filtering extractors with source-specific collectors that
  preserve semantic evidence and provenance.
- Remove workflow-to-organization coupling.
- Remove separate `mod_subsystem` handling and fold subsystem proposal fully
  into the vocabulary pipeline.
- Audit progression, workflow, organization, subsystem, and universal-default
  gates for hardcoded pack concepts or example-as-constraint behavior.
- Add diagnostics for candidate counts by facet, source kind, policy decision,
  and "candidate has no semantic evidence".

Exit criteria:

- deterministic extraction no longer turns workstation/process labels into
  organization buckets by default
- no production policy or prompt hardcodes TerraFirmaGreg-specific concepts
- review artifacts make over-filtering visible

### Slice 3 - Stage 3 Flexibility And Review Proposals

- Add `vocabulary_proposals` to Stage 3 parsing and run reports.
- Update Stage 3 prompts so output constraints are repeated at the end of the
  user prompt.
- Keep final layer validation closed over accepted vocabulary ids.
- Stop treating "invalid value dropped" as enough feedback when the real issue
  is missing vocabulary; surface a review proposal instead when the model
  provides one.

Exit criteria:

- item classification can express "the accepted vocabulary is missing a useful
  value" without writing invalid ids into generated resources
- retry/report output distinguishes parse failures, invalid ids, and missing
  vocabulary proposals

### Slice 4 - Dry-Run Audit And Regeneration Gate

- Run vocabulary prompt dry-runs before spending a live LLM run.
- Inspect source distributions, semantic evidence coverage, policy decisions,
  and prompt shape.
- Regenerate vocabulary only after dry-run output looks generic, semantically
  rich, and not constrained to examples.
- Run a small Stage 3 canary with the regenerated vocabulary.
- Only then decide whether to run full `classify-runtime-pack`.

Exit criteria:

- dry-run artifacts show broad semantic context reaching the LLM
- vocabulary output does not look like deterministic workstation/tag leakage
- Stage 3 canary validates and produces useful review proposals instead of
  silent drops for missing values

Current validation snapshot:

- `out/tfg-refactor-vocabulary-20260512-generic-512-policy5/tfg.facet-vocabulary.json`
  validates with 723 accepted values and no legacy separate `mod_subsystem`
  path.
- `out/tfg-refactor-stage3-canary-20260512/policy5-prompt-v11-sample101-live/`
  parsed 101/101 mixed TFG sample items and validated against the accepted
  vocabulary.
- The 101-item run surfaced deterministic gaps for processed ore material
  families and bare log form; those are patched in `material_family` and
  `form` rules and covered by tests. A focused v11 rerun over the earlier
  prompt/integration failure items is clean.
- Remaining non-blocking signal: the current policy5 vocabulary predates the
  generic `slot:open` use affordance default and still lacks some vanilla
  station ids such as `minecraft:smoker`. Stage 3 drops those rather than
  writing invalid ids, but a final vocabulary refresh is cleaner before a full
  run.

## Test Plan

Run after the relevant slices:

```bash
cd tools/classification
bunx tsc --noEmit
bun test
```

Add focused tests for:

- value-id normalization and scoping
- semantic evidence indexing and prompt-budget selection
- candidate extractor provenance and weak-signal handling
- `organization_group` not being generated from workstation names alone
- `mod_subsystem` generated only through the vocabulary pipeline
- prompt rendering that treats examples as examples, not closed lists
- Stage 3 accepted-vocabulary enforcement plus `vocabulary_proposals`
- review artifact diagnostics for source distribution and evidence coverage

Use synthetic generic fixtures for policy tests. Keep TFG-like fixtures as
stress cases only; they must not be the source of production rules.

## Acceptance Criteria

- The vocabulary code is split into small modules with clear ownership.
- The pipeline preserves rich semantic input through vocabulary generation and
  item classification prompts.
- Deterministic extraction collects evidence and review candidates instead of
  pre-deciding semantic membership.
- No production prompt, policy, or default contains pack-specific TFG concepts.
- `organization_group` values read like player inventory buckets, not
  workstation names.
- `mod_subsystem` is part of the normal pack vocabulary artifact.
- Stage 3 can report missing useful vocabulary without writing invalid ids into
  final resources.
- A full pack run remains gated until dry-run and canary artifacts pass review.

## Non-Goals

- no new runtime UI surface
- no public classification database work
- no EMI goal-tab or task-view work
- no pack-specific rule engine in this refactor
- no full vocabulary or Stage 3 run until the refactor and canary gate pass
