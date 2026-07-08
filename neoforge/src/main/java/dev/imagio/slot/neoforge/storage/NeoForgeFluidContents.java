package dev.imagio.slot.neoforge.storage;

import dev.imagio.slot.inventory.core.SlotResourceIdentity;
import dev.imagio.slot.inventory.storage.CarriedSourceAccess;
import dev.imagio.slot.inventory.storage.FluidStackAccess;
import dev.imagio.slot.inventory.storage.WorldStorageAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

final class NeoForgeFluidContents {
    private NeoForgeFluidContents() {
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
            handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
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
            handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
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
        DataComponentPatch patch = stack.getComponentsPatch();
        String fingerprint = patch == null || stack.isComponentsPatchEmpty() ? "" : patch.toString();
        SlotResourceIdentity identity = SlotResourceIdentity.fluid(id.toString(), fingerprint);
        String label = stack.getHoverName() == null ? id.toString() : stack.getHoverName().getString();
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
