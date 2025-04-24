package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.StudyMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {
    Optional<StudyMaterial> findByIdAndSubjectSchoolId(Long id, Long schoolId);
    Page<StudyMaterial> findAllBySubjectSchoolId(Long id, Pageable pageable);

    @Query("SELECT m FROM StudyMaterial m WHERE m.classBlock.id IN " +
            "(SELECT c.id FROM ClassBlock c JOIN c.studentList s WHERE s.id = :profileId) " +
            "AND m.subject.school.id = :schoolId")
    Page<StudyMaterial> findAllByClassBlockStudentsProfileIdAndSubjectSchoolId(Long profileId, Long schoolId, Pageable pageable);}