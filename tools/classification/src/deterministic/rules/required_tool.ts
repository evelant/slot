import type { Rule, RuleOutput } from "../types.ts";

/**
 * Read block-tag membership for the item's block counterpart. When the item
 * isn't a block (tool, food, etc.) this rule does nothing.
 *
 * Tool-category tags: `minecraft:mineable/pickaxe|axe|shovel|hoe`.
 * Tier tags: `minecraft:needs_stone_tool|needs_iron_tool|needs_diamond_tool`.
 * Default tier (not tagged) is `wood` for anything under the pickaxe/etc.
 */

const TOOL_TAG_TO_KIND: Record<string, string> = {
  "minecraft:mineable/pickaxe": "pickaxe",
  "minecraft:mineable/axe": "axe",
  "minecraft:mineable/shovel": "shovel",
  "minecraft:mineable/hoe": "hoe",
};

const TIER_TAG_TO_VALUE: Record<string, string> = {
  "minecraft:needs_stone_tool": "stone",
  "minecraft:needs_iron_tool": "iron",
  "minecraft:needs_diamond_tool": "diamond",
};

export const requiredToolRule: Rule = {
  id: "required_tool",
  facets: ["required_tool", "required_tool_tier"],
  run({ record, blockTagClosure }) {
    const out: RuleOutput[] = [];
    const blockTags = blockTagClosure.get(record.id);
    if (!blockTags || blockTags.length === 0) return out;

    for (const tag of blockTags) {
      const kind = TOOL_TAG_TO_KIND[tag];
      if (kind) {
        out.push({
          facet: "required_tool",
          kind: "single",
          value: kind,
          source: "rule:required_tool_from_block_tag",
          confidence: 1,
          rationale: `tag ${tag}`,
        });
        break;
      }
    }

    for (const tag of blockTags) {
      const tier = TIER_TAG_TO_VALUE[tag];
      if (tier) {
        out.push({
          facet: "required_tool_tier",
          kind: "single",
          value: tier,
          source: "rule:required_tool_tier_from_block_tag",
          confidence: 1,
          rationale: `tag ${tag}`,
        });
        break;
      }
    }

    return out;
  },
};
