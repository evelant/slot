package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.ui.spi.SlotUiElement;
import dev.imagio.slot.ui.spi.SlotUiEvent;
import dev.imagio.slot.ui.spi.SlotUiEventKind;
import dev.imagio.slot.workflow.domain.CraftRunAlternative;
import dev.imagio.slot.workflow.domain.CraftRunIngredientGroup;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import dev.imagio.slot.workflow.domain.CraftRunRecipeEntry;
import dev.imagio.slot.workflow.domain.CraftRunState;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftRunUiBuilderTest {
    @Test
    void headerIconTitleAndStageOpenOutputRecipe() {
        TestContext context = new TestContext(state(entry(12)));
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));
        try {
            SlotUiElement section = new CraftRunUiBuilder(context).entrySections(List.of()).get(0);
            SlotUiElement header = section.children().get(0);

            header.children().get(0).dispatch(click());
            header.children().get(1).dispatch(click());
            header.children().get(2).dispatch(click());

            assertEquals(List.of(
                    ItemIdentity.of("minecraft:torch"),
                    ItemIdentity.of("minecraft:torch"),
                    ItemIdentity.of("minecraft:torch")
            ), context.openedRecipes);
            assertEquals(List.of("craft-run-test"), context.stagedEntries);
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
    }

    @Test
    void entryHeaderStartsWithRemainingRunCountAndExplainsOutputInTooltip() {
        TestContext context = new TestContext(state(entry(12)));
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));
        try {
            SlotUiElement section = new CraftRunUiBuilder(context).entrySections(List.of()).get(0);
            SlotUiElement title = section.children().get(0).children().get(1);

            assertEquals("x3 Torch", title.text());
            assertTrue(tooltipText(title).contains("Runs: 3"));
            assertTrue(tooltipText(title).contains("Each run: 4"));
            assertTrue(tooltipText(title).contains("Total output: 12"));
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
    }

    @Test
    void fluidEntryHeaderShowsRunsAndMillibucketTotalsInTooltip() {
        TestContext context = new TestContext(state(fluidEntry(4000L)));
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));
        try {
            SlotUiElement section = new CraftRunUiBuilder(context).entrySections(List.of()).get(0);
            SlotUiElement title = section.children().get(0).children().get(0);

            assertEquals("x4 Oxygen", title.text());
            assertTrue(tooltipText(title).contains("Runs: 4"));
            assertTrue(tooltipText(title).contains("Each run: 1 B"));
            assertTrue(tooltipText(title).contains("Total output: 4 B"));
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
    }

    @Test
    void visibleRecipeActionStartsWithRemainingOutputCount() {
        TestContext context = new TestContext(
                CraftRunState.empty(),
                List.of(capture("Visible Torch Recipe", 12)));

        SlotUiElement row = new CraftRunUiBuilder(context).visibleRecipeActions().get(0);

        SlotUiElement label = row.children().get(0);
        assertTrue(label.text().startsWith("x3 Visible Torch Recipe"));
        assertTrue(tooltipText(label).contains("Total output: 12"));
    }

    @Test
    void completedEntryStaysVisibleWithDoneHeaderStyle() {
        TestContext context = new TestContext(state(entry(0)));
        SlotWorkspaceViewModel.setGhostStackResolver(itemId -> new ItemStack(itemId, 1, 64));
        try {
            List<SlotUiElement> sections = new CraftRunUiBuilder(context).entrySections(List.of());

            assertEquals(1, sections.size());
            SlotUiElement title = sections.get(0).children().get(0).children().get(1);
            assertEquals("Torch done", title.text());
            assertEquals(WorkspaceUiPalette.ACCENT, title.textStyle().color());
            assertTrue(context.state.active());
        } finally {
            SlotWorkspaceViewModel.setGhostStackResolver(null);
        }
    }

    private static SlotUiEvent click() {
        return new SlotUiEvent(SlotUiEventKind.CLICK, 0, 0, 0, false);
    }

    private static List<String> tooltipText(SlotUiElement element) {
        return element.tooltipLines().stream().map(line -> line.getString()).toList();
    }

    private static CraftRunState state(CraftRunRecipeEntry entry) {
        return new CraftRunState(1, entry.entryId(), List.of(entry));
    }

    private static CraftRunRecipeEntry entry(int remainingOutputCount) {
        return new CraftRunRecipeEntry(
                "craft-run-test",
                1,
                "emi:slot/torch",
                "slot:torch",
                "Torch",
                ItemIdentity.of("minecraft:torch"),
                "Torch",
                4,
                remainingOutputCount,
                List.of(new CraftRunIngredientGroup(
                        "coal",
                        "Coal",
                        1,
                        List.of(new CraftRunAlternative(ItemIdentity.of("minecraft:coal"), "Coal")),
                        List.of())),
                List.of());
    }

    private static CraftRunRecipeEntry fluidEntry(long remainingOutputAmount) {
        SlotResourceIdentity oxygen = SlotResourceIdentity.fluid("gtceu:oxygen");
        return new CraftRunRecipeEntry(
                "craft-run-fluid-test",
                1,
                "emi:slot/oxygen",
                "slot:oxygen",
                "Oxygen",
                null,
                "Oxygen",
                1000,
                remainingOutputAmount >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remainingOutputAmount,
                List.of(new CraftRunIngredientGroup(
                        "dust",
                        "Dust",
                        1,
                        List.of(new CraftRunAlternative(ItemIdentity.of("gtceu:dust"), "Dust")),
                        List.of())),
                oxygen,
                1000L,
                remainingOutputAmount,
                List.of());
    }

    private static CraftRunRecipeCapture capture(String label, int remainingOutputCount) {
        return new CraftRunRecipeCapture(
                "emi:test/" + remainingOutputCount,
                "emi:slot/test",
                label,
                ItemIdentity.of("minecraft:torch"),
                label,
                4,
                remainingOutputCount,
                List.of(new CraftRunIngredientGroup(
                        "coal",
                        "Coal",
                        1,
                        List.of(new CraftRunAlternative(ItemIdentity.of("minecraft:coal"), "Coal")),
                        List.of())),
                List.of());
    }

    private static final class TestContext implements CraftRunUiBuilder.Context {
        private final CraftRunState state;
        private final List<CraftRunRecipeCapture> visibleRecipes;
        private final ArrayList<String> stagedEntries = new ArrayList<>();
        private final ArrayList<ItemIdentity> openedRecipes = new ArrayList<>();

        private TestContext(CraftRunState state) {
            this(state, List.of());
        }

        private TestContext(CraftRunState state, List<CraftRunRecipeCapture> visibleRecipes) {
            this.state = state;
            this.visibleRecipes = visibleRecipes == null ? List.of() : List.copyOf(visibleRecipes);
        }

        @Override
        public CraftRunState craftRun() {
            return state;
        }

        @Override
        public List<CraftRunRecipeCapture> visibleRecipes() {
            return visibleRecipes;
        }

        @Override
        public void addVisibleRecipe(CraftRunRecipeCapture capture) {
        }

        @Override
        public void stageEntry(String entryId) {
            stagedEntries.add(entryId);
        }

        @Override
        public void openRecipe(CraftRunRecipeEntry entry) {
            openedRecipes.add(entry == null ? null : entry.outputIdentity());
        }

        @Override
        public void adjustEntry(String entryId, int delta) {
        }

        @Override
        public void removeEntry(String entryId) {
        }
    }
}
