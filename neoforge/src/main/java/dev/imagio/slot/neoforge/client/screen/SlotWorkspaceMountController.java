package dev.imagio.slot.neoforge.client.screen;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceComposer;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceProfileId;
import dev.imagio.slot.neoforge.client.host.ObservedScreenContext;
import dev.imagio.slot.neoforge.client.host.ObservedScreenContexts;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.network.SlotWorkspaceOpenPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SlotWorkspaceMountController {
    private static boolean registered;
    // One-shot gate: when true, the next InventoryScreen.Opening is allowed
    // through to vanilla instead of being redirected to the SLOT workspace.
    // Used by the "open vanilla inventory" button/hotkey to escape to the
    // plain inventory screen when needed (debugging, compat checks, etc.).
    private static boolean bypassNextMount;

    private SlotWorkspaceMountController() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(SlotWorkspaceMountController::onScreenOpening);
        registered = true;
    }

    public static void openVanillaInventory() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        // setScreen() alone doesn't send a ServerboundContainerClosePacket for
        // the active ModularUI menu; the server can be left with the SLOT
        // workspace menu as the player's containerMenu while the client shows
        // vanilla InventoryScreen. Slot clicks in the vanilla UI then resolve
        // locally but miss the server, producing silent desync (items appear
        // to move but revert on relog). closeContainer() on the client sends
        // the close packet and resets the local containerMenu reference
        // before we show the vanilla screen.
        if (minecraft.player.containerMenu != minecraft.player.inventoryMenu) {
            minecraft.player.closeContainer();
        }
        bypassNextMount = true;
        minecraft.setScreen(new InventoryScreen(minecraft.player));
    }

    public static void openSlotWorkspace() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        if (minecraft.player.containerMenu != minecraft.player.inventoryMenu) {
            minecraft.player.closeContainer();
        }
        minecraft.setScreen(new InventoryScreen(minecraft.player));
    }

    private static void onScreenOpening(ScreenEvent.Opening event) {
        Screen candidate = event.getNewScreen();
        if (!(candidate instanceof AbstractContainerScreen<?> containerScreen)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            fail("missing_client_or_player", candidate);
            return;
        }
        if (!(candidate instanceof InventoryScreen)) {
            fail("only_player_inventory_enabled_for_first_slice", candidate);
            return;
        }
        if (bypassNextMount) {
            bypassNextMount = false;
            SlotDebugLog.log("Vanilla inventory bypass requested; skipping SLOT mount for {}", candidate.getClass().getName());
            return;
        }
        // Persistent escape hatch: when SLOT is disabled the player gets
        // the vanilla inventory screen on every E-press until they
        // explicitly re-enable from the vanilla inventory's "Re-enable
        // SLOT" pill.
        if (!SlotClientConfig.CLIENT.slotEnabled.get()) {
            SlotDebugLog.log("SLOT disabled in client config; passing through to {}", candidate.getClass().getName());
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
        PacketDistributor.sendToServer(new SlotWorkspaceOpenPayload());
        event.setCanceled(true);
        SlotDebugLog.log("Requesting SLOT LDLib workspace menu for {}", candidate.getClass().getName());
    }

    private static void fail(String reason, Screen candidate) {
        SlotDebugLog.log(
                "SLOT workspace fallback {} for {}",
                reason,
                candidate == null ? "null" : candidate.getClass().getName()
        );
    }
}
