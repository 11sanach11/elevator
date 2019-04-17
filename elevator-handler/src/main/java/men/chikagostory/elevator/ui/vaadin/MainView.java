package men.chikagostory.elevator.ui.vaadin;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import men.chikagostory.elevator.service.ElevatorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;

@SpringComponent
@Route("ui")
public class MainView extends HorizontalLayout {

    private static final Logger log = LoggerFactory.getLogger(MainView.class);
    private int firstFloor = 1;
    private int lastFloor = 15;

    @Autowired
    public MainView() {

        Map<Component, BiMap<Button, Integer>> buttonsMap = Maps.newHashMap();
        VerticalLayout passengerElevatorLayout = new VerticalLayout();
        VerticalLayout freightElevatorLayout = new VerticalLayout();

        ComponentEventListener<ClickEvent<Button>> floorButtonListener = event -> {
            Component elevatorLayout = event.getSource().getParent().get().getParent().get();
            log.info("Click on {} floor for {} elevator", buttonsMap.get(elevatorLayout).get(event.getSource()),
                    Objects.equals(elevatorLayout, freightElevatorLayout) ? "FREIGHT" : "PASSENGER");
        };

        for (VerticalLayout layout : ImmutableList.of(freightElevatorLayout, passengerElevatorLayout)) {
            BiMap<Button, Integer> buttonsDict = HashBiMap.create();
            buttonsMap.put(layout, buttonsDict);
            add(layout);
            HorizontalLayout floorsButtonLayout = null;
            for (int i = firstFloor; i <= lastFloor; i++) {
                if ((i - firstFloor) % 5 == 0) {
                    floorsButtonLayout = new HorizontalLayout();
                    layout.add(floorsButtonLayout);
                }
                Button floorButton = new Button(Integer.valueOf(i).toString());
                buttonsDict.put(floorButton, i);
                floorButton.addClickListener(floorButtonListener);
                floorsButtonLayout.add(floorButton);
            }
            layout.add(floorsButtonLayout);
            layout.setSizeFull();
            layout.setWidth("50%");
            VerticalLayout floorsLayout = new VerticalLayout();
            layout.add(floorsLayout);
            for (int i = firstFloor; i <= lastFloor; i++) {
                HorizontalLayout floorLayout = new HorizontalLayout();
                VerticalLayout buttonLayout = new VerticalLayout();
                Button buttonUp = new Button();
                buttonUp.setIcon(VaadinIcon.ANGLE_UP.create());
                buttonLayout.add(buttonUp);
                Button buttonDown = new Button();
                buttonDown.setIcon(VaadinIcon.ANGLE_DOWN.create());
                buttonLayout.add(buttonDown);
                floorLayout.add(buttonLayout);
                Label label = new Label("Этаж " + i);
                label.setSizeFull();
                floorLayout.add(label);
                floorsLayout.add(floorLayout);
            }
        }
    }

}
