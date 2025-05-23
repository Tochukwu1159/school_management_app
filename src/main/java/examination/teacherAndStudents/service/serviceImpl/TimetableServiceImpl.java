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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    private void validateTimetableInputs(DayOfWeek dayOfWeek, List<SubjectScheduleRequest> subjectSchedules) {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Day of week must not be null");
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
        if (!List.of("id", "startTime", "endTime", "dayOfWeek").contains(sortBy)) {
            log.warn("Unknown sort field '{}', defaulting to 'id'", sortBy);
            sortBy = "id";
        }
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    @Transactional
    @Override
    public Timetable createTimetable(DayOfWeek dayOfWeek, List<SubjectScheduleRequest> subjectSchedules, Long termId, Long sessionId, Long classBlockId) {
        try {
            User admin = validateAuthenticatedUser(true);
            School school = admin.getSchool();
            validateTimetableInputs(dayOfWeek, subjectSchedules);

            ClassBlock classBlock = classBlockRepository.findByIdAndSchoolId(classBlockId, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException("Class block not found with ID: " + classBlockId));
            if (classBlock.getClassLevel() == null) {
                throw new CustomNotFoundException("Class level not found for class block ID: " + classBlockId);
            }
            classLevelRepository.findById(classBlock.getClassLevel().getId())
                    .orElseThrow(() -> new CustomNotFoundException("Class level not found with ID: " + classBlock.getClassLevel().getId()));

            StudentTerm studentTerm = studentTermRepository.findByIdAndAcademicSessionId(termId, sessionId)
                    .orElseThrow(() -> new CustomNotFoundException("Student term not found with ID: " + termId));

            if (timetableRepository.existsByClassBlockIdAndTermIdAndDayOfWeek(classBlockId, termId, dayOfWeek)) {
                throw new IllegalStateException(dayOfWeek + " already exists for this class block and term. Please update the existing timetable.");
            }

            validateScheduleTimeOverlaps(subjectSchedules, classBlockId, termId, dayOfWeek);


            Timetable timetable = Timetable.builder()
                    .classBlock(classBlock)
                    .dayOfWeek(dayOfWeek)
                    .term(studentTerm)
                    .academicYear(studentTerm.getAcademicSession())
                    .school(school)
                    .subjectSchedules(new ArrayList<>())
                    .build();

            timetable = timetableRepository.save(timetable);

            List<SubjectSchedule> schedules = createSubjectSchedules(subjectSchedules, timetable, dayOfWeek, classBlock.getId(), school.getId() );
            timetable.getSubjectSchedules().clear();
            timetable.getSubjectSchedules().addAll(schedules);

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


    private void validateScheduleTimeOverlaps(List<SubjectScheduleRequest> subjectSchedules, Long classBlockId, Long termId, DayOfWeek dayOfWeek) {
        // Check for overlaps within the payload
        for (int i = 0; i < subjectSchedules.size(); i++) {
            SubjectScheduleRequest current = subjectSchedules.get(i);
            LocalTime currentStart = parseTime(current.getStartTime());
            LocalTime currentEnd = parseTime(current.getEndTime());

            if (!currentEnd.isAfter(currentStart)) {
                throw new IllegalArgumentException("End time must be after start time for schedule starting at: " + current.getStartTime());
            }

            for (int j = i + 1; j < subjectSchedules.size(); j++) {
                SubjectScheduleRequest other = subjectSchedules.get(j);
                LocalTime otherStart = parseTime(other.getStartTime());
                LocalTime otherEnd = parseTime(other.getEndTime());

                if (currentStart.isBefore(otherEnd) && currentEnd.isAfter(otherStart)) {
                    throw new IllegalArgumentException("Time overlap detected in payload between " +
                            current.getStartTime() + "-" + current.getEndTime() + " and " +
                            other.getStartTime() + "-" + other.getEndTime());
                }
            }
        }

        // Check for overlaps with existing schedules in the database
        List<SubjectSchedule> existingSchedules = subjectScheduleRepository.findByClassBlockIdAndTermIdAndDayOfWeek(classBlockId, termId, dayOfWeek);
        for (SubjectScheduleRequest request : subjectSchedules) {
            LocalTime newStart = parseTime(request.getStartTime());
            LocalTime newEnd = parseTime(request.getEndTime());

            for (SubjectSchedule existing : existingSchedules) {
                LocalTime existingStart = LocalTime.parse(existing.getStartTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
                LocalTime existingEnd = LocalTime.parse(existing.getEndTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));

                if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                    throw new IllegalStateException("Time slot " + request.getStartTime() + " to " + request.getEndTime() +
                            " overlaps with existing schedule " + existing.getStartTime() + " to " + existing.getEndTime() +
                            " on " + dayOfWeek);
                }
            }
        }
    }

    private List<SubjectSchedule> createSubjectSchedules(List<SubjectScheduleRequest> subjectSchedules, Timetable timetable, DayOfWeek dayOfWeek, Long classBlockId, Long schoolId) {
        List<SubjectSchedule> schedules = new ArrayList<>();

        for (SubjectScheduleRequest request : subjectSchedules) {
            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new IllegalArgumentException("Start time and end time must not be null");
            }

            LocalTime startTime = parseTime(request.getStartTime());
            LocalTime endTime = parseTime(request.getEndTime());

            if (!endTime.isAfter(startTime)) {
                throw new IllegalArgumentException("End time must be after start time");
            }

            String normalizedStartTime = normalizeTimeFormat(startTime);
            String normalizedEndTime = normalizeTimeFormat(endTime);

            if (hasTimeOverlap(timetable, normalizedStartTime, normalizedEndTime, dayOfWeek)) {
                throw new IllegalStateException("Time slot " + normalizedStartTime + " to " + normalizedEndTime + " on " + dayOfWeek + " is already occupied");
            }

            SubjectSchedule.SubjectScheduleBuilder scheduleBuilder = SubjectSchedule.builder()
                    .timetable(timetable)
                    .startTime(normalizedStartTime)
                    .endTime(normalizedEndTime)
                    .isBreak(request.isBreakTime());

            if (!request.isBreakTime()) {
                if ( request.getTeacherId() == null) {
                    throw new IllegalArgumentException("Subject ID and teacher ID must not be null for non-break schedules");
                }

                ClassSubject classSubject = classSubjectRepository.findByIdAndClassBlockIdAndSchoolId(request.getSubjectId(), classBlockId, schoolId )
                        .orElseThrow(() -> new CustomNotFoundException("Class subject not found with ID: " + request.getSubjectId()));

                subjectRepository.findById(classSubject.getSubject().getId())
                        .orElseThrow(() -> new CustomNotFoundException("Subject not found with ID: " + classSubject.getSubject().getId()));

                Profile teacherProfile = profileRepository.findByIdAndSchoolId(request.getTeacherId(),schoolId)
                        .orElseThrow(() -> new CustomNotFoundException("Teacher profile not found with ID: " + request.getTeacherId()));

                if (hasTeacherTimeOverlap(timetable.getSchool().getId(), teacherProfile, normalizedStartTime, normalizedEndTime, dayOfWeek)) {
                    throw new IllegalStateException("Teacher ID " + teacherProfile.getId() + " is already scheduled at this time on " + dayOfWeek);
                }

                scheduleBuilder.subject(classSubject).teacher(teacherProfile);
            } else {
                if (request.getSubjectId() != null || request.getTeacherId() != null) {
                    throw new IllegalArgumentException("Break schedules must not include subject ID or teacher ID");
                }
            }

            schedules.add(scheduleBuilder.build());
        }
        return schedules;
    }

    private LocalTime parseTime(String timeInput) {
        try {
            return LocalTime.parse(timeInput, DateTimeFormatter.ofPattern("HH:mm[:ss]"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format: " + timeInput + ". Expected HH:mm:ss or HH:mm");
        }
    }

    private String normalizeTimeFormat(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private boolean hasTeacherTimeOverlap(Long schoolId, Profile teacher, String startTime, String endTime, DayOfWeek dayOfWeek) {
        LocalTime newStart = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime newEnd = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm:ss"));

        List<SubjectSchedule> existingSchedules = subjectScheduleRepository.findByTeacherAndSchoolIdAndTimetableDayOfWeek(teacher, schoolId, dayOfWeek);
        for (SubjectSchedule schedule : existingSchedules) {
            LocalTime existingStart = LocalTime.parse(schedule.getStartTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
            LocalTime existingEnd = LocalTime.parse(schedule.getEndTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));

            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTimeOverlap(Timetable timetable, String startTime, String endTime, DayOfWeek dayOfWeek) {
        LocalTime newStart = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime newEnd = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm:ss"));

        List<SubjectSchedule> existingSchedules = subjectScheduleRepository.findByTimetableIdAndTimetableDayOfWeek(timetable.getId(), dayOfWeek);
        for (SubjectSchedule schedule : existingSchedules) {
            LocalTime existingStart = LocalTime.parse(schedule.getStartTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
            LocalTime existingEnd = LocalTime.parse(schedule.getEndTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));

            if (newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    @Override
    public Timetable updateTimetable(Long timetableId, DayOfWeek dayOfWeek, List<SubjectScheduleRequest> subjectSchedules,
                                     Long termId, Long sessionId) {
        try {
            User admin = validateAuthenticatedUser(true);
            School school = admin.getSchool();
            validateTimetableInputs(dayOfWeek, subjectSchedules);

            Timetable timetable = timetableRepository.findByIdAndSchoolId(timetableId, school.getId())
                    .orElseThrow(() -> new CustomNotFoundException("Timetable not found with ID: " + timetableId + " in school ID: " + school.getId()));

            StudentTerm studentTerm = studentTermRepository.findByIdAndAcademicSessionId(termId, sessionId)
                    .orElseThrow(() -> new CustomNotFoundException("Student term not found with ID: " + termId));


            if (!timetable.getDayOfWeek().equals(dayOfWeek) &&
                    timetableRepository.existsByClassBlockIdAndTermIdAndDayOfWeek(timetable.getClassBlock().getId(), termId, dayOfWeek)) {
                throw new IllegalStateException(dayOfWeek + " already exists for this class block and term. Please choose a different day.");
            }

            validateScheduleTimeOverlaps(subjectSchedules, timetable.getClassBlock().getId(), termId, dayOfWeek);


            timetable.setDayOfWeek(dayOfWeek);
            timetable.setTerm(studentTerm);
            timetable.setAcademicYear(studentTerm.getAcademicSession());

            List<SubjectSchedule> schedules = createSubjectSchedules(subjectSchedules, timetable, dayOfWeek, timetable.getClassBlock().getId(), school.getId());
            timetable.getSubjectSchedules().clear();
            timetable.getSubjectSchedules().addAll(schedules);

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