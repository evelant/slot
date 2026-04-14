package dev.imagio.slot.session;

import dev.imagio.slot.capability.MenuCapabilityDescriptor;
import dev.imagio.slot.capability.ToolCapabilityDescriptor;
import dev.imagio.slot.client.source.InventorySource;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import dev.imagio.slot.storage.provider.SupplementalCarriedSourceDescriptor;
import dev.imagio.slot.storage.provider.SupplementalCarriedSourceProviderRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record InventoryHostDescriptor(
        UUID hostId,
        ServerMenuRef serverMenuRef,
        String screenClassName,
        Component title,
        AbstractContainerMenu menu,
        ChestLikeMenuLayout layout,
        HostTopologyDescriptor topology,
        StorageViewProviderSession providerSession,
        PlayerRuntimeStateDescriptor playerRuntimeState,
        List<InventorySourceDescriptor> sourceDescriptors,
        List<InventoryToolDescriptor> toolDescriptors,
        List<SupplementalCarriedSourceDescriptor> supplementalCarriedSources,
        boolean slotOwned,
        boolean recordsRecent,
        boolean carriedOnly
) {
    public InventoryHostDescriptor {
        hostId = hostId == null ? UUID.randomUUID() : hostId;
        serverMenuRef = serverMenuRef == null ? new ServerMenuRef("", -1) : serverMenuRef;
        screenClassName = screenClassName == null ? "" : screenClassName;
        title = title == null ? Component.empty() : title;
        Objects.requireNonNull(menu, "menu");
        Objects.requireNonNull(layout, "layout");
        topology = topology == null ? HostTopologyDescriptor.empty() : topology;
        Objects.requireNonNull(providerSession, "providerSession");
        playerRuntimeState = playerRuntimeState == null ? PlayerRuntimeStateDescriptor.vanilla(-1) : playerRuntimeState;
        sourceDescriptors = sourceDescriptors == null ? List.of() : List.copyOf(sourceDescriptors);
        toolDescriptors = toolDescriptors == null ? List.of() : List.copyOf(toolDescriptors);
        supplementalCarriedSources = supplementalCarriedSources == null ? List.of() : List.copyOf(supplementalCarriedSources);
    }

    public static InventoryHostDescriptor create(
            String screenClassName,
            Component title,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            boolean slotOwned,
            boolean recordsRecent,
            boolean carriedOnly
    ) {
        return create(screenClassName, title, menu, layout, -1, slotOwned, recordsRecent, carriedOnly);
    }

    public static InventoryHostDescriptor create(
            String screenClassName,
            Component title,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            int selectedHotbarSlotIndex,
            boolean slotOwned,
            boolean recordsRecent,
            boolean carriedOnly
    ) {
        Objects.requireNonNull(menu, "menu");
        Objects.requireNonNull(layout, "layout");
        StorageViewProviderSession providerSession = Objects.requireNonNull(layout.primaryStorageSession(), "layout.primaryStorageSession");
        UUID hostId = UUID.randomUUID();
        HostTopologyDescriptor topology = mergeTopology(layout, providerSession);
        List<InventorySourceDescriptor> baseSources = mergeSources(
                providerSession.hostSources(),
                builtInPlayerSources(topology)
        );
        PlayerRuntimeStateDescriptor playerRuntimeState = PlayerRuntimeStateDescriptor.vanilla(selectedHotbarSlotIndex);

        InventoryHostDescriptor base = new InventoryHostDescriptor(
                hostId,
                serverMenuRef(menu),
                screenClassName,
                title,
                menu,
                layout,
                topology,
                providerSession,
                playerRuntimeState,
                baseSources,
                providerSession.tools(),
                List.of(),
                slotOwned,
                recordsRecent,
                carriedOnly
        );

        List<SupplementalCarriedSourceDescriptor> supplementalSources = SupplementalCarriedSourceProviderRegistry.describe(base);
        List<InventorySourceDescriptor> allSources = mergeSources(
                baseSources,
                supplementalSources.stream().map(SupplementalCarriedSourceDescriptor::sourceDescriptor).toList()
        );

        return new InventoryHostDescriptor(
                base.hostId(),
                base.serverMenuRef(),
                base.screenClassName(),
                base.title(),
                base.menu(),
                base.layout(),
                base.topology(),
                base.providerSession(),
                base.playerRuntimeState(),
                allSources,
                base.toolDescriptors(),
                supplementalSources,
                base.slotOwned(),
                base.recordsRecent(),
                base.carriedOnly()
        );
    }

    public static InventoryHostDescriptor compatibilityHost(
            AbstractContainerMenu menu,
            StorageViewProviderSession providerSession
    ) {
        if (menu == null || providerSession == null) {
            return null;
        }
        List<InventorySourceDescriptor> hostSources = providerSession.hostSources();
        InventorySourceDescriptor primary = hostSources.stream()
                .filter(source -> source.domain() == InventorySourceDomain.HOST_STORAGE)
                .findFirst()
                .orElse(null);
        int containerSlotCount = hostSources.stream()
                .filter(source -> source.domain() == InventorySourceDomain.HOST_STORAGE)
                .mapToInt(InventorySourceDescriptor::logicalSlotCount)
                .sum();
        HostTopologyDescriptor topology = providerSession.topology();
        ChestLikeMenuLayout layout = new ChestLikeMenuLayout(
                containerSlotCount,
                hostSources.stream().map(InventorySourceDescriptor::toInventorySource).toList(),
                providerSession.primaryStorageLabel(),
                topology.menuSlotsBySourceId(),
                topology.sourceIdByMenuSlot(),
                hostSources.stream()
                        .filter(source -> source.domain() == InventorySourceDomain.HOST_STORAGE)
                        .allMatch(InventorySourceDescriptor::inCarriedInventory),
                providerSession
        );
        return new InventoryHostDescriptor(
                UUID.randomUUID(),
                serverMenuRef(menu),
                "",
                Component.empty(),
                menu,
                layout,
                topology,
                providerSession,
                PlayerRuntimeStateDescriptor.vanilla(-1),
                hostSources,
                providerSession.tools(),
                List.of(),
                false,
                false,
                hostSources.stream()
                        .filter(source -> source.domain() == InventorySourceDomain.HOST_STORAGE)
                        .allMatch(InventorySourceDescriptor::inCarriedInventory)
        );
    }

    public MenuCapabilityDescriptor capabilities() {
        LinkedHashMap<String, List<Integer>> logicalMenuSlotsBySource = new LinkedHashMap<>();
        LinkedHashSet<String> actionableSourceIds = new LinkedHashSet<>();
        for (InventorySourceDescriptor source : sourceDescriptors) {
            logicalMenuSlotsBySource.put(source.id(), topology.menuSlotsForSource(source.id()));
            if (source.actionable()) {
                actionableSourceIds.add(source.id());
            }
        }
        return new MenuCapabilityDescriptor(
                logicalMenuSlotsBySource,
                actionableSourceIds,
                toolDescriptors.stream()
                        .map(tool -> new ToolCapabilityDescriptor(
                                tool.id(),
                                tool.providerId(),
                                tool.kind(),
                                tool.presentationSpec(),
                                tool.live(),
                                tool.activationCommand(),
                                tool.toggleStates(),
                                tool.statePayload()
                        ))
                        .toList()
        );
    }

    public boolean includesSource(String sourceId) {
        return sourceDescriptor(sourceId) != null;
    }

    public List<InventorySourceDescriptor> hostStorageSources() {
        return sourceDescriptors.stream()
                .filter(source -> source.domain() == InventorySourceDomain.HOST_STORAGE)
                .toList();
    }

    public boolean hasCarriedHostStorage() {
        return hostStorageSources().stream().anyMatch(InventorySourceDescriptor::inCarriedInventory);
    }

    public boolean hasExternalHostStorage() {
        return hostStorageSources().stream().anyMatch(InventorySourceDescriptor::inExternalInventory);
    }

    public boolean hasOnlyCarriedHostStorage() {
        List<InventorySourceDescriptor> hostStorageSources = hostStorageSources();
        return !hostStorageSources.isEmpty()
                && hostStorageSources.stream().allMatch(InventorySourceDescriptor::inCarriedInventory);
    }

    public boolean hasOnlyExternalHostStorage() {
        List<InventorySourceDescriptor> hostStorageSources = hostStorageSources();
        return !hostStorageSources.isEmpty()
                && hostStorageSources.stream().allMatch(InventorySourceDescriptor::inExternalInventory);
    }

    public InventorySourceDescriptor singleHostStorageSource() {
        List<InventorySourceDescriptor> hostStorageSources = hostStorageSources();
        return hostStorageSources.size() == 1 ? hostStorageSources.get(0) : null;
    }

    public boolean menuBacksSource(String sourceId) {
        InventorySourceDescriptor descriptor = sourceDescriptor(sourceId);
        return descriptor != null && topology.sourceMenuBacked(sourceId);
    }

    public InventorySourceDescriptor sourceDescriptor(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return null;
        }
        return sourceDescriptors.stream()
                .filter(source -> source.id().equals(sourceId))
                .findFirst()
                .orElse(null);
    }

    public List<InventorySource> sources() {
        return sourceDescriptors.stream()
                .filter(source -> !source.hidden() && !source.toolOnly())
                .map(InventorySourceDescriptor::toInventorySource)
                .toList();
    }

    public Set<String> carriedSourceIds() {
        return sourceDescriptors.stream()
                .filter(InventorySourceDescriptor::inCarriedInventory)
                .map(InventorySourceDescriptor::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public Set<String> externalSourceIds() {
        return sourceDescriptors.stream()
                .filter(InventorySourceDescriptor::inExternalInventory)
                .map(InventorySourceDescriptor::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public Set<String> actionableSourceIds() {
        return sourceDescriptors.stream()
                .filter(InventorySourceDescriptor::actionable)
                .map(InventorySourceDescriptor::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public Set<String> menuBackedCarriedSourceIds() {
        return sourceDescriptors.stream()
                .filter(InventorySourceDescriptor::inCarriedInventory)
                .filter(source -> topology.sourceMenuBacked(source.id()))
                .map(InventorySourceDescriptor::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public List<SupplementalCarriedSourceDescriptor> supplementalCarriedSources(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return List.of();
        }
        return supplementalCarriedSources.stream()
                .filter(descriptor -> descriptor.matchesSource(sourceId))
                .toList();
    }

    public Set<String> sourceIdsForPane(InventoryPane pane) {
        return sourceDescriptors.stream()
                .filter(source -> pane == InventoryPane.CARRIED
                        ? source.paneMembership().carried()
                        : source.paneMembership().external())
                .map(InventorySourceDescriptor::id)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static ServerMenuRef serverMenuRef(AbstractContainerMenu menu) {
        if (menu == null) {
            return new ServerMenuRef("", -1);
        }
        return new ServerMenuRef(menu.getClass().getName(), menu.containerId);
    }

    private static HostTopologyDescriptor mergeTopology(
            ChestLikeMenuLayout layout,
            StorageViewProviderSession providerSession
    ) {
        LinkedHashMap<String, List<Integer>> menuSlotsBySource = new LinkedHashMap<>(providerSession.topology().menuSlotsBySourceId());
        menuSlotsBySource.putIfAbsent(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN, layout.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN));
        menuSlotsBySource.putIfAbsent(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR, layout.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR));
        menuSlotsBySource.putIfAbsent(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR, layout.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR));
        menuSlotsBySource.putIfAbsent(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND, layout.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND));

        LinkedHashMap<Integer, String> sourceIdByMenuSlot = new LinkedHashMap<>(providerSession.topology().sourceIdByMenuSlot());
        menuSlotsBySource.forEach((sourceId, menuSlots) -> menuSlots.forEach(menuSlot -> sourceIdByMenuSlot.putIfAbsent(menuSlot, sourceId)));

        LinkedHashMap<String, List<Integer>> toolRegionSlots = new LinkedHashMap<>(providerSession.topology().toolRegionSlots());
        for (InventoryToolDescriptor tool : providerSession.tools()) {
            for (ToolRegionDescriptor region : tool.regions()) {
                toolRegionSlots.putIfAbsent(region.id(), region.logicalSlots());
            }
        }

        return new HostTopologyDescriptor(menuSlotsBySource, sourceIdByMenuSlot, toolRegionSlots);
    }

    private static List<InventorySourceDescriptor> mergeSources(
            List<InventorySourceDescriptor> first,
            List<InventorySourceDescriptor> second
    ) {
        LinkedHashMap<String, InventorySourceDescriptor> descriptors = new LinkedHashMap<>();
        if (first != null) {
            for (InventorySourceDescriptor descriptor : first) {
                if (descriptor != null) {
                    descriptors.put(descriptor.id(), descriptor);
                }
            }
        }
        if (second != null) {
            for (InventorySourceDescriptor descriptor : second) {
                if (descriptor != null) {
                    descriptors.putIfAbsent(descriptor.id(), descriptor);
                }
            }
        }
        return List.copyOf(descriptors.values());
    }

    private static List<InventorySourceDescriptor> builtInPlayerSources(HostTopologyDescriptor topology) {
        List<InventorySourceDescriptor> sources = new ArrayList<>();
        sources.add(playerMain(topology));
        sources.add(playerHotbar(topology));
        sources.add(playerArmor(topology));
        sources.add(playerOffhand(topology));
        return List.copyOf(sources);
    }

    private static InventorySourceDescriptor playerMain(HostTopologyDescriptor topology) {
        boolean menuBacked = topology.sourceMenuBacked(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN);
        return InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN)
                .label(Component.translatable("slot.source.main"))
                .domain(InventorySourceDomain.PLAYER)
                .role(InventorySourceRole.MAIN)
                .groupId("inventory")
                .slotCount(Math.max(topology.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_MAIN).size(), 27))
                .backingKind(menuBacked ? InventorySourceBackingKind.MENU_BACKED : InventorySourceBackingKind.PLAYER_BACKED)
                .capabilities(Set.of(
                        InventorySourceCapability.INSERT,
                        InventorySourceCapability.EXTRACT,
                        InventorySourceCapability.QUICK_ACCESS_ASSIGN
                ))
                .actionRoute(menuBacked ? InventorySourceActionRoute.MENU_MUTATION : InventorySourceActionRoute.PLAYER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(10)
                .build();
    }

    private static InventorySourceDescriptor playerHotbar(HostTopologyDescriptor topology) {
        boolean menuBacked = topology.sourceMenuBacked(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR);
        return InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR)
                .label(Component.translatable("slot.source.hotbar"))
                .domain(InventorySourceDomain.PLAYER)
                .role(InventorySourceRole.HOTBAR)
                .laneId("0")
                .groupId("inventory")
                .slotCount(Math.max(topology.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_HOTBAR).size(), 9))
                .backingKind(menuBacked ? InventorySourceBackingKind.MENU_BACKED : InventorySourceBackingKind.PLAYER_BACKED)
                .capabilities(Set.of(
                        InventorySourceCapability.INSERT,
                        InventorySourceCapability.EXTRACT,
                        InventorySourceCapability.USE,
                        InventorySourceCapability.DROP,
                        InventorySourceCapability.QUICK_ACCESS_ASSIGN
                ))
                .actionRoute(menuBacked ? InventorySourceActionRoute.MENU_MUTATION : InventorySourceActionRoute.PLAYER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(20)
                .build();
    }

    private static InventorySourceDescriptor playerArmor(HostTopologyDescriptor topology) {
        boolean menuBacked = topology.sourceMenuBacked(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR);
        return InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR)
                .label(Component.translatable("slot.source.armor"))
                .domain(InventorySourceDomain.PLAYER)
                .role(InventorySourceRole.EQUIPMENT)
                .groupId("armor")
                .slotCount(Math.max(topology.menuSlotsForSource(ChestLikeMenuLayout.SOURCE_PLAYER_ARMOR).size(), 4))
                .backingKind(menuBacked ? InventorySourceBackingKind.MENU_BACKED : InventorySourceBackingKind.PLAYER_BACKED)
                .capabilities(Set.of(
                        InventorySourceCapability.INSERT,
                        InventorySourceCapability.EXTRACT,
                        InventorySourceCapability.EQUIP,
                        InventorySourceCapability.UNEQUIP
                ))
                .actionRoute(menuBacked ? InventorySourceActionRoute.MENU_MUTATION : InventorySourceActionRoute.PLAYER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(25)
                .build();
    }

    private static InventorySourceDescriptor playerOffhand(HostTopologyDescriptor topology) {
        boolean menuBacked = topology.sourceMenuBacked(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND);
        return InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_PLAYER_OFFHAND)
                .label(Component.translatable("slot.source.offhand"))
                .domain(InventorySourceDomain.PLAYER)
                .role(InventorySourceRole.OFFHAND)
                .groupId("offhand")
                .slotCount(1)
                .backingKind(menuBacked ? InventorySourceBackingKind.MENU_BACKED : InventorySourceBackingKind.PLAYER_BACKED)
                .capabilities(Set.of(
                        InventorySourceCapability.INSERT,
                        InventorySourceCapability.EXTRACT,
                        InventorySourceCapability.USE,
                        InventorySourceCapability.DROP,
                        InventorySourceCapability.EQUIP,
                        InventorySourceCapability.UNEQUIP,
                        InventorySourceCapability.QUICK_ACCESS_ASSIGN
                ))
                .actionRoute(menuBacked ? InventorySourceActionRoute.MENU_MUTATION : InventorySourceActionRoute.PLAYER_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(30)
                .build();
    }
}
