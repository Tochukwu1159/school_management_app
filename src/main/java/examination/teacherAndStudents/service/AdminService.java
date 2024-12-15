package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.User;

import java.util.List;

public interface AdminService {
    List<User> getAllStudents();
    List<User> getAllTeachers();
}
