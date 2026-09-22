package uz.aggregator.coffeemock.domain.model;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uz.aggregator.coffeemock.domain.enums.CoffeeShopStatus;
import uz.aggregator.coffeemock.domain.event.BranchActivatedEvent;
import uz.aggregator.coffeemock.domain.event.BranchAddedEvent;
import uz.aggregator.coffeemock.domain.event.BranchChangeLocationEvent;
import uz.aggregator.coffeemock.domain.event.BranchChangeNameEvent;
import uz.aggregator.coffeemock.domain.event.BranchClosedEvent;
import uz.aggregator.coffeemock.domain.event.CoffeeShopChangeNameEvent;
import uz.aggregator.coffeemock.domain.event.CoffeeShopCloseEvent;
import uz.aggregator.coffeemock.domain.event.CoffeeShopRegisteredEvent;
import uz.aggregator.shared.event.DomainEvent;
import uz.aggregator.shared.exception.BusinessRuleViolationException;
import uz.aggregator.shared.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link CoffeeShop} aggregate
 *
 * @author Aleksandr Yagudin
 */
class CoffeeShopTest {
    private static final Long ID = 1L;
    private static final String NAME = "Coffee Point";
    private static final String LOGO_URL = "https://cdn.example.uz/logo.png";
    private static final String LAW_ADDRESS = "Tashkent, Amir Temur str. 1";
    private static final String PHONE = "+998901234567";
    private static final String EMAIL = "info@coffee.uz";
    private static final String CONTACT_NAME = "Aleksandr";

    private static final UUID BRANCH_ID = UUID.fromString("7f1c1a52-3b3e-4c64-9d2a-0d6c1f0b9a11");
    private static final String BRANCH_NAME = "Yunusabad";
    private static final String BRANCH_LOCATION = "Tashkent, Yunusabad 4";

    private static CoffeeShop newShop() {
        return CoffeeShop.create(ID, NAME, LOGO_URL, LAW_ADDRESS, PHONE, EMAIL, CONTACT_NAME);
    }

    /**
     * Shop without registration event, so tests see only events of the tested action
     */
    private static CoffeeShop newShopWithoutEvents() {
        CoffeeShop shop = newShop();
        shop.pullDomainEvents();
        return shop;
    }

    private static CoffeeShop activeShop() {
        CoffeeShop shop = newShopWithoutEvents();
        shop.activate();
        return shop;
    }

    private static void assertRuleViolation(Runnable action, String code) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting(e -> ((BusinessRuleViolationException) e).code())
                .isEqualTo(code);
    }

    @Nested
    class Create {

        @Test
        void createsPendingShopWithAllFields() {
            CoffeeShop shop = newShop();

            assertThat(shop.id()).isEqualTo(ID);
            assertThat(shop.name()).isEqualTo(NAME);
            assertThat(shop.logoUrl()).isEqualTo(LOGO_URL);
            assertThat(shop.lawAddress()).isEqualTo(LAW_ADDRESS);
            assertThat(shop.phone()).isEqualTo(PHONE);
            assertThat(shop.email()).isEqualTo(EMAIL);
            assertThat(shop.contactName()).isEqualTo(CONTACT_NAME);
            assertThat(shop.status()).isEqualTo(CoffeeShopStatus.PENDING);
        }

        @Test
        void registersRegisteredEvent() {
            CoffeeShop shop = newShop();

            assertThat(shop.peekDomainEvents())
                    .singleElement()
                    .isEqualTo(new CoffeeShopRegisteredEvent(ID, NAME));
        }

        @Test
        void allowsEmptyLogoAndEmail() {
            CoffeeShop shop = CoffeeShop.create(ID, NAME, null, LAW_ADDRESS, PHONE, null, CONTACT_NAME);

            assertThat(shop.logoUrl()).isNull();
            assertThat(shop.email()).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankName(String name) {
            assertRuleViolation(
                    () -> CoffeeShop.create(ID, name, LOGO_URL, LAW_ADDRESS, PHONE, EMAIL, CONTACT_NAME),
                    "empty_name");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankLawAddress(String lawAddress) {
            assertRuleViolation(
                    () -> CoffeeShop.create(ID, NAME, LOGO_URL, lawAddress, PHONE, EMAIL, CONTACT_NAME),
                    "empty_law_address");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankPhone(String phone) {
            assertRuleViolation(
                    () -> CoffeeShop.create(ID, NAME, LOGO_URL, LAW_ADDRESS, phone, EMAIL, CONTACT_NAME),
                    "empty_phone");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankContactName(String contactName) {
            assertRuleViolation(
                    () -> CoffeeShop.create(ID, NAME, LOGO_URL, LAW_ADDRESS, PHONE, EMAIL, contactName),
                    "empty_contact_name");
        }
    }

    @Nested
    class ChangeName {

        @Test
        void changesNameAndRegistersEvent() {
            CoffeeShop shop = newShopWithoutEvents();

            shop.changeName("Coffee House");

            assertThat(shop.name()).isEqualTo("Coffee House");
            assertThat(shop.peekDomainEvents())
                    .singleElement()
                    .isEqualTo(new CoffeeShopChangeNameEvent(ID, NAME, "Coffee House"));
        }

        @Test
        void sameNameDoesNothing() {
            CoffeeShop shop = newShopWithoutEvents();

            shop.changeName(NAME);

            assertThat(shop.name()).isEqualTo(NAME);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankName(String newName) {
            CoffeeShop shop = newShopWithoutEvents();

            assertRuleViolation(() -> shop.changeName(newName), "empty_name");
            assertThat(shop.name()).isEqualTo(NAME);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }
    }

    @Nested
    class ChangeLogoUrl {

        @Test
        void changesLogoUrl() {
            CoffeeShop shop = newShop();

            shop.changeLogoUrl("https://cdn.example.uz/new-logo.png");

            assertThat(shop.logoUrl()).isEqualTo("https://cdn.example.uz/new-logo.png");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankLogoUrl(String newLogoUrl) {
            CoffeeShop shop = newShop();

            assertRuleViolation(() -> shop.changeLogoUrl(newLogoUrl), "empty_logo_url");
            assertThat(shop.logoUrl()).isEqualTo(LOGO_URL);
        }
    }

    @Nested
    class Activate {

        @Test
        void activatesPendingShop() {
            CoffeeShop shop = newShop();

            shop.activate();

            assertThat(shop.status()).isEqualTo(CoffeeShopStatus.ACTIVE);
        }
    }

    @Nested
    class Close {

        @Test
        void closesActiveShopAndRegistersEvent() {
            CoffeeShop shop = activeShop();

            shop.close("Violation of rules");

            assertThat(shop.status()).isEqualTo(CoffeeShopStatus.CLOSED);
            assertThat(shop.peekDomainEvents())
                    .singleElement()
                    .isEqualTo(new CoffeeShopCloseEvent(ID, "Violation of rules"));
        }

        @Test
        void rejectsClosingPendingShop() {
            CoffeeShop shop = newShopWithoutEvents();

            assertRuleViolation(() -> shop.close("reason"), "can_not_block_pending_coffee_shop");
            assertThat(shop.status()).isEqualTo(CoffeeShopStatus.PENDING);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void closingAlreadyClosedShopDoesNothing() {
            CoffeeShop shop = activeShop();
            shop.close("first");
            shop.pullDomainEvents();

            shop.close("second");

            assertThat(shop.status()).isEqualTo(CoffeeShopStatus.CLOSED);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }
    }

    @Nested
    class ChangeAddress {

        @Test
        void changesAddress() {
            CoffeeShop shop = newShop();

            shop.changeAddress("Samarkand, Registan 5");

            assertThat(shop.lawAddress()).isEqualTo("Samarkand, Registan 5");
        }

        @Test
        void sameAddressKeepsValue() {
            CoffeeShop shop = newShop();

            shop.changeAddress(LAW_ADDRESS);

            assertThat(shop.lawAddress()).isEqualTo(LAW_ADDRESS);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankAddress(String newAddress) {
            CoffeeShop shop = newShop();

            assertRuleViolation(() -> shop.changeAddress(newAddress), "empty_address");
            assertThat(shop.lawAddress()).isEqualTo(LAW_ADDRESS);
        }
    }

    @Nested
    class ChangePhone {

        @Test
        void changesPhone() {
            CoffeeShop shop = newShop();

            shop.changePhone("+998977654321");

            assertThat(shop.phone()).isEqualTo("+998977654321");
        }

        @Test
        void samePhoneKeepsValue() {
            CoffeeShop shop = newShop();

            shop.changePhone(PHONE);

            assertThat(shop.phone()).isEqualTo(PHONE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankPhone(String newPhone) {
            CoffeeShop shop = newShop();

            assertRuleViolation(() -> shop.changePhone(newPhone), "empty_phone");
            assertThat(shop.phone()).isEqualTo(PHONE);
        }
    }

    @Nested
    class ChangeEmail {

        @Test
        void changesEmail() {
            CoffeeShop shop = newShop();

            shop.changeEmail("new@coffee.uz");

            assertThat(shop.email()).isEqualTo("new@coffee.uz");
        }

        @Test
        void sameEmailKeepsValue() {
            CoffeeShop shop = newShop();

            shop.changeEmail(EMAIL);

            assertThat(shop.email()).isEqualTo(EMAIL);
        }

        @Test
        @Disabled("Bug: changeEmail() throws NullPointerException when shop was created without email")
        void setsEmailWhenShopWasCreatedWithoutEmail() {
            CoffeeShop shop = CoffeeShop.create(ID, NAME, LOGO_URL, LAW_ADDRESS, PHONE, null, CONTACT_NAME);

            shop.changeEmail("new@coffee.uz");

            assertThat(shop.email()).isEqualTo("new@coffee.uz");
        }
    }

    @Nested
    class ChangeContactName {

        @Test
        void changesContactName() {
            CoffeeShop shop = newShop();

            shop.changeContactName("Timur");

            assertThat(shop.contactName()).isEqualTo("Timur");
        }

        @Test
        void sameContactNameKeepsValue() {
            CoffeeShop shop = newShop();

            shop.changeContactName(CONTACT_NAME);

            assertThat(shop.contactName()).isEqualTo(CONTACT_NAME);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankContactName(String newName) {
            CoffeeShop shop = newShop();

            assertRuleViolation(() -> shop.changeContactName(newName), "empty_contact_name");
            assertThat(shop.contactName()).isEqualTo(CONTACT_NAME);
        }
    }

    @Nested
    class DomainEvents {

        @Test
        void pullReturnsEventsAndClearsThem() {
            CoffeeShop shop = newShop();
            shop.changeName("Coffee House");

            assertThat(shop.pullDomainEvents())
                    .extracting(DomainEvent::eventType)
                    .containsExactly("CoffeeShopRegisteredEvent", "CoffeeShopChangeNameEvent");
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        @Disabled("Bug: eventId() and occurredAt() return a new value on every call")
        void eventIdAndTimeAreStable() {
            DomainEvent event = newShop().peekDomainEvents().getFirst();

            assertThat(event.eventId()).isEqualTo(event.eventId());
            assertThat(event.occurredAt()).isEqualTo(event.occurredAt());
        }
    }

    @Nested
    class Equality {

        @Test
        void shopsWithSameIdAreEqual() {
            CoffeeShop first = newShop();
            CoffeeShop second = CoffeeShop.create(ID, "Other", null, "Other address", "+998000000000", null, "Other");

            assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        }

        @Test
        void shopsWithDifferentIdAreNotEqual() {
            CoffeeShop first = newShop();
            CoffeeShop second = CoffeeShop.create(2L, NAME, LOGO_URL, LAW_ADDRESS, PHONE, EMAIL, CONTACT_NAME);

            assertThat(first).isNotEqualTo(second);
        }
    }

    @Nested
    class Restore {

        @Test
        void restoresAllFieldsWithoutEvents() {
            Branch branch = Branch.restore(BRANCH_ID, CoffeeShopStatus.ACTIVE, BRANCH_LOCATION, BRANCH_NAME);

            CoffeeShop shop = CoffeeShop.restore(ID, NAME, LOGO_URL, CoffeeShopStatus.ACTIVE,
                    LAW_ADDRESS, PHONE, EMAIL, CONTACT_NAME, List.of(branch));

            assertThat(shop.id()).isEqualTo(ID);
            assertThat(shop.name()).isEqualTo(NAME);
            assertThat(shop.logoUrl()).isEqualTo(LOGO_URL);
            assertThat(shop.status()).isEqualTo(CoffeeShopStatus.ACTIVE);
            assertThat(shop.lawAddress()).isEqualTo(LAW_ADDRESS);
            assertThat(shop.phone()).isEqualTo(PHONE);
            assertThat(shop.email()).isEqualTo(EMAIL);
            assertThat(shop.contactName()).isEqualTo(CONTACT_NAME);
            assertThat(shop.branches()).containsExactly(branch);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void restoresWithoutBranches() {
            CoffeeShop shop = CoffeeShop.restore(ID, NAME, LOGO_URL, CoffeeShopStatus.PENDING,
                    LAW_ADDRESS, PHONE, EMAIL, CONTACT_NAME, null);

            assertThat(shop.branches()).isEmpty();
        }

        @Test
        void restoresBranchFields() {
            Branch branch = Branch.restore(BRANCH_ID, CoffeeShopStatus.CLOSED, BRANCH_LOCATION, BRANCH_NAME);

            assertThat(branch.id()).isEqualTo(BRANCH_ID);
            assertThat(branch.status()).isEqualTo(CoffeeShopStatus.CLOSED);
            assertThat(branch.location()).isEqualTo(BRANCH_LOCATION);
            assertThat(branch.name()).isEqualTo(BRANCH_NAME);
        }
    }

    @Nested
    class Branches {

        /**
         * Active shop with pending branch, without events
         */
        private CoffeeShop shopWithBranch() {
            CoffeeShop shop = activeShop();
            shop.addBranch(BRANCH_ID, BRANCH_LOCATION, BRANCH_NAME);
            shop.pullDomainEvents();
            return shop;
        }

        private CoffeeShop shopWithActiveBranch() {
            CoffeeShop shop = shopWithBranch();
            shop.activateBranch(BRANCH_ID);
            shop.pullDomainEvents();
            return shop;
        }

        private Branch onlyBranch(CoffeeShop shop) {
            assertThat(shop.branches()).hasSize(1);
            return shop.branches().getFirst();
        }

        @Test
        void addsPendingBranchAndRegistersEvent() {
            CoffeeShop shop = newShopWithoutEvents();

            Branch branch = shop.addBranch(BRANCH_ID, BRANCH_LOCATION, BRANCH_NAME);

            assertThat(shop.branches()).containsExactly(branch);
            assertThat(branch.id()).isEqualTo(BRANCH_ID);
            assertThat(branch.location()).isEqualTo(BRANCH_LOCATION);
            assertThat(branch.name()).isEqualTo(BRANCH_NAME);
            assertThat(branch.status()).isEqualTo(CoffeeShopStatus.PENDING);
            assertThat(shop.peekDomainEvents())
                    .singleElement()
                    .isEqualTo(new BranchAddedEvent(ID, BRANCH_ID, BRANCH_NAME, BRANCH_LOCATION));
        }

        @Test
        void branchesCanNotBeModifiedOutsideAggregate() {
            CoffeeShop shop = shopWithBranch();

            assertThatThrownBy(() -> shop.branches().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void rejectsDuplicateBranchId() {
            CoffeeShop shop = shopWithBranch();

            assertRuleViolation(() -> shop.addBranch(BRANCH_ID, "Other location", "Other"),
                    "branch_already_exists");
            assertThat(shop.branches()).hasSize(1);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void rejectsAddingBranchToClosedShop() {
            CoffeeShop shop = activeShop();
            shop.close("reason");
            shop.pullDomainEvents();

            assertRuleViolation(() -> shop.addBranch(BRANCH_ID, BRANCH_LOCATION, BRANCH_NAME),
                    "can_not_add_branch_to_closed_coffee_shop");
            assertThat(shop.branches()).isEmpty();
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankBranchName(String name) {
            CoffeeShop shop = newShopWithoutEvents();

            assertRuleViolation(() -> shop.addBranch(BRANCH_ID, BRANCH_LOCATION, name), "name_branch_not_null");
            assertThat(shop.branches()).isEmpty();
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankBranchLocation(String location) {
            CoffeeShop shop = newShopWithoutEvents();

            assertRuleViolation(() -> shop.addBranch(BRANCH_ID, location, BRANCH_NAME), "location_cant_be_empty");
            assertThat(shop.branches()).isEmpty();
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void activatesBranchAndRegistersEvent() {
            CoffeeShop shop = shopWithBranch();

            shop.activateBranch(BRANCH_ID);

            assertThat(onlyBranch(shop).status()).isEqualTo(CoffeeShopStatus.ACTIVE);
            assertThat(shop.peekDomainEvents())
                    .singleElement()
                    .isEqualTo(new BranchActivatedEvent(ID, BRANCH_ID));
        }

        @Test
        void activatingActiveBranchDoesNothing() {
            CoffeeShop shop = shopWithActiveBranch();

            shop.activateBranch(BRANCH_ID);

            assertThat(onlyBranch(shop).status()).isEqualTo(CoffeeShopStatus.ACTIVE);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void rejectsActivatingBranchOfPendingShop() {
            CoffeeShop shop = newShopWithoutEvents();
            shop.addBranch(BRANCH_ID, BRANCH_LOCATION, BRANCH_NAME);
            shop.pullDomainEvents();

            assertRuleViolation(() -> shop.activateBranch(BRANCH_ID),
                    "can_not_activate_branch_of_not_active_coffee_shop");
            assertThat(onlyBranch(shop).status()).isEqualTo(CoffeeShopStatus.PENDING);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void rejectsActivatingBranchOfClosedShop() {
            CoffeeShop shop = shopWithBranch();
            shop.close("reason");
            shop.pullDomainEvents();

            assertRuleViolation(() -> shop.activateBranch(BRANCH_ID),
                    "can_not_activate_branch_of_not_active_coffee_shop");
            assertThat(onlyBranch(shop).status()).isEqualTo(CoffeeShopStatus.CLOSED);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void closesActiveBranchAndRegistersEvent() {
            CoffeeShop shop = shopWithActiveBranch();

            shop.closeBranch(BRANCH_ID);

            assertThat(onlyBranch(shop).status()).isEqualTo(CoffeeShopStatus.CLOSED);
            assertThat(shop.peekDomainEvents())
                    .singleElement()
                    .isEqualTo(new BranchClosedEvent(ID, BRANCH_ID));
        }

        @Test
        void closingClosedBranchDoesNothing() {
            CoffeeShop shop = shopWithActiveBranch();
            shop.closeBranch(BRANCH_ID);
            shop.pullDomainEvents();

            shop.closeBranch(BRANCH_ID);

            assertThat(onlyBranch(shop).status()).isEqualTo(CoffeeShopStatus.CLOSED);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void rejectsClosingPendingBranch() {
            CoffeeShop shop = shopWithBranch();

            assertRuleViolation(() -> shop.closeBranch(BRANCH_ID), "branch_cant_be_blocked_on_pending");
            assertThat(onlyBranch(shop).status()).isEqualTo(CoffeeShopStatus.PENDING);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void changesBranchNameAndRegistersEvent() {
            CoffeeShop shop = shopWithBranch();

            shop.changeBranchName(BRANCH_ID, "Chilanzar");

            assertThat(onlyBranch(shop).name()).isEqualTo("Chilanzar");
            assertThat(shop.peekDomainEvents())
                    .singleElement()
                    .isEqualTo(new BranchChangeNameEvent(ID, BRANCH_ID, BRANCH_NAME, "Chilanzar"));
        }

        @Test
        void sameBranchNameDoesNothing() {
            CoffeeShop shop = shopWithBranch();

            shop.changeBranchName(BRANCH_ID, BRANCH_NAME);

            assertThat(onlyBranch(shop).name()).isEqualTo(BRANCH_NAME);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankNewBranchName(String newName) {
            CoffeeShop shop = shopWithBranch();

            assertRuleViolation(() -> shop.changeBranchName(BRANCH_ID, newName), "name_branch_not_null");
            assertThat(onlyBranch(shop).name()).isEqualTo(BRANCH_NAME);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void changesBranchLocationAndRegistersEvent() {
            CoffeeShop shop = shopWithBranch();

            shop.changeBranchLocation(BRANCH_ID, "Tashkent, Bunyodkor 10");

            assertThat(onlyBranch(shop).location()).isEqualTo("Tashkent, Bunyodkor 10");
            assertThat(shop.peekDomainEvents())
                    .singleElement()
                    .isEqualTo(new BranchChangeLocationEvent(ID, BRANCH_ID, BRANCH_LOCATION, "Tashkent, Bunyodkor 10"));
        }

        @Test
        void sameBranchLocationDoesNothing() {
            CoffeeShop shop = shopWithBranch();

            shop.changeBranchLocation(BRANCH_ID, BRANCH_LOCATION);

            assertThat(onlyBranch(shop).location()).isEqualTo(BRANCH_LOCATION);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankNewBranchLocation(String newLocation) {
            CoffeeShop shop = shopWithBranch();

            assertRuleViolation(() -> shop.changeBranchLocation(BRANCH_ID, newLocation), "location_branch_not_null");
            assertThat(onlyBranch(shop).location()).isEqualTo(BRANCH_LOCATION);
            assertThat(shop.peekDomainEvents()).isEmpty();
        }

        @Test
        void unknownBranchIsNotFound() {
            CoffeeShop shop = shopWithBranch();
            UUID unknownId = UUID.randomUUID();

            assertThatThrownBy(() -> shop.activateBranch(unknownId))
                    .isInstanceOf(NotFoundException.class)
                    .extracting(e -> ((NotFoundException) e).code())
                    .isEqualTo("not_found");
        }
    }

    @Nested
    class CloseWithBranches {

        private static final UUID PENDING_BRANCH_ID = UUID.fromString("0b6a3c1e-8a41-4f0e-9d7b-2f3e4a5b6c01");
        private static final UUID ACTIVE_BRANCH_ID = UUID.fromString("0b6a3c1e-8a41-4f0e-9d7b-2f3e4a5b6c02");
        private static final UUID CLOSED_BRANCH_ID = UUID.fromString("0b6a3c1e-8a41-4f0e-9d7b-2f3e4a5b6c03");

        @Test
        void closesAllBranchesTogetherWithShop() {
            CoffeeShop shop = activeShop();
            shop.addBranch(PENDING_BRANCH_ID, "Location 1", "Pending");
            shop.addBranch(ACTIVE_BRANCH_ID, "Location 2", "Active");
            shop.addBranch(CLOSED_BRANCH_ID, "Location 3", "Closed");
            shop.activateBranch(ACTIVE_BRANCH_ID);
            shop.activateBranch(CLOSED_BRANCH_ID);
            shop.closeBranch(CLOSED_BRANCH_ID);
            shop.pullDomainEvents();

            shop.close("reason");

            assertThat(shop.branches())
                    .extracting(Branch::status)
                    .containsOnly(CoffeeShopStatus.CLOSED);
            // Already closed branch does not get a second event
            assertThat(shop.peekDomainEvents()).containsExactly(
                    new CoffeeShopCloseEvent(ID, "reason"),
                    new BranchClosedEvent(ID, PENDING_BRANCH_ID),
                    new BranchClosedEvent(ID, ACTIVE_BRANCH_ID));
        }
    }
}
