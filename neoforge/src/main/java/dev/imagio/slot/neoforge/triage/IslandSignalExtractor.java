package dev.imagio.slot.neoforge.triage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.inventory.triage.IslandSignal;
import dev.imagio.slot.inventory.triage.IslandSignalDescriptor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

public final class IslandSignalExtractor {

    private IslandSignalExtractor() {
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
        } catch (LinkageError ignored) {
            tags = Set.of();
        }
        String namespace = namespaceOf(identity.itemId());
        return new IslandSignalDescriptor(identity, signals, tags, namespace, "");
    }

    private static void populateClassSignals(ItemStack stack, Set<IslandSignal> signals) {
        if (stack.has(DataComponents.FOOD)) {
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
        if (item instanceof MaceItem) {
            signals.add(IslandSignal.MACE);
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
