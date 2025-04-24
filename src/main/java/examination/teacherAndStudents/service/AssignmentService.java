package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.AssignmentFilter;
import examination.teacherAndStudents.dto.AssignmentRequest;
import examination.teacherAndStudents.dto.AssignmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AssignmentService {
    AssignmentResponse saveAssignment(AssignmentRequest request);    // Add new assignment
    AssignmentResponse updateAssignment(Long id, AssignmentRequest request); // Edit assignment
    AssignmentResponse getAssignmentById(Long id);  // Get assignment by ID
    Page<AssignmentResponse> getAllAssignments(AssignmentFilter filter, Pageable pageable);  // Get all assignments
    void deleteAssignment(Long id);  // Delete assignment
}

