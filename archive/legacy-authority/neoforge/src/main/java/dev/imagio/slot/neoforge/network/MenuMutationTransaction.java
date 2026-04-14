package dev.imagio.slot.neoforge.network;

import net.minecraft.world.inventory.AbstractContainerMenu;

final class MenuMutationTransaction implements AutoCloseable {
    private final AbstractContainerMenu menu;
    private final MenuMutationSnapshot snapshot;
    private boolean committed;

    private MenuMutationTransaction(AbstractContainerMenu menu, MenuMutationSnapshot snapshot) {
        this.menu = menu;
        this.snapshot = snapshot;
    }

    static MenuMutationTransaction capture(AbstractContainerMenu menu) {
        return new MenuMutationTransaction(menu, MenuMutationSnapshot.capture(menu));
    }

    void commit() {
        committed = true;
    }

    void rollback() {
        if (!committed && snapshot != null) {
            snapshot.restore(menu);
        }
    }

    @Override
    public void close() {
        rollback();
    }
}
