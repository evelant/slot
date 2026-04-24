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
    const timeout = options.timeoutMs ?? 120_000;
    const args = ["-p", "--model", options.model, "--output-format", "json"];

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
 * `<dir>/<hash>.response.txt`. On miss, the call throws — no network
 * fallback, so test runs can't accidentally burn tokens.
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
    return join(this.fixtureDir, `${fixtureHash(prompt)}.response.txt`);
  }
}

/**
 * Recording wrapper: calls an inner client, persists the request+response pair
 * alongside the fixtures, and returns the response to the caller. Use this when
 * you want to bootstrap a fixture set from a real run.
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
    writeFileSync(join(this.fixtureDir, `${hash}.prompt.txt`), prompt);
    writeFileSync(join(this.fixtureDir, `${hash}.response.txt`), response);
    return response;
  }
}

export function fixtureHash(prompt: string): string {
  return createHash("sha256").update(prompt).digest("hex").slice(0, 16);
}
