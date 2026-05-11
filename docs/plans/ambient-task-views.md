# Ambient Task Views Plan

Last updated: 2026-05-10

Status: deferred/research. This plan captures the generic task-view idea so it
does not block the more actionable work in
[classification-facet-vocabulary.md](classification-facet-vocabulary.md) and
[emi-goal-projections.md](emi-goal-projections.md).

Ambient task views are reusable workflow lenses such as Cooking, Exploration,
Mining, Building, Farming, Combat, Cleanup, and Ore Processing. They answer:
"show me relevant things I already have or know about for this activity."

They do not answer: "what do I need for this specific recipe?" That belongs to
EMI goal projections.

## Why Deferred

This overlaps with Kits and needs more product thought before implementation.

- Kits are explicit task packages with hotbar/loadout behavior and protection.
- Ambient views are read-only projections over current authority.
- Goal projections are recipe-derived and desired-count-aware.

Until these boundaries are playtested, ambient task views should stay a data/UI
research track rather than an implementation dependency.

## Candidate Generic Tasks

### Cooking / Food Prep

Shows edible food, ingredients, bowls/vessels, preservation supplies,
spices/sauces, and cooking-related tools. Uses food facets and TFC/FirmaLife
tags once the facet-vocabulary work lands.

### Exploration / Trip Prep

Shows food, light, weapons, tools, spare containers, navigation-like items,
bed/shelter supplies, repair supplies, and likely trip gear. This is a pressure
and preparation view, not a recipe goal.

### Mining / Prospecting

Shows picks, prospecting tools, supports, light, ladders/ropes where present,
ore samples, containers, and food. May later narrow by target material.

### Building / Base Work

Shows blocks, scaffolding, doors, lighting, workbench-like utility items,
decoration, and storage blocks.

### Storage / Cleanup

Shows recent pickups, triage/unhomed items, containers, crates, labels/upgrades
where present, and overflow candidates. This may overlap heavily with existing
Recent, Triage, and learned-storage concepts.

### Farming / Husbandry

Shows seeds, crops, soil/fertilizer, tools, animal feed, buckets/vessels,
produce, and breeding-related items.

### Combat / Defense

Shows weapons, armor, shields, ammo, food, healing, lighting, barriers, and
defensive building materials.

### Ore Processing / Metallurgy

Shows ores, crushed/dust forms, molds, ingots, sheets, fuels/fluxes, relevant
tools, and stations. This likely needs early parameters such as material and
process stage.

### Automation / Machines

Shows machine blocks, wires/cables, pipes, circuits, motors, covers, tools,
fluids/containers, and related parts. This will be noisy without strong
workflow and subsystem facets.

## Product Rules

- Ambient task views show relevant things the player has or has known storage
  ghosts for.
- They should not create missing ghosts or desired counts unless backed by a
  concrete EMI goal projection.
- They should not mutate homes or influence deposit routing.
- They should not become a permanent wall of buttons.
- They should be available as contextual suggestions, search/mode chips, or a
  compact overflow, not as the primary navigation model.
- They should use the same wall/card/pip language as the rest of SLOT.

## Data Dependencies

Ambient views depend on:

- `activity`
- `workflow`
- `workflow_role`
- `organization_group`
- domain facets such as `food_category`, `preparation_state`,
  `material_process_stage`, and `process_material`
- player overrides such as "show this in this view" / "hide from this view"

Because the data quality is not ready yet, implementation should wait until the
facet-vocabulary plan produces useful coverage for at least Cooking and one
metal/process workflow.

## Open Questions

- Are ambient views meaningfully different from Kits in daily play?
- Should ambient views be tabs, chips, search prefixes, or contextual
  suggestions?
- Which generic views are actually useful after EMI goal tabs exist?
- How should broad views avoid becoming noisy catch-all categories?
- Should player corrections attach to facets, view membership, or both?

