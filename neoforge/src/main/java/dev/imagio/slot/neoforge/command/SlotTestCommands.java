package dev.imagio.slot.neoforge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.debug.SyntheticHomedAtlasGenerator;
import dev.imagio.slot.debug.SyntheticHomedAtlasPlan;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasWorkflowDomainService;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SlotTestCommands {
    private static final long DEFAULT_HOMED_SEED = 1L;
    private static final int DEFAULT_HOMED_ISLANDS = 12;
    private static boolean registered;

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
                        .then(Commands.literal("populate-atlas")
                                .then(Commands.literal("triage")
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 100_000))
                                                .executes(SlotTestCommands::runPopulateTriage)))
                                .then(Commands.literal("homed")
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 100_000))
                                                .executes(context -> runPopulateHomed(
                                                        context,
                                                        DEFAULT_HOMED_ISLANDS,
                                                        DEFAULT_HOMED_SEED))
                                                .then(Commands.argument("islands", IntegerArgumentType.integer(1, 256))
                                                        .executes(context -> runPopulateHomed(
                                                                context,
                                                                IntegerArgumentType.getInteger(context, "islands"),
                                                                DEFAULT_HOMED_SEED))
                                                        .then(Commands.argument("seed", LongArgumentType.longArg())
                                                                .executes(context -> runPopulateHomed(
                                                                        context,
                                                                        IntegerArgumentType.getInteger(context, "islands"),
                                                                        LongArgumentType.getLong(context, "seed"))))))))
                        .then(Commands.literal("clear")
                                .executes(SlotTestCommands::runClear)));
        dispatcher.register(root);
    }

    private static int runPopulateTriage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int requested = IntegerArgumentType.getInteger(context, "count");

        List<ItemStack> pool = nonEmptyItemStackPool();
        if (pool.isEmpty()) {
            context.getSource().sendFailure(Component.literal("[SLOT] populate-atlas triage: no items available from registry"));
            return 0;
        }

        int limit = Math.min(requested, pool.size());
        List<ItemStack> sampled = new ArrayList<>(limit);
        // Simple deterministic-ish sample: shuffle by request index hash so repeats vary.
        long seed = player.getUUID().getLeastSignificantBits() ^ System.nanoTime();
        java.util.Collections.shuffle(pool, new java.util.Random(seed));
        for (int index = 0; index < limit; index++) {
            sampled.add(pool.get(index).copy());
        }

        int addedToInventory = 0;
        int droppedToWorld = 0;
        for (ItemStack stack : sampled) {
            boolean added = player.getInventory().add(stack);
            if (added && stack.isEmpty()) {
                addedToInventory++;
            } else {
                droppedToWorld++;
                player.drop(stack, false);
            }
        }

        int finalAdded = addedToInventory;
        int finalDropped = droppedToWorld;
        context.getSource().sendSuccess(() -> Component.literal(String.format(
                "[SLOT] populate-atlas triage: %d sampled (%d to inventory, %d dropped)",
                sampled.size(), finalAdded, finalDropped
        )), false);
        SlotCommon.LOGGER.info("[SLOT] /slot test populate-atlas triage {} -> sampled={} inventory={} dropped={}",
                requested, sampled.size(), finalAdded, finalDropped);
        return sampled.size();
    }

    private static int runPopulateHomed(
            CommandContext<CommandSourceStack> context,
            int islandCount,
            long seed
    ) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        int requested = IntegerArgumentType.getInteger(context, "count");

        List<ItemIdentity> pool = nonEmptyIdentityPool();
        if (pool.isEmpty()) {
            context.getSource().sendFailure(Component.literal("[SLOT] populate-atlas homed: no items available from registry"));
            return 0;
        }

        SyntheticHomedAtlasPlan plan = SyntheticHomedAtlasGenerator.generate(
                pool,
                requested,
                islandCount,
                seed,
                SyntheticHomedAtlasGenerator.Config.defaults()
        );

        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        VisualAtlasWorkflowDomainService workflow = runtime.visualAtlasWorkflow();

        int islandsCreated = 0;
        int islandsSkipped = 0;
        for (VisualAtlasIsland island : plan.islands()) {
            VisualAtlasIsland created = workflow.createIslandWithId(
                    island.id(),
                    island.label(),
                    island.x(),
                    island.y(),
                    island.width(),
                    island.height(),
                    island.color(),
                    island.iconIdentity()
            );
            if (created != null) {
                islandsCreated++;
            } else {
                islandsSkipped++;
            }
        }

        int assignmentsApplied = 0;
        for (VisualHomeAssignment assignment : plan.assignments().values()) {
            VisualHomeAssignment applied = workflow.assignHome(
                    assignment.identity(),
                    assignment.islandId(),
                    assignment.localX(),
                    assignment.localY()
            );
            if (applied != null) {
                assignmentsApplied++;
            }
        }

        int finalCreated = islandsCreated;
        int finalSkipped = islandsSkipped;
        int finalAssigned = assignmentsApplied;
        context.getSource().sendSuccess(() -> Component.literal(String.format(
                "[SLOT] populate-atlas homed: islands=%d (created=%d, existing=%d) assignments=%d seed=%d",
                plan.islands().size(), finalCreated, finalSkipped, finalAssigned, seed
        )), false);
        SlotCommon.LOGGER.info(
                "[SLOT] /slot test populate-atlas homed count={} islands={} seed={} -> created={} existing={} assignments={}",
                requested, islandCount, seed, finalCreated, finalSkipped, finalAssigned
        );
        return finalAssigned;
    }

    private static int runClear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        VisualAtlasWorkflowDomainService workflow = runtime.visualAtlasWorkflow();
        VisualHomeMap map = workflow.visualHomeMap();

        List<String> syntheticIslandIds = new ArrayList<>();
        for (VisualAtlasIsland island : map.playerIslands()) {
            if (island.id().startsWith(SyntheticHomedAtlasGenerator.SYNTHETIC_ISLAND_ID_PREFIX)) {
                syntheticIslandIds.add(island.id());
            }
        }

        int clearedAssignments = 0;
        Map<ItemIdentity, VisualHomeAssignment> assignments = new LinkedHashMap<>(map.assignments());
        for (Map.Entry<ItemIdentity, VisualHomeAssignment> entry : assignments.entrySet()) {
            VisualHomeAssignment assignment = entry.getValue();
            if (assignment != null && syntheticIslandIds.contains(assignment.islandId())) {
                if (workflow.clearHome(entry.getKey())) {
                    clearedAssignments++;
                }
            }
        }

        int deletedIslands = 0;
        for (String islandId : syntheticIslandIds) {
            if (workflow.deleteIsland(islandId)) {
                deletedIslands++;
            }
        }

        int finalDeletedIslands = deletedIslands;
        int finalClearedAssignments = clearedAssignments;
        context.getSource().sendSuccess(() -> Component.literal(String.format(
                "[SLOT] test clear: removed %d synthetic islands, %d assignments (use /clear for inventory)",
                finalDeletedIslands, finalClearedAssignments
        )), false);
        SlotCommon.LOGGER.info(
                "[SLOT] /slot test clear -> islands={} assignments={}",
                finalDeletedIslands, finalClearedAssignments
        );
        return finalDeletedIslands;
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

    private static List<ItemIdentity> nonEmptyIdentityPool() {
        ArrayList<ItemIdentity> identities = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == null) {
                continue;
            }
            ItemStack stack = new ItemStack(item);
            if (stack.isEmpty()) {
                continue;
            }
            identities.add(ItemIdentityMatcher.create(stack));
        }
        return identities;
    }
}
