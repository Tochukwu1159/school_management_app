package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.RatingRequest;
import examination.teacherAndStudents.dto.RatingResponse;
import examination.teacherAndStudents.entity.Rating;
import examination.teacherAndStudents.entity.School;

import java.util.List;

public interface RatingService {
    RatingResponse createRating(RatingRequest ratingRequest);
    RatingResponse updateRating(Long ratingId, RatingRequest ratingRequest);
    void deleteRatingById(Long ratingId);
    RatingResponse getRatingById(Long ratingId);
    List<RatingResponse> findAllRatingsBySchool(Long schoolId);

//    Rating calculateRating(School school, double totalMarks);
}


//{
//        "school": {
//        "id": 1
//        },
//        "minMarks": 85,
//        "maxMarks": 100,
//        "grade": "A"
//        }

//        {
//        "school": {
//        "id": 1
//        },
//        "minMarks": 85,
//        "maxMarks": 100,
//        "rating": "Excellent"
//        }