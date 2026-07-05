package dev.imagio.slot.inventory.workspace;

public record WorkspaceCardProjectionStats(
        int reusedCards,
        int rebuiltCards,
        int removedCards
) {
    public WorkspaceCardProjectionStats {
        reusedCards = Math.max(0, reusedCards);
        rebuiltCards = Math.max(0, rebuiltCards);
        removedCards = Math.max(0, removedCards);
    }

    public static WorkspaceCardProjectionStats empty() {
        return new WorkspaceCardProjectionStats(0, 0, 0);
    }
}
