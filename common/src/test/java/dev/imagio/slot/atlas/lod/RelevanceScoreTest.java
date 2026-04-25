package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.atlas.lod.contributors.CarriedContributor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelevanceScoreTest {

    private static final ItemIdentity IRON = ItemIdentity.of("minecraft:iron_ingot");
    private static final ItemIdentity GOLD = ItemIdentity.of("minecraft:gold_ingot");

    @Test
    void emptyContributorsReturnsBaseline() {
        RelevanceScore score = RelevanceScore.compute(IRON, RelevanceContext.empty(), List.of());
        assertSame(RelevanceScore.BASELINE, score);
    }

    @Test
    void nullIdentityReturnsBaseline() {
        RelevanceScore score = RelevanceScore.compute(null, RelevanceContext.empty(), List.of(new CarriedContributor()));
        assertSame(RelevanceScore.BASELINE, score);
    }

    @Test
    void carriedContributorScoresHighWhenCarried() {
        RelevanceContext ctx = RelevanceContext.ofCarried(Set.of(IRON));
        RelevanceScore score = RelevanceScore.compute(IRON, ctx, List.of(new CarriedContributor()));
        assertEquals(0.9f, score.value(), 1e-6);
        assertEquals(0.9f, score.contributions().get(CarriedContributor.NAME), 1e-6);
    }

    @Test
    void carriedContributorScoresZeroForGhost() {
        RelevanceContext ctx = RelevanceContext.ofCarried(Set.of(IRON));
        RelevanceScore score = RelevanceScore.compute(GOLD, ctx, List.of(new CarriedContributor()));
        assertEquals(0f, score.value(), 1e-6);
    }

    @Test
    void multipleContributorsCombineViaMax() {
        RelevanceContributor low = constContributor("low", 0.3f);
        RelevanceContributor medium = constContributor("medium", 0.6f);
        RelevanceContributor high = constContributor("high", 0.85f);
        RelevanceScore score = RelevanceScore.compute(IRON, RelevanceContext.empty(), List.of(low, medium, high));
        assertEquals(0.85f, score.value(), 1e-6);
        assertEquals(0.3f, score.contributions().get("low"), 1e-6);
        assertEquals(0.6f, score.contributions().get("medium"), 1e-6);
        assertEquals(0.85f, score.contributions().get("high"), 1e-6);
    }

    @Test
    void contributorReturningNanIsTreatedAsZero() {
        RelevanceContributor nan = constContributor("nan", Float.NaN);
        RelevanceScore score = RelevanceScore.compute(IRON, RelevanceContext.empty(), List.of(nan));
        assertEquals(0f, score.value(), 1e-6);
        assertEquals(0f, score.contributions().get("nan"), 1e-6);
    }

    @Test
    void contributorReturningOutOfRangeIsClamped() {
        RelevanceContributor over = constContributor("over", 2.5f);
        RelevanceContributor under = constContributor("under", -1.0f);
        RelevanceScore score = RelevanceScore.compute(IRON, RelevanceContext.empty(), List.of(over, under));
        assertEquals(1.0f, score.value(), 1e-6);
        assertEquals(1.0f, score.contributions().get("over"), 1e-6);
        assertEquals(0f, score.contributions().get("under"), 1e-6);
    }

    @Test
    void scoreContributionsAreImmutable() {
        RelevanceScore score = RelevanceScore.compute(IRON, RelevanceContext.empty(), List.of(constContributor("x", 0.5f)));
        assertTrue(score.contributions().containsKey("x"));
        // copyOf returns immutable map
        assertEquals(1, score.contributions().size());
    }

    private static RelevanceContributor constContributor(String name, float value) {
        return new RelevanceContributor() {
            @Override public String name() { return name; }
            @Override public float score(ItemIdentity identity, RelevanceContext ctx) { return value; }
        };
    }
}
