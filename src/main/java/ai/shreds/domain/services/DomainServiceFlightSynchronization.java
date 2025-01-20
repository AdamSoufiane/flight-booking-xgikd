package ai.shreds.domain.services;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.entities.DomainEntitySeatAvailability;
import ai.shreds.domain.entities.DomainEntityAirlineInfo;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.ports.DomainPortFlightDataSync;
import ai.shreds.domain.ports.DomainPortFlightRepository;
import ai.shreds.domain.ports.DomainPortSeatAvailabilityRepository;
import ai.shreds.domain.ports.DomainPortAirlineInfoRepository;

/**
 * Domain service responsible for synchronizing flight data from external sources.
 * Handles the synchronization of flight schedules, seat availability, and airline information.
 */
public class DomainServiceFlightSynchronization {

    private final DomainPortFlightDataSync flightDataSync;
    private final DomainPortFlightRepository flightRepository;
    private final DomainPortSeatAvailabilityRepository seatAvailabilityRepository;
    private final DomainPortAirlineInfoRepository airlineInfoRepository;
    private final DomainDataValidationService validationService;

    public DomainServiceFlightSynchronization(
            DomainPortFlightDataSync flightDataSync,
            DomainPortFlightRepository flightRepository,
            DomainPortSeatAvailabilityRepository seatAvailabilityRepository,
            DomainPortAirlineInfoRepository airlineInfoRepository,
            DomainDataValidationService validationService) {
        this.flightDataSync = flightDataSync;
        this.flightRepository = flightRepository;
        this.seatAvailabilityRepository = seatAvailabilityRepository;
        this.airlineInfoRepository = airlineInfoRepository;
        this.validationService = validationService;
    }

    /**
     * Synchronizes flight data for specified airlines.
     * 
     * @param airlineIds List of airline IDs to synchronize data for
     * @throws DomainException if synchronization fails
     */
    public void synchronizeFlightData(List<UUID> airlineIds) {
        List<String> errors = new ArrayList<>();
        AtomicInteger processedCount = new AtomicInteger(0);

        try {
            // First, sync airline information
            List<DomainEntityAirlineInfo> airlineInfos = syncAirlineInfo(airlineIds, errors);

            // Then, sync flight schedules
            List<DomainEntityFlightSchedule> flights = syncFlightSchedules(airlineIds, errors);

            // Finally, sync seat availability
            syncSeatAvailability(flights, errors);

            // If there were any errors during synchronization
            if (!errors.isEmpty()) {
                throw new DomainException("Synchronization completed with errors: " + 
                        String.join("; ", errors));
            }

        } catch (Exception e) {
            throw new DomainException("Flight data synchronization failed: " + e.getMessage());
        }
    }

    private List<DomainEntityAirlineInfo> syncAirlineInfo(
            List<UUID> airlineIds, List<String> errors) {
        
        List<DomainEntityAirlineInfo> airlineInfos = flightDataSync.fetchExternalAirlineData();
        List<DomainEntityAirlineInfo> validAirlineInfos = new ArrayList<>();

        for (DomainEntityAirlineInfo airlineInfo : airlineInfos) {
            try {
                if (validationService.validateAirlineInfo(airlineInfo)) {
                    airlineInfoRepository.save(airlineInfo);
                    validAirlineInfos.add(airlineInfo);
                }
            } catch (Exception e) {
                errors.add("Failed to sync airline info for ID " + 
                        airlineInfo.getAirlineId() + ": " + e.getMessage());
            }
        }

        return validAirlineInfos;
    }

    private List<DomainEntityFlightSchedule> syncFlightSchedules(
            List<UUID> airlineIds, List<String> errors) {
        
        List<DomainEntityFlightSchedule> flights = flightDataSync.fetchFlightData(airlineIds);
        List<DomainEntityFlightSchedule> validFlights = new ArrayList<>();

        for (DomainEntityFlightSchedule flight : flights) {
            try {
                if (validationService.validateFlightSchedule(flight)) {
                    DomainEntityFlightSchedule existingFlight = 
                            flightRepository.findById(flight.getFlightId());

                    if (existingFlight != null) {
                        // Update existing flight
                        flightRepository.updateFlightSchedule(flight);
                    } else {
                        // Create new flight
                        flightRepository.createFlightSchedule(flight);
                    }
                    validFlights.add(flight);
                }
            } catch (Exception e) {
                errors.add("Failed to sync flight schedule for ID " + 
                        flight.getFlightId() + ": " + e.getMessage());
            }
        }

        return validFlights;
    }

    private void syncSeatAvailability(
            List<DomainEntityFlightSchedule> flights, List<String> errors) {
        
        List<DomainEntitySeatAvailability> seatData = flightDataSync.fetchExternalSeatData();

        for (DomainEntitySeatAvailability seatAvailability : seatData) {
            try {
                if (validationService.validateSeatAvailability(seatAvailability)) {
                    List<DomainEntitySeatAvailability> existingSeats = 
                            seatAvailabilityRepository.findByFlightId(seatAvailability.getFlightId());

                    if (!existingSeats.isEmpty()) {
                        // Update existing seat availability
                        seatAvailabilityRepository.updateSeatAvailability(seatAvailability);
                    } else {
                        // Create new seat availability
                        seatAvailabilityRepository.createSeatAvailability(seatAvailability);
                    }
                }
            } catch (Exception e) {
                errors.add("Failed to sync seat availability for flight ID " + 
                        seatAvailability.getFlightId() + ": " + e.getMessage());
            }
        }
    }
}
