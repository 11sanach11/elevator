package men.chikagostory.elevator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import men.chikagostory.elevator.internal.ElevatorUtils;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import men.chikagostory.elevator.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElevatorUtilsTest {

    private final LinkedList<Integer> queueDestination = Lists.newLinkedList();

    @BeforeEach
    public void beforeEach() {
        queueDestination.clear();
    }

    @Test
    public void addFloorInEmptyQueueTest() {
        ElevatorUtils.addDestinationFloor(queueDestination, 10, Position.StateEnum.STAY, 5, DirectionForFloorDestination.NO_MATTER);
        assertEquals(1, queueDestination.size(), "must be exactly one item");
    }

    @Test
    public void addFloorNoMatterDirectionTest1() {
        queueDestination.addAll(ImmutableList.of(3, 5, 9));
        ElevatorUtils.addDestinationFloor(queueDestination, 2, Position.StateEnum.STAY, 1,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(2, 3, 5, 9), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest2() {
        queueDestination.addAll(ImmutableList.of(3, 5, 9));
        ElevatorUtils.addDestinationFloor(queueDestination, 6, Position.StateEnum.STAY, 4,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(3, 5, 6, 9), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest3() {
        queueDestination.addAll(ImmutableList.of(3, 5, 9, 7, 2));
        ElevatorUtils.addDestinationFloor(queueDestination, 6, Position.StateEnum.STAY, 4,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(3, 5, 6, 9, 7, 2), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest4() {
        queueDestination.addAll(ImmutableList.of(3, 5));
        ElevatorUtils.addDestinationFloor(queueDestination, 6, Position.StateEnum.STAY, 4,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(3, 5, 6), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest5() {
        queueDestination.addAll(ImmutableList.of(3));
        ElevatorUtils.addDestinationFloor(queueDestination, 5, Position.StateEnum.STAY, 4,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(3, 5), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest6() {
        queueDestination.addAll(ImmutableList.of(3, 5, 9));
        ElevatorUtils.addDestinationFloor(queueDestination, 5, Position.StateEnum.STAY, 4,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(3, 5, 9), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest7() {
        queueDestination.addAll(ImmutableList.of(9, 5, 3));
        ElevatorUtils.addDestinationFloor(queueDestination, 4, Position.StateEnum.STAY, 7,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(9, 5, 4, 3), queueDestination, "wrong destination queue");
    }

}
