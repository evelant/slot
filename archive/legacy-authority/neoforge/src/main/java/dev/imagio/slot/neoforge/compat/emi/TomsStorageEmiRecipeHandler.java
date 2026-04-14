package dev.imagio.slot.neoforge.compat.emi;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.client.screen.container.SlotInventoryWorkspaceScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import com.google.common.collect.Lists;

public final class TomsStorageEmiRecipeHandler implements EmiRecipeHandler<AbstractContainerMenu> {
    private static final ReflectionState REFLECTION = ReflectionState.load();
    private static final TomsStorageEmiRecipeHandler INSTANCE = new TomsStorageEmiRecipeHandler();
    private static int lastWorkspaceInventoryLogMenuIdentity = Integer.MIN_VALUE;
    private static int lastWorkspaceInventoryLogStackCount = Integer.MIN_VALUE;
    private static final Map<SlotInventoryWorkspaceScreen<?>, CachedWorkspaceInventory> CACHED_WORKSPACE_INVENTORIES = new WeakHashMap<>();
    private static final Set<String> SUPPORTED_CATEGORY_IDS = Set.of(
            "minecraft:crafting",
            "emi:crafting"
    );

    private TomsStorageEmiRecipeHandler() {
    }

    static void register(EmiRegistry registry) {
        if (!REFLECTION.available()) {
            SlotDebugLog.log("Tom's EMI reflection bridge unavailable; skipping handler registration");
            return;
        }
        MenuType<?> menuType = REFLECTION.craftingTerminalMenuType();
        if (registry == null) {
            return;
        }
        if (menuType == null) {
            SlotDebugLog.log("Tom's EMI crafting terminal menu type was unavailable during registration");
            return;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        MenuType<AbstractContainerMenu> rawMenuType = (MenuType) menuType;
        @SuppressWarnings({"rawtypes", "unchecked"})
        EmiRecipeHandler<AbstractContainerMenu> handler = (EmiRecipeHandler) INSTANCE;
        registry.addRecipeHandler(rawMenuType, handler);
        preferHandler(rawMenuType, handler, true);
        SlotDebugLog.log("Registered SLOT EMI recipe handler for Tom's crafting terminal");
    }

    static void ensurePreferredHandler(AbstractContainerMenu menu) {
        if (menu == null) {
            return;
        }
        if (!REFLECTION.available()) {
            if ("com.tom.storagemod.menu.CraftingTerminalMenu".equals(menu.getClass().getName())) {
                SlotDebugLog.log("Tom's EMI reflection bridge unavailable while SLOT owns a crafting terminal menu");
            }
            return;
        }
        if (!REFLECTION.matches(menu)) {
            return;
        }

        MenuType<?> menuType = menu.getType();
        if (menuType == null) {
            return;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        MenuType<AbstractContainerMenu> rawMenuType = (MenuType) menuType;
        @SuppressWarnings({"rawtypes", "unchecked"})
        EmiRecipeHandler<AbstractContainerMenu> handler = (EmiRecipeHandler) INSTANCE;
        preferHandler(rawMenuType, handler, false);
    }

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<AbstractContainerMenu> screen) {
        List<EmiStack> inventory = new ArrayList<>();
        if (screen == null) {
            return new EmiPlayerInventory(inventory);
        }

        AbstractContainerMenu menu = screen.getMenu();
        if (menu == null) {
            return new EmiPlayerInventory(inventory);
        }

        if (screen instanceof SlotInventoryWorkspaceScreen<?> workspace && REFLECTION.matches(menu)) {
            EmiPlayerInventory workspaceInventory = workspaceInventory(workspace);
            int menuIdentity = System.identityHashCode(menu);
            if (menuIdentity != lastWorkspaceInventoryLogMenuIdentity
                    || workspaceInventory.inventory.size() != lastWorkspaceInventoryLogStackCount) {
                lastWorkspaceInventoryLogMenuIdentity = menuIdentity;
                lastWorkspaceInventoryLogStackCount = workspaceInventory.inventory.size();
                SlotDebugLog.log(
                        "Built SLOT EMI inventory from workspace aggregates: menu={} stackCount={}",
                        menu.getClass().getName(),
                        workspaceInventory.inventory.size()
                );
            }
            return workspaceInventory;
        }

        for (int slotIndex = 1; slotIndex < menu.slots.size(); slotIndex++) {
            Slot slot = menu.getSlot(slotIndex);
            if (slot == null) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (!stack.isEmpty()) {
                inventory.add(EmiStack.of(stack));
            }
        }

        for (Object storedItem : REFLECTION.storedItems(menu)) {
            ItemStack stack = REFLECTION.displayStack(storedItem);
            long quantity = REFLECTION.quantity(storedItem);
            if (!stack.isEmpty() && quantity > 0L) {
                inventory.add(EmiStack.of(stack, quantity));
            }
        }

        return new EmiPlayerInventory(inventory);
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
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<AbstractContainerMenu> context) {
        boolean craftable = context != null && context.getInventory().canCraft(recipe);
        if (context != null && context.getType() == EmiCraftContext.Type.FILL_BUTTON) {
            SlotDebugLog.log(
                    "Tom's EMI canCraft check: menu={} recipe={} type={} storedCount={} craftable={}",
                    context.getScreenHandler() == null ? "<none>" : context.getScreenHandler().getClass().getName(),
                    recipe == null ? "<null>" : recipe.getId(),
                    context.getType(),
                    context.getInventory().inventory.size(),
                    craftable
            );
        }
        return craftable;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<AbstractContainerMenu> context) {
        if (recipe == null || context == null || context.getScreen() == null) {
            return false;
        }

        ResourceLocation recipeId = recipe.getId();
        if (recipeId == null && recipe.getBackingRecipe() != null) {
            recipeId = recipe.getBackingRecipe().id();
        }
        if (recipeId == null) {
            return false;
        }

        AbstractContainerMenu menu = context.getScreen().getMenu();
        boolean sent = REFLECTION.sendFillMessage(menu, recipeId);
        if (sent) {
            SlotDebugLog.log("Requested Tom's EMI recipe fill through SLOT: recipe={} menu={}", recipeId, menu.getClass().getName());
        }
        return sent;
    }

    public static Object preferredHandlerForScreen(Object screen, Object recipe, Object currentHandler) {
        if (!(screen instanceof SlotInventoryWorkspaceScreen<?> workspace)
                || !(workspace.getMenu() instanceof AbstractContainerMenu menu)
                || !(recipe instanceof EmiRecipe emiRecipe)
                || !REFLECTION.matches(menu)
                || !INSTANCE.supportsRecipe(emiRecipe)) {
            return currentHandler;
        }

        if (currentHandler != INSTANCE) {
            SlotDebugLog.log(
                    "Overriding EMI handler for SLOT Tom's workspace: previous={} menu={}",
                    currentHandler == null ? "<none>" : currentHandler.getClass().getName(),
                    menu.getClass().getName()
            );
        }
        return INSTANCE;
    }

    private static EmiPlayerInventory workspaceInventory(SlotInventoryWorkspaceScreen<?> workspace) {
        List<SlotInventoryWorkspaceScreen.EmiAggregateStackView> stackViews = workspace.emiAggregateStacks();
        synchronized (CACHED_WORKSPACE_INVENTORIES) {
            CachedWorkspaceInventory cached = CACHED_WORKSPACE_INVENTORIES.get(workspace);
            if (cached != null && cached.stackViews() == stackViews) {
                return cached.inventory();
            }

            long buildStart = System.nanoTime();
            List<EmiStack> inventory = new ArrayList<>(stackViews.size());
            for (SlotInventoryWorkspaceScreen.EmiAggregateStackView stackView : stackViews) {
                if (!stackView.stack().isEmpty() && stackView.quantity() > 0L) {
                    inventory.add(EmiStack.of(stackView.stack(), stackView.quantity()));
                }
            }

            EmiPlayerInventory builtInventory = new EmiPlayerInventory(inventory);
            CACHED_WORKSPACE_INVENTORIES.put(workspace, new CachedWorkspaceInventory(stackViews, builtInventory));
            if (SlotDebugLog.enabled()) {
                double buildMs = Math.round(((System.nanoTime() - buildStart) / 1_000_000.0D) * 10.0D) / 10.0D;
                if (buildMs >= 3.0D) {
                    SlotDebugLog.log(
                            "Built Tom's EMI workspace inventory cache: stackCount={} buildMs={}",
                            inventory.size(),
                            buildMs
                    );
                }
            }
            return builtInventory;
        }
    }

    @SuppressWarnings("unchecked")
    private static void preferHandler(MenuType<AbstractContainerMenu> menuType, EmiRecipeHandler<AbstractContainerMenu> handler, boolean alreadyRegistered) {
        if (menuType == null || handler == null) {
            return;
        }
        try {
            Class<?> recipeFillerClass = Class.forName("dev.emi.emi.registry.EmiRecipeFiller");
            Field handlersField = recipeFillerClass.getDeclaredField("handlers");
            handlersField.setAccessible(true);
            Object handlersValue = handlersField.get(null);
            if (!(handlersValue instanceof Map<?, ?> handlers)) {
                return;
            }

            Object registeredHandlers = handlers.get(menuType);
            List<EmiRecipeHandler<AbstractContainerMenu>> typedList;
            if (registeredHandlers instanceof List<?> rawList) {
                typedList = (List<EmiRecipeHandler<AbstractContainerMenu>>) (List<?>) rawList;
            } else {
                typedList = Lists.newArrayList();
                ((Map<MenuType<?>, List<EmiRecipeHandler<?>>>) (Map<?, ?>) handlers).put(menuType, (List<EmiRecipeHandler<?>>) (List<?>) typedList);
            }

            boolean removed = typedList.remove(handler);
            if (!alreadyRegistered && !removed) {
                typedList.add(handler);
                removed = true;
            }
            if (removed) {
                typedList.add(0, handler);
                SlotDebugLog.log(
                        "Moved SLOT EMI recipe handler to the front for Tom's crafting terminal: menuType={} handlers={}",
                        menuType,
                        describeHandlers(typedList)
                );
            } else if (typedList.isEmpty()) {
                typedList.add(handler);
                SlotDebugLog.log(
                        "Registered SLOT EMI recipe handler directly on live Tom's crafting terminal menu type: menuType={} handlers={}",
                        menuType,
                        describeHandlers(typedList)
                );
            }
        } catch (ReflectiveOperationException ignored) {
            SlotDebugLog.log("Could not prioritize SLOT EMI recipe handler for Tom's crafting terminal");
        }
    }

    private static String describeHandlers(List<EmiRecipeHandler<AbstractContainerMenu>> handlers) {
        List<String> names = new ArrayList<>();
        for (EmiRecipeHandler<AbstractContainerMenu> entry : handlers) {
            names.add(entry == null ? "<null>" : entry.getClass().getName());
        }
        return names.toString();
    }

    private record CachedWorkspaceInventory(
            List<SlotInventoryWorkspaceScreen.EmiAggregateStackView> stackViews,
            EmiPlayerInventory inventory
    ) {
    }

    private record ReflectionState(
            boolean available,
            Class<?> menuClass,
            Method getStoredItemsMethod,
            Field syncField,
            Method syncGetAsListMethod,
            Method getStackMethod,
            Method getQuantityMethod,
            Method sendMessageMethod,
            Object craftingTerminalMenuObject,
            Method gameObjectGetMethod
    ) {
        private static ReflectionState load() {
            try {
                ClassLoader loader = TomsStorageEmiRecipeHandler.class.getClassLoader();
                Class<?> menuClass = Class.forName("com.tom.storagemod.menu.CraftingTerminalMenu", false, loader);
                Class<?> storageMenuClass = Class.forName("com.tom.storagemod.menu.StorageTerminalMenu", false, loader);
                Class<?> storedItemStackClass = Class.forName("com.tom.storagemod.inventory.StoredItemStack", false, loader);
                Class<?> contentClass = Class.forName("com.tom.storagemod.Content", false, loader);

                Field craftingMenuField = contentClass.getDeclaredField("craftingTerminalMenu");
                craftingMenuField.setAccessible(true);
                Object craftingMenuObject = craftingMenuField.get(null);
                Method gameObjectGetMethod = craftingMenuObject == null ? null : craftingMenuObject.getClass().getMethod("get");
                Field syncField = storageMenuClass.getField("sync");
                syncField.setAccessible(true);
                Class<?> syncClass = Class.forName("com.tom.storagemod.util.TerminalSyncManager", false, loader);

                return new ReflectionState(
                        true,
                        menuClass,
                        menuClass.getMethod("getStoredItems"),
                        syncField,
                        syncClass.getMethod("getAsList"),
                        storedItemStackClass.getMethod("getStack"),
                        storedItemStackClass.getMethod("getQuantity"),
                        menuClass.getMethod("sendMessage", CompoundTag.class),
                        craftingMenuObject,
                        gameObjectGetMethod
                );
            } catch (ReflectiveOperationException exception) {
                SlotDebugLog.log("Failed to initialize Tom's EMI reflection bridge: {}", exception.toString());
                return new ReflectionState(false, null, null, null, null, null, null, null, null, null);
            }
        }

        private MenuType<?> craftingTerminalMenuType() {
            if (!available || craftingTerminalMenuObject == null || gameObjectGetMethod == null) {
                return null;
            }
            try {
                Object value = gameObjectGetMethod.invoke(craftingTerminalMenuObject);
                return value instanceof MenuType<?> menuType ? menuType : null;
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }

        private boolean matches(AbstractContainerMenu menu) {
            return available && menu != null && menuClass.isInstance(menu);
        }

        @SuppressWarnings("unchecked")
        private List<Object> storedItems(AbstractContainerMenu menu) {
            if (!available || menu == null || !menuClass.isInstance(menu)) {
                return List.of();
            }
            try {
                Object sync = syncField == null ? null : syncField.get(menu);
                if (sync != null) {
                    Object syncedValue = syncGetAsListMethod.invoke(sync);
                    if (syncedValue instanceof List<?> syncedList && !syncedList.isEmpty()) {
                        return (List<Object>) List.copyOf(syncedList);
                    }
                }
                Object value = getStoredItemsMethod.invoke(menu);
                return value instanceof List<?> list ? (List<Object>) List.copyOf(list) : List.of();
            } catch (ReflectiveOperationException ignored) {
                return List.of();
            }
        }

        private ItemStack displayStack(Object storedItem) {
            if (!available || storedItem == null) {
                return ItemStack.EMPTY;
            }
            try {
                Object value = getStackMethod.invoke(storedItem);
                return value instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException ignored) {
                return ItemStack.EMPTY;
            }
        }

        private long quantity(Object storedItem) {
            if (!available || storedItem == null) {
                return 0L;
            }
            try {
                Object value = getQuantityMethod.invoke(storedItem);
                return value instanceof Number number ? number.longValue() : 0L;
            } catch (ReflectiveOperationException ignored) {
                return 0L;
            }
        }

        private boolean sendFillMessage(AbstractContainerMenu menu, ResourceLocation recipeId) {
            if (!available || menu == null || recipeId == null || !menuClass.isInstance(menu)) {
                return false;
            }
            try {
                CompoundTag tag = new CompoundTag();
                tag.putString("fill", recipeId.toString());
                sendMessageMethod.invoke(menu, tag);
                return true;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }
    }
}
