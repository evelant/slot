import Ajv, { type ValidateFunction } from "ajv";
import addFormats from "ajv-formats";
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";

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

export function validateLayer(obj: unknown): ValidateResult {
  const validate = layerValidator();
  const ok = validate(obj);
  if (ok) return { ok: true, errors: [] };
  return {
    ok: false,
    errors: (validate.errors ?? []).map(
      (e) => `${e.instancePath || "<root>"} ${e.message ?? "invalid"}`,
    ),
  };
}

export function validateLayerFile(path: string): ValidateResult {
  const raw = readFileSync(path, "utf8");
  let parsed: unknown;
  try {
    parsed = JSON.parse(raw);
  } catch (err) {
    return { ok: false, errors: [`invalid JSON: ${(err as Error).message}`] };
  }
  return validateLayer(parsed);
}
