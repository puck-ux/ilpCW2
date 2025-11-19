package uk.ac.ed.acp.cw2.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.DataObjects.*;

import java.time.LocalTime;
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

    // returns service points as List of ServicePoints
    public static List<ServicePoints> getServicePoints(String endpoint){
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List<ServicePoints>> responseEntity = restTemplate.exchange(
                endpoint + "/drones-for-service-points",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return responseEntity.getBody();
    }

    // returns restricted areas as List of RestrictedAreas
    public static List<RestrictedAreas> getRestrictedAreas(String endpoint){
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<List<RestrictedAreas>> responseEntity = restTemplate.exchange(
                endpoint + "/restricted-areas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );
        return responseEntity.getBody();
    }

    public static List<ServicePointCoords> getServicePointCoords(String endpoint){
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<ServicePointCoords>> responseEntity = restTemplate.exchange(
                endpoint + "/service-points",
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

    public static ArrayList<String> findCommonElements(List<List<String>> listOfLists) {
        if (listOfLists == null || listOfLists.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> intersection = new HashSet<>(listOfLists.getFirst());
        for (int i = 1; i < listOfLists.size(); i++) {
            intersection.retainAll(listOfLists.get(i));
        }
        // Convert the set to an ArrayList before returning
        return new ArrayList<>(intersection);
    }

    /*
    public static Map<String, List<ServicePoints.Day>> getDroneDays(List<ServicePoints> servicePoints) {
        Map<String, List<ServicePoints.Day>> droneDays = new HashMap<>();
        for (ServicePoints servicePoint : servicePoints) {
            for(ServicePoints.DroneService servDrone : servicePoint.getDrones()){
                droneDays.put(servDrone.getId(), servDrone.getAvailability());
            }
        }
        return droneDays;
    }
    */

    // get start pos of drone
    public static Position getDroneStartPosition(String droneId, List<ServicePointCoords> servicePointCoords, List<ServicePoints> servicePoints) {
        // go through points
        for(ServicePoints servicePoint: servicePoints){
            // go through drones
            for(ServicePoints.DroneService droneservice :  servicePoint.getDrones()){
                // if drone id is found
                if(droneId.equals(droneservice.getId())){
                    // go through service coords
                    for(ServicePointCoords servicecoords : servicePointCoords){
                        // if service point id equals that of the one the drone was found at
                        if(servicecoords.getId().equals(servicePoint.getServicePointId())){
                            // make position with coords from location
                            Position output = new Position();
                            output.setLng(servicecoords.getLocation().getLng());
                            output.setLat(servicecoords.getLocation().getLat());
                            return output;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isPositionValid(Position pos, List<RestrictedAreas> restrictedAreas) {
        for (RestrictedAreas area : restrictedAreas) {
            RegionFormat rf = new RegionFormat();
            rf.setPosition(pos);
            Region region = new Region();
            region.setVertices(area.getVertices());
            region.setName(area.getName());
            rf.setRegion(region);
            if (Calculations.isInRegionCalc(rf)) {
                return false; // cannot go here
            }
        }
        return true;
    }

    // calculate nextmove
    private static final double[] ALLOWED_ANGLES = {0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
            180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5};

    public static Position nextStep(Position current, Position destination, List<RestrictedAreas> restricted) {
        Position bestMove = null;
        double minDist = Double.MAX_VALUE;

        for (double angle : ALLOWED_ANGLES) {
            DroneMovement move = new DroneMovement();
            move.setStart(current);
            move.setAngle(angle);
            Position nextPos = Calculations.nextPositionCalc(move);

            if (!isPositionValid(nextPos, restricted)) continue;

            // calculate distance to destination
            TwoPosConfig cfg = new TwoPosConfig();
            cfg.setPosition1(nextPos);
            cfg.setPosition2(destination);
            double dist = Calculations.distanceToCalc(cfg);

            if (dist < minDist) {
                minDist = dist;
                bestMove = nextPos;
            }
        }
        return bestMove;
    }


    // --------------------------- END OF HELPERS ---------------------------

    // returns drones with given cooling
    public static ArrayList<String> dronesWithCoolingCalc(String endpoint, Boolean state){
        List<Drones> drones = getDrones(endpoint);
        ArrayList<String> droneIDs = new ArrayList<>();
        for(Drones drone :  drones){
            if(drone.getCapability().getCooling() == state){
                droneIDs.add(drone.getId());
            }
        }
        return droneIDs;
    }

    // returns drone with given id
    public static Drones droneDetailsCalc(String endpoint, String id){
        List<Drones> drones = getDrones(endpoint);
        Map<String, Drones> droneMap = drones.stream().collect(Collectors.toMap(Drones::getId, drone -> drone));
        Drones foundDrone;
        if ((foundDrone = droneMap.get(id)) != null) {
            return foundDrone;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "404");
        }
    }


    // finds drones with attributes
    public static ArrayList<String> queryAsPathCalc(String endpoint, String attributeName, String attributeValue, String operator){

        List<Drones> drones = getDrones(endpoint);
        ArrayList<String> droneIDs = new ArrayList<>();

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


    public static ArrayList<String> queryCalc(String endpoint, List<queryFormat> queries){
        List<List<String>> droneIDsList = new ArrayList<>();

        for(queryFormat query : queries){
            droneIDsList.add(queryAsPathCalc(endpoint, query.getAttribute(), query.getValue(), query.getOperator()));
        }

        return findCommonElements(droneIDsList);
    }



    public static ArrayList<String> queryAvailableDronesCalc(String endpoint, List<MedDispatchRec> medRecords){
        List<Drones> drones = getDrones(endpoint);
        List<ServicePoints> servicePoints = getServicePoints(endpoint);
        List<ServicePointCoords> servicePointCoords = getServicePointCoords(endpoint);
        ArrayList<String> droneIDs = new ArrayList<>();
        int numRecords = medRecords.size();

        for (Drones drone : drones) {
            Drones.Capability capability = drone.getCapability();
            int count = 0;
            for (MedDispatchRec medRecord : medRecords) {
                String medRecDay = medRecord.getDate().getDayOfWeek().toString().toUpperCase();
                LocalTime medRecTime = medRecord.getTime();

                // go through service points
                for (ServicePoints servicePoint : servicePoints) {
                    // go through drones
                    for(ServicePoints.DroneService droneLists : servicePoint.getDrones()) {
                        // get drone with id
                        if(droneLists.getId().equals(drone.getId())) {
                            // go through available days
                            for (ServicePoints.Day day : droneLists.getAvailability()) {
                                // check drone can deliver on day
                                if (day.getDayOfWeek().equals(medRecDay) && (!medRecTime.isBefore(day.getFrom()) && !medRecTime.isAfter(day.getUntil()))) {
                                    // go through service point coords to get position
                                    for(ServicePointCoords servicePointCoord : servicePointCoords) {
                                        if (servicePointCoord.getId().equals(servicePoint.getServicePointId())) {
                                            //create service pos
                                            Position start = new Position();
                                            start.setLng(servicePointCoord.getLocation().getLng());
                                            start.setLat(servicePointCoord.getLocation().getLat());

                                            // make TwoposConfig
                                            TwoPosConfig cfg = new  TwoPosConfig();
                                            cfg.setPosition1(start);
                                            cfg.setPosition2(medRecord.getDelivery());

                                            //calculate distance
                                            Double numbMoves = (Calculations.distanceToCalc(cfg))/0.00015;

                                            // check location is valid with cost and moves
                                            if(numbMoves*drone.getCapability().getCostPerMove() <= medRecord.getRequirements().getMaxCost()  && numbMoves <= drone.getCapability().getMaxMoves()) {
                                                // check requirements
                                                MedDispatchRec.Requirements requirements = medRecord.getRequirements();
                                                if ((requirements.getCooling() ? capability.getCooling() : true) &&
                                                        (requirements.getHeating() ? capability.getHeating() : true) &&
                                                        capability.getCapacity() >= requirements.getCapacity()) {
                                                    count++;
                                                } else break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
            if (count == numRecords) {
                droneIDs.add(drone.getId());
            }
        }
        return droneIDs;
    }


    public static DeliveryPath calcDeliveryPathCalc(String endpoint, List<MedDispatchRec> medRecords){
        DeliveryPath result = new DeliveryPath();
        ArrayList<String> availableDrones = queryAvailableDronesCalc(endpoint, medRecords);
        // check valid drones and that there are medRecords
        if (medRecords.isEmpty() || availableDrones.isEmpty()) return result;

        // get data from server
        List<ServicePoints> servicePoints = getServicePoints(endpoint);
        List<ServicePointCoords> servicePointCoords = getServicePointCoords(endpoint);
        List<RestrictedAreas> restrictedAreas = getRestrictedAreas(endpoint);

        Map<Integer, ServicePointCoords> spCoordsById = servicePointCoords.stream()
                .collect(Collectors.toMap(ServicePointCoords::getId, sp -> sp));

        Set<Integer> completedDeliveries = new HashSet<>();

        long totalCost = 0;
        long totalMoves = 0;

        for(String droneID : availableDrones) {
            for(MedDispatchRec medRecord : medRecords) {
                if(!completedDeliveries.contains(medRecord.getId())) {
                    Position destination = medRecord.getDelivery();
                    Position start = getDroneStartPosition(droneID, servicePointCoords, servicePoints);

                }
            }
        }



        return result;
    }
}
