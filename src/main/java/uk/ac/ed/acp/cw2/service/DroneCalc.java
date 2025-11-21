package uk.ac.ed.acp.cw2.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.acp.cw2.DataObjects.*;

import java.time.LocalDate;
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

    // returns list of restricted areas
    public static List<RestrictedAreas> getRestrictedAreas(String endpoint) {
        RestTemplate restTemplate = new RestTemplate();

        // parse JSON as list of a custom class that has vertices as VertexDTO
        ResponseEntity<List<RestrictedAreas>> responseEntity = restTemplate.exchange(
                endpoint + "/restricted-areas",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        List<RestrictedAreas> dtoList = responseEntity.getBody();

        // convert to RestrictedAreas with clean Position vertices
        return dtoList.stream().map(dto -> {
            RestrictedAreas area = new RestrictedAreas();
            area.setId(dto.getId());
            area.setName(dto.getName());
            area.setLimits(dto.getLimits());
            List<Position> positions = dto.getVertices().stream()
                    .map(v -> new Position(v.getLng(), v.getLat())) // ignore alt
                    .collect(Collectors.toList());
            area.setVertices(positions);
            return area;
        }).collect(Collectors.toList());
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
                            return new Position(servicecoords.getLocation().getLng(), servicecoords.getLocation().getLat());
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isPositionValid(Position pos, List<RestrictedAreas> restrictedAreas) {
        System.out.println("[DEBUG] Checking validity of position: " + pos.getLat() + ", " + pos.getLng());

        for (RestrictedAreas area : restrictedAreas) {
            RegionFormat rf = new RegionFormat();
            rf.setPosition(pos);

            Region region = new Region();
            region.setVertices(area.getVertices());
            region.setName(area.getName());
            rf.setRegion(region);

            if (Calculations.isInRegionCalc(rf)) {
                System.out.println("[DEBUG] Position is inside restricted area: " + area.getName());
                return false;
            }
        }

        System.out.println("[DEBUG] Position valid.");
        return true;
    }

    // Allowed movement angles
    private static final double[] ALLOWED_ANGLES = {
            0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
            180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5
    };

    public static Position nextStep(Position current, Position destination, List<RestrictedAreas> restricted, Position lastPosition) {

        Position bestMove = null;
        double bestScore = Double.MAX_VALUE;

        // Calculate main angle to destination
        double mainAngle = Math.toDegrees(Math.atan2(destination.getLat() - current.getLat(),
                destination.getLng() - current.getLng()));
        if (mainAngle < 0) mainAngle += 360;

        // Snap to nearest allowed angle
        double snapped = ALLOWED_ANGLES[0];
        double diffMin = Math.abs(mainAngle - snapped);
        for (double angle : ALLOWED_ANGLES) {
            double diff = Math.abs(mainAngle - angle);
            if (diff < diffMin) {
                diffMin = diff;
                snapped = angle;
            }
        }

        double[] angleOffsets = {0, -45, -22.5, 22.5, 45, -67.5, 67.5, -90, 90, -112.5, 112.5, -135, 135, -157.5, 157.5};

        for (double offset : angleOffsets) {
            double angle = snapped + offset;
            if (angle >= 360) angle -= 360;
            if (angle < 0) angle += 360;

            DroneMovement move = new DroneMovement();
            move.setStart(current);
            move.setAngle(angle);

            Position nextPos = Calculations.nextPositionCalc(move);

            // Skip if blocked by restricted areas
            if (!isPositionValid(nextPos, restricted)) continue;

            // Skip if too close to last position (avoid backtracking)
            double distanceToLast = Calculations.distanceToCalc(new TwoPosConfig(lastPosition, nextPos));
            if (distanceToLast < 0.00005) continue;

            // Score = distance to destination
            double distToDest = Calculations.distanceToCalc(new TwoPosConfig(nextPos, destination));

            if (distToDest < bestScore) {
                bestScore = distToDest;
                bestMove = nextPos;
            }
        }

        return bestMove;
    }


    public static PathResult computePath(
            Position start, Position destination, List<RestrictedAreas> restricted) {

        System.out.println("\n[DEBUG] === computePath() ===");
        System.out.println("[DEBUG] Start: " + start.getLat() + ", " + start.getLng());
        System.out.println("[DEBUG] Destination: " + destination.getLat() + ", " + destination.getLng());

        if (start == null || destination == null)
            throw new IllegalArgumentException("Start or destination cannot be null.");

        if (start.getLat() == null || start.getLng() == null ||
                destination.getLat() == null || destination.getLng() == null)
            throw new IllegalArgumentException("Lat/Lng cannot be null.");

        List<Position> path = new ArrayList<>();
        path.add(start);

        int moves = 0;

        Position lastPosition = new Position(0.0, 0.0);
        Position current = new Position(start.getLng(), start.getLat());

        TwoPosConfig cfg = new TwoPosConfig(current, destination);


        int safety = 0;

        while (Calculations.distanceToCalc(cfg) > 0.00015) {
            System.out.println("\n[DEBUG] Loop iteration " + moves);
            System.out.println("[DEBUG] Current distance: " + Calculations.distanceToCalc(cfg));

            Position next = nextStep(current, destination, restricted, lastPosition);

            if (next == null) {
                System.out.println("[DEBUG] Next step is null → stuck!");
                break;
            }

            if (next.equals(current)) {
                System.out.println("[DEBUG] Next equals current → no progress → breaking");
                break;
            }

            System.out.println("[DEBUG] Moving to: " + next.getLat() + ", " + next.getLng());

            path.add(next);
            lastPosition = current;
            current = next;
            cfg.setPosition1(next);
            moves++;

            if (safety++ > 20000) {
                System.out.println("[DEBUG] Safety limit exceeded — aborting path.");
                break;
            }
        }

        if (!current.equals(destination)) {
            System.out.println("[DEBUG] Adding final destination to path.");
            path.add(destination);
            moves++;
        }

        System.out.println("[DEBUG] Path complete. Total moves = " + moves);
        return new PathResult(path, moves);
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
                                            Position start = new Position(servicePointCoord.getLocation().getLng(), servicePointCoord.getLocation().getLat());

                                            // make TwoposConfig
                                            TwoPosConfig cfg = new  TwoPosConfig(start, medRecord.getDelivery());

                                            //calculate distance times by two since every drone must go back
                                            Double numbMoves = 2*((Calculations.distanceToCalc(cfg))/0.00015);
                                            System.out.println("NUMBER OF MOVES " + numbMoves);
                                            // check location is valid with cost and moves
                                            if(medRecord.getRequirements().getMaxCost() != null){
                                                System.out.println("TRIGGERING");
                                                if((numbMoves*drone.getCapability().getCostPerMove() + drone.getCapability().getCostFinal() + drone.getCapability().getCostInitial()) > medRecord.getRequirements().getMaxCost()) {
                                                    break;
                                                }
                                            }
                                            // drone either passes cost or there is no maxcost
                                            if( numbMoves <= drone.getCapability().getMaxMoves()) {
                                                // check requirements
                                                MedDispatchRec.Requirements requirements = medRecord.getRequirements();
                                                System.out.println("drone capacity for drone " + drone.getId() + " is: " + capability.getCapacity() + " medrecord capability " + requirements.getCapacity());
                                                if (requirements.getCooling() != null && requirements.getHeating() != null) {
                                                    if ((requirements.getCooling() ? capability.getCooling() : true) &&
                                                            (requirements.getHeating() ? capability.getHeating() : true) &&
                                                            capability.getCapacity() >= requirements.getCapacity()) {
                                                        count++;
                                                    }
                                                }else if(capability.getCapacity() >= requirements.getCapacity()){
                                                    count++;
                                                }

                                            }else break;
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


    public static DeliveryPath calcDeliveryPathMain(String endpoint, List<MedDispatchRec> medRecords) {

        System.out.println("\n==============================");
        System.out.println("calcDeliveryPathMain START");
        System.out.println("Incoming medRecords = " + medRecords.size());
        System.out.println("==============================\n");

        DeliveryPath result = new DeliveryPath();
        result.setDronePaths(new ArrayList<>());

        if (medRecords.isEmpty()) return result;

        ArrayList<String> availableDrones = queryAvailableDronesCalc(endpoint, medRecords);
        if (availableDrones.isEmpty()) return result;

        List<ServicePoints> servicePoints = getServicePoints(endpoint);
        List<ServicePointCoords> servicePointCoords = getServicePointCoords(endpoint);
        List<RestrictedAreas> restrictedAreas = getRestrictedAreas(endpoint);
        List<Drones> drones = getDrones(endpoint);

        Map<String, Drones> droneMap = drones.stream()
                .collect(Collectors.toMap(Drones::getId, d -> d));

        Set<Integer> completedDeliveries = new HashSet<>();
        double globalTotalCost = 0.0;
        double globalTotalMoves = 0.0;

        for (String droneID : availableDrones) {
            Position start = getDroneStartPosition(droneID, servicePointCoords, servicePoints);
            if (start == null) continue;

            double droneMaxMoves = droneMap.get(droneID).getCapability().getMaxMoves();
            double droneCapacity = droneMap.get(droneID).getCapability().getCapacity();
            double costPerMove = droneMap.get(droneID).getCapability().getCostPerMove();
            double costInitial = droneMap.get(droneID).getCapability().getCostInitial();
            double costFinal = droneMap.get(droneID).getCapability().getCostFinal();

            List<DeliveryPath.Delivery> droneDeliveries = new ArrayList<>();
            Position currentPos = start;
            double usedMoves = 0.0;
            double usedCostForTotals = 0.0;

            List<MedDispatchRec> sequenceDeliveries = new ArrayList<>();
            double sequenceCapacityUsed = 0.0;

            boolean keepTrying = true;

            while (keepTrying) {
                keepTrying = false;

                for (MedDispatchRec candidate : medRecords) {
                    if (completedDeliveries.contains(candidate.getId())) continue;
                    if (candidate.getDelivery() == null) continue;

                    double candidateCapacity = (candidate.getRequirements() != null && candidate.getRequirements().getCapacity() != null)
                            ? candidate.getRequirements().getCapacity() : 0.0;

                    boolean needsReturn = (sequenceCapacityUsed + candidateCapacity) > droneCapacity;

                    PathResult legPath;
                    PathResult returnPath;

                    if (needsReturn) {
                        // Drone must return to start first (no init/final added for this return)
                        legPath = computePath(start, candidate.getDelivery(), restrictedAreas);
                        returnPath = computePath(candidate.getDelivery(), start, restrictedAreas);
                        sequenceCapacityUsed = candidateCapacity;
                        sequenceDeliveries.clear();
                        currentPos = start;
                    } else {
                        legPath = computePath(currentPos, candidate.getDelivery(), restrictedAreas);
                        returnPath = computePath(candidate.getDelivery(), start, restrictedAreas);
                        sequenceCapacityUsed += candidateCapacity;
                    }


                    double legMoves = legPath.getMoves();
                    double retMoves = returnPath.getMoves();
                    double projectedMoves = usedMoves + legMoves + retMoves;
                    if (projectedMoves > droneMaxMoves) continue;

                    // Accept delivery
                    usedMoves += legMoves + retMoves;

                    double deliveryCost = legMoves * costPerMove + retMoves * costPerMove;

                    // Add initial/final only if this delivery starts a sequence (or after a return)
                    if (needsReturn || sequenceDeliveries.isEmpty()) {
                        deliveryCost += costInitial + costFinal;
                    }

                    usedCostForTotals += deliveryCost;

                    DeliveryPath.Delivery acceptedDelivery = new DeliveryPath.Delivery();
                    acceptedDelivery.setDeliveryId(candidate.getId());

                    List<Position> flight = new ArrayList<>();
                    flight.addAll(legPath.getPaths());
                    flight.addAll(returnPath.getPaths());
                    acceptedDelivery.setFlightPath(flight);

                    droneDeliveries.add(acceptedDelivery);
                    completedDeliveries.add(candidate.getId());

                    currentPos = candidate.getDelivery();
                    sequenceDeliveries.add(candidate);
                    keepTrying = true;
                    break; // restart scanning medRecords
                }
            }

            if (!droneDeliveries.isEmpty()) {
                DeliveryPath.DronePath dp = new DeliveryPath.DronePath();
                dp.setDroneId(droneID);
                dp.setDeliveries(droneDeliveries);
                result.getDronePaths().add(dp);

                globalTotalCost += usedCostForTotals;
                globalTotalMoves += usedMoves;
            }
        }

        result.setTotalCost(globalTotalCost);
        result.setTotalMoves(globalTotalMoves);

        System.out.println("\n==============================");
        System.out.println("FINAL totalCost = " + result.getTotalCost());
        System.out.println("FINAL totalMoves = " + result.getTotalMoves());
        System.out.println("==============================\n");

        return result;
    }



    public static DeliveryPath calcDeliveryPathCalc(String endpoint, List<MedDispatchRec> medRecords) {
        System.out.println("\n==============================");
        System.out.println("calcDeliveryPathByDay START");
        System.out.println("Incoming medRecords = " + medRecords.size());
        System.out.println("==============================\n");

        DeliveryPath result = new DeliveryPath();
        result.setDronePaths(new ArrayList<>());
        result.setTotalCost(0.0);
        result.setTotalMoves(0.0);

        if (medRecords.isEmpty()) return result;

        // group records by date
        Map<LocalDate, List<MedDispatchRec>> recordsByDate = medRecords.stream()
                .collect(Collectors.groupingBy(MedDispatchRec::getDate));

        for (LocalDate date : recordsByDate.keySet()) {
            System.out.println("\n[DEBUG] Processing deliveries for date: " + date);
            List<MedDispatchRec> dailyRecords = recordsByDate.get(date);

            // Reuse your existing single-day delivery logic
            DeliveryPath dailyPath = calcDeliveryPathMain(endpoint, dailyRecords);

            // Merge daily results into overall result
            if (dailyPath.getDronePaths() != null) {
                result.getDronePaths().addAll(dailyPath.getDronePaths());
            }
            result.setTotalCost(result.getTotalCost() + dailyPath.getTotalCost());
            result.setTotalMoves(result.getTotalMoves() + dailyPath.getTotalMoves());
        }

        System.out.println("\n==============================");
        System.out.println("FINAL CALC FUNCtotalCost = " + result.getTotalCost());
        System.out.println("FINAL totalMoves = " + result.getTotalMoves());
        System.out.println("==============================\n");

        return result;
    }



    public static Map<String, Object> convertDeliveryPathToGeoJson(DeliveryPath deliveryPath) {
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");

        List<Map<String, Object>> features = new ArrayList<>();

        for (DeliveryPath.DronePath dronePath : deliveryPath.getDronePaths()) {
            for (DeliveryPath.Delivery delivery : dronePath.getDeliveries()) {

                Map<String, Object> feature = new HashMap<>();

                feature.put("type", "Feature");

                // Properties (optional)
                Map<String, Object> properties = new HashMap<>();
                feature.put("properties", properties);


                // Geometry
                Map<String, Object> geometry = new HashMap<>();
                geometry.put("type", "LineString");

                List<List<Double>> coordinates = new ArrayList<>();
                for (Position pos : delivery.getFlightPath()) {
                    coordinates.add(Arrays.asList(pos.getLng(), pos.getLat()));
                }
                geometry.put("coordinates", coordinates);

                feature.put("geometry", geometry);
                features.add(feature);
            }
        }

        geoJson.put("features", features);
        return geoJson;
    }

    public static Map<String, Object> calcDeliveryPathAsGeoJsonCalc(String endpoint, List<MedDispatchRec> medRecords){
        return convertDeliveryPathToGeoJson(calcDeliveryPathCalc(endpoint, medRecords));
    }


}
