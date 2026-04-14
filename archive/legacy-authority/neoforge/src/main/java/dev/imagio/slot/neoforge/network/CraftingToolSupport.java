package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.session.StorageViewResolver;
import dev.imagio.slot.storage.adapter.ExternalToolKind;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.List;

final class CraftingToolSupport {
    private CraftingToolSupport() {
    }

    static List<ExternalToolSpec> resolveCraftingTools(AbstractContainerMenu menu, ServerPlayer player) {
        if (menu == null || player == null) {
            return List.of();
        }

        InventoryHostDescriptor host = StorageViewResolver.resolve(null, menu, player.getInventory(), null);
        return host == null
                ? List.of()
                : host.capabilities().liveToolSpecsOfKind(ExternalToolKind.CRAFTING_GRID);
    }
}
