import type {
  ItemDefinitionJson,
  ModelJson,
} from "./vanilla/source.ts";

/**
 * Resolve an item's `item_definition` -> model chain. Returns the chain of
 * model ids from the item's direct model up through parents, stripping
 * `minecraft:` namespaces so downstream callers can pattern-match on the
 * short form (`item/generated`, `block/stairs`).
 *
 * `item_definition` entries for 1.21+ wrap the model in a variant tree
 * (`minecraft:range_dispatch`, `minecraft:condition`, etc.). We recurse into
 * those to find the first concrete `{ type: 'minecraft:model', model: ... }`
 * node and follow its parent chain — that's the "representative" model for
 * classification purposes. Contextual variants aren't useful to us here.
 */
export function resolveModelParents(
  definition: ItemDefinitionJson | undefined,
  models: Record<string, ModelJson>,
): string[] {
  const rootModelId = findFirstModelId(definition?.model);
  if (!rootModelId) return [];

  const chain: string[] = [];
  const seen = new Set<string>();
  let current: string | undefined = rootModelId;
  while (current && !seen.has(current)) {
    seen.add(current);
    chain.push(current);
    const model = models[current];
    if (!model?.parent) break;
    current = stripNamespace(model.parent);
  }
  return chain;
}

function findFirstModelId(def: unknown): string | undefined {
  if (!def || typeof def !== "object") return undefined;
  const d = def as Record<string, unknown>;
  if (d.type === "minecraft:model" && typeof d.model === "string") {
    return stripNamespace(d.model);
  }
  // minecraft:special (banner, chest, head, shulker_box, conduit) carries the
  // shape data inline but always names a plain item-model in `base`. That base
  // is the right id for classification — the `model` sub-object is renderer data.
  if (d.type === "minecraft:special" && typeof d.base === "string") {
    return stripNamespace(d.base);
  }
  // composite picks arbitrarily many child models; the first is a fine representative.
  if (Array.isArray(d.models)) {
    for (const m of d.models) {
      const found = findFirstModelId(m);
      if (found) return found;
    }
  }
  // select / condition / range_dispatch wrappers — look inside cases/entries/fallback/branches.
  if (Array.isArray(d.cases)) {
    for (const c of d.cases) {
      if (!c || typeof c !== "object") continue;
      const found = findFirstModelId((c as { model?: unknown }).model);
      if (found) return found;
    }
  }
  if (Array.isArray(d.entries)) {
    for (const e of d.entries) {
      if (!e || typeof e !== "object") continue;
      const found = findFirstModelId((e as { model?: unknown }).model);
      if (found) return found;
    }
  }
  if (d.fallback) {
    const found = findFirstModelId(d.fallback);
    if (found) return found;
  }
  if (d.on_true) {
    const found = findFirstModelId(d.on_true);
    if (found) return found;
  }
  if (d.on_false) {
    const found = findFirstModelId(d.on_false);
    if (found) return found;
  }
  return undefined;
}

function stripNamespace(id: string): string {
  const idx = id.indexOf(":");
  return idx >= 0 ? id.slice(idx + 1) : id;
}
