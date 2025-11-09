package uk.ac.ed.acp.cw2.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.configuration.*;
import uk.ac.ed.acp.cw2.service.Calculations;

import java.net.URL;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
@RequestMapping("/api/v1")
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    @Value("${ilp.service.url}")
    public URL serviceUrl;


    @GetMapping("/")
    public String index() {
        return "<html><body>" +
                "<h1>Welcome from ILP</h1>" +
                "<h4>ILP-REST-Service-URL:</h4> <a href=\"" + serviceUrl + "\" target=\"_blank\"> " + serviceUrl+ " </a>" +
                "</body></html>";
    }


    // 2. GET for student id
    @GetMapping("/uid")
    public String uid() {
        return "s2179931";
    }


    // checks if a position is invalid and returns true if it is
    public boolean checkPosinValid(Position position){
        return (position == null || position.getLat() == null || position.getLng() == null);
    }

    // throws error 400
    public void throw400(){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }


    // 3. POST for calculating Euclidian distance between two positions
    @PostMapping("/distanceTo")
    // request body of post
    public Double distanceTo(@RequestBody TwoPosConfig input){
       return Calculations.distanceToCalc(input);
    }


    // 4. POST for calculating if two positions are close to each other
    @PostMapping("/isCloseTo")
    // request body of post
    public Boolean isCloseTo(@RequestBody TwoPosConfig input){
        // get distance between positions using distanceTo
        Double distance = distanceTo(input);
        // check closeness of positions
        return distance < 0.00015;
    }


    // 5. POST for calculating the next position using current pos and angle
    @PostMapping("/nextPosition")
    public Position nextPosition(@RequestBody DroneMovement input){
        return Calculations.nextPositionCalc(input);
    }


    // 6. Checks whether a position is in a region
    @PostMapping("/isInRegion")
    public Boolean isInRegion(@RequestBody RegionFormat input){
        return Calculations.isInRegionCalc(input);
    }

    /*
    ADD UNIT TESTS
    EXCEPTION HANDLING CLASS
    MAKE COMMENTS INCLUDE PARAMETER DESCRIPTIONS
     */
}
