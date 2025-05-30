package examination.teacherAndStudents.cloudinaryconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone(); // Uses system default timezone
    }
}
