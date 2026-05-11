import { createHash } from "node:crypto";
import { existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";

/**
 * An abstract LLM client. The live implementation calls OpenRouter; the replay
 * client reads recorded fixtures keyed by prompt hash; the recording wrapper
 * calls the live client and persists the result for later replay.
 *
 * Keeping this behind an interface means every test can inject a deterministic
 * replay without touching the CLI or network.
 */
export interface LlmClient {
  query(prompt: string, options: QueryOptions): Promise<string>;
  /**
   * Split-prompt variant: stable `system` content is sent separately from
   * volatile per-batch `user` content. Optional — clients that don't implement
   * it fall back to sending `system + "\\n" + user` as a single message.
   */
  querySplit?(system: string, user: string, options: QueryOptions): Promise<string>;
}

export interface QueryOptions {
  /** OpenRouter model id (`deepseek/deepseek-v4-flash`, `openai/gpt-4.1-mini`, …). */
  model: string;
  /** Maximum generated tokens to request from the provider when supported. */
  maxTokens?: number;
  /** Abort signal for long-running batches. */
  signal?: AbortSignal;
  /** Maximum call duration in ms. Default 120s. */
  timeoutMs?: number;
  /**
   * Optional content validator. Called inside the client's retry loop
   * after the upstream response unwraps to a non-empty string. When
   * the validator returns `{ ok: false }`, the client treats the
   * response as a transient failure and retries (subject to its own
   * backoff budget). Use this to recover from upstream truncations
   * that pass HTTP-level checks but produce unparseable content (e.g.
   * a connection drop mid-JSON or a token-limit cut-off).
   *
   * `ReplayLlmClient` ignores this field; `RecordingLlmClient` applies it
   * before accepting a cached response, and `OpenRouterClient` uses it inside
   * its retry loop.
   */
  responseValidator?: (content: string) => { ok: boolean; reason?: string };
}

/**
 * Deterministic fixture-based client used by tests and by `--use-replay` runs.
 *
 * Each prompt is hashed (sha256 → first 16 hex chars) and looked up as
 * `<dir>/<hash>.response.json`. On miss, the call throws — no network
 * fallback, so test runs can't accidentally burn tokens.
 *
 * For backwards compatibility with older fixture sets we also fall back to
 * `.response.txt` — the file body is the same; only the extension changed.
 */
export class ReplayLlmClient implements LlmClient {
  constructor(private readonly fixtureDir: string) {}

  async query(prompt: string): Promise<string> {
    return this.readFixture(prompt);
  }

  async querySplit(system: string, user: string): Promise<string> {
    // Split-mode fixtures are hashed on `system\n\n${user}` so the hash is
    // deterministic no matter which code path recorded them. See
    // `splitFixtureKey`.
    return this.readFixture(splitFixtureKey(system, user));
  }

  private readFixture(keyContent: string): string {
    const path = this.pathFor(keyContent);
    if (!existsSync(path)) {
      throw new Error(
        `no replay fixture for prompt hash ${fixtureHash(keyContent)} (looked at ${path}). ` +
          `Record one first with \`--record-replay\`.`,
      );
    }
    return readFileSync(path, "utf8");
  }

  pathFor(keyContent: string): string {
    const hash = fixtureHash(keyContent);
    const primary = join(this.fixtureDir, `${hash}.response.json`);
    if (existsSync(primary)) return primary;
    const legacy = join(this.fixtureDir, `${hash}.response.txt`);
    if (existsSync(legacy)) return legacy;
    return primary;
  }
}

/**
 * Recording wrapper with cache-then-call semantics. Before asking the inner
 * client, it checks for an existing fixture keyed by the prompt hash:
 *   - hit  → return the cached response, no LLM call (free resume)
 *   - miss → call inner, persist response to disk, return it
 *
 * Persisted files per call:
 *   - `<hash>.prompt.md`      — combined-mode prompt text
 *   - `<hash>.system.md`      — split-mode system content (split mode only)
 *   - `<hash>.user.md`        — split-mode user content  (split mode only)
 *   - `<hash>.response.json`  — raw response text, pretty-printed when JSON
 *   - `<hash>.parsed.json`    — extracted classification JSON, pretty-printed
 *
 * Because fixtures are keyed by prompt content (including the stable system
 * prompt in split mode), any prompt change invalidates all caches. That's
 * intentional — you never want yesterday's classification replayed under a
 * new prompt spec.
 */
export class RecordingLlmClient implements LlmClient {
  constructor(
    private readonly inner: LlmClient,
    private readonly fixtureDir: string,
    /** Optional callback fired on each cache hit/miss; used by the CLI
     *  to print progress ("batch 5/80 cached"). */
    private readonly onCache?: (event: CacheEvent) => void,
  ) {
    mkdirSync(fixtureDir, { recursive: true });
  }

  async query(prompt: string, options: QueryOptions): Promise<string> {
    const hash = fixtureHash(prompt);
    const cached = this.readCachedResponse(hash);
    if (cached !== null && this.cachedResponseIsValid(cached, options)) {
      this.onCache?.({ hit: true, hash, mode: "combined" });
      return cached;
    }
    this.onCache?.({ hit: false, hash, mode: "combined" });
    const response = await this.inner.query(prompt, options);
    this.persist(prompt, response);
    return response;
  }

  async querySplit(system: string, user: string, options: QueryOptions): Promise<string> {
    if (!this.inner.querySplit) {
      return this.query(`${system}\n\n${user}`, options);
    }
    const key = splitFixtureKey(system, user);
    const hash = fixtureHash(key);
    const cached = this.readCachedResponse(hash);
    if (cached !== null && this.cachedResponseIsValid(cached, options)) {
      this.onCache?.({ hit: true, hash, mode: "split" });
      return cached;
    }
    this.onCache?.({ hit: false, hash, mode: "split" });
    const response = await this.inner.querySplit(system, user, options);
    writeFileSync(join(this.fixtureDir, `${hash}.system.md`), system);
    writeFileSync(join(this.fixtureDir, `${hash}.user.md`), user);
    this.persistEnvelope(hash, response);
    return response;
  }

  /**
   * A cached response is only useful if downstream parsing accepts it.
   * Apply the caller-supplied {@link QueryOptions.responseValidator} (if
   * any) before returning the cache hit. When validation fails, fall
   * through to the live call — the new response will overwrite the
   * stale fixture. Without this, a fixture written from a truncated /
   * unparseable upstream response (which we recover from on the live
   * path via the same validator) would replay forever.
   */
  private cachedResponseIsValid(cached: string, options: QueryOptions): boolean {
    if (!options.responseValidator) return true;
    const verdict = options.responseValidator(cached);
    if (!verdict.ok) {
      console.warn(
        `[recording-cache] cached fixture rejected by validator (${verdict.reason ?? "unknown"}); falling through to live call`,
      );
    }
    return verdict.ok;
  }

  private readCachedResponse(hash: string): string | null {
    const primary = join(this.fixtureDir, `${hash}.response.json`);
    if (existsSync(primary)) return readFileSync(primary, "utf8");
    const legacy = join(this.fixtureDir, `${hash}.response.txt`);
    if (existsSync(legacy)) return readFileSync(legacy, "utf8");
    return null;
  }

  private persist(prompt: string, response: string): void {
    const hash = fixtureHash(prompt);
    writeFileSync(join(this.fixtureDir, `${hash}.prompt.md`), prompt);
    this.persistEnvelope(hash, response);
  }

  private persistEnvelope(hash: string, response: string): void {
    writeFileSync(
      join(this.fixtureDir, `${hash}.response.json`),
      formatEnvelope(response),
    );
    const parsed = extractParsedContent(response);
    if (parsed !== null) {
      writeFileSync(join(this.fixtureDir, `${hash}.parsed.json`), parsed);
    }
  }
}

export interface CacheEvent {
  hit: boolean;
  hash: string;
  mode: "combined" | "split";
}

/**
 * Canonical key used to hash a split prompt. Concatenates system and user
 * with a separator that's stable across recorders/replayers. Keep in sync
 * with ReplayLlmClient.querySplit.
 */
function splitFixtureKey(system: string, user: string): string {
  return `${system}\n\n---\n\n${user}`;
}

/**
 * Pretty-print JSON response text, preserving the structure but making it
 * human-readable. Falls back to the raw text if parsing fails.
 */
function formatEnvelope(raw: string): string {
  try {
    const obj = JSON.parse(raw.trim());
    return JSON.stringify(obj, null, 2) + "\n";
  } catch {
    return raw.endsWith("\n") ? raw : raw + "\n";
  }
}

/**
 * Extract the model's classification JSON from whatever wire shape the
 * upstream client returned, and pretty-print it. Two shapes are supported:
 *
 *   1. Legacy wrapped envelope: `{ result: "...inner JSON or fenced JSON..." }`.
 *      Pull `.result` out, strip a ```json fence if present, parse the inner.
 *   2. Raw classification JSON returned directly by the inner client (the
 *      OpenRouter path — `OpenRouterClient.send` already unwraps
 *      `message.content` for us so the recorder receives the raw model
 *      text, not an OpenRouter HTTP envelope). Recognized by the presence
 *      of any of `items` / `schema_proposals` / `corrections` at the top
 *      level — those are the canonical classification-output keys.
 *
 * Returns null when neither shape applies (the raw envelope is still
 * preserved in `response.json` regardless).
 */
function extractParsedContent(raw: string): string | null {
  let envelope: unknown;
  try {
    envelope = JSON.parse(raw.trim());
  } catch {
    // Raw text wasn't JSON — could be a fenced markdown response from a
    // model that didn't honor "strict JSON only". Try unwrapping the
    // fence as a last resort.
    const stripped = stripFence(raw.trim());
    try {
      const inner = JSON.parse(stripped);
      if (looksLikeClassificationOutput(inner)) {
        return JSON.stringify(inner, null, 2) + "\n";
      }
    } catch { /* fallthrough */ }
    return null;
  }
  if (!envelope || typeof envelope !== "object") return null;

  // Shape 1: legacy wrapped envelope.
  const result = (envelope as { result?: unknown }).result;
  if (typeof result === "string") {
    const stripped = stripFence(result.trim());
    try {
      const inner = JSON.parse(stripped);
      return JSON.stringify(inner, null, 2) + "\n";
    } catch {
      return null;
    }
  }

  // Shape 2: raw classification output (OpenRouter path).
  if (looksLikeClassificationOutput(envelope)) {
    return JSON.stringify(envelope, null, 2) + "\n";
  }
  return null;
}

function looksLikeClassificationOutput(value: unknown): boolean {
  if (!value || typeof value !== "object") return false;
  const obj = value as Record<string, unknown>;
  return "items" in obj || "schema_proposals" in obj || "corrections" in obj;
}

function stripFence(text: string): string {
  const fence = text.match(/^```(?:json)?\n([\s\S]*?)\n```$/);
  return fence?.[1] ?? text;
}

export function fixtureHash(prompt: string): string {
  return createHash("sha256").update(prompt).digest("hex").slice(0, 16);
}
