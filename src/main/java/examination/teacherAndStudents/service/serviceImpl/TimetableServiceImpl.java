package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.SubjectScheduleRequest;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.error_handler.AuthenticationFailedException;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.TimetableService;
import examination.teacherAndStudents.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TimetableServiceImpl implements TimetableService {
    private static final Logger log = LoggerFactory.getLogger(TimetableServiceImpl.class);

    @Autowired
        private TimetableRepository timetableRepository;
    @Autowired
    private ClassLevelRepository classLevelRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubjectScheduleRepository subjectScheduleRepository;
    @Autowired
    private ClassBlockRepository classBlockRepository;
    @Autowired
    private ClassSubjectRepository classSubjectRepository;
    @Autowired
    private AcademicSessionRepository academicSessionRepository;
    @Autowired
    private StudentTermRepository studentTermRepository;
    @Autowired
    private ProfileRepository profileRepository;

    @Transactional
        @Override
    public Timetable createTimetable(DayOfWeek dayOfWeek, List<SubjectScheduleRequest> subjectSchedules, TimetableType timetableType, Long termId, Long sessionId, Long classBlockId) {
        try {
            // Ensure the user has admin role
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            Optional<User> userDetails = userRepository.findByEmail(email);


            Optional<ClassBlock> classBlock = classBlockRepository.findById(classBlockId);
            if (classBlock.isEmpty()) {
                throw new CustomNotFoundException("Class block with ID: " + classBlockId + " not found");
            }

            Optional<StudentTerm> studentTerm = studentTermRepository.findById(termId);
            if (studentTerm.isEmpty()) {
                throw new CustomNotFoundException("Student with ID " + termId + " not found");
            }

            // Perform any validation or processing if needed
            Optional<ClassLevel> classLevel = classLevelRepository.findById(classBlock.get().getClassLevel().getId());
            if (classLevel.isEmpty()) {
                throw new CustomNotFoundException("Class Level not found for claass block with Id " + classBlock.get().getId() + " not found");
            }
            // Find the academic year by ID
            Optional<AcademicSession> academicYearOptional = academicSessionRepository.findById(sessionId);
            AcademicSession academicYear = academicYearOptional.orElseThrow(() ->
                    new CustomNotFoundException("Academic year with ID: " + sessionId + " not found"));

            // Create a Timetable entity
            Timetable timetable = new Timetable();
            timetable.setClassBlock(classBlock.get());
            timetable.setDayOfWeek(dayOfWeek);
            timetable.setTerm(studentTerm.get());
            timetable.setSchool(userDetails.get().getSchool());
            timetable.setAcademicYear(academicYear);
            timetable.setTimetableType(timetableType); // Set the timetable type

            // Create SubjectSchedule entities and associate them with the timetable
            List<SubjectSchedule> schedules = new ArrayList<>();
            for (SubjectScheduleRequest scheduleRequest : subjectSchedules) {

                // Retrieve the Subject by ID
                Optional<ClassSubject> subjectForSchedule = classSubjectRepository.findById(scheduleRequest.getSubjectId());
                if (subjectForSchedule.isEmpty()) {
                    throw new CustomNotFoundException("Subject not found with ID: " + scheduleRequest.getSubjectId());
                }
                subjectRepository.findById(subjectForSchedule.get().getSubject().getId())
                        .orElseThrow(() -> new CustomNotFoundException("Subject with ID " + scheduleRequest.getSubjectId() + " not found in the general subject."));

                User teacher = userRepository.findById(scheduleRequest.getTeacherId())
                        .orElseThrow(() -> new CustomNotFoundException("User with ID " + scheduleRequest.getTeacherId() + " not found."));

          Profile teacherProfile = profileRepository.findByUser(teacher)
                        .orElseThrow(() -> new CustomNotFoundException("Profile with ID " + scheduleRequest.getTeacherId() + " not found."));


                // Set the Subject for the SubjectSchedule
                SubjectSchedule schedule = new SubjectSchedule();
                schedule.setSubject(subjectForSchedule.get());
                schedule.setStartTime(scheduleRequest.getStartTime());
                schedule.setTeachingStatus(TeachingStatus.PENDING);
                schedule.setTopic(scheduleRequest.getTopic());
                schedule.setTeacher(teacherProfile);
                schedule.setEndTime(scheduleRequest.getEndTime());
                schedule.setTimetable(timetable);
                schedules.add(schedule);
            }

            timetable.setSubjectSchedules(schedules);
            // Save the timetable and associated subject schedules
            timetable = timetableRepository.save(timetable);

            return timetable;
        } catch (CustomNotFoundException e) {
            // Log the exception
            log.error("Error creating timetable: " + e.getMessage(), e);
            // Rethrow the custom exception
            throw new CustomNotFoundException("Error creating timetable: " + e.getMessage());
        } catch (Exception e) {
            // Log the exception
            log.error("Unexpected error creating timetable " + e.getMessage());
            // Wrap and throw a more general exception
            throw new CustomInternalServerException("Unexpected error creating timetable " + e.getMessage());
        }
    }



   @Override
    public Timetable updateTimetable(Long timetableId, DayOfWeek dayOfWeek, List<SubjectScheduleRequest> subjectSchedules, Long termId, Long sessionId) {
        try {
            // Ensure the user has admin role
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            // Retrieve the existing timetable
            Timetable existingTimetable = timetableRepository.findById(timetableId)
                    .orElseThrow(() -> new CustomNotFoundException("Timetable not found with ID: " + timetableId));

            Optional<AcademicSession> academicYearOptional = academicSessionRepository.findById(sessionId);
            AcademicSession academicYear = academicYearOptional.orElseThrow(() ->
                    new CustomNotFoundException("Academic year with ID: " + sessionId + " not found"));


            Optional<StudentTerm> studentTerm = studentTermRepository.findById(termId);
            if (studentTerm.isEmpty()) {
                throw new CustomNotFoundException("Student with ID " + termId + " not found");
            }

            // Update existingTimetable properties
            existingTimetable.setDayOfWeek(dayOfWeek);
            existingTimetable.setTerm(studentTerm.get());
            existingTimetable.setAcademicYear(academicYear);
            // Update SubjectSchedule entities and associate them with the timetable
            List<SubjectSchedule> schedules = new ArrayList<>();
            for (SubjectScheduleRequest scheduleRequest : subjectSchedules) {
                SubjectSchedule schedule = new SubjectSchedule();

                // Retrieve the Subject by ID
                // Retrieve the Subject by ID
                Optional<ClassSubject> subjectForSchedule = classSubjectRepository.findById(scheduleRequest.getSubjectId());
                if (subjectForSchedule.isEmpty()) {
                    throw new CustomNotFoundException("Subject not found with ID: " + scheduleRequest.getSubjectId());
                }
                Optional<Subject> allSubject = subjectRepository.findById(subjectForSchedule.get().getSubject().getId());

                User teacher = userRepository.findById(scheduleRequest.getTeacherId())
                        .orElseThrow(() -> new CustomNotFoundException("User with ID " + scheduleRequest.getTeacherId() + " not found."));

                Profile teacherProfile = profileRepository.findByUser(teacher)
                        .orElseThrow(() -> new CustomNotFoundException("Profile with ID " + scheduleRequest.getTeacherId() + " not found."));


                // Set the Subject for the SubjectSchedule
                schedule.setSubject(subjectForSchedule.get());
                schedule.setStartTime(scheduleRequest.getStartTime());
                schedule.setEndTime(scheduleRequest.getEndTime());
                schedule.setTeacher(teacherProfile);
                schedule.setTimetable(existingTimetable);
                schedules.add(schedule);
            }

            existingTimetable.setSubjectSchedules(schedules);

            // Save the updated timetable and associated subject schedules
            existingTimetable = timetableRepository.save(existingTimetable);

            return existingTimetable;
        } catch (CustomNotFoundException e) {
            // Log the exception
            log.error("Error updating timetable: " + e.getMessage(), e);
            // Rethrow the custom exception
            throw new CustomNotFoundException("Error updating timetable: " + e.getMessage());
        } catch (Exception e) {
            // Log the exception
            log.error("Unexpected error updating timetable " + e.getMessage());
            // Wrap and throw a more general exception
            throw new CustomInternalServerException("Unexpected error updating timetable " + e.getMessage());
        }
    }


    public List<Timetable> getAllTimetables() {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);
            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }
            return timetableRepository.findAll();
        } catch (Exception e) {
            throw new CustomInternalServerException("Unexpected error fetching all timetable "+e.getMessage());

        }

    }

    public void deleteTimetable(Long timetableId) {
        try {
            // Perform any validation or checks if needed
            Optional<Timetable> existingTimetable = timetableRepository.findById(timetableId);
            if (existingTimetable.isEmpty()) {
                throw new CustomNotFoundException("Timetable not found with ID: " + timetableId);
            }

            // Delete the timetable
            timetableRepository.deleteById(timetableId);

        } catch (CustomNotFoundException e) {
            // Log the exception
            log.error("Error deleting timetable: " + e.getMessage(), e);
            throw new CustomNotFoundException("Error deleting timetable: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting timetable " + e.getMessage());
            throw new CustomInternalServerException("Unexpected error deleting timetable " + e.getMessage());
        }
    }

    public ResponseEntity<Timetable> getTimetableById(Long timetableId) {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            User admin = userRepository.findByEmailAndRoles(email, Roles.ADMIN);

            if (admin == null) {
                throw new CustomNotFoundException("Please login as an Admin");
            }

            Optional<Timetable> timetableOptional = timetableRepository.findById(timetableId);

            if (timetableOptional.isPresent()) {
                return ResponseEntity.ok(timetableOptional.get());
            } else {
                throw new CustomNotFoundException("Timetable not found with ID: " + timetableId);
            }
        } catch (Exception e) {
            throw new CustomInternalServerException("Unexpected error fetching timetable by ID: " + timetableId + ". " + e.getMessage());
        }
    }

    public List<SubjectSchedule> getAllSubjectSchedules() {
        return subjectScheduleRepository.findAll();
    }





    // Other methods related to SubjectSchedule can be added here
}

