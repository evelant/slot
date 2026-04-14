package dev.imagio.slot.session;

import dev.imagio.slot.capability.ToolCapabilityDescriptor;
import dev.imagio.slot.storage.adapter.ExternalToolKind;
import dev.imagio.slot.storage.adapter.ExternalToolPresentation;

import java.util.Comparator;
import java.util.List;

public final class InventoryHostToolCoordinator {
    private InventoryHostToolCoordinator() {
    }

    public static List<InventoryToolDescriptor> toolDescriptors(InventoryHostDescriptor host) {
        return host == null ? List.of() : host.toolDescriptors();
    }

    public static List<ToolCapabilityDescriptor> tools(InventoryHostDescriptor host) {
        return host == null ? List.of() : host.capabilities().tools();
    }

    public static InventoryToolDescriptor preferredDockedToolDescriptor(InventoryHostDescriptor host) {
        return toolDescriptors(host).stream()
                .filter(InventoryToolDescriptor::live)
                .filter(tool -> tool.presentationSpec() != null
                        && tool.presentationSpec().preferredPresentation() == ExternalToolPresentation.DOCKED)
                .sorted(Comparator
                        .comparingInt((InventoryToolDescriptor tool) -> tool.presentationSpec().priority())
                        .thenComparing(InventoryToolDescriptor::id))
                .findFirst()
                .orElse(null);
    }

    public static ToolCapabilityDescriptor preferredDockedTool(InventoryHostDescriptor host) {
        InventoryToolDescriptor tool = preferredDockedToolDescriptor(host);
        return tool == null ? null : host.capabilities().toolById(tool.id());
    }

    public static InventoryToolDescriptor firstToolDescriptor(
            InventoryHostDescriptor host,
            ExternalToolKind kind,
            String providerId
    ) {
        if (host == null || kind == null) {
            return null;
        }
        return toolDescriptors(host).stream()
                .filter(tool -> tool.kind() == kind)
                .filter(tool -> providerId == null || providerId.isBlank() || providerId.equals(tool.providerId()))
                .findFirst()
                .orElse(null);
    }

    public static ToolCapabilityDescriptor firstTool(
            InventoryHostDescriptor host,
            ExternalToolKind kind,
            String providerId
    ) {
        InventoryToolDescriptor tool = firstToolDescriptor(host, kind, providerId);
        return tool == null ? null : host.capabilities().toolById(tool.id());
    }

    public static ToolOpenCommand activationCommand(InventoryToolDescriptor tool) {
        if (tool == null || tool.live()) {
            return null;
        }
        return tool.activationCommand();
    }

    public static ToolOpenCommand activationCommand(ToolCapabilityDescriptor tool) {
        if (tool == null || tool.live()) {
            return null;
        }
        return tool.activationCommand();
    }
}
