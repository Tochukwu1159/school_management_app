package examination.teacherAndStudents.dto;

import lombok.Data;

import java.util.List;

@Data
public class GoogleMapsResponse {
    private List<Route> routes;

    @Data
    public static class Route {
        private List<Leg> legs;
    }

    @Data
    public static class Leg {
        private Distance distance;
        private Duration duration;
    }

    @Data
    public static class Distance {
        private long value; // in meters
    }

    @Data
    public static class Duration {
        private String text; // e.g., "15 mins"
    }
}