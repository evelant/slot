package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEvent;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.action.WorkspaceActionChannel;
import dev.imagio.slot.ui.action.WorkspaceActionCatalog;
import dev.imagio.slot.ui.action.WorkspaceActionId;
import dev.imagio.slot.ui.action.WorkspaceActionValidation;
import dev.imagio.slot.ui.action.WorkspaceActionValidator;
import dev.imagio.slot.workflow.domain.ChestRole;
import dev.imagio.slot.workflow.domain.WorkflowAcceptedInputRule;

import java.util.EnumMap;

final class WorkspaceRpcDispatcher implements WorkspaceActionChannel {
    private final SlotWorkspaceUiController host;
    private final EnumMap<WorkspaceActionId, RPCEmitter> actionEmitters = new EnumMap<>(WorkspaceActionId.class);

    RPCEmitter transferEmitter;
    RPCEmitter homeEmitter;
    RPCEmitter createNamedIslandEmitter;
    RPCEmitter hotbarToAtlasEmitter;
    RPCEmitter moveIslandEmitter;
    RPCEmitter reorderIslandEmitter;
    RPCEmitter moveChestEmitter;
    RPCEmitter relabelChestEmitter;
    RPCEmitter forgetItemAffinityEmitter;
    RPCEmitter depositEmitter;
    RPCEmitter gatherActiveKitEmitter;
    RPCEmitter takeAllEmitter;
    RPCEmitter lootChestTakeAllEmitter;
    RPCEmitter lootChestTakeIdentityEmitter;
    RPCEmitter lootChestOpenVanillaEmitter;
    RPCEmitter lootChestClaimAndDepositEmitter;
    RPCEmitter setSearchQueryEmitter;
    RPCEmitter renameClusterEmitter;
    RPCEmitter renameIslandEmitter;
    RPCEmitter recolorIslandEmitter;
    RPCEmitter setIslandIconEmitter;
    RPCEmitter deleteIslandEmitter;
    RPCEmitter acceptChipEmitter;
    RPCEmitter saveKitEmitter;
    RPCEmitter createWorkflowTabEmitter;
    RPCEmitter createKitVariantEmitter;
    RPCEmitter activateKitEmitter;
    RPCEmitter deactivateKitEmitter;
    RPCEmitter undoEmitter;
    RPCEmitter redoEmitter;
    RPCEmitter deleteKitEmitter;
    RPCEmitter switchKitPageEmitter;
    RPCEmitter addKitPageEmitter;
    RPCEmitter removeKitPageEmitter;
    RPCEmitter setKitMemberEmitter;
    RPCEmitter setKitAcceptedInputEmitter;
    RPCEmitter setKitScopedDesiredCountEmitter;
    RPCEmitter setKitSlotIdentityEmitter;
    RPCEmitter renameKitEmitter;
    RPCEmitter duplicateKitEmitter;
    RPCEmitter reorderKitEmitter;
    RPCEmitter swapKitSlotsEmitter;
    RPCEmitter returnHotbarToHomeEmitter;
    RPCEmitter assignHomeToFreeHotbarEmitter;
    RPCEmitter depositCarriedToChestEmitter;
    RPCEmitter depositHotbarToChestEmitter;
    RPCEmitter takeFromChestEmitter;
    RPCEmitter takeOneFromChestEmitter;
    RPCEmitter takeOneByIdentityEmitter;
    RPCEmitter takeItemsByIdentityEmitter;
    RPCEmitter takeDesiredGapOrStackByIdentityEmitter;
    RPCEmitter takeStackByIdentityEmitter;
    RPCEmitter toggleWantedItemEmitter;
    RPCEmitter setWantedCountEmitter;
    RPCEmitter adjustWantedCountEmitter;
    RPCEmitter setJunkEmitter;
    RPCEmitter trashIdentityEmitter;
    RPCEmitter craftRunStageEntryEmitter;
    RPCEmitter craftRunAdjustEntryEmitter;
    RPCEmitter craftRunSelectIngredientEmitter;
    RPCEmitter craftRunRemoveEntryEmitter;
    RPCEmitter assignHomeToHotbarOnlyEmitter;
    RPCEmitter assignIdentityToAutoHotbarEmitter;
    RPCEmitter assignIdentityToHotbarSlotEmitter;
    RPCEmitter moveIdentityToMainInventoryEmitter;
    RPCEmitter moveIdentityToBackpackEmitter;
    RPCEmitter depositHomeToLinkedChestEmitter;
    RPCEmitter depositOneHomeToLinkedChestEmitter;
    RPCEmitter depositItemsHomeToLinkedChestEmitter;
    RPCEmitter setPlayerDesiredCountEmitter;
    RPCEmitter adjustPlayerDesiredCountEmitter;
    RPCEmitter crossSurfaceDropOnHostSlotEmitter;
    RPCEmitter crossSurfaceQuickMoveAtlasEmitter;
    RPCEmitter crossSurfaceQuickMoveHotbarEmitter;
    RPCEmitter pickupToCursorEmitter;
    RPCEmitter cursorCancelEmitter;
    RPCEmitter cursorSmartDepositEmitter;
    RPCEmitter dropCursorIntoChestEmitter;
    RPCEmitter dropCursorAtHotbarEmitter;
    RPCEmitter claimChestAtPosEmitter;
    RPCEmitter setChestRoleAtPosEmitter;

    WorkspaceRpcDispatcher(SlotWorkspaceUiController host) {
        this.host = host;
    }

    private RPCEmitter add(WorkspaceActionId action, RPCEvent event) {
        var definition = WorkspaceActionCatalog.require(action);
        if (event == null) {
            throw new IllegalArgumentException("Missing RPC event for " + definition.wireId());
        }
        if (event.argHolders().length != definition.argumentTypes().size()) {
            throw new IllegalArgumentException("RPC argument count mismatch for " + definition.wireId());
        }
        for (int index = 0; index < event.argHolders().length; index++) {
            Class<?> expected = definition.argumentTypes().get(index).javaType();
            if (!expected.equals(event.argHolders()[index].type)) {
                throw new IllegalArgumentException(
                        "RPC argument " + index + " mismatch for " + definition.wireId()
                                + ": expected " + expected + " got " + event.argHolders()[index].type
                );
            }
        }
        RPCEmitter emitter = host.root.addRPCEvent(event);
        if (actionEmitters.put(action, emitter) != null) {
            throw new IllegalStateException("Duplicate workspace RPC action registration: " + action);
        }
        return emitter;
    }

    @Override
    public boolean send(WorkspaceActionId action, Object... arguments) {
        host.flushWheelTransferBeforeAction();
        WorkspaceActionValidation validation = WorkspaceActionValidator.validate(action, arguments);
        if (!validation.valid()) {
            host.localStatus.set("action rejected: " + validation.diagnostics());
            return false;
        }
        RPCEmitter emitter = actionEmitters.get(action);
        if (emitter == null) {
            host.localStatus.set("action unavailable: " + (action == null ? "<missing>" : action.wireId()));
            return false;
        }
        return emitter.send(arguments == null ? new Object[0] : arguments);
    }

    void register() {
        transferEmitter = add(WorkspaceActionId.TRANSFER, RPCEventBuilder.simple(
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                String.class,
                host.session::transfer
        ));
        homeEmitter = add(WorkspaceActionId.ASSIGN_HOME, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::assignHome
        ));
        createNamedIslandEmitter = add(WorkspaceActionId.CREATE_NAMED_ISLAND, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                host.session::createNamedIslandForItem
        ));
        hotbarToAtlasEmitter = add(WorkspaceActionId.MOVE_HOTBAR_TO_ATLAS, RPCEventBuilder.simple(
                Integer.class,
                String.class,
                Integer.class,
                host.session::moveHotbarToAtlas
        ));
        moveIslandEmitter = add(WorkspaceActionId.MOVE_ISLAND, RPCEventBuilder.simple(
                String.class,
                Double.class,
                Double.class,
                host.session::moveIsland
        ));
        reorderIslandEmitter = add(WorkspaceActionId.REORDER_ISLAND, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::reorderIsland
        ));
        moveChestEmitter = add(WorkspaceActionId.MOVE_CHEST, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                host.session::moveChest
        ));
        relabelChestEmitter = add(WorkspaceActionId.RELABEL_CHEST, RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::relabelChest
        ));
        claimChestAtPosEmitter = add(WorkspaceActionId.CLAIM_CHEST_AT_POS, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                host.session::claimChestAtPos
        ));
        setChestRoleAtPosEmitter = add(WorkspaceActionId.SET_CHEST_ROLE_AT_POS, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                String.class,
                host.session::setChestRoleAtPos
        ));
        forgetItemAffinityEmitter = add(WorkspaceActionId.FORGET_ITEM_AFFINITY, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::forgetItemAffinity
        ));
        depositEmitter = add(WorkspaceActionId.DEPOSIT, RPCEventBuilder.simple((Runnable) host.session::deposit));
        gatherActiveKitEmitter = add(WorkspaceActionId.GATHER_ACTIVE_KIT,
                RPCEventBuilder.simple((Runnable) host.session::gatherActiveKit));
        takeAllEmitter = add(WorkspaceActionId.TAKE_ALL_FROM_CHEST, RPCEventBuilder.simple(
                String.class,
                host.session::takeAllFromChest
        ));
        lootChestTakeAllEmitter = add(WorkspaceActionId.LOOT_CHEST_TAKE_ALL, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                host.session::takeAllFromLootChest
        ));
        lootChestTakeIdentityEmitter = add(WorkspaceActionId.LOOT_CHEST_TAKE_IDENTITY, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                host.session::takeIdentityFromLootChest
        ));
        lootChestOpenVanillaEmitter = add(WorkspaceActionId.LOOT_CHEST_OPEN_VANILLA, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                host.session::openVanillaForLootChest
        ));
        lootChestClaimAndDepositEmitter = add(WorkspaceActionId.LOOT_CHEST_CLAIM_AND_DEPOSIT, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                host.session::claimAndDepositCarriedToLootChest
        ));
        setSearchQueryEmitter = add(WorkspaceActionId.SET_SEARCH_QUERY, RPCEventBuilder.simple(
                String.class,
                host.session::setSearchQuery
        ));
        renameClusterEmitter = add(WorkspaceActionId.RENAME_CLUSTER, RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::renameCluster
        ));
        renameIslandEmitter = add(WorkspaceActionId.RENAME_ISLAND, RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::renameIsland
        ));
        recolorIslandEmitter = add(WorkspaceActionId.RECOLOR_ISLAND, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::recolorIsland
        ));
        setIslandIconEmitter = add(WorkspaceActionId.SET_ISLAND_ICON, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::setIslandIcon
        ));
        deleteIslandEmitter = add(WorkspaceActionId.DELETE_ISLAND, RPCEventBuilder.simple(
                String.class,
                host.session::deleteIsland
        ));
        acceptChipEmitter = add(WorkspaceActionId.ACCEPT_CHIP, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::acceptChip
        ));
        saveKitEmitter = add(WorkspaceActionId.SAVE_KIT, RPCEventBuilder.simple(
                String.class,
                host.session::saveBeltAsKit
        ));
        createWorkflowTabEmitter = add(WorkspaceActionId.CREATE_WORKFLOW_TAB, RPCEventBuilder.simple(
                String.class,
                host.session::createWorkflowTab
        ));
        createKitVariantEmitter = add(WorkspaceActionId.CREATE_KIT_VARIANT, RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::createKitVariant
        ));
        activateKitEmitter = add(WorkspaceActionId.ACTIVATE_KIT, RPCEventBuilder.simple(
                String.class,
                host.session::activateKit
        ));
        deactivateKitEmitter = add(WorkspaceActionId.DEACTIVATE_KIT, RPCEventBuilder.simple(
                (Runnable) host.session::deactivateKit
        ));
        undoEmitter = add(WorkspaceActionId.UNDO, RPCEventBuilder.simple(
                (Runnable) host.session::performUndo
        ));
        redoEmitter = add(WorkspaceActionId.REDO, RPCEventBuilder.simple(
                (Runnable) host.session::performRedo
        ));
        deleteKitEmitter = add(WorkspaceActionId.DELETE_KIT, RPCEventBuilder.simple(
                String.class,
                host.session::deleteKit
        ));
        switchKitPageEmitter = add(WorkspaceActionId.SWITCH_KIT_PAGE, RPCEventBuilder.simple(
                Integer.class,
                host.session::switchKitPage
        ));
        addKitPageEmitter = add(WorkspaceActionId.ADD_KIT_PAGE, RPCEventBuilder.simple(
                String.class,
                host.session::addKitPage
        ));
        removeKitPageEmitter = add(WorkspaceActionId.REMOVE_KIT_PAGE, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::removeKitPage
        ));
        setKitMemberEmitter = add(WorkspaceActionId.SET_KIT_MEMBER, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::setKitMember
        ));
        setKitAcceptedInputEmitter = add(WorkspaceActionId.SET_KIT_ACCEPTED_INPUT, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::setKitAcceptedInput
        ));
        // Replaces the legacy addKitBring/removeKitBring pair: writes the
        // kit-scoped desired count for an explicit kitId, even when that
        // kit isn't the active one. The kit-rack UI uses count=1 for "add"
        // and count=0 for "remove."
        setKitScopedDesiredCountEmitter = add(WorkspaceActionId.SET_KIT_SCOPED_DESIRED_COUNT, RPCEventBuilder.simple(
                String.class,    // kitId
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // count (0 = clear)
                host.session::setKitScopedDesiredCount
        ));
        setKitSlotIdentityEmitter = add(WorkspaceActionId.SET_KIT_SLOT_IDENTITY, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                host.session::setKitSlotIdentity
        ));
        renameKitEmitter = add(WorkspaceActionId.RENAME_KIT, RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::renameKit
        ));
        duplicateKitEmitter = add(WorkspaceActionId.DUPLICATE_KIT, RPCEventBuilder.simple(
                String.class,
                host.session::duplicateKit
        ));
        reorderKitEmitter = add(WorkspaceActionId.REORDER_KIT, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::reorderKit
        ));
        swapKitSlotsEmitter = add(WorkspaceActionId.SWAP_KIT_SLOTS, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                host.session::swapKitSlots
        ));
        returnHotbarToHomeEmitter = add(WorkspaceActionId.RETURN_HOTBAR_TO_HOME, RPCEventBuilder.simple(
                Integer.class,
                host.session::returnHotbarToHome
        ));
        assignHomeToFreeHotbarEmitter = add(WorkspaceActionId.ASSIGN_HOME_TO_FREE_HOTBAR, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::assignHomeToFreeHotbar
        ));
        depositCarriedToChestEmitter = add(WorkspaceActionId.DEPOSIT_CARRIED_TO_CHEST, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::depositCarriedToChest
        ));
        depositHotbarToChestEmitter = add(WorkspaceActionId.DEPOSIT_HOTBAR_TO_CHEST, RPCEventBuilder.simple(
                Integer.class,
                String.class,
                host.session::depositHotbarToChest
        ));
        takeFromChestEmitter = add(WorkspaceActionId.TAKE_FROM_CHEST, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::takeFromChest
        ));
        takeOneFromChestEmitter = add(WorkspaceActionId.TAKE_ONE_FROM_CHEST, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::takeOneFromChest
        ));
        takeOneByIdentityEmitter = add(WorkspaceActionId.TAKE_ONE_BY_IDENTITY, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::takeOneByIdentity
        ));
        takeItemsByIdentityEmitter = add(WorkspaceActionId.TAKE_ITEMS_BY_IDENTITY, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::takeItemsByIdentity
        ));
        takeDesiredGapOrStackByIdentityEmitter = add(WorkspaceActionId.TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::takeDesiredGapOrStackByIdentity
        ));
        takeStackByIdentityEmitter = add(WorkspaceActionId.TAKE_STACK_BY_IDENTITY, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::takeStackByIdentity
        ));
        toggleWantedItemEmitter = add(WorkspaceActionId.TOGGLE_WANTED_ITEM, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::toggleWantedItem
        ));
        setWantedCountEmitter = add(WorkspaceActionId.SET_WANTED_COUNT, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::setWantedCount
        ));
        adjustWantedCountEmitter = add(WorkspaceActionId.ADJUST_WANTED_COUNT, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::adjustWantedCount
        ));
        setJunkEmitter = add(WorkspaceActionId.SET_JUNK, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::setJunk
        ));
        trashIdentityEmitter = add(WorkspaceActionId.TRASH_IDENTITY, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::trashIdentity
        ));
        craftRunStageEntryEmitter = add(WorkspaceActionId.CRAFT_RUN_STAGE_ENTRY, RPCEventBuilder.simple(
                String.class,
                host.session::stageCraftRunEntry
        ));
        craftRunAdjustEntryEmitter = add(WorkspaceActionId.CRAFT_RUN_ADJUST_ENTRY, RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::adjustCraftRunEntry
        ));
        craftRunSelectIngredientEmitter = add(WorkspaceActionId.CRAFT_RUN_SELECT_INGREDIENT, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::selectCraftRunIngredient
        ));
        craftRunRemoveEntryEmitter = add(WorkspaceActionId.CRAFT_RUN_REMOVE_ENTRY, RPCEventBuilder.simple(
                String.class,
                host.session::removeCraftRunEntry
        ));
        assignHomeToHotbarOnlyEmitter = add(WorkspaceActionId.ASSIGN_HOME_TO_HOTBAR_ONLY, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::assignHomeToHotbarOnly
        ));
        assignIdentityToAutoHotbarEmitter = add(WorkspaceActionId.ASSIGN_IDENTITY_TO_AUTO_HOTBAR, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::assignIdentityToAutoHotbar
        ));
        assignIdentityToHotbarSlotEmitter = add(WorkspaceActionId.ASSIGN_IDENTITY_TO_HOTBAR_SLOT, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::assignIdentityToHotbarSlot
        ));
        moveIdentityToMainInventoryEmitter = add(WorkspaceActionId.MOVE_IDENTITY_TO_MAIN_INVENTORY, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::moveIdentityToMainInventory
        ));
        moveIdentityToBackpackEmitter = add(WorkspaceActionId.MOVE_IDENTITY_TO_BACKPACK, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::moveIdentityToBackpack
        ));
        depositHomeToLinkedChestEmitter = add(WorkspaceActionId.DEPOSIT_HOME_TO_LINKED_CHEST, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::depositHomeToLinkedChest
        ));
        depositOneHomeToLinkedChestEmitter = add(WorkspaceActionId.DEPOSIT_ONE_HOME_TO_LINKED_CHEST, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::depositOneHomeToLinkedChest
        ));
        depositItemsHomeToLinkedChestEmitter = add(WorkspaceActionId.DEPOSIT_ITEMS_HOME_TO_LINKED_CHEST, RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::depositItemsHomeToLinkedChest
        ));
        setPlayerDesiredCountEmitter = add(WorkspaceActionId.SET_PLAYER_DESIRED_COUNT, RPCEventBuilder.simple(
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // count (0 = clear)
                host.session::setPlayerDesiredCount
        ));
        adjustPlayerDesiredCountEmitter = add(WorkspaceActionId.ADJUST_PLAYER_DESIRED_COUNT, RPCEventBuilder.simple(
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // delta (signed, often ±1 from ctrl+scroll)
                host.session::adjustPlayerDesiredCount
        ));
        crossSurfaceDropOnHostSlotEmitter = add(WorkspaceActionId.CROSS_SURFACE_DROP_ON_HOST_SLOT, RPCEventBuilder.simple(
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // hostSlotIndex
                host.session::crossSurfaceDropOnHostSlot
        ));
        crossSurfaceQuickMoveAtlasEmitter = add(WorkspaceActionId.CROSS_SURFACE_QUICK_MOVE_ATLAS, RPCEventBuilder.simple(
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // count of stacks to quick-move
                host.session::crossSurfaceQuickMoveAtlas
        ));
        crossSurfaceQuickMoveHotbarEmitter = add(WorkspaceActionId.CROSS_SURFACE_QUICK_MOVE_HOTBAR, RPCEventBuilder.simple(
                Integer.class,   // hotbarIndex
                host.session::crossSurfaceQuickMoveHotbar
        ));
        pickupToCursorEmitter = add(WorkspaceActionId.PICKUP_TO_CURSOR, RPCEventBuilder.simple(
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // count cap (Integer.MAX_VALUE for full)
                host.session::pickupToCursor
        ));
        cursorCancelEmitter = add(WorkspaceActionId.CURSOR_CANCEL, RPCEventBuilder.simple(
                (Runnable) host.session::cursorCancel
        ));
        cursorSmartDepositEmitter = add(WorkspaceActionId.CURSOR_SMART_DEPOSIT, RPCEventBuilder.simple(
                (Runnable) host.session::cursorSmartDeposit
        ));
        dropCursorIntoChestEmitter = add(WorkspaceActionId.DROP_CURSOR_INTO_CHEST, RPCEventBuilder.simple(
                String.class,
                host.session::dropCursorIntoChest
        ));
        dropCursorAtHotbarEmitter = add(WorkspaceActionId.DROP_CURSOR_AT_HOTBAR, RPCEventBuilder.simple(
                Integer.class,
                Integer.class,
                host.session::dropCursorAtHotbar
        ));
    }

    void sendUndo() {
        if (actionEmitters.containsKey(WorkspaceActionId.UNDO)) {
            host.localStatus.set("undo");
            send(WorkspaceActionId.UNDO);
        }
    }

    void sendRedo() {
        if (actionEmitters.containsKey(WorkspaceActionId.REDO)) {
            host.localStatus.set("redo");
            send(WorkspaceActionId.REDO);
        }
    }

    void sendDepositCarriedToChest(SlotWorkspaceViewModel.IdentityRef identity, String storageId) {
        if (depositCarriedToChestEmitter == null || identity == null || storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = send(WorkspaceActionId.DEPOSIT_CARRIED_TO_CHEST,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                storageId
        );
        if (!sent) {
            host.localStatus.set("deposit unavailable");
            host.rebuild();
        }
    }

    void sendDepositHotbarToChest(int hotbarIndex, String storageId) {
        if (depositHotbarToChestEmitter == null || storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = send(WorkspaceActionId.DEPOSIT_HOTBAR_TO_CHEST, hotbarIndex, storageId);
        if (!sent) {
            host.localStatus.set("deposit unavailable");
            host.rebuild();
        }
    }

    void sendTakeFromChest(String storageId, int chestSlotIndex) {
        if (takeFromChestEmitter == null || storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = send(WorkspaceActionId.TAKE_FROM_CHEST, storageId, chestSlotIndex);
        if (!sent) {
            host.localStatus.set("take unavailable");
            host.rebuild();
        }
    }

    void sendLootChestTakeAll(SlotWorkspaceViewModel.LootChestPanel panel) {
        if (lootChestTakeAllEmitter == null || panel == null || !panel.isPresent()) {
            return;
        }
        boolean sent = send(WorkspaceActionId.LOOT_CHEST_TAKE_ALL,
                panel.dimensionId(),
                panel.chestX(),
                panel.chestY(),
                panel.chestZ()
        );
        if (!sent) {
            host.localStatus.set("loot take-all unavailable");
            host.rebuild();
        } else {
            host.localStatus.set("loot take-all…");
        }
    }

    void sendRenameCluster(String clusterId, String label) {
        if (renameClusterEmitter == null || clusterId == null || clusterId.isBlank()) {
            return;
        }
        boolean sent = send(WorkspaceActionId.RENAME_CLUSTER, clusterId, label == null ? "" : label);
        if (!sent) {
            host.localStatus.set("rename cluster unavailable");
            host.rebuild();
        }
    }

    void sendLootChestTakeIdentity(
            SlotWorkspaceViewModel.LootChestPanel panel,
            SlotWorkspaceViewModel.AtlasItem item
    ) {
        if (lootChestTakeIdentityEmitter == null || panel == null || !panel.isPresent() || item == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.LOOT_CHEST_TAKE_IDENTITY,
                panel.dimensionId(),
                panel.chestX(),
                panel.chestY(),
                panel.chestZ(),
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        if (!sent) {
            host.localStatus.set("loot take unavailable");
            host.rebuild();
        }
    }

    /**
     * Mirror the local search query into the server's session so the
     * next view-model projection can synthesize remote-only ghosts only
     * for matching identities. Server stores the value and short-circuits
     * the broadcast when it hasn't changed, so spamming this on every
     * keystroke is fine.
     */
    void sendSearchQuery(String query) {
        if (setSearchQueryEmitter == null) {
            return;
        }
        send(WorkspaceActionId.SET_SEARCH_QUERY, query == null ? "" : query);
    }

    void sendLootChestClaimAndDeposit(
            SlotWorkspaceViewModel.LootChestPanel panel,
            SlotWorkspaceViewModel.IdentityRef identity
    ) {
        if (lootChestClaimAndDepositEmitter == null || panel == null || !panel.isPresent() || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.LOOT_CHEST_CLAIM_AND_DEPOSIT,
                panel.dimensionId(),
                panel.chestX(),
                panel.chestY(),
                panel.chestZ(),
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint()
        );
        if (!sent) {
            host.localStatus.set("claim & deposit unavailable");
            host.rebuild();
        } else {
            host.localStatus.set("claiming chest…");
        }
    }

    void sendLootChestOpenVanilla(SlotWorkspaceViewModel.LootChestPanel panel) {
        if (lootChestOpenVanillaEmitter == null || panel == null || !panel.isPresent()) {
            return;
        }
        boolean sent = send(WorkspaceActionId.LOOT_CHEST_OPEN_VANILLA,
                panel.dimensionId(),
                panel.chestX(),
                panel.chestY(),
                panel.chestZ()
        );
        if (!sent) {
            host.localStatus.set("open vanilla unavailable");
            host.rebuild();
        } else {
            host.localStatus.set("opening vanilla chest…");
        }
    }

    void sendChipAccept(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion chip) {
        if (acceptChipEmitter == null) {
            return;
        }
        String templateName = chip.template() == null ? "" : chip.template().name();
        // Move only the clicked item. The previous batch-apply semantic
        // (move every atlas + triage item with a matching chip target)
        // looked like a runaway from the player's perspective: clicking
        // one Tools chip emptied half the inbox at once. Single-item is
        // the predictable mental model — one click, one move.
        send(WorkspaceActionId.ACCEPT_CHIP,
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint(),
                chip.islandId(),
                templateName
        );
        host.localStatus.set("accepting chip: " + chip.label());
        host.rebuild();
    }

    void sendDuplicateKit(String kitId) {
        boolean sent = send(WorkspaceActionId.DUPLICATE_KIT, kitId);
        host.localStatus.set(sent ? "duplicating workflow..." : "duplicate unavailable");
        host.rebuild();
    }

    void sendReorderKit(String kitId, int targetIndex) {
        boolean sent = send(WorkspaceActionId.REORDER_KIT, kitId, Math.max(0, targetIndex));
        host.localStatus.set(sent ? "moving workflow..." : "move workflow unavailable");
        host.rebuild();
    }

    void sendCreateKitVariant(String parentKitId) {
        boolean sent = send(WorkspaceActionId.CREATE_KIT_VARIANT, parentKitId, "");
        host.localStatus.set(sent ? "creating workflow variant..." : "variant unavailable");
        host.rebuild();
    }

    void sendCreateWorkflowTab() {
        boolean sent = send(WorkspaceActionId.CREATE_WORKFLOW_TAB, "");
        host.localStatus.set(sent ? "creating workflow..." : "create workflow unavailable");
        host.rebuild();
    }

    void sendSaveKit() {
        boolean sent = send(WorkspaceActionId.SAVE_KIT, "");
        host.localStatus.set(sent ? "saving workflow..." : "save workflow unavailable");
        host.rebuild();
    }

    void sendActivateKit(String kitId) {
        boolean sent = send(WorkspaceActionId.ACTIVATE_KIT, kitId);
        host.localStatus.set(sent ? "activating workflow..." : "activate workflow unavailable");
        host.rebuild();
    }

    void sendDeactivateKit() {
        boolean sent = send(WorkspaceActionId.DEACTIVATE_KIT);
        host.localStatus.set(sent ? "deactivating workflow..." : "deactivate workflow unavailable");
        host.rebuild();
    }

    void sendDeleteKit(String kitId) {
        boolean sent = send(WorkspaceActionId.DELETE_KIT, kitId);
        host.localStatus.set(sent ? "deleting workflow..." : "delete workflow unavailable");
        host.rebuild();
    }

    void sendSwitchKitPage(int direction) {
        boolean sent = send(WorkspaceActionId.SWITCH_KIT_PAGE, direction);
        host.localStatus.set(sent ? "switching workflow page..." : "page switch unavailable");
        host.rebuild();
    }

    void sendAddKitPage(String kitId) {
        boolean sent = send(WorkspaceActionId.ADD_KIT_PAGE, kitId);
        host.localStatus.set(sent ? "adding workflow page..." : "add page unavailable");
        host.rebuild();
    }

    void sendRemoveKitPage(String kitId, int pageIndex) {
        boolean sent = send(WorkspaceActionId.REMOVE_KIT_PAGE, kitId, pageIndex);
        host.localStatus.set(sent ? "removing workflow page..." : "remove page unavailable");
        host.rebuild();
    }

    void sendSetKitMember(String kitId, SlotWorkspaceViewModel.IdentityRef identity, boolean member) {
        if (kitId == null || kitId.isBlank() || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.SET_KIT_MEMBER,
                kitId,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                member ? 1 : 0);
        host.localStatus.set(sent
                ? (member ? "added to workflow" : "removed from workflow")
                : "workflow membership unavailable");
        host.rebuild();
    }

    void sendSetKitAcceptedInput(String kitId, WorkflowAcceptedInputRule rule, boolean accepted) {
        if (kitId == null || kitId.isBlank() || rule == null) {
            return;
        }
        SlotWorkspaceViewModel.IdentityRef identity = rule.identity() == null
                ? new SlotWorkspaceViewModel.IdentityRef("", "", "")
                : SlotWorkspaceViewModel.IdentityRef.from(rule.identity());
        boolean sent = send(WorkspaceActionId.SET_KIT_ACCEPTED_INPUT,
                kitId,
                rule.kind().name(),
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                rule.tagId(),
                accepted ? 1 : 0);
        host.localStatus.set(sent
                ? (accepted ? "accepted workflow input" : "removed workflow input")
                : "workflow input update unavailable");
        host.rebuild();
    }

    /**
     * Set or clear a kit-scoped desired count. Used by the kit-rack bring
     * panel: dragging an item in calls this with count=1 to seed the
     * standing order; dragging out calls with count=0 to clear it. The
     * legacy add/remove-bring RPC pair was retired in favour of this
     * count-aware setter so the bring concept lives entirely on top of
     * the desired-counts machinery.
     */
    void sendSetKitScopedDesiredCount(String kitId, SlotWorkspaceViewModel.IdentityRef identity, int count) {
        if (kitId == null || kitId.isBlank() || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.SET_KIT_SCOPED_DESIRED_COUNT,
                kitId,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                count);
        host.localStatus.set(sent
                ? (count > 0 ? "workflow target updated" : "workflow target cleared")
                : "workflow target unavailable");
        host.rebuild();
    }

    void sendSwapKitSlots(String kitId, int pageIndex, int fromIndex, int toIndex) {
        boolean sent = send(WorkspaceActionId.SWAP_KIT_SLOTS, kitId, pageIndex, fromIndex, toIndex);
        host.localStatus.set(sent ? "swapping workflow slots..." : "swap slots unavailable");
        host.rebuild();
    }

    void sendSetKitSlotIdentity(String kitId, int pageIndex, int slotIndex, SlotWorkspaceViewModel.IdentityRef identity) {
        String itemId = identity == null ? "" : identity.itemId();
        String comparisonMode = identity == null ? "" : identity.comparisonMode();
        String fingerprint = identity == null ? "" : identity.componentFingerprint();
        boolean sent = send(WorkspaceActionId.SET_KIT_SLOT_IDENTITY,
                kitId, pageIndex, slotIndex, itemId, comparisonMode, fingerprint);
        host.localStatus.set(sent ? "updating workflow slot..." : "update slot unavailable");
        host.rebuild();
    }

    /**
     * Cross-surface: a wall card was drag-released over a vanilla slot
     * in the host menu. Server picks a player-inventory slot containing
     * the identity and synthesizes vanilla PICKUP source → PICKUP target
     * so the host menu's slot rules govern the move.
     */
    void sendCrossSurfaceDropOnHostSlot(SlotWorkspaceViewModel.IdentityRef identity, int hostSlotIndex) {
        if (crossSurfaceDropOnHostSlotEmitter == null || identity == null || hostSlotIndex < 0) {
            return;
        }
        boolean sent = send(WorkspaceActionId.CROSS_SURFACE_DROP_ON_HOST_SLOT,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                hostSlotIndex
        );
        host.localStatus.set(sent ? "dropping on host slot..." : "drop unavailable");
        host.rebuild();
    }

    /**
     * Cross-surface: shift+click or shift+wheel-up on a wall card while
     * the sidebar is mounted. Server runs vanilla quick-move on a
     * player-inventory slot carrying the identity, repeated up to
     * {@code count} times so multiple stacks fan into the host menu's
     * accepting slots.
     */
    /**
     * Universal cancel: route the cursor stack back to its origin (or
     * smart-deposit cascade if origin can't accept). Bound to the
     * root-level right-click handler when carrying.
     */
    void sendCursorCancel() {
        if (cursorCancelEmitter == null) {
            return;
        }
        stashLastDropped();
        send(WorkspaceActionId.CURSOR_CANCEL);
    }

    /**
     * Smart-deposit: route the cursor stack through the deposit cascade
     * (desired/wanted-count gap → proximate chest with affinity or matching
     * contents → home → Triage). Bound to the root-level left-click
     * handler when carrying and no specific drop target handles the click.
     */
    void sendCursorSmartDeposit() {
        if (cursorSmartDepositEmitter == null) {
            return;
        }
        stashLastDropped();
        send(WorkspaceActionId.CURSOR_SMART_DEPOSIT);
    }

    /**
     * Click a player-hotbar slot via vanilla {@code menu.clicked}. With a
     * non-empty cursor, left = drop-all/merge/swap and right = drop-one; with
     * an empty cursor, left picks the slot up onto the real menu cursor.
     */
    void sendDropCursorAtHotbar(int hotbarIndex, int button) {
        if (dropCursorAtHotbarEmitter == null || hotbarIndex < 0 || hotbarIndex >= 9) {
            return;
        }
        if (WorkspaceCursorState.carriedIdentity() == null) {
            host.lastDroppedIdentity = null;
        } else {
            stashLastDropped();
        }
        boolean sent = send(WorkspaceActionId.DROP_CURSOR_AT_HOTBAR, hotbarIndex, button);
        if (!sent) {
            host.localStatus.set("drop unavailable");
            host.rebuild();
        }
    }

    /**
     * Drop the cursor stack directly into a specific chest. Used by
     * left-click on a chest chip / loot chest panel row when carrying.
     */
    void sendDropCursorIntoChest(String storageId) {
        if (dropCursorIntoChestEmitter == null || storageId == null || storageId.isBlank()) {
            return;
        }
        stashLastDropped();
        boolean sent = send(WorkspaceActionId.DROP_CURSOR_INTO_CHEST, storageId);
        if (!sent) {
            host.localStatus.set("drop unavailable");
            host.rebuild();
        }
    }

    /**
     * Snapshot the cursor's identity right before any drop / cancel RPC
     * fires so the wall card chrome can keep the dropped identity
     * highlighted after the cursor goes empty.
     */
    private void stashLastDropped() {
        SlotWorkspaceViewModel.IdentityRef cursorId = WorkspaceCursorState.carriedIdentity();
        if (cursorId != null) {
            host.lastDroppedIdentity = cursorId;
        }
    }

    /**
     * Eager extract the identity onto {@code menu.getCarried()} via the
     * server's ranked source resolution (carry → backpacks → proximate
     * chests by affinity). {@code count} caps the amount; pass
     * {@link Integer#MAX_VALUE} for "as much as fits."
     */
    void sendPickupToCursor(SlotWorkspaceViewModel.IdentityRef identity, int count) {
        if (pickupToCursorEmitter == null || identity == null || count <= 0) {
            return;
        }
        // A fresh pickup overrides last-dropped — the new cursor identity
        // is now the active chrome source.
        host.lastDroppedIdentity = null;
        boolean sent = send(WorkspaceActionId.PICKUP_TO_CURSOR,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                count
        );
        if (!sent) {
            host.localStatus.set("pickup unavailable");
            host.rebuild();
        }
    }

    void sendCrossSurfaceQuickMove(SlotWorkspaceViewModel.IdentityRef identity, int count) {
        if (crossSurfaceQuickMoveAtlasEmitter == null || identity == null || count <= 0) {
            return;
        }
        boolean sent = send(WorkspaceActionId.CROSS_SURFACE_QUICK_MOVE_ATLAS,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                count
        );
        host.localStatus.set(sent ? "quick-moving to host..." : "host quick-move unavailable");
        host.rebuild();
    }

    void sendCrossSurfaceQuickMoveHotbar(int hotbarIndex) {
        if (crossSurfaceQuickMoveHotbarEmitter == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.CROSS_SURFACE_QUICK_MOVE_HOTBAR, hotbarIndex);
        host.localStatus.set(sent ? "quick-moving to host..." : "host quick-move unavailable");
        host.rebuild();
    }

    void sendTransfer(int sourceKind, int sourceIndex, int destinationKind, int destinationIndex) {
        boolean sent = send(WorkspaceActionId.TRANSFER,
                sourceKind,
                sourceIndex,
                destinationKind,
                destinationIndex,
                "slot_workspace.ldlib.hotbar_transfer"
        );
        host.localStatus.set(sent ? "transfer requested" : "transfer unavailable");
        host.rebuild();
    }

    /**
     * Send a home-assignment with an explicit insert position.
     *
     * <p>{@code ordinal} is the user-perspective slot in
     * {@code islandId}: pass the ordinal of the item the user dropped
     * onto (the new entry shoves it +1) or {@code null} to append at the
     * end. Triage assignments ignore ordinal — see
     * {@link SlotWorkspaceCommandService#applyHomeDrop}.
     */
    void sendAssignHome(
            SlotWorkspaceViewModel.IdentityRef identity,
            String islandId,
            Integer ordinal
    ) {
        if (identity == null || islandId == null || islandId.isBlank()) {
            host.localStatus.set("invalid home target");
            host.rebuild();
            return;
        }
        boolean sent = send(WorkspaceActionId.ASSIGN_HOME,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                islandId,
                ordinal
        );
        if (sent) {
            host.rememberRehomeTarget(islandId);
        }
        host.localStatus.set(sent ? "home assignment requested" : "home assignment unavailable");
        host.rebuild();
    }

    void sendReturnHotbarToHome(int hotbarIndex) {
        if (returnHotbarToHomeEmitter == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.RETURN_HOTBAR_TO_HOME, hotbarIndex);
        if (!sent) {
            host.localStatus.set("return-to-home unavailable");
            host.rebuild();
        }
    }

    void sendAssignHomeToFreeHotbar(SlotWorkspaceViewModel.AtlasItem item) {
        if (assignHomeToFreeHotbarEmitter == null || item == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.ASSIGN_HOME_TO_FREE_HOTBAR,
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        if (!sent) {
            host.localStatus.set("assign-to-hotbar unavailable");
            host.rebuild();
        }
    }

    void sendAssignHomeToHotbarOnly(SlotWorkspaceViewModel.AtlasItem item) {
        if (assignHomeToHotbarOnlyEmitter == null || item == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.ASSIGN_HOME_TO_HOTBAR_ONLY,
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        if (!sent) {
            host.localStatus.set("assign-to-hotbar unavailable");
            host.rebuild();
        }
    }

    void sendAssignToAutoHotbar(SlotWorkspaceViewModel.AtlasItem item) {
        if (assignIdentityToAutoHotbarEmitter == null || item == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.ASSIGN_IDENTITY_TO_AUTO_HOTBAR,
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        host.localStatus.set(sent ? "moving to hotbar" : "assign-to-hotbar unavailable");
        if (!sent) {
            host.rebuild();
        }
    }

    void sendAssignToHotbarSlot(SlotWorkspaceViewModel.AtlasItem item, int hotbarIndex) {
        if (assignIdentityToHotbarSlotEmitter == null || item == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.ASSIGN_IDENTITY_TO_HOTBAR_SLOT,
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint(),
                hotbarIndex
        );
        host.localStatus.set(sent ? "transfer requested" : "transfer unavailable");
    }

    void sendDepositHomeToLinkedChest(SlotWorkspaceViewModel.AtlasItem item) {
        if (depositHomeToLinkedChestEmitter == null || item == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.DEPOSIT_HOME_TO_LINKED_CHEST,
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        if (!sent) {
            host.localStatus.set("deposit unavailable");
            host.rebuild();
        }
    }

    void sendDepositOneHomeToLinkedChest(SlotWorkspaceViewModel.AtlasItem item) {
        if (depositOneHomeToLinkedChestEmitter == null || item == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.DEPOSIT_ONE_HOME_TO_LINKED_CHEST,
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        if (!sent) {
            host.localStatus.set("deposit unavailable");
            host.rebuild();
        }
    }

    void sendTakeOneFromChest(String storageId, int chestSlotIndex) {
        if (takeOneFromChestEmitter == null || storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = send(WorkspaceActionId.TAKE_ONE_FROM_CHEST, storageId, chestSlotIndex);
        if (!sent) {
            host.localStatus.set("take unavailable");
            host.rebuild();
        }
    }

    void sendTakeOneByIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
        if (takeOneByIdentityEmitter == null || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.TAKE_ONE_BY_IDENTITY,
                identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
        if (!sent) {
            host.localStatus.set("take unavailable");
            host.rebuild();
        }
    }

    void sendTakeStackByIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
        if (takeStackByIdentityEmitter == null || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.TAKE_STACK_BY_IDENTITY,
                identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
        if (!sent) {
            host.localStatus.set("take unavailable");
            host.rebuild();
        }
    }

    void sendTakeDesiredGapOrStackByIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
        if (takeDesiredGapOrStackByIdentityEmitter == null || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.TAKE_DESIRED_GAP_OR_STACK_BY_IDENTITY,
                identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
        if (!sent) {
            host.localStatus.set("take unavailable");
            host.rebuild();
        }
    }

    void sendToggleWantedItem(SlotWorkspaceViewModel.IdentityRef identity) {
        if (toggleWantedItemEmitter == null || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.TOGGLE_WANTED_ITEM,
                identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
        host.localStatus.set(sent ? "wanted item updated" : "wanted item unavailable");
        host.rebuild();
    }

    void sendSetWantedCount(SlotWorkspaceViewModel.IdentityRef identity, int count) {
        if (setWantedCountEmitter == null || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.SET_WANTED_COUNT,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                Math.max(0, count)
        );
        host.localStatus.set(sent ? "wanted count updated" : "wanted count update unavailable");
        host.rebuild();
    }

    void sendAdjustWantedCount(SlotWorkspaceViewModel.IdentityRef identity, int delta) {
        if (adjustWantedCountEmitter == null || identity == null || delta == 0) {
            return;
        }
        boolean sent = send(WorkspaceActionId.ADJUST_WANTED_COUNT,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                delta
        );
        if (!sent) {
            host.localStatus.set("wanted count update unavailable");
            host.rebuild();
        }
    }

    void sendSetJunk(SlotWorkspaceViewModel.IdentityRef identity, boolean marked) {
        if (setJunkEmitter == null || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.SET_JUNK,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                marked ? 1 : 0);
        host.localStatus.set(sent ? (marked ? "marked as junk" : "unmarked junk") : "junk update unavailable");
        host.rebuild();
    }

    void sendTrashIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
        if (trashIdentityEmitter == null || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.TRASH_IDENTITY,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint());
        host.localStatus.set(sent ? "trashing carried item" : "trash unavailable");
        host.rebuild();
    }

    void sendSelectCraftRunIngredient(
            String entryId,
            String groupId,
            SlotWorkspaceViewModel.IdentityRef identity
    ) {
        if (craftRunSelectIngredientEmitter == null || entryId == null || groupId == null || identity == null) {
            return;
        }
        boolean sent = send(
                WorkspaceActionId.CRAFT_RUN_SELECT_INGREDIENT,
                entryId,
                groupId,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint());
        host.localStatus.set(sent ? "recipe ingredient selected" : "ingredient selection unavailable");
        host.rebuild();
    }

    void sendMoveHotbarToAtlas(int hotbarIndex, String islandId, Integer ordinal) {
        boolean sent = send(WorkspaceActionId.MOVE_HOTBAR_TO_ATLAS,
                hotbarIndex,
                islandId,
                ordinal
        );
        host.localStatus.set(sent ? "return to atlas requested" : "return to atlas unavailable");
        host.rebuild();
    }

    void sendMoveIsland(String islandId, double worldX, double worldY) {
        if (islandId == null || islandId.isBlank()) {
            host.localStatus.set("invalid island move");
            host.rebuild();
            return;
        }
        boolean sent = send(WorkspaceActionId.MOVE_ISLAND,
                islandId,
                worldX,
                worldY
        );
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] sendMoveIsland id={} worldX={} worldY={} sent={}",
                islandId, worldX, worldY, sent);
        host.localStatus.set(sent ? "island move requested" : "island move unavailable");
        host.rebuild();
    }

    void sendReorderIsland(String islandId, int targetIndex) {
        if (islandId == null || islandId.isBlank()) {
            host.localStatus.set("invalid section reorder");
            host.rebuild();
            return;
        }
        boolean sent = send(WorkspaceActionId.REORDER_ISLAND,
                islandId,
                Math.max(0, targetIndex)
        );
        host.localStatus.set(sent ? "section reorder requested" : "section reorder unavailable");
        host.rebuild();
    }

    void sendTakeAll(String storageId) {
        boolean sent = send(WorkspaceActionId.TAKE_ALL_FROM_CHEST, storageId);
        host.localStatus.set(sent ? "take-all requested" : "take-all unavailable");
        host.rebuild();
    }

    void sendDeposit() {
        boolean sent = send(WorkspaceActionId.DEPOSIT);
        dev.imagio.slot.SlotCommon.LOGGER.info(
                "[SLOT] deposit RPC send: emitterPresent={} sent={}",
                depositEmitter != null, sent);
        host.localStatus.set(sent ? "deposit requested" : "deposit unavailable (no RPC emitter)");
        host.rebuild();
    }

    /**
     * Claim the chest at the given world position. Only sent from the
     * active-chest strip when the chest the player is currently viewing
     * is unclaimed; the server resolves the BlockPos to a ChestAnchor
     * and runs the same {@code autoClaimByAnchor} path used by the
     * deposit observer.
     */
    void sendClaimChestAt(String dimensionId, int x, int y, int z) {
        if (claimChestAtPosEmitter == null || dimensionId == null || dimensionId.isBlank()) {
            host.localStatus.set("claim unavailable");
            host.rebuild();
            return;
        }
        boolean sent = send(WorkspaceActionId.CLAIM_CHEST_AT_POS, dimensionId, x, y, z);
        host.localStatus.set(sent ? "chest claimed" : "claim unavailable");
        host.rebuild();
    }

    void sendSetChestRoleAt(SlotWorkspaceViewModel.ActiveChestPanel panel, ChestRole role) {
        if (panel == null || !panel.isPresent() || role == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.SET_CHEST_ROLE_AT_POS,
                panel.dimensionId(), panel.posX(), panel.posY(), panel.posZ(), role.name());
        host.localStatus.set(sent ? "chest role: " + role.displayLabel() : "role unavailable");
        host.rebuild();
    }

    void sendForgetItemAffinity(String storageId, SlotWorkspaceViewModel.IdentityRef identity) {
        if (storageId == null || storageId.isBlank() || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.FORGET_ITEM_AFFINITY,
                storageId,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint()
        );
        host.localStatus.set(sent ? "affinity forgotten" : "forget unavailable");
        host.rebuild();
    }

    void sendMoveChest(String storageId, int atlasX, int atlasY) {
        if (storageId == null || storageId.isBlank()) {
            host.localStatus.set("invalid chest move");
            host.rebuild();
            return;
        }
        boolean sent = send(WorkspaceActionId.MOVE_CHEST,
                storageId,
                atlasX,
                atlasY
        );
        host.localStatus.set(sent ? "chest move requested" : "chest move unavailable");
        host.rebuild();
    }

    void sendSetPlayerDesiredCount(SlotWorkspaceViewModel.IdentityRef identity, int count) {
        if (setPlayerDesiredCountEmitter == null || identity == null) {
            return;
        }
        boolean sent = send(WorkspaceActionId.SET_PLAYER_DESIRED_COUNT,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                count
        );
        if (!sent) {
            host.localStatus.set("desired count update unavailable");
            host.rebuild();
        }
    }

    void sendAdjustPlayerDesiredCount(SlotWorkspaceViewModel.IdentityRef identity, int delta) {
        if (adjustPlayerDesiredCountEmitter == null || identity == null || delta == 0) {
            return;
        }
        boolean sent = send(WorkspaceActionId.ADJUST_PLAYER_DESIRED_COUNT,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                delta
        );
        if (!sent) {
            host.localStatus.set("desired count update unavailable");
            host.rebuild();
        }
    }

}
