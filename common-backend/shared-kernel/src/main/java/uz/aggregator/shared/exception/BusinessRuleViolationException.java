package uz.aggregator.shared.exception;

/**
 * Rule business exception
 *
 * @author Aleksandr Yagudin
 */
public class BusinessRuleViolationException extends DomainException {
    /**
     * Domain exception
     *
     * @param code    exception code
     * @param message exception message
     */
    public BusinessRuleViolationException(String code, String message) {
        super(code, message);
    }
}
