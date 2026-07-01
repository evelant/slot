package dev.imagio.slot.neoforge.compat.tfc;

import dev.imagio.slot.SlotDebugLog;
import dev.imagio.slot.compat.tfc.TfcDisplayStorageIds;
import dev.imagio.slot.debug.BoundedDiagnosticThrottle;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageKind;
import dev.imagio.slot.inventory.storage.WorldDisplayStorageSource;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TerraFirmaCraft item displays. TFC tool racks and placed-item blocks own
 * inventories but do not expose NeoForge's block item-handler capability, so
 * SLOT reaches their public inventory through reflection and keeps the rest of
 * the storage pipeline generic.
 */
public final class TfcWorldDisplayStorageDelegate implements WorldStorageAccess.Delegate {
    private static final long DIAGNOSTIC_LOG_INTERVAL_NANOS = 5_000_000_000L;
    private static final int DIAGNOSTIC_LOG_KEY_LIMIT = 2_048;
    private static final BoundedDiagnosticThrottle DIAGNOSTIC_LOG_THROTTLE =
            new BoundedDiagnosticThrottle(DIAGNOSTIC_LOG_INTERVAL_NANOS, DIAGNOSTIC_LOG_KEY_LIMIT);

    private static final ResourceLocation TFC_TOOL_RACKS_ID = ResourceLocation.fromNamespaceAndPath("tfc", "tool_racks");
    private static volatile TagKey<Block> tfcToolRacks;
    private static volatile boolean toolRackTagUnavailable;

    @Override
    public boolean matches(WorldStorageAccess.Target target) {
        return target instanceof WorldStorageAccess.Target.Display display && supports(display.kind());
    }

    @Override
    public Optional<ItemStack> insert(
            MinecraftServer server,
            WorldStorageAccess.Target target,
            ItemStack stack,
            boolean simulate
    ) {
        if (!(target instanceof WorldStorageAccess.Target.Display display) || stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        Optional<ResolvedInventory> inventory = resolveInventory(server, display);
        return inventory.map(value -> {
            ItemStack remaining = insertIntoHandler(value.handler(), stack, simulate);
            if (!simulate && insertedAny(stack, remaining)) {
                markDisplayChanged(value);
            }
            return remaining;
        });
    }

    @Override
    public Optional<ItemStack> extract(
            MinecraftServer server,
            WorldStorageAccess.Target target,
            int slotIndex,
            int amount,
            boolean simulate
    ) {
        if (!(target instanceof WorldStorageAccess.Target.Display display) || amount <= 0) {
            return Optional.empty();
        }
        Optional<ResolvedInventory> inventory = resolveInventory(server, display);
        return inventory.map(value -> {
            ItemStack extracted = extractFromHandler(value.handler(), slotIndex, amount, simulate);
            if (!simulate && extracted != null && !extracted.isEmpty()) {
                markDisplayChanged(value);
            }
            return extracted;
        });
    }

    @Override
    public Optional<List<WorldStorageAccess.SlotContent>> enumerate(
            MinecraftServer server,
            WorldStorageAccess.Target target
    ) {
        if (!(target instanceof WorldStorageAccess.Target.Display display)) {
            return Optional.empty();
        }
        Optional<ResolvedInventory> inventory = resolveInventory(server, display);
        return inventory.map(value -> enumerateHandler(value.handler()));
    }

    @Override
    public Optional<Integer> slotCount(MinecraftServer server, WorldStorageAccess.Target target) {
        if (!(target instanceof WorldStorageAccess.Target.Display display)) {
            return Optional.empty();
        }
        Optional<ResolvedInventory> inventory = resolveInventory(server, display);
        return inventory.map(value -> slotCount(value.handler()));
    }

    @Override
    public List<WorldDisplayStorageSource> proximateDisplaySources(ServerPlayer player, int radiusBlocks) {
        if (player == null || player.getServer() == null) {
            return List.of();
        }
        int radius = Math.max(0, radiusBlocks);
        ServerLevel level = player.serverLevel();
        String dimension = level.dimension().location().toString();
        BlockPos center = player.blockPosition();
        long radiusSquared = (long) radius * radius;
        ArrayList<WorldDisplayStorageSource> sources = new ArrayList<>();
        int candidateBlocks = 0;
        int recognizedBlocks = 0;
        int blocksWithEntity = 0;
        int blocksWithInventory = 0;
        for (BlockPos cursor : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            long dx = (long) cursor.getX() - center.getX();
            long dy = (long) cursor.getY() - center.getY();
            long dz = (long) cursor.getZ() - center.getZ();
            if (dx * dx + dy * dy + dz * dz > radiusSquared) {
                continue;
            }
            BlockState state = level.getBlockState(cursor);
            BlockClassification classification = classify(state);
            if (classification.diagnosticCandidate()) {
                candidateBlocks++;
            }
            WorldDisplayStorageKind kind = classification.kind();
            if (kind == null) {
                if (classification.diagnosticCandidate()) {
                    logCandidate(
                            dimension,
                            cursor,
                            classification,
                            "unrecognized_candidate",
                            "block entity not inspected");
                }
                continue;
            }
            recognizedBlocks++;
            BlockEntity blockEntity = level.getBlockEntity(cursor);
            if (blockEntity != null) {
                blocksWithEntity++;
            }
            InventoryLookup lookup = inventoryLookup(blockEntity);
            Object handler = lookup.handler();
            if (handler == null) {
                logCandidate(
                        dimension,
                        cursor,
                        classification,
                        "inventory_unavailable",
                        "blockEntity={} diagnostic={}",
                        blockEntityClass(blockEntity),
                        lookup.diagnostic());
                continue;
            }
            blocksWithInventory++;
            HandlerContents inspection = inspectHandler(handler);
            int slots = inspection.slots();
            List<WorldStorageAccess.SlotContent> contents = inspection.contents();
            if (slots <= 0 || (contents.isEmpty() && !kind.depositTarget())) {
                logCandidate(
                        dimension,
                        cursor,
                        classification,
                        "contents_skipped",
                        "blockEntity={} handler={} route={} slots={} contents={} diagnostic={}",
                        blockEntityClass(blockEntity),
                        handler.getClass().getName(),
                        lookup.route(),
                        slots,
                        contentsSummary(contents),
                        inspection.diagnostic());
                continue;
            }
            sources.add(new WorldDisplayStorageSource(
                    null,
                    kind,
                    label(kind, cursor),
                    dimension,
                    cursor.getX(),
                    cursor.getY(),
                    cursor.getZ(),
                    slots,
                    contents));
            logCandidate(
                    dimension,
                    cursor,
                    classification,
                    "source_added",
                    "blockEntity={} handler={} route={} slots={} contents={}",
                    blockEntityClass(blockEntity),
                    handler.getClass().getName(),
                    lookup.route(),
                    slots,
                    contentsSummary(contents));
        }
        logScanSummary(
                player,
                dimension,
                center,
                radius,
                candidateBlocks,
                recognizedBlocks,
                blocksWithEntity,
                blocksWithInventory,
                sources.size());
        sources.sort(Comparator
                .comparingLong((WorldDisplayStorageSource source) -> distanceSquared(source, center))
                .thenComparing(WorldDisplayStorageSource::storageId));
        return sources.isEmpty() ? List.of() : List.copyOf(sources);
    }

    private static Optional<ResolvedInventory> resolveInventory(
            MinecraftServer server,
            WorldStorageAccess.Target.Display display
    ) {
        if (server == null || display == null || !supports(display.kind())) {
            return Optional.empty();
        }
        ServerLevel level = level(server, display.dimensionId());
        if (level == null) {
            return Optional.empty();
        }
        BlockPos pos = new BlockPos(display.x(), display.y(), display.z());
        if (!level.isLoaded(pos) || kindFor(level.getBlockState(pos)) != display.kind()) {
            return Optional.empty();
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return inventory(blockEntity).map(handler -> new ResolvedInventory(handler, level, pos, blockEntity));
    }

    private static ServerLevel level(MinecraftServer server, String dimensionId) {
        if (server == null || dimensionId == null || dimensionId.isBlank()) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().location().toString().equals(dimensionId)) {
                return level;
            }
        }
        return null;
    }

    private static Optional<Object> inventory(BlockEntity blockEntity) {
        Object handler = inventoryLookup(blockEntity).handler();
        return handler == null ? Optional.empty() : Optional.of(handler);
    }

    private static InventoryLookup inventoryLookup(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return new InventoryLookup(null, "", "no_block_entity");
        }
        StringBuilder diagnostic = new StringBuilder();
        Object handler = invokeInventoryMethod(blockEntity, "getInventory", diagnostic);
        if (handler != null) {
            return new InventoryLookup(handler, "getInventory", "");
        }
        handler = invokeSidedInventory(blockEntity, diagnostic);
        if (handler != null) {
            return new InventoryLookup(handler, "getSidedInventory(null)", "");
        }
        handler = readInventoryField(blockEntity, diagnostic);
        if (handler != null) {
            return new InventoryLookup(handler, "field:inventory", "");
        }
        String reason = diagnostic.isEmpty() ? "no_inventory_route" : diagnostic.toString();
        return new InventoryLookup(null, "", reason);
    }

    private static Object invokeInventoryMethod(BlockEntity blockEntity, String methodName, StringBuilder diagnostic) {
        try {
            Method method = findMethod(blockEntity.getClass(), methodName);
            if (method == null) {
                appendDiagnostic(diagnostic, methodName + ":missing");
                return null;
            }
            method.setAccessible(true);
            Object result = method.invoke(blockEntity);
            if (result == null) {
                appendDiagnostic(diagnostic, methodName + ":null");
            }
            return result;
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError exception) {
            appendDiagnostic(diagnostic, methodName + ":" + diagnosticName(exception));
            return null;
        }
    }

    private static Object invokeSidedInventory(BlockEntity blockEntity, StringBuilder diagnostic) {
        try {
            Method method = findMethod(blockEntity.getClass(), "getSidedInventory", Direction.class);
            if (method == null) {
                appendDiagnostic(diagnostic, "getSidedInventory:missing");
                return null;
            }
            method.setAccessible(true);
            Object result = method.invoke(blockEntity, (Object) null);
            if (result == null) {
                appendDiagnostic(diagnostic, "getSidedInventory:null");
            }
            return result;
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError exception) {
            appendDiagnostic(diagnostic, "getSidedInventory:" + diagnosticName(exception));
            return null;
        }
    }

    private static Object readInventoryField(BlockEntity blockEntity, StringBuilder diagnostic) {
        try {
            Field field = findField(blockEntity.getClass(), "inventory");
            if (field == null) {
                appendDiagnostic(diagnostic, "field:inventory:missing");
                return null;
            }
            field.setAccessible(true);
            Object result = field.get(blockEntity);
            if (result == null) {
                appendDiagnostic(diagnostic, "field:inventory:null");
            }
            return result;
        } catch (IllegalAccessException | RuntimeException | LinkageError exception) {
            appendDiagnostic(diagnostic, "field:inventory:" + diagnosticName(exception));
            return null;
        }
    }

    private static int slotCount(Object handler) {
        if (handler == null) {
            return 0;
        }
        try {
            Method method = findMethod(handler.getClass(), "getSlots");
            if (method == null) {
                return 0;
            }
            method.setAccessible(true);
            Object result = method.invoke(handler);
            return result instanceof Number number ? Math.max(0, number.intValue()) : 0;
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
            return 0;
        }
    }

    private static List<WorldStorageAccess.SlotContent> enumerateHandler(Object handler) {
        return inspectHandler(handler).contents();
    }

    private static HandlerContents inspectHandler(Object handler) {
        int slots = slotCount(handler);
        if (slots <= 0) {
            return new HandlerContents(slots, List.of(), slotCountDiagnostic(handler));
        }
        Method method = findMethod(handler.getClass(), "getStackInSlot", int.class);
        if (method == null) {
            return new HandlerContents(slots, List.of(), "getStackInSlot:missing");
        }
        method.setAccessible(true);
        ArrayList<WorldStorageAccess.SlotContent> contents = new ArrayList<>(slots);
        int failedSlots = 0;
        String firstFailure = "";
        for (int slot = 0; slot < slots; slot++) {
            try {
                Object result = method.invoke(handler, slot);
                if (result instanceof ItemStack stack && !stack.isEmpty()) {
                    contents.add(new WorldStorageAccess.SlotContent(slot, stack.copy()));
                }
            } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError exception) {
                failedSlots++;
                if (firstFailure.isBlank()) {
                    firstFailure = diagnosticName(exception);
                }
            }
        }
        String diagnostic = failedSlots == 0
                ? ""
                : "getStackInSlot:failed_slots=" + failedSlots + ":first=" + firstFailure;
        return new HandlerContents(slots, contents.isEmpty() ? List.of() : List.copyOf(contents), diagnostic);
    }

    private static ItemStack insertIntoHandler(Object handler, ItemStack stack, boolean simulate) {
        if (handler == null || stack == null || stack.isEmpty()) {
            return stack == null ? ItemStack.EMPTY : stack;
        }
        Method method = findMethod(handler.getClass(), "insertItem", int.class, ItemStack.class, boolean.class);
        if (method == null) {
            return stack;
        }
        method.setAccessible(true);
        ItemStack remaining = stack.copy();
        int slots = slotCount(handler);
        for (int slot = 0; slot < slots && !remaining.isEmpty(); slot++) {
            try {
                Object result = method.invoke(handler, slot, remaining, simulate);
                remaining = result instanceof ItemStack leftover ? leftover : ItemStack.EMPTY;
            } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
                return stack;
            }
        }
        return remaining == null ? ItemStack.EMPTY : remaining;
    }

    private static boolean insertedAny(ItemStack original, ItemStack remaining) {
        if (original == null || original.isEmpty()) {
            return false;
        }
        int remainingCount = remaining == null || remaining.isEmpty() ? 0 : remaining.getCount();
        return remainingCount < original.getCount();
    }

    private static ItemStack extractFromHandler(Object handler, int slotIndex, int amount, boolean simulate) {
        if (handler == null || slotIndex < 0 || amount <= 0 || slotIndex >= slotCount(handler)) {
            return ItemStack.EMPTY;
        }
        Method method = findMethod(handler.getClass(), "extractItem", int.class, int.class, boolean.class);
        if (method == null) {
            return ItemStack.EMPTY;
        }
        method.setAccessible(true);
        try {
            Object result = method.invoke(handler, slotIndex, amount, simulate);
            return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static void markDisplayChanged(ResolvedInventory inventory) {
        if (inventory == null || inventory.level() == null || inventory.pos() == null) {
            return;
        }
        if (inventory.blockEntity() != null) {
            if (invokeMarkForSync(inventory.blockEntity())) {
                return;
            }
            inventory.blockEntity().setChanged();
        }
        BlockState state = inventory.level().getBlockState(inventory.pos());
        inventory.level().sendBlockUpdated(inventory.pos(), state, state, 3);
    }

    private static boolean invokeMarkForSync(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return false;
        }
        try {
            Method method = findMethod(blockEntity.getClass(), "markForSync");
            if (method == null) {
                return false;
            }
            method.setAccessible(true);
            method.invoke(blockEntity);
            return true;
        } catch (IllegalAccessException | InvocationTargetException | RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameters) {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                return cursor.getDeclaredMethod(name, parameters);
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        try {
            return type.getMethod(name, parameters);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                return cursor.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        try {
            return type.getField(name);
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private static WorldDisplayStorageKind kindFor(BlockState state) {
        return classify(state).kind();
    }

    private static BlockClassification classify(BlockState state) {
        if (state == null) {
            return BlockClassification.empty();
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        WorldDisplayStorageKind idKind = id == null
                ? null
                : TfcDisplayStorageIds.kindForBlockId(id.getNamespace(), id.getPath());
        boolean toolRackTag = toolRackTag(state);
        WorldDisplayStorageKind kind = toolRackTag ? WorldDisplayStorageKind.TOOL_RACK : idKind;
        boolean diagnosticCandidate = toolRackTag || idKind != null || suspiciousDisplayId(id);
        return new BlockClassification(id, toolRackTag, idKind, kind, diagnosticCandidate);
    }

    private static boolean toolRackTag(BlockState state) {
        if (state == null || toolRackTagUnavailable) {
            return false;
        }
        TagKey<Block> tag = tfcToolRacksTag();
        if (tag == null) {
            return false;
        }
        try {
            return state.is(tag);
        } catch (RuntimeException | LinkageError exception) {
            markToolRackTagUnavailable(exception);
            return false;
        }
    }

    private static TagKey<Block> tfcToolRacksTag() {
        TagKey<Block> cached = tfcToolRacks;
        if (cached != null || toolRackTagUnavailable) {
            return cached;
        }
        try {
            TagKey<Block> created = TagKey.create(Registries.BLOCK, TFC_TOOL_RACKS_ID);
            tfcToolRacks = created;
            return created;
        } catch (RuntimeException | LinkageError exception) {
            markToolRackTagUnavailable(exception);
            return null;
        }
    }

    private static void markToolRackTagUnavailable(Throwable exception) {
        toolRackTagUnavailable = true;
        logEvery(
                "tool_rack_tag_unavailable",
                "[tfc-display] tool rack tag unavailable; falling back to block-id recognition diagnostic={}",
                diagnosticName(exception));
    }

    private static boolean suspiciousDisplayId(ResourceLocation id) {
        if (id == null) {
            return false;
        }
        String path = id.getPath();
        return "placed_item".equals(path)
                || path.contains("tool_rack")
                || path.contains("toolrack");
    }

    private static boolean supports(WorldDisplayStorageKind kind) {
        return kind == WorldDisplayStorageKind.TOOL_RACK || kind == WorldDisplayStorageKind.PLACED_ITEM;
    }

    private static String label(WorldDisplayStorageKind kind, BlockPos pos) {
        String base = kind == WorldDisplayStorageKind.TOOL_RACK ? "Tool rack" : "Placed item";
        return base + " @ " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static long distanceSquared(WorldDisplayStorageSource source, BlockPos center) {
        long dx = (long) source.x() - center.getX();
        long dy = (long) source.y() - center.getY();
        long dz = (long) source.z() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static void logScanSummary(
            ServerPlayer player,
            String dimension,
            BlockPos center,
            int radius,
            int candidateBlocks,
            int recognizedBlocks,
            int blocksWithEntity,
            int blocksWithInventory,
            int sources
    ) {
        logEvery(
                "scan:" + player.getUUID() + ":" + dimension + ":" + radius,
                "[tfc-display] scan player={} dim={} center={},{},{} radius={} candidates={} recognized={} withEntity={} withInventory={} sources={}",
                player.getScoreboardName(),
                dimension,
                center.getX(),
                center.getY(),
                center.getZ(),
                radius,
                candidateBlocks,
                recognizedBlocks,
                blocksWithEntity,
                blocksWithInventory,
                sources);
    }

    private static void logCandidate(
            String dimension,
            BlockPos pos,
            BlockClassification classification,
            String outcome,
            String detail,
            Object... args
    ) {
        String formattedDetail = format(detail, args);
        logEvery(
                "candidate:" + dimension + ":" + pos.asLong() + ":" + outcome,
                "[tfc-display] candidate outcome={} dim={} pos={},{},{} block={} tagToolRack={} idKind={} kind={} {}",
                outcome,
                dimension,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                blockIdText(classification.id()),
                classification.toolRackTag(),
                classification.idKind(),
                classification.kind(),
                formattedDetail);
    }

    private static void logEvery(String key, String message, Object... args) {
        if (!SlotDebugLog.enabled()) {
            return;
        }
        if (!DIAGNOSTIC_LOG_THROTTLE.shouldEmit(key, System.nanoTime())) {
            return;
        }
        SlotDebugLog.log(message, args);
    }

    private static String format(String pattern, Object... args) {
        if (args == null || args.length == 0) {
            return pattern;
        }
        String value = pattern;
        for (Object arg : args) {
            value = value.replaceFirst("\\{}", java.util.regex.Matcher.quoteReplacement(String.valueOf(arg)));
        }
        return value;
    }

    private static String blockIdText(ResourceLocation id) {
        return id == null ? "<unknown>" : id.toString();
    }

    private static String blockEntityClass(BlockEntity blockEntity) {
        return blockEntity == null ? "<none>" : blockEntity.getClass().getName();
    }

    private static String contentsSummary(List<WorldStorageAccess.SlotContent> contents) {
        if (contents == null || contents.isEmpty()) {
            return "<empty>";
        }
        String summary = contents.stream()
                .limit(4)
                .map(content -> content.slotIndex() + ":"
                        + itemId(content.stack()) + "x" + content.stack().getCount())
                .collect(Collectors.joining(","));
        return contents.size() <= 4 ? summary : summary + ",...";
    }

    private static String itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? stack.getDescriptionId() : id.toString();
    }

    private static String slotCountDiagnostic(Object handler) {
        if (handler == null) {
            return "handler:null";
        }
        Method method = findMethod(handler.getClass(), "getSlots");
        if (method == null) {
            return "getSlots:missing";
        }
        return "getSlots:returned_zero";
    }

    private static void appendDiagnostic(StringBuilder diagnostic, String value) {
        if (!diagnostic.isEmpty()) {
            diagnostic.append("; ");
        }
        diagnostic.append(value);
    }

    private static String diagnosticName(Throwable exception) {
        Throwable value = exception instanceof InvocationTargetException invocation && invocation.getCause() != null
                ? invocation.getCause()
                : exception;
        return value.getClass().getSimpleName();
    }

    private record BlockClassification(
            ResourceLocation id,
            boolean toolRackTag,
            WorldDisplayStorageKind idKind,
            WorldDisplayStorageKind kind,
            boolean diagnosticCandidate
    ) {
        static BlockClassification empty() {
            return new BlockClassification(null, false, null, null, false);
        }
    }

    private record InventoryLookup(Object handler, String route, String diagnostic) {
    }

    private record ResolvedInventory(Object handler, ServerLevel level, BlockPos pos, BlockEntity blockEntity) {
    }

    private record HandlerContents(
            int slots,
            List<WorldStorageAccess.SlotContent> contents,
            String diagnostic
    ) {
    }
}
