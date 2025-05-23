package examination.teacherAndStudents.service.serviceImpl;
import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.StudentManifestRequest;
import examination.teacherAndStudents.dto.StudentManifestResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.UnauthorizedException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.StudentManifestService;
import examination.teacherAndStudents.utils.ManifestStatus;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentManifestServiceImpl implements StudentManifestService {

    private static final Logger logger = LoggerFactory.getLogger(StudentManifestServiceImpl.class);

    private final StudentManifestRepository manifestRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TransportRepository busRepository;
    private final StudentTransportAllocationRepository studentTransportAllocationRepository;
    private final BusRouteRepository busRouteRepository;
    private final StudentTermRepository studentTermRepository;

    @Transactional
    @Override
    public StudentManifestResponse createOrUpdateManifest(StudentManifestRequest request) {

        String email = SecurityConfig.getAuthenticatedUserEmail();

        Profile driver = profileRepository.findByUserEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("driver not found "));

        Bus bus = busRepository.findByDriver(driver)
                .orElseThrow(() -> new CustomNotFoundException("Bus not found "));

        StudentManifestRequest.ManifestEntry entry = request.getManifestEntries();
        if (entry == null) {
            throw new IllegalArgumentException("Manifest entry cannot be null");
        }


        Profile student = profileRepository.findById(entry.getProfileId())
                .orElseThrow(() -> new CustomNotFoundException("Student profile not found"));

        StudentTransportAllocation transportAllocation = studentTransportAllocationRepository.findByProfileIdAndTransportBusId(student.getId(), bus.getBusId())
                .orElseThrow(() -> new CustomNotFoundException("Student not allocated to this bus"));
        Long id = transportAllocation.getSchool().getId();
        LocalDate date = LocalDate.now();

        StudentTerm studentTerm = studentTermRepository.findCurrentTerm(date,transportAllocation.getSchool().getId()).orElseThrow(() -> new CustomNotFoundException("Student term not found"));




        StudentManifest manifest = manifestRepository.existsByBusIdAndStudentProfileId(bus.getBusId(), entry.getProfileId())
                ? manifestRepository.findByBus(bus).stream()
                .filter(m -> m.getStudentProfile().getId().equals(entry.getProfileId()))
                .findFirst()
                .orElseThrow(() -> new CustomNotFoundException("Manifest not found"))
                : new StudentManifest();

        manifest.setStudentProfile(student);
        manifest.setBus(transportAllocation.getTransport());
        manifest.setRoute(transportAllocation.getRoute());
        manifest.setStatus(ManifestStatus.valueOf(entry.getStatus()));
        manifest.setPickupPerson(entry.getPickupPerson());
        manifest.setSchool(student.getUser().getSchool());
        manifest.setStudentTerm(studentTerm);
        manifest.setAcademicSession(studentTerm.getAcademicSession());

        StudentManifest savedManifest = manifestRepository.save(manifest);
        logger.debug("Created/Updated manifest ID: {} ", savedManifest.getId());
        return mapToResponse(savedManifest);
    }

    @Transactional(readOnly = true)
    @Override
    public StudentManifestResponse getManifestById(Long id) {
        User driver = verifyDriverAccess();
        logger.info("Driver {} fetching manifest ID: {}", driver.getEmail(), id);

        StudentManifest manifest = manifestRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Manifest not found with ID: " + id));

        return mapToResponse(manifest);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StudentManifestResponse> getManifestsByTripId(Long routeId, int page, int size, String sortBy, String sortDirection,
                                                              Long academicSessionId, Long studentTermId, Long profileId, String status) {
        User driver = verifyDriverAccess();
        logger.info("Driver with ID {} fetching manifests for route ID: {}", driver.getId(), routeId);

        // Validate pagination parameters
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero");
        }

        // Validate status if provided
        ManifestStatus manifestStatus = null;
        if (status != null) {
            try {
                manifestStatus = ManifestStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid manifest status: " + status);
            }
        }

        // Fetch route
        BusRoute route = busRouteRepository.findById(routeId)
                .orElseThrow(() -> new CustomNotFoundException("Route not found"));

        // Verify driver is authorized for this route
        Profile driverProfile = profileRepository.findByUserEmail(driver.getEmail())
                .orElseThrow(() -> new CustomNotFoundException("Driver profile not found"));
        Bus bus = busRepository.findByDriver(driverProfile)
                .orElseThrow(() -> new CustomNotFoundException("Bus not found for driver"));
        if (!bus.getBusRoute().getId().equals(routeId)) {
            throw new UnauthorizedException("Driver not authorized for route ID: " + routeId);
        }

        // Validate sortDirection
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDirection);
        }

        // Validate sortBy
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "id";
        }

        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<StudentManifest> manifests = manifestRepository.findByRouteAndFilters(
                route, academicSessionId, studentTermId, profileId, manifestStatus, pageable);
        return manifests.map(this::mapToResponse);
    }

    @Transactional
    @Override
    public void deleteManifest(Long id) {
        User driver = verifyDriverAccess();
        logger.info("Driver {} deleting manifest ID: {}", driver.getEmail(), id);
        StudentManifest manifest = manifestRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Manifest not found "));
        manifestRepository.delete(manifest);
        logger.debug("Deleted manifest ID: {}", id);
    }

    private User verifyDriverAccess() {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found"));
        if (!(user.getRoles().contains(Roles.DRIVER) || user.getRoles().contains(Roles.ADMIN))) {
            logger.warn("Unauthorized access attempt by user: {}", email);
            throw new UnauthorizedException("Access restricted to DRIVER role");
        }
        return user;
    }

    private StudentManifestResponse mapToResponse(StudentManifest manifest) {
        String studentName = manifest.getStudentProfile().getUser().getFirstName() + " " + manifest.getStudentProfile().getUser().getLastName();
        return StudentManifestResponse.builder()
                .id(manifest.getId())
                .profileId(manifest.getStudentProfile().getId())
                .studentName(studentName)
                .status(manifest.getStatus().name())
                .pickupPerson(manifest.getPickupPerson())
                .createdAt(manifest.getCreatedAt())
                .updatedAt(manifest.getUpdatedAt())
                .build();
    }
}