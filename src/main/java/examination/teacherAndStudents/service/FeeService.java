package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.FeeDTO;
import examination.teacherAndStudents.dto.StudentFeeResponse;
import examination.teacherAndStudents.entity.Fee;
import examination.teacherAndStudents.dto.FeeResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface FeeService {
    FeeResponseDto createFee(FeeDTO feeDTO);
    List<StudentFeeResponse> getApplicableFeesForStudent(Long studentId);
    List<StudentFeeResponse> getApplicableUnpaidFeesForStudent(Long studentId);
    BigDecimal getApplicationFee(Long schoolId, Long classLevelId, Long subClassId);
}