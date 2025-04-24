package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SubjectScheduleRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.TimetableService;
import examination.teacherAndStudents.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TimetableServiceImpl implements TimetableService {
    private static final Logger log = LoggerFactory.getLogger(TimetableServiceImpl.class);

    private final TimetableRepository timetableRepository;
    private final ClassLevelRepository classLevelRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final SubjectScheduleRepository subjectScheduleRepository;
    private final ClassBlockRepository classBlockRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final ProfileRepository profileRepository;

    public TimetableServiceImpl(
            TimetableRepository timetableRepository,
            ClassLevelRepository classLevelRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            SubjectScheduleRepository subjectScheduleRepository,
            ClassBlockRepository classBlockRepository,
            ClassSubjectRepository classSubjectRepository,
            AcademicSessionRepository academicSessionRepository,
            StudentTermRepository studentTermRepository,
            ProfileRepository profileRepository) {
        this.timetableRepository = timetableRepository;
        this.classLevelRepository = classLevelRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.subjectScheduleRepository = subjectScheduleRepository;
        this.classBlockRepository = classBlockRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.academicSessionRepository = academicSessionRepository;
        this.studentTermRepository = studentTermRepository;
        this.profileRepository = profileRepository;
    }

    private User validateAuthenticatedUser(boolean requireAdmin) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        if (email == null) {
            throw new CustomNotFoundException("No authenticated user found");
        }

        Set<Roles> allowedRoles = requireAdmin ? Set.of(Roles.ADMIN) : Set.of(Roles.ADMIN, Roles.TEACHER);
        User user = userRepository.findByEmailAndRolesIn(email, allowedRoles)
                .orElseThrow(() -> new CustomNotFoundException("Please login as an " + (requireAdmin ? "Admin" : "Admin or Teacher")));
        School school = user.getSchool();
        if (school == null) {
            throw new CustomInternalServerException("User is not associated with any school");
        }
        return user;
    }

    private void validateTimetableInputs(DayOfWeek dayOfWeek, List<SubjectScheduleRequest> subjectSchedules, TimetableType timetableType) {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Day of week must not be null");
        }
        if (timetableType == null) {
            throw new IllegalArgumentException("Timetable type must not be null");
        }
        if (subjectSchedules == null || subjectSchedules.isEmpty()) {
            throw new IllegalArgumentException("Subject schedules list cannot be null or empty");
        }
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "id";
        }
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDirection + ". Use 'ASC' or 'DESC'");
        }
        // Validate sortBy against known fields (optional)
        if (!List.of("id", "startTime", "endTime", "dayOfWeek").contains(sortBy)) {
            log.warn("Unknown sort field '{}', defaulting to 'id'", sortBy);
            sortBy = "id";
        }
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    @Transactional
    @Override
    public Timetable createTimetable(DayOfWeek dayOfWeek, List<SubjectScheduleRequest> subjectSchedules,
                                     TimetableType timetableType, Long termId, Long sessionId, Long classBlockId) {
        try {
            User admin = validateAuthenticatedUser(true);
            School school = admin.getSchool();
            validateTimetableInputs(dayOfWeek, subjectSchedules, timetableType);

            ClassBlock classBlock = classBlockRepository.findById(classBlockId)
                    .orElseThrow(() -> new CustomNotFoundException("Class block not found with ID: " + classBlockId));
            if (classBlock.getClassLevel() == null) {
                throw new CustomNotFoundException("Class level not found for class block ID: " + classBlockId);
            }
            classLevelRepository.findById(classBlock.getClassLevel().getId())
                    .orElseThrow(() -> new CustomNotFoundException("Class level not found with ID: " + classBlock.getClassLevel().getId()));

            StudentTerm studentTerm = studentTermRepository.findById(termId)
                    .orElseThrow(() -> new CustomNotFoundException("Student term not found with ID: " + termId));

            AcademicSession academicYear = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new CustomNotFoundException("Academic session not found with ID: " + sessionId));

            Timetable timetable = Timetable.builder()
                    .classBlock(classBlock)
                    .dayOfWeek(dayOfWeek)
                    .timetableType(timetableType)
                    .term(studentTerm)
                    .academicYear(academicYear)
                    .school(school)
                    .subjectSchedules(new ArrayList<>())
                    .build();

            List<SubjectSchedule> schedules = createSubjectSchedules(subjectSchedules, timetable);
            timetable.setSubjectSchedules(schedules); // JPA cascade handles persistence

            Timetable savedTimetable = timetableRepository.save(timetable);
            log.info("Created timetable ID {} for school ID {} on {}", savedTimetable.getId(), school.getId(), dayOfWeek);
            return savedTimetable;
        } catch (IllegalArgumentException | CustomNotFoundException e) {
            log.error("Error creating timetable: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating timetable: {}", e.getMessage(), e);
            throw new CustomInternalServerException("Failed to create timetable: " + e.getMessage());
        }
    }

    private List<SubjectSchedule> createSubjectSchedules(List<SubjectScheduleRequest> subjectSchedules, Timetable timetable) {
        List<SubjectSchedule> schedules = new ArrayList<>();

        for (SubjectScheduleRequest request : subjectSchedules) {
            if (request.getSubjectId() == null || request.getTeacherId() == null ||
                    request.getStartTime() == null || request.getEndTime() == null) {
                throw new IllegalArgumentException("Subject ID, teacher ID, start time, and end time must not be null");
            }
            if (!request.getEndTime().isAfter(request.getStartTime())) {
                throw new IllegalArgumentException("End time must be after start time for subject ID: " + request.getSubjectId());
            }

            ClassSubject classSubject = classSubjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new CustomNotFoundException("Class subject not found with ID: " + request.getSubjectId()));

            subjectRepository.findById(classSubject.getSubject().getId())
                    .orElseThrow(() -> new CustomNotFoundException("Subject not found with ID: " + classSubject.getSubject().getId()));

            Profile teacherProfile = profileRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new CustomNotFoundException("Teacher profile not found with ID: " + request.getTeacherId()));

            if (subjectScheduleRepository.existsByTimetableAndTeacherAndTimeOverlap(
                    timetable, teacherProfile, request.getStartTime(), request.getEndTime())) {
                throw new IllegalStateException("Teacher ID " + teacherProfile.getId() + " is already scheduled at this time");
            }

            SubjectSchedule schedule = SubjectSchedule.builder()
                    .timetable(timetable)
                    .subject(classSubject)
                    .teacher(teacherProfile)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .build();

            schedules.add(schedule);
        }
        return schedules;
    }

    @Transactional
    @Override
    public Timetable updateTimetable(Long timetableId, DayOfWeek dayOfWeek, List<SubjectScheduleRequest> subjectSchedules,
                                     Long termId, Long sessionId) {
        try {
            User admin = validateAuthenticatedUser(true);
            School school = admin.getSchool();
            validateTimetableInputs(dayOfWeek, subjectSchedules, null);

            Timetable timetable = timetableRepository.findByIdAndSchoolId(timetableId, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException("Timetable not found with ID: " + timetableId + " in school ID: " + school.getId()));

            StudentTerm studentTerm = studentTermRepository.findById(termId)
                    .orElseThrow(() -> new CustomNotFoundException("Student term not found with ID: " + termId));

            AcademicSession academicYear = academicSessionRepository.findById(sessionId)
                    .orElseThrow(() -> new CustomNotFoundException("Academic session not found with ID: " + sessionId));

            timetable.setDayOfWeek(dayOfWeek);
            timetable.setTerm(studentTerm);
            timetable.setAcademicYear(academicYear);

            List<SubjectSchedule> schedules = createSubjectSchedules(subjectSchedules, timetable);
            timetable.setSubjectSchedules(schedules); // Cascade handles deletion of old schedules

            Timetable updatedTimetable = timetableRepository.save(timetable);
            log.info("Updated timetable ID {} for school ID {}", timetableId, school.getId());
            return updatedTimetable;
        } catch (IllegalArgumentException | CustomNotFoundException e) {
            log.error("Error updating timetable ID {}: {}", timetableId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating timetable ID {}: {}", timetableId, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to update timetable ID " + timetableId + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Timetable> getAllTimetables(int page, int size, String sortBy, String sortDirection) {
        try {
            User user = validateAuthenticatedUser(false);
            School school = user.getSchool();

            Pageable pageable = createPageable(page, size, sortBy, sortDirection);
            Page<Timetable> timetables = timetableRepository.findAllBySchoolId(school.getId(), pageable);
            log.info("Fetched {} timetables for school ID {}", timetables.getTotalElements(), school.getId());
            return timetables;
        } catch (IllegalArgumentException | CustomNotFoundException e) {
            log.error("Error fetching timetables: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching timetables: {}", e.getMessage(), e);
            throw new CustomInternalServerException("Failed to fetch timetables: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public void deleteTimetable(Long timetableId) {
        try {
            User admin = validateAuthenticatedUser(true);
            School school = admin.getSchool();

            Timetable timetable = timetableRepository.findByIdAndSchoolId(timetableId, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException("Timetable not found with ID: " + timetableId + " in school ID: " + school.getId()));

            timetableRepository.delete(timetable);
            log.info("Deleted timetable ID {} for school ID {}", timetableId, school.getId());
        } catch (CustomNotFoundException e) {
            log.error("Error deleting timetable ID {}: {}", timetableId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error deleting timetable ID {}: {}", timetableId, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to delete timetable ID " + timetableId + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Timetable getTimetableById(Long timetableId) {
        try {
            User user = validateAuthenticatedUser(false);
            School school = user.getSchool();

            Timetable timetable = timetableRepository.findByIdAndSchoolId(timetableId, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException("Timetable not found with ID: " + timetableId + " in school ID: " + school.getId()));
            log.info("Fetched timetable ID {} for school ID {}", timetableId, school.getId());
            return timetable;
        } catch (CustomNotFoundException e) {
            log.error("Error fetching timetable ID {}: {}", timetableId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching timetable ID {}: {}", timetableId, e.getMessage(), e);
            throw new CustomInternalServerException("Failed to fetch timetable ID " + timetableId + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SubjectSchedule> getAllSubjectSchedules(int page, int size, String sortBy, String sortDirection) {
        try {
            User user = validateAuthenticatedUser(false);
            School school = user.getSchool();

            Pageable pageable = createPageable(page, size, sortBy, sortDirection);
            Page<SubjectSchedule> schedules = subjectScheduleRepository.findAllByTimetableSchoolId(school.getId(), pageable);
            log.info("Fetched {} subject schedules for school ID {}", schedules.getTotalElements(), school.getId());
            return schedules;
        } catch (IllegalArgumentException | CustomNotFoundException e) {
            log.error("Error fetching subject schedules: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching subject schedules: {}", e.getMessage(), e);
            throw new CustomInternalServerException("Failed to fetch subject schedules: " + e.getMessage());
        }
    }
}