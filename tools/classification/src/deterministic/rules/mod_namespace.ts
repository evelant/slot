import type { Rule } from "../types.ts";

export const modNamespaceRule: Rule = {
  id: "mod_namespace",
  facets: ["mod_namespace"],
  run({ record }) {
    return [
      {
        facet: "mod_namespace",
        kind: "single",
        value: record.namespace,
        source: "rule:mod_namespace",
        confidence: 1,
      },
    ];
  },
};
