package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.core.CraftingSurfaceDescriptor;
import dev.imagio.slot.inventory.core.InventoryActionRoute;
import dev.imagio.slot.inventory.core.InventoryBindingRoute;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventoryPaneMembership;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDomain;
import dev.imagio.slot.inventory.core.InventorySourceRole;
import dev.imagio.slot.inventory.core.InventoryToolAction;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolKind;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ToolPresentationHints;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionRole;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VanillaCraftingInventoryIntegrationProvider implements InventoryIntegrationProvider {
    private static final String PROVIDER_ID = "vanilla_crafting";

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public int priority() {
        return 90;
    }

    @Override
    public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
        if (context == null || context.menu() == null || context.playerInventory() == null) {
            return ProviderResult.unsupported(providerId(), "missing_context", "Vanilla crafting host context was missing");
        }

        SurfaceRef surfaceRef = resolveSurface(context.menu(), context.playerInventory());
        if (surfaceRef == null) {
            return ProviderResult.unsupported(providerId(), "unsupported_menu", "Menu is not a supported vanilla crafting surface");
        }

        InventorySourceDescriptor inputSource = InventorySourceDescriptor.builder(surfaceRef.inputSourceId())
                .label(Component.literal("Crafting Input"))
                .domain(InventorySourceDomain.TOOL_REGION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(surfaceRef.inputMenuSlots().size())
                .bindingRoute(InventoryBindingRoute.MENU)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.HIDDEN)
                .stableOrder(200)
                .build();
        InventorySourceDescriptor outputSource = InventorySourceDescriptor.builder(surfaceRef.outputSourceId())
                .label(Component.literal("Crafting Output"))
                .domain(InventorySourceDomain.TOOL_REGION)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .logicalSlotCount(1)
                .bindingRoute(InventoryBindingRoute.MENU)
                .capabilities(Set.of(InventoryCapability.EXTRACT))
                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.HIDDEN)
                .stableOrder(201)
                .build();

        ToolRegionDescriptor inputRegion = new ToolRegionDescriptor(
                surfaceRef.inputRegionId(),
                ToolRegionRole.INPUT,
                surfaceRef.inputMenuSlots().size(),
                InventoryBindingRoute.MENU,
                Set.of(InventoryCapability.TOOL_REGION_MUTATION, InventoryCapability.INSERT, InventoryCapability.EXTRACT),
                true,
                surfaceRef.inputSourceId(),
                ""
        );
        ToolRegionDescriptor outputRegion = new ToolRegionDescriptor(
                surfaceRef.outputRegionId(),
                ToolRegionRole.OUTPUT,
                1,
                InventoryBindingRoute.MENU,
                Set.of(InventoryCapability.TOOL_REGION_MUTATION, InventoryCapability.EXTRACT),
                true,
                surfaceRef.outputSourceId(),
                ""
        );

        List<InventoryToolAction> actions = surfaceRef.gridWidth() == 3
                ? List.of(
                new InventoryToolAction("clear_grid", InventoryToolActionId.CLEAR_GRID, Component.translatable("slot.tool.action.clear_grid"), Component.empty()),
                new InventoryToolAction("balance_grid", InventoryToolActionId.BALANCE_GRID, Component.translatable("slot.tool.action.balance_grid"), Component.empty()),
                new InventoryToolAction("rotate_grid", InventoryToolActionId.ROTATE_GRID, Component.translatable("slot.tool.action.rotate_grid"), Component.empty())
        )
                : List.of(
                new InventoryToolAction("clear_grid", InventoryToolActionId.CLEAR_GRID, Component.translatable("slot.tool.action.clear_grid"), Component.empty()),
                new InventoryToolAction("balance_grid", InventoryToolActionId.BALANCE_GRID, Component.translatable("slot.tool.action.balance_grid"), Component.empty())
        );

        InventoryToolDescriptor craftingTool = new InventoryToolDescriptor(
                surfaceRef.toolId(),
                providerId(),
                InventoryToolKind.CRAFTING_GRID,
                Component.translatable("slot.screen.container.tool_panel.crafting"),
                new ToolPresentationHints(
                        Component.translatable("slot.screen.container.tool_panel.crafting").getString(),
                        70,
                        "docked",
                        70
                ),
                70,
                true,
                true,
                true,
                null,
                List.of(inputRegion, outputRegion),
                actions,
                List.of(),
                Map.of(),
                Map.of("surfaceType", surfaceRef.scopeId()),
                new CraftingSurfaceDescriptor(
                        java.util.stream.IntStream.range(0, surfaceRef.inputMenuSlots().size())
                                .mapToObj(index -> new InventoryActionTarget.SourceSlotTarget(surfaceRef.inputSourceId(), index))
                                .toList(),
                        new InventoryActionTarget.SourceSlotTarget(surfaceRef.outputSourceId(), 0),
                        surfaceRef.gridWidth(),
                        surfaceRef.gridHeight(),
                        true,
                        true,
                        true,
                        surfaceRef.gridWidth() == 3,
                        ""
                ),
                ""
        );

        return ProviderResult.supported(new InventoryHostSession() {
            @Override
            public String providerId() {
                return PROVIDER_ID;
            }

            @Override
            public String providerScopeId() {
                return surfaceRef.scopeId();
            }

            @Override
            public List<InventorySourceDescriptor> hostSources() {
                return List.of(inputSource, outputSource);
            }

            @Override
            public InventoryTopologyDescriptor topology() {
                LinkedHashMap<String, List<Integer>> menuSlotsBySource = new LinkedHashMap<>();
                menuSlotsBySource.put(inputSource.id(), surfaceRef.inputMenuSlots());
                menuSlotsBySource.put(outputSource.id(), surfaceRef.outputMenuSlots());

                LinkedHashMap<Integer, String> sourceIdByMenuSlot = new LinkedHashMap<>();
                for (int menuSlot : surfaceRef.inputMenuSlots()) {
                    sourceIdByMenuSlot.put(menuSlot, inputSource.id());
                }
                for (int menuSlot : surfaceRef.outputMenuSlots()) {
                    sourceIdByMenuSlot.put(menuSlot, outputSource.id());
                }

                return new InventoryTopologyDescriptor(
                        Map.copyOf(menuSlotsBySource),
                        Map.copyOf(sourceIdByMenuSlot),
                        Map.of(
                                surfaceRef.inputRegionId(), surfaceRef.inputMenuSlots(),
                                surfaceRef.outputRegionId(), surfaceRef.outputMenuSlots()
                        )
                );
            }

            @Override
            public List<InventoryToolDescriptor> tools() {
                return List.of(craftingTool);
            }

            @Override
            public InventorySourceSnapshot readSourceSnapshot(InventoryHostDescriptor host, String sourceId) {
                if (inputSource.id().equals(sourceId)) {
                    return MenuBackedHostSupport.readSourceSnapshot(host.menu(), sourceId, surfaceRef.inputMenuSlots());
                }
                if (outputSource.id().equals(sourceId)) {
                    return MenuBackedHostSupport.readSourceSnapshot(host.menu(), sourceId, surfaceRef.outputMenuSlots());
                }
                return InventorySourceSnapshot.empty(sourceId == null || sourceId.isBlank() ? "__missing__" : sourceId);
            }

            @Override
            public MutationResult mutate(
                    InventoryHostDescriptor host,
                    InventoryMutationRequest request,
                    InventoryMutationMode mode
            ) {
                if (host == null || request == null) {
                    return MutationResult.blocked("unsupported_source", request == null ? null : request.stack());
                }
                if (inputSource.id().equals(request.sourceId())) {
                    return MenuBackedHostSupport.mutateMenuSlots(host, request, mode, surfaceRef.inputMenuSlots());
                }
                if (outputSource.id().equals(request.sourceId())) {
                    return MenuBackedHostSupport.mutateMenuSlots(host, request, mode, surfaceRef.outputMenuSlots());
                }
                return MutationResult.blocked("unsupported_source", request.stack());
            }

            @Override
            public ToolActionResult executeToolAction(
                    InventoryHostDescriptor host,
                    String toolId,
                    InventoryToolActionId actionId,
                    InventoryActionMode mode
            ) {
                if (host == null || !surfaceRef.toolId().equals(toolId)) {
                    return ToolActionResult.blocked("unsupported_tool");
                }
                return MenuBackedToolActionExecutor.execute(host, craftingTool, actionId, mode);
            }
        });
    }

    private static SurfaceRef resolveSurface(
            AbstractContainerMenu menu,
            Inventory playerInventory
    ) {
        if (!(menu instanceof InventoryMenu) && !(menu instanceof CraftingMenu)) {
            return null;
        }

        Container inputContainer = null;
        Container outputContainer = null;
        java.util.ArrayList<Integer> inputSlots = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> outputSlots = new java.util.ArrayList<>();
        for (int menuSlot = 0; menuSlot < menu.slots.size(); menuSlot++) {
            Slot slot = menu.getSlot(menuSlot);
            if (slot == null || slot.container == null || slot.container == playerInventory) {
                continue;
            }
            if (slot.container instanceof ResultContainer) {
                outputContainer = slot.container;
                outputSlots.add(menuSlot);
                continue;
            }
            if (slot.container instanceof CraftingContainer) {
                inputContainer = slot.container;
                inputSlots.add(menuSlot);
            }
        }
        if (inputContainer == null || outputContainer == null || outputSlots.size() != 1) {
            return null;
        }

        int gridWidth;
        int gridHeight;
        String scopeId;
        String toolId;
        if (menu instanceof InventoryMenu && inputSlots.size() == 4) {
            gridWidth = 2;
            gridHeight = 2;
            scopeId = "inventory_menu";
            toolId = "minecraft:player_crafting";
        } else if (menu instanceof CraftingMenu && inputSlots.size() == 9) {
            gridWidth = 3;
            gridHeight = 3;
            scopeId = "crafting_menu";
            toolId = "minecraft:crafting_table";
        } else {
            return null;
        }

        return new SurfaceRef(
                scopeId,
                toolId,
                toolId + "/input",
                toolId + "/output",
                toolId + "/input/source",
                toolId + "/output/source",
                List.copyOf(inputSlots),
                List.copyOf(outputSlots),
                gridWidth,
                gridHeight
        );
    }

    private record SurfaceRef(
            String scopeId,
            String toolId,
            String inputRegionId,
            String outputRegionId,
            String inputSourceId,
            String outputSourceId,
            List<Integer> inputMenuSlots,
            List<Integer> outputMenuSlots,
            int gridWidth,
            int gridHeight
    ) {
    }
}
