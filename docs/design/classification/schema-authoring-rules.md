# Classification Schema Authoring Rules And Audit

Last updated: 2026-05-15

This is the active contract for authoring classification facets. Its job is to
prevent schema constants, prompt wording, validation, or legacy deterministic
rules from smuggling pack-specific judgment back into the pipeline.

The short version: if a modpack can add a valid value, the value set is not a
hardcoded TypeScript enum. It is either vocabulary-backed, derived from exact
runtime data, or free text.

## Value-source Classes

Every facet must fit one of these classes before it is added to
`tools/classification/src/schema/facets.ts`.

| Class | When to use it | Validation shape |
| --- | --- | --- |
| Exact runtime fact | The value can be read completely and accurately from registry/export/component/recipe data. | Boolean, numeric, resource id, or raw string pattern. Code may derive it. |
| SLOT-owned judgment scale | SLOT deliberately defines a small product scale and a modpack cannot add a new valid value without us changing the product meaning. | Closed enum/multi-enum. |
| Vocabulary-backed semantic set | Values are semantic categories, player concepts, modpack concepts, forms, origins, locations, effects, roles, or other reusable labels that can vary by pack. | Free-text/multi-free-text pattern plus pack vocabulary artifact. |
| Human-readable phrase | The model should write a short phrase for audit/search, not a reusable id. | Free-text/multi-free-text phrase pattern. |
| Boolean judgment | The answer is truly yes/no, but exact data may be incomplete and the LLM may need to judge. | Boolean, with exact evidence used as evidence rather than authority unless it is complete. |

## Rules

1. Closed enums are rare. A facet may be closed only when every valid value is
   SLOT-owned or externally fixed. Ask: "Can a mod or datapack add a valid
   value?" If yes, do not hardcode the set.
2. Exact data is not semantic authority. Runtime/code-derived facts can be
   emitted when they are complete raw facts (`mod_namespace`, `is_block_item`,
   `processing_in`). Heuristic semantic guesses belong in evidence or diagnostics,
   not in the classification layer as model constraints.
3. Vocabulary is the grounding mechanism for semantic consistency. Any
   reusable semantic value set that is not exact and complete must be generated
   by the vocabulary loop, then supplied to classification.
4. Vanilla belongs in a baseline vocabulary artifact, not in universal
   TypeScript defaults, unless the facet is explicitly a vanilla standard such
   as the 16 dye colors.
5. Pack-specific values must not appear in schema constants, prompt rules, or
   deterministic production rules. If a value is discovered from TFG, Create,
   TFC, GregTech, Ad Astra, or any other pack/mod, it belongs in generated
   vocabulary or data-derived evidence.
6. The parser must not silently discard plausible model judgment for semantic
   facets. Unlisted vocabulary-backed values stay in the layer with
   `vocab_review: true`. Dropping invalid values is acceptable only for true
   closed SLOT-owned scales or exact fixed standards.
7. `organization_group` has a higher product impact, but it is not a schema
   exception. It is vocabulary-backed, every classified item should receive one
   best home when targeted, and new values are accepted with review/watchlist
   flags rather than overwritten by rules.
8. Before a long run, audit the schema and generation paths. A canary that
   produces schema proposals or closed-enum warnings for semantic facets is a
   schema failure, not a reason to add a post-LLM bandaid.

## Decision Procedure

For each facet:

1. Is the value a raw fact that can be derived completely from the runtime or
   static export? Use exact derivation and do not ask the LLM unless data is
   incomplete.
2. Is the value a SLOT product scale where new values would be a product-design
   change, not a modpack feature? A closed enum is allowed.
3. Is the value a reusable semantic category, type, form, origin, place,
   subsystem, workflow, status, role, medium, or material that mods can extend?
   Make it vocabulary-backed.
4. Is the value just a short human phrase? Keep it free text and do not treat it
   as controlled vocabulary.
5. If none of those answers is clear, do not run a full classifier pass yet.
   Decide the value source first.

## Current Facet Audit

Verdicts:

- **OK** means the schema shape matches the rules.
- **Fix before full run** means the current schema can discard or distort valid
  modpack judgment.
- **Remove facet** means the facet does not have a clear enough use case to
  keep in the schema.
- **Advisory input only** means code-derived input can help the model, but LLM
  classification output wins and the code-derived value must not override it.

| Facet | Current shape | Correct class | Verdict | Notes |
| --- | --- | --- | --- | --- |
| `mod_namespace` | `free_text`, deterministic | Exact runtime fact | OK | Directly from item id namespace. |
| `role` | Closed enum, LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. Item roles are semantic and pack-shaped, not a hardcoded universal enum. |
| `material_family` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Primary material varies by pack and is correctly vocabulary-driven. |
| `material_secondary` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Same value source as `material_family`. |
| `form` | Closed enum, deterministic + LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. Mods add forms such as wires, cables, gears, rails, gravel, flowers, bricks, and many pack-specific shapes. |
| `tier` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `required_tool` | Closed enum, deterministic + LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. Mods can add hammers, wrenches, knives, saws, chisels, and other player-recognized tool classes. |
| `required_tool_tier` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `equip_slot` | Closed enum, deterministic + LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. Vanilla equipment slots are fixed, but modded accessory/curio/body slots are not. |
| `dye_color` | Closed enum, deterministic + LLM | Fixed vanilla standard | OK, advisory input only | The 16 dye colors are a valid closed set if the facet means exactly vanilla dye color. Code-derived color evidence is advisory; LLM classification wins. |
| `rarity` | Closed enum, deterministic + LLM | SLOT-owned judgment scale | OK, advisory input only | Closed scale is fine. Legacy deterministic rarity evidence is advisory; LLM classification wins. |
| `emits_light` | Boolean, deterministic + LLM | Boolean judgment/exact fact | OK, advisory input only | Component/tag/id evidence is useful input; LLM classification wins when targeted. |
| `carry_frequency` | Closed enum, LLM | SLOT-owned judgment scale | OK | Product-defined player-inventory frequency scale. |
| `activity` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `flavor` | Closed multi-enum, LLM | None | Remove facet | Decision: remove. No clear current use case justifies maintaining this facet. |
| `palette` | Closed multi-enum, LLM | None | Remove facet | Decision: remove. No clear current use case justifies maintaining this facet; keep `dye_color` for the fixed vanilla dye standard. |
| `origin` | Closed multi-enum, deterministic + LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. Dimensions, structures, planets, biomes, and acquisition sources are pack-specific. |
| `storage_categories` | Closed multi-enum, LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. Storage behavior and slot kinds are mod-defined. |
| `spawn_interaction` | Closed multi-enum, LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. Mob/spawn mechanics are mod-defined. |
| `combat_bonus` | Closed multi-enum, LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. Mob families, bosses, statuses, and combat mechanics are mod-defined. |
| `environmental_property` | Closed multi-enum, LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. World physics and ambient mechanics expand heavily in modpacks. |
| `transport_medium` | Closed multi-enum, LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. Mods add heat, steam, pressure, data, mana, oxygen, radiation, and other carried media. |
| `processing_in` | `multi_free_text`, deterministic | Exact runtime fact | OK | Raw recipe-consumption evidence. Keep separate from semantic `workflow`/`used_at`. |
| `workflow` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `workflow_role` | Vocabulary-backed scoped values | Vocabulary-backed semantic set | OK | Correctly depends on generated `workflow`. |
| `used_at` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Station/tool/surface names are pack-shaped. |
| `food_category` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `food_use` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `preparation_state` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `material_process_stage` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly moved away from universal hardcoded stages. Keep it that way. |
| `stock_profile` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Model-owned inventory behavior vocabulary. |
| `container_state` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `equipment_effect` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Equipment capabilities and effects are mod-shaped. |
| `protection_context` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Hazards/environments are mod-shaped. |
| `progression_stage` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `loadout_context` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `use_affordance` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correctly pack-shaped. |
| `primary_uses` | Phrase free text | Human-readable phrase | OK | Not a controlled id set. |
| `organization_group` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Correct value source. Product risk is handled by prompt/review/watchlist, not hardcoded filtering. |
| `mod_subsystem` | Vocabulary-backed | Vocabulary-backed semantic set | OK | Query/subsystem signal, not main home authority. |
| `produces_effect` | Vocabulary-backed namespaced id | Exact/vocabulary-backed semantic set | OK | Effects should be registry ids when evidence supports them; vocabulary keeps batch naming stable. |
| `multiblock_component_of` | Vocabulary-backed namespaced id | Vocabulary-backed semantic set | OK | Named structures/multiblocks are mod-shaped. |
| `multiblock_role` | Closed enum, LLM | Vocabulary-backed semantic set | Fix before full run | Decision: make vocabulary-backed. Multiblock roles are useful but not complete as a hardcoded enum. |
| `biome` | Vocabulary-backed namespaced id | Exact/vocabulary-backed semantic set | OK | Biomes are registry-shaped and pack-specific. |
| `y_level_range` | Closed enum, deterministic + LLM | SLOT-owned judgment scale | OK, advisory input only | Coarse buckets are fine; id-pattern evidence is advisory and LLM classification wins when targeted. |
| `is_block_item` | Boolean, deterministic | Exact runtime fact | OK | Direct registry/component fact. |
| `is_stackable` | Boolean, deterministic | Exact runtime fact | OK | Direct max-stack fact. |
| `is_fuel` | Boolean, deterministic + LLM | Boolean judgment/exact fact | OK, advisory input only | Boolean shape is fine; hardcoded vanilla fuel lists are not complete for modpacks. Code-derived evidence is advisory and LLM classification wins when targeted. |
| `has_durability` | Boolean, deterministic + LLM | Exact/runtime fact | OK | Direct component fact when exported. |
| `has_enchantments` | Boolean, deterministic + LLM | Exact/runtime fact | OK | Direct component fact when exported; LLM can fill gaps. |
| `has_nbt_variation` | Boolean, deterministic + LLM | Exact/runtime fact | OK | Component-driven, with LLM fill for missing export details. |
| `is_creative_only` | Boolean, LLM | Boolean judgment | OK | Exact derivation is hard; LLM judgment is acceptable. |

## Generation-path Audit

| Path | Facets affected | Status | Required follow-up |
| --- | --- | --- | --- |
| `FACETS` closed value constants | `role`, `form`, `required_tool`, `equip_slot`, `origin`, `storage_categories`, `spawn_interaction`, `combat_bonus`, `environmental_property`, `transport_medium`, `multiblock_role`, `flavor`, `palette` | Breaks or risks breaking the rules | Migrate the listed semantic facets to vocabulary-backed values, and remove `flavor` / `palette`. Keep closed only for true SLOT-owned scales or fixed standards. |
| `VOCABULARY_BACKED_FACETS` | All semantic controlled sets | Missing several semantic fields above | Add every pack-shaped semantic facet listed in the audit. The vocabulary loop should generate or carry these values. |
| `src/llm/prompt.ts` | All LLM-authored facets | Mostly aligned with LLM-judgment strategy | The prompt correctly treats vocabulary-backed values leniently, but it still tells the model closed enums must stay closed. That is only safe after migrating semantic enums and removing `flavor` / `palette`. |
| `src/llm/parse.ts` | Closed enum/multi-enum facets | Breaks current semantic facets | It drops out-of-enum values. That behavior is correct only for true closed scales. For semantic facets, make the facet vocabulary-backed first. |
| `src/llm/run.ts` | Vocabulary-backed facets | Good | It keeps out-of-vocabulary values with `vocab_review: true` and records proposals. This is the desired behavior for semantic controlled sets. |
| `src/deterministic/run.ts` | Legacy stage 2 rules | Diagnostic only in current runtime-pack flow | Keep stage 2 out of modpack semantic output. If any command starts merging it into Stage 3 again, that is a regression. |
| `rules/form.ts` | `form` | Breaks rules if authoritative | Tag/id/model heuristics are not a complete modpack form taxonomy. Convert `form` to vocabulary-backed or downgrade this to evidence/diagnostics. |
| `rules/origin.ts` | `origin` | Breaks rules if authoritative | Contains vanilla structure/dimension/source mappings and id overrides. `origin` must be vocabulary-backed for modpacks. |
| `rules/rarity.ts` | `rarity` | Advisory input only | Uses semantic id overrides and vanilla component interpretation. Fine as diagnostic evidence; not an authority over LLM rarity judgment. |
| `rules/y_level_range.ts` | `y_level_range` | Advisory input only | Coarse buckets are valid, but id-pattern heuristics should not override pack-specific context. |
| `rules/required_tool.ts` | `required_tool` | Breaks rules if authoritative | Convert `required_tool` to vocabulary-backed; tag/component/category evidence can still inform the model. |
| `rules/equip_slot.ts` | `equip_slot` | Breaks rules if authoritative | Convert `equip_slot` to vocabulary-backed; component slot evidence can still inform the model. |
| `rules/dye_color.ts` | `dye_color` | Advisory input only | Closed 16-color facet is okay, but the rule contains mod-specific tag/suffix knowledge. Prefer generic dye evidence or LLM judgment over hardcoded mod literals. |
| `rules/is_fuel.ts` | `is_fuel` | Advisory input only | Boolean facet is fine, but vanilla fuel hardcoding is incomplete for modpacks unless runtime fuel evidence is available. |
| `rules/emits_light.ts` | `emits_light` | Advisory input only | Component/tag evidence is useful; known-id/suffix lists are hints only. |
| `rules/booleans.ts` | Boolean exact facts | OK | Component/registry facts are appropriate. |
| `rules/processing_in.ts` | `processing_in` | OK | Emits raw recipe-consumption evidence and passes mod recipe types through. |
| `tools/classification/src/cli.ts` Stage 3 base layer | All facets | Good in current runtime-pack/classify defaults | Defaults use stages `1,3` and create an LLM-only base layer. Keep `1,2` as explicit diagnostics only. |
| Tests and fixtures | Parser, prompt, deterministic rules | Mixed | Tests that assert semantic out-of-enum values are dropped are only valid for true closed scales. Update them after schema migration. |

## Full-run Gate

Before another long classification run:

1. Re-run this audit against `FACETS` and confirm every pack-shaped semantic
   facet is vocabulary-backed.
2. Confirm `flavor` and `palette` are removed from schema, prompts, target
   facet defaults, validators, fixtures, and tests.
3. Remove stale `confidence` / `signal` support from the classification
   contract: prompts, docs, schema, parser types, layer writing, validators,
   fixtures, and tests. Do not preserve them as compatibility metadata.
4. Define one canonical output root and naming format for the whole rerun:
   vanilla baseline evidence/vocabulary, pack evidence, vocabulary refinement
   rounds, reviewed vocabulary, classification layers, datapack output, run
   reports, warnings/proposals, and replay fixtures. The latest artifact should
   be obvious from the path, and stale experiments should not sit beside it with
   similar names.
5. Confirm no production prompt says a pack-shaped semantic facet is a closed
   enum.
6. Confirm Stage 3 receives the full usable vanilla + pack vocabulary for every
   vocabulary-backed target facet.
7. Confirm Stage 3 starts from an empty/LLM-owned base layer for semantic
   classification.
8. Confirm code-derived semantic inputs are passed only as evidence/diagnostics;
   they do not override, suppress, or constrain LLM output.
9. Run a canary and treat semantic `schema_proposals`, out-of-enum warnings, or
   dropped values as schema failures, not as output-quality nitpicks.
10. Only then run the full classifier and package the layer/datapack.
