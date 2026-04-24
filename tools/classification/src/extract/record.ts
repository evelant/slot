/**
 * Stage 1 output — one record per item, written as NDJSON. Everything downstream
 * (stage 2 deterministic rules, stage 3 LLM prompts) is authored against this shape.
 *
 * The shape is mod-agnostic: vanilla and modded extractors produce the same record.
 * Extractor-specific signals go into `extractor_meta` rather than new top-level fields,
 * so the schema stays stable as new extractors come online.
 */
export interface ItemExtractRecord {
  /** Fully-qualified id, e.g. `minecraft:iron_ingot`. */
  id: string;
  /** Id namespace, e.g. `minecraft`. */
  namespace: string;
  /** Id path (everything after the colon), e.g. `iron_ingot`. */
  path: string;
  /** Human-readable name from en_us lang, or null if no translation exists. */
  display_name: string | null;
  /**
   * Full transitive closure of Minecraft item tags this item belongs to, as
   * fully-qualified tag ids without the leading `#`. Only tags that actually
   * contain the item (directly or via nested tag references) are included.
   */
  minecraft_tags: string[];
  /**
   * How this item participates in recipes. `ingredient_of` and `output_of`
   * are de-duplicated lists of recipe ids (fully-qualified).
   */
  recipe_role: RecipeRole;
  /**
   * The `item_definition` -> `model` chain walked from the item entry down
   * through parent chains in `assets/model/`. First entry is the item's
   * direct model; subsequent entries are its ancestors. The last ancestor's
   * parent is either undefined (root) or a builtin like `item/generated` or
   * `block/block`.
   */
  model_parents: string[];
  /** Loot tables that produce this item as a pool entry. Fully-qualified ids. */
  loot_table_sources: string[];
  /** Creative tabs that reference this item. Empty for vanilla (not data-driven). */
  creative_tabs: string[];
  /** Item-component map from mcmeta, e.g. keyed by `minecraft:max_stack_size`. */
  component_data: Record<string, unknown> | null;
  /**
   * Extractor-local debug hints / signals that don't fit the shared shape.
   * Included so per-extractor diagnostics can be written without growing
   * the schema.
   */
  extractor_meta?: Record<string, unknown>;
}

export interface RecipeRole {
  /** Recipe ids that consume this item (as a declared ingredient). */
  ingredient_of: string[];
  /** Recipe ids that produce this item as a result. */
  output_of: string[];
  /** Count of `ingredient_of`. */
  in_degree: number;
  /** Count of `output_of`. */
  out_degree: number;
}

export interface ExtractRunMeta {
  /** Extractor identifier, e.g. `vanilla` or `create`. */
  extractor: string;
  /** Source identifier — for vanilla this is the MC version (e.g. `1.21.10`). */
  source_version: string;
  /** ISO-8601 timestamp when the extract ran. */
  generated_at: string;
  /** Pipeline tool version; read from package.json. */
  generated_by: string;
}
