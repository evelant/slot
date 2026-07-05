package dev.imagio.slot.forge.network;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.inventory.workspace.WorkspaceViewSliceKeys;
import dev.imagio.slot.inventory.workspace.WorkspaceViewTransferMode;
import dev.imagio.slot.inventory.workspace.WorkspaceViewTransferPlan;
import dev.imagio.slot.inventory.workspace.WorkspaceViewTransferSlice;
import dev.imagio.slot.workflow.domain.ChestRole;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import dev.imagio.slot.workflow.domain.WorkflowAcceptedInputRule;
import dev.imagio.slot.workflow.domain.CraftRunAlternative;
import dev.imagio.slot.workflow.domain.CraftRunIngredientGroup;
import dev.imagio.slot.workflow.domain.CraftRunRecipeCapture;
import dev.imagio.slot.workflow.domain.CraftRunRecipeEntry;
import dev.imagio.slot.workflow.domain.CraftRunState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class Forge120WorkspaceViewModelCodec {
    private static final String TRANSFER_MODE = "__slotViewMode";
    private static final String TRANSFER_BASE_REVISION = "__slotBaseRevision";
    private static final String TRANSFER_REVISION = "__slotRevision";
    private static final String TRANSFER_SLICES = "__slotSlices";

    private Forge120WorkspaceViewModelCodec() {
    }

    public static CompoundTag encode(SlotWorkspaceViewModel viewModel) {
        return encode(viewModel, true);
    }

    public static CompoundTag encode(SlotWorkspaceViewModel viewModel, boolean includeRevision) {
        return encode(viewModel, includeRevision, null);
    }

    public static CompoundTag encode(SlotWorkspaceViewModel viewModel, EncodedSliceCache cache) {
        return encode(viewModel, true, cache);
    }

    public static CompoundTag encode(
            SlotWorkspaceViewModel viewModel,
            boolean includeRevision,
            EncodedSliceCache cache
    ) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        if (cache == null) {
            return encodeFresh(resolved, includeRevision);
        }
        WorkspaceViewSliceKeys keys = WorkspaceViewSliceKeys.from(resolved);
        CompoundTag tag = new CompoundTag();
        if (includeRevision) {
            tag.putLong("revision", resolved.revision());
        }
        CompoundTag previousTag = cache.lastTag;
        WorkspaceViewSliceKeys previousKeys = cache.lastKeys;
        if (previousTag == null || previousKeys == null) {
            writeAllSlices(tag, resolved);
            cache.store(keys, tag, new SliceStats(7, 0));
            return tag;
        }
        int encoded = 0;
        int reused = 0;
        if (copyOrWrite(tag, previousTag, previousKeys.frame(), keys.frame(), () -> writeFrame(tag, resolved),
                "status", "diagnostics", "pendingCount", "selectedQuickAccessSlot")) {
            reused++;
        } else {
            encoded++;
        }
        if (copyOrWrite(tag, previousTag, previousKeys.wall(), keys.wall(), () -> writeWall(tag, resolved),
                "canvasWidth", "canvasHeight", "carriedFreeSlotCount", "carriedSlotCapacity",
                "islands", "atlasItems", "triageItems")) {
            reused++;
        } else {
            encoded++;
        }
        if (copyOrWrite(tag, previousTag, previousKeys.storage(), keys.storage(), () -> writeStorage(tag, resolved),
                "chestChips", "chestClusters", "wayfindingTargets", "depositableIdentities")) {
            reused++;
        } else {
            encoded++;
        }
        if (copyOrWrite(tag, previousTag, previousKeys.hotbar(), keys.hotbar(), () -> writeHotbar(tag, resolved),
                "recentIdentities", "hotbarSlots", "offhand")) {
            reused++;
        } else {
            encoded++;
        }
        if (copyOrWrite(tag, previousTag, previousKeys.workflow(), keys.workflow(), () -> writeWorkflow(tag, resolved),
                "kits", "craftRun")) {
            reused++;
        } else {
            encoded++;
        }
        if (copyOrWrite(tag, previousTag, previousKeys.panels(), keys.panels(), () -> writePanels(tag, resolved),
                "lootChestPanel", "activeChestPanel")) {
            reused++;
        } else {
            encoded++;
        }
        if (copyOrWrite(tag, previousTag, previousKeys.contextual(), keys.contextual(), () -> writeContextual(tag, resolved),
                "contextualSuggestionLanes")) {
            reused++;
        } else {
            encoded++;
        }
        cache.store(keys, tag, new SliceStats(encoded, reused));
        return tag;
    }

    public static CompoundTag encodeTransfer(
            SlotWorkspaceViewModel viewModel,
            EncodedSliceCache cache,
            boolean forceFull
    ) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        if (cache == null) {
            CompoundTag full = encodeFresh(resolved, true);
            markTransfer(full, WorkspaceViewTransferPlan.full(resolved.revision()));
            return full;
        }
        WorkspaceViewSliceKeys previousKeys = cache.lastKeys;
        CompoundTag previousTag = cache.lastTag;
        long previousRevision = cache.lastRevision;
        WorkspaceViewSliceKeys keys = WorkspaceViewSliceKeys.from(resolved);
        WorkspaceViewTransferPlan plan = WorkspaceViewTransferPlan.from(
                previousRevision,
                previousKeys,
                resolved.revision(),
                keys,
                forceFull || previousTag == null);
        if (plan.mode() == WorkspaceViewTransferMode.FULL_SNAPSHOT) {
            CompoundTag full = encodeFresh(resolved, true);
            cache.store(keys, full, new SliceStats(7, 0));
            CompoundTag transfer = full.copy();
            markTransfer(transfer, plan);
            return transfer;
        }

        CompoundTag delta = new CompoundTag();
        markTransfer(delta, plan);
        CompoundTag merged = previousTag.copy();
        merged.putLong("revision", resolved.revision());
        int encoded = 0;
        for (WorkspaceViewTransferSlice slice : plan.slices()) {
            writeSlice(delta, resolved, slice);
            copyKeys(delta, merged, keysForSlice(slice));
            encoded++;
        }
        cache.store(keys, merged, new SliceStats(encoded, 7 - encoded));
        return delta;
    }

    public static TransferApplyResult applyTransfer(CompoundTag previousFullTag, Tag transferTag) {
        if (!(transferTag instanceof CompoundTag transfer)) {
            return TransferApplyResult.failed("missing_transfer_tag", true);
        }
        WorkspaceViewTransferMode mode = transferMode(transfer);
        if (mode == WorkspaceViewTransferMode.FULL_SNAPSHOT) {
            CompoundTag full = transfer.copy();
            return TransferApplyResult.applied(full, decode(full));
        }
        if (previousFullTag == null) {
            return TransferApplyResult.failed("missing_delta_base", true);
        }
        long expectedBase = transfer.getLong(TRANSFER_BASE_REVISION);
        long actualBase = previousFullTag.getLong("revision");
        if (expectedBase != actualBase) {
            long transferRevision = transfer.getLong(TRANSFER_REVISION);
            if (actualBase >= transferRevision) {
                CompoundTag full = previousFullTag.copy();
                return TransferApplyResult.applied(full, decode(full));
            }
            return TransferApplyResult.failed(
                    "delta_base_mismatch:expected=" + expectedBase + ":actual=" + actualBase,
                    true);
        }
        EnumSet<WorkspaceViewTransferSlice> slices = transferSlices(transfer);
        if (slices.isEmpty()) {
            return TransferApplyResult.failed("delta_has_no_slices", true);
        }
        CompoundTag merged = previousFullTag.copy();
        merged.putLong("revision", transfer.getLong(TRANSFER_REVISION));
        for (WorkspaceViewTransferSlice slice : slices) {
            String[] keys = keysForSlice(slice);
            if (!copyKeys(transfer, merged, keys)) {
                return TransferApplyResult.failed("delta_missing_slice:" + slice.name(), true);
            }
        }
        return TransferApplyResult.applied(merged, decode(merged));
    }

    private static CompoundTag encodeFresh(SlotWorkspaceViewModel resolved, boolean includeRevision) {
        CompoundTag tag = new CompoundTag();
        if (includeRevision) {
            tag.putLong("revision", resolved.revision());
        }
        writeAllSlices(tag, resolved);
        return tag;
    }

    private static void writeAllSlices(CompoundTag tag, SlotWorkspaceViewModel resolved) {
        writeFrame(tag, resolved);
        writeWall(tag, resolved);
        writeStorage(tag, resolved);
        writeHotbar(tag, resolved);
        writeWorkflow(tag, resolved);
        writePanels(tag, resolved);
        writeContextual(tag, resolved);
    }

    private static void writeSlice(
            CompoundTag tag,
            SlotWorkspaceViewModel resolved,
            WorkspaceViewTransferSlice slice
    ) {
        switch (slice) {
            case FRAME -> writeFrame(tag, resolved);
            case WALL -> writeWall(tag, resolved);
            case STORAGE -> writeStorage(tag, resolved);
            case HOTBAR -> writeHotbar(tag, resolved);
            case WORKFLOW -> writeWorkflow(tag, resolved);
            case PANELS -> writePanels(tag, resolved);
            case CONTEXTUAL -> writeContextual(tag, resolved);
        }
    }

    private static void writeFrame(CompoundTag tag, SlotWorkspaceViewModel resolved) {
        tag.putString("status", resolved.status());
        tag.putString("diagnostics", resolved.diagnostics());
        tag.putInt("pendingCount", resolved.pendingCount());
        tag.putInt("selectedQuickAccessSlot", resolved.selectedQuickAccessSlot());
    }

    private static void writeWall(CompoundTag tag, SlotWorkspaceViewModel resolved) {
        tag.putInt("canvasWidth", resolved.canvasWidth());
        tag.putInt("canvasHeight", resolved.canvasHeight());
        tag.putInt("carriedFreeSlotCount", resolved.carriedFreeSlotCount());
        tag.putInt("carriedSlotCapacity", resolved.carriedSlotCapacity());

        ListTag islands = new ListTag();
        for (SlotWorkspaceViewModel.AtlasIsland island : resolved.islands()) {
            islands.add(encodeIsland(island));
        }
        tag.put("islands", islands);

        ListTag items = new ListTag();
        for (SlotWorkspaceViewModel.AtlasItem item : resolved.atlasItems()) {
            items.add(encodeItem(item));
        }
        tag.put("atlasItems", items);

        ListTag triageItems = new ListTag();
        for (SlotWorkspaceViewModel.AtlasItem item : resolved.triageItems()) {
            triageItems.add(encodeItem(item));
        }
        tag.put("triageItems", triageItems);
    }

    private static void writeStorage(CompoundTag tag, SlotWorkspaceViewModel resolved) {
        ListTag chestChips = new ListTag();
        for (SlotWorkspaceViewModel.ChestChip chip : resolved.chestChips()) {
            CompoundTag chipTag = encodeChestChip(chip);
            writeChestChipContents(chipTag, chip);
            chestChips.add(chipTag);
        }
        tag.put("chestChips", chestChips);

        ListTag chestClusters = new ListTag();
        for (SlotWorkspaceViewModel.ChestClusterDescriptor cluster : resolved.chestClusters()) {
            chestClusters.add(encodeChestCluster(cluster));
        }
        tag.put("chestClusters", chestClusters);

        ListTag wayfindingTargets = new ListTag();
        for (WayfindingTarget target : resolved.wayfindingTargets()) {
            wayfindingTargets.add(encodeWayfindingTarget(target));
        }
        tag.put("wayfindingTargets", wayfindingTargets);

        ListTag depositableIdentities = new ListTag();
        for (SlotWorkspaceViewModel.IdentityRef identity : resolved.depositableIdentities()) {
            depositableIdentities.add(encodeIdentity(identity));
        }
        tag.put("depositableIdentities", depositableIdentities);
    }

    private static void writeHotbar(CompoundTag tag, SlotWorkspaceViewModel resolved) {
        ListTag recents = new ListTag();
        for (SlotWorkspaceViewModel.IdentityRef identity : resolved.recentIdentities()) {
            recents.add(encodeIdentity(identity));
        }
        tag.put("recentIdentities", recents);

        ListTag hotbar = new ListTag();
        for (SlotWorkspaceViewModel.HotbarSlot slot : resolved.hotbarSlots()) {
            hotbar.add(encodeHotbarSlot(slot));
        }
        tag.put("hotbarSlots", hotbar);
        tag.put("offhand", encodeOffhand(resolved.offhand()));
    }

    private static void writeWorkflow(CompoundTag tag, SlotWorkspaceViewModel resolved) {
        ListTag kits = new ListTag();
        for (SlotWorkspaceViewModel.KitCard card : resolved.kits()) {
            kits.add(encodeKitCard(card));
        }
        tag.put("kits", kits);
        tag.put("craftRun", encodeCraftRunState(resolved.craftRun()));
    }

    private static void writePanels(CompoundTag tag, SlotWorkspaceViewModel resolved) {
        tag.put("lootChestPanel", encodeLootChestPanel(resolved.lootChestPanel()));
        tag.put("activeChestPanel", encodeActiveChestPanel(resolved.activeChestPanel()));
    }

    private static void writeContextual(CompoundTag tag, SlotWorkspaceViewModel resolved) {
        tag.put("contextualSuggestionLanes", encodeSuggestionLanes(resolved.contextualSuggestionLanes()));
    }

    private static boolean copyOrWrite(
            CompoundTag target,
            CompoundTag previous,
            String previousKey,
            String currentKey,
            SliceWriter writer,
            String... topLevelKeys
    ) {
        if (previousKey != null && previousKey.equals(currentKey) && copyKeys(previous, target, topLevelKeys)) {
            return true;
        }
        writer.write();
        return false;
    }

    private static boolean copyKeys(CompoundTag source, CompoundTag target, String... keys) {
        if (source == null || target == null || keys == null) {
            return false;
        }
        for (String key : keys) {
            if (!hasKnownKey(source, key)) {
                return false;
            }
        }
        for (String key : keys) {
            copyKnownKey(source, target, key);
        }
        return true;
    }

    private static boolean hasKnownKey(CompoundTag source, String key) {
        if (source == null || key == null) {
            return false;
        }
        return switch (key) {
            case "status", "diagnostics" -> source.contains(key, Tag.TAG_STRING);
            case "pendingCount", "selectedQuickAccessSlot", "canvasWidth", "canvasHeight",
                    "carriedFreeSlotCount", "carriedSlotCapacity" -> source.contains(key, Tag.TAG_INT);
            case "islands", "atlasItems", "triageItems", "chestChips", "chestClusters",
                    "wayfindingTargets", "depositableIdentities", "recentIdentities", "hotbarSlots",
                    "kits", "contextualSuggestionLanes" -> source.contains(key, Tag.TAG_LIST);
            case "offhand", "craftRun", "lootChestPanel", "activeChestPanel" ->
                    source.contains(key, Tag.TAG_COMPOUND);
            default -> false;
        };
    }

    private static void copyKnownKey(CompoundTag source, CompoundTag target, String key) {
        switch (key) {
            case "status", "diagnostics" -> target.putString(key, source.getString(key));
            case "pendingCount", "selectedQuickAccessSlot", "canvasWidth", "canvasHeight",
                    "carriedFreeSlotCount", "carriedSlotCapacity" -> target.putInt(key, source.getInt(key));
            case "islands", "atlasItems", "triageItems", "chestChips", "chestClusters",
                    "wayfindingTargets", "depositableIdentities", "recentIdentities", "hotbarSlots",
                    "kits", "contextualSuggestionLanes" -> target.put(key, source.getList(key, Tag.TAG_COMPOUND).copy());
            case "offhand", "craftRun", "lootChestPanel", "activeChestPanel" ->
                    target.put(key, source.getCompound(key).copy());
            default -> {
            }
        }
    }

    private static void markTransfer(CompoundTag tag, WorkspaceViewTransferPlan plan) {
        WorkspaceViewTransferPlan resolved = plan == null
                ? WorkspaceViewTransferPlan.full(tag == null ? 0L : tag.getLong("revision"))
                : plan;
        tag.putString(TRANSFER_MODE, resolved.mode().name());
        tag.putLong(TRANSFER_BASE_REVISION, resolved.baseRevision());
        tag.putLong(TRANSFER_REVISION, resolved.revision());
        tag.putString(TRANSFER_SLICES, sliceList(resolved.slices()));
    }

    private static WorkspaceViewTransferMode transferMode(CompoundTag tag) {
        if (tag == null || !tag.contains(TRANSFER_MODE)) {
            return WorkspaceViewTransferMode.FULL_SNAPSHOT;
        }
        try {
            return WorkspaceViewTransferMode.valueOf(tag.getString(TRANSFER_MODE));
        } catch (IllegalArgumentException ignored) {
            return WorkspaceViewTransferMode.FULL_SNAPSHOT;
        }
    }

    private static String sliceList(EnumSet<WorkspaceViewTransferSlice> slices) {
        if (slices == null || slices.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (WorkspaceViewTransferSlice slice : slices) {
            if (!out.isEmpty()) {
                out.append(',');
            }
            out.append(slice.name());
        }
        return out.toString();
    }

    private static EnumSet<WorkspaceViewTransferSlice> transferSlices(CompoundTag tag) {
        EnumSet<WorkspaceViewTransferSlice> slices = EnumSet.noneOf(WorkspaceViewTransferSlice.class);
        if (tag == null) {
            return slices;
        }
        String raw = tag.getString(TRANSFER_SLICES);
        if (raw == null || raw.isBlank()) {
            return slices;
        }
        for (String part : raw.split(",")) {
            if (part == null || part.isBlank()) {
                continue;
            }
            try {
                slices.add(WorkspaceViewTransferSlice.valueOf(part.trim()));
            } catch (IllegalArgumentException ignored) {
                return EnumSet.noneOf(WorkspaceViewTransferSlice.class);
            }
        }
        return slices;
    }

    private static String[] keysForSlice(WorkspaceViewTransferSlice slice) {
        return switch (slice) {
            case FRAME -> new String[]{"status", "diagnostics", "pendingCount", "selectedQuickAccessSlot"};
            case WALL -> new String[]{
                    "canvasWidth", "canvasHeight", "carriedFreeSlotCount", "carriedSlotCapacity",
                    "islands", "atlasItems", "triageItems"};
            case STORAGE -> new String[]{"chestChips", "chestClusters", "wayfindingTargets", "depositableIdentities"};
            case HOTBAR -> new String[]{"recentIdentities", "hotbarSlots", "offhand"};
            case WORKFLOW -> new String[]{"kits", "craftRun"};
            case PANELS -> new String[]{"lootChestPanel", "activeChestPanel"};
            case CONTEXTUAL -> new String[]{"contextualSuggestionLanes"};
        };
    }

    @FunctionalInterface
    private interface SliceWriter {
        void write();
    }

    public static final class EncodedSliceCache {
        private CompoundTag lastTag;
        private WorkspaceViewSliceKeys lastKeys;
        private long lastRevision;
        private SliceStats lastStats = SliceStats.empty();

        public SliceStats lastStats() {
            return lastStats;
        }

        public void clear() {
            lastTag = null;
            lastKeys = null;
            lastRevision = 0L;
            lastStats = SliceStats.empty();
        }

        private void store(WorkspaceViewSliceKeys keys, CompoundTag tag, SliceStats stats) {
            lastKeys = keys;
            lastTag = tag == null ? null : tag.copy();
            lastRevision = tag == null ? 0L : Math.max(0L, tag.getLong("revision"));
            lastStats = stats == null ? SliceStats.empty() : stats;
        }
    }

    public record SliceStats(int encodedSlices, int reusedSlices) {
        public static SliceStats empty() {
            return new SliceStats(0, 0);
        }
    }

    public record TransferApplyResult(
            boolean applied,
            boolean requiresFullSnapshot,
            CompoundTag fullTag,
            SlotWorkspaceViewModel viewModel,
            String diagnostics
    ) {
        private static TransferApplyResult applied(CompoundTag fullTag, SlotWorkspaceViewModel viewModel) {
            return new TransferApplyResult(
                    true,
                    false,
                    fullTag == null ? null : fullTag.copy(),
                    viewModel,
                    "");
        }

        private static TransferApplyResult failed(String diagnostics, boolean requiresFullSnapshot) {
            return new TransferApplyResult(
                    false,
                    requiresFullSnapshot,
                    null,
                    SlotWorkspaceViewModel.empty(),
                    diagnostics);
        }

        public TransferApplyResult {
            fullTag = fullTag == null ? null : fullTag.copy();
            viewModel = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
            diagnostics = diagnostics == null ? "" : diagnostics;
        }
    }

    public static SlotWorkspaceViewModel decode(Tag tag) {
        if (!(tag instanceof CompoundTag compound)) {
            return SlotWorkspaceViewModel.empty();
        }

        ArrayList<SlotWorkspaceViewModel.AtlasIsland> islands = new ArrayList<>();
        ListTag islandTags = compound.getList("islands", Tag.TAG_COMPOUND);
        for (int index = 0; index < islandTags.size(); index++) {
            islands.add(decodeIsland(islandTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        ListTag itemTags = compound.getList("atlasItems", Tag.TAG_COMPOUND);
        for (int index = 0; index < itemTags.size(); index++) {
            items.add(decodeItem(itemTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.AtlasItem> triageItems = new ArrayList<>();
        ListTag triageTags = compound.getList("triageItems", Tag.TAG_COMPOUND);
        for (int index = 0; index < triageTags.size(); index++) {
            triageItems.add(decodeItem(triageTags.getCompound(index)));
        }
        List<SlotWorkspaceViewModel.ContextualSuggestionLane> contextualSuggestionLanes =
                decodeSuggestionLanes(compound);

        ArrayList<SlotWorkspaceViewModel.ChestChip> chestChips = new ArrayList<>();
        ListTag chipTags = compound.getList("chestChips", Tag.TAG_COMPOUND);
        for (int index = 0; index < chipTags.size(); index++) {
            CompoundTag chipTag = chipTags.getCompound(index);
            SlotWorkspaceViewModel.ChestChip chip = decodeChestChip(chipTag);
            chestChips.add(new SlotWorkspaceViewModel.ChestChip(
                    chip.storageId(),
                    chip.dimensionId(),
                    chip.label(),
                    chip.anchorCount(),
                    chip.slotCapacity(),
                    chip.filledSlots(),
                    chip.proximate(),
                    chip.affinityIdentities(),
                    chip.worldX(),
                    chip.worldY(),
                    chip.worldZ(),
                    chip.clusterId(),
                    readChestChipContents(chipTag)
            ));
        }

        ArrayList<SlotWorkspaceViewModel.ChestClusterDescriptor> chestClusters = new ArrayList<>();
        ListTag clusterTags = compound.getList("chestClusters", Tag.TAG_COMPOUND);
        for (int index = 0; index < clusterTags.size(); index++) {
            chestClusters.add(decodeChestCluster(clusterTags.getCompound(index)));
        }

        SlotWorkspaceViewModel.LootChestPanel lootChestPanel =
                decodeLootChestPanel(compound.getCompound("lootChestPanel"));

        ArrayList<SlotWorkspaceViewModel.IdentityRef> recents = new ArrayList<>();
        ListTag recentTags = compound.getList("recentIdentities", Tag.TAG_COMPOUND);
        for (int index = 0; index < recentTags.size(); index++) {
            recents.add(decodeIdentity(recentTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.HotbarSlot> hotbar = new ArrayList<>();
        ListTag hotbarTags = compound.getList("hotbarSlots", Tag.TAG_COMPOUND);
        for (int index = 0; index < hotbarTags.size(); index++) {
            hotbar.add(decodeHotbarSlot(hotbarTags.getCompound(index)));
        }
        SlotWorkspaceViewModel.OffhandSlot offhand = compound.contains("offhand", Tag.TAG_COMPOUND)
                ? decodeOffhand(compound.getCompound("offhand"))
                : SlotWorkspaceViewModel.OffhandSlot.empty();

        ArrayList<SlotWorkspaceViewModel.KitCard> kits = new ArrayList<>();
        ListTag kitTags = compound.getList("kits", Tag.TAG_COMPOUND);
        for (int index = 0; index < kitTags.size(); index++) {
            kits.add(decodeKitCard(kitTags.getCompound(index)));
        }

        ArrayList<WayfindingTarget> wayfindingTargets = new ArrayList<>();
        ListTag wayfindingTags = compound.getList("wayfindingTargets", Tag.TAG_COMPOUND);
        for (int index = 0; index < wayfindingTags.size(); index++) {
            wayfindingTargets.add(decodeWayfindingTarget(wayfindingTags.getCompound(index)));
        }

        LinkedHashSet<SlotWorkspaceViewModel.IdentityRef> depositableIdentities = new LinkedHashSet<>();
        ListTag depositableTags = compound.getList("depositableIdentities", Tag.TAG_COMPOUND);
        for (int index = 0; index < depositableTags.size(); index++) {
            depositableIdentities.add(decodeIdentity(depositableTags.getCompound(index)));
        }

        SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel =
                decodeActiveChestPanel(compound.getCompound("activeChestPanel"));
        CraftRunState craftRun = decodeCraftRunState(compound.getCompound("craftRun"));

        return new SlotWorkspaceViewModel(
                compound.getLong("revision"),
                compound.getString("status"),
                compound.getString("diagnostics"),
                compound.getInt("pendingCount"),
                compound.getInt("selectedQuickAccessSlot"),
                compound.getInt("canvasWidth"),
                compound.getInt("canvasHeight"),
                compound.getInt("carriedFreeSlotCount"),
                compound.getInt("carriedSlotCapacity"),
                islands.isEmpty()
                        ? SlotWorkspaceAtlasLayout.baseIslands(VisualHomeMap.empty())
                        : islands,
                items,
                triageItems,
                chestChips,
                chestClusters,
                hotbar.isEmpty() ? SlotWorkspaceViewModel.emptyHotbar() : hotbar,
                offhand,
                kits,
                lootChestPanel,
                wayfindingTargets,
                depositableIdentities,
                recents,
                activeChestPanel,
                craftRun,
                contextualSuggestionLanes
        );
    }

    private static ListTag encodeSuggestionLanes(List<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes) {
        ListTag tags = new ListTag();
        if (lanes == null || lanes.isEmpty()) {
            return tags;
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionLane lane : lanes) {
            if (lane == null || !lane.displayable()) {
                continue;
            }
            CompoundTag tag = new CompoundTag();
            tag.putString("id", lane.id());
            tag.putString("label", lane.label());
            if (!lane.placeholderText().isBlank()) {
                tag.putString("placeholderText", lane.placeholderText());
            }
            ListTag itemTags = new ListTag();
            for (SlotWorkspaceViewModel.AtlasItem item : lane.items()) {
                itemTags.add(encodeItem(item));
            }
            tag.put("items", itemTags);
            tag.put("debugInfo", encodeSuggestionDebugInfo(lane.debugInfo()));
            tags.add(tag);
        }
        return tags;
    }

    private static ListTag encodeSuggestionDebugInfo(
            List<SlotWorkspaceViewModel.ContextualSuggestionDebugInfo> debugInfo
    ) {
        ListTag tags = new ListTag();
        if (debugInfo == null || debugInfo.isEmpty()) {
            return tags;
        }
        for (SlotWorkspaceViewModel.ContextualSuggestionDebugInfo info : debugInfo) {
            if (info == null) {
                continue;
            }
            CompoundTag tag = new CompoundTag();
            tag.put("identity", encodeIdentity(info.identity()));
            tag.putDouble("score", info.score());
            tag.putDouble("relevance", info.relevance());
            ListTag reasons = new ListTag();
            for (String reason : info.reasons()) {
                if (reason == null || reason.isBlank()) {
                    continue;
                }
                CompoundTag reasonTag = new CompoundTag();
                reasonTag.putString("text", reason);
                reasons.add(reasonTag);
            }
            tag.put("reasons", reasons);
            tags.add(tag);
        }
        return tags;
    }

    private static List<SlotWorkspaceViewModel.ContextualSuggestionLane> decodeSuggestionLanes(CompoundTag compound) {
        ArrayList<SlotWorkspaceViewModel.ContextualSuggestionLane> lanes = new ArrayList<>();
        if (compound == null) {
            return lanes;
        }
        ListTag laneTags = compound.getList("contextualSuggestionLanes", Tag.TAG_COMPOUND);
        for (int laneIndex = 0; laneIndex < laneTags.size(); laneIndex++) {
            CompoundTag laneTag = laneTags.getCompound(laneIndex);
            ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
            ListTag itemTags = laneTag.getList("items", Tag.TAG_COMPOUND);
            for (int itemIndex = 0; itemIndex < itemTags.size(); itemIndex++) {
                items.add(decodeItem(itemTags.getCompound(itemIndex)));
            }
            lanes.add(new SlotWorkspaceViewModel.ContextualSuggestionLane(
                    laneTag.getString("id"),
                    laneTag.getString("label"),
                    items,
                    laneTag.getString("placeholderText"),
                    decodeSuggestionDebugInfo(laneTag)));
        }
        return lanes;
    }

    private static List<SlotWorkspaceViewModel.ContextualSuggestionDebugInfo> decodeSuggestionDebugInfo(
            CompoundTag laneTag
    ) {
        ArrayList<SlotWorkspaceViewModel.ContextualSuggestionDebugInfo> result = new ArrayList<>();
        if (laneTag == null) {
            return result;
        }
        ListTag tags = laneTag.getList("debugInfo", Tag.TAG_COMPOUND);
        for (int index = 0; index < tags.size(); index++) {
            CompoundTag tag = tags.getCompound(index);
            ArrayList<String> reasons = new ArrayList<>();
            ListTag reasonTags = tag.getList("reasons", Tag.TAG_COMPOUND);
            for (int reasonIndex = 0; reasonIndex < reasonTags.size(); reasonIndex++) {
                String reason = reasonTags.getCompound(reasonIndex).getString("text");
                if (!reason.isBlank()) {
                    reasons.add(reason);
                }
            }
            result.add(new SlotWorkspaceViewModel.ContextualSuggestionDebugInfo(
                    decodeIdentity(tag.getCompound("identity")),
                    tag.getDouble("score"),
                    tag.getDouble("relevance"),
                    reasons));
        }
        return result;
    }

    public static CompoundTag encodeCraftRunRecipeCapture(CraftRunRecipeCapture capture) {
        CompoundTag tag = new CompoundTag();
        CraftRunRecipeCapture resolved = capture == null ? CraftRunRecipeCapture.empty() : capture;
        tag.putString("sourceKey", resolved.sourceKey());
        tag.putString("recipeId", resolved.recipeId());
        tag.putString("label", resolved.label());
        if (resolved.outputIdentity() != null) {
            tag.put("outputIdentity", encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(resolved.outputIdentity())));
        }
        tag.putString("outputLabel", resolved.outputLabel());
        tag.putInt("outputCountPerBatch", resolved.outputCountPerBatch());
        tag.putInt("remainingOutputCount", resolved.remainingOutputCount());
        tag.put("inputs", encodeCraftRunIngredientGroups(resolved.inputs()));
        tag.put("diagnostics", encodeStrings(resolved.diagnostics()));
        return tag;
    }

    public static CraftRunRecipeCapture decodeCraftRunRecipeCapture(CompoundTag tag) {
        if (tag == null) {
            return CraftRunRecipeCapture.empty();
        }
        ItemIdentity outputIdentity = tag.contains("outputIdentity", Tag.TAG_COMPOUND)
                ? decodeIdentity(tag.getCompound("outputIdentity")).toIdentity()
                : null;
        return new CraftRunRecipeCapture(
                tag.getString("sourceKey"),
                tag.getString("recipeId"),
                tag.getString("label"),
                outputIdentity,
                tag.getString("outputLabel"),
                tag.getInt("outputCountPerBatch"),
                tag.getInt("remainingOutputCount"),
                decodeCraftRunIngredientGroups(tag.getList("inputs", Tag.TAG_COMPOUND)),
                decodeStrings(tag.getList("diagnostics", Tag.TAG_STRING)));
    }

    public static CompoundTag encodeCraftRunState(CraftRunState state) {
        CompoundTag tag = new CompoundTag();
        CraftRunState resolved = state == null ? CraftRunState.empty() : state;
        tag.putInt("revision", resolved.revision());
        tag.putString("selectedEntryId", resolved.selectedEntryId());
        ListTag entries = new ListTag();
        for (CraftRunRecipeEntry entry : resolved.entries()) {
            entries.add(encodeCraftRunRecipeEntry(entry));
        }
        tag.put("entries", entries);
        return tag;
    }

    public static CraftRunState decodeCraftRunState(CompoundTag tag) {
        if (tag == null) {
            return CraftRunState.empty();
        }
        ArrayList<CraftRunRecipeEntry> entries = new ArrayList<>();
        ListTag entryTags = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int index = 0; index < entryTags.size(); index++) {
            entries.add(decodeCraftRunRecipeEntry(entryTags.getCompound(index)));
        }
        return new CraftRunState(tag.getInt("revision"), tag.getString("selectedEntryId"), entries);
    }

    private static CompoundTag encodeCraftRunRecipeEntry(CraftRunRecipeEntry entry) {
        CompoundTag tag = new CompoundTag();
        if (entry == null) {
            return tag;
        }
        tag.putString("entryId", entry.entryId());
        tag.putLong("sequence", entry.sequence());
        tag.putString("sourceKey", entry.sourceKey());
        tag.putString("recipeId", entry.recipeId());
        tag.putString("label", entry.label());
        if (entry.outputIdentity() != null) {
            tag.put("outputIdentity", encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(entry.outputIdentity())));
        }
        tag.putString("outputLabel", entry.outputLabel());
        tag.putInt("outputCountPerBatch", entry.outputCountPerBatch());
        tag.putInt("remainingOutputCount", entry.remainingOutputCount());
        tag.put("inputs", encodeCraftRunIngredientGroups(entry.inputs()));
        tag.put("diagnostics", encodeStrings(entry.diagnostics()));
        return tag;
    }

    private static CraftRunRecipeEntry decodeCraftRunRecipeEntry(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        ItemIdentity outputIdentity = tag.contains("outputIdentity", Tag.TAG_COMPOUND)
                ? decodeIdentity(tag.getCompound("outputIdentity")).toIdentity()
                : null;
        return new CraftRunRecipeEntry(
                tag.getString("entryId"),
                tag.getLong("sequence"),
                tag.getString("sourceKey"),
                tag.getString("recipeId"),
                tag.getString("label"),
                outputIdentity,
                tag.getString("outputLabel"),
                tag.getInt("outputCountPerBatch"),
                tag.getInt("remainingOutputCount"),
                decodeCraftRunIngredientGroups(tag.getList("inputs", Tag.TAG_COMPOUND)),
                decodeStrings(tag.getList("diagnostics", Tag.TAG_STRING)));
    }

    private static ListTag encodeCraftRunIngredientGroups(List<CraftRunIngredientGroup> groups) {
        ListTag tags = new ListTag();
        if (groups == null || groups.isEmpty()) {
            return tags;
        }
        for (CraftRunIngredientGroup group : groups) {
            if (group != null) {
                tags.add(encodeCraftRunIngredientGroup(group));
            }
        }
        return tags;
    }

    private static List<CraftRunIngredientGroup> decodeCraftRunIngredientGroups(ListTag tags) {
        ArrayList<CraftRunIngredientGroup> groups = new ArrayList<>();
        if (tags == null) {
            return groups;
        }
        for (int index = 0; index < tags.size(); index++) {
            groups.add(decodeCraftRunIngredientGroup(tags.getCompound(index)));
        }
        return List.copyOf(groups);
    }

    private static CompoundTag encodeCraftRunIngredientGroup(CraftRunIngredientGroup group) {
        CompoundTag tag = new CompoundTag();
        tag.putString("groupId", group.groupId());
        tag.putString("label", group.label());
        tag.putInt("requiredCountPerBatch", group.requiredCountPerBatch());
        tag.putBoolean("consumed", group.consumed());
        if (group.selectedAlternativeIdentity() != null) {
            tag.put(
                    "selectedAlternativeIdentity",
                    encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(group.selectedAlternativeIdentity())));
        }
        ListTag alternatives = new ListTag();
        for (CraftRunAlternative alternative : group.alternatives()) {
            if (alternative == null || alternative.identity() == null) {
                continue;
            }
            CompoundTag alternativeTag = new CompoundTag();
            alternativeTag.put("identity", encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(alternative.identity())));
            alternativeTag.putString("label", alternative.label());
            alternatives.add(alternativeTag);
        }
        tag.put("alternatives", alternatives);
        tag.put("diagnostics", encodeStrings(group.diagnostics()));
        return tag;
    }

    private static CraftRunIngredientGroup decodeCraftRunIngredientGroup(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        ArrayList<CraftRunAlternative> alternatives = new ArrayList<>();
        ListTag alternativeTags = tag.getList("alternatives", Tag.TAG_COMPOUND);
        for (int index = 0; index < alternativeTags.size(); index++) {
            CompoundTag alternativeTag = alternativeTags.getCompound(index);
            ItemIdentity identity = decodeIdentity(alternativeTag.getCompound("identity")).toIdentity();
            if (identity != null) {
                alternatives.add(new CraftRunAlternative(identity, alternativeTag.getString("label")));
            }
        }
        return new CraftRunIngredientGroup(
                tag.getString("groupId"),
                tag.getString("label"),
                tag.getInt("requiredCountPerBatch"),
                !tag.contains("consumed") || tag.getBoolean("consumed"),
                tag.contains("selectedAlternativeIdentity", Tag.TAG_COMPOUND)
                        ? decodeIdentity(tag.getCompound("selectedAlternativeIdentity")).toIdentity()
                        : null,
                alternatives,
                decodeStrings(tag.getList("diagnostics", Tag.TAG_STRING)));
    }

    private static ListTag encodeStrings(List<String> values) {
        ListTag tags = new ListTag();
        if (values == null || values.isEmpty()) {
            return tags;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                tags.add(net.minecraft.nbt.StringTag.valueOf(value));
            }
        }
        return tags;
    }

    private static List<String> decodeStrings(ListTag tags) {
        ArrayList<String> values = new ArrayList<>();
        if (tags == null) {
            return values;
        }
        for (int index = 0; index < tags.size(); index++) {
            String value = tags.getString(index);
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }

    private static CompoundTag encodeActiveChestPanel(SlotWorkspaceViewModel.ActiveChestPanel panel) {
        CompoundTag tag = new CompoundTag();
        if (panel == null || !panel.isPresent()) {
            return tag;
        }
        tag.putString("storageId", panel.storageId());
        tag.putString("label", panel.label());
        tag.putString("clusterId", panel.clusterId());
        tag.putString("clusterLabel", panel.clusterLabel());
        tag.putInt("swatchColor", panel.swatchColor());
        tag.putInt("posX", panel.posX());
        tag.putInt("posY", panel.posY());
        tag.putInt("posZ", panel.posZ());
        tag.putString("dimensionId", panel.dimensionId());
        tag.putString("role", panel.role().name());
        ListTag affinityTags = new ListTag();
        for (SlotWorkspaceViewModel.IdentityRef identity : panel.affinityIdentities()) {
            affinityTags.add(encodeIdentity(identity));
        }
        tag.put("affinityIdentities", affinityTags);
        return tag;
    }

    private static SlotWorkspaceViewModel.ActiveChestPanel decodeActiveChestPanel(CompoundTag tag) {
        if (tag == null || tag.getString("dimensionId").isBlank()) {
            return SlotWorkspaceViewModel.ActiveChestPanel.empty();
        }
        ArrayList<SlotWorkspaceViewModel.IdentityRef> affinityIdentities = new ArrayList<>();
        ListTag affinityTags = tag.getList("affinityIdentities", Tag.TAG_COMPOUND);
        for (int index = 0; index < affinityTags.size(); index++) {
            affinityIdentities.add(decodeIdentity(affinityTags.getCompound(index)));
        }
        return new SlotWorkspaceViewModel.ActiveChestPanel(
                tag.getString("storageId"),
                tag.getString("label"),
                tag.getString("clusterId"),
                tag.getString("clusterLabel"),
                tag.getInt("swatchColor"),
                tag.getInt("posX"),
                tag.getInt("posY"),
                tag.getInt("posZ"),
                tag.getString("dimensionId"),
                ChestRole.parse(tag.getString("role")),
                affinityIdentities
        );
    }

    private static CompoundTag encodeWayfindingTarget(WayfindingTarget target) {
        CompoundTag tag = new CompoundTag();
        tag.putString("storageId", target.storageId());
        tag.putString("dimensionId", target.dimensionId());
        tag.putInt("worldX", target.worldX());
        tag.putInt("worldY", target.worldY());
        tag.putInt("worldZ", target.worldZ());
        tag.putInt("totalMissingCount", target.totalMissingCount());
        tag.putString("scope", target.scope().name());
        tag.put("missingIdentities", encodeIdentitySet(target.missingIdentities()));
        tag.put("kitMissingIdentities", encodeIdentitySet(target.kitMissingIdentities()));
        tag.put("desiredMissingIdentities", encodeIdentitySet(target.desiredMissingIdentities()));
        tag.put("wantedMissingIdentities", encodeIdentitySet(target.wantedMissingIdentities()));
        tag.put("putAwayIdentities", encodeIdentitySet(target.putAwayIdentities()));
        return tag;
    }

    private static WayfindingTarget decodeWayfindingTarget(CompoundTag tag) {
        LinkedHashSet<ItemIdentity> identities = decodeIdentitySet(tag, "missingIdentities");
        LinkedHashSet<ItemIdentity> kitIdentities = decodeIdentitySet(tag, "kitMissingIdentities");
        LinkedHashSet<ItemIdentity> desiredIdentities = decodeIdentitySet(tag, "desiredMissingIdentities");
        LinkedHashSet<ItemIdentity> wantedIdentities = decodeIdentitySet(tag, "wantedMissingIdentities");
        LinkedHashSet<ItemIdentity> putAwayIdentities = decodeIdentitySet(tag, "putAwayIdentities");
        WayfindingTarget.Scope scope;
        try {
            String raw = tag.getString("scope");
            scope = raw == null || raw.isBlank()
                    ? WayfindingTarget.Scope.PLAYER
                    : WayfindingTarget.Scope.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            scope = WayfindingTarget.Scope.PLAYER;
        }
        return new WayfindingTarget(
                tag.getString("storageId"),
                tag.getString("dimensionId"),
                tag.getInt("worldX"),
                tag.getInt("worldY"),
                tag.getInt("worldZ"),
                identities,
                kitIdentities,
                desiredIdentities,
                wantedIdentities,
                putAwayIdentities,
                tag.getInt("totalMissingCount"),
                scope
        );
    }

    private static ListTag encodeIdentitySet(java.util.Set<ItemIdentity> identities) {
        ListTag tags = new ListTag();
        if (identities != null) {
            for (ItemIdentity identity : identities) {
                if (identity != null) {
                    tags.add(encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(identity)));
                }
            }
        }
        return tags;
    }

    private static LinkedHashSet<ItemIdentity> decodeIdentitySet(CompoundTag tag, String key) {
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        ListTag identityTags = tag.getList(key, Tag.TAG_COMPOUND);
        for (int index = 0; index < identityTags.size(); index++) {
            ItemIdentity identity = decodeIdentity(identityTags.getCompound(index)).toIdentity();
            if (identity != null) {
                identities.add(identity);
            }
        }
        return identities;
    }

    private static CompoundTag encodeKitCard(SlotWorkspaceViewModel.KitCard card) {
        CompoundTag tag = new CompoundTag();
        tag.putString("kitId", card.kitId());
        tag.putString("name", card.name());
        tag.putString("parentId", card.parentId());
        tag.putInt("pageCount", card.pageCount());
        tag.putInt("activePageIndex", card.activePageIndex());
        tag.putBoolean("active", card.active());
        tag.putBoolean("variant", card.variant());
        tag.putInt("memberCount", card.memberCount());
        ListTag members = new ListTag();
        for (SlotWorkspaceViewModel.IdentityRef member : card.members()) {
            members.add(encodeIdentity(member));
        }
        tag.put("members", members);
        ListTag acceptedInputs = new ListTag();
        for (WorkflowAcceptedInputRule rule : card.acceptedInputs()) {
            acceptedInputs.add(encodeAcceptedInput(rule));
        }
        tag.put("acceptedInputs", acceptedInputs);
        tag.putInt("slotCount", card.slotCount());
        tag.putInt("readyCount", card.readyCount());
        tag.putInt("carriedSlotCount", card.carriedSlotCount());
        tag.putInt("carriedSlotCapacity", card.carriedSlotCapacity());
        tag.putInt("bringSlotCount", card.bringSlotCount());
        tag.putInt("bringReadyCount", card.bringReadyCount());

        ListTag slots = new ListTag();
        for (SlotWorkspaceViewModel.KitSlotState slot : card.slots()) {
            slots.add(encodeKitSlot(slot));
        }
        tag.put("slots", slots);

        ListTag pages = new ListTag();
        for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
            pages.add(encodeKitPage(page));
        }
        tag.put("pages", pages);

        ListTag bring = new ListTag();
        for (SlotWorkspaceViewModel.KitBringItem item : card.bring()) {
            bring.add(encodeKitBring(item));
        }
        tag.put("bring", bring);
        return tag;
    }

    private static SlotWorkspaceViewModel.KitCard decodeKitCard(CompoundTag tag) {
        ArrayList<SlotWorkspaceViewModel.KitSlotState> slots = new ArrayList<>();
        ListTag slotTags = tag.getList("slots", Tag.TAG_COMPOUND);
        for (int index = 0; index < slotTags.size(); index++) {
            slots.add(decodeKitSlot(slotTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.KitPageView> pages = new ArrayList<>();
        ListTag pageTags = tag.getList("pages", Tag.TAG_COMPOUND);
        for (int index = 0; index < pageTags.size(); index++) {
            pages.add(decodeKitPage(pageTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.KitBringItem> bring = new ArrayList<>();
        ListTag bringTags = tag.getList("bring", Tag.TAG_COMPOUND);
        for (int index = 0; index < bringTags.size(); index++) {
            bring.add(decodeKitBring(bringTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.IdentityRef> members = new ArrayList<>();
        ListTag memberTags = tag.getList("members", Tag.TAG_COMPOUND);
        for (int index = 0; index < memberTags.size(); index++) {
            members.add(decodeIdentity(memberTags.getCompound(index)));
        }

        ArrayList<WorkflowAcceptedInputRule> acceptedInputs = new ArrayList<>();
        ListTag acceptedInputTags = tag.getList("acceptedInputs", Tag.TAG_COMPOUND);
        for (int index = 0; index < acceptedInputTags.size(); index++) {
            WorkflowAcceptedInputRule rule = decodeAcceptedInput(acceptedInputTags.getCompound(index));
            if (rule != null) {
                acceptedInputs.add(rule);
            }
        }

        return new SlotWorkspaceViewModel.KitCard(
                tag.getString("kitId"),
                tag.getString("name"),
                tag.getString("parentId"),
                tag.getInt("pageCount"),
                tag.getInt("activePageIndex"),
                tag.getBoolean("active"),
                tag.getBoolean("variant"),
                tag.getInt("memberCount"),
                members,
                acceptedInputs,
                tag.getInt("slotCount"),
                tag.getInt("readyCount"),
                tag.getInt("carriedSlotCount"),
                tag.getInt("carriedSlotCapacity"),
                tag.getInt("bringSlotCount"),
                tag.getInt("bringReadyCount"),
                slots,
                pages,
                bring
        );
    }

    private static CompoundTag encodeAcceptedInput(WorkflowAcceptedInputRule rule) {
        CompoundTag tag = new CompoundTag();
        if (rule == null) {
            return tag;
        }
        tag.putString("kind", rule.kind().name());
        tag.putString("tagId", rule.tagId());
        if (rule.identity() != null) {
            tag.put("identity", encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(rule.identity())));
        }
        return tag;
    }

    private static WorkflowAcceptedInputRule decodeAcceptedInput(CompoundTag tag) {
        if (tag == null) {
            return null;
        }
        WorkflowAcceptedInputRule.Kind kind = WorkflowAcceptedInputRule.parseKind(tag.getString("kind"));
        if (kind == WorkflowAcceptedInputRule.Kind.ITEM_TAG) {
            return WorkflowAcceptedInputRule.itemTag(tag.getString("tagId"));
        }
        SlotWorkspaceViewModel.IdentityRef identity = decodeIdentity(tag.getCompound("identity"));
        return WorkflowAcceptedInputRule.exact(identity.toIdentity());
    }

    private static CompoundTag encodeKitPage(SlotWorkspaceViewModel.KitPageView page) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("pageIndex", page.pageIndex());
        tag.putInt("slotCount", page.slotCount());
        tag.putInt("readyCount", page.readyCount());
        ListTag slots = new ListTag();
        for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
            slots.add(encodeKitSlot(slot));
        }
        tag.put("slots", slots);
        return tag;
    }

    private static SlotWorkspaceViewModel.KitPageView decodeKitPage(CompoundTag tag) {
        ArrayList<SlotWorkspaceViewModel.KitSlotState> slots = new ArrayList<>();
        ListTag slotTags = tag.getList("slots", Tag.TAG_COMPOUND);
        for (int index = 0; index < slotTags.size(); index++) {
            slots.add(decodeKitSlot(slotTags.getCompound(index)));
        }
        return new SlotWorkspaceViewModel.KitPageView(
                tag.getInt("pageIndex"),
                tag.getInt("slotCount"),
                tag.getInt("readyCount"),
                slots
        );
    }

    private static CompoundTag encodeKitSlot(SlotWorkspaceViewModel.KitSlotState slot) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("slotIndex", slot.slotIndex());
        tag.putBoolean("filled", slot.filled());
        tag.putBoolean("ready", slot.ready());
        tag.put("identity", encodeIdentity(slot.identity()));
        tag.put("displayStack", slot.displayStack().save(new CompoundTag()));
        tag.putString("name", slot.name());
        return tag;
    }

    private static SlotWorkspaceViewModel.KitSlotState decodeKitSlot(CompoundTag tag) {
        return new SlotWorkspaceViewModel.KitSlotState(
                tag.getInt("slotIndex"),
                tag.getBoolean("filled"),
                tag.getBoolean("ready"),
                decodeIdentity(tag.getCompound("identity")),
                ItemStack.of(tag.getCompound("displayStack")),
                tag.getString("name")
        );
    }

    private static CompoundTag encodeKitBring(SlotWorkspaceViewModel.KitBringItem item) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("ready", item.ready());
        tag.put("identity", encodeIdentity(item.identity()));
        tag.put("displayStack", item.displayStack().save(new CompoundTag()));
        tag.putString("name", item.name());
        tag.putInt("presentCount", item.presentCount());
        tag.putInt("targetCount", item.targetCount());
        return tag;
    }

    private static SlotWorkspaceViewModel.KitBringItem decodeKitBring(CompoundTag tag) {
        return new SlotWorkspaceViewModel.KitBringItem(
                decodeIdentity(tag.getCompound("identity")),
                tag.getBoolean("ready"),
                ItemStack.of(tag.getCompound("displayStack")),
                tag.getString("name"),
                tag.getInt("presentCount"),
                tag.getInt("targetCount")
        );
    }

    private static CompoundTag encodeHotbarSlot(SlotWorkspaceViewModel.HotbarSlot slot) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("hotbarIndex", slot.hotbarIndex());
        tag.putBoolean("selected", slot.selected());
        tag.putBoolean("occupied", slot.occupied());
        tag.put("displayStack", slot.displayStack().save(new CompoundTag()));
        tag.putInt("count", slot.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.HotbarSlot decodeHotbarSlot(CompoundTag tag) {
        return new SlotWorkspaceViewModel.HotbarSlot(
                tag.getInt("hotbarIndex"),
                tag.getBoolean("selected"),
                tag.getBoolean("occupied"),
                ItemStack.of(tag.getCompound("displayStack")),
                tag.getInt("count")
        );
    }

    private static CompoundTag encodeOffhand(SlotWorkspaceViewModel.OffhandSlot offhand) {
        SlotWorkspaceViewModel.OffhandSlot resolved = offhand == null
                ? SlotWorkspaceViewModel.OffhandSlot.empty()
                : offhand;
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("occupied", resolved.occupied());
        tag.put("displayStack", resolved.displayStack().save(new CompoundTag()));
        tag.putInt("count", resolved.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.OffhandSlot decodeOffhand(CompoundTag tag) {
        return new SlotWorkspaceViewModel.OffhandSlot(
                tag.getBoolean("occupied"),
                ItemStack.of(tag.getCompound("displayStack")),
                tag.getInt("count")
        );
    }

    private static CompoundTag encodeIsland(SlotWorkspaceViewModel.AtlasIsland island) {
        CompoundTag tag = new CompoundTag();
        tag.putString("islandId", island.islandId());
        tag.putString("label", island.label());
        tag.putString("kind", island.kind().name());
        tag.putDouble("x", island.x());
        tag.putDouble("y", island.y());
        tag.putInt("color", island.color());
        tag.putInt("itemCount", island.itemCount());
        tag.putInt("carriedCount", island.carriedCount());
        return tag;
    }

    private static SlotWorkspaceViewModel.AtlasIsland decodeIsland(CompoundTag tag) {
        return new SlotWorkspaceViewModel.AtlasIsland(
                tag.getString("islandId"),
                tag.getString("label"),
                decodeIslandKind(tag.getString("kind")),
                tag.getDouble("x"),
                tag.getDouble("y"),
                tag.getInt("color"),
                tag.getInt("itemCount"),
                tag.getInt("carriedCount")
        );
    }

    private static VisualAtlasIslandKind decodeIslandKind(String raw) {
        try {
            return raw == null || raw.isBlank()
                    ? VisualAtlasIslandKind.PLAYER
                    : VisualAtlasIslandKind.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return VisualAtlasIslandKind.PLAYER;
        }
    }

    private static CompoundTag encodeItem(SlotWorkspaceViewModel.AtlasItem item) {
        CompoundTag tag = new CompoundTag();
        tag.put("identity", encodeIdentity(item.identity()));
        tag.put("displayStack", item.displayStack().save(new CompoundTag()));
        tag.putString("name", item.name());
        tag.putInt("totalCount", item.totalCount());
        tag.putInt("firstSlotIndex", item.firstSlotIndex());
        tag.putString("islandId", item.islandId());
        tag.putBoolean("recent", item.recent());
        tag.putBoolean("playerPlaced", item.playerPlaced());
        tag.putBoolean("carried", item.carried());
        tag.putBoolean("ghost", item.ghost());
        tag.putInt("proximateCount", item.proximateCount());
        tag.putBoolean("isCarriedContainer", item.isCarriedContainer());
        tag.putInt("containerFreeSlotCount", item.containerFreeSlotCount());
        tag.putInt("containerSlotCapacity", item.containerSlotCapacity());
        ListTag chipTags = new ListTag();
        for (ChipSuggestion chip : item.chipSuggestions()) {
            chipTags.add(encodeChip(chip));
        }
        tag.put("chipSuggestions", chipTags);
        ListTag presenceTags = new ListTag();
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.presence()) {
            presenceTags.add(encodeChestPresence(entry));
        }
        tag.put("presence", presenceTags);
        ListTag elsewhereTags = new ListTag();
        for (SlotWorkspaceViewModel.ChestPresenceEntry entry : item.elsewhere()) {
            elsewhereTags.add(encodeChestPresence(entry));
        }
        tag.put("elsewhere", elsewhereTags);
        tag.putBoolean("kitNeeded", item.kitNeeded());
        tag.putInt("desiredCount", item.desiredCount());
        tag.putBoolean("desiredCountFromKit", item.desiredCountFromKit());
        tag.putInt("wantedCount", item.wantedCount());
        tag.putBoolean("wanted", item.wanted());
        tag.putBoolean("junk", item.junk());
        tag.putBoolean("acceptedWorkflowInput", item.acceptedWorkflowInput());
        tag.putString("largestCarriedSourceId", item.largestCarriedSourceId());
        tag.putInt("largestCarriedSlotIndex", item.largestCarriedSlotIndex());
        tag.putInt("largestCarriedSlotCount", item.largestCarriedSlotCount());
        tag.putString("putAwayState", item.putAwayState().name());
        return tag;
    }

    private static SlotWorkspaceViewModel.AtlasItem decodeItem(CompoundTag tag) {
        ArrayList<ChipSuggestion> chipSuggestions = new ArrayList<>();
        ListTag chipTags = tag.getList("chipSuggestions", Tag.TAG_COMPOUND);
        for (int index = 0; index < chipTags.size(); index++) {
            ChipSuggestion chip = decodeChip(chipTags.getCompound(index));
            if (chip != null) {
                chipSuggestions.add(chip);
            }
        }
        ArrayList<SlotWorkspaceViewModel.ChestPresenceEntry> presence = new ArrayList<>();
        ListTag presenceTags = tag.getList("presence", Tag.TAG_COMPOUND);
        for (int index = 0; index < presenceTags.size(); index++) {
            presence.add(decodeChestPresence(presenceTags.getCompound(index)));
        }
        ArrayList<SlotWorkspaceViewModel.ChestPresenceEntry> elsewhere = new ArrayList<>();
        ListTag elsewhereTags = tag.getList("elsewhere", Tag.TAG_COMPOUND);
        for (int index = 0; index < elsewhereTags.size(); index++) {
            elsewhere.add(decodeChestPresence(elsewhereTags.getCompound(index)));
        }
        return new SlotWorkspaceViewModel.AtlasItem(
                decodeIdentity(tag.getCompound("identity")),
                ItemStack.of(tag.getCompound("displayStack")),
                tag.getString("name"),
                tag.getInt("totalCount"),
                tag.getInt("firstSlotIndex"),
                tag.getString("islandId"),
                tag.getBoolean("recent"),
                tag.getBoolean("playerPlaced"),
                tag.getBoolean("carried"),
                tag.getBoolean("ghost"),
                tag.getInt("proximateCount"),
                chipSuggestions,
                presence,
                elsewhere,
                tag.getBoolean("isCarriedContainer"),
                tag.getInt("containerFreeSlotCount"),
                tag.getInt("containerSlotCapacity"),
                tag.getBoolean("kitNeeded"),
                tag.getInt("desiredCount"),
                tag.getBoolean("desiredCountFromKit"),
                decodeWantedCount(tag),
                tag.getBoolean("junk"),
                tag.getBoolean("acceptedWorkflowInput"),
                tag.getString("largestCarriedSourceId"),
                tag.getInt("largestCarriedSlotIndex"),
                tag.getInt("largestCarriedSlotCount"),
                SlotWorkspaceViewModel.PutAwayState.parse(tag.getString("putAwayState"))
        );
    }

    private static int decodeWantedCount(CompoundTag tag) {
        if (tag.contains("wantedCount", Tag.TAG_INT)) {
            return tag.getInt("wantedCount");
        }
        return tag.getBoolean("wanted") ? 1 : 0;
    }

    private static CompoundTag encodeChip(ChipSuggestion chip) {
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", chip.kind().name());
        tag.putString("template", chip.template() == null ? "" : chip.template().name());
        tag.putString("islandId", chip.islandId());
        tag.putString("label", chip.label());
        tag.putInt("color", chip.color());
        if (chip.iconIdentity() != null) {
            tag.put("iconIdentity", encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(chip.iconIdentity())));
        }
        return tag;
    }

    private static ChipSuggestion decodeChip(CompoundTag tag) {
        ChipSuggestion.ChipKind kind;
        try {
            kind = ChipSuggestion.ChipKind.valueOf(tag.getString("kind"));
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        IslandSuggestionTemplate template = null;
        String templateName = tag.getString("template");
        if (templateName != null && !templateName.isBlank()) {
            try {
                template = IslandSuggestionTemplate.valueOf(templateName);
            } catch (IllegalArgumentException ignored) {
            }
        }

        ItemIdentity iconIdentity = null;
        if (tag.contains("iconIdentity", Tag.TAG_COMPOUND)) {
            iconIdentity = decodeIdentity(tag.getCompound("iconIdentity")).toIdentity();
        }

        String islandId = tag.getString("islandId");
        String label = tag.getString("label");
        int color = tag.getInt("color");
        if (kind == ChipSuggestion.ChipKind.TEMPLATE && template == null) {
            return null;
        }
        if (kind == ChipSuggestion.ChipKind.LEARNED && (islandId == null || islandId.isBlank())) {
            return null;
        }
        return new ChipSuggestion(kind, template, islandId, label, color, iconIdentity);
    }

    private static CompoundTag encodeChestChip(SlotWorkspaceViewModel.ChestChip chip) {
        CompoundTag tag = new CompoundTag();
        tag.putString("storageId", chip.storageId());
        tag.putString("dimensionId", chip.dimensionId());
        tag.putString("label", chip.label());
        tag.putInt("anchorCount", chip.anchorCount());
        tag.putInt("slotCapacity", chip.slotCapacity());
        tag.putInt("filledSlots", chip.filledSlots());
        tag.putBoolean("proximate", chip.proximate());
        tag.putInt("affinityIdentities", chip.affinityIdentities());
        tag.putInt("worldX", chip.worldX());
        tag.putInt("worldY", chip.worldY());
        tag.putInt("worldZ", chip.worldZ());
        tag.putString("clusterId", chip.clusterId());
        return tag;
    }

    private static SlotWorkspaceViewModel.ChestChip decodeChestChip(CompoundTag tag) {
        return new SlotWorkspaceViewModel.ChestChip(
                tag.getString("storageId"),
                tag.getString("dimensionId"),
                tag.getString("label"),
                tag.getInt("anchorCount"),
                tag.getInt("slotCapacity"),
                tag.getInt("filledSlots"),
                tag.getBoolean("proximate"),
                tag.getInt("affinityIdentities"),
                tag.getInt("worldX"),
                tag.getInt("worldY"),
                tag.getInt("worldZ"),
                tag.getString("clusterId")
        );
    }

    private static void writeChestChipContents(CompoundTag tag, SlotWorkspaceViewModel.ChestChip chip) {
        ListTag contents = new ListTag();
        for (SlotWorkspaceViewModel.ChestContentSummary summary : chip.contents()) {
            contents.add(encodeChestContentSummary(summary));
        }
        tag.put("contents", contents);
    }

    private static List<SlotWorkspaceViewModel.ChestContentSummary> readChestChipContents(CompoundTag tag) {
        ListTag contents = tag.getList("contents", Tag.TAG_COMPOUND);
        if (contents.isEmpty()) {
            return List.of();
        }
        ArrayList<SlotWorkspaceViewModel.ChestContentSummary> result = new ArrayList<>(contents.size());
        for (int index = 0; index < contents.size(); index++) {
            result.add(decodeChestContentSummary(contents.getCompound(index)));
        }
        return List.copyOf(result);
    }

    private static CompoundTag encodeChestContentSummary(SlotWorkspaceViewModel.ChestContentSummary summary) {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", summary.itemId());
        tag.putString("componentFingerprint", summary.componentFingerprint());
        tag.putString("name", summary.name());
        tag.put("displayStack", summary.displayStack().save(new CompoundTag()));
        tag.putInt("count", summary.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.ChestContentSummary decodeChestContentSummary(CompoundTag tag) {
        return new SlotWorkspaceViewModel.ChestContentSummary(
                tag.getString("itemId"),
                tag.getString("componentFingerprint"),
                tag.getString("name"),
                ItemStack.of(tag.getCompound("displayStack")),
                tag.getInt("count")
        );
    }

    private static CompoundTag encodeChestCluster(SlotWorkspaceViewModel.ChestClusterDescriptor cluster) {
        CompoundTag tag = new CompoundTag();
        tag.putString("clusterId", cluster.clusterId());
        tag.putString("label", cluster.label());
        tag.putInt("ordinal", cluster.ordinal());
        return tag;
    }

    private static SlotWorkspaceViewModel.ChestClusterDescriptor decodeChestCluster(CompoundTag tag) {
        return new SlotWorkspaceViewModel.ChestClusterDescriptor(
                tag.getString("clusterId"),
                tag.getString("label"),
                tag.getInt("ordinal")
        );
    }

    private static CompoundTag encodeLootChestPanel(SlotWorkspaceViewModel.LootChestPanel panel) {
        SlotWorkspaceViewModel.LootChestPanel resolved = panel == null
                ? SlotWorkspaceViewModel.LootChestPanel.empty()
                : panel;
        CompoundTag tag = new CompoundTag();
        tag.putInt("chestX", resolved.chestX());
        tag.putInt("chestY", resolved.chestY());
        tag.putInt("chestZ", resolved.chestZ());
        tag.putString("dimensionId", resolved.dimensionId());
        tag.putString("label", resolved.label());
        ListTag items = new ListTag();
        for (SlotWorkspaceViewModel.AtlasItem item : resolved.items()) {
            items.add(encodeItem(item));
        }
        tag.put("items", items);
        return tag;
    }

    private static SlotWorkspaceViewModel.LootChestPanel decodeLootChestPanel(CompoundTag tag) {
        if (tag == null) {
            return SlotWorkspaceViewModel.LootChestPanel.empty();
        }
        String dimensionId = tag.getString("dimensionId");
        if (dimensionId == null || dimensionId.isBlank()) {
            return SlotWorkspaceViewModel.LootChestPanel.empty();
        }
        ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        ListTag itemTags = tag.getList("items", Tag.TAG_COMPOUND);
        for (int index = 0; index < itemTags.size(); index++) {
            items.add(decodeItem(itemTags.getCompound(index)));
        }
        return new SlotWorkspaceViewModel.LootChestPanel(
                tag.getInt("chestX"),
                tag.getInt("chestY"),
                tag.getInt("chestZ"),
                dimensionId,
                tag.getString("label"),
                items
        );
    }

    private static CompoundTag encodeChestPresence(SlotWorkspaceViewModel.ChestPresenceEntry entry) {
        CompoundTag tag = new CompoundTag();
        SlotWorkspaceViewModel.ChestPresenceEntry resolved = entry == null
                ? new SlotWorkspaceViewModel.ChestPresenceEntry("", "", 0)
                : entry;
        tag.putString("storageId", resolved.storageId());
        tag.putString("label", resolved.label());
        tag.putInt("count", resolved.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.ChestPresenceEntry decodeChestPresence(CompoundTag tag) {
        return new SlotWorkspaceViewModel.ChestPresenceEntry(
                tag.getString("storageId"),
                tag.getString("label"),
                tag.getInt("count"));
    }

    private static CompoundTag encodeIdentity(SlotWorkspaceViewModel.IdentityRef identity) {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", identity.itemId());
        tag.putString("comparisonMode", identity.comparisonMode());
        tag.putString("componentFingerprint", identity.componentFingerprint());
        return tag;
    }

    private static SlotWorkspaceViewModel.IdentityRef decodeIdentity(CompoundTag tag) {
        return new SlotWorkspaceViewModel.IdentityRef(
                tag.getString("itemId"),
                tag.getString("comparisonMode"),
                tag.getString("componentFingerprint")
        );
    }
}
