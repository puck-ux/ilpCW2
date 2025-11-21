package uk.ac.ed.acp.cw2.DataObjects;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PathResult {

    private List<Position> paths;
    private Integer moves;

    public PathResult(List<Position> path, int moves) {
        this.paths = path;
        this.moves = moves;
    }
}
