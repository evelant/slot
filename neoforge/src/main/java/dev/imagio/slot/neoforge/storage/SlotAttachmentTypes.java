package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.SlotCommon;
import net.minecraft.core.UUIDUtil;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.UUID;
import java.util.function.Supplier;

public final class SlotAttachmentTypes {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, SlotCommon.MOD_ID
    );

    public static final Supplier<AttachmentType<UUID>> STORAGE_ID = ATTACHMENT_TYPES.register(
            "storage_id",
            () -> AttachmentType.<UUID>builder(() -> null)
                    .serialize(UUIDUtil.CODEC)
                    .build()
    );

    private SlotAttachmentTypes() {
    }

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
