package dev.imagio.slot.debug;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.workflow.domain.VisualAtlasIsland;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeAssignment;
import dev.imagio.slot.workflow.domain.VisualHomeOrigin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class SyntheticHomedAtlasGenerator {
    public static final String SYNTHETIC_ISLAND_ID_PREFIX = "slot-test-island-";

    private static final int[] COLOR_PALETTE = new int[]{
            0xCC5A4A6E, 0xCC3C6E55, 0xCC6E5A3C, 0xCC3C4A6E,
            0xCC6E3C5A, 0xCC4A6E3C, 0xCC6E6E3C, 0xCC3C6E6E
    };

    private SyntheticHomedAtlasGenerator() {
    }

    public static SyntheticHomedAtlasPlan generate(
            List<ItemIdentity> identityPool,
            int identityCount,
            int islandCount,
            long seed,
            Config config
    ) {
        Objects.requireNonNull(config, "config");
        List<ItemIdentity> identities = IdentitySampler.sample(identityPool, identityCount, seed);
        int resolvedIslandCount = Math.max(1, islandCount);
        if (identities.isEmpty()) {
            return new SyntheticHomedAtlasPlan(List.of(), Map.of());
        }
        int itemsPerIsland = divCeil(identities.size(), resolvedIslandCount);
        int cardsPerRow = Math.max(1, config.cardsPerRow());
        int islandColumns = Math.max(1, config.islandColumns());

        ArrayList<VisualAtlasIsland> islands = new ArrayList<>(resolvedIslandCount);
        LinkedHashMap<ItemIdentity, VisualHomeAssignment> assignments = new LinkedHashMap<>(identities.size());

        for (int islandIndex = 0; islandIndex < resolvedIslandCount; islandIndex++) {
            int firstItem = islandIndex * itemsPerIsland;
            int lastItemExclusive = Math.min(firstItem + itemsPerIsland, identities.size());
            int itemsInIsland = Math.max(0, lastItemExclusive - firstItem);
            int rowsInIsland = Math.max(1, divCeil(itemsInIsland, cardsPerRow));

            int islandWidth = config.islandContentPaddingX() * 2
                    + cardsPerRow * config.cardWidth()
                    + Math.max(0, cardsPerRow - 1) * config.cardGap();
            int islandHeight = config.islandContentTop()
                    + rowsInIsland * config.cardHeight()
                    + Math.max(0, rowsInIsland - 1) * config.cardGap()
                    + config.islandContentPaddingY();

            int islandColumn = islandIndex % islandColumns;
            int islandRow = islandIndex / islandColumns;
            int originX = config.originX() + islandColumn * (islandWidth + config.islandGap());
            int originY = config.originY() + islandRow * (islandHeight + config.islandGap());

            String islandId = String.format(Locale.ROOT, "%s%02d", SYNTHETIC_ISLAND_ID_PREFIX, islandIndex + 1);
            String label = String.format(Locale.ROOT, "Test Island %d", islandIndex + 1);
            ItemIdentity iconIdentity = itemsInIsland > 0 ? identities.get(firstItem) : null;

            VisualAtlasIsland island = new VisualAtlasIsland(
                    islandId,
                    label,
                    VisualAtlasIslandKind.PLAYER,
                    originX,
                    originY,
                    islandWidth,
                    islandHeight,
                    COLOR_PALETTE[islandIndex % COLOR_PALETTE.length],
                    iconIdentity
            );
            islands.add(island);

            for (int itemIndex = 0; itemIndex < itemsInIsland; itemIndex++) {
                int column = itemIndex % cardsPerRow;
                int row = itemIndex / cardsPerRow;
                int localX = config.islandContentPaddingX()
                        + column * (config.cardWidth() + config.cardGap());
                int localY = config.islandContentTop()
                        + row * (config.cardHeight() + config.cardGap());
                ItemIdentity identity = identities.get(firstItem + itemIndex);
                assignments.put(identity, new VisualHomeAssignment(
                        identity,
                        islandId,
                        localX,
                        localY,
                        VisualHomeOrigin.PLAYER_PLACED,
                        true
                ));
            }
        }

        return new SyntheticHomedAtlasPlan(islands, assignments);
    }

    private static int divCeil(int numerator, int denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return (numerator + denominator - 1) / denominator;
    }

    public record Config(
            int cardWidth,
            int cardHeight,
            int cardGap,
            int cardsPerRow,
            int islandContentPaddingX,
            int islandContentPaddingY,
            int islandContentTop,
            int islandGap,
            int islandColumns,
            int originX,
            int originY
    ) {
        public static Config defaults() {
            return new Config(
                    32,
                    32,
                    4,
                    8,
                    14,
                    14,
                    56,
                    200,
                    4,
                    64,
                    64
            );
        }
    }
}
