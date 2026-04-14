package dev.imagio.slot.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchWorkflowServiceTest {
    @Test
    void initializePrefersPeerQueryWhenFieldIsEmpty() {
        SearchWorkflowService workflow = new SearchWorkflowService();
        FakeSearchField field = new FakeSearchField("", false);
        FakeSearchPeer peer = new FakeSearchPeer(true, "diamond");

        workflow.initialize(field, true, peer);

        assertEquals("diamond", field.query());
        assertEquals("diamond", peer.query());
        assertEquals("diamond", workflow.currentQuery());
    }

    @Test
    void tickPushesFieldChangeToPeer() {
        SearchWorkflowService workflow = new SearchWorkflowService();
        FakeSearchField field = new FakeSearchField("stone", true);
        FakeSearchPeer peer = new FakeSearchPeer(true, "stone");
        workflow.initialize(field, true, peer);

        field.setQuery("logs");
        workflow.tick(field, true, peer);

        assertEquals("logs", field.query());
        assertEquals("logs", peer.query());
        assertEquals("logs", workflow.currentQuery());
    }

    @Test
    void tickPullsPeerChangeWhenFieldIsNotFocused() {
        SearchWorkflowService workflow = new SearchWorkflowService();
        FakeSearchField field = new FakeSearchField("apple", false);
        FakeSearchPeer peer = new FakeSearchPeer(true, "apple");
        workflow.initialize(field, true, peer);

        peer.setQuery("bread");
        workflow.tick(field, true, peer);

        assertEquals("bread", field.query());
        assertEquals("bread", workflow.currentQuery());
    }

    @Test
    void initializeWithoutSyncRemembersFieldOnly() {
        SearchWorkflowService workflow = new SearchWorkflowService();
        FakeSearchField field = new FakeSearchField("coal", false);
        FakeSearchPeer peer = new FakeSearchPeer(true, "ignored");

        workflow.initialize(field, false, peer);

        assertEquals("coal", workflow.currentQuery());
        assertEquals("ignored", peer.query());
    }

    private static final class FakeSearchField implements SearchWorkflowService.SearchField {
        private String query;
        private final boolean focused;

        private FakeSearchField(String query, boolean focused) {
            this.query = query;
            this.focused = focused;
        }

        @Override
        public String query() {
            return query;
        }

        @Override
        public void setQuery(String query) {
            this.query = query;
        }

        @Override
        public boolean focused() {
            return focused;
        }
    }

    private static final class FakeSearchPeer implements SearchWorkflowService.SearchPeer {
        private final boolean available;
        private String query;

        private FakeSearchPeer(boolean available, String query) {
            this.available = available;
            this.query = query;
        }

        @Override
        public boolean available() {
            return available;
        }

        @Override
        public String query() {
            return query;
        }

        @Override
        public void setQuery(String query) {
            this.query = query;
        }
    }
}
