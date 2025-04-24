package examination.teacherAndStudents.service;
import examination.teacherAndStudents.dto.*;
import java.math.BigDecimal;


public interface WalletService {
    WalletResponse getProfileWalletBalance ();
    PaymentResponse fundWallet(FundWalletRequest fundWalletRequest) throws Exception;
SchoolBalanceResponse schoolTotalWallet();
    void handlePaymentWebhook(WebhookRequest webhookRequest);

    void creditWalletFromWebhook(String reference, BigDecimal amount, String ngn, String email, String gatewayResponse);

    String transferFunds(TransferRequest  transferRequest);
}
