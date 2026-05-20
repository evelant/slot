package dev.imagio.slot.ui.workspace;

import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shared presentation math for wayfinding direction/distance labels.
 *
 * <p>Inputs are plain coordinates and yaw so platform renderers can keep
 * Minecraft client objects at the boundary while sharing the actual display
 * semantics.
 */
public final class WayfindingDisplay {
    private WayfindingDisplay() {
    }

    public static CardText forStorage(
            String storageId,
            List<WayfindingTarget> targets,
            List<SlotWorkspaceViewModel.ChestChip> chestChips,
            String playerDimensionId,
            double playerX,
            double playerY,
            double playerZ,
            float playerYawDegrees
    ) {
        Location location = locationFor(storageId, targets, chestChips);
        if (location == null) {
            return CardText.unavailable();
        }
        return forLocation(
                location.dimensionId(),
                location.worldX(),
                location.worldY(),
                location.worldZ(),
                playerDimensionId,
                playerX,
                playerY,
                playerZ,
                playerYawDegrees);
    }

    public static CardText forLocation(
            String targetDimensionId,
            int worldX,
            int worldY,
            int worldZ,
            String playerDimensionId,
            double playerX,
            double playerY,
            double playerZ,
            float playerYawDegrees
    ) {
        if (targetDimensionId == null || targetDimensionId.isBlank()) {
            return CardText.unavailable();
        }
        if (!targetDimensionId.equals(playerDimensionId)) {
            return new CardText(shortDimension(targetDimensionId), "");
        }
        double dx = (worldX + 0.5) - playerX;
        double dy = (worldY + 0.5) - playerY;
        double dz = (worldZ + 0.5) - playerZ;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float yawRadians = (float) Math.toRadians(playerYawDegrees);
        double absoluteBearing = Math.atan2(-dx, dz);
        double relativeBearing = absoluteBearing - yawRadians;
        return new CardText(arrowGlyph(relativeBearing), Math.max(0, Math.round(distance)) + "m");
    }

    public static String arrowGlyph(double relativeBearing) {
        double normalized = ((relativeBearing % (Math.PI * 2)) + Math.PI * 2) % (Math.PI * 2);
        int sector = (int) Math.floor((normalized + Math.PI / 8.0) / (Math.PI / 4.0)) % 8;
        return switch (sector) {
            case 0 -> "↑";
            case 1 -> "↗";
            case 2 -> "→";
            case 3 -> "↘";
            case 4 -> "↓";
            case 5 -> "↙";
            case 6 -> "←";
            case 7 -> "↖";
            default -> "·";
        };
    }

    public static String shortDimension(String dimensionId) {
        if (dimensionId == null) {
            return "";
        }
        int colon = dimensionId.indexOf(':');
        String tail = colon < 0 ? dimensionId : dimensionId.substring(colon + 1);
        if (tail.startsWith("the_")) {
            tail = tail.substring(4);
        }
        return tail;
    }

    public static HudTargets splitTargets(
            List<WayfindingTarget> targets,
            String playerDimensionId,
            double playerX,
            double playerY,
            double playerZ
    ) {
        if (targets == null || targets.isEmpty()) {
            return new HudTargets(List.of(), List.of());
        }
        ArrayList<RankedTarget> here = new ArrayList<>();
        ArrayList<WayfindingTarget> elsewhere = new ArrayList<>();
        for (WayfindingTarget target : targets) {
            if (target == null) {
                continue;
            }
            if (target.dimensionId().equals(playerDimensionId)) {
                double dx = (target.worldX() + 0.5) - playerX;
                double dy = (target.worldY() + 0.5) - playerY;
                double dz = (target.worldZ() + 0.5) - playerZ;
                here.add(new RankedTarget(target, dx * dx + dy * dy + dz * dz));
            } else {
                elsewhere.add(target);
            }
        }
        here.sort(Comparator.comparingDouble(RankedTarget::distSq));
        return new HudTargets(here, elsewhere);
    }

    public static String chestLabel(WayfindingTarget target) {
        String storageId = target == null ? "" : target.storageId();
        if (storageId == null || storageId.isBlank()) {
            return "Chest";
        }
        var displayTarget = WorldDisplayStorageSource.targetFromStorageId(storageId);
        if (displayTarget.isPresent()) {
            return displayLabel(displayTarget.get().kind());
        }
        int dash = storageId.indexOf('-');
        String shortId = dash < 0 ? storageId : storageId.substring(0, dash);
        if (shortId.length() > 4) {
            shortId = shortId.substring(shortId.length() - 4);
        }
        return "Chest #" + shortId;
    }

    public static String targetLabel(WayfindingTarget target) {
        String label = chestLabel(target);
        return target != null && target.putAwayOnly() ? "Put away: " + label : label;
    }

    private static String displayLabel(WorldDisplayStorageKind kind) {
        return switch (kind) {
            case TOOL_RACK -> "Tool rack";
            case PLACED_ITEM -> "Placed item";
        };
    }

    private static Location locationFor(
            String storageId,
            List<WayfindingTarget> targets,
            List<SlotWorkspaceViewModel.ChestChip> chestChips
    ) {
        if (storageId == null || storageId.isBlank()) {
            return null;
        }
        if (targets != null) {
            for (WayfindingTarget target : targets) {
                if (target != null && storageId.equals(target.storageId())) {
                    return new Location(
                            target.dimensionId(),
                            target.worldX(),
                            target.worldY(),
                            target.worldZ());
                }
            }
        }
        if (chestChips != null) {
            for (SlotWorkspaceViewModel.ChestChip chip : chestChips) {
                if (chip != null && storageId.equals(chip.storageId())) {
                    return new Location(
                            chip.dimensionId(),
                            chip.worldX(),
                            chip.worldY(),
                            chip.worldZ());
                }
            }
        }
        return null;
    }

    public record CardText(String arrow, String distance) {
        public CardText {
            arrow = arrow == null ? "" : arrow;
            distance = distance == null ? "" : distance;
        }

        public static CardText unavailable() {
            return new CardText("·", "--m");
        }
    }

    public record HudTargets(List<RankedTarget> here, List<WayfindingTarget> elsewhere) {
        public HudTargets {
            here = here == null ? List.of() : List.copyOf(here);
            elsewhere = elsewhere == null ? List.of() : List.copyOf(elsewhere);
        }
    }

    public record RankedTarget(WayfindingTarget target, double distSq) {
    }

    private record Location(String dimensionId, int worldX, int worldY, int worldZ) {
    }
}
