package uk.ac.ed.acp.cw2.DataObjects;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class MedDispatchRec {

    private Long id;
    private LocalDate date;
    private LocalTime time;
    private Requirements requirements;
    private Position delivery;

    @Getter
    @Setter
    public static class Requirements {
        private Double capacity;
        private Boolean cooling;
        private Boolean heating;
        public Double maxCost;
    }

}
