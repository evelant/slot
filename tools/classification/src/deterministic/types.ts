import type { ItemExtractRecord } from "../extract/record.ts";
import type { SummaryBundle } from "../extract/vanilla/source.ts";

/**
 * Everything a deterministic rule might need. Rules are pure functions over
 * this context — they never touch disk. The runner builds the context once and
 * calls every rule against every record.
 *
 * The shared precomputed tables (`blockTagClosure`, `recipeTypes`) are built
 * once up front so each rule stays O(record * rule), not O(record * rule * N).
 */
export interface RuleContext {
  record: ItemExtractRecord;
  bundle: SummaryBundle;
  /** item id -> full block-tag closure (block tags, namespaced). */
  blockTagClosure: Map<string, readonly string[]>;
  /** fully-qualified recipe id -> `type` field (e.g. `minecraft:smelting`). */
  recipeTypes: ReadonlyMap<string, string>;
}

export interface SingleRuleOutput {
  facet: string;
  kind: "single";
  value: string | number | boolean | null;
  mode?: "replace" | "override-if-null";
  confidence?: number;
  source: string;
  rationale?: string;
}

export interface MultiRuleOutput {
  facet: string;
  kind: "multi";
  values: (string | number)[];
  mode?: "replace" | "add" | "remove";
  confidence?: number;
  source: string;
  rationale?: string;
}

export type RuleOutput = SingleRuleOutput | MultiRuleOutput;

export interface Rule {
  /** Stable rule id used as the `source` prefix in layer entries. */
  id: string;
  /** The facets this rule can emit — for coverage metrics and debugging. */
  facets: readonly string[];
  run(ctx: RuleContext): RuleOutput[];
}
