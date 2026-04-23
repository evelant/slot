package dev.imagio.slot.debug;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;

import java.util.HashSet;
import java.util.Set;

public final class SemanticBucketResolver {

    private static final Set<String> MATERIAL_TAGS = Set.of(
            "c:ingots", "c:gems", "c:raw_materials", "c:nuggets", "c:dusts",
            "c:ores", "c:rods", "c:raw_ores", "c:plates", "c:storage_blocks",
            "minecraft:coals", "create:crushed_raw_materials"
    );

    private static final Set<String> MECHANISM_TAGS = Set.of(
            "create:casing"
    );

    private static final Set<String> MECHANISM_NAME_TOKENS = Set.of(
            "cogwheel", "gearbox", "gearshift", "clutch", "shaft",
            "encased", "bearing", "pipe", "funnel", "chute", "belt",
            "millstone", "mechanical", "deployer", "press", "mixer",
            "depot", "contraption", "schematic"
    );

    private static final Set<String> STORAGE_TAGS = Set.of(
            "c:chests", "c:shulker_boxes", "c:barrels", "c:wooden_chests",
            "minecraft:shulker_boxes"
    );

    private static final Set<String> REDSTONE_TAGS = Set.of(
            "c:redstone_dusts"
    );

    private static final Set<String> NATURAL_TAGS = Set.of(
            "minecraft:leaves", "minecraft:saplings", "minecraft:flowers",
            "minecraft:small_flowers", "minecraft:tall_flowers", "c:seeds",
            "c:crops", "c:mushrooms", "c:fruits", "c:vegetables"
    );

    private static final Set<String> DECORATION_TAGS = Set.of(
            "c:dyes", "minecraft:banners", "minecraft:wool",
            "minecraft:wool_carpets", "minecraft:candles", "minecraft:beds"
    );

    private static final Set<String> BUILDING_TAGS = Set.of(
            "minecraft:planks", "minecraft:logs", "minecraft:walls",
            "minecraft:stairs", "minecraft:slabs", "minecraft:stone_bricks",
            "minecraft:fence_gates", "minecraft:wooden_fences",
            "minecraft:wooden_doors", "minecraft:wooden_trapdoors",
            "c:cobblestones", "c:stones", "c:sandstone_blocks"
    );

    private static final Set<String> FOOD_TAGS = Set.of(
            "c:foods", "minecraft:fishes", "c:cooked_fishes", "c:raw_fishes"
    );

    private static final Set<String> COMBAT_TAGS = Set.of(
            "minecraft:swords", "c:tools/sword", "c:tools/shield",
            "c:weapons/sword", "c:ranged_weapons"
    );

    private static final Set<String> TOOL_TAGS = Set.of(
            "minecraft:pickaxes", "minecraft:axes", "minecraft:shovels",
            "minecraft:hoes", "c:tools", "c:tools/pickaxe", "c:tools/axe",
            "c:tools/shovel", "c:tools/hoe", "c:tools/shears",
            "c:tools/fishing_rod"
    );

    private SemanticBucketResolver() {
    }

    public static SemanticBucket classify(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return SemanticBucket.MISC;
        }
        Set<String> tags = collectTags(stack);
        Item item = stack.getItem();

        try {
            if (item instanceof ArmorItem) {
                return SemanticBucket.ARMOR;
            }
            if (item instanceof SwordItem
                    || item instanceof TridentItem
                    || item instanceof BowItem
                    || item instanceof CrossbowItem
                    || item instanceof MaceItem
                    || item instanceof ShieldItem) {
                return SemanticBucket.COMBAT;
            }
            if (item instanceof PickaxeItem
                    || item instanceof net.minecraft.world.item.AxeItem
                    || item instanceof ShovelItem
                    || item instanceof HoeItem
                    || item instanceof ShearsItem
                    || item instanceof FishingRodItem
                    || item instanceof FlintAndSteelItem
                    || item instanceof DiggerItem) {
                return SemanticBucket.TOOLS;
            }
            if (stack.has(DataComponents.FOOD) || item instanceof PotionItem) {
                return SemanticBucket.FOOD;
            }
        } catch (LinkageError ignored) {
            // fall through to tag-based / block-based checks
        }

        if (anyMatch(tags, COMBAT_TAGS)) return SemanticBucket.COMBAT;
        if (anyMatch(tags, TOOL_TAGS)) return SemanticBucket.TOOLS;
        if (anyMatch(tags, FOOD_TAGS)) return SemanticBucket.FOOD;
        if (anyMatch(tags, MATERIAL_TAGS)) return SemanticBucket.MATERIALS;
        if (anyMatch(tags, MECHANISM_TAGS)) return SemanticBucket.MECHANISMS;
        if (anyMatch(tags, STORAGE_TAGS)) return SemanticBucket.STORAGE;
        if (anyMatch(tags, REDSTONE_TAGS)) return SemanticBucket.REDSTONE;
        if (anyMatch(tags, NATURAL_TAGS)) return SemanticBucket.NATURAL;
        if (anyMatch(tags, DECORATION_TAGS)) return SemanticBucket.DECORATION;
        if (anyMatch(tags, BUILDING_TAGS)) return SemanticBucket.BUILDING;

        try {
            if (item instanceof BlockItem blockItem) {
                SemanticBucket blockBucket = classifyBlock(blockItem.getBlock());
                if (blockBucket != null) {
                    return blockBucket;
                }
            }
        } catch (LinkageError ignored) {
        }

        if (matchesMechanismNameToken(stack)) {
            return SemanticBucket.MECHANISMS;
        }

        String path = pathOf(stack);
        SemanticBucket keywordBucket = ParentKeywordRules.classifyByName(path);
        if (keywordBucket != null) {
            return keywordBucket;
        }

        return SemanticBucket.MISC;
    }

    private static String pathOf(ItemStack stack) {
        try {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key == null) {
                return "";
            }
            return key.getPath();
        } catch (RuntimeException | LinkageError ignored) {
            return "";
        }
    }

    private static SemanticBucket classifyBlock(Block block) {
        if (block == null) {
            return null;
        }
        if (block instanceof ChestBlock
                || block instanceof BarrelBlock
                || block instanceof ShulkerBoxBlock
                || block instanceof HopperBlock
                || block instanceof DispenserBlock) {
            return SemanticBucket.STORAGE;
        }
        if (block instanceof RedStoneWireBlock
                || block instanceof RepeaterBlock
                || block instanceof ComparatorBlock
                || block instanceof DiodeBlock
                || block instanceof LeverBlock) {
            return SemanticBucket.REDSTONE;
        }
        if (block instanceof LeavesBlock
                || block instanceof SaplingBlock
                || block instanceof FlowerBlock
                || block instanceof CropBlock
                || block instanceof BushBlock) {
            return SemanticBucket.NATURAL;
        }
        if (block instanceof BedBlock
                || block instanceof CarpetBlock
                || block instanceof CandleBlock
                || block instanceof TorchBlock) {
            return SemanticBucket.DECORATION;
        }
        if (block instanceof StairBlock
                || block instanceof SlabBlock
                || block instanceof WallBlock
                || block instanceof FenceBlock
                || block instanceof FenceGateBlock
                || block instanceof DoorBlock
                || block instanceof TrapDoorBlock) {
            return SemanticBucket.BUILDING;
        }
        return null;
    }

    private static boolean matchesMechanismNameToken(ItemStack stack) {
        String path;
        try {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key == null) {
                return false;
            }
            path = key.getPath();
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalized = "_" + path + "_";
        for (String token : MECHANISM_NAME_TOKENS) {
            if (normalized.contains("_" + token + "_")) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> collectTags(ItemStack stack) {
        HashSet<String> tags = new HashSet<>();
        try {
            stack.getTags().forEach(tagKey -> {
                ResourceLocation location = tagKey.location();
                if (location != null) {
                    tags.add(location.getNamespace() + ":" + location.getPath());
                }
            });
        } catch (LinkageError ignored) {
        }
        return tags;
    }

    private static boolean anyMatch(Set<String> tags, Set<String> candidates) {
        if (tags.isEmpty()) {
            return false;
        }
        for (String candidate : candidates) {
            if (tags.contains(candidate)) {
                return true;
            }
        }
        return false;
    }
}
