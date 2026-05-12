import { readFileSync } from "node:fs";
import { basename, dirname, join } from "node:path";
import type { ItemExtractRecord } from "./record.ts";

export interface RuntimeExportSummary {
  schema_version?: number;
  format?: string;
  generated_by?: string;
  generated_at?: string;
  pack_id?: string;
  requested_pack_id?: string;
  loader?: string;
  minecraft_version?: string;
  item_count?: number;
  items_file?: string;
  item_tag_membership?: string;
  direct_item_tags_available?: boolean;
  namespace_counts?: Record<string, number>;
  item_tag_members?: Record<string, string[]>;
  block_tag_members?: Record<string, string[]>;
  recipe_type_counts?: Record<string, number>;
}

export function defaultRuntimeSummaryPath(runtimeItemsPath: string): string {
  if (runtimeItemsPath.endsWith(".runtime-items.ndjson")) {
    return runtimeItemsPath.slice(0, -".runtime-items.ndjson".length) + ".runtime-summary.json";
  }
  return join(dirname(runtimeItemsPath), basename(runtimeItemsPath).replace(/\.ndjson$/, ".summary.json"));
}

export function readRuntimeExportRecords(path: string): ItemExtractRecord[] {
  return readFileSync(path, "utf8")
    .split(/\r?\n/)
    .filter((line) => line.trim().length > 0)
    .map((line) => JSON.parse(line) as ItemExtractRecord);
}

export function readRuntimeExportSummary(path: string): RuntimeExportSummary {
  return JSON.parse(readFileSync(path, "utf8")) as RuntimeExportSummary;
}
