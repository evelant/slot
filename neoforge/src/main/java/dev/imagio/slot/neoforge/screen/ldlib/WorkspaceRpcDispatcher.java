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
    RPCEmitter reorderIslandEmitter;
    RPCEmitter moveChestEmitter;
    RPCEmitter relabelChestEmitter;
    RPCEmitter forgetChestEmitter;
    RPCEmitter forgetItemAffinityEmitter;
    RPCEmitter depositEmitter;
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
    RPCEmitter activateKitEmitter;
    RPCEmitter deactivateKitEmitter;
    RPCEmitter undoEmitter;
    RPCEmitter redoEmitter;
    RPCEmitter deleteKitEmitter;
    RPCEmitter switchKitPageEmitter;
    RPCEmitter addKitPageEmitter;
    RPCEmitter removeKitPageEmitter;
    RPCEmitter setKitScopedDesiredCountEmitter;
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
    RPCEmitter takeOneByIdentityEmitter;
    RPCEmitter takeStackByIdentityEmitter;
    RPCEmitter assignHomeToHotbarOnlyEmitter;
    RPCEmitter assignIdentityToHotbarSlotEmitter;
    RPCEmitter depositHomeToLinkedChestEmitter;
    RPCEmitter depositOneHomeToLinkedChestEmitter;
    RPCEmitter setPlayerDesiredCountEmitter;
    RPCEmitter adjustPlayerDesiredCountEmitter;
    RPCEmitter crossSurfaceDropOnHostSlotEmitter;
    RPCEmitter crossSurfaceQuickMoveAtlasEmitter;
    RPCEmitter pickupToCursorEmitter;
    RPCEmitter cursorCancelEmitter;
    RPCEmitter cursorSmartDepositEmitter;
    RPCEmitter dropCursorIntoChestEmitter;
    RPCEmitter dropCursorAtHotbarEmitter;
    RPCEmitter claimChestAtPosEmitter;

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
                Double.class,
                Double.class,
                host.session::moveIsland
        ));
        reorderIslandEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                host.session::reorderIsland
        ));
        moveChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                host.session::moveChest
        ));
        relabelChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::relabelChest
        ));
        forgetChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::forgetChest
        ));
        claimChestAtPosEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                host.session::claimChestAtPos
        ));
        forgetItemAffinityEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                String.class,
                host.session::forgetItemAffinity
        ));
        depositEmitter = host.root.addRPCEvent(RPCEventBuilder.simple((Runnable) host.session::deposit));
        takeAllEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::takeAllFromChest
        ));
        lootChestTakeAllEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                host.session::takeAllFromLootChest
        ));
        lootChestTakeIdentityEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                host.session::takeIdentityFromLootChest
        ));
        lootChestOpenVanillaEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                host.session::openVanillaForLootChest
        ));
        lootChestClaimAndDepositEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                Integer.class,
                Integer.class,
                Integer.class,
                String.class,
                String.class,
                String.class,
                host.session::claimAndDepositCarriedToLootChest
        ));
        setSearchQueryEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::setSearchQuery
        ));
        renameClusterEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                host.session::renameCluster
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
        // Replaces the legacy addKitBring/removeKitBring pair: writes the
        // kit-scoped desired count for an explicit kitId, even when that
        // kit isn't the active one. The kit-rack UI uses count=1 for "add"
        // and count=0 for "remove."
        setKitScopedDesiredCountEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,    // kitId
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // count (0 = clear)
                host.session::setKitScopedDesiredCount
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
        takeOneByIdentityEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::takeOneByIdentity
        ));
        takeStackByIdentityEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                String.class,
                String.class,
                host.session::takeStackByIdentity
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
        setPlayerDesiredCountEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // count (0 = clear)
                host.session::setPlayerDesiredCount
        ));
        adjustPlayerDesiredCountEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // delta (signed, often ±1 from ctrl+scroll)
                host.session::adjustPlayerDesiredCount
        ));
        crossSurfaceDropOnHostSlotEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // hostSlotIndex
                host.session::crossSurfaceDropOnHostSlot
        ));
        crossSurfaceQuickMoveAtlasEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // count of stacks to quick-move
                host.session::crossSurfaceQuickMoveAtlas
        ));
        pickupToCursorEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,    // itemId
                String.class,    // comparisonMode
                String.class,    // componentFingerprint
                Integer.class,   // count cap (Integer.MAX_VALUE for full)
                host.session::pickupToCursor
        ));
        cursorCancelEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                (Runnable) host.session::cursorCancel
        ));
        cursorSmartDepositEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                (Runnable) host.session::cursorSmartDeposit
        ));
        dropCursorIntoChestEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                String.class,
                host.session::dropCursorIntoChest
        ));
        dropCursorAtHotbarEmitter = host.root.addRPCEvent(RPCEventBuilder.simple(
                Integer.class,
                Integer.class,
                host.session::dropCursorAtHotbar
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

    void sendLootChestTakeAll(SlotWorkspaceViewModel.LootChestPanel panel) {
        if (lootChestTakeAllEmitter == null || panel == null || !panel.isPresent()) {
            return;
        }
        boolean sent = lootChestTakeAllEmitter.send(
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
        boolean sent = renameClusterEmitter.send(clusterId, label == null ? "" : label);
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
        boolean sent = lootChestTakeIdentityEmitter.send(
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
        setSearchQueryEmitter.send(query == null ? "" : query);
    }

    void sendLootChestClaimAndDeposit(
            SlotWorkspaceViewModel.LootChestPanel panel,
            SlotWorkspaceViewModel.IdentityRef identity
    ) {
        if (lootChestClaimAndDepositEmitter == null || panel == null || !panel.isPresent() || identity == null) {
            return;
        }
        boolean sent = lootChestClaimAndDepositEmitter.send(
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
        boolean sent = lootChestOpenVanillaEmitter.send(
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
        boolean sent = setKitScopedDesiredCountEmitter != null && setKitScopedDesiredCountEmitter.send(
                kitId,
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                count);
        host.localStatus.set(sent
                ? (count > 0 ? "kit desired count updated" : "kit desired count cleared")
                : "kit desired count unavailable");
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
        boolean sent = crossSurfaceDropOnHostSlotEmitter.send(
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
        cursorCancelEmitter.send();
    }

    /**
     * Smart-deposit: route the cursor stack through the deposit cascade
     * (desired-count gap → proximate chest with affinity → home → Triage).
     * Bound to the root-level left-click handler when carrying and no
     * specific drop target handles the click.
     */
    void sendCursorSmartDeposit() {
        if (cursorSmartDepositEmitter == null) {
            return;
        }
        stashLastDropped();
        cursorSmartDepositEmitter.send();
    }

    /**
     * Drop the cursor stack onto a player-hotbar slot via vanilla
     * {@code menu.clicked} so left = drop-all/merge/swap and right =
     * drop-one. Bound to belt-panel left/right click while carrying.
     */
    void sendDropCursorAtHotbar(int hotbarIndex, int button) {
        if (dropCursorAtHotbarEmitter == null || hotbarIndex < 0 || hotbarIndex >= 9) {
            return;
        }
        stashLastDropped();
        boolean sent = dropCursorAtHotbarEmitter.send(hotbarIndex, button);
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
        boolean sent = dropCursorIntoChestEmitter.send(storageId);
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
        boolean sent = pickupToCursorEmitter.send(
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
        boolean sent = crossSurfaceQuickMoveAtlasEmitter.send(
                identity.itemId(),
                identity.comparisonMode(),
                identity.componentFingerprint(),
                count
        );
        host.localStatus.set(sent ? "shift-clicking to host..." : "shift-click unavailable");
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

    void sendTakeOneByIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
        if (takeOneByIdentityEmitter == null || identity == null) {
            return;
        }
        boolean sent = takeOneByIdentityEmitter.send(
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
        boolean sent = takeStackByIdentityEmitter.send(
                identity.itemId(), identity.comparisonMode(), identity.componentFingerprint());
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

    void sendMoveIsland(String islandId, double worldX, double worldY) {
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

    void sendReorderIsland(String islandId, int targetIndex) {
        if (islandId == null || islandId.isBlank()) {
            host.localStatus.set("invalid section reorder");
            host.rebuild();
            return;
        }
        boolean sent = reorderIslandEmitter != null && reorderIslandEmitter.send(
                islandId,
                Math.max(0, targetIndex)
        );
        host.localStatus.set(sent ? "section reorder requested" : "section reorder unavailable");
        host.rebuild();
    }

    void sendTakeAll(String storageId) {
        boolean sent = takeAllEmitter != null && takeAllEmitter.send(storageId);
        host.localStatus.set(sent ? "take-all requested" : "take-all unavailable");
        host.rebuild();
    }

    void sendDeposit() {
        boolean sent = depositEmitter != null && depositEmitter.send();
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
        boolean sent = claimChestAtPosEmitter.send(dimensionId, x, y, z);
        host.localStatus.set(sent ? "chest claimed" : "claim unavailable");
        host.rebuild();
    }

    void sendForgetChest(String storageId) {
        if (storageId == null || storageId.isBlank()) {
            return;
        }
        boolean sent = forgetChestEmitter != null && forgetChestEmitter.send(storageId);
        host.localStatus.set(sent ? "chest forgotten" : "forget unavailable");
        host.rebuild();
    }

    void sendForgetItemAffinity(String storageId, SlotWorkspaceViewModel.IdentityRef identity) {
        if (storageId == null || storageId.isBlank() || identity == null) {
            return;
        }
        boolean sent = forgetItemAffinityEmitter != null && forgetItemAffinityEmitter.send(
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
        boolean sent = moveChestEmitter != null && moveChestEmitter.send(
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
        boolean sent = setPlayerDesiredCountEmitter.send(
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
        boolean sent = adjustPlayerDesiredCountEmitter.send(
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
