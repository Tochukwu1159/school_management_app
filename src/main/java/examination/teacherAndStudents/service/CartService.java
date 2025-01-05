package examination.teacherAndStudents.service;

import examination.teacherAndStudents.dto.CartRequest;
import examination.teacherAndStudents.dto.CartResponse;

import java.util.List;

public interface CartService {
    void removeFromCart(Long cartId);
    List<CartResponse> getCartForProfile(Long profileId);
    CartResponse addToCart(Long profileId, CartRequest request);
}
