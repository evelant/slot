package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.storage.adapter.ExternalToolSlotRegion;
import dev.imagio.slot.storage.adapter.ExternalToolSlotRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingToolBindingsTest {
    @Test
    void preservesPendingInteractionWhenSlotBindingsStayTheSame() {
        ExternalToolSlotRegion currentInput = ExternalToolSlotRegion.grid("inputs", ExternalToolSlotRole.INPUT, 3, List.of(166, 167, 168, 169, 170, 171, 172, 173, 174));
        ExternalToolSlotRegion currentOutput = ExternalToolSlotRegion.single("result", ExternalToolSlotRole.OUTPUT, 175);
        ExternalToolSlotRegion nextInput = ExternalToolSlotRegion.grid("inputs", ExternalToolSlotRole.INPUT, 3, List.of(166, 167, 168, 169, 170, 171, 172, 173, 174));
        ExternalToolSlotRegion nextOutput = ExternalToolSlotRegion.single("result", ExternalToolSlotRole.OUTPUT, 175);

        assertTrue(CraftingToolBindings.preservesPendingInteraction(currentInput, currentOutput, nextInput, nextOutput));
    }

    @Test
    void clearsPendingInteractionWhenInputMappingChanges() {
        ExternalToolSlotRegion currentInput = ExternalToolSlotRegion.grid("inputs", ExternalToolSlotRole.INPUT, 3, List.of(166, 167, 168, 169, 170, 171, 172, 173, 174));
        ExternalToolSlotRegion currentOutput = ExternalToolSlotRegion.single("result", ExternalToolSlotRole.OUTPUT, 175);
        ExternalToolSlotRegion nextInput = ExternalToolSlotRegion.grid("inputs", ExternalToolSlotRole.INPUT, 3, List.of(176, 177, 178, 179, 180, 181, 182, 183, 184));

        assertFalse(CraftingToolBindings.preservesPendingInteraction(currentInput, currentOutput, nextInput, currentOutput));
    }

    @Test
    void clearsPendingInteractionWhenResultMappingChanges() {
        ExternalToolSlotRegion currentInput = ExternalToolSlotRegion.grid("inputs", ExternalToolSlotRole.INPUT, 3, List.of(166, 167, 168, 169, 170, 171, 172, 173, 174));
        ExternalToolSlotRegion currentOutput = ExternalToolSlotRegion.single("result", ExternalToolSlotRole.OUTPUT, 175);
        ExternalToolSlotRegion nextOutput = ExternalToolSlotRegion.single("result", ExternalToolSlotRole.OUTPUT, 190);

        assertFalse(CraftingToolBindings.preservesPendingInteraction(currentInput, currentOutput, currentInput, nextOutput));
    }
}
