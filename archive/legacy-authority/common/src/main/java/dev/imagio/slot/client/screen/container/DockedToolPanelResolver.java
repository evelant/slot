package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.capability.MenuCapabilityDescriptor;
import dev.imagio.slot.capability.ToolCapabilityDescriptor;
import dev.imagio.slot.session.ChestLikeMenuLayout;
import dev.imagio.slot.session.InventoryHostDescriptor;
import dev.imagio.slot.storage.adapter.ExternalToolPresentation;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;
import dev.imagio.slot.storage.provider.StorageViewProviderSession;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Comparator;

public final class DockedToolPanelResolver {
    private DockedToolPanelResolver() {
    }

    public static DockedToolPanel resolve(AbstractContainerMenu menu, InventoryHostDescriptor host) {
        if (host == null) {
            return null;
        }
        return resolve(menu, host.layout(), host.capabilities(), host.providerSession());
    }

    public static DockedToolPanel resolve(AbstractContainerMenu menu, ChestLikeMenuLayout layout) {
        return resolve(menu, layout, null, layout == null ? null : layout.primaryStorageSession());
    }

    private static DockedToolPanel resolve(
            AbstractContainerMenu menu,
            ChestLikeMenuLayout layout,
            MenuCapabilityDescriptor capabilities,
            StorageViewProviderSession providerSession
    ) {
        if (menu == null || layout == null) {
            return null;
        }

        if (capabilities != null) {
            return capabilities.liveTools().stream()
                    .filter(tool -> tool.toolSpec() != null
                            && tool.toolSpec().preferredPresentation() == ExternalToolPresentation.DOCKED)
                    .sorted(Comparator.comparingInt((ToolCapabilityDescriptor tool) -> tool.toolSpec().priority())
                            .thenComparing(ToolCapabilityDescriptor::id))
                    .findFirst()
                    .map(tool -> createPanel(menu, tool))
                    .orElse(null);
        }

        return providerSession == null
                ? null
                : providerSession.toolDescriptors().stream()
                        .filter(ToolCapabilityDescriptor::live)
                        .filter(tool -> tool.toolSpec() != null
                                && tool.toolSpec().preferredPresentation() == ExternalToolPresentation.DOCKED)
                        .sorted(Comparator.comparingInt((ToolCapabilityDescriptor tool) -> tool.toolSpec().priority())
                                .thenComparing(ToolCapabilityDescriptor::id))
                        .findFirst()
                        .map(tool -> createPanel(menu, tool))
                        .orElse(null);
    }

    public static DockedToolPanel createPanel(AbstractContainerMenu menu, ToolCapabilityDescriptor tool) {
        if (tool == null || tool.toolSpec() == null) {
            return null;
        }
        return switch (tool.kind()) {
            case CRAFTING_GRID -> new CraftingGridToolPanel(menu, tool);
        };
    }

    public static DockedToolPanel reconcile(AbstractContainerMenu menu, DockedToolPanel existing, ToolCapabilityDescriptor tool) {
        if (tool == null || tool.toolSpec() == null) {
            return null;
        }
        if (existing instanceof CraftingGridToolPanel craftingPanel
                && DockedToolPanelReusePolicy.canReuse(craftingPanel.matches(menu, tool), craftingPanel.toolKind(), tool.kind())) {
            craftingPanel.updateToolDescriptor(tool);
            return craftingPanel;
        }
        return createPanel(menu, tool);
    }
}
