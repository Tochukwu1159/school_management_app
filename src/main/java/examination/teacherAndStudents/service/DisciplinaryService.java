package examination.teacherAndStudents.service;

import examination.teacherAndStudents.entity.DisciplinaryAction;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.utils.DisciplinaryActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface DisciplinaryService {

    DisciplinaryAction issueDisciplinaryAction(
            Profile profile,
            Profile issuedBy,
            DisciplinaryActionType actionType,
            String reason,
            String description,
            LocalDate startDate,
            LocalDate endDate);

    List<DisciplinaryAction> getActiveActionsForProfile(Profile profile);
    boolean isProfileSuspended(Profile profile);

    void deactivateExpiredActions();
    void updateDisciplinaryAction(Long actionId, String reason, String description,
                                  LocalDate endDate, boolean isActive);

    void cancelDisciplinaryAction(Long actionId);

    DisciplinaryAction getDisciplinaryActionById(Long id);

    Page<DisciplinaryAction> getAllActiveDisciplinaryActions(Pageable pageable);
}
