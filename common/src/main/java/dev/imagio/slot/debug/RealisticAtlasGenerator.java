package dev.imagio.slot.debug;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public final class RealisticAtlasGenerator {
    public static final String SYNTHETIC_ISLAND_ID_PREFIX = "slot-test-island-";

    // Mirror the real atlas layout so generated assignments land on the same invisible
    // grid the UI uses for clamp + snap — otherwise every card shows up misaligned and
    // needs a drag to snap into place.
    private static final int CARD_WIDTH = SlotWorkspaceAtlasLayout.CARD_WIDTH;
    private static final int CARD_HEIGHT = SlotWorkspaceAtlasLayout.CARD_HEIGHT;
    private static final int CARD_GAP = SlotWorkspaceAtlasLayout.CARD_GAP;
    private static final int ISLAND_PADDING_X = SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_X;
    private static final int ISLAND_PADDING_Y = SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_Y;
    private static final int ISLAND_CONTENT_TOP = SlotWorkspaceAtlasLayout.ISLAND_CONTENT_TOP;
    private static final int ISLAND_GAP = 120;
    private static final int ISLAND_PARENT_GROUP_GAP = 220;
    private static final int ATLAS_ORIGIN_X = 64;
    private static final int ATLAS_ORIGIN_Y = 64;

    private static final int MIN_CARDS_PER_ROW = 4;
    private static final int MAX_CARDS_PER_ROW = 20;

    private static final int CHEST_SLOTS = 27;
    private static final int CHEST_FILL_MIN_SLOTS = 4;
    private static final int CHEST_FILL_MAX_SLOTS = 20;
    private static final double CHEST_UNLINKED_FRACTION = 0.10;
    private static final double CHEST_BUCKET_BIAS = 0.85;
    private static final int CHEST_MAX_PER_BUCKET = 5;
    private static final int CHEST_ITEMS_PER_ADDITIONAL = 20;
    // Within a cluster: chests are packed tight (1-block gap between adjacent chests
    // both along primary axis and between lanes). Between parent clusters sharing a
    // cardinal: 5 spacer rows × 2 blocks = 10 blocks of empty space before the next
    // parent's first chest. Different cardinals are already ≥14 blocks apart because
    // of the 10-block base offset.
    private static final int CHEST_CLUSTER_BASE_OFFSET = 10;
    private static final int CHEST_CLUSTER_STEP = 2;
    private static final int CHEST_CLUSTER_LANE_OFFSET = 1;
    private static final int CHEST_CLUSTER_PARENT_SPACER_ROWS = 5;

    private enum Cardinal {
        WEST(-1, 0, true),
        NORTH(0, -1, false),
        EAST(1, 0, true),
        SOUTH(0, 1, false);

        final int dx;
        final int dz;
        final boolean primaryIsX;

        Cardinal(int dx, int dz, boolean primaryIsX) {
            this.dx = dx;
            this.dz = dz;
            this.primaryIsX = primaryIsX;
        }
    }

    private static final Cardinal[] CARDINAL_BY_COLUMN = {
            Cardinal.WEST, Cardinal.NORTH, Cardinal.EAST, Cardinal.SOUTH
    };

    private RealisticAtlasGenerator() {
    }

    public static RealisticAtlasPlan generate(
            List<ItemStack> pool,
            PopulateProfile profile,
            Random random,
            Function<ItemStack, SemanticBucket> classifier
    ) {
        Objects.requireNonNull(pool, "pool");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(classifier, "classifier");
        if (pool.isEmpty()) {
            return new RealisticAtlasPlan(List.of(), Map.of(), List.of(), List.of(), List.of());
        }

        List<ItemStack> deduped = dedupeByIdentity(pool);
        Collections.shuffle(deduped, random);

        int homedTarget = Math.min(profile.identityCount(), deduped.size());
        int triageTarget = Math.min(
                deduped.size() - homedTarget,
                (int) Math.round(homedTarget * profile.triageFraction())
        );
        triageTarget = Math.max(0, triageTarget);

        List<ItemStack> homedTemplates = new ArrayList<>(deduped.subList(0, homedTarget));
        List<ItemStack> triageTemplates = new ArrayList<>(
                deduped.subList(homedTarget, Math.min(homedTarget + triageTarget, deduped.size()))
        );

        LinkedHashMap<String, CategoryGroup> grouped = new LinkedHashMap<>();
        for (ItemStack stack : homedTemplates) {
            SemanticBucket resolvedParent = classifier.apply(stack);
            final SemanticBucket parent = resolvedParent == null ? SemanticBucket.MISC : resolvedParent;
            SubBucketRule sub = SubBucketResolver.resolve(stack, parent);
            String key = sub != null ? sub.subId() : parent.id();
            final String label = sub != null ? sub.label() : parent.label();
            final String islandId = sub != null ? sub.islandId() : SYNTHETIC_ISLAND_ID_PREFIX + parent.id();
            final int subPriority = sub != null ? sub.priority() : -1;
            grouped.computeIfAbsent(key, k -> new CategoryGroup(islandId, label, parent, subPriority))
                    .stacks().add(stack);
        }

        List<IslandBuild> builds = planIslands(grouped);
        layoutIslands(builds);

        ArrayList<VisualAtlasIsland> islands = new ArrayList<>(builds.size());
        LinkedHashMap<ItemIdentity, VisualHomeAssignment> assignments = new LinkedHashMap<>();
        for (IslandBuild build : builds) {
            islands.add(build.island());
            for (VisualHomeAssignment assignment : build.assignments()) {
                assignments.put(assignment.identity(), assignment);
            }
        }

        // Homed stacks cap: a real player carries a subset of homed identities, not
        // every one. Pick `carriedIdentityCap` identities across all islands uniformly
        // at random and materialize those into stacks; the rest stay as pure-ghost
        // assignments on the atlas.
        ArrayList<BucketedStack> carriedPool = new ArrayList<>();
        for (IslandBuild build : builds) {
            for (ItemStack stack : build.stacks()) {
                carriedPool.add(new BucketedStack(build.parentBucket(), stack));
            }
        }
        Collections.shuffle(carriedPool, random);
        int carriedCap = Math.min(carriedPool.size(), profile.carriedIdentityCap());
        ArrayList<ItemStack> homedStacks = new ArrayList<>(carriedCap);
        for (int i = 0; i < carriedCap; i++) {
            BucketedStack bs = carriedPool.get(i);
            ItemStack copy = bs.stack().copy();
            copy.setCount(Math.max(1, rollStackCount(copy, bs.bucket(), random)));
            homedStacks.add(copy);
        }

        List<ChestSpec> chests = planChests(builds, profile.chestCount(), random);
        List<ItemStack> triageStacks = rollTriageStacks(triageTemplates, classifier, random);

        return new RealisticAtlasPlan(islands, assignments, triageStacks, chests, homedStacks);
    }

    private static List<ItemStack> rollTriageStacks(
            List<ItemStack> templates,
            Function<ItemStack, SemanticBucket> classifier,
            Random random
    ) {
        ArrayList<ItemStack> out = new ArrayList<>(templates.size());
        for (ItemStack template : templates) {
            if (template == null || template.isEmpty()) {
                continue;
            }
            ItemStack copy = template.copy();
            SemanticBucket bucket = classifier.apply(copy);
            if (bucket == null) {
                bucket = SemanticBucket.MISC;
            }
            copy.setCount(Math.max(1, rollStackCount(copy, bucket, random)));
            out.add(copy);
        }
        return out;
    }

    private static List<ItemStack> dedupeByIdentity(List<ItemStack> pool) {
        LinkedHashMap<ItemIdentity, ItemStack> byIdentity = new LinkedHashMap<>(pool.size());
        for (ItemStack stack : pool) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            ItemIdentity identity = ItemIdentityMatcher.create(stack);
            byIdentity.putIfAbsent(identity, stack);
        }
        return new ArrayList<>(byIdentity.values());
    }

    private static List<IslandBuild> planIslands(LinkedHashMap<String, CategoryGroup> grouped) {
        ArrayList<IslandBuild> builds = new ArrayList<>();
        for (CategoryGroup group : grouped.values()) {
            List<ItemStack> stacks = group.stacks();
            if (stacks.isEmpty()) {
                continue;
            }
            int itemCount = stacks.size();
            int cardsPerRow = dynamicCardsPerRow(itemCount);
            int rows = Math.max(1, (itemCount + cardsPerRow - 1) / cardsPerRow);
            int width = ISLAND_PADDING_X * 2
                    + cardsPerRow * CARD_WIDTH
                    + Math.max(0, cardsPerRow - 1) * CARD_GAP;
            int height = ISLAND_CONTENT_TOP
                    + rows * CARD_HEIGHT
                    + Math.max(0, rows - 1) * CARD_GAP
                    + ISLAND_PADDING_Y;

            String islandId = group.islandId();
            ItemIdentity iconIdentity = ItemIdentityMatcher.create(stacks.get(0));

            ArrayList<VisualHomeAssignment> assignments = new ArrayList<>(stacks.size());
            for (int index = 0; index < stacks.size(); index++) {
                int col = index % cardsPerRow;
                int row = index / cardsPerRow;
                int localX = ISLAND_PADDING_X + col * (CARD_WIDTH + CARD_GAP);
                int localY = ISLAND_CONTENT_TOP + row * (CARD_HEIGHT + CARD_GAP);
                ItemIdentity identity = ItemIdentityMatcher.create(stacks.get(index));
                assignments.add(new VisualHomeAssignment(
                        identity,
                        islandId,
                        localX,
                        localY,
                        VisualHomeOrigin.PLAYER_PLACED,
                        true
                ));
            }

            VisualAtlasIsland island = new VisualAtlasIsland(
                    islandId,
                    group.label(),
                    VisualAtlasIslandKind.PLAYER,
                    0,
                    0,
                    width,
                    height,
                    group.parent().color(),
                    iconIdentity
            );
            builds.add(new IslandBuild(group.parent(), group.subPriority(), island, assignments, new ArrayList<>(stacks)));
        }
        return builds;
    }

    private static int dynamicCardsPerRow(int itemCount) {
        int estimate = (int) Math.round(Math.sqrt(itemCount * 1.5));
        return Math.min(MAX_CARDS_PER_ROW, Math.max(MIN_CARDS_PER_ROW, estimate));
    }

    private static void layoutIslands(List<IslandBuild> builds) {
        // Group by parent cluster row; within a row, sort by parent.clusterColumn, then
        // by sub-priority (parent-default first at priority=-1 goes to end, so subs
        // appear before default — but sort high->low so higher-priority subs come first).
        // We want: parent default last (or first?). Let me put default first, then subs
        // in priority order. That way the broad parent label anchors each cluster.
        builds.sort(Comparator
                .comparingInt((IslandBuild b) -> b.parentBucket().clusterRow())
                .thenComparingInt(b -> b.parentBucket().clusterColumn())
                .thenComparingInt(b -> -b.subPriority()));

        int currentY = ATLAS_ORIGIN_Y;
        int currentRow = -1;
        int currentX = ATLAS_ORIGIN_X;
        int rowMaxHeight = 0;
        int lastParentColumn = -1;
        SemanticBucket lastParentBucket = null;

        for (int index = 0; index < builds.size(); index++) {
            IslandBuild build = builds.get(index);
            int row = build.parentBucket().clusterRow();
            if (row != currentRow) {
                if (currentRow != -1) {
                    currentY += rowMaxHeight + ISLAND_GAP;
                }
                currentRow = row;
                currentX = ATLAS_ORIGIN_X;
                rowMaxHeight = 0;
                lastParentColumn = -1;
                lastParentBucket = null;
            }

            int parentColumn = build.parentBucket().clusterColumn();
            if (lastParentColumn != -1 && parentColumn != lastParentColumn) {
                currentX += ISLAND_PARENT_GROUP_GAP;
            } else if (lastParentColumn != -1) {
                currentX += ISLAND_GAP;
            }
            lastParentColumn = parentColumn;
            lastParentBucket = build.parentBucket();

            VisualAtlasIsland old = build.island();
            VisualAtlasIsland placed = new VisualAtlasIsland(
                    old.id(),
                    old.label(),
                    old.kind(),
                    currentX,
                    currentY,
                    old.width(),
                    old.height(),
                    old.color(),
                    old.iconIdentity()
            );
            builds.set(index, new IslandBuild(
                    build.parentBucket(),
                    build.subPriority(),
                    placed,
                    build.assignments(),
                    build.stacks()
            ));
            currentX += placed.width();
            rowMaxHeight = Math.max(rowMaxHeight, placed.height());
        }
    }

    private static List<ChestSpec> planChests(
            List<IslandBuild> builds,
            int chestCount,
            Random random
    ) {
        if (chestCount <= 0 || builds.isEmpty()) {
            return List.of();
        }

        ArrayList<BucketedStack> allHomedStacks = new ArrayList<>();
        for (IslandBuild build : builds) {
            for (ItemStack stack : build.stacks()) {
                allHomedStacks.add(new BucketedStack(build.parentBucket(), stack));
            }
        }
        if (allHomedStacks.isEmpty()) {
            return List.of();
        }

        // Allocate chests per island: at least 1, scaled by item count, capped.
        // 10% of chestCount reserved as unlinked overflow.
        int unlinkedBudget = Math.min(chestCount, Math.max(1, (int) Math.round(chestCount * CHEST_UNLINKED_FRACTION)));
        int linkedBudget = Math.max(0, chestCount - unlinkedBudget);
        int[] alloc = allocateChestsPerBucket(builds, linkedBudget, random);
        unlinkedBudget = chestCount - sum(alloc);

        ArrayList<ChestSpec> chests = new ArrayList<>(chestCount);
        int chestIndex = 0;

        // Group linked-chest assignments by parent bucket, then place in cardinal clusters.
        // Each parent bucket is assigned a cardinal direction based on clusterColumn; sub-
        // buckets inherit their parent's direction so related islands cluster together in
        // the world.
        LinkedHashMap<SemanticBucket, List<IslandAllocation>> byParent = new LinkedHashMap<>();
        for (int index = 0; index < builds.size(); index++) {
            if (alloc[index] <= 0) {
                continue;
            }
            IslandBuild build = builds.get(index);
            byParent.computeIfAbsent(build.parentBucket(), parent -> new ArrayList<>())
                    .add(new IslandAllocation(build, alloc[index]));
        }

        // Within a cardinal direction we may have multiple parent buckets sharing it; lay
        // them out back-to-back along the primary axis, with a skip row between parents.
        EnumMap<Cardinal, Integer> cardinalDepth = new EnumMap<>(Cardinal.class);
        for (Cardinal c : Cardinal.values()) {
            cardinalDepth.put(c, 0);
        }

        for (Map.Entry<SemanticBucket, List<IslandAllocation>> entry : byParent.entrySet()) {
            SemanticBucket parent = entry.getKey();
            Cardinal cardinal = CARDINAL_BY_COLUMN[parent.clusterColumn() % CARDINAL_BY_COLUMN.length];

            int perParentLocal = 0;
            for (IslandAllocation allocation : entry.getValue()) {
                int startingDepth = cardinalDepth.get(cardinal);
                for (int n = 0; n < allocation.count(); n++) {
                    int localSlot = perParentLocal + n;
                    int[] offset = cardinalOffset(cardinal, startingDepth, localSlot);
                    chests.add(buildChestSpec(
                            chestIndex++,
                            allocation.build(),
                            offset[0], offset[1],
                            allHomedStacks, random
                    ));
                }
                perParentLocal += allocation.count();
            }
            // Advance cardinal depth past this parent's cluster; add spacer rows between
            // parents sharing the same cardinal so they don't visually run together.
            int parentDepthRows = (perParentLocal + 1) / 2;
            cardinalDepth.merge(cardinal, parentDepthRows + CHEST_CLUSTER_PARENT_SPACER_ROWS, Integer::sum);
        }

        // Unlinked chests land in a small pile behind the player (NORTH, past any linked
        // clusters that claimed NORTH).
        int unlinkedStartDepth = cardinalDepth.get(Cardinal.NORTH);
        for (int u = 0; u < unlinkedBudget; u++) {
            int[] offset = cardinalOffset(Cardinal.NORTH, unlinkedStartDepth, u);
            chests.add(buildChestSpec(
                    chestIndex++,
                    null,
                    offset[0], offset[1],
                    allHomedStacks, random
            ));
        }

        return chests;
    }

    private static int[] allocateChestsPerBucket(
            List<IslandBuild> builds,
            int linkedBudget,
            Random random
    ) {
        int[] alloc = new int[builds.size()];
        if (linkedBudget <= 0 || builds.isEmpty()) {
            return alloc;
        }

        double[] weights = new double[builds.size()];
        double totalWeight = 0.0;
        int[] caps = new int[builds.size()];
        for (int index = 0; index < builds.size(); index++) {
            int count = builds.get(index).assignments().size();
            weights[index] = Math.sqrt(Math.max(1, count));
            totalWeight += weights[index];
            caps[index] = Math.min(CHEST_MAX_PER_BUCKET,
                    Math.max(1, (int) Math.ceil(count / (double) CHEST_ITEMS_PER_ADDITIONAL)));
        }

        // Baseline: ensure every linked bucket gets at least 1 chest if budget allows.
        int allocated = 0;
        for (int index = 0; index < builds.size() && allocated < linkedBudget; index++) {
            alloc[index] = 1;
            allocated++;
        }

        // Remaining budget: weighted sampling with per-bucket cap.
        int failsafe = Math.max(1, linkedBudget * 4);
        while (allocated < linkedBudget && failsafe-- > 0) {
            int pick = weightedIndex(weights, totalWeight, random);
            if (alloc[pick] < caps[pick]) {
                alloc[pick]++;
                allocated++;
            } else if (allCapsHit(alloc, caps)) {
                break;
            }
        }
        return alloc;
    }

    private static boolean allCapsHit(int[] alloc, int[] caps) {
        for (int i = 0; i < alloc.length; i++) {
            if (alloc[i] < caps[i]) {
                return false;
            }
        }
        return true;
    }

    private static int weightedIndex(double[] weights, double totalWeight, Random random) {
        if (totalWeight <= 0.0) {
            return 0;
        }
        double target = random.nextDouble() * totalWeight;
        double cumulative = 0.0;
        for (int index = 0; index < weights.length; index++) {
            cumulative += weights[index];
            if (target <= cumulative) {
                return index;
            }
        }
        return weights.length - 1;
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int value : values) {
            total += value;
        }
        return total;
    }

    private static int[] cardinalOffset(Cardinal cardinal, int startingDepth, int localSlot) {
        int depth = startingDepth + localSlot / 2;
        int lane = (localSlot % 2 == 0) ? -1 : 1;
        int primary = CHEST_CLUSTER_BASE_OFFSET + depth * CHEST_CLUSTER_STEP;
        int secondary = lane * CHEST_CLUSTER_LANE_OFFSET;
        int dx;
        int dz;
        if (cardinal.primaryIsX) {
            dx = cardinal.dx * primary;
            dz = secondary;
        } else {
            dx = secondary;
            dz = cardinal.dz * primary;
        }
        return new int[]{dx, dz};
    }

    private static ChestSpec buildChestSpec(
            int index,
            IslandBuild linked,
            int deltaX,
            int deltaZ,
            List<BucketedStack> allHomedStacks,
            Random random
    ) {
        SemanticBucket linkedBucket = linked != null ? linked.parentBucket() : null;
        List<ItemStack> linkedPool = linked != null ? linked.stacks() : List.of();

        int slotCount = CHEST_FILL_MIN_SLOTS
                + random.nextInt(CHEST_FILL_MAX_SLOTS - CHEST_FILL_MIN_SLOTS + 1);
        slotCount = Math.min(slotCount, CHEST_SLOTS);

        ArrayList<Integer> slotIndices = new ArrayList<>(CHEST_SLOTS);
        for (int slot = 0; slot < CHEST_SLOTS; slot++) {
            slotIndices.add(slot);
        }
        Collections.shuffle(slotIndices, random);

        ArrayList<ChestContentEntry> contents = new ArrayList<>(slotCount);
        for (int fillIndex = 0; fillIndex < slotCount; fillIndex++) {
            boolean useLinked = !linkedPool.isEmpty()
                    && random.nextDouble() < CHEST_BUCKET_BIAS;
            ItemStack template;
            SemanticBucket stackBucket;
            if (useLinked) {
                template = linkedPool.get(random.nextInt(linkedPool.size()));
                stackBucket = linkedBucket;
            } else if (!allHomedStacks.isEmpty()) {
                BucketedStack picked = allHomedStacks.get(random.nextInt(allHomedStacks.size()));
                template = picked.stack();
                stackBucket = picked.bucket();
            } else {
                continue;
            }
            if (template == null || template.isEmpty()) {
                continue;
            }
            ItemStack stack = template.copy();
            int count = rollStackCount(stack, stackBucket, random);
            stack.setCount(Math.max(1, count));
            contents.add(new ChestContentEntry(slotIndices.get(fillIndex), stack));
        }

        String linkedIslandId = linked != null ? linked.island().id() : "";
        return new ChestSpec(index, linkedIslandId, contents, deltaX, deltaZ);
    }

    private record IslandAllocation(IslandBuild build, int count) {
    }

    private static int rollStackCount(ItemStack stack, SemanticBucket bucket, Random random) {
        int max;
        try {
            max = Math.max(1, stack.getMaxStackSize());
        } catch (RuntimeException | LinkageError ignored) {
            max = 64;
        }
        if (max <= 1) {
            return 1;
        }
        return switch (bucket) {
            case TOOLS, COMBAT, ARMOR, WORKBENCHES, UPGRADES -> 1;
            case MATERIALS, BUILDING, NATURAL, MECHANISMS -> {
                int floor = Math.min(16, max);
                int span = Math.max(1, max - floor + 1);
                yield floor + random.nextInt(span);
            }
            case FOOD -> {
                int floor = Math.min(4, max);
                int ceiling = Math.min(max, 32);
                int span = Math.max(1, ceiling - floor + 1);
                yield floor + random.nextInt(span);
            }
            case DECORATION, REDSTONE, STORAGE -> {
                int ceiling = Math.min(max, 16);
                yield 1 + random.nextInt(ceiling);
            }
            case MISC -> {
                int ceiling = Math.min(max, 8);
                yield 1 + random.nextInt(ceiling);
            }
        };
    }

    private record BucketedStack(SemanticBucket bucket, ItemStack stack) {
    }

    private static final class CategoryGroup {
        private final String islandId;
        private final String label;
        private final SemanticBucket parent;
        private final int subPriority;
        private final ArrayList<ItemStack> stacks;

        private CategoryGroup(String islandId, String label, SemanticBucket parent, int subPriority) {
            this.islandId = islandId;
            this.label = label;
            this.parent = parent;
            this.subPriority = subPriority;
            this.stacks = new ArrayList<>();
        }

        String islandId() {
            return islandId;
        }

        String label() {
            return label;
        }

        SemanticBucket parent() {
            return parent;
        }

        int subPriority() {
            return subPriority;
        }

        ArrayList<ItemStack> stacks() {
            return stacks;
        }
    }

    private static final class IslandBuild {
        private final SemanticBucket parentBucket;
        private final int subPriority;
        private final VisualAtlasIsland island;
        private final List<VisualHomeAssignment> assignments;
        private final List<ItemStack> stacks;

        private IslandBuild(
                SemanticBucket parentBucket,
                int subPriority,
                VisualAtlasIsland island,
                List<VisualHomeAssignment> assignments,
                List<ItemStack> stacks
        ) {
            this.parentBucket = parentBucket;
            this.subPriority = subPriority;
            this.island = island;
            this.assignments = assignments;
            this.stacks = stacks;
        }

        SemanticBucket parentBucket() {
            return parentBucket;
        }

        int subPriority() {
            return subPriority;
        }

        VisualAtlasIsland island() {
            return island;
        }

        List<VisualHomeAssignment> assignments() {
            return assignments;
        }

        List<ItemStack> stacks() {
            return stacks;
        }
    }
}
