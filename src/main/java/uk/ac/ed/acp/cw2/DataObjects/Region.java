package uk.ac.ed.acp.cw2.DataObjects;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Region {

    private String name;
    private List<Position> vertices;

}
