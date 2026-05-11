package dev.imagio.slot.classification;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacetIndexTest {

    @Test
    void emptyIndexHasNoRoles() {
        FacetIndex index = FacetIndex.empty();
        assertTrue(index.isEmpty());
        assertEquals(0, index.size());
        assertEquals(Optional.empty(), index.role("minecraft:diamond_pickaxe"));
        assertEquals(Optional.empty(), index.role(null));
        assertEquals(Optional.empty(), index.role(""));
    }

    @Test
    void loadsRoleFromSingleValueEntry() {
        String json = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "minecraft:diamond_pickaxe": {
                      "facets": {
                        "role": {"value": "tool", "confidence": 0.95}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(1, index.size());
        assertEquals(Optional.of("tool"), index.role("minecraft:diamond_pickaxe"));
        assertEquals(Optional.empty(), index.role("minecraft:unknown"));
        assertEquals(Set.of("minecraft:diamond_pickaxe"), index.itemIds());
    }

    @Test
    void loadsRoleFromAmbiguousEntryAsFirstCandidate() {
        String json = """
                {
                  "schema_version": 1,
                  "layer": "per-mod",
                  "entries": {
                    "create:cogwheel": {
                      "facets": {
                        "role": {"values": ["mechanism", "redstone_component"], "ambiguous": true, "confidence": 0.6}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(Optional.of("mechanism"), index.role("create:cogwheel"));
    }

    @Test
    void loadAllWithLayersMergesDatapackLayerOnTopOfBundledData() {
        String json = """
                {
                  "schema_version": 1,
                  "layer": "modpack",
                  "source": "test-pack",
                  "entries": {
                    "testpack:custom_widget": {
                      "facets": {
                        "role": {"value": "mechanism", "confidence": 0.9}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndexBootstrap.loadAllWithLayers(List.of(
                new FacetIndexBootstrap.NamedLayerResource("test layer", () -> new StringReader(json))
        ));
        assertEquals(Optional.of("mechanism"), index.role("testpack:custom_widget"));
        assertTrue(index.size() > 1);
    }

    @Test
    void loadAllWithReportTracksDatapackLayers() {
        String json = """
                {
                  "schema_version": 1,
                  "layer": "modpack",
                  "source": "test-pack",
                  "entries": {
                    "testpack:custom_widget": {
                      "facets": {
                        "role": {"value": "mechanism", "confidence": 0.9}
                      }
                    }
                  }
                }
                """;
        FacetIndexBootstrap.LoadResult result = FacetIndexBootstrap.loadAllWithReport(List.of(
                new FacetIndexBootstrap.NamedLayerResource(
                        "datapack:slot:classification/layers/test-pack.json",
                        () -> new StringReader(json))
        ));

        assertEquals(Optional.of("mechanism"), result.index().role("testpack:custom_widget"));
        assertEquals(result.index().size(), result.report().totalEntries());
        assertEquals(1, result.report().datapackLayers().size());
        FacetIndexLoadReport.Layer layer = result.report().datapackLayers().get(0);
        assertTrue(layer.loaded());
        assertEquals("datapack:slot:classification/layers/test-pack.json", layer.description());
        assertEquals(1, layer.entries());
    }

    @Test
    void statusFormatterReportsDatapackLayerSummary() {
        FacetIndexLoadReport report = new FacetIndexLoadReport(
                0L,
                10,
                List.of(FacetIndexLoadReport.Layer.loaded("/data/slot/classification/vanilla-base.json", 9)),
                List.of(
                        FacetIndexLoadReport.Layer.loaded("datapack:slot:classification/layers/pack.json", 1),
                        FacetIndexLoadReport.Layer.failed("datapack:slot:classification/layers/bad.json", "bad json")
                )
        );

        List<String> lines = FacetIndexStatusFormatter.format(report, true);

        assertTrue(lines.get(0).contains("entries=10"));
        assertTrue(lines.get(0).contains("datapack=1/2"));
        assertTrue(lines.get(0).contains("failed=1"));
        assertTrue(lines.stream().anyMatch(line -> line.contains(
                "loaded datapack:slot:classification/layers/pack.json entries=1")));
        assertTrue(lines.stream().anyMatch(line -> line.contains(
                "failed datapack:slot:classification/layers/bad.json error=bad json")));
    }

    @Test
    void itemsWithoutAnyKnownFacetAreSkipped() {
        // Entries with only facets we don't load (e.g. tier, has_durability)
        // shouldn't pollute the index. role / material_family / form / the
        // multi-value facets all qualify an entry for inclusion.
        String json = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "minecraft:tier_only_entry": {
                      "facets": {
                        "tier": {"value": "diamond"}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(0, index.size());
        assertEquals(Optional.empty(), index.role("minecraft:tier_only_entry"));
        assertEquals(Optional.empty(), index.materialFamily("minecraft:tier_only_entry"));
    }

    @Test
    void itemWithOnlyFormFacetIsStillIndexed() {
        // Form-keyed templates (Stairs, Slabs, Doors, …) need the form
        // facet to fire even when the LLM-authored facets are missing.
        String json = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "modded:strange_stair": {
                      "facets": {
                        "form": {"value": "stairs"}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(Optional.of("stairs"), index.form("modded:strange_stair"));
        assertEquals(Optional.empty(), index.role("modded:strange_stair"));
    }

    @Test
    void itemEntriesWithMalformedIdsAreSkipped() {
        String json = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "BadId": {"facets": {"role": {"value": "tool"}}},
                    "minecraft:diamond_pickaxe": {"facets": {"role": {"value": "tool"}}}
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(1, index.size());
        assertEquals(Optional.of("tool"), index.role("minecraft:diamond_pickaxe"));
        assertEquals(Optional.empty(), index.role("BadId"));
    }

    @Test
    void rejectsUnsupportedSchemaVersion() {
        String json = """
                {"schema_version": 99, "layer": "vanilla-base", "entries": {}}
                """;
        assertThrows(IllegalArgumentException.class,
                () -> FacetIndex.load(new StringReader(json)));
    }

    @Test
    void rejectsUnknownLayerName() {
        String json = """
                {"schema_version": 1, "layer": "fictitious", "entries": {}}
                """;
        assertThrows(IllegalArgumentException.class,
                () -> FacetIndex.load(new StringReader(json)));
    }

    @Test
    void bundledVanillaBaseLoadsAndCoversKnownItems() {
        FacetIndex index = FacetIndexBootstrap.loadVanillaBase();
        assertFalse(index.isEmpty(),
                "bundled vanilla-base.json must produce a non-empty FacetIndex");
        assertTrue(index.size() >= 1000,
                "bundled vanilla-base.json should contain at least 1000 role entries; got " + index.size());

        assertEquals(Optional.of("tool"), index.role("minecraft:diamond_pickaxe"));
        assertEquals(Optional.of("weapon"), index.role("minecraft:diamond_sword"));
        assertEquals(Optional.of("armor"), index.role("minecraft:diamond_helmet"));
        assertEquals(Optional.of("consumable"), index.role("minecraft:cooked_beef"));
        assertEquals(Optional.of("material"), index.role("minecraft:iron_ingot"));
        assertEquals(Optional.of("building_block"), index.role("minecraft:oak_planks"));
        assertEquals(Optional.of("storage_block"), index.role("minecraft:chest"));

        // material_family unifies wood blocks across role inconsistencies
        // — e.g. birch_wood is currently labelled natural_resource while
        // birch_planks is building_block, but both share wood_birch and
        // that shared family is what learned-rule adjacency keys on.
        assertEquals(Optional.of("wood_birch"), index.materialFamily("minecraft:birch_wood"));
        assertEquals(Optional.of("wood_birch"), index.materialFamily("minecraft:birch_planks"));
        assertEquals(Optional.of("wood_oak"), index.materialFamily("minecraft:oak_planks"));
    }

    @Test
    void parsesMaterialFamilyFromSingleValueEntry() {
        String json = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "minecraft:iron_ingot": {
                      "facets": {
                        "role": {"value": "material"},
                        "material_family": {"value": "iron"}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(Optional.of("material"), index.role("minecraft:iron_ingot"));
        assertEquals(Optional.of("iron"), index.materialFamily("minecraft:iron_ingot"));
        assertEquals(Optional.empty(), index.materialFamily("minecraft:unknown"));
    }

    @Test
    void mergedWithLayersOtherOnTopOfThis() {
        String baseJson = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "minecraft:diamond_pickaxe": {"facets": {"role": {"value": "tool"}}},
                    "minecraft:iron_ingot":      {"facets": {"role": {"value": "material"}}}
                  }
                }
                """;
        String overlayJson = """
                {
                  "schema_version": 1,
                  "layer": "per-mod",
                  "entries": {
                    "create:cogwheel":           {"facets": {"role": {"value": "mechanism"}}},
                    "minecraft:iron_ingot":      {"facets": {"role": {"value": "material_overridden"}}}
                  }
                }
                """;
        FacetIndex base = FacetIndex.load(new StringReader(baseJson));
        FacetIndex overlay = FacetIndex.load(new StringReader(overlayJson));
        FacetIndex merged = base.mergedWith(overlay);

        // base-only entries pass through
        assertEquals(Optional.of("tool"), merged.role("minecraft:diamond_pickaxe"));
        // overlay-only entries land
        assertEquals(Optional.of("mechanism"), merged.role("create:cogwheel"));
        // conflict: overlay wins
        assertEquals(Optional.of("material_overridden"), merged.role("minecraft:iron_ingot"));
        assertEquals(3, merged.size());
    }

    @Test
    void mergedWithEmptyIsIdentity() {
        FacetIndex base = FacetIndex.load(new StringReader("""
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "minecraft:diamond_pickaxe": {"facets": {"role": {"value": "tool"}}}
                  }
                }
                """));
        assertEquals(1, base.mergedWith(FacetIndex.empty()).size());
        assertEquals(1, FacetIndex.empty().mergedWith(base).size());
    }

    @Test
    void bundledLoadAllIncludesPerModEntries() {
        FacetIndex index = FacetIndexBootstrap.loadAll();
        // Vanilla items remain accessible.
        assertEquals(Optional.of("tool"), index.role("minecraft:diamond_pickaxe"));
        // A handful of mod items from each per-mod layer should resolve.
        // These IDs come from the bundled per-mod files; if any are
        // renamed upstream, update the assertion alongside the bump.
        assertEquals(Optional.of("mechanism"), index.role("create:cogwheel"));
        assertEquals(Optional.of("upgrade"),
                index.role("sophisticatedstorage:advanced_alchemy_upgrade"));
        assertTrue(index.size() > 1500,
                "bundled per-mod entries should push total size above vanilla-base alone");
    }

    @Test
    void parsesSubsystemsAsMultiValueList() {
        String json = """
                {
                  "schema_version": 1,
                  "layer": "per-mod",
                  "entries": {
                    "create:cogwheel": {
                      "facets": {
                        "role": {"value": "mechanism"},
                        "mod_subsystem": {
                          "values": ["create:mechanical_power"],
                          "mode": "add"
                        },
                        "organization_group": {
                          "values": ["create:mechanical_power"],
                          "mode": "add"
                        },
                        "activity": {
                          "values": ["slot:redstone", "slot:transportation"],
                          "mode": "add"
                        },
                        "flavor": {"values": ["mechanical"], "mode": "add"},
                        "carry_frequency": {"value": "frequent"},
                        "rarity": {"value": "common"},
                        "origin": {"values": ["crafted_only"]},
                        "dye_color": {"value": "black"}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(List.of("create:mechanical_power"), index.subsystems("create:cogwheel"));
        assertEquals(List.of("create:mechanical_power"), index.organizationGroups("create:cogwheel"));
        assertEquals(List.of("slot:redstone", "slot:transportation"), index.activities("create:cogwheel"));
        assertEquals(Optional.of("mechanical"), index.flavor("create:cogwheel"));
        assertEquals(Optional.of("frequent"), index.carryFrequency("create:cogwheel"));
        assertEquals(Optional.of("common"), index.rarity("create:cogwheel"));
        assertEquals(Optional.of("crafted_only"), index.origin("create:cogwheel"));
        assertEquals(Optional.of("black"), index.dyeColor("create:cogwheel"));
    }

    @Test
    void parsesPaletteAsMultiValueListWithFirstAsPrimary() {
        // palette is multi-valued in the canonical schema (an item can
        // share more than one tone bucket — e.g. acacia is both
        // wood_red and warm). Loader must preserve order so callers
        // that key on the primary value (within-island palette cluster)
        // get a stable choice.
        String json = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "minecraft:acacia_planks": {
                      "facets": {
                        "role": {"value": "building_block"},
                        "palette": {
                          "values": ["wood_red", "warm"],
                          "mode": "add"
                        }
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(List.of("wood_red", "warm"), index.palette("minecraft:acacia_planks"));
    }

    @Test
    void itemWithOnlyPaletteFacetIsStillIndexed() {
        // Palette-only items still appear in the index so the
        // within-island palette cluster fires on them.
        String json = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "modded:exotic_block": {
                      "facets": {
                        "palette": {"values": ["copper_oxidized"]}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(List.of("copper_oxidized"), index.palette("modded:exotic_block"));
        assertEquals(Optional.empty(), index.role("modded:exotic_block"));
    }

    @Test
    void missingFacetAccessorsReturnEmpty() {
        String json = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "minecraft:apple": {
                      "facets": {
                        "role": {"value": "consumable"}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertTrue(index.subsystems("minecraft:apple").isEmpty());
        assertTrue(index.organizationGroups("minecraft:apple").isEmpty());
        assertTrue(index.activities("minecraft:apple").isEmpty());
        assertEquals(Optional.empty(), index.flavor("minecraft:apple"));
        assertEquals(Optional.empty(), index.carryFrequency("minecraft:apple"));
        assertEquals(Optional.empty(), index.rarity("minecraft:apple"));
        assertEquals(Optional.empty(), index.origin("minecraft:apple"));
        assertEquals(Optional.empty(), index.dyeColor("minecraft:apple"));
        assertTrue(index.palette("minecraft:apple").isEmpty());
        // Unknown items return empty as well.
        assertTrue(index.subsystems("minecraft:unknown").isEmpty());
        assertTrue(index.organizationGroups("minecraft:unknown").isEmpty());
        assertTrue(index.activities("minecraft:unknown").isEmpty());
        assertTrue(index.palette("minecraft:unknown").isEmpty());
    }

    @Test
    void itemWithOnlyOrganizationGroupFacetIsStillIndexed() {
        String json = """
                {
                  "schema_version": 1,
                  "layer": "modpack",
                  "entries": {
                    "tfc:ceramic/ingot_mold": {
                      "facets": {
                        "organization_group": {"values": ["tfc:casting"]}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(List.of("tfc:casting"), index.organizationGroups("tfc:ceramic/ingot_mold"));
        assertEquals(Optional.empty(), index.role("tfc:ceramic/ingot_mold"));
    }

    @Test
    void singleFacetReaderPrefersValueWhenBothShapesPresent() {
        // Defensive: if a facet is emitted with both `value` and `values`,
        // prefer the canonical single value over the list.
        String json = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "modded:weird": {
                      "facets": {
                        "carry_frequency": {"value": "frequent", "values": ["rare"]}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(Optional.of("frequent"), index.carryFrequency("modded:weird"));
    }

    @Test
    void itemWithOnlySubsystemFacetIsStillIndexed() {
        // After phase 1, items whose only useful facets are the new ones
        // (no role, no material_family) must still land in the index so
        // subsystem-primary matching can fire on them.
        String json = """
                {
                  "schema_version": 1,
                  "layer": "per-mod",
                  "entries": {
                    "modded:gizmo": {
                      "facets": {
                        "mod_subsystem": {"values": ["modded:logistics"]}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(List.of("modded:logistics"), index.subsystems("modded:gizmo"));
        assertEquals(Optional.empty(), index.role("modded:gizmo"));
    }

    @Test
    void itemWithOnlyMaterialFamilyStillIndexed() {
        String json = """
                {
                  "schema_version": 1,
                  "layer": "vanilla-base",
                  "entries": {
                    "modded:mystery_alloy": {
                      "facets": {
                        "material_family": {"value": "iron"}
                      }
                    }
                  }
                }
                """;
        FacetIndex index = FacetIndex.load(new StringReader(json));
        assertEquals(Optional.empty(), index.role("modded:mystery_alloy"));
        assertEquals(Optional.of("iron"), index.materialFamily("modded:mystery_alloy"));
    }
}
