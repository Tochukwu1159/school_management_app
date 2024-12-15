package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.SickLeaveRequest;
import examination.teacherAndStudents.entity.SickLeave;

import java.util.List;

public interface SickLeaveService {
    void applyForSickLeave(SickLeaveRequest sickLeaveRequest);
    List<SickLeave> getPendingSickLeaves();
    void updateSickLeave(Long sickLeaveId, SickLeaveRequest updatedSickLeave);
    void cancelSickLeave(Long sickLeaveId);
    List<SickLeave> getAllSickLeave();

}
