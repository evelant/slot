import { describe, expect, test } from "bun:test";
import { validateLayer } from "../src/schema/validate.ts";
import {
  isScopedVocabularyValueId,
  isVocabularyValueId,
} from "../src/schema/facets.ts";
import {
  validateLayerAgainstVocabulary,
  validateVocabularyArtifact,
  type PackFacetVocabulary,
} from "../src/schema/vocabulary.ts";

function tinyVocabulary(): PackFacetVocabulary {
  return {
    schema_version: 1,
    kind: "slot-pack-facet-vocabulary",
    pack_id: "fixture_pack",
    generated_by: "slot-classify v0.1.0",
    facets: {
      role: {
        values: {
          material: {
            label: "Material",
            origin: "pack_generated",
            state: "accepted",
          },
        },
      },
      activity: {
        values: {
          "cooking": {
            label: "Cooking",
            origin: "built_in",
            state: "accepted",
            confidence: 0.9,
          },
        },
      },
      workflow: {
        values: {
          "steelmaking": {
            label: "Steelmaking",
            aliases: ["steel chain"],
            origin: "pack_generated",
            state: "accepted",
            evidence: [{ kind: "recipe_type", id: "gtceu:alloy_smelter", confidence: 0.8 }],
            seed_items: ["gtceu:steel_ingot"],
            related_activity: ["cooking"],
            default_organization_group: "steelmaking",
            confidence: 0.85,
          },
        },
      },
      workflow_role: {
        values: {
          "steelmaking#input": {
            label: "Steelmaking input",
            origin: "pack_generated",
            state: "accepted",
            parent: "steelmaking",
          },
        },
      },
      organization_group: {
        values: {
          "steelmaking": {
            label: "Steelmaking",
            origin: "pack_generated",
            state: "accepted",
          },
        },
      },
      material_family: {
        values: {
          iron: {
            label: "Iron",
            origin: "pack_generated",
            state: "accepted",
          },
          brass: {
            label: "Brass",
            origin: "pack_generated",
            state: "review",
          },
          copper: {
            label: "Copper",
            origin: "pack_generated",
            state: "rejected",
          },
        },
      },
    },
  };
}

function tinyLayer() {
  return {
    schema_version: 1,
    layer: "modpack",
    source: "fixture_pack",
    entries: {
      "gtceu:steel_ingot": {
        facets: {
          role: { value: "material" },
          material_family: { value: "iron" },
          activity: { values: ["cooking"] },
          workflow: { values: ["steelmaking"] },
          workflow_role: { values: ["steelmaking#input"] },
          organization_group: { values: ["steelmaking"] },
        },
      },
    },
  };
}

describe("vocabulary id grammar", () => {
  test("accepts facet-scoped, real namespace, path, and scoped workflow-role ids", () => {
    expect(isVocabularyValueId("cooking")).toBe(true);
    expect(isVocabularyValueId("create:mechanical_power")).toBe(true);
    expect(isVocabularyValueId("steelmaking")).toBe(true);
    expect(isVocabularyValueId("food/prep")).toBe(true);
    expect(isScopedVocabularyValueId("steelmaking#catalyst")).toBe(true);
  });

  test("rejects display labels, artificial provenance scopes, hyphenated tokens, and missing role delimiters", () => {
    expect(isVocabularyValueId("Food Prep")).toBe(false);
    expect(isVocabularyValueId("slot:cooking")).toBe(false);
    expect(isVocabularyValueId("pack:tfg2/steelmaking")).toBe(false);
    expect(isVocabularyValueId("food-prep")).toBe(false);
    expect(isScopedVocabularyValueId("steelmaking/catalyst")).toBe(false);
  });
});

describe("layer facet validation", () => {
  test("accepts new vocabulary-backed facets with scoped ids", () => {
    expect(validateLayer(tinyLayer()).ok).toBe(true);
  });

  test("rejects unknown facets and malformed vocabulary values", () => {
    const layer = tinyLayer();
    const facets = layer.entries["gtceu:steel_ingot"].facets as Record<string, unknown>;
    facets.workflow = { values: ["Steelmaking"] };
    facets.made_up = { value: "x" };
    const result = validateLayer(layer);
    expect(result.ok).toBe(false);
    expect(result.errors.some((error) => error.includes("unknown facet"))).toBe(true);
    expect(result.errors.some((error) => error.includes("fails pattern"))).toBe(true);
  });

  test("validates layer values against usable vocabulary values", () => {
    const vocabulary = tinyVocabulary();
    const layer = tinyLayer();
    expect(validateLayer(layer, { vocabulary }).ok).toBe(true);

    const reviewValue = tinyLayer();
    reviewValue.entries["gtceu:steel_ingot"].facets.material_family = {
      value: "brass",
    };
    expect(validateLayer(reviewValue, { vocabulary }).ok).toBe(true);

    const missingBuiltIn = tinyLayer();
    missingBuiltIn.entries["gtceu:steel_ingot"].facets.organization_group = {
      values: ["metal_stock"],
    };
    expect(validateLayer(missingBuiltIn, { vocabulary }).ok).toBe(false);

    const rejected = tinyLayer();
    rejected.entries["gtceu:steel_ingot"].facets.material_family = {
      value: "copper",
    };
    const errors = validateLayerAgainstVocabulary(rejected, vocabulary);
    expect(errors).toHaveLength(1);
    expect(errors[0]!).toContain("not usable by vocabulary");

    const reviewMarked = tinyLayer();
    reviewMarked.entries["gtceu:steel_ingot"].facets.material_family = {
      value: "copper",
      vocab_review: true,
    } as any;
    expect(validateLayer(reviewMarked, { vocabulary }).ok).toBe(true);
  });
});

describe("pack facet vocabulary validation", () => {
  test("accepts a tiny vocabulary artifact", () => {
    const result = validateVocabularyArtifact(tinyVocabulary());
    expect(result.ok).toBe(true);
    expect(result.vocabulary?.pack_id).toBe("fixture_pack");
  });

  test("rejects malformed values, unknown facets, and bad workflow-role parents", () => {
    const vocabulary = tinyVocabulary() as unknown as Record<string, unknown>;
    const facets = vocabulary.facets as Record<string, { values: Record<string, unknown> }>;
    facets.workflow!.values["Bad Label"] = {
      label: "Bad Label",
      origin: "pack_generated",
      state: "accepted",
    };
    facets.not_a_facet = { values: {} };
    facets.workflow_role!.values["steelmaking#output"] = {
      label: "Wrong parent",
      origin: "pack_generated",
      state: "accepted",
      parent: "other",
    };
    const result = validateVocabularyArtifact(vocabulary);
    expect(result.ok).toBe(false);
    expect(result.errors.some((error) => error.includes("unknown facet"))).toBe(true);
    expect(result.errors.some((error) => error.includes("fails pattern"))).toBe(true);
    expect(result.errors.some((error) => error.includes("parent must equal"))).toBe(true);
  });
});
