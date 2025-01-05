package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.AssignmentRequest;
import examination.teacherAndStudents.dto.AssignmentResponse;

import java.util.List;

public interface AssignmentService {
    AssignmentResponse saveAssignment(AssignmentRequest request);    // Add new assignment
    AssignmentResponse updateAssignment(Long id, AssignmentRequest request); // Edit assignment
    AssignmentResponse getAssignmentById(Long id);  // Get assignment by ID
    List<AssignmentResponse> getAllAssignments();  // Get all assignments
    void deleteAssignment(Long id);  // Delete assignment
}

