package uz.aggregator.coffeemock.domain.event;

import uz.aggregator.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Coffee shop change name EVENT
 *
 * @param id      identification
 * @param name    name
 * @param oldName old name
 * @author Aleksandr Yagudin
 */
public record CoffeeShopChangeNameEvent(Long id, String oldName, String name) implements DomainEvent {
    @Override
    public UUID eventId() {
        return UUID.randomUUID();
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }
}
