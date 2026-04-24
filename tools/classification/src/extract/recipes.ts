import type { RecipeJson } from "./vanilla/source.ts";
import type { RecipeRole } from "./record.ts";

/**
 * Walk every recipe and build, for each item, the set of recipes that consume
 * it as an ingredient and the set of recipes that produce it as a result.
 *
 * Ingredient references can be:
 *   - a concrete item id (`"minecraft:stick"`)
 *   - a tag reference (`"#minecraft:planks"`) — resolved via the provided tag closure
 *   - an ingredient object `{ item | tag }`
 *   - a list of the above (union — any one satisfies)
 *
 * For tag refs we fan out to every concrete member, then de-dup at the end.
 *
 * The key for every recipe is the recipe id from the input map. We don't try
 * to preserve the recipe namespace — the map is already keyed that way.
 */
export function buildRecipeRoles(
  recipes: Record<string, RecipeJson>,
  defaultNamespace: string,
  tagMembers: Map<string, string[]>,
): Map<string, RecipeRole> {
  const ingredientOf = new Map<string, Set<string>>();
  const outputOf = new Map<string, Set<string>>();
  const ingredientCounts = new Map<string, Map<string, number>>();
  const outputCounts = new Map<string, Map<string, number>>();

  const bump = (
    counts: Map<string, Map<string, number>>,
    itemId: string,
    type: string,
  ) => {
    const bucket = counts.get(itemId) ?? new Map<string, number>();
    bucket.set(type, (bucket.get(type) ?? 0) + 1);
    counts.set(itemId, bucket);
  };

  for (const [shortId, recipe] of Object.entries(recipes)) {
    const recipeId = normalize(shortId, defaultNamespace);
    const type = shortRecipeType(recipe.type);

    // Dedup ingredient items within a recipe so a shaped pattern that uses
    // the same key twice counts as one contribution for this recipe.
    const seenIngredients = new Set<string>();
    const ingest = (value: unknown) => {
      collectIngredientItems(value, defaultNamespace, tagMembers).forEach(
        (itemId) => {
          if (seenIngredients.has(itemId)) return;
          seenIngredients.add(itemId);
          const bucket = ingredientOf.get(itemId) ?? new Set<string>();
          bucket.add(recipeId);
          ingredientOf.set(itemId, bucket);
          bump(ingredientCounts, itemId, type);
        },
      );
    };

    for (const field of INGREDIENT_FIELDS) {
      const value = (recipe as Record<string, unknown>)[field];
      if (value !== undefined) ingest(value);
    }
    if (recipe.key && typeof recipe.key === "object") {
      for (const value of Object.values(recipe.key)) ingest(value);
    }
    if (recipe.shapes && typeof recipe.shapes === "object") {
      for (const value of Object.values(recipe.shapes)) ingest(value);
    }

    const resultId = extractResultId(recipe.result, defaultNamespace);
    if (resultId) {
      const bucket = outputOf.get(resultId) ?? new Set<string>();
      bucket.add(recipeId);
      outputOf.set(resultId, bucket);
      bump(outputCounts, resultId, type);
    }
  }

  const result = new Map<string, RecipeRole>();
  const allItems = new Set<string>([
    ...ingredientOf.keys(),
    ...outputOf.keys(),
  ]);
  for (const item of allItems) {
    const ing = [...(ingredientOf.get(item) ?? [])].sort();
    const out = [...(outputOf.get(item) ?? [])].sort();
    result.set(item, {
      ingredient_of: ing,
      output_of: out,
      in_degree: ing.length,
      out_degree: out.length,
      ingredient_of_counts: mapToObject(ingredientCounts.get(item)),
      output_of_counts: mapToObject(outputCounts.get(item)),
    });
  }
  return result;
}

/** Strip the `minecraft:` prefix off a recipe type (`minecraft:smelting` →
 *  `smelting`). Mod-authored types keep their namespace prefix. */
function shortRecipeType(type: string | undefined): string {
  if (!type) return "unknown";
  return type.startsWith("minecraft:") ? type.slice("minecraft:".length) : type;
}

function mapToObject(m: Map<string, number> | undefined): Record<string, number> {
  const out: Record<string, number> = {};
  if (!m) return out;
  for (const [k, v] of m) out[k] = v;
  return out;
}

const INGREDIENT_FIELDS = [
  "ingredients",
  "ingredient",
  "input",
  "material",
  "source",
  "addition",
  "base",
  "template",
  "target",
  "dye",
  "fuel",
  "shell",
  "star",
  "map",
  "banner",
] as const;

function collectIngredientItems(
  value: unknown,
  defaultNamespace: string,
  tagMembers: Map<string, string[]>,
): Set<string> {
  const out = new Set<string>();
  const visit = (v: unknown) => {
    if (v === undefined || v === null) return;
    if (typeof v === "string") {
      if (v.startsWith("#")) {
        const tagId = normalize(v.slice(1), defaultNamespace);
        for (const m of tagMembers.get(tagId) ?? []) out.add(m);
      } else {
        out.add(normalize(v, defaultNamespace));
      }
      return;
    }
    if (Array.isArray(v)) {
      for (const child of v) visit(child);
      return;
    }
    if (typeof v === "object") {
      const obj = v as Record<string, unknown>;
      if (typeof obj.item === "string") visit(obj.item);
      if (typeof obj.tag === "string") visit(`#${obj.tag}`);
      if (typeof obj.id === "string") visit(obj.id);
    }
  };
  visit(value);
  return out;
}

function extractResultId(
  result: RecipeJson["result"],
  defaultNamespace: string,
): string | null {
  if (!result) return null;
  if (typeof result === "string") return normalize(result, defaultNamespace);
  if (typeof result === "object" && typeof result.id === "string") {
    return normalize(result.id, defaultNamespace);
  }
  return null;
}

function normalize(id: string, defaultNamespace: string): string {
  return id.includes(":") ? id : `${defaultNamespace}:${id}`;
}
