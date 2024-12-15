package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.FundWalletRequest;
import examination.teacherAndStudents.dto.SchoolRequest;
import examination.teacherAndStudents.dto.SchoolResponse;
import examination.teacherAndStudents.entity.School;

import java.util.List;

public interface SchoolService {
    SchoolResponse onboardSchool(SchoolRequest school);
    List<String> getSelectedServices(Long schoolId);
    boolean isValidSubscriptionKey(String subscriptionKey);
    void accessibleService(Long schoolId, String serviceName);

    School findBySubscriptionKey(String subscriptionKey);
    void renewSubscription(Long schoolId, FundWalletRequest fundWalletRequest);
}
