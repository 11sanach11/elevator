package men.chikagostory.elevator;

import men.chikagostory.elevator.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import men.chikagostory.elevator.api.ElevatorsApi;

@Configuration
public class MainConfiguration {

    @Bean
    public ApiClient apiClient() {
        return new ApiClient();
    }

    @Bean
    public ElevatorsApi elevatorsApi(ApiClient apiClient) {
        return apiClient.createService(ElevatorsApi.class);
    }
}
