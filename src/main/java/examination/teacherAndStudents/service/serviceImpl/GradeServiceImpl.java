package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.GradeRequest;
import examination.teacherAndStudents.dto.GradeResponse;
import examination.teacherAndStudents.entity.Grade;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.GradeRepository;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GradeServiceImpl implements GradeService {

        @Autowired
        private GradeRepository gradeRepository;

        @Autowired
        private SchoolRepository schoolRepository;
    @Autowired
    private UserRepository userRepository;

    public GradeResponse createGrade(GradeRequest gradeRequest) {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> user = userRepository.findByEmail(email);
            if (user == null) {
                throw new CustomNotFoundException("Please login as a Student");
            }

            Grade grade = new Grade();
            grade.setSchool(user.get().getSchool());
            grade.setMinMarks(gradeRequest.getMinMarks());
            grade.setMaxMarks(gradeRequest.getMaxMarks());
            grade.setGrade(gradeRequest.getGrade());

            Grade savedGrade = gradeRepository.save(grade);
            return mapToGradeResponse(savedGrade);
        }

        public GradeResponse updateGrade(Long gradeId, GradeRequest gradeRequest) {
            Grade existingGrade = gradeRepository.findById(gradeId)
                    .orElseThrow(() -> new NotFoundException("Grade not found"));

            existingGrade.setMinMarks(gradeRequest.getMinMarks());
            existingGrade.setMaxMarks(gradeRequest.getMaxMarks());
            existingGrade.setGrade(gradeRequest.getGrade());

            Grade updatedGrade = gradeRepository.save(existingGrade);
            return mapToGradeResponse(updatedGrade);
        }

        public void deleteGradeById(Long gradeId) {
            Grade existingGrade = gradeRepository.findById(gradeId)
                    .orElseThrow(() -> new NotFoundException("Grade not found"));
            gradeRepository.delete(existingGrade);
        }

        public GradeResponse getGradeById(Long gradeId) {
            Grade grade = gradeRepository.findById(gradeId)
                    .orElseThrow(() -> new NotFoundException("Grade not found"));
            return mapToGradeResponse(grade);
        }

        public List<GradeResponse> findAllGradesBySchool(Long schoolId) {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new NotFoundException("School not found"));
            return gradeRepository.findBySchool(school).stream()
                    .map(this::mapToGradeResponse)
                    .collect(Collectors.toList());
        }


    public Grade calculateGrade(School school, double totalMarks) {
        return gradeRepository.findBySchool(school).stream()
                .filter(g -> totalMarks >= g.getMinMarks() && totalMarks <= g.getMaxMarks())
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Grade not found for the specified marks"));
    }

        private GradeResponse mapToGradeResponse(Grade grade) {
            GradeResponse response = new GradeResponse();
            response.setId(grade.getId());
            response.setSchoolId(grade.getSchool().getId());
            response.setMinMarks(grade.getMinMarks());
            response.setMaxMarks(grade.getMaxMarks());
            response.setGrade(grade.getGrade());
            return response;
        }
    }


