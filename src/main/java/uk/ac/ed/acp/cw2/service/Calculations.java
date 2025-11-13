package uk.ac.ed.acp.cw2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.DataObjects.*;
import uk.ac.ed.acp.cw2.configuration.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Calculations {

    /* checks if a position is invalid and returns true if it is
    input parameters: position
    output: boolean
    */
    public static boolean checkPosinValid(Position position){
        return (position == null || position.getLat() == null || position.getLng() == null);
    }

    // throws error 400
    public static void throw400(){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "400");
    }

    public static Double distanceToCalc(@RequestBody TwoPosConfig input){

        // get positions from the input
        Position pos1 = input.getPosition1();
        Position pos2 = input.getPosition2();

        // check that all fields are not null and throws error 400 if they are
        if(checkPosinValid(pos1) || checkPosinValid(pos2)){
            throw400();
        }
        // check in range
        if(pos1.getLng() < -180 || pos1.getLng() > 180 || pos2.getLng() < -180 || pos2.getLng() > 180 || pos1.getLat() <  -90 || pos1.getLat() > 90 || pos2.getLat() < -90 || pos2.getLat() > 90){
            throw400();
        }

        // get distance between the x(lng) values
        double distanceX = pos1.getLng() - pos2.getLng();

        // get distance between the y(lat) values
        double distanceY = pos1.getLat() - pos2.getLat();

        // use pythagoras to get Euclidian distance and return it
        return Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
    }

    public static Position nextPositionCalc(@RequestBody DroneMovement input){
        double STEP = 0.00015;
        Position start = input.getStart();
        Double angle = input.getAngle();

        // handle angle that is not one of the 16 given in document
        if(angle % 22.5 > 0 || checkPosinValid(start) || angle > 360){
            throw400();
        }

        // get angle in radians
        double radAngle = Math.toRadians(angle);
        // update lng and lat with added distance from start
        start.setLng(start.getLng() + (STEP * Math.cos(radAngle)));
        start.setLat(start.getLat() + (STEP * Math.sin(radAngle)));

        return start;
    }

    private static Boolean OnLine(double poslng, double poslat, double lngi, double lati, double lngj, double latj, double tolerance){
        double cross = (poslat - lati) * (lngj - lngi) - (poslng - lngi) * (latj - lati);
        if (Math.abs(cross) > tolerance){
            return false;
        }
        double dot = (poslng - lngi) * (poslng - lngj) + (poslat - lati) * (poslat - latj);
        return dot <= tolerance;
    }

    // 6. Checks whether a position is in a region
    public static Boolean isInRegionCalc(@RequestBody RegionFormat input){
        boolean inside = false;
        Position position = input.getPosition();
        Region region = input.getRegion();
        // check that the vertices exist
        if (region == null || region.getVertices() == null) {
            throw400();
        }
        List<Position> vertices = region.getVertices();
        Position first = vertices.getFirst();
        Position last = vertices.getLast();
        // check validity of position and list length
        if(checkPosinValid(position) || vertices.size() != 5){
            throw400();
        } else if (!Objects.equals(first.getLat(), last.getLat()) ||
                !Objects.equals(first.getLng(), last.getLng())) {
            // make sure start and end are equal
            throw400();
        }

        // go through list and check each position is valid
        for (Position pos : vertices) {
            if(checkPosinValid(pos)){
                throw400();};
        }

        // get size and run for loop to check if inside the region
        int n = vertices.size();
        for(int i = 0, j = n - 1; i < n; j = i++){
            double lngi = vertices.get(i).getLng(), lati = vertices.get(i).getLat();
            double lngj = vertices.get(j).getLng(), latj = vertices.get(j).getLat();

            // check if on edge
            if(OnLine(position.getLng(), position.getLat(), lngi, lati, lngj, latj, 1e-9)){
                return true;
            }

            boolean intersect = ((lati > position.getLat()) != (latj > position.getLat())) &&
                    (position.getLng() < (lngj - lngi) * (position.getLat() - lati) / (latj  - lati) + lngi);
            if (intersect) inside = !inside;
        }

        return inside;
    }
}
