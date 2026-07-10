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
        return seedInitialContents(chestService, storageId, contents.length, Arrays.asList(contents), tick);
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
        return seedInitialContents(chestService, storageId, snapshot.slotCount(), snapshot.contents(), tick);
    }

    public static int seedInitialContents(
            ChestClaimWorkflowDomainService chestService,
            UUID storageId,
            List<ItemStack> contents,
            long tick
    ) {
        return seedInitialContents(chestService, storageId,
                contents == null ? 0 : contents.size(), contents, tick);
    }

    public static int seedInitialContents(
            ChestClaimWorkflowDomainService chestService,
            UUID storageId,
            int slotCount,
            List<ItemStack> contents,
            long tick
    ) {
        if (chestService == null || storageId == null || contents == null || contents.isEmpty()
                || chestService.chest(storageId) == null) {
            return 0;
        }
        if (!chestService.chest(storageId).role().learnsAffinity()) {
            return 0;
        }
        if (!StorageAffinityPolicy.isEligibleSlotCount(slotCount)) {
            return 0;
        }
        return chestService.recordInitialContents(
                storageId,
                countsByIdentity(contents),
                tick,
                INITIAL_CONTENTS_ORIGIN);
    }

    private static Map<ItemIdentity, Integer> countsByIdentity(List<ItemStack> contents) {
        LinkedHashMap<ItemIdentity, Integer> counts = new LinkedHashMap<>();
        for (ItemStack stack : contents) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            counts.merge(
                    ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack)),
                    stack.getCount(),
                    Integer::sum);
        }
        return Map.copyOf(counts);
    }
}
