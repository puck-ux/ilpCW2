package uk.ac.ed.acp.cw2.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.DataObjects.*;

import java.util.*;
import java.util.stream.Collectors;

public class DroneCalc {

    // --------------------------- HELPER FUNCTIONS ---------------------------

    // returns drones as List of Drones
    public static List<Drones> getDrones(String endpoint){
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List<Drones>> responseEntity = restTemplate.exchange(
                endpoint + "/drones",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return responseEntity.getBody();
    }

    public static boolean isValidLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public static boolean compareLongs(Long a, Long b, String operator) {
        return switch (operator) {
            case ">" -> a > b;
            case "<" -> a < b;
            case "=" -> a == b;
            case "!=" -> a != b;
            default -> false;
        };
    }

    public static boolean compareBool(Boolean a, Boolean b, String operator) {
        return switch (operator){
            case "=" -> a == b;
            case "!=" -> a != b;
            default -> false;
        };
    }

    public static ArrayList<Long> findCommonElements(List<List<Long>> listOfLists) {
        if (listOfLists == null || listOfLists.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> intersection = new HashSet<>(listOfLists.getFirst());
        for (int i = 1; i < listOfLists.size(); i++) {
            intersection.retainAll(listOfLists.get(i));
        }
        // Convert the set to an ArrayList before returning
        return new ArrayList<>(intersection);
    }

    // --------------------------- END OF HELPERS ---------------------------

    // returns drones with given cooling
    public static ArrayList<Long> dronesWithCoolingCalc(String endpoint, Boolean state){
        List<Drones> drones = getDrones(endpoint);
        ArrayList<Long> droneIDs = new ArrayList<>();
        for(Drones drone :  drones){
            if(drone.getCapability().getCooling() == state){
                droneIDs.add(drone.getId());
            }
        }
        return droneIDs;
    }

    // returns drone with given id
    public static Drones droneDetailsCalc(String endpoint, Long id){
        List<Drones> drones = getDrones(endpoint);
        Map<Long, Drones> droneMap = drones.stream().collect(Collectors.toMap(Drones::getId, drone -> drone));
        Drones foundDrone;
        if ((foundDrone = droneMap.get(id)) != null) {
            return foundDrone;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "404");
        }
    }


    // finds drones with attributes
    public static ArrayList<Long> queryAsPathCalc(String endpoint, String attributeName, String attributeValue, String operator){

        List<Drones> drones = getDrones(endpoint);
        ArrayList<Long> droneIDs = new ArrayList<>();

        // handle cooling and heating
        if((attributeValue.equals("true") || attributeValue.equals("false"))) {
            Boolean state = Boolean.parseBoolean(attributeValue);
            switch (attributeName){
                case "cooling":
                    for(Drones drone : drones){
                        if(compareBool(drone.getCapability().getCooling(), state, operator)){
                            droneIDs.add(drone.getId());
                        }
                    } break;
                case "heating":
                    for(Drones drone : drones){
                        if(compareBool(drone.getCapability().getHeating(), state, operator)){
                            droneIDs.add(drone.getId());
                        }
                    } break;
            }
            return droneIDs;
        }

        // handle not valid
        if(!isValidLong(attributeValue)){
            return droneIDs;
        }

        // handles cases of longs
        Long value = Long.parseLong(attributeValue);
        for(Drones drone :  drones){
            Drones.Capability capability = drone.getCapability();
            switch (attributeName){
                case "capacity":
                    if(compareLongs(capability.getCapacity(), value, operator)){
                        droneIDs.add(drone.getId());
                    } break;
                case "maxMoves":
                    if(compareLongs(capability.getMaxMoves(), value, operator)){
                        droneIDs.add(drone.getId());
                    } break;
                case "costPerMove":
                    if(compareLongs(capability.getCostPerMove(), value, operator)){
                        droneIDs.add(drone.getId());
                    } break;
                case "costInitial":
                    if(compareLongs(capability.getCostInitial(), value, operator)){
                        droneIDs.add(drone.getId());
                    } break;
                case "costFinal":
                    if(compareLongs(capability.getCostFinal(), value, operator)){
                        droneIDs.add(drone.getId());
                    } break;
            }
        }
        return droneIDs;
    }


    public static ArrayList<Long> queryCalc(String endpoint, List<queryFormat> queries){
        List<List<Long>> droneIDsList = new ArrayList<>();

        for(queryFormat query : queries){
            droneIDsList.add(queryAsPathCalc(endpoint, query.getAttribute(), query.getValue(), query.getOperator()));
        }

        return findCommonElements(droneIDsList);
    }
}
