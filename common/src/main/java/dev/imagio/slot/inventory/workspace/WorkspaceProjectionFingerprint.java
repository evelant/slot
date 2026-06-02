package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.platform.SlotStackAccess;
import dev.imagio.slot.workflow.domain.WorkflowDomainSnapshot;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.RecordComponent;
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
        Fingerprinter out = new Fingerprinter();
        out.appendValue(viewModel == null ? SlotWorkspaceViewModel.empty() : viewModel, "revision");
        return out.toString();
    }

    static String inputKey(WorkspaceProjectionRequest request, ItemIdentityMatcher.Memo memo) {
        WorkspaceProjectionRequest resolved = request == null
                ? new WorkspaceProjectionRequest(
                        null, null, "ready", "", 0, -1, 0,
                        null, null, null, null, null, null, "",
                        0L, null, null, null, null, null, null, null, null, null)
                : request;
        return ItemIdentityMatcher.withMemo(memo, () -> {
            Fingerprinter out = new Fingerprinter();
            appendAuthority(out, resolved.authority());
            appendWorkflow(out, resolved.workflow());
            out.appendText("search=").appendText(resolved.searchQuery()).separator();
            out.appendText("tickBucket=").appendLong(resolved.currentTick() / 20L).separator();
            out.appendText("activeChest=").appendValue(resolved.activeChestPanel()).separator();
            out.appendText("lootChest=").appendValue(resolved.lootChestSource()).separator();
            out.appendText("proximate=").appendValue(resolved.proximateStorageIds()).separator();
            out.appendText("contextualStorage=").appendValue(resolved.contextualSuggestionStorageIds()).separator();
            out.appendText("displaySources=").appendValue(resolved.worldDisplaySources()).separator();
            out.appendText("contextualDisplays=").appendValue(resolved.contextualSuggestionDisplaySources()).separator();
            out.appendText("trackedDisplays=").appendValue(resolved.trackedDisplayStorageEntries()).separator();
            out.appendText("depositEligible=").appendValue(resolved.depositEligibleStorageIds()).separator();
            appendStorageIndex(out, resolved.storageIndex());
            out.appendText("learnedRules=")
                    .appendValue(resolved.learnedRules() == null ? List.of() : resolved.learnedRules().allRules())
                    .separator();
            return out.toString();
        });
    }

    private static void appendAuthority(Fingerprinter out, InventoryAuthoritySnapshot authority) {
        InventoryAuthoritySnapshot resolved = authority == null ? InventoryAuthoritySnapshot.empty() : authority;
        out.appendText("authority{");
        out.appendText("sources=");
        List<InventorySourceDescriptor> descriptors = new ArrayList<>(resolved.sourceDescriptors());
        descriptors.sort(Comparator.comparing(InventorySourceDescriptor::id));
        for (InventorySourceDescriptor descriptor : descriptors) {
            if (descriptor == null) {
                continue;
            }
            out.appendText("[")
                    .appendText(descriptor.id()).separator()
                    .appendText(descriptor.label().getString()).separator()
                    .appendText(String.valueOf(descriptor.domain())).separator()
                    .appendText(String.valueOf(descriptor.role())).separator()
                    .appendText(descriptor.laneId()).separator()
                    .appendText(descriptor.groupId()).separator()
                    .appendInt(descriptor.logicalSlotCount()).separator()
                    .appendText(String.valueOf(descriptor.bindingRoute())).separator()
                    .appendText(String.valueOf(descriptor.actionRoute())).separator()
                    .appendText(String.valueOf(descriptor.paneMembership())).separator()
                    .appendInt(descriptor.stableOrder())
                    .appendText("]");
        }
        out.separator().appendText("snapshots=");
        List<InventorySourceSnapshot> snapshots = new ArrayList<>(resolved.sourcesById().values());
        snapshots.sort(Comparator.comparing(InventorySourceSnapshot::sourceId));
        for (InventorySourceSnapshot snapshot : snapshots) {
            if (snapshot == null) {
                continue;
            }
            out.appendText("[")
                    .appendText(snapshot.sourceId()).separator()
                    .appendInt(snapshot.slotCapacity()).separator()
                    .appendText(snapshot.diagnostics()).separator();
            List<InventoryEntrySnapshot> entries = new ArrayList<>(snapshot.entries());
            entries.sort(Comparator.comparing(entry -> entry == null ? "" : entry.entryKey().stableKey()));
            for (InventoryEntrySnapshot entry : entries) {
                appendEntry(out, entry);
            }
            out.appendText("]");
        }
        out.separator().appendText("cursor=").appendValue(resolved.cursorState());
        out.appendText("}");
    }

    private static void appendEntry(Fingerprinter out, InventoryEntrySnapshot entry) {
        if (entry == null) {
            out.appendText("null-entry;");
            return;
        }
        out.appendText("{")
                .appendText(entry.entryKey().stableKey()).separator()
                .appendInt(entry.count()).separator()
                .appendText(entry.diagnostics()).separator()
                .appendStack(entry.stack())
                .appendText("}");
    }

    private static void appendWorkflow(Fingerprinter out, WorkflowDomainSnapshot workflow) {
        WorkflowDomainSnapshot resolved = workflow == null ? WorkflowDomainSnapshot.empty() : workflow;
        out.appendText("workflow{")
                .appendLong(resolved.nextGlobalSequence()).separator()
                .appendValue(resolved.workflowProjection()).separator()
                .appendValue(resolved.activityProjection()).separator()
                .appendValue(resolved.browsePreferences()).separator()
                .appendValue(resolved.browseSessionState()).separator()
                .appendValue(resolved.craftRun()).separator()
                .appendValue(resolved.contextualSuggestions())
                .appendText("}");
    }

    private static void appendStorageIndex(Fingerprinter out, WorkspaceStorageIndex index) {
        WorkspaceStorageIndex resolved = index == null ? WorkspaceStorageIndex.empty() : index;
        ArrayList<WorkspaceStorageIndex.StorageEntry> entries = new ArrayList<>(resolved.entries());
        entries.sort(Comparator.comparing(entry -> {
            if (entry == null || entry.target() == null) {
                return "";
            }
            return entry.target().storageId();
        }));
        out.appendText("storageIndex{")
                .appendLong(resolved.memoryRevision()).separator()
                .appendValue(resolved.displaySources()).separator()
                .appendValue(entries).separator()
                .appendValue(resolved.liveDepositStorageIds())
                .appendText("}");
    }

    private static final class Fingerprinter {
        private final StringBuilder builder = new StringBuilder(4096);

        private Fingerprinter appendValue(Object value) {
            return appendValue(value, "");
        }

        private Fingerprinter appendValue(Object value, String skippedRecordComponent) {
            if (value == null) {
                return appendText("<null>");
            }
            if (value instanceof String string) {
                return appendText("s:").appendText(string);
            }
            if (value instanceof Number number) {
                return appendText("n:").appendText(number.toString());
            }
            if (value instanceof Boolean bool) {
                return appendText("b:").appendText(Boolean.toString(bool));
            }
            if (value instanceof Enum<?> enumValue) {
                return appendText("e:").appendText(enumValue.getDeclaringClass().getName())
                        .appendText("#").appendText(enumValue.name());
            }
            if (value instanceof ItemStack stack) {
                return appendStack(stack);
            }
            if (value instanceof Map<?, ?> map) {
                return appendMap(map);
            }
            if (value instanceof Set<?> set) {
                return appendSet(set);
            }
            if (value instanceof Collection<?> collection) {
                return appendCollection(collection);
            }
            if (value.getClass().isArray()) {
                return appendArray(value);
            }
            if (value.getClass().isRecord()) {
                return appendRecord(value, skippedRecordComponent);
            }
            return appendText("o:").appendText(value.getClass().getName()).appendText(":").appendText(String.valueOf(value));
        }

        private Fingerprinter appendRecord(Object value, String skippedRecordComponent) {
            appendText("r:").appendText(value.getClass().getName()).appendText("{");
            for (RecordComponent component : value.getClass().getRecordComponents()) {
                if (component == null || component.getName().equals(skippedRecordComponent)) {
                    continue;
                }
                appendText(component.getName()).appendText("=");
                try {
                    appendValue(component.getAccessor().invoke(value));
                } catch (ReflectiveOperationException | RuntimeException exception) {
                    appendText("<unreadable:").appendText(component.getName()).appendText(">");
                }
                separator();
            }
            return appendText("}");
        }

        private Fingerprinter appendCollection(Collection<?> collection) {
            appendText("c[");
            for (Object item : collection) {
                appendValue(item).separator();
            }
            return appendText("]");
        }

        private Fingerprinter appendSet(Set<?> set) {
            ArrayList<String> values = new ArrayList<>();
            for (Object item : set) {
                values.add(new Fingerprinter().appendValue(item).toString());
            }
            values.sort(String::compareTo);
            appendText("set[");
            for (String value : values) {
                appendText(value).separator();
            }
            return appendText("]");
        }

        private Fingerprinter appendMap(Map<?, ?> map) {
            ArrayList<String> values = new ArrayList<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Fingerprinter nested = new Fingerprinter();
                nested.appendValue(entry.getKey()).appendText("=>").appendValue(entry.getValue());
                values.add(nested.toString());
            }
            values.sort(String::compareTo);
            appendText("map[");
            for (String value : values) {
                appendText(value).separator();
            }
            return appendText("]");
        }

        private Fingerprinter appendArray(Object array) {
            appendText("a[");
            int length = Array.getLength(array);
            for (int index = 0; index < length; index++) {
                appendValue(Array.get(array, index)).separator();
            }
            return appendText("]");
        }

        private Fingerprinter appendStack(ItemStack stack) {
            if (stack == null || stack.isEmpty()) {
                return appendText("stack:<empty>");
            }
            appendText("stack{")
                    .appendText(SlotStackAccess.current().itemId(stack)).separator()
                    .appendInt(stack.getCount()).separator()
                    .appendInt(stack.getMaxStackSize()).separator()
                    .appendText(SlotStackAccess.current().dataFingerprint(stack)).separator()
                    .appendText(stack.getHoverName().getString())
                    .appendText("}");
            return this;
        }

        private Fingerprinter appendText(String value) {
            builder.append(value == null ? "" : value.replace("\\", "\\\\").replace("|", "\\|"));
            return this;
        }

        private Fingerprinter appendInt(int value) {
            builder.append(value);
            return this;
        }

        private Fingerprinter appendLong(long value) {
            builder.append(value);
            return this;
        }

        private Fingerprinter separator() {
            builder.append('|');
            return this;
        }

        @Override
        public String toString() {
            return builder.toString();
        }
    }
}
