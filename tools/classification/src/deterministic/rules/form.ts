import type { Rule, RuleOutput } from "../types.ts";

/**
 * Assign `form` using (in order):
 *   1. Tag membership — `minecraft:stairs`, `minecraft:slabs`, etc. cover the
 *      bulk of shaped blocks cleanly.
 *   2. Model-parent chain — catches items that don't have a tag
 *      (`item/template_shulker_box` → storage_block).
 *   3. Exact id / id suffix fallback for long tails (`_ingot`, `_sword`).
 *
 * The tag-first approach keeps us honest about modded extensions: mods that
 * register shape variants usually put them in the canonical vanilla tags, so
 * this rule will keep working for Create et al without adjustment.
 */

/** Tag membership → form. */
const TAG_TO_FORM: Record<string, string> = {
  "minecraft:stairs": "stairs",
  "minecraft:slabs": "slab",
  "minecraft:walls": "wall",
  "minecraft:fences": "fence",
  "minecraft:fence_gates": "fence_gate",
  "minecraft:doors": "door",
  "minecraft:trapdoors": "trapdoor",
  "minecraft:buttons": "button",
  "minecraft:wooden_pressure_plates": "pressure_plate",
  "minecraft:saplings": "sapling",
  "minecraft:banners": "banner",
  "minecraft:signs": "sign",
  "minecraft:hanging_signs": "hanging_sign",
  "minecraft:beds": "bed",
  "minecraft:wool_carpets": "carpet",
  "minecraft:candles": "candle",
  "minecraft:logs": "log",
  "minecraft:planks": "whole_block",
  "minecraft:wool": "whole_block",
};

/** Model parent id → form. Only used when no tag matched. */
const MODEL_PARENT_TO_FORM: Record<string, string> = {
  "block/button": "button",
  "block/button_inventory": "button",
  "block/pressure_plate_up": "pressure_plate",
  "block/pressure_plate_down": "pressure_plate",
  "block/template_ladder": "ladder",
  "block/template_cauldron_empty": "special",
  "block/template_torch": "torch",
  "block/template_lantern": "lantern",
  "block/template_hanging_lantern": "lantern",
  "item/template_skull": "head",
  "item/template_shulker_box": "storage_block",
  "item/template_chest": "storage_block",
  "item/template_bundle_open_front": "bottle",
  "item/template_spawn_egg": "special",
  "item/boat": "vehicle",
  "item/chest_boat": "vehicle",
  "item/minecart": "vehicle",
};

/** Exact id overrides. */
const ID_EXACT_TO_FORM: Record<string, string> = {
  "minecraft:arrow": "projectile",
  "minecraft:spectral_arrow": "projectile",
  "minecraft:tipped_arrow": "projectile",
  "minecraft:firework_rocket": "projectile",
  "minecraft:ender_pearl": "projectile",
  "minecraft:egg": "projectile",
  "minecraft:snowball": "projectile",
  "minecraft:splash_potion": "potion",
  "minecraft:lingering_potion": "potion",
  "minecraft:potion": "potion",
  "minecraft:glass_bottle": "bottle",
  "minecraft:bucket": "bucket",
  "minecraft:water_bucket": "bucket",
  "minecraft:lava_bucket": "bucket",
  "minecraft:milk_bucket": "bucket",
  "minecraft:powder_snow_bucket": "bucket",
  "minecraft:axolotl_bucket": "bucket",
  "minecraft:cod_bucket": "bucket",
  "minecraft:pufferfish_bucket": "bucket",
  "minecraft:salmon_bucket": "bucket",
  "minecraft:tadpole_bucket": "bucket",
  "minecraft:tropical_fish_bucket": "bucket",
  "minecraft:bow": "weapon",
  "minecraft:crossbow": "weapon",
  "minecraft:trident": "weapon",
  "minecraft:mace": "weapon",
  "minecraft:fishing_rod": "tool",
  "minecraft:flint_and_steel": "tool",
  "minecraft:shears": "tool",
  "minecraft:shield": "tool",
  "minecraft:elytra": "armor_piece",
};

/** Ordered id-suffix fallbacks; order matters (longer suffixes first). */
const ID_SUFFIX_TO_FORM: Array<[suffix: string, form: string]> = [
  ["_pressure_plate", "pressure_plate"],
  ["_hanging_sign", "hanging_sign"],
  ["_fence_gate", "fence_gate"],
  ["_chestplate", "armor_piece"],
  ["_leggings", "armor_piece"],
  ["_pickaxe", "tool"],
  ["_helmet", "armor_piece"],
  ["_trapdoor", "trapdoor"],
  ["_shovel", "tool"],
  ["_boots", "armor_piece"],
  ["_stairs", "stairs"],
  ["_button", "button"],
  ["_carpet", "carpet"],
  ["_candle", "candle"],
  ["_banner", "banner"],
  ["_bucket", "bucket"],
  ["_sword", "weapon"],
  ["_nugget", "nugget"],
  ["_ingot", "ingot"],
  ["_shard", "shard"],
  ["_seeds", "seed"],
  ["_dust", "dust"],
  ["_hoe", "tool"],
  ["_axe", "tool"],
  ["_saddle", "special"],
  ["_door", "door"],
  ["_wall", "wall"],
  ["_slab", "slab"],
  ["_fence", "fence"],
  ["_lantern", "lantern"],
  ["_torch", "torch"],
  ["_sign", "sign"],
  ["_bed", "bed"],
  ["_ladder", "ladder"],
  ["_bars", "bars"],
  // Window / glass / pane family — covers Create's _window blocks
  // (form was previously null because nothing matched) plus vanilla
  // glass + pane variants. `_glass_pane` and `_pane` both map to pane;
  // `_window`, `_window_pane`, `_glass`, and bare `glass` map to a
  // single `pane` form so the form-keyed WINDOWS template fires
  // uniformly. Players don't distinguish window vs pane vs glass at
  // the level that matters for inventory organization.
  ["_window_pane", "pane"],
  ["_glass_pane", "pane"],
  ["_window", "pane"],
  ["_pane", "pane"],
  ["_stained_glass", "pane"],
  ["_glass", "pane"],
  ["_sapling", "sapling"],
  ["_boat", "vehicle"],
  ["_minecart", "vehicle"],
  ["_ore", "ore"],
  ["_crystal", "crystal"],
];

/**
 * Bare ids (no suffix prefix) that should map to a form. Mostly the
 * one-off "glass" / "ladder" cases where the item is the canonical
 * thing and has no material prefix.
 */
const BARE_ID_TO_FORM: Record<string, string> = {
  glass: "pane",
  ladder: "ladder",
};

export const formRule: Rule = {
  id: "form",
  facets: ["form"],
  run({ record }) {
    const exactId = ID_EXACT_TO_FORM[record.id];
    if (exactId) return emit(exactId, "rule:form_from_id", `exact id`);

    // Stripped variants: stripped_oak_log → stripped_log, stripped_oak_wood → wood
    if (record.path.startsWith("stripped_")) {
      if (record.path.endsWith("_wood")) return emit("wood", "rule:form_from_id", "stripped wood");
      if (record.path.endsWith("_log") || record.path.endsWith("_stem") || record.path.endsWith("_hyphae")) {
        return emit("stripped_log", "rule:form_from_id", "stripped log");
      }
    }

    // 6-sided log (wood / bamboo block) vs trunk log
    if (record.path.endsWith("_wood") || record.path.endsWith("_hyphae")) {
      return emit("wood", "rule:form_from_id", "id suffix _wood");
    }

    for (const tag of record.minecraft_tags) {
      const form = TAG_TO_FORM[tag];
      if (form) return emit(form, "rule:form_from_tag", `tag ${tag}`);
    }

    for (const parent of record.model_parents) {
      const form = MODEL_PARENT_TO_FORM[parent];
      if (form) return emit(form, "rule:form_from_model", `model ${parent}`);
    }

    for (const [suffix, form] of ID_SUFFIX_TO_FORM) {
      if (record.path.endsWith(suffix)) {
        return emit(form, "rule:form_from_id", `suffix ${suffix}`);
      }
    }

    const bare = BARE_ID_TO_FORM[record.path];
    if (bare) return emit(bare, "rule:form_from_id", `bare id`);

    return [];
  },
};

function emit(value: string, source: string, rationale: string): RuleOutput[] {
  return [
    {
      facet: "form",
      kind: "single",
      value,
      source,
      confidence: 1,
      rationale,
    },
  ];
}
