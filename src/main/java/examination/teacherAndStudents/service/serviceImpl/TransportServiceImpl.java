package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.EmailDetailsToMultipleEmails;
import examination.teacherAndStudents.dto.TransportRequest;
import examination.teacherAndStudents.dto.TransportResponse;
import examination.teacherAndStudents.dto.TransportResponseTracker;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.PaymentService;
import examination.teacherAndStudents.service.TransportService;
import examination.teacherAndStudents.utils.AllocationStatus;
import examination.teacherAndStudents.utils.PaymentStatus;
import examination.teacherAndStudents.utils.Roles;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportServiceImpl implements TransportService {


    private final TransportRepository transportRepository;
    private final TransportTrackerRepository transportTrackerRepository;


    private final UserRepository userRepository;

    private final EmailService emailService;

    private final PaymentService paymentService;

    private final ProfileRepository profileRepository;
    private final BusRouteRepository busRouteRepository;
    private final StudentTransportTrackerRepository studentTransportTrackerRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final DuesRepository duesRepository;
    private final StudentTermRepository studentTermRepository;
    private final DuesPaymentRepository duePaymentRepository;

    @Override
    public TransportResponse createTransport(TransportRequest transportRequest) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            Optional<User> userDetails = userRepository.findByEmail(email);
            Optional<BusRoute> busRoute = busRouteRepository.findById(transportRequest.getBusRouteId());

            Profile driver = profileRepository.findById(transportRequest.getDriverId())
                    .orElseThrow(() -> new CustomInternalServerException("Driver not found with ID: " + transportRequest.getDriverId()));

            Transport transport = new Transport();
            transport.setVehicleNumber(transportRequest.getVehicleNumber());
            transport.setAvailable(true);
            transport.setVehicleName(transportRequest.getVehicleName());
            transport.setCapacity(transportRequest.getCapacity());
            transport.setDriver(driver);
            transport.setLicenceNumber(transportRequest.getLicenceNumber());
            transport.setSchool(userDetails.get().getSchool());
            transport.setBusRoute(busRoute.get());
            transport = transportRepository.save(transport);

            TransportTracker transportTracker = new TransportTracker();
            transportTracker.setTransport(transport);
            transportTracker.setRemainingCapacity(transport.getCapacity()); // Set initial capacity
            transportTrackerRepository.save(transportTracker);
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

            // Update TransportTracker if capacity changes
            TransportTracker tracker = transportTrackerRepository.findByTransport(transport)
                    .orElseThrow(() -> new CustomInternalServerException("Transport Tracker not found"));
            tracker.setRemainingCapacity(updatedTransport.getCapacity());
            transportTrackerRepository.save(tracker);


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


    @Transactional
    public StudentTransportAllocation payForTransport(Long dueId, Long sessionId, Long termId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User student = userRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomNotFoundException("Please login as a Student"));

            AcademicSession academicSession = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new EntityNotFoundException("Academic session not found with ID: " + sessionId));

            StudentTerm term = studentTermRepository.findById(termId)
                    .orElseThrow(() -> new EntityNotFoundException("student term not found with ID: " + termId));

            Profile profile = profileRepository.findByUser(student)
                    .orElseThrow(() -> new EntityNotFoundException("User profile not found"));

            DuePayment duePayment = duePaymentRepository
                    .findByDueIdAndAcademicYearAndStudentTermAndProfile(dueId,academicSession, term, profile);
            if(duePayment!= null) {
                throw new CustomInternalServerException("You have paid for transport for the month");
            }
            // Check if allocation already made
            StudentTransportAllocation existingTracker = studentTransportTrackerRepository
                    .findByDuesIdAndAcademicSessionAndTermAndProfile(dueId,academicSession, term, profile);

            if (existingTracker != null) {
                throw new EntityAlreadyExistException("Transport payment already made, please wait for transport allocation");
            }

            // Process payment
            paymentService.payDue(dueId, termId, academicSession.getId());

            // Save tracker entry after payment
            StudentTransportAllocation transportTracker = new StudentTransportAllocation();
            transportTracker.setPaymentStatus(PaymentStatus.SUCCESS);
            transportTracker.setTerm(term);
            transportTracker.setStatus(AllocationStatus.PENDING);
            transportTracker.setDues(duesRepository.findById(dueId).get());
            transportTracker.setAcademicSession(academicSession);
            transportTracker.setProfile(profile);

            studentTransportTrackerRepository.save(transportTracker);

            // Return the transport response
              return transportTracker;

        } catch (EntityNotFoundException e) {
            throw new NotFoundException("Transport payment not processed: " + e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException("Error processing transport payment: " + e.getMessage());
        }
    }




    @Override
    public TransportResponse addStudentToTransport(Long transportTrackerId, Long studentId, Long transportId,
                                                   Long academicYearId, Long termId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }
            // Find the transport by ID
            Transport transport = transportRepository.findById(transportId)
                    .orElseThrow(() -> new NotFoundException("Transport not found with ID: " + transportId));

            AcademicSession academicSession = academicSessionRepository.findById(academicYearId)
                    .orElseThrow(() -> new NotFoundException("Academic year not found with ID: " + academicYearId));

            StudentTerm studentTerm = studentTermRepository.findById(termId)
                    .orElseThrow(() -> new NotFoundException("Student term not found with ID: " + studentId));

            // Find the student by ID
            User user = userRepository.findById(studentId)
                    .orElseThrow(() -> new NotFoundException("Student not found with ID: " + studentId));

            // Find the student by ID
            Profile profile = profileRepository.findByUser(user)
                    .orElseThrow(() -> new NotFoundException("Student profile not found with ID: " + studentId));


            StudentTransportAllocation studentTransportAllocation = studentTransportTrackerRepository.findByIdAndAcademicSessionAndTermAndProfile(transportTrackerId,academicSession, studentTerm, profile)
                    .orElseThrow(() -> new CustomNotFoundException("Student Transport allocation not found OR fair not paid"));

            StudentTransportAllocation existingAllocation = studentTransportTrackerRepository.findByTransportAndProfileAndAcademicSessionAndStatus(transport, profile, academicSession, AllocationStatus.SUCCESS);
            if (existingAllocation != null) {
                throw new AttendanceAlreadyTakenException(
                        "Student has already been allocated transport");
            }
            if (transportTrackerId != null) {
                StudentTransportAllocation existingAllocation1 = studentTransportTrackerRepository.findById(transportTrackerId)
                        .orElseThrow(() -> new NotFoundException("Transport allocation not found"));

                if (!existingAllocation1.getProfile().equals(profile)) {
                    throw new NotFoundException("Allocation ID belongs to another student");
                }
                if (existingAllocation1.getPaymentStatus() != PaymentStatus.SUCCESS) {
                    throw new RuntimeException("Payment not made for the allocation or pending");
                }}

                // Check if a tracker exists for the transport, if not, create one
            TransportTracker tracker = transportTrackerRepository.findByTransport(transport)
                    .orElseGet(() -> {
                        TransportTracker newTracker = new TransportTracker();
                        newTracker.setTransport(transport);
                        newTracker.setRemainingCapacity(transport.getCapacity()); // Assuming capacity is part of the transport entity
                        return transportTrackerRepository.save(newTracker);
                    });

            // Check if already assigned
            boolean alreadyAssigned = studentTransportTrackerRepository.existsByProfileAndTransportAndStatus(
                    profile, transport, AllocationStatus.SUCCESS);

            if (alreadyAssigned) {
                throw new EntityAlreadyExistException("Student already allocated to this transport");
            }


            if (tracker.getRemainingCapacity() <= 0) {
                throw new IllegalStateException("No available capacity in transport");
            }

            Profile student = profileRepository.findByUser(user)
                    .orElseThrow(() -> new NotFoundException("Student Profile not found with ID: " + studentId));

            // Check if student is already added
            boolean alreadyAdded = studentTransportTrackerRepository.existsByProfileAndTransportAndStatus(
                    student, transport, AllocationStatus.SUCCESS);

            if (alreadyAdded) {
                throw new EntityNotFoundException("Student with ID " + studentId + " is already assigned to this transport");
            }

            // Set the transport for the student
            student.setTransport(transport);

            // Save the updated student
            profileRepository.save(student);

            // Create StudentTransportTracker entry for added student
            studentTransportAllocation.setTransport(transport);
            studentTransportAllocation.setStatus(AllocationStatus.SUCCESS);
            studentTransportTrackerRepository.save(studentTransportAllocation );

            tracker.assignStudent();
            transportTrackerRepository.save(tracker);

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


            TransportTracker tracker = transportTrackerRepository.findByTransport(transport)
                    .orElseThrow(() -> new CustomNotFoundException("Transport Tracker not found"));

            List<Profile> students = new ArrayList<>();

            // Find each student by ID and add them to the transport
            for (Long studentId : studentIds) {
                if (tracker.getRemainingCapacity() <= 0) {
                    throw new IllegalStateException("No available capacity in transport");
                }
                Profile student = profileRepository.findByUserId(studentId)
                        .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: " + studentId));

                // Check if student is already added
                boolean alreadyAdded = studentTransportTrackerRepository.existsByProfileAndTransportAndStatus(
                        student, transport,AllocationStatus.SUCCESS);

                if (alreadyAdded) {
                    throw new EntityAlreadyExistException("Student with ID " + studentId + " is already assigned to this transport");
                }

                // Set the transport for the student
                student.setTransport(transport);

                // Create StudentTransportTracker entry for added student
                StudentTransportAllocation transportTracker = new StudentTransportAllocation();
                transportTracker.setTransport(transport);
                transportTracker.setProfile(student);
                transportTracker.setStatus(AllocationStatus.SUCCESS);
                studentTransportTrackerRepository.save(transportTracker );

                // Add student to the list
                tracker.assignStudent();
                students.add(student);
            }

            // Save the list of students
            profileRepository.saveAll(students);

            transportTrackerRepository.save(tracker);

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

    @Override
    public TransportResponse removeStudentFromTransport(Long transportId, Long studentId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
        if (admin == null) {
            throw new CustomNotFoundException("Please login as an Admin");
        }

        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + transportId));

        TransportTracker tracker = transportTrackerRepository.findByTransport(transport)
                .orElseThrow(() -> new CustomNotFoundException("Transport Tracker not found"));

        Profile student = profileRepository.findById(studentId)
                .orElseThrow(() -> new CustomNotFoundException("Student Profile not found with ID: " + studentId));

        // Check if the student has already been removed
        boolean alreadyRemoved = studentTransportTrackerRepository.existsByProfileAndTransportAndStatus(
                student, transport, AllocationStatus.REMOVED);

        if (alreadyRemoved) {
            throw new EntityAlreadyExistException("Student with ID " + studentId + " has already been removed from this transport");
        }

        if (!student.getTransport().equals(transport)) {
            throw new EntityAlreadyExistException("Student is not assigned to this transport");
        }

        // Unassign the student from transport
        student.setTransport(null);
        profileRepository.save(student);

        // Update the transport tracker
        tracker.removeStudent();
        transportTrackerRepository.save(tracker);

        // Create StudentTransportTracker entry for added student
        StudentTransportAllocation transportTracker = new StudentTransportAllocation();
        transportTracker.setTransport(transport);
        transportTracker.setProfile(student);
        transportTracker.setStatus(AllocationStatus.REMOVED);
        studentTransportTrackerRepository.save(transportTracker );

        return mapToTransportResponse(transport);
    }



    private TransportResponse mapToTransportResponse(Transport transport) {
        TransportResponse transportResponse = new TransportResponse();
        transportResponse.setId(transport.getId());
        transportResponse.setVehicleName(transport.getVehicleName());
        transportResponse.setVehicleNumber(transport.getVehicleNumber());
        transportResponse.setLicenceNumber(transport.getLicenceNumber());
        return transportResponse;
    }

//    private TransportResponse mapToTransportResponseTracker(StudentTransportAllocation
//                                                                    transport) {
//        TransportResponseTracker transportResponse = new TransportResponse();
//        transportResponse.setId(transport.getId());
//        transportResponse.setVehicleName(transport.());
//        transportResponse.setVehicleNumber(transport.getVehicleNumber());
//        transportResponse.setLicenceNumber(transport.getLicenceNumber());
//        return transportResponse;
//    }
}
