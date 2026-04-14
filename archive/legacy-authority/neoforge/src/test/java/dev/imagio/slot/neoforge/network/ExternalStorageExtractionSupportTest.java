package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.storage.adapter.ExternalStorageStackSnapshot;
import dev.imagio.slot.storage.adapter.StorageTransferMode;
import net.minecraft.server.level.ServerPlayer;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalStorageExtractionSupportTest {
    @Test
    void exactIdentityDoesNotRelaxToItemIdFallback() {
        RecordingSession session = new RecordingSession();
        ItemIdentity identity = ItemIdentity.exact("minecraft:potion", "healing");

        ItemStack extracted = ExternalStorageExtractionSupport.extractMatchingIdentity(
                null,
                null,
                session,
                identity,
                StorageTransferMode.STACK
        );

        assertTrue(extracted.isEmpty());
        assertEquals(List.of(identity), session.identities);
    }

    @Test
    void itemIdIdentityStillExtractsNormally() {
        RecordingSession session = new RecordingSession();
        ItemIdentity identity = ItemIdentity.of("minecraft:stone");

        ItemStack extracted = ExternalStorageExtractionSupport.extractMatchingIdentity(
                null,
                null,
                session,
                identity,
                StorageTransferMode.ONE
        );

        assertEquals(1, extracted.getCount());
        assertEquals(List.of(identity), session.identities);
    }

    private static final class RecordingSession implements StorageViewProviderSession {
        private final List<ItemIdentity> identities = new ArrayList<>();

        @Override
        public String providerId() {
            return "test";
        }

        @Override
        public dev.imagio.slot.session.InventorySourceDescriptor primaryStorageSource() {
            return dev.imagio.slot.session.InventorySourceDescriptor.builder("open_container")
                    .label(net.minecraft.network.chat.Component.literal("test"))
                    .domain(dev.imagio.slot.session.InventorySourceDomain.HOST_STORAGE)
                    .role(dev.imagio.slot.session.InventorySourceRole.PRIMARY_STORAGE)
                    .slotCount(1)
                    .backingKind(dev.imagio.slot.session.InventorySourceBackingKind.MENU_BACKED)
                    .capabilities(java.util.Set.of(
                            dev.imagio.slot.session.InventorySourceCapability.INSERT,
                            dev.imagio.slot.session.InventorySourceCapability.EXTRACT
                    ))
                    .actionable(true)
                    .menuBacked(false)
                    .build();
        }

        @Override
        public boolean primaryStorageIsCarried() {
            return false;
        }

        @Override
        public List<Integer> primaryMenuSlots() {
            return List.of();
        }

        @Override
        public List<ExternalStorageStackSnapshot> readClientPrimarySnapshots(AbstractContainerMenu menu) {
            return List.of();
        }

        @Override
        public ItemStack extractFromPrimary(
                AbstractContainerMenu menu,
                ServerPlayer player,
                ItemIdentity identity,
                StorageTransferMode mode
        ) {
            identities.add(identity);
            if ("minecraft:stone".equals(identity.itemId()) && identity.componentFingerprint().isEmpty()) {
                return new ItemStack(identity.itemId(), 1, 64);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertIntoPrimary(AbstractContainerMenu menu, ServerPlayer player, ItemStack stack) {
            return stack;
        }

    }
}
