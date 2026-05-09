package dev.imagio.slot.forge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.imagio.slot.debug.ChestContentEntry;
import dev.imagio.slot.debug.ChestSpec;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackSupport;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.debug.FacetIndexTemplateClassifier;
import dev.imagio.slot.debug.PopulateProfile;
import dev.imagio.slot.debug.RealisticAtlasGenerator;
import dev.imagio.slot.debug.RealisticAtlasPlan;
import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.storage.ForgeCarriedActivityTracker;
import dev.imagio.slot.forge.storage.ForgeChestStorageAnchors;
import dev.imagio.slot.forge.storage.ForgeChestStorageIds;
import dev.imagio.slot.forge.triage.Forge120IslandSignalExtractor;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.session.InventoryAcquisitionActivityRecorder;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasWorkflowDomainService;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = SlotForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeSlotTestCommands {
    private static final SuggestionProvider<CommandSourceStack> PROFILE_SUGGESTIONS =
            ForgeSlotTestCommands::suggestProfiles;

    /**
     * Match the NeoForge debug generator: guarantee real high-capacity
     * backpack carriers before test stacks route through carried storage.
     */
    private static final int TOP_TIER_BACKPACK_TARGET = 3;

    private static final List<String> BACKPACK_TIER_FALLBACK = List.of(
            "sophisticatedbackpacks:netherite_backpack",
            "sophisticatedbackpacks:diamond_backpack",
            "sophisticatedbackpacks:gold_backpack",
            "sophisticatedbackpacks:iron_backpack",
            "sophisticatedbackpacks:backpack"
    );

    private ForgeSlotTestCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("slot")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("test")
                        .then(Commands.literal("populate")
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .suggests(PROFILE_SUGGESTIONS)
                                        .executes(ForgeSlotTestCommands::runPopulate)))
                        .then(Commands.literal("clear")
                                .executes(ForgeSlotTestCommands::runClear)));
        dispatcher.register(root);
    }

    private static CompletableFuture<Suggestions> suggestProfiles(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (PopulateProfile profile : PopulateProfile.values()) {
            if (profile.id().startsWith(remaining)) {
                builder.suggest(profile.id());
            }
        }
        return builder.buildFuture();
    }

    private static int runPopulate(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String profileArg = StringArgumentType.getString(context, "profile");
        PopulateProfile profile = PopulateProfile.fromId(profileArg);
        if (profile == null) {
            context.getSource().sendFailure(Component.literal(
                    "[SLOT] populate: unknown profile '" + profileArg + "'"));
            return 0;
        }

        List<ItemStack> pool = nonEmptyItemStackPool();
        if (pool.isEmpty()) {
            context.getSource().sendFailure(Component.literal(
                    "[SLOT] populate: no items available from registry"));
            return 0;
        }

        FacetIndexTemplateClassifier classifier =
                new FacetIndexTemplateClassifier(Forge120IslandSignalExtractor::extract);
        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool,
                profile,
                new Random(),
                classifier::describe);

        WorkflowDomainRuntime runtime = ForgePlayerWorkflowRuntimeService.runtime(player);
        VisualAtlasWorkflowDomainService workflow = runtime.visualAtlasWorkflow();
        int islandsCreated = applyIslands(workflow, plan);
        int assignmentsApplied = applyAssignments(workflow, plan);
        int backpacksGranted = ensureTopTierBackpacks(player);
        int readableBackpacks = SophisticatedBackpackSupport.readPlayerBackpacks(player, null).size();
        String backpackDiagnostic = backpackRoutingDiagnostic(player, readableBackpacks);
        if (!backpackDiagnostic.isBlank()) {
            context.getSource().sendFailure(Component.literal("[SLOT] forge populate: " + backpackDiagnostic));
            SlotCommon.LOGGER.warn("[SLOT] /slot test populate forge aborted: {}", backpackDiagnostic);
            return 0;
        }
        InventoryGiveResult giveResult = giveStacksToPlayer(player, runtime, plan);
        ForgeCarriedActivityTracker.suppressNext(player);
        ChestPopulateResult chestResult = placeChests(player, runtime, plan);

        int finalIslands = islandsCreated;
        int finalAssignments = assignmentsApplied;
        int finalMain = giveResult.mainInserted;
        int finalBackpack = giveResult.backpackInserted;
        int finalDropped = giveResult.dropped;
        int finalChests = chestResult.chestsPlaced;
        int finalChestItems = chestResult.itemsStocked;
        int finalBackpacksGranted = backpacksGranted;
        int finalReadableBackpacks = readableBackpacks;
        context.getSource().sendSuccess(() -> Component.literal(String.format(
                "[SLOT] forge populate %s: islands=%d assignments=%d stacks main=%d backpack=%d dropped=%d chests=%d chestItems=%d backpacks_granted=%d readable_backpacks=%d",
                profile.id(), finalIslands, finalAssignments, finalMain, finalBackpack, finalDropped,
                finalChests, finalChestItems, finalBackpacksGranted, finalReadableBackpacks
        )), false);
        SlotCommon.LOGGER.info(
                "[SLOT] /slot test populate forge profile={} -> islands={} assignments={} stacks_main={} stacks_backpack={} stacks_dropped={} chests={} chestItems={} backpacks_granted={} readable_backpacks={}",
                profile.id(), finalIslands, finalAssignments, finalMain, finalBackpack, finalDropped,
                finalChests, finalChestItems, finalBackpacksGranted, finalReadableBackpacks);
        return finalAssignments;
    }

    private static int runClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        WorkflowDomainRuntime runtime = ForgePlayerWorkflowRuntimeService.runtime(player);
        VisualAtlasWorkflowDomainService workflow = runtime.visualAtlasWorkflow();

        int clearedAssignments = 0;
        Map<ItemIdentity, VisualHomeAssignment> assignments =
                new LinkedHashMap<>(workflow.visualHomeMap().assignments());
        for (ItemIdentity itemIdentity : assignments.keySet()) {
            if (workflow.clearHome(itemIdentity)) {
                clearedAssignments++;
            }
        }

        int deletedIslands = 0;
        ArrayList<String> islandIds = new ArrayList<>();
        for (VisualAtlasIsland island : workflow.visualHomeMap().playerIslands()) {
            islandIds.add(island.id());
        }
        for (String islandId : islandIds) {
            if (workflow.deleteIsland(islandId)) {
                deletedIslands++;
            }
        }

        ChestClearResult chestClear = clearClaimedChests(player, runtime);
        InventoryClearResult inventoryResult = clearPlayerInventory(player);
        int finalDeletedIslands = deletedIslands;
        int finalClearedAssignments = clearedAssignments;
        int finalChestClaims = chestClear.claimsCleared;
        int finalChestBlocks = chestClear.blocksRemoved;
        int finalInventoryCleared = inventoryResult.slotsCleared;
        int finalBackpacksPreserved = inventoryResult.backpacksPreserved;
        int finalBackpackContents = inventoryResult.backpackContentsCleared;
        context.getSource().sendSuccess(() -> Component.literal(String.format(
                "[SLOT] forge test clear: islands=%d assignments=%d inventory=%d backpacks_kept=%d backpack_contents=%d chestClaims=%d chestBlocks=%d",
                finalDeletedIslands, finalClearedAssignments, finalInventoryCleared,
                finalBackpacksPreserved, finalBackpackContents,
                finalChestClaims, finalChestBlocks
        )), false);
        SlotCommon.LOGGER.info(
                "[SLOT] /slot test clear forge -> islands={} assignments={} inventory={} backpacks_preserved={} backpack_contents_cleared={} chestClaims={} chestBlocks={}",
                finalDeletedIslands, finalClearedAssignments, finalInventoryCleared,
                finalBackpacksPreserved, finalBackpackContents,
                finalChestClaims, finalChestBlocks);
        return finalDeletedIslands;
    }

    private static int ensureTopTierBackpacks(ServerPlayer player) {
        Item backpackItem = resolveTopTierBackpackItem();
        if (backpackItem == null) {
            return 0;
        }
        NonNullList<ItemStack> items = player.getInventory().items;
        int existing = 0;
        for (ItemStack stack : items) {
            if (stack != null && !stack.isEmpty() && stack.getItem() == backpackItem) {
                existing++;
            }
        }
        int needed = TOP_TIER_BACKPACK_TARGET - existing;
        int granted = 0;
        for (int i = 0; i < needed; i++) {
            if (player.getInventory().add(new ItemStack(backpackItem, 1))) {
                granted++;
            }
        }
        return granted;
    }

    private static boolean hasTopTierBackpack(ServerPlayer player) {
        Item backpackItem = resolveTopTierBackpackItem();
        if (backpackItem == null) {
            return false;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack != null && !stack.isEmpty() && stack.getItem() == backpackItem) {
                return true;
            }
        }
        return false;
    }

    private static String backpackRoutingDiagnostic(ServerPlayer player, int readableBackpacks) {
        if (!hasTopTierBackpack(player)) {
            return "";
        }
        if (!SophisticatedBackpackSupport.isAvailable()) {
            return "sophisticated backpack item exists, but backpack read support is unavailable";
        }
        if (!SophisticatedBackpackTransferSupport.isAvailable()) {
            return "sophisticated backpack item exists, but backpack transfer support is unavailable";
        }
        if (readableBackpacks <= 0) {
            return "sophisticated backpack item exists, but carried-provider enumeration returned zero sources";
        }
        return "";
    }

    private static Item resolveTopTierBackpackItem() {
        for (String id : BACKPACK_TIER_FALLBACK) {
            ResourceLocation key = ResourceLocation.tryParse(id);
            if (key == null || !BuiltInRegistries.ITEM.containsKey(key)) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(key);
            if (item != null && !new ItemStack(item).isEmpty()) {
                return item;
            }
        }
        return null;
    }

    private static int applyIslands(VisualAtlasWorkflowDomainService workflow, RealisticAtlasPlan plan) {
        int islandsCreated = 0;
        for (VisualAtlasIsland island : plan.islands()) {
            VisualAtlasIsland created = workflow.createIslandWithId(
                    island.id(),
                    island.label(),
                    island.x(),
                    island.y(),
                    island.color(),
                    island.iconIdentity()
            );
            if (created != null) {
                islandsCreated++;
            }
        }
        return islandsCreated;
    }

    private static int applyAssignments(VisualAtlasWorkflowDomainService workflow, RealisticAtlasPlan plan) {
        int applied = 0;
        for (VisualHomeAssignment assignment : plan.assignments().values()) {
            VisualHomeAssignment result = workflow.assignHome(
                    assignment.identity(),
                    assignment.islandId(),
                    assignment.ordinal()
            );
            if (result != null) {
                applied++;
            }
        }
        return applied;
    }

    private static InventoryGiveResult giveStacksToPlayer(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            RealisticAtlasPlan plan
    ) {
        ArrayList<ItemStack> allStacks = new ArrayList<>(plan.homedStacks().size() + plan.triageStacks().size());
        allStacks.addAll(plan.homedStacks());
        allStacks.addAll(plan.triageStacks());

        ArrayList<ItemStack> reordered = new ArrayList<>(allStacks.size());
        for (ItemStack stack : allStacks) {
            if (stack != null && !stack.isEmpty() && isBackpackStack(stack)) {
                reordered.add(stack);
            }
        }
        for (ItemStack stack : allStacks) {
            if (stack == null || stack.isEmpty() || !isBackpackStack(stack)) {
                reordered.add(stack);
            }
        }
        allStacks = reordered;

        CarriedSourceAccess carried = StorageAccessRegistry.isInstalled()
                ? StorageAccessRegistry.carriedSourceAccess()
                : null;
        int mainInserted = 0;
        int backpackInserted = 0;
        int dropped = 0;
        for (ItemStack template : allStacks) {
            if (template == null || template.isEmpty()) {
                continue;
            }
            ItemStack stack = template.copy();
            if (stack.getCount() <= 0) {
                stack.setCount(1);
            }
            ItemStack acquiredStack = stack.copy();
            int initial = stack.getCount();
            ItemStack remaining;
            int vanillaDelta;
            if (isBackpackStack(stack) || carried == null) {
                int before = countVanillaInventory(player);
                player.getInventory().add(stack);
                int after = countVanillaInventory(player);
                vanillaDelta = Math.max(0, after - before);
                remaining = stack;
            } else {
                int before = countVanillaInventory(player);
                remaining = carried.insertBestFit(player, stack, false);
                int after = countVanillaInventory(player);
                vanillaDelta = Math.max(0, after - before);
            }
            int leftover = remaining == null || remaining.isEmpty() ? 0 : remaining.getCount();
            int placed = Math.max(0, initial - leftover);
            mainInserted += vanillaDelta;
            backpackInserted += Math.max(0, placed - vanillaDelta);
            if (placed > 0) {
                InventoryAcquisitionActivityRecorder.recordStackAcquired(
                        runtime,
                        acquiredStack,
                        placed,
                        InventoryActivityProducer.COMPATIBILITY_API,
                        InventoryActivityConfidence.AUTHORITATIVE,
                        "forge_test_populate");
            }
            if (leftover > 0) {
                dropped += leftover;
                player.drop(remaining, false);
            }
        }
        return new InventoryGiveResult(mainInserted, backpackInserted, dropped);
    }

    private static int countVanillaInventory(ServerPlayer player) {
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack != null && !stack.isEmpty()) {
                total += stack.getCount();
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack != null && !stack.isEmpty()) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static ChestPopulateResult placeChests(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            RealisticAtlasPlan plan
    ) {
        if (player == null || runtime == null || plan == null || plan.chests().isEmpty()) {
            return new ChestPopulateResult(0, 0);
        }
        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        ChestClaimWorkflowDomainService chestWorkflow = runtime.chestClaimWorkflow();
        long tick = level.getGameTime();
        int chestsPlaced = 0;
        int itemsStocked = 0;
        for (ChestSpec spec : plan.chests()) {
            if (spec == null) {
                continue;
            }
            BlockPos pos = origin.offset(spec.deltaX(), 0, spec.deltaZ());
            if (!level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            if (!level.setBlock(pos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL)) {
                SlotCommon.LOGGER.warn("[SLOT] forge populate: setBlock failed for chest at {}", pos);
                continue;
            }
            if (!ForgeChestStorageAnchors.isClaimable(level, pos)) {
                SlotCommon.LOGGER.warn("[SLOT] forge populate: placed chest is not claimable at {}", pos);
                continue;
            }
            ClaimedChest claimed = chestWorkflow.claim(
                    ForgeChestStorageAnchors.resolveAnchors(level, pos),
                    spec.deltaX() * 100,
                    spec.deltaZ() * 100,
                    chestLabel(spec)
            );
            if (claimed == null) {
                SlotCommon.LOGGER.warn("[SLOT] forge populate: claim failed for chest at {}", pos);
                continue;
            }
            ForgeChestStorageIds.write(level, pos, claimed.storageId());
            chestsPlaced++;

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ChestBlockEntity chest)) {
                continue;
            }
            int containerSize = chest.getContainerSize();
            for (ChestContentEntry entry : spec.contents()) {
                if (entry == null || entry.slot() < 0 || entry.slot() >= containerSize) {
                    continue;
                }
                ItemStack stack = entry.stack().copy();
                if (stack.isEmpty()) {
                    continue;
                }
                chest.setItem(entry.slot(), stack);
                chestWorkflow.recordDeposit(
                        claimed.storageId(),
                        ItemIdentityMatcher.create(stack),
                        stack.getCount(),
                        tick);
                itemsStocked++;
            }
            chest.setChanged();
        }
        return new ChestPopulateResult(chestsPlaced, itemsStocked);
    }

    private static String chestLabel(ChestSpec spec) {
        if (spec.areaLabel() != null && !spec.areaLabel().isBlank()) {
            return spec.areaLabel() + " " + (spec.index() + 1);
        }
        if (spec.linkedIslandId() != null && !spec.linkedIslandId().isBlank()) {
            return spec.linkedIslandId();
        }
        return "Storage " + (spec.index() + 1);
    }

    private static ChestClearResult clearClaimedChests(
            ServerPlayer player,
            WorkflowDomainRuntime runtime
    ) {
        if (player == null || runtime == null) {
            return new ChestClearResult(0, 0);
        }
        ArrayList<ClaimedChest> chests = new ArrayList<>(runtime.snapshot().claimedChestMap().chests());
        int claimsCleared = 0;
        int blocksRemoved = 0;
        for (ClaimedChest chest : chests) {
            if (chest == null) {
                continue;
            }
            for (ChestAnchor anchor : chest.anchors()) {
                ServerLevel level = level(player, anchor);
                if (level == null) {
                    continue;
                }
                BlockPos pos = new BlockPos(anchor.x(), anchor.y(), anchor.z());
                if (ForgeChestStorageIds.read(level, pos)
                        .filter(chest.storageId()::equals)
                        .isPresent()) {
                    ForgeChestStorageIds.clear(level, pos);
                    if (level.getBlockEntity(pos) instanceof ChestBlockEntity) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        blocksRemoved++;
                    }
                }
            }
            if (runtime.chestClaimWorkflow().deleteChest(chest.storageId())) {
                claimsCleared++;
            }
        }
        return new ChestClearResult(claimsCleared, blocksRemoved);
    }

    private static ServerLevel level(ServerPlayer player, ChestAnchor anchor) {
        if (player == null || anchor == null || anchor.dimensionId() == null || anchor.dimensionId().isBlank()) {
            return null;
        }
        if (player.getServer() == null) {
            return null;
        }
        for (ServerLevel level : player.getServer().getAllLevels()) {
            if (level.dimension().location().toString().equals(anchor.dimensionId())) {
                return level;
            }
        }
        return null;
    }

    private static InventoryClearResult clearPlayerInventory(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        InventoryListResult main = clearPreservingBackpacks(inventory.items);
        InventoryListResult armor = clearPreservingBackpacks(inventory.armor);
        InventoryListResult offhand = clearPreservingBackpacks(inventory.offhand);
        int backpackContents = clearPlayerBackpackContents(player);
        inventory.setChanged();
        return new InventoryClearResult(
                main.cleared + armor.cleared + offhand.cleared,
                main.preserved + armor.preserved + offhand.preserved,
                backpackContents
        );
    }

    private static InventoryListResult clearPreservingBackpacks(NonNullList<ItemStack> slots) {
        int cleared = 0;
        int preserved = 0;
        for (int index = 0; index < slots.size(); index++) {
            ItemStack stack = slots.get(index);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (isBackpackStack(stack)) {
                preserved++;
                continue;
            }
            slots.set(index, ItemStack.EMPTY);
            cleared++;
        }
        return new InventoryListResult(cleared, preserved);
    }

    private static int clearPlayerBackpackContents(ServerPlayer player) {
        int cleared = 0;
        for (SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpacks(player, null)) {
            if (snapshot == null) {
                continue;
            }
            for (SophisticatedBackpackSupport.BackpackEntry entry : snapshot.entries()) {
                ItemStack extracted = SophisticatedBackpackTransferSupport.extractBackpackSlot(
                        player,
                        snapshot.carrier(),
                        entry.slotIndex(),
                        Integer.MAX_VALUE,
                        false,
                        new LinkedHashMap<>()
                );
                if (extracted != null && !extracted.isEmpty()) {
                    cleared++;
                }
            }
        }
        return cleared;
    }

    private static boolean isBackpackStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (SophisticatedBackpackSupport.isBackpackItem(stack)) {
            return true;
        }
        try {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (key == null) {
                return false;
            }
            String path = key.getPath();
            return path != null && path.toLowerCase(Locale.ROOT).contains("backpack");
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static List<ItemStack> nonEmptyItemStackPool() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == null) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private record InventoryGiveResult(int mainInserted, int backpackInserted, int dropped) {
    }

    private record InventoryClearResult(int slotsCleared, int backpacksPreserved, int backpackContentsCleared) {
    }

    private record InventoryListResult(int cleared, int preserved) {
    }

    private record ChestPopulateResult(int chestsPlaced, int itemsStocked) {
    }

    private record ChestClearResult(int claimsCleared, int blocksRemoved) {
    }
}
