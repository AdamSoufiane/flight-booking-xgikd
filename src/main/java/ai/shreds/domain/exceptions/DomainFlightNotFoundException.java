package ai.shreds.domain.exceptions;

import lombok.Getter;

/**
 * Exception thrown when a requested flight cannot be found.
 * Used in domain layer to indicate flight lookup failures.
 */
@Getter
public class DomainFlightNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String flightReference;

    /**
     * Creates a new exception with a message.
     *
     * @param message error message
     */
    public DomainFlightNotFoundException(String message) {
        super(message);
        this.flightReference = null;
    }

    /**
     * Creates a new exception with a message and flight reference.
     *
     * @param message error message
     * @param flightReference reference to the flight that wasn't found
     */
    public DomainFlightNotFoundException(String message, String flightReference) {
        super(message);
        this.flightReference = flightReference;
    }

    /**
     * Creates a new exception with a message, cause, and flight reference.
     *
     * @param message error message
     * @param cause the cause of this exception
     * @param flightReference reference to the flight that wasn't found
     */
    public DomainFlightNotFoundException(String message, Throwable cause, String flightReference) {
        super(message, cause);
        this.flightReference = flightReference;
    }

    /**
     * Gets a detailed message including flight reference if available.
     *
     * @return detailed error message
     */
    @Override
    public String getMessage() {
        if (flightReference != null && !flightReference.isEmpty()) {
            return super.getMessage() + " (Flight Reference: " + flightReference + ")";
        }
        return super.getMessage();
    }
}
