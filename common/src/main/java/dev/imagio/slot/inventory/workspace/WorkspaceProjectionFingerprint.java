package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.inventory.triage.ChipSuggestion;
import dev.imagio.slot.inventory.triage.LearnedAdjacencyKey;
import dev.imagio.slot.inventory.triage.LearnedIslandRule;
import dev.imagio.slot.platform.SlotStackAccess;
import dev.imagio.slot.workflow.domain.CraftRunState;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class WorkspaceProjectionFingerprint {
    private WorkspaceProjectionFingerprint() {
    }

    static String contentKey(SlotWorkspaceViewModel viewModel) {
        HashSink out = new HashSink();
        appendViewModel(out, viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel);
        return out.toString();
    }

    static WorkspaceViewSliceKeys sliceKeys(SlotWorkspaceViewModel viewModel) {
        SlotWorkspaceViewModel resolved = viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel;
        return new WorkspaceViewSliceKeys(
                sliceKey("frame", out -> appendFrame(out, resolved)),
                sliceKey("wall", out -> appendWall(out, resolved)),
                sliceKey("storage", out -> appendStorage(out, resolved)),
                sliceKey("hotbar", out -> appendHotbar(out, resolved)),
                sliceKey("workflow", out -> appendWorkflowSlice(out, resolved)),
                sliceKey("panels", out -> appendPanels(out, resolved)),
                sliceKey("contextual", out -> appendContextual(out, resolved)));
    }

    static String cardKey(SlotWorkspaceViewModel.AtlasItem item) {
        return sliceKey("card", out -> out.appendObject(item));
    }

    static String storageChipKey(SlotWorkspaceViewModel.ChestChip chip) {
        return sliceKey("storage-chip", out -> out.appendObject(chip));
    }

    static String wayfindingTargetKey(WayfindingTarget target) {
        return sliceKey("wayfinding-target", out -> out.appendObject(target));
    }

    static String depositableIdentitiesKey(Set<SlotWorkspaceViewModel.IdentityRef> identities) {
        return sliceKey("depositability", out -> out.appendObject(identities == null ? Set.of() : identities));
    }

    static String inputKey(WorkspaceProjectionRequest request, ItemIdentityMatcher.Memo memo) {
        WorkspaceProjectionRequest resolved = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        null, 0L, null, null, null, null, null, null, null, null, null)
                : request;
        return ItemIdentityMatcher.withMemo(memo, () -> {
            HashSink out = new HashSink();
            appendAuthority(out, resolved.authority());
            appendWorkflow(out, resolved.workflow());
            out.appendObject(resolved.remoteStorageDetailIntent());
            List<ItemIdentity> remoteDetailIdentities = new ArrayList<>(resolved.remoteDetailIdentities());
            remoteDetailIdentities.sort(Comparator
                    .comparing(ItemIdentity::itemId)
                    .thenComparing(identity -> identity.comparisonMode().name())
                    .thenComparing(ItemIdentity::componentFingerprint));
            out.appendObject(remoteDetailIdentities);
            if (resolved.remoteStorageDetailIntent() == RemoteStorageDetailIntent.SEARCH) {
                out.appendText(resolved.searchQuery());
            }
            out.appendLong(resolved.currentTick() / 20L);
            out.appendObject(resolved.activeChestPanel());
            out.appendObject(resolved.lootChestSource());
            out.appendObject(resolved.proximateStorageIds());
            out.appendObject(resolved.contextualSuggestionStorageIds());
            out.appendObject(resolved.depositEligibleStorageIds());
            out.appendObject(resolved.carriedFluidCounts());
            appendStorageIndex(out, resolved.storageIndex());
            out.appendObject(resolved.learnedRules() == null ? List.of() : resolved.learnedRules().allRules());
            return out.toString();
        });
    }

    static String authorityKey(InventoryAuthoritySnapshot authority) {
        HashSink out = new HashSink();
        appendAuthority(out, authority);
        return out.toString();
    }

    private static void appendAuthority(HashSink out, InventoryAuthoritySnapshot authority) {
        InventoryAuthoritySnapshot resolved = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        out.appendText("authority");
        List<InventorySourceDescriptor> descriptors = new ArrayList<>(resolved.sourceDescriptors());
        descriptors.sort(Comparator.comparing(InventorySourceDescriptor::id));
        out.appendInt(descriptors.size());
        for (InventorySourceDescriptor descriptor : descriptors) {
            if (descriptor == null) {
                out.appendNull();
                continue;
            }
            out.appendText(descriptor.id());
            out.appendText(descriptor.label().getString());
            out.appendObject(descriptor.domain());
            out.appendObject(descriptor.role());
            out.appendText(descriptor.laneId());
            out.appendText(descriptor.groupId());
            out.appendInt(descriptor.logicalSlotCount());
            out.appendObject(descriptor.bindingRoute());
            out.appendObject(descriptor.actionRoute());
            out.appendObject(descriptor.paneMembership());
            out.appendInt(descriptor.stableOrder());
        }

        List<InventorySourceSnapshot> snapshots = new ArrayList<>(resolved.sourcesById().values());
        snapshots.sort(Comparator.comparing(InventorySourceSnapshot::sourceId));
        out.appendInt(snapshots.size());
        for (InventorySourceSnapshot snapshot : snapshots) {
            if (snapshot == null) {
                out.appendNull();
                continue;
            }
            out.appendText(snapshot.sourceId());
            out.appendInt(snapshot.slotCapacity());
            out.appendText(snapshot.diagnostics());
            List<InventoryEntrySnapshot> entries = new ArrayList<>(snapshot.entries());
            entries.sort(Comparator.comparing(entry -> entry == null ? "" : entry.entryKey().stableKey()));
            out.appendInt(entries.size());
            for (InventoryEntrySnapshot entry : entries) {
                appendEntry(out, entry);
            }
        }
        out.appendObject(resolved.cursorState().diagnostics());
        out.appendStack(resolved.cursorState().stack());
    }

    private static void appendEntry(HashSink out, InventoryEntrySnapshot entry) {
        if (entry == null) {
            out.appendNull();
            return;
        }
        out.appendText(entry.entryKey().stableKey());
        out.appendInt(entry.count());
        out.appendText(entry.diagnostics());
        out.appendStack(entry.stack());
    }

    private static void appendWorkflow(HashSink out, WorkflowDomainSnapshot workflow) {
        WorkflowDomainSnapshot resolved = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        out.appendText("workflow");
        out.appendLong(resolved.nextGlobalSequence());
        out.appendInt(resolved.craftRun().revision());
        out.appendObject(resolved.workflowProjection());
        out.appendObject(resolved.activityProjection());
        out.appendObject(resolved.browsePreferences());
        out.appendObject(resolved.browseSessionState());
        out.appendObject(resolved.craftRun());
        out.appendObject(resolved.contextualSuggestions());
    }

    private static void appendStorageIndex(HashSink out, WorkspaceStorageIndex index) {
        WorkspaceStorageIndex resolved = index == null ? WorkspaceStorageIndex.empty() : index;
        ArrayList<WorkspaceStorageIndex.StorageEntry> entries = new ArrayList<>(resolved.entries());
        entries.sort(Comparator.comparing(entry -> {
            if (entry == null || entry.target() == null) {
                return "";
            }
            return entry.target().storageId();
        }));
        out.appendText("storageIndex");
        out.appendLong(resolved.memoryRevision());
        out.appendObject(resolved.displaySources());
        out.appendObject(entries);
        out.appendObject(resolved.liveDepositStorageIds());
    }

    private static void appendViewModel(HashSink out, SlotWorkspaceViewModel viewModel) {
        out.appendText("viewModel");
        appendFrame(out, viewModel);
        appendWall(out, viewModel);
        appendStorage(out, viewModel);
        appendHotbar(out, viewModel);
        appendWorkflowSlice(out, viewModel);
        appendPanels(out, viewModel);
        appendContextual(out, viewModel);
    }

    private static String sliceKey(String label, HashAppender appender) {
        HashSink out = new HashSink();
        out.appendText(label);
        if (appender != null) {
            appender.append(out);
        }
        return out.toString();
    }

    private static void appendFrame(HashSink out, SlotWorkspaceViewModel viewModel) {
        out.appendText("frame");
        out.appendText(viewModel.status());
        out.appendText(viewModel.diagnostics());
        out.appendInt(viewModel.pendingCount());
        out.appendInt(viewModel.selectedQuickAccessSlot());
    }

    private static void appendWall(HashSink out, SlotWorkspaceViewModel viewModel) {
        out.appendText("wall");
        out.appendInt(viewModel.canvasWidth());
        out.appendInt(viewModel.canvasHeight());
        out.appendInt(viewModel.carriedFreeSlotCount());
        out.appendInt(viewModel.carriedSlotCapacity());
        out.appendObject(viewModel.islands());
        out.appendObject(viewModel.atlasItems());
        out.appendObject(viewModel.triageItems());
    }

    private static void appendStorage(HashSink out, SlotWorkspaceViewModel viewModel) {
        out.appendText("storage");
        out.appendObject(viewModel.chestChips());
        out.appendObject(viewModel.chestClusters());
        out.appendObject(viewModel.wayfindingTargets());
        out.appendObject(viewModel.depositableIdentities());
    }

    private static void appendHotbar(HashSink out, SlotWorkspaceViewModel viewModel) {
        out.appendText("hotbar");
        out.appendObject(viewModel.hotbarSlots());
        out.appendObject(viewModel.offhand());
        out.appendObject(viewModel.recentIdentities());
    }

    private static void appendWorkflowSlice(HashSink out, SlotWorkspaceViewModel viewModel) {
        out.appendText("workflow");
        out.appendObject(viewModel.kits());
        out.appendObject(viewModel.craftRun());
    }

    private static void appendPanels(HashSink out, SlotWorkspaceViewModel viewModel) {
        out.appendText("panels");
        out.appendObject(viewModel.lootChestPanel());
        out.appendObject(viewModel.activeChestPanel());
    }

    private static void appendContextual(HashSink out, SlotWorkspaceViewModel viewModel) {
        out.appendText("contextual");
        out.appendObject(viewModel.contextualSuggestionLanes());
    }

    @FunctionalInterface
    private interface HashAppender {
        void append(HashSink out);
    }

    private static final class HashSink {
        private static final long OFFSET = 0xcbf29ce484222325L;
        private static final long PRIME = 0x100000001b3L;

        private long hash = OFFSET;

        private void appendObject(Object value) {
            if (value == null) {
                appendNull();
            } else if (value instanceof String string) {
                appendText(string);
            } else if (value instanceof Integer integer) {
                appendInt(integer);
            } else if (value instanceof Long longValue) {
                appendLong(longValue);
            } else if (value instanceof Boolean bool) {
                appendBoolean(bool);
            } else if (value instanceof Double doubleValue) {
                appendLong(Double.doubleToLongBits(doubleValue));
            } else if (value instanceof Float floatValue) {
                appendInt(Float.floatToIntBits(floatValue));
            } else if (value instanceof Number number) {
                appendLong(number.longValue());
            } else if (value instanceof Enum<?> enumValue) {
                appendText(enumValue.getDeclaringClass().getName());
                appendText(enumValue.name());
            } else if (value instanceof ItemStack stack) {
                appendStack(stack);
            } else if (value instanceof ItemIdentity identity) {
                appendIdentity(identity);
            } else if (value instanceof SlotResourceIdentity identity) {
                appendResourceIdentity(identity);
            } else if (value instanceof Collection<?> collection) {
                appendCollection(collection);
            } else if (value instanceof Set<?> set) {
                appendSet(set);
            } else if (value instanceof Map<?, ?> map) {
                appendMap(map);
            } else if (value instanceof SlotWorkspaceViewModel.IdentityRef ref) {
                appendIdentityRef(ref);
            } else if (value instanceof SlotWorkspaceViewModel.ResourceRef ref) {
                appendResourceRef(ref);
            } else if (value instanceof SlotWorkspaceViewModel.AtlasIsland island) {
                appendAtlasIsland(island);
            } else if (value instanceof SlotWorkspaceViewModel.ContextualSuggestionLane lane) {
                appendContextualLane(lane);
            } else if (value instanceof SlotWorkspaceViewModel.ContextualSuggestionDebugInfo info) {
                appendContextualDebug(info);
            } else if (value instanceof SlotWorkspaceViewModel.AtlasItem item) {
                appendAtlasItem(item);
            } else if (value instanceof SlotWorkspaceViewModel.ChestPresenceEntry presence) {
                appendChestPresence(presence);
            } else if (value instanceof SlotWorkspaceViewModel.ChestChip chip) {
                appendChestChip(chip);
            } else if (value instanceof SlotWorkspaceViewModel.ChestContentSummary summary) {
                appendChestContentSummary(summary);
            } else if (value instanceof SlotWorkspaceViewModel.ChestClusterDescriptor cluster) {
                appendChestCluster(cluster);
            } else if (value instanceof SlotWorkspaceViewModel.LootChestSource source) {
                appendLootChestSource(source);
            } else if (value instanceof SlotWorkspaceViewModel.LootChestPanel panel) {
                appendLootChestPanel(panel);
            } else if (value instanceof SlotWorkspaceViewModel.ActiveChestPanel panel) {
                appendActiveChestPanel(panel);
            } else if (value instanceof SlotWorkspaceViewModel.ChestContentsSnapshot snapshot) {
                appendChestSnapshot(snapshot);
            } else if (value instanceof SlotWorkspaceViewModel.HotbarSlot slot) {
                appendHotbarSlot(slot);
            } else if (value instanceof SlotWorkspaceViewModel.KitCard kit) {
                appendKitCard(kit);
            } else if (value instanceof SlotWorkspaceViewModel.KitBringItem bring) {
                appendKitBring(bring);
            } else if (value instanceof SlotWorkspaceViewModel.KitPageView page) {
                appendKitPage(page);
            } else if (value instanceof SlotWorkspaceViewModel.KitSlotState slot) {
                appendKitSlot(slot);
            } else if (value instanceof SlotWorkspaceViewModel.OffhandSlot offhand) {
                appendOffhand(offhand);
            } else if (value instanceof WayfindingTarget target) {
                appendWayfindingTarget(target);
            } else if (value instanceof WorldDisplayStorageSource source) {
                appendDisplaySource(source);
            } else if (value instanceof WorldStorageAccess.SlotContent content) {
                appendSlotContent(content);
            } else if (value instanceof WorldStorageAccess.FluidContent content) {
                appendFluidContent(content);
            } else if (value instanceof StorageTargetRef target) {
                appendStorageTarget(target);
            } else if (value instanceof WorkspaceStorageIndex.StorageEntry entry) {
                appendStorageEntry(entry);
            } else if (value instanceof ChipSuggestion suggestion) {
                appendChipSuggestion(suggestion);
            } else if (value instanceof LearnedIslandRule rule) {
                appendLearnedRule(rule);
            } else if (value instanceof LearnedAdjacencyKey key) {
                appendLearnedAdjacency(key);
            } else if (value instanceof CraftRunState craftRun) {
                appendInt(craftRun.revision());
                appendText(craftRun.selectedEntryId());
                appendObject(craftRun.entries());
            } else {
                appendText(value.getClass().getName());
                appendInt(value.hashCode());
            }
        }

        private void appendCollection(Collection<?> collection) {
            if (collection instanceof Set<?> set) {
                appendSet(set);
                return;
            }
            appendText("list");
            appendInt(collection.size());
            for (Object item : collection) {
                appendObject(item);
            }
        }

        private void appendSet(Set<?> set) {
            appendText("set");
            ArrayList<Long> hashes = new ArrayList<>(set.size());
            for (Object item : set) {
                HashSink nested = new HashSink();
                nested.appendObject(item);
                hashes.add(nested.hash);
            }
            hashes.sort(Long::compareUnsigned);
            appendInt(hashes.size());
            for (Long value : hashes) {
                appendLong(value == null ? 0L : value);
            }
        }

        private void appendMap(Map<?, ?> map) {
            appendText("map");
            ArrayList<Long> hashes = new ArrayList<>(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                HashSink nested = new HashSink();
                nested.appendObject(entry.getKey());
                nested.appendObject(entry.getValue());
                hashes.add(nested.hash);
            }
            hashes.sort(Long::compareUnsigned);
            appendInt(hashes.size());
            for (Long value : hashes) {
                appendLong(value == null ? 0L : value);
            }
        }

        private void appendIdentity(ItemIdentity identity) {
            appendText(identity.itemId());
            appendObject(identity.comparisonMode());
            appendText(identity.componentFingerprint());
        }

        private void appendResourceIdentity(SlotResourceIdentity identity) {
            appendObject(identity.kind());
            appendText(identity.id());
            appendText(identity.fingerprint());
        }

        private void appendIdentityRef(SlotWorkspaceViewModel.IdentityRef ref) {
            appendText(ref.itemId());
            appendText(ref.comparisonMode());
            appendText(ref.componentFingerprint());
        }

        private void appendResourceRef(SlotWorkspaceViewModel.ResourceRef ref) {
            appendText(ref.kind());
            appendText(ref.id());
            appendText(ref.fingerprint());
        }

        private void appendAtlasIsland(SlotWorkspaceViewModel.AtlasIsland island) {
            appendText(island.islandId());
            appendText(island.label());
            appendObject(island.kind());
            appendLong(Double.doubleToLongBits(island.x()));
            appendLong(Double.doubleToLongBits(island.y()));
            appendInt(island.color());
            appendInt(island.itemCount());
            appendInt(island.carriedCount());
        }

        private void appendContextualLane(SlotWorkspaceViewModel.ContextualSuggestionLane lane) {
            appendText(lane.id());
            appendText(lane.label());
            appendObject(lane.items());
            appendText(lane.placeholderText());
            appendObject(lane.debugInfo());
        }

        private void appendContextualDebug(SlotWorkspaceViewModel.ContextualSuggestionDebugInfo info) {
            appendObject(info.identity());
            appendLong(Double.doubleToLongBits(info.score()));
            appendLong(Double.doubleToLongBits(info.relevance()));
            appendObject(info.reasons());
        }

        private void appendAtlasItem(SlotWorkspaceViewModel.AtlasItem item) {
            appendObject(item.identity());
            appendStack(item.displayStack());
            appendText(item.name());
            appendInt(item.totalCount());
            appendInt(item.firstSlotIndex());
            appendText(item.islandId());
            appendBoolean(item.recent());
            appendBoolean(item.playerPlaced());
            appendBoolean(item.carried());
            appendBoolean(item.ghost());
            appendInt(item.proximateCount());
            appendObject(item.chipSuggestions());
            appendObject(item.presence());
            appendObject(item.elsewhere());
            appendBoolean(item.isCarriedContainer());
            appendInt(item.containerFreeSlotCount());
            appendInt(item.containerSlotCapacity());
            appendBoolean(item.kitNeeded());
            appendInt(item.desiredCount());
            appendBoolean(item.desiredCountFromKit());
            appendInt(item.wantedCount());
            appendBoolean(item.junk());
            appendBoolean(item.acceptedWorkflowInput());
            appendText(item.largestCarriedSourceId());
            appendInt(item.largestCarriedSlotIndex());
            appendInt(item.largestCarriedSlotCount());
            appendObject(item.putAwayState());
            appendObject(item.resource());
            appendLong(item.resourceAmount());
        }

        private void appendChestPresence(SlotWorkspaceViewModel.ChestPresenceEntry presence) {
            appendText(presence.storageId());
            appendText(presence.label());
            appendInt(presence.count());
        }

        private void appendChestChip(SlotWorkspaceViewModel.ChestChip chip) {
            appendText(chip.storageId());
            appendText(chip.dimensionId());
            appendText(chip.label());
            appendInt(chip.anchorCount());
            appendInt(chip.slotCapacity());
            appendInt(chip.filledSlots());
            appendBoolean(chip.proximate());
            appendInt(chip.affinityIdentities());
            appendInt(chip.worldX());
            appendInt(chip.worldY());
            appendInt(chip.worldZ());
            appendText(chip.clusterId());
            appendObject(chip.contents());
        }

        private void appendChestContentSummary(SlotWorkspaceViewModel.ChestContentSummary summary) {
            appendText(summary.itemId());
            appendText(summary.componentFingerprint());
            appendText(summary.name());
            appendStack(summary.displayStack());
            appendInt(summary.count());
        }

        private void appendChestCluster(SlotWorkspaceViewModel.ChestClusterDescriptor cluster) {
            appendText(cluster.clusterId());
            appendText(cluster.label());
            appendInt(cluster.ordinal());
        }

        private void appendLootChestSource(SlotWorkspaceViewModel.LootChestSource source) {
            appendInt(source.chestX());
            appendInt(source.chestY());
            appendInt(source.chestZ());
            appendText(source.dimensionId());
            appendText(source.label());
            appendObject(source.contents());
        }

        private void appendLootChestPanel(SlotWorkspaceViewModel.LootChestPanel panel) {
            appendInt(panel.chestX());
            appendInt(panel.chestY());
            appendInt(panel.chestZ());
            appendText(panel.dimensionId());
            appendText(panel.label());
            appendObject(panel.items());
        }

        private void appendActiveChestPanel(SlotWorkspaceViewModel.ActiveChestPanel panel) {
            appendText(panel.storageId());
            appendText(panel.label());
            appendText(panel.clusterId());
            appendText(panel.clusterLabel());
            appendInt(panel.swatchColor());
            appendInt(panel.posX());
            appendInt(panel.posY());
            appendInt(panel.posZ());
            appendText(panel.dimensionId());
            appendObject(panel.role());
            appendObject(panel.affinityIdentities());
        }

        private void appendChestSnapshot(SlotWorkspaceViewModel.ChestContentsSnapshot snapshot) {
            appendInt(snapshot.slotCount());
            appendObject(snapshot.slotIndices());
            appendObject(snapshot.contents());
            appendObject(snapshot.countsByIdentity());
            appendObject(snapshot.fluidCountsByIdentity());
        }

        private void appendHotbarSlot(SlotWorkspaceViewModel.HotbarSlot slot) {
            appendInt(slot.hotbarIndex());
            appendBoolean(slot.selected());
            appendBoolean(slot.occupied());
            appendStack(slot.displayStack());
            appendInt(slot.count());
        }

        private void appendKitCard(SlotWorkspaceViewModel.KitCard kit) {
            appendText(kit.kitId());
            appendText(kit.name());
            appendText(kit.parentId());
            appendInt(kit.pageCount());
            appendInt(kit.activePageIndex());
            appendBoolean(kit.active());
            appendBoolean(kit.variant());
            appendInt(kit.memberCount());
            appendObject(kit.members());
            appendObject(kit.acceptedInputs());
            appendInt(kit.slotCount());
            appendInt(kit.readyCount());
            appendInt(kit.carriedSlotCount());
            appendInt(kit.carriedSlotCapacity());
            appendInt(kit.bringSlotCount());
            appendInt(kit.bringReadyCount());
            appendObject(kit.slots());
            appendObject(kit.pages());
            appendObject(kit.bring());
        }

        private void appendKitBring(SlotWorkspaceViewModel.KitBringItem bring) {
            appendObject(bring.identity());
            appendBoolean(bring.ready());
            appendStack(bring.displayStack());
            appendText(bring.name());
            appendInt(bring.presentCount());
            appendInt(bring.targetCount());
        }

        private void appendKitPage(SlotWorkspaceViewModel.KitPageView page) {
            appendInt(page.pageIndex());
            appendInt(page.slotCount());
            appendInt(page.readyCount());
            appendObject(page.slots());
        }

        private void appendKitSlot(SlotWorkspaceViewModel.KitSlotState slot) {
            appendInt(slot.slotIndex());
            appendBoolean(slot.filled());
            appendBoolean(slot.ready());
            appendObject(slot.identity());
            appendStack(slot.displayStack());
            appendText(slot.name());
        }

        private void appendOffhand(SlotWorkspaceViewModel.OffhandSlot offhand) {
            appendBoolean(offhand.occupied());
            appendStack(offhand.displayStack());
            appendInt(offhand.count());
        }

        private void appendWayfindingTarget(WayfindingTarget target) {
            appendText(target.storageId());
            appendText(target.dimensionId());
            appendInt(target.worldX());
            appendInt(target.worldY());
            appendInt(target.worldZ());
            appendObject(target.missingIdentities());
            appendObject(target.kitMissingIdentities());
            appendObject(target.desiredMissingIdentities());
            appendObject(target.wantedMissingIdentities());
            appendObject(target.putAwayIdentities());
            appendInt(target.totalMissingCount());
            appendObject(target.scope());
        }

        private void appendDisplaySource(WorldDisplayStorageSource source) {
            appendText(source.storageId());
            appendObject(source.kind());
            appendText(source.label());
            appendText(source.dimensionId());
            appendInt(source.x());
            appendInt(source.y());
            appendInt(source.z());
            appendInt(source.slotCount());
            appendObject(source.contents());
            appendObject(source.fluidContents());
            appendObject(source.aliasedBlocks());
            appendObject(source.mediaIds());
            appendObject(source.mediaObservations());
            appendObject(source.target());
        }

        private void appendSlotContent(WorldStorageAccess.SlotContent content) {
            appendInt(content.slotIndex());
            appendStack(content.stack());
            appendInt(content.count());
        }

        private void appendFluidContent(WorldStorageAccess.FluidContent content) {
            appendInt(content.tankIndex());
            appendInt(content.containingSlotIndex());
            appendObject(content.identity());
            appendLong(content.amount());
            appendText(content.label());
        }

        private void appendStorageTarget(StorageTargetRef target) {
            appendText(target.storageId());
            appendText(target.targetKind());
            appendText(target.label());
            appendText(target.dimensionId());
            appendInt(target.x());
            appendInt(target.y());
            appendInt(target.z());
            appendBoolean(target.liveReadable());
            appendBoolean(target.depositTarget());
            appendBoolean(target.takeTarget());
            appendBoolean(target.remembered());
            appendBoolean(target.proximate());
        }

        private void appendStorageEntry(WorkspaceStorageIndex.StorageEntry entry) {
            appendObject(entry.target());
            appendObject(entry.snapshot());
            if (entry.snapshot().countsByIdentity().isEmpty()) {
                appendObject(entry.countsByIdentity());
            }
            if (entry.snapshot().fluidCountsByIdentity().isEmpty()) {
                appendObject(entry.fluidCountsByIdentity());
            }
            appendBoolean(entry.live());
            appendBoolean(entry.remembered());
        }

        private void appendChipSuggestion(ChipSuggestion suggestion) {
            appendObject(suggestion.kind());
            appendObject(suggestion.template());
            appendText(suggestion.islandId());
            appendText(suggestion.label());
            appendInt(suggestion.color());
            appendObject(suggestion.iconIdentity());
        }

        private void appendLearnedRule(LearnedIslandRule rule) {
            appendObject(rule.adjacency());
            appendText(rule.islandId());
            appendObject(rule.confirmingIdentities());
            appendLong(rule.lastConfirmedAtEpochMillis());
        }

        private void appendLearnedAdjacency(LearnedAdjacencyKey key) {
            appendObject(key.kind());
            appendText(key.value());
        }

        private void appendStack(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                appendText("empty-stack");
                return;
            }
            appendText(SlotStackAccess.current().itemId(stack));
            appendInt(stack.getCount());
            appendInt(stack.getMaxStackSize());
            appendText(SlotStackAccess.current().dataFingerprint(stack));
            appendText(stack.getHoverName().getString());
        }

        private void appendNull() {
            appendLong(0x9e3779b97f4a7c15L);
        }

        private void appendBoolean(boolean value) {
            appendLong(value ? 0x9e3779b97f4a7c15L : 0xbf58476d1ce4e5b9L);
        }

        private void appendInt(int value) {
            appendLong(value);
        }

        private void appendLong(long value) {
            mix(value);
            mix(value >>> 32);
        }

        private void appendText(String value) {
            String text = value == null ? "" : value;
            appendInt(text.length());
            for (int index = 0; index < text.length(); index++) {
                mix(text.charAt(index));
            }
        }

        private void mix(long value) {
            hash ^= value & 0xffL;
            hash *= PRIME;
            hash ^= (value >>> 8) & 0xffL;
            hash *= PRIME;
            hash ^= (value >>> 16) & 0xffL;
            hash *= PRIME;
            hash ^= (value >>> 24) & 0xffL;
            hash *= PRIME;
            hash ^= (value >>> 32) & 0xffL;
            hash *= PRIME;
            hash ^= (value >>> 40) & 0xffL;
            hash *= PRIME;
            hash ^= (value >>> 48) & 0xffL;
            hash *= PRIME;
            hash ^= (value >>> 56) & 0xffL;
            hash *= PRIME;
        }

        @Override
        public String toString() {
            return Long.toUnsignedString(hash, 16);
        }
    }
}
