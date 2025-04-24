package examination.teacherAndStudents.service.funding;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentProviderFactory {
    private final List<PaymentProvider> paymentProviders;

    public PaymentProvider getProvider(String providerName) {
        return paymentProviders.stream()
                .filter(provider -> provider.getClass().getSimpleName().toLowerCase().contains(providerName.toLowerCase()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No payment provider found for: " + providerName));
    }

    public PaymentProvider getProviderForWebhook(String providerName) {
        return paymentProviders.stream()
                .filter(provider -> provider.supportsWebhook(providerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No webhook handler found for: " + providerName));
    }
}
