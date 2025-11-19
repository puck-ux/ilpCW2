package uk.ac.ed.acp.cw2.DataObjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Drones {

    private String name;
    private String id;
    private Capability capability;

    @Getter
    @Setter
    public static class Capability {
        private Boolean cooling;
        private Boolean heating;
        private Long capacity;
        private Long maxMoves;
        private Long costPerMove;
        private Long costInitial;
        private Long costFinal;
    }

}
