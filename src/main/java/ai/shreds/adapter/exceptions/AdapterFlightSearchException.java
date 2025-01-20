package ai.shreds.adapter.exceptions;

public class AdapterFlightSearchException extends RuntimeException {
    private final String errorCode;

    public AdapterFlightSearchException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}