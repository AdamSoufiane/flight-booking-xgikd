package ai.shreds.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSearchRequest {
    private String origin;
    private String destination;
    private LocalDateTime departureDate;
    private LocalDateTime returnDate;
    private String seatClass;
    
    // Additional fields for application layer processing
    private boolean isRoundTrip;
    private String searchId;
    private LocalDateTime searchTimestamp;
}