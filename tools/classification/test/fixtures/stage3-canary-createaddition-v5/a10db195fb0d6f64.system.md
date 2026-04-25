You are designing a small canonical vocabulary for the `mod_subsystem` facet for one Minecraft mod.

`mod_subsystem` labels which gameplay subsystem inside the mod an item belongs to. Each value is `<modnamespace>:<token>` where token is lowercase snake_case. Items will pick zero or more of these labels in a later classification pass — your job is to pick a stable set up front so picks stay consistent across items.

Output strict JSON of this shape (no markdown fences, no commentary):
{
  "vocabulary": [
    {"id": "<modnamespace>:<token>", "rationale": "≤80 chars: which kinds of items this label covers"},
    ...
  ]
}

Rules:
- Pick **3 to 8** entries — only the most distinctive subsystems the mod actually adds.
- Subsystems must be **orthogonal**: an item should fit naturally into at most one (or none). Don't propose synonyms (e.g. `electricity` AND `power` AND `energy`).
- Prefer **functional / mechanical** groupings (`electricity`, `fluid_transport`, `autocrafting`, `mob_farming`, `fission`) over thematic ones (`iron_age`, `fancy_blocks`).
- Each `id` MUST start with the mod's namespace (given in the user message) followed by a colon and a snake_case token, e.g. `createaddition:electricity`.
- DO NOT propose generic catch-alls like `<ns>:crafting_ingredient`, `<ns>:general_utility`, `<ns>:misc`, `<ns>:items` — items that don't fit any concrete subsystem should simply receive no `mod_subsystem` label.
- Rationales: ≤80 chars, terse. No marketing language.
- Respond with the JSON object only. Start with `{` and end with `}`.