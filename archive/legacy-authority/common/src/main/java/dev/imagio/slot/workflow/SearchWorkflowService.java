package dev.imagio.slot.workflow;

public final class SearchWorkflowService {
    public interface SearchField {
        String query();

        void setQuery(String query);

        boolean focused();
    }

    public interface SearchPeer {
        SearchPeer NONE = new SearchPeer() {
            @Override
            public boolean available() {
                return false;
            }

            @Override
            public String query() {
                return "";
            }

            @Override
            public void setQuery(String query) {
            }
        };

        boolean available();

        String query();

        void setQuery(String query);
    }

    private String query = "";
    private String lastFieldQuery = "";
    private String lastPeerQuery = "";

    public String currentQuery() {
        return query;
    }

    public void remember(String value) {
        query = normalize(value);
    }

    public void clear() {
        remember("");
    }

    public void initialize(SearchField field, boolean syncEnabled, SearchPeer peer) {
        if (field == null) {
            resetTracking();
            return;
        }

        SearchPeer resolvedPeer = peer == null ? SearchPeer.NONE : peer;
        if (!syncEnabled || !resolvedPeer.available()) {
            remember(field.query());
            lastFieldQuery = field.query();
            lastPeerQuery = "";
            return;
        }

        String fieldQuery = normalize(field.query());
        String peerQuery = normalize(resolvedPeer.query());
        String mergedQuery = !peerQuery.isBlank() || fieldQuery.isBlank() ? peerQuery : fieldQuery;

        if (!mergedQuery.equals(fieldQuery)) {
            field.setQuery(mergedQuery);
            fieldQuery = mergedQuery;
        }
        if (!mergedQuery.equals(peerQuery)) {
            resolvedPeer.setQuery(mergedQuery);
            peerQuery = mergedQuery;
        }

        remember(mergedQuery);
        lastFieldQuery = fieldQuery;
        lastPeerQuery = peerQuery;
    }

    public void tick(SearchField field, boolean syncEnabled, SearchPeer peer) {
        if (field == null) {
            resetTracking();
            return;
        }

        SearchPeer resolvedPeer = peer == null ? SearchPeer.NONE : peer;
        if (!syncEnabled || !resolvedPeer.available()) {
            remember(field.query());
            lastFieldQuery = field.query();
            lastPeerQuery = "";
            return;
        }

        String fieldQuery = normalize(field.query());
        String peerQuery = normalize(resolvedPeer.query());
        boolean fieldChanged = !fieldQuery.equals(lastFieldQuery);
        boolean peerChanged = !peerQuery.equals(lastPeerQuery);

        if (!fieldChanged && !peerChanged && !fieldQuery.equals(peerQuery)) {
            if (fieldQuery.isBlank() && !peerQuery.isBlank()) {
                field.setQuery(peerQuery);
                fieldQuery = peerQuery;
            } else if (peerQuery.isBlank() && !fieldQuery.isBlank()) {
                resolvedPeer.setQuery(fieldQuery);
                peerQuery = fieldQuery;
            } else if (field.focused()) {
                resolvedPeer.setQuery(fieldQuery);
                peerQuery = fieldQuery;
            } else {
                field.setQuery(peerQuery);
                fieldQuery = peerQuery;
            }
        } else if (fieldChanged && !peerChanged) {
            resolvedPeer.setQuery(fieldQuery);
            peerQuery = fieldQuery;
        } else if (peerChanged && !fieldChanged) {
            if (!peerQuery.equals(fieldQuery)) {
                field.setQuery(peerQuery);
            }
            fieldQuery = peerQuery;
        } else if (fieldChanged && peerChanged && !fieldQuery.equals(peerQuery)) {
            if (field.focused()) {
                resolvedPeer.setQuery(fieldQuery);
                peerQuery = fieldQuery;
            } else {
                field.setQuery(peerQuery);
                fieldQuery = peerQuery;
            }
        }

        remember(fieldQuery);
        lastFieldQuery = fieldQuery;
        lastPeerQuery = peerQuery;
    }

    private void resetTracking() {
        lastFieldQuery = "";
        lastPeerQuery = "";
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
