package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.RatingRequest;
import examination.teacherAndStudents.dto.RatingResponse;
import examination.teacherAndStudents.entity.Rating;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.RatingRepository;
import examination.teacherAndStudents.repository.SchoolRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private UserRepository userRepository;

    public RatingResponse createRating(RatingRequest ratingRequest) {
        String email = SecurityConfig.getAuthenticatedUserEmail();
        Optional<User> user = userRepository.findByEmail(email);
        if (user == null) {
            throw new CustomNotFoundException("Please login as a Student");
        }

        Rating rating = new Rating();
        rating.setSchool(user.get().getSchool());
        rating.setMinMarks(ratingRequest.getMinMarks());
        rating.setMaxMarks(ratingRequest.getMaxMarks());
        rating.setRating(ratingRequest.getRating());

        Rating savedRating = ratingRepository.save(rating);
        return mapToRatingResponse(savedRating);
    }

    public RatingResponse updateRating(Long ratingId, RatingRequest ratingRequest) {
        Rating existingRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found"));

        existingRating.setMinMarks(ratingRequest.getMinMarks());
        existingRating.setMaxMarks(ratingRequest.getMaxMarks());
        existingRating.setRating(ratingRequest.getRating());

        Rating updatedRating = ratingRepository.save(existingRating);
        return mapToRatingResponse(updatedRating);
    }

    public void deleteRatingById(Long ratingId) {
        Rating existingRating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found"));
        ratingRepository.delete(existingRating);
    }

    public RatingResponse getRatingById(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NotFoundException("Rating not found"));
        return mapToRatingResponse(rating);
    }

    public List<RatingResponse> findAllRatingsBySchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new NotFoundException("School not found"));
        return ratingRepository.findBySchool(school).stream()
                .map(this::mapToRatingResponse)
                .collect(Collectors.toList());
    }
    public Rating calculateRating(School school, double totalMarks) {
        return ratingRepository.findBySchool(school).stream()
                .filter(r -> totalMarks >= r.getMinMarks() && totalMarks <= r.getMaxMarks())
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("Rating not found for the specified marks"));
    }


    private RatingResponse mapToRatingResponse(Rating rating) {
        RatingResponse response = new RatingResponse();
        response.setId(rating.getId());
        response.setSchoolId(rating.getSchool().getId());
        response.setMinMarks(rating.getMinMarks());
        response.setMaxMarks(rating.getMaxMarks());
        response.setRating(rating.getRating());
        return response;
    }
}
