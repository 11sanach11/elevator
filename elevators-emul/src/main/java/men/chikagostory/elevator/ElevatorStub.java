package men.chikagostory.elevator;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiFunction;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import men.chikagostory.elevator.internal.ElevatorUtils;
import men.chikagostory.elevator.internal.domain.MotionInfo;
import men.chikagostory.elevator.model.Elevator;
import men.chikagostory.elevator.model.Position;

/**
 * Класс должен эмулировать движение лифта. При инициализации ему задаются параметры, в соответствии с которыми, при участии рандома, кабина "инициализирует"
 * некоторое состояние, с которого начинает работать. Предполагается, что лифт знает об этажах и самостояетельно накапливает очередь будущих остановок.
 */
public class ElevatorStub {

    private static final Logger log = LoggerFactory.getLogger(ElevatorStub.class);

    private int id;
    private String name;
    private int maxWeight;
    /**
     * Минимальный этаж, доступный лифту
     */
    private int firstFloor;
    /**
     * Максимальный этаж, доступный лифту
     */
    private int lastFloor;
    /**
     * Задержка на переход между состояниями
     */
    private int changeStateDuration;
    /**
     * За это время кабина проезжает один этаж
     */
    private int speedByFloor;

    private Elevator.TypeEnum type;

    private AtomicReference<MotionInfo> currentState = new AtomicReference<>();

    private Thread emulationThread;

    /**
     * Флаг, если установлен, поток при следующей работе завершит свою работу
     */
    private boolean stopEmulation;

    /**
     * Кабину, если очередь ее посещений пуста, можно поставить на паузу. Стоять она на паузе будет до тех пор, пока ее не сделают resume или не
     * остановят эмуляцию.
     */
    private boolean onPause = false;

    private BiFunction<Integer, Integer, MotionInfo> initMotionFunc = null;

    public ElevatorStub(int id, String name, int maxWeight, int firstFloor, int lastFloor, int changeStateDuration, int speedByFloor, Elevator.TypeEnum type) {
        Assert.isTrue(lastFloor - firstFloor >= 2, "minimai count of floors in building is 2");
        this.id = id;
        this.name = name;
        this.maxWeight = maxWeight;
        this.firstFloor = firstFloor;
        this.lastFloor = lastFloor;
        this.changeStateDuration = changeStateDuration;
        this.speedByFloor = speedByFloor;
        this.type = type;
    }

    public void setInitMotionFunc(BiFunction<Integer, Integer, MotionInfo> initMotionFunc) {
        this.initMotionFunc = initMotionFunc;
    }

    @PostConstruct
    public ElevatorStub startEmulation() {
        stopEmulation = false;
        emulationThread = new Thread(() -> {
            MotionInfo info = Optional.ofNullable(initMotionFunc).orElse(ElevatorUtils.randomMotionInfo).apply(firstFloor, lastFloor);
            log.info("Start state for elevator #{} {}: {}", id, name, info);
            currentState.set(info);
            try {
                while (!Thread.interrupted() && !stopEmulation) {
                    while (!CollectionUtils.isEmpty(currentState.get().getDestinationQueue())) {
                        MotionInfo newState = currentState.updateAndGet(state -> {
                            Integer nextDestination = state.getDestinationQueue().peek();
                            //Если следующий этаж совпадает с этажом назначения - нужна дополнительная логика
                            if (Objects.equals(nextDestination, state.getNextFloor())) {
                                //Если текущее состояние - остановка - нужно кабину двинуть дальше в соответствии с состоянием очереди
                                if (state.getState() == Position.StateEnum.STAY) {
                                    int previousDestination = state.getDestinationQueue().pollFirst();
                                    Optional.ofNullable(state.getDestinationQueue().peek()).ifPresent(newDestination -> {
                                        state.setDelay(changeStateDuration);
                                        state.setPreviousFloor(previousDestination);
                                        if (newDestination > previousDestination) {
                                            //лифт поедет вверх
                                            state.setNextFloor(previousDestination + 1);
                                            state.setState(Position.StateEnum.UP);
                                        } else if (newDestination < previousDestination) {
                                            //лифт поедет вниз
                                            state.setNextFloor(previousDestination - 1);
                                            state.setState(Position.StateEnum.DOWN);
                                        } else {
                                            String message = "Wrong value! Destination list can't get two identity destination in range!";
                                            log.error(message);
                                            log.error("Wrong state for #{}: {}", id, state);
                                            throw new RuntimeException(message);
                                        }
                                    });
                                } else {
                                    //нужно перевести кабину в состояние остановка
                                    state.setPreviousFloor(state.getNextFloor());
                                    state.setState(Position.StateEnum.STAY);
                                    state.setDelay(changeStateDuration);
                                }
                            } else {
                                if (state.getState() == Position.StateEnum.STAY) {
                                    // для состояния остановки нужно поменять состояние и поменять следующий этаж.
                                    int koef = 0;
                                    if (state.getNextFloor() < nextDestination) {
                                        //лифт едет вверх
                                        state.setState(Position.StateEnum.UP);
                                        koef = 1;
                                    } else if (nextDestination < state.getNextFloor()) {
                                        //лифт едет вниз
                                        state.setState(Position.StateEnum.DOWN);
                                        koef = -1;
                                    }
                                    state.setNextFloor(state.getNextFloor() + koef);
                                    state.setDelay(speedByFloor);
                                } else {
                                    //иначе нужно "подвинуть" текущие этажи
                                    int koef = state.getState() == Position.StateEnum.UP ? 1 : -1;
                                    state.setNextFloor(state.getNextFloor() + koef);
                                    state.setPreviousFloor(state.getPreviousFloor() + koef);
                                    state.setDelay(speedByFloor);
                                }
                            }
                            return state;
                        });
                        log.trace("State for #{}: {}", id, newState);
                        Thread.sleep(newState.getDelay());
                    }
                    //Проверяю, может лифт на паузе...
                    while (onPause && !stopEmulation) {
                        log.info("set stub #{} on pause.");
                        LockSupport.park();
                    }
                    //Если больше этажей нет - ждем внешних событий на их добавление.
                    LockSupport.park();
                }
            } catch (InterruptedException e) {
                log.info("Interrupt execution: {}", e.getMessage());
            }
        });
        emulationThread.setName("el_" + id);
        emulationThread.setDaemon(true);
        emulationThread.start();
        return this;
    }

    public void addNewDestination(int toFloor) {
        Assert.isTrue(toFloor >= firstFloor, "can't go underground");
        Assert.isTrue(toFloor <= lastFloor, "can't go to the sky");
        Assert.notNull(emulationThread, "Emulation thread is null, it's impossible");
        currentState.updateAndGet(current -> {
            //хитрый алгоритм добавления этажа по "пути", если это возможно, а пока просто добавляю этаж в конец, что конечно неправильно
            log.warn("хитрый алгоритм добавления этажа по \"пути\", если это возможно, а пока просто добавляю этаж в конец, что конечно неправильно");
            if (!Objects.equals(current.getDestinationQueue().peekLast(), toFloor)) {
                //Если добавляемый этаж совпадает с последним - нет смысла его добавлять - на нем и так будет остановка
                current.getDestinationQueue().add(toFloor);
            }
            return current;
        });
        LockSupport.unpark(emulationThread);
    }

    public Elevator getElevatorModel() {
        return new Elevator().id(id).name(name).maxWeight(maxWeight).speedByFloor(speedByFloor).changeStateDuration(changeStateDuration).type(type);
    }

    public Position getPosition() {
        MotionInfo state = currentState.get();
        return new Position().nextFloor(state.getNextFloor()).previousFloor(state.getPreviousFloor()).state(state.getState());
    }

    public void stopEmulation() {
        this.stopEmulation = true;
        LockSupport.unpark(emulationThread);
    }

}
