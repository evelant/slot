package dev.imagio.slot.client.screen;

import dev.imagio.slot.client.source.InventorySource;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.StorageViewResolver;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Objects;
import java.util.Set;

public final class InventoryScreenContext {
    private final InventoryHostDescriptor host;

    public InventoryScreenContext(InventoryHostDescriptor host) {
        this.host = Objects.requireNonNull(host, "host");
    }

    public static InventoryScreenContext fromHost(InventoryHostDescriptor host) {
        return host == null ? null : new InventoryScreenContext(host);
    }

    public static InventoryScreenContext carriedOnly(
            Component title,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout
    ) {
        if (menu == null || layout == null) {
            return null;
        }
        return fromHost(InventoryHostDescriptor.create("", title, menu, layout, -1, false, false, true));
    }

    public static InventoryScreenContext carriedAndExternal(
            Component title,
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout
    ) {
        if (menu == null || layout == null) {
            return null;
        }
        return fromHost(InventoryHostDescriptor.create("", title, menu, layout, -1, false, false, false));
    }

    public static InventoryScreenContext resolve(
            Component title,
            AbstractContainerMenu menu,
            net.minecraft.world.entity.player.Inventory playerInventory,
            String screenClassName,
            boolean slotOwned,
            boolean recordsRecent,
            boolean carriedOnly
    ) {
        return fromHost(StorageViewResolver.resolve(
                title,
                menu,
                playerInventory,
                screenClassName,
                slotOwned,
                recordsRecent,
                carriedOnly
        ));
    }

    public InventoryHostDescriptor host() {
        return host;
    }

    public Component title() {
        return host.title();
    }

    public AbstractContainerMenu menu() {
        return host.menu();
    }

    public ChestLikeMenuLayout layout() {
        return host.layout();
    }

    public Set<String> carriedSourceIds() {
        return host.carriedSourceIds();
    }

    public Set<String> externalSourceIds() {
        return host.externalSourceIds();
    }

    public Set<String> actionableSourceIds() {
        return host.actionableSourceIds();
    }

    public Set<String> menuBackedCarriedSourceIds() {
        return host.menuBackedCarriedSourceIds();
    }

    public boolean carriedOnly() {
        return host.carriedOnly();
    }

    public boolean includesSource(String sourceId) {
        return host.includesSource(sourceId);
    }

    public boolean menuBacksSource(String sourceId) {
        return host.menuBacksSource(sourceId);
    }

    public CarriedSourceSet carriedSourceSet() {
        return CarriedSourceSet.of(carriedSourceIds(), menuBackedCarriedSourceIds());
    }

    public java.util.List<InventorySource> sources() {
        return host.sources();
    }

    public java.util.List<dev.imagio.slot.storage.provider.SupplementalCarriedSourceDescriptor> supplementalCarriedSources() {
        return host.supplementalCarriedSources();
    }

    public java.util.List<dev.imagio.slot.storage.provider.SupplementalCarriedSourceDescriptor> supplementalCarriedSources(String sourceId) {
        return host.supplementalCarriedSources(sourceId);
    }

    public boolean primaryStorageIsCarried() {
        return host.hasOnlyCarriedHostStorage();
    }

    public Set<String> sourceIdsForPane(InventoryPane pane) {
        return host.sourceIdsForPane(pane);
    }
}
