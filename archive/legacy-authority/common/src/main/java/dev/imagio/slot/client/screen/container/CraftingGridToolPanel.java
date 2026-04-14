package dev.imagio.slot.client.screen.container;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.capability.ToolCapabilityDescriptor;
import dev.imagio.slot.client.intent.CraftingIntent;
import dev.imagio.slot.client.intent.IntentRouter;
import dev.imagio.slot.client.intent.ToolActionIntent;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.client.screen.SlotTooltipRenderer;
import dev.imagio.slot.storage.adapter.ExternalToolAction;
import dev.imagio.slot.storage.adapter.ExternalToolActionId;
import dev.imagio.slot.storage.adapter.ExternalToolSlotRegion;
import dev.imagio.slot.storage.adapter.ExternalToolSlotRole;
import dev.imagio.slot.storage.adapter.ExternalToolSpec;
import dev.imagio.slot.storage.adapter.ExternalToolToggle;
import dev.imagio.slot.storage.adapter.ExternalToolToggleId;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class CraftingGridToolPanel implements SlotBackedToolPanel {
    private static final int HEADER_HEIGHT = 11;
    private static final int HEADER_CONTROL_GAP = 6;
    private static final int HEADER_GRID_GAP = 3;
    private static final int SLOT_SIZE = 16;
    private static final int RESULT_GAP = 18;
    private static final int FRAME_PADDING_X = 4;
    private static final int FRAME_PADDING_Y = 2;
    private static final int CONTROL_HEIGHT = 10;
    private static final int CONTROL_PADDING_X = 3;
    private static final int CONTROL_GAP = 2;

    private final AbstractContainerMenu menu;
    private ExternalToolSpec tool;
    private ExternalToolSlotRegion inputRegion;
    private ExternalToolSlotRegion outputRegion;
    private Map<ExternalToolToggleId, Boolean> toggleStates = Map.of();
    private final Set<Integer> quickCraftVisitedSlots = new LinkedHashSet<>();

    private int boundsX;
    private int boundsY;
    private int boundsWidth = -1;
    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int gridOriginX;
    private int gridOriginY;
    private int pendingSlotClickId = -1;
    private int pendingSlotClickButton = -1;
    private boolean quickCraftDragging = false;
    private boolean refreshRequested = false;

    public CraftingGridToolPanel(AbstractContainerMenu menu, ToolCapabilityDescriptor descriptor) {
        this.menu = menu;
        applyDescriptor(Objects.requireNonNull(descriptor, "descriptor"));
    }

    boolean matches(AbstractContainerMenu menu, ToolCapabilityDescriptor descriptor) {
        return this.menu == menu
                && descriptor != null
                && descriptor.toolSpec() != null
                && descriptor.id().equals(tool.id());
    }

    dev.imagio.slot.storage.adapter.ExternalToolKind toolKind() {
        return tool.kind();
    }

    void updateToolDescriptor(ToolCapabilityDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        ExternalToolSpec updatedSpec = requireCraftingSpec(descriptor.toolSpec());
        if (!CraftingToolBindings.preservesPendingInteraction(tool, updatedSpec)) {
            clearPendingQuickCraft();
        }
        applyDescriptor(descriptor);
    }

    @Override
    public Component title() {
        return tool.title();
    }

    @Override
    public int preferredHeight() {
        return tool.preferredHeight();
    }

    @Override
    public void layout(int x, int y, int width) {
        boundsX = x;
        boundsY = y;
        boundsWidth = width;

        Font font = Minecraft.getInstance().font;
        int craftingWidth = (SLOT_SIZE * inputColumns()) + RESULT_GAP + SLOT_SIZE;
        int controlsWidth = controlRowWidth(font);
        int titleWidth = font.width(title());
        int headerWidth = titleWidth + (controlsWidth > 0 ? HEADER_CONTROL_GAP + controlsWidth : 0);

        panelWidth = Math.max(craftingWidth + (FRAME_PADDING_X * 2), headerWidth + (FRAME_PADDING_X * 2));
        panelHeight = (FRAME_PADDING_Y * 2) + HEADER_HEIGHT + HEADER_GRID_GAP + (SLOT_SIZE * inputRows());
        panelX = x + Math.max(0, (width - panelWidth) / 2);
        panelY = y + Math.max(0, (preferredHeight() - panelHeight) / 2);
        gridOriginX = panelX + Math.max(0, (panelWidth - craftingWidth) / 2);
        gridOriginY = panelY + FRAME_PADDING_Y + HEADER_HEIGHT + HEADER_GRID_GAP;
    }

    @Override
    public void containerTick() {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (boundsWidth <= 0 || panelWidth <= 0 || panelHeight <= 0) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        int panelBottom = panelY + panelHeight;
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelBottom, 0x6E181411);
        guiGraphics.fill(panelX + 1, panelY + 1, panelX + panelWidth - 1, panelBottom - 1, 0xB313100D);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0xA07A6232);
        guiGraphics.fill(panelX, panelBottom - 1, panelX + panelWidth, panelBottom, 0xA07A6232);
        guiGraphics.fill(panelX, panelY, panelX + 1, panelBottom, 0xA07A6232);
        guiGraphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelBottom, 0xA07A6232);
        guiGraphics.drawString(font, title(), panelX + 2, panelY + 1, 0xC8C8C8, false);

        List<ControlButton> controls = controlButtons(font);
        for (ControlButton control : controls) {
            renderControlButton(guiGraphics, font, control, control.contains(mouseX, mouseY));
        }

        int hoveredSlot = hoveredSlotId(mouseX, mouseY);
        for (int index = 0; index < inputSlots().size(); index++) {
            int slotId = inputSlots().get(index);
            renderInputSlot(guiGraphics, index, slotId, hoveredSlot == slotId);
        }

        if (resultSlotId() >= 0) {
            guiGraphics.drawString(font, ">", gridOriginX + (SLOT_SIZE * inputColumns()) + 8, resultSlotY() + 4, 0xA0A0A0, false);
            renderResultSlot(guiGraphics, resultSlotId(), hoveredSlot == resultSlotId());
        }

        if (hoveredSlot >= 0) {
            ItemStack stack = slotItem(hoveredSlot);
            if (!stack.isEmpty()) {
                Minecraft minecraft = Minecraft.getInstance();
                SlotTooltipRenderer.renderItemTooltip(
                        guiGraphics,
                        font,
                        stack,
                        mouseX,
                        mouseY,
                        minecraft.getWindow().getGuiScaledWidth(),
                        minecraft.getWindow().getGuiScaledHeight()
                );
            }
            return;
        }

        ControlButton hoveredControl = hoveredControl(mouseX, mouseY, controls);
        if (hoveredControl != null && !hoveredControl.tooltip().getString().isBlank()) {
            Minecraft minecraft = Minecraft.getInstance();
            SlotTooltipRenderer.renderTextTooltip(
                    guiGraphics,
                    font,
                    List.of(hoveredControl.tooltip()),
                    mouseX,
                    mouseY,
                    minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight()
            );
        }
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= panelX && mouseX < panelX + panelWidth && mouseY >= panelY && mouseY < panelY + panelHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) {
            return false;
        }

        ControlButton control = hoveredControl(mouseX, mouseY, controlButtons(Minecraft.getInstance().font));
        if (control != null) {
            return handleControlClick(control, button);
        }

        int slotId = slotAt(mouseX, mouseY);
        if (slotId < 0) {
            return false;
        }

        // Arm input-slot interactions on press so simple clicks can place on release,
        // while drag motions can still enter quick-craft distribution.
        if (isInputSlot(slotId) && !menu.getCarried().isEmpty()) {
            pendingSlotClickId = slotId;
            pendingSlotClickButton = button;
            quickCraftDragging = false;
            quickCraftVisitedSlots.clear();
            return true;
        }

        if (canStartQuickCraft(slotId, button)) {
            pendingSlotClickId = slotId;
            pendingSlotClickButton = button;
            quickCraftDragging = false;
            quickCraftVisitedSlots.clear();
            return true;
        }

        return performSlotClick(slotId, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (pendingSlotClickId < 0 || button != pendingSlotClickButton) {
            return false;
        }

        try {
            if (quickCraftDragging) {
                boolean requested = requestQuickCraftDistribution();
                if (requested) {
                    refreshRequested = true;
                }
                return requested;
            }
            return performSlotClick(pendingSlotClickId, pendingSlotClickButton);
        } finally {
            clearPendingQuickCraft();
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (pendingSlotClickId < 0 || button != pendingSlotClickButton) {
            return false;
        }
        if (!tool.supports(dev.imagio.slot.storage.adapter.ExternalToolCapability.DRAG_DISTRIBUTE) || menu.getCarried().isEmpty()) {
            return true;
        }

        if (!quickCraftDragging) {
            quickCraftDragging = true;
            addQuickCraftSlot(pendingSlotClickId);
        }

        int slotId = slotAt(mouseX, mouseY);
        if (slotId >= 0 && isInputSlot(slotId)) {
            addQuickCraftSlot(slotId);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0.0D || resultSlotId() < 0 || hoveredSlotId(mouseX, mouseY) != resultSlotId()) {
            return false;
        }

        int resultMenuSlotId = menuSlotId(resultSlotId());
        if (resolveMenuSlot(resultMenuSlotId) == null) {
            return false;
        }

        int repeats = Math.max(1, (int) Math.round(Math.abs(scrollY)));
        boolean requested = IntentRouter.route(
                CraftingIntent.ExtractResult.forCurrentSession(
                        menu.containerId,
                        MenuSlotId.of(resultMenuSlotId),
                        CraftingIntent.ResultAction.PICKUP,
                        0,
                        repeats
                )
        );
        refreshRequested |= requested;
        return requested;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    @Override
    public boolean consumeRefreshRequested() {
        boolean requested = refreshRequested;
        refreshRequested = false;
        return requested;
    }

    public int slotAt(double mouseX, double mouseY) {
        return hoveredSlotId(mouseX, mouseY);
    }

    public Slot menuSlot(int slotId) {
        return resolveMenuSlot(slotId);
    }

    @Override
    public ExternalToolSlotRegion regionForSlot(int slotId) {
        if (slotId < 0) {
            return null;
        }
        if (inputRegion.containsMenuSlot(slotId)) {
            return inputRegion;
        }
        if (outputRegion != null && outputRegion.containsMenuSlot(slotId)) {
            return outputRegion;
        }
        return null;
    }

    private boolean performSlotClick(int slotId, int button) {
        Minecraft minecraft = Minecraft.getInstance();
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        Slot slot = resolveMenuSlot(slotId);
        int menuSlotId = menuSlotId(slotId);
        if (minecraft.player == null || gameMode == null || slot == null || menuSlotId < 0) {
            SlotDebugLog.log(
                    "Crafting tool panel click ignored: menu={} slotId={} menuSlotId={} resolvedSlot={} carriedEmpty={}",
                    menu == null ? "<none>" : menu.getClass().getName(),
                    slotId,
                    menuSlotId,
                    slot != null,
                    menu == null || menu.getCarried().isEmpty()
            );
            return false;
        }

        if (isInputSlot(slotId) && !menu.getCarried().isEmpty()) {
            boolean requested = IntentRouter.route(
                    CraftingIntent.PlaceCursor.forCurrentSession(
                            menu.containerId,
                            MenuSlotId.of(menuSlotId),
                            ItemBehaviorPolicy.createIdentity(menu.getCarried()),
                            button == 1 ? CraftingIntent.CursorMode.ONE : CraftingIntent.CursorMode.STACK
                    )
            );
            SlotDebugLog.log(
                    "Crafting tool panel cursor place request: menu={} targetMenuSlot={} button={} carriedItem={} requested={} slotClass={} containerClass={}",
                    menu.getClass().getName(),
                    menuSlotId,
                    button,
                    menu.getCarried().getItem(),
                    requested,
                    slot.getClass().getName(),
                    slot.container == null ? "<none>" : slot.container.getClass().getName()
            );
            refreshRequested |= requested;
            return requested;
        }

        if (slotId == resultSlotId()) {
            boolean requested = IntentRouter.route(
                    CraftingIntent.ExtractResult.forCurrentSession(
                            menu.containerId,
                            MenuSlotId.of(menuSlotId),
                            Screen.hasShiftDown() ? CraftingIntent.ResultAction.QUICK_MOVE : CraftingIntent.ResultAction.PICKUP,
                            button,
                            1
                    )
            );
            refreshRequested |= requested;
            return requested;
        }

        ClickType clickType = Screen.hasShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP;
        gameMode.handleInventoryMouseClick(menu.containerId, menuSlotId, button, clickType, minecraft.player);
        refreshRequested = true;
        return true;
    }

    private boolean canStartQuickCraft(int slotId, int button) {
        if (button != 0 && button != 1) {
            return false;
        }
        if (Screen.hasShiftDown() || !tool.supports(dev.imagio.slot.storage.adapter.ExternalToolCapability.DRAG_DISTRIBUTE)) {
            return false;
        }
        if (menu.getCarried().isEmpty() || !isInputSlot(slotId)) {
            return false;
        }
        return acceptsPlacement(slotId, menu.getCarried());
    }

    private void addQuickCraftSlot(int slotId) {
        if (slotId < 0 || quickCraftVisitedSlots.contains(slotId)) {
            return;
        }

        Slot slot = resolveMenuSlot(slotId);
        ItemStack carried = menu.getCarried();
        if (slot == null
                || carried.isEmpty()
                || !AbstractContainerMenu.canItemQuickReplace(slot, carried, true)
                || !slot.mayPlace(carried)
                || !menu.canDragTo(slot)
                || carried.getCount() <= quickCraftVisitedSlots.size()) {
            return;
        }

        quickCraftVisitedSlots.add(slotId);
    }

    private boolean requestQuickCraftDistribution() {
        if (quickCraftVisitedSlots.isEmpty() || menu.getCarried().isEmpty()) {
            return false;
        }

        return IntentRouter.route(
                CraftingIntent.DistributeCursor.forCurrentSession(
                        menu.containerId,
                        quickCraftVisitedSlots.stream().map(MenuSlotId::of).toList(),
                        ItemBehaviorPolicy.createIdentity(menu.getCarried()),
                        pendingSlotClickButton == 1 ? CraftingIntent.CursorMode.ONE : CraftingIntent.CursorMode.STACK
                )
        );
    }

    private void clearPendingQuickCraft() {
        pendingSlotClickId = -1;
        pendingSlotClickButton = -1;
        quickCraftDragging = false;
        quickCraftVisitedSlots.clear();
    }

    private static ExternalToolSpec requireCraftingSpec(ExternalToolSpec spec) {
        if (spec == null || spec.kind() != dev.imagio.slot.storage.adapter.ExternalToolKind.CRAFTING_GRID) {
            throw new IllegalArgumentException("Crafting grid tool spec is required");
        }
        return Objects.requireNonNull(spec, "spec");
    }

    private boolean handleControlClick(ControlButton control, int button) {
        if (control.action() != null) {
            if (button != 0 && !(control.action().id() == ExternalToolActionId.ROTATE_GRID && button == 1)) {
                return false;
            }
            ToolActionIntent.Action requestedAction = switch (control.action().id()) {
                case CLEAR_GRID -> ToolActionIntent.Action.CLEAR_GRID;
                case BALANCE_GRID -> ToolActionIntent.Action.BALANCE_GRID;
                case ROTATE_GRID -> button == 1 ? ToolActionIntent.Action.ROTATE_GRID_CCW : ToolActionIntent.Action.ROTATE_GRID_CW;
            };
            boolean requested = IntentRouter.route(ToolActionIntent.forCurrentSession(menu.containerId, tool.id(), requestedAction));
            refreshRequested |= requested;
            return requested;
        }

        if (control.toggle() != null && button == 0) {
            boolean requested = IntentRouter.route(
                    ToolActionIntent.forCurrentSession(menu.containerId, tool.id(), ToolActionIntent.Action.TOGGLE_AUTO_REFILL)
            );
            refreshRequested |= requested;
            return requested;
        }

        return false;
    }

    private boolean isInputSlot(int slotId) {
        return regionForSlot(slotId) != null && regionForSlot(slotId).role() == ExternalToolSlotRole.INPUT;
    }

    private void renderInputSlot(GuiGraphics guiGraphics, int gridIndex, int slotId, boolean hovered) {
        int slotX = gridOriginX + ((gridIndex % inputColumns()) * SLOT_SIZE);
        int slotY = gridOriginY + ((gridIndex / inputColumns()) * SLOT_SIZE);
        renderSlot(guiGraphics, slotId, slotX, slotY, hovered, false);
    }

    private void renderResultSlot(GuiGraphics guiGraphics, int slotId, boolean hovered) {
        int slotX = gridOriginX + (SLOT_SIZE * inputColumns()) + RESULT_GAP;
        int slotY = resultSlotY();
        renderSlot(guiGraphics, slotId, slotX, slotY, hovered, true);
    }

    private void renderSlot(GuiGraphics guiGraphics, int slotId, int slotX, int slotY, boolean hovered, boolean resultSlot) {
        int outer = hovered ? 0xD0A88444 : resultSlot ? 0xC0895D2F : 0xB0674D28;
        guiGraphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, outer);
        guiGraphics.fill(slotX + 1, slotY + 1, slotX + SLOT_SIZE - 1, slotY + SLOT_SIZE - 1, 0xD91C1816);

        ItemStack stack = slotItem(slotId);
        if (!stack.isEmpty()) {
            guiGraphics.renderItem(stack, slotX + 1, slotY + 1);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, slotX + 1, slotY + 1);
        }
    }

    private void renderControlButton(GuiGraphics guiGraphics, Font font, ControlButton control, boolean hovered) {
        int frame = hovered ? 0xC08C7040 : 0xA05A4626;
        int fill = hovered ? 0xD0211810 : 0xB018120D;
        guiGraphics.fill(control.left(), control.top(), control.right(), control.bottom(), frame);
        guiGraphics.fill(control.left() + 1, control.top() + 1, control.right() - 1, control.bottom() - 1, fill);
        guiGraphics.drawString(font, control.label(), control.left() + CONTROL_PADDING_X, control.top() + 1, 0xD8D8D8, false);
    }

    private List<ControlButton> controlButtons(Font font) {
        List<ControlDescriptor> descriptors = new ArrayList<>();
        for (ExternalToolAction action : tool.actions()) {
            descriptors.add(ControlDescriptor.action(action, action.label(), action.tooltip()));
        }
        for (ExternalToolToggle toggle : tool.toggles()) {
            boolean enabled = toggleEnabled(toggle.id());
            descriptors.add(ControlDescriptor.toggle(toggle, toggle.label(enabled), toggle.tooltip(enabled)));
        }

        if (descriptors.isEmpty()) {
            return List.of();
        }

        int totalWidth = 0;
        for (int index = 0; index < descriptors.size(); index++) {
            if (index > 0) {
                totalWidth += CONTROL_GAP;
            }
            totalWidth += controlWidth(font, descriptors.get(index).label());
        }

        int x = panelX + panelWidth - FRAME_PADDING_X - totalWidth;
        int y = panelY + 1;
        List<ControlButton> buttons = new ArrayList<>(descriptors.size());
        for (ControlDescriptor descriptor : descriptors) {
            int width = controlWidth(font, descriptor.label());
            buttons.add(new ControlButton(
                    x,
                    y,
                    width,
                    CONTROL_HEIGHT,
                    descriptor.action(),
                    descriptor.toggle(),
                    descriptor.label(),
                    descriptor.tooltip()
            ));
            x += width + CONTROL_GAP;
        }
        return List.copyOf(buttons);
    }

    private int controlRowWidth(Font font) {
        int width = 0;
        List<ControlButton> controls = controlButtons(font);
        for (int index = 0; index < controls.size(); index++) {
            if (index > 0) {
                width += CONTROL_GAP;
            }
            width += controls.get(index).width();
        }
        return width;
    }

    private static int controlWidth(Font font, Component label) {
        return Math.max(18, font.width(label) + (CONTROL_PADDING_X * 2));
    }

    private boolean toggleEnabled(ExternalToolToggleId toggleId) {
        return toggleId != null && Boolean.TRUE.equals(toggleStates.get(toggleId));
    }

    private void applyDescriptor(ToolCapabilityDescriptor descriptor) {
        tool = requireCraftingSpec(descriptor.toolSpec());
        toggleStates = descriptor.toggleStates();
        inputRegion = CraftingToolBindings.requiredRegion(tool, ExternalToolSlotRole.INPUT);
        outputRegion = CraftingToolBindings.optionalRegion(tool, ExternalToolSlotRole.OUTPUT);
    }

    private ControlButton hoveredControl(double mouseX, double mouseY, List<ControlButton> controls) {
        for (ControlButton control : controls) {
            if (control.contains(mouseX, mouseY)) {
                return control;
            }
        }
        return null;
    }

    private int hoveredSlotId(double mouseX, double mouseY) {
        for (int index = 0; index < inputSlots().size(); index++) {
            int slotX = gridOriginX + ((index % inputColumns()) * SLOT_SIZE);
            int slotY = gridOriginY + ((index / inputColumns()) * SLOT_SIZE);
            if (containsSlot(mouseX, mouseY, slotX, slotY)) {
                return inputSlots().get(index);
            }
        }

        if (resultSlotId() >= 0) {
            int resultX = gridOriginX + (SLOT_SIZE * inputColumns()) + RESULT_GAP;
            int resultY = resultSlotY();
            if (containsSlot(mouseX, mouseY, resultX, resultY)) {
                return resultSlotId();
            }
        }

        return -1;
    }

    private static boolean containsSlot(double mouseX, double mouseY, int slotX, int slotY) {
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE;
    }

    private List<Integer> inputSlots() {
        return inputRegion.menuSlots();
    }

    private int inputColumns() {
        return Math.max(1, inputRegion.columns());
    }

    private int inputRows() {
        return Math.max(1, (inputSlots().size() + inputColumns() - 1) / inputColumns());
    }

    private int resultSlotY() {
        return gridOriginY + Math.max(0, ((inputRows() - 1) * SLOT_SIZE) / 2);
    }

    private int resultSlotId() {
        return outputRegion == null || outputRegion.menuSlots().isEmpty() ? -1 : outputRegion.menuSlots().getFirst();
    }

    private ItemStack slotItem(int slotId) {
        Slot slot = resolveMenuSlot(slotId);
        return slot == null ? ItemStack.EMPTY : slot.getItem();
    }

    private Slot resolveMenuSlot(int slotId) {
        if (menu == null || slotId < 0) {
            return null;
        }
        try {
            Slot slot = menu.getSlot(slotId);
            return slot;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private record ControlDescriptor(
            ExternalToolAction action,
            ExternalToolToggle toggle,
            Component label,
            Component tooltip
    ) {
        private static ControlDescriptor action(ExternalToolAction action, Component label, Component tooltip) {
            return new ControlDescriptor(action, null, label, tooltip);
        }

        private static ControlDescriptor toggle(ExternalToolToggle toggle, Component label, Component tooltip) {
            return new ControlDescriptor(null, toggle, label, tooltip);
        }
    }

    private record ControlButton(
            int left,
            int top,
            int width,
            int height,
            ExternalToolAction action,
            ExternalToolToggle toggle,
            Component label,
            Component tooltip
    ) {
        private int right() {
            return left + width;
        }

        private int bottom() {
            return top + height;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= left && mouseX < right() && mouseY >= top && mouseY < bottom();
        }
    }
}
