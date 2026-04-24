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
}

export interface QueryOptions {
  /** Claude model id (`claude-haiku-4-5`, `claude-sonnet-4-6`, …). */
  model: string;
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
    const bin = options.claudeBinary ?? "claude";
    // 10 min default — Haiku batches of 20 items can take 2–3 minutes to
    // emit a long JSON response; Sonnet retries on ambiguous items are slower.
    const timeout = options.timeoutMs ?? 600_000;
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

    return new Promise((resolve, reject) => {
      const child = spawn(bin, args, {
        stdio: ["pipe", "pipe", "pipe"],
        signal: options.signal,
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
      child.stdin.end(prompt);
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
    const path = this.pathFor(prompt);
    if (!existsSync(path)) {
      throw new Error(
        `no replay fixture for prompt hash ${fixtureHash(prompt)} (looked at ${path}). ` +
          `Record one first with \`--record-replay\`.`,
      );
    }
    return readFileSync(path, "utf8");
  }

  pathFor(prompt: string): string {
    const hash = fixtureHash(prompt);
    const primary = join(this.fixtureDir, `${hash}.response.json`);
    if (existsSync(primary)) return primary;
    const legacy = join(this.fixtureDir, `${hash}.response.txt`);
    if (existsSync(legacy)) return legacy;
    return primary;
  }
}

/**
 * Recording wrapper: calls an inner client, persists the request+response pair
 * alongside the fixtures, and returns the response to the caller. Use this when
 * you want to bootstrap a fixture set from a real run.
 *
 * Three files are written per call:
 *   - `<hash>.prompt.md`      — the prompt as sent (markdown-flavored text).
 *   - `<hash>.response.json`  — the claude -p envelope, pretty-printed for review.
 *   - `<hash>.parsed.json`    — the model's actual classification output, extracted
 *                                from the envelope and pretty-printed. This is the
 *                                file a human typically wants to read.
 */
export class RecordingLlmClient implements LlmClient {
  constructor(
    private readonly inner: LlmClient,
    private readonly fixtureDir: string,
  ) {
    mkdirSync(fixtureDir, { recursive: true });
  }

  async query(prompt: string, options: QueryOptions): Promise<string> {
    const response = await this.inner.query(prompt, options);
    const hash = fixtureHash(prompt);
    writeFileSync(join(this.fixtureDir, `${hash}.prompt.md`), prompt);
    writeFileSync(
      join(this.fixtureDir, `${hash}.response.json`),
      formatEnvelope(response),
    );
    const parsed = extractParsedContent(response);
    if (parsed !== null) {
      writeFileSync(join(this.fixtureDir, `${hash}.parsed.json`), parsed);
    }
    return response;
  }
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
