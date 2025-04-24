package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.GradeRatingPair;
import examination.teacherAndStudents.dto.GradeRatingRequestArray;
import examination.teacherAndStudents.dto.GradeRatingResponseArray;
import examination.teacherAndStudents.entity.School;

public interface GradeRatingService
{
    void createGradeRatings(GradeRatingRequestArray requestArray);

    GradeRatingResponseArray getGradeRatingsBySchool(Long schoolId);

    GradeRatingPair calculateGradeAndRating(School school, double totalMarks);
    void deleteGradeRatingById(Long id, String type);
    void updateGradeRatings(GradeRatingRequestArray requestArray);
    void deleteGradeRatingsBySchool(Long schoolId);
}
