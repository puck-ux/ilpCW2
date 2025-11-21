package uk.ac.ed.acp.cw2.DataObjects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoPosConfig {

    // initialise positions
    private Position position1;
    private Position position2;

    public TwoPosConfig(Position pos1, Position pos2) {
        this.position1 = pos1;
        this.position2 = pos2;
    }
}
