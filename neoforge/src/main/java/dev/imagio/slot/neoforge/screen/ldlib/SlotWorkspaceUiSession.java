package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.SlotDiagnostics;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryActionExecutor;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.SophisticatedBackpackInventoryIntegrationProvider;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.triage.LearnedIslandRuleStore;
import dev.imagio.slot.inventory.workspace.DepositPlan;
import dev.imagio.slot.inventory.workspace.DepositPlanner;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceCommandService;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceTransferRequestFactory;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferExecution;
import dev.imagio.slot.inventory.workspace.WorkspaceTransferFeedback;
import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.neoforge.storage.ChestContentsReader;
import dev.imagio.slot.neoforge.storage.ChestDepositObserver;
import dev.imagio.slot.neoforge.storage.ChestProximityResolver;
import dev.imagio.slot.neoforge.storage.ChestStorageAnchors;
import dev.imagio.slot.neoforge.storage.ChestStorageIds;
import dev.imagio.slot.neoforge.storage.DepositExecutor;
import dev.imagio.slot.neoforge.storage.DepositReverser;
import dev.imagio.slot.neoforge.storage.HotbarSlotReverser;
import dev.imagio.slot.neoforge.storage.LootChestProximityResolver;
import dev.imagio.slot.neoforge.storage.TakeAllExecutor;
import dev.imagio.slot.neoforge.triage.IslandSignalExtractor;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClusterMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.ProtectionPolicy;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.DesiredCountWorkflowDomainService;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

final class SlotWorkspaceUiSession {
    static final int TARGET_MAIN_SOURCE = 1;
    static final int TARGET_MAIN_SLOT = 2;
    static final int TARGET_HOTBAR_SLOT = 3;

    private final Player player;
    private final LearnedIslandRuleStore learnedRules = new LearnedIslandRuleStore();
    private SlotWorkspaceViewModel viewModel = SlotWorkspaceViewModel.empty();
    private CompoundTag lastContentTag = new CompoundTag();
    private CompoundTag lastViewTag;
    private long nextRevision = 1L;
    private String status = "ready";
    private String diagnostics = "";
    /**
     * Identities the auto-home pipeline has already attempted in this
     * session, so we don't replay the same suggest-and-assign work on
     * every refresh. Each {@code assignHome} hits the persistence
     * service synchronously; without this gate the first refresh after
     * opening a screen with a full inventory would hang the server tick
     * long enough that the player's close-screen packet never gets
     * processed. The set lives for the lifetime of the session — once
     * persistence is in place, future sessions inherit the assignments
     * via the workflow snapshot.
     */
    private final java.util.Set<ItemIdentity> autoHomeAttempted = new java.util.HashSet<>();
    /**
     * Mirror of the client-side search query, pushed via
     * {@link #setSearchQuery}. Drives server-side gating of the remote-only
     * ghost synthesis: those ghosts pollute the atlas full-time if always on,
     * but disappearing them under no search blocks search-as-find. Storing
     * the active query lets the projection conditionally synthesize matching
     * ghosts only when the player is searching.
     */
    private String searchQuery = "";

    /**
     * Origin stamp for the current cursor stack. Set whenever a SLOT-
     * initiated pickup writes to {@code menu.setCarried(...)}; cleared on
     * any drop / cancel that empties the cursor. Phase B's right-click
     * cancel reads this to route the cursor stack back to its source
     * (so eager-from-chest pickups reverse cleanly into the chest rather
     * than dump into player inventory). When {@code menu.getCarried()} is
     * non-empty but origin is null, vanilla put the cursor there directly
     * (player clicked a vanilla slot without going through SLOT) — Phase B
     * routes that case through smart-deposit.
     */
    private CursorOrigin cursorOrigin = null;

    enum CursorSourceKind { CARRY, CHEST, HOST_SLOT }

    record CursorOrigin(CursorSourceKind kind, String sourceId, int slotIndex) {
        CursorOrigin {
            sourceId = sourceId == null ? "" : sourceId;
        }
    }

    SlotWorkspaceUiSession(Player player) {
        this.player = player;
    }

    SlotWorkspaceViewModel viewModel() {
        return viewModel;
    }

    Tag viewTag() {
        if (player instanceof ServerPlayer serverPlayer) {
            refreshServerView(serverPlayer);
        }
        return lastViewTag == null ? SlotWorkspaceViewModelCodec.encode(viewModel, player.registryAccess()) : lastViewTag.copy();
    }

    void acceptRemoteView(Tag tag) {
        viewModel = SlotWorkspaceViewModelCodec.decode(player.registryAccess(), tag);
    }

    /**
     * Eager extract to the menu cursor. Resolves the identity, walks
     * carry → backpacks (via {@link CarriedSourceAccess#findIdentity})
     * → proximate chests by affinity (matching {@link #takeByIdentity})
     * and writes the extracted stack to {@code menu.setCarried(...)}.
     * Stamps {@link #cursorOrigin} so a subsequent right-click cancel
     * (Phase B) can route the stack back to the exact source slot.
     *
     * <p>{@code count} caps the extract amount; pass
     * {@link Integer#MAX_VALUE} for "as much as fits on the cursor."
     * If the cursor already holds the same identity, the new amount is
     * merged into it up to the stack's max size; mixing identities is
     * rejected (Phase B will replace this with cancel + pickup).
     */
    void pickupToCursor(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer count
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        if (menu == null) {
            reject("no_menu");
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        int requested = (count == null || count <= 0) ? Integer.MAX_VALUE : count;
        ItemStack carriedStack = menu.getCarried();
        boolean cursorHasSameIdentity = !carriedStack.isEmpty()
                && identity.equals(ItemIdentityMatcher.create(carriedStack));
        if (!carriedStack.isEmpty() && !cursorHasSameIdentity) {
            reject("cursor_occupied");
            return;
        }
        int cursorRoom;
        if (cursorHasSameIdentity) {
            cursorRoom = carriedStack.getMaxStackSize() - carriedStack.getCount();
            if (cursorRoom <= 0) {
                reject("cursor_full");
                return;
            }
        } else {
            cursorRoom = Integer.MAX_VALUE;
        }
        int amount = Math.min(requested, cursorRoom);
        if (amount <= 0) {
            reject("cursor_full");
            return;
        }

        ItemStack extracted = ItemStack.EMPTY;
        CursorOrigin newOrigin = null;
        String sourceLabel = "";

        CarriedSourceAccess carriedAccess = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> carriedLoc = carriedAccess.findIdentity(serverPlayer, identity);
        if (carriedLoc.isPresent()) {
            CarriedSourceAccess.CarriedLocation loc = carriedLoc.get();
            ItemStack peeked = carriedAccess.peek(serverPlayer, loc.sourceId(), loc.slotIndex());
            if (!peeked.isEmpty()) {
                int extractAmount = Math.min(amount, peeked.getCount());
                extracted = carriedAccess.extract(serverPlayer, loc.sourceId(), loc.slotIndex(), extractAmount, false);
                if (!extracted.isEmpty()) {
                    newOrigin = new CursorOrigin(CursorSourceKind.CARRY, loc.sourceId(), loc.slotIndex());
                    sourceLabel = loc.sourceId();
                }
            }
        }

        if (extracted.isEmpty()) {
            MinecraftServer server = serverPlayer.getServer();
            if (server != null) {
                WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
                ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
                Set<String> proximate = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
                if (!proximate.isEmpty()) {
                    long tick = serverPlayer.serverLevel().getGameTime();
                    ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap().decayed(tick);
                    java.util.List<ClaimedChest> ranked = DepositPlanner.rankProximateChestsForTake(
                            identity, claimedChestMap, affinityMap, proximate);
                    WorldStorageAccess worldStorage = StorageAccessRegistry.worldStorageAccess();
                    for (ClaimedChest chest : ranked) {
                        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
                        for (WorldStorageAccess.SlotContent entry : worldStorage.enumerate(server, target)) {
                            ItemStack stackInChest = entry.stack();
                            if (stackInChest.isEmpty()) {
                                continue;
                            }
                            if (!identity.equals(ItemIdentityMatcher.create(stackInChest))) {
                                continue;
                            }
                            int extractAmount = Math.min(amount, stackInChest.getCount());
                            ItemStack pulled = worldStorage.extract(server, target, entry.slotIndex(), extractAmount, false);
                            if (pulled != null && !pulled.isEmpty()) {
                                extracted = pulled;
                                newOrigin = new CursorOrigin(CursorSourceKind.CHEST,
                                        chest.storageId().toString(), entry.slotIndex());
                                String label = chest.label();
                                sourceLabel = (label == null || label.isBlank())
                                        ? chest.storageId().toString()
                                        : label;
                                break;
                            }
                        }
                        if (!extracted.isEmpty()) {
                            break;
                        }
                    }
                }
            }
        }

        if (extracted.isEmpty()) {
            reject("nothing_to_pick");
            return;
        }

        if (cursorHasSameIdentity) {
            ItemStack merged = carriedStack.copy();
            merged.grow(extracted.getCount());
            menu.setCarried(merged);
        } else {
            menu.setCarried(extracted);
        }
        cursorOrigin = newOrigin;
        status = "picked_up";
        diagnostics = "moved=" + extracted.getCount() + " from=" + sourceLabel;
        SlotDebugLog.log("[cursor][pickup] {} count={} from={} kind={}",
                identity.itemId(), extracted.getCount(), sourceLabel,
                newOrigin == null ? "?" : newOrigin.kind());
        broadcast(serverPlayer);
    }

    /**
     * Universal cancel: route the cursor stack back to its origin. Used
     * by the right-click handler (universal-table row 1). Falls through
     * to {@link #smartDepositLeftover} when origin is missing, the chest
     * is gone, or the original source can't fully accept the stack.
     */
    void cursorCancel() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        if (menu == null) {
            return;
        }
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            cursorOrigin = null;
            return;
        }
        ItemStack remaining = carried.copy();
        CursorOrigin origin = cursorOrigin;
        if (origin != null) {
            switch (origin.kind()) {
                case CARRY -> {
                    CarriedSourceAccess access = StorageAccessRegistry.carriedSourceAccess();
                    ItemStack leftover = access.insertBestFit(serverPlayer, remaining, false);
                    remaining = leftover == null ? ItemStack.EMPTY : leftover;
                }
                case CHEST -> {
                    MinecraftServer server = serverPlayer.getServer();
                    if (server != null) {
                        ClaimedChest chest = lookupChestByStorageId(serverPlayer, origin.sourceId());
                        if (chest != null) {
                            WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
                            WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
                            ItemStack leftover = world.insert(server, target, remaining, false);
                            remaining = leftover == null ? ItemStack.EMPTY : leftover;
                        }
                    }
                }
                case HOST_SLOT -> {
                    // Recipes / machine result slots can't accept items back
                    // cleanly. Smart-deposit handles it.
                }
            }
        }
        if (!remaining.isEmpty()) {
            remaining = smartDepositLeftover(serverPlayer, remaining);
        }
        menu.setCarried(remaining);
        if (remaining.isEmpty()) {
            cursorOrigin = null;
            status = "cursor_cancelled";
        } else {
            status = "cursor_partial_cancel";
        }
        diagnostics = "remaining=" + remaining.getCount();
        SlotDebugLog.log("[cursor][cancel] kind={} remaining={}",
                origin == null ? "null" : origin.kind(), remaining.getCount());
        broadcast(serverPlayer);
    }

    /**
     * Smart-deposit: route the cursor stack through the cascade —
     * desired-count gap fill → proximate chest with affinity → home
     * routing → Triage. Used by the universal-table row 8 (left-click
     * on no specific target while carrying) and as the fallback for
     * {@link #cursorCancel} when origin can't accept.
     */
    void cursorSmartDeposit() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        if (menu == null) {
            return;
        }
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            cursorOrigin = null;
            return;
        }
        ItemStack remaining = smartDepositLeftover(serverPlayer, carried.copy());
        menu.setCarried(remaining);
        if (remaining.isEmpty()) {
            cursorOrigin = null;
            status = "cursor_deposited";
        } else {
            status = "cursor_partial_deposit";
        }
        diagnostics = "remaining=" + remaining.getCount();
        SlotDebugLog.log("[cursor][smart-deposit] remaining={}", remaining.getCount());
        broadcast(serverPlayer);
    }

    /**
     * Drop the cursor stack onto a player-hotbar slot. Translates to
     * vanilla {@code menu.clicked(slotId, button, ClickType.PICKUP)}
     * so left-click does drop-all/merge/swap and right-click does
     * drop-one — the same semantics vanilla applies to its own slots.
     * Used by belt panel left/right click when the cursor is non-empty.
     */
    void dropCursorAtHotbar(Integer hotbarIndex, Integer button) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        if (menu == null) {
            return;
        }
        int idx = hotbarIndex == null ? -1 : hotbarIndex;
        if (idx < 0 || idx >= 9) {
            reject("invalid_hotbar_slot");
            return;
        }
        if (menu.getCarried().isEmpty()) {
            return;
        }
        int menuSlotId = -1;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot s = menu.slots.get(i);
            if (s.container == serverPlayer.getInventory() && s.getContainerSlot() == idx) {
                menuSlotId = i;
                break;
            }
        }
        if (menuSlotId < 0) {
            reject("hotbar_slot_not_in_menu");
            return;
        }
        int btn = button == null ? 0 : button;
        if (btn != 0 && btn != 1) {
            return;
        }
        menu.clicked(menuSlotId, btn, ClickType.PICKUP, serverPlayer);
        if (menu.getCarried().isEmpty()) {
            cursorOrigin = null;
            status = "cursor_deposited";
        } else {
            status = "cursor_partial_deposit";
        }
        diagnostics = "remaining=" + menu.getCarried().getCount();
        broadcast(serverPlayer);
    }

    /**
     * Direct chest drop: insert the cursor stack into a specific chest
     * (via {@link WorldStorageAccess#insert}). Bumps affinity so future
     * routing prefers this chest. Used by chest chip / loot chest panel
     * left-click when the cursor is non-empty (universal-table rows 5,
     * 6).
     */
    void dropCursorIntoChest(String storageIdRaw) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        if (menu == null) {
            return;
        }
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return;
        }
        ChestProximityResult resolved = resolveProximateChest(serverPlayer, storageIdRaw);
        if (resolved.outcome != null) {
            applyChestDepositRejection(serverPlayer, resolved.outcome);
            return;
        }
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) {
            reject("server_unavailable");
            return;
        }
        ItemStack remaining = carried.copy();
        ItemIdentity identity = ItemIdentityMatcher.create(remaining);
        int beforeCount = remaining.getCount();
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(resolved.chest);
        ItemStack leftover = world.insert(server, target, remaining, false);
        remaining = leftover == null ? ItemStack.EMPTY : leftover;
        int deposited = beforeCount - remaining.getCount();
        if (deposited > 0) {
            workflowRuntime(serverPlayer).chestClaimWorkflow().recordDeposit(
                    resolved.chest.storageId(), identity, deposited,
                    serverPlayer.serverLevel().getGameTime());
        }
        menu.setCarried(remaining);
        if (remaining.isEmpty()) {
            cursorOrigin = null;
            status = "cursor_deposited";
        } else {
            status = "cursor_partial_deposit";
        }
        diagnostics = "deposited=" + deposited + " remaining=" + remaining.getCount();
        SlotDebugLog.log("[cursor][drop-chest] chest={} deposited={} remaining={}",
                resolved.chest.storageId(), deposited, remaining.getCount());
        broadcast(serverPlayer);
    }

    private ItemStack smartDepositLeftover(ServerPlayer serverPlayer, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack remaining = stack.copy();
        ItemIdentity identity = ItemIdentityMatcher.create(remaining);
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);

        // Step 1: satisfy desired-count gap by inserting into player carry.
        int desired = runtime.desiredCountWorkflow().resolved(runtime.snapshot().kitMap(), identity);
        if (desired > 0) {
            int currentInCarry = totalCarriedCount(serverPlayer, identity);
            int gap = Math.max(0, desired - currentInCarry);
            if (gap > 0) {
                int amountToFill = Math.min(gap, remaining.getCount());
                ItemStack toInsert = remaining.copyWithCount(amountToFill);
                CarriedSourceAccess carriedAccess = StorageAccessRegistry.carriedSourceAccess();
                ItemStack carryLeftover = carriedAccess.insertBestFit(serverPlayer, toInsert, false);
                int leftoverCount = carryLeftover == null || carryLeftover.isEmpty() ? 0 : carryLeftover.getCount();
                int actuallyInserted = amountToFill - leftoverCount;
                remaining.shrink(actuallyInserted);
            }
        }
        if (remaining.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Step 2: deposit to proximate chest using the canonical
        // {@link DepositPlanner} ranking — direct affinity > facet-
        // similar > presence. Sharing the planner with the deposit
        // button means there's exactly one definition of "where does
        // this item belong"; the cursor and button paths can never
        // drift again.
        MinecraftServer server = serverPlayer.getServer();
        if (server != null) {
            ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
            Set<String> proximate = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
            if (!proximate.isEmpty()) {
                long tick = serverPlayer.serverLevel().getGameTime();
                ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap().decayed(tick);
                java.util.Map<UUID, java.util.Set<ItemIdentity>> contentsCache = new java.util.HashMap<>();
                java.util.function.Function<UUID, java.util.Set<ItemIdentity>> contentsLookup =
                        chestContentsLookup(server, claimedChestMap, contentsCache);
                java.util.List<UUID> ranked = DepositPlanner.rankChestsForIdentity(
                        identity, claimedChestMap, affinityMap, proximate,
                        this::descriptorForIdentity, contentsLookup);
                WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
                for (UUID storageUuid : ranked) {
                    ClaimedChest chest = claimedChestMap.chest(storageUuid);
                    if (chest == null) {
                        continue;
                    }
                    int beforeCount = remaining.getCount();
                    WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
                    ItemStack leftover = world.insert(server, target, remaining, false);
                    remaining = leftover == null ? ItemStack.EMPTY : leftover;
                    int depositedHere = beforeCount - remaining.getCount();
                    if (depositedHere > 0) {
                        runtime.chestClaimWorkflow().recordDeposit(
                                storageUuid, identity, depositedHere, tick);
                    }
                    if (remaining.isEmpty()) {
                        break;
                    }
                }
            }
        }
        if (remaining.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Step 3-5: home routing and Triage fold into a final
        // insertBestFit fallback. Phase B v1: the wall card / Triage
        // distinction is downstream of the projection, not the carry
        // primitive — so insertBestFit ends up in the player's carry
        // which is exactly where home / Triage land at projection time.
        CarriedSourceAccess carriedAccess = StorageAccessRegistry.carriedSourceAccess();
        ItemStack finalLeftover = carriedAccess.insertBestFit(serverPlayer, remaining, false);
        return finalLeftover == null ? ItemStack.EMPTY : finalLeftover;
    }

    /**
     * Build a memoised chest-contents lookup for the
     * {@link DepositPlanner}'s presence tier. Reads each chest at most
     * once per call and returns a stable, copy-of identity set so
     * repeated lookups during a single deposit pass are O(1) after the
     * first hit. Both the deposit-button RPC and the cursor smart-
     * deposit pipeline build their own cache via this helper.
     */
    private static java.util.function.Function<UUID, java.util.Set<ItemIdentity>>
            chestContentsLookup(
                    MinecraftServer server,
                    ClaimedChestMap claimedChestMap,
                    java.util.Map<UUID, java.util.Set<ItemIdentity>> cache
            ) {
        return storageId -> {
            if (server == null) {
                return java.util.Set.of();
            }
            return cache.computeIfAbsent(storageId, id -> {
                ClaimedChest chest = claimedChestMap.chest(id);
                if (chest == null) {
                    return java.util.Set.of();
                }
                SlotWorkspaceViewModel.ChestContentsSnapshot snapshot =
                        ChestContentsReader.read(server, chest);
                java.util.LinkedHashSet<ItemIdentity> identities = new java.util.LinkedHashSet<>();
                for (ItemStack stack : snapshot.contents()) {
                    if (stack != null && !stack.isEmpty()) {
                        identities.add(ItemIdentityMatcher.create(stack));
                    }
                }
                return java.util.Set.copyOf(identities);
            });
        };
    }

    private int totalCarriedCount(ServerPlayer serverPlayer, ItemIdentity identity) {
        CarriedSourceAccess access = StorageAccessRegistry.carriedSourceAccess();
        int total = 0;
        for (CarriedSourceAccess.CarriedLocation loc : access.findAllMatching(serverPlayer, identity)) {
            total += access.peek(serverPlayer, loc.sourceId(), loc.slotIndex()).getCount();
        }
        return total;
    }

    private ClaimedChest lookupChestByStorageId(ServerPlayer serverPlayer, String storageIdRaw) {
        if (storageIdRaw == null || storageIdRaw.isBlank()) {
            return null;
        }
        UUID storageId;
        try {
            storageId = UUID.fromString(storageIdRaw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        return workflowRuntime(serverPlayer).chestClaimWorkflow().claimedChestMap().chest(storageId);
    }

    /**
     * Cross-surface: a wall card was drag-released over a vanilla slot
     * in the host menu (chest, crafting input, machine input). Find a
     * player-inventory slot inside the host menu carrying the same
     * identity, then synthesize the vanilla two-click sequence
     * {@code PICKUP source → PICKUP target} so the host menu's own
     * {@code Slot.mayPlace} / {@code Slot.safeInsert} logic governs
     * the move (so e.g. crafting input slot's max-stack-size-1 still
     * applies and machine input filters reject what they reject).
     *
     * <p>If the target rejects part of the stack the leftover stays on
     * the menu's cursor; we PICKUP the source again to put it back,
     * and as a final fallback drop any irreducible leftover into the
     * player inventory.
     */
    void crossSurfaceDropOnHostSlot(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer hostSlotIndex
    ) {
        SlotDebugLog.log(
                "[xsurface][server] DropOnHostSlot itemId={} cmp={} fp='{}' hostSlot={}",
                itemId, comparisonMode, componentFingerprint, hostSlotIndex);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            SlotDebugLog.log("[xsurface][server] drop bailing: player is not ServerPlayer");
            return;
        }
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        if (menu == null || hostSlotIndex == null
                || hostSlotIndex < 0 || hostSlotIndex >= menu.slots.size()) {
            SlotDebugLog.log(
                    "[xsurface][server] drop bailing: menu={} hostSlot={} slotCount={}",
                    menu == null ? "null" : menu.getClass().getSimpleName(),
                    hostSlotIndex,
                    menu == null ? 0 : menu.slots.size());
            return;
        }
        Slot targetSlot = menu.slots.get(hostSlotIndex);
        // Player-side slot as target is a no-op shuffle — the cross-surface
        // gesture only makes sense for "outside the player inventory" slots.
        if (targetSlot.container == serverPlayer.getInventory()) {
            SlotDebugLog.log("[xsurface][server] drop bailing: target slot is player-side");
            return;
        }
        ItemIdentity identity = new SlotWorkspaceViewModel.IdentityRef(itemId, comparisonMode, componentFingerprint).toIdentity();
        if (identity == null) {
            SlotDebugLog.log("[xsurface][server] drop bailing: identity null");
            return;
        }
        // Use CarriedSourceAccess instead of a raw player.getInventory()
        // scan: items can live in backpacks / curios / future provider
        // sources too, and their entries are not in the menu's slots
        // list at all. CarriedSourceAccess walks every registered carried
        // source (vanilla main + hotbar + offhand + armor + every
        // backpack) in stableOrder.
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> located = carried.findIdentity(serverPlayer, identity);
        if (located.isEmpty()) {
            SlotDebugLog.log("[xsurface][server] drop: no carried source has {}", identity.itemId());
            return;
        }
        CarriedSourceAccess.CarriedLocation loc = located.get();
        ItemStack peeked = carried.peek(serverPlayer, loc.sourceId(), loc.slotIndex());
        if (peeked.isEmpty()) {
            SlotDebugLog.log(
                    "[xsurface][server] drop: peek({}, {}) returned empty after findIdentity hit",
                    loc.sourceId(), loc.slotIndex());
            return;
        }
        // Cap the extract at what the target slot can accept in one go
        // (its max stack size for this stack). Anything beyond the slot's
        // accept window stays in the source — no need to extract just to
        // immediately put back.
        int maxAccept = Math.max(1, targetSlot.getMaxStackSize(peeked));
        int extractAmount = Math.min(peeked.getCount(), maxAccept);
        ItemStack extracted = carried.extract(serverPlayer, loc.sourceId(), loc.slotIndex(), extractAmount, false);
        if (extracted.isEmpty()) {
            SlotDebugLog.log(
                    "[xsurface][server] drop: extract({}, {}, {}) returned empty",
                    loc.sourceId(), loc.slotIndex(), extractAmount);
            return;
        }
        ItemStack targetBefore = targetSlot.getItem().copy();
        ItemStack leftover = targetSlot.safeInsert(extracted);
        ItemStack putBack = leftover.isEmpty() ? ItemStack.EMPTY : carried.insertBestFit(serverPlayer, leftover, false);
        SlotDebugLog.log(
                "[xsurface][server] drop done: extracted {} from {}#{}, target {} → {}, leftover={}, putback-remainder={}",
                describeStack(extracted),
                loc.sourceId(),
                loc.slotIndex(),
                describeStack(targetBefore),
                describeStack(targetSlot.getItem()),
                describeStack(leftover),
                describeStack(putBack));
        broadcast(serverPlayer);
    }

    /**
     * Cross-surface: shift+click or shift+wheel-up on a wall card
     * routed to the host menu. Synthesizes vanilla shift-click on a
     * player-inventory slot containing the identity, repeated up to
     * {@code count} times so the host menu's {@code quickMoveStack}
     * fans the stacks across whatever slots it considers "outside"
     * (crafting matrix, machine inputs, chest slots, etc.).
     *
     * <p>Bails when no further player slot carries the identity, or
     * when a quickMove call results in no change — the latter prevents
     * an infinite loop if the host's {@code quickMoveStack} returns
     * the source unchanged (e.g. nothing accepts it).
     */
    void crossSurfaceQuickMoveAtlas(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer count
    ) {
        SlotDebugLog.log(
                "[xsurface][server] QuickMoveAtlas itemId={} cmp={} fp='{}' count={}",
                itemId, comparisonMode, componentFingerprint, count);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            SlotDebugLog.log("[xsurface][server] quickMove bailing: player is not ServerPlayer");
            return;
        }
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        if (menu == null || count == null || count <= 0) {
            SlotDebugLog.log(
                    "[xsurface][server] quickMove bailing: menu={} count={}",
                    menu == null ? "null" : menu.getClass().getSimpleName(), count);
            return;
        }
        ItemIdentity identity = new SlotWorkspaceViewModel.IdentityRef(itemId, comparisonMode, componentFingerprint).toIdentity();
        if (identity == null) {
            SlotDebugLog.log("[xsurface][server] quickMove bailing: identity null");
            return;
        }
        // For QuickMove the source can be in a backpack (not in the
        // menu's slots list), so vanilla menu.clicked QUICK_MOVE on the
        // source isn't an option. Mirror the drop path: extract one
        // stack from any carried source per iteration via
        // CarriedSourceAccess, then walk the host's non-player menu
        // slots and try Slot.safeInsert on each until the stack is
        // consumed or no host slot accepts. safeInsert respects each
        // slot's mayPlace so crafting input limits, machine input
        // filters, and chest accept rules all govern natively.
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        int requested = Math.min(count, 64);
        int remaining = requested;
        int moved = 0;
        while (remaining > 0) {
            Optional<CarriedSourceAccess.CarriedLocation> located = carried.findIdentity(serverPlayer, identity);
            if (located.isEmpty()) {
                SlotDebugLog.log(
                        "[xsurface][server] quickMove halt: no carried source has {} (remaining={})",
                        identity.itemId(), remaining);
                break;
            }
            CarriedSourceAccess.CarriedLocation loc = located.get();
            ItemStack peeked = carried.peek(serverPlayer, loc.sourceId(), loc.slotIndex());
            if (peeked.isEmpty()) {
                break;
            }
            ItemStack extracted = carried.extract(serverPlayer, loc.sourceId(), loc.slotIndex(), peeked.getCount(), false);
            if (extracted.isEmpty()) {
                break;
            }
            int extractedCount = extracted.getCount();
            ItemStack remainingStack = extracted;
            for (Slot hostSlot : menu.slots) {
                if (remainingStack.isEmpty()) {
                    break;
                }
                if (hostSlot.container == serverPlayer.getInventory()) {
                    continue;
                }
                if (!hostSlot.mayPlace(remainingStack)) {
                    continue;
                }
                remainingStack = hostSlot.safeInsert(remainingStack);
            }
            int placed = extractedCount - (remainingStack.isEmpty() ? 0 : remainingStack.getCount());
            if (!remainingStack.isEmpty()) {
                carried.insertBestFit(serverPlayer, remainingStack, false);
            }
            if (placed <= 0) {
                SlotDebugLog.log(
                        "[xsurface][server] quickMove halt: host menu rejected {} ({} extracted, all returned)",
                        identity.itemId(), extractedCount);
                break;
            }
            moved++;
            remaining--;
        }
        SlotDebugLog.log(
                "[xsurface][server] quickMove done itemId={} requested={} moved={}",
                identity.itemId(), requested, moved);
        broadcast(serverPlayer);
    }

    private static String describeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "EMPTY";
        }
        return stack.getCount() + "x" + stack.getItem();
    }

    void transfer(Integer sourceKind, Integer sourceIndex, Integer destinationKind, Integer destinationIndex, String origin) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        InventoryActionTarget source = target(sourceKind, sourceIndex, true);
        InventoryActionTarget destination = target(destinationKind, destinationIndex, false);
        if (source == null || destination == null) {
            SlotDiagnostics.workspaceTransferInvalid(sourceKind, sourceIndex, destinationKind, destinationIndex, origin);
            reject("invalid_transfer_target");
            return;
        }
        WorkspaceTransferExecution execution = executeTransfer(serverPlayer, source, destination, origin);
        status = execution.feedback().status();
        diagnostics = execution.feedback().diagnostics();
        broadcast(serverPlayer);
    }

    void assignHome(String itemId, String comparisonMode, String componentFingerprint, String islandId, Integer ordinal) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.assignHome(
                workflowRuntime(serverPlayer),
                viewModel,
                learnedRules,
                IslandSignalExtractor::extract,
                itemId,
                comparisonMode,
                componentFingerprint,
                islandId,
                ordinal
        );
        applyOutcome(serverPlayer, outcome);
    }

    void acceptChip(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            String chipIslandId,
            String templateName
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.acceptChip(
                workflowRuntime(serverPlayer),
                viewModel,
                learnedRules,
                IslandSignalExtractor::extract,
                itemId,
                comparisonMode,
                componentFingerprint,
                chipIslandId,
                templateName
        );
        applyOutcome(serverPlayer, outcome);
    }

    void createNamedIslandForItem(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            String label,
            Integer color,
            Integer worldX,
            Integer worldY
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.createNamedIslandForItem(
                workflowRuntime(serverPlayer),
                viewModel,
                learnedRules,
                IslandSignalExtractor::extract,
                itemId,
                comparisonMode,
                componentFingerprint,
                label,
                color,
                worldX,
                worldY
        );
        applyOutcome(serverPlayer, outcome);
    }

    void moveIsland(String islandId, Double worldX, Double worldY) {
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] session.moveIsland received id={} requestedX={} requestedY={}",
                islandId, worldX, worldY);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.moveIsland(
                workflowRuntime(serverPlayer),
                viewModel,
                islandId,
                worldX,
                worldY
        );
        if (outcome.success()) {
            dev.imagio.slot.SlotCommon.LOGGER.info(
                    "[SLOT] session.moveIsland applied id={} newPos=({},{})",
                    islandId, worldX, worldY);
        }
        applyOutcome(serverPlayer, outcome);
    }

    void reorderIsland(String islandId, Integer targetIndex) {
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] session.reorderIsland received id={} targetIndex={}",
                islandId, targetIndex);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome outcome = SlotWorkspaceCommandService.reorderIsland(
                workflowRuntime(serverPlayer),
                viewModel,
                islandId,
                targetIndex
        );
        applyOutcome(serverPlayer, outcome);
    }

    void deposit() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            dev.imagio.slot.SlotCommon.LOGGER.warn(
                    "[SLOT] deposit RPC received but player is not a ServerPlayer (player={})", player);
            return;
        }
        // No leading refreshServerView: the client view we're acting on
        // is from the *previous* broadcast (or the deposit button
        // wouldn't have been clickable), and the proximity / affinity
        // lookups below read from the runtime + world directly. The
        // trailing broadcast() rebuilds the view post-deposit; doing it
        // twice was the dominant cost on the deposit click path.
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] deposit RPC received: player={} claimedChests={} proximate={}",
                serverPlayer.getName().getString(),
                claimedChestMap.chests().size(),
                proximate.size());
        if (proximate.isEmpty()) {
            status = "rejected";
            diagnostics = "no_proximate_chest";
            broadcast(serverPlayer);
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        long tick = serverPlayer.serverLevel().getGameTime();
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap().decayed(tick);
        MinecraftServer server = serverPlayer.getServer();
        java.util.Map<UUID, java.util.Set<ItemIdentity>> contentsCache = new java.util.HashMap<>();
        java.util.function.ToIntFunction<ItemIdentity> reservedCountResolver =
                identity -> {
                    var kitMap = runtime.snapshot().kitMap();
                    var activation = kitMap == null ? null : kitMap.activation();
                    String kitId = activation != null && activation.isActive() ? activation.kitId() : null;
                    java.util.Map<ItemIdentity, Integer> activeKitDesired = kitId == null
                            ? java.util.Map.of()
                            : runtime.snapshot().kitDesiredCounts().getOrDefault(kitId, java.util.Map.of());
                    return SlotWorkspaceViewModel.reservedCarryCount(
                            identity,
                            kitMap,
                            activeKitDesired,
                            runtime.snapshot().playerDesiredCounts());
                };
        DepositPlan plan = DepositPlanner.plan(
                authority,
                affinityMap,
                claimedChestMap,
                proximate,
                this::descriptorForIdentity,
                chestContentsLookup(server, claimedChestMap, contentsCache),
                reservedCountResolver
        );
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] deposit plan: assignments={} (one per stack with affinity / facet / presence)",
                plan.assignments().size());
        DepositExecutor.DepositOutcome outcome = DepositExecutor.execute(serverPlayer, plan, claimedChestMap);
        for (DepositExecutor.DepositRecord record : outcome.records()) {
            runtime.chestClaimWorkflow().recordDeposit(
                    record.storageId(), record.identity(), record.count(), tick);
        }
        if (outcome.deposited() > 0 && !outcome.records().isEmpty()) {
            // Snapshot the records so the closure doesn't share state with
            // future deposits. Single batched undo entry covers all stacks
            // moved by this click. Affinity bumps recorded above are NOT
            // reverted on undo — affinity is a learned signal that decays
            // naturally, and there's no decrement-by-N API; replaying via
            // redo would re-bump anyway.
            java.util.List<DepositExecutor.DepositRecord> records = java.util.List.copyOf(outcome.records());
            ServerPlayer undoPlayer = serverPlayer;
            String label = records.size() == 1
                    ? "deposit " + records.get(0).identity().itemId()
                    : "deposit (" + records.size() + ")";
            runtime.undoStack().record(
                    label,
                    ctx -> {
                        for (DepositExecutor.DepositRecord r : records) {
                            DepositReverser.pullFromChestToCarry(
                                    undoPlayer, r.storageId(), r.identity(), r.count());
                        }
                    },
                    ctx -> {
                        for (DepositExecutor.DepositRecord r : records) {
                            DepositReverser.pushFromCarryToChest(
                                    undoPlayer, r.storageId(), r.identity(), r.count());
                        }
                    }
            );
        }
        if (outcome.deposited() == 0 && outcome.failed() == 0) {
            // Empty plan + empty outcome means no carried stack had a
            // direct affinity bond, facet-similar bond, OR presence in
            // any proximate claimed chest. Surface this clearly so the
            // player understands the affinity-driven nature of deposit
            // (vs "deposit anything") — the chest still needs to either
            // hold the item already or have a learned bond before it
            // becomes a deposit target.
            status = "nothing_to_deposit";
            diagnostics = plan.assignments().isEmpty()
                    ? "no carried stack matches a proximate chest (no affinity, no facet match, not present)"
                    : "all candidate chests rejected the items";
        } else if (outcome.deposited() > 0 && outcome.failed() == 0) {
            status = "deposited";
            diagnostics = "deposited=" + outcome.deposited();
        } else if (outcome.deposited() == 0) {
            status = "rejected";
            diagnostics = "deposit_failed=" + outcome.failed();
        } else {
            status = "deposited_partial";
            diagnostics = "deposited=" + outcome.deposited() + " failed=" + outcome.failed();
        }
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] deposit complete: status={} diagnostics={}", status, diagnostics);
        broadcast(serverPlayer);
    }

    /**
     * Resolve an {@link ItemIdentity} back to an
     * {@link dev.imagio.slot.inventory.triage.IslandSignalDescriptor}
     * for the deposit-planner facet-affinity fallback. Builds a
     * synthetic default {@link ItemStack} via the registry, which is
     * enough to recover tags, role, material_family, etc. — chest
     * affinity bonds don't carry component fingerprints, so the
     * default stack is good enough for the adjacency-key lookup.
     * Returns {@code null} when the registry doesn't have the item
     * (datapack-only or removed mods); the caller treats null as "no
     * facet match" and falls through to direct affinity only.
     */
    private dev.imagio.slot.inventory.triage.IslandSignalDescriptor descriptorForIdentity(ItemIdentity identity) {
        if (identity == null || identity.itemId() == null || identity.itemId().isBlank()) {
            return null;
        }
        ItemStack stack = GhostAtlasStackFactory.resolve(identity.itemId());
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        try {
            return IslandSignalExtractor.extract(stack);
        } catch (RuntimeException | LinkageError ignored) {
            return null;
        }
    }

    /**
     * Push the active client-side search query into the session so the
     * next view-model projection can gate remote-only ghost synthesis on
     * it. Without this hook, server has no way to know whether the
     * player is currently searching, and would either always or never
     * synthesize the ghosts.
     */
    void setSearchQuery(String query) {
        String normalized = query == null ? "" : query;
        if (normalized.equals(searchQuery)) {
            return;
        }
        searchQuery = normalized;
        if (player instanceof ServerPlayer serverPlayer) {
            broadcast(serverPlayer);
        }
    }

    /**
     * Claim the chest at {@code (x, y, z)} in {@code dimensionId}. Driven
     * by the active-chest strip's "Claim" button when the player is
     * looking at an unclaimed chest. Routes through
     * {@link ChestDepositObserver#resolveOrCreateClaim} so a double chest
     * folds both halves into one storage UUID and stamps the
     * {@code slot:storage_id} attachment on the partner — matching what
     * the deposit observer produces on a real deposit.
     */
    void claimChestAtPos(String dimensionId, Integer x, Integer y, Integer z) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || dimensionId == null || dimensionId.isBlank()
                || x == null || y == null || z == null) {
            reject("invalid_claim_request");
            return;
        }
        ServerLevel level = serverPlayer.serverLevel();
        if (!level.dimension().location().toString().equals(dimensionId)) {
            reject("dimension_mismatch");
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        if (!ChestStorageAnchors.isClaimable(level, pos)) {
            reject("not_claimable");
            return;
        }
        ChestAnchor anchor = ChestStorageAnchors.toAnchor(level, pos);
        if (anchor == null) {
            reject("anchor_resolution_failed");
            return;
        }
        UUID storageId = ChestDepositObserver.resolveOrCreateClaim(
                workflowRuntime(serverPlayer).chestClaimWorkflow(), level, pos, anchor);
        if (storageId == null) {
            reject("claim_failed");
            return;
        }
        status = "chest_claimed";
        diagnostics = "";
        broadcast(serverPlayer);
    }

    void forgetChest(String storageId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        // Capture anchors BEFORE the workflow service deletes the chest;
        // afterwards the chest record is gone and we can't find its
        // world positions to clear the stale BE storage-id attachment.
        // Without this clear, a forgotten chest still shows up as
        // "claimed" via {@link ChestStorageIds#read} on the next
        // deposit, blocking re-claim — the bug where forgetting a
        // chest meant losing it permanently until block-break.
        java.util.List<BlockPos> anchorPositions = anchorPositionsForChest(serverPlayer, storageId);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.forgetChest(
                workflowRuntime(serverPlayer),
                storageId
        ));
        ServerLevel level = serverPlayer.serverLevel();
        for (BlockPos pos : anchorPositions) {
            ChestStorageIds.clear(level, pos);
        }
    }

    private java.util.List<BlockPos> anchorPositionsForChest(ServerPlayer serverPlayer, String storageId) {
        if (storageId == null || storageId.isBlank()) {
            return java.util.List.of();
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(storageId);
        } catch (IllegalArgumentException ignored) {
            return java.util.List.of();
        }
        ClaimedChest existing = workflowRuntime(serverPlayer).chestClaimWorkflow()
                .claimedChestMap().chest(uuid);
        if (existing == null) {
            return java.util.List.of();
        }
        String currentDimension = serverPlayer.serverLevel().dimension().location().toString();
        java.util.ArrayList<BlockPos> positions = new java.util.ArrayList<>();
        existing.anchors().forEach(anchor -> {
            if (currentDimension.equals(anchor.dimensionId())) {
                positions.add(new BlockPos(anchor.x(), anchor.y(), anchor.z()));
            }
        });
        return positions;
    }

    void forgetItemAffinity(String storageId, String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.forgetItemAffinity(
                workflowRuntime(serverPlayer),
                storageId,
                itemId,
                comparisonMode,
                componentFingerprint
        ));
    }

    /**
     * Take every stack from a loot (unclaimed) chest at the supplied
     * world position into the player's inventory. Auto-accepts the top
     * chip suggestion for any unhomed identity in the chest before
     * taking, so items land at their suggested home instead of in
     * Triage. Used by the atlas-side loot panel's "Take all" button.
     */
    void takeAllFromLootChest(String dimensionId, Integer x, Integer y, Integer z) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        // Refresh so viewModel.lootChestPanel().items() reflects the
        // current chip suggestions for this chest.
        refreshServerView(serverPlayer);
        for (SlotWorkspaceViewModel.AtlasItem item : viewModel.lootChestPanel().items()) {
            if (item.playerPlaced()) {
                continue;
            }
            // Skip items already at a non-Triage home — homed items will
            // be routed by deposit flow naturally.
            if (!item.islandId().isBlank()
                    && !item.islandId().equals(SlotWorkspaceAtlasLayout.ISLAND_TRIAGE)) {
                continue;
            }
            if (item.chipSuggestions().isEmpty()) {
                continue;
            }
            ChipSuggestion top = item.chipSuggestions().get(0);
            acceptChip(
                    item.identity().itemId(),
                    item.identity().comparisonMode(),
                    item.identity().componentFingerprint(),
                    top.islandId(),
                    top.template() == null ? "" : top.template().name()
            );
        }
        takeFromLootChest(dimensionId, x, y, z, null, null, null);
    }

    /**
     * Take every stack matching {@code identity} from a loot chest into
     * the player's inventory. Combined with a chip-accept (sent first
     * by the client), this performs the "shift+click row → home + take"
     * gesture.
     */
    void takeIdentityFromLootChest(
            String dimensionId,
            Integer x,
            Integer y,
            Integer z,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        takeFromLootChest(dimensionId, x, y, z, itemId, comparisonMode, componentFingerprint);
    }

    /**
     * Drag-carried-onto-loot-panel claim+deposit: auto-claims the
     * unclaimed chest at the loot-panel position and immediately
     * deposits the carried identity into it. Lets the player bootstrap
     * a claim from inside the SLOT workspace without going through the
     * vanilla chest GUI — answers the "I right-clicked an unclaimed
     * chest, the loot panel is showing, but I can't deposit anything
     * into it without opening vanilla" failure mode.
     */
    void claimAndDepositCarriedToLootChest(
            String dimensionId,
            Integer x,
            Integer y,
            Integer z,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (dimensionId == null || x == null || y == null || z == null) {
            reject("invalid_loot_chest_pos");
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        ServerLevel level = serverPlayer.serverLevel();
        if (!level.dimension().location().toString().equals(dimensionId)) {
            reject("loot_chest_other_dimension");
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        long radiusSq = (long) LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS
                * LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS;
        BlockPos playerPos = serverPlayer.blockPosition();
        long dx = (long) playerPos.getX() - pos.getX();
        long dy = (long) playerPos.getY() - pos.getY();
        long dz = (long) playerPos.getZ() - pos.getZ();
        if (dx * dx + dy * dy + dz * dz > radiusSq) {
            reject("loot_chest_out_of_range");
            return;
        }
        if (!ChestStorageAnchors.isClaimable(level, pos)) {
            reject("loot_chest_not_claimable");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        var chestService = runtime.chestClaimWorkflow();
        var anchor = ChestStorageAnchors.toAnchor(level, pos);
        UUID storageId = ChestDepositObserver.resolveOrCreateClaim(chestService, level, pos, anchor);
        if (storageId == null) {
            reject("loot_chest_claim_failed");
            return;
        }
        ClaimedChest claim = chestService.claimedChestMap().chest(storageId);
        if (claim == null) {
            reject("loot_chest_claim_missing");
            return;
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> sourceLocation = carried.findIdentity(serverPlayer, identity);
        if (sourceLocation.isEmpty()) {
            // Claim still stands — the player has now made this chest
            // a storage chest even if they had nothing in carry to deposit.
            // Surface the partial success so the panel refreshes.
            refreshServerView(serverPlayer);
            status = "claimed";
            diagnostics = "claimed_no_deposit_source";
            broadcast(serverPlayer);
            return;
        }
        DepositExecutor.SingleStackOutcome outcome = DepositExecutor.depositSingleStack(
                serverPlayer,
                sourceLocation.get().sourceId(),
                sourceLocation.get().slotIndex(),
                claim);
        if (outcome.success() && outcome.record() != null) {
            chestService.recordDeposit(
                    outcome.record().storageId(),
                    outcome.record().identity(),
                    outcome.record().count(),
                    level.getGameTime());
        }
        applyChestDepositOutcome(serverPlayer, outcome, claim);
    }

    /**
     * "Open vanilla chest GUI here" escape hatch from the loot panel:
     * the right-click intercept redirects every unclaimed-chest open into
     * the SLOT atlas, leaving no path to actually deposit into a loot
     * chest. Wiring the loot panel's button to this RPC opens the
     * vanilla chest UI server-side so the player can drop items in;
     * {@link ChestDepositObserver} then auto-claims the chest and bumps
     * affinity on close. Validates the chest is still unclaimed and in
     * range before opening; rejects otherwise so a stale view-model row
     * can't be used to teleport the open into the wrong target.
     */
    void openVanillaForLootChest(String dimensionId, Integer x, Integer y, Integer z) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (dimensionId == null || x == null || y == null || z == null) {
            reject("invalid_loot_chest_pos");
            return;
        }
        ServerLevel level = serverPlayer.serverLevel();
        if (!level.dimension().location().toString().equals(dimensionId)) {
            reject("loot_chest_other_dimension");
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        long radiusSq = (long) LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS
                * LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS;
        BlockPos playerPos = serverPlayer.blockPosition();
        long dx = (long) playerPos.getX() - pos.getX();
        long dy = (long) playerPos.getY() - pos.getY();
        long dz = (long) playerPos.getZ() - pos.getZ();
        if (dx * dx + dy * dy + dz * dz > radiusSq) {
            reject("loot_chest_out_of_range");
            return;
        }
        if (!ChestStorageAnchors.isClaimable(level, pos)) {
            reject("loot_chest_not_claimable");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedMap = runtime.chestClaimWorkflow().claimedChestMap();
        if (ChestStorageIds.read(level, pos).isPresent()
                || claimedMap.chestByAnchor(ChestStorageAnchors.toAnchor(level, pos)) != null) {
            reject("loot_chest_already_claimed");
            return;
        }
        BlockState state = level.getBlockState(pos);
        MenuProvider provider = state.getMenuProvider(level, pos);
        if (provider == null) {
            reject("loot_chest_no_menu");
            return;
        }
        // Mark the synthetic open as authorized so the existing observer
        // snapshots the chest's contents and runs its auto-claim path on
        // close — without this, openMenu fires onContainerOpen with no
        // PENDING entry and the deposit is silently ignored.
        ChestDepositObserver.expectOpen(serverPlayer, pos);
        serverPlayer.openMenu(provider);
    }

    private void takeFromLootChest(
            String dimensionId,
            Integer x,
            Integer y,
            Integer z,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (dimensionId == null || x == null || y == null || z == null) {
            reject("invalid_loot_chest_pos");
            return;
        }
        ServerLevel level = serverPlayer.serverLevel();
        if (!level.dimension().location().toString().equals(dimensionId)) {
            reject("loot_chest_other_dimension");
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        long radiusSq = (long) LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS
                * LootChestProximityResolver.DEFAULT_RADIUS_BLOCKS;
        BlockPos playerPos = serverPlayer.blockPosition();
        long dx = (long) playerPos.getX() - pos.getX();
        long dy = (long) playerPos.getY() - pos.getY();
        long dz = (long) playerPos.getZ() - pos.getZ();
        if (dx * dx + dy * dy + dz * dz > radiusSq) {
            reject("loot_chest_out_of_range");
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof Container container) || !ChestStorageAnchors.isClaimable(level, pos)) {
            reject("loot_chest_not_a_chest");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedMap = runtime.chestClaimWorkflow().claimedChestMap();
        if (ChestStorageIds.read(be).isPresent() || claimedMap.chestByAnchor(
                ChestStorageAnchors.toAnchor(level, pos)) != null) {
            reject("loot_chest_already_claimed");
            return;
        }
        ItemIdentity targetIdentity = null;
        if (itemId != null && !itemId.isBlank()) {
            ItemComparisonMode mode = ItemComparisonMode.ITEM_ID;
            if (comparisonMode != null) {
                try {
                    mode = ItemComparisonMode.valueOf(comparisonMode);
                } catch (IllegalArgumentException ignored) {
                }
            }
            targetIdentity = new ItemIdentity(
                    itemId,
                    mode,
                    componentFingerprint == null ? "" : componentFingerprint
            );
        }
        int containerSize = container.getContainerSize();
        int taken = 0;
        for (int slotIndex = 0; slotIndex < containerSize; slotIndex++) {
            ItemStack stack = container.getItem(slotIndex);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            if (targetIdentity != null) {
                ItemIdentity stackIdentity = ItemIdentityMatcher.create(stack);
                if (!stackIdentity.equals(targetIdentity)) {
                    continue;
                }
            }
            ItemStack copy = stack.copy();
            ItemStack removed = container.removeItem(slotIndex, copy.getCount());
            if (removed == null || removed.isEmpty()) {
                continue;
            }
            ItemStack remaining = StorageAccessRegistry.carriedSourceAccess()
                    .insertBestFit(serverPlayer, removed, false);
            int placed = (removed.getCount() - (remaining == null || remaining.isEmpty() ? 0 : remaining.getCount()));
            if (placed <= 0) {
                // Carry full — put back what wasn't placed.
                container.setItem(slotIndex, removed);
                break;
            }
            taken += placed;
            if (remaining != null && !remaining.isEmpty()) {
                container.setItem(slotIndex, remaining);
                break;
            }
        }
        if (taken > 0) {
            container.setChanged();
            status = "looted";
            diagnostics = "taken=" + taken;
            reapplyActiveKitFromCarry(serverPlayer);
        } else {
            status = "nothing_to_take";
            diagnostics = "";
        }
        broadcast(serverPlayer);
    }

    void takeAllFromChest(String storageIdRaw) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        if (storageIdRaw == null || storageIdRaw.isBlank()) {
            status = "rejected";
            diagnostics = "invalid_chest_storage_id";
            broadcast(serverPlayer);
            return;
        }
        UUID storageId;
        try {
            storageId = UUID.fromString(storageIdRaw);
        } catch (IllegalArgumentException ignored) {
            status = "rejected";
            diagnostics = "invalid_chest_storage_id";
            broadcast(serverPlayer);
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChest chest = runtime.chestClaimWorkflow().claimedChestMap().chest(storageId);
        if (chest == null) {
            status = "rejected";
            diagnostics = "unknown_chest_tile";
            broadcast(serverPlayer);
            return;
        }
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(
                serverPlayer, runtime.chestClaimWorkflow().claimedChestMap()
        );
        if (!proximate.contains(storageIdRaw)) {
            status = "rejected";
            diagnostics = "chest_not_proximate";
            broadcast(serverPlayer);
            return;
        }
        TakeAllExecutor.TakeAllOutcome outcome = TakeAllExecutor.execute(serverPlayer, chest);
        if (outcome.movedStacks() == 0 && outcome.leftoverSlots() == 0) {
            status = "nothing_to_take";
            diagnostics = "";
        } else if (outcome.leftoverSlots() == 0) {
            status = "took_all";
            diagnostics = "moved=" + outcome.movedStacks();
        } else {
            status = "took_all_partial";
            diagnostics = "moved=" + outcome.movedStacks() + " leftover_slots=" + outcome.leftoverSlots();
        }
        if (outcome.movedStacks() > 0) {
            reapplyActiveKitFromCarry(serverPlayer);
        }
        broadcast(serverPlayer);
    }

    void relabelChest(String storageId, String label) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.relabelChest(
                workflowRuntime(serverPlayer),
                viewModel,
                storageId,
                label
        ));
    }

    void moveChest(String storageId, Integer atlasX, Integer atlasY) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.moveChest(
                workflowRuntime(serverPlayer),
                viewModel,
                storageId,
                atlasX,
                atlasY
        ));
    }

    void renameIsland(String islandId, String label) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.renameIsland(
                workflowRuntime(serverPlayer),
                islandId,
                label
        ));
    }

    void recolorIsland(String islandId, Integer color) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.recolorIsland(
                workflowRuntime(serverPlayer),
                islandId,
                color
        ));
    }

    void setIslandIcon(String islandId, String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.setIslandIcon(
                workflowRuntime(serverPlayer),
                islandId,
                itemId,
                comparisonMode,
                componentFingerprint
        ));
    }

    void deleteIsland(String islandId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.deleteIsland(
                workflowRuntime(serverPlayer),
                islandId
        ));
    }

    void saveBeltAsKit(String name) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.saveBeltAsKit(
                workflowRuntime(serverPlayer),
                authority,
                KIT_IDENTITY_RESOLVER,
                name
        ));
    }

    void activateKit(String kitId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            workflowRuntime(serverPlayer).recordOutcome(outcome);
            return outcome;
        };
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.activateKit(
                workflowRuntime(serverPlayer),
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor,
                kitId
        ));
        // Auto-fetch toward kit-scoped desired counts. Runs after the kit
        // is recorded as active so the carried snapshot reads the
        // post-apply state. Each identity gets pulled from proximate
        // chests up to (desired - currently_carried). Best-effort: if no
        // chest has the item or carry is full the gap stays.
        autoFetchKitDesiredCounts(serverPlayer, kitId);
    }

    /**
     * Pull items from proximate chests toward the kit-scoped desired
     * counts. Replaces the legacy bring-list fetch path (the bring list
     * itself is now folded into kit-scoped desired counts). Mirrors the
     * chest-walk in {@link #takeByIdentity}: highest-affinity proximate
     * chest first, falls through to the rest. Multi-pass per identity
     * because a chest may only have part of the gap; subsequent chests
     * fill the rest.
     */
    private void autoFetchKitDesiredCounts(ServerPlayer serverPlayer, String kitId) {
        if (kitId == null || kitId.isBlank()) {
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        java.util.Map<ItemIdentity, Integer> wants = runtime.desiredCountWorkflow().forKit(kitId);
        if (wants.isEmpty()) {
            return;
        }
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
        if (proximate.isEmpty()) {
            return;
        }
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap();
        // Refresh authority snapshot once before the pull loop so the
        // carried-count check sees the post-apply state.
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        for (java.util.Map.Entry<ItemIdentity, Integer> want : wants.entrySet()) {
            ItemIdentity identity = want.getKey();
            int desired = want.getValue() == null ? 0 : want.getValue();
            if (identity == null || desired <= 0) {
                continue;
            }
            int carried = countCarried(authority, identity);
            int gap = desired - carried;
            if (gap <= 0) {
                continue;
            }
            // Sort chests for THIS identity by affinity score so the most
            // likely home gets walked first.
            java.util.List<ClaimedChest> ranked = DepositPlanner.rankProximateChestsForTake(
                    identity, claimedChestMap, affinityMap, proximate);
            int remaining = gap;
            for (ClaimedChest chest : ranked) {
                if (remaining <= 0) {
                    break;
                }
                TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeByIdentity(
                        serverPlayer, chest, identity, remaining, "kit-auto-fetch");
                if (outcome.tookAnything()) {
                    remaining -= outcome.moved();
                }
            }
        }
        // Re-apply the kit so the new carry surfaces in any kit-needed slot.
        reapplyActiveKitFromCarry(serverPlayer);
    }

    private static int countCarried(InventoryAuthoritySnapshot authority, ItemIdentity identity) {
        if (authority == null || identity == null) {
            return 0;
        }
        int total = 0;
        for (var source : authority.carriedSources()) {
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                if (ItemIdentityMatcher.matchesMovable(entry.stack(), identity)) {
                    total += entry.count();
                }
            }
        }
        return total;
    }

    /**
     * Re-run the active kit's plan against the current inventory.
     * Called after any path that adds items into carry (chest take,
     * deposit, ground pickup), so a freshly-acquired item that matches
     * a still-empty kit slot snaps into that slot instead of waiting
     * for the next manual activation.
     */
    private void reapplyActiveKitFromCarry(ServerPlayer serverPlayer) {
        if (!workflowRuntime(serverPlayer).kitWorkflow().activation().isActive()) {
            return;
        }
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            workflowRuntime(serverPlayer).recordOutcome(outcome);
            return outcome;
        };
        SlotWorkspaceCommandService.reapplyActiveKit(
                workflowRuntime(serverPlayer),
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor
        );
    }

    void deactivateKit() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.deactivateKit(
                workflowRuntime(serverPlayer)
        ));
    }

    void performUndo() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.performUndo(
                workflowRuntime(serverPlayer)
        ));
    }

    void performRedo() {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.performRedo(
                workflowRuntime(serverPlayer)
        ));
    }

    void renameCluster(String clusterId, String label) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.relabelCluster(
                workflowRuntime(serverPlayer),
                clusterId,
                label
        ));
    }

    void renameKit(String kitId, String newName) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.renameKit(
                workflowRuntime(serverPlayer),
                kitId,
                newName
        ));
    }

    void duplicateKit(String kitId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.duplicateKit(
                workflowRuntime(serverPlayer),
                kitId
        ));
    }

    void deleteKit(String kitId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.deleteKit(
                workflowRuntime(serverPlayer),
                kitId
        ));
    }

    void switchKitPage(Integer direction) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            workflowRuntime(serverPlayer).recordOutcome(outcome);
            return outcome;
        };
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.switchKitPage(
                workflowRuntime(serverPlayer),
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor,
                direction == null ? 1 : direction
        ));
    }

    void addKitPage(String kitId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.addKitPage(
                workflowRuntime(serverPlayer),
                kitId
        ));
    }

    void removeKitPage(String kitId, Integer pageIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.removeKitPage(
                workflowRuntime(serverPlayer),
                kitId,
                pageIndex == null ? -1 : pageIndex
        ));
    }

    /**
     * Set or clear a kit-scoped desired count for an explicit kitId
     * (which may not be the active kit). Replaces the legacy
     * addKitBring/removeKitBring pair — count=1 seeds the standing
     * order, count=0 clears it.
     */
    void setKitScopedDesiredCount(
            String kitId,
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer countBoxed
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (kitId == null || kitId.isBlank()) {
            reject("invalid_kit_id");
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        int count = countBoxed == null ? 0 : countBoxed;
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        if (runtime.kitWorkflow().kit(kitId) == null) {
            reject("unknown_kit");
            return;
        }
        boolean changed = runtime.desiredCountWorkflow().setForKit(kitId, identity, count);
        if (changed) {
            status = count > 0 ? "kit_desired_set_" + count : "kit_desired_cleared";
            diagnostics = "";
        } else {
            status = "noop";
            diagnostics = "";
        }
        broadcast(serverPlayer);
    }

    void swapKitSlots(String kitId, Integer pageIndex, Integer fromIndex, Integer toIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.swapKitSlots(
                workflowRuntime(serverPlayer),
                kitId,
                pageIndex == null ? -1 : pageIndex,
                fromIndex == null ? -1 : fromIndex,
                toIndex == null ? -1 : toIndex
        ));
    }

    void setKitSlotIdentity(
            String kitId,
            Integer pageIndex,
            Integer slotIndex,
            String itemId,
            String comparisonMode,
            String componentFingerprint
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        refreshServerView(serverPlayer);
        // Belt sync only fires when the edit lands on the active kit's active page, and
        // only when we can resolve an inventory host to execute live mutations through.
        // On host-resolution failure the command falls back to definition-only update.
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = host == null
                ? null
                : request -> {
                    InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                            host,
                            serverPlayer,
                            request,
                            ProtectionPolicy.allowAll()
                    );
                    workflowRuntime(serverPlayer).recordOutcome(outcome);
                    return outcome;
                };
        applyOutcome(serverPlayer, SlotWorkspaceCommandService.setKitSlotIdentity(
                workflowRuntime(serverPlayer),
                authority,
                ProtectionPolicy.allowAll(),
                KIT_IDENTITY_RESOLVER,
                actionExecutor,
                kitId,
                pageIndex == null ? -1 : pageIndex,
                slotIndex == null ? -1 : slotIndex,
                itemId,
                comparisonMode,
                componentFingerprint
        ));
    }

    void returnHotbarToHome(Integer hotbarIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int index = hotbarIndex == null ? -1 : hotbarIndex;
        if (index < 0 || index >= 9) {
            reject("invalid_hotbar_slot");
            return;
        }
        refreshServerView(serverPlayer);
        SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(index);
        if (!slot.occupied()) {
            status = "nothing_to_return";
            diagnostics = "hotbar slot " + (index + 1) + " is empty";
            broadcast(serverPlayer);
            return;
        }
        ItemIdentity identity = ItemIdentityMatcher.create(slot.displayStack());
        boolean homed = workflowRuntime(serverPlayer).snapshot().visualHomeMap().assignment(identity) != null;
        ItemStack hotbarBefore = HotbarSlotReverser.peekSlot(serverPlayer, index);
        ClaimedChest depositTarget = resolveProximateLinkedChestForIdentity(
                serverPlayer, identity, slot.displayStack());
        if (depositTarget != null) {
            DepositExecutor.SingleStackOutcome outcome = DepositExecutor.depositSingleStack(
                    serverPlayer,
                    BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                    index,
                    depositTarget
            );
            // Bumps affinity + undo entry only on success — same shape as
            // depositCarriedToChest. Undo's chest-pull will restore the
            // identity into carry; the hotbar slot stays empty (the
            // player can re-assign via the existing hotbar-undo flow).
            if (outcome.success() && outcome.record() != null) {
                workflowRuntime(serverPlayer).chestClaimWorkflow().recordDeposit(
                        outcome.record().storageId(),
                        outcome.record().identity(),
                        outcome.record().count(),
                        serverPlayer.serverLevel().getGameTime());
                recordChestTransferUndo(
                        serverPlayer, outcome.record().storageId(),
                        outcome.record().identity(), outcome.record().count(),
                        ChestTransferDirection.DEPOSIT);
            }
            applyChestDepositOutcome(serverPlayer, outcome, depositTarget);
            return;
        }
        WorkspaceTransferExecution execution = executeTransfer(
                serverPlayer,
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, index),
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                "slot_workspace.ldlib.return_hotbar_to_home"
        );
        if (execution.appliedCompletely()) {
            status = homed ? "returned_to_home" : "returned_unhomed";
            diagnostics = homed ? "returned to its home" : "returned to inventory";
            ItemStack hotbarAfter = HotbarSlotReverser.peekSlot(serverPlayer, index);
            recordHotbarSlotUndo(serverPlayer, index, hotbarBefore, hotbarAfter,
                    "return hotbar " + (index + 1) + " to inventory");
        } else {
            String feedbackDiagnostics = execution.feedback().diagnostics();
            boolean fullDestination = "destination_full_or_incompatible".equals(feedbackDiagnostics);
            status = fullDestination ? "no_free_main_slot" : execution.feedback().status();
            diagnostics = fullDestination ? "no free main inventory slot" : feedbackDiagnostics;
        }
        broadcast(serverPlayer);
    }

    void assignHomeToFreeHotbar(String itemId, String comparisonMode, String componentFingerprint) {
        assignHomeToFreeHotbar(itemId, comparisonMode, componentFingerprint, false);
    }

    void assignHomeToHotbarOnly(String itemId, String comparisonMode, String componentFingerprint) {
        assignHomeToFreeHotbar(itemId, comparisonMode, componentFingerprint, true);
    }

    private void assignHomeToFreeHotbar(String itemId, String comparisonMode, String componentFingerprint, boolean suppressChestPreference) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        refreshServerView(serverPlayer);
        if (!suppressChestPreference
                && tryDepositIdentityToLinkedChest(serverPlayer, identity, DepositQuantity.STACK)
                        == DepositAttempt.DISPATCHED) {
            return;
        }
        // Prefer a partial-stack hotbar slot of the same identity over a free
        // slot. Without this, "shift+click steak with 1 already on the belt"
        // would land the rest in a new free slot instead of filling the
        // existing stack — visibly the same item appears in two places.
        // The downstream LoadoutApplyService Pass 3 fill consolidates
        // remaining carried stocks into whichever slot we pick, so picking
        // the partial slot keeps the steak in the player's chosen position.
        int targetHotbarIndex = -1;
        for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
            if (!slot.occupied()) {
                continue;
            }
            ItemStack hot = slot.displayStack();
            if (hot == null || hot.isEmpty()) {
                continue;
            }
            int max = hot.getMaxStackSize();
            if (max <= 1 || slot.count() >= max) {
                continue;
            }
            if (ItemIdentityMatcher.matchesMovable(hot, identity)) {
                targetHotbarIndex = slot.hotbarIndex();
                break;
            }
        }
        if (targetHotbarIndex < 0) {
            for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
                if (!slot.occupied()) {
                    targetHotbarIndex = slot.hotbarIndex();
                    break;
                }
            }
        }
        if (targetHotbarIndex < 0) {
            status = "no_free_hotbar_slot";
            diagnostics = "all hotbar slots are occupied";
            broadcast(serverPlayer);
            return;
        }
        // Route through the same identity-based hotbar assignment that digit-press
        // uses so items living in backpacks / other non-player carried sources can
        // reach the hotbar — CarriedSourceAccess.findIdentity walks every carried
        // source so the target doesn't need to live in PLAYER_MAIN.
        assignIdentityToHotbarIndex(serverPlayer, identity, targetHotbarIndex);
    }

    private void assignIdentityToHotbarIndex(ServerPlayer serverPlayer, ItemIdentity identity, int hotbarIndex) {
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> located = carried.findIdentity(serverPlayer, identity);
        if (located.isEmpty()) {
            status = "nothing_to_assign";
            diagnostics = "identity not found in any carried source";
            broadcast(serverPlayer);
            return;
        }
        ItemStack hotbarBefore = HotbarSlotReverser.peekSlot(serverPlayer, hotbarIndex);
        String sourceId = located.get().sourceId();
        int slotIndex = located.get().slotIndex();
        if (BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)
                || BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)) {
            WorkspaceTransferExecution execution = executeTransfer(
                    serverPlayer,
                    new InventoryActionTarget.SourceSlotTarget(sourceId, slotIndex),
                    new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, hotbarIndex),
                    "slot_workspace.ldlib.assign_identity_to_hotbar"
            );
            if (execution.appliedCompletely()) {
                status = "assigned_to_hotbar_" + (hotbarIndex + 1);
                diagnostics = "moved to hotbar " + (hotbarIndex + 1);
                ItemStack hotbarAfter = HotbarSlotReverser.peekSlot(serverPlayer, hotbarIndex);
                recordHotbarSlotUndo(serverPlayer, hotbarIndex, hotbarBefore, hotbarAfter,
                        "assign " + identity.itemId() + " to hotbar " + (hotbarIndex + 1));
            } else {
                status = execution.feedback().status();
                diagnostics = execution.feedback().diagnostics();
            }
            broadcast(serverPlayer);
            return;
        }
        applyLoadoutSingleTarget(serverPlayer, hotbarIndex, identity);
        ItemStack hotbarAfter = HotbarSlotReverser.peekSlot(serverPlayer, hotbarIndex);
        recordHotbarSlotUndo(serverPlayer, hotbarIndex, hotbarBefore, hotbarAfter,
                "assign " + identity.itemId() + " to hotbar " + (hotbarIndex + 1));
    }

    // Identity-based hotbar slot assignment. Unlike the slot-index-based
    // transfer path (TARGET_MAIN_SLOT + firstSlotIndex), this resolves the
    // source on the server by scanning every source tagged as CARRIED — so
    // an item that only lives in a Sophisticated Backpack is still reachable
    // from digit-press / drag-to-hotbar / click-hotbar-while-selected.
    void assignIdentityToHotbarSlot(
            String itemId,
            String comparisonMode,
            String componentFingerprint,
            Integer hotbarIndexBoxed
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        int hotbarIndex = hotbarIndexBoxed == null ? -1 : hotbarIndexBoxed;
        if (hotbarIndex < 0 || hotbarIndex >= 9) {
            reject("invalid_hotbar_slot");
            return;
        }

        refreshServerView(serverPlayer);
        // If the source is a player slot (main or hotbar) the factory builds a single ASSIGN
        // request and the in-place swap path handles displacement. If it lives in a backpack
        // (or any other non-player carried source) the in-place swap path rejects with
        // `assign_requires_player_bound_targets`, so assignIdentityToHotbarIndex routes
        // through LoadoutApplyService's staging flow — TRANSFER source→hotbar, with an
        // automatic staging step that moves any current hotbar occupant into a free main slot.
        assignIdentityToHotbarIndex(serverPlayer, identity, hotbarIndex);
    }

    private void applyLoadoutSingleTarget(ServerPlayer serverPlayer, int hotbarIndex, ItemIdentity identity) {
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            reject("host_resolution_failed");
            return;
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        var entry = new dev.imagio.slot.workflow.domain.QuickAccessLoadoutEntry(
                new dev.imagio.slot.workflow.domain.LoadoutTarget.QuickAccessLaneTarget(
                        BuiltinInventoryIds.QUICK_ACCESS_LANE_0, hotbarIndex),
                identity
        );
        var loadout = new dev.imagio.slot.workflow.domain.QuickAccessLoadoutDefinition(
                "assign_identity_" + hotbarIndex,
                identity.itemId() + " -> hotbar " + (hotbarIndex + 1),
                java.util.Set.of(entry)
        );
        var plan = dev.imagio.slot.workflow.domain.LoadoutApplyService.plan(
                loadout,
                authority,
                ProtectionPolicy.allowAll(),
                dev.imagio.slot.inventory.action.InventoryActionMode.EXECUTE,
                e -> ItemIdentityMatcher.create(e.stack())
        );
        java.util.function.Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor = request -> {
            InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                    host,
                    serverPlayer,
                    request,
                    ProtectionPolicy.allowAll()
            );
            workflowRuntime(serverPlayer).recordOutcome(outcome);
            return outcome;
        };
        var result = new dev.imagio.slot.workflow.domain.LoadoutApplyExecutor(actionExecutor).execute(plan);
        if (result.satisfiedTargets().stream().anyMatch(t ->
                t instanceof dev.imagio.slot.workflow.domain.LoadoutTarget.QuickAccessLaneTarget q
                        && q.slotIndex() == hotbarIndex)) {
            status = "assigned_to_hotbar_" + (hotbarIndex + 1);
            diagnostics = "moved to hotbar " + (hotbarIndex + 1);
        } else {
            status = "rejected";
            diagnostics = result.diagnostics().isEmpty()
                    ? "assign failed"
                    : String.join(",", result.diagnostics());
        }
        broadcast(serverPlayer);
    }

    void depositHomeToLinkedChest(String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        refreshServerView(serverPlayer);
        switch (tryDepositIdentityToLinkedChest(serverPlayer, identity, DepositQuantity.STACK)) {
            case DISPATCHED -> { /* applyChestDepositOutcome already broadcast */ }
            case SOURCE_MISSING -> {
                status = "rejected";
                diagnostics = "nothing_to_deposit";
                broadcast(serverPlayer);
            }
            case NO_CHEST -> {
                status = "rejected";
                diagnostics = "no_linked_proximate_chest_with_room";
                broadcast(serverPlayer);
            }
        }
    }

    void depositCarriedToChest(String itemId, String comparisonMode, String componentFingerprint, String storageIdRaw) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        ChestProximityResult resolved = resolveProximateChest(serverPlayer, storageIdRaw);
        if (resolved.outcome != null) {
            applyChestDepositRejection(serverPlayer, resolved.outcome);
            return;
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> sourceLocation = carried.findIdentity(serverPlayer, identity);
        if (sourceLocation.isEmpty()) {
            status = "rejected";
            diagnostics = "nothing_to_deposit";
            broadcast(serverPlayer);
            return;
        }
        DepositExecutor.SingleStackOutcome outcome = DepositExecutor.depositSingleStack(
                serverPlayer,
                sourceLocation.get().sourceId(),
                sourceLocation.get().slotIndex(),
                resolved.chest);
        // Bump affinity so future routing prefers this chest for the
        // identity. The omnibus deposit() method already does this loop
        // for its multi-stack outcomes; the per-chip path was missing
        // it, leaving drag-onto-chip deposits invisible to the
        // affinity learner.
        if (outcome.success() && outcome.record() != null) {
            workflowRuntime(serverPlayer).chestClaimWorkflow().recordDeposit(
                    outcome.record().storageId(),
                    outcome.record().identity(),
                    outcome.record().count(),
                    serverPlayer.serverLevel().getGameTime());
            recordChestTransferUndo(
                    serverPlayer, outcome.record().storageId(),
                    outcome.record().identity(), outcome.record().count(),
                    ChestTransferDirection.DEPOSIT);
        }
        applyChestDepositOutcome(serverPlayer, outcome, resolved.chest);
    }

    void depositHotbarToChest(Integer hotbarIndex, String storageIdRaw) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int index = hotbarIndex == null ? -1 : hotbarIndex;
        if (index < 0 || index >= 9) {
            reject("invalid_hotbar_slot");
            return;
        }
        ChestProximityResult resolved = resolveProximateChest(serverPlayer, storageIdRaw);
        if (resolved.outcome != null) {
            applyChestDepositRejection(serverPlayer, resolved.outcome);
            return;
        }
        DepositExecutor.SingleStackOutcome outcome = DepositExecutor.depositSingleStack(
                serverPlayer,
                BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                index,
                resolved.chest
        );
        if (outcome.success() && outcome.record() != null) {
            workflowRuntime(serverPlayer).chestClaimWorkflow().recordDeposit(
                    outcome.record().storageId(),
                    outcome.record().identity(),
                    outcome.record().count(),
                    serverPlayer.serverLevel().getGameTime());
            recordChestTransferUndo(
                    serverPlayer, outcome.record().storageId(),
                    outcome.record().identity(), outcome.record().count(),
                    ChestTransferDirection.DEPOSIT);
        }
        applyChestDepositOutcome(serverPlayer, outcome, resolved.chest);
    }

    void takeOneFromChest(String storageIdRaw, Integer chestSlotIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int slotIndex = chestSlotIndex == null ? -1 : chestSlotIndex;
        if (slotIndex < 0) {
            reject("invalid_chest_slot");
            return;
        }
        ChestProximityResult resolved = resolveProximateChest(serverPlayer, storageIdRaw);
        if (resolved.outcome != null) {
            applyChestDepositRejection(serverPlayer, resolved.outcome);
            return;
        }
        ItemIdentity preTakeIdentity = peekChestSlotIdentity(serverPlayer, resolved.chest, slotIndex);
        TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeSingleItem(
                serverPlayer, resolved.chest, slotIndex);
        if (!outcome.tookAnything()) {
            status = "nothing_to_take";
            diagnostics = "";
        } else {
            status = "took_one";
            diagnostics = "moved=" + outcome.moved();
        }
        if (outcome.tookAnything()) {
            recordChestTransferUndo(
                    serverPlayer, resolved.chest.storageId(), preTakeIdentity, outcome.moved(),
                    ChestTransferDirection.TAKE);
            reapplyActiveKitFromCarry(serverPlayer);
        }
        broadcast(serverPlayer);
    }

    void depositOneHomeToLinkedChest(String itemId, String comparisonMode, String componentFingerprint) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        refreshServerView(serverPlayer);
        switch (tryDepositIdentityToLinkedChest(serverPlayer, identity, DepositQuantity.ITEM)) {
            case DISPATCHED -> { /* applyChestDepositOutcome already broadcast */ }
            case SOURCE_MISSING -> {
                status = "rejected";
                diagnostics = "nothing_to_deposit";
                broadcast(serverPlayer);
            }
            case NO_CHEST -> {
                status = "rejected";
                diagnostics = "no_linked_proximate_chest_with_room";
                broadcast(serverPlayer);
            }
        }
    }

    /**
     * Set the active-scope desired count "I want N of this carried at all
     * times." Resolves scope server-side: kit-scoped if a kit is active,
     * else player-global. The client doesn't pick the scope so right-click
     * "Set desired count…" always edits whatever scope the pip is showing.
     * Count {@code 0} clears the entry.
     */
    void setPlayerDesiredCount(String itemId, String comparisonMode, String componentFingerprint, Integer countBoxed) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        int count = countBoxed == null ? 0 : countBoxed;
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        DesiredCountWorkflowDomainService desired = runtime.desiredCountWorkflow();
        String activeKit = desired.activeScope(runtime.snapshot().kitMap());
        boolean changed = activeKit != null
                ? desired.setForKit(activeKit, identity, count)
                : desired.setPlayer(identity, count);
        if (changed) {
            String scopeTag = activeKit != null ? "kit" : "global";
            status = count > 0 ? "desired_count_" + scopeTag + "_" + count : "desired_count_cleared";
            diagnostics = "";
        } else {
            status = "noop";
            diagnostics = "";
        }
        broadcast(serverPlayer);
    }

    /**
     * Bump the active-scope desired count by {@code delta}. Wired to
     * ctrl+scrollwheel on atlas cards. Routes to kit-scope when a kit is
     * active so the same gesture edits whatever the player sees on the
     * card. Negative deltas decrement; clamps to zero.
     */
    void adjustPlayerDesiredCount(String itemId, String comparisonMode, String componentFingerprint, Integer deltaBoxed) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        int delta = deltaBoxed == null ? 0 : deltaBoxed;
        if (delta == 0) {
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        DesiredCountWorkflowDomainService desired = runtime.desiredCountWorkflow();
        String activeKit = desired.activeScope(runtime.snapshot().kitMap());
        boolean changed = activeKit != null
                ? desired.adjustForKit(activeKit, identity, delta)
                : desired.adjustPlayer(identity, delta);
        if (changed) {
            int now = activeKit != null
                    ? desired.getForKit(activeKit, identity)
                    : desired.getPlayer(identity);
            String scopeTag = activeKit != null ? "kit" : "global";
            status = "desired_count_" + scopeTag + "_" + now;
            diagnostics = "";
        }
        broadcast(serverPlayer);
    }

    /**
     * Cursor-drop: move {@code count} items from the cursor's recorded
     * origin slot to the target hotbar slot. Uses TRANSFER + INSERT_ONLY
     * + EXACT_COUNT with a count-clamped stack so the executor honors the
     * cursor's chosen amount instead of dumping the source's whole stack.
     * Failure to insert (target occupied with a different item, etc.) is
     * surfaced as a status — drag remains the swap path; the cursor only
     * moves into compatible slots.
     */
    /**
     * Take one item of {@code identity} from the highest-affinity proximate
     * chest that contains it. Replaces slot-precise client-side take so
     * the player can act on a ghost card without knowing which chest /
     * slot the matching item happens to live in.
     */
    void takeOneByIdentity(String itemId, String comparisonMode, String componentFingerprint) {
        takeByIdentity(itemId, comparisonMode, componentFingerprint, 1);
    }

    void takeStackByIdentity(String itemId, String comparisonMode, String componentFingerprint) {
        takeByIdentity(itemId, comparisonMode, componentFingerprint, Integer.MAX_VALUE);
    }

    private void takeByIdentity(String itemId, String comparisonMode, String componentFingerprint, int maxCount) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ItemIdentity identity = resolveIdentity(itemId, comparisonMode, componentFingerprint);
        if (identity == null) {
            reject("invalid_identity");
            return;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
        if (proximate.isEmpty()) {
            status = "rejected";
            diagnostics = "no_proximate_chest";
            broadcast(serverPlayer);
            return;
        }
        // Walk proximate chests in affinity-score order; first one with a
        // matching stack wins. If none have it, walk all proximate chests
        // (a chest may carry the item without the player having deposited
        // it yet — affinity is monotonically learned, not authoritative).
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap();
        java.util.List<ClaimedChest> ranked = DepositPlanner.rankProximateChestsForTake(
                identity, claimedChestMap, affinityMap, proximate);
        boolean foundMatchButCouldNotInsert = false;
        for (ClaimedChest chest : ranked) {
            TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeByIdentity(
                    serverPlayer, chest, identity, maxCount,
                    maxCount == 1 ? "take-one-by-identity" : "take-stack-by-identity");
            if (outcome.tookAnything()) {
                status = maxCount == 1 ? "took_one" : "took_stack";
                diagnostics = "moved=" + outcome.moved();
                recordChestTransferUndo(
                        serverPlayer, chest.storageId(), identity, outcome.moved(),
                        ChestTransferDirection.TAKE);
                reapplyActiveKitFromCarry(serverPlayer);
                broadcast(serverPlayer);
                return;
            }
            if (outcome.partial()) {
                // Chest had the matching item but inventory rejected the
                // insert — almost always a full-carry problem.
                foundMatchButCouldNotInsert = true;
            }
        }
        if (foundMatchButCouldNotInsert) {
            status = "rejected";
            diagnostics = "carry_full";
        } else {
            status = "nothing_to_take";
            diagnostics = "no_matching_proximate_chest";
        }
        broadcast(serverPlayer);
    }

    void takeFromChest(String storageIdRaw, Integer chestSlotIndex) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int slotIndex = chestSlotIndex == null ? -1 : chestSlotIndex;
        if (slotIndex < 0) {
            reject("invalid_chest_slot");
            return;
        }
        ChestProximityResult resolved = resolveProximateChest(serverPlayer, storageIdRaw);
        if (resolved.outcome != null) {
            applyChestDepositRejection(serverPlayer, resolved.outcome);
            return;
        }
        ItemIdentity preTakeIdentity = peekChestSlotIdentity(serverPlayer, resolved.chest, slotIndex);
        TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeSingleStack(
                serverPlayer, resolved.chest, slotIndex);
        if (!outcome.tookAnything()) {
            status = "nothing_to_take";
            diagnostics = "";
        } else if (outcome.partial()) {
            status = "took_partial";
            diagnostics = "moved=" + outcome.moved() + " leftover=" + outcome.leftover();
        } else {
            status = "took_stack";
            diagnostics = "moved=" + outcome.moved();
        }
        if (outcome.tookAnything()) {
            recordChestTransferUndo(
                    serverPlayer, resolved.chest.storageId(), preTakeIdentity, outcome.moved(),
                    ChestTransferDirection.TAKE);
            reapplyActiveKitFromCarry(serverPlayer);
        }
        broadcast(serverPlayer);
    }

    private ChestProximityResult resolveProximateChest(ServerPlayer serverPlayer, String storageIdRaw) {
        if (storageIdRaw == null || storageIdRaw.isBlank()) {
            return ChestProximityResult.failed("invalid_chest_storage_id");
        }
        UUID storageId;
        try {
            storageId = UUID.fromString(storageIdRaw);
        } catch (IllegalArgumentException ignored) {
            return ChestProximityResult.failed("invalid_chest_storage_id");
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChest chest = runtime.chestClaimWorkflow().claimedChestMap().chest(storageId);
        if (chest == null) {
            return ChestProximityResult.failed("unknown_chest_tile");
        }
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(
                serverPlayer, runtime.chestClaimWorkflow().claimedChestMap());
        if (!proximate.contains(storageIdRaw)) {
            return ChestProximityResult.failed("not_proximate");
        }
        return new ChestProximityResult(chest, null);
    }

    private void applyChestDepositRejection(ServerPlayer serverPlayer, String reason) {
        status = "rejected";
        diagnostics = reason;
        broadcast(serverPlayer);
    }

    /**
     * Push a single undo entry for a chest ↔ carry transfer.
     * {@code direction} is the action that just succeeded; the undo
     * closure runs the opposite direction, the redo closure replays the
     * action. Captured {@code serverPlayer} is safe — the runtime (and
     * its undo stack) is dropped on player disconnect.
     */
    private void recordChestTransferUndo(
            ServerPlayer serverPlayer,
            UUID storageId,
            ItemIdentity identity,
            int count,
            ChestTransferDirection direction
    ) {
        if (serverPlayer == null || storageId == null || identity == null
                || count <= 0 || direction == null) {
            return;
        }
        String verb = direction == ChestTransferDirection.DEPOSIT ? "deposit" : "take";
        String label = count == 1
                ? verb + " " + identity.itemId()
                : verb + " " + identity.itemId() + " ×" + count;
        workflowRuntime(serverPlayer).undoStack().record(
                label,
                ctx -> {
                    if (direction == ChestTransferDirection.DEPOSIT) {
                        DepositReverser.pullFromChestToCarry(serverPlayer, storageId, identity, count);
                    } else {
                        DepositReverser.pushFromCarryToChest(serverPlayer, storageId, identity, count);
                    }
                },
                ctx -> {
                    if (direction == ChestTransferDirection.DEPOSIT) {
                        DepositReverser.pushFromCarryToChest(serverPlayer, storageId, identity, count);
                    } else {
                        DepositReverser.pullFromChestToCarry(serverPlayer, storageId, identity, count);
                    }
                }
        );
    }

    private enum ChestTransferDirection { DEPOSIT, TAKE }

    /**
     * Push a single undo entry for a hotbar-slot mutation. {@code before} /
     * {@code after} are the slot's stack snapshots taken around the
     * action; the closures route through {@link HotbarSlotReverser} which
     * pushes the displaced occupant back into carry and pulls matching
     * identity from elsewhere in carry to seed the target.
     *
     * <p>Best-effort: if the player has consumed the item between the
     * action and the undo, the slot ends up with whatever is still in
     * carry — same shape as the chest-transfer undo.
     */
    private void recordHotbarSlotUndo(
            ServerPlayer serverPlayer,
            int hotbarIndex,
            ItemStack before,
            ItemStack after,
            String label
    ) {
        if (serverPlayer == null || hotbarIndex < 0 || hotbarIndex >= 9) {
            return;
        }
        ItemStack beforeCopy = before == null ? ItemStack.EMPTY : before.copy();
        ItemStack afterCopy = after == null ? ItemStack.EMPTY : after.copy();
        if (ItemStack.matches(beforeCopy, afterCopy)) {
            return; // No-op action — nothing to undo.
        }
        workflowRuntime(serverPlayer).undoStack().record(
                label,
                ctx -> HotbarSlotReverser.restoreSlot(serverPlayer, hotbarIndex, beforeCopy),
                ctx -> HotbarSlotReverser.restoreSlot(serverPlayer, hotbarIndex, afterCopy)
        );
    }

    /**
     * Read the identity at {@code slotIndex} in {@code chest} so a slot-precise
     * take can record an undo entry that knows what to push back. Returns
     * {@code null} when the slot is empty / the chest isn't accessible —
     * the caller skips undo recording in that case.
     */
    private static ItemIdentity peekChestSlotIdentity(ServerPlayer serverPlayer, ClaimedChest chest, int slotIndex) {
        if (serverPlayer == null || chest == null || slotIndex < 0) {
            return null;
        }
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) {
            return null;
        }
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        if (!world.isAccessible(server, target)) {
            return null;
        }
        for (WorldStorageAccess.SlotContent entry : world.enumerate(server, target)) {
            if (entry.slotIndex() != slotIndex) {
                continue;
            }
            ItemStack stack = entry.stack();
            if (stack == null || stack.isEmpty()) {
                return null;
            }
            return ItemIdentityMatcher.create(stack);
        }
        return null;
    }

    private void applyChestDepositOutcome(
            ServerPlayer serverPlayer,
            DepositExecutor.SingleStackOutcome outcome,
            ClaimedChest chest
    ) {
        if (outcome.success()) {
            status = "deposited_stack";
            diagnostics = "deposited to " + chestLabel(chest);
        } else {
            status = "rejected";
            diagnostics = outcome.diagnostic();
        }
        broadcast(serverPlayer);
    }

    /** Quantity variant for {@link #tryDepositIdentityToLinkedChest}: whole stack vs single item. */
    private enum DepositQuantity { STACK, ITEM }

    /** Outcome of a deposit attempt. DISPATCHED means the outcome was already applied + broadcast. */
    private enum DepositAttempt { DISPATCHED, SOURCE_MISSING, NO_CHEST }

    /**
     * Find the first carried slot matching {@code identity}, resolve a
     * proximate linked chest that accepts it, and deposit via
     * {@link DepositExecutor}. Applies the outcome (status + broadcast) only
     * when {@link DepositAttempt#DISPATCHED} is returned — callers translate
     * SOURCE_MISSING / NO_CHEST into their own rejection diagnostics (the
     * two modes differ: opportunistic fall-through for assign-to-hotbar,
     * hard rejection for explicit deposit verbs).
     */
    private DepositAttempt tryDepositIdentityToLinkedChest(
            ServerPlayer serverPlayer,
            ItemIdentity identity,
            DepositQuantity quantity
    ) {
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> located = carried.findIdentity(serverPlayer, identity);
        if (located.isEmpty()) {
            return DepositAttempt.SOURCE_MISSING;
        }
        ItemStack sourceStack = carried.peek(serverPlayer,
                located.get().sourceId(), located.get().slotIndex());
        if (sourceStack.isEmpty()) {
            return DepositAttempt.SOURCE_MISSING;
        }
        ClaimedChest chest = resolveProximateLinkedChestForIdentity(serverPlayer, identity, sourceStack);
        if (chest == null) {
            return DepositAttempt.NO_CHEST;
        }
        DepositExecutor.SingleStackOutcome outcome = switch (quantity) {
            case STACK -> DepositExecutor.depositSingleStack(
                    serverPlayer, located.get().sourceId(), located.get().slotIndex(), chest);
            case ITEM -> DepositExecutor.depositSingleItem(
                    serverPlayer, located.get().sourceId(), located.get().slotIndex(), chest);
        };
        if (outcome.success() && outcome.record() != null) {
            workflowRuntime(serverPlayer).chestClaimWorkflow().recordDeposit(
                    outcome.record().storageId(),
                    outcome.record().identity(),
                    outcome.record().count(),
                    serverPlayer.serverLevel().getGameTime());
            recordChestTransferUndo(
                    serverPlayer, outcome.record().storageId(),
                    outcome.record().identity(), outcome.record().count(),
                    ChestTransferDirection.DEPOSIT);
        }
        applyChestDepositOutcome(serverPlayer, outcome, chest);
        return DepositAttempt.DISPATCHED;
    }

    private static String chestLabel(ClaimedChest chest) {
        if (chest == null) {
            return "chest";
        }
        if (chest.label() != null && !chest.label().isBlank()) {
            return chest.label();
        }
        String hex = chest.storageId().toString();
        int dash = hex.indexOf('-');
        String shortId = dash < 0 ? hex : hex.substring(0, dash);
        if (shortId.length() > 4) {
            shortId = shortId.substring(shortId.length() - 4);
        }
        return "Chest #" + shortId;
    }

    /**
     * Pick a proximate claimed chest for {@code identity} for the
     * per-card "deposit home to linked chest" actions. Routes through
     * {@link DepositPlanner#rankChestsForIdentity} so direct affinity,
     * facet-similar, and presence tiers match the deposit button —
     * filtering by simulated-insert capacity so a chest that can't fit
     * the stack is skipped over.
     */
    private ClaimedChest resolveProximateLinkedChestForIdentity(
            ServerPlayer serverPlayer,
            ItemIdentity identity,
            ItemStack sourceStack
    ) {
        if (serverPlayer == null || identity == null || sourceStack == null || sourceStack.isEmpty()) {
            return null;
        }
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
        if (proximate.isEmpty()) {
            return null;
        }
        MinecraftServer server = serverPlayer.getServer();
        if (server == null || !StorageAccessRegistry.isInstalled()) {
            return null;
        }
        long tick = serverPlayer.serverLevel().getGameTime();
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap().decayed(tick);
        java.util.Map<UUID, java.util.Set<ItemIdentity>> contentsCache = new java.util.HashMap<>();
        java.util.List<UUID> ranked = DepositPlanner.rankChestsForIdentity(
                identity, claimedChestMap, affinityMap, proximate,
                this::descriptorForIdentity,
                chestContentsLookup(server, claimedChestMap, contentsCache));
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        for (UUID storageId : ranked) {
            ClaimedChest chest = claimedChestMap.chest(storageId);
            if (chest == null) {
                continue;
            }
            WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
            if (!world.isAccessible(server, target)) {
                continue;
            }
            ItemStack simulation = world.insert(server, target, sourceStack.copy(), true);
            if (simulation.isEmpty()) {
                return chest;
            }
        }
        return null;
    }

    private static final class ChestProximityResult {
        private final ClaimedChest chest;
        private final String outcome;

        private ChestProximityResult(ClaimedChest chest, String outcome) {
            this.chest = chest;
            this.outcome = outcome;
        }

        private static ChestProximityResult failed(String reason) {
            return new ChestProximityResult(null, reason);
        }
    }

    private static ItemIdentity resolveIdentity(String itemId, String comparisonMode, String componentFingerprint) {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        return new SlotWorkspaceViewModel.IdentityRef(itemId, comparisonMode, componentFingerprint).toIdentity();
    }

    private static final Function<InventoryEntrySnapshot, ItemIdentity> KIT_IDENTITY_RESOLVER =
            entry -> entry == null ? null : ItemIdentityMatcher.create(entry.stack());

    void moveHotbarToAtlas(Integer hotbarIndex, String islandId, Integer ordinal) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int resolvedHotbarIndex = hotbarIndex == null ? -1 : hotbarIndex;
        if (resolvedHotbarIndex < 0 || resolvedHotbarIndex >= 9 || islandId == null || islandId.isBlank()) {
            reject("invalid_hotbar_drop");
            return;
        }
        SlotWorkspaceViewModel.HotbarSlot hotbarSlot = visibleHotbarSlot(serverPlayer, resolvedHotbarIndex);
        if (hotbarSlot == null) {
            reject("selected_hotbar_not_visible");
            return;
        }
        if (!SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)
                && viewModel.island(islandId) == null) {
            reject("unknown_island");
            return;
        }
        ItemIdentity identity = ItemIdentityMatcher.create(hotbarSlot.displayStack());
        ItemStack hotbarBefore = HotbarSlotReverser.peekSlot(serverPlayer, resolvedHotbarIndex);
        VisualHomeAssignment homeBefore = workflowRuntime(serverPlayer)
                .snapshot().visualHomeMap().assignment(identity);
        WorkspaceTransferExecution execution = executeTransfer(
                serverPlayer,
                new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, resolvedHotbarIndex),
                new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN),
                "slot_workspace.ldlib.drag.hotbar_to_atlas"
        );
        if (!execution.appliedCompletely()) {
            status = execution.feedback().status();
            diagnostics = execution.feedback().diagnostics();
            broadcast(serverPlayer);
            return;
        }
        refreshServerView(serverPlayer);
        WorkspaceCommandOutcome dropOutcome = SlotWorkspaceCommandService.applyHomeDrop(
                workflowRuntime(serverPlayer),
                viewModel,
                learnedRules,
                IslandSignalExtractor::extract,
                identity,
                islandId,
                ordinal,
                "slot_workspace.ldlib.drag.hotbar_home"
        );
        if (dropOutcome.success()) {
            // Capture the post-action home assignment so redo replays it.
            // Combined undo restores both the hotbar slot and the home in
            // one entry — the transfer + drop are a single user gesture
            // and should reverse together. Learned-rule recording inside
            // applyHomeDrop is NOT reverted (same pragmatism as affinity
            // bumps); the rule is a long-lived signal that decays via
            // future drags.
            VisualHomeAssignment homeAfter = workflowRuntime(serverPlayer)
                    .snapshot().visualHomeMap().assignment(identity);
            ItemStack hotbarAfter = HotbarSlotReverser.peekSlot(serverPlayer, resolvedHotbarIndex);
            ServerPlayer undoPlayer = serverPlayer;
            String label = SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)
                    ? "send " + identity.itemId() + " to triage"
                    : "drag hotbar " + (resolvedHotbarIndex + 1) + " to atlas";
            workflowRuntime(serverPlayer).undoStack().record(
                    label,
                    ctx -> {
                        SlotWorkspaceCommandService.restoreHomeAssignment(
                                ctx.runtime(), identity, homeBefore);
                        HotbarSlotReverser.restoreSlot(undoPlayer, resolvedHotbarIndex, hotbarBefore);
                    },
                    ctx -> {
                        HotbarSlotReverser.restoreSlot(undoPlayer, resolvedHotbarIndex, hotbarAfter);
                        SlotWorkspaceCommandService.restoreHomeAssignment(
                                ctx.runtime(), identity, homeAfter);
                    }
            );
        }
        applyOutcome(serverPlayer, dropOutcome);
    }

    private void applyOutcome(ServerPlayer serverPlayer, WorkspaceCommandOutcome outcome) {
        status = outcome.status();
        diagnostics = outcome.diagnostics();
        broadcast(serverPlayer);
    }

    private void reject(String reason) {
        status = "rejected";
        diagnostics = reason == null || reason.isBlank() ? "rejected" : reason;
        if (player instanceof ServerPlayer serverPlayer) {
            broadcast(serverPlayer);
        }
    }

    private void refreshServerView(ServerPlayer serverPlayer) {
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        InventoryAuthoritySnapshot authority = host == null
                ? InventoryAuthoritySnapshot.empty()
                : InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        String hostDiagnostics = host == null ? "host_resolution_failed" : "";
        String combinedDiagnostics = combineDiagnostics(hostDiagnostics, diagnostics);
        int selected = serverPlayer.getInventory().selected;
        WorkflowDomainRuntime runtime = workflowRuntime(serverPlayer);
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        MinecraftServer server = serverPlayer.getServer();
        Function<String, SlotWorkspaceViewModel.ChestContentsSnapshot> contentsResolver = storageId -> {
            if (server == null) {
                return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
            }
            UUID uuid;
            try {
                uuid = UUID.fromString(storageId);
            } catch (IllegalArgumentException ignored) {
                return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
            }
            ClaimedChest chest = claimedChestMap.chest(uuid);
            if (chest == null) {
                return SlotWorkspaceViewModel.ChestContentsSnapshot.empty();
            }
            return ChestContentsReader.read(server, chest);
        };
        Set<String> proximateIds = ChestProximityResolver.proximateStorageIds(serverPlayer, claimedChestMap);
        Map<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerInfo =
                SophisticatedBackpackInventoryIntegrationProvider.carriedContainerInfoByIdentity(serverPlayer);
        Function<ItemIdentity, SlotWorkspaceViewModel.CarriedContainerInfo> containerResolver =
                containerInfo.isEmpty() ? identity -> null : containerInfo::get;
        SlotWorkspaceViewModel.LootChestSource lootChestSource = resolveLootChestSource(serverPlayer, claimedChestMap);
        SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel = resolveActiveChestPanel(
                serverPlayer, runtime, claimedChestMap);
        SlotWorkspaceViewModel projected = SlotWorkspaceViewModel.project(
                authority,
                runtime.snapshot(),
                status,
                combinedDiagnostics,
                0,
                selected,
                0,
                learnedRules,
                IslandSignalExtractor::extract,
                contentsResolver,
                proximateIds,
                containerResolver,
                lootChestSource,
                searchQuery,
                serverPlayer.serverLevel().getGameTime(),
                activeChestPanel
        );
        // Pickup-time auto-home: at most one carried-but-unassigned
        // identity per refresh gets routed via its top chip suggestion
        // (or Misc when none fires). Throttled to one per refresh
        // because each {@code assignHome} writes through the persistence
        // service synchronously; without throttling, opening a screen
        // with a full inventory froze the server tick long enough that
        // the close-screen packet never landed. Subsequent refreshes
        // chip away at the remaining triage list. Re-projects against
        // the freshly-mutated snapshot so the broadcast reflects the
        // new assignment.
        if (SlotWorkspaceCommandService.autoHomeTriageItems(runtime, projected, autoHomeAttempted)) {
            activeChestPanel = resolveActiveChestPanel(serverPlayer, runtime, claimedChestMap);
            projected = SlotWorkspaceViewModel.project(
                    authority,
                    runtime.snapshot(),
                    status,
                    combinedDiagnostics,
                    0,
                    selected,
                    0,
                    learnedRules,
                    IslandSignalExtractor::extract,
                    contentsResolver,
                    proximateIds,
                    containerResolver,
                    lootChestSource,
                    searchQuery,
                    serverPlayer.serverLevel().getGameTime(),
                    activeChestPanel
            );
        }
        CompoundTag nextContent = SlotWorkspaceViewModelCodec.encode(projected, serverPlayer.registryAccess(), false);
        if (!nextContent.equals(lastContentTag)) {
            lastContentTag = nextContent.copy();
            viewModel = projected.withRevision(nextRevision++);
            lastViewTag = SlotWorkspaceViewModelCodec.encode(viewModel, serverPlayer.registryAccess());
        }
    }

    /**
     * Build the active-chest panel snapshot for the player's currently
     * open chest screen. Empty when the player isn't viewing a chest
     * (host = crafting / inventory / etc.) or when the chest's BlockPos
     * isn't tracked by the deposit observer's session table. Surfaces
     * the chest's claim state + cluster info so the sidebar's chest
     * control strip can render rename / forget / claim affordances
     * scoped to the chest the player is actively interacting with.
     */
    private static SlotWorkspaceViewModel.ActiveChestPanel resolveActiveChestPanel(
            ServerPlayer serverPlayer,
            WorkflowDomainRuntime runtime,
            ClaimedChestMap claimedChestMap
    ) {
        BlockPos pos = ChestDepositObserver.activeChestPos(serverPlayer);
        if (pos == null) {
            return SlotWorkspaceViewModel.ActiveChestPanel.empty();
        }
        ServerLevel level = serverPlayer.serverLevel();
        String dimensionId = level.dimension().location().toString();
        ChestAnchor anchor = ChestStorageAnchors.toAnchor(level, pos);
        ClaimedChest claim = anchor == null
                ? null
                : runtime.chestClaimWorkflow().chestByAnchor(anchor);
        if (claim == null) {
            return new SlotWorkspaceViewModel.ActiveChestPanel(
                    "", "", "", "", 0,
                    pos.getX(), pos.getY(), pos.getZ(),
                    dimensionId
            );
        }
        ChestClusterMap clusterMap = ChestClusterMap.derive(claimedChestMap);
        ChestClusterMap.Cluster cluster = null;
        for (ChestClusterMap.Cluster c : clusterMap.clusters()) {
            if (c.storageIds().contains(claim.storageId())) {
                cluster = c;
                break;
            }
        }
        String clusterId = cluster == null ? "" : cluster.clusterId();
        String customClusterLabel = clusterId.isEmpty()
                ? ""
                : runtime.snapshot().clusterLabels().getOrDefault(clusterId, "");
        String clusterLabel = customClusterLabel.isBlank() && cluster != null
                ? cluster.defaultLabel()
                : customClusterLabel;
        String chestLabel = claim.label() == null || claim.label().isBlank()
                ? "Chest"
                : claim.label();
        return new SlotWorkspaceViewModel.ActiveChestPanel(
                claim.storageId().toString(),
                chestLabel,
                clusterId,
                clusterLabel,
                0,
                pos.getX(), pos.getY(), pos.getZ(),
                dimensionId
        );
    }

    private static SlotWorkspaceViewModel.LootChestSource resolveLootChestSource(
            ServerPlayer serverPlayer, ClaimedChestMap claimedChestMap
    ) {
        return LootChestProximityResolver.closest(serverPlayer, claimedChestMap)
                .map(pos -> {
                    ServerLevel level = serverPlayer.serverLevel();
                    BlockEntity be = level.getBlockEntity(pos);
                    if (!(be instanceof ChestBlockEntity)) {
                        return null;
                    }
                    Container container = (Container) be;
                    int size = container.getContainerSize();
                    java.util.ArrayList<ItemStack> contents = new java.util.ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        contents.add(container.getItem(i).copy());
                    }
                    String dimensionId = level.dimension().location().toString();
                    String label = "Loot chest at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
                    return new SlotWorkspaceViewModel.LootChestSource(
                            pos.getX(), pos.getY(), pos.getZ(), dimensionId, label, contents
                    );
                })
                .orElse(null);
    }

    private InventoryHostDescriptor resolveHost(ServerPlayer serverPlayer) {
        AbstractContainerMenu menu = serverPlayer.containerMenu;
        if (menu == null) {
            return null;
        }
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                serverPlayer.getInventory(),
                Component.literal("SLOT Workspace"),
                SlotWorkspaceUiSession.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("slotWorkspace", "ldlib")
                )
        ));
    }

    private WorkflowDomainRuntime workflowRuntime(ServerPlayer serverPlayer) {
        return SlotPlayerWorkflowRuntimeService.runtime(serverPlayer);
    }

    private WorkspaceTransferExecution executeTransfer(
            ServerPlayer serverPlayer,
            InventoryActionTarget source,
            InventoryActionTarget destination,
            String origin
    ) {
        InventoryHostDescriptor host = resolveHost(serverPlayer);
        if (host == null) {
            SlotDiagnostics.workspaceTransferHostMissing(source, destination, serverPlayer.containerMenu);
            return WorkspaceTransferExecution.rejected("host_resolution_failed");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(serverPlayer, host);
        var sourceEntry = InventoryAuthorityReadService.entrySnapshot(authority, source);
        SlotDiagnostics.workspaceTransferRequested(origin, host, source, destination, sourceEntry);
        SlotWorkspaceTransferRequestFactory.BuildResult build = SlotWorkspaceTransferRequestFactory.build(
                host,
                authority,
                source,
                destination,
                origin
        );
        if (!build.dispatchable()) {
            SlotDiagnostics.workspaceTransferBuildRejected(build.diagnostics(), host, source, destination, sourceEntry);
            return WorkspaceTransferExecution.rejected(build.diagnostics());
        }

        InventoryActionRequest request = build.request();
        SlotDiagnostics.workspaceTransferRequestBuilt(host, request);
        InventoryActionOutcome outcome = InventoryActionExecutor.execute(
                host,
                serverPlayer,
                request,
                ProtectionPolicy.allowAll()
        );
        workflowRuntime(serverPlayer).recordOutcome(outcome);
        WorkspaceTransferFeedback feedback = WorkspaceTransferFeedback.interpret(request, outcome);
        SlotDiagnostics.workspaceTransferExecuted(host, request, outcome, feedback.status(), feedback.diagnostics());
        SlotDebugLog.log(
                "LDLib workspace transfer {} {} -> {} {}",
                outcome == null ? "missing_outcome" : outcome.status(),
                source.stableKey(),
                destination.stableKey(),
                feedback.diagnostics()
        );
        return new WorkspaceTransferExecution(host, request, outcome, feedback);
    }

    private SlotWorkspaceViewModel.HotbarSlot visibleHotbarSlot(ServerPlayer serverPlayer, int hotbarIndex) {
        if (hotbarIndex < 0 || hotbarIndex >= 9) {
            return null;
        }
        refreshServerView(serverPlayer);
        SlotWorkspaceViewModel.HotbarSlot slot = viewModel.hotbarSlots().get(hotbarIndex);
        return slot.occupied() ? slot : null;
    }

    private void broadcast(ServerPlayer serverPlayer) {
        refreshServerView(serverPlayer);
        if (serverPlayer.containerMenu != null) {
            serverPlayer.containerMenu.broadcastChanges();
        }
    }

    private static String combineDiagnostics(String first, String second) {
        boolean hasFirst = first != null && !first.isBlank();
        boolean hasSecond = second != null && !second.isBlank();
        if (hasFirst && hasSecond) {
            return first + "  " + second;
        }
        return hasFirst ? first : hasSecond ? second : "";
    }

    /**
     * Maps an RPC transfer-target kind (an int the client sends) to an
     * {@link InventoryActionTarget}. The set of valid kinds is protocol-bound —
     * the client must know the integer constants at compile time — so this
     * switch intentionally enumerates exactly what the UI emits over RPC, not
     * every possible carried source in the world.
     *
     * <p>Identity-based flows (drag-to-hotbar, drag-to-chest) don't come
     * through here; they go through
     * {@code CarriedSourceAccess.findIdentity} which is source-agnostic.
     *
     * <p>Adding a new transfer-target kind means: (1) add a TARGET_* constant,
     * (2) update the client-side emitter, (3) add a case here. The scope is
     * the RPC protocol, not the storage layer — bona fide registry-style
     * extensibility lives in {@link CarriedSourceAccess}.
     */
    private static InventoryActionTarget target(Integer kind, Integer index, boolean source) {
        int resolvedKind = kind == null ? -1 : kind;
        int resolvedIndex = index == null ? -1 : index;
        return switch (resolvedKind) {
            case TARGET_MAIN_SOURCE -> source
                    ? null
                    : new InventoryActionTarget.SourceTarget(BuiltinInventoryIds.PLAYER_MAIN);
            case TARGET_MAIN_SLOT -> resolvedIndex >= 0 && resolvedIndex < 27
                    ? new InventoryActionTarget.SourceSlotTarget(BuiltinInventoryIds.PLAYER_MAIN, resolvedIndex)
                    : null;
            case TARGET_HOTBAR_SLOT -> resolvedIndex >= 0 && resolvedIndex < 9
                    ? new InventoryActionTarget.QuickAccessTarget(BuiltinInventoryIds.QUICK_ACCESS_LANE_0, resolvedIndex)
                    : null;
            default -> null;
        };
    }

}
