package men.chikagostory.elevator.invoker;

import men.chikagostory.elevator.model.Elevator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmulApplicationTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @LocalServerPort
    private Integer serverPort;

    @Test
    public void simpleResponse() {
        Elevator elevator = testRestTemplate.getForEntity(String.format("http://localhost:%s/elevator/v1/elevators/1", serverPort), Elevator.class).getBody();
        assertNotNull(elevator, "object must be");
        assertEquals(1, elevator.getId().intValue(), "wrong identificator");
    }
}