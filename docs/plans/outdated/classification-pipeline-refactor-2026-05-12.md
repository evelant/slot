# Classification Pipeline Refactor Plan

> **Outdated historical plan.** This file was moved out of the active docs on
> 2026-05-14. It records the refactor path that led to the current strategy, but
> it is not current process guidance. Current classification authoring direction
> lives in
> [../classification-facet-vocabulary.md](../classification-facet-vocabulary.md).

Last updated: 2026-05-14

Status: active, implementation gate passed. The cleanup direction changed after
TFG validation exposed the main architectural mistake: deterministic semantic
"stage 2" curation biased and constrained the model while still leaving many
wrong values for the model to correct. The current implementation target is
LLM-judgement-first:

1. gather, normalize, deduplicate, and summarize pack evidence
2. refine usable vocabulary with iterative LLM calls over semantic context,
   previous working vocabulary, and rotating item samples
3. classify items with the usable vocabulary as grounding input
4. accept valid model output without semantic overwrites or keyword deny-list
   cleanup
5. assemble the generated layer into bundled resources or a datapack

The existing vocabulary feature plan remains the design reference for why pack
vocabulary matters; this plan is the implementation cleanup path.

Related docs:

- [classification-facet-vocabulary-2026-05-13.md](classification-facet-vocabulary-2026-05-13.md) -
  vocabulary-backed semantic facet design and validation history
- [item-classification-v1.md](item-classification-v1.md) - original classification
  pipeline and runtime export plan
- [../../design/classification/README.md](../../design/classification/README.md) -
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
5. use the usable vocabulary plus item evidence to classify individual items
6. emit validated mod resources / datapack layers for SLOT runtime use

Iteration has left several problems in the implementation:

- useful semantic evidence can be filtered, collapsed, or reduced before the
  LLM sees it
- some old deterministic extractors acted like final semantic classifiers
  instead of evidence collectors
- prompts and validation gates sometimes overconstrain the LLM to small fixed sets
  instead of letting it make human-like categorization judgements
- TerraFirmaGreg validation concepts have leaked into generic code and prompt
  shape
- `organization_group` has been polluted by workstation / workflow labels even
  though it should represent player-facing storage or mental buckets
- `mod_subsystem` has legacy separate handling even though it is just another
  pack vocabulary facet
- Stage 3 should surface useful missing-vocabulary proposals for the next
  vocabulary loop instead of treating "invalid value dropped" as sufficient
  feedback
- `tools/classification/src/vocabulary/pack_vocabulary.ts` has grown into a
  multi-responsibility file of roughly three thousand lines

The immediate goal is not a new feature surface. The goal is to make the
pipeline understandable and generic again, while preserving the good pieces
that already exist.

## Guiding Rules

- The core pipeline is modpack-agnostic. Pack-specific examples may exist in
  fixtures, generated artifacts, or explicitly supplied future hint files, but
  not in production defaults, prompts, or validation gates.
- Semantic text is primary evidence. Tooltip/lore text, guidebook bodies,
  quest text, advancement/lang text, Ponder/category labels, KubeJS/datapack
  overlays, stack groups, resource-pack lang overrides, and mod descriptions
  should remain available to the LLM unless they are clear machine noise.
- Pre-LLM processing collects and organizes evidence. It may remove syntactic
  garbage, duplicates, and known non-semantic boilerplate, but it must not
  decide semantic membership before the LLM stage.
- Vocabulary generation creates usable `accepted`/`review` values and explicit
  `rejected` blocks. It is not an item membership classifier.
- Stage 3 uses the usable vocabulary as grounding, makes the item-facet
  judgement itself, and writes valid model output into final classification
  resources. When no vocabulary value fits, it should surface a useful proposal
  for the next vocabulary loop.
- `processing_in`, `used_at`, and `workflow_role` stay separate:
  `processing_in` is raw recipe/process evidence, `used_at` is
  player-facing station or process context, and `workflow_role` is the item's
  role in a process.

## Target Module Shape

Keep `pack_vocabulary.ts` as a compatibility barrel exporting the existing
top-level functions while moving implementation into focused modules under
`tools/classification/src/vocabulary/`:

- `pipeline.ts` - orchestration for context extraction, curation, retries,
  and artifact assembly
- `types.ts` - shared context, evidence, policy, decision, and review types
- `ids.ts` - value-id grammar, scoping helpers, label normalization, alias
  helpers, and duplicate detection
- `semantic_index.ts` - semantic evidence indexing, source joins, boilerplate
  filtering, and prompt-budget selection
- `prompt.ts` - vocabulary curation prompt construction and repeated output
  rules
- `curation.ts` - LLM response parsing, coverage validation, retry prompts, and
  accepted/review/rejected normalization
- `reports.ts` - review artifact formatting and diagnostic summaries
- `context/` - source-specific extractors with a shared context contract
- `policies/` - per-facet guidance and mechanical validation helpers

Context extractors should be split by source or facet concern:

- universal defaults and previous/manual vocabulary
- document and semantic-text context
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

### Context Extraction Contract

Every extractor returns context records with:

- facet id
- possible label / aliases when available
- source kind and source ids
- semantic evidence snippets or document references
- confidence or review reason
- machine-noise rejection reason when rejected before the LLM

Weak semantic signals can be marked as review/watchlist context. They should not
be silently dropped unless they are unusable as semantic input.

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
Low-evidence titles and incidental prose should be review/watchlist values, not
silently rejected.

### Stage 3 Missing-Vocabulary Path

Stage 3 prompts should tell the LLM to prefer usable vocabulary ids for
consistency. When no usable value fits, the model should make the best
classification judgement it can and emit a structured vocabulary proposal for
the next loop:

```json
{
  "vocabulary_proposals": [
    {
      "item": "namespace:item_id",
      "facet": "organization_group",
      "label": "Cooking Tools",
      "proposed_id": "cooking_tools",
      "rationale": "The usable vocabulary has cookware stations but no player-facing bucket for reusable cooking implements.",
      "evidence": ["display name, tooltip, guide, tag, or stack-group references"]
    }
  ]
}
```

Generated resources should remain schema-valid and grounded in usable
vocabulary. Missing-value proposals are watchlist artifacts that feed the next
vocabulary refinement pass; they are not an excuse to silently discard the
model's semantic signal.

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

### Slice 2 - Semantic Evidence And Prompt Repair

- Replace over-filtering extractors with source-specific collectors that
  preserve semantic evidence.
- Remove workflow-to-organization coupling.
- Remove separate `mod_subsystem` handling and fold subsystem proposal fully
  into the vocabulary pipeline.
- Audit progression, workflow, organization, subsystem, and universal-default
  prompt/context paths for hardcoded pack concepts or example-as-constraint
  behavior.
- Add diagnostics for context counts by facet, source kind, validation decision,
  and "context has no semantic evidence".

Exit criteria:

- pre-LLM evidence extraction no longer turns workstation/process labels into
  organization buckets by default
- no production policy or prompt hardcodes TerraFirmaGreg-specific concepts
- review artifacts make over-filtering visible

### Slice 3 - Stage 3 Flexibility And Vocabulary Proposals

- Add `vocabulary_proposals` to Stage 3 parsing and run reports.
- Update Stage 3 prompts so output constraints are repeated at the end of the
  user prompt.
- Keep final layer validation closed over usable vocabulary ids.
- Stop treating "invalid value dropped" as enough feedback when the real issue
  is missing vocabulary; surface a proposal instead when the model provides
  one.

Exit criteria:

- item classification can express "the usable vocabulary is missing a useful
  value" without writing invalid ids into generated resources
- retry/report output distinguishes parse failures, invalid ids, and missing
  vocabulary proposals

### Slice 4 - Dry-Run Audit And Full-Run Gate

- Run vocabulary prompt dry-runs before spending a live LLM run.
- Inspect source distributions, semantic evidence coverage, policy decisions,
  and prompt shape.
- Regenerate vocabulary only after dry-run output looks generic, semantically
  rich, and not constrained to examples.
- Run broad Stage 3 canaries with the regenerated vocabulary.
- Move to full `classify-runtime-pack` once prompt shape, watchlists, and sample
  classifications look healthy.

Exit criteria:

- dry-run artifacts show broad semantic context reaching the LLM
- vocabulary output does not look like workstation/tag leakage
- Stage 3 canary validates and produces useful review proposals instead of
  silent drops for missing values

Current validation snapshot:

- The older 2026-05-12 policy5 canaries are now historical evidence of the
  failure mode: deterministic gaps and accepted-only prompting forced the model
  into corrections and omissions.
- The 2026-05-14 vocabulary loop uses larger rotating item samples, much better
  prompt caching, concise semantic context, and `accepted` + `review` as usable
  vocabulary.
- The next validation target is a larger classification canary using the full
  refined vocabulary and no semantic stage-2 base layer, followed by a full
  `classify-runtime-pack` and datapack assembly once the sampled output looks
  player-sensible.

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
- evidence extractor weak-signal handling
- `organization_group` not being generated from workstation names alone
- `mod_subsystem` generated only through the vocabulary pipeline
- prompt rendering that treats examples as examples, not closed lists
- Stage 3 usable-vocabulary grounding plus `vocabulary_proposals`
- review artifact diagnostics for source distribution and evidence coverage

Use synthetic generic fixtures for policy tests. Keep TFG-like fixtures as
stress cases only; they must not be the source of production rules.

## Acceptance Criteria

- The vocabulary code is split into small modules with clear ownership.
- The pipeline preserves rich semantic input through vocabulary generation and
  item classification prompts.
- Pre-LLM extraction collects evidence and watchlist context instead of
  pre-deciding semantic membership.
- No production prompt, policy, or default contains pack-specific TFG concepts.
- `organization_group` values read like player inventory buckets, not
  workstation names.
- `mod_subsystem` is part of the normal pack vocabulary artifact.
- Stage 3 can report missing useful vocabulary without silently discarding the
  model's semantic signal.
- A full pack run follows dry-run and canary artifacts once they look healthy
  enough to assemble into resources/datapack.

## Non-Goals

- no new runtime UI surface
- no public classification database work
- no EMI goal-tab or task-view work
- no pack-specific rule engine in this refactor
- no semantic stage-2 rule engine or pack-specific keyword cleanup in this
  refactor
