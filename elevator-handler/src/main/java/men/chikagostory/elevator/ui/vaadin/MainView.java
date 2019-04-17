package men.chikagostory.elevator.ui.vaadin;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("ui")
public class MainView extends VerticalLayout {
    public MainView() {
        add(new TextField());
    }
}
