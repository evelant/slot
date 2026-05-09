import { describe, expect, test } from "bun:test";
import { existsSync, mkdirSync, mkdtempSync, rmSync, writeFileSync } from "node:fs";
import { tmpdir } from "node:os";
import { join, resolve } from "node:path";
import {
  loadModpackManifest,
  planModpack,
  resolveModSource,
  type Modpack,
} from "../src/modpack.ts";

function withTempDir<T>(fn: (dir: string) => T): T {
  const dir = mkdtempSync(join(tmpdir(), "slot-classification-modpack-"));
  try {
    return fn(dir);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
}

function writeManifest(dir: string, pack: unknown): string {
  const path = join(dir, "pack.json");
  writeFileSync(path, JSON.stringify(pack, null, 2) + "\n");
  return path;
}

function validLayer(source = "example", entries: Record<string, unknown> = {
  [`${source}:item`]: {
    facets: {
      role: {
        value: "material",
        source: "test",
      },
    },
  },
}): unknown {
  return {
    schema_version: 1,
    layer: "per-mod",
    source,
    entries,
  };
}

describe("loadModpackManifest", () => {
  test("rejects malformed and incomplete manifests", () => {
    withTempDir((dir) => {
      const malformed = join(dir, "bad.json");
      writeFileSync(malformed, "{");
      expect(() => loadModpackManifest(malformed)).toThrow(/not valid JSON/);

      const noMods = writeManifest(dir, { name: "empty" });
      expect(() => loadModpackManifest(noMods)).toThrow(/mods/);

      const missingSource = writeManifest(dir, {
        name: "pack",
        mods: [{ namespace: "create", displayName: "Create" }],
      });
      expect(() => loadModpackManifest(missingSource)).toThrow(/sourcePath/);
    });
  });

  test("accepts skipped entries without source paths", () => {
    withTempDir((dir) => {
      const path = writeManifest(dir, {
        name: "pack",
        mods: [{ namespace: "architectury", displayName: "Architectury", skip: "library" }],
      });
      const resolved = loadModpackManifest(path);
      expect(resolved.pack.mods[0]?.skip).toBe("library");
    });
  });
});

describe("resolveModSource", () => {
  test("prefers paths relative to the manifest", () => {
    withTempDir((dir) => {
      const source = join(dir, "mods", "create");
      mkdirSync(source, { recursive: true });
      expect(resolveModSource(dir, "mods/create")).toBe(source);
    });
  });

  test("falls back to repo-root-relative paths when a tools/classification marker exists", () => {
    withTempDir((dir) => {
      const manifestDir = join(dir, "tools", "classification", "modpacks");
      const source = join(dir, "reference", "classification", "create");
      mkdirSync(manifestDir, { recursive: true });
      mkdirSync(source, { recursive: true });

      expect(resolveModSource(manifestDir, "reference/classification/create")).toBe(source);
    });
  });
});

describe("planModpack", () => {
  test("distinguishes skip, reusable output, malformed output, invalid output, empty output, and missing output", () => {
    withTempDir((dir) => {
      const outDir = join(dir, "out");
      mkdirSync(outDir);

      writeFileSync(
        join(outDir, "done.facets.complete.json"),
        JSON.stringify(validLayer("done"), null, 2) + "\n",
      );
      writeFileSync(join(outDir, "malformed.facets.complete.json"), "{");
      writeFileSync(
        join(outDir, "invalid.facets.complete.json"),
        JSON.stringify({ schema_version: 1, entries: {} }, null, 2) + "\n",
      );
      writeFileSync(
        join(outDir, "empty.facets.complete.json"),
        JSON.stringify(validLayer("empty", {}), null, 2) + "\n",
      );

      const pack: Modpack = {
        name: "pack",
        mods: [
          { namespace: "library", displayName: "Library", skip: "api-only" },
          { namespace: "done", displayName: "Done", sourcePath: "sources/done" },
          { namespace: "malformed", displayName: "Malformed", sourcePath: "sources/malformed" },
          { namespace: "invalid", displayName: "Invalid", sourcePath: "sources/invalid" },
          { namespace: "empty", displayName: "Empty", sourcePath: "sources/empty" },
          { namespace: "missing", displayName: "Missing", sourcePath: "sources/missing" },
        ],
      };
      const manifestPath = writeManifest(dir, pack);
      const resolved = loadModpackManifest(manifestPath);
      const decisions = planModpack(resolved, outDir);
      const byNamespace = Object.fromEntries(decisions.map((d) => [d.entry.namespace, d]));

      expect(byNamespace.library?.decision).toBe("skipped:library");
      expect(byNamespace.done?.decision).toBe("skipped:already-classified");
      expect(byNamespace.done?.entryCount).toBe(1);
      expect(byNamespace.malformed?.decision).toBe("process");
      expect(byNamespace.malformed?.reason).toContain("not valid JSON");
      expect(byNamespace.invalid?.decision).toBe("process");
      expect(byNamespace.invalid?.reason).toContain("schema validation");
      expect(byNamespace.empty?.decision).toBe("process");
      expect(byNamespace.empty?.reason).toContain("no entries");
      expect(byNamespace.missing?.decision).toBe("process");
      expect(existsSync(resolve(byNamespace.missing!.sourcePath!))).toBe(false);
    });
  });
});
