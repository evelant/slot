package dev.imagio.slot.forge.compat.ae2;

import java.util.ArrayList;
import java.util.List;

final class Ae2GridMachineScan {
    private Ae2GridMachineScan() {
    }

    static List<Class<?>> assignableMachineClasses(
            Iterable<Class<?>> registeredMachineClasses,
            Class<?> requestedType
    ) {
        if (registeredMachineClasses == null || requestedType == null) {
            return List.of();
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (Class<?> machineClass : registeredMachineClasses) {
            if (machineClass != null && requestedType.isAssignableFrom(machineClass)) {
                classes.add(machineClass);
            }
        }
        return classes.isEmpty() ? List.of() : List.copyOf(classes);
    }
}
