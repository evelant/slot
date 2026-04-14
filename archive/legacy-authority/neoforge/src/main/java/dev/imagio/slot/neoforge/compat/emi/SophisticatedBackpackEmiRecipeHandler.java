package dev.imagio.slot.neoforge.compat.emi;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.compat.sophisticated.SophisticatedBackpackUpgradeSupport;
import dev.imagio.slot.client.screen.debug.SlotDebugInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public final class SophisticatedBackpackEmiRecipeHandler implements EmiRecipeHandler<InventoryMenu> {
    private static final SophisticatedBackpackEmiRecipeHandler INSTANCE = new SophisticatedBackpackEmiRecipeHandler();
    private static final ReflectionState REFLECTION = ReflectionState.load();
    private static final Set<AbstractContainerMenu> LOGGED_OVERRIDE_MENUS = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<String> SUPPORTED_CATEGORY_IDS = Set.of(
            "minecraft:crafting",
            "emi:crafting"
    );

    private SophisticatedBackpackEmiRecipeHandler() {
    }

    public static Object preferredHandlerForScreen(Object screen, Object recipe, Object currentHandler) {
        if (!(screen instanceof SlotDebugInventoryScreen slotScreen)
                || !(recipe instanceof EmiRecipe emiRecipe)
                || !INSTANCE.supportsRecipe(emiRecipe)
                || !REFLECTION.available()
                || !hasBackpackCraftingUpgrade(slotScreen)) {
            return currentHandler;
        }

        AbstractContainerMenu observedMenu = slotScreen.emiObservedMenu();
        if (currentHandler != INSTANCE && observedMenu != null && LOGGED_OVERRIDE_MENUS.add(observedMenu)) {
            SlotDebugLog.log(
                    "Overriding EMI handler for SLOT Sophisticated backpack crafting: previous={} menu={}",
                    currentHandler == null ? "<none>" : currentHandler.getClass().getName(),
                    observedMenu == null ? "<none>" : observedMenu.getClass().getName()
            );
        }
        return INSTANCE;
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<InventoryMenu> screen) {
        DelegatedContext delegated = delegatedContext(screen, false);
        if (delegated == null) {
            return new EmiPlayerInventory(List.of());
        }
        return delegated.delegate().getInventory(delegated.screen());
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        if (recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING) {
            return true;
        }
        return recipe.getCategory() != null
                && SUPPORTED_CATEGORY_IDS.contains(recipe.getCategory().getId().toString());
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<InventoryMenu> context) {
        DelegatedContext delegated = delegatedContext(context == null ? null : context.getScreen(), false);
        if (delegated == null) {
            return false;
        }
        EmiPlayerInventory inventory = delegated.delegate().getInventory(delegated.screen());
        EmiCraftContext<AbstractContainerMenu> delegatedContext = new EmiCraftContext<>(
                delegated.screen(),
                inventory,
                context.getType(),
                context.getDestination(),
                context.getAmount()
        );
        return delegated.delegate().canCraft(recipe, delegatedContext);
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<InventoryMenu> context) {
        if (recipe == null || context == null) {
            return false;
        }

        DelegatedContext delegated = delegatedContext(context.getScreen(), true);
        if (delegated == null) {
            return false;
        }

        List<ItemStack> stacks = REFLECTION.getStacks(delegated.delegate(), recipe, delegated.screen(), context.getAmount());
        if (stacks == null) {
            return false;
        }

        if (!REFLECTION.isEmiServerTransferAvailable()) {
            return REFLECTION.clientFill(delegated.delegate(), recipe, delegated.screen(), stacks, context.getDestination());
        }

        ResourceLocation recipeId = recipe.getId();
        if (recipeId == null && recipe.getBackingRecipe() != null) {
            recipeId = recipe.getBackingRecipe().id();
        }
        if (recipeId == null) {
            return false;
        }

        ResourceLocation recipeTypeId = BuiltInRegistries.RECIPE_TYPE.getKey(RecipeType.CRAFTING);
        if (recipeTypeId == null) {
            return false;
        }

        List<Integer> sourceSlots = delegated.delegate().getInputSources(delegated.menu()).stream()
                .map(slot -> slot == null ? -1 : slot.index)
                .toList();
        List<Integer> craftingSlots = delegated.delegate().getCraftingSlots(recipe, delegated.menu()).stream()
                .map(slot -> slot == null ? -1 : slot.index)
                .toList();
        Slot outputSlot = delegated.delegate().getOutputSlot(delegated.menu());
        int action = switch (context.getDestination()) {
            case NONE -> 0;
            case CURSOR -> 1;
            case INVENTORY -> 2;
        };

        CustomPacketPayload payload = REFLECTION.createTransferPayload(
                recipeId,
                recipeTypeId,
                action,
                sourceSlots,
                craftingSlots,
                outputSlot == null ? -1 : outputSlot.index,
                stacks,
                context.getAmount() > 1
        );
        if (payload == null) {
            return false;
        }

        PacketDistributor.sendToServer(payload);
        SlotDebugLog.log(
                "Requested Sophisticated backpack EMI recipe fill through SLOT: recipe={} menu={} slots={} crafting={}",
                recipeId,
                delegated.menu().getClass().getName(),
                sourceSlots.size(),
                craftingSlots.size()
        );
        return true;
    }

    private static boolean hasBackpackCraftingUpgrade(SlotDebugInventoryScreen slotScreen) {
        AbstractContainerMenu observedMenu = slotScreen.emiObservedMenu();
        if (observedMenu == null || !SophisticatedBackpackUpgradeSupport.isAvailable()) {
            return false;
        }
        return SophisticatedBackpackUpgradeSupport.findCraftingUpgrade(observedMenu) != null;
    }

    @SuppressWarnings("unchecked")
    private static DelegatedContext delegatedContext(AbstractContainerScreen<InventoryMenu> screen, boolean ensureOpen) {
        if (!(screen instanceof SlotDebugInventoryScreen slotScreen) || !REFLECTION.available()) {
            return null;
        }

        AbstractContainerMenu observedMenu = slotScreen.emiObservedMenu();
        if (observedMenu == null || !hasBackpackCraftingUpgrade(slotScreen)) {
            return null;
        }
        if (ensureOpen && !slotScreen.emiEnsureCraftingUpgradeOpen()) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Inventory playerInventory = minecraft.player == null ? null : minecraft.player.getInventory();
        if (playerInventory == null) {
            return null;
        }

        StandardRecipeHandler<AbstractContainerMenu> delegate = (StandardRecipeHandler<AbstractContainerMenu>) REFLECTION.createCraftingHandler();
        if (delegate == null) {
            return null;
        }

        return new DelegatedContext(
                observedMenu,
                delegate,
                new ObservedMenuScreen(observedMenu, playerInventory, screen.getTitle())
        );
    }

    private record DelegatedContext(
            AbstractContainerMenu menu,
            StandardRecipeHandler<AbstractContainerMenu> delegate,
            ObservedMenuScreen screen
    ) {
    }

    private static final class ObservedMenuScreen extends AbstractContainerScreen<AbstractContainerMenu> {
        private ObservedMenuScreen(AbstractContainerMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
        }

        @Override
        protected void init() {
            this.imageWidth = 176;
            this.imageHeight = 166;
            super.init();
        }

        @Override
        protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        }
    }

    private record ReflectionState(
            boolean available,
            Method craftingFactoryMethod,
            Method emiGetStacksMethod,
            Method emiClientFillMethod,
            Field emiOnServerField,
            Constructor<?> transferPayloadConstructor
    ) {
        private static ReflectionState load() {
            try {
                ClassLoader loader = SophisticatedBackpackEmiRecipeHandler.class.getClassLoader();
                Class<?> emiGridMenuInfoClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.EmiGridMenuInfo", false, loader);
                Class<?> emiRecipeFillerClass = Class.forName("dev.emi.emi.registry.EmiRecipeFiller", false, loader);
                Class<?> emiClientClass = Class.forName("dev.emi.emi.platform.EmiClient", false, loader);
                Class<?> transferPayloadClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.emi.EmiTransferRecipePayload", false, loader);
                Method getStacksMethod = null;
                Method clientFillMethod = null;
                for (Method method : emiRecipeFillerClass.getMethods()) {
                    if (method.getName().equals("getStacks") && method.getParameterCount() == 4) {
                        getStacksMethod = method;
                    } else if (method.getName().equals("clientFill") && method.getParameterCount() == 5) {
                        clientFillMethod = method;
                    }
                }
                return new ReflectionState(
                        true,
                        emiGridMenuInfoClass.getMethod("crafting"),
                        getStacksMethod,
                        clientFillMethod,
                        emiClientClass.getField("onServer"),
                        transferPayloadClass.getConstructor(
                                ResourceLocation.class,
                                ResourceLocation.class,
                                int.class,
                                List.class,
                                List.class,
                                int.class,
                                List.class,
                                boolean.class
                        )
                );
            } catch (ReflectiveOperationException ignored) {
                return new ReflectionState(false, null, null, null, null, null);
            }
        }

        private boolean ready() {
            return available
                    && craftingFactoryMethod != null
                    && emiGetStacksMethod != null
                    && emiClientFillMethod != null
                    && emiOnServerField != null
                    && transferPayloadConstructor != null;
        }

        private Object createCraftingHandler() {
            if (!ready()) {
                return null;
            }
            try {
                return craftingFactoryMethod.invoke(null);
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        private List<ItemStack> getStacks(
                StandardRecipeHandler<AbstractContainerMenu> handler,
                EmiRecipe recipe,
                ObservedMenuScreen screen,
                int amount
        ) {
            if (!ready()) {
                return null;
            }
            try {
                Object result = emiGetStacksMethod.invoke(null, handler, recipe, screen, amount);
                return result instanceof List<?> list ? (List<ItemStack>) list : null;
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        private boolean isEmiServerTransferAvailable() {
            if (!ready()) {
                return false;
            }
            try {
                return emiOnServerField.getBoolean(null);
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        private boolean clientFill(
                StandardRecipeHandler<AbstractContainerMenu> handler,
                EmiRecipe recipe,
                ObservedMenuScreen screen,
                List<ItemStack> stacks,
                EmiCraftContext.Destination destination
        ) {
            if (!ready()) {
                return false;
            }
            try {
                Object result = emiClientFillMethod.invoke(null, handler, recipe, screen, stacks, destination);
                return result instanceof Boolean value && value;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }

        private CustomPacketPayload createTransferPayload(
                ResourceLocation recipeId,
                ResourceLocation recipeTypeId,
                int action,
                List<Integer> sourceSlots,
                List<Integer> craftingSlots,
                int output,
                List<ItemStack> stacks,
                boolean maxTransfer
        ) {
            if (!ready()) {
                return null;
            }
            try {
                Object payload = transferPayloadConstructor.newInstance(
                        recipeId,
                        recipeTypeId,
                        action,
                        sourceSlots,
                        craftingSlots,
                        output,
                        stacks,
                        maxTransfer
                );
                return payload instanceof CustomPacketPayload customPacketPayload ? customPacketPayload : null;
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
    }
}
