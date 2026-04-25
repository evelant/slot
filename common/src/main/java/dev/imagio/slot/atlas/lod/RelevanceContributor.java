package dev.imagio.slot.atlas.lod;

import dev.imagio.slot.inventory.core.ItemIdentity;

/**
 * One named axis that lifts an item's relevance. Implementations are
 * pure functions of (identity, context); they MUST NOT read mutable
 * state outside the context. Contributors are addable without changing
 * the model — see the catalog in
 * {@code docs/design/relevance-lod.md § Relevance contributors}.
 */
public interface RelevanceContributor {
    /**
     * Stable, debuggable identifier used as the key in
     * {@link RelevanceScore#contributions}. Lower-snake-case
     * (e.g. {@code "carried"}, {@code "kit_member"}).
     */
    String name();

    /**
     * Score this contributor assigns to {@code identity}, in
     * {@code [0, 1]}. Values outside that range are clamped by
     * {@link RelevanceScore#compute}; {@code NaN} is treated as zero.
     */
    float score(ItemIdentity identity, RelevanceContext context);
}
