import type { ItemExtractRecord } from "../extract/record.ts";
import { FACETS, type FacetDef } from "../schema/facets.ts";

/**
 * One item as presented to the LLM — a trimmed projection of the extract record
 * plus the stage-2 deterministic facets already resolved for it. Keeping the
 * wire format narrow (only fields the LLM will actually reason over) keeps the
 * prompt short and the response focused.
 */
export interface LlmItemPayload {
  id: string;
  display_name: string | null;
  namespace: string;
  minecraft_tags: readonly string[];
  /** Short list of recipe types this item is consumed in (from stage 2 output). */
  processing_in: readonly string[];
  /** Top-10 of the hundreds-element recipe lists — enough for shape signal. */
  sample_ingredient_of: readonly string[];
  /** Handful of output recipes. */
  sample_output_of: readonly string[];
  /** Model chain from stage 1 — useful for template-shape hints. */
  model_parents: readonly string[];
  /** Loot-table ids (trimmed) — useful when stage 2's origin rule didn't fire. */
  sample_loot_sources: readonly string[];
  /** Lore lines from minecraft:lore — big semantic signal when present. */
  lore: readonly string[];
  /**
   * Key component values with semantic meaning the LLM can't derive from
   * tags/recipes alone — food nutrition, potion effects, damage resistance
   * types, weapon stats, jukebox song, dye color, trim material, etc.
   * Pulled from `component_data` and kept narrow so the prompt stays short.
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
}

const SYSTEM_PREAMBLE = `You are classifying Minecraft items for an inventory mod called Slot. \
For each input item you will emit a concise facet record per the schema below.

Rules:
- Only output values from the facet's allowed list, or (for free_text facets) values matching the pattern.
- Only emit facets that actually apply to the item. If a facet doesn't apply (e.g. combat_bonus on bread, biome on crafted_only items), OMIT it from the facets object. Do not emit \`null\`, empty arrays, or placeholder values.
- For single-value enum facets where two values could apply with similar confidence, emit a two-element \`values\` array AND set \`ambiguous: true\`. Downstream reviewers see both.
- **Each facet entry MUST include a \`signal\` field** — it drives the confidence score automatically. Format:
    \`{value, signal: "named|pattern|inferred|guess", rationale: "<≤40 char terse reason>"}\`
    The four signal levels:
      • \`named\` — the value is explicitly stated in a tag/component/lore string. You're transcribing, not judging.
      • \`pattern\` — the value follows mechanically from an id/tag/model pattern (e.g. _ingot + *_tool_materials → material).
      • \`inferred\` — you're making a judgment call from context (recipes, lore, mod conventions, similar items, gameplay knowledge). This is the **normal case** for most classification decisions — Minecraft items rarely announce what they are; you're the judge. Use this freely when your reasoning is sound.
      • \`guess\` — you have essentially no information and are applying a default. Use sparingly; prefer \`ambiguous: true\` + two values, or omit the facet entirely.
    An optional \`evidence: "<short quote>"\` field is allowed if you want to cite the specific input that informed the decision. Not required — skip it to save output tokens.
    \`confidence\` is computed from \`signal\` (named=0.95, pattern=0.80, inferred=0.60, guess=0.30). You may include \`confidence\` to nudge DOWN within the band, but the runner caps it at the signal floor — overclaiming on a guess is silently demoted.
- **Be terse**: rationales must be ≤40 characters. No evidence field unless it adds real information beyond the rationale. Compact output means more items fit in a batch before hitting the token cap.
- If the item's lore, component_highlights, or display_name explicitly names a behaviour, weight that over generic defaults.
- If you want to use a value that isn't in the schema, DO NOT emit the facet; instead add an entry to \`schema_proposals\` at the top level.
- Don't re-emit facets listed under \`stage2_facets\` inside \`facets\` — those are already fixed by deterministic rules. But if you think a stage 2 assertion is **clearly wrong** (e.g. wrong material, wrong form), record it in the top-level \`corrections\` array instead of silently accepting it. Only flag stage 2 values you're confident are wrong (confidence ≥ 0.7) — it costs a human review round.
- Output strict JSON only: no markdown, no code fences, no comments (// or /* */), no trailing commas, no commentary outside the JSON object.
- Your response MUST start with \`{\` and end with \`}\`. Do NOT prepend any narration (no "Here is…", "Continuing with…", etc.). Do NOT append any text after the closing brace.
- Classify every item listed in the \`items\` array. If output is getting long, shorten rationales further rather than dropping items.

Common confusions to avoid:
- \`activity\` does NOT include \`crafting\` — every item is craftable, so "crafting" is noise. If the item is used as a crafting ingredient, that's already captured in \`processing_in\`. Pick an end-use activity instead (\`building\`, \`mining\`, \`combat\`, \`redstone\`, etc.) or omit.
- \`flavor\` is a small set of **aesthetic categories** (\`plain\`, \`variant\`, \`fancy\`, \`ominous\`, \`ancient\`, \`mystical\`, \`mechanical\`, \`natural\`, \`colored\`). Colors, finishes, and moods go in \`palette\`, not \`flavor\`.
- \`palette\` values are in the schema's enum — don't invent new ones like \`green\`, \`colored\`, \`wool_light\`. Use \`leaf_green\` for green, \`pastel\`/\`light\` for soft tones, \`dye_color\` (separate facet) when the item is actually one of the 16 dye colors.
- \`environmental_property\` is world/physics behaviour (fireproof, slippery, waterlogs, piston_movable). \`spawn_interaction\` is mob-farm behaviour (blocks_monster_spawn, allows_spawning, damages_entities). Pick the right facet before picking a value.`;

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
 * the per-batch items (~30KB, varies). Pass `system` to `claude --system-prompt`
 * and `user` on stdin.
 *
 * The split improves prompt-cache hit rate — the system portion is stable
 * across every batch of a run, so Claude Code can cache all of it rather
 * than just the opening preamble. It also avoids mixing our task
 * instructions with Claude Code's default coding-assistant system prompt,
 * which historically caused Sonnet to prepend "Continuing with…" narration.
 */
export function buildSplitPrompt(input: LlmPromptInput): { system: string; user: string } {
  const schemaDoc = renderSchemaForPrompt(input.target_facets);
  const outputShape = renderExpectedOutput(input.target_facets);
  const neighborsNote = renderNeighborsSection(input.neighbors);

  const system = [
    SYSTEM_PREAMBLE,
    "",
    "# Facet schema",
    schemaDoc,
    "",
    FACET_DISAMBIGUATION,
    "",
    "# Common misconceptions to avoid",
    COMMON_MISCONCEPTIONS,
    "",
    "# Expected output shape",
    outputShape,
  ]
    .filter((section) => section.length > 0)
    .join("\n");

  const user = [
    neighborsNote,
    "# Items to classify",
    JSON.stringify({ items: input.items }, null, 2),
    "",
    "Respond with a single JSON object matching the expected output shape above. No other text.",
  ]
    .filter((section) => section.length > 0)
    .join("\n");

  return { system, user };
}

/**
 * Inline guidance for facets with commonly-misapplied values. Kept adjacent
 * to the schema so the LLM reads it while facet vocabulary is top-of-mind.
 */
const FACET_DISAMBIGUATION = `# Facet disambiguation (read before emitting)

## role: material vs natural_resource
- \`natural_resource\` — items obtained **from the world** in raw form: ores,
  raw ore chunks (raw_iron, raw_gold, raw_copper, ancient_debris), logs, mob
  drops (feather, leather, bone, slime_ball, string), unprocessed plants
  (sugar_cane, kelp, bamboo, nether_wart).
- \`material\` — **processed / intermediate** crafting ingredients: ingots,
  nuggets, gems, dusts, plates, dyes, shards. You made it from something raw.
- Test: "did the player pick this up from the world as-is, or is it a
  refined product?" If raw → natural_resource. If refined → material.

## role: building_block vs decorative_block vs functional_block
- \`building_block\` — primary purpose is structure: planks, stairs, slabs,
  walls, logs (when placed), bricks, sandstone, stone variants.
- \`decorative_block\` — primary purpose is aesthetics: banners, carpets,
  paintings, flower pots, candles (unlit or as decor), heads.
- \`functional_block\` — active interactive behaviour: crafting table,
  furnace, anvil, beacon, jukebox, composter, cauldron, lectern, grindstone.
- A block that's *usable* for decoration but primarily structural (like
  stairs) is \`building_block\`, not \`decorative_block\`.

## tier
- Apply to tools/weapons/armor (their progression tier) and to the raw
  materials they're made from (iron_ingot, diamond, netherite_ingot — each
  has tier=iron/diamond/netherite).
- Ingots/gems that aren't part of a tool-tier progression (redstone,
  emerald, amethyst_shard, lapis) do **not** have a \`tier\`.

## activity — "what you DO with this item"
- Pick the gameplay activities where this item is **actively used**, not
  activities it's an ingredient of. A pickaxe is \`mining\` (used to mine);
  a diamond is not \`mining\` (it's crafted into pickaxes).
- A crafting ingredient gets activities from its **downstream use**, not
  from the act of crafting — so \`feather\` gets \`combat\` (via arrows),
  not \`mining\` (even though you might mine to get emeralds to trade for
  feathers).
- If unsure, omit \`activity\` rather than guess.
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
    corrections: [],
  };
  return [
    "Structure (pure JSON, no comments):",
    JSON.stringify(shape, null, 2),
    "",
    "Field rules:",
    "- Single-value facets (enum / free_text / boolean): `value: <scalar>`.",
    "- Multi-value facets (multi_enum / multi_free_text): `values: [<scalar>, ...]`.",
    "- Ambiguous single-value (enum / free_text only): `values: [a, b]` AND `ambiguous: true`.",
    "- `schema_proposals` (optional top-level array, default `[]`): use when you want a value the schema doesn't include. Each entry is `{kind: 'add_value', facet, value, rationale}` or `{kind: 'add_facet', name, suggested_kind, rationale}`.",
    "- `corrections` (optional top-level array, default `[]`): use when a stage 2 facet is clearly wrong. Each entry is `{item, facet, current, suggested, rationale, confidence}` — confidence ≥ 0.7 required.",
  ].join("\n");
}

function chooseMultiExampleFacet(targetFacets: readonly string[]): {
  facet: string;
  entry: Record<string, unknown>;
} {
  for (const candidate of ["activity", "primary_uses", "flavor"]) {
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
): LlmItemPayload {
  const components = record.component_data ?? {};
  const lore = extractLore(components);
  const componentHighlights = extractComponentHighlights(components);
  // The stage-2 layer already wrote processing_in as a multi-value entry —
  // pull the array directly so the LLM doesn't have to re-derive it.
  const processingIn = extractProcessingIn(stage2Facets["processing_in"]);

  return {
    id: record.id,
    namespace: record.namespace,
    display_name: record.display_name,
    minecraft_tags: record.minecraft_tags,
    processing_in: processingIn,
    sample_ingredient_of: record.recipe_role.ingredient_of.slice(0, 10),
    sample_output_of: record.recipe_role.output_of.slice(0, 10),
    model_parents: record.model_parents,
    sample_loot_sources: record.loot_table_sources.slice(0, 10),
    lore,
    component_highlights: componentHighlights,
    stage2_facets: stage2Facets,
  };
}

function extractLore(components: Record<string, unknown>): string[] {
  const raw = components["minecraft:lore"];
  if (!Array.isArray(raw)) return [];
  return raw
    .map((entry) => (typeof entry === "string" ? entry : flattenTranslatable(entry)))
    .filter((s): s is string => typeof s === "string" && s.length > 0);
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
 * keep prompts compact.
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
