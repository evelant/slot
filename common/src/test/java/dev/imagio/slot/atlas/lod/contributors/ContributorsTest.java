package dev.imagio.slot.atlas.lod.contributors;

import dev.imagio.slot.atlas.lod.RelevanceContext;
import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContributorsTest {

    private static final ItemIdentity IRON = ItemIdentity.of("minecraft:iron_ingot");
    private static final ItemIdentity GOLD = ItemIdentity.of("minecraft:gold_ingot");

    @Test
    void searchMatch() {
        SearchMatchContributor c = new SearchMatchContributor();
        RelevanceContext ctx = RelevanceContext.builder()
                .searchMatchedIdentities(Set.of(IRON))
                .build();
        assertEquals(0.95f, c.score(IRON, ctx), 1e-6);
        assertEquals(0f, c.score(GOLD, ctx), 1e-6);
        assertEquals(0f, c.score(IRON, RelevanceContext.empty()), 1e-6);
        assertEquals(0f, c.score(IRON, null), 1e-6);
    }

    @Test
    void recentlyTouched() {
        RecentlyTouchedContributor c = new RecentlyTouchedContributor();
        RelevanceContext ctx = RelevanceContext.builder()
                .recentIdentities(Set.of(IRON))
                .build();
        assertEquals(0.6f, c.score(IRON, ctx), 1e-6);
        assertEquals(0f, c.score(GOLD, ctx), 1e-6);
    }

    @Test
    void kitMember() {
        KitMemberContributor c = new KitMemberContributor();
        RelevanceContext ctx = RelevanceContext.builder()
                .activeKitMembers(Set.of(IRON))
                .build();
        assertEquals(0.85f, c.score(IRON, ctx), 1e-6);
        assertEquals(0f, c.score(GOLD, ctx), 1e-6);
    }

    @Test
    void kitMissing() {
        KitMissingContributor c = new KitMissingContributor();
        RelevanceContext ctx = RelevanceContext.builder()
                .activeKitMissing(Set.of(GOLD))
                .build();
        assertEquals(0.85f, c.score(GOLD, ctx), 1e-6);
        assertEquals(0f, c.score(IRON, ctx), 1e-6);
    }

    @Test
    void contributorNamesAreStable() {
        assertEquals("search_match", new SearchMatchContributor().name());
        assertEquals("recently_touched", new RecentlyTouchedContributor().name());
        assertEquals("kit_member", new KitMemberContributor().name());
        assertEquals("kit_missing", new KitMissingContributor().name());
        assertEquals("carried", new CarriedContributor().name());
    }
}
