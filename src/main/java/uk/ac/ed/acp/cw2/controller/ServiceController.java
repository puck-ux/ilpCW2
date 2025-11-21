package uk.ac.ed.acp.cw2.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.DataObjects.*;
import uk.ac.ed.acp.cw2.service.*;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Controller class that handles various HTTP endpoints for the application.
 * Provides functionality for serving the index page, retrieving a static UUID,
 * and managing key-value pairs through POST requests.
 */
@RestController()
@RequestMapping("/api/v1")
public class ServiceController {


    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    private final String ilpEndpoint;
    private final URL serviceUrl;

    @Autowired
    public ServiceController(@Qualifier("ilpEndpoint") String ilpEndpoint) throws MalformedURLException {
        this.ilpEndpoint = ilpEndpoint;
        this.serviceUrl = new URL(ilpEndpoint);
    }


    @GetMapping("/showDrones")
    public List<Map<String, Object>> showDrones() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(ilpEndpoint + "/drones", List.class);
    }


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


    // COURSEWORK 2
    @GetMapping("/dronesWithCooling/{cooling}")
    public ArrayList<String> dronesWithCooling(@PathVariable boolean cooling) {
        return DroneCalc.dronesWithCoolingCalc(ilpEndpoint, cooling);
    }

    @GetMapping("/droneDetails/{id}")
    public Drones droneDetails(@PathVariable String id){
        return DroneCalc.droneDetailsCalc(ilpEndpoint, id);
    }

    @GetMapping("/queryAsPath/{attributeName}/{attributeValue}")
    public ArrayList<String> queryAsPath(@PathVariable String attributeName, @PathVariable String attributeValue){
        return DroneCalc.queryAsPathCalc(ilpEndpoint, attributeName, attributeValue, "=");
    }

    @PostMapping("/query")
    public ArrayList<String> query(@RequestBody List<queryFormat> input){
        return DroneCalc.queryCalc(ilpEndpoint, input);
    }

    @PostMapping("/queryAvailableDrones")
    public ArrayList<String> queryAvailableDrones(@RequestBody List<MedDispatchRec> input){
        return  DroneCalc.queryAvailableDronesCalc(ilpEndpoint, input);
    }

    @PostMapping("/calcDeliveryPath")
    public DeliveryPath calcDeliveryPath(@RequestBody List<MedDispatchRec> input){
        return DroneCalc.calcDeliveryPathCalc(ilpEndpoint, input);
    }

    @PostMapping("/calcDeliveryPathAsGeoJson")
    public Map<String, Object> calcDeliveryPathAsGeoJson(@RequestBody List<MedDispatchRec> input){
        return DroneCalc.calcDeliveryPathAsGeoJsonCalc(ilpEndpoint, input);
    }
}
