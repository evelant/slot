package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.atlas.lod.contributors.CarriedContributor;
import dev.imagio.slot.atlas.lod.contributors.KitMemberContributor;
import dev.imagio.slot.atlas.lod.contributors.KitMissingContributor;
import dev.imagio.slot.atlas.lod.contributors.RecentlyTouchedContributor;
import dev.imagio.slot.atlas.lod.contributors.SearchMatchContributor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtlasRelevanceTest {

    private static final ItemIdentity IRON = ItemIdentity.of("minecraft:iron_ingot");
    private static final ItemIdentity GOLD = ItemIdentity.of("minecraft:gold_ingot");
    private static final ItemIdentity PICKAXE = ItemIdentity.of("minecraft:iron_pickaxe");
    private static final ItemIdentity SHIELD = ItemIdentity.of("minecraft:shield");

    @Test
    void contextCollectsCarriedFromAtlasAndTriage() {
        SlotWorkspaceViewModel.AtlasItem ironCarried = atlasItem(IRON, "Tools", true, false);
        SlotWorkspaceViewModel.AtlasItem goldGhost = atlasItem(GOLD, "Tools", false, false);

        RelevanceContext ctx = AtlasRelevance.contextFrom(viewModel(List.of(ironCarried, goldGhost), List.of()));
        assertTrue(ctx.isCarried(IRON));
        assertFalse(ctx.isCarried(GOLD));
    }

    @Test
    void contextCollectsRecentItems() {
        SlotWorkspaceViewModel.AtlasItem ironRecent = atlasItem(IRON, "Tools", false, true);
        SlotWorkspaceViewModel.AtlasItem goldStable = atlasItem(GOLD, "Tools", false, false);

        RelevanceContext ctx = AtlasRelevance.contextFrom(viewModel(List.of(ironRecent, goldStable), List.of()));
        assertTrue(ctx.isRecent(IRON));
        assertFalse(ctx.isRecent(GOLD));
    }

    @Test
    void contextCollectsActiveKitMembersFromPagesAndBring() {
        SlotWorkspaceViewModel.KitCard activeKit = activeKitWithSlotsAndBring(
                List.of(PICKAXE),
                List.of(SHIELD)
        );
        SlotWorkspaceViewModel vm = viewModelWithKits(List.of(activeKit));

        RelevanceContext ctx = AtlasRelevance.contextFrom(vm);
        assertTrue(ctx.isActiveKitMember(PICKAXE), "page slot identity is a member");
        assertTrue(ctx.isActiveKitMember(SHIELD), "bring identity is a member");
        assertFalse(ctx.isActiveKitMember(IRON));
    }

    @Test
    void contextDerivesKitMissingFromBringMinusCarried() {
        SlotWorkspaceViewModel.KitCard activeKit = activeKitWithSlotsAndBring(
                List.of(),
                List.of(PICKAXE, SHIELD)
        );
        // Player carries the pickaxe, but not the shield.
        SlotWorkspaceViewModel.AtlasItem carriedPick = atlasItem(PICKAXE, "Tools", true, false);
        SlotWorkspaceViewModel vm = new SlotWorkspaceViewModel(
                1L, "ready", "", 0, 0, 2200, 1480, 0, 0,
                List.of(), List.of(carriedPick), List.of(), List.of(), List.of(), null,
                List.of(activeKit)
        );

        RelevanceContext ctx = AtlasRelevance.contextFrom(vm);
        assertFalse(ctx.isActiveKitMissing(PICKAXE), "carried bring item is not missing");
        assertTrue(ctx.isActiveKitMissing(SHIELD), "absent bring item is missing");
    }

    @Test
    void contextSkipsKitMembersWhenNoneActive() {
        SlotWorkspaceViewModel.KitCard inactive = inactiveKitWithBring(List.of(PICKAXE));
        SlotWorkspaceViewModel vm = viewModelWithKits(List.of(inactive));

        RelevanceContext ctx = AtlasRelevance.contextFrom(vm);
        assertFalse(ctx.isActiveKitMember(PICKAXE));
        assertFalse(ctx.isActiveKitMissing(PICKAXE));
    }

    @Test
    void contextPopulatesSearchMatchesFromQuery() {
        SlotWorkspaceViewModel.AtlasItem iron = atlasItem(IRON, "Tools", false, false);
        SlotWorkspaceViewModel.AtlasItem gold = atlasItem(GOLD, "Tools", false, false);

        RelevanceContext ctx = AtlasRelevance.contextFrom(viewModel(List.of(iron, gold), List.of()), "iron");
        assertTrue(ctx.matchesActiveSearch(IRON));
        assertFalse(ctx.matchesActiveSearch(GOLD));
    }

    @Test
    void contextEmptyQueryProducesNoMatches() {
        SlotWorkspaceViewModel.AtlasItem iron = atlasItem(IRON, "Tools", false, false);

        RelevanceContext ctx = AtlasRelevance.contextFrom(viewModel(List.of(iron), List.of()), "");
        assertFalse(ctx.matchesActiveSearch(IRON));
    }

    @Test
    void scoresForCombinesContributorsViaMax() {
        // Iron is carried AND in active kit's bring. Max should pick kit_member (0.85)
        // over carried (0.9)? — actually carried is higher. Let's verify: max(0.9, 0.85) = 0.9.
        SlotWorkspaceViewModel.KitCard kit = activeKitWithSlotsAndBring(List.of(), List.of(IRON));
        SlotWorkspaceViewModel.AtlasItem ironCarried = atlasItem(IRON, "Tools", true, false);
        SlotWorkspaceViewModel vm = new SlotWorkspaceViewModel(
                1L, "ready", "", 0, 0, 2200, 1480, 0, 0,
                List.of(), List.of(ironCarried), List.of(), List.of(), List.of(), null,
                List.of(kit)
        );

        Map<SlotWorkspaceViewModel.IdentityRef, RelevanceScore> scores = AtlasRelevance.scoresFor(
                vm,
                AtlasRelevance.DEFAULT_CONTRIBUTORS
        );
        RelevanceScore ironScore = scores.get(SlotWorkspaceViewModel.IdentityRef.from(IRON));
        assertNotNull(ironScore);
        assertEquals(0.9f, ironScore.value(), 1e-6);
        assertEquals(0.9f, ironScore.contributions().get(CarriedContributor.NAME), 1e-6);
        assertEquals(0.85f, ironScore.contributions().get(KitMemberContributor.NAME), 1e-6);
    }

    @Test
    void scoresForSearchMatchOutranksCarried() {
        SlotWorkspaceViewModel.AtlasItem ironCarried = atlasItem(IRON, "Tools", true, false);
        RelevanceContext ctx = AtlasRelevance.contextFrom(
                viewModel(List.of(ironCarried), List.of()),
                "iron"
        );
        Map<SlotWorkspaceViewModel.IdentityRef, RelevanceScore> scores = AtlasRelevance.scoresFor(
                viewModel(List.of(ironCarried), List.of()),
                ctx,
                AtlasRelevance.DEFAULT_CONTRIBUTORS
        );
        RelevanceScore ironScore = scores.get(SlotWorkspaceViewModel.IdentityRef.from(IRON));
        assertNotNull(ironScore);
        assertEquals(0.95f, ironScore.value(), 1e-6);
        assertEquals(0.95f, ironScore.contributions().get(SearchMatchContributor.NAME), 1e-6);
        assertEquals(0.9f, ironScore.contributions().get(CarriedContributor.NAME), 1e-6);
    }

    @Test
    void nullViewModelYieldsEmpty() {
        assertEquals(RelevanceContext.empty(), AtlasRelevance.contextFrom(null));
        assertTrue(AtlasRelevance.scoresFor(null, AtlasRelevance.DEFAULT_CONTRIBUTORS).isEmpty());
    }

    @Test
    void recentContributorIsInDefaultChain() {
        // Sanity: ensure the contributor list actually wires recently_touched.
        boolean found = AtlasRelevance.DEFAULT_CONTRIBUTORS.stream()
                .anyMatch(c -> c instanceof RecentlyTouchedContributor);
        assertTrue(found);
        boolean foundMissing = AtlasRelevance.DEFAULT_CONTRIBUTORS.stream()
                .anyMatch(c -> c instanceof KitMissingContributor);
        assertTrue(foundMissing);
    }

    private static SlotWorkspaceViewModel.AtlasItem atlasItem(
            ItemIdentity identity, String islandId, boolean carried, boolean recent) {
        return new SlotWorkspaceViewModel.AtlasItem(
                SlotWorkspaceViewModel.IdentityRef.from(identity),
                ItemStack.EMPTY,
                identity.itemId(),
                1,
                0,
                islandId,
                recent,
                false,
                carried,
                List.of()
        );
    }

    private static SlotWorkspaceViewModel.KitCard activeKitWithSlotsAndBring(
            List<ItemIdentity> pageSlotIdentities,
            List<ItemIdentity> bringIdentities
    ) {
        return kitCard(true, pageSlotIdentities, bringIdentities);
    }

    private static SlotWorkspaceViewModel.KitCard inactiveKitWithBring(List<ItemIdentity> bringIdentities) {
        return kitCard(false, List.of(), bringIdentities);
    }

    private static SlotWorkspaceViewModel.KitCard kitCard(
            boolean active,
            List<ItemIdentity> pageSlotIdentities,
            List<ItemIdentity> bringIdentities
    ) {
        List<SlotWorkspaceViewModel.KitSlotState> slots = new java.util.ArrayList<>();
        int idx = 0;
        for (ItemIdentity id : pageSlotIdentities) {
            slots.add(new SlotWorkspaceViewModel.KitSlotState(
                    idx++, true, false, SlotWorkspaceViewModel.IdentityRef.from(id), ItemStack.EMPTY, id.itemId()
            ));
        }
        SlotWorkspaceViewModel.KitPageView page = new SlotWorkspaceViewModel.KitPageView(
                0, slots.size(), 0, slots
        );
        List<SlotWorkspaceViewModel.KitBringItem> bring = new java.util.ArrayList<>();
        for (ItemIdentity id : bringIdentities) {
            bring.add(new SlotWorkspaceViewModel.KitBringItem(
                    SlotWorkspaceViewModel.IdentityRef.from(id), false, ItemStack.EMPTY, id.itemId()
            ));
        }
        return new SlotWorkspaceViewModel.KitCard(
                "test_kit",
                "Test Kit",
                1,
                0,
                active,
                slots.size(),
                0,
                0,
                0,
                bring.size(),
                0,
                slots,
                List.of(page),
                bring
        );
    }

    private static SlotWorkspaceViewModel viewModel(
            List<SlotWorkspaceViewModel.AtlasItem> atlasItems,
            List<SlotWorkspaceViewModel.AtlasItem> triageItems
    ) {
        return new SlotWorkspaceViewModel(
                1L, "ready", "", 0, 0, 2200, 1480, 0, 0,
                List.of(), atlasItems, triageItems, List.of(), List.of(), null,
                List.of()
        );
    }

    private static SlotWorkspaceViewModel viewModelWithKits(List<SlotWorkspaceViewModel.KitCard> kits) {
        return new SlotWorkspaceViewModel(
                1L, "ready", "", 0, 0, 2200, 1480, 0, 0,
                List.of(), List.of(), List.of(), List.of(), List.of(), null,
                kits
        );
    }
}
