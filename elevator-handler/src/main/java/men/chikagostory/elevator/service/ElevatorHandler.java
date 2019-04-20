package men.chikagostory.elevator.service;

import men.chikagostory.elevator.api.ElevatorsApi;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import men.chikagostory.elevator.model.Elevator;
import men.chikagostory.elevator.model.Position;
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

    /**
     * Функция должна посчитать примерное время ожидания лифта на этаже
     *
     * @param neededFloor          требуемый этаж
     * @param currentElevatorFloor текущий этаж, на котором находиться лифт
     * @param maxElevatorFloor     этаж, на котором лифт может сменить направление
     * @param neededDirection      требуемое направление движения лифта
     * @param currentElevatorState текущее направление движения лифта
     * @param speedByFloor         скорость движения лифта на этаж
     * @return примерное время ожидания
     */
    private int calculateElevatorWaitTime(int neededFloor, int currentElevatorFloor, int maxElevatorFloor, DirectionForFloorDestination neededDirection,
                                          Position.StateEnum currentElevatorState, int speedByFloor) {
//        if()
        return 1;
    }

    public void positionRequestForFloor(int floor, DirectionForFloorDestination direction) {
        log.trace("Position request   on floor: {} with direction: {}", floor, direction);
        try {
            Integer nearest = null;
            int minDuration = Integer.MAX_VALUE;
            for (Elevator elevator : elevatorsApi.getAll().execute().body()) {
                Position position = elevatorsApi.getPositionById(elevator.getId()).execute().body();

                elevatorsApi.setPositionForId(1, floor, Objects.toString(direction == null ? DirectionForFloorDestination.NO_MATTER : direction)).execute();
            }
        } catch (IOException e) {
            log.warn("Can't send floor request: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
