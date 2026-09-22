package uz.aggregator.shared.exception;

/**
 * Resource not found exception
 *
 * @author Aleksandr Yagudin
 */
public class NotFoundException extends DomainException {
    private static final String CODE = "not_found";

    /**
     * Resource not found
     *
     * @param entity entity or aggregate
     */
    public NotFoundException(String entity) {
        super(CODE, "Entity: " + entity + " not found");
    }

    /**
     * Resource not found
     *
     * @param message exception message
     * @param entity  entity or aggregate
     */
    public NotFoundException(String entity, String message) {
        super(CODE, "Entity: " + entity + " not found. " + message);
    }
}
