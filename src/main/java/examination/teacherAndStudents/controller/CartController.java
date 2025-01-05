package examination.teacherAndStudents.controller;
import examination.teacherAndStudents.dto.CartRequest;
import examination.teacherAndStudents.dto.CartResponse;
import examination.teacherAndStudents.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/{profileId}/add")
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable Long profileId,
            @RequestBody CartRequest request) {
        CartResponse response = cartService.addToCart(profileId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<List<CartResponse>> getCartForProfile(@PathVariable Long profileId) {
        List<CartResponse> cartItems = cartService.getCartForProfile(profileId);
        return ResponseEntity.ok(cartItems);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartId) {
        cartService.removeFromCart(cartId);
        return ResponseEntity.noContent().build();
    }
}

