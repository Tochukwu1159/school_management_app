package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SickLeaveCancelRequest;
import examination.teacherAndStudents.dto.SickLeaveRequest;
import examination.teacherAndStudents.dto.SickLeaveRequestDto;
import examination.teacherAndStudents.entity.Leave;

import java.util.List;

public interface SickLeaveService {
    String applyForSickLeave(SickLeaveRequest sickLeaveRequest);
    List<Leave> getPendingSickLeaves();
    String approveOrRejectSickLeaveRequest(Long sickLeaveId, SickLeaveRequestDto updatedSickLeave);
    String cancelSickLeave(SickLeaveCancelRequest sickLeaveCancelRequest);
    List<Leave> getAllSickLeave();

}
