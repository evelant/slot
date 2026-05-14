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
      activity: {
        values: {
          "slot:cooking": {
            label: "Cooking",
            origin: "universal_default",
            state: "accepted",
            confidence: 0.9,
          },
        },
      },
      workflow: {
        values: {
          "pack:fixture_pack/steelmaking": {
            label: "Steelmaking",
            aliases: ["steel chain"],
            origin: "pack_generated",
            state: "accepted",
            evidence: [{ kind: "recipe_type", id: "gtceu:alloy_smelter", confidence: 0.8 }],
            seed_items: ["gtceu:steel_ingot"],
            related_activity: ["slot:cooking"],
            default_organization_group: "pack:fixture_pack/steelmaking",
            confidence: 0.85,
          },
        },
      },
      workflow_role: {
        values: {
          "pack:fixture_pack/steelmaking#input": {
            label: "Steelmaking input",
            origin: "pack_generated",
            state: "accepted",
            parent: "pack:fixture_pack/steelmaking",
          },
        },
      },
      organization_group: {
        values: {
          "pack:fixture_pack/steelmaking": {
            label: "Steelmaking",
            origin: "pack_generated",
            state: "accepted",
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
          activity: { values: ["slot:cooking"] },
          workflow: { values: ["pack:fixture_pack/steelmaking"] },
          workflow_role: { values: ["pack:fixture_pack/steelmaking#input"] },
          organization_group: { values: ["pack:fixture_pack/steelmaking"] },
        },
      },
    },
  };
}

describe("vocabulary id grammar", () => {
  test("accepts universal, namespace, pack, and scoped workflow-role ids", () => {
    expect(isVocabularyValueId("slot:cooking")).toBe(true);
    expect(isVocabularyValueId("create:mechanical_power")).toBe(true);
    expect(isVocabularyValueId("pack:tfg2/steelmaking")).toBe(true);
    expect(isVocabularyValueId("pack:tfg2/food/prep")).toBe(true);
    expect(isScopedVocabularyValueId("pack:tfg2/steelmaking#catalyst")).toBe(true);
  });

  test("rejects display labels, hyphenated tokens, missing role delimiters, and unscoped ids", () => {
    expect(isVocabularyValueId("Food Prep")).toBe(false);
    expect(isVocabularyValueId("pack:tfg2/food-prep")).toBe(false);
    expect(isVocabularyValueId("cooking")).toBe(false);
    expect(isScopedVocabularyValueId("pack:tfg2/steelmaking/catalyst")).toBe(false);
  });
});

describe("layer facet validation", () => {
  test("accepts new vocabulary-backed facets with scoped ids", () => {
    expect(validateLayer(tinyLayer()).ok).toBe(true);
  });

  test("rejects unknown facets and malformed vocabulary ids", () => {
    const layer = tinyLayer();
    const facets = layer.entries["gtceu:steel_ingot"].facets as Record<string, unknown>;
    facets.workflow = { values: ["Steelmaking"] };
    facets.made_up = { value: "x" };
    const result = validateLayer(layer);
    expect(result.ok).toBe(false);
    expect(result.errors.some((error) => error.includes("unknown facet"))).toBe(true);
    expect(result.errors.some((error) => error.includes("fails pattern"))).toBe(true);
  });

  test("validates layer values against accepted vocabulary values", () => {
    const vocabulary = tinyVocabulary();
    const layer = tinyLayer();
    expect(validateLayer(layer, { vocabulary }).ok).toBe(true);

    const universalDefault = tinyLayer();
    universalDefault.entries["gtceu:steel_ingot"].facets.organization_group = {
      values: ["slot:metal_stock"],
    };
    expect(validateLayer(universalDefault, { vocabulary }).ok).toBe(true);

    const rejected = tinyLayer();
    rejected.entries["gtceu:steel_ingot"].facets.workflow = {
      values: ["pack:fixture_pack/review_only"],
    };
    const errors = validateLayerAgainstVocabulary(rejected, vocabulary);
    expect(errors).toHaveLength(1);
    expect(errors[0]!).toContain("not accepted by vocabulary");
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
    facets.workflow_role!.values["pack:fixture_pack/steelmaking#output"] = {
      label: "Wrong parent",
      origin: "pack_generated",
      state: "accepted",
      parent: "pack:fixture_pack/other",
    };
    const result = validateVocabularyArtifact(vocabulary);
    expect(result.ok).toBe(false);
    expect(result.errors.some((error) => error.includes("unknown facet"))).toBe(true);
    expect(result.errors.some((error) => error.includes("fails pattern"))).toBe(true);
    expect(result.errors.some((error) => error.includes("parent must equal"))).toBe(true);
  });
});
