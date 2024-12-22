package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.TransactionResponse;
import examination.teacherAndStudents.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/transaction")
public class TransactionController {


    private final TransactionService transactionService;

    @GetMapping("/student")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getStudentTransactions(
            @RequestParam("offset") int offset,
            @RequestParam("pageSize") int pageSize
    ) throws Exception {
       List<TransactionResponse> transactionEntities = transactionService.getProfileTransactions(offset,pageSize);
        return new ResponseEntity<>(new ApiResponse<>("success",true,transactionEntities), HttpStatus.OK);
    }

}
