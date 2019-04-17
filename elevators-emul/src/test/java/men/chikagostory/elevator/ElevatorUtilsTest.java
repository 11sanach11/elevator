package men.chikagostory.elevator;

import java.util.LinkedList;

import men.chikagostory.elevator.model.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import men.chikagostory.elevator.internal.ElevatorUtils;

public class ElevatorUtilsTest {

    private final LinkedList<Integer> queueDestination = Lists.newLinkedList();

    @BeforeEach
    public void beforeEach() {
        queueDestination.clear();
    }

    @Test
    public void addFloorInEmptyQueue() {
        ElevatorUtils.addDestinationFloor(queueDestination, 10, Position.StateEnum.STAY);
        Assertions.assertEquals(1, queueDestination.size(), "must be exactly one item");
    }
}
