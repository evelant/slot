package dev.imagio.slot.workflow.domain;

import java.util.Objects;

public final class WorkflowDomainPersistenceService {
    private final WorkflowDomainPersistencePort persistencePort;

    public WorkflowDomainPersistenceService(WorkflowDomainPersistencePort persistencePort) {
        this.persistencePort = Objects.requireNonNull(persistencePort, "persistencePort");
    }

    public WorkflowDomainSnapshot loadInto(WorkflowDomainStateRepository repository) {
        Objects.requireNonNull(repository, "repository");
        WorkflowDomainSnapshot snapshot = persistencePort.load();
        repository.replaceWith(snapshot);
        return snapshot;
    }

    public void saveFrom(WorkflowDomainStateRepository repository) {
        Objects.requireNonNull(repository, "repository");
        persistencePort.save(repository.snapshot());
    }
}
