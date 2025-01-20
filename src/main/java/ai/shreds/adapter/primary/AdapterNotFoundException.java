package ai.shreds.adapter.primary;

import ai.shreds.adapter.exceptions.AdapterFlightSearchException;

public class AdapterNotFoundException extends AdapterFlightSearchException {

    public AdapterNotFoundException(String message, String errorCode) {
        super(message, errorCode);
    }
}