import { createHash } from "node:crypto";
import { existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { spawn } from "node:child_process";

/**
 * An abstract LLM client. The production impl wraps `claude -p`; the replay
 * impl reads recorded fixtures keyed by prompt hash; the record impl calls
 * the real backend and persists the result for later replay.
 *
 * Keeping this behind an interface means every test can inject a deterministic
 * replay without touching the CLI or network.
 */
export interface LlmClient {
  query(prompt: string, options: QueryOptions): Promise<string>;
  /**
   * Split-prompt variant: the stable `system` content replaces Claude Code's
   * default system prompt; the `user` content is sent on stdin as the user
   * message. Optional — clients that don't implement it fall back to sending
   * `system + "\\n" + user` on stdin as a single message.
   */
  querySplit?(system: string, user: string, options: QueryOptions): Promise<string>;
}

export interface QueryOptions {
  /** Claude model id (`claude-haiku-4-5`, `claude-sonnet-4-6`, …). */
  model: string;
  /**
   * Reasoning effort level — `low` | `medium` | `high` | `xhigh` | `max`.
   * Mapped to `claude --effort`. Sonnet/Opus respect this for extended
   * thinking budget; Haiku's behaviour is undocumented (likely a no-op
   * outside of `xhigh`/`max` since `high` is its default).
   */
  effort?: "low" | "medium" | "high" | "xhigh" | "max";
  /**
   * Explicit extended-thinking budget in tokens, set via the
   * `MAX_THINKING_TOKENS` env var on the spawned `claude` process.
   * Use this when you want a guaranteed thinking budget regardless of
   * the model's default `--effort` mapping. Set to 0 to disable thinking.
   */
  thinkingBudget?: number;
  /**
   * If true, sets `CLAUDE_CODE_DISABLE_ADAPTIVE_THINKING=1` so the
   * `MAX_THINKING_TOKENS` budget is honored on adaptive-reasoning models
   * (otherwise the budget is ignored when adaptive reasoning is on).
   */
  disableAdaptiveThinking?: boolean;
  /** Abort signal for long-running batches. */
  signal?: AbortSignal;
  /** Override the `claude` executable path (defaults to "claude" on PATH). */
  claudeBinary?: string;
  /** Maximum call duration in ms. Default 120s. */
  timeoutMs?: number;
}

/** Shell out to `claude -p --model <m> --output-format json` with the prompt on stdin. */
export class ClaudeCliClient implements LlmClient {
  async query(prompt: string, options: QueryOptions): Promise<string> {
    return this.run({ args: this.baseArgs(options), env: this.buildEnv(options), stdin: prompt, options });
  }

  async querySplit(system: string, user: string, options: QueryOptions): Promise<string> {
    const args = [...this.baseArgs(options), "--system-prompt", system];
    return this.run({ args, env: this.buildEnv(options), stdin: user, options });
  }

  private baseArgs(options: QueryOptions): string[] {
    // --tools "" disables tool use (prompt is pure classification, no tools
    // needed) and --dangerously-skip-permissions skips the workspace trust
    // dialog since this is a read-only text-in / text-out call.
    const args = [
      "-p",
      "--model", options.model,
      "--output-format", "json",
      "--tools", "",
      "--dangerously-skip-permissions",
    ];
    if (options.effort) args.push("--effort", options.effort);
    return args;
  }

  private buildEnv(options: QueryOptions): Record<string, string> {
    // Forward env vars that control extended thinking. We don't replace
    // process.env wholesale — claude needs PATH, HOME, etc. — just layer
    // ours on top.
    const env: Record<string, string> = { ...process.env } as Record<string, string>;
    if (options.thinkingBudget !== undefined) {
      env.MAX_THINKING_TOKENS = String(options.thinkingBudget);
    }
    if (options.disableAdaptiveThinking) {
      env.CLAUDE_CODE_DISABLE_ADAPTIVE_THINKING = "1";
    }
    return env;
  }

  private async run(opts: {
    args: string[];
    env: Record<string, string>;
    stdin: string;
    options: QueryOptions;
  }): Promise<string> {
    const bin = opts.options.claudeBinary ?? "claude";
    // 30 min default — Haiku batches of 20 items finish in 2–3 min but
    // Sonnet 4.6 routinely takes 7+ min on the first batch (cache warm-up
    // + verbose output). Subsequent batches amortize faster but we still
    // want headroom.
    const timeout = opts.options.timeoutMs ?? 1_800_000;

    return new Promise((resolve, reject) => {
      const child = spawn(bin, opts.args, {
        stdio: ["pipe", "pipe", "pipe"],
        signal: opts.options.signal,
        env: opts.env,
      });
      const timer = setTimeout(() => child.kill("SIGKILL"), timeout);
      let stdout = "";
      let stderr = "";
      child.stdout.on("data", (chunk) => {
        stdout += chunk.toString("utf8");
      });
      child.stderr.on("data", (chunk) => {
        stderr += chunk.toString("utf8");
      });
      child.on("error", (err) => {
        clearTimeout(timer);
        reject(err);
      });
      child.on("close", (code) => {
        clearTimeout(timer);
        if (code !== 0) {
          reject(new Error(`claude -p exit ${code}: ${stderr || stdout}`));
          return;
        }
        resolve(stdout);
      });
      child.stdin.end(opts.stdin);
    });
  }
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
 *   - `<hash>.response.json`  — the claude -p envelope, pretty-printed
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
    if (cached !== null) {
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
    if (cached !== null) {
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
 * Pretty-print the claude -p envelope JSON, preserving the structure but
 * making it human-readable. Falls back to the raw text if parsing fails.
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
 * Extract the model's actual output from the envelope, strip the ```json fence
 * if present, re-parse, and pretty-print. Returns null when the response
 * doesn't fit the expected shape (the envelope is still preserved separately).
 */
function extractParsedContent(raw: string): string | null {
  let envelope: unknown;
  try {
    envelope = JSON.parse(raw.trim());
  } catch {
    return null;
  }
  if (!envelope || typeof envelope !== "object") return null;
  const result = (envelope as { result?: unknown }).result;
  if (typeof result !== "string") return null;
  const stripped = stripFence(result.trim());
  try {
    const inner = JSON.parse(stripped);
    return JSON.stringify(inner, null, 2) + "\n";
  } catch {
    return null;
  }
}

function stripFence(text: string): string {
  const fence = text.match(/^```(?:json)?\n([\s\S]*?)\n```$/);
  return fence?.[1] ?? text;
}

export function fixtureHash(prompt: string): string {
  return createHash("sha256").update(prompt).digest("hex").slice(0, 16);
}
