package uz.aggregator.shared.exception;

/**
 * Base abstract domain exception
 *
 * @author Aleksandr Yagudin
 */
public abstract class DomainException extends RuntimeException {
    private final String code;

    /**
     * Domain exception
     *
     * @param code    exception code
     * @param message exception message
     */
    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Exception code
     *
     * @return code
     */
    public String code() {
        return this.code;
    }
}
