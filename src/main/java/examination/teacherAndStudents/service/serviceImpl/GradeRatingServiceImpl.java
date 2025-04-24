package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.service.GradeRatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.entity.Grade;
import examination.teacherAndStudents.entity.Rating;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.GradeRepository;
import examination.teacherAndStudents.repository.RatingRepository;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GradeRatingServiceImpl implements GradeRatingService {
    private static final Logger logger = LoggerFactory.getLogger(GradeRatingServiceImpl.class);

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void createGradeRatings(GradeRatingRequestArray requestArray) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new CustomNotFoundException("Please login as a Student");
        }

        School school = user.get().getSchool();
        logger.info("User {} creating grade/rating configurations for school ID: {}", user.get().getEmail(), school.getId());

        // First delete existing grade and rating configurations for the school
        gradeRepository.deleteBySchool(school);
        ratingRepository.deleteBySchool(school);

        // Create new configurations
        for (GradeRatingRequest request : requestArray.getGradeRatingRequests()) {
            // Create and save Grade
            Grade grade = new Grade();
            grade.setSchool(school);
            grade.setMinMarks(request.getMinMarks());
            grade.setMaxMarks(request.getMaxMarks());
            grade.setGrade(request.getGrade());
            gradeRepository.save(grade);

            // Create and save Rating
            Rating rating = new Rating();
            rating.setSchool(school);
            rating.setMinMarks(request.getMinMarks());
            rating.setMaxMarks(request.getMaxMarks());
            rating.setRating(request.getRating());
            ratingRepository.save(rating);
        }
        logger.info("Created {} grade/rating pairs for school ID: {}",
                requestArray.getGradeRatingRequests().size(), school.getId());
    }

    public GradeRatingResponseArray getGradeRatingsBySchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new NotFoundException("School not found"));

        List<Grade> grades = gradeRepository.findBySchool(school);
        List<Rating> ratings = ratingRepository.findBySchool(school);

        if (grades.size() != ratings.size()) {
            throw new IllegalStateException("Grade and Rating configurations are out of sync for school: " + schoolId);
        }

        List<GradeRatingResponse> responses = grades.stream()
                .map(grade -> {
                    Rating matchingRating = ratings.stream()
                            .filter(r -> r.getMinMarks() == grade.getMinMarks() && r.getMaxMarks() == grade.getMaxMarks())
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No matching rating found for grade"));

                    return mapToGradeRatingResponse(grade, matchingRating);
                })
                .collect(Collectors.toList());

        GradeRatingResponseArray responseArray = new GradeRatingResponseArray();
        responseArray.setGradeRatingResponses(responses);
        return responseArray;
    }

    public GradeRatingPair calculateGradeAndRating(School school, double totalMarks) {
        Grade grade = gradeRepository.findBySchool(school).stream()
                .filter(g -> totalMarks >= g.getMinMarks() && totalMarks <= g.getMaxMarks())
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Grade not found for the specified marks"));

        Rating rating = ratingRepository.findBySchool(school).stream()
                .filter(r -> totalMarks >= r.getMinMarks() && totalMarks <= r.getMaxMarks())
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Rating not found for the specified marks"));

        return new GradeRatingPair(grade.getGrade(), rating.getRating());
    }

    @Transactional
    public void deleteGradeRatingById(Long id, String type) {
        if ("grade".equalsIgnoreCase(type)) {
            Grade grade = gradeRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Grade not found"));

            // Find matching rating by marks range and delete both
            Rating matchingRating = ratingRepository.findBySchoolAndMinMarksAndMaxMarks(
                            grade.getSchool(), grade.getMinMarks(), grade.getMaxMarks())
                    .orElseThrow(() -> new NotFoundException("Matching rating not found"));

            gradeRepository.delete(grade);
            ratingRepository.delete(matchingRating);
        } else if ("rating".equalsIgnoreCase(type)) {
            Rating rating = ratingRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Rating not found"));

            // Find matching grade by marks range and delete both
            Grade matchingGrade = gradeRepository.findBySchoolAndMinMarksAndMaxMarks(
                            rating.getSchool(), rating.getMinMarks(), rating.getMaxMarks())
                    .orElseThrow(() -> new NotFoundException("Matching grade not found"));

            ratingRepository.delete(rating);
            gradeRepository.delete(matchingGrade);
        } else {
            throw new IllegalArgumentException("Invalid type. Must be 'grade' or 'rating'");
        }
    }

    @Transactional
    public void updateGradeRatings(GradeRatingRequestArray requestArray) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new CustomNotFoundException("Please login as a Student");
        }

        School school = user.get().getSchool();

        // First delete existing configurations for the school
        gradeRepository.deleteBySchool(school);
        ratingRepository.deleteBySchool(school);

        // Then create new configurations (same as create operation)
        createGradeRatings(requestArray);
    }

    @Transactional
    public void deleteGradeRatingsBySchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new NotFoundException("School not found"));

        gradeRepository.deleteBySchool(school);
        ratingRepository.deleteBySchool(school);
    }


    private GradeRatingResponse mapToGradeRatingResponse(Grade grade, Rating rating) {
        GradeRatingResponse response = new GradeRatingResponse();
        response.setSchoolId(grade.getSchool().getId());
        response.setMinMarks(grade.getMinMarks());
        response.setMaxMarks(grade.getMaxMarks());
        response.setGrade(grade.getGrade());
        response.setRating(rating.getRating());
        return response;
    }
}