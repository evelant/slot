package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.workflow.domain.ChestAffinityMap;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.ClaimedChestMap;
import dev.imagio.slot.workflow.domain.KitActivation;
import dev.imagio.slot.workflow.domain.KitDefinition;
import dev.imagio.slot.workflow.domain.KitMap;
import dev.imagio.slot.workflow.domain.KitPage;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Server-side "gather desired carried items from nearby chests" action shared
 * by every loader/transport.
 *
 * <p>Resolves missing identities as the union of player-global desired counts
 * plus, when a kit is active, active kit-page slots and kit-scoped desired
 * counts. Each missing identity walks proximate claimed chests in
 * affinity-score order until the gap closes or no chest can provide another
 * matching stack.
 */
public final class KitGatherService {
    public record Outcome(
            int identitiesPulled,
            int totalItemsPulled,
            int identitiesUnreachable,
            String reason
    ) {
        public static Outcome empty(String reason) {
            return new Outcome(0, 0, 0, reason);
        }
    }

    private KitGatherService() {
    }

    public static Outcome gatherActiveKit(ServerPlayer player, WorkflowDomainRuntime runtime) {
        if (player == null) {
            return Outcome.empty("no_player");
        }
        if (runtime == null) {
            return Outcome.empty("no_runtime");
        }
        var snapshot = runtime.snapshot();
        KitMap kitMap = snapshot.kitMap();
        KitActivation activation = kitMap.activation();
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = WorkspaceChestProjectionSupport.proximateStorageIds(player, claimedChestMap);
        if (proximate.isEmpty()) {
            return Outcome.empty("no_proximate_chest");
        }
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return Outcome.empty("no_host");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);

        Map<ItemIdentity, Integer> targets = gatherTargets(runtime, kitMap, activation);
        if (targets.isEmpty()) {
            return Outcome.empty("no_desired_counts");
        }

        ChestAffinityMap affinityMap = snapshot.chestAffinityMap();
        int identitiesPulled = 0;
        int totalItemsPulled = 0;
        int unreachable = 0;
        for (Map.Entry<ItemIdentity, Integer> want : targets.entrySet()) {
            ItemIdentity identity = want.getKey();
            int target = want.getValue();
            int carried = countCarried(authority, identity);
            int gap = target - carried;
            if (gap <= 0) {
                continue;
            }
            var ranked = DepositPlanner.rankProximateChestsForTake(
                    identity,
                    claimedChestMap,
                    affinityMap,
                    proximate);
            int remaining = gap;
            int pulledForIdentity = 0;
            for (ClaimedChest chest : ranked) {
                if (remaining <= 0) {
                    break;
                }
                TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeByIdentity(
                        player,
                        chest,
                        identity,
                        remaining,
                        "gather");
                if (outcome.tookAnything()) {
                    WorkspaceChestCommandService.recordTakeRecord(
                            player, runtime, outcome.record(), "gather");
                    remaining -= outcome.moved();
                    pulledForIdentity += outcome.moved();
                }
            }
            if (pulledForIdentity > 0) {
                identitiesPulled++;
                totalItemsPulled += pulledForIdentity;
            }
            if (remaining > 0) {
                unreachable++;
            }
        }
        SlotCommon.LOGGER.info(
                "[SLOT] gather desired items: activeKit={} targets={} identitiesPulled={} totalItems={} unreachable={}",
                activation.isActive() ? activation.kitId() : "<none>",
                targets.size(),
                identitiesPulled,
                totalItemsPulled,
                unreachable);
        String reason = totalItemsPulled > 0
                ? "ok"
                : (unreachable > 0 ? "all_short" : "all_satisfied");
        return new Outcome(identitiesPulled, totalItemsPulled, unreachable, reason);
    }

    private static Map<ItemIdentity, Integer> gatherTargets(
            WorkflowDomainRuntime runtime,
            KitMap kitMap,
            KitActivation activation
    ) {
        Map<ItemIdentity, Integer> targets = new LinkedHashMap<>();
        for (Map.Entry<ItemIdentity, Integer> entry : runtime.desiredCountWorkflow().allPlayer().entrySet()) {
            mergePositiveTarget(targets, entry.getKey(), entry.getValue());
        }
        if (activation == null || !activation.isActive()) {
            return targets;
        }
        KitDefinition kit = kitMap.kit(activation.kitId());
        if (kit == null) {
            return targets;
        }
        int activePageIndex = Math.max(0, Math.min(activation.pageIndex(), kit.pageCount() - 1));
        KitPage page = kit.page(activePageIndex);
        if (page != null) {
            for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
                mergePositiveTarget(targets, page.slot(slotIndex), 1);
            }
        }
        Map<ItemIdentity, Integer> kitWants = runtime.desiredCountWorkflow().forKit(kit.id());
        for (Map.Entry<ItemIdentity, Integer> entry : kitWants.entrySet()) {
            mergePositiveTarget(targets, entry.getKey(), entry.getValue());
        }
        return targets;
    }

    private static void mergePositiveTarget(Map<ItemIdentity, Integer> targets, ItemIdentity identity, Integer count) {
        if (targets == null || identity == null || count == null || count <= 0) {
            return;
        }
        targets.merge(identity, count, Math::max);
    }

    public static WorkspaceCommandOutcome toWorkspaceOutcome(Outcome outcome) {
        if (outcome == null) {
            return WorkspaceCommandOutcome.rejected("gather_failed");
        }
        String reason = outcome.reason() == null ? "" : outcome.reason();
        if ("no_player".equals(reason) || "no_runtime".equals(reason) || "no_host".equals(reason)) {
            return WorkspaceCommandOutcome.rejected(reason);
        }
        if ("ok".equals(reason)) {
            return WorkspaceCommandOutcome.accepted(
                    "gathered items",
                    "pulled=" + outcome.totalItemsPulled()
                            + " identities=" + outcome.identitiesPulled()
                            + " unreachable=" + outcome.identitiesUnreachable());
        }
        return WorkspaceCommandOutcome.accepted(
                "nothing to gather",
                reason + " pulled=" + outcome.totalItemsPulled()
                        + " unreachable=" + outcome.identitiesUnreachable());
    }

    private static int countCarried(InventoryAuthoritySnapshot authority, ItemIdentity identity) {
        if (authority == null || identity == null) {
            return 0;
        }
        int total = 0;
        for (var source : authority.carriedSources()) {
            for (InventoryEntrySnapshot entry : authority.entries(source.id())) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                if (ItemIdentityMatcher.matchesMovable(entry.stack(), identity)) {
                    total += entry.count();
                }
            }
        }
        return total;
    }

    private static InventoryHostDescriptor resolveHost(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (menu == null) {
            return null;
        }
        return InventoryHostResolver.resolve(new InventoryHostContext(
                menu,
                player.getInventory(),
                Component.literal("SLOT Kit Gather"),
                KitGatherService.class.getName(),
                new InventoryHostObservationHints(
                        InventoryHostFamilyHint.CARRIED_ONLY,
                        InventorySlotOwnershipPosture.SLOT_OWNED,
                        true,
                        true,
                        Map.of("slotKitGather", "server")
                )
        ));
    }
}
