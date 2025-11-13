package uk.ac.ed.acp.cw2.DataObjects;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestrictedAreas {

    private String name;
    private Long id;
    private Limits limits;
    private List<Position> vertices;

    @Getter
    @Setter
    public static class Limits{
        private Long upper;
        private Long lower;
    }
}
