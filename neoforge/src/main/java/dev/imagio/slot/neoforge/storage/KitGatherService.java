package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.integration.InventoryHostFamilyHint;
import dev.imagio.slot.inventory.integration.InventoryHostObservationHints;
import dev.imagio.slot.inventory.integration.InventoryHostContext;
import dev.imagio.slot.inventory.integration.InventoryHostResolver;
import dev.imagio.slot.inventory.integration.InventorySlotOwnershipPosture;
import dev.imagio.slot.inventory.query.InventoryAuthorityReadService;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.workspace.DepositPlanner;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
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
 * Server-side "gather everything the active kit needs from nearby chests"
 * action. Both the in-screen action overlay (Gather button + atlas
 * hotkey) and the in-world hotkey route through here so the resulting
 * server state and side effects are identical.
 *
 * <p>Resolves missing identities as the union of (a) kit page slots not
 * currently carried (gap = 1) and (b) kit-scoped desired counts where
 * carried &lt; desired (gap = desired - carried). For each missing
 * identity, walks proximate claimed chests in affinity-score order and
 * pulls until the gap closes or no chest holds another copy.
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

    public static Outcome gatherActiveKit(ServerPlayer player) {
        if (player == null) {
            return Outcome.empty("no_player");
        }
        WorkflowDomainRuntime runtime = SlotPlayerWorkflowRuntimeService.runtime(player);
        if (runtime == null) {
            return Outcome.empty("no_runtime");
        }
        KitMap kitMap = runtime.snapshot().kitMap();
        KitActivation activation = kitMap.activation();
        if (!activation.isActive()) {
            return Outcome.empty("no_active_kit");
        }
        KitDefinition kit = kitMap.kit(activation.kitId());
        if (kit == null) {
            return Outcome.empty("kit_definition_missing");
        }
        ClaimedChestMap claimedChestMap = runtime.chestClaimWorkflow().claimedChestMap();
        Set<String> proximate = dev.imagio.slot.neoforge.storage.ChestProximityResolver.proximateStorageIds(player, claimedChestMap);
        if (proximate.isEmpty()) {
            return Outcome.empty("no_proximate_chest");
        }
        InventoryHostDescriptor host = resolveHost(player);
        if (host == null) {
            return Outcome.empty("no_host");
        }
        InventoryAuthoritySnapshot authority = InventoryAuthorityReadService.serverAuthority(player, host);

        // Build per-identity gap map. Active page slots want at least 1
        // of the slot's identity; kit-scoped desired counts want N. When
        // both apply to the same identity the larger gap wins.
        Map<ItemIdentity, Integer> targets = new LinkedHashMap<>();
        int activePageIndex = Math.max(0, Math.min(activation.pageIndex(), kit.pageCount() - 1));
        KitPage page = kit.page(activePageIndex);
        if (page != null) {
            for (int slotIndex = 0; slotIndex < KitPage.HOTBAR_SLOT_COUNT; slotIndex++) {
                ItemIdentity identity = page.slot(slotIndex);
                if (identity != null) {
                    targets.merge(identity, 1, Math::max);
                }
            }
        }
        Map<ItemIdentity, Integer> kitWants = runtime.desiredCountWorkflow().forKit(kit.id());
        for (Map.Entry<ItemIdentity, Integer> entry : kitWants.entrySet()) {
            ItemIdentity identity = entry.getKey();
            Integer want = entry.getValue();
            if (identity == null || want == null || want <= 0) {
                continue;
            }
            targets.merge(identity, want, Math::max);
        }
        if (targets.isEmpty()) {
            return Outcome.empty("kit_has_no_needs");
        }

        ChestAffinityMap affinityMap = runtime.snapshot().chestAffinityMap();
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
            // Affinity-ranked walk through proximate chests so the
            // chest most likely to hold this identity gets first dibs.
            java.util.List<ClaimedChest> ranked = DepositPlanner.rankProximateChestsForTake(
                    identity, claimedChestMap, affinityMap, proximate);
            int remaining = gap;
            int pulledForIdentity = 0;
            for (ClaimedChest chest : ranked) {
                if (remaining <= 0) {
                    break;
                }
                TakeAllExecutor.TakeSingleOutcome outcome = TakeAllExecutor.takeByIdentity(
                        player, chest, identity, remaining, "kit-gather");
                if (outcome.tookAnything()) {
                    remaining -= outcome.moved();
                    pulledForIdentity += outcome.moved();
                }
            }
            if (pulledForIdentity > 0) {
                identitiesPulled++;
                totalItemsPulled += pulledForIdentity;
            }
            if (remaining > 0) {
                // Still short for this identity — every proximate chest
                // we walked either had nothing matching or its insert
                // failed. Track for player feedback.
                unreachable++;
            }
        }
        SlotCommon.LOGGER.info(
                "[SLOT] gather active kit: kitId={} identitiesPulled={} totalItems={} unreachable={}",
                kit.id(), identitiesPulled, totalItemsPulled, unreachable);
        String reason = totalItemsPulled > 0
                ? "ok"
                : (unreachable > 0 ? "all_short" : "all_satisfied");
        return new Outcome(identitiesPulled, totalItemsPulled, unreachable, reason);
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
