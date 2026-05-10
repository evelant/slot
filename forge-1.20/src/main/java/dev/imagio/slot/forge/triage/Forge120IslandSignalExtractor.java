package dev.imagio.slot.forge.triage;

import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.classification.FacetIndexHolder;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.triage.IslandSignal;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Forge120IslandSignalExtractor {
    private Forge120IslandSignalExtractor() {
    }

    public static IslandSignalDescriptor extract(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return IslandSignalDescriptor.empty(ItemIdentity.of("minecraft:air"));
        }
        ItemIdentity identity = ItemIdentityMatcher.create(stack);
        EnumSet<IslandSignal> signals = EnumSet.noneOf(IslandSignal.class);
        Set<String> tags;
        try {
            populateClassSignals(stack, signals);
            tags = collectTags(stack);
        } catch (RuntimeException | LinkageError ignored) {
            tags = Set.of();
        }
        String namespace = namespaceOf(identity.itemId());
        Facets facets = facetsFor(identity);
        return new IslandSignalDescriptor(
                identity,
                signals,
                tags,
                namespace,
                "",
                facets.role,
                facets.roleAlternatives,
                facets.materialFamily,
                facets.subsystems,
                facets.organizationGroups,
                facets.activities,
                facets.flavor,
                facets.carryFrequency,
                facets.rarity,
                facets.origin,
                facets.dyeColor,
                facets.palette,
                facets.form,
                facets.emitsLight
        );
    }

    private record Facets(
            String role,
            List<String> roleAlternatives,
            String materialFamily,
            List<String> subsystems,
            List<String> organizationGroups,
            List<String> activities,
            String flavor,
            String carryFrequency,
            String rarity,
            String origin,
            String dyeColor,
            List<String> palette,
            String form,
            boolean emitsLight
    ) {
        static Facets empty() {
            return new Facets(null, List.of(), null, List.of(), List.of(), List.of(), null, null, null, null, null, List.of(), null, false);
        }
    }

    private static Facets facetsFor(ItemIdentity identity) {
        if (!FacetIndex.ENABLED || identity == null) {
            return Facets.empty();
        }
        try {
            FacetIndex index = FacetIndexHolder.get();
            String itemId = identity.itemId();
            return new Facets(
                    index.role(itemId).orElse(null),
                    index.roleAlternatives(itemId),
                    index.materialFamily(itemId).orElse(null),
                    index.subsystems(itemId),
                    index.organizationGroups(itemId),
                    index.activities(itemId),
                    index.flavor(itemId).orElse(null),
                    index.carryFrequency(itemId).orElse(null),
                    index.rarity(itemId).orElse(null),
                    index.origin(itemId).orElse(null),
                    index.dyeColor(itemId).orElse(null),
                    index.palette(itemId),
                    index.form(itemId).orElse(null),
                    index.emitsLight(itemId)
            );
        } catch (RuntimeException | LinkageError ignored) {
            return Facets.empty();
        }
    }

    private static void populateClassSignals(ItemStack stack, Set<IslandSignal> signals) {
        if (stack.getFoodProperties(null) != null) {
            signals.add(IslandSignal.FOOD);
        }
        Item item = stack.getItem();
        if (item instanceof DiggerItem) {
            signals.add(IslandSignal.DIGGER_TOOL);
        }
        if (item instanceof SwordItem) {
            signals.add(IslandSignal.SWORD);
        }
        if (item instanceof BowItem) {
            signals.add(IslandSignal.BOW);
        }
        if (item instanceof CrossbowItem) {
            signals.add(IslandSignal.CROSSBOW);
        }
        if (item instanceof TridentItem) {
            signals.add(IslandSignal.TRIDENT);
        }
        if (item instanceof ArmorItem armor) {
            ArmorItem.Type type = armor.getType();
            if (type == ArmorItem.Type.HELMET) {
                signals.add(IslandSignal.ARMOR_HEAD);
            } else if (type == ArmorItem.Type.CHESTPLATE) {
                signals.add(IslandSignal.ARMOR_CHEST);
            } else if (type == ArmorItem.Type.LEGGINGS) {
                signals.add(IslandSignal.ARMOR_LEGS);
            } else if (type == ArmorItem.Type.BOOTS) {
                signals.add(IslandSignal.ARMOR_FEET);
            }
        }
    }

    private static Set<String> collectTags(ItemStack stack) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        stack.getTags().forEach(tagKey -> {
            ResourceLocation location = tagKey.location();
            if (location != null) {
                tags.add(location.getNamespace() + ":" + location.getPath());
            }
        });
        return tags;
    }

    private static String namespaceOf(String itemId) {
        if (itemId == null) {
            return "";
        }
        int colon = itemId.indexOf(':');
        return colon <= 0 ? "" : itemId.substring(0, colon);
    }
}
