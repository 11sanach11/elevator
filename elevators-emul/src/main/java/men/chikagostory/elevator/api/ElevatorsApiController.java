package men.chikagostory.elevator.api;

import com.google.common.collect.Lists;
import men.chikagostory.elevator.ElevatorStub;
import men.chikagostory.elevator.model.Elevator;
import men.chikagostory.elevator.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2019-04-14T14:11:53.971+07:00[Asia/Barnaul]")

@Controller
@RequestMapping("${openapi.elevator.base-path:/elevator/v1}")
public class ElevatorsApiController implements ElevatorsApi {

    private final NativeWebRequest request;
    private ElevatorStub passengerElevator;
    private ElevatorStub freightElevator;

    @Autowired
    public ElevatorsApiController(NativeWebRequest request, ElevatorStub passengerElevator, ElevatorStub freightElevator) {
        this.request = request;
        this.passengerElevator = passengerElevator;
        this.freightElevator = freightElevator;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<Elevator> getById(Integer id) {
        switch (id) {
            case 1:
                return ResponseEntity.ok(passengerElevator.getElevatorModel());
            case 2:
                return ResponseEntity.ok(freightElevator.getElevatorModel());
            default:
                return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<List<Elevator>> getAll() {
        return ResponseEntity.ok(Lists.newArrayList(passengerElevator.getElevatorModel(), freightElevator.getElevatorModel()));
    }

    @Override
    public ResponseEntity<Position> getPositionById(Integer id) {
        switch (id) {
            case 1:
                return ResponseEntity.ok(passengerElevator.getPosition());
            case 2:
                return ResponseEntity.ok(freightElevator.getPosition());
            default:
                return ResponseEntity.badRequest().build();
        }
    }
}
