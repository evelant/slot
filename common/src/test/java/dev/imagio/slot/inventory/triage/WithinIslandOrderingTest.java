package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Direct coverage of the extracted comparator. Confirms the cluster
 * key zones (dye → palette → plain), the canonical Minecraft dye
 * order, the flavor / origin sub-ranks, and the carry-rank
 * domination. Whatever {@code RealisticAtlasGenerator} ships in its
 * populate output, the live chip-accept placement path produces the
 * same ordering by sharing this comparator.
 */
class WithinIslandOrderingTest {

    @Test
    void carryRankDominatesAllOtherKeys() {
        // An everyday + ominous + creative_only item still leads a
        // rare + plain + overworld item because carry-rank dominates.
        WithinIslandOrdering.DescribedStack everyday = describe(
                "modded:trinket", null, null, "everyday", null,
                null, null, "ominous", null, null);
        WithinIslandOrdering.DescribedStack rare = describe(
                "modded:plank", "building_block", null, "rare", null,
                "overworld_surface", null, "plain", null, null);
        assertEquals(-1, sign(WithinIslandOrdering.WITHIN_ISLAND_COMPARATOR.compare(everyday, rare)));
    }

    @Test
    void dyedItemsClusterAndSortByCanonicalColorOrder() {
        // White, gray, black wools — alphabetical would put black
        // first; canonical Minecraft order puts white → gray → black.
        List<WithinIslandOrdering.DescribedStack> stacks = new ArrayList<>(List.of(
                described("modded:black_wool", "decorative_block", "black"),
                described("modded:gray_wool", "decorative_block", "gray"),
                described("modded:white_wool", "decorative_block", "white")
        ));
        stacks.sort(WithinIslandOrdering.WITHIN_ISLAND_COMPARATOR);
        assertEquals(List.of("modded:white_wool", "modded:gray_wool", "modded:black_wool"),
                idsOf(stacks));
    }

    @Test
    void palettedItemsClusterByPrimaryToneThenFlavorThenOrigin() {
        // Two palette tones × two flavors × two origins (plus a
        // tiebreak by id). Expected ordering:
        //   wood_medium-plain-early < wood_medium-plain-late
        //     < wood_medium-fancy-early
        //   < wood_red-plain-early < wood_red-fancy-early
        WithinIslandOrdering.DescribedStack wmPlainEarly = describe(
                "modded:oak_plain_early", "building_block", null, null, null,
                "overworld_surface", null, "plain", null, "wood_medium");
        WithinIslandOrdering.DescribedStack wmPlainLate = describe(
                "modded:oak_plain_late", "building_block", null, null, null,
                "nether", null, "plain", null, "wood_medium");
        WithinIslandOrdering.DescribedStack wmFancyEarly = describe(
                "modded:oak_fancy_early", "building_block", null, null, null,
                "overworld_surface", null, "fancy", null, "wood_medium");
        WithinIslandOrdering.DescribedStack wrPlainEarly = describe(
                "modded:acacia_plain_early", "building_block", null, null, null,
                "overworld_surface", null, "plain", null, "wood_red");
        WithinIslandOrdering.DescribedStack wrFancyEarly = describe(
                "modded:acacia_fancy_early", "building_block", null, null, null,
                "overworld_surface", null, "fancy", null, "wood_red");
        List<WithinIslandOrdering.DescribedStack> stacks = new ArrayList<>(List.of(
                wrFancyEarly, wmPlainLate, wmFancyEarly, wrPlainEarly, wmPlainEarly));
        stacks.sort(WithinIslandOrdering.WITHIN_ISLAND_COMPARATOR);
        assertEquals(List.of(
                "modded:oak_plain_early",
                "modded:oak_plain_late",
                "modded:oak_fancy_early",
                "modded:acacia_plain_early",
                "modded:acacia_fancy_early"
        ), idsOf(stacks));
    }

    @Test
    void plainZoneComesAfterDyeAndPaletteZones() {
        WithinIslandOrdering.DescribedStack dyed = described("modded:white_wool", "decorative_block", "white");
        WithinIslandOrdering.DescribedStack paletted = describe(
                "modded:oak_plank", "building_block", null, null, null,
                null, null, null, null, "wood_medium");
        WithinIslandOrdering.DescribedStack plain = describe(
                "modded:plain_brick", "building_block", null, null, null,
                null, null, null, null, null);
        List<WithinIslandOrdering.DescribedStack> stacks = new ArrayList<>(List.of(plain, paletted, dyed));
        stacks.sort(WithinIslandOrdering.WITHIN_ISLAND_COMPARATOR);
        assertEquals(List.of(
                "modded:white_wool",
                "modded:oak_plank",
                "modded:plain_brick"
        ), idsOf(stacks));
    }

    @Test
    void clusterKeyExposesZoneStructureForCallersThatNeedIt() {
        IslandSignalDescriptor dyed = descriptor("modded:white_wool",
                "decorative_block", null, null, null, null, null, "white", null);
        IslandSignalDescriptor paletted = descriptor("modded:oak_plank",
                "building_block", null, null, null, null, null, null, "wood_medium");
        IslandSignalDescriptor plain = descriptor("modded:plain_brick",
                "building_block", null, null, null, null, null, null, null);
        // Sanity: every cluster key starts with its zone prefix so
        // callers can detect the zone without re-inspecting facets.
        org.junit.jupiter.api.Assertions.assertTrue(
                WithinIslandOrdering.clusterKey("modded:white_wool", dyed).startsWith("1dye:"));
        org.junit.jupiter.api.Assertions.assertTrue(
                WithinIslandOrdering.clusterKey("modded:oak_plank", paletted).startsWith("2pal:"));
        org.junit.jupiter.api.Assertions.assertTrue(
                WithinIslandOrdering.clusterKey("modded:plain_brick", plain).startsWith("3pln:"));
    }

    // --- helpers ------------------------------------------------------

    private static int sign(int value) {
        return Integer.compare(value, 0);
    }

    private static List<String> idsOf(List<WithinIslandOrdering.DescribedStack> stacks) {
        ArrayList<String> ids = new ArrayList<>(stacks.size());
        for (WithinIslandOrdering.DescribedStack s : stacks) {
            ids.add(s.stack().itemId());
        }
        return ids;
    }

    private static WithinIslandOrdering.DescribedStack described(String itemId, String role, String dyeColor) {
        return describe(itemId, role, null, null, null, null, null, null, dyeColor, null);
    }

    private static WithinIslandOrdering.DescribedStack describe(
            String itemId,
            String role,
            String materialFamily,
            String carryFrequency,
            String rarity,
            String origin,
            String activity,
            String flavor,
            String dyeColor,
            String paletteValue
    ) {
        IslandSignalDescriptor descriptor = descriptor(
                itemId, role, materialFamily, carryFrequency, rarity,
                origin, activity, dyeColor, paletteValue);
        // Note: flavor sneaks in via the descriptor builder below.
        descriptor = withFlavor(descriptor, flavor);
        ItemStack stack = new ItemStack(itemId, 1, 64);
        return new WithinIslandOrdering.DescribedStack(stack, descriptor);
    }

    private static IslandSignalDescriptor descriptor(
            String itemId,
            String role,
            String materialFamily,
            String carryFrequency,
            String rarity,
            String origin,
            String activity,
            String dyeColor,
            String paletteValue
    ) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of(),
                namespaceOf(itemId),
                "",
                role,
                null,
                materialFamily,
                List.of(),
                activity == null ? List.of() : List.of(activity),
                null,
                carryFrequency,
                rarity,
                origin,
                dyeColor,
                paletteValue == null ? List.of() : List.of(paletteValue),
                null,
                false
        );
    }

    private static IslandSignalDescriptor withFlavor(IslandSignalDescriptor base, String flavor) {
        return new IslandSignalDescriptor(
                base.identity(),
                base.classSignals(),
                base.itemTags(),
                base.namespace(),
                base.creativeTabId(),
                base.role(),
                base.roleAlternatives(),
                base.materialFamily(),
                base.subsystems(),
                base.activities(),
                flavor,
                base.carryFrequency(),
                base.rarity(),
                base.origin(),
                base.dyeColor(),
                base.palette(),
                base.form(),
                base.emitsLight()
        );
    }

    private static String namespaceOf(String itemId) {
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }
}
