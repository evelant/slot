package dev.imagio.slot.neoforge.client;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.neoforge.client.input.SlotAtlasKeyMappings;
import dev.imagio.slot.neoforge.client.wayfinding.WayfindingChestGlowRenderer;
import dev.imagio.slot.neoforge.client.wayfinding.WayfindingHudRenderer;
import dev.imagio.slot.neoforge.config.SlotClientConfig;
import dev.imagio.slot.neoforge.client.screen.SlotContainerSidebar;
import dev.imagio.slot.neoforge.client.screen.SlotReenableButton;
import dev.imagio.slot.neoforge.client.screen.SlotWorkspaceMountController;
import dev.imagio.slot.neoforge.network.SlotGatherActiveKitPayload;
import dev.imagio.slot.neoforge.network.SlotKitPageCyclePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SlotNeoForgeClient {
    private static boolean setupListenerRegistered;
    private static boolean runtimeInitialized;

    private SlotNeoForgeClient() {
    }

    public static void init(IEventBus modBus) {
        if (setupListenerRegistered) {
            return;
        }
        modBus.addListener(SlotNeoForgeClient::onClientSetup);
        modBus.addListener(SlotAtlasKeyMappings::register);
        setupListenerRegistered = true;
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        if (runtimeInitialized) {
            return;
        }
        SlotDebugLog.setEnabledSupplier(() -> SlotClientConfig.CLIENT.debugLogging.get());
        SlotDebugLog.setVerboseSupplier(() -> SlotClientConfig.CLIENT.verboseLogging.get());
        SlotWorkspaceMountController.init();
        SlotReenableButton.init();
        SlotContainerSidebar.init();
        NeoForge.EVENT_BUS.addListener(SlotNeoForgeClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, WayfindingChestGlowRenderer::onRenderLevelStage);
        NeoForge.EVENT_BUS.addListener(RenderGuiEvent.Post.class, WayfindingHudRenderer::onRenderGui);
        runtimeInitialized = true;
    }

    // Drain every queued click on the "Open Vanilla Inventory" mapping each
    // tick; each consumeClick() returns true once per press, so a loop
    // handles held/repeated presses without re-firing.
    private static void onClientTick(ClientTickEvent.Post event) {
        while (SlotAtlasKeyMappings.openVanillaInventoryMapping().consumeClick()) {
            SlotWorkspaceMountController.openVanillaInventory();
        }
        // In-world kit-page cycle. The SLOT atlas's HotkeyRouter handles
        // the same key when the workspace screen is mounted; this loop
        // covers the in-world case so the player can swap kit pages
        // without first opening the inventory. Skip while any GUI screen
        // is open — consumeClick fires for both, but we only want this
        // path for the no-screen case (the screen path drives RPC).
        Minecraft client = Minecraft.getInstance();
        Screen current = client == null ? null : client.screen;
        while (SlotAtlasKeyMappings.cycleKitPageMapping().consumeClick()) {
            if (current != null) {
                continue;
            }
            int direction = Screen.hasShiftDown() ? -1 : 1;
            PacketDistributor.sendToServer(new SlotKitPageCyclePayload(direction));
        }
        // Gather hotkey. Works in or out of the SLOT atlas;
        // when the atlas is open, HotkeyRouter intercepts the key event
        // and sends the same packet, so this loop covers the in-world
        // case (no screen) and skips otherwise to avoid double-firing.
        while (SlotAtlasKeyMappings.gatherActiveKitMapping().consumeClick()) {
            if (current != null) {
                continue;
            }
            PacketDistributor.sendToServer(new SlotGatherActiveKitPayload());
        }
    }
}
