//package examination.teacherAndStudents.cloudinaryconfig;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import com.google.firebase.messaging.FirebaseMessaging;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//@Configuration
//public class FirebaseConfig {
//
//    @Value("${firebase.credentials.path}")
//    private String firebaseConfigPath;
//
//    @Bean
//    public FirebaseApp firebaseApp() throws IOException {
//        InputStream serviceAccount = new FileInputStream(firebaseConfigPath);
//        FirebaseOptions options = FirebaseOptions.builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .build();
//
//        if (FirebaseApp.getApps().isEmpty()) {
//            return FirebaseApp.initializeApp(options);
//        }
//        return FirebaseApp.getInstance();
//    }
//
//    @Bean
//    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
//        return FirebaseMessaging.getInstance(firebaseApp);
//    }
//}