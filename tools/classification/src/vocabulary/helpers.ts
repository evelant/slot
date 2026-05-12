import type { VocabularyEvidenceRef, VocabularyState } from "../schema/vocabulary.ts";

export function tokenPath(value: string): string | null {
  const parts = value
    .split(/[\/:#.\s-]+/)
    .map(token)
    .filter(Boolean);
  if (parts.length === 0) return null;
  return parts.join("/");
}

export function token(value: string): string {
  const normalized = value
    .toLowerCase()
    .replace(/&/g, " and ")
    .replace(/[^a-z0-9]+/g, "_")
    .replace(/^_+|_+$/g, "")
    .replace(/_+/g, "_");
  if (!normalized) return "";
  if (/^[a-z]/.test(normalized)) return normalized;
  return `value_${normalized}`;
}

export function tokenSet(value: string): Set<string> {
  const out = new Set<string>();
  for (const part of value.toLowerCase().split(/[^a-z0-9]+/)) {
    if (!part) continue;
    out.add(part);
    if (part.endsWith("s") && part.length > 3) out.add(part.slice(0, -1));
  }
  return out;
}

export function splitResourceLocation(value: string): { namespace: string; path: string } | null {
  const match = value.match(/^([a-z0-9_.-]+):([a-z0-9_./-]+)$/);
  if (!match) return null;
  return { namespace: match[1]!, path: match[2]! };
}

export function labelFromId(id: string): string {
  const raw = id.includes("#") ? id.slice(id.indexOf("#") + 1) : id;
  const tail = raw.includes("/") ? raw.slice(raw.lastIndexOf("/") + 1) : raw.slice(raw.lastIndexOf(":") + 1);
  return tail
    .replace(/[_./-]+/g, " ")
    .trim()
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

export function defaultTuple(input: [string, string, string?, string[]?]): {
  id: string;
  label: string;
  description?: string;
  aliases?: string[];
} {
  return {
    id: input[0]!,
    label: input[1]!,
    ...(input[2] ? { description: input[2] } : {}),
    ...(input[3] ? { aliases: input[3] } : {}),
  };
}

export function slotDefault(value: string): { id: string; label: string } {
  return { id: `slot:${value}`, label: labelFromId(value) };
}

export function strongestState(a: VocabularyState, b: VocabularyState): VocabularyState {
  return stateRank(b) < stateRank(a) ? b : a;
}

export function stateRank(state: VocabularyState): number {
  switch (state) {
    case "accepted":
      return 0;
    case "review":
      return 1;
    case "rejected":
      return 2;
  }
}

export function isVocabularyState(value: unknown): value is VocabularyState {
  return value === "accepted" || value === "review" || value === "rejected";
}

export function isConfidence(value: unknown): value is number {
  return typeof value === "number" && Number.isFinite(value) && value >= 0 && value <= 1;
}

export function evidenceRefs(values: unknown[]): VocabularyEvidenceRef[] {
  const out: VocabularyEvidenceRef[] = [];
  for (const value of values) {
    if (!isRecord(value) || typeof value.kind !== "string" || typeof value.id !== "string") continue;
    out.push({
      kind: value.kind,
      id: value.id,
      ...(isConfidence(value.confidence) ? { confidence: value.confidence } : {}),
    });
  }
  return out;
}

export function stringArray(values: unknown[]): string[] {
  return values.filter((value): value is string => typeof value === "string" && value.trim().length > 0);
}

export function sortedLimited(values: Iterable<string>, limit: number): string[] {
  return [...new Set([...values].filter((value) => value.length > 0))]
    .sort()
    .slice(0, limit);
}

export function round(value: number): number {
  return Math.round(value * 1000) / 1000;
}

export function looksLikeResourceLocation(value: string): boolean {
  return /^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(value);
}

export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

export function unwrapEnvelope(raw: string): string {
  const trimmed = raw.trim();
  if (!trimmed.startsWith("{")) return trimmed;
  try {
    const obj = JSON.parse(trimmed);
    if (isRecord(obj) && typeof obj.result === "string") return obj.result;
  } catch {
    return trimmed;
  }
  return trimmed;
}

export function firstJsonObject(text: string): string | null {
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
      else if (ch === "\"") inString = false;
      continue;
    }
    if (ch === "\"") {
      inString = true;
      continue;
    }
    if (ch === "{") depth++;
    if (ch === "}") {
      depth--;
      if (depth === 0) return body.slice(start, i + 1);
    }
  }
  return null;
}
