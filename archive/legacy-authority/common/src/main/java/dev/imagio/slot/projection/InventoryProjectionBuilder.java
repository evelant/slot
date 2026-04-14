package dev.imagio.slot.projection;

import dev.imagio.slot.client.collection.CollectionStore;
import dev.imagio.slot.client.model.ComparisonMode;
import dev.imagio.slot.client.model.ItemIdentity;
import dev.imagio.slot.client.policy.ItemBehaviorPolicy;
import dev.imagio.slot.projection.InventoryPane;
import dev.imagio.slot.projection.InventoryViewData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntBiFunction;

public final class InventoryProjectionBuilder {
    private InventoryProjectionBuilder() {
    }

    public static InventoryProjection buildWorkspace(WorkspaceInput input) {
        if (input == null || input.sections().isEmpty()) {
            return InventoryProjection.empty();
        }

        EnumMap<InventoryPane, PaneBuildState> paneStates = new EnumMap<>(InventoryPane.class);
        for (InventoryPane pane : InventoryPane.values()) {
            paneStates.put(pane, new PaneBuildState(pane));
        }

        LinkedHashMap<String, Integer> combinedSectionCounts = new LinkedHashMap<>();
        for (InventoryViewData.Section section : input.sections()) {
            PaneSectionOptions openOptions = requireOptions(input.sectionPolicy().sectionOptions(section, InventoryPane.OPEN_CONTAINER));
            PaneSectionOptions carriedOptions = requireOptions(input.sectionPolicy().sectionOptions(section, InventoryPane.CARRIED));

            InventoryVisibleEntrySupport.PaneSectionEntries paneEntries = InventoryVisibleEntrySupport.paneSectionEntries(
                    section,
                    input.entries(),
                    section.isRecent() ? input.recentCarriedEntries() : List.of(),
                    input.query(),
                    input.collectionStore(),
                    input.comparator(),
                    openOptions.visible(),
                    input.localCount()
            );

            combinedSectionCounts.put(section.id(), paneEntries.combinedEntries().size());
            applySectionPlan(paneStates.get(InventoryPane.OPEN_CONTAINER), section, paneEntries.openPaneEntries(), openOptions);
            applySectionPlan(paneStates.get(InventoryPane.CARRIED), section, paneEntries.carriedPaneEntries(), carriedOptions);
        }

        EnumMap<InventoryPane, InventoryProjection.PaneProjection> panes = new EnumMap<>(InventoryPane.class);
        for (Map.Entry<InventoryPane, PaneBuildState> entry : paneStates.entrySet()) {
            panes.put(entry.getKey(), entry.getValue().toProjection());
        }
        return new InventoryProjection(Collections.unmodifiableMap(panes), Collections.unmodifiableMap(combinedSectionCounts));
    }

    private static void applySectionPlan(
            PaneBuildState paneState,
            InventoryViewData.Section section,
            List<InventoryViewData.EntryView> entries,
            PaneSectionOptions options
    ) {
        if (paneState == null || section == null || options == null || !options.visible()) {
            return;
        }

        List<InventoryViewData.EntryView> safeEntries = List.copyOf(entries == null ? List.of() : entries);
        InventorySectionRowPlan.Plan plan = InventorySectionRowPlan.plan(
                section,
                safeEntries,
                new InventorySectionRowPlan.Options(
                        options.retainEmptySection(),
                        options.expanded(),
                        options.includeLoadoutRow(),
                        options.includeLoadoutPreviewRow()
                )
        );
        if (!plan.included()) {
            return;
        }

        paneState.visibleEntriesBySection.put(section.id(), safeEntries);
        paneState.sectionCounts.put(section.id(), plan.sectionEntryCount());
        paneState.visibleEntryCount += plan.visibleItemCount();
        for (InventorySectionRowPlan.Row row : plan.rows()) {
            paneState.rows.add(toProjectionRow(paneState.pane, row, plan.sectionEntryCount()));
        }
    }

    private static InventoryProjection.RowProjection toProjectionRow(
            InventoryPane pane,
            InventorySectionRowPlan.Row row,
            int sectionEntryCount
    ) {
        return switch (row.kind()) {
            case SECTION -> new InventoryProjection.SectionRowProjection(
                    sectionRowId(pane, row.section()),
                    pane,
                    row.section(),
                    sectionEntryCount
            );
            case LOADOUT -> new InventoryProjection.LoadoutRowProjection(
                    loadoutRowId(pane, row.section()),
                    pane,
                    row.section()
            );
            case LOADOUT_PREVIEW -> new InventoryProjection.LoadoutPreviewRowProjection(
                    loadoutPreviewRowId(pane, row.section()),
                    pane,
                    row.section()
            );
            case ITEM -> new InventoryProjection.ItemRowProjection(
                    itemRowId(pane, row.section(), row.entry()),
                    pane,
                    row.section(),
                    Objects.requireNonNull(row.entry(), "row.entry")
            );
        };
    }

    private static PaneSectionOptions requireOptions(PaneSectionOptions options) {
        return Objects.requireNonNull(options, "PaneSectionOptions must not be null");
    }

    private static String sectionRowId(InventoryPane pane, InventoryViewData.Section section) {
        return paneId(pane) + "/section/" + section.id();
    }

    private static String loadoutRowId(InventoryPane pane, InventoryViewData.Section section) {
        return paneId(pane) + "/loadout/" + section.id();
    }

    private static String loadoutPreviewRowId(InventoryPane pane, InventoryViewData.Section section) {
        return paneId(pane) + "/loadout_preview/" + section.id();
    }

    private static String itemRowId(
            InventoryPane pane,
            InventoryViewData.Section section,
            InventoryViewData.EntryView entry
    ) {
        ItemIdentity normalizedIdentity = ItemBehaviorPolicy.normalizeTrackedIdentity(entry.itemEntry().identity());
        return paneId(pane) + "/item/" + section.id() + "/" + normalizedIdentityKey(normalizedIdentity);
    }

    private static String paneId(InventoryPane pane) {
        return pane == InventoryPane.OPEN_CONTAINER ? "open" : "carried";
    }

    private static String normalizedIdentityKey(ItemIdentity identity) {
        String mode = identity.comparisonMode() == ComparisonMode.ITEM_ID_AND_COMPONENTS
                ? "item_components"
                : "item";
        if (identity.componentFingerprint().isBlank()) {
            return identity.itemId() + "/" + mode;
        }
        String fingerprint = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(identity.componentFingerprint().getBytes(StandardCharsets.UTF_8));
        return identity.itemId() + "/" + mode + "/" + fingerprint;
    }

    public interface SectionPolicy {
        PaneSectionOptions sectionOptions(InventoryViewData.Section section, InventoryPane pane);
    }

    public record PaneSectionOptions(
            boolean visible,
            boolean retainEmptySection,
            boolean expanded,
            boolean includeLoadoutRow,
            boolean includeLoadoutPreviewRow
    ) {
    }

    public record WorkspaceInput(
            List<InventoryViewData.Section> sections,
            List<InventoryViewData.EntryView> entries,
            List<InventoryViewData.EntryView> recentCarriedEntries,
            String query,
            CollectionStore collectionStore,
            Comparator<InventoryViewData.EntryView> comparator,
            ToIntBiFunction<InventoryViewData.EntryView, InventoryPane> localCount,
            SectionPolicy sectionPolicy
    ) {
        public WorkspaceInput {
            sections = List.copyOf(sections == null ? List.of() : sections);
            entries = List.copyOf(entries == null ? List.of() : entries);
            recentCarriedEntries = List.copyOf(recentCarriedEntries == null ? List.of() : recentCarriedEntries);
            query = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
            collectionStore = Objects.requireNonNull(collectionStore, "collectionStore");
            comparator = Objects.requireNonNull(comparator, "comparator");
            localCount = Objects.requireNonNull(localCount, "localCount");
            sectionPolicy = Objects.requireNonNull(sectionPolicy, "sectionPolicy");
        }
    }

    private static final class PaneBuildState {
        private final InventoryPane pane;
        private final List<InventoryProjection.RowProjection> rows = new ArrayList<>();
        private final LinkedHashMap<String, List<InventoryViewData.EntryView>> visibleEntriesBySection = new LinkedHashMap<>();
        private final LinkedHashMap<String, Integer> sectionCounts = new LinkedHashMap<>();
        private int visibleEntryCount;

        private PaneBuildState(InventoryPane pane) {
            this.pane = pane;
        }

        private InventoryProjection.PaneProjection toProjection() {
            return new InventoryProjection.PaneProjection(
                    pane,
                    rows,
                    visibleEntriesBySection,
                    sectionCounts,
                    visibleEntryCount
            );
        }
    }
}
