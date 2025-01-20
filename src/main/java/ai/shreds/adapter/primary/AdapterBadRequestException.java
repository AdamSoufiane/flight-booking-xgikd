package ai.shreds.adapter.primary;

import ai.shreds.adapter.exceptions.AdapterFlightSearchException;

public class AdapterBadRequestException extends AdapterFlightSearchException {

    public AdapterBadRequestException(String message, String errorCode) {
        super(message, errorCode);
    }
}