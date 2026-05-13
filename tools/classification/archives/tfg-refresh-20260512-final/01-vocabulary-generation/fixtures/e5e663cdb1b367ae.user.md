{
  "pack_id": "tfg",
  "facet": "loadout_context",
  "policy": "Vocabulary-backed trip, kit, or task context where a player would pack this item.",
  "min_evidence": 2,
  "previous_accepted": [],
  "prompt_budget": {
    "max_chars": 3200000,
    "semantic_evidence_per_candidate": 64,
    "evidence_refs_per_candidate": 64
  },
  "candidates": [
    {
      "id": "slot:base_maintenance",
      "label": "Base Maintenance",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:building_project",
      "label": "Building Project",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:cave_run",
      "label": "Cave Run",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:combat_trip",
      "label": "Combat Trip",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:exploration_trip",
      "label": "Exploration Trip",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:farming_run",
      "label": "Farming Run",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:machine_setup",
      "label": "Machine Setup",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:mining_run",
      "label": "Mining Run",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    }
  ],
  "required_output_contract": {
    "required_values_count": 8,
    "required_candidate_ids": [
      "slot:base_maintenance",
      "slot:building_project",
      "slot:cave_run",
      "slot:combat_trip",
      "slot:exploration_trip",
      "slot:farming_run",
      "slot:machine_setup",
      "slot:mining_run"
    ],
    "final_instructions": [
      "Return strict JSON only: one object with a top-level values array.",
      "The values array must contain exactly one object for every id in required_candidate_ids.",
      "Every output id must exactly match one candidate id from required_candidate_ids.",
      "Never omit rejected, low-quality, generic, or uncertain candidates; mark them rejected or review.",
      "Do not add ids that are not in required_candidate_ids.",
      "Before responding, count values.length and verify it equals required_values_count."
    ]
  }
}