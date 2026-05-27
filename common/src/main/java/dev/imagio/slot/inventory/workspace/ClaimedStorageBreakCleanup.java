package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public final class ClaimedStorageBreakCleanup {
    private ClaimedStorageBreakCleanup() {
    }

    public static boolean removeBrokenAnchor(
            MinecraftServer server,
            WorkflowDomainRuntime runtime,
            UUID storageId,
            ChestAnchor anchor
    ) {
        if (runtime == null || storageId == null || anchor == null) {
            return false;
        }
        ClaimedChest before = runtime.chestClaimWorkflow().chest(storageId);
        if (before == null || !before.anchors().contains(anchor)) {
            return false;
        }
        ClaimedChest after = runtime.chestClaimWorkflow().removeAnchor(
                storageId,
                anchor,
                DomainEventMetadata.origin("workflow.storage.chest.break"));
        boolean removed = after == null || !after.anchors().contains(anchor);
        if (removed && runtime.chestClaimWorkflow().chest(storageId) == null) {
            forgetRememberedContents(server, storageId);
        }
        return removed;
    }

    public static boolean forgetRememberedContents(MinecraftServer server, UUID storageId) {
        if (server == null || storageId == null) {
            return false;
        }
        WorkspaceStorageMemoryStore store = WorkspaceStorageMemoryStore.forServer(server);
        return store != null && store.forget(storageId.toString());
    }
}
