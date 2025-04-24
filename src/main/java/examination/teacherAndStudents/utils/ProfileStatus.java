package examination.teacherAndStudents.utils;

public enum ProfileStatus {
    ACTIVE,
    SUSPENDED,
    EXPELLED,
    ON_PROBATION,
    WARNED,
    RESTRICTED,
    FINED, PENDING,
    FIRED,
    GRADUATED,
    ON_LEAVE,
    ALUMNI,
    WITHDRAWN,
    ON_COMMUNITY_SERVICE,
    TRANSFERRING,
    INACTIVE,
        PENDING_REVIEW,       // For self-registered students
        CONSIDERING_ADMISSION, // After initial review
        PAYMENT_PENDING,      // After documents approved, awaiting payment
        PAYMENT_VERIFICATION, // Payment made, awaiting verification
        REJECTED              // Application rejected

}
