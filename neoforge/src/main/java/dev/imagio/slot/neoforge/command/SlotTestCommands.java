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
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.triage.IslandTemplateMatch;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.neoforge.storage.ChestStorageIds;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.KitWorkflowDomainService;
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

    /**
     * Number of top-tier Sophisticated Backpacks the populate command
     * guarantees before giving items. A single base-tier backpack
     * (which the natural item-pool draw might place) lacks the
     * capacity for any meaningful profile, so item stacks spilled into
     * the main inventory and then to the floor — making populate
     * unusable for testing without a manual top-up.
     */
    private static final int TOP_TIER_BACKPACK_TARGET = 3;

    /**
     * Tiered fallback when picking which Sophisticated Backpack item
     * to grant. Netherite is the largest container the mod ships;
     * lower tiers are the graceful-degradation order so a modpack
     * without the netherite tier still gets the next-best capacity.
     * Empty when Sophisticated Backpacks isn't installed.
     */
    private static final List<String> BACKPACK_TIER_FALLBACK = List.of(
            "sophisticatedbackpacks:netherite_backpack",
            "sophisticatedbackpacks:diamond_backpack",
            "sophisticatedbackpacks:gold_backpack",
            "sophisticatedbackpacks:iron_backpack",
            "sophisticatedbackpacks:backpack"
    );

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
                        .then(Commands.literal("kits")
                                .executes(SlotTestCommands::runKits))
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
        ChestClaimWorkflowDomainService chestWorkflow = runtime.chestClaimWorkflow();

        int islandsCreated = applyIslands(workflow, plan);
        int assignmentsApplied = applyAssignments(workflow, plan);
        int backpacksGranted = ensureTopTierBackpacks(player);
        InventoryGiveResult giveResult = giveStacksToPlayer(player, plan);
        ChestPlacementResult chestResult = placeChests(player, chestWorkflow, plan, random);

        int finalIslands = islandsCreated;
        int finalAssignments = assignmentsApplied;
        int finalMain = giveResult.mainInserted;
        int finalBackpack = giveResult.backpackInserted;
        int finalDropped = giveResult.dropped;
        int finalPlaced = chestResult.placed;
        int finalClaimed = chestResult.claimed;
        int finalAffinity = chestResult.affinityRecorded;
        int finalClaimFailed = chestResult.claimFailed;
        int finalBackpacksGranted = backpacksGranted;

        context.getSource().sendSuccess(() -> Component.literal(String.format(
                "[SLOT] populate %s: islands=%d assignments=%d stacks main=%d backpack=%d dropped=%d chests placed=%d claimed=%d affinity=%d claim_failed=%d backpacks_granted=%d",
                profile.id(), finalIslands, finalAssignments,
                finalMain, finalBackpack, finalDropped,
                finalPlaced, finalClaimed, finalAffinity, finalClaimFailed,
                finalBackpacksGranted
        )), false);
        SlotCommon.LOGGER.info(
                "[SLOT] /slot test populate profile={} -> islands={} assignments={} stacks_main={} stacks_backpack={} stacks_dropped={} chests placed={} claimed={} affinity={} claim_failed={} backpacks_granted={}",
                profile.id(), finalIslands, finalAssignments,
                finalMain, finalBackpack, finalDropped,
                finalPlaced, finalClaimed, finalAffinity, finalClaimFailed,
                finalBackpacksGranted
        );
        return finalAssignments;
    }

    /**
     * Top up the player's inventory to {@link #TOP_TIER_BACKPACK_TARGET}
     * netherite-tier Sophisticated Backpacks (or the highest tier that
     * the active modpack actually ships). Returns the number granted —
     * zero when the inventory already has enough or when Sophisticated
     * Backpacks isn't installed at all.
     *
     * <p>Only the chosen target tier counts toward the cap, so a base
     * backpack the player picked up elsewhere doesn't suppress the
     * netherite top-up. Lower-tier backpacks are left untouched.
     */
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

    /**
     * Hand-authored plausible kit templates — the loadouts a player
     * would build by hand on day 50 of a modded world. Each entry
     * names the hotbar slot-by-slot (nulls allowed for empty slots),
     * the bring list (extra inventory items the kit guarantees), and
     * the offhand. Modded ids degrade gracefully: any entry whose
     * item is missing from the registry is silently dropped, so a
     * vanilla-only world still gets the four vanilla kits and skips
     * the Create one.
     */
    private static final List<KitTemplate> KIT_TEMPLATES = List.of(
            new KitTemplate(
                    "Explore",
                    List.of(
                            "minecraft:netherite_pickaxe",
                            "minecraft:netherite_axe",
                            "minecraft:netherite_shovel",
                            "minecraft:netherite_sword",
                            "minecraft:bow",
                            "minecraft:torch",
                            "minecraft:cooked_beef",
                            "minecraft:ender_pearl",
                            "minecraft:water_bucket"
                    ),
                    List.of(
                            "minecraft:arrow",
                            "minecraft:flint_and_steel",
                            "minecraft:cobblestone",
                            "minecraft:golden_apple"
                    ),
                    "minecraft:shield"
            ),
            new KitTemplate(
                    "Base Building",
                    List.of(
                            "minecraft:netherite_pickaxe",
                            "minecraft:netherite_axe",
                            "minecraft:cobblestone",
                            "minecraft:oak_planks",
                            "minecraft:oak_stairs",
                            "minecraft:oak_slab",
                            "minecraft:oak_door",
                            "minecraft:glass_pane",
                            "minecraft:torch"
                    ),
                    List.of(
                            "minecraft:dirt",
                            "minecraft:stone",
                            "minecraft:glass",
                            "minecraft:oak_log",
                            "minecraft:white_wool"
                    ),
                    "minecraft:shield"
            ),
            new KitTemplate(
                    "Boss Fight",
                    List.of(
                            "minecraft:netherite_sword",
                            "minecraft:bow",
                            "minecraft:totem_of_undying",
                            "minecraft:ender_pearl",
                            "minecraft:enchanted_golden_apple",
                            "minecraft:cooked_beef",
                            "minecraft:splash_potion",
                            "minecraft:potion",
                            "minecraft:milk_bucket"
                    ),
                    List.of(
                            "minecraft:arrow",
                            "minecraft:tipped_arrow",
                            "minecraft:golden_apple",
                            "minecraft:water_bucket"
                    ),
                    "minecraft:shield"
            ),
            new KitTemplate(
                    "Create Machines",
                    List.of(
                            "create:wrench",
                            "create:cogwheel",
                            "create:large_cogwheel",
                            "create:shaft",
                            "create:gearbox",
                            "create:mechanical_press",
                            "create:water_wheel",
                            "create:andesite_alloy",
                            "create:brass_ingot"
                    ),
                    List.of(
                            "minecraft:copper_ingot",
                            "create:zinc_ingot",
                            "create:brass_ingot",
                            "minecraft:redstone"
                    ),
                    null
            ),
            new KitTemplate(
                    "Farming",
                    List.of(
                            "minecraft:netherite_hoe",
                            "minecraft:water_bucket",
                            "minecraft:wheat_seeds",
                            "minecraft:carrot",
                            "minecraft:potato",
                            "minecraft:beetroot_seeds",
                            "minecraft:bone_meal",
                            "minecraft:oak_fence",
                            "minecraft:torch"
                    ),
                    List.of(
                            "minecraft:dirt",
                            "minecraft:hay_block",
                            "minecraft:bread",
                            "minecraft:composter"
                    ),
                    "minecraft:shield"
            )
    );

    private static int runKits(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        KitWorkflowDomainService kitWorkflow = runtime.kitWorkflow();

        int created = 0;
        int skipped = 0;
        ArrayList<String> createdNames = new ArrayList<>();
        ArrayList<String> skippedNames = new ArrayList<>();
        java.util.LinkedHashSet<ItemIdentity> kitItems = new java.util.LinkedHashSet<>();
        for (KitTemplate template : KIT_TEMPLATES) {
            ResolvedKit resolved = template.resolve();
            if (resolved == null) {
                skipped++;
                skippedNames.add(template.name());
                continue;
            }
            try {
                dev.imagio.slot.workflow.domain.KitDefinition createdKit = kitWorkflow.create(
                        resolved.name(),
                        resolved.firstPage(),
                        resolved.offhand(),
                        DomainEventMetadata.origin("slot.test.kits")
                );
                // Bring list is now a kit-scoped desired count map. Seed
                // each "bring" identity with count=1 so the new kit reads
                // identically to the legacy bring-list shape.
                if (createdKit != null) {
                    for (ItemIdentity bringIdentity : resolved.bring()) {
                        if (bringIdentity == null) continue;
                        runtime.desiredCountWorkflow().setForKit(
                                createdKit.id(),
                                bringIdentity,
                                1,
                                DomainEventMetadata.origin("slot.test.kits.bring"));
                    }
                }
                created++;
                createdNames.add(resolved.name());
                resolved.collectIdentities(kitItems);
            } catch (RuntimeException e) {
                skipped++;
                skippedNames.add(template.name() + "(error)");
                SlotCommon.LOGGER.warn(
                        "[SLOT] /slot test kits: failed to create '{}': {}",
                        template.name(), e.getMessage());
            }
        }

        // Stock the kit items into nearby chests so the "Gather" flow on
        // each kit has somewhere to fetch from. Without this the kits
        // generator was useless for testing fetch — every kit slot read
        // as missing-and-unfetchable. We don't grant anything to the
        // player so every slot starts as needs-fetch.
        List<ItemIdentity> kitItemList = List.copyOf(kitItems);
        KitChestStockResult stocked = stockKitItemsIntoNearbyChests(
                player, runtime.chestClaimWorkflow(), kitItemList);

        // Assign each kit item to its template-default home island. Without
        // this the atlas never "sees" identities that only exist in inactive
        // kits + fresh chests — proximate-chest ghosts skip rendering when
        // the identity has no home assignment ("ghost-only-unhomed → skip"
        // in SlotWorkspaceViewModel.project), so kit ghosts and search
        // indicators come up empty. Materializing template islands and
        // homing each kit identity gives the projection something to anchor
        // the ghost cards to.
        int homedAssigned = autoHomeKitItems(runtime.visualAtlasWorkflow(), kitItemList);

        int finalCreated = created;
        int finalSkipped = skipped;
        int finalChestsPlaced = stocked.chestsPlaced;
        int finalItemsStocked = stocked.itemsStocked;
        int finalItemsLeftover = stocked.itemsLeftover;
        int finalHomedAssigned = homedAssigned;
        String createdStr = String.join(", ", createdNames);
        String skippedStr = skippedNames.isEmpty() ? "" : " (skipped: " + String.join(", ", skippedNames) + ")";
        context.getSource().sendSuccess(() -> Component.literal(String.format(
                "[SLOT] kits: created=%d skipped=%d chests=%d stocked=%d leftover=%d homed=%d %s%s",
                finalCreated, finalSkipped,
                finalChestsPlaced, finalItemsStocked, finalItemsLeftover, finalHomedAssigned,
                createdStr, skippedStr
        )), false);
        SlotCommon.LOGGER.info(
                "[SLOT] /slot test kits -> created={} skipped={} chests={} stocked={} leftover={} homed={} names={}",
                finalCreated, finalSkipped,
                finalChestsPlaced, finalItemsStocked, finalItemsLeftover, finalHomedAssigned,
                createdNames);
        return created;
    }

    /**
     * Classify each kit identity via the same template logic populate uses
     * and assign it to the matching template-default island, materializing
     * the island if it doesn't exist yet. The atlas projection skips
     * unhomed ghost-only items, so kit identities living only in chests
     * stay invisible until they have a home — even with the kit-needed
     * synthesis, only the *active* kit's items survive that path. Homing
     * everything makes inactive kits, search, and gather all work.
     */
    private static int autoHomeKitItems(
            VisualAtlasWorkflowDomainService visualWorkflow,
            List<ItemIdentity> kitItems
    ) {
        if (kitItems.isEmpty()) {
            return 0;
        }
        FacetIndexTemplateClassifier classifier =
                new FacetIndexTemplateClassifier(IslandSignalExtractor::extract);
        int assigned = 0;
        for (ItemIdentity identity : kitItems) {
            ItemStack stack = createDefaultStack(identity);
            if (stack.isEmpty()) {
                continue;
            }
            IslandSignalDescriptor descriptor = classifier.describe(stack);
            if (descriptor == null) {
                continue;
            }
            IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                    descriptor, null);
            if (match == null) {
                continue;
            }
            String islandId = resolveOrMaterializeKitIsland(
                    visualWorkflow, match.parentTemplate(), identity);
            if (islandId == null) {
                continue;
            }
            VisualHomeAssignment result = visualWorkflow.assignHome(identity, islandId, 0);
            if (result != null) {
                assigned++;
            }
        }
        return assigned;
    }

    private static String resolveOrMaterializeKitIsland(
            VisualAtlasWorkflowDomainService visualWorkflow,
            IslandSuggestionTemplate template,
            ItemIdentity seedIdentity
    ) {
        VisualAtlasIsland existing = visualWorkflow.visualHomeMap().playerIslands().stream()
                .filter(island -> island != null
                        && template.defaultLabel().equalsIgnoreCase(island.label()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            return existing.id();
        }
        SlotWorkspaceAtlasLayout.PlayerIslandDraft draft =
                SlotWorkspaceAtlasLayout.createNextPlayerIslandDraft(
                        template.defaultLabel(),
                        seedIdentity,
                        visualWorkflow.visualHomeMap()
                );
        VisualAtlasIsland created = visualWorkflow.createIsland(
                draft.label(),
                draft.x(),
                draft.y(),
                template.defaultColor(),
                seedIdentity,
                DomainEventMetadata.origin("slot.test.kits.island_create")
        );
        return created == null ? null : created.id();
    }

    /**
     * Place a small cluster of claimed chests adjacent to the player and
     * fill them with one of every kit-needed item (1 stack each). The
     * chests are claimed via the same workflow path populate uses, so
     * proximity detection and Gather routing pick them up immediately.
     * Returns telemetry for the command status line.
     */
    private static KitChestStockResult stockKitItemsIntoNearbyChests(
            ServerPlayer player,
            ChestClaimWorkflowDomainService chestWorkflow,
            List<ItemIdentity> kitItems
    ) {
        if (kitItems.isEmpty()) {
            return new KitChestStockResult(0, 0, 0);
        }
        ServerLevel level = player.serverLevel();
        int centerX = (int) Math.floor(player.getX());
        int centerY = (int) Math.floor(player.getY());
        int centerZ = (int) Math.floor(player.getZ());
        String dimensionId = level.dimension().location().toString();
        long tick = level.getGameTime();

        // Place the chests close enough to be in proximity range but not
        // on top of the player; keep them in a row so they're easy to spot.
        BlockPos[] positions = {
                new BlockPos(centerX + 2, centerY, centerZ),
                new BlockPos(centerX + 2, centerY, centerZ + 1),
                new BlockPos(centerX + 2, centerY, centerZ + 2)
        };

        int chestsPlaced = 0;
        int itemsStocked = 0;
        int itemIndex = 0;

        for (BlockPos pos : positions) {
            if (itemIndex >= kitItems.size()) {
                break;
            }
            BlockState previous = level.getBlockState(pos);
            if (!previous.isAir() && !previous.canBeReplaced()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            if (!level.setBlock(pos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL)) {
                SlotCommon.LOGGER.warn("[SLOT] kits: setBlock failed for chest at {}", pos);
                continue;
            }
            chestsPlaced++;

            ChestAnchor anchor = new ChestAnchor(dimensionId, pos.getX(), pos.getY(), pos.getZ());
            ClaimedChest claimed = chestWorkflow.autoClaimByAnchor(
                    anchor, pos.getX() * 100, pos.getZ() * 100, null);
            if (claimed == null) {
                SlotCommon.LOGGER.warn("[SLOT] kits: claim returned null for chest at {}", pos);
                continue;
            }
            ChestStorageIds.write(level, pos, claimed.storageId());

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ChestBlockEntity chest)) {
                continue;
            }
            int containerSize = chest.getContainerSize();
            int slot = 0;
            while (slot < containerSize && itemIndex < kitItems.size()) {
                ItemIdentity identity = kitItems.get(itemIndex++);
                ItemStack stack = createDefaultStack(identity);
                if (stack.isEmpty()) {
                    continue;
                }
                stack.setCount(Math.min(stack.getMaxStackSize(), 4));
                chest.setItem(slot, stack);
                chestWorkflow.recordDeposit(claimed.storageId(), identity, stack.getCount(), tick);
                slot++;
                itemsStocked++;
            }
            chest.setChanged();
        }

        int leftover = Math.max(0, kitItems.size() - itemIndex);
        return new KitChestStockResult(chestsPlaced, itemsStocked, leftover);
    }

    private static ItemStack createDefaultStack(ItemIdentity identity) {
        if (identity == null || identity.itemId() == null) {
            return ItemStack.EMPTY;
        }
        ResourceLocation key = ResourceLocation.tryParse(identity.itemId());
        if (key == null || !BuiltInRegistries.ITEM.containsKey(key)) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(key);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item, 1);
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    private record KitChestStockResult(int chestsPlaced, int itemsStocked, int itemsLeftover) {
    }

    private static ItemIdentity tryResolveItemIdentity(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        ResourceLocation key = ResourceLocation.tryParse(itemId);
        if (key == null || !BuiltInRegistries.ITEM.containsKey(key)) {
            return null;
        }
        return ItemIdentity.of(itemId);
    }

    private record KitTemplate(
            String name,
            List<String> hotbar,
            List<String> bring,
            String offhand
    ) {
        /**
         * Resolve the template against the active item registry.
         * Returns {@code null} when no hotbar slot can be filled
         * (e.g., a Create-only kit on a vanilla server). Empty
         * hotbar slots and missing bring entries are silently
         * dropped so a partial mod overlap still yields a usable
         * kit.
         */
        ResolvedKit resolve() {
            ArrayList<ItemIdentity> hotbarIds = new ArrayList<>(KitPage.HOTBAR_SLOT_COUNT);
            int filled = 0;
            for (int i = 0; i < KitPage.HOTBAR_SLOT_COUNT; i++) {
                String id = i < hotbar.size() ? hotbar.get(i) : null;
                ItemIdentity resolved = tryResolveItemIdentity(id);
                hotbarIds.add(resolved);
                if (resolved != null) {
                    filled++;
                }
            }
            if (filled == 0) {
                return null;
            }
            ArrayList<ItemIdentity> bringIds = new ArrayList<>();
            for (String id : bring) {
                ItemIdentity resolved = tryResolveItemIdentity(id);
                if (resolved != null) {
                    bringIds.add(resolved);
                }
            }
            ItemIdentity offhandId = tryResolveItemIdentity(offhand);
            return new ResolvedKit(name, new KitPage(hotbarIds), bringIds, offhandId);
        }
    }

    private record ResolvedKit(
            String name,
            KitPage firstPage,
            List<ItemIdentity> bring,
            ItemIdentity offhand
    ) {
        void collectIdentities(java.util.Collection<ItemIdentity> sink) {
            for (ItemIdentity slot : firstPage.hotbarIdentities()) {
                if (slot != null) {
                    sink.add(slot);
                }
            }
            for (ItemIdentity id : bring) {
                if (id != null) {
                    sink.add(id);
                }
            }
            if (offhand != null) {
                sink.add(offhand);
            }
        }
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

        // Backpack carriers first so they're seated in the inventory
        // before non-backpack items try to route into them. Stable
        // partition keeps the relative order of non-carrier stacks.
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

        // Route through SLOT's normal carried-source pipeline (backpack-first
        // via Sophisticated Backpacks' own enumeration, then vanilla add).
        // The hand-rolled insertIntoBackpacks we used to call here walked
        // inventory.items and queried Capabilities.ItemHandler.ITEM directly
        // — that path missed curios-slot backpacks and (more importantly)
        // returned null for freshly-spawned backpacks whose contents UUID
        // hadn't been initialised yet, so items spilled to main and then
        // to the floor once main filled.
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
            int initial = stack.getCount();
            ItemStack remaining;
            int vanillaDelta;

            if (isBackpackStack(stack) || carried == null) {
                // Backpacks themselves get added directly so they become
                // carriers, not contents (most backpack mods refuse to
                // nest backpacks inside backpacks anyway).
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
            int placed = initial - leftover;
            mainInserted += vanillaDelta;
            backpackInserted += Math.max(0, placed - vanillaDelta);

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

    private static ChestPlacementResult placeChests(
            ServerPlayer player,
            ChestClaimWorkflowDomainService chestWorkflow,
            RealisticAtlasPlan plan,
            Random random
    ) {
        List<ChestSpec> chests = plan.chests();
        if (chests.isEmpty()) {
            return new ChestPlacementResult(0, 0, 0, 0);
        }

        ServerLevel level = player.serverLevel();
        int centerX = (int) Math.floor(player.getX());
        int centerY = (int) Math.floor(player.getY());
        int centerZ = (int) Math.floor(player.getZ());
        String dimensionId = level.dimension().location().toString();
        long tick = level.getGameTime();

        int placed = 0;
        int claimed = 0;
        int affinityRecorded = 0;
        int claimFailed = 0;

        for (ChestSpec spec : chests) {
            BlockPos pos = new BlockPos(
                    centerX + spec.deltaX(),
                    centerY,
                    centerZ + spec.deltaZ()
            );
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
            ChestAnchor anchor = new ChestAnchor(dimensionId, pos.getX(), pos.getY(), pos.getZ());
            ClaimedChest claimedChest = chestWorkflow.autoClaimByAnchor(
                    anchor, pos.getX() * 100, pos.getZ() * 100, null);
            if (claimedChest == null) {
                claimFailed++;
                SlotCommon.LOGGER.warn(
                        "[SLOT] populate: claim returned null for chest {} at {}",
                        spec.index(), pos
                );
                continue;
            }
            // Stamp the BE with the canonical slot:storage_id so
            // ChestPersistenceReconciliation doesn't prune the claim on
            // next login (it walks claimed chests, reads each anchor's
            // BE attachment, and deletes claims whose anchors are
            // attachment-less). The pre-refactor `ChestClaimServerService.claim`
            // path wrote this implicitly; the autoClaimByAnchor path
            // doesn't, so populate has to do it explicitly.
            ChestStorageIds.write(level, pos, claimedChest.storageId());
            claimed++;
            // Seed affinity from the chest's contents so deposit routing
            // works immediately without needing observed deposits.
            for (ChestContentEntry entry : spec.contents()) {
                ItemStack stack = entry.stack();
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                ItemIdentity identity = ItemIdentityMatcher.create(stack);
                chestWorkflow.recordDeposit(claimedChest.storageId(), identity, stack.getCount(), tick);
                affinityRecorded++;
            }
        }

        return new ChestPlacementResult(placed, claimed, affinityRecorded, claimFailed);
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

    private record ChestPlacementResult(int placed, int claimed, int affinityRecorded, int claimFailed) {
    }

    private record InventoryGiveResult(int mainInserted, int backpackInserted, int dropped) {
    }
}
