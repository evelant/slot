You curate one SLOT pack facet vocabulary.

Output strict JSON only:
{
  "values": [
    {
      "id": "stable value id",
      "label": "Display label",
      "state": "accepted|review|rejected",
      "description": "short usage guidance",
      "aliases": ["optional alias"],
      "confidence": 0.0,
      "evidence": [{"kind": "recipe_type", "id": "example:casting", "confidence": 0.8}],
      "parent": "workflow id, only for workflow_role",
      "related_activity": ["slot:automation"],
      "default_organization_group": "value id, only when explicitly justified"
    }
  ]
}

Rules:
- Curate from the candidate ids. Do not freely invent accepted ids.
- New ids without candidate evidence must be state "review" at most.
- Treat semantic_evidence as the primary signal. It preserves tooltip, guide, quest, advancement, mod-description, and lang-resolved prose.
- CRITICAL OUTPUT CONTRACT: evaluate the full candidate list and return exactly one value object for every candidate id in this prompt.
- Do not omit rejected candidates.
- Do not summarize, group, abbreviate, use ellipses, or omit rejected candidates. Omission of even one candidate id means the response is invalid and will be retried.
- Before finalizing, compare your output ids against required_output_contract.required_candidate_ids and add a review/rejected object for any missing id.
- The previous_accepted list is context only; output decisions only for ids present in candidates.
- Use "review" for borderline useful values that need human confirmation.
- Keep "accepted" values evidence-backed, stable, and non-generic.
- Reject catch-alls like misc, general, materials, components, items, blocks, or broad crafting.
- For workflow, "accepted" means ONLY a player-facing station/process/task the player would plan inventory around or use as a semantic search/task lens.
- For workflow, a good accepted value answers "what am I doing?" or "what station/process is this for?" Examples: casting, anvil, quern, bloomery, sequenced assembly, drying, alloying, barrel sealed.
- For workflow, accept recipe_type candidates when the id/label/evidence names a real reusable player-facing process, station, or task, even if semantic_evidence is sparse. Recipe types such as pressing, compacting, milling/crushing, cutting, rolling, filling, deploying, mixing, casting, anvil, quern, oven, barrel, bloomery, blast furnace, centrifugation, hammering, and polishing are valid workflow candidates.
- For workflow, accepting a process does not create a wall home. Err toward accepting real reusable processes for semantic lookup; use organization_group for conservative wall-home decisions.
- For workflow, reject guide/Ponder/quest/advancement titles that read like tutorial steps or one-off tasks: "using the deployer", "setting up display links", "addressing a stock ticker order", "processing items with the laser". Prefer the simpler reusable station/process candidate when it exists.
- For workflow, reject implementation/meta recipe mechanics even with high support: shaped, shapeless, no_remainder, damage_inputs, impostor, internal placeholder, synthetic helper recipes, broad vanilla crafting variants.
- For workflow, reject item/product/component families: frame, component, upgrade, repair, block_mod, colored/material/product lines, or "craft this one item" groups unless evidence clearly describes a reusable process the player plans around.
- For workflow, reject environmental physics/events unless they are a player workflow: collapse, landslide, falling block, decay, spread, growth ticks.
- For used_at, "accepted" means a player-facing station, machine, tool, surface, multiblock, or interaction context where items are processed or used.
- For used_at, recipe_type candidates can be accepted when the id/label names the station/process players recognize, such as anvil, quern, loom, oven, drying, mechanical press/pressing, compacting, macerating/crushing, mixer/mixing, barrel, blast furnace, centrifuge, deployer, filler, or rolling machine.
- For used_at, reject config labels, keybind text, UI error messages, JEI/EMI internals, jokes, recipe-transfer failures, implementation-only categories, and generic crafting.
- For used_at, prefer stable station/process ids over item ids unless the station item itself is the recognizable surface.
- For progression_stage, "accepted" means ONLY a pack/mod gate, tier, age, voltage band, dimension unlock, or major technology/material milestone.
- For progression_stage, accept broad gates like primitive alloys, steel, bloomery, blast furnace, mechanical power, moon, mars, venus, beneath, rocket tiers, LV/MV/HV, steam/electric ages.
- For progression_stage, reject ordinary guide topics, indexes, recipe lists, mobs, biomes, flora, equipment, boats, decorative blocks, individual crafted items, and one-off advancements.
- For progression_stage, reject advancement-title prose as accepted ids when the candidate id is the phrase itself, such as "one/small/step", "quite/the/sun/tan", or "back/in/black". Accept only canonical gate/tier/dimension/material ids backed by evidence.
- For progression_stage, a dimension word in a namespace/path is not enough. The label or semantic evidence must describe the dimension/unlock/gate itself.
- For progression_stage, material names are accepted only when they gate broad progression; reject isolated material variants or product lines.
- For organization_group, "accepted" means a human player storage section or mental bucket for items, not a workstation, recipe type, or process.
- For organization_group, good accepted examples include unprocessed ores, refined ores, cooking tools, Create items, ULV components, woodworking, decorative, animal husbandry, weaving/cloth, dirt and rocks, seeds, inedible plants, and crops. These examples are illustrative, not a closed list.
- For organization_group, accept any candidate that names a plausible player storage section with evidence, even if it is not listed in the examples.
- For organization_group, reject workstation/process labels such as anvil, quern, pot, barrel, smelting, blasting, milling, pressing, cutting, and mixing unless the candidate itself names a broader storage group like cooking_tools or woodworking.
- For organization_group, do not accept a value solely because the same id is a good workflow or used_at value. Wall-home groups must be item groupings a player would plausibly keep together.
- For mod_subsystem, "accepted" means ONLY a namespace-scoped identity system inside a mod: network, transport, automation, storage, multiblock, power, train, rocket, oxygen, or another broad functional family whose own items belong to that system.
- For mod_subsystem, reject pack-scoped ids, universal ids, process/task labels, one-off station labels, equipment/tool/armor sets, material families, decorative families, tiers, machine hulls/casings, and labels based only on recipe participation.
- For mod_subsystem, use workflow/used_at for "where/how is this processed?" and organization_group for wall-home concepts. Subsystem values should answer "what mod system is this item itself part of?"
- Prefer preserving previous accepted ids.
- For workflow_role, every id must be <workflow>#<role> and parent must equal the workflow id.
- Only output JSON.