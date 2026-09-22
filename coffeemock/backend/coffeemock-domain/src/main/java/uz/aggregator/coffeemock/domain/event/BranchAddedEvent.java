package uz.aggregator.coffeemock.domain.event;

import uz.aggregator.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Branch has been added to coffee shop EVENT
 *
 * @param coffeeShopId coffee shop identification
 * @param branchId     branch identification
 * @param name         name of branch
 * @param location     location
 * @author Aleksandr Yagudin
 */
public record BranchAddedEvent(Long coffeeShopId, UUID branchId, String name, String location) implements DomainEvent {
    @Override
    public UUID eventId() {
        return UUID.randomUUID();
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }
}
