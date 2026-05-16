package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.goal.GoalChoiceResolution;
import dev.imagio.slot.inventory.goal.GoalDescriptor;
import dev.imagio.slot.inventory.goal.GoalIngredientDescriptor;
import dev.imagio.slot.inventory.goal.GoalPlanState;
import dev.imagio.slot.inventory.goal.GoalRecipeDescriptor;
import dev.imagio.slot.inventory.goal.GoalStackDescriptor;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NBT transport for {@link SlotWorkspaceViewModel}. The view model and all its records are
 * platform-neutral data; serialization to Minecraft NBT lives here so the common module
 * does not depend on {@code net.minecraft.nbt} types.
 */
public final class SlotWorkspaceViewModelCodec {
    private SlotWorkspaceViewModelCodec() {
    }

    public static CompoundTag encode(SlotWorkspaceViewModel viewModel, HolderLookup.Provider provider) {
        return encode(viewModel, provider, true);
    }

    public static CompoundTag encode(
            SlotWorkspaceViewModel viewModel,
            HolderLookup.Provider provider,
            boolean includeRevision
    ) {
        CompoundTag tag = new CompoundTag();
        if (includeRevision) {
            tag.putLong("revision", viewModel.revision());
        }
        tag.putString("status", viewModel.status());
        tag.putString("diagnostics", viewModel.diagnostics());
        tag.putInt("pendingCount", viewModel.pendingCount());
        tag.putInt("selectedQuickAccessSlot", viewModel.selectedQuickAccessSlot());
        tag.putInt("canvasWidth", viewModel.canvasWidth());
        tag.putInt("canvasHeight", viewModel.canvasHeight());
        tag.putInt("carriedFreeSlotCount", viewModel.carriedFreeSlotCount());
        tag.putInt("carriedSlotCapacity", viewModel.carriedSlotCapacity());

        ListTag islandTags = new ListTag();
        for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
            islandTags.add(encodeIsland(island));
        }
        tag.put("islands", islandTags);

        ListTag itemTags = new ListTag();
        for (SlotWorkspaceViewModel.AtlasItem atlasItem : viewModel.atlasItems()) {
            itemTags.add(encodeItem(atlasItem, provider));
        }
        tag.put("atlasItems", itemTags);

        ListTag triageTags = new ListTag();
        for (SlotWorkspaceViewModel.AtlasItem triageItem : viewModel.triageItems()) {
            triageTags.add(encodeItem(triageItem, provider));
        }
        tag.put("triageItems", triageTags);
        tag.put("contextualSuggestionLanes", encodeSuggestionLanes(viewModel.contextualSuggestionLanes(), provider));

        ListTag chipTags = new ListTag();
        for (SlotWorkspaceViewModel.ChestChip chip : viewModel.chestChips()) {
            CompoundTag chipTag = encodeChestChip(chip);
            writeChestChipContents(chipTag, chip, provider);
            chipTags.add(chipTag);
        }
        tag.put("chestChips", chipTags);

        ListTag clusterTags = new ListTag();
        for (SlotWorkspaceViewModel.ChestClusterDescriptor cluster : viewModel.chestClusters()) {
            clusterTags.add(encodeChestCluster(cluster));
        }
        tag.put("chestClusters", clusterTags);

        tag.put("lootChestPanel", encodeLootChestPanel(viewModel.lootChestPanel(), provider));

        ListTag hotbarTags = new ListTag();
        for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
            hotbarTags.add(encodeHotbar(slot, provider));
        }
        tag.put("hotbarSlots", hotbarTags);
        tag.put("offhand", encodeOffhand(viewModel.offhand(), provider));

        ListTag kitTags = new ListTag();
        for (SlotWorkspaceViewModel.KitCard card : viewModel.kits()) {
            kitTags.add(encodeKitCard(card, provider));
        }
        tag.put("kits", kitTags);

        ListTag wayfindingTags = new ListTag();
        for (WayfindingTarget target : viewModel.wayfindingTargets()) {
            wayfindingTags.add(encodeWayfindingTarget(target));
        }
        tag.put("wayfindingTargets", wayfindingTags);

        ListTag depositableTags = new ListTag();
        for (SlotWorkspaceViewModel.IdentityRef ref : viewModel.depositableIdentities()) {
            depositableTags.add(encodeIdentity(ref));
        }
        tag.put("depositableIdentities", depositableTags);

        ListTag recentTags = new ListTag();
        for (SlotWorkspaceViewModel.IdentityRef ref : viewModel.recentIdentities()) {
            recentTags.add(encodeIdentity(ref));
        }
        tag.put("recentIdentities", recentTags);

        tag.put("activeChestPanel", encodeActiveChestPanel(viewModel.activeChestPanel()));
        tag.put("goalRecipeDefaults", encodeGoalRecipeDefaults(viewModel.goalRecipeDefaults().recipeChoicesByOutputItemId()));
        tag.put("goalPlans", encodeGoalPlans(viewModel.goalPlans()));
        return tag;
    }

    private static CompoundTag encodeActiveChestPanel(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        CompoundTag tag = new CompoundTag();
        if (panel == null || !panel.isPresent()) {
            return tag;
        }
        tag.putString("storageId", panel.storageId());
        tag.putString("label", panel.label());
        tag.putString("clusterId", panel.clusterId());
        tag.putString("clusterLabel", panel.clusterLabel());
        tag.putInt("swatchColor", panel.swatchColor());
        tag.putInt("posX", panel.posX());
        tag.putInt("posY", panel.posY());
        tag.putInt("posZ", panel.posZ());
        tag.putString("dimensionId", panel.dimensionId());
        return tag;
    }

    private static SlotWorkspaceViewModel.ActiveChestPanel decodeActiveChestPanel(CompoundTag tag) {
        // The encoder writes an empty tag (no keys) when no chest is
        // active; treat absence-or-blank-dimensionId as "not present"
        // since dimensionId is the panel's isPresent() check.
        if (tag == null || tag.getString("dimensionId").isBlank()) {
            return SlotWorkspaceViewModel.ActiveChestPanel.empty();
        }
        return new SlotWorkspaceViewModel.ActiveChestPanel(
                tag.getString("storageId"),
                tag.getString("label"),
                tag.getString("clusterId"),
                tag.getString("clusterLabel"),
                tag.getInt("swatchColor"),
                tag.getInt("posX"),
                tag.getInt("posY"),
                tag.getInt("posZ"),
                tag.getString("dimensionId")
        );
    }

    public static SlotWorkspaceViewModel decode(HolderLookup.Provider provider, Tag tag) {
        if (!(tag instanceof CompoundTag compoundTag)) {
            return SlotWorkspaceViewModel.empty();
        }

        ArrayList<SlotWorkspaceViewModel.AtlasIsland> islands = new ArrayList<>();
        ListTag islandTags = compoundTag.getList("islands", Tag.TAG_COMPOUND);
        for (int index = 0; index < islandTags.size(); index++) {
            islands.add(decodeIsland(islandTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.AtlasItem> atlasItems = new ArrayList<>();
        ListTag itemTags = compoundTag.getList("atlasItems", Tag.TAG_COMPOUND);
        for (int index = 0; index < itemTags.size(); index++) {
            atlasItems.add(decodeItem(provider, itemTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.AtlasItem> triageItems = new ArrayList<>();
        ListTag triageTags = compoundTag.getList("triageItems", Tag.TAG_COMPOUND);
        for (int index = 0; index < triageTags.size(); index++) {
            triageItems.add(decodeItem(provider, triageTags.getCompound(index)));
        }
        List<SlotWorkspaceViewModel.ContextualSuggestionLane> contextualSuggestionLanes =
                decodeSuggestionLanes(provider, compoundTag);

        ArrayList<SlotWorkspaceViewModel.ChestChip> chestChips = new ArrayList<>();
        ListTag chipTags = compoundTag.getList("chestChips", Tag.TAG_COMPOUND);
        for (int index = 0; index < chipTags.size(); index++) {
            CompoundTag chipTag = chipTags.getCompound(index);
            SlotWorkspaceViewModel.ChestChip baseChip = decodeChestChip(chipTag);
            java.util.List<SlotWorkspaceViewModel.ChestContentSummary> contents =
                    readChestChipContents(chipTag, provider);
            chestChips.add(new SlotWorkspaceViewModel.ChestChip(
                    baseChip.storageId(),
                    baseChip.dimensionId(),
                    baseChip.label(),
                    baseChip.anchorCount(),
                    baseChip.slotCapacity(),
                    baseChip.filledSlots(),
                    baseChip.proximate(),
                    baseChip.affinityIdentities(),
                    baseChip.worldX(),
                    baseChip.worldY(),
                    baseChip.worldZ(),
                    baseChip.clusterId(),
                    contents
            ));
        }

        ArrayList<SlotWorkspaceViewModel.ChestClusterDescriptor> chestClusters = new ArrayList<>();
        ListTag clusterTags = compoundTag.getList("chestClusters", Tag.TAG_COMPOUND);
        for (int index = 0; index < clusterTags.size(); index++) {
            chestClusters.add(decodeChestCluster(clusterTags.getCompound(index)));
        }

        SlotWorkspaceViewModel.LootChestPanel lootChestPanel =
                decodeLootChestPanel(provider, compoundTag.getCompound("lootChestPanel"));

        ArrayList<SlotWorkspaceViewModel.HotbarSlot> hotbarSlots = new ArrayList<>();
        ListTag hotbarTags = compoundTag.getList("hotbarSlots", Tag.TAG_COMPOUND);
        for (int index = 0; index < hotbarTags.size(); index++) {
            hotbarSlots.add(decodeHotbar(provider, hotbarTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.KitCard> kits = new ArrayList<>();
        ListTag kitTags = compoundTag.getList("kits", Tag.TAG_COMPOUND);
        for (int index = 0; index < kitTags.size(); index++) {
            kits.add(decodeKitCard(provider, kitTags.getCompound(index)));
        }

        ArrayList<WayfindingTarget> wayfindingTargets = new ArrayList<>();
        ListTag wayfindingTags = compoundTag.getList("wayfindingTargets", Tag.TAG_COMPOUND);
        for (int index = 0; index < wayfindingTags.size(); index++) {
            wayfindingTargets.add(decodeWayfindingTarget(wayfindingTags.getCompound(index)));
        }

        java.util.LinkedHashSet<SlotWorkspaceViewModel.IdentityRef> depositableIdentities =
                new java.util.LinkedHashSet<>();
        ListTag depositableTags = compoundTag.getList("depositableIdentities", Tag.TAG_COMPOUND);
        for (int index = 0; index < depositableTags.size(); index++) {
            depositableIdentities.add(decodeIdentity(depositableTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.IdentityRef> recentIdentities = new ArrayList<>();
        ListTag recentTags = compoundTag.getList("recentIdentities", Tag.TAG_COMPOUND);
        for (int index = 0; index < recentTags.size(); index++) {
            recentIdentities.add(decodeIdentity(recentTags.getCompound(index)));
        }

        SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel =
                decodeActiveChestPanel(compoundTag.getCompound("activeChestPanel"));
        dev.imagio.slot.inventory.goal.GoalRecipeDefaults goalRecipeDefaults =
                new dev.imagio.slot.inventory.goal.GoalRecipeDefaults(decodeGoalRecipeDefaults(compoundTag));
        List<GoalPlanState> goalPlans = decodeGoalPlans(compoundTag);

        return new SlotWorkspaceViewModel(
                compoundTag.getLong("revision"),
                compoundTag.getString("status"),
                compoundTag.getString("diagnostics"),
                compoundTag.getInt("pendingCount"),
                compoundTag.getInt("selectedQuickAccessSlot"),
                compoundTag.getInt("canvasWidth"),
                compoundTag.getInt("canvasHeight"),
                compoundTag.getInt("carriedFreeSlotCount"),
                compoundTag.getInt("carriedSlotCapacity"),
                islands.isEmpty()
                        ? SlotWorkspaceAtlasLayout.baseIslands(VisualHomeMap.empty())
                        : islands,
                atlasItems,
                triageItems,
                chestChips,
                chestClusters,
                hotbarSlots.isEmpty() ? SlotWorkspaceViewModel.emptyHotbar() : hotbarSlots,
                decodeOffhand(provider, compoundTag.getCompound("offhand")),
                kits,
                lootChestPanel,
                wayfindingTargets,
                depositableIdentities,
                recentIdentities,
                activeChestPanel,
                goalRecipeDefaults,
                goalPlans,
                contextualSuggestionLanes
        );
    }

    private static ListTag encodeSuggestionLanes(
            List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes,
            HolderLookup.Provider provider
    ) {
        ListTag tags = new ListTag();
        if (lanes == null || lanes.isEmpty()) {
            return tags;
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : lanes) {
            if (lane == null || !lane.displayable()) {
                continue;
            }
            CompoundTag tag = new CompoundTag();
            tag.putString("id", lane.id());
            tag.putString("label", lane.label());
            if (!lane.placeholderText().isBlank()) {
                tag.putString("placeholderText", lane.placeholderText());
            }
            ListTag itemTags = new ListTag();
            for (SlotWorkspaceViewModel.AtlasItem item : lane.items()) {
                itemTags.add(encodeItem(item, provider));
            }
            tag.put("items", itemTags);
            tag.put("debugInfo", encodeSuggestionDebugInfo(lane.debugInfo()));
            tags.add(tag);
        }
        return tags;
    }

    private static ListTag encodeSuggestionDebugInfo(
            List<SlotWorkspaceViewModel.ContextualSuggestionDebugInfo> debugInfo
    ) {
        ListTag tags = new ListTag();
        if (debugInfo == null || debugInfo.isEmpty()) {
            return tags;
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionDebugInfo info : debugInfo) {
            if (info == null) {
                continue;
            }
            CompoundTag tag = new CompoundTag();
            tag.put("identity", encodeIdentity(info.identity()));
            tag.putDouble("score", info.score());
            tag.putDouble("relevance", info.relevance());
            ListTag reasons = new ListTag();
            for (String reason : info.reasons()) {
                if (reason == null || reason.isBlank()) {
                    continue;
                }
                CompoundTag reasonTag = new CompoundTag();
                reasonTag.putString("text", reason);
                reasons.add(reasonTag);
            }
            tag.put("reasons", reasons);
            tags.add(tag);
        }
        return tags;
    }

    private static List<SlotWorkspaceViewModel.ContextualSuggestionLane> decodeSuggestionLanes(
            HolderLookup.Provider provider,
            CompoundTag compound
    ) {
        ArrayList<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = new ArrayList<>();
        if (compound == null) {
            return lanes;
        }
        ListTag laneTags = compound.getList("contextualSuggestionLanes", Tag.TAG_COMPOUND);
        for (int laneIndex = 0; laneIndex < laneTags.size(); laneIndex++) {
            CompoundTag laneTag = laneTags.getCompound(laneIndex);
            ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
            ListTag itemTags = laneTag.getList("items", Tag.TAG_COMPOUND);
            for (int itemIndex = 0; itemIndex < itemTags.size(); itemIndex++) {
                items.add(decodeItem(provider, itemTags.getCompound(itemIndex)));
            }
            lanes.add(new SlotWorkspaceViewModel.ContextualSuggestionLane(
                    laneTag.getString("id"),
                    laneTag.getString("label"),
                    items,
                    laneTag.getString("placeholderText"),
                    decodeSuggestionDebugInfo(laneTag)));
        }
        return lanes;
    }

    private static List<SlotWorkspaceViewModel.ContextualSuggestionDebugInfo> decodeSuggestionDebugInfo(
            CompoundTag laneTag
    ) {
        ArrayList<SlotWorkspaceViewModel.ContextualSuggestionDebugInfo> result = new ArrayList<>();
        if (laneTag == null) {
            return result;
        }
        ListTag tags = laneTag.getList("debugInfo", Tag.TAG_COMPOUND);
        for (int index = 0; index < tags.size(); index++) {
            CompoundTag tag = tags.getCompound(index);
            ArrayList<String> reasons = new ArrayList<>();
            ListTag reasonTags = tag.getList("reasons", Tag.TAG_COMPOUND);
            for (int reasonIndex = 0; reasonIndex < reasonTags.size(); reasonIndex++) {
                String reason = reasonTags.getCompound(reasonIndex).getString("text");
                if (!reason.isBlank()) {
                    reasons.add(reason);
                }
            }
            result.add(new SlotWorkspaceViewModel.ContextualSuggestionDebugInfo(
                    decodeIdentity(tag.getCompound("identity")),
                    tag.getDouble("score"),
                    tag.getDouble("relevance"),
                    reasons));
        }
        return result;
    }

    private static ListTag encodeGoalRecipeDefaults(Map<String, String> defaults) {
        ListTag tags = new ListTag();
        if (defaults == null || defaults.isEmpty()) {
            return tags;
        }
        defaults.forEach((outputItemId, recipeId) -> {
            if (outputItemId == null || outputItemId.isBlank() || recipeId == null || recipeId.isBlank()) {
                return;
            }
            CompoundTag tag = new CompoundTag();
            tag.putString("outputItemId", outputItemId);
            tag.putString("recipeId", recipeId);
            tags.add(tag);
        });
        return tags;
    }

    private static Map<String, String> decodeGoalRecipeDefaults(CompoundTag compound) {
        LinkedHashMap<String, String> defaults = new LinkedHashMap<>();
        if (compound == null) {
            return defaults;
        }
        ListTag tags = compound.getList("goalRecipeDefaults", Tag.TAG_COMPOUND);
        for (int index = 0; index < tags.size(); index++) {
            CompoundTag tag = tags.getCompound(index);
            String outputItemId = tag.getString("outputItemId");
            String recipeId = tag.getString("recipeId");
            if (!outputItemId.isBlank() && !recipeId.isBlank()) {
                defaults.put(outputItemId, recipeId);
            }
        }
        return defaults;
    }

    private static ListTag encodeGoalPlans(List<GoalPlanState> goals) {
        ListTag tags = new ListTag();
        if (goals == null || goals.isEmpty()) {
            return tags;
        }
        for (GoalPlanState goal : goals) {
            CompoundTag tag = encodeGoalPlan(goal);
            if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
        return tags;
    }

    private static List<GoalPlanState> decodeGoalPlans(CompoundTag compound) {
        ArrayList<GoalPlanState> goals = new ArrayList<>();
        if (compound == null) {
            return goals;
        }
        ListTag tags = compound.getList("goalPlans", Tag.TAG_COMPOUND);
        for (int index = 0; index < tags.size(); index++) {
            GoalPlanState goal = decodeGoalPlan(tags.getCompound(index));
            if (goal != null) {
                goals.add(goal);
            }
        }
        return List.copyOf(goals);
    }

    public static CompoundTag encodeGoalPlan(GoalPlanState goal) {
        CompoundTag tag = new CompoundTag();
        if (goal == null || goal.descriptor() == null) {
            return tag;
        }
        tag.putString("goalId", goal.goalId());
        tag.putString("label", goal.label());
        tag.putInt("targetCount", goal.targetCount());
        tag.put("descriptor", encodeGoalDescriptor(goal.descriptor()));
        tag.put("choiceResolution", encodeGoalChoiceResolution(goal.choiceResolution()));
        return tag;
    }

    public static GoalPlanState decodeGoalPlan(CompoundTag tag) {
        if (tag == null || !tag.contains("descriptor", Tag.TAG_COMPOUND)) {
            return null;
        }
        GoalDescriptor descriptor = decodeGoalDescriptor(tag.getCompound("descriptor"));
        if (descriptor == null) {
            return null;
        }
        return new GoalPlanState(
                tag.getString("goalId"),
                tag.getString("label"),
                tag.getInt("targetCount"),
                descriptor,
                decodeGoalChoiceResolution(tag.getCompound("choiceResolution"))
        );
    }

    private static CompoundTag encodeGoalDescriptor(GoalDescriptor descriptor) {
        CompoundTag tag = new CompoundTag();
        if (descriptor == null) {
            return tag;
        }
        tag.putString("goalId", descriptor.goalId());
        tag.putString("label", descriptor.label());
        tag.put("targetOutputs", encodeGoalStacks(descriptor.targetOutputs()));
        tag.putInt("targetCount", descriptor.targetCount());
        tag.putString("focusedRecipeId", descriptor.focusedRecipeId());
        tag.putString("focusedCategoryId", descriptor.focusedCategoryId());
        ListTag recipes = new ListTag();
        for (GoalRecipeDescriptor recipe : descriptor.recipes()) {
            recipes.add(encodeGoalRecipe(recipe));
        }
        tag.put("recipes", recipes);
        return tag;
    }

    private static GoalDescriptor decodeGoalDescriptor(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        ArrayList<GoalRecipeDescriptor> recipes = new ArrayList<>();
        ListTag recipeTags = tag.getList("recipes", Tag.TAG_COMPOUND);
        for (int index = 0; index < recipeTags.size(); index++) {
            GoalRecipeDescriptor recipe = decodeGoalRecipe(recipeTags.getCompound(index));
            if (recipe != null) {
                recipes.add(recipe);
            }
        }
        return new GoalDescriptor(
                tag.getString("goalId"),
                tag.getString("label"),
                decodeGoalStacks(tag.getList("targetOutputs", Tag.TAG_COMPOUND)),
                tag.getInt("targetCount"),
                tag.getString("focusedRecipeId"),
                tag.getString("focusedCategoryId"),
                recipes
        );
    }

    private static CompoundTag encodeGoalRecipe(GoalRecipeDescriptor recipe) {
        CompoundTag tag = new CompoundTag();
        if (recipe == null) {
            return tag;
        }
        tag.putString("recipeId", recipe.recipeId());
        tag.putString("categoryId", recipe.categoryId());
        tag.putBoolean("supportsTree", recipe.supportsTree());
        tag.put("outputs", encodeGoalStacks(recipe.outputs()));
        tag.put("inputs", encodeGoalIngredients(recipe.inputs()));
        tag.put("catalysts", encodeGoalIngredients(recipe.catalysts()));
        tag.put("diagnostics", encodeStrings(recipe.diagnostics()));
        return tag;
    }

    private static GoalRecipeDescriptor decodeGoalRecipe(CompoundTag tag) {
        if (tag == null || tag.getString("recipeId").isBlank()) {
            return null;
        }
        return new GoalRecipeDescriptor(
                tag.getString("recipeId"),
                tag.getString("categoryId"),
                tag.getBoolean("supportsTree"),
                decodeGoalStacks(tag.getList("outputs", Tag.TAG_COMPOUND)),
                decodeGoalIngredients(tag.getList("inputs", Tag.TAG_COMPOUND)),
                decodeGoalIngredients(tag.getList("catalysts", Tag.TAG_COMPOUND)),
                decodeStrings(tag.getList("diagnostics", Tag.TAG_STRING))
        );
    }

    private static ListTag encodeGoalIngredients(List<GoalIngredientDescriptor> ingredients) {
        ListTag tags = new ListTag();
        if (ingredients == null || ingredients.isEmpty()) {
            return tags;
        }
        for (GoalIngredientDescriptor ingredient : ingredients) {
            tags.add(encodeGoalIngredient(ingredient));
        }
        return tags;
    }

    private static List<GoalIngredientDescriptor> decodeGoalIngredients(ListTag tags) {
        ArrayList<GoalIngredientDescriptor> ingredients = new ArrayList<>();
        if (tags == null) {
            return ingredients;
        }
        for (int index = 0; index < tags.size(); index++) {
            GoalIngredientDescriptor ingredient = decodeGoalIngredient(tags.getCompound(index));
            if (ingredient != null) {
                ingredients.add(ingredient);
            }
        }
        return List.copyOf(ingredients);
    }

    private static CompoundTag encodeGoalIngredient(GoalIngredientDescriptor ingredient) {
        CompoundTag tag = new CompoundTag();
        if (ingredient == null) {
            return tag;
        }
        tag.putString("ingredientId", ingredient.ingredientId());
        tag.putString("label", ingredient.label());
        tag.putInt("quantity", ingredient.quantity());
        tag.putDouble("chance", ingredient.chance());
        tag.putString("serializedIngredient", ingredient.serializedIngredient());
        tag.put("alternatives", encodeGoalStacks(ingredient.alternatives()));
        tag.putBoolean("choiceRequired", ingredient.choiceRequired());
        tag.putBoolean("consumed", ingredient.consumed());
        tag.putString("tagOrListLabel", ingredient.tagOrListLabel());
        tag.put("diagnostics", encodeStrings(ingredient.diagnostics()));
        return tag;
    }

    private static GoalIngredientDescriptor decodeGoalIngredient(CompoundTag tag) {
        if (tag == null || tag.getString("ingredientId").isBlank()) {
            return null;
        }
        return new GoalIngredientDescriptor(
                tag.getString("ingredientId"),
                tag.getString("label"),
                tag.getInt("quantity"),
                tag.getDouble("chance"),
                tag.getString("serializedIngredient"),
                decodeGoalStacks(tag.getList("alternatives", Tag.TAG_COMPOUND)),
                tag.getBoolean("choiceRequired"),
                tag.getBoolean("consumed"),
                tag.getString("tagOrListLabel"),
                decodeStrings(tag.getList("diagnostics", Tag.TAG_STRING))
        );
    }

    private static ListTag encodeGoalStacks(List<GoalStackDescriptor> stacks) {
        ListTag tags = new ListTag();
        if (stacks == null || stacks.isEmpty()) {
            return tags;
        }
        for (GoalStackDescriptor stack : stacks) {
            CompoundTag tag = encodeGoalStack(stack);
            if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
        return tags;
    }

    private static List<GoalStackDescriptor> decodeGoalStacks(ListTag tags) {
        ArrayList<GoalStackDescriptor> stacks = new ArrayList<>();
        if (tags == null) {
            return stacks;
        }
        for (int index = 0; index < tags.size(); index++) {
            GoalStackDescriptor stack = decodeGoalStack(tags.getCompound(index));
            if (stack != null) {
                stacks.add(stack);
            }
        }
        return List.copyOf(stacks);
    }

    private static CompoundTag encodeGoalStack(GoalStackDescriptor stack) {
        CompoundTag tag = new CompoundTag();
        if (stack == null || stack.identity() == null) {
            return tag;
        }
        tag.put("identity", encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(stack.identity())));
        tag.putString("displayName", stack.displayName());
        tag.putInt("count", stack.count());
        return tag;
    }

    private static GoalStackDescriptor decodeGoalStack(CompoundTag tag) {
        if (tag == null || !tag.contains("identity", Tag.TAG_COMPOUND)) {
            return null;
        }
        ItemIdentity identity = decodeIdentity(tag.getCompound("identity")).toIdentity();
        return identity == null ? null : new GoalStackDescriptor(identity, tag.getString("displayName"), tag.getInt("count"));
    }

    private static CompoundTag encodeGoalChoiceResolution(GoalChoiceResolution resolution) {
        GoalChoiceResolution resolved = resolution == null ? GoalChoiceResolution.empty() : resolution;
        CompoundTag tag = new CompoundTag();
        ListTag choices = new ListTag();
        resolved.choicesByKey().forEach((choiceGroupId, identity) -> {
            if (identity == null) {
                return;
            }
            CompoundTag choiceTag = new CompoundTag();
            choiceTag.putString("choiceGroupId", choiceGroupId);
            choiceTag.put("identity", encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(identity)));
            choices.add(choiceTag);
        });
        tag.put("choices", choices);
        ListTag recipeChoices = new ListTag();
        resolved.recipeChoicesByKey().forEach((choiceGroupId, recipeId) -> {
            CompoundTag choiceTag = new CompoundTag();
            choiceTag.putString("choiceGroupId", choiceGroupId);
            choiceTag.putString("recipeId", recipeId);
            recipeChoices.add(choiceTag);
        });
        tag.put("recipeChoices", recipeChoices);
        return tag;
    }

    private static GoalChoiceResolution decodeGoalChoiceResolution(CompoundTag tag) {
        if (tag == null) {
            return GoalChoiceResolution.empty();
        }
        LinkedHashMap<String, ItemIdentity> choices = new LinkedHashMap<>();
        ListTag choiceTags = tag.getList("choices", Tag.TAG_COMPOUND);
        for (int index = 0; index < choiceTags.size(); index++) {
            CompoundTag choiceTag = choiceTags.getCompound(index);
            ItemIdentity identity = decodeIdentity(choiceTag.getCompound("identity")).toIdentity();
            String choiceGroupId = choiceTag.getString("choiceGroupId");
            if (!choiceGroupId.isBlank() && identity != null) {
                choices.put(choiceGroupId, identity);
            }
        }
        LinkedHashMap<String, String> recipeChoices = new LinkedHashMap<>();
        ListTag recipeChoiceTags = tag.getList("recipeChoices", Tag.TAG_COMPOUND);
        for (int index = 0; index < recipeChoiceTags.size(); index++) {
            CompoundTag choiceTag = recipeChoiceTags.getCompound(index);
            String choiceGroupId = choiceTag.getString("choiceGroupId");
            String recipeId = choiceTag.getString("recipeId");
            if (!choiceGroupId.isBlank() && !recipeId.isBlank()) {
                recipeChoices.put(choiceGroupId, recipeId);
            }
        }
        return new GoalChoiceResolution(choices, recipeChoices);
    }

    private static ListTag encodeStrings(List<String> values) {
        ListTag tags = new ListTag();
        if (values == null || values.isEmpty()) {
            return tags;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                tags.add(net.minecraft.nbt.StringTag.valueOf(value));
            }
        }
        return tags;
    }

    private static List<String> decodeStrings(ListTag tags) {
        ArrayList<String> values = new ArrayList<>();
        if (tags == null) {
            return values;
        }
        for (int index = 0; index < tags.size(); index++) {
            String value = tags.getString(index);
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }

    private static CompoundTag encodeWayfindingTarget(WayfindingTarget target) {
        CompoundTag tag = new CompoundTag();
        tag.putString("storageId", target.storageId());
        tag.putString("dimensionId", target.dimensionId());
        tag.putInt("worldX", target.worldX());
        tag.putInt("worldY", target.worldY());
        tag.putInt("worldZ", target.worldZ());
        tag.putInt("totalMissingCount", target.totalMissingCount());
        tag.putString("scope", target.scope().name());
        tag.put("missingIdentities", encodeIdentitySet(target.missingIdentities()));
        tag.put("kitMissingIdentities", encodeIdentitySet(target.kitMissingIdentities()));
        tag.put("desiredMissingIdentities", encodeIdentitySet(target.desiredMissingIdentities()));
        tag.put("wantedMissingIdentities", encodeIdentitySet(target.wantedMissingIdentities()));
        return tag;
    }

    private static WayfindingTarget decodeWayfindingTarget(CompoundTag tag) {
        java.util.LinkedHashSet<ItemIdentity> identities = decodeIdentitySet(tag, "missingIdentities");
        java.util.LinkedHashSet<ItemIdentity> kitIdentities = decodeIdentitySet(tag, "kitMissingIdentities");
        java.util.LinkedHashSet<ItemIdentity> desiredIdentities = decodeIdentitySet(tag, "desiredMissingIdentities");
        java.util.LinkedHashSet<ItemIdentity> wantedIdentities = decodeIdentitySet(tag, "wantedMissingIdentities");
        WayfindingTarget.Scope scope;
        try {
            String raw = tag.getString("scope");
            scope = raw == null || raw.isBlank()
                    ? WayfindingTarget.Scope.PLAYER
                    : WayfindingTarget.Scope.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            scope = WayfindingTarget.Scope.PLAYER;
        }
        return new WayfindingTarget(
                tag.getString("storageId"),
                tag.getString("dimensionId"),
                tag.getInt("worldX"),
                tag.getInt("worldY"),
                tag.getInt("worldZ"),
                identities,
                kitIdentities,
                desiredIdentities,
                wantedIdentities,
                tag.getInt("totalMissingCount"),
                scope
        );
    }

    private static ListTag encodeIdentitySet(java.util.Set<ItemIdentity> identities) {
        ListTag tags = new ListTag();
        if (identities != null) {
            for (ItemIdentity identity : identities) {
                if (identity != null) {
                    tags.add(encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(identity)));
                }
            }
        }
        return tags;
    }

    private static java.util.LinkedHashSet<ItemIdentity> decodeIdentitySet(CompoundTag tag, String key) {
        java.util.LinkedHashSet<ItemIdentity> identities = new java.util.LinkedHashSet<>();
        ListTag identityTags = tag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < identityTags.size(); i++) {
            ItemIdentity identity = decodeIdentity(identityTags.getCompound(i)).toIdentity();
            if (identity != null) {
                identities.add(identity);
            }
        }
        return identities;
    }

    private static CompoundTag encodeKitCard(SlotWorkspaceViewModel.KitCard card, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("kitId", card.kitId());
        tag.putString("name", card.name());
        tag.putInt("pageCount", card.pageCount());
        tag.putInt("activePageIndex", card.activePageIndex());
        tag.putBoolean("active", card.active());
        tag.putInt("slotCount", card.slotCount());
        tag.putInt("readyCount", card.readyCount());
        tag.putInt("carriedSlotCount", card.carriedSlotCount());
        tag.putInt("carriedSlotCapacity", card.carriedSlotCapacity());
        tag.putInt("bringSlotCount", card.bringSlotCount());
        tag.putInt("bringReadyCount", card.bringReadyCount());
        ListTag slots = new ListTag();
        for (SlotWorkspaceViewModel.KitSlotState slot : card.slots()) {
            slots.add(encodeKitSlot(slot, provider));
        }
        tag.put("slots", slots);
        ListTag pages = new ListTag();
        for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
            pages.add(encodeKitPage(page, provider));
        }
        tag.put("pages", pages);
        ListTag bring = new ListTag();
        for (SlotWorkspaceViewModel.KitBringItem item : card.bring()) {
            bring.add(encodeKitBring(item, provider));
        }
        tag.put("bring", bring);
        return tag;
    }

    private static SlotWorkspaceViewModel.KitCard decodeKitCard(HolderLookup.Provider provider, CompoundTag tag) {
        ArrayList<SlotWorkspaceViewModel.KitSlotState> slots = new ArrayList<>();
        ListTag slotTags = tag.getList("slots", Tag.TAG_COMPOUND);
        for (int index = 0; index < slotTags.size(); index++) {
            slots.add(decodeKitSlot(provider, slotTags.getCompound(index)));
        }
        ArrayList<SlotWorkspaceViewModel.KitPageView> pages = new ArrayList<>();
        ListTag pageTags = tag.getList("pages", Tag.TAG_COMPOUND);
        for (int index = 0; index < pageTags.size(); index++) {
            pages.add(decodeKitPage(provider, pageTags.getCompound(index)));
        }
        ArrayList<SlotWorkspaceViewModel.KitBringItem> bring = new ArrayList<>();
        ListTag bringTags = tag.getList("bring", Tag.TAG_COMPOUND);
        for (int index = 0; index < bringTags.size(); index++) {
            bring.add(decodeKitBring(provider, bringTags.getCompound(index)));
        }
        return new SlotWorkspaceViewModel.KitCard(
                tag.getString("kitId"),
                tag.getString("name"),
                tag.getInt("pageCount"),
                tag.getInt("activePageIndex"),
                tag.getBoolean("active"),
                tag.getInt("slotCount"),
                tag.getInt("readyCount"),
                tag.getInt("carriedSlotCount"),
                tag.getInt("carriedSlotCapacity"),
                tag.getInt("bringSlotCount"),
                tag.getInt("bringReadyCount"),
                slots,
                pages,
                bring
        );
    }

    private static CompoundTag encodeKitBring(SlotWorkspaceViewModel.KitBringItem item, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("ready", item.ready());
        tag.put("identity", encodeIdentity(item.identity()));
        tag.put("displayStack", item.displayStack().saveOptional(provider));
        tag.putString("name", item.name());
        tag.putInt("presentCount", item.presentCount());
        tag.putInt("targetCount", item.targetCount());
        return tag;
    }

    private static SlotWorkspaceViewModel.KitBringItem decodeKitBring(HolderLookup.Provider provider, CompoundTag tag) {
        return new SlotWorkspaceViewModel.KitBringItem(
                decodeIdentity(tag.getCompound("identity")),
                tag.getBoolean("ready"),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getString("name"),
                tag.getInt("presentCount"),
                tag.getInt("targetCount")
        );
    }

    private static CompoundTag encodeKitPage(SlotWorkspaceViewModel.KitPageView page, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("pageIndex", page.pageIndex());
        tag.putInt("slotCount", page.slotCount());
        tag.putInt("readyCount", page.readyCount());
        ListTag slots = new ListTag();
        for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
            slots.add(encodeKitSlot(slot, provider));
        }
        tag.put("slots", slots);
        return tag;
    }

    private static SlotWorkspaceViewModel.KitPageView decodeKitPage(HolderLookup.Provider provider, CompoundTag tag) {
        ArrayList<SlotWorkspaceViewModel.KitSlotState> slots = new ArrayList<>();
        ListTag slotTags = tag.getList("slots", Tag.TAG_COMPOUND);
        for (int index = 0; index < slotTags.size(); index++) {
            slots.add(decodeKitSlot(provider, slotTags.getCompound(index)));
        }
        return new SlotWorkspaceViewModel.KitPageView(
                tag.getInt("pageIndex"),
                tag.getInt("slotCount"),
                tag.getInt("readyCount"),
                slots
        );
    }

    private static CompoundTag encodeKitSlot(SlotWorkspaceViewModel.KitSlotState slot, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("slotIndex", slot.slotIndex());
        tag.putBoolean("filled", slot.filled());
        tag.putBoolean("ready", slot.ready());
        tag.put("identity", encodeIdentity(slot.identity()));
        tag.put("displayStack", slot.displayStack().saveOptional(provider));
        tag.putString("name", slot.name());
        return tag;
    }

    private static SlotWorkspaceViewModel.KitSlotState decodeKitSlot(HolderLookup.Provider provider, CompoundTag tag) {
        return new SlotWorkspaceViewModel.KitSlotState(
                tag.getInt("slotIndex"),
                tag.getBoolean("filled"),
                tag.getBoolean("ready"),
                decodeIdentity(tag.getCompound("identity")),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getString("name")
        );
    }

    private static CompoundTag encodeIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", identity.itemId());
        tag.putString("comparisonMode", identity.comparisonMode());
        tag.putString("componentFingerprint", identity.componentFingerprint());
        return tag;
    }

    private static SlotWorkspaceViewModel.IdentityRef decodeIdentity(CompoundTag tag) {
        return new SlotWorkspaceViewModel.IdentityRef(
                tag.getString("itemId"),
                tag.getString("comparisonMode"),
                tag.getString("componentFingerprint")
        );
    }

    private static CompoundTag encodeIsland(SlotWorkspaceViewModel.AtlasIsland island) {
        CompoundTag tag = new CompoundTag();
        tag.putString("islandId", island.islandId());
        tag.putString("label", island.label());
        tag.putString("kind", island.kind().name());
        tag.putDouble("x", island.x());
        tag.putDouble("y", island.y());
        tag.putInt("color", island.color());
        tag.putInt("itemCount", island.itemCount());
        tag.putInt("carriedCount", island.carriedCount());
        return tag;
    }

    private static SlotWorkspaceViewModel.AtlasIsland decodeIsland(CompoundTag tag) {
        return new SlotWorkspaceViewModel.AtlasIsland(
                tag.getString("islandId"),
                tag.getString("label"),
                decodeIslandKind(tag.getString("kind")),
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getInt("color"),
                tag.getInt("itemCount"),
                tag.getInt("carriedCount")
        );
    }

    private static CompoundTag encodeItem(SlotWorkspaceViewModel.AtlasItem item, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("identity", encodeIdentity(item.identity()));
        tag.put("displayStack", item.displayStack().saveOptional(provider));
        tag.putString("name", item.name());
        tag.putInt("totalCount", item.totalCount());
        tag.putInt("firstSlotIndex", item.firstSlotIndex());
        tag.putString("islandId", item.islandId());
        tag.putBoolean("recent", item.recent());
        tag.putBoolean("playerPlaced", item.playerPlaced());
        tag.putBoolean("carried", item.carried());
        tag.putBoolean("ghost", item.ghost());
        tag.putInt("proximateCount", item.proximateCount());
        tag.putBoolean("isCarriedContainer", item.isCarriedContainer());
        tag.putInt("containerFreeSlotCount", item.containerFreeSlotCount());
        tag.putInt("containerSlotCapacity", item.containerSlotCapacity());
        ListTag chipTags = new ListTag();
        for (ChipSuggestion chip : item.chipSuggestions()) {
            chipTags.add(encodeChip(chip));
        }
        tag.put("chipSuggestions", chipTags);
        ListTag presenceTags = new ListTag();
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
            presenceTags.add(encodeChestPresence(entry));
        }
        tag.put("presence", presenceTags);
        ListTag elsewhereTags = new ListTag();
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
            elsewhereTags.add(encodeChestPresence(entry));
        }
        tag.put("elsewhere", elsewhereTags);
        tag.putBoolean("kitNeeded", item.kitNeeded());
        tag.putInt("desiredCount", item.desiredCount());
        tag.putBoolean("desiredCountFromKit", item.desiredCountFromKit());
        tag.putInt("wantedCount", item.wantedCount());
        tag.putBoolean("wanted", item.wanted());
        tag.putString("largestCarriedSourceId", item.largestCarriedSourceId());
        tag.putInt("largestCarriedSlotIndex", item.largestCarriedSlotIndex());
        tag.putInt("largestCarriedSlotCount", item.largestCarriedSlotCount());
        return tag;
    }

    private static CompoundTag encodeChestPresence(SlotWorkspaceViewModel.ChestPresenceEntry entry) {
        CompoundTag tag = new CompoundTag();
        tag.putString("storageId", entry.storageId());
        tag.putString("label", entry.label());
        tag.putInt("count", entry.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.ChestPresenceEntry decodeChestPresence(CompoundTag tag) {
        return new SlotWorkspaceViewModel.ChestPresenceEntry(
                tag.getString("storageId"),
                tag.getString("label"),
                tag.getInt("count")
        );
    }

    private static SlotWorkspaceViewModel.AtlasItem decodeItem(HolderLookup.Provider provider, CompoundTag tag) {
        ArrayList<ChipSuggestion> chipSuggestions = new ArrayList<>();
        ListTag chipTags = tag.getList("chipSuggestions", Tag.TAG_COMPOUND);
        for (int index = 0; index < chipTags.size(); index++) {
            ChipSuggestion chip = decodeChip(chipTags.getCompound(index));
            if (chip != null) {
                chipSuggestions.add(chip);
            }
        }
        ArrayList<SlotWorkspaceViewModel.ChestPresenceEntry> presence = new ArrayList<>();
        ListTag presenceTags = tag.getList("presence", Tag.TAG_COMPOUND);
        for (int index = 0; index < presenceTags.size(); index++) {
            presence.add(decodeChestPresence(presenceTags.getCompound(index)));
        }
        ArrayList<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere = new ArrayList<>();
        ListTag elsewhereTags = tag.getList("elsewhere", Tag.TAG_COMPOUND);
        for (int index = 0; index < elsewhereTags.size(); index++) {
            elsewhere.add(decodeChestPresence(elsewhereTags.getCompound(index)));
        }
        // Older payloads (pre largest-carried-slot field) just return 0 for
        // the missing slot index, which is harmless: hasLargestCarriedSlot()
        // also requires count > 0, so the cursor pickup path correctly
        // reads "no info" and falls back to the not-on-hotbar status.
        int decodedSlotIndex = tag.getInt("largestCarriedSlotIndex");
        return new SlotWorkspaceViewModel.AtlasItem(
                decodeIdentity(tag.getCompound("identity")),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getString("name"),
                tag.getInt("totalCount"),
                tag.getInt("firstSlotIndex"),
                tag.getString("islandId"),
                tag.getBoolean("recent"),
                tag.getBoolean("playerPlaced"),
                tag.getBoolean("carried"),
                tag.getBoolean("ghost"),
                tag.getInt("proximateCount"),
                chipSuggestions,
                presence,
                elsewhere,
                tag.getBoolean("isCarriedContainer"),
                tag.getInt("containerFreeSlotCount"),
                tag.getInt("containerSlotCapacity"),
                tag.getBoolean("kitNeeded"),
                tag.getInt("desiredCount"),
                tag.getBoolean("desiredCountFromKit"),
                decodeWantedCount(tag),
                tag.getString("largestCarriedSourceId"),
                decodedSlotIndex,
                tag.getInt("largestCarriedSlotCount")
        );
    }

    private static int decodeWantedCount(CompoundTag tag) {
        if (tag.contains("wantedCount", Tag.TAG_INT)) {
            return tag.getInt("wantedCount");
        }
        return tag.getBoolean("wanted") ? 1 : 0;
    }

    private static CompoundTag encodeChip(ChipSuggestion chip) {
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", chip.kind().name());
        tag.putString("template", chip.template() == null ? "" : chip.template().name());
        tag.putString("islandId", chip.islandId());
        tag.putString("label", chip.label());
        tag.putInt("color", chip.color());
        if (chip.iconIdentity() != null) {
            tag.put("iconIdentity", encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(chip.iconIdentity())));
        }
        return tag;
    }

    private static ChipSuggestion decodeChip(CompoundTag tag) {
        ChipSuggestion.ChipKind kind;
        try {
            kind = ChipSuggestion.ChipKind.valueOf(tag.getString("kind"));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
        IslandSuggestionTemplate template = null;
        String templateName = tag.getString("template");
        if (templateName != null && !templateName.isBlank()) {
            try {
                template = IslandSuggestionTemplate.valueOf(templateName);
            } catch (IllegalArgumentException ignored) {
            }
        }
        ItemIdentity iconIdentity = null;
        if (tag.contains("iconIdentity", Tag.TAG_COMPOUND)) {
            iconIdentity = decodeIdentity(tag.getCompound("iconIdentity")).toIdentity();
        }
        String islandId = tag.getString("islandId");
        String label = tag.getString("label");
        int color = tag.getInt("color");
        if (kind == ChipSuggestion.ChipKind.TEMPLATE && template == null) {
            return null;
        }
        if (kind == ChipSuggestion.ChipKind.LEARNED && (islandId == null || islandId.isBlank())) {
            return null;
        }
        return new ChipSuggestion(kind, template, islandId, label, color, iconIdentity);
    }

    private static CompoundTag encodeChestChip(SlotWorkspaceViewModel.ChestChip chip) {
        CompoundTag tag = new CompoundTag();
        tag.putString("storageId", chip.storageId());
        tag.putString("dimensionId", chip.dimensionId());
        tag.putString("label", chip.label());
        tag.putInt("anchorCount", chip.anchorCount());
        tag.putInt("slotCapacity", chip.slotCapacity());
        tag.putInt("filledSlots", chip.filledSlots());
        tag.putBoolean("proximate", chip.proximate());
        tag.putInt("affinityIdentities", chip.affinityIdentities());
        tag.putInt("worldX", chip.worldX());
        tag.putInt("worldY", chip.worldY());
        tag.putInt("worldZ", chip.worldZ());
        tag.putString("clusterId", chip.clusterId());
        return tag;
    }

    private static SlotWorkspaceViewModel.ChestChip decodeChestChip(CompoundTag tag) {
        return new SlotWorkspaceViewModel.ChestChip(
                tag.getString("storageId"),
                tag.getString("dimensionId"),
                tag.getString("label"),
                tag.getInt("anchorCount"),
                tag.getInt("slotCapacity"),
                tag.getInt("filledSlots"),
                tag.getBoolean("proximate"),
                tag.getInt("affinityIdentities"),
                tag.getInt("worldX"),
                tag.getInt("worldY"),
                tag.getInt("worldZ"),
                tag.getString("clusterId")
        );
    }

    /**
     * The chip's per-identity {@link SlotWorkspaceViewModel.ChestContentSummary
     * contents} list intentionally goes through a sibling sub-list on the
     * chip CompoundTag because the {@code chestChips} list itself is
     * stored as a flat list of {@code CompoundTag} entries. We attach
     * a {@code "contents"} {@link ListTag} so the encode/decode pair
     * round-trips cleanly without reshaping the outer envelope.
     */
    private static void writeChestChipContents(
            CompoundTag tag,
            SlotWorkspaceViewModel.ChestChip chip,
            HolderLookup.Provider provider
    ) {
        ListTag contentsTag = new ListTag();
        for (SlotWorkspaceViewModel.ChestContentSummary summary : chip.contents()) {
            contentsTag.add(encodeChestContentSummary(summary, provider));
        }
        tag.put("contents", contentsTag);
    }

    private static java.util.List<SlotWorkspaceViewModel.ChestContentSummary> readChestChipContents(
            CompoundTag tag,
            HolderLookup.Provider provider
    ) {
        ListTag contentsTag = tag.getList("contents", Tag.TAG_COMPOUND);
        if (contentsTag.size() == 0) {
            return java.util.List.of();
        }
        ArrayList<SlotWorkspaceViewModel.ChestContentSummary> out = new ArrayList<>(contentsTag.size());
        for (int i = 0; i < contentsTag.size(); i++) {
            out.add(decodeChestContentSummary(provider, contentsTag.getCompound(i)));
        }
        return java.util.List.copyOf(out);
    }

    private static CompoundTag encodeChestContentSummary(
            SlotWorkspaceViewModel.ChestContentSummary summary, HolderLookup.Provider provider
    ) {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", summary.itemId());
        tag.putString("componentFingerprint", summary.componentFingerprint());
        tag.putString("name", summary.name());
        tag.put("displayStack", summary.displayStack().saveOptional(provider));
        tag.putInt("count", summary.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.ChestContentSummary decodeChestContentSummary(
            HolderLookup.Provider provider, CompoundTag tag
    ) {
        return new SlotWorkspaceViewModel.ChestContentSummary(
                tag.getString("itemId"),
                tag.getString("componentFingerprint"),
                tag.getString("name"),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getInt("count")
        );
    }

    private static CompoundTag encodeChestCluster(SlotWorkspaceViewModel.ChestClusterDescriptor cluster) {
        CompoundTag tag = new CompoundTag();
        tag.putString("clusterId", cluster.clusterId());
        tag.putString("label", cluster.label());
        tag.putInt("ordinal", cluster.ordinal());
        return tag;
    }

    private static SlotWorkspaceViewModel.ChestClusterDescriptor decodeChestCluster(CompoundTag tag) {
        return new SlotWorkspaceViewModel.ChestClusterDescriptor(
                tag.getString("clusterId"),
                tag.getString("label"),
                tag.getInt("ordinal")
        );
    }

    private static CompoundTag encodeLootChestPanel(
            SlotWorkspaceViewModel.LootChestPanel panel, HolderLookup.Provider provider
    ) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("chestX", panel.chestX());
        tag.putInt("chestY", panel.chestY());
        tag.putInt("chestZ", panel.chestZ());
        tag.putString("dimensionId", panel.dimensionId());
        tag.putString("label", panel.label());
        ListTag itemTags = new ListTag();
        for (SlotWorkspaceViewModel.AtlasItem item : panel.items()) {
            itemTags.add(encodeItem(item, provider));
        }
        tag.put("items", itemTags);
        return tag;
    }

    private static SlotWorkspaceViewModel.LootChestPanel decodeLootChestPanel(
            HolderLookup.Provider provider, CompoundTag tag
    ) {
        if (tag == null) {
            return SlotWorkspaceViewModel.LootChestPanel.empty();
        }
        // Empty (unset) dimensionId is the sentinel for "no panel" — the
        // server only writes a non-blank dimension when a loot chest is
        // active. Avoids relying on CompoundTag#contains, which isn't on
        // the test-classpath stub.
        String dimensionId = tag.getString("dimensionId");
        if (dimensionId == null || dimensionId.isBlank()) {
            return SlotWorkspaceViewModel.LootChestPanel.empty();
        }
        ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        ListTag itemTags = tag.getList("items", Tag.TAG_COMPOUND);
        for (int index = 0; index < itemTags.size(); index++) {
            items.add(decodeItem(provider, itemTags.getCompound(index)));
        }
        return new SlotWorkspaceViewModel.LootChestPanel(
                tag.getInt("chestX"),
                tag.getInt("chestY"),
                tag.getInt("chestZ"),
                dimensionId,
                tag.getString("label"),
                items
        );
    }

    private static CompoundTag encodeHotbar(SlotWorkspaceViewModel.HotbarSlot slot, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("hotbarIndex", slot.hotbarIndex());
        tag.putBoolean("selected", slot.selected());
        tag.putBoolean("occupied", slot.occupied());
        tag.put("displayStack", slot.displayStack().saveOptional(provider));
        tag.putInt("count", slot.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.HotbarSlot decodeHotbar(HolderLookup.Provider provider, CompoundTag tag) {
        return new SlotWorkspaceViewModel.HotbarSlot(
                tag.getInt("hotbarIndex"),
                tag.getBoolean("selected"),
                tag.getBoolean("occupied"),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getInt("count")
        );
    }

    private static CompoundTag encodeOffhand(SlotWorkspaceViewModel.OffhandSlot offhand, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("occupied", offhand.occupied());
        tag.put("displayStack", offhand.displayStack().saveOptional(provider));
        tag.putInt("count", offhand.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.OffhandSlot decodeOffhand(HolderLookup.Provider provider, CompoundTag tag) {
        return new SlotWorkspaceViewModel.OffhandSlot(
                tag.getBoolean("occupied"),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getInt("count")
        );
    }

    private static VisualAtlasIslandKind decodeIslandKind(String raw) {
        try {
            return raw == null || raw.isBlank()
                    ? VisualAtlasIslandKind.PLAYER
                    : VisualAtlasIslandKind.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return VisualAtlasIslandKind.PLAYER;
        }
    }
}
