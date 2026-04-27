package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;

final class WorkspaceRpcDispatcher {
    private final SlotWorkspaceUiController host;

    RPCEmitter transferEmitter;
    RPCEmitter homeEmitter;
    RPCEmitter createNamedIslandEmitter;
    RPCEmitter hotbarToAtlasEmitter;
    RPCEmitter moveIslandEmitter;
    RPCEmitter moveChestEmitter;
    RPCEmitter moveStorageZoneEmitter;
    RPCEmitter relabelChestEmitter;
    RPCEmitter linkChestEmitter;
    RPCEmitter unlinkChestEmitter;
    RPCEmitter depositEmitter;
    RPCEmitter takeAllEmitter;
    RPCEmitter renameIslandEmitter;
    RPCEmitter recolorIslandEmitter;
    RPCEmitter setIslandIconEmitter;
    RPCEmitter deleteIslandEmitter;
    RPCEmitter acceptChipEmitter;
    RPCEmitter saveKitEmitter;
    RPCEmitter activateKitEmitter;
    RPCEmitter deactivateKitEmitter;
    RPCEmitter undoEmitter;
    RPCEmitter redoEmitter;
    RPCEmitter deleteKitEmitter;
    RPCEmitter switchKitPageEmitter;
    RPCEmitter addKitPageEmitter;
    RPCEmitter removeKitPageEmitter;
    RPCEmitter addKitBringEmitter;
    RPCEmitter removeKitBringEmitter;
    RPCEmitter setKitSlotIdentityEmitter;
    RPCEmitter renameKitEmitter;
    RPCEmitter duplicateKitEmitter;
    RPCEmitter swapKitSlotsEmitter;
    RPCEmitter returnHotbarToHomeEmitter;
    RPCEmitter assignHomeToFreeHotbarEmitter;
    RPCEmitter depositCarriedToChestEmitter;
    RPCEmitter depositHotbarToChestEmitter;
    RPCEmitter takeFromChestEmitter;
    RPCEmitter takeOneFromChestEmitter;
    RPCEmitter assignHomeToHotbarOnlyEmitter;
    RPCEmitter assignIdentityToHotbarSlotEmitter;
    RPCEmitter depositHomeToLinkedChestEmitter;
    RPCEmitter depositOneHomeToLinkedChestEmitter;

    WorkspaceRpcDispatcher(SlotWorkspaceUiController host) {
        this.host = host;
    }

    void register() {
        transferEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                Integer.class,
                Integer.class,
                Integer.class,
                Integer.class,
                String.class,
                host.session::transfer
        ));
        homeEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::assignHome
        ));
        createNamedIslandEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                host.session::createNamedIslandForItem
        ));
        hotbarToAtlasEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                Integer.class,
                String.class,
                Integer.class,
                host.session::moveHotbarToAtlas
        ));
        moveIslandEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                host.session::moveIsland
        ));
        moveChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                host.session::moveChest
        ));
        moveStorageZoneEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                Integer.class,
                Integer.class,
                host.session::moveStorageZone
        ));
        relabelChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::relabelChest
        ));
        linkChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::linkIslandToChest
        ));
        unlinkChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::unlinkIslandFromChest
        ));
        depositEmitter = host.root.addRPCEvent(RPCEventBuilder.simple((Runnable) host.session::deposit));
        takeAllEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::takeAllFromChest
        ));
        renameIslandEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::renameIsland
        ));
        recolorIslandEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::recolorIsland
        ));
        setIslandIconEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::setIslandIcon
        ));
        deleteIslandEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::deleteIsland
        ));
        acceptChipEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::acceptChip
        ));
        saveKitEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::saveBeltAsKit
        ));
        activateKitEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::activateKit
        ));
        deactivateKitEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                (Runnable) host.session::deactivateKit
        ));
        undoEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                (Runnable) host.session::performUndo
        ));
        redoEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                (Runnable) host.session::performRedo
        ));
        deleteKitEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::deleteKit
        ));
        switchKitPageEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                Integer.class,
                host.session::switchKitPage
        ));
        addKitPageEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::addKitPage
        ));
        removeKitPageEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::removeKitPage
        ));
        addKitBringEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::addKitBring
        ));
        removeKitBringEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::removeKitBring
        ));
        setKitSlotIdentityEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                host.session::setKitSlotIdentity
        ));
        renameKitEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::renameKit
        ));
        duplicateKitEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::duplicateKit
        ));
        swapKitSlotsEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                host.session::swapKitSlots
        ));
        returnHotbarToHomeEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                Integer.class,
                host.session::returnHotbarToHome
        ));
        assignHomeToFreeHotbarEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::assignHomeToFreeHotbar
        ));
        depositCarriedToChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::depositCarriedToChest
        ));
        depositHotbarToChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                Integer.class,
                String.class,
                host.session::depositHotbarToChest
        ));
        takeFromChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::takeFromChest
        ));
        takeOneFromChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::takeOneFromChest
        ));
        assignHomeToHotbarOnlyEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::assignHomeToHotbarOnly
        ));
        assignIdentityToHotbarSlotEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                Integer.class,
                host.session::assignIdentityToHotbarSlot
        ));
        depositHomeToLinkedChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::depositHomeToLinkedChest
        ));
        depositOneHomeToLinkedChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::depositOneHomeToLinkedChest
        ));
    }

    void sendUndo() {
        if (undoEmitter != null) {
            host.localStatus.set("undo");
            undoEmitter.send();
        }
    }

    void sendRedo() {
        if (redoEmitter != null) {
            host.localStatus.set("redo");
            redoEmitter.send();
        }
    }

    void sendDepositCarriedToChest(SlotWorkspaceViewModel.IdentityRef identity, String storageId) {
        if (depositCarriedToChestEmitter == null || identity == null || storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = depositCarriedToChestEmitter.send(
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
        boolean sent = depositHotbarToChestEmitter.send(hotbarIndex, storageId);
        if (!sent) {
            host.localStatus.set("deposit unavailable");
            host.rebuild();
        }
    }

    void sendTakeFromChest(String storageId, int chestSlotIndex) {
        if (takeFromChestEmitter == null || storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = takeFromChestEmitter.send(storageId, chestSlotIndex);
        if (!sent) {
            host.localStatus.set("take unavailable");
            host.rebuild();
        }
    }

    void sendChipAccept(SlotWorkspaceViewModel.AtlasItem item, ChipSuggestion chip) {
        if (acceptChipEmitter == null) {
            return;
        }
        host.selectedAtlasIdentity.set(item.identity());
        String templateName = chip.template() == null ? "" : chip.template().name();
        // Move only the clicked item. The previous batch-apply semantic
        // (move every atlas + triage item with a matching chip target)
        // looked like a runaway from the player's perspective: clicking
        // one Tools chip emptied half the inbox at once. Single-item is
        // the predictable mental model — one click, one move.
        acceptChipEmitter.send(
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
        boolean sent = duplicateKitEmitter != null && duplicateKitEmitter.send(kitId);
        host.localStatus.set(sent ? "duplicating kit..." : "duplicate unavailable");
        host.rebuild();
    }

    void sendSaveKit() {
        boolean sent = saveKitEmitter != null && saveKitEmitter.send("");
        host.localStatus.set(sent ? "saving kit..." : "save kit unavailable");
        host.rebuild();
    }

    void sendActivateKit(String kitId) {
        boolean sent = activateKitEmitter != null && activateKitEmitter.send(kitId);
        host.localStatus.set(sent ? "activating kit..." : "activate kit unavailable");
        host.rebuild();
    }

    void sendDeactivateKit() {
        boolean sent = deactivateKitEmitter != null && deactivateKitEmitter.send();
        host.localStatus.set(sent ? "deactivating kit..." : "deactivate kit unavailable");
        host.rebuild();
    }

    void sendDeleteKit(String kitId) {
        boolean sent = deleteKitEmitter != null && deleteKitEmitter.send(kitId);
        host.localStatus.set(sent ? "deleting kit..." : "delete kit unavailable");
        host.rebuild();
    }

    void sendSwitchKitPage(int direction) {
        boolean sent = switchKitPageEmitter != null && switchKitPageEmitter.send(direction);
        host.localStatus.set(sent ? "switching kit page..." : "page switch unavailable");
        host.rebuild();
    }

    void sendAddKitPage(String kitId) {
        boolean sent = addKitPageEmitter != null && addKitPageEmitter.send(kitId);
        host.localStatus.set(sent ? "adding kit page..." : "add page unavailable");
        host.rebuild();
    }

    void sendRemoveKitPage(String kitId, int pageIndex) {
        boolean sent = removeKitPageEmitter != null && removeKitPageEmitter.send(kitId, pageIndex);
        host.localStatus.set(sent ? "removing kit page..." : "remove page unavailable");
        host.rebuild();
    }

    void sendAddKitBring(String kitId, SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            return;
        }
        boolean sent = addKitBringEmitter != null && addKitBringEmitter.send(
                kitId, identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
        host.localStatus.set(sent ? "adding to bring..." : "add bring unavailable");
        host.rebuild();
    }

    void sendRemoveKitBring(String kitId, SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            return;
        }
        boolean sent = removeKitBringEmitter != null && removeKitBringEmitter.send(
                kitId, identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
        host.localStatus.set(sent ? "removing from bring..." : "remove bring unavailable");
        host.rebuild();
    }

    void sendSwapKitSlots(String kitId, int pageIndex, int fromIndex, int toIndex) {
        boolean sent = swapKitSlotsEmitter != null
                && swapKitSlotsEmitter.send(kitId, pageIndex, fromIndex, toIndex);
        host.localStatus.set(sent ? "swapping kit slots..." : "swap slots unavailable");
        host.rebuild();
    }

    void sendSetKitSlotIdentity(String kitId, int pageIndex, int slotIndex, SlotWorkspaceViewModel.IdentityRef identity) {
        String itemId = identity == null ? "" : identity.itemId();
        String comparisonMode = identity == null ? "" : identity.comparisonMode();
        String fingerprint = identity == null ? "" : identity.componentFingerprint();
        boolean sent = setKitSlotIdentityEmitter != null && setKitSlotIdentityEmitter.send(
                kitId, pageIndex, slotIndex, itemId, comparisonMode, fingerprint);
        host.localStatus.set(sent ? "updating kit slot..." : "update slot unavailable");
        host.rebuild();
    }

    void sendTransfer(int sourceKind, int sourceIndex, int destinationKind, int destinationIndex) {
        boolean sent = transferEmitter != null && transferEmitter.send(
                sourceKind,
                sourceIndex,
                destinationKind,
                destinationIndex,
                "slot_workspace.ldlib.hotbar_transfer"
        );
        host.localStatus.set(sent ? "transfer requested" : "transfer unavailable");
        host.selectedAtlasIdentity.set(null);
        host.selectedHotbarIndex.set(-1);
        host.rebuild();
    }

    void sendAssignHome(String islandId) {
        SlotWorkspaceViewModel.AtlasItem item = host.selectedAtlasItem();
        if (item == null) {
            host.localStatus.set("select an atlas item first");
            host.rebuild();
            return;
        }
        // Append at end-of-island when no spatial target was provided.
        sendAssignHome(item.identity(), islandId, null);
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
        boolean sent = homeEmitter != null && homeEmitter.send(
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
        host.selectedAtlasIdentity.set(null);
        host.selectedHotbarIndex.set(-1);
        host.rebuild();
    }

    void sendReturnHotbarToHome(int hotbarIndex) {
        if (returnHotbarToHomeEmitter == null) {
            return;
        }
        boolean sent = returnHotbarToHomeEmitter.send(hotbarIndex);
        if (!sent) {
            host.localStatus.set("return-to-home unavailable");
            host.rebuild();
        }
    }

    void sendAssignHomeToFreeHotbar(SlotWorkspaceViewModel.AtlasItem item) {
        if (assignHomeToFreeHotbarEmitter == null || item == null) {
            return;
        }
        boolean sent = assignHomeToFreeHotbarEmitter.send(
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
        boolean sent = assignHomeToHotbarOnlyEmitter.send(
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint()
        );
        if (!sent) {
            host.localStatus.set("assign-to-hotbar unavailable");
            host.rebuild();
        }
    }

    void sendAssignToHotbarSlot(SlotWorkspaceViewModel.AtlasItem item, int hotbarIndex) {
        if (assignIdentityToHotbarSlotEmitter == null || item == null) {
            return;
        }
        boolean sent = assignIdentityToHotbarSlotEmitter.send(
                item.identity().itemId(),
                item.identity().comparisonMode(),
                item.identity().componentFingerprint(),
                hotbarIndex
        );
        host.localStatus.set(sent ? "transfer requested" : "transfer unavailable");
        host.selectedAtlasIdentity.set(null);
        host.selectedHotbarIndex.set(-1);
        // No explicit host.rebuild here — atlas-card TICK picks up the
        // selection change next frame, and the server sync after the RPC
        // triggers its own host.rebuild. Calling host.rebuild() now produced a
        // noticeable blank-frame flash because the entire content tree
        // (header, body, statusBar) got torn down between the local
        // state update and the server's authoritative one.
    }

    void sendDepositHomeToLinkedChest(SlotWorkspaceViewModel.AtlasItem item) {
        if (depositHomeToLinkedChestEmitter == null || item == null) {
            return;
        }
        boolean sent = depositHomeToLinkedChestEmitter.send(
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
        boolean sent = depositOneHomeToLinkedChestEmitter.send(
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
        boolean sent = takeOneFromChestEmitter.send(storageId, chestSlotIndex);
        if (!sent) {
            host.localStatus.set("take unavailable");
            host.rebuild();
        }
    }

    void sendMoveHotbarToAtlas(int hotbarIndex, String islandId, Integer ordinal) {
        boolean sent = hotbarToAtlasEmitter != null && hotbarToAtlasEmitter.send(
                hotbarIndex,
                islandId,
                ordinal
        );
        host.localStatus.set(sent ? "return to atlas requested" : "return to atlas unavailable");
        host.selectedAtlasIdentity.set(null);
        host.selectedHotbarIndex.set(-1);
        host.rebuild();
    }

    void sendMoveIsland(String islandId, int worldX, int worldY) {
        if (islandId == null || islandId.isBlank()) {
            host.localStatus.set("invalid island move");
            host.rebuild();
            return;
        }
        boolean sent = moveIslandEmitter != null && moveIslandEmitter.send(
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

    void sendTakeAll(String storageId) {
        boolean sent = takeAllEmitter != null && takeAllEmitter.send(storageId);
        host.localStatus.set(sent ? "take-all requested" : "take-all unavailable");
        host.rebuild();
    }

    void sendDeposit() {
        boolean sent = depositEmitter != null && depositEmitter.send();
        host.localStatus.set(sent ? "deposit requested" : "deposit unavailable");
        host.rebuild();
    }

    void sendLinkChest(String islandId, String storageId) {
        if (islandId == null || islandId.isBlank() || storageId == null || storageId.isBlank()) {
            host.localStatus.set("invalid chest link");
            host.rebuild();
            return;
        }
        boolean sent = linkChestEmitter != null && linkChestEmitter.send(islandId, storageId);
        host.localStatus.set(sent ? "chest link requested" : "chest link unavailable");
        host.rebuild();
    }

    void sendUnlinkChest(String islandId, String storageId) {
        if (islandId == null || islandId.isBlank() || storageId == null || storageId.isBlank()) {
            host.localStatus.set("invalid chest unlink");
            host.rebuild();
            return;
        }
        boolean sent = unlinkChestEmitter != null && unlinkChestEmitter.send(islandId, storageId);
        host.localStatus.set(sent ? "chest unlink requested" : "chest unlink unavailable");
        host.rebuild();
    }

    void sendMoveStorageZone(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0) {
            return;
        }
        boolean sent = moveStorageZoneEmitter != null && moveStorageZoneEmitter.send(deltaX, deltaY);
        host.localStatus.set(sent ? "storage zone moved" : "storage zone move unavailable");
        host.rebuild();
    }

    void sendMoveChest(String storageId, int atlasX, int atlasY) {
        if (storageId == null || storageId.isBlank()) {
            host.localStatus.set("invalid chest move");
            host.rebuild();
            return;
        }
        boolean sent = moveChestEmitter != null && moveChestEmitter.send(
                storageId,
                atlasX,
                atlasY
        );
        host.localStatus.set(sent ? "chest move requested" : "chest move unavailable");
        host.rebuild();
    }

}
