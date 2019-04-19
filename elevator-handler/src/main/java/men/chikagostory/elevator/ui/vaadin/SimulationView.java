package men.chikagostory.elevator.ui.vaadin;

import com.google.common.collect.Maps;
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

    private UIEventsHandler uiEventsHandler;

    @Autowired
    public SimulationView(ElevatorsApi elevatorsApi, ElevatorHandler handler, UIEventsHandler uiEventsHandler, @Value("${server.port}") Integer serverPort) throws IOException {

        this.uiEventsHandler = uiEventsHandler;
        this.uiEventsHandler.setMainView(this);
        boolean successRemoteCall = false;
        List<Elevator> elevators = null;
        try {
            elevators = elevatorsApi.getAll().execute().body();
            successRemoteCall = elevatorsApi.subscribeOnElevatorEvents(
                    new InlineObject().callbackUrl("http://localhost:" + serverPort + "/v1" + "/events" + "/elevator")
                            .id(VaadinSession.getCurrent().getSession().getId())).execute().isSuccessful();
        } catch (Exception e) {
            log.warn("Can't get into from emul elevator service", e);
        }
        if (!successRemoteCall) {
            Notification.show("Не удалось подключиться к эмулятору лифта! Попробуйте обновить страницу", 0, Notification.Position.MIDDLE);
        } else {
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
                    Button floorButton = new Button(Integer.valueOf(floor).toString());
                    int finalFloor = floor;
                    floorButton.addClickListener(event -> {
                        handler.positionRequestForElevator(elevator.getId(), finalFloor, DirectionForFloorDestination.NO_MATTER);
                    });
                    floorsButtonLayout.add(floorButton);
                }
            }
            //считаю, что количество этажей для каждого лифта одинаково, и беру значения этажности только из одного

            int firstFloor = elevators.get(0).getFirstFloor();
            int lastFloor = elevators.get(0).getLastFloor();
            for (Integer floor = firstFloor; floor <= lastFloor; floor++) {
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
                if (floor != firstFloor) {
                    Button buttonUp = new Button();
                    buttonUp.setIcon(VaadinIcon.ARROW_CIRCLE_UP.create());
                    int finalFloorUp = floor;
                    buttonUp.addClickListener(event -> {
                        handler.positionRequestForFloor(finalFloorUp, DirectionForFloorDestination.UP);
                    });
                    buttonLayout.add(buttonUp);
                }
                if (floor != lastFloor) {
                    Button buttonDown = new Button();
                    buttonDown.setIcon(VaadinIcon.ARROW_CIRCLE_DOWN.create());
                    int finalFloorDown = floor;
                    buttonDown.addClickListener(event -> {
                        handler.positionRequestForFloor(finalFloorDown, DirectionForFloorDestination.DOWN);
                    });
                    buttonLayout.add(buttonDown);
                }
                floorLayout.add(buttonLayout);

            }
            for (Elevator elevator : elevators) {
                Position position = elevatorsApi.getPositionById(elevator.getId()).execute().body();
                updateElevatorCabinPresentation(elevator.getId(), position.getPreviousFloor());
            }
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
        cabinIcon.setSize("80%");
        return cabinIcon;
    }

    public void updateFloorForElevator(ElevatorEvent event) {
        log.info("event from emulator: {}", event.getDescription());
        getUI().ifPresent(ui -> ui.access(() -> {
            updateElevatorCabinPresentation(event.getId(), event.getOnFloor());
        }));
    }

}
