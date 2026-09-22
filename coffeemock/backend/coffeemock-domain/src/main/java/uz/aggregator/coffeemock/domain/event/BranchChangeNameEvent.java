package uz.aggregator.coffeemock.domain.event;

import uz.aggregator.shared.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Branch change name EVENT
 *
 * @param coffeeShopId coffee shop identification
 * @param branchId     branch identification
 * @param oldName      old name
 * @param name         new name
 * @author Aleksandr Yagudin
 */
public record BranchChangeNameEvent(Long coffeeShopId, UUID branchId, String oldName, String name) implements DomainEvent {
    @Override
    public UUID eventId() {
        return UUID.randomUUID();
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }
}
