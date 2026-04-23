package dev.imagio.slot.debug;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class SubBucketRules {
    private static final List<SubBucketRule> ALL = List.of(
            // BUILDING — trim beats wood/stone/copper so "cut_copper_stairs" lands in Trim
            new SubBucketRule(SemanticBucket.BUILDING, "building-trim", "Building · Trim",
                    100,
                    List.of("stairs", "door", "trapdoor", "wall", "fence", "fence_gate",
                            "gate", "pane", "pressure_plate", "button", "bars")),
            new SubBucketRule(SemanticBucket.BUILDING, "building-glass", "Building · Glass",
                    85,
                    List.of("glass")),
            new SubBucketRule(SemanticBucket.BUILDING, "building-concrete", "Building · Concrete",
                    80,
                    List.of("concrete", "concrete_powder")),
            new SubBucketRule(SemanticBucket.BUILDING, "building-glazed", "Building · Glazed",
                    80,
                    List.of("glazed_terracotta")),
            new SubBucketRule(SemanticBucket.BUILDING, "building-copper", "Building · Copper",
                    60,
                    List.of("copper", "oxidized", "weathered", "exposed", "waxed")),
            new SubBucketRule(SemanticBucket.BUILDING, "building-wood", "Building · Wood",
                    50,
                    List.of("oak", "birch", "spruce", "acacia", "dark_oak", "jungle",
                            "pale_oak", "mangrove", "cherry", "bamboo", "crimson", "warped",
                            "log", "logs", "plank", "planks", "stripped", "wood")),
            new SubBucketRule(SemanticBucket.BUILDING, "building-stone", "Building · Stone",
                    40,
                    List.of("stone", "granite", "diorite", "andesite", "tuff", "calcite",
                            "cobblestone", "cobbled_deepslate", "deepslate", "blackstone",
                            "basalt", "sandstone", "bricks", "brick")),

            // MATERIALS
            new SubBucketRule(SemanticBucket.MATERIALS, "materials-metals", "Materials · Metals",
                    60,
                    List.of("ingot", "nugget", "plate", "sheet")),
            new SubBucketRule(SemanticBucket.MATERIALS, "materials-raw-ores", "Materials · Ores & Raw",
                    50,
                    List.of("ore", "raw", "crushed")),
            new SubBucketRule(SemanticBucket.MATERIALS, "materials-gems", "Materials · Gems",
                    45,
                    List.of("diamond", "emerald", "amethyst", "quartz", "lapis",
                            "ruby", "sapphire", "gem", "shard", "crystal")),
            new SubBucketRule(SemanticBucket.MATERIALS, "materials-dusts", "Materials · Dusts",
                    40,
                    List.of("dust", "powder")),
            new SubBucketRule(SemanticBucket.MATERIALS, "materials-storage-blocks", "Materials · Storage Blocks",
                    35,
                    List.of("block")),

            // NATURAL
            new SubBucketRule(SemanticBucket.NATURAL, "natural-leaves", "Natural · Leaves & Saplings",
                    60,
                    List.of("leaves", "sapling")),
            new SubBucketRule(SemanticBucket.NATURAL, "natural-flowers", "Natural · Flowers",
                    50,
                    List.of("flower", "rose", "tulip", "orchid", "daisy", "allium",
                            "peony", "lilac", "poppy", "dandelion", "azalea")),
            new SubBucketRule(SemanticBucket.NATURAL, "natural-crops", "Natural · Crops & Seeds",
                    40,
                    List.of("seed", "seeds", "wheat", "carrot", "potato", "beetroot",
                            "melon", "pumpkin")),

            // DECORATION
            new SubBucketRule(SemanticBucket.DECORATION, "decoration-banners", "Decoration · Banners",
                    70,
                    List.of("banner")),
            new SubBucketRule(SemanticBucket.DECORATION, "decoration-wool", "Decoration · Wool & Carpets",
                    60,
                    List.of("wool", "carpet")),
            new SubBucketRule(SemanticBucket.DECORATION, "decoration-dyes", "Decoration · Dyes",
                    50,
                    List.of("dye")),
            new SubBucketRule(SemanticBucket.DECORATION, "decoration-lights", "Decoration · Lights",
                    40,
                    List.of("candle", "torch", "lantern", "lamp")),

            // MECHANISMS — keeps parity with the resolver's name-token fallback
            new SubBucketRule(SemanticBucket.MECHANISMS, "mechanisms-kinetics", "Mechanisms · Kinetics",
                    60,
                    List.of("shaft", "cogwheel", "gearbox", "gearshift", "clutch",
                            "bearing", "crank")),
            new SubBucketRule(SemanticBucket.MECHANISMS, "mechanisms-logistics", "Mechanisms · Logistics",
                    50,
                    List.of("pipe", "funnel", "chute", "belt", "depot",
                            "portable_storage_interface")),
            new SubBucketRule(SemanticBucket.MECHANISMS, "mechanisms-contraptions", "Mechanisms · Contraptions",
                    40,
                    List.of("mechanical", "deployer", "press", "mixer", "millstone",
                            "crushing", "fan", "contraption")),
            new SubBucketRule(SemanticBucket.MECHANISMS, "mechanisms-casings", "Mechanisms · Casings",
                    30,
                    List.of("casing")),

            // MISC — these catch stuff that slips through everything else
            new SubBucketRule(SemanticBucket.MISC, "misc-spawn-eggs", "Misc · Spawn Eggs",
                    80,
                    List.of("spawn_egg")),
            new SubBucketRule(SemanticBucket.MISC, "misc-music-discs", "Misc · Music Discs",
                    70,
                    List.of("music_disc", "disc")),
            new SubBucketRule(SemanticBucket.MISC, "misc-transport", "Misc · Transport",
                    60,
                    List.of("boat", "raft", "minecart", "saddle", "elytra")),
            new SubBucketRule(SemanticBucket.MISC, "misc-utility", "Misc · Utility",
                    50,
                    List.of("bucket", "compass", "clock", "map", "lead", "name_tag",
                            "spyglass", "totem", "ender_pearl", "eye_of_ender", "nether_star")),
            new SubBucketRule(SemanticBucket.MISC, "misc-brewing", "Misc · Brewing",
                    45,
                    List.of("potion", "bottle", "blaze_powder", "glistering", "fermented",
                            "magma_cream", "ghast_tear", "nether_wart")),
            new SubBucketRule(SemanticBucket.MISC, "misc-books", "Misc · Books & Paper",
                    40,
                    List.of("book", "paper", "map"))
    );

    private static final Map<SemanticBucket, List<SubBucketRule>> BY_PARENT = groupByParent(ALL);

    private SubBucketRules() {
    }

    public static List<SubBucketRule> rulesFor(SemanticBucket parent) {
        return BY_PARENT.getOrDefault(parent, List.of());
    }

    public static List<SubBucketRule> all() {
        return ALL;
    }

    private static Map<SemanticBucket, List<SubBucketRule>> groupByParent(List<SubBucketRule> rules) {
        EnumMap<SemanticBucket, List<SubBucketRule>> map = new EnumMap<>(SemanticBucket.class);
        for (SubBucketRule rule : rules) {
            map.computeIfAbsent(rule.parent(), parent -> new ArrayList<>()).add(rule);
        }
        for (Map.Entry<SemanticBucket, List<SubBucketRule>> entry : map.entrySet()) {
            entry.getValue().sort(Comparator.comparingInt(SubBucketRule::priority).reversed());
        }
        EnumMap<SemanticBucket, List<SubBucketRule>> frozen = new EnumMap<>(SemanticBucket.class);
        for (Map.Entry<SemanticBucket, List<SubBucketRule>> entry : map.entrySet()) {
            frozen.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return frozen;
    }
}
