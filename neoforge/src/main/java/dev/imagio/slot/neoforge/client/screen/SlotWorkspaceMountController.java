package dev.imagio.slot.neoforge.client.screen;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceComposer;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceProfileId;
import dev.imagio.slot.neoforge.client.host.ObservedScreenContext;
import dev.imagio.slot.neoforge.client.host.ObservedScreenContexts;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class SlotWorkspaceMountController {
    private static boolean registered;

    private SlotWorkspaceMountController() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotWorkspaceMountController::onScreenOpening);
        registered = true;
    }

    private static void onScreenOpening(ScreenEvent.Opening event) {
        Screen candidate = event.getNewScreen();
        if (!(candidate instanceof AbstractContainerScreen<?> containerScreen) || candidate instanceof SlotWorkspaceScreen) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            fail("missing_client_or_player", candidate);
            return;
        }
        if (!SlotClientConfig.CLIENT.enabled.get()) {
            fail("client_disabled", candidate);
            return;
        }
        if (!(candidate instanceof InventoryScreen)) {
            fail("only_player_inventory_enabled_for_first_slice", candidate);
            return;
        }
        if (!SlotClientConfig.CLIENT.replacePlayerInventory.get()) {
            fail("replace_player_inventory_disabled", candidate);
            return;
        }

        ObservedScreenContext observed = ObservedScreenContexts.observe(containerScreen, minecraft.player.getInventory());
        if (observed == null) {
            fail("observation_failed", candidate);
            return;
        }

        InventoryHostDescriptor host = InventoryHostResolver.resolve(observed.toHostContext());
        if (host == null) {
            fail("host_resolution_failed", candidate);
            return;
        }
        if (!host.observationHints().carriedOnly()) {
            fail("not_carried_only", candidate);
            return;
        }
        InventoryWorkspaceProfileId profileId = InventoryWorkspaceComposer.compose(
                dev.imagio.slot.inventory.session.InventorySessionSnapshot.create(
                        null,
                        host,
                        null,
                        null,
                        null,
                        java.util.List.of(),
                        "mount_preflight"
                )
        ).profileId();
        if (profileId != InventoryWorkspaceProfileId.CARRIED) {
            fail("profile_not_carried:" + profileId, candidate);
            return;
        }

        event.setNewScreen(new SlotWorkspaceScreen(observed));
        SlotDebugLog.log("Replacing {} with SLOT workspace shell", candidate.getClass().getName());
    }

    private static void fail(String reason, Screen candidate) {
        SlotDebugLog.log(
                "SLOT workspace fallback {} for {}",
                reason,
                candidate == null ? "null" : candidate.getClass().getName()
        );
    }
}
