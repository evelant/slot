package dev.imagio.slot.ui.action;

import java.util.List;

import static dev.imagio.slot.ui.action.WorkspaceActionArgumentType.DOUBLE;
import static dev.imagio.slot.ui.action.WorkspaceActionArgumentType.INTEGER;
import static dev.imagio.slot.ui.action.WorkspaceActionArgumentType.STRING;

public enum WorkspaceActionId {
    TRANSFER("slot.workspace.transfer", INTEGER, INTEGER, INTEGER, INTEGER, STRING),
    ASSIGN_HOME("slot.workspace.assign_home", STRING, STRING, STRING, STRING, INTEGER),
    CREATE_NAMED_ISLAND("slot.workspace.create_named_island", STRING, STRING, STRING, STRING, INTEGER, INTEGER, INTEGER),
    MOVE_HOTBAR_TO_ATLAS("slot.workspace.move_hotbar_to_atlas", INTEGER, STRING, INTEGER),
    MOVE_ISLAND("slot.workspace.move_island", STRING, DOUBLE, DOUBLE),
    REORDER_ISLAND("slot.workspace.reorder_island", STRING, INTEGER),
    MOVE_CHEST("slot.workspace.move_chest", STRING, INTEGER, INTEGER),
    RELABEL_CHEST("slot.workspace.relabel_chest", STRING, STRING),
    FORGET_CHEST("slot.workspace.forget_chest", STRING),
    CLAIM_CHEST_AT_POS("slot.workspace.claim_chest_at_pos", STRING, INTEGER, INTEGER, INTEGER),
    FORGET_ITEM_AFFINITY("slot.workspace.forget_item_affinity", STRING, STRING, STRING, STRING),
    DEPOSIT("slot.workspace.deposit"),
    GATHER_ACTIVE_KIT("slot.workspace.gather_active_kit"),
    TAKE_ALL_FROM_CHEST("slot.workspace.take_all_from_chest", STRING),
    LOOT_CHEST_TAKE_ALL("slot.workspace.loot_chest_take_all", STRING, INTEGER, INTEGER, INTEGER),
    LOOT_CHEST_TAKE_IDENTITY("slot.workspace.loot_chest_take_identity", STRING, INTEGER, INTEGER, INTEGER, STRING, STRING, STRING),
    LOOT_CHEST_OPEN_VANILLA("slot.workspace.loot_chest_open_vanilla", STRING, INTEGER, INTEGER, INTEGER),
    LOOT_CHEST_CLAIM_AND_DEPOSIT("slot.workspace.loot_chest_claim_and_deposit", STRING, INTEGER, INTEGER, INTEGER, STRING, STRING, STRING),
    SET_SEARCH_QUERY("slot.workspace.set_search_query", STRING),
    RENAME_CLUSTER("slot.workspace.rename_cluster", STRING, STRING),
    RENAME_ISLAND("slot.workspace.rename_island", STRING, STRING),
    RECOLOR_ISLAND("slot.workspace.recolor_island", STRING, INTEGER),
    SET_ISLAND_ICON("slot.workspace.set_island_icon", STRING, STRING, STRING, STRING),
    DELETE_ISLAND("slot.workspace.delete_island", STRING),
    ACCEPT_CHIP("slot.workspace.accept_chip", STRING, STRING, STRING, STRING, STRING),
    SAVE_KIT("slot.workspace.save_kit", STRING),
    ACTIVATE_KIT("slot.workspace.activate_kit", STRING),
    DEACTIVATE_KIT("slot.workspace.deactivate_kit"),
    UNDO("slot.workspace.undo"),
    REDO("slot.workspace.redo"),
    DELETE_KIT("slot.workspace.delete_kit", STRING),
    SWITCH_KIT_PAGE("slot.workspace.switch_kit_page", INTEGER),
    ADD_KIT_PAGE("slot.workspace.add_kit_page", STRING),
    REMOVE_KIT_PAGE("slot.workspace.remove_kit_page", STRING, INTEGER),
    SET_KIT_SCOPED_DESIRED_COUNT("slot.workspace.set_kit_scoped_desired_count", STRING, STRING, STRING, STRING, INTEGER),
    SET_KIT_SLOT_IDENTITY("slot.workspace.set_kit_slot_identity", STRING, INTEGER, INTEGER, STRING, STRING, STRING),
    RENAME_KIT("slot.workspace.rename_kit", STRING, STRING),
    DUPLICATE_KIT("slot.workspace.duplicate_kit", STRING),
    SWAP_KIT_SLOTS("slot.workspace.swap_kit_slots", STRING, INTEGER, INTEGER, INTEGER),
    RETURN_HOTBAR_TO_HOME("slot.workspace.return_hotbar_to_home", INTEGER),
    ASSIGN_HOME_TO_FREE_HOTBAR("slot.workspace.assign_home_to_free_hotbar", STRING, STRING, STRING),
    DEPOSIT_CARRIED_TO_CHEST("slot.workspace.deposit_carried_to_chest", STRING, STRING, STRING, STRING),
    DEPOSIT_HOTBAR_TO_CHEST("slot.workspace.deposit_hotbar_to_chest", INTEGER, STRING),
    TAKE_FROM_CHEST("slot.workspace.take_from_chest", STRING, INTEGER),
    TAKE_ONE_FROM_CHEST("slot.workspace.take_one_from_chest", STRING, INTEGER),
    TAKE_ONE_BY_IDENTITY("slot.workspace.take_one_by_identity", STRING, STRING, STRING),
    TAKE_STACK_BY_IDENTITY("slot.workspace.take_stack_by_identity", STRING, STRING, STRING),
    ASSIGN_HOME_TO_HOTBAR_ONLY("slot.workspace.assign_home_to_hotbar_only", STRING, STRING, STRING),
    ASSIGN_IDENTITY_TO_HOTBAR_SLOT("slot.workspace.assign_identity_to_hotbar_slot", STRING, STRING, STRING, INTEGER),
    DEPOSIT_HOME_TO_LINKED_CHEST("slot.workspace.deposit_home_to_linked_chest", STRING, STRING, STRING),
    DEPOSIT_ONE_HOME_TO_LINKED_CHEST("slot.workspace.deposit_one_home_to_linked_chest", STRING, STRING, STRING),
    SET_PLAYER_DESIRED_COUNT("slot.workspace.set_player_desired_count", STRING, STRING, STRING, INTEGER),
    ADJUST_PLAYER_DESIRED_COUNT("slot.workspace.adjust_player_desired_count", STRING, STRING, STRING, INTEGER),
    CROSS_SURFACE_DROP_ON_HOST_SLOT("slot.workspace.cross_surface_drop_on_host_slot", STRING, STRING, STRING, INTEGER),
    CROSS_SURFACE_QUICK_MOVE_ATLAS("slot.workspace.cross_surface_quick_move_atlas", STRING, STRING, STRING, INTEGER),
    PICKUP_TO_CURSOR("slot.workspace.pickup_to_cursor", STRING, STRING, STRING, INTEGER),
    CURSOR_CANCEL("slot.workspace.cursor_cancel"),
    CURSOR_SMART_DEPOSIT("slot.workspace.cursor_smart_deposit"),
    DROP_CURSOR_INTO_CHEST("slot.workspace.drop_cursor_into_chest", STRING),
    DROP_CURSOR_AT_HOTBAR("slot.workspace.drop_cursor_at_hotbar", INTEGER, INTEGER);

    private final String wireId;
    private final List<WorkspaceActionArgumentType> argumentTypes;

    WorkspaceActionId(String wireId, WorkspaceActionArgumentType... argumentTypes) {
        this.wireId = wireId;
        this.argumentTypes = List.of(argumentTypes);
    }

    public String wireId() {
        return wireId;
    }

    public List<WorkspaceActionArgumentType> argumentTypes() {
        return argumentTypes;
    }

    public WorkspaceActionDefinition definition() {
        return new WorkspaceActionDefinition(this, wireId, argumentTypes);
    }
}
