package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChestContentAffinitySeederTest {
    @Test
    void seedsOneAffinityPerExistingIdentityWhenChestIsClaimed() {
        ChestClaimWorkflowDomainService chestService = service();
        UUID storageId = UUID.randomUUID();
        chestService.claimWithId(storageId, Set.of(anchor()), 0, 0, "");
        ItemIdentity coal = ItemIdentityMatcher.create(new ItemStack("minecraft:coal", 1, 64));
        ItemIdentity stone = ItemIdentityMatcher.create(new ItemStack("minecraft:stone", 1, 64));

        int seeded = ChestContentAffinitySeeder.seedInitialContents(
                chestService,
                storageId,
                new ItemStack[]{
                        new ItemStack("minecraft:coal", 8, 64),
                        new ItemStack("minecraft:coal", 4, 64),
                        new ItemStack("minecraft:stone", 2, 64),
                        ItemStack.EMPTY
                },
                200L);

        assertEquals(2, seeded);
        assertEquals(1, chestService.chestAffinityMap().score(storageId, coal));
        assertEquals(1, chestService.chestAffinityMap().score(storageId, stone));
    }

    @Test
    void doesNotReSeedIdentitiesThatAlreadyHaveAffinity() {
        ChestClaimWorkflowDomainService chestService = service();
        UUID storageId = UUID.randomUUID();
        chestService.claimWithId(storageId, Set.of(anchor()), 0, 0, "");
        ItemIdentity coal = ItemIdentityMatcher.create(new ItemStack("minecraft:coal", 1, 64));
        ChestContentAffinitySeeder.seedInitialContents(
                chestService,
                storageId,
                new ItemStack[]{new ItemStack("minecraft:coal", 8, 64)},
                200L);

        int seeded = ChestContentAffinitySeeder.seedInitialContents(
                chestService,
                storageId,
                new ItemStack[]{new ItemStack("minecraft:coal", 32, 64)},
                400L);

        assertEquals(0, seeded);
        assertEquals(1, chestService.chestAffinityMap().score(storageId, coal));
    }

    private static ChestClaimWorkflowDomainService service() {
        return new ChestClaimWorkflowDomainService(new InMemoryWorkflowDomainStateRepository());
    }

    private static ChestAnchor anchor() {
        return new ChestAnchor("minecraft:overworld", 1, 64, 1);
    }
}
