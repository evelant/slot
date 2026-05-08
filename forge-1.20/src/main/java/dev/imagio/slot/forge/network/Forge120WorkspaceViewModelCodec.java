package dev.imagio.slot.forge.network;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.inventory.workspace.WayfindingTarget;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class Forge120WorkspaceViewModelCodec {
    private Forge120WorkspaceViewModelCodec() {
    }

    public static CompoundTag encode(SlotWorkspaceViewModel viewModel) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        CompoundTag tag = new CompoundTag();
        tag.putLong("revision", resolved.revision());
        tag.putString("status", resolved.status());
        tag.putString("diagnostics", resolved.diagnostics());
        tag.putInt("pendingCount", resolved.pendingCount());
        tag.putInt("selectedQuickAccessSlot", resolved.selectedQuickAccessSlot());
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

        tag.put("lootChestPanel", encodeLootChestPanel(resolved.lootChestPanel()));

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

        ListTag kits = new ListTag();
        for (SlotWorkspaceViewModel.KitCard card : resolved.kits()) {
            kits.add(encodeKitCard(card));
        }
        tag.put("kits", kits);

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

        tag.put("activeChestPanel", encodeActiveChestPanel(resolved.activeChestPanel()));
        return tag;
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
                activeChestPanel
        );
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

    private static CompoundTag encodeWayfindingTarget(WayfindingTarget target) {
        CompoundTag tag = new CompoundTag();
        tag.putString("storageId", target.storageId());
        tag.putString("dimensionId", target.dimensionId());
        tag.putInt("worldX", target.worldX());
        tag.putInt("worldY", target.worldY());
        tag.putInt("worldZ", target.worldZ());
        tag.putInt("totalMissingCount", target.totalMissingCount());
        tag.putString("scope", target.scope().name());
        ListTag identities = new ListTag();
        for (ItemIdentity identity : target.missingIdentities()) {
            identities.add(encodeIdentity(SlotWorkspaceViewModel.IdentityRef.from(identity)));
        }
        tag.put("missingIdentities", identities);
        return tag;
    }

    private static WayfindingTarget decodeWayfindingTarget(CompoundTag tag) {
        LinkedHashSet<ItemIdentity> identities = new LinkedHashSet<>();
        ListTag identityTags = tag.getList("missingIdentities", Tag.TAG_COMPOUND);
        for (int index = 0; index < identityTags.size(); index++) {
            ItemIdentity identity = decodeIdentity(identityTags.getCompound(index)).toIdentity();
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

    private static CompoundTag encodeKitCard(SlotWorkspaceViewModel.KitCard card) {
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
        tag.putString("largestCarriedSourceId", item.largestCarriedSourceId());
        tag.putInt("largestCarriedSlotIndex", item.largestCarriedSlotIndex());
        tag.putInt("largestCarriedSlotCount", item.largestCarriedSlotCount());
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
                tag.getString("largestCarriedSourceId"),
                tag.getInt("largestCarriedSlotIndex"),
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
