package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.action.InventoryActionKind;
import dev.imagio.slot.inventory.action.InventoryActionConflictPolicy;
import dev.imagio.slot.inventory.action.InventoryActionMode;
import dev.imagio.slot.inventory.action.InventoryActionOutcome;
import dev.imagio.slot.inventory.action.InventoryActionQuantity;
import dev.imagio.slot.inventory.action.InventoryActionRequest;
import dev.imagio.slot.inventory.action.InventoryActionScope;
import dev.imagio.slot.inventory.action.InventoryActionTarget;
import dev.imagio.slot.inventory.action.InventoryTargetCanonicalizer;
import dev.imagio.slot.inventory.core.InventoryActionPolicy;
import dev.imagio.slot.inventory.core.InventoryCapability;
import dev.imagio.slot.inventory.core.InventoryHostDescriptor;
import dev.imagio.slot.inventory.core.InventorySourceDescriptor;
import dev.imagio.slot.inventory.core.InventoryToolActionId;
import dev.imagio.slot.inventory.core.InventoryToolToggleId;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class LoadoutApplyService {
    private LoadoutApplyService() {
    }

    public static LoadoutApplyPlan plan(
            QuickAccessLoadoutDefinition loadout,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy
    ) {
        return plan(
                loadout,
                authority,
                protectionPolicy,
                InventoryActionMode.EXECUTE,
                entry -> ItemIdentityMatcher.create(entry.stack())
        );
    }

    public static LoadoutApplyPlan plan(
            QuickAccessLoadoutDefinition loadout,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            InventoryActionMode mode,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        return plan(loadout, Set.of(), authority, protectionPolicy, mode, identityResolver);
    }

    public static LoadoutApplyPlan plan(
            QuickAccessLoadoutDefinition loadout,
            Set<LoadoutTarget> clearTargets,
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            InventoryActionMode mode,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        if (loadout == null
                || authority == null
                || authority.host() == null
                || identityResolver == null) {
            return LoadoutApplyPlan.empty(loadout == null ? "" : loadout.id());
        }

        InventoryHostDescriptor host = authority.host();
        ProtectionPolicy resolvedProtection = protectionPolicy == null ? ProtectionPolicy.allowAll() : protectionPolicy;
        InventoryActionMode resolvedMode = mode == null ? InventoryActionMode.EXECUTE : mode;
        Map<String, TargetOccupant> currentTargets = new LinkedHashMap<>(currentTargetOccupants(authority, identityResolver));
        Set<String> reservedSourceSlots = new LinkedHashSet<>();
        // Items moved by an earlier stage step still physically exist — their new home is
        // the staging slot. Track them here so a later entry that happens to want the same
        // identity can fetch it from the staging slot (bypassing the reservedSourceSlots
        // exclusion that keeps authority-based candidate search from double-consuming).
        List<CandidateSource> stagedCandidates = new ArrayList<>();
        List<PlannedTargetOperation> operations = new ArrayList<>();
        List<LoadoutTarget> satisfiedTargets = new ArrayList<>();
        List<LoadoutTarget> missingTargets = new ArrayList<>();
        List<String> diagnostics = new ArrayList<>();

        List<QuickAccessLoadoutEntry> orderedEntries = loadout.entries().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(entry -> InventoryTargetCanonicalizer.canonicalKey(entry.target())))
                .toList();
        for (QuickAccessLoadoutEntry entry : orderedEntries) {
            if (entry.identity() == null || entry.target() == null) {
                continue;
            }

            InventoryActionTarget target = toActionTarget(entry.target());
            InventoryActionKind targetKind = actionKindFor(entry.target());
            String targetKey = InventoryTargetCanonicalizer.canonicalKey(entry.target());
            TargetOccupant currentOccupant = currentTargets.get(targetKey);

            if (ItemIdentityMatcher.matchesMovable(currentOccupant == null ? null : currentOccupant.identity(), entry.identity())) {
                satisfiedTargets.add(entry.target());
                continue;
            }

            if (!InventoryActionPolicy.allows(host, targetKind, target, resolvedProtection)) {
                missingTargets.add(entry.target());
                diagnostics.add("target_blocked_by_policy:" + targetKey);
                continue;
            }

            CandidateSource candidate = findStagedCandidate(stagedCandidates, entry.identity());
            if (candidate != null) {
                stagedCandidates.remove(candidate);
                // If a prior stage put the identity at the exact target slot (can happen when
                // findStagingTarget had to use a quick-access slot because main was full),
                // the target is already satisfied — emitting a self-transfer would be a no-op
                // at best and fail-closed at worst. Skip the apply step entirely.
                if (stagedCandidateIsAtTarget(host, candidate, entry.target())) {
                    satisfiedTargets.add(entry.target());
                    currentTargets.put(targetKey, new TargetOccupant(entry.identity(), candidate.stack()));
                    continue;
                }
            } else {
                candidate = findCandidateSource(
                        authority,
                        identityResolver,
                        entry.identity(),
                        targetKind,
                        resolvedProtection,
                        reservedSourceSlots
                );
            }
            if (candidate == null) {
                missingTargets.add(entry.target());
                diagnostics.add("no_candidate_source_for_target:" + targetKey);
                continue;
            }

            ArrayList<InventoryActionRequest> requests = new ArrayList<>();
            InventoryActionRequest rollbackRequest = null;

            if (currentOccupant != null && currentOccupant.present()) {
                if (InventoryActionPolicy.blockedByProtection(
                        targetKind,
                        currentOccupant.identity(),
                        currentOccupant.stack(),
                        resolvedProtection
                )) {
                    missingTargets.add(entry.target());
                    diagnostics.add("target_occupant_blocked_by_protection:" + targetKey);
                    continue;
                }

                StagingTarget stagingTarget = findStagingTarget(
                        authority,
                        resolvedProtection,
                        reservedSourceSlots
                );
                if (stagingTarget == null) {
                    missingTargets.add(entry.target());
                    diagnostics.add("no_staging_slot_for_target:" + targetKey);
                    continue;
                }

                requests.add(new InventoryActionRequest(
                        host.hostId(),
                        host.serverMenuRef(),
                        UUID.randomUUID().toString(),
                        InventoryActionKind.TRANSFER,
                        resolvedMode,
                        InventoryActionQuantity.STACK,
                        InventoryActionScope.SINGLE_TARGET,
                        InventoryActionConflictPolicy.INSERT_ONLY,
                        "workflow:loadout_stage",
                        target,
                        new InventoryActionTarget.SourceSlotTarget(stagingTarget.sourceId(), stagingTarget.slotIndex()),
                        0,
                        currentOccupant.identity(),
                        currentOccupant.stack(),
                        InventoryToolActionId.PROVIDER_DEFINED,
                        InventoryToolToggleId.PROVIDER_DEFINED,
                        false,
                        ""
                ));
                // Rollback uses TRANSFER + INSERT_ONLY (not ASSIGN) so it works for any
                // staging source — ASSIGN's in-place swap requires PLAYER-bound ends, and
                // backpack staging makes the staging slot PROVIDER-bound. The staged
                // slot will be empty by the time rollback runs (we just moved something
                // out of it), so INSERT_ONLY succeeds without needing displacement.
                rollbackRequest = new InventoryActionRequest(
                        host.hostId(),
                        host.serverMenuRef(),
                        UUID.randomUUID().toString(),
                        InventoryActionKind.TRANSFER,
                        resolvedMode,
                        InventoryActionQuantity.STACK,
                        InventoryActionScope.SINGLE_TARGET,
                        InventoryActionConflictPolicy.INSERT_ONLY,
                        "workflow:loadout_stage_rollback",
                        new InventoryActionTarget.SourceSlotTarget(stagingTarget.sourceId(), stagingTarget.slotIndex()),
                        target,
                        0,
                        currentOccupant.identity(),
                        currentOccupant.stack(),
                        InventoryToolActionId.PROVIDER_DEFINED,
                        InventoryToolToggleId.PROVIDER_DEFINED,
                        false,
                        ""
                );
                reservedSourceSlots.add(stagingTarget.stableKey());
                stagedCandidates.add(new CandidateSource(
                        stagingTarget.sourceId(),
                        stagingTarget.slotIndex(),
                        "",
                        currentOccupant.stack(),
                        currentOccupant.identity()
                ));
                currentTargets.remove(targetKey);
            }

            reservedSourceSlots.add(candidate.stableKey());
            // ASSIGN's in-place swap path requires both ends be player-bound (main/hotbar/
            // equipment). If the candidate lives in a non-player carried source like a
            // Sophisticated Backpack, use TRANSFER + INSERT_ONLY (stage above guarantees
            // the target is empty when we reach this point) so the extract/insert path
            // runs through the source's capability handler.
            boolean candidateIsPlayerBound = isPlayerBoundSource(host, candidate.sourceId());
            InventoryActionKind applyKind = candidateIsPlayerBound ? targetKind : InventoryActionKind.TRANSFER;
            InventoryActionConflictPolicy applyPolicy = candidateIsPlayerBound
                    ? conflictPolicyFor(targetKind)
                    : InventoryActionConflictPolicy.INSERT_ONLY;
            InventoryActionQuantity applyQuantity = candidateIsPlayerBound
                    ? quantityFor(targetKind)
                    : InventoryActionQuantity.STACK;
            requests.add(new InventoryActionRequest(
                    host.hostId(),
                    host.serverMenuRef(),
                    UUID.randomUUID().toString(),
                    applyKind,
                    resolvedMode,
                    applyQuantity,
                    InventoryActionScope.LOADOUT,
                    applyPolicy,
                    "workflow:loadout_apply",
                    candidate.actionTarget(),
                    target,
                    0,
                    entry.identity(),
                    candidate.stack(),
                    InventoryToolActionId.PROVIDER_DEFINED,
                    InventoryToolToggleId.PROVIDER_DEFINED,
                    false,
                    ""
            ));
            currentTargets.put(targetKey, new TargetOccupant(entry.identity(), candidate.stack()));
            // The candidate slot is now empty. If it's a tracked quick-access / equipment
            // lane, drop its stale currentTargets entry so a later entry doesn't try to
            // stage from it. Without this, page swaps that reorder two belt items produce
            // stale stage requests and fail with player_slot_identity_mismatch.
            String candidateTargetKey = targetKeyForSourceSlot(host, candidate.sourceId(), candidate.slotIndex());
            if (candidateTargetKey != null) {
                currentTargets.remove(candidateTargetKey);
            }
            // Reserve the target's source slot so later findCandidateSource calls do not
            // pick it up as a source (the target now holds the identity we just assigned,
            // not the identity authority's pre-plan snapshot claims is there).
            String targetSourceKey = sourceKeyForTarget(host, target);
            if (targetSourceKey != null) {
                reservedSourceSlots.add(targetSourceKey);
            }
            operations.add(new PlannedTargetOperation(entry.target(), requests, rollbackRequest));
        }

        // Pass 2: targets the loadout explicitly asks to be empty (kit page slots with a
        // null identity). Skip any target already satisfied / missing / planned above.
        // Semantics: if the target has an occupant, stage it out to a free carried slot;
        // if it's already empty, mark satisfied and do nothing.
        Set<String> plannedKeys = new LinkedHashSet<>();
        for (LoadoutTarget t : satisfiedTargets) {
            plannedKeys.add(InventoryTargetCanonicalizer.canonicalKey(t));
        }
        for (LoadoutTarget t : missingTargets) {
            plannedKeys.add(InventoryTargetCanonicalizer.canonicalKey(t));
        }
        for (PlannedTargetOperation op : operations) {
            if (op != null && op.target() != null) {
                plannedKeys.add(InventoryTargetCanonicalizer.canonicalKey(op.target()));
            }
        }
        if (clearTargets != null) {
            List<LoadoutTarget> orderedClearTargets = clearTargets.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(InventoryTargetCanonicalizer::canonicalKey))
                    .toList();
            for (LoadoutTarget clearTarget : orderedClearTargets) {
                String clearKey = InventoryTargetCanonicalizer.canonicalKey(clearTarget);
                if (plannedKeys.contains(clearKey)) {
                    continue;
                }
                TargetOccupant occupant = currentTargets.get(clearKey);
                if (occupant == null || !occupant.present()) {
                    satisfiedTargets.add(clearTarget);
                    continue;
                }
                InventoryActionTarget actionTarget = toActionTarget(clearTarget);
                InventoryActionKind targetKind = actionKindFor(clearTarget);
                if (InventoryActionPolicy.blockedByProtection(
                        targetKind,
                        occupant.identity(),
                        occupant.stack(),
                        resolvedProtection
                )) {
                    missingTargets.add(clearTarget);
                    diagnostics.add("clear_blocked_by_protection:" + clearKey);
                    continue;
                }
                StagingTarget stagingTarget = findStagingTarget(
                        authority,
                        resolvedProtection,
                        reservedSourceSlots
                );
                if (stagingTarget == null) {
                    missingTargets.add(clearTarget);
                    diagnostics.add("no_staging_slot_for_clear:" + clearKey);
                    continue;
                }
                InventoryActionRequest clearRequest = new InventoryActionRequest(
                        host.hostId(),
                        host.serverMenuRef(),
                        UUID.randomUUID().toString(),
                        InventoryActionKind.TRANSFER,
                        resolvedMode,
                        InventoryActionQuantity.STACK,
                        InventoryActionScope.SINGLE_TARGET,
                        InventoryActionConflictPolicy.INSERT_ONLY,
                        "workflow:loadout_clear",
                        actionTarget,
                        new InventoryActionTarget.SourceSlotTarget(stagingTarget.sourceId(), stagingTarget.slotIndex()),
                        0,
                        occupant.identity(),
                        occupant.stack(),
                        InventoryToolActionId.PROVIDER_DEFINED,
                        InventoryToolToggleId.PROVIDER_DEFINED,
                        false,
                        ""
                );
                reservedSourceSlots.add(stagingTarget.stableKey());
                stagedCandidates.add(new CandidateSource(
                        stagingTarget.sourceId(),
                        stagingTarget.slotIndex(),
                        "",
                        occupant.stack(),
                        occupant.identity()
                ));
                currentTargets.remove(clearKey);
                satisfiedTargets.add(clearTarget);
                operations.add(new PlannedTargetOperation(
                        clearTarget,
                        List.of(clearRequest),
                        null
                ));
            }
        }

        return new LoadoutApplyPlan(
                loadout.id(),
                List.copyOf(operations),
                List.copyOf(satisfiedTargets),
                List.copyOf(missingTargets),
                List.copyOf(new LinkedHashSet<>(diagnostics))
        );
    }

    public static LoadoutApplyResult execute(
            LoadoutApplyPlan plan,
            Function<InventoryActionRequest, InventoryActionOutcome> actionExecutor
    ) {
        if (plan == null) {
            return LoadoutApplyResult.empty("");
        }
        if (actionExecutor == null) {
            LinkedHashSet<String> diagnostics = new LinkedHashSet<>(plan.diagnostics());
            diagnostics.add("missing_action_executor");
            return new LoadoutApplyResult(
                    plan.loadoutId(),
                    requestedTargets(plan),
                    plan.satisfiedTargets(),
                    plan.missingTargets(),
                    Map.of(),
                    List.copyOf(diagnostics)
            );
        }

        LinkedHashMap<LoadoutTarget, InventoryActionOutcome> outcomesByTarget = new LinkedHashMap<>();
        ArrayList<LoadoutTarget> satisfiedTargets = new ArrayList<>(plan.satisfiedTargets());
        LinkedHashSet<String> diagnostics = new LinkedHashSet<>(plan.diagnostics());

        for (PlannedTargetOperation operation : plan.operations()) {
            if (operation == null || operation.target() == null || operation.requests().isEmpty()) {
                continue;
            }

            boolean stageCompleted = false;
            InventoryActionOutcome terminalOutcome = null;
            for (int index = 0; index < operation.requests().size(); index++) {
                InventoryActionRequest request = operation.requests().get(index);
                if (request == null) {
                    continue;
                }

                InventoryActionOutcome outcome = actionExecutor.apply(request);
                terminalOutcome = outcome;
                if (outcome == null || !outcome.successful()) {
                    if (outcome != null && !outcome.diagnostics().isBlank()) {
                        diagnostics.add(outcome.diagnostics());
                    }
                    if (stageCompleted && operation.rollbackRequest() != null) {
                        InventoryActionOutcome rollbackOutcome = actionExecutor.apply(operation.rollbackRequest());
                        if (rollbackOutcome == null || !rollbackOutcome.successful()) {
                            diagnostics.add("loadout_stage_rollback_failed:" + InventoryTargetCanonicalizer.canonicalKey(operation.target()));
                            if (rollbackOutcome != null && !rollbackOutcome.diagnostics().isBlank()) {
                                diagnostics.add(rollbackOutcome.diagnostics());
                            }
                        }
                    }
                    break;
                }

                stageCompleted = operation.requests().size() > 1 && index < operation.requests().size() - 1;
            }

            if (terminalOutcome != null) {
                outcomesByTarget.put(operation.target(), terminalOutcome);
                if (terminalOutcome.successful()) {
                    satisfiedTargets.add(operation.target());
                }
            }
        }

        return new LoadoutApplyResult(
                plan.loadoutId(),
                requestedTargets(plan),
                List.copyOf(new LinkedHashSet<>(satisfiedTargets)),
                plan.missingTargets(),
                outcomesByTarget,
                List.copyOf(diagnostics)
        );
    }

    private static Map<String, TargetOccupant> currentTargetOccupants(
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver
    ) {
        LinkedHashMap<String, TargetOccupant> occupants = new LinkedHashMap<>();
        InventoryHostDescriptor host = authority.host();
        host.quickAccessLanes().forEach(lane -> {
            if (lane == null) {
                return;
            }
            InventorySourceSnapshot source = authority.sourceSnapshot(lane.sourceId());
            if (source == null) {
                return;
            }
            for (InventoryEntrySnapshot snapshot : source.entries()) {
                if (snapshot == null || !snapshot.slotBacked() || snapshot.stack() == null || snapshot.stack().isEmpty()) {
                    continue;
                }
                ItemIdentity identity = identityResolver.apply(snapshot);
                if (identity != null) {
                    occupants.put(
                            InventoryTargetCanonicalizer.canonicalKey(new LoadoutTarget.QuickAccessLaneTarget(lane.id(), snapshot.slotIndex())),
                            new TargetOccupant(identity, snapshot.stack())
                    );
                }
            }
        });
        host.equipmentGroups().forEach(group -> {
            if (group == null) {
                return;
            }
            InventorySourceSnapshot source = authority.sourceSnapshot(group.sourceId());
            if (source == null) {
                return;
            }
            for (InventoryEntrySnapshot snapshot : source.entries()) {
                if (snapshot == null || !snapshot.slotBacked() || snapshot.stack() == null || snapshot.stack().isEmpty()) {
                    continue;
                }
                ItemIdentity identity = identityResolver.apply(snapshot);
                if (identity != null) {
                    occupants.put(
                            InventoryTargetCanonicalizer.canonicalKey(new LoadoutTarget.EquipmentSlotTarget(group.id(), snapshot.slotIndex())),
                            new TargetOccupant(identity, snapshot.stack())
                    );
                }
            }
        });
        return Map.copyOf(occupants);
    }

    private static CandidateSource findCandidateSource(
            InventoryAuthoritySnapshot authority,
            Function<InventoryEntrySnapshot, ItemIdentity> identityResolver,
            ItemIdentity identity,
            InventoryActionKind targetKind,
            ProtectionPolicy protectionPolicy,
            Set<String> reservedSourceSlots
    ) {
        for (InventorySourceDescriptor source : authority.carriedSources().stream()
                .filter(candidate -> candidate != null && candidate.supports(InventoryCapability.EXTRACT))
                .sorted(Comparator.comparingInt(InventorySourceDescriptor::stableOrder))
                .toList()) {
            InventorySourceSnapshot sourceSnapshot = authority.sourceSnapshot(source.id());
            if (sourceSnapshot == null) {
                continue;
            }
            for (InventoryEntrySnapshot snapshot : sourceSnapshot.entries()) {
                if (snapshot == null || snapshot.stack() == null || snapshot.stack().isEmpty()) {
                    continue;
                }
                ItemIdentity candidateIdentity = identityResolver.apply(snapshot);
                if (!ItemIdentityMatcher.matchesMovable(candidateIdentity, identity)) {
                    continue;
                }
                InventoryActionTarget protectionTarget = protectionTargetForEntry(authority.host(), source.id(), snapshot);
                if (InventoryActionPolicy.blockedByProtection(
                        targetKind,
                        protectionTarget,
                        candidateIdentity,
                        snapshot.stack(),
                        protectionPolicy
                )) {
                    continue;
                }
                CandidateSource candidate = new CandidateSource(
                        source.id(),
                        snapshot.slotBacked() ? snapshot.slotIndex() : -1,
                        snapshot.slotBacked() ? "" : snapshot.entryId(),
                        snapshot.stack(),
                        candidateIdentity
                );
                if (!reservedSourceSlots.contains(candidate.stableKey())) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static StagingTarget findStagingTarget(
            InventoryAuthoritySnapshot authority,
            ProtectionPolicy protectionPolicy,
            Set<String> reservedSourceSlots
    ) {
        // Staging = "park displaced target occupant somewhere carried." We exclude
        // quick-access + equipment sources because they are the loadout's own target
        // surfaces (parking into one would collide with a later entry and produce a
        // self-transfer). Otherwise we follow the general carried stableOrder — which
        // per BuiltinInventoryDescriptors is backpack → main → hotbar. The "backpack
        // first" order matches the design rule that overflow lands in backpacks so
        // the main inventory's layout stays intact across kit switches.
        InventoryHostDescriptor host = authority.host();
        return findStagingTarget(
                authority,
                reservedSourceSlots,
                protectionPolicy,
                source -> source != null
                        && source.supports(InventoryCapability.INSERT)
                        && !isQuickAccessOrEquipmentSource(host, source.id())
        );
    }

    private static StagingTarget findStagingTarget(
            InventoryAuthoritySnapshot authority,
            Set<String> reservedSourceSlots,
            ProtectionPolicy protectionPolicy,
            java.util.function.Predicate<InventorySourceDescriptor> sourceFilter
    ) {
        for (InventorySourceDescriptor source : authority.carriedSources().stream()
                .filter(sourceFilter)
                .sorted(Comparator.comparingInt(InventorySourceDescriptor::stableOrder))
                .toList()) {
            InventorySourceSnapshot sourceSnapshot = authority.sourceSnapshot(source.id());
            if (sourceSnapshot == null) {
                continue;
            }
            Map<Integer, InventoryEntrySnapshot> occupied = new LinkedHashMap<>();
            for (InventoryEntrySnapshot snapshot : sourceSnapshot.entries()) {
                if (snapshot != null && snapshot.slotBacked()) {
                    occupied.put(snapshot.slotIndex(), snapshot);
                }
            }

            int slotCapacity = Math.max(0, sourceSnapshot.slotCapacity());
            for (int slotIndex = 0; slotIndex < slotCapacity; slotIndex++) {
                if (occupied.containsKey(slotIndex)) {
                    continue;
                }
                StagingTarget target = new StagingTarget(source.id(), slotIndex);
                if (reservedSourceSlots.contains(target.stableKey())) {
                    continue;
                }
                if (InventoryActionPolicy.blockedByProtection(
                        InventoryActionKind.TRANSFER,
                        protectionTargetForSource(authority.host(), source.id(), slotIndex),
                        null,
                        ItemStack.EMPTY,
                        protectionPolicy
                )) {
                    continue;
                }
                return target;
            }
        }
        return null;
    }

    private static CandidateSource findStagedCandidate(List<CandidateSource> staged, ItemIdentity identity) {
        if (identity == null || staged == null || staged.isEmpty()) {
            return null;
        }
        for (CandidateSource candidate : staged) {
            if (ItemIdentityMatcher.matchesMovable(candidate.identity(), identity)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean stagedCandidateIsAtTarget(
            InventoryHostDescriptor host,
            CandidateSource candidate,
            LoadoutTarget target
    ) {
        if (host == null || candidate == null || target == null) {
            return false;
        }
        return switch (target) {
            case LoadoutTarget.QuickAccessLaneTarget quickAccessLaneTarget -> {
                var lane = host.quickAccessLane(quickAccessLaneTarget.laneId());
                yield lane != null
                        && candidate.sourceId() != null
                        && candidate.sourceId().equals(lane.sourceId())
                        && candidate.slotIndex() == quickAccessLaneTarget.slotIndex();
            }
            case LoadoutTarget.EquipmentSlotTarget equipmentSlotTarget -> {
                var group = host.equipmentGroup(equipmentSlotTarget.groupId());
                yield group != null
                        && candidate.sourceId() != null
                        && candidate.sourceId().equals(group.sourceId())
                        && candidate.slotIndex() == equipmentSlotTarget.slotIndex();
            }
        };
    }

    private static boolean isQuickAccessOrEquipmentSource(InventoryHostDescriptor host, String sourceId) {
        if (host == null || sourceId == null || sourceId.isBlank()) {
            return false;
        }
        for (var lane : host.quickAccessLanes()) {
            if (lane != null && sourceId.equals(lane.sourceId())) {
                return true;
            }
        }
        for (var group : host.equipmentGroups()) {
            if (group != null && sourceId.equals(group.sourceId())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPlayerBoundSource(InventoryHostDescriptor host, String sourceId) {
        if (host == null || sourceId == null || sourceId.isBlank()) {
            return false;
        }
        InventorySourceDescriptor descriptor = host.source(sourceId);
        return descriptor != null && descriptor.playerBacked();
    }

    private static String targetKeyForSourceSlot(InventoryHostDescriptor host, String sourceId, int slotIndex) {
        if (host == null || sourceId == null || sourceId.isBlank() || slotIndex < 0) {
            return null;
        }
        for (var lane : host.quickAccessLanes()) {
            if (lane != null && sourceId.equals(lane.sourceId())) {
                return InventoryTargetCanonicalizer.canonicalKey(
                        new LoadoutTarget.QuickAccessLaneTarget(lane.id(), slotIndex));
            }
        }
        for (var group : host.equipmentGroups()) {
            if (group != null && sourceId.equals(group.sourceId())) {
                return InventoryTargetCanonicalizer.canonicalKey(
                        new LoadoutTarget.EquipmentSlotTarget(group.id(), slotIndex));
            }
        }
        return null;
    }

    private static String sourceKeyForTarget(InventoryHostDescriptor host, InventoryActionTarget target) {
        if (host == null || target == null) {
            return null;
        }
        if (target instanceof InventoryActionTarget.QuickAccessTarget quickAccessTarget) {
            var lane = host.quickAccessLane(quickAccessTarget.laneId());
            return lane == null ? null : lane.sourceId() + "#" + Math.max(0, quickAccessTarget.slotIndex());
        }
        if (target instanceof InventoryActionTarget.EquipmentTarget equipmentTarget) {
            var group = host.equipmentGroup(equipmentTarget.groupId());
            return group == null ? null : group.sourceId() + "#" + Math.max(0, equipmentTarget.slotIndex());
        }
        return null;
    }

    private static InventoryActionTarget toActionTarget(LoadoutTarget target) {
        return switch (target) {
            case LoadoutTarget.QuickAccessLaneTarget quickAccessLaneTarget ->
                    new InventoryActionTarget.QuickAccessTarget(quickAccessLaneTarget.laneId(), quickAccessLaneTarget.slotIndex());
            case LoadoutTarget.EquipmentSlotTarget equipmentSlotTarget ->
                    new InventoryActionTarget.EquipmentTarget(equipmentSlotTarget.groupId(), equipmentSlotTarget.slotIndex());
        };
    }

    private static InventoryActionTarget protectionTargetForSource(
            InventoryHostDescriptor host,
            String sourceId,
            int slotIndex
    ) {
        if (host == null || sourceId == null || sourceId.isBlank()) {
            return slotIndex < 0
                    ? new InventoryActionTarget.SourceTarget(sourceId)
                    : new InventoryActionTarget.SourceSlotTarget(sourceId, slotIndex);
        }

        InventorySourceDescriptor source = host.source(sourceId);
        if (source == null) {
            return slotIndex < 0
                    ? new InventoryActionTarget.SourceTarget(sourceId)
                    : new InventoryActionTarget.SourceSlotTarget(sourceId, slotIndex);
        }
        if (slotIndex < 0) {
            return new InventoryActionTarget.SourceTarget(sourceId);
        }
        return switch (source.role()) {
            case QUICK_ACCESS -> new InventoryActionTarget.QuickAccessTarget(source.laneId(), slotIndex);
            case EQUIPMENT, OFFHAND -> new InventoryActionTarget.EquipmentTarget(source.groupId(), slotIndex);
            default -> new InventoryActionTarget.SourceSlotTarget(sourceId, slotIndex);
        };
    }

    private static InventoryActionTarget protectionTargetForEntry(
            InventoryHostDescriptor host,
            String sourceId,
            InventoryEntrySnapshot snapshot
    ) {
        if (snapshot == null) {
            return new InventoryActionTarget.SourceTarget(sourceId);
        }
        return snapshot.slotBacked()
                ? protectionTargetForSource(host, sourceId, snapshot.slotIndex())
                : new InventoryActionTarget.SourceEntryTarget(sourceId, snapshot.entryId());
    }

    private static InventoryActionKind actionKindFor(LoadoutTarget target) {
        return switch (target) {
            case LoadoutTarget.QuickAccessLaneTarget ignored -> InventoryActionKind.ASSIGN;
            case LoadoutTarget.EquipmentSlotTarget ignored -> InventoryActionKind.ASSIGN;
        };
    }

    private static InventoryActionQuantity quantityFor(InventoryActionKind kind) {
        return switch (kind) {
            case ASSIGN -> InventoryActionQuantity.STACK;
            default -> InventoryActionQuantity.DEFAULT;
        };
    }

    private static InventoryActionConflictPolicy conflictPolicyFor(InventoryActionKind kind) {
        return switch (kind) {
            case ASSIGN -> InventoryActionConflictPolicy.ASSIGN_WITH_DISPLACE;
            case TRANSFER -> InventoryActionConflictPolicy.INSERT_ONLY;
            default -> InventoryActionConflictPolicy.DEFAULT;
        };
    }

    private static LoadoutTarget toLoadoutTarget(InventoryActionTarget target) {
        if (target == null) {
            return null;
        }
        return switch (target) {
            case InventoryActionTarget.QuickAccessTarget quickAccessTarget ->
                    new LoadoutTarget.QuickAccessLaneTarget(quickAccessTarget.laneId(), quickAccessTarget.slotIndex());
            case InventoryActionTarget.EquipmentTarget equipmentTarget ->
                    new LoadoutTarget.EquipmentSlotTarget(equipmentTarget.groupId(), equipmentTarget.slotIndex());
            default -> null;
        };
    }

    private static List<LoadoutTarget> requestedTargets(LoadoutApplyPlan plan) {
        LinkedHashSet<LoadoutTarget> requestedTargets = new LinkedHashSet<>(plan.satisfiedTargets());
        requestedTargets.addAll(plan.missingTargets());
        for (PlannedTargetOperation operation : plan.operations()) {
            if (operation != null && operation.target() != null) {
                requestedTargets.add(operation.target());
            }
        }
        return List.copyOf(requestedTargets);
    }

    public record LoadoutApplyPlan(
            String loadoutId,
            List<PlannedTargetOperation> operations,
            List<LoadoutTarget> satisfiedTargets,
            List<LoadoutTarget> missingTargets,
            List<String> diagnostics
    ) {
        public LoadoutApplyPlan {
            loadoutId = loadoutId == null ? "" : loadoutId;
            operations = operations == null ? List.of() : List.copyOf(operations);
            satisfiedTargets = satisfiedTargets == null ? List.of() : List.copyOf(satisfiedTargets);
            missingTargets = missingTargets == null ? List.of() : List.copyOf(missingTargets);
            diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
        }

        public List<InventoryActionRequest> requests() {
            return operations.stream()
                    .filter(Objects::nonNull)
                    .flatMap(operation -> operation.requests().stream())
                    .filter(Objects::nonNull)
                    .toList();
        }

        public static LoadoutApplyPlan empty(String loadoutId) {
            return new LoadoutApplyPlan(loadoutId, List.of(), List.of(), List.of(), List.of());
        }
    }

    public record PlannedTargetOperation(
            LoadoutTarget target,
            List<InventoryActionRequest> requests,
            InventoryActionRequest rollbackRequest
    ) {
        public PlannedTargetOperation {
            requests = requests == null ? List.of() : List.copyOf(requests);
        }
    }

    private record CandidateSource(
            String sourceId,
            int slotIndex,
            String entryId,
            ItemStack stack,
            ItemIdentity identity
    ) {
        private String stableKey() {
            return entryId == null || entryId.isBlank()
                    ? sourceId + "#" + Math.max(0, slotIndex)
                    : sourceId + "@" + entryId;
        }

        private InventoryActionTarget actionTarget() {
            return entryId == null || entryId.isBlank()
                    ? new InventoryActionTarget.SourceSlotTarget(sourceId, slotIndex)
                    : new InventoryActionTarget.SourceEntryTarget(sourceId, entryId);
        }
    }

    private record StagingTarget(String sourceId, int slotIndex) {
        private String stableKey() {
            return sourceId + "#" + Math.max(0, slotIndex);
        }
    }

    private record TargetOccupant(
            ItemIdentity identity,
            ItemStack stack
    ) {
        private boolean present() {
            return identity != null && stack != null && !stack.isEmpty();
        }
    }
}
