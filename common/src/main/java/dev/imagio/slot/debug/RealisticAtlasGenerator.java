package dev.imagio.slot.debug;

import dev.imagio.slot.classification.DynamicHomeCohortPolicy;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.triage.CommonItemTagFamilies;
import dev.imagio.slot.inventory.triage.IslandSignal;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.triage.IslandTemplateMatch;
import dev.imagio.slot.inventory.triage.LearnedAdjacencyKey;
import dev.imagio.slot.inventory.triage.WithinIslandOrdering;
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
import java.util.Set;
import java.util.function.Function;

public final class RealisticAtlasGenerator {

    /**
     * Minimum classifier-tagged items required for a dynamic group to qualify
     * as its own island. Smaller cohorts fold back into the parent template so
     * generated atlases do not fragment into singleton sections.
     */
    public static final int DEFAULT_MIN_SUBSYSTEM_ITEMS = DynamicHomeCohortPolicy.DEFAULT_MIN_SUBSYSTEM_ITEMS;

    // Mirror the real atlas layout so generated assignments land on the same invisible
    // grid the UI uses for clamp + snap — otherwise every card shows up misaligned and
    // needs a drag to snap into place.
    private static final int CARD_WIDTH = SlotWorkspaceAtlasLayout.CARD_WIDTH;
    private static final int CARD_HEIGHT = SlotWorkspaceAtlasLayout.CARD_HEIGHT;
    private static final int CARD_GAP = SlotWorkspaceAtlasLayout.CARD_GAP;
    private static final int ISLAND_PADDING_X = SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_X;
    private static final int ISLAND_PADDING_Y = SlotWorkspaceAtlasLayout.ISLAND_CONTENT_PADDING_Y;
    private static final int ISLAND_CONTENT_TOP = SlotWorkspaceAtlasLayout.ISLAND_CONTENT_TOP;
    // Tight by design: populate-generated atlases were tedious to use because
    // every island landed a screen apart and required manual drag-and-snap
    // before the layout was usable for testing. Without ghost rendering most
    // islands hold only a handful of carried items, so a card-sized gap
    // already reads as a comfortable break. The nudge layout will push any
    // accidental overlap apart on first display, so we err small.
    //
    // Inter-row gap must clear the renderer's per-island chrome: 24px header
    // band reserved above each island + 16px atlas pad below each island
    // (see AtlasLayout.packAtlas) = 40px minimum body-to-body. We add a few
    // pixels of breathing room so labels don't crowd.
    private static final int ISLAND_GAP = 16;
    private static final int ISLAND_PARENT_GROUP_GAP = 32;
    private static final int ISLAND_CLUSTER_ROW_GAP = 48;

    /**
     * Cluster colors override the per-template color for populate-generated
     * islands so the atlas reads as a few large color bands (gear / build /
     * materials / machinery / decoration) instead of a confetti of unrelated
     * tints. Indexed by {@code clusterRow}; {@code MISC}'s row 4 lands here
     * too.
     */
    private static final int[] CLUSTER_ROW_COLORS = new int[]{
            0xCC6E3D3D, // 0 — player gear (warm red)
            0xCC5A4A38, // 1 — building (wood/stone brown)
            0xCC3D6E5A, // 2 — materials & natural (green)
            0xCC8A5E24, // 3 — machinery (industrial amber)
            0xCC4F3D6E  // 4 — decoration / curio / misc (purple)
    };
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
     * {@link IslandSignalDescriptor}, allowing dynamic classifier matching,
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

        // First pass: extract descriptors for every homed stack; histogram dynamic
        // classification ids so we can decide which groups qualify as their own
        // islands. Small cohorts collapse back to their parent template so populate
        // does not fragment into singleton sections.
        ArrayList<DescribedStack> described = new ArrayList<>(homedTemplates.size());
        HashMap<String, Integer> subsystemHistogram = new HashMap<>();
        HashMap<String, Integer> organizationGroupHistogram = new HashMap<>();
        for (ItemStack stack : homedTemplates) {
            IslandSignalDescriptor descriptor = descriptorFn.apply(stack);
            if (descriptor == null) {
                descriptor = IslandSignalDescriptor.empty(ItemIdentityMatcher.create(stack));
            }
            described.add(new DescribedStack(stack, descriptor));
            // Trophies bypass dynamic grouping by design — they belong on
            // display, not filed under their mod's workflow/subsystem.
            if (IslandSuggestionTemplate.isTrophy(descriptor)) {
                continue;
            }
            IslandSuggestionTemplate parent = IslandSuggestionTemplate.firstMatchOrMisc(descriptor);
            if (parent.allowsSubsystemGrouping()) {
                for (String subsystemId : descriptor.subsystems()) {
                    if (subsystemId == null || subsystemId.isBlank()) {
                        continue;
                    }
                    subsystemHistogram.merge(subsystemId, 1, Integer::sum);
                }
            }
            if (parent.allowsOrganizationGrouping()) {
                for (String groupId : descriptor.organizationGroups()) {
                    if (groupId == null || groupId.isBlank()) {
                        continue;
                    }
                    organizationGroupHistogram.merge(groupId, 1, Integer::sum);
                }
            }
        }
        int threshold = Math.max(1, minSubsystemItems);
        java.util.function.Predicate<String> subsystemQualifier = id -> {
            Integer count = subsystemHistogram.get(id);
            return count != null && count >= threshold;
        };
        java.util.function.Predicate<String> organizationGroupQualifier = id -> {
            Integer count = organizationGroupHistogram.get(id);
            return count != null && count >= threshold;
        };

        // Second pass: classify each stack via firstMatchExtended.
        LinkedHashMap<String, CategoryGroup> grouped = new LinkedHashMap<>();
        for (DescribedStack ds : described) {
            IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                    ds.descriptor,
                    subsystemQualifier,
                    organizationGroupQualifier
            );
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

        // Realistic carry: a real player has a loadout (one pickaxe, one
        // axe, one sword, armor set, food, torches, basic blocks) plus a
        // pile of loot from recent mining/fighting (raw ores, ingots,
        // mob drops, gathered naturals). Build the loadout first so we
        // always get tier-appropriate gear, then top up with loot-biased
        // sampling so it looks like an active session, not a uniform
        // weighted dump of every homed identity.
        ArrayList<TemplatedStack> carriedPool = new ArrayList<>();
        for (IslandBuild build : builds) {
            for (DescribedStack ds : build.stacks()) {
                carriedPool.add(new TemplatedStack(
                        build.match().parentTemplate(), ds.stack, ds.descriptor));
            }
        }
        int carriedCap = Math.min(carriedPool.size(), profile.carriedIdentityCap());
        List<TemplatedStack> picked = pickRealisticCarriedSet(carriedPool, carriedCap, random);
        ArrayList<ItemStack> homedStacks = new ArrayList<>(picked.size());
        for (TemplatedStack ts : picked) {
            ItemStack copy = ts.stack().copy();
            copy.setCount(Math.max(1, rollStackCount(copy, ts.template(), ts.descriptor(), random)));
            homedStacks.add(copy);
        }

        List<ChestSpec> chests = planChests(builds, profile.chestCount(), random);
        List<ItemStack> triageStacks = rollTriageStacks(
                triageTemplates,
                descriptorFn,
                subsystemQualifier,
                organizationGroupQualifier,
                random
        );

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
                java.util.List.of(),
                null, null, null, null, null,
                java.util.List.of(),
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
            case INGOTS -> java.util.Set.of(
                    CommonItemTagFamilies.canonicalRootTag(CommonItemTagFamilies.Family.INGOTS));
            case GEMS -> java.util.Set.of(
                    CommonItemTagFamilies.canonicalRootTag(CommonItemTagFamilies.Family.GEMS));
            case RAW_MATERIALS -> java.util.Set.of(
                    CommonItemTagFamilies.canonicalRootTag(CommonItemTagFamilies.Family.RAW_MATERIALS));
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
            java.util.function.Predicate<String> organizationGroupQualifier,
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
                    descriptor,
                    subsystemQualifier,
                    organizationGroupQualifier
            );
            copy.setCount(Math.max(1, rollStackCount(copy, match.parentTemplate(), descriptor, random)));
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
            // Floor predicted size at the renderer's PLAYER_ISLAND_MIN_*
            // so a 1-item island doesn't reserve 4-card-wide space the
            // renderer never uses, leaving a fat empty band between
            // neighbours. Without the floor the layout matched, the
            // generator produced wide gaps that made the populate atlas
            // need manual cleanup.
            int width = Math.max(SlotWorkspaceAtlasLayout.PLAYER_ISLAND_MIN_WIDTH,
                    ISLAND_PADDING_X * 2
                            + cardsPerRow * CARD_WIDTH
                            + Math.max(0, cardsPerRow - 1) * CARD_GAP);
            int height = Math.max(SlotWorkspaceAtlasLayout.PLAYER_ISLAND_MIN_HEIGHT,
                    ISLAND_CONTENT_TOP
                            + rows * CARD_HEIGHT
                            + Math.max(0, rows - 1) * CARD_GAP
                            + ISLAND_PADDING_Y);

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
                    clusterColor(group.match().clusterRow()),
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
        if (itemCount <= 1) {
            return 1;
        }
        int estimate = (int) Math.round(Math.sqrt(itemCount * 1.5));
        // Don't force MIN_CARDS_PER_ROW for tiny islands: the renderer
        // packs a 1-item island into PLAYER_ISLAND_MIN_WIDTH (~1 card +
        // padding), so over-predicting the column count just leaves
        // dead space between neighbours.
        return Math.min(MAX_CARDS_PER_ROW, Math.max(1, Math.min(itemCount, estimate)));
    }

    private static int clusterColor(int clusterRow) {
        if (clusterRow < 0 || clusterRow >= CLUSTER_ROW_COLORS.length) {
            return CLUSTER_ROW_COLORS[CLUSTER_ROW_COLORS.length - 1];
        }
        return CLUSTER_ROW_COLORS[clusterRow];
    }

    private static void layoutIslands(List<IslandBuild> builds) {
        // Group by template cluster row; within a row, sort by clusterColumn.
        // This gives populate-generated atlases a deterministic spatial
        // layout that mirrors the template enum's intended grouping
        // (player-gear row, raw/build row, machinery row, extras row).
        // Dynamic islands inherit cluster row/column from their parent
        // template, so all TFC masonry/material groups still land in the
        // materials row and sort by classifier id within that row.
        builds.sort(Comparator
                .comparingInt((IslandBuild b) -> b.match().clusterRow())
                .thenComparingInt(b -> b.match().clusterColumn())
                .thenComparingInt(b -> b.match().isDynamic() ? 1 : 0)
                .thenComparingInt(b -> b.match().isOrganizationGroup() ? 0 : 1)
                .thenComparing(b -> b.island().id()));

        // Wide target: monitors are ~16:10, so prefer a long horizontal
        // band over a square. The five cluster rows stack vertically
        // anyway, so the height floor is fixed; widening the wrap target
        // mostly lets each cluster row keep its islands on a single line
        // instead of folding into sub-rows the player has to scan
        // top-down. The atlas zooms / pans freely, so we err generous.
        long totalArea = 0L;
        for (IslandBuild b : builds) {
            totalArea += (long) b.predictedWidth() * (long) b.predictedHeight();
        }
        int targetWidth = Math.max(1600,
                (int) Math.round(Math.sqrt(Math.max(1L, totalArea)) * 2.0));

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
                    currentY += rowMaxHeight + ISLAND_CLUSTER_ROW_GAP;
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
                allHomedStacks.add(new TemplatedStack(
                        build.match().parentTemplate(), ds.stack(), ds.descriptor()));
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

        // Per-island "already-seeded keys" so multiple chests in the
        // same island span different facet themes. With 3 chests on a
        // 24-item iron+gold+copper MATERIALS island, the second chest
        // skips iron-keyed seeds (claimed by chest 1), the third skips
        // both iron and gold. Without this, three independent uniform
        // seed picks could all land on iron and the populated atlas
        // would read as "three iron chests" instead of
        // "iron / gold / copper".
        HashMap<String, Set<LearnedAdjacencyKey>> claimedSeedKeysByIsland = new HashMap<>();

        for (Map.Entry<Integer, List<IslandAllocation>> entry : byColumn.entrySet()) {
            int column = entry.getKey();
            Cardinal cardinal = CARDINAL_BY_COLUMN[Math.floorMod(column, CARDINAL_BY_COLUMN.length)];
            String areaLabel = areaLabelForCardinal(cardinal);

            int perColumnLocal = 0;
            for (IslandAllocation allocation : entry.getValue()) {
                int startingDepth = cardinalDepth.get(cardinal);
                Set<LearnedAdjacencyKey> claimedForIsland = claimedSeedKeysByIsland
                        .computeIfAbsent(allocation.build().island().id(),
                                ignored -> new java.util.LinkedHashSet<>());
                for (int n = 0; n < allocation.count(); n++) {
                    int localSlot = perColumnLocal + n;
                    int[] offset = cardinalOffset(cardinal, startingDepth, localSlot);
                    chests.add(buildChestSpec(
                            chestIndex++,
                            allocation.build(),
                            offset[0], offset[1],
                            allHomedStacks, random,
                            areaLabel,
                            claimedForIsland
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
                    unlinkedAreaLabel,
                    new java.util.LinkedHashSet<>()
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

    /**
     * Probability that a "linked" fill picks a facet-similar item over a
     * uniform-random pick from the linked island's pool. Higher values
     * make each chest more strongly themed (one chest = iron-family,
     * another = gold-family). 0.7 is enough to read as "the iron chest"
     * vs "the gold chest" while still letting siblings spill over so a
     * single chest doesn't capture every iron-tagged item in the
     * island.
     */
    private static final double CHEST_FACET_SIMILARITY_BIAS = 0.7;

    private static ChestSpec buildChestSpec(
            int index,
            IslandBuild linked,
            int deltaX,
            int deltaZ,
            List<TemplatedStack> allHomedStacks,
            Random random,
            String areaLabel,
            Set<LearnedAdjacencyKey> claimedSeedKeysForIsland
    ) {
        IslandSuggestionTemplate linkedTemplate = linked != null ? linked.match().parentTemplate() : null;
        List<DescribedStack> linkedDescribed = linked != null ? linked.stacks() : List.of();

        // Each chest picks a "seed" from the linked island; subsequent
        // linked-pool fills prefer items that share a *specific*
        // adjacency key with the seed (organization group, subsystem, tag,
        // material_family, dye_color — the priority-rank-0 kinds). NAMESPACE and
        // CREATIVE_TAB are deliberately excluded from this clustering
        // signal: every modpack item shares "minecraft" or "create"
        // namespace, so namespace matches collapse the cluster back to
        // "everything in the linked island". Specific keys are what
        // make one MATERIALS chest read as "iron stuff" and another as
        // "gold stuff".
        //
        // Across multiple chests in the same island, prefer seed items
        // whose specific keys haven't been claimed yet so chests span
        // different facet themes (one iron chest, one gold chest, …)
        // rather than re-rolling the same theme.
        DescribedStack seed = pickDiverseSeed(linkedDescribed, claimedSeedKeysForIsland, random);
        Set<LearnedAdjacencyKey> seedKeys = seed == null
                ? Set.of()
                : specificKeysFor(seed.descriptor());
        if (claimedSeedKeysForIsland != null) {
            claimedSeedKeysForIsland.addAll(seedKeys);
        }

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
            boolean useLinked = !linkedDescribed.isEmpty()
                    && random.nextDouble() < CHEST_BUCKET_BIAS;
            ItemStack template;
            IslandSuggestionTemplate stackTemplate;
            IslandSignalDescriptor stackDescriptor;
            if (useLinked) {
                DescribedStack picked = pickLinkedItem(linkedDescribed, seedKeys, random);
                template = picked.stack();
                stackTemplate = linkedTemplate;
                stackDescriptor = picked.descriptor();
            } else if (!allHomedStacks.isEmpty()) {
                TemplatedStack picked = allHomedStacks.get(random.nextInt(allHomedStacks.size()));
                template = picked.stack();
                stackTemplate = picked.template();
                stackDescriptor = picked.descriptor();
            } else {
                continue;
            }
            if (template == null || template.isEmpty()) {
                continue;
            }
            ItemStack stack = template.copy();
            int count = rollStackCount(stack, stackTemplate, stackDescriptor, random);
            stack.setCount(Math.max(1, count));
            contents.add(new ChestContentEntry(slotIndices.get(fillIndex), stack));
        }

        String linkedIslandId = linked != null ? linked.island().id() : "";
        return new ChestSpec(index, linkedIslandId, contents, deltaX, deltaZ, areaLabel);
    }

    /**
     * Pick an item from {@code linkedDescribed}, biased toward items
     * sharing at least one adjacency key with the chest's seed. With
     * probability {@link #CHEST_FACET_SIMILARITY_BIAS} the pick is
     * restricted to facet-similar items; otherwise it's uniform random
     * across the linked pool. When the seed has no usable facet keys
     * (or no items match), falls through to a uniform pick so the chest
     * still gets filled.
     */
    private static DescribedStack pickLinkedItem(
            List<DescribedStack> linkedDescribed,
            Set<LearnedAdjacencyKey> seedKeys,
            Random random
    ) {
        if (seedKeys.isEmpty()
                || random.nextDouble() >= CHEST_FACET_SIMILARITY_BIAS) {
            return linkedDescribed.get(random.nextInt(linkedDescribed.size()));
        }
        ArrayList<DescribedStack> matching = new ArrayList<>();
        for (DescribedStack candidate : linkedDescribed) {
            IslandSignalDescriptor descriptor = candidate.descriptor();
            if (descriptor == null) {
                continue;
            }
            Set<LearnedAdjacencyKey> candidateKeys = specificKeysFor(descriptor);
            for (LearnedAdjacencyKey key : candidateKeys) {
                if (seedKeys.contains(key)) {
                    matching.add(candidate);
                    break;
                }
            }
        }
        if (matching.isEmpty()) {
            return linkedDescribed.get(random.nextInt(linkedDescribed.size()));
        }
        return matching.get(random.nextInt(matching.size()));
    }

    /**
     * Pick a chest seed from the linked island, preferring items whose
     * specific adjacency keys are disjoint from
     * {@code claimedSeedKeysForIsland}. When every candidate's keys
     * overlap (or there's no claim set), falls back to uniform random
     * so the chest still gets seeded.
     */
    private static DescribedStack pickDiverseSeed(
            List<DescribedStack> linkedDescribed,
            Set<LearnedAdjacencyKey> claimedSeedKeysForIsland,
            Random random
    ) {
        if (linkedDescribed.isEmpty()) {
            return null;
        }
        if (claimedSeedKeysForIsland == null || claimedSeedKeysForIsland.isEmpty()) {
            return linkedDescribed.get(random.nextInt(linkedDescribed.size()));
        }
        ArrayList<DescribedStack> diverse = new ArrayList<>();
        for (DescribedStack candidate : linkedDescribed) {
            Set<LearnedAdjacencyKey> candidateKeys = specificKeysFor(candidate.descriptor());
            if (candidateKeys.isEmpty()) {
                // Item has no specific keys — fine to pick, doesn't
                // claim anything new.
                diverse.add(candidate);
                continue;
            }
            boolean disjoint = true;
            for (LearnedAdjacencyKey key : candidateKeys) {
                if (claimedSeedKeysForIsland.contains(key)) {
                    disjoint = false;
                    break;
                }
            }
            if (disjoint) {
                diverse.add(candidate);
            }
        }
        if (diverse.isEmpty()) {
            // Every facet theme has been claimed by previous chests;
            // accept duplication and pick uniformly.
            return linkedDescribed.get(random.nextInt(linkedDescribed.size()));
        }
        return diverse.get(random.nextInt(diverse.size()));
    }

    /**
     * Subset of {@link LearnedAdjacencyKey#keysFor} keeping only the
     * "specific" priority-rank-0 kinds (TAG, MATERIAL_FAMILY,
     * SUBSYSTEM, DYE_COLOR). The chest-clustering pass uses this
     * tighter set so namespace matches don't bleed every modpack item
     * into a single cluster.
     */
    private static Set<LearnedAdjacencyKey> specificKeysFor(IslandSignalDescriptor descriptor) {
        if (descriptor == null) {
            return Set.of();
        }
        java.util.LinkedHashSet<LearnedAdjacencyKey> filtered = new java.util.LinkedHashSet<>();
        for (LearnedAdjacencyKey key : LearnedAdjacencyKey.keysFor(descriptor)) {
            if (key.priorityRank() == 0) {
                filtered.add(key);
            }
        }
        return Set.copyOf(filtered);
    }

    private record IslandAllocation(IslandBuild build, int count) {
    }

    private static int rollStackCount(ItemStack stack, IslandSuggestionTemplate template, Random random) {
        return rollStackCount(stack, template, null, random);
    }

    /**
     * Descriptor-aware variant. Trophy-tier ({@code rarity=unique}) and
     * display-only ({@code carry_frequency in {display_only, never}})
     * items always roll as a single item — a {@code nether_star} or
     * {@code dragon_egg} shouldn't appear as a stack of 8 in a chest.
     * Otherwise falls through to the template-keyed roll.
     */
    private static int rollStackCount(
            ItemStack stack,
            IslandSuggestionTemplate template,
            IslandSignalDescriptor descriptor,
            Random random
    ) {
        int max;
        try {
            max = Math.max(1, stack.getMaxStackSize());
        } catch (RuntimeException | LinkageError ignored) {
            max = 64;
        }
        if (max <= 1) {
            return 1;
        }
        if (descriptor != null) {
            // Trophies (role=trophy or rarity=unique) and display-only
            // items always roll as a single item — a nether_star /
            // dragon_egg shouldn't land in a chest as a stack of 8, and
            // creative-only / dev items shouldn't appear as bulk
            // either.
            if (IslandSuggestionTemplate.isTrophy(descriptor)) {
                return 1;
            }
            String frequency = descriptor.carryFrequency();
            if ("display_only".equals(frequency) || "never".equals(frequency)) {
                return 1;
            }
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

    private record TemplatedStack(
            IslandSuggestionTemplate template,
            ItemStack stack,
            IslandSignalDescriptor descriptor
    ) {
    }

    /**
     * Re-export of {@link WithinIslandOrdering#WITHIN_ISLAND_COMPARATOR}
     * so existing callers and tests don't need to import the triage
     * package directly. Definitive logic lives in
     * {@link WithinIslandOrdering}.
     */
    public static final Comparator<DescribedStack> WITHIN_ISLAND_COMPARATOR = (a, b) ->
            WithinIslandOrdering.WITHIN_ISLAND_COMPARATOR.compare(
                    new WithinIslandOrdering.DescribedStack(a.stack(), a.descriptor()),
                    new WithinIslandOrdering.DescribedStack(b.stack(), b.descriptor()));

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

    /**
     * Stack + descriptor pair carried through the populate pipeline.
     * Public so the within-island comparator can be used in tests.
     * Mirrors {@link WithinIslandOrdering.DescribedStack} so existing
     * test code keeps compiling.
     */
    public record DescribedStack(ItemStack stack, IslandSignalDescriptor descriptor) {
    }

    /**
     * Compose the player's carried set as a realistic loadout (one of
     * each core gear slot at the best available tier) plus a loot-biased
     * fill (raw materials, ingots, mob drops, etc.) up to {@code cap}.
     * Returns at most {@code cap} stacks; preserves loadout-first
     * ordering so the inventory layout is predictable.
     */
    private static List<TemplatedStack> pickRealisticCarriedSet(
            List<TemplatedStack> pool,
            int cap,
            Random random
    ) {
        if (pool.isEmpty() || cap <= 0) {
            return List.of();
        }
        ArrayList<TemplatedStack> result = new ArrayList<>(cap);
        java.util.LinkedHashSet<ItemIdentity> taken = new java.util.LinkedHashSet<>();
        // Core loadout: best-tier representative of each gear slot the
        // player almost certainly has on them. Skipping a slot when no
        // candidate exists is fine — modpacks vary in coverage.
        addBestTier(result, taken, pool, t -> t.template() == IslandSuggestionTemplate.TOOLS
                && idEndsWith(t, "_pickaxe"), cap);
        addBestTier(result, taken, pool, t -> t.template() == IslandSuggestionTemplate.TOOLS
                && idEndsWith(t, "_axe") && !idEndsWith(t, "_pickaxe"), cap);
        addBestTier(result, taken, pool, t -> t.template() == IslandSuggestionTemplate.TOOLS
                && idEndsWith(t, "_shovel"), cap);
        addBestTier(result, taken, pool, t -> t.template() == IslandSuggestionTemplate.TOOLS
                && idEndsWith(t, "_hoe"), cap);
        addBestTier(result, taken, pool, t -> t.template() == IslandSuggestionTemplate.WEAPONS
                && idEndsWith(t, "_sword"), cap);
        addBestTier(result, taken, pool, t -> t.template() == IslandSuggestionTemplate.WEAPONS
                && hasClassSignal(t, IslandSignal.BOW), cap);
        addBestTier(result, taken, pool, t -> hasClassSignal(t, IslandSignal.ARMOR_HEAD), cap);
        addBestTier(result, taken, pool, t -> hasClassSignal(t, IslandSignal.ARMOR_CHEST), cap);
        addBestTier(result, taken, pool, t -> hasClassSignal(t, IslandSignal.ARMOR_LEGS), cap);
        addBestTier(result, taken, pool, t -> hasClassSignal(t, IslandSignal.ARMOR_FEET), cap);
        addBestTier(result, taken, pool, t -> t.template() == IslandSuggestionTemplate.FOOD, cap);
        addBestTier(result, taken, pool, t -> t.template() == IslandSuggestionTemplate.LIGHTING, cap);
        addBestTier(result, taken, pool, t -> t.template() == IslandSuggestionTemplate.STORAGE, cap);

        // Loot fill: weight loot-typical templates higher (raw materials,
        // ingots, gems, naturals, mob-drop misc) so the leftover budget
        // captures the "I just got back from a mining/fighting session"
        // feel rather than another stack of stairs.
        if (result.size() >= cap) {
            return result;
        }
        ArrayList<TemplatedStack> remaining = new ArrayList<>();
        ArrayList<Double> lootWeights = new ArrayList<>();
        for (TemplatedStack t : pool) {
            ItemIdentity id = ItemIdentityMatcher.create(t.stack());
            if (taken.contains(id)) {
                continue;
            }
            remaining.add(t);
            lootWeights.add(lootWeight(t));
        }
        int slotsLeft = cap - result.size();
        List<TemplatedStack> loot = weightedSampleWithoutReplacement(
                remaining, lootWeights, slotsLeft, random);
        for (TemplatedStack t : loot) {
            result.add(t);
            taken.add(ItemIdentityMatcher.create(t.stack()));
        }
        return result;
    }

    private static void addBestTier(
            List<TemplatedStack> result,
            java.util.LinkedHashSet<ItemIdentity> taken,
            List<TemplatedStack> pool,
            java.util.function.Predicate<TemplatedStack> filter,
            int cap
    ) {
        if (result.size() >= cap) {
            return;
        }
        TemplatedStack best = null;
        int bestRank = Integer.MIN_VALUE;
        for (TemplatedStack t : pool) {
            ItemIdentity id = ItemIdentityMatcher.create(t.stack());
            if (taken.contains(id) || !filter.test(t)) {
                continue;
            }
            int rank = tierRank(t.descriptor().materialFamily());
            if (best == null || rank > bestRank) {
                best = t;
                bestRank = rank;
            }
        }
        if (best != null) {
            result.add(best);
            taken.add(ItemIdentityMatcher.create(best.stack()));
        }
    }

    private static int tierRank(String materialFamily) {
        if (materialFamily == null) {
            return 0;
        }
        return switch (materialFamily) {
            case "netherite" -> 6;
            case "diamond" -> 5;
            case "iron" -> 4;
            case "gold", "golden" -> 3;
            case "stone" -> 2;
            case "wood", "wooden", "wood_oak", "wood_birch", "wood_spruce",
                    "wood_jungle", "wood_acacia", "wood_dark_oak",
                    "wood_mangrove", "wood_cherry" -> 1;
            default -> 0;
        };
    }

    private static double lootWeight(TemplatedStack t) {
        // Loot bias mirrors what mining/fighting sessions actually drop:
        // raw ores, ingots from smelting, gems, natural resources, and
        // misc mob drops (gunpowder/string/bones land in MISC or NATURAL
        // depending on classification). Trophies and curiosities are
        // rare loot; staircases and decorative blocks aren't.
        double bias = switch (t.template()) {
            case RAW_MATERIALS, INGOTS, GEMS -> 6.0;
            case NATURAL -> 4.0;
            case MISC -> 2.5;
            case FOOD -> 2.0;
            case BUILDING -> 1.5;
            case MATERIALS, LIGHTING -> 1.2;
            case STAIRS, SLABS, WALLS, DOORS, FENCES, WINDOWS -> 0.6;
            case DECORATION, UTILITY, REDSTONE -> 0.5;
            case STORAGE, TRANSPORT, UPGRADES, MECHANISMS, WORKBENCHES -> 0.4;
            case CURIOSITY -> 0.05;
            default -> 1.0;
        };
        return bias * Math.sqrt(Math.max(0.05, carryFrequencyWeight(t.descriptor().carryFrequency())));
    }

    private static boolean idEndsWith(TemplatedStack t, String suffix) {
        ItemIdentity id = t.descriptor().identity();
        if (id == null || id.itemId() == null) {
            return false;
        }
        return id.itemId().endsWith(suffix);
    }

    private static boolean hasClassSignal(TemplatedStack t, IslandSignal signal) {
        return t.descriptor().classSignals() != null
                && t.descriptor().classSignals().contains(signal);
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
