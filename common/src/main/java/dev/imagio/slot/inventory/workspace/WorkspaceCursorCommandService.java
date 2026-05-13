package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.session.InventoryAcquisitionActivityRecorder;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.platform.SlotStackAccess;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.InventoryActivityConfidence;
import dev.imagio.slot.workflow.domain.InventoryActivityProducer;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class WorkspaceCursorCommandService {
    private WorkspaceCursorCommandService() {
    }

    public static CursorCommandOutcome pickupToCursor(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            Integer count
    ) {
        if (player == null || runtime == null) {
            return rejected("invalid_cursor_context", null);
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return rejected("no_menu", null);
        }
        if (identity == null) {
            return rejected("invalid_identity", null);
        }
        int requested = count == null || count <= 0 ? Integer.MAX_VALUE : count;
        ItemStack carriedStack = menu.getCarried();
        boolean cursorHasSameIdentity = !carriedStack.isEmpty()
                && identity.equals(ItemIdentityMatcher.create(carriedStack));
        if (!carriedStack.isEmpty() && !cursorHasSameIdentity) {
            return rejected("cursor_occupied", null);
        }
        int cursorRoom = cursorHasSameIdentity
                ? carriedStack.getMaxStackSize() - carriedStack.getCount()
                : Integer.MAX_VALUE;
        if (cursorRoom <= 0) {
            return rejected("cursor_full", null);
        }
        int amount = Math.min(requested, cursorRoom);
        if (amount <= 0) {
            return rejected("cursor_full", null);
        }

        Extraction extraction = extractFromCarry(player, identity, amount);
        if (extraction.stack().isEmpty()) {
            extraction = extractFromProximateChest(player, runtime, identity, amount);
        }
        if (extraction.stack().isEmpty()) {
            return rejected("nothing_to_pick", null);
        }
        if (cursorHasSameIdentity) {
            ItemStack merged = carriedStack.copy();
            merged.grow(extraction.stack().getCount());
            menu.setCarried(merged);
        } else {
            menu.setCarried(extraction.stack());
        }
        SlotDebugLog.log("[cursor][pickup] {} count={} from={} kind={}",
                identity.itemId(), extraction.stack().getCount(), extraction.sourceLabel(),
                extraction.origin() == null ? "?" : extraction.origin().kind());
        if (extraction.origin() != null && extraction.origin().kind() == CursorSourceKind.CHEST) {
            InventoryAcquisitionActivityRecorder.recordIdentityAcquired(
                    runtime,
                    identity,
                    extraction.stack().getCount(),
                    InventoryActivityProducer.EXTERNAL_WITHDRAWAL,
                    InventoryActivityConfidence.AUTHORITATIVE,
                    "cursor_pickup_from_chest");
        }
        return accepted(
                "picked_up",
                "moved=" + extraction.stack().getCount() + " from=" + extraction.sourceLabel(),
                extraction.origin());
    }

    public static CursorCommandOutcome cursorCancel(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            CursorOrigin origin
    ) {
        if (player == null || runtime == null) {
            return rejected("invalid_cursor_context", null);
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return rejected("no_menu", origin);
        }
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return accepted("ready", "", null);
        }
        ItemStack remaining = carried.copy();
        if (origin != null) {
            remaining = switch (origin.kind()) {
                case CARRY -> insertIntoCarry(player, remaining);
                case CHEST -> insertIntoOriginChest(player, runtime, remaining, origin.sourceId());
                case HOST_SLOT -> remaining;
            };
        }
        if (!remaining.isEmpty()) {
            remaining = smartDepositLeftover(player, runtime, remaining);
        }
        menu.setCarried(remaining);
        CursorOrigin nextOrigin = remaining.isEmpty() ? null : origin;
        SlotDebugLog.log("[cursor][cancel] kind={} remaining={}",
                origin == null ? "null" : origin.kind(), remaining.getCount());
        return accepted(
                remaining.isEmpty() ? "cursor_cancelled" : "cursor_partial_cancel",
                "remaining=" + remaining.getCount(),
                nextOrigin);
    }

    public static CursorCommandOutcome cursorSmartDeposit(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            CursorOrigin origin
    ) {
        if (player == null || runtime == null) {
            return rejected("invalid_cursor_context", null);
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return rejected("no_menu", null);
        }
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return accepted("ready", "", null);
        }
        if (origin != null && origin.kind() == CursorSourceKind.CHEST) {
            ItemStack remaining = insertIntoCarry(player, carried.copy());
            menu.setCarried(remaining);
            SlotDebugLog.log("[cursor][take-from-chest] remaining={}", remaining.getCount());
            return accepted(
                    remaining.isEmpty() ? "cursor_carried" : "cursor_partial_carry",
                    "remaining=" + remaining.getCount(),
                    remaining.isEmpty() ? null : origin);
        }
        ItemStack remaining = smartDepositLeftover(player, runtime, carried.copy());
        menu.setCarried(remaining);
        SlotDebugLog.log("[cursor][smart-deposit] remaining={}", remaining.getCount());
        return accepted(
                remaining.isEmpty() ? "cursor_deposited" : "cursor_partial_deposit",
                "remaining=" + remaining.getCount(),
                remaining.isEmpty() ? null : new CursorOrigin(CursorSourceKind.CARRY, "", -1));
    }

    public static CursorCommandOutcome dropCursorAtHotbar(
            ServerPlayer player,
            CursorOrigin origin,
            Integer hotbarIndex,
            Integer button
    ) {
        if (player == null) {
            return rejected("invalid_cursor_context", origin);
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return rejected("no_menu", origin);
        }
        int idx = hotbarIndex == null ? -1 : hotbarIndex;
        if (idx < 0 || idx >= 9) {
            return rejected("invalid_hotbar_slot", origin);
        }
        int menuSlotId = -1;
        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            if (slot.container == player.getInventory() && slot.getContainerSlot() == idx) {
                menuSlotId = i;
                break;
            }
        }
        if (menuSlotId < 0) {
            return rejected("hotbar_slot_not_in_menu", origin);
        }
        int btn = button == null ? 0 : button;
        if (btn != 0 && btn != 1) {
            return rejected("invalid_mouse_button", origin);
        }
        Slot hotbarSlot = menu.slots.get(menuSlotId);
        ItemStack carriedBefore = menu.getCarried().copy();
        ItemStack slotBefore = hotbarSlot.getItem().copy();
        if (carriedBefore.isEmpty() && slotBefore.isEmpty()) {
            return rejected("hotbar_slot_empty", null);
        }
        menu.clicked(menuSlotId, btn, ClickType.PICKUP, player);
        ItemStack carriedAfter = menu.getCarried();
        CursorOrigin nextOrigin = hotbarCursorOrigin(origin, idx, carriedBefore, carriedAfter);
        if (carriedBefore.isEmpty()) {
            return accepted(
                    carriedAfter.isEmpty() ? "ready" : "picked_up",
                    carriedAfter.isEmpty()
                            ? ""
                            : "moved=" + carriedAfter.getCount() + " from=hotbar:" + (idx + 1),
                    nextOrigin);
        }
        boolean swapped = !carriedAfter.isEmpty() && !sameStackIdentity(carriedBefore, carriedAfter);
        return accepted(
                carriedAfter.isEmpty()
                        ? "cursor_deposited"
                        : swapped ? "cursor_swapped" : "cursor_partial_deposit",
                "remaining=" + carriedAfter.getCount(),
                nextOrigin);
    }

    public static CursorCommandOutcome dropCursorIntoChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            CursorOrigin origin,
            String storageIdRaw
    ) {
        if (player == null || runtime == null) {
            return rejected("invalid_cursor_context", origin);
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return rejected("no_menu", origin);
        }
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return accepted("ready", "", null);
        }
        WorkspaceChestCommandService.ChestProximityResult resolved =
                WorkspaceChestCommandService.resolveProximateChest(player, runtime, storageIdRaw);
        if (resolved.outcome() != null) {
            return rejected(resolved.outcome(), origin);
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return rejected("server_unavailable", origin);
        }
        ItemStack remaining = carried.copy();
        ItemIdentity identity = ItemIdentityMatcher.create(remaining);
        int beforeCount = remaining.getCount();
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(resolved.chest());
        ItemStack leftover = world.insert(server, target, remaining, false);
        remaining = leftover == null ? ItemStack.EMPTY : leftover;
        int deposited = beforeCount - remaining.getCount();
        if (deposited > 0) {
            runtime.chestClaimWorkflow().recordDeposit(
                    resolved.chest().storageId(), identity, deposited,
                    player.serverLevel().getGameTime());
        }
        menu.setCarried(remaining);
        SlotDebugLog.log("[cursor][drop-chest] chest={} deposited={} remaining={}",
                resolved.chest().storageId(), deposited, remaining.getCount());
        return accepted(
                remaining.isEmpty() ? "cursor_deposited" : "cursor_partial_deposit",
                "deposited=" + deposited + " remaining=" + remaining.getCount(),
                remaining.isEmpty() ? null : origin);
    }

    public static WorkspaceCommandOutcome crossSurfaceDropOnHostSlot(
            ServerPlayer player,
            ItemIdentity identity,
            Integer hostSlotIndex
    ) {
        if (player == null) {
            return WorkspaceCommandOutcome.rejected("missing_player");
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || hostSlotIndex == null || hostSlotIndex < 0 || hostSlotIndex >= menu.slots.size()) {
            return WorkspaceCommandOutcome.rejected("invalid_host_slot");
        }
        Slot targetSlot = menu.slots.get(hostSlotIndex);
        if (targetSlot.container == player.getInventory()) {
            return WorkspaceCommandOutcome.rejected("target_slot_is_player_side");
        }
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> located = carried.findIdentity(player, identity);
        if (located.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("nothing_to_drop");
        }
        CarriedSourceAccess.CarriedLocation loc = located.get();
        ItemStack peeked = carried.peek(player, loc.sourceId(), loc.slotIndex());
        if (peeked.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("source_empty");
        }
        int maxAccept = Math.max(1, targetSlot.getMaxStackSize(peeked));
        int extractAmount = Math.min(peeked.getCount(), maxAccept);
        ItemStack extracted = carried.extract(player, loc.sourceId(), loc.slotIndex(), extractAmount, false);
        if (extracted.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("extract_failed");
        }
        ItemStack leftover = targetSlot.safeInsert(extracted);
        ItemStack putBack = leftover.isEmpty() ? ItemStack.EMPTY : carried.insertBestFit(player, leftover, false);
        int moved = extracted.getCount() - (leftover.isEmpty() ? 0 : leftover.getCount());
        SlotDebugLog.log(
                "[xsurface][server] drop done: extracted={} target={} leftover={} putback={}",
                describeStack(extracted), describeStack(targetSlot.getItem()),
                describeStack(leftover), describeStack(putBack));
        return moved > 0
                ? WorkspaceCommandOutcome.accepted("cross_surface_dropped", "moved=" + moved)
                : WorkspaceCommandOutcome.rejected("host_rejected_stack");
    }

    public static WorkspaceCommandOutcome crossSurfaceQuickMoveAtlas(
            ServerPlayer player,
            ItemIdentity identity,
            Integer count
    ) {
        if (player == null) {
            return WorkspaceCommandOutcome.rejected("missing_player");
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null || count == null || count <= 0) {
            return WorkspaceCommandOutcome.rejected("invalid_quick_move");
        }
        if (identity == null) {
            return WorkspaceCommandOutcome.rejected("invalid_identity");
        }
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        int requested = Math.min(count, 64);
        int remaining = requested;
        int moved = 0;
        while (remaining > 0) {
            Optional<CarriedSourceAccess.CarriedLocation> located = carried.findIdentity(player, identity);
            if (located.isEmpty()) {
                break;
            }
            CarriedSourceAccess.CarriedLocation loc = located.get();
            ItemStack peeked = carried.peek(player, loc.sourceId(), loc.slotIndex());
            if (peeked.isEmpty()) {
                break;
            }
            ItemStack extracted = carried.extract(player, loc.sourceId(), loc.slotIndex(), peeked.getCount(), false);
            if (extracted.isEmpty()) {
                break;
            }
            int extractedCount = extracted.getCount();
            ItemStack remainingStack = extracted;
            for (Slot hostSlot : menu.slots) {
                if (remainingStack.isEmpty()) {
                    break;
                }
                if (hostSlot.container == player.getInventory() || !hostSlot.mayPlace(remainingStack)) {
                    continue;
                }
                remainingStack = hostSlot.safeInsert(remainingStack);
            }
            int placed = extractedCount - (remainingStack.isEmpty() ? 0 : remainingStack.getCount());
            if (!remainingStack.isEmpty()) {
                carried.insertBestFit(player, remainingStack, false);
            }
            if (placed <= 0) {
                break;
            }
            moved++;
            remaining--;
        }
        return moved > 0
                ? WorkspaceCommandOutcome.accepted("cross_surface_quick_moved", "moved=" + moved)
                : WorkspaceCommandOutcome.rejected("host_rejected_stack");
    }

    public static WorkspaceCommandOutcome crossSurfaceQuickMoveHotbar(
            ServerPlayer player,
            Integer hotbarIndex
    ) {
        if (player == null) {
            return WorkspaceCommandOutcome.rejected("missing_player");
        }
        AbstractContainerMenu menu = player.containerMenu;
        int index = hotbarIndex == null ? -1 : hotbarIndex;
        if (menu == null || index < 0 || index >= 9) {
            return WorkspaceCommandOutcome.rejected("invalid_quick_move");
        }
        ItemStack source = player.getInventory().getItem(index);
        if (source == null || source.isEmpty()) {
            return WorkspaceCommandOutcome.rejected("hotbar_slot_empty");
        }

        ItemStack extracted = source.copy();
        int extractedCount = extracted.getCount();
        player.getInventory().setItem(index, ItemStack.EMPTY);
        ItemStack remainingStack = extracted;
        for (Slot hostSlot : menu.slots) {
            if (remainingStack.isEmpty()) {
                break;
            }
            if (hostSlot.container == player.getInventory() || !hostSlot.mayPlace(remainingStack)) {
                continue;
            }
            remainingStack = hostSlot.safeInsert(remainingStack);
        }
        int placed = extractedCount - (remainingStack.isEmpty() ? 0 : remainingStack.getCount());
        if (!remainingStack.isEmpty()) {
            player.getInventory().setItem(index, remainingStack);
        }
        if (placed <= 0) {
            player.getInventory().setItem(index, source.copy());
            return WorkspaceCommandOutcome.rejected("host_rejected_stack");
        }
        SlotDebugLog.log("[xsurface][server] hotbar quick-move idx={} moved={} remaining={}",
                index, placed, remainingStack.isEmpty() ? 0 : remainingStack.getCount());
        return WorkspaceCommandOutcome.accepted("cross_surface_quick_moved", "moved=" + placed);
    }

    private static Extraction extractFromCarry(ServerPlayer player, ItemIdentity identity, int amount) {
        CarriedSourceAccess carriedAccess = StorageAccessRegistry.carriedSourceAccess();
        Optional<CarriedSourceAccess.CarriedLocation> carriedLoc = carriedAccess.findIdentity(player, identity);
        if (carriedLoc.isEmpty()) {
            return Extraction.empty();
        }
        CarriedSourceAccess.CarriedLocation loc = carriedLoc.get();
        ItemStack peeked = carriedAccess.peek(player, loc.sourceId(), loc.slotIndex());
        if (peeked.isEmpty()) {
            return Extraction.empty();
        }
        int extractAmount = Math.min(amount, peeked.getCount());
        ItemStack extracted = carriedAccess.extract(player, loc.sourceId(), loc.slotIndex(), extractAmount, false);
        if (extracted.isEmpty()) {
            return Extraction.empty();
        }
        return new Extraction(
                extracted,
                new CursorOrigin(CursorSourceKind.CARRY, loc.sourceId(), loc.slotIndex()),
                loc.sourceId());
    }

    private static Extraction extractFromProximateChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemIdentity identity,
            int amount
    ) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return Extraction.empty();
        }
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(player, claimedChestMap);
        if (proximate.isEmpty()) {
            return Extraction.empty();
        }
        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap().decayed(player.serverLevel().getGameTime());
        List<ClaimedChest> ranked = DepositPlanner.rankProximateChestsForTake(
                identity, claimedChestMap, affinityMap, proximate);
        WorldStorageAccess worldStorage = StorageAccessRegistry.worldStorageAccess();
        for (ClaimedChest chest : ranked) {
            WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
            for (WorldStorageAccess.SlotContent entry : worldStorage.enumerate(server, target)) {
                ItemStack stackInChest = entry.stack();
                if (stackInChest.isEmpty() || !identity.equals(ItemIdentityMatcher.create(stackInChest))) {
                    continue;
                }
                int extractAmount = Math.min(amount, stackInChest.getCount());
                ItemStack pulled = worldStorage.extract(server, target, entry.slotIndex(), extractAmount, false);
                if (pulled == null || pulled.isEmpty()) {
                    continue;
                }
                String label = chest.label();
                return new Extraction(
                        pulled,
                        new CursorOrigin(CursorSourceKind.CHEST, chest.storageId().toString(), entry.slotIndex()),
                        label == null || label.isBlank() ? chest.storageId().toString() : label);
            }
        }
        return Extraction.empty();
    }

    private static ItemStack smartDepositLeftover(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemStack stack
    ) {
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack remaining = stack.copy();
        ItemIdentity identity = ItemIdentityMatcher.create(remaining);

        int desired = runtime.desiredCountWorkflow().resolved(runtime.snapshot().kitMap(), identity);
        if (desired > 0) {
            int currentInCarry = totalCarriedCount(player, identity);
            int gap = Math.max(0, desired - currentInCarry);
            if (gap > 0) {
                int amountToFill = Math.min(gap, remaining.getCount());
                ItemStack toInsert = remaining.copy();
                toInsert.setCount(amountToFill);
                ItemStack carryLeftover = StorageAccessRegistry.carriedSourceAccess()
                        .insertBestFit(player, toInsert, false);
                int leftoverCount = carryLeftover == null || carryLeftover.isEmpty() ? 0 : carryLeftover.getCount();
                remaining.shrink(amountToFill - leftoverCount);
            }
        }
        if (remaining.isEmpty()) {
            return ItemStack.EMPTY;
        }

        MinecraftServer server = player.getServer();
        if (server != null) {
            ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
            Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(player, claimedChestMap);
            if (!proximate.isEmpty()) {
                long tick = player.serverLevel().getGameTime();
                ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap().decayed(tick);
                List<UUID> ranked = DepositPlanner.rankChestsForIdentity(
                        identity,
                        claimedChestMap,
                        affinityMap,
                        proximate,
                        WorkspaceChestCommandService.liveChestContentPresence(player));
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
                        runtime.chestClaimWorkflow().recordDeposit(storageUuid, identity, depositedHere, tick);
                    }
                    if (remaining.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        }
        return insertIntoCarry(player, remaining);
    }

    private static ItemStack insertIntoCarry(ServerPlayer player, ItemStack stack) {
        ItemStack leftover = StorageAccessRegistry.carriedSourceAccess().insertBestFit(player, stack, false);
        return leftover == null ? ItemStack.EMPTY : leftover;
    }

    private static ItemStack insertIntoOriginChest(
            ServerPlayer player,
            WorkflowDomainRuntime runtime,
            ItemStack stack,
            String storageIdRaw
    ) {
        MinecraftServer server = player.getServer();
        ClaimedChest chest = lookupChestByStorageId(runtime, storageIdRaw);
        if (server == null || chest == null) {
            return stack;
        }
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
        ItemStack leftover = world.insert(server, target, stack, false);
        return leftover == null ? ItemStack.EMPTY : leftover;
    }

    private static int totalCarriedCount(ServerPlayer player, ItemIdentity identity) {
        CarriedSourceAccess access = StorageAccessRegistry.carriedSourceAccess();
        int total = 0;
        for (CarriedSourceAccess.CarriedLocation loc : access.findAllMatching(player, identity)) {
            total += access.peek(player, loc.sourceId(), loc.slotIndex()).getCount();
        }
        return total;
    }

    private static ClaimedChest lookupChestByStorageId(WorkflowDomainRuntime runtime, String storageIdRaw) {
        if (runtime == null || storageIdRaw == null || storageIdRaw.isBlank()) {
            return null;
        }
        UUID storageId;
        try {
            storageId = UUID.fromString(storageIdRaw);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        return runtime.chestClaimWorkflow().claimedChestMap().chest(storageId);
    }

    private static CursorCommandOutcome accepted(String status, String diagnostics, CursorOrigin origin) {
        return new CursorCommandOutcome(WorkspaceCommandOutcome.accepted(status, diagnostics), origin);
    }

    private static CursorCommandOutcome rejected(String diagnostics, CursorOrigin origin) {
        return new CursorCommandOutcome(WorkspaceCommandOutcome.rejected(diagnostics), origin);
    }

    private static String describeStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        return stack.getCount() + "x" + ItemIdentityMatcher.create(stack).itemId();
    }

    private static CursorOrigin hotbarCursorOrigin(
            CursorOrigin previous,
            int hotbarIndex,
            ItemStack carriedBefore,
            ItemStack carriedAfter
    ) {
        if (carriedAfter == null || carriedAfter.isEmpty()) {
            return null;
        }
        if (carriedBefore == null || carriedBefore.isEmpty() || !sameStackIdentity(carriedBefore, carriedAfter)) {
            return new CursorOrigin(
                    CursorSourceKind.CARRY,
                    BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0,
                    hotbarIndex);
        }
        return previous;
    }

    private static boolean sameStackIdentity(ItemStack first, ItemStack second) {
        if (first == null || first.isEmpty() || second == null || second.isEmpty()) {
            return false;
        }
        return SlotStackAccess.current().sameItemAndData(first, second);
    }

    private record Extraction(ItemStack stack, CursorOrigin origin, String sourceLabel) {
        static Extraction empty() {
            return new Extraction(ItemStack.EMPTY, null, "");
        }
    }

    public enum CursorSourceKind { CARRY, CHEST, HOST_SLOT }

    public record CursorOrigin(CursorSourceKind kind, String sourceId, int slotIndex) {
        public CursorOrigin {
            sourceId = sourceId == null ? "" : sourceId;
        }
    }

    public record CursorCommandOutcome(WorkspaceCommandOutcome outcome, CursorOrigin cursorOrigin) {
        public CursorCommandOutcome {
            outcome = outcome == null ? WorkspaceCommandOutcome.rejected("cursor_command_failed") : outcome;
        }
    }
}
