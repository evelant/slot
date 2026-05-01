package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure within-island sort logic, shared between the populate-time
 * {@code RealisticAtlasGenerator} and the live chip-accept placement
 * path in {@code SlotWorkspaceCommandService}. Extracted from the
 * generator so production code can land new homes in the right cluster
 * slot without depending on the {@code debug} package.
 *
 * <p>Items with the highest "carry score" — combined frequency + rarity
 * signal — appear first regardless of material family or flavor, so
 * the top rows of every island are the things players actually grab
 * (cobblestone, sticks, iron_ingot, oak_planks) rather than the
 * alphabetically-first variant of whatever family happens to start
 * with "a" (asphalt, andesite_polished_brick, azurine).
 *
 * <p>Within a single carry-score band, items group by a layered
 * cluster key:
 * <ol>
 *   <li><b>{@code 1dye:<stem>}</b> — items with a {@code dye_color}
 *       facet, keyed by their id with the color prefix stripped, so
 *       all 16 wools / carpets / concretes form a contiguous block
 *       ordered as a dye wheel (white → light_gray → gray → black →
 *       brown → red → ...). The dye-table color wheel reads as a
 *       block of related items rather than alphabetical chaos
 *       (black, blue, brown, cyan). Dye clusters are kept intact —
 *       flavor doesn't refine them.</li>
 *   <li><b>{@code 2pal:<primary>:<flavor>:<origin>}</b> — items
 *       with a {@code palette} facet but no dye_color, keyed by
 *       their primary palette tone ({@code wood_red},
 *       {@code wood_medium}, {@code copper_oxidized}, {@code warm}),
 *       then by flavor so plain wood_red items lead fancy wood_red,
 *       then by origin tier so early-game items lead late-game.</li>
 *   <li><b>{@code 3pln:<flavor>:<origin>:<id>}</b> — everything
 *       else, partitioned first by flavor rank (plain → natural →
 *       variant → colored → fancy → mechanical → mystical →
 *       ominous → ancient → unflavored), then by origin tier
 *       (early overworld+crafted → mid structures → late
 *       nether+end → unknown → creative-only), then id-alpha
 *       which already clusters siblings by prefix
 *       ({@code oak_log / oak_planks / oak_stairs}).</li>
 * </ol>
 *
 * <p>Null-safe: unknown frequency / rarity contribute mid-tier
 * weight; nulls in dye_color skip the canonical order branch and
 * sort by full id.
 */
public final class WithinIslandOrdering {

    private WithinIslandOrdering() {
    }

    /**
     * Stack + descriptor pair carried through the populate pipeline
     * and the live placement path. Public so callers can build it
     * from their own (stack, descriptor) sources.
     */
    public record DescribedStack(ItemStack stack, IslandSignalDescriptor descriptor) {
    }

    public static final Comparator<DescribedStack> WITHIN_ISLAND_COMPARATOR = (a, b) -> {
        int carryCompare = Integer.compare(
                carryRank(a.descriptor()),
                carryRank(b.descriptor()));
        if (carryCompare != 0) {
            return carryCompare;
        }
        String aId = a.stack() == null ? "" : ItemIdentityMatcher.create(a.stack()).itemId();
        String bId = b.stack() == null ? "" : ItemIdentityMatcher.create(b.stack()).itemId();
        int clusterCompare = clusterKey(aId, a.descriptor())
                .compareTo(clusterKey(bId, b.descriptor()));
        if (clusterCompare != 0) {
            return clusterCompare;
        }
        // Same cluster → canonical dye index orders the wheel within a
        // dye cluster (no-op for palette / plain-id clusters since neither
        // side will have a dye_color). Final tiebreak is full id so
        // identical-key items keep arrival order.
        int orderCompare = Integer.compare(
                canonicalDyeIndex(a.descriptor() == null ? null : a.descriptor().dyeColor()),
                canonicalDyeIndex(b.descriptor() == null ? null : b.descriptor().dyeColor()));
        if (orderCompare != 0) {
            return orderCompare;
        }
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
    public static int carryRank(IslandSignalDescriptor descriptor) {
        if (descriptor == null) {
            return 3 * 10;
        }
        int frequency = frequencyRank(descriptor.carryFrequency());
        int rarity = rarityRank(descriptor.rarity());
        return frequency * 10 + rarity;
    }

    /**
     * Frequency ordering: items players touch every game-day get the
     * top-left of an island, items seen rarely get pushed to the bottom.
     * Unknown frequency sorts in the middle. {@code display_only} (creative-
     * only / dev / deprecated) goes last — these populate the MISC.deep
     * pseudo-section without producing a separate island.
     */
    public static int frequencyRank(String frequency) {
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

    public static int rarityRank(String rarity) {
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
     * Layered cluster key used by {@link #WITHIN_ISLAND_COMPARATOR}. The
     * numeric prefix is what enforces ordering between zones — dyed
     * blocks at the top of an island, palette-toned items in the
     * middle, plain-id items at the bottom. See the class javadoc
     * for the full reasoning.
     */
    public static String clusterKey(String itemId, IslandSignalDescriptor descriptor) {
        if (itemId == null) {
            itemId = "";
        }
        String dye = descriptor == null ? null : descriptor.dyeColor();
        if (dye != null) {
            return "1dye:" + stripDyePrefix(itemId, dye);
        }
        String flavor = descriptor == null ? null : descriptor.flavor();
        char flavorChar = flavorRankChar(flavor);
        char originChar = originTierChar(descriptor == null ? null : descriptor.origin());
        List<String> palette = descriptor == null ? List.of() : descriptor.palette();
        if (palette != null && !palette.isEmpty()) {
            return "2pal:" + palette.get(0) + ":" + flavorChar + ":" + originChar;
        }
        return "3pln:" + flavorChar + ":" + originChar + ":" + itemId;
    }

    /**
     * Strip a dye-color prefix from the path portion of an item id,
     * leaving namespace + remainder. Used by the dye-cluster zone so
     * all 16 wools share a sort key.
     */
    public static String stripDyePrefix(String itemId, String dyeColor) {
        if (itemId == null) {
            return "";
        }
        if (dyeColor == null || dyeColor.isBlank()) {
            return itemId;
        }
        int colon = itemId.indexOf(':');
        if (colon < 0) {
            return itemId;
        }
        String namespace = itemId.substring(0, colon);
        String path = itemId.substring(colon + 1);
        String prefix = dyeColor + "_";
        if (path.startsWith(prefix)) {
            return namespace + ":" + path.substring(prefix.length());
        }
        return itemId;
    }

    /**
     * Canonical Minecraft dye order, mirroring the dye crafting recipe
     * panel. Used as the within-island secondary sort so a 16-cell
     * wool / carpet / concrete block reads as a dye wheel rather than
     * alphabetical noise (black, blue, brown, cyan, …).
     */
    public static int canonicalDyeIndex(String dyeColor) {
        if (dyeColor == null) {
            return Integer.MAX_VALUE;
        }
        Integer index = CANONICAL_DYE_ORDER.get(dyeColor);
        return index == null ? Integer.MAX_VALUE : index;
    }

    /**
     * Rank character for the {@code flavor} facet, mapping each
     * canonical value to a single letter so it slots cleanly into the
     * cluster key without breaking string ordering. The progression is
     * "what you start with" → "decorative variants" → "ancient /
     * special": plain → natural → variant → colored → fancy →
     * mechanical → mystical → ominous → ancient → (unflavored). Drives
     * the within-island sub-cluster so a BUILDING island lays out
     * plain stone bricks before chiseled / mossy / cracked variants.
     */
    public static char flavorRankChar(String flavor) {
        if (flavor == null) {
            return 'j';
        }
        return switch (flavor) {
            case "plain" -> 'a';
            case "natural" -> 'b';
            case "variant" -> 'c';
            case "colored" -> 'd';
            case "fancy" -> 'e';
            case "mechanical" -> 'f';
            case "mystical" -> 'g';
            case "ominous" -> 'h';
            case "ancient" -> 'i';
            default -> 'j';
        };
    }

    /**
     * Tier character for the {@code origin} facet, collapsing the
     * dataset's 27 distinct origin values into 5 progression bands so
     * it slots into the cluster key as a single sortable letter:
     * <ol>
     *   <li><b>'a' early</b> — overworld_surface, overworld_cave,
     *       overworld_ocean, crafted_only, village, mob_drop, fishing,
     *       trading.</li>
     *   <li><b>'b' mid</b> — overworld structures requiring exploration:
     *       mineshaft, desert_temple, jungle_temple, woodland_mansion,
     *       ruined_portal, pillager_outpost, ocean_monument, ancient_city,
     *       stronghold, archaeology_site, sniffer_garden, trial_chamber.</li>
     *   <li><b>'c' late</b> — nether + end progression: nether,
     *       nether_fortress, bastion, end, end_city, deep_dark.</li>
     *   <li><b>'y' unknown</b> — origin unset (null).</li>
     *   <li><b>'z' creative</b> — creative_only items (admin / dev /
     *       deprecated) sort at the bottom of every flavor band.</li>
     * </ol>
     */
    public static char originTierChar(String origin) {
        if (origin == null) {
            return 'y';
        }
        return switch (origin) {
            case "overworld_surface", "overworld_cave", "overworld_ocean",
                    "crafted_only", "village", "mob_drop",
                    "fishing", "trading" -> 'a';
            case "mineshaft", "desert_temple", "jungle_temple",
                    "woodland_mansion", "ruined_portal", "pillager_outpost",
                    "ocean_monument", "ancient_city", "stronghold",
                    "archaeology_site", "sniffer_garden", "trial_chamber" -> 'b';
            case "nether", "nether_fortress", "bastion",
                    "end", "end_city", "deep_dark" -> 'c';
            case "creative_only" -> 'z';
            default -> 'y';
        };
    }

    private static final Map<String, Integer> CANONICAL_DYE_ORDER;
    static {
        String[] order = {
                "white", "light_gray", "gray", "black",
                "brown", "red", "orange", "yellow",
                "lime", "green", "cyan", "light_blue",
                "blue", "purple", "magenta", "pink"
        };
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>(order.length);
        for (int i = 0; i < order.length; i++) {
            map.put(order[i], i);
        }
        CANONICAL_DYE_ORDER = Collections.unmodifiableMap(map);
    }
}
