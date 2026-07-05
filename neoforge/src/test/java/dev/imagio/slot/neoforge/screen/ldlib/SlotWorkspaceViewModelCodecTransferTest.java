package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.core.ItemComparisonMode;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.CraftRunState;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotWorkspaceViewModelCodecTransferTest {
    private static final HolderLookup.Provider PROVIDER = new HolderLookup.Provider() {
    };

    @Test
    void transferEncodingSendsFullSnapshotThenDelta() {
        SlotWorkspaceViewModel first = minimalView(1, "ready", "");
        SlotWorkspaceViewModelCodec.EncodedSliceCache cache =
                new SlotWorkspaceViewModelCodec.EncodedSliceCache();

        CompoundTag full = SlotWorkspaceViewModelCodec.encodeTransfer(first, PROVIDER, cache, false);

        assertEquals("FULL_SNAPSHOT", full.getString("__slotViewMode"));
        assertTrue(full.contains("atlasItems"));
        SlotWorkspaceViewModelCodec.TransferApplyResult appliedFull =
                SlotWorkspaceViewModelCodec.applyTransfer(PROVIDER, null, full);
        assertTrue(appliedFull.applied());

        CompoundTag delta = SlotWorkspaceViewModelCodec.encodeTransfer(
                minimalView(2, "busy", "delta-test"),
                PROVIDER,
                cache,
                false);

        assertEquals("DELTA", delta.getString("__slotViewMode"));
        assertEquals(1L, delta.getLong("__slotBaseRevision"));
        assertEquals(2L, delta.getLong("__slotRevision"));
        assertTrue(delta.contains("status"));
        assertFalse(delta.contains("atlasItems"));

        SlotWorkspaceViewModelCodec.TransferApplyResult appliedDelta =
                SlotWorkspaceViewModelCodec.applyTransfer(PROVIDER, appliedFull.fullTag(), delta);

        assertTrue(appliedDelta.applied());
        assertEquals(2, appliedDelta.viewModel().revision());
        assertEquals("busy", appliedDelta.viewModel().status());
        assertEquals(1, appliedDelta.viewModel().atlasItems().size());
    }

    @Test
    void transferDeltaRejectsMissingBaseAndAllowsRepeat() {
        SlotWorkspaceViewModel first = minimalView(1, "ready", "");
        SlotWorkspaceViewModelCodec.EncodedSliceCache cache =
                new SlotWorkspaceViewModelCodec.EncodedSliceCache();
        CompoundTag full = SlotWorkspaceViewModelCodec.encodeTransfer(first, PROVIDER, cache, false);
        SlotWorkspaceViewModelCodec.TransferApplyResult appliedFull =
                SlotWorkspaceViewModelCodec.applyTransfer(PROVIDER, null, full);
        CompoundTag delta = SlotWorkspaceViewModelCodec.encodeTransfer(
                minimalView(2, "busy", "delta-test"),
                PROVIDER,
                cache,
                false);

        SlotWorkspaceViewModelCodec.TransferApplyResult missing =
                SlotWorkspaceViewModelCodec.applyTransfer(PROVIDER, null, delta);
        SlotWorkspaceViewModelCodec.TransferApplyResult appliedDelta =
                SlotWorkspaceViewModelCodec.applyTransfer(PROVIDER, appliedFull.fullTag(), delta);
        SlotWorkspaceViewModelCodec.TransferApplyResult repeated =
                SlotWorkspaceViewModelCodec.applyTransfer(PROVIDER, appliedDelta.fullTag(), delta);

        assertFalse(missing.applied());
        assertTrue(missing.requiresFullSnapshot());
        assertTrue(repeated.applied());
        assertEquals(2, repeated.viewModel().revision());
    }

    private static SlotWorkspaceViewModel minimalView(long revision, String status, String diagnostics) {
        SlotWorkspaceViewModel.IdentityRef stone = new SlotWorkspaceViewModel.IdentityRef(
                "minecraft:stone",
                ItemComparisonMode.ITEM_ID.name(),
                "");
        ItemIdentity stoneIdentity = ItemIdentity.of("minecraft:stone");
        return new SlotWorkspaceViewModel(
                revision,
                status,
                diagnostics,
                0,
                0,
                1,
                1,
                7,
                36,
                List.of(),
                List.of(new SlotWorkspaceViewModel.AtlasItem(
                        stone,
                        new ItemStack("minecraft:stone", 5, 64),
                        "Stone",
                        5,
                        0,
                        "island-raw",
                        true,
                        true,
                        true,
                        List.of())),
                List.of(),
                List.of(),
                List.of(),
                SlotWorkspaceViewModel.emptyHotbar(),
                SlotWorkspaceViewModel.OffhandSlot.empty(),
                List.of(),
                SlotWorkspaceViewModel.LootChestPanel.empty(),
                List.of(),
                Set.of(SlotWorkspaceViewModel.IdentityRef.from(stoneIdentity)),
                List.of(),
                SlotWorkspaceViewModel.ActiveChestPanel.empty(),
                CraftRunState.empty(),
                List.of());
    }
}
