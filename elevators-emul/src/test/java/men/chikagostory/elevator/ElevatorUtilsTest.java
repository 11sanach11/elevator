package men.chikagostory.elevator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import men.chikagostory.elevator.internal.ElevatorUtils;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ElevatorUtilsTest {

    private final LinkedList<Integer> queueDestination = Lists.newLinkedList();

    @BeforeEach
    public void beforeEach() {
        queueDestination.clear();
    }

    @Test
    public void addFloorInEmptyQueueTest() {
        ElevatorUtils.addDestinationFloor(queueDestination, 10, 5, DirectionForFloorDestination.NO_MATTER);
        assertEquals(1, queueDestination.size(), "must be exactly one item");
    }

    @Test
    public void addFloorNoMatterDirectionTest1() {
        queueDestination.addAll(ImmutableList.of(3, 5, 9));
        ElevatorUtils.addDestinationFloor(queueDestination, 2, 1,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(2, 3, 5, 9), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest2() {
        queueDestination.addAll(ImmutableList.of(3, 5, 9));
        ElevatorUtils.addDestinationFloor(queueDestination, 6, 4,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(3, 5, 6, 9), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest3() {
        queueDestination.addAll(ImmutableList.of(3, 5, 9, 7, 2));
        ElevatorUtils.addDestinationFloor(queueDestination, 6, 4,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(3, 5, 6, 9, 7, 2), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest4() {
        queueDestination.addAll(ImmutableList.of(3, 5));
        ElevatorUtils.addDestinationFloor(queueDestination, 6, 4,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(3, 5, 6), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest5() {
        queueDestination.addAll(ImmutableList.of(3));
        ElevatorUtils.addDestinationFloor(queueDestination, 5, 4,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(3, 5), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest6() {
        queueDestination.addAll(ImmutableList.of(3, 5, 9));
        ElevatorUtils.addDestinationFloor(queueDestination, 5, 4,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(3, 5, 9), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest7() {
        queueDestination.addAll(ImmutableList.of(9, 5, 3));
        ElevatorUtils.addDestinationFloor(queueDestination, 4, 7,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(9, 5, 4, 3), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorNoMatterDirectionTest8() {
        ElevatorUtils.addDestinationFloor(queueDestination, 1, 0,
                DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 2, 0,
                DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 1, 0,
                DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(1, 2), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorUpDirectionTest1() {
        queueDestination.addAll(ImmutableList.of(9, 5, 3));
        ElevatorUtils.addDestinationFloor(queueDestination, 2, 3,
                DirectionForFloorDestination.UP);
        assertEquals(ImmutableList.of(9, 5, 3, 2), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorUpDirectionTest2() {
        queueDestination.addAll(ImmutableList.of());
        ElevatorUtils.addDestinationFloor(queueDestination, 2, 3,
                DirectionForFloorDestination.UP);
        assertEquals(ImmutableList.of(2), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorUpDirectionTest3() {
        queueDestination.addAll(ImmutableList.of(9, 1, 7));
        ElevatorUtils.addDestinationFloor(queueDestination, 1, 4,
                DirectionForFloorDestination.UP);
        assertEquals(ImmutableList.of(9, 1, 1, 7), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorUpDirectionTest4() {
        queueDestination.addAll(ImmutableList.of(4, 5, 6, 7, 1));
        ElevatorUtils.addDestinationFloor(queueDestination, 6, 1,
                DirectionForFloorDestination.UP);
        assertEquals(ImmutableList.of(4, 5, 6, 7, 1), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorUpDirectionTest5() {
        queueDestination.addAll(ImmutableList.of(4, 5, 6, 8));
        ElevatorUtils.addDestinationFloor(queueDestination, 2, 1,
                DirectionForFloorDestination.UP);
        assertEquals(ImmutableList.of(2, 4, 5, 6, 8), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorUpDirectionTest6() {
        queueDestination.addAll(ImmutableList.of(4, 5, 6, 8));
        ElevatorUtils.addDestinationFloor(queueDestination, 2, 2,
                DirectionForFloorDestination.UP);
        assertEquals(ImmutableList.of(4, 5, 6, 8, 2), queueDestination, "wrong destination queue");
    }

    @Test
    public void addFloorDownDirectionTest1() {
        queueDestination.addAll(ImmutableList.of(4, 5, 6, 8));
        ElevatorUtils.addDestinationFloor(queueDestination, 2, 2,
                DirectionForFloorDestination.DOWN);
        assertEquals(ImmutableList.of(4, 5, 6, 8, 2), queueDestination, "wrong destination queue");
    }

    @RepeatedTest(10)
    public void addSerialFloorsTest() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < 10; i++) {
            DirectionForFloorDestination direction = DirectionForFloorDestination.values()[RandomUtils.nextInt(0,
                    DirectionForFloorDestination.values().length)];
            int addedFloor = RandomUtils.nextInt(0, 100);
            buffer.append(String.format("ElevatorUtils.addDestinationFloor(queueDestination, %s, 50, DirectionForFloorDestination.%s);\n", addedFloor,
                    direction));
            ElevatorUtils.addDestinationFloor(queueDestination, addedFloor, 50, direction);
        }
        int first = queueDestination.get(0);
        List<Integer> extremums = Lists.newArrayList(getLocalMinimum(queueDestination));
        extremums.addAll(getLocalMaximum(queueDestination));
        assertTrue(extremums.size() < 3, "two much extremums in range: \n" + extremums + "\n" + buffer);
    }

    private List<Integer> getLocalMinimum(List<Integer> input) {
        if (input.size() < 3) {
            return Lists.newArrayList(input.stream().min(Comparator.comparing(i -> i)).get());
        } else {
            List<Integer> result = Lists.newArrayList();
            int v1 = input.get(0);
            int v2 = input.get(1);
            int v3;
            for (int i = 2; i < input.size(); i++) {
                v3 = input.get(i);
                if (v1 > v2 && v3 > v2) {
                    result.add(v2);
                }
                v1 = v2;
                v2 = v3;
            }
            return result;
        }
    }

    private List<Integer> getLocalMaximum(List<Integer> input) {
        if (input.size() < 3) {
            return Lists.newArrayList(input.stream().max(Comparator.comparing(i -> i)).get());
        } else {
            List<Integer> result = Lists.newArrayList();
            int v1 = input.get(0);
            int v2 = input.get(1);
            int v3;
            for (int i = 2; i < input.size(); i++) {
                v3 = input.get(i);
                if (v1 < v2 && v3 < v2) {
                    result.add(v2);
                }
                v1 = v2;
                v2 = v3;
            }
            return result;
        }
    }

    @Test
    public void bugTestWorkFlowTest1() {
        ElevatorUtils.addDestinationFloor(queueDestination, 89, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 47, 50, DirectionForFloorDestination.DOWN);
        ElevatorUtils.addDestinationFloor(queueDestination, 47, 50, DirectionForFloorDestination.UP);
        ElevatorUtils.addDestinationFloor(queueDestination, 32, 50, DirectionForFloorDestination.UP);
        assertEquals(ImmutableList.of(89, 47, 32, 47), queueDestination, "wrong destination queue");
    }

    @Test
    public void bugTestWorkFlowTest2() {
        ElevatorUtils.addDestinationFloor(queueDestination, 89, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 47, 50, DirectionForFloorDestination.DOWN);
        ElevatorUtils.addDestinationFloor(queueDestination, 47, 50, DirectionForFloorDestination.UP);
        ElevatorUtils.addDestinationFloor(queueDestination, 47, 50, DirectionForFloorDestination.DOWN);
        assertEquals(ImmutableList.of(89, 47, 47), queueDestination, "wrong destination queue");
    }

    @Test
    public void bugTestWorkFlowTest3() {
        ElevatorUtils.addDestinationFloor(queueDestination, 94, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 77, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 29, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 94, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 35, 50, DirectionForFloorDestination.DOWN);
        ElevatorUtils.addDestinationFloor(queueDestination, 38, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 73, 50, DirectionForFloorDestination.DOWN);
        ElevatorUtils.addDestinationFloor(queueDestination, 92, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 29, 50, DirectionForFloorDestination.DOWN);
        ElevatorUtils.addDestinationFloor(queueDestination, 35, 50, DirectionForFloorDestination.DOWN);
        assertEquals(ImmutableList.of(77, 92, 94, 73, 38, 35, 29), queueDestination, "wrong destination queue");
    }

    @Test
    public void bugTestWorkFlowTest4() {
        ElevatorUtils.addDestinationFloor(queueDestination, 15, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 11, 50, DirectionForFloorDestination.DOWN);
        ElevatorUtils.addDestinationFloor(queueDestination, 61, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 58, 50, DirectionForFloorDestination.DOWN);
        ElevatorUtils.addDestinationFloor(queueDestination, 15, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 61, 50, DirectionForFloorDestination.DOWN);
        ElevatorUtils.addDestinationFloor(queueDestination, 66, 50, DirectionForFloorDestination.NO_MATTER);
        ElevatorUtils.addDestinationFloor(queueDestination, 28, 50, DirectionForFloorDestination.DOWN);
        ElevatorUtils.addDestinationFloor(queueDestination, 96, 50, DirectionForFloorDestination.DOWN);
        ElevatorUtils.addDestinationFloor(queueDestination, 94, 50, DirectionForFloorDestination.NO_MATTER);
        assertEquals(ImmutableList.of(28, 15, 11, 61, 66, 94, 96, 61, 58), queueDestination, "wrong destination queue");
    }
}
