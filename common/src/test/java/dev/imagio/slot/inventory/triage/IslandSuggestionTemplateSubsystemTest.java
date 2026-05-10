package dev.imagio.slot.inventory.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IslandSuggestionTemplateSubsystemTest {

    @Test
    void qualifiedSubsystemReturnsSubsystemMatchInheritingParentColor() {
        IslandSignalDescriptor descriptor = subsystemDescriptor(
                "create:cogwheel",
                "mechanism",
                List.of("create:mechanical_power"),
                List.of()
        );
        IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, id -> "create:mechanical_power".equals(id));
        assertTrue(match.isSubsystem());
        assertEquals("subsystem:create:mechanical_power", match.islandId());
        assertEquals("Create — Mechanical Power", match.label());
        // Subsystem inherits color and cluster placement from the parent
        // role-resolved template (MECHANISMS for "mechanism").
        assertEquals(IslandSuggestionTemplate.MECHANISMS, match.parentTemplate());
        assertEquals(IslandSuggestionTemplate.MECHANISMS.defaultColor(), match.color());
        assertEquals(IslandSuggestionTemplate.MECHANISMS.clusterRow(), match.clusterRow());
    }

    @Test
    void unqualifiedSubsystemFallsBackToParentTemplate() {
        IslandSignalDescriptor descriptor = subsystemDescriptor(
                "modded:tiny_gizmo",
                "mechanism",
                List.of("modded:tiny_subsystem"),
                List.of()
        );
        IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, id -> false);
        assertFalse(match.isSubsystem());
        assertEquals(IslandSuggestionTemplate.MECHANISMS, match.parentTemplate());
        assertEquals(IslandSuggestionTemplate.MECHANISMS.defaultIslandId(), match.islandId());
    }

    @Test
    void broadUtilityAndMaterialParentsCanHonorQualifiedSubsystems() {
        IslandTemplateMatch casting = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                subsystemDescriptor("tfc:ceramic/ingot_mold", "utility", List.of("tfc:casting"), List.of()),
                id -> "tfc:casting".equals(id));
        assertTrue(casting.isSubsystem());
        assertEquals(IslandSuggestionTemplate.UTILITY, casting.parentTemplate());

        IslandTemplateMatch weaving = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                subsystemDescriptor("tfc:fiber/linen_thread", "material", List.of("tfc:weaving"), List.of()),
                id -> "tfc:weaving".equals(id));
        assertTrue(weaving.isSubsystem());
        assertEquals(IslandSuggestionTemplate.MATERIALS, weaving.parentTemplate());
    }

    @Test
    void decorationParentNeverHonorsSubsystem() {
        // Even with a fully-qualified subsystem, decoration items don't
        // get a "create:decoration" island — players don't think of
        // "Create's decoration" as separate from vanilla decoration.
        IslandSignalDescriptor descriptor = subsystemDescriptor(
                "create:diamond_lattice",
                "decorative_block",
                List.of("create:decoration"),
                List.of()
        );
        IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, id -> true);
        assertFalse(match.isSubsystem());
        assertEquals(IslandSuggestionTemplate.DECORATION, match.parentTemplate());
    }

    @Test
    void upgradeParentNeverHonorsSubsystem() {
        // Smithing templates / upgrade modules go to UPGRADES regardless of
        // subsystem grouping. Players don't subdivide their upgrades pile
        // by mod source.
        IslandSignalDescriptor descriptor = subsystemDescriptor(
                "modded:fancy_upgrade",
                "upgrade",
                List.of("modded:tier_upgrade"),
                List.of()
        );
        IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, id -> true);
        assertFalse(match.isSubsystem());
        assertEquals(IslandSuggestionTemplate.UPGRADES, match.parentTemplate());
    }

    @Test
    void storageParentNeverHonorsSubsystem() {
        // Storage items live in a single "Storage" pile; players don't want
        // sophisticatedstorage:barrel as a separate island from chests.
        IslandSignalDescriptor descriptor = subsystemDescriptor(
                "sophisticatedstorage:diamond_barrel",
                "storage_block",
                List.of("sophisticatedstorage:barrel"),
                List.of()
        );
        IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, id -> true);
        assertFalse(match.isSubsystem());
        assertEquals(IslandSuggestionTemplate.STORAGE, match.parentTemplate());
    }

    @Test
    void vanillaItemWithoutSubsystemUnchanged() {
        IslandSignalDescriptor descriptor = new IslandSignalDescriptor(
                ItemIdentity.of("minecraft:diamond_pickaxe"),
                Set.of(IslandSignal.DIGGER_TOOL),
                Set.of(),
                "minecraft",
                "",
                "tool"
        );
        IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, id -> true);
        assertFalse(match.isSubsystem());
        assertEquals(IslandSuggestionTemplate.TOOLS, match.parentTemplate());
        assertEquals(IslandSuggestionTemplate.TOOLS.defaultIslandId(), match.islandId());
    }

    @Test
    void trophyShuntBypassesSubsystemEvenIfQualified() {
        // nether_star carries role=trophy + rarity=rare; even if a
        // hypothetical subsystem qualified, trophies belong on display
        // (CURIOSITY), not filed under a mod's subsystem.
        IslandSignalDescriptor descriptor = subsystemDescriptor(
                "minecraft:nether_star",
                "trophy",
                List.of("create:mechanical_power"),
                List.of()
        );
        IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, id -> true);
        assertFalse(match.isSubsystem());
        assertEquals(IslandSuggestionTemplate.CURIOSITY, match.parentTemplate());
    }

    @Test
    void uniqueRarityAlsoTriggersTrophyShunt() {
        IslandSignalDescriptor descriptor = new IslandSignalDescriptor(
                ItemIdentity.of("modded:rare_artifact"),
                Set.of(),
                Set.of(),
                "modded",
                "",
                "material",
                null,
                null,
                List.of(),
                List.of(),
                null,
                null,
                "unique",
                null,
                null,
                List.of(),
                null,
                false
        );
        IslandTemplateMatch match = IslandSuggestionTemplate.firstMatchExtendedOrMisc(
                descriptor, id -> true);
        assertEquals(IslandSuggestionTemplate.CURIOSITY, match.parentTemplate());
    }

    @Test
    void formatSubsystemLabelTitleCasesAndSeparates() {
        assertEquals("Create — Mechanical Power",
                IslandTemplateMatch.formatSubsystemLabel("create:mechanical_power"));
        assertEquals("Sophisticatedstorage — Storage",
                IslandTemplateMatch.formatSubsystemLabel("sophisticatedstorage:storage"));
        // No-namespace fallback.
        assertEquals("Loose Bucket",
                IslandTemplateMatch.formatSubsystemLabel("loose_bucket"));
    }

    @Test
    void multiRoleAmbiguousFiresMultipleTemplatesAndActivityPicks() {
        // role=[material, building_block] would normally pick MATERIALS
        // (declaration order, since material comes before building_block
        // role triggers in the enum). With activity=[building], the
        // activity tie-break narrows to BUILDING.
        IslandSignalDescriptor descriptor = activityDescriptor(
                "modded:multi_role",
                List.of("material", "building_block"),
                List.of("building")
        );
        IslandSuggestionTemplate match = IslandSuggestionTemplate.firstMatch(descriptor);
        assertEquals(IslandSuggestionTemplate.BUILDING, match);
    }

    @Test
    void activityTieBreakDoesNotAffectSingleMatch() {
        // Single role match: even if activity points at another template's
        // trigger, no tie-break needed and the unique match wins.
        IslandSignalDescriptor descriptor = activityDescriptor(
                "modded:single_role",
                List.of("tool"),
                List.of("building", "redstone")
        );
        IslandSuggestionTemplate match = IslandSuggestionTemplate.firstMatch(descriptor);
        assertEquals(IslandSuggestionTemplate.TOOLS, match);
    }

    @Test
    void activityTieBreakFallsBackToDeclarationOrderWhenNoActivityHit() {
        // Two templates match, no activity intersection — fall back to
        // declaration order.
        IslandSignalDescriptor descriptor = activityDescriptor(
                "modded:ambiguous",
                List.of("material", "building_block"),
                List.of("nonsense_activity")
        );
        IslandSuggestionTemplate match = IslandSuggestionTemplate.firstMatch(descriptor);
        assertEquals(IslandSuggestionTemplate.MATERIALS, match);
    }

    @Test
    void firstMatchOrMiscReturnsMiscWhenNothingFires() {
        IslandSignalDescriptor descriptor = IslandSignalDescriptor.empty(
                ItemIdentity.of("modded:anonymous"));
        assertNotNull(IslandSuggestionTemplate.firstMatchOrMisc(descriptor));
        assertEquals(IslandSuggestionTemplate.MISC,
                IslandSuggestionTemplate.firstMatchOrMisc(descriptor));
    }

    private static IslandSignalDescriptor subsystemDescriptor(
            String itemId,
            String role,
            List<String> subsystems,
            List<String> activities
    ) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of(),
                itemId.substring(0, itemId.indexOf(':')),
                "",
                role,
                null,
                null,
                subsystems,
                activities,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                false
        );
    }

    private static IslandSignalDescriptor activityDescriptor(
            String itemId,
            List<String> roles,
            List<String> activities
    ) {
        return new IslandSignalDescriptor(
                ItemIdentity.of(itemId),
                Set.of(),
                Set.of(),
                itemId.substring(0, itemId.indexOf(':')),
                "",
                roles.isEmpty() ? null : roles.get(0),
                roles,
                null,
                List.of(),
                activities,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                false
        );
    }
}
