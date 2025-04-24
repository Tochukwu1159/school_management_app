package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
//    private final TransferRecordRepository transferRecordRepository;
//    private final AuditLogRepository auditLogRepository;

    @Override
    public void recordSuccessfulTransfer(String transferId, BigDecimal amount,
                                         String currency, String recipientEmail,
                                         String description) {
//        TransferRecord record = TransferRecord.builder()
//                .transferId(transferId)
//                .amount(amount)
//                .currency(currency)
//                .recipientEmail(recipientEmail)
//                .status("COMPLETED")
//                .description(description)
//                .timestamp(LocalDateTime.now())
//                .build();
//
//        transferRecordRepository.save(record);

        // Audit log
//        auditLogRepository.save(AuditLog.builder()
//                .action("TRANSFER_SUCCESS")
//                .entityId(transferId)
//                .details("Amount: " + amount + " " + currency)
//                .timestamp(LocalDateTime.now())
//                .build());
    }

    @Override
    public void recordFailedTransfer(String transferId, String failureReason, String provider) {
//        TransferRecord record = TransferRecord.builder()
//                .transferId(transferId)
//                .status("FAILED")
//                .failureReason(failureReason)
//                .provider(provider)
//                .timestamp(LocalDateTime.now())
//                .build();
//
//        transferRecordRepository.save(record);
//
//        // Audit log
//        auditLogRepository.save(AuditLog.builder()
//                .action("TRANSFER_FAILED")
//                .entityId(transferId)
//                .details("Reason: " + failureReason)
//                .timestamp(LocalDateTime.now())
//                .build());
//    }
}}