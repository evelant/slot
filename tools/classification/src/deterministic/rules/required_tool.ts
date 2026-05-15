import type { Rule, RuleOutput } from "../types.ts";

/**
 * Read block-tag membership for the item's block counterpart. When the item
 * isn't a block (tool, food, etc.) this rule does nothing.
 *
 * Tool-category tags: `minecraft:mineable/pickaxe|axe|shovel|hoe`.
 * Tool-tier vocabulary is pack-shaped, so `required_tool_tier` is assigned by
 * the LLM against usable vocabulary instead of this reference-only rule.
 */

const TOOL_TAG_PRIORITY: readonly { tag: string; kind: string }[] = [
  { tag: "minecraft:mineable/pickaxe", kind: "pickaxe" },
  { tag: "minecraft:mineable/axe", kind: "axe" },
  { tag: "minecraft:mineable/shovel", kind: "shovel" },
  { tag: "minecraft:mineable/hoe", kind: "hoe" },
];

/**
 * Items where the `mineable/<tool>` tag describes mining speed but the actual
 * required tool to GET A DROP is different. For glow_lichen, axe mines it
 * faster but only shears/silk-touch yield the item. Vanilla v1 canary catch.
 */
const REQUIRED_TOOL_OVERRIDES: Record<string, string> = {
  "minecraft:glow_lichen": "shears",
};

export const requiredToolRule: Rule = {
  id: "required_tool",
  facets: ["required_tool"],
  run({ record, blockTagClosure }) {
    const out: RuleOutput[] = [];
    const blockTags = blockTagsForRecord(record, blockTagClosure);

    const override = REQUIRED_TOOL_OVERRIDES[record.id];
    if (override) {
      out.push({
        facet: "required_tool",
        kind: "single",
        value: override,
        source: "rule:required_tool_id_override",
        confidence: 1,
        rationale: "id-specific override",
      });
      // Continue so ordinary mineable-tag inspection can still run for
      // diagnostics if needed; this rule no longer emits tool-tier facets.
    }
    if (!blockTags || blockTags.length === 0) {
      if (!override && record.path.startsWith("ore/") && isBlockItem(record)) {
        out.push({
          facet: "required_tool",
          kind: "single",
          value: "pickaxe",
          source: "rule:required_tool_from_ore_path",
          confidence: 0.95,
          rationale: "ore path block",
        });
      }
      return out;
    }

    if (!override) {
      const blockTagSet = new Set(blockTags);
      for (const { tag, kind } of TOOL_TAG_PRIORITY) {
        if (blockTagSet.has(tag)) {
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
    }

    return out;
  },
};

function isBlockItem(record: { extractor_meta?: Record<string, unknown> | null }): boolean {
  const meta = record.extractor_meta ?? {};
  return meta["is_block_item"] === true || typeof meta["block_id"] === "string";
}

function blockTagsForRecord(
  record: { id: string; extractor_meta?: Record<string, unknown> | null },
  blockTagClosure: ReadonlyMap<string, readonly string[]>,
): readonly string[] | undefined {
  const fromClosure = blockTagClosure.get(record.id);
  if (fromClosure && fromClosure.length > 0) return fromClosure;

  const meta = record.extractor_meta ?? {};
  const blockTags = meta["block_tags"];
  if (!Array.isArray(blockTags)) return undefined;
  const normalized = blockTags.filter((tag): tag is string => typeof tag === "string");
  return normalized.length > 0 ? normalized : undefined;
}
