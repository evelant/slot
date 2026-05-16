package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
import dev.imagio.slot.neoforge.workflow.SlotPlayerWorkflowRuntimeService;
import dev.imagio.slot.workflow.domain.DomainEventMetadata;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class NeoForgeWorldItemUseObserver {
    private static boolean registered;

    private NeoForgeWorldItemUseObserver() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, NeoForgeWorldItemUseObserver::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, NeoForgeWorldItemUseObserver::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, NeoForgeWorldItemUseObserver::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, NeoForgeWorldItemUseObserver::onEntityInteractSpecific);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, NeoForgeWorldItemUseObserver::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, NeoForgeWorldItemUseObserver::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, NeoForgeWorldItemUseObserver::onBlockPlace);
        NeoForge.EVENT_BUS.addListener(NeoForgeWorldItemUseObserver::onItemUseFinish);
        NeoForge.EVENT_BUS.addListener(NeoForgeWorldItemUseObserver::onPlayerDestroyItem);
        registered = true;
    }

    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        recordUse(event.getEntity(), event.getItemStack(), event.getLevel(), event.getHand(),
                "right_click_block", blockKey(event.getLevel(), event.getPos()));
    }

    private static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        recordUse(event.getEntity(), event.getItemStack(), event.getLevel(), event.getHand(),
                "right_click_item", "air");
    }

    private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        recordUse(event.getEntity(), event.getItemStack(), event.getLevel(), event.getHand(),
                "right_click_entity", entityKey(event.getTarget()));
    }

    private static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        recordUse(event.getEntity(), event.getItemStack(), event.getLevel(), event.getHand(),
                "right_click_entity", entityKey(event.getTarget()));
    }

    private static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        recordUse(player, player.getMainHandItem(), player.serverLevel(), InteractionHand.MAIN_HAND,
                "attack_entity", entityKey(event.getTarget()));
    }

    private static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        recordUse(player, player.getMainHandItem(), player.serverLevel(), InteractionHand.MAIN_HAND,
                "break_block", blockKey(event.getState()));
    }

    private static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockState placed = event.getPlacedBlock();
        ItemStack stack = placed == null ? ItemStack.EMPTY : new ItemStack(placed.getBlock().asItem());
        if (stack.isEmpty()) {
            return;
        }
        SlotPlayerWorkflowRuntimeService.runtime(player).contextualSuggestions().observeItemPlaced(
                identity(stack),
                1,
                player.serverLevel().getGameTime(),
                blockKey(placed),
                "",
                DomainEventMetadata.origin("contextual.neoforge.item_placed"));
    }

    private static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = event.getItem();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        SlotPlayerWorkflowRuntimeService.runtime(player).contextualSuggestions().observeItemConsumed(
                identity(stack),
                stack.getCount(),
                player.serverLevel().getGameTime(),
                "",
                DomainEventMetadata.origin("contextual.neoforge.item_consumed"));
    }

    private static void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = event.getOriginal();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        SlotPlayerWorkflowRuntimeService.runtime(player).contextualSuggestions().observeItemDamaged(
                identity(stack),
                player.serverLevel().getGameTime(),
                "item_destroyed",
                sourceKey(event.getHand()),
                DomainEventMetadata.origin("contextual.neoforge.item_damaged"));
    }

    private static void recordUse(
            Player playerEntity,
            ItemStack stack,
            Level level,
            InteractionHand hand,
            String action,
            String targetKey
    ) {
        if (!(playerEntity instanceof ServerPlayer player) || stack == null || stack.isEmpty()) {
            return;
        }
        long tick = level instanceof ServerLevel serverLevel ? serverLevel.getGameTime() : player.serverLevel().getGameTime();
        SlotPlayerWorkflowRuntimeService.runtime(player).contextualSuggestions().observeItemUse(
                identity(stack),
                tick,
                action,
                targetKey,
                sourceKey(hand),
                DomainEventMetadata.origin("contextual.neoforge.item_used"));
    }

    private static ItemIdentity identity(ItemStack stack) {
        return ItemIdentityMatcher.normalizeMovable(ItemIdentityMatcher.create(stack));
    }

    private static String sourceKey(InteractionHand hand) {
        return hand == null ? "" : "hand:" + hand.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static String blockKey(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return "";
        }
        return blockKey(level.getBlockState(pos));
    }

    private static String blockKey(BlockState state) {
        if (state == null) {
            return "";
        }
        Block block = state.getBlock();
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return key == null ? "" : "block:" + key;
    }

    private static String entityKey(Entity entity) {
        if (entity == null) {
            return "";
        }
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key == null ? "" : "entity:" + key;
    }
}
