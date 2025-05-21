package examination.teacherAndStudents.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "session_class")

public class SessionClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private AcademicSession academicSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_block_id", nullable = false)
    private ClassBlock classBlock;

    @ManyToMany
    @JoinTable(
            name = "session_class_profiles",
            joinColumns = @JoinColumn(name = "session_class_id"),
            inverseJoinColumns = @JoinColumn(name = "profile_id")
    )
    @Builder.Default
    private Set<Profile> profiles = new HashSet<>();


    @Column(name = "number_of_profiles", nullable = false)
    private int numberOfProfiles = 0;

    @ManyToMany
    @JoinTable(
            name = "session_class_assignments",
            joinColumns = @JoinColumn(name = "session_class_id"),
            inverseJoinColumns = @JoinColumn(name = "assignment_id")
    )
    @Builder.Default
    private Set<Assignment> assignments = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods for managing profiles
    public void addProfile(Profile profile) {
        if (profile != null && !profiles.contains(profile)) {
            profiles.add(profile);
            numberOfProfiles = profiles.size();
        }
    }

    public void removeProfile(Profile profile) {
        if (profile != null && profiles.contains(profile)) {
            profiles.remove(profile);
            numberOfProfiles = profiles.size();
        }
    }

    // Helper methods for managing assignments
    public void addAssignment(Assignment assignment) {
        if (assignment != null && !assignments.contains(assignment)) {
            assignments.add(assignment);
            assignment.getClassBlocks().add(this.classBlock);
        }
    }

    public void removeAssignment(Assignment assignment) {
        if (assignment != null && assignments.contains(assignment)) {
            assignments.remove(assignment);
            assignment.getClassBlocks().remove(this.classBlock);
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        numberOfProfiles = profiles.size();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        numberOfProfiles = profiles.size();
    }
}