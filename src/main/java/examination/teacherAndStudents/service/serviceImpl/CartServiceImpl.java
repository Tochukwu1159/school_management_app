package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.CartRequest;
import examination.teacherAndStudents.dto.CartResponse;
import examination.teacherAndStudents.entity.Cart;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.StoreItem;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.UnauthorizedException;
import examination.teacherAndStudents.repository.CartRepository;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.StoreItemRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.CartService;
import examination.teacherAndStudents.utils.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProfileRepository profileRepository;
    private final StoreItemRepository storeItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse addToCart(@NotNull Long profileId, @Valid CartRequest request) {
        User student = validateStudentUser();
        Profile profile = validateProfile(profileId, student);

        StoreItem storeItem = storeItemRepository.findById(request.getStoreItemId())
                .orElseThrow(() -> new CustomNotFoundException("Store item not found with ID: " + request.getStoreItemId()));

        validateSizeAndQuantity(storeItem, request.getSize(), request.getQuantity());

        Cart cart = Cart.builder()
                .profile(profile)
                .storeItem(storeItem)
                .size(request.getSize())
                .quantity(request.getQuantity())
                .checkedOut(false)
                .build();

        Cart savedCart = cartRepository.save(cart);
        log.info("Item added to cart [cartId={}, profileId={}, storeItemId={}]", savedCart.getId(), profileId, request.getStoreItemId());

        return mapToCartResponse(savedCart);
    }

    @Override
    @Transactional
    public void removeFromCart(@NotNull Long cartId) {
        User student = validateStudentUser();
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CustomNotFoundException("Cart item not found with ID: " + cartId));

        validateCartOwnership(student, cart);

        cartRepository.delete(cart);
        log.info("Item removed from cart [cartId={}]", cartId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartResponse> getCartForProfile(@NotNull Long profileId) {
        User student = validateStudentUser();
        Profile profile = validateProfile(profileId, student);

        List<Cart> cartItems = cartRepository.findByProfileId(profileId);
        log.debug("Retrieved {} cart items for profile [profileId={}]", cartItems.size(), profileId);

        return cartItems.stream()
                .map(this::mapToCartResponse)
                .collect(Collectors.toList());
    }

    private User validateStudentUser() {
        String email = getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found with email: " + email));

        if (!user.getRoles().contains(Roles.STUDENT)) {
            throw new UnauthorizedException("Please login as a Student");
        }

        return user;
    }

    private String getAuthenticatedUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            log.error("Failed to retrieve authenticated user email", e);
            throw new UnauthorizedException("Unable to authenticate user");
        }
    }

    private Profile validateProfile(Long profileId, User user) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new CustomNotFoundException("Profile not found with ID: " + profileId));

        if (!profile.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have access to this profile");
        }

        return profile;
    }

    private void validateCartOwnership(User student, Cart cart) {
        if (!cart.getProfile().getUser().getId().equals(student.getId())) {
            throw new UnauthorizedException("You do not have permission to modify this cart");
        }
    }

    private void validateSizeAndQuantity(StoreItem storeItem, String size, Integer quantity) {
        if (storeItem.getSizes() != null && !storeItem.getSizes().isEmpty()) {
            if (size == null || !storeItem.getSizes().containsKey(size)) {
                throw new IllegalArgumentException("Invalid size: " + size);
            }
            if (quantity <= 0 || quantity > storeItem.getSizes().get(size)) {
                throw new IllegalArgumentException("Invalid quantity: " + quantity + " for size: " + size);
            }
        } else if (storeItem.getQuantity() != null) {
            if (size != null) {
                throw new IllegalArgumentException("Size not applicable for this item");
            }
            if (quantity <= 0 || quantity > storeItem.getQuantity()) {
                throw new IllegalArgumentException("Invalid quantity: " + quantity);
            }
        } else {
            throw new IllegalArgumentException("Item has no stock information");
        }
    }

    private CartResponse mapToCartResponse(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getStoreItem().getId(),
                cart.getProfile().getId(),
                cart.getStoreItem().getName(),
                cart.getStoreItem().getPhotoUrl(),
                cart.getStoreItem().getPrice(),
                cart.getSize(),
                cart.getQuantity(),
                cart.isCheckedOut()
        );
    }
}