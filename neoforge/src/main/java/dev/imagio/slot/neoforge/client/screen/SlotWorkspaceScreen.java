package dev.imagio.slot.neoforge.client.screen;

import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryCommandId;
import dev.imagio.slot.inventory.browse.InventoryBrowseEntry;
import dev.imagio.slot.inventory.browse.InventoryBrowsePane;
import dev.imagio.slot.inventory.browse.InventoryBrowseSection;
import dev.imagio.slot.inventory.browse.InventoryBrowseSubjectRef;
import dev.imagio.slot.inventory.session.InventorySessionCoordinator;
import dev.imagio.slot.inventory.session.InventorySessionSnapshot;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceModel;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceSurface;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceSurfaceKind;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceZone;
import dev.imagio.slot.inventory.workspace.InventoryWorkspaceZoneKind;
import dev.imagio.slot.neoforge.client.SlotNeoForgeClient;
import dev.imagio.slot.neoforge.client.host.ObservedScreenContext;
import dev.imagio.slot.neoforge.client.host.ObservedScreenContextProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class SlotWorkspaceScreen extends AbstractContainerScreen<AbstractContainerMenu> implements ObservedScreenContextProvider {
    private static final int BACKGROUND = 0xF011151A;
    private static final int PANEL = 0xF0222830;
    private static final int PANEL_ALT = 0xF02E3740;
    private static final int ACCENT = 0xFF7AC7A7;
    private static final int TEXT = 0xFFE8ECEF;
    private static final int MUTED = 0xFF9AA4AE;
    private static final int WARNING = 0xFFFFC66D;

    private final ObservedScreenContext observedContext;
    private final SlotWorkspaceInputMapper inputMapper;
    private final List<RowHit> rowHits = new ArrayList<>();
    private int tickCounter;
    private int scrollOffset;

    public SlotWorkspaceScreen(ObservedScreenContext observedContext) {
        super(
                observedContext.menu(),
                observedContext.playerInventory(),
                observedContext.title()
        );
        this.observedContext = observedContext;
        this.inputMapper = new SlotWorkspaceInputMapper(
                SlotNeoForgeClient.sessionCoordinator(),
                SlotNeoForgeClient.intentRouter()
        );
    }

    @Override
    public ObservedScreenContext observedScreenContext() {
        return observedContext;
    }

    @Override
    protected void init() {
        super.init();
        this.imageWidth = Math.min(420, Math.max(240, this.width - 48));
        this.imageHeight = Math.min(260, Math.max(180, this.height - 48));
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        refresh("slot_workspace.init");
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        tickCounter++;
        if (tickCounter % 5 == 0) {
            refresh("slot_workspace.tick");
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        rowHits.clear();
        InventorySessionSnapshot snapshot = snapshot();
        InventoryWorkspaceModel workspace = snapshot.workspaceModel();
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        guiGraphics.fill(0, 0, this.width, this.height, BACKGROUND);
        guiGraphics.fill(x, y, x + w, y + h, PANEL);
        guiGraphics.fill(x, y, x + w, y + 22, PANEL_ALT);
        guiGraphics.drawString(this.font, this.title, x + 10, y + 7, TEXT, false);
        guiGraphics.drawString(this.font, workspace.profileId().name().toLowerCase(), x + w - 110, y + 7, MUTED, false);

        int sideWidth = Math.min(128, Math.max(96, w / 4));
        renderBrowse(guiGraphics, workspace, x + 10, y + 32, w - sideWidth - 26, h - 44, mouseX, mouseY);
        renderSupportRail(guiGraphics, workspace, x + w - sideWidth - 8, y + 32, sideWidth, h - 44);
        renderStatus(guiGraphics, workspace, x + 10, y + h - 14);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        RowHit hit = rowHit(mouseX, mouseY);
        if (hit != null) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT || Screen.hasShiftDown()) {
                return inputMapper.invokeBrowseCommand(
                        hit.subjectRef(),
                        InventoryCommandId.TRANSFER_STACK,
                        InventoryActionMode.EXECUTE,
                        "slot_workspace.row_transfer"
                );
            }
            return inputMapper.selectBrowseSubject(hit.subjectRef(), "slot_workspace.row_select");
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!rowHits.isEmpty() || inside(mouseX, mouseY, leftPos, topPos, imageWidth, imageHeight)) {
            scrollOffset = Math.max(0, scrollOffset - (int) Math.signum(scrollY) * 12);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InventoryBrowseSubjectRef selected = snapshot().browseDocument().sessionState().selectedSubject();
        if (selected != null && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            return inputMapper.invokeBrowseCommand(
                    selected,
                    InventoryCommandId.TRANSFER_STACK,
                    InventoryActionMode.EXECUTE,
                    "slot_workspace.key_transfer"
            );
        }
        if (selected != null && keyCode == GLFW.GLFW_KEY_F) {
            return inputMapper.invokeBrowseCommand(
                    selected,
                    InventoryCommandId.TOGGLE_FAVORITE,
                    InventoryActionMode.EXECUTE,
                    "slot_workspace.key_favorite"
            );
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE
                || (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode))) {
            this.onClose();
            return true;
        }
        return true;
    }

    private InventorySessionSnapshot refresh(String origin) {
        InventorySessionCoordinator coordinator = SlotNeoForgeClient.sessionCoordinator();
        return coordinator == null ? InventorySessionSnapshot.empty() : coordinator.refresh(origin);
    }

    private InventorySessionSnapshot snapshot() {
        InventorySessionCoordinator coordinator = SlotNeoForgeClient.sessionCoordinator();
        return coordinator == null ? InventorySessionSnapshot.empty() : coordinator.snapshot();
    }

    private void renderBrowse(
            GuiGraphics guiGraphics,
            InventoryWorkspaceModel workspace,
            int x,
            int y,
            int w,
            int h,
            int mouseX,
            int mouseY
    ) {
        guiGraphics.fill(x, y, x + w, y + h, 0xD91B2027);
        InventoryWorkspaceSurface.BrowsePaneSurface browseSurface = browseSurface(workspace.zone(InventoryWorkspaceZoneKind.PRIMARY_BROWSE));
        if (browseSurface == null || browseSurface.pane() == null) {
            guiGraphics.drawString(this.font, "No browse surface", x + 8, y + 8, WARNING, false);
            return;
        }

        InventoryBrowsePane pane = browseSurface.pane();
        String paneTitle = pane.paneMembership().name().toLowerCase();
        guiGraphics.drawString(this.font, paneTitle, x + 8, y + 8, ACCENT, false);
        int rowY = y + 24 - scrollOffset;
        int visibleTop = y + 20;
        int visibleBottom = y + h - 8;
        for (InventoryBrowseSection section : pane.sections()) {
            if (rowY >= visibleTop && rowY <= visibleBottom) {
                guiGraphics.drawString(this.font, section.title(), x + 8, rowY, MUTED, false);
            }
            rowY += 13;
            if (!section.expanded()) {
                continue;
            }
            for (InventoryBrowseEntry entry : section.entries()) {
                if (rowY >= visibleTop && rowY <= visibleBottom) {
                    boolean hovered = inside(mouseX, mouseY, x + 4, rowY - 2, w - 8, 12);
                    boolean selected = selected(entry);
                    int rowColor = selected ? 0x663B7D64 : hovered ? 0x443B4650 : 0x00000000;
                    if (rowColor != 0) {
                        guiGraphics.fill(x + 4, rowY - 2, x + w - 4, rowY + 10, rowColor);
                    }
                    guiGraphics.drawString(this.font, entryLabel(entry), x + 10, rowY, selected ? ACCENT : TEXT, false);
                    rowHits.add(new RowHit(x + 4, rowY - 2, w - 8, 12, entry.subjectRef()));
                }
                rowY += 12;
            }
        }
    }

    private void renderSupportRail(GuiGraphics guiGraphics, InventoryWorkspaceModel workspace, int x, int y, int w, int h) {
        guiGraphics.fill(x, y, x + w, y + h, 0xD91B2027);
        int rowY = y + 8;
        guiGraphics.drawString(this.font, "Support", x + 8, rowY, ACCENT, false);
        rowY += 16;
        rowY = renderZoneSummary(guiGraphics, workspace.zone(InventoryWorkspaceZoneKind.QUICK_ACCESS), "Quick", x, rowY);
        rowY = renderZoneSummary(guiGraphics, workspace.zone(InventoryWorkspaceZoneKind.EQUIPMENT), "Gear", x, rowY);
        rowY = renderZoneSummary(guiGraphics, workspace.zone(InventoryWorkspaceZoneKind.WORKFLOW_RAIL), "Workflow", x, rowY);
        renderZoneSummary(guiGraphics, workspace.zone(InventoryWorkspaceZoneKind.TOOL_DOCK), "Tools", x, rowY);
    }

    private int renderZoneSummary(GuiGraphics guiGraphics, InventoryWorkspaceZone zone, String label, int x, int rowY) {
        int count = zone == null ? 0 : zone.surfaces().size();
        guiGraphics.drawString(this.font, label + ": " + count, x + 8, rowY, count == 0 ? MUTED : TEXT, false);
        return rowY + 13;
    }

    private void renderStatus(GuiGraphics guiGraphics, InventoryWorkspaceModel workspace, int x, int y) {
        String status = "pending " + workspace.status().pendingActionCount();
        if (workspace.status().craftingPresent()) {
            status += "  crafting";
        }
        if (!workspace.status().diagnostics().isEmpty()) {
            status += "  " + workspace.status().diagnostics().getFirst();
        }
        guiGraphics.drawString(this.font, status, x, y, MUTED, false);
    }

    private InventoryWorkspaceSurface.BrowsePaneSurface browseSurface(InventoryWorkspaceZone zone) {
        if (zone == null) {
            return null;
        }
        return zone.surfaces().stream()
                .filter(InventoryWorkspaceSurface.BrowsePaneSurface.class::isInstance)
                .map(InventoryWorkspaceSurface.BrowsePaneSurface.class::cast)
                .findFirst()
                .orElse(null);
    }

    private boolean selected(InventoryBrowseEntry entry) {
        return switch (entry) {
            case InventoryBrowseEntry.ItemEntry itemEntry -> itemEntry.selected();
            case InventoryBrowseEntry.PlaceholderEntry placeholderEntry -> placeholderEntry.selected();
            case InventoryBrowseEntry.LoadoutEntry loadoutEntry -> loadoutEntry.selected();
        };
    }

    private String entryLabel(InventoryBrowseEntry entry) {
        return switch (entry) {
            case InventoryBrowseEntry.ItemEntry itemEntry -> itemEntry.row().identity().itemId()
                    + " x" + itemEntry.row().visibleTotalCount();
            case InventoryBrowseEntry.PlaceholderEntry placeholderEntry -> placeholderEntry.identity().itemId() + " (missing)";
            case InventoryBrowseEntry.LoadoutEntry loadoutEntry -> "Loadout: " + loadoutEntry.loadout().name();
        };
    }

    private RowHit rowHit(double mouseX, double mouseY) {
        return rowHits.stream()
                .filter(hit -> inside(mouseX, mouseY, hit.x(), hit.y(), hit.width(), hit.height()))
                .findFirst()
                .orElse(null);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private record RowHit(int x, int y, int width, int height, InventoryBrowseSubjectRef subjectRef) {
    }
}
