package dev.imagio.slot.inventory.integration;

import dev.imagio.slot.SlotDebugLog;
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
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.InventoryToolAction;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolKind;
import dev.imagio.slot.inventory.core.InventoryToolToggle;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.InventoryTopologyDescriptor;
import dev.imagio.slot.inventory.core.ToolPresentationHints;
import dev.imagio.slot.inventory.core.ToolRegionDescriptor;
import dev.imagio.slot.inventory.core.ToolRegionRole;
import dev.imagio.slot.inventory.query.InventoryEntryKey;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.registry.ProviderResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class TomsStorageInventoryIntegrationProvider implements InventoryIntegrationProvider {
    private static final String PROVIDER_ID = "toms_storage_terminal";
    private static final String PRIMARY_SOURCE_ID = "toms_storage:terminal";
    private static final String CRAFTING_TOOL_ID = "toms_storage:crafting_terminal";
    private static final String CRAFTING_INPUT_REGION = "toms_storage:crafting_terminal/input";
    private static final String CRAFTING_OUTPUT_REGION = "toms_storage:crafting_terminal/output";
    private static final ReflectionState REFLECTION = ReflectionState.load();
    private static final WeakHashMap<AbstractContainerMenu, String> LAST_CLIENT_STATE_LOG = new WeakHashMap<>();

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public ProviderResult<InventoryHostSession> openHost(InventoryHostContext context) {
        if (!REFLECTION.available()) {
            return ProviderResult.unsupported(providerId(), "reflection_unavailable", "Tom's Storage classes are not available");
        }
        if (context == null || context.menu() == null || !REFLECTION.matches(context.menu(), context.screenClassName())) {
            return ProviderResult.unsupported(providerId(), "unsupported_menu", "Menu is not a Tom's Storage terminal");
        }

        boolean craftingTerminal = REFLECTION.isCraftingTerminal(context.menu(), context.screenClassName());
        String label = context.title() == null || context.title().getString().isBlank()
                ? Component.translatable("menu.toms_storage.storage_terminal").getString()
                : context.title().getString();
        InventorySourceDescriptor primarySource = InventorySourceDescriptor.builder(PRIMARY_SOURCE_ID)
                .label(Component.literal(label))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .logicalSlotCount(0)
                .bindingRoute(InventoryBindingRoute.PROVIDER)
                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                .simulationSupported(false)
                .actionRoute(InventoryActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.EXTERNAL)
                .stableOrder(0)
                .build();
        InventoryToolDescriptor craftingTool = craftingTerminal ? new InventoryToolDescriptor(
                CRAFTING_TOOL_ID,
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
                false,
                true,
                null,
                List.of(
                        new ToolRegionDescriptor(
                                CRAFTING_INPUT_REGION,
                                ToolRegionRole.INPUT,
                                9,
                                InventoryBindingRoute.MENU,
                                Set.of(InventoryCapability.TOOL_REGION_MUTATION, InventoryCapability.INSERT, InventoryCapability.EXTRACT),
                                true,
                                CRAFTING_INPUT_REGION + "/source",
                                ""
                        ),
                        new ToolRegionDescriptor(
                                CRAFTING_OUTPUT_REGION,
                                ToolRegionRole.OUTPUT,
                                1,
                                InventoryBindingRoute.MENU,
                                Set.of(InventoryCapability.TOOL_REGION_MUTATION, InventoryCapability.EXTRACT),
                                true,
                                CRAFTING_OUTPUT_REGION + "/source",
                                ""
                        )
                ),
                List.of(
                        new InventoryToolAction("clear_grid", InventoryToolActionId.CLEAR_GRID, Component.translatable("slot.tool.action.clear_grid"), Component.empty()),
                        new InventoryToolAction("balance_grid", InventoryToolActionId.BALANCE_GRID, Component.translatable("slot.tool.action.balance_grid"), Component.empty()),
                        new InventoryToolAction("rotate_grid", InventoryToolActionId.ROTATE_GRID, Component.translatable("slot.tool.action.rotate_grid"), Component.empty())
                ),
                List.of(),
                Map.of(),
                Map.of("terminalType", "crafting"),
                new CraftingSurfaceDescriptor(
                        java.util.stream.IntStream.range(0, 9)
                                .mapToObj(index -> new InventoryActionTarget.SourceSlotTarget(CRAFTING_INPUT_REGION + "/source", index))
                                .toList(),
                        new InventoryActionTarget.SourceSlotTarget(CRAFTING_OUTPUT_REGION + "/source", 0),
                        3,
                        3,
                        true,
                        true,
                        true,
                        true,
                        ""
                ),
                ""
        ) : null;

        return ProviderResult.supported(new InventoryHostSession() {
            @Override
            public String providerId() {
                return PROVIDER_ID;
            }

            @Override
            public String providerScopeId() {
                return craftingTerminal ? "crafting_terminal" : "storage_terminal";
            }

            @Override
            public List<InventorySourceDescriptor> hostSources() {
                if (!craftingTerminal) {
                    return List.of(primarySource);
                }
                return List.of(
                        primarySource,
                        InventorySourceDescriptor.builder(CRAFTING_INPUT_REGION + "/source")
                                .label(Component.literal("Crafting Input"))
                                .domain(InventorySourceDomain.TOOL_REGION)
                                .role(InventorySourceRole.PROVIDER_DEFINED)
                                .logicalSlotCount(9)
                                .bindingRoute(InventoryBindingRoute.MENU)
                                .capabilities(Set.of(InventoryCapability.INSERT, InventoryCapability.EXTRACT))
                                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                                .paneMembership(InventoryPaneMembership.HIDDEN)
                                .stableOrder(200)
                                .build(),
                        InventorySourceDescriptor.builder(CRAFTING_OUTPUT_REGION + "/source")
                                .label(Component.literal("Crafting Output"))
                                .domain(InventorySourceDomain.TOOL_REGION)
                                .role(InventorySourceRole.PROVIDER_DEFINED)
                                .logicalSlotCount(1)
                                .bindingRoute(InventoryBindingRoute.MENU)
                                .capabilities(Set.of(InventoryCapability.EXTRACT))
                                .actionRoute(InventoryActionRoute.MENU_MUTATION)
                                .paneMembership(InventoryPaneMembership.HIDDEN)
                                .stableOrder(201)
                                .build()
                );
            }

            @Override
            public InventoryTopologyDescriptor topology() {
                return craftingTerminal
                        ? new InventoryTopologyDescriptor(
                                Map.of(
                                        CRAFTING_INPUT_REGION + "/source", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9),
                                        CRAFTING_OUTPUT_REGION + "/source", List.of(0)
                                ),
                                Map.of(
                                        1, CRAFTING_INPUT_REGION + "/source",
                                        2, CRAFTING_INPUT_REGION + "/source",
                                        3, CRAFTING_INPUT_REGION + "/source",
                                        4, CRAFTING_INPUT_REGION + "/source",
                                        5, CRAFTING_INPUT_REGION + "/source",
                                        6, CRAFTING_INPUT_REGION + "/source",
                                        7, CRAFTING_INPUT_REGION + "/source",
                                        8, CRAFTING_INPUT_REGION + "/source",
                                        9, CRAFTING_INPUT_REGION + "/source",
                                        0, CRAFTING_OUTPUT_REGION + "/source"
                                ),
                                Map.of(
                                        CRAFTING_INPUT_REGION, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9),
                                        CRAFTING_OUTPUT_REGION, List.of(0)
                                )
                        )
                        : InventoryTopologyDescriptor.empty();
            }

            @Override
            public List<InventoryToolDescriptor> tools() {
                return craftingTool == null ? List.of() : List.of(craftingTool);
            }

            @Override
            public InventorySourceSnapshot readSourceSnapshot(InventoryHostDescriptor host, String sourceId) {
                if (host == null) {
                    return InventorySourceSnapshot.empty(sourceId == null || sourceId.isBlank() ? "__missing__" : sourceId);
                }
                if (CRAFTING_INPUT_REGION.concat("/source").equals(sourceId)) {
                    return MenuBackedHostSupport.readSourceSnapshot(host.menu(), sourceId, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
                }
                if (CRAFTING_OUTPUT_REGION.concat("/source").equals(sourceId)) {
                    return MenuBackedHostSupport.readSourceSnapshot(host.menu(), sourceId, List.of(0));
                }
                if (!PRIMARY_SOURCE_ID.equals(sourceId)) {
                    return InventorySourceSnapshot.empty(sourceId);
                }
                return providerSourceSnapshot(host.menu());
            }

            @Override
            public MutationResult mutate(
                    InventoryHostDescriptor host,
                    InventoryMutationRequest request,
                    InventoryMutationMode mode
            ) {
                if (host == null || request == null || !PRIMARY_SOURCE_ID.equals(request.sourceId())) {
                    if (host == null || request == null) {
                        return MutationResult.blocked("unsupported_source", request == null ? null : request.stack());
                    }
                    if (CRAFTING_INPUT_REGION.concat("/source").equals(request.sourceId())) {
                        return MenuBackedHostSupport.mutateMenuSlots(host, request, mode, List.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
                    }
                    if (CRAFTING_OUTPUT_REGION.concat("/source").equals(request.sourceId())) {
                        return MenuBackedHostSupport.mutateMenuSlots(host, request, mode, List.of(0));
                    }
                    return MutationResult.blocked("unsupported_source", request.stack());
                }
                if (mode == InventoryMutationMode.SIMULATE) {
                    return MutationResult.blocked("provider_does_not_support_simulation", request.stack());
                }

                return switch (request.kind()) {
                    case EXTRACT -> MutationResult.success(extract(
                            host.menu(),
                            request.entryId(),
                            request.identity(),
                            request.requestedCount(),
                            request.transferMode()
                    ));
                    case INSERT -> MutationResult.success(insert(host.menu(), request.stack()));
                    case ACTIVATE_TARGET, UNSPECIFIED -> MutationResult.blocked("unsupported_mutation", request.stack());
                };
            }

            @Override
            public ToolActionResult executeToolAction(
                    InventoryHostDescriptor host,
                    String toolId,
                    dev.imagio.slot.inventory.core.InventoryToolActionId actionId,
                    InventoryActionMode mode
            ) {
                if (craftingTool == null || host == null || !CRAFTING_TOOL_ID.equals(toolId)) {
                    return ToolActionResult.blocked("unsupported_tool");
                }
                return MenuBackedToolActionExecutor.execute(host, craftingTool, actionId, mode);
            }
        });
    }

    private static InventorySourceSnapshot providerSourceSnapshot(AbstractContainerMenu menu) {
        ClientItemsView clientItemsView = REFLECTION.clientItems(menu);
        ArrayList<InventoryEntrySnapshot> entries = new ArrayList<>();
        for (Object storedStack : clientItemsView.items()) {
            ItemStack rawDisplay = REFLECTION.displayStack(storedStack);
            ItemStack actualStack = REFLECTION.actualStack(storedStack);
            if (rawDisplay.isEmpty() || actualStack.isEmpty()) {
                continue;
            }

            int quantity = clampToInt(REFLECTION.quantity(storedStack));
            ItemStack displayStack = rawDisplay.copy();
            displayStack.setCount(Math.min(Math.max(1, quantity), displayStack.getMaxStackSize()));
            entries.add(new InventoryEntrySnapshot(
                    InventoryEntryKey.providerEntry(PRIMARY_SOURCE_ID, providerEntryId(actualStack)),
                    displayStack,
                    quantity,
                    ""
            ));
        }
        logClientState(menu, clientItemsView, entries.size());
        return new InventorySourceSnapshot(PRIMARY_SOURCE_ID, 0, List.copyOf(entries), "");
    }

    private static void logClientState(AbstractContainerMenu menu, ClientItemsView clientItemsView, int snapshotCount) {
        if (!SlotDebugLog.enabled() || menu == null) {
            return;
        }

        String state = "source=" + clientItemsView.source()
                + " syncSize=" + clientItemsView.syncSize()
                + " clientSize=" + clientItemsView.clientSize()
                + " sortedSize=" + clientItemsView.sortedSize()
                + " rawSize=" + clientItemsView.rawSize()
                + " itemsLoaded=" + clientItemsView.itemsLoaded()
                + " snapshotCount=" + snapshotCount;
        String previous = LAST_CLIENT_STATE_LOG.get(menu);
        if (state.equals(previous)) {
            return;
        }

        LAST_CLIENT_STATE_LOG.put(menu, state);
        SlotDebugLog.log(
                "Tom's terminal provider client state: menu={} {}",
                menu.getClass().getName(),
                state
        );
    }

    private static ItemStack extract(
            AbstractContainerMenu menu,
            String entryId,
            dev.imagio.slot.inventory.core.ItemIdentity identity,
            int requestedCount,
            InventoryTransferMode mode
    ) {
        Object terminal = REFLECTION.terminal(menu);
        Object storedStack = entryId == null || entryId.isBlank()
                ? REFLECTION.findMatchingStoredStack(terminal, identity)
                : REFLECTION.findStoredStackByEntryId(terminal, entryId);
        if (terminal == null || storedStack == null) {
            return ItemStack.EMPTY;
        }

        long amount = requestedCount > 0
                ? requestedCount
                : switch (mode) {
            case ONE -> 1L;
            case STACK, ALL -> Math.max(1, REFLECTION.actualStack(storedStack).getMaxStackSize());
        };
        return REFLECTION.pullStack(terminal, storedStack, amount);
    }

    private static ItemStack insert(AbstractContainerMenu menu, ItemStack stack) {
        Object terminal = REFLECTION.terminal(menu);
        return terminal == null ? stack : REFLECTION.pushStack(terminal, stack);
    }

    private static String providerEntryId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        dev.imagio.slot.inventory.core.ItemIdentity identity = ItemIdentityMatcher.create(stack);
        if (identity == null) {
            return stack.getItem().toString();
        }
        return identity.itemId() + "|" + identity.componentFingerprint();
    }

    private static int clampToInt(long value) {
        if (value <= 0L) {
            return 0;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }

    private record ReflectionState(
            boolean available,
            Class<?> menuClass,
            Field terminalField,
            Field syncField,
            Field itemListField,
            Field itemListClientField,
            Field itemListClientSortedField,
            Field itemsLoadedField,
            Method getSyncAsListMethod,
            Method getStacksMethod,
            Method getStackMethod,
            Method getActualStackMethod,
            Method getQuantityMethod,
            Method pullStackMethod,
            Method pushStackMethod,
            Constructor<?> storedItemStackConstructor,
            Class<?> craftingMenuClass
    ) {
        private static ReflectionState load() {
            try {
                ClassLoader loader = TomsStorageInventoryIntegrationProvider.class.getClassLoader();
                Class<?> menuClass = Class.forName("com.tom.storagemod.menu.StorageTerminalMenu", false, loader);
                Class<?> craftingMenuClass = Class.forName("com.tom.storagemod.menu.CraftingTerminalMenu", false, loader);
                Class<?> storedItemStackClass = Class.forName("com.tom.storagemod.inventory.StoredItemStack", false, loader);
                Class<?> terminalClass = Class.forName("com.tom.storagemod.block.entity.StorageTerminalBlockEntity", false, loader);
                Class<?> syncClass = Class.forName("com.tom.storagemod.util.TerminalSyncManager", false, loader);

                Field terminalField = menuClass.getDeclaredField("te");
                terminalField.setAccessible(true);
                Field syncField = menuClass.getDeclaredField("sync");
                syncField.setAccessible(true);
                Field itemListField = menuClass.getDeclaredField("itemList");
                itemListField.setAccessible(true);
                Field itemListClientField = menuClass.getDeclaredField("itemListClient");
                itemListClientField.setAccessible(true);
                Field itemListClientSortedField = menuClass.getDeclaredField("itemListClientSorted");
                itemListClientSortedField.setAccessible(true);
                Field itemsLoadedField = menuClass.getDeclaredField("itemsLoaded");
                itemsLoadedField.setAccessible(true);

                return new ReflectionState(
                        true,
                        menuClass,
                        terminalField,
                        syncField,
                        itemListField,
                        itemListClientField,
                        itemListClientSortedField,
                        itemsLoadedField,
                        syncClass.getMethod("getAsList"),
                        terminalClass.getMethod("getStacks"),
                        storedItemStackClass.getMethod("getStack"),
                        storedItemStackClass.getMethod("getActualStack"),
                        storedItemStackClass.getMethod("getQuantity"),
                        terminalClass.getMethod("pullStack", storedItemStackClass, long.class),
                        terminalClass.getMethod("pushStack", ItemStack.class),
                        storedItemStackClass.getConstructor(ItemStack.class),
                        craftingMenuClass
                );
            } catch (ReflectiveOperationException ignored) {
                return new ReflectionState(false, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
            }
        }

        private boolean matches(AbstractContainerMenu menu, String screenClassName) {
            if (!available || menu == null) {
                return false;
            }
            if (menuClass.isInstance(menu)) {
                return true;
            }
            return "com.tom.storagemod.screen.StorageTerminalScreen".equals(screenClassName)
                    || "com.tom.storagemod.screen.CraftingTerminalScreen".equals(screenClassName);
        }

        private boolean isCraftingTerminal(AbstractContainerMenu menu, String screenClassName) {
            if (!available) {
                return false;
            }
            if (craftingMenuClass != null && menu != null && craftingMenuClass.isInstance(menu)) {
                return true;
            }
            return "com.tom.storagemod.screen.CraftingTerminalScreen".equals(screenClassName);
        }

        @SuppressWarnings("unchecked")
        private ClientItemsView clientItems(AbstractContainerMenu menu) {
            if (!available || menu == null || !menuClass.isInstance(menu)) {
                return ClientItemsView.empty();
            }
            try {
                int syncSize = 0;
                int sortedSize = 0;
                int clientSize = 0;
                int rawSize = 0;
                boolean itemsLoaded = itemsLoadedField != null && itemsLoadedField.getBoolean(menu);
                Object sync = syncField.get(menu);
                if (sync != null) {
                    Object syncList = getSyncAsListMethod.invoke(sync);
                    if (syncList instanceof List<?> list) {
                        syncSize = list.size();
                        if (!list.isEmpty()) {
                            return new ClientItemsView((List<Object>) List.copyOf(list), "sync", syncSize, clientSize, sortedSize, rawSize, itemsLoaded);
                        }
                    }
                }
                Object sorted = itemListClientSortedField.get(menu);
                if (sorted instanceof List<?> list) {
                    sortedSize = list.size();
                    if (!list.isEmpty()) {
                        return new ClientItemsView((List<Object>) List.copyOf(list), "client_sorted", syncSize, clientSize, sortedSize, rawSize, itemsLoaded);
                    }
                }
                Object unsorted = itemListClientField.get(menu);
                if (unsorted instanceof List<?> list) {
                    clientSize = list.size();
                    if (!list.isEmpty()) {
                        return new ClientItemsView((List<Object>) List.copyOf(list), "client", syncSize, clientSize, sortedSize, rawSize, itemsLoaded);
                    }
                }
                Object raw = itemListField.get(menu);
                if (raw instanceof List<?> list) {
                    rawSize = list.size();
                    return new ClientItemsView((List<Object>) List.copyOf(list), "raw", syncSize, clientSize, sortedSize, rawSize, itemsLoaded);
                }
            } catch (ReflectiveOperationException ignored) {
            }
            return ClientItemsView.empty();
        }

        private ItemStack displayStack(Object storedStack) {
            if (!available || storedStack == null) {
                return ItemStack.EMPTY;
            }
            try {
                Object rawDisplay = getStackMethod.invoke(storedStack);
                return rawDisplay instanceof ItemStack stack ? stack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException ignored) {
                return ItemStack.EMPTY;
            }
        }

        private ItemStack actualStack(Object storedStack) {
            if (!available || storedStack == null) {
                return ItemStack.EMPTY;
            }
            try {
                Object actual = getActualStackMethod.invoke(storedStack);
                return actual instanceof ItemStack stack ? stack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException ignored) {
                return ItemStack.EMPTY;
            }
        }

        private long quantity(Object storedStack) {
            if (!available || storedStack == null) {
                return 0L;
            }
            try {
                Object quantity = getQuantityMethod.invoke(storedStack);
                return quantity instanceof Number number ? number.longValue() : 0L;
            } catch (ReflectiveOperationException ignored) {
                return 0L;
            }
        }

        private Object terminal(AbstractContainerMenu menu) {
            if (!available || menu == null || !menuClass.isInstance(menu)) {
                return null;
            }
            try {
                return terminalField.get(menu);
            } catch (IllegalAccessException ignored) {
                return null;
            }
        }

        private Object findMatchingStoredStack(Object terminal, dev.imagio.slot.inventory.core.ItemIdentity identity) {
            if (!available || terminal == null || identity == null) {
                return null;
            }
            try {
                Object rawStacks = getStacksMethod.invoke(terminal);
                if (!(rawStacks instanceof List<?> storedStacks)) {
                    return null;
                }
                for (Object storedStack : storedStacks) {
                    if (storedStack == null) {
                        continue;
                    }
                    ItemStack actualStack = actualStack(storedStack);
                    if (!actualStack.isEmpty() && ItemIdentityMatcher.matchesMovable(actualStack, identity)) {
                        return storedStack;
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
            return null;
        }

        private Object findStoredStackByEntryId(Object terminal, String entryId) {
            if (!available || terminal == null || entryId == null || entryId.isBlank()) {
                return null;
            }
            try {
                Object rawStacks = getStacksMethod.invoke(terminal);
                if (!(rawStacks instanceof List<?> storedStacks)) {
                    return null;
                }
                for (Object storedStack : storedStacks) {
                    if (storedStack == null) {
                        continue;
                    }
                    ItemStack actualStack = actualStack(storedStack);
                    if (!actualStack.isEmpty() && entryId.equals(providerEntryId(actualStack))) {
                        return storedStack;
                    }
                }
            } catch (ReflectiveOperationException ignored) {
            }
            return null;
        }

        private ItemStack pullStack(Object terminal, Object storedStack, long amount) {
            try {
                Object pulled = pullStackMethod.invoke(terminal, storedStack, amount);
                return pulled instanceof ItemStack stack ? stack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException ignored) {
                return ItemStack.EMPTY;
            }
        }

        private ItemStack pushStack(Object terminal, ItemStack stack) {
            try {
                Object remainder = pushStackMethod.invoke(terminal, stack);
                return remainder instanceof ItemStack itemStack ? itemStack : stack;
            } catch (ReflectiveOperationException ignored) {
                return stack;
            }
        }
    }

    private record ClientItemsView(
            List<Object> items,
            String source,
            int syncSize,
            int clientSize,
            int sortedSize,
            int rawSize,
            boolean itemsLoaded
    ) {
        private static ClientItemsView empty() {
            return new ClientItemsView(List.of(), "none", 0, 0, 0, 0, false);
        }
    }
}
