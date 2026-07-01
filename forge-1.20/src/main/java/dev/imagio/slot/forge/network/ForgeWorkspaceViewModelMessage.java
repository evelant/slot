package dev.imagio.slot.forge.network;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.action.WorkspaceActionEnvelope;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record ForgeWorkspaceViewModelMessage(
        WorkspaceActionEnvelope envelope,
        SlotWorkspaceViewModel viewModel,
        CompoundTag encodedView
) {
    public ForgeWorkspaceViewModelMessage {
        if (envelope == null) {
            throw new IllegalArgumentException("envelope must not be null");
        }
        viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        encodedView = encodedView == null ? null : encodedView.copy();
    }

    public ForgeWorkspaceViewModelMessage(
            WorkspaceActionEnvelope envelope,
            SlotWorkspaceViewModel viewModel
    ) {
        this(envelope, viewModel, null);
    }

    static void encode(ForgeWorkspaceViewModelMessage message, FriendlyByteBuf buffer) {
        WorkspaceActionEnvelope envelope = message.envelope();
        buffer.writeUtf(envelope.sessionId());
        buffer.writeInt(envelope.menuContainerId());
        buffer.writeLong(envelope.viewRevision());
        CompoundTag tag = message.encodedView() == null
                ? Forge120WorkspaceViewModelCodec.encode(message.viewModel())
                : message.encodedView();
        buffer.writeNbt(tag);
    }

    static ForgeWorkspaceViewModelMessage decode(FriendlyByteBuf buffer) {
        WorkspaceActionEnvelope envelope = new WorkspaceActionEnvelope(
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readLong());
        CompoundTag tag = buffer.readNbt();
        return new ForgeWorkspaceViewModelMessage(envelope, Forge120WorkspaceViewModelCodec.decode(tag), tag);
    }
}
