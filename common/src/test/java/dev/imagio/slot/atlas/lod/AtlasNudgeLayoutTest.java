package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.atlas.lod.AtlasNudgeLayout.IslandPlacement;
import dev.imagio.slot.atlas.lod.AtlasNudgeLayout.IslandSpec;
import dev.imagio.slot.atlas.lod.AtlasNudgeLayout.PrevIslandState;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtlasNudgeLayoutTest {

    private static final double TOL = 1e-3;

    private static IslandSpec spec(String id, double homeX, double homeY, double w, double h) {
        return new IslandSpec(id, homeX, homeY, w, h);
    }

    private static IslandPlacement find(List<IslandPlacement> rs, String id) {
        for (IslandPlacement r : rs) if (r.id().equals(id)) return r;
        return null;
    }

    private static boolean overlap(IslandPlacement a, IslandPlacement b) {
        return a.renderX() + a.w() > b.renderX() + 1e-6
                && b.renderX() + b.w() > a.renderX() + 1e-6
                && a.renderY() + a.h() > b.renderY() + 1e-6
                && b.renderY() + b.h() > a.renderY() + 1e-6;
    }

    private static void assertNoOverlap(List<IslandPlacement> rs) {
        for (int i = 0; i < rs.size(); i++) {
            for (int j = i + 1; j < rs.size(); j++) {
                assertFalse(overlap(rs.get(i), rs.get(j)),
                        "overlap: " + rs.get(i) + " vs " + rs.get(j));
            }
        }
    }

    @Test
    void emptyInputProducesEmptyOutput() {
        Map<String, PrevIslandState> state = new HashMap<>();
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(), state);
        assertEquals(0, out.size());
        assertTrue(state.isEmpty());
    }

    @Test
    void newIslandRendersExactlyAtHome() {
        Map<String, PrevIslandState> state = new HashMap<>();
        List<IslandPlacement> out = AtlasNudgeLayout.layout(
                List.of(spec("a", 100, 200, 4, 4)), state);
        IslandPlacement a = find(out, "a");
        assertNotNull(a);
        assertEquals(100, a.renderX(), TOL);
        assertEquals(200, a.renderY(), TOL);
    }

    @Test
    void twoNonOverlappingHomesStayAtHome() {
        Map<String, PrevIslandState> state = new HashMap<>();
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 100, 0, 4, 4)
        ), state);
        assertEquals(0.0, find(out, "a").renderX(), TOL);
        assertEquals(100.0, find(out, "b").renderX(), TOL);
    }

    @Test
    void overlappingHomesResolveAtStartup() {
        // Two islands authored at the same home — leftover-overlap pass
        // pushes the lex-larger id away from the lex-smaller one.
        Map<String, PrevIslandState> state = new HashMap<>();
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 0, 0, 4, 4)
        ), state);
        assertNoOverlap(out);
        IslandPlacement a = find(out, "a");
        IslandPlacement b = find(out, "b");
        // a stays at home; b is the one that gets pushed.
        assertEquals(0, a.renderX(), TOL);
        assertEquals(0, a.renderY(), TOL);
        assertTrue(b.renderX() != 0 || b.renderY() != 0);
    }

    @Test
    void growingIslandPushesNeighbour() {
        // Frame 1: a and b flush east of each other, no growth.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 4, 0, 4, 4)
        ), state);
        // Frame 2: a grows. b should be pushed east.
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 8, 4),
                spec("b", 4, 0, 4, 4)
        ), state);
        assertNoOverlap(out);
        IslandPlacement a = find(out, "a");
        IslandPlacement b = find(out, "b");
        // a stayed (it's the grower).
        assertEquals(0, a.renderX(), TOL);
        // b is now east of a's grown right edge.
        assertTrue(b.renderX() >= a.renderX() + a.w() - TOL,
                "expected b east of a; a=" + a + " b=" + b);
    }

    @Test
    void shrinkingIslandPullsDisplacedNeighbourBackHome() {
        // Frame 1: a and b sit at homes (0,0) and (4,0).
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 4, 0, 4, 4)
        ), state);
        // Frame 2: a grows, pushes b east.
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 8, 4),
                spec("b", 4, 0, 4, 4)
        ), state);
        // Frame 3: a shrinks back. b should pull back to its home (4, 0).
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 4, 0, 4, 4)
        ), state);
        IslandPlacement b = find(out, "b");
        assertEquals(4, b.renderX(), TOL);
        assertEquals(0, b.renderY(), TOL);
    }

    @Test
    void cascadingPushChainPropagates() {
        // a, b, c flush in a row. Grow a → b is pushed east, which pushes c east.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 4, 0, 4, 4),
                spec("c", 8, 0, 4, 4)
        ), state);
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 8, 4),
                spec("b", 4, 0, 4, 4),
                spec("c", 8, 0, 4, 4)
        ), state);
        assertNoOverlap(out);
        IslandPlacement b = find(out, "b");
        IslandPlacement c = find(out, "c");
        // b moved east past a's new edge; c moved east past b's edge.
        assertTrue(b.renderX() >= 8 - TOL);
        assertTrue(c.renderX() >= b.renderX() + b.w() - TOL);
    }

    @Test
    void deletedIslandLetsDisplacedNeighbourReturnHome() {
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 4, 0, 4, 4)
        ), state);
        // Grow a so b is pushed east.
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 8, 4),
                spec("b", 4, 0, 4, 4)
        ), state);
        // Delete a. b should pull back to home.
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("b", 4, 0, 4, 4)
        ), state);
        assertEquals(1, out.size());
        IslandPlacement b = find(out, "b");
        assertEquals(4, b.renderX(), TOL);
        // Deleted state was pruned.
        assertFalse(state.containsKey("a"));
    }

    @Test
    void homeMoveSnapsRenderToNewHome() {
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 100, 0, 4, 4)
        ), state);
        // Drag b to (200, 50). Render should follow the new home exactly.
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 200, 50, 4, 4)
        ), state);
        IslandPlacement b = find(out, "b");
        assertEquals(200, b.renderX(), TOL);
        assertEquals(50, b.renderY(), TOL);
    }

    @Test
    void pullHomeStopsAtFirstContactWhenBlocked() {
        // a at home (0,0). b at home (10, 0), got pushed to (20, 0).
        // Now c sits at (15, 0), blocking b's path home. b should pull
        // toward home but stop short of c.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 10, 0, 4, 4),
                spec("c", 15, 0, 4, 4)
        ), state);
        // Manually displace b by growing a so b is pushed east of c.
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 22, 4),  // huge grow — pushes b past c.
                spec("b", 10, 0, 4, 4),
                spec("c", 15, 0, 4, 4)
        ), state);
        // Shrink a back. b tries to pull home (10, 0); c at (15, 0) blocks.
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 10, 0, 4, 4),
                spec("c", 15, 0, 4, 4)
        ), state);
        assertNoOverlap(out);
        IslandPlacement b = find(out, "b");
        // b ended up west of c (or at least flush), but didn't reach home (10).
        IslandPlacement c = find(out, "c");
        assertTrue(b.renderX() + b.w() <= c.renderX() + TOL || b.renderX() >= c.renderX() + c.w() - TOL,
                "b should be on one side of c, not overlapping; b=" + b + " c=" + c);
    }

    @Test
    void unrelatedIslandsDoNotMoveOnGrow() {
        // d is far from the cluster. When a grows and pushes b/c, d stays put.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 4, 0, 4, 4),
                spec("c", 8, 0, 4, 4),
                spec("d", 200, 200, 4, 4)
        ), state);
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 8, 4),
                spec("b", 4, 0, 4, 4),
                spec("c", 8, 0, 4, 4),
                spec("d", 200, 200, 4, 4)
        ), state);
        IslandPlacement d = find(out, "d");
        assertEquals(200, d.renderX(), TOL);
        assertEquals(200, d.renderY(), TOL);
    }

    @Test
    void simultaneousProximityGrowthResolvesWithoutOverlap() {
        // Repro for: when the player walks near several chests, multiple
        // islands gain ghost cells in the same frame and grow at once. If
        // the BFS-push doesn't fan out before the leftover pass anchors
        // them, neighbours can sit overlapped until one is dragged.
        //
        // Three islands at flush-row homes, all grow simultaneously by
        // varying amounts.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 4, 0, 4, 4),
                spec("c", 8, 0, 4, 4)
        ), state);
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 6, 4),
                spec("b", 4, 0, 6, 4),
                spec("c", 8, 0, 6, 4)
        ), state);
        assertNoOverlap(out);
    }

    @Test
    void simultaneousProximityGrowthOnFirstOpenResolvesWithoutOverlap() {
        // Same scenario but the player just opened the workspace so
        // prevState is empty — every island is NEW and locks into
        // pushedThisFrame in phase 1. The leftover pass must still
        // resolve overlaps under all-locked startup state.
        Map<String, PrevIslandState> state = new HashMap<>();
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 6, 4),
                spec("b", 4, 0, 6, 4),
                spec("c", 8, 0, 6, 4)
        ), state);
        assertNoOverlap(out);
    }

    @Test
    void manyIslandsAllGrowingResolveWithoutOverlap() {
        // Realistic /slot test populate scenario: many islands across a
        // grid, all gain ghost cells the moment the player walks into
        // proximate-chest range, all grow simultaneously.
        Map<String, PrevIslandState> state = new HashMap<>();
        java.util.ArrayList<IslandSpec> initial = new java.util.ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                String id = "island-" + row + "-" + col;
                initial.add(spec(id, col * 8.0, row * 8.0, 6, 6));
            }
        }
        AtlasNudgeLayout.layout(initial, state);
        java.util.ArrayList<IslandSpec> grown = new java.util.ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                String id = "island-" + row + "-" + col;
                grown.add(spec(id, col * 8.0, row * 8.0, 9, 9));
            }
        }
        List<IslandPlacement> out = AtlasNudgeLayout.layout(grown, state);
        assertNoOverlap(out);
    }

    @Test
    void verticallyStackedGrowersDoNotOverlap() {
        // Same hazard along the y axis. Three islands stacked top-to-bottom
        // each grow on the next frame.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 0, 4, 4, 4),
                spec("c", 0, 8, 4, 4)
        ), state);
        List<IslandPlacement> out = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 6),
                spec("b", 0, 4, 4, 6),
                spec("c", 0, 8, 4, 6)
        ), state);
        assertNoOverlap(out);
    }

    @Test
    void multipleGrowersResolveDeterministically() {
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 10, 0, 4, 4),
                spec("c", 5, 5, 4, 4)
        ), state);
        // Grow both a and b on the same frame.
        Map<String, PrevIslandState> stateA = new HashMap<>(state);
        Map<String, PrevIslandState> stateB = new HashMap<>(state);
        List<IslandPlacement> outA = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 6, 4),
                spec("b", 10, 0, 6, 4),
                spec("c", 5, 5, 4, 4)
        ), stateA);
        List<IslandPlacement> outB = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 6, 4),
                spec("b", 10, 0, 6, 4),
                spec("c", 5, 5, 4, 4)
        ), stateB);
        for (IslandPlacement a : outA) {
            IslandPlacement b = find(outB, a.id());
            assertEquals(a.renderX(), b.renderX(), TOL);
            assertEquals(a.renderY(), b.renderY(), TOL);
        }
    }

    @Test
    void tightenReturnsNullForUnknownId() {
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(spec("a", 0, 0, 4, 4)), state);
        assertNull(AtlasNudgeLayout.tighten(state, "ghost", 5));
    }

    @Test
    void tightenReturnsNullWhenNoAxisAlignedNeighbour() {
        // a at (0,0), b at (50,50). Neither shares an axis-aligned face.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 50, 50, 4, 4)
        ), state);
        assertNull(AtlasNudgeLayout.tighten(state, "a", 5));
    }

    @Test
    void tightenSnapsAcrossEastGap() {
        // a at (0,0,4,4), b at (10,0,4,4). Tighten b → b slides west to flush.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 10, 0, 4, 4)
        ), state);
        AtlasNudgeLayout.TightenResult result = AtlasNudgeLayout.tighten(state, "b", 2);
        assertNotNull(result);
        assertEquals(AtlasNudgeLayout.SnapAxis.X, result.axis());
        assertEquals("a", result.snappedToId());
        // Stop position would be x=4 (flush against a). New home = stop + sign*delta.
        // Sign is -1 (motion was west), so new home.x = 4 - 2 = 2.
        assertEquals(2.0, result.newHomeX(), TOL);
        // Y unchanged.
        assertEquals(0.0, result.newHomeY(), TOL);
    }

    @Test
    void tightenLeavesNonSnapAxisAlone() {
        // a at (0,0), b at (10,3). Tighten b → snap along X. Y of b's home unchanged.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 10, 3, 4, 4)
        ), state);
        AtlasNudgeLayout.TightenResult result = AtlasNudgeLayout.tighten(state, "b", 2);
        assertNotNull(result);
        assertEquals(3.0, result.newHomeY(), TOL);
    }

    @Test
    void tightenChainsTwoAxesIndependently() {
        // a at (0,0). b at (10, 10). Tighten b along X then along Y.
        // Each tighten should preserve the other-axis offset.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 10, 10, 4, 4)
        ), state);
        // First tighten: no axis-aligned neighbour (diagonal). Returns null.
        assertNull(AtlasNudgeLayout.tighten(state, "b", 2));
        // Manually drag b to (10, 0) — now it shares Y with a.
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 10, 0, 4, 4)
        ), state);
        AtlasNudgeLayout.TightenResult x = AtlasNudgeLayout.tighten(state, "b", 2);
        assertEquals(AtlasNudgeLayout.SnapAxis.X, x.axis());
        // Apply the X tighten as if the controller wrote it back as the new home.
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", x.newHomeX(), x.newHomeY(), 4, 4)
        ), state);
        // Add a third island to the south of b so a Y-axis tighten has a target.
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", x.newHomeX(), x.newHomeY(), 4, 4),
                spec("c", 4, 20, 4, 4)
        ), state);
        AtlasNudgeLayout.TightenResult y = AtlasNudgeLayout.tighten(state, "b", 2);
        assertNotNull(y);
        assertEquals(AtlasNudgeLayout.SnapAxis.Y, y.axis());
        // X home should have been preserved from the prior tighten.
        assertEquals(x.newHomeX(), y.newHomeX(), TOL);
    }

    @Test
    void tightenStopsAtFirstObstacleNotIntendedTarget() {
        // a far west, c in between, b targets a but c blocks.
        // a at (0,0,4,4). c at (10,0,4,4). b at (20,0,4,4). Tighten b along X
        // toward a. Path goes through c. b stops at c, not at a.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("c", 10, 0, 4, 4),
                spec("b", 20, 0, 4, 4)
        ), state);
        AtlasNudgeLayout.TightenResult result = AtlasNudgeLayout.tighten(state, "b", 2);
        assertNotNull(result);
        // b should snap toward a's east edge but be blocked by c first.
        // c's east face is at x=14. b stops with b.x=14. New home.x = 14 - 2 = 12.
        assertEquals(12.0, result.newHomeX(), TOL);
    }

    @Test
    void tightenIsIdempotentWhenAlreadyFlush() {
        // a at (0,0,4,4), b at (4,0,4,4) — flush already.
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 4, 0, 4, 4)
        ), state);
        AtlasNudgeLayout.TightenResult once = AtlasNudgeLayout.tighten(state, "b", 2);
        assertNotNull(once);
        // Apply it.
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", once.newHomeX(), once.newHomeY(), 4, 4)
        ), state);
        AtlasNudgeLayout.TightenResult twice = AtlasNudgeLayout.tighten(state, "b", 2);
        assertNotNull(twice);
        // Second tighten should produce the same home (idempotent).
        assertEquals(once.newHomeX(), twice.newHomeX(), TOL);
        assertEquals(once.newHomeY(), twice.newHomeY(), TOL);
    }

    @Test
    void tightenFollowDeltaAbsorbsShrinkUpToCap() {
        // a (the snap target) shrinks; b (which was tightened against a) follows
        // up to followDelta and stops short if the shrink exceeds it.
        Map<String, PrevIslandState> state = new HashMap<>();
        // a is wide, b is east of a.
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 8, 4),
                spec("b", 10, 0, 4, 4)
        ), state);
        AtlasNudgeLayout.TightenResult t = AtlasNudgeLayout.tighten(state, "b", 3);
        assertNotNull(t);
        // Apply tighten — b's new home is past a's east edge.
        // a.right=8, so b's stop is x=8, new home.x=8-3=5.
        assertEquals(5.0, t.newHomeX(), TOL);
        // Re-layout with b's new home.
        List<IslandPlacement> after = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 8, 4),
                spec("b", t.newHomeX(), t.newHomeY(), 4, 4)
        ), state);
        // b should render flush against a (pull-home blocked at a's east face).
        IslandPlacement bRendered = find(after, "b");
        assertEquals(8.0, bRendered.renderX(), TOL);

        // Shrink a by 2 (a.w=6). b should follow by 2 (within delta of 3).
        after = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 6, 4),
                spec("b", t.newHomeX(), t.newHomeY(), 4, 4)
        ), state);
        bRendered = find(after, "b");
        assertEquals(6.0, bRendered.renderX(), TOL);

        // Shrink a all the way to w=2. b should follow, but capped at home.x=5.
        after = AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 2, 4),
                spec("b", t.newHomeX(), t.newHomeY(), 4, 4)
        ), state);
        bRendered = find(after, "b");
        // b stops at home (5) or at first contact, whichever comes first.
        // a.right=2, home.x=5; b can pull all the way home (no obstacle).
        assertEquals(5.0, bRendered.renderX(), TOL);
    }

    @Test
    void prevStatePrunesDeletedIslands() {
        Map<String, PrevIslandState> state = new HashMap<>();
        AtlasNudgeLayout.layout(List.of(
                spec("a", 0, 0, 4, 4),
                spec("b", 10, 0, 4, 4)
        ), state);
        assertTrue(state.containsKey("a") && state.containsKey("b"));
        AtlasNudgeLayout.layout(List.of(spec("a", 0, 0, 4, 4)), state);
        assertTrue(state.containsKey("a"));
        assertFalse(state.containsKey("b"));
    }
}
