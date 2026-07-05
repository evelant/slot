package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityCollections;
import dev.imagio.slot.inventory.workspace.RemoteDetailIdentityPayload;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Transient recipe-ingredient view for sidebar mode. It is deliberately not a
 * goal or planning model: recipe viewers own recipe explanation, while this
 * projection answers "which of this visible recipe's ingredients do I already
 * have in carried or known storage?"
 */
public record RecipeIngredientSidebarSpec(
        String sourceKey,
        String label,
        List<Ingredient> ingredients
) {
    private static final ItemIdentity PLACEHOLDER_ID = ItemIdentity.of("minecraft:knowledge_book");

    public RecipeIngredientSidebarSpec {
        sourceKey = sourceKey == null ? "" : sourceKey;
        label = label == null || label.isBlank() ? "Recipe ingredients" : label;
        ingredients = ingredients == null ? List.of() : List.copyOf(ingredients);
    }

    public static RecipeIngredientSidebarSpec empty() {
        return new RecipeIngredientSidebarSpec("", "", List.of());
    }

    public boolean active() {
        return !sourceKey.isBlank() && !ingredients.isEmpty();
    }

    public Projection project(SlotWorkspaceViewModel source) {
        if (!active()) {
            return null;
        }
        ProjectionBuilder builder = new ProjectionBuilder(source, this);
        return builder.build();
    }

    public Set<ItemIdentity> remoteDetailIdentities() {
        if (!active()) {
            return Set.of();
        }
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient == null) {
                continue;
            }
            for (Alternative alternative : ingredient.alternatives()) {
                if (alternative != null) {
                    ItemIdentityCollections.add(identities, alternative.identity());
                }
            }
        }
        return identities.isEmpty() ? Set.of() : Collections.unmodifiableSet(identities);
    }

    public String remoteDetailIdentityPayload() {
        return RemoteDetailIdentityPayload.encode(remoteDetailIdentities());
    }

    public record Ingredient(
            String ingredientId,
            String label,
            int requiredCount,
            List<Alternative> alternatives
    ) {
        public Ingredient {
            ingredientId = ingredientId == null || ingredientId.isBlank() ? "ingredient" : ingredientId;
            label = label == null || label.isBlank() ? ingredientId : label;
            requiredCount = Math.max(1, requiredCount);
            alternatives = alternatives == null ? List.of() : List.copyOf(alternatives);
        }
    }

    public record Alternative(
            ItemIdentity identity,
            String label,
            int requiredCount,
            ItemStack displayStack
    ) {
        public Alternative {
            label = label == null || label.isBlank()
                    ? identity == null ? "Ingredient" : identity.itemId()
                    : label;
            requiredCount = Math.max(1, requiredCount);
            displayStack = displayStack == null ? ItemStack.EMPTY : displayStack.copy();
        }
    }

    public record Projection(
            String sourceKey,
            String label,
            List<SlotWorkspaceViewModel.AtlasIsland> islands,
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems,
            Map<SlotWorkspaceViewModel.IdentityRef, Ingredient> ingredientByIdentity
    ) {
        public Projection {
            sourceKey = sourceKey == null ? "" : sourceKey;
            label = label == null || label.isBlank() ? "Recipe ingredients" : label;
            islands = islands == null ? List.of() : List.copyOf(islands);
            atlasItems = atlasItems == null ? List.of() : List.copyOf(atlasItems);
            ingredientByIdentity = ingredientByIdentity == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new LinkedHashMap<>(ingredientByIdentity));
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

        public Ingredient ingredient(SlotWorkspaceViewModel.AtlasItem item) {
            return item == null ? null : ingredientByIdentity.get(item.identity());
        }

        public boolean suppressVanillaTooltip(SlotWorkspaceViewModel.AtlasItem item) {
            Ingredient ingredient = ingredient(item);
            return ingredient != null
                    && item != null
                    && item.identity().itemId().startsWith("slot:recipe_ingredient/");
        }

        public List<Component> tooltipLines(SlotWorkspaceViewModel.AtlasItem item) {
            Ingredient ingredient = ingredient(item);
            if (ingredient == null) {
                return WorkspaceItemTooltipBuilder.slotLines(item);
            }
            ArrayList<Component> lines = new ArrayList<>();
            if (suppressVanillaTooltip(item)) {
                lines.add(Component.literal(ingredient.label()));
            }
            lines.addAll(WorkspaceItemTooltipBuilder.slotLines(item));
            lines.add(Component.empty());
            lines.add(Component.literal("EMI recipe ingredient"));
            lines.add(Component.literal("Required: " + ingredient.requiredCount()));
            if (ingredient.alternatives().size() > 1) {
                lines.add(Component.literal("Alternatives: " + ingredient.alternatives().size()));
            }
            return List.copyOf(lines);
        }
    }

    private static final class ProjectionBuilder {
        private final SlotWorkspaceViewModel source;
        private final RecipeIngredientSidebarSpec spec;
        private final LinkedHashMap<String, SlotWorkspaceViewModel.AtlasIsland> islandsById = new LinkedHashMap<>();
        private final LinkedHashMap<SlotWorkspaceViewModel.IdentityRef, SlotWorkspaceViewModel.AtlasItem> itemsByIdentity =
                new LinkedHashMap<>();
        private final LinkedHashMap<SlotWorkspaceViewModel.IdentityRef, Ingredient> ingredientByIdentity =
                new LinkedHashMap<>();

        private ProjectionBuilder(SlotWorkspaceViewModel source, RecipeIngredientSidebarSpec spec) {
            this.source = source == null ? SlotWorkspaceViewModel.empty() : source;
            this.spec = spec;
        }

        private Projection build() {
            for (Ingredient ingredient : spec.ingredients()) {
                addIngredient(ingredient);
            }
            if (itemsByIdentity.isEmpty()) {
                ensureFallbackSection();
            }
            return new Projection(
                    spec.sourceKey(),
                    spec.label(),
                    List.copyOf(islandsById.values()),
                    List.copyOf(itemsByIdentity.values()),
                    ingredientByIdentity);
        }

        private void addIngredient(Ingredient ingredient) {
            if (ingredient == null) {
                return;
            }
            ArrayList<ResolvedAlternative> present = new ArrayList<>();
            for (Alternative alternative : ingredient.alternatives()) {
                if (alternative == null || alternative.identity() == null) {
                    continue;
                }
                SlotWorkspaceViewModel.AtlasItem existing = existingItem(alternative.identity());
                if (existing != null && visibleCount(existing) > 0) {
                    present.add(new ResolvedAlternative(alternative, existing));
                }
            }
            if (!present.isEmpty()) {
                for (ResolvedAlternative alternative : present) {
                    addCard(ingredient, alternative.alternative(), alternative.existing());
                }
                return;
            }
            addMissingCard(ingredient);
        }

        private void addCard(Ingredient ingredient, Alternative alternative, SlotWorkspaceViewModel.AtlasItem existing) {
            String islandId = existing == null ? fallbackSectionId() : existing.islandId();
            if (islandId == null || islandId.isBlank() || SlotWorkspaceAtlasLayout.ISLAND_TRIAGE.equals(islandId)) {
                islandId = fallbackSectionId();
            }
            ensureIsland(islandId);
            ItemStack displayStack = existing == null ? displayStack(alternative) : existing.displayStack();
            if (displayStack == null || displayStack.isEmpty()) {
                displayStack = placeholderDisplayStack(ingredient.label());
            }
            if (displayStack.isEmpty()) {
                return;
            }
            SlotWorkspaceViewModel.AtlasItem item = existing == null
                    ? missingItem(ingredient, alternative, displayStack, islandId)
                    : withRecipeTarget(existing, ingredient.requiredCount(), islandId);
            addOrMerge(item, ingredient);
        }

        private void addMissingCard(Ingredient ingredient) {
            ensureFallbackSection();
            Alternative alternative = missingAlternative(ingredient);
            ItemIdentity identity = alternative == null || alternative.identity() == null
                    ? ItemIdentity.of("slot:recipe_ingredient/" + sanitize(ingredient.ingredientId()))
                    : alternative.identity();
            SlotWorkspaceViewModel.AtlasItem existing = existingItem(identity);
            if (existing != null && !existing.displayStack().isEmpty()) {
                addOrMerge(withRecipeTarget(existing, ingredient.requiredCount(), fallbackSectionId()), ingredient);
                return;
            }
            ItemStack displayStack = alternative == null ? ItemStack.EMPTY : displayStack(alternative);
            if (displayStack == null || displayStack.isEmpty()) {
                displayStack = displayStack(identity);
            }
            if (displayStack.isEmpty()) {
                displayStack = placeholderDisplayStack("Missing: " + ingredient.label());
            } else if (identity.itemId().startsWith("slot:recipe_ingredient/")) {
                displayStack = namedStack(displayStack, ingredient.label());
            }
            if (displayStack.isEmpty()) {
                return;
            }
            SlotWorkspaceViewModel.AtlasItem item = missingItem(
                    ingredient,
                    alternative == null
                            ? new Alternative(identity, ingredient.label(), ingredient.requiredCount(), displayStack)
                            : alternative,
                    displayStack,
                    fallbackSectionId());
            addOrMerge(item, ingredient);
        }

        private SlotWorkspaceViewModel.AtlasItem missingItem(
                Ingredient ingredient,
                Alternative alternative,
                ItemStack displayStack,
                String islandId
        ) {
            ItemIdentity identity = alternative == null || alternative.identity() == null
                    ? ItemIdentity.of("slot:recipe_ingredient/" + sanitize(ingredient.ingredientId()))
                    : alternative.identity();
            return new SlotWorkspaceViewModel.AtlasItem(
                    SlotWorkspaceViewModel.IdentityRef.from(identity),
                    displayStack,
                    ingredient.label(),
                    0,
                    0,
                    islandId,
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
                    ingredient.requiredCount(),
                    false,
                    0,
                    false,
                    false,
                    "",
                    -1,
                    0,
                    SlotWorkspaceViewModel.PutAwayState.NONE
            );
        }

        private SlotWorkspaceViewModel.AtlasItem withRecipeTarget(
                SlotWorkspaceViewModel.AtlasItem existing,
                int requiredCount,
                String islandId
        ) {
            return new SlotWorkspaceViewModel.AtlasItem(
                    existing.identity(),
                    existing.displayStack(),
                    existing.name(),
                    existing.totalCount(),
                    existing.firstSlotIndex(),
                    islandId,
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
                    Math.max(1, requiredCount),
                    false,
                    existing.wantedCount(),
                    existing.junk(),
                    existing.acceptedWorkflowInput(),
                    existing.largestCarriedSourceId(),
                    existing.largestCarriedSlotIndex(),
                    existing.largestCarriedSlotCount(),
                    existing.putAwayState()
            );
        }

        private void addOrMerge(SlotWorkspaceViewModel.AtlasItem item, Ingredient ingredient) {
            if (item == null || item.identity() == null) {
                return;
            }
            SlotWorkspaceViewModel.AtlasItem existing = itemsByIdentity.get(item.identity());
            if (existing == null) {
                itemsByIdentity.put(item.identity(), item);
                ingredientByIdentity.put(item.identity(), ingredient);
                return;
            }
            int mergedRequired = Math.max(1, existing.desiredCount()) + Math.max(1, item.desiredCount());
            SlotWorkspaceViewModel.AtlasItem merged = withRecipeTarget(existing, mergedRequired, existing.islandId());
            itemsByIdentity.put(merged.identity(), merged);
            ingredientByIdentity.put(merged.identity(), mergeIngredient(ingredientByIdentity.get(item.identity()), ingredient, mergedRequired));
        }

        private Ingredient mergeIngredient(Ingredient first, Ingredient second, int requiredCount) {
            if (first == null) {
                return second == null ? null : new Ingredient(
                        second.ingredientId(),
                        second.label(),
                        requiredCount,
                        second.alternatives());
            }
            return new Ingredient(
                    first.ingredientId(),
                    first.label(),
                    requiredCount,
                    first.alternatives());
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
                if (triage != null && ref.equals(triage.identity())) {
                    return triage;
                }
            }
            return null;
        }

        private int visibleCount(SlotWorkspaceViewModel.AtlasItem item) {
            if (item == null) {
                return 0;
            }
            int carried = item.carried() ? Math.max(0, item.totalCount()) : 0;
            return carried + presenceCount(item.presence()) + presenceCount(item.elsewhere()) + Math.max(0, item.proximateCount());
        }

        private void ensureIsland(String islandId) {
            if (islandId == null || islandId.isBlank()) {
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

        private static Alternative firstAlternative(Ingredient ingredient) {
            return ingredient == null || ingredient.alternatives().isEmpty() ? null : ingredient.alternatives().get(0);
        }

        private Alternative missingAlternative(Ingredient ingredient) {
            if (ingredient == null) {
                return null;
            }
            for (Alternative alternative : ingredient.alternatives()) {
                if (alternative == null || alternative.identity() == null) {
                    continue;
                }
                SlotWorkspaceViewModel.AtlasItem existing = existingItem(alternative.identity());
                if (existing != null
                        && (existing.wantedCount() > 0 || existing.desiredCount() > 0 || existing.kitNeeded())) {
                    return alternative;
                }
            }
            return firstAlternative(ingredient);
        }

        private static String fallbackSectionId() {
            return SlotWorkspaceAtlasLayout.ISLAND_MISC;
        }

        private static ItemStack displayStack(Alternative alternative) {
            if (alternative == null) {
                return ItemStack.EMPTY;
            }
            if (alternative.displayStack() != null && !alternative.displayStack().isEmpty()) {
                return alternative.displayStack().copy();
            }
            return displayStack(alternative.identity());
        }

        private static ItemStack displayStack(ItemIdentity identity) {
            ItemStack stack = SlotWorkspaceViewModel.displayStackForIdentity(identity);
            return stack == null ? ItemStack.EMPTY : stack;
        }

        private static ItemStack placeholderDisplayStack(String hoverName) {
            return namedStack(displayStack(PLACEHOLDER_ID), hoverName);
        }

        private static int presenceCount(List<SlotWorkspaceViewModel.ChestPresenceEntry> entries) {
            int count = 0;
            if (entries == null) {
                return 0;
            }
            for (SlotWorkspaceViewModel.ChestPresenceEntry entry : entries) {
                if (entry != null) {
                    count += Math.max(0, entry.count());
                }
            }
            return count;
        }

        private static String sanitize(String value) {
            String input = value == null || value.isBlank() ? "ingredient" : value;
            String sanitized = input.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
            return sanitized.isBlank() ? "ingredient" : sanitized;
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
                // Display stack naming is best-effort; SLOT tooltip lines still carry recipe details.
            }
        }

        private record ResolvedAlternative(Alternative alternative, SlotWorkspaceViewModel.AtlasItem existing) {
        }
    }
}
