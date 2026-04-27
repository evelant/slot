package dev.imagio.slot.atlas.lod.contributors;

import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaProximityContributorTest {
    private static final ItemIdentity IRON = ItemIdentity.of("minecraft:iron_ingot");
    private static final ItemIdentity GOLD = ItemIdentity.of("minecraft:gold_ingot");

    @Test
    void scoresHighWhenIdentityIsAreaProximityBoosted() {
        RelevanceContext ctx = RelevanceContext.builder()
                .areaProximityBoostedIdentities(Set.of(IRON))
                .build();

        AreaProximityContributor contributor = new AreaProximityContributor();

        assertTrue(contributor.score(IRON, ctx) > 0f);
        assertEquals(0f, contributor.score(GOLD, ctx));
    }

    @Test
    void scoresZeroWhenContextIsEmpty() {
        AreaProximityContributor contributor = new AreaProximityContributor();
        assertEquals(0f, contributor.score(IRON, RelevanceContext.empty()));
        assertEquals(0f, contributor.score(IRON, null));
    }
}
