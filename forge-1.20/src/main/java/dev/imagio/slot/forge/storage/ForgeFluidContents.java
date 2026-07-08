package dev.imagio.slot.forge.storage;

import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.FluidStackAccess;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

final class ForgeFluidContents {
    private ForgeFluidContents() {
    }

    static List<WorldStorageAccess.FluidContent> directTankContents(IFluidHandler handler) {
        if (handler == null) {
            return List.of();
        }
        ArrayList<WorldStorageAccess.FluidContent> contents = new ArrayList<>();
        for (int tank = 0; tank < Math.max(0, handler.getTanks()); tank++) {
            FluidStackAccess access = stackAccess(safeFluidInTank(handler, tank));
            if (access.present()) {
                contents.add(new WorldStorageAccess.FluidContent(
                        tank,
                        WorldStorageAccess.FluidContent.DIRECT_TANK_SLOT,
                        access.identity(),
                        access.amount(),
                        access.label()));
            }
        }
        return contents.isEmpty() ? List.of() : List.copyOf(contents);
    }

    static List<WorldStorageAccess.FluidContent> itemContainerContents(int slotIndex, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }
        IFluidHandler handler;
        try {
            LazyOptional<? extends IFluidHandler> optional = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
            handler = optional.orElse(null);
        } catch (RuntimeException | LinkageError ignored) {
            return List.of();
        }
        if (handler == null) {
            return List.of();
        }
        ArrayList<WorldStorageAccess.FluidContent> contents = new ArrayList<>();
        for (int tank = 0; tank < Math.max(0, handler.getTanks()); tank++) {
            FluidStackAccess access = stackAccess(safeFluidInTank(handler, tank));
            if (access.present()) {
                contents.add(new WorldStorageAccess.FluidContent(
                        tank,
                        Math.max(0, slotIndex),
                        access.identity(),
                        access.amount(),
                        access.label()));
            }
        }
        return contents.isEmpty() ? List.of() : List.copyOf(contents);
    }

    static List<CarriedSourceAccess.CarriedFluidContent> carriedItemContainerContents(
            String sourceId,
            int slotIndex,
            ItemStack stack
    ) {
        if (stack == null || stack.isEmpty()) {
            return List.of();
        }
        IFluidHandler handler;
        try {
            LazyOptional<? extends IFluidHandler> optional = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
            handler = optional.orElse(null);
        } catch (RuntimeException | LinkageError ignored) {
            return List.of();
        }
        if (handler == null) {
            return List.of();
        }
        ArrayList<CarriedSourceAccess.CarriedFluidContent> contents = new ArrayList<>();
        for (int tank = 0; tank < Math.max(0, handler.getTanks()); tank++) {
            FluidStackAccess access = stackAccess(safeFluidInTank(handler, tank));
            if (access.present()) {
                contents.add(new CarriedSourceAccess.CarriedFluidContent(
                        sourceId,
                        slotIndex,
                        tank,
                        access.identity(),
                        access.amount(),
                        access.label()));
            }
        }
        return contents.isEmpty() ? List.of() : List.copyOf(contents);
    }

    private static FluidStack safeFluidInTank(IFluidHandler handler, int tank) {
        try {
            FluidStack stack = handler.getFluidInTank(tank);
            return stack == null ? FluidStack.EMPTY : stack.copy();
        } catch (RuntimeException | LinkageError ignored) {
            return FluidStack.EMPTY;
        }
    }

    private static FluidStackAccess stackAccess(FluidStack stack) {
        if (stack == null || stack.isEmpty() || stack.getAmount() <= 0) {
            return EmptyFluidAccess.INSTANCE;
        }
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        if (id == null) {
            return EmptyFluidAccess.INSTANCE;
        }
        CompoundTag tag = stack.getTag();
        String fingerprint = tag == null || tag.isEmpty() ? "" : tag.toString();
        SlotResourceIdentity identity = SlotResourceIdentity.fluid(id.toString(), fingerprint);
        String label = stack.getDisplayName() == null ? id.toString() : stack.getDisplayName().getString();
        return new ObservedFluidAccess(identity, stack.getAmount(), label);
    }

    private enum EmptyFluidAccess implements FluidStackAccess {
        INSTANCE;

        @Override
        public SlotResourceIdentity identity() {
            return null;
        }

        @Override
        public long amount() {
            return 0L;
        }

        @Override
        public String label() {
            return "";
        }
    }

    private record ObservedFluidAccess(
            SlotResourceIdentity identity,
            long amount,
            String label
    ) implements FluidStackAccess {
        private ObservedFluidAccess {
            amount = Math.max(0L, amount);
            label = label == null || label.isBlank()
                    ? identity == null ? "Fluid" : identity.id()
                    : label.trim();
        }
    }
}
