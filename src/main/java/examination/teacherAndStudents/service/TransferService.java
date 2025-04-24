package examination.teacherAndStudents.service;

import java.math.BigDecimal;

public interface TransferService {
    void recordSuccessfulTransfer(String transferId, BigDecimal amount, String currency,
                                  String recipientEmail, String description);
    void recordFailedTransfer(String transferId, String failureReason, String provider);
}