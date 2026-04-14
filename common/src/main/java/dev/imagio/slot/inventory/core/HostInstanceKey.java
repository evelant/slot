package dev.imagio.slot.inventory.core;

import net.minecraft.world.inventory.AbstractContainerMenu;

public record HostInstanceKey(
        String menuClass,
        int containerId,
        String providerId,
        String providerScopeId
) {
    public HostInstanceKey {
        menuClass = menuClass == null ? "" : menuClass;
        containerId = Math.max(-1, containerId);
        providerId = providerId == null ? "" : providerId;
        providerScopeId = providerScopeId == null ? "" : providerScopeId;
    }

    public static HostInstanceKey empty() {
        return new HostInstanceKey("", -1, "", "");
    }

    public static HostInstanceKey of(
            AbstractContainerMenu menu,
            String providerId,
            String providerScopeId
    ) {
        if (menu == null) {
            return empty();
        }
        return new HostInstanceKey(
                menu.getClass().getName(),
                menu.containerId,
                providerId,
                providerScopeId
        );
    }
}
