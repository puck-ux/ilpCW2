package uk.ac.ed.acp.cw2.DataObjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServicePointCoords {

    private String name;
    private Long id;
    private ServicePos location;

    @Getter
    @Setter
    public static class ServicePos {
        private Long lng;
        private Long lat;
        private Long alt;
    }
}
