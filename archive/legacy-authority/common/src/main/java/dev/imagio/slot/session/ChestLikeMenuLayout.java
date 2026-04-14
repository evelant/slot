package dev.imagio.slot.session;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.source.BasicInventorySource;
import dev.imagio.slot.client.source.InventorySource;
import dev.imagio.slot.client.source.SourceGroup;
import dev.imagio.slot.registry.ProviderResult;
import dev.imagio.slot.storage.provider.StorageViewProviderContext;
import dev.imagio.slot.storage.provider.StorageViewProviderRegistry;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import dev.imagio.slot.projection.InventoryPane;

public record ChestLikeMenuLayout(
        int containerSlotCount,
        List<InventorySource> sources,
        String openContainerLabel,
        Map<String, List<Integer>> sourceMenuSlots,
        Map<Integer, String> sourceIdsByMenuSlot,
        boolean primaryStorageIsCarried,
        StorageViewProviderSession primaryStorageSession
) {
    public static final String SOURCE_OPEN_CONTAINER = "open_container";
    public static final String SOURCE_CARRIED_STORAGE = "carried_storage";
    public static final String SOURCE_PLAYER_BACKPACK = "player_backpack";
    public static final String SOURCE_PLAYER_MAIN = "player_main";
    public static final String SOURCE_PLAYER_HOTBAR = "player_hotbar";
    public static final String SOURCE_PLAYER_ARMOR = "player_armor";
    public static final String SOURCE_PLAYER_OFFHAND = "player_offhand";

    public static final Set<String> OPEN_CONTAINER_SOURCES = Set.of(SOURCE_OPEN_CONTAINER);
    public static final Set<String> BASE_CARRIED_SOURCES = Set.of(
            SOURCE_PLAYER_MAIN,
            SOURCE_PLAYER_HOTBAR,
            SOURCE_PLAYER_BACKPACK,
            SOURCE_PLAYER_ARMOR,
            SOURCE_PLAYER_OFFHAND
    );

    public ChestLikeMenuLayout {
        if (containerSlotCount < 0) {
            throw new IllegalArgumentException("containerSlotCount must not be negative");
        }
        sources = List.copyOf(sources);
        openContainerLabel = openContainerLabel == null || openContainerLabel.isBlank()
                ? Component.translatable("slot.source.open_container").getString()
                : openContainerLabel;
        sourceMenuSlots = sourceMenuSlots == null ? Map.of() : copySourceSlots(sourceMenuSlots);
        sourceIdsByMenuSlot = sourceIdsByMenuSlot == null ? Map.of() : Map.copyOf(sourceIdsByMenuSlot);
        if (primaryStorageSession == null) {
            throw new IllegalArgumentException("primaryStorageSession must not be null");
        }
    }

    public static ChestLikeMenuLayout resolve(StorageViewProviderContext context) {
        AbstractContainerMenu menu = context == null ? null : context.menu();
        Inventory playerInventory = context == null ? null : context.playerInventory();
        Component openContainerTitle = context == null ? Component.empty() : context.openContainerTitle();
        String screenClassName = context == null ? "" : context.screenClassName();
        ProviderResult<StorageViewProviderSession> resolution = StorageViewProviderRegistry.resolve(context);
        if (resolution.status() != ProviderResult.Status.SUPPORTED || resolution.value() == null) {
            SlotDebugLog.log(
                    "Rejected unsupported storage menu after adapter resolution: screen={} menu={} totalMenuSlots={} provider={} reason={} summary={}",
                    screenClassName,
                    menu.getClass().getName(),
                    menu.slots.size(),
                    resolution.diagnostics().providerId(),
                    resolution.diagnostics().reasonCode(),
                    resolution.diagnostics().summary()
            );
            return null;
        }
        StorageViewProviderSession primaryStorageSession = resolution.value();
        InventorySourceDescriptor primaryStorageSource = primaryStorageSession.primaryStorageSource();
        int containerSlotCount = primaryStorageSession.hostSources().stream()
                .filter(source -> source.domain() == InventorySourceDomain.HOST_STORAGE)
                .mapToInt(InventorySourceDescriptor::slotCount)
                .sum();
        boolean primaryStorageIsCarried = primaryStorageSource != null && primaryStorageSource.inCarriedInventory();
        String primaryStorageSourceId = primaryStorageSource == null
                ? (primaryStorageIsCarried ? SOURCE_CARRIED_STORAGE : SOURCE_OPEN_CONTAINER)
                : primaryStorageSource.id();
        String resolvedOpenContainerLabel = primaryStorageSource == null
                ? Component.translatable("slot.source.open_container").getString()
                : primaryStorageSource.label().getString();

        Map<String, List<Integer>> sourceMenuSlots = buildSourceMenuSlots(
                menu,
                playerInventory,
                primaryStorageSession.topology().menuSlotsBySourceId()
        );
        Map<Integer, String> sourceIdsByMenuSlot = buildSourceIdsByMenuSlot(sourceMenuSlots);
        List<InventorySource> resolvedSources = new ArrayList<>();
        for (InventorySourceDescriptor source : primaryStorageSession.hostSources()) {
            resolvedSources.add(source.toInventorySource());
        }
        resolvedSources.add(new BasicInventorySource(SOURCE_PLAYER_MAIN, Component.translatable("slot.source.main").getString(), SourceGroup.PLAYER_MAIN, 10, false, true, true));
        resolvedSources.add(new BasicInventorySource(SOURCE_PLAYER_BACKPACK, Component.translatable("slot.source.backpack").getString(), SourceGroup.CARRIED, 15, false, true, true));
        resolvedSources.add(new BasicInventorySource(SOURCE_PLAYER_ARMOR, Component.translatable("slot.source.armor").getString(), SourceGroup.CARRIED, 25, false, true, true));
        resolvedSources.add(new BasicInventorySource(SOURCE_PLAYER_HOTBAR, Component.translatable("slot.source.hotbar").getString(), SourceGroup.PLAYER_HOTBAR, 20, false, true, true));
        resolvedSources.add(new BasicInventorySource(SOURCE_PLAYER_OFFHAND, Component.translatable("slot.source.offhand").getString(), SourceGroup.CARRIED, 30, false, true, true));

        if (SlotDebugLog.enabled()) {
            SlotDebugLog.log(
                    "Resolved storage layout for screen={} menu={} adapter={} containerSlots={} playerMainSlots={} hotbarSlots={} armorSlots={} offhandSlots={} carriedStorageSlots={} totalMenuSlots={} primaryStorageIsCarried={} primaryMenuBacked={}",
                    screenClassName,
                    menu.getClass().getName(),
                    primaryStorageSession.providerId(),
                    sourceMenuSlots.getOrDefault(primaryStorageSourceId, List.of()).size(),
                    sourceMenuSlots.getOrDefault(SOURCE_PLAYER_MAIN, List.of()).size(),
                    sourceMenuSlots.getOrDefault(SOURCE_PLAYER_HOTBAR, List.of()).size(),
                    sourceMenuSlots.getOrDefault(SOURCE_PLAYER_ARMOR, List.of()).size(),
                    sourceMenuSlots.getOrDefault(SOURCE_PLAYER_OFFHAND, List.of()).size(),
                    sourceMenuSlots.getOrDefault(SOURCE_PLAYER_BACKPACK, List.of()).size()
                            + primaryStorageSession.hostSources().stream()
                            .filter(InventorySourceDescriptor::inCarriedInventory)
                            .mapToInt(source -> sourceMenuSlots.getOrDefault(source.id(), List.of()).size())
                            .sum(),
                    menu.slots.size(),
                    primaryStorageIsCarried,
                    primaryStorageSession.primaryStorageMenuBacked()
            );
        }

        return new ChestLikeMenuLayout(
                containerSlotCount,
                resolvedSources,
                resolvedOpenContainerLabel,
                sourceMenuSlots,
                sourceIdsByMenuSlot,
                primaryStorageIsCarried,
                primaryStorageSession
        );
    }

    public int totalHandledSlots() {
        return sourceIdsByMenuSlot.size()
                + (primaryStorageSession.primaryStorageMenuBacked() ? 0 : Math.max(0, containerSlotCount));
    }

    public String sourceIdForMenuSlot(int menuSlot) {
        return sourceIdsByMenuSlot.get(menuSlot);
    }

    public List<Integer> menuSlotsForSource(String sourceId) {
        return sourceMenuSlots.getOrDefault(sourceId, List.of());
    }

    public int sourceSlotIndexForMenuSlot(String sourceId, int menuSlot) {
        if (!usesSourceRelativeSlotIndex(sourceId)) {
            return menuSlot;
        }
        return menuSlotsForSource(sourceId).indexOf(menuSlot);
    }

    public Integer resolveMenuSlot(String sourceId, int slotIndex) {
        if (!usesSourceRelativeSlotIndex(sourceId)) {
            return slotIndex;
        }
        List<Integer> menuSlots = menuSlotsForSource(sourceId);
        if (menuSlots.contains(slotIndex)) {
            return slotIndex;
        }
        if (slotIndex < 0 || slotIndex >= menuSlots.size()) {
            return null;
        }
        return menuSlots.get(slotIndex);
    }

    public boolean primaryStorageMenuBacked() {
        return primaryStorageSession.primaryStorageMenuBacked();
    }

    public boolean sourceMenuBacked(String sourceId) {
        if (SOURCE_OPEN_CONTAINER.equals(sourceId) || SOURCE_CARRIED_STORAGE.equals(sourceId)) {
            return primaryStorageMenuBacked();
        }
        return !menuSlotsForSource(sourceId).isEmpty();
    }

    public Set<String> unifiedCarriedSourceIds() {
        LinkedHashSet<String> sourceIds = new LinkedHashSet<>(BASE_CARRIED_SOURCES);
        if (primaryStorageIsCarried) {
            sourceIds.add(SOURCE_CARRIED_STORAGE);
        }
        return Set.copyOf(sourceIds);
    }

    public Set<String> sourceIdsForPane(InventoryPane pane) {
        if (pane == InventoryPane.OPEN_CONTAINER) {
            return primaryStorageIsCarried ? Set.of(SOURCE_CARRIED_STORAGE) : OPEN_CONTAINER_SOURCES;
        }
        return primaryStorageIsCarried ? unifiedCarriedSourceIds() : BASE_CARRIED_SOURCES;
    }

    public Set<String> compareSourceIdsForPane(InventoryPane pane) {
        if (!primaryStorageIsCarried) {
            return sourceIdsForPane(pane == InventoryPane.OPEN_CONTAINER ? InventoryPane.CARRIED : InventoryPane.OPEN_CONTAINER);
        }
        return pane == InventoryPane.OPEN_CONTAINER
                ? BASE_CARRIED_SOURCES
                : Set.of(SOURCE_CARRIED_STORAGE);
    }

    public Set<String> actionSourceIdsForPane(InventoryPane pane) {
        if (pane == InventoryPane.OPEN_CONTAINER) {
            return sourceIdsForPane(pane);
        }
        if (!primaryStorageIsCarried) {
            LinkedHashSet<String> sourceIds = new LinkedHashSet<>();
            if (sourceMenuBacked(SOURCE_PLAYER_MAIN)) {
                sourceIds.add(SOURCE_PLAYER_MAIN);
            }
            if (sourceMenuBacked(SOURCE_PLAYER_HOTBAR)) {
                sourceIds.add(SOURCE_PLAYER_HOTBAR);
            }
            if (sourceMenuBacked(SOURCE_PLAYER_ARMOR)) {
                sourceIds.add(SOURCE_PLAYER_ARMOR);
            }
            if (sourceMenuBacked(SOURCE_PLAYER_OFFHAND)) {
                sourceIds.add(SOURCE_PLAYER_OFFHAND);
            }
            return Set.copyOf(sourceIds);
        }
        return unifiedCarriedSourceIds();
    }

    public Set<String> transferTargetSourceIds(InventoryPane sourcePane) {
        if (!primaryStorageIsCarried) {
            return sourceIdsForPane(sourcePane == InventoryPane.OPEN_CONTAINER ? InventoryPane.CARRIED : InventoryPane.OPEN_CONTAINER);
        }
        return sourcePane == InventoryPane.OPEN_CONTAINER
                ? BASE_CARRIED_SOURCES
                : Set.of(SOURCE_CARRIED_STORAGE);
    }

    public String preferredSourceId(InventoryPane pane) {
        if (pane == InventoryPane.OPEN_CONTAINER) {
            return primaryStorageIsCarried ? SOURCE_CARRIED_STORAGE : SOURCE_OPEN_CONTAINER;
        }
        return SOURCE_PLAYER_MAIN;
    }

    public String paneTitle(InventoryPane pane) {
        if (pane == InventoryPane.OPEN_CONTAINER) {
            return primaryStorageIsCarried
                    ? openContainerLabel
                    : Component.translatable("slot.screen.container.pane.open_container").getString();
        }
        return Component.translatable("slot.screen.container.pane.carried").getString();
    }

    public String compareHintLabel(InventoryPane pane) {
        return pane == InventoryPane.OPEN_CONTAINER
                ? Component.translatable("slot.source.short.carried").getString()
                : Component.translatable("slot.source.short.container").getString();
    }

    public String sourceLabel(String sourceId) {
        if (SOURCE_OPEN_CONTAINER.equals(sourceId) || SOURCE_CARRIED_STORAGE.equals(sourceId)) {
            return openContainerLabel;
        }

        for (InventorySource source : sources) {
            if (source.id().equals(sourceId)) {
                return source.displayName();
            }
        }
        return sourceId;
    }

    public String shortSource(String sourceId) {
        return switch (sourceId) {
            case SOURCE_OPEN_CONTAINER -> Component.translatable("slot.source.short.container").getString();
            case SOURCE_CARRIED_STORAGE -> shortCarriedStorageSource();
            case SOURCE_PLAYER_BACKPACK -> Component.translatable("slot.source.short.backpack").getString();
            case SOURCE_PLAYER_MAIN -> Component.translatable("slot.source.short.main").getString();
            case SOURCE_PLAYER_HOTBAR -> Component.translatable("slot.source.short.hotbar").getString();
            case SOURCE_PLAYER_ARMOR -> Component.translatable("slot.source.short.armor").getString();
            case SOURCE_PLAYER_OFFHAND -> Component.translatable("slot.source.short.offhand").getString();
            default -> sourceId;
        };
    }

    public static boolean usesSourceRelativeSlotIndex(String sourceId) {
        return SOURCE_PLAYER_MAIN.equals(sourceId)
                || SOURCE_PLAYER_HOTBAR.equals(sourceId)
                || SOURCE_PLAYER_ARMOR.equals(sourceId)
                || SOURCE_PLAYER_OFFHAND.equals(sourceId);
    }

    public static int mainSourceSlotIndexForInventorySlot(int inventorySlot) {
        if (inventorySlot < 9 || inventorySlot >= 36) {
            return -1;
        }
        return inventorySlot - 9;
    }

    public static int hotbarSourceSlotIndexForInventorySlot(int inventorySlot) {
        if (inventorySlot < 0 || inventorySlot >= 9) {
            return -1;
        }
        return inventorySlot;
    }

    public static int armorSourceSlotIndexForInventorySlot(int armorInventorySlot) {
        if (armorInventorySlot < 0 || armorInventorySlot >= 4) {
            return -1;
        }
        return 3 - armorInventorySlot;
    }

    private String shortCarriedStorageSource() {
        String normalized = openContainerLabel.toLowerCase(Locale.ROOT);
        if (normalized.contains("backpack") || normalized.contains("pack")) {
            return Component.translatable("slot.source.short.backpack").getString();
        }
        return Component.translatable("slot.source.short.carried").getString();
    }

    private static List<Integer> slotRange(int startInclusive, int endInclusive) {
        if (endInclusive < startInclusive) {
            return List.of();
        }
        List<Integer> slots = new ArrayList<>(endInclusive - startInclusive + 1);
        for (int slot = startInclusive; slot <= endInclusive; slot++) {
            slots.add(slot);
        }
        return List.copyOf(slots);
    }

    private static Map<String, List<Integer>> buildSourceMenuSlots(
            AbstractContainerMenu menu,
            Inventory playerInventory,
            Map<String, List<Integer>> providerSourceMenuSlots
    ) {
        Map<String, List<Integer>> sourceSlots = new LinkedHashMap<>();
        if (providerSourceMenuSlots != null) {
            providerSourceMenuSlots.forEach((sourceId, menuSlots) ->
                    sourceSlots.put(sourceId, menuSlots == null ? List.of() : List.copyOf(menuSlots))
            );
        }
        sourceSlots.put(SOURCE_PLAYER_MAIN, new ArrayList<>());
        sourceSlots.put(SOURCE_PLAYER_HOTBAR, new ArrayList<>());
        sourceSlots.put(SOURCE_PLAYER_ARMOR, new ArrayList<>());
        sourceSlots.put(SOURCE_PLAYER_OFFHAND, new ArrayList<>());

        for (int menuSlot = 0; menuSlot < menu.slots.size(); menuSlot++) {
            int currentMenuSlot = menuSlot;
            if (sourceSlots.values().stream().anyMatch(slots -> slots.contains(currentMenuSlot))) {
                continue;
            }

            Slot slot = menu.getSlot(menuSlot);
            if (slot.container != playerInventory) {
                continue;
            }

            int playerSlot = slot.getContainerSlot();
            if (playerSlot >= 0 && playerSlot < 9) {
                sourceSlots.get(SOURCE_PLAYER_HOTBAR).add(menuSlot);
            } else if (playerSlot >= 9 && playerSlot < 36) {
                sourceSlots.get(SOURCE_PLAYER_MAIN).add(menuSlot);
            } else if (playerSlot >= 36 && playerSlot < 40) {
                sourceSlots.get(SOURCE_PLAYER_ARMOR).add(menuSlot);
            } else if (playerSlot == 40) {
                sourceSlots.get(SOURCE_PLAYER_OFFHAND).add(menuSlot);
            }
        }

        return copySourceSlots(sourceSlots);
    }

    private static Map<Integer, String> buildSourceIdsByMenuSlot(Map<String, List<Integer>> sourceMenuSlots) {
        Map<Integer, String> sourceIdsByMenuSlot = new LinkedHashMap<>();
        for (Map.Entry<String, List<Integer>> entry : sourceMenuSlots.entrySet()) {
            for (int menuSlot : entry.getValue()) {
                sourceIdsByMenuSlot.put(menuSlot, entry.getKey());
            }
        }
        return Map.copyOf(sourceIdsByMenuSlot);
    }

    private static Map<String, List<Integer>> copySourceSlots(Map<String, List<Integer>> sourceSlots) {
        Map<String, List<Integer>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<Integer>> entry : sourceSlots.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }
}
