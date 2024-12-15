package examination.teacherAndStudents.error_handler;
public class LessonNoteNotFoundException extends RuntimeException{
    public LessonNoteNotFoundException(String message) {
        super(message);
    }
}