package dev.imagio.slot.workflow.domain;

import dev.imagio.slot.inventory.core.ItemIdentity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public final class WorkflowAcceptedInputOptions {
    private static final int MAX_TAG_OPTIONS = 8;

    private WorkflowAcceptedInputOptions() {
    }

    public static List<WorkflowAcceptedInputRule> forItem(ItemIdentity identity, Collection<String> itemTags) {
        if (identity == null) {
            return List.of();
        }
        ArrayList<WorkflowAcceptedInputRule> rules = new ArrayList<>();
        WorkflowAcceptedInputRule exact = WorkflowAcceptedInputRule.exact(identity);
        if (exact != null) {
            rules.add(exact);
        }

        ArrayList<String> tags = new ArrayList<>(normalizedTags(itemTags));
        tags.sort(Comparator
                .comparingInt(WorkflowAcceptedInputOptions::tagRank)
                .thenComparingInt(WorkflowAcceptedInputOptions::namespaceRank)
                .thenComparing(WorkflowAcceptedInputOptions::tagPath)
                .thenComparing(tag -> tag));
        int added = 0;
        for (String tag : tags) {
            if (broadProcessPath(tagPath(tag))) {
                continue;
            }
            WorkflowAcceptedInputRule rule = WorkflowAcceptedInputRule.itemTag(tag);
            if (rule != null && !rules.contains(rule)) {
                rules.add(rule);
                added++;
                if (added >= MAX_TAG_OPTIONS) {
                    break;
                }
            }
        }
        return rules.isEmpty() ? List.of() : List.copyOf(rules);
    }

    private static LinkedHashSet<String> normalizedTags(Collection<String> itemTags) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        if (itemTags == null) {
            return tags;
        }
        for (String tag : itemTags) {
            String normalized = WorkflowAcceptedInputRule.normalizeTagId(tag);
            if (!normalized.isBlank()) {
                tags.add(normalized);
            }
        }
        return tags;
    }

    private static int tagRank(String tag) {
        String path = tagPath(tag);
        if (path.isBlank()) {
            return 100;
        }
        if (materialSpecificPath(path)) {
            return 0;
        }
        if (broadProcessPath(path)) {
            return 20;
        }
        if (path.contains("/")) {
            return 40;
        }
        return 80;
    }

    private static boolean materialSpecificPath(String path) {
        if (!path.contains("/")) {
            return false;
        }
        return startsWithAny(path,
                "ores/",
                "raw_materials/",
                "raw_blocks/",
                "crushed_ores/",
                "crushed/",
                "dusts/",
                "dirty_dusts/",
                "impure_dusts/",
                "pure_dusts/",
                "powders/",
                "gems/",
                "ingots/",
                "nuggets/",
                "plates/",
                "rods/",
                "sheets/",
                "double_ingots/",
                "double_sheets/",
                "storage_blocks/",
                "clumps/",
                "shards/",
                "crystals/");
    }

    private static boolean broadProcessPath(String path) {
        return path.equals("ores")
                || path.equals("metal_ores")
                || path.equals("ore_pieces")
                || path.equals("small_ore_pieces")
                || path.equals("raw_materials")
                || path.equals("raw_blocks")
                || path.equals("crushed_ores")
                || path.equals("crushed")
                || path.equals("dirty_dusts")
                || path.equals("impure_dusts")
                || path.equals("pure_dusts")
                || path.equals("dusts")
                || path.equals("powders")
                || path.equals("gems")
                || path.equals("ingots")
                || path.equals("nuggets")
                || path.equals("plates")
                || path.equals("rods")
                || path.equals("sheets")
                || path.equals("double_ingots")
                || path.equals("double_sheets")
                || path.equals("storage_blocks")
                || path.equals("clumps")
                || path.equals("shards")
                || path.equals("crystals");
    }

    private static boolean startsWithAny(String value, String... prefixes) {
        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static int namespaceRank(String tag) {
        String namespace = tagNamespace(tag);
        if (namespace.equals("c")) {
            return 0;
        }
        if (namespace.equals("forge")) {
            return 1;
        }
        return 2;
    }

    private static String tagNamespace(String tag) {
        int colon = tag == null ? -1 : tag.indexOf(':');
        return colon <= 0 ? "" : tag.substring(0, colon).toLowerCase(Locale.ROOT);
    }

    private static String tagPath(String tag) {
        int colon = tag == null ? -1 : tag.indexOf(':');
        return colon < 0 || colon + 1 >= tag.length()
                ? ""
                : tag.substring(colon + 1).toLowerCase(Locale.ROOT);
    }
}
