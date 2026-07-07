package dev.imagio.slot.forge;

import dev.imagio.slot.platform.SlotResourceAccess;
import dev.imagio.slot.platform.SlotStackAccess;
import dev.imagio.slot.forge.compat.ae2.Ae2ForgeCompat;
import dev.imagio.slot.forge.compat.tfc.TfcWorldDisplayStorageDelegate;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.workspace.SlotWorkspaceViewModel;
import dev.imagio.slot.forge.storage.ForgeCarriedSourceAccess;
import dev.imagio.slot.forge.storage.ForgeWorldStorageAccess;

final class Forge120Platform {
    private Forge120Platform() {
    }

    static void bootstrap() {
        SlotResourceAccess.install(new Forge120ResourceAccess());
        SlotStackAccess.install(new Forge120StackAccess());
        SlotWorkspaceViewModel.setGhostStackResolver(Forge120GhostStackFactory::resolve);
        StorageAccessRegistry.installCarriedSourceAccess(new ForgeCarriedSourceAccess());
        ForgeWorldStorageAccess worldStorageAccess = new ForgeWorldStorageAccess();
        worldStorageAccess.registerDelegate(new TfcWorldDisplayStorageDelegate());
        Ae2ForgeCompat.registerWorldStorage(worldStorageAccess);
        StorageAccessRegistry.installWorldStorageAccess(worldStorageAccess);
    }
}
