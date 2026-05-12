package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.goal.GoalChoiceResolution;
import dev.imagio.slot.inventory.goal.GoalChoiceKeys;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalProjection;
import dev.imagio.slot.inventory.goal.GoalProjectionEntry;
import dev.imagio.slot.inventory.goal.GoalProjectionEntryKind;
import dev.imagio.slot.inventory.goal.GoalProjectionOptions;
import dev.imagio.slot.inventory.goal.GoalProjectionService;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalRecipeDefaults;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import dev.imagio.slot.inventory.goal.GoalVisibleAuthority;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record GoalWorkspaceProjection(
        String goalId,
        String label,
        int targetCount,
        GoalProjection projection,
        List<SlotWorkspaceViewModel.AtlasIsland> islands,
        List<SlotWorkspaceViewModel.AtlasItem> atlasItems,
        Map<SlotWorkspaceViewModel.IdentityRef, GoalProjectionEntry> entryByIdentity,
        Set<SlotWorkspaceViewModel.IdentityRef> choiceIdentities,
        Map<String, List<GoalStackDescriptor>> choiceAlternativesByGroup,
        GoalChoiceResolution manualChoices,
        GoalRecipeDefaults recipeDefaults
) {
    public static final String FIXTURE_GOAL_ID = "fixture:coke_oven";
    private static final ItemIdentity CHOICE_PLACEHOLDER_ID = ItemIdentity.of("minecraft:knowledge_book");

    public GoalWorkspaceProjection {
        goalId = goalId == null || goalId.isBlank() ? FIXTURE_GOAL_ID : goalId;
        label = label == null || label.isBlank() ? "Coke Oven" : label;
        targetCount = Math.max(1, targetCount);
        projection = projection == null
                ? new GoalProjectionService().project(fixtureGoal(targetCount), GoalVisibleAuthority.empty())
                : projection;
        islands = islands == null ? List.of() : List.copyOf(islands);
        atlasItems = atlasItems == null ? List.of() : List.copyOf(atlasItems);
        entryByIdentity = entryByIdentity == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(entryByIdentity));
        choiceIdentities = choiceIdentities == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(choiceIdentities));
        choiceAlternativesByGroup = copyChoiceAlternatives(choiceAlternativesByGroup);
        manualChoices = manualChoices == null ? GoalChoiceResolution.empty() : manualChoices;
        recipeDefaults = recipeDefaults == null ? GoalRecipeDefaults.empty() : recipeDefaults;
    }

    public static GoalWorkspaceProjection fixture(SlotWorkspaceViewModel source, int targetCount) {
        int safeTarget = Math.max(1, targetCount);
        SlotWorkspaceViewModel base = source == null ? SlotWorkspaceViewModel.empty() : source;
        GoalDescriptor goal = fixtureGoal(safeTarget);
        GoalProjection projection = new GoalProjectionService().project(
                goal,
                visibleAuthority(base),
                GoalProjectionOptions.defaults());
        ProjectionBuilder builder = new ProjectionBuilder(
                base,
                goal,
                projection,
                safeTarget,
                GoalChoiceResolution.empty(),
                GoalRecipeDefaults.empty());
        return builder.build();
    }

    public static GoalWorkspaceProjection fromGoal(SlotWorkspaceViewModel source, GoalWorkspaceClientState.GoalTab tab) {
        return tab == null ? null : fromGoal(
                source,
                tab.descriptor(),
                tab.targetCount(),
                tab.choiceResolution(),
                recipeDefaults(source).mergedWith(GoalWorkspaceClientState.rememberedRecipeDefaults()));
    }

    public static GoalWorkspaceProjection fromGoal(
            SlotWorkspaceViewModel source,
            GoalDescriptor descriptor,
            int targetCount
    ) {
        return fromGoal(source, descriptor, targetCount, GoalChoiceResolution.empty());
    }

    public static GoalWorkspaceProjection fromGoal(
            SlotWorkspaceViewModel source,
            GoalDescriptor descriptor,
            int targetCount,
            GoalChoiceResolution manualChoices
    ) {
        return fromGoal(source, descriptor, targetCount, manualChoices, GoalRecipeDefaults.empty());
    }

    public static GoalWorkspaceProjection fromGoal(
            SlotWorkspaceViewModel source,
            GoalDescriptor descriptor,
            int targetCount,
            GoalChoiceResolution manualChoices,
            GoalRecipeDefaults recipeDefaults
    ) {
        if (descriptor == null) {
            return null;
        }
        int safeTarget = Math.max(1, targetCount);
        SlotWorkspaceViewModel base = source == null ? SlotWorkspaceViewModel.empty() : source;
        GoalDescriptor enriched = GoalWorkspaceIntegration.enrichVisibleAlternatives(descriptor, base);
        GoalDescriptor goal = GoalWorkspaceClientState.withTargetCount(
                enriched == null ? descriptor : enriched,
                safeTarget);
        GoalProjection projection = new GoalProjectionService().project(
                goal,
                visibleAuthority(base),
                GoalProjectionOptions.defaults(),
                manualChoices,
                recipeDefaults);
        ProjectionBuilder builder = new ProjectionBuilder(base, goal, projection, safeTarget, manualChoices, recipeDefaults);
        return builder.build();
    }

    public SlotWorkspaceViewModel.AtlasItem atlasItem(SlotWorkspaceViewModel.IdentityRef identity) {
        if (identity == null) {
            return null;
        }
        for (SlotWorkspaceViewModel.AtlasItem item : atlasItems) {
            if (identity.equals(item.identity())) {
                return item;
            }
        }
        return null;
    }

    public GoalProjectionEntry entry(SlotWorkspaceViewModel.AtlasItem item) {
        return item == null ? null : entryByIdentity.get(item.identity());
    }

    public boolean choiceInvolved(SlotWorkspaceViewModel.AtlasItem item) {
        if (item == null) {
            return false;
        }
        if (choiceIdentities.contains(item.identity())) {
            return true;
        }
        GoalProjectionEntry entry = entry(item);
        return entry != null && entry.choiceIndicator();
    }

    public boolean choiceCard(SlotWorkspaceViewModel.AtlasItem item) {
        GoalProjectionEntry entry = entry(item);
        return entry != null && entry.kind() == GoalProjectionEntryKind.CHOICE_CARD;
    }

    public boolean suppressVanillaTooltip(SlotWorkspaceViewModel.AtlasItem item) {
        GoalProjectionEntry entry = entry(item);
        if (entry == null) {
            return false;
        }
        if (entry.kind() == GoalProjectionEntryKind.CHOICE_CARD) {
            return true;
        }
        return entry.identity() != null
                && !ItemIdentityMatcher.matchesMovable(entry.identity(), CHOICE_PLACEHOLDER_ID)
                && isChoicePlaceholderStack(item == null ? ItemStack.EMPTY : item.displayStack());
    }

    public List<GoalStackDescriptor> choiceAlternatives(SlotWorkspaceViewModel.AtlasItem item) {
        GoalProjectionEntry entry = entry(item);
        if (entry == null || entry.choiceGroupId().isBlank()) {
            return List.of();
        }
        return choiceAlternativesByGroup.getOrDefault(entry.choiceGroupId(), List.of());
    }

    public boolean hasManualChoice(SlotWorkspaceViewModel.AtlasItem item) {
        GoalProjectionEntry entry = entry(item);
        return entry != null && manualChoices.hasChoice(entry.choiceGroupId());
    }

    public boolean hasChoiceControls(SlotWorkspaceViewModel.AtlasItem item) {
        GoalProjectionEntry entry = entry(item);
        return choiceInvolved(item)
                || hasManualChoice(item)
                || hasRecipeDefaultChoice(item)
                || (entry != null && !entry.choiceGroupId().isBlank());
    }

    public boolean hasRecipeDefaultChoice(SlotWorkspaceViewModel.AtlasItem item) {
        GoalProjectionEntry entry = entry(item);
        if (entry == null || entry.choiceGroupId().isBlank() || entry.identity() == null) {
            return false;
        }
        String rememberedRecipeId = recipeDefaults.recipeChoiceFor(entry.identity());
        return !rememberedRecipeId.isBlank() && rememberedRecipeId.equals(entry.producerRecipeId());
    }

    public ItemIdentity delegationIdentity(SlotWorkspaceViewModel.AtlasItem item) {
        GoalProjectionEntry entry = entry(item);
        if (entry != null && entry.kind() == GoalProjectionEntryKind.CHOICE_CARD && !entry.alternatives().isEmpty()) {
            return entry.alternatives().get(0).identity();
        }
        if (entry != null && entry.kind() == GoalProjectionEntryKind.CHOICE_CARD && entry.identity() != null) {
            return entry.identity();
        }
        return item == null ? null : item.identity().toIdentity();
    }

    public List<Component> tooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
        GoalProjectionEntry entry = entry(item);
        if (entry == null) {
            return WorkspaceItemTooltipBuilder.slotLines(item);
        }
        ArrayList<Component> lines = new ArrayList<>();
        if (suppressVanillaTooltip(item)) {
            String title = item == null || item.name().isBlank() ? entry.label() : item.name();
            lines.add(Component.literal(title));
        }
        lines.addAll(WorkspaceItemTooltipBuilder.slotLines(item));
        lines.add(Component.empty());
        lines.add(Component.literal("SLOT goal"));
        if (entry.kind() == GoalProjectionEntryKind.CHOICE_CARD) {
            lines.add(Component.literal("Choice required: " + entry.label()));
            lines.add(Component.literal("Unresolved: " + entry.missingCount()));
        } else {
            lines.add(Component.literal("Required: " + entry.requiredCount()));
            int visible = entry.carriedCount() + entry.storageCount();
            lines.add(Component.literal("Visible: " + visible + " (" + entry.carriedCount()
                    + " carried, " + entry.storageCount() + " stored)"));
            if (entry.missingCount() > 0) {
                lines.add(Component.literal("Need more: " + entry.missingCount()));
            }
        }
        if (entry.choiceIndicator()) {
            lines.add(Component.literal("Recipe alternative"));
        }
        if (!entry.breadcrumbs().isEmpty()) {
            lines.add(Component.literal("Chain: " + String.join(" -> ", entry.breadcrumbs())));
        }
        if (entry.kind() == GoalProjectionEntryKind.CHOICE_CARD) {
            if (!entry.recipeId().isBlank()) {
                lines.add(Component.literal("Parent recipe: " + entry.recipeId()));
            }
            if (!entry.choiceGroupId().isBlank()) {
                lines.add(Component.literal("Choice id: " + entry.choiceGroupId()));
            }
            if (entry.alternatives().isEmpty()) {
                lines.add(Component.literal("EMI exposed no item alternatives for this input"));
            }
        }
        if (!entry.diagnostics().isEmpty()) {
            lines.add(Component.literal("Diagnostics: " + String.join(", ", entry.diagnostics())));
        }
        return List.copyOf(lines);
    }

    private static GoalRecipeDefaults recipeDefaults(SlotWorkspaceViewModel source) {
        return source == null ? GoalRecipeDefaults.empty() : source.goalRecipeDefaults();
    }

    private static boolean isChoicePlaceholderStack(ItemStack stack) {
        try {
            return ItemIdentityMatcher.matchesMovable(stack, CHOICE_PLACEHOLDER_ID);
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static GoalVisibleAuthority visibleAuthority(SlotWorkspaceViewModel source) {
        LinkedHashMap<ItemIdentity, Integer> carried = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, Integer> proximate = new LinkedHashMap<>();
        LinkedHashMap<ItemIdentity, Integer> elsewhere = new LinkedHashMap<>();
        for (SlotWorkspaceViewModel.AtlasItem item : allKnownItems(source)) {
            ItemIdentity identity = item.identity().toIdentity();
            if (identity == null) {
                continue;
            }
            if (item.carried()) {
                carried.merge(identity, item.totalCount(), Integer::sum);
            }
        }
        boolean storageFromChips = mergeTrackedStorageAuthority(source, proximate, elsewhere);
        if (!storageFromChips) {
            for (SlotWorkspaceViewModel.AtlasItem item : allKnownItems(source)) {
                ItemIdentity identity = item.identity().toIdentity();
                if (identity == null) {
                    continue;
                }
                int proximateCount = sum(item.presence());
                if (proximateCount > 0) {
                    proximate.merge(identity, proximateCount, Integer::sum);
                }
                int elsewhereCount = sum(item.elsewhere());
                if (elsewhereCount > 0) {
                    elsewhere.merge(identity, elsewhereCount, Integer::sum);
                }
            }
        }
        return GoalVisibleAuthority.fromCounts(carried, proximate, elsewhere);
    }

    private static boolean mergeTrackedStorageAuthority(
            SlotWorkspaceViewModel source,
            LinkedHashMap<ItemIdentity, Integer> proximate,
            LinkedHashMap<ItemIdentity, Integer> elsewhere
    ) {
        if (source == null || source.chestChips().isEmpty()) {
            return false;
        }
        boolean found = false;
        for (SlotWorkspaceViewModel.ChestChip chip : source.chestChips()) {
            if (chip == null || chip.contents().isEmpty()) {
                continue;
            }
            LinkedHashMap<ItemIdentity, Integer> target = chip.proximate() ? proximate : elsewhere;
            for (SlotWorkspaceViewModel.ChestContentSummary summary : chip.contents()) {
                if (summary == null || summary.count() <= 0) {
                    continue;
                }
                ItemIdentity identity = chestSummaryIdentity(summary);
                if (identity == null) {
                    continue;
                }
                target.merge(identity, summary.count(), Integer::sum);
                found = true;
            }
        }
        return found;
    }

    private static ItemIdentity chestSummaryIdentity(SlotWorkspaceViewModel.ChestContentSummary summary) {
        if (summary == null) {
            return null;
        }
        ItemStack displayStack = summary.displayStack();
        if (displayStack != null && !displayStack.isEmpty()) {
            return ItemIdentityMatcher.create(displayStack);
        }
        if (summary.itemId().isBlank()) {
            return null;
        }
        return summary.componentFingerprint().isBlank()
                ? ItemIdentity.of(summary.itemId())
                : ItemIdentity.exact(summary.itemId(), summary.componentFingerprint());
    }

    private static List<SlotWorkspaceViewModel.AtlasItem> allKnownItems(SlotWorkspaceViewModel source) {
        ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        if (source != null) {
            items.addAll(source.atlasItems());
            items.addAll(source.triageItems());
        }
        return items;
    }

    private static int sum(List<SlotWorkspaceViewModel.ChestPresenceEntry> entries) {
        int total = 0;
        if (entries == null) {
            return 0;
        }
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : entries) {
            total += entry == null ? 0 : Math.max(0, entry.count());
        }
        return total;
    }

    private static Map<String, List<GoalStackDescriptor>> choiceAlternativesByGroup(GoalDescriptor goal) {
        if (goal == null || goal.recipes().isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, List<GoalStackDescriptor>> result = new LinkedHashMap<>();
        for (GoalRecipeDescriptor recipe : goal.recipes()) {
            for (GoalIngredientDescriptor ingredient : recipe.inputs()) {
                if (ingredient.choiceRequired() || ingredient.alternatives().size() > 1) {
                    result.putIfAbsent(
                            GoalChoiceKeys.ingredientChoiceGroupId(recipe.recipeId(), ingredient.ingredientId()),
                            ingredient.alternatives());
                }
            }
        }
        return copyChoiceAlternatives(result);
    }

    private static Map<String, List<GoalStackDescriptor>> copyChoiceAlternatives(
            Map<String, List<GoalStackDescriptor>> source
    ) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, List<GoalStackDescriptor>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<GoalStackDescriptor>> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }
            copy.put(entry.getKey().trim(), entry.getValue() == null ? List.of() : List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    private static GoalDescriptor fixtureGoal(int targetCount) {
        ItemIdentity cokeOven = ItemIdentity.of("minecraft:blast_furnace");
        ItemIdentity brickBlock = ItemIdentity.of("minecraft:bricks");
        ItemIdentity brick = ItemIdentity.of("minecraft:brick");
        ItemIdentity sand = ItemIdentity.of("minecraft:sand");
        ItemIdentity waterBucket = ItemIdentity.of("minecraft:water_bucket");
        ItemIdentity clayBall = ItemIdentity.of("minecraft:clay_ball");
        ItemIdentity terracotta = ItemIdentity.of("minecraft:terracotta");
        return new GoalDescriptor(
                FIXTURE_GOAL_ID,
                "Coke Oven",
                List.of(stack(cokeOven, "Coke Oven", 1)),
                targetCount,
                "fixture:recipe/coke_oven",
                "fixture:crafting",
                List.of(
                        recipe(
                                "fixture:recipe/coke_oven",
                                stack(cokeOven, "Coke Oven", 1),
                                GoalIngredientDescriptor.concrete(
                                        "brick_blocks",
                                        stack(brickBlock, "Coke Brick Block", 1),
                                        3
                                )
                        ),
                        recipe(
                                "fixture:recipe/coke_brick_block",
                                stack(brickBlock, "Coke Brick Block", 1),
                                GoalIngredientDescriptor.concrete(
                                        "bricks",
                                        stack(brick, "Coke Brick", 1),
                                        4
                                )
                        ),
                        recipe(
                                "fixture:recipe/coke_brick",
                                stack(brick, "Coke Brick", 1),
                                GoalIngredientDescriptor.concrete("sand", stack(sand, "Sand", 1), 1),
                                GoalIngredientDescriptor.concrete(
                                        "water",
                                        stack(waterBucket, "Water Bucket", 1),
                                        1
                                ),
                                GoalIngredientDescriptor.choice(
                                        "binder",
                                        "Any clay binder",
                                        1,
                                        "#forge:clay_binders",
                                        List.of(
                                                stack(clayBall, "Clay Ball", 1),
                                                stack(terracotta, "Terracotta", 1)
                                        )
                                )
                        )
                )
        );
    }

    private static GoalRecipeDescriptor recipe(
            String recipeId,
            GoalStackDescriptor output,
            GoalIngredientDescriptor... inputs
    ) {
        return new GoalRecipeDescriptor(
                recipeId,
                "fixture:crafting",
                true,
                List.of(output),
                List.of(inputs),
                List.of(),
                List.of()
        );
    }

    private static GoalStackDescriptor stack(ItemIdentity identity, String label, int count) {
        return new GoalStackDescriptor(identity, label, count);
    }

    private static final class ProjectionBuilder {
        private final SlotWorkspaceViewModel source;
        private final GoalDescriptor goal;
        private final GoalProjection projection;
        private final int targetCount;
        private final GoalChoiceResolution manualChoices;
        private final GoalRecipeDefaults recipeDefaults;
        private final Map<String, List<GoalStackDescriptor>> choiceAlternativesByGroup;
        private final LinkedHashMap<String, SlotWorkspaceViewModel.AtlasIsland> islandsById = new LinkedHashMap<>();
        private final ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        private final LinkedHashMap<SlotWorkspaceViewModel.IdentityRef, GoalProjectionEntry> entryByIdentity =
                new LinkedHashMap<>();
        private final LinkedHashSet<SlotWorkspaceViewModel.IdentityRef> choiceIdentities = new LinkedHashSet<>();

        private ProjectionBuilder(
                SlotWorkspaceViewModel source,
                GoalDescriptor goal,
                GoalProjection projection,
                int targetCount,
                GoalChoiceResolution manualChoices,
                GoalRecipeDefaults recipeDefaults
        ) {
            this.source = source == null ? SlotWorkspaceViewModel.empty() : source;
            this.goal = goal;
            this.projection = projection;
            this.targetCount = targetCount;
            this.manualChoices = manualChoices == null ? GoalChoiceResolution.empty() : manualChoices;
            this.recipeDefaults = recipeDefaults == null ? GoalRecipeDefaults.empty() : recipeDefaults;
            this.choiceAlternativesByGroup = choiceAlternativesByGroup(goal);
        }

        private GoalWorkspaceProjection build() {
            for (GoalProjectionEntry entry : aggregateEntries(projection.entries())) {
                if (entry.kind() == GoalProjectionEntryKind.CHOICE_CARD) {
                    addChoiceCard(entry);
                } else if (entry.identity() != null) {
                    addRequirementCard(entry);
                }
            }
            if (items.isEmpty()) {
                ensureFallbackSection();
            }
            return new GoalWorkspaceProjection(
                    projection.goalId(),
                    projection.label(),
                    targetCount,
                    projection,
                    List.copyOf(islandsById.values()),
                    List.copyOf(items),
                    entryByIdentity,
                    choiceIdentities,
                    choiceAlternativesByGroup,
                    manualChoices,
                    recipeDefaults
            );
        }

        private void addRequirementCard(GoalProjectionEntry entry) {
            SlotWorkspaceViewModel.AtlasItem existing = existingItem(entry.identity());
            String islandId = existing == null ? fallbackSectionId() : existing.islandId();
            if (islandId == null || islandId.isBlank() || SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)) {
                islandId = fallbackSectionId();
            }
            ensureIsland(islandId, existing);
            ItemStack displayStack = existing == null || existing.displayStack().isEmpty()
                    ? displayStack(entry.identity())
                    : existing.displayStack();
            if (existing == null && syntheticEmiIdentity(entry.identity())) {
                displayStack = namedStack(displayStack, entry.label());
            }
            if (displayStack.isEmpty()) {
                logPlaceholderCard(entry, entry.identity());
                displayStack = placeholderDisplayStack("Missing: " + entry.label());
            }
            if (displayStack.isEmpty()) {
                logSkippedBlankCard(entry, entry.identity());
                return;
            }
            int carried = Math.max(0, entry.carriedCount());
            int storage = Math.max(0, entry.storageCount());
            SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                    SlotWorkspaceViewModel.IdentityRef.from(entry.identity()),
                    displayStack,
                    entry.label(),
                    carried > 0 ? carried : storage,
                    existing == null ? 0 : existing.firstSlotIndex(),
                    islandId,
                    existing != null && existing.recent(),
                    existing != null && existing.playerPlaced(),
                    carried > 0,
                    carried <= 0,
                    storage,
                    existing == null ? List.of() : existing.chipSuggestions(),
                    presence("Known goal stock", storage),
                    List.of(),
                    existing != null && existing.isCarriedContainer(),
                    existing == null ? 0 : existing.containerFreeSlotCount(),
                    existing == null ? 0 : existing.containerSlotCapacity(),
                    false,
                    0,
                    false,
                    entry.wantedCount(),
                    existing == null ? "" : existing.largestCarriedSourceId(),
                    existing == null ? -1 : existing.largestCarriedSlotIndex(),
                    existing == null ? 0 : existing.largestCarriedSlotCount()
            );
            addItem(item, entry);
            if (entry.choiceIndicator()) {
                choiceIdentities.add(item.identity());
            }
        }

        private void addChoiceCard(GoalProjectionEntry entry) {
            ensureFallbackSection();
            ItemIdentity synthetic = ItemIdentity.of("slot:goal_choice/" + sanitize(entry.choiceGroupId()));
            ItemStack displayStack = choiceDisplayStack(entry);
            if (displayStack.isEmpty()) {
                logSkippedBlankCard(entry, synthetic);
                return;
            }
            SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                    SlotWorkspaceViewModel.IdentityRef.from(synthetic),
                    displayStack,
                    entry.label(),
                    0,
                    0,
                    fallbackSectionId(),
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
                    0,
                    "",
                    -1,
                    0
            );
            addItem(item, entry);
            choiceIdentities.add(item.identity());
        }

        private void addItem(SlotWorkspaceViewModel.AtlasItem item, GoalProjectionEntry entry) {
            items.add(item);
            entryByIdentity.put(item.identity(), entry);
        }

        private SlotWorkspaceViewModel.AtlasItem existingItem(ItemIdentity identity) {
            if (identity == null) {
                return null;
            }
            SlotWorkspaceViewModel.IdentityRef ref = SlotWorkspaceViewModel.IdentityRef.from(identity);
            SlotWorkspaceViewModel.AtlasItem item = source.atlasItem(ref);
            if (item != null) {
                return item;
            }
            for (SlotWorkspaceViewModel.AtlasItem triage : source.triageItems()) {
                if (ref.equals(triage.identity())) {
                    return triage;
                }
            }
            return null;
        }

        private void ensureIsland(String islandId, SlotWorkspaceViewModel.AtlasItem existing) {
            if (fallbackSectionId().equals(islandId)) {
                ensureFallbackSection();
                return;
            }
            if (islandsById.containsKey(islandId)) {
                return;
            }
            SlotWorkspaceViewModel.AtlasIsland island = source.island(islandId);
            if (island == null) {
                ensureFallbackSection();
                return;
            }
            islandsById.put(islandId, island);
        }

        private void ensureFallbackSection() {
            SlotWorkspaceViewModel.AtlasIsland existing = source.island(fallbackSectionId());
            islandsById.putIfAbsent(fallbackSectionId(), existing == null ? new SlotWorkspaceViewModel.AtlasIsland(
                    fallbackSectionId(),
                    SlotWorkspaceAtlasLayout.ISLAND_MISC_LABEL,
                    VisualAtlasIslandKind.PLAYER,
                    0,
                    0,
                    SlotWorkspaceAtlasLayout.ISLAND_MISC_COLOR,
                    0,
                    0
            ) : existing);
        }

        private static String fallbackSectionId() {
            return SlotWorkspaceAtlasLayout.ISLAND_MISC;
        }

        private static List<SlotWorkspaceViewModel.ChestPresenceEntry> presence(String label, int count) {
            if (count <= 0) {
                return List.of();
            }
            return List.of(new SlotWorkspaceViewModel.ChestPresenceEntry("goal:projection", label, count));
        }

        private static ItemStack displayStack(ItemIdentity identity) {
            ItemStack stack = SlotWorkspaceViewModel.displayStackForIdentity(identity);
            return stack == null ? ItemStack.EMPTY : stack;
        }

        private static ItemStack choiceDisplayStack(GoalProjectionEntry entry) {
            String hoverName = "Choice: " + entry.label();
            for (GoalStackDescriptor alternative : entry.alternatives()) {
                ItemStack stack = displayStack(alternative.identity());
                if (!stack.isEmpty()) {
                    return namedStack(stack, hoverName);
                }
            }
            if (entry.identity() != null) {
                ItemStack stack = displayStack(entry.identity());
                if (!stack.isEmpty()) {
                    return namedStack(stack, hoverName);
                }
            }
            return placeholderDisplayStack(hoverName);
        }

        private static ItemStack placeholderDisplayStack(String hoverName) {
            return namedStack(displayStack(CHOICE_PLACEHOLDER_ID), hoverName);
        }

        private static boolean syntheticEmiIdentity(ItemIdentity identity) {
            return identity != null && identity.itemId().startsWith("slot:emi/");
        }

        private static ItemStack namedStack(ItemStack stack, String hoverName) {
            if (stack == null || stack.isEmpty() || hoverName == null || hoverName.isBlank()) {
                return stack == null ? ItemStack.EMPTY : stack;
            }
            ItemStack named = stack.copy();
            applyHoverName(named, Component.literal(hoverName.trim()));
            return named;
        }

        private static void applyHoverName(ItemStack stack, Component hoverName) {
            try {
                java.lang.reflect.Method setHoverName = ItemStack.class.getMethod("setHoverName", Component.class);
                setHoverName.invoke(stack, hoverName);
                return;
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                // Minecraft 1.21 stores custom names in data components instead of exposing setHoverName.
            }
            try {
                Class<?> dataComponents = Class.forName("net.minecraft.core.component.DataComponents");
                Object customName = dataComponents.getField("CUSTOM_NAME").get(null);
                for (java.lang.reflect.Method method : ItemStack.class.getMethods()) {
                    if (!"set".equals(method.getName()) || method.getParameterCount() != 2) {
                        continue;
                    }
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes[0].isInstance(customName) && parameterTypes[1].isInstance(hoverName)) {
                        method.invoke(stack, customName, hoverName);
                        return;
                    }
                }
            } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
                // Display stack naming is best-effort; SLOT tooltip lines still carry the goal details.
            }
        }

        private static List<GoalProjectionEntry> aggregateEntries(List<GoalProjectionEntry> entries) {
            LinkedHashMap<ItemIdentity, GoalProjectionEntry> requirements = new LinkedHashMap<>();
            LinkedHashMap<String, GoalProjectionEntry> choices = new LinkedHashMap<>();
            if (entries == null) {
                return List.of();
            }
            for (GoalProjectionEntry entry : entries) {
                if (entry == null) {
                    continue;
                }
                if (entry.kind() == GoalProjectionEntryKind.CHOICE_CARD) {
                    String key = entry.choiceGroupId().isBlank() ? "choice:" + choices.size() : entry.choiceGroupId();
                    choices.merge(key, entry, ProjectionBuilder::mergeEntry);
                    continue;
                }
                if (entry.identity() == null) {
                    continue;
                }
                ItemIdentity key = matchingRequirementKey(requirements, entry.identity());
                requirements.merge(key, entry, ProjectionBuilder::mergeEntry);
            }
            ArrayList<GoalProjectionEntry> result = new ArrayList<>(requirements.size() + choices.size());
            result.addAll(requirements.values());
            result.addAll(choices.values());
            return List.copyOf(result);
        }

        private static ItemIdentity matchingRequirementKey(
                LinkedHashMap<ItemIdentity, GoalProjectionEntry> requirements,
                ItemIdentity identity
        ) {
            for (ItemIdentity key : requirements.keySet()) {
                if (ItemIdentityMatcher.matchesMovable(key, identity)) {
                    return key;
                }
            }
            return identity;
        }

        private static GoalProjectionEntry mergeEntry(GoalProjectionEntry left, GoalProjectionEntry right) {
            GoalProjectionEntryKind kind = mergedKind(left, right);
            return new GoalProjectionEntry(
                    kind,
                    left.identity() != null ? left.identity() : right.identity(),
                    !left.label().isBlank() ? left.label() : right.label(),
                    !left.recipeId().isBlank() ? left.recipeId() : right.recipeId(),
                    !left.ingredientId().isBlank() ? left.ingredientId() : right.ingredientId(),
                    !left.serializedIngredient().isBlank() ? left.serializedIngredient() : right.serializedIngredient(),
                    !left.producerRecipeId().isBlank() ? left.producerRecipeId() : right.producerRecipeId(),
                    left.requiredCount() + right.requiredCount(),
                    left.carriedCount() + right.carriedCount(),
                    left.storageCount() + right.storageCount(),
                    left.missingCount() + right.missingCount(),
                    left.wantedCount() + right.wantedCount(),
                    left.choiceIndicator() || right.choiceIndicator(),
                    !left.choiceGroupId().isBlank() ? left.choiceGroupId() : right.choiceGroupId(),
                    mergeAlternatives(left.alternatives(), right.alternatives()),
                    mergeStrings(left.breadcrumbs(), right.breadcrumbs()),
                    mergeStrings(left.diagnostics(), right.diagnostics())
            );
        }

        private static GoalProjectionEntryKind mergedKind(GoalProjectionEntry left, GoalProjectionEntry right) {
            if (left.kind() == GoalProjectionEntryKind.CHOICE_CARD || right.kind() == GoalProjectionEntryKind.CHOICE_CARD) {
                return GoalProjectionEntryKind.CHOICE_CARD;
            }
            int carried = left.carriedCount() + right.carriedCount();
            int storage = left.storageCount() + right.storageCount();
            if (carried > 0) {
                return GoalProjectionEntryKind.REAL_CARD;
            }
            return storage > 0 ? GoalProjectionEntryKind.STORAGE_GHOST : GoalProjectionEntryKind.MISSING_GHOST;
        }

        private static List<GoalStackDescriptor> mergeAlternatives(
                List<GoalStackDescriptor> left,
                List<GoalStackDescriptor> right
        ) {
            LinkedHashMap<ItemIdentity, GoalStackDescriptor> merged = new LinkedHashMap<>();
            for (GoalStackDescriptor stack : left) {
                merged.putIfAbsent(stack.identity(), stack);
            }
            for (GoalStackDescriptor stack : right) {
                merged.putIfAbsent(stack.identity(), stack);
            }
            return List.copyOf(merged.values());
        }

        private static List<String> mergeStrings(List<String> left, List<String> right) {
            LinkedHashSet<String> merged = new LinkedHashSet<>();
            for (String value : left) {
                if (value != null && !value.isBlank()) {
                    merged.add(value.trim());
                }
            }
            for (String value : right) {
                if (value != null && !value.isBlank()) {
                    merged.add(value.trim());
                }
            }
            return List.copyOf(merged);
        }

        private void logSkippedBlankCard(GoalProjectionEntry entry, ItemIdentity displayIdentity) {
            SlotDebugLog.verboseLog(
                    "[goal] skipped blank goal card goal={} entryKind={} identity={} label={} recipe={} producer={} choice={}",
                    goal == null ? projection.goalId() : goal.goalId(),
                    entry.kind(),
                    displayIdentity == null ? "" : displayIdentity.itemId(),
                    entry.label(),
                    entry.recipeId(),
                    entry.producerRecipeId(),
                    entry.choiceGroupId());
        }

        private void logPlaceholderCard(GoalProjectionEntry entry, ItemIdentity displayIdentity) {
            SlotDebugLog.verboseLog(
                    "[goal] rendering placeholder goal card goal={} entryKind={} identity={} label={} recipe={} producer={} choice={}",
                    goal == null ? projection.goalId() : goal.goalId(),
                    entry.kind(),
                    displayIdentity == null ? "" : displayIdentity.itemId(),
                    entry.label(),
                    entry.recipeId(),
                    entry.producerRecipeId(),
                    entry.choiceGroupId());
        }

        private static String sanitize(String value) {
            String input = value == null || value.isBlank() ? "choice" : value;
            String sanitized = input.toLowerCase(java.util.Locale.ROOT)
                    .replaceAll("[^a-z0-9_./-]", "_");
            return sanitized.isBlank() ? "choice" : sanitized;
        }
    }
}
