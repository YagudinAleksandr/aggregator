package uz.aggregator.shared.aggregate;

import uz.aggregator.shared.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base aggregate root
 *
 * @param <T> type of ID
 * @author Aleksandr Yagudin
 */
public abstract class AggregateRoot<T> {
    /**
     * List of domain events
     */
    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Identification
     */
    public abstract T id();

    protected void registerEvent(DomainEvent event) {
        domainEvents.add(Objects.requireNonNull(event, "event"));
    }

    /**
     * Get domain event like copy.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> pulled = List.copyOf(domainEvents);
        domainEvents.clear();
        return pulled;
    }

    public List<DomainEvent> peekDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return Objects.equals(id(), ((AggregateRoot<?>) other).id());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id());
    }
}
