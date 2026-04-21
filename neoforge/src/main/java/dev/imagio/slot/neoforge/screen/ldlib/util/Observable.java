package dev.imagio.slot.neoforge.screen.ldlib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class Observable<T> {
    @FunctionalInterface
    public interface Subscription {
        void unsubscribe();
    }

    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public Observable() {
        this.value = null;
    }

    public Observable(T initialValue) {
        this.value = initialValue;
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        if (Objects.equals(value, newValue)) {
            return;
        }
        value = newValue;
        for (Consumer<T> listener : List.copyOf(listeners)) {
            listener.accept(newValue);
        }
    }

    public Subscription subscribe(Consumer<T> listener) {
        listeners.add(listener);
        listener.accept(value);
        return () -> listeners.remove(listener);
    }

    public Subscription subscribeLater(Consumer<T> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }
}
