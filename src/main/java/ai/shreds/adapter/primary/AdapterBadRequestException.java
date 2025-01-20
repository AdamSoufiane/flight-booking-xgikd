package ai.shreds.adapter.exceptions;

public class AdapterBadRequestException extends AdapterFlightSearchException {
    public AdapterBadRequestException(String message, String errorCode) {
        super(message, errorCode);
    }
}