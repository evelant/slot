#!/usr/bin/env bash
# Runs the canonical 60-item prompt-evaluation sample against a chosen
# backend + model. Reads the existing stage-1/2 outputs from
# tools/classification/out/ (so you don't need an mcmeta clone) and
# writes its own complete.json + fixtures under /tmp/slot-prompt-eval-$LABEL/.
#
# Usage:
#   scripts/eval-prompt.sh sonnet
#   scripts/eval-prompt.sh deepseek-v4-flash
#   scripts/eval-prompt.sh --backend openrouter --model openai/gpt-4o-mini
#
# Presets:
#   sonnet              — claude-cli backend, model=sonnet
#   deepseek-v4-flash   — openrouter backend, model=deepseek/deepseek-v4-flash
#                          (requires OPENROUTER_API_KEY in env)
#
# Or pass any --backend / --model combo directly.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
TOOLS_DIR="$REPO_ROOT/tools/classification"
OUT_BASE="/tmp"

# Pick up OPENROUTER_API_KEY (and other secrets) from the repo-root
# .env if present. Bun auto-loads .env from its cwd, but we cd into
# tools/classification before exec — so bun would only see a .env in
# that subdir. Sourcing the root .env here keeps the key in one place.
if [[ -f "$REPO_ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$REPO_ROOT/.env"
  set +a
fi

# The 60-item sample covering the categories playtest has surfaced as
# tricky: doors / trapdoors / fence_gates, beds, Block-of-X,
# rails, spawn eggs, mob drops, raw ores, lighting (torch / lantern /
# sea_lantern / etc), buckets, ingredient-stage blocks (concrete_powder
# / packed_mud / clay_ball), trophy / progression items
# (nether_star / dragon_egg / wither_skeleton_skull), single-narrow-
# purpose crafting inputs (pottery sherds / disc_fragment /
# glistering_melon_slice), corals, fireworks, plus canonical sanity
# items. Keep this in sync with the items the prompt's disambiguation
# section is supposed to handle.
SAMPLE="minecraft:torch,minecraft:soul_torch,minecraft:lantern,minecraft:soul_lantern,minecraft:sea_lantern,minecraft:redstone_lamp,minecraft:redstone_torch,minecraft:end_rod,minecraft:glowstone,minecraft:jack_o_lantern,minecraft:shroomlight,minecraft:ladder,minecraft:bucket,minecraft:water_bucket,minecraft:lava_bucket,minecraft:milk_bucket,minecraft:powder_snow_bucket,minecraft:axolotl_bucket,minecraft:cod_bucket,minecraft:salmon_bucket,minecraft:tropical_fish_bucket,minecraft:pufferfish_bucket,minecraft:tadpole_bucket,minecraft:white_concrete_powder,minecraft:green_concrete_powder,minecraft:white_concrete,minecraft:green_concrete,minecraft:packed_mud,minecraft:mud,minecraft:clay,minecraft:clay_ball,minecraft:nether_star,minecraft:dragon_egg,minecraft:dragon_head,minecraft:wither_skeleton_skull,minecraft:angler_pottery_sherd,minecraft:archer_pottery_sherd,minecraft:heart_pottery_sherd,minecraft:disc_fragment_5,minecraft:glistering_melon_slice,minecraft:firework_star,minecraft:firework_rocket,minecraft:tipped_arrow,minecraft:bee_spawn_egg,minecraft:music_disc_13,minecraft:dead_fire_coral,minecraft:dead_fire_coral_block,minecraft:tube_coral,minecraft:tube_coral_block,minecraft:iron_ingot,minecraft:diamond_pickaxe,minecraft:oak_planks,minecraft:cobblestone,minecraft:dandelion,minecraft:oak_sapling,minecraft:bread,minecraft:chest,minecraft:furnace,minecraft:jungle_door,minecraft:brown_bed,minecraft:diamond_block,minecraft:rail"

# Resolve preset / explicit flags.
BACKEND_ARGS=()
LABEL=""
case "${1:-}" in
  sonnet)
    # Explicit --backend claude-cli since the CLI now auto-infers
    # openrouter when model contains a vendor slash. "sonnet" alone is
    # a Claude alias so the default infers correctly, but pin
    # explicitly so the preset is robust to future default flips.
    BACKEND_ARGS=(--backend claude-cli --model sonnet)
    LABEL="sonnet"
    ;;
  deepseek-v4-flash)
    : "${OPENROUTER_API_KEY:?OPENROUTER_API_KEY must be set in env to run this preset}"
    # Pin to the official `deepseek` provider — lowest price, best
    # caching, best throughput, and the data-policy is acceptable for
    # this experiment. Avoids upstream rate-limits / format quirks
    # we've hit on third-party providers (deepinfra, siliconflow).
    BACKEND_ARGS=(
      --backend openrouter
      --model deepseek/deepseek-v4-flash
      --only-provider deepseek
    )
    LABEL="deepseek-v4-flash"
    ;;
  deepseek-v4-pro)
    : "${OPENROUTER_API_KEY:?OPENROUTER_API_KEY must be set in env to run this preset}"
    # Pro variant of v4: higher capability per OpenRouter catalog,
    # closer in price to flash than to sonnet. Same provider pin as
    # the flash preset.
    BACKEND_ARGS=(
      --backend openrouter
      --model deepseek/deepseek-v4-pro
      --only-provider deepseek
    )
    LABEL="deepseek-v4-pro"
    ;;
  --backend|--model|"")
    # User passed flags directly (or no args).
    BACKEND_ARGS=("$@")
    LABEL="custom"
    ;;
  *)
    echo "unknown preset: $1"
    echo "usage: $0 sonnet | deepseek-v4-flash | --backend X --model Y"
    exit 2
    ;;
esac

OUT_DIR="$OUT_BASE/slot-prompt-eval-$LABEL"
mkdir -p "$OUT_DIR"

# Stage 1 + 2 outputs are already on disk from a prior real-pipeline run.
# We just need the ndjson + partial layer; stage 3 reads both and
# produces complete.json from there.
SRC_NDJSON="$TOOLS_DIR/out/minecraft.items.ndjson"
SRC_PARTIAL="$TOOLS_DIR/out/minecraft.facets.partial.json"
[[ -f "$SRC_NDJSON" ]] || { echo "missing $SRC_NDJSON — run stage 1 first"; exit 1; }
[[ -f "$SRC_PARTIAL" ]] || { echo "missing $SRC_PARTIAL — run stage 2 first"; exit 1; }
cp "$SRC_NDJSON" "$OUT_DIR/"
cp "$SRC_PARTIAL" "$OUT_DIR/"

cd "$TOOLS_DIR"
exec bun run src/cli.ts classify \
  --mod minecraft \
  --source /tmp/missing-mcmeta \
  --out "$OUT_DIR" \
  --stages 3 \
  --sample "$SAMPLE" \
  --batch-size 12 \
  --concurrency 4 \
  --record-replay \
  --fixture-dir "$OUT_DIR/fixtures" \
  "${BACKEND_ARGS[@]}"
