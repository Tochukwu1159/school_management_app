package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.EmailDetailsToMultipleEmails;
import examination.teacherAndStudents.dto.TransportRequest;
import examination.teacherAndStudents.dto.TransportResponse;
import examination.teacherAndStudents.dto.UserRequestDto;
import examination.teacherAndStudents.entity.BusRoute;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Transport;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.BusRouteRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.TransportRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.TransportService;
import examination.teacherAndStudents.utils.Roles;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportServiceImpl implements TransportService {


    private final TransportRepository transportRepository;


    private final UserRepository userRepository;

    private final EmailService emailService;

    private final ProfileRepository profileRepository;
    private final BusRouteRepository busRouteRepository;

    @Override
    public TransportResponse createTransport(TransportRequest transportRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }
            Optional<BusRoute> busRoute = busRouteRepository.findById(transportRequest.getBusRouteId());

            Optional<Profile> driver = profileRepository.findById(transportRequest.getDriverId());

            Transport transport = new Transport();
            transport.setVehicleNumber(transportRequest.getVehicleNumber());
            transport.setAvailable(true);
            transport.setVehicleName(transportRequest.getVehicleName());
            transport.setCapacity(transportRequest.getCapacity());
            transport.setDriver(driver.get());
            transport.setLicenceNumber(transportRequest.getLicenceNumber());
            transport.setBusRoute(busRoute.get());
            transport = transportRepository.save(transport);
            return mapToTransportResponse(transport);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error creating transport: " + e.getMessage());
        }
    }

    @Override
    public TransportResponse updateTransport(Long transportId, TransportRequest updatedTransport) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            Transport transport = transportRepository.findById(transportId)
                    .orElseThrow(() -> new CustomInternalServerException("Transport not found with ID: " + transportId));


            transport.setVehicleNumber(updatedTransport.getVehicleNumber());
            transport.setCapacity(updatedTransport.getCapacity());
            transport.setLicenceNumber(updatedTransport.getLicenceNumber());
            transport = transportRepository.save(transport);
            return mapToTransportResponse(transport);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error updating transport: " + e.getMessage());
        }
    }

    @Override
    public void deleteTransport(Long transportId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            Transport transport = transportRepository.findById(transportId)
                    .orElseThrow(() -> new CustomInternalServerException("Transport not found with ID: " + transportId));
            transportRepository.delete(transport);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error deleting transport: " + e.getMessage());
        }
    }

    @Override
    public List<TransportResponse> getAllTransports() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            List<Transport> transports = transportRepository.findAll();
            return transports.stream()
                    .map(this::mapToTransportResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new CustomInternalServerException("Unexpected error fetching all transports " + e.getMessage());
        }
    }

    @Override
    public TransportResponse getTransportById(Long transportId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            Transport transport = transportRepository.findById(transportId)
                    .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + transportId));

            return mapToTransportResponse(transport);
        } catch (Exception e) {
            throw new CustomInternalServerException("Unexpected error fetching transport by ID " + e.getMessage());
        }
    }

    @Override
    public TransportResponse addStudentToTransport(Long transportId, Long studentId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            // Find the transport by ID
            Transport transport = transportRepository.findById(transportId)
                    .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + transportId));

            // Find the student by ID
            User user = userRepository.findById(studentId)
                    .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: " + studentId));

            Profile student = profileRepository.findById(studentId)
                    .orElseThrow(() -> new CustomNotFoundException("Student Profile not found with ID: " + studentId));

            // Set the transport for the student
            student.setTransport(transport);

            // Save the updated student
            profileRepository.save(student);

            // Return the transport response
            return mapToTransportResponse(transport);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error adding student to transport: " + e.getMessage());
        }
    }

    @Override
    public TransportResponse addStudentsToTransport(Long transportId, List<Long> studentIds) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            // Find the transport by ID
            Transport transport = transportRepository.findById(transportId)
                    .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + transportId));

            List<Profile> students = new ArrayList<>();

            // Find each student by ID and add them to the transport
            for (Long studentId : studentIds) {
                Profile student = profileRepository.findByUserId(studentId)
                        .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: " + studentId));

                // Set the transport for the student
                student.setTransport(transport);

                // Add student to the list
                students.add(student);
            }

            // Save the list of students
            profileRepository.saveAll(students);

            // Add students to the transport
            transport.getUserProfiles().addAll(students);

            // Save the updated transport
            transport = transportRepository.save(transport);

            // Return the transport response
            return mapToTransportResponse(transport);
        } catch (Exception e) {
            throw new CustomInternalServerException("Error adding students to transport: " + e.getMessage());
        }
    }

    public void sendEmailToStudents(String busRoute, Set<User> students) throws MessagingException {
        if (students == null || students.isEmpty()) {
            return;
        }

        for (User student : students) {
            Map<String, Object> model = new HashMap<>();
            model.put("name", student.getFirstName() + " " + student.getLastName());
            model.put("busRoute", busRoute);

            // Build the email details for each student
            EmailDetailsToMultipleEmails emailDetailsToMultipleEmails = EmailDetailsToMultipleEmails.builder()
                    .toEmails(Collections.singletonList(student.getEmail())) // Use a singleton list for each student
                    .subject("Bus Breakdown Update")
                    .templateName("email-template-bus-breakdown")
                    .model(model)
                    .build();

            // Send the email for each student
            emailService.sendToMultipleEmails(emailDetailsToMultipleEmails);
        }
    }


    private TransportResponse mapToTransportResponse(Transport transport) {
        TransportResponse transportResponse = new TransportResponse();
        transportResponse.setId(transport.getId());
        transportResponse.setVehicleName(transport.getVehicleName());
        transportResponse.setVehicleNumber(transport.getVehicleNumber());
        transportResponse.setLicenceNumber(transport.getLicenceNumber());
        return transportResponse;
    }
}
