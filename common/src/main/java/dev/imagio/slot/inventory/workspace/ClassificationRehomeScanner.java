package dev.imagio.slot.inventory.workspace;

import dev.imagio.slot.inventory.query.InventoryAuthoritySnapshot;
import dev.imagio.slot.inventory.query.InventoryEntrySnapshot;
import dev.imagio.slot.inventory.query.InventorySourceSnapshot;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.StorageAccessRegistry;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import dev.imagio.slot.workflow.domain.ClaimedChest;
import dev.imagio.slot.workflow.domain.WorkflowDomainRuntime;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared scan step for classifier rehome commands. Loader adapters only
 * provide the player/runtime and the platform signal extractor; this class
 * walks every carried source plus every currently accessible claimed chest
 * through the common storage abstraction.
 */
public final class ClassificationRehomeScanner {
    private ClassificationRehomeScanner() {
    }

    public static ScanResult scan(ServerPlayer player, WorkflowDomainRuntime runtime) {
        if (player == null || runtime == null) {
            return new ScanResult(List.of(), 0, 0, 0, 0, List.of("missing_player_or_runtime"));
        }
        if (!StorageAccessRegistry.isInstalled()) {
            return new ScanResult(List.of(), 0, 0, 0, 0, List.of("storage_access_not_installed"));
        }

        ArrayList<ItemStack> stacks = new ArrayList<>();
        ArrayList<String> diagnostics = new ArrayList<>();
        int carriedStacks = scanCarried(player, stacks, diagnostics);

        ChestScanResult chestScan = scanClaimedChests(player.getServer(), runtime, stacks, diagnostics);
        return new ScanResult(
                stacks,
                carriedStacks,
                chestScan.chestStacks(),
                chestScan.readableClaimedChests(),
                chestScan.skippedClaimedChests(),
                diagnostics
        );
    }

    private static int scanCarried(
            ServerPlayer player,
            List<ItemStack> stacks,
            List<String> diagnostics
    ) {
        CarriedSourceAccess carried = StorageAccessRegistry.carriedSourceAccess();
        InventoryAuthoritySnapshot authority;
        try {
            authority = carried.currentAuthority(player);
        } catch (RuntimeException exception) {
            diagnostics.add("carried_scan_failed:" + safeMessage(exception));
            return 0;
        }
        if (authority == null) {
            diagnostics.add("carried_scan_failed:null_authority");
            return 0;
        }

        int carriedStacks = 0;
        for (InventorySourceSnapshot source : authority.sourcesById().values()) {
            if (source == null) {
                continue;
            }
            for (InventoryEntrySnapshot entry : source.entries()) {
                if (entry == null || !entry.present()) {
                    continue;
                }
                ItemStack stack = entry.stack();
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                stacks.add(stack.copy());
                carriedStacks++;
            }
        }
        return carriedStacks;
    }

    private static ChestScanResult scanClaimedChests(
            MinecraftServer server,
            WorkflowDomainRuntime runtime,
            List<ItemStack> stacks,
            List<String> diagnostics
    ) {
        if (server == null) {
            diagnostics.add("claimed_chest_scan_failed:missing_server");
            return new ChestScanResult(0, 0, runtime.snapshot().claimedChestMap().chests().size());
        }
        WorldStorageAccess world = StorageAccessRegistry.worldStorageAccess();
        int chestStacks = 0;
        int readable = 0;
        int skipped = 0;
        for (ClaimedChest chest : runtime.snapshot().claimedChestMap().chests()) {
            if (chest == null) {
                skipped++;
                continue;
            }
            WorldStorageAccess.Target target = new WorldStorageAccess.Target.Chest(chest);
            boolean accessible;
            try {
                accessible = world.isAccessible(server, target);
            } catch (RuntimeException exception) {
                skipped++;
                diagnostics.add("claimed_chest_access_failed:" + chest.storageId() + ":" + safeMessage(exception));
                continue;
            }
            if (!accessible) {
                skipped++;
                continue;
            }
            readable++;
            List<WorldStorageAccess.SlotContent> contents;
            try {
                contents = world.enumerate(server, target);
            } catch (RuntimeException exception) {
                skipped++;
                readable--;
                diagnostics.add("claimed_chest_enumerate_failed:" + chest.storageId() + ":" + safeMessage(exception));
                continue;
            }
            WorkspaceStorageMemoryStore store = WorkspaceStorageMemoryStore.forServer(server);
            if (store != null) {
                try {
                    int slots = Math.max(0, world.slotCount(server, target));
                    store.observe(
                            StorageTargetRef.claimed(chest, true, false, false),
                            slots,
                            contents,
                            0L,
                            "classification_rehome_scan");
                } catch (RuntimeException exception) {
                    diagnostics.add("claimed_chest_memory_observe_failed:" + chest.storageId() + ":" + safeMessage(exception));
                }
            }
            for (WorldStorageAccess.SlotContent content : contents) {
                if (content == null || content.stack().isEmpty()) {
                    continue;
                }
                stacks.add(content.stack().copy());
                chestStacks++;
            }
        }
        return new ChestScanResult(chestStacks, readable, skipped);
    }

    private static String safeMessage(RuntimeException exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "runtime_exception";
        }
        return exception.getMessage().replace('\n', ' ').replace('\r', ' ');
    }

    private record ChestScanResult(
            int chestStacks,
            int readableClaimedChests,
            int skippedClaimedChests
    ) {
    }

    public record ScanResult(
            List<ItemStack> stacks,
            int carriedStacks,
            int chestStacks,
            int readableClaimedChests,
            int skippedClaimedChests,
            List<String> diagnostics
    ) {
        public ScanResult {
            ArrayList<ItemStack> copiedStacks = new ArrayList<>();
            if (stacks != null) {
                for (ItemStack stack : stacks) {
                    if (stack != null && !stack.isEmpty()) {
                        copiedStacks.add(stack.copy());
                    }
                }
            }
            stacks = List.copyOf(copiedStacks);
            diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
            carriedStacks = Math.max(0, carriedStacks);
            chestStacks = Math.max(0, chestStacks);
            readableClaimedChests = Math.max(0, readableClaimedChests);
            skippedClaimedChests = Math.max(0, skippedClaimedChests);
        }

        public boolean failed() {
            return stacks.isEmpty() && diagnostics.stream().anyMatch(diagnostic ->
                    "storage_access_not_installed".equals(diagnostic)
                            || "missing_player_or_runtime".equals(diagnostic));
        }
    }
}
