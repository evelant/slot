import { existsSync, readFileSync } from "node:fs";
import { join } from "node:path";
import type { ModSourceBundle } from "../extract/mod/source.ts";
import type { LlmClient, QueryOptions } from "./client.ts";

/**
 * Lightweight bag of human-readable mod metadata that the subsystem proposer
 * feeds to the LLM. We strip nothing — the model can parse markdown — but we
 * trim long readmes to keep the prompt bounded.
 */
export interface ModMetadata {
  modNamespace: string;
  /** Display name from mods.toml `displayName=` (e.g. "Create Crafts & Additions"). */
  displayName: string | null;
  /** Free-text mod description, usually a paragraph from mods.toml. */
  description: string | null;
  /** Truncated README body. Empty string when no README is found. */
  readme: string;
  /** Distinct recipe types declared by this mod — strong subsystem hint. */
  modRecipeTypes: string[];
  /** Sample of distinct item display names — used as conceptual hints. */
  itemDisplayNames: string[];
}

export interface ProposeSubsystemsOptions {
  client: LlmClient;
  model?: string;
  /** Forwarded to the LLM client. */
  clientOptions?: Partial<QueryOptions>;
}

/** What the proposer returns. The vocabulary list is what gets injected
 *  into stage 3; rationales are kept for transparency. */
export interface SubsystemVocabulary {
  modNamespace: string;
  vocabulary: SubsystemEntry[];
  /** Raw model response, kept verbatim for debugging. */
  raw: string;
}

export interface SubsystemEntry {
  /** Fully-qualified id (`<modnamespace>:<token>`). */
  id: string;
  /** ≤80 char description of which items this label covers. */
  rationale: string;
}

const README_MAX_CHARS = 8_000;
const ITEM_NAME_SAMPLE = 60;

export function extractModMetadata(args: {
  modPath: string;
  bundle: ModSourceBundle;
}): ModMetadata {
  const { modPath, bundle } = args;
  const modNs = bundle.modNamespace;

  const readme = readReadme(modPath);
  const { displayName, description } = readModsToml(modPath, modNs);

  const modRecipeTypes = collectModRecipeTypes(bundle);
  const itemDisplayNames = collectDisplayNames(bundle);

  return {
    modNamespace: modNs,
    displayName,
    description,
    readme,
    modRecipeTypes,
    itemDisplayNames,
  };
}

function readReadme(modPath: string): string {
  for (const name of ["README.md", "README.MD", "readme.md", "Readme.md"]) {
    const path = join(modPath, name);
    if (existsSync(path)) {
      const txt = readFileSync(path, "utf8");
      if (txt.length <= README_MAX_CHARS) return txt;
      return txt.slice(0, README_MAX_CHARS) + "\n…[truncated]";
    }
  }
  return "";
}

function readModsToml(
  modPath: string,
  modNs: string,
): { displayName: string | null; description: string | null } {
  const candidates = [
    join(modPath, "src", "main", "resources", "META-INF", "neoforge.mods.toml"),
    join(modPath, "src", "main", "resources", "META-INF", "mods.toml"),
    join(modPath, "src", "main", "templates", "META-INF", "neoforge.mods.toml"),
    join(modPath, "src", "main", "templates", "META-INF", "mods.toml"),
    // AE2 keeps it directly under src/main/ (no META-INF subdir)
    join(modPath, "src", "main", "neoforge.mods.toml"),
    join(modPath, "src", "main", "mods.toml"),
  ];
  let displayName: string | null = null;
  let description: string | null = null;
  for (const path of candidates) {
    if (!existsSync(path)) continue;
    const txt = readFileSync(path, "utf8");
    displayName = matchToml(txt, "displayName");
    description = matchTomlBlock(txt, "description");
    break;
  }
  // SophisticatedStorage canary: many neoforge mods write `displayName="${mod_name}"`
  // and substitute via gradle build. The source-tree we read hasn't been
  // through gradle, so we resolve gradle template references manually from
  // gradle.properties when present.
  const gradleProps = readGradleProperties(modPath);
  displayName = resolveTemplate(displayName, gradleProps);
  description = resolveTemplate(description, gradleProps);
  // If the toml is silent but gradle.properties has a `mod_name`/`mod_description`
  // (matching the SophisticatedStorage convention), use those directly.
  if (!displayName && gradleProps.mod_name) displayName = gradleProps.mod_name;
  if (!description && gradleProps.mod_description) description = gradleProps.mod_description;
  void modNs;
  return { displayName, description };
}

/**
 * Parse a mod's `gradle.properties` into a flat key→value map. Used to
 * resolve `${mod_*}` placeholders in mods.toml templates. Returns an empty
 * object when the file is absent, so callers can treat it as best-effort.
 */
function readGradleProperties(modPath: string): Record<string, string> {
  const path = join(modPath, "gradle.properties");
  if (!existsSync(path)) return {};
  const out: Record<string, string> = {};
  const txt = readFileSync(path, "utf8");
  for (const line of txt.split(/\r?\n/)) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) continue;
    const eq = trimmed.indexOf("=");
    if (eq < 0) continue;
    const k = trimmed.slice(0, eq).trim();
    const v = trimmed.slice(eq + 1).trim();
    if (k) out[k] = v;
  }
  return out;
}

/**
 * Substitute `${name}` references in `value` against `props`. Leaves
 * unresolved references in place — best-effort.
 */
function resolveTemplate(
  value: string | null,
  props: Record<string, string>,
): string | null {
  if (!value) return value;
  return value.replace(/\$\{([a-zA-Z0-9_]+)\}/g, (full, key) => props[key] ?? full);
}

function matchToml(txt: string, key: string): string | null {
  const m = txt.match(new RegExp(`${key}\\s*=\\s*"([^"]+)"`));
  return m ? m[1]! : null;
}

function matchTomlBlock(txt: string, key: string): string | null {
  // TOML triple-single-quoted multi-line literal.
  const m = txt.match(new RegExp(`${key}\\s*=\\s*'''([\\s\\S]*?)'''`));
  if (m) return m[1]!.trim();
  return matchToml(txt, key);
}

function collectModRecipeTypes(bundle: ModSourceBundle): string[] {
  const seen = new Set<string>();
  for (const recipe of Object.values(bundle.recipes)) {
    const t = (recipe as { type?: string }).type;
    if (typeof t === "string" && t.startsWith(`${bundle.modNamespace}:`)) {
      seen.add(t);
    }
  }
  return [...seen].sort();
}

function collectDisplayNames(bundle: ModSourceBundle): string[] {
  const lang = bundle.lang.en_us ?? {};
  const modNs = bundle.modNamespace;
  const out: string[] = [];
  const itemKeyRe = new RegExp(`^(item|block)\\.${escapeRegex(modNs)}\\.([a-z0-9_/]+)$`);
  for (const [key, value] of Object.entries(lang)) {
    if (typeof value !== "string") continue;
    if (itemKeyRe.test(key)) out.push(value);
  }
  out.sort();
  if (out.length <= ITEM_NAME_SAMPLE) return out;
  // Sample evenly so we get coverage rather than an alphabetic prefix.
  const step = out.length / ITEM_NAME_SAMPLE;
  const sampled: string[] = [];
  for (let i = 0; i < ITEM_NAME_SAMPLE; i++) {
    sampled.push(out[Math.floor(i * step)]!);
  }
  return sampled;
}

function escapeRegex(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

const PROPOSER_SYSTEM = `You are designing a small canonical vocabulary for the \`mod_subsystem\` facet for one Minecraft mod.

\`mod_subsystem\` labels which gameplay subsystem inside the mod an item belongs to. Each value is \`<modnamespace>:<token>\` where token is lowercase snake_case. Items will pick zero or more of these labels in a later classification pass — your job is to pick a stable set up front so picks stay consistent across items.

Output strict JSON of this shape (no markdown fences, no commentary):
{
  "vocabulary": [
    {"id": "<modnamespace>:<token>", "rationale": "≤80 chars: which kinds of items this label covers"},
    ...
  ]
}

Rules:
- Pick **3 to 8** entries — only the most distinctive subsystems the mod actually adds.
- Subsystems must be **orthogonal**: an item should fit naturally into at most one (or none). Don't propose synonyms (e.g. \`electricity\` AND \`power\` AND \`energy\`).
- Prefer **functional / mechanical** groupings (\`electricity\`, \`fluid_transport\`, \`autocrafting\`, \`mob_farming\`, \`fission\`) over thematic ones (\`iron_age\`, \`fancy_blocks\`).
- Each \`id\` MUST start with the mod's namespace (given in the user message) followed by a colon and a snake_case token, e.g. \`createaddition:electricity\`.
- DO NOT propose generic catch-alls like \`<ns>:crafting_ingredient\`, \`<ns>:general_utility\`, \`<ns>:misc\`, \`<ns>:items\` — items that don't fit any concrete subsystem should simply receive no \`mod_subsystem\` label.
- Rationales: ≤80 chars, terse. No marketing language.
- Respond with the JSON object only. Start with \`{\` and end with \`}\`.`;

/**
 * Build the per-mod user message: hand the model the README + description +
 * recipe types + a sampling of item names. The model picks subsystems from
 * that ground truth.
 */
function buildProposerUser(meta: ModMetadata): string {
  const parts: string[] = [];
  parts.push(`Mod namespace: ${meta.modNamespace}`);
  if (meta.displayName) parts.push(`Display name: ${meta.displayName}`);
  if (meta.description) {
    parts.push("");
    parts.push("Description (from mods.toml):");
    parts.push(meta.description);
  }
  if (meta.modRecipeTypes.length > 0) {
    parts.push("");
    parts.push(`Mod-namespace recipe types (${meta.modRecipeTypes.length}):`);
    for (const t of meta.modRecipeTypes) parts.push(`  - ${t}`);
  }
  if (meta.itemDisplayNames.length > 0) {
    parts.push("");
    parts.push(`Sample of item display names (${meta.itemDisplayNames.length}):`);
    for (const n of meta.itemDisplayNames) parts.push(`  - ${n}`);
  }
  if (meta.readme.length > 0) {
    parts.push("");
    parts.push("README:");
    parts.push(meta.readme);
  }
  parts.push("");
  parts.push(
    `Propose 3-8 \`mod_subsystem\` values for ${meta.modNamespace}. Respond with the JSON object only.`,
  );
  return parts.join("\n");
}

// Production default — same as the main stage-3 path. The proposer
// is a small README-summarization call; deepseek-v4-flash handles it
// quickly and cheaply.
const DEFAULT_MODEL = "deepseek/deepseek-v4-flash";

/**
 * Ask the LLM for a canonical mod_subsystem vocabulary based on the mod's
 * documentation. Returns at most 8 entries; the caller passes the resulting
 * id list to stage 3 as a soft suggestion.
 *
 * The call uses the same `LlmClient` abstraction stage 3 uses — wrap it in
 * `RecordingLlmClient` upstream and the proposal is cached/replayable on
 * the same fixture-key scheme as everything else.
 */
export async function proposeSubsystems(
  metadata: ModMetadata,
  options: ProposeSubsystemsOptions,
): Promise<SubsystemVocabulary> {
  const model = options.model ?? DEFAULT_MODEL;
  const user = buildProposerUser(metadata);
  const queryOptions: QueryOptions = { model, ...options.clientOptions };

  let raw: string;
  if (options.client.querySplit) {
    raw = await options.client.querySplit(PROPOSER_SYSTEM, user, queryOptions);
  } else {
    raw = await options.client.query(`${PROPOSER_SYSTEM}\n\n${user}`, queryOptions);
  }

  const vocabulary = parseProposerResponse(raw, metadata.modNamespace);
  return { modNamespace: metadata.modNamespace, vocabulary, raw };
}

const SUBSYSTEM_TOKEN_RE = /^[a-z0-9_]+$/;

/**
 * Parse the proposer's JSON response. Extracts the first {...} block in case
 * the model wrapped it in fences or prose, then validates each entry against
 * the namespace constraint and token shape. Invalid entries are dropped.
 */
export function parseProposerResponse(
  raw: string,
  modNamespace: string,
): SubsystemEntry[] {
  const inner = unwrapEnvelope(raw);
  const json = firstJsonObject(inner);
  if (!json) return [];

  let parsed: unknown;
  try {
    parsed = JSON.parse(json);
  } catch {
    return [];
  }
  if (!parsed || typeof parsed !== "object") return [];
  const vocab = (parsed as { vocabulary?: unknown }).vocabulary;
  if (!Array.isArray(vocab)) return [];

  const seen = new Set<string>();
  const out: SubsystemEntry[] = [];
  const expectedPrefix = `${modNamespace}:`;
  for (const entry of vocab) {
    if (!entry || typeof entry !== "object") continue;
    const id = (entry as { id?: unknown }).id;
    const rationale = (entry as { rationale?: unknown }).rationale;
    if (typeof id !== "string") continue;
    if (!id.startsWith(expectedPrefix)) continue;
    const token = id.slice(expectedPrefix.length);
    if (!SUBSYSTEM_TOKEN_RE.test(token)) continue;
    if (seen.has(id)) continue;
    seen.add(id);
    out.push({
      id,
      rationale:
        typeof rationale === "string" ? rationale.slice(0, 160) : "",
    });
    if (out.length >= 8) break;
  }
  return out;
}

/**
 * Unwrap the claude -p envelope so we get the model's text. Falls back to the
 * raw input when the envelope shape isn't recognized.
 */
function unwrapEnvelope(raw: string): string {
  const trimmed = raw.trim();
  if (!trimmed.startsWith("{")) return trimmed;
  try {
    const obj = JSON.parse(trimmed);
    if (obj && typeof obj === "object" && typeof (obj as { result?: unknown }).result === "string") {
      return (obj as { result: string }).result;
    }
  } catch {
    // Not a JSON envelope — caller already parsed the inner JSON.
  }
  return trimmed;
}

/**
 * Find the first balanced {...} block in `text`. Tolerates code fences,
 * leading prose, and trailing notes — same robustness pattern as
 * `parse.ts`'s firstJsonObject.
 */
function firstJsonObject(text: string): string | null {
  const fenced = text.match(/```(?:json)?\s*([\s\S]*?)```/);
  const body = fenced?.[1] ?? text;
  const start = body.indexOf("{");
  if (start < 0) return null;
  let depth = 0;
  let inString = false;
  let escape = false;
  for (let i = start; i < body.length; i++) {
    const ch = body[i]!;
    if (escape) {
      escape = false;
      continue;
    }
    if (inString) {
      if (ch === "\\") escape = true;
      else if (ch === '"') inString = false;
      continue;
    }
    if (ch === '"') {
      inString = true;
      continue;
    }
    if (ch === "{") depth++;
    else if (ch === "}") {
      depth--;
      if (depth === 0) return body.slice(start, i + 1);
    }
  }
  return null;
}
