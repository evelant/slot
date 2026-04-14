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
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class MenuBackedStorageViewProvider implements StorageViewProvider {
    private static final String PROVIDER_ID = "menu_backed";

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public ProviderResult<StorageViewProviderSession> createSession(StorageViewProviderContext context) {
        AbstractContainerMenu menu = context.menu();
        Inventory playerInventory = context.playerInventory();

        int containerSlotCount;
        if (menu instanceof ChestMenu chestMenu) {
            containerSlotCount = chestMenu.getRowCount() * 9;
        } else if (menu instanceof ShulkerBoxMenu) {
            containerSlotCount = 27;
        } else {
            containerSlotCount = inferSupportedModdedStorageSlots(menu, playerInventory, context.screenClassName());
            if (containerSlotCount <= 0) {
                return ProviderResult.unsupported(
                        providerId(),
                        "unsupported_menu",
                        "Menu does not expose supported menu-backed storage slots"
                );
            }
        }

        String label = context.openContainerTitle() == null || context.openContainerTitle().getString().isBlank()
                ? Component.translatable("slot.source.open_container").getString()
                : context.openContainerTitle().getString();
        List<Integer> primaryMenuSlots = slotRange(0, Math.min(containerSlotCount, menu.slots.size()) - 1);
        InventorySourceDescriptor primarySource = InventorySourceDescriptor.builder("open_container")
                .label(Component.literal(label))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .groupId("primary")
                .slotCount(containerSlotCount)
                .backingKind(InventorySourceBackingKind.MENU_BACKED)
                .capabilities(Set.of(InventorySourceCapability.INSERT, InventorySourceCapability.EXTRACT))
                .actionRoute(InventorySourceActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.EXTERNAL)
                .stableOrder(0)
                .build();
        return ProviderResult.supported(new Session(
                List.of(primarySource),
                new HostTopologyDescriptor(
                        Map.of(primarySource.id(), primaryMenuSlots),
                        sourceIdsByMenuSlot(primarySource.id(), primaryMenuSlots),
                        Map.of()
                )
        ));
    }

    public static int inferSupportedModdedStorageSlots(AbstractContainerMenu menu, Inventory playerInventory, String screenClassName) {
        Integer reflectedStorageSlots = reflectedStorageSlotCount(menu);
        if (reflectedStorageSlots != null) {
            if (reflectedStorageSlots > 0 && menu.slots.size() >= reflectedStorageSlots) {
                SlotDebugLog.log(
                        "Accepted menu-backed provider by reflected slot count: screen={} menu={} reflectedStorageSlots={} totalMenuSlots={}",
                        screenClassName,
                        menu.getClass().getName(),
                        reflectedStorageSlots,
                        menu.slots.size()
                );
                return reflectedStorageSlots;
            }
        }

        if (!isSupportedModdedStorage(menu, screenClassName)) {
            return -1;
        }

        int totalSlots = menu.slots.size();
        if (totalSlots <= 36) {
            return -1;
        }

        int trailingPlayerSlots = 0;
        for (int slotIndex = totalSlots - 1; slotIndex >= 0; slotIndex--) {
            if (menu.getSlot(slotIndex).container == playerInventory) {
                trailingPlayerSlots++;
            } else {
                break;
            }
        }

        if (trailingPlayerSlots != 36) {
            return -1;
        }

        int containerSlots = totalSlots - trailingPlayerSlots;
        return containerSlots > 0 ? containerSlots : -1;
    }

    public static boolean isSupportedModdedStorage(AbstractContainerMenu menu, String screenClassName) {
        String resolvedScreenClassName = screenClassName == null ? "" : screenClassName;
        return classChainContains(menu.getClass(), "net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase")
                || "net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen".equals(resolvedScreenClassName)
                || "net.p3pp3rf1y.sophisticatedstorage.client.gui.LimitedBarrelScreen".equals(resolvedScreenClassName);
    }

    public static boolean classChainContains(Class<?> type, String className) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (className.equals(current.getName())) {
                return true;
            }
        }
        return false;
    }

    public static Integer reflectedStorageSlotCount(AbstractContainerMenu menu) {
        try {
            Method method = menu.getClass().getMethod("getNumberOfStorageInventorySlots");
            Object value = method.invoke(menu);
            if (value instanceof Integer storageSlots && storageSlots > 0) {
                return storageSlots;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return null;
    }

    public static List<Integer> slotRange(int startInclusive, int endInclusive) {
        if (endInclusive < startInclusive) {
            return List.of();
        }
        List<Integer> slots = new ArrayList<>(endInclusive - startInclusive + 1);
        for (int slot = startInclusive; slot <= endInclusive; slot++) {
            slots.add(slot);
        }
        return List.copyOf(slots);
    }

    private static LinkedHashMap<Integer, String> sourceIdsByMenuSlot(String sourceId, List<Integer> menuSlots) {
        LinkedHashMap<Integer, String> sourceIdsByMenuSlot = new LinkedHashMap<>();
        for (int menuSlot : menuSlots) {
            sourceIdsByMenuSlot.put(menuSlot, sourceId);
        }
        return sourceIdsByMenuSlot;
    }

    private record Session(
            List<InventorySourceDescriptor> hostSources,
            HostTopologyDescriptor topology
    ) implements StorageViewProviderSession {
        @Override
        public InventorySourceDescriptor primaryStorageSource() {
            return hostSources.isEmpty() ? null : hostSources.get(0);
        }

        @Override
        public List<Integer> primaryMenuSlots() {
            InventorySourceDescriptor primary = primaryStorageSource();
            return primary == null ? List.of() : topology.menuSlotsForSource(primary.id());
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

            List<InventoryStackSnapshot> snapshots = new ArrayList<>();
            for (int menuSlot : topology.menuSlotsForSource(primary.id())) {
                if (menuSlot < 0 || menuSlot >= menu.slots.size()) {
                    continue;
                }
                ItemStack stack = menu.getSlot(menuSlot).getItem();
                if (stack.isEmpty()) {
                    continue;
                }
                snapshots.add(new InventoryStackSnapshot(menuSlot, stack.copy(), stack.getCount()));
            }
            return List.copyOf(snapshots);
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
                case EXTRACT -> MutationResult.success(extract(menu, player, mutation.identity(), mutation.transferMode(), primary.id()));
                case INSERT -> MutationResult.success(insert(menu, mutation.stack(), primary.id()));
                case ACTIVATE_TARGET, UNSPECIFIED -> MutationResult.blocked("unsupported_mutation", mutation.stack());
            };
        }

        @Override
        public ItemStack extractFromPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemIdentity identity, StorageTransferMode mode) {
            InventorySourceDescriptor primary = primaryStorageSource();
            return primary == null ? ItemStack.EMPTY : extract(menu, player, identity, mode, primary.id());
        }

        @Override
        public ItemStack insertIntoPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemStack stack) {
            InventorySourceDescriptor primary = primaryStorageSource();
            return primary == null ? stack : insert(menu, stack, primary.id());
        }

        private ItemStack extract(
                AbstractContainerMenu menu,
                ServerPlayer player,
                ItemIdentity identity,
                StorageTransferMode mode,
                String sourceId
        ) {
            if (identity == null || menu == null || player == null) {
                return ItemStack.EMPTY;
            }

            for (int menuSlot : topology.menuSlotsForSource(sourceId)) {
                if (menuSlot < 0 || menuSlot >= menu.slots.size()) {
                    continue;
                }

                Slot slot = menu.getSlot(menuSlot);
                ItemStack stack = slot.getItem();
                if (stack.isEmpty() || !slot.mayPickup(player) || !ItemBehaviorPolicy.matchesMovableIdentity(stack, identity)) {
                    continue;
                }

                int amount = switch (mode) {
                    case ONE -> 1;
                    case STACK, ALL -> stack.getCount();
                };
                return slot.safeTake(amount, stack.getCount(), player);
            }
            return ItemStack.EMPTY;
        }

        private ItemStack insert(AbstractContainerMenu menu, ItemStack stack, String sourceId) {
            if (menu == null || stack == null || stack.isEmpty()) {
                return stack;
            }

            ItemStack remainder = stack;
            for (int menuSlot : topology.menuSlotsForSource(sourceId)) {
                if (menuSlot < 0 || menuSlot >= menu.slots.size() || remainder.isEmpty()) {
                    continue;
                }
                remainder = menu.getSlot(menuSlot).safeInsert(remainder);
            }
            return remainder;
        }
    }
}
