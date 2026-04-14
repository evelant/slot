package dev.imagio.slot.neoforge.network;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.RecipeType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingMenuRefreshSupportTest {
    @Test
    void refreshesThroughMenuSlotsChangedOverride() {
        SlotsChangedMenu menu = new SlotsChangedMenu();

        CraftingMenuRefreshSupport.RefreshPlan plan = CraftingMenuRefreshSupport.resolve(menu, List.of(0));

        assertTrue(plan.supported());
        assertEquals(CraftingMenuRefreshSupport.RefreshResult.REFRESHED, plan.refresh(menu));
        assertTrue(menu.refreshed);
    }

    @Test
    void refreshesThroughMenuNoArgCraftMatrixHook() {
        NoArgCraftMatrixMenu menu = new NoArgCraftMatrixMenu();

        CraftingMenuRefreshSupport.RefreshPlan plan = CraftingMenuRefreshSupport.resolve(menu, List.of(0));

        assertTrue(plan.supported());
        assertEquals(CraftingMenuRefreshSupport.RefreshResult.REFRESHED, plan.refresh(menu));
        assertTrue(menu.refreshed);
    }

    @Test
    void refreshesThroughOpenCraftingContainerHook() {
        OpenCraftingContainerMenu menu = new OpenCraftingContainerMenu();

        CraftingMenuRefreshSupport.RefreshPlan plan = CraftingMenuRefreshSupport.resolve(menu, List.of(0));

        assertTrue(plan.supported());
        assertEquals(CraftingMenuRefreshSupport.RefreshResult.REFRESHED, plan.refresh(menu));
        assertTrue(menu.craftingContainer.refreshed);
    }

    @Test
    void resolvesInputContainerFromLogicalUpgradeSlotIds() {
        LogicalUpgradeSlotMenu menu = new LogicalUpgradeSlotMenu();

        CraftingMenuRefreshSupport.RefreshPlan plan = CraftingMenuRefreshSupport.resolve(menu, List.of(1));

        assertTrue(plan.supported());
        assertEquals(CraftingMenuRefreshSupport.RefreshResult.REFRESHED, plan.refresh(menu));
        assertSame(menu.upgradeMatrix, menu.craftingContainer.refreshedContainer);
    }

    @Test
    void reportsFailedPlanWhenOpenCraftingContainerResolutionThrows() {
        FailingResolveMenu menu = new FailingResolveMenu();

        CraftingMenuRefreshSupport.RefreshPlan plan = CraftingMenuRefreshSupport.resolve(menu, List.of(0));

        assertFalse(plan.supported());
        assertEquals(CraftingMenuRefreshSupport.RefreshPlan.State.FAILED, plan.state());
    }

    @Test
    void fallsBackToMenuHooksWhenOpenCraftingContainerResolutionThrows() {
        FailingResolveWithFallbackMenu menu = new FailingResolveWithFallbackMenu();

        CraftingMenuRefreshSupport.RefreshPlan plan = CraftingMenuRefreshSupport.resolve(menu, List.of(0));

        assertTrue(plan.supported());
        assertEquals(CraftingMenuRefreshSupport.RefreshResult.REFRESHED, plan.refresh(menu));
        assertTrue(menu.refreshed);
    }

    @Test
    void fallsBackWhenResolvedCraftingContainerDoesNotOwnInputContainer() {
        MismatchedCraftingContainerMenu menu = new MismatchedCraftingContainerMenu();

        CraftingMenuRefreshSupport.RefreshPlan plan = CraftingMenuRefreshSupport.resolve(menu, List.of(2));

        assertTrue(plan.supported());
        assertEquals(CraftingMenuRefreshSupport.RefreshResult.REFRESHED, plan.refresh(menu));
        assertTrue(menu.refreshed);
        assertSame(menu.secondMatrix, menu.refreshedContainer);
        assertFalse(menu.firstContainer.wasRefreshed());
    }

    private static class SlotsChangedMenu extends BaseMenu {
        private boolean refreshed;

        @Override
        public void slotsChanged(Container container) {
            refreshed = container == craftMatrix;
        }
    }

    private static class NoArgCraftMatrixMenu extends BaseMenu {
        private boolean refreshed;

        @SuppressWarnings("unused")
        private void onCraftMatrixChanged() {
            refreshed = true;
        }
    }

    private static class OpenCraftingContainerMenu extends BaseMenu {
        private final CraftingContainerTarget craftingContainer = new CraftingContainerTarget();

        @SuppressWarnings("unused")
        private Optional<CraftingContainerTarget> getOpenOrFirstCraftingContainer(RecipeType<?> recipeType) {
            return Optional.of(craftingContainer);
        }
    }

    private static class FailingResolveMenu extends BaseMenu {
        @SuppressWarnings("unused")
        private Optional<Object> getOpenOrFirstCraftingContainer(RecipeType<?> recipeType) {
            throw new IllegalStateException("boom");
        }
    }

    private static class FailingResolveWithFallbackMenu extends BaseMenu {
        private boolean refreshed;

        @SuppressWarnings("unused")
        private Optional<Object> getOpenOrFirstCraftingContainer(RecipeType<?> recipeType) {
            throw new IllegalStateException("boom");
        }

        @Override
        public void slotsChanged(Container container) {
            refreshed = container == craftMatrix;
        }
    }

    private static class CraftingContainerTarget {
        private boolean refreshed;
        private Container refreshedContainer;

        @SuppressWarnings("unused")
        private void onCraftMatrixChanged(Container container) {
            refreshed = container != null;
            refreshedContainer = container;
        }

        boolean wasRefreshed() {
            return refreshed;
        }
    }

    private static class RecipeSlotCraftingContainerTarget extends CraftingContainerTarget {
        private final List<Slot> recipeSlots;

        private RecipeSlotCraftingContainerTarget(List<Slot> recipeSlots) {
            this.recipeSlots = recipeSlots;
        }

        @SuppressWarnings("unused")
        private List<Slot> getRecipeSlots() {
            return recipeSlots;
        }
    }

    private static class LogicalUpgradeSlotMenu extends BaseMenu {
        private final SimpleContainer upgradeMatrix = new SimpleContainer(9);
        private final Slot logicalUpgradeSlot = new Slot(upgradeMatrix, 0, 0, 0);
        private final CraftingContainerTarget craftingContainer = new CraftingContainerTarget();

        @Override
        public Slot getSlot(int index) {
            if (index == 1) {
                return logicalUpgradeSlot;
            }
            return super.getSlot(index);
        }

        @SuppressWarnings("unused")
        private Optional<CraftingContainerTarget> getOpenOrFirstCraftingContainer(RecipeType<?> recipeType) {
            return Optional.of(craftingContainer);
        }
    }

    private static class MismatchedCraftingContainerMenu extends BaseMenu {
        private final SimpleContainer firstMatrix = new SimpleContainer(9);
        private final SimpleContainer secondMatrix = new SimpleContainer(9);
        private final Slot firstLogicalSlot = new Slot(firstMatrix, 0, 0, 0);
        private final Slot secondLogicalSlot = new Slot(secondMatrix, 0, 0, 0);
        private final RecipeSlotCraftingContainerTarget firstContainer = new RecipeSlotCraftingContainerTarget(List.of(firstLogicalSlot));
        private boolean refreshed;
        private Container refreshedContainer;

        @Override
        public Slot getSlot(int index) {
            if (index == 1) {
                return firstLogicalSlot;
            }
            if (index == 2) {
                return secondLogicalSlot;
            }
            return super.getSlot(index);
        }

        @Override
        public void slotsChanged(Container container) {
            refreshed = container == secondMatrix;
            refreshedContainer = container;
        }

        @SuppressWarnings("unused")
        private Optional<RecipeSlotCraftingContainerTarget> getOpenOrFirstCraftingContainer(RecipeType<?> recipeType) {
            return Optional.of(firstContainer);
        }
    }

    private abstract static class BaseMenu extends AbstractContainerMenu {
        protected final SimpleContainer craftMatrix = new SimpleContainer(9);

        protected BaseMenu() {
            super(null, 0);
            addSlot(new Slot(craftMatrix, 0, 0, 0));
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
