package uz.aggregator.coffeemock.domain.event;

import uz.aggregator.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Branch has been activated EVENT
 *
 * @param coffeeShopId coffee shop identification
 * @param branchId     branch identification
 * @author Aleksandr Yagudin
 */
public record BranchActivatedEvent(Long coffeeShopId, UUID branchId) implements DomainEvent {
    @Override
    public UUID eventId() {
        return UUID.randomUUID();
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }
}
