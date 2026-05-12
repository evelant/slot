import { VOCABULARY_BACKED_FACETS } from "../schema/facets.ts";
import type { VocabularyFacetId } from "./types.ts";

export function facetSet(facets: readonly string[] | undefined): Set<VocabularyFacetId> {
  const out = new Set<VocabularyFacetId>();
  const requested = facets && facets.length > 0 ? facets : VOCABULARY_BACKED_FACETS;
  const unknown: string[] = [];
  for (const facet of requested) {
    if (isVocabularyFacet(facet)) out.add(facet);
    else unknown.push(facet);
  }
  if (unknown.length > 0) {
    throw new Error(
      `unknown vocabulary facet(s): ${unknown.join(", ")}; expected one of ${VOCABULARY_BACKED_FACETS.join(", ")}`,
    );
  }
  return out;
}

export function isVocabularyFacet(facet: string): facet is VocabularyFacetId {
  return (VOCABULARY_BACKED_FACETS as readonly string[]).includes(facet);
}
