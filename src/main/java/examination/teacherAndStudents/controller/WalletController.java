package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.*;
import examination.teacherAndStudents.service.WalletService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final WalletService walletService;
    @PostMapping("/fund")
    public ResponseEntity<ApiResponse<PaymentResponse>> fundWallet(@RequestBody FundWalletRequest fundWalletRequest) throws Exception {
       PaymentResponse paymentResponse = walletService.fundWallet(fundWalletRequest);
        return new ResponseEntity<>(new ApiResponse<>("success",true,paymentResponse), HttpStatus.OK);
    }

    @GetMapping("student/balance")
    public ResponseEntity<ApiResponse<WalletResponse>> getProfileWalletBalance() {
       WalletResponse walletResponse = walletService.getProfileWalletBalance();
        return new ResponseEntity<>(new ApiResponse<>("success",true,walletResponse),HttpStatus.OK);
    }

    @GetMapping("school/balance")
    public ResponseEntity<ApiResponse<SchoolBalanceResponse>> schoolTotalWallet(){
        SchoolBalanceResponse schoolBalanceResponse = walletService.schoolTotalWallet();
        return  new ResponseEntity<>(new ApiResponse<>("success", true, schoolBalanceResponse), HttpStatus.OK);

    }



    @GetMapping("/transfer")
    public ResponseEntity<ApiResponse<String>> transferFunds(TransferRequest  transferRequest){
        String schoolBalanceResponse = walletService.transferFunds(transferRequest);
        return  new ResponseEntity<>(new ApiResponse<>("success", true, schoolBalanceResponse), HttpStatus.OK);

    }




}
