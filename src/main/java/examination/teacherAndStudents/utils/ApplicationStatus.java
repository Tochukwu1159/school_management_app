package examination.teacherAndStudents.utils;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    DRAFT("Draft", "Application has been started but not submitted"),
    PENDING_REVIEW("Pending Review", "Application submitted and awaiting admin review"),
    DOCUMENTS_REQUIRED("Documents Required", "Additional documents needed to process application"),
    EXAM_SCHEDULED("Exam Scheduled", "Entrance exam has been scheduled"),
    EXAM_COMPLETED("Exam Completed", "Entrance exam completed, awaiting final review"),
    PAYMENT_PENDING("Payment Pending", "Application approved pending payment of fees"),
    PAYMENT_VERIFICATION("Payment Verification", "Payment received, awaiting admin verification"),
    PENDING_FINAL_APPROVAL("Pending Final Approval", "Payment verified, awaiting final admin approval"),
    PAYMENT_IN_PROGRESS("Payment In Progress", "Payment initiated, awaiting webhook feedback"),
    APPROVED("Approved", "Application fully approved and student admitted"),
    REJECTED("Rejected", "Application has been rejected"),
    WITHDRAWN("Withdrawn", "Application withdrawn by student"),
    EXPIRED("Expired", "Application expired due to inactivity");

    private final String displayName;
    private final String description;

    ApplicationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public boolean isPending() {
        return this == PENDING_REVIEW ||
                this == DOCUMENTS_REQUIRED ||
                this == EXAM_SCHEDULED ||
                this == EXAM_COMPLETED ||
                this == PAYMENT_PENDING ||
                this == PAYMENT_VERIFICATION ||
                this == PENDING_FINAL_APPROVAL;
    }

    public boolean isTerminal() {
        return this == APPROVED ||
                this == REJECTED ||
                this == WITHDRAWN ||
                this == EXPIRED;
    }

    public boolean requiresStudentAction() {
        return this == DOCUMENTS_REQUIRED ||
                this == PAYMENT_PENDING ||
                this == EXAM_SCHEDULED;
    }

    public boolean requiresAdminAction() {
        return this == PENDING_REVIEW ||
                this == PAYMENT_VERIFICATION ||
                this == PENDING_FINAL_APPROVAL ||
                this == EXAM_COMPLETED;
    }
}