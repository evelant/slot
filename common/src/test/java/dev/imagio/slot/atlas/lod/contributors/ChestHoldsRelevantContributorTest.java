package dev.imagio.slot.atlas.lod.contributors;

import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChestHoldsRelevantContributorTest {
    private static final ItemIdentity IRON = ItemIdentity.of("minecraft:iron_ingot");
    private static final ItemIdentity GOLD = ItemIdentity.of("minecraft:gold_ingot");

    @Test
    void scoresIdentitiesPresentInRelevantStorages() {
        RelevanceContext ctx = RelevanceContext.builder()
                .chestHoldsRelevantIdentities(Set.of(IRON))
                .build();

        ChestHoldsRelevantContributor contributor = new ChestHoldsRelevantContributor();

        assertTrue(contributor.score(IRON, ctx) > 0f);
        assertEquals(0f, contributor.score(GOLD, ctx));
    }

    @Test
    void areaProximityScoreOutweighsChestHoldsScore() {
        RelevanceContext both = RelevanceContext.builder()
                .areaProximityBoostedIdentities(Set.of(IRON))
                .chestHoldsRelevantIdentities(Set.of(IRON))
                .build();

        AreaProximityContributor proximity = new AreaProximityContributor();
        ChestHoldsRelevantContributor holds = new ChestHoldsRelevantContributor();

        // Per the plan: area proximity = "high"; chest holds = "medium-high".
        assertTrue(proximity.score(IRON, both) > holds.score(IRON, both));
    }

    @Test
    void contextHelpersExposeAreaAndStorageMembership() {
        UUID area = UUID.randomUUID();
        UUID storage = UUID.randomUUID();
        RelevanceContext ctx = RelevanceContext.builder()
                .proximateAreaIds(Set.of(area))
                .relevantStorageIds(Set.of(storage))
                .build();

        assertTrue(ctx.isProximateArea(area));
        assertFalse(ctx.isProximateArea(UUID.randomUUID()));
        assertTrue(ctx.isRelevantStorage(storage));
        assertFalse(ctx.isRelevantStorage(UUID.randomUUID()));
    }
}
