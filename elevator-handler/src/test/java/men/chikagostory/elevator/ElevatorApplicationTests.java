package men.chikagostory.elevator;

import men.chikagostory.elevator.api.ElevatorsApi;
import men.chikagostory.elevator.ui.vaadin.SimulationView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ElevatorApplicationTests {

    @Autowired
    private ElevatorsApi elevatorsApi;

    @Test
    public void contextLoads() {
        Assert.notNull(elevatorsApi, "api can't be null");
    }

}
