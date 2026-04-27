package dev.imagio.slot.classification;

public final class FacetIndexHolder {

    private static volatile FacetIndex instance;

    private FacetIndexHolder() {
    }

    public static FacetIndex get() {
        FacetIndex local = instance;
        if (local == null) {
            synchronized (FacetIndexHolder.class) {
                if (instance == null) {
                    instance = FacetIndexBootstrap.loadAll();
                }
                local = instance;
            }
        }
        return local;
    }

    public static void install(FacetIndex index) {
        synchronized (FacetIndexHolder.class) {
            instance = index;
        }
    }

    public static void reset() {
        synchronized (FacetIndexHolder.class) {
            instance = null;
        }
    }
}
