package dev.imagio.slot.workflow.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record StorageAreaMap(
        List<StorageArea> areas
) {
    public static final UUID DEFAULT_AREA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String DEFAULT_AREA_LABEL = "Main Base";
    public static final int DEFAULT_AREA_COLOR = 0xCC5A4A6E;

    /**
     * Default palette new areas pick from (rotating by display order).
     * Picks the same eight muted hues player atlas islands already use,
     * so a base chip and its linked island read as related at a glance.
     */
    private static final int[] AREA_PALETTE = {
            0xCC5A4A6E, 0xCC7D5A3A, 0xCC5A6E3D, 0xCC6E3D3D,
            0xCC3D5A6E, 0xCC3D6E5A, 0xCC5A3D6E, 0xCC4E5A4A
    };

    public static int paletteColorFor(int displayOrder) {
        int normalized = ((displayOrder % AREA_PALETTE.length) + AREA_PALETTE.length) % AREA_PALETTE.length;
        return AREA_PALETTE[normalized];
    }

    public StorageAreaMap {
        areas = copyAreas(areas);
    }

    public static StorageAreaMap empty() {
        return new StorageAreaMap(List.of());
    }

    public static StorageArea defaultArea(int atlasX, int atlasY) {
        return new StorageArea(
                DEFAULT_AREA_ID,
                DEFAULT_AREA_LABEL,
                DEFAULT_AREA_COLOR,
                atlasX,
                atlasY,
                0
        );
    }

    public StorageArea area(UUID areaId) {
        if (areaId == null) {
            return null;
        }
        for (StorageArea area : areas) {
            if (area != null && areaId.equals(area.areaId())) {
                return area;
            }
        }
        return null;
    }

    public boolean contains(UUID areaId) {
        return area(areaId) != null;
    }

    public int nextDisplayOrder() {
        int max = -1;
        for (StorageArea area : areas) {
            if (area != null) {
                max = Math.max(max, area.displayOrder());
            }
        }
        return max + 1;
    }

    public static List<StorageArea> copyAreas(List<StorageArea> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        Map<UUID, StorageArea> uniqueById = new LinkedHashMap<>();
        for (StorageArea area : source) {
            if (area != null) {
                uniqueById.put(area.areaId(), area);
            }
        }
        return List.copyOf(new ArrayList<>(uniqueById.values()));
    }
}
