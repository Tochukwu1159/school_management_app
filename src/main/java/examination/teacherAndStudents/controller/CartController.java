package examination.teacherAndStudents.controller;

import examination.teacherAndStudents.dto.ApiResponse;
import examination.teacherAndStudents.dto.CartRequest;
import examination.teacherAndStudents.dto.CartResponse;
import examination.teacherAndStudents.service.CartService;
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

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestBody CartRequest request) {
        CartResponse response = cartService.addToCart(request);
        return ResponseEntity.ok(new ApiResponse<>("Item added to cart", true, response));
    }

    @GetMapping("/{profileId}")
    public ResponseEntity<ApiResponse<List<CartResponse>>> getCartForProfile(@PathVariable Long profileId) {
        List<CartResponse> cartItems = cartService.getCartForProfile(profileId);
        return ResponseEntity.ok(new ApiResponse<>("Cart items retrieved", true, cartItems));
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable Long cartId) {
        cartService.removeFromCart(cartId);
        return ResponseEntity.ok(new ApiResponse<>("Item removed from cart", true));
    }
}