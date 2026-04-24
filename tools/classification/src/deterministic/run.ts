import type { ItemExtractRecord } from "../extract/record.ts";
import type { SummaryBundle } from "../extract/vanilla/source.ts";
import { buildItemTagClosure } from "../extract/tags.ts";
import { validateSingleValue, validateMultiValue } from "../schema/facets.ts";
import type { Rule, RuleContext, RuleOutput } from "./types.ts";

import { modNamespaceRule } from "./rules/mod_namespace.ts";
import { booleansRule } from "./rules/booleans.ts";
import { equipSlotRule } from "./rules/equip_slot.ts";
import { materialFamilyRule } from "./rules/material_family.ts";
import { formRule } from "./rules/form.ts";
import { dyeColorRule } from "./rules/dye_color.ts";
import { requiredToolRule } from "./rules/required_tool.ts";
import { processingInRule } from "./rules/processing_in.ts";
import { originRule } from "./rules/origin.ts";
import { rarityRule } from "./rules/rarity.ts";

/** The ordered rule set. Order matters only for the (rare) case where two rules
 *  assert the same single-value facet — earlier wins. We currently don't have
 *  overlapping rules, but keep the order stable for future additions. */
export const DEFAULT_RULES: Rule[] = [
  modNamespaceRule,
  booleansRule,
  equipSlotRule,
  rarityRule,
  materialFamilyRule,
  formRule,
  dyeColorRule,
  requiredToolRule,
  processingInRule,
  originRule,
];

export interface DeterministicRunOptions {
  records: ItemExtractRecord[];
  bundle: SummaryBundle;
  namespace: string;
  rules?: Rule[];
  /** Callback invoked for validation warnings; defaults to console.warn. */
  onWarn?: (msg: string) => void;
}

export interface DeterministicRunResult {
  /** Layer-format JSON object (ready for wire-format validation). */
  layer: LayerFile;
  /** Per-facet coverage metrics: facet -> item count. */
  coverage: Record<string, number>;
  /** Validation warnings collected during the run. */
  warnings: string[];
}

export interface LayerFile {
  schema_version: number;
  layer: "vanilla-base" | "per-mod";
  source: string;
  generated_by?: string;
  generated_at?: string;
  entries: Record<string, LayerEntry>;
}

interface LayerEntry {
  facets: Record<string, LayerFacetEntry>;
}

type LayerFacetEntry =
  | SingleEntry
  | MultiEntry;

interface SingleEntry {
  value: string | number | boolean | null;
  mode?: "replace" | "override-if-null";
  confidence?: number;
  source?: string;
  rationale?: string;
}

interface MultiEntry {
  values: (string | number)[];
  mode?: "replace" | "add" | "remove";
  confidence?: number;
  source?: string;
  rationale?: string;
}

/**
 * Run every rule against every record, merge outputs, validate against the
 * facet registry, and assemble a layer file.
 *
 * Merge rules within a single run:
 *   - Single-value facet: first output wins (later rules don't override);
 *     duplicates from the same rule id raise a warning.
 *   - Multi-value facet: values are unioned across rule outputs.
 */
export function runDeterministic(
  options: DeterministicRunOptions,
): DeterministicRunResult {
  const rules = options.rules ?? DEFAULT_RULES;
  const warnings: string[] = [];
  const warn = (m: string) => {
    warnings.push(m);
    options.onWarn?.(m);
  };

  const blockTagClosure = buildItemTagClosure(
    options.bundle.blockTags ?? {},
    "minecraft",
  );
  const recipeTypes = buildRecipeTypeIndex(options.bundle, options.namespace);

  const coverage: Record<string, number> = {};
  const entries: Record<string, LayerEntry> = {};
  for (const record of options.records) {
    const facets: Record<string, LayerFacetEntry> = {};
    for (const rule of rules) {
      const ctx: RuleContext = {
        record,
        bundle: options.bundle,
        blockTagClosure,
        recipeTypes,
      };
      const outputs = rule.run(ctx);
      for (const output of outputs) {
        const invalid = validateOutput(output);
        if (invalid) {
          warn(`${record.id} ${rule.id}: ${invalid}`);
          continue;
        }
        mergeInto(facets, output, warn, record.id);
      }
    }
    if (Object.keys(facets).length === 0) continue;
    entries[record.id] = { facets };
    for (const facet of Object.keys(facets)) {
      coverage[facet] = (coverage[facet] ?? 0) + 1;
    }
  }

  const layer: LayerFile = {
    schema_version: 1,
    layer: "vanilla-base",
    source: options.namespace,
    entries,
  };
  return { layer, coverage, warnings };
}

function mergeInto(
  facets: Record<string, LayerFacetEntry>,
  output: RuleOutput,
  warn: (m: string) => void,
  itemId: string,
): void {
  if (output.kind === "single") {
    if (facets[output.facet]) {
      warn(
        `${itemId}: duplicate single-value assertion for '${output.facet}' from ${output.source}; keeping earlier`,
      );
      return;
    }
    facets[output.facet] = {
      value: output.value,
      ...(output.mode ? { mode: output.mode } : {}),
      ...(output.confidence !== undefined ? { confidence: output.confidence } : {}),
      source: output.source,
      ...(output.rationale ? { rationale: output.rationale } : {}),
    };
    return;
  }
  // multi
  const existing = facets[output.facet];
  if (!existing) {
    facets[output.facet] = {
      values: [...output.values].sort(),
      ...(output.mode ? { mode: output.mode } : { mode: "add" }),
      ...(output.confidence !== undefined ? { confidence: output.confidence } : {}),
      source: output.source,
      ...(output.rationale ? { rationale: output.rationale } : {}),
    };
    return;
  }
  if ("values" in existing) {
    const merged = new Set<string | number>([...existing.values, ...output.values]);
    existing.values = [...merged].sort((a, b) => String(a).localeCompare(String(b)));
  }
}

function validateOutput(output: RuleOutput): string | null {
  if (output.kind === "single") {
    const issue = validateSingleValue(output.facet, output.value);
    return issue ? issue.reason : null;
  }
  const issue = validateMultiValue(output.facet, output.values);
  return issue ? issue.reason : null;
}

function buildRecipeTypeIndex(
  bundle: SummaryBundle,
  namespace: string,
): Map<string, string> {
  const out = new Map<string, string>();
  for (const [shortId, recipe] of Object.entries(bundle.recipes)) {
    if (typeof recipe.type === "string") {
      out.set(`${namespace}:${shortId}`, recipe.type);
    }
  }
  return out;
}

