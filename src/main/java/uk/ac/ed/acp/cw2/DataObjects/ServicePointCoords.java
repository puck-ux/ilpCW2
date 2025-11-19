package uk.ac.ed.acp.cw2.DataObjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicePointCoords {

    private String name;
    private Integer id;
    private ServicePos location;

    @Getter
    @Setter
    public static class ServicePos {
        private Double lng;
        private Double lat;
        private Double alt;
    }
}
