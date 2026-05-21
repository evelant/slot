package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.neoforge.screen.ldlib.SlotWorkspaceViewModelCodec;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlotCraftRunRecipePayload(CraftRunRecipeCapture capture) implements CustomPacketPayload {
    public static final Type<SlotCraftRunRecipePayload> TYPE = new Type<>(SlotCommon.id("craft_run_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlotCraftRunRecipePayload> STREAM_CODEC =
            StreamCodec.of(SlotCraftRunRecipePayload::encode, SlotCraftRunRecipePayload::decode);

    public SlotCraftRunRecipePayload {
        capture = capture == null ? CraftRunRecipeCapture.empty() : capture;
    }

    public static SlotCraftRunRecipePayload add(CraftRunRecipeCapture capture) {
        return new SlotCraftRunRecipePayload(capture);
    }

    private static void encode(RegistryFriendlyByteBuf buffer, SlotCraftRunRecipePayload payload) {
        SlotCraftRunRecipePayload resolved = payload == null ? add(CraftRunRecipeCapture.empty()) : payload;
        buffer.writeNbt(SlotWorkspaceViewModelCodec.encodeCraftRunRecipeCapture(resolved.capture()));
    }

    private static SlotCraftRunRecipePayload decode(RegistryFriendlyByteBuf buffer) {
        CompoundTag tag = buffer.readNbt();
        return new SlotCraftRunRecipePayload(SlotWorkspaceViewModelCodec.decodeCraftRunRecipeCapture(tag));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
