package dev.imagio.slot.neoforge.network;

import dev.imagio.slot.SlotCommon;
import dev.imagio.slot.inventory.goal.GoalPlanState;
import dev.imagio.slot.neoforge.screen.ldlib.SlotWorkspaceViewModelCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SlotGoalPlanPayload(String action, String goalId, GoalPlanState goal) implements CustomPacketPayload {
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_REMOVE = "remove";
    public static final Type<SlotGoalPlanPayload> TYPE = new Type<>(SlotCommon.id("goal_plan"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SlotGoalPlanPayload> STREAM_CODEC =
            StreamCodec.of(SlotGoalPlanPayload::encode, SlotGoalPlanPayload::decode);

    public SlotGoalPlanPayload {
        action = action == null || action.isBlank() ? ACTION_SAVE : action.trim();
        goalId = goalId == null ? "" : goalId.trim();
    }

    public static SlotGoalPlanPayload save(GoalPlanState goal) {
        return new SlotGoalPlanPayload(ACTION_SAVE, goal == null ? "" : goal.goalId(), goal);
    }

    public static SlotGoalPlanPayload remove(String goalId) {
        return new SlotGoalPlanPayload(ACTION_REMOVE, goalId, null);
    }

    private static void encode(RegistryFriendlyByteBuf buffer, SlotGoalPlanPayload payload) {
        SlotGoalPlanPayload resolved = payload == null ? remove("") : payload;
        buffer.writeUtf(resolved.action());
        buffer.writeUtf(resolved.goalId());
        buffer.writeNbt(SlotWorkspaceViewModelCodec.encodeGoalPlan(resolved.goal()));
    }

    private static SlotGoalPlanPayload decode(RegistryFriendlyByteBuf buffer) {
        String action = buffer.readUtf();
        String goalId = buffer.readUtf();
        CompoundTag tag = buffer.readNbt();
        GoalPlanState goal = SlotWorkspaceViewModelCodec.decodeGoalPlan(tag);
        return new SlotGoalPlanPayload(action, goalId, goal);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
