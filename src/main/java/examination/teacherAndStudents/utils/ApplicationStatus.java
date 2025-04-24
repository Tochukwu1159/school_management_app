package examination.teacherAndStudents.utils;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    /**
     * Initial state when application is first submitted
     */
    DRAFT("Draft", "Application has been started but not submitted"),

    /**
     * Application submitted but not yet reviewed
     */
    PENDING_REVIEW("Pending Review", "Application submitted and awaiting admin review"),

    /**
     * Admin has requested additional documents
     */
    DOCUMENTS_REQUIRED("Documents Required", "Additional documents needed to process application"),

    /**
     * Application meets initial requirements, awaiting payment
     */
    PAYMENT_PENDING("Payment Pending", "Application approved pending payment of fees"),

    /**
     * Payment submitted but awaiting verification
     */
    PAYMENT_VERIFICATION("Payment Verification", "Payment received, awaiting admin verification"),

    /**
     * Payment verified, final admin approval needed
     */
    PENDING_FINAL_APPROVAL("Pending Final Approval", "Payment verified, awaiting final admin approval"),

    PAYMENT_IN_PROGRESS("Payment In Progress", "Payment initiated, awaiting webhook feedback"),

    /**
     * Application fully approved and processed
     */
    APPROVED("Approved", "Application fully approved and student admitted"),

    PRE_APPROVED("Pre Approved", "Application fully approved and student admitted"),

    /**
     * Application rejected at any stage
     */
    REJECTED("Rejected", "Application has been rejected"),

    /**
     * Application withdrawn by applicant
     */
    WITHDRAWN("Withdrawn", "Application withdrawn by student"),
    EXAM_COMPLETED("Exam Completed", "Application completed"),
    /**
     * Application expired due to inactivity
     */
    EXPIRED("Expired", "Application expired due to inactivity");

       // New status after exam results are recorded


    private final String displayName;
    private final String description;

    ApplicationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Check if application is in a pending state
     */
    public boolean isPending() {
        return this == PENDING_REVIEW ||
                this == DOCUMENTS_REQUIRED ||
                this == PAYMENT_PENDING ||
                this == PAYMENT_VERIFICATION ||
                this == PENDING_FINAL_APPROVAL;
    }

    /**
     * Check if application is in a terminal state
     */
    public boolean isTerminal() {
        return this == APPROVED ||
                this == REJECTED ||
                this == WITHDRAWN ||
                this == EXPIRED;
    }

    /**
     * Check if application requires student action
     */
    public boolean requiresStudentAction() {
        return this == DOCUMENTS_REQUIRED ||
                this == PAYMENT_PENDING;
    }

    /**
     * Check if application requires admin action
     */
    public boolean requiresAdminAction() {
        return this == PENDING_REVIEW ||
                this == PAYMENT_VERIFICATION ||
                this == PENDING_FINAL_APPROVAL;
    }
}