package dev.imagio.slot.client.policy;

import dev.imagio.slot.client.category.CategoryResolver;
import dev.imagio.slot.client.category.CategorySignal;
import dev.imagio.slot.client.category.SlotCategory;
import dev.imagio.slot.client.model.ItemIdentitySupport;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.EnumSet;
import java.util.Set;

final class ItemCategoryHeuristics {
    private static final Set<String> STORAGE_TOKENS = Set.of(
            "backpack", "barrel", "belt", "box", "bundle", "cache", "cart", "chest", "chute",
            "conveyor", "crate", "drawer", "funnel", "hopper", "locker", "pack", "pipe", "pouch",
            "satchel", "shulker", "tank", "tunnel", "vault"
    );
    private static final Set<String> MACHINE_TOKENS = Set.of(
            "anvil", "assembler", "altar", "blast_furnace", "campfire", "crafter", "crafting_table",
            "crusher", "deployer", "drill", "engine", "fan", "furnace", "generator", "grindstone",
            "loom", "mill", "mixer", "press", "pump", "saw", "smithing_table", "smoker",
            "station", "stonecutter", "workbench"
    );
    private static final Set<String> COMPONENT_TOKENS = Set.of(
            "alloy", "bearing", "cable", "casing", "chain", "chip", "chassis", "circuit", "coil", "cog",
            "core", "gear", "gearbox", "module", "motor", "plate", "processor", "rod", "shaft", "spring",
            "upgrade", "valve", "wire"
    );
    private static final Set<String> MATERIAL_TOKENS = Set.of(
            "amethyst", "bone", "clay", "copper", "crystal", "diamond", "dust", "emerald", "experience",
            "gem", "gold", "hide", "ingot", "iron", "lapis", "leather", "netherite", "nugget",
            "ore", "pearl", "quartz", "raw", "redstone", "resin", "scrap", "shard", "slime", "string", "xp"
    );
    private static final Set<String> DECORATION_TOKENS = Set.of(
            "banner", "bed", "bookcase", "bookshelf", "candle", "carpet", "chair", "curtain",
            "frame", "head", "lantern", "lamp", "painting", "petal", "postbox", "sign",
            "skull", "sofa", "statue", "torch"
    );
    private static final Set<String> BUILDING_TOKENS = Set.of(
            "bars", "beam", "brick", "bricks", "concrete", "door", "fence", "gate", "glass", "hyphae",
            "log", "pane", "pillar", "planks", "slab", "slabs", "stairs",
            "terracotta", "tile", "tiles", "trapdoor", "wall", "walls", "wood"
    );
    private static final Set<String> NATURE_TOKENS = Set.of(
            "apricorn", "bamboo", "berry", "cactus", "crop", "fern", "flower", "grass", "kelp",
            "leaf", "leaves", "mushroom", "petal", "sapling", "seed", "soil", "sprout", "vine"
    );
    private static final Set<String> CONSUMABLE_TOKENS = Set.of(
            "drink", "elixir", "ether", "food", "juice", "potion", "remedy", "revive", "tea", "tonic"
    );
    private static final Set<String> COBBLEMON_CONSUMABLE_TOKENS = Set.of(
            "ether", "healing", "medicine", "potion", "restore", "revive"
    );
    private static final Set<String> COBBLEMON_NATURE_TOKENS = Set.of(
            "apricorn", "berry", "bug"
    );
    private static final Set<String> COBBLEMON_MATERIAL_TOKENS = Set.of(
            "cable", "stone"
    );
    private static final Set<String> TOOL_USE_TOKENS = Set.of(
            "compass", "flute", "mirror", "radio", "rod", "scope", "scanner", "wand"
    );
    private static final Set<String> STONE_BUILDING_TOKENS = Set.of(
            "andesite", "basalt", "brick", "bricks", "calcite", "cobble", "cobbled", "deepslate",
            "diorite", "granite", "limestone", "marble", "mossy", "quartz", "sandstone", "slate",
            "stone", "terracotta", "tuff"
    );
    private static final CategoryResolver CATEGORY_RESOLVER = createCategoryResolver();

    private ItemCategoryHeuristics() {
    }

    static CategoryResolver categoryResolver() {
        return CATEGORY_RESOLVER;
    }

    static EnumSet<CategorySignal> collectSignals(ItemStack stack) {
        EnumSet<CategorySignal> signals = EnumSet.noneOf(CategorySignal.class);
        if (isStorage(stack)) {
            signals.add(CategorySignal.STORAGE);
        }
        if (isMachine(stack)) {
            signals.add(CategorySignal.MACHINE);
        }
        if (isWearable(stack)) {
            signals.add(CategorySignal.WEARABLE);
        }
        if (isCombat(stack)) {
            signals.add(CategorySignal.COMBAT);
        }
        if (isConsumable(stack)) {
            signals.add(CategorySignal.CONSUMABLE);
        }
        if (isNature(stack)) {
            signals.add(CategorySignal.NATURE);
        }
        if (isTool(stack)) {
            signals.add(CategorySignal.TOOL);
        }
        if (isComponent(stack)) {
            signals.add(CategorySignal.COMPONENT);
        }
        if (isMaterial(stack)) {
            signals.add(CategorySignal.MATERIAL);
        }
        if (isDecoration(stack)) {
            signals.add(CategorySignal.DECORATION);
        }
        if (isBuilding(stack)) {
            signals.add(CategorySignal.BUILDING);
        }
        return signals;
    }

    static boolean supportsStableMovableIdentity(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (isPortableContainerStack(stack)) {
            return true;
        }
        if (isWearable(stack) || isCombat(stack) || isTool(stack)) {
            return true;
        }
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return ItemIdentitySupport.usesStableMovableIdentity(itemId);
    }

    static ItemBehaviorPolicy.DirectInventoryAction resolveDirectInventoryAction(ItemStack stack, boolean portableContainer) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        Item item = stack.getItem();
        String path = path(stack);
        if (item instanceof BlockItem) {
            return ItemBehaviorPolicy.DirectInventoryAction.PLACE;
        }
        if (stack.has(DataComponents.FOOD)
                || item instanceof PotionItem
                || item instanceof BucketItem
                || item instanceof CompassItem
                || item instanceof FishingRodItem
                || item instanceof FlintAndSteelItem
                || item instanceof SpawnEggItem) {
            return ItemBehaviorPolicy.DirectInventoryAction.USE;
        }
        if ("cobblemon".equals(namespace(stack))) {
            return hasAnyToken(path, COBBLEMON_CONSUMABLE_TOKENS) || hasAnyToken(path, Set.of("berry", "bait"))
                    ? ItemBehaviorPolicy.DirectInventoryAction.USE
                    : null;
        }
        ItemBehaviorPolicy.DirectInventoryAction tokenAction = hasAnyToken(path, CONSUMABLE_TOKENS) || hasAnyToken(path, TOOL_USE_TOKENS)
                ? ItemBehaviorPolicy.DirectInventoryAction.USE
                : null;
        if (tokenAction != null) {
            return tokenAction;
        }
        return null;
    }

    static boolean isPortableContainerStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.has(DataComponents.CONTAINER) || stack.has(DataComponents.BUNDLE_CONTENTS)) {
            return true;
        }
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            return true;
        }
        return ItemHeuristics.hasPortableContainerFallbackToken(itemId(stack));
    }

    static boolean isImplicitJunkCandidateStack(ItemStack stack, boolean portableContainer) {
        if (stack == null || stack.isEmpty() || portableContainer || !"minecraft".equals(namespace(stack))) {
            return false;
        }

        String path = path(stack);
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof LeavesBlock
                    || block instanceof SaplingBlock
                    || block.defaultBlockState().is(BlockTags.LEAVES)
                    || block.defaultBlockState().is(BlockTags.DIRT)
                    || block.defaultBlockState().is(BlockTags.SAPLINGS)
                    || block.defaultBlockState().is(BlockTags.STAIRS)) {
                return true;
            }
        }
        return ItemHeuristics.isConservativeVanillaAutoJunkItemId(itemId(stack));
    }

    private static boolean isStorage(ItemStack stack) {
        String path = path(stack);
        Item item = stack.getItem();
        return hasAnyToken(path, STORAGE_TOKENS)
                || item instanceof BucketItem;
    }

    private static boolean isMachine(ItemStack stack) {
        return hasAnyToken(path(stack), MACHINE_TOKENS);
    }

    private static boolean isWearable(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ArmorItem || item instanceof ElytraItem;
    }

    private static boolean isCombat(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SwordItem
                || item instanceof ProjectileWeaponItem
                || item instanceof TridentItem
                || item instanceof ShieldItem
                || item instanceof ArrowItem;
    }

    private static boolean isConsumable(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof PotionItem
                || stack.has(DataComponents.FOOD)
                || hasAnyToken(path(stack), CONSUMABLE_TOKENS)
                || ("cobblemon".equals(namespace(stack)) && hasAnyToken(path(stack), COBBLEMON_CONSUMABLE_TOKENS));
    }

    private static boolean isNature(ItemStack stack) {
        String path = path(stack);
        Item item = stack.getItem();
        if (item instanceof SpawnEggItem) {
            return true;
        }
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof FlowerBlock
                    || block instanceof SaplingBlock
                    || block instanceof CropBlock
                    || block instanceof LeavesBlock
                    || block instanceof BushBlock) {
                return true;
            }
        }
        return hasAnyToken(path, NATURE_TOKENS)
                || ("cobblemon".equals(namespace(stack)) && hasAnyToken(path, COBBLEMON_NATURE_TOKENS));
    }

    private static boolean isTool(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DiggerItem
                || item instanceof AxeItem
                || item instanceof ShearsItem
                || item instanceof FlintAndSteelItem
                || item instanceof FishingRodItem
                || item instanceof CompassItem
                || path(stack).contains("clock");
    }

    private static boolean isComponent(ItemStack stack) {
        return hasAnyToken(path(stack), COMPONENT_TOKENS);
    }

    private static boolean isMaterial(ItemStack stack) {
        String path = path(stack);
        return hasAnyToken(path, MATERIAL_TOKENS)
                || isResourceBlock(path)
                || ("cobblemon".equals(namespace(stack)) && hasAnyToken(path, COBBLEMON_MATERIAL_TOKENS));
    }

    private static boolean isDecoration(ItemStack stack) {
        Item item = stack.getItem();
        String path = path(stack);
        return hasAnyToken(path, DECORATION_TOKENS)
                || (item instanceof BlockItem && (path.contains("lantern") || path.contains("banner") || path.contains("sign")));
    }

    private static boolean isBuilding(ItemStack stack) {
        String path = path(stack);
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        Block block = blockItem.getBlock();
        return hasAnyToken(path, BUILDING_TOKENS)
                || path.equals("copycat_panel")
                || path.equals("copycat_step")
                || hasAnyToken(path, STONE_BUILDING_TOKENS)
                || block.defaultBlockState().is(BlockTags.PLANKS)
                || block.defaultBlockState().is(BlockTags.SLABS)
                || block.defaultBlockState().is(BlockTags.STAIRS)
                || block.defaultBlockState().is(BlockTags.WALLS)
                || block.defaultBlockState().is(BlockTags.FENCES)
                || block.defaultBlockState().is(BlockTags.FENCE_GATES)
                || block.defaultBlockState().is(BlockTags.DOORS)
                || block.defaultBlockState().is(BlockTags.TRAPDOORS)
                || block.defaultBlockState().is(BlockTags.LOGS);
    }

    private static String path(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }

    private static String namespace(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
    }

    private static boolean hasAnyToken(String path, Set<String> tokens) {
        for (String token : tokens) {
            if (hasToken(path, token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasToken(String path, String token) {
        String normalizedPath = "_" + path + "_";
        String normalizedToken = "_" + token + "_";
        return normalizedPath.contains(normalizedToken);
    }

    private static boolean isResourceBlock(String path) {
        return path.endsWith("_block")
                && hasAnyToken(path, Set.of("amethyst", "coal", "copper", "diamond", "emerald", "experience", "gold", "iron", "lapis", "netherite", "quartz", "redstone", "xp"));
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static CategoryResolver createCategoryResolver() {
        return new CategoryResolver()
                .addExactOverride("comforts:sleeping_bag", SlotCategory.TOOLS_AND_UTILITY)
                .addExactOverride("create:gearbox", SlotCategory.MACHINES_AND_WORKSTATIONS)
                .addExactOverride("create:track", SlotCategory.STORAGE_AND_TRANSPORT)
                .addExactOverride("minecraft:coal", SlotCategory.MATERIALS)
                .addExactOverride("minecraft:glass_bottle", SlotCategory.COMPONENTS)
                .addExactOverride("minecraft:stick", SlotCategory.COMPONENTS)
                .addExactOverride("minecraft:kelp", SlotCategory.NATURE_AND_FARMING)
                .addExactOverride("naturescompass:natures_compass", SlotCategory.TOOLS_AND_UTILITY)
                .addExactOverride("cobblemon:amethyst_bug", SlotCategory.NATURE_AND_FARMING)
                .addExactOverride("cobblemon:ether", SlotCategory.CONSUMABLES)
                .addExactOverride("cobblemon:hyper_potion", SlotCategory.CONSUMABLES)
                .addExactOverride("cobblemon:link_cable", SlotCategory.COMPONENTS)
                .addExactOverride("cobblemon:shiny_stone", SlotCategory.MATERIALS)
                .addExactOverride("minecraft:copper_block", SlotCategory.MATERIALS)
                .addExactOverride("minecraft:gold_block", SlotCategory.MATERIALS)
                .addExactOverride("minecraft:experience_bottle", SlotCategory.CONSUMABLES)
                .addExactOverride("minecraft:stone", SlotCategory.BUILDING)
                .addExactOverride("minecraft:stone_bricks", SlotCategory.BUILDING);
    }
}
