package dev.imagio.slot.storage.provider;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.registry.ProviderResult;
import dev.imagio.slot.session.HostTopologyDescriptor;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.InventoryPaneMembership;
import dev.imagio.slot.session.InventorySourceActionRoute;
import dev.imagio.slot.session.InventorySourceBackingKind;
import dev.imagio.slot.session.InventorySourceCapability;
import dev.imagio.slot.session.InventorySourceDescriptor;
import dev.imagio.slot.session.InventorySourceDomain;
import dev.imagio.slot.session.InventorySourceRole;
import dev.imagio.slot.session.InventoryToolDescriptor;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class TomsStorageTerminalProvider implements StorageViewProvider {
    private static final String PROVIDER_ID = "toms_storage_terminal";
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
    public ProviderResult<StorageViewProviderSession> createSession(StorageViewProviderContext context) {
        if (!REFLECTION.available()) {
            return ProviderResult.unsupported(
                    providerId(),
                    "reflection_unavailable",
                    "Tom's Storage classes are not available"
            );
        }
        if (!REFLECTION.matches(context.menu(), context.screenClassName())) {
            return ProviderResult.unsupported(
                    providerId(),
                    "unsupported_menu",
                    "Menu is not a Tom's Storage terminal"
            );
        }

        boolean craftingTerminal = REFLECTION.isCraftingTerminal(context.menu(), context.screenClassName());
        String label = context.openContainerTitle() == null || context.openContainerTitle().getString().isBlank()
                ? Component.translatable("menu.toms_storage.storage_terminal").getString()
                : context.openContainerTitle().getString();
        InventorySourceDescriptor primarySource = InventorySourceDescriptor.builder("open_container")
                .label(Component.literal(label))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .slotCount(0)
                .backingKind(InventorySourceBackingKind.PROVIDER_BACKED)
                .capabilities(java.util.Set.of(InventorySourceCapability.INSERT, InventorySourceCapability.EXTRACT))
                .actionRoute(InventorySourceActionRoute.PROVIDER_MUTATION)
                .paneMembership(InventoryPaneMembership.EXTERNAL)
                .stableOrder(0)
                .build();
        return ProviderResult.supported(new Session(
                List.of(primarySource),
                HostTopologyDescriptor.empty(),
                craftingTerminal
                        ? List.of(InventoryToolDescriptor.fromLegacy(
                                providerId(),
                                ExternalToolSpec.craftingGrid(
                                        "toms_storage:crafting_terminal",
                                        Component.translatable("slot.screen.container.tool_panel.crafting"),
                                        70,
                                        List.of(1, 2, 3, 4, 5, 6, 7, 8, 9),
                                        0
                                ),
                                true,
                                null,
                                Map.of(),
                                Map.of()
                        ))
                        : List.of()
        ));
    }

    private record Session(
            List<InventorySourceDescriptor> hostSources,
            HostTopologyDescriptor topology,
            List<InventoryToolDescriptor> tools
    ) implements StorageViewProviderSession {
        @Override
        public InventorySourceDescriptor primaryStorageSource() {
            return hostSources.isEmpty() ? null : hostSources.get(0);
        }

        @Override
        public List<Integer> primaryMenuSlots() {
            return List.of();
        }

        @Override
        public String providerId() {
            return PROVIDER_ID;
        }

        @Override
        public List<ExternalStorageStackSnapshot> readClientPrimarySnapshots(AbstractContainerMenu menu) {
            InventorySourceDescriptor primary = primaryStorageSource();
            if (primary == null || menu == null) {
                return List.of();
            }
            return readSnapshots(InventoryHostDescriptor.compatibilityHost(menu, this), primary.id()).stream()
                    .map(snapshot -> new ExternalStorageStackSnapshot(snapshot.handle(), snapshot.stack(), snapshot.count()))
                    .toList();
        }

        @Override
        public List<InventoryStackSnapshot> readSnapshots(InventoryHostDescriptor host, String sourceId) {
            InventorySourceDescriptor primary = primaryStorageSource();
            AbstractContainerMenu menu = host == null ? null : host.menu();
            if (primary == null || menu == null || !primary.id().equals(sourceId)) {
                return List.of();
            }
            ClientItemsView clientItemsView = REFLECTION.clientItems(menu);
            List<InventoryStackSnapshot> snapshots = new ArrayList<>();
            for (Object storedStack : clientItemsView.items()) {
                ItemStack rawDisplay = REFLECTION.displayStack(storedStack);
                if (rawDisplay.isEmpty()) {
                    continue;
                }

                int quantity = clampToInt(REFLECTION.quantity(storedStack));
                ItemStack displayStack = rawDisplay.copy();
                displayStack.setCount(Math.min(Math.max(1, quantity), displayStack.getMaxStackSize()));
                snapshots.add(new InventoryStackSnapshot(snapshots.size(), displayStack, quantity));
            }
            logClientState(menu, clientItemsView, snapshots.size());
            return List.copyOf(snapshots);
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

        @Override
        public MutationResult applyMutation(InventoryHostDescriptor host, InventoryMutation mutation) {
            InventorySourceDescriptor primary = primaryStorageSource();
            AbstractContainerMenu menu = host == null ? null : host.menu();
            ServerPlayer player = mutation == null ? null : mutation.player();
            if (primary == null
                    || mutation == null
                    || menu == null
                    || !primary.id().equals(mutation.sourceId())) {
                return MutationResult.blocked("unsupported_source", mutation == null ? ItemStack.EMPTY : mutation.stack());
            }

            return switch (mutation.kind()) {
                case EXTRACT -> MutationResult.success(extract(menu, mutation.identity(), mutation.transferMode()));
                case INSERT -> MutationResult.success(insert(menu, mutation.stack()));
                case ACTIVATE_TARGET, UNSPECIFIED -> MutationResult.blocked("unsupported_mutation", mutation.stack());
            };
        }

        @Override
        public ItemStack extractFromPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemIdentity identity, StorageTransferMode mode) {
            return extract(menu, identity, mode);
        }

        @Override
        public ItemStack insertIntoPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemStack stack) {
            return insert(menu, stack);
        }

        private ItemStack extract(AbstractContainerMenu menu, ItemIdentity identity, StorageTransferMode mode) {
            Object terminal = REFLECTION.terminal(menu);
            Object storedStack = REFLECTION.findMatchingStoredStack(terminal, identity);
            if (terminal == null || storedStack == null) {
                return ItemStack.EMPTY;
            }

            long amount = switch (mode) {
                case ONE -> 1L;
                case STACK, ALL -> Math.max(1, REFLECTION.actualStack(storedStack).getMaxStackSize());
            };
            return REFLECTION.pullStack(terminal, storedStack, amount);
        }

        private ItemStack insert(AbstractContainerMenu menu, ItemStack stack) {
            Object terminal = REFLECTION.terminal(menu);
            return terminal == null ? stack : REFLECTION.pushStack(terminal, stack);
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
                ClassLoader loader = TomsStorageTerminalProvider.class.getClassLoader();
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
            return "com.tom.storagemod.screen.StorageTerminalScreen".equals(screenClassName);
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

        @SuppressWarnings("unchecked")
        private Object findMatchingStoredStack(Object terminal, ItemIdentity identity) {
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
                    if (!actualStack.isEmpty() && ItemBehaviorPolicy.matchesMovableIdentity(actualStack, identity)) {
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
