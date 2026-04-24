import type { LootTableJson } from "./vanilla/source.ts";

/**
 * For each item, the set of loot tables that can produce it. Walks the
 * pool/entry tree recursively; follows `children` for alternatives/groups;
 * also picks up `set_contents` / `looting_enchant`-style function outputs
 * that name items.
 *
 * Loot-table ids are stored as short paths (no namespace) in mcmeta's summary;
 * we prefix them with the default namespace to make them fully-qualified for
 * downstream consumers.
 */
export function buildLootSources(
  lootTables: Record<string, LootTableJson>,
  defaultNamespace: string,
): Map<string, string[]> {
  const sources = new Map<string, Set<string>>();
  const add = (itemId: string, tableId: string) => {
    const bucket = sources.get(itemId) ?? new Set<string>();
    bucket.add(tableId);
    sources.set(itemId, bucket);
  };

  for (const [shortId, table] of Object.entries(lootTables)) {
    const tableId = normalize(shortId, defaultNamespace);
    const items = new Set<string>();
    walkTable(table, defaultNamespace, items);
    for (const item of items) add(item, tableId);
  }

  const result = new Map<string, string[]>();
  for (const [item, tables] of sources) {
    result.set(item, [...tables].sort());
  }
  return result;
}

function walkTable(
  table: LootTableJson,
  defaultNamespace: string,
  out: Set<string>,
): void {
  for (const pool of table.pools ?? []) {
    for (const entry of pool.entries ?? []) walkEntry(entry, defaultNamespace, out);
  }
}

function walkEntry(
  entry: unknown,
  defaultNamespace: string,
  out: Set<string>,
): void {
  if (!entry || typeof entry !== "object") return;
  const e = entry as Record<string, unknown>;

  if (e.type === "minecraft:item" && typeof e.name === "string") {
    out.add(normalize(e.name, defaultNamespace));
  }

  // alternatives/group/sequence: `children: [entry, ...]`
  if (Array.isArray(e.children)) {
    for (const child of e.children) walkEntry(child, defaultNamespace, out);
  }

  // functions may add additional items (set_contents, etc.). Walk them loosely —
  // anything with an `add`, `options`, or nested `name`.
  if (Array.isArray(e.functions)) {
    for (const fn of e.functions) walkFunction(fn, defaultNamespace, out);
  }
}

function walkFunction(
  fn: unknown,
  defaultNamespace: string,
  out: Set<string>,
): void {
  if (!fn || typeof fn !== "object") return;
  const f = fn as Record<string, unknown>;
  // minecraft:set_contents ships its own `entries: [entry, ...]` for container drops.
  if (Array.isArray(f.entries)) {
    for (const inner of f.entries) walkEntry(inner, defaultNamespace, out);
  }
}

function normalize(id: string, defaultNamespace: string): string {
  return id.includes(":") ? id : `${defaultNamespace}:${id}`;
}
