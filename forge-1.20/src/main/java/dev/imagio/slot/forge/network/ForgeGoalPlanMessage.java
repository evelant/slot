package dev.imagio.slot.forge.network;

import dev.imagio.slot.inventory.goal.GoalPlanState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record ForgeGoalPlanMessage(String action, String goalId, GoalPlanState goal) {
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_REMOVE = "remove";

    public ForgeGoalPlanMessage {
        action = action == null || action.isBlank() ? ACTION_SAVE : action.trim();
        goalId = goalId == null ? "" : goalId.trim();
    }

    public static ForgeGoalPlanMessage save(GoalPlanState goal) {
        return new ForgeGoalPlanMessage(ACTION_SAVE, goal == null ? "" : goal.goalId(), goal);
    }

    public static ForgeGoalPlanMessage remove(String goalId) {
        return new ForgeGoalPlanMessage(ACTION_REMOVE, goalId, null);
    }

    public static void encode(ForgeGoalPlanMessage message, FriendlyByteBuf buffer) {
        ForgeGoalPlanMessage resolved = message == null ? remove("") : message;
        buffer.writeUtf(resolved.action());
        buffer.writeUtf(resolved.goalId());
        buffer.writeNbt(Forge120WorkspaceViewModelCodec.encodeGoalPlan(resolved.goal()));
    }

    public static ForgeGoalPlanMessage decode(FriendlyByteBuf buffer) {
        String action = buffer.readUtf();
        String goalId = buffer.readUtf();
        CompoundTag tag = buffer.readNbt();
        GoalPlanState goal = Forge120WorkspaceViewModelCodec.decodeGoalPlan(tag);
        return new ForgeGoalPlanMessage(action, goalId, goal);
    }
}
