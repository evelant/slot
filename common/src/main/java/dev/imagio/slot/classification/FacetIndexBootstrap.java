package dev.imagio.slot.classification;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FacetIndexBootstrap {

    public static final String VANILLA_BASE_RESOURCE = "/data/slot/classification/vanilla-base.json";
    public static final String PER_MOD_INDEX_RESOURCE = "/data/slot/classification/per-mod/index.json";
    public static final String PER_MOD_RESOURCE_PREFIX = "/data/slot/classification/per-mod/";
    public static final String DATAPACK_LAYER_PREFIX = "classification/layers";

    private static final System.Logger LOGGER = System.getLogger(FacetIndexBootstrap.class.getName());

    private FacetIndexBootstrap() {
    }

    @FunctionalInterface
    public interface LayerReaderFactory {
        Reader open() throws IOException;
    }

    public record NamedLayerResource(String description, LayerReaderFactory readerFactory) {
        public NamedLayerResource {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("classification layer resource description is required");
            }
            if (readerFactory == null) {
                throw new IllegalArgumentException("classification layer reader factory is required");
            }
        }
    }

    public static FacetIndex loadVanillaBase() {
        return loadResource(VANILLA_BASE_RESOURCE);
    }

    /**
     * Load {@code vanilla-base.json} plus every per-mod layer listed in
     * {@code per-mod/index.json}, merging into a single FacetIndex with
     * per-mod entries layered on top of vanilla. This is what the
     * {@link FacetIndexHolder} singleton serves at runtime — callers see
     * one index spanning every namespace SLOT ships data for.
     *
     * <p>If a listed per-mod resource is missing or malformed, we log
     * and skip it; missing per-mod data is not fatal.</p>
     *
     * <p>Mods whose items aren't actually present at runtime simply
     * won't be queried — there's no harm in shipping their data eagerly.
     * The runtime can later replace this with mod-detection-based lazy
     * loading if memory becomes a concern; for now (~7K extra entries
     * across 9 mods) the cost is trivial relative to vanilla-base's
     * ~1.5K entries.</p>
     */
    public static FacetIndex loadAll() {
        return loadAllWithLayers(List.of());
    }

    public static FacetIndex loadAllWithLayers(Collection<NamedLayerResource> datapackLayers) {
        FacetIndex base = loadVanillaBase();
        List<String> modIds = readPerModManifest();
        FacetIndex merged = base;
        for (String modId : modIds) {
            FacetIndex perMod = loadResource(PER_MOD_RESOURCE_PREFIX + modId + ".json");
            if (perMod.isEmpty()) {
                continue;
            }
            merged = merged.mergedWith(perMod);
        }
        if (!modIds.isEmpty()) {
            LOGGER.log(Level.INFO,
                    "[SLOT] FacetIndex bootstrap merged " + modIds.size()
                            + " per-mod layer(s); total entries: " + merged.size());
        }
        int extraCount = 0;
        if (datapackLayers != null) {
            for (NamedLayerResource layer : datapackLayers) {
                FacetIndex datapack = loadNamedLayer(layer);
                if (datapack.isEmpty()) {
                    continue;
                }
                merged = merged.mergedWith(datapack);
                extraCount++;
            }
        }
        if (extraCount > 0) {
            LOGGER.log(Level.INFO,
                    "[SLOT] FacetIndex bootstrap merged " + extraCount
                            + " datapack classification layer(s); total entries: " + merged.size());
        }
        return merged;
    }

    public static FacetIndex loadResource(String resourcePath) {
        InputStream stream = FacetIndexBootstrap.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            LOGGER.log(Level.WARNING,
                    "[SLOT] FacetIndex resource missing: " + resourcePath + "; falling back to empty index");
            return FacetIndex.empty();
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            FacetIndex index = FacetIndex.load(reader);
            LOGGER.log(Level.INFO,
                    "[SLOT] FacetIndex loaded " + index.size() + " role-bearing entries from " + resourcePath);
            return index;
        } catch (IOException | RuntimeException exception) {
            LOGGER.log(Level.ERROR,
                    "[SLOT] FacetIndex failed to load " + resourcePath + "; falling back to empty index",
                    exception);
            return FacetIndex.empty();
        }
    }

    private static FacetIndex loadNamedLayer(NamedLayerResource layer) {
        try (Reader reader = layer.readerFactory().open()) {
            FacetIndex index = FacetIndex.load(reader);
            LOGGER.log(Level.INFO,
                    "[SLOT] FacetIndex loaded " + index.size()
                            + " role-bearing entries from " + layer.description());
            return index;
        } catch (IOException | RuntimeException exception) {
            LOGGER.log(Level.ERROR,
                    "[SLOT] FacetIndex failed to load " + layer.description() + "; skipping layer",
                    exception);
            return FacetIndex.empty();
        }
    }

    /**
     * Read {@link #PER_MOD_INDEX_RESOURCE} and return the list of mod ids
     * to load. Returns an empty list when the manifest is missing or
     * malformed — the bootstrap then degrades to vanilla-base only.
     */
    private static List<String> readPerModManifest() {
        InputStream stream = FacetIndexBootstrap.class.getResourceAsStream(PER_MOD_INDEX_RESOURCE);
        if (stream == null) {
            return List.of();
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (parsed == null || !parsed.isJsonObject()) {
                LOGGER.log(Level.WARNING,
                        "[SLOT] per-mod manifest is not a JSON object: " + PER_MOD_INDEX_RESOURCE);
                return List.of();
            }
            JsonObject root = parsed.getAsJsonObject();
            JsonElement modsElement = root.get("mods");
            if (modsElement == null || !modsElement.isJsonArray()) {
                return List.of();
            }
            JsonArray modsArr = modsElement.getAsJsonArray();
            List<String> ids = new ArrayList<>(modsArr.size());
            for (JsonElement m : modsArr) {
                if (m != null && m.isJsonPrimitive() && m.getAsJsonPrimitive().isString()) {
                    String id = m.getAsString();
                    // Defensive sanitization — mod ids must be safe to splice
                    // into a resource path. Anything outside [a-z0-9_] is
                    // either a mistake or an attempt to escape the per-mod
                    // directory (e.g. "../foo").
                    if (id.matches("[a-z0-9_]+")) {
                        ids.add(id);
                    } else {
                        LOGGER.log(Level.WARNING,
                                "[SLOT] per-mod manifest entry rejected (unsafe id): " + id);
                    }
                }
            }
            return ids;
        } catch (IOException | RuntimeException exception) {
            LOGGER.log(Level.ERROR,
                    "[SLOT] failed to read per-mod manifest " + PER_MOD_INDEX_RESOURCE,
                    exception);
            return List.of();
        }
    }
}
