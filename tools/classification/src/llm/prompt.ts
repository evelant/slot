import type { ItemExtractRecord } from "../extract/record.ts";
import { FACETS, type FacetDef } from "../schema/facets.ts";
import { isUsableVocabularyState, type PackFacetVocabulary } from "../schema/vocabulary.ts";
import type { LlmDocumentContext } from "./document_context.ts";

export const PROMPT_VERSION = "stage3-prompt-v36";

const RECIPE_EXAMPLE_LIMIT = 24;
const LOOT_SOURCE_LIMIT = 16;
const BLOCK_TAG_LIMIT = 32;

/**
 * One item as presented to the LLM: a trimmed projection of the extracted item
 * evidence. The wire format is intentionally bounded but not tiny: include
 * enough concrete evidence for judgment calls while preventing single items
 * from dominating a batch.
 */
export interface LlmItemPayload {
  id: string;
  display_name: string | null;
  /**
   * Tags where this item is a **direct** listed member (not via a #tag
   * reference). Strongest classification signal; show first.
   */
  minecraft_tags_direct?: readonly string[];
  /**
   * Tags reached **transitively** via nested #tag references, excluding the
   * direct ones. Weaker but still useful context (e.g. oak_planks → `planks`
   * is direct, → `wooden_tool_materials` is inherited via `#planks`).
   */
  minecraft_tags_inherited?: readonly string[];
  /**
   * Live runtime tag membership when direct/inherited provenance is unavailable.
   * These are useful semantic signals, but weaker than known-direct tags because
   * they may include direct or transitive membership.
   */
  minecraft_tags_resolved?: readonly string[];
  /** Short list of recipe types this item is consumed in. */
  processing_in?: readonly string[];
  /**
   * Count of recipes consuming this item, grouped by recipe type —
   * e.g. { crafting_shaped: 38, smelting: 8, smithing_transform: 6 }.
   * Lets the LLM weigh the item's role across recipe categories without
   * re-deriving it from a flat id list.
   */
  recipe_consumption_by_type?: Record<string, number>;
  /** Count of recipes producing this item, grouped by recipe type. */
  recipe_production_by_type?: Record<string, number>;
  /** Count of concrete recipe ids that consume this item. */
  recipe_ingredient_count?: number;
  /** Count of concrete recipe ids that produce this item. */
  recipe_output_count?: number;
  /**
   * Bounded representative recipe ids that consume this item. When count is
   * larger than this array, treat it as examples only, not the full universe.
   */
  recipe_ingredient_examples?: readonly string[];
  /** Bounded representative recipe ids that produce this item. */
  recipe_output_examples?: readonly string[];
  /** True when one or both recipe example arrays were capped. */
  recipe_examples_truncated?: boolean;
  /** Runtime/static creative inventory group ids, when available. */
  creative_tabs?: readonly string[];
  /** Count of loot-table ids that can produce this item. */
  loot_source_count?: number;
  /** Bounded representative loot-table ids. */
  loot_source_examples?: readonly string[];
  /** True when loot source examples were capped. */
  loot_sources_truncated?: boolean;
  /** Block-side context for block items, when the extractor has it. */
  block_context?: {
    block_id?: string;
    block_tags?: readonly string[];
    block_tags_truncated?: boolean;
    requires_correct_tool?: boolean;
  };
  /** Lore lines from minecraft:lore — big semantic signal when present. */
  lore?: readonly string[];
  /** Tooltip/lang/exported player-facing prose preserved from extraction. */
  semantic_text?: readonly { source?: string; text: string }[];
  /**
   * Gated guidebook/advancement snippets linked to this runtime item by
   * actual item ids. Kept separate from direct item prose so the LLM can weigh
   * broad document context more carefully.
   */
  document_context?: readonly LlmPromptDocumentContext[];
  /**
   * Key component values with semantic meaning the LLM can't derive from
   * tags/recipes alone — food nutrition, potion effects, damage resistance
   * types, weapon stats, jukebox song, dye color, trim material, etc.
   * Pulled from `component_data` and kept focused on player-visible semantics.
   */
  component_highlights?: Record<string, unknown>;
}

export interface LlmPromptDocumentContext {
  kind: LlmDocumentContext["kind"];
  label?: string;
  related_items?: readonly string[];
  snippets: readonly string[];
}

export interface LlmPromptInput {
  /** Pack id, used only for review/proposal guidance. */
  pack_id?: string;
  /** Items to classify in this batch. */
  items: readonly LlmItemPayload[];
  /** Facet ids the LLM is expected to try to populate. */
  target_facets: readonly string[];
  /** Nearest-neighbor priming: for each item id, id → summary string.
   *  Empty during milestone 5 (stage 4 fills this later). */
  neighbors?: Record<string, readonly string[]>;
  /**
   * Usable pack vocabulary values for vocabulary-backed facets. Values marked
   * `accepted` or `review` are listed here; rejected values are excluded.
   */
  facet_vocabulary?: PromptFacetVocabulary;
  /**
   * Optional verbose-prompt sections.
   *
   * `verbose_facet_disambiguation` (default **ON**) — principle-level
   * reasoning per facet: how a player thinks about, uses, and groups
   * items in each category. A/B testing on the 40-item playtest sample
   * showed adding this substantially improves hard-category accuracy
   * over the cardinal rule alone. It generalizes to novel
   * items because the rules are reasoning-based, not enumerative.
   * Flip OFF only for experimentation against the lean baseline.
   *
   * `verbose_common_misconceptions` (default **OFF**) — item-level
   * checklist of past LLM failures (logs / doors / beds / rails /
   * spawn-eggs / Block-of-X / mob-drops). Useful when a new category
   * surfaces a regression and we want to nudge the LLM with explicit
   * cases, but routinely keeping it on biases toward pattern-matching
   * enumerated examples over trusting the principle.
   */
  prompt_extras?: {
    verbose_facet_disambiguation?: boolean;
    verbose_common_misconceptions?: boolean;
  };
}

export type PromptFacetVocabulary = Record<string, readonly PromptFacetVocabularyValue[]>;

export interface PromptFacetVocabularyValue {
  id: string;
  label: string;
  state?: "review";
  description?: string;
  aliases?: readonly string[];
  parent?: string;
}

const SYSTEM_PREAMBLE = `You are classifying Minecraft items for the
inventory mod Slot. Slot uses these classifications for inventory
organization, search, filters, task/context views, and future
player-assistive features. Your job is to capture **how a player thinks
about each item** — what they call it, where they store it, what they
use it with, how they interact with it, and which pack concepts it
belongs to — by emitting a concise facet record per the schema below.

This is a semantic classification task, not just a storage-section task.
\`organization_group\` is the high-impact primary home facet, so it should
be filled when targeted; new organization-group proposals need special
caution because they can create or split main-wall sections. Most other
facets are lower-risk semantic/query metadata, so useful inferred values
are preferred over silence.

# Cardinal rule (overrides everything else in this prompt)

Classify items by **how a player thinks about them, uses and interacts
with them, and organizes them in practice**, NOT by the raw technical
data attached. The data is input; the player's mental model is the
answer.

This rule applies to every LLM-authored judgment — role, activity,
primary_uses, carry_frequency, vocabulary-backed facets, etc.
— not just \`role\`. Every other rule below is a concretization of
this principle; your job is to make judgment calls about player
perception, not to mechanically transform the data into a category.

The concrete test, applied on every facet decision:

> "If a player handed me this item and asked 'where in my organized
> inventory would I expect to find this, what activities would I use it
> for, and which other items belong with it?', what's the answer?"

That answer beats the literal reading of any rule. After picking a
value, do one more pass: "Would a player be surprised by this answer,
or surprised to NOT see this item grouped with its siblings?" If yes,
reconsider.

# Output rules
- Facet kinds differ:
  - enum / multi_enum facets are closed: choose only listed allowed values.
  - ordinary non-vocabulary free_text / multi_free_text facets are judgment outputs:
    synthesize concise values matching the pattern. Do not send those to
    \`schema_proposals\` just because the exact value was not shown in an
    example.
  - vocabulary-backed facets are grounded by the usable Pack facet vocabulary:
    choose listed values from that facet's vocabulary whenever they fit. When
    the vocabulary is missing a useful value, you may still emit a concise
    facet-scoped value in \`facets\`; set \`vocab_review: true\`, include a
    short \`rationale\`, and optionally add a top-level \`vocabulary_proposals\`
    entry so the missing vocabulary is easy to review later.
- Default to useful judgment, not silence. For semantic/query facets such as
  \`activity\`, \`workflow\`, \`workflow_role\`, \`used_at\`,
  \`progression_stage\`, \`material_process_stage\`, \`mod_subsystem\`,
  food facets, container facets, use affordances, and similar metadata,
  a reasonable inferred value is usually better than leaving the facet empty.
  Use recipe ids/types, quest and advancement text, guide snippets, tags,
  display names, component data, neighboring items, and pack vocabulary labels
  to make judgment calls. Do not wait for an exact string match when the
  listed vocabulary value clearly fits the item.
- \`organization_group\` is the high-impact primary home facet. When it is
  targeted, assign exactly one best organization value for every item:
  preferably a listed built-in section such as \`metal_stock\` or a reviewed
  pack-specific split such as \`beekeeping\`. Be conservative about unlisted
  organization groups, but do not leave the facet empty merely because a
  default section fits. If a missing broad human storage bucket would improve
  the pack and the listed homes would be genuinely misleading, use the concise
  unlisted value with \`vocab_review: true\` and add an \`organization_group\`
  \`vocabulary_proposals\` entry when practical.
- Vocabulary-backed facet values are scoped by the facet itself. Use the exact
  listed value string, usually a simple lower_snake token such as \`cooking\`,
  \`mechanical_power\`, or \`steelmaking\`. Use namespace-qualified values only
  when the value is a real registry/resource id such as \`minecraft:furnace\`
  or \`minecraft:regeneration\`. Never emit display labels like "Steelmaking"
  as facet values.
- When the prompt includes a "Pack facet vocabulary" section, treat each
  facet's listed values as the preferred grounding set for \`facets\` in this
  batch. Actively use listed values when they fit the item; do not withhold an
  applicable vocabulary-backed facet merely because the evidence is an
  inference rather than an exact name match. If no listed value fits but a
  concise value matching the facet pattern would improve the classification,
  emit that value with \`vocab_review: true\` and a short \`rationale\`.
  A matching top-level \`vocabulary_proposals\` entry is useful but not a
  substitute for classifying the item. Exception: when \`organization_group\`
  is targeted, prefer the best listed home for every item and use unlisted
  groups only when the listed homes would be genuinely misleading. Copy listed
  values exactly as printed instead of normalizing separators or path segments.
- If the prompt has no "Pack facet vocabulary" section at all, the semantic
  job remains: emit useful vocabulary-backed values that match the facet
  grammar, mark them with \`vocab_review: true\`, and optionally add
  \`vocabulary_proposals\` for review. Do not go silent merely because the
  grounding vocabulary is incomplete.
- Only emit facets that actually apply to the item. The test: would a player consider this facet meaningful for this item? \`combat_bonus\` on bread, \`biome\` on a crafted-only item — players wouldn't expect a value, so omit. Exception: when \`organization_group\` is targeted, every item needs one primary home; prefer the best listed group, or use an unlisted \`vocab_review\` group only when the listed homes would be misleading. Do not emit \`null\`, empty arrays, or placeholder values to satisfy other target-facet lists.
- Multi-value facets must use \`values: [...]\` even when there is only one
  value. This includes \`organization_group\` and \`mod_subsystem\`; never emit it as a scalar \`value\` facet.
- For single-value enum facets where two values fit similarly, emit a two-element \`values\` array AND set \`ambiguous: true\`. Downstream reviewers see both. Never set \`ambiguous\` on multi-value facets; for multi-value facets emit the applicable \`values\` without \`ambiguous\`. For \`organization_group\`, emit exactly one best primary home in \`values\`.
- Keep facet entries small. The required payload is just \`value\` for a
  single-value facet or \`values\` for a multi-value facet. Optional review
  fields are allowed but should not become the task:
  - \`rationale: "<≤80 char>"\` when a short note would help audit a
    non-obvious judgment.
  - \`evidence: "<short quote>"\` only for the exact tag, recipe id,
    component, or player-facing phrase that drove a tricky choice.
  - \`vocab_review: true\` when a vocabulary-backed facet uses an unlisted
    value because no listed value fits well enough.
  Do not spend output tokens proving obvious calls. If output is getting long,
  omit optional review fields before dropping facets or items.
- semantic_text, document_context, lore, display_name, creative_tabs, and
  component_highlights are **player-perception signals** — strings, compact
  context, or group labels the player actually reads and forms expectations
  from. Take them seriously when present.
- \`document_context\` is input evidence, not an output facet. Never emit
  \`document_context\` inside an item's \`facets\` block.
- document_context contains conservative guidebook/advancement snippets linked
  to the item by actual runtime item ids. Treat it as supporting context for
  player-facing purpose, workflow, station, and progression judgments; direct
  item tooltip/lore still wins when they conflict. \`related_items\` shows
  the neighboring runtime items when a low-breadth page covers a small group.
  Guide snippets often describe several related items in one paragraph; assign
  only the use that belongs to the current item. Do not copy a use onto this
  item merely because the snippet also names a sibling item, ingredient, or
  workstation. Pay attention to the grammatical subject and to direct recipe
  evidence for the current item. Example: if a page about containers says the
  container can salt meat and can apply powders to glass, a salt item gets
  salting/preserving uses; it does not get glass-coloring unless the current
  item's own recipes or text support that use.
- If \`minecraft_tags_resolved\` is present, those tags are live runtime membership with unknown directness. Use them as semantic context, but don't treat them as intentional direct-tag evidence.
- Fields ending in \`_examples\` are bounded evidence. If the matching
  \`*_count\` is larger, do not infer that omitted recipe / loot ids are absent.
- For enum / multi_enum facets, if the closed allowed list lacks an important
  player-facing value, leave the invalid value out of \`facets\` and add an entry to
  \`schema_proposals\` at the top level. This does not apply to ordinary
  free_text facets: for those, synthesize a valid value directly in
  \`facets\`.
- If you need an unlisted value for a vocabulary-backed facet, put that value
  in \`facets\` with \`vocab_review: true\` and a short \`rationale\`. You may
  also add \`{item, facet, label, proposed_id, rationale, evidence}\` to
  top-level \`vocabulary_proposals\` so later vocabulary passes can absorb it.
- \`schema_proposals\` and \`vocabulary_proposals\` are top-level arrays only, siblings of \`items\`. Never put these keys inside an individual item's \`facets\` object; inside \`facets\`, every key must be a real facet id.
- Emit a best \`role\` for every item unless the data is genuinely unusable.
  If two roles are close, use the ambiguous two-value shape.
- Emit \`primary_uses\` for every item unless the data is genuinely unusable:
  one to three short player-facing phrases are enough. For SLOT, \`role\`,
  \`primary_uses\`, and \`carry_frequency\` are the core inventory semantics.
  After those, actively consider every target facet. Prefer a useful
  judgment-call value over omission for semantic/query facets.
- \`origin\` and \`rarity\` are lower impact than storage layout, so reasonable
  judgment is allowed. Do not use absent fields as negative evidence, and do
  not emit placeholders merely to satisfy a list, but do emit them when tags,
  loot/recipe presence, item names, quest/guide text, tier/progression context,
  or ordinary pack knowledge give a plausible player-facing answer. For
  example, \`crafted_only\` is valid when positive production-recipe evidence
  says the item is primarily crafted; \`common\` is valid when the item is a
  normal staple rather than rare progression stock.
- Output strict JSON only: no markdown, no code fences, no comments (// or /* */), no trailing commas, no commentary outside the JSON object.
- Your response MUST start with \`{\` and end with \`}\`. Do NOT prepend any narration (no "Here is…", "Continuing with…", etc.). Do NOT append any text after the closing brace.
- Classify every item listed in the \`items\` array. If output is getting long, omit optional review fields before dropping facets or items.`;

/**
 * Build a combined prompt for the legacy single-message path. Kept for
 * backwards-compat; prefer `buildSplitPrompt()` so the stable system-prompt
 * content separates from the per-batch data.
 */
export function buildBatchPrompt(input: LlmPromptInput): string {
  const { system, user } = buildSplitPrompt(input);
  return [system, "", user].join("\n");
}

/**
 * Build a split prompt: the `system` section contains the stable
 * classification rules + facet schema + pack vocabulary + disambiguation +
 * output-shape example. The `user` section carries the volatile per-batch item
 * evidence. Pass `system` as the chat system message and `user` as the chat
 * user message.
 *
 * The split improves provider-side cache hit rate because the system portion
 * is stable across every batch of a run. It also keeps task instructions
 * separate from volatile item evidence.
 */
export function buildSplitPrompt(input: LlmPromptInput): { system: string; user: string } {
  const schemaDoc = renderSchemaForPrompt(input.target_facets);
  const outputShape = renderExpectedOutput(input.target_facets, input.facet_vocabulary);
  const neighborsNote = renderNeighborsSection(input.neighbors);
  const facetVocabularyHint = renderPackFacetVocabulary(input.facet_vocabulary, input.target_facets, input.pack_id);
  const runtimeInputNotes = renderInputEvidenceNotes();

  // Default-on for `verbose_facet_disambiguation`: A/B testing on the
  // 40-item playtest sample showed it carries production accuracy from
  // ~50% (lean prompt) to ~93% on hard categories (doors, beds, rails,
  // spawn eggs, Block-of-X). It's principle-based, not item-enumeration,
  // so it generalizes to novel items. Caller can flip it OFF for
  // experimentation by passing `verbose_facet_disambiguation: false`.
  //
  // Default-off for `verbose_common_misconceptions`: that's the
  // item-level checklist of past LLM failures. Useful when a new
  // category surfaces a category-wide regression and we want to nudge
  // the LLM with explicit cases, but routinely keeping it on biases
  // toward pattern-matching enumerated examples instead of trusting
  // the principle.
  const extras = input.prompt_extras ?? {};
  const includeDisambiguation = extras.verbose_facet_disambiguation ?? true;
  const includeMisconceptions = extras.verbose_common_misconceptions ?? false;
  const sections: string[] = [
    SYSTEM_PREAMBLE,
    "",
    runtimeInputNotes,
    "",
    "# Facet schema",
    schemaDoc,
  ];
  if (facetVocabularyHint) {
    sections.push("", facetVocabularyHint);
  }
  if (includeDisambiguation) {
    sections.push("", FACET_DISAMBIGUATION);
  }
  if (includeMisconceptions) {
    sections.push("", "# Common misconceptions to avoid", COMMON_MISCONCEPTIONS);
  }
  sections.push("", "# Expected output shape", outputShape);
  const system = sections.filter((section) => section.length > 0).join("\n");

  const user = [
    neighborsNote,
    "# Items to classify",
    JSON.stringify({ items: input.items }, null, 2),
    "",
    renderFinalUserChecklist(input),
  ]
    .filter((section) => section.length > 0)
    .join("\n");

  return { system, user };
}

function renderFinalUserChecklist(input: LlmPromptInput): string {
  const hasFacetVocabulary = !!input.facet_vocabulary && Object.keys(input.facet_vocabulary).length > 0;
  const hasSubsystemVocabulary = !!input.facet_vocabulary?.mod_subsystem?.length;
  const lines = [
    "# Final response checklist",
    "- Respond with one strict JSON object matching the expected output shape above. No markdown, no prose, no comments.",
    "- Include every item id from `items` exactly once. If output gets long, omit optional rationale/evidence fields before dropping facets or items.",
    "- `schema_proposals` and `vocabulary_proposals` are top-level arrays only. Never put them inside `<item_id>.facets`; every key inside `facets` must be a real facet id.",
    "- Use `ambiguous: true` only for single-value enum/free_text facets. Never put `ambiguous` on multi-value facets such as `origin`, `activity`, `organization_group`, or `mod_subsystem`.",
    "- Pick `role` from the player's storage-home mental model, not from recipe participation. Machine parts, machine components, hulls, casings, pumps, presses, pipes, cables, and placed processing parts are mechanisms or functional blocks, not generic materials, even when they are ingredients.",
    "- Keep `form` separate from `role`. `form` is the physical shape/form factor (`ingot`, `stairs`, `tool`, `whole_block`, `special`); values like `functional_block`, `mechanism`, `redstone_component`, `container_portable`, and `material` are roles, not forms.",
    "- Keep high-value inventory semantics first: `role`, `primary_uses`, and `carry_frequency` should be present unless the item data is genuinely unusable. Then use judgment to fill useful semantic/query facets; do not be timid just because the evidence is inferential.",
    "- `document_context` is input evidence only. Never output it as an item facet.",
  ];
  if (hasFacetVocabulary) {
    lines.push(
      "- Vocabulary-backed facets should use values listed for that exact facet in `Pack facet vocabulary` whenever they fit. If no listed value fits and a useful value matching the facet grammar is needed, emit it inside `facets` with `vocab_review: true` and a short `rationale`; optionally add a matching `vocabulary_proposals` entry for later vocabulary cleanup. Copy listed values exactly as printed; do not rewrite slashes or underscores.",
      "- Vocabulary aliases are matching hints, not output values. If a listed value has aliases, you may use the alias to recognize the item, but the facet value must still be the listed usable value.",
      "- Values marked `state: review` in Pack facet vocabulary are usable by default; the marker is only a watchlist/debugging flag. Use them when they fit.",
      "- For semantic/query vocabulary-backed facets such as `activity`, `workflow`, `workflow_role`, `used_at`, `progression_stage`, `material_process_stage`, food facets, container facets, and use affordances, prefer high recall: emit plausible listed values from recipes, quests, advancements, guide text, tags, names, and neighboring context. A good inferred value is better than an empty facet.",
      "- Keep vocabulary-backed values in the right facet. `used_at` is a physical station, machine, tool, or surface; workflow values such as `casting` or `panning` belong under `workflow`, not `used_at`, unless that exact value is listed under `used_at`. `activity` uses the listed broad player activities; ordinary crafting can live in `primary_uses` or a workflow rather than an invented near-miss value such as `crafting`.",
      "- Do not move values across vocabulary-backed facets. A good `mod_subsystem` value such as `kinetics` is not an `organization_group` unless that exact value is listed under `organization_group`; use the subsystem facet and choose the best listed organization home separately.",
      "- For `organization_group`, emit exactly one value for every item when the facet is targeted. Use listed built-in homes such as `storage`, `lighting`, `crops`, `ceramics_molds`, `redstone`, `metal_stock`, `ores_raw_stock`, and `materials` when they are the right home; use reviewed pack-specific groups when they are a better broad home. Use an unlisted organization group only when the listed homes would be genuinely misleading, and mark it with `vocab_review: true`. Do not use organization groups as mod filters, rock/material taxonomy, material form/state splits, workstation-specific processes, or other query-only views.",
      "- If a missing broad player-maintained storage bucket is worth using, use a concise lower_snake value, include a short rationale citing the sibling/evidence family, and add a top-level `vocabulary_proposals` entry when practical. Do not propose singleton quirks, mod filters, rock taxonomy, material-state splits, or one-machine workflow views.",
    );
  } else {
    lines.push(
      "- No usable Pack facet vocabulary is supplied. Still classify useful vocabulary-backed facets such as `activity`, `workflow`, `used_at`, `organization_group`, and `mod_subsystem` when the value matches the facet grammar; mark those entries with `vocab_review: true` and add `vocabulary_proposals` when practical.",
    );
  }
  if (!hasSubsystemVocabulary) {
    lines.push(
      "- No usable subsystem vocabulary is supplied for this batch. If the item clearly belongs to a meaningful subsystem, emit the `mod_subsystem` value with `vocab_review: true` and add a `vocabulary_proposals` entry when practical.",
    );
  } else {
    lines.push(
    "- Emit listed `mod_subsystem` values when the item itself belongs to a listed subsystem. Recipe participation alone is weak evidence, but names, tags, guide text, component families, installed parts, cables, terminals, machines, modules, and neighboring items can justify a subsystem call.",
    );
  }
  lines.push(
    "- Organization group is required when targeted, but new organization-group values are precision-first. For other semantic/query facets, use reasonable judgment; low-evidence but plausible metadata is better than leaving useful listed vocabulary unused.",
  );
  return lines.join("\n");
}

function renderInputEvidenceNotes(): string {
  return `# Input evidence notes

Treat present runtime evidence as pack-specific truth for this exact modpack,
including KubeJS and datapack recipe/tag changes. Some batches may also include
static jar facts. Use the fields that are present; do not infer that omitted
fields are semantically absent.

- Omitted payload fields mean "no useful collected evidence in this prompt",
  not "semantically absent." Do not turn missing loot/model/creative-tab fields
  into negative evidence.
- Recipe presences, counts, and recipe-type names are strong evidence for this
  pack. Recipe absences are weaker: some custom recipe classes expose only a
  primary result through the runtime API, so missing output examples do not
  prove the item has no secondary/custom outputs.
- For \`origin\`, use positive acquisition evidence and player relevance rather
  than mechanically filling a value for every craftable item. Empty loot/source
  fields are not evidence. Positive production-recipe evidence can justify
  \`crafted_only\` when that acquisition route is distinctive or player-relevant;
  it does not make \`crafted_only\` useful for every ordinary craftable material,
  container, machine, or component. Rationales like "no loot source", "crafted
  from components", or "crafted from materials" are invalid unless a concrete
  output recipe id/count is present.
- Runtime resolved tags include helper and compat tags. Treat technical tags
  like \`c:hidden_from_recipe_viewers\`, \`buildinggadgets2:deny\`,
  \`tacz:*\`, \`*_whitelist\`, \`*_blacklist\`, and generic mineability tags
  as weak implementation context unless they clearly match player-facing
  semantics.
- Ignore Minecraft formatting codes in display names (for example \`§b\`).
  The visible words still matter; the color/style code usually does not.`;
}

/**
 * Reasoning notes for the trickiest facet distinctions. These are
 * principle-led: each section gives the player-perception question to
 * ask and one or two anchor examples, NOT a list of items per category.
 * The rules generalize to novel items by teaching the reasoning rather
 * than enumerating cases.
 */
const FACET_DISAMBIGUATION = `# Facet disambiguation (read before emitting)

These notes give the *reasoning* behind tricky facet calls. Apply the
principle, then sanity-check with the cardinal-rule test ("where would
a player expect to find this?"). The goal is to think like a player
organizing items, not to match items to a list.

## role: where does the player put this in their inventory?

The role facet sorts every item into a single home. The framing
question: **"if the player held this and asked which section or home it
belongs in, what's the answer?"**

### Inventory-side vs placement-side

The biggest single source of role mistakes is choosing a placement
role (\`building_block\`, \`decorative_block\`, \`functional_block\`,
\`storage_block\`) for an item the player thinks of as inventory-side
(\`material\`, \`utility\`, \`ammunition\`, \`consumable\`). Items that
are technically placeable but spend most of their life in a player's
inventory or hotbar belong on the inventory side.

- Test: *"if a player had 32 of these, what's the next thing they
  do?"* If the answer is "deploy / craft / spend / drink / shoot,"
  it's an inventory-side role. If the answer is "place once and
  forget," it's a placement-side role.
- Anchors:
  - **Torch**: placeable lighting, but the player's mental model is
    "thing I bring to caves" — stacks of 64 deployed disposably while
    exploring. Inventory dominates. → \`utility\`, not
    \`decorative_block\` or \`functional_block\`. The same logic
    applies to **ladders** (carried and deployed for traversal).
  - **Buckets-of-X** (water, lava, fish, mob, powder snow, empty bucket):
    the player wields these as tools — scoop, pour, deliver. → \`utility\`.
    The exception is \`milk_bucket\` which is \`consumable\` because the
    player drinks it. Buckets are NOT \`container_portable\` —
    \`container_portable\` is for open-and-put-items-in pouches like
    bundles or ender_pouches, not for tool-wielded fluid carriers.
  - **Ingredient-stage variants** of building blocks (\`*_concrete_powder\`,
    \`packed_mud\`, \`clay_ball\`): the player stacks them in inventory
    waiting to craft into the placed final form (\`*_concrete\`,
    \`mud_bricks\`, \`bricks\`). → \`material\`. The crafted final form is
    the placement-side block.

### natural_resource vs material

- \`natural_resource\` — the player **plants it or places it as living
  nature**. The mental tag is "garden / forest" (saplings, flowers,
  kelp, mushrooms, sugar_cane).
- \`material\` — the player **keeps it in their crafting stash**: refined
  ingredients (ingots, dyes), mob drops they craft with (feather,
  leather, blaze_rod), raw chunks (raw_iron), and compressed material
  blocks (\`iron_block\`, \`diamond_block\` — these are 9× the base material,
  not containers). The mental tag is "stuff I use in a recipe."
- Ore blocks and ore variants (\`*_ore\`, deepslate ore, modded stone ore,
  raw ore blocks) are also \`material\`, not \`natural_resource\`. They are
  mined and processed into crafting stock; players store them with metals /
  minerals, not with plants or living nature.
- Test: *garden or crafting stash?*
- Anchor: \`raw_iron\` is mined and smelted; players store it next to
  ingots, not next to saplings. → \`material\`.
- "How was it obtained?" is the wrong test. Players sort by where they
  USE the item, not by its origin. A blaze_rod comes from a mob and
  becomes blaze_powder — it lives in the crafting stash.
- **Single-narrow-purpose crafting inputs are \`material\` even when
  individually rare-feeling.** disc_fragment (only crafts a music
  disc), glistering_melon_slice (only brews potion of healing). The
  player accumulates them as crafting stock and spends them; they're
  not trophies to display. Test: *"if I have a stack of these, am I
  planning to display them or to craft them?"* If craft → \`material\`.

  **Pottery sherds are an exception** — they come in 20+ archeology
  patterns and players think of them as a *collected set* (like spawn
  eggs / banner patterns). Their craft sink (decorated_pot) is
  decorative-only and rare. → \`curiosity\`. The "set of patterns I've
  found" mental model dominates over "crafting stock." Same for any
  modded find-a-set archeology items.

### building_block vs decorative_block vs functional_block

When a player places this, what are they DOING?

- **Building** the structure → \`building_block\`. Walls, floors, roofs,
  fences, doors and trapdoors of any material, planks, stairs, slabs,
  bricks, glass. Doors are interactive but they're part of the
  building's openings — they're structural, not workstations.
- **Decorating** for looks → \`decorative_block\`. Banners, carpets,
  paintings, candles, heads, beds, decorated pots, item frames, flower
  pots. Beds are interactive (sleep) but the player puts one per home
  and treats it as bedroom decor; that's decorating, not operating.
- **Operating it to perform a task** → \`functional_block\`. The player
  walks up to a **single block**, opens a **UI**, and performs a craft
  / smelt / brew / enchant / repair / bake / read / play-record. Anvil,
  furnace, smithing_table, enchanting_table, lectern, jukebox, beacon,
  brewing_stand. The shared shape: it appears in JEI/EMI as a
  workstation icon. The strict tests are *single-block* AND *opens a
  UI* — failing either disqualifies the item.
- Test: place the item; what verb describes what the player just did?
  *Built / decorated / went to work at it.*
- Anchor: doors — oak / iron / oxidized_copper alike — share role
  because they're all openings in the building's envelope. Don't let
  the metal prefix flip the role; the family is structural.

### Processing-machine PARTS vs workstations (Create / similar tech mods)

A common LLM failure is reaching for \`functional_block\` whenever an
item participates in a processing recipe. Most Create-style processing
blocks are NOT workstations — they're kinetic / power-transmitting /
recipe-input components the player chains together to form a
multi-block contraption. The player interacts with the system, not
with each block individually.

- \`mechanism\` — kinetic / power-transmitting / processing-input parts
  the player builds INTO a multi-block contraption. The block has no
  single-block UI in vanilla NEI/EMI sense; recipes route through it
  because it sits in a processing line. Examples: basin, mechanical_press,
  mechanical_mixer, mechanical_fan, crushing_wheel, deployer,
  mechanical_saw, mechanical_drill, mechanical_harvester, mechanical_plough,
  encased_fan, portable_storage_interface, portable_fluid_interface,
  portable_energy_interface, electrical connectors, accumulators,
  industrial_fan, item_drains, weighted_ejector, smart_chute. All
  \`mechanism\` — none are \`functional_block\`.
- \`functional_block\` is rare in Create — the genuine workstation
  examples are blocks the player walks up to and opens a configuration
  UI on (e.g., \`mechanical_crafter\` faces accept a recipe slot,
  \`schematic_table\` opens a schematic-load UI). When in doubt, prefer
  \`mechanism\` over \`functional_block\` for any block whose primary
  role is "step in a processing line."
- Test (more strict than the generic functional_block test): *can I
  place this single block, walk up to it alone, right-click, and see a
  UI that lets me perform a one-shot craft?* If the block needs a
  contraption-mate (a basin under a mixer, a fan blowing through
  something, a press above a basin) to do anything, → \`mechanism\`.

### Vanilla edge cases that are NOT workstations

- **Lightning rod**: emits a redstone signal when struck by lightning.
  Player interacts via the redstone wire it powers, not a UI on the
  rod itself. → \`redstone_component\`, not \`functional_block\`.
- **Hanging signs (every variant — oak / spruce / birch / jungle /
  dark_oak / acacia / cherry / mangrove / bamboo / crimson / warped)**:
  one-shot text-edit interaction at place-time, like regular signs.
  Players treat them as decoration. → \`decorative_block\`, not
  \`functional_block\` and not \`building_block\`.
- **Pointed dripstone** and **dripstone block**: cave nature; pointed
  dripstone grows / falls / acts as a spike trap. → \`natural_resource\`
  for pointed_dripstone (organic-stalactite that grows in caves);
  dripstone_block is a building_block (placeable terrain block crafted
  from pointed pieces).

### storage_block

- \`storage_block\` — a placeable container the player **opens** and
  puts OTHER items inside. Chest, barrel, shulker_box, ender_chest,
  drawers. Opens into a slot grid in the UI.
- Test: *can the player open this and see other items inside?*
- Compressed material blocks (\`iron_block\`, \`diamond_block\`) FAIL this
  test — they're 9× a base material via crafting, not containers.
  → \`material\`.

### transport

- \`transport\` — the item **moves the player or items through the
  world**. Rails of every kind, minecarts, boats, saddles, lead, elytra
  (also armor — emit ambiguous), horse_armor.
- Test: *is this part of getting around or hauling things?*
- Anchor: powered_rail / detector_rail / activator_rail are powered by
  redstone, but their job is the rail network — players store them
  with the rest of the rails, not with redstone components.
- Wearable mobility gear follows the same mental model. Elytra, jetpacks,
  gliders, flight packs, and space-flight packs are transport-first if their
  player-facing purpose is movement. If they also occupy an armor slot and
  provide meaningful protection, emit the ambiguous role shape
  \`values: ["armor", "transport"], ambiguous: true\` rather than forcing
  them into plain \`armor\`.

### upgrade — applied to other items in a UI

\`upgrade\` covers items the player **applies to another item to enhance
it** in a smithing-table / anvil / enchanting-style transformation.
Each one is consumed on a single upgrade event; the player keeps a
small accumulating stash of them.

- **Smithing templates** — netherite_upgrade_smithing_template and
  every armor-trim template (coast, dune, eye, host, raiser, rib, sentry,
  shaper, silence, snout, spire, tide, vex, ward, wayfinder, wild,
  bolt, flow). Even though trim templates come in many varieties,
  the player's mental model is "my upgrade stash for smithing-table
  use," not "a display set." → \`upgrade\`.
- Tool / armor / storage enhancement modules from mods (upgrade modules,
  socketed upgrade items, netherite-style tier-bump items).
- Test: *does the player put this item into another item's UI to
  enhance it?* If yes → \`upgrade\`. If they only look at it / display
  it / read it → \`curiosity\`. If they wear / wield / consume it
  themselves → not upgrade.
- Anchor: a smithing template is consumed in the smithing table
  alongside diamond armor + a netherite ingot to upgrade the armor.
  That's the upgrade verb. Don't be misled by "many varieties exist"
  — variety doesn't make something a curiosity; **what the player DOES
  with it** does.

### curiosity vs trophy vs utility vs admin

- \`trophy\` — a **single iconic item from a hard-won fight or rare
  achievement** the player keeps as a permanent display. Dragon_egg,
  wither_skeleton_skull. Nether_star also fits here even though it
  crafts a beacon — the player treats it as the trophy from defeating
  the wither and spends it once on a long-term build, not as routine
  crafting stock.
- \`curiosity\` — items the player **collects as a set or novelty**
  (multiple-of-many, accumulated over time, primarily for display or
  reference). Spawn eggs, music_discs, banner patterns, mob heads,
  written books, paintings the player rotates through.

  Smithing templates are NOT curiosities even though they come in many
  varieties — see \`upgrade\` below.
- \`utility\` — the player **keeps it around for a recurring helper
  job**. Not a tool (pickaxe-class), not a workstation. Inventory-side
  helpers carried for their function: shears, lead, name_tag,
  totem_of_undying, ender_pearl, bucket variants, torches, ladders.
- \`admin\` — the item **only appears in the worldgen / debug tab**, not
  any survival-creative tab. command_block, barrier, structure_block,
  jigsaw, debug_stick, light, structure_void.
- Test order:
  *Iconic single trophy?* → \`trophy\`.
  *Set the player collects?* → \`curiosity\`.
  *Recurring helper they keep handy?* → \`utility\`.
  *Debug-tab only?* → \`admin\`.
- Anchor for trophy vs curiosity: dragon_egg is a one-of-a-kind boss
  reward (\`trophy\`); spawn eggs come in 80+ varieties players
  accumulate (\`curiosity\`).
- Anchor for trophy vs material: nether_star crafts a beacon, but the
  player's mental relationship is "I beat the wither — keeping this"
  until the rare moment they commit to a beacon. → \`trophy\`, not
  \`material\` (despite the crafting use).

## Consistency within material_family

When two items share a \`material_family\` (e.g. \`wood_oak\`), they
should share a role unless one variant fundamentally functions
differently. Players who organize by material want the whole family
in one section, not scattered across roles.

- Test: *would a player be surprised to see this variant on a
  different section from its siblings?* If yes, align it.
- Anchor: an oak family has its planks, stairs, slabs, walls, fences,
  doors, trapdoors, logs, and stripped logs all in one section.
  → all \`building_block\`. The exceptions are variants that genuinely
  belong elsewhere: \`oak_sapling\` (planted plant → natural_resource),
  \`oak_button\` (redstone trigger → redstone_component), \`oak_boat\`
  (transport).

## form: physical shape, not purpose

\`form\` describes the item's visible or inventory-recognizable shape:
ingot, nugget, dust, stairs, slab, whole_block, tool, weapon, armor_piece,
bottle, bucket, seed, sapling, vehicle, special. It is a physical/form-factor
facet, not another home or purpose facet.

Do not put role words into \`form\`. \`functional_block\`, \`storage_block\`,
\`mechanism\`, \`redstone_component\`, \`container_portable\`,
\`building_block\`, and \`material\` are role values. For a placed machine or
controller whose form is not one of the named block variants, use
\`form=whole_block\` or \`form=special\` and put the player-purpose judgment in
\`role\`, \`workflow\`, \`used_at\`, and \`mod_subsystem\`.

## tier

Apply to tools / weapons / armor (their progression rung) and to the
raw materials those rungs are MADE from (iron_ingot, diamond,
netherite_ingot all carry tier=iron/diamond/netherite). Items not
part of the tool-tier ladder don't get a tier — emerald, redstone,
lapis, amethyst_shard.

## primary_uses — what the current item is for

Primary uses are one to three short phrases for why a player keeps **this exact
item** around. They are not a summary of every neighboring item or every
recipe page that mentioned it.

- If a guide page says "bowls can make salads, salt meat, or apply powder
  to glass", those uses belong to bowls. A related powder item should get
  the powder's actual use, not every use of the bowl.
- If a recipe/process produces the item, that is not automatically a
  primary use of the item. An ingot refined from a bloom is used for
  smithing/crafting metal goods, not "smelting tools and armor."
- For broad ingredients, prefer the dominant player-facing sink. If the
  item has many unrelated recipe examples, keep the phrase general
  ("crafting ingredient", "metal stock") instead of cherry-picking a
  random recipe.
- Never emit more than three \`primary_uses\` values. Merge or omit the
  weakest use when several are true.

## activity — what you DO with the item

Pick activities the player **actively performs with the item**, not
activities the item is an ingredient of.

- A pickaxe gets \`mining\`. A diamond does not — diamonds are crafted
  INTO pickaxes; the activity belongs to the tool.
- An ingredient can carry an activity only when its dominant downstream
  player-facing purpose is narrow and obvious. A feather mostly becomes
  arrows, so → \`combat\`; a diamond has too many possible uses, so
  it should not inherit \`mining\` merely because it can become a
  pickaxe. Crafting itself is never an activity (every item is craftable;
  the value would be noise).
- Use judgment. It is fine to infer an activity from recipe clusters,
  guide context, tags, names, or a narrow downstream purpose. One or two
  plausible activities are better than silence; just avoid dumping every
  activity that appears somewhere in a long recipe list.

## workflow, used_at, and workflow_role — process semantics

These three facets answer different questions:

- \`workflow\`: which recognizable player-facing process this item
  participates in.
- \`used_at\`: where the player uses, inserts, processes, places, or
  operates this item. Prefer the downstream / next-use station for
  intermediate items.
- \`workflow_role\`: the item's role inside that workflow, scoped as
  \`<workflow>#input\` or \`<workflow>#output\`.

When the usable vocabulary contains a matching process, station, or
role value, use it even when the evidence is inferential from recipes,
tags, names, or nearby guide context rather than an exact tooltip
sentence. If the usable vocabulary does not contain the process or
station a player would expect, emit a concise unlisted value with
\`vocab_review: true\` and a short rationale; add a \`vocabulary_proposals\`
entry when practical so the vocabulary can absorb it later.

Do not set \`used_at\` to the recipe/process that only produced the
item. If an item is made by process A and then fired, inserted, worked,
or consumed by process B, \`used_at\` is B. The fact that process A
produced the item belongs in \`workflow\` and \`workflow_role\` only
when that relationship is useful to the player.

These are semantic/query facets, not main-wall storage homes. Favor
recall. If the item is clearly an input, output, tool, station, catalyst,
container, or product in a listed workflow, emit the listed workflow
and scoped workflow_role. If the item is normally used at a listed
station, emit used_at. Reasonable inferences from recipe ids, recipe
types, guide snippets, and item names are intended.

## progression_stage — where this sits in pack progression

Use listed progression_stage values when the item is characteristic of
a pack age, voltage tier, technology phase, or quest/progression gate.
This facet is also semantic/query metadata, so favor useful judgment over
silence.

Strong signals include advancement/quest/guide text, tier labels, voltage
names, material names, recipe chains, machine tiers, and obvious pack ages
such as copper/bronze/iron. You do not need a sentence saying "this item
is in the Iron Age" if the usable vocabulary contains \`iron_age\`
and the item is wrought iron, an iron anvil input, or an iron-age tool
or machine. Use the listed stage when a player would reasonably think
of the item as belonging to that stage.

## organization_group — where would a player put this item?

The \`organization_group\` facet is SLOT's direct primary-home signal for
large modpacks. It answers:

> "If a skilled player had only a small number of broad main-wall
> sections for this pack, which named storage section would this item
> belong in?"

When this facet is targeted, every item must get exactly one
\`organization_group\` value. Prefer the listed built-in
homes such as \`storage\`, \`metal_stock\`, \`tools\`,
\`lighting\`, and \`miscellaneous\`, plus reviewed pack-specific
splits such as beekeeping, glass products, papermaking, or weaving/cloth.
Use the exact values printed in Pack facet vocabulary when they fit; use an
unlisted value with \`vocab_review: true\` only when the listed homes would be
genuinely misleading.

Built-in groups are good player homes, not failures. If a portable
container belongs in Storage, emit the listed Storage value. If a lamp belongs
in Lighting, emit the listed Lighting value. If a crop belongs in Crops, emit
the listed Crops value. Do not leave \`organization_group\` empty just because
the right answer is a built-in section.

Use reviewed pack-specific groups when they are a better broad home than a
default. Think primarily in terms of item type or role; use case, material
state, or workflow context can refine a broad type, but must not become the
main reason to split related items. Beekeeping, glass products, papermaking,
weaving/cloth supplies, masonry supplies, cooking supplies, and reagents can
be good pack-specific homes when the usable vocabulary lists them and the
item is a core member.

\`materials\` and \`miscellaneous\` are last-resort homes. Prefer a
narrower listed default or reviewed pack-specific group when one fits. If
many items would land in Materials and evidence supports a broad human
storage split, still assign the best existing home for this item and add a
top-level \`vocabulary_proposals\` entry for the missing group. Do not put the
unlisted value in \`facets\` unless the listed homes would be genuinely
misleading; if you do use it, set \`vocab_review: true\` and keep the
rationale short.

The vocabulary review step spends the scarce custom section slots. A real
manual wall only supports about 15-20 human-named sections before it becomes
worse than unsorted storage, so do not propose new custom groups casually.
When proposing a missing custom organization group, use a concise lower_snake
value, include a concise human label, and explain the broad
sibling family it would keep together.

Do not propose custom groups that merely duplicate built-in homes:
\`crops\`, \`pottery\`, \`redstone\`, \`item_containers\`, \`lamps\`,
\`wood\`, \`seeds\`, or \`tools\` are already covered by default homes unless
the proposal is a distinct broad overloaded-parent split.

The main wall is not a faceted search result. Do NOT emit
\`organization_group\` for categories that merely answer a query such
as "which mod did this come from?", "which rock type is this?", "which
material property or form/state is this?", "which narrow recipe
mechanic/tag is this in?", or "which workstation-specific process is
this for?". Those can be useful for search, filters, task views, or
within-section sorting, but they actively fragment the primary
inventory list.

This facet is intentionally different from \`mod_subsystem\`.
\`organization_group\` is allowed on materials, utility items,
building blocks, natural resources, and intermediate crafting items
when those items form a broad player-recognizable storage bucket. Do NOT
create custom groups for singleton quirks, decorative style families,
color/material families, or generic catch-alls like \`<ns>:materials\`,
\`<ns>:crafting\`, \`<ns>:blocks\`, \`<ns>:misc\`.

Do not withhold the facet just because the item has a narrow form like
\`ingot\`, \`gem\`, \`raw ore\`, \`stairs\`, \`slab\`, \`tool\`, or
\`armor\`; choose the listed group a player would use for that item, or mark
an unlisted group with \`vocab_review: true\` if the listed homes would
misplace it.
If a reviewed custom bucket would split obvious siblings across two main
wall sections, choose the built-in home instead.

Concrete anchors use usable vocabulary labels, not literal ids:
- Bee, hive, honey, wax, or apiary supplies in a beekeeping-heavy pack →
  use the listed Beekeeping organization value if it is listed. Honey or
  wax still count when packaged as bottles, buckets, combs, or blocks;
  the Storage exception is for actual storage containers, not product
  packaging.
- Glass blocks, panes, bottles, vials, and glassware in a glass-heavy pack →
  use the listed Glass Products organization value if it is listed.
- Reusable cookware, utensils, bowls, pots, knives, and food-prep tools →
  use the listed Cooking Tools / Cooking Supplies organization value if listed.
- Cloth, thread, string, fabric, cheesecloth, weaving, and sewing supplies →
  use the listed Weaving/Cloth organization value if listed.
- Paper, books, maps, papyrus, and bookmaking supplies →
  use the listed Papermaking organization value if listed.
- Portable or placeable item containers → use the listed Storage value.
- Lamps, lanterns, and light-source blocks → use the listed Lighting value.
- Crop produce, pottery/mold stock, or redstone components → use the listed
  Crops / Ceramics & Molds / Redstone values.
- A plain metal ingot with no workflow-specific storage expectation → use the
  listed Metal Stock value.
- A material state, block-form variant, or individual rock-type subgroup →
  use the existing Metal Stock / Ores & Raw Stock / Stairs / Building value or a
  broad reviewed custom materials group.
- A bucket named after one mod or one mod's mechanical-power subsystem →
  use the listed home for the item type; keep the mod/system detail in role,
  activity, mod_namespace, mod_subsystem, or search/query facets.
- Stackable plates → use the listed Metal Stock value; "stackable plates" is a
  material property, not a custom organization group.
- Anvil smithing → use the listed Metal Stock, Materials, or reviewed
  metalworking-supplies home; "anvil smithing" is a process view, not a custom
  main-wall group.

## mod_subsystem — what part of the mod IS this item

The \`mod_subsystem\` facet groups items by the **functional sub-area
of the mod they themselves belong to** — a mechanical-power network, a
logistics network, a storage-upgrade module set. The question is
**identity**, not **interaction graph**.
The short version: \`mod_subsystem\` is identity, not namespace or recipe participation.

\`mod_subsystem\` is semantic/query metadata, not a main-wall home. It
does not create storage sections and should not inherit
\`organization_group\`'s strictness. When a listed subsystem value
describes the item itself, use it.

The main failure mode is assigning a subsystem solely because one of
that subsystem's recipes consumes or produces the item. A
\`golden_sheet\` may be *processed by* a press, but in the player's
inventory it is usually a refined metal sheet, not a press-network part.
Recipe participation alone is weak evidence; names, tags, guide text,
component families, neighboring items, and installed/operated behavior
can turn that into a good subsystem call.

The test, applied per item:

> "Is this item itself a member of the named subsystem — a part the
> player installs / wields / configures / chains into the subsystem's
> machinery — or does it merely appear as an ingredient/output of
> recipes the subsystem owns?"

Favor useful recall when the answer is "member of the subsystem." Parts,
blocks, tools, cables, pipes, tracks, terminals, controllers, machines,
upgrades, modules, interfaces, and branded components can all qualify
when the usable vocabulary contains the matching subsystem. Generic
materials, foods, decorative variants, building blocks, natural
resources, ordinary tools, weapons, armor, and curiosities usually do
not need \`mod_subsystem\` unless their own name, tooltip, tags, or guide
context says they are subsystem-specific.

Cross-check: \`mod_subsystem\` is semantic/query identity evidence, not a
main-wall home. The runtime does not auto-create wall sections from
\`mod_subsystem\`; use \`organization_group\` only when a broad storage
section passes that facet's stricter storage-section rule. Role is a cross-check,
not a hard ban: an item can have role \`storage_block\`, \`functional_block\`,
\`transport\`, or \`mechanism\` and still belong to a listed subsystem when it
is itself an installed/operated part of that system.

If no usable \`mod_subsystem\` vocabulary is present, emit a meaningful
subsystem value with \`vocab_review: true\` when the item clearly belongs to
one, and add a \`vocabulary_proposals\` entry when practical. If usable
subsystem vocabulary is present, use the listed value when it fits the item.
Avoid narrow one-off labels for equipment sets,
material families, tool families, or individual equipment lines unless the
usable vocabulary explicitly defines that subsystem. Use \`role\`,
\`tier\`, \`material_family\`, and \`primary_uses\` for those details instead.

Concrete anchors:
- \`examplemod:cogwheel\` IS a mechanical_power part the player chains
  into contraptions. → mod_subsystem=mechanical_power.
- \`examplemod:mechanical_press\` IS a processing machine. →
  mod_subsystem=processing.
- \`examplemod:storage_terminal\` IS the interface to a storage network. →
  role=functional_block or storage_block, and mod_subsystem=storage_network
  when that value is listed vocabulary.
- \`examplemod:rail_signal\` IS part of a train/rail network. →
  role=transport or redstone_component, and mod_subsystem=train
  when that value is listed vocabulary.
- \`examplemod:golden_sheet\` is a refined material output by the press.
  → no mod_subsystem; role=material.
- \`examplemod:honeyed_apple\` is a food consumable. → no
  mod_subsystem; role=consumable.
- \`examplemod:metal_girder\` is a building_block (the player builds
  with it as part of an industrial-aesthetic structure). → no
  mod_subsystem; role=building_block.
- \`examplemod:diamond_barrel\` is a storage_block sibling of ordinary
  chests. → no mod_subsystem unless the usable vocabulary has a broad
  storage-network/storage-upgrade subsystem it belongs to; role=storage_block.
- A smithing-style upgrade module from a storage mod: → mod_subsystem only
  when the usable vocabulary defines that storage-upgrade subsystem;
  role=upgrade.
`;

const COMMON_MISCONCEPTIONS = `These factual errors recur in LLM output — avoid them:

- **Netherite armor does NOT grant fire resistance status effect.** The
  \`minecraft:damage_resistant\` component (tag minecraft:is_fire) means the
  ITEM doesn't burn when dropped in lava — it's about item durability,
  not player protection. Do not add "fire resistance" to
  \`primary_uses\` of netherite armor.
- **Chainmail repairs with iron ingot**, but its \`material_family\` is
  \`iron\`, not \`chainmail\` (chainmail isn't a material — it's a tier name).
- **Gold armor gives piglin neutrality**, but only when WORN. Don't claim
  it "crafts gold" or similar derivatives.
- **"Abundant" rarity** is for items found by the chest in every world
  (cobblestone, dirt, wood). "Common" is for farmable staples (iron,
  wheat). Don't put iron at \`abundant\` or netherite at \`common\`.
- **Music discs**: role=\`curiosity\` (the plan explicitly says so), not
  \`decorative_block\` or \`utility\`.
- **Totem of undying**: role=\`utility\` (consumed on lethal damage; NOT
  \`trophy\` even though it's rare-ish).
- **Logs and wood (all species, all states)**: role=\`building_block\`,
  not \`natural_resource\` and not \`material\`. Logs are predominantly a
  crafting input (processed into planks/sticks/charcoal), but players
  group them with the rest of the wood family — planks, stairs, slabs,
  fences — in a single wood-themed section. Don't split logs off into a
  separate raw-materials or Nature category just because their dominant
  use is crafting. Same family rule applies across all states (log / wood
  / stripped_log / stripped_wood) and all species (oak / birch / spruce /
  jungle / dark_oak / acacia / cherry / mangrove / bamboo / crimson /
  warped). Saplings and bamboo-as-plant ARE \`natural_resource\` —
  they're organic and planted.
- **Stone variants**: role=\`building_block\`. cobblestone, mossy_cobblestone,
  cracked_stone_bricks, all stone-brick variants, deepslate variants —
  all \`building_block\`. Don't flip role between the base block and its
  cracked/mossy/chiseled siblings.
- **Doors / trapdoors / fence_gates of all materials**: role=\`building_block\`,
  not \`functional_block\`. They open and close, but their primary use is
  sealing a building. Includes iron_door, copper_door (every oxidation
  state, every wax state), and every wood species. Same family rule as
  wood/stone: don't flip role across siblings (a recurring failure mode
  has been e.g. exposed_copper_door=building_block but
  oxidized_copper_door=functional_block).
- **Beds (every color)**: role=\`decorative_block\`, not
  \`functional_block\`. Sleeping is a use, but the player has one bed
  per home — beds belong in the Decoration section.
- **Decorated pot**: role=\`decorative_block\`, not \`functional_block\`.
  It holds a single display item; it's a pot, not a workstation.
- **Rails (rail / powered_rail / detector_rail / activator_rail)**:
  role=\`transport\`, not \`functional_block\` and not
  \`redstone_component\`. The redstone-powered variants are part of a
  rail network; their primary purpose is moving minecarts.
- **Mob drops + raw ore chunks used as crafting ingredients**:
  role=\`material\`, not \`natural_resource\`. feather, leather, bone,
  string, slime_ball, blaze_rod, ghast_tear, magma_cream, gunpowder,
  phantom_membrane, prismarine_shard, prismarine_crystals, ink_sac,
  glow_ink_sac, nautilus_shell, rabbit_hide, armadillo_scute,
  turtle_scute, honeycomb, scute, raw_iron, raw_copper, raw_gold,
  ancient_debris — all \`material\`. natural_resource is reserved for
  placeable nature (saplings, leaves, flowers, crops, kelp, sugar_cane,
  bamboo, mushrooms, fungi).
- **Compressed material blocks (Block of X)**: role=\`material\`, not
  \`storage_block\`. iron_block, gold_block, diamond_block, emerald_block,
  lapis_block, coal_block, copper_block (every oxidation/wax state),
  netherite_block, raw_iron_block, raw_copper_block, raw_gold_block,
  amethyst_block, quartz_block — all \`material\`. \`storage_block\` is
  for container-UI blocks (chests, barrels, shulker boxes, drawers).
- **Spawn eggs (every mob)**: role=\`curiosity\`, not \`utility\` and not
  \`admin\`. They're creative-collectible.
- **Smithing templates (every variant — netherite_upgrade and every
  armor-trim template)**: role=\`upgrade\`, not \`curiosity\`. The player
  applies them in the smithing table to upgrade gear; they're consumed
  in a UI, not displayed as a collection. Storage/backpack mod
  *_upgrade items are also \`upgrade\`.
- **Storage-network parts (tom's storage \`storage_terminal\` /
  \`storage_output\` / \`inventory_proxy\` / \`inventory_connector\`,
  Sophisticated Storage controllers and links, Applied Energistics ME
  terminals and controllers, refined-storage grids)**: role=
  \`storage_block\` or \`functional_block\` — the player walks up to
  them and opens a UI to view, search, or move items. NOT
  \`mechanism\` and NOT \`redstone_component\`, even when they have
  redstone-driven IO. The shared mental model is "storage UI I open,"
  not "kinetic gizmo." Default to \`storage_block\` for the chest-like
  ones and \`functional_block\` for the workstation-like ones (the
  search terminal you stand at to look something up).
- **mod_subsystem is identity, not namespace or recipe participation**:
  use it only when the item itself is part of a listed subsystem
  vocabulary value. Cogwheels, funnels, tracks, terminals, cables,
  storage buses, train signals, and processing machines can qualify
  when the matching subsystem value is listed. A wood variant from a mod,
  an ingot from a mod, a food item from a mod, a decorative block from
  a mod, a smithing-template-style upgrade from a mod, or a chest
  variant that merely belongs to the mod namespace usually collapses
  into the cross-mod role-based pile. Don't tag those with a subsystem
  just because recipes connect them.
- **Elytra**: ambiguous between \`armor\` and \`transport\`. Emit
  \`ambiguous: true\` with \`values: [armor, transport]\` rather than picking
  one.
`;

function renderSchemaForPrompt(targetFacets: readonly string[]): string {
  const lines: string[] = [];
  for (const facetId of targetFacets) {
    const def = FACETS[facetId];
    if (!def) continue;
    lines.push(`## ${facetId}`);
    lines.push(`- kind: ${def.kind}`);
    lines.push(`- description: ${def.description}`);
    if (def.values) lines.push(`- allowed: ${def.values.join(", ")}`);
    if (def.pattern) lines.push(`- pattern: ${def.pattern.source}`);
    if (def.vocabulary_backed) {
      lines.push("- vocabulary-backed: use Pack facet vocabulary values when they fit; if no listed value fits and a useful value matching this facet pattern is needed, emit it with `vocab_review: true` and a short rationale, and optionally add `vocabulary_proposals`");
      lines.push("- examples omitted: vocabulary values are pack-specific; do not copy schema examples as usable values");
    } else if (def.examples) {
      lines.push(`- examples: ${def.examples.join(" | ")}`);
    }
    lines.push("");
  }
  return lines.join("\n");
}

function renderExpectedOutput(
  targetFacets: readonly string[],
  vocabulary: PromptFacetVocabulary | undefined,
): string {
  const exampleFacet = chooseExampleFacet(targetFacets, vocabulary);
  const exampleMulti = chooseMultiExampleFacet(targetFacets, vocabulary);
  const facets: Record<string, Record<string, unknown>> = {};
  if (exampleFacet) facets[exampleFacet.facet] = exampleFacet.entry;
  if (exampleMulti && exampleMulti.facet !== exampleFacet?.facet) {
    facets[exampleMulti.facet] = exampleMulti.entry;
  }
  const shape = {
    items: {
      "<item_id>": {
        facets,
      },
    },
    schema_proposals: [],
    vocabulary_proposals: [],
  };
  return [
    "Structure (pure JSON, no comments):",
    JSON.stringify(shape, null, 2),
    "",
    "Field rules:",
    "- Single-value facets (enum / free_text / boolean): `value: <scalar>`.",
    "- Multi-value facets (multi_enum / multi_free_text): `values: [<scalar>, ...]`.",
    "- Ambiguous single-value (enum / free_text only): `values: [a, b]` AND `ambiguous: true`. Never use `ambiguous` on multi-value facets.",
    "- Optional per-facet review fields: `rationale`, `evidence`, and `vocab_review: true`. Use `rationale` sparingly for non-obvious calls; use `vocab_review: true` when a vocabulary-backed facet uses an unlisted value.",
    "- `schema_proposals` (optional top-level array, default `[]`): use when an enum/multi_enum facet lacks an important allowed value, or when a genuinely new facet is needed. Each entry is `{kind: 'add_value', facet, value, rationale}` or `{kind: 'add_facet', name, suggested_kind, rationale}`. Do not use this for ordinary free_text values that already match the facet pattern.",
    "- `vocabulary_proposals` (optional top-level array, default `[]`): use when a vocabulary-backed facet needed an unlisted value. Each entry is `{item, facet, label, proposed_id, rationale, evidence}`. This is a review aid; still emit the value in `facets` with `vocab_review: true` when it improves the item classification.",
    "- The only allowed top-level keys are `items`, `schema_proposals`, and `vocabulary_proposals`; do not nest those review arrays under an item.",
  ].join("\n");
}

function chooseMultiExampleFacet(
  targetFacets: readonly string[],
  vocabulary: PromptFacetVocabulary | undefined,
): {
  facet: string;
  entry: Record<string, unknown>;
} | undefined {
  for (const candidate of ["primary_uses", "activity", "organization_group"]) {
    if (!targetFacets.includes(candidate)) continue;
    const def = FACETS[candidate]!;
    if (!canUseFacetInExample(candidate, def, vocabulary)) continue;
    return {
      facet: candidate,
      entry: exampleEntryFor(candidate, def, vocabulary),
    };
  }
  return chooseExampleFacet(targetFacets, vocabulary);
}

function chooseExampleFacet(
  targetFacets: readonly string[],
  vocabulary: PromptFacetVocabulary | undefined,
): {
  facet: string;
  entry: Record<string, unknown>;
} | undefined {
  // prefer `role` as the example because it's the most recognizable facet
  for (const candidate of ["role", "material_family", "primary_uses"]) {
    if (!targetFacets.includes(candidate)) continue;
    const def = FACETS[candidate]!;
    if (!canUseFacetInExample(candidate, def, vocabulary)) continue;
    return {
      facet: candidate,
      entry: exampleEntryFor(candidate, def, vocabulary),
    };
  }
  for (const fallback of targetFacets) {
    const def = FACETS[fallback];
    if (!def || !canUseFacetInExample(fallback, def, vocabulary)) continue;
    return {
      facet: fallback,
      entry: exampleEntryFor(fallback, def, vocabulary),
    };
  }
  return undefined;
}

function canUseFacetInExample(
  facet: string,
  def: FacetDef,
  vocabulary: PromptFacetVocabulary | undefined,
): boolean {
  return !def.vocabulary_backed || !!vocabulary?.[facet]?.[0];
}

function exampleEntryFor(
  facet: string,
  def: FacetDef,
  vocabulary: PromptFacetVocabulary | undefined,
): Record<string, unknown> {
  const isMulti =
    def.kind === "multi_enum" ||
    def.kind === "multi_free_text" ||
    def.kind === "multi_item_ref";
  if (def.vocabulary_backed) {
    const usableId = vocabulary?.[facet]?.[0]?.id;
    if (isMulti) {
      return {
        values: usableId ? [usableId] : [],
      };
    }
    return {
      value: usableId ?? "",
    };
  }
  if (isMulti) {
    return {
      values: def.values?.slice(0, 2) ?? def.examples?.slice(0, 2) ?? ["<value>"],
    };
  }
  if (facet === "role") {
    return {
      value: "material",
    };
  }
  return {
    value: def.values?.[0] ?? def.examples?.[0] ?? "<value>",
  };
}

export function buildPromptFacetVocabulary(
  vocabulary: PackFacetVocabulary | undefined,
  targetFacets: readonly string[],
): PromptFacetVocabulary | undefined {
  const target = new Set(targetFacets);
  const out: PromptFacetVocabulary = {};
  if (!vocabulary) return undefined;
  for (const facetId of [...target].sort((a, b) => a.localeCompare(b))) {
    const def = FACETS[facetId];
    if (!def?.vocabulary_backed) continue;
    const facet = vocabulary.facets?.[facetId];
    if (!facet) continue;
    const values: PromptFacetVocabularyValue[] = [];
    const existing = new Set<string>();
    for (const [id, value] of Object.entries(facet.values ?? {}).sort(([a], [b]) => a.localeCompare(b))) {
      if (!isUsableVocabularyState(value.state)) continue;
      if (existing.has(id)) continue;
      existing.add(id);
      values.push({
        id,
        label: value.label,
        ...(value.state === "review" ? { state: "review" } : {}),
        ...(value.description ? { description: value.description } : {}),
        ...(value.aliases?.length ? { aliases: value.aliases } : {}),
        ...(value.parent ? { parent: value.parent } : {}),
      });
    }
    if (values.length > 0) out[facetId] = values;
  }
  return Object.keys(out).length > 0 ? out : undefined;
}

function renderPackFacetVocabulary(
  vocabulary: PromptFacetVocabulary | undefined,
  targetFacets: readonly string[],
  packId?: string,
): string {
  if (!vocabulary || Object.keys(vocabulary).length === 0) return "";
  const lines: string[] = ["# Pack facet vocabulary"];
  lines.push(
    "For each facet listed below, these are the grounding values supplied to this classification batch. Values marked `state: review` are usable by default; the marker is only a watchlist/debugging flag. Use listed values for the matching facet whenever they fit the item. If a target vocabulary-backed facet has no section here, or if no listed value fits an item, you may emit a concise unlisted value in `facets` with `vocab_review: true` and a short rationale; add a top-level vocabulary_proposals entry when useful missing vocabulary should be reviewed later. Exception: when `organization_group` is targeted, built-in homes are listed here and every item should still receive the best organization_group value, with unlisted custom homes used only when the listed homes would be genuinely misleading. Labels/descriptions are guidance. Aliases are matching hints only, not output values. Output listed values exactly as printed.",
  );
  if (packId) {
    lines.push(
      `Pack id for review context: \`${packId}\`. Missing vocabulary proposals should use concise lower_snake values unless the value is a real registry/resource id.`,
    );
  }
  lines.push("");
  for (const facetId of targetFacets) {
    const values = vocabulary[facetId];
    if (!values || values.length === 0) continue;
    lines.push(`## ${facetId}`);
    for (const value of values) {
      const details: string[] = [];
      if (value.state === "review") details.push("state: review");
      if (value.description) details.push(value.description);
      if (value.aliases?.length) details.push(`aliases: ${value.aliases.join(", ")}`);
      if (value.parent) details.push(`parent: ${value.parent}`);
      lines.push(`- \`${value.id}\` — ${value.label}${details.length ? `; ${details.join("; ")}` : ""}`);
    }
    lines.push("");
  }
  return lines.join("\n").trimEnd();
}

function renderNeighborsSection(
  neighbors: Record<string, readonly string[]> | undefined,
): string {
  if (!neighbors || Object.keys(neighbors).length === 0) return "";
  const lines: string[] = ["# Nearest-neighbor priming"];
  for (const [itemId, summaries] of Object.entries(neighbors)) {
    if (summaries.length === 0) continue;
    lines.push(`## ${itemId}`);
    for (const summary of summaries) lines.push(`- ${summary}`);
  }
  lines.push("");
  return lines.join("\n");
}

/**
 * Project an extract record into the compact payload the prompt ships. Keeps
 * the per-item token cost bounded for big batches.
 */
export function buildItemPayload(
  record: ItemExtractRecord,
  // Retained while legacy runner plumbing still calls this API; never include
  // these facets in the prompt payload.
  _legacyFacets: Record<string, unknown> = {},
  documentContext?: readonly LlmDocumentContext[],
): LlmItemPayload {
  const components = record.component_data ?? {};
  const lore = extractLore(components);
  const componentHighlights = extractComponentHighlights(components);
  const processingIn = Object.keys(record.recipe_role.ingredient_of_counts ?? {}).sort();

  const tagMembership = tagMembershipMode(record);
  const directTags = tagMembership === "resolved_runtime"
    ? new Set<string>()
    : new Set(record.minecraft_tags_direct);
  const inheritedTags = tagMembership === "resolved_runtime"
    ? []
    : record.minecraft_tags.filter((t) => !directTags.has(t));
  const tagFields: {
    minecraft_tags_direct?: readonly string[];
    minecraft_tags_inherited?: readonly string[];
    minecraft_tags_resolved?: readonly string[];
  } = tagMembership === "resolved_runtime"
    ? {
        minecraft_tags_resolved: record.minecraft_tags,
      }
    : {
        minecraft_tags_direct: record.minecraft_tags_direct,
        minecraft_tags_inherited: inheritedTags,
      };

  const ingredientExamples = boundedExamples(
    record.recipe_role.ingredient_of,
    RECIPE_EXAMPLE_LIMIT,
  );
  const outputExamples = boundedExamples(
    record.recipe_role.output_of,
    RECIPE_EXAMPLE_LIMIT,
  );
  const lootExamples = boundedExamples(record.loot_table_sources, LOOT_SOURCE_LIMIT);
  const blockContext = extractBlockContext(record);

  const payload: LlmItemPayload = {
    id: record.id,
    display_name: record.display_name,
  };

  appendArray(payload, "minecraft_tags_direct", tagFields.minecraft_tags_direct);
  appendArray(payload, "minecraft_tags_inherited", tagFields.minecraft_tags_inherited);
  appendArray(payload, "minecraft_tags_resolved", tagFields.minecraft_tags_resolved);
  appendArray(payload, "processing_in", processingIn);
  appendRecord(payload, "recipe_consumption_by_type", record.recipe_role.ingredient_of_counts ?? {});
  appendRecord(payload, "recipe_production_by_type", record.recipe_role.output_of_counts ?? {});
  appendPositiveNumber(payload, "recipe_ingredient_count", record.recipe_role.in_degree);
  appendPositiveNumber(payload, "recipe_output_count", record.recipe_role.out_degree);
  appendArray(payload, "recipe_ingredient_examples", ingredientExamples);
  appendArray(payload, "recipe_output_examples", outputExamples);
  if (ingredientExamples.length < record.recipe_role.ingredient_of.length
    || outputExamples.length < record.recipe_role.output_of.length) {
    payload.recipe_examples_truncated = true;
  }
  appendArray(payload, "creative_tabs", record.creative_tabs);
  appendPositiveNumber(payload, "loot_source_count", record.loot_table_sources.length);
  appendArray(payload, "loot_source_examples", lootExamples);
  if (lootExamples.length < record.loot_table_sources.length) {
    payload.loot_sources_truncated = true;
  }
  if (blockContext) payload.block_context = blockContext;
  appendArray(payload, "lore", lore);
  const semanticText = semanticTextPayload(record);
  appendArray(payload, "semantic_text", semanticText);
  const documentPayload = documentContextPayload(record, documentContext);
  appendArray(payload, "document_context", documentPayload);
  appendRecord(payload, "component_highlights", componentHighlights);
  return payload;
}

function appendArray<K extends keyof LlmItemPayload>(
  payload: LlmItemPayload,
  key: K,
  value: LlmItemPayload[K] | undefined,
): void {
  if (Array.isArray(value) && value.length > 0) {
    payload[key] = value;
  }
}

function appendRecord<K extends keyof LlmItemPayload>(
  payload: LlmItemPayload,
  key: K,
  value: LlmItemPayload[K] | undefined,
): void {
  if (value && typeof value === "object" && !Array.isArray(value) && Object.keys(value).length > 0) {
    payload[key] = value;
  }
}

function appendPositiveNumber<K extends keyof LlmItemPayload>(
  payload: LlmItemPayload,
  key: K,
  value: number,
): void {
  if (value > 0) {
    payload[key] = value as LlmItemPayload[K];
  }
}

function boundedExamples(values: readonly string[], limit: number): readonly string[] {
  if (values.length <= limit) return values;
  if (limit <= 0) return [];
  if (limit === 1) return [values[0]!];

  const indexes = new Set<number>();
  for (let i = 0; i < limit; i++) {
    indexes.add(Math.round(i * (values.length - 1) / (limit - 1)));
  }
  for (let i = 0; indexes.size < limit && i < values.length; i++) {
    indexes.add(i);
  }
  return [...indexes]
    .sort((a, b) => a - b)
    .map((index) => values[index]!)
    .filter((value): value is string => typeof value === "string" && value.length > 0);
}

function extractBlockContext(record: ItemExtractRecord): LlmItemPayload["block_context"] {
  const meta = record.extractor_meta ?? {};
  if (meta["is_block_item"] !== true && typeof meta["block_id"] !== "string") return undefined;

  const rawTags = Array.isArray(meta["block_tags"])
    ? meta["block_tags"].filter((tag): tag is string => typeof tag === "string")
    : [];
  const blockTags = boundedExamples(rawTags, BLOCK_TAG_LIMIT);
  const out: NonNullable<LlmItemPayload["block_context"]> = {};
  if (typeof meta["block_id"] === "string" && meta["block_id"].length > 0) {
    out.block_id = meta["block_id"];
  }
  if (blockTags.length > 0) {
    out.block_tags = blockTags;
  }
  if (blockTags.length < rawTags.length) {
    out.block_tags_truncated = true;
  }
  if (typeof meta["block_requires_correct_tool"] === "boolean") {
    out.requires_correct_tool = meta["block_requires_correct_tool"];
  }
  return Object.keys(out).length === 0 ? undefined : out;
}

function tagMembershipMode(record: ItemExtractRecord): "direct_and_inherited" | "resolved_runtime" {
  const meta = record.extractor_meta ?? {};
  if (meta["direct_item_tags_available"] === false) return "resolved_runtime";
  if (meta["item_tag_membership"] === "resolved_runtime") return "resolved_runtime";
  return "direct_and_inherited";
}

function extractLore(components: Record<string, unknown>): string[] {
  const raw = components["minecraft:lore"];
  if (!Array.isArray(raw)) return [];
  return raw
    .map((entry) => (typeof entry === "string" ? entry : flattenTranslatable(entry)))
    .filter((s): s is string => typeof s === "string" && s.length > 0);
}

function semanticTextPayload(record: ItemExtractRecord): NonNullable<LlmItemPayload["semantic_text"]> {
  const seen = new Set<string>();
  const out: Array<{ source?: string; text: string }> = [];
  for (const entry of record.semantic_text ?? []) {
    const text = entry.text.trim();
    if (!text) continue;
    if (seen.has(text)) continue;
    seen.add(text);
    out.push({
      ...(entry.source ? { source: entry.source } : {}),
      text: text.length <= 2_000 ? text : `${text.slice(0, 1_997)}...`,
    });
    if (out.length >= 64) break;
  }
  return out;
}

function documentContextPayload(
  record: ItemExtractRecord,
  contexts: readonly LlmDocumentContext[] | undefined,
): NonNullable<LlmItemPayload["document_context"]> {
  const out: LlmPromptDocumentContext[] = [];
  for (const context of contexts ?? []) {
    const snippets = compactSnippetTexts(record, context);
    if (snippets.length === 0) continue;
    out.push({
      kind: context.kind,
      ...(context.label ? { label: context.label } : {}),
      ...(context.related_item_refs?.length ? { related_items: context.related_item_refs } : {}),
      snippets,
    });
  }
  return out;
}

function compactSnippetTexts(
  record: ItemExtractRecord,
  context: LlmDocumentContext,
): readonly string[] {
  const seen = new Set<string>();
  const out: string[] = [];
  for (const snippet of context.snippets) {
    const text = focusSnippetText(record, context, snippet.text.trim());
    if (!text || seen.has(text)) continue;
    seen.add(text);
    out.push(text);
  }
  return out;
}

function focusSnippetText(
  record: ItemExtractRecord,
  context: LlmDocumentContext,
  text: string,
): string {
  if (!text) return "";
  const tokens = itemFocusTokens(record);
  if (tokens.length === 0 || labelMatchesItem(context.label, tokens)) return text;
  const clauses = splitSnippetClauses(text)
    .map((clause) => clause.trim())
    .filter((clause) => clause.length > 0 && tokens.some((token) => clause.toLowerCase().includes(token)));
  if (clauses.length === 0) return text;
  const focused = clauses.join("; ");
  return focused.length <= 500 ? focused : `${focused.slice(0, 497).trimEnd()}...`;
}

function labelMatchesItem(label: string | undefined, tokens: readonly string[]): boolean {
  if (!label) return false;
  const normalized = label.toLowerCase();
  return tokens.some((token) => normalized.includes(token));
}

function itemFocusTokens(record: ItemExtractRecord): readonly string[] {
  const pathParts = record.path.split("/");
  const raw = [
    record.display_name ?? "",
    pathParts[pathParts.length - 1] ?? record.path,
  ].join(" ");
  const stop = new Set([
    "block",
    "item",
    "small",
    "large",
    "empty",
    "filled",
    "powder",
    "ingot",
    "nugget",
    "plate",
  ]);
  const seen = new Set<string>();
  const tokens: string[] = [];
  for (const part of raw.toLowerCase().split(/[^a-z0-9]+/)) {
    if (part.length < 3 || stop.has(part) || seen.has(part)) continue;
    seen.add(part);
    tokens.push(part);
  }
  return tokens;
}

function splitSnippetClauses(text: string): readonly string[] {
  return text.split(/(?<=[.!?])\s+|;\s+|,\s+|\s+\bor\s+to\s+/i);
}

/**
 * Pull semantic-signal components into a flat object for the prompt. The LLM
 * can already see tags and recipes — this surfaces the component data those
 * signals don't cover: food/nutrition, potion effects, jukebox song identity,
 * damage resistance types, weapon damage, tool rules, trim material, dye color
 * component, consumable effects.
 *
 * We pass the raw shape through unchanged (not stringified / renamed) so the
 * LLM sees the real Minecraft vocabulary. Unknown components are dropped to
 * keep prompts focused and bounded.
 */
function extractComponentHighlights(
  components: Record<string, unknown>,
): Record<string, unknown> {
  const out: Record<string, unknown> = {};
  const keep = [
    "minecraft:food",
    "minecraft:consumable",
    "minecraft:potion_contents",
    "minecraft:weapon",
    "minecraft:tool",
    "minecraft:damage_resistant",
    "minecraft:jukebox_playable",
    "minecraft:dye",
    "minecraft:provides_trim_material",
    "minecraft:provides_banner_patterns",
    "minecraft:banner_patterns",
    "minecraft:instrument_component",
    "minecraft:use_cooldown",
    "minecraft:glider",
    "minecraft:repairable",
    "minecraft:fire_resistant",
    "minecraft:death_protection",
    "minecraft:bees",
    "minecraft:max_stack_size",
    "minecraft:max_damage",
    "minecraft:enchantable",
    "minecraft:equippable",
    "minecraft:light_emission",
    "minecraft:writable_book_content",
    "minecraft:written_book_content",
    "minecraft:rarity",
  ];
  for (const key of keep) {
    if (key in components) out[key] = trimComponentHighlight(components[key]);
  }
  return out;
}

function trimComponentHighlight(value: unknown, depth = 0): unknown {
  if (typeof value === "string") {
    return value.length <= 500 ? value : `${value.slice(0, 497)}...`;
  }
  if (typeof value !== "object" || value === null) return value;
  if (depth >= 3) return summarizeComponentValue(value);
  if (Array.isArray(value)) {
    return value.slice(0, 16).map((entry) => trimComponentHighlight(entry, depth + 1));
  }
  const out: Record<string, unknown> = {};
  for (const [key, entry] of Object.entries(value).slice(0, 24)) {
    out[key] = trimComponentHighlight(entry, depth + 1);
  }
  return out;
}

function summarizeComponentValue(value: object): string {
  if (Array.isArray(value)) return `[${value.length} entries]`;
  const keys = Object.keys(value);
  if (keys.length === 0) return "{}";
  return `{${keys.slice(0, 8).join(", ")}${keys.length > 8 ? ", ..." : ""}}`;
}

function flattenTranslatable(entry: unknown): string | null {
  if (!entry || typeof entry !== "object") return null;
  const e = entry as Record<string, unknown>;
  if (typeof e.translate === "string") return e.translate;
  if (typeof e.text === "string") return e.text;
  return null;
}

/**
 * The default target-facet list for stage 3 on a vanilla run: every facet
 * flagged `llm_authored` in the registry. Callers can override.
 */
export function defaultTargetFacets(): string[] {
  return Object.entries(FACETS)
    .filter(([, def]) => def.llm_authored)
    .map(([id]) => id);
}
