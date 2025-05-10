package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.BusTrackingResponse;
import examination.teacherAndStudents.dto.GoogleMapsResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.BusTrackingService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BusTrackingServiceImpl implements BusTrackingService {

    private final ProfileRepository profileRepository;
    private final StudentTransportTrackerRepository studentTransportTrackerRepository;
    private final BusLocationRepository busLocationRepository;
    private final StudentTermRepository studentTermRepository;
    private final RestTemplate restTemplate; // For calling Google Maps API

    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyDJFsAzinZoqJ8R0g8vR4iKA9woxWNa520";
    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";

    @Override
    public BusTrackingResponse trackBus() {
        String email = SecurityConfig.getAuthenticatedUserEmail();

        // Find student profile
        Profile student = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: "));

        // Find current term based on current date
        StudentTerm currentTerm = studentTermRepository.findCurrentTerm(LocalDate.now())
                .orElseThrow(() -> new CustomNotFoundException("No active term found for current date"));

        // Find transport allocation for the student and current term
        StudentTransportAllocation allocation = studentTransportTrackerRepository.findByProfileAndTerm(student, currentTerm)
                .orElseThrow(() -> new CustomNotFoundException("No transport allocation found for student in current term"));

        Bus bus = allocation.getTransport();
        Stop stop = allocation.getStop();

        // Get bus location
        BusLocation busLocation = busLocationRepository.findByBusBusId(bus.getBusId())
                .orElseThrow(() -> new CustomNotFoundException("Bus location not found"));

        // Call Google Maps Directions API
        String origin = busLocation.getLatitude() + "," + busLocation.getLongitude();
        String destination = stop.getLatitude() + "," + stop.getLongitude();
        String url = String.format("%s?origin=%s&destination=%s&key=%s", DIRECTIONS_API_URL, origin, destination, GOOGLE_MAPS_API_KEY);

        // Make API call (simplified; handle errors in production)
        GoogleMapsResponse response = restTemplate.getForObject(url, GoogleMapsResponse.class);

        // Extract distance and duration
        assert response != null;
        Double distance = response.getRoutes().get(0).getLegs().get(0).getDistance().getValue() / 1000.0; // Convert meters to km
        String duration = response.getRoutes().get(0).getLegs().get(0).getDuration().getText(); // e.g., "15 mins"

        return BusTrackingResponse.builder()
                .busId(bus.getBusId())
                .busLatitude(busLocation.getLatitude())
                .busLongitude(busLocation.getLongitude())
                .stopAddress(stop.getAddress())
                .stopLatitude(stop.getLatitude())
                .stopLongitude(stop.getLongitude())
                .distance(distance)
                .estimatedTime(duration)
                .build();
    }
}