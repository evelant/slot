import { OpenRouter } from "@openrouter/sdk";
import type { LlmClient, QueryOptions } from "./client.ts";

/**
 * OpenRouter-backed {@link LlmClient}. Lets us run stage 3 against any
 * model in the OpenRouter catalog (deepseek/*, openai/*, mistralai/*,
 * google/*, …) instead of going through `claude -p`. Keeps the same
 * input/output contract as {@link ClaudeCliClient} so the rest of the
 * pipeline (record/replay, parse, retry) doesn't change.
 *
 * Auth: reads the API key from `process.env.OPENROUTER_API_KEY`. The
 * caller can override via `OpenRouterClient.fromApiKey(...)`.
 *
 * Claude-specific {@link QueryOptions} fields (`effort`,
 * `thinkingBudget`, `disableAdaptiveThinking`, `claudeBinary`) are
 * silently ignored — they don't translate to OpenRouter's API surface.
 * The `model` field IS used and is the OpenRouter model slug
 * (e.g. `deepseek/deepseek-v4-flash`, `openai/gpt-4o-mini`).
 */
export class OpenRouterClient implements LlmClient {
  private readonly client: OpenRouter;
  private readonly ignoredProviders: readonly string[];
  private readonly onlyProviders: readonly string[];

  constructor(opts: {
    apiKey?: string;
    appTitle?: string;
    httpReferer?: string;
    /**
     * Provider slugs to exclude from routing (e.g. `["deepinfra"]`).
     * Forwarded as `provider.ignore` on every chat request. Useful
     * when an upstream provider is rate-limited or returns flaky
     * responses; OpenRouter routes around them.
     *
     * Falls back to comma-separated `OPENROUTER_IGNORE_PROVIDERS` env
     * var when unspecified. Ignored if {@link onlyProviders} is set.
     */
    ignoredProviders?: readonly string[];
    /**
     * Provider slugs to **pin** the request to (e.g. `["deepseek"]`).
     * Forwarded as `provider.only` + `allow_fallbacks: false` so
     * OpenRouter never routes anywhere else. Useful when one provider
     * has a price / caching / throughput / data-policy property the
     * caller cares about. Takes precedence over ignoredProviders.
     *
     * Falls back to comma-separated `OPENROUTER_ONLY_PROVIDERS` env
     * var when unspecified.
     */
    onlyProviders?: readonly string[];
  } = {}) {
    const apiKey = opts.apiKey ?? process.env.OPENROUTER_API_KEY;
    if (!apiKey) {
      throw new Error(
        "OpenRouterClient requires an API key — set OPENROUTER_API_KEY in env or pass apiKey explicitly.",
      );
    }
    this.client = new OpenRouter({
      apiKey,
      // App identifiers show up in OpenRouter's dashboard for the
      // account; harmless to set so traffic is attributable.
      appTitle: opts.appTitle ?? "slot-classify",
      httpReferer: opts.httpReferer ?? "https://github.com/imagio/slot",
    });
    this.ignoredProviders =
      opts.ignoredProviders
      ?? splitCsvEnv(process.env.OPENROUTER_IGNORE_PROVIDERS);
    this.onlyProviders =
      opts.onlyProviders
      ?? splitCsvEnv(process.env.OPENROUTER_ONLY_PROVIDERS);
  }

  static fromApiKey(apiKey: string): OpenRouterClient {
    return new OpenRouterClient({ apiKey });
  }

  async query(prompt: string, options: QueryOptions): Promise<string> {
    return this.send({ messages: [{ role: "user", content: prompt }], options });
  }

  async querySplit(system: string, user: string, options: QueryOptions): Promise<string> {
    return this.send({
      messages: [
        { role: "system", content: system },
        { role: "user", content: user },
      ],
      options,
    });
  }

  private async send(args: {
    messages: Array<{ role: "system" | "user"; content: string }>;
    options: QueryOptions;
  }): Promise<string> {
    const { messages, options } = args;
    const requestOptions = options.timeoutMs ? { timeoutMs: options.timeoutMs } : undefined;
    // `only` pins the request to a specific provider and disables
    // fallbacks — useful for price/caching/throughput/data-policy
    // reasons. Falls back to `ignore` when only-list is empty so the
    // caller can still blacklist flaky providers.
    const provider = this.onlyProviders.length > 0
      ? { only: [...this.onlyProviders], allowFallbacks: false }
      : this.ignoredProviders.length > 0
        ? { ignore: [...this.ignoredProviders] }
        : undefined;
    return this.sendWithRetry(
      {
        chatRequest: {
          model: options.model,
          messages,
          stream: false,
          ...(provider ? { provider } : {}),
        },
      },
      requestOptions,
      options.responseValidator,
    );
  }

  /**
   * Retry on transient upstream errors. Five categories qualify:
   *
   *   1. HTTP 429 (rate limit) — OpenRouterError exposes statusCode; we
   *      honor a Retry-After header when present.
   *   2. HTTP 5xx (server error) — same path.
   *   3. Network transport resets/timeouts — long prompt uploads can trip a
   *      provider or edge socket even when the request is otherwise valid.
   *   4. Empty / malformed response body — the SDK throws a SyntaxError
   *      from `JSON.parse(body)` inside `matchFunc` when the upstream
   *      returns an empty body or a partial response. We've observed
   *      this on deepseek under load even with the provider pinned;
   *      it's transient and a simple retry recovers.
   *   5. 200 OK with empty / null `message.content` — the upstream
   *      returned a syntactically-valid envelope but no actual text. We
   *      hit this on sophisticatedstorage after a string of 429s; it's
   *      transient at the upstream-provider layer and a retry recovers.
   *   6. Truncated / unparseable content — when a `responseValidator`
   *      is supplied, we run it on the unwrapped text and treat
   *      `{ ok: false }` as transient. We hit this on the create
   *      modpack run: 3 batches' content cut off mid-JSON
   *      (connection-drop or token-limit) and downstream parsing
   *      threw, dropping 60 items silently. The validator lets the
   *      caller (stage 3) hand us a `parseLlmResponse`-shaped check so
   *      we re-ask while we still have the prompt-cache hot.
   *
   * Uses exponential backoff capped at ~60s with up to 5 attempts.
   * Returns the unwrapped content string so the caller doesn't need to
   * re-validate the response shape outside the retry loop.
   */
  private async sendWithRetry(
    request: Parameters<OpenRouter["chat"]["send"]>[0],
    options: Parameters<OpenRouter["chat"]["send"]>[1],
    responseValidator?: (content: string) => { ok: boolean; reason?: string },
  ): Promise<string> {
    const maxAttempts = 5;
    let attempt = 0;
    let lastErr: unknown;
    while (attempt < maxAttempts) {
      try {
        const result = await this.client.chat.send(request, options);
        if (!("choices" in result) || !Array.isArray(result.choices)) {
          throw new EmptyContentError(
            "OpenRouter returned an unexpected shape (no `choices` array). " +
              "This usually means the request was streamed inadvertently; " +
              "OpenRouterClient.send always sets stream=false.",
          );
        }
        const choice = result.choices[0];
        if (!choice) {
          throw new EmptyContentError("OpenRouter returned an empty choices array.");
        }
        const text = unwrapContent(choice.message?.content);
        if (text === null) {
          throw new EmptyContentError(
            "OpenRouter returned an empty / unrecognized content shape. " +
              "Expected string or array of {type:'text', text:string} parts.",
          );
        }
        if (responseValidator) {
          const verdict = responseValidator(text);
          if (!verdict.ok) {
            throw new InvalidContentError(
              verdict.reason ?? "responseValidator rejected the response",
            );
          }
        }
        return text;
      } catch (err) {
        lastErr = err;
        // OpenRouterError exposes the HTTP status as `statusCode`; fall
        // back to `code` / `status` for non-SDK errors that may bubble
        // up from fetch internals.
        const code = (err as { statusCode?: number; code?: number; status?: number }).statusCode
          ?? (err as { code?: number; status?: number }).code
          ?? (err as { code?: number; status?: number }).status;
        // OpenRouterError carries the upstream Headers object;
        // retry-after may live there. Try a couple of shapes.
        const headers = (err as { headers?: Headers | Record<string, string> }).headers;
        const retryAfterHeader = headers instanceof Headers
          ? headers.get("retry-after")
          : (headers as Record<string, string> | undefined)?.["retry-after"];
        const retryAfter =
          (err as { retryAfter?: number }).retryAfter
          ?? (retryAfterHeader ? Number(retryAfterHeader) : undefined);
        const isHttpTransient = code === 429
          || (typeof code === "number" && code >= 500 && code < 600);
        // Empty / partial body: the SDK throws a SyntaxError from
        // JSON.parse when the upstream response body was empty or
        // truncated. Treat as transient.
        const message = err instanceof Error ? err.message : String(err);
        const isParseTransient = err instanceof SyntaxError
          || /Unexpected EOF|Unexpected end of JSON input/i.test(message);
        const isNetworkTransient = isTransientNetworkError(err, message);
        const isEmptyContent = err instanceof EmptyContentError;
        const isInvalidContent = err instanceof InvalidContentError;
        const transient = isHttpTransient || isNetworkTransient || isParseTransient || isEmptyContent || isInvalidContent;
        if (!transient || attempt === maxAttempts - 1) {
          throw err;
        }
        const baseMs = retryAfter && Number.isFinite(retryAfter) ? retryAfter * 1000 : 0;
        // Backoff: 5s, 10s, 20s, 40s, capped at 60s. If retry-after says
        // longer, honor that.
        const backoffMs = Math.max(baseMs, Math.min(60_000, 5_000 * Math.pow(2, attempt)));
        const reason = isHttpTransient
          ? `code=${code}`
          : isNetworkTransient
            ? `network:${message.slice(0, 60)}`
          : isEmptyContent
            ? `empty-content`
            : isInvalidContent
              ? `invalid-content:${message.slice(0, 60)}`
              : `parse:${message.slice(0, 60)}`;
        // eslint-disable-next-line no-console
        console.warn(
          `[openrouter] transient error (${reason}); retrying in ${(backoffMs / 1000).toFixed(0)}s (attempt ${attempt + 2}/${maxAttempts})`,
        );
        await new Promise((res) => setTimeout(res, backoffMs));
        attempt++;
      }
    }
    // Unreachable, but keep type-checker happy.
    throw lastErr;
  }
}

function isTransientNetworkError(err: unknown, message: string): boolean {
  const code = (err as { code?: unknown; errno?: unknown }).code;
  if (typeof code === "string" && [
    "ECONNRESET",
    "ETIMEDOUT",
    "ECONNABORTED",
    "EPIPE",
    "UND_ERR_SOCKET",
    "UND_ERR_CONNECT_TIMEOUT",
    "UND_ERR_HEADERS_TIMEOUT",
    "UND_ERR_BODY_TIMEOUT",
  ].includes(code)) {
    return true;
  }
  return /socket connection was closed|connection reset|network error|fetch failed|terminated/i.test(message);
}

/**
 * Thrown from inside `sendWithRetry` when the upstream returns a
 * syntactically-valid envelope but no usable content (missing
 * `choices`, empty `choices[]`, or `message.content` that unwraps to
 * null). Tagged so the catch block can recognize it as transient and
 * retry instead of bubbling out and failing the mod.
 */
class EmptyContentError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "EmptyContentError";
  }
}

/**
 * Thrown from inside `sendWithRetry` when a caller-supplied
 * `responseValidator` rejects the unwrapped content. Used for upstream
 * truncations that pass HTTP / envelope checks but produce invalid
 * downstream payloads (e.g. JSON cut off mid-object). Tagged so the
 * catch block treats it as transient.
 */
class InvalidContentError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "InvalidContentError";
  }
}

function splitCsvEnv(raw: string | undefined): string[] {
  if (!raw) return [];
  return raw.split(",").map((s) => s.trim()).filter(Boolean);
}

/**
 * Unwrap OpenAI-style assistant message content into a plain text
 * string. Most providers return a string outright, but some (e.g.
 * deepseek under certain conditions, anthropic via openrouter, vision
 * models) return an array of content parts:
 *   `[{ type: "text", text: "..." }, ...]`
 *
 * For text-only classification we just concatenate every `text` part
 * we find. Returns null when the shape is genuinely unrecognized so
 * the caller can throw a useful error.
 */
function unwrapContent(content: unknown): string | null {
  if (typeof content === "string") return content;
  if (content === null || content === undefined) return null;
  if (Array.isArray(content)) {
    const parts: string[] = [];
    for (const part of content) {
      if (typeof part === "string") {
        parts.push(part);
        continue;
      }
      if (part && typeof part === "object") {
        const candidate = (part as { text?: unknown; type?: unknown }).text;
        if (typeof candidate === "string") parts.push(candidate);
      }
    }
    return parts.length > 0 ? parts.join("") : null;
  }
  // Object form: some providers wrap the response as { text: "..." }.
  if (typeof content === "object") {
    const candidate = (content as { text?: unknown }).text;
    if (typeof candidate === "string") return candidate;
  }
  return null;
}
