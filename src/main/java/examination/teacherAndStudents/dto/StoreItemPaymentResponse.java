
package examination.teacherAndStudents.dto;

import examination.teacherAndStudents.entity.StoreItem;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
@Builder
public class StoreItemPaymentResponse {
    // Getters and Setters
    private String paymentId;
    private double totalAmountPaid;
    private List<StoreItem> storeItems;
    private String paymentStatus;

    // Constructor
    public StoreItemPaymentResponse(String paymentId, double totalAmountPaid, List<StoreItem> storeItems, String paymentStatus) {
        this.paymentId = paymentId;
        this.totalAmountPaid = totalAmountPaid;
        this.storeItems = storeItems;
        this.paymentStatus = paymentStatus;
    }

}
