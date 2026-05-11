package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalProjection;
import dev.imagio.slot.inventory.goal.GoalProjectionEntry;
import dev.imagio.slot.inventory.goal.GoalProjectionEntryKind;
import dev.imagio.slot.inventory.goal.GoalProjectionOptions;
import dev.imagio.slot.inventory.goal.GoalProjectionService;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
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
        Set<SlotWorkspaceViewModel.IdentityRef> choiceIdentities
) {
    public static final String FIXTURE_GOAL_ID = "fixture:coke_oven";
    public static final String GOAL_SECTION_ID = "goal.requirements";

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
    }

    public static GoalWorkspaceProjection fixture(SlotWorkspaceViewModel source, int targetCount) {
        int safeTarget = Math.max(1, targetCount);
        SlotWorkspaceViewModel base = source == null ? SlotWorkspaceViewModel.empty() : source;
        GoalDescriptor goal = fixtureGoal(safeTarget);
        GoalProjection projection = new GoalProjectionService().project(
                goal,
                visibleAuthority(base),
                GoalProjectionOptions.defaults());
        ProjectionBuilder builder = new ProjectionBuilder(base, projection, safeTarget);
        return builder.build();
    }

    public static GoalWorkspaceProjection fromGoal(
            SlotWorkspaceViewModel source,
            GoalDescriptor descriptor,
            int targetCount
    ) {
        if (descriptor == null) {
            return null;
        }
        int safeTarget = Math.max(1, targetCount);
        SlotWorkspaceViewModel base = source == null ? SlotWorkspaceViewModel.empty() : source;
        GoalDescriptor goal = GoalWorkspaceClientState.withTargetCount(descriptor, safeTarget);
        GoalProjection projection = new GoalProjectionService().project(
                goal,
                visibleAuthority(base),
                GoalProjectionOptions.defaults());
        ProjectionBuilder builder = new ProjectionBuilder(base, projection, safeTarget);
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

    public List<Component> tooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
        GoalProjectionEntry entry = entry(item);
        if (entry == null) {
            return WorkspaceItemTooltipBuilder.slotLines(item);
        }
        ArrayList<Component> lines = new ArrayList<>();
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
        if (!entry.diagnostics().isEmpty()) {
            lines.add(Component.literal("Diagnostics: " + String.join(",", entry.diagnostics())));
        }
        return List.copyOf(lines);
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
            int proximateCount = sum(item.presence());
            if (proximateCount > 0) {
                proximate.merge(identity, proximateCount, Integer::sum);
            }
            int elsewhereCount = sum(item.elsewhere());
            if (elsewhereCount > 0) {
                elsewhere.merge(identity, elsewhereCount, Integer::sum);
            }
        }
        return GoalVisibleAuthority.fromCounts(carried, proximate, elsewhere);
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
        private final GoalProjection projection;
        private final int targetCount;
        private final LinkedHashMap<String, SlotWorkspaceViewModel.AtlasIsland> islandsById = new LinkedHashMap<>();
        private final ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        private final LinkedHashMap<SlotWorkspaceViewModel.IdentityRef, GoalProjectionEntry> entryByIdentity =
                new LinkedHashMap<>();
        private final LinkedHashSet<SlotWorkspaceViewModel.IdentityRef> choiceIdentities = new LinkedHashSet<>();

        private ProjectionBuilder(SlotWorkspaceViewModel source, GoalProjection projection, int targetCount) {
            this.source = source == null ? SlotWorkspaceViewModel.empty() : source;
            this.projection = projection;
            this.targetCount = targetCount;
        }

        private GoalWorkspaceProjection build() {
            for (GoalProjectionEntry entry : projection.entries()) {
                if (entry.kind() == GoalProjectionEntryKind.CHOICE_CARD) {
                    addChoiceCard(entry);
                } else if (entry.identity() != null) {
                    addRequirementCard(entry);
                }
            }
            if (items.isEmpty()) {
                ensureGoalSection();
            }
            return new GoalWorkspaceProjection(
                    projection.goalId(),
                    projection.label(),
                    targetCount,
                    projection,
                    List.copyOf(islandsById.values()),
                    List.copyOf(items),
                    entryByIdentity,
                    choiceIdentities
            );
        }

        private void addRequirementCard(GoalProjectionEntry entry) {
            SlotWorkspaceViewModel.AtlasItem existing = existingItem(entry.identity());
            String islandId = existing == null ? GOAL_SECTION_ID : existing.islandId();
            if (islandId == null || islandId.isBlank() || SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)) {
                islandId = GOAL_SECTION_ID;
            }
            ensureIsland(islandId, existing);
            ItemStack displayStack = existing == null || existing.displayStack().isEmpty()
                    ? displayStack(entry.identity())
                    : existing.displayStack();
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
                    Math.max(entry.requiredCount(), entry.desiredCount()),
                    false,
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
            ensureGoalSection();
            ItemIdentity synthetic = ItemIdentity.of("slot:goal_choice/" + sanitize(entry.choiceGroupId()));
            GoalStackDescriptor first = entry.alternatives().isEmpty() ? null : entry.alternatives().get(0);
            ItemStack displayStack = first == null ? ItemStack.EMPTY : displayStack(first.identity());
            SlotWorkspaceViewModel.AtlasItem item = new SlotWorkspaceViewModel.AtlasItem(
                    SlotWorkspaceViewModel.IdentityRef.from(synthetic),
                    displayStack,
                    entry.label(),
                    0,
                    0,
                    GOAL_SECTION_ID,
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
                    Math.max(entry.missingCount(), entry.requiredCount()),
                    false,
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
            if (GOAL_SECTION_ID.equals(islandId)) {
                ensureGoalSection();
                return;
            }
            if (islandsById.containsKey(islandId)) {
                return;
            }
            SlotWorkspaceViewModel.AtlasIsland island = source.island(islandId);
            if (island == null) {
                ensureGoalSection();
                return;
            }
            islandsById.put(islandId, island);
        }

        private void ensureGoalSection() {
            islandsById.putIfAbsent(GOAL_SECTION_ID, new SlotWorkspaceViewModel.AtlasIsland(
                    GOAL_SECTION_ID,
                    "Goal",
                    VisualAtlasIslandKind.PLAYER,
                    0,
                    0,
                    0xFF7AC7A7,
                    0,
                    0
            ));
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

        private static String sanitize(String value) {
            String input = value == null || value.isBlank() ? "choice" : value;
            String sanitized = input.toLowerCase(java.util.Locale.ROOT)
                    .replaceAll("[^a-z0-9_./-]", "_");
            return sanitized.isBlank() ? "choice" : sanitized;
        }
    }
}
