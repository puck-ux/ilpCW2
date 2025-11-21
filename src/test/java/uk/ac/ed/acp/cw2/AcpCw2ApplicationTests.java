package uk.ac.ed.acp.cw2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;



@SpringBootTest
@AutoConfigureMockMvc
class AcpCw2ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {

    }

    // valid data
    @Test
    void testDistanceTo1() throws Exception {
        String json = """
        {
          "position1": { "lng": -3.192473, "lat": 55.946233 },
          "position2": { "lng": -3.192473, "lat": 55.942617 }
        }
        """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("0.003616000000000952"));
    }

    // invalid data: position 2 lng = string
    @Test
    void testDistanceTo2() throws Exception {
        String json = """
        {
          "position1": { "lng": -3.192473, "lat": 55.946233 },
          "position2": { "lng": "start", "lat": 55.942617 }
        }
        """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // valid data
    @Test
    void testisCloseTo1() throws Exception {
        String json = """
        {
          "position1": { "lng": -3.192473, "lat": 55.946233 },
          "position2": { "lng": -3.192473, "lat": 55.942617 }
        }
        """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // valid data
    @Test
    void testNextPosition1() throws Exception {
        String json = """
        {
          "start": { "lng": -3.192473, "lat": 55.946233 },
          "angle": 45
        }
        """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lng").exists())
                .andExpect(jsonPath("$.lat").exists());
    }

    // invalid data: invalid angle
    @Test
    void testNextPosition2() throws Exception {
        String json = """
        {
          "start": { "lng": -3.192473, "lat": 55.946233 },
          "angle": 13
        }
        """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // invalid data: no start position
    @Test
    void testNextPosition3() throws Exception {
        String json = """
        {
          "angle": 90
        }
        """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // valid data and point inside
    @Test
    void testIsInRegion1() throws Exception {
        String json = """
        {
            "position": {"lng": -3.188, "lat": 55.944},
            "region": {
                "name": "central",
                "vertices": [
                    {"lng": -3.192473, "lat": 55.946233},
                    {"lng": -3.192473, "lat": 55.942617},
                    {"lng": -3.184319, "lat": 55.942617},
                    {"lng": -3.184319, "lat": 55.946233},
                    {"lng": -3.192473, "lat": 55.946233}
                ]
            }
        }
        """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // valid data and point outside
    @Test
    void testIsInRegion2() throws Exception {
        String json = """
        {
            "position": {"lng": 1.234, "lat": 1.222},
            "region": {
                "name": "central",
                "vertices": [
                    {"lng": -3.192473, "lat": 55.946233},
                    {"lng": -3.192473, "lat": 55.942617},
                    {"lng": -3.184319, "lat": 55.942617},
                    {"lng": -3.184319, "lat": 55.946233},
                    {"lng": -3.192473, "lat": 55.946233}
                ]
            }
        }
        """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // invalid data: region doesn't close
    @Test
    void testIsInRegion3() throws Exception {
        String json = """
        {
            "position": {"lng": -3.188, "lat": 55.944},
            "region": {
                "name": "central",
                "vertices": [
                    {"lng": -3.192473, "lat": 55.946233},
                    {"lng": -3.192473, "lat": 55.942617},
                    {"lng": -3.184319, "lat": 55.942617},
                    {"lng": -3.184319, "lat": 55.946233}
                ]
            }
        }
        """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // invalid data: no position
    @Test
    void testIsInRegion4() throws Exception {
        String json = """
        {
            "region": {
                "name": "central",
                "vertices": [
                    {"lng": -3.192473, "lat": 55.946233},
                    {"lng": -3.192473, "lat": 55.942617},
                    {"lng": -3.184319, "lat": 55.942617},
                    {"lng": -3.184319, "lat": 55.946233},
                    {"lng": -3.192473, "lat": 55.946233}
                ]
            }
        }
        """;

        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCalcDeliveryPath() throws Exception {
        String json = """
    [
        {
            "id": 1,
            "date": "2025-01-01",
            "time": "14:30",
            "requirements": {
                "capacity": 0.01,
                "cooling": false,
                "heating": false,
                "maxCost": 1000.0
            },
            "delivery": {
                "lng": -3.189,
                "lat": 55.941
            }
        },
        {
            "id": 2,
            "date": "2025-01-02",
            "time": "14:30",
            "requirements": {
                "capacity": 0.01,
                "cooling": false,
                "heating": false,
                "maxCost": 1000.0
            },
            "delivery": {
                "lng": -3.180,
                "lat": 55.941
            }
        }
    ]
    """;

        mockMvc.perform(post("/api/v1/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dronePaths").isArray())
                .andExpect(jsonPath("$.dronePaths.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.dronePaths[0].droneId").isNotEmpty())
                .andExpect(jsonPath("$.dronePaths[0].deliveries").isArray())
                .andExpect(jsonPath("$.dronePaths[0].deliveries.length()").value(org.hamcrest.Matchers.greaterThan(0)))
                .andExpect(jsonPath("$.dronePaths[0].deliveries[0].deliveryId").isNumber())
                .andExpect(jsonPath("$.dronePaths[0].deliveries[0].flightPath").isArray())
                .andExpect(jsonPath("$.totalCost").exists())
                .andExpect(jsonPath("$.totalMoves").exists());
    }

}
