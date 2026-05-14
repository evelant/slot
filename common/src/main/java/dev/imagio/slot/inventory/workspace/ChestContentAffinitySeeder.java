package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.workflow.domain.ChestClaimWorkflowDomainService;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converts already-present claimed storage contents into initial affinity.
 *
 * <p>Claiming a stocked chest is a strong routing signal: if the player claims
 * it while it already contains coal, deposit should remember coal belongs there
 * even after the last coal stack is later removed.
 */
public final class ChestContentAffinitySeeder {
    private static final DomainEventMetadata INITIAL_CONTENTS_ORIGIN =
            DomainEventMetadata.origin("workflow.storage.chest.initial_contents");

    private ChestContentAffinitySeeder() {
    }

    public static int seedInitialContents(
            ChestClaimWorkflowDomainService chestService,
            UUID storageId,
            ItemStack[] contents,
            long tick
    ) {
        if (contents == null || contents.length == 0) {
            return 0;
        }
        return seedInitialContents(chestService, storageId, Arrays.asList(contents), tick);
    }

    public static int seedInitialContents(
            ChestClaimWorkflowDomainService chestService,
            UUID storageId,
            SlotWorkspaceViewModel.ChestContentsSnapshot snapshot,
            long tick
    ) {
        if (snapshot == null) {
            return 0;
        }
        return seedInitialContents(chestService, storageId, snapshot.contents(), tick);
    }

    public static int seedInitialContents(
            ChestClaimWorkflowDomainService chestService,
            UUID storageId,
            List<ItemStack> contents,
            long tick
    ) {
        if (chestService == null || storageId == null || contents == null || contents.isEmpty()
                || chestService.chest(storageId) == null) {
            return 0;
        }
        Map<ItemIdentity, Integer> counts = countsByIdentity(contents);
        int seeded = 0;
        for (Map.Entry<ItemIdentity, Integer> entry : counts.entrySet()) {
            ItemIdentity identity = entry.getKey();
            if (identity == null || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            if (chestService.chestAffinityMap().score(storageId, identity) > 0) {
                continue;
            }
            chestService.recordDeposit(
                    storageId,
                    identity,
                    entry.getValue(),
                    tick,
                    INITIAL_CONTENTS_ORIGIN);
            seeded++;
        }
        return seeded;
    }

    private static Map<ItemIdentity, Integer> countsByIdentity(List<ItemStack> contents) {
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        for (ItemStack stack : contents) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            counts.merge(ItemIdentityMatcher.create(stack), stack.getCount(), Integer::sum);
        }
        return Map.copyOf(counts);
    }
}
