package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEmitter;
import com.lowdragmc.lowdraglib2.gui.sync.rpc.RPCEventBuilder;

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
}
