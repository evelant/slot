package dev.imagio.slot.forge.classification;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.classification.FacetIndex;
import dev.imagio.slot.classification.FacetIndexBootstrap;
import dev.imagio.slot.classification.FacetIndexBootstrap.NamedLayerResource;
import dev.imagio.slot.classification.FacetIndexHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class Forge120ClassificationLayerReloadListener implements PreparableReloadListener {

    private Forge120ClassificationLayerReloadListener() {
    }

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(Forge120ClassificationLayerReloadListener::onAddReloadListener);
    }

    private static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new Forge120ClassificationLayerReloadListener());
    }

    @Override
    public CompletableFuture<Void> reload(
            PreparationBarrier stage,
            ResourceManager resourceManager,
            ProfilerFiller preparationsProfiler,
            ProfilerFiller reloadProfiler,
            Executor backgroundExecutor,
            Executor gameExecutor
    ) {
        CompletableFuture<FacetIndex> prepared = CompletableFuture.supplyAsync(
                () -> load(resourceManager),
                backgroundExecutor
        );
        return prepared
                .thenCompose(stage::wait)
                .thenAcceptAsync(FacetIndexHolder::install, gameExecutor);
    }

    @Override
    public String getName() {
        return "slot:classification_layers";
    }

    private static FacetIndex load(ResourceManager resourceManager) {
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                FacetIndexBootstrap.DATAPACK_LAYER_PREFIX,
                location -> SlotCommon.MOD_ID.equals(location.getNamespace())
                        && location.getPath().endsWith(".json")
        );
        List<Map.Entry<ResourceLocation, Resource>> entries = new ArrayList<>(resources.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getKey));

        List<NamedLayerResource> layers = new ArrayList<>(entries.size());
        for (Map.Entry<ResourceLocation, Resource> entry : entries) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();
            layers.add(new NamedLayerResource(
                    "datapack:" + location,
                    resource::openAsReader
            ));
        }
        return FacetIndexBootstrap.loadAllWithLayers(layers);
    }
}
