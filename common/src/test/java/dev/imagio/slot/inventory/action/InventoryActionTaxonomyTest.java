package dev.imagio.slot.inventory.action;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryActionTaxonomyTest {
    @Test
    void domainActionsStayVerbBasedRatherThanEncodingQuantityOrTargetType() {
        Set<String> names = Stream.of(InventoryActionKind.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertTrue(names.containsAll(Set.of(
                "TRANSFER",
                "ASSIGN",
                "SWAP",
                "CURSOR_PICKUP",
                "CURSOR_PLACE",
                "DROP_TO_WORLD",
                "TRASH",
                "SORT_SOURCE",
                "DISTRIBUTE",
                "COLLECT_MATCHING"
        )));
        assertFalse(names.contains("TRANSFER_ONE"));
        assertFalse(names.contains("TRANSFER_STACK"));
        assertFalse(names.contains("TRANSFER_ALL"));
        assertFalse(names.contains("QUICK_ACCESS_ASSIGN"));
        assertFalse(names.contains("EQUIP"));
        assertFalse(names.contains("UNEQUIP"));
        assertFalse(names.contains("PLACE"));
        assertFalse(names.contains("DROP"));
    }

    @Test
    void quantityAndConflictDimensionsRepresentCommonGestureVariants() {
        Set<String> quantities = Stream.of(InventoryActionQuantity.values())
                .map(Enum::name)
                .collect(Collectors.toSet());
        Set<String> conflictPolicies = Stream.of(InventoryActionConflictPolicy.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertTrue(quantities.containsAll(Set.of(
                "ONE",
                "STACK",
                "ALL_MATCHING",
                "HALF_SOURCE",
                "HALF_CURSOR",
                "EVEN_SPLIT",
                "SINGLE_PER_TARGET"
        )));
        assertTrue(conflictPolicies.containsAll(Set.of(
                "INSERT_ONLY",
                "ASSIGN_WITH_DISPLACE",
                "SWAP_EXACT",
                "REJECT_IF_OCCUPIED"
        )));
    }
}
