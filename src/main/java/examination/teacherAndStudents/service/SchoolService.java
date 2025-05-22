package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.School;
import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.utils.ServiceType;
import examination.teacherAndStudents.utils.SubscriptionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SchoolService {
    /**
     * Onboards a new school.
     *
     * @param school the school request details.
     * @return the onboarded school response.
     * @throws RuntimeException if an error occurs during onboarding.
     */
    SchoolResponse onboardSchool(SchoolRequest school);
    List<ProfileData> teacherProfilesForSchool(Long schoolId);
    School updateSchool(Long schoolId, SchoolRequest schoolRequest);
    List<ProfileData> studentsProfilesForSchool(Long schoolId);
    List<ProfileData> adminProfilesForSchool(Long schoolId);
    List<ProfileData> gateManProfilesForSchool(Long schoolId);

    BigDecimal getAmountToSubscribe(Long schoolId);
    void deleteSchool(Long schoolId);
    List<School> getAllSchools();

    boolean canAccessService(Long schoolId, Long serviceId);

    /**
     * Retrieves the selected services for a school.
     *
     * @param schoolId the school ID.
     * @return a list of selected services.
     * @throws RuntimeException if an error occurs or the school is not found.
     */
    List<ServiceOffered> getSelectedServices(Long schoolId);

    /**
     * Checks if a school can access a specific service.
     *
     * @param schoolId    the school ID.
     * @param serviceName the name of the service.
     * @throws RuntimeException if an error occurs or access is denied.
     */
    void accessibleService(Long schoolId, String serviceName);

    /**
     * Deactivates expired subscriptions for all schools.
     *
     * @throws RuntimeException if an error occurs during deactivation.
     */
    void deactivateExpiredSubscriptions();

    /**
     * Validates if a subscription key for a school is valid.
     *
     * @param schoolId the school ID.
     * @return true if the subscription key is valid, false otherwise.
     * @throws RuntimeException if an error occurs or the school is not found.
     */
    boolean isValidSubscriptionKey(Long schoolId);

    /**
     * Subscribes a school to a new expiry date.
     *
     * @return the updated school.
     * @throws RuntimeException if an error occurs or the school is not found.
     */
    School subscribeSchool(SubscriptionRequest subscriptionRequest) throws Exception;

    /**
     * Finds a school by its subscription key.
     *
     * @param subscriptionKey the subscription key.
     * @return the school entity.
     * @throws RuntimeException if an error occurs or the school is not found.
     */
    School findBySubscriptionKey(String subscriptionKey);

    School renewSubscription(SubscriptionType subscriptionType) throws Exception;

    School getSchoolById(Long schoolId);
    WalletResponse walletBalance();
}
