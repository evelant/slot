import type { TagJson } from "./vanilla/source.ts";

/**
 * Build a reverse index: for every item, the set of fully-qualified item-tag
 * ids it belongs to transitively. Tag values may reference other tags via
 * `#namespace:tag`; we resolve those chains down to concrete items.
 *
 * Tags live in a single namespace (the one whose data file we're reading). Any
 * `#namespace:id` value ending in a different namespace still resolves if we
 * know about it — when loading vanilla alone, foreign-namespace references are
 * just ignored.
 *
 * The `replace: true` flag means "the lower layer's members are discarded" —
 * vanilla's own summary never sets this, but we honor it for completeness.
 */
export function buildItemTagClosure(
  itemTags: Record<string, TagJson>,
  defaultNamespace: string,
): Map<string, string[]> {
  // direct members: tag id -> set of item ids (both fully-qualified)
  const directMembers = new Map<string, Set<string>>();
  // tag id -> set of sub-tag ids it references
  const subTagRefs = new Map<string, Set<string>>();

  for (const [shortId, def] of Object.entries(itemTags)) {
    const tagId = `${defaultNamespace}:${shortId}`;
    const members = directMembers.get(tagId) ?? new Set<string>();
    const subs = subTagRefs.get(tagId) ?? new Set<string>();

    for (const raw of def.values ?? []) {
      // In some newer formats, a value may be `{ id: "...", required: false }`.
      const entry = typeof raw === "string" ? raw : (raw.id ?? "");
      if (!entry) continue;

      if (entry.startsWith("#")) {
        subs.add(normalize(entry.slice(1), defaultNamespace));
      } else {
        members.add(normalize(entry, defaultNamespace));
      }
    }
    directMembers.set(tagId, members);
    subTagRefs.set(tagId, subs);
  }

  // Fixed-point iteration: propagate sub-tag members into their parents until
  // no set grows. Cycles just converge on the union of their parts. Tag data
  // is small (≈200 item tags in vanilla), so the extra passes are cheap.
  const fullMembers = new Map<string, Set<string>>();
  for (const [tagId, members] of directMembers) {
    fullMembers.set(tagId, new Set(members));
  }
  let changed = true;
  while (changed) {
    changed = false;
    for (const [tagId, subs] of subTagRefs) {
      const target = fullMembers.get(tagId)!;
      for (const sub of subs) {
        const subMembers = fullMembers.get(sub);
        if (!subMembers) continue;
        for (const m of subMembers) {
          if (!target.has(m)) {
            target.add(m);
            changed = true;
          }
        }
      }
    }
  }

  // Invert: item id -> tag ids.
  const itemToTags = new Map<string, Set<string>>();
  for (const [tagId, items] of fullMembers) {
    for (const item of items) {
      const bucket = itemToTags.get(item) ?? new Set<string>();
      bucket.add(tagId);
      itemToTags.set(item, bucket);
    }
  }

  const result = new Map<string, string[]>();
  for (const [item, tags] of itemToTags) {
    result.set(item, [...tags].sort());
  }
  return result;
}

function normalize(id: string, defaultNamespace: string): string {
  return id.includes(":") ? id : `${defaultNamespace}:${id}`;
}
