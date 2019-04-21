package men.chikagostory.elevator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import men.chikagostory.elevator.internal.ElevatorUtils;
import men.chikagostory.elevator.internal.domain.MotionInfo;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import men.chikagostory.elevator.model.Elevator;
import men.chikagostory.elevator.model.Position;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class ElevatorStubTest {
    private static final BiFunction<Integer, Integer, MotionInfo> emptyInitFunction =
            (firstFloor, lastFloor) -> MotionInfo.builder().state(Position.StateEnum.STAY).nextFloor(0).previousFloor(0).delay(0).id(1).destinationQueue(Lists.newLinkedList()).build();
    private ElevatorStub elevatorStub;

    @BeforeEach
    public void beforeAll() {
        elevatorStub = new ElevatorStub(1, "1", 1000, 0, 50, 1, 2, Elevator.TypeEnum.PASSENGER);
    }

    @Test
    public void beginFromSTAYTest() throws InterruptedException {
        AtomicInteger destFloor = new AtomicInteger();
        elevatorStub.setInitMotionFunc((firstFloor, lastFloor) -> {
            MotionInfo motionInfo = MotionInfo.builder().destinationQueue(Lists.newLinkedList()).build();
            destFloor.set(firstFloor + 1);
            motionInfo.getDestinationQueue().add(destFloor.get());
            motionInfo.setNextFloor(firstFloor);
            motionInfo.setPreviousFloor(firstFloor);
            motionInfo.setState(Position.StateEnum.STAY);
            return motionInfo;
        });
        CountDownLatch waitExecution = new CountDownLatch(3);
        elevatorStub.addNextStepListener(motionInfo -> waitExecution.countDown());
        elevatorStub.startEmulation();
        waitExecution.await(300, TimeUnit.MILLISECONDS);
        Position position = elevatorStub.getPosition();
        assertEquals(Position.StateEnum.STAY, position.getState(), "Wrong state for elevator");
        assertEquals(destFloor.get(), position.getNextFloor().intValue(), "Wrong floor");

    }

    @Test
    public void addNewDestinationTest1() {
        elevatorStub.setInitMotionFunc(emptyInitFunction);
        elevatorStub.pause();
        elevatorStub.startEmulation();
        elevatorStub.addNewDestination(1, DirectionForFloorDestination.NO_MATTER);
        elevatorStub.addNewDestination(1, DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(1), elevatorStub.getCurrentDestinationQueue(), "wrong destination queue");
    }

    @Test
    public void addNewDestinationTest2() {
        elevatorStub.setInitMotionFunc(emptyInitFunction);
        elevatorStub.pause();
        elevatorStub.startEmulation();
        elevatorStub.addNewDestination(1, DirectionForFloorDestination.NO_MATTER);
        elevatorStub.addNewDestination(2, DirectionForFloorDestination.NO_MATTER);
        elevatorStub.addNewDestination(1, DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(1, 2), elevatorStub.getCurrentDestinationQueue(), "wrong destination queue");
    }

    @Test
    public void multiThreadAddNewSingleDestinationUPTest() throws InterruptedException {
        ExecutorService service = Executors.newWorkStealingPool(10);
        elevatorStub.setInitMotionFunc(emptyInitFunction);
        elevatorStub.startEmulation();
        elevatorStub.pause();
        for (int number = 0; number < 10; number++) {
            service.submit(() -> elevatorStub.addNewDestination(4, DirectionForFloorDestination.UP));
        }
        service.shutdown();
        assertTrue(service.awaitTermination(2, TimeUnit.SECONDS), "operation must be executed for presented time");
        service.shutdownNow();
        assertEquals(ImmutableList.of(4), elevatorStub.getCurrentDestinationQueue(), "wrong destination queue");
        CountDownLatch countDownLatch = new CountDownLatch(6);
        elevatorStub.addNextStepListener(event -> countDownLatch.countDown());
        elevatorStub.resume();
        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS), "too long wait");
        assertTrue(elevatorStub.getCurrentDestinationQueue().isEmpty(), "the elevator destination queue must be empty");
        Position position = elevatorStub.getPosition();
        assertEquals(4, position.getNextFloor().intValue(), "wrong final floor");
        assertEquals(4, position.getPreviousFloor().intValue(), "wrong final floor");
        assertEquals(Position.StateEnum.STAY, position.getState(), "wrong state");
        assertEquals(DirectionForFloorDestination.NO_MATTER, position.getDirection(), "wrong direction");
    }

    @Test
    public void multiThreadAddNewSingleDestinationDownTest() throws InterruptedException {
        ExecutorService service = Executors.newWorkStealingPool(10);
        elevatorStub.setInitMotionFunc(emptyInitFunction);
        elevatorStub.startEmulation();
        elevatorStub.pause();
        for (int number = 0; number < 10; number++) {
            service.submit(() -> elevatorStub.addNewDestination(4, DirectionForFloorDestination.DOWN));
        }
        service.shutdown();
        assertTrue(service.awaitTermination(2, TimeUnit.SECONDS), "operation must be executed for presented time");
        service.shutdownNow();
        assertEquals(ImmutableList.of(4, 4), elevatorStub.getCurrentDestinationQueue(), "wrong destination queue");
        CountDownLatch countDownLatch = new CountDownLatch(6);
        elevatorStub.addNextStepListener(event -> countDownLatch.countDown());
        elevatorStub.resume();
        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS), "too long wait");
        assertTrue(elevatorStub.getCurrentDestinationQueue().isEmpty(), "the elevator destination queue must be empty");
        Position position = elevatorStub.getPosition();
        assertEquals(4, position.getNextFloor().intValue(), "wrong final floor");
        assertEquals(4, position.getPreviousFloor().intValue(), "wrong final floor");
        assertEquals(Position.StateEnum.STAY, position.getState(), "wrong state");
        assertEquals(DirectionForFloorDestination.NO_MATTER, position.getDirection(), "wrong direction");
    }

    @RepeatedTest(10)
    public void multiThreadAddNewDifferentDestinationTest() throws InterruptedException {
        ExecutorService service = Executors.newWorkStealingPool(10);
        elevatorStub.startEmulation();
        elevatorStub.pause();
        for (int number = 0; number < 10; number++) {
            service.submit(() -> elevatorStub.addNewDestination(RandomUtils.nextInt(elevatorStub.getElevatorModel().getFirstFloor(),
                    elevatorStub.getElevatorModel().getLastFloor()),
                    DirectionForFloorDestination.values()[RandomUtils.nextInt(0,
                            DirectionForFloorDestination.values().length)]));
        }
        service.shutdown();
        assertTrue(service.awaitTermination(2, TimeUnit.SECONDS), "operation must be executed for presented time");
        List<Integer> destination = elevatorStub.getCurrentDestinationQueue();
        assertTrue(!CollectionUtils.isEmpty(destination));
        int first = destination.get(0);
        int second = destination.get(1);
        for (int i = 2; i < destination.size(); i++) {
            assertFalse(Objects.equals(first, destination.get(i)) && Objects.equals(first, second), "two items in range can't be identity");
            first = second;
            second = destination.get(i);
        }
    }

    @RepeatedTest(10)
    public void concurrentElevatorMotionAndFloorAddition() throws InterruptedException {
        List<Integer> stayFloors = Lists.newArrayList();
        elevatorStub.addNextStepListener(event -> {
            if (event.getState() == Position.StateEnum.STAY) {
                stayFloors.add(event.getNextFloor());
            }
        });
        elevatorStub.startEmulation();
        ExecutorService service = Executors.newWorkStealingPool(10);
        for (int number = 0; number < 10; number++) {
            service.submit(() -> elevatorStub.addNewDestination(RandomUtils.nextInt(elevatorStub.getElevatorModel().getFirstFloor(),
                    elevatorStub.getElevatorModel().getLastFloor()),
                    DirectionForFloorDestination.values()[RandomUtils.nextInt(0,
                            DirectionForFloorDestination.values().length)]));
        }
        //жду пока очередь опустеет
        await().pollDelay(100, TimeUnit.MILLISECONDS).timeout(Duration.FIVE_SECONDS).until(() ->
                CollectionUtils.isEmpty(elevatorStub.getCurrentDestinationQueue()));
    }

    @RepeatedTest(10)
    public void serialElevatorMotionAndFloorAddition() throws InterruptedException {
        List<Integer> stayFloors = Lists.newArrayList();
        elevatorStub.addNextStepListener(event -> {
            if (event.getState() == Position.StateEnum.STAY) {
                stayFloors.add(event.getNextFloor());
            }
        });
        StringBuffer buffer = new StringBuffer();
        elevatorStub.setInitMotionFunc((firstFloor, lastFloor) -> {
            MotionInfo info = ElevatorUtils.randomMotionInfo.apply(firstFloor, lastFloor);
            buffer.append(String.format("\nMotionInfo.builder().id(%s).delay(%s).previousFloor(%s).nextFloor(%s).state(Position.StateEnum.%s)" +
                            ".destinationQueue(Lists.newLinkedList(ImmutableList.of(%s))).build();\n", info.getId(), info.getDelay(), info.getPreviousFloor(),
                    info.getNextFloor(), info.getState(),
                    StringUtils.join(info.getDestinationQueue(), ",")));
            return info;
        });
        elevatorStub.startEmulation();
        for (int number = 0; number < 10; number++) {
            int newFloor = RandomUtils.nextInt(elevatorStub.getElevatorModel().getFirstFloor() + 1,
                    elevatorStub.getElevatorModel().getLastFloor() - 1);
            DirectionForFloorDestination direction = DirectionForFloorDestination.values()[RandomUtils.nextInt(0,
                    DirectionForFloorDestination.values().length)];
            elevatorStub.addNewDestination(newFloor, direction);
            buffer.append(String.format("elevatorStub.addNewDestination(%s, DirectionForFloorDestination.%s);\n", newFloor, direction));
        }
        //жду пока очередь опустеет
        await(buffer.toString()).pollDelay(100, TimeUnit.MILLISECONDS).timeout(Duration.FIVE_SECONDS).until(() ->
                CollectionUtils.isEmpty(elevatorStub.getCurrentDestinationQueue()));
    }

    @Test
    public void bugWorkflowTest1() {
        ExecutorService service = Executors.newWorkStealingPool(10);
        elevatorStub.setInitMotionFunc(emptyInitFunction);
        elevatorStub.startEmulation();
        service.submit(() -> elevatorStub.addNewDestination(1, DirectionForFloorDestination.UP));
        service.submit(() -> elevatorStub.addNewDestination(2, DirectionForFloorDestination.UP));
        service.submit(() -> elevatorStub.addNewDestination(1, DirectionForFloorDestination.DOWN));
        //жду пока очередь опустеет
        await().pollDelay(100, TimeUnit.MILLISECONDS).timeout(Duration.ONE_SECOND).until(() ->
                CollectionUtils.isEmpty(elevatorStub.getCurrentDestinationQueue()));
    }


    @AfterEach
    public void after() {
        elevatorStub.stopEmulation();
    }

}
