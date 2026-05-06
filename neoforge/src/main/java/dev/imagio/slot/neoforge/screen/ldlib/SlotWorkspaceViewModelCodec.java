package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

/**
 * NBT transport for {@link SlotWorkspaceViewModel}. The view model and all its records are
 * platform-neutral data; serialization to Minecraft NBT lives here so the common module
 * does not depend on {@code net.minecraft.nbt} types.
 */
public final class SlotWorkspaceViewModelCodec {
    private SlotWorkspaceViewModelCodec() {
    }

    public static CompoundTag encode(SlotWorkspaceViewModel viewModel, HolderLookup.Provider provider) {
        return encode(viewModel, provider, true);
    }

    public static CompoundTag encode(
            SlotWorkspaceViewModel viewModel,
            HolderLookup.Provider provider,
            boolean includeRevision
    ) {
        CompoundTag tag = new CompoundTag();
        if (includeRevision) {
            tag.putLong("revision", viewModel.revision());
        }
        tag.putString("status", viewModel.status());
        tag.putString("diagnostics", viewModel.diagnostics());
        tag.putInt("pendingCount", viewModel.pendingCount());
        tag.putInt("selectedQuickAccessSlot", viewModel.selectedQuickAccessSlot());
        tag.putInt("canvasWidth", viewModel.canvasWidth());
        tag.putInt("canvasHeight", viewModel.canvasHeight());
        tag.putInt("carriedFreeSlotCount", viewModel.carriedFreeSlotCount());
        tag.putInt("carriedSlotCapacity", viewModel.carriedSlotCapacity());

        ListTag islandTags = new ListTag();
        for (SlotWorkspaceViewModel.AtlasIsland island : viewModel.islands()) {
            islandTags.add(encodeIsland(island));
        }
        tag.put("islands", islandTags);

        ListTag itemTags = new ListTag();
        for (SlotWorkspaceViewModel.AtlasItem atlasItem : viewModel.atlasItems()) {
            itemTags.add(encodeItem(atlasItem, provider));
        }
        tag.put("atlasItems", itemTags);

        ListTag triageTags = new ListTag();
        for (SlotWorkspaceViewModel.AtlasItem triageItem : viewModel.triageItems()) {
            triageTags.add(encodeItem(triageItem, provider));
        }
        tag.put("triageItems", triageTags);

        ListTag chipTags = new ListTag();
        for (SlotWorkspaceViewModel.ChestChip chip : viewModel.chestChips()) {
            CompoundTag chipTag = encodeChestChip(chip);
            writeChestChipContents(chipTag, chip, provider);
            chipTags.add(chipTag);
        }
        tag.put("chestChips", chipTags);

        ListTag clusterTags = new ListTag();
        for (SlotWorkspaceViewModel.ChestClusterDescriptor cluster : viewModel.chestClusters()) {
            clusterTags.add(encodeChestCluster(cluster));
        }
        tag.put("chestClusters", clusterTags);

        tag.put("lootChestPanel", encodeLootChestPanel(viewModel.lootChestPanel(), provider));

        ListTag hotbarTags = new ListTag();
        for (SlotWorkspaceViewModel.HotbarSlot slot : viewModel.hotbarSlots()) {
            hotbarTags.add(encodeHotbar(slot, provider));
        }
        tag.put("hotbarSlots", hotbarTags);
        tag.put("offhand", encodeOffhand(viewModel.offhand(), provider));

        ListTag kitTags = new ListTag();
        for (SlotWorkspaceViewModel.KitCard card : viewModel.kits()) {
            kitTags.add(encodeKitCard(card, provider));
        }
        tag.put("kits", kitTags);

        ListTag wayfindingTags = new ListTag();
        for (WayfindingTarget target : viewModel.wayfindingTargets()) {
            wayfindingTags.add(encodeWayfindingTarget(target));
        }
        tag.put("wayfindingTargets", wayfindingTags);

        ListTag depositableTags = new ListTag();
        for (SlotWorkspaceViewModel.IdentityRef ref : viewModel.depositableIdentities()) {
            depositableTags.add(encodeIdentity(ref));
        }
        tag.put("depositableIdentities", depositableTags);

        ListTag recentTags = new ListTag();
        for (SlotWorkspaceViewModel.IdentityRef ref : viewModel.recentIdentities()) {
            recentTags.add(encodeIdentity(ref));
        }
        tag.put("recentIdentities", recentTags);

        tag.put("activeChestPanel", encodeActiveChestPanel(viewModel.activeChestPanel()));
        return tag;
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
        return tag;
    }

    private static SlotWorkspaceViewModel.ActiveChestPanel decodeActiveChestPanel(CompoundTag tag) {
        // The encoder writes an empty tag (no keys) when no chest is
        // active; treat absence-or-blank-dimensionId as "not present"
        // since dimensionId is the panel's isPresent() check.
        if (tag == null || tag.getString("dimensionId").isBlank()) {
            return SlotWorkspaceViewModel.ActiveChestPanel.empty();
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
                tag.getString("dimensionId")
        );
    }

    public static SlotWorkspaceViewModel decode(HolderLookup.Provider provider, Tag tag) {
        if (!(tag instanceof CompoundTag compoundTag)) {
            return SlotWorkspaceViewModel.empty();
        }

        ArrayList<SlotWorkspaceViewModel.AtlasIsland> islands = new ArrayList<>();
        ListTag islandTags = compoundTag.getList("islands", Tag.TAG_COMPOUND);
        for (int index = 0; index < islandTags.size(); index++) {
            islands.add(decodeIsland(islandTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.AtlasItem> atlasItems = new ArrayList<>();
        ListTag itemTags = compoundTag.getList("atlasItems", Tag.TAG_COMPOUND);
        for (int index = 0; index < itemTags.size(); index++) {
            atlasItems.add(decodeItem(provider, itemTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.AtlasItem> triageItems = new ArrayList<>();
        ListTag triageTags = compoundTag.getList("triageItems", Tag.TAG_COMPOUND);
        for (int index = 0; index < triageTags.size(); index++) {
            triageItems.add(decodeItem(provider, triageTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.ChestChip> chestChips = new ArrayList<>();
        ListTag chipTags = compoundTag.getList("chestChips", Tag.TAG_COMPOUND);
        for (int index = 0; index < chipTags.size(); index++) {
            CompoundTag chipTag = chipTags.getCompound(index);
            SlotWorkspaceViewModel.ChestChip baseChip = decodeChestChip(chipTag);
            java.util.List<SlotWorkspaceViewModel.ChestContentSummary> contents =
                    readChestChipContents(chipTag, provider);
            chestChips.add(new SlotWorkspaceViewModel.ChestChip(
                    baseChip.storageId(),
                    baseChip.dimensionId(),
                    baseChip.label(),
                    baseChip.anchorCount(),
                    baseChip.slotCapacity(),
                    baseChip.filledSlots(),
                    baseChip.proximate(),
                    baseChip.affinityIdentities(),
                    baseChip.worldX(),
                    baseChip.worldY(),
                    baseChip.worldZ(),
                    baseChip.clusterId(),
                    contents
            ));
        }

        ArrayList<SlotWorkspaceViewModel.ChestClusterDescriptor> chestClusters = new ArrayList<>();
        ListTag clusterTags = compoundTag.getList("chestClusters", Tag.TAG_COMPOUND);
        for (int index = 0; index < clusterTags.size(); index++) {
            chestClusters.add(decodeChestCluster(clusterTags.getCompound(index)));
        }

        SlotWorkspaceViewModel.LootChestPanel lootChestPanel =
                decodeLootChestPanel(provider, compoundTag.getCompound("lootChestPanel"));

        ArrayList<SlotWorkspaceViewModel.HotbarSlot> hotbarSlots = new ArrayList<>();
        ListTag hotbarTags = compoundTag.getList("hotbarSlots", Tag.TAG_COMPOUND);
        for (int index = 0; index < hotbarTags.size(); index++) {
            hotbarSlots.add(decodeHotbar(provider, hotbarTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.KitCard> kits = new ArrayList<>();
        ListTag kitTags = compoundTag.getList("kits", Tag.TAG_COMPOUND);
        for (int index = 0; index < kitTags.size(); index++) {
            kits.add(decodeKitCard(provider, kitTags.getCompound(index)));
        }

        ArrayList<WayfindingTarget> wayfindingTargets = new ArrayList<>();
        ListTag wayfindingTags = compoundTag.getList("wayfindingTargets", Tag.TAG_COMPOUND);
        for (int index = 0; index < wayfindingTags.size(); index++) {
            wayfindingTargets.add(decodeWayfindingTarget(wayfindingTags.getCompound(index)));
        }

        java.util.LinkedHashSet<SlotWorkspaceViewModel.IdentityRef> depositableIdentities =
                new java.util.LinkedHashSet<>();
        ListTag depositableTags = compoundTag.getList("depositableIdentities", Tag.TAG_COMPOUND);
        for (int index = 0; index < depositableTags.size(); index++) {
            depositableIdentities.add(decodeIdentity(depositableTags.getCompound(index)));
        }

        ArrayList<SlotWorkspaceViewModel.IdentityRef> recentIdentities = new ArrayList<>();
        ListTag recentTags = compoundTag.getList("recentIdentities", Tag.TAG_COMPOUND);
        for (int index = 0; index < recentTags.size(); index++) {
            recentIdentities.add(decodeIdentity(recentTags.getCompound(index)));
        }

        SlotWorkspaceViewModel.ActiveChestPanel activeChestPanel =
                decodeActiveChestPanel(compoundTag.getCompound("activeChestPanel"));

        return new SlotWorkspaceViewModel(
                compoundTag.getLong("revision"),
                compoundTag.getString("status"),
                compoundTag.getString("diagnostics"),
                compoundTag.getInt("pendingCount"),
                compoundTag.getInt("selectedQuickAccessSlot"),
                compoundTag.getInt("canvasWidth"),
                compoundTag.getInt("canvasHeight"),
                compoundTag.getInt("carriedFreeSlotCount"),
                compoundTag.getInt("carriedSlotCapacity"),
                islands.isEmpty()
                        ? SlotWorkspaceAtlasLayout.baseIslands(VisualHomeMap.empty())
                        : islands,
                atlasItems,
                triageItems,
                chestChips,
                chestClusters,
                hotbarSlots.isEmpty() ? SlotWorkspaceViewModel.emptyHotbar() : hotbarSlots,
                decodeOffhand(provider, compoundTag.getCompound("offhand")),
                kits,
                lootChestPanel,
                wayfindingTargets,
                depositableIdentities,
                recentIdentities,
                activeChestPanel
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
        ListTag identityTags = new ListTag();
        for (ItemIdentity identity : target.missingIdentities()) {
            identityTags.add(encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(identity)));
        }
        tag.put("missingIdentities", identityTags);
        return tag;
    }

    private static WayfindingTarget decodeWayfindingTarget(CompoundTag tag) {
        java.util.LinkedHashSet<ItemIdentity> identities = new java.util.LinkedHashSet<>();
        ListTag identityTags = tag.getList("missingIdentities", Tag.TAG_COMPOUND);
        for (int i = 0; i < identityTags.size(); i++) {
            ItemIdentity identity = decodeIdentity(identityTags.getCompound(i)).toIdentity();
            if (identity != null) {
                identities.add(identity);
            }
        }
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
                tag.getInt("totalMissingCount"),
                scope
        );
    }

    private static CompoundTag encodeKitCard(SlotWorkspaceViewModel.KitCard card, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("kitId", card.kitId());
        tag.putString("name", card.name());
        tag.putInt("pageCount", card.pageCount());
        tag.putInt("activePageIndex", card.activePageIndex());
        tag.putBoolean("active", card.active());
        tag.putInt("slotCount", card.slotCount());
        tag.putInt("readyCount", card.readyCount());
        tag.putInt("carriedSlotCount", card.carriedSlotCount());
        tag.putInt("carriedSlotCapacity", card.carriedSlotCapacity());
        tag.putInt("bringSlotCount", card.bringSlotCount());
        tag.putInt("bringReadyCount", card.bringReadyCount());
        ListTag slots = new ListTag();
        for (SlotWorkspaceViewModel.KitSlotState slot : card.slots()) {
            slots.add(encodeKitSlot(slot, provider));
        }
        tag.put("slots", slots);
        ListTag pages = new ListTag();
        for (SlotWorkspaceViewModel.KitPageView page : card.pages()) {
            pages.add(encodeKitPage(page, provider));
        }
        tag.put("pages", pages);
        ListTag bring = new ListTag();
        for (SlotWorkspaceViewModel.KitBringItem item : card.bring()) {
            bring.add(encodeKitBring(item, provider));
        }
        tag.put("bring", bring);
        return tag;
    }

    private static SlotWorkspaceViewModel.KitCard decodeKitCard(HolderLookup.Provider provider, CompoundTag tag) {
        ArrayList<SlotWorkspaceViewModel.KitSlotState> slots = new ArrayList<>();
        ListTag slotTags = tag.getList("slots", Tag.TAG_COMPOUND);
        for (int index = 0; index < slotTags.size(); index++) {
            slots.add(decodeKitSlot(provider, slotTags.getCompound(index)));
        }
        ArrayList<SlotWorkspaceViewModel.KitPageView> pages = new ArrayList<>();
        ListTag pageTags = tag.getList("pages", Tag.TAG_COMPOUND);
        for (int index = 0; index < pageTags.size(); index++) {
            pages.add(decodeKitPage(provider, pageTags.getCompound(index)));
        }
        ArrayList<SlotWorkspaceViewModel.KitBringItem> bring = new ArrayList<>();
        ListTag bringTags = tag.getList("bring", Tag.TAG_COMPOUND);
        for (int index = 0; index < bringTags.size(); index++) {
            bring.add(decodeKitBring(provider, bringTags.getCompound(index)));
        }
        return new SlotWorkspaceViewModel.KitCard(
                tag.getString("kitId"),
                tag.getString("name"),
                tag.getInt("pageCount"),
                tag.getInt("activePageIndex"),
                tag.getBoolean("active"),
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

    private static CompoundTag encodeKitBring(SlotWorkspaceViewModel.KitBringItem item, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("ready", item.ready());
        tag.put("identity", encodeIdentity(item.identity()));
        tag.put("displayStack", item.displayStack().saveOptional(provider));
        tag.putString("name", item.name());
        tag.putInt("presentCount", item.presentCount());
        tag.putInt("targetCount", item.targetCount());
        return tag;
    }

    private static SlotWorkspaceViewModel.KitBringItem decodeKitBring(HolderLookup.Provider provider, CompoundTag tag) {
        return new SlotWorkspaceViewModel.KitBringItem(
                decodeIdentity(tag.getCompound("identity")),
                tag.getBoolean("ready"),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getString("name"),
                tag.getInt("presentCount"),
                tag.getInt("targetCount")
        );
    }

    private static CompoundTag encodeKitPage(SlotWorkspaceViewModel.KitPageView page, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("pageIndex", page.pageIndex());
        tag.putInt("slotCount", page.slotCount());
        tag.putInt("readyCount", page.readyCount());
        ListTag slots = new ListTag();
        for (SlotWorkspaceViewModel.KitSlotState slot : page.slots()) {
            slots.add(encodeKitSlot(slot, provider));
        }
        tag.put("slots", slots);
        return tag;
    }

    private static SlotWorkspaceViewModel.KitPageView decodeKitPage(HolderLookup.Provider provider, CompoundTag tag) {
        ArrayList<SlotWorkspaceViewModel.KitSlotState> slots = new ArrayList<>();
        ListTag slotTags = tag.getList("slots", Tag.TAG_COMPOUND);
        for (int index = 0; index < slotTags.size(); index++) {
            slots.add(decodeKitSlot(provider, slotTags.getCompound(index)));
        }
        return new SlotWorkspaceViewModel.KitPageView(
                tag.getInt("pageIndex"),
                tag.getInt("slotCount"),
                tag.getInt("readyCount"),
                slots
        );
    }

    private static CompoundTag encodeKitSlot(SlotWorkspaceViewModel.KitSlotState slot, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("slotIndex", slot.slotIndex());
        tag.putBoolean("filled", slot.filled());
        tag.putBoolean("ready", slot.ready());
        tag.put("identity", encodeIdentity(slot.identity()));
        tag.put("displayStack", slot.displayStack().saveOptional(provider));
        tag.putString("name", slot.name());
        return tag;
    }

    private static SlotWorkspaceViewModel.KitSlotState decodeKitSlot(HolderLookup.Provider provider, CompoundTag tag) {
        return new SlotWorkspaceViewModel.KitSlotState(
                tag.getInt("slotIndex"),
                tag.getBoolean("filled"),
                tag.getBoolean("ready"),
                decodeIdentity(tag.getCompound("identity")),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getString("name")
        );
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

    private static CompoundTag encodeItem(SlotWorkspaceViewModel.AtlasItem item, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("identity", encodeIdentity(item.identity()));
        tag.put("displayStack", item.displayStack().saveOptional(provider));
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
        tag.putString("largestCarriedSourceId", item.largestCarriedSourceId());
        tag.putInt("largestCarriedSlotIndex", item.largestCarriedSlotIndex());
        tag.putInt("largestCarriedSlotCount", item.largestCarriedSlotCount());
        return tag;
    }

    private static CompoundTag encodeChestPresence(SlotWorkspaceViewModel.ChestPresenceEntry entry) {
        CompoundTag tag = new CompoundTag();
        tag.putString("storageId", entry.storageId());
        tag.putString("label", entry.label());
        tag.putInt("count", entry.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.ChestPresenceEntry decodeChestPresence(CompoundTag tag) {
        return new SlotWorkspaceViewModel.ChestPresenceEntry(
                tag.getString("storageId"),
                tag.getString("label"),
                tag.getInt("count")
        );
    }

    private static SlotWorkspaceViewModel.AtlasItem decodeItem(HolderLookup.Provider provider, CompoundTag tag) {
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
        // Older payloads (pre largest-carried-slot field) just return 0 for
        // the missing slot index, which is harmless: hasLargestCarriedSlot()
        // also requires count > 0, so the cursor pickup path correctly
        // reads "no info" and falls back to the not-on-hotbar status.
        int decodedSlotIndex = tag.getInt("largestCarriedSlotIndex");
        return new SlotWorkspaceViewModel.AtlasItem(
                decodeIdentity(tag.getCompound("identity")),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
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
                tag.getString("largestCarriedSourceId"),
                decodedSlotIndex,
                tag.getInt("largestCarriedSlotCount")
        );
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

    /**
     * The chip's per-identity {@link SlotWorkspaceViewModel.ChestContentSummary
     * contents} list intentionally goes through a sibling sub-list on the
     * chip CompoundTag because the {@code chestChips} list itself is
     * stored as a flat list of {@code CompoundTag} entries. We attach
     * a {@code "contents"} {@link ListTag} so the encode/decode pair
     * round-trips cleanly without reshaping the outer envelope.
     */
    private static void writeChestChipContents(
            CompoundTag tag,
            SlotWorkspaceViewModel.ChestChip chip,
            HolderLookup.Provider provider
    ) {
        ListTag contentsTag = new ListTag();
        for (SlotWorkspaceViewModel.ChestContentSummary summary : chip.contents()) {
            contentsTag.add(encodeChestContentSummary(summary, provider));
        }
        tag.put("contents", contentsTag);
    }

    private static java.util.List<SlotWorkspaceViewModel.ChestContentSummary> readChestChipContents(
            CompoundTag tag,
            HolderLookup.Provider provider
    ) {
        ListTag contentsTag = tag.getList("contents", Tag.TAG_COMPOUND);
        if (contentsTag.size() == 0) {
            return java.util.List.of();
        }
        ArrayList<SlotWorkspaceViewModel.ChestContentSummary> out = new ArrayList<>(contentsTag.size());
        for (int i = 0; i < contentsTag.size(); i++) {
            out.add(decodeChestContentSummary(provider, contentsTag.getCompound(i)));
        }
        return java.util.List.copyOf(out);
    }

    private static CompoundTag encodeChestContentSummary(
            SlotWorkspaceViewModel.ChestContentSummary summary, HolderLookup.Provider provider
    ) {
        CompoundTag tag = new CompoundTag();
        tag.putString("itemId", summary.itemId());
        tag.putString("componentFingerprint", summary.componentFingerprint());
        tag.putString("name", summary.name());
        tag.put("displayStack", summary.displayStack().saveOptional(provider));
        tag.putInt("count", summary.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.ChestContentSummary decodeChestContentSummary(
            HolderLookup.Provider provider, CompoundTag tag
    ) {
        return new SlotWorkspaceViewModel.ChestContentSummary(
                tag.getString("itemId"),
                tag.getString("componentFingerprint"),
                tag.getString("name"),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
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

    private static CompoundTag encodeLootChestPanel(
            SlotWorkspaceViewModel.LootChestPanel panel, HolderLookup.Provider provider
    ) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("chestX", panel.chestX());
        tag.putInt("chestY", panel.chestY());
        tag.putInt("chestZ", panel.chestZ());
        tag.putString("dimensionId", panel.dimensionId());
        tag.putString("label", panel.label());
        ListTag itemTags = new ListTag();
        for (SlotWorkspaceViewModel.AtlasItem item : panel.items()) {
            itemTags.add(encodeItem(item, provider));
        }
        tag.put("items", itemTags);
        return tag;
    }

    private static SlotWorkspaceViewModel.LootChestPanel decodeLootChestPanel(
            HolderLookup.Provider provider, CompoundTag tag
    ) {
        if (tag == null) {
            return SlotWorkspaceViewModel.LootChestPanel.empty();
        }
        // Empty (unset) dimensionId is the sentinel for "no panel" — the
        // server only writes a non-blank dimension when a loot chest is
        // active. Avoids relying on CompoundTag#contains, which isn't on
        // the test-classpath stub.
        String dimensionId = tag.getString("dimensionId");
        if (dimensionId == null || dimensionId.isBlank()) {
            return SlotWorkspaceViewModel.LootChestPanel.empty();
        }
        ArrayList<SlotWorkspaceViewModel.AtlasItem> items = new ArrayList<>();
        ListTag itemTags = tag.getList("items", Tag.TAG_COMPOUND);
        for (int index = 0; index < itemTags.size(); index++) {
            items.add(decodeItem(provider, itemTags.getCompound(index)));
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

    private static CompoundTag encodeHotbar(SlotWorkspaceViewModel.HotbarSlot slot, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("hotbarIndex", slot.hotbarIndex());
        tag.putBoolean("selected", slot.selected());
        tag.putBoolean("occupied", slot.occupied());
        tag.put("displayStack", slot.displayStack().saveOptional(provider));
        tag.putInt("count", slot.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.HotbarSlot decodeHotbar(HolderLookup.Provider provider, CompoundTag tag) {
        return new SlotWorkspaceViewModel.HotbarSlot(
                tag.getInt("hotbarIndex"),
                tag.getBoolean("selected"),
                tag.getBoolean("occupied"),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getInt("count")
        );
    }

    private static CompoundTag encodeOffhand(SlotWorkspaceViewModel.OffhandSlot offhand, HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("occupied", offhand.occupied());
        tag.put("displayStack", offhand.displayStack().saveOptional(provider));
        tag.putInt("count", offhand.count());
        return tag;
    }

    private static SlotWorkspaceViewModel.OffhandSlot decodeOffhand(HolderLookup.Provider provider, CompoundTag tag) {
        return new SlotWorkspaceViewModel.OffhandSlot(
                tag.getBoolean("occupied"),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getInt("count")
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
}
