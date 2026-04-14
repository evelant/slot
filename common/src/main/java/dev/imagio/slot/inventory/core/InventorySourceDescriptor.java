package dev.imagio.slot.inventory.core;

import net.minecraft.network.chat.Component;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record InventorySourceDescriptor(
        String id,
        Component label,
        InventorySourceDomain domain,
        InventorySourceRole role,
        String laneId,
        String groupId,
        int logicalSlotCount,
        InventoryBindingRoute bindingRoute,
        Set<InventoryCapability> capabilities,
        boolean simulationSupported,
        InventoryActionRoute actionRoute,
        InventoryPaneMembership paneMembership,
        String diagnostics,
        int stableOrder
) {
    public InventorySourceDescriptor {
        id = id == null ? "" : id;
        label = label == null ? Component.empty() : label;
        domain = domain == null ? InventorySourceDomain.PLAYER : domain;
        role = role == null ? InventorySourceRole.PROVIDER_DEFINED : role;
        laneId = laneId == null ? "" : laneId;
        groupId = groupId == null ? "" : groupId;
        logicalSlotCount = Math.max(0, logicalSlotCount);
        bindingRoute = bindingRoute == null ? InventoryBindingRoute.PROVIDER : bindingRoute;
        capabilities = capabilities == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(capabilities));
        actionRoute = actionRoute == null ? InventoryActionRoute.NON_ACTIONABLE : actionRoute;
        paneMembership = paneMembership == null ? defaultPaneMembership(domain, role) : paneMembership;
        diagnostics = diagnostics == null ? "" : diagnostics;
        if (id.isBlank()) {
            throw new IllegalArgumentException("source id must not be blank");
        }
    }

    public boolean supports(InventoryCapability capability) {
        return capability != null && capabilities.contains(capability);
    }

    public boolean actionable() {
        return actionRoute != InventoryActionRoute.NON_ACTIONABLE;
    }

    public boolean menuBacked() {
        return bindingRoute == InventoryBindingRoute.MENU;
    }

    public boolean playerBacked() {
        return bindingRoute == InventoryBindingRoute.PLAYER;
    }

    public boolean providerBacked() {
        return bindingRoute == InventoryBindingRoute.PROVIDER;
    }

    public boolean toolBacked() {
        return bindingRoute == InventoryBindingRoute.TOOL;
    }

    public boolean visibleToUser() {
        return paneMembership.visibleToUser();
    }

    public boolean inCarriedPane() {
        return paneMembership.carried();
    }

    public boolean inExternalPane() {
        return paneMembership.external();
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final String id;
        private Component label = Component.empty();
        private InventorySourceDomain domain = InventorySourceDomain.PLAYER;
        private InventorySourceRole role = InventorySourceRole.PROVIDER_DEFINED;
        private String laneId = "";
        private String groupId = "";
        private int logicalSlotCount;
        private InventoryBindingRoute bindingRoute = InventoryBindingRoute.PROVIDER;
        private Set<InventoryCapability> capabilities = Set.of();
        private boolean simulationSupported = true;
        private InventoryActionRoute actionRoute = InventoryActionRoute.NON_ACTIONABLE;
        private InventoryPaneMembership paneMembership;
        private String diagnostics = "";
        private int stableOrder;

        private Builder(String id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder label(Component label) {
            this.label = label;
            return this;
        }

        public Builder domain(InventorySourceDomain domain) {
            this.domain = domain;
            return this;
        }

        public Builder role(InventorySourceRole role) {
            this.role = role;
            return this;
        }

        public Builder laneId(String laneId) {
            this.laneId = laneId;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder logicalSlotCount(int logicalSlotCount) {
            this.logicalSlotCount = logicalSlotCount;
            return this;
        }

        public Builder bindingRoute(InventoryBindingRoute bindingRoute) {
            this.bindingRoute = bindingRoute;
            return this;
        }

        public Builder capabilities(Set<InventoryCapability> capabilities) {
            this.capabilities = capabilities;
            return this;
        }

        public Builder simulationSupported(boolean simulationSupported) {
            this.simulationSupported = simulationSupported;
            return this;
        }

        public Builder actionRoute(InventoryActionRoute actionRoute) {
            this.actionRoute = actionRoute;
            return this;
        }

        public Builder paneMembership(InventoryPaneMembership paneMembership) {
            this.paneMembership = paneMembership;
            return this;
        }

        public Builder diagnostics(String diagnostics) {
            this.diagnostics = diagnostics;
            return this;
        }

        public Builder stableOrder(int stableOrder) {
            this.stableOrder = stableOrder;
            return this;
        }

        public InventorySourceDescriptor build() {
            return new InventorySourceDescriptor(
                    id,
                    label,
                    domain,
                    role,
                    laneId,
                    groupId,
                    logicalSlotCount,
                    bindingRoute,
                    capabilities,
                    simulationSupported,
                    actionRoute,
                    paneMembership,
                    diagnostics,
                    stableOrder
            );
        }
    }

    private static InventoryPaneMembership defaultPaneMembership(InventorySourceDomain domain, InventorySourceRole role) {
        if (domain == InventorySourceDomain.HOST_STORAGE) {
            return role == InventorySourceRole.PRIMARY_STORAGE
                    ? InventoryPaneMembership.EXTERNAL
                    : InventoryPaneMembership.HIDDEN;
        }
        if (domain == InventorySourceDomain.TOOL_REGION) {
            return InventoryPaneMembership.TOOL_ONLY;
        }
        return InventoryPaneMembership.CARRIED;
    }
}
