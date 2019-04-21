package men.chikagostory.elevator.api;

import men.chikagostory.elevator.ElevatorStub;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import men.chikagostory.elevator.model.Elevator;
import men.chikagostory.elevator.model.InlineObject;
import men.chikagostory.elevator.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElevatorsApiControllerTest {

    @Mock
    private ElevatorStub elevatorStub;

    @InjectMocks
    private ElevatorsApiController elevatorsApiController;

    @Test
    void getById() {
        ResponseEntity r1 = elevatorsApiController.getById(1);
        ResponseEntity r2 = elevatorsApiController.getById(2);
        verify(elevatorStub, VerificationModeFactory.times(2)).getElevatorModel();
        assertEquals(HttpStatus.OK, r1.getStatusCode());
        assertEquals(HttpStatus.OK, r2.getStatusCode());
    }

    @Test()
    void getByIdExceptioni() {
        assertThrows(IllegalArgumentException.class, () -> elevatorsApiController.getById(42));
    }

    @Test
    void getAll() {
        ResponseEntity<List<Elevator>> response = elevatorsApiController.getAll();
        verify(elevatorStub, VerificationModeFactory.times(2)).getElevatorModel();
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getPositionById() {
        when(elevatorStub.getPosition()).thenReturn(new Position().nextFloor(777));
        ResponseEntity<Position> response = elevatorsApiController.getPositionById(1);
        verify(elevatorStub, VerificationModeFactory.times(1)).getPosition();
        assertEquals(777, response.getBody().getNextFloor().longValue());
    }

    @Test
    void setPositionForId() {
        ResponseEntity<Void> response = elevatorsApiController.setPositionForId(1, 10, DirectionForFloorDestination.NO_MATTER.toString());
        verify(elevatorStub, VerificationModeFactory.times(1)).addNewDestination(eq(10), eq(DirectionForFloorDestination.NO_MATTER));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void setPositionForIdException() {
        assertThrows(IllegalArgumentException.class, () -> elevatorsApiController.setPositionForId(33, 10,
                DirectionForFloorDestination.NO_MATTER.toString()));
    }

    @Test
    void subscribeOnElevatorEvents() {
        elevatorsApiController.subscribeOnElevatorEvents(new InlineObject().callbackUrl("a/b/c/d").id("zzzzz"));
        verify(elevatorStub, VerificationModeFactory.times(2)).addNextStepListener(any(ElevatorStub.NextStepListener.class));
        verify(elevatorStub, VerificationModeFactory.noMoreInteractions()).removeNextStepListener(any(ElevatorStub.NextStepListener.class));
        elevatorsApiController.subscribeOnElevatorEvents(new InlineObject().callbackUrl("a/b/c/d").id("zzzzz"));
        verify(elevatorStub, VerificationModeFactory.times(4)).addNextStepListener(any(ElevatorStub.NextStepListener.class));
        verify(elevatorStub, VerificationModeFactory.times(2)).removeNextStepListener(any(ElevatorStub.NextStepListener.class));
    }
}