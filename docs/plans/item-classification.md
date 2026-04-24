# Item Classification & Facet Schema

Status: **planning** (no code yet; specs complete through milestone 2). Iterate on this
document before writing the pipeline.
Last updated: 2026-04-24.

## Reading order for a fresh session

This document is ~1400 lines. If you're picking it up without prior context:

1. **[Purpose](#purpose)** and **[Glossary](#glossary)** (≈ 40 lines) — the vocabulary and why this project exists.
2. **[Goals](#goals)** / **[Non-goals (V1)](#non-goals-v1)** — scope boundaries.
3. **[Facet kinds](#facet-kinds)** — the type system. Everything downstream assumes these eight kinds.
4. **[Layer file format](#layer-file-format)** + the JSONSchema at [tools/classification/layer.schema.json](../../tools/classification/layer.schema.json) — the wire contract.
5. **[Layering & merging](#layering--merging)** — how the 6 layers stack, including the [Resource-location matrix](#resource-location-matrix).
6. **[Pipeline](#pipeline)** and **[Runtime discovery](#runtime-discovery)** — what the offline pipeline does vs what the runtime crawl does.
7. **[Milestones](#milestones)** — the execution order. Milestones 1–4 complete (stage-1 extractor + stage-2 deterministic rules in [tools/classification/](../../tools/classification/)); milestone 5 (stage-3 LLM completion) is the next concrete work.
8. **Facet list (1–28)** — skim the headings; read the specific facets you need.

Skip **[Modeling principles](#modeling-principles)** until you're about to add or change a facet — those rules are authored for that case.

The **[Test strategy](#test-strategy)** section is the contract for what must be tested; consult it before writing any stage or runtime component.

## Purpose

Replace the current keyword/tag rule resolver
([SemanticBucketResolver.java](../../common/src/main/java/dev/imagio/slot/debug/SemanticBucketResolver.java),
[ParentKeywordRules.java](../../common/src/main/java/dev/imagio/slot/debug/ParentKeywordRules.java))
with a structured, queryable item metadata layer that powers:

- High-quality default home/island suggestions (the immediate pain point — current match rate feels ~40%).
- Future surfacing features (smart search, recipe-aware highlighting, deposit hints, clutter review, theme detection).
- Mod extensibility: point the pipeline at any mod and produce a classification file for its items.

The UI contract stays unchanged for V1: every item has **one home on one island**. That home
is now derived from a rich per-item metadata record rather than a single bucket enum, but the
player-facing model is identical.

## Glossary

Words matter here — several are overloaded in Minecraft or in our current codebase.

| Term | Meaning in this document |
| --- | --- |
| **item** | A single in-game item type, identified by a namespaced id (`minecraft:iron_ingot`, `create:cogwheel`). |
| **facet** | A property type that describes items. Examples: `role`, `activity`, `material_family`, `form`, `tier`. The schema lists every facet. |
| **facet kind** | The type category a facet belongs to — `enum`, `multi_enum`, `free_text`, `multi_free_text`, `boolean`, `numeric`, `item_ref`, `multi_item_ref`. Determines value shape, validation, and valid `mode`s. See [Facet kinds](#facet-kinds). |
| **facet value** | A specific assignment within a facet. For facet `material_family`, values include `iron`, `copper`, `wood_oak`. |
| **facet entry** | `(item, facet, value, mode, metadata)` — a single assertion about an item asserted by one layer. An item has many facet entries across several facets. |
| **mode** | How a facet entry combines with entries from lower layers. One of `replace` (default for single-value), `add` (default for multi-value), `remove`, `override-if-null`. See [Merge modes](#merge-modes). |
| **schema** | The versioned catalog of valid facets, their kinds, and their allowed values. Human-authored, lives at `common/src/main/resources/slot/classification/schema.v<N>.json` once frozen. |
| **classification** | The output of running the pipeline: a file mapping each item to its facet entries, confidences, and provenance. |
| **island** | Existing product concept. A visual/logical grouping of items the player sees in the atlas. Unchanged in V1. |
| **home** | Existing product concept. The single island an item is routed to by default. In V1 this is picked from the item's `role` facet (see [Homing rule](#homing-rule-v1)). |
| **layer** | A source of classification data. Six kinds, lowest → highest priority: `vanilla-base`, `per-mod`, `runtime-crawl`, `modpack`, `server`, `player`. Layers stack under explicit merge rules. |
| **override** | A facet entry from a higher layer that replaces or extends a lower layer's entry for the same `(item, facet)`. |
| **FacetIndex** | The runtime component that merges all layers and exposes queries + per-item lookup. `dev.imagio.slot.classification.FacetIndex`. |
| **runtime-crawl** | A synthetic layer built at `FacetIndex` init time by walking live game registries. Deterministic-only, no LLM facets. Handles datapacks, KubeJS, cross-mod tag closure, and unknown mods. See [Runtime discovery](#runtime-discovery). |
| **Minecraft item tag** | Vanilla's own `data/<ns>/tags/item/*.json` concept. Input signal only; never user-facing in this system. Avoid the word "tag" for our own concepts. |

**Hard rule:** in this project's code and docs, "tag" refers only to Minecraft item tags.
Our property system uses **facet** / **facet value**. Anywhere you feel like writing "tag"
for our metadata, use "facet value" instead.

## Goals

- **Correct homing** for items that have an obvious home, driven by explicit data (Minecraft item tags, creative tab membership, mod-specific signals) rather than brittle path keyword matches.
- **Judgement calls** for items that don't have an obvious home or to enrich data with semantics, made once per item by an LLM and captured into the classification file. Non-deterministic at generation time, deterministic at runtime.
- **Queryable metadata** so future features ("mining loadout," "oak workshop," "trophy shelf") are `FacetIndex.where(...)` calls, not new resolvers.
- **Per-mod files** so adding a mod is a pipeline run against that mod's source/jar, not a code change.
- **Community-editable** output: a flat, diff-friendly JSON per mod. Corrections land as PRs.

## Non-goals (V1)

Explicitly deferred so scope stays manageable:

- Cross-player community-shared island templates.
- Vector-embedding similarity / runtime fuzzy search.
- Player-island inference-and-auto-catch ("want this island to auto-include future oak items?").
- Query-authoring UI for players.
- Ranking multiple candidate homes in the UI. Islands stay 1-to-1 with items.
- **Presets.** No named configurations that switch how homing is derived. The UI is unchanged; behind the scenes there is exactly one homing rule in V1 and it uses a fixed facet priority.
- Running mod data generators offline. Source-tree / jar scan for the precompute pipeline, plus live registry inspection at runtime (see [Runtime discovery](#runtime-discovery)) — no Gradle-driven data gen per mod.
- Our own in-game item search UI. If we ever need it, reuse EMI's search (see Integration).

All of these are cheap add-ons **once the facet layer exists**. Building them first
is putting the roof before the walls.

## Proposed facet schema (v1)

The schema is the most important artifact in this project. Below is a concrete starting
proposal to iterate against. Each facet has an id, cardinality, description, and a
candidate value list. Cardinality: `single` = at most one value, `multi` = zero or more.

Facet counts below include `null` / `unknown` as a valid absence — most facets are not
applicable to every item (a food item has no `tier`; a potion has no `wood_type`).

### Facet kinds

Every facet declares a **kind**, which determines its value shape, validation rules, and
the valid `mode`s it can use in a layer entry. The schema-authoring format
([classification-layer.schema.json](../../tools/classification/layer.schema.json))
enforces these via JSONSchema.

| Kind | Cardinality | Value shape | Valid modes | Example facets |
| --- | --- | --- | --- | --- |
| `enum` | single | string, must be in declared value set | `replace`, `override-if-null` | `role`, `rarity`, `frequency`, `form`, `dye_color`, `equip_slot`, `required_tool`, `y_level_range`, `multiblock_role` |
| `multi_enum` | multi | array of strings, each must be in declared value set | `replace`, `add`, `remove` | `activity`, `flavor`, `palette`, `origin`, `storage_categories`, `spawn_interaction`, `combat_bonus`, `environmental_property`, `transport_medium`, `material_secondary` |
| `free_text` | single | string matching declared regex | `replace`, `override-if-null` | `tier`, `required_tool_tier`, `mod_namespace`, `material_family` (when admitting mod-specific values) |
| `multi_free_text` | multi | array of strings, each matching declared regex | `replace`, `add`, `remove` | `mod_subsystem`, `processing_in`, `primary_uses`, `biome`, `produces_effect`, `multiblock_component_of`, `player_island` |
| `boolean` | single | `true` or `false` | `replace`, `override-if-null` | all derived booleans (`is_block_item`, `is_fuel`, etc.) |
| `numeric` | single | number, with optional declared unit | `replace`, `override-if-null` | deferred — first V1 candidate is `container_capacity` |
| `item_ref` | single | string matching `^[a-z0-9_.-]+:[a-z0-9_/.-]+$`, must resolve to a registered item | `replace`, `override-if-null` | deferred to V2 |
| `multi_item_ref` | multi | array of item refs | `replace`, `add`, `remove` | deferred to V2 |

**Value validation happens at layer load, not at query time.** A layer declaring a
value not permitted by the facet's kind is rejected with an error; queries against a
validated index are always type-safe.

**Single-value ambiguity exception.** Facets of kind `enum` or `free_text` may receive
a two-element value array instead of a scalar when the producing stage (usually LLM)
flags `ambiguous: true`. This is the only case where a single-cardinality facet carries
more than one value. Homing rules pick the highest-confidence entry; human reviewers
see both.

### 1. `role` — single

The fundamental kind of thing. Every item has exactly one role.

- `material` — intermediate or ingredient resource (ingots, gems, dusts, plates)
- `natural_resource` — raw/unprocessed resources obtained from the world (ores, raw logs, wheat, feathers, leather)
- `building_block` — blocks whose primary purpose is structure (stairs, slabs, walls, planks, bricks)
- `decorative_block` — blocks whose primary purpose is aesthetics (banners, carpets, paintings, candles, flower pots)
- `functional_block` — blocks with interactive behavior that aren't storage (crafting table, furnace, anvil, jukebox, beacon)
- `storage_block` — chests, barrels, shulker boxes, vaults
- `mechanism` — kinetic / logistics / automation components (Create cogwheels, pipes, belts; pistons; hoppers; Mekanism tubes)
- `redstone_component` — redstone dust, repeaters, comparators, observers
- `tool` — breaking / mining / utility handheld items (pickaxes, shovels, shears, fishing rods, goggles)
- `weapon` — damage-dealing handhelds (swords, axes-as-weapon, bows, crossbows, tridents, maces)
- `armor` — wearable defense / utility (helmets, chestplates, leggings, boots, elytra)
- `consumable` — foods, potions, honey bottles, milk
- `ammunition` — arrows, firework rockets used in crossbow
- `transport` — boats, minecarts, saddles, carpets-on-llama
- `container_portable` — backpacks, shulker boxes used as bags, bundles
- `utility` — compasses, maps, spyglass, name tags, leads, buckets
- `curiosity` — items without clear gameplay role (debug stick, music discs, pottery sherds, banner patterns, smithing templates are better under `upgrade`)
- `upgrade` — smithing templates, trims, apotheosis-style enhancers, sophisticated backpack upgradese
- `trophy` — unique / one-of-a-kind items that players display rather than use (dragon egg, nether star, mob heads, totem of undying)
- `admin` — command blocks, barrier, jigsaw, structure_void

**Resolved:** `trophy` stays a role. Rationale: it fundamentally changes how the item is treated layout-wise (never-used display item), which is a role concern, not a frequency one.

### 2. `activity` — multi

What player activity this item participates in. Multi-value because many items serve
several activities (an iron pickaxe is both `mining` and `exploration`; a redstone torch
is both `redstone` and `building`).

- `mining` — extracting resources from the world
- `combat` — fighting mobs / PvP
- `farming` — crops, animal husbandry, food production
- `building` — placing blocks for structure
- `decorating` — placing blocks/items for aesthetics
- `redstone` — redstone circuits and contraptions
- `automation` — large-scale or mechanized production (Create contraptions, AE2 networks, Mekanism factories)
- `logistics` — item/fluid transport and routing (pipes, belts, funnels, hoppers)
- `storage_management` — sorting, deposit, retrieval
- `exploration` — travel, navigation, light, mobility (ladders, boats, lanterns, elytra, compass)
- `brewing` — potion creation
- `enchanting` — enchanting and book handling
- `trading` — villager trading, emeralds
- `fishing`
- `magic` — mod-specific supernatural mechanics (Botania, Ars Nouveau if present)
- `power_generation` — furnaces, generators, kinetic sources, AE2 energy acceptors
- `transportation` — moving the player (not logistics; this is about player locomotion)

There is no `crafting_ingredient` activity. Ingredient-ness is captured via the new
`processing_in` facet below (specific verbs like `smelting`, `crushing`, `pressing`)
plus `primary_uses` (short LLM-authored summary phrases). A generic "used in recipes"
bit is too noisy to be useful — almost every item qualifies.

### 3. `material_family` — single, nullable (primary) + `material_secondary` — multi, nullable

The material an item is made of or thematically tied to.

`material_family` is the **primary** material — the single material value a player would
most naturally identify the item with. A `create:brass_casing` is primarily `brass`.

`material_secondary` captures additional material associations for composite items.
A `create:brass_casing` has secondary `wood_oak` (its wooden frame). A `mekanism:enriched_alloy`
might have `iron` + `redstone` as secondaries. Most items have zero secondaries.

Both facets draw from the same value list; same scoping rules apply to both. When a
query matches on material, either primary or secondary is a hit unless the query
explicitly says `primary_only`.

**Wood types:** `wood_oak`, `wood_birch`, `wood_spruce`, `wood_dark_oak`, `wood_jungle`,
`wood_acacia`, `wood_mangrove`, `wood_cherry`, `wood_bamboo`, `wood_pale_oak`,
`wood_crimson`, `wood_warped`.

**Stone types:** `stone`, `granite`, `diorite`, `andesite`, `cobblestone`, `deepslate`,
`tuff`, `calcite`, `basalt`, `blackstone`, `sandstone`, `red_sandstone`, `end_stone`,
`purpur`, `prismarine`, `nether_bricks`.

**Metals:** `iron`, `gold`, `copper`, `netherite`. Plus `diamond`, `emerald`, `lapis`,
`amethyst`, `quartz`, `redstone` treated as metal-like for storage-block purposes.

**Organic:** `wool`, `leather`, `bone`, `slime`, `honey`, `scute`.

**Mod metals (examples, not exhaustive):** `brass`, `zinc`, `andesite_alloy` (Create);
`osmium`, `tin`, `lead`, `uranium`, `refined_obsidian`, `refined_glowstone` (Mekanism);
`certus_quartz`, `fluix`, `sky_stone` (AE2).

**Compound themes:** `amethyst`, `dripstone` — borderline, debate inclusion.

**Scoping rule:** a material only gets a family value if it produces ≥3 items. One-off
mod materials don't get their own family; they get `null`. This keeps the list maintainable.

### 4. `form` — single, nullable

Shape / form factor. Crucial for sub-organization within a material family.

- `raw` — `raw_iron`, `raw_copper`
- `ore` — in-world ore blocks (`iron_ore`, `deepslate_diamond_ore`)
- `ingot`, `nugget`, `plate`, `sheet`, `rod`, `gem`, `dust`, `shard`, `crystal`
- `storage_block` — `iron_block`, `diamond_block`, `brass_block`
- `whole_block` — plain full-cube decorative/natural (`planks`, `stone`, `cobblestone`)
- `stairs`, `slab`, `wall`, `fence`, `fence_gate`, `door`, `trapdoor`, `pane`, `pillar`, `pressure_plate`, `button`, `ladder`, `bars`
- `log`, `stripped_log`, `wood` (for 6-sided logs)
- `carpet`, `bed`, `banner`, `sign`, `hanging_sign`, `head`, `pot`, `candle`, `torch`, `lantern`
- `tool`, `weapon`, `armor_piece`
- `food_raw`, `food_cooked`, `potion`, `bottle`
- `bucket` — water, lava, fish, milk
- `projectile` — arrows, fire_charge
- `vehicle` — boat, minecart, chest_minecart
- `seed`, `sapling`, `bulb`
- `special` — everything else

**Resolved:** `stairs`/`slab`/`wall` etc. stay as flat values inside `form`. No
separate `shape_family` facet in V1.

### 5. `tier` — single, free-text, nullable

Progression tier, applicable to tools, weapons, armor, and the materials that produce them.
Null for items outside any tier progression.

**Free-text** rather than a closed enum because mod tier vocabularies diverge wildly
(TerraFirmaCraft has its own rock ages; Mekanism has `basic`/`advanced`/`elite`/`ultimate`;
Tetra has material-based tiers without discrete steps). Forcing a shared enum loses
meaning that's already encoded in each mod's own conventions.

Shared loose vocabulary to encourage consistency where applicable:

- Vanilla tool/armor progression: `wooden`, `stone`, `leather`, `chainmail`, `copper`, `iron`, `golden`, `diamond`, `netherite`
- Vanilla endgame specials: `trial`, `ancient_city`, `elytra_tier`
- Mod tiers use the mod's own vocabulary: `mekanism:basic`, `mekanism:advanced`, `terrafirmacraft:copper_age`, etc.

LLM assigns best-fit tier name from the item's context; schema validation checks for
`^[a-z0-9_]+(:[a-z0-9_]+)?$` only, not membership.

### 6. `origin` — multi, nullable

Where the item is sourced from in the world. Multi-value because many items are
available in several places (gold: overworld_cave + nether + trading + mob_drop).
Mods also add/change sources, so a per-mod classification may *add* origins to an
existing vanilla item rather than replace them.

- `overworld_surface`, `overworld_cave`, `overworld_ocean`, `deep_dark`
- `nether`, `nether_fortress`, `bastion`
- `end`, `end_city`, `end_ship`
- `stronghold`, `woodland_mansion`, `ancient_city`, `ruined_portal`, `pillager_outpost`, `village`, `trial_chamber`, `desert_temple`, `jungle_temple`, `ocean_monument`, `mineshaft`
- `trading` — obtained from villagers
- `mob_drop` — dropped by mobs
- `archaeology_site` — obtained via brushing suspicious sand/gravel
- `sniffer_garden` — obtained via sniffer digging
- `crafted_only` — special marker for items with no world source, only recipes (useful negative filter)

### 7. `rarity` — single

How hard this is to get, from a player's perspective.

- `abundant` — stackable ingredients available by the chest (cobble, dirt, wood, string)
- `common` — readily farmable with infrastructure (iron, wheat, leather)
- `uncommon` — requires exploration or dedicated farms (diamonds, gold ingots, enchanted books, ender pearls)
- `rare` — specific activities, structures, or late-game (nether star, ancient debris, echo shard)
- `unique` — one-shot or near-unique (dragon egg, music disc 13, specific trim templates)

### 8. `frequency` — single, derived

How often a player realistically *uses* this item. Different from rarity — ender pearls
are uncommon but used frequently once obtained.

- `everyday` — touched multiple times per play session (cobblestone, torches, food, basic tools)
- `frequent` — used reliably within its activity context (arrows, buckets, repeaters)
- `occasional` — used when building or doing a specific task (slabs of a particular color, rare dyes)
- `rare` — used in niche situations (beacon, conduit frame components)
- `display_only` — never used after placement (paintings, trophies, banner patterns post-application)

**Derivation idea:** recipe out-degree (how many recipes use this as ingredient) × a usage
prior × presence in tool/weapon/armor slots = frequency bucket. LLM refines.

### 9. `flavor` — multi, nullable

Aesthetic / qualitative attributes. Helps the "variant" and "theme" surfacing.

- `plain` — the "default" version of its block type
- `variant` — alternate version (cut, polished, chiseled, smooth, cracked)
- `fancy` — ornate, detailed models (glazed terracotta, chiseled stone variants, Create icons)
- `ominous` — ominous / cursed / dark-themed (bastion items, ancient city loot, sculk)
- `ancient` — archaeology, pottery sherds, trial chamber decor
- `mystical` — enchanted, soul-themed, Ender
- `mechanical` — tech-look (Create, Mekanism, AE2 visual style)
- `natural` — organic, plant-themed
- `colored` — carries one of the 16 dye colors prominently

### 10. `dye_color` — single, nullable

Strict: one of the 16 vanilla dye colors, assigned only when the item has been
explicitly dyed or built from a dyed variant. Null if the item isn't dyed.
`white_wool` → `white`; `prismarine` → `null` (not a dye color).

`white`, `orange`, `magenta`, `light_blue`, `yellow`, `lime`, `pink`, `gray`,
`light_gray`, `cyan`, `purple`, `blue`, `brown`, `green`, `red`, `black`.

### 11. `palette` — multi, nullable

Broader visual descriptors for items that *read as* a color but aren't dyed. This is
the facet that makes themed-build searches work. Many items belong to multiple palette
entries (prismarine reads as both `teal` and `cool`).

- Hues beyond the dye set: `teal`, `turquoise`, `aqua`, `indigo`, `violet`, `maroon`, `amber`, `olive`, `sage`, `coral`, `ivory`, `mint`
- Metallic / finish: `gold`, `silver`, `copper_bright`, `copper_oxidized`, `iron_dark`, `netherite_dark`, `iridescent`, `glossy`, `matte`
- Natural / organic tones: `wood_light`, `wood_medium`, `wood_dark`, `wood_red`, `wood_pale`, `leaf_green`, `earthy`, `sandy`, `muddy`
- Temperature / mood: `warm`, `cool`, `pastel`, `vivid`, `muted`, `dark`, `light`
- Translucency: `translucent`, `opaque_glass`, `crystal`
- Glow: `glowing`, `emissive`

LLM fills palette. Schema v1 ships with the above vocabulary but it's expected to grow
(likely free-text with a shared vocabulary after feedback).

### 12. `mod_namespace` — single, derived

Free-text: the item id namespace (`minecraft`, `create`, `mekanism`). Trivially derived
but exposed as a facet so queries can scope ("all Create materials").

### 13. `processing_in` — multi, nullable

Which processing / transformation verbs consume this item as input. Replaces the vague
"crafting_ingredient" idea with specific, queryable verbs drawn from vanilla + mod vocabulary.

**Vocabulary is borrowed from EMI's `EmiRecipeCategory` ids**
([VanillaEmiRecipeCategories.java](../../reference/classification/emi_and_plugins/emi/xplat/src/main/java/dev/emi/emi/api/recipe/VanillaEmiRecipeCategories.java)).
Every mod that integrates with EMI registers a stable category id for each process — we
shouldn't invent a parallel vocabulary. Examples:

- Vanilla: `crafting`, `smelting`, `blasting`, `smoking`, `campfire_cooking`, `stonecutting`, `smithing`, `brewing`, `anvil_repairing`, `grinding`, `fuel`, `composting`
- Create: `create:crushing`, `create:pressing`, `create:mixing`, `create:milling`, `create:haunting`, `create:emptying`, `create:filling`, `create:cutting`, `create:sequenced_assembly`
- Mekanism: `mekanism:enriching`, `mekanism:crushing`, `mekanism:smelting`, `mekanism:injecting`, `mekanism:purifying`, `mekanism:infusing`, `mekanism:sawing`, `mekanism:reaction`
- AE2: `ae2:inscriber`, `ae2:crafting_unit`
- Mod-authored verbs follow each mod's own EMI category id.

Derivable from recipe files at extract time; refined at runtime if EMI is present (see
Integration section).

### 14. `primary_uses` — multi, free-text, LLM-authored

Short human-readable phrases summarizing the **top 3–5** reasons a player picks up this
item. The intent is a high-signal semantic summary, not an exhaustive recipe list.

Examples:
- `minecraft:iron_ingot`: `["crafting tools and armor", "building with iron blocks", "anvil repairs"]`
- `minecraft:ender_pearl`: `["teleporting across distance", "crafting eyes of ender", "powering endermites in farms"]`
- `create:brass_ingot`: `["crafting Create mechanisms (brass tier)", "building with brass blocks"]`

Free-text, capped at ~40 chars per phrase. LLM generates; curator reviews. This is the
facet that most directly answers "what is this useful for?" for the player.

### 15. `biome` — multi, nullable

Biomes where this item is naturally found. Only populated for items with biome-specific
sources (azalea bushes, kelp, cherry saplings). Null for items obtained anywhere or
from non-biome sources.

Values follow vanilla biome ids (`minecraft:plains`, `minecraft:lush_caves`, `minecraft:cherry_grove`,
`minecraft:deep_dark`). Mod biomes use their own namespace.

### 16. `y_level_range` — single, nullable

Where in the world height this item is realistically encountered. Coarse enum; precise
y-ranges are available from mcmeta for those that want them but aren't useful for UI.

- `sky` — above ~y=200 (end islands, extreme mountains)
- `surface` — typical build height (y ~60–120)
- `underground` — y ~0–60 (iron, coal)
- `deep` — y ~-64–0 (diamond, deepslate, ancient debris, sculk)
- `nether_surface` — nether y=0–128
- `end_islands` — end

Null for items without a world y-level source.

### 17. `required_tool` — single, nullable

The tool required to harvest or use this item, when it's a block or has a harvest
requirement. Null for non-block items.

- `none` — any hand (dirt, wool)
- `pickaxe`
- `axe`
- `shovel`
- `hoe`
- `shears`
- `any_tool` — anything works (e.g. glass only breaks correctly with silk touch, but any tool yields drop)

When it matters, `required_tool_tier` (free-text, nullable) captures the minimum tier:
`wood`, `stone`, `iron`, `diamond`, `netherite`, or mod-specific.

### 18. `storage_categories` — multi, nullable

What container types / systems can legitimately hold or represent this item. Useful
for suggesting where a player should put something when they pick it up.

- `standard` — normal Minecraft inventory slot
- `fluid` — fluid-only slot (Mekanism / Create fluid tanks; bucket contents)
- `gas` — gas-only slot (Mekanism gas tanks)
- `energy` — energy buffer (FE, Joules, AE energy)
- `ae_storage` — Applied Energistics storage cell content
- `backpack_restricted` — backpack upgrades, storage inputs/outputs
- `curio` — curio / trinket / baubles slot (if those mods are present)
- `pedestal` — items designed to sit on arcane/display pedestals
- `jukebox` — music discs and other jukebox-playable items

Multi-value because the same item can belong to several (e.g. a water bucket is both
`standard` and represents `fluid` content).

### 19. `equip_slot` — single, nullable

Where the item equips if equippable. Null for non-equipment.

- `head`, `chest`, `legs`, `feet`, `main_hand`, `off_hand`
- `curio_<kind>` — when mods like Curios are present: `curio_ring`, `curio_amulet`, `curio_belt`, `curio_back`, `curio_body`, `curio_head`, `curio_charm`, `curio_hands`
- `saddle` — for horse/llama saddles
- `llama_carpet` — specifically decorative llama slot

### Interaction facets (20–25)

The following facets describe **how an item interacts** with the world, with mobs, with
other items, or with the player. These are distinct from identity (what the item *is*) or
activity (what you *do* with it) — they capture mechanical behaviors that players
frequently query against when planning specific builds or encounters.

### 20. `mod_subsystem` — multi, free-text, nullable

Named sub-system within a mod. `mod_namespace: create` is too coarse; many mods have
internal product lines that players think of as distinct kits.

- Create: `create:trains`, `create:logistics`, `create:contraptions`, `create:schematics`, `create:power`, `create:curiosities`
- AE2: `ae2:storage_network`, `ae2:p2p`, `ae2:spatial_storage`, `ae2:autocrafting`
- Mekanism: `mekanism:chemical`, `mekanism:fission`, `mekanism:matrix`, `mekanism:transport`
- Sophisticated: `sophisticated:backpacks`, `sophisticated:storage_tiers`

Multi-value because some items span subsystems (Create train controls are both `trains`
and `logistics`). Free-text with `<namespace>:<subsystem>` convention. Per-mod layers
author these; LLM proposes them during classification when it notices a recurring theme.

### 21. `spawn_interaction` — multi, nullable

How the item/block affects mob spawning, movement, and survival. The mob-farm construction facet.

- `blocks_monster_spawn` — slabs, carpets, glass, fences, trapdoors closed horizontal, leaves
- `allows_spawning` — full opaque blocks where mobs can pathfind and the light level permits
- `damages_entities` — magma, cactus, sweet berry, powdered snow, campfire, crying obsidian (minor), pointed dripstone
- `mob_transport` — water, lava (kills but transports), soul sand (up), bubble columns, scaffolding
- `mob_launcher` — slime block, honey block (slow), bubble column
- `suffocates_mobs` — sand, gravel falling blocks, powdered snow (with leather)
- `repels_mobs` — specific mod items (mob repellent torches etc.)
- `attracts_mobs` — food blocks in breeding contexts, warden-lure items

### 22. `combat_bonus` — multi, nullable

What the item is *specifically* effective against — beyond its generic damage. Captures
enchanted books, tipped arrows, mod weapons with mob-type or boss-type bonuses.

- Vanilla mob-category bonuses: `undead`, `arthropod`, `aquatic`, `illager`, `piglin`
- Boss bonuses: `boss:ender_dragon`, `boss:warden`, `boss:wither`, `boss:elder_guardian`
- Status-effect weapons: `inflicts_poison`, `inflicts_slowness`, `inflicts_weakness`, `inflicts_wither`
- Environmental-context bonuses: `bonus_in_water`, `bonus_in_daylight`

Query: "What's my best weapon against the Warden?" → `combat_bonus has boss:warden`.

### 23. `environmental_property` — multi, nullable

Interaction with world-physics and ambient mechanics.

- Heat / fire: `fireproof`, `lava_safe`, `burnable`, `ignitable_by_fire`
- Blast: `blast_resistant_low`, `blast_resistant_high`, `blast_resistant_max` (obsidian, ancient debris)
- Piston: `piston_movable`, `piston_immovable`, `piston_sticky`
- Sound / sculk: `sculk_silent` (wool), `sculk_noisy`, `warden_distracting` (throwable projectiles, snowballs)
- Piglin reactions: `piglin_pacifying` (gold armor worn), `piglin_barters_with` (gold ingot etc.), `piglin_aggroes_on_open` (chests)
- Weather / temperature: `conducts_lightning`, `melts_in_powdered_snow`, `frost_walker_triggers`
- Movement effects: `slippery` (ice, blue ice), `slows_walking` (soul sand, slime), `bounces` (slime, honey)
- Light: `emits_light`, `emits_light_underwater` (sea lantern, glowstone does not)
- Water: `waterlogs`, `floats`, `sinks`

### 24. `transport_medium` — multi, nullable

What the item moves, for logistics and automation queries.

- `item` — belts, item pipes, funnels, chutes, item conveyors
- `fluid` — fluid pipes, Create fluid pipes, Mekanism mechanical pipes, vanilla cauldrons
- `gas` — Mekanism pressurized tubes
- `energy` — universal cables, FE cables, kinetic shafts, Create encased shafts
- `signal` — redstone dust, bundled cable, Create redstone link
- `player` — elytra launchers, Create contraptions carrying players, minecarts
- `mob` — water streams used to herd, soul-sand elevators, bubble columns

Often multi-value — a Create mechanical arm transports items but the shaft driving it
transports energy. Tag the arm with `item`, the shaft with `energy`.

### 25. `produces_effect` — multi, nullable

Status effects that the item grants when consumed, applied, or triggered. Pulls from the
effect registry; derivable from potion recipes, food components, and `emiffect`-style lang conventions.

Values use effect ids: `minecraft:regeneration`, `minecraft:speed`, `minecraft:night_vision`,
`minecraft:fire_resistance`, `minecraft:water_breathing`, `minecraft:slow_falling`,
`minecraft:luck`, `mekanism:radiation_resistance`, etc. `<namespace>:<effect_id>` convention.

Query: "What food/potion gives me fire resistance?" → `produces_effect has minecraft:fire_resistance`.

### 26. `multiblock_component_of` — multi, free-text, nullable

Named multiblock structures this item is a required component of. Value is
`<namespace>:<multiblock_id>`.

Examples:

- `mekanism:induction_matrix_casing` → `[mekanism:induction_matrix]`
- `mekanism:basic_induction_port` → `[mekanism:induction_matrix]`
- A structural block shared across several multiblocks → `[mekanism:induction_matrix, mekanism:fusion_reactor, mekanism:boiler]`
- `create:fluid_tank` → `[create:fluid_tank]` (self-connective multiblock — any size)

Enables queries like:

- "What do I need to build a fission reactor?" → `multiblock_component_of has mekanism:fission_reactor`
- "What parts are shared between multiblocks?" → items with ≥2 entries
- "Show me all of Mekanism's induction matrix parts" → `multiblock_component_of has mekanism:induction_matrix`

**Data sources are harder than other facets.** Most multiblock definitions live in Java
code, not in jar data files. V1 authoring strategy:

1. **LLM during stage 3** picks up the facet from tooltip lore (e.g. "Part of the Fission
   Reactor multiblock") and item name conventions. Sparse but catches the major cases.
2. **Manual authoring per major mod** for the ~5–10 most common multiblocks in our
   reference mods. Ships as a `<mod>.multiblocks.json` layer authored once per major
   mod version. Low maintenance because these don't change often.
3. **Runtime inspection of preview plugins** (Create's Ponder, Mekanism's render,
   Patchouli books) — speculative V2 research. Worth exploring once the V1 runtime-crawl
   layer is proven.

### 27. `multiblock_role` — single, nullable

Role of this item within any multiblock it belongs to. Applies when
`multiblock_component_of` is non-empty.

- `controller` — primary interaction point; this is the block you right-click to form/control the structure
- `wall` / `casing` — structural, non-functional
- `port` — item I/O interface
- `valve` — fluid/gas I/O interface
- `power_access` — energy I/O interface
- `core` — internal functional component (fuel cells, reactor plates, fusion cores)

Query enabled: "What are the controllers of multiblocks I can build?" → filter
`multiblock_role = controller` and check material availability for their
`multiblock_component_of` partners.

### Deferred: a full multiblocks registry (V2)

The two facets above answer "is this item part of multiblock X?" but not "here are the
components, counts, and arrangement for multiblock X." A full registry —
`<mod>.multiblocks.json` with `{id, display_name, category, controller, components, produces}` —
is a V2 artifact. Required for reverse queries ("show me multiblocks I can build given
my inventory"); not required for the per-item queries V1 focuses on. Flag but don't build.

### 28. Derived boolean facets

Cheap to compute, useful for filtering:

- `is_block_item` — places a block when used
- `is_stackable` — max stack > 1
- `is_fuel` — burns in a furnace
- `has_durability` — damageable
- `has_enchantments` — either is enchanted or accepts enchantments
- `has_nbt_variation` — state varies between stacks (shulker boxes, books, banners)
- `is_creative_only` — only obtainable in creative (admin blocks, debug items)

### Modeling principles

Design rules that apply across the whole schema but bite hardest on the interaction
facets (20–25). Follow these when adding values or extending the schema:

**1. Two-layer vocabulary for cross-mod normalization.** Tag items with both an abstract
cross-mod value AND a mod-specific value where applicable. Example: a Create crushing
wheel is `processing_in: [create:crushing, grinds]`; Mekanism's crusher is
`[mekanism:crushing, grinds]`. Queries can target either layer — "anything that grinds"
hits both, "specifically Create's crusher" hits one.

This is the only cross-mod equivalence our data model captures — EMI itself doesn't
unify recipe-category semantics, only item identity via Minecraft tags, so this layer
of abstraction is a novel contribution.

**2. Enum tiers over raw numbers for ranked queries.** Players rarely ask for an exact
value; they ask "strong enough?" Use graded enum values (`blast_resistant_low/high/max`)
rather than numeric fields. The inverted-index `FacetIndex` gets the queries for free.
Add a numeric facet kind only when ranking is the primary UX (`container_capacity` is
the one V1 candidate).

**3. No parameters in enum values.** `blocks_monster_spawn` ≠
`blocks_monster_spawn_at_light_level_8`. Parameter-laden values destroy queryability.
Collapse conditions into tiers or variant values; if raw parameters are needed, store
them on a separate numeric field.

**4. Stable abstract vocabulary, growing mod-specific vocabulary.** The cross-mod
abstract layer (`grinds`, `blocks_monster_spawn`, `fireproof`) must not churn — queries
depend on it. Mod-specific values (`create:crushing`, `mekanism:chemical`) can grow
freely as new mods are classified. Enforce this by:
- Requiring `schema_proposals` review before any abstract value is renamed or retired.
- Letting mod-specific values appear freely as long as they follow `<namespace>:<token>` form.

**5. `item_ref` facet value kind — deferred but designed for.** Facets like
`grows_from` / `grows_into` (V2) want item-id values rather than enum strings.
Same for future `crafting_companion`, `charges_with`, `parent_item`. The schema
validator distinguishes all eight kinds (`enum` / `multi_enum` / `free_text` /
`multi_free_text` / `boolean` / `numeric` / `item_ref` / `multi_item_ref`) from
day one, even though V1 only populates the first five. Numeric and the item-refs
are reserved but fully validated when used.

**6. Name 3 target queries per interaction facet.** Before closing the value list of
any interaction facet, write down the three queries it should enable. If the value
list doesn't answer them, iterate on the values — not the facet's shape. This is the
strongest discipline against schema sprawl.

**7. LLM confidence intervals as the human-in-the-loop hook.** Interaction facets rely
more on LLM judgement than identity/activity ones. A wrong `combat_bonus` is a worse
failure than a wrong `material_family` because it drives the player toward gameplay
mistakes (axe-of-arthropods vs Warden). Per-entry confidence scores + `ambiguous: true`
flags (already in the stage-3 spec) let a curator triage the risky ones. For the
interaction facets specifically, lean hard on nearest-neighbor priming: include ~20
verified examples of each value in the stage-3 context so the LLM stays consistent
with prior judgements rather than generating fresh interpretations.

### Schema size summary

28 facet types. Closed-enum values: ~460. Free-text facets: `tier`, `mod_namespace`,
`mod_subsystem`, `palette` (schema-vocab-seeded), `primary_uses`, `biome`,
`produces_effect`, `multiblock_component_of`.

Average item carries 8–14 facet entries (most items have no interaction facets; combat, mob-farm, and multiblock items have several).
A modded catalog of ~15k items × 12 entries = ~180k facet entries — trivially small for
in-memory storage.

### What we expect the LLM to contribute

High-leverage, judgement-heavy:

- `role` — the andesite-ladder problem
- `activity` — multi-value, context-dependent
- `frequency` — requires gameplay intuition
- `flavor` — aesthetic judgement
- `palette` — color-reading judgement beyond the 16 dyes
- `primary_uses` — the most important facet for "what is this for?"
- `tier` — for mod items where vanilla tier markers don't apply
- `material_secondary` — composite items
- `storage_categories` — especially curio / pedestal / backpack_restricted values
- `mod_subsystem` — the LLM typically knows the major product lines of big mods
- `combat_bonus` — requires lore knowledge (which enchantments help vs which mobs)
- `spawn_interaction` — requires game-physics knowledge (what blocks monster spawns, what damages mobs)
- `environmental_property` — lore + physics knowledge
- `multiblock_component_of` — reads tooltip/lore evidence ("Part of the Fission Reactor multiblock"); sparse but catches major cases
- `multiblock_role` — follows once `multiblock_component_of` is assigned
- Refining `rarity` / `frequency` / `origin` where the deterministic pass was uncertain

Deterministic extraction handles:

- `material_family` (primary), `form` — via Minecraft item tags (`minecraft:planks`, `c:ingots/iron`) and model-parent resolution
- `dye_color` — via tag + name suffix
- `origin` (partial) — via loot tables where extractable; refined using ALI's loot categorization when available
- `processing_in` — via recipe files; cross-referenced with EMI category ids
- `required_tool`, `required_tool_tier` — via block tags / mining-required tags
- `equip_slot` — via item component data
- `biome` — via world-gen configs directly, or harvested from emi-ores plugin registrations when present
- `y_level_range` — via ore placement configs, or harvested from emi-ores plugin registrations when present
- `produces_effect` — via potion recipes and food components
- `transport_medium` — largely derivable from recipe roles (item pipes appear in item-moving recipes, fluid pipes in fluid recipes)
- `mod_namespace`, all boolean facets

## Pipeline

Tooling: Bun + TypeScript. Reason: fast startup, built-in file IO and JSON parsing, easy
dependency install, excellent for this shape of work.

### Pipeline layout

Directory: `tools/classification/`.

```
tools/classification/
├── package.json             # bun project — name, scripts, deps
├── tsconfig.json
├── layer.schema.json        # wire-format JSONSchema (source of truth)
├── src/
│   ├── cli.ts               # bun entry; argv → pipeline run
│   ├── schema/
│   │   ├── facets.ts        # TypeScript types generated from schema.v1.json
│   │   └── validate.ts      # ajv wrapper around layer.schema.json
│   ├── extract/             # stage 1
│   ├── deterministic/       # stage 2
│   ├── llm/                 # stage 3 (shells out to `claude -p`)
│   ├── neighbors/           # stage 4
│   └── compile/             # stage 5
├── out/                     # generated layer files (gitignored during dev,
│                            # committed when releasing a classification update)
└── test/                    # bun:test suites
```

package.json scripts (minimal):

```json
{
  "scripts": {
    "classify": "bun run src/cli.ts",
    "validate": "bun run src/cli.ts validate",
    "test": "bun test"
  }
}
```

Invocation examples:

```sh
# Full run against vanilla mcmeta, writes out/minecraft.json
bun classify --mod minecraft --source reference/classification/mcmeta

# Per-mod run against a source tree
bun classify --mod create --source reference/classification/Create

# Only stages 1–2 (deterministic; no LLM calls)
bun classify --mod create --source ... --stages 1,2

# Validate an existing layer file against the wire-format schema
bun validate out/minecraft.json
```

### LLM gateway: `claude -p`

Stage 3 shells out to `claude -p <prompt>` (Claude Code CLI in print mode) rather than
using the Anthropic API directly. Design decisions:

- **No API-key management.** The pipeline inherits the maintainer's Claude Code session.
- **Solo-maintainer ownership.** Only the primary maintainer runs the pipeline in
  production. Contributors who want to re-run it locally need their own Claude
  subscription — no shared org key.
- **Not run in CI.** The pipeline is a local development tool. Outputs are committed
  layer JSON files; those are what ship.
- **Model selection** via the `--model` flag on `claude -p` (`haiku-4.5` default,
  escalate to `sonnet-4.6` when stage 3 confidence is low or `ambiguous: true` on
  any facet).
- **Batching.** Stage 3 sends batches of ~20 items per `claude -p` call to amortize
  invocation overhead; context window is plenty large. Each call gets the schema + the
  items' extracted data + nearest-neighbor priming + clear output-format instructions.

Concrete invocation shape (schematic — exact prompt content is stage 3's responsibility):

```sh
claude -p --model=haiku-4-5 --output-format=json <<'EOF'
{
  "instruction": "Classify these items against the Slot facet schema...",
  "schema": { ... },
  "items": [ { "id": "create:cogwheel", "tags": [...], "recipe_role": {...}, ... } ],
  "neighbors": { "create:cogwheel": [ ... ] }
}
EOF
```

The pipeline reads stdout, parses JSON, validates against the wire-format schema,
and feeds results into stage 4. Non-zero exit or parse failure triggers a
Sonnet 4.6 retry for the failing batch.

### Stage 1 — Extract

Input: a mod source tree (like those under `reference/classification/<ModName>/`) OR a jar file.
For vanilla, input is the `reference/classification/mcmeta` repo checked out to its data branches.

For each item the extractor collects:

- `id`, `namespace`, `path`, `display_name` (from lang file)
- `minecraft_tags` — full transitive closure of all Minecraft item tags this item belongs to
- `recipe_role` — outputs of which recipes, inputs of which recipes, total ingredient in-degree and out-degree
- `model_parent` chain — resolved to a top-level shape (`item/generated`, `block/stairs`, `block/slab`, etc.)
- `loot_table_sources` — which loot tables produce this item (if extractable statically)
- `creative_tabs` — which creative tabs reference this item (where data-driven)
- `component_data` — from mcmeta `item_components/data.min.json` for vanilla; from mod code annotations where parseable; otherwise null

Output: `<mod>.items.ndjson`, one line per item.

### Stage 2 — Deterministic facet extraction

For each item record, compute whatever facet entries can be derived with full confidence.
Full list of deterministic sources is in [What we expect the LLM to contribute / Deterministic extraction handles](#what-we-expect-the-llm-to-contribute). Stage 2 implements that half.

Each entry carries `confidence: 1.0` and `source: "rule:<rule-name>"` so stage 3 only
fills gaps and later debug tooling can trace provenance.

Output: `<mod>.facets.partial.json`.

### Stage 3 — LLM-assisted completion

For each item, send to the LLM:

- Id, display name, namespace
- Creative tab membership and Minecraft item tag membership (full closure)
- Recipe role (ingredient-of, output-of, terminal?)
- **Lore / tooltip / flavor text** from the item's lang entries and component data. Mod items
  frequently carry semantic hints here that appear nowhere else (a lore line like
  "Crushes ore when powered by rotation" is the clearest classification signal available).
- **EMI search aliases** if the mod ships an EMI plugin with alias data
  ([EmiAlias.java](../../reference/classification/emi_and_plugins/emi/xplat/src/main/java/dev/emi/emi/data/EmiAlias.java)).
  These are human-authored search keywords and prime `primary_uses` directly — e.g. the
  "heal" alias on healing potions already encodes the semantic summary we want.
- Deterministic facet entries from stage 2 (so the LLM only fills gaps)
- Nearest-neighbor items already classified (top-K by facet-overlap in the extraction data)
  for priming and consistency
- The full schema (facet list + allowed values + descriptions) as context, plus explicit
  instructions to emit `schema_proposals` when it wants a value that doesn't exist

Request the LLM fill in the remaining applicable facets per the schema, emitting per
facet entry: `{value, confidence 0-1, rationale}`.

**Ambiguity policy.** When multiple values could apply to the same single-value facet
with similar confidence (e.g. `role: building_block` vs `role: decorative_block` for cut
copper stairs), the LLM may emit **up to two entries** with low confidence each and a
flag `ambiguous: true`. Downstream tools prefer the highest-confidence entry but can
surface both during human review. Especially valuable in early schema iterations where
human reviewers improve the schema based on which ambiguities keep appearing.

Model: Haiku 4.5 first pass for speed/cost via `claude -p --model=haiku-4-5`.
Sonnet 4.6 re-pass (`--model=sonnet-4-6`) for items where Haiku marks
`ambiguous: true` on any facet or emits overall confidence < 0.4. No CI loop; runs
once per mod version, locally, by the solo maintainer (see [Pipeline layout](#pipeline-layout)
for the `claude -p` rationale). Results cached by `(item_id, mod_version, schema_version)`.

The LLM also emits a side-channel `schema_proposals` list: facets or values it wanted to
use but couldn't find in the schema. Curator reviews offline, rolls approved proposals
into schema v2.

Output: `<mod>.facets.complete.json` — per-item facet entries with confidence and provenance.

### Stage 4 — Nearest-neighbor precompute

For each item, compute its top-20 neighbors by facet-Jaccard similarity across the whole
catalog (including already-classified items from other mods). Store as `neighbors: [item_id, ...]`
on each item record.

Rationale: this is cheap offline, gives us "similar items" functionality at runtime
with zero math, and helps the suggestion system later ("80% of this item's neighbors
live in island X, so X is a likely home"). Dropped from the runtime side of the deferred
vector-search idea — the output of this stage *is* the vector-search cache.

### Stage 5 — Compile & ship

Validate against JSONSchema. Package as `assets/slot/classification/<mod>.json`.
Ship in the mod's resource directory (or as a datapack for community-authored additions).

Community corrections land as PRs against these files. Pipeline is idempotent so a PR
can be regenerated at will.

### Incremental re-runs

When the schema version changes, a migration pass re-runs stage 3 only for items whose
applicable facets have changed. Recipe / tag extraction (stages 1–2) only re-run when the
mod version changes.

## Layering & merging

The most important architectural point in this document. Classification is never a single
file — it's a stack of layers, each contributing or overriding facet entries for items.
Same mechanism handles: mods adding new items, mods overriding vanilla semantics, modpack
curators applying corrections, servers enforcing rules, and **players organizing their own
items** (player-created islands are just another layer).

### Layer order (lowest priority first)

1. **vanilla-base** — bundled classification for `minecraft:*`. Ships with Slot.
2. **per-mod** — one file per mod, shipped as mod resources or datapack. Covers the precomputed modset.
3. **runtime-crawl** — populated at `FacetIndex` build time by walking live game registries. Covers datapack additions, KubeJS/CraftTweaker script modifications, cross-mod tag closure, and any mod without a precomputed layer. **Deterministic facets only** — no LLM judgement at runtime. See [Runtime discovery](#runtime-discovery).
4. **modpack** — curator-authored overrides (`config/slot/classification/modpack.json`). Optional.
5. **server** — server-admin overrides. Shared across all players on a server. Optional.
6. **player** — per-player overrides, stored in worldsave. Player islands live here.

Higher layers override lower layers under explicit merge rules. At `FacetIndex` build time,
layers are merged into a single denormalized view. There is no runtime merging per query.

**LLM-authored facets** (`role`, `primary_uses`, `palette`, `flavor`, `frequency`, and
refined `tier`) only appear in precomputed layers (`vanilla-base`, `per-mod`, `modpack`).
Items classified purely by the runtime-crawl layer will have deterministic facets filled
in but the semantic facets empty — that's the quality floor for unknown mods.

### Layer file format

Every layer uses the same top-level structure. Formal schema:
[tools/classification/layer.schema.json](../../tools/classification/layer.schema.json).

```json
{
  "schema_version": 1,
  "layer": "per-mod",
  "source": "create",
  "generated_by": "slot-classify v0.1.0",
  "entries": {
    "create:cogwheel": {
      "facets": {
        "role": {
          "value": "mechanism",
          "mode": "replace",
          "confidence": 0.95,
          "source": "llm:haiku-4.5"
        },
        "activity": {
          "values": ["automation", "logistics"],
          "mode": "add",
          "confidence": 0.9,
          "source": "llm:haiku-4.5"
        },
        "material_secondary": {
          "values": ["wood_oak"],
          "mode": "add",
          "confidence": 1.0,
          "source": "rule:ingredient-material"
        }
      }
    },
    "minecraft:iron_ingot": {
      "facets": {
        "processing_in": {
          "values": ["create:pressing", "create:crushing"],
          "mode": "add",
          "source": "rule:recipe-ingredient"
        }
      }
    }
  },
  "schema_proposals": []
}
```

**Entry shape by facet kind:**

- **Single-value kinds** (`enum`, `free_text`, `boolean`, `numeric`, `item_ref`) use
  `value: <scalar>`.
- **Multi-value kinds** (`multi_enum`, `multi_free_text`, `multi_item_ref`) use
  `values: [<scalar>, ...]`.
- **Ambiguous single-value** exception: `values: [<a>, <b>]` plus `ambiguous: true`
  may appear on `enum` and `free_text` facets when the producer flagged a tie.

**Optional per-entry metadata** (all kinds):

- `confidence: 0–1` — authoring confidence. `1.0` for rule-derived, <1 for LLM.
- `source: <string>` — provenance string like `rule:<name>`, `llm:<model>`, `runtime:<walker>`, `manual`.
- `rationale: <string>` — free-text reason, mostly from LLM.
- `ambiguous: true` — only on `enum`/`free_text` with a two-element `values` array.

**`schema_proposals` side-channel** (optional top-level array, stage 3 only):

```json
"schema_proposals": [
  { "kind": "add_value", "facet": "activity", "value": "ritual_magic", "rationale": "..." },
  { "kind": "add_facet", "name": "ritual_reagent", "suggested_kind": "multi_enum", "rationale": "..." }
]
```

Curator reviews offline; approved proposals roll into the next schema version.

Notice the second entry in the example: Create's per-mod file can **add** processing
verbs to a vanilla item without touching vanilla-base. That's the common case for
mod-adds-uses-for-existing-item.

### Merge modes

Each facet entry in a layer carries a `mode` that controls how it combines with lower layers:

| Mode | Single-value facet | Multi-value facet |
| --- | --- | --- |
| `replace` | Replace the lower-layer value | Discard lower-layer values; use these |
| `add` | N/A (error) | Union with lower-layer values |
| `remove` | N/A (error) | Subtract these from lower-layer values |
| `override-if-null` | Only apply if lower layers had no value | N/A (error) |

Default mode if unspecified: `replace` for single-value, `add` for multi-value. This
is the intuitive behavior — overrides replace, additions union.

### Player islands as a facet

Player-created islands are modeled as a multi-value facet `player_island`. When the player
drops `minecraft:iron_ingot` into their "My Mining Stuff" island, the player layer for
that world records:

```json
{
  "entries": {
    "minecraft:iron_ingot": {
      "facets": {
        "player_island": { "values": ["my-mining-stuff"], "mode": "add" }
      }
    }
  }
}
```

This unifies player organizing with all other classification sources:

- **Suggestion algorithm is the same** for all layers. "Should new item X go into island Y?"
  becomes "Do X's facet values overlap with the facet values of items already in Y?" —
  regardless of whether Y is a default island or a player-authored one.
- **Persisting player state** reuses the layer JSON format. No new storage concept.
- **Export / share / import** of player islands is just shipping the player layer file.
  (V2 feature — not implemented in V1 but the format is ready.)

### Conflict resolution

When two layers at the same priority produce conflicting single-value entries (e.g. two
mod files both override `role` for `minecraft:iron_ingot`): last-loaded wins, warning logged.
Modpack layer exists partly to resolve these deterministically.

When a player layer and a lower layer conflict (player puts `iron_ingot` in `my-trophies`
while all classification says it's `material`): player layer always wins — never override
the player's intent. This aligns with the existing "re-home is intentional" invariant.

### Provenance tracking

Every merged facet entry retains provenance (which layer it came from) in the
`FacetIndex`. Useful for:

- Debug / "why is this item here?" tooltip
- Audit: "the reason iron_ingot shows in my smelting island is that Create's per-mod
  layer added `processing_in: mekanism:smelting`"
- Future UI: let players see and revoke specific overrides

### Resource-location matrix

Where does each layer file actually live on disk? At `FacetIndex` build time the
runtime walks a fixed set of locations in priority order.

| Layer | Location | Access | Authored by |
| --- | --- | --- | --- |
| `vanilla-base` | `assets/slot/classification/minecraft.json` in the Slot mod jar | Classpath resource | Pipeline output; checked into the Slot repo |
| `per-mod` (shipped) | `assets/slot/classification/<mod>.json` in Slot's jar | Classpath resource | Pipeline output for mods we support out-of-box |
| `per-mod` (datapack) | `data/<ns>/slot_classification/<mod>.json` in any loaded datapack | `ResourceManager` at datapack reload | Community contributors; pack authors |
| `runtime-crawl` | (none — synthesized at runtime, not a file) | Built from live registries when `FacetIndex` rebuilds | Always-on runtime code |
| `modpack` | `config/slot/classification/modpack.json` | Filesystem, read at game start + on `/reload` | Pack maintainer |
| `server` | `config/slot/classification/server/*.json` (any number of files, merged in filename order) | Filesystem, server-side only | Server admin |
| `player` | `<worldsave>/slot/classification/player/<player-uuid>.json` | Worldsave data, written as player organizes | The game, on player action |

**Load precedence and when merging happens:**

- All file-backed layers are re-read on `/reload` (vanilla Minecraft datapack reload hook).
- The runtime-crawl layer rebuilds whenever any upstream registry (item, tag, recipe, loot, placed-feature) is reloaded.
- The player layer is persisted every time its contents change and reloaded at the same cadence as the other file-backed layers. In multiplayer, the player layer is worldsave-side; clients receive their effective `FacetIndex` via the Slot sync channel the mod already uses for workflow state.
- Schema-version mismatches: see [Versioning layers](#versioning-layers) below — incompatible files log a warning and are ignored; compatible-but-older files are accepted with dropped-facet warnings.

### Versioning layers

Each layer declares `schema_version`. At merge time:

- If a layer declares a schema version lower than the bundled schema, entries are
  accepted but any facets/values retired in newer schemas are dropped with a warning.
- If a layer declares a higher schema version, the entire layer is ignored (we can't
  validate its invariants).
- Layers can be forward-migrated by the pipeline when schema bumps.

## Runtime

New component: `dev.imagio.slot.classification.FacetIndex` in the `common` module.

- At mod init: discover all classification layer files (mod resources, `config/slot/classification/`,
  worldsave player layer), validate against the bundled schema, merge in priority order.
- Stores inverted indices: `Map<FacetKey, Map<FacetValue, BitSet<ItemOrdinal>>>` plus a
  per-item denormalized record including provenance.
- Query API: `index.query(Expr)` where `Expr` is a tiny AST (`and`, `or`, `not`, `eq`, `in`, `has`).
- Per-item lookup: `index.facets(itemId): ItemRecord`.
- Fallback: if an item has no facet entries from any layer, fall through to the existing
  [SemanticBucketResolver.java](../../common/src/main/java/dev/imagio/slot/debug/SemanticBucketResolver.java)
  so brand-new mods / datapacks / custom items still get a reasonable answer.

### Homing rule (V1)

Islands in V1 continue to be single-home per item. No presets, no user-facing switching.
Exactly one rule, hardcoded:

1. If the item has any `player_island` entry, its home is the first player island listed.
2. Otherwise, its home is the island that corresponds to the item's `role` value
   (one island per role: `mechanism-island`, `mining-island`, `building-island`, etc.).
3. If the item has no `role` (classification gap), fall through to
   `SemanticBucketResolver` for a best-effort bucket.

No priority list, no "default island map" as a separate artifact — just `role`. The
bundled roles (~20 values) map 1:1 to islands. That's close enough to the current
bucket layout to be a drop-in replacement and simple enough to not require a whole
authoring pass.

### Why this is intentionally underbuilt

The single-home-per-item invariant is likely to be replaced in the next iteration with
something richer (multi-home suggestions, ranked candidates, islands-as-queries, or
similar — exact shape TBD). The Runtime code above is a stopgap designed to prove the
facet layer works and keep the UI unchanged. Don't invest in sophisticated homing
heuristics right now; they'll be thrown away.

### Why no presets in V1

User feedback: keep UI as-is, only use facet data for suggestions for now. Adding preset
switching adds UI and migration complexity for no V1 benefit. The facet infrastructure
is forward-compatible — presets become a layer of "facet priority lists + island maps"
whenever we want them.

## Runtime discovery

Precomputed layers cover the known modset at pinned versions, with full LLM-authored
facets. They do not cover:

- **Datapack additions and overrides** — a pack maintainer or server can add recipes, override loot tables, introduce tags, and modify worldgen without touching any jar. Our per-mod scan can't see these.
- **Script mods (KubeJS, CraftTweaker)** — these modify recipes, tags, and registries at runtime from user-authored scripts. The data literally doesn't exist until the game boots. **This is the common case in big modpacks**, not an edge case.
- **Cross-mod tag closure** — `c:ingots/iron` gets contributions from vanilla + every mod that registers an iron-like item. Per-mod precompute sees one contribution; full closure requires merging at load.
- **Custom `EmiPlugin` registrations** — pure Java code, only observable live.
- **Unknown mods** the player has loaded that we haven't run the pipeline against.

The runtime-crawl layer addresses all of these. It runs once per world load, walks the
live game registries, and produces facet entries that merge into the stack.

### Direct registry crawl (always-on)

The always-on baseline. Reuses the pattern [emi-ores](../../reference/classification/emi_and_plugins/emi-ores/)
uses for modded-ore support: iterate the vanilla dynamic registries and extract facets
from whatever's there, without any per-mod code.

For each item in the fully-resolved item registry, we can derive at runtime:

| Facet | Source |
| --- | --- |
| `mod_namespace`, `is_block_item`, `is_stackable`, `is_fuel`, `has_durability`, `has_enchantments` | Item registry + item component data |
| `material_family` (primary) via tag | Fully resolved tag closure from `TagManager` |
| `form` | Tag closure + block class hierarchy |
| `dye_color` | Tag + registered color component |
| `equip_slot` | `Equipable` / item component |
| `processing_in` | `RecipeManager` walk, keyed by `RecipeType` — covers every vanilla-type recipe including those added by KubeJS |
| `origin` (loot sources) | `LootDataManager` walk over chest/mob/block/gameplay loot tables |
| `biome`, `y_level_range` (ore items) | `PlacedFeatures` + `ConfiguredFeatures` dynamic registry walk |
| `required_tool`, `required_tool_tier` | Block tags + `DiggerItem` inspection |

Implementation sketch: a `RuntimeCrawlSource` that runs on `onResourceReload` / first
`FacetIndex` build per world. Output is a synthetic layer merged at the `runtime-crawl`
slot. Deterministic-only — produces no LLM-authored facets. An unknown mod's items will
have all the derivable facets populated but nothing for `role`, `primary_uses`, `flavor`,
`palette`, `frequency`.

### EMI core as an optional enrichment

When [EMI core](../../reference/classification/emi_and_plugins/emi/) is installed, the
runtime-crawl layer can tap additional sources that EMI has already resolved:

- **Recipe categories** via `EmiRecipeCategory` registrations — includes *custom* categories (Create's crushing, Mekanism's enriching, etc.) that our own `RecipeManager` walk wouldn't recognize, because those require per-mod `EmiPlugin` code.
- **Search aliases** via [EmiAlias](../../reference/classification/emi_and_plugins/emi/xplat/src/main/java/dev/emi/emi/data/EmiAlias.java) — high-quality semantic keywords for `primary_uses` priming.
- **Tag-exclusion and adapter data** that EMI has pre-resolved.

EMI complements the direct crawl rather than replacing it — it specifically fills in the
"custom recipe categories" gap that direct `RecipeManager` walks miss.

### EMI plugin ecosystem as pre-classified facet sources

The EMI plugin ecosystem is effectively a distributed, community-maintained
pre-classification system. Each plugin registers `EmiRecipeCategory` instances that
encode "item X belongs to category Y" for specific aspects. Membership detection
becomes "is this item an output/input of the category?" — no re-derivation needed.

Specific high-value plugins we should consume when present (all under
[reference/classification/emi_and_plugins/](../../reference/classification/emi_and_plugins/)):

| Plugin | Facet it feeds | What it provides |
| --- | --- | --- |
| [emi-ores](../../reference/classification/emi_and_plugins/emi-ores/) | `y_level_range`, `biome` | Directly reads worldgen configs for ore placement, including modded ores. Eliminates the need for us to parse `placed_feature` / `configured_feature` JSONs ourselves. |
| [EMITrades](../../reference/classification/emi_and_plugins/EMITrades/) | `origin` (`trading`) | Full villager-trade mapping with profession context |
| [EMIProfessions](../../reference/classification/emi_and_plugins/EMIProfessions/) | Workstation-block context | Profession → workstation block metadata |
| [EMI_loot](../../reference/classification/emi_and_plugins/EMI_loot/) | `origin` (chest/mob/block) | Loot drops per source; redundant with ALI, use whichever is present |
| [JustEnoughBreeding](../../reference/classification/emi_and_plugins/JustEnoughBreeding/) | `primary_uses` for food | Maps food item → breedable animals ("breeds cows, sheep") |
| [jearchaeology](../../reference/classification/emi_and_plugins/jearchaeology/) | `origin` (`archaeology_site`, `sniffer_garden`) | Sniff/brush loot categorization |
| [emiffect](../../reference/classification/emi_and_plugins/emiffect/) | `primary_uses` for potions/foods | Status-effect descriptions from `effect.<mod>.<name>.description` lang convention |
| [extra-mod-integrations](../../reference/classification/emi_and_plugins/extra-mod-integrations/) | `processing_in` | EMI category bridges for Actually Additions, Chipped, Farmer's Delight, Iron's Spells, PneumaticCraft, Rechiseled, Reliquary, Tech Reborn |

Crucially, this means **any mod with EMI plugin coverage gets basic classification
automatically**, even without a precomputed per-mod layer. Precomputed layers still
win when available (they include LLM-authored facets EMI doesn't have), but the
floor is meaningfully higher.

### AdvancedLootInfo as a signal provider for `origin`

When [ALI](../../reference/classification/emi_and_plugins/AdvancedLootInfo/) is
installed, its `BlockLootCategory` / `EntityLootCategory` / `GameplayLootCategory` /
`TradeLootCategory` data already answers the "where does this come from?" question
that our `origin` facet captures. Read ALI's synced loot tables at runtime rather
than re-parsing them ourselves.

### Pattern worth stealing: lang-file semantic conventions

EMIffect's convention `effect.<mod>.<name>.description` is an example of mods
encoding semantic data in lang files via well-known keys. Worth scanning each mod's
lang files for other conventions: JEI/REI have similar patterns for recipe-type
descriptions. Cheap extract-time enrichment.

### Non-goal: our own in-game item search UI

If we later want fuzzy query UI over facets, reuse EMI's search infrastructure
([EmiSearch.java](../../reference/classification/emi_and_plugins/emi/xplat/src/main/java/dev/emi/emi/search/EmiSearch.java))
or ship a slot-specific EMI plugin that exposes our facets as queryable tokens.
Don't re-solve suffix-array search or write a new query parser. EMI's query grammar
(`@mod #tag $tooltip`, regex, quoted, AND/OR) is a well-designed prior art if we
ever need our own.

### Dependency stance

EMI / ALI are **optional**. The precomputed per-mod classification layer must work
without them. These are enrichments that improve coverage for mods we don't ship
precomputed files for.

## Test strategy

One-liner per component. Concretely written so "does this have tests?" is answerable.

### Pipeline (Bun/TS in `tools/classification/`)

- **Stage 1 (Extract)** — per-parser unit tests using fixture jar directories under `test/fixtures/mod-*`. Assert NDJSON lines match expected shape.
- **Stage 2 (Deterministic)** — per-rule unit test. Each extractor (e.g. `material_family_from_tag`) gets hand-crafted input and expected facet entries. Keep rules pure functions; test at that boundary.
- **Stage 3 (LLM)** — not unit-tested (non-deterministic by design). **Replay-based integration test** instead: `test/fixtures/stage3-replay/` holds recorded `claude -p` responses keyed by prompt hash; the test runs stage 3 with the LLM call swapped for the replay fixture. Assert that known items end up with expected facets, that `ambiguous: true` triggers on the canary ambiguous-items set (e.g. cut copper stairs), and that `schema_proposals` is emitted when expected.
- **Stage 4 (Neighbors)** — fixture-based test: feed a hand-authored facet set, assert top-K neighbors match expected item ids.
- **Stage 5 (Compile)** — round-trip test: compile → validate against `layer.schema.json` → re-parse → deep-equal.
- **Wire-format validator** — exhaustive invalid-input suite (wrong kinds, wrong modes, forbidden fields, pattern mismatches).
- **End-to-end golden files** — `test/golden/minecraft.json` checked in; any pipeline change that alters its output requires an explicit golden update and a changelog note.

### Runtime (Java in `common/src/main/java/dev/imagio/slot/classification/`)

- **`FacetIndex` loader** — test each layer kind loads cleanly, schema-mismatches are rejected/accepted per policy, and malformed entries don't crash the loader.
- **Merge engine** — per-`mode` tests: `replace`, `add`, `remove`, `override-if-null`. Plus the conflict cases: same-layer duplicate, player-layer-wins.
- **Query engine** — fixture index + canonical queries: `role = mining-tool`, `activity has mining AND material_family = iron`, etc.
- **Homing rule** — given fixture item, assert home island. Cover: has-player-island, has-role, has-neither.
- **Runtime-crawl** — integration test with a minimal in-memory registry: one vanilla item, one tag, one recipe, one loot table. Assert produced layer has expected entries. More comprehensive integration via the testinstance mod setup.
- **Player layer persistence** — create island, assert serialization; reload, assert round-trip; mutate, assert re-serialization.
- **Shadow-mode divergence logger** (per milestone 7) — test that when `FacetIndex` and `SemanticBucketResolver` disagree, divergence is logged with both results attached.

### Schema + docs

- **Schema validator** — linter that loads `schema.v1.json` and verifies every facet declares a valid kind, that enum value lists are non-empty, that regex patterns compile.
- **Cross-check** — test that every facet referenced in `SemanticBucketResolver`'s fallback logic exists in the schema.
- **Link check** — all inter-doc markdown links resolve (cheap CI step once a link-check action is wired up).

### What we explicitly *don't* test

- LLM output quality (playtesting decides — see milestone 5 goal language).
- Specific facet values for specific items (brittle; the schema shape is tested, the content is validated by human review).
- Performance. Mentioned as a pitfall — revisit when we have a concrete budget.

## Schema evolution

- Facet schema file: `common/src/main/resources/slot/classification/schema.v<N>.json` — the list of facets, their kinds, and their permitted values.
- Wire-format schema: [`tools/classification/layer.schema.json`](../../tools/classification/layer.schema.json) — the JSONSchema that validates layer files.
- Each classification layer file records `schema_version` in its header and is validated against both at load time.

### Breaking vs non-breaking changes

**Non-breaking** (bump minor: `v1.1 → v1.2`):

- Adding a new facet.
- Adding a value to an `enum` or `multi_enum` facet.
- Widening a regex on a `free_text` / `multi_free_text` facet.
- Widening a numeric range.
- Marking a facet `deprecated` (it still works but new runs don't populate it).

Existing layer files remain valid. The next pipeline run may produce richer output.

**Breaking** (bump major: `v1.x → v2`):

- Removing or renaming a facet.
- Removing a value from an `enum` / `multi_enum`.
- Narrowing a regex.
- Changing a facet's kind (e.g. `enum → multi_enum`).
- Changing a facet's cardinality.
- Changing the default `mode` for a facet.

Breaking changes require:

1. A migration path for each affected facet documented in `CHANGELOG.md`.
2. A regeneration sweep of all checked-in layer files (pipeline re-run against vanilla + each per-mod target).
3. A migrator in the `FacetIndex` loader that either rewrites old entries on the fly or rejects the old file with a loud error — per-facet choice documented in the changelog.
4. For the **player layer specifically**: facet-rename migrators must preserve `player_island` values (rename the facet id without dropping entries). A deliberate choice to drop player data on a schema bump requires an explicit in-game prompt ("your island assignments for these N items were lost in a schema change; re-home them?"), never silent loss.

### Changelog policy

A `CHANGELOG.md` lives next to `schema.v1.json` and records every schema change with:

- Version bump (minor/major).
- Changed facets, listed one per line.
- Rationale.
- Migration path for each breaking change.

The pipeline refuses to run against an uncommitted schema change (dirty schema file
without a changelog entry). CI (once present) enforces the same rule.

### In-runtime behavior

- `FacetIndex` only loads layer files where `layer.schema_version`'s major matches the bundled schema's major. Mismatch = warning + ignored.
- Minor mismatch: layer loads, unknown facets/values are dropped with a one-line warning per facet.
- Player-layer mismatches block only the affected entries, never the whole layer; the game always ships with a forward-migrator for the player layer.

## Milestones

1. **Schema v1 freeze.** Iterate on the facet list in this doc until satisfied. No code. *(In progress — schema still accepting revisions; `CHANGELOG.md` not yet created since no versions have shipped.)*
2. **Layer file format spec + JSONSchema**, including merge modes. *(Done: [layer.schema.json](../../tools/classification/layer.schema.json), facet kinds spec'd in this doc.)*
3. **Extractor** (stage 1, Bun/TS) against vanilla only. Output `minecraft.items.ndjson`. *(Done: [tools/classification/src/extract/vanilla/](../../tools/classification/src/extract/vanilla/). Run with `bun classify --mod minecraft --source ../mcmeta`; produces one record per item with tags, recipe role, loot sources, model chain, and component data.)*
4. **Deterministic facet extractor** (stage 2) against vanilla. Measure coverage — what fraction of items have each facet assigned deterministically? *(Done: [tools/classification/src/deterministic/](../../tools/classification/src/deterministic/). Rules cover `mod_namespace`, `material_family`, `form`, `dye_color`, `equip_slot`, `required_tool`/`required_tool_tier`, `processing_in`, `origin`, `rarity`, and the boolean facets. Vanilla coverage at a glance: `mod_namespace`/`rarity` 100%, `is_stackable` 84%, `is_block_item` 68%, `required_tool` 53%, `processing_in` 46%, `form` 43%, `material_family` 34%, `origin` 22%, `dye_color` 13%, plus smaller slices for `equip_slot`/`has_durability`/`has_enchantments`/`has_nbt_variation`. Everything else waits for stage 3.)*
5. **LLM completer** (stage 3) against vanilla. Manual spot-check the output on ~100 items the team has opinions on. Goal: it feels meaningfully better than the current resolver in playtesting — no formal match-rate target.
6. **Nearest-neighbor precompute** (stage 4) and compile to layer-format JSON (stage 5).
7. **Runtime `FacetIndex`** — layer loading + merging + queries + lookup. Wire it into the atlas generator behind a feature flag. Regression-check current tests still pass.
8. **Runtime-crawl layer** — direct registry walk to populate deterministic facets for items without precomputed entries. Ship without EMI/ALI enrichment first; add those as follow-ups once the base crawl is proven.
9. **Player-island layer** — persist player island assignments as a player layer; read back into `FacetIndex`. Exercise the merge path end-to-end.
10. **Expand to Create** as the first modded target. Identify schema gaps and merge-mode edge cases the vanilla-only run didn't reveal.
11. **Add remaining reference mods.** One per iteration, catching schema holes each time.
12. **EMI / ALI enrichment** on the runtime-crawl layer — optional integrations that lift quality for mods without precomputed layers (custom recipe categories, loot origin, breeding, archaeology). Ships independently from milestone 11.
13. **Ship** classification layers as mod resources / datapacks. `FacetIndex` becomes the default source of homing suggestions; `SemanticBucketResolver` becomes the fallback only.

Stop after milestone 9 and review before expanding to modded targets. If V1 feels right,
continue with mod expansion. If the abstraction isn't working, rewind.

## Open questions

### Resolved

- **Trophy** — role.
- **Tier** — free-text with shared vocabulary.
- **Origin** — multi-value, including `crafted_only` negative marker.
- **Material family** — primary single + secondary multi, same vocabulary.
- **Crafting ingredient** — not a facet value; replaced by `processing_in` + `primary_uses`.
- **Form shapes** — stay flat inside `form`, no separate `shape_family` facet.
- **Color** — strict `dye_color` (16 values) + broader `palette` (multi). `palette` ships as closed enum but the decision is revisitable after one iteration round.
- **Presets** — not in V1. One hardcoded homing rule.
- **Derivation rules in JSON** — no. Code only.
- **Nearest neighbors** — in V1 (stage 4, precomputed).
- **LLM ambiguity policy** — up to two low-confidence entries with `ambiguous: true`.
- **Player islands** — modeled as the `player_island` multi-value facet in a per-world player layer. Same merge machinery as mod overrides.
- **Mod overrides / merging** — explicit layer system with `mode` per entry. Player layer always wins.
- **Default island map** — no separate map. Homing is just `role → one-island-per-role` plus the `player_island` shortcut.
- **Server layer** — included in the layer stack for completeness, but classification isn't gameplay-authoritative, so priority details don't matter much. Current order (modpack < server < player) stands unless a concrete scenario pushes back.
- **Datapack support** — both paths. Community per-mod layers can ship as Slot resources or as datapacks, whichever the contributor finds easier.
- **Load-time merge** — all layers merged at init. No lazy merging. Cross the "worldsave write is hot" bridge only if it actually becomes hot.
- **Mod uninstall** — invalid entries (player_island for items whose mod is gone) are preserved silently; they re-activate if the mod returns. Tests required.
- **Match-rate measurement** — no formal protocol. Human playtesting decides whether it feels good enough. Milestone wording updated accordingly.
- **API-key ownership** — solo maintainer. Pipeline uses `claude -p` (Claude Code CLI) rather than the Anthropic SDK directly. Contributors who re-run it locally need their own Claude subscription. No CI runs the LLM; outputs are committed.
- **Pipeline layout** — `tools/classification/`, Bun + TypeScript, skeleton spec'd in [Pipeline layout](#pipeline-layout). Implementation starts at milestone 3.
- **Resource-location matrix** — spec'd in [Resource-location matrix](#resource-location-matrix).
- **Facet kind system** — spec'd in [Facet kinds](#facet-kinds). 6 kinds in V1 use (`enum`, `multi_enum`, `free_text`, `multi_free_text`, `boolean`) plus `numeric` reserved for first numeric facet; 2 reserved for V2 (`item_ref`, `multi_item_ref`).
- **Layer file JSONSchema** — committed at [`tools/classification/layer.schema.json`](../../tools/classification/layer.schema.json).
- **Schema changelog policy** — spec'd in [Schema evolution](#schema-evolution). CHANGELOG.md file is required alongside the schema; pipeline refuses to run against a schema change without a changelog entry.

### Still open

1. **Whether the single-home invariant survives.** Strongly expected to be replaced by a richer model (multi-home ranked suggestions, islands-as-queries, or similar) in the iteration after this one. Keep the V1 homing code throwaway.
2. **Palette: closed vs free-text after round 1.** Ship closed; let `schema_proposals` collect LLM's wants; re-decide after the first round of real mod runs.
3. **Player-island inference.** When a player puts 5 items in an island that all share `material_family: wood_oak`, should the system offer to add a query-rule to that island? Deferred to V2 but the layer format should be able to express it. Minimal player-driven signal learning now; bigger pass when singular-home resolves.
4. **Ship an EMI plugin exposing our facets?** Would let EMI users query by `#palette:teal` or `@role:mechanism` from EMI's search bar. Good surface for power users, zero marginal cost once `FacetIndex` is in place. V2 consideration.
5. **Precomputed-vs-runtime conflict.** When EMI reports `processing_in: create:milling` for an item but our precomputed per-mod layer doesn't, who wins? Probably union (both are additive signals for a multi-value facet), but explicit conflict rules needed for any single-value facet where this overlaps.
