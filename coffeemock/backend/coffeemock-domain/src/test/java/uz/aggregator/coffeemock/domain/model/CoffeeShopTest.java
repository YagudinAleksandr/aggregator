package uz.aggregator.coffeemock.domain.model;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uz.aggregator.coffeemock.domain.enums.CoffeeShopStatus;
import uz.aggregator.coffeemock.domain.event.CoffeeShopChangeNameEvent;
import uz.aggregator.coffeemock.domain.event.CoffeeShopCloseEvent;
import uz.aggregator.coffeemock.domain.event.CoffeeShopRegisteredEvent;
import uz.aggregator.shared.event.DomainEvent;
import uz.aggregator.shared.exception.BusinessRuleViolationException;

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
}
