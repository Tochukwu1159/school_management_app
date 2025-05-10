package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.DisciplinaryActionRequest;
import examination.teacherAndStudents.dto.DisciplinaryActionResponse;
import examination.teacherAndStudents.entity.DisciplinaryAction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DisciplinaryService {
    DisciplinaryActionResponse issueDisciplinaryAction(DisciplinaryActionRequest request);
    List<DisciplinaryAction> getActiveActionsForProfile(Long profileId);
    boolean isProfileSuspended(Long profileId);
    void deactivateExpiredActions();
    void updateDisciplinaryAction(Long actionId, DisciplinaryActionRequest request);
    void cancelDisciplinaryAction(Long actionId);
    DisciplinaryActionResponse getDisciplinaryActionById(Long id);
    Page<DisciplinaryActionResponse> getAllActiveDisciplinaryActions(Pageable pageable);
}