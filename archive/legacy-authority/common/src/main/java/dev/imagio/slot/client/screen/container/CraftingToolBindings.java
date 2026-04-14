package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.storage.adapter.ExternalToolSlotRegion;
import dev.imagio.slot.storage.adapter.ExternalToolSlotRole;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;

import java.util.Objects;

final class CraftingToolBindings {
    private CraftingToolBindings() {
    }

    static ExternalToolSlotRegion requiredRegion(ExternalToolSpec spec, ExternalToolSlotRole role) {
        if (spec == null) {
            throw new IllegalArgumentException("Crafting tool spec is required");
        }
        return spec.firstRegion(role)
                .orElseThrow(() -> new IllegalArgumentException("Crafting tool requires a " + role.name().toLowerCase() + " region"));
    }

    static ExternalToolSlotRegion optionalRegion(ExternalToolSpec spec, ExternalToolSlotRole role) {
        if (spec == null) {
            return null;
        }
        return spec.firstRegion(role).orElse(null);
    }

    static boolean preservesPendingInteraction(
            ExternalToolSlotRegion currentInput,
            ExternalToolSlotRegion currentOutput,
            ExternalToolSlotRegion nextInput,
            ExternalToolSlotRegion nextOutput
    ) {
        return Objects.equals(currentInput, nextInput)
                && Objects.equals(currentOutput, nextOutput);
    }

    static boolean preservesPendingInteraction(ExternalToolSpec current, ExternalToolSpec next) {
        return preservesPendingInteraction(
                requiredRegion(current, ExternalToolSlotRole.INPUT),
                optionalRegion(current, ExternalToolSlotRole.OUTPUT),
                requiredRegion(next, ExternalToolSlotRole.INPUT),
                optionalRegion(next, ExternalToolSlotRole.OUTPUT)
        );
    }
}
