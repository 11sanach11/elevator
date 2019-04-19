package men.chikagostory.elevator.service;

import men.chikagostory.elevator.api.ElevatorsApi;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class ElevatorHandler {

    private static final Logger log = LoggerFactory.getLogger(ElevatorHandler.class);

    private ElevatorsApi elevatorsApi;

    @Autowired
    public ElevatorHandler(ElevatorsApi elevatorsApi) {
        this.elevatorsApi = elevatorsApi;
    }

    public void positionRequestForElevator(int elevatorId, int floor, DirectionForFloorDestination direction) {
        log.trace("Position request for elevator: {} on floor: {} with direction: {}", elevatorId, floor, direction);
        try {
            elevatorsApi.setPositionForId(elevatorId, floor, Objects.toString(direction == null ? DirectionForFloorDestination.NO_MATTER : direction)).execute();
        } catch (IOException e) {
            log.warn("Can't send floor request: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void positionRequestForFloor(int floor, DirectionForFloorDestination direction) {
        log.trace("Position request for on floor: {} with direction: {}", floor, direction);
        try {
            elevatorsApi.setPositionForId(1, floor, Objects.toString(direction == null ? DirectionForFloorDestination.NO_MATTER : direction)).execute();
        } catch (IOException e) {
            log.warn("Can't send floor request: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
