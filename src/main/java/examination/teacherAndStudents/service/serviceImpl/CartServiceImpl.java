package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.CartRequest;
import examination.teacherAndStudents.dto.CartResponse;
import examination.teacherAndStudents.entity.Cart;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.StoreItem;
import examination.teacherAndStudents.repository.CartRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.StoreItemRepository;
import examination.teacherAndStudents.service.CartService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProfileRepository profileRepository;
    private final StoreItemRepository storeRepository;

    public CartServiceImpl(CartRepository cartRepository, ProfileRepository profileRepository, StoreItemRepository storeRepository) {
        this.cartRepository = cartRepository;
        this.profileRepository = profileRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public void removeFromCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        cartRepository.delete(cart);
    }

    @Override
    public List<CartResponse> getCartForProfile(Long profileId) {
        List<Cart> cartItems = cartRepository.findByProfileId(profileId);
        return cartItems.stream().map(this::mapToCartResponse).collect(Collectors.toList());
    }

    @Override
    public CartResponse addToCart(Long profileId, CartRequest request) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        StoreItem store = storeRepository.findById(request.getStoreItemId())
                .orElseThrow(() -> new RuntimeException("Store item not found"));

        Cart cart = new Cart();
        cart.setProfile(profile);
        cart.setStoreItem(store);
        cart.setSize(request.getSize());
        cart.setQuantity(request.getQuantity());

        cart = cartRepository.save(cart);

        return mapToCartResponse(cart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setStoreId(cart.getStoreItem().getId());
        response.setProfileId(cart.getProfile().getId());
        response.setItemName(cart.getStoreItem().getName());
        response.setItemPhoto(cart.getStoreItem().getPhotoUrl());
        response.setItemPrice(cart.getStoreItem().getPrice());
        response.setSize(cart.getSize());
        response.setQuantity(cart.getQuantity());
        response.setCheckedOut(cart.isCheckedOut());
        return response;
    }
}
