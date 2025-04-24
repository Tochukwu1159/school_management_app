package examination.teacherAndStudents.utils;

public enum TaskStatus {
    PENDING("pending"),
    IN_PROGRESS("in-progress"),
    DONE("done");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isValid(String status) {
        if (status == null) {
            return false;
        }
        for (TaskStatus ts : values()) {
            if (ts.value.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }
}