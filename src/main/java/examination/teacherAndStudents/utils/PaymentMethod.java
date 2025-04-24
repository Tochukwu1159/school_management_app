package examination.teacherAndStudents.utils;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("Cash"),
    BALANCE("Wallet Balance"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_MONEY("Mobile Money"),
    CREDIT_CARD("Credit Card"),
    CHEQUE("Cheque"),
    SCHOLARSHIP("Scholarship"),
    BANK_DEPOSIT("Bank Deposit"),
    ONLINE_PAYMENT("Online Payment"),
    PAYSTACK("Paystack"),
    FLUTTERWAVE("Flutterwave"),
    STRIPE("Stripe"),
    POS("POS Terminal"),
    OTHER("Other");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

}