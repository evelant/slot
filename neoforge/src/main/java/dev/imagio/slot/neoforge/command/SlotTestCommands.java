package dev.imagio.slot.neoforge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.debug.ChestContentEntry;
import dev.imagio.slot.debug.ChestSpec;
import dev.imagio.slot.debug.FacetIndexTemplateClassifier;
import dev.imagio.slot.debug.PopulateProfile;
import dev.imagio.slot.debug.RealisticAtlasGenerator;
import dev.imagio.slot.debug.RealisticAtlasPlan;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.neoforge.storage.ChestClaimServerService;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.ChestLinkWorkflowDomainService;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasWorkflowDomainService;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SlotTestCommands {
    private static boolean registered;

    private static final SuggestionProvider<CommandSourceStack> PROFILE_SUGGESTIONS = SlotTestCommands::suggestProfiles;

    private SlotTestCommands() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotTestCommands::onRegisterCommands);
        registered = true;
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("slot")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("test")
                        .then(Commands.literal("populate")
                                .then(Commands.argument("profile", StringArgumentType.word())
                                        .suggests(PROFILE_SUGGESTIONS)
                                        .executes(SlotTestCommands::runPopulate)))
                        .then(Commands.literal("clear")
                                .executes(SlotTestCommands::runClear)));
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

        Random random = new Random();
        FacetIndexTemplateClassifier classifier = new FacetIndexTemplateClassifier(IslandSignalExtractor::extract);
        RealisticAtlasPlan plan = RealisticAtlasGenerator.generateWithDescriptors(
                pool, profile, random, classifier::describe);

        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        VisualAtlasWorkflowDomainService workflow = runtime.visualAtlasWorkflow();
        ChestLinkWorkflowDomainService linkWorkflow = runtime.chestLinkWorkflow();

        int islandsCreated = applyIslands(workflow, plan);
        int assignmentsApplied = applyAssignments(workflow, plan);
        InventoryGiveResult giveResult = giveStacksToPlayer(player, plan);
        ChestPlacementResult chestResult = placeChests(player, linkWorkflow, plan, random);

        int finalIslands = islandsCreated;
        int finalAssignments = assignmentsApplied;
        int finalMain = giveResult.mainInserted;
        int finalBackpack = giveResult.backpackInserted;
        int finalDropped = giveResult.dropped;
        int finalPlaced = chestResult.placed;
        int finalClaimed = chestResult.claimed;
        int finalLinked = chestResult.linked;
        int finalClaimFailed = chestResult.claimFailed;
        int finalLinkFailed = chestResult.linkFailed;

        context.getSource().sendSuccess(() -> Component.literal(String.format(
                "[SLOT] populate %s: islands=%d assignments=%d stacks main=%d backpack=%d dropped=%d chests placed=%d claimed=%d linked=%d claim_failed=%d link_failed=%d",
                profile.id(), finalIslands, finalAssignments,
                finalMain, finalBackpack, finalDropped,
                finalPlaced, finalClaimed, finalLinked, finalClaimFailed, finalLinkFailed
        )), false);
        SlotCommon.LOGGER.info(
                "[SLOT] /slot test populate profile={} -> islands={} assignments={} stacks_main={} stacks_backpack={} stacks_dropped={} chests placed={} claimed={} linked={} claim_failed={} link_failed={}",
                profile.id(), finalIslands, finalAssignments,
                finalMain, finalBackpack, finalDropped,
                finalPlaced, finalClaimed, finalLinked, finalClaimFailed, finalLinkFailed
        );
        return finalAssignments;
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

    private static InventoryGiveResult giveStacksToPlayer(ServerPlayer player, RealisticAtlasPlan plan) {
        ArrayList<ItemStack> allStacks = new ArrayList<>(plan.homedStacks().size() + plan.triageStacks().size());
        allStacks.addAll(plan.homedStacks());
        allStacks.addAll(plan.triageStacks());

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
            int beforeBackpack = stack.getCount();
            stack = insertIntoBackpacks(player, stack);
            int afterBackpack = stack == null || stack.isEmpty() ? 0 : stack.getCount();
            int movedToBackpack = beforeBackpack - afterBackpack;
            if (movedToBackpack > 0) {
                backpackInserted += movedToBackpack;
            }
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            int beforeMain = stack.getCount();
            player.getInventory().add(stack);
            int afterMain = stack.getCount();
            int movedToMain = beforeMain - afterMain;
            if (movedToMain > 0) {
                mainInserted += movedToMain;
            }
            if (!stack.isEmpty()) {
                dropped += stack.getCount();
                player.drop(stack, false);
            }
        }
        return new InventoryGiveResult(mainInserted, backpackInserted, dropped);
    }

    private static ItemStack insertIntoBackpacks(ServerPlayer player, ItemStack stack) {
        ItemStack remaining = stack;
        NonNullList<ItemStack> items = player.getInventory().items;
        for (int index = 0; index < items.size(); index++) {
            if (remaining == null || remaining.isEmpty()) {
                break;
            }
            ItemStack carrier = items.get(index);
            if (carrier == null || carrier.isEmpty() || !isBackpackStack(carrier)) {
                continue;
            }
            IItemHandler handler;
            try {
                handler = carrier.getCapability(Capabilities.ItemHandler.ITEM);
            } catch (RuntimeException | LinkageError ignored) {
                handler = null;
            }
            if (handler == null) {
                continue;
            }
            try {
                remaining = ItemHandlerHelper.insertItemStacked(handler, remaining, false);
            } catch (RuntimeException | LinkageError ignored) {
                // ignore and try next backpack
            }
        }
        return remaining;
    }

    private static ChestPlacementResult placeChests(
            ServerPlayer player,
            ChestLinkWorkflowDomainService linkWorkflow,
            RealisticAtlasPlan plan,
            Random random
    ) {
        List<ChestSpec> chests = plan.chests();
        if (chests.isEmpty()) {
            return new ChestPlacementResult(0, 0, 0, 0, 0);
        }

        ServerLevel level = player.serverLevel();
        int centerX = (int) Math.floor(player.getX());
        int centerY = (int) Math.floor(player.getY());
        int centerZ = (int) Math.floor(player.getZ());

        int placed = 0;
        int claimed = 0;
        int linked = 0;
        int claimFailed = 0;
        int linkFailed = 0;

        for (ChestSpec spec : chests) {
            BlockPos pos = new BlockPos(
                    centerX + spec.deltaX(),
                    centerY,
                    centerZ + spec.deltaZ()
            );
            // Force-replace: clear whatever is at the spot first (terrain would block
            // canBeReplaced checks in populated biomes). Debug command, ops-gated.
            BlockState previous = level.getBlockState(pos);
            if (!previous.isAir() && !previous.canBeReplaced()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            BlockState chestState = Blocks.CHEST.defaultBlockState();
            if (!level.setBlock(pos, chestState, Block.UPDATE_ALL)) {
                SlotCommon.LOGGER.warn(
                        "[SLOT] populate: setBlock failed for chest {} at {}",
                        spec.index(), pos
                );
                continue;
            }
            placed++;
            fillChestFromSpec(level, pos, spec);
            ClaimedChest claimedChest = ChestClaimServerService.claim(player, pos);
            if (claimedChest == null) {
                claimFailed++;
                SlotCommon.LOGGER.warn(
                        "[SLOT] populate: claim returned null for chest {} at {}",
                        spec.index(), pos
                );
                continue;
            }
            claimed++;
            if (spec.isLinked()) {
                boolean ok = linkWorkflow.linkIslandToChest(spec.linkedIslandId(), claimedChest.storageId());
                if (ok) {
                    linked++;
                } else {
                    linkFailed++;
                    SlotCommon.LOGGER.warn(
                            "[SLOT] populate: link failed for chest {} -> island {}",
                            claimedChest.storageId(), spec.linkedIslandId()
                    );
                }
            }
        }

        return new ChestPlacementResult(placed, claimed, linked, claimFailed, linkFailed);
    }

    private static void fillChestFromSpec(ServerLevel level, BlockPos pos, ChestSpec spec) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chest)) {
            return;
        }
        int size = chest.getContainerSize();
        for (ChestContentEntry entry : spec.contents()) {
            int slot = entry.slot();
            if (slot < 0 || slot >= size) {
                continue;
            }
            ItemStack stack = entry.stack();
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            chest.setItem(slot, stack.copy());
        }
        chest.setChanged();
    }

    private static int runClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        VisualAtlasWorkflowDomainService workflow = runtime.visualAtlasWorkflow();
        ChestClaimWorkflowDomainService chestClaimWorkflow = runtime.chestClaimWorkflow();

        ChestClearResult chestResult = clearChests(player.getServer(), chestClaimWorkflow);

        int clearedAssignments = 0;
        Map<ItemIdentity, VisualHomeAssignment> assignments =
                new LinkedHashMap<>(workflow.visualHomeMap().assignments());
        for (ItemIdentity identity : assignments.keySet()) {
            if (workflow.clearHome(identity)) {
                clearedAssignments++;
            }
        }

        int deletedIslands = 0;
        List<String> islandIds = new ArrayList<>();
        for (VisualAtlasIsland island : workflow.visualHomeMap().playerIslands()) {
            islandIds.add(island.id());
        }
        for (String islandId : islandIds) {
            if (workflow.deleteIsland(islandId)) {
                deletedIslands++;
            }
        }

        InventoryClearResult inventoryResult = clearPlayerInventory(player);

        int finalDeletedIslands = deletedIslands;
        int finalClearedAssignments = clearedAssignments;
        int finalChestsDeleted = chestResult.chestsDeleted;
        int finalBlocksBroken = chestResult.blocksBroken;
        int finalUnloadedAnchors = chestResult.unloadedAnchors;
        int finalInventoryCleared = inventoryResult.slotsCleared;
        int finalBackpacksPreserved = inventoryResult.backpacksPreserved;
        int finalBackpackContents = inventoryResult.backpackContentsCleared;
        context.getSource().sendSuccess(() -> Component.literal(String.format(
                "[SLOT] test clear: islands=%d assignments=%d chests=%d (blocks=%d unloaded=%d) inventory=%d backpacks_kept=%d backpack_contents=%d",
                finalDeletedIslands, finalClearedAssignments,
                finalChestsDeleted, finalBlocksBroken, finalUnloadedAnchors,
                finalInventoryCleared, finalBackpacksPreserved, finalBackpackContents
        )), false);
        SlotCommon.LOGGER.info(
                "[SLOT] /slot test clear -> islands={} assignments={} chests={} blocks_broken={} unloaded_anchors={} inventory_cleared={} backpacks_preserved={} backpack_contents_cleared={}",
                finalDeletedIslands, finalClearedAssignments,
                finalChestsDeleted, finalBlocksBroken, finalUnloadedAnchors,
                finalInventoryCleared, finalBackpacksPreserved, finalBackpackContents
        );
        return finalDeletedIslands;
    }

    private static ChestClearResult clearChests(
            MinecraftServer server,
            ChestClaimWorkflowDomainService chestClaimWorkflow
    ) {
        if (server == null) {
            return new ChestClearResult(0, 0, 0);
        }
        List<ClaimedChest> chests = new ArrayList<>(chestClaimWorkflow.claimedChestMap().chests());
        int chestsDeleted = 0;
        int blocksBroken = 0;
        int unloadedAnchors = 0;
        for (ClaimedChest chest : chests) {
            for (ChestAnchor anchor : chest.anchors()) {
                ServerLevel level = resolveLevel(server, anchor);
                if (level == null) {
                    unloadedAnchors++;
                    continue;
                }
                BlockPos pos = new BlockPos(anchor.x(), anchor.y(), anchor.z());
                if (!level.isLoaded(pos)) {
                    unloadedAnchors++;
                    continue;
                }
                emptyContainerAt(level, pos);
                if (level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)) {
                    blocksBroken++;
                }
            }
            UUID storageId = chest.storageId();
            if (storageId != null && chestClaimWorkflow.deleteChest(storageId)) {
                chestsDeleted++;
            }
        }
        return new ChestClearResult(chestsDeleted, blocksBroken, unloadedAnchors);
    }

    private static void emptyContainerAt(ServerLevel level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chest)) {
            return;
        }
        int size = chest.getContainerSize();
        for (int slot = 0; slot < size; slot++) {
            chest.setItem(slot, ItemStack.EMPTY);
        }
        chest.setChanged();
    }

    private static ServerLevel resolveLevel(MinecraftServer server, ChestAnchor anchor) {
        if (anchor == null || anchor.dimensionId() == null || anchor.dimensionId().isBlank()) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
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
        inventory.setChanged();
        return new InventoryClearResult(
                main.cleared + armor.cleared + offhand.cleared,
                main.preserved + armor.preserved + offhand.preserved,
                main.backpackContents + armor.backpackContents + offhand.backpackContents
        );
    }

    private static InventoryListResult clearPreservingBackpacks(NonNullList<ItemStack> slots) {
        int cleared = 0;
        int preserved = 0;
        int backpackContents = 0;
        for (int index = 0; index < slots.size(); index++) {
            ItemStack stack = slots.get(index);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (isBackpackStack(stack)) {
                preserved++;
                backpackContents += clearBackpackContents(stack);
                continue;
            }
            slots.set(index, ItemStack.EMPTY);
            cleared++;
        }
        return new InventoryListResult(cleared, preserved, backpackContents);
    }

    private static int clearBackpackContents(ItemStack backpack) {
        if (backpack == null || backpack.isEmpty()) {
            return 0;
        }
        try {
            IItemHandler handler = backpack.getCapability(Capabilities.ItemHandler.ITEM);
            if (handler == null) {
                return 0;
            }
            int count = 0;
            int size = handler.getSlots();
            if (handler instanceof IItemHandlerModifiable modifiable) {
                for (int slot = 0; slot < size; slot++) {
                    if (!handler.getStackInSlot(slot).isEmpty()) {
                        modifiable.setStackInSlot(slot, ItemStack.EMPTY);
                        count++;
                    }
                }
            } else {
                for (int slot = 0; slot < size; slot++) {
                    ItemStack extracted = handler.extractItem(slot, Integer.MAX_VALUE, false);
                    if (!extracted.isEmpty()) {
                        count++;
                    }
                }
            }
            return count;
        } catch (RuntimeException | LinkageError ignored) {
            return 0;
        }
    }

    private static boolean isBackpackStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
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

    private record ChestClearResult(int chestsDeleted, int blocksBroken, int unloadedAnchors) {
    }

    private record InventoryClearResult(int slotsCleared, int backpacksPreserved, int backpackContentsCleared) {
    }

    private record InventoryListResult(int cleared, int preserved, int backpackContents) {
    }

    private static List<ItemStack> nonEmptyItemStackPool() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == null) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            if (stack.isEmpty()) {
                continue;
            }
            stacks.add(stack);
        }
        return stacks;
    }

    private record ChestPlacementResult(int placed, int claimed, int linked, int claimFailed, int linkFailed) {
    }

    private record InventoryGiveResult(int mainInserted, int backpackInserted, int dropped) {
    }
}
