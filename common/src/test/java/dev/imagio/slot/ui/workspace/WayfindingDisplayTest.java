package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WayfindingDisplayTest {
    @Test
    void usesChestChipLocationWhenNoWayfindingTargetExists() {
        SlotWorkspaceViewModel.ChestChip chip = new SlotWorkspaceViewModel.ChestChip(
                "storage-1",
                "minecraft:overworld",
                "Chest",
                1,
                27,
                1,
                false,
                0,
                0,
                64,
                10);

        WayfindingDisplay.CardText text = WayfindingDisplay.forStorage(
                "storage-1",
                List.of(),
                List.of(chip),
                "minecraft:overworld",
                0.5,
                64.5,
                0.5,
                0f);

        assertEquals("↑", text.arrow());
        assertEquals("10m", text.distance());
    }

    @Test
    void crossDimensionShowsDimensionShortName() {
        WayfindingDisplay.CardText text = WayfindingDisplay.forLocation(
                "minecraft:the_nether",
                0,
                64,
                0,
                "minecraft:overworld",
                0,
                64,
                0,
                0f);

        assertEquals("nether", text.arrow());
        assertEquals("", text.distance());
    }

    @Test
    void displayStorageLabelUsesDisplayKind() {
        String storageId = WorldDisplayStorageSource.storageId(
                WorldDisplayStorageKind.TOOL_RACK,
                "minecraft:overworld",
                4,
                64,
                0);
        WayfindingTarget target = new WayfindingTarget(
                storageId,
                "minecraft:overworld",
                4,
                64,
                0,
                Set.of(),
                1,
                WayfindingTarget.Scope.WANTED);

        assertEquals("Tool rack", WayfindingDisplay.chestLabel(target));
    }
}
