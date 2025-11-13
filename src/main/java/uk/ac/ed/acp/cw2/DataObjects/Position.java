package uk.ac.ed.acp.cw2.DataObjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Position {

    // initialise lng/lat using Double over double so that null entry isn't set to 0
    private Double lng;
    private Double lat;


}
