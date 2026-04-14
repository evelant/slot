package dev.imagio.slot.workflow;

public interface SettingsService {
    boolean slotEnabled();

    void setSlotEnabled(boolean enabled);

    boolean replacePlayerInventory();

    void setReplacePlayerInventory(boolean enabled);

    boolean replaceChestLikeStorage();

    void setReplaceChestLikeStorage(boolean enabled);

    boolean syncSearchWithEmi();

    void setSyncSearchWithEmi(boolean enabled);
}
