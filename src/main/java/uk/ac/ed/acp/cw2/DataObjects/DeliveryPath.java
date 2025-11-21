package uk.ac.ed.acp.cw2.DataObjects;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryPath {

    private Double totalCost;
    private Double totalMoves;
    private List<DronePath> dronePaths;

    @Getter
    @Setter
    public static class DronePath {
        private String droneId;
        private List<Delivery> deliveries;
    }

    @Getter
    @Setter
    public static class Delivery {
        private Integer deliveryId;
        private List<Position> flightPath;
    }
}
