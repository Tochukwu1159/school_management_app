package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.dto.CheckoutResponse;
import examination.teacherAndStudents.entity.*;
import examination.teacherAndStudents.entity.StudentTerm;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.InsufficientBalanceException;
import examination.teacherAndStudents.error_handler.UnauthorizedException;
import examination.teacherAndStudents.repository.*;
import examination.teacherAndStudents.service.CheckoutService;
import examination.teacherAndStudents.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final ProfileRepository profileRepository;
    private final WalletRepository walletRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AcademicSessionRepository academicSessionRepository;
    private final StudentTermRepository studentTermRepository;
    private final StoreItemRepository storeItemRepository;

    @Override
    @Transactional
    public CheckoutResponse checkout() {
        // Validate student user
        User student = validateStudentUser();
        Profile profile = profileRepository.findByUser(student)
                .orElseThrow(() -> new CustomNotFoundException("Profile not found: " ));
        Wallet wallet = validateWallet(profile);

        // Fetch cart items
        List<Cart> cartItems = cartRepository.findByProfileIdAndCheckedOutFalse(profile.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Validate stock and calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cart : cartItems) {
            StoreItem storeItem = cart.getStoreItem();
            if (storeItem.getSizes() != null && cart.getSize() != null) {
                Integer availableQty = storeItem.getSizes().get(cart.getSize());
                if (availableQty == null || availableQty < cart.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient stock for item: " + storeItem.getName() + ", size: " + cart.getSize());
                }
            } else if (storeItem.getQuantity() != null) {
                if (cart.getSize() != null || storeItem.getQuantity() < cart.getQuantity()) {
                    throw new IllegalArgumentException("Insufficient stock for item: " + storeItem.getName());
                }
            } else {
                throw new IllegalArgumentException("Item has no stock information: " + storeItem.getName());
            }
            totalAmount = totalAmount.add(storeItem.getPrice().multiply(new BigDecimal(cart.getQuantity())));
        }

        // Validate wallet balance
        if (wallet.getBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance. Available: %s, Required: %s", wallet.getBalance(), totalAmount));
        }

        // Create order
        Order order = Order.builder()
                .profile(profile)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        // Create order items
        List<OrderItem> orderItems = cartItems.stream()
                .map(cart -> OrderItem.builder()
                        .order(order)
                        .storeItem(cart.getStoreItem())
                        .size(cart.getSize())
                        .quantity(cart.getQuantity())
                        .price(cart.getStoreItem().getPrice())
                        .build())
                .collect(Collectors.toList());
        order.setOrderItems(orderItems);

        // Save order
        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        // Update stock
        for (Cart cart : cartItems) {
            StoreItem storeItem = cart.getStoreItem();
            storeItem.reduceStock(cart.getSize(), cart.getQuantity());
            storeItemRepository.save(storeItem);
        }

        // Debit wallet
        wallet.debit(totalAmount);
        walletRepository.save(wallet);

        // Create payment
        Payment payment = Payment.builder()
                .amount(totalAmount)
                .paymentDate(LocalDate.now())
                .method(PaymentMethod.BALANCE)
                .referenceNumber(ReferenceGenerator.generateShortReference())
                .transactionId(ReferenceGenerator.generateTransactionId("BAL"))
                .profile(profile)
                .academicSession(getCurrentAcademicSession())
                .studentTerm(getCurrentStudentTerm())
                .status(examination.teacherAndStudents.utils.FeeStatus.PAID)
                .purpose(Purpose.STORE_PURCHASE)
                .paid(true)
                .fullyPaid(true)
                .build();

        paymentRepository.save(payment);

        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.DEBIT)
                .user(profile)
                .amount(totalAmount)
                .status(TransacStatus.SUCCESS)
                .session(getCurrentAcademicSession())
                .classBlock(profile.getClassBlock())
                .description("Store purchase for order ID: " + order.getId())
                .studentTerm(getCurrentStudentTerm())
                .build();

        transactionRepository.save(transaction);

        // Update cart items
        cartItems.forEach(cart -> cart.setCheckedOut(true));
        cartRepository.saveAll(cartItems);

        // Update order status
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        log.info("Checkout completed [orderId={}, profileId={}, totalAmount={}]", order.getId(), profile.getId(), totalAmount);

        return new CheckoutResponse(
                order.getId(),
                profile.getId(),
                totalAmount,
                payment.getTransactionId(),
                OrderStatus.COMPLETED.name()
        );
    }

    private User validateStudentUser() {
        String email = getAuthenticatedUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomNotFoundException("User not found with email: " + email));

        if (!user.getRoles().contains(examination.teacherAndStudents.utils.Roles.STUDENT)) {
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



    private Wallet validateWallet(Profile profile) {
        return walletRepository.findByUserProfileId(profile.getId())
                .orElseThrow(() -> new CustomNotFoundException("Wallet not found for profile ID: " + profile.getId()));
    }

    private AcademicSession getCurrentAcademicSession() {
        return academicSessionRepository.findCurrentSession1(LocalDate.now())
                .orElseThrow(() -> new CustomNotFoundException("No active academic session found"));
    }

    private StudentTerm getCurrentStudentTerm() {
        return studentTermRepository.findCurrentTerm(LocalDate.now())
                .orElseThrow(() -> new CustomNotFoundException("No active student term found"));
    }
}