package examination.teacherAndStudents.utils;

import lombok.Getter;

@Getter
public enum Purpose {
    TUITION("Tuition Fee"),
    APPLICATION_FEE("Application Fee"),
    REGISTRATION("Registration"),
    EXAMINATION("Examination"),
    APPLICATION("Application"),
    ADMISSION("Admission"),
    LIBRARY("Library"),
    SPORTS("Sports"),
    UNIFORM("Uniform"),
    TRANSPORT("Transport"),
    DEVELOPMENT("Development"),
    TECHNOLOGY("Technology"),
    BOARDING("Boarding"),
    MEDICAL("Medical"),
    ACTIVITY("Activity"),
    LATE("Late Payment"),
    STORE_PURCHASE("Store Purchase"),
    OTHER("Other");


    private final String displayName;

    Purpose(String displayName) {
        this.displayName = displayName;
    }

}