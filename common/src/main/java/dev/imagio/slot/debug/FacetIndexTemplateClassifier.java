package dev.imagio.slot.debug;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import dev.imagio.slot.inventory.triage.IslandSuggestionTemplate;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

/**
 * Drives the realistic-populate command's island taxonomy off the same
 * pipeline the live triage chip suggestions use. Given an
 * {@link ItemStack}, runs the platform-side extractor to produce an
 * {@link IslandSignalDescriptor} (FacetIndex role / material_family /
 * subsystem / organization group / activity / flavor / frequency /
 * rarity / origin / dye color, class subclass signals, item tags), then asks
 * {@link IslandSuggestionTemplate#firstMatchOrMisc} to pick a target
 * template.
 *
 * <p>Pairing populate output with chip-template targets means a player
 * accepting a chip on a populated atlas lands the item in the existing
 * island instead of a duplicate "Tools" / "Building Blocks" / etc.
 *
 * <p>The descriptor extractor is injected so {@code common/} doesn't
 * have to depend on the neoforge-side implementation.
 */
public final class FacetIndexTemplateClassifier {

    private final Function<ItemStack, IslandSignalDescriptor> extractor;

    public FacetIndexTemplateClassifier(Function<ItemStack, IslandSignalDescriptor> extractor) {
        if (extractor == null) {
            throw new IllegalArgumentException("extractor must not be null");
        }
        this.extractor = extractor;
    }

    public IslandSuggestionTemplate classify(ItemStack stack) {
        return IslandSuggestionTemplate.firstMatchOrMisc(describe(stack));
    }

    /**
     * Return the full descriptor for a stack — used by the populate
     * generator's dynamic-group matching pass, which needs richer facets
     * than just the resolved template.
     */
    public IslandSignalDescriptor describe(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return IslandSignalDescriptor.empty(ItemIdentity.of("minecraft:air"));
        }
        if (!FacetIndex.ENABLED) {
            // Same fallback the chip pipeline uses when classification is
            // disabled: rely on class/tag signals only. Items that
            // produce no class/tag match land on MISC.
            return extractor.apply(stack);
        }
        IslandSignalDescriptor descriptor = extractor.apply(stack);
        if (descriptor == null) {
            return IslandSignalDescriptor.empty(ItemIdentityMatcher.create(stack));
        }
        return descriptor;
    }
}
