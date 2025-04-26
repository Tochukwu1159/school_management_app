package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.CartRequest;
import examination.teacherAndStudents.dto.CartResponse;
import examination.teacherAndStudents.dto.CheckoutResponse;
import examination.teacherAndStudents.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/check-out")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;


    @PostMapping()
    public ResponseEntity<CheckoutResponse> addToCart() {
        CheckoutResponse response = checkoutService.checkout();
        return ResponseEntity.ok(response);
    }

}
