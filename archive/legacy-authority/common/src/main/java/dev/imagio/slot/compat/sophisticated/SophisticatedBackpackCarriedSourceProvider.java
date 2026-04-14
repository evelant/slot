package dev.imagio.slot.compat.sophisticated;

import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.InventorySourceBackingKind;
import dev.imagio.slot.session.InventorySourceCapability;
import dev.imagio.slot.session.InventorySourceDescriptor;
import dev.imagio.slot.session.InventorySourceDomain;
import dev.imagio.slot.session.InventorySourceRole;
import dev.imagio.slot.storage.provider.SupplementalCarriedSourceDescriptor;
import dev.imagio.slot.storage.provider.SupplementalCarriedSourceProvider;
import dev.imagio.slot.storage.provider.SupplementalCarriedStackSnapshot;
import net.minecraft.network.chat.Component;
import net.minecraft.client.player.LocalPlayer;

import java.util.List;
import java.util.Set;

public final class SophisticatedBackpackCarriedSourceProvider implements SupplementalCarriedSourceProvider {
    private static final String PROVIDER_ID = "sophisticatedbackpacks:carried";

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public List<SupplementalCarriedSourceDescriptor> describeDefault(Set<String> sourceIds) {
        if (!SophisticatedBackpackSupport.isAvailable()
                || sourceIds == null
                || !sourceIds.contains(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK)) {
            return List.of();
        }

        return List.of(new SupplementalCarriedSourceDescriptor(
                providerId(),
                ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK,
                "",
                backpackSourceDescriptor()
        ));
    }

    @Override
    public List<SupplementalCarriedSourceDescriptor> describe(InventoryHostDescriptor host) {
        if (!SophisticatedBackpackSupport.isAvailable()
                || host == null
                || !host.carriedSourceIds().contains(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK)) {
            return List.of();
        }

        return List.of(new SupplementalCarriedSourceDescriptor(
                providerId(),
                ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK,
                encodeCarrier(SophisticatedBackpackSupport.openedBackpackCarrier(host.menu())),
                backpackSourceDescriptor()
        ));
    }

    @Override
    public List<SupplementalCarriedStackSnapshot> readSnapshots(
            LocalPlayer player,
            InventoryHostDescriptor host,
            SupplementalCarriedSourceDescriptor descriptor
    ) {
        if (player == null || descriptor == null) {
            return List.of();
        }

        SophisticatedBackpackSupport.BackpackCarrierRef excludedCarrier = decodeCarrier(descriptor.referenceKey());
        return SophisticatedBackpackSupport.readPlayerBackpackStacks(player, excludedCarrier).stream()
                .map(stack -> new SupplementalCarriedStackSnapshot(descriptor.sourceId(), stack.slotIndex(), stack.stack()))
                .toList();
    }

    @Override
    public int slotCapacity(
            LocalPlayer player,
            InventoryHostDescriptor host,
            SupplementalCarriedSourceDescriptor descriptor
    ) {
        if (player == null || descriptor == null) {
            return 0;
        }
        return SophisticatedBackpackSupport.countPlayerBackpackSlots(player, decodeCarrier(descriptor.referenceKey()));
    }

    public static String encodeReference(SophisticatedBackpackSupport.BackpackCarrierRef carrier) {
        if (carrier == null) {
            return "";
        }
        return carrier.handlerName() + "|" + carrier.identifier() + "|" + carrier.carrierSlotIndex();
    }

    public static SophisticatedBackpackSupport.BackpackCarrierRef carrierFromReference(String encoded) {
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

    private static String encodeCarrier(SophisticatedBackpackSupport.BackpackCarrierRef carrier) {
        return encodeReference(carrier);
    }

    private static SophisticatedBackpackSupport.BackpackCarrierRef decodeCarrier(String encoded) {
        return carrierFromReference(encoded);
    }

    private static InventorySourceDescriptor backpackSourceDescriptor() {
        return InventorySourceDescriptor.builder(ChestLikeMenuLayout.SOURCE_PLAYER_BACKPACK)
                .label(Component.translatable("slot.source.backpack"))
                .domain(InventorySourceDomain.SUPPLEMENTAL_CARRIED)
                .role(InventorySourceRole.PROVIDER_DEFINED)
                .slotCount(0)
                .backingKind(InventorySourceBackingKind.PROVIDER_BACKED)
                .capabilities(Set.of(
                        InventorySourceCapability.INSERT,
                        InventorySourceCapability.EXTRACT,
                        InventorySourceCapability.QUICK_ACCESS_ASSIGN
                ))
                .actionable(true)
                .menuBacked(false)
                .stableOrder(15)
                .build();
    }
}
