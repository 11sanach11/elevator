package men.chikagostory.elevator.invoker;

import men.chikagostory.elevator.ElevatorStub;
import men.chikagostory.elevator.model.Elevator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmulConfiguration {
    @Bean
    ElevatorStub passengerElevator(@Value("${elevator.emulation.passenger.max-weight}") int maxWeight,
                                   @Value("${elevator.emulation.building.first-floor}") int firstFloor,
                                   @Value("${elevator.emulation.building.last-floor}") int lastFloor,
                                   @Value("${elevator.emulation.common.change-state-duration}") int changeStateDuration,
                                   @Value("${elevator.emulation.common.speed-by-floor}") int speedByFloor) {
        return new ElevatorStub(1, "PASSENGER", maxWeight, firstFloor, lastFloor, changeStateDuration, speedByFloor, Elevator.TypeEnum.PASSENGER);
    }

    @Bean
    ElevatorStub freightElevator(@Value("${elevator.emulation.freight.max-weight}") int maxWeight,
                                 @Value("${elevator.emulation.building.first-floor}") int firstFloor,
                                 @Value("${elevator.emulation.building.last-floor}") int lastFloor,
                                 @Value("${elevator.emulation.common.change-state-duration}") int changeStateDuration,
                                 @Value("${elevator.emulation.common.speed-by-floor}") int speedByFloor) {
        return new ElevatorStub(2, "FREIGHT", maxWeight, firstFloor, lastFloor, changeStateDuration, speedByFloor, Elevator.TypeEnum.FREIGHT);
    }
}
