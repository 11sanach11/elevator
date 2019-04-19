package men.chikagostory.elevator.invoker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"men.chikagostory.elevator.invoker", "men.chikagostory.elevator.api", "org.openapitools.configuration"})
public class EmulApplication {

    public static void main(String[] args) {
        new SpringApplication(EmulApplication.class).run(args);
    }

}
