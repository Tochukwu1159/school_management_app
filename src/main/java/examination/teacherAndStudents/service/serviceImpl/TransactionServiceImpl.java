package examination.teacherAndStudents.service.serviceImpl;

import examination.teacherAndStudents.Security.SecurityConfig;
import examination.teacherAndStudents.dto.TransactionResponse;
import examination.teacherAndStudents.entity.Profile;
import examination.teacherAndStudents.entity.Transaction;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.error_handler.CustomInternalServerException;
import examination.teacherAndStudents.error_handler.CustomNotFoundException;
import examination.teacherAndStudents.error_handler.NotFoundException;
import examination.teacherAndStudents.repository.ProfileRepository;
import examination.teacherAndStudents.repository.TransactionRepository;
import examination.teacherAndStudents.repository.UserRepository;
import examination.teacherAndStudents.service.TransactionService;
import examination.teacherAndStudents.utils.AccountUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ProfileRepository profileRepository;


    @Override
    public List<TransactionResponse> getProfileTransactions(int offset, int pageSize) throws Exception {
        try {
            String email = SecurityConfig.getAuthenticatedUserEmail();
            Optional<User> studentOptional = userRepository.findByEmail(email);

            if (studentOptional.isEmpty()) {
                throw new CustomNotFoundException("Profile with email " + email + " is not valid");
            }

            Profile profile = profileRepository.findByUser(studentOptional.get())
                    .orElseThrow(() -> new NotFoundException("Profile not found"));

            User student = studentOptional.get();
            Pageable pageable = PageRequest.of(offset, pageSize);
            Page<Transaction> pageList = transactionRepository.findTransactionByUserOrderByCreatedAtDesc(pageable, profile);

            List<TransactionResponse> transactionResponses = new ArrayList<>();

            pageList.forEach(page -> {
                TransactionResponse transactionResponse1 = TransactionResponse.builder()
                        .transactionType(page.getTransactionType().name())
                        .amount(page.getAmount())
                        .description(page.getDescription())
                        .createdAt(AccountUtils.localDateTimeConverter(page.getCreatedAt()))
                        .build();
                transactionResponses.add(transactionResponse1);
            });

            return transactionResponses;
        } catch (Exception ex) {
            throw new CustomInternalServerException("Error retrieving student transactions");
        }
    }

}



