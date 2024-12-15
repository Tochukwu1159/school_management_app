package examination.teacherAndStudents.service;


import examination.teacherAndStudents.dto.FundWalletRequest;
import examination.teacherAndStudents.dto.PaymentResponse;
import examination.teacherAndStudents.dto.SchoolBalanceResponse;
import examination.teacherAndStudents.dto.WalletResponse;


public interface WalletService {
    WalletResponse getStudentWalletBalance ();
    PaymentResponse fundWallet(FundWalletRequest fundWalletRequest) throws Exception;
//    WalletResponse getTeacherWalletBalance();
SchoolBalanceResponse schoolTotalWallet();

}
