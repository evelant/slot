import type { SemanticTextEvidence } from "./record.ts";

const SEMANTIC_SUFFIXES = [
  "tooltip",
  "tooltip.summary",
  "tooltip.condition",
  "tooltip.behaviour",
  "tooltip.control",
  "tooltip.description",
  "description",
  "desc",
  "summary",
];

export function itemSemanticTextFromLang(args: {
  lang: Record<string, string>;
  namespace: string;
  path: string;
}): SemanticTextEvidence[] {
  const prefixes = [
    `item.${args.namespace}.${args.path}`,
    `block.${args.namespace}.${args.path}`,
  ];
  const out: SemanticTextEvidence[] = [];
  for (const [key, value] of Object.entries(args.lang)) {
    if (!isSemanticLangKey(key, prefixes)) continue;
    for (const text of splitSemanticText(value)) {
      out.push({ source: "lang", key, text });
    }
  }
  return dedupeSemanticText(out);
}

export function splitSemanticText(value: unknown): string[] {
  if (typeof value !== "string") return [];
  return value
    .split(/\r?\n+/)
    .map(cleanSemanticText)
    .filter((text) => text.length > 0);
}

export function cleanSemanticText(value: string): string {
  return value
    .replace(/\$\(br2?\)/g, "\n")
    .replace(/\$\(li\)/g, "\n- ")
    .replace(/\$\(l:[^)]+\)([^$]*)/g, "$1")
    .replace(/\$\([^)]+\)/g, "")
    .replace(/\$\(\)/g, "")
    .replace(/&[0-9a-fk-or]/gi, "")
    .replace(/§[0-9a-fk-or]/gi, "")
    .replace(/\\n/g, "\n")
    .replace(/[ \t]+/g, " ")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
}

export function dedupeSemanticText(values: Iterable<SemanticTextEvidence>): SemanticTextEvidence[] {
  const seen = new Set<string>();
  const out: SemanticTextEvidence[] = [];
  for (const value of values) {
    const text = cleanSemanticText(value.text);
    if (!text) continue;
    const key = `${value.source}\u0000${value.key ?? ""}\u0000${text}`;
    if (seen.has(key)) continue;
    seen.add(key);
    out.push({
      source: value.source,
      text,
      ...(value.key ? { key: value.key } : {}),
    });
  }
  return out;
}

function isSemanticLangKey(key: string, prefixes: readonly string[]): boolean {
  for (const prefix of prefixes) {
    if (!key.startsWith(`${prefix}.`)) continue;
    const suffix = key.slice(prefix.length + 1);
    if (SEMANTIC_SUFFIXES.some((known) =>
      suffix === known || suffix.startsWith(`${known}.`) || suffix.startsWith(`${known}_`) || new RegExp(`^${escapeRegExp(known)}\\d+$`).test(suffix)
    )) {
      return true;
    }
  }
  return false;
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}
