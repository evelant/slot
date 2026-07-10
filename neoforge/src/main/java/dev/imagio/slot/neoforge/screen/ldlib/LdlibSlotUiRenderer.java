package dev.imagio.slot.neoforge.screen.ldlib;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventBinding;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class LdlibSlotUiRenderer {
    interface InteractionBridge {
        void afterRender(SlotUiElement model, UIElement element);
    }

    private final InteractionBridge bridge;

    LdlibSlotUiRenderer(InteractionBridge bridge) {
        this.bridge = bridge == null ? (model, element) -> { } : bridge;
    }

    UIElement render(SlotUiElement model) {
        if (model == null) {
            return new UIElement();
        }
        UIElement element = createElement(model);
        if (model.id() != null && !model.id().isBlank()) {
            element.setId(model.id());
        }
        applyLayout(element, model.layout());
        applyMutableState(element, model);
        registerEvents(element, model);
        for (SlotUiElement child : model.children()) {
            element.addChild(render(child));
        }
        element.setAllowHitTest(model.allowHitTest());
        bridge.afterRender(model, element);
        return element;
    }

    private UIElement createElement(SlotUiElement model) {
        switch (model.kind()) {
            case BUTTON:
                Button button = WorkspaceUi.button(model.text(), model.buttonActive(), model.buttonColor());
                button.setText(textComponent(model.text()));
                if (!model.buttonHasText()) {
                    button.noText();
                }
                applyTextStyle(button, model.textStyle());
                return button;
            case LABEL:
                Label label = WorkspaceUi.label(model.text(), model.textStyle().color());
                label.setText(textComponent(model.text()));
                applyTextStyle(label, model.textStyle());
                return label;
            case ITEM_ICON:
                return WorkspaceUi.itemIcon(
                        model.itemStack(),
                        model.itemIconSize(),
                        model.itemCarried(),
                        model.renderVanillaCount()
                );
            case FLUID_ICON:
                return WorkspaceUi.fluidIcon(
                        model.fluidId(),
                        model.itemIconSize(),
                        model.itemCarried()
                );
            case ELEMENT:
            default:
                return new UIElement();
        }
    }

    private void applyLayout(UIElement element, SlotUiLayout layout) {
        element.layout(style -> {
            if (layout.hasWidth()) {
                style.width(layout.width());
            }
            if (layout.hasHeight()) {
                style.height(layout.height());
            }
            if (layout.hasWidthPercent()) {
                style.widthPercent(layout.widthPercent());
            }
            if (layout.hasHeightPercent()) {
                style.heightPercent(layout.heightPercent());
            }
            if (layout.hasMaxWidth()) {
                style.maxWidth(layout.maxWidth());
            }
            if (layout.hasFlex()) {
                style.flex(layout.flex());
            }
            if (layout.hasPaddingAll()) {
                style.paddingAll(layout.paddingAll());
            }
            if (layout.hasPaddingHorizontal()) {
                style.paddingHorizontal(layout.paddingHorizontal());
            }
            if (layout.hasPaddingVertical()) {
                style.paddingVertical(layout.paddingVertical());
            }
            if (layout.hasPaddingLeft()) {
                style.paddingLeft(layout.paddingLeft());
            }
            if (layout.hasPaddingRight()) {
                style.paddingRight(layout.paddingRight());
            }
            if (layout.hasGapAll()) {
                style.gapAll(layout.gapAll());
            }
            if (layout.flexDirection() != null) {
                style.flexDirection(map(layout.flexDirection()));
            }
            if (layout.alignItems() != null) {
                style.alignItems(map(layout.alignItems()));
            }
            if (layout.alignContent() != null) {
                style.alignContent(map(layout.alignContent()));
            }
            if (layout.flexWrap() != null) {
                style.flexWrap(map(layout.flexWrap()));
            }
            if (layout.positionType() != null) {
                style.positionType(map(layout.positionType()));
            }
            if (layout.hasLeft()) {
                style.left(layout.left());
            }
            if (layout.hasRight()) {
                style.right(layout.right());
            }
            if (layout.hasTop()) {
                style.top(layout.top());
            }
            if (layout.hasBottom()) {
                style.bottom(layout.bottom());
            }
        });
    }

    private void applyMutableState(UIElement element, SlotUiElement model) {
        if (element instanceof Button button) {
            button.setActive(model.buttonActive());
            button.setText(textComponent(model.text()));
            WorkspaceUi.applyButtonColors(button, model.buttonActive(), model.buttonColor());
            applyTextStyle(button, model.textStyle());
        }
        if (element instanceof Label label) {
            label.setText(textComponent(model.text()));
        }
        element.style(style -> {
            if (model.backgroundColor() != null) {
                style.backgroundTexture(WorkspaceUi.rect(model.backgroundColor()));
            }
            style.overlayTexture(model.overlayColor() == null
                    ? IGuiTexture.EMPTY
                    : WorkspaceUi.rect(model.overlayColor()));
            if (model.zIndex() != null) {
                style.zIndex(model.zIndex());
            }
        });
    }

    private void applyTextStyle(Label label, SlotUiTextStyle textStyle) {
        label.textStyle(style -> style
                .font(WorkspaceTheme.fontUi())
                .textColor(textStyle.color())
                .fontSize(textStyle.fontSize())
                .textShadow(textStyle.shadow())
                .adaptiveWidth(textStyle.adaptiveWidth())
                .textAlignHorizontal(map(textStyle.horizontal()))
                .textAlignVertical(map(textStyle.vertical())));
    }

    private static Component textComponent(String text) {
        return Component.literal(text == null ? "" : text)
                .withStyle(style -> style.withFont(WorkspaceTheme.fontUi()));
    }

    private void applyTextStyle(Button button, SlotUiTextStyle textStyle) {
        button.textStyle(style -> style
                .font(WorkspaceTheme.fontUi())
                .textColor(textStyle.color())
                .fontSize(textStyle.fontSize())
                .textShadow(textStyle.shadow())
                .adaptiveWidth(textStyle.adaptiveWidth())
                .textAlignHorizontal(map(textStyle.horizontal()))
                .textAlignVertical(map(textStyle.vertical())));
        if (button.text != null) {
            button.text.setAllowHitTest(false);
        }
    }

    private void registerEvents(UIElement element, SlotUiElement model) {
        for (SlotUiEventBinding binding : model.eventBindings()) {
            switch (binding.kind()) {
                case MOUSE_DOWN:
                    element.addEventListener(UIEvents.MOUSE_DOWN,
                            event -> dispatch(binding, model, element, event),
                            binding.includeChildren());
                    break;
                case CLICK:
                    element.addEventListener(UIEvents.CLICK,
                            event -> dispatch(binding, model, element, event),
                            binding.includeChildren());
                    break;
                case MOUSE_ENTER:
                    element.addEventListener(UIEvents.MOUSE_ENTER,
                            event -> dispatch(binding, model, element, event),
                            binding.includeChildren());
                    break;
                case MOUSE_LEAVE:
                    element.addEventListener(UIEvents.MOUSE_LEAVE,
                            event -> dispatch(binding, model, element, event),
                            binding.includeChildren());
                    break;
                case MOUSE_WHEEL:
                    element.addEventListener(UIEvents.MOUSE_WHEEL,
                            event -> dispatch(binding, model, element, event),
                            binding.includeChildren());
                    break;
                case TICK:
                    element.addEventListener(UIEvents.TICK,
                            event -> dispatch(binding, model, element, event),
                            binding.includeChildren());
                    break;
                default:
                    break;
            }
        }
    }

    private void dispatch(
            SlotUiEventBinding binding,
            SlotUiElement model,
            UIElement element,
            UIEvent nativeEvent
    ) {
        SlotUiEvent event = new SlotUiEvent(
                binding.kind(),
                nativeEvent.button,
                nativeEvent.x,
                nativeEvent.y,
                Screen.hasShiftDown(),
                Screen.hasControlDown(),
                nativeEvent.deltaY != 0f ? nativeEvent.deltaY : nativeEvent.deltaX
        );
        binding.handler().handle(event);
        applyMutableState(element, model);
        if (event.propagationStopped()) {
            nativeEvent.stopPropagation();
        }
    }

    private static FlexDirection map(SlotUiLayout.FlexDirection direction) {
        if (direction == SlotUiLayout.FlexDirection.ROW) {
            return FlexDirection.ROW;
        }
        return FlexDirection.COLUMN;
    }

    private static AlignItems map(SlotUiLayout.AlignItems alignItems) {
        if (alignItems == SlotUiLayout.AlignItems.CENTER) {
            return AlignItems.CENTER;
        }
        if (alignItems == SlotUiLayout.AlignItems.FLEX_START) {
            return AlignItems.FLEX_START;
        }
        return AlignItems.STRETCH;
    }

    private static AlignContent map(SlotUiLayout.AlignContent alignContent) {
        if (alignContent == SlotUiLayout.AlignContent.SPACE_BETWEEN) {
            return AlignContent.SPACE_BETWEEN;
        }
        return AlignContent.FLEX_START;
    }

    private static FlexWrap map(SlotUiLayout.FlexWrap flexWrap) {
        if (flexWrap == SlotUiLayout.FlexWrap.WRAP) {
            return FlexWrap.WRAP;
        }
        return FlexWrap.NO_WRAP;
    }

    private static TaffyPosition map(SlotUiLayout.PositionType positionType) {
        if (positionType == SlotUiLayout.PositionType.ABSOLUTE) {
            return TaffyPosition.ABSOLUTE;
        }
        return TaffyPosition.RELATIVE;
    }

    private static Horizontal map(SlotUiTextStyle.Horizontal horizontal) {
        if (horizontal == SlotUiTextStyle.Horizontal.CENTER) {
            return Horizontal.CENTER;
        }
        if (horizontal == SlotUiTextStyle.Horizontal.RIGHT) {
            return Horizontal.RIGHT;
        }
        return Horizontal.LEFT;
    }

    private static Vertical map(SlotUiTextStyle.Vertical vertical) {
        if (vertical == SlotUiTextStyle.Vertical.TOP) {
            return Vertical.TOP;
        }
        if (vertical == SlotUiTextStyle.Vertical.BOTTOM) {
            return Vertical.BOTTOM;
        }
        return Vertical.CENTER;
    }
}
