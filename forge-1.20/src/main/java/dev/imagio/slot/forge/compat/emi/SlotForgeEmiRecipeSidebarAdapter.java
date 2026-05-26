package dev.imagio.slot.forge.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.forge.client.ForgeContainerSidebar;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.ItemStackTags;
import dev.imagio.slot.ui.workspace.RecipeIngredientSidebarSpec;
import dev.imagio.slot.workflow.domain.CraftRunAlternative;
import dev.imagio.slot.workflow.domain.CraftRunIngredientGroup;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

final class SlotForgeEmiRecipeSidebarAdapter {
    private static final int MAX_INPUTS_PER_RECIPE = 128;
    private static final int MAX_ALTERNATIVES_PER_INPUT = 128;
    private static final int RECIPE_TAB_OVERHANG_PX = 24;
    private static final int RECENTS_RECIPE_GAP_PX = 4;
    private static final int RECIPE_BOTTOM_MARGIN_PX = 4;
    private static final String EMI_RECIPE_SCREEN = "dev.emi.emi.screen.RecipeScreen";
    private static boolean recipeScreenReflectionWarningLogged;

    private SlotForgeEmiRecipeSidebarAdapter() {
    }

    static ForgeContainerSidebar.SidebarHost sidebarHost(Screen screen) {
        if (!isEmiRecipeScreen(screen)) {
            return null;
        }
        Object oldScreen = fieldValue(screen, "old");
        if (oldScreen instanceof AbstractContainerScreen<?> containerScreen) {
            return new ForgeContainerSidebar.SidebarHost(screen, containerScreen);
        }
        return null;
    }

    static boolean hasSidebarHost(Screen screen) {
        ForgeContainerSidebar.SidebarHost host = sidebarHost(screen);
        return host != null && !host.menuScreen().getClass().getName().startsWith("dev.imagio.slot.");
    }

    static void constrainRecipeScreenBelowRecents(Screen screen, int recentsBottom) {
        if (!isEmiRecipeScreen(screen)) {
            return;
        }
        int currentY = intField(screen, "y", Integer.MIN_VALUE);
        int currentHeight = intField(screen, "backgroundHeight", -1);
        if (currentY == Integer.MIN_VALUE || currentHeight <= 0) {
            return;
        }
        int requiredY = recentsBottom + RECENTS_RECIPE_GAP_PX + RECIPE_TAB_OVERHANG_PX;
        if (currentY >= requiredY) {
            return;
        }
        int availableHeight = screen.height - requiredY - RECIPE_BOTTOM_MARGIN_PX;
        if (availableHeight <= 0) {
            return;
        }
        int newHeight = Math.min(currentHeight, availableHeight);
        if (!setRecipeScreenBounds(screen, requiredY, newHeight)) {
            return;
        }
        rebakeRecipeTabs(screen, newHeight);
        invokeSetPage(
                screen,
                intField(screen, "tabPage", 0),
                intField(screen, "tab", 0),
                intField(screen, "page", 0));
    }

    static RecipeIngredientSidebarSpec recipeSidebarSpec(Screen screen) {
        List<EmiRecipe> recipes = visibleRecipes(screen);
        if (recipes.isEmpty()) {
            return RecipeIngredientSidebarSpec.empty();
        }
        ArrayList<RecipeIngredientSidebarSpec.Ingredient> ingredients = new ArrayList<>();
        StringBuilder sourceKey = new StringBuilder("emi:recipe-sidebar");
        for (int recipeIndex = 0; recipeIndex < recipes.size(); recipeIndex++) {
            EmiRecipe recipe = recipes.get(recipeIndex);
            if (recipe == null) {
                continue;
            }
            String recipeId = recipeId(recipe);
            sourceKey.append("|r:").append(recipeId);
            List<EmiIngredient> inputs = recipe.getInputs();
            if (inputs == null || inputs.isEmpty()) {
                continue;
            }
            for (int inputIndex = 0; inputIndex < inputs.size() && inputIndex < MAX_INPUTS_PER_RECIPE; inputIndex++) {
                EmiIngredient input = inputs.get(inputIndex);
                if (input == null || input.isEmpty()) {
                    continue;
                }
                RecipeIngredientSidebarSpec.Ingredient ingredient =
                        ingredientSpec(input, recipeId, recipeIndex, inputIndex);
                ingredients.add(ingredient);
                appendIngredientKey(sourceKey, ingredient);
            }
        }
        if (ingredients.isEmpty()) {
            return RecipeIngredientSidebarSpec.empty();
        }
        return new RecipeIngredientSidebarSpec(
                sourceKey.toString(),
                recipes.size() == 1 ? "EMI recipe ingredients" : "Visible EMI recipe ingredients",
                ingredients);
    }

    static List<CraftRunRecipeCapture> craftRunRecipeCaptures(Screen screen) {
        List<VisibleRecipe> recipes = visibleRecipeEntries(screen);
        if (recipes.isEmpty()) {
            return List.of();
        }
        ArrayList<CraftRunRecipeCapture> captures = new ArrayList<>();
        for (VisibleRecipe visible : recipes) {
            CraftRunRecipeCapture capture = craftRunRecipeCapture(visible.recipe(), visible.index());
            if (capture.active()) {
                captures.add(capture);
            }
        }
        return captures.isEmpty() ? List.of() : List.copyOf(captures);
    }

    static CraftRunRecipeCapture hoveredCraftRunRecipeCapture(Screen screen) {
        List<VisibleRecipe> recipes = visibleRecipeEntries(screen);
        if (recipes.isEmpty()) {
            return CraftRunRecipeCapture.empty();
        }
        double mouseX = guiMouseX();
        double mouseY = guiMouseY();
        for (VisibleRecipe visible : recipes) {
            if (groupContains(visible.group(), mouseX, mouseY)) {
                CraftRunRecipeCapture capture = craftRunRecipeCapture(visible.recipe(), visible.index());
                return capture.active() ? capture : CraftRunRecipeCapture.empty();
            }
        }
        if (recipes.size() == 1) {
            CraftRunRecipeCapture capture = craftRunRecipeCapture(recipes.get(0).recipe(), recipes.get(0).index());
            return capture.active() ? capture : CraftRunRecipeCapture.empty();
        }
        return CraftRunRecipeCapture.empty();
    }

    private static CraftRunRecipeCapture craftRunRecipeCapture(EmiRecipe recipe, int recipeIndex) {
        if (recipe == null) {
            return CraftRunRecipeCapture.empty();
        }
        Output output = firstOutput(recipe);
        if (output == null || output.identity() == null) {
            return CraftRunRecipeCapture.empty();
        }
        String recipeId = recipeId(recipe);
        ArrayList<CraftRunIngredientGroup> groups = new ArrayList<>();
        ArrayList<String> diagnostics = new ArrayList<>();
        List<EmiIngredient> inputs = recipe.getInputs();
        if (inputs != null) {
            for (int inputIndex = 0; inputIndex < inputs.size() && inputIndex < MAX_INPUTS_PER_RECIPE; inputIndex++) {
                CraftRunIngredientGroup group = craftRunIngredient(inputs.get(inputIndex), recipe, recipeId, inputIndex);
                if (group != null && group.resolvable()) {
                    groups.add(group);
                }
            }
            if (inputs.size() > MAX_INPUTS_PER_RECIPE) {
                diagnostics.add("input_limit:" + inputs.size());
            }
        }
        if (groups.isEmpty()) {
            return CraftRunRecipeCapture.empty();
        }
        return new CraftRunRecipeCapture(
                craftRunSourceKey(recipeId, recipeIndex, output, groups),
                recipeId,
                output.label(),
                output.identity(),
                output.label(),
                output.count(),
                output.count(),
                groups,
                diagnostics);
    }

    private static CraftRunIngredientGroup craftRunIngredient(
            EmiIngredient input,
            EmiRecipe recipe,
            String recipeId,
            int inputIndex
    ) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        int requiredCount = safeCount(input.getAmount());
        ConsumptionClassification consumption = classifyInputConsumption(input, recipe);
        LinkedHashMap<String, CraftRunAlternative> alternatives = new LinkedHashMap<>();
        ArrayList<String> diagnostics = new ArrayList<>();
        if (!consumption.diagnostic().isBlank()) {
            diagnostics.add(consumption.diagnostic());
        }
        String firstNonItemLabel = "";
        for (EmiStack emiStack : input.getEmiStacks()) {
            if (emiStack == null || emiStack.isEmpty()) {
                continue;
            }
            ItemStack stack = itemStack(emiStack);
            if (stack == null || stack.isEmpty()) {
                if (firstNonItemLabel.isBlank()) {
                    firstNonItemLabel = nonItemAlternativeLabel(emiStack);
                }
                diagnostics.add("non_item_alternative");
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.create(stack);
            alternatives.putIfAbsent(identityKey(identity), new CraftRunAlternative(identity, labelFor(emiStack, stack)));
        }
        String label = alternatives.isEmpty()
                ? firstNonItemLabel.isBlank() ? "Ingredient " + (inputIndex + 1) : firstNonItemLabel
                : alternatives.values().iterator().next().label();
        return new CraftRunIngredientGroup(
                sanitize(recipeId) + "/input_" + inputIndex,
                label,
                requiredCount,
                consumption.consumed(),
                List.copyOf(alternatives.values()),
                diagnostics);
    }

    private static Output firstOutput(EmiRecipe recipe) {
        List<EmiStack> outputs = recipe.getOutputs();
        if (outputs == null || outputs.isEmpty()) {
            return null;
        }
        for (EmiStack output : outputs) {
            if (output == null || output.isEmpty()) {
                continue;
            }
            ItemStack stack = itemStack(output);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.create(stack);
            int count = safeCount(output.getAmount());
            if (count <= 0) {
                count = Math.max(1, stack.getCount());
            }
            return new Output(identity, labelFor(output, stack), count);
        }
        return null;
    }

    private static RecipeIngredientSidebarSpec.Ingredient ingredientSpec(
            EmiIngredient input,
            String recipeId,
            int recipeIndex,
            int inputIndex
    ) {
        int requiredCount = safeCount(input.getAmount());
        LinkedHashMap<String, RecipeIngredientSidebarSpec.Alternative> alternatives = new LinkedHashMap<>();
        String firstNonItemLabel = "";
        int inspected = 0;
        for (EmiStack emiStack : input.getEmiStacks()) {
            if (inspected++ >= MAX_ALTERNATIVES_PER_INPUT) {
                break;
            }
            if (emiStack == null || emiStack.isEmpty()) {
                continue;
            }
            ItemStack stack = itemStack(emiStack);
            if (stack == null || stack.isEmpty()) {
                if (firstNonItemLabel.isBlank()) {
                    firstNonItemLabel = nonItemAlternativeLabel(emiStack);
                }
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.create(stack);
            String label = labelFor(emiStack, stack);
            ItemStack displayStack = stack.copy();
            displayStack.setCount(Math.min(displayStack.getMaxStackSize(), requiredCount));
            alternatives.putIfAbsent(
                    identity.itemId(),
                    new RecipeIngredientSidebarSpec.Alternative(identity, label, requiredCount, displayStack));
        }
        String label = alternatives.isEmpty()
                ? firstNonItemLabel.isBlank() ? "Ingredient " + (inputIndex + 1) : firstNonItemLabel
                : alternatives.values().iterator().next().label();
        return new RecipeIngredientSidebarSpec.Ingredient(
                sanitize(recipeId) + "/input_" + recipeIndex + "_" + inputIndex,
                label,
                requiredCount,
                List.copyOf(alternatives.values()));
    }

    private static List<EmiRecipe> visibleRecipes(Screen screen) {
        List<VisibleRecipe> entries = visibleRecipeEntries(screen);
        if (entries.isEmpty()) {
            return List.of();
        }
        ArrayList<EmiRecipe> recipes = new ArrayList<>();
        for (VisibleRecipe entry : entries) {
            recipes.add(entry.recipe());
        }
        return List.copyOf(recipes);
    }

    private static List<VisibleRecipe> visibleRecipeEntries(Screen screen) {
        if (!isEmiRecipeScreen(screen)) {
            return List.of();
        }
        Object currentPage = fieldValue(screen, "currentPage");
        if (!(currentPage instanceof List<?> groups) || groups.isEmpty()) {
            return List.of();
        }
        ArrayList<VisibleRecipe> recipes = new ArrayList<>();
        int recipeIndex = 0;
        for (Object group : groups) {
            EmiRecipe recipe = emiRecipeField(group);
            if (recipe != null) {
                recipes.add(new VisibleRecipe(recipe, group, recipeIndex));
                recipeIndex++;
            }
        }
        return List.copyOf(recipes);
    }

    private static boolean groupContains(Object group, double mouseX, double mouseY) {
        if (group == null) {
            return false;
        }
        int x = intField(group, "x", Integer.MIN_VALUE);
        int y = intField(group, "y", Integer.MIN_VALUE);
        int width = intField(group, "width", 0);
        int height = intField(group, "height", 0);
        return width > 0
                && height > 0
                && mouseX >= x
                && mouseY >= y
                && mouseX < x + width
                && mouseY < y + height;
    }

    private static boolean isEmiRecipeScreen(Screen screen) {
        return screen != null && EMI_RECIPE_SCREEN.equals(screen.getClass().getName());
    }

    private static EmiRecipe emiRecipeField(Object target) {
        Object recipe = fieldValue(target, "recipe");
        return recipe instanceof EmiRecipe emiRecipe ? emiRecipe : null;
    }

    private static Object fieldValue(Object target, String fieldName) {
        if (target == null) {
            return null;
        }
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException | RuntimeException error) {
            logRecipeScreenReflectionFailure(error);
            return null;
        }
    }

    private static int intField(Object target, String fieldName, int fallback) {
        Object value = fieldValue(target, fieldName);
        return value instanceof Number number ? number.intValue() : fallback;
    }

    private static boolean setRecipeScreenBounds(Object target, int y, int height) {
        try {
            Field yField = target.getClass().getDeclaredField("y");
            Field heightField = target.getClass().getDeclaredField("backgroundHeight");
            yField.setAccessible(true);
            heightField.setAccessible(true);
            yField.setInt(target, y);
            heightField.setInt(target, height);
            return true;
        } catch (ReflectiveOperationException | RuntimeException error) {
            logRecipeScreenReflectionFailure(error);
            return false;
        }
    }

    private static void rebakeRecipeTabs(Object screen, int backgroundHeight) {
        Object value = fieldValue(screen, "tabs");
        if (!(value instanceof List<?> tabs)) {
            return;
        }
        for (Object tab : tabs) {
            if (tab == null) {
                continue;
            }
            try {
                Method method = tab.getClass().getDeclaredMethod("bakePages", int.class);
                method.setAccessible(true);
                method.invoke(tab, backgroundHeight);
            } catch (ReflectiveOperationException | RuntimeException error) {
                logRecipeScreenReflectionFailure(error);
                return;
            }
        }
    }

    private static void invokeSetPage(Object screen, int tabPage, int tab, int page) {
        try {
            Method method = screen.getClass().getDeclaredMethod("setPage", int.class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(screen, tabPage, tab, page);
        } catch (ReflectiveOperationException | RuntimeException error) {
            logRecipeScreenReflectionFailure(error);
        }
    }

    private static double guiMouseX() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.mouseHandler == null) {
            return 0;
        }
        return minecraft.mouseHandler.xpos()
                * minecraft.getWindow().getGuiScaledWidth()
                / minecraft.getWindow().getScreenWidth();
    }

    private static double guiMouseY() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null || minecraft.mouseHandler == null) {
            return 0;
        }
        return minecraft.mouseHandler.ypos()
                * minecraft.getWindow().getGuiScaledHeight()
                / minecraft.getWindow().getScreenHeight();
    }

    private static void logRecipeScreenReflectionFailure(Throwable error) {
        if (recipeScreenReflectionWarningLogged) {
            return;
        }
        recipeScreenReflectionWarningLogged = true;
        SlotCommon.LOGGER.warn("[SLOT][emi] cannot inspect EMI recipe screen for Forge recipe sidebar", error);
    }

    private static String craftRunSourceKey(
            String recipeId,
            int recipeIndex,
            Output output,
            List<CraftRunIngredientGroup> groups
    ) {
        StringBuilder sourceKey = new StringBuilder("emi:craft-run|r:")
                .append(recipeId)
                .append("|visible:")
                .append(recipeIndex)
                .append("|out:")
                .append(output == null ? "" : identityKey(output.identity()))
                .append("|inputs:")
                .append(groups == null ? 0 : groups.size());
        if (groups != null) {
            for (CraftRunIngredientGroup group : groups) {
                appendCraftRunGroupKey(sourceKey, group);
            }
        }
        return sourceKey.toString();
    }

    private static void appendCraftRunGroupKey(StringBuilder sourceKey, CraftRunIngredientGroup group) {
        if (sourceKey == null || group == null) {
            return;
        }
        sourceKey.append("|g:")
                .append(group.groupId())
                .append("x")
                .append(group.requiredCountPerBatch())
                .append(group.consumed() ? "c" : "r");
        for (CraftRunAlternative alternative : group.alternatives()) {
            sourceKey.append(":")
                    .append(alternative == null ? "opaque" : identityKey(alternative.identity()));
        }
    }

    private static void appendIngredientKey(StringBuilder sourceKey, RecipeIngredientSidebarSpec.Ingredient ingredient) {
        sourceKey.append("|i:")
                .append(ingredient.ingredientId())
                .append("x")
                .append(ingredient.requiredCount());
        for (RecipeIngredientSidebarSpec.Alternative alternative : ingredient.alternatives()) {
            sourceKey.append(":")
                    .append(alternative.identity() == null ? "opaque" : alternative.identity().itemId());
        }
    }

    private static ItemStack itemStack(EmiStack emiStack) {
        try {
            ItemStack stack = emiStack.getItemStack();
            return stack == null ? ItemStack.EMPTY : stack;
        } catch (RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static ConsumptionClassification classifyInputConsumption(EmiIngredient input, EmiRecipe recipe) {
        if (input == null || input.isEmpty()) {
            return ConsumptionClassification.CONSUMED;
        }
        int requiredCount = safeCount(input.getAmount());
        boolean sawConcreteItem = false;
        boolean allRemaindersSelf = true;
        boolean allReusableTools = true;
        boolean allReusableMolds = true;
        for (EmiStack emiStack : input.getEmiStacks()) {
            if (emiStack == null || emiStack.isEmpty()) {
                continue;
            }
            ItemStack stack = itemStack(emiStack);
            if (stack == null || stack.isEmpty()) {
                allRemaindersSelf = false;
                allReusableTools = false;
                allReusableMolds = false;
                continue;
            }
            sawConcreteItem = true;
            if (!remainderIsSelf(emiStack)) {
                allRemaindersSelf = false;
            }
            allReusableTools = allReusableTools && reusableToolInput(requiredCount, stack);
            allReusableMolds = allReusableMolds && reusableMoldInput(requiredCount, stack, recipe);
        }
        if (!sawConcreteItem) {
            return ConsumptionClassification.CONSUMED;
        }
        if (allRemaindersSelf) {
            return ConsumptionClassification.REMAINDER;
        }
        if (allReusableTools) {
            return ConsumptionClassification.TOOL;
        }
        if (allReusableMolds) {
            return ConsumptionClassification.MOLD;
        }
        if (listedAsCatalyst(input, recipe)) {
            return ConsumptionClassification.CATALYST;
        }
        return ConsumptionClassification.CONSUMED;
    }

    private static boolean remainderIsSelf(EmiStack emiStack) {
        try {
            EmiStack remainder = emiStack.getRemainder();
            return remainder != null && !remainder.isEmpty() && remainder.isEqual(emiStack);
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean reusableToolInput(int requiredCount, ItemStack stack) {
        return requiredCount == 1
                && stack != null
                && !stack.isEmpty()
                && stack.getMaxStackSize() == 1
                && (stack.isDamageableItem() || hasToolTag(stack) || itemPathLooksLikeReusableTool(stack));
    }

    private static boolean hasToolTag(ItemStack stack) {
        for (String tag : ItemStackTags.itemTagIds(stack)) {
            String normalized = tag == null ? "" : tag.toLowerCase(java.util.Locale.ROOT).trim();
            int namespace = normalized.indexOf(':');
            String path = namespace >= 0 ? normalized.substring(namespace + 1) : normalized;
            if (path.equals("tools") || path.startsWith("tools/")) {
                return true;
            }
        }
        return false;
    }

    private static boolean itemPathLooksLikeReusableTool(ItemStack stack) {
        ItemIdentity identity = ItemIdentityMatcher.itemOnly(stack);
        String itemId = identity.itemId().toLowerCase(java.util.Locale.ROOT);
        String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        return path.contains("hammer")
                || path.contains("wrench")
                || path.contains("screwdriver")
                || path.contains("wire_cutter")
                || path.contains("saw")
                || path.endsWith("/file")
                || path.endsWith("_file");
    }

    private static boolean reusableMoldInput(int requiredCount, ItemStack stack, EmiRecipe recipe) {
        return requiredCount == 1
                && stack != null
                && !stack.isEmpty()
                && reusableMoldRecipe(recipe)
                && looksLikeFiredOrMachineMold(stack);
    }

    private static boolean reusableMoldRecipe(EmiRecipe recipe) {
        String category = recipeCategoryId(recipe).toLowerCase(java.util.Locale.ROOT);
        return category.contains("casting")
                || category.contains("molding")
                || category.contains("mold")
                || category.contains("extrud");
    }

    private static boolean looksLikeFiredOrMachineMold(ItemStack stack) {
        ItemIdentity identity = ItemIdentityMatcher.itemOnly(stack);
        String itemId = identity.itemId().toLowerCase(java.util.Locale.ROOT);
        String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        if (path.contains("unfired")) {
            return false;
        }
        for (String tag : ItemStackTags.itemTagIds(stack)) {
            String normalized = tag == null ? "" : tag.toLowerCase(java.util.Locale.ROOT).trim();
            if (normalized.contains("fired_molds")
                    || normalized.contains("casting_molds")
                    || normalized.contains("extruder_molds")
                    || normalized.endsWith(":molds")
                    || normalized.endsWith("/molds")) {
                return true;
            }
        }
        return path.contains("mold");
    }

    private static boolean listedAsCatalyst(EmiIngredient input, EmiRecipe recipe) {
        if (input == null || recipe == null) {
            return false;
        }
        try {
            List<EmiIngredient> catalysts = recipe.getCatalysts();
            if (catalysts == null || catalysts.isEmpty()) {
                return false;
            }
            for (EmiIngredient catalyst : catalysts) {
                if (catalyst != null && EmiIngredient.areEqual(input, catalyst)) {
                    return true;
                }
            }
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
        return false;
    }

    private static String recipeCategoryId(EmiRecipe recipe) {
        try {
            ResourceLocation id = recipe == null || recipe.getCategory() == null ? null : recipe.getCategory().getId();
            return id == null ? "" : id.toString();
        } catch (RuntimeException | LinkageError ignored) {
            return "";
        }
    }

    private static String labelFor(EmiStack emiStack, ItemStack fallback) {
        try {
            Component name = emiStack.getName();
            if (name != null && !name.getString().isBlank()) {
                return name.getString();
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        return fallback == null || fallback.isEmpty() ? "Ingredient" : fallback.getHoverName().getString();
    }

    private static String nonItemAlternativeLabel(EmiStack emiStack) {
        if (emiStack == null || emiStack.isEmpty()) {
            return "";
        }
        try {
            Component name = emiStack.getName();
            if (name != null && !name.getString().isBlank()) {
                return name.getString();
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        try {
            ResourceLocation id = emiStack.getId();
            if (id != null) {
                return id.toString();
            }
        } catch (RuntimeException | LinkageError ignored) {
        }
        try {
            Object key = emiStack.getKey();
            return key == null ? "" : key.toString();
        } catch (RuntimeException | LinkageError ignored) {
            return "";
        }
    }

    private static String recipeId(EmiRecipe recipe) {
        ResourceLocation id = recipe == null ? null : recipe.getId();
        if (id != null) {
            return id.toString();
        }
        return "slot:anonymous_emi_recipe/" + Integer.toHexString(System.identityHashCode(recipe));
    }

    private static int safeCount(long amount) {
        if (amount <= 0) {
            return 1;
        }
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    private static String sanitize(String value) {
        String input = value == null || value.isBlank() ? "ingredient" : value;
        StringBuilder builder = new StringBuilder(input.length());
        for (int index = 0; index < input.length(); index++) {
            char ch = Character.toLowerCase(input.charAt(index));
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
                    || ch == '_' || ch == '-' || ch == '.' || ch == '/') {
                builder.append(ch);
            } else {
                builder.append('_');
            }
        }
        return builder.length() == 0 ? "ingredient" : builder.toString();
    }

    private static String identityKey(ItemIdentity identity) {
        if (identity == null) {
            return "";
        }
        return identity.itemId() + "|" + identity.comparisonMode().name() + "|" + identity.componentFingerprint();
    }

    private record ConsumptionClassification(boolean consumed, String diagnostic) {
        private static final ConsumptionClassification CONSUMED = new ConsumptionClassification(true, "");
        private static final ConsumptionClassification REMAINDER = new ConsumptionClassification(false, "reusable_remainder");
        private static final ConsumptionClassification TOOL = new ConsumptionClassification(false, "reusable_tool");
        private static final ConsumptionClassification MOLD = new ConsumptionClassification(false, "reusable_mold");
        private static final ConsumptionClassification CATALYST = new ConsumptionClassification(false, "reusable_catalyst");
    }

    private record VisibleRecipe(EmiRecipe recipe, Object group, int index) {
    }

    private record Output(ItemIdentity identity, String label, int count) {
    }
}
