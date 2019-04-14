package men.chikagostory.elevator.service;

import men.chikagostory.elevator.api.ElevatorsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ElevatorHandler {

    private static final Logger log= LoggerFactory.getLogger(ElevatorHandler.class);

    private ElevatorsApi elevatorsApi;

    @Autowired
    public ElevatorHandler(ElevatorsApi elevatorsApi) {
        this.elevatorsApi = elevatorsApi;
    }

    @PostConstruct
    private void postConstruct() {
        log.info("Get current state of all elevators...");
    }
}
