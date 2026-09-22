package uz.aggregator.shared.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Basic domain event
 *
 * @author Aleksandr Yagudin
 */
public interface DomainEvent {
    /**
     * Event identification
     *
     * @return identification on {@link UUID}
     */
    UUID eventId();

    /**
     * Date and time when event start
     *
     * @return {@link Instant}
     */
    Instant occurredAt();

    /**
     * Event type
     *
     * @return class name
     */
    default String eventType() {
        return getClass().getSimpleName();
    }
}
