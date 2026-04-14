package dev.imagio.slot.workflow.domain;

public interface WorkflowDomainPersistencePort {
    WorkflowDomainSnapshot load();

    void save(WorkflowDomainSnapshot snapshot);
}
