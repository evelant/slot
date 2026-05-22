package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.ui.spi.SlotUiLayout;
import dev.imagio.slot.ui.spi.SlotUiTextStyle;
import dev.imagio.slot.workflow.domain.CraftRunAlternative;
import dev.imagio.slot.workflow.domain.CraftRunIngredientGroup;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import dev.imagio.slot.workflow.domain.CraftRunRecipeEntry;
import dev.imagio.slot.workflow.domain.CraftRunState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CraftRunUiBuilder {
    public static final int PANEL_WIDTH_PX = 188;

    private static final int ROW_GAP_PX = 3;
    private static final int ACTION_ROW_HEIGHT_PX = 16;
    private static final int ENTRY_HEADER_HEIGHT_PX = 15;
    private static final ItemIdentity PLACEHOLDER_ID = ItemIdentity.of("minecraft:knowledge_book");

    private final Context context;

    public CraftRunUiBuilder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        this.context = context;
    }

    public List<SlotUiElement> visibleRecipeActions() {
        List<CraftRunRecipeCapture> visibleRecipes = context.visibleRecipes();
        if (visibleRecipes == null || visibleRecipes.isEmpty()) {
            return List.of();
        }
        boolean includeIngredients = activeRecipeCount(visibleRecipes) > 1;
        ArrayList<SlotUiElement> rows = new ArrayList<>();
        for (CraftRunRecipeCapture visible : visibleRecipes) {
            if (visible == null || !visible.active()) {
                continue;
            }
            SlotUiElement row = SlotUiElement.panel(0x7010171D)
                    .id("recipe-action:" + visible.sourceKey())
                    .layout(layout -> layout
                            .widthPercent(100)
                            .height(ACTION_ROW_HEIGHT_PX)
                            .paddingAll(1)
                            .gapAll(ROW_GAP_PX)
                            .alignItems(SlotUiLayout.AlignItems.CENTER)
                            .flexDirection(SlotUiLayout.FlexDirection.ROW));
            row.addChild(SlotUiElement.label(captureText(visible, includeIngredients), WorkspaceUiPalette.TEXT)
                    .allowHitTest(false)
                    .layout(layout -> layout.flex(1).height(12))
                    .textStyle(style -> style
                            .color(WorkspaceUiPalette.TEXT)
                            .fontSize(6.5f)
                            .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                            .vertical(SlotUiTextStyle.Vertical.CENTER)));
            row.addChild(button(
                    "Add Recipe",
                    "Add this EMI recipe to the recipe list",
                    () -> context.addVisibleRecipe(visible),
                    58,
                    WorkspaceUiPalette.SELECTED));
            rows.add(row);
        }
        return rows.isEmpty() ? List.of() : List.copyOf(rows);
    }

    public List<SlotUiElement> entrySections(List<SlotWorkspaceViewModel.AtlasItem> availableItems) {
        CraftRunState run = context.craftRun() == null ? CraftRunState.empty() : context.craftRun();
        if (!run.active()) {
            return List.of();
        }
        ArrayList<SlotUiElement> sections = new ArrayList<>();
        for (CraftRunRecipeEntry entry : run.entries()) {
            if (entry != null && entry.active()) {
                sections.add(entrySection(entry, availableItems));
            }
        }
        return sections.isEmpty() ? List.of() : List.copyOf(sections);
    }

    public List<SlotUiElement> panelRows(List<SlotWorkspaceViewModel.AtlasItem> availableItems) {
        ArrayList<SlotUiElement> rows = new ArrayList<>();
        rows.addAll(visibleRecipeActions());
        rows.addAll(entrySections(availableItems));
        return rows.isEmpty() ? List.of() : List.copyOf(rows);
    }

    public boolean hasPanelRows(List<SlotWorkspaceViewModel.AtlasItem> availableItems) {
        return !panelRows(availableItems).isEmpty();
    }

    private SlotUiElement entrySection(
            CraftRunRecipeEntry entry,
            List<SlotWorkspaceViewModel.AtlasItem> availableItems
    ) {
        List<SlotWorkspaceViewModel.AtlasItem> cards = entryCards(entry, availableItems);
        SlotUiElement section = SlotUiElement.panel(0x7010171D)
                .id("craft-run:" + entry.entryId())
                .layout(layout -> layout
                        .widthPercent(100)
                        .paddingAll(0)
                        .gapAll(2)
                        .flexDirection(SlotUiLayout.FlexDirection.COLUMN));
        section.addChild(entryHeader(entry));
        SlotUiElement grid = SlotUiElement.element()
                .attach(WorkspaceUiAttachments.WALL_CRAFT_RUN_GRID, Boolean.TRUE)
                .attach(WorkspaceUiAttachments.ATLAS_ITEMS, cards)
                .layout(layout -> layout
                        .widthPercent(100)
                        .gapAll(WallSectionUiBuilder.CARD_GAP_PX)
                        .paddingAll(0)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW)
                        .flexWrap(SlotUiLayout.FlexWrap.WRAP)
                        .alignItems(SlotUiLayout.AlignItems.FLEX_START)
                        .alignContent(SlotUiLayout.AlignContent.FLEX_START));
        section.addChild(grid);
        return section;
    }

    private SlotUiElement entryHeader(CraftRunRecipeEntry entry) {
        String entryId = entry.entryId();
        SlotUiElement header = SlotUiElement.panel(0xA0365743)
                .layout(layout -> layout
                        .widthPercent(100)
                        .height(ENTRY_HEADER_HEIGHT_PX)
                        .paddingHorizontal(2)
                        .gapAll(2)
                        .alignItems(SlotUiLayout.AlignItems.CENTER)
                        .flexDirection(SlotUiLayout.FlexDirection.ROW));
        ItemStack output = outputStack(entry);
        if (!output.isEmpty()) {
            header.addChild(SlotUiElement.itemIcon(output, 12, true)
                    .allowHitTest(true)
                    .renderVanillaCount(true)
                    .tooltipStack(output)
                    .on(SlotUiEventKind.CLICK, event -> openRecipe(entry, event)));
        }
        header.addChild(SlotUiElement.label(entryText(entry), WorkspaceUiPalette.TEXT)
                .allowHitTest(true)
                .tooltip(Component.literal("Open recipe in EMI"))
                .layout(layout -> layout.flex(1).heightPercent(100))
                .textStyle(style -> style
                        .color(entry != null && entry.complete() ? WorkspaceUiPalette.ACCENT : WorkspaceUiPalette.TEXT)
                        .fontSize(6.5f)
                        .horizontal(SlotUiTextStyle.Horizontal.LEFT)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> openRecipe(entry, event)));
        header.addChild(button(
                "Stage",
                "Stage missing ingredients into main inventory",
                () -> {
                    context.stageEntry(entryId);
                    context.openRecipe(entry);
                },
                31,
                WorkspaceUiPalette.ROW_DIM));
        header.addChild(button("-", "Reduce remaining output count", () ->
                context.adjustEntry(entryId, -entry.outputCountPerBatch()), 12, WorkspaceUiPalette.ROW_DIM));
        header.addChild(button("+", "Add another output batch", () ->
                context.adjustEntry(entryId, entry.outputCountPerBatch()), 12, WorkspaceUiPalette.ROW_DIM));
        header.addChild(button("Done", "Remove this recipe from the list", () ->
                context.removeEntry(entryId), 27, WorkspaceUiPalette.ROW_DIM));
        return header;
    }

    private void openRecipe(CraftRunRecipeEntry entry, dev.imagio.slot.ui.spi.SlotUiEvent event) {
        if (event.button() != 0) {
            return;
        }
        event.stopPropagation();
        context.openRecipe(entry);
    }

    private static SlotUiElement button(String text, String tooltip, Runnable action, int width, int color) {
        return SlotUiElement.button(text, true, color)
                .tooltip(Component.literal(tooltip == null ? "" : tooltip))
                .layout(layout -> layout
                        .width(width)
                        .height(12)
                        .paddingHorizontal(3))
                .textStyle(style -> style
                        .color(WorkspaceUiPalette.TEXT)
                        .fontSize(6.5f)
                        .horizontal(SlotUiTextStyle.Horizontal.CENTER)
                        .vertical(SlotUiTextStyle.Vertical.CENTER))
                .on(SlotUiEventKind.CLICK, event -> {
                    if (event.button() != 0) {
                        return;
                    }
                    event.stopPropagation();
                    if (action != null) {
                        action.run();
                    }
                });
    }

    private static List<SlotWorkspaceViewModel.AtlasItem> entryCards(
            CraftRunRecipeEntry entry,
            List<SlotWorkspaceViewModel.AtlasItem> availableItems
    ) {
        if (entry == null) {
            return List.of();
        }
        List<SlotWorkspaceViewModel.AtlasItem> available = availableItems == null ? List.of() : availableItems;
        Set<ItemIdentity> carried = carriedIdentities(available);
        LinkedHashSet<ItemIdentity> emitted = new LinkedHashSet<>();
        ArrayList<SlotWorkspaceViewModel.AtlasItem> cards = new ArrayList<>();
        for (CraftRunIngredientGroup group : entry.inputs()) {
            int before = cards.size();
            ItemIdentity selected = craftRunPressureIdentity(group, carried);
            if (selected != null) {
                addCard(cards, emitted, selected, available);
                if (cards.size() == before) {
                    addMissingCard(cards, emitted, entry, group, selected);
                }
                continue;
            }
            if (group != null) {
                for (CraftRunAlternative alternative : group.alternatives()) {
                    if (alternative != null) {
                        addCard(cards, emitted, alternative.identity(), available);
                    }
                }
                if (cards.size() == before) {
                    addChoiceCard(cards, emitted, entry, group);
                }
            }
        }
        return cards.isEmpty() ? List.of() : List.copyOf(cards);
    }

    private static Set<ItemIdentity> carriedIdentities(List<SlotWorkspaceViewModel.AtlasItem> items) {
        LinkedHashSet<ItemIdentity> carried = new LinkedHashSet<>();
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item == null || !item.carried() || item.totalCount() <= 0 || item.identity() == null) {
                continue;
            }
            ItemIdentityCollections.add(carried, item.identity().toIdentity());
        }
        return carried.isEmpty() ? Set.of() : Set.copyOf(carried);
    }

    private static ItemIdentity craftRunPressureIdentity(
            CraftRunIngredientGroup group,
            Set<ItemIdentity> carriedIdentities
    ) {
        if (group == null || group.alternatives().isEmpty()) {
            return null;
        }
        if (group.selectedAlternativeIdentity() != null) {
            return group.selectedAlternativeIdentity();
        }
        if (group.alternatives().size() == 1) {
            CraftRunAlternative alternative = group.alternatives().get(0);
            return alternative == null ? null : alternative.identity();
        }
        Set<ItemIdentity> carried = carriedIdentities == null ? Set.of() : carriedIdentities;
        for (CraftRunAlternative alternative : group.alternatives()) {
            if (alternative != null && ItemIdentityCollections.contains(carried, alternative.identity())) {
                return alternative.identity();
            }
        }
        return null;
    }

    private static void addCard(
            List<SlotWorkspaceViewModel.AtlasItem> cards,
            Set<ItemIdentity> emitted,
            ItemIdentity identity,
            List<SlotWorkspaceViewModel.AtlasItem> availableItems
    ) {
        if (cards == null || emitted == null || identity == null || availableItems == null) {
            return;
        }
        ItemIdentity key = ItemIdentityCollections.key(identity);
        if (emitted.contains(key)) {
            return;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : availableItems) {
            ItemIdentity candidate = item == null || item.identity() == null ? null : item.identity().toIdentity();
            if (candidate != null && ItemIdentityMatcher.matchesMovable(candidate, key)) {
                emitted.add(key);
                cards.add(item);
                return;
            }
        }
    }

    private static void addMissingCard(
            List<SlotWorkspaceViewModel.AtlasItem> cards,
            Set<ItemIdentity> emitted,
            CraftRunRecipeEntry entry,
            CraftRunIngredientGroup group,
            ItemIdentity identity
    ) {
        if (cards == null || emitted == null || entry == null || group == null || identity == null) {
            return;
        }
        ItemIdentity key = ItemIdentityCollections.key(identity);
        if (key == null || emitted.contains(key)) {
            return;
        }
        ItemStack displayStack = SlotWorkspaceViewModel.displayStackForIdentity(key);
        if (displayStack == null || displayStack.isEmpty()) {
            displayStack = SlotWorkspaceViewModel.displayStackForIdentity(PLACEHOLDER_ID);
        }
        if (displayStack == null || displayStack.isEmpty()) {
            return;
        }
        emitted.add(key);
        cards.add(new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(key),
                displayStack.copy(),
                ingredientCardName(group, key),
                0,
                0,
                SlotWorkspaceAtlasLayout.ISLAND_MISC,
                false,
                false,
                false,
                true,
                0,
                List.of(),
                List.of(),
                List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                group.requiredForBatches(entry.remainingBatches()),
                false,
                false,
                "",
                -1,
                0,
                SlotWorkspaceViewModel.PutAwayState.NONE
        ));
    }

    private static void addChoiceCard(
            List<SlotWorkspaceViewModel.AtlasItem> cards,
            Set<ItemIdentity> emitted,
            CraftRunRecipeEntry entry,
            CraftRunIngredientGroup group
    ) {
        if (cards == null || emitted == null || entry == null || group == null) {
            return;
        }
        ItemIdentity choiceIdentity = CraftRunIngredientChoiceRef.placeholderIdentity(entry.entryId(), group.groupId());
        if (choiceIdentity == null || emitted.contains(choiceIdentity)) {
            return;
        }
        ItemStack displayStack = SlotWorkspaceViewModel.displayStackForIdentity(PLACEHOLDER_ID);
        if (displayStack == null || displayStack.isEmpty()) {
            return;
        }
        emitted.add(choiceIdentity);
        cards.add(new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(choiceIdentity),
                displayStack.copy(),
                ingredientCardName(group, null),
                0,
                0,
                SlotWorkspaceAtlasLayout.ISLAND_MISC,
                false,
                false,
                false,
                true,
                0,
                List.of(),
                List.of(),
                List.of(),
                false,
                0,
                0,
                false,
                0,
                false,
                group.requiredForBatches(entry.remainingBatches()),
                false,
                false,
                "",
                -1,
                0,
                SlotWorkspaceViewModel.PutAwayState.NONE
        ));
    }

    private static String ingredientCardName(CraftRunIngredientGroup group, ItemIdentity identity) {
        if (group == null || group.label() == null || group.label().isBlank()) {
            return "Recipe ingredient";
        }
        if (identity != null && !CraftRunIngredientChoiceRef.isPlaceholder(identity)) {
            for (CraftRunAlternative alternative : group.alternatives()) {
                if (alternative != null
                        && alternative.identity() != null
                        && ItemIdentityMatcher.matchesMovable(alternative.identity(), identity)) {
                    return alternative.label();
                }
            }
        }
        return group.alternatives().size() > 1 ? "Any " + group.label() : group.label();
    }

    private static ItemStack outputStack(CraftRunRecipeEntry entry) {
        if (entry == null || entry.outputIdentity() == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = SlotWorkspaceViewModel.displayStackForIdentity(entry.outputIdentity());
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(Math.min(copy.getMaxStackSize(), Math.max(1, entry.outputCountPerBatch())));
        return copy;
    }

    private static String entryText(CraftRunRecipeEntry entry) {
        String label = entry == null ? "Recipe" : entry.label();
        if (label == null || label.isBlank()) {
            label = "Recipe";
        }
        if (label.length() > 20) {
            label = label.substring(0, 19) + ".";
        }
        if (entry != null && entry.complete()) {
            return label + " done";
        }
        return label + " x" + Math.max(1, entry == null ? 1 : entry.remainingOutputCount());
    }

    private static int activeRecipeCount(List<CraftRunRecipeCapture> captures) {
        int count = 0;
        for (CraftRunRecipeCapture capture : captures) {
            if (capture != null && capture.active()) {
                count++;
            }
        }
        return count;
    }

    private static String captureText(CraftRunRecipeCapture capture, boolean includeIngredients) {
        String label = capture == null ? "Recipe" : capture.label();
        if (label == null || label.isBlank()) {
            label = "Recipe";
        }
        String text = label + " x" + Math.max(1, capture == null ? 1 : capture.remainingOutputCount());
        if (includeIngredients) {
            String ingredients = ingredientSummary(capture);
            if (!ingredients.isBlank()) {
                text = text + " - " + ingredients;
            }
        }
        if (text.length() > 46) {
            text = text.substring(0, 45) + ".";
        }
        return text;
    }

    private static String ingredientSummary(CraftRunRecipeCapture capture) {
        if (capture == null || capture.inputs().isEmpty()) {
            return "";
        }
        ArrayList<String> labels = new ArrayList<>();
        for (CraftRunIngredientGroup group : capture.inputs()) {
            if (group == null || group.label().isBlank()) {
                continue;
            }
            labels.add(group.label());
            if (labels.size() >= 2) {
                break;
            }
        }
        if (labels.isEmpty()) {
            return "";
        }
        String summary = String.join(", ", labels);
        return capture.inputs().size() > labels.size() ? summary + ", ..." : summary;
    }

    public interface Context {
        CraftRunState craftRun();

        List<CraftRunRecipeCapture> visibleRecipes();

        void addVisibleRecipe(CraftRunRecipeCapture capture);

        void stageEntry(String entryId);

        void openRecipe(CraftRunRecipeEntry entry);

        void adjustEntry(String entryId, int delta);

        void removeEntry(String entryId);
    }
}
