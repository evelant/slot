package dev.imagio.slot.debug;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ParentKeywordRules {
    public record Rule(SemanticBucket bucket, String keyword, int priority) {
    }

    // Priority-ordered rule list. Higher priority wins. Word-boundary keyword match
    // (`_keyword_` inside `_path_`). Designed so that specific/structural signals beat
    // generic material or color signals. Keywords match the item id's path component
    // (e.g. `brass_block` from `create:brass_block`).
    private static final List<Rule> RAW_RULES = List.of(
            // --- Upgrades (highest priority — otherwise "crafting_upgrade" would
            // match crafting_table rule; "*_upgrade" and smithing_templates all
            // go to UPGRADES regardless of the rest of their name). ---
            new Rule(SemanticBucket.UPGRADES, "upgrade", 210),
            new Rule(SemanticBucket.UPGRADES, "downgrade", 210),
            new Rule(SemanticBucket.UPGRADES, "smithing_template", 210),
            new Rule(SemanticBucket.UPGRADES, "armor_trim_smithing_template", 210),
            new Rule(SemanticBucket.UPGRADES, "upgrade_template", 210),

            // --- Storage: containers ONLY (not workbenches) ---
            new Rule(SemanticBucket.STORAGE, "chest", 200),
            new Rule(SemanticBucket.STORAGE, "barrel", 200),
            new Rule(SemanticBucket.STORAGE, "shulker_box", 200),
            new Rule(SemanticBucket.STORAGE, "backpack", 195),
            new Rule(SemanticBucket.STORAGE, "storage_connector", 195),
            new Rule(SemanticBucket.STORAGE, "storage_tool", 195),
            new Rule(SemanticBucket.STORAGE, "storage_input", 195),
            new Rule(SemanticBucket.STORAGE, "storage_output", 195),
            new Rule(SemanticBucket.STORAGE, "storage_terminal", 195),
            new Rule(SemanticBucket.STORAGE, "item_vault", 190),
            new Rule(SemanticBucket.STORAGE, "beehive", 185),
            new Rule(SemanticBucket.STORAGE, "bee_nest", 185),

            // --- Workbenches: crafting surfaces + smelting + processing stations ---
            new Rule(SemanticBucket.WORKBENCHES, "crafting_table", 190),
            new Rule(SemanticBucket.WORKBENCHES, "furnace", 190),
            new Rule(SemanticBucket.WORKBENCHES, "smoker", 190),
            new Rule(SemanticBucket.WORKBENCHES, "blast_furnace", 190),
            new Rule(SemanticBucket.WORKBENCHES, "brewing_stand", 190),
            new Rule(SemanticBucket.WORKBENCHES, "smithing_table", 190),
            new Rule(SemanticBucket.WORKBENCHES, "cartography_table", 190),
            new Rule(SemanticBucket.WORKBENCHES, "fletching_table", 190),
            new Rule(SemanticBucket.WORKBENCHES, "stonecutter", 190),
            new Rule(SemanticBucket.WORKBENCHES, "grindstone", 190),
            new Rule(SemanticBucket.WORKBENCHES, "enchanting_table", 190),
            new Rule(SemanticBucket.WORKBENCHES, "anvil", 190),
            new Rule(SemanticBucket.WORKBENCHES, "loom", 190),
            new Rule(SemanticBucket.WORKBENCHES, "roll_table", 190),
            new Rule(SemanticBucket.WORKBENCHES, "composter", 185),
            new Rule(SemanticBucket.WORKBENCHES, "chiseled_bookshelf", 180),
            new Rule(SemanticBucket.WORKBENCHES, "bookshelf", 180),

            // --- Mechanisms: kinetic / logistics / tech ---
            new Rule(SemanticBucket.MECHANISMS, "cogwheel", 170),
            new Rule(SemanticBucket.MECHANISMS, "gearbox", 170),
            new Rule(SemanticBucket.MECHANISMS, "gearshift", 170),
            new Rule(SemanticBucket.MECHANISMS, "clutch", 170),
            new Rule(SemanticBucket.MECHANISMS, "shaft", 170),
            new Rule(SemanticBucket.MECHANISMS, "encased", 170),
            new Rule(SemanticBucket.MECHANISMS, "bearing", 170),
            new Rule(SemanticBucket.MECHANISMS, "mechanical", 170),
            new Rule(SemanticBucket.MECHANISMS, "mechanism", 170),
            new Rule(SemanticBucket.MECHANISMS, "millstone", 170),
            new Rule(SemanticBucket.MECHANISMS, "crushing_wheel", 170),
            new Rule(SemanticBucket.MECHANISMS, "contraption", 170),
            new Rule(SemanticBucket.MECHANISMS, "schematic", 170),
            new Rule(SemanticBucket.MECHANISMS, "deployer", 170),
            new Rule(SemanticBucket.MECHANISMS, "press", 170),
            new Rule(SemanticBucket.MECHANISMS, "mixer", 170),
            new Rule(SemanticBucket.MECHANISMS, "depot", 170),
            new Rule(SemanticBucket.MECHANISMS, "pipe", 170),
            new Rule(SemanticBucket.MECHANISMS, "funnel", 170),
            new Rule(SemanticBucket.MECHANISMS, "chute", 170),
            new Rule(SemanticBucket.MECHANISMS, "belt", 170),
            new Rule(SemanticBucket.MECHANISMS, "tunnel", 165),
            new Rule(SemanticBucket.MECHANISMS, "conveyor", 165),
            new Rule(SemanticBucket.MECHANISMS, "assembler", 165),
            new Rule(SemanticBucket.MECHANISMS, "pulley", 165),
            new Rule(SemanticBucket.MECHANISMS, "carriage", 165),
            new Rule(SemanticBucket.MECHANISMS, "display_link", 165),
            new Rule(SemanticBucket.MECHANISMS, "portable_storage_interface", 165),
            new Rule(SemanticBucket.MECHANISMS, "engine", 165),
            new Rule(SemanticBucket.MECHANISMS, "turbine", 165),
            new Rule(SemanticBucket.MECHANISMS, "fan", 165),
            new Rule(SemanticBucket.MECHANISMS, "filter", 165),
            new Rule(SemanticBucket.MECHANISMS, "basin", 165),
            new Rule(SemanticBucket.MECHANISMS, "valve_handle", 165),
            new Rule(SemanticBucket.MECHANISMS, "sail", 160),
            new Rule(SemanticBucket.MECHANISMS, "bore_block", 160),
            new Rule(SemanticBucket.MECHANISMS, "multimeter", 160),
            new Rule(SemanticBucket.MECHANISMS, "crank", 160),
            new Rule(SemanticBucket.MECHANISMS, "toolbox", 160),
            new Rule(SemanticBucket.MECHANISMS, "metal_girder", 160),
            new Rule(SemanticBucket.MECHANISMS, "metal_bracket", 160),
            new Rule(SemanticBucket.MECHANISMS, "scaffolding", 155),
            new Rule(SemanticBucket.MECHANISMS, "fluid_tank", 160),
            new Rule(SemanticBucket.MECHANISMS, "cart_assembler", 160),
            new Rule(SemanticBucket.MECHANISMS, "ejector", 160),

            // --- Redstone ---
            new Rule(SemanticBucket.REDSTONE, "redstone", 150),
            new Rule(SemanticBucket.REDSTONE, "observer", 150),
            new Rule(SemanticBucket.REDSTONE, "piston", 150),
            new Rule(SemanticBucket.REDSTONE, "sticky_piston", 150),
            new Rule(SemanticBucket.REDSTONE, "dispenser", 150),
            new Rule(SemanticBucket.REDSTONE, "dropper", 150),
            new Rule(SemanticBucket.REDSTONE, "hopper", 150),
            new Rule(SemanticBucket.REDSTONE, "comparator", 150),
            new Rule(SemanticBucket.REDSTONE, "repeater", 150),
            new Rule(SemanticBucket.REDSTONE, "note_block", 150),
            new Rule(SemanticBucket.REDSTONE, "tripwire", 150),
            new Rule(SemanticBucket.REDSTONE, "tripwire_hook", 150),
            new Rule(SemanticBucket.REDSTONE, "lightning_rod", 150),
            new Rule(SemanticBucket.REDSTONE, "daylight_detector", 150),
            new Rule(SemanticBucket.REDSTONE, "sculk_sensor", 150),
            new Rule(SemanticBucket.REDSTONE, "sculk_shrieker", 150),

            // --- Building: trim/structural (high priority — beats wood/stone below) ---
            new Rule(SemanticBucket.BUILDING, "stairs", 140),
            new Rule(SemanticBucket.BUILDING, "slab", 140),
            new Rule(SemanticBucket.BUILDING, "door", 140),
            new Rule(SemanticBucket.BUILDING, "trapdoor", 140),
            new Rule(SemanticBucket.BUILDING, "wall", 140),
            new Rule(SemanticBucket.BUILDING, "fence", 140),
            new Rule(SemanticBucket.BUILDING, "fence_gate", 140),
            new Rule(SemanticBucket.BUILDING, "gate", 140),
            new Rule(SemanticBucket.BUILDING, "pressure_plate", 140),
            new Rule(SemanticBucket.BUILDING, "button", 140),
            new Rule(SemanticBucket.BUILDING, "pane", 140),
            new Rule(SemanticBucket.BUILDING, "bars", 140),
            new Rule(SemanticBucket.BUILDING, "ladder", 140),
            new Rule(SemanticBucket.BUILDING, "window", 140),
            new Rule(SemanticBucket.BUILDING, "window_pane", 140),

            // --- Decoration ---
            new Rule(SemanticBucket.DECORATION, "banner", 135),
            new Rule(SemanticBucket.DECORATION, "banner_pattern", 135),
            new Rule(SemanticBucket.DECORATION, "painting", 135),
            new Rule(SemanticBucket.DECORATION, "item_frame", 135),
            new Rule(SemanticBucket.DECORATION, "lantern", 135),
            new Rule(SemanticBucket.DECORATION, "torch", 135),
            new Rule(SemanticBucket.DECORATION, "candle", 135),
            new Rule(SemanticBucket.DECORATION, "bed", 135),
            new Rule(SemanticBucket.DECORATION, "sign", 135),
            new Rule(SemanticBucket.DECORATION, "hanging_sign", 135),
            new Rule(SemanticBucket.DECORATION, "carpet", 135),
            new Rule(SemanticBucket.DECORATION, "wool", 130),
            new Rule(SemanticBucket.DECORATION, "armor_stand", 130),
            new Rule(SemanticBucket.DECORATION, "flower_pot", 130),
            new Rule(SemanticBucket.DECORATION, "pot", 130),
            new Rule(SemanticBucket.DECORATION, "seat", 130),
            new Rule(SemanticBucket.DECORATION, "table_cloth", 130),
            new Rule(SemanticBucket.DECORATION, "postbox", 130),
            new Rule(SemanticBucket.DECORATION, "bell", 130),
            new Rule(SemanticBucket.DECORATION, "peculiar_bell", 130),
            new Rule(SemanticBucket.DECORATION, "head", 130),
            new Rule(SemanticBucket.DECORATION, "skull", 130),
            new Rule(SemanticBucket.DECORATION, "dye", 125),

            // --- Building: colored/themed surfaces (concrete, terracotta, glass, glazed) ---
            new Rule(SemanticBucket.BUILDING, "concrete", 120),
            new Rule(SemanticBucket.BUILDING, "concrete_powder", 120),
            new Rule(SemanticBucket.BUILDING, "glazed_terracotta", 120),
            new Rule(SemanticBucket.BUILDING, "glazed", 120),
            new Rule(SemanticBucket.BUILDING, "terracotta", 115),
            new Rule(SemanticBucket.BUILDING, "stained_glass", 115),
            new Rule(SemanticBucket.BUILDING, "glass", 115),
            new Rule(SemanticBucket.BUILDING, "bricks", 115),
            new Rule(SemanticBucket.BUILDING, "brick", 115),
            new Rule(SemanticBucket.BUILDING, "pillar", 115),
            new Rule(SemanticBucket.BUILDING, "mosaic", 115),
            new Rule(SemanticBucket.BUILDING, "shingles", 115),
            new Rule(SemanticBucket.BUILDING, "tiles", 115),
            new Rule(SemanticBucket.BUILDING, "asphalt", 115),

            // --- Building: stone-type blocks (unambiguous stone names) ---
            new Rule(SemanticBucket.BUILDING, "blackstone", 110),
            new Rule(SemanticBucket.BUILDING, "gilded_blackstone", 110),
            new Rule(SemanticBucket.BUILDING, "polished_blackstone", 110),
            new Rule(SemanticBucket.BUILDING, "deepslate", 110),
            new Rule(SemanticBucket.BUILDING, "cobbled_deepslate", 110),
            new Rule(SemanticBucket.BUILDING, "basalt", 110),
            new Rule(SemanticBucket.BUILDING, "calcite", 110),
            new Rule(SemanticBucket.BUILDING, "tuff", 110),
            new Rule(SemanticBucket.BUILDING, "sandstone", 110),
            new Rule(SemanticBucket.BUILDING, "granite", 110),
            new Rule(SemanticBucket.BUILDING, "diorite", 110),
            new Rule(SemanticBucket.BUILDING, "andesite", 110),
            new Rule(SemanticBucket.BUILDING, "crimsite", 110),
            new Rule(SemanticBucket.BUILDING, "limestone", 110),
            new Rule(SemanticBucket.BUILDING, "ochrum", 110),
            new Rule(SemanticBucket.BUILDING, "veridium", 110),
            new Rule(SemanticBucket.BUILDING, "dripstone", 110),
            new Rule(SemanticBucket.BUILDING, "obsidian", 110),
            new Rule(SemanticBucket.BUILDING, "crying_obsidian", 110),
            new Rule(SemanticBucket.BUILDING, "cobblestone", 110),
            new Rule(SemanticBucket.BUILDING, "smooth_stone", 110),
            new Rule(SemanticBucket.BUILDING, "smooth_sandstone", 110),
            new Rule(SemanticBucket.BUILDING, "smooth_quartz", 110),
            new Rule(SemanticBucket.BUILDING, "quartz", 105),
            new Rule(SemanticBucket.BUILDING, "nether_bricks", 105),
            new Rule(SemanticBucket.BUILDING, "prismarine_bricks", 105),
            new Rule(SemanticBucket.BUILDING, "polished", 100),
            new Rule(SemanticBucket.BUILDING, "chiseled", 100),
            new Rule(SemanticBucket.BUILDING, "cut", 100),
            new Rule(SemanticBucket.BUILDING, "smooth", 100),
            new Rule(SemanticBucket.BUILDING, "cracked", 100),

            // --- Building: copper (cut_copper, oxidized_copper, etc. — after storage_blocks wins for copper_block) ---
            new Rule(SemanticBucket.BUILDING, "oxidized_copper", 95),
            new Rule(SemanticBucket.BUILDING, "weathered_copper", 95),
            new Rule(SemanticBucket.BUILDING, "exposed_copper", 95),
            new Rule(SemanticBucket.BUILDING, "cut_copper", 95),
            new Rule(SemanticBucket.BUILDING, "copper_bulb", 95),
            new Rule(SemanticBucket.BUILDING, "copper_grate", 95),
            new Rule(SemanticBucket.BUILDING, "copper_bars", 95),

            // --- Materials: ingots / raw / gems / dusts (tagged items usually catch first, this is the fallback) ---
            new Rule(SemanticBucket.MATERIALS, "ingot", 90),
            new Rule(SemanticBucket.MATERIALS, "nugget", 90),
            new Rule(SemanticBucket.MATERIALS, "plate", 90),
            new Rule(SemanticBucket.MATERIALS, "sheet", 90),
            new Rule(SemanticBucket.MATERIALS, "gem", 90),
            new Rule(SemanticBucket.MATERIALS, "shard", 90),
            new Rule(SemanticBucket.MATERIALS, "crystal", 90),
            new Rule(SemanticBucket.MATERIALS, "dust", 90),
            new Rule(SemanticBucket.MATERIALS, "powder", 90),
            new Rule(SemanticBucket.MATERIALS, "rod", 90),
            new Rule(SemanticBucket.MATERIALS, "ore", 90),
            new Rule(SemanticBucket.MATERIALS, "raw", 90),
            new Rule(SemanticBucket.MATERIALS, "crushed", 90),
            new Rule(SemanticBucket.MATERIALS, "incomplete", 85),
            new Rule(SemanticBucket.MATERIALS, "pulp", 85),
            new Rule(SemanticBucket.MATERIALS, "honeycomb", 85),
            new Rule(SemanticBucket.MATERIALS, "feather", 85),
            new Rule(SemanticBucket.MATERIALS, "flint", 85),
            new Rule(SemanticBucket.MATERIALS, "bone_meal", 85),
            new Rule(SemanticBucket.MATERIALS, "echo_shard", 85),
            new Rule(SemanticBucket.MATERIALS, "phantom_membrane", 85),
            new Rule(SemanticBucket.MATERIALS, "shulker_shell", 85),
            new Rule(SemanticBucket.MATERIALS, "glow_ink_sac", 85),
            new Rule(SemanticBucket.MATERIALS, "ink_sac", 85),
            new Rule(SemanticBucket.MATERIALS, "amethyst_bud", 85),
            new Rule(SemanticBucket.MATERIALS, "dragon_breath", 85),

            // --- Materials: storage blocks (diamond_block, iron_block, brass_block…) ---
            // low priority so specific kinds (e.g. redstone_block, note_block) route correctly first.
            new Rule(SemanticBucket.MATERIALS, "diamond_block", 80),
            new Rule(SemanticBucket.MATERIALS, "emerald_block", 80),
            new Rule(SemanticBucket.MATERIALS, "iron_block", 80),
            new Rule(SemanticBucket.MATERIALS, "gold_block", 80),
            new Rule(SemanticBucket.MATERIALS, "copper_block", 80),
            new Rule(SemanticBucket.MATERIALS, "lapis_block", 80),
            new Rule(SemanticBucket.MATERIALS, "coal_block", 80),
            new Rule(SemanticBucket.MATERIALS, "netherite_block", 80),
            new Rule(SemanticBucket.MATERIALS, "bone_block", 80),
            new Rule(SemanticBucket.MATERIALS, "brass_block", 80),
            new Rule(SemanticBucket.MATERIALS, "zinc_block", 80),
            new Rule(SemanticBucket.MATERIALS, "andesite_alloy_block", 80),
            new Rule(SemanticBucket.MATERIALS, "industrial_iron_block", 80),

            // --- Natural: leaves, saplings, flowers, crops, roots, coral, mushrooms ---
            new Rule(SemanticBucket.NATURAL, "leaves", 75),
            new Rule(SemanticBucket.NATURAL, "sapling", 75),
            new Rule(SemanticBucket.NATURAL, "flower", 75),
            new Rule(SemanticBucket.NATURAL, "rose", 75),
            new Rule(SemanticBucket.NATURAL, "tulip", 75),
            new Rule(SemanticBucket.NATURAL, "orchid", 75),
            new Rule(SemanticBucket.NATURAL, "allium", 75),
            new Rule(SemanticBucket.NATURAL, "daisy", 75),
            new Rule(SemanticBucket.NATURAL, "peony", 75),
            new Rule(SemanticBucket.NATURAL, "lilac", 75),
            new Rule(SemanticBucket.NATURAL, "poppy", 75),
            new Rule(SemanticBucket.NATURAL, "dandelion", 75),
            new Rule(SemanticBucket.NATURAL, "azalea", 75),
            new Rule(SemanticBucket.NATURAL, "seeds", 75),
            new Rule(SemanticBucket.NATURAL, "seed", 75),
            new Rule(SemanticBucket.NATURAL, "wheat", 75),
            new Rule(SemanticBucket.NATURAL, "carrot", 75),
            new Rule(SemanticBucket.NATURAL, "potato", 75),
            new Rule(SemanticBucket.NATURAL, "beetroot", 75),
            new Rule(SemanticBucket.NATURAL, "pumpkin", 75),
            new Rule(SemanticBucket.NATURAL, "melon", 75),
            new Rule(SemanticBucket.NATURAL, "kelp", 75),
            new Rule(SemanticBucket.NATURAL, "coral", 75),
            new Rule(SemanticBucket.NATURAL, "nylium", 75),
            new Rule(SemanticBucket.NATURAL, "mycelium", 75),
            new Rule(SemanticBucket.NATURAL, "moss", 75),
            new Rule(SemanticBucket.NATURAL, "mangrove_roots", 75),
            new Rule(SemanticBucket.NATURAL, "roots", 75),
            new Rule(SemanticBucket.NATURAL, "froglight", 75),
            new Rule(SemanticBucket.NATURAL, "shroomlight", 75),
            new Rule(SemanticBucket.NATURAL, "mushroom", 75),
            new Rule(SemanticBucket.NATURAL, "hay_block", 75),
            new Rule(SemanticBucket.NATURAL, "dirt_path", 75),
            new Rule(SemanticBucket.NATURAL, "vines", 75),
            new Rule(SemanticBucket.NATURAL, "twisting_vines", 75),
            new Rule(SemanticBucket.NATURAL, "soul_sand", 75),
            new Rule(SemanticBucket.NATURAL, "soul_soil", 75),
            new Rule(SemanticBucket.NATURAL, "red_sand", 75),

            // --- Natural/Building: wood-type keywords (LOW priority so stairs/door beat them) ---
            // Bare wood types without structural keyword → Natural · Wood (log, planks, etc.)
            new Rule(SemanticBucket.NATURAL, "log", 70),
            new Rule(SemanticBucket.NATURAL, "logs", 70),
            new Rule(SemanticBucket.NATURAL, "planks", 70),
            new Rule(SemanticBucket.NATURAL, "stripped", 70),
            new Rule(SemanticBucket.NATURAL, "bamboo", 70),

            // --- Misc: templates, records, trinkets, admin blocks ---
            new Rule(SemanticBucket.MISC, "pottery_sherd", 60),
            new Rule(SemanticBucket.MISC, "trial_key", 60),
            new Rule(SemanticBucket.MISC, "ominous_trial_key", 60),
            new Rule(SemanticBucket.MISC, "trial_spawner", 60),
            new Rule(SemanticBucket.MISC, "spawner", 60),
            new Rule(SemanticBucket.MISC, "beacon", 60),
            new Rule(SemanticBucket.MISC, "end_portal_frame", 60),
            new Rule(SemanticBucket.MISC, "end_rod", 60),
            new Rule(SemanticBucket.MISC, "debug_stick", 60),
            new Rule(SemanticBucket.MISC, "jigsaw", 60),
            new Rule(SemanticBucket.MISC, "command_block", 60),
            new Rule(SemanticBucket.MISC, "chain_command_block", 60),
            new Rule(SemanticBucket.MISC, "barrier", 60),
            new Rule(SemanticBucket.MISC, "package", 60),
            new Rule(SemanticBucket.MISC, "rare_creeper_package", 60),
            new Rule(SemanticBucket.MISC, "turtle_egg", 60),
            new Rule(SemanticBucket.MISC, "sniffer_egg", 60),

            // --- Tools (rare leftovers) ---
            new Rule(SemanticBucket.TOOLS, "goggles", 55),
            new Rule(SemanticBucket.TOOLS, "wrench", 55),

            // --- Redstone secondaries ---
            new Rule(SemanticBucket.REDSTONE, "redstone_torch", 50),
            new Rule(SemanticBucket.REDSTONE, "powered_rail", 50),
            new Rule(SemanticBucket.REDSTONE, "detector_rail", 50),
            new Rule(SemanticBucket.REDSTONE, "activator_rail", 50),

            // --- Natural: coral colors, sand ---
            new Rule(SemanticBucket.NATURAL, "sand", 40),

            // --- Last-resort: "_block" generic (catches unknown colored storage blocks) ---
            new Rule(SemanticBucket.MATERIALS, "block", 20)
    );

    private static final List<Rule> RULES;

    static {
        ArrayList<Rule> sorted = new ArrayList<>(RAW_RULES);
        sorted.sort(Comparator.comparingInt(Rule::priority).reversed());
        RULES = List.copyOf(sorted);
    }

    private ParentKeywordRules() {
    }

    public static SemanticBucket classifyByName(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        String normalized = "_" + path + "_";
        for (Rule rule : RULES) {
            if (normalized.contains("_" + rule.keyword() + "_")) {
                return rule.bucket();
            }
        }
        return null;
    }

    public static List<Rule> rules() {
        return RULES;
    }
}
