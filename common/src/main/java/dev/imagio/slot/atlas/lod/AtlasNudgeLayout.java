package dev.imagio.slot.atlas.lod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Push-and-recover atlas layout. Replaces the gravity model with a strictly
 * local, event-driven scheme: islands stay at their authored home, and only
 * deviate when forced.
 *
 * <p>Per island we track a {@code home} (player intent, server-authoritative)
 * and a {@code render} (the actual on-screen top-left, derived locally).
 * Render defaults to home; it deviates only via the two primitives below.
 *
 * <p><b>Push:</b> when an island grows, gets dropped, or has its home moved
 * (drag), it stays put. Any other islands that now overlap it are pushed
 * away along the smaller-displacement axis, in the direction away from the
 * grower's center. The push cascades in BFS order; each island is pushed
 * at most once per render to prevent oscillation.
 *
 * <p><b>Pull-home:</b> after pushes settle, every island that's not at its
 * home tries to sweep back along the straight line to home, stopping at
 * first contact with another island. Iterated until no further progress.
 * Pull-home never displaces other islands — only growers push.
 *
 * <p>Compared to the gravity model: there is no global force, no centroid,
 * no slide-along-edge, and no sweep-toward-origin. Layout is stable under
 * size changes (only growers' direct/cascaded neighbors can move). The
 * tradeoff is that gaps from sparse manual placement are not auto-closed —
 * compaction, if wanted, layers on top.
 */
public final class AtlasNudgeLayout {

    private static final Logger LOG = LoggerFactory.getLogger("dev.imagio.slot.atlas.nudge");
    private static final double EPS = 1e-6;
    private static final double INF = 1e18;
    /** Defensive cap on pull-home iterations. Convergence is normally 1–2 passes. */
    private static final int MAX_PULL_ITERATIONS = 64;
    /** Defensive cap on the leftover-overlap resolution loop (handles startup-overlap data). */
    private static final int MAX_LEFTOVER_PASSES = 256;

    private AtlasNudgeLayout() {
    }

    /** Axis along which a {@link #tighten} gesture moves an island. */
    public enum SnapAxis { X, Y }

    /**
     * Result of a {@link #tighten} gesture. The returned home coordinates
     * replace the target island's home. Only the snap-axis component is
     * mutated relative to the input home; the other axis is preserved
     * exactly so chained tightens (one per axis) accumulate independently.
     */
    public record TightenResult(double newHomeX, double newHomeY, SnapAxis axis, String snappedToId) {
    }

    /** Per-island input. {@code (homeX, homeY)} = player intent; {@code (w, h)} = current packed size. */
    public record IslandSpec(String id, double homeX, double homeY, double w, double h) {
        public IslandSpec {
            Objects.requireNonNull(id, "id");
            if (id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            if (!(w > 0.0) || !(h > 0.0)) {
                throw new IllegalArgumentException("size must be positive: " + w + "," + h);
            }
        }
    }

    /** Per-island output. {@code (renderX, renderY)} is the actual on-screen top-left. */
    public record IslandPlacement(String id, double renderX, double renderY, double w, double h) {
    }

    /**
     * Per-island state kept across frames so the next call can detect
     * "this island grew", "the player moved its home", etc. The caller
     * owns the map and passes it in / out unchanged; {@link #layout}
     * mutates it in place.
     */
    public record PrevIslandState(
            double homeX,
            double homeY,
            double renderX,
            double renderY,
            double w,
            double h
    ) {
    }

    /**
     * Compute placements for {@code input}, mutating {@code prevState} so
     * the next call's change-detection is correct.
     *
     * @param input islands with current home + size, in deterministic order.
     * @param prevState previous-frame state, keyed by id; mutated in place.
     *     Pass an empty map on the first call.
     */
    public static List<IslandPlacement> layout(
            List<IslandSpec> input,
            Map<String, PrevIslandState> prevState
    ) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(prevState, "prevState");

        // Diagnostic: log entry conditions so the initial-open overlap
        // bug can be traced. Includes input island count, prev-state
        // size (0 = first call), and the home coords each island
        // arrives with so we can tell whether the issue is upstream
        // (overlapping homes) or in the layout passes themselves.
        if (LOG.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder("[SLOT][nudge] layout entry input.size=");
            sb.append(input.size()).append(" prevState.size=").append(prevState.size());
            for (IslandSpec spec : input) {
                sb.append(" {").append(spec.id).append(" home=(").append(spec.homeX)
                        .append(",").append(spec.homeY).append(") size=(").append(spec.w)
                        .append("x").append(spec.h).append(")");
                PrevIslandState prev = prevState.get(spec.id);
                if (prev != null) {
                    sb.append(" prev=(").append(prev.renderX).append(",").append(prev.renderY).append(")");
                }
                sb.append("}");
            }
            LOG.info(sb.toString());
        }

        // Drop deleted islands' state. Any displacement they were holding
        // open is now slack that pull-home can recover.
        Set<String> liveIds = new HashSet<>();
        for (IslandSpec spec : input) {
            liveIds.add(spec.id);
        }
        prevState.keySet().retainAll(liveIds);

        // Build mutable rect state, classifying each island's change kind
        // since the previous frame.
        LinkedHashMap<String, Mutable> byId = new LinkedHashMap<>();
        Set<String> activeIds = new HashSet<>();
        for (IslandSpec spec : input) {
            PrevIslandState prev = prevState.get(spec.id);
            double rx;
            double ry;
            if (prev == null) {
                // NEW
                rx = spec.homeX;
                ry = spec.homeY;
                activeIds.add(spec.id);
            } else if (spec.homeX != prev.homeX || spec.homeY != prev.homeY) {
                // HOME_MOVED — player dragged. Snap the render to the new home.
                rx = spec.homeX;
                ry = spec.homeY;
                activeIds.add(spec.id);
            } else {
                rx = prev.renderX;
                ry = prev.renderY;
                if (spec.w > prev.w + EPS || spec.h > prev.h + EPS) {
                    // GREW (any dim larger). Stays at its rendered position;
                    // overlappers get pushed.
                    activeIds.add(spec.id);
                }
                // SHRUNK / UNCHANGED — not active. Pull-home recovers
                // displaced neighbours below.
            }
            byId.put(spec.id, new Mutable(spec.id, rx, ry, spec.w, spec.h, spec.homeX, spec.homeY));
        }

        // Phase 1: BFS-push from each active island in deterministic order.
        Set<String> pushedThisFrame = new HashSet<>();
        ArrayList<String> activeOrdered = new ArrayList<>(activeIds);
        activeOrdered.sort(Comparator.naturalOrder());
        for (String activeId : activeOrdered) {
            if (pushedThisFrame.contains(activeId)) {
                continue;
            }
            pushedThisFrame.add(activeId);
            bfsPush(byId.get(activeId), byId, pushedThisFrame);
        }

        // Catch leftover overlaps (startup state, two homes that happen to
        // overlap, or rare cases where the active push didn't fully resolve).
        // Pick the lex-smaller id of any overlapping pair as the new anchor.
        // First-open hazard: every island is NEW, so phase 1 locks them all.
        // When the leftover pass finds an overlap pair both already locked,
        // we unlock the non-anchor side so the anchor's push can move it.
        // Anchor stays locked → no oscillation.
        for (int pass = 0; pass < MAX_LEFTOVER_PASSES; pass++) {
            String[] pair = anyOverlap(byId);
            if (pair == null) {
                break;
            }
            String anchor = pair[0].compareTo(pair[1]) <= 0 ? pair[0] : pair[1];
            String other = pair[0].equals(anchor) ? pair[1] : pair[0];
            if (pushedThisFrame.contains(anchor) && pushedThisFrame.contains(other)) {
                // Deadlocked pair: both anchored. Unlock {@code other} so
                // the anchor's bfsPush can relocate it.
                pushedThisFrame.remove(other);
            } else if (pushedThisFrame.contains(anchor)) {
                // Anchor is already locked but {@code other} is free.
                // Promote {@code other} to anchor — forward progress.
                anchor = other;
            }
            pushedThisFrame.add(anchor);
            bfsPush(byId.get(anchor), byId, pushedThisFrame);
        }

        // Phase 2: pull every displaced island toward home along a straight sweep.
        pullHome(byId);

        // Phase 3 — final separation safety net. If any overlap still
        // remains (rare; can happen when many islands grow simultaneously
        // from ghost projections arriving in a later projection pass and
        // the leftover-resolution loop terminates with edge-case
        // configurations), brute-force separate by pushing the lex-larger
        // rect out of the lex-smaller. This bypasses {@code
        // pushedThisFrame}, so it always converges — at the cost of
        // potentially moving an island further from its home than the
        // earlier passes would. Acceptable: pull-home will recover ground
        // on the next layout call.
        //
        // After each pushAway, sweep the mover past every OTHER island
        // it now overlaps in a tight inner loop. Without this chain
        // resolve, N islands sharing a starting position need O(N²)
        // outer-loop iterations to fan out — and on a first-open with
        // many fresh islands at home (0, 0) we'd blow the outer budget
        // before convergence, leaving visible overlap until the next
        // layout pass picked up after a player action. Chain-resolving
        // per pushAway makes Phase 3 settle in a single sweep regardless
        // of starting cluster size.
        ArrayList<Mutable> phase3Sorted = new ArrayList<>(byId.values());
        phase3Sorted.sort(Comparator.comparing(m -> m.id));
        for (int pass = 0; pass < MAX_LEFTOVER_PASSES; pass++) {
            String[] pair = anyOverlap(byId);
            if (pair == null) {
                break;
            }
            String anchorId = pair[0].compareTo(pair[1]) <= 0 ? pair[0] : pair[1];
            String moverId = pair[0].equals(anchorId) ? pair[1] : pair[0];
            Mutable mover = byId.get(moverId);
            pushAway(mover, byId.get(anchorId));
            chainResolve(mover, phase3Sorted);
        }

        // Diagnostic: post-phase overlap audit. Walks every pair after
        // all 3 phases run and logs any remaining overlap with
        // positions. Tells us whether the layout output is clean (and
        // the bug lives downstream in the renderer / pack post-process)
        // or whether some overlap path is escaping our resolution
        // passes despite the chain-resolve.
        if (LOG.isInfoEnabled()) {
            ArrayList<Mutable> finalList = new ArrayList<>(byId.values());
            finalList.sort(Comparator.comparing(m -> m.id));
            int overlaps = 0;
            StringBuilder overlapDetail = new StringBuilder();
            for (int i = 0; i < finalList.size(); i++) {
                Mutable a = finalList.get(i);
                for (int j = i + 1; j < finalList.size(); j++) {
                    Mutable b = finalList.get(j);
                    if (overlaps(a, b)) {
                        overlaps++;
                        overlapDetail.append(" {").append(a.id).append("@(").append(a.x).append(",").append(a.y)
                                .append(",").append(a.w).append("x").append(a.h)
                                .append(") <-> ").append(b.id).append("@(").append(b.x).append(",").append(b.y)
                                .append(",").append(b.w).append("x").append(b.h).append(")}");
                    }
                }
            }
            if (overlaps > 0) {
                LOG.warn("[SLOT][nudge] EXIT WITH {} OVERLAPS:{}", overlaps, overlapDetail.toString());
            } else {
                LOG.info("[SLOT][nudge] layout clean exit (no overlaps), island.count={}", byId.size());
            }
        }

        // Build output and update prevState.
        ArrayList<IslandPlacement> out = new ArrayList<>(byId.size());
        for (IslandSpec spec : input) {
            Mutable m = byId.get(spec.id);
            out.add(new IslandPlacement(m.id, m.x, m.y, m.w, m.h));
            prevState.put(m.id, new PrevIslandState(m.homeX, m.homeY, m.x, m.y, m.w, m.h));
        }
        if (LOG.isDebugEnabled() && !activeIds.isEmpty()) {
            LOG.debug("[SLOT][nudge] active={} pushed={} total={}",
                    activeIds, pushedThisFrame, byId.size());
        }
        return out;
    }

    /**
     * Tighten gesture: slide {@code targetId} along the smaller-gap axis
     * until it touches its nearest axis-aligned neighbour (or any other
     * island in the path), and set its new home to the stop position
     * plus {@code followDelta} past, so subsequent shrinks of the snap
     * target are absorbed automatically.
     *
     * <p>The home update mutates only the snap-axis component; the other
     * axis stays at the input home. Two consecutive tightens (one per
     * axis) compose into a sticky-on-both-axes snap.
     *
     * <p>Returns {@code null} when {@code targetId} has no axis-aligned
     * neighbour (i.e., every other island is fully diagonal from it) —
     * the gesture is a no-op in that case.
     *
     * <p>Mutates {@code state} so the snap effect persists into the next
     * {@link #layout} call: the target's entry is updated with the new
     * home AND the stop position as its new render. Without this update,
     * the next layout call would observe a home change and snap render
     * back to home (which is now past the snap target), then push the
     * snap target aside — exactly the wrong outcome.
     *
     * @param state per-island state, keyed by id. Mutated in place if a
     *     snap target was found.
     * @param targetId id of the island the player Shift+clicked.
     * @param followDelta how far past the stop position to set the new
     *     home along the snap axis. Bounds the deletion-teleport distance
     *     and the maximum follow-on-shrink. Must be non-negative.
     */
    public static TightenResult tighten(
            Map<String, PrevIslandState> state,
            String targetId,
            double followDelta
    ) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(targetId, "targetId");
        if (followDelta < 0) {
            throw new IllegalArgumentException("followDelta must be non-negative: " + followDelta);
        }
        PrevIslandState target = state.get(targetId);
        if (target == null) {
            return null;
        }

        SnapAxis bestAxis = null;
        int bestSign = 0;
        double bestGap = Double.POSITIVE_INFINITY;
        String bestId = null;
        for (Map.Entry<String, PrevIslandState> entry : state.entrySet()) {
            String otherId = entry.getKey();
            if (otherId.equals(targetId)) continue;
            PrevIslandState other = entry.getValue();

            // X-axis candidate: requires Y projection overlap.
            if (rangesOverlap(target.renderY, target.h, other.renderY, other.h)) {
                if (other.renderX + other.w <= target.renderX + EPS) {
                    double gap = target.renderX - (other.renderX + other.w);
                    if (gap >= 0 && betterCandidate(gap, otherId, bestGap, bestId)) {
                        bestAxis = SnapAxis.X;
                        bestSign = -1;
                        bestGap = gap;
                        bestId = otherId;
                    }
                } else if (target.renderX + target.w <= other.renderX + EPS) {
                    double gap = other.renderX - (target.renderX + target.w);
                    if (gap >= 0 && betterCandidate(gap, otherId, bestGap, bestId)) {
                        bestAxis = SnapAxis.X;
                        bestSign = +1;
                        bestGap = gap;
                        bestId = otherId;
                    }
                }
            }

            // Y-axis candidate: requires X projection overlap.
            if (rangesOverlap(target.renderX, target.w, other.renderX, other.w)) {
                if (other.renderY + other.h <= target.renderY + EPS) {
                    double gap = target.renderY - (other.renderY + other.h);
                    if (gap >= 0 && betterCandidate(gap, otherId, bestGap, bestId)) {
                        bestAxis = SnapAxis.Y;
                        bestSign = -1;
                        bestGap = gap;
                        bestId = otherId;
                    }
                } else if (target.renderY + target.h <= other.renderY + EPS) {
                    double gap = other.renderY - (target.renderY + target.h);
                    if (gap >= 0 && betterCandidate(gap, otherId, bestGap, bestId)) {
                        bestAxis = SnapAxis.Y;
                        bestSign = +1;
                        bestGap = gap;
                        bestId = otherId;
                    }
                }
            }
        }

        if (bestAxis == null) {
            LOG.debug("[SLOT][nudge] tighten id={} → no axis-aligned neighbour", targetId);
            return null;
        }

        // Sweep target along (axis, sign) until first contact with any island
        // in the path. The result may stop short of `bestId` if some other
        // island is closer along the sweep.
        double sweepDistance = sweepDistanceAlongAxis(targetId, target, state, bestAxis, bestSign);

        double stopX = target.renderX + (bestAxis == SnapAxis.X ? sweepDistance * bestSign : 0);
        double stopY = target.renderY + (bestAxis == SnapAxis.Y ? sweepDistance * bestSign : 0);

        double newHomeX = target.homeX;
        double newHomeY = target.homeY;
        if (bestAxis == SnapAxis.X) {
            newHomeX = stopX + bestSign * followDelta;
        } else {
            newHomeY = stopY + bestSign * followDelta;
        }
        // Persist render and home so a subsequent layout() call observes
        // no home change and leaves the cluster alone.
        state.put(targetId, new PrevIslandState(newHomeX, newHomeY, stopX, stopY, target.w, target.h));
        LOG.info("[SLOT][nudge] tighten id={} axis={} sign={} stop=({},{}) newHome=({},{}) snappedTo={}",
                targetId, bestAxis, bestSign, stopX, stopY, newHomeX, newHomeY, bestId);
        return new TightenResult(newHomeX, newHomeY, bestAxis, bestId);
    }

    /**
     * Prefer a real gap (> EPS) over a zero gap (already flush) regardless
     * of magnitude — this lets a chain of tightens move to a different
     * axis once one axis is flush, instead of re-picking the flush
     * neighbour and producing a no-op snap. Within the same category
     * (both real or both flush), smaller gap wins; final tiebreak by id.
     */
    private static boolean betterCandidate(double gap, String id, double bestGap, String bestId) {
        if (bestId == null) return true;
        boolean newFlush = gap < EPS;
        boolean bestFlush = bestGap < EPS;
        if (newFlush != bestFlush) {
            return !newFlush;
        }
        if (gap < bestGap - EPS) return true;
        if (gap > bestGap + EPS) return false;
        return id.compareTo(bestId) < 0;
    }

    private static boolean rangesOverlap(double aStart, double aLen, double bStart, double bLen) {
        return aStart + aLen > bStart + EPS && bStart + bLen > aStart + EPS;
    }

    private static double sweepDistanceAlongAxis(
            String targetId,
            PrevIslandState target,
            Map<String, PrevIslandState> state,
            SnapAxis axis,
            int sign
    ) {
        double minGap = Double.POSITIVE_INFINITY;
        for (Map.Entry<String, PrevIslandState> entry : state.entrySet()) {
            if (entry.getKey().equals(targetId)) continue;
            PrevIslandState other = entry.getValue();
            if (axis == SnapAxis.X) {
                if (!rangesOverlap(target.renderY, target.h, other.renderY, other.h)) continue;
                if (sign > 0) {
                    if (other.renderX + EPS < target.renderX + target.w) continue;
                    double gap = other.renderX - (target.renderX + target.w);
                    if (gap < minGap) minGap = gap;
                } else {
                    if (other.renderX + other.w > target.renderX + EPS) continue;
                    double gap = target.renderX - (other.renderX + other.w);
                    if (gap < minGap) minGap = gap;
                }
            } else {
                if (!rangesOverlap(target.renderX, target.w, other.renderX, other.w)) continue;
                if (sign > 0) {
                    if (other.renderY + EPS < target.renderY + target.h) continue;
                    double gap = other.renderY - (target.renderY + target.h);
                    if (gap < minGap) minGap = gap;
                } else {
                    if (other.renderY + other.h > target.renderY + EPS) continue;
                    double gap = target.renderY - (other.renderY + other.h);
                    if (gap < minGap) minGap = gap;
                }
            }
        }
        return Double.isFinite(minGap) ? Math.max(0.0, minGap) : 0.0;
    }

    /** Run a BFS push starting from {@code seed} until no more overlaps remain reachable. */
    private static void bfsPush(
            Mutable seed,
            LinkedHashMap<String, Mutable> byId,
            Set<String> pushed
    ) {
        Deque<Mutable> queue = new ArrayDeque<>();
        queue.add(seed);
        ArrayList<Mutable> sorted = new ArrayList<>(byId.values());
        sorted.sort(Comparator.comparing(m -> m.id));
        while (!queue.isEmpty()) {
            Mutable pusher = queue.poll();
            for (Mutable other : sorted) {
                if (other == pusher) continue;
                if (pushed.contains(other.id)) continue;
                if (!overlaps(pusher, other)) continue;
                pushAway(other, pusher);
                // Multiple overlappers of the same pusher all land at the
                // pusher's far edge initially; chain them out so siblings
                // don't stack on top of each other.
                resolveAgainstPushed(other, pushed, sorted);
                pushed.add(other.id);
                queue.add(other);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[SLOT][nudge] push id={} away from={} to=({},{})",
                            other.id, pusher.id, other.x, other.y);
                }
            }
        }
    }

    /**
     * Phase-3 helper: shove {@code mover} past every island it still
     * overlaps. Uses a monotonic push strategy — always increase
     * {@code mover.x} (and fall back to increasing {@code mover.y} when
     * x has nothing left to push past). The smaller-axis policy used
     * by {@link #pushAway} alone cycles when {@code mover} is sandwiched
     * between obstacles on opposite sides: pushing right onto obstacle
     * A flips the smaller axis to "left" against obstacle B, which
     * sends {@code mover} back into A. Forcing strictly-rightward
     * progress (or downward as a tiebreaker when no rightward push is
     * available) guarantees termination — the mover walks past every
     * obstacle once and ends past their collective right edge.
     */
    private static void chainResolve(
            Mutable mover,
            ArrayList<Mutable> sorted
    ) {
        int budget = sorted.size() * 4;
        while (budget-- > 0) {
            Mutable conflict = null;
            for (Mutable other : sorted) {
                if (other == mover) continue;
                if (overlaps(mover, other)) {
                    conflict = other;
                    break;
                }
            }
            if (conflict == null) return;
            // Monotonic push: snap mover's left edge to conflict's
            // right edge. Always moves forward so prior obstacles can't
            // re-conflict — even when {@link #pushAway}'s smaller-axis
            // pick would have bounced mover backwards.
            double moveTo = conflict.x + conflict.w;
            if (moveTo > mover.x) {
                mover.x = moveTo;
            } else {
                // Already past on x but still overlapping (rare: same
                // x range, different y). Step down past conflict.
                mover.y = conflict.y + conflict.h;
            }
        }
    }

    /**
     * After {@code mover} has been pushed once, walk through the already-
     * pushed set and shove {@code mover} past any sibling it still overlaps.
     * This is what makes a row of islands fan out from a single pusher
     * instead of stacking at the pusher's far edge.
     */
    private static void resolveAgainstPushed(
            Mutable mover,
            Set<String> pushed,
            ArrayList<Mutable> sorted
    ) {
        int budget = sorted.size() * 2;
        while (budget-- > 0) {
            Mutable conflict = null;
            for (Mutable p : sorted) {
                if (p == mover) continue;
                if (!pushed.contains(p.id)) continue;
                if (overlaps(mover, p)) {
                    conflict = p;
                    break;
                }
            }
            if (conflict == null) return;
            pushAway(mover, conflict);
        }
    }

    /**
     * Move {@code mover} just clear of {@code pusher} along whichever axis
     * separates them with the smaller displacement. Direction is chosen by
     * the relative center positions, so the mover ends up on the far side
     * of the pusher from where it started.
     *
     * <p>The mover's edge ends exactly flush with the pusher's edge — no
     * EPS slop. {@link #overlaps} uses strict inequality so flush rects
     * read as non-overlapping; adding EPS here would shift the pusher's
     * center and flip the next cascaded push's "which side?" decision.
     */
    private static void pushAway(Mutable mover, Mutable pusher) {
        double dxRight = (pusher.x + pusher.w) - mover.x;
        double dxLeft = (mover.x + mover.w) - pusher.x;
        double dx = Math.min(dxRight, dxLeft);
        double dyDown = (pusher.y + pusher.h) - mover.y;
        double dyUp = (mover.y + mover.h) - pusher.y;
        double dy = Math.min(dyDown, dyUp);
        boolean useX = dx <= dy;
        if (useX) {
            if (mover.centerX() >= pusher.centerX()) {
                mover.x = pusher.x + pusher.w;
            } else {
                mover.x = pusher.x - mover.w;
            }
        } else {
            if (mover.centerY() >= pusher.centerY()) {
                mover.y = pusher.y + pusher.h;
            } else {
                mover.y = pusher.y - mover.h;
            }
        }
    }

    /**
     * For every island where render ≠ home, sweep along the straight line
     * to home and stop at first contact. Iterates until no further progress.
     *
     * <p>Order: smallest displacement first, so islands closest to home
     * recover before farther-displaced ones — this avoids the further
     * island finding the closer one in its way and bailing immediately.
     */
    private static void pullHome(LinkedHashMap<String, Mutable> byId) {
        for (int iter = 0; iter < MAX_PULL_ITERATIONS; iter++) {
            ArrayList<Mutable> ordered = new ArrayList<>(byId.values());
            ordered.sort(Comparator
                    .<Mutable>comparingDouble(m -> Math.hypot(m.x - m.homeX, m.y - m.homeY))
                    .thenComparing(m -> m.id));
            boolean anyMoved = false;
            for (Mutable m : ordered) {
                double vx = m.homeX - m.x;
                double vy = m.homeY - m.y;
                if (Math.abs(vx) < EPS && Math.abs(vy) < EPS) {
                    continue;
                }
                double t = sweepMaxT(m, vx, vy, byId);
                if (t < EPS) {
                    continue;
                }
                m.x += vx * t;
                m.y += vy * t;
                anyMoved = true;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[SLOT][nudge] pull-home id={} t={} to=({},{}) home=({},{})",
                            m.id, t, m.x, m.y, m.homeX, m.homeY);
                }
            }
            if (!anyMoved) {
                return;
            }
        }
    }

    /**
     * Maximum {@code t} in {@code [0, 1]} such that translating {@code mover}
     * by {@code (vx*t, vy*t)} doesn't push it into any other island. Uses
     * standard swept-AABB. Returns 0 if {@code mover} is already touching
     * another island in the move direction.
     */
    private static double sweepMaxT(
            Mutable mover,
            double vx, double vy,
            LinkedHashMap<String, Mutable> byId
    ) {
        double maxT = 1.0;
        for (Mutable other : byId.values()) {
            if (other == mover) continue;
            double sumHalfX = (mover.w + other.w) / 2.0;
            double sumHalfY = (mover.h + other.h) / 2.0;
            double dx = mover.centerX() - other.centerX();
            double dy = mover.centerY() - other.centerY();

            // For each axis, [tEnter, tLeave] is the t-interval during
            // which the mover's projection overlaps the other's.
            double tEnterX;
            double tLeaveX;
            if (Math.abs(vx) < EPS) {
                if (Math.abs(dx) <= sumHalfX + EPS) {
                    tEnterX = -INF;
                    tLeaveX = INF;
                } else {
                    continue; // projections never overlap on x — can't collide.
                }
            } else {
                double tA = (sumHalfX - dx) / vx;
                double tB = (-sumHalfX - dx) / vx;
                tEnterX = Math.min(tA, tB);
                tLeaveX = Math.max(tA, tB);
            }

            double tEnterY;
            double tLeaveY;
            if (Math.abs(vy) < EPS) {
                if (Math.abs(dy) <= sumHalfY + EPS) {
                    tEnterY = -INF;
                    tLeaveY = INF;
                } else {
                    continue;
                }
            } else {
                double tA = (sumHalfY - dy) / vy;
                double tB = (-sumHalfY - dy) / vy;
                tEnterY = Math.min(tA, tB);
                tLeaveY = Math.max(tA, tB);
            }

            double tEnter = Math.max(tEnterX, tEnterY);
            double tLeave = Math.min(tLeaveX, tLeaveY);
            if (tEnter > tLeave) continue; // never collide on this sweep.
            if (tEnter <= EPS) {
                // Currently touching/overlapping; can't progress.
                return 0.0;
            }
            if (tEnter < maxT) {
                maxT = tEnter;
            }
        }
        return Math.max(0.0, maxT);
    }

    private static String[] anyOverlap(LinkedHashMap<String, Mutable> byId) {
        ArrayList<Mutable> list = new ArrayList<>(byId.values());
        list.sort(Comparator.comparing(m -> m.id));
        for (int i = 0; i < list.size(); i++) {
            Mutable a = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                Mutable b = list.get(j);
                if (overlaps(a, b)) {
                    return new String[]{a.id, b.id};
                }
            }
        }
        return null;
    }

    private static boolean overlaps(Mutable a, Mutable b) {
        return a.x + a.w > b.x + EPS
                && b.x + b.w > a.x + EPS
                && a.y + a.h > b.y + EPS
                && b.y + b.h > a.y + EPS;
    }

    private static final class Mutable {
        final String id;
        double x;
        double y;
        final double w;
        final double h;
        final double homeX;
        final double homeY;

        Mutable(String id, double x, double y, double w, double h, double homeX, double homeY) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.homeX = homeX;
            this.homeY = homeY;
        }

        double centerX() {
            return x + w / 2.0;
        }

        double centerY() {
            return y + h / 2.0;
        }
    }
}
