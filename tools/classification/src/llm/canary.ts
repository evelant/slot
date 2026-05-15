/**
 * A hand-picked set of vanilla items that exercise stage 3 across its
 * interesting axes. Chosen for:
 *   - Unambiguous canonical cases (`iron_ingot`, `diamond_pickaxe`) — if stage
 *     3 gets these wrong, something's off with the prompt structure.
 *   - Role-ambiguous cases the plan specifically calls out (`cut_copper_stairs`
 *     for building vs decorative; `creaking_heart` for functional vs decorative).
 *   - Items old deterministic rules could not classify (no form / no material)
 *     so we can see the LLM's default behavior.
 *   - Items with strong lore/tooltip signals (`totem_of_undying`, `music_disc_13`,
 *     `enchanted_golden_apple`) that should flow through into `primary_uses`.
 *   - Breadth: one of each major role bucket plus biome/tier/processing edge cases.
 *
 * Keep this list short enough to eyeball in a single review pass.
 */
export const VANILLA_CANARY_ITEMS: readonly string[] = [
  // --- canonical materials ---
  "minecraft:iron_ingot",
  "minecraft:gold_ingot",
  "minecraft:copper_ingot",
  "minecraft:netherite_ingot",
  "minecraft:diamond",
  "minecraft:redstone",
  "minecraft:coal",
  "minecraft:emerald",
  "minecraft:amethyst_shard",
  "minecraft:slime_ball",

  // --- natural resources / raw ---
  "minecraft:raw_iron",
  "minecraft:ancient_debris",
  "minecraft:oak_log",
  "minecraft:cherry_log",
  "minecraft:bamboo",
  "minecraft:sugar_cane",
  "minecraft:kelp",
  "minecraft:feather",

  // --- tools / weapons / armor (tier + material variety) ---
  "minecraft:wooden_pickaxe",
  "minecraft:stone_pickaxe",
  "minecraft:diamond_pickaxe",
  "minecraft:netherite_sword",
  "minecraft:mace",
  "minecraft:trident",
  "minecraft:crossbow",
  "minecraft:netherite_helmet",
  "minecraft:chainmail_chestplate",
  "minecraft:leather_boots",
  "minecraft:turtle_helmet",
  "minecraft:elytra",
  "minecraft:shield",

  // --- storage / container ---
  "minecraft:shulker_box",
  "minecraft:ender_chest",
  "minecraft:bundle",
  "minecraft:barrel",

  // --- functional blocks ---
  "minecraft:beacon",
  "minecraft:brewing_stand",
  "minecraft:enchanting_table",
  "minecraft:anvil",
  "minecraft:composter",
  "minecraft:lectern",
  "minecraft:grindstone",
  "minecraft:smoker",

  // --- mechanism / redstone ---
  "minecraft:piston",
  "minecraft:sticky_piston",
  "minecraft:hopper",
  "minecraft:dropper",
  "minecraft:observer",
  "minecraft:repeater",
  "minecraft:comparator",

  // --- consumables ---
  "minecraft:bread",
  "minecraft:golden_apple",
  "minecraft:enchanted_golden_apple",
  "minecraft:potion",
  "minecraft:splash_potion",
  "minecraft:milk_bucket",
  "minecraft:cake",

  // --- ammunition / projectile ---
  "minecraft:arrow",
  "minecraft:tipped_arrow",
  "minecraft:firework_rocket",
  "minecraft:snowball",

  // --- transport / utility ---
  "minecraft:ender_pearl",
  "minecraft:chorus_fruit",
  "minecraft:compass",
  "minecraft:recovery_compass",
  "minecraft:spyglass",
  "minecraft:clock",
  "minecraft:name_tag",
  "minecraft:lead",

  // --- ambiguous / decorative block shapes ---
  "minecraft:cut_copper_stairs",
  "minecraft:copper_grate",
  "minecraft:waxed_cut_copper_slab",
  "minecraft:mangrove_fence_gate",
  "minecraft:cherry_button",
  "minecraft:bamboo_mosaic",

  // --- dye-colored family ---
  "minecraft:white_wool",
  "minecraft:light_blue_bed",
  "minecraft:magenta_banner",
  "minecraft:pink_candle",
  "minecraft:orange_glazed_terracotta",

  // --- biome / world-gen cases ---
  "minecraft:creaking_heart",
  "minecraft:pale_oak_log",
  "minecraft:cherry_sapling",
  "minecraft:pitcher_pod",
  "minecraft:torchflower_seeds",
  "minecraft:azalea",
  "minecraft:glow_berries",
  "minecraft:nether_wart",

  // --- trophy / curiosity / upgrade ---
  "minecraft:dragon_egg",
  "minecraft:dragon_head",
  "minecraft:creeper_head",
  "minecraft:music_disc_13",
  "minecraft:music_disc_creator",
  "minecraft:totem_of_undying",
  "minecraft:nether_star",
  "minecraft:netherite_upgrade_smithing_template",
  "minecraft:sentry_armor_trim_smithing_template",
  "minecraft:angler_pottery_sherd",

  // --- admin ---
  "minecraft:command_block",
  "minecraft:barrier",
  "minecraft:structure_block",
  "minecraft:debug_stick",
];
