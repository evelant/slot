package dev.imagio.slot.compat.sophisticated;

import dev.imagio.slot.SlotDebugLog;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SophisticatedBackpackUpgradeSupport {
    private static final int CRAFTING_PANEL_HEIGHT = 70;
    private static final ReflectionState REFLECTION = ReflectionState.load();
    private static final Set<String> LOGGED_RESOLVED_MAPPINGS = ConcurrentHashMap.newKeySet();
    private static final Set<String> LOGGED_UNRESOLVED_MAPPINGS = ConcurrentHashMap.newKeySet();
    private static final Set<String> LOGGED_DETECTION_FAILURES = ConcurrentHashMap.newKeySet();

    private SophisticatedBackpackUpgradeSupport() {
    }

    public static boolean isAvailable() {
        return REFLECTION.available();
    }

    public static CraftingUpgradePanelRef findCraftingUpgrade(AbstractContainerMenu menu) {
        return REFLECTION.findCraftingUpgrade(menu);
    }

    public static List<CraftingUpgradePanelRef> findCraftingUpgrades(AbstractContainerMenu menu) {
        return REFLECTION.findCraftingUpgrades(menu);
    }

    public static boolean ensureCraftingUpgradeOpen(AbstractContainerMenu menu, CraftingUpgradePanelRef panelRef) {
        return REFLECTION.ensureCraftingUpgradeOpen(menu, panelRef);
    }

    public static boolean ensureCraftingUpgradeOpen(AbstractContainerMenu menu, int tabId) {
        return REFLECTION.ensureCraftingUpgradeOpen(menu, tabId);
    }

    public static boolean shouldRefillCraftingGrid(AbstractContainerMenu menu) {
        return REFLECTION.shouldRefillCraftingGrid(menu);
    }

    public static boolean setRefillCraftingGrid(AbstractContainerMenu menu, boolean enabled) {
        return REFLECTION.setRefillCraftingGrid(menu, enabled);
    }

    public static List<UpgradeTabRef> findNonCraftingUpgradeTabs(AbstractContainerMenu menu) {
        return REFLECTION.findNonCraftingUpgradeTabs(menu);
    }

    public record CraftingUpgradePanelRef(
            String toolId,
            Component title,
            int preferredHeight,
            List<Integer> inputSlots,
            int resultSlot,
            boolean supportsAutoRefillToggle,
            boolean liveToolPanel,
            int tabId
    ) {
        public boolean hasLiveToolPanel() {
            return liveToolPanel;
        }
    }

    public record UpgradeTabRef(
            String toolId,
            Component title,
            int tabId,
            boolean open,
            String containerClassName
    ) {
    }

    private record CraftingUpgradeContainerRef(
            Object container,
            int tabId
    ) {
    }

    private record ReflectionState(
            boolean available,
            Class<?> backpackContainerClass,
            Class<?> craftingUpgradeContainerClass,
            Class<?> slotItemHandlerClass,
            Method getOpenOrFirstCraftingContainerMethod,
            Method getUpgradeContainersMethod,
            Method getSlotUpgradeContainerMethod,
            Method setOpenTabIdMethod,
            Method getTotalSlotsNumberMethod,
            Method refreshAllSlotsMethod,
            Method getRecipeSlotsMethod,
            Method getSlotsMethod,
            Method shouldRefillCraftingGridMethod,
            Method setRefillCraftingGridMethod,
            Method isOpenMethod
    ) {
        private static ReflectionState load() {
            try {
                ClassLoader loader = SophisticatedBackpackUpgradeSupport.class.getClassLoader();
                Class<?> backpackContainerClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer", false, loader);
                Class<?> storageContainerMenuBaseClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase", false, loader);
                Class<?> upgradeContainerBaseClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase", false, loader);
                Class<?> craftingUpgradeContainerClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.upgrades.crafting.CraftingUpgradeContainer", false, loader);
                Class<?> slotItemHandlerClass = firstClass(
                        loader,
                        "net.neoforged.neoforge.items.SlotItemHandler",
                        "net.minecraftforge.items.SlotItemHandler"
                );
                Method refreshAllSlotsMethod = storageContainerMenuBaseClass.getDeclaredMethod("refreshAllSlots");
                refreshAllSlotsMethod.setAccessible(true);

                return new ReflectionState(
                        true,
                        backpackContainerClass,
                        craftingUpgradeContainerClass,
                        slotItemHandlerClass,
                        storageContainerMenuBaseClass.getMethod("getOpenOrFirstCraftingContainer", RecipeType.class),
                        storageContainerMenuBaseClass.getMethod("getUpgradeContainers"),
                        storageContainerMenuBaseClass.getMethod("getSlotUpgradeContainer", Slot.class),
                        storageContainerMenuBaseClass.getMethod("setOpenTabId", int.class),
                        storageContainerMenuBaseClass.getMethod("getTotalSlotsNumber"),
                        refreshAllSlotsMethod,
                        craftingUpgradeContainerClass.getMethod("getRecipeSlots"),
                        upgradeContainerBaseClass.getMethod("getSlots"),
                        craftingUpgradeContainerClass.getMethod("shouldRefillCraftingGrid"),
                        craftingUpgradeContainerClass.getMethod("setRefillCraftingGrid", boolean.class),
                        upgradeContainerBaseClass.getMethod("isOpen")
                );
            } catch (ReflectiveOperationException ignored) {
                return new ReflectionState(false, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
            }
        }

        private CraftingUpgradePanelRef findCraftingUpgrade(AbstractContainerMenu menu) {
            List<CraftingUpgradePanelRef> upgrades = findCraftingUpgrades(menu);
            return upgrades.isEmpty() ? null : upgrades.get(0);
        }

        private static Class<?> firstClass(ClassLoader loader, String... classNames) throws ClassNotFoundException {
            ClassNotFoundException last = null;
            for (String className : classNames) {
                try {
                    return Class.forName(className, false, loader);
                } catch (ClassNotFoundException exception) {
                    last = exception;
                }
            }
            throw last == null ? new ClassNotFoundException("missing class candidates") : last;
        }

        private List<CraftingUpgradePanelRef> findCraftingUpgrades(AbstractContainerMenu menu) {
            if (!available || menu == null || !backpackContainerClass.isInstance(menu)) {
                return List.of();
            }

            try {
                List<CraftingUpgradeContainerRef> craftingContainerRefs = resolveCraftingUpgradeContainers(menu);
                if (craftingContainerRefs.isEmpty()) {
                    logDetectionFailure(
                            "missing|" + menu.getClass().getName() + "|" + menu.containerId,
                            "Sophisticated Backpack crafting upgrade container was not found on SLOT menu inspection: menu={} totalMenuSlots={}",
                            menu.getClass().getName(),
                            menu.slots.size()
                    );
                    return List.of();
                }

                List<CraftingUpgradePanelRef> panelRefs = new ArrayList<>();
                for (CraftingUpgradeContainerRef craftingContainerRef : craftingContainerRefs) {
                    panelRefs.add(resolveCraftingPanelRef(menu, craftingContainerRef));
                }
                return List.copyOf(panelRefs);
            } catch (ReflectiveOperationException exception) {
                logDetectionFailure(
                        "reflection|" + menu.getClass().getName() + "|" + exception.getClass().getName() + "|" + String.valueOf(exception.getMessage()),
                        "Failed to inspect Sophisticated Backpack crafting upgrade for SLOT: menu={} error={} message={}",
                        menu.getClass().getName(),
                        exception.getClass().getName(),
                        String.valueOf(exception.getMessage())
                );
                return List.of();
            }
        }

        private boolean ensureCraftingUpgradeOpen(AbstractContainerMenu menu, CraftingUpgradePanelRef panelRef) {
            if (panelRef == null) {
                return true;
            }
            return ensureCraftingUpgradeOpen(menu, panelRef.tabId(), panelRef.hasLiveToolPanel());
        }

        private boolean ensureCraftingUpgradeOpen(AbstractContainerMenu menu, int tabId) {
            return ensureCraftingUpgradeOpen(menu, tabId, false);
        }

        private boolean ensureCraftingUpgradeOpen(AbstractContainerMenu menu, int tabId, boolean liveToolPanel) {
            if (!available || menu == null || tabId < 0 || !backpackContainerClass.isInstance(menu)) {
                return true;
            }

            try {
                CraftingUpgradeContainerRef containerRef = resolveCraftingUpgradeContainer(menu);
                boolean alreadyOpen = containerRef != null && isCraftingContainerOpen(containerRef.container());
                if (liveToolPanel && alreadyOpen) {
                    return true;
                }

                setOpenTabIdMethod.invoke(menu, tabId);
                refreshAllSlotsMethod.invoke(menu);
                SlotDebugLog.log(
                        "Requested Sophisticated Backpack crafting upgrade open: menu={} tabId={} livePanel={} alreadyOpen={}",
                        menu.getClass().getName(),
                        tabId,
                        liveToolPanel,
                        alreadyOpen
                );
                return true;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        private boolean shouldRefillCraftingGrid(AbstractContainerMenu menu) {
            if (!available || menu == null || !backpackContainerClass.isInstance(menu)) {
                return false;
            }

            try {
                CraftingUpgradeContainerRef craftingContainerRef = resolveCraftingUpgradeContainer(menu);
                if (craftingContainerRef == null) {
                    return false;
                }
                Object refill = shouldRefillCraftingGridMethod.invoke(craftingContainerRef.container());
                return refill instanceof Boolean bool && bool;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        private boolean setRefillCraftingGrid(AbstractContainerMenu menu, boolean enabled) {
            if (!available || menu == null || !backpackContainerClass.isInstance(menu)) {
                return false;
            }

            try {
                CraftingUpgradeContainerRef craftingContainerRef = resolveCraftingUpgradeContainer(menu);
                if (craftingContainerRef == null) {
                    return false;
                }
                setRefillCraftingGridMethod.invoke(craftingContainerRef.container(), enabled);
                return true;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        private List<UpgradeTabRef> findNonCraftingUpgradeTabs(AbstractContainerMenu menu) {
            if (!available || menu == null || !backpackContainerClass.isInstance(menu)) {
                return List.of();
            }

            try {
                Object containersValue = getUpgradeContainersMethod.invoke(menu);
                if (!(containersValue instanceof Map<?, ?> containers)) {
                    return List.of();
                }

                List<UpgradeTabRef> tabs = new ArrayList<>();
                for (Map.Entry<?, ?> entry : containers.entrySet()) {
                    Object container = entry.getValue();
                    if (container == null || craftingUpgradeContainerClass.isInstance(container)) {
                        continue;
                    }
                    int tabId = entry.getKey() instanceof Integer integer ? integer : -1;
                    boolean open = isCraftingContainerOpen(container);
                    String containerClassName = container.getClass().getName();
                    String simpleName = container.getClass().getSimpleName();
                    tabs.add(new UpgradeTabRef(
                            "sophisticatedbackpacks:upgrade#" + tabId + ":" + simpleName.toLowerCase(java.util.Locale.ROOT),
                            Component.literal(humanizeUpgradeName(simpleName)),
                            tabId,
                            open,
                            containerClassName
                    ));
                }
                return List.copyOf(tabs);
            } catch (ReflectiveOperationException ignored) {
                return List.of();
            }
        }

        private List<Integer> resolveRecipeSlots(AbstractContainerMenu menu, Object craftingContainer) throws ReflectiveOperationException {
            Object recipeSlotsValue = getRecipeSlotsMethod.invoke(craftingContainer);
            if (!(recipeSlotsValue instanceof List<?> recipeSlots)) {
                return List.of();
            }

            List<Integer> resolvedSlots = new ArrayList<>(recipeSlots.size());
            for (Object recipeSlot : recipeSlots) {
                int menuSlot = resolveMenuSlot(menu, craftingContainer, recipeSlot);
                if (menuSlot >= 0) {
                    resolvedSlots.add(menuSlot);
                }
            }
            return List.copyOf(resolvedSlots);
        }

        private int resolveResultSlot(AbstractContainerMenu menu, Object craftingContainer) throws ReflectiveOperationException {
            Object allSlotsValue = getSlotsMethod.invoke(craftingContainer);
            if (!(allSlotsValue instanceof List<?> allSlots) || allSlots.isEmpty()) {
                return -1;
            }
            return resolveMenuSlot(menu, craftingContainer, allSlots.get(allSlots.size() - 1));
        }

        private int resolveTabId(AbstractContainerMenu menu, Object craftingContainer) throws ReflectiveOperationException {
            Object containersValue = getUpgradeContainersMethod.invoke(menu);
            if (!(containersValue instanceof Map<?, ?> containers)) {
                return -1;
            }

            for (Map.Entry<?, ?> entry : containers.entrySet()) {
                if (entry.getValue() != craftingContainer) {
                    continue;
                }
                if (entry.getKey() instanceof Integer tabId) {
                    return tabId;
                }
            }

            return -1;
        }

        private CraftingUpgradeContainerRef resolveCraftingUpgradeContainer(AbstractContainerMenu menu) throws ReflectiveOperationException {
            Object openOrFirst = getOpenOrFirstCraftingContainerMethod.invoke(menu, RecipeType.CRAFTING);
            if (openOrFirst instanceof Optional<?> optional) {
                Object craftingContainer = optional.orElse(null);
                if (craftingContainer != null && craftingUpgradeContainerClass.isInstance(craftingContainer)) {
                    return new CraftingUpgradeContainerRef(craftingContainer, resolveTabId(menu, craftingContainer));
                }
            }

            Object containersValue = getUpgradeContainersMethod.invoke(menu);
            if (containersValue instanceof Map<?, ?> containers) {
                for (Map.Entry<?, ?> entry : containers.entrySet()) {
                    Object container = entry.getValue();
                    if (!craftingUpgradeContainerClass.isInstance(container)) {
                        continue;
                    }
                    int tabId = entry.getKey() instanceof Integer integer ? integer : -1;
                    return new CraftingUpgradeContainerRef(container, tabId);
                }
            }
            return null;
        }

        private List<CraftingUpgradeContainerRef> resolveCraftingUpgradeContainers(AbstractContainerMenu menu) throws ReflectiveOperationException {
            LinkedHashMap<String, CraftingUpgradeContainerRef> resolved = new LinkedHashMap<>();
            CraftingUpgradeContainerRef openOrFirst = resolveCraftingUpgradeContainer(menu);
            if (openOrFirst != null) {
                resolved.put(uniqueContainerKey(openOrFirst), openOrFirst);
            }

            Object containersValue = getUpgradeContainersMethod.invoke(menu);
            if (containersValue instanceof Map<?, ?> containers) {
                for (Map.Entry<?, ?> entry : containers.entrySet()) {
                    Object container = entry.getValue();
                    if (!craftingUpgradeContainerClass.isInstance(container)) {
                        continue;
                    }
                    int tabId = entry.getKey() instanceof Integer integer ? integer : -1;
                    CraftingUpgradeContainerRef ref = new CraftingUpgradeContainerRef(container, tabId);
                    resolved.putIfAbsent(uniqueContainerKey(ref), ref);
                }
            }
            return List.copyOf(resolved.values());
        }

        private String uniqueContainerKey(CraftingUpgradeContainerRef ref) {
            return ref.container().getClass().getName() + "@" + System.identityHashCode(ref.container()) + "#" + ref.tabId();
        }

        private String humanizeUpgradeName(String simpleName) {
            if (simpleName == null || simpleName.isBlank()) {
                return "Upgrade";
            }
            String normalized = simpleName.replace("Container", "").replace("Upgrade", " Upgrade");
            StringBuilder title = new StringBuilder(normalized.length() + 8);
            for (int i = 0; i < normalized.length(); i++) {
                char current = normalized.charAt(i);
                if (i > 0 && Character.isUpperCase(current) && Character.isLowerCase(normalized.charAt(i - 1))) {
                    title.append(' ');
                }
                title.append(current);
            }
            return title.toString().trim();
        }

        private CraftingUpgradePanelRef resolveCraftingPanelRef(
                AbstractContainerMenu menu,
                CraftingUpgradeContainerRef craftingContainerRef
        ) throws ReflectiveOperationException {
            Object craftingContainer = craftingContainerRef.container();
            List<Integer> inputSlots = resolveRecipeSlots(menu, craftingContainer);
            int resultSlot = resolveResultSlot(menu, craftingContainer);
            int tabId = craftingContainerRef.tabId();
            boolean open = isCraftingContainerOpen(craftingContainer);
            if (inputSlots.size() != 9 || resultSlot < 0) {
                logUnresolvedMapping(menu, inputSlots.size(), resultSlot, tabId, open);
                return new CraftingUpgradePanelRef(
                        "sophisticatedbackpacks:crafting_upgrade#" + tabId,
                        Component.translatable("slot.screen.container.tool_panel.crafting"),
                        CRAFTING_PANEL_HEIGHT,
                        List.of(),
                        -1,
                        false,
                        false,
                        tabId
                );
            }

            logResolvedMapping(menu, inputSlots, resultSlot, tabId, open);
            return new CraftingUpgradePanelRef(
                    "sophisticatedbackpacks:crafting_upgrade#" + tabId,
                    Component.translatable("slot.screen.container.tool_panel.crafting"),
                    CRAFTING_PANEL_HEIGHT,
                    List.copyOf(inputSlots),
                    resultSlot,
                    true,
                    true,
                    tabId
            );
        }

        private int resolveMenuSlot(AbstractContainerMenu menu, Object craftingContainer, Object slotValue) {
            if (!(slotValue instanceof Slot slot)) {
                return -1;
            }

            if (slot.index >= 0) {
                Slot indexedCandidate = safeMenuSlot(menu, slot.index);
                if (indexedCandidate != null && matchesMenuSlot(menu, craftingContainer, indexedCandidate, slot)) {
                    return slot.index;
                }
            }

            int directIndex = menu.slots.indexOf(slot);
            if (directIndex >= 0) {
                return directIndex;
            }

            int totalSlots = totalSlotCount(menu);
            for (int menuSlot = 0; menuSlot < totalSlots; menuSlot++) {
                Slot candidate = safeMenuSlot(menu, menuSlot);
                if (candidate != null && matchesMenuSlot(menu, craftingContainer, candidate, slot)) {
                    return menuSlot;
                }
            }

            return -1;
        }

        private boolean matchesMenuSlot(AbstractContainerMenu menu, Object craftingContainer, Slot candidate, Slot target) {
            if (candidate == target) {
                return true;
            }

            if (candidate.getContainerSlot() != target.getContainerSlot()) {
                return false;
            }

            if (belongsToUpgradeContainer(menu, craftingContainer, candidate)) {
                return true;
            }

            if (slotItemHandlerClass != null && slotItemHandlerClass.isInstance(target)) {
                return false;
            }

            return candidate.container == target.container;
        }

        private boolean belongsToUpgradeContainer(AbstractContainerMenu menu, Object craftingContainer, Slot candidate) {
            if (getSlotUpgradeContainerMethod == null) {
                return false;
            }

            try {
                Object candidateOwner = unwrapOptional(getSlotUpgradeContainerMethod.invoke(menu, candidate));
                return candidateOwner != null && candidateOwner == craftingContainer;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        private static Object unwrapOptional(Object value) {
            if (value instanceof Optional<?> optional) {
                return optional.orElse(null);
            }
            return null;
        }

        private int totalSlotCount(AbstractContainerMenu menu) {
            try {
                Object total = getTotalSlotsNumberMethod.invoke(menu);
                if (total instanceof Integer count) {
                    return count;
                }
            } catch (ReflectiveOperationException ignored) {
            }
            return menu.slots.size();
        }

        private boolean isCraftingContainerOpen(Object craftingContainer) {
            if (craftingContainer == null || isOpenMethod == null) {
                return false;
            }
            try {
                Object open = isOpenMethod.invoke(craftingContainer);
                return open instanceof Boolean bool && bool;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        private static Slot safeMenuSlot(AbstractContainerMenu menu, int slotId) {
            if (menu == null || slotId < 0) {
                return null;
            }
            try {
                Slot slot = menu.getSlot(slotId);
                return slot;
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        private static void logResolvedMapping(AbstractContainerMenu menu, List<Integer> inputSlots, int resultSlot, int tabId, boolean open) {
            String mappingKey = menu.getClass().getName()
                    + "|" + inputSlots
                    + "|" + resultSlot
                    + "|" + tabId
                    + "|" + open;
            if (!LOGGED_RESOLVED_MAPPINGS.add(mappingKey)) {
                return;
            }

            SlotDebugLog.log(
                    "Resolved Sophisticated Backpack crafting upgrade mapping: menu={} logicalTotalSlots={} physicalSlots={} inputSlots={} resultSlot={} tabId={} open={}",
                    menu.getClass().getName(),
                    REFLECTION.totalSlotCount(menu),
                    menu.slots.size(),
                    inputSlots,
                    resultSlot,
                    tabId,
                    open
            );
        }

        private static void logUnresolvedMapping(AbstractContainerMenu menu, int inputSlotCount, int resultSlot, int tabId, boolean open) {
            String mappingKey = menu.getClass().getName()
                    + "|" + menu.getClass().getName()
                    + "|" + inputSlotCount
                    + "|" + resultSlot
                    + "|" + tabId;
            if (!LOGGED_UNRESOLVED_MAPPINGS.add(mappingKey)) {
                return;
            }

            SlotDebugLog.log(
                    "Sophisticated Backpack crafting upgrade detected without live SLOT panel mapping: menu={} totalMenuSlots={} inputSlotMatches={} resultSlot={} tabId={}",
                    menu.getClass().getName(),
                    menu.slots.size(),
                    inputSlotCount,
                    resultSlot,
                    tabId
            );
        }

        private static void logDetectionFailure(String key, String pattern, Object... args) {
            if (!LOGGED_DETECTION_FAILURES.add(key)) {
                return;
            }
            SlotDebugLog.log(pattern, args);
        }
    }
}
