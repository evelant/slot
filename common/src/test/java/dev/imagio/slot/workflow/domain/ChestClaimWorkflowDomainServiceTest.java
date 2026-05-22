package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChestClaimWorkflowDomainServiceTest {
    private static final UUID CHEST_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CHEST_B = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final ItemIdentity REDSTONE = ItemIdentity.of("minecraft:redstone");

    @Test
    void nonStorageRoleClearsAndStopsLearningAffinity() {
        ChestClaimWorkflowDomainService service = service();
        service.claimWithId(CHEST_A, Set.of(anchor(0)), 0, 0, "");
        service.recordDeposit(CHEST_A, REDSTONE, 16, 100L);

        service.setRole(CHEST_A, ChestRole.BUFFER);
        service.recordDeposit(CHEST_A, REDSTONE, 16, 200L);

        assertEquals(ChestRole.BUFFER, service.chest(CHEST_A).role());
        assertEquals(0, service.chestAffinityMap().score(CHEST_A, REDSTONE));
    }

    @Test
    void rehomingItemToAnotherStorageChestForgetsEmptiedOriginAffinity() {
        ChestClaimWorkflowDomainService service = service();
        service.claimWithId(CHEST_A, Set.of(anchor(0)), 0, 0, "");
        service.claimWithId(CHEST_B, Set.of(anchor(1)), 0, 0, "");
        service.recordDeposit(CHEST_A, REDSTONE, 16, 100L);

        service.recordPossibleRehomeTake(CHEST_A, Map.of(REDSTONE, 16), Set.of(), 150L);
        service.recordDeposit(CHEST_B, REDSTONE, 16, 200L);

        assertEquals(0, service.chestAffinityMap().score(CHEST_A, REDSTONE));
        assertEquals(1, service.chestAffinityMap().score(CHEST_B, REDSTONE));
    }

    @Test
    void rehomeCandidateIsCanceledWhenOriginStillContainsIdentity() {
        ChestClaimWorkflowDomainService service = service();
        service.claimWithId(CHEST_A, Set.of(anchor(0)), 0, 0, "");
        service.claimWithId(CHEST_B, Set.of(anchor(1)), 0, 0, "");
        service.recordDeposit(CHEST_A, REDSTONE, 16, 100L);

        service.recordPossibleRehomeTake(CHEST_A, Map.of(REDSTONE, 16), Set.of(REDSTONE), 150L);
        service.recordDeposit(CHEST_B, REDSTONE, 16, 200L);

        assertEquals(1, service.chestAffinityMap().score(CHEST_A, REDSTONE));
        assertEquals(1, service.chestAffinityMap().score(CHEST_B, REDSTONE));
    }

    private static ChestClaimWorkflowDomainService service() {
        return new ChestClaimWorkflowDomainService(new InMemoryWorkflowDomainStateRepository());
    }

    private static ChestAnchor anchor(int x) {
        return new ChestAnchor("minecraft:overworld", x, 64, 0);
    }
}
