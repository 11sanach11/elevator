package men.chikagostory.elevator;

import men.chikagostory.elevator.internal.domain.MotionInfo;
import men.chikagostory.elevator.model.Elevator;
import men.chikagostory.elevator.model.Position;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElevatorStubTest {
    private ElevatorStub elevatorStub;

    @BeforeEach
    public void beforeAll() {
        elevatorStub = new ElevatorStub(1, "1", 1000, 1, 50, 1, 2, Elevator.TypeEnum.PASSENGER);
    }

    @Test
    public void simpleTest() throws InterruptedException {
        elevatorStub.startEmulation();
        Thread.sleep(1000);
        elevatorStub.stopEmulation();
    }

    @Test
    public void beginFromSTAYTest() throws InterruptedException {
        AtomicInteger destFloor = new AtomicInteger();
        elevatorStub.setInitMotionFunc((firstFloor, lastFloor) -> {
            MotionInfo motionInfo = new MotionInfo();
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

    @AfterEach
    public void after() {
        elevatorStub.stopEmulation();
    }

}
