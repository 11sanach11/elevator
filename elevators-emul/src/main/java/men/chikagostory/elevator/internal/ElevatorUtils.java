package men.chikagostory.elevator.internal;

import com.google.common.collect.Lists;
import men.chikagostory.elevator.internal.domain.MotionInfo;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import men.chikagostory.elevator.model.Position;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ElevatorUtils {

    /**
     * При инициализации лифта рандомно выбирается текущий этаж, направление движения (или режим остановки), а также этаж, на который лифт движется
     * (если он "оказался" в движении). Значения подбираются случайно, но со здравым смыслом (если лифт едет с пятого этажа вниз, то пунктом назначения
     * не может быть 7 этаж.
     */
    public static final BiFunction<Integer, Integer, MotionInfo> randomMotionInfo = (firstFloor, lastFloor) -> {
        MotionInfo info = MotionInfo.builder().destinationQueue(Lists.newLinkedList()).build();
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
            info.setNextFloor(initialFloor + (info.getState() == Position.StateEnum.UP ? 1 : -1));
            if (info.getState() == Position.StateEnum.UP) {
                firstDestination = RandomUtils.nextInt(info.getNextFloor(), lastFloor + 1);
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
     * Добавить в очередь остановок лифта новый этаж
     *
     * @param destinationQueue очередь остановок для лифта
     * @param addingFloor      этаж, на котором требуется добавить остановку
     * @param currentFloor     текущий этаж, на котором находится кабина лифта
     * @param requestDirection направление движения, которое необходимо при достижении требуемого этажа
     */
    public static void addDestinationFloor(@NotNull List<Integer> destinationQueue, int addingFloor, int currentFloor,
                                           @NotNull DirectionForFloorDestination requestDirection) {
        // в пустую очередь достаточно добавить новую запись.
        if (CollectionUtils.isEmpty(destinationQueue)) {
            destinationQueue.add(addingFloor);
        } else {
            //если этаж уже в очереди - ничего добавлять не нужно
            boolean floorAlreadyInQueue = false;
            int i;
            Integer prevFloor = null;
            int floor1 = currentFloor;
            for (i = 0; i < destinationQueue.size(); i++) {
                int floor2 = destinationQueue.get(i);
                if (preferDirection(requestDirection, floor1, floor2)) {
                    if (Objects.equals(addingFloor, floor2)) {//заново добавлять этаж, на котором уже запланирована остановка, не нужно
                        floorAlreadyInQueue = true;
                        break;
                    }
                    if (destinationBetween(addingFloor, floor1, floor2)) {
                        break;
                    }
                }
                if (isAddNewExtremum(addingFloor, prevFloor, floor1, floor2)) {
                    floorAlreadyInQueue = isAddedFloorEqualsItEnvironment(addingFloor, floor1, floor2);
                    break;
                }
                // Третья остановка в пртивоположном направлении - не нужна.
                if (isAddedFloorEqualsItEnvironment(addingFloor, floor1, floor2)) {
                    floorAlreadyInQueue = true;
                    break;
                }
                prevFloor = floor1;
                floor1 = floor2;
            }
            if (!floorAlreadyInQueue) {
                destinationQueue.add(i, addingFloor);
            }
        }
        log.trace("Updated destination queue: {}", destinationQueue);
    }

    /**
     * Проверка что добавляемый этаж и его окружение - один и тот же этаж
     *
     * @param addingFloor добавляемый этаж
     * @param floor1      предыдущий в очереди
     * @param floor2      следующий в очереди
     * @return
     */
    private static boolean isAddedFloorEqualsItEnvironment(int addingFloor, int floor1, int floor2) {
        return addingFloor == floor1 && floor1 == floor2;
    }

    /**
     * Проверка того, что этаж назначения находится между другими
     *
     * @param destination проверяемый этаж
     * @param floor1
     * @param floor2
     * @return
     */
    private static boolean destinationBetween(int destination, int floor1, int floor2) {
        if (floor1 < floor2) {
            return floor1 < destination && destination < floor2;
        } else {
            return floor2 < destination && destination < floor1;
        }
    }

    /**
     * Направление, в котором сейчас движется кабина лифта, совпадает с тем, которое необходимо клиенту
     *
     * @param requiredDirection требуемое направление движения
     * @param floor1            этаж, с которого движется кабина лифта
     * @param floor2            этаж, на который движется кабина
     * @return
     */
    private static boolean preferDirection(DirectionForFloorDestination requiredDirection, int floor1, int floor2) {
        if (requiredDirection == DirectionForFloorDestination.NO_MATTER) {
            return true;
        } else {
            return requiredDirection == DirectionForFloorDestination.UP ? floor1 < floor2 : floor1 > floor2;
        }
    }

    /**
     * Проверить, можно ли добавить добавляемый этаж, сместив текущий экстремум
     *
     * @param destination      добавляемый этаж
     * @param wrappedPrevFloor -1 этаж в цепочке (для определения движения лифта), может быть null
     * @param floor1           этаж, с которого едет кабина
     * @param floor2           этаж, на который едет кабина
     * @return
     */
    private static boolean isAddNewExtremum(int destination, Integer wrappedPrevFloor, int floor1, int floor2) {
        if (wrappedPrevFloor != null) {
            int prevFloor = wrappedPrevFloor;
            if (inLocalMinimum(destination, floor1, floor2, prevFloor) || inLocalMaximum(destination, floor1, floor2, prevFloor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean inLocalMaximum(int destination, int floor1, int floor2, int prevFloor) {
        return (prevFloor < floor1 && floor1 <= destination && destination >= floor2) && !(prevFloor < floor1 && floor1 < floor2);
    }

    private static boolean inLocalMinimum(int destination, int floor1, int floor2, int prevFloor) {
        return (prevFloor > floor1 && floor1 >= destination && destination <= floor2) && !(prevFloor > floor1 && floor1 > floor2);
    }
}
