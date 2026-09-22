package uz.aggregator.shared.exception;

/**
 * Access denied exception
 *
 * @author Aleksandr Yagudin
 */
public class AccessDeniedException extends DomainException {
    private static final String CODE = "access_denied";

    /**
     * Access denied
     *
     * @param resource resource name
     * @param message  exception message
     */
    public AccessDeniedException(String resource, String message) {
        super(CODE, "Access: " + resource + " denied. " + message);
    }

    /**
     * Access denied
     *
     * @param resource resource
     */
    public AccessDeniedException(String resource) {
        super(CODE, "Access: " + resource + " denied. ");
    }
}
