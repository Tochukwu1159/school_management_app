package examination.teacherAndStudents.repository;

import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.StudentTransportTracker;
import examination.teacherAndStudents.entity.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentTransportTrackerRepository extends JpaRepository<StudentTransportTracker, Long> {
    List<StudentTransportTracker> findByStudent(Profile student);
    List<StudentTransportTracker> findByTransport(Transport transport);

    boolean existsByStudentAndTransportAndStatus(Profile student, Transport transport, StudentTransportTracker.Status status);
}
