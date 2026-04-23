package dev.imagio.slot.debug;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

public record RealisticAtlasPlan(
        List<VisualAtlasIsland> islands,
        Map<ItemIdentity, VisualHomeAssignment> assignments,
        List<ItemStack> triageStacks,
        List<ChestSpec> chests,
        List<ItemStack> homedStacks
) {
    public RealisticAtlasPlan {
        islands = islands == null ? List.of() : List.copyOf(islands);
        assignments = assignments == null ? Map.of() : Map.copyOf(assignments);
        triageStacks = triageStacks == null ? List.of() : List.copyOf(triageStacks);
        chests = chests == null ? List.of() : List.copyOf(chests);
        homedStacks = homedStacks == null ? List.of() : List.copyOf(homedStacks);
    }
}
