package dev.imagio.slot.classification;

public final class FacetIndexHolder {

    private static volatile FacetIndex instance;
    private static volatile FacetIndexLoadReport status;

    private FacetIndexHolder() {
    }

    public static FacetIndex get() {
        FacetIndex local = instance;
        if (local == null) {
            synchronized (FacetIndexHolder.class) {
                if (instance == null) {
                    FacetIndexBootstrap.LoadResult result = FacetIndexBootstrap.loadAllWithReport();
                    instance = result.index();
                    status = result.report();
                }
                local = instance;
            }
        }
        return local;
    }

    public static void install(FacetIndex index) {
        install(index, FacetIndexLoadReport.unknown(index));
    }

    public static void install(FacetIndex index, FacetIndexLoadReport report) {
        synchronized (FacetIndexHolder.class) {
            instance = index;
            status = report == null ? FacetIndexLoadReport.unknown(index) : report;
        }
    }

    public static FacetIndexLoadReport status() {
        FacetIndexLoadReport local = status;
        if (local == null) {
            get();
            local = status;
        }
        return local == null ? FacetIndexLoadReport.unknown(instance) : local;
    }

    public static void reset() {
        synchronized (FacetIndexHolder.class) {
            instance = null;
            status = null;
        }
    }
}
