package examination.teacherAndStudents.utils;

public enum TransactionType {
    CREDIT,
    PAYMENT,
    DEBIT,// For fee payments or purchases
    REFUND,      // For refunds
    FINE,        // For penalties or fines
    SCHOLARSHIP, // For scholarships or grants
    OTHER
}
