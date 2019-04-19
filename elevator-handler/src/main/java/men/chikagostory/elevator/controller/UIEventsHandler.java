package men.chikagostory.elevator.controller;

import java.util.Optional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import men.chikagostory.elevator.model.ElevatorEvent;
import men.chikagostory.elevator.ui.vaadin.SimulationView;

@RestController()
public class UIEventsHandler {

    private SimulationView mainView;

    public UIEventsHandler() {
    }

    @PostMapping("v1/events/elevator")
    public void elevatorEvent(@RequestBody ElevatorEvent elevatorEvent) {

        Optional.ofNullable(mainView).ifPresent(view -> view.updateFloorForElevator(elevatorEvent));
    }

    public void setMainView(SimulationView mainView) {
        this.mainView = mainView;
    }
}
