package men.chikagostory.elevator.ui.vaadin;

import com.google.common.collect.Maps;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import men.chikagostory.elevator.api.ElevatorsApi;
import men.chikagostory.elevator.controller.UIEventsHandler;
import men.chikagostory.elevator.model.*;
import men.chikagostory.elevator.service.ElevatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringComponent
@UIScope
@Route("ui")
@Push
public class SimulationView extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(SimulationView.class);
    private Map<Integer, Map<Integer, VerticalLayout>> floorsElevatorsLayout = Maps.newConcurrentMap();
    private Map<Integer, VerticalLayout> currentCabinFloorForElevator = Maps.newConcurrentMap();
    private transient ElevatorsApi elevatorsApi;
    private transient ElevatorHandler elevatorHandler;
    private Integer serverPort;
    private transient UIEventsHandler uiEventsHandler;

    @Autowired
    public SimulationView(ElevatorsApi elevatorsApi, ElevatorHandler elevatorHandler, UIEventsHandler uiEventsHandler,
                          @Value("${server.port}") Integer serverPort) {
        UI.getCurrent().getSession().setErrorHandler(event -> {
            log.info("Error :", event.getThrowable());
            UI.getCurrent().access(() -> Notification.show("Ошибка при обращении к сервису эмулятора лифта: " + event.getThrowable().getLocalizedMessage() +
                    "\nПопробуйте обновить " +
                    "страницу.", 0, Notification.Position.MIDDLE));
        });

        this.uiEventsHandler = uiEventsHandler;
        this.uiEventsHandler.setMainView(this);
        this.elevatorsApi = elevatorsApi;
        this.elevatorHandler = elevatorHandler;
        this.serverPort = serverPort;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {

        super.onAttach(attachEvent);
        List<Elevator> elevators;
        try {
            elevators = elevatorsApi.getAll().execute().body();
            elevatorsApi.subscribeOnElevatorEvents(
                    new InlineObject().callbackUrl("http://localhost:" + serverPort + "/v1" + "/events" + "/elevator")
                            .id(VaadinSession.getCurrent().getSession().getId())).execute().isSuccessful();
        } catch (Exception e) {
            log.error("Can't get into from emul elevator service", e);
            Notification.show("Не удается получить информацию с эмулятора лифта. Проверьте запущен ли он. Для продолжение перезапустите страницу.", 0,
                    Notification.Position.MIDDLE);
            return;
        }
        HorizontalLayout floorsElevatorButtonsLayout = new HorizontalLayout();
        add(floorsElevatorButtonsLayout);
        for (Elevator elevator : elevators) {
            VerticalLayout elevatorLayout = new VerticalLayout();
            floorsElevatorButtonsLayout.add(elevatorLayout);
            HorizontalLayout floorsButtonLayout = null;
            for (int floor = elevator.getFirstFloor(); floor <= elevator.getLastFloor(); floor++) {
                if ((floor - elevator.getFirstFloor()) % 5 == 0) {
                    floorsButtonLayout = new HorizontalLayout();
                    elevatorLayout.add(floorsButtonLayout);
                }
                Button floorButton = new Button(Integer.toString(floor));
                int finalFloor = floor;
                floorButton.addClickListener(event -> elevatorHandler.positionRequestForElevator(elevator.getId(), finalFloor,
                        DirectionForFloorDestination.NO_MATTER));
                Optional.ofNullable(floorsButtonLayout).ifPresent(layout -> layout.add(floorButton));
            }
        }
        //считаю, что количество этажей для каждого лифта одинаково, и беру значения этажности только из одного

        int firstFloor = elevators.get(0).getFirstFloor();
        int lastFloor = elevators.get(0).getLastFloor();
        for (Integer floor = lastFloor; floor >= firstFloor; floor--) {
            HorizontalLayout floorLayout = new HorizontalLayout();
            floorLayout.setAlignItems(Alignment.CENTER);
            add(floorLayout);
            //метка с номером этажа
            Label label = new Label(floor.toString());
            floorLayout.add(label);
            //кабины лифтов на этажах
            Map<Integer, VerticalLayout> elevatorOnFloorLayoutMap = Maps.newConcurrentMap();
            floorsElevatorsLayout.put(floor, elevatorOnFloorLayoutMap);
            elevators.forEach(elevator -> {
                VerticalLayout cabinLayout = new VerticalLayout();
                elevatorOnFloorLayoutMap.put(elevator.getId(), cabinLayout);
                cabinLayout.add(getCabinFloorIcon(true));
                floorLayout.add(cabinLayout);
                floorLayout.add(cabinLayout);
            });
            //кнопки вызова лифта
            VerticalLayout buttonLayout = new VerticalLayout();
            if (floor != lastFloor) {
                Button buttonUp = new Button();
                buttonUp.setIcon(VaadinIcon.ARROW_CIRCLE_UP.create());
                int finalFloorUp = floor;
                buttonUp.addClickListener(event -> elevatorHandler.positionRequestForFloor(finalFloorUp, DirectionForFloorDestination.UP));
                buttonLayout.add(buttonUp);
            }
            if (floor != firstFloor) {
                Button buttonDown = new Button();
                buttonDown.setIcon(VaadinIcon.ARROW_CIRCLE_DOWN.create());
                int finalFloorDown = floor;
                buttonDown.addClickListener(event -> elevatorHandler.positionRequestForFloor(finalFloorDown, DirectionForFloorDestination.DOWN));
                buttonLayout.add(buttonDown);
            }
            floorLayout.add(buttonLayout);

        }
        try {
            for (Elevator elevator : elevators) {
                Position position = elevatorsApi.getPositionById(elevator.getId()).execute().body();
                updateElevatorCabinPresentation(elevator.getId(), position.getPreviousFloor());
            }
        } catch (IOException e) {
            throw new RuntimeException("can't get position", e);
        }

    }

    private void updateElevatorCabinPresentation(Integer elevatorId, Integer onFloor) {
        Optional.ofNullable(currentCabinFloorForElevator.get(elevatorId)).ifPresent(old -> {
            old.removeAll();
            old.add(getCabinFloorIcon(true));
        });
        Optional.ofNullable(floorsElevatorsLayout.get(onFloor).get(elevatorId)).ifPresent(current -> {
            current.removeAll();
            current.add(getCabinFloorIcon(false));
            currentCabinFloorForElevator.put(elevatorId, current);
        });
    }

    private Icon getCabinFloorIcon(boolean empty) {
        Icon cabinIcon = empty ? VaadinIcon.THIN_SQUARE.create() : VaadinIcon.STOP.create();
        cabinIcon.setSize("40%");
        return cabinIcon;
    }

    public void updateFloorForElevator(ElevatorEvent event) {
        log.info("event from emulator: {}", event.getDescription());
        getUI().ifPresent(ui -> ui.access(() -> updateElevatorCabinPresentation(event.getId(), event.getOnFloor())));
    }

}
