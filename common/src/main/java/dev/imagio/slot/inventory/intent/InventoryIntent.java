package dev.imagio.slot.inventory.intent;

public sealed interface InventoryIntent permits InventoryBrowseIntent, InventoryWorkflowIntent, InventoryMutationIntent {
    InventoryIntentKind kind();

    String origin();
}
