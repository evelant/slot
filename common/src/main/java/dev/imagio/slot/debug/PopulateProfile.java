package dev.imagio.slot.debug;

public enum PopulateProfile {
    STARTER("starter", 30, 20, 0.40, 1),
    ORGANIZED("organized", 160, 50, 0.10, 6),
    LATE_MODPACK("late-modpack", 800, 100, 0.07, 16);

    private final String id;
    private final int identityCount;
    private final int carriedIdentityCap;
    private final double triageFraction;
    private final int chestCount;

    PopulateProfile(
            String id,
            int identityCount,
            int carriedIdentityCap,
            double triageFraction,
            int chestCount
    ) {
        this.id = id;
        this.identityCount = identityCount;
        this.carriedIdentityCap = carriedIdentityCap;
        this.triageFraction = triageFraction;
        this.chestCount = chestCount;
    }

    public String id() {
        return id;
    }

    public int identityCount() {
        return identityCount;
    }

    public int carriedIdentityCap() {
        return carriedIdentityCap;
    }

    public double triageFraction() {
        return triageFraction;
    }

    public int chestCount() {
        return chestCount;
    }

    public static PopulateProfile fromId(String id) {
        if (id == null) {
            return null;
        }
        String trimmed = id.trim().toLowerCase(java.util.Locale.ROOT);
        for (PopulateProfile profile : values()) {
            if (profile.id.equals(trimmed)) {
                return profile;
            }
        }
        return null;
    }
}
