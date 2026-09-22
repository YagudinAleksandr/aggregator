package uz.aggregator.coffeemock.domain.event;

import uz.aggregator.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Coffee shop has been registered EVENT
 *
 * @param id   identification
 * @param name name of coffee shop
 * @author Aleksandr Yagudin
 */
public record CoffeeShopRegisteredEvent(Long id, String name) implements DomainEvent {
    @Override
    public UUID eventId() {
        return UUID.randomUUID();
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }
}
