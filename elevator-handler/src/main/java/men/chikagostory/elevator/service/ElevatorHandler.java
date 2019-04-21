package men.chikagostory.elevator.service;

import men.chikagostory.elevator.api.ElevatorsApi;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import men.chikagostory.elevator.model.Position;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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

    /**
     * Функция должна посчитать примерное время ожидания лифта на этаже
     *
     * @param neededFloor              требуемый этаж
     * @param currentElevatorFloor     текущий этаж, на котором находиться лифт
     * @param maxElevatorFloor         этаж, на котором лифт может сменить направление
     * @param neededDirection          требуемое направление движения лифта
     * @param currentElevatorDirection текущее направление движения лифта
     * @param speedByFloor             скорость движения лифта на этаж
     * @return примерное время ожидания
     */
    private static int calculateElevatorWaitTime(int neededFloor, DirectionForFloorDestination neededDirection, int maxElevatorFloor, int currentElevatorFloor,
                                                 DirectionForFloorDestination currentElevatorDirection, int speedByFloor) {
        if (currentElevatorDirection == DirectionForFloorDestination.NO_MATTER) {//если текущее направление не задано - значит кабина стоит, и сразу начнет
            // движение до нужного этажа
            return Math.abs(neededFloor - currentElevatorFloor) * speedByFloor;
        } else if (currentElevatorDirection == neededDirection) {//если необходимое направление и текущее у лифта совпадает, то лифт тоже может быстро
            // добраться до нужного места
            if ((neededDirection == DirectionForFloorDestination.UP && currentElevatorFloor < neededFloor) ||
                    (neededDirection == DirectionForFloorDestination.DOWN && neededFloor < currentElevatorFloor)) {//если необходимый этаж дальше по движению
                return Math.abs(neededFloor - currentElevatorFloor) * speedByFloor;
            } else {//иначе для достижения нужного этажа нужно "перескочить" через экстремум
                return (Math.abs(maxElevatorFloor - currentElevatorFloor) + Math.abs(maxElevatorFloor - neededFloor)) * speedByFloor;
            }
        } else {//направления лифта и требуемое не совпадает - нужно перескочить через экстремум
            return (Math.abs(maxElevatorFloor - currentElevatorFloor) + Math.abs(maxElevatorFloor - neededFloor)) * speedByFloor;
        }
    }

    public void positionRequestForElevator(int elevatorId, int floor, DirectionForFloorDestination direction) {
        log.trace("Position request for elevator: {} on floor: {} with direction: {}", elevatorId, floor, direction);
        try {
            elevatorsApi.setPositionForId(elevatorId, floor, Objects.toString(direction == null ? DirectionForFloorDestination.NO_MATTER : direction)).execute();
        } catch (IOException e) {
            log.warn("Can't send floor request: {}", e.getMessage());
            throw new RuntimeException("Can't send floor request: {}", e);
        }
    }

    public void positionRequestForFloor(int floor, DirectionForFloorDestination direction) {
        log.trace("Position request on floor: {} with direction: {}", floor, direction);
        Assert.notNull(direction, "direction can't be null");
        Assert.isTrue(direction == DirectionForFloorDestination.UP ||
                direction == DirectionForFloorDestination.DOWN, "function works only for " +
                "directed destinations");
        try {
            elevatorsApi.getAll().execute().body().parallelStream().map(elevator -> {
                try {
                    Position position = elevatorsApi.getPositionById(elevator.getId()).execute().body();
                    int waitTime = calculateElevatorWaitTime(floor, direction, position.getMaxFloorInLoop(), position.getNextFloor(), position.getDirection(),
                            elevator.getSpeedByFloor());
                    log.trace("Wait time for #{} is {}", elevator.getId(), waitTime);
                    return Pair.of(elevator.getId(), waitTime);
                } catch (IOException e) {
                    throw new RuntimeException("Can't get info for " + elevator, e);
                }
            }).min((o1, o2) -> Integer.compare(o1.getRight(), o2.getValue())).ifPresent(pair -> {
                try {
                    log.trace("Call #{} with wait time: {}", pair.getLeft(), pair.getRight());
                    elevatorsApi.setPositionForId(pair.getLeft(), floor,
                            direction.toString()).execute();
                } catch (IOException e) {
                    throw new RuntimeException("Can't get info for " + pair.getLeft(), e);
                }
            });
        } catch (IOException e) {
            log.warn("Can't send floor request:", e);
            throw new RuntimeException("Can't send floor request:", e);
        }
    }
}
