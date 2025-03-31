package examination.teacherAndStudents.service;


import examination.teacherAndStudents.dto.FundWalletRequest;
import examination.teacherAndStudents.dto.PaymentResponse;
import examination.teacherAndStudents.dto.SchoolBalanceResponse;
import examination.teacherAndStudents.dto.WalletResponse;
import examination.teacherAndStudents.entity.Wallet;

import java.math.BigDecimal;


public interface WalletService {
    WalletResponse getProfileWalletBalance ();
    PaymentResponse fundWallet(FundWalletRequest fundWalletRequest) throws Exception;
SchoolBalanceResponse schoolTotalWallet();
//    void debit(Wallet wallet, BigDecimal amount);

}
