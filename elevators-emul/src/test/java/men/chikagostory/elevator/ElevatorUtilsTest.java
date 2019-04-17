package men.chikagostory.elevator;

import com.google.common.collect.Lists;
import men.chikagostory.elevator.internal.ElevatorUtils;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import men.chikagostory.elevator.model.Position;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

public class ElevatorUtilsTest {

    private final LinkedList<Integer> queueDestination = Lists.newLinkedList();

    @BeforeEach
    public void beforeEach() {
        queueDestination.clear();
    }

    @Test
    public void addFloorInEmptyQueue() {
        ElevatorUtils.addDestinationFloor(queueDestination, 10, Position.StateEnum.STAY, DirectionForFloorDestination.NO_MATTER);
        Assertions.assertEquals(1, queueDestination.size(), "must be exactly one item");
    }
}
