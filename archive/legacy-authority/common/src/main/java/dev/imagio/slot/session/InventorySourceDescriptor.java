package dev.imagio.slot.session;

import dev.imagio.slot.client.source.BasicInventorySource;
import dev.imagio.slot.client.source.InventorySource;
import dev.imagio.slot.client.source.SourceGroup;
import net.minecraft.network.chat.Component;

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
        InventorySourceBackingKind backingKind,
        Set<InventorySourceCapability> capabilities,
        InventorySourceActionRoute actionRoute,
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
        backingKind = backingKind == null ? InventorySourceBackingKind.PROVIDER_BACKED : backingKind;
        capabilities = capabilities == null ? Set.of() : Set.copyOf(capabilities);
        actionRoute = actionRoute == null ? InventorySourceActionRoute.NON_ACTIONABLE : actionRoute;
        paneMembership = paneMembership == null ? defaultPaneMembership(domain, role) : paneMembership;
        diagnostics = diagnostics == null ? "" : diagnostics;
        if (id.isBlank()) {
            throw new IllegalArgumentException("source id must not be blank");
        }
    }

    public boolean inCarriedInventory() {
        return paneMembership.carried();
    }

    public boolean inExternalInventory() {
        return paneMembership.external();
    }

    public boolean hidden() {
        return paneMembership == InventoryPaneMembership.HIDDEN;
    }

    public boolean toolOnly() {
        return paneMembership == InventoryPaneMembership.TOOL_ONLY;
    }

    public int slotCount() {
        return logicalSlotCount;
    }

    public boolean menuBacked() {
        return backingKind == InventorySourceBackingKind.MENU_BACKED;
    }

    public boolean actionable() {
        return actionRoute != InventorySourceActionRoute.NON_ACTIONABLE;
    }

    public boolean supports(InventorySourceCapability capability) {
        return capability != null && capabilities.contains(capability);
    }

    public InventorySource toInventorySource() {
        return new BasicInventorySource(
                id,
                label.getString(),
                sourceGroup(),
                stableOrder,
                false,
                supports(InventorySourceCapability.INSERT),
                supports(InventorySourceCapability.EXTRACT)
        );
    }

    public SourceGroup sourceGroup() {
        if (role == InventorySourceRole.HOTBAR) {
            return SourceGroup.PLAYER_HOTBAR;
        }
        if (role == InventorySourceRole.MAIN) {
            return SourceGroup.PLAYER_MAIN;
        }
        return inExternalInventory() ? SourceGroup.OPEN_CONTAINER : SourceGroup.CARRIED;
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
        private int logicalSlotCount = 0;
        private InventorySourceBackingKind backingKind = InventorySourceBackingKind.PROVIDER_BACKED;
        private Set<InventorySourceCapability> capabilities = Set.of();
        private InventorySourceActionRoute actionRoute = InventorySourceActionRoute.NON_ACTIONABLE;
        private InventoryPaneMembership paneMembership;
        private String diagnostics = "";
        private int stableOrder = 0;

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

        public Builder slotCount(int slotCount) {
            this.logicalSlotCount = slotCount;
            return this;
        }

        public Builder logicalSlotCount(int logicalSlotCount) {
            this.logicalSlotCount = logicalSlotCount;
            return this;
        }

        public Builder backingKind(InventorySourceBackingKind backingKind) {
            this.backingKind = backingKind;
            return this;
        }

        public Builder capabilities(Set<InventorySourceCapability> capabilities) {
            this.capabilities = capabilities;
            return this;
        }

        public Builder actionable(boolean actionable) {
            if (!actionable) {
                this.actionRoute = InventorySourceActionRoute.NON_ACTIONABLE;
            } else if (backingKind == InventorySourceBackingKind.PROVIDER_BACKED) {
                this.actionRoute = InventorySourceActionRoute.PROVIDER_MUTATION;
            } else if (backingKind == InventorySourceBackingKind.PLAYER_BACKED) {
                this.actionRoute = InventorySourceActionRoute.PLAYER_MUTATION;
            } else {
                this.actionRoute = InventorySourceActionRoute.MENU_MUTATION;
            }
            return this;
        }

        public Builder menuBacked(boolean menuBacked) {
            if (menuBacked) {
                this.backingKind = InventorySourceBackingKind.MENU_BACKED;
                if (this.actionRoute != InventorySourceActionRoute.NON_ACTIONABLE) {
                    this.actionRoute = InventorySourceActionRoute.MENU_MUTATION;
                }
            } else if (this.backingKind == InventorySourceBackingKind.MENU_BACKED) {
                this.backingKind = InventorySourceBackingKind.PROVIDER_BACKED;
            }
            return this;
        }

        public Builder actionRoute(InventorySourceActionRoute actionRoute) {
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
                    backingKind,
                    capabilities,
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
        if (domain == InventorySourceDomain.TOOL) {
            return InventoryPaneMembership.TOOL_ONLY;
        }
        return InventoryPaneMembership.CARRIED;
    }
}
