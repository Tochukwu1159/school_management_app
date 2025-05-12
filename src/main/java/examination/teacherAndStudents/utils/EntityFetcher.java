package examination.teacherAndStudents.utils;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.entity.AcademicSession;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.entity.StudentTerm;

import examination.teacherAndStudents.error_handler.*;
import examination.teacherAndStudents.repository.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class EntityFetcher {

    private final AcademicSessionRepository academicSessionRepository;
    private final ClassBlockRepository classBlockRepository;
    private final StudentTermRepository studentTermRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final TimetableRepository timetableRepository;
    private final StaffLevelRepository staffLevelRepository;
    private final StaffAttendanceRepository staffAttendanceRepository;
    private final SubjectRepository subjectRepository;
    private final BookRepository bookRepository;
    private final NoticeRepository noticeRepository;
    private final BookBorrowingRepository bookBorrowingRepository;
    private final ResultRepository resultRepository;
    private final PositionRepository positionRepository;
    private final SessionAverageRepository sessionAverageRepository;
    private final ClassLevelRepository classLevelRepository;
    private final BusRouteRepository busRouteRepository;
    private final RatingRepository ratingRepository;
    private final ClassSubjectRepository classSubjectRepository;
    private final CurriculumRepository curriculumRepository;
    private final SessionNameRepository sessionNameRepository;
    private final ClassNameRepository classNameRepository;

    public EntityFetcher(AcademicSessionRepository academicSessionRepository,
                         ClassBlockRepository classBlockRepository,
                         StudentTermRepository studentTermRepository, UserRepository userRepository, ProfileRepository profileRepository, TimetableRepository timetableRepository, StaffLevelRepository staffLevelRepository, StaffAttendanceRepository staffAttendanceRepository, SubjectRepository subjectRepository, BookRepository bookRepository, NoticeRepository noticeRepository, BookBorrowingRepository bookBorrowingRepository, ResultRepository resultRepository, PositionRepository positionRepository, SessionAverageRepository sessionAverageRepository, ClassLevelRepository classLevelRepository, BusRouteRepository busRouteRepository, RatingRepository ratingRepository, ClassSubjectRepository classSubjectRepository, CurriculumRepository curriculumRepository, SessionNameRepository sessionNameRepository, ClassNameRepository classNameRepository) {
        this.academicSessionRepository = academicSessionRepository;
        this.classBlockRepository = classBlockRepository;
        this.studentTermRepository = studentTermRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.timetableRepository = timetableRepository;
        this.staffLevelRepository = staffLevelRepository;
        this.staffAttendanceRepository = staffAttendanceRepository;
        this.subjectRepository = subjectRepository;
        this.bookRepository = bookRepository;
        this.noticeRepository = noticeRepository;
        this.bookBorrowingRepository = bookBorrowingRepository;
        this.resultRepository = resultRepository;
        this.positionRepository = positionRepository;
        this.sessionAverageRepository = sessionAverageRepository;
        this.classLevelRepository = classLevelRepository;
        this.busRouteRepository = busRouteRepository;
        this.ratingRepository = ratingRepository;
        this.classSubjectRepository = classSubjectRepository;
        this.curriculumRepository = curriculumRepository;
        this.sessionNameRepository = sessionNameRepository;
        this.classNameRepository = classNameRepository;
    }

public User fetchLoggedInAdmin(String email) {
            Optional<User> admin = userRepository.findByEmailAndRole(email, Roles.ADMIN);
    if (admin == null) {
        throw new AuthenticationFailedException("Please login as an Admin");
    }
    return admin.get();
}

    public String fetchLoggedInUser(){
        return SecurityConfig.getAuthenticatedUserEmail();
    }

    public User fetchUserFromEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

    }


    public AcademicSession fetchAcademicSession(Long sessionId) {
        return academicSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));
    }

    public ClassBlock fetchClassBlock(Long classLevelId) {
        return classBlockRepository.findById(classLevelId)
                .orElseThrow(() -> new NotFoundException("Class level not found"));
    }
    public  Rating fetchRating(Long ratingId) {
        return ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found"));
    }

    public Optional<User> fetchUser(String email) {
        return userRepository.findByEmail(email);
    }


    public Curriculum fetchCurriculum(Long curriculumId) {
        return curriculumRepository.findById(curriculumId)
                .orElseThrow(() -> new NotFoundException("Curriculum with id " + curriculumId + " not found"));
    }


    public examination.teacherAndStudents.entity.StudentTerm fetchStudentTerm(Long termId) {
        return studentTermRepository.findById(termId)
                .orElseThrow(() -> new NotFoundException("Term not found"));
    }


    public BusRoute fetchBusRoute(Long busRouteId) {
        BusRoute busRoute = busRouteRepository.findById(busRouteId).orElse(null);
        if (busRoute == null) {
            throw new IllegalArgumentException("Route not found for id: " + busRouteId);
        }
        return busRoute;
    }

    public User fetchUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomNotFoundException("User not found with ID: " + userId));
    }
    public Notice fetchNotice(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new CustomNotFoundException("Notice post not found with id: " + noticeId));
    }

    public Profile fetchProfileByUser(User user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new CustomNotFoundException("Profile not found with ID: " + user.getId()));
    }
    public Subject fetchSubject(Long subjectId) {
        return  subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }

    public ClassSubject fetchClassSubject(Long subjectId) {
        return  classSubjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
    }

    public Book fetchBook(Long bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    public ClassBlock fetchNewClassBlockByClass(Long classLevelId) {
   return classBlockRepository.findById(classLevelId)
           .orElseThrow(() -> new BadRequestException("Class block not found with ID: " + classLevelId));
}



public  Map<Profile, Position>  fetchExistingPositions(ClassBlock classBlock, AcademicSession academicSession, StudentTerm studentTerm) {
        return positionRepository.findByClassBlockAndAcademicYearAndStudentTerm(
                        classBlock, academicSession, studentTerm).stream()
                .collect(Collectors.toMap(Position::getUserProfile, Function.identity()));

}

public Position fetchSecondTermPosition(Profile profile, ClassBlock classBlock, AcademicSession academicSession) {
        return positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
                profile, classBlock, academicSession,
                studentTermRepository.findByNameAndAcademicSession("Second Term", academicSession));
}

    public Position fetchFirstTermPosition(Profile profile, ClassBlock classBlock, AcademicSession academicSession) {
        return positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
                profile, classBlock, academicSession,
                studentTermRepository.findByNameAndAcademicSession("First Term", academicSession));
    }

    public SessionAverage fetchSessionAverage(Profile profile, AcademicSession academicSession, ClassBlock classBlock) {
        return sessionAverageRepository.findByUserProfileAndAcademicYearAndClassBlock(
                profile, academicSession, classBlock);
    }

    public Position fetchThirdTermPosition(Profile profile, ClassBlock classBlock, AcademicSession academicSession) {
        return positionRepository.findByUserProfileAndClassBlockAndAcademicYearAndStudentTerm(
                profile, classBlock, academicSession,
                studentTermRepository.findByNameAndAcademicSession("Third Term", academicSession));
    }

public List<SessionAverage> fetchSessionAverage(ClassBlock classBlock, AcademicSession academicSession) {
        return sessionAverageRepository.findAllByClassBlockAndAcademicYear(classBlock, academicSession);
}

public Timetable fetchTimetable(Long timetableId) {
        return timetableRepository.findById(timetableId)
                .orElseThrow(() -> new CustomNotFoundException("Timetable not found with ID: " + timetableId));
}

public List<Result> FetchResults(ClassBlock classBlock, AcademicSession academicSession, StudentTerm studentTerm) {
        return resultRepository.findAllByClassBlockAndAcademicYearAndStudentTerm(
                classBlock, academicSession, studentTerm);
}

    public SessionName fetchSessionName(Long sessionNameId) {
        return sessionNameRepository.findById(sessionNameId).orElseThrow(()-> new CustomNotFoundException("Session name not found"));
    }

    public ClassName fetchClassName(Long id) {
        return classNameRepository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Class name not found with ID: " + id));
    }
}
