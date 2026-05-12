import { GENERIC_TOKENS } from "./constants.ts";
import { splitResourceLocation, token } from "./helpers.ts";

export function resourcePathTail(value: string): string | null {
  const split = splitResourceLocation(value);
  const raw = split?.path ?? value;
  const parts = raw.split(/[\/_.\s-]+/).filter(Boolean);
  if (parts.length === 0) return null;
  return token(parts[parts.length - 1]!);
}

export function isGenericValueId(id: string): boolean {
  const raw = id.includes("#") ? id.slice(0, id.indexOf("#")) : id;
  const tail = raw.includes("/") ? raw.slice(raw.lastIndexOf("/") + 1) : raw.slice(raw.lastIndexOf(":") + 1);
  return GENERIC_TOKENS.has(tail) || tail.startsWith("crafting_");
}
