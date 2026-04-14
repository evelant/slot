package net.minecraft.core;

import java.util.ArrayList;

public class NonNullList<E> extends ArrayList<E> {
    public static <E> NonNullList<E> create() {
        return new NonNullList<>();
    }
}
