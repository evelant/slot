package dev.imagio.slot.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.client.model.ComparisonMode;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.screen.container.MenuSlotId;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CraftingGridPlacementPayload(
        int containerId,
        int targetMenuSlot,
        SourcePane sourcePane,
        String itemId,
        ComparisonMode comparisonMode,
        String componentFingerprint
) implements CustomPacketPayload {
    public static final Type<CraftingGridPlacementPayload> TYPE = new Type<>(SlotCommon.id("crafting_grid_place"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingGridPlacementPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CraftingGridPlacementPayload decode(RegistryFriendlyByteBuf buf) {
            return new CraftingGridPlacementPayload(
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readEnum(SourcePane.class),
                    buf.readUtf(),
                    buf.readEnum(ComparisonMode.class),
                    buf.readUtf()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CraftingGridPlacementPayload payload) {
            buf.writeVarInt(payload.containerId);
            buf.writeVarInt(payload.targetMenuSlot);
            buf.writeEnum(payload.sourcePane);
            buf.writeUtf(payload.itemId);
            buf.writeEnum(payload.comparisonMode);
            buf.writeUtf(payload.componentFingerprint);
        }
    };

    public CraftingGridPlacementPayload {
        sourcePane = sourcePane == null ? SourcePane.CARRIED : sourcePane;
        itemId = itemId == null ? "" : itemId;
        comparisonMode = comparisonMode == null ? ComparisonMode.ITEM_ID : comparisonMode;
        componentFingerprint = componentFingerprint == null ? "" : componentFingerprint;
    }

    public static CraftingGridPlacementPayload placeOne(
            int containerId,
            int targetMenuSlot,
            SourcePane sourcePane,
            ItemIdentity identity
    ) {
        ItemIdentity resolvedIdentity = identity;
        return new CraftingGridPlacementPayload(
                containerId,
                targetMenuSlot,
                sourcePane,
                resolvedIdentity == null ? "" : resolvedIdentity.itemId(),
                resolvedIdentity == null ? ComparisonMode.ITEM_ID : resolvedIdentity.comparisonMode(),
                resolvedIdentity == null ? "" : resolvedIdentity.componentFingerprint()
        );
    }

    public static CraftingGridPlacementPayload placeOne(
            int containerId,
            MenuSlotId targetMenuSlot,
            SourcePane sourcePane,
            ItemIdentity identity
    ) {
        return placeOne(containerId, targetMenuSlot == null ? -1 : targetMenuSlot.orElse(-1), sourcePane, identity);
    }

    public ItemIdentity identity() {
        if (itemId == null || itemId.isBlank()) {
            return null;
        }
        return new ItemIdentity(itemId, comparisonMode, componentFingerprint);
    }

    public MenuSlotId targetMenuSlotId() {
        return MenuSlotId.of(targetMenuSlot);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum SourcePane {
        OPEN_CONTAINER,
        CARRIED
    }
}
