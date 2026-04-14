package dev.imagio.slot.policy;

import dev.imagio.slot.source.SourceCapability;
import dev.imagio.slot.source.SourceDescriptor;
import dev.imagio.slot.source.SourceGroup;
import dev.imagio.slot.source.SourceId;
import dev.imagio.slot.source.SourceKind;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SourcePreferencePolicyTest {
    private final SourcePreferencePolicy policy = new SourcePreferencePolicy();

    @Test
    void prefersExplicitSourceWhenAvailable() {
        SourceDescriptor main = descriptor("main", SourceGroup.PLAYER_MAIN, 0, false, true, true);
        SourceDescriptor bag = descriptor("bag", SourceGroup.CARRIED, 1, true, true, true);

        SourceDescriptor selected = policy.chooseExistingSource(
                        java.util.List.of(main, bag),
                        new SourceSelectionPreference(SourceId.of("bag"), null, null)
                )
                .orElseThrow();

        assertEquals("bag", selected.id().value());
    }

    @Test
    void fallsBackToPlayerMainBeforeHotbarAndExternal() {
        SourceDescriptor external = descriptor("open_container", SourceGroup.EXTERNAL, 0, false, true, true);
        SourceDescriptor hotbar = descriptor("hotbar", SourceGroup.PLAYER_HOTBAR, 0, false, true, true);
        SourceDescriptor main = descriptor("main", SourceGroup.PLAYER_MAIN, 0, false, true, true);

        SourceDescriptor selected = policy.chooseExistingSource(
                        java.util.List.of(external, hotbar, main),
                        new SourceSelectionPreference(null, null, null)
                )
                .orElseThrow();

        assertEquals("main", selected.id().value());
    }

    @Test
    void targetSelectionSkipsNonInsertablePreferredSource() {
        SourceDescriptor readOnlyBag = descriptor("bag", SourceGroup.CARRIED, 0, true, false, true);
        SourceDescriptor hotbar = descriptor("hotbar", SourceGroup.PLAYER_HOTBAR, 0, false, true, true);

        SourceDescriptor selected = policy.chooseTargetSource(
                        java.util.List.of(readOnlyBag, hotbar),
                        new SourceSelectionPreference(SourceId.of("bag"), null, null)
                )
                .orElseThrow();

        assertEquals("hotbar", selected.id().value());
    }

    private static SourceDescriptor descriptor(
            String id,
            SourceGroup group,
            int stableOrder,
            boolean primaryCarried,
            boolean canInsert,
            boolean canExtract
    ) {
        Set<SourceCapability> capabilities = new java.util.LinkedHashSet<>();
        capabilities.add(SourceCapability.BROWSE_SNAPSHOT);
        if (canInsert) {
            capabilities.add(SourceCapability.INSERT);
        }
        if (canExtract) {
            capabilities.add(SourceCapability.EXTRACT_ONE);
            capabilities.add(SourceCapability.EXTRACT_STACK);
            capabilities.add(SourceCapability.EXTRACT_ALL_MATCHING);
        }
        return new SourceDescriptor(
                SourceId.of(id),
                id,
                id,
                group,
                SourceKind.UNKNOWN,
                capabilities,
                stableOrder,
                primaryCarried,
                "test"
        );
    }
}
