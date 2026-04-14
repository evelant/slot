package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.BuiltinInventoryIds;
import dev.imagio.slot.inventory.core.EquipmentGroupDescriptor;
import dev.imagio.slot.inventory.core.InventoryBindingResolver;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.MenuCursorAccess;
import dev.imagio.slot.inventory.core.QuickAccessLaneDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

final class BuiltinInventoryActionExecutor {
    private static final int OFFHAND_SWAP_BUTTON = 40;

    private BuiltinInventoryActionExecutor() {
    }

    static MutationResult mutateSource(
            InventoryHostDescriptor host,
            InventoryMutationRequest request,
            InventoryMutationMode mode
    ) {
        if (host == null || request == null) {
            return MutationResult.blocked("missing_host_or_request", request == null ? ItemStack.EMPTY : request.stack());
        }

        InventorySourceDescriptor source = host.source(request.sourceId());
        if (source == null) {
            return MutationResult.blocked("unknown_source", request.stack());
        }

        return switch (request.kind()) {
            case INSERT -> mutateSourceInsert(host, source, request, mode);
            case EXTRACT -> mutateSourceExtract(host, source, request, mode);
            case ACTIVATE_TARGET, UNSPECIFIED -> MutationResult.blocked("unsupported_mutation", request.stack());
        };
    }

    static ExecutionResult transfer(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        if (request.primaryTarget() == null || request.secondaryTarget() == null) {
            return ExecutionResult.blocked("transfer_requires_primary_and_secondary_targets");
        }

        StackResult extracted = extract(host, player, request.primaryTarget(), request);
        if (!extracted.successful()) {
            return ExecutionResult.blocked(extracted.diagnostics());
        }
        if (extracted.stack().isEmpty()) {
            return ExecutionResult.blocked("source_stack_missing");
        }

        StackResult inserted = insert(host, player, request.secondaryTarget(), request, extracted.stack());
        if (!inserted.successful()) {
            if (request.mode() == InventoryActionMode.EXECUTE) {
                restore(host, player, request.primaryTarget(), request, extracted.stack());
            }
            return ExecutionResult.blocked(inserted.diagnostics());
        }

        if (request.mode() == InventoryActionMode.EXECUTE && !inserted.stack().isEmpty()) {
            StackResult restored = restore(host, player, request.primaryTarget(), request, inserted.stack());
            if (!restored.successful() || !restored.stack().isEmpty()) {
                return new ExecutionResult(false, inserted.stack(), extracted.stack(), "transfer_restore_incomplete");
            }
        }

        return new ExecutionResult(true, inserted.stack(), extracted.stack(), "");
    }

    static ExecutionResult drop(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        if (request.primaryTarget() == null) {
            return ExecutionResult.blocked("drop_requires_primary_target");
        }
        StackResult extracted = extract(host, player, request.primaryTarget(), request);
        if (!extracted.successful() || extracted.stack().isEmpty()) {
            return ExecutionResult.blocked(extracted.diagnostics().isBlank() ? "drop_failed" : extracted.diagnostics());
        }
        if (request.mode() == InventoryActionMode.EXECUTE) {
            player.drop(extracted.stack(), false);
        }
        return ExecutionResult.success(ItemStack.EMPTY, extracted.stack());
    }

    static ExecutionResult use(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        if (host == null || player == null || request == null || request.primaryTarget() == null) {
            return ExecutionResult.blocked("use_requires_primary_target");
        }

        ResolvedTarget resolved = resolve(host, request.primaryTarget());
        if (resolved == null || !resolved.canExtract()) {
            return ExecutionResult.blocked("use_target_unavailable");
        }

        if (isSelectedMainHandTarget(host, player, request.primaryTarget(), resolved)) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.isEmpty() || !matchesIdentity(request, mainHand)) {
                return ExecutionResult.blocked("main_hand_identity_mismatch");
            }
            return useHand(player, InteractionHand.MAIN_HAND)
                    ? ExecutionResult.success(mainHand.copy())
                    : ExecutionResult.blocked("main_hand_use_failed");
        }

        if (isOffhandTarget(host, request.primaryTarget(), resolved)) {
            ItemStack offhand = player.getOffhandItem();
            if (offhand.isEmpty() || !matchesIdentity(request, offhand)) {
                return ExecutionResult.blocked("offhand_identity_mismatch");
            }
            return useHand(player, InteractionHand.OFF_HAND)
                    ? ExecutionResult.success(offhand.copy())
                    : ExecutionResult.blocked("offhand_use_failed");
        }

        ResolvedTarget stagingTarget = null;
        ItemStack offhandBefore = player.getOffhandItem().copy();
        if (!offhandBefore.isEmpty()) {
            stagingTarget = findTemporaryTarget(host, player.getInventory(), resolved);
            if (stagingTarget == null || !swapWithOffhand(host.menu(), player, stagingTarget)) {
                return ExecutionResult.blocked("unable_to_stage_offhand");
            }
        }

        boolean sourceSwapped = swapWithOffhand(host.menu(), player, resolved);
        if (!sourceSwapped) {
            if (stagingTarget != null) {
                swapWithOffhand(host.menu(), player, stagingTarget);
            }
            return ExecutionResult.blocked("unable_to_bind_source_to_offhand");
        }

        ItemStack offhandDuringUse = player.getOffhandItem().copy();
        boolean used = useHand(player, InteractionHand.OFF_HAND);
        swapWithOffhand(host.menu(), player, resolved);
        if (stagingTarget != null) {
            swapWithOffhand(host.menu(), player, stagingTarget);
        }
        return used
                ? ExecutionResult.success(offhandDuringUse)
                : ExecutionResult.blocked("offhand_use_failed");
    }

    static ExecutionResult pickup(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        if (host == null || player == null || request == null || request.primaryTarget() == null) {
            return ExecutionResult.blocked("pickup_requires_primary_target");
        }
        ItemStack cursorBefore = MenuCursorAccess.get(host.menu());
        if (!cursorBefore.isEmpty() && request.mode() == InventoryActionMode.EXECUTE) {
            return ExecutionResult.blocked("pickup_requires_empty_cursor");
        }
        StackResult extracted = extract(host, player, request.primaryTarget(), request);
        if (!extracted.successful()) {
            return ExecutionResult.blocked(extracted.diagnostics());
        }
        if (request.mode() == InventoryActionMode.EXECUTE) {
            MenuCursorAccess.set(host.menu(), extracted.stack().copy());
        }
        return ExecutionResult.success(extracted.stack(), extracted.stack());
    }

    static ExecutionResult place(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        if (host == null || player == null || request == null || request.primaryTarget() == null) {
            return ExecutionResult.blocked("place_requires_primary_target");
        }
        ItemStack cursorStack = MenuCursorAccess.get(host.menu());
        if (cursorStack.isEmpty()) {
            return ExecutionResult.blocked("place_requires_cursor_stack");
        }
        StackResult inserted = insert(host, player, request.primaryTarget(), request, cursorStack);
        if (inserted.successful() && request.mode() == InventoryActionMode.EXECUTE) {
            MenuCursorAccess.set(host.menu(), inserted.stack().copy());
        }
        return inserted.successful()
                ? ExecutionResult.success(inserted.stack(), cursorStack)
                : ExecutionResult.blocked(inserted.diagnostics());
    }

    static ExecutionResult swap(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        if (host == null || player == null || request == null || request.primaryTarget() == null) {
            return ExecutionResult.blocked("swap_requires_primary_target");
        }

        ResolvedTarget resolved = resolve(host, request.primaryTarget());
        if (resolved == null || (!resolved.canInsert() && !resolved.canExtract())) {
            return ExecutionResult.blocked("swap_target_unavailable");
        }

        ItemStack cursorStack = MenuCursorAccess.get(host.menu());
        ItemStack targetBefore = currentStack(host.menu(), player.getInventory(), resolved).copy();
        if (!cursorStack.isEmpty() && !canInsert(targetBefore, cursorStack, resolved.source())) {
            return ExecutionResult.blocked("swap_target_not_insertable");
        }
        if (!cursorStack.isEmpty() && request.mode() == InventoryActionMode.EXECUTE && !setTargetStack(host.menu(), player.getInventory(), resolved, cursorStack)) {
            return ExecutionResult.blocked("swap_apply_failed");
        }
        if (cursorStack.isEmpty() && request.mode() == InventoryActionMode.EXECUTE && !setTargetStack(host.menu(), player.getInventory(), resolved, ItemStack.EMPTY)) {
            return ExecutionResult.blocked("swap_apply_failed");
        }
        if (request.mode() == InventoryActionMode.EXECUTE) {
            MenuCursorAccess.set(host.menu(), targetBefore.copy());
        }
        return ExecutionResult.success(targetBefore, cursorStack);
    }

    static ExecutionResult quickMove(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionRequest request
    ) {
        if (request == null || request.primaryTarget() == null) {
            return ExecutionResult.blocked("quick_move_requires_primary_target");
        }

        ResolvedTarget resolved = resolve(host, request.primaryTarget());
        if (resolved == null) {
            return ExecutionResult.blocked("quick_move_target_unavailable");
        }
        if (resolved.bindingRoute() != InventoryBindingRoute.MENU || resolved.menuSlot() == null || resolved.menuSlot() < 0) {
            return ExecutionResult.blocked("quick_move_requires_menu_bound_target");
        }
        AbstractContainerMenu menu = host == null ? null : host.menu();
        Slot slot = safeMenuSlot(menu, resolved.menuSlot());
        if (slot == null || !slot.hasItem() || !slot.mayPickup(player)) {
            return ExecutionResult.blocked("quick_move_target_unavailable");
        }
        ItemStack before = slot.getItem().copy();
        if (request.mode() == InventoryActionMode.SIMULATE) {
            return ExecutionResult.success(before, before);
        }
        menu.clicked(resolved.menuSlot(), 0, ClickType.QUICK_MOVE, player);
        ItemStack after = slot.getItem();
        return !ItemStack.matches(before, after)
                ? ExecutionResult.success(after.copy(), before)
                : ExecutionResult.blocked("quick_move_failed");
    }

    static StackResult extract(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionTarget target,
            InventoryActionRequest request
    ) {
        if (host == null || player == null || target == null || request == null) {
            return StackResult.blocked("missing_host_player_or_target");
        }
        if (target instanceof InventoryActionTarget.SourceTarget sourceTarget) {
            MutationResult mutation = mutateSource(
                    host,
                    InventoryMutationRequest.extract(
                            host,
                            player,
                            sourceTarget.sourceId(),
                            request.identity(),
                            transferMode(request.kind())
                    ),
                    InventoryMutationMode.valueOf(request.mode().name())
            );
            return mutation.successful()
                    ? StackResult.success(mutation.stackRemainder())
                    : StackResult.blocked(mutation.diagnostics());
        }

        ResolvedTarget resolved = resolve(host, target);
        if (resolved == null) {
            return StackResult.blocked("unresolved_target");
        }

        return switch (resolved.bindingRoute()) {
            case MENU -> extractMenuBound(host.menu(), player, resolved, request);
            case PLAYER -> extractPlayerBound(player.getInventory(), resolved, request);
            case PROVIDER, TOOL -> StackResult.blocked("non_builtin_target_route");
        };
    }

    static StackResult insert(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionTarget target,
            InventoryActionRequest request,
            ItemStack stack
    ) {
        if (host == null || player == null || target == null || request == null) {
            return StackResult.blocked("missing_host_player_or_target");
        }
        if (target instanceof InventoryActionTarget.SourceTarget sourceTarget) {
            MutationResult mutation = mutateSource(
                    host,
                    InventoryMutationRequest.insert(host, player, sourceTarget.sourceId(), stack),
                    InventoryMutationMode.valueOf(request.mode().name())
            );
            return mutation.successful()
                    ? StackResult.success(mutation.stackRemainder())
                    : StackResult.blocked(mutation.diagnostics());
        }

        ResolvedTarget resolved = resolve(host, target);
        if (resolved == null) {
            return StackResult.blocked("unresolved_target");
        }

        return switch (resolved.bindingRoute()) {
            case MENU -> insertMenuBound(host.menu(), resolved, request.mode(), stack);
            case PLAYER -> insertPlayerBound(player.getInventory(), resolved, request.mode(), stack);
            case PROVIDER, TOOL -> StackResult.blocked("non_builtin_target_route");
        };
    }

    static ItemStack currentStack(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionTarget target
    ) {
        if (host == null || player == null || target == null) {
            return ItemStack.EMPTY;
        }
        if (target instanceof InventoryActionTarget.CursorTarget) {
            return MenuCursorAccess.get(host.menu());
        }
        ResolvedTarget resolved = resolve(host, target);
        return resolved == null
                ? ItemStack.EMPTY
                : currentStack(host.menu(), player.getInventory(), resolved).copy();
    }

    private static StackResult restore(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionTarget target,
            InventoryActionRequest request,
            ItemStack stack
    ) {
        return insert(host, player, target, request, stack);
    }

    private static StackResult extractMenuBound(
            AbstractContainerMenu menu,
            ServerPlayer player,
            ResolvedTarget resolved,
            InventoryActionRequest request
    ) {
        Integer menuSlotId = resolved.menuSlot();
        if (menu == null || menuSlotId == null || menuSlotId < 0) {
            return StackResult.blocked("menu_slot_unavailable");
        }

        Slot slot = safeMenuSlot(menu, menuSlotId);
        if (slot == null || !slot.hasItem() || !slot.mayPickup(player)) {
            return StackResult.blocked("menu_slot_not_extractable");
        }

        ItemStack current = slot.getItem();
        if (!matchesIdentity(request, current)) {
            return StackResult.blocked("menu_slot_identity_mismatch");
        }

        int amount = requestedAmount(request, current.getCount());
        if (request.mode() == InventoryActionMode.SIMULATE) {
            ItemStack simulated = current.copy();
            simulated.setCount(Math.min(amount, simulated.getCount()));
            return StackResult.success(simulated);
        }

        return StackResult.success(slot.safeTake(amount, current.getCount(), player));
    }

    private static StackResult extractPlayerBound(
            Inventory inventory,
            ResolvedTarget resolved,
            InventoryActionRequest request
    ) {
        ItemStack current = playerStack(inventory, resolved.playerSlot());
        if (current.isEmpty() || !matchesIdentity(request, current)) {
            return StackResult.blocked("player_slot_identity_mismatch");
        }

        int amount = requestedAmount(request, current.getCount());
        if (request.mode() == InventoryActionMode.SIMULATE) {
            ItemStack simulated = current.copy();
            simulated.setCount(Math.min(amount, simulated.getCount()));
            return StackResult.success(simulated);
        }

        ItemStack extracted = current.copy();
        extracted.setCount(Math.min(amount, current.getCount()));
        current.shrink(extracted.getCount());
        if (current.isEmpty()) {
            setPlayerStack(inventory, resolved.playerSlot(), ItemStack.EMPTY);
        }
        return StackResult.success(extracted);
    }

    private static StackResult insertMenuBound(
            AbstractContainerMenu menu,
            ResolvedTarget resolved,
            InventoryActionMode mode,
            ItemStack stack
    ) {
        Integer menuSlotId = resolved.menuSlot();
        if (menu == null || menuSlotId == null || menuSlotId < 0) {
            return StackResult.blocked("menu_slot_unavailable");
        }
        Slot slot = safeMenuSlot(menu, menuSlotId);
        if (slot == null) {
            return StackResult.blocked("menu_slot_unavailable");
        }

        ItemStack input = stack == null ? ItemStack.EMPTY : stack.copy();
        if (input.isEmpty()) {
            return StackResult.success(ItemStack.EMPTY);
        }

        ItemStack remainder = mode == InventoryActionMode.SIMULATE
                ? simulateInsert(slot, input)
                : slot.safeInsert(input);
        return StackResult.success(remainder);
    }

    private static StackResult insertPlayerBound(
            Inventory inventory,
            ResolvedTarget resolved,
            InventoryActionMode mode,
            ItemStack stack
    ) {
        ItemStack input = stack == null ? ItemStack.EMPTY : stack.copy();
        if (input.isEmpty()) {
            return StackResult.success(ItemStack.EMPTY);
        }

        ItemStack existing = playerStack(inventory, resolved.playerSlot());
        if (!canInsert(existing, input, resolved.source())) {
            return StackResult.blocked("player_slot_not_insertable");
        }

        int transferable = transferableCount(existing, input, resolved.source());
        if (transferable <= 0) {
            return StackResult.success(input);
        }

        if (mode == InventoryActionMode.EXECUTE) {
            if (existing.isEmpty()) {
                ItemStack placed = input.copy();
                placed.setCount(transferable);
                setPlayerStack(inventory, resolved.playerSlot(), placed);
            } else {
                existing.grow(transferable);
            }
        }

        ItemStack remainder = input.copy();
        remainder.shrink(transferable);
        return StackResult.success(remainder);
    }

    private static boolean matchesIdentity(InventoryActionRequest request, ItemStack stack) {
        return request.identity() == null || ItemIdentityMatcher.matchesMovable(stack, request.identity());
    }

    private static int requestedAmount(InventoryActionRequest request, int available) {
        if (request.requestedCount() > 0) {
            return Math.min(request.requestedCount(), Math.max(1, available));
        }
        return switch (request.kind()) {
            case TRANSFER_ONE -> 1;
            case TRANSFER_STACK, TRANSFER_ALL, DROP, EQUIP, UNEQUIP, PICKUP, PLACE, SWAP, QUICK_MOVE -> Math.max(1, available);
            default -> Math.max(1, available);
        };
    }

    private static InventoryTransferMode transferMode(InventoryActionKind kind) {
        return switch (kind) {
            case TRANSFER_ONE -> InventoryTransferMode.ONE;
            case TRANSFER_STACK, EQUIP, UNEQUIP, DROP -> InventoryTransferMode.STACK;
            case TRANSFER_ALL -> InventoryTransferMode.ALL;
            default -> InventoryTransferMode.STACK;
        };
    }

    private static MutationResult mutateSourceInsert(
            InventoryHostDescriptor host,
            InventorySourceDescriptor source,
            InventoryMutationRequest request,
            InventoryMutationMode mode
    ) {
        ItemStack input = request.stack() == null ? ItemStack.EMPTY : request.stack().copy();
        if (input.isEmpty()) {
            return MutationResult.success(ItemStack.EMPTY);
        }

        if (source.bindingRoute() == InventoryBindingRoute.MENU) {
            ItemStack remainder = input;
            for (int menuSlot : host.topology().menuSlotsForSource(source.id())) {
                Slot slot = safeMenuSlot(host.menu(), menuSlot);
                if (slot == null) {
                    continue;
                }
                remainder = mode == InventoryMutationMode.SIMULATE
                        ? simulateInsert(slot, remainder)
                        : slot.safeInsert(remainder);
                if (remainder.isEmpty()) {
                    return MutationResult.success(ItemStack.EMPTY);
                }
            }
            return mode == InventoryMutationMode.SIMULATE
                    ? MutationResult.blocked("simulation_incomplete", remainder)
                    : MutationResult.success(remainder);
        }

        if (source.bindingRoute() != InventoryBindingRoute.PLAYER || request.player() == null) {
            return MutationResult.blocked("unsupported_builtin_insert_route", input);
        }

        ItemStack remainder = input;
        Inventory inventory = request.player().getInventory();
        for (int logicalSlot = 0; logicalSlot < source.logicalSlotCount(); logicalSlot++) {
            int playerSlot = playerSlotIndex(source.id(), logicalSlot);
            if (playerSlot < 0) {
                continue;
            }
            remainder = insertPlayerBound(
                    inventory,
                    new ResolvedTarget(source, InventoryBindingRoute.PLAYER, null, playerSlot, true, source.supports(InventoryCapability.EXTRACT)),
                    mode == InventoryMutationMode.SIMULATE ? InventoryActionMode.SIMULATE : InventoryActionMode.EXECUTE,
                    remainder
            ).stack();
            if (remainder.isEmpty()) {
                return MutationResult.success(ItemStack.EMPTY);
            }
        }
        return mode == InventoryMutationMode.SIMULATE
                ? MutationResult.blocked("simulation_incomplete", remainder)
                : MutationResult.success(remainder);
    }

    private static MutationResult mutateSourceExtract(
            InventoryHostDescriptor host,
            InventorySourceDescriptor source,
            InventoryMutationRequest request,
            InventoryMutationMode mode
    ) {
        if (request.identity() == null) {
            return MutationResult.blocked("missing_identity", ItemStack.EMPTY);
        }

        if (source.bindingRoute() == InventoryBindingRoute.MENU) {
            for (int menuSlot : host.topology().menuSlotsForSource(source.id())) {
                Slot slot = safeMenuSlot(host.menu(), menuSlot);
                ItemStack stack = slot == null ? ItemStack.EMPTY : slot.getItem();
                if (slot == null
                        || stack.isEmpty()
                        || (request.player() != null && !slot.mayPickup(request.player()))
                        || !ItemIdentityMatcher.matchesMovable(stack, request.identity())) {
                    continue;
                }
                int amount = requestedTransferAmount(request.transferMode(), stack.getCount());
                if (mode == InventoryMutationMode.SIMULATE) {
                    ItemStack simulated = stack.copy();
                    simulated.setCount(Math.min(amount, simulated.getCount()));
                    return MutationResult.success(simulated);
                }
                return MutationResult.success(slot.safeTake(amount, stack.getCount(), request.player()));
            }
            return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
        }

        if (source.bindingRoute() != InventoryBindingRoute.PLAYER || request.player() == null) {
            return MutationResult.blocked("unsupported_builtin_extract_route", ItemStack.EMPTY);
        }

        Inventory inventory = request.player().getInventory();
        for (int logicalSlot = 0; logicalSlot < source.logicalSlotCount(); logicalSlot++) {
            int playerSlot = playerSlotIndex(source.id(), logicalSlot);
            ItemStack stack = playerStack(inventory, playerSlot);
            if (playerSlot < 0 || stack.isEmpty() || !ItemIdentityMatcher.matchesMovable(stack, request.identity())) {
                continue;
            }
            int amount = requestedTransferAmount(request.transferMode(), stack.getCount());
            if (mode == InventoryMutationMode.SIMULATE) {
                ItemStack simulated = stack.copy();
                simulated.setCount(Math.min(amount, simulated.getCount()));
                return MutationResult.success(simulated);
            }
            ItemStack extracted = stack.copy();
            extracted.setCount(Math.min(amount, stack.getCount()));
            stack.shrink(extracted.getCount());
            if (stack.isEmpty()) {
                setPlayerStack(inventory, playerSlot, ItemStack.EMPTY);
            }
            return MutationResult.success(extracted);
        }
        return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
    }

    private static ResolvedTarget resolve(InventoryHostDescriptor host, InventoryActionTarget target) {
        return switch (target) {
            case InventoryActionTarget.CursorTarget ignored -> null;
            case InventoryActionTarget.SourceTarget ignored -> null;
            case InventoryActionTarget.SourceSlotTarget slotTarget -> resolveSourceTarget(host, slotTarget.sourceId(), slotTarget.slotIndex());
            case InventoryActionTarget.SourceEntryTarget ignored -> null;
            case InventoryActionTarget.QuickAccessTarget laneTarget -> {
                QuickAccessLaneDescriptor lane = host.quickAccessLane(laneTarget.laneId());
                yield lane == null ? null : resolveSourceTarget(host, lane.sourceId(), laneTarget.slotIndex());
            }
            case InventoryActionTarget.EquipmentTarget equipmentTarget -> {
                EquipmentGroupDescriptor group = host.equipmentGroup(equipmentTarget.groupId());
                yield group == null ? null : resolveSourceTarget(host, group.sourceId(), equipmentTarget.slotIndex());
            }
            case InventoryActionTarget.ToolRegionTarget regionTarget -> {
                InventoryToolDescriptor tool = host.tool(regionTarget.toolId());
                ToolRegionDescriptor region = tool == null ? null : tool.regions().stream()
                        .filter(candidate -> candidate.id().equals(regionTarget.regionId()))
                        .findFirst()
                        .orElse(null);
                if (region != null && !region.linkedSourceId().isBlank()) {
                    yield resolveSourceTarget(host, region.linkedSourceId(), regionTarget.slotIndex());
                }
                Integer menuSlot = InventoryBindingResolver.resolveMenuSlot(host, target);
                yield region == null ? null : new ResolvedTarget(null, region.bindingRoute(), menuSlot, -1, region.supports(InventoryCapability.INSERT), region.supports(InventoryCapability.EXTRACT));
            }
            case InventoryActionTarget.ToolControlTarget ignored -> null;
        };
    }

    private static ResolvedTarget resolveSourceTarget(InventoryHostDescriptor host, String sourceId, int logicalSlotIndex) {
        InventorySourceDescriptor source = host.source(sourceId);
        if (source == null) {
            return null;
        }
        Integer menuSlot = host.topology().resolveMenuSlot(sourceId, logicalSlotIndex);
        int playerSlot = playerSlotIndex(source.id(), logicalSlotIndex);
        return new ResolvedTarget(
                source,
                source.bindingRoute(),
                menuSlot,
                playerSlot,
                source.supports(InventoryCapability.INSERT),
                source.supports(InventoryCapability.EXTRACT)
        );
    }

    private static int playerSlotIndex(String sourceId, int logicalSlotIndex) {
        if (BuiltinInventoryIds.PLAYER_MAIN.equals(sourceId)) {
            return logicalSlotIndex < 0 || logicalSlotIndex >= 27 ? -1 : logicalSlotIndex + 9;
        }
        if (BuiltinInventoryIds.PLAYER_QUICK_ACCESS_LANE_0.equals(sourceId)) {
            return logicalSlotIndex < 0 || logicalSlotIndex >= 9 ? -1 : logicalSlotIndex;
        }
        if (BuiltinInventoryIds.PLAYER_ARMOR.equals(sourceId)) {
            return logicalSlotIndex < 0 || logicalSlotIndex >= 4 ? -1 : 39 - logicalSlotIndex;
        }
        if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(sourceId)) {
            return logicalSlotIndex == 0 ? 40 : -1;
        }
        return -1;
    }

    private static Slot safeMenuSlot(AbstractContainerMenu menu, int menuSlotId) {
        if (menu == null || menuSlotId < 0) {
            return null;
        }
        try {
            return menu.getSlot(menuSlotId);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static ItemStack simulateInsert(Slot slot, ItemStack stack) {
        if (slot == null || stack.isEmpty() || !slot.mayPlace(stack)) {
            return stack;
        }

        ItemStack existing = slot.getItem();
        int transferable;
        if (existing.isEmpty()) {
            transferable = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), slot.getMaxStackSize(stack)));
        } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
            int capacity = Math.max(0, Math.min(existing.getMaxStackSize(), slot.getMaxStackSize(existing)) - existing.getCount());
            transferable = Math.min(stack.getCount(), capacity);
        } else {
            transferable = 0;
        }

        ItemStack remainder = stack.copy();
        remainder.shrink(Math.max(0, transferable));
        return remainder;
    }

    private static int requestedTransferAmount(InventoryTransferMode mode, int available) {
        return switch (mode) {
            case ONE -> 1;
            case STACK, ALL -> Math.max(1, available);
        };
    }

    private static boolean isSelectedMainHandTarget(
            InventoryHostDescriptor host,
            ServerPlayer player,
            InventoryActionTarget target,
            ResolvedTarget resolved
    ) {
        if (host == null || player == null || target == null || resolved == null) {
            return false;
        }
        if (!(target instanceof InventoryActionTarget.QuickAccessTarget laneTarget)) {
            return false;
        }
        return BuiltinInventoryIds.QUICK_ACCESS_LANE_0.equals(laneTarget.laneId())
                && laneTarget.slotIndex() == player.getInventory().selected;
    }

    private static boolean isOffhandTarget(
            InventoryHostDescriptor host,
            InventoryActionTarget target,
            ResolvedTarget resolved
    ) {
        if (host == null || target == null || resolved == null) {
            return false;
        }
        if (target instanceof InventoryActionTarget.EquipmentTarget equipmentTarget) {
            return BuiltinInventoryIds.EQUIPMENT_GROUP_OFFHAND.equals(equipmentTarget.groupId())
                    && equipmentTarget.slotIndex() == 0;
        }
        return resolved.source() != null && BuiltinInventoryIds.PLAYER_OFFHAND.equals(resolved.source().id());
    }

    private static boolean useHand(ServerPlayer player, InteractionHand hand) {
        if (player == null || hand == null || player.gameMode == null) {
            return false;
        }
        ItemStack stack = hand == InteractionHand.MAIN_HAND ? player.getMainHandItem() : player.getOffhandItem();
        if (stack.isEmpty()) {
            return false;
        }
        InteractionResult result = player.gameMode.useItem(player, player.level(), stack, hand);
        return result.consumesAction() || player.isUsingItem();
    }

    private static ResolvedTarget findTemporaryTarget(
            InventoryHostDescriptor host,
            Inventory inventory,
            ResolvedTarget excluded
    ) {
        if (host == null) {
            return null;
        }
        for (InventorySourceDescriptor source : host.carriedSources()) {
            if (source == null || !source.supports(InventoryCapability.INSERT)) {
                continue;
            }
            if (BuiltinInventoryIds.PLAYER_OFFHAND.equals(source.id()) || BuiltinInventoryIds.PLAYER_ARMOR.equals(source.id())) {
                continue;
            }
            for (int logicalSlot = 0; logicalSlot < source.logicalSlotCount(); logicalSlot++) {
                ResolvedTarget candidate = resolveSourceTarget(host, source.id(), logicalSlot);
                if (candidate == null || sameSlot(candidate, excluded) || !candidate.canInsert()) {
                    continue;
                }
                ItemStack current = currentStack(host.menu(), inventory, candidate);
                if (current.isEmpty()) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static boolean sameSlot(ResolvedTarget first, ResolvedTarget second) {
        if (first == null || second == null) {
            return false;
        }
        if (first.bindingRoute() != second.bindingRoute()) {
            return false;
        }
        return switch (first.bindingRoute()) {
            case MENU -> java.util.Objects.equals(first.menuSlot(), second.menuSlot());
            case PLAYER -> first.playerSlot() == second.playerSlot();
            case PROVIDER, TOOL -> false;
        };
    }

    private static boolean swapWithOffhand(
            AbstractContainerMenu menu,
            ServerPlayer player,
            ResolvedTarget target
    ) {
        if (player == null || target == null) {
            return false;
        }

        return switch (target.bindingRoute()) {
            case MENU -> swapMenuTargetWithOffhand(menu, player, target.menuSlot());
            case PLAYER -> swapPlayerTargetWithOffhand(player.getInventory(), target.playerSlot());
            case PROVIDER, TOOL -> false;
        };
    }

    private static boolean swapMenuTargetWithOffhand(
            AbstractContainerMenu menu,
            ServerPlayer player,
            Integer menuSlotId
    ) {
        if (menu == null || menuSlotId == null || menuSlotId < 0 || !MenuCursorAccess.get(menu).isEmpty()) {
            return false;
        }
        Slot slot = safeMenuSlot(menu, menuSlotId);
        if (slot == null) {
            return false;
        }
        ItemStack beforeSlot = slot.getItem().copy();
        ItemStack beforeOffhand = player.getOffhandItem().copy();
        menu.clicked(menuSlotId, OFFHAND_SWAP_BUTTON, ClickType.SWAP, player);
        ItemStack afterSlot = slot.getItem();
        ItemStack afterOffhand = player.getOffhandItem();
        return !ItemStack.matches(beforeSlot, afterSlot) || !ItemStack.matches(beforeOffhand, afterOffhand);
    }

    private static boolean swapPlayerTargetWithOffhand(Inventory inventory, int playerSlot) {
        if (inventory == null || playerSlot < 0) {
            return false;
        }
        ItemStack target = playerStack(inventory, playerSlot).copy();
        ItemStack offhand = playerStack(inventory, 40).copy();
        setPlayerStack(inventory, playerSlot, offhand);
        setPlayerStack(inventory, 40, target);
        return !ItemStack.matches(target, playerStack(inventory, playerSlot))
                || !ItemStack.matches(offhand, playerStack(inventory, 40));
    }

    private static ItemStack currentStack(
            AbstractContainerMenu menu,
            Inventory inventory,
            ResolvedTarget target
    ) {
        if (target == null) {
            return ItemStack.EMPTY;
        }
        return switch (target.bindingRoute()) {
            case MENU -> {
                Slot slot = safeMenuSlot(menu, target.menuSlot() == null ? -1 : target.menuSlot());
                yield slot == null ? ItemStack.EMPTY : slot.getItem();
            }
            case PLAYER -> playerStack(inventory, target.playerSlot());
            case PROVIDER, TOOL -> ItemStack.EMPTY;
        };
    }

    private static boolean setTargetStack(
            AbstractContainerMenu menu,
            Inventory inventory,
            ResolvedTarget target,
            ItemStack stack
    ) {
        if (target == null) {
            return false;
        }
        ItemStack resolved = stack == null ? ItemStack.EMPTY : stack.copy();
        switch (target.bindingRoute()) {
            case MENU -> {
                Slot slot = safeMenuSlot(menu, target.menuSlot() == null ? -1 : target.menuSlot());
                if (slot == null) {
                    return false;
                }
                slot.set(resolved);
                slot.setChanged();
                return true;
            }
            case PLAYER -> {
                setPlayerStack(inventory, target.playerSlot(), resolved);
                return true;
            }
            case PROVIDER, TOOL -> {
                return false;
            }
        }
        return false;
    }

    private static boolean canInsert(ItemStack existing, ItemStack input, InventorySourceDescriptor source) {
        if (input.isEmpty() || source == null || !source.supports(InventoryCapability.INSERT)) {
            return false;
        }
        return existing.isEmpty() || ItemStack.isSameItemSameComponents(existing, input);
    }

    private static int transferableCount(ItemStack existing, ItemStack input, InventorySourceDescriptor source) {
        if (!canInsert(existing, input, source)) {
            return 0;
        }
        if (existing.isEmpty()) {
            int limit = source.id().equals(BuiltinInventoryIds.PLAYER_OFFHAND) ? 1 : input.getMaxStackSize();
            return Math.min(input.getCount(), Math.max(1, limit));
        }
        return Math.min(input.getCount(), Math.max(0, existing.getMaxStackSize() - existing.getCount()));
    }

    private static ItemStack playerStack(Inventory inventory, int playerSlot) {
        if (inventory == null || playerSlot < 0) {
            return ItemStack.EMPTY;
        }
        if (playerSlot < inventory.items.size()) {
            return inventory.items.get(playerSlot);
        }
        if (playerSlot >= 36 && playerSlot < 40) {
            return inventory.armor.get(playerSlot - 36);
        }
        if (playerSlot == 40 && !inventory.offhand.isEmpty()) {
            return inventory.offhand.getFirst();
        }
        return ItemStack.EMPTY;
    }

    private static void setPlayerStack(Inventory inventory, int playerSlot, ItemStack stack) {
        if (inventory == null || playerSlot < 0) {
            return;
        }
        ItemStack resolved = stack == null ? ItemStack.EMPTY : stack;
        if (playerSlot < inventory.items.size()) {
            inventory.items.set(playerSlot, resolved);
            return;
        }
        if (playerSlot >= 36 && playerSlot < 40) {
            inventory.armor.set(playerSlot - 36, resolved);
            return;
        }
        if (playerSlot == 40 && !inventory.offhand.isEmpty()) {
            inventory.offhand.set(0, resolved);
        }
    }

    record ExecutionResult(
            boolean successful,
            ItemStack stackRemainder,
            ItemStack actionStack,
            String diagnostics
    ) {
        static ExecutionResult success(ItemStack remainder) {
            return success(remainder, ItemStack.EMPTY);
        }

        static ExecutionResult success(ItemStack remainder, ItemStack actionStack) {
            return new ExecutionResult(
                    true,
                    remainder == null ? ItemStack.EMPTY : remainder,
                    actionStack == null ? ItemStack.EMPTY : actionStack,
                    ""
            );
        }

        static ExecutionResult blocked(String diagnostics) {
            return new ExecutionResult(false, ItemStack.EMPTY, ItemStack.EMPTY, diagnostics == null ? "" : diagnostics);
        }
    }

    record StackResult(
            boolean successful,
            ItemStack stack,
            String diagnostics
    ) {
        static StackResult success(ItemStack stack) {
            return new StackResult(true, stack == null ? ItemStack.EMPTY : stack, "");
        }

        static StackResult blocked(String diagnostics) {
            return new StackResult(false, ItemStack.EMPTY, diagnostics == null ? "" : diagnostics);
        }
    }

    private record ResolvedTarget(
            InventorySourceDescriptor source,
            InventoryBindingRoute bindingRoute,
            Integer menuSlot,
            int playerSlot,
            boolean canInsert,
            boolean canExtract
    ) {
    }
}
