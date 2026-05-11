import Ajv, { type ValidateFunction } from "ajv";
import addFormats from "ajv-formats";
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";
import {
  FACETS,
  validateMultiValue,
  validateSingleValue,
} from "./facets.ts";
import {
  validateLayerAgainstVocabulary,
  type PackFacetVocabulary,
} from "./vocabulary.ts";

const here = dirname(fileURLToPath(import.meta.url));
const schemaPath = join(here, "..", "..", "layer.schema.json");

let cached: ValidateFunction | undefined;

/**
 * Build (and cache) the AJV validator for the layer wire format. Consumers call
 * `validateLayerFile(path)` or `validateLayer(obj)` to get a pass/fail + error list.
 */
export function layerValidator(): ValidateFunction {
  if (cached) return cached;
  const schema = JSON.parse(readFileSync(schemaPath, "utf8"));
  const ajv = new Ajv({ allErrors: true, strict: false });
  addFormats(ajv);
  cached = ajv.compile(schema);
  return cached;
}

export interface ValidateResult {
  ok: boolean;
  errors: string[];
}

export interface ValidateLayerOptions {
  vocabulary?: PackFacetVocabulary;
}

export function validateLayer(obj: unknown, options: ValidateLayerOptions = {}): ValidateResult {
  const validate = layerValidator();
  const ok = validate(obj);
  const errors = ok
    ? []
    : (validate.errors ?? []).map(
      (e) => `${e.instancePath || "<root>"} ${e.message ?? "invalid"}`,
    );
  if (errors.length === 0) {
    errors.push(...validateLayerFacets(obj));
  }
  if (errors.length === 0 && options.vocabulary) {
    errors.push(...validateLayerAgainstVocabulary(obj, options.vocabulary));
  }
  return errors.length === 0 ? { ok: true, errors: [] } : { ok: false, errors };
}

export function validateLayerFile(path: string, options: ValidateLayerOptions = {}): ValidateResult {
  const raw = readFileSync(path, "utf8");
  let parsed: unknown;
  try {
    parsed = JSON.parse(raw);
  } catch (err) {
    return { ok: false, errors: [`invalid JSON: ${(err as Error).message}`] };
  }
  return validateLayer(parsed, options);
}

function validateLayerFacets(obj: unknown): string[] {
  if (!isRecord(obj) || !isRecord(obj.entries)) {
    return [];
  }
  const errors: string[] = [];
  for (const [itemId, itemEntry] of Object.entries(obj.entries)) {
    if (!isRecord(itemEntry) || !isRecord(itemEntry.facets)) {
      continue;
    }
    for (const [facetId, rawEntry] of Object.entries(itemEntry.facets)) {
      const def = FACETS[facetId];
      if (!def) {
        errors.push(`/entries/${itemId}/facets/${facetId} unknown facet`);
        continue;
      }
      if (!isRecord(rawEntry)) {
        errors.push(`/entries/${itemId}/facets/${facetId} facet entry must be an object`);
        continue;
      }
      const isMulti =
        def.kind === "multi_enum" ||
        def.kind === "multi_free_text" ||
        def.kind === "multi_item_ref";
      const hasValues = "values" in rawEntry;
      const hasValue = "value" in rawEntry;
      const ambiguous = rawEntry.ambiguous === true;

      if (ambiguous) {
        if (def.kind !== "enum" && def.kind !== "free_text") {
          errors.push(`/entries/${itemId}/facets/${facetId} ambiguous only valid on enum/free_text`);
          continue;
        }
        const values = rawEntry.values;
        if (!Array.isArray(values) || values.length !== 2) {
          errors.push(`/entries/${itemId}/facets/${facetId} ambiguous requires exactly two values`);
          continue;
        }
        for (const value of values) {
          const issue = validateSingleValue(facetId, value);
          if (issue) errors.push(`/entries/${itemId}/facets/${facetId} ${issue.reason}`);
        }
        continue;
      }

      if (isMulti) {
        if (!hasValues) {
          errors.push(`/entries/${itemId}/facets/${facetId} multi-value facet requires values[]`);
          continue;
        }
        const values = rawEntry.values;
        if (!Array.isArray(values)) {
          errors.push(`/entries/${itemId}/facets/${facetId} values must be an array`);
          continue;
        }
        const issue = validateMultiValue(facetId, values);
        if (issue) errors.push(`/entries/${itemId}/facets/${facetId} ${issue.reason}`);
        continue;
      }

      if (hasValues) {
        errors.push(`/entries/${itemId}/facets/${facetId} single-value facet requires value`);
        continue;
      }
      if (!hasValue) {
        errors.push(`/entries/${itemId}/facets/${facetId} single-value facet requires value`);
        continue;
      }
      const issue = validateSingleValue(facetId, rawEntry.value);
      if (issue) errors.push(`/entries/${itemId}/facets/${facetId} ${issue.reason}`);
    }
  }
  return errors;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return !!value && typeof value === "object" && !Array.isArray(value);
}
