package men.chikagostory.elevator.internal.domain;

import lombok.Builder;
import lombok.Data;
import men.chikagostory.elevator.model.Position;

import java.util.LinkedList;

@Data
@Builder
public class MotionInfo {
    /**
     * ID лифта
     */
    private int id;
    /**
     * Режим, в котром находится кабина
     */
    private Position.StateEnum state;
    /**
     * Предыдущий текущему этаж, если лифт находится в движении, или тот, на котором стоит кабина, в режиме остановки
     */
    private int previousFloor;
    /**
     * Следующий за текущим этаж, если лифт находится в движении, или тот, на котором стоит кабина, в режиме остановки
     */
    private int nextFloor;
    /**
     * Очередь будущих этажей, на которые должна попасть кабина
     */
    private LinkedList<Integer> destinationQueue = new LinkedList<>();
    /**
     * Время, которое будет выполняться следующий цикл работы лифта.
     */
    private int delay = 0;
}