package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.SlotResourceCollections;
import dev.imagio.slot.inventory.core.SlotResourceDisplay;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
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
                    .allowHitTest(true)
                    .tooltipLines(captureTooltip(visible))
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
        boolean fluidOutput = fluidOutput(entry);
        if (fluidOutput || !output.isEmpty()) {
            header.addChild(outputIcon(entry, output)
                    .allowHitTest(true)
                    .renderVanillaCount(true)
                    .tooltipStack(fluidOutput ? null : output)
                    .tooltipLines(entryTooltip(entry))
                    .on(SlotUiEventKind.CLICK, event -> openRecipe(entry, event)));
        }
        header.addChild(SlotUiElement.label(entryText(entry), WorkspaceUiPalette.TEXT)
                .allowHitTest(true)
                .tooltipLines(entryTooltip(entry))
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
        header.addChild(button("-", "Reduce recipe runs by one", () ->
                context.adjustEntry(entryId, -entry.outputCountPerBatch()), 12, WorkspaceUiPalette.ROW_DIM));
        header.addChild(button("+", "Add one recipe run", () ->
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
        Set<SlotResourceIdentity> carried = carriedResources(available);
        LinkedHashSet<SlotResourceIdentity> emitted = new LinkedHashSet<>();
        ArrayList<SlotWorkspaceViewModel.AtlasItem> cards = new ArrayList<>();
        for (CraftRunIngredientGroup group : entry.inputs()) {
            int before = cards.size();
            SlotResourceIdentity selected = craftRunPressureResource(group, carried);
            if (selected != null) {
                addCard(cards, emitted, selected, available, entry, group);
                if (cards.size() == before) {
                    addMissingCard(cards, emitted, entry, group, selected);
                }
                continue;
            }
            if (group != null) {
                for (CraftRunAlternative alternative : group.alternatives()) {
                    if (alternative != null) {
                        addCard(cards, emitted, alternative.resourceIdentity(), available, entry, group);
                    }
                }
                if (cards.size() == before) {
                    addChoiceCard(cards, emitted, entry, group);
                }
            }
        }
        return cards.isEmpty() ? List.of() : List.copyOf(cards);
    }

    private static Set<SlotResourceIdentity> carriedResources(List<SlotWorkspaceViewModel.AtlasItem> items) {
        LinkedHashSet<SlotResourceIdentity> carried = new LinkedHashSet<>();
        for (SlotWorkspaceViewModel.AtlasItem item : items) {
            if (item == null || !item.carried() || item.totalCount() <= 0 || item.resource() == null) {
                continue;
            }
            addResource(carried, item.resource().toIdentity());
        }
        return carried.isEmpty() ? Set.of() : Set.copyOf(carried);
    }

    private static SlotResourceIdentity craftRunPressureResource(
            CraftRunIngredientGroup group,
            Set<SlotResourceIdentity> carriedResources
    ) {
        if (group == null || group.alternatives().isEmpty()) {
            return null;
        }
        if (group.selectedAlternativeResource() != null) {
            return group.selectedAlternativeResource();
        }
        if (group.alternatives().size() == 1) {
            CraftRunAlternative alternative = group.alternatives().get(0);
            return alternative == null ? null : alternative.resourceIdentity();
        }
        Set<SlotResourceIdentity> carried = carriedResources == null ? Set.of() : carriedResources;
        for (CraftRunAlternative alternative : group.alternatives()) {
            if (alternative != null && containsResource(carried, alternative.resourceIdentity())) {
                return alternative.resourceIdentity();
            }
        }
        return null;
    }

    private static void addCard(
            List<SlotWorkspaceViewModel.AtlasItem> cards,
            Set<SlotResourceIdentity> emitted,
            SlotResourceIdentity resource,
            List<SlotWorkspaceViewModel.AtlasItem> availableItems,
            CraftRunRecipeEntry entry,
            CraftRunIngredientGroup group
    ) {
        if (cards == null || emitted == null || resource == null || availableItems == null) {
            return;
        }
        SlotResourceIdentity key = SlotResourceCollections.key(resource);
        if (containsResource(emitted, key)) {
            return;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : availableItems) {
            SlotResourceIdentity candidate = item == null || item.resource() == null ? null : item.resource().toIdentity();
            if (resourcesMatch(candidate, key)) {
                addResource(emitted, key);
                cards.add(withRecipeTarget(item, entry, group));
                return;
            }
        }
    }

    private static void addMissingCard(
            List<SlotWorkspaceViewModel.AtlasItem> cards,
            Set<SlotResourceIdentity> emitted,
            CraftRunRecipeEntry entry,
            CraftRunIngredientGroup group,
            SlotResourceIdentity resource
    ) {
        if (cards == null || emitted == null || entry == null || group == null || resource == null) {
            return;
        }
        SlotResourceIdentity key = SlotResourceCollections.key(resource);
        if (key == null || containsResource(emitted, key)) {
            return;
        }
        ItemIdentity displayIdentity = displayIdentity(key);
        ItemStack displayStack = SlotWorkspaceViewModel.displayStackForResource(key);
        if ((displayStack == null || displayStack.isEmpty()) && !key.fluid()) {
            displayStack = SlotWorkspaceViewModel.displayStackForIdentity(PLACEHOLDER_ID);
        }
        if ((displayStack == null || displayStack.isEmpty()) && !key.fluid()) {
            return;
        }
        addResource(emitted, key);
        cards.add(new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(displayIdentity),
                displayStack == null ? ItemStack.EMPTY : displayStack.copy(),
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
                group.requiredForBatches(entry.remainingBatches()),
                false,
                0,
                false,
                false,
                "",
                -1,
                0,
                SlotWorkspaceViewModel.PutAwayState.NONE,
                SlotWorkspaceViewModel.ResourceRef.from(key),
                group.requiredAmountForBatches(entry.remainingBatches())
        ));
    }

    private static void addChoiceCard(
            List<SlotWorkspaceViewModel.AtlasItem> cards,
            Set<SlotResourceIdentity> emitted,
            CraftRunRecipeEntry entry,
            CraftRunIngredientGroup group
    ) {
        if (cards == null || emitted == null || entry == null || group == null) {
            return;
        }
        ItemIdentity choiceIdentity = CraftRunIngredientChoiceRef.placeholderIdentity(entry.entryId(), group.groupId());
        SlotResourceIdentity choiceResource = SlotResourceIdentity.item(choiceIdentity);
        if (choiceIdentity == null || containsResource(emitted, choiceResource)) {
            return;
        }
        ItemStack displayStack = SlotWorkspaceViewModel.displayStackForIdentity(PLACEHOLDER_ID);
        if (displayStack == null || displayStack.isEmpty()) {
            return;
        }
        addResource(emitted, choiceResource);
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
                group.requiredForBatches(entry.remainingBatches()),
                false,
                0,
                false,
                false,
                "",
                -1,
                0,
                SlotWorkspaceViewModel.PutAwayState.NONE,
                SlotWorkspaceViewModel.ResourceRef.from(choiceResource),
                group.requiredAmountForBatches(entry.remainingBatches())
        ));
    }

    private static String ingredientCardName(CraftRunIngredientGroup group, SlotResourceIdentity resource) {
        if (group == null || group.label() == null || group.label().isBlank()) {
            return "Recipe ingredient";
        }
        if (resource != null && !CraftRunIngredientChoiceRef.isPlaceholder(displayIdentity(resource))) {
            for (CraftRunAlternative alternative : group.alternatives()) {
                if (alternative != null
                        && alternative.resourceIdentity() != null
                        && resourcesMatch(alternative.resourceIdentity(), resource)) {
                    return alternative.label();
                }
            }
        }
        return group.alternatives().size() > 1 ? "Any " + group.label() : group.label();
    }

    private static SlotWorkspaceViewModel.AtlasItem withRecipeTarget(
            SlotWorkspaceViewModel.AtlasItem existing,
            CraftRunRecipeEntry entry,
            CraftRunIngredientGroup group
    ) {
        if (existing == null || entry == null || group == null) {
            return existing;
        }
        return new SlotWorkspaceViewModel.AtlasItem(
                existing.identity(),
                existing.displayStack(),
                existing.name(),
                existing.totalCount(),
                existing.firstSlotIndex(),
                existing.islandId(),
                existing.recent(),
                existing.playerPlaced(),
                existing.carried(),
                existing.ghost(),
                existing.proximateCount(),
                existing.chipSuggestions(),
                existing.presence(),
                existing.elsewhere(),
                existing.isCarriedContainer(),
                existing.containerFreeSlotCount(),
                existing.containerSlotCapacity(),
                existing.kitNeeded(),
                group.requiredForBatches(entry.remainingBatches()),
                false,
                0,
                existing.junk(),
                existing.acceptedWorkflowInput(),
                existing.largestCarriedSourceId(),
                existing.largestCarriedSlotIndex(),
                existing.largestCarriedSlotCount(),
                existing.putAwayState(),
                existing.resource(),
                existing.resourceAmount());
    }

    private static ItemIdentity displayIdentity(SlotResourceIdentity resource) {
        SlotResourceIdentity key = SlotResourceCollections.key(resource);
        if (key == null) {
            return PLACEHOLDER_ID;
        }
        if (key.fluid()) {
            return ItemIdentity.of(key.syntheticItemId());
        }
        ItemIdentity identity = key.toItemIdentity();
        return identity == null ? PLACEHOLDER_ID : identity;
    }

    private static boolean containsResource(Set<SlotResourceIdentity> resources, SlotResourceIdentity resource) {
        if (resources == null || resource == null) {
            return false;
        }
        for (SlotResourceIdentity candidate : resources) {
            if (resourcesMatch(candidate, resource)) {
                return true;
            }
        }
        return false;
    }

    private static void addResource(Set<SlotResourceIdentity> resources, SlotResourceIdentity resource) {
        if (resources != null && resource != null && !containsResource(resources, resource)) {
            resources.add(SlotResourceCollections.key(resource));
        }
    }

    private static boolean resourcesMatch(SlotResourceIdentity left, SlotResourceIdentity right) {
        SlotResourceIdentity leftKey = SlotResourceCollections.key(left);
        SlotResourceIdentity rightKey = SlotResourceCollections.key(right);
        if (leftKey == null || rightKey == null || leftKey.kind() != rightKey.kind()) {
            return false;
        }
        if (leftKey.item()) {
            return ItemIdentityMatcher.matchesMovable(leftKey.toItemIdentity(), rightKey.toItemIdentity());
        }
        return leftKey.equals(rightKey);
    }

    private static ItemStack outputStack(CraftRunRecipeEntry entry) {
        if (entry == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = entry.outputResourceIdentity() != null && entry.outputResourceIdentity().fluid()
                ? SlotWorkspaceViewModel.displayStackForResource(entry.outputResourceIdentity())
                : SlotWorkspaceViewModel.displayStackForIdentity(entry.outputIdentity());
        if (stack == null || stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(Math.min(copy.getMaxStackSize(), Math.max(1, entry.outputCountPerBatch())));
        return copy;
    }

    private static SlotUiElement outputIcon(CraftRunRecipeEntry entry, ItemStack outputStack) {
        SlotResourceIdentity outputResource = entry == null
                ? null
                : SlotResourceCollections.key(entry.outputResourceIdentity());
        if (outputResource != null && outputResource.fluid()) {
            return SlotUiElement.fluidIcon(outputResource.id(), 12, true);
        }
        return SlotUiElement.itemIcon(outputStack, 12, true);
    }

    private static boolean fluidOutput(CraftRunRecipeEntry entry) {
        SlotResourceIdentity outputResource = entry == null
                ? null
                : SlotResourceCollections.key(entry.outputResourceIdentity());
        return outputResource != null && outputResource.fluid();
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
        return runCountPrefix(entry == null ? 1 : entry.remainingBatches()) + label;
    }

    private static List<Component> entryTooltip(CraftRunRecipeEntry entry) {
        if (entry == null) {
            return List.of(Component.literal("Open recipe in EMI"));
        }
        ArrayList<Component> lines = new ArrayList<>();
        if (fluidOutput(entry)) {
            lines.add(Component.literal(entry.label() == null || entry.label().isBlank()
                    ? "Fluid output"
                    : entry.label()));
        }
        lines.add(Component.literal("Open recipe in EMI"));
        lines.add(Component.empty());
        lines.add(Component.literal("Runs: " + entry.remainingBatches()));
        lines.add(Component.literal("Each run: " + formatResourceAmount(
                entry.outputResourceIdentity(),
                entry.outputAmountPerBatch())));
        lines.add(Component.literal("Total output: " + formatResourceAmount(
                entry.outputResourceIdentity(),
                outputForRuns(entry.outputAmountPerBatch(), entry.remainingBatches()))));
        if (entry.remainingOutputAmount() != outputForRuns(entry.outputAmountPerBatch(), entry.remainingBatches())) {
            lines.add(Component.literal("Remaining tracked output: " + formatResourceAmount(
                    entry.outputResourceIdentity(),
                    entry.remainingOutputAmount())));
        }
        return List.copyOf(lines);
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
        String text = runCountPrefix(capture == null ? 1 : remainingBatches(capture)) + label;
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

    private static List<Component> captureTooltip(CraftRunRecipeCapture capture) {
        if (capture == null) {
            return List.of();
        }
        int runs = remainingBatches(capture);
        ArrayList<Component> lines = new ArrayList<>();
        lines.add(Component.literal(capture.label()));
        lines.add(Component.empty());
        lines.add(Component.literal("Runs: " + runs));
        lines.add(Component.literal("Each run: " + formatResourceAmount(
                capture.outputResourceIdentity(),
                capture.outputAmountPerBatch())));
        lines.add(Component.literal("Total output: " + formatResourceAmount(
                capture.outputResourceIdentity(),
                outputForRuns(capture.outputAmountPerBatch(), runs))));
        if (capture.remainingOutputAmount() != outputForRuns(capture.outputAmountPerBatch(), runs)) {
            lines.add(Component.literal("Remaining tracked output: " + formatResourceAmount(
                    capture.outputResourceIdentity(),
                    capture.remainingOutputAmount())));
        }
        return List.copyOf(lines);
    }

    private static String runCountPrefix(int count) {
        return "x" + Math.max(1, count) + " ";
    }

    private static int remainingBatches(CraftRunRecipeCapture capture) {
        if (capture == null || capture.remainingOutputAmount() <= 0L) {
            return 0;
        }
        long perBatch = Math.max(1L, capture.outputAmountPerBatch());
        long batches = (capture.remainingOutputAmount() + perBatch - 1L) / perBatch;
        return batches >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(1L, batches);
    }

    private static long outputForRuns(long outputAmountPerBatch, int runs) {
        if (outputAmountPerBatch <= 0L || runs <= 0) {
            return 0L;
        }
        if (outputAmountPerBatch >= Long.MAX_VALUE / runs) {
            return Long.MAX_VALUE;
        }
        return outputAmountPerBatch * runs;
    }

    private static String formatResourceAmount(SlotResourceIdentity resource, long amount) {
        return SlotResourceDisplay.formatAmount(resource, amount);
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
