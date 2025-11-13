package uk.ac.ed.acp.cw2.DataObjects;

import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicePoints {

    private Long servicePointId;
    private List<DroneService> drones;

    @Getter
    @Setter
    public static class DroneService{
        private String id;
        private List<Day> availability;
    }

    @Getter
    @Setter
    public static class Day{
        private String dayOfWeek;
        private LocalTime from;
        private LocalTime until;
    }
}
