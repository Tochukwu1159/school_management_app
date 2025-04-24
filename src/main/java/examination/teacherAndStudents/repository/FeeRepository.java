package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeeRepository extends JpaRepository<Fee, Long> {



    @Query("SELECT f FROM Fee f WHERE " +
            "f.school.id = :schoolId AND " +
            "f.session.id = :sessionId AND " +
            "(:classLevelId IS NULL OR f.classLevel.id = :classLevelId OR f.classLevel IS NULL) AND " +
            "(:subClassId IS NULL OR f.subClass.id = :subClassId OR f.subClass IS NULL) AND " +
            "(:termId IS NULL OR f.term.id = :termId OR f.term IS NULL) " +
            "ORDER BY f.isCompulsory DESC")
    List<Fee> findApplicableFees(
            @Param("schoolId") Long schoolId,
            @Param("sessionId") Long sessionId,
            @Param("classLevelId") Long classLevelId,
            @Param("subClassId") Long subClassId,
            @Param("termId") Long termId);

    @Query("SELECT f FROM Fee f WHERE " +
            "f.school.id = :schoolId AND " +
            "(f.classLevel IS NULL OR f.classLevel.id = :classLevelId) AND " +
            "(f.subClass IS NULL OR f.subClass.id = :subClassId) AND " +
            "(f.term IS NULL OR f.term.id = :termId) " +
            "ORDER BY f.amount DESC")
    List<Fee> findApplicationFeeBySchool(@Param("schoolId") Long schoolId,
                                         @Param("classLevelId") Long classLevelId,
                                         @Param("subClassId") Long subClassId,
                                         @Param("termId") Long termId);

    @Query("SELECT f FROM Fee f WHERE " +
            "f.school.id = :schoolId AND " +
            "(f.classLevel IS NULL OR f.classLevel.id = :classLevelId) AND " +
            "(f.subClass IS NULL OR f.subClass.id = :subClassId) AND " +
            "(f.term IS NULL OR f.term.id = :termId) " +
            "ORDER BY f.amount DESC")
    List<Fee> findAdmissionFeeBySchoolAndClass(@Param("schoolId") Long schoolId,
                                               @Param("classLevelId") Long classLevelId,
                                               @Param("subClassId") Long subClassId,
                                               @Param("termId") Long termId);



//    @Query("SELECT f FROM Fee f WHERE " +
//            "(:schoolId IS NULL OR f.school.id = :schoolId) AND " +
//            "(:sessionId IS NULL OR f.session.id = :sessionId) AND " +
//            "(:classLevelId IS NULL OR f.classLevel.id = :classLevelId) AND " +
//            "(:subClassId IS NULL OR f.subClass.id = :subClassId) AND " +
//            "(:termId IS NULL OR f.term.id = :termId)")
//    List<Fee> findApplicableFees(@Param("schoolId") Long schoolId,
//                                 @Param("sessionId") Long sessionId,
//                                 @Param("classLevelId") Long classLevelId,
//                                 @Param("subClassId") Long subClassId,
//                                 @Param("termId") Long termId);

    boolean existsByCategoryId(Long id);
}