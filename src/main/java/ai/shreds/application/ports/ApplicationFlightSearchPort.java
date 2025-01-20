package ai.shreds.application.ports;

import ai.shreds.application.exceptions.ApplicationFlightSearchException;
import ai.shreds.application.exceptions.ApplicationInvalidSearchException;
import ai.shreds.shared.dtos.SharedFlightSearchRequest;
import ai.shreds.shared.dtos.SharedFlightSearchResponse;

/**
 * Port defining flight search operations at the application layer.
 * Implementations should handle caching, validation, and coordination with domain services.
 */
public interface ApplicationFlightSearchPort {

    /**
     * Searches for flights based on the provided criteria.
     * Implements caching and validation before delegating to domain services.
     *
     * @param request the search criteria including origin, destination, dates, and preferences
     * @return response containing matching flights and metadata
     * @throws ApplicationInvalidSearchException if the search parameters are invalid
     * @throws ApplicationFlightSearchException if an error occurs during the search process
     */
    SharedFlightSearchResponse searchFlights(SharedFlightSearchRequest request);

    /**
     * Asynchronously refreshes the cache for a specific search criteria.
     * This method should be called periodically to keep the cache up to date.
     *
     * @param request the search criteria to refresh
     * @throws ApplicationFlightSearchException if an error occurs during cache refresh
     */
    default void refreshSearchCache(SharedFlightSearchRequest request) {
        // Default implementation does nothing
        // Implementations should override this method if they support cache refresh
    }

    /**
     * Clears the cache for a specific search criteria.
     * This method should be called when the cached data becomes invalid.
     *
     * @param request the search criteria to clear from cache
     */
    default void clearSearchCache(SharedFlightSearchRequest request) {
        // Default implementation does nothing
        // Implementations should override this method if they support cache clearing
    }
}
