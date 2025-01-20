package ai.shreds.adapter.exceptions;

public class AdapterNotFoundException extends AdapterFlightSearchException {
    public AdapterNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}