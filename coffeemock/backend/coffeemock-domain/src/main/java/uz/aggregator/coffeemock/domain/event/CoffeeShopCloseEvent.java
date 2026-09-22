package uz.aggregator.coffeemock.domain.event;

import uz.aggregator.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Coffee shop was blocked
 *
 * @param id     identification
 * @param reason reason of block
 * @author Aleksandr Yagudin
 */
public record CoffeeShopCloseEvent(Long id, String reason) implements DomainEvent {
    @Override
    public UUID eventId() {
        return UUID.randomUUID();
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }
}
