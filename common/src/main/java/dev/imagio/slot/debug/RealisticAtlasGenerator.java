package dev.imagio.slot.debug;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.triage.IslandTemplateMatch;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

public final class RealisticAtlasGenerator {

    /**
     * Minimum subsystem-tagged items required for a subsystem to qualify
     * as its own island. Subsystems with fewer items fold back into the
     * parent template — otherwise the atlas fragments into small
     * subsystem islands that just add navigation overhead. Playtest of
     * the late-modpack profile showed 4 was too aggressive (every minor
     * subsystem-of-a-subsystem split into its own island); 10 keeps only
     * the genuine "machinery area" and "logistics network" splits and
     * lets smaller groups collapse into the parent template's pile.
     */
    public static final int DEFAULT_MIN_SUBSYSTEM_ITEMS = 10;

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

    /**
     * Storage-area split assigned to populate-generated chests. Tests
     * exercise multiple areas in the storage panel by mapping each
     * cardinal direction to an area label; cardinals 0/2 (WEST/EAST)
     * land in "Mountain Mine" and 1/3 (NORTH/SOUTH) land in "Main
     * Base", so even small chest budgets populate at least two
     * tabs. Unlinked overflow inherits "Main Base" since it stacks
     * behind the player (NORTH).
     */
    static final String CHEST_AREA_MAIN = "Main Base";
    static final String CHEST_AREA_MOUNTAIN = "Mountain Mine";
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

    /**
     * Backward-compat overload used by tests that mock classification via a
     * simple stack → template function. Wraps every result as a non-subsystem
     * {@link IslandTemplateMatch}.
     */
    public static RealisticAtlasPlan generate(
            List<ItemStack> pool,
            PopulateProfile profile,
            Random random,
            Function<ItemStack, IslandSuggestionTemplate> classifier
    ) {
        Objects.requireNonNull(classifier, "classifier");
        Function<ItemStack, IslandSignalDescriptor> descriptorFn =
                stack -> descriptorForTemplate(stack, classifier.apply(stack));
        return generateWithDescriptors(pool, profile, random, descriptorFn, DEFAULT_MIN_SUBSYSTEM_ITEMS);
    }

    /**
     * Production overload: classify each stack via its full
     * {@link IslandSignalDescriptor}, allowing subsystem-primary matching,
     * frequency-driven ordering, and trophy-shunt logic to fire.
     */
    public static RealisticAtlasPlan generateWithDescriptors(
            List<ItemStack> pool,
            PopulateProfile profile,
            Random random,
            Function<ItemStack, IslandSignalDescriptor> descriptorFn
    ) {
        return generateWithDescriptors(pool, profile, random, descriptorFn, DEFAULT_MIN_SUBSYSTEM_ITEMS);
    }

    public static RealisticAtlasPlan generateWithDescriptors(
            List<ItemStack> pool,
            PopulateProfile profile,
            Random random,
            Function<ItemStack, IslandSignalDescriptor> descriptorFn,
            int minSubsystemItems
    ) {
        Objects.requireNonNull(pool, "pool");
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(descriptorFn, "descriptorFn");
        if (pool.isEmpty()) {
            return new RealisticAtlasPlan(List.of(), Map.of(), List.of(), List.of(), List.of());
        }

        List<ItemStack> deduped = dedupeByIdentity(pool);

        int homedTarget = Math.min(profile.identityCount(), deduped.size());
        int triageTarget = Math.min(
                deduped.size() - homedTarget,
                (int) Math.round(homedTarget * profile.triageFraction())
        );
        triageTarget = Math.max(0, triageTarget);

        // Weight the homed-pool selection itself by frequency so the
        // synthesized atlas fills with the items a real player would
        // have homed (cobblestone, sticks, planks, ingots) rather than
        // a uniform-random sweep that pulls in chiseled niche stairs
        // and one-off display blocks. Triage gets the leftovers,
        // shuffled uniformly so it captures the "random new pickups"
        // feel rather than a sorted everyday-first list.
        ArrayList<Double> homedWeights = new ArrayList<>(deduped.size());
        for (ItemStack stack : deduped) {
            IslandSignalDescriptor descriptor = descriptorFn.apply(stack);
            String freq = descriptor == null ? null : descriptor.carryFrequency();
            homedWeights.add(carryFrequencyWeight(freq));
        }
        List<ItemStack> homedTemplates = weightedSampleWithoutReplacement(
                deduped, homedWeights, homedTarget, random);
        java.util.HashSet<ItemIdentity> homedIds = new java.util.HashSet<>();
        for (ItemStack stack : homedTemplates) {
            homedIds.add(ItemIdentityMatcher.create(stack));
        }
        ArrayList<ItemStack> triageCandidates = new ArrayList<>();
        for (ItemStack stack : deduped) {
            if (!homedIds.contains(ItemIdentityMatcher.create(stack))) {
                triageCandidates.add(stack);
            }
        }
        Collections.shuffle(triageCandidates, random);
        List<ItemStack> triageTemplates = new ArrayList<>(
                triageCandidates.subList(0, Math.min(triageTarget, triageCandidates.size()))
        );

        // First pass: extract descriptors for every homed stack; histogram subsystem ids
        // so we can decide which subsystems qualify as their own islands. Subsystems with
        // fewer than `minSubsystemItems` items collapse back to their parent template.
        ArrayList<DescribedStack> described = new ArrayList<>(homedTemplates.size());
        HashMap<String, Integer> subsystemHistogram = new HashMap<>();
        for (ItemStack stack : homedTemplates) {
            IslandSignalDescriptor descriptor = descriptorFn.apply(stack);
            if (descriptor == null) {
                descriptor = IslandSignalDescriptor.empty(ItemIdentityMatcher.create(stack));
            }
            described.add(new DescribedStack(stack, descriptor));
            // Trophies bypass subsystem grouping by design — they belong on
            // display, not filed under their mod's subsystem.
            if (IslandSuggestionTemplate.isTrophy(descriptor)) {
                continue;
            }
            for (String subsystemId : descriptor.subsystems()) {
                if (subsystemId == null || subsystemId.isBlank()) {
                    continue;
                }
                subsystemHistogram.merge(subsystemId, 1, Integer::sum);
            }
        }
        int threshold = Math.max(1, minSubsystemItems);
        java.util.function.Predicate<String> subsystemQualifier = id -> {
            Integer count = subsystemHistogram.get(id);
            return count != null && count >= threshold;
        };

        // Second pass: classify each stack via firstMatchExtended.
        LinkedHashMap<String, CategoryGroup> grouped = new LinkedHashMap<>();
        for (DescribedStack ds : described) {
            IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                    ds.descriptor, subsystemQualifier);
            grouped.computeIfAbsent(match.islandId(), id -> new CategoryGroup(match))
                    .stacks().add(ds);
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

        // Homed stacks cap: a real player carries a subset of homed
        // identities, not every one. Bias the pick toward high-frequency
        // identities so the synthesized inventory looks like what a
        // player would actually carry — not a uniform-random sample of
        // every homed thing including chiseled niche stairs and
        // single-use display blocks.
        ArrayList<TemplatedStack> carriedPool = new ArrayList<>();
        ArrayList<Double> carryWeights = new ArrayList<>();
        for (IslandBuild build : builds) {
            for (DescribedStack ds : build.stacks()) {
                carriedPool.add(new TemplatedStack(build.match().parentTemplate(), ds.stack));
                carryWeights.add(carryFrequencyWeight(ds.descriptor.carryFrequency()));
            }
        }
        int carriedCap = Math.min(carriedPool.size(), profile.carriedIdentityCap());
        List<TemplatedStack> picked = weightedSampleWithoutReplacement(
                carriedPool, carryWeights, carriedCap, random);
        ArrayList<ItemStack> homedStacks = new ArrayList<>(picked.size());
        for (TemplatedStack ts : picked) {
            ItemStack copy = ts.stack().copy();
            copy.setCount(Math.max(1, rollStackCount(copy, ts.template(), random)));
            homedStacks.add(copy);
        }

        List<ChestSpec> chests = planChests(builds, profile.chestCount(), random);
        List<ItemStack> triageStacks = rollTriageStacks(triageTemplates, descriptorFn, subsystemQualifier, random);

        return new RealisticAtlasPlan(islands, assignments, triageStacks, chests, homedStacks);
    }

    private static IslandSignalDescriptor descriptorForTemplate(ItemStack stack, IslandSuggestionTemplate template) {
        // Synthesize a minimal descriptor that fires the supplied template
        // via its first role / tag / form trigger. Backward-compat shim
        // for tests; the production path passes real descriptors directly.
        ItemIdentity identity = stack == null || stack.isEmpty()
                ? ItemIdentity.of("minecraft:air")
                : ItemIdentityMatcher.create(stack);
        IslandSuggestionTemplate t = template == null ? IslandSuggestionTemplate.MISC : template;
        String role = roleTriggerFor(t);
        java.util.Set<String> tags = tagTriggerFor(t);
        String form = formTriggerFor(t);
        return new IslandSignalDescriptor(
                identity,
                java.util.Set.of(),
                tags,
                namespaceOf(identity.itemId()),
                "",
                role,
                null,
                null,
                java.util.List.of(),
                java.util.List.of(),
                null, null, null, null, null,
                form,
                template == IslandSuggestionTemplate.LIGHTING
        );
    }

    private static String roleTriggerFor(IslandSuggestionTemplate template) {
        // Returns a role value the template's matches() would accept,
        // or null when the template is form-keyed or tag-keyed only.
        return switch (template) {
            case FOOD -> "consumable";
            case TOOLS -> "tool";
            case WEAPONS -> "weapon";
            case ARMOR -> "armor";
            case LIGHTING -> null;
            case INGOTS, GEMS, RAW_MATERIALS -> null;
            case MATERIALS -> "material";
            case STORAGE -> "storage_block";
            case STAIRS, SLABS, WALLS, DOORS, FENCES, WINDOWS -> null;
            case BUILDING -> "building_block";
            case DECORATION -> "decorative_block";
            case NATURAL -> "natural_resource";
            case WORKBENCHES -> "functional_block";
            case MECHANISMS -> "mechanism";
            case REDSTONE -> "redstone_component";
            case UPGRADES -> "upgrade";
            case TRANSPORT -> "transport";
            case UTILITY -> "utility";
            case CURIOSITY -> "curiosity";
            case MISC -> "admin";
        };
    }

    private static java.util.Set<String> tagTriggerFor(IslandSuggestionTemplate template) {
        return switch (template) {
            case INGOTS -> java.util.Set.of("c:ingots");
            case GEMS -> java.util.Set.of("c:gems");
            case RAW_MATERIALS -> java.util.Set.of("c:raw_materials");
            default -> java.util.Set.of();
        };
    }

    private static String formTriggerFor(IslandSuggestionTemplate template) {
        return switch (template) {
            case STAIRS -> "stairs";
            case SLABS -> "slab";
            case WALLS -> "wall";
            case DOORS -> "door";
            case FENCES -> "fence";
            case WINDOWS -> "pane";
            default -> null;
        };
    }

    private static String namespaceOf(String itemId) {
        if (itemId == null) {
            return "";
        }
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }

    private static List<ItemStack> rollTriageStacks(
            List<ItemStack> templates,
            Function<ItemStack, IslandSignalDescriptor> descriptorFn,
            java.util.function.Predicate<String> subsystemQualifier,
            Random random
    ) {
        ArrayList<ItemStack> out = new ArrayList<>(templates.size());
        for (ItemStack template : templates) {
            if (template == null || template.isEmpty()) {
                continue;
            }
            ItemStack copy = template.copy();
            IslandSignalDescriptor descriptor = descriptorFn.apply(copy);
            if (descriptor == null) {
                descriptor = IslandSignalDescriptor.empty(ItemIdentityMatcher.create(copy));
            }
            IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                    descriptor, subsystemQualifier);
            copy.setCount(Math.max(1, rollStackCount(copy, match.parentTemplate(), random)));
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

    private static List<IslandBuild> planIslands(Map<String, CategoryGroup> grouped) {
        ArrayList<IslandBuild> builds = new ArrayList<>();
        for (CategoryGroup group : grouped.values()) {
            List<DescribedStack> stacks = group.stacks();
            if (stacks.isEmpty()) {
                continue;
            }
            // Phase 4 + 5 sort: within an island, group items that share a
            // material_family / flavor (so wood_oak items end up adjacent),
            // and within those groups push frequent items toward the top
            // and rare items toward the bottom. The comparator is stable
            // so identical-key items keep arrival order.
            stacks = new ArrayList<>(stacks);
            stacks.sort(WITHIN_ISLAND_COMPARATOR);

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

            String islandId = group.match().islandId();
            ItemIdentity iconIdentity = ItemIdentityMatcher.create(stacks.get(0).stack());

            ArrayList<VisualHomeAssignment> assignments = new ArrayList<>(stacks.size());
            ArrayList<DescribedStack> orderedStacks = new ArrayList<>(stacks.size());
            for (int index = 0; index < stacks.size(); index++) {
                DescribedStack ds = stacks.get(index);
                ItemIdentity identity = ItemIdentityMatcher.create(ds.stack());
                assignments.add(new VisualHomeAssignment(
                        identity,
                        islandId,
                        index,
                        VisualHomeOrigin.PLAYER_PLACED,
                        true
                ));
                orderedStacks.add(ds);
            }

            VisualAtlasIsland island = new VisualAtlasIsland(
                    islandId,
                    group.match().label(),
                    VisualAtlasIslandKind.PLAYER,
                    0,
                    0,
                    group.match().color(),
                    iconIdentity
            );
            builds.add(new IslandBuild(
                    group.match(),
                    island,
                    width,
                    height,
                    assignments,
                    orderedStacks
            ));
        }
        return builds;
    }

    private static int dynamicCardsPerRow(int itemCount) {
        int estimate = (int) Math.round(Math.sqrt(itemCount * 1.5));
        return Math.min(MAX_CARDS_PER_ROW, Math.max(MIN_CARDS_PER_ROW, estimate));
    }

    private static void layoutIslands(List<IslandBuild> builds) {
        // Group by template cluster row; within a row, sort by clusterColumn.
        // This gives populate-generated atlases a deterministic spatial
        // layout that mirrors the template enum's intended grouping
        // (player-gear row, raw/build row, machinery row, extras row).
        // Subsystem islands inherit cluster row/column from their parent
        // template, so all Create-mechanism subsystems still land in the
        // mechanisms row and sort by subsystem id within that row.
        builds.sort(Comparator
                .comparingInt((IslandBuild b) -> b.match().clusterRow())
                .thenComparingInt(b -> b.match().clusterColumn())
                .thenComparingInt(b -> b.match().isSubsystem() ? 1 : 0)
                .thenComparing(b -> b.island().id()));

        // Square-ish wrap target: islands flow horizontally inside a band
        // sized to roughly sqrt(total area) so a populate run with many
        // mechanism subsystems doesn't strew them in a single 3000-wide
        // strip. Sub-rows within a cluster row stack tightly; different
        // cluster rows still get the full ISLAND_GAP between them so the
        // semantic grouping reads.
        long totalArea = 0L;
        for (IslandBuild b : builds) {
            totalArea += (long) b.predictedWidth() * (long) b.predictedHeight();
        }
        int targetWidth = Math.max(800,
                (int) Math.round(Math.sqrt(Math.max(1L, totalArea)) * 1.4));

        int currentY = ATLAS_ORIGIN_Y;
        int currentRow = -1;
        int currentX = ATLAS_ORIGIN_X;
        int rowMaxHeight = 0;
        int lastColumn = -1;
        int subRowGap = ISLAND_GAP;

        for (int index = 0; index < builds.size(); index++) {
            IslandBuild build = builds.get(index);
            int row = build.match().clusterRow();
            int column = build.match().clusterColumn();
            boolean newClusterRow = row != currentRow;
            if (newClusterRow) {
                if (currentRow != -1) {
                    currentY += rowMaxHeight + ISLAND_GAP;
                }
                currentRow = row;
                currentX = ATLAS_ORIGIN_X;
                rowMaxHeight = 0;
                lastColumn = -1;
            } else {
                int gap = column != lastColumn ? ISLAND_PARENT_GROUP_GAP : ISLAND_GAP;
                int prospective = currentX + gap + build.predictedWidth();
                if (prospective > ATLAS_ORIGIN_X + targetWidth && currentX > ATLAS_ORIGIN_X) {
                    // Wrap to a sub-row within the same cluster row.
                    currentY += rowMaxHeight + subRowGap;
                    currentX = ATLAS_ORIGIN_X;
                    rowMaxHeight = 0;
                    lastColumn = -1;
                }
            }

            if (lastColumn != -1) {
                currentX += column != lastColumn ? ISLAND_PARENT_GROUP_GAP : ISLAND_GAP;
            }
            lastColumn = column;

            VisualAtlasIsland old = build.island();
            VisualAtlasIsland placed = new VisualAtlasIsland(
                    old.id(),
                    old.label(),
                    old.kind(),
                    currentX,
                    currentY,
                    old.color(),
                    old.iconIdentity()
            );
            builds.set(index, new IslandBuild(
                    build.match(),
                    placed,
                    build.predictedWidth(),
                    build.predictedHeight(),
                    build.assignments(),
                    build.stacks()
            ));
            currentX += build.predictedWidth();
            rowMaxHeight = Math.max(rowMaxHeight, build.predictedHeight());
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

        ArrayList<TemplatedStack> allHomedStacks = new ArrayList<>();
        for (IslandBuild build : builds) {
            for (DescribedStack ds : build.stacks()) {
                allHomedStacks.add(new TemplatedStack(build.match().parentTemplate(), ds.stack()));
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

        // Group linked-chest assignments by template cluster column, then place
        // each cluster in a cardinal direction. Templates sharing a column lay
        // out back-to-back along the primary axis with a spacer between groups.
        LinkedHashMap<Integer, List<IslandAllocation>> byColumn = new LinkedHashMap<>();
        for (int index = 0; index < builds.size(); index++) {
            if (alloc[index] <= 0) {
                continue;
            }
            IslandBuild build = builds.get(index);
            byColumn.computeIfAbsent(build.match().clusterColumn(), k -> new ArrayList<>())
                    .add(new IslandAllocation(build, alloc[index]));
        }

        // Within a cardinal direction we may have multiple parent groups; lay
        // them out back-to-back along the primary axis, with a skip row between.
        EnumMap<Cardinal, Integer> cardinalDepth = new EnumMap<>(Cardinal.class);
        for (Cardinal c : Cardinal.values()) {
            cardinalDepth.put(c, 0);
        }

        for (Map.Entry<Integer, List<IslandAllocation>> entry : byColumn.entrySet()) {
            int column = entry.getKey();
            Cardinal cardinal = CARDINAL_BY_COLUMN[Math.floorMod(column, CARDINAL_BY_COLUMN.length)];
            String areaLabel = areaLabelForCardinal(cardinal);

            int perColumnLocal = 0;
            for (IslandAllocation allocation : entry.getValue()) {
                int startingDepth = cardinalDepth.get(cardinal);
                for (int n = 0; n < allocation.count(); n++) {
                    int localSlot = perColumnLocal + n;
                    int[] offset = cardinalOffset(cardinal, startingDepth, localSlot);
                    chests.add(buildChestSpec(
                            chestIndex++,
                            allocation.build(),
                            offset[0], offset[1],
                            allHomedStacks, random,
                            areaLabel
                    ));
                }
                perColumnLocal += allocation.count();
            }
            int parentDepthRows = (perColumnLocal + 1) / 2;
            cardinalDepth.merge(cardinal, parentDepthRows + CHEST_CLUSTER_PARENT_SPACER_ROWS, Integer::sum);
        }

        // Unlinked chests land in a small pile behind the player (NORTH, past any linked
        // clusters that claimed NORTH). NORTH is "Main Base", matching the cardinal split.
        int unlinkedStartDepth = cardinalDepth.get(Cardinal.NORTH);
        String unlinkedAreaLabel = areaLabelForCardinal(Cardinal.NORTH);
        for (int u = 0; u < unlinkedBudget; u++) {
            int[] offset = cardinalOffset(Cardinal.NORTH, unlinkedStartDepth, u);
            chests.add(buildChestSpec(
                    chestIndex++,
                    null,
                    offset[0], offset[1],
                    allHomedStacks, random,
                    unlinkedAreaLabel
            ));
        }

        return chests;
    }

    private static String areaLabelForCardinal(Cardinal cardinal) {
        return switch (cardinal) {
            case WEST, EAST -> CHEST_AREA_MOUNTAIN;
            case NORTH, SOUTH -> CHEST_AREA_MAIN;
        };
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
            List<TemplatedStack> allHomedStacks,
            Random random,
            String areaLabel
    ) {
        IslandSuggestionTemplate linkedTemplate = linked != null ? linked.match().parentTemplate() : null;
        List<ItemStack> linkedPool = linked != null ? linked.stacksOnly() : List.of();

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
            IslandSuggestionTemplate stackTemplate;
            if (useLinked) {
                template = linkedPool.get(random.nextInt(linkedPool.size()));
                stackTemplate = linkedTemplate;
            } else if (!allHomedStacks.isEmpty()) {
                TemplatedStack picked = allHomedStacks.get(random.nextInt(allHomedStacks.size()));
                template = picked.stack();
                stackTemplate = picked.template();
            } else {
                continue;
            }
            if (template == null || template.isEmpty()) {
                continue;
            }
            ItemStack stack = template.copy();
            int count = rollStackCount(stack, stackTemplate, random);
            stack.setCount(Math.max(1, count));
            contents.add(new ChestContentEntry(slotIndices.get(fillIndex), stack));
        }

        String linkedIslandId = linked != null ? linked.island().id() : "";
        return new ChestSpec(index, linkedIslandId, contents, deltaX, deltaZ, areaLabel);
    }

    private record IslandAllocation(IslandBuild build, int count) {
    }

    private static int rollStackCount(ItemStack stack, IslandSuggestionTemplate template, Random random) {
        int max;
        try {
            max = Math.max(1, stack.getMaxStackSize());
        } catch (RuntimeException | LinkageError ignored) {
            max = 64;
        }
        if (max <= 1) {
            return 1;
        }
        IslandSuggestionTemplate t = template == null ? IslandSuggestionTemplate.MISC : template;
        return switch (t) {
            case TOOLS, WEAPONS, ARMOR, WORKBENCHES, UPGRADES, TRANSPORT -> 1;
            case INGOTS, GEMS, RAW_MATERIALS, MATERIALS, BUILDING,
                    STAIRS, SLABS, WALLS, DOORS, FENCES, WINDOWS,
                    NATURAL, MECHANISMS -> {
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
            case LIGHTING -> {
                // Players carry stacks of torches / lanterns when caving.
                int floor = Math.min(8, max);
                int ceiling = Math.min(max, 32);
                int span = Math.max(1, ceiling - floor + 1);
                yield floor + random.nextInt(span);
            }
            case UTILITY, CURIOSITY, MISC -> {
                int ceiling = Math.min(max, 8);
                yield 1 + random.nextInt(ceiling);
            }
        };
    }

    private record TemplatedStack(IslandSuggestionTemplate template, ItemStack stack) {
    }

    /**
     * Stable comparator used for the within-island sort. Items with the
     * highest "carry score" — combined frequency + rarity signal —
     * appear first regardless of material family or flavor, so the top
     * rows of every island are the things players actually grab
     * (cobblestone, sticks, iron_ingot, oak_planks) rather than the
     * alphabetically-first variant of whatever family happens to start
     * with "a" (asphalt, andesite_polished_brick, azurine).
     *
     * <p>Within a single carry-score band, items still cluster by
     * material_family and flavor for residual visual order — so oak
     * stairs sit next to oak planks within the same tier — but
     * material_family is no longer the dominant key. The earlier sort
     * (tier → material_family → flavor → id) was producing
     * "random-looking" output because alphabetical family ordering
     * doesn't track player perception of importance: a stone-family
     * block sorted after asphalt-family because "asphalt" precedes
     * "stone" alphabetically.
     *
     * <p>Null-safe: unknown frequency / rarity contribute mid-tier
     * weight; nulls in material_family / flavor sort last within their
     * carry-score band.
     */
    public static final Comparator<DescribedStack> WITHIN_ISLAND_COMPARATOR = (a, b) -> {
        int carryCompare = Integer.compare(
                carryRank(a.descriptor),
                carryRank(b.descriptor));
        if (carryCompare != 0) {
            return carryCompare;
        }
        // Within a carry-score band, fall back to alphabetical id. We
        // tried material_family + flavor as secondary keys but those
        // produced visible alphabetical-family-name noise (asphalt
        // ahead of brick ahead of stone) that read as "random." Item id
        // alphabetical naturally clusters siblings (oak_planks /
        // oak_log / oak_stairs share the "oak_" prefix) without
        // dragging unrelated families to the top by name.
        String aId = a.stack == null ? "" : ItemIdentityMatcher.create(a.stack).itemId();
        String bId = b.stack == null ? "" : ItemIdentityMatcher.create(b.stack).itemId();
        return aId.compareTo(bId);
    };

    /**
     * Combined "how much would the player carry / grab this?" score.
     * Lower is more-carried; sorted ascending so high-score items land
     * at the top of an island. Frequency dominates (×10) with rarity
     * as a fine-grained tiebreaker. Items with neither classified
     * land in the middle, ahead of explicitly-rare items but behind
     * classified-frequent ones.
     */
    private static int carryRank(IslandSignalDescriptor descriptor) {
        int frequency = frequencyRank(descriptor.carryFrequency());
        int rarity = rarityRank(descriptor.rarity());
        return frequency * 10 + rarity;
    }

    private static int rarityRank(String rarity) {
        // Collapse abundant / common / null to the same rank so a single
        // outlier classification (e.g., granite happens to be flagged
        // "abundant" while oak_planks has no rarity) doesn't reorder the
        // island. Only uncommon+ tiers contribute a meaningful penalty.
        // The dataset shows this is the right shape — across all 2700+
        // entries only 21 are "abundant" and ~1300 are null, so treating
        // them together prevents abundance-as-noise from dominating the
        // sort while still letting "uncommon" / "rare" / "unique" push
        // niche items down.
        if (rarity == null) {
            return 0;
        }
        return switch (rarity) {
            case "abundant", "common" -> 0;
            case "uncommon" -> 2;
            case "rare" -> 4;
            case "unique" -> 6;
            default -> 0;
        };
    }

    /**
     * Weight for the carried-sample selection. Items the player uses
     * every game-day get overwhelmingly heavy weight; rare /
     * display-only items get vanishingly low weight so they almost
     * never appear in the synthesized inventory. Unknown frequency
     * lands in the middle tier, matching how the within-island sort
     * treats nulls. The everyday : occasional ratio is intentionally
     * extreme (50:1) — only ~5% of items are classified everyday in
     * the bundled dataset, so weaker weights would still let
     * occasional items dominate the carried sample by sheer count.
     */
    private static double carryFrequencyWeight(String frequency) {
        if (frequency == null) {
            return 1.0;
        }
        return switch (frequency) {
            case "everyday" -> 50.0;
            case "frequent" -> 12.0;
            case "occasional" -> 1.0;
            case "rare" -> 0.1;
            case "display_only", "never" -> 0.01;
            default -> 1.0;
        };
    }

    /**
     * Weighted sample of {@code k} elements from {@code items} without
     * replacement, using the parallel {@code weights} list. O(k·n) but
     * n is bounded by the homed-pool size (low hundreds in practice).
     */
    private static <T> List<T> weightedSampleWithoutReplacement(
            List<T> items,
            List<Double> weights,
            int k,
            Random random
    ) {
        int n = items.size();
        if (k <= 0 || n == 0) {
            return List.of();
        }
        if (k >= n) {
            ArrayList<T> shuffled = new ArrayList<>(items);
            Collections.shuffle(shuffled, random);
            return shuffled;
        }
        double[] remainingWeights = new double[n];
        boolean[] taken = new boolean[n];
        double total = 0.0;
        for (int i = 0; i < n; i++) {
            double w = Math.max(0.0, weights.get(i));
            remainingWeights[i] = w;
            total += w;
        }
        ArrayList<T> picked = new ArrayList<>(k);
        for (int draw = 0; draw < k; draw++) {
            if (total <= 0.0) {
                // Every remaining weight is zero; fall back to uniform
                // pick over still-available items so we still hit `k`.
                int chosen = -1;
                int remaining = n - picked.size();
                int rolled = random.nextInt(remaining);
                for (int i = 0; i < n; i++) {
                    if (!taken[i] && rolled-- == 0) {
                        chosen = i;
                        break;
                    }
                }
                if (chosen < 0) {
                    break;
                }
                taken[chosen] = true;
                picked.add(items.get(chosen));
                continue;
            }
            double target = random.nextDouble() * total;
            double cumulative = 0.0;
            int chosen = -1;
            for (int i = 0; i < n; i++) {
                if (taken[i]) {
                    continue;
                }
                cumulative += remainingWeights[i];
                if (target <= cumulative) {
                    chosen = i;
                    break;
                }
            }
            if (chosen < 0) {
                break;
            }
            taken[chosen] = true;
            total -= remainingWeights[chosen];
            picked.add(items.get(chosen));
        }
        return picked;
    }

    private static int nullsLast(String a, String b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return a.compareTo(b);
    }

    /**
     * Frequency ordering: items players touch every game-day get the
     * top-left of an island, items seen rarely get pushed to the bottom.
     * Unknown frequency sorts in the middle. {@code display_only} (creative-
     * only / dev / deprecated) goes last — these populate the MISC.deep
     * pseudo-section without producing a separate island.
     */
    private static int frequencyRank(String frequency) {
        if (frequency == null) {
            return 3;
        }
        return switch (frequency) {
            case "everyday" -> 0;
            case "frequent" -> 1;
            case "occasional" -> 2;
            case "rare" -> 4;
            case "display_only", "never" -> 5;
            default -> 3;
        };
    }

    /**
     * Stack + descriptor pair carried through the populate pipeline.
     * Public so the within-island comparator can be used in tests.
     */
    public record DescribedStack(ItemStack stack, IslandSignalDescriptor descriptor) {
    }

    private static final class CategoryGroup {
        private final IslandTemplateMatch match;
        private final ArrayList<DescribedStack> stacks;

        private CategoryGroup(IslandTemplateMatch match) {
            this.match = match;
            this.stacks = new ArrayList<>();
        }

        IslandTemplateMatch match() {
            return match;
        }

        ArrayList<DescribedStack> stacks() {
            return stacks;
        }
    }

    private record IslandBuild(
            IslandTemplateMatch match,
            VisualAtlasIsland island,
            int predictedWidth,
            int predictedHeight,
            List<VisualHomeAssignment> assignments,
            List<DescribedStack> stacks
    ) {
        List<ItemStack> stacksOnly() {
            ArrayList<ItemStack> out = new ArrayList<>(stacks.size());
            for (DescribedStack ds : stacks) {
                out.add(ds.stack());
            }
            return out;
        }
    }
}
