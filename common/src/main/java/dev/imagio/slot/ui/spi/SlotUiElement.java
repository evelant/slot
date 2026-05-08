package dev.imagio.slot.ui.spi;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class SlotUiElement {
    public enum Kind {
        ELEMENT,
        BUTTON,
        LABEL,
        ITEM_ICON
    }

    private final Kind kind;
    private final SlotUiLayout layout = new SlotUiLayout();
    private final SlotUiTextStyle textStyle = new SlotUiTextStyle();
    private final List<SlotUiElement> children = new ArrayList<>();
    private final EnumMap<SlotUiEventKind, List<SlotUiEventBinding>> eventBindings =
            new EnumMap<>(SlotUiEventKind.class);
    private final Map<String, Object> attachments = new HashMap<>();
    private String id;
    private String text = "";
    private boolean allowHitTest = true;
    private Integer backgroundColor;
    private Integer overlayColor;
    private boolean buttonActive = true;
    private boolean buttonHasText = true;
    private int buttonColor;
    private ItemStack itemStack = ItemStack.EMPTY;
    private float itemIconSize = 16;
    private boolean itemCarried = true;
    private boolean renderVanillaCount = true;
    private Integer zIndex;
    private ItemStack tooltipStack = ItemStack.EMPTY;
    private List<Component> tooltipLines = List.of();

    private SlotUiElement(Kind kind) {
        this.kind = kind == null ? Kind.ELEMENT : kind;
    }

    public static SlotUiElement element() {
        return new SlotUiElement(Kind.ELEMENT);
    }

    public static SlotUiElement panel(int color) {
        return element().backgroundColor(color);
    }

    public static SlotUiElement button(String text, boolean active, int color) {
        SlotUiElement element = new SlotUiElement(Kind.BUTTON);
        element.text(text);
        element.buttonActive(active);
        element.buttonColor(color);
        return element;
    }

    public static SlotUiElement label(String text, int color) {
        SlotUiElement element = new SlotUiElement(Kind.LABEL);
        element.text(text);
        element.textStyle(style -> style
                .color(color)
                .fontSize(8)
                .shadow(false)
                .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                .vertical(SlotUiTextStyle.Vertical.CENTER));
        element.allowHitTest(false);
        return element;
    }

    public static SlotUiElement itemIcon(ItemStack stack, float size, boolean carried) {
        SlotUiElement element = new SlotUiElement(Kind.ITEM_ICON);
        element.itemStack(stack);
        element.itemIconSize(size);
        element.itemCarried(carried);
        element.layout(layout -> layout.width(size).height(size));
        element.allowHitTest(false);
        return element;
    }

    public Kind kind() {
        return kind;
    }

    public SlotUiElement id(String value) {
        id = value == null ? "" : value;
        return this;
    }

    public String id() {
        return id;
    }

    public SlotUiLayout layout() {
        return layout;
    }

    public SlotUiElement layout(Consumer<SlotUiLayout> mutator) {
        if (mutator != null) {
            mutator.accept(layout);
        }
        return this;
    }

    public SlotUiTextStyle textStyle() {
        return textStyle;
    }

    public SlotUiElement textStyle(Consumer<SlotUiTextStyle> mutator) {
        if (mutator != null) {
            mutator.accept(textStyle);
        }
        return this;
    }

    public SlotUiElement addChild(SlotUiElement child) {
        if (child != null) {
            children.add(child);
        }
        return this;
    }

    public SlotUiElement addChildren(SlotUiElement... newChildren) {
        if (newChildren == null) {
            return this;
        }
        for (SlotUiElement child : newChildren) {
            addChild(child);
        }
        return this;
    }

    public List<SlotUiElement> children() {
        return Collections.unmodifiableList(children);
    }

    public SlotUiElement on(SlotUiEventKind kind, SlotUiEventHandler handler) {
        return on(kind, handler, false);
    }

    public SlotUiElement on(SlotUiEventKind kind, SlotUiEventHandler handler, boolean includeChildren) {
        SlotUiEventBinding binding = new SlotUiEventBinding(kind, handler, includeChildren);
        eventBindings.computeIfAbsent(binding.kind(), ignored -> new ArrayList<>()).add(binding);
        return this;
    }

    public List<SlotUiEventBinding> eventBindings() {
        ArrayList<SlotUiEventBinding> bindings = new ArrayList<>();
        for (List<SlotUiEventBinding> byKind : eventBindings.values()) {
            bindings.addAll(byKind);
        }
        return List.copyOf(bindings);
    }

    public void dispatch(SlotUiEvent event) {
        if (event == null) {
            return;
        }
        List<SlotUiEventBinding> bindings = eventBindings.get(event.kind());
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        for (SlotUiEventBinding binding : List.copyOf(bindings)) {
            binding.handler().handle(event);
        }
    }

    public SlotUiElement text(String value) {
        text = value == null ? "" : value;
        return this;
    }

    public String text() {
        return text;
    }

    public SlotUiElement allowHitTest(boolean value) {
        allowHitTest = value;
        return this;
    }

    public boolean allowHitTest() {
        return allowHitTest;
    }

    public SlotUiElement backgroundColor(Integer value) {
        backgroundColor = value;
        return this;
    }

    public Integer backgroundColor() {
        return backgroundColor;
    }

    public SlotUiElement overlayColor(Integer value) {
        overlayColor = value;
        return this;
    }

    public Integer overlayColor() {
        return overlayColor;
    }

    public SlotUiElement zIndex(Integer value) {
        zIndex = value;
        return this;
    }

    public Integer zIndex() {
        return zIndex;
    }

    public SlotUiElement buttonActive(boolean value) {
        buttonActive = value;
        return this;
    }

    public boolean buttonActive() {
        return buttonActive;
    }

    public SlotUiElement noText() {
        buttonHasText = false;
        return this;
    }

    public boolean buttonHasText() {
        return buttonHasText;
    }

    public SlotUiElement buttonColor(int value) {
        buttonColor = value;
        return this;
    }

    public int buttonColor() {
        return buttonColor;
    }

    public SlotUiElement itemStack(ItemStack value) {
        itemStack = value == null ? ItemStack.EMPTY : value.copy();
        return this;
    }

    public ItemStack itemStack() {
        return itemStack == null ? ItemStack.EMPTY : itemStack.copy();
    }

    public SlotUiElement itemIconSize(float value) {
        itemIconSize = value;
        return this;
    }

    public float itemIconSize() {
        return itemIconSize;
    }

    public SlotUiElement itemCarried(boolean value) {
        itemCarried = value;
        return this;
    }

    public boolean itemCarried() {
        return itemCarried;
    }

    public SlotUiElement renderVanillaCount(boolean value) {
        renderVanillaCount = value;
        return this;
    }

    public boolean renderVanillaCount() {
        return renderVanillaCount;
    }

    public SlotUiElement tooltipStack(ItemStack value) {
        tooltipStack = value == null ? ItemStack.EMPTY : value.copy();
        return this;
    }

    public ItemStack tooltipStack() {
        return tooltipStack == null ? ItemStack.EMPTY : tooltipStack.copy();
    }

    public SlotUiElement tooltip(Component... lines) {
        if (lines == null || lines.length == 0) {
            tooltipLines = List.of();
            return this;
        }
        ArrayList<Component> next = new ArrayList<>();
        for (Component line : lines) {
            if (line != null) {
                next.add(line);
            }
        }
        tooltipLines = List.copyOf(next);
        return this;
    }

    public SlotUiElement tooltipLines(List<Component> lines) {
        tooltipLines = lines == null ? List.of() : List.copyOf(lines);
        return this;
    }

    public List<Component> tooltipLines() {
        return tooltipLines == null ? List.of() : tooltipLines;
    }

    public SlotUiElement attach(String key, Object value) {
        if (key == null || key.isBlank()) {
            return this;
        }
        if (value == null) {
            attachments.remove(key);
        } else {
            attachments.put(key, value);
        }
        return this;
    }

    public boolean hasAttachment(String key) {
        return key != null && attachments.containsKey(key);
    }

    public <T> T attachment(String key, Class<T> type) {
        if (key == null || type == null) {
            return null;
        }
        Object value = attachments.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }
}
