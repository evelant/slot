import { existsSync, mkdirSync, readFileSync, statSync } from "node:fs";
import { join, resolve } from "node:path";
import { spawnSync } from "node:child_process";

/**
 * A git worktree of misode/mcmeta's `summary` branch, which consolidates every
 * registry (items, tags, recipes, loot tables, lang, models, item definitions,
 * item components) into single `data.min.json` files. Everything Stage 1 needs
 * for a vanilla run lives here.
 */
export interface VanillaSource {
  summaryRoot: string;
}

/**
 * Ensure a worktree for a mcmeta summary ref exists and return its path.
 * Defaults to the moving `origin/summary` branch, but callers may pass an
 * exact version tag such as `1.20.1-summary` for reproducible extraction.
 */
export function ensureVanillaSource(
  mcmetaRepoPath: string,
  options: { ref?: string } = {},
): VanillaSource {
  const repo = resolve(mcmetaRepoPath);
  if (!existsSync(join(repo, ".git"))) {
    throw new Error(
      `mcmeta source path is not a git repo: ${repo}. ` +
        `Clone https://github.com/misode/mcmeta.git there first.`,
    );
  }
  const worktreeRoot = join(repo, ".worktrees");
  mkdirSync(worktreeRoot, { recursive: true });
  const requestedRef = options.ref?.trim() || "summary";
  const gitRef = requestedRef === "summary" ? "origin/summary" : requestedRef;
  return {
    summaryRoot: ensureRefWorktree(
      repo,
      worktreeRoot,
      safeWorktreeName(requestedRef),
      gitRef,
    ),
  };
}

function ensureRefWorktree(
  repo: string,
  worktreeRoot: string,
  worktreeName: string,
  ref: string,
): string {
  const path = join(worktreeRoot, worktreeName);
  if (existsSync(path) && statSync(path).isDirectory()) return path;

  const result = spawnSync(
    "git",
    ["worktree", "add", "--detach", path, ref],
    { cwd: repo, encoding: "utf8" },
  );
  if (result.status !== 0) {
    throw new Error(
      `git worktree add failed for ${ref}:\n${result.stderr || result.stdout}`,
    );
  }
  return path;
}

function safeWorktreeName(ref: string): string {
  const safe = ref
    .replace(/^refs\//, "")
    .replace(/^origin\//, "")
    .replace(/[^A-Za-z0-9._-]+/g, "-")
    .replace(/^-+|-+$/g, "");
  return safe || "summary";
}

function readJson<T>(source: VanillaSource, subpath: string): T {
  const full = join(source.summaryRoot, subpath);
  return JSON.parse(readFileSync(full, "utf8")) as T;
}

function readJsonOrDefault<T>(source: VanillaSource, subpath: string, fallback: T): T {
  const full = join(source.summaryRoot, subpath);
  if (!existsSync(full)) return fallback;
  return JSON.parse(readFileSync(full, "utf8")) as T;
}

export interface SummaryBundle {
  /** Registry map: registry id -> list of member ids (short form, no namespace). */
  registries: Record<string, string[]>;
  /** Item id (short form) -> component map. */
  itemComponents: Record<string, Record<string, unknown>>;
  /** Recipe id (short form) -> recipe JSON. */
  recipes: Record<string, RecipeJson>;
  /** Loot-table id -> loot-table JSON. */
  lootTables: Record<string, LootTableJson>;
  /** Item-tag short id (no namespace) -> { values, replace? }. */
  itemTags: Record<string, TagJson>;
  /** Block-tag short id (no namespace) -> { values, replace? }. */
  blockTags: Record<string, TagJson>;
  /** Item id (short) -> item definition (contains model ref). */
  itemDefinitions: Record<string, ItemDefinitionJson>;
  /** Model id (e.g. `item/iron_ingot`) -> model JSON. */
  models: Record<string, ModelJson>;
  /** Lang code -> key -> translation. */
  lang: Record<string, Record<string, string>>;
  /** Block id (short) -> [state defs, default state]. */
  blocks: Record<string, unknown>;
  /** Minecraft version string (e.g. "1.21.10"). */
  version: string;
}

export interface RecipeJson {
  type: string;
  ingredients?: unknown;
  ingredient?: unknown;
  key?: Record<string, unknown>;
  pattern?: unknown;
  input?: unknown;
  material?: unknown;
  source?: unknown;
  addition?: unknown;
  base?: unknown;
  template?: unknown;
  target?: unknown;
  dye?: unknown;
  fuel?: unknown;
  shell?: unknown;
  star?: unknown;
  map?: unknown;
  banner?: unknown;
  shapes?: Record<string, unknown>;
  result?: { id?: string; count?: number } | string;
  [k: string]: unknown;
}

export interface LootTableJson {
  type?: string;
  pools?: LootPool[];
  [k: string]: unknown;
}

interface LootPool {
  entries?: LootEntry[];
  [k: string]: unknown;
}

interface LootEntry {
  type?: string;
  name?: string;
  value?: string;
  children?: LootEntry[];
  functions?: LootFunction[];
  [k: string]: unknown;
}

interface LootFunction {
  function?: string;
  add?: unknown;
  options?: unknown;
  [k: string]: unknown;
}

export interface TagJson {
  replace?: boolean;
  values: (string | { id?: string; required?: boolean })[];
}

export interface ItemDefinitionJson {
  /** Loosely typed — item-definition variant trees have a ton of shapes
   *  (`minecraft:model`, `minecraft:special`, `minecraft:composite`,
   *  `minecraft:range_dispatch`, …). `resolveModelParents` walks them
   *  structurally via runtime type checks, so a strong schema here buys
   *  little and fights the test fixtures. */
  model?: unknown;
}

export interface ModelJson {
  parent?: string;
  textures?: Record<string, string>;
  [k: string]: unknown;
}

/**
 * Read every consolidated file we need. Worktree reads are cheap (single JSON.parse
 * per file), so we front-load the whole summary at startup rather than streaming.
 */
export function loadSummaryBundle(source: VanillaSource): SummaryBundle {
  const registries = readJson<Record<string, string[]>>(
    source,
    "registries/data.min.json",
  );
  const itemComponents = readJsonOrDefault<Record<string, Record<string, unknown>>>(
    source,
    "item_components/data.min.json",
    {},
  );
  const recipes = readJson<Record<string, RecipeJson>>(
    source,
    "data/recipe/data.min.json",
  );
  const lootTables = readJson<Record<string, LootTableJson>>(
    source,
    "data/loot_table/data.min.json",
  );
  const itemTags = readJson<Record<string, TagJson>>(
    source,
    "data/tag/item/data.min.json",
  );
  const blockTags = readJson<Record<string, TagJson>>(
    source,
    "data/tag/block/data.min.json",
  );
  const itemDefinitions = readJsonOrDefault<Record<string, ItemDefinitionJson>>(
    source,
    "assets/item_definition/data.min.json",
    {},
  );
  const models = readJson<Record<string, ModelJson>>(
    source,
    "assets/model/data.min.json",
  );
  const lang = readJsonOrDefault<Record<string, Record<string, string>>>(
    source,
    "assets/lang/data.min.json",
    {},
  );
  const blocks = readJson<Record<string, unknown>>(
    source,
    "blocks/data.min.json",
  );
  const version = readFileSync(
    join(source.summaryRoot, "version.txt"),
    "utf8",
  ).trim();

  return {
    registries,
    itemComponents,
    recipes,
    lootTables,
    itemTags,
    blockTags,
    itemDefinitions,
    models,
    lang,
    blocks,
    version,
  };
}
