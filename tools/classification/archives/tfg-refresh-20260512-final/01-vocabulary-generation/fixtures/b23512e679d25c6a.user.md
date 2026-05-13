{
  "pack_id": "tfg",
  "facet": "equipment_effect",
  "policy": "Vocabulary-backed player-visible effect granted by carrying, wearing, or using the item.",
  "min_evidence": 2,
  "previous_accepted": [],
  "prompt_budget": {
    "max_chars": 3200000,
    "semantic_evidence_per_candidate": 64,
    "evidence_refs_per_candidate": 64
  },
  "candidates": [
    {
      "id": "slot:flight",
      "label": "Flight",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:night_vision",
      "label": "Night Vision",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:oxygen_supply",
      "label": "Oxygen Supply",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:reach_boost",
      "label": "Reach Boost",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:speed_boost",
      "label": "Speed Boost",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:step_assist",
      "label": "Step Assist",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:tool_mode",
      "label": "Tool Mode",
      "origin": "universal_default",
      "confidence": 0.95,
      "support": 999,
      "evidence": [],
      "semantic_evidence": [],
      "aliases": []
    },
    {
      "id": "slot:water_breathing",
      "label": "Water Breathing",
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
      "slot:flight",
      "slot:night_vision",
      "slot:oxygen_supply",
      "slot:reach_boost",
      "slot:speed_boost",
      "slot:step_assist",
      "slot:tool_mode",
      "slot:water_breathing"
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