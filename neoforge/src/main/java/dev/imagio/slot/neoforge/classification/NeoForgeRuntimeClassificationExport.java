package dev.imagio.slot.neoforge.classification;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.imagio.slot.classification.runtime.RuntimeClassificationExportBuilder;
import dev.imagio.slot.classification.runtime.RuntimeClassificationExportBuilder.BlockEntry;
import dev.imagio.slot.classification.runtime.RuntimeClassificationExportBuilder.ItemEntry;
import dev.imagio.slot.classification.runtime.RuntimeClassificationExportBuilder.RecipeIndex;
import dev.imagio.slot.classification.runtime.RuntimeClassificationExportWriter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class NeoForgeRuntimeClassificationExport {
    private NeoForgeRuntimeClassificationExport() {
    }

    public static RuntimeClassificationExportWriter.Result export(
            MinecraftServer server,
            String requestedPackId
    ) throws IOException {
        Objects.requireNonNull(server, "server");
        RecipeIndex recipeIndex = buildRecipeIndex(server);
        ArrayList<ItemEntry> items = new ArrayList<>();

        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            if (key == null) {
                continue;
            }
            ItemStack stack = item.getDefaultInstance();
            if (stack.isEmpty()) {
                continue;
            }
            items.add(new ItemEntry(
                    key.toString(),
                    key.getNamespace(),
                    key.getPath(),
                    stack.getHoverName().getString(),
                    stack.getDescriptionId(),
                    itemTags(stack),
                    componentData(item, stack),
                    blockEntry(item)
            ));
        }

        return RuntimeClassificationExportBuilder.write(
                FMLPaths.CONFIGDIR.get(),
                requestedPackId,
                defaultPackId(server),
                "neoforge",
                "1.21.1",
                items,
                recipeIndex
        );
    }

    private static String defaultPackId(MinecraftServer server) {
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        Path fileName = worldRoot == null ? null : worldRoot.getFileName();
        return fileName == null ? "runtime" : fileName.toString();
    }

    private static JsonObject componentData(Item item, ItemStack stack) {
        JsonObject components = new JsonObject();
        components.addProperty("minecraft:max_stack_size", stack.getMaxStackSize());
        if (stack.getMaxDamage() > 0) {
            components.addProperty("minecraft:max_damage", stack.getMaxDamage());
        }
        if (stack.isEnchantable()) {
            components.add("minecraft:enchantable", new JsonObject());
        }
        components.addProperty("minecraft:rarity", stack.getRarity().name().toLowerCase(Locale.ROOT));

        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food != null) {
            components.add("minecraft:food", foodData(food));
        }

        if (item instanceof ArmorItem armorItem) {
            JsonObject equippable = new JsonObject();
            equippable.addProperty("slot", armorItem.getEquipmentSlot().getName());
            components.add("minecraft:equippable", equippable);
        }
        if (item instanceof BlockItem blockItem) {
            int light = blockItem.getBlock().defaultBlockState().getLightEmission();
            if (light > 0) {
                components.addProperty("minecraft:light_emission", light);
            }
        }
        return components;
    }

    private static JsonObject foodData(FoodProperties food) {
        JsonObject object = new JsonObject();
        object.addProperty("nutrition", food.nutrition());
        object.addProperty("saturation_modifier", food.saturation());
        object.addProperty("can_always_eat", food.canAlwaysEat());
        object.addProperty("eat_seconds", food.eatSeconds());
        object.addProperty("eat_duration_ticks", food.eatDurationTicks());
        food.usingConvertsTo().ifPresent(remainder -> {
            JsonObject convertsTo = new JsonObject();
            convertsTo.addProperty("id", itemId(remainder.getItem()));
            convertsTo.addProperty("count", remainder.getCount());
            object.add("using_converts_to", convertsTo);
        });

        JsonArray effects = new JsonArray();
        for (FoodProperties.PossibleEffect entry : food.effects()) {
            MobEffectInstance effect = entry.effect();
            if (effect == null) {
                continue;
            }
            JsonObject effectData = new JsonObject();
            effectData.addProperty("effect", effectId(effect));
            effectData.addProperty("duration", effect.getDuration());
            effectData.addProperty("amplifier", effect.getAmplifier());
            effectData.addProperty("chance", entry.probability());
            effects.add(effectData);
        }
        object.add("effects", effects);
        return object;
    }

    private static String effectId(MobEffectInstance effect) {
        ResourceLocation key = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
        return key == null ? "" : key.toString();
    }

    private static BlockEntry blockEntry(Item item) {
        if (!(item instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(block);
        BlockState state = block.defaultBlockState();
        return new BlockEntry(
                blockKey == null ? "" : blockKey.toString(),
                state.requiresCorrectToolForDrops(),
                blockTags(state)
        );
    }

    private static List<String> itemTags(ItemStack stack) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        stack.getTags().forEach(tagKey -> {
            ResourceLocation location = tagKey.location();
            if (location != null) {
                tags.add(location.toString());
            }
        });
        return RuntimeClassificationExportBuilder.sortedStrings(tags);
    }

    private static List<String> blockTags(BlockState state) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        state.getTags().forEach(tagKey -> {
            ResourceLocation location = tagKey.location();
            if (location != null) {
                tags.add(location.toString());
            }
        });
        return RuntimeClassificationExportBuilder.sortedStrings(tags);
    }

    private static RecipeIndex buildRecipeIndex(MinecraftServer server) {
        RecipeIndex index = new RecipeIndex();
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            Recipe<?> recipe = holder.value();
            String kind = recipeKind(recipe);
            index.addRecipe(kind);
            String recipeId = holder.id().toString();
            LinkedHashSet<String> ingredientItems = new LinkedHashSet<>();

            for (Ingredient ingredient : recipe.getIngredients()) {
                for (ItemStack ingredientStack : ingredient.getItems()) {
                    if (ingredientStack == null || ingredientStack.isEmpty()) {
                        continue;
                    }
                    String id = itemId(ingredientStack.getItem());
                    if (id != null) {
                        ingredientItems.add(id);
                    }
                }
            }
            for (String id : ingredientItems) {
                index.addIngredient(id, recipeId, kind);
            }

            ItemStack result = recipe.getResultItem(server.registryAccess());
            if (result != null && !result.isEmpty()) {
                String id = itemId(result.getItem());
                if (id != null) {
                    index.addOutput(id, recipeId, kind);
                }
            }
        }
        return index;
    }

    private static String itemId(Item item) {
        if (item == null) {
            return null;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key == null ? null : key.toString();
    }

    private static String recipeKind(Recipe<?> recipe) {
        ResourceLocation serializer = BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer());
        if (serializer != null) {
            return shortRecipeType(serializer.toString());
        }
        ResourceLocation type = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
        return shortRecipeType(type == null ? null : type.toString());
    }

    private static String shortRecipeType(String type) {
        if (type == null) {
            return "unknown";
        }
        return type.startsWith("minecraft:") ? type.substring("minecraft:".length()) : type;
    }
}
