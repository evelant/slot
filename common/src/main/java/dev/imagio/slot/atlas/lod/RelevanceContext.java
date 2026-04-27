package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.Set;
import java.util.UUID;

/**
 * Inputs every {@link RelevanceContributor} reads when scoring an
 * identity. The context is computed once per consumer (per render
 * frame on the client; per scoring decision on the server) and reused
 * across all contributors for that frame.
 *
 * <p>Per
 * {@code docs/decisions/0005-relevance-score-and-layout-locality.md},
 * the context is a derivation, not state — server and client may both
 * build their own with different field populations depending on what
 * they need the score for.
 *
 * <p>Adding a field is a constructor-signature break. Use
 * {@link #builder()} from production sites so they stay stable as the
 * context grows; tests and convenience constructors that only need
 * one field can keep using the {@code of*} factories.
 */
public record RelevanceContext(
        Set<ItemIdentity> carriedIdentities,
        Set<ItemIdentity> recentIdentities,
        Set<ItemIdentity> activeKitMembers,
        Set<ItemIdentity> activeKitMissing,
        Set<ItemIdentity> searchMatchedIdentities,
        Set<UUID> proximateAreaIds,
        Set<UUID> relevantStorageIds,
        Set<ItemIdentity> areaProximityBoostedIdentities,
        Set<ItemIdentity> chestHoldsRelevantIdentities
) {
    private static final RelevanceContext EMPTY = new RelevanceContext(
            Set.of(), Set.of(), Set.of(), Set.of(), Set.of(),
            Set.of(), Set.of(), Set.of(), Set.of());

    public RelevanceContext {
        carriedIdentities = carriedIdentities == null ? Set.of() : Set.copyOf(carriedIdentities);
        recentIdentities = recentIdentities == null ? Set.of() : Set.copyOf(recentIdentities);
        activeKitMembers = activeKitMembers == null ? Set.of() : Set.copyOf(activeKitMembers);
        activeKitMissing = activeKitMissing == null ? Set.of() : Set.copyOf(activeKitMissing);
        searchMatchedIdentities = searchMatchedIdentities == null ? Set.of() : Set.copyOf(searchMatchedIdentities);
        proximateAreaIds = proximateAreaIds == null ? Set.of() : Set.copyOf(proximateAreaIds);
        relevantStorageIds = relevantStorageIds == null ? Set.of() : Set.copyOf(relevantStorageIds);
        areaProximityBoostedIdentities = areaProximityBoostedIdentities == null ? Set.of() : Set.copyOf(areaProximityBoostedIdentities);
        chestHoldsRelevantIdentities = chestHoldsRelevantIdentities == null ? Set.of() : Set.copyOf(chestHoldsRelevantIdentities);
    }

    public static RelevanceContext empty() {
        return EMPTY;
    }

    public static RelevanceContext ofCarried(Set<ItemIdentity> carriedIdentities) {
        return builder().carriedIdentities(carriedIdentities).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isCarried(ItemIdentity identity) {
        return identity != null && carriedIdentities.contains(identity);
    }

    public boolean isRecent(ItemIdentity identity) {
        return identity != null && recentIdentities.contains(identity);
    }

    public boolean isActiveKitMember(ItemIdentity identity) {
        return identity != null && activeKitMembers.contains(identity);
    }

    public boolean isActiveKitMissing(ItemIdentity identity) {
        return identity != null && activeKitMissing.contains(identity);
    }

    public boolean matchesActiveSearch(ItemIdentity identity) {
        return identity != null && searchMatchedIdentities.contains(identity);
    }

    public boolean isProximateArea(UUID areaId) {
        return areaId != null && proximateAreaIds.contains(areaId);
    }

    public boolean isRelevantStorage(UUID storageId) {
        return storageId != null && relevantStorageIds.contains(storageId);
    }

    public boolean isAreaProximityBoosted(ItemIdentity identity) {
        return identity != null && areaProximityBoostedIdentities.contains(identity);
    }

    public boolean chestHoldsRelevant(ItemIdentity identity) {
        return identity != null && chestHoldsRelevantIdentities.contains(identity);
    }

    public static final class Builder {
        private Set<ItemIdentity> carriedIdentities = Set.of();
        private Set<ItemIdentity> recentIdentities = Set.of();
        private Set<ItemIdentity> activeKitMembers = Set.of();
        private Set<ItemIdentity> activeKitMissing = Set.of();
        private Set<ItemIdentity> searchMatchedIdentities = Set.of();
        private Set<UUID> proximateAreaIds = Set.of();
        private Set<UUID> relevantStorageIds = Set.of();
        private Set<ItemIdentity> areaProximityBoostedIdentities = Set.of();
        private Set<ItemIdentity> chestHoldsRelevantIdentities = Set.of();

        private Builder() {
        }

        public Builder carriedIdentities(Set<ItemIdentity> values) {
            this.carriedIdentities = values == null ? Set.of() : values;
            return this;
        }

        public Builder recentIdentities(Set<ItemIdentity> values) {
            this.recentIdentities = values == null ? Set.of() : values;
            return this;
        }

        public Builder activeKitMembers(Set<ItemIdentity> values) {
            this.activeKitMembers = values == null ? Set.of() : values;
            return this;
        }

        public Builder activeKitMissing(Set<ItemIdentity> values) {
            this.activeKitMissing = values == null ? Set.of() : values;
            return this;
        }

        public Builder searchMatchedIdentities(Set<ItemIdentity> values) {
            this.searchMatchedIdentities = values == null ? Set.of() : values;
            return this;
        }

        public Builder proximateAreaIds(Set<UUID> values) {
            this.proximateAreaIds = values == null ? Set.of() : values;
            return this;
        }

        public Builder relevantStorageIds(Set<UUID> values) {
            this.relevantStorageIds = values == null ? Set.of() : values;
            return this;
        }

        public Builder areaProximityBoostedIdentities(Set<ItemIdentity> values) {
            this.areaProximityBoostedIdentities = values == null ? Set.of() : values;
            return this;
        }

        public Builder chestHoldsRelevantIdentities(Set<ItemIdentity> values) {
            this.chestHoldsRelevantIdentities = values == null ? Set.of() : values;
            return this;
        }

        public RelevanceContext build() {
            return new RelevanceContext(
                    carriedIdentities,
                    recentIdentities,
                    activeKitMembers,
                    activeKitMissing,
                    searchMatchedIdentities,
                    proximateAreaIds,
                    relevantStorageIds,
                    areaProximityBoostedIdentities,
                    chestHoldsRelevantIdentities
            );
        }
    }
}
