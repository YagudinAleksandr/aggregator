package uz.aggregator.coffeemock.domain.model;

import uz.aggregator.coffeemock.domain.enums.CoffeeShopStatus;
import uz.aggregator.shared.exception.BusinessRuleViolationException;

import java.util.Objects;
import java.util.UUID;

/**
 * Branch of coffee shop.
 * Changed only through {@link CoffeeShop} aggregate, so business methods are package-private.
 *
 * @author Aleksandr Yagudin
 */
public class Branch {
    private final UUID id;
    private CoffeeShopStatus status;
    private String location;
    private String name;

    private Branch(UUID id, CoffeeShopStatus status, String location, String name) {
        this.id = id;
        this.status = status;
        this.location = location;
        this.name = name;
    }

    /**
     * Create new branch
     *
     * @param id       identification
     * @param location location
     * @param name     name of branch
     * @return {@link Branch}
     */
    static Branch createBranch(UUID id, String location, String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("name_branch_not_null", "name cannot be null or blank");
        }

        if (location == null || location.isBlank()) {
            throw new BusinessRuleViolationException("location_cant_be_empty", "location cannot be null or blank");
        }

        Branch branch = new Branch(id, CoffeeShopStatus.PENDING, location, name);
        return branch;
    }

    /**
     * Restore branch from storage: without validation
     *
     * @param id       identification
     * @param status   status
     * @param location location
     * @param name     name of branch
     * @return {@link Branch}
     */
    public static Branch restore(UUID id, CoffeeShopStatus status, String location, String name) {
        return new Branch(id, status, location, name);
    }

    /**
     * Close branch
     */
    void close() {
        if (status.equals(CoffeeShopStatus.PENDING)) {
            throw new BusinessRuleViolationException("branch_cant_be_blocked_on_pending",
                    "Branch can not be blocked on Pending");
        }

        this.status = CoffeeShopStatus.CLOSED;
    }

    /**
     * Close branch together with coffee shop: in any status, including pending
     */
    void closeWithCoffeeShop() {
        this.status = CoffeeShopStatus.CLOSED;
    }

    /**
     * Activate branch
     */
    void activate() {
        this.status = CoffeeShopStatus.ACTIVE;
    }

    /**
     * Change branch name
     *
     * @param newName new branch name
     */
    void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessRuleViolationException("name_branch_not_null", "name cannot be null or blank");
        }

        if (this.name.equals(newName)) {
            return;
        }

        this.name = newName;
    }

    /**
     * Change location
     *
     * @param newLocation new location
     */
    void changeLocation(String newLocation) {
        if (newLocation == null || newLocation.isBlank()) {
            throw new BusinessRuleViolationException("location_branch_not_null", "location cannot be null or blank");
        }

        if (this.location.equals(newLocation)) {
            return;
        }

        this.location = newLocation;
    }

    public UUID id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String location() {
        return this.location;
    }

    public CoffeeShopStatus status() {
        return this.status;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Branch branch)) {
            return false;
        }
        return id.equals(branch.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
