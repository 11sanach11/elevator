package men.chikagostory.elevator.internal;

import men.chikagostory.elevator.internal.domain.MotionInfo;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import men.chikagostory.elevator.model.Position;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.function.BiFunction;

public class ElevatorUtils {

    /**
     * При инициализации лифта рандомно выбирается текущий этаж, направление движения (или режим остановки), а также этаж, на который лифт движется
     * (если он "оказался" в движении). Значения подбираются случайно, но со здравым смыслом (если лифт едет с пятого этажа вниз, то пунктом назначения
     * не может быть 7 этаж.
     */
    public static final BiFunction<Integer, Integer, MotionInfo> randomMotionInfo = (firstFloor, lastFloor) -> {
        MotionInfo info = new MotionInfo();
        int initialFloor = RandomUtils.nextInt(firstFloor, lastFloor + 1);
        int firstDestination;
        info.setPreviousFloor(initialFloor);
        boolean inMotion = RandomUtils.nextBoolean();
        if (inMotion) {
            if (initialFloor == firstFloor) {
                info.setState(Position.StateEnum.UP);
            } else if (initialFloor == lastFloor) {
                info.setState(Position.StateEnum.DOWN);
            } else {
                info.setState(RandomUtils.nextBoolean() ? Position.StateEnum.UP : Position.StateEnum.DOWN);
            }
            info.setNextFloor(initialFloor + (1 * (info.getState() == Position.StateEnum.UP ? 1 : -1)));
            if (info.getState() == Position.StateEnum.UP) {
                firstDestination = RandomUtils.nextInt(info.getPreviousFloor(), lastFloor + 1);
            } else {
                firstDestination = RandomUtils.nextInt(firstFloor, info.getPreviousFloor());
            }
        } else {
            info.setState(Position.StateEnum.STAY);
            info.setNextFloor(initialFloor);
            firstDestination = RandomUtils.nextInt(firstFloor, lastFloor + 1);
            while (firstDestination == info.getPreviousFloor()) {
                firstDestination = RandomUtils.nextInt(firstFloor, lastFloor + 1);
            }
        }
        info.getDestinationQueue().add(firstDestination);
        int countOfDestination = RandomUtils.nextInt(1, 5);
        while (info.getDestinationQueue().size() < countOfDestination) {
            int nextFloor = RandomUtils.nextInt(firstFloor, lastFloor + 1);
            if (nextFloor != firstDestination) {
                firstDestination = nextFloor;
                info.getDestinationQueue().add(nextFloor);
            }
        }
        return info;
    };
    private static final Logger log = LoggerFactory.getLogger(ElevatorUtils.class);

    private ElevatorUtils() {
        //do nothing
    }

    /**
     * Поведение, которое будет использоваться при добавлении нового этажа в очередь лифта. По умолчанию выполняется код из {@link ElevatorUtils}
     */
    public static void addDestinationFloor(LinkedList<Integer> destinationQueue, int addingFloor, Position.StateEnum currentState,
                                           DirectionForFloorDestination requestDirection) {
        //хитрый алгоритм добавления этажа по "пути", если это возможно, а пока просто добавляю этаж в конец, что конечно неправильно
        log.warn("хитрый алгоритм добавления этажа по \"пути\", если это возможно, а пока просто добавляю этаж в конец, что конечно неправильно");
        destinationQueue.add(addingFloor);
    }
}
