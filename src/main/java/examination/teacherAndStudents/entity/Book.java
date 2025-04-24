package examination.teacherAndStudents.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(min = 1, max = 255)
    private String title;

    @NotNull
    @Size(min = 1, max = 255)
    private String author;

    @NotNull
    @Pattern(regexp = "^(?:[A-Z]+|[0-9]+|[A-Z]+[0-9]+)$", message = "Shelf location must be all letters (e.g., AAAA), all numbers (e.g., 333333), or letters followed by numbers (e.g., A1, A11111)")
    private String shelfLocation;

    @Min(0)
    @Column(nullable = false)
    private int quantityAvailable;

    @Min(0)
    @Column(nullable = false)
    private int totalCopies;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, columnDefinition = "BOOLEAN default false")
    private boolean archived;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setQuantityAvailable(int quantityAvailable) {
        if (quantityAvailable < 0) {
            throw new IllegalArgumentException("Quantity available cannot be negative");
        }
        if (quantityAvailable > totalCopies) {
            throw new IllegalArgumentException("Quantity available cannot exceed total copies");
        }
        this.quantityAvailable = quantityAvailable;
    }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies < 0) {
            throw new IllegalArgumentException("Total copies cannot be negative");
        }
        if (totalCopies < quantityAvailable) {
            throw new IllegalArgumentException("Total copies cannot be less than quantity available");
        }
        this.totalCopies = totalCopies;
    }
}