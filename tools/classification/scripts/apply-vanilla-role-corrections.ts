#!/usr/bin/env bun
/**
 * Apply systematic role corrections to the vanilla classification layer.
 *
 * This script encodes per-category role overrides surfaced by playtest:
 * the LLM stage-3 output had recurring inconsistencies (some doors as
 * functional_block while others as building_block, beds as
 * functional_block, mob drops as natural_resource even though players
 * treat them as crafting materials, "Block of X" as storage_block, …).
 * The prompt has been tightened so future regenerations converge, but
 * the existing shipped dataset still carries the old values — this
 * script patches them in place.
 *
 * Run via:
 *   bun tools/classification/scripts/apply-vanilla-role-corrections.ts
 *
 * The script is idempotent — re-running it leaves correct entries
 * untouched and only flips entries that still match a wrong-role rule.
 *
 * After running, copy the dataset into the runtime resource path:
 *   cp tools/classification/datasets/minecraft/minecraft.facets.complete.json \
 *      common/src/main/resources/data/slot/classification/vanilla-base.json
 */

import { readFileSync, writeFileSync } from "node:fs";
import { resolve } from "node:path";

type FacetEntry = {
  value?: string | number | boolean | null;
  values?: Array<string | number>;
  ambiguous?: boolean;
  confidence?: number;
  source?: string;
  rationale?: string;
  mode?: string;
};

type LayerEntry = {
  facets: Record<string, FacetEntry>;
};

type Layer = {
  schema_version: number;
  layer: string;
  entries: Record<string, LayerEntry>;
};

const REPO_ROOT = resolve(import.meta.dir, "..", "..", "..");
const DATASET = resolve(
  REPO_ROOT,
  "tools/classification/datasets/minecraft/minecraft.facets.complete.json",
);
const RUNTIME_RESOURCE = resolve(
  REPO_ROOT,
  "common/src/main/resources/data/slot/classification/vanilla-base.json",
);

type Rule = {
  /** Human-readable rule name for the change report. */
  name: string;
  /** Predicate over the (lowercase) item id sans namespace. */
  matches: (id: string, ns: string, fullId: string) => boolean;
  /** New role to write. */
  role: string;
  /** Optional rationale recorded on the patched facet entry. */
  rationale: string;
};

const RULES: Rule[] = [
  // Doors / trapdoors / fence_gates of any material → building_block.
  {
    name: "doors → building_block",
    matches: (id) => id.endsWith("_door") || id === "iron_door",
    role: "building_block",
    rationale: "vanilla-corrections: doors seal buildings; their primary use is structural",
  },
  {
    name: "trapdoors → building_block",
    matches: (id) => id.endsWith("_trapdoor"),
    role: "building_block",
    rationale: "vanilla-corrections: trapdoors are structural openings; primary use is building",
  },
  {
    name: "fence_gates → building_block",
    matches: (id) => id.endsWith("_fence_gate"),
    role: "building_block",
    rationale: "vanilla-corrections: fence gates are structural; primary use is enclosure",
  },
  // Beds → decorative_block.
  {
    name: "beds → decorative_block",
    matches: (id) => id.endsWith("_bed"),
    role: "decorative_block",
    rationale: "vanilla-corrections: beds belong on a Decoration island, one per home",
  },
  // Decorated pot → decorative_block.
  {
    name: "decorated_pot → decorative_block",
    matches: (id) => id === "decorated_pot",
    role: "decorative_block",
    rationale: "vanilla-corrections: decorated_pot is a display piece, not a workstation",
  },
  // All rails → transport.
  {
    name: "rails → transport",
    matches: (id) => id === "rail" || id.endsWith("_rail"),
    role: "transport",
    rationale: "vanilla-corrections: rails are transport infrastructure regardless of redstone variant",
  },
  // Spawn eggs → curiosity.
  {
    name: "spawn_eggs → curiosity",
    matches: (id) => id.endsWith("_spawn_egg"),
    role: "curiosity",
    rationale: "vanilla-corrections: spawn eggs are creative-collectible, not utility/admin",
  },
  // Compressed material blocks (Block of X) → material.
  {
    name: "compressed material blocks → material",
    matches: (id) =>
      [
        "iron_block",
        "gold_block",
        "diamond_block",
        "emerald_block",
        "lapis_block",
        "netherite_block",
        "coal_block",
        "amethyst_block",
        "quartz_block",
        "raw_iron_block",
        "raw_copper_block",
        "raw_gold_block",
        "copper_block",
        "exposed_copper",
        "weathered_copper",
        "oxidized_copper",
        "waxed_copper_block",
        "waxed_exposed_copper",
        "waxed_weathered_copper",
        "waxed_oxidized_copper",
      ].includes(id),
    role: "material",
    rationale: "vanilla-corrections: Block of X is compressed material storage, not a container",
  },
  // Smithing templates (netherite_upgrade + every armor-trim variant) →
  // upgrade. The LLM mis-categorizes these as material (because they
  // craft into trim variants) or curiosity (because they come in 16+
  // patterns). Player perception: smithing-table fodder, applied once
  // each in the smithing UI. → upgrade.
  {
    name: "smithing_templates → upgrade",
    matches: (id) => id.endsWith("_smithing_template"),
    role: "upgrade",
    rationale: "vanilla-corrections: smithing templates apply via smithing-table UI; upgrade fodder, not curiosity",
  },
  // Pottery sherds (every variant) → curiosity. Sherds come in 20+
  // archeology-found patterns and players collect them as a set; the
  // craft sink (decorated_pot) is decorative-only and rare. Better
  // mental model: "set of patterns I've found", not "crafting stash."
  {
    name: "pottery_sherds → curiosity",
    matches: (id) => id.endsWith("_pottery_sherd"),
    role: "curiosity",
    rationale: "vanilla-corrections: archeology sherds are collect-a-set; rare decorative-only craft sink",
  },
  // Hanging signs (every variant) → decorative_block.
  // Players treat hanging signs identically to regular signs — display
  // pieces, not workstations. Edit-on-place is one-shot, not a UI loop.
  {
    name: "hanging signs → decorative_block",
    matches: (id) => id.endsWith("_hanging_sign"),
    role: "decorative_block",
    rationale: "vanilla-corrections: hanging signs are decoration, not workstations",
  },
  // Regular signs are sometimes mis-classified the same way.
  {
    name: "signs → decorative_block",
    matches: (id) => id.endsWith("_sign") && !id.endsWith("_hanging_sign"),
    role: "decorative_block",
    rationale: "vanilla-corrections: signs are display pieces, not workstations",
  },
  // Lightning rod → redstone_component (emits power on lightning strike).
  {
    name: "lightning_rod → redstone_component",
    matches: (id) => id === "lightning_rod",
    role: "redstone_component",
    rationale: "vanilla-corrections: lightning_rod emits redstone signal on strike",
  },
  // Pointed dripstone → natural_resource (cave organic).
  {
    name: "pointed_dripstone → natural_resource",
    matches: (id) => id === "pointed_dripstone",
    role: "natural_resource",
    rationale: "vanilla-corrections: pointed_dripstone is cave nature; grows / drips / spike-trap",
  },
  // Mob drops + raw ore chunks → material.
  {
    name: "mob drops + raw ores → material",
    matches: (id) =>
      [
        "blaze_rod",
        "string",
        "leather",
        "feather",
        "bone",
        "slime_ball",
        "gunpowder",
        "ghast_tear",
        "magma_cream",
        "phantom_membrane",
        "prismarine_shard",
        "prismarine_crystals",
        "ink_sac",
        "glow_ink_sac",
        "nautilus_shell",
        "rabbit_hide",
        "armadillo_scute",
        "turtle_scute",
        "honeycomb",
        "scute",
        "dragon_breath",
        "raw_iron",
        "raw_copper",
        "raw_gold",
        "ancient_debris",
      ].includes(id),
    role: "material",
    rationale: "vanilla-corrections: mob drops + raw chunks are crafting materials, not placeable nature",
  },
];

function applyRules(layer: Layer): { updated: Map<string, { from: string | undefined; to: string; rule: string }> } {
  const updated = new Map<string, { from: string | undefined; to: string; rule: string }>();

  for (const [fullId, entry] of Object.entries(layer.entries)) {
    if (!entry.facets) continue;
    const colonIdx = fullId.indexOf(":");
    if (colonIdx <= 0) continue;
    const ns = fullId.substring(0, colonIdx);
    const id = fullId.substring(colonIdx + 1);
    if (ns !== "minecraft") continue;

    for (const rule of RULES) {
      if (!rule.matches(id, ns, fullId)) continue;
      const existing = entry.facets["role"];
      const currentRole = existing?.value as string | undefined;
      // Skip if already correct and not ambiguous.
      if (currentRole === rule.role && !existing?.ambiguous) continue;
      const newEntry: FacetEntry = {
        value: rule.role,
        confidence: 0.95,
        source: "vanilla-corrections:role-overrides",
        rationale: rule.rationale,
      };
      entry.facets["role"] = newEntry;
      updated.set(fullId, { from: currentRole, to: rule.role, rule: rule.name });
      break; // one rule per item
    }
  }

  return { updated };
}

function main(): void {
  const raw = readFileSync(DATASET, "utf-8");
  const layer = JSON.parse(raw) as Layer;
  if (layer.schema_version !== 1) {
    throw new Error(`unsupported schema_version: ${layer.schema_version}`);
  }
  if (layer.layer !== "vanilla-base") {
    throw new Error(`expected layer=vanilla-base, got: ${layer.layer}`);
  }

  const { updated } = applyRules(layer);

  if (updated.size === 0) {
    console.log("No corrections applied — dataset already matches the rules.");
    return;
  }

  // Group by rule for the report.
  const byRule = new Map<string, Array<{ id: string; from: string | undefined; to: string }>>();
  for (const [id, change] of updated) {
    const list = byRule.get(change.rule) ?? [];
    list.push({ id, from: change.from, to: change.to });
    byRule.set(change.rule, list);
  }

  console.log(`Patched ${updated.size} entries across ${byRule.size} rule(s):`);
  for (const [rule, items] of byRule) {
    console.log(`  ${rule}: ${items.length} entries`);
    const sample = items.slice(0, 5).map((i) => `${i.id} (${i.from ?? "ambiguous/none"} → ${i.to})`);
    for (const s of sample) console.log(`    - ${s}`);
    if (items.length > sample.length) {
      console.log(`    … and ${items.length - sample.length} more`);
    }
  }

  const out = JSON.stringify(layer, null, 2) + "\n";
  writeFileSync(DATASET, out);
  writeFileSync(RUNTIME_RESOURCE, out);
  console.log(`\nWrote ${DATASET}`);
  console.log(`Wrote ${RUNTIME_RESOURCE}`);
}

main();
