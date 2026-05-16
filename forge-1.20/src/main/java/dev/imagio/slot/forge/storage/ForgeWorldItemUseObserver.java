package dev.imagio.slot.forge.storage;

import dev.imagio.slot.forge.SlotForge;
import dev.imagio.slot.forge.workflow.ForgePlayerWorkflowRuntimeService;
import dev.imagio.slot.inventory.core.ItemIdentity;
import dev.imagio.slot.inventory.core.ItemIdentityMatcher;
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
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SlotForge.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeWorldItemUseObserver {
    private ForgeWorldItemUseObserver() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        recordUse(event.getEntity(), event.getItemStack(), event.getLevel(), event.getHand(),
                "right_click_block", blockKey(event.getLevel(), event.getPos()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        recordUse(event.getEntity(), event.getItemStack(), event.getLevel(), event.getHand(),
                "right_click_item", "air");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        recordUse(event.getEntity(), event.getItemStack(), event.getLevel(), event.getHand(),
                "right_click_entity", entityKey(event.getTarget()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        recordUse(event.getEntity(), event.getItemStack(), event.getLevel(), event.getHand(),
                "right_click_entity", entityKey(event.getTarget()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        recordUse(player, player.getMainHandItem(), player.serverLevel(), InteractionHand.MAIN_HAND,
                "attack_entity", entityKey(event.getTarget()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        recordUse(player, player.getMainHandItem(), player.serverLevel(), InteractionHand.MAIN_HAND,
                "break_block", blockKey(event.getState()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockState placed = event.getPlacedBlock();
        ItemStack stack = placed == null ? ItemStack.EMPTY : new ItemStack(placed.getBlock().asItem());
        if (stack.isEmpty()) {
            return;
        }
        ForgePlayerWorkflowRuntimeService.runtime(player).contextualSuggestions().observeItemPlaced(
                identity(stack),
                1,
                player.serverLevel().getGameTime(),
                blockKey(placed),
                "",
                DomainEventMetadata.origin("contextual.forge.item_placed"));
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = event.getItem();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ForgePlayerWorkflowRuntimeService.runtime(player).contextualSuggestions().observeItemConsumed(
                identity(stack),
                stack.getCount(),
                player.serverLevel().getGameTime(),
                "",
                DomainEventMetadata.origin("contextual.forge.item_consumed"));
    }

    @SubscribeEvent
    public static void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = event.getOriginal();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ForgePlayerWorkflowRuntimeService.runtime(player).contextualSuggestions().observeItemDamaged(
                identity(stack),
                player.serverLevel().getGameTime(),
                "item_destroyed",
                sourceKey(event.getHand()),
                DomainEventMetadata.origin("contextual.forge.item_damaged"));
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
        ForgePlayerWorkflowRuntimeService.runtime(player).contextualSuggestions().observeItemUse(
                identity(stack),
                tick,
                action,
                targetKey,
                sourceKey(hand),
                DomainEventMetadata.origin("contextual.forge.item_used"));
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
