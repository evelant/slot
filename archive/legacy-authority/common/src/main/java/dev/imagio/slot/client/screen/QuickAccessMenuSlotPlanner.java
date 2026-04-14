package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.inventory.kernel.ActionableSourcePolicy;
import dev.imagio.slot.inventory.kernel.MenuSlotResolver;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Set;

final class QuickAccessMenuSlotPlanner {
    private final InventoryScreenContext screenContext;

    QuickAccessMenuSlotPlanner(InventoryScreenContext screenContext) {
        this.screenContext = screenContext;
    }

    Integer findMatchingSourceMenuSlot(AbstractContainerMenu menu, int targetMenuSlot, ItemIdentity identity) {
        return findMatchingSourceMenuSlot(menu, targetMenuSlot, identity, candidateSourceMenuSlots(Set.of()));
    }

    Integer findMatchingSourceMenuSlot(
            AbstractContainerMenu menu,
            int targetMenuSlot,
            ItemIdentity identity,
            List<Integer> candidateMenuSlots
    ) {
        for (int sourceMenuSlot : candidateMenuSlots) {
            if (sourceMenuSlot == targetMenuSlot) {
                continue;
            }

            Slot slot = MenuSlotResolver.safeSlot(menu, sourceMenuSlot);
            if (slot == null) {
                continue;
            }
            if (ItemBehaviorPolicy.matchesMovableIdentity(slot.getItem(), identity)) {
                return sourceMenuSlot;
            }
        }
        return null;
    }

    List<Integer> candidateSourceMenuSlots(Set<String> preferredSourceIds) {
        return policy(planningMenu()).quickAccessCandidateSourceSlots(preferredSourceIds);
    }

    int offhandMenuSlot(AbstractContainerMenu menu) {
        Integer offhandSlot = new ActionableSourcePolicy(new MenuSlotResolver(menu, currentLayout())).offhandMenuSlot();
        return offhandSlot == null ? -1 : offhandSlot;
    }

    List<Integer> hotbarMenuSlots() {
        return policy(planningMenu()).hotbarMenuSlots();
    }

    List<Integer> mainInventoryMenuSlots() {
        return policy(planningMenu()).mainInventoryMenuSlots();
    }

    AbstractContainerMenu activeMenu(LocalPlayer player) {
        if (screenContext != null) {
            return screenContext.menu();
        }
        if (player == null) {
            return null;
        }
        return player.containerMenu != null ? player.containerMenu : player.inventoryMenu;
    }

    ChestLikeMenuLayout currentLayout() {
        return screenContext == null ? null : screenContext.layout();
    }

    String excludedBackpackSourceReference() {
        if (screenContext == null) {
            return "";
        }
        return screenContext.supplementalCarriedSources(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK).stream()
                .findFirst()
                .map(dev.imagio.slot.storage.provider.SupplementalCarriedSourceDescriptor::referenceKey)
                .orElse("");
    }

    private AbstractContainerMenu planningMenu() {
        return screenContext == null ? null : screenContext.menu();
    }

    private ActionableSourcePolicy policy(AbstractContainerMenu menu) {
        return new ActionableSourcePolicy(new MenuSlotResolver(menu, currentLayout()));
    }
}
