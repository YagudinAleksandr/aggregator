package uz.aggregator.coffeemock.domain.model;

import uz.aggregator.coffeemock.domain.enums.CoffeeShopStatus;
import uz.aggregator.coffeemock.domain.event.BranchActivatedEvent;
import uz.aggregator.coffeemock.domain.event.BranchAddedEvent;
import uz.aggregator.coffeemock.domain.event.BranchChangeLocationEvent;
import uz.aggregator.coffeemock.domain.event.BranchChangeNameEvent;
import uz.aggregator.coffeemock.domain.event.BranchClosedEvent;
import uz.aggregator.coffeemock.domain.event.CoffeeShopCloseEvent;
import uz.aggregator.coffeemock.domain.event.CoffeeShopChangeNameEvent;
import uz.aggregator.coffeemock.domain.event.CoffeeShopRegisteredEvent;
import uz.aggregator.shared.aggregate.AggregateRoot;
import uz.aggregator.shared.exception.BusinessRuleViolationException;
import uz.aggregator.shared.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Coffee shop aggregate
 *
 * @author Aleksandr Yagudin
 */
public class CoffeeShop extends AggregateRoot<Long> {
    private final Long id;
    private String name;
    private String logoUrl;
    private CoffeeShopStatus status;
    private String lawAddress;
    private String phone;
    private String email;
    private String contactName;
    private final List<Branch> branches = new ArrayList<>();

    private CoffeeShop(Long id, String name,
                       String logoUrl,
                       CoffeeShopStatus status,
                       String lawAddress,
                       String phone,
                       String email,
                       String contactName) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.lawAddress = lawAddress;
        this.status = status;
    }

    /**
     * Create coffee shop
     *
     * @param id          identification
     * @param name        name
     * @param logoUrl     logo img url
     * @param lawAddress  address
     * @param phone       contact phone
     * @param email       e-mail address
     * @param contactName contact name
     * @return {@link CoffeeShop}
     */
    public static CoffeeShop create(Long id,
                                    String name,
                                    String logoUrl,
                                    String lawAddress,
                                    String phone,
                                    String email,
                                    String contactName) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("empty_name", "Name can not be empty");
        }

        if (lawAddress == null || lawAddress.isBlank()) {
            throw new BusinessRuleViolationException("empty_law_address", "Law Address can not be empty");
        }

        if (phone == null || phone.isBlank()) {
            throw new BusinessRuleViolationException("empty_phone", "Phone can not be empty");
        }

        if (contactName == null || contactName.isBlank()) {
            throw new BusinessRuleViolationException("empty_contact_name", "Contact Name can not be empty");
        }

        CoffeeShop coffeeShop = new CoffeeShop(id,
                name,
                logoUrl,
                CoffeeShopStatus.PENDING,
                lawAddress,
                phone,
                email,
                contactName);

        coffeeShop.registerEvent(new CoffeeShopRegisteredEvent(id, name));

        return coffeeShop;
    }

    /**
     * Restore coffee shop from storage: without validation and domain events
     *
     * @param id          identification
     * @param name        name
     * @param logoUrl     logo img url
     * @param status      status
     * @param lawAddress  address
     * @param phone       contact phone
     * @param email       e-mail address
     * @param contactName contact name
     * @param branches    branches of coffee shop
     * @return {@link CoffeeShop}
     */
    public static CoffeeShop restore(Long id,
                                     String name,
                                     String logoUrl,
                                     CoffeeShopStatus status,
                                     String lawAddress,
                                     String phone,
                                     String email,
                                     String contactName,
                                     List<Branch> branches) {
        CoffeeShop coffeeShop = new CoffeeShop(id,
                name,
                logoUrl,
                status,
                lawAddress,
                phone,
                email,
                contactName);

        if (branches != null) {
            coffeeShop.branches.addAll(branches);
        }

        return coffeeShop;
    }

    /**
     * Change coffee shop name
     *
     * @param newName new name
     */
    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessRuleViolationException("empty_name", "Name can not be empty");
        }
        if (newName.equals(this.name)) {
            return;
        }
        registerEvent(new CoffeeShopChangeNameEvent(this.id, this.name, newName));
        this.name = newName;
    }

    /**
     * Change logo
     *
     * @param newLogoUrl new logo URL
     */
    public void changeLogoUrl(String newLogoUrl) {
        if (newLogoUrl == null || newLogoUrl.isBlank()) {
            throw new BusinessRuleViolationException("empty_logo_url", "Logo Url can not be empty");
        }
        this.logoUrl = newLogoUrl;
    }

    /**
     * Close coffee shop together with all its branches
     *
     * @param reason reason of close
     */
    public void close(String reason) {
        if (status == CoffeeShopStatus.PENDING) {
            throw new BusinessRuleViolationException("can_not_block_pending_coffee_shop",
                    "Can not block pending coffee shop");
        }

        if (status.equals(CoffeeShopStatus.CLOSED)) {
            return;
        }

        status = CoffeeShopStatus.CLOSED;
        registerEvent(new CoffeeShopCloseEvent(this.id, reason));

        for (Branch branch : branches) {
            if (branch.status() == CoffeeShopStatus.CLOSED) {
                continue;
            }
            branch.closeWithCoffeeShop();
            registerEvent(new BranchClosedEvent(this.id, branch.id()));
        }
    }

    /**
     * Activate coffee shop
     */
    public void activate() {
        this.status = CoffeeShopStatus.ACTIVE;
    }

    /**
     * Change law address
     *
     * @param newAddress new law address
     */
    public void changeAddress(String newAddress) {
        if (newAddress == null || newAddress.isBlank()) {
            throw new BusinessRuleViolationException("empty_address", "Address can not be empty");
        }

        if (newAddress.equals(this.lawAddress)) {
            return;
        }

        this.lawAddress = newAddress;
    }

    /**
     * Change contact phone number
     *
     * @param newPhone new phone number
     */
    public void changePhone(String newPhone) {
        if (newPhone == null || newPhone.isBlank()) {
            throw new BusinessRuleViolationException("empty_phone", "Phone can not be empty");
        }

        if (phone.equals(newPhone)) {
            return;
        }

        phone = newPhone;
    }

    /**
     * Change e-mail
     *
     * @param newEmail new E-mail
     */
    public void changeEmail(String newEmail) {
        if (this.email.equals(newEmail)) {
            return;
        }

        this.email = newEmail;
    }

    /**
     * Change contact name
     *
     * @param newName new contact name
     */
    public void changeContactName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessRuleViolationException("empty_contact_name", "Contact Name can not be empty");
        }

        if (this.contactName.equals(newName)) {
            return;
        }

        this.contactName = newName;
    }

    /**
     * Add new branch
     *
     * @param branchId identification of branch
     * @param location location
     * @param name     name of branch
     * @return created {@link Branch}
     */
    public Branch addBranch(UUID branchId, String location, String name) {
        if (status == CoffeeShopStatus.CLOSED) {
            throw new BusinessRuleViolationException("can_not_add_branch_to_closed_coffee_shop",
                    "Can not add branch to closed coffee shop");
        }

        if (branches.stream().anyMatch(branch -> branch.id().equals(branchId))) {
            throw new BusinessRuleViolationException("branch_already_exists", "Branch already exists");
        }

        Branch branch = Branch.createBranch(branchId, location, name);
        branches.add(branch);
        registerEvent(new BranchAddedEvent(this.id, branchId, name, location));
        return branch;
    }

    /**
     * Activate branch. Only active coffee shop can activate branches
     *
     * @param branchId identification of branch
     */
    public void activateBranch(UUID branchId) {
        if (status != CoffeeShopStatus.ACTIVE) {
            throw new BusinessRuleViolationException("can_not_activate_branch_of_not_active_coffee_shop",
                    "Can not activate branch of not active coffee shop");
        }

        Branch branch = branch(branchId);
        if (branch.status() == CoffeeShopStatus.ACTIVE) {
            return;
        }

        branch.activate();
        registerEvent(new BranchActivatedEvent(this.id, branchId));
    }

    /**
     * Close branch
     *
     * @param branchId identification of branch
     */
    public void closeBranch(UUID branchId) {
        Branch branch = branch(branchId);
        if (branch.status() == CoffeeShopStatus.CLOSED) {
            return;
        }

        branch.close();
        registerEvent(new BranchClosedEvent(this.id, branchId));
    }

    /**
     * Change branch name
     *
     * @param branchId identification of branch
     * @param newName  new branch name
     */
    public void changeBranchName(UUID branchId, String newName) {
        Branch branch = branch(branchId);
        String oldName = branch.name();

        branch.changeName(newName);

        if (!oldName.equals(branch.name())) {
            registerEvent(new BranchChangeNameEvent(this.id, branchId, oldName, branch.name()));
        }
    }

    /**
     * Change branch location
     *
     * @param branchId    identification of branch
     * @param newLocation new location
     */
    public void changeBranchLocation(UUID branchId, String newLocation) {
        Branch branch = branch(branchId);
        String oldLocation = branch.location();

        branch.changeLocation(newLocation);

        if (!oldLocation.equals(branch.location())) {
            registerEvent(new BranchChangeLocationEvent(this.id, branchId, oldLocation, branch.location()));
        }
    }

    private Branch branch(UUID branchId) {
        return branches.stream()
                .filter(branch -> branch.id().equals(branchId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Branch", "Id: " + branchId));
    }

    @Override
    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String lawAddress() {
        return lawAddress;
    }

    public String phone() {
        return phone;
    }

    public String contactName() {
        return contactName;
    }

    public String email() {
        return email;
    }

    public String logoUrl() {
        return logoUrl;
    }

    public CoffeeShopStatus status() {
        return status;
    }

    public List<Branch> branches() {
        return Collections.unmodifiableList(branches);
    }
}
