package dev.imagio.slot.neoforge.screen.ldlib;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceAtlasLayout;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.workflow.domain.VisualAtlasIslandKind;
import dev.imagio.slot.workflow.domain.VisualHomeMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

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

        ListTag chestTileTags = new ListTag();
        for (SlotWorkspaceViewModel.ClaimedChestTile tile : viewModel.claimedChestTiles()) {
            chestTileTags.add(encodeClaimedChestTile(tile, provider));
        }
        tag.put("claimedChestTiles", chestTileTags);

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
        return tag;
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

        ArrayList<SlotWorkspaceViewModel.ClaimedChestTile> claimedChestTiles = new ArrayList<>();
        ListTag chestTileTags = compoundTag.getList("claimedChestTiles", Tag.TAG_COMPOUND);
        for (int index = 0; index < chestTileTags.size(); index++) {
            claimedChestTiles.add(decodeClaimedChestTile(provider, chestTileTags.getCompound(index)));
        }

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
                        ? SlotWorkspaceAtlasLayout.fittedIslands(
                        SlotWorkspaceAtlasLayout.baseIslands(VisualHomeMap.empty()),
                        List.of()
                )
                        : islands,
                atlasItems,
                triageItems,
                claimedChestTiles,
                hotbarSlots.isEmpty() ? SlotWorkspaceViewModel.emptyHotbar() : hotbarSlots,
                decodeOffhand(provider, compoundTag.getCompound("offhand")),
                kits
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
        return tag;
    }

    private static SlotWorkspaceViewModel.KitBringItem decodeKitBring(HolderLookup.Provider provider, CompoundTag tag) {
        return new SlotWorkspaceViewModel.KitBringItem(
                decodeIdentity(tag.getCompound("identity")),
                tag.getBoolean("ready"),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getString("name")
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
        tag.putInt("x", island.x());
        tag.putInt("y", island.y());
        tag.putInt("width", island.width());
        tag.putInt("height", island.height());
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
                tag.getInt("x"),
                tag.getInt("y"),
                tag.getInt("width"),
                tag.getInt("height"),
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
        tag.putInt("x", item.x());
        tag.putInt("y", item.y());
        tag.putInt("width", item.width());
        tag.putInt("height", item.height());
        tag.putBoolean("recent", item.recent());
        tag.putBoolean("playerPlaced", item.playerPlaced());
        tag.putBoolean("carried", item.carried());
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
        return new SlotWorkspaceViewModel.AtlasItem(
                decodeIdentity(tag.getCompound("identity")),
                ItemStack.parseOptional(provider, tag.getCompound("displayStack")),
                tag.getString("name"),
                tag.getInt("totalCount"),
                tag.getInt("firstSlotIndex"),
                tag.getString("islandId"),
                tag.getInt("x"),
                tag.getInt("y"),
                tag.getInt("width"),
                tag.getInt("height"),
                tag.getBoolean("recent"),
                tag.getBoolean("playerPlaced"),
                tag.getBoolean("carried"),
                chipSuggestions,
                presence,
                tag.getBoolean("isCarriedContainer"),
                tag.getInt("containerFreeSlotCount"),
                tag.getInt("containerSlotCapacity")
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

    private static CompoundTag encodeClaimedChestTile(
            SlotWorkspaceViewModel.ClaimedChestTile tile,
            HolderLookup.Provider provider
    ) {
        CompoundTag tag = new CompoundTag();
        tag.putString("storageId", tile.storageId());
        tag.putString("dimensionId", tile.dimensionId());
        tag.putInt("atlasX", tile.atlasX());
        tag.putInt("atlasY", tile.atlasY());
        tag.putInt("width", tile.width());
        tag.putInt("height", tile.height());
        tag.putString("label", tile.label());
        tag.putInt("anchorCount", tile.anchorCount());
        tag.putInt("slotCount", tile.slotCount());
        tag.putBoolean("proximate", tile.proximate());
        ListTag contentTags = new ListTag();
        List<Integer> indices = tile.contentSlotIndices();
        for (int i = 0; i < tile.contents().size(); i++) {
            ItemStack stack = tile.contents().get(i);
            CompoundTag entry = new CompoundTag();
            entry.put("stack", stack == null ? new CompoundTag() : stack.saveOptional(provider));
            int slotIndex = i < indices.size() ? indices.get(i) : i;
            entry.putInt("slotIndex", slotIndex);
            contentTags.add(entry);
        }
        tag.put("contents", contentTags);
        ListTag linkedIslandTags = new ListTag();
        for (String islandId : tile.linkedIslandIds()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("islandId", islandId);
            linkedIslandTags.add(entry);
        }
        tag.put("linkedIslandIds", linkedIslandTags);
        return tag;
    }

    private static SlotWorkspaceViewModel.ClaimedChestTile decodeClaimedChestTile(
            HolderLookup.Provider provider,
            CompoundTag tag
    ) {
        ArrayList<ItemStack> contents = new ArrayList<>();
        ArrayList<Integer> contentSlotIndices = new ArrayList<>();
        ListTag contentTags = tag.getList("contents", Tag.TAG_COMPOUND);
        for (int index = 0; index < contentTags.size(); index++) {
            CompoundTag entry = contentTags.getCompound(index);
            ItemStack stack = ItemStack.parseOptional(provider, entry.getCompound("stack"));
            contents.add(stack);
            contentSlotIndices.add(entry.contains("slotIndex") ? entry.getInt("slotIndex") : index);
        }
        ArrayList<String> linkedIslandIds = new ArrayList<>();
        ListTag linkedIslandTags = tag.getList("linkedIslandIds", Tag.TAG_COMPOUND);
        for (int index = 0; index < linkedIslandTags.size(); index++) {
            linkedIslandIds.add(linkedIslandTags.getCompound(index).getString("islandId"));
        }
        return new SlotWorkspaceViewModel.ClaimedChestTile(
                tag.getString("storageId"),
                tag.getString("dimensionId"),
                tag.getInt("atlasX"),
                tag.getInt("atlasY"),
                tag.getInt("width"),
                tag.getInt("height"),
                tag.getString("label"),
                tag.getInt("anchorCount"),
                tag.getInt("slotCount"),
                contents,
                contentSlotIndices,
                tag.getBoolean("proximate"),
                linkedIslandIds
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
