package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.EmailService;
import examination.teacherAndStudents.service.FeePaymentService;
import examination.teacherAndStudents.service.TransportService;
import examination.teacherAndStudents.utils.AllocationStatus;
import examination.teacherAndStudents.utils.PaymentStatus;
import examination.teacherAndStudents.utils.Roles;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransportServiceImpl implements TransportService {

    private final TransportRepository transportRepository;
    private final TransportTrackerRepository transportTrackerRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final FeePaymentService paymentService;
    private final ProfileRepository profileRepository;
    private final BusRouteRepository busRouteRepository;
    private final StudentTransportTrackerRepository studentTransportTrackerRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final FeeRepository feeRepository;
    private final PaymentRepository paymentRepository;
    private final StopRepository stopRepository;

    @Override
    @Transactional
    public TransportResponse createTransport(TransportRequest transportRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        // Validate route
        BusRoute busRoute = busRouteRepository.findById(transportRequest.getBusRouteId())
                .orElseThrow(() -> new CustomNotFoundException("BusRoute not found with ID: " + transportRequest.getBusRouteId()));

        Profile driver = null;
        if (transportRequest.getDriverId() != null) {
            driver = profileRepository.findById(transportRequest.getDriverId())
                    .orElseThrow(() -> new CustomNotFoundException("Driver not found with ID: " + transportRequest.getDriverId()));
        }

        Profile user = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Profile not found"));

        Bus transport = Bus.builder()
                .vehicleNumber(transportRequest.getVehicleNumber())
                .available(true)
                .vehicleName(transportRequest.getVehicleName())
                .capacity(transportRequest.getCapacity())
                .driver(driver)
                .licenceNumber(transportRequest.getLicenceNumber())
                .school(user.getUser().getSchool())
                .busRoute(busRoute)
                .build();

        busRoute.addBus(transport);
        transport = transportRepository.save(transport);

        // Initialize TransportTracker
        TransportTracker transportTracker = TransportTracker.builder()
                .bus(transport)
                .busRoute(busRoute)
                .remainingCapacity(transport.getCapacity())
                .session(academicSessionRepository.findCurrentSession(transport.getSchool().getId())
                        .orElseThrow(() -> new CustomNotFoundException("No active academic session")))
                .term(studentTermRepository.findCurrentTerm(LocalDate.now())
                        .orElseThrow(() -> new CustomNotFoundException("No active term")))
                .build();
        transportTrackerRepository.save(transportTracker);

        return mapToTransportResponse(transport);
    }

    @Override
    @Transactional
    public TransportResponse updateTransport(Long transportId, TransportRequest updatedTransport) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        Bus transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + transportId));

        if (updatedTransport.getVehicleNumber() != null) {
            transport.setVehicleNumber(updatedTransport.getVehicleNumber());
        }
        if (updatedTransport.getVehicleName() != null) {
            transport.setVehicleName(updatedTransport.getVehicleName());
        }
        if (updatedTransport.getLicenceNumber() != null) {
            transport.setLicenceNumber(updatedTransport.getLicenceNumber());
        }
        if (updatedTransport.getDriverId() != null) {
            Profile driver = profileRepository.findById(updatedTransport.getDriverId())
                    .orElseThrow(() -> new CustomNotFoundException("Driver not found with ID: " + updatedTransport.getDriverId()));
            transport.setDriver(driver);
        }
        if (updatedTransport.getBusRouteId() != null) {
            BusRoute newRoute = busRouteRepository.findById(updatedTransport.getBusRouteId())
                    .orElseThrow(() -> new CustomNotFoundException("BusRoute not found with ID: " + updatedTransport.getBusRouteId()));
            if (transport.getBusRoute() != null) {
                transport.getBusRoute().removeBus(transport);
            }
            newRoute.addBus(transport);
            transport.setBusRoute(newRoute);
        }
        if (updatedTransport.getCapacity() > 0) {
            TransportTracker tracker = transportTrackerRepository.findByBusAndSessionAndTerm(
                            transport,
                            academicSessionRepository.findCurrentSession(admin.getSchool().getId())
                                    .orElseThrow(() -> new CustomNotFoundException("No active academic session")),
                            studentTermRepository.findCurrentTerm(LocalDate.now())
                                    .orElseThrow(() -> new CustomNotFoundException("No active term")))
                    .orElseThrow(() -> new CustomNotFoundException("Transport Tracker not found"));

            int currentUsage = transport.getCapacity() - tracker.getRemainingCapacity();
            int newCapacity = updatedTransport.getCapacity();
            if (newCapacity < currentUsage) {
                throw new CustomInternalServerException("New capacity cannot be less than current usage");
            }
            transport.setCapacity(newCapacity);
            tracker.setRemainingCapacity(newCapacity - currentUsage);
            transportTrackerRepository.save(tracker);
        }

        transport = transportRepository.save(transport);
        return mapToTransportResponse(transport);
    }

    @Override
    @Transactional
    public void deleteTransport(Long transportId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        Bus transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + transportId));

        if (studentTransportTrackerRepository.existsByTransport(transport)) {
            throw new IllegalStateException("Cannot delete transport with active student assignments");
        }

        if (transport.getBusRoute() != null) {
            transport.getBusRoute().removeBus(transport);
        }

        transportTrackerRepository.deleteByBus(transport);
        transportRepository.delete(transport);
    }

    @Override
    public Page<TransportResponse> getAllTransports(
            Long id,
            String vehicleNumber,
            String licenceNumber,
            Long driverId,
            Boolean available,
            int page,
            int size,
            String sortBy,
            String sortDirection) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        School school = admin.getSchool();
        if (school == null) {
            throw new CustomInternalServerException("Admin is not associated with any school");
        }

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Bus> transportsPage = transportRepository.findAllBySchoolWithFilters(
                school.getId(),
                id,
                vehicleNumber,
                licenceNumber,
                driverId,
                available,
                pageable);

        return transportsPage.map(this::mapToTransportResponse);
    }

    @Override
    public TransportResponse getTransportById(Long transportId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        School school = admin.getSchool();
        if (school == null) {
            throw new CustomInternalServerException("Admin is not associated with any school");
        }

        Bus transport = transportRepository.findByBusIdAndSchoolId(transportId, school.getId())
                .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + transportId));

        return mapToTransportResponse(transport);
    }

    @Override
    @Transactional
    public TransportAllocationResponse payForTransport(TransportPaymentRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("Please login as a Student"));

        AcademicSession academicSession = academicSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new CustomNotFoundException("Academic session not found"));

        StudentTerm term = studentTermRepository.findById(request.getTermId())
                .orElseThrow(() -> new CustomNotFoundException("Term not found"));

        BusRoute route = busRouteRepository.findById(request.getRouteId())
                .orElseThrow(() -> new CustomNotFoundException("Route not found"));

        Stop stop = stopRepository.findById(request.getStopId())
                .orElseThrow(() -> new CustomNotFoundException("Stop not found"));

        if (!stop.getRoute().getId().equals(route.getId())) {
            throw new IllegalArgumentException("Selected stop does not belong to the chosen route");
        }

        Profile profile = profileRepository.findByUser(student)
                .orElseThrow(() -> new CustomNotFoundException("User profile not found"));

        Fee fee = feeRepository.findById(request.getFeeId())
                .orElseThrow(() -> new CustomNotFoundException("Fee not found"));

        if (paymentRepository.existsByStudentFeeAndProfileAndAcademicSessionAndStudentTerm(fee, profile, academicSession, term)) {
            throw new EntityAlreadyExistException("Transport payment already made for this term");
        }

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .feeId(request.getFeeId())
                .amount(fee.getAmount())
                .build();

            paymentService.processPayment(paymentRequest);

        Payment payment = paymentRepository.findPaymentsForSessionAndTerm(fee.getId(), academicSession,profile, term)
                .orElseThrow(() -> new CustomInternalServerException("Payment record not created"));

        StudentTransportAllocation allocation = StudentTransportAllocation.builder()
                .profile(profile)
                .payment(payment)
                .paymentStatus(PaymentStatus.SUCCESS)
                .route(route)
                .stop(stop)
                .academicSession(academicSession)
                .term(term)
                .status(AllocationStatus.PENDING)
                .build();

        StudentTransportAllocation savedAllocation = studentTransportTrackerRepository.save(allocation);

        return mapToAllocationResponse(savedAllocation);
    }

    @Override
    @Transactional
    public TransportAllocationResponse assignTransportToStudent(AddStudentToTransportRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        StudentTransportAllocation allocation = studentTransportTrackerRepository.findById(request.getTransportAllocationId())
                .orElseThrow(() -> new CustomNotFoundException("Transport allocation not found with ID: " + request.getTransportAllocationId()));

        if (allocation.getStatus() == AllocationStatus.SUCCESS) {
            throw new EntityAlreadyExistException("Student already has transport assigned");
        }

        if (allocation.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Payment not completed for this allocation");
        }

        // Find available buses for the route
        List<TransportTracker> trackers = transportTrackerRepository.findByBusRouteAndSessionAndTermAndRemainingCapacityGreaterThan(
                allocation.getRoute(),
                allocation.getAcademicSession(),
                allocation.getTerm(),
                0);

        if (trackers.isEmpty()) {
            throw new IllegalStateException("No available buses with capacity for this route. Please add a new bus.");
        }

        // Select tracker with most capacity
        TransportTracker tracker = trackers.stream()
                .max(Comparator.comparingInt(TransportTracker::getRemainingCapacity))
                .orElseThrow(() -> new IllegalStateException("Error selecting bus"));

        Bus transport = tracker.getBus();
        allocation.setTransport(transport);
        allocation.setStatus(AllocationStatus.SUCCESS);
        studentTransportTrackerRepository.save(allocation);

        Profile profile = allocation.getProfile();
        profile.setTransport(transport);
        profileRepository.save(profile);

        tracker.assignStudent();
        transportTrackerRepository.save(tracker);

        return mapToAllocationResponse(allocation);
    }

    @Override
    @Transactional
    public TransportResponse addStudentsToTransport(Long transportId, List<Long> studentIds) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        Bus transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + transportId));

        TransportTracker tracker = transportTrackerRepository.findByBusAndSessionAndTerm(
                        transport,
                        academicSessionRepository.findCurrentSession(admin.getSchool().getId())
                                .orElseThrow(() -> new CustomNotFoundException("No active academic session")),
                        studentTermRepository.findCurrentTerm(LocalDate.now())
                                .orElseThrow(() -> new CustomNotFoundException("No active term")))
                .orElseThrow(() -> new CustomNotFoundException("Transport Tracker not found"));

        List<Profile> students = new ArrayList<>();

        for (Long studentId : studentIds) {
            if (tracker.getRemainingCapacity() <= 0) {
                throw new IllegalStateException("No available capacity in transport");
            }

            Profile student = profileRepository.findByUserId(studentId)
                    .orElseThrow(() -> new CustomNotFoundException("Student not found with ID: " + studentId));

            boolean alreadyAdded = studentTransportTrackerRepository.existsByProfileAndTransportAndStatus(
                    student, transport, AllocationStatus.SUCCESS);

            if (alreadyAdded) {
                throw new EntityAlreadyExistException("Student with ID " + studentId + " is already assigned to this transport");
            }

            student.setTransport(transport);

            StudentTransportAllocation allocation = StudentTransportAllocation.builder()
                    .profile(student)
                    .transport(transport)
                    .route(transport.getBusRoute())
                    .stop(studentTransportTrackerRepository.findByProfile(student)
                            .map(StudentTransportAllocation::getStop)
                            .orElseThrow(() -> new CustomNotFoundException("No stop defined for student")))
                    .payment(studentTransportTrackerRepository.findByProfile(student)
                            .map(StudentTransportAllocation::getPayment)
                            .orElseThrow(() -> new CustomNotFoundException("No payment found for student")))
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .status(AllocationStatus.SUCCESS)
                    .academicSession(academicSessionRepository.findCurrentSession(admin.getSchool().getId())
                            .orElseThrow(() -> new CustomNotFoundException("No active academic session")))
                    .term(studentTermRepository.findCurrentTerm(LocalDate.now())
                            .orElseThrow(() -> new CustomNotFoundException("No active term")))
                    .build();

            studentTransportTrackerRepository.save(allocation);
            tracker.assignStudent();
            students.add(student);
        }

        profileRepository.saveAll(students);
        transportTrackerRepository.save(tracker);

        return mapToTransportResponse(transport);
    }

    @Override
    @Transactional
    public TransportAllocationResponse removeStudentFromTransport(Long transportId, Long studentId) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        Bus transport = transportRepository.findById(transportId)
                .orElseThrow(() -> new CustomNotFoundException("Transport not found with ID: " + transportId));

        Profile student = profileRepository.findById(studentId)
                .orElseThrow(() -> new CustomNotFoundException("Student Profile not found with ID: " + studentId));

        StudentTransportAllocation allocation = studentTransportTrackerRepository
                .findByProfileAndTransportAndStatus(student, transport, AllocationStatus.SUCCESS)
                .orElseThrow(() -> new CustomNotFoundException("Student is not assigned to this transport"));

        TransportTracker tracker = transportTrackerRepository
                .findByBusAndSessionAndTerm(transport, allocation.getAcademicSession(), allocation.getTerm())
                .orElseThrow(() -> new CustomNotFoundException("Transport Tracker not found"));

        student.setTransport(null);
        profileRepository.save(student);

        tracker.removeStudent();
        transportTrackerRepository.save(tracker);

        allocation.setStatus(AllocationStatus.REMOVED);
        allocation.setUpdatedDate(LocalDateTime.now());
        studentTransportTrackerRepository.save(allocation);

        return mapToAllocationResponse(allocation);
    }

    @Override
    @Transactional
    public TransportResponse addBusToRoute(AddBusToRouteRequest request) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User admin = userRepository.findByEmailAndRole(email, Roles.ADMIN)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an Admin"));

        // Validate route
        BusRoute busRoute = busRouteRepository.findById(request.getRouteId())
                .orElseThrow(() -> new CustomNotFoundException("BusRoute not found with ID: " + request.getRouteId()));

        // Validate driver
        Profile driver = profileRepository.findById(request.getDriverId())
                .orElseThrow(() -> new CustomNotFoundException("Driver not found with ID: " + request.getDriverId()));

        // Validate capacity
        if (request.getCapacity() <= 0) {
            throw new IllegalArgumentException("Bus capacity must be greater than zero");
        }

        // Get school from admin
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found"));
        School school = user.getSchool();
        if (school == null) {
            throw new CustomInternalServerException("Admin is not associated with any school");
        }

        // Create new bus
        Bus bus = Bus.builder()
                .vehicleNumber(request.getVehicleNumber())
                .vehicleName(request.getVehicleName())
                .licenceNumber(request.getLicenceNumber())
                .capacity(request.getCapacity())
                .available(true)
                .driver(driver)
                .busRoute(busRoute)
                .school(school)
                .build();

        // Associate bus with route
        busRoute.addBus(bus);
        bus = transportRepository.save(bus);

        // Initialize TransportTracker for the bus
        AcademicSession currentSession = academicSessionRepository.findCurrentSession(bus.getSchool().getId())
                .orElseThrow(() -> new CustomNotFoundException("No active academic session"));
        StudentTerm currentTerm = studentTermRepository.findCurrentTerm(LocalDate.now())
                .orElseThrow(() -> new CustomNotFoundException("No active term"));

        TransportTracker transportTracker = TransportTracker.builder()
                .bus(bus)
                .busRoute(busRoute)
                .session(currentSession)
                .term(currentTerm)
                .remainingCapacity(bus.getCapacity())
                .build();
        transportTrackerRepository.save(transportTracker);

        return mapToTransportResponse(bus);
    }

    public void sendEmailToStudents(String busRoute, Set<User> students) throws MessagingException {
        for (User student : students) {
            Map<String, Object> model = new HashMap<>();
            model.put("name", student.getFirstName() + " " + student.getLastName());
            model.put("busRoute", busRoute);

            EmailDetailsToMultipleEmails emailDetails = EmailDetailsToMultipleEmails.builder()
                    .toEmails(Collections.singletonList(student.getEmail()))
                    .subject("Bus Breakdown Update")
                    .templateName("email-template-bus-breakdown")
                    .model(model)
                    .build();

            emailService.sendToMultipleEmails(emailDetails);
        }
    }

    private TransportResponse mapToTransportResponse(Bus transport) {
        return TransportResponse.builder()
                .id(transport.getBusId())
                .routeName(transport.getBusRoute().getRouteName())
                .vehicleName(transport.getVehicleName())
                .vehicleNumber(transport.getVehicleNumber())
                .licenceNumber(transport.getLicenceNumber())
                .busRouteId(transport.getBusRoute() != null ? transport.getBusRoute().getId() : null)
                .build();
    }

    private TransportAllocationResponse mapToAllocationResponse(StudentTransportAllocation allocation) {
        return TransportAllocationResponse.builder()
                .allocationId(allocation.getId())
                .studentId(allocation.getProfile().getId())
                .studentName(allocation.getProfile().getUser().getFirstName() + " " +
                        allocation.getProfile().getUser().getLastName())
                .routeId(allocation.getRoute().getId())
                .routeName(allocation.getRoute().getRouteName())
                .stopId(allocation.getStop().getStopId())
                .stopName(allocation.getStop().getStopName())
                .transportId(allocation.getTransport() != null ? allocation.getTransport().getBusId() : null)
                .transportName(allocation.getTransport() != null ? allocation.getTransport().getVehicleName() : null)
                .paymentStatus(allocation.getPaymentStatus())
                .allocationStatus(allocation.getStatus())
                .createdDate(allocation.getCreatedDate())
                .build();
    }
}