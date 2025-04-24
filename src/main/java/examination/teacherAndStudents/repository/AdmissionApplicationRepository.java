package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.AdmissionApplication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AdmissionApplicationRepository extends JpaRepository<AdmissionApplication, Long> {
}
