package examination.teacherAndStudents.utils;

public enum FeeStatus {
    UNPAID,         // No payments made
    PARTIALLY_PAID, // Some payment made but not full amount
    PAID,           // Fully paid
    OVERDUE,        // Payment not completed by due date
    WAIVED,         // Fee has been waived
    CANCELLED ,      // Fee has been cancelled
    PENDING,
}