package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelevanceContextTest {

    private static final ItemIdentity IRON = ItemIdentity.of("minecraft:iron_ingot");
    private static final ItemIdentity GOLD = ItemIdentity.of("minecraft:gold_ingot");

    @Test
    void emptyHasNoMembership() {
        RelevanceContext ctx = RelevanceContext.empty();
        assertFalse(ctx.isCarried(IRON));
        assertFalse(ctx.isRecent(IRON));
        assertFalse(ctx.isActiveKitMember(IRON));
        assertFalse(ctx.isActiveKitMissing(IRON));
        assertFalse(ctx.matchesActiveSearch(IRON));
    }

    @Test
    void emptyIsCachedSingleton() {
        assertSame(RelevanceContext.empty(), RelevanceContext.empty());
    }

    @Test
    void builderPopulatesEachField() {
        RelevanceContext ctx = RelevanceContext.builder()
                .carriedIdentities(Set.of(IRON))
                .recentIdentities(Set.of(GOLD))
                .activeKitMembers(Set.of(IRON))
                .activeKitMissing(Set.of(GOLD))
                .searchMatchedIdentities(Set.of(IRON))
                .build();
        assertTrue(ctx.isCarried(IRON));
        assertFalse(ctx.isCarried(GOLD));
        assertTrue(ctx.isRecent(GOLD));
        assertTrue(ctx.isActiveKitMember(IRON));
        assertTrue(ctx.isActiveKitMissing(GOLD));
        assertTrue(ctx.matchesActiveSearch(IRON));
    }

    @Test
    void nullSetsBecomeEmpty() {
        RelevanceContext ctx = RelevanceContext.builder()
                .carriedIdentities(null)
                .recentIdentities(null)
                .build();
        assertFalse(ctx.isCarried(IRON));
        assertFalse(ctx.isRecent(GOLD));
    }

    @Test
    void nullIdentityNeverMatches() {
        RelevanceContext ctx = RelevanceContext.builder()
                .carriedIdentities(Set.of(IRON))
                .recentIdentities(Set.of(IRON))
                .activeKitMembers(Set.of(IRON))
                .activeKitMissing(Set.of(IRON))
                .searchMatchedIdentities(Set.of(IRON))
                .build();
        assertFalse(ctx.isCarried(null));
        assertFalse(ctx.isRecent(null));
        assertFalse(ctx.isActiveKitMember(null));
        assertFalse(ctx.isActiveKitMissing(null));
        assertFalse(ctx.matchesActiveSearch(null));
    }

    @Test
    void ofCarriedIsAShortcut() {
        RelevanceContext ctx = RelevanceContext.ofCarried(Set.of(IRON));
        assertTrue(ctx.isCarried(IRON));
        assertFalse(ctx.isRecent(IRON));
    }
}
