package net.minecraft.server.level;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ServerPlayer extends Player {
    private final Inventory inventory = new Inventory(this);
    public AbstractContainerMenu containerMenu;

    public Inventory getInventory() {
        return inventory;
    }
}
