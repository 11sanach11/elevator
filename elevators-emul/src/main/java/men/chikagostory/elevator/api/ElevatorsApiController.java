package men.chikagostory.elevator.api;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.NativeWebRequest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import men.chikagostory.elevator.ElevatorStub;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import men.chikagostory.elevator.model.Elevator;
import men.chikagostory.elevator.model.ElevatorEvent;
import men.chikagostory.elevator.model.InlineObject;
import men.chikagostory.elevator.model.Position;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2019-04-14T14:11:53.971+07:00[Asia/Barnaul]")

@Controller
@RequestMapping("${openapi.elevator.base-path:/elevator/v1}")
public class ElevatorsApiController implements ElevatorsApi {

    private static final Logger log = LoggerFactory.getLogger(ElevatorsApiController.class);

    private final NativeWebRequest request;
    private ElevatorStub passengerElevator;
    private ElevatorStub freightElevator;

    private Map<String, ElevatorStub.NextStepListener> eventHandler = Maps.newConcurrentMap();
    private RestTemplate restTemplate = new RestTemplate();

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

    private ElevatorStub getElevator(Integer id) {
        Assert.isTrue(id == 1 || id == 2, "Support only '1' and '2' ids");
        switch (id) {
        case 1:
            return passengerElevator;
        case 2:
            return freightElevator;
        default:
            return null;
        }
    }

    @Override
    public ResponseEntity<Elevator> getById(Integer id) {
        return ResponseEntity.ok(getElevator(id).getElevatorModel());
    }

    @Override
    public ResponseEntity<List<Elevator>> getAll() {
        return ResponseEntity.ok(Lists.newArrayList(passengerElevator.getElevatorModel(), freightElevator.getElevatorModel()));
    }

    @Override
    public ResponseEntity<Position> getPositionById(Integer id) {
        return ResponseEntity.ok(getElevator(id).getPosition());
    }

    @Override
    public ResponseEntity<Void> setPositionForId(Integer id, Integer floor, @Valid String direction) {
        DirectionForFloorDestination directionForFloorDestination = DirectionForFloorDestination.fromValue(direction);
        ElevatorStub elevatorStub = getElevator(id);
        elevatorStub.addNewDestination(floor, directionForFloorDestination);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> subscribeOnElevatorEvents(@Valid InlineObject inlineObject) {
        log.info("Subscribe elevator enum events for: {}", inlineObject);
        if (StringUtils.isNotEmpty(inlineObject.getCallbackUrl()) && StringUtils.isNotEmpty(inlineObject.getId())) {
            if (eventHandler.containsKey(inlineObject.getId())) {
                ElevatorStub.NextStepListener old = eventHandler.get(inlineObject.getId());
                freightElevator.removeNextStepListener(old);
                passengerElevator.removeNextStepListener(old);
                eventHandler.remove(old);
            }
            ElevatorStub.NextStepListener eventListener = event -> restTemplate.postForObject(inlineObject.getCallbackUrl(),
                    new ElevatorEvent().description(event.toString()).id(event.getId()).onFloor(event.getPreviousFloor()), Void.class);
            freightElevator.addNextStepListener(eventListener);
            passengerElevator.addNextStepListener(eventListener);
            eventHandler.put(inlineObject.getId(), eventListener);
        }
        return ResponseEntity.created(URI.create(inlineObject.getCallbackUrl())).build();
    }
}
