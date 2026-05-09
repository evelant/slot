export const INPUT_MANIFEST_V2_SCHEMA_VERSION = 2;

export type ClassificationInputSourceKind =
  | "mods-folder"
  | "jar"
  | "source-tree"
  | "curseforge-manifest"
  | "modrinth-mrpack"
  | "runtime-export";

export type ClassificationLoader = "forge" | "neoforge" | "fabric" | "quilt" | "unknown";

export type ScanStatus =
  | "covered:bundled"
  | "covered:public-exact"
  | "covered:public-compatible"
  | "covered:mod-shipped"
  | "covered:local-cache"
  | "partial:runtime-crawl-only"
  | "missing:semantic-generation-available"
  | "skipped:library"
  | "blocked:ambiguous-mod-id"
  | "blocked:malformed-jar";

export interface InputManifestV2 {
  kind: "slot-classification-input-manifest";
  schema_version: typeof INPUT_MANIFEST_V2_SCHEMA_VERSION;
  generated_by: string;
  generated_at: string;
  source: InputManifestSource;
  summary: InputManifestSummary;
  mods: InputManifestMod[];
}

export interface InputManifestSource {
  kind: ClassificationInputSourceKind;
  requested_path: string;
  resolved_mods_path?: string;
  minecraft_version?: string;
  loader?: ClassificationLoader;
  loader_version?: string;
  pack_name?: string;
  pack_version?: string;
  platform?: "curseforge" | "modrinth" | "prism" | "unknown";
  platform_project_id?: string | number;
  platform_version_id?: string | number;
  pack_scripts?: PackScriptSummary;
}

export interface PackScriptSummary {
  kubejs?: {
    server_scripts: number;
    startup_scripts: number;
    client_scripts: number;
    data_files: number;
    asset_files: number;
  };
}

export interface InputManifestSummary {
  total_jars: number;
  total_mod_entries: number;
  total_item_candidates: number;
  status_counts: Record<string, number>;
}

export interface InputManifestMod {
  id: string;
  display_name?: string;
  version?: string;
  loader: ClassificationLoader;
  minecraft_versions: string[];
  source_kind: "jar";
  path: string;
  file_name: string;
  hashes: {
    sha1: string;
    sha512: string;
  };
  platform_ids?: PlatformIds;
  namespaces: string[];
  resource_counts: ResourceCounts;
  item_candidate_count: number;
  item_set_signature: string;
  status: ScanStatus;
  skipped_reason?: string;
  diagnostics: string[];
}

export interface PlatformIds {
  curseforge_project_id?: number;
  curseforge_file_id?: number;
  modrinth_project_id?: string;
  modrinth_version_id?: string;
}

export interface ResourceCounts {
  recipes: number;
  loot_tables: number;
  item_tags: number;
  block_tags: number;
  lang_files: number;
  item_definitions: number;
  item_models: number;
  models: number;
  blockstates: number;
}
