package dev.imagio.slot.inventory.triage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public enum IslandSuggestionTemplate {
    // Cluster row/column drives populate-time atlas placement (see
    // RealisticAtlasGenerator.layoutIslands). Same column = packed tight
    // with ISLAND_GAP, different column = ISLAND_PARENT_GROUP_GAP visual
    // break, different row = ISLAND_CLUSTER_ROW_GAP. The five logical
    // groups: row 0 player gear, row 1 building, row 2 materials,
    // row 3 machinery, row 4 decoration/curio/misc.
    SEEDS(
            "template.seeds",
            "Seeds",
            0xCC7A6A2E,
            2, 1,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:farming", "slot:gathering"),
            Set.of()
    ),
    CROPS(
            "template.crops",
            "Crops",
            0xCC5F7A35,
            2, 1,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:farming", "slot:gathering"),
            Set.of()
    ),
    ORGANIC_MATERIALS(
            "template.organic_materials",
            "Organic Materials",
            0xCC6A4E35,
            2, 1,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:combat", "slot:gathering"),
            Set.of()
    ),
    FOOD(
            "template.food",
            "Food",
            0xCC7D5A3A,
            0, 2,
            Set.of(IslandSignal.FOOD),
            Set.of(),
            Set.of("consumable", "food", "drink"),
            Set.of("slot:eating"),
            Set.of()
    ),
    TOOLS(
            "template.tools",
            "Tools",
            0xCC5A6E3D,
            0, 0,
            Set.of(IslandSignal.DIGGER_TOOL),
            Set.of(),
            Set.of("tool"),
            Set.of("slot:mining", "slot:harvesting", "slot:tool_use"),
            Set.of()
    ),
    WEAPONS(
            "template.weapons",
            "Weapons",
            0xCC6E3D3D,
            0, 0,
            Set.of(
                    IslandSignal.SWORD,
                    IslandSignal.BOW,
                    IslandSignal.CROSSBOW,
                    IslandSignal.TRIDENT,
                    IslandSignal.MACE
            ),
            Set.of(),
            Set.of("weapon", "ammunition"),
            Set.of("slot:combat"),
            Set.of()
    ),
    ARMOR(
            "template.armor",
            "Armor",
            0xCC3D5A6E,
            0, 0,
            Set.of(
                    IslandSignal.ARMOR_HEAD,
                    IslandSignal.ARMOR_CHEST,
                    IslandSignal.ARMOR_LEGS,
                    IslandSignal.ARMOR_FEET
            ),
            Set.of(),
            Set.of("armor"),
            Set.of("slot:combat", "slot:defense"),
            Set.of()
    ),
    // Lighting — torches, lanterns, glowstone, shroomlight, etc.
    // Cluster cave/base lighting in its own island so the player can
    // grab "a torch" without scanning DECORATION or UTILITY. Fires on
    // the emits_light facet (rule-derived from a vanilla id list +
    // suffix patterns). Declared early so it wins over UTILITY (which
    // would catch torch via role) and DECORATION (which would catch
    // glowstone via role).
    LIGHTING(
            "template.lighting",
            "Lighting",
            0xCC8A7A28,
            0, 2,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
    ),
    // Materials family — split by tag/form/id so common stock (wood,
    // seeds, crops, plant stock, ceramics/molds, organic materials,
    // metal stock, gems/crystals, ore stock, and dusts/powders) does not
    // collapse into one giant Materials pile. Players grab "a stick",
    // "wheat seeds", or "an iron plate" distinctly; a single Materials
    // pile lumps the lot and pushes ingredients halfway down.
    METAL_STOCK(
            "template.metal_stock",
            "Metal Stock",
            0xCC3D6E5A,
            2, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
    ),
    GEMS_CRYSTALS(
            "template.gems_crystals",
            "Gems & Crystals",
            0xCC3D6E70,
            2, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
    ),
    ORES_RAW_STOCK(
            "template.ores_raw_stock",
            "Ores & Raw Stock",
            0xCC3D5C4A,
            2, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
    ),
    DUSTS_POWDERS(
            "template.dusts_powders",
            "Dusts & Powders",
            0xCC6E6A5A,
            2, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
    ),
    WOOD(
            "template.wood",
            "Wood",
            0xCC6B4A2E,
            2, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
    ),
    PLANTS(
            "template.plants",
            "Plants",
            0xCC3F7A45,
            2, 1,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:farming", "slot:gathering", "slot:decorating"),
            Set.of()
    ),
    CERAMICS_MOLDS(
            "template.ceramics_molds",
            "Ceramics & Molds",
            0xCC8A6042,
            2, 1,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:crafting", "slot:building", "slot:decorating"),
            Set.of()
    ),
    MATERIALS(
            "template.materials",
            "Materials",
            0xCC3D6E5A,
            2, 0,
            Set.of(),
            Set.of(),
            Set.of("material", "ingredient"),
            Set.of(),
            Set.of()
    ),
    STORAGE(
            "template.storage",
            "Storage",
            0xCC5A3D6E,
            3, 1,
            Set.of(),
            Set.of("c:chests", "c:shulker_boxes", "c:barrels"),
            Set.of("storage_block", "container_portable", "storage"),
            Set.of("slot:storage_management"),
            Set.of()
    ),
    // Roles below are role-only (FacetIndex-driven). The legacy
    // class/tag fallback can't reliably distinguish them — a stone
    // brick block has no equivalent of `DiggerItem` — so they fire
    // exclusively when the precomputed dataset has classified the
    // item. That's the whole point of the FacetIndex track: lift
    // chip coverage past what subclass + tag heuristics can do.
    //
    // Building family — declared BEFORE the catch-all BUILDING so
    // form-keyed templates fire first (an _stairs item lands on
    // STAIRS, not on BUILDING). Form lookup comes from the classifier
    // dataset's `form` facet, which is rule-derived from id suffix +
    // tag, so coverage is high even without LLM input.
    STAIRS(
            "template.stairs",
            "Stairs",
            0xCC55534A,
            1, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:building", "slot:construction"),
            Set.of("stairs")
    ),
    SLABS(
            "template.slabs",
            "Slabs",
            0xCC55504A,
            1, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:building", "slot:construction"),
            Set.of("slab")
    ),
    WALLS(
            "template.walls",
            "Walls",
            0xCC4A4A4A,
            1, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:building", "slot:construction"),
            Set.of("wall")
    ),
    DOORS(
            "template.doors",
            "Doors",
            0xCC553A2A,
            1, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:building", "slot:construction"),
            Set.of("door", "trapdoor", "fence_gate")
    ),
    FENCES(
            "template.fences",
            "Fences",
            0xCC504A38,
            1, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:building", "slot:construction"),
            Set.of("fence")
    ),
    WINDOWS(
            "template.windows",
            "Windows",
            0xCC4A6E70,
            1, 0,
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("slot:building", "slot:construction"),
            Set.of("pane", "bars")
    ),
    BUILDING(
            "template.building",
            "Building Blocks",
            0xCC5A5A5A,
            1, 0,
            Set.of(),
            Set.of(),
            Set.of("building_block", "block"),
            Set.of("slot:building", "slot:construction"),
            Set.of()
    ),
    DECORATION(
            "template.decoration",
            "Decoration",
            0xCC5A3C6E,
            4, 0,
            Set.of(),
            Set.of(),
            Set.of("decorative_block"),
            Set.of("slot:decorating"),
            Set.of()
    ),
    NATURAL(
            "template.natural",
            "Natural",
            0xCC3C6E3C,
            2, 1,
            Set.of(),
            Set.of(),
            Set.of("natural_resource"),
            Set.of("slot:farming", "slot:gathering"),
            Set.of()
    ),
    WORKBENCHES(
            "template.workbenches",
            "Workbenches",
            0xCC6E3C24,
            3, 0,
            Set.of(),
            Set.of(),
            Set.of("functional_block"),
            Set.of("slot:crafting", "slot:automation", "slot:smelting"),
            Set.of()
    ),
    MECHANISMS(
            "template.mechanisms",
            "Mechanisms",
            0xCC8A5E24,
            3, 0,
            Set.of(),
            Set.of(),
            Set.of("mechanism"),
            Set.of("slot:automation"),
            Set.of()
    ),
    REDSTONE(
            "template.redstone",
            "Redstone",
            0xCC6E2E2E,
            3, 0,
            Set.of(),
            Set.of(),
            Set.of("redstone_component"),
            Set.of("slot:redstone"),
            Set.of()
    ),
    UPGRADES(
            "template.upgrades",
            "Upgrades",
            0xCC4A5E8A,
            3, 1,
            Set.of(),
            Set.of(),
            Set.of("upgrade"),
            Set.of("slot:enchanting"),
            Set.of()
    ),
    TRANSPORT(
            "template.transport",
            "Transport",
            0xCC3D6E6E,
            0, 1,
            Set.of(),
            Set.of(),
            Set.of("transport"),
            Set.of("slot:transportation", "slot:logistics"),
            Set.of()
    ),
    UTILITY(
            "template.utility",
            "Utility",
            0xCC555575,
            0, 1,
            Set.of(),
            Set.of(),
            Set.of("utility"),
            Set.of(),
            Set.of()
    ),
    CURIOSITY(
            "template.curiosity",
            "Curiosities",
            0xCC4F3D6E,
            4, 0,
            Set.of(),
            Set.of(),
            Set.of("curiosity", "trophy", "spawn_egg", "music_disc"),
            Set.of("slot:display"),
            Set.of()
    ),
    MISC(
            "template.misc",
            "Miscellaneous",
            0xCC4A4A4A,
            4, 0,
            Set.of(),
            Set.of(),
            Set.of("admin"),
            Set.of(),
            Set.of()
    );

    private final String defaultIslandId;
    private final String defaultLabel;
    private final int defaultColor;
    private final int clusterRow;
    private final int clusterColumn;
    private final Set<IslandSignal> classSignals;
    private final Set<String> itemTagTriggers;
    private final Set<String> roleTriggers;
    private final Set<String> activityTriggers;
    private final Set<String> formTriggers;

    IslandSuggestionTemplate(
            String defaultIslandId,
            String defaultLabel,
            int defaultColor,
            int clusterRow,
            int clusterColumn,
            Set<IslandSignal> classSignals,
            Set<String> itemTagTriggers,
            Set<String> roleTriggers,
            Set<String> activityTriggers,
            Set<String> formTriggers
    ) {
        this.defaultIslandId = defaultIslandId;
        this.defaultLabel = defaultLabel;
        this.defaultColor = defaultColor;
        this.clusterRow = clusterRow;
        this.clusterColumn = clusterColumn;
        this.classSignals = Set.copyOf(classSignals);
        this.itemTagTriggers = Set.copyOf(itemTagTriggers);
        this.roleTriggers = Set.copyOf(roleTriggers);
        this.activityTriggers = Set.copyOf(activityTriggers);
        this.formTriggers = Set.copyOf(formTriggers);
    }

    public String defaultIslandId() {
        return defaultIslandId;
    }

    public String defaultLabel() {
        return defaultLabel;
    }

    public int defaultColor() {
        return defaultColor;
    }

    public int clusterRow() {
        return clusterRow;
    }

    public int clusterColumn() {
        return clusterColumn;
    }

    /**
     * Whether this template can split into a count-qualified subsystem island.
     * Disabled by default: {@code mod_subsystem} is useful semantic/query
     * evidence, but mod-internal subsystem names are not player main-wall
     * sections. If a subsystem-like concept deserves a wall section, model it
     * as a broad {@code organization_group} instead.
     */
    public boolean allowsSubsystemGrouping() {
        return false;
    }

    /**
     * Whether this template can split into player-facing organization
     * groups. These are not mod-internal subsystems; they represent where a
     * player would manually put the item in a large modpack ("Casting Molds",
     * "Masonry Supplies", "Leatherworking"). Because this facet creates main
     * wall homes, only broad parents allow it to split; high-specificity
     * sections like Metal Stock, Food, Tools, and Storage keep ownership even
     * when the layer carries a tempting query-style group.
     */
    public boolean allowsOrganizationGrouping() {
        return switch (this) {
            case MATERIALS, BUILDING, DECORATION, NATURAL,
                    MECHANISMS, WORKBENCHES, REDSTONE, TRANSPORT, UTILITY -> true;
            default -> false;
        };
    }

    /**
     * Whether a match for this template is a strong, narrow signal —
     * strong enough that it should outrank broad learned chips
     * (especially NAMESPACE / CREATIVE_TAB) when surfacing
     * suggestions. The form-keyed templates (STAIRS / SLABS / WALLS /
     * DOORS / FENCES / WINDOWS) are the obvious case: a "_wall" form
     * facet is a much more confident signal than "the player just
     * homed two other Create blocks somewhere". LIGHTING fires on the
     * narrow {@code emits_light} facet, also strong. Specific
     * commodity templates (METAL_STOCK / GEMS_CRYSTALS /
     * ORES_RAW_STOCK / DUSTS_POWDERS / WOOD / SEEDS / CROPS /
     * PLANTS / CERAMICS_MOLDS / ORGANIC_MATERIALS / STORAGE)
     * key on stable forms, tags, or ids that map cleanly to a player's mental
     * model. Class-signal templates (TOOLS / WEAPONS / ARMOR / FOOD)
     * key on Minecraft subclasses, which the player thinks of as
     * "this IS a sword / armor piece / food", not "this is a thing in
     * the same mod as".
     *
     * <p>Generic role templates (BUILDING / DECORATION / NATURAL /
     * MECHANISMS / REDSTONE / UPGRADES / TRANSPORT / UTILITY /
     * CURIOSITY / WORKBENCHES / MISC) are deliberately excluded —
     * their roleTriggers are wide enough that a learned-rule chip
     * with player-confirmed adjacency is usually the better signal.
     */
    public boolean isHighSpecificity() {
        return switch (this) {
            case STAIRS, SLABS, WALLS, DOORS, FENCES, WINDOWS,
                    LIGHTING,
                    METAL_STOCK, GEMS_CRYSTALS, ORES_RAW_STOCK, DUSTS_POWDERS, WOOD,
                    SEEDS, CROPS, PLANTS, CERAMICS_MOLDS, ORGANIC_MATERIALS,
                    STORAGE,
                    TOOLS, WEAPONS, ARMOR, FOOD -> true;
            default -> false;
        };
    }

    public boolean matches(IslandSignalDescriptor descriptor) {
        if (descriptor == null) {
            return false;
        }
        // LIGHTING fires when the descriptor's emits_light facet is set
        // — declared first so torches and glowstone don't fall through
        // to UTILITY / DECORATION first. Light emission is a stronger
        // signal for "where would the player look for this" than role.
        if (this == LIGHTING) {
            return descriptor.emitsLight();
        }
        if ((this == METAL_STOCK || this == GEMS_CRYSTALS) && isOreStockBlock(descriptor)) {
            return false;
        }
        // Form-keyed templates (STAIRS, SLABS, DOORS, …) are
        // form-sufficient: a stair is a stair regardless of role. The
        // form facet is rule-derived from id suffix + tag, but coverage
        // misses some modded items (Create's `dark_oak_window` is
        // role=building_block with form=null because there's no
        // window-form derivation rule). For those, fall back to an
        // id-suffix check so the WINDOWS / DOORS / etc. templates still
        // capture them.
        if (!formTriggers.isEmpty()) {
            String form = descriptor.form();
            if (form != null && formTriggers.contains(form)) {
                return true;
            }
            String itemId = descriptor.identity() == null ? "" : descriptor.identity().itemId();
            if (itemId != null && !itemId.isBlank()) {
                int colon = itemId.indexOf(':');
                String path = colon >= 0 ? itemId.substring(colon + 1) : itemId;
                for (String suffix : idSuffixFallbacksFor(this)) {
                    if (path.endsWith(suffix)) {
                        return true;
                    }
                }
            }
            return false;
        }
        // Classification role wins when present — that's the precomputed
        // signal we trust most. Fall through to the legacy class/tag
        // checks when no role is available (datapack items, KubeJS
        // additions, unknown mods, or FacetIndex.ENABLED == false).
        for (String candidate : descriptor.roleAlternatives()) {
            if (this == BUILDING
                    && "block".equals(candidate)
                    && blockRoleShouldYieldToLaterOrganizationGroup(descriptor.organizationGroups())) {
                continue;
            }
            if (candidate != null && roleTriggers.contains(candidate)) {
                return true;
            }
        }
        if (hasMatchingOrganizationGroup(descriptor.organizationGroups(), organizationGroupTriggersFor(this))) {
            return true;
        }
        for (IslandSignal signal : classSignals) {
            if (descriptor.classSignals().contains(signal)) {
                return true;
            }
        }
        if (hasMatchingTag(descriptor.itemTags(), itemTagTriggers)) {
            return true;
        }
        if (this == SEEDS && matchesSeedStock(descriptor)) {
            return true;
        }
        if (this == CROPS && matchesCropStock(descriptor)) {
            return true;
        }
        if (this == ORGANIC_MATERIALS && matchesOrganicMaterials(descriptor)) {
            return true;
        }
        if (this == WOOD && matchesWoodStock(descriptor)) {
            return true;
        }
        if (this == PLANTS && matchesPlantStock(descriptor)) {
            return true;
        }
        if (this == CERAMICS_MOLDS && matchesCeramicsMolds(descriptor)) {
            return true;
        }
        if (matchesCommodityFormOrPath(descriptor)) {
            return true;
        }
        // ORES_RAW_STOCK id-suffix fallback: "Block of X" items
        // (raw_iron_block, iron_block, gold_block, diamond_block,
        // copper_block, netherite_block, etc.) cluster with raw chunks
        // in the player's "ore stockpile" mental model. The
        // c:raw_materials / c:ores tags don't catch compressed blocks
        // (they sit in c:storage_blocks alongside chests, which we
        // don't want to share an island with), so route by id pattern.
        if (this == ORES_RAW_STOCK) {
            String itemId = descriptor.identity() == null ? "" : descriptor.identity().itemId();
            if (itemId != null) {
                int colon = itemId.indexOf(':');
                String path = colon >= 0 ? itemId.substring(colon + 1) : itemId;
                if (ORE_STOCK_BLOCK_IDS.contains(path)
                        || path.startsWith("raw_") && path.endsWith("_block")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isOreStockBlock(IslandSignalDescriptor descriptor) {
        String path = identityPath(descriptor);
        return ORE_STOCK_BLOCK_IDS.contains(path)
                || path.startsWith("raw_") && path.endsWith("_block");
    }

    private boolean matchesCommodityFormOrPath(IslandSignalDescriptor descriptor) {
        return switch (this) {
            case METAL_STOCK -> matchesMetalStock(descriptor);
            case GEMS_CRYSTALS -> matchesGemsCrystals(descriptor);
            case ORES_RAW_STOCK -> matchesOresRawStock(descriptor);
            case DUSTS_POWDERS -> matchesDustsPowders(descriptor);
            default -> false;
        };
    }

    private static boolean matchesMetalStock(IslandSignalDescriptor descriptor) {
        if (!allowsCommodityPathMatch(descriptor)) {
            return false;
        }
        if (CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.INGOTS)
                || CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.NUGGETS)
                || CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.PLATES)
                || CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.RODS)
                || CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.WIRES)
                || CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.GEARS)) {
            return true;
        }
        String form = descriptor.form();
        if (form != null && METAL_STOCK_FORMS.contains(form)) {
            return true;
        }
        String path = identityPath(descriptor);
        if (path.isBlank() || hasAnyPathToken(path, METAL_STOCK_BLOCKED_PATH_TOKENS)) {
            return false;
        }
        return hasAnyPathToken(path, METAL_STOCK_PATH_TOKENS)
                || (hasPathToken(path, "metal") && hasAnyPathToken(path, METAL_PART_PATH_TOKENS));
    }

    private static boolean matchesGemsCrystals(IslandSignalDescriptor descriptor) {
        if (!allowsCommodityPathMatch(descriptor)) {
            return false;
        }
        if (CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.GEMS)) {
            return true;
        }
        String form = descriptor.form();
        if (form != null && ("gem".equals(form) || "crystal".equals(form))) {
            return true;
        }
        String path = identityPath(descriptor);
        return !path.isBlank()
                && (hasPathToken(path, "gem")
                || hasPathToken(path, "gems")
                || hasPathToken(path, "crystal")
                || hasPathToken(path, "crystals"));
    }

    private static boolean matchesOresRawStock(IslandSignalDescriptor descriptor) {
        if (!allowsCommodityPathMatch(descriptor)) {
            return false;
        }
        if (CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.RAW_MATERIALS)
                || CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.ORES)) {
            return true;
        }
        String form = descriptor.form();
        if (form != null && ORE_STOCK_FORMS.contains(form)) {
            return true;
        }
        String path = identityPath(descriptor);
        if (path.isBlank() || matchesDustPowderPath(path)) {
            return false;
        }
        return hasAnyPathToken(path, ORE_STOCK_PATH_TOKENS);
    }

    private static boolean matchesDustsPowders(IslandSignalDescriptor descriptor) {
        if (!allowsCommodityPathMatch(descriptor)) {
            return false;
        }
        if (CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.DUSTS)
                || CommonItemTagFamilies.hasFamily(descriptor.itemTags(), CommonItemTagFamilies.Family.POWDERS)) {
            return true;
        }
        String form = descriptor.form();
        if (form != null && DUST_POWDER_FORMS.contains(form)) {
            return true;
        }
        String path = identityPath(descriptor);
        return !path.isBlank()
                && (DUST_POWDER_EXACT_PATHS.contains(path) || matchesDustPowderPath(path));
    }

    private static boolean allowsCommodityPathMatch(IslandSignalDescriptor descriptor) {
        List<String> roles = descriptor.roleAlternatives();
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        for (String role : roles) {
            if ("material".equals(role) || "natural_resource".equals(role) || "ingredient".equals(role)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesSeedStock(IslandSignalDescriptor descriptor) {
        if (!rolesWithin(descriptor, SEED_ALLOWED_ROLES)) {
            return false;
        }
        String form = descriptor.form();
        if ("seed".equals(form)) {
            return true;
        }
        String path = identityPath(descriptor);
        if (path.isBlank() || hasAnyPathToken(path, SEED_BLOCKED_PATH_TOKENS)) {
            return false;
        }
        return path.endsWith("_seed")
                || path.endsWith("_seeds")
                || path.endsWith("/seed")
                || path.endsWith("/seeds")
                || SEED_EXACT_PATHS.contains(path);
    }

    private static boolean matchesCropStock(IslandSignalDescriptor descriptor) {
        if (!rolesWithin(descriptor, CROP_ALLOWED_ROLES)) {
            return false;
        }
        String form = descriptor.form();
        if (form != null && CROP_BLOCKED_FORMS.contains(form)) {
            return false;
        }
        String path = identityPath(descriptor);
        if (path.isBlank() || hasAnyPathToken(path, CROP_BLOCKED_PATH_TOKENS)) {
            return false;
        }
        return CROP_EXACT_PATHS.contains(path)
                || hasPathToken(path, "crop")
                || hasPathToken(path, "crops");
    }

    private static boolean matchesOrganicMaterials(IslandSignalDescriptor descriptor) {
        String path = identityPath(descriptor);
        if (!path.isBlank() && ORGANIC_EXACT_PATHS.contains(path)) {
            return true;
        }
        if (!rolesWithin(descriptor, ORGANIC_ALLOWED_ROLES)) {
            return false;
        }
        if (path.isBlank() || hasAnyPathToken(path, ORGANIC_BLOCKED_PATH_TOKENS)) {
            return false;
        }
        String materialFamily = descriptor.materialFamily();
        if (materialFamily != null && ORGANIC_MATERIAL_FAMILIES.contains(materialFamily)) {
            return true;
        }
        return hasAnyPathToken(path, ORGANIC_PATH_TOKENS);
    }

    private static boolean matchesPlantStock(IslandSignalDescriptor descriptor) {
        if (!rolesWithin(descriptor, PLANT_ALLOWED_ROLES)) {
            return false;
        }
        String form = descriptor.form();
        if (form != null) {
            if (PLANT_STOCK_FORMS.contains(form)) {
                return true;
            }
            if (PLANT_BLOCKED_FORMS.contains(form)) {
                return false;
            }
        }
        if (hasPlantStockTag(descriptor.itemTags())) {
            return true;
        }
        String path = identityPath(descriptor);
        if (path.isBlank() || hasAnyPathToken(path, PLANT_BLOCKED_PATH_TOKENS)) {
            return false;
        }
        if (PLANT_EXACT_PATHS.contains(path)) {
            return true;
        }
        return path.endsWith("_sapling")
                || path.endsWith("_propagule")
                || path.endsWith("_leaves")
                || path.endsWith("_flower")
                || path.endsWith("_flowers")
                || path.endsWith("_vines")
                || path.endsWith("_vine")
                || path.endsWith("_fungus")
                || path.endsWith("_roots");
    }

    private static boolean matchesCeramicsMolds(IslandSignalDescriptor descriptor) {
        if (!rolesWithin(descriptor, CERAMICS_MOLDS_ALLOWED_ROLES)) {
            return false;
        }
        String form = descriptor.form();
        if (form != null && CERAMICS_MOLDS_BLOCKED_FORMS.contains(form)) {
            return false;
        }
        String path = identityPath(descriptor);
        if (path.isBlank() || hasAnyPathToken(path, CERAMICS_MOLDS_BLOCKED_PATH_TOKENS)) {
            return false;
        }
        if (CERAMICS_MOLDS_EXACT_PATHS.contains(path)
                || hasAnyPathToken(path, CERAMICS_MOLDS_STRONG_PATH_TOKENS)) {
            return true;
        }
        return hasAnyPathToken(path, CERAMICS_MOLDS_CONTEXTUAL_PATH_TOKENS)
                && hasCeramicsMoldsContext(path, descriptor);
    }

    private static boolean matchesWoodStock(IslandSignalDescriptor descriptor) {
        if (!allowsWoodStockMatch(descriptor)) {
            return false;
        }
        String form = descriptor.form();
        if (form != null) {
            if (WOOD_STOCK_FORMS.contains(form)) {
                return true;
            }
            if (WOOD_BLOCKED_FORMS.contains(form)) {
                return false;
            }
        }
        if (hasWoodStockTag(descriptor.itemTags())) {
            return true;
        }
        String path = identityPath(descriptor);
        if (path.isBlank() || hasAnyPathToken(path, WOOD_BLOCKED_PATH_TOKENS)) {
            return false;
        }
        if (path.endsWith("_wood") || path.endsWith("/wood")
                || path.endsWith("_hyphae") || path.endsWith("/hyphae")) {
            return true;
        }
        if (hasAnyPathToken(path, WOOD_STOCK_PATH_TOKENS)) {
            return true;
        }
        return hasAnyPathToken(path, WOOD_CONTEXTUAL_STOCK_PATH_TOKENS)
                && hasWoodContext(path, descriptor);
    }

    private static boolean allowsWoodStockMatch(IslandSignalDescriptor descriptor) {
        List<String> roles = descriptor.roleAlternatives();
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        for (String role : roles) {
            if (role == null || role.isBlank()) {
                continue;
            }
            if (!WOOD_ALLOWED_ROLES.contains(role)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasWoodStockTag(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            if (WOOD_STOCK_TAGS.contains(tag)
                    || tag.startsWith("minecraft:") && (tag.endsWith("_logs") || tag.endsWith("_stems"))
                    || tag.equals("c:rods/wooden")
                    || tag.equals("forge:rods/wooden")
                    || tag.equals("c:sticks/wooden")
                    || tag.equals("forge:sticks/wooden")
                    || tag.equals("c:lumber")
                    || tag.equals("forge:lumber")
                    || tag.equals("c:boards")
                    || tag.equals("forge:boards")) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasWoodContext(String path, IslandSignalDescriptor descriptor) {
        if (hasPathToken(path, "wood") || hasPathToken(path, "wooden")) {
            return true;
        }
        String materialFamily = descriptor.materialFamily();
        return materialFamily != null && (materialFamily.equals("wood") || materialFamily.startsWith("wood_"));
    }

    private static boolean hasPlantStockTag(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                continue;
            }
            if (PLANT_STOCK_TAGS.contains(tag)
                    || tag.startsWith("minecraft:") && (tag.endsWith("_flowers") || tag.endsWith("_leaves"))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasCeramicsMoldsContext(String path, IslandSignalDescriptor descriptor) {
        if (hasAnyPathToken(path, CERAMICS_MOLDS_STRONG_PATH_TOKENS)
                || hasPathToken(path, "mud")
                || hasPathToken(path, "adobe")
                || hasPathToken(path, "fire")) {
            return true;
        }
        String materialFamily = descriptor.materialFamily();
        return materialFamily != null && CERAMICS_MOLDS_MATERIAL_FAMILIES.contains(materialFamily);
    }

    private static boolean rolesWithin(IslandSignalDescriptor descriptor, Set<String> allowedRoles) {
        List<String> roles = descriptor.roleAlternatives();
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        for (String role : roles) {
            if (role == null || role.isBlank()) {
                continue;
            }
            if (!allowedRoles.contains(role)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasMatchingTag(Set<String> actualTags, Set<String> triggerTags) {
        if (actualTags == null || actualTags.isEmpty() || triggerTags == null || triggerTags.isEmpty()) {
            return false;
        }
        for (String actual : actualTags) {
            if (actual == null || actual.isBlank()) {
                continue;
            }
            for (String trigger : triggerTags) {
                if (actual.equals(trigger) || actual.startsWith(trigger + "/")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasPathToken(String path, String token) {
        if (path == null || path.isBlank() || token == null || token.isBlank()) {
            return false;
        }
        for (String part : path.split("[/_.-]+")) {
            if (token.equals(part)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnyPathToken(String path, Set<String> tokens) {
        if (path == null || path.isBlank() || tokens == null || tokens.isEmpty()) {
            return false;
        }
        for (String part : path.split("[/_.-]+")) {
            if (tokens.contains(part)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesDustPowderPath(String path) {
        return DUST_POWDER_EXACT_PATHS.contains(path)
                || hasAnyPathToken(path, DUST_POWDER_PATH_TOKENS)
                || path.endsWith("_dust")
                || path.endsWith("_dusts")
                || path.endsWith("_powder")
                || path.endsWith("_powders");
    }

    private static String identityPath(IslandSignalDescriptor descriptor) {
        if (descriptor == null || descriptor.identity() == null || descriptor.identity().itemId() == null) {
            return "";
        }
        String itemId = descriptor.identity().itemId();
        int colon = itemId.indexOf(':');
        return colon >= 0 ? itemId.substring(colon + 1) : itemId;
    }

    /**
     * Specific id paths that route to Ores & Raw Stock via the "ore
     * stockpile" id-fallback. Vanilla compressed material blocks plus
     * common modded equivalents. Anything matching {@code raw_*_block}
     * is also caught by a startsWith/endsWith check.
     */
    private static final Set<String> ORE_STOCK_BLOCK_IDS = Set.of(
            "iron_block",
            "gold_block",
            "diamond_block",
            "emerald_block",
            "lapis_block",
            "copper_block",
            "netherite_block",
            "amethyst_block",
            "quartz_block",
            "redstone_block"
    );

    private static final Set<String> METAL_STOCK_FORMS = Set.of(
            "ingot",
            "nugget",
            "plate",
            "rod",
            "wire",
            "gear",
            "sheet",
            "foil",
            "bolt",
            "screw",
            "ring"
    );

    private static final Set<String> METAL_STOCK_PATH_TOKENS = Set.of(
            "ingot",
            "ingots",
            "nugget",
            "nuggets",
            "plate",
            "plates",
            "rod",
            "rods",
            "wire",
            "wires",
            "gear",
            "gears",
            "sheet",
            "sheets",
            "foil",
            "foils",
            "bolt",
            "bolts",
            "screw",
            "screws",
            "ring",
            "rings"
    );

    private static final Set<String> METAL_PART_PATH_TOKENS = Set.of(
            "part",
            "parts",
            "component",
            "components"
    );

    private static final Set<String> METAL_STOCK_BLOCKED_PATH_TOKENS = Set.of(
            "wood",
            "wooden",
            "stick",
            "sticks",
            "stone",
            "clay",
            "ceramic",
            "bone",
            "blaze",
            "dust",
            "dusts",
            "powder",
            "powders",
            "ore",
            "ores",
            "raw"
    );

    private static final Set<String> ORE_STOCK_FORMS = Set.of(
            "raw",
            "ore",
            "crushed_ore",
            "impure_ore",
            "purified_ore"
    );

    private static final Set<String> ORE_STOCK_PATH_TOKENS = Set.of(
            "raw",
            "ore",
            "ores",
            "crushed",
            "impure",
            "purified"
    );

    private static final Set<String> DUST_POWDER_FORMS = Set.of(
            "dust",
            "powder",
            "ground"
    );

    private static final Set<String> DUST_POWDER_EXACT_PATHS = Set.of(
            "gunpowder",
            "blaze_powder",
            "glowstone_dust"
    );

    private static final Set<String> DUST_POWDER_PATH_TOKENS = Set.of(
            "dust",
            "dusts",
            "powder",
            "powders",
            "ground",
            "grit"
    );

    private static final Set<String> SEED_ALLOWED_ROLES = Set.of(
            "material",
            "natural_resource",
            "consumable",
            "ingredient",
            "food"
    );

    private static final Set<String> SEED_EXACT_PATHS = Set.of(
            "pitcher_pod"
    );

    private static final Set<String> SEED_BLOCKED_PATH_TOKENS = Set.of(
            "oil",
            "cake",
            "meal"
    );

    private static final Set<String> CROP_ALLOWED_ROLES = Set.of(
            "material",
            "natural_resource",
            "consumable",
            "ingredient",
            "food"
    );

    private static final Set<String> CROP_EXACT_PATHS = Set.of(
            "wheat",
            "carrot",
            "carrots",
            "potato",
            "potatoes",
            "poisonous_potato",
            "beetroot",
            "beetroots",
            "melon",
            "melon_slice",
            "pumpkin",
            "sugar_cane",
            "cactus",
            "kelp",
            "bamboo",
            "cocoa_beans",
            "nether_wart",
            "sweet_berries",
            "glow_berries"
    );

    private static final Set<String> CROP_BLOCKED_FORMS = Set.of(
            "seed",
            "food_cooked",
            "bottle",
            "bucket",
            "storage_block",
            "tool",
            "weapon",
            "armor_piece"
    );

    private static final Set<String> CROP_BLOCKED_PATH_TOKENS = Set.of(
            "seed",
            "seeds",
            "baked",
            "golden",
            "glistering",
            "dried",
            "pie",
            "soup",
            "stew",
            "bread",
            "cookie",
            "cake",
            "block",
            "blocks"
    );

    private static final Set<String> ORGANIC_ALLOWED_ROLES = Set.of(
            "material",
            "natural_resource",
            "building_block",
            "decorative_block",
            "consumable",
            "ingredient",
            "block",
            "food"
    );

    private static final Set<String> ORGANIC_EXACT_PATHS = Set.of(
            "string",
            "leather",
            "rabbit_hide",
            "rabbit_foot",
            "feather",
            "bone",
            "bone_meal",
            "slime_ball",
            "magma_cream",
            "rotten_flesh",
            "spider_eye",
            "ghast_tear",
            "phantom_membrane",
            "ink_sac",
            "glow_ink_sac",
            "turtle_scute",
            "armadillo_scute",
            "egg"
    );

    private static final Set<String> ORGANIC_MATERIAL_FAMILIES = Set.of(
            "leather",
            "bone",
            "slime",
            "scute",
            "wool",
            "fiber",
            "fibre"
    );

    private static final Set<String> ORGANIC_PATH_TOKENS = Set.of(
            "string",
            "leather",
            "hide",
            "hides",
            "fiber",
            "fibers",
            "fibre",
            "fibres",
            "wool",
            "feather",
            "bone",
            "slime",
            "scute",
            "flesh",
            "sinew",
            "tendon",
            "membrane",
            "sac",
            "egg"
    );

    private static final Set<String> ORGANIC_BLOCKED_PATH_TOKENS = Set.of(
            "block",
            "blocks",
            "carpet",
            "bed",
            "banner",
            "armor",
            "helmet",
            "chestplate",
            "leggings",
            "boots",
            "horse",
            "wolf",
            "painting",
            "frame"
    );

    private static final Set<String> WOOD_ALLOWED_ROLES = Set.of(
            "material",
            "natural_resource",
            "building_block",
            "block"
    );

    private static final Set<String> WOOD_STOCK_FORMS = Set.of(
            "log",
            "stripped_log",
            "wood"
    );

    private static final Set<String> WOOD_BLOCKED_FORMS = Set.of(
            "stairs",
            "slab",
            "wall",
            "fence",
            "fence_gate",
            "door",
            "trapdoor",
            "pane",
            "bars",
            "button",
            "pressure_plate",
            "ladder",
            "sign",
            "hanging_sign",
            "vehicle",
            "bed",
            "banner",
            "carpet",
            "candle",
            "torch",
            "lantern",
            "storage_block",
            "tool",
            "weapon",
            "armor_piece",
            "projectile",
            "bucket",
            "bottle",
            "seed",
            "sapling",
            "special"
    );

    private static final Set<String> WOOD_STOCK_TAGS = Set.of(
            "minecraft:logs",
            "minecraft:planks",
            "minecraft:bamboo_blocks"
    );

    private static final Set<String> WOOD_STOCK_PATH_TOKENS = Set.of(
            "stick",
            "sticks",
            "twig",
            "twigs",
            "log",
            "logs",
            "stem",
            "stems",
            "hyphae",
            "plank",
            "planks"
    );

    private static final Set<String> WOOD_CONTEXTUAL_STOCK_PATH_TOKENS = Set.of(
            "board",
            "boards",
            "lumber",
            "timber"
    );

    private static final Set<String> WOOD_BLOCKED_PATH_TOKENS = Set.of(
            "stairs",
            "stair",
            "slabs",
            "slab",
            "walls",
            "wall",
            "fences",
            "fence",
            "gate",
            "gates",
            "doors",
            "door",
            "trapdoors",
            "trapdoor",
            "windows",
            "window",
            "panes",
            "pane",
            "bars",
            "button",
            "buttons",
            "pressure",
            "plate",
            "plates",
            "ladders",
            "ladder",
            "sign",
            "signs",
            "boat",
            "boats",
            "minecart",
            "minecarts",
            "bed",
            "beds",
            "banner",
            "banners",
            "carpet",
            "carpets",
            "chest",
            "chests",
            "barrel",
            "barrels",
            "axe",
            "pickaxe",
            "sword",
            "shovel",
            "hoe",
            "helmet",
            "chestplate",
            "leggings",
            "boots"
    );

    private static final Set<String> PLANT_ALLOWED_ROLES = Set.of(
            "material",
            "natural_resource",
            "building_block",
            "decorative_block",
            "block"
    );

    private static final Set<String> PLANT_STOCK_FORMS = Set.of(
            "sapling",
            "bulb"
    );

    private static final Set<String> PLANT_BLOCKED_FORMS = Set.of(
            "seed",
            "food_raw",
            "food_cooked",
            "storage_block",
            "stairs",
            "slab",
            "wall",
            "fence",
            "fence_gate",
            "door",
            "trapdoor",
            "pane",
            "bars",
            "button",
            "pressure_plate",
            "ladder",
            "sign",
            "hanging_sign",
            "vehicle",
            "pot",
            "tool",
            "weapon",
            "armor_piece",
            "projectile",
            "bucket",
            "bottle",
            "special"
    );

    private static final Set<String> PLANT_STOCK_TAGS = Set.of(
            "minecraft:saplings",
            "minecraft:flowers",
            "minecraft:small_flowers",
            "minecraft:tall_flowers",
            "minecraft:leaves"
    );

    private static final Set<String> PLANT_EXACT_PATHS = Set.of(
            "short_grass",
            "tall_grass",
            "fern",
            "large_fern",
            "dead_bush",
            "vine",
            "vines",
            "weeping_vines",
            "twisting_vines",
            "lily_pad",
            "spore_blossom",
            "hanging_roots",
            "big_dripleaf",
            "small_dripleaf",
            "glow_lichen",
            "seagrass",
            "sea_pickle",
            "dandelion",
            "poppy",
            "blue_orchid",
            "allium",
            "azure_bluet",
            "red_tulip",
            "orange_tulip",
            "white_tulip",
            "pink_tulip",
            "oxeye_daisy",
            "cornflower",
            "lily_of_the_valley",
            "sunflower",
            "lilac",
            "rose_bush",
            "peony",
            "wildflowers",
            "torchflower",
            "pitcher_plant",
            "brown_mushroom",
            "red_mushroom",
            "crimson_fungus",
            "warped_fungus",
            "crimson_roots",
            "warped_roots",
            "nether_sprouts"
    );

    private static final Set<String> PLANT_BLOCKED_PATH_TOKENS = Set.of(
            "seed",
            "seeds",
            "crop",
            "crops",
            "pot",
            "potted",
            "flowerpot",
            "flower_pot",
            "plank",
            "planks",
            "log",
            "logs",
            "wood",
            "stem",
            "stems",
            "hyphae",
            "grass_block",
            "moss_block"
    );

    private static final Set<String> CERAMICS_MOLDS_ALLOWED_ROLES = Set.of(
            "material",
            "natural_resource",
            "building_block",
            "decorative_block",
            "utility",
            "ingredient",
            "block"
    );

    private static final Set<String> CERAMICS_MOLDS_EXACT_PATHS = Set.of(
            "clay",
            "clay_ball",
            "brick",
            "bricks",
            "terracotta",
            "flower_pot",
            "decorated_pot",
            "vessel"
    );

    private static final Set<String> CERAMICS_MOLDS_MATERIAL_FAMILIES = Set.of(
            "clay",
            "ceramic",
            "terracotta",
            "pottery"
    );

    private static final Set<String> CERAMICS_MOLDS_STRONG_PATH_TOKENS = Set.of(
            "clay",
            "ceramic",
            "terracotta",
            "pottery",
            "sherd",
            "mold",
            "molds",
            "mould",
            "moulds",
            "vessel",
            "vessels"
    );

    private static final Set<String> CERAMICS_MOLDS_CONTEXTUAL_PATH_TOKENS = Set.of(
            "brick",
            "bricks",
            "pot",
            "pots",
            "unfired"
    );

    private static final Set<String> CERAMICS_MOLDS_BLOCKED_FORMS = Set.of(
            "stairs",
            "slab",
            "wall",
            "fence",
            "fence_gate",
            "door",
            "trapdoor",
            "pane",
            "bars",
            "button",
            "pressure_plate",
            "ladder",
            "sign",
            "hanging_sign",
            "storage_block",
            "tool",
            "weapon",
            "armor_piece"
    );

    private static final Set<String> CERAMICS_MOLDS_BLOCKED_PATH_TOKENS = Set.of(
            "stairs",
            "stair",
            "slab",
            "slabs",
            "wall",
            "walls",
            "fence",
            "fences",
            "door",
            "doors",
            "trapdoor",
            "trapdoors",
            "button",
            "buttons",
            "pressure",
            "plate",
            "plates",
            "nether",
            "stone",
            "deepslate",
            "blackstone",
            "prismarine",
            "quartz",
            "end"
    );

    /**
     * Id-suffix fallbacks for form-keyed templates whose `form` facet
     * coverage is incomplete. Stuff like Create's `dark_oak_window`
     * has role=building_block but form=null, so the WINDOWS template
     * relies on the `_window` suffix to catch it.
     */
    private static Set<String> idSuffixFallbacksFor(IslandSuggestionTemplate template) {
        return switch (template) {
            case STAIRS -> Set.of("_stairs");
            case SLABS -> Set.of("_slab");
            case WALLS -> Set.of("_wall");
            case DOORS -> Set.of("_door", "_trapdoor", "_fence_gate");
            case FENCES -> Set.of("_fence");
            case WINDOWS -> Set.of("_window", "_pane", "glass");
            default -> Set.of();
        };
    }

    private static Set<String> organizationGroupTriggersFor(IslandSuggestionTemplate template) {
        return switch (template) {
            case SEEDS -> Set.of("seeds");
            case CROPS -> Set.of("crops");
            case ORGANIC_MATERIALS -> Set.of("organic_materials");
            case FOOD -> Set.of("food");
            case TOOLS -> Set.of("tools");
            case WEAPONS -> Set.of("weapons");
            case ARMOR -> Set.of("armor");
            case LIGHTING -> Set.of("lighting");
            case METAL_STOCK -> Set.of("metal_stock");
            case GEMS_CRYSTALS -> Set.of("gems_crystals");
            case ORES_RAW_STOCK -> Set.of("ores_raw_stock");
            case DUSTS_POWDERS -> Set.of("dusts_powders");
            case WOOD -> Set.of("wood");
            case PLANTS -> Set.of("plants");
            case CERAMICS_MOLDS -> Set.of("ceramics_molds");
            case MATERIALS -> Set.of("materials");
            case STORAGE -> Set.of("storage");
            case STAIRS -> Set.of("stairs");
            case SLABS -> Set.of("slabs");
            case WALLS -> Set.of("walls");
            case DOORS -> Set.of("doors");
            case FENCES -> Set.of("fences");
            case WINDOWS -> Set.of("windows");
            case BUILDING -> Set.of("building_blocks");
            case DECORATION -> Set.of("decoration");
            case NATURAL -> Set.of("natural");
            case WORKBENCHES -> Set.of("workbenches");
            case MECHANISMS -> Set.of("mechanisms");
            case REDSTONE -> Set.of("redstone");
            case UPGRADES -> Set.of("upgrades");
            case TRANSPORT -> Set.of("transport");
            case UTILITY -> Set.of("utility");
            case CURIOSITY -> Set.of("curiosities");
            case MISC -> Set.of("miscellaneous");
        };
    }

    private static boolean hasMatchingOrganizationGroup(List<String> actualGroups, Set<String> triggerGroups) {
        if (actualGroups == null || actualGroups.isEmpty() || triggerGroups == null || triggerGroups.isEmpty()) {
            return false;
        }
        for (String group : actualGroups) {
            if (group != null && triggerGroups.contains(group)) {
                return true;
            }
        }
        return false;
    }

    private static boolean blockRoleShouldYieldToLaterOrganizationGroup(List<String> actualGroups) {
        if (actualGroups == null || actualGroups.isEmpty()) {
            return false;
        }
        for (String group : actualGroups) {
            if (POST_BUILDING_ORGANIZATION_GROUPS.contains(group)) {
                return true;
            }
        }
        return false;
    }

    private static final Set<String> POST_BUILDING_ORGANIZATION_GROUPS = Set.of(
            "decoration",
            "natural",
            "workbenches",
            "mechanisms",
            "redstone",
            "upgrades",
            "transport",
            "utility",
            "curiosities",
            "miscellaneous"
    );

    /**
     * First template that matches the descriptor in declaration order, or
     * {@code null} when nothing fires. The chip-suggestion path treats
     * this as "no chip" — it's the right answer when role / class / tag
     * signals are all absent (e.g. an unmatched modded item with no
     * precomputed layer).
     *
     * <p>When multiple templates match (e.g. a multi-valued role like
     * {@code track_signal} with both {@code mechanism} and
     * {@code redstone_component}), an activity tie-break narrows the
     * choice — see {@link #firstMatchWithActivityTieBreak}.
     */
    public static IslandSuggestionTemplate firstMatch(IslandSignalDescriptor descriptor) {
        return firstMatchWithActivityTieBreak(descriptor);
    }

    /**
     * Like {@link #firstMatch(IslandSignalDescriptor)} but never returns
     * {@code null} — falls back to {@link #MISC} for items without a
     * matching template. Used by the populate-time classifier so every
     * synthesised stack lands on some island, mirroring "every chip
     * action drops the item somewhere" UX.
     */
    public static IslandSuggestionTemplate firstMatchOrMisc(IslandSignalDescriptor descriptor) {
        IslandSuggestionTemplate match = firstMatch(descriptor);
        return match != null ? match : MISC;
    }

    /**
     * Subsystem-aware extension of {@link #firstMatchOrMisc}. When the
     * descriptor carries any subsystem id passing the supplied
     * {@code subsystemQualifier}, returns a synthetic
     * {@link IslandTemplateMatch} keyed on that subsystem (with the
     * resolved parent template providing color and cluster placement).
     * Otherwise returns the parent template alone.
     *
     * <p>{@code subsystemQualifier == null} disables the subsystem
     * branch — equivalent to legacy behavior. Trophies (rarity =
     * {@code unique} or role = {@code trophy}) bypass the subsystem
     * branch because they belong on display, not filed.
     */
    public static IslandTemplateMatch firstMatchExtendedOrMisc(
            IslandSignalDescriptor descriptor,
            Predicate<String> subsystemQualifier
    ) {
        return firstMatchExtendedOrMisc(descriptor, subsystemQualifier, id -> false);
    }

    /**
     * Organization-aware extension of {@link #firstMatchOrMisc}. A
     * count-qualified {@code organization_group} can override broad built-in
     * parents because it is the direct "where would the player put this"
     * signal. {@code mod_subsystem} stays semantic/query evidence unless a
     * future template explicitly opts into subsystem wall sections.
     */
    public static IslandTemplateMatch firstMatchExtendedOrMisc(
            IslandSignalDescriptor descriptor,
            Predicate<String> subsystemQualifier,
            Predicate<String> organizationGroupQualifier
    ) {
        if (descriptor == null) {
            return IslandTemplateMatch.of(MISC);
        }
        IslandSuggestionTemplate parent = firstMatchOrMisc(descriptor);
        if (isTrophy(descriptor)) {
            return IslandTemplateMatch.of(CURIOSITY);
        }
        if (organizationGroupQualifier != null && parent.allowsOrganizationGrouping()) {
            for (String groupId : descriptor.organizationGroups()) {
                if (groupId == null || groupId.isBlank()) {
                    continue;
                }
                if (organizationGroupQualifier.test(groupId)) {
                    return IslandTemplateMatch.organizationGroup(parent, groupId, null);
                }
            }
        }
        if (subsystemQualifier != null && parent.allowsSubsystemGrouping()) {
            for (String subsystemId : descriptor.subsystems()) {
                if (subsystemId == null || subsystemId.isBlank()) {
                    continue;
                }
                if (subsystemQualifier.test(subsystemId)) {
                    return IslandTemplateMatch.subsystem(parent, subsystemId, null);
                }
            }
        }
        return IslandTemplateMatch.of(parent);
    }

    /**
     * True for items the dataset marks as a trophy — either via
     * {@code role = "trophy"} or {@code rarity = "unique"} (the rarest
     * tier in the canonical schema; nether_star / dragon_egg /
     * wither_skeleton_skull and modded analogs).
     */
    public static boolean isTrophy(IslandSignalDescriptor descriptor) {
        if (descriptor == null) {
            return false;
        }
        if ("trophy".equals(descriptor.role())) {
            return true;
        }
        return "unique".equals(descriptor.rarity());
    }

    private static IslandSuggestionTemplate firstMatchWithActivityTieBreak(IslandSignalDescriptor descriptor) {
        if (descriptor == null) {
            return null;
        }
        ArrayList<IslandSuggestionTemplate> matches = new ArrayList<>(2);
        for (IslandSuggestionTemplate template : values()) {
            if (template.matches(descriptor)) {
                matches.add(template);
                if (matches.size() >= 3) {
                    break;
                }
            }
        }
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        // Multiple templates fired (e.g. a multi-value role list, or a
        // role + class signal pointing at different templates). When the
        // descriptor carries activity hints, prefer a template whose
        // activityTriggers intersect — that's the more specific signal.
        // Fall back to declaration order when activities don't help.
        List<String> activities = descriptor.activities();
        if (activities != null && !activities.isEmpty()) {
            for (String activity : activities) {
                if (activity == null || activity.isBlank()) {
                    continue;
                }
                for (IslandSuggestionTemplate candidate : matches) {
                    if (candidate.activityTriggers.contains(activity)) {
                        return candidate;
                    }
                }
            }
        }
        return matches.get(0);
    }
}
