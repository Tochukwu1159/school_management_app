package examination.teacherAndStudents.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import examination.teacherAndStudents.entity.ServiceOffered;
import examination.teacherAndStudents.entity.User;
import examination.teacherAndStudents.utils.SubscriptionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Data
@AllArgsConstructor
public class SchoolDto {
   private long id;
  private   String name;
    List<ServiceOffered> selectedServices;
    private LocalDateTime subscriptionExpiryDate;
    private String schoolAddress;
    private String phoneNumber;
    private String subscriptionKey;
    private SubscriptionType subscriptionType;
}