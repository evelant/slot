package dev.imagio.slot.compat.sophisticated;

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
import dev.imagio.slot.session.ToolOpenCommand;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.adapter.ExternalToolToggleId;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import dev.imagio.slot.storage.provider.InventoryMutation;
import dev.imagio.slot.storage.provider.InventoryStackSnapshot;
import dev.imagio.slot.storage.provider.MenuBackedStorageViewProvider;
import dev.imagio.slot.storage.provider.MutationResult;
import dev.imagio.slot.storage.provider.StorageViewProvider;
import dev.imagio.slot.storage.provider.StorageViewProviderContext;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import dev.imagio.slot.storage.provider.ToolActionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SophisticatedBackpackStorageViewProvider implements StorageViewProvider {
    private static final String PROVIDER_ID = "sophisticatedbackpacks";

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public int priority() {
        return 200;
    }

    @Override
    public ProviderResult<StorageViewProviderSession> createSession(StorageViewProviderContext context) {
        if (!isBackpackStorage(context.menu(), context.screenClassName())) {
            return ProviderResult.unsupported(
                    providerId(),
                    "unsupported_menu",
                    "Menu is not a Sophisticated Backpack"
            );
        }

        int containerSlotCount = MenuBackedStorageViewProvider.inferSupportedModdedStorageSlots(
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

        String label = context.openContainerTitle() == null || context.openContainerTitle().getString().isBlank()
                ? Component.translatable("slot.source.open_container").getString()
                : context.openContainerTitle().getString();
        InventorySourceDescriptor primarySource = InventorySourceDescriptor.builder("carried_storage")
                .label(Component.literal(label))
                .domain(InventorySourceDomain.HOST_STORAGE)
                .role(InventorySourceRole.PRIMARY_STORAGE)
                .groupId("primary")
                .slotCount(containerSlotCount)
                .backingKind(InventorySourceBackingKind.MENU_BACKED)
                .capabilities(Set.of(InventorySourceCapability.INSERT, InventorySourceCapability.EXTRACT))
                .actionRoute(InventorySourceActionRoute.MENU_MUTATION)
                .paneMembership(InventoryPaneMembership.CARRIED)
                .stableOrder(5)
                .build();

        List<Integer> primaryMenuSlots = MenuBackedStorageViewProvider.slotRange(
                0,
                Math.min(containerSlotCount, context.menu().slots.size()) - 1
        );
        return ProviderResult.supported(new Session(
                List.of(primarySource),
                new HostTopologyDescriptor(
                        Map.of(primarySource.id(), primaryMenuSlots),
                        sourceIdsByMenuSlot(primarySource.id(), primaryMenuSlots),
                        Map.of()
                ),
                resolveToolDescriptors(context.menu())
        ));
    }

    private static boolean isBackpackStorage(AbstractContainerMenu menu, String screenClassName) {
        String resolvedScreenClassName = screenClassName == null ? "" : screenClassName;
        return MenuBackedStorageViewProvider.classChainContains(
                menu.getClass(),
                "net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer"
        ) || "net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen".equals(resolvedScreenClassName);
    }

    private static LinkedHashMap<Integer, String> sourceIdsByMenuSlot(String sourceId, List<Integer> menuSlots) {
        LinkedHashMap<Integer, String> sourceIdsByMenuSlot = new LinkedHashMap<>();
        for (int menuSlot : menuSlots) {
            sourceIdsByMenuSlot.put(menuSlot, sourceId);
        }
        return sourceIdsByMenuSlot;
    }

    private static List<InventoryToolDescriptor> resolveToolDescriptors(AbstractContainerMenu menu) {
        List<InventoryToolDescriptor> descriptors = new ArrayList<>();
        for (SophisticatedBackpackUpgradeSupport.CraftingUpgradePanelRef panelRef : SophisticatedBackpackUpgradeSupport.findCraftingUpgrades(menu)) {
            if (panelRef == null) {
                continue;
            }
            dev.imagio.slot.storage.adapter.ExternalToolSpec toolSpec = panelRef.toolSpec();
            if (toolSpec == null) {
                continue;
            }
            String toolId = toolSpec.id().isBlank()
                    ? "sophisticatedbackpacks:crafting_upgrade#" + panelRef.tabId()
                    : toolSpec.id();
            descriptors.add(InventoryToolDescriptor.fromLegacy(
                    PROVIDER_ID,
                    toolSpec,
                    panelRef.hasLiveToolPanel(),
                    panelRef.tabId() >= 0 ? new ToolOpenCommand(PROVIDER_ID, toolId, panelRef.tabId()) : null,
                    Map.of(dev.imagio.slot.storage.adapter.ExternalToolToggleId.AUTO_REFILL, SophisticatedBackpackUpgradeSupport.shouldRefillCraftingGrid(menu)),
                    Map.of()
            ));
        }
        return List.copyOf(descriptors);
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
                case EXTRACT -> MutationResult.success(extract(menu, player, mutation.identity(), mutation.transferMode()));
                case INSERT -> MutationResult.success(insert(menu, mutation.stack()));
                case ACTIVATE_TARGET, UNSPECIFIED -> MutationResult.blocked("unsupported_mutation", mutation.stack());
            };
        }

        @Override
        public ItemStack extractFromPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemIdentity identity, StorageTransferMode mode) {
            return extract(menu, player, identity, mode);
        }

        @Override
        public ItemStack insertIntoPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemStack stack) {
            return insert(menu, stack);
        }

        private ItemStack extract(AbstractContainerMenu menu, ServerPlayer player, ItemIdentity identity, StorageTransferMode mode) {
            if (identity == null || menu == null || player == null) {
                return ItemStack.EMPTY;
            }

            for (int menuSlot : topology.menuSlotsForSource(primaryStorageSource().id())) {
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

        private ItemStack insert(AbstractContainerMenu menu, ItemStack stack) {
            if (menu == null || stack == null || stack.isEmpty()) {
                return stack;
            }

            ItemStack remainder = stack;
            for (int menuSlot : topology.menuSlotsForSource(primaryStorageSource().id())) {
                if (menuSlot < 0 || menuSlot >= menu.slots.size() || remainder.isEmpty()) {
                    continue;
                }
                remainder = menu.getSlot(menuSlot).safeInsert(remainder);
            }
            return remainder;
        }

        @Override
        public ToolActionResult activateTool(InventoryHostDescriptor host, String toolId) {
            AbstractContainerMenu menu = host == null ? null : host.menu();
            if (toolId == null || toolId.isBlank()) {
                return ToolActionResult.blocked("missing_tool_id");
            }
            boolean activated = tools.stream()
                    .filter(tool -> tool.matchesToolId(toolId))
                    .map(InventoryToolDescriptor::activationCommand)
                    .filter(command -> command != null && command.present())
                    .findFirst()
                    .map(command -> SophisticatedBackpackUpgradeSupport.ensureCraftingUpgradeOpen(menu, command.tabId()))
                    .orElse(false);
            return activated ? ToolActionResult.success() : ToolActionResult.blocked("tool_activation_failed");
        }

        @Override
        public ToolActionResult setToolToggle(
                InventoryHostDescriptor host,
                String toolId,
                ExternalToolToggleId toggleId,
                boolean enabled
        ) {
            AbstractContainerMenu menu = host == null ? null : host.menu();
            if (toggleId != ExternalToolToggleId.AUTO_REFILL || toolId == null || toolId.isBlank()) {
                return ToolActionResult.blocked("unsupported_tool_toggle");
            }
            boolean matchesTool = tools.stream().anyMatch(tool -> tool.matchesToolId(toolId));
            boolean applied = matchesTool && SophisticatedBackpackUpgradeSupport.setRefillCraftingGrid(menu, enabled);
            return applied ? ToolActionResult.success() : ToolActionResult.blocked("tool_toggle_failed");
        }
    }
}
