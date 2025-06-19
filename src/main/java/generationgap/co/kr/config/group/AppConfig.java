package generationgap.co.kr.config.group;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean // 이 메서드가 반환하는 RestTemplate 객체를 Spring 빈으로 등록
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
