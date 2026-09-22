package uz.aggregator.shared.event;

/**
 * Integration event
 *
 * @author Aleksandr Yagudin
 */
public interface IntegrationEvent extends DomainEvent {
    /**
     * Aggregate identification
     *
     * @return String key
     */
    String aggregateId();
}
