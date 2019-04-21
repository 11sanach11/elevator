package men.chikagostory.elevator.service;

import men.chikagostory.elevator.api.ElevatorsApi;
import men.chikagostory.elevator.model.DirectionForFloorDestination;
import men.chikagostory.elevator.model.Elevator;
import men.chikagostory.elevator.model.Position;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.MockitoJUnitRunner;
import retrofit2.Call;
import retrofit2.Response;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElevatorHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ElevatorsApi elevatorsApi;

    @InjectMocks
    private ElevatorHandler elevatorHandler;

    @Test
    public void positionRequestForElevatorNullDirection() {
        elevatorHandler.positionRequestForElevator(44, 10, null);
        verify(elevatorsApi, VerificationModeFactory.times(1)).setPositionForId(eq(44), eq(10),
                eq(DirectionForFloorDestination.NO_MATTER.toString()));
    }

    @Test
    public void positionRequestForElevatorUpDirection() {
        elevatorHandler.positionRequestForElevator(44, 10, DirectionForFloorDestination.UP);
        verify(elevatorsApi, VerificationModeFactory.times(1)).setPositionForId(eq(44), eq(10),
                eq(DirectionForFloorDestination.UP.toString()));
    }

    @Test
    public void positionRequestForFloor() {
        when(elevatorsApi.getAll()).thenAnswer(invocation -> {
            Call call = Mockito.mock(Call.class);
            when(call.execute()).thenReturn(Response.success(Lists.newArrayList(new Elevator().id(98).firstFloor(1).lastFloor(13).speedByFloor(45))));
            return call;
        });
        when(elevatorsApi.getPositionById(eq(98))).thenAnswer(invocation -> {
            Call call = Mockito.mock(Call.class);
            when(call.execute()).thenReturn(
                    Response.success(new Position().direction(DirectionForFloorDestination.UP).maxFloorInLoop(12).nextFloor(10).previousFloor(9)));
            return call;
        });
        elevatorHandler.positionRequestForFloor(1, DirectionForFloorDestination.DOWN);
        verify(elevatorsApi, VerificationModeFactory.times(1)).
                setPositionForId(eq(98), eq(1), eq(DirectionForFloorDestination.DOWN.toString()));

    }
}