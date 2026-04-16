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

            CandidateSource candidate = findCandidateSource(
                    authority,
                    identityResolver,
                    entry.identity(),
                    targetKind,
                    resolvedProtection,
                    reservedSourceSlots
            );
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
                rollbackRequest = new InventoryActionRequest(
                        host.hostId(),
                        host.serverMenuRef(),
                        UUID.randomUUID().toString(),
                        targetKind,
                        resolvedMode,
                        quantityFor(targetKind),
                        InventoryActionScope.LOADOUT,
                        conflictPolicyFor(targetKind),
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
                currentTargets.remove(targetKey);
            }

            reservedSourceSlots.add(candidate.stableKey());
            requests.add(new InventoryActionRequest(
                    host.hostId(),
                    host.serverMenuRef(),
                    UUID.randomUUID().toString(),
                    targetKind,
                    resolvedMode,
                    quantityFor(targetKind),
                    InventoryActionScope.LOADOUT,
                    conflictPolicyFor(targetKind),
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
            operations.add(new PlannedTargetOperation(entry.target(), requests, rollbackRequest));
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
        for (InventorySourceDescriptor source : authority.carriedSources().stream()
                .filter(candidate -> candidate != null && candidate.supports(InventoryCapability.INSERT))
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
