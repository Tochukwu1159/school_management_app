package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.BusLocationRequest;
import examination.teacherAndStudents.entity.Bus;
import examination.teacherAndStudents.entity.BusLocation;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.BusLocationRepository;
import examination.teacherAndStudents.repository.TransportRepository;
import examination.teacherAndStudents.service.BusLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusLocationServiceImpl implements BusLocationService {

    private final BusLocationRepository busLocationRepository;
    private final TransportRepository transportRepository;

    @Override
    @Transactional
    public String updateBusLocation(BusLocationRequest request) {
        Bus bus = transportRepository.findById(request.getBusId())
                .orElseThrow(() -> new CustomNotFoundException("Bus not found with ID: " + request.getBusId()));

        BusLocation location = busLocationRepository.findByBusBusId(request.getBusId())
                .orElse(BusLocation.builder().bus(bus).build());

        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setBus(bus);

        busLocationRepository.save(location);
        return null;
    }

    @Override
    public BusLocation getBusLocation(Long busId) {
        return busLocationRepository.findByBusBusId(busId)
                .orElseThrow(() -> new CustomNotFoundException("Location not found for bus ID: " + busId));
    }
}


//
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        config.enableSimpleBroker("/topic");
//        config.setApplicationDestinationPrefixes("/app");
//    }
//
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws").withSockJS();
//    }
//}
//
//// Send Bus Location Updates
//@Service
//@RequiredArgsConstructor
//public class BusLocationServiceImpl implements BusLocationService {
//    private final SimpMessagingTemplate messagingTemplate;
//
//    @Override
//    @Transactional
//    public void updateBusLocation(BusLocationRequest request) {
//        // Existing save logic
//        Bus bus = transportRepository.findById(request.getBusId())
//                .orElseThrow(() -> new CustomNotFoundException("Bus not found with ID: " + request.getBusId()));
//        BusLocation location = busLocationRepository.findByBusId(request.getBusId())
//                .orElse(BusLocation.builder().bus(bus).build());
//        location.setLatitude(request.getLatitude());
//        location.setLongitude(request.getLongitude());
//        busLocationRepository.save(location);
//
//        // Broadcast update to subscribed clients
//        messagingTemplate.convertAndSend("/topic/bus/" + request.getBusId(), location);
//    }
//}