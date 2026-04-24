/**
 * A hand-picked set of ~20 vanilla items that exercise stage 3 across its
 * interesting axes. Chosen for:
 *   - Unambiguous canonical cases (`iron_ingot`, `diamond_pickaxe`) — if stage
 *     3 gets these wrong, something's off with the prompt structure.
 *   - Role-ambiguous cases the plan specifically calls out (`cut_copper_stairs`
 *     for building vs decorative; `creeper_head` for trophy vs decorative).
 *   - Items stage 2 couldn't classify (no form / no material) so we can see
 *     the LLM's default behavior.
 *   - Items with strong lore/tooltip signals (`totem_of_undying`, `music_disc_13`,
 *     `creaking_heart`) that should flow through into `primary_uses`.
 *   - One of each major role bucket (tool / weapon / armor / consumable /
 *     ammunition / transport / container / utility / upgrade / trophy / admin /
 *     curiosity) so role-coverage is exercised broadly.
 *
 * Keep this list small — stage 3 sample runs should stay cheap and easy to
 * eyeball.
 */
export const VANILLA_CANARY_ITEMS: readonly string[] = [
  // canonical material / tool / armor
  "minecraft:iron_ingot",
  "minecraft:diamond_pickaxe",
  "minecraft:netherite_helmet",
  // storage + NBT variation
  "minecraft:shulker_box",
  "minecraft:bundle",
  // admin
  "minecraft:command_block",
  // trophy / display
  "minecraft:dragon_egg",
  "minecraft:totem_of_undying",
  "minecraft:creeper_head",
  // consumable / potion / food
  "minecraft:potion",
  "minecraft:golden_apple",
  "minecraft:bread",
  // ammunition / projectile
  "minecraft:firework_rocket",
  "minecraft:tipped_arrow",
  // transport / utility
  "minecraft:ender_pearl",
  "minecraft:elytra",
  "minecraft:compass",
  // role-ambiguous shape items
  "minecraft:cut_copper_stairs",
  "minecraft:copper_grate",
  // mechanism / redstone
  "minecraft:piston",
  "minecraft:redstone",
  // curiosity + decorative
  "minecraft:music_disc_13",
  "minecraft:creaking_heart",
  "minecraft:oak_sapling",
];
