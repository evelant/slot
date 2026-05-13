import type { ItemExtractRecord } from "../extract/record.ts";
import { FACETS, type FacetDef } from "../schema/facets.ts";
import type { PackFacetVocabulary } from "../schema/vocabulary.ts";
import type { LlmDocumentContext } from "./document_context.ts";

export const PROMPT_VERSION = "stage3-prompt-v11";

const RECIPE_EXAMPLE_LIMIT = 96;
const LOOT_SOURCE_LIMIT = 64;
const BLOCK_TAG_LIMIT = 96;

/**
 * One item as presented to the LLM — a trimmed projection of the extract record
 * plus the stage-2 deterministic facets already resolved for it. The wire
 * format is intentionally bounded but not tiny: include enough concrete
 * evidence for judgment calls while preventing single items from dominating a
 * batch.
 */
export interface LlmItemPayload {
  id: string;
  display_name: string | null;
  namespace: string;
  /**
   * Tags where this item is a **direct** listed member (not via a #tag
   * reference). Strongest classification signal; show first.
   */
  minecraft_tags_direct: readonly string[];
  /**
   * Tags reached **transitively** via nested #tag references, excluding the
   * direct ones. Weaker but still useful context (e.g. oak_planks → `planks`
   * is direct, → `wooden_tool_materials` is inherited via `#planks`).
   */
  minecraft_tags_inherited: readonly string[];
  /**
   * Live runtime tag membership when direct/inherited provenance is unavailable.
   * These are useful semantic signals, but weaker than known-direct tags because
   * they may include direct or transitive membership.
   */
  minecraft_tags_resolved?: readonly string[];
  /** How to interpret the tag fields above. */
  minecraft_tag_membership?: "direct_and_inherited" | "resolved_runtime";
  /** Short list of recipe types this item is consumed in (from stage 2 output). */
  processing_in: readonly string[];
  /**
   * Count of recipes consuming this item, grouped by recipe type —
   * e.g. { crafting_shaped: 38, smelting: 8, smithing_transform: 6 }.
   * Lets the LLM weigh the item's role across recipe categories without
   * re-deriving it from a flat id list.
   */
  recipe_consumption_by_type: Record<string, number>;
  /** Count of recipes producing this item, grouped by recipe type. */
  recipe_production_by_type: Record<string, number>;
  /** Count of concrete recipe ids that consume this item. */
  recipe_ingredient_count: number;
  /** Count of concrete recipe ids that produce this item. */
  recipe_output_count: number;
  /**
   * Bounded representative recipe ids that consume this item. When count is
   * larger than this array, treat it as examples only, not the full universe.
   */
  recipe_ingredient_examples: readonly string[];
  /** Bounded representative recipe ids that produce this item. */
  recipe_output_examples: readonly string[];
  /** True when one or both recipe example arrays were capped. */
  recipe_examples_truncated?: boolean;
  /** Model chain from stage 1 — useful for template-shape hints. */
  model_parents: readonly string[];
  /** Runtime/static creative inventory group ids, when available. */
  creative_tabs: readonly string[];
  /** Count of loot-table ids that can produce this item. */
  loot_source_count: number;
  /** Bounded representative loot-table ids. */
  loot_source_examples: readonly string[];
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
  lore: readonly string[];
  /** Tooltip/lang/exported player-facing prose preserved from extraction. */
  semantic_text?: readonly { source: string; key?: string; text: string }[];
  /**
   * Gated guidebook/advancement snippets linked to this runtime item by
   * actual item ids. Kept separate from direct item prose so the LLM can weigh
   * broad document context more carefully.
   */
  document_context?: readonly LlmDocumentContext[];
  /**
   * Key component values with semantic meaning the LLM can't derive from
   * tags/recipes alone — food nutrition, potion effects, damage resistance
   * types, weapon stats, jukebox song, dye color, trim material, etc.
   * Pulled from `component_data` and kept focused on player-visible semantics.
   */
  component_highlights: Record<string, unknown>;
  /** Facets stage 2 already assigned; the LLM should not overwrite these. */
  stage2_facets: Record<string, unknown>;
}

export interface LlmPromptInput {
  /** Items to classify in this batch. */
  items: readonly LlmItemPayload[];
  /** Facet ids the LLM is expected to try to populate. */
  target_facets: readonly string[];
  /** Nearest-neighbor priming: for each item id, id → summary string.
   *  Empty during milestone 5 (stage 4 fills this later). */
  neighbors?: Record<string, readonly string[]>;
  /**
   * Accepted pack vocabulary values for vocabulary-backed facets. When present,
   * Stage 3 must pick from these ids instead of inventing new scoped ids.
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
  description?: string;
  aliases?: readonly string[];
  parent?: string;
}

const SYSTEM_PREAMBLE = `You are classifying Minecraft items for the
inventory mod Slot, which uses these classifications to organize a
player's inventory. Your job is to capture **how a player thinks about
each item** — what they call it, where they store it, what they use it
with, how they interact with it — by emitting a concise facet record
per the schema below.

# Cardinal rule (overrides everything else in this prompt)

Classify items by **how a player thinks about them, uses and interacts
with them, and organizes them in practice**, NOT by the raw technical
data attached. The data is input; the player's mental model is the
answer.

This rule applies to every facet — role, material_family, activity,
primary_uses, carry_frequency, palette, processing_in, etc. — not just
\`role\`. Every other rule below is a concretization of this principle;
your job is to make judgment calls about player perception, not to
mechanically transform the data into a category.

The concrete test, applied on every facet decision:

> "If a player handed me this item and asked 'where in my organized
> inventory would I expect to find this, what activities would I use it
> for, and which other items belong with it?', what's the answer?"

That answer beats the literal reading of any rule. After picking a
value, do one more pass: "Would a player be surprised by this answer,
or surprised to NOT see this item grouped with its siblings?" If yes,
reconsider.

# Output rules
- Only output values from the facet's allowed list, or (for free_text facets) values matching the pattern.
- Vocabulary-backed facets use stable ids such as \`slot:cooking\`,
  \`modid:mechanical_power\`, \`pack:example/steelmaking\`, or scoped
  \`pack:example/steelmaking#input\`; never emit display labels like
  "Steelmaking" as facet values.
- When the prompt includes a "Pack facet vocabulary" section, vocabulary-backed
  facets MUST use only the listed accepted ids for that facet. If none fit,
  omit the facet and, when a useful missing value is clear, add a top-level
  \`vocabulary_proposals\` entry for review. If a vocabulary-backed target
  facet has no listed values in the Pack facet vocabulary section, omit that
  facet entirely. Do not invent a syntactically valid id as a substitute in
  \`facets\`, and copy accepted ids exactly as printed instead of normalizing
  separators or path segments.
- Only emit facets that actually apply to the item. The test: would a player consider this facet meaningful for this item? \`combat_bonus\` on bread, \`biome\` on a crafted-only item — players wouldn't expect a value, so omit. Do not emit \`null\`, empty arrays, or placeholder values to satisfy a target-facet list.
- Multi-value facets must use \`values: [...]\` even when there is only one
  value. This includes \`organization_group\` and \`mod_subsystem\`; never emit it as a scalar \`value\` facet.
- For single-value enum facets where two values could apply with similar confidence, emit a two-element \`values\` array AND set \`ambiguous: true\`. Downstream reviewers see both. Never set \`ambiguous\` on multi-value facets; for multi-value facets emit the applicable \`values\` without \`ambiguous\`, or omit the facet if evidence is too weak.
- **Each facet entry MUST include a \`signal\` field** — a marker for
    downstream review tools, NOT a quality grade. All four levels are
    valid answers. Format:
    \`{value, signal: "named|pattern|inferred|guess", rationale: "<≤80 char>"}\`

    The signal records HOW the value happens to be supported so review
    tools can filter (e.g. "show me only the judgment calls"). Player
    perception is the design target; most facets, most items, are
    judgment calls and emit \`inferred\` — that's correct, not a
    consolation tier.

      • \`named\` — an in-game string explicitly states the value
        (semantic_text, document_context, lore, display name, a tag whose name
        IS the value) AND a player would agree.
      • \`pattern\` — a regular id/tag pattern points at the value AND a
        player would agree (e.g. _ingot + *_tool_materials → role=material).
      • \`inferred\` — **the normal case**: you're judging how a player
        thinks about, uses, organizes, or interacts with the item. The
        literal data may be ambiguous, absent, or point the wrong way;
        player perception is the answer regardless. Use freely.
      • \`guess\` — no confident read either way. Prefer \`ambiguous: true\`
        with two candidates, or omit the facet.

    \`confidence\` is computed from \`signal\` (named=0.95, pattern=0.80,
    inferred=0.60, guess=0.30). You may nudge DOWN within the band; the
    runner caps overclaiming. The numbers are for filtering, not
    grading — an \`inferred\` answer that follows the cardinal rule is
    as correct as a \`pattern\` one.

    Optional \`evidence: "<short quote>"\` field allowed for citing a
    specific input. Use it when it makes a non-obvious judgment auditable.
- Be concise, not cryptic: rationales should be ≤80 characters. Use
  \`evidence\` for the tag, recipe id, component, or display-name phrase that
  actually drove a tricky choice.
- semantic_text, document_context, lore, display_name, creative_tabs, and component_highlights are **player-perception signals** — they're strings or group labels the player actually reads and forms expectations from. Take them seriously when present.
- document_context contains conservative guidebook/advancement snippets linked
  to the item by actual runtime item ids. Treat it as supporting context for
  player-facing purpose, workflow, station, and progression judgments; direct
  item tooltip/lore still wins when they conflict. \`item_ref_count\` says how
  many runtime items the document was linked to, and \`related_item_refs\` shows
  the neighboring items when a low-breadth page covers a small group.
- If \`minecraft_tags_resolved\` is present, those tags are live runtime membership with unknown directness. Use them as semantic context, but don't treat them as intentional direct-tag evidence.
- Fields ending in \`_examples\` are bounded evidence. If the matching
  \`*_count\` is larger, do not infer that omitted recipe / loot ids are absent.
- If you want to use a value that isn't in the fixed schema, DO NOT emit the facet; instead add an entry to \`schema_proposals\` at the top level.
- If you want to use a value for a vocabulary-backed facet that is not listed in Pack facet vocabulary, DO NOT emit the facet; add \`{item, facet, label, proposed_id, rationale, evidence}\` to top-level \`vocabulary_proposals\` instead.
- \`schema_proposals\`, \`vocabulary_proposals\`, \`corrections\`, and \`fill_ins\` are top-level arrays only, siblings of \`items\`. Never put these keys inside an individual item's \`facets\` object; inside \`facets\`, every key must be a real facet id.
- Emit a best \`role\` for every item unless the data is genuinely unusable.
  If two roles are close, use the ambiguous two-value shape.
- Emit \`primary_uses\` for every item unless the data is genuinely unusable:
  one to three short player-facing phrases are enough. For SLOT, \`role\`,
  \`primary_uses\`, and \`carry_frequency\` are more valuable than low-evidence
  \`origin\`, \`flavor\`, or \`palette\`; if output is getting long, keep the
  high-value inventory semantics and omit lower-value facets first.
- \`origin\` is optional and should be high-evidence. Do not emit it merely
  because an item is probably crafted, probably looted, or probably found in
  the world. A valid \`origin\` needs positive input evidence such as a
  production recipe, a loot source, an ore/wild-crop/mob-drop tag, or a name
  that directly identifies the origin.
- Don't re-emit facets listed under \`stage2_facets\` inside \`facets\` — those are already fixed by deterministic rules. But if you think a stage 2 assertion is **clearly wrong** (e.g. wrong material, wrong form), record it in the top-level \`corrections\` array instead of silently accepting it. Only flag stage 2 values you're confident are wrong (confidence ≥ 0.7) — it costs a human review round.
- **Stage-2 fill-in.** If a deterministic facet is *missing* from \`stage2_facets\` but the item obviously has a value (e.g., \`dark_oak_window\` clearly has \`form=window\` even though stage-2's \`form_from_id\` rule didn't catch it; \`copper_pipe\` clearly has \`material_family=copper\`; \`lime_concrete\` clearly has \`dye_color=lime\`; a modded glowing block whose stage-2 \`emits_light\` is missing), record it in the top-level \`fill_ins\` array — DON'T emit it inside \`facets\`. \`fill_ins\` exists alongside \`schema_proposals\` and \`corrections\` so downstream review tools can audit gaps in the stage-2 deterministic layer separately from your judgment-call work. Format each entry as \`{item, facet, value, rationale}\`. Use sparingly: only obvious gaps where a player would clearly agree, not edge cases. The deterministic facets you can fill: \`form\`, \`material_family\`, \`dye_color\`, \`required_tool\`, \`required_tool_tier\`, \`is_fuel\`, \`emits_light\`. \`emits_light\` specifically catches modded lit blocks (Create's lit variants, glowing crystals, modded glowstones) — set \`value: true\` when the block visibly emits light when placed, even if stage-2's id-list and suffix patterns missed it. Don't fill role / activity / carry_frequency / rarity / etc. — those are llm-authored, just emit them in \`facets\` normally.
- Output strict JSON only: no markdown, no code fences, no comments (// or /* */), no trailing commas, no commentary outside the JSON object.
- Your response MUST start with \`{\` and end with \`}\`. Do NOT prepend any narration (no "Here is…", "Continuing with…", etc.). Do NOT append any text after the closing brace.
- Classify every item listed in the \`items\` array. If output is getting long, shorten rationales further rather than dropping items.`;

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
 * classification rules + facet schema + disambiguation + output-shape
 * example (~15KB, identical across batches), and the `user` section carries
 * the per-batch items (~30KB, varies). Pass `system` as the chat system
 * message and `user` as the chat user message.
 *
 * The split improves provider-side cache hit rate because the system portion
 * is stable across every batch of a run. It also keeps task instructions
 * separate from volatile item evidence.
 */
export function buildSplitPrompt(input: LlmPromptInput): { system: string; user: string } {
  const schemaDoc = renderSchemaForPrompt(input.target_facets);
  const outputShape = renderExpectedOutput(input.target_facets);
  const neighborsNote = renderNeighborsSection(input.neighbors);
  const facetVocabularyHint = renderPackFacetVocabulary(input.facet_vocabulary, input.target_facets);
  const runtimeInputNotes = renderRuntimeInputNotes(input.items);

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
    "- Include every item id from `items` exactly once. If output gets long, shorten rationales instead of dropping items.",
    "- `schema_proposals`, `vocabulary_proposals`, `corrections`, and `fill_ins` are top-level arrays only. Never put them inside `<item_id>.facets`; every key inside `facets` must be a real facet id.",
    "- Use `ambiguous: true` only for single-value enum/free_text facets. Never put `ambiguous` on multi-value facets such as `origin`, `activity`, `organization_group`, or `mod_subsystem`.",
    "- Pick `role` from the player's storage-home mental model, not from recipe participation. Machine parts, machine components, hulls, casings, pumps, presses, pipes, cables, and placed processing parts are mechanisms or functional blocks, not generic materials, even when they are ingredients.",
    "- Keep high-value inventory semantics first: `role`, `primary_uses`, `carry_frequency`, and `rarity` should be present unless the item data is genuinely unusable.",
    "- Do not re-emit `stage2_facets` in `facets`. Use `corrections` only for clearly wrong stage-2 values; use `fill_ins` only for missing deterministic facets and only with values allowed by the schema.",
  ];
  if (hasFacetVocabulary) {
    lines.push(
      "- Vocabulary-backed facets may use only ids listed for that exact facet in `Pack facet vocabulary`. If that facet has no section, or no listed id fits, omit the facet and add `vocabulary_proposals` when a useful missing value is clear. Copy accepted ids exactly as printed; do not rewrite slashes, underscores, namespace, or pack prefix.",
      "- Do not move ids across vocabulary-backed facets. A good `mod_subsystem` id such as `modid:kinetics` is not an `organization_group` unless that exact id is listed under `organization_group`; use the subsystem facet, omit the organization group, or add a vocabulary proposal for the missing storage bucket.",
      "- For `organization_group`, use an accepted storage-bucket id only when it is a scarce, broad item-type section clearly better than the built-in section. Do not use organization groups as mod filters, rock/material taxonomy, material form/state splits, workstation-specific processes, or other query-only views.",
    );
  }
  if (!hasSubsystemVocabulary) {
    lines.push(
      "- Omit `mod_subsystem`; no accepted subsystem vocabulary is supplied for this batch.",
    );
  } else {
    lines.push(
      "- Emit `mod_subsystem` only when the item itself belongs to a listed subsystem. Never assign it just because the item is consumed or produced by a subsystem recipe.",
    );
  }
  lines.push(
    "- Optional low-evidence facets are better omitted than guessed.",
  );
  return lines.join("\n");
}

function renderRuntimeInputNotes(items: readonly LlmItemPayload[]): string {
  const hasRuntimeResolvedInput = items.some(
    (item) => item.minecraft_tag_membership === "resolved_runtime",
  );
  if (!hasRuntimeResolvedInput) return "";

  return `# Runtime export input notes

This batch was generated from a live Minecraft registry / recipe manager
instead of only static mod files. Treat present runtime evidence as
pack-specific truth for this exact modpack, including KubeJS and datapack
recipe/tag changes.

- Runtime export v1 intentionally lacks model, loot-table, and creative-tab
  extraction. In this prompt, empty \`model_parents\` or
  \`loot_source_examples\` means "not collected here", not "semantically absent".
- Recipe presences, counts, and recipe-type names are strong evidence for this
  pack. Recipe absences are weaker: some custom recipe classes expose only a
  primary result through the runtime API, so missing output examples do not
  prove the item has no secondary/custom outputs.
- For \`origin\`, do not use empty loot/source fields as evidence, and do not
  emit origin for every item. Emit \`crafted_only\` only when there is positive
  production-recipe evidence in the payload. Rationales like "no loot source",
  "crafted from components", or "crafted from materials" are invalid unless a
  concrete output recipe id/count is present.
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
question: **"if the player held this and asked which island it belongs
on, what's the answer?"**

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
in one island, not scattered across roles.

- Test: *would a player be surprised to see this variant on a
  different island from its siblings?* If yes, align it.
- Anchor: an oak family has its planks, stairs, slabs, walls, fences,
  doors, trapdoors, logs, and stripped logs all on one island.
  → all \`building_block\`. The exceptions are variants that genuinely
  belong elsewhere: \`oak_sapling\` (planted plant → natural_resource),
  \`oak_button\` (redstone trigger → redstone_component), \`oak_boat\`
  (transport).

## tier

Apply to tools / weapons / armor (their progression rung) and to the
raw materials those rungs are MADE from (iron_ingot, diamond,
netherite_ingot all carry tier=iron/diamond/netherite). Items not
part of the tool-tier ladder don't get a tier — emerald, redstone,
lapis, amethyst_shard.

## activity — what you DO with the item

Pick activities the player **actively performs with the item**, not
activities the item is an ingredient of.

- A pickaxe gets \`slot:mining\`. A diamond does not — diamonds are crafted
  INTO pickaxes; the activity belongs to the tool.
- A feather's activity comes from its downstream use: it becomes an
  arrow, so → \`slot:combat\`. Crafting itself is never an activity
  (every item is craftable; the value would be noise).
- If unsure, omit. One good activity beats three weak ones.

## workflow, used_at, and workflow_role — process semantics

These three facets answer different questions:

- \`workflow\`: which recognizable player-facing process this item
  participates in.
- \`used_at\`: where the player uses, inserts, processes, places, or
  operates this item. Prefer the downstream / next-use station for
  intermediate items.
- \`workflow_role\`: the item's role inside that workflow, scoped as
  \`<workflow>#input\` or \`<workflow>#output\`.

Do not set \`used_at\` to the recipe/process that only produced the
item. If an item is made by process A and then fired, inserted, worked,
or consumed by process B, \`used_at\` is B. The fact that process A
produced the item belongs in \`workflow\` and \`workflow_role\` only
when that relationship is useful to the player.

## organization_group — where would a player put this item?

The \`organization_group\` facet is the direct SLOT auto-home signal for
large modpacks. It answers:

> "If a skilled player had only a small number of broad main-wall
> sections for this pack, which named storage section would this item
> belong in?"

Use it for broad, stable storage sections a player would actually
maintain. Think primarily in terms of item type or role: equipment,
raw ore, refined ore, ingots and metal, tools, cooked food, uncooked
food, cooking supplies, plants, natural materials, workbenches,
decorations, bricks and clay, molds, wood, seeds. Those examples are
illustrative, not a hard list. Use case, material state, or workflow
context can refine a broad type, but must not become the main reason to
split related items.

Be stingy. A real manual wall only supports about 15-20 human-named
sections before it becomes worse than unsorted storage. Emit
\`organization_group\` only when the item belongs in a section that
would deserve one of those scarce slots across the pack.

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
when those items form a broad player-recognizable storage bucket. Do NOT emit
it for singleton quirks, decorative style families, color/material
families, or generic catch-alls like \`<ns>:materials\`,
\`<ns>:crafting\`, \`<ns>:blocks\`, \`<ns>:misc\`.

Prefer namespaced, stable, player-facing tokens. Use the item's own
namespace unless the group is clearly pack-owned. Do not omit the facet
just because the item also has a narrow form like \`ingot\`, \`gem\`,
\`raw ore\`, \`stairs\`, \`slab\`, \`tool\`, or \`armor\`; emit the group
when the broad storage bucket is the more useful manual-storage destination.
Omit it when the universal section really is where a player would put
the item, or when no useful group has enough sibling items.

Role, form, and material_family often should be enough. If the accepted
vocabulary contains a bucket a player would actually use for this broad
item family — molds, bricks and clay, natural materials, animal
husbandry, cloth, cooking supplies — emit that accepted
organization_group id. If the listed bucket would split obvious
siblings across two main wall sections, omit it and let the built-in
section win.

Concrete anchors:
- A fired ingot mold item → organization_group=\`pack:example/casting_molds\`.
- An unfired/prepared mold item → organization_group=\`pack:example/casting_molds\`.
- Mortar or masonry-specific inputs → organization_group=\`pack:example/masonry_supplies\`.
- Prepared hides and leatherworking tools → organization_group=\`pack:example/leatherworking\`.
- A plain metal ingot with no workflow-specific storage expectation → no
  organization_group; the general ingots/materials section wins.
- A material state, block-form variant, or individual rock-type subgroup →
  no organization_group; the existing Ingots / Materials / Raw Materials /
  Stairs / Building section wins.
- A bucket named after one mod or one mod's mechanical-power subsystem →
  no organization_group; use role, activity, mod_namespace, mod_subsystem,
  or search/query views instead of a main wall section.
- Stackable plates → no organization_group; that is a material property and
  it would split related metal items.
- Anvil smithing → no organization_group; that is a workstation/process view,
  not the broad metalworking or materials section a player would maintain.

## mod_subsystem — what part of the mod IS this item

The \`mod_subsystem\` facet groups items by the **functional sub-area
of the mod they themselves belong to** — a mechanical-power network, a
logistics network, a storage-upgrade module set. The question is
**identity**, not **interaction graph**.
\`mod_subsystem\` is optional and high-precision; bad subsystem labels
are worse than omission because they fragment inventory homes.

The single biggest failure mode here is assigning a subsystem based on
which recipes the item appears in. A \`golden_sheet\` is *processed by*
some mod's press (so it appears in that mod's processing recipes), but
it IS a refined metal sheet — a \`material\` in the
player's inventory, NOT a processing machine. Its mod_subsystem is
omitted entirely.

The test, applied per item:

> "Is this item itself a member of the named subsystem — a part the
> player installs / wields / configures / chains into the subsystem's
> machinery — or does it merely appear as an ingredient/output of
> recipes the subsystem owns?"

Only the first earns a mod_subsystem assignment. If the item is a
material, food, decorative_block, building_block, natural_resource,
upgrade, tool, weapon, or armor that some processing recipe happens to
consume or produce, OMIT mod_subsystem and let the role decide.

Cross-check: \`mod_subsystem\` is semantic/query identity evidence, not a
main-wall home. The runtime does not auto-create wall sections from
\`mod_subsystem\`; use \`organization_group\` only when a broad storage
section passes that facet's stricter wall-home rule. Items with role
\`material\`, \`building_block\`, \`decorative_block\`, \`consumable\`,
\`natural_resource\`, \`upgrade\`, \`tool\`, \`weapon\`, \`armor\`,
\`storage_block\`, \`utility\`, \`curiosity\` should generally NOT carry
mod_subsystem. Exceptions are rare; if you're not certain, omit.

If no "Suggested mod_subsystem vocabulary" section is present, be even
more conservative: emit \`mod_subsystem\` only for a broad, unmistakable
system that would group many mechanism / workstation / logistics /
transport / redstone items. Do NOT coin narrow one-off labels for
equipment sets, material families, machine hulls/casings, tool families,
or individual equipment lines. Use \`role\`, \`tier\`,
\`material_family\`, and \`primary_uses\` for those instead.

Concrete anchors:
- \`examplemod:cogwheel\` IS a mechanical_power part the player chains
  into contraptions. → mod_subsystem=examplemod:mechanical_power.
- \`examplemod:mechanical_press\` IS a processing machine. →
  mod_subsystem=examplemod:processing.
- \`examplemod:golden_sheet\` is a refined material output by the press.
  → no mod_subsystem; role=material.
- \`examplemod:honeyed_apple\` is a food consumable. → no
  mod_subsystem; role=consumable.
- \`examplemod:metal_girder\` is a building_block (the player builds
  with it as part of an industrial-aesthetic structure). → no
  mod_subsystem; role=building_block.
- \`examplemod:diamond_barrel\` is a storage_block sibling of ordinary
  chests. → no mod_subsystem; role=storage_block.
- A smithing-style upgrade module from a storage mod: → no
  mod_subsystem; role=upgrade.
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
  fences — in a single wood-themed island. Don't split logs off into a
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
  per home — beds belong on a Decoration island.
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
- **mod_subsystem on materials / food / building / decoration /
  upgrade / tool / weapon / armor / storage / utility / curiosity
  / natural items**: omit. mod_subsystem is for items the player
  thinks of as "part of [mod]'s [subsystem network]" —
  cogwheels, funnels, tracks, processing machines. A wood
  variant from a mod, an ingot from a mod, a food item from a
  mod, a smithing-template-style upgrade from a mod, a chest
  variant from a mod — those collapse into the cross-mod
  role-based pile. Don't tag them with their mod's subsystem
  even if recipes connect them.
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
      lines.push("- vocabulary-backed: use only accepted ids from the Pack facet vocabulary section when supplied");
    }
    if (def.examples) lines.push(`- examples: ${def.examples.join(" | ")}`);
    lines.push("");
  }
  return lines.join("\n");
}

function renderExpectedOutput(targetFacets: readonly string[]): string {
  const exampleFacet = chooseExampleFacet(targetFacets);
  const exampleMulti = chooseMultiExampleFacet(targetFacets);
  const shape = {
    items: {
      "<item_id>": {
        facets: {
          [exampleFacet.facet]: exampleFacet.entry,
          [exampleMulti.facet]: exampleMulti.entry,
        },
      },
    },
    schema_proposals: [],
    vocabulary_proposals: [],
    corrections: [],
    fill_ins: [],
  };
  return [
    "Structure (pure JSON, no comments):",
    JSON.stringify(shape, null, 2),
    "",
    "Field rules:",
    "- Single-value facets (enum / free_text / boolean): `value: <scalar>`.",
    "- Multi-value facets (multi_enum / multi_free_text): `values: [<scalar>, ...]`.",
    "- Ambiguous single-value (enum / free_text only): `values: [a, b]` AND `ambiguous: true`. Never use `ambiguous` on multi-value facets.",
    "- `schema_proposals` (optional top-level array, default `[]`): use when you want a value the schema doesn't include. Each entry is `{kind: 'add_value', facet, value, rationale}` or `{kind: 'add_facet', name, suggested_kind, rationale}`.",
    "- `vocabulary_proposals` (optional top-level array, default `[]`): use when a vocabulary-backed facet has no accepted id that fits. Each entry is `{item, facet, label, proposed_id, rationale, evidence}`. Do not emit the proposed id inside `facets`.",
    "- `corrections` (optional top-level array, default `[]`): use when a stage 2 facet is clearly wrong. Each entry is `{item, facet, current, suggested, rationale, confidence}` — confidence ≥ 0.7 required.",
    "- `fill_ins` (optional top-level array, default `[]`): use when a stage-2 deterministic facet is *missing* but the item obviously has a value. Each entry is `{item, facet, value, rationale}`. Only the deterministic facets (`form`, `material_family`, `dye_color`, `required_tool`, `required_tool_tier`, `is_fuel`, `emits_light`) — NOT llm-authored facets like role/activity/carry_frequency.",
    "- The only allowed top-level keys are `items`, `schema_proposals`, `vocabulary_proposals`, `corrections`, and `fill_ins`; do not nest those review arrays under an item.",
  ].join("\n");
}

function chooseMultiExampleFacet(targetFacets: readonly string[]): {
  facet: string;
  entry: Record<string, unknown>;
} {
  for (const candidate of ["organization_group", "activity", "primary_uses", "flavor"]) {
    if (!targetFacets.includes(candidate)) continue;
    const def = FACETS[candidate]!;
    return {
      facet: candidate,
      entry: exampleEntryFor(candidate, def),
    };
  }
  return chooseExampleFacet(targetFacets);
}

function chooseExampleFacet(targetFacets: readonly string[]): {
  facet: string;
  entry: Record<string, unknown>;
} {
  // prefer `role` as the example because it's the most recognizable facet
  for (const candidate of ["role", "material_family", "primary_uses"]) {
    if (!targetFacets.includes(candidate)) continue;
    const def = FACETS[candidate]!;
    return {
      facet: candidate,
      entry: exampleEntryFor(candidate, def),
    };
  }
  const fallback = targetFacets[0]!;
  return {
    facet: fallback,
    entry: exampleEntryFor(fallback, FACETS[fallback]!),
  };
}

function exampleEntryFor(facet: string, def: FacetDef): Record<string, unknown> {
  const isMulti =
    def.kind === "multi_enum" ||
    def.kind === "multi_free_text" ||
    def.kind === "multi_item_ref";
  if (isMulti) {
    return {
      values: def.values?.slice(0, 2) ?? def.examples?.slice(0, 2) ?? ["<value>"],
      signal: "pattern",
      evidence: "id suffix + tag minecraft:<example>",
      rationale: "short reason",
    };
  }
  if (facet === "role") {
    return {
      value: "material",
      signal: "pattern",
      evidence: "id ends _ingot + tag minecraft:iron_tool_materials",
      rationale: "ingot of a known crafting material",
    };
  }
  return {
    value: def.values?.[0] ?? def.examples?.[0] ?? "<value>",
    signal: "named",
    evidence: "<exact quote from a tag/component/lore string>",
    rationale: "short reason",
  };
}

export function buildPromptFacetVocabulary(
  vocabulary: PackFacetVocabulary | undefined,
  targetFacets: readonly string[],
): PromptFacetVocabulary | undefined {
  if (!vocabulary) return undefined;
  const target = new Set(targetFacets);
  const out: PromptFacetVocabulary = {};
  for (const [facetId, facet] of Object.entries(vocabulary.facets ?? {}).sort(([a], [b]) => a.localeCompare(b))) {
    if (!target.has(facetId)) continue;
    const def = FACETS[facetId];
    if (!def?.vocabulary_backed) continue;
    const values: PromptFacetVocabularyValue[] = [];
    for (const [id, value] of Object.entries(facet.values ?? {}).sort(([a], [b]) => a.localeCompare(b))) {
      if (value.state !== "accepted") continue;
      values.push({
        id,
        label: value.label,
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
): string {
  if (!vocabulary || Object.keys(vocabulary).length === 0) return "";
  const lines: string[] = ["# Pack facet vocabulary"];
  lines.push(
    "For vocabulary-backed facets, use only these accepted ids for the matching facet. If a target facet has no section here, omit that facet entirely. If no listed id fits an item, omit the facet and add a top-level vocabulary_proposals entry when a useful missing value is clear. Labels/descriptions are guidance; output accepted ids exactly as printed.",
  );
  lines.push("");
  for (const facetId of targetFacets) {
    const values = vocabulary[facetId];
    if (!values || values.length === 0) continue;
    lines.push(`## ${facetId}`);
    for (const value of values) {
      const details: string[] = [];
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
 * Project an extract record plus its stage-2 facets into the compact payload
 * the prompt ships. Keeps the per-item token cost bounded for big batches.
 */
export function buildItemPayload(
  record: ItemExtractRecord,
  stage2Facets: Record<string, unknown>,
  documentContext?: readonly LlmDocumentContext[],
): LlmItemPayload {
  const components = record.component_data ?? {};
  const lore = extractLore(components);
  const componentHighlights = extractComponentHighlights(components);
  // The stage-2 layer already wrote processing_in as a multi-value entry —
  // pull the array directly so the LLM doesn't have to re-derive it.
  const processingIn = extractProcessingIn(stage2Facets["processing_in"]);

  const tagMembership = tagMembershipMode(record);
  const directTags = tagMembership === "resolved_runtime"
    ? new Set<string>()
    : new Set(record.minecraft_tags_direct);
  const inheritedTags = tagMembership === "resolved_runtime"
    ? []
    : record.minecraft_tags.filter((t) => !directTags.has(t));
  const tagFields = tagMembership === "resolved_runtime"
    ? {
        minecraft_tags_direct: [] as readonly string[],
        minecraft_tags_inherited: [] as readonly string[],
        minecraft_tags_resolved: record.minecraft_tags,
        minecraft_tag_membership: "resolved_runtime" as const,
      }
    : {
        minecraft_tags_direct: record.minecraft_tags_direct,
        minecraft_tags_inherited: inheritedTags,
        minecraft_tag_membership: "direct_and_inherited" as const,
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

  return {
    id: record.id,
    namespace: record.namespace,
    display_name: record.display_name,
    ...tagFields,
    processing_in: processingIn,
    recipe_consumption_by_type: record.recipe_role.ingredient_of_counts ?? {},
    recipe_production_by_type: record.recipe_role.output_of_counts ?? {},
    recipe_ingredient_count: record.recipe_role.in_degree,
    recipe_output_count: record.recipe_role.out_degree,
    recipe_ingredient_examples: ingredientExamples,
    recipe_output_examples: outputExamples,
    ...((ingredientExamples.length < record.recipe_role.ingredient_of.length
      || outputExamples.length < record.recipe_role.output_of.length)
      ? { recipe_examples_truncated: true }
      : {}),
    model_parents: record.model_parents,
    creative_tabs: record.creative_tabs,
    loot_source_count: record.loot_table_sources.length,
    loot_source_examples: lootExamples,
    ...(lootExamples.length < record.loot_table_sources.length
      ? { loot_sources_truncated: true }
      : {}),
    ...(blockContext ? { block_context: blockContext } : {}),
    lore,
    ...(record.semantic_text?.length ? { semantic_text: semanticTextPayload(record) } : {}),
    ...(documentContext?.length ? { document_context: documentContext } : {}),
    component_highlights: componentHighlights,
    stage2_facets: stage2Facets,
  };
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
  const out: Array<{ source: string; key?: string; text: string }> = [];
  for (const entry of record.semantic_text ?? []) {
    const text = entry.text.trim();
    if (!text) continue;
    const key = `${entry.source}\u0000${entry.key ?? ""}\u0000${text}`;
    if (seen.has(key)) continue;
    seen.add(key);
    out.push({
      source: entry.source,
      ...(entry.key ? { key: entry.key } : {}),
      text: text.length <= 2_000 ? text : `${text.slice(0, 1_997)}...`,
    });
    if (out.length >= 64) break;
  }
  return out;
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
    if (key in components) out[key] = components[key];
  }
  return out;
}

function flattenTranslatable(entry: unknown): string | null {
  if (!entry || typeof entry !== "object") return null;
  const e = entry as Record<string, unknown>;
  if (typeof e.translate === "string") return e.translate;
  if (typeof e.text === "string") return e.text;
  return null;
}

function extractProcessingIn(raw: unknown): string[] {
  if (!raw || typeof raw !== "object") return [];
  const values = (raw as { values?: unknown }).values;
  if (!Array.isArray(values)) return [];
  return values.filter((v): v is string => typeof v === "string");
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
