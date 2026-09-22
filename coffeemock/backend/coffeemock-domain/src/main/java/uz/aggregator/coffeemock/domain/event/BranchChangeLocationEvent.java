package uz.aggregator.coffeemock.domain.event;

import uz.aggregator.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Branch change location EVENT
 *
 * @param coffeeShopId coffee shop identification
 * @param branchId     branch identification
 * @param oldLocation  old location
 * @param location     new location
 * @author Aleksandr Yagudin
 */
public record BranchChangeLocationEvent(Long coffeeShopId, UUID branchId, String oldLocation, String location) implements DomainEvent {
    @Override
    public UUID eventId() {
        return UUID.randomUUID();
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }
}
