package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.collection.HotbarLoadoutCapture;
import dev.imagio.slot.client.collection.HotbarLoadoutDefinition;
import dev.imagio.slot.client.collection.HotbarLoadoutSlot;
import dev.imagio.slot.client.model.ItemIdentity;

import java.util.List;
import java.util.Set;

public final class QuickAccessService implements QuickAccessCapability {
    private final QuickAccessSupport support;
    private final QuickAccessLoadoutService loadoutService;
    private final QuickAccessInventoryActionService inventoryActionService;

    public QuickAccessService(InventoryScreenContext screenContext) {
        this(screenContext, "", true);
    }

    public QuickAccessService(InventoryScreenContext screenContext, String requestRoutingKey, boolean trackTransferHistory) {
        this.support = new QuickAccessSupport(screenContext, requestRoutingKey, trackTransferHistory);
        this.loadoutService = new QuickAccessLoadoutService(support);
        this.inventoryActionService = new QuickAccessInventoryActionService(support);
    }

    public void tick() {
        support.tick();
    }

    public HotbarLoadoutCapture captureCurrentLoadout() {
        return loadoutService.captureCurrentLoadout();
    }

    public List<HotbarLoadoutSlot> captureCurrentHotbar() {
        return loadoutService.captureCurrentHotbar();
    }

    public QuickAccessMutationResult restoreCapturedLoadoutMutation(HotbarLoadoutCapture capture) {
        return loadoutService.restoreCapturedLoadoutMutation(capture);
    }

    public boolean restoreCapturedLoadout(HotbarLoadoutCapture capture) {
        return loadoutService.restoreCapturedLoadout(capture);
    }

    public QuickAccessMutationResult applyLoadoutMutation(HotbarLoadoutDefinition loadout) {
        return loadoutService.applyLoadoutMutation(loadout);
    }

    public boolean applyLoadout(HotbarLoadoutDefinition loadout) {
        return loadoutService.applyLoadout(loadout);
    }

    public QuickAccessMutationResult assignToQuickAccessMutation(ItemIdentity identity, int quickAccessIndex) {
        return loadoutService.assignToQuickAccessMutation(identity, quickAccessIndex);
    }

    public QuickAccessMutationResult assignToQuickAccessMutation(ItemIdentity identity, int quickAccessIndex, Set<String> preferredSourceIds) {
        return loadoutService.assignToQuickAccessMutation(identity, quickAccessIndex, preferredSourceIds);
    }

    public boolean assignToQuickAccess(ItemIdentity identity, int quickAccessIndex) {
        return loadoutService.assignToQuickAccess(identity, quickAccessIndex);
    }

    public boolean assignToQuickAccess(ItemIdentity identity, int quickAccessIndex, Set<String> preferredSourceIds) {
        return loadoutService.assignToQuickAccess(identity, quickAccessIndex, preferredSourceIds);
    }

    public boolean assignToHotbar(ItemIdentity identity, int hotbarIndex) {
        return loadoutService.assignToQuickAccess(identity, hotbarIndex);
    }

    public boolean assignToHotbar(ItemIdentity identity, int hotbarIndex, Set<String> preferredSourceIds) {
        return loadoutService.assignToQuickAccess(identity, hotbarIndex, preferredSourceIds);
    }

    @Override
    public boolean canAssignToQuickAccess(ItemIdentity identity) {
        return loadoutService.canAssignToQuickAccess(identity);
    }

    @Override
    public boolean canAssignToQuickAccess(ItemIdentity identity, Set<String> preferredSourceIds) {
        return loadoutService.canAssignToQuickAccess(identity, preferredSourceIds);
    }

    public boolean canAssignToHotbar(ItemIdentity identity) {
        return loadoutService.canAssignToQuickAccess(identity);
    }

    public boolean canAssignToHotbar(ItemIdentity identity, Set<String> preferredSourceIds) {
        return loadoutService.canAssignToQuickAccess(identity, preferredSourceIds);
    }

    public QuickAccessMutationResult stashQuickAccessSlotMutation(int quickAccessIndex) {
        return loadoutService.stashQuickAccessSlotMutation(quickAccessIndex);
    }

    public boolean stashQuickAccessSlot(int quickAccessIndex) {
        return loadoutService.stashQuickAccessSlot(quickAccessIndex);
    }

    public boolean stashHotbarSlot(int hotbarIndex) {
        return loadoutService.stashQuickAccessSlot(hotbarIndex);
    }

    public QuickAccessMutationResult clickQuickAccessSlotMutation(int quickAccessIndex, int mouseButton) {
        return loadoutService.clickQuickAccessSlotMutation(quickAccessIndex, mouseButton);
    }

    public boolean clickQuickAccessSlot(int quickAccessIndex, int mouseButton) {
        return loadoutService.clickQuickAccessSlot(quickAccessIndex, mouseButton);
    }

    public boolean clickHotbarSlot(int hotbarIndex, int mouseButton) {
        return loadoutService.clickQuickAccessSlot(hotbarIndex, mouseButton);
    }

    @Override
    public boolean canUseFromInventory(ItemIdentity identity) {
        return inventoryActionService.canUseFromInventory(identity);
    }

    @Override
    public boolean canUseFromInventory(ItemIdentity identity, Set<String> preferredSourceIds) {
        return inventoryActionService.canUseFromInventory(identity, preferredSourceIds);
    }

    public boolean useFromInventory(ItemIdentity identity) {
        return inventoryActionService.useFromInventory(identity);
    }

    public boolean useFromInventory(ItemIdentity identity, Set<String> preferredSourceIds) {
        return inventoryActionService.useFromInventory(identity, preferredSourceIds);
    }

    public QuickAccessInventoryActionResult useFromInventoryAction(ItemIdentity identity) {
        return inventoryActionService.useFromInventoryAction(identity);
    }

    public QuickAccessInventoryActionResult useFromInventoryAction(ItemIdentity identity, Set<String> preferredSourceIds) {
        return inventoryActionService.useFromInventoryAction(identity, preferredSourceIds);
    }

    @Override
    public boolean canDropFromInventory(ItemIdentity identity) {
        return inventoryActionService.canDropFromInventory(identity);
    }

    @Override
    public boolean canDropFromInventory(ItemIdentity identity, Set<String> preferredSourceIds) {
        return inventoryActionService.canDropFromInventory(identity, preferredSourceIds);
    }

    public boolean dropFromInventory(ItemIdentity identity) {
        return inventoryActionService.dropFromInventory(identity);
    }

    public boolean dropFromInventory(ItemIdentity identity, Set<String> preferredSourceIds) {
        return inventoryActionService.dropFromInventory(identity, preferredSourceIds);
    }

    public QuickAccessInventoryActionResult dropFromInventoryAction(ItemIdentity identity) {
        return inventoryActionService.dropFromInventoryAction(identity);
    }

    public QuickAccessInventoryActionResult dropFromInventoryAction(ItemIdentity identity, Set<String> preferredSourceIds) {
        return inventoryActionService.dropFromInventoryAction(identity, preferredSourceIds);
    }
}
