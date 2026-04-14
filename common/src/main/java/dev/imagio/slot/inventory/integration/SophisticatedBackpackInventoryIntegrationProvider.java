package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackSupport;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackTransferSupport;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackUpgradeSupport;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.CraftingSurfaceDescriptor;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryStackSnapshot;
import dev.imagio.slot.inventory.core.InventoryToolAction;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolKind;
import dev.imagio.slot.inventory.core.InventoryToolToggle;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.PortableContainerClassifiers;
import dev.imagio.slot.inventory.core.ToolActivationToken;
import dev.imagio.slot.inventory.core.ToolPresentationHints;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionRole;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SophisticatedBackpackInventoryIntegrationProvider implements InventoryIntegrationProvider {
    private static final String OPEN_PROVIDER_ID = "sophisticatedbackpacks";
    private static final String CARRIED_PROVIDER_ID = "sophisticatedbackpacks:carried";
    private static final String PRIMARY_SOURCE_ID = "sophisticatedbackpacks:open_backpack";

    static {
        PortableContainerClassifiers.register(SophisticatedBackpackSupport::isBackpackItem);
    }

    @Override
    public String providerId() {
        return OPEN_PROVIDER_ID;
    }

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
        if (context == null || context.menu() == null || context.playerInventory() == null || !isBackpackStorage(context.menu(), context.screenClassName())) {
            return ProviderResult.unsupported(providerId(), "unsupported_menu", "Menu is not a Sophisticated Backpack");
        }

        int containerSlotCount = MenuBackedHostSupport.inferSupportedStorageSlots(
                context.menu(),
                context.playerInventory(),
                context.screenClassName()
        );
        if (containerSlotCount <= 0) {
            return ProviderResult.unsupported(
                    providerId(),
                    "unsupported_menu",
                    "Sophisticated Backpack menu did not expose storage inventory slots"
            );
        }

        String label = context.title() == null || context.title().getString().isBlank()
                ? Component.translatable("slot.source.open_container").getString()
                : context.title().getString();
        InventorySourceDescriptor primarySource = InventorySourceDescriptor.builder(PRIMARY_SOURCE_ID)
                .label(Component.literal(label))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .groupId("primary")
                .logicalSlotCount(containerSlotCount)
                .bindingRoute(InventoryBindingRoute.MENU)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(5)
                .build();

        List<Integer> primaryMenuSlots = MenuBackedHostSupport.slotRange(
                0,
                Math.min(containerSlotCount, context.menu().slots.size()) - 1
        );
        ResolvedToolArtifacts toolArtifacts = resolveToolArtifacts(context.menu());

        return ProviderResult.supported(new InventoryHostSession() {
            @Override
            public String providerId() {
                return OPEN_PROVIDER_ID;
            }

            @Override
            public String providerScopeId() {
                return encodeCarrier(SophisticatedBackpackSupport.openedBackpackCarrier(context.menu()));
            }

            @Override
            public List<InventorySourceDescriptor> hostSources() {
                ArrayList<InventorySourceDescriptor> sources = new ArrayList<>();
                sources.add(primarySource);
                sources.addAll(toolArtifacts.sources());
                return List.copyOf(sources);
            }

            @Override
            public InventoryTopologyDescriptor topology() {
                LinkedHashMap<String, List<Integer>> sourceSlots = new LinkedHashMap<>();
                sourceSlots.put(primarySource.id(), primaryMenuSlots);
                sourceSlots.putAll(toolArtifacts.sourceSlots());
                LinkedHashMap<Integer, String> sourceIdsByMenuSlot = new LinkedHashMap<>();
                sourceIdsByMenuSlot.putAll(MenuBackedHostSupport.sourceIdsByMenuSlot(primarySource.id(), primaryMenuSlots));
                toolArtifacts.sourceSlots().forEach((sourceId, slots) -> sourceIdsByMenuSlot.putAll(MenuBackedHostSupport.sourceIdsByMenuSlot(sourceId, slots)));
                return new InventoryTopologyDescriptor(
                        Map.copyOf(sourceSlots),
                        Map.copyOf(sourceIdsByMenuSlot),
                        toolArtifacts.regionSlots()
                );
            }

            @Override
            public List<InventoryToolDescriptor> tools() {
                return toolArtifacts.tools();
            }

            @Override
            public InventorySourceSnapshot readSourceSnapshot(InventoryHostDescriptor host, String sourceId) {
                if (host == null) {
                    return InventorySourceSnapshot.empty(sourceId == null || sourceId.isBlank() ? "__missing__" : sourceId);
                }
                if (PRIMARY_SOURCE_ID.equals(sourceId)) {
                    return MenuBackedHostSupport.readSourceSnapshot(host.menu(), sourceId, primaryMenuSlots);
                }
                List<Integer> menuSlots = toolArtifacts.sourceSlots().get(sourceId);
                return menuSlots == null
                        ? InventorySourceSnapshot.empty(sourceId)
                        : MenuBackedHostSupport.readSourceSnapshot(host.menu(), sourceId, menuSlots);
            }

            @Override
            public MutationResult mutate(
                    InventoryHostDescriptor host,
                    InventoryMutationRequest request,
                    InventoryMutationMode mode
            ) {
                if (request == null) {
                    return MutationResult.blocked("unsupported_source", request == null ? null : request.stack());
                }
                if (PRIMARY_SOURCE_ID.equals(request.sourceId())) {
                    return MenuBackedHostSupport.mutateMenuSlots(host, request, mode, primaryMenuSlots);
                }
                List<Integer> menuSlots = toolArtifacts.sourceSlots().get(request.sourceId());
                return menuSlots == null
                        ? MutationResult.blocked("unsupported_source", request.stack())
                        : MenuBackedHostSupport.mutateMenuSlots(host, request, mode, menuSlots);
            }

            @Override
            public ToolActionResult activateTool(InventoryHostDescriptor host, String toolId, InventoryActionMode mode) {
                if (host == null || toolId == null || toolId.isBlank()) {
                    return ToolActionResult.blocked("missing_host_or_tool");
                }
                InventoryToolDescriptor tool = toolArtifacts.tools().stream().filter(candidate -> candidate.matchesToolId(toolId)).findFirst().orElse(null);
                if (tool == null || tool.activationToken() == null || !tool.activationToken().present()) {
                    return ToolActionResult.blocked("tool_activation_unavailable");
                }
                if (mode == InventoryActionMode.SIMULATE) {
                    return ToolActionResult.success();
                }
                String tabIdValue = tool.activationToken().arguments().getOrDefault("tabId", "");
                try {
                    return SophisticatedBackpackUpgradeSupport.ensureCraftingUpgradeOpen(host.menu(), Integer.parseInt(tabIdValue))
                            ? ToolActionResult.success()
                            : ToolActionResult.blocked("tool_activation_failed");
                } catch (NumberFormatException ignored) {
                    return ToolActionResult.blocked("invalid_tab_id");
                }
            }

            @Override
            public ToolActionResult executeToolAction(
                    InventoryHostDescriptor host,
                    String toolId,
                    InventoryToolActionId actionId,
                    InventoryActionMode mode
            ) {
                InventoryToolDescriptor tool = toolArtifacts.tools().stream().filter(candidate -> candidate.matchesToolId(toolId)).findFirst().orElse(null);
                if (tool == null) {
                    return ToolActionResult.blocked("unsupported_tool");
                }
                return MenuBackedToolActionExecutor.execute(host, tool, actionId, mode);
            }

            @Override
            public ToolActionResult setToolToggle(
                    InventoryHostDescriptor host,
                    String toolId,
                    InventoryToolToggleId toggleId,
                    boolean enabled,
                    InventoryActionMode mode
            ) {
                if (host == null || toggleId != InventoryToolToggleId.AUTO_REFILL || toolId == null || toolId.isBlank()) {
                    return ToolActionResult.blocked("unsupported_tool_toggle");
                }
                if (mode == InventoryActionMode.SIMULATE) {
                    return ToolActionResult.success();
                }
                boolean matchesTool = toolArtifacts.tools().stream().anyMatch(tool -> tool.matchesToolId(toolId));
                boolean applied = matchesTool && SophisticatedBackpackUpgradeSupport.setRefillCraftingGrid(host.menu(), enabled);
                return applied ? ToolActionResult.success() : ToolActionResult.blocked("tool_toggle_failed");
            }
        });
    }

    @Override
    public List<PlayerInventoryExtension> playerExtensions(PlayerInventoryContext context) {
        if (context == null || context.playerInventory() == null || !SophisticatedBackpackSupport.isAvailable()) {
            return List.of();
        }

        String excludedCarrier = encodeCarrier(SophisticatedBackpackSupport.openedBackpackCarrier(context.activeMenu()));
        Map<String, SophisticatedBackpackSupport.BackpackInventorySnapshot> initialBackpacksBySourceId =
                carriedBackpacksBySourceId(context.playerInventory().player, excludedCarrier);
        return List.of(new PlayerInventoryExtension() {
            @Override
            public String providerId() {
                return CARRIED_PROVIDER_ID;
            }

            @Override
            public List<InventorySourceDescriptor> additionalSources() {
                return initialBackpacksBySourceId.values().stream()
                        .map(snapshot -> InventorySourceDescriptor.builder(carriedSourceId(snapshot.stableContainerId()))
                                .label(Component.translatable("slot.source.backpack"))
                                .domain(InventorySourceDomain.PLAYER_EXTENSION)
                                .role(InventorySourceRole.PROVIDER_DEFINED)
                                .logicalSlotCount(snapshot.slotCount())
                                .bindingRoute(InventoryBindingRoute.PROVIDER)
                                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                                .paneMembership(InventoryPaneMembership.CARRIED)
                                .diagnostics(encodeCarrier(snapshot.carrier()))
                                .stableOrder(15 + Math.max(0, snapshot.carrier().carrierSlotIndex()))
                                .build())
                        .toList();
            }

            @Override
            public InventorySourceSnapshot readSourceSnapshot(
                    LocalPlayer player,
                    InventoryHostDescriptor host,
                    String sourceId
            ) {
                return carriedBackpackSourceSnapshot(player, sourceId, excludedCarrier);
            }

            @Override
            public InventorySourceSnapshot readSourceSnapshot(
                    ServerPlayer player,
                    InventoryHostDescriptor host,
                    String sourceId
            ) {
                return carriedBackpackSourceSnapshot(player, sourceId, excludedCarrier);
            }

            @Override
            public int slotCapacity(
                    LocalPlayer player,
                    InventoryHostDescriptor host,
                    String sourceId
            ) {
                SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot = findCarriedBackpack(player, sourceId, excludedCarrier);
                return snapshot == null ? 0 : snapshot.slotCount();
            }

            @Override
            public int serverSlotCapacity(ServerPlayer player, InventoryHostDescriptor host, String sourceId) {
                SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot = findCarriedBackpack(player, sourceId, excludedCarrier);
                return snapshot == null ? 0 : snapshot.slotCount();
            }

            @Override
            public MutationResult mutate(
                    InventoryHostDescriptor host,
                    InventoryMutationRequest request,
                    InventoryMutationMode mode
            ) {
                if (request == null || request.player() == null) {
                    return MutationResult.blocked("unsupported_source", request == null ? ItemStack.EMPTY : request.stack());
                }
                InventorySourceDescriptor source = host == null ? null : host.source(request.sourceId());
                SophisticatedBackpackSupport.BackpackCarrierRef carrier = decodeCarrier(source == null ? "" : source.diagnostics());
                if (carrier == null) {
                    return MutationResult.blocked("unsupported_source", request.stack());
                }

                return switch (request.kind()) {
                    case INSERT -> mutateInsert(request, carrier, mode);
                    case EXTRACT -> mutateExtract(request, carrier, mode);
                    case ACTIVATE_TARGET, UNSPECIFIED -> MutationResult.blocked("unsupported_mutation", request.stack());
                };
            }

            private MutationResult mutateInsert(
                    InventoryMutationRequest request,
                    SophisticatedBackpackSupport.BackpackCarrierRef carrier,
                    InventoryMutationMode mode
            ) {
                if (request.stack() == null || request.stack().isEmpty()) {
                    return MutationResult.success(ItemStack.EMPTY);
                }
                if (request.targetsExactSlot()) {
                    ItemStack remainder = SophisticatedBackpackTransferSupport.insertIntoBackpackSlot(
                            request.player(),
                            carrier,
                            request.slotIndex(),
                            request.stack(),
                            mode == InventoryMutationMode.SIMULATE,
                            new LinkedHashMap<>()
                    );
                    if (mode == InventoryMutationMode.SIMULATE) {
                        return remainder.isEmpty()
                                ? MutationResult.success(ItemStack.EMPTY)
                                : MutationResult.blocked("simulation_incomplete", remainder);
                    }
                    return MutationResult.success(remainder);
                }
                if (mode == InventoryMutationMode.SIMULATE) {
                    return SophisticatedBackpackTransferSupport.canFullyInsertIntoBackpack(request.player(), carrier, request.stack())
                            ? MutationResult.success(ItemStack.EMPTY)
                            : MutationResult.blocked("simulation_incomplete", request.stack());
                }

                ItemStack remainder = SophisticatedBackpackTransferSupport.insertIntoBackpack(
                        request.player(),
                        carrier,
                        request.stack(),
                        new LinkedHashMap<>()
                );
                return MutationResult.success(remainder);
            }

            private MutationResult mutateExtract(
                    InventoryMutationRequest request,
                    SophisticatedBackpackSupport.BackpackCarrierRef carrier,
                    InventoryMutationMode mode
            ) {
                if (request.targetsExactSlot()) {
                    ItemStack preview = SophisticatedBackpackTransferSupport.previewBackpackSlot(request.player(), carrier, request.slotIndex());
                    if (preview.isEmpty()
                            || (request.identity() != null && !dev.imagio.slot.inventory.core.ItemIdentityMatcher.matchesMovable(preview, request.identity()))) {
                        return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
                    }
                    int amount = switch (request.transferMode()) {
                        case ONE -> 1;
                        case STACK, ALL -> preview.getCount();
                    };
                    if (mode == InventoryMutationMode.SIMULATE) {
                        preview.setCount(Math.min(amount, preview.getCount()));
                        return MutationResult.success(preview);
                    }
                    ItemStack extracted = SophisticatedBackpackTransferSupport.extractBackpackSlot(
                            request.player(),
                            carrier,
                            request.slotIndex(),
                            amount,
                            false,
                            new LinkedHashMap<>()
                    );
                    return extracted.isEmpty()
                            ? MutationResult.blocked("no_matching_stack", ItemStack.EMPTY)
                            : MutationResult.success(extracted);
                }
                if (request.identity() == null) {
                    return MutationResult.blocked("missing_identity", ItemStack.EMPTY);
                }
                if (mode == InventoryMutationMode.SIMULATE) {
                    ItemStack preview = SophisticatedBackpackTransferSupport.copyFirstMatchingBackpackStack(request.player(), carrier, request.identity());
                    if (preview.isEmpty()) {
                        return MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
                    }
                    preview.setCount(switch (request.transferMode()) {
                        case ONE -> 1;
                        case STACK, ALL -> preview.getCount();
                    });
                    return MutationResult.success(preview);
                }

                MutableInsertCapture capture = new MutableInsertCapture();
                boolean moved = SophisticatedBackpackTransferSupport.moveMatchingBackpackStack(
                        request.player(),
                        carrier,
                        request.identity(),
                        switch (request.transferMode()) {
                            case ONE -> 1;
                            case STACK, ALL -> Integer.MAX_VALUE;
                        },
                        capture::capture,
                        new LinkedHashMap<java.util.UUID, CompoundTag>()
                );
                return moved ? MutationResult.success(capture.result()) : MutationResult.blocked("no_matching_stack", ItemStack.EMPTY);
            }
        });
    }

    private static ResolvedToolArtifacts resolveToolArtifacts(
            net.minecraft.world.inventory.AbstractContainerMenu menu
    ) {
        List<InventoryToolDescriptor> descriptors = new ArrayList<>();
        List<InventorySourceDescriptor> sources = new ArrayList<>();
        LinkedHashMap<String, List<Integer>> sourceSlots = new LinkedHashMap<>();
        LinkedHashMap<String, List<Integer>> toolRegionSlots = new LinkedHashMap<>();
        for (SophisticatedBackpackUpgradeSupport.CraftingUpgradePanelRef panelRef : SophisticatedBackpackUpgradeSupport.findCraftingUpgrades(menu)) {
            if (panelRef == null) {
                continue;
            }
            String inputSourceId = panelRef.toolId() + "/source/input";
            String outputSourceId = panelRef.toolId() + "/source/output";
            String inputRegionId = panelRef.toolId() + "/input";
            String outputRegionId = panelRef.toolId() + "/output";
            List<ToolRegionDescriptor> regions = panelRef.inputSlots().isEmpty() || panelRef.resultSlot() < 0
                    ? List.of()
                    : List.of(
                            new ToolRegionDescriptor(
                                    inputRegionId,
                                    ToolRegionRole.INPUT,
                                    panelRef.inputSlots().size(),
                                    InventoryBindingRoute.MENU,
                                    Set.of(InventoryCapability.TOOL_REGION_MUTATION, InventoryCapability.INSERT, InventoryCapability.EXTRACT),
                                    true,
                                    inputSourceId,
                                    ""
                            ),
                            new ToolRegionDescriptor(
                                    outputRegionId,
                                    ToolRegionRole.OUTPUT,
                                    1,
                                    InventoryBindingRoute.MENU,
                                    Set.of(InventoryCapability.TOOL_REGION_MUTATION, InventoryCapability.EXTRACT),
                                    true,
                                    outputSourceId,
                                    ""
                            )
                    );
            if (!panelRef.inputSlots().isEmpty()) {
                toolRegionSlots.put(inputRegionId, List.copyOf(panelRef.inputSlots()));
                sourceSlots.put(inputSourceId, List.copyOf(panelRef.inputSlots()));
                sources.add(InventorySourceDescriptor.builder(inputSourceId)
                        .label(Component.literal(panelRef.title().getString() + " Input"))
                        .domain(InventorySourceDomain.TOOL_REGION)
                        .role(InventorySourceRole.PROVIDER_DEFINED)
                        .logicalSlotCount(panelRef.inputSlots().size())
                        .bindingRoute(InventoryBindingRoute.MENU)
                        .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                        .actionRoute(InventoryActionRoute.MENU_MUTATION)
                        .paneMembership(InventoryPaneMembership.HIDDEN)
                        .stableOrder(200)
                        .build());
            }
            if (panelRef.resultSlot() >= 0) {
                toolRegionSlots.put(outputRegionId, List.of(panelRef.resultSlot()));
                sourceSlots.put(outputSourceId, List.of(panelRef.resultSlot()));
                sources.add(InventorySourceDescriptor.builder(outputSourceId)
                        .label(Component.literal(panelRef.title().getString() + " Output"))
                        .domain(InventorySourceDomain.TOOL_REGION)
                        .role(InventorySourceRole.PROVIDER_DEFINED)
                        .logicalSlotCount(1)
                        .bindingRoute(InventoryBindingRoute.MENU)
                        .capabilities(Set.of(InventoryCapability.EXTRACT))
                        .actionRoute(InventoryActionRoute.MENU_MUTATION)
                        .paneMembership(InventoryPaneMembership.HIDDEN)
                        .stableOrder(201)
                        .build());
            }
            descriptors.add(new InventoryToolDescriptor(
                    panelRef.toolId(),
                    OPEN_PROVIDER_ID,
                    InventoryToolKind.CRAFTING_GRID,
                    panelRef.title(),
                    new ToolPresentationHints(
                            panelRef.title().getString(),
                            70,
                            "docked",
                            panelRef.preferredHeight()
                    ),
                    70,
                    panelRef.hasLiveToolPanel(),
                    panelRef.tabId() >= 0,
                    panelRef.hasLiveToolPanel(),
                    panelRef.tabId() >= 0
                            ? new ToolActivationToken(OPEN_PROVIDER_ID, panelRef.toolId(), Map.of("tabId", Integer.toString(panelRef.tabId())))
                            : null,
                    regions,
                    List.of(
                            new InventoryToolAction("clear_grid", InventoryToolActionId.CLEAR_GRID, Component.translatable("slot.tool.action.clear_grid"), Component.empty()),
                            new InventoryToolAction("balance_grid", InventoryToolActionId.BALANCE_GRID, Component.translatable("slot.tool.action.balance_grid"), Component.empty()),
                            new InventoryToolAction("rotate_grid", InventoryToolActionId.ROTATE_GRID, Component.translatable("slot.tool.action.rotate_grid"), Component.empty())
                    ),
                    panelRef.supportsAutoRefillToggle()
                            ? List.of(new InventoryToolToggle(
                            "auto_refill",
                            InventoryToolToggleId.AUTO_REFILL,
                            Component.translatable("slot.tool.toggle.auto_refill"),
                            Component.empty()
                    ))
                            : List.of(),
                    panelRef.supportsAutoRefillToggle()
                            ? Map.of(InventoryToolToggleId.AUTO_REFILL, SophisticatedBackpackUpgradeSupport.shouldRefillCraftingGrid(menu))
                            : Map.of(),
                    Map.of("tabId", Integer.toString(panelRef.tabId())),
                    new CraftingSurfaceDescriptor(
                            java.util.stream.IntStream.range(0, panelRef.inputSlots().size())
                                    .mapToObj(index -> new InventoryActionTarget.SourceSlotTarget(inputSourceId, index))
                                    .toList(),
                            panelRef.resultSlot() < 0 ? null : new InventoryActionTarget.SourceSlotTarget(outputSourceId, 0),
                            true,
                            true,
                            true,
                            true,
                            ""
                    ),
                    ""
            ));
        }

        for (SophisticatedBackpackUpgradeSupport.UpgradeTabRef tabRef : SophisticatedBackpackUpgradeSupport.findNonCraftingUpgradeTabs(menu)) {
            descriptors.add(new InventoryToolDescriptor(
                    tabRef.toolId(),
                    OPEN_PROVIDER_ID,
                    inferToolKind(tabRef.containerClassName()),
                    tabRef.title(),
                    new ToolPresentationHints(tabRef.title().getString(), 40, "docked", 0),
                    40,
                    true,
                    tabRef.tabId() >= 0,
                    tabRef.open(),
                    tabRef.tabId() >= 0
                            ? new ToolActivationToken(OPEN_PROVIDER_ID, tabRef.toolId(), Map.of("tabId", Integer.toString(tabRef.tabId())))
                            : null,
                    List.of(),
                    List.of(),
                    List.of(),
                    Map.of(),
                    Map.of(
                            "tabId", Integer.toString(tabRef.tabId()),
                            "containerClass", tabRef.containerClassName()
                    ),
                    ""
            ));
        }

        return new ResolvedToolArtifacts(
                List.copyOf(sources),
                List.copyOf(descriptors),
                Map.copyOf(sourceSlots),
                Map.copyOf(toolRegionSlots)
        );
    }

    private static Map<String, SophisticatedBackpackSupport.BackpackInventorySnapshot> carriedBackpacksBySourceId(
            Player player,
            String excludedCarrier
    ) {
        LinkedHashMap<String, SophisticatedBackpackSupport.BackpackInventorySnapshot> backpacksBySourceId = new LinkedHashMap<>();
        for (SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot :
                SophisticatedBackpackSupport.readPlayerBackpacks(player, decodeCarrier(excludedCarrier))) {
            backpacksBySourceId.put(carriedSourceId(snapshot.stableContainerId()), snapshot);
        }
        return Map.copyOf(backpacksBySourceId);
    }

    private static SophisticatedBackpackSupport.BackpackInventorySnapshot findCarriedBackpack(
            Player player,
            String sourceId,
            String excludedCarrier
    ) {
        if (player == null || sourceId == null || sourceId.isBlank()) {
            return null;
        }
        return carriedBackpacksBySourceId(player, excludedCarrier).get(sourceId);
    }

    private static InventorySourceSnapshot carriedBackpackSourceSnapshot(
            Player player,
            String sourceId,
            String excludedCarrier
    ) {
        SophisticatedBackpackSupport.BackpackInventorySnapshot snapshot = findCarriedBackpack(player, sourceId, excludedCarrier);
        if (snapshot == null) {
            return InventorySourceSnapshot.empty(sourceId == null || sourceId.isBlank() ? "__missing__" : sourceId);
        }
        List<InventoryEntrySnapshot> entries = snapshot.entries().stream()
                .map(entry -> new InventoryEntrySnapshot(
                        InventoryEntryKey.slot(sourceId, entry.slotIndex()),
                        entry.stack(),
                        entry.stack().getCount(),
                        ""
                ))
                .toList();
        return new InventorySourceSnapshot(sourceId, snapshot.slotCount(), entries, "");
    }

    private static String carriedSourceId(String stableContainerId) {
        return CARRIED_PROVIDER_ID + "/" + (stableContainerId == null ? "" : stableContainerId);
    }

    private static InventoryToolKind inferToolKind(String containerClassName) {
        String normalized = containerClassName == null ? "" : containerClassName.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("filter")) {
            return InventoryToolKind.FILTER;
        }
        if (normalized.contains("settings") || normalized.contains("config")) {
            return InventoryToolKind.INVENTORY_CONFIG;
        }
        if (normalized.contains("smelt")) {
            return InventoryToolKind.SMELTING_GRID;
        }
        return InventoryToolKind.PROVIDER_DEFINED;
    }

    private static boolean isBackpackStorage(net.minecraft.world.inventory.AbstractContainerMenu menu, String screenClassName) {
        String resolvedScreenClassName = screenClassName == null ? "" : screenClassName;
        return menu != null && (
                classChainContains(menu.getClass(), "net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer")
                        || "net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen".equals(resolvedScreenClassName)
        );
    }

    private static boolean classChainContains(Class<?> type, String className) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (className.equals(current.getName())) {
                return true;
            }
        }
        return false;
    }

    private static String encodeCarrier(SophisticatedBackpackSupport.BackpackCarrierRef carrier) {
        if (carrier == null) {
            return "";
        }
        return carrier.handlerName() + "|" + carrier.identifier() + "|" + carrier.carrierSlotIndex();
    }

    private static SophisticatedBackpackSupport.BackpackCarrierRef decodeCarrier(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return null;
        }
        String[] parts = encoded.split("\\|", 3);
        if (parts.length != 3) {
            return null;
        }
        try {
            return new SophisticatedBackpackSupport.BackpackCarrierRef(parts[0], parts[1], Integer.parseInt(parts[2]));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static final class MutableInsertCapture {
        private ItemStack inserted = ItemStack.EMPTY;

        private ItemStack capture(ItemStack stack) {
            inserted = stack.copy();
            return ItemStack.EMPTY;
        }

        private ItemStack result() {
            return inserted;
        }
    }

    private record ResolvedToolArtifacts(
            List<InventorySourceDescriptor> sources,
            List<InventoryToolDescriptor> tools,
            Map<String, List<Integer>> sourceSlots,
            Map<String, List<Integer>> regionSlots
    ) {
    }
}
