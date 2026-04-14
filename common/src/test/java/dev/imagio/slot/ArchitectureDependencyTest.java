package dev.imagio.slot;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectureDependencyTest {
    private static final Path REPO_ROOT = locateRepoRoot();
    private static final Path SOURCE_ROOT = REPO_ROOT.resolve("common/src/main/java/dev/imagio/slot");
    private static final Path LEGACY_ARCHIVE_ROOT = REPO_ROOT.resolve("archive/legacy-authority");
    private static final List<String> GUARDED_PACKAGES = List.of(
            "session",
            "source",
            "intent",
            "operation",
            "registry",
            "projection",
            "workflow",
            "workflow/domain",
            "action/session",
            "inventory/action",
            "inventory/core",
            "client/policy",
            "inventory/kernel",
            "inventory/query",
            "storage/provider"
    );
    private static final List<String> ARCHIVED_AUTHORITY_PATHS = List.of(
            "action/session",
            "capability",
            "client",
            "intent",
            "network",
            "operation",
            "policy",
            "projection",
            "recent",
            "session",
            "source",
            "storage/adapter",
            "storage/provider",
            "inventory/kernel",
            "inventory/CarriedPlacementPolicy.java"
    );
    private static final List<String> FORBIDDEN_IMPORT_PREFIXES = List.of(
            "import dev.imagio.slot.client.screen",
            "import dev.imagio.slot.compat",
            "import dev.imagio.slot.neoforge"
    );

    private static Path locateRepoRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.exists(current.resolve("settings.gradle"))
                    && Files.exists(current.resolve("common/src/main/java/dev/imagio/slot/SlotCommon.java"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Unable to locate repository root from " + Path.of("").toAbsolutePath());
    }

    @Test
    void archivedAuthorityPackagesAreOutOfCompiledSourceRoots() {
        List<String> remaining = ARCHIVED_AUTHORITY_PATHS.stream()
                .map(SOURCE_ROOT::resolve)
                .filter(Files::exists)
                .map(Path::toString)
                .toList();

        assertTrue(
                remaining.isEmpty(),
                () -> "Legacy authority paths are still under src/main: " + remaining
        );
    }

    @Test
    void legacyArchiveTreeExistsForSearchableReference() {
        assertTrue(
                Files.exists(LEGACY_ARCHIVE_ROOT),
                () -> "Expected legacy archive tree at " + LEGACY_ARCHIVE_ROOT
        );
    }

    @Test
    void kernelPackagesDoNotImportUiOrPlatformLayers() throws IOException {
        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (String guardedPackage : GUARDED_PACKAGES) {
            Path packageRoot = SOURCE_ROOT.resolve(guardedPackage);
            if (!Files.exists(packageRoot)) {
                continue;
            }

            try (Stream<Path> files = Files.walk(packageRoot)) {
                files.filter(path -> path.toString().endsWith(".java"))
                        .sorted()
                        .forEach(path -> {
                            try {
                                List<String> matchingImports = Files.readAllLines(path).stream()
                                        .map(String::trim)
                                        .filter(line -> FORBIDDEN_IMPORT_PREFIXES.stream().anyMatch(line::startsWith))
                                        .toList();
                                if (!matchingImports.isEmpty()) {
                                    violations.put(path.toString(), matchingImports);
                                }
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }
                        });
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Forbidden architecture imports detected: " + violations
        );
    }

    @Test
    void migratedClientMutationServicesDoNotUseRawSlotBoundsOrVanillaSlotConstants() throws IOException {
        List<Path> guardedFiles = List.of(
                SOURCE_ROOT.resolve("client/screen/PlayerCarriedTransferService.java"),
                SOURCE_ROOT.resolve("client/screen/WorkspaceCarriedTransferService.java"),
                SOURCE_ROOT.resolve("client/screen/QuickAccessMenuSlotPlanner.java"),
                SOURCE_ROOT.resolve("client/screen/QuickAccessMenuOperations.java"),
                SOURCE_ROOT.resolve("client/screen/QuickAccessLoadoutService.java"),
                SOURCE_ROOT.resolve("client/screen/QuickAccessBackpackFallbackSupport.java"),
                SOURCE_ROOT.resolve("client/screen/QuickAccessInventoryActionService.java"),
                SOURCE_ROOT.resolve("client/screen/QuickAccessFollowUpExecutor.java")
        );

        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (Path file : guardedFiles) {
            if (!Files.exists(file)) {
                continue;
            }

            List<String> matches = Files.readAllLines(file).stream()
                    .map(String::trim)
                    .filter(line -> line.contains("menu.slots.size()") || line.contains("InventoryMenu."))
                    .toList();
            if (!matches.isEmpty()) {
                violations.put(file.toString(), matches);
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Migrated client mutation services regressed to raw slot math: " + violations
        );
    }

    @Test
    void migratedServerMutationServicesDoNotUseVanillaSlotConstantsForTargetability() throws IOException {
        List<Path> guardedFiles = List.of(
                REPO_ROOT.resolve("neoforge/src/main/java/dev/imagio/slot/neoforge/network/BackpackTransferOperations.java"),
                REPO_ROOT.resolve("neoforge/src/main/java/dev/imagio/slot/neoforge/network/CursorTransferSupport.java"),
                REPO_ROOT.resolve("neoforge/src/main/java/dev/imagio/slot/neoforge/network/CursorPickupOperations.java"),
                REPO_ROOT.resolve("neoforge/src/main/java/dev/imagio/slot/neoforge/network/CursorVoidOperations.java"),
                REPO_ROOT.resolve("neoforge/src/main/java/dev/imagio/slot/neoforge/network/CursorDropOperations.java")
        );

        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (Path file : guardedFiles) {
            if (!Files.exists(file)) {
                continue;
            }

            List<String> matches = Files.readAllLines(file).stream()
                    .map(String::trim)
                    .filter(line -> line.contains("InventoryMenu."))
                    .toList();
            if (!matches.isEmpty()) {
                violations.put(file.toString(), matches);
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Migrated server mutation services regressed to vanilla slot constants: " + violations
        );
    }

    @Test
    void sessionProjectionAndScreensDoNotImportCompatBridgesDirectly() throws IOException {
        List<Path> guardedRoots = List.of(
                SOURCE_ROOT.resolve("client/session"),
                SOURCE_ROOT.resolve("client/screen"),
                SOURCE_ROOT.resolve("projection")
        );

        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (Path root : guardedRoots) {
            if (!Files.exists(root)) {
                continue;
            }

            try (Stream<Path> files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".java"))
                        .sorted()
                        .forEach(file -> {
                            try {
                                List<String> matches = Files.readAllLines(file).stream()
                                        .map(String::trim)
                                        .filter(line -> line.startsWith("import dev.imagio.slot.compat"))
                                        .toList();
                                if (!matches.isEmpty()) {
                                    violations.put(file.toString(), matches);
                                }
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }
                        });
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Session/projection/screen adapters regressed to direct compat imports: " + violations
        );
    }

    @Test
    void legacyAdapterAndCapabilityResolverSeamsAreDeleted() {
        List<Path> forbiddenFiles = List.of(
                SOURCE_ROOT.resolve("storage/adapter/ExternalStorageAdapter.java"),
                SOURCE_ROOT.resolve("storage/adapter/ExternalStorageAdapterContext.java"),
                SOURCE_ROOT.resolve("storage/adapter/ExternalStorageAdapterSession.java"),
                SOURCE_ROOT.resolve("storage/adapter/ExternalStorageAdapterRegistry.java"),
                SOURCE_ROOT.resolve("capability/MenuCapabilityDescriptorResolver.java"),
                SOURCE_ROOT.resolve("inventory/integration/LegacyIntegrationAdapters.java")
        );

        List<String> existing = forbiddenFiles.stream()
                .filter(Files::exists)
                .map(Path::toString)
                .toList();

        assertTrue(
                existing.isEmpty(),
                () -> "Legacy adapter/capability seams still exist: " + existing
        );
    }

    @Test
    void providerRegistriesUseExplicitRegistrationInsteadOfReflection() throws IOException {
        List<Path> guardedFiles = List.of(
                SOURCE_ROOT.resolve("storage/provider/StorageViewProviderRegistry.java"),
                SOURCE_ROOT.resolve("storage/provider/SupplementalCarriedSourceProviderRegistry.java")
        );

        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (Path file : guardedFiles) {
            if (!Files.exists(file)) {
                continue;
            }

            List<String> matches = Files.readAllLines(file).stream()
                    .map(String::trim)
                    .filter(line -> line.contains("Class.forName(") || line.contains("getDeclaredConstructor("))
                    .toList();
            if (!matches.isEmpty()) {
                violations.put(file.toString(), matches);
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Provider registries regressed to reflective discovery: " + violations
        );
    }

    @Test
    void descriptorDrivenCoreServicesDoNotBranchOnLegacyHostStorageIds() throws IOException {
        List<Path> guardedFiles = List.of(
                SOURCE_ROOT.resolve("session/InventoryHostDescriptor.java"),
                SOURCE_ROOT.resolve("session/InventorySourceDescriptor.java"),
                SOURCE_ROOT.resolve("projection/InventoryHostSnapshotService.java"),
                SOURCE_ROOT.resolve("projection/InventoryHostCapacityService.java")
        );

        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (Path file : guardedFiles) {
            if (!Files.exists(file)) {
                continue;
            }

            List<String> matches = Files.readAllLines(file).stream()
                    .map(String::trim)
                    .filter(line -> line.contains("SOURCE_OPEN_CONTAINER")
                            || line.contains("SOURCE_CARRIED_STORAGE")
                            || line.contains("SOURCE_PLAYER_BACKPACK"))
                    .toList();
            if (!matches.isEmpty()) {
                violations.put(file.toString(), matches);
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Descriptor-driven core services regressed to legacy host-storage/source-id branching: " + violations
        );
    }

    @Test
    void hostAndProjectionCoreDoNotDependOnPrimaryStorageCompatibilitySeams() throws IOException {
        List<Path> guardedFiles = List.of(
                SOURCE_ROOT.resolve("session/InventoryHostDescriptor.java"),
                SOURCE_ROOT.resolve("projection/InventoryHostSnapshotService.java"),
                SOURCE_ROOT.resolve("projection/InventoryHostCapacityService.java"),
                SOURCE_ROOT.resolve("client/session/SlotScreenSessionResolver.java")
        );

        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (Path file : guardedFiles) {
            if (!Files.exists(file)) {
                continue;
            }

            List<String> matches = Files.readAllLines(file).stream()
                    .map(String::trim)
                    .filter(line -> line.contains("primaryStorageSource(")
                            || line.contains("primaryStorageIsCarried("))
                    .toList();
            if (!matches.isEmpty()) {
                violations.put(file.toString(), matches);
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Core host/projection services still depend on primary-storage compatibility seams: " + violations
        );
    }

    @Test
    void newCoreAndWorkflowPackagesDoNotDependOnLegacyUiOrCompatSeams() throws IOException {
        List<Path> guardedRoots = List.of(
                SOURCE_ROOT.resolve("inventory/core"),
                SOURCE_ROOT.resolve("inventory/query"),
                SOURCE_ROOT.resolve("inventory/action"),
                SOURCE_ROOT.resolve("workflow/domain")
        );
        List<String> forbiddenFragments = List.of(
                "import dev.imagio.slot.client.",
                "import dev.imagio.slot.session.",
                "import dev.imagio.slot.storage.provider.",
                "import dev.imagio.slot.storage.adapter.",
                "import dev.imagio.slot.storage.adapter.ExternalTool",
                "import dev.imagio.slot.compat."
        );

        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (Path root : guardedRoots) {
            if (!Files.exists(root)) {
                continue;
            }

            try (Stream<Path> files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".java"))
                        .sorted()
                        .forEach(file -> {
                            try {
                                List<String> matches = Files.readAllLines(file).stream()
                                        .map(String::trim)
                                        .filter(line -> forbiddenFragments.stream().anyMatch(line::contains))
                                        .toList();
                                if (!matches.isEmpty()) {
                                    violations.put(file.toString(), matches);
                                }
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }
                        });
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "New core/workflow packages regressed to legacy UI or compat seams: " + violations
        );
    }

    @Test
    void newCoreAndWorkflowPackagesDoNotBranchOnLegacySourceIds() throws IOException {
        List<Path> guardedRoots = List.of(
                SOURCE_ROOT.resolve("inventory/core"),
                SOURCE_ROOT.resolve("inventory/query"),
                SOURCE_ROOT.resolve("inventory/action"),
                SOURCE_ROOT.resolve("workflow/domain")
        );
        List<String> forbiddenFragments = List.of(
                "ChestLikeMenuLayout.SOURCE_",
                "SOURCE_OPEN_CONTAINER",
                "SOURCE_CARRIED_STORAGE",
                "SOURCE_PLAYER_BACKPACK",
                "SOURCE_PLAYER_MAIN",
                "SOURCE_PLAYER_HOTBAR",
                "SOURCE_PLAYER_ARMOR",
                "SOURCE_PLAYER_OFFHAND"
        );

        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (Path root : guardedRoots) {
            if (!Files.exists(root)) {
                continue;
            }

            try (Stream<Path> files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".java"))
                        .sorted()
                        .forEach(file -> {
                            try {
                                List<String> matches = Files.readAllLines(file).stream()
                                        .map(String::trim)
                                        .filter(line -> forbiddenFragments.stream().anyMatch(line::contains))
                                        .toList();
                                if (!matches.isEmpty()) {
                                    violations.put(file.toString(), matches);
                                }
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }
                        });
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "New core/workflow packages regressed to legacy source-id control flow: " + violations
        );
    }

    @Test
    void commonBootstrapDoesNotRebootstrapLegacyProviderRegistries() throws IOException {
        Path slotCommon = SOURCE_ROOT.resolve("SlotCommon.java");
        List<String> violations = Files.readAllLines(slotCommon).stream()
                .map(String::trim)
                .filter(line -> line.contains("StorageViewProviderRegistry")
                        || line.contains("SupplementalCarriedSourceProviderRegistry"))
                .toList();

        assertTrue(
                violations.isEmpty(),
                () -> "SlotCommon regressed to bootstrapping legacy provider registries: " + violations
        );
    }

    @Test
    void authorityPackagesDoNotDependOnLegacyAuthoritySeams() throws IOException {
        List<Path> guardedRoots = List.of(
                SOURCE_ROOT.resolve("inventory/core"),
                SOURCE_ROOT.resolve("inventory/action"),
                SOURCE_ROOT.resolve("inventory/query"),
                SOURCE_ROOT.resolve("workflow/domain")
        );
        List<String> forbiddenFragments = List.of(
                "ChestLikeMenuLayout",
                "ActionableSourcePolicy",
                "StorageViewProviderSession",
                "StorageViewProviderRegistry",
                "SupplementalCarriedSourceProvider",
                "MenuCapabilityDescriptor",
                "ExternalToolSpec",
                "ExternalToolAction",
                "ExternalToolToggle"
        );

        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (Path root : guardedRoots) {
            if (!Files.exists(root)) {
                continue;
            }

            try (Stream<Path> files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".java"))
                        .sorted()
                        .forEach(file -> {
                            try {
                                List<String> matches = Files.readAllLines(file).stream()
                                        .map(String::trim)
                                        .filter(line -> forbiddenFragments.stream().anyMatch(line::contains))
                                        .toList();
                                if (!matches.isEmpty()) {
                                    violations.put(file.toString(), matches);
                                }
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }
                        });
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Authority packages regressed to legacy host/provider/tool seams: " + violations
        );
    }

    @Test
    void integrationPackagesDoNotDependOnLegacySessionOrProviderSeams() throws IOException {
        List<Path> guardedRoots = List.of(SOURCE_ROOT.resolve("inventory/integration"));
        List<String> forbiddenFragments = List.of(
                "import dev.imagio.slot.session.",
                "import dev.imagio.slot.storage.provider.",
                "import dev.imagio.slot.storage.adapter."
        );

        Map<String, List<String>> violations = new LinkedHashMap<>();
        for (Path root : guardedRoots) {
            if (!Files.exists(root)) {
                continue;
            }

            try (Stream<Path> files = Files.walk(root)) {
                files.filter(path -> path.toString().endsWith(".java"))
                        .sorted()
                        .forEach(file -> {
                            try {
                                List<String> matches = Files.readAllLines(file).stream()
                                        .map(String::trim)
                                        .filter(line -> forbiddenFragments.stream().anyMatch(line::contains))
                                        .toList();
                                if (!matches.isEmpty()) {
                                    violations.put(file.toString(), matches);
                                }
                            } catch (IOException exception) {
                                throw new RuntimeException(exception);
                            }
                        });
            }
        }

        assertTrue(
                violations.isEmpty(),
                () -> "Integration packages regressed to legacy session/provider seams: " + violations
        );
    }

    @Test
    void platformBootstrapDoesNotExposeRawWorkflowRepositoryState() throws IOException {
        Path slotNeoForgeClient = REPO_ROOT.resolve("neoforge/src/main/java/dev/imagio/slot/neoforge/client/SlotNeoForgeClient.java");
        if (!Files.exists(slotNeoForgeClient)) {
            return;
        }

        List<String> violations = Files.readAllLines(slotNeoForgeClient).stream()
                .map(String::trim)
                .filter(line -> line.contains("workflowStateRepository(") || line.contains(".repository()"))
                .toList();

        assertTrue(
                violations.isEmpty(),
                () -> "Platform bootstrap regressed to exposing raw workflow repository state: " + violations
        );
    }
}
