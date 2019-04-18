package men.chikagostory.elevator.controller;

import men.chikagostory.elevator.model.ElevatorEvent;
import men.chikagostory.elevator.ui.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class UIEventsHandler {

    private MainView mainView;

    @Autowired
    public UIEventsHandler(MainView mainView) {
        this.mainView = mainView;
    }

    @PostMapping("v1/events/elevator")
    public void elevatorEvent(@RequestBody ElevatorEvent elevatorEvent) {
        mainView.updateFloorForElevator(elevatorEvent);
    }
}
