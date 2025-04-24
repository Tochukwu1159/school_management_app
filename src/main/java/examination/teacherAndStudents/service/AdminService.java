package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.ProfileStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminService {
    Page<User> getAllStudents(
            String firstName,
            String lastName,
            String middleName,
            String email,
            ProfileStatus profileStatus,
            Long id,
            int page,
            int size,
            String sortBy,
            String sortDirection);
    List<User> getAllTeachers();
}
