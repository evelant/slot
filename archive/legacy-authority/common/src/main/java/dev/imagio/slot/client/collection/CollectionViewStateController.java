package dev.imagio.slot.client.collection;

public interface CollectionViewStateController {
    CollectionViewStateController NOOP = new CollectionViewStateController() {
        @Override
        public boolean isCollectionCollapsed(String collectionId) {
            return false;
        }

        @Override
        public void setCollectionCollapsed(String collectionId, boolean collapsed) {
        }

        @Override
        public boolean pinLoadoutsWhenCollectionCollapsed(String collectionId) {
            return true;
        }

        @Override
        public void setPinLoadoutsWhenCollectionCollapsed(String collectionId, boolean pinned) {
        }
    };

    boolean isCollectionCollapsed(String collectionId);

    void setCollectionCollapsed(String collectionId, boolean collapsed);

    boolean pinLoadoutsWhenCollectionCollapsed(String collectionId);

    void setPinLoadoutsWhenCollectionCollapsed(String collectionId, boolean pinned);

    default boolean toggleCollectionCollapsed(String collectionId) {
        boolean collapsed = !isCollectionCollapsed(collectionId);
        setCollectionCollapsed(collectionId, collapsed);
        return collapsed;
    }

    default boolean togglePinLoadoutsWhenCollectionCollapsed(String collectionId) {
        boolean pinned = !pinLoadoutsWhenCollectionCollapsed(collectionId);
        setPinLoadoutsWhenCollectionCollapsed(collectionId, pinned);
        return pinned;
    }
}
