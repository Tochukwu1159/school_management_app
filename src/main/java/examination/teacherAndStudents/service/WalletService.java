package examination.teacherAndStudents.service;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.School;

import java.math.BigDecimal;


public interface WalletService {
    WalletResponse getProfileWalletBalance ();
//    PaymentResponse fundWallet(FundWalletRequest fundWalletRequest) throws Exception;
SchoolBalanceResponse schoolTotalWallet();
    void handlePaymentWebhook(WebhookRequest webhookRequest);
    String fundWallet1(BigDecimal amount, Profile profile);

    void creditWalletFromWebhook(String reference, BigDecimal amount, String ngn, String email, String gatewayResponse);

    String transferFunds(TransferRequest  transferRequest);
}
