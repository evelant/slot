package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.workflow.domain.ChestAnchor;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.InMemoryWorkflowDomainStateRepository;
import dev.imagio.slot.workflow.domain.WorkflowEvent;
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
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY
                },
                200L);

        assertEquals(2, seeded);
        assertEquals(1, chestService.chestAffinityMap().score(storageId, coal));
        assertEquals(1, chestService.chestAffinityMap().score(storageId, stone));
    }

    @Test
    void seedsMultipleIdentitiesWithOneWorkflowEvent() {
        InMemoryWorkflowDomainStateRepository repository = new InMemoryWorkflowDomainStateRepository();
        ChestClaimWorkflowDomainService chestService = new ChestClaimWorkflowDomainService(repository);
        UUID storageId = UUID.randomUUID();
        chestService.claimWithId(storageId, Set.of(anchor()), 0, 0, "");

        int seeded = ChestContentAffinitySeeder.seedInitialContents(
                chestService,
                storageId,
                new ItemStack[]{
                        new ItemStack("minecraft:coal", 8, 64),
                        new ItemStack("minecraft:stone", 2, 64),
                        new ItemStack("minecraft:dirt", 3, 64),
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY
                },
                200L);

        long seedEvents = repository.workflowEvents().records().stream()
                .filter(record -> record.event() instanceof WorkflowEvent.ChestInitialContentsSeeded)
                .count();
        assertEquals(3, seeded);
        assertEquals(1, seedEvents);
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
                new ItemStack[]{
                        new ItemStack("minecraft:coal", 8, 64),
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY
                },
                200L);

        int seeded = ChestContentAffinitySeeder.seedInitialContents(
                chestService,
                storageId,
                new ItemStack[]{
                        new ItemStack("minecraft:coal", 32, 64),
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY
                },
                400L);

        assertEquals(0, seeded);
        assertEquals(1, chestService.chestAffinityMap().score(storageId, coal));
    }

    @Test
    void ignoresSmallStationInventories() {
        ChestClaimWorkflowDomainService chestService = service();
        UUID storageId = UUID.randomUUID();
        chestService.claimWithId(storageId, Set.of(anchor()), 0, 0, "");
        ItemIdentity hotPart = ItemIdentityMatcher.create(new ItemStack("tfc:hot_metal_part", 1, 64));

        int seeded = ChestContentAffinitySeeder.seedInitialContents(
                chestService,
                storageId,
                new ItemStack[]{new ItemStack("tfc:hot_metal_part", 1, 64)},
                200L);

        assertEquals(0, seeded);
        assertEquals(0, chestService.chestAffinityMap().score(storageId, hotPart));
    }

    private static ChestClaimWorkflowDomainService service() {
        return new ChestClaimWorkflowDomainService(new InMemoryWorkflowDomainStateRepository());
    }

    private static ChestAnchor anchor() {
        return new ChestAnchor("minecraft:overworld", 1, 64, 1);
    }
}
