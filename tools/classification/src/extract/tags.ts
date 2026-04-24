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
export interface ItemTagMembership {
  /** Full transitive closure of tags this item belongs to. */
  all: string[];
  /** Subset of `all`: tags where this item appears as a direct listed member
   *  (not via a nested `#tag` reference). Usually the stronger signal for
   *  classification. */
  direct: string[];
}

/**
 * Like `buildItemTagClosure` but returns a richer membership object that
 * splits direct vs transitive memberships. Prefer this over the legacy
 * closure-only helper when the caller wants to weight direct tags higher.
 */
export function buildItemTagMembership(
  itemTags: Record<string, TagJson>,
  defaultNamespace: string,
): Map<string, ItemTagMembership> {
  const { closure, directPerItem } = buildTagIndices(itemTags, defaultNamespace);
  const out = new Map<string, ItemTagMembership>();
  for (const [item, all] of closure) {
    out.set(item, {
      all: [...all].sort(),
      direct: [...(directPerItem.get(item) ?? new Set<string>())].sort(),
    });
  }
  return out;
}

export function buildItemTagClosure(
  itemTags: Record<string, TagJson>,
  defaultNamespace: string,
): Map<string, string[]> {
  const { closure } = buildTagIndices(itemTags, defaultNamespace);
  const result = new Map<string, string[]>();
  for (const [item, tags] of closure) {
    result.set(item, [...tags].sort());
  }
  return result;
}

/**
 * Shared core for `buildItemTagClosure` and `buildItemTagMembership`:
 * returns both the item→transitive-tag-closure map and the
 * item→direct-tag-only map computed off the same data.
 */
function buildTagIndices(
  itemTags: Record<string, TagJson>,
  defaultNamespace: string,
): {
  closure: Map<string, Set<string>>;
  directPerItem: Map<string, Set<string>>;
} {
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

  // Invert closure: item id -> tag ids (transitive)
  const closure = new Map<string, Set<string>>();
  for (const [tagId, items] of fullMembers) {
    for (const item of items) {
      const bucket = closure.get(item) ?? new Set<string>();
      bucket.add(tagId);
      closure.set(item, bucket);
    }
  }

  // Invert direct-only: item id -> tag ids where item is a direct member
  const directPerItem = new Map<string, Set<string>>();
  for (const [tagId, items] of directMembers) {
    for (const item of items) {
      const bucket = directPerItem.get(item) ?? new Set<string>();
      bucket.add(tagId);
      directPerItem.set(item, bucket);
    }
  }

  return { closure, directPerItem };
}

function normalize(id: string, defaultNamespace: string): string {
  return id.includes(":") ? id : `${defaultNamespace}:${id}`;
}
