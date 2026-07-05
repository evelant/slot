package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.holder.IModularUIHolderMenu;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WorkspaceCommandOutcome;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Server-side per-player handle for an active SLOT sidebar. Owns a
 * {@link SlotWorkspaceUiSession} + {@link ModularUI} built in
 * {@link WorkspaceLayoutMode#SIDEBAR} mode and attaches the modular UI
 * to whichever {@link AbstractContainerMenu} the player currently has
 * open (chest, crafting, machine, …) via LDLib2's existing
 * {@link IModularUIHolderMenu} mixin.
 *
 * <p><strong>Why attach to the host menu.</strong> {@code PacketModularUISync}
 * routes inbound sync packets through {@code player.containerMenu
 * instanceof IUISyncManagerHolder}. With the chest/crafting/machine
 * menu being the player's container, the only way for the client's
 * RPCs and server's view-model pushes to reach each other is for the
 * host menu to expose our sidebar's sync manager. LDLib2's
 * {@code AbstractContainerMenuMixin} already implements
 * {@code IModularUIHolderMenu} on every menu — calling
 * {@code setModularUI(...)} switches the carrier. As a side effect,
 * vanilla's {@code broadcastChanges()} (called every server tick on
 * the active menu) routes through the same mixin and ticks our
 * modular UI for free, so we don't need a separate per-player tick
 * loop.
 *
 * <p>The host menu's gameplay logic (chest contents, crafting matrix,
 * machine slots) is unaffected — the holder mixin just stores a
 * reference and does not touch slot or item handling.
 */
public final class SlotSidebarUiHandle {
    private static final Set<ModularUI> SIDEBAR_UIS = Collections.synchronizedSet(
            Collections.newSetFromMap(new WeakHashMap<>()));

    private final ServerPlayer player;
    private final SlotWorkspaceUiSession session;
    private final ModularUI modularUI;
    private final AbstractContainerMenu hostMenu;
    private boolean disposed;

    SlotSidebarUiHandle(ServerPlayer player) {
        this.player = player;
        this.session = new SlotWorkspaceUiSession(player);
        this.modularUI = SlotWorkspaceUiFactory.create(session, player);
        SIDEBAR_UIS.add(modularUI);
        this.hostMenu = player.containerMenu;
        attachToHostMenu();
    }

    public ServerPlayer player() {
        return player;
    }

    public SlotWorkspaceViewModel currentViewModel() {
        return session.currentViewModel();
    }

    void applyExternalOutcome(WorkspaceCommandOutcome outcome) {
        if (!disposed) {
            session.applyExternalOutcome(player, outcome);
        }
    }

    private void attachToHostMenu() {
        if (hostMenu instanceof IModularUIHolderMenu holder) {
            // If the host already had its own ModularUI (i.e. this is an
            // LDLib2-driven menu), we'd clobber it — bail. This shouldn't
            // happen because SlotContainerSidebar excludes LDLib2 / SLOT
            // screens, but the guard is cheap. A stale previous SLOT
            // sidebar can remain on the same menu while EMI swaps from the
            // handled screen to RecipeScreen; replacing that one is safe.
            ModularUI existing = holder.getModularUI();
            if (existing != null && existing != modularUI && !SIDEBAR_UIS.contains(existing)) {
                SlotDebugLog.log(
                        "[SLOT][sidebar] host menu {} already carries a ModularUI; not attaching",
                        hostMenu.getClass().getName()
                );
                return;
            }
            holder.setModularUI(modularUI);
        }
    }

    void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        // No active detach from the host menu: AbstractContainerMenuMixin's
        // setModularUI(null) NPEs (it always calls modularUI.setMenu(...)),
        // and the host menu is about to be replaced anyway when the player
        // closes the host screen. The stale holder field on the dead host
        // menu won't be reachable once the menu is GC'd.
        modularUI.onRemoved();
    }

    boolean isDisposed() {
        return disposed;
    }
}
